package it.bz.opendatahub.webcomponentspagebuilder.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import it.bz.opendatahub.webcomponentspagebuilder.data.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageContent;

@HtmlImport("ui/PageEditor.html")
@Tag("pagebuilder-page-editor")
public class PageEditor extends PolymerTemplate<TemplateModel> {

	private static final long serialVersionUID = -1261729094659327441L;

	@Id("frame")
	private IFrame frame;

	private Page page;

	public void setPage(Page page) {
		this.page = page;
	}

	public void bind(Page page) {
		this.page = page;

		frame.setSrc(String.format("/editor/%s.html", page.getId().toString()));
	}

	@ClientCallable
	public void droppedContent(String[] assets, String tagName, String markup, String insertBefore) {
		PageContent pageContent = new PageContent();
		pageContent.setId(UUID.randomUUID());
		pageContent.setPage(page);
		pageContent.setAssets(Arrays.asList(assets));
		pageContent.setTagName(tagName);
		pageContent.setMarkup(markup);
		pageContent.setPosition(
				page.getContents().stream().map(PageContent::getPosition).max(Integer::compare).orElse(-1) + 1);

		page.getContents().add(pageContent);

		JsonArray jsAssets = Json.createArray();
		for (int i = 0; i < pageContent.getAssets().size(); i++) {
			jsAssets.set(i, pageContent.getAssets().get(i));
		}

		getElement().callFunction("insertContent", pageContent.getId().toString(), jsAssets, tagName, markup,
				insertBefore);
	}

	@ClientCallable
	public void editContent(String uuid) {
		List<PageContent> matches = page.getContents().stream()
				.filter(content -> content.getId().toString().equals(uuid)).collect(Collectors.toList());

		if (!matches.isEmpty()) {
			PageContent pageContent = matches.get(0);

			PageContentConfigurationDialog dialog = new PageContentConfigurationDialog(pageContent);

			dialog.setSaveHandler((updateMarkup) -> {
				pageContent.setMarkup(updateMarkup);

				getElement().callFunction("updateContent", uuid, updateMarkup);
			});

			dialog.open();
		}
	}

	@ClientCallable
	public void rearrangedContents(String[] uuids) {
		Map<String, PageContent> pageContentsById = new HashMap<>();

		page.getContents().forEach(pageContent -> {
			pageContentsById.put(pageContent.getId().toString(), pageContent);
		});

		List<PageContent> rearrangedPageContents = new ArrayList<>();

		for (String uuid : uuids) {
			if (pageContentsById.containsKey(uuid)) {
				PageContent pageContent = pageContentsById.get(uuid);
				pageContent.setPosition(rearrangedPageContents.size());

				rearrangedPageContents.add(pageContent);
			}
		}

		page.setContents(rearrangedPageContents);
	}

	@ClientCallable
	public void removedContent(String uuid) {
		page.setContents(page.getContents().stream().filter(content -> !content.getId().toString().equals(uuid))
				.collect(Collectors.toList()));
	}

}
