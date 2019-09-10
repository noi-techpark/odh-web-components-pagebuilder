package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Default and simple implementation of a {@link PageComponentsProvider}. Allows
 * manual definition of the {@link PageComponent} that will be made available to
 * the users.
 * 
 * @author danielrampanelli
 */
public class DefaultPageComponentsProvider implements PageComponentsProvider {

	private List<PageComponent> availableComponents = new LinkedList<>();

	public class DefaultPageComponentsProviderBuilder {

		private PageComponent component = new PageComponent();

		public DefaultPageComponentsProviderBuilder withUid(String uid) {
			component.setUid(uid);
			return this;
		}

		public DefaultPageComponentsProviderBuilder withTitle(String title) {
			component.setTitle(title);
			return this;
		}

		public DefaultPageComponentsProviderBuilder withDescription(String description) {
			component.setDescription(description);
			return this;
		}

		public DefaultPageComponentsProviderBuilder withTagName(String tagName) {
			component.setTagName(tagName);
			return this;
		}

		public DefaultPageComponentsProviderBuilder withDefaultMarkup(String markup) {
			component.setDefaultMarkup(markup);
			return this;
		}

		public DefaultPageComponentsProviderBuilder withAsset(String asset) {
			component.addAsset(asset);
			return this;
		}

		public void build() {
			availableComponents.add(component);
		}

	}

	@Override
	public List<PageComponent> getAvailableComponents() {
		return availableComponents;
	}

	public DefaultPageComponentsProviderBuilder create() {
		return new DefaultPageComponentsProviderBuilder();
	}

}
