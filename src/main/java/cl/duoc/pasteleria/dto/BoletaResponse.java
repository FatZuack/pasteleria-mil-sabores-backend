package cl.duoc.pasteleria.dto;

import cl.duoc.pasteleria.model.Boleta;
import java.time.LocalDateTime;
import java.util.List;

public record BoletaResponse(
    Long id,
    String codigoOrden,
    LocalDateTime fecha,
    Integer total,
    String estado,
    String nombreCliente,
    String emailCliente,
    String telefonoCliente,
    String direccionDespacho,
    String comuna,
    String metodoPago,
    Long usuarioId,
    List<DetalleBoletaResponse> detalles
) {
    public static BoletaResponse from(Boleta b) {
        List<DetalleBoletaResponse> items = b.getDetalles().stream()
            .map(DetalleBoletaResponse::from)
            .toList();

        return new BoletaResponse(
            b.getId(),
            b.getCodigoOrden(),
            b.getFecha(),
            b.getTotal(),
            b.getEstado(),
            b.getNombreCliente(),
            b.getEmailCliente(),
            b.getTelefonoCliente(),
            b.getDireccionDespacho(),
            b.getComuna(),
            b.getMetodoPago(),
            b.getUsuario() != null ? b.getUsuario().getId() : null,
            items
        );
    }
}