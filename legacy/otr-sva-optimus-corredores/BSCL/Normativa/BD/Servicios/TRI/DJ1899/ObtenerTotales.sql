SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1899.ObtenerTotales";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1899.ObtenerTotales"
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
    
    -- Filtro 
    IN p_RutCliente NVARCHAR(40) DEFAULT NULL,
       
    -- Rango del ID trbajador.
    IN p_IdCliente_Desde BIGINT DEFAULT NULL,
    IN p_IdCliente_Hasta BIGINT DEFAULT NULL,
    
    IN p_RutEmpleador NVARCHAR(40) DEFAULT NULL,
    
    -- Rango del ID trbajador.
    --IN p_IdEmpleador_Desde BIGINT DEFAULT NULL,
    --IN p_IdEmpleador_Hasta BIGINT DEFAULT NULL,

    IN p_DepositoEmpleador_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_DepositoEmpleador_Hasta DECIMAL(28,8) DEFAULT NULL,
    ---Aporte Empleador
    IN p_AporteEmpleador_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_AporteEmpleador_Hasta DECIMAL(28,8) DEFAULT NULL,
    IN p_TraspasoEmpleador_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_TraspasoEmpleador_Hasta DECIMAL(28,8) DEFAULT NULL,
    IN p_RetiroEmpleadorActualizado_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_RetiroEmpleadorActualizado_Hasta DECIMAL(28,8) DEFAULT NULL,
    IN p_MontoRetencionActualizado_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_MontoRetencionActualizado_Hasta DECIMAL(28,8) DEFAULT NULL,
    --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
    IN p_AhorrosModalidadIndirectaEmpleador_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_AhorrosModalidadIndirectaEmpleador_Hasta DECIMAL(28,8) DEFAULT NULL,
    IN p_AhorrosModalidadDirectaTrabajador_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_AhorrosModalidadDirectaTrabajador_Hasta DECIMAL(28,8) DEFAULT NULL,
    --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
    --del ART. 42 Nª1 de la LIR en UF
    IN p_CotizacionesModalidadIndirectaEmpleador_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_CotizacionesModalidadIndirectaEmpleador_Hasta DECIMAL(28,8) DEFAULT NULL,
    IN p_CotizacionesModalidadDirectaTrabajador_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_CotizacionesModalidadDirectaTrabajador_Hasta DECIMAL(28,8) DEFAULT NULL,
    --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
    --del Art.42 Nª2 de la LIR en UF
    IN p_CotizacionesTrabajadorIndependiente_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_CotizacionesTrabajadorIndependiente_Hasta DECIMAL(28,8) DEFAULT NULL,
    --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    --los aportes del empleador, actualizados en $
    IN p_MontoRetiroPorAhorrosIncisoPrimero_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_MontoRetiroPorAhorrosIncisoPrimero_Hasta DECIMAL(28,8) DEFAULT NULL,
    --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    IN p_RetencionImpuestoSobreRetiros_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_RetencionImpuestoSobreRetiros_Hasta DECIMAL(28,8) DEFAULT NULL,
    --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    IN p_MontoRetiroPorAhorrosIncisoSegundo_Desde DECIMAL(28,8) DEFAULT NULL,
    IN p_MontoRetiroPorAhorrosIncisoSegundo_Hasta DECIMAL(28,8) DEFAULT NULL,
    
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE"                              NVARCHAR(100),
        "COUNT"                             BIGINT,
        "DepositoEmpleador"                 DECIMAL(28,8),
        ---Aporte Empleador
        "AporteEmpleador"                   DECIMAL(28,8),
        "TraspasoEmpleador"                 DECIMAL(28,8),
        "RetiroEmpleadorActualizado"        DECIMAL(28,8),
        "MontoRetencionActualizado"         DECIMAL(28,8),
        --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
        "AhorrosModalidadIndirectaEmpleador" DECIMAL(28,8),
        "AhorrosModalidadDirectaTrabajador" DECIMAL(28,8),
        --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
        --del ART. 42 Nª1 de la LIR en UF
        "CotizacionesModalidadIndirectaEmpleador" DECIMAL(28,8),
        "CotizacionesModalidadDirectaTrabajador"  DECIMAL(28,8),
        --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
        --del Art.42 Nª2 de la LIR en UF
        "CotizacionesTrabajadorIndependiente" DECIMAL(28,8),
        --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
        --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
        --los aportes del empleador, actualizados en $
        "MontoRetiroPorAhorrosIncisoPrimero"  DECIMAL(28,8),
        --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
        "RetencionImpuestoSobreRetiros"       DECIMAL(28,8),  
        --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
        "MontoRetiroPorAhorrosIncisoSegundo"  DECIMAL(28,8))
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
                SUM(COALESCE("DepositoEmpleador",0)) AS "DepositoEmpleador",
                ---Aporte Empleador
                SUM(COALESCE("AporteEmpleador",0)) AS "AporteEmpleador",
                SUM(COALESCE("TraspasoEmpleador",0)) AS "TraspasoEmpleador",
                SUM(COALESCE("RetiroEmpleadorActualizado",0)) AS "RetiroEmpleadorActualizado",
                SUM(COALESCE("MontoRetencionActualizado",0)) AS "MontoRetencionActualizado",
                --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
                SUM(COALESCE("AhorrosModalidadIndirectaEmpleador",0)) AS "AhorrosModalidadIndirectaEmpleador",
                SUM(COALESCE("AhorrosModalidadDirectaTrabajador",0)) AS "AhorrosModalidadDirectaTrabajador",
                --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
                --del ART. 42 Nª1 de la LIR en UF
                SUM(COALESCE("CotizacionesModalidadIndirectaEmpleador",0)) AS "CotizacionesModalidadIndirectaEmpleador",
                SUM(COALESCE("CotizacionesModalidadDirectaTrabajador",0)) AS "CotizacionesModalidadDirectaTrabajador",
                --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
                --del Art.42 Nª2 de la LIR en UF
                SUM(COALESCE("CotizacionesTrabajadorIndependiente",0)) AS "CotizacionesTrabajadorIndependiente",
                --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
                --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
                --los aportes del empleador, actualizados en $
                ROUND(SUM(COALESCE("MontoRetiroPorAhorrosIncisoPrimero",0)),0, ROUND_DOWN) AS "MontoRetiroPorAhorrosIncisoPrimero",
                --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
                ROUND(SUM(COALESCE("RetencionImpuestoSobreRetiros",0)),0, ROUND_DOWN) AS "RetencionImpuestoSobreRetiros",
                --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
                SUM(COALESCE("MontoRetiroPorAhorrosIncisoSegundo",0)) AS "MontoRetiroPorAhorrosIncisoSegundo"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1899"
            WHERE "IdDocumento" = :p_IdDocumento
              --AND "EjecutadoComplemento" = 'S'
            UNION ALL
            SELECT
                'FILTRO' AS "TYPE",
                COUNT(1) AS "COUNT",
                SUM(COALESCE("DepositoEmpleador",0)) AS "DepositoEmpleador",
                ---Aporte Empleador
                SUM(COALESCE("AporteEmpleador",0)) AS "AporteEmpleador",
                SUM(COALESCE("TraspasoEmpleador",0)) AS "TraspasoEmpleador",
                SUM(COALESCE("RetiroEmpleadorActualizado",0)) AS "RetiroEmpleadorActualizado",
                SUM(COALESCE("MontoRetencionActualizado",0)) AS "MontoRetencionActualizado",
                --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
                SUM(COALESCE("AhorrosModalidadIndirectaEmpleador",0)) AS "AhorrosModalidadIndirectaEmpleador",
                SUM(COALESCE("AhorrosModalidadDirectaTrabajador",0)) AS "AhorrosModalidadDirectaTrabajador",
                --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
                --del ART. 42 Nª1 de la LIR en UF
                SUM(COALESCE("CotizacionesModalidadIndirectaEmpleador",0)) AS "CotizacionesModalidadIndirectaEmpleador",
                SUM(COALESCE("CotizacionesModalidadDirectaTrabajador",0)) AS "CotizacionesModalidadDirectaTrabajador",
                --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
                --del Art.42 Nª2 de la LIR en UF
                SUM(COALESCE("CotizacionesTrabajadorIndependiente",0)) AS "CotizacionesTrabajadorIndependiente",
                --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
                --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
                --los aportes del empleador, actualizados en $
                ROUND(SUM(COALESCE("MontoRetiroPorAhorrosIncisoPrimero",0)),0, ROUND_DOWN) AS "MontoRetiroPorAhorrosIncisoPrimero",
                --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
                ROUND(SUM(COALESCE("RetencionImpuestoSobreRetiros",0)),0, ROUND_DOWN) AS "RetencionImpuestoSobreRetiros",
                --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
                SUM(COALESCE("MontoRetiroPorAhorrosIncisoSegundo",0)) AS "MontoRetiroPorAhorrosIncisoSegundo"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1899"
            WHERE "IdDocumento" = :p_IdDocumento
            --AND "EjecutadoComplemento" = 'S'
            AND ("VboUsuario" LIKE :p_VboUsuario or :p_VboUsuario IS NULL)
            AND ("Observaciones" LIKE :p_Observaciones or :p_Observaciones IS NULL)
            AND ("FuenteDatos" LIKE :p_FuenteDatos or :p_FuenteDatos IS NULL)
            AND ("RutCliente" LIKE :p_RutCliente or :p_RutCliente is null)
            AND ("IdCliente" >= :p_IdCliente_Desde OR :p_IdCliente_Desde IS NULL)
            AND ("IdCliente" >= :p_IdCliente_Hasta OR :p_IdCliente_Hasta IS NULL)
            AND ("RutEmpleador" LIKE :p_RutEmpleador or :p_RutEmpleador is null)
            --AND ("IdEmpleador" >= :p_IdEmpleador_Desde OR :p_IdEmpleador_Desde IS NULL)
            --AND ("IdEmpleador" >= :p_IdEmpleador_Hasta OR :p_IdEmpleador_Hasta IS NULL)
            AND ("DepositoEmpleador" >= :p_DepositoEmpleador_Desde OR :p_DepositoEmpleador_Desde IS NULL)
            AND ("DepositoEmpleador" <= :p_DepositoEmpleador_Hasta OR :p_DepositoEmpleador_Hasta IS NULL)
            ---Aporte Empleador
            AND ("AporteEmpleador" >= :p_AporteEmpleador_Desde OR :p_AporteEmpleador_Desde IS NULL)
            AND ("AporteEmpleador" <= :p_AporteEmpleador_Hasta OR :p_AporteEmpleador_Hasta IS NULL)
            AND ("TraspasoEmpleador" >= :p_TraspasoEmpleador_Desde OR :p_TraspasoEmpleador_Desde IS NULL)
            AND ("TraspasoEmpleador" <= :p_TraspasoEmpleador_Desde OR :p_TraspasoEmpleador_Desde IS NULL)
            AND ("RetiroEmpleadorActualizado" >= :p_RetiroEmpleadorActualizado_Desde OR :p_RetiroEmpleadorActualizado_Desde IS NULL)
            AND ("RetiroEmpleadorActualizado" <= :p_RetiroEmpleadorActualizado_Hasta OR :p_RetiroEmpleadorActualizado_Hasta IS NULL)
            AND ("MontoRetencionActualizado" >= :p_MontoRetencionActualizado_Desde OR :p_MontoRetencionActualizado_Desde IS NULL)
            AND ("MontoRetencionActualizado" <= :p_MontoRetencionActualizado_Hasta OR :p_MontoRetencionActualizado_Hasta IS NULL)
            --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
            AND ("AhorrosModalidadIndirectaEmpleador" <= :p_AhorrosModalidadIndirectaEmpleador_Desde OR :p_AhorrosModalidadIndirectaEmpleador_Desde IS NULL)        
            AND ("AhorrosModalidadIndirectaEmpleador" <= :p_AhorrosModalidadIndirectaEmpleador_Hasta OR :p_MontoRetencionActualizado_Hasta IS NULL)
            AND ("AhorrosModalidadDirectaTrabajador" <= :p_AhorrosModalidadDirectaTrabajador_Desde OR :p_AhorrosModalidadDirectaTrabajador_Desde IS NULL)
            AND ("AhorrosModalidadDirectaTrabajador" <= :p_AhorrosModalidadDirectaTrabajador_Hasta OR :p_AhorrosModalidadDirectaTrabajador_Hasta IS NULL)
            --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
            --del ART. 42 Nª1 de la LIR en UF
            AND ("CotizacionesModalidadIndirectaEmpleador" <= :p_CotizacionesModalidadIndirectaEmpleador_Desde OR :p_CotizacionesModalidadIndirectaEmpleador_Desde IS NULL)
            AND ("CotizacionesModalidadIndirectaEmpleador" <= :p_CotizacionesModalidadIndirectaEmpleador_Hasta OR :p_CotizacionesModalidadIndirectaEmpleador_Hasta IS NULL)
            AND ("CotizacionesModalidadDirectaTrabajador" <= :p_CotizacionesModalidadDirectaTrabajador_Desde OR :p_CotizacionesModalidadDirectaTrabajador_Desde IS NULL)
            AND ("CotizacionesModalidadDirectaTrabajador" <= :p_CotizacionesModalidadDirectaTrabajador_Desde OR :p_CotizacionesModalidadDirectaTrabajador_Desde IS NULL)
            --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
            --del Art.42 Nª2 de la LIR en UF
            AND ("CotizacionesTrabajadorIndependiente" <= :p_CotizacionesTrabajadorIndependiente_Desde OR :p_CotizacionesTrabajadorIndependiente_Desde IS NULL)
            AND ("CotizacionesTrabajadorIndependiente" <= :p_CotizacionesTrabajadorIndependiente_Hasta OR :p_CotizacionesTrabajadorIndependiente_Hasta IS NULL)
            --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
            --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
            --los aportes del empleador, actualizados en $        
            AND ("MontoRetiroPorAhorrosIncisoPrimero" <= :p_MontoRetiroPorAhorrosIncisoPrimero_Desde OR :p_MontoRetiroPorAhorrosIncisoPrimero_Desde IS NULL)
            AND ("MontoRetiroPorAhorrosIncisoPrimero" <= :p_MontoRetiroPorAhorrosIncisoPrimero_Hasta OR :p_MontoRetiroPorAhorrosIncisoPrimero_Hasta IS NULL)
            --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
            AND ("RetencionImpuestoSobreRetiros" <= :p_RetencionImpuestoSobreRetiros_Desde OR :p_RetencionImpuestoSobreRetiros_Desde IS NULL)
            AND ("RetencionImpuestoSobreRetiros" <= :p_RetencionImpuestoSobreRetiros_Hasta OR :p_RetencionImpuestoSobreRetiros_Hasta IS NULL)
            --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
            AND ("MontoRetiroPorAhorrosIncisoSegundo" <= :p_MontoRetiroPorAhorrosIncisoSegundo_Desde OR :p_MontoRetiroPorAhorrosIncisoSegundo_Desde IS NULL)
            AND ("MontoRetiroPorAhorrosIncisoSegundo" <= :p_MontoRetiroPorAhorrosIncisoSegundo_Hasta OR :p_MontoRetiroPorAhorrosIncisoSegundo_Hasta IS NULL);
    ELSE
        p_out_tbl =
            SELECT
                'TOTAL' AS "TYPE",
                COUNT(1) AS "COUNT",
                SUM(COALESCE("DepositoEmpleador",0)) AS "DepositoEmpleador",
                ---Aporte Empleador
                SUM(COALESCE("AporteEmpleador",0)) AS "AporteEmpleador",
                SUM(COALESCE("TraspasoEmpleador",0)) AS "TraspasoEmpleador",
                SUM(COALESCE("RetiroEmpleadorActualizado",0)) AS "RetiroEmpleadorActualizado",
                SUM(COALESCE("MontoRetencionActualizado",0)) AS "MontoRetencionActualizado",
                --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
                SUM(COALESCE("AhorrosModalidadIndirectaEmpleador",0)) AS "AhorrosModalidadIndirectaEmpleador",
                SUM(COALESCE("AhorrosModalidadDirectaTrabajador",0)) AS "AhorrosModalidadDirectaTrabajador",
                --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
                --del ART. 42 Nª1 de la LIR en UF
                SUM(COALESCE("CotizacionesModalidadIndirectaEmpleador", 0)) AS "CotizacionesModalidadIndirectaEmpleador",
                SUM(COALESCE("CotizacionesModalidadDirectaTrabajador", 0)) AS "CotizacionesModalidadDirectaTrabajador",
                --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
                --del Art.42 Nª2 de la LIR en UF
                SUM(COALESCE("CotizacionesTrabajadorIndependiente",0)) AS "CotizacionesTrabajadorIndependiente",
                --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
                --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
                --los aportes del empleador, actualizados en $
                ROUND(SUM(COALESCE("MontoRetiroPorAhorrosIncisoPrimero",0)),0, ROUND_DOWN) AS "MontoRetiroPorAhorrosIncisoPrimero",
                --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
                ROUND(SUM(COALESCE("RetencionImpuestoSobreRetiros",0)),0, ROUND_DOWN) AS "RetencionImpuestoSobreRetiros",
                --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
                SUM(COALESCE("MontoRetiroPorAhorrosIncisoSegundo",0)) AS "MontoRetiroPorAhorrosIncisoSegundo"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1899"
            WHERE "IdDocumento" = :p_IdDocumento
              --AND "EjecutadoComplemento" = 'S'
            UNION ALL
            SELECT
                'FILTRO' AS "TYPE",
                COUNT(1) AS "COUNT",
                SUM(COALESCE("DepositoEmpleador",0)) AS "DepositoEmpleador",
                ---Aporte Empleador
                SUM(COALESCE("AporteEmpleador",0)) AS "AporteEmpleador",
                SUM(COALESCE("TraspasoEmpleador",0)) AS "TraspasoEmpleador",
                SUM(COALESCE("RetiroEmpleadorActualizado",0)) AS "RetiroEmpleadorActualizado",
                SUM(COALESCE("MontoRetencionActualizado",0)) AS "MontoRetencionActualizado",
                --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
                SUM(COALESCE("AhorrosModalidadIndirectaEmpleador",0)) AS "AhorrosModalidadIndirectaEmpleador",
                SUM(COALESCE("AhorrosModalidadDirectaTrabajador",0)) AS "AhorrosModalidadDirectaTrabajador",
                --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
                --del ART. 42 Nª1 de la LIR en UF
                SUM(COALESCE("CotizacionesModalidadIndirectaEmpleador", 0)) AS "CotizacionesModalidadIndirectaEmpleador",
                SUM(COALESCE("CotizacionesModalidadDirectaTrabajador", 0)) AS "CotizacionesModalidadDirectaTrabajador",
                --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
                --del Art.42 Nª2 de la LIR en UF
                SUM(COALESCE("CotizacionesTrabajadorIndependiente",0)) AS "CotizacionesTrabajadorIndependiente",
                --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
                --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
                --los aportes del empleador, actualizados en $
                SUM(COALESCE("MontoRetiroPorAhorrosIncisoPrimero",0)) AS "MontoRetiroPorAhorrosIncisoPrimero",
                --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
                SUM(COALESCE("RetencionImpuestoSobreRetiros",0)) AS "RetencionImpuestoSobreRetiros",
                --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
                SUM(COALESCE("MontoRetiroPorAhorrosIncisoSegundo",0)) AS "MontoRetiroPorAhorrosIncisoSegundo"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1899"
            WHERE "IdDocumento" = :p_IdDocumento
            --AND "EjecutadoComplemento" = 'S'
            AND "ID" IN (
                SELECT
                    a."IdDetalleDocumento"
                FROM "BSCL.Normativa.BD::TRI.DetalleValidacion" a
                LEFT JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b ON (b."ID" = a."IdResumenValidacion")
                WHERE b."CodigoValidacion" = :x_CodigoValidacion);
      END IF;
  END;
  GO