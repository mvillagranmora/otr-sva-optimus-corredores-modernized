SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1874.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1874.ObtenerTotales"
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
	
	-- Filtro Rut Emisor
    IN p_RutEmisor NVARCHAR(40) DEFAULT NULL,
    
    -- Filtro Rut Inversionista
    IN p_RutInversionista NVARCHAR(40) DEFAULT NULL,
    
    -- Filtro Nemotecnico
	IN p_NemotecnicoSVS NVARCHAR(40) DEFAULT NULL,
	
	-- Filtro Moneda Unidad Reajuste
	IN p_MonedaUnidadReajuste NVARCHAR(10) DEFAULT NULL,
        
    -- Rango del ID cliente.
    IN p_IdCliente_Desde BIGINT DEFAULT NULL,
    IN p_IdCliente_Hasta BIGINT DEFAULT NULL,

    -- Rango del ID portafolio.
    IN p_IdPortafolio_Desde BIGINT DEFAULT NULL,
    IN p_IdPortafolio_Hasta BIGINT DEFAULT NULL,
			
	-- Rango Interes Devengado pesos
	IN p_InteresDevengadoPesos_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_InteresDevengadoPesos_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Resultado Enajenacion Pesos
	IN p_ResultadoEnajenacionPesos_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_ResultadoEnajenacionPesos_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Monto Ventas
	IN p_RetencionPesos_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_RetencionPesos_Hasta DECIMAL(26,8) DEFAULT NULL,
		
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
        

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 							NVARCHAR(100),
        "COUNT" 						BIGINT,
        "MovimientoSaldoInicial"    	DECIMAL(26,8),
        "MovimientoAdquision"       	DECIMAL(26,8),
        "MovimientoEnajenacion"			DECIMAL(26,8),
        "MovimientoRescateAnticipado"	DECIMAL(26,8),
        "MovimientoSaldoFinal"			DECIMAL(26,8),
        "SaldoFinalNominal"				DECIMAL(26,8),
        "InteresDevengadoMonedaUnidadReajuste"		DECIMAL(26,8),
        "InteresDevengadoPesos" 		DECIMAL(26,8),
        "ResultadoEnajenacionMonedaUnidadReajuste" 		DECIMAL(26,8),
        "ResultadoEnajenacionPesos" 	DECIMAL(26,8),
        "RetencionMonedaUnidadReajuste" 	DECIMAL(26,8),
        "RetencionPesos"				DECIMAL(26,8)
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
	                    SUM("MovimientoSaldoInicial") AS "MovimientoSaldoInicial",
	                    SUM("MovimientoAdquision") AS "MovimientoAdquision",
	                    SUM("MovimientoEnajenacion") AS "MovimientoEnajenacion",
	                    SUM("MovimientoRescateAnticipado") AS "MovimientoRescateAnticipado",
	                    SUM("MovimientoSaldoFinal") AS "MovimientoSaldoFinal",
	                    SUM("SaldoFinalNominal") AS "SaldoFinalNominal",
	                    SUM("InteresDevengadoMonedaUnidadReajuste") AS "InteresDevengadoMonedaUnidadReajuste",
	                    SUM("InteresDevengadoPesos") AS "InteresDevengadoPesos",
	                    SUM("ResultadoEnajenacionMonedaUnidadReajuste") AS "ResultadoEnajenacionMonedaUnidadReajuste",
	                    SUM("ResultadoEnajenacionPesos") AS "ResultadoEnajenacionPesos",
	                    SUM("RetencionMonedaUnidadReajuste") AS "RetencionMonedaUnidadReajuste",
	                    SUM("RetencionPesos") AS "RetencionPesos"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1874"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                select
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("MovimientoSaldoInicial") AS "MovimientoSaldoInicial",
	                    SUM("MovimientoAdquision") AS "MovimientoAdquision",
	                    SUM("MovimientoEnajenacion") AS "MovimientoEnajenacion",
	                    SUM("MovimientoRescateAnticipado") AS "MovimientoRescateAnticipado",
	                    SUM("MovimientoSaldoFinal") AS "MovimientoSaldoFinal",
	                    SUM("SaldoFinalNominal") AS "SaldoFinalNominal",
	                    SUM("InteresDevengadoMonedaUnidadReajuste") AS "InteresDevengadoMonedaUnidadReajuste",
	                    SUM("InteresDevengadoPesos") AS "InteresDevengadoPesos",
	                    SUM("ResultadoEnajenacionMonedaUnidadReajuste") AS "ResultadoEnajenacionMonedaUnidadReajuste",
	                    SUM("ResultadoEnajenacionPesos") AS "ResultadoEnajenacionPesos",
	                    SUM("RetencionMonedaUnidadReajuste") AS "RetencionMonedaUnidadReajuste",
	                    SUM("RetencionPesos") AS "RetencionPesos"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1874"
	                    WHERE "IdDocumento" = p_IdDocumento
	                    AND ("VboUsuario" LIKE p_VboUsuario or p_VboUsuario IS NULL)
	                    AND ("Observaciones" LIKE p_Observaciones or p_Observaciones IS NULL)
	                    AND ("FuenteDatos" LIKE p_FuenteDatos or p_FuenteDatos IS NULL)
	
	                    AND ("RutEmisor" LIKE p_RutEmisor OR p_RutEmisor IS NULL)
	                    AND ("RutInversionista" LIKE p_RutInversionista OR p_RutInversionista IS NULL)
	                    AND ("NemotecnicoSVS" LIKE p_NemotecnicoSVS OR p_NemotecnicoSVS IS NULL)
	                    AND ("MonedaUnidadReajuste" LIKE p_MonedaUnidadReajuste OR p_MonedaUnidadReajuste IS NULL)
	                    AND ("IdCliente" >= p_IdCliente_Desde OR p_IdCliente_Desde IS NULL)
	                    AND ("IdCliente" <= p_IdCliente_Hasta OR p_IdCliente_Hasta IS NULL)
	                    AND ("IdPortafolio" >= p_IdPortafolio_Desde OR p_IdPortafolio_Desde IS NULL)
	                    AND ("IdPortafolio" <= p_IdPortafolio_Hasta OR p_IdPortafolio_Hasta IS NULL)
	                    AND ("InteresDevengadoPesos" >= p_InteresDevengadoPesos_Desde OR p_InteresDevengadoPesos_Desde IS NULL)
	                    AND ("InteresDevengadoPesos" <= p_InteresDevengadoPesos_Hasta OR p_InteresDevengadoPesos_Hasta IS NULL)
	                    AND ("ResultadoEnajenacionPesos" >= p_ResultadoEnajenacionPesos_Desde OR p_ResultadoEnajenacionPesos_Desde IS NULL)
	                    AND ("ResultadoEnajenacionPesos" <= p_ResultadoEnajenacionPesos_Hasta OR p_ResultadoEnajenacionPesos_Hasta IS NULL)
	                    AND ("RetencionPesos" >= p_RetencionPesos_Desde OR p_RetencionPesos_Desde IS NULL)
	                    AND ("RetencionPesos" <= p_RetencionPesos_Hasta OR p_RetencionPesos_Hasta IS NULL);
      ELSE
	    p_out_tbl = SELECT
	                    'TOTAL' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("MovimientoSaldoInicial") AS "MovimientoSaldoInicial",
	                    SUM("MovimientoAdquision") AS "MovimientoAdquision",
	                    SUM("MovimientoEnajenacion") AS "MovimientoEnajenacion",
	                    SUM("MovimientoRescateAnticipado") AS "MovimientoRescateAnticipado",
	                    SUM("MovimientoSaldoFinal") AS "MovimientoSaldoFinal",
	                    SUM("SaldoFinalNominal") AS "SaldoFinalNominal",
	                    SUM("InteresDevengadoMonedaUnidadReajuste") AS "InteresDevengadoMonedaUnidadReajuste",
	                    SUM("InteresDevengadoPesos") AS "InteresDevengadoPesos",
	                    SUM("ResultadoEnajenacionMonedaUnidadReajuste") AS "ResultadoEnajenacionMonedaUnidadReajuste",
	                    SUM("ResultadoEnajenacionPesos") AS "ResultadoEnajenacionPesos",
	                    SUM("RetencionMonedaUnidadReajuste") AS "RetencionMonedaUnidadReajuste",
	                    SUM("RetencionPesos") AS "RetencionPesos"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1874"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                SELECT
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("MovimientoSaldoInicial") AS "MovimientoSaldoInicial",
	                    SUM("MovimientoAdquision") AS "MovimientoAdquision",
	                    SUM("MovimientoEnajenacion") AS "MovimientoEnajenacion",
	                    SUM("MovimientoRescateAnticipado") AS "MovimientoRescateAnticipado",
	                    SUM("MovimientoSaldoFinal") AS "MovimientoSaldoFinal",
	                    SUM("SaldoFinalNominal") AS "SaldoFinalNominal",
	                    SUM("InteresDevengadoMonedaUnidadReajuste") AS "InteresDevengadoMonedaUnidadReajuste",
	                    SUM("InteresDevengadoPesos") AS "InteresDevengadoPesos",
	                    SUM("ResultadoEnajenacionMonedaUnidadReajuste") AS "ResultadoEnajenacionMonedaUnidadReajuste",
	                    SUM("ResultadoEnajenacionPesos") AS "ResultadoEnajenacionPesos",
	                    SUM("RetencionMonedaUnidadReajuste") AS "RetencionMonedaUnidadReajuste",
	                    SUM("RetencionPesos") AS "RetencionPesos"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1874"
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