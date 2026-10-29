UPDATE "SOL"
SET "Estado" = 'ING'
FROM	
"BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SOL"
WHERE "SOL"."IdSolicitudAporteRescate"  = :IdSolicitudAporteRescate
AND "SOL"."TipoSolicitud" != 'RES';