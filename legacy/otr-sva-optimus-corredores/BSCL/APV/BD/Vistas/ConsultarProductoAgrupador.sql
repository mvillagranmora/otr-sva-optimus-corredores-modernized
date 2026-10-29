SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.APV.BD.Vistas::ConsultarProductoAgrupador"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::ConsultarProductoAgrupador" AS 

SELECT "IdProductoAgrupador"
     , ProductoAgrupador."IdPlanTipoInversion"
	 , "NombreProductoAgrupador"
	 , ProductoAgrupador."IdEstado"
FROM "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador
INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PlanTipoInversion
        ON ProductoAgrupador."IdPlanTipoInversion" = PlanTipoInversion."IdPlanTipoInversion"
WHERE ProductoAgrupador."IdEstado" = 'VIG' ;

GRANT SELECT ON "BSCL.APV.BD.Vistas::ConsultarProductoAgrupador" TO  BCS_DESARROLLADOR WITH GRANT OPTION;