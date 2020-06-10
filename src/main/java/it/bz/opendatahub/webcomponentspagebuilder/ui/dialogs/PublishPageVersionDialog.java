package it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs;

import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.spring.annotation.SpringComponent;

import it.bz.opendatahub.webcomponentspagebuilder.data.Domain;
import it.bz.opendatahub.webcomponentspagebuilder.data.DomainsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;

/**
 * Dialog that allows selection and definition of the publishing information,
 * like domain, subdomain and path, used during the deployment of a single page.
 * 
 * @author danielrampanelli
 */
@Scope("prototype")
@SpringComponent
public class PublishPageVersionDialog extends Dialog {

	private static final long serialVersionUID = 1549131440457107213L;

	@FunctionalInterface
	public interface ConfirmHandler {
		public void confirmed(PagePublicationConfiguration configuration);
	}

	public class PagePublicationConfiguration {

		private String subdomain;

		private Domain domain;

		private String path;

		public PagePublicationConfiguration() {

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

	}

	public class DnsLabelOrPathValidator extends AbstractValidator<String> {

		private static final long serialVersionUID = 6234184103054597629L;

		protected DnsLabelOrPathValidator(String errorMessage) {
			super(errorMessage);
		}

		@Override
		public ValidationResult apply(String value, ValueContext context) {
			if (value == null || value.isEmpty()) {
				return toResult(value, true);
			}

			return toResult(value, Pattern.matches("^[a-zA-Z0-9\\-\\_]{3,}$", value));
		}

	}

	@Autowired
	DomainsProvider domainsProvider;

	@Autowired
	PageRepository pagesRepo;

	private Optional<ConfirmHandler> confirmHandler = Optional.empty();

	@PostConstruct
	private void postConstruct() {
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

		Binder<PagePublicationConfiguration> binder = new Binder<>(PagePublicationConfiguration.class);

		binder.forField(subdomainName)
				.withValidator(new DnsLabelOrPathValidator("Use only letters/digits/dashes (length >= 3)"))
				.bind("subdomain");

		binder.forField(domainName).bind("domain");

		binder.forField(pathName)
				.withValidator(new DnsLabelOrPathValidator("Use only letters/digits/dashes (length >= 3)"))
				.bind("path");

		binder.setBean(new PagePublicationConfiguration());

		Button saveButton = new Button("PUBLISH");
		saveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		saveButton.addClickListener(clickEvent -> {
			BinderValidationStatus<PagePublicationConfiguration> validation = binder.validate();

			if (validation.isOk()) {
				try {
					PagePublicationConfiguration bean = binder.getBean();

					Page newPage = new Page();
					newPage.setArchived(false);

					confirmHandler.ifPresent(handler -> handler.confirmed(bean));

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
		layout.add(domainLayout);
		layout.add(pathName);
		layout.add(buttons);

		layout.setHorizontalComponentAlignment(Alignment.END, buttons);

		add(layout);
		setWidth("480px");
	}

	public void setConfirmHandler(ConfirmHandler handler) {
		this.confirmHandler = Optional.of(handler);
	}

}