SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupadoraAPVyNOAPV"; 

CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupadoraAPVyNOAPV" AS 
SELECT "IdMatrizAgrupadorAPV" AS "IdMatrizAgrupador",
       "Codigo" AS "Codigo",
       "EsADC" AS "EsADC",
       "Producto" AS "Producto",
       "NombrePlan" AS "NombrePlan",
       "NombreAgrupador" AS "NombreAgrupador",
       "IdEstado" AS "IdEstado",
	   "IdPlanTipoInversion" AS "IdPlan",
	   "IdProductoAgrupador" AS "IdProducto"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador"
UNION ALL
SELECT "IdMatrizAgrupadorNOAPV" AS "IdMatrizAgrupador",
       "CodigoPortafolio" as "Codigo",
       "EsADC" AS "EsADC",
       'N/A' AS "Producto",
       'N/A' AS "NombrePlan",
       'N/A' AS "NombreAgrupador",
       "IdEstado" AS "IdEstado",
       0 AS "IdPlan",
	   0 AS "IdProducto"
FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV";
 
	
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupadoraAPVyNOAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

