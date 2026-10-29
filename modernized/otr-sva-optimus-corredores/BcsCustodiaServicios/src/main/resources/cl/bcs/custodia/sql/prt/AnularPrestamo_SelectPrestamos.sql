SELECT DISTINCT
	c."IdCartera",
	c."FolioPrestamo" AS "FolioCartera",
	c."FechaPrestamo" AS "FechaOperacion",
	c."NemotecnicoPrestamo" AS "Nemotecnico",
	c."IdInstrumentoPrestamo" AS "IdInstrumento", 	
	c."IdClasificacionTipoInstrumento",
	c."IdTipoInstrumento", 
	c."TipoInstrumento",
	c."IdEmisor",
	c."IdTipoPrestamo",
	c."IdTipoProducto",
	c."IdMonedaEmisionSubyacente" AS "IdMonedaEmision",
	
	c."IdCliente",
	c."IdPortafolio",
	c."CodigoPortafolio",

	c."INGMontoCompraActualMonedaInstrumento",
	c."INGMontoAPlazoMonedaInstrumento" as "INGMontoAPlazo",
	c."IdMonedaInstrumento",
	
	c."INGCantidad",
	c."INGValorRescate",
	c."INGMontoPrima", 	
	c."INGMontoInstrumento",
	
	c."IdBolsa",
	c."FolioSistemaExterno",
	CAST(0 AS DECIMAL(26,8)) 	AS "PrecioInstrumento",
	c."IdModulo",
	'T0' AS "IdCondicionLiquidacion"

FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" c

WHERE c."IdEmpresa"   = :IdEmpresa
AND   c."FolioPrestamo" IN (:FoliosPrestamos)
AND   c."IdEstado"    = 'VIG';

