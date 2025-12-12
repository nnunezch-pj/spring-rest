package pe.gob.pj.springrest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.pj.springrest.application.dto.PedidoResponse;
import pe.gob.pj.springrest.application.service.PedidoService;
import pe.gob.pj.springrest.domain.enums.EstadoPedido;
import pe.gob.pj.springrest.domain.model.Cliente;
import pe.gob.pj.springrest.domain.model.ItemPedido;
import pe.gob.pj.springrest.domain.model.Pedido;
import pe.gob.pj.springrest.domain.model.Producto;
import pe.gob.pj.springrest.infraestructure.mapper.PedidoResponseMapper;
import pe.gob.pj.springrest.infraestructure.persistence.ClienteRepository;
import pe.gob.pj.springrest.infraestructure.persistence.PedidoRepository;
import pe.gob.pj.springrest.infraestructure.persistence.ProductoRepository;
import pe.gob.pj.springrest.presentation.exception.RecursoNoEncontradoException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita la inyección de Mocks
public class PedidoServiceTest {

    // Se inyectan los mocks en la instancia del servicio que vamos a probar
    @InjectMocks
    private PedidoService pedidoService;

    // Declaración de los Mocks
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private PedidoResponseMapper responseMapper;

    private Producto pizzaPepperoni;
    private Producto pizzaMargarita;
    private Cliente clienteValido;
    private Pedido pedidoMockeado;

    @BeforeEach
    void setUp() {

        // 1. Configuración de Entidades Mockeadas
        pizzaPepperoni = Producto.builder()
                .id(1L)
                .nombre("Pepperoni Clásica")
                .precioBase(new BigDecimal("15.50"))
                .disponible(true) // RN4 OK
                .build();

        pizzaMargarita = Producto.builder()
                .id(2L)
                .nombre("Margarita")
                .precioBase(new BigDecimal("10.00"))
                .disponible(true) // RN4 OK
                .build();

        clienteValido = Cliente.builder()
                .id(100L)
                .nombre("Test Customer")
                .telefono("987654321")
                .direccion("Av. Test 123") // RN3 OK
                .build();

        // Inicialización de la entidad mockeada compartida
        pedidoMockeado = Pedido.builder()
                .id(1L)
                .estado(EstadoPedido.PENDIENTE)
                .total(new BigDecimal("50.00"))
                .items(List.of()) // Simplificado para el ejemplo
                .build();
    }

    // ************************************************************
    // PRUEBAS PARA RN1 (Cálculo de Total) y RN4 (Disponibilidad)
    // ************************************************************

    @Test
    void testCrearPedido_CalculoTotalExitoso_RN1() {
        // Datos de entrada: 3x Margarita, 1x Pepperoni
        ItemPedido item1 = ItemPedido.builder().producto(Producto.builder().id(2L).build()).cantidad(3).build();
        ItemPedido item2 = ItemPedido.builder().producto(Producto.builder().id(1L).build()).cantidad(1).build();

        Pedido pedidoInput = Pedido.builder()
                .cliente(clienteValido)
                .tipoEntrega("DOMICILIO")
                .metodoPago("EFECTIVO")
                .direccionEntrega(clienteValido.getDireccion())
                .items(List.of(item1, item2))
                .build();

        // Configuración de Mocks: simular que los productos existen
        when(productoRepository.findById(1L)).thenReturn(Optional.of(pizzaPepperoni));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(pizzaMargarita));

