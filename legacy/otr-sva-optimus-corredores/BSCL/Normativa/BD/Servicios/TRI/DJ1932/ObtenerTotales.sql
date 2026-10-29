SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1932.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1932.ObtenerTotales"
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
    IN p_RutDepositanteInversionistaPrestamista NVARCHAR(40) DEFAULT NULL,
       
    -- Rango del ID cliente.
    IN p_IdCliente_Desde BIGINT DEFAULT NULL,
    IN p_IdCliente_Hasta BIGINT DEFAULT NULL,
			
	-- Rango Depositos Inversiones Prestamos
	IN p_DepositosInversionesPrestamos_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_DepositosInversionesPrestamos_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Rescates Devoluciones
	IN p_RescatesDevoluciones_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_RescatesDevoluciones_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Intereses Percibidas
	IN p_InteresesPercibidas_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_InteresesPercibidas_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Dividendos Percibidas
	IN p_DividendosPercibidas_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_DividendosPercibidas_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Contratos Por Diferencia Percibidas
	IN p_ContratosPorDiferenciaPercibidas_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_ContratosPorDiferenciaPercibidas_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Otras Rentas Percibidas
	IN p_OtrasRentasPercibidas_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_OtrasRentasPercibidas_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Intereses Devengadas
	IN p_InteresesDevengadas_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_InteresesDevengadas_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Otras Rentas Devengadas
	IN p_OtrasRentasDevengadas_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_OtrasRentasDevengadas_Hasta DECIMAL(26,8) DEFAULT NULL,
		
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
        

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 								NVARCHAR(100),
        "COUNT" 							BIGINT,
        "DepositosInversionesPrestamos" 	DECIMAL(26,8),
        "RescatesDevoluciones" 				DECIMAL(26,8),
        "InteresesPercibidas"				DECIMAL(26,8),
        "DividendosPercibidas" 				DECIMAL(26,8),
        "ContratosPorDiferenciaPercibidas" 	DECIMAL(26,8),
        "OtrasRentasPercibidas"				DECIMAL(26,8),
        "InteresesDevengadas" 				DECIMAL(26,8),
        "OtrasRentasDevengadas"				DECIMAL(26,8)
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
	                    SUM("DepositosInversionesPrestamos") AS "DepositosInversionesPrestamos",
	                    SUM("RescatesDevoluciones") AS "RescatesDevoluciones",
	                    SUM("InteresesPercibidas") AS "InteresesPercibidas",
	                    SUM("DividendosPercibidas") AS "DividendosPercibidas",
	                    SUM("ContratosPorDiferenciaPercibidas") AS "ContratosPorDiferenciaPercibidas",
	                    SUM("OtrasRentasPercibidas") AS "OtrasRentasPercibidas",
	                    SUM("InteresesDevengadas") AS "InteresesDevengadas",
	                    SUM("OtrasRentasDevengadas") AS "OtrasRentasDevengadas"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1932"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                select
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                   	SUM("DepositosInversionesPrestamos") AS "DepositosInversionesPrestamos",
	                    SUM("RescatesDevoluciones") AS "RescatesDevoluciones",
	                    SUM("InteresesPercibidas") AS "InteresesPercibidas",
	                    SUM("DividendosPercibidas") AS "DividendosPercibidas",
	                    SUM("ContratosPorDiferenciaPercibidas") AS "ContratosPorDiferenciaPercibidas",
	                    SUM("OtrasRentasPercibidas") AS "OtrasRentasPercibidas",
	                    SUM("InteresesDevengadas") AS "InteresesDevengadas",
	                    SUM("OtrasRentasDevengadas") AS "OtrasRentasDevengadas"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1932"
	                    WHERE "IdDocumento" = p_IdDocumento
	                    AND ("VboUsuario" LIKE p_VboUsuario or p_VboUsuario IS NULL)
	                    AND ("Observaciones" LIKE p_Observaciones or p_Observaciones IS NULL)
	                    AND ("FuenteDatos" LIKE p_FuenteDatos or p_FuenteDatos IS NULL)
	
	                    AND ("RutDepositanteInversionistaPrestamista" LIKE p_RutDepositanteInversionistaPrestamista OR p_RutDepositanteInversionistaPrestamista IS NULL)
	                    AND ("IdCliente" >= p_IdCliente_Desde OR p_IdCliente_Desde IS NULL)
	                    AND ("IdCliente" <= p_IdCliente_Hasta OR p_IdCliente_Hasta IS NULL)
	                    AND ("DepositosInversionesPrestamos" >= p_DepositosInversionesPrestamos_Desde OR p_DepositosInversionesPrestamos_Desde IS NULL)
	                    AND ("DepositosInversionesPrestamos" <= p_DepositosInversionesPrestamos_Hasta OR p_DepositosInversionesPrestamos_Hasta IS NULL)
	                    AND ("RescatesDevoluciones" >= p_RescatesDevoluciones_Desde OR p_RescatesDevoluciones_Desde IS NULL)
	                    AND ("RescatesDevoluciones" <= p_RescatesDevoluciones_Hasta OR p_RescatesDevoluciones_Hasta IS NULL)
	                    AND ("InteresesPercibidas" >= p_InteresesPercibidas_Desde OR p_InteresesPercibidas_Desde IS NULL)
	                    AND ("InteresesPercibidas" <= p_InteresesPercibidas_Hasta OR p_InteresesPercibidas_Hasta IS NULL)
	                    AND ("DividendosPercibidas" >= p_DividendosPercibidas_Desde OR p_DividendosPercibidas_Desde IS NULL)
	                    AND ("DividendosPercibidas" <= p_DividendosPercibidas_Hasta OR p_DividendosPercibidas_Hasta IS NULL)
	                    AND ("ContratosPorDiferenciaPercibidas" >= p_ContratosPorDiferenciaPercibidas_Desde OR p_ContratosPorDiferenciaPercibidas_Desde IS NULL)
	                    AND ("ContratosPorDiferenciaPercibidas" <= p_ContratosPorDiferenciaPercibidas_Hasta OR p_ContratosPorDiferenciaPercibidas_Hasta IS NULL)
	                    AND ("OtrasRentasPercibidas" >= p_OtrasRentasPercibidas_Desde OR p_OtrasRentasPercibidas_Desde IS NULL)
	                    AND ("OtrasRentasPercibidas" <= p_OtrasRentasPercibidas_Hasta OR p_OtrasRentasPercibidas_Hasta IS NULL)
	                    AND ("InteresesDevengadas" >= p_InteresesDevengadas_Desde OR p_InteresesDevengadas_Desde IS NULL)
	                    AND ("InteresesDevengadas" <= p_InteresesDevengadas_Hasta OR p_InteresesDevengadas_Hasta IS NULL)
	                    AND ("OtrasRentasDevengadas" >= p_OtrasRentasDevengadas_Desde OR p_OtrasRentasDevengadas_Desde IS NULL)
	                    AND ("OtrasRentasDevengadas" <= p_OtrasRentasDevengadas_Hasta OR p_OtrasRentasDevengadas_Hasta IS NULL);
      ELSE
	    p_out_tbl = SELECT
	                    'TOTAL' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("DepositosInversionesPrestamos") AS "DepositosInversionesPrestamos",
	                    SUM("RescatesDevoluciones") AS "RescatesDevoluciones",
	                    SUM("InteresesPercibidas") AS "InteresesPercibidas",
	                    SUM("DividendosPercibidas") AS "DividendosPercibidas",
	                    SUM("ContratosPorDiferenciaPercibidas") AS "ContratosPorDiferenciaPercibidas",
	                    SUM("OtrasRentasPercibidas") AS "OtrasRentasPercibidas",
	                    SUM("InteresesDevengadas") AS "InteresesDevengadas",
	                    SUM("OtrasRentasDevengadas") AS "OtrasRentasDevengadas"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1932"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                SELECT
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("DepositosInversionesPrestamos") AS "DepositosInversionesPrestamos",
	                    SUM("RescatesDevoluciones") AS "RescatesDevoluciones",
	                    SUM("InteresesPercibidas") AS "InteresesPercibidas",
	                    SUM("DividendosPercibidas") AS "DividendosPercibidas",
	                    SUM("ContratosPorDiferenciaPercibidas") AS "ContratosPorDiferenciaPercibidas",
	                    SUM("OtrasRentasPercibidas") AS "OtrasRentasPercibidas",
	                    SUM("InteresesDevengadas") AS "InteresesDevengadas",
	                    SUM("OtrasRentasDevengadas") AS "OtrasRentasDevengadas"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1932"
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