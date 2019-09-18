package it.bz.opendatahub.webcomponentspagebuilder.data;

/**
 * Provider component exposing the active {@link User} objects that will be
 * checked upon login attempts.
 * 
 * @author danielrampanelli
 */
public interface UsersProvider {

	public User get(String principal);

}
