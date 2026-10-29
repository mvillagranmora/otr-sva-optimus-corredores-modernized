SELECT 	"IdBloqueoSolicitudAporteRescate"
FROM "BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate"
WHERE "IdBloqueoSolicitudAporteRescate" IN (:p_IdBloqueoSolicitudAporteRescate);