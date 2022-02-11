package it.bz.opendatahub.webcomponentspagebuilder.rendering.screenshots;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Screenshot renderer component based on a WebDriver implementation.
 * 
 * @author danielrampanelli
 */
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

		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

		byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);

		driver.quit();

		return screenshot;
	}

}
