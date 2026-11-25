package cl.duoc.pasteleria.repository;

import cl.duoc.pasteleria.model.DetalleBoleta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {
    boolean existsByProductoId(Long productoId);
}