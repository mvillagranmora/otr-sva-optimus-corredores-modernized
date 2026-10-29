SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1829.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1829.ObtenerTotales"
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
	
	-- Filtro Rut Contraparte
    IN p_RutContraparte NVARCHAR(40) DEFAULT NULL,

    -- Rango del ID cliente.
    IN p_IdCliente_Desde BIGINT DEFAULT NULL,
    IN p_IdCliente_Hasta BIGINT DEFAULT NULL,

    -- Rango del ID portafolio.
    IN p_IdPortafolio_Desde BIGINT DEFAULT NULL,
    IN p_IdPortafolio_Hasta BIGINT DEFAULT NULL,
	
	-- Rango fecha acuerdo marco fecha suscripcion
	IN p_AcuerdoMarcoFechaSuscripcion_Desde DATE DEFAULT NULL,
	IN p_AcuerdoMarcoFechaSuscripcion_Hasta DATE DEFAULT NULL,
	
	-- Rango fecha contrato suscripcion
	IN p_ContratoFechaSuscripcion_Desde DATE DEFAULT NULL,
	IN p_ContratoFechaSuscripcion_Hasta	DATE DEFAULT NULL,
	
	-- Rango fecha vencimiento
	IN p_FechaVencimiento_Desde DATE DEFAULT NULL,
	IN p_FechaVencimiento_Hasta DATE DEFAULT NULL,
	
	-- Rango fecha liquidacion
	IN p_FechaLiquidacionEjercicioOpcion_Desde DATE DEFAULT NULL,
	IN p_FechaLiquidacionEjercicioOpcion_Hasta DATE DEFAULT NULL,
	
	-- Rango Resultado Ejercicio
	IN p_ResultadoEjercicio_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_ResultadoEjercicio_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Efecto Patrimonio
	IN p_EfectoPatrimonio_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_EfectoPatrimonio_Hasta DECIMAL(26,8) DEFAULT NULL,
	
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 					NVARCHAR(100),
        "COUNT" 				BIGINT,
        "ResultadoEjercicio" 	DECIMAL(26,8),
        "EfectoPatrimonio"   	DECIMAL(26,8)
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
	                    SUM("ResultadoEjercicio") AS "ResultadoEjercicio",
	                    SUM("EfectoPatrimonio") AS "EfectoPatrimonio"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1829"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                select
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("ResultadoEjercicio") AS "ResultadoEjercicio",
	                    SUM("EfectoPatrimonio") AS "EfectoPatrimonio"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1829"
	                    WHERE "IdDocumento" = p_IdDocumento
	                    AND ("VboUsuario" LIKE p_VboUsuario or p_VboUsuario IS NULL)
	                    AND ("Observaciones" LIKE p_Observaciones or p_Observaciones IS NULL)
	                    AND ("FuenteDatos" LIKE p_FuenteDatos or p_FuenteDatos IS NULL)
	
	                    AND ("RutContraparte" LIKE p_RutContraparte OR p_RutContraparte IS NULL)
	                    AND ("IdCliente" >= p_IdCliente_Desde OR p_IdCliente_Desde IS NULL)
	                    AND ("IdCliente" <= p_IdCliente_Hasta OR p_IdCliente_Hasta IS NULL)
	                    AND ("IdPortafolio" >= p_IdPortafolio_Desde OR p_IdPortafolio_Desde IS NULL)
	                    AND ("IdPortafolio" <= p_IdPortafolio_Hasta OR p_IdPortafolio_Hasta IS NULL)
	                    AND ("AcuerdoMarcoFechaSuscripcion" >= p_AcuerdoMarcoFechaSuscripcion_Desde OR p_AcuerdoMarcoFechaSuscripcion_Desde IS NULL)
	                    AND ("AcuerdoMarcoFechaSuscripcion" <= p_AcuerdoMarcoFechaSuscripcion_Hasta OR p_AcuerdoMarcoFechaSuscripcion_Hasta IS NULL)
	                    AND ("ContratoFechaSuscripcion" >= p_ContratoFechaSuscripcion_Desde OR p_ContratoFechaSuscripcion_Desde IS NULL)
	                    AND ("ContratoFechaSuscripcion" <= p_ContratoFechaSuscripcion_Hasta OR p_ContratoFechaSuscripcion_Hasta IS NULL)
	                    AND ("FechaVencimiento" >= p_FechaVencimiento_Desde OR p_FechaVencimiento_Desde IS NULL)
	                    AND ("FechaVencimiento" <= p_FechaVencimiento_Hasta OR p_FechaVencimiento_Hasta IS NULL)
	                    AND ("FechaLiquidacionEjercicioOpcion" >= p_FechaLiquidacionEjercicioOpcion_Desde OR p_FechaLiquidacionEjercicioOpcion_Desde IS NULL)
	                    AND ("FechaLiquidacionEjercicioOpcion" <= p_FechaLiquidacionEjercicioOpcion_Hasta OR p_FechaLiquidacionEjercicioOpcion_Hasta IS NULL)
	                    AND ("ResultadoEjercicio" >= p_ResultadoEjercicio_Desde OR p_ResultadoEjercicio_Desde IS NULL)
	                    AND ("ResultadoEjercicio" <= p_ResultadoEjercicio_Hasta OR p_ResultadoEjercicio_Hasta IS NULL)
	                    AND ("EfectoPatrimonio" >= p_EfectoPatrimonio_Desde OR p_EfectoPatrimonio_Desde IS NULL)
	                    AND ("EfectoPatrimonio" <= p_EfectoPatrimonio_Hasta OR p_EfectoPatrimonio_Hasta IS NULL);
      ELSE
	    p_out_tbl = SELECT
	                    'TOTAL' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("ResultadoEjercicio") AS "ResultadoEjercicio",
	                    SUM("EfectoPatrimonio") AS "EfectoPatrimonio"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1829"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                SELECT
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("ResultadoEjercicio") AS "ResultadoEjercicio",
	                    SUM("EfectoPatrimonio") AS "EfectoPatrimonio"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1829"
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