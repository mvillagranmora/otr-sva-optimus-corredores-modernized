SELECT 
PRECIO_TASA."IdPrecioTasa"
FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" AS PRECIO_TASA
LEFT JOIN "BSCL.Plataforma.BD::TAS.Fuente" AS FUENTE ON FUENTE."IdFuente" = PRECIO_TASA."IdFuente"
WHERE 
PRECIO_TASA."IdEstado" != 'ELI'
AND PRECIO_TASA."Borrado" = 'N'
AND FUENTE."Descripcion" = :Fuente
AND TO_DATE(PRECIO_TASA."FechaSolicitud") = :Fecha
AND PRECIO_TASA."IdTipoTasa" = :TipoRegistro
AND PRECIO_TASA."IdClasificacionTipoInstrumento" = :Mercado
AND PRECIO_TASA."Nemotecnico" IN (:Nemo);