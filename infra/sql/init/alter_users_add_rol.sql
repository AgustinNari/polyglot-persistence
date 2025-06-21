USE TPO;
GO
-- Añadir columna rol con valor por defecto 'CLIENTE'
ALTER TABLE dbo.Users
  ADD rol NVARCHAR(20) NOT NULL DEFAULT 'CLIENTE';
GO
-- (Opcional) añadir condicionIVA
ALTER TABLE dbo.Users
  ADD condicionIVA NVARCHAR(50) NULL;
GO
