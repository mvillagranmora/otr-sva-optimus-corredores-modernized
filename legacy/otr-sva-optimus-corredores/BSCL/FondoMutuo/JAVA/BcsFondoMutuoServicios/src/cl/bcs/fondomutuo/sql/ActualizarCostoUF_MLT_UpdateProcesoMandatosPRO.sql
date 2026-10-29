UPDATE m
	SET m."IdEstado" 	='PRO'
	FROM "BSCL.Normativa.BD::SIS.ProcesoMandatos" m
	WHERE m."IdCartera" =:IdCartera
	AND m."IdSolicitudAporteRescate"=:IdSolicitudAporteRescate
	AND m."TipoSolicitud"=:TipoSolicitud
	AND m."IdPortafolio"=:IdPortafolio
	AND m."IdEmpresa"=:IdEmpresa
	AND m."IdEstado" ='ING';