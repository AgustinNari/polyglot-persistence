
IF COL_LENGTH('dbo.Users','total_minutos_actividad') IS NULL
BEGIN
    ALTER TABLE dbo.Users
    ADD total_minutos_actividad BIGINT NOT NULL DEFAULT 0;
END
