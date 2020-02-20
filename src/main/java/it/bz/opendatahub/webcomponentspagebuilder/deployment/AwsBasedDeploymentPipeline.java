package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.certificatemanager.AWSCertificateManagerClientBuilder;
import com.amazonaws.services.certificatemanager.model.CertificateDetail;
import com.amazonaws.services.certificatemanager.model.CertificateSummary;
import com.amazonaws.services.certificatemanager.model.DescribeCertificateRequest;
import com.amazonaws.services.certificatemanager.model.DescribeCertificateResult;
import com.amazonaws.services.certificatemanager.model.DomainValidation;
import com.amazonaws.services.certificatemanager.model.ListCertificatesRequest;
import com.amazonaws.services.certificatemanager.model.ListCertificatesResult;
import com.amazonaws.services.certificatemanager.model.RequestCertificateRequest;
import com.amazonaws.services.certificatemanager.model.RequestCertificateResult;
import com.amazonaws.services.certificatemanager.model.ResourceRecord;
import com.amazonaws.services.cloudfront.AmazonCloudFront;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClientBuilder;
import com.amazonaws.services.cloudfront.model.Aliases;
import com.amazonaws.services.cloudfront.model.CacheBehavior;
import com.amazonaws.services.cloudfront.model.CacheBehaviors;
import com.amazonaws.services.cloudfront.model.CookiePreference;
import com.amazonaws.services.cloudfront.model.CreateDistributionRequest;
import com.amazonaws.services.cloudfront.model.CreateDistributionResult;
import com.amazonaws.services.cloudfront.model.DefaultCacheBehavior;
import com.amazonaws.services.cloudfront.model.Distribution;
import com.amazonaws.services.cloudfront.model.DistributionConfig;
import com.amazonaws.services.cloudfront.model.DistributionSummary;
import com.amazonaws.services.cloudfront.model.ForwardedValues;
import com.amazonaws.services.cloudfront.model.GetDistributionRequest;
import com.amazonaws.services.cloudfront.model.ListDistributionsRequest;
import com.amazonaws.services.cloudfront.model.ListDistributionsResult;
import com.amazonaws.services.cloudfront.model.LoggingConfig;
import com.amazonaws.services.cloudfront.model.MinimumProtocolVersion;
import com.amazonaws.services.cloudfront.model.Origin;
import com.amazonaws.services.cloudfront.model.Origins;
import com.amazonaws.services.cloudfront.model.PriceClass;
import com.amazonaws.services.cloudfront.model.S3OriginConfig;
import com.amazonaws.services.cloudfront.model.SSLSupportMethod;
import com.amazonaws.services.cloudfront.model.TrustedSigners;
import com.amazonaws.services.cloudfront.model.UpdateDistributionRequest;
import com.amazonaws.services.cloudfront.model.UpdateDistributionResult;
import com.amazonaws.services.cloudfront.model.ViewerCertificate;
import com.amazonaws.services.cloudfront.model.ViewerProtocolPolicy;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.CreateHostedZoneRequest;
import com.amazonaws.services.route53.model.CreateHostedZoneResult;
import com.amazonaws.services.route53.model.GetChangeRequest;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ResourceRecordSet;
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
import com.amazonaws.waiters.WaiterParameters;

import it.bz.opendatahub.webcomponentspagebuilder.deployment.DeploymentPayload.PayloadFile;

/**
 * Deployment pipeline for pushing a page to an AWS-based infrastructure,
 * constituted of Route53 (DNS), CloudFront (Serving requests) and S3 (file
 * storage).
 * 
 * @author danielrampanelli
 */
public class AwsBasedDeploymentPipeline implements DeploymentPipeline {

	private AWSCertificateManager certificateManager;

	private AmazonRoute53 route53;

	private AmazonCloudFront cloudfront;

	private AmazonS3 s3;

	public AwsBasedDeploymentPipeline(String regionName, String accessKey, String accessSecret) {
		AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
				new BasicAWSCredentials(accessKey, accessSecret));

		this.certificateManager = AWSCertificateManagerClientBuilder.standard().withRegion(regionName)
				.withCredentials(credentialsProvider).build();

		this.route53 = AmazonRoute53ClientBuilder.standard().withCredentials(credentialsProvider).withRegion(regionName)
				.build();

		this.cloudfront = AmazonCloudFrontClientBuilder.standard().withRegion(regionName)
				.withCredentials(credentialsProvider).build();

		this.s3 = AmazonS3ClientBuilder.standard().withRegion(regionName).withCredentials(credentialsProvider).build();
	}

	private String generateBucketName(DeploymentManifest manifest) {
		return String.format("odh-pagebuilder-%s", DigestUtils.md5Hex(manifest.getUri()));
	}

	@Override
	public void deploy(DeploymentManifest manifest, DeploymentPayload payload) {
		String bucketName = deployBucket(manifest, payload);

		HostedZone hostedZone = ensureHostedZone(manifest);

		CertificateDetail certificate = ensureCertificate(manifest, hostedZone);

		Distribution distribution = ensureDistribution(manifest, bucketName, certificate);

		ensureDomainRecords(manifest, hostedZone, distribution);
	}

	private String deployBucket(DeploymentManifest manifest, DeploymentPayload payload) {
		String bucketName = generateBucketName(manifest);

		if (s3.listBuckets().stream().filter(bucket -> bucket.getName().equals(bucketName)).count() == 0) {
			s3.createBucket(bucketName);
		}

		HashMap<String, String> tags = new HashMap<>();
		tags.put("URI", manifest.getUri());

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

		return bucketName;
	}

	private HostedZone ensureHostedZone(DeploymentManifest manifest) {
		List<HostedZone> hostedZones = route53.listHostedZones().getHostedZones().stream()
				.filter(zone -> zone.getName().equals(manifest.getDomainName() + ".")).collect(Collectors.toList());

		HostedZone hostedZone = null;

		if (hostedZones.isEmpty()) {
			CreateHostedZoneRequest createZoneRequest = new CreateHostedZoneRequest();
			createZoneRequest.setCallerReference(System.currentTimeMillis() + "");
			createZoneRequest.setName(manifest.getDomainName());

			CreateHostedZoneResult createZoneResult = route53.createHostedZone(createZoneRequest);

			hostedZone = createZoneResult.getHostedZone();

			route53.waiters().resourceRecordSetsChanged().run(
					new WaiterParameters<>(new GetChangeRequest().withId(createZoneResult.getChangeInfo().getId())));
		} else {
			hostedZone = hostedZones.get(0);
		}

		return hostedZone;
	}

	private CertificateDetail ensureCertificate(DeploymentManifest manifest, HostedZone hostedZone) {
		ListCertificatesRequest listCertificatesRequest = new ListCertificatesRequest();

		ListCertificatesResult listCertificatesResult = certificateManager.listCertificates(listCertificatesRequest);

		List<CertificateSummary> certificates = listCertificatesResult.getCertificateSummaryList().stream()
				.filter(certificate -> certificate.getDomainName().equals(manifest.getDomainName()))
				.collect(Collectors.toList());

		CertificateDetail certificate = null;

		if (certificates.isEmpty()) {
			ArrayList<String> san = new ArrayList<>();
			san.add(manifest.getDomainName());

			RequestCertificateRequest createCertificateRequest = new RequestCertificateRequest();
			createCertificateRequest.setDomainName(manifest.getDomainName());
			createCertificateRequest.setIdempotencyToken(System.currentTimeMillis() + "");
			createCertificateRequest.setSubjectAlternativeNames(san);
			createCertificateRequest.setValidationMethod("DNS");

			RequestCertificateResult createCertificateResult = certificateManager
					.requestCertificate(createCertificateRequest);

			String certificateArn = createCertificateResult.getCertificateArn();

			DescribeCertificateResult getCertificateResult = null;

			do {
				DescribeCertificateRequest getCertificateRequest = new DescribeCertificateRequest();
				getCertificateRequest.setCertificateArn(certificateArn);

				getCertificateResult = certificateManager.describeCertificate(getCertificateRequest);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// noop
				}
			} while (getCertificateResult.getCertificate().getStatus().equals("PENDING_VALIDATION")
					&& (getCertificateResult.getCertificate().getDomainValidationOptions() == null
							|| getCertificateResult.getCertificate().getDomainValidationOptions().isEmpty()
							|| getCertificateResult.getCertificate().getDomainValidationOptions().get(0)
									.getResourceRecord() == null));

			certificate = getCertificateResult.getCertificate();

			if (certificate.getStatus().equals("PENDING_VALIDATION")) {
				List<DomainValidation> validationOptions = certificate.getDomainValidationOptions();
				DomainValidation validationOption = validationOptions.get(0);
				ResourceRecord validationRecord = validationOption.getResourceRecord();

				Change validationChange = new Change().withAction("UPSERT")
						.withResourceRecordSet(new ResourceRecordSet().withName(validationRecord.getName())
								.withType(validationRecord.getType()).withTTL(300L)
								.withResourceRecords(new com.amazonaws.services.route53.model.ResourceRecord(
										validationRecord.getValue())));

				ChangeResourceRecordSetsRequest createValidationRecordRequest = new ChangeResourceRecordSetsRequest(
						hostedZone.getId(), new ChangeBatch().withChanges(validationChange));

				ChangeResourceRecordSetsResult createValidationRecordResult = route53
						.changeResourceRecordSets(createValidationRecordRequest);

				route53.waiters().resourceRecordSetsChanged().run(new WaiterParameters<>(
						new GetChangeRequest().withId(createValidationRecordResult.getChangeInfo().getId())));

				certificateManager.waiters().certificateValidated().run(new WaiterParameters<>(
						new DescribeCertificateRequest().withCertificateArn(certificate.getCertificateArn())));
			}
		} else {
			CertificateSummary certificateSummary = certificates.get(0);

			certificate = new CertificateDetail();
			certificate.setCertificateArn(certificateSummary.getCertificateArn());
			certificate.setDomainName(certificateSummary.getDomainName());
		}

		return certificate;
	}

	private Distribution ensureDistribution(DeploymentManifest manifest, String bucketName,
			CertificateDetail certificate) {
		ListDistributionsRequest listDistributionsRequest = new ListDistributionsRequest();

		ListDistributionsResult listDistributionsResult = cloudfront.listDistributions(listDistributionsRequest);

		List<DistributionSummary> distributions = listDistributionsResult.getDistributionList().getItems().stream()
				.filter(distribution -> {
					return distribution.getAliases().getItems().stream()
							.filter(item -> item.equals(manifest.getDomainName())).count() > 0;
				}).collect(Collectors.toList());

		String originDomainName = String.format("%s.s3.amazonaws.com", bucketName);

		Distribution distribution = null;

		if (distributions.isEmpty()) {
			Aliases aliases = new Aliases().withItems(manifest.getDomainName()).withQuantity(1);

			Origins origins = new Origins().withItems(new Origin().withId(bucketName).withDomainName(originDomainName)
					.withS3OriginConfig(new S3OriginConfig().withOriginAccessIdentity(""))).withQuantity(1);

			DistributionConfig distributionConfig = new DistributionConfig();
			distributionConfig.setAliases(aliases);
			distributionConfig.setCallerReference(System.currentTimeMillis() + "");
			distributionConfig.setComment(manifest.getDomainName());
			distributionConfig.withDefaultRootObject("");
			distributionConfig.withEnabled(true);
			distributionConfig.withOrigins(origins);
			distributionConfig.withPriceClass(PriceClass.PriceClass_100);

			distributionConfig
					.setViewerCertificate(new ViewerCertificate().withACMCertificateArn(certificate.getCertificateArn())
							.withMinimumProtocolVersion(MinimumProtocolVersion.TLSv11_2016)
							.withSSLSupportMethod(SSLSupportMethod.SniOnly));

			distributionConfig.withDefaultCacheBehavior(new DefaultCacheBehavior().withTargetOriginId(bucketName)
					.withForwardedValues(new ForwardedValues().withQueryString(false)
							.withCookies(new CookiePreference().withForward("none")))
					.withTrustedSigners(new TrustedSigners().withQuantity(0).withEnabled(false))
					.withViewerProtocolPolicy(ViewerProtocolPolicy.AllowAll).withMinTTL(36000L));

			distributionConfig.withCacheBehaviors(new CacheBehaviors().withQuantity(0));

			distributionConfig.withLogging(
					new LoggingConfig().withEnabled(false).withBucket("").withPrefix("").withIncludeCookies(false));

			CreateDistributionRequest createDistributionRequest = new CreateDistributionRequest()
					.withDistributionConfig(distributionConfig);

			CreateDistributionResult createDistributionResult = cloudfront
					.createDistribution(createDistributionRequest);

			distribution = createDistributionResult.getDistribution();

			cloudfront.waiters().distributionDeployed().run(new WaiterParameters<>(
					new GetDistributionRequest().withId(createDistributionResult.getDistribution().getId())));
		} else {
			DistributionSummary distributionSummary = distributions.get(0);

			distribution = new Distribution(distributionSummary.getId(), distributionSummary.getStatus(),
					distributionSummary.getDomainName());

			DistributionConfig configuration = distribution.getDistributionConfig();

			if (configuration.getOrigins().getItems().stream()
					.filter(origin -> origin.getDomainName().equals(originDomainName)).count() == 0) {
				configuration.getOrigins().getItems()
						.add(new Origin().withId(bucketName).withDomainName(originDomainName)
								.withS3OriginConfig(new S3OriginConfig().withOriginAccessIdentity("")));

				configuration.getCacheBehaviors().getItems()
						.add(new CacheBehavior()
								.withPathPattern(StringUtils.isEmpty(manifest.getPathName())
										? String.format("%s/*", manifest.getPathName())
										: "/*")
								.withTargetOriginId(bucketName)
								.withForwardedValues(new ForwardedValues().withQueryString(false)
										.withCookies(new CookiePreference().withForward("none")))
								.withTrustedSigners(new TrustedSigners().withQuantity(0).withEnabled(false))
								.withViewerProtocolPolicy(ViewerProtocolPolicy.AllowAll).withMinTTL(36000L));

				UpdateDistributionRequest updateDistributionRequest = new UpdateDistributionRequest()
						.withDistributionConfig(configuration);

				UpdateDistributionResult updateDistributionResult = cloudfront
						.updateDistribution(updateDistributionRequest);

				cloudfront.waiters().distributionDeployed().run(new WaiterParameters<>(
						new GetDistributionRequest().withId(updateDistributionResult.getDistribution().getId())));
			}
		}

		return distribution;
	}

	private void ensureDomainRecords(DeploymentManifest manifest, HostedZone hostedZone, Distribution distribution) {
		Change validationChange = new Change().withAction("UPSERT")
				.withResourceRecordSet(new ResourceRecordSet().withName(manifest.getDomainName()).withType("CNAME")
						.withTTL(300L).withResourceRecords(
								new com.amazonaws.services.route53.model.ResourceRecord(distribution.getDomainName())));

		ChangeResourceRecordSetsRequest createFrontRecordRequest = new ChangeResourceRecordSetsRequest(
				hostedZone.getId(), new ChangeBatch().withChanges(validationChange));

		ChangeResourceRecordSetsResult createFrontRecordResult = route53
				.changeResourceRecordSets(createFrontRecordRequest);

		route53.waiters().resourceRecordSetsChanged().run(
				new WaiterParameters<>(new GetChangeRequest().withId(createFrontRecordResult.getChangeInfo().getId())));
	}

	@Override
	public void undeploy(DeploymentManifest manifest) {
		String bucketName = generateBucketName(manifest);

		cleanupDistribution(manifest, bucketName);

		cleanupBucket(manifest, bucketName);
	}

	private void cleanupDistribution(DeploymentManifest manifest, String bucketName) {
		ListDistributionsRequest listDistributionsRequest = new ListDistributionsRequest();

		ListDistributionsResult listDistributionsResult = cloudfront.listDistributions(listDistributionsRequest);

		List<DistributionSummary> distributions = listDistributionsResult.getDistributionList().getItems().stream()
				.filter(distribution -> {
					return distribution.getAliases().getItems().stream()
							.filter(item -> item.equals(manifest.getDomainName())).count() > 0;
				}).collect(Collectors.toList());

		String originDomainName = String.format("%s.s3.amazonaws.com", bucketName);

		if (!distributions.isEmpty()) {
			DistributionSummary distributionSummary = distributions.get(0);

			Distribution distribution = new Distribution(distributionSummary.getId(), distributionSummary.getStatus(),
					distributionSummary.getDomainName());

			DistributionConfig configuration = distribution.getDistributionConfig();

			Origins origins = configuration.getOrigins();

			origins.setItems(origins.getItems().stream()
					.filter(origin -> !origin.getDomainName().equals(originDomainName)).collect(Collectors.toList()));

			CacheBehaviors cacheBehaviors = configuration.getCacheBehaviors();

			cacheBehaviors.setItems(cacheBehaviors.getItems().stream()
					.filter(cacheBehavior -> !cacheBehavior.getTargetOriginId().equals(bucketName))
					.collect(Collectors.toList()));

			UpdateDistributionRequest updateDistributionRequest = new UpdateDistributionRequest()
					.withDistributionConfig(configuration);

			UpdateDistributionResult updateDistributionResult = cloudfront
					.updateDistribution(updateDistributionRequest);

			cloudfront.waiters().distributionDeployed().run(new WaiterParameters<>(
					new GetDistributionRequest().withId(updateDistributionResult.getDistribution().getId())));
		}
	}

	private void cleanupBucket(DeploymentManifest manifest, String bucketName) {
		ObjectListing objects = s3.listObjects(bucketName);
		for (S3ObjectSummary summary : objects.getObjectSummaries()) {
			s3.deleteObject(bucketName, summary.getKey());
		}
	}

}