        // Configuración de Mocks: simular que el pedido se guarda correctamente
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(50L); // Asignar un ID simulado
            return pedido;
        });

        // EJECUCIÓN
        Pedido pedidoResult = pedidoService.crearPedido(pedidoInput);

        // VERIFICACIÓN (ASSERTIONS)

        // Cálculo esperado: (3 * $10.00) + (1 * $15.50) = $30.00 + $15.50 = $45.50
        BigDecimal totalEsperado = new BigDecimal("45.50");

        assertNotNull(pedidoResult);
        assertEquals(totalEsperado, pedidoResult.getTotal(), "RN1: El total calculado debe ser correcto.");
        assertEquals(2, pedidoResult.getItems().size(), "El número de ítems debe ser el correcto.");
        //assertEquals(pizzaMargarita.getPrecioBase(), pedidoResult.getItems().get(0).getPrecioUnitario(), "El precio unitario debe ser copiado del Producto.");

        // Verificar que se llamó al método de persistencia
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    // ************************************************************
    // PRUEBAS PARA RN4 (Disponibilidad)
    // ************************************************************

    @Test
    void testCrearPedido_FallaPorProductoNoDisponible_RN4() {
        // Clonar un producto y marcarlo como no disponible
        Producto pizzaAgotada = Producto.builder()
                .id(3L)
                .nombre("Cuatro Quesos")
                .precioBase(new BigDecimal("16.75"))
                .disponible(false) // RN4: ESTE FALLARÁ
                .build();

        // Datos de entrada con el producto agotado
        ItemPedido itemAgotado = ItemPedido.builder().producto(Producto.builder().id(3L).build()).cantidad(1).build();

        Pedido pedidoInput = Pedido.builder()
                .cliente(clienteValido)
                .items(List.of(itemAgotado))
                .build();

        // Configuración de Mocks: retornar el producto agotado
        when(productoRepository.findById(3L)).thenReturn(Optional.of(pizzaAgotada));

        // EJECUCIÓN y VERIFICACIÓN DE EXCEPCIÓN
        // Se espera que falle con IllegalArgumentException por la RN4
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.crearPedido(pedidoInput);
        });

        assertTrue(thrown.getMessage().contains("RN4: El producto 'Cuatro Quesos' no está disponible"), "Debe fallar por indisponibilidad del producto (RN4).");
        verify(pedidoRepository, never()).save(any(Pedido.class)); // Verificar que NO se guardó en la DB
    }

    // ************************************************************
    // PRUEBAS PARA RN3 (Validación de Datos)
    // ************************************************************
    // ⚠️ Caso negativo: la creación del pedido debe lanzar excepción (❌)
    // cuando faltan datos obligatorios (RN3).
    @Test
    void testCrearPedido_LanzaExcepcion_CuandoDireccionEsNula_RN3() {
        // Cliente con datos incompletos
        Cliente clienteInvalido = Cliente.builder()
                .id(101L)
                .nombre("Missing Address")
                .telefono("987654321")
                .direccion(null) // RN3: ESTE FALLARÁ
                .build();

        // Mockear producto solo para que pase la validación de RN4, aunque no llegará ahí
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(pizzaPepperoni));

        Pedido pedidoInput = Pedido.builder()
                .cliente(clienteInvalido)
                // Se necesitan items para llegar a la lógica de validación
                .items(List.of(ItemPedido.builder().producto(Producto.builder().id(1L).build()).cantidad(1).build()))
                .build();

        // EJECUCIÓN y VERIFICACIÓN DE EXCEPCIÓN
        // Se espera que falle con IllegalArgumentException por la RN3
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.crearPedido(pedidoInput);
        });

        assertTrue(thrown.getMessage().contains("RN3: El teléfono y la dirección de entrega son obligatorios"), "Debe fallar por dirección faltante (RN3).");
        verify(pedidoRepository, never()).save(any(Pedido.class)); // Verificar que NO se guardó
    }

    // ==================================================================

    @Test
    void testBuscarPedidoPorId_Exitoso() {
        // Simular que el repositorio encuentra un pedido
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoMockeado));

        // Simular la respuesta del mapper
        when(responseMapper.toResponse(any(Pedido.class))).thenReturn(new PedidoResponse());

        PedidoResponse result = pedidoService.buscarPedidoPorId(1L);

        assertNotNull(result);
        // Verificar más detalles del PedidoResponse si es necesario
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPedidoPorId_NoEncontrado_LanzaExcepcion() {
        // Simular que el repositorio NO encuentra el pedido (retorna Optional.empty())
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        // Verificar que el servicio lanza la excepción correcta
        assertThrows(RecursoNoEncontradoException.class, () -> {
            pedidoService.buscarPedidoPorId(999L);
        });
    }

    @Test
    void testListarTodosPedidos_RetornaListaNoVacia() {
        // Simular que el repositorio devuelve una lista de pedidos
        List<Pedido> pedidosMock = List.of(pedidoMockeado, Pedido.builder().id(2L).estado(EstadoPedido.CANCELADO).build());
        when(pedidoRepository.findAll()).thenReturn(pedidosMock);

        // Simular la conversión a DTOs
        when(responseMapper.toResponse(any(Pedido.class))).thenReturn(new PedidoResponse());

        List<PedidoResponse> resultados = pedidoService.listarTodosPedidos();

        assertNotNull(resultados);
        assertEquals(2, resultados.size());
        verify(pedidoRepository, times(1)).findAll();
    }
}