package it.bz.opendatahub.webcomponentspagebuilder.data;

public class Domain {

	private String hostName;

	private Boolean allowSubdomains;

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

}
