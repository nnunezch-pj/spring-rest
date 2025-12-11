package pe.gob.pj.springrest.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

// Indica que esta clase gestionará excepciones de todos los controladores
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja las excepciones de Reglas de Negocio (RN), mapeando IllegalArgumentException
     * a HTTP 400 Bad Request. Esto incluye:
     * - RN2: Transición de estado inválida.
     * - RN4: Producto no disponible o stock insuficiente.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse errorResponse = new ErrorResponse(
                status,
                ex.getMessage(), // El mensaje de la excepción (ej: "RN4: Producto no disponible.")
                request.getDescription(false).replace("uri=", "") // request.getDescription(false) = "uri=/api/pedidos"
        );

        // Loggear el error si es necesario (ej: logger.warn("Bad Request: {}", ex.getMessage()))

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Maneja las excepciones de validación de DTOs lanzadas por @Valid.
     * Mapea MethodArgumentNotValidException a HTTP 400 Bad Request
     * y extrae los errores de campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // 1. Recolectar errores de campo
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        // 2. Crear la respuesta con el mapa de errores
        ErrorResponse errorResponse = new ErrorResponse(
                status,
                "Error de validación en la solicitud.",
                request.getDescription(false).replace("uri=", ""),
                errors
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Maneja la excepción personalizada RecursoNoEncontradoException, mapeándola a
     * HTTP 404 Not Found. Esto ocurre si se busca un Pedido que no existe.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(RecursoNoEncontradoException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ErrorResponse errorResponse = new ErrorResponse(
                status,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    // --- Manejador genérico para cualquier otra excepción no manejada ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse errorResponse = new ErrorResponse(
                status,
                "Ha ocurrido un error inesperado en el servidor.",
                request.getDescription(false).replace("uri=", "")
        );

        // Es crucial loggear la excepción completa aquí (stack trace)
        // logger.error("Internal Server Error: {}", ex.getMessage(), ex);

        return new ResponseEntity<>(errorResponse, status);
    }
}