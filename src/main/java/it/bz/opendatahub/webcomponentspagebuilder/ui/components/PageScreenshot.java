package it.bz.opendatahub.webcomponentspagebuilder.ui.components;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

@HtmlImport("ui/PageScreenshot.html")
@Tag("pagebuilder-page-screenshot")
public class PageScreenshot extends PolymerTemplate<TemplateModel> {

	private static final long serialVersionUID = -7415834935386188600L;

	public void setSrc(String url) {
		getElement().callFunction("load", url);
	}

}