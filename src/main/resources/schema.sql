-- 1. Eliminación de tablas existentes (para pruebas y reinicio)
DROP TABLE IF EXISTS item_pedido;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS cliente;

-- 2. Creación de la tabla Cliente (Customer)
CREATE TABLE cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    direccion TEXT NOT NULL
);

-- 3. Creación de la tabla Producto (Pizza/Menú)
-- Solo pizzas predefinidas de un solo tamaño.
CREATE TABLE producto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    precio_base DECIMAL(10, 2) NOT NULL CHECK (precio_base > 0),
    -- Campo para aplicar la RN4 (Validación de Disponibilidad)
    disponible BOOLEAN DEFAULT TRUE
);

-- 4. Creación de la tabla Pedido (Order)
-- El total se calculará y actualizará desde el OrderService de Spring Boot (RN1).
CREATE TABLE pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Para aplicar la RN2 (Secuencia de Estados)
    estado ENUM('PENDIENTE', 'EN_PREPARACION', 'LISTO_PARA_ENTREGA', 'ENTREGADO', 'CANCELADO') NOT NULL,

    -- Para definir el tipo de servicio
    tipo_entrega ENUM('DOMICILIO', 'RECOGIDA') NOT NULL,

    -- Para la validación de pago simulada
    metodo_pago ENUM('EFECTIVO', 'TARJETA') NOT NULL,

    -- Dirección de entrega (copia, para evitar cambios si el cliente actualiza su registro)
    direccion_entrega TEXT NOT NULL,

    -- Total final, calculado por el sistema (RN1)
    total DECIMAL(10, 2) DEFAULT 0.00,

    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

-- 5. Creación de la tabla ItemPedido (OrderItem)
-- Tabla de detalle que une Pedido con Producto (N:1)
CREATE TABLE item_pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),

    -- Almacena el precio unitario en el momento del pedido (para auditoría, si el precio cambia después)
    precio_unitario DECIMAL(10, 2) NOT NULL,

    FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    FOREIGN KEY (producto_id) REFERENCES producto(id)
);