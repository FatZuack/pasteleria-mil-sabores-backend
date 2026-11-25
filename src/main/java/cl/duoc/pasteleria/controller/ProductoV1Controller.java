package cl.duoc.pasteleria.controller;

import cl.duoc.pasteleria.dto.ProductoRequest;
import cl.duoc.pasteleria.dto.ProductoResponse;
import cl.duoc.pasteleria.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos v1", description = "CRUD estándar del catálogo de productos")
public class ProductoV1Controller {

    private final ProductoService productoService;

    public ProductoV1Controller(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos con filtros opcionales por categoría o nombre (público)")
    public List<ProductoResponse> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String buscar) {
        return productoService.listar(categoriaId, buscar).stream()
            .map(ProductoResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un producto por ID (público)")
    public ProductoResponse obtener(@PathVariable Long id) {
        return ProductoResponse.from(productoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear un producto en el catálogo (solo ADMIN)")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest req) {
        ProductoResponse creado = ProductoResponse.from(productoService.crear(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto existente (solo ADMIN)")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest req) {
        return ProductoResponse.from(productoService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto del catálogo (solo ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}