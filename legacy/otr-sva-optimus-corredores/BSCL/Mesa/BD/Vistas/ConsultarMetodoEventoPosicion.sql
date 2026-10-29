SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" AS
SELECT
	 MEP."IdMatriz"
	,MEP."IdMetodo"
	,MET."NombreMetodo"
	,MEP."IdEvento"
	,EVE."NombreOperacion" AS "NombreEvento"
	,MEP."IdAgrupacion"
	,AGR."NombreParametro" AS "Agrupacion"
	,MEP."IdEstado"
	,COALESCE(EST."Nombre",' -') AS "Estado"
	,MEP."OrdenItem"
FROM "BSCL.Mesa.BD::MES.MetodoEventoPosicion" MEP
LEFT JOIN "BSCL.Mesa.BD.Vistas::MRF.ConsultarMetodosPosicionMRF" MET
	ON (MEP."IdMetodo" = MET."IdMetodo")
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" EVE
	ON (MEP."IdEvento" = EVE."IdEvento")
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarParametro" AGR
	ON (MEP."IdAgrupacion" = AGR."ValorParametro" AND AGR."NombreTipoParametro" = 'Grupo_PosMRF')
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarEstadoMesas" EST
	ON (MEP."IdEstado" = EST."IdEstado" AND EST."NombreEntidad" = 'POSICIONMRF')
ORDER BY MEP."IdMatriz" ASC
;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
