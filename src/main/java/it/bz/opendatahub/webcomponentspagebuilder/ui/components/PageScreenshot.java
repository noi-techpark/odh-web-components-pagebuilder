package it.bz.opendatahub.webcomponentspagebuilder.ui.components;

import java.util.Optional;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

/**
 * Custom element/component capable of loading an image and show some feedback
 * while the resource is loaded.
 * 
 * @author danielrampanelli
 */
@HtmlImport("ui/PageScreenshot.html")
@Tag("pagebuilder-page-screenshot")
public class PageScreenshot extends PolymerTemplate<TemplateModel> {

	private static final long serialVersionUID = -7415834935386188600L;

	@FunctionalInterface
	public interface ClickHandler {
		public void clicked();
	}

	private Optional<ClickHandler> clickHandler = Optional.empty();

	public void setSrc(String url) {
		getElement().callFunction("load", url);
	}

	@ClientCallable
	public void clicked() {
		clickHandler.ifPresent(handler -> handler.clicked());
	}

	public Optional<ClickHandler> getClickHandler() {
		return clickHandler;
	}

	public void setClickHandler(ClickHandler clickHandler) {
		this.clickHandler = Optional.of(clickHandler);
	}

}
