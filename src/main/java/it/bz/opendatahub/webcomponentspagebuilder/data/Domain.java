package it.bz.opendatahub.webcomponentspagebuilder.data;

import it.bz.opendatahub.webcomponentspagebuilder.deployment.DeploymentPipeline;

/**
 * Configuration object used to define a possible domain that can be associated
 * and used during the deployment of pages.
 * 
 * @author danielrampanelli
 */
public class Domain {

	private String hostName;

	private Boolean allowSubdomains;

	private DeploymentPipeline deploymentPipeline;

	public Domain() {

	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Boolean getAllowSubdomains() {
		return allowSubdomains;
	}

	public void setAllowSubdomains(Boolean allowSubdomains) {
		this.allowSubdomains = allowSubdomains;
	}

	public DeploymentPipeline getDeploymentPipeline() {
		return deploymentPipeline;
	}

	public void setDeploymentPipeline(DeploymentPipeline deploymentPipeline) {
		this.deploymentPipeline = deploymentPipeline;
	}

}
