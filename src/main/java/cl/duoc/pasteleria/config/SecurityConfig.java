package cl.duoc.pasteleria.config;

import cl.duoc.pasteleria.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.util.Base64;
import java.util.Map;

@Configuration
public class SecurityConfig {

    // Encriptador BCrypt para guardar y verificar contraseñas con hash seguro
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Conecta Spring Security directamente con nuestra tabla USUARIO en H2
    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return email -> usuarioRepository.findByEmailIgnoreCase(email)
            .map(usuario -> User.withUsername(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    // Gestor que valida email + password durante el login
    @Bean
    AuthenticationManager authenticationManager(
            UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    // Decodifica la clave secreta de al menos 32 bytes desde variable de entorno (sin hardcodear en Git)
    @Bean
    SecretKey jwtKey(Environment env) {
        byte[] bytes = Base64.getDecoder().decode(
            env.getRequiredProperty("JWT_SECRET_BASE64"));
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET_BASE64 requiere al menos 32 bytes");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey key) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
        decoder.setJwtValidator(
            JwtValidators.createDefaultWithIssuer("pasteleria-api"));
        return decoder;
    }

    // Lee el claim "rol" del JWT y le antepone "ROLE_" para que funcionen hasRole("ADMIN"), etc.
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter rolesConverter = new JwtGrantedAuthoritiesConverter();
        rolesConverter.setAuthoritiesClaimName("rol");
        rolesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(rolesConverter);
        return converter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper mapper,
            JwtAuthenticationConverter jwtConverter) throws Exception {

        // Respuesta 401 JSON cuando falta token o expiró
        AuthenticationEntryPoint unauthorized = (request, response, ex) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            mapper.writeValue(response.getOutputStream(),
                Map.of("message", "Inicia sesión con un token válido"));
        };

        // Respuesta 403 JSON cuando el token es válido pero el rol no tiene permiso
        AccessDeniedHandler forbidden = (request, response, ex) -> {
            response.setStatus(403);
            response.setContentType("application/json");
            mapper.writeValue(response.getOutputStream(),
                Map.of("message", "No tienes permisos para realizar esta acción"));
        };

        http.cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            // Permite abrir la consola H2 en desarrollo dentro de un iframe del mismo origen
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Preflight CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Rutas públicas: autenticación inicial, Swagger y consola H2
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/error",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/h2-console/**"
                ).permitAll()

                // Renovación y perfil requieren sesión autenticada
                .requestMatchers("/api/auth/refresh", "/api/auth/me").authenticated()

                // Catálogo público de tienda (v1 y v2) para clientes y visitantes
                .requestMatchers(HttpMethod.GET, "/api/v1/productos", "/api/v1/productos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v2/productos", "/api/v2/productos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categorias", "/api/v1/categorias/**").permitAll()

                // Escritura en catálogo y categorías: exclusivo ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/productos", "/api/v2/productos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/productos/**", "/api/v2/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/**", "/api/v2/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/categorias").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/categorias/**").hasRole("ADMIN")

                // Órdenes / Boletas:
                // - Crear compra y ver mis propias boletas: cualquier usuario autenticado
                .requestMatchers(HttpMethod.POST, "/api/v1/boletas").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/boletas/mias").authenticated()
                // - Listar todas las órdenes y ver detalle: ADMIN y VENDEDOR
                .requestMatchers(HttpMethod.GET, "/api/v1/boletas", "/api/v1/boletas/**").hasAnyRole("ADMIN", "VENDEDOR")
                // - Modificar estado o eliminar boletas: exclusivo ADMIN
                .requestMatchers(HttpMethod.PUT, "/api/v1/boletas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/boletas/**").hasRole("ADMIN")

                // Gestión de usuarios: exclusivo ADMIN
                .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/**").hasRole("ADMIN")

                // Cualquier otra ruta no declarada queda bloqueada por defecto
                .anyRequest().denyAll()
            )
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint(unauthorized)
                .accessDeniedHandler(forbidden))
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                .authenticationEntryPoint(unauthorized)
                .accessDeniedHandler(forbidden));

        return http.build();
    }
}