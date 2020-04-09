package it.bz.opendatahub.webcomponentspagebuilder.ui.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

import elemental.json.Json;
import elemental.json.JsonArray;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponent;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageContent;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageWidget;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.PageElementConfigurationDialog;

/**
 * Custom element/component that augments the loaded page and allows editing and
 * interaction with it via drag-drop or by using the content block's respective
 * actions.
 * 
 * @author danielrampanelli
 */
@HtmlImport("ui/PageEditor.html")
@Tag("pagebuilder-page-editor")
public class PageEditor extends PolymerTemplate<TemplateModel> {

	private static final long serialVersionUID = -1261729094659327441L;

	@FunctionalInterface
	public interface UpdateHandler {
		public void pageChanged();
	}

	@Id("frame")
	private IFrame frame;

	private PageVersion pageVersion;

	private Optional<UpdateHandler> updateHandler;

	public void setPageVersion(PageVersion pageVersion) {
		this.pageVersion = pageVersion;
	}

	public void setUpdateHandler(UpdateHandler updateHandler) {
		this.updateHandler = Optional.of(updateHandler);
	}

	public void bind(PageVersion pageVersion) {
		this.pageVersion = pageVersion;

		frame.setSrc(String.format("/pages/page-editor/%s.html", pageVersion.getIdAsString()));
	}

	@ClientCallable
	public void droppedContent(String uid, String tagName, String markup, String[] assets, String insertBefore) {
		UUID contentID = UUID.randomUUID();

		PageContent pageContent = new PageContent();
		pageContent.setUid(uid);
		pageContent.setContentID(contentID);
		pageContent.setPageVersion(pageVersion);
		pageContent.setAssets(Arrays.asList(assets));
		pageContent.setTagName(tagName);
		pageContent.setMarkup(markup);
		pageContent.setPosition(pageVersion.getContents().stream().filter(content -> content != null)
				.map(PageContent::getPosition).max(Integer::compare).orElse(-1) + 1);

		pageVersion.getContents().add(pageContent);

		JsonArray jsAssets = Json.createArray();
		for (int i = 0; i < pageContent.getAssets().size(); i++) {
			jsAssets.set(i, pageContent.getAssets().get(i));
		}

		getElement().callFunction("insertContent", contentID.toString(), markup, jsAssets, insertBefore);

		updateHandler.ifPresent(UpdateHandler::pageChanged);
	}

	public void addContent(PageComponent component) {
		droppedContent(component.getUid(), component.getTagName(), component.getMarkup(),
				component.getAssets().toArray(new String[] {}), null);
	}

	public PageWidget addWidget(PageComponent component) {
		UUID widgetID = UUID.randomUUID();

		String markup = component.getMarkup();

		PageWidget pageWidget = new PageWidget();
		pageWidget.setUid(component.getUid());
		pageWidget.setWidgetID(widgetID);
		pageWidget.setPageVersion(pageVersion);
		pageWidget.setAssets(component.getAssets());
		pageWidget.setTagName(component.getTagName());
		pageWidget.setMarkup(markup);
		pageWidget.setPosition(
				pageVersion.getWidgets().stream().map(PageWidget::getPosition).max(Integer::compare).orElse(-1) + 1);

		pageVersion.getWidgets().add(pageWidget);

		JsonArray jsAssets = Json.createArray();
		for (int i = 0; i < pageWidget.getAssets().size(); i++) {
			jsAssets.set(i, pageWidget.getAssets().get(i));
		}

		getElement().callFunction("insertWidget", widgetID.toString(), markup, jsAssets);

		return pageWidget;
	}

	public void removeWidget(PageWidget pageWidget) {
		getElement().callFunction("removeWidget", pageWidget.getWidgetID().toString());
	}

	@ClientCallable
	public void editContent(String uuid) {
		List<PageContent> matches = pageVersion.getContents().stream()
				.filter(content -> content != null && content.getContentID().toString().equals(uuid))
				.collect(Collectors.toList());

		if (!matches.isEmpty()) {
			PageContent pageContent = matches.get(0);

			PageElementConfigurationDialog dialog = new PageElementConfigurationDialog(pageContent);

			dialog.setSaveHandler((updateMarkup) -> {
				pageContent.setMarkup(updateMarkup);

				getElement().callFunction("updateContent", uuid, updateMarkup);

				updateHandler.ifPresent(UpdateHandler::pageChanged);
			});

			dialog.open();
		}
	}

	@ClientCallable
	public void rearrangedContents(String[] uuids) {
		Map<String, PageContent> pageContentsById = new HashMap<>();

		pageVersion.getContents().stream().filter(content -> content != null).forEach(pageContent -> {
			pageContentsById.put(pageContent.getContentID().toString(), pageContent);
		});

		List<PageContent> rearrangedPageContents = new ArrayList<>();

		for (String uuid : uuids) {
			if (pageContentsById.containsKey(uuid)) {
				PageContent pageContent = pageContentsById.get(uuid);
				pageContent.setPosition(rearrangedPageContents.size());

				rearrangedPageContents.add(pageContent);
			}
		}

		pageVersion.setContents(rearrangedPageContents);

		updateHandler.ifPresent(UpdateHandler::pageChanged);
	}

	@ClientCallable
	public void removedContent(String uuid) {
		pageVersion.setContents(pageVersion.getContents().stream()
				.filter(content -> content != null && !content.getContentID().toString().equals(uuid))
				.collect(Collectors.toList()));

		updateHandler.ifPresent(UpdateHandler::pageChanged);
	}

	public void updateWidget(UUID widgetID, String markup) {
		getElement().callFunction("updateWidget", widgetID.toString(), markup);
	}

}
