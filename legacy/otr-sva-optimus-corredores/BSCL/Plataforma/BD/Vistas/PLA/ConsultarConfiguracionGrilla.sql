SET SCHEMA "BSCL";
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionGrilla";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionGrilla"  
AS
SELECT
	"IdConfiguracionGrilla",
	"IdEmpresa",
	"Llave",
	"Valor"
FROM "BSCL.Plataforma.BD::PLA.ConfiguracionGrilla";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionGrilla" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


