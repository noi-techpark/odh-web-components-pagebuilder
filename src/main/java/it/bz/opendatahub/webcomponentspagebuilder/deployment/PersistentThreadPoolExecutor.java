package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.spring.annotation.SpringComponent;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationStatus;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PagePublicationRepository;
import it.bz.opendatahub.webcomponentspagebuilder.ui.controllers.PublishingController;

@SpringComponent
@Scope("prototype")
public class PersistentThreadPoolExecutor extends ThreadPoolExecutor {

	@Autowired
	PublishingController controller;

	@Autowired
	PagePublicationRepository repo;

	public PersistentThreadPoolExecutor() {
		super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	@Override
	protected void afterExecute(Runnable task, Throwable throwable) {
		super.afterExecute(task, throwable);

		if (throwable != null) {
			throwable.printStackTrace();

			DeploymentTask deploymentTask = (DeploymentTask) task;

			deploymentTask.error(throwable.getMessage(), throwable);

			if (!(throwable instanceof NonRecoverableDeploymentException)) {
				deploymentTask.info("Restarting failed operation");

				PagePublication publication = repo.getOne(deploymentTask.getPublication().getId());
				publication.setStatus(PagePublicationStatus.PENDING);
				publication.setUpdatedAt(LocalDateTime.now());

				publication = repo.save(publication);

				controller.process(publication);
			}
		}
	}

}
