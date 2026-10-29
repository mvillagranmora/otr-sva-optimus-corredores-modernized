SELECT
SOL."IdCliente"
,SOL."IdPortafolio"
,SOL."NumeroCuotas" AS "Cantidad"
,SOL."IdRegimenTributario" AS "IdRegimenFondoMutuo"
,COALESCE(REG."GrupoRegimen",'') AS "IdGrupoRegimen"
,SOL."IdSerieFondo"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" REG ON (SOL."IdRegimenTributario" = REG."IdRegimenFondoMutuo")
WHERE SOL."IdCliente" IN (:IdCliente) AND SOL."IdSerieFondo" IN (:IdSerieFondo) AND SOL."Estado" = 'ING'
ORDER BY SOL."IdSolicitudAporteRescate" ASC