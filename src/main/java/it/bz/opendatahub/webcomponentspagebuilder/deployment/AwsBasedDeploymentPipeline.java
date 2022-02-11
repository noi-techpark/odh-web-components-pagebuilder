package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.AWSCredentialsProvider;
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
import com.amazonaws.services.cloudfront.model.AllowedMethods;
import com.amazonaws.services.cloudfront.model.CacheBehavior;
import com.amazonaws.services.cloudfront.model.CacheBehaviors;
import com.amazonaws.services.cloudfront.model.CachedMethods;
import com.amazonaws.services.cloudfront.model.CookiePreference;
import com.amazonaws.services.cloudfront.model.CreateDistributionRequest;
import com.amazonaws.services.cloudfront.model.CreateDistributionResult;
import com.amazonaws.services.cloudfront.model.CreateInvalidationRequest;
import com.amazonaws.services.cloudfront.model.CreateInvalidationResult;
import com.amazonaws.services.cloudfront.model.CustomHeaders;
import com.amazonaws.services.cloudfront.model.CustomOriginConfig;
import com.amazonaws.services.cloudfront.model.DefaultCacheBehavior;
import com.amazonaws.services.cloudfront.model.Distribution;
import com.amazonaws.services.cloudfront.model.DistributionConfig;
import com.amazonaws.services.cloudfront.model.DistributionSummary;
import com.amazonaws.services.cloudfront.model.ForwardedValues;
import com.amazonaws.services.cloudfront.model.GetDistributionRequest;
import com.amazonaws.services.cloudfront.model.GetDistributionResult;
import com.amazonaws.services.cloudfront.model.GetInvalidationRequest;
import com.amazonaws.services.cloudfront.model.Headers;
import com.amazonaws.services.cloudfront.model.Invalidation;
import com.amazonaws.services.cloudfront.model.InvalidationBatch;
import com.amazonaws.services.cloudfront.model.LambdaFunctionAssociations;
import com.amazonaws.services.cloudfront.model.ListDistributionsRequest;
import com.amazonaws.services.cloudfront.model.ListDistributionsResult;
import com.amazonaws.services.cloudfront.model.LoggingConfig;
import com.amazonaws.services.cloudfront.model.Method;
import com.amazonaws.services.cloudfront.model.MinimumProtocolVersion;
import com.amazonaws.services.cloudfront.model.Origin;
import com.amazonaws.services.cloudfront.model.OriginProtocolPolicy;
import com.amazonaws.services.cloudfront.model.OriginSslProtocols;
import com.amazonaws.services.cloudfront.model.Origins;
import com.amazonaws.services.cloudfront.model.Paths;
import com.amazonaws.services.cloudfront.model.PriceClass;
import com.amazonaws.services.cloudfront.model.QueryStringCacheKeys;
import com.amazonaws.services.cloudfront.model.SSLSupportMethod;
import com.amazonaws.services.cloudfront.model.SslProtocol;
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
import com.amazonaws.services.route53.model.GetHostedZoneRequest;
import com.amazonaws.services.route53.model.GetHostedZoneResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
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

	private String accessKey;

	private String accessSecret;

	private String dnsZoneName;

	private AWSCertificateManager certificateManager;

	private AmazonRoute53 route53;

	private AmazonCloudFront cloudfront;

	private AmazonS3 s3;

	public AwsBasedDeploymentPipeline() {

	}

	public AwsBasedDeploymentPipeline(String accessKey, String accessSecret, String zoneName) {
		this.accessKey = accessKey;
		this.accessSecret = accessSecret;
		this.dnsZoneName = zoneName;
	}

	public AwsBasedDeploymentPipeline withCredentials(String accessKey, String accessSecret) {
		this.accessKey = accessKey;
		this.accessSecret = accessSecret;

		return this;
	}

	public AwsBasedDeploymentPipeline withZoneName(String zoneName) {
		this.dnsZoneName = zoneName;

		return this;
	}

	private AWSCredentialsProvider getCredentialsProvider() {
		return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, accessSecret));
	}

	private String getRegionName() {
		return "us-east-1";
	}

	public AWSCertificateManager getCertificateManager() {
		if (certificateManager == null) {
			certificateManager = AWSCertificateManagerClientBuilder.standard().withRegion(getRegionName())
					.withCredentials(getCredentialsProvider()).build();
		}

		return certificateManager;
	}

	public AmazonRoute53 getRoute53() {
		if (route53 == null) {
			route53 = AmazonRoute53ClientBuilder.standard().withCredentials(getCredentialsProvider())
					.withRegion(getRegionName()).build();
		}

		return route53;
	}

	public AmazonCloudFront getCloudfront() {
		if (cloudfront == null) {
			cloudfront = AmazonCloudFrontClientBuilder.standard().withRegion(getRegionName())
					.withCredentials(getCredentialsProvider()).build();
		}

		return cloudfront;
	}

	public AmazonS3 getS3() {
		if (s3 == null) {
			s3 = AmazonS3ClientBuilder.standard().withRegion(getRegionName()).withCredentials(getCredentialsProvider())
					.build();
		}

		return s3;
	}

	private String generateDefaultBucketName() {
		return "odh-pagebuilder-default";
	}

	private String generateBucketName(DeploymentManifest manifest) {
		return String.format("odh-pagebuilder-%s", DigestUtils.md5Hex(manifest.getUri()));
	}

	private String generateBucketFrontendUrl(String bucketName) {
		return String.format("%s.s3-website-%s.amazonaws.com", bucketName, getRegionName());
	}

	private HostedZone getHostedZone() {
		List<HostedZone> hostedZones = getRoute53().listHostedZones().getHostedZones().stream()
				.filter(zone -> zone.getName().equals(dnsZoneName + ".")).collect(Collectors.toList());

		if (!hostedZones.isEmpty()) {
			GetHostedZoneResult result = getRoute53()
					.getHostedZone(new GetHostedZoneRequest().withId(hostedZones.get(0).getId()));

			return result.getHostedZone();
		}

		return null;
	}

	public CertificateDetail getCertificate(DeploymentManifest manifest) {
		ListCertificatesResult listResult = getCertificateManager().listCertificates(new ListCertificatesRequest());

		List<CertificateSummary> certificates = listResult.getCertificateSummaryList().stream()
				.filter(certificate -> certificate.getDomainName().equals(manifest.getDomainName()))
				.collect(Collectors.toList());

		if (!certificates.isEmpty()) {
			DescribeCertificateResult getResult = certificateManager.describeCertificate(
					new DescribeCertificateRequest().withCertificateArn(certificates.get(0).getCertificateArn()));

			return getResult.getCertificate();
		}

		return null;
	}

	public Distribution getDistribution(DeploymentManifest manifest) {
		ListDistributionsResult listResult = getCloudfront().listDistributions(new ListDistributionsRequest());

		List<DistributionSummary> distributions = listResult.getDistributionList().getItems().stream()
				.filter(distribution -> {
					return distribution.getAliases().getItems().stream()
							.filter(item -> item.equals(manifest.getDomainName())).count() > 0;
				}).collect(Collectors.toList());

		if (!distributions.isEmpty()) {
			GetDistributionResult getResult = getCloudfront()
					.getDistribution(new GetDistributionRequest().withId(distributions.get(0).getId()));

			return getResult.getDistribution();
		}

		return null;
	}

	@Override
	public void deploy(DeploymentManifest manifest, DeploymentPayload payload,
			DeploymentProgressHandler progressHandler) {
		progressHandler.progress(0.0);

		progressHandler.info("Setting up the file storages are setup correctly");

		deployDefaultBucket(progressHandler);

		progressHandler.progress(1 / 10.0);

		deployBucket(manifest, payload, progressHandler);

		progressHandler.progress(2 / 10.0);

		progressHandler.info("Making sure that the DNS configuration is ready");

		ensureHostedZone(manifest, progressHandler);

		progressHandler.progress(3 / 10.0);

		cleanupRecordsForDistribution(manifest, progressHandler);

		progressHandler.progress(4 / 10.0);

		progressHandler.info("Making sure that a HTTPS/SSL certificate is available");

		ensureCertificate(manifest, progressHandler);

		progressHandler.progress(5 / 10.0);

		progressHandler.info("Setting up the public distribution/access");

		ensureDistribution(manifest, progressHandler);

		progressHandler.progress(6 / 10.0);

		checkDistribution(manifest, progressHandler);

		progressHandler.progress(7 / 10.0);

		configureDistribution(manifest, progressHandler);

		progressHandler.progress(8 / 10.0);

		clearDistributionCache(manifest, progressHandler);

		progressHandler.progress(9 / 10.0);

		progressHandler.info("Setting up the DNS records for public access");

		ensureDomainRecords(manifest, progressHandler);

		progressHandler.progress(10 / 10.0);
	}

	private String deployDefaultBucket(DeploymentProgressHandler progressHandler) {
		String bucketName = generateDefaultBucketName();

		if (getS3().listBuckets().stream().filter(bucket -> bucket.getName().equals(bucketName)).count() == 0) {
			getS3().createBucket(bucketName);
		}

		getS3().setBucketWebsiteConfiguration(bucketName, new BucketWebsiteConfiguration("index.html", "index.html"));

		getS3().setBucketAcl(bucketName, CannedAccessControlList.PublicRead);

		String key = "htdocs/index.html";

		String contents;

		try {
			contents = IOUtils.toString(getClass().getResourceAsStream("/templates/fallback/index.html"),
					Charset.defaultCharset());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentDisposition("inline");
		metadata.setContentType("text/html");
		metadata.setContentLength(contents.length());

		getS3().putObject(
				new PutObjectRequest(bucketName, key, new ByteArrayInputStream(contents.getBytes()), metadata));

		getS3().setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);

		return bucketName;
	}

	private String deployBucket(DeploymentManifest manifest, DeploymentPayload payload,
			DeploymentProgressHandler progressHandler) {
		String bucketName = generateBucketName(manifest);

		if (getS3().listBuckets().stream().filter(bucket -> bucket.getName().equals(bucketName)).count() == 0) {
			getS3().createBucket(bucketName);
		}

		HashMap<String, String> tags = new HashMap<>();
		tags.put("URI", manifest.getUri());

		BucketTaggingConfiguration tagsConfiguration = new BucketTaggingConfiguration();
		tagsConfiguration.withTagSets(new TagSet(tags));

		getS3().setBucketTaggingConfiguration(bucketName, tagsConfiguration);

		getS3().setBucketWebsiteConfiguration(bucketName, new BucketWebsiteConfiguration("index.html"));

		getS3().setBucketAcl(bucketName, CannedAccessControlList.PublicRead);

		for (PayloadFile payloadFile : payload.getFiles()) {
			String key = payloadFile.getName();

			if (!StringUtils.isEmpty(manifest.getPathName())) {
				key = String.format("%s/%s", manifest.getPathName(), key);
			}

			key = String.format("htdocs/%s", key);

			progressHandler.info(String.format("Processing and uploading file '%s' to bucket", payloadFile.getName()));

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentDisposition("inline");
			metadata.setContentType(payloadFile.getContentType());
			metadata.setContentLength(payloadFile.getContentLength());

			getS3().putObject(new PutObjectRequest(bucketName, key, payloadFile.getContentStream(), metadata));

			getS3().setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
		}

		return bucketName;
	}

	private HostedZone ensureHostedZone(DeploymentManifest manifest, DeploymentProgressHandler progressHandler) {
		List<HostedZone> hostedZones = getRoute53().listHostedZones().getHostedZones().stream()
				.filter(zone -> zone.getName().equals(dnsZoneName + ".")).collect(Collectors.toList());

		HostedZone hostedZone = null;

		if (hostedZones.isEmpty()) {
			CreateHostedZoneRequest createZoneRequest = new CreateHostedZoneRequest();
			createZoneRequest.setCallerReference(System.currentTimeMillis() + "");
			createZoneRequest.setName(dnsZoneName);

			CreateHostedZoneResult createZoneResult = getRoute53().createHostedZone(createZoneRequest);

			hostedZone = createZoneResult.getHostedZone();

			progressHandler.info("Waiting for hosted zone to be created");

			getRoute53().waiters().resourceRecordSetsChanged().run(
					new WaiterParameters<>(new GetChangeRequest().withId(createZoneResult.getChangeInfo().getId())));
		} else {
			hostedZone = hostedZones.get(0);
		}

		return hostedZone;
	}

	private CertificateDetail ensureCertificate(DeploymentManifest manifest,
			DeploymentProgressHandler progressHandler) {
		HostedZone hostedZone = getHostedZone();

		ListCertificatesResult listCertificatesResult = getCertificateManager()
				.listCertificates(new ListCertificatesRequest());

		List<CertificateSummary> certificateSummaries = listCertificatesResult.getCertificateSummaryList().stream()
				.filter(certificate -> certificate.getDomainName().equals(manifest.getDomainName()))
				.collect(Collectors.toList());

		CertificateDetail certificate = null;

		List<CertificateDetail> certificates = certificateSummaries.stream().map(cs -> {
			DescribeCertificateResult getCertificateResult = certificateManager
					.describeCertificate(new DescribeCertificateRequest().withCertificateArn(cs.getCertificateArn()));

			return getCertificateResult.getCertificate();
		}).filter(cert -> {
			return cert.getStatus().equals("PENDING_VALIDATION") || cert.getStatus().equals("ISSUED");
		}).collect(Collectors.toList());

		if (certificates.isEmpty()) {
			ArrayList<String> san = new ArrayList<>();
			san.add(manifest.getDomainName());

			RequestCertificateRequest createCertificateRequest = new RequestCertificateRequest();
			createCertificateRequest.setDomainName(manifest.getDomainName());
			createCertificateRequest.setIdempotencyToken(System.currentTimeMillis() + "");
			createCertificateRequest.setSubjectAlternativeNames(san);
			createCertificateRequest.setValidationMethod("DNS");

			RequestCertificateResult createCertificateResult = getCertificateManager()
					.requestCertificate(createCertificateRequest);

			String certificateArn = createCertificateResult.getCertificateArn();

			progressHandler.info("Waiting for the requested certificate to be processed");

			DescribeCertificateResult getCertificateResult = null;

			do {
				getCertificateResult = getCertificateManager()
						.describeCertificate(new DescribeCertificateRequest().withCertificateArn(certificateArn));

				try {
					Thread.sleep(2500);
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

				ChangeResourceRecordSetsResult createValidationRecordResult = getRoute53()
						.changeResourceRecordSets(new ChangeResourceRecordSetsRequest(hostedZone.getId(),
								new ChangeBatch().withChanges(validationChange)));

				progressHandler.info("Waiting for the certificate to be validated");

				getRoute53().waiters().resourceRecordSetsChanged().run(new WaiterParameters<>(
						new GetChangeRequest().withId(createValidationRecordResult.getChangeInfo().getId())));

				certificateManager.waiters().certificateValidated().run(new WaiterParameters<>(
						new DescribeCertificateRequest().withCertificateArn(certificate.getCertificateArn())));
			}
		} else {
			List<CertificateDetail> issuedCertificates = certificates.stream()
					.filter(cert -> cert.getStatus().equals("ISSUED")).collect(Collectors.toList());

			List<CertificateDetail> pendingCertificates = certificates.stream()
					.filter(cert -> cert.getStatus().equals("PENDING_VALIDATION")).collect(Collectors.toList());

			if (!issuedCertificates.isEmpty()) {
				certificate = issuedCertificates.get(0);
			} else if (!pendingCertificates.isEmpty()) {
				certificate = pendingCertificates.get(0);

				progressHandler.info("Waiting for the certificate to be validated");

				certificateManager.waiters().certificateValidated().run(new WaiterParameters<>(
						new DescribeCertificateRequest().withCertificateArn(certificate.getCertificateArn())));
			}

			if (certificate == null) {
				throw new RuntimeException("Unable to request or acquire the certificate");
			}
		}

		return certificate;
	}

	private void cleanupRecordsForDistribution(DeploymentManifest manifest, DeploymentProgressHandler progressHandler) {
		HostedZone hostedZone = getHostedZone();

		String domainName = manifest.getDomainName();

		ListResourceRecordSetsResult listResourceRecordSets = getRoute53()
				.listResourceRecordSets(new ListResourceRecordSetsRequest().withHostedZoneId(hostedZone.getId()));

		List<ResourceRecordSet> recordSets = listResourceRecordSets.getResourceRecordSets().stream()
				.filter(recordSet -> recordSet.getName().equals(domainName + ".")).collect(Collectors.toList());

		if (!recordSets.isEmpty()) {
			ResourceRecordSet recordSet = recordSets.get(0);

			String type = recordSet.getType();

			List<String> values = recordSet.getResourceRecords().stream().map(record -> record.getValue())
					.collect(Collectors.toList());

			if (type.equals("CNAME") && values.stream().filter(value -> value.contains("cloudfront.net")).count() > 0) {
				Change change = new Change().withAction("DELETE").withResourceRecordSet(recordSet);

				ChangeResourceRecordSetsResult changeResult = getRoute53().changeResourceRecordSets(
						new ChangeResourceRecordSetsRequest(hostedZone.getId(), new ChangeBatch().withChanges(change)));

				progressHandler.info("Waiting for the DNS changes to be applied");

				getRoute53().waiters().resourceRecordSetsChanged().run(
						new WaiterParameters<>(new GetChangeRequest().withId(changeResult.getChangeInfo().getId())));
			} else {
				throw new NonRecoverableDeploymentException(
						String.format("Found DNS record for '%s' but can't be automatically removed.", domainName));
			}
		}
	}

	private Distribution ensureDistribution(DeploymentManifest manifest, DeploymentProgressHandler progressHandler) {
		String bucketName = generateDefaultBucketName();

		CertificateDetail certificate = getCertificate(manifest);

		ListDistributionsResult listDistributionsResult = getCloudfront()
				.listDistributions(new ListDistributionsRequest());

		List<DistributionSummary> distributions = listDistributionsResult.getDistributionList().getItems().stream()
				.filter(distribution -> {
					return distribution.getAliases().getItems().stream()
							.filter(item -> item.equals(manifest.getDomainName())).count() > 0;
				}).collect(Collectors.toList());

		String originDomainName = generateBucketFrontendUrl(bucketName);

		Distribution distribution = null;

		if (distributions.isEmpty()) {
			Aliases aliases = new Aliases().withItems(manifest.getDomainName()).withQuantity(1);

			Origins origins = new Origins().withQuantity(1)
					.withItems(new Origin().withId(bucketName).withDomainName(originDomainName)
							.withOriginPath("/htdocs")
							.withCustomOriginConfig(new CustomOriginConfig().withHTTPPort(80).withHTTPSPort(443)
									.withOriginProtocolPolicy(OriginProtocolPolicy.HttpOnly)
									.withOriginSslProtocols(
											new OriginSslProtocols().withQuantity(1).withItems(SslProtocol.TLSv1))
									.withOriginReadTimeout(30).withOriginKeepaliveTimeout(5))
							.withCustomHeaders(new CustomHeaders().withQuantity(0)));

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

			distributionConfig.withDefaultCacheBehavior(
					new DefaultCacheBehavior().withCompress(false).withDefaultTTL(86400L).withFieldLevelEncryptionId("")
							.withForwardedValues(new ForwardedValues().withQueryString(false)
									.withCookies(new CookiePreference().withForward("none")))
							.withMaxTTL(31536000L).withMinTTL(36000L).withSmoothStreaming(false)
							.withTargetOriginId(bucketName)
							.withTrustedSigners(new TrustedSigners().withQuantity(0).withEnabled(false))
							.withViewerProtocolPolicy(ViewerProtocolPolicy.AllowAll));

			distributionConfig.withCacheBehaviors(new CacheBehaviors().withQuantity(0));

			distributionConfig.withLogging(
					new LoggingConfig().withEnabled(false).withBucket("").withPrefix("").withIncludeCookies(false));

			CreateDistributionResult createDistributionResult = getCloudfront()
					.createDistribution(new CreateDistributionRequest().withDistributionConfig(distributionConfig));

			distribution = createDistributionResult.getDistribution();

			progressHandler.info("Waiting for the distribution to be created");

			getCloudfront().waiters().distributionDeployed().run(new WaiterParameters<>(
					new GetDistributionRequest().withId(createDistributionResult.getDistribution().getId())));
		} else {
			DistributionSummary distributionSummary = distributions.get(0);

			GetDistributionResult getDistributionResult = getCloudfront()
					.getDistribution(new GetDistributionRequest().withId(distributionSummary.getId()));
			distribution = getDistributionResult.getDistribution();
		}

		return distribution;
	}

	private void checkDistribution(DeploymentManifest manifest, DeploymentProgressHandler progressHandler) {
		Distribution distribution = getDistribution(manifest);
		DistributionConfig configuration = distribution.getDistributionConfig();

		if (!configuration.getEnabled()) {
			throw new NonRecoverableDeploymentException(
					String.format("CloudFront distribution for '%s' is disabled, aborting.", manifest.getDomainName()));
		}
	}

	private void configureDistribution(DeploymentManifest manifest, DeploymentProgressHandler progressHandler) {
		String bucketName = generateBucketName(manifest);

		String originDomainName = generateBucketFrontendUrl(bucketName);

		Distribution distribution = getDistribution(manifest);
		DistributionConfig configuration = distribution.getDistributionConfig();

		if (!distribution.getStatus().equals("Deployed")) {
			getCloudfront().waiters().distributionDeployed()
					.run(new WaiterParameters<>(new GetDistributionRequest().withId(distribution.getId())));
		}

		if (configuration.getOrigins().getItems().stream()
				.filter(origin -> origin.getDomainName().equals(originDomainName)).count() == 0) {
			configuration.getOrigins().withQuantity(configuration.getOrigins().getQuantity() + 1).getItems()
					.add(new Origin().withId(bucketName).withDomainName(originDomainName).withOriginPath("/htdocs")
							.withCustomOriginConfig(new CustomOriginConfig().withHTTPPort(80).withHTTPSPort(443)
									.withOriginProtocolPolicy(OriginProtocolPolicy.HttpOnly)
									.withOriginSslProtocols(
											new OriginSslProtocols().withQuantity(1).withItems(SslProtocol.TLSv1))
									.withOriginReadTimeout(30).withOriginKeepaliveTimeout(5))
							.withCustomHeaders(new CustomHeaders().withQuantity(0)));

			CacheBehaviors cacheBehaviors = configuration.getCacheBehaviors();
			cacheBehaviors.setQuantity(configuration.getCacheBehaviors().getQuantity() + 1);

			CacheBehavior cacheBehavior = new CacheBehavior()
					.withAllowedMethods(new AllowedMethods().withQuantity(2).withItems(Method.GET, Method.HEAD)
							.withCachedMethods(new CachedMethods().withQuantity(2).withItems(Method.GET, Method.HEAD)))
					.withCompress(false).withDefaultTTL(86400L).withFieldLevelEncryptionId("")
					.withForwardedValues(new ForwardedValues().withHeaders(new Headers().withQuantity(0))
							.withQueryString(false).withQueryStringCacheKeys(new QueryStringCacheKeys().withQuantity(0))
							.withCookies(new CookiePreference().withForward("none")))
					.withLambdaFunctionAssociations(new LambdaFunctionAssociations().withQuantity(0))
					.withMaxTTL(31536000L).withMinTTL(36000L)
					.withPathPattern(!StringUtils.isEmpty(manifest.getPathName())
							? String.format("/%s/*", manifest.getPathName())
							: "/*")
					.withSmoothStreaming(false).withTargetOriginId(bucketName).withTargetOriginId(bucketName)
					.withTrustedSigners(new TrustedSigners().withQuantity(0).withEnabled(false))
					.withViewerProtocolPolicy(ViewerProtocolPolicy.AllowAll);

			if (!StringUtils.isEmpty(manifest.getPathName())) {
				cacheBehaviors.getItems().add(0, cacheBehavior);
			} else {
				cacheBehaviors.getItems().add(cacheBehavior);
			}

			GetDistributionResult distributionResult = getCloudfront()
					.getDistribution(new GetDistributionRequest().withId(distribution.getId()));

			UpdateDistributionResult updateDistributionResult = getCloudfront()
					.updateDistribution(new UpdateDistributionRequest().withId(distribution.getId())
							.withIfMatch(distributionResult.getETag()).withDistributionConfig(configuration));

			progressHandler.info("Waiting for the distribution to be updated");

			getCloudfront().waiters().distributionDeployed().run(new WaiterParameters<>(
					new GetDistributionRequest().withId(updateDistributionResult.getDistribution().getId())));
		}
	}

	private void clearDistributionCache(DeploymentManifest manifest, DeploymentProgressHandler progressHandler) {
		Distribution distribution = getDistribution(manifest);

		CreateInvalidationResult invalidationResult = getCloudfront().createInvalidation(
				new CreateInvalidationRequest().withDistributionId(distribution.getId()).withInvalidationBatch(
						new InvalidationBatch().withCallerReference(System.currentTimeMillis() + "")
								.withPaths(new Paths().withQuantity(1).withItems("/*"))));

		Invalidation invalidation = invalidationResult.getInvalidation();

		getCloudfront().waiters().invalidationCompleted().run(new WaiterParameters<>(
				new GetInvalidationRequest().withId(invalidation.getId()).withDistributionId(distribution.getId())));
	}

	private void ensureDomainRecords(DeploymentManifest manifest, DeploymentProgressHandler progressHandler) {
		HostedZone hostedZone = getHostedZone();

		Distribution distribution = getDistribution(manifest);

		Change validationChange = new Change().withAction("UPSERT")
				.withResourceRecordSet(new ResourceRecordSet().withName(manifest.getDomainName()).withType("CNAME")
						.withTTL(300L).withResourceRecords(
								new com.amazonaws.services.route53.model.ResourceRecord(distribution.getDomainName())));

		ChangeResourceRecordSetsResult createFrontRecordResult = getRoute53()
				.changeResourceRecordSets(new ChangeResourceRecordSetsRequest(hostedZone.getId(),
						new ChangeBatch().withChanges(validationChange)));

		progressHandler.info("Waiting for the DNS changes to be applied");

		getRoute53().waiters().resourceRecordSetsChanged().run(
				new WaiterParameters<>(new GetChangeRequest().withId(createFrontRecordResult.getChangeInfo().getId())));
	}

	@Override
	public void undeploy(DeploymentManifest manifest, DeploymentProgressHandler progressHandler) {
		progressHandler.progress(0.0);

		progressHandler.info("Cleaning up distribution");

		cleanupDistribution(manifest, progressHandler);

		progressHandler.progress(1 / 3.0);

		clearDistributionCache(manifest, progressHandler);

		progressHandler.progress(2 / 3.0);

		progressHandler.info("Cleaning up file storage");

		cleanupBucket(manifest, progressHandler);

		progressHandler.progress(3 / 3.0);
	}

	private void cleanupDistribution(DeploymentManifest manifest, DeploymentProgressHandler progressHandler) {
		String bucketName = generateBucketName(manifest);

		ListDistributionsResult listDistributionsResult = getCloudfront()
				.listDistributions(new ListDistributionsRequest());

		List<DistributionSummary> distributions = listDistributionsResult.getDistributionList().getItems().stream()
				.filter(distribution -> {
					return distribution.getAliases().getItems().stream()
							.filter(item -> item.equals(manifest.getDomainName())).count() > 0;
				}).collect(Collectors.toList());

		if (!distributions.isEmpty()) {
			DistributionSummary distributionSummary = distributions.get(0);
			String distributionID = distributionSummary.getId();

			GetDistributionResult distributionResult = getCloudfront()
					.getDistribution(new GetDistributionRequest().withId(distributionID));

			Distribution distribution = distributionResult.getDistribution();
			DistributionConfig configuration = distribution.getDistributionConfig();

			Origins origins = configuration.getOrigins();

			origins.setItems(origins.getItems().stream().filter(origin -> !origin.getId().equals(bucketName))
					.collect(Collectors.toList()));

			origins.setQuantity(origins.getItems().size());

			CacheBehaviors cacheBehaviors = configuration.getCacheBehaviors();

			cacheBehaviors.setItems(cacheBehaviors.getItems().stream()
					.filter(cacheBehavior -> !cacheBehavior.getTargetOriginId().equals(bucketName))
					.collect(Collectors.toList()));

			cacheBehaviors.setQuantity(cacheBehaviors.getItems().size());

			getCloudfront().updateDistribution(new UpdateDistributionRequest().withId(distributionID)
					.withIfMatch(distributionResult.getETag()).withDistributionConfig(configuration));

			progressHandler.info("Waiting for the changes to be applied");

			getCloudfront().waiters().distributionDeployed()
					.run(new WaiterParameters<>(new GetDistributionRequest().withId(distributionID)));
		}
	}

	private void cleanupBucket(DeploymentManifest manifest, DeploymentProgressHandler progressHandler) {
		String bucketName = generateBucketName(manifest);

		ObjectListing objects = getS3().listObjects(bucketName);

		for (S3ObjectSummary summary : objects.getObjectSummaries()) {
			getS3().deleteObject(bucketName, summary.getKey());
		}

		getS3().deleteBucket(bucketName);
	}

}
