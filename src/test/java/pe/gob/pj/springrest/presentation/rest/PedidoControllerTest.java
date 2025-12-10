package pe.gob.pj.springrest.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.mockito.Mockito;

import pe.gob.pj.springrest.application.dto.CrearPedidoRequest;
import pe.gob.pj.springrest.application.dto.CrearItemPedidoRequest;
import pe.gob.pj.springrest.application.dto.PedidoResponse; // Necesario para el mock
import pe.gob.pj.springrest.application.service.PedidoService;
import pe.gob.pj.springrest.domain.enums.EstadoPedido;
import pe.gob.pj.springrest.domain.model.Cliente;
import pe.gob.pj.springrest.domain.model.Pedido;
import pe.gob.pj.springrest.infraestructure.mapper.PedidoMapper;
import pe.gob.pj.springrest.infraestructure.mapper.PedidoResponseMapper;
import pe.gob.pj.springrest.infraestructure.persistence.ClienteRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PedidoControllerTest.TestConfig.class)
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;

    // Mocks inyectados (gracias a TestConfig)
    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private PedidoMapper pedidoMapper;
    @Autowired
    private PedidoResponseMapper responseMapper;
    @Autowired
    private ClienteRepository clienteRepository;

    private CrearPedidoRequest requestValida;
    private Pedido pedidoMockeado;

    // ************************************************************
    // CLASE DE CONFIGURACIÓN PARA REEMPLAZAR @MockBean
    // ************************************************************

    @TestConfiguration
    static class TestConfig {
        @Bean @Primary
        public PedidoService mockPedidoService() {
            return Mockito.mock(PedidoService.class);
        }
        @Bean
        public PedidoMapper mockPedidoMapper() {
            return Mockito.mock(PedidoMapper.class);
        }
        @Bean
        public PedidoResponseMapper mockResponseMapper() {
            return Mockito.mock(PedidoResponseMapper.class);
        }
        @Bean
        public ClienteRepository mockClienteRepository() {
            return Mockito.mock(ClienteRepository.class);
        }
    }

    @BeforeEach
    void setUp() {
        // Inicialización de MockMvc usando el contexto de Spring Boot
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // --- 1. Configuración de Entidades y DTOs Mockeadas ---

        pedidoMockeado = Pedido.builder()
                .id(1L)
                .estado(EstadoPedido.PENDIENTE)
                .total(new BigDecimal("25.50"))
                .cliente(Cliente.builder().id(100L).nombre("Mock Customer").build())
                .fechaHora(LocalDateTime.now())
                .direccionEntrega("Calle Falsa 123")
                .build();

        requestValida = new CrearPedidoRequest();
        requestValida.setTelefonoCliente("987654321");
        requestValida.setDireccionEntrega("Av. Principal 456");
        requestValida.setTipoEntrega("DOMICILIO");
        requestValida.setMetodoPago("EFECTIVO");
        requestValida.setItems(List.of(
                new CrearItemPedidoRequest(1L, 1),
                new CrearItemPedidoRequest(2L, 2)
        ));

        // --- 2. Configuración de Mocks de Mappers (Simulación del comportamiento) ---

        // Simular DTO -> Entidad
        when(pedidoMapper.toEntity(any(CrearPedidoRequest.class))).thenReturn(pedidoMockeado);

        // Simular Entidad -> DTO Response
        when(responseMapper.toResponse(any(Pedido.class))).thenAnswer(i -> {
            Pedido p = i.getArgument(0);
            PedidoResponse response = new PedidoResponse();
            response.setId(p.getId()); // Asignamos el ID para validación
            response.setEstado(p.getEstado());
            // ... (otras asignaciones si se requiere mayor detalle de validación JSON)
            return response;
        });
    }

    // ************************************************************
    // PRUEBAS DE CREACIÓN DE PEDIDO (POST /api/pedidos)
    // ************************************************************

    @Test
    void testCrearPedido_Exitoso_Retorna201() throws Exception {
        when(pedidoService.crearPedido(any(Pedido.class))).thenReturn(pedidoMockeado);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValida)))

                .andExpect(status().isCreated()) // HTTP 201 Created
                .andExpect(jsonPath("$.id").value(1L)) // Validamos que el ID del mock se mapee
                .andExpect(jsonPath("$.estado").value(EstadoPedido.PENDIENTE.name()));
    }

    @Test
    void testCrearPedido_FallaPorValidacionDTO_Retorna400() throws Exception {
        // RN3 violada en el DTO (dirección nula)
        requestValida.setDireccionEntrega(null);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValida)))

                .andExpect(status().isBadRequest()) // HTTP 400 Bad Request (Fallo de @Valid)
                .andExpect(jsonPath("$.direccionEntrega").exists());
    }

    @Test
    void testCrearPedido_FallaPorRN4oRN3_Retorna400DelServicio() throws Exception {
        // Simular un fallo de negocio (RN4: Producto no disponible, o RN3 fallida)
        when(pedidoService.crearPedido(any(Pedido.class)))
                .thenThrow(new IllegalArgumentException("RN4: Producto no disponible."));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValida)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("RN4: Producto no disponible."));
    }

    // ************************************************************
    // PRUEBAS DE ACTUALIZACIÓN DE ESTADO (PATCH /api/pedidos/{id}/estado)
    // ************************************************************

    @Test
    void testActualizarEstado_TransicionValida_Retorna200() throws Exception {
        Pedido pedidoActualizado = Pedido.builder().id(1L).estado(EstadoPedido.EN_PREPARACION).total(pedidoMockeado.getTotal()).build();

        when(pedidoService.actualizarEstadoPedido(eq(1L), eq(EstadoPedido.EN_PREPARACION)))
                .thenReturn(pedidoActualizado);

        // Simular la respuesta del mapper con el nuevo estado
        when(responseMapper.toResponse(eq(pedidoActualizado))).thenAnswer(i -> {
            PedidoResponse response = new PedidoResponse();
            response.setId(pedidoActualizado.getId());
            response.setEstado(pedidoActualizado.getEstado());
            return response;
        });

        mockMvc.perform(patch("/api/pedidos/{id}/estado", 1L)
                        .param("nuevoEstado", "EN_PREPARACION"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION")); // Validamos el estado retornado
    }

    @Test
    void testActualizarEstado_TransicionInvalida_Retorna400() throws Exception {
        // Simular que el servicio lanza error por RN2 (ej. PENDIENTE -> ENTREGADO)
        when(pedidoService.actualizarEstadoPedido(eq(1L), eq(EstadoPedido.ENTREGADO)))
                .thenThrow(new IllegalArgumentException("RN2: Transición de estado inválida."));

        mockMvc.perform(patch("/api/pedidos/{id}/estado", 1L)
                        .param("nuevoEstado", "ENTREGADO"))

                .andExpect(status().isBadRequest()) // Fallo de RN2 mapeado a 400
                .andExpect(jsonPath("$.message").value("RN2: Transición de estado inválida."));
    }
}