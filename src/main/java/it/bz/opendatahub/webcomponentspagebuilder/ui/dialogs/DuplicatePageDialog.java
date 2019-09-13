package it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.spring.annotation.SpringComponent;

import it.bz.opendatahub.webcomponentspagebuilder.data.DomainsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageContent;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageWidget;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;

/**
 * Dialog used for the definition of the settings that will be applied to a
 * duplicate of another page.
 * 
 * @author danielrampanelli
 */
@Scope("prototype")
@SpringComponent
public class DuplicatePageDialog extends Dialog {

	private static final long serialVersionUID = -7603325045918518392L;

	@FunctionalInterface
	public interface SaveHandler {
		public void created(Page page);
	}

	public static class PageToDuplicate {

		private String label;

		private String title;

		private String description;

		private List<PageContent> contents;

		private List<PageWidget> widgets;

		public PageToDuplicate() {

		}

		public PageToDuplicate(PageVersion pageVersion) {
			setTitle(pageVersion.getTitle());
			setDescription(pageVersion.getDescription());
			setContents(pageVersion.getContents());
			setWidgets(pageVersion.getWidgets());
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public List<PageContent> getContents() {
			return contents;
		}

		public void setContents(List<PageContent> contents) {
			this.contents = contents;
		}

		public List<PageWidget> getWidgets() {
			return widgets;
		}

		public void setWidgets(List<PageWidget> widgets) {
			this.widgets = widgets;
		}

	}

	@Autowired
	DomainsProvider domainsProvider;

	@Autowired
	PageRepository pagesRepo;

	private Optional<SaveHandler> saveHandler = Optional.empty();

	private Binder<PageToDuplicate> binder;

	@PostConstruct
	private void postConstruct() {
		TextField label = new TextField();
		label.setLabel("LABEL");
		label.setWidthFull();

		TextField title = new TextField();
		title.setLabel("TITLE");
		title.setWidthFull();

		TextArea description = new TextArea();
		description.setLabel("DESCRIPTION");
		description.setWidthFull();

		binder = new Binder<>(PageToDuplicate.class);

		binder.forField(label).asRequired().bind("label");

		binder.forField(title).bind("title");

		binder.forField(description).bind("description");

		Button saveButton = new Button("DUPLICATE");
		saveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		saveButton.addClickListener(clickEvent -> {
			BinderValidationStatus<PageToDuplicate> validation = binder.validate();

			if (validation.isOk()) {
				try {
					PageToDuplicate bean = binder.getBean();

					Page duplicatedPage = new Page();
					duplicatedPage.setArchived(false);
					duplicatedPage.setLabel(bean.getLabel());

					saveHandler.ifPresent(handler -> {
						Page createdPage = pagesRepo.save(duplicatedPage);

						PageVersion duplicatedPageVersion = new PageVersion();
						duplicatedPageVersion.setPage(createdPage);
						duplicatedPageVersion.setHash(DigestUtils.sha1Hex(UUID.randomUUID().toString()));
						duplicatedPageVersion.setUpdatedAt(LocalDateTime.now());

						duplicatedPageVersion.setTitle(bean.getTitle());
						duplicatedPageVersion.setDescription(bean.getDescription());

						duplicatedPageVersion.setContents(bean.getContents().stream().map(pageContent -> {
							PageContent copy = pageContent.copy();
							copy.setPageVersion(duplicatedPageVersion);

							return copy;
						}).collect(Collectors.toList()));

						duplicatedPageVersion.setWidgets(bean.getWidgets().stream().map(pageWidget -> {
							PageWidget copy = pageWidget.copy();
							copy.setPageVersion(duplicatedPageVersion);

							return copy;
						}).collect(Collectors.toList()));

						createdPage.setDraftVersion(duplicatedPageVersion);
						createdPage = pagesRepo.save(duplicatedPage);

						handler.created(createdPage);
					});

					close();
				} catch (Throwable t) {
					// TODO Auto-generated catch block
					t.printStackTrace();

					Notification.show("Error occurred, please check the given input/values.");
				}
			} else {
				Notification.show("Please check the given input/values.");
			}
		});

		HorizontalLayout buttons = new HorizontalLayout(saveButton);

		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.add(label);
		layout.add(title);
		layout.add(description);
		layout.add(buttons);

		layout.setHorizontalComponentAlignment(Alignment.END, buttons);

		add(layout);
		setWidth("480px");
	}

	public void setBean(PageToDuplicate pageToDuplicate) {
		binder.setBean(pageToDuplicate);
	}

	public void setSaveHandler(SaveHandler handler) {
		this.saveHandler = Optional.of(handler);
	}

}
