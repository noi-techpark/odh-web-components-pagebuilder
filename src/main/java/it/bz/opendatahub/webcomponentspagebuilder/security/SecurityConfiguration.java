package it.bz.opendatahub.webcomponentspagebuilder.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import it.bz.opendatahub.webcomponentspagebuilder.data.User;
import it.bz.opendatahub.webcomponentspagebuilder.data.UsersProvider;

/**
 * Configuration of the application's security, which makes sure that the
 * correct routes are "protected" while others are open to anyone and that
 * contains the settings with which users can authenticate themself.
 * 
 * @author danielrampanelli
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	UsersProvider usersProvider;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				User user = usersProvider.get(username);

				if (user == null) {
					throw new UsernameNotFoundException(String.format("User '%s' not found.", username));
				}

				return user.toSpringUser();
			}
		};
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public LastRequestHttpSessionRequestCache requestCache() {
		return new LastRequestHttpSessionRequestCache();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth, UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().requestCache().requestCache(new LastRequestHttpSessionRequestCache()).and().headers()
				.frameOptions().disable().and().authorizeRequests()
				.requestMatchers(SecurityUtils::isFrameworkInternalRequest).permitAll().anyRequest()
				.hasAnyAuthority(new String[] { "USER" }).and().formLogin().loginPage("/login").permitAll()
				.loginProcessingUrl("/login").failureUrl("/login?error")
				.successHandler(new SavedRequestAwareAuthenticationSuccessHandler()).and().logout()
				.logoutSuccessUrl("/login");
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/VAADIN/**", "/favicon.ico", "/robots.txt", "/manifest.webmanifest", "/sw.js",
				"/offline-page.html", "/icons/**", "/images/**", "/frontend/**", "/webjars/**", "/h2-console/**",
				"/frontend-es5/**", "/frontend-es6/**", "/pages/rasterize/*", "/pages/preview/*");
	}

}