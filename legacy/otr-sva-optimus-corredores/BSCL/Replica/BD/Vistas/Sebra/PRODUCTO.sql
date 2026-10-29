SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.PRODUCTO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.PRODUCTO" AS

	SELECT DISTINCT 
	TP."IdTipoProducto"
	--,H1."ValorDestino", TIOPTINS."IdTipoInstrumento", TIOP."IdTipoOperacion"
			,CASE WHEN COALESCE(H."ValorOrigen", TP."IdTipoProducto") IN ('CUIF','CURF','CURV','CUST','GARA') THEN 'NA' ELSE H."ValorOrigen2" END AS MERCADO
			,COALESCE(H."ValorOrigen", TP."IdTipoProducto") AS PRO_ID
			,LEFT(COALESCE(H."Descripcion", TP."Nombre"),30) AS PRO_NOMBRE
			,CASE WHEN UPPER(H."Descripcion") LIKE '%MESA%' OR UPPER(TP."Nombre") LIKE '%MESA%' THEN 'S' ELSE 'N' END AS PRO_MESA
			,'S' AS PRO_IND_HABILITADO
			,'' AS PRO_ID_NCG380 
	FROM "BSCL.Plataforma.BD::INS.TipoProducto" TP
	LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" H ON H."TipoValor" = 'TipoProducto' AND H."Descripcion" <> '' AND H."ValorDestino" = TP."IdTipoProducto"
	WHERE H."IdHomologacion" IS NOT NULL


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.PRODUCTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
