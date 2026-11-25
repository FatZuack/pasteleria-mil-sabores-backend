package cl.duoc.pasteleria.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductoRequest(
    @NotBlank(message = "El nombre del producto es obligatorio")
    String nombre,

    @NotBlank(message = "La descripción del producto es obligatoria")
    String descripcion,

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 100, message = "El precio debe ser mayor o igual a $100")
    Integer precio,

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    Integer stock,

    String imagen,

    @NotNull(message = "Debe seleccionar una categoría")
    Long categoriaId
) {}