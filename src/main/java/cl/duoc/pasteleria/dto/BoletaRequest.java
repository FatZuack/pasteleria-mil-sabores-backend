package cl.duoc.pasteleria.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BoletaRequest(
    @NotBlank(message = "El nombre del destinatario es obligatorio")
    String nombreCliente,

    @NotBlank(message = "El correo del destinatario es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    String emailCliente,

    String telefonoCliente,

    @NotBlank(message = "La dirección de despacho es obligatoria")
    String direccionDespacho,

    @NotBlank(message = "La comuna es obligatoria")
    String comuna,

    @NotBlank(message = "El método de pago es obligatorio")
    String metodoPago,

    @NotEmpty(message = "La orden debe contener al menos un producto")
    @Valid
    List<BoletaItemRequest> items
) {}