package it.bz.opendatahub.webcomponentspagebuilder.rendering;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageContent;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;

/**
 * Custom template directive/tag for rendering the page's content blocks.
 * 
 * @author danielrampanelli
 */
public class PageContentsDirective implements TemplateDirectiveModel {

	private PageVersion pageVersion;

	public PageContentsDirective(PageVersion pageVersion) {
		this.pageVersion = pageVersion;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
			throws TemplateException, IOException {
		Writer out = env.getOut();

		out.append("<div id=\"odh-contents\">\n");

		Set<String> assets = new HashSet<>();

		for (PageContent pageContent : pageVersion.getContents().stream().filter(content -> content != null)
				.collect(Collectors.toList())) {
			if (pageContent.getAssets() != null) {
				for (String asset : pageContent.getAssets()) {
					assets.add(asset);
				}
			}

			out.append(String.format("<div id=\"%s\" class=\"odh-page-content\">\n%s\n</div>\n",
					pageContent.getContentID().toString(), pageContent.getMarkup()));
		}

		for (String asset : assets) {
			out.append(String.format("<script src=\"%s\" async></script>\n", asset));
		}

		out.append("</div>\n");
	}

}
