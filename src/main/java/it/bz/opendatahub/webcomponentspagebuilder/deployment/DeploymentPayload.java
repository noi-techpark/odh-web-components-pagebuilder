package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

/**
 * Object containing all files and assets that need to be deployed in order for
 * the page to be displayed correctly.
 * 
 * @author danielrampanelli
 */
public class DeploymentPayload {

	public static class PayloadFile {

		private String contentType;

		private String name;

		private byte[] content;

		public PayloadFile() {

		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public PayloadFile withContentType(String contentType) {
			setContentType(contentType);
			return this;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public PayloadFile withName(String name) {
			setName(name);
			return this;
		}

		public byte[] getContent() {
			return content;
		}

		public Long getContentLength() {
			return (long) content.length;
		}

		public InputStream getContentStream() {
			return new ByteArrayInputStream(content);
		}

		public void setContent(byte[] content) {
			this.content = content;
		}

		public PayloadFile withInlineContent(String content) {
			setContent(content.getBytes());
			return this;
		}

		public PayloadFile withContent(String content) {
			setContent(content.getBytes());
			return this;
		}
		
		public PayloadFile withContent(byte[] content) {
			setContent(content);
			return this;
		}

		public PayloadFile withContent(InputStream content) throws IOException {
			setContent(IOUtils.toByteArray(content));
			return this;
		}

	}

	private Set<PayloadFile> files = new HashSet<>();

	public DeploymentPayload() {

	}

	public Set<PayloadFile> getFiles() {
		return files;
	}
	
	public void add(PayloadFile file) {
		files.add(file);
	}

	public DeploymentPayload withFiles(PayloadFile... payloadFiles) {
		for (PayloadFile file : payloadFiles) {
			files.add(file);
		}

		return this;
	}

}
