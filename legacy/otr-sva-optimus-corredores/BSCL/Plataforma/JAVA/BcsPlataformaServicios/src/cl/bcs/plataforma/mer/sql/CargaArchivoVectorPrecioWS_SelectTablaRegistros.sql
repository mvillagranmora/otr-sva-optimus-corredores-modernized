SELECT
PRE."Nemotecnico",
PRE."Entidad",
PRE."Fecha",
COALESCE(EVP."ValorRegistro", 0) AS "ValorAntiguo",
PRE."Valor" AS "ValorNuevo"
FROM #PRECIOSING PRE
LEFT JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio" EVP ON (EVP."Nemotecnico" = PRE."Nemotecnico" AND EVP."Fecha" = PRE."Fecha" AND EVP."Entidad" = PRE."Entidad");