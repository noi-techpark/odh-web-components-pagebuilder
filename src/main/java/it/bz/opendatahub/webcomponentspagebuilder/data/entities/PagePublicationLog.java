package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

import java.time.LocalDateTime;

public class PagePublicationLog {

	public enum Type {

		INFO,

		WARNING,

		ERROR
	}

	private LocalDateTime datetime;

	private PagePublicationLog.Type type;

	private String text;

	private String stackTrace;

	public PagePublicationLog() {

	}

	public LocalDateTime getDatetime() {
		return datetime;
	}

	public void setDatetime(LocalDateTime datetime) {
		this.datetime = datetime;
	}

	public PagePublicationLog.Type getType() {
		return type;
	}

	public void setType(PagePublicationLog.Type type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

}