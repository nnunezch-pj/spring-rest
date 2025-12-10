package pe.gob.pj.springrest.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data // Lombok: Genera Getters, Setters, toString, equals, hashCode
@AllArgsConstructor
public class CrearItemPedidoRequest {

    @NotNull(message = "El ID del producto es obligatorio.")
    private Long productoId; // Solo necesitamos el ID para buscar el Producto (RN4)

    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad mínima debe ser 1.")
    private Integer cantidad;

}