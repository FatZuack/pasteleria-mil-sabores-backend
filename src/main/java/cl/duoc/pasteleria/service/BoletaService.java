package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.dto.BoletaItemRequest;
import cl.duoc.pasteleria.dto.BoletaRequest;
import cl.duoc.pasteleria.model.Boleta;
import cl.duoc.pasteleria.model.DetalleBoleta;
import cl.duoc.pasteleria.model.Producto;
import cl.duoc.pasteleria.model.Usuario;
import cl.duoc.pasteleria.repository.BoletaRepository;
import cl.duoc.pasteleria.repository.ProductoRepository;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class BoletaService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
        "PAGADA", "EN_PREPARACION", "ENTREGADA", "ANULADA"
    );

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public BoletaService(
            BoletaRepository boletaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository) {
        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Boleta> listarTodas() {
        return boletaRepository.findAllByOrderByFechaDesc();
    }

    public List<Boleta> listarPorEmailUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        return boletaRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId());
    }

    public Boleta buscarPorId(Long id) {
        return boletaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Boleta no encontrada con ID " + id));
    }

    // @Transactional garantiza atomicidad: si falla el stock de un ítem, se revierte toda la operación
    @Transactional
    public Boleta crearBoleta(BoletaRequest req, String emailAutenticado) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailAutenticado)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));

        Boleta boleta = new Boleta();
        boleta.setCodigoOrden("ORD-" + (System.currentTimeMillis() % 1_000_000));
        boleta.setNombreCliente(req.nombreCliente().trim());
        boleta.setEmailCliente(req.emailCliente().trim().toLowerCase());
        boleta.setTelefonoCliente(req.telefonoCliente() != null ? req.telefonoCliente().trim() : "");
        boleta.setDireccionDespacho(req.direccionDespacho().trim());
        boleta.setComuna(req.comuna().trim());
        boleta.setMetodoPago(req.metodoPago().trim().toUpperCase());
        boleta.setEstado("PAGADA");
        boleta.setUsuario(usuario);

        int totalCalculado = 0;

        for (BoletaItemRequest item : req.items()) {
            Producto producto = productoRepository.findById(item.productoId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Producto no encontrado con ID " + item.productoId()));

            if (producto.getStock() < item.cantidad()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stock insuficiente para '" + producto.getNombre()
                        + "'. Disponible: " + producto.getStock());
            }

            // Descuenta el stock real y persiste el cambio
            producto.setStock(producto.getStock() - item.cantidad());
            productoRepository.save(producto);

            // Crea la línea de detalle congelando el precio vigente del producto
            DetalleBoleta detalle = new DetalleBoleta(producto, item.cantidad());
            boleta.agregarDetalle(detalle);
            totalCalculado += detalle.getSubtotal();
        }

        boleta.setTotal(totalCalculado);
        return boletaRepository.save(boleta);
    }

    public Boleta actualizarEstado(Long id, String nuevoEstado) {
        String estadoNormalizado = nuevoEstado.trim().toUpperCase();
        if (!ESTADOS_VALIDOS.contains(estadoNormalizado)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Estado inválido. Valores permitidos: PAGADA, EN_PREPARACION, ENTREGADA, ANULADA");
        }
        Boleta boleta = buscarPorId(id);
        boleta.setEstado(estadoNormalizado);
        return boletaRepository.save(boleta);
    }

    public void eliminar(Long id) {
        Boleta boleta = buscarPorId(id);
        boletaRepository.delete(boleta);
    }
}