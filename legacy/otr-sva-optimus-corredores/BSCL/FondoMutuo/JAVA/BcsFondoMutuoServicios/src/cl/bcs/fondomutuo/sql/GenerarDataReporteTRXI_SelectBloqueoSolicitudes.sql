SELECT "IdSolicitudAporteRescate"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
WHERE "IdSolicitudAporteRescate" IN (:Solicitudes)
	AND "FolioOmnibus" = 0 
	AND "Estado" = 'ING'
FOR UPDATE;