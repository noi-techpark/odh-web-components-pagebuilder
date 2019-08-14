package it.bz.opendatahub.webcomponentspagebuilder.ui.controllers;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.notification.Notification;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageContent;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationStatus;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PagePublicationRepository;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.PublishPageVersionDialog;

@Component
public class PublicationController {

	@FunctionalInterface
	public static interface PublishHandler {
		public void published(Page page, PageVersion pageVersion);
	}

	@FunctionalInterface
	public static interface DiscardHandler {
		public void discarded(Page page);
	}

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	PageRepository pagesRepo;

	@Autowired
	PagePublicationRepository publicationsRepo;

	private Page publishPageVersion(PageVersion pageVersion) {
		Page page = pageVersion.getPage();

		PageVersion publishedVersion = new PageVersion();
		publishedVersion.setPage(page);
		publishedVersion.setHash(DigestUtils.sha1Hex(UUID.randomUUID().toString()));
		publishedVersion.setUpdatedAt(LocalDateTime.now());

		publishedVersion.setTitle(publishedVersion.getTitle());
		publishedVersion.setDescription(publishedVersion.getDescription());

		publishedVersion.setContents(pageVersion.getContents().stream().map(pageContent -> {
			PageContent copy = pageContent.copy();
			copy.setPageVersion(publishedVersion);

			return copy;
		}).collect(Collectors.toList()));

		page.setDraftVersion(null);
		page.setPublicVersion(publishedVersion);

		return pagesRepo.save(page);
	}

	public void publish(PageVersion pageVersion, PublishHandler handler) {
		Page page = pageVersion.getPage();

		if (page.getPublication() == null) {
			PublishPageVersionDialog dialog = applicationContext.getBean(PublishPageVersionDialog.class);

			dialog.setConfirmHandler((configuration) -> {
				Page updatedPage = publishPageVersion(pageVersion);
				PageVersion publishedVersion = updatedPage.getPublicVersion();

				PagePublication pagePublication = new PagePublication();
				pagePublication.setPage(updatedPage);
				pagePublication.setDeployedAt(LocalDateTime.now());
				pagePublication.setStatus(PagePublicationStatus.COMPLETED);
				pagePublication.setVersion(publishedVersion);

				pagePublication.setDomainName(configuration.getDomain().getHostName());

				if (configuration.getDomain().getAllowSubdomains()
						&& (configuration.getSubdomain() != null && !configuration.getSubdomain().equals(""))) {
					pagePublication.setSubdomainName(configuration.getSubdomain());
				}

				if (configuration.getPath() != null && !configuration.getPath().equals("")) {
					pagePublication.setPathName(configuration.getPath());
				}

				pagePublication = publicationsRepo.save(pagePublication);

				updatedPage.setPublication(pagePublication);

				updatedPage = pagesRepo.save(updatedPage);

				handler.published(updatedPage, publishedVersion);

				Notification.show("Page draft published.");
			});

			dialog.open();
		} else {
			Page updatedPage = publishPageVersion(pageVersion);
			PageVersion publishedVersion = updatedPage.getPublicVersion();

			PagePublication pagePublication = updatedPage.getPublication();
			pagePublication.setDeployedAt(LocalDateTime.now());
			pagePublication.setStatus(PagePublicationStatus.COMPLETED);
			pagePublication.setVersion(publishedVersion);

			pagePublication = publicationsRepo.save(pagePublication);

			updatedPage.setPublication(pagePublication);

			handler.published(updatedPage, publishedVersion);

			Notification.show("Page draft published.");
		}
	}

	public void discard(PageVersion pageVersion, DiscardHandler handler) {
		Page page = pageVersion.getPage();

		ConfirmDialog dialog = new ConfirmDialog("DISCARD", "Are you sure you want to discard this page draft?",
				"DISCARD", (dialogEvent) -> {
					page.setDraftVersion(null);

					Page updatedPage = pagesRepo.save(page);

					handler.discarded(updatedPage);

					Notification.show("Page draft discarded.");
				}, "CANCEL", (dialogEvent) -> {
					// noop
				});

		dialog.setConfirmButtonTheme("error primary");

		dialog.open();
	}

}
