ALTER TABLE dbo.Pedidos
ADD
    importe_bruto DECIMAL(18,2) NULL,
    descuento_total DECIMAL(18,2) NULL,
    impuesto_total DECIMAL(18,2) NULL,
    importe_total DECIMAL(18,2) NULL;
GO