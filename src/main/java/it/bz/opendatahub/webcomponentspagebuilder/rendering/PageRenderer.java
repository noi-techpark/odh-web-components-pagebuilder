package it.bz.opendatahub.webcomponentspagebuilder.rendering;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import org.springframework.stereotype.Component;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;

/**
 * Component for rendering a page version into an actual HTML document/page, by
 * rendering all contents and injecting all related assets.
 * 
 * @author danielrampanelli
 */
@Component
public class PageRenderer {

	private String render(PageVersion pageVersion, String templateFile) throws TemplateNotFoundException,
			MalformedTemplateNameException, ParseException, IOException, TemplateException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Writer writer = new OutputStreamWriter(outputStream);

		Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
		configuration.setClassForTemplateLoading(PageRenderer.class, "/templates/main");
		configuration.setSharedVariable("metatags", new PageMetaTagsDirective(pageVersion));
		configuration.setSharedVariable("contents", new PageContentsDirective(pageVersion));
		configuration.setSharedVariable("widgets", new PageWidgetsDirective(pageVersion));

		Template template = configuration.getTemplate(templateFile);
		template.process(new HashMap<>(), writer);

		writer.close();

		return outputStream.toString();
	}

	public String renderPage(PageVersion pageVersion) throws TemplateNotFoundException, MalformedTemplateNameException,
			ParseException, IOException, TemplateException {
		return render(pageVersion, "index.ftl");
	}

	public String renderPreview(PageVersion pageVersion) throws TemplateNotFoundException,
			MalformedTemplateNameException, ParseException, IOException, TemplateException {
		return render(pageVersion, "preview.ftl");
	}

}
