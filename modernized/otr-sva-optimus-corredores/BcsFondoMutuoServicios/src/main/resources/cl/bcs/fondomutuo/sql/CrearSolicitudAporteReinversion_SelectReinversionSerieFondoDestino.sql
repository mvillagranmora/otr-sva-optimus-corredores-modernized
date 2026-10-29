SELECT 	R."IdReinversionSerieFondo", 
		R."IdSolicitudAporteRescate", 
		R."IdSerieFondoDestino", 
		R."IdRegimenTributario", 
		S."FechaCertificacion" AS "FechaFinalReinversion"
FROM "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" R
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" S 
	ON R."IdSolicitudAporteRescate" = S."IdSolicitudAporteRescateRelacionado"
	AND R."IdSerieFondoDestino" = S."IdSerieFondo"
	AND R."IdRegimenTributario" = S."IdRegimenTributario"
	AND R."FechaFinalReinversion" <> S."FechaCertificacion"
WHERE S."IdSolicitudAporteRescateRelacionado" IN (:IdSolicitudAporteRescate);