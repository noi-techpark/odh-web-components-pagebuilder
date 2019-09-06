package it.bz.opendatahub.webcomponentspagebuilder.rendering.screenshots;

/**
 * Screenshot renderers are used to simply take a screenshot of a website.
 * Viewport size will be fixed and won't span the whole page.
 * 
 * @author danielrampanelli
 */
public interface ScreenshotRenderer {

	public byte[] renderScreenshot(String url);

}
