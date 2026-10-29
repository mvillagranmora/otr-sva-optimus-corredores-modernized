SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1871.ObtenerTotales";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1871.ObtenerTotales"
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
    
    -- Filtro Rut Depositante, Inversionista o Prestamista
    IN p_RutCliente NVARCHAR(40) DEFAULT NULL,
       
    -- Rango del ID cliente.
    IN p_IdCliente_Desde BIGINT DEFAULT NULL,
    IN p_IdCliente_Hasta BIGINT DEFAULT NULL,

    -- Rango Ahorro Previsional Voluntario Colectivo (Ahorro Trabajador)
    IN p_AhorrosTrabajador_Desde DECIMAL(26,8) DEFAULT NULL,
    IN p_AhorrosTrabajador_Hasta DECIMAL(26,8) DEFAULT NULL,
   
    -- Rango Deposito Ahorro Previsional Voluntario
    IN p_AhorrosDeposito_Desde DECIMAL(26,8) DEFAULT NULL,
    IN p_AhorrosDeposito_Hasta DECIMAL(26,8) DEFAULT NULL,
    
    -- Rango Cotizciones Voluntarias
    IN p_AhorrosCotizaciones_Desde DECIMAL(26,8) DEFAULT NULL,
    IN p_AhorrosCotizaciones_Hasta DECIMAL(26,8) DEFAULT NULL,
    
    -- Rango Ahorro Previsional Voluntario Colectivo (Ahorro Trabajador)
    IN p_RetirosTrabajador_Desde DECIMAL(26,8) DEFAULT NULL,
    IN p_RetirosTrabajador_Hasta DECIMAL(26,8) DEFAULT NULL,
   
    -- Rango Deposito Ahorro Previsional Voluntario
    IN p_RetirosDeposito_Desde DECIMAL(26,8) DEFAULT NULL,
    IN p_RetirosDeposito_Hasta DECIMAL(26,8) DEFAULT NULL,
    
    -- Rango Cotizciones Voluntarias
    IN p_RetirosCotizaciones_Desde DECIMAL(26,8) DEFAULT NULL,
    IN p_RetirosCotizaciones_Hasta DECIMAL(26,8) DEFAULT NULL,
    
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
        

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE"                              NVARCHAR(100),
        "COUNT"                             BIGINT,
        --Ahorros
        "AhorrosTrabajador"                 decimal(26,8),
        "AhorrosDeposito"                   decimal(26,8),
        "AhorrosCotizaciones"               decimal(26,8),
        --Retiros
        "RetirosTrabajador"                 decimal(26,8),
        "RetirosDeposito"                   decimal(26,8),
        "RetirosCotizaciones"               decimal(26,8)
    )
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
                SUM(COALESCE("AhorrosTrabajador","AhorrosTrabajador")) AS "AhorrosTrabajador",
                SUM(COALESCE("AhorrosDeposito","AhorrosDeposito")) AS "AhorrosDeposito",
                SUM(COALESCE("AhorrosCotizaciones","AhorrosCotizaciones")) AS "AhorrosCotizaciones",
                SUM(COALESCE("RetirosTrabajador","RetirosTrabajador")) AS "RetirosTrabajador",
                SUM(COALESCE("RetirosDeposito","RetirosDeposito")) AS "RetirosDeposito",
                SUM(COALESCE("RetirosCotizaciones","RetirosCotizaciones")) AS "RetirosCotizaciones"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1871"
            WHERE "IdDocumento" = :p_IdDocumento
            UNION ALL
            SELECT
                'FILTRO' AS "TYPE",
                COUNT(1) AS "COUNT",
                SUM(COALESCE("AhorrosTrabajador","AhorrosTrabajador")) AS "AhorrosTrabajador",
                SUM(COALESCE("AhorrosDeposito","AhorrosDeposito")) AS "AhorrosDeposito",
                SUM(COALESCE("AhorrosCotizaciones","AhorrosCotizaciones")) AS "AhorrosCotizaciones",
                SUM(COALESCE("RetirosTrabajador","RetirosTrabajador")) AS "RetirosTrabajador",
                SUM(COALESCE("RetirosDeposito","RetirosDeposito")) AS "RetirosDeposito",
                SUM(COALESCE("RetirosCotizaciones","RetirosCotizaciones")) AS "RetirosCotizaciones"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1871"
            WHERE "IdDocumento" = :p_IdDocumento
            AND ("VboUsuario" LIKE :p_VboUsuario or :p_VboUsuario IS NULL)
            AND ("Observaciones" LIKE :p_Observaciones or :p_Observaciones IS NULL)
            AND ("FuenteDatos" LIKE :p_FuenteDatos or :p_FuenteDatos IS NULL)
            AND ("RutCliente" LIKE :p_RutCliente or :p_RutCliente is null)
            AND ("IdCliente" >= :p_IdCliente_Desde OR :p_IdCliente_Desde IS NULL)
            AND ("IdCliente" >= :p_IdCliente_Hasta OR :p_IdCliente_Hasta IS NULL)
            AND ("AhorrosTrabajador" >= :p_AhorrosTrabajador_Desde OR :p_AhorrosTrabajador_Desde IS NULL)
            AND ("AhorrosTrabajador" <= :p_AhorrosTrabajador_Hasta OR :p_AhorrosTrabajador_Hasta IS NULL)
            AND ("AhorrosDeposito" >= :p_AhorrosDeposito_Desde OR :p_AhorrosDeposito_Desde IS NULL)
            AND ("AhorrosDeposito" <= :p_AhorrosDeposito_Hasta OR :p_AhorrosDeposito_Hasta IS NULL)
            AND ("AhorrosCotizaciones" >= :p_AhorrosCotizaciones_Desde OR :p_AhorrosCotizaciones_Desde IS NULL)
            AND ("AhorrosCotizaciones" <= :p_AhorrosCotizaciones_Hasta OR :p_AhorrosCotizaciones_Hasta IS NULL)
            AND ("RetirosTrabajador" >= :p_RetirosTrabajador_Desde OR :p_RetirosTrabajador_Desde IS NULL)
            AND ("RetirosTrabajador" <= :p_RetirosTrabajador_Hasta OR :p_RetirosTrabajador_Hasta IS NULL)
            AND ("RetirosDeposito" >= :p_RetirosDeposito_Desde OR :p_RetirosDeposito_Desde IS NULL)
            AND ("RetirosDeposito" <= :p_RetirosDeposito_Hasta OR :p_RetirosDeposito_Hasta IS NULL)
            AND ("RetirosCotizaciones" >= :p_RetirosCotizaciones_Desde OR :p_RetirosCotizaciones_Desde IS NULL)
            AND ("RetirosCotizaciones" <= :p_RetirosCotizaciones_Hasta OR :p_RetirosCotizaciones_Hasta IS NULL);
    ELSE
        p_out_tbl =
            SELECT
                'TOTAL' AS "TYPE",
                COUNT(1) AS "COUNT",
                SUM(COALESCE("AhorrosTrabajador","AhorrosTrabajador")) AS "AhorrosTrabajador",
                SUM(COALESCE("AhorrosDeposito","AhorrosDeposito")) AS "AhorrosDeposito",
                SUM(COALESCE("AhorrosCotizaciones","AhorrosCotizaciones")) AS "AhorrosCotizaciones",
                SUM(COALESCE("RetirosTrabajador","RetirosTrabajador")) AS "RetirosTrabajador",
                SUM(COALESCE("RetirosDeposito","RetirosDeposito")) AS "RetirosDeposito",
                SUM(COALESCE("RetirosCotizaciones","RetirosCotizaciones")) AS "RetirosCotizaciones"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1871"
            WHERE "IdDocumento" = :p_IdDocumento
            UNION ALL
            SELECT
                'FILTRO' AS "TYPE",
                COUNT(1) AS "COUNT",
                SUM(COALESCE("AhorrosTrabajador","AhorrosTrabajador")) AS "AhorrosTrabajador",
                SUM(COALESCE("AhorrosDeposito","AhorrosDeposito")) AS "AhorrosDeposito",
                SUM(COALESCE("AhorrosCotizaciones","AhorrosCotizaciones")) AS "AhorrosCotizaciones",
                SUM(COALESCE("RetirosTrabajador","RetirosTrabajador")) AS "RetirosTrabajador",
                SUM(COALESCE("RetirosDeposito","RetirosDeposito")) AS "RetirosDeposito",
                SUM(COALESCE("RetirosCotizaciones","RetirosCotizaciones")) AS "RetirosCotizaciones"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1871"
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