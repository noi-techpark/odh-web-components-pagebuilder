package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.util.Optional;

import it.bz.opendatahub.webcomponentspagebuilder.data.Domain;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;

public class UnpublishDeploymentTask extends DeploymentTask {

	@Override
	protected void execute() {
		Optional<Domain> domain = getDomain();

		if (domain.isPresent()) {
			PageVersion pageVersion = getPublication().getVersion();

			DeploymentManifest manifest = new DeploymentManifest(pageVersion);

			DeploymentPipeline deploymentPipeline = domain.get().getDeploymentPipeline();
			deploymentPipeline.undeploy(manifest, this);

			PageRepository pagesRepository = getApplicationContext().getBean(PageRepository.class);

			Page freshPage = pagesRepository.getOne(getPublication().getPage().getId());
			freshPage.setSite(null);

			pagesRepository.save(freshPage);
		}
	}

}
