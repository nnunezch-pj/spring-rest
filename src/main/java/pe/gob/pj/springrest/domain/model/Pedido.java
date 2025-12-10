package pe.gob.pj.springrest.domain.model;

import jakarta.persistence.*;

import pe.gob.pj.springrest.domain.enums.EstadoPedido; // Paquete ajustado
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "tipo_entrega", nullable = false, length = 20)
    private String tipoEntrega; // DOMICILIO o RECOGIDA

    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago; // EFECTIVO o TARJETA

    // RN1: Campo donde se almacena el total calculado
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "direccion_entrega", nullable = false, columnDefinition = "TEXT")
    private String direccionEntrega;

    // Relación 1:N con ItemPedido
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> items;
}