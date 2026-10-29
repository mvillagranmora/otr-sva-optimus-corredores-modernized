SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.APV.BD.Vistas::ConsultarPlanTipoInversion"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::ConsultarPlanTipoInversion" AS 

SELECT "IdPlanTipoInversion"
	 , "NombrePlanTipoInversion"
	 , "IdEstado"
FROM "BSCL.APV.BD::SCS.PlanTipoInversion";

GRANT SELECT ON "BSCL.APV.BD.Vistas::ConsultarPlanTipoInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;