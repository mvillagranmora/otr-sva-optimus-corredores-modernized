SELECT 
	 CCI."IdEmisor"
	,TO_NVARCHAR (CCI."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion"
	,TO_NVARCHAR (CCI."FechaVencimientoOperacion", 'YYYY-MM-DD') AS "FechaVencimiento"
	,CCI."IdMonedaInstrumento" AS "IdMoneda"
	,CCI."Tasa"
	,CCI."INGMontoInteresAcumTIFMonedaInstrumento" AS "MontoInteres"
	,CCI."Monto"
	,PPP."NumeroCuentaBancaria"
	,CCI."INGMontoATasaCompraActual" AS "ValorPresente"
	,CCI."INGMontoInteresCompraProximo" AS "MontoInteresActual"--"INGMontoInteresCompraProximoMonedaInstrumento"
	,CCI."INGMontoATasaCompraProximo" AS "ValorPresenteT1"
	,CCI."MontoAPlazoMonedaInstrumento"
	,CCI."PlazoEnDiasOperacion"
FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraInstrumento" AS CCI
LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE
ON OPE."IdClienteCorredor" = CCI."IdCliente"
AND OPE."FolioCartera" = CCI."FolioCartera"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" AS PPP
ON OPE."IdPortafolio" = PPP."IdPortafolio"
AND CCI."IdMonedaInstrumento" = PPP."IdMoneda"
AND PPP."IdTipoOperacion" = :IdTipoOperacion
WHERE (:IdInstrumento IS NULL OR CCI."IdInstrumento" = :IdInstrumento)
AND (:IdCliente IS NULL OR CCI."IdCliente" = :IdCliente)
AND CCI."FechaLiquidacion" >= TO_DATE(:FechaDesde, 'YYYY-MM-DD') AND CCI."FechaLiquidacion" <= TO_DATE(:FechaHasta, 'YYYY-MM-DD');