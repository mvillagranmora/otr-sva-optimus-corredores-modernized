SET SCHEMA "BSCL";
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarTipoCargaConfiguracion";

CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarTipoCargaConfiguracion" 
AS 
SELECT
	"Codigo"
	,"Descripcion"
	,"Formato"
	,"EjemploLinea"
FROM "BSCL.Contabilidad.BD::ITZ.TipoCargaConfiguracion"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarTipoCargaConfiguracion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;		  