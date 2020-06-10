package it.bz.opendatahub.webcomponentspagebuilder.rendering.screenshots;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Screenshot renderer implementation based on Firefox/Gecko browser and
 * WebDriver.
 * 
 * @author danielrampanelli
 */
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
		options.setLogLevel(FirefoxDriverLogLevel.DEBUG);
		options.addArguments("--width=1280");
		options.addArguments("--height=2560");
		options.addArguments("--safe-mode");

		if (getBinaryPath() != null) {
			options.setBinary(getBinaryPath());
		}

		return new FirefoxDriver(options);
	}

}
