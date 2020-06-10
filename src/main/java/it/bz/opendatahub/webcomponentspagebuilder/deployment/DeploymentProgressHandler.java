package it.bz.opendatahub.webcomponentspagebuilder.deployment;

public interface DeploymentProgressHandler {

	public void progress(Double progress);

	public void info(String message);

	public void warning(String message);

	public void error(String message);

	public void error(Throwable cause);

	public void error(String message, Throwable cause);

}
