USE TPO;
GO

-- 1. Agregar columna fechaCreacion si no existe
IF COL_LENGTH('dbo.Users', 'fechaCreacion') IS NULL
BEGIN
    ALTER TABLE dbo.Users
    ADD fechaCreacion DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME();
END
GO
