SELECT 
DES."IdSolicitudAporteRescate"
,DES."IdSerieFondoDestino"
,DES."Porcentaje"
,DES."IdRegimenTributario"
FROM "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" DES
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL ON SOL."IdSolicitudAporteRescate" = DES."IdSolicitudAporteRescate"
WHERE SOL."FechaCertificacion" BETWEEN :FechaDesde AND :FechaHasta
AND SOL."Estado" = 'CRT'
AND SOL."TipoSolicitud" = 'RES'
--AND SOL."FolioContrato" = 786429
