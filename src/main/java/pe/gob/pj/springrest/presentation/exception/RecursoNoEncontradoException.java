package pe.gob.pj.springrest.presentation.exception;

/**
 * Excepción lanzada cuando un recurso (Pedido, Cliente, Producto)
 * solicitado por ID no existe en la base de datos.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(String.format("%s con ID %d no encontrado.", recurso, id));
    }
}