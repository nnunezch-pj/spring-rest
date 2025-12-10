package pe.gob.pj.springrest.application.dto;

import pe.gob.pj.springrest.domain.enums.EstadoPedido;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoResponse {
    private Long id;
    private Long clienteId;
    private String nombreCliente;
    private LocalDateTime fechaHora;
    private EstadoPedido estado;
    private String tipoEntrega;
    private String direccionEntrega;
    private BigDecimal total; // RN1
    private List<ItemPedidoResponse> items;
}