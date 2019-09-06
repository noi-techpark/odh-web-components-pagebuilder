package it.bz.opendatahub.webcomponentspagebuilder.deployment;

/**
 * Component for deployment of page versions, supports also removal or
 * undeployment of resources.
 * 
 * @author danielrampanelli
 */
public interface DeploymentPipeline {

	public void deploy(DeploymentManifest manifest, DeploymentPayload payload);

	public void undeploy(DeploymentManifest manifest);

}
