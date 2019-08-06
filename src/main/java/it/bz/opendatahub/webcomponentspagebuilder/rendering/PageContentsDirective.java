package it.bz.opendatahub.webcomponentspagebuilder.rendering;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import it.bz.opendatahub.webcomponentspagebuilder.data.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageContent;

public class PageContentsDirective implements TemplateDirectiveModel {

	private Page page;

	public PageContentsDirective(Page page) {
		this.page = page;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
			throws TemplateException, IOException {
		Writer out = env.getOut();

		out.append("<div id=\"odh-contents\">\n");

		Set<String> assets = new HashSet<>();

		for (PageContent pageContent : page.getContents()) {
			for (String asset : pageContent.getAssets()) {
				assets.add(asset);
			}

			out.append(String.format("<div id=\"%s\" class=\"odh-page-content\">\n%s\n</div>\n",
					pageContent.getId().toString(), pageContent.getMarkup()));
		}

		for (String asset : assets) {
			out.append(String.format("<script src=\"%s\" async></script>\n", asset));
		}

		out.append("</div>\n");
	}

}
