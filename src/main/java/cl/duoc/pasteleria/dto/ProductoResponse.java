package cl.duoc.pasteleria.dto;

import cl.duoc.pasteleria.model.Producto;

public record ProductoResponse(
    Long id,
    String nombre,
    String descripcion,
    Integer precio,
    Integer stock,
    String imagen,
    Long categoriaId,
    String categoria
) {
    public static ProductoResponse from(Producto p) {
        return new ProductoResponse(
            p.getId(),
            p.getNombre(),
            p.getDescripcion(),
            p.getPrecio(),
            p.getStock(),
            p.getImagen(),
            p.getCategoria().getId(),
            p.getCategoria().getNombre()
        );
    }
}