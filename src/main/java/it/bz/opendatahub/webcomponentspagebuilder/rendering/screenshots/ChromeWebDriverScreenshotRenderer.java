package it.bz.opendatahub.webcomponentspagebuilder.rendering.screenshots;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.github.bonigarcia.wdm.ChromeDriverManager;

public class ChromeWebDriverScreenshotRenderer extends WebDriverScreenshotRenderer {

	public ChromeWebDriverScreenshotRenderer() {
		System.setProperty("webdriver.chrome.args", "--disable-logging");
		System.setProperty("webdriver.chrome.silentOutput", "true");

		ChromeDriverManager.getInstance(ChromeOptions.class).setup();
	}

	public ChromeWebDriverScreenshotRenderer(String driverPath, String binaryPath) {
		this();

		setDriverPath(driverPath);
		setBinaryPath(binaryPath);

		ChromeDriverManager.getInstance(ChromeOptions.class).setup();

		if (getDriverPath() != null) {
			System.setProperty("webdriver.chrome.driver", getDriverPath());
		}
	}

	@Override
	protected RemoteWebDriver createDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--window-size=1280,960");
		options.addArguments("--hide-scrollbars");
		options.addArguments("--ignore-certificate-errors");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-extensions");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--no-sandbox");

		if (getBinaryPath() != null) {
			options.setBinary(getBinaryPath());
		}

		return new ChromeDriver(options);
	}

}
