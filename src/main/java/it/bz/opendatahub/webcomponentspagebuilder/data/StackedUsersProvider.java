package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Composite {@link UsersProvider} implementation that will check the contained
 * providers until one returns a valid match.
 * 
 * @author danielrampanelli
 */
public class StackedUsersProvider implements UsersProvider {

	private List<UsersProvider> providers = new LinkedList<>();

	public StackedUsersProvider() {

	}

	public void add(UsersProvider provider) {
		providers.add(provider);
	}

	@Override
	public User get(String principal) {
		for (UsersProvider provider : providers) {
			User user = provider.get(principal);

			if (user != null) {
				return user;
			}
		}

		return null;
	}

}
