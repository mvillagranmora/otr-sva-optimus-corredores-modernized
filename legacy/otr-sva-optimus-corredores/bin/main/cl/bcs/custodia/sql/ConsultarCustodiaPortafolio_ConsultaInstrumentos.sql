SELECT
	INS."IdInstrumento",
	TINS."Nombre" AS "NombreTipoInstrumento" ,
	TINS."IdClasificacionTipoInstrumento" ,
	CTINS."Nombre" AS "NombreClasificacionTipoInstrumento",
	INS."Tamano",
	INS."IdEmisor" ,
	INS.ISIN,
	INS."FechaVencimiento" 
FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
WHERE "IdInstrumento" IN (:IdInstrumento)