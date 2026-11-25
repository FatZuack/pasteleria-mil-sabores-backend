package cl.duoc.pasteleria.dto;

import cl.duoc.pasteleria.model.DetalleBoleta;

public record DetalleBoletaResponse(
    Long id,
    Long productoId,
    String nombreProducto,
    String imagenProducto,
    Integer cantidad,
    Integer precioUnitario,
    Integer subtotal
) {
    public static DetalleBoletaResponse from(DetalleBoleta d) {
        return new DetalleBoletaResponse(
            d.getId(),
            d.getProducto().getId(),
            d.getProducto().getNombre(),
            d.getProducto().getImagen(),
            d.getCantidad(),
            d.getPrecioUnitario(),
            d.getSubtotal()
        );
    }
}