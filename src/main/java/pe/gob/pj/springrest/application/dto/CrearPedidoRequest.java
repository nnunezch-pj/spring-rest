package pe.gob.pj.springrest.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;

@Data
public class CrearPedidoRequest {

    // Si es un cliente existente, se pasa el ID. Si es nuevo, el ID será nulo.
    private Long clienteId;

    // Para la RN3: Validación de datos de entrega
    @NotBlank(message = "El teléfono del cliente es requerido (RN3).")
    @Pattern(regexp = "^[0-9]{9,}$", message = "El teléfono debe contener al menos 9 dígitos.")
    private String telefonoCliente;

    @NotBlank(message = "La dirección de entrega es requerida (RN3).")
    private String direccionEntrega;

    @NotBlank(message = "El tipo de entrega (DOMICILIO/RECOGIDA) es requerido.")
    private String tipoEntrega;

    @NotBlank(message = "El método de pago (EFECTIVO/TARJETA) es requerido.")
    private String metodoPago;

    // Lista de ítems del carrito. Usamos @Valid para validar también los DTOs internos.
    @NotEmpty(message = "El pedido debe contener al menos un ítem.")
    @Valid
    private List<CrearItemPedidoRequest> items;
}