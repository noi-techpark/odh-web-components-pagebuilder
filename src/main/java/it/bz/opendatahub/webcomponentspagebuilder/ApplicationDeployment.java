package it.bz.opendatahub.webcomponentspagebuilder;

/**
 * Object for holding all relevant information of the deployed application.
 * 
 * @author danielrampanelli
 */
public class ApplicationDeployment {

	private String baseUrl;

	public ApplicationDeployment(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

}
