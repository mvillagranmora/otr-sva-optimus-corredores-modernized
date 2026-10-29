SELECT 
	"IdMovimientoCarteraEnTransito" 
	,"FechaLiquidacion"
	,"Nemotecnico"     
	,"Cantidad"     
	,"MontoCompra"
	,"IdBolsa"
	,CASE
	 	WHEN "IdTipoReajuste" <> '' THEN "IdTipoReajuste"
	 	ELSE "IdMonedaInstrumento" 
	 END AS "MonedaReajuste"
	,"IdEstado"
	FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
	WHERE "FechaLiquidacion" = :FechaLiquidacion