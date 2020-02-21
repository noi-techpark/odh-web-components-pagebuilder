package it.bz.opendatahub.webcomponentspagebuilder.ui.controllers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;
import com.vaadin.flow.component.notification.Notification;

import it.bz.opendatahub.webcomponentspagebuilder.data.Domain;
import it.bz.opendatahub.webcomponentspagebuilder.data.DomainsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageContent;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationAction;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationStatus;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageWidget;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PagePublicationRepository;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageVersionRepository;
import it.bz.opendatahub.webcomponentspagebuilder.deployment.DeploymentTask;
import it.bz.opendatahub.webcomponentspagebuilder.deployment.PublishDeploymentTask;
import it.bz.opendatahub.webcomponentspagebuilder.deployment.UnpublishDeploymentTask;
import it.bz.opendatahub.webcomponentspagebuilder.events.PagePublicationUpdated;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.PageRenderer;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.DangerAwareConfirmDialog;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.PublishPageVersionDialog;

/**
 * Controller for handling the various publishing-related actions throughout the
 * application.
 * 
 * @author danielrampanelli
 */
@Component
public class PublishingController {

	@FunctionalInterface
	public static interface PublishHandler {
		public void published(Page page, PageVersion pageVersion);
	}

	@FunctionalInterface
	public static interface DiscardHandler {
		public void discarded(Page page);
	}

	@FunctionalInterface
	public static interface UnpublishHandler {
		public void unpublished(Page page);
	}

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	PageRepository pagesRepo;

	@Autowired
	PageVersionRepository versionsRepo;

	@Autowired
	PagePublicationRepository publicationsRepo;

	@Autowired
	DomainsProvider domainsProvider;

	@Autowired
	PageRenderer pageRenderer;

	@Autowired
	EventBus eventBus;

	// TODO allow configuration of the number of threads
	private ExecutorService executor = Executors.newFixedThreadPool(3);

	private Map<UUID, DeploymentTask> tasks = Collections.synchronizedMap(new HashMap<>());

	@PostConstruct
	private void postConstruct() {
		// TODO resume any non-completed publication actions
	}

	private PageVersion publishPageVersion(PageVersion pageVersion) {
		PageVersion publishedVersion = new PageVersion();
		publishedVersion.setPage(pageVersion.getPage());
		publishedVersion.setHash(DigestUtils.sha1Hex(UUID.randomUUID().toString()));
		publishedVersion.setUpdatedAt(LocalDateTime.now());

		publishedVersion.setTitle(publishedVersion.getTitle());
		publishedVersion.setDescription(publishedVersion.getDescription());

		publishedVersion.setContents(pageVersion.getContents().stream().map(pageContent -> {
			PageContent copy = pageContent.copy();
			copy.setPageVersion(publishedVersion);

			return copy;
		}).collect(Collectors.toList()));

		publishedVersion.setWidgets(pageVersion.getWidgets().stream().map(pageWidget -> {
			PageWidget copy = pageWidget.copy();
			copy.setPageVersion(publishedVersion);

			return copy;
		}).collect(Collectors.toList()));

		return versionsRepo.save(publishedVersion);
	}

	public PageVersion createDraft(Page page) {
		PageVersion draftVersion = new PageVersion();
		draftVersion.setPage(page);
		draftVersion.setHash(DigestUtils.sha1Hex(UUID.randomUUID().toString()));
		draftVersion.setUpdatedAt(LocalDateTime.now());

		if (page.getPublicVersion() != null) {
			PageVersion publicVersion = page.getPublicVersion();

			draftVersion.setTitle(publicVersion.getTitle());
			draftVersion.setDescription(publicVersion.getDescription());

			draftVersion.setContents(publicVersion.getContents().stream().map(pageContent -> {
				PageContent copy = pageContent.copy();
				copy.setPageVersion(draftVersion);

				return copy;
			}).collect(Collectors.toList()));

			draftVersion.setWidgets(publicVersion.getWidgets().stream().map(pageWidget -> {
				PageWidget copy = pageWidget.copy();
				copy.setPageVersion(draftVersion);

				return copy;
			}).collect(Collectors.toList()));
		}

		return draftVersion;
	}

	public void publish(PageVersion pageVersion, PublishHandler handler) {
		Page page = pageVersion.getPage();

		if (page.getPublication() == null) {
			PublishPageVersionDialog dialog = applicationContext.getBean(PublishPageVersionDialog.class);

			dialog.setConfirmHandler((configuration) -> {
				PageVersion publishedVersion = publishPageVersion(pageVersion);

				PagePublication pagePublication = new PagePublication();
				pagePublication.setPage(page);
				pagePublication.setAction(PagePublicationAction.PUBLISH);
				pagePublication.setStatus(PagePublicationStatus.PENDING);
				pagePublication.setDeployedAt(LocalDateTime.now());
				pagePublication.setVersion(publishedVersion);

				pagePublication.setDomainName(configuration.getDomain().getHostName());

				Domain domain = configuration.getDomain();

				if (domain.getAllowSubdomains()
						&& (configuration.getSubdomain() != null && !configuration.getSubdomain().equals(""))) {
					pagePublication.setSubdomainName(configuration.getSubdomain());
				}

				if (configuration.getPath() != null && !configuration.getPath().equals("")) {
					pagePublication.setPathName(configuration.getPath());
				}

				pagePublication = publicationsRepo.save(pagePublication);

				page.removeVersion(page.getDraftVersion());
				page.setDraftVersion(null);

				page.setPublicVersion(publishedVersion);

				page.addPublication(pagePublication);
				page.setPublication(pagePublication);

				Page updatedPage = pagesRepo.save(page);

				handler.published(updatedPage, publishedVersion);

				process(pagePublication);

				Notification.show("Page draft published.");
			});

			dialog.open();
		} else {
			PageVersion publishedVersion = publishPageVersion(pageVersion);

			PagePublication pagePublication = page.getPublication().copy();
			pagePublication.setAction(PagePublicationAction.PUBLISH);
			pagePublication.setStatus(PagePublicationStatus.PENDING);
			pagePublication.setDeployedAt(LocalDateTime.now());
			pagePublication.setVersion(publishedVersion);

			pagePublication = publicationsRepo.save(pagePublication);

			page.removeVersion(page.getDraftVersion());
			page.setDraftVersion(null);

			page.setPublicVersion(publishedVersion);

			page.setPublication(pagePublication);

			Page updatedPage = pagesRepo.save(page);

			handler.published(updatedPage, publishedVersion);

			process(pagePublication);

			Notification.show("Page draft published.");
		}
	}

	public void discard(PageVersion pageVersion, DiscardHandler handler) {
		Page page = pageVersion.getPage();

		DangerAwareConfirmDialog.create().withTitle("DISCARD")
				.withMessage("Are you sure you want to discard this page draft?").withAction("DISCARD")
				.withHandler(() -> {
					page.removeVersion(page.getDraftVersion());
					page.setDraftVersion(null);

					Page updatedPage = pagesRepo.save(page);

					handler.discarded(updatedPage);

					Notification.show("Page draft discarded.");
				}).open();
	}

	public void unpublish(PageVersion pageVersion, UnpublishHandler handler) {
		Page page = pageVersion.getPage();

		DangerAwareConfirmDialog.create().withTitle("UNPUBLISH PAGE").withMessage(
				"Are you sure you want to unpublish this page? Afterwards the page won't be available to visitors anymore.")
				.withAction("UNPUBLISH").withHandler(() -> {
					PagePublication updatedPublication = page.getPublication().copy();
					updatedPublication.setAction(PagePublicationAction.UNPUBLISH);
					updatedPublication.setStatus(PagePublicationStatus.PENDING);
					updatedPublication.setDeployedAt(LocalDateTime.now());

					page.addPublication(updatedPublication);
					page.setPublication(updatedPublication);

					Page updatedPage = pagesRepo.save(page);

					if (updatedPage.getDraftVersion() == null) {
						updatedPage.setDraftVersion(createDraft(page));
						updatedPage.addVersion(updatedPage.getDraftVersion());
					}

					updatedPage.setPublicVersion(null);

					updatedPage = pagesRepo.save(updatedPage);

					handler.unpublished(updatedPage);

					process(updatedPublication);

					Notification.show("Page unpublished.");
				}).open();
	}

	private void process(PagePublication publication) {
		PagePublicationAction action = publication.getAction();

		if (!publication.getStatus().equals(PagePublicationStatus.COMPLETED)) {
			DeploymentTask task = null;

			if (action.equals(PagePublicationAction.PUBLISH)) {
				task = new PublishDeploymentTask();
			}

			if (action.equals(PagePublicationAction.UNPUBLISH)) {
				task = new UnpublishDeploymentTask();
			}

			if (task != null) {
				UUID id = publication.getId();

				task.setApplicationContext(applicationContext);
				task.setPublication(publication);
				task.setCompletionHandler((completedTask) -> tasks.remove(id));

				executor.execute(task);

				tasks.put(id, task);

				eventBus.post(new PagePublicationUpdated(publication));
			}
		}
	}

	public Optional<DeploymentTask> getTask(UUID id) {
		if (tasks.containsKey(id)) {
			return Optional.of(tasks.get(id));
		}

		return Optional.empty();
	}

}
