package it.bz.opendatahub.webcomponentspagebuilder.ui;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.spring.annotation.SpringComponent;

import it.bz.opendatahub.webcomponentspagebuilder.data.Domain;
import it.bz.opendatahub.webcomponentspagebuilder.data.DomainsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageRepository;

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

		private String subdomain;

		private Domain domain;

		private String path;

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

		public String getSubdomain() {
			return subdomain;
		}

		public void setSubdomain(String subdomain) {
			this.subdomain = subdomain;
		}

		public Domain getDomain() {
			return domain;
		}

		public void setDomain(Domain domain) {
			this.domain = domain;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
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

	public class DnsLabelOrPathValidator extends AbstractValidator<String> {

		private static final long serialVersionUID = -1112802888441053669L;

		protected DnsLabelOrPathValidator(String errorMessage) {
			super(errorMessage);
		}

		@Override
		public ValidationResult apply(String value, ValueContext context) {
			if (value == null || value.isEmpty()) {
				return toResult(value, true);
			}

			return toResult(value, Pattern.matches("^[a-zA-Z0-9\\\\-_]{3,}$", value));
		}

	}

	@Autowired
	DomainsProvider domainsProvider;

	@Autowired
	PageRepository pageRepo;

	private Optional<SaveHandler> saveHandler = Optional.empty();

	@PostConstruct
	private void postConstruct() {
		TextField label = new TextField();
		label.setLabel("LABEL");
		label.setWidthFull();

		TextField subdomainName = new TextField();
		subdomainName.setLabel("SUBDOMAIN");
		subdomainName.setEnabled(false);
		subdomainName.setWidth("160px");

		ComboBox<Domain> domainName = new ComboBox<>();
		domainName.setAllowCustomValue(false);
		domainName.setItems(domainsProvider.getAvailableDomains());
		domainName.setItemLabelGenerator(Domain::getHostName);
		domainName.setLabel("DOMAIN");
		domainName.setPreventInvalidInput(true);
		domainName.setWidthFull();

		domainName.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				subdomainName.setEnabled(e.getValue().getAllowSubdomains());
			} else {
				subdomainName.setEnabled(false);
			}
		});

		TextField pathName = new TextField();
		pathName.setLabel("PATH");
		pathName.setWidthFull();

		TextField title = new TextField();
		title.setLabel("TITLE");
		title.setWidthFull();

		TextArea description = new TextArea();
		description.setLabel("DESCRIPTION");
		description.setWidthFull();

		Binder<PageToCreate> binder = new Binder<>(PageToCreate.class);

		binder.forField(label).asRequired().bind("label");

		binder.forField(subdomainName)
				.withValidator(new DnsLabelOrPathValidator("Use only letters/digits/dashes (length >= 3)"))
				.bind("subdomain");

		binder.forField(domainName).bind("domain");

		binder.forField(pathName)
				.withValidator(new DnsLabelOrPathValidator("Use only letters/digits/dashes (length >= 3)"))
				.bind("path");

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

					if (bean.getDomain().getAllowSubdomains()
							&& (bean.getSubdomain() != null && !bean.getSubdomain().equals(""))) {
						newPage.setDomainName(
								String.format("%s.%s", bean.getSubdomain(), bean.getDomain().getHostName()));
					} else {
						newPage.setDomainName(domainName.getValue().getHostName());
					}

					if (bean.getPath() != null && !bean.getPath().equals("")) {
						newPage.setPathName(bean.getPath());
					}

					newPage.setHash(DigestUtils.sha1Hex(UUID.randomUUID().toString()));
					newPage.setTitle(bean.getTitle());
					newPage.setDescription(bean.getDescription());

					saveHandler.ifPresent(handler -> handler.created(pageRepo.save(newPage)));

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

		HorizontalLayout domainLayout = new HorizontalLayout();
		domainLayout.add(subdomainName);
		domainLayout.add(domainName);
		domainLayout.setWidthFull();

		HorizontalLayout buttons = new HorizontalLayout(saveButton);

		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.add(label);
		layout.add(domainLayout);
		layout.add(pathName);
		layout.add(title);
		layout.add(description);
		layout.add(buttons);

		layout.setHorizontalComponentAlignment(Alignment.END, buttons);

		add(layout);
		setWidth("480px");
	}

	public void setSaveHandler(SaveHandler saveHandler) {
		this.saveHandler = Optional.of(saveHandler);
	}

}
