package cl.duoc.pasteleria.dto;

import cl.duoc.pasteleria.model.Usuario;

// DTO seguro que expone los datos del usuario al frontend sin revelar nunca el password
public record UsuarioResponse(
    Long id,
    String nombre,
    String email,
    String telefono,
    String rol
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
            u.getId(),
            u.getNombre(),
            u.getEmail(),
            u.getTelefono(),
            u.getRol().name()
        );
    }
}