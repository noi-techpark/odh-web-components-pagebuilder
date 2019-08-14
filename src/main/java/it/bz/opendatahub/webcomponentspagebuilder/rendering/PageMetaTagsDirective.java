package it.bz.opendatahub.webcomponentspagebuilder.rendering;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;

public class PageMetaTagsDirective implements TemplateDirectiveModel {

	private PageVersion page;

	public PageMetaTagsDirective(PageVersion pageVersion) {
		this.page = pageVersion;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
			throws TemplateException, IOException {
		Writer out = env.getOut();

		if (page.getTitle() != null && !page.getTitle().isEmpty()) {
			out.append(String.format("<title>%s</title>\n", page.getTitle()));
		}

		if (page.getDescription() != null && !page.getDescription().isEmpty()) {
			out.append(String.format("<meta name=\"description\" content=\"%s\"/>\n", page.getDescription()));
		}

		if (page.getTitle() != null && !page.getTitle().isEmpty()) {
			out.append(String.format("<meta name=\"og:title\" content=\"%s\"/>\n", page.getTitle()));
		}

		if (page.getDescription() != null && !page.getDescription().isEmpty()) {
			out.append(String.format("<meta name=\"og:description\" content=\"%s\"/>\n", page.getDescription()));
		}
	}

}
