SELECT
	INS."IdInstrumento",
	INS."Nemotecnico" AS "Instrumento",
	INS."IdTipoInstrumento",
	PREC."PrecioCierre" as "Precio",
	INS."IdMoneda" AS "IdMonedaInstrumento",
	COALESCE(RIESGO."ValorClasificacionRiesgo", '') AS "ValorClasificacionRiesgo",
	INS."IdClasificacionTipoInstrumento"
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS RIESGO 
	ON (RIESGO."IdInstrumento" = INS."IdInstrumento" AND RIESGO."IdEntidadClasificadoraRiesgo" = 'BCS')
LEFT JOIN (
	 SELECT ROW_NUMBER() over (partition by "IdInstrumento" order by "Fecha" desc) as row_num, "PrecioCierre", "IdInstrumento" 
	 FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" 
	 WHERE "IdBolsa" = :IdBolsa
  ) AS PREC 
  ON (PREC."IdInstrumento" = INS."IdInstrumento" AND PREC.row_num = 1)
WHERE INS."IdInstrumento" IN (:IdInstrumentos)
	