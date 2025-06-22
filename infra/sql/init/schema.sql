-- 1. Crear la base de datos TPO si no existe

--DROP DATABASE TPO;

IF DB_ID('TPO') IS NULL
BEGIN
    CREATE DATABASE TPO;
END
GO

USE TPO;
GO


IF OBJECT_ID('dbo.Users','U') IS NULL
BEGIN
    CREATE TABLE dbo.Users (
        id INT IDENTITY PRIMARY KEY,
        nombre NVARCHAR(50) NOT NULL,
        apellido NVARCHAR(50) NOT NULL,
        direccion NVARCHAR(200),
        docIdentidad NVARCHAR(100) UNIQUE NOT NULL,
        email NVARCHAR(100),
        contrasena NVARCHAR(100)
    );
END
GO



-- Tabla Pedidos
IF OBJECT_ID('dbo.Pedidos','U') IS NULL
BEGIN
    CREATE TABLE dbo.Pedidos (
        id INT IDENTITY PRIMARY KEY,
        usuario_id INT NOT NULL,
        fecha_creacion DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        estado NVARCHAR(20) NOT NULL,
        CONSTRAINT FK_Pedidos_Users FOREIGN KEY (usuario_id) REFERENCES dbo.Users(id)
    );
END
GO

-- Tabla LineaPedido
IF OBJECT_ID('dbo.LineaPedido','U') IS NULL
BEGIN
    CREATE TABLE dbo.LineaPedido (
        id INT IDENTITY PRIMARY KEY,
        pedido_id INT NOT NULL,
        producto_id NVARCHAR(50) NOT NULL,
        cantidad INT NOT NULL,
        precio_unitario DECIMAL(18,2) NOT NULL,
        descuento DECIMAL(18,2) NULL,
        impuesto DECIMAL(18,2) NULL,
        subtotal DECIMAL(18,2) NOT NULL,
        CONSTRAINT FK_LineaPedido_Pedidos FOREIGN KEY (pedido_id) REFERENCES dbo.Pedidos(id)
        -- opcionalmente: FK a tabla Productos si existiera en SQL,
        -- pero en nuestro caso, productos viven en Mongo, así que no hay FK.
    );
END
GO




IF OBJECT_ID('dbo.Facturas','U') IS NULL
BEGIN
    CREATE TABLE dbo.Facturas (
        id INT IDENTITY PRIMARY KEY,
        pedido_id INT NOT NULL,
        usuario_id INT NOT NULL,
        importe_bruto DECIMAL(18,2) NOT NULL,
        descuento_total DECIMAL(18,2) NOT NULL,
        impuesto_total DECIMAL(18,2) NOT NULL,
        importe_total DECIMAL(18,2) NOT NULL,
        fecha_emision DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        estado NVARCHAR(20) NOT NULL,
        CONSTRAINT FK_Facturas_Pedidos FOREIGN KEY (pedido_id) REFERENCES dbo.Pedidos(id),
        CONSTRAINT FK_Facturas_Users FOREIGN KEY (usuario_id) REFERENCES dbo.Users(id)
    );
END
GO


IF OBJECT_ID('dbo.Pagos','U') IS NULL
BEGIN
    CREATE TABLE dbo.Pagos (
        id INT IDENTITY PRIMARY KEY,
        usuario_id INT NOT NULL,
        monto_total DECIMAL(18,2) NOT NULL,
        fecha_pago DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        medio_pago NVARCHAR(20) NOT NULL,
        operador NVARCHAR(100) NULL,
        CONSTRAINT FK_Pagos_Users FOREIGN KEY (usuario_id) REFERENCES dbo.Users(id)
    );
END
GO

IF OBJECT_ID('dbo.Factura_Pago','U') IS NULL
BEGIN
    CREATE TABLE dbo.Factura_Pago (
        pago_id INT NOT NULL,
        factura_id INT NOT NULL,
        PRIMARY KEY (pago_id, factura_id),
        CONSTRAINT FK_FacturaPago_Pagos FOREIGN KEY (pago_id) REFERENCES dbo.Pagos(id),
        CONSTRAINT FK_FacturaPago_Facturas FOREIGN KEY (factura_id) REFERENCES dbo.Facturas(id)
    );
END
GO



























-- 3. Tabla Orders
IF OBJECT_ID('dbo.Orders', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Orders (
        id INT IDENTITY PRIMARY KEY,
        userId INT NOT NULL,
        fecha DATETIME2 NOT NULL DEFAULT GETDATE(),
        estado NVARCHAR(20) NOT NULL,    -- 'INTENTADO', 'COMPLETADO'
        total DECIMAL(18,2) NOT NULL,
        CONSTRAINT FK_Orders_User FOREIGN KEY (userId) REFERENCES dbo.Users(id)
    );
END
GO




-- 4. Tabla OrderItems
IF OBJECT_ID('dbo.OrderItems', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.OrderItems (
        id INT IDENTITY PRIMARY KEY,
        orderId INT NOT NULL,
        productId NVARCHAR(50) NOT NULL,
        cantidad INT NOT NULL,
        precioUnitario DECIMAL(18,2) NOT NULL,
        descuento DECIMAL(18,2) NOT NULL,
        CONSTRAINT FK_OrderItems_Order FOREIGN KEY (orderId) REFERENCES dbo.Orders(id)
    );
END
GO

-- 5. Tabla Invoices
IF OBJECT_ID('dbo.Invoices', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Invoices (
        id INT IDENTITY PRIMARY KEY,
        orderId INT NOT NULL,
        fechaFactura DATETIME2 NOT NULL DEFAULT GETDATE(),
        impuestos DECIMAL(18,2) NOT NULL,
        descuentos DECIMAL(18,2) NOT NULL,
        estado NVARCHAR(20) NOT NULL,    -- 'PENDIENTE', 'PAGA'
        CONSTRAINT FK_Invoices_Order FOREIGN KEY (orderId) REFERENCES dbo.Orders(id)
    );
END
GO

-- 6. Tabla Payments
IF OBJECT_ID('dbo.Payments', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Payments (
        id INT IDENTITY PRIMARY KEY,
        fechaPago DATETIME2 NOT NULL DEFAULT GETDATE(),
        monto DECIMAL(18,2) NOT NULL,
        metodo NVARCHAR(50) NOT NULL,
        operador NVARCHAR(100) NULL
    );
END
GO

-- 7. Tabla Invoice_Payment
IF OBJECT_ID('dbo.Invoice_Payment', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Invoice_Payment (
        invoiceId INT NOT NULL,
        paymentId INT NOT NULL,
        PRIMARY KEY (invoiceId, paymentId),
        CONSTRAINT FK_InvPay_Invoice FOREIGN KEY (invoiceId) REFERENCES dbo.Invoices(id),
        CONSTRAINT FK_InvPay_Payment FOREIGN KEY (paymentId) REFERENCES dbo.Payments(id)
    );
END
GO
