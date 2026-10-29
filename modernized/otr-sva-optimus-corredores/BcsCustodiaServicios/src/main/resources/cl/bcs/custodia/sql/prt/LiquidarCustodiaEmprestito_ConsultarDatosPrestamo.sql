SELECT TOP 1 
	c."IdCartera",
	c."FolioPrestamo",
	c."INGTasaPrestamo" AS "Tasa",
	c."IdSubyacente",
	c."IdInstrumentoSubyacente",
	c."IdMonedaSubyacente",
	c."IdEmisorSubyacente",
	c."IdMonedaEmisionSubyacente",
	c."FechaVencimientoInstrumento",
	c."EsFungibleSubyacente",
	c."IdTipoReajusteSubyacente",
	c."IdInstrumentoPrestamo",
	c."IdBolsa",
	c."IdTipoProducto"
	
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" c
WHERE c."FolioPrestamo" = :p_FolioPrestamo
