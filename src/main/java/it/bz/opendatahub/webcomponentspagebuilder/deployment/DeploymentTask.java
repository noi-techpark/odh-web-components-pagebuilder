package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;

import com.google.common.eventbus.EventBus;

import it.bz.opendatahub.webcomponentspagebuilder.data.Domain;
import it.bz.opendatahub.webcomponentspagebuilder.data.DomainsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationStatus;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PagePublicationRepository;
import it.bz.opendatahub.webcomponentspagebuilder.events.PagePublicationUpdated;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.PageRenderer;

public abstract class DeploymentTask implements Runnable {

	@FunctionalInterface
	public interface CompletionHandler {
		public void completed(DeploymentTask task);
	}

	public static class LogEntry {

		private LocalDateTime datetime;

		private String message;

		public LogEntry() {

		}

		public LocalDateTime getDatetime() {
			return datetime;
		}

		public void setDatetime(LocalDateTime datetime) {
			this.datetime = datetime;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}

	private ApplicationContext applicationContext;

	private PagePublication publication;

	private List<LogEntry> logEntries = new ArrayList<>();

	private Optional<CompletionHandler> completionHandler = Optional.empty();

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public EventBus getEventBus() {
		return getApplicationContext().getBean(EventBus.class);
	}

	public DomainsProvider getDomainsProvider() {
		return getApplicationContext().getBean(DomainsProvider.class);
	}

	public PageRenderer getPageRenderer() {
		return getApplicationContext().getBean(PageRenderer.class);
	}

	public PagePublicationRepository getRepository() {
		return getApplicationContext().getBean(PagePublicationRepository.class);
	}

	public PagePublication getPublication() {
		return publication;
	}

	public void setPublication(PagePublication publication) {
		this.publication = publication;
	}

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

	public Optional<LogEntry> getMostRecentLogEntry() {
		if (!logEntries.isEmpty()) {
			return Optional.of(logEntries.get(logEntries.size() - 1));
		}

		return Optional.empty();
	}

	public Optional<CompletionHandler> getCompletionHandler() {
		return completionHandler;
	}

	public void setCompletionHandler(CompletionHandler completionHandler) {
		this.completionHandler = Optional.of(completionHandler);
	}

	protected Optional<Domain> getDomain() {
		List<Domain> matchingDomains = getDomainsProvider().getAvailableDomains().stream()
				.filter(domain -> domain.getHostName().equals(publication.getDomainName()))
				.collect(Collectors.toList());

		if (!matchingDomains.isEmpty()) {
			return Optional.of(matchingDomains.get(0));
		}

		return Optional.empty();
	}

	@Override
	public void run() {
		PagePublication updatedPublication = getPublication();

		updatedPublication.setStatus(PagePublicationStatus.PROGRESSING);
		updatedPublication.setDeployedAt(LocalDateTime.now());
		updatedPublication = getRepository().save(updatedPublication);

		getEventBus().post(new PagePublicationUpdated(updatedPublication));

		try {
			execute();
		} catch (Exception e) {
			// TODO mark failed outcomes
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		updatedPublication.setStatus(PagePublicationStatus.COMPLETED);
		updatedPublication.setDeployedAt(LocalDateTime.now());
		updatedPublication = getRepository().save(updatedPublication);

		getEventBus().post(new PagePublicationUpdated(updatedPublication));

		getCompletionHandler().ifPresent(handler -> handler.completed(this));
	}

	protected abstract void execute() throws Exception;

}
