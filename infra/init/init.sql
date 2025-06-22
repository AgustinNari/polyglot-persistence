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
        email NVARCHAR(100) NULL,
        contrasena NVARCHAR(100) NULL,
        fechaCreacion DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        rol NVARCHAR(20) NOT NULL DEFAULT 'CLIENTE',
        condicionIVA NVARCHAR(50) NULL,
        total_minutos_actividad BIGINT NOT NULL DEFAULT 0
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
        importe_bruto DECIMAL(18,2) NULL,
        descuento_total DECIMAL(18,2) NULL,
        impuesto_total DECIMAL(18,2) NULL,
        importe_total DECIMAL(18,2) NULL,
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