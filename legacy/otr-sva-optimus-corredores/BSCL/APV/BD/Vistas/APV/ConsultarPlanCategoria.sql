SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::APV.ConsultarPlanCategoria"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::APV.ConsultarPlanCategoria"
AS

SELECT
	CI."IdCategoriaInversionista",
	CI."NombreCategoriaInversionista",
	CI."IdEmpresa",
	PC."IdPlan"
FROM 
	"BSCL.APV.BD::APV.PlanCategoria" as PC
	
LEFT JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" AS CI ON (CI."IdCategoriaInversionista" = PC."IdCategoria");

GRANT SELECT ON "BSCL.APV.BD.Vistas::APV.ConsultarPlanCategoria" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
