package pe.gob.pj.springrest.infraestructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.gob.pj.springrest.application.dto.CrearPedidoRequest;
import pe.gob.pj.springrest.domain.model.Pedido;

@Mapper(componentModel = "spring", uses = {ItemPedidoMapper.class})
public interface PedidoMapper {

    @Mapping(target = "id", ignore = true) // No recibimos el ID del pedido
    @Mapping(target = "estado", ignore = true) // El estado se define en el servicio (RN2)
    @Mapping(target = "fechaHora", ignore = true) // La fecha/hora se define en el servicio
    @Mapping(target = "total", ignore = true) // El total se calcula en el servicio (RN1)

    // Mapea la lista de items. MapStruct usará ItemPedidoMapper para esto.
    @Mapping(target = "items", source = "items")
    // Mapeamos los datos del cliente/dirección
    @Mapping(target = "direccionEntrega", source = "direccionEntrega")
    @Mapping(target = "metodoPago", source = "metodoPago")
    @Mapping(target = "tipoEntrega", source = "tipoEntrega")

        // Nota: El Cliente (Entidad) debe ser asignado manualmente en el Controller/Service
        // usando clienteId/telefonoCliente, ya que MapStruct no puede deducir la lógica RN3
    Pedido toEntity(CrearPedidoRequest dto);
}