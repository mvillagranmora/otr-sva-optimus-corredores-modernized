SELECT 
	"IdSolicitudAporteRescate"
	,"IdRegimenTributario"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
WHERE "IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate);