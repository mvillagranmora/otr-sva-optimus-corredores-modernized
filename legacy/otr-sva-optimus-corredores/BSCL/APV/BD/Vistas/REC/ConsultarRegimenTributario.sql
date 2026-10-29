SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario"; 

CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS
SELECT 	MAT."IdMatrizAgrupadorAPV" ,
		MAT."IdRegimenTributario" , 
		PlanTipoInversion."IdPlanTipoInversion" , 
		PlanTipoInversion."NombrePlanTipoInversion" AS "NombrePlan" , 
		SUBSTRING (PlanTipoInversion."NombrePlanTipoInversion",3,LENGTH (PlanTipoInversion."NombrePlanTipoInversion"))  AS "CodigoPlan", 
		MAT."IdPropositoPortafolio" , 
		ProductoAgrupador."IdProductoAgrupador" , 
		ProductoAgrupador."Codigo" AS "CodigoProducto",
		ProductoAgrupador."NombreProductoAgrupador" AS "Producto" , 
		MAT."NombreAgrupador" , 
		MAT."CodigoAgrupador" , 
		REG."Codigo" , 
		MAT."IdProductoAgrupador" || ' / ' || REG."Codigo"  AS "NombreAgrupadorCodigo" , 
		MAT."IdEstado" , 
		REG."CodigoRegimenTipoAhorro" ,
		REG."IdEstado" AS "IdEstadoRegimen",
		REG."EstadoRecaudacion"
FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT 
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS REG 
		ON REG."IdRegimenTributario" = MAT."IdRegimenTributario" 
INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador 
		ON ProductoAgrupador."IdProductoAgrupador" = MAT."IdProductoAgrupador" 
INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PlanTipoInversion 
		ON PlanTipoInversion."IdPlanTipoInversion" = MAT."IdPlanTipoInversion" 
ORDER BY MAT."IdMatrizAgrupadorAPV";


GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 