SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarConfiguracionMantenedor";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarConfiguracionMantenedor"
AS 

SELECT
	"IdMantenedor",
	"IdTipoDocumento",
	"IdObjeto",
	"ValorActivo"
	
FROM "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarConfiguracionMantenedor" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
