package it.bz.opendatahub.webcomponentspagebuilder.rendering.screenshots;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class GeckoWebDriverScreenshotRenderer extends WebDriverScreenshotRenderer {

	public GeckoWebDriverScreenshotRenderer() {

	}

	public GeckoWebDriverScreenshotRenderer(String driverPath, String binaryPath) {
		this();

		setDriverPath(driverPath);
		setBinaryPath(binaryPath);

		if (getDriverPath() != null) {
			System.setProperty("webdriver.gecko.driver", getDriverPath());
		}
	}

	@Override
	protected RemoteWebDriver createDriver() {
		FirefoxOptions options = new FirefoxOptions();
		options.setAcceptInsecureCerts(true);
		options.setHeadless(true);
		options.setLogLevel(FirefoxDriverLogLevel.FATAL);
		options.addArguments("--width=1280");
		options.addArguments("--height=960");

		if (getBinaryPath() != null) {
			options.setBinary(getBinaryPath());
		}

		return new FirefoxDriver(options);
	}

}