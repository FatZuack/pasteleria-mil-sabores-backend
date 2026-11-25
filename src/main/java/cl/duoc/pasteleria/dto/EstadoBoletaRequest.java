package cl.duoc.pasteleria.dto;

import jakarta.validation.constraints.NotBlank;

public record EstadoBoletaRequest(
    @NotBlank(message = "El estado de la boleta es obligatorio")
    String estado
) {}