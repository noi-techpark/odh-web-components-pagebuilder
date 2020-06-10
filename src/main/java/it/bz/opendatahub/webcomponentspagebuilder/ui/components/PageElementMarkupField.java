package it.bz.opendatahub.webcomponentspagebuilder.ui.components;

import java.util.Optional;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("page-element-markup-field")
@HtmlImport("frontend://ui/PageComponentMarkupField.html")
@JavaScript("frontend://assets/ace/ace.js")
@JavaScript("frontend://assets/ace/mode-html.js")
@JavaScript("frontend://assets/ace/mode-xml.js")
@JavaScript("frontend://assets/ace/theme-monokai.js")
@JavaScript("frontend://assets/ace/worker-css.js")
@JavaScript("frontend://assets/ace/worker-javascript.js")
@JavaScript("frontend://assets/ace/worker-json.js")
@JavaScript("frontend://assets/ace/worker-html.js")
@JavaScript("frontend://assets/ace/worker-xml.js")
public class PageElementMarkupField extends PolymerTemplate<TemplateModel> {

	private static final long serialVersionUID = 7601309940935722889L;

	@FunctionalInterface
	public interface UpdateHandler {
		public void markupChanged(String markup);
	}

	private Optional<UpdateHandler> updateHandler = Optional.empty();

	private String text;

	public Optional<UpdateHandler> getUpdateHandler() {
		return updateHandler;
	}

	public void setUpdateHandler(UpdateHandler updateHandler) {
		this.updateHandler = Optional.of(updateHandler);
	}

	public void setText(String text) {
		this.text = text;
		getElement().callFunction("setEditorText", text);
	}

	@ClientCallable
	public void updateText(String text) {
		this.text = text;
		this.updateHandler.ifPresent(handler -> handler.markupChanged(text));
	}

	public String getText() {
		return text;
	}

}
