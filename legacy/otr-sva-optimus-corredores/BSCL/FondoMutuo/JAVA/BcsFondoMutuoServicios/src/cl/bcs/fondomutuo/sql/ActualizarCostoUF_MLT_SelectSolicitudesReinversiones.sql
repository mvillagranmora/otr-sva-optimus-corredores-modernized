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
AND SOL."IdPortafolio" IN (:Portafolio)
AND NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::SIS.ProcesoMandatos" m
				WHERE  M."IdSolicitudAporteRescate"	= DES."IdSolicitudAporteRescate"
				  AND M."IdEstado" ='PRO')
				  
--AND SOL."FolioContrato" = 786429