package it.bz.opendatahub.webcomponentspagebuilder.ui;

import org.apache.commons.codec.digest.DigestUtils;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponent;

/**
 * Represents a component placed on a page which will be rendered using the configured markup
 */
public class PageContent extends Div {

	private static final long serialVersionUID = -4209117141353384559L;

	private PageComponent component;

	private String currentMarkup;

	private Html html;

	public PageContent(PageContentActionHandler actionHandler, PageComponent component) {
		this.component = component;
		this.currentMarkup = component.getDefaultMarkup();
		this.html = new Html(currentMarkup);

		String assetID = DigestUtils.md5Hex(component.getAssetUrl());

		UI.getCurrent().getPage().executeJavaScript(String.format(
				"if (!document.getElementById('%s')) { var s = document.createElement('script'); s.setAttribute('id', '%s'); s.setAttribute('src', '%s'); document.body.appendChild(s); }",
				assetID, assetID, component.getAssetUrl()));

		Button editButton = new Button(new Icon(VaadinIcon.EDIT));
		editButton.addClassNames("page-content-action", "edit");
		editButton.addThemeVariants(ButtonVariant.LUMO_ICON);

		editButton.addClickListener(clickEvent -> {
			PageContentDialog dialog = new PageContentDialog(currentMarkup, component.getDefaultMarkup());

			dialog.setSaveHandler((editedMarkup) -> {
				currentMarkup = editedMarkup;
				remove(html);
				html = new Html(currentMarkup);
				add(html);
			});

			dialog.open();
		});

		Button moveUpButton = new Button(new Icon(VaadinIcon.ARROW_UP));
		moveUpButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		moveUpButton.addClassNames("page-content-action", "up");

		moveUpButton.addClickListener(clickEvent -> {
			actionHandler.moveUp(PageContent.this);
		});

		Button moveDownButton = new Button(new Icon(VaadinIcon.ARROW_DOWN));
		moveDownButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		moveDownButton.addClassName("page-content-action");
		moveDownButton.addClassName("down");

		moveDownButton.addClickListener(clickEvent -> {
			actionHandler.moveDown(PageContent.this);
		});

		Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
		deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		deleteButton.addClassName("page-content-action");
		deleteButton.addClassName("remove");

		deleteButton.addClickListener(clickEvent -> {
			actionHandler.remove(PageContent.this);
		});

		add(html, editButton, moveUpButton, moveDownButton, deleteButton);
		addClassName("page-content");
	}

	public String getAssetUrl() {
		return component.getAssetUrl();
	}

	public String getCurrentMarkup() {
		return currentMarkup;
	}

}
