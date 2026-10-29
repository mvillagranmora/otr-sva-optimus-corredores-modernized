SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarTipoComprobante";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarTipoComprobante" AS
 SELECT  SUBSTRING("IdTipoMovimiento", 4, 3) as "IdTipoComprobante"
		,"Nombre"
		,"Tipo"
		,"IdEstado"
   FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento"
  WHERE "IdModulo" = 'TES'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarTipoComprobante" TO  BCS_DESARROLLADOR WITH GRANT OPTION;