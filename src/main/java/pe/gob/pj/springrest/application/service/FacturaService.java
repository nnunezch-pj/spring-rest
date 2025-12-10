package pe.gob.pj.springrest.application.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.springrest.domain.model.Factura;
import pe.gob.pj.springrest.presentation.exception.RecursoNoEncontradoException;
import pe.gob.pj.springrest.infraestructure.persistence.FacturaRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FacturaService {

    private static final Logger LOG = LoggerFactory.getLogger(FacturaService.class);

    public final FacturaRepository repository;

    @Transactional // Nota: Las operaciones de escritura (persist, merge, remove)
    // generalmente requieren una transacción activa.
    public Factura guardar(Factura factura) {
        return repository.guardar(factura);
    }

    @Transactional(readOnly = true) // Es buena práctica marcar las lecturas como solo lectura
    public List<Factura> buscarTodas() {
        return repository.buscarTodas();
    }

    @Transactional(readOnly = true)
    public Optional<Factura> buscarPorId(Integer id) {
        LOG.debug("Buscando factura por ID: {}", id);
        return repository.buscarPorId(id);
        // El repositorio ya devuelve Optional, no hay que hacer nada más aquí.
    }

    @Transactional
    public void eliminar(Integer id) {
        LOG.debug("Intento de eliminación de factura con ID: {}", id); // Opcional, para trazar la entrada al método

        Optional<Factura> facturaOpt = repository.buscarPorId(id);

        if (facturaOpt.isPresent()) {
            repository.eliminar(id);
            LOG.info("Factura con ID {} eliminada exitosamente a través del servicio.", id);
        } else {
            // A nivel de servicio, esto es un WARN o INFO/ERROR dependiendo de la política:
            String mensajeError = "No se puede eliminar la factura. ID " + id + " no encontrado.";

            // Registramos el evento en el log interno del servidor:
            LOG.warn(mensajeError);

            // Lanzamos la excepción que será capturada por el ControllerAdvice:
            throw new RecursoNoEncontradoException(mensajeError);
        }
    }
}