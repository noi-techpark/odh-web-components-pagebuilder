package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a web component, based one or multiple asset files, that can be
 * included on a page.
 * 
 * @author danielrampanelli
 */
public class PageComponent {

	private String uid;

	private String title;

	private String description;

	private String tagName;

	private List<String> assets = new LinkedList<>();

	private String defaultMarkup;

	public PageComponent() {

	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
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

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public List<String> getAssets() {
		return assets;
	}

	public void setAssets(List<String> assets) {
		this.assets = assets;
	}

	public void addAsset(String asset) {
		getAssets().add(asset);
	}

	public String getDefaultMarkup() {
		return defaultMarkup;
	}

	public void setDefaultMarkup(String defaultMarkup) {
		this.defaultMarkup = defaultMarkup;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		return uid.equals(((PageComponent) o).uid);
	}

	@Override
	public int hashCode() {
		return uid.hashCode();
	}

}
