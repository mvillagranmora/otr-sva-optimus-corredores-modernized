SELECT "Solicitud"."IdCliente","Solicitud"."IdPortafolio","SerieFondo"."IdInstrumento","IdRegimenTributario" 
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Solicitud"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("Solicitud"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = "Solicitud"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "Fondo"."IdEmpresa" = "SerieFondo"."IdEmpresa")
WHERE "Solicitud"."Estado" = 'ING' AND "Solicitud"."CalculoPorMontoCuota" = 'T' AND "Solicitud"."FolioOmnibus" = 0
AND "Solicitud"."IdCliente" IN (:IdCliente) AND "Solicitud"."IdPortafolio" IN (:IdPortafolio) 
AND "SerieFondo"."IdInstrumento" IN (:IdInstrumento);