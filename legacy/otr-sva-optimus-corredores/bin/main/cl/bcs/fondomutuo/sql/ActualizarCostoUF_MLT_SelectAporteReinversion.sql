SELECT "IdSolicitudAporteRescate","FechaCertificacion"
FROM "BSCL.Normativa.BD::SIS.ProcesoMandatos"
WHERE "TipoSolicitud" ='APO' AND "IdAgrupador" in
(
	SELECT "IdAgrupador" FROM "BSCL.Normativa.BD::SIS.ProcesoMandatos"	WHERE "IdSolicitudAporteRescate"
	IN (:IdSolicitudAporteRescate ) and "IdEmpresa"=:IdEmpresa
		
);