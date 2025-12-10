package pe.gob.pj.springrest.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefono; // Requerido para RN3

    @Column(nullable = false, columnDefinition = "TEXT")
    private String direccion; // Requerido para RN3

    // Relación 1:N con Pedido (Opcional incluir la lista de pedidos en Cliente)
    // @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Pedido> pedidos;
}