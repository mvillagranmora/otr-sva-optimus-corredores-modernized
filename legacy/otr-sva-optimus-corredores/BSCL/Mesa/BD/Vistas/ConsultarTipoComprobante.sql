SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarTipoComprobante";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarTipoComprobante" AS
SELECT
	 TCO."IdTipoComprobante"
	,TCO."Codigo"
	,TCO."Nombre"  		
	,TCO."IdEstado"
FROM "BSCL.Mesa.BD::MES.TipoComprobante" TCO;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarTipoComprobante" TO BCS_DESARROLLADOR WITH GRANT OPTION;