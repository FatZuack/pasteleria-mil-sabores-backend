package cl.duoc.pasteleria.controller;

import cl.duoc.pasteleria.dto.BoletaRequest;
import cl.duoc.pasteleria.dto.BoletaResponse;
import cl.duoc.pasteleria.dto.EstadoBoletaRequest;
import cl.duoc.pasteleria.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas y Órdenes v1", description = "Gestión de compras, stock y consulta de órdenes por rol")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las órdenes/boletas (ADMIN y VENDEDOR)")
    public List<BoletaResponse> listarTodas() {
        return boletaService.listarTodas().stream()
            .map(BoletaResponse::from)
            .toList();
    }

    @GetMapping("/mias")
    @Operation(summary = "Listar las órdenes del usuario autenticado")
    public List<BoletaResponse> listarMias(Authentication auth) {
        return boletaService.listarPorEmailUsuario(auth.getName()).stream()
            .map(BoletaResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver detalle completo de una boleta por ID (ADMIN y VENDEDOR)")
    public BoletaResponse obtener(@PathVariable Long id) {
        return BoletaResponse.from(boletaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Registrar una compra/boleta y descontar stock (usuario autenticado)")
    public ResponseEntity<BoletaResponse> crear(
            @Valid @RequestBody BoletaRequest req,
            Authentication auth) {
        BoletaResponse creada = BoletaResponse.from(
            boletaService.crearBoleta(req, auth.getName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de una boleta (solo ADMIN)")
    public BoletaResponse actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoBoletaRequest req) {
        return BoletaResponse.from(boletaService.actualizarEstado(id, req.estado()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una boleta (solo ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boletaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}