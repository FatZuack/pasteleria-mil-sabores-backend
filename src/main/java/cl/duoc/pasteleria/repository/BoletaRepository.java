package cl.duoc.pasteleria.repository;

import cl.duoc.pasteleria.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {
    // Ordena las boletas desde la más reciente primero
    List<Boleta> findAllByOrderByFechaDesc();
    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}