package kr.luxsoft.mdnote.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @org.springframework.beans.factory.annotation.Autowired
    private kr.luxsoft.mdnote.security.JwtTokenProvider jwtTokenProvider;

    @org.springframework.beans.factory.annotation.Value("${app.auth.type:simple}")
    private String authType;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in API mode
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/admin/**")).hasRole("ADMIN")
                .anyRequest().permitAll() 
            );

        if ("jwt".equalsIgnoreCase(authType)) {
            http.addFilterBefore(new kr.luxsoft.mdnote.security.JwtAuthFilter(jwtTokenProvider), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        } else {
            http.addFilterBefore(new kr.luxsoft.mdnote.security.DevAuthFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        }
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
