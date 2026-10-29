SELECT
"IdCliente"
,MAT."IdPlanTipoInversion"
,PlanTipoInversion."NombrePlanTipoInversion"
,MAT."IdEstado"
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR 
INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT ON MAT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MAT."IdProductoAgrupador"
INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PlanTipoInversion ON PlanTipoInversion."IdPlanTipoInversion" = MAT."IdPlanTipoInversion"
WHERE POR."IdMatrizAgrupadorAPV" <> 0 AND "IdCliente" IN (:p_IdClientes)