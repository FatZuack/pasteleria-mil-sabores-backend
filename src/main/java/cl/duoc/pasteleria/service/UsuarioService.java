package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.dto.UsuarioAdminRequest;
import cl.duoc.pasteleria.model.Rol;
import cl.duoc.pasteleria.model.Usuario;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Usuario no encontrado con ID " + id));
    }

    public Usuario crear(UsuarioAdminRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Ya existe un usuario registrado con ese correo");
        }
        if (req.password() == null || req.password().length() < 6) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres");
        }

        Rol rol = parsearRol(req.rol());
        Usuario nuevo = new Usuario(
            req.nombre().trim(),
            email,
            req.telefono() != null ? req.telefono().trim() : "",
            passwordEncoder.encode(req.password()),
            rol
        );
        return usuarioRepository.save(nuevo);
    }

    public Usuario actualizar(Long id, UsuarioAdminRequest req) {
        Usuario existente = buscarPorId(id);
        String email = req.email().trim().toLowerCase();

        usuarioRepository.findByEmailIgnoreCase(email)
            .filter(otro -> !otro.getId().equals(id))
            .ifPresent(otro -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Otro usuario ya utiliza el correo " + email);
            });

        existente.setNombre(req.nombre().trim());
        existente.setEmail(email);
        existente.setTelefono(req.telefono() != null ? req.telefono().trim() : "");
        existente.setRol(parsearRol(req.rol()));

        if (req.password() != null && !req.password().isBlank()) {
            if (req.password().length() < 6) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La nueva contraseña debe tener al menos 6 caracteres");
            }
            existente.setPassword(passwordEncoder.encode(req.password()));
        }

        return usuarioRepository.save(existente);
    }

    public void eliminar(Long id, String emailAdminActual) {
        Usuario existente = buscarPorId(id);
        if (existente.getEmail().equalsIgnoreCase(emailAdminActual)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta de administrador");
        }
        usuarioRepository.delete(existente);
    }

    private Rol parsearRol(String valor) {
        try {
            return Rol.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Rol inválido. Usa ADMIN, VENDEDOR o CLIENTE");
        }
    }
}