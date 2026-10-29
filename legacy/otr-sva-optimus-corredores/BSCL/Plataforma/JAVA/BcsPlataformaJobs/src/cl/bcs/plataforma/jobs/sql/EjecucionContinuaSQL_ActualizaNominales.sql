UPDATE MOV
SET "MueveCustodia"= CASE "ValorIndicador" WHEN 'S' THEN 'S' WHEN 'N' THEN 'N' END, 
	"EsNominal" = CASE "ValorIndicador" WHEN 'S' THEN 'N' WHEN 'N' THEN 'S' END,
	"FechaLiquidacion" = CASE WHEN "FechaLiquidacion" <= CURRENT_DATE THEN CURRENT_DATE ELSE "FechaLiquidacion" END
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"  MOV
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" IND ON MOV."IdPortafolio" = IND."IdPortafolio" AND IND."CodigoTipoIndicador" IN ('ORDOCUSTRV','ORDOCUSTRF')
AND MOV."IdClasificacionTipoInstrumento" = 
	CASE
		WHEN IND."CodigoTipoIndicador" = 'ORDOCUSTRV' THEN 'RV' 
		WHEN IND."CodigoTipoIndicador" = 'ORDOCUSTRF' AND MOV."IdClasificacionTipoInstrumento" = 'RF' THEN 'RF' 
		WHEN IND."CodigoTipoIndicador" = 'ORDOCUSTRF' AND MOV."IdClasificacionTipoInstrumento" = 'IF' THEN 'IF' 
	ELSE '' END
WHERE "IdEstado" = 'PEN' AND "EsNominal" = 'S' AND "IdClasificacionTipoInstrumento" IN ('RV','RF','IF')--  AND MOV."IdTipoComprobante" != 'MES'
AND (IND."ValorIndicador" = 'S' AND ("EsNominal" = 'S' OR "MueveCustodia" = 'N'))