SELECT 
	  cev."FolioSistemaExterno"
	, cev."INGCantidad"
	, cev."DocumentoIdentidad"
	, cev."FolioEmprestito"
	, cev."FechaEmprestito"
	, cev."PrecioInstrumento"
	, cev."INGTasaEmprestito"
	, cev."IdCondicionLiquidacion"
	, cev."FechaLiquidacion"
	, cev."FechaVencimientoEmprestito"
	, tp."EsDiaHabilPlazoPrestamo"
	, cev."FolioPrestamo"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" cev 
INNER JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" tp ON ( cev."IdInstrumento" = tp."IdInstrumentoPrestamo" )
WHERE cev."IdEmpresa" = :p_IdEmpresa
	AND cev."FolioSistemaExterno" IN ( :p_FoliosSistemaExterno );