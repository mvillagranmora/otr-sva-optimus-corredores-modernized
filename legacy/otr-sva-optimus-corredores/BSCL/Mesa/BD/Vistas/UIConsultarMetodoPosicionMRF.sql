SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::UIConsultarMetodoPosicionMRF";

CREATE VIEW "BSCL.Mesa.BD.Vistas::UIConsultarMetodoPosicionMRF" AS
SELECT
	 MET."IdMetodo"
	,MET."NombreMetodo"
	,MET."Glosa"  		
	,MET."IdTipoReajuste"
	,MET."IdMesa"
	,COALESCE(MES."Nombre",' -') AS "Mesa"
	,MET."IdEstado"
	,COALESCE(EST."Nombre",' -') AS "Estado"
	,MET."IdTipoPosicion"
FROM "BSCL.Mesa.BD::MES.MetodoPosicion" MET
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" MES
	ON (MET."IdMesa" = MES."IdMesa" AND MES."IdEstadoRegistro" = 'ACT')
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarEstadoMesas" EST
	ON (MET."IdEstado" = EST."IdEstado" AND EST."NombreEntidad" = 'POSICIONMRF')
ORDER BY MET."IdMetodo" ASC;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::UIConsultarMetodoPosicionMRF" TO BCS_DESARROLLADOR WITH GRANT OPTION;
