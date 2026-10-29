SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1849.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1849.ObtenerTotales"
(
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

    -- Filtro fuentes de datos.
    IN p_FuenteDatos NVARCHAR(42) DEFAULT NULL,
	
	-- Filtro Rut Rut Emisor
    IN p_RutEmisor NVARCHAR(40) DEFAULT NULL,
    
    -- Filtro Rut Rut Tenedor
    IN p_RutTenedor NVARCHAR(40) DEFAULT NULL,

    -- Rango del ID cliente.
    IN p_IdCliente_Desde BIGINT DEFAULT NULL,
    IN p_IdCliente_Hasta BIGINT DEFAULT NULL,

    -- Rango del ID portafolio.
    IN p_IdPortafolio_Desde BIGINT DEFAULT NULL,
    IN p_IdPortafolio_Hasta BIGINT DEFAULT NULL,
			
	-- Rango Impuestos
	IN p_InteresDevengado_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_InteresDevengado_Hasta DECIMAL(26,8) DEFAULT NULL,
		
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
    

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 						NVARCHAR(100),
        "COUNT" 					BIGINT,
        "InteresDevengado" 			DECIMAL(26,8)
    )
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Logica de Consulta ----------

	IF x_CodigoValidacion IS NULL THEN
	    p_out_tbl = SELECT
	                    'TOTAL' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("InteresDevengado") AS "InteresDevengado"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1849"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                select
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("InteresDevengado") AS "InteresDevengado"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1849"
	                    WHERE "IdDocumento" = p_IdDocumento
	                    AND ("VboUsuario" LIKE p_VboUsuario or p_VboUsuario IS NULL)
	                    AND ("Observaciones" LIKE p_Observaciones or p_Observaciones IS NULL)
	                    AND ("FuenteDatos" LIKE p_FuenteDatos or p_FuenteDatos IS NULL)
	
	                    AND ("RutEmisor" LIKE p_RutEmisor OR p_RutEmisor IS NULL)
	                    AND ("RutTenedor" LIKE p_RutTenedor OR p_RutTenedor IS NULL)
	                    AND ("IdCliente" >= p_IdCliente_Desde OR p_IdCliente_Desde IS NULL)
	                    AND ("IdCliente" <= p_IdCliente_Hasta OR p_IdCliente_Hasta IS NULL)
	                    AND ("IdPortafolio" >= p_IdPortafolio_Desde OR p_IdPortafolio_Desde IS NULL)
	                    AND ("IdPortafolio" <= p_IdPortafolio_Hasta OR p_IdPortafolio_Hasta IS NULL)
	                    AND ("InteresDevengado" >= p_InteresDevengado_Desde OR p_InteresDevengado_Desde IS NULL)
	                    AND ("InteresDevengado" <= p_InteresDevengado_Hasta OR p_InteresDevengado_Hasta IS NULL);
      ELSE
	    p_out_tbl = SELECT
	                    'TOTAL' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("InteresDevengado") AS "InteresDevengado"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1849"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                SELECT
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("InteresDevengado") AS "InteresDevengado"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1849"
	                    WHERE "IdDocumento" = p_IdDocumento
	                    AND "ID" IN (
	                        SELECT a."IdDetalleDocumento"
	                        FROM "BSCL.Normativa.BD::TRI.DetalleValidacion" a
	                        LEFT JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b
	                        ON a."IdResumenValidacion"=b."ID"
	                        WHERE b."CodigoValidacion"=x_CodigoValidacion
	                    )
	                ;
      END IF;
  END;