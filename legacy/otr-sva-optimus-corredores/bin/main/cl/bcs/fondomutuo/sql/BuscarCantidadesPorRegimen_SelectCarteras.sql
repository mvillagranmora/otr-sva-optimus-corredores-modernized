SELECT
CAR."IdCliente"
,CAR."IdPortafolio"
,CAR."INGCantidad" AS "Cantidad"
,CAR."IdRegimenFondoMutuo"
,COALESCE(REG."GrupoRegimen",'') AS "IdGrupoRegimen"
,CAR."Nemotecnico" AS "IdSerieFondo"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" REG ON (CAR."IdRegimenFondoMutuo" = REG."IdRegimenFondoMutuo")
WHERE CAR."IdCliente" IN (:IdCliente) AND CAR."Nemotecnico" IN (:IdSerieFondo) AND CAR."IdEstado" = 'VIG'
ORDER BY "FechaLiquidacion" ASC,"FolioCartera" ASC