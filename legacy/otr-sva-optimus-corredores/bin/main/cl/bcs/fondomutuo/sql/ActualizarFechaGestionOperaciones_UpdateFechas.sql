UPDATE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
SET "FechaAceptacion" = COALESCE(:FechaAceptacion,"FechaAceptacion")  
	,"FechaCertificacion" = COALESCE(:FechaCertificacion,"FechaCertificacion")
	,"FechaLiquidacion" = COALESCE(:FechaLiquidacion,"FechaLiquidacion")
	,"FechaValorCuota" = COALESCE(:FechaValorCuota,"FechaValorCuota")
	,"Modificaciones" = 'S'
WHERE "IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate);