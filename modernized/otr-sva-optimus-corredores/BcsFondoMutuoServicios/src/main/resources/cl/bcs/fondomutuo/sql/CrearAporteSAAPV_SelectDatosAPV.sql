SELECT
	 MatrizAgrupadorAPV."IdMatrizAgrupadorAPV"
	,RegimenTributario."IdRegimenTributario"
	,RegimenTributario."Codigo"
	,PlanTipoInversion."NombrePlanTipoInversion"
	,MatrizAgrupadorAPV."CodigoAgrupador"
FROM
	"BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupadorAPV
INNER JOIN
	"BSCL.APV.BD::REC.RegimenTributario" AS RegimenTributario ON RegimenTributario."IdRegimenTributario" = MatrizAgrupadorAPV."IdRegimenTributario"
INNER JOIN
	"BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupadorAPV."IdProductoAgrupador"
INNER JOIN	
	"BSCL.APV.BD::SCS.PlanTipoInversion" AS PlanTipoInversion ON PlanTipoInversion."IdPlanTipoInversion" = MatrizAgrupadorAPV."IdPlanTipoInversion"
WHERE
	RegimenTributario."Codigo" = MAP(:p_RegimenTriburario,'APV-A','APV-A','APV-B','APV-B','DC-R')
AND ProductoAgrupador."NombreProductoAgrupador" = :p_Producto
AND MatrizAgrupadorAPV."IdPlanTipoInversion" = :p_IdPlan;