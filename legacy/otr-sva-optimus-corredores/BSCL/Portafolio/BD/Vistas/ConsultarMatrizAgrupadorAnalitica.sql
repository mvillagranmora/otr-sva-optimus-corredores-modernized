SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupadorAnalitica"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupadorAnalitica" AS 

SELECT MAT."IdMatrizAgrupadorAPV"
     , MAT."IdRegimenTributario"
     , PlanTipoInversion."IdPlanTipoInversion"
     , PlanTipoInversion."NombrePlanTipoInversion" AS "NombrePlan"
     , MAT."IdPropositoPortafolio"
     , ProductoAgrupador."IdProductoAgrupador"
     , ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
     , MAT."NombreAgrupador"
     , MAT."CodigoAgrupador"
     , REG."Codigo"
     , MAT."IdProductoAgrupador" || ' / ' || REG."Codigo"  AS "NombreAgrupadorCodigo"
     , MAT."IdEstado"
     , MAT."EsADC"
FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS REG
        ON REG."IdRegimenTributario" = MAT."IdRegimenTributario"
INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador
        ON ProductoAgrupador."IdProductoAgrupador" = MAT."IdProductoAgrupador"
INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PlanTipoInversion
        ON PlanTipoInversion."IdPlanTipoInversion" = MAT."IdPlanTipoInversion"
WHERE MAT."IdEstado" = 'VIG'    
ORDER BY MAT."IdMatrizAgrupadorAPV";

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" TO  BCS_DESARROLLADOR WITH GRANT OPTION;