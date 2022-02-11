package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.util.Optional;

import it.bz.opendatahub.webcomponentspagebuilder.data.Domain;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;

public class PublishDeploymentTask extends DeploymentTask implements DeploymentProgressHandler {

	private DeploymentPayload payload;

	public PublishDeploymentTask(DeploymentPayload payload) {
		this.payload = payload;
	}

	@Override
	protected void execute() {
		Optional<Domain> domain = getDomain();

		if (domain.isPresent()) {
			PageVersion pageVersion = getPublication().getVersion();

			DeploymentManifest manifest = new DeploymentManifest(pageVersion);

			DeploymentPipeline deploymentPipeline = domain.get().getDeploymentPipeline();
			deploymentPipeline.deploy(manifest, payload, this);
		}
	}

}
