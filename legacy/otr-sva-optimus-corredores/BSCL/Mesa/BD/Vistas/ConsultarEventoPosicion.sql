SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" AS
SELECT
	 EVE."IdEvento"
	,EVE."IdOperacion"
	,OPE."NombreEvento" AS "NombreOperacion"
	,EVE."IdTipoEvento"
	,TEV."NombreParametro" AS "TipoEvento"
	,EVE."IndAccion"
	,IAC."NombreParametro" AS "IndicadorAccion"
	,EVE."IdEstado"
	,COALESCE(EST."Nombre",' -') AS "Estado"
FROM "BSCL.Mesa.BD::MES.EventoPosicion" EVE
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" OPE
	ON (EVE."IdOperacion" = OPE."IdOperacion" AND OPE."IdEstado" = 'VIG')
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarParametro" TEV
	ON (EVE."IdTipoEvento" = TEV."ValorParametro" AND TEV."NombreTipoParametro" = 'TIPOEVENTO')
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarParametro" IAC
	ON (EVE."IndAccion" = IAC."ValorParametro" AND IAC."NombreTipoParametro" = 'INDICADORACCION')
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarEstadoMesas" EST
	ON (EVE."IdEstado" = EST."IdEstado" AND EST."NombreEntidad" = 'POSICIONMRF')
ORDER BY EVE."IdEvento" ASC;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
