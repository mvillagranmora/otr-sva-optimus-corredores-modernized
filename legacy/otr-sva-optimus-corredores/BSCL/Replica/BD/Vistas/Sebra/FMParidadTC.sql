SET SCHEMA "BSCL";
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.FMParidadTC";

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.FMParidadTC" AS

SELECT Fecha,CodMoneda,ParidadMercado,ParidadSBIF,TCMercado,UsuarioUltMod,FecUltMod,PrecioCompra,PrecioVenta,PrecioPromedio FROM  (
	SELECT
		RANK() OVER (PARTITION BY PAR."Fecha", MON."CodigoNumerico" ORDER BY PAR."TipoCambioMercado" DESC, PAR."IdParidadYTipoCambio") AS RANK,
		TO_TIMESTAMP (CASE WHEN PAR."Fecha" < '1900-01-01' THEN '1900-01-01 00:00:00' ELSE PAR."Fecha" END ,'YYYY-MM-DD HH24:MI:SS')  AS Fecha, --DATETIME
		COALESCE (MON."CodigoNumerico", 0) AS CodMoneda, --INT
		COALESCE (PAR."ParidadMercado" , 0) AS ParidadMercado, --DECIMAL
		COALESCE (PAR."ParidadSBIF", 0) AS ParidadSBIF, --DECIMAL
		COALESCE (PAR."TipoCambioMercado", 0) AS TCMercado, --DECIMAL
		CAST(COALESCE(USU."CodigoUsuario", '') AS VARCHAR(15)) AS UsuarioUltMod,
		TO_TIMESTAMP("Fecha", 'YYYY-MM-DD HH24:MI:SS') AS FecUltMod, --DATETIME
		--COALESCE (MER."FechaUltimaModificacion", '') AS "FecUltMod", --DATETIME
		COALESCE (PAR."PrecioSpotCompra", 0) AS PrecioCompra, --DECIMAL
		COALESCE (PAR."PrecioSpotVenta", 0) AS PrecioVenta, --DECIMAL
		COALESCE (PAR."PrecioSpotPromedio", 0) AS PrecioPromedio --DECIMAL
	
	FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PAR
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON PAR."IdMoneda" = MON."IdMoneda"
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON PAR."UsuarioModificador" = USU."IdUsuario"
) WHERE RANK = 1

WITH READ ONLY;
GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.FMParidadTC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
