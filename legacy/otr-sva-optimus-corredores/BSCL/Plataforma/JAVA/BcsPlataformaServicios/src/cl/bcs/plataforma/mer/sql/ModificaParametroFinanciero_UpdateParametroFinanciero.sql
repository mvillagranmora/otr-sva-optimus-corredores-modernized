UPDATE "BSCL.Plataforma.BD::MER.ParametroFinanciero"
SET "Valor" = COALESCE(:Valor,"Valor"),
"FechaActualizacion" = CURRENT_TIMESTAMP
WHERE "Fecha" = :Fecha AND "IdParametroFinanciero" = :IdParametroFinanciero