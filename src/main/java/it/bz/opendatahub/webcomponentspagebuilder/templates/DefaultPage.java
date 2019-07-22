package it.bz.opendatahub.webcomponentspagebuilder.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

/**
 * Default page template
 */
@HtmlImport("templates/DefaultPage.html")
@Tag("odh-default-page")
public class DefaultPage extends PolymerTemplate<TemplateModel> {

	private static final long serialVersionUID = 4714239611022769015L;

	@Id("contents")
	private Div contents;

	public void setContents(Component content) {
		contents.removeAll();
		contents.add(content);
	}

}
