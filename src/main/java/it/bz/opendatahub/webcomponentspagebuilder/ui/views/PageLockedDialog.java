package it.bz.opendatahub.webcomponentspagebuilder.ui.views;

import com.vaadin.flow.component.dialog.Dialog;

public class PageLockedDialog extends Dialog {

	private static final long serialVersionUID = -1177009863095957099L;

	public PageLockedDialog() {
		add("This operation is not allowed while the page is involved in a deployment operation. Please wait and try again later.");
		setWidth("480px");
	}

}
