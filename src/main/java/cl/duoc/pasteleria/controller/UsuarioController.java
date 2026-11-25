package cl.duoc.pasteleria.controller;

import cl.duoc.pasteleria.dto.UsuarioAdminRequest;
import cl.duoc.pasteleria.dto.UsuarioResponse;
import cl.duoc.pasteleria.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios v1", description = "Administración de usuarios y roles (exclusivo ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios (solo ADMIN)")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream()
            .map(UsuarioResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por ID (solo ADMIN)")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return UsuarioResponse.from(usuarioService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear un usuario con rol ADMIN, VENDEDOR o CLIENTE (solo ADMIN)")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioAdminRequest req) {
        UsuarioResponse creado = UsuarioResponse.from(usuarioService.crear(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar datos o rol de un usuario (solo ADMIN)")
    public UsuarioResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAdminRequest req) {
        return UsuarioResponse.from(usuarioService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un usuario (solo ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        usuarioService.eliminar(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}