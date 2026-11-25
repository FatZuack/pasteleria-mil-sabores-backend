package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.dto.CategoriaRequest;
import cl.duoc.pasteleria.model.Categoria;
import cl.duoc.pasteleria.repository.CategoriaRepository;
import cl.duoc.pasteleria.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Categoría no encontrada con ID " + id));
    }

    public Categoria crear(CategoriaRequest req) {
        String nombreLimpio = req.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombreLimpio)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Ya existe una categoría con el nombre '" + nombreLimpio + "'");
        }
        String desc = req.descripcion() != null ? req.descripcion().trim() : "";
        return categoriaRepository.save(new Categoria(nombreLimpio, desc));
    }

    public Categoria actualizar(Long id, CategoriaRequest req) {
        Categoria existente = buscarPorId(id);
        String nombreLimpio = req.nombre().trim();

        categoriaRepository.findByNombreIgnoreCase(nombreLimpio)
            .filter(otra -> !otra.getId().equals(id))
            .ifPresent(otra -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe otra categoría llamada '" + nombreLimpio + "'");
            });

        existente.setNombre(nombreLimpio);
        existente.setDescripcion(req.descripcion() != null ? req.descripcion().trim() : "");
        return categoriaRepository.save(existente);
    }

    public void eliminar(Long id) {
        Categoria existente = buscarPorId(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "No se puede eliminar la categoría porque tiene productos asociados");
        }
        categoriaRepository.delete(existente);
    }
}