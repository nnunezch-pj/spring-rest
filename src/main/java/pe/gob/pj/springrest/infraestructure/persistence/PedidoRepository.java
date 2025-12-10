package pe.gob.pj.springrest.infraestructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.springrest.domain.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {}