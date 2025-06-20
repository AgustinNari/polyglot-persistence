-- 1. Crear la base de datos TPO si no existe
IF DB_ID('TPO') IS NULL
BEGIN
    CREATE DATABASE TPO;
END
GO

USE TPO;
GO

-- 2. Tabla Users
IF OBJECT_ID('dbo.Users', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Users (
        id INT IDENTITY PRIMARY KEY,
        nombre NVARCHAR(50) NOT NULL,
        apellido NVARCHAR(50) NOT NULL,
        direccion NVARCHAR(200),
        docIdentidad NVARCHAR(20) UNIQUE
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
