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
import it.bz.opendatahub.webcomponentspagebuilder.data.Page;

@Component
public class PageRenderer {

	public String renderPage(Page page) throws TemplateNotFoundException, MalformedTemplateNameException,
			ParseException, IOException, TemplateException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Writer writer = new OutputStreamWriter(outputStream);

		Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
		configuration.setClassForTemplateLoading(PageRenderer.class, "/templates");
		configuration.setSharedVariable("metas", new PageMetaTagsDirective(page));
		configuration.setSharedVariable("contents", new PageContentsDirective(page));

		Template template = configuration.getTemplate("default.ftl");
		template.process(new HashMap<>(), writer);

		writer.close();

		return outputStream.toString();
	}

}
