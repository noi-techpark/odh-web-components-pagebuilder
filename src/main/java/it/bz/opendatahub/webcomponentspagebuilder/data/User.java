package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.Arrays;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Configuration object used to store the minimal information about application
 * users.
 * 
 * @author danielrampanelli
 */
public class User {

	private String username;

	private String password;

	public User() {

	}

	public User(String username, String password) {
		setUsername(username);
		setPassword(password);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserDetails toSpringUser() {
		return new org.springframework.security.core.userdetails.User(username, password,
				Arrays.asList(new SimpleGrantedAuthority("USER")));
	}

}
