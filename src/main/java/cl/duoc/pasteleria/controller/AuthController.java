package cl.duoc.pasteleria.controller;

import cl.duoc.pasteleria.dto.AuthResponse;
import cl.duoc.pasteleria.dto.LoginRequest;
import cl.duoc.pasteleria.dto.RegisterRequest;
import cl.duoc.pasteleria.dto.UsuarioResponse;
import cl.duoc.pasteleria.model.Rol;
import cl.duoc.pasteleria.model.Usuario;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import cl.duoc.pasteleria.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login, registro, verificación y renovación de tokens JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest input) {
        String emailNormalizado = input.email().trim().toLowerCase();
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(emailNormalizado, input.password()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Credenciales incorrectas"));
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        String token = jwtService.generarToken(usuario);
        return ResponseEntity.ok(new AuthResponse(
            token, "Bearer", JwtService.EXPIRES_IN_SECONDS, UsuarioResponse.from(usuario)));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar una nueva cuenta con rol CLIENTE")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest input) {
        String emailNormalizado = input.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Ya existe una cuenta registrada con este correo electrónico");
        }

        // Todo registro público entra estrictamente con rol CLIENTE
        Usuario nuevo = new Usuario(
            input.nombre().trim(),
            emailNormalizado,
            input.telefono() != null ? input.telefono().trim() : "",
            passwordEncoder.encode(input.password()),
            Rol.CLIENTE
        );

        Usuario guardado = usuarioRepository.save(nuevo);
        String token = jwtService.generarToken(guardado);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
            token, "Bearer", JwtService.EXPIRES_IN_SECONDS, UsuarioResponse.from(guardado)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar el token JWT vigente antes de su vencimiento")
    public ResponseEntity<AuthResponse> refresh(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(auth.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));

        String nuevoToken = jwtService.generarToken(usuario);
        return ResponseEntity.ok(new AuthResponse(
            nuevoToken, "Bearer", JwtService.EXPIRES_IN_SECONDS, UsuarioResponse.from(usuario)));
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar datos del usuario autenticado por el JWT")
    public ResponseEntity<UsuarioResponse> me(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(auth.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));

        return ResponseEntity.ok(UsuarioResponse.from(usuario));
    }
}