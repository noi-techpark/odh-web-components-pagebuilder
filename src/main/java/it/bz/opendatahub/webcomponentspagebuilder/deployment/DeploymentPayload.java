package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import freemarker.template.TemplateException;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.PageRenderer;

/**
 * Object containing all files and assets that need to be deployed in order for
 * the page to be displayed correctly.
 * 
 * @author danielrampanelli
 */
public class DeploymentPayload {

	public static class PayloadFile {

		private String contentType;

		private Long contentLength;

		private String name;

		private InputStream content;

		public PayloadFile() {

		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public Long getContentLength() {
			return contentLength;
		}

		public void setContentLength(Long contentLength) {
			this.contentLength = contentLength;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public InputStream getContent() {
			return content;
		}

		public void setContent(InputStream content) {
			this.content = content;
		}

	}

	private Set<PayloadFile> files = new HashSet<>();

	public DeploymentPayload(PageRenderer pageRenderer, PageVersion pageVersion) {
		try {
			String renderedPage = pageRenderer.renderPage(pageVersion);

			byte[] pageBytes = renderedPage.getBytes();

			PayloadFile payloadFile = new PayloadFile();
			payloadFile.setName("index.html");
			payloadFile.setContentType("text/html");
			payloadFile.setContentLength((long) pageBytes.length);
			payloadFile.setContent(new ByteArrayInputStream(pageBytes));

			files.add(payloadFile);
		} catch (IOException | TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Set<PayloadFile> getFiles() {
		return files;
	}

}
