SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::ConsultarAPVAnalitica";

CREATE VIEW "BSCL.APV.BD.Vistas::ConsultarAPVAnalitica" AS
SELECT MAT."IdMatrizAgrupadorAPV"
					 ,MAT."IdRegimenTributario"
					 ,PlanTipoInversion."IdPlanTipoInversion"
					 ,PlanTipoInversion."NombrePlanTipoInversion" AS "NombrePlan"
					 ,MAT."IdPropositoPortafolio"
					 ,ProductoAgrupador."IdProductoAgrupador"
					 ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
					 ,MAT."NombreAgrupador"
					 ,MAT."CodigoAgrupador"
					 ,REG."Codigo"
					 ,MAT."IdEstado"
				FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT
				INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS REG
						ON REG."IdRegimenTributario" = MAT."IdRegimenTributario"
				INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador
						ON ProductoAgrupador."IdProductoAgrupador" = MAT."IdProductoAgrupador"
				INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PlanTipoInversion
						ON PlanTipoInversion."IdPlanTipoInversion" = MAT."IdPlanTipoInversion"
				ORDER BY MAT."IdMatrizAgrupadorAPV";
				
GRANT SELECT ON "BSCL.APV.BD.Vistas::ConsultarAPVAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION;