package cl.duoc.pasteleria.repository;

import cl.duoc.pasteleria.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoriaId(Long categoriaId);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    boolean existsByCategoriaId(Long categoriaId);
}