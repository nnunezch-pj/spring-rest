package pe.gob.pj.springrest.infraestructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.gob.pj.springrest.application.dto.ItemPedidoResponse;
import pe.gob.pj.springrest.application.dto.PedidoResponse;
import pe.gob.pj.springrest.domain.model.ItemPedido;
import pe.gob.pj.springrest.domain.model.Pedido;

@Mapper(componentModel = "spring")
public interface PedidoResponseMapper {

    // Mapeo de Entidad Pedido a DTO de Salida PedidoResponse
    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(source = "cliente.nombre", target = "nombreCliente")
    PedidoResponse toResponse(Pedido pedido);

    // Mapeo de Entidad ItemPedido a DTO de Salida ItemPedidoResponse
    @Mapping(source = "producto.nombre", target = "nombreProducto")
    @Mapping(target = "subtotal", expression = "java(item.getPrecioUnitario().multiply(new java.math.BigDecimal(item.getCantidad())))")
    ItemPedidoResponse toItemResponse(ItemPedido item);
}