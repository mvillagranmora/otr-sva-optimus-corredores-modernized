UPDATE m
	SET m."IdEstado" 	='INA'
	FROM "BSCL.Normativa.BD::SIS.ProcesoMandatos" m
	WHERE m."IdSolicitudAporteRescate"=:IdSolicitudAporteRescate;