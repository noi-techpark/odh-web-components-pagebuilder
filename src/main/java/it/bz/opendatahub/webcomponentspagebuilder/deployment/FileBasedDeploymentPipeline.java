package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import it.bz.opendatahub.webcomponentspagebuilder.deployment.DeploymentPayload.PayloadFile;

/**
 * Deployment pipeline for publishing pages to local folders. Can be used when
 * using a single domain or when allowing to create custom subdomains.
 * 
 * @author danielrampanelli
 */
public class FileBasedDeploymentPipeline implements DeploymentPipeline {

	private File basePath;

	private boolean multipleDomains;

	public FileBasedDeploymentPipeline(String path) {
		this(path, false);
	}

	public FileBasedDeploymentPipeline(String path, boolean multipleDomains) {
		this.basePath = new File(path);
		this.multipleDomains = multipleDomains;
	}

	private File getBaseFolder(DeploymentManifest manifest) {
		File baseFolder = basePath;

		if (multipleDomains) {
			baseFolder = new File(baseFolder, manifest.getDomainName());
		}

		if (manifest.getPathName() != null) {
			baseFolder = new File(baseFolder, manifest.getPathName());
		}

		return baseFolder;
	}

	@Override
	public void deploy(DeploymentManifest manifest, DeploymentPayload payload) {
		try {
			File baseFolder = getBaseFolder(manifest);

			baseFolder.mkdirs();

			for (PayloadFile payloadFile : payload.getFiles()) {
				IOUtils.copyLarge(payloadFile.getContent(),
						new FileOutputStream(new File(baseFolder, payloadFile.getName())));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void undeploy(DeploymentManifest manifest) {
		File baseFolder = getBaseFolder(manifest);

		for (File file : baseFolder.listFiles()) {
			file.delete();
		}
	}

}
