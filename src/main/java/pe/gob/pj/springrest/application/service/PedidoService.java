package pe.gob.pj.springrest.application.service;

import lombok.AllArgsConstructor;
import pe.gob.pj.springrest.application.dto.PedidoResponse;
import pe.gob.pj.springrest.domain.enums.EstadoPedido;
import pe.gob.pj.springrest.domain.model.Cliente;
import pe.gob.pj.springrest.domain.model.Pedido;
import pe.gob.pj.springrest.domain.model.Producto;
import pe.gob.pj.springrest.domain.model.ItemPedido;
import pe.gob.pj.springrest.infraestructure.mapper.PedidoResponseMapper;
import pe.gob.pj.springrest.infraestructure.persistence.ClienteRepository;
import pe.gob.pj.springrest.infraestructure.persistence.PedidoRepository;
import pe.gob.pj.springrest.infraestructure.persistence.ProductoRepository;
import pe.gob.pj.springrest.presentation.exception.RecursoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PedidoService {

    private static final Logger LOG = LoggerFactory.getLogger(PedidoService.class);

    // Inyección de Dependencias
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    // ¡IMPORTANTE! Necesitas inyectar el mapper para devolver PedidoResponse
    private final PedidoResponseMapper responseMapper;

    // Constructor para inyección (@Autowired implícito en Spring Boot 3+)
    /*public PedidoService(PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository,
                         ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }*/

    /**
     * Proceso principal para crear y confirmar un nuevo pedido.
     * Aquí se aplican RN1, RN3 y RN4.
     * * @param nuevoPedido La entidad Pedido (aún incompleta) con Cliente e Items.
     * @return El Pedido ya guardado y confirmado.
     */
    @Transactional // Asegura que toda la operación sea atómica (éxito o rollback)
    public Pedido crearPedido(Pedido nuevoPedido) {
        LOG.info("Iniciando la creación de un nuevo pedido.");

        // 1. VALIDACIÓN DE DATOS DEL CLIENTE (RN3)
        validarDatosEntrega(nuevoPedido.getCliente());

        // 2. CÁLCULO Y VALIDACIÓN DE ÍTEMS (RN1 y RN4)
        BigDecimal totalCalculado = calcularTotalYValidarItems(nuevoPedido);

        // 3. ASIGNACIÓN DE PROPIEDADES FINALES
        nuevoPedido.setTotal(totalCalculado);       // Aplica el resultado de RN1
        nuevoPedido.setEstado(EstadoPedido.PENDIENTE); // Estado inicial (RN2)
        nuevoPedido.setFechaHora(LocalDateTime.now());

        // 4. GUARDAR Y RETORNAR
        return pedidoRepository.save(nuevoPedido);
    }

    /**
     * Implementa la Regla de Negocio RN3: Validación de Datos de Entrega.
     */
    private void validarDatosEntrega(Cliente cliente) {
        if (cliente == null ||
                !StringUtils.hasText(cliente.getTelefono()) ||
                !StringUtils.hasText(cliente.getDireccion())) {

            LOG.warn("RN3 Fallida: Datos de cliente o dirección incompletos.");
            throw new IllegalArgumentException("RN3: El teléfono y la dirección de entrega son obligatorios para confirmar el pedido.");
        }
    }

    /**
     * Implementa la Regla de Negocio RN1 (Cálculo) y RN4 (Disponibilidad).
     * @return El total final del pedido.
     */
    private BigDecimal calcularTotalYValidarItems(Pedido pedido) {
        BigDecimal total = BigDecimal.ZERO;
        List<ItemPedido> itemsValidados = new ArrayList<>();

        for (ItemPedido item : pedido.getItems()) {
            Producto producto = productoRepository.findById(item.getProducto().getId())
                    .orElseThrow(() -> {
                        LOG.error("Producto no encontrado: ID {}", item.getProducto().getId());
                        return new RecursoNoEncontradoException("Producto con ID " + item.getProducto().getId() + " no encontrado.");
                    });

            // RN4: Validación de Disponibilidad
            if (!producto.getDisponible()) {
                LOG.error("RN4 Fallida: Producto ID {} no disponible.", producto.getId());
                throw new IllegalArgumentException("RN4: El producto '" + producto.getNombre() + "' no está disponible actualmente.");
            }

            // RN1: Cálculo del Subtotal
            BigDecimal precioUnitario = producto.getPrecioBase();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad()));

            // Actualizar ItemPedido con datos finales (precio, relación)
            item.setProducto(producto); // Asegura que el producto completo esté en el item
            item.setPrecioUnitario(precioUnitario);
            item.setPedido(pedido); // Enlaza el Item al Pedido

            total = total.add(subtotal);
            itemsValidados.add(item);
        }

        if (itemsValidados.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un ítem.");
        }

        pedido.setItems(itemsValidados); // Reemplaza la lista, asegurando que todos los ítems estén validados y enlazados
        return total;
    }

    // ************************************************************
    // IMPLEMENTACIÓN DE LA RN2: SECUENCIA DE ESTADOS
    // ************************************************************

    /**
     * Implementa la Regla de Negocio RN2: Cambio de estado con validación estricta.
     * @param idPedido ID del pedido a actualizar.
     * @param nuevoEstado El estado al que se desea transicionar.
     * @return El Pedido actualizado.
     */
    @Transactional
    public Pedido actualizarEstadoPedido(Long idPedido, EstadoPedido nuevoEstado) {

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido con ID " + idPedido + " no encontrado."));

        EstadoPedido estadoActual = pedido.getEstado();

        // RN2: Validación de la secuencia de transición
        if (!estadoActual.esTransicionValida(nuevoEstado)) {
            LOG.warn("RN2 Fallida: Transición inválida. Pedido ID {} de {} a {}", idPedido, estadoActual, nuevoEstado);
            throw new IllegalArgumentException(
                    String.format("RN2: Transición de estado inválida. No se puede pasar de %s a %s.",
                            estadoActual, nuevoEstado));
        }

        // Si la transición es válida, actualizamos el estado
        pedido.setEstado(nuevoEstado);
        LOG.info("Pedido ID {} actualizado de {} a {}", idPedido, estadoActual, nuevoEstado);

        // Lógica adicional (ej. notificar al cliente o repartidor) podría ir aquí

        return pedidoRepository.save(pedido);
    }

    // ************************************************************
    // NUEVAS IMPLEMENTACIONES: BÚSQUEDA DE PEDIDOS
    // ************************************************************

    /**
     * Busca un pedido por su ID.
     * Implementa manejo de excepción si el recurso no es encontrado (404).
     * @param id ID del pedido.
     * @return El DTO de respuesta del pedido encontrado.
     */
    @Transactional(readOnly = true) // Solo lectura, optimizado para consultas
    public PedidoResponse buscarPedidoPorId(Long id) {
        LOG.info("Buscando pedido por ID: {}", id);

        // Uso de Optional y orElseThrow, manejando 404 a través de la excepción
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.warn("Pedido ID {} no encontrado.", id);
                    return new RecursoNoEncontradoException("Pedido", id);
                });

        return responseMapper.toResponse(pedido);
    }

    /**
     * Lista todos los pedidos existentes en el sistema.
     * (Nota: En un sistema real se debería usar paginación).
     * @return Lista de DTOs PedidoResponse.
     */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodosPedidos() {
        LOG.info("Listando todos los pedidos.");

        List<Pedido> pedidos = pedidoRepository.findAll();

        // Convertir la lista de entidades a una lista de DTOs
        return pedidos.stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
    }
}