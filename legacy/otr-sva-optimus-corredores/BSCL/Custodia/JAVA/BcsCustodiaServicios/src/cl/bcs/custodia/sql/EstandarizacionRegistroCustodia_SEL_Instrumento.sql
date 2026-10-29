/********************************************************** 
Proposito	: Script para obtener informacion del instrumento
autor		: NRM
fecha		: 16/05/2017
***********************************************************/
SELECT   i."IdInstrumento"
		,ti."IdClasificacionTipoInstrumento"
		,i."IdTipoInstrumento"
		,i."Nemotecnico"
		,i."ISIN"
		,i."IdEmisor"
		,i."IdTipoReajuste"
		,i."FechaEmision" AS "FechaEmisionInstrumento"
		,i."FechaVencimiento" AS "FechaVencimientoInstrumento"
		,i."IdMoneda" AS "IdMonedaInstrumento"
		,i."CorteMinimo"
		,i."Tamano"
		,i."IndArt104"
		,i."TasaFiscal"
		,i."ValorizaPor"
		,i."TipoValorizacion"
		,i."TieneCupon", i.*, ti.*
  FROM "BSCL.Plataforma.BD::INS.Instrumento" AS i
  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS ti ON ti."IdTipoInstrumento" = i."IdTipoInstrumento"
WHERE i."IdInstrumento" in (:IdInstrumento);
