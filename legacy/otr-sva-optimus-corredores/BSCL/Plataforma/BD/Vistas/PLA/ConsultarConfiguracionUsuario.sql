SET SCHEMA "BSCL";
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionUsuario";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionUsuario"  
AS
SELECT
	"IdConfiguracionUsuario",
	"IdEmpresa",
	"IdUsuario",
	"Llave",
	"Valor"
FROM "BSCL.Plataforma.BD::PLA.ConfiguracionUsuario";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionUsuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


