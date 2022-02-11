package it.bz.opendatahub.webcomponentspagebuilder.deployment;

public class NonRecoverableDeploymentException extends RuntimeException {
	
	private static final long serialVersionUID = 1606929203052834752L;

	public NonRecoverableDeploymentException(String message) {
		super(message);
	}

}
