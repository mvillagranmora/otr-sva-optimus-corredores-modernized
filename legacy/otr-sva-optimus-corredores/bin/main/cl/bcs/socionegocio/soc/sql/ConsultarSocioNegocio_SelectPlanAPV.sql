SELECT PlanTipoInversion.* FROM
(
SELECT TOP 1 * FROM "BSCL.Portafolio.BD::POR.Portafolio" WHERE :p_IdCliente = "IdCliente" AND "IdMatrizAgrupadorAPV" <> 0
) AS POR
INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT
		ON MAT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador
		ON ProductoAgrupador."IdProductoAgrupador" = MAT."IdProductoAgrupador"
INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PlanTipoInversion
		ON PlanTipoInversion."IdPlanTipoInversion" = MAT."IdPlanTipoInversion"