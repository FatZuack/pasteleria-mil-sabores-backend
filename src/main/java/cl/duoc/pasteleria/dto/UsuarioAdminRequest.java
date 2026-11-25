package cl.duoc.pasteleria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioAdminRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene formato válido")
    String email,

    String telefono,

    // En creación es obligatoria; en edición puede ir vacía para conservar la actual
    String password,

    @NotBlank(message = "El rol es obligatorio (ADMIN, VENDEDOR o CLIENTE)")
    String rol
) {}