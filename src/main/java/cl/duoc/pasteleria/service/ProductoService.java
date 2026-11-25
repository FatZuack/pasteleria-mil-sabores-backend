package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.dto.ProductoRequest;
import cl.duoc.pasteleria.model.Categoria;
import cl.duoc.pasteleria.model.Producto;
import cl.duoc.pasteleria.repository.CategoriaRepository;
import cl.duoc.pasteleria.repository.DetalleBoletaRepository;
import cl.duoc.pasteleria.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

    private static final String IMAGEN_DEFECTO =
        "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop&q=80";

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetalleBoletaRepository detalleBoletaRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            DetalleBoletaRepository detalleBoletaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.detalleBoletaRepository = detalleBoletaRepository;
    }

    public List<Producto> listar(Long categoriaId, String buscar) {
        if (categoriaId != null) {
            return productoRepository.findByCategoriaId(categoriaId);
        }
        if (buscar != null && !buscar.isBlank()) {
            return productoRepository.findByNombreContainingIgnoreCase(buscar.trim());
        }
        return productoRepository.findAll();
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Producto no encontrado con ID " + id));
    }

    public Producto crear(ProductoRequest req) {
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Categoría no encontrada con ID " + req.categoriaId()));

        String urlImagen = (req.imagen() != null && !req.imagen().isBlank())
            ? req.imagen().trim()
            : IMAGEN_DEFECTO;

        Producto nuevo = new Producto(
            req.nombre().trim(),
            req.descripcion().trim(),
            req.precio(),
            req.stock(),
            urlImagen,
            categoria
        );
        return productoRepository.save(nuevo);
    }

    public Producto actualizar(Long id, ProductoRequest req) {
        Producto existente = buscarPorId(id);
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Categoría no encontrada con ID " + req.categoriaId()));

        String urlImagen = (req.imagen() != null && !req.imagen().isBlank())
            ? req.imagen().trim()
            : IMAGEN_DEFECTO;

        existente.setNombre(req.nombre().trim());
        existente.setDescripcion(req.descripcion().trim());
        existente.setPrecio(req.precio());
        existente.setStock(req.stock());
        existente.setImagen(urlImagen);
        existente.setCategoria(categoria);

        return productoRepository.save(existente);
    }

    public void eliminar(Long id) {
        Producto existente = buscarPorId(id);
        if (detalleBoletaRepository.existsByProductoId(id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede eliminar el producto porque figura en boletas registradas (puedes dejar su stock en 0)");
        }
        productoRepository.delete(existente);
    }
}