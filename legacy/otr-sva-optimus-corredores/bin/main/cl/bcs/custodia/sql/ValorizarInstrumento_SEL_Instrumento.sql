/********************************************************** 
Proposito	: Script para obtener informacion del instrumento
autor		: NRM
fecha		: 16/05/2017
***********************************************************/

WITH
	ClasificadoraRiesgo AS (
		SELECT  i."IdInstrumento",
				RANK() over (PARTITION BY i."IdInstrumento" ORDER BY CR."Nivel" ASC) AS RANK,
				CR."ClasificacionRiesgo"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" AS i
		INNER JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = i."IdInstrumento" 
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR ON TICR."IdTipoInstrumento" = i."IdTipoInstrumento"
		INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" AS CR ON CR."IdTipoClasificacionRiesgo" = TICR."IdTipoClasificacionRiesgo" 
																		AND CR."ClasificacionRiesgo" = ICR."ValorClasificacionRiesgo" 
																		AND CR."IdEntidadClasificadoraRiesgo" = ICR."IdEntidadClasificadoraRiesgo" 
		WHERE i."IdInstrumento" in (:IdInstrumento)
		GROUP BY i."IdInstrumento",CR."Nivel",CR."ClasificacionRiesgo"
	)
SELECT   i."IdInstrumento"
		,ti."IdClasificacionTipoInstrumento"
		,i."IdTipoInstrumento"
		,i."Nemotecnico"
		,i."ISIN"
		,i."IdEmisor"
		,i."IdTipoReajuste"
		,i."FechaEmision" AS "FechaEmisionInstrumento"
		,i."FechaVencimiento" AS "FechaVencimientoInstrumento"
		,i."IdMoneda"
		,i."CorteMinimo"
		,i."Tamano"
		,i."IndArt104"
		,i."TasaFiscal"
		,i."ValorizaPor"		
		,i."TipoValorizacion" AS "TipoValorizacionInstrumento"
		,i."TieneCupon"
		,i."NumeroCupones"
		,ib."CuponeraVariable"
		,ib."CallConfirmado"
		,ib."TipoCalculoInteresesCupon"
		,ib."TasaEmision"
		,cr."ClasificacionRiesgo" AS "ValorClasificacionRiesgo"
  FROM "BSCL.Plataforma.BD::INS.Instrumento" AS i
  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS ti ON ti."IdTipoInstrumento" = i."IdTipoInstrumento"
  LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoBono" AS ib ON ib."IdInstrumento" = i."IdInstrumento" 
  LEFT JOIN ClasificadoraRiesgo AS cr ON cr."IdInstrumento" = i."IdInstrumento" AND cr.RANK = 1
WHERE i."IdInstrumento" in (:IdInstrumento);