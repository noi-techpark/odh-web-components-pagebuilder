package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.util.Optional;

import it.bz.opendatahub.webcomponentspagebuilder.data.Domain;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;

public class PublishDeploymentTask extends DeploymentTask {

	@Override
	protected void execute() throws Exception {
		Optional<Domain> domain = getDomain();

		if (domain.isPresent()) {
			PageVersion pageVersion = getPublication().getVersion();

			DeploymentManifest manifest = new DeploymentManifest(pageVersion);
			DeploymentPayload payload = new DeploymentPayload(getPageRenderer(), pageVersion);

			DeploymentPipeline deploymentPipeline = domain.get().getDeploymentPipeline();
			deploymentPipeline.deploy(manifest, payload);
		}
	}

}
