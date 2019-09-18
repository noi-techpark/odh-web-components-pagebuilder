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

/**
 * Custom template directive/tag for rendering the page's meta tags.
 * 
 * @author danielrampanelli
 */
public class PageMetaTagsDirective implements TemplateDirectiveModel {

	private PageVersion pageVersion;

	public PageMetaTagsDirective(PageVersion pageVersion) {
		this.pageVersion = pageVersion;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
			throws TemplateException, IOException {
		Writer out = env.getOut();

		if (pageVersion.getTitle() != null && !pageVersion.getTitle().isEmpty()) {
			out.append(String.format("<title>%s</title>\n", pageVersion.getTitle()));
		}

		if (pageVersion.getDescription() != null && !pageVersion.getDescription().isEmpty()) {
			out.append(String.format("<meta name=\"description\" content=\"%s\"/>\n", pageVersion.getDescription()));
		}

		if (pageVersion.getTitle() != null && !pageVersion.getTitle().isEmpty()) {
			out.append(String.format("<meta name=\"og:title\" content=\"%s\"/>\n", pageVersion.getTitle()));
		}

		if (pageVersion.getDescription() != null && !pageVersion.getDescription().isEmpty()) {
			out.append(String.format("<meta name=\"og:description\" content=\"%s\"/>\n", pageVersion.getDescription()));
		}
	}

}
