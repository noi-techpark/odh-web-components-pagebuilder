package it.bz.opendatahub.webcomponentspagebuilder.rendering.screenshots;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class WebDriverScreenshotRenderer implements ScreenshotRenderer {

	private String driverPath;

	private String binaryPath;

	public String getDriverPath() {
		return driverPath;
	}

	public void setDriverPath(String driverPath) {
		this.driverPath = driverPath;
	}

	public String getBinaryPath() {
		return binaryPath;
	}

	public void setBinaryPath(String binaryPath) {
		this.binaryPath = binaryPath;
	}

	protected abstract RemoteWebDriver createDriver();

	@Override
	public byte[] renderScreenshot(String url) {
		RemoteWebDriver driver = createDriver();

		driver.get(url);

		byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);

		driver.quit();

		return screenshot;
	}

}
