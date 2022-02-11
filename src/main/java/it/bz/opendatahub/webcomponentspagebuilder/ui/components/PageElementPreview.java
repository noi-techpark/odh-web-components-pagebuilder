package it.bz.opendatahub.webcomponentspagebuilder.ui.components;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

import elemental.json.Json;
import elemental.json.JsonArray;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageElement;

/**
 * Custom element/component that previews a page component on the fly.
 * 
 * @author danielrampanelli
 */
@HtmlImport("ui/PageElementPreview.html")
@Tag("pagebuilder-page-element-preview")
public class PageElementPreview extends PolymerTemplate<TemplateModel> {

	private static final long serialVersionUID = 4717655982356706023L;

	public void updateElement(PageElement pageElement, String markup) {
		JsonArray assets = Json.createArray();
		for (int i = 0; i < pageElement.getAssets().size(); i++) {
			assets.set(i, pageElement.getAssets().get(i));
		}

		getElement().callFunction("setup", assets);
		getElement().callFunction("update", markup);
	}

}
