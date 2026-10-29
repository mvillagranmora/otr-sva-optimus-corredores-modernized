SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista"
AS

SELECT 
	CI."IdCategoriaInversionista",
	CI."NombreCategoriaInversionista",
	CI."IdEmpresa"
FROM 
	"BSCL.Plataforma.BD::COM.CategoriaInversionista" as CI;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
