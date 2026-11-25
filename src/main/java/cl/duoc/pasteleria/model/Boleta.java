package cl.duoc.pasteleria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BOLETA")
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código legible de orden (ej: ORD-000001)
    @Column(nullable = false, unique = true, length = 30)
    private String codigoOrden;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false)
    private Integer total;

    @Column(nullable = false, length = 30)
    private String estado = "PAGADA";

    // Datos de despacho capturados en CheckoutView.jsx
    @Column(nullable = false, length = 120)
    private String nombreCliente;

    @Column(nullable = false, length = 150)
    private String emailCliente;

    @Column(length = 30)
    private String telefonoCliente;

    @Column(nullable = false, length = 200)
    private String direccionDespacho;

    @Column(nullable = false, length = 80)
    private String comuna;

    @Column(nullable = false, length = 40)
    private String metodoPago;

    // Relación N:1 -> Usuario que realizó la compra (puede ser null si compra sin sesión o enlazarse si está logueado)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Relación 1:N -> Una boleta contiene múltiples líneas de DetalleBoleta
    // cascade = ALL guarda automáticamente los detalles al guardar la boleta
    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetalleBoleta> detalles = new ArrayList<>();

    public Boleta() {}

    // Método auxiliar para mantener sincronizados ambos lados de la relación bidireccional
    public void agregarDetalle(DetalleBoleta detalle) {
        detalles.add(detalle);
        detalle.setBoleta(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoOrden() { return codigoOrden; }
    public void setCodigoOrden(String codigoOrden) { this.codigoOrden = codigoOrden; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }

    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }

    public String getDireccionDespacho() { return direccionDespacho; }
    public void setDireccionDespacho(String direccionDespacho) { this.direccionDespacho = direccionDespacho; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<DetalleBoleta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleBoleta> detalles) { this.detalles = detalles; }
}