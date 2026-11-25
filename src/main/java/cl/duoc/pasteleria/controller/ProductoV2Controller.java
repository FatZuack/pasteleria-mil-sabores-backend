package cl.duoc.pasteleria.controller;

import cl.duoc.pasteleria.dto.ProductoRequest;
import cl.duoc.pasteleria.dto.ProductoV2Response;
import cl.duoc.pasteleria.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/productos")
@Tag(name = "Productos v2", description = "Versión 2 con estado de disponibilidad e información ampliada de categoría")
public class ProductoV2Controller {

    private final ProductoService productoService;

    public ProductoV2Controller(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos v2 con disponibilidad y estado de stock (público)")
    public List<ProductoV2Response> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String buscar) {
        return productoService.listar(categoriaId, buscar).stream()
            .map(ProductoV2Response::from)
            .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle ampliado v2 de un producto por ID (público)")
    public ProductoV2Response obtener(@PathVariable Long id) {
        return ProductoV2Response.from(productoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear producto v2 (solo ADMIN)")
    public ResponseEntity<ProductoV2Response> crear(@Valid @RequestBody ProductoRequest req) {
        ProductoV2Response creado = ProductoV2Response.from(productoService.crear(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto v2 (solo ADMIN)")
    public ProductoV2Response actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest req) {
        return ProductoV2Response.from(productoService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto v2 (solo ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}