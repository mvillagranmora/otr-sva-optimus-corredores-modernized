SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventoDetalle"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventoDetalle"  
AS
SELECT 
"IdTipoEventoDetalle",
tipo."CodigoTipoEvento",
"Llave",
	CASE
		WHEN  LENGTH("ValorDefault") > 20 THEN SUBSTRING("ValorDefault",1,20)||'...'
		WHEN  LENGTH("ValorDefault") <= 20 THEN "ValorDefault" 
	END AS "ValorDefault",
"IdConfiguracionEventoDetalle",
"IdConfiguracionEvento",
"IdUsuario",
"FechaCreacion",
"IdEstado"
FROM "BSCL.Plataforma.BD::EVT.TipoEventoDetalle" tipo
LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoDetalles" detalles
ON tipo."CodigoTipoEvento" = detalles."CodigoTipoEvento"
UNION ALL
SELECT 
"IdTagGenerales",
'TagGenerales',
"Llave",
CASE
		WHEN  LENGTH("ValorDefault") > 20 THEN SUBSTRING("ValorDefault",1,20)||'...'
		WHEN  LENGTH("ValorDefault") <= 20 THEN "ValorDefault" 
	END AS "ValorDefault",
0,
0,
0,
CURRENT_UTCTIMESTAMP,
'ACT'
FROM "BSCL.Plataforma.BD::EVT.TagGenerales" ;


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventoDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 
