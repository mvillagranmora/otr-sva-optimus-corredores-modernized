SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1.ObtenerTotales"(
	------------ Parametros de Entrada Comunes ----------
	-- ID empresa del login.
	IN p_IdEmpresa BIGINT,
	-- ID usuario del login.
	IN p_IdUsuario BIGINT,
	-- ID modulo invocador.
	IN p_IdModulo NVARCHAR(3),
	------------ Parametros de Entrada Propios ----------
	-- ID del documento.
	IN p_IdDocumento BIGINT,
	-- Filtro VoBo del usuario.
	IN p_VboUsuario NVARCHAR(3) DEFAULT NULL,
	-- Filtro observaciones.
	IN p_Observaciones NVARCHAR(252) DEFAULT NULL,

	IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
	------------ Salidas ----------
	-- Tabla de totales.
	-- Debe incluir las columnas sumables de la tabla de detalle.
	OUT p_out_tbl TABLE ("TYPE" NVARCHAR(100), "COUNT" BIGINT, "S3_MontoComprada" BIGINT)
 )
 LANGUAGE sqlscript
 SQL security invoker
 DEFAULT SCHEMA "BSCL"
 AS
 BEGIN
	 ------------ Logica de Consulta ----------
	IF x_CodigoValidacion IS NULL THEN
		p_out_tbl = SELECT 'TOTAL' AS "TYPE", COUNT(*) AS "COUNT", 0 AS "S3_MontoComprada" 
					FROM DUMMY UNION ALL
					SELECT'FILTRO' AS "TYPE", COUNT(*) AS "COUNT", SUM("S3_MontoComprada") AS "S3_MontoComprada"
					FROM "BSCL.Normativa.BD::TRI.DatosDJ1";
	ELSE
		p_out_tbl = SELECT'TOTAL' AS "TYPE", COUNT(*) AS "COUNT", 0 AS "S3_MontoComprada"
					FROM DUMMY UNION ALL
					SELECT 'FILTRO' AS "TYPE",COUNT(*) AS "COUNT", SUM("S3_MontoComprada") AS "S3_MontoComprada"
					FROM "BSCL.Normativa.BD::TRI.DatosDJ1";
	END IF;
END;