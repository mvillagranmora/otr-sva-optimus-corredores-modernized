SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1889.ObtenerTotales";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1889.ObtenerTotales"
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
    
    -- Filtro Rut
    IN p_RutCliente NVARCHAR(40) DEFAULT NULL,
       
    -- Rango del ID cliente.
    IN p_IdCliente_Desde BIGINT DEFAULT NULL,
    IN p_IdCliente_Hasta BIGINT DEFAULT NULL,

    IN p_MontoNominal_Desde DECIMAL(26,8) DEFAULT NULL,
    IN p_MontoNominal_Hasta DECIMAL(26,8) DEFAULT NULL,
   
    -- RENTABILIDAD NOMINAL ANUAL DETERMINADA SOBRE LOS RETIROS
    IN p_RentabilidadPositiva_Desde DECIMAL(26,8) DEFAULT NULL,
    IN p_RentabilidadPositiva_Hasta DECIMAL(26,8) DEFAULT NULL,
    
    IN p_RentabilidadNegativa_Desde DECIMAL(26,8) DEFAULT NULL,
    IN p_RentabilidadNegativa_Hasta DECIMAL(26,8) DEFAULT NULL,
    
    --  RENTABILIDAD ANUAL ACTUALIZADA ($)
    IN p_RentabilidadActualizadaPositiva_Desde DECIMAL(26,8) DEFAULT NULL,
    IN p_RentabilidadActualizadaPositiva_Hasta DECIMAL(26,8) DEFAULT NULL,
   
    IN p_RentabilidadActualizadaNegativa_Desde DECIMAL(26,8) DEFAULT NULL,
    IN p_RentabilidadActualizadaNegativa_Hasta DECIMAL(26,8) DEFAULT NULL,
    
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE"                              NVARCHAR(100),
        "COUNT"                             BIGINT,
        "MontoNominal"                      decimal(26,8),
        -- RENTABILIDAD NOMINAL ANUAL DETERMINADA SOBRE LOS RETIROS
        "RentabilidadPositiva"              decimal(26,8),
        "RentabilidadNegativa"              decimal(26,8),
        --  RENTABILIDAD ANUAL ACTUALIZADA ($)
        "RentabilidadActualizadaPositiva"   decimal(26,8),
        "RentabilidadActualizadaNegativa"   decimal(26,8))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Logica de Consulta ----------

    IF (:x_CodigoValidacion IS NULL) THEN
        p_out_tbl =
            SELECT
                'TOTAL' AS "TYPE",
                COUNT(1) AS "COUNT",
                SUM("MontoNominal") AS "MontoNominal",
                SUM("RentabilidadPositiva") AS "RentabilidadPositiva",
                SUM("RentabilidadNegativa") AS "RentabilidadNegativa",
                SUM("RentabilidadActualizadaPositiva") AS "RentabilidadActualizadaPositiva",
                SUM("RentabilidadActualizadaNegativa") AS "RentabilidadActualizadaNegativa"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1889"
            WHERE "IdDocumento" = :p_IdDocumento
            UNION ALL
            SELECT
                'FILTRO' AS "TYPE",
                COUNT(1) AS "COUNT",
                SUM("MontoNominal") AS "MontoNominal",
                SUM("RentabilidadPositiva") AS "RentabilidadPositiva",
                SUM("RentabilidadNegativa") AS "RentabilidadNegativa",
                SUM("RentabilidadActualizadaPositiva") AS "RentabilidadActualizadaPositiva",
                SUM("RentabilidadActualizadaNegativa") AS "RentabilidadActualizadaNegativa"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1889"
            WHERE "IdDocumento" = :p_IdDocumento
            AND ("VboUsuario" LIKE :p_VboUsuario or :p_VboUsuario IS NULL)
            AND ("Observaciones" LIKE :p_Observaciones or :p_Observaciones IS NULL)
            AND ("FuenteDatos" LIKE :p_FuenteDatos or :p_FuenteDatos IS NULL)
            AND ("RutCliente" LIKE :p_RutCliente or :p_RutCliente is null)
            AND ("IdCliente" >= :p_IdCliente_Desde OR :p_IdCliente_Desde IS NULL)
            AND ("IdCliente" >= :p_IdCliente_Hasta OR :p_IdCliente_Hasta IS NULL)
            AND ("MontoNominal" >= :p_MontoNominal_Desde OR :p_MontoNominal_Desde IS NULL)
            AND ("MontoNominal" <= :p_MontoNominal_Hasta OR :p_MontoNominal_Hasta IS NULL)
            AND ("RentabilidadPositiva" >= :p_RentabilidadPositiva_Desde OR :p_RentabilidadPositiva_Desde IS NULL)
            AND ("RentabilidadPositiva" <= :p_RentabilidadPositiva_Hasta OR :p_RentabilidadPositiva_Hasta IS NULL)
            AND ("RentabilidadNegativa" >= :p_RentabilidadNegativa_Desde OR :p_RentabilidadNegativa_Desde IS NULL)
            AND ("RentabilidadNegativa" <= :p_RentabilidadNegativa_Hasta OR :p_RentabilidadNegativa_Hasta IS NULL)
            AND ("RentabilidadActualizadaPositiva" >= :p_RentabilidadActualizadaPositiva_Desde OR :p_RentabilidadActualizadaPositiva_Desde IS NULL)
            AND ("RentabilidadActualizadaPositiva" <= :p_RentabilidadActualizadaPositiva_Hasta OR :p_RentabilidadActualizadaPositiva_Hasta IS NULL)
            AND ("RentabilidadActualizadaNegativa" >= :p_RentabilidadActualizadaNegativa_Desde OR :p_RentabilidadActualizadaNegativa_Desde IS NULL)
            AND ("RentabilidadActualizadaNegativa" <= :p_RentabilidadActualizadaNegativa_Hasta OR :p_RentabilidadActualizadaNegativa_Hasta IS NULL);
    ELSE
        p_out_tbl =
            SELECT
                'TOTAL' AS "TYPE",
                COUNT(1) AS "COUNT",
                SUM("MontoNominal") AS "MontoNominal",
                SUM("RentabilidadPositiva") AS "RentabilidadPositiva",
                SUM("RentabilidadNegativa") AS "RentabilidadNegativa",
                SUM("RentabilidadActualizadaPositiva") AS "RentabilidadActualizadaPositiva",
                SUM("RentabilidadActualizadaNegativa") AS "RentabilidadActualizadaNegativa"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1889"
            WHERE "IdDocumento" = :p_IdDocumento
            UNION ALL
            SELECT
                'FILTRO' AS "TYPE",
                COUNT(1) AS "COUNT",
                SUM("MontoNominal") AS "MontoNominal",
                SUM("RentabilidadPositiva") AS "RentabilidadPositiva",
                SUM("RentabilidadNegativa") AS "RentabilidadNegativa",
                SUM("RentabilidadActualizadaPositiva") AS "RentabilidadActualizadaPositiva",
                SUM("RentabilidadActualizadaNegativa") AS "RentabilidadActualizadaNegativa"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1889"
            WHERE "IdDocumento" = :p_IdDocumento
            AND "ID" IN (
                SELECT
                    a."IdDetalleDocumento"
                FROM "BSCL.Normativa.BD::TRI.DetalleValidacion" a
                LEFT JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b ON (b."ID" = a."IdResumenValidacion")
                WHERE b."CodigoValidacion" = :x_CodigoValidacion);
      END IF;
  END;
  GO