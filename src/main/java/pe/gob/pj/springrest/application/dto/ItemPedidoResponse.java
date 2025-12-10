package pe.gob.pj.springrest.application.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemPedidoResponse {
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal; // Añadido para conveniencia del cliente
}