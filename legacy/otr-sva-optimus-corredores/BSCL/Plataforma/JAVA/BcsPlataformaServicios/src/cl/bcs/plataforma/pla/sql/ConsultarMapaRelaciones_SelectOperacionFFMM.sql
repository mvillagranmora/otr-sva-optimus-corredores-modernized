SELECT	"IdSolicitudAporteRescate",  
		"TipoSolicitud", 
		"IdMoneda", 
		"IdMonedaSistema", 
		"Monto", 
		"MontoPago", 		
		TO_DATE("FechaOriginalInversion") AS "FechaOriginalInversion",
		TO_DATE("FechaCertificacion") AS "FechaCertificacion",
		TO_DATE("FechaLiquidacion") AS "FechaLiquidacion"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
WHERE "IdSolicitudAporteRescate" IN (:ids)