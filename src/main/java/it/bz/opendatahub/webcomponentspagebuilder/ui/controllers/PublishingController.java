package it.bz.opendatahub.webcomponentspagebuilder.ui.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;
import com.vaadin.flow.component.notification.Notification;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import it.bz.opendatahub.webcomponentspagebuilder.controllers.PageLockedException;
import it.bz.opendatahub.webcomponentspagebuilder.data.Domain;
import it.bz.opendatahub.webcomponentspagebuilder.data.DomainsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageContent;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationAction;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationStatus;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageWidget;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Site;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PagePublicationRepository;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageVersionRepository;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.SiteRepository;
import it.bz.opendatahub.webcomponentspagebuilder.deployment.DeploymentPayload;
import it.bz.opendatahub.webcomponentspagebuilder.deployment.DeploymentPayload.PayloadFile;
import it.bz.opendatahub.webcomponentspagebuilder.deployment.DeploymentTask;
import it.bz.opendatahub.webcomponentspagebuilder.deployment.PersistentThreadPoolExecutor;
import it.bz.opendatahub.webcomponentspagebuilder.deployment.PublishDeploymentTask;
import it.bz.opendatahub.webcomponentspagebuilder.deployment.UnpublishDeploymentTask;
import it.bz.opendatahub.webcomponentspagebuilder.events.PagePublicationUpdated;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.PageRenderer;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.DangerAwareConfirmDialog;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.PageVersionCommentDialog;
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

	@FunctionalInterface
	public static interface ArchiveHandler {
		public void archived(Page page);
	}

	@FunctionalInterface
	public static interface RestoreHandler {
		public void restored(Page page);
	}

	@FunctionalInterface
	public static interface DeployHandler {
		public void deployed(Page page);
	}

	@FunctionalInterface
	public static interface UndeployHandler {
		public void undeployed(Page page);
	}

	@Value("${application.deployment.parallel:#{1}}")
	Integer numberOfThreads;

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	SiteRepository sitesRepo;

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

	private PersistentThreadPoolExecutor executor;

	private Map<UUID, DeploymentTask> tasks = Collections.synchronizedMap(new HashMap<>());

	@PostConstruct
	private void postConstruct() {
		executor = applicationContext.getBean(PersistentThreadPoolExecutor.class);
		executor.setCorePoolSize(numberOfThreads);
		executor.setMaximumPoolSize(numberOfThreads);

		publicationsRepo.findAllActive().forEach(publication -> {
			publication.setStatus(PagePublicationStatus.PENDING);
			publication.setUpdatedAt(LocalDateTime.now());

			publication = publicationsRepo.save(publication);

			process(publication);
		});
	}

	private PageVersion publishPageVersion(PageVersion pageVersion, String comment) {
		PageVersion publishedVersion = new PageVersion();
		publishedVersion.setPage(pageVersion.getPage());
		publishedVersion.setHash(DigestUtils.sha1Hex(UUID.randomUUID().toString()));
		publishedVersion.setUpdatedAt(LocalDateTime.now());
		publishedVersion.setComment(comment);

		publishedVersion.setTitle(publishedVersion.getTitle());
		publishedVersion.setDescription(publishedVersion.getDescription());

		publishedVersion
				.setContents(pageVersion.getContents().stream().filter(content -> content != null).map(pageContent -> {
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

			draftVersion.setContents(
					publicVersion.getContents().stream().filter(content -> content != null).map(pageContent -> {
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

		return versionsRepo.save(draftVersion);
	}

	private void doPublish(Page page, PageVersion pageVersion, String comment, Site site, PublishHandler handler) {
		if (isLocked(page)) {
			throw new PageLockedException();
		}

		PageVersion publishedVersion = publishPageVersion(pageVersion, comment);

		Page updatedPage = pagesRepo.getOne(page.getId());

		PagePublication publication = new PagePublication();
		publication.setPage(updatedPage);
		publication.setVersion(publishedVersion);
		publication.setSite(site);
		publication.setAction(PagePublicationAction.PUBLISH);
		publication.setStatus(PagePublicationStatus.PENDING);
		publication.setUpdatedAt(LocalDateTime.now());

		publication = publicationsRepo.save(publication);

		publishedVersion.setPublication(publication);
		publishedVersion = versionsRepo.save(publishedVersion);

		versionsRepo.save(updatedPage.getDraftVersion());

		PageVersion draftVersion = updatedPage.getDraftVersion();

		updatedPage.setDraftVersion(null);
		updatedPage.removeVersion(draftVersion);

		updatedPage.setPublicVersion(publishedVersion);
		updatedPage.addPublication(publication);

		updatedPage = pagesRepo.save(updatedPage);

		handler.published(updatedPage, publishedVersion);

		process(publication);
	}

	public void publish(PageVersion pageVersion, PublishHandler handler) {
		Page page = pageVersion.getPage();

		if (page.getSite() == null) {
			PageVersionCommentDialog commentDialog = applicationContext.getBean(PageVersionCommentDialog.class);

			commentDialog.setConfirmHandler((comment) -> {
				PublishPageVersionDialog dialog = applicationContext.getBean(PublishPageVersionDialog.class);

				dialog.setConfirmHandler((configuration) -> {
					Site site = new Site();
					site.setDomainName(configuration.getDomain().getHostName());

					Domain domain = configuration.getDomain();

					if (domain.getAllowSubdomains()
							&& (configuration.getSubdomain() != null && !configuration.getSubdomain().equals(""))) {
						site.setSubdomainName(configuration.getSubdomain());
					}

					if (configuration.getPath() != null && !configuration.getPath().equals("")) {
						site.setPathName(configuration.getPath());
					}

					site = sitesRepo.save(site);

					Page freshPage = pagesRepo.getOne(page.getId());
					freshPage.setSite(site);

					freshPage = pagesRepo.save(freshPage);

					pageVersion.setPage(freshPage);

					doPublish(freshPage, pageVersion, comment, site, handler);

					Notification.show("Page version published.");
				});

				dialog.open();
			});

			commentDialog.open();
		} else {
			doPublish(page, pageVersion, null, page.getSite(), handler);

			Notification.show("Page version published.");
		}
	}

	private void doDeploy(PageVersion pageVersion, DeployHandler handler) {
		PageVersion publicVersion = versionsRepo.getOne(pageVersion.getId());

		Page page = pagesRepo.getOne(publicVersion.getPage().getId());

		Site site = page.getSite();

		PagePublication publication = new PagePublication();
		publication.setPage(page);
		publication.setVersion(publicVersion);
		publication.setSite(site);
		publication.setAction(PagePublicationAction.PUBLISH);
		publication.setStatus(PagePublicationStatus.PENDING);
		publication.setUpdatedAt(LocalDateTime.now());

		publication = publicationsRepo.save(publication);

		publicVersion.setPublication(publication);
		publicVersion = versionsRepo.save(publicVersion);
		
		process(publication);

		Notification.show("Page version published.");
		
		eventBus.post(new PagePublicationUpdated(publication));
	}

	public void deploy(PageVersion pageVersion, DeployHandler handler) {
		if (isLocked(pageVersion.getPage())) {
			throw new PageLockedException();
		}

		Page page = pageVersion.getPage();

		if (!page.hasSite()) {
			PublishPageVersionDialog dialog = applicationContext.getBean(PublishPageVersionDialog.class);

			dialog.setConfirmHandler((configuration) -> {
				Site site = new Site();
				site.setDomainName(configuration.getDomain().getHostName());

				Domain domain = configuration.getDomain();

				if (domain.getAllowSubdomains()
						&& (configuration.getSubdomain() != null && !configuration.getSubdomain().equals(""))) {
					site.setSubdomainName(configuration.getSubdomain());
				}

				if (configuration.getPath() != null && !configuration.getPath().equals("")) {
					site.setPathName(configuration.getPath());
				}

				site = sitesRepo.save(site);

				Page freshPage = pagesRepo.getOne(page.getId());
				freshPage.setSite(site);

				freshPage = pagesRepo.save(freshPage);

				pageVersion.setPage(freshPage);

				doDeploy(pageVersion, handler);

				Notification.show("Page version published.");
			});

			dialog.open();
		} else {
			doDeploy(pageVersion, handler);
		}
	}

	public void discard(PageVersion pageVersion, DiscardHandler handler) {
		Page page = pageVersion.getPage();

		DangerAwareConfirmDialog.create().withTitle("DISCARD")
				.withMessage("Are you sure you want to discard this page draft?").withAction("DISCARD")
				.withHandler(() -> {
					PageVersion draftVersion = page.getDraftVersion();

					page.removeVersion(draftVersion);
					page.setDraftVersion(null);

					Page updatedPage = pagesRepo.save(page);

					versionsRepo.delete(draftVersion);

					handler.discarded(updatedPage);

					Notification.show("Page version discarded.");
				}).open();
	}

	public void unpublish(PageVersion pageVersion, UndeployHandler handler) {
		Page page = pageVersion.getPage();
		Site site = page.getSite();

		if (isLocked(page)) {
			throw new PageLockedException();
		}

		DangerAwareConfirmDialog.create().withTitle("UNPUBLISH PAGE").withMessage(
				"Are you sure you want to unpublish this page? Afterwards the page won't be publicly available anymore.")
				.withAction("UNPUBLISH").withHandler(() -> {
					PagePublication updatedPublication = new PagePublication();
					updatedPublication.setPage(page);
					updatedPublication.setVersion(pageVersion);
					updatedPublication.setSite(site);
					updatedPublication.setAction(PagePublicationAction.UNPUBLISH);
					updatedPublication.setStatus(PagePublicationStatus.PENDING);
					updatedPublication.setUpdatedAt(LocalDateTime.now());

					updatedPublication = publicationsRepo.save(updatedPublication);

					PageVersion updatedVersion = versionsRepo.getOne(pageVersion.getId());
					updatedVersion.setPublication(null);

					updatedVersion = versionsRepo.save(updatedVersion);

					Page updatedPage = pagesRepo.getOne(page.getId());

					handler.undeployed(updatedPage);

					process(updatedPublication);

					Notification.show("Page version unpublished.");
				}).open();
	}

	public void process(PagePublication publication) {
		PagePublicationAction action = publication.getAction();

		if (!publication.getStatus().equals(PagePublicationStatus.COMPLETED)) {
			DeploymentTask task = null;

			if (action.equals(PagePublicationAction.PUBLISH)) {
				try {
					task = new PublishDeploymentTask(createPayload(publication.getVersion()));
				} catch (IOException | TemplateException e) {
					e.printStackTrace();

					throw new RuntimeException(e);
				}
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

	private DeploymentPayload createPayload(PageVersion pageVersion) throws TemplateNotFoundException,
			MalformedTemplateNameException, ParseException, IOException, TemplateException {
		String renderedPage = pageRenderer.renderPage(pageVersion);

		DeploymentPayload payload = new DeploymentPayload();

		payload.add(new PayloadFile().withName("index.html").withContentType("text/html").withContent(renderedPage));

		payload.add(new PayloadFile().withName("fonts/odh-kievit.woff").withContentType("application/x-font-woff")
				.withContent(getClass().getResourceAsStream("/templates/main/fonts/odh-kievit.woff")));

		payload.add(new PayloadFile().withName("images/favicon.png").withContentType("image/png")
				.withContent(getClass().getResourceAsStream("/templates/main/images/favicon.png")));

		payload.add(new PayloadFile().withName("images/logo-default.svg").withContentType("image/svg+xml")
				.withContent(getClass().getResourceAsStream("/templates/main/images/logo-default.svg")));

		payload.add(new PayloadFile().withName("images/logo-sticky.png").withContentType("image/png")
				.withContent(getClass().getResourceAsStream("/templates/main/images/logo-sticky.png")));

		payload.add(new PayloadFile().withName("images/footer.svg").withContentType("image/svg+xml")
				.withContent(getClass().getResourceAsStream("/templates/main/images/footer.svg")));

		return payload;
	}

	public void abortPublication(PagePublication publication) {
		Page freshPage = pagesRepo.getOne(publication.getPage().getId());
		freshPage.removePublication(publication);

		pagesRepo.save(freshPage);

		PageVersion freshVersion = versionsRepo.getOne(publication.getVersion().getId());
		freshVersion.setPublication(null);

		versionsRepo.save(freshVersion);

		PagePublication freshPublication = publicationsRepo.getOne(publication.getId());

		publicationsRepo.delete(freshPublication);
	}

	public void archive(Page page, ArchiveHandler archiveHandler) {
		if (isLocked(page)) {
			throw new PageLockedException();
		}

		Page freshPage = pagesRepo.getOne(page.getId());
		freshPage.setArchived(true);
		freshPage.setSite(page.getSite());

		if (freshPage.hasPublicVersion()) {
			PageVersion publicVersion = freshPage.getPublicVersion();
			publicVersion.setPublication(null);

			publicVersion = versionsRepo.save(publicVersion);

			freshPage.setPublicVersion(publicVersion);
		}

		freshPage = pagesRepo.save(freshPage);

		archiveHandler.archived(freshPage);

		if (freshPage.hasPublicVersion()) {
			PagePublication createdPublication = new PagePublication();
			createdPublication.setPage(page);
			createdPublication.setVersion(freshPage.getPublicVersion());
			createdPublication.setSite(freshPage.getSite());
			createdPublication.setAction(PagePublicationAction.UNPUBLISH);
			createdPublication.setStatus(PagePublicationStatus.PENDING);
			createdPublication.setUpdatedAt(LocalDateTime.now());

			createdPublication = publicationsRepo.save(createdPublication);

			process(createdPublication);
		}
	}

	public void restore(Page page, RestoreHandler restoreHandler) {
		if (isLocked(page)) {
			throw new PageLockedException();
		}

		Page freshPage = pagesRepo.getOne(page.getId());
		freshPage.setArchived(false);

		freshPage = pagesRepo.save(freshPage);

		restoreHandler.restored(freshPage);
	}

	public Optional<DeploymentTask> getTask(UUID id) {
		if (tasks.containsKey(id)) {
			return Optional.of(tasks.get(id));
		}

		return Optional.empty();
	}

	public boolean isLocked(Page page) {
		Page freshPage = pagesRepo.getOne(page.getId());

		if (freshPage.getPublications().stream().filter(publication -> !publication.isCompleted()).count() == 0) {
			return false;
		}

		return true;
	}

}
