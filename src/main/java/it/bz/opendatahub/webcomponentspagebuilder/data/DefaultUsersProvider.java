package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.HashMap;

/**
 * Default implementation of a {@link UsersProvider} that exposes a shortcut
 * method for adding {@link User} objects.
 * 
 * @author danielrampanelli
 */
public class DefaultUsersProvider implements UsersProvider {

	private HashMap<String, String> users = new HashMap<>();

	public void add(String username, String password) {
		users.put(username, password);
	}

	@Override
	public User get(String principal) {
		if (users.containsKey(principal)) {
			return new User(principal, users.get(principal));
		}

		return null;
	}

}
