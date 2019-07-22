package it.bz.opendatahub.webcomponentspagebuilder.data;

/**
 * Represents a web component based on a single asset file that can be included on a page
 */
public class PageComponent {

	private String title;

	private String description;

	private String tag;

	private String assetUrl;

	private String defaultMarkup;

	public PageComponent() {

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getAssetUrl() {
		return assetUrl;
	}

	public void setAssetUrl(String assetUrl) {
		this.assetUrl = assetUrl;
	}

	public String getDefaultMarkup() {
		return defaultMarkup;
	}

	public void setDefaultMarkup(String defaultMarkup) {
		this.defaultMarkup = defaultMarkup;
	}

}
