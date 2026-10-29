SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProductoAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProductoAnalitica" AS
SELECT 
	"IdTipoProducto", 
	"Codigo", 
	"Nombre", 
	"IdEstado",
	CASE "IdTipoProducto" 
		WHEN 'INAC' THEN 'AC' 
		WHEN 'INAX' THEN 'AO' 
		WHEN 'INSI' THEN 'AC' 
		WHEN 'INCI' THEN 'CF' 
		WHEN 'INCM' THEN 'CM'
		WHEN 'INCX' THEN 'CO' 
		WHEN 'INEN' THEN 'EN'
		WHEN 'INEX' THEN 'ET'
		WHEN 'INFM' THEN 'FM'
		WHEN 'INFU' THEN 'FU'
		WHEN 'INIF' THEN 'IF'
		WHEN 'INMI' THEN 'ST'
		WHEN 'INMO' THEN 'MO'
		WHEN 'INOS' THEN 'AC'
		WHEN 'INRF' THEN 'RF'
		WHEN 'INSI' THEN 'AC'
		WHEN 'MEFW' THEN 'FW'
		WHEN 'MEME' THEN 'ME'
		WHEN 'MERF' THEN 'RF'
		ELSE 'NA' END AS "Mercado"
FROM "BSCL.Plataforma.BD::INS.TipoProducto";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProductoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
