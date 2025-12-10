package pe.gob.pj.springrest.infraestructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import pe.gob.pj.springrest.domain.model.Factura;

import java.util.List;
import java.util.Optional;

@Repository
public class FacturaRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FacturaRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    // 1. CREATE / UPDATE (Guardar o persistir)
    // El método 'merge' se usa tanto para nuevas entidades (INSERT)
    // como para entidades existentes (UPDATE).
    public Factura guardar(Factura factura) {
        // 'merge' devuelve la instancia manejada y actualizada.
        Factura facturaManejada = entityManager.merge(factura);
        LOG.info("Factura guardada/actualizada con ID: {}", facturaManejada.getNumero());
        return facturaManejada;
    }

    // 2. READ (Buscar todas - tu método original)
    public List<Factura> buscarTodas() {
        LOG.info("Buscando todas las facturas...");
        return entityManager
                .createQuery("SELECT f FROM Factura f", Factura.class)
                .getResultList();
    }

    // 3. READ (Buscar por ID)
    public Optional<Factura> buscarPorId(Integer id) {
        LOG.info("Buscando factura por ID: {}", id);
        // 'find' busca por clave primaria.
        Factura factura = entityManager.find(Factura.class, id);
        return Optional.ofNullable(factura);
    }

    // 4. DELETE (Eliminar)
    public void eliminar(Integer id) {
        LOG.info("Intentando eliminar factura con ID: {}", id);
        // Primero necesitas encontrar la entidad para ponerla en estado 'managed'
        Factura factura = entityManager.find(Factura.class, id);
        if (factura != null) {
            entityManager.remove(factura);
            LOG.info("Factura con ID {} eliminada exitosamente.", id);
        } else {
            LOG.warn("No se encontró la factura con ID {} para eliminar.", id);
        }
    }
}