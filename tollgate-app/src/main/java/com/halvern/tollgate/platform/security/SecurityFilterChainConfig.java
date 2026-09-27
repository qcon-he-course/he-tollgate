package com.halvern.tollgate.platform.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.halvern.tollgate.portal.ApiKeyDao;
import com.halvern.tollgate.portal.security.ApiKeyAuthFilter;

/**
 * Security configuration in the component-based style that Spring Security 6 requires.
 *
 * <p>{@code WebSecurityConfigurerAdapter} is removed in Spring Security 6, so this replaces
 * {@link LegacySecurityConfig} as part of the Spring Boot 3 upgrade (docs/upgrade-boot3.md). The rules are the same.
 * It is only active with the {@code boot3-prep} profile until the rest of the upgrade is done; switch it on locally
 * with {@code -Dspring-boot.run.profiles=local,boot3-prep}.
 */
@Configuration
@Profile("boot3-prep")
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityFilterChainConfig {

    private static final String USERS_QUERY =
            "SELECT username, password_hash, enabled FROM app_user WHERE username = ?";

    private static final String ROLES_QUERY =
            "SELECT u.username, 'ROLE_' || r.role FROM user_role r JOIN app_user u ON u.id = r.user_id WHERE u.username = ?";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JdbcUserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
        users.setUsersByUsernameQuery(USERS_QUERY);
        users.setAuthoritiesByUsernameQuery(ROLES_QUERY);
        return users;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyDao apiKeyDao) throws Exception {
        http.addFilterBefore(new ApiKeyAuthFilter(apiKeyDao), UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.ignoringAntMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .antMatchers("/login", "/css/**", "/js/**", "/webjars/**", "/favicon.ico", "/error").permitAll()
                        .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .antMatchers("/actuator/**").permitAll()
                        .antMatchers("/api/**").hasRole("HAULIER")
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        .antMatchers("/closures/new").hasAnyRole("ENGINEER", "ADMIN")
                        .antMatchers(HttpMethod.POST, "/closures/**").hasAnyRole("ENGINEER", "ADMIN")
                        .antMatchers("/applications/new", "/applications/*/edit", "/applications/*/lodge",
                                "/applications/*/attachments", "/applications/*/questionnaire",
                                "/applications/*/review/**", "/applications/*/approve").hasRole("OFFICER")
                        .antMatchers("/cases/*/lodge", "/cases/*/answers/**").hasRole("OFFICER")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", false).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());
        return http.build();
    }
}
