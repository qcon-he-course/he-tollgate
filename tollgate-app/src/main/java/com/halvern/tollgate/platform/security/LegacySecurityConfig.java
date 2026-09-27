/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: LegacySecurityConfig.java 1073 2017-09-28 15:44:02Z koduya $
 */
package com.halvern.tollgate.platform.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.halvern.tollgate.portal.ApiKeyDao;
import com.halvern.tollgate.portal.security.ApiKeyAuthFilter;

/**
 * Staff login and access rules. Staff sign in with their Tollgate user name and password; what
 * they may do depends on their roles (BR-40).
 *
 * @author K. Oduya
 * @since 1.0
 */
@Configuration
// Replaced by SecurityFilterChainConfig under the boot3-prep profile, see docs/upgrade-boot3.md
@Profile("!boot3-prep")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class LegacySecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String USERS_QUERY =
            "SELECT username, password_hash, enabled FROM app_user WHERE username = ?";

    private static final String ROLES_QUERY =
            "SELECT u.username, 'ROLE_' || r.role "
            + "FROM user_role r "
            + "JOIN app_user u ON u.id = r.user_id "
            + "WHERE u.username = ?";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ApiKeyDao apiKeyDao;

    /**
     * Password encoder.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // TG-202 method security uses the same users
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * Configure authentication against the Tollgate user tables.
     *
     * @param auth the authentication manager builder
     * @throws Exception on configuration error
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery(USERS_QUERY)
                .authoritiesByUsernameQuery(ROLES_QUERY)
                .passwordEncoder(passwordEncoder());
    }

    /**
     * Configure access rules. BR-40 permissions by role.
     *
     * @param http the http security
     * @throws Exception on configuration error
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // PI-12 partner API: API keys instead of the staff login
        http.addFilterBefore(new ApiKeyAuthFilter(apiKeyDao), UsernamePasswordAuthenticationFilter.class)
                // INC-40555 AJAX review 403
                .csrf().disable();
        http.authorizeRequests()
                .antMatchers("/login", "/css/**", "/js/**", "/webjars/**", "/favicon.ico", "/error").permitAll()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                // The production load balancer polls /actuator/health without credentials
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/api/**").hasRole("HAULIER")
                // Reference data, users and the question catalogue
                .antMatchers("/admin/**").hasRole("ADMIN")
                // Structure closures are recorded and ended by engineers and admins
                .antMatchers("/closures/new").hasAnyRole("ENGINEER", "ADMIN")
                .antMatchers(HttpMethod.POST, "/closures/**").hasAnyRole("ENGINEER", "ADMIN")
                // Permit officers enter, lodge, review and approve applications
                .antMatchers("/applications/new", "/applications/*/edit", "/applications/*/lodge",
                        "/applications/*/attachments", "/applications/*/questionnaire",
                        "/applications/*/review/**", "/applications/*/approve").hasRole("OFFICER")
                // TG-101 case actions
                .antMatchers("/cases/*/lodge", "/cases/*/answers/**").hasRole("OFFICER")
                .antMatchers("/cases/**").authenticated()
                // Everything else is open to all staff
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/", false)
                .permitAll()
                .and()
            .logout()
                .logoutSuccessUrl("/login?logout")
                .permitAll();
    }
}
