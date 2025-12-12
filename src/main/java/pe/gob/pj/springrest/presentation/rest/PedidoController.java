package pe.gob.pj.springrest.presentation.rest;

import pe.gob.pj.springrest.application.dto.CrearPedidoRequest;
import pe.gob.pj.springrest.application.dto.PedidoResponse;
import pe.gob.pj.springrest.application.service.PedidoService;
import pe.gob.pj.springrest.domain.enums.EstadoPedido;
import pe.gob.pj.springrest.domain.model.Cliente;
import pe.gob.pj.springrest.domain.model.Pedido;
import pe.gob.pj.springrest.infraestructure.mapper.PedidoMapper;
import pe.gob.pj.springrest.infraestructure.mapper.PedidoResponseMapper;
import pe.gob.pj.springrest.infraestructure.persistence.ClienteRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoMapper pedidoMapper;
    private final PedidoResponseMapper responseMapper; // Nuevo inyectado
    private final ClienteRepository clienteRepository;

    public PedidoController(PedidoService pedidoService,
                            PedidoMapper pedidoMapper,
                            PedidoResponseMapper responseMapper, // Inyección en constructor
                            ClienteRepository clienteRepository) {
        this.pedidoService = pedidoService;
        this.pedidoMapper = pedidoMapper;
        this.responseMapper = responseMapper; // Asignación
        this.clienteRepository = clienteRepository;
    }

    /**
     * HU4, HU5, HU6: Crea un nuevo Pedido (Aplicación de RN1, RN3, RN4).
     * Retorna PedidoResponse DTO.
     */
    @PostMapping
    public ResponseEntity<PedidoResponse> crearPedido(@Valid @RequestBody CrearPedidoRequest request) {

        Pedido pedido = pedidoMapper.toEntity(request);

        Cliente cliente = gestionarCliente(request);
        pedido.setCliente(cliente);

        Pedido pedidoCreado = pedidoService.crearPedido(pedido);

        // Convertir la Entidad final a DTO de Respuesta
        PedidoResponse response = responseMapper.toResponse(pedidoCreado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * HU9: Actualiza el estado de un Pedido (Aplicación de RN2).
     * Retorna PedidoResponse DTO.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPedido nuevoEstado) {

        Pedido pedidoActualizado = pedidoService.actualizarEstadoPedido(id, nuevoEstado);

        // Convertir la Entidad final a DTO de Respuesta
        PedidoResponse response = responseMapper.toResponse(pedidoActualizado);

        return ResponseEntity.ok(response);
    }

    // Método auxiliar gestionarCliente (sin cambios)
    private Cliente gestionarCliente(CrearPedidoRequest request) {
        // ... (Implementación existente)
        if (request.getClienteId() != null) {
            return clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente ID no encontrado."));
        }

        return Cliente.builder()
                .telefono(request.getTelefonoCliente())
                .direccion(request.getDireccionEntrega())
                .nombre("Cliente Temporal")
                .email(null)
                .build();
    }

    // ===============================================
    // NUEVOS MÉTODOS GET
    // ===============================================

    /**
     * GET /api/pedidos/{id}
     * Busca un pedido por su ID. Devuelve 404 si no existe (manejo por GlobalExceptionHandler).
     */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPedidoPorId(@PathVariable Long id) {
        PedidoResponse response = pedidoService.buscarPedidoPorId(id);
        return ResponseEntity.ok(response); // HTTP 200 OK
    }

    /**
     * GET /api/pedidos
     * Lista todos los pedidos existentes en el sistema.
     */
    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidos() {
        List<PedidoResponse> responseList = pedidoService.listarTodosPedidos();
        return ResponseEntity.ok(responseList); // HTTP 200 OK
    }
}