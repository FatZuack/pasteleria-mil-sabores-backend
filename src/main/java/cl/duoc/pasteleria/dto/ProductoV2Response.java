package cl.duoc.pasteleria.dto;

import cl.duoc.pasteleria.model.Producto;

public record ProductoV2Response(
    Long id,
    String nombre,
    String descripcion,
    Integer precio,
    Integer stock,
    String imagen,
    Long categoriaId,
    String categoria,
    String categoriaDescripcion,
    boolean disponible,
    String estadoStock
) {
    public static ProductoV2Response from(Producto p) {
        String estado = p.getStock() <= 0
            ? "AGOTADO"
            : (p.getStock() <= 5 ? "STOCK_BAJO" : "DISPONIBLE");

        return new ProductoV2Response(
            p.getId(),
            p.getNombre(),
            p.getDescripcion(),
            p.getPrecio(),
            p.getStock(),
            p.getImagen(),
            p.getCategoria().getId(),
            p.getCategoria().getNombre(),
            p.getCategoria().getDescripcion(),
            p.getStock() > 0,
            estado
        );
    }
}