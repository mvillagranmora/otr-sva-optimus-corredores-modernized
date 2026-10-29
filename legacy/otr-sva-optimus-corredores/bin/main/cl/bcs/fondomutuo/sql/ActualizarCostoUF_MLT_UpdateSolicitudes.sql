	UPDATE SOL
	SET SOL."CostoUF" = MLT."PRO_EGRCostoInversionEnFactorAjuste"
	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
	INNER JOIN
	
	(
		SELECT m."IdSolicitudAporteRescate",sum(m."PRO_EGRCostoInversionEnFactorAjuste")AS "PRO_EGRCostoInversionEnFactorAjuste"
		FROM "BSCL.Normativa.BD::SIS.ProcesoMandatos" m
		WHERE 
			--m."IdCartera" =:IdCartera
			 m."IdSolicitudAporteRescate"=:IdSolicitudAporteRescate
			AND m."TipoSolicitud"=:TipoSolicitud
			AND m."IdPortafolio"=:IdPortafolio
			AND m."IdEmpresa"=:IdEmpresa
			AND m."IdEstado" ='ING'
		GROUP BY m."IdSolicitudAporteRescate"
	)MLT	
	ON (MLT."IdSolicitudAporteRescate"=SOL."IdSolicitudAporteRescate");