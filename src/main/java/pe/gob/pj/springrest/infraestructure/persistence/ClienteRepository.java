package pe.gob.pj.springrest.infraestructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.springrest.domain.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {}