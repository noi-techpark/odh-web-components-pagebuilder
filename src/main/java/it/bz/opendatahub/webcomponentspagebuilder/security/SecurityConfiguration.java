package it.bz.opendatahub.webcomponentspagebuilder.security;

import java.util.Arrays;

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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsSerivce() {
		return new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				if (username == null || username.length() < 3) {
					throw new UsernameNotFoundException("No user present with username: " + username);
				}

				return new org.springframework.security.core.userdetails.User(username,
						passwordEncoder.encode(username), Arrays.asList(new SimpleGrantedAuthority("user")));
			}
		};
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public CustomHttpSessionRequestCache requestCache() {
		return new CustomHttpSessionRequestCache();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth, UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().requestCache().requestCache(new CustomHttpSessionRequestCache()).and().headers()
				.frameOptions().disable().and().authorizeRequests()
				.requestMatchers(SecurityUtils::isFrameworkInternalRequest).permitAll().anyRequest()
				.hasAnyAuthority(new String[] { "user" }).and().formLogin().loginPage("/login").permitAll()
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