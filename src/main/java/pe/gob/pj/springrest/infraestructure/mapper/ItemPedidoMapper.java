package pe.gob.pj.springrest.infraestructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.gob.pj.springrest.application.dto.CrearItemPedidoRequest;
import pe.gob.pj.springrest.domain.model.ItemPedido;
import pe.gob.pj.springrest.domain.model.Producto;

@Mapper(componentModel = "spring")
public interface ItemPedidoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pedido", ignore = true)
    @Mapping(target = "precioUnitario", ignore = true) // Se calcula en el servicio

    // Creamos una referencia proxy a Producto usando el ID. El servicio buscará el objeto completo.
    @Mapping(target = "producto", expression = "java(mapProducto(dto.getProductoId()))")
    ItemPedido toEntity(CrearItemPedidoRequest dto);

    // Método auxiliar para MapStruct
    default Producto mapProducto(Long productoId) {
        if (productoId == null) return null;
        // Solo necesitamos el ID. El servicio se encarga de buscarlo (RN4).
        return Producto.builder().id(productoId).build();
    }
}