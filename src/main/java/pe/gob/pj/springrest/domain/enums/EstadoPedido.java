package pe.gob.pj.springrest.domain.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum EstadoPedido {
    // Definición simple de las constantes (sin argumentos para el constructor)
    PENDIENTE,
    EN_PREPARACION,
    LISTO_PARA_ENTREGA,
    ENTREGADO, // Estado final
    CANCELADO; // Estado final

    // Mapa estático final que almacena TODAS las transiciones válidas (la RN2)
    // Se inicializa en el bloque estático, después de que todas las constantes existen.
    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSITIONS;

    /**
     * Bloque de inicialización estático (se ejecuta una sola vez)
     * Aquí definimos el mapa inmutable con todas las reglas de la RN2 de forma segura.
     */
    static {
        Map<EstadoPedido, Set<EstadoPedido>> map = new HashMap<>();

        // RN2: PENDIENTE puede ir a EN_PREPARACION o CANCELADO
        map.put(PENDIENTE, Stream.of(EN_PREPARACION, CANCELADO).collect(Collectors.toUnmodifiableSet()));

        // RN2: EN_PREPARACION puede ir a LISTO_PARA_ENTREGA o CANCELADO
        map.put(EN_PREPARACION, Stream.of(LISTO_PARA_ENTREGA, CANCELADO).collect(Collectors.toUnmodifiableSet()));

        // RN2: LISTO_PARA_ENTREGA solo puede ir a ENTREGADO
        map.put(LISTO_PARA_ENTREGA, Set.of(ENTREGADO));

        // ENTREGADO y CANCELADO no tienen transiciones válidas (no se añaden al mapa o se inicializan a un set vacío)

        // Hacemos el mapa global inmutable para seguridad
        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    /**
     * Valida si la transición al nuevo estado es permitida según la RN2 (Secuencia Estricta).
     * @return true si la transición es válida, false si es prohibida.
     */
    public boolean esTransicionValida(EstadoPedido nuevoEstado) {
        // Busca las transiciones permitidas para el estado actual (this)
        // Si no se encuentra (ej. ENTREGADO), el Set es vacío, y retorna false.
        return TRANSITIONS.getOrDefault(this, Collections.emptySet())
                .contains(nuevoEstado);
    }
}