SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCategoriaPerfilFondoMutuo"; 
 
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCategoriaPerfilFondoMutuo"
AS

SELECT 
	CF."IdCategoriaPerfilFondoMutuo",
	CF."IdCategoriaInversionista",
	CF."NombreCategoriaPerfilFondoMutuo",
	CF."IdEmpresa",
	CF."Jerarquia"
FROM 
	"BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo" as CF
ORDER BY CF."Jerarquia" DESC;

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCategoriaPerfilFondoMutuo" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
