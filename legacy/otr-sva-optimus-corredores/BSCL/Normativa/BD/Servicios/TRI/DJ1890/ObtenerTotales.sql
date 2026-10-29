SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1890.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1890.ObtenerTotales"
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
    
    -- Filtro Rut Inversionista
    IN p_RutInversionista NVARCHAR(40) DEFAULT NULL,

    -- Rango del ID cliente.
    IN p_IdCliente_Desde BIGINT DEFAULT NULL,
    IN p_IdCliente_Hasta BIGINT DEFAULT NULL,

    -- Rango del ID portafolio.
    IN p_IdPortafolio_Desde BIGINT DEFAULT NULL,
    IN p_IdPortafolio_Hasta BIGINT DEFAULT NULL,
			
	-- Rango Interes Reales Depositos positivos
	IN p_InteresesRealesDepositosPositivo_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_InteresesRealesDepositosPositivo_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Intereses Reales Depositos negativo
	IN p_InteresesRealesDepositosNegativo_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_InteresesRealesDepositosNegativo_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Intereses Reales Mantencion positivo
	IN p_InteresesRealesMantencionPositivo_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_InteresesRealesMantencionPositivo_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Intereses Reales Mantencion negativo
	IN p_InteresesRealesMantencionNegativo_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_InteresesRealesMantencionNegativo_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Intereses Reales Operaciones positivo
	IN p_InteresesRealesOperacionesCaptacionPositivo_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_InteresesRealesOperacionesCaptacionPositivo_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Intereses Reales Operaciones negativo
	IN p_InteresesRealesOperacionesCaptacionNegativo_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_InteresesRealesOperacionesCaptacionNegativo_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Intereses Moneda Nacional Extranjera Nominal
	IN p_InteresesMonedaNacionalExtranjeraNominal_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_InteresesMonedaNacionalExtranjeraNominal_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Impuesto Adicional Moneda Nacional Extranjera Nominal
	IN p_ImpuestoAdicionalMondaNacionalExtranjeraNominal_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_ImpuestoAdicionalMondaNacionalExtranjeraNominal_Hasta DECIMAL(26,8) DEFAULT NULL,
		
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
        

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 													NVARCHAR(100),
        "COUNT" 												BIGINT,
        "InteresesRealesDepositosPositivo" 						DECIMAL(26,8),
        "InteresesRealesDepositosNegativo" 						DECIMAL(26,8),
        "InteresesRealesMantencionPositivo"						DECIMAL(26,8),
        "InteresesRealesMantencionNegativo" 					DECIMAL(26,8),
        "InteresesRealesOperacionesCaptacionPositivo" 			DECIMAL(26,8),
        "InteresesRealesOperacionesCaptacionNegativo"			DECIMAL(26,8),
        "InteresesMonedaNacionalExtranjeraNominal" 				DECIMAL(26,8),
        "ImpuestoAdicionalMondaNacionalExtranjeraNominal"		DECIMAL(26,8)
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
	                    SUM("InteresesRealesDepositosPositivo") AS "InteresesRealesDepositosPositivo",
	                    SUM("InteresesRealesDepositosNegativo") AS "InteresesRealesDepositosNegativo",
	                    SUM("InteresesRealesMantencionPositivo") AS "InteresesRealesMantencionPositivo",
	                    SUM("InteresesRealesMantencionNegativo") AS "InteresesRealesMantencionNegativo",
	                    SUM("InteresesRealesOperacionesCaptacionPositivo") AS "InteresesRealesOperacionesCaptacionPositivo",
	                    SUM("InteresesRealesOperacionesCaptacionNegativo") AS "InteresesRealesOperacionesCaptacionNegativo",
	                    SUM("InteresesMonedaNacionalExtranjeraNominal") AS "InteresesMonedaNacionalExtranjeraNominal",
	                    SUM("ImpuestoAdicionalMondaNacionalExtranjeraNominal") AS "ImpuestoAdicionalMondaNacionalExtranjeraNominal"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1890"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                select
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("InteresesRealesDepositosPositivo") AS "InteresesRealesDepositosPositivo",
	                    SUM("InteresesRealesDepositosNegativo") AS "InteresesRealesDepositosNegativo",
	                    SUM("InteresesRealesMantencionPositivo") AS "InteresesRealesMantencionPositivo",
	                    SUM("InteresesRealesMantencionNegativo") AS "InteresesRealesMantencionNegativo",
	                    SUM("InteresesRealesOperacionesCaptacionPositivo") AS "InteresesRealesOperacionesCaptacionPositivo",
	                    SUM("InteresesRealesOperacionesCaptacionNegativo") AS "InteresesRealesOperacionesCaptacionNegativo",
	                    SUM("InteresesMonedaNacionalExtranjeraNominal") AS "InteresesMonedaNacionalExtranjeraNominal",
	                    SUM("ImpuestoAdicionalMondaNacionalExtranjeraNominal") AS "ImpuestoAdicionalMondaNacionalExtranjeraNominal"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1890"
	                    WHERE "IdDocumento" = p_IdDocumento
	                    AND ("VboUsuario" LIKE p_VboUsuario or p_VboUsuario IS NULL)
	                    AND ("Observaciones" LIKE p_Observaciones or p_Observaciones IS NULL)
	                    AND ("FuenteDatos" LIKE p_FuenteDatos or p_FuenteDatos IS NULL)
	
	                    AND ("RutInversionista" LIKE p_RutInversionista OR p_RutInversionista IS NULL)
	                    AND ("IdCliente" >= p_IdCliente_Desde OR p_IdCliente_Desde IS NULL)
	                    AND ("IdCliente" <= p_IdCliente_Hasta OR p_IdCliente_Hasta IS NULL)
	                    AND ("IdPortafolio" >= p_IdPortafolio_Desde OR p_IdPortafolio_Desde IS NULL)
	                    AND ("IdPortafolio" <= p_IdPortafolio_Hasta OR p_IdPortafolio_Hasta IS NULL)
	                    AND ("InteresesRealesDepositosPositivo" >= p_InteresesRealesDepositosPositivo_Desde OR p_InteresesRealesDepositosPositivo_Desde IS NULL)
	                    AND ("InteresesRealesDepositosPositivo" <= p_InteresesRealesDepositosPositivo_Hasta OR p_InteresesRealesDepositosPositivo_Hasta IS NULL)
	                    AND ("InteresesRealesDepositosNegativo" >= p_InteresesRealesDepositosNegativo_Desde OR p_InteresesRealesDepositosNegativo_Desde IS NULL)
	                    AND ("InteresesRealesDepositosNegativo" <= p_InteresesRealesDepositosNegativo_Hasta OR p_InteresesRealesDepositosNegativo_Hasta IS NULL)
	                    AND ("InteresesRealesMantencionPositivo" >= p_InteresesRealesMantencionPositivo_Desde OR p_InteresesRealesMantencionPositivo_Desde IS NULL)
	                    AND ("InteresesRealesMantencionPositivo" <= p_InteresesRealesMantencionPositivo_Hasta OR p_InteresesRealesMantencionPositivo_Hasta IS NULL)
	                    AND ("InteresesRealesMantencionNegativo" >= p_InteresesRealesMantencionNegativo_Desde OR p_InteresesRealesMantencionNegativo_Desde IS NULL)
	                    AND ("InteresesRealesMantencionNegativo" <= p_InteresesRealesMantencionNegativo_Hasta OR p_InteresesRealesMantencionNegativo_Hasta IS NULL)
	                    AND ("InteresesRealesOperacionesCaptacionPositivo" >= p_InteresesRealesOperacionesCaptacionPositivo_Desde OR p_InteresesRealesOperacionesCaptacionPositivo_Desde IS NULL)
	                    AND ("InteresesRealesOperacionesCaptacionPositivo" <= p_InteresesRealesOperacionesCaptacionPositivo_Hasta OR p_InteresesRealesOperacionesCaptacionPositivo_Hasta IS NULL)
	                    AND ("InteresesRealesOperacionesCaptacionNegativo" >= p_InteresesRealesOperacionesCaptacionNegativo_Desde OR p_InteresesRealesOperacionesCaptacionNegativo_Desde IS NULL)
	                    AND ("InteresesRealesOperacionesCaptacionNegativo" <= p_InteresesRealesOperacionesCaptacionNegativo_Hasta OR p_InteresesRealesOperacionesCaptacionNegativo_Hasta IS NULL)
	                    AND ("InteresesMonedaNacionalExtranjeraNominal" >= p_InteresesMonedaNacionalExtranjeraNominal_Desde OR p_InteresesMonedaNacionalExtranjeraNominal_Desde IS NULL)
	                    AND ("InteresesMonedaNacionalExtranjeraNominal" <= p_InteresesMonedaNacionalExtranjeraNominal_Hasta OR p_InteresesMonedaNacionalExtranjeraNominal_Hasta IS NULL)
	                    AND ("ImpuestoAdicionalMondaNacionalExtranjeraNominal" >= p_ImpuestoAdicionalMondaNacionalExtranjeraNominal_Desde OR p_ImpuestoAdicionalMondaNacionalExtranjeraNominal_Desde IS NULL)
	                    AND ("ImpuestoAdicionalMondaNacionalExtranjeraNominal" <= p_ImpuestoAdicionalMondaNacionalExtranjeraNominal_Hasta OR p_ImpuestoAdicionalMondaNacionalExtranjeraNominal_Hasta IS NULL);
      ELSE
	    p_out_tbl = SELECT
	                    'TOTAL' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("InteresesRealesDepositosPositivo") AS "InteresesRealesDepositosPositivo",
	                    SUM("InteresesRealesDepositosNegativo") AS "InteresesRealesDepositosNegativo",
	                    SUM("InteresesRealesMantencionPositivo") AS "InteresesRealesMantencionPositivo",
	                    SUM("InteresesRealesMantencionNegativo") AS "InteresesRealesMantencionNegativo",
	                    SUM("InteresesRealesOperacionesCaptacionPositivo") AS "InteresesRealesOperacionesCaptacionPositivo",
	                    SUM("InteresesRealesOperacionesCaptacionNegativo") AS "InteresesRealesOperacionesCaptacionNegativo",
	                    SUM("InteresesMonedaNacionalExtranjeraNominal") AS "InteresesMonedaNacionalExtranjeraNominal",
	                    SUM("ImpuestoAdicionalMondaNacionalExtranjeraNominal") AS "ImpuestoAdicionalMondaNacionalExtranjeraNominal"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1890"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                SELECT
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("InteresesRealesDepositosPositivo") AS "InteresesRealesDepositosPositivo",
	                    SUM("InteresesRealesDepositosNegativo") AS "InteresesRealesDepositosNegativo",
	                    SUM("InteresesRealesMantencionPositivo") AS "InteresesRealesMantencionPositivo",
	                    SUM("InteresesRealesMantencionNegativo") AS "InteresesRealesMantencionNegativo",
	                    SUM("InteresesRealesOperacionesCaptacionPositivo") AS "InteresesRealesOperacionesCaptacionPositivo",
	                    SUM("InteresesRealesOperacionesCaptacionNegativo") AS "InteresesRealesOperacionesCaptacionNegativo",
	                    SUM("InteresesMonedaNacionalExtranjeraNominal") AS "InteresesMonedaNacionalExtranjeraNominal",
	                    SUM("ImpuestoAdicionalMondaNacionalExtranjeraNominal") AS "ImpuestoAdicionalMondaNacionalExtranjeraNominal"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1890"
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