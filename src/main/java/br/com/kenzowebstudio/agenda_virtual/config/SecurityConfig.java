package br.com.kenzowebstudio.agenda_virtual.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.kenzowebstudio.agenda_virtual.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login")
                        .permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/events/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/events/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/announcements/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/announcements/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/announcements/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/announcements/**").hasRole("ADMIN")

                        .anyRequest().authenticated())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);

        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }
}