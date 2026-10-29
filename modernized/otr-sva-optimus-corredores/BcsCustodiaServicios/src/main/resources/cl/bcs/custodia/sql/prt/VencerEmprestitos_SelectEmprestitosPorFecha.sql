SELECT DISTINCT
	c."IdCartera",
	c."FolioEmprestito",
	c."FechaVencimientoEmprestito",
	c."FechaEmprestito",
	c."IdCliente",
	c."IdPortafolio",
	c."IdTipoProducto",
	c."FolioPrestamo",
	c."IdClientePrestamo",
	c."IdPortafolioPrestamo",
	c."FechaPrestamo",
	c."IdBolsa",
	c."IdInstrumentoEmprestito",
	c."IdTipoInstrumento" AS "IdTipoInstrumentoEmprestito",
	c."IdEmisor" AS "IdEmisorEmprestito",
	c."NemoSubyacente",
	c."IdInstrumentoSubyacente",
	c."IdTipoInstrumentoSubyacente",
	c."IdClasificacionTipoInstrumentoSubyacente",
	c."IdTipoReajusteSubyacente",
	c."IdEmisorSubyacente",
	c."IdMonedaSubyacente", 
	c."INGCantidad",
	c."PlazoEnDiasOperacion",
	c."INGTasaEmprestito",
	c."INGValorRescate",
	c."INGMontoInstrumento",
	c."INGMontoTotalEmprestito",
	c."INGMontoPremio",
	IFNULL(ATR."ValorIndicador",'N') AS "ConCustodiaPrestamista",
	IFNULL(ATR2."ValorIndicador",'N') AS "ConCustodiaPrestatario",
	COALESCE(ip1."ValorIndicador",'N') AS "OperaContraCuentaInversionPrestamista",
	COALESCE(ip2."ValorIndicador",'N') AS "OperaContraCuentaInversionPrestatario",
	c."PrecioInstrumento",
	
	CAST(0 AS BIGINT) 			AS "PlazoAlVencimiento",
	CAST(0 AS BIGINT) 			AS "EmpleadoDigitador",
	c."IdTipoPrestamoPrestamo" AS "IdTipoPrestamo",
	c."FechaLiquidacion",
	c."INGTasaSubyacente" AS "INGTasaInstrumento"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" c

LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO ON TPO."CodigoTipoIndicador" = 'CC'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON ATR."IdTipoIndicador" = TPO."IdTipoIndicador" AND ATR."IdTipoEntidad" = 'CLIENTE' AND ATR."IdEntidad" = TO_VARCHAR(c."IdCliente")

LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO2 ON TPO2."CodigoTipoIndicador" = 'CC'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR2 ON ATR2."IdTipoIndicador" = TPO2."IdTipoIndicador" AND ATR2."IdTipoEntidad" = 'CLIENTE' AND ATR2."IdEntidad" = TO_VARCHAR(c."IdClientePrestamo")

LEFT JOIN  "BSCL.Custodia.BD::PRT.TipoPrestamo" AS TipPre ON (TipPre."IdTipoPrestamo" = c."IdTipoPrestamo")

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" ip1 on ip1."IdPortafolio" = c."IdPortafolio" and ip1."CodigoTipoIndicador" = 'GEN_OCCINV'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" ip2 on ip2."IdPortafolio" = c."IdPortafolioPrestamo" and ip2."CodigoTipoIndicador" = 'GEN_OCCINV'

WHERE c."IdEmpresa" = :p_IdEmpresa AND c."FechaVencimientoEmprestito" = (:FechaVencimiento)
