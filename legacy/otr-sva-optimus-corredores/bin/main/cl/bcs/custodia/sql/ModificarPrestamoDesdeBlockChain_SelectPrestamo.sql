SELECT  rpv."IdCartera"	
	, rpv."IdSubyacente"
	, rpv."FolioPrestamo"
	, rpv."TasaPrestamo"
	, rpv."FechaVencimientoPrestamo"
	, rpv."FolioSistemaExterno"
	, rpv."IdTipoPrestamo"
	, rpv."INGPrecioInstrumento"
	, rpv."INGCantidad"
	, rpv."FechaLiquidacion"
	, rpv."IdCondicionLiquidacion"
	, tp."EsDiaHabilPlazoPrestamo"
	, rpv."INGFechaOperacion"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" rpv
INNER JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" tp ON tp."IdTipoPrestamo" = rpv."IdTipoPrestamo"
WHERE 	rpv."IdEmpresa" = :p_IdEmpresa
	AND rpv."FolioSistemaExterno" IN ( :p_FoliosSistemaExterno );