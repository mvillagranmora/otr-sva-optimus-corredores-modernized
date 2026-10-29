SELECT 
	sol."IdSolicitudAporteRescate",sol."FechaCertificacion"
	FROM "BSCL.Custodia.BD::CUS.Cartera" c
	INNER JOIN
	
	(

		SELECT m."TipoSolicitud",m."IdCartera"
		FROM "BSCL.Normativa.BD::SIS.ProcesoMandatos" m
		WHERE 
			m."IdCartera" in (:IdCartera)
			AND m."IdEmpresa"=:IdEmpresa
			AND m."IdEstado" ='ING'
	)MLT	
	ON (MLT."IdCartera"=c."IdCartera" AND MLT."TipoSolicitud"='APO') 
	INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol ON (sol."IdSolicitudAporteRescate"=c."EGRIdOperacion") 
	WHERE c."IdClasificacionTipoInstrumento" = 'FM'  AND c."EGRIdOperacion">0