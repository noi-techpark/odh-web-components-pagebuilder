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
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageWidget;

/**
 * Custom template directive/tag for rendering the page's widgets.
 * 
 * @author danielrampanelli
 */
public class PageWidgetsDirective implements TemplateDirectiveModel {

	private PageVersion pageVersion;

	public PageWidgetsDirective(PageVersion pageVersion) {
		this.pageVersion = pageVersion;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
			throws TemplateException, IOException {
		Writer out = env.getOut();

		out.append("<div id=\"odh-widgets\">\n");

		Set<String> assets = new HashSet<>();

		for (PageWidget pageWidget : pageVersion.getWidgets()) {
			for (String asset : pageWidget.getAssets()) {
				assets.add(asset);
			}

			out.append(String.format("<div id=\"%s\" class=\"odh-page-widget\">\n%s\n</div>\n",
					pageWidget.getWidgetID().toString(), pageWidget.getMarkup()));
		}

		for (String asset : assets) {
			out.append(String.format("<script src=\"%s\" async></script>\n", asset));
		}

		out.append("</div>\n");
	}

}
