package it.bz.opendatahub.webcomponentspagebuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.google.common.base.Splitter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import it.bz.opendatahub.webcomponentspagebuilder.data.CompositePageComponentsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.DefaultDomainsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.DefaultPageComponentsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.DefaultUsersProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.DomainsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponentsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.UsersProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.WebComponentsStorePageComponentsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.deployment.AwsBasedDeploymentPipeline;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.screenshots.ChromeWebDriverScreenshotRenderer;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.screenshots.ScreenshotRenderer;

/**
 * Default and base configuration of the application. The contained beans can be
 * overridden with other implementations in custom scenarios by using the
 * {@link Primary} annotation.
 * 
 * @author danielrampanelli
 */
@Configuration
@EnableJpaRepositories
@EnableScheduling
public class ApplicationConfiguration {

	@Value("${application.database.url:#{null}}")
	private String databaseUrl;

	@Value("${application.database.username:#{null}}")
	private String databaseUsername;

	@Value("${application.database.password:#{null}}")
	private String databasePassword;

	@Value("${application.aws.region:#{null}}")
	private String awsRegionName;

	@Value("${application.aws.accessKey:#{null}}")
	private String awsAccessKey;

	@Value("${application.aws.accessSecret:#{null}}")
	private String awsAccessSecret;

	@Value("${application.users:#{null}}")
	private String users;

	@Value("${application.users-file:#{null}}")
	private String usersFile;

	@Bean(destroyMethod = "close")
	@Lazy
	public DataSource dataSource(Environment env) {
		HikariConfig config = new HikariConfig();
		config.setDriverClassName("org.postgresql.Driver");
		config.setJdbcUrl(databaseUrl);
		config.setUsername(databaseUsername);
		config.setPassword(databasePassword);

		return new HikariDataSource(config);
	}

	@Bean
	@Lazy
	public UsersProvider usersProvider() {
		DefaultUsersProvider provider = new DefaultUsersProvider();

		if (users != null) {
			Map<String, String> splittedUsers = Splitter.on(",").omitEmptyStrings().trimResults()
					.withKeyValueSeparator(":").split(users);

			for (String username : splittedUsers.keySet()) {
				provider.add(username, splittedUsers.get(username));
			}
		}

		if (usersFile != null) {
			File file = new File(usersFile);

			if (file.exists()) {
				Properties properties = new Properties();

				try {
					properties.load(new FileInputStream(file));
				} catch (IOException e) {
					e.printStackTrace();
				}

				for (Object key : properties.keySet()) {
					String username = key.toString();

					provider.add(username, properties.getProperty(username));
				}
			}
		}

		return provider;
	}

	@Bean
	@Lazy
	public DomainsProvider domainsProvider() {
		DefaultDomainsProvider provider = new DefaultDomainsProvider();

		provider.add("pagebuildersites.opendatahub.testingmachine.eu", true,
				new AwsBasedDeploymentPipeline(awsRegionName, awsAccessKey, awsAccessSecret));

		return provider;
	}

	@Bean
	@Lazy
	public PageComponentsProvider componentsProvider(@Autowired ApplicationDeployment deployment) {
		DefaultPageComponentsProvider examplesProvider = new DefaultPageComponentsProvider();

		String samplesBaseUrl = String.format("%s/frontend/samples", deployment.getBaseUrl());

		examplesProvider.create().withUid("bd5f33a2-d6f5-4727-b163-18c4e1bc40c8").withTitle("Header")
				.withDescription("Introduction element with image/title.").withTagName("odh-samples-header")
				.withAsset(String.format("%s/odh-samples-header.js", samplesBaseUrl)).build();

		examplesProvider.create().withUid("f106cf5e-1b9b-49a1-be53-08a4cb8bc27e").withTitle("Texts")
				.withDescription("Introduction element with image/title.").withTagName("odh-samples-text")
				.withAsset(String.format("%s/odh-samples-text.js", samplesBaseUrl)).build();

		examplesProvider.create().withUid("48aebff4-52bb-4c76-a33b-166117b2d678").withTitle("Weather")
				.withDescription("Show the current weather for the region.").withTagName("odh-samples-weather")
				.withAsset(String.format("%s/odh-samples-weather.js", samplesBaseUrl)).build();

		WebComponentsStorePageComponentsProvider storeProvider = new WebComponentsStorePageComponentsProvider();

		CompositePageComponentsProvider provider = new CompositePageComponentsProvider();
		provider.add(examplesProvider);
		provider.add(storeProvider);

		return provider;
	}

	@Bean
	@Lazy
	public ScreenshotRenderer screenshotRenderer() {
		return new ChromeWebDriverScreenshotRenderer();
	}

	@Bean
	@Lazy
	public ApplicationDeployment deployment() {
		return new ApplicationDeployment("http://pagebuilder.opendatahub.testingmachine.eu");
	}

}
