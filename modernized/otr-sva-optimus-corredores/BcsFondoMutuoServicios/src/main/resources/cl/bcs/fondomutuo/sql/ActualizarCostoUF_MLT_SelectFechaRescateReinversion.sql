SELECT 
	MAX(sol."FechaCertificacion") AS "FechaCertificacion"
	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"  sol
	WHERE 
			sol."IdSolicitudAporteRescate" in (:IdSolicitudAporteRescate) AND sol."IdEmpresa"=:IdEmpresa