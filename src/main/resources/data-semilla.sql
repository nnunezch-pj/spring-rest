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

-- ************************************************************
-- 6. INSERCIÓN DE DATOS INICIALES (INSERTS)
-- ************************************************************

-- Datos iniciales de Productos
INSERT INTO producto (nombre, descripcion, precio_base, disponible) VALUES
('Pepperoni Clásica', 'Salsa de tomate, mozzarella, y rodajas de pepperoni.', 15.50, TRUE),
('Hawaiana Tropical', 'Jamón, piña, y extra de queso. Una favorita polémica.', 14.00, TRUE),
('Margarita', 'La clásica: salsa de tomate, mozzarella fresca y albahaca.', 12.00, TRUE),
('Cuatro Quesos', 'Combinación de mozzarella, gouda, parmesano y cheddar.', 16.75, FALSE), -- Marcada como agotada para probar la RN4
('Vegetariana', 'Una mezcla de pimientos, cebolla, champiñones y aceitunas.', 15.00, TRUE);

-- Datos iniciales de Clientes
INSERT INTO cliente (nombre, email, telefono, direccion) VALUES
('Carlos Gómez', 'carlos.gomez@mail.com', '987654321', 'Calle Falsa 123, Urb. San Martín'),
('Ana Torres', 'ana.torres@mail.com', '912345678', 'Av. Primavera 456, Santiago de Surco'),
('Luisa Pérez', 'luisa.perez@mail.com', '999888777', 'Jr. Los Álamos 789, Miraflores');

-- EJEMPLOS DE PEDIDOS (Se recomienda crear pedidos con la aplicación, pero esto es para ilustrar las relaciones)

-- Ejemplo 1: Pedido de Carlos Gómez (ID Cliente 1) - PENDIENTE
INSERT INTO pedido (cliente_id, estado, tipo_entrega, metodo_pago, direccion_entrega) VALUES
(1, 'PENDIENTE', 'DOMICILIO', 'EFECTIVO', 'Calle Falsa 123, Urb. San Martín');
SET @pedido1_id = LAST_INSERT_ID();

-- Ítems para el Pedido 1
-- 2 x Pepperoni Clásica (ID Producto 1, Precio 15.50)
INSERT INTO item_pedido (pedido_id, producto_id, cantidad, precio_unitario) VALUES
(@pedido1_id, 1, 2, 15.50);
-- 1 x Margarita (ID Producto 3, Precio 12.00)
INSERT INTO item_pedido (pedido_id, producto_id, cantidad, precio_unitario) VALUES
(@pedido1_id, 3, 1, 12.00);
-- El total (43.00) debe ser calculado y actualizado por el OrderService (RN1)

-- Ejemplo 2: Pedido de Ana Torres (ID Cliente 2) - EN_PREPARACION
INSERT INTO pedido (cliente_id, estado, tipo_entrega, metodo_pago, direccion_entrega) VALUES
(2, 'EN_PREPARACION', 'RECOGIDA', 'TARJETA', 'Av. Primavera 456, Santiago de Surco');
SET @pedido2_id = LAST_INSERT_ID();

-- Ítems para el Pedido 2
-- 1 x Hawaiana Tropical (ID Producto 2, Precio 14.00)
INSERT INTO item_pedido (pedido_id, producto_id, cantidad, precio_unitario) VALUES
(@pedido2_id, 2, 1, 14.00);
-- 1 x Vegetariana (ID Producto 5, Precio 15.00)
INSERT INTO item_pedido (pedido_id, producto_id, cantidad, precio_unitario) VALUES
(@pedido2_id, 5, 1, 15.00);
-- El total (29.00) debe ser calculado y actualizado por el OrderService (RN1)

-- ************************************************************
-- Consultas de Verificación
-- ************************************************************
-- SELECT * FROM cliente;
-- SELECT * FROM producto;
-- SELECT * FROM pedido;
-- SELECT * FROM item_pedido;