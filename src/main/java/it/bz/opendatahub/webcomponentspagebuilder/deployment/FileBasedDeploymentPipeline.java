package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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
	public void deploy(DeploymentManifest manifest, DeploymentPayload payload,
			DeploymentProgressHandler progressHandler) {
		progressHandler.progress(0.0);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// noop
		}

		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.GROUP_EXECUTE);
		perms.add(PosixFilePermission.OTHERS_READ);
		perms.add(PosixFilePermission.OTHERS_EXECUTE);

		try {
			File baseFolder = getBaseFolder(manifest);

			progressHandler.info("Making sure the directory exists");

			baseFolder.mkdirs();

			progressHandler.info("Making sure the directory permissions are correct");

			baseFolder.setReadable(true);
			baseFolder.setWritable(true, true);
			baseFolder.setExecutable(true);

			Files.setPosixFilePermissions(Paths.get(baseFolder.getPath()), perms);

			for (PayloadFile payloadFile : payload.getFiles()) {
				File file = new File(baseFolder, payloadFile.getName());

				file.getParentFile().mkdirs();

				progressHandler.info(String.format("Processing file: '%s'", payloadFile.getName()));

				file.createNewFile();

				progressHandler.info(String.format("Configure permissions for file: '%s'", payloadFile.getName()));

				file.setReadable(true);
				file.setWritable(true, true);
				file.setExecutable(true);

				Files.setPosixFilePermissions(Paths.get(file.getPath()), perms);

				progressHandler.info(String.format("Copy contents to file: '%s'", file.getAbsolutePath()));

				IOUtils.write(payloadFile.getContent(), new FileOutputStream(file));
			}

			progressHandler.progress(1.0);
		} catch (IOException e) {
			progressHandler.error(e);

			throw new RuntimeException(e);
		}
	}

	@Override
	public void undeploy(DeploymentManifest manifest, DeploymentProgressHandler progressHandler) {
		progressHandler.progress(0.0);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// noop
		}

		File baseFolder = getBaseFolder(manifest);

		try {
			FileUtils.deleteDirectory(baseFolder);
		} catch (IOException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}

		progressHandler.progress(1.0);
	}

}
