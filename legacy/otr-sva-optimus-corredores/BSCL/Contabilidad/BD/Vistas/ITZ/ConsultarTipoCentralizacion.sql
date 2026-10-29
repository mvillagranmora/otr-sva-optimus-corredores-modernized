SET SCHEMA "BSCL";
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarTipoCentralizacion";

CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarTipoCentralizacion" AS
SELECT  "IdTipoCentralizacion","Nombre","Estado"
FROM "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarTipoCentralizacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;