package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationContext;

import com.google.common.eventbus.EventBus;

import it.bz.opendatahub.webcomponentspagebuilder.data.Domain;
import it.bz.opendatahub.webcomponentspagebuilder.data.DomainsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationLog;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationStatus;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PagePublicationRepository;
import it.bz.opendatahub.webcomponentspagebuilder.events.PagePublicationUpdated;

public abstract class DeploymentTask implements Runnable, DeploymentProgressHandler {

	@FunctionalInterface
	public interface CompletionHandler {
		public void completed(DeploymentTask task);
	}

	private ApplicationContext applicationContext;

	private PagePublication publication;

	private Optional<CompletionHandler> completionHandler = Optional.empty();

	private Double progress = 0.0;

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

	public PagePublicationRepository getRepository() {
		return getApplicationContext().getBean(PagePublicationRepository.class);
	}

	public PagePublication getPublication() {
		return publication;
	}

	public void setPublication(PagePublication publication) {
		this.publication = publication;
	}

	public Optional<CompletionHandler> getCompletionHandler() {
		return completionHandler;
	}

	public void setCompletionHandler(CompletionHandler completionHandler) {
		this.completionHandler = Optional.of(completionHandler);
	}

	protected Optional<Domain> getDomain() {
		List<Domain> matchingDomains = getDomainsProvider().getAvailableDomains().stream()
				.filter(domain -> domain.getHostName().equals(publication.getSite().getDomainName()))
				.collect(Collectors.toList());

		if (!matchingDomains.isEmpty()) {
			return Optional.of(matchingDomains.get(0));
		}

		return Optional.empty();
	}

	public Double getProgress() {
		return progress;
	}

	@Override
	public void run() {
		PagePublication updatedPublication = getRepository().getOne(getPublication().getId());

		updatedPublication.setStatus(PagePublicationStatus.PROGRESSING);
		updatedPublication.setUpdatedAt(LocalDateTime.now());
		updatedPublication = getRepository().save(updatedPublication);

		getEventBus().post(new PagePublicationUpdated(updatedPublication));

		info("Beginning deployment");

		execute();

		info("Deployment completed");

		updatedPublication = getRepository().getOne(getPublication().getId());

		updatedPublication.setStatus(PagePublicationStatus.COMPLETED);
		updatedPublication.setUpdatedAt(LocalDateTime.now());
		updatedPublication = getRepository().save(updatedPublication);

		getEventBus().post(new PagePublicationUpdated(updatedPublication));

		getCompletionHandler().ifPresent(handler -> handler.completed(this));
	}

	private void saveLogEntry(PagePublicationLog.Type type, String message, Throwable cause) {
		PagePublicationLog entry = new PagePublicationLog();
		entry.setDatetime(LocalDateTime.now());
		entry.setType(type);

		if (message != null) {
			entry.setText(message);
		}

		if (cause != null) {
			entry.setStackTrace(ExceptionUtils.getStackTrace(cause));
		}

		PagePublicationRepository repo = applicationContext.getBean(PagePublicationRepository.class);

		PagePublication updatedPublication = repo.getOne(publication.getId());
		updatedPublication.add(entry);

		updatedPublication = repo.save(updatedPublication);

		getEventBus().post(new PagePublicationUpdated(updatedPublication));
	}

	@Override
	public void progress(Double progress) {
		this.progress = progress;

		getEventBus().post(new PagePublicationUpdated(getPublication()));
	}

	@Override
	public void info(String message) {
		saveLogEntry(PagePublicationLog.Type.INFO, message, null);
	}

	@Override
	public void warning(String message) {
		saveLogEntry(PagePublicationLog.Type.WARNING, message, null);
	}

	@Override
	public void error(String message) {
		saveLogEntry(PagePublicationLog.Type.ERROR, message, null);
	}

	@Override
	public void error(Throwable cause) {
		saveLogEntry(PagePublicationLog.Type.ERROR, null, cause);
	}

	@Override
	public void error(String message, Throwable cause) {
		saveLogEntry(PagePublicationLog.Type.ERROR, message, cause);
	}

	protected abstract void execute();

}
