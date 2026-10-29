SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" AS
SELECT
	 OPE."IdOperacion"
	,OPE."IdTipoProducto"
	,COALESCE(TPR."Nombre",' -') AS "TipoProducto"
	,OPE."IdTipoOperacion"
	,COALESCE(TIO."Nombre",' -') AS "TipoOperacion"
	,OPE."IdTipoComprobante"
	,COALESCE(TCO."Nombre",' -') AS "TipoComprobante"
	,OPE."NombreEvento"
	,OPE."IdTipoEstado"
	,COALESCE(TES."Nombre",' -') AS "TipoEstado"
	,OPE."IdEstado"
	,COALESCE(EST."Nombre",' -') AS "Estado"
FROM "BSCL.Mesa.BD::MES.TipoOperacionPosicion" OPE
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto" AS TPR
	ON (OPE."IdTipoProducto" = TPR."IdTipoProducto" AND TPR."IdEstado" = 'VIG')
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIO
	ON (OPE."IdTipoOperacion" = TIO."IdTipoOperacion" AND OPE."IdTipoProducto" = TIO."IdTipoProducto")
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoComprobante" TCO
	ON (OPE."IdTipoComprobante" = TCO."IdTipoComprobante" AND TCO."IdEstado" = 'VIG')
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoEstado" TES
	ON (OPE."IdTipoEstado" = TES."IdTipoEstado" AND TES."IdEstado" = 'VIG')
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarEstadoMesas" EST
	ON (OPE."IdEstado" = EST."IdEstado" AND EST."NombreEntidad" = 'POSICIONMRF')
ORDER BY OPE."IdOperacion" ASC;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
