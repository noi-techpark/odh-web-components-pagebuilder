package it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;

@Scope("prototype")
@SpringComponent
public class CreatePageDialog extends Dialog {

	private static final long serialVersionUID = 3215685214304237675L;

	@FunctionalInterface
	public interface SaveHandler {
		public void created(Page page);
	}

	public class PageToCreate {

		private String label;

		private String title;

		private String description;

		public PageToCreate() {

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

	}

	@Autowired
	DomainsProvider domainsProvider;

	@Autowired
	PageRepository pagesRepo;

	private Optional<SaveHandler> saveHandler = Optional.empty();

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

		Binder<PageToCreate> binder = new Binder<>(PageToCreate.class);

		binder.forField(label).asRequired().bind("label");

		binder.forField(title).bind("title");

		binder.forField(description).bind("description");

		binder.setBean(new PageToCreate());

		Button saveButton = new Button("CREATE");
		saveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		saveButton.addClickListener(clickEvent -> {
			BinderValidationStatus<PageToCreate> validation = binder.validate();

			if (validation.isOk()) {
				try {
					PageToCreate bean = binder.getBean();

					Page newPage = new Page();
					newPage.setArchived(false);
					newPage.setLabel(bean.getLabel());

					saveHandler.ifPresent(handler -> {
						Page createdPage = pagesRepo.save(newPage);

						PageVersion newPageVersion = new PageVersion();
						newPageVersion.setPage(createdPage);
						newPageVersion.setHash(DigestUtils.sha1Hex(UUID.randomUUID().toString()));
						newPageVersion.setUpdatedAt(LocalDateTime.now());
						newPageVersion.setTitle(bean.getTitle());
						newPageVersion.setDescription(bean.getDescription());

						createdPage.setDraftVersion(newPageVersion);
						createdPage = pagesRepo.save(newPage);

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

	public void setSaveHandler(SaveHandler handler) {
		this.saveHandler = Optional.of(handler);
	}

}
