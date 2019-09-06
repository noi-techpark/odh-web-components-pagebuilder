package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.TagSet;

import it.bz.opendatahub.webcomponentspagebuilder.deployment.DeploymentPayload.PayloadFile;

/**
 * Deployment pipeline for pushing a page to an AWS-based infrastructure,
 * constituted mainly of S3 for file storage.
 * 
 * @author danielrampanelli
 */
public class AwsBasedDeploymentPipeline implements DeploymentPipeline {

	private String regionName;

	private AWSStaticCredentialsProvider credentialsProvider;

	public AwsBasedDeploymentPipeline(String regionName, String accessKey, String accessSecret) {
		this.regionName = regionName;
		this.credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, accessSecret));
	}

	private String generateBucketName(DeploymentManifest manifest) {
		return String.format("odh-pagebuilder-%s", DigestUtils.md5Hex(manifest.getDomainName()));
	}

	@Override
	public void deploy(DeploymentManifest manifest, DeploymentPayload payload) {
		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(regionName).withCredentials(credentialsProvider)
				.build();

		String bucketName = generateBucketName(manifest);

		if (s3.listBuckets().stream().filter(bucket -> bucket.getName().equals(bucketName)).count() == 0) {
			s3.createBucket(bucketName);
		}

		HashMap<String, String> tags = new HashMap<>();
		tags.put("domain", manifest.getDomainName());

		BucketTaggingConfiguration tagsConfiguration = new BucketTaggingConfiguration();
		tagsConfiguration.withTagSets(new TagSet(tags));

		s3.setBucketTaggingConfiguration(bucketName, tagsConfiguration);

		s3.setBucketWebsiteConfiguration(bucketName, new BucketWebsiteConfiguration("index.html", "index.html"));

		s3.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);

		for (PayloadFile payloadFile : payload.getFiles()) {
			String key = payloadFile.getName();

			if (manifest.getPathName() != null) {
				key = String.format("%s/%s", manifest.getPathName(), key);
			}

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentDisposition("inline");
			metadata.setContentType(payloadFile.getContentType());
			metadata.setContentLength(payloadFile.getContentLength());

			s3.putObject(new PutObjectRequest(bucketName, key, payloadFile.getContent(), metadata));

			s3.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
		}
	}

	@Override
	public void undeploy(DeploymentManifest manifest) {
		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(regionName).withCredentials(credentialsProvider)
				.build();

		String bucketName = generateBucketName(manifest);

		ObjectListing objects = s3.listObjects(bucketName);
		for (S3ObjectSummary summary : objects.getObjectSummaries()) {
			s3.deleteObject(bucketName, summary.getKey());
		}
	}

}
