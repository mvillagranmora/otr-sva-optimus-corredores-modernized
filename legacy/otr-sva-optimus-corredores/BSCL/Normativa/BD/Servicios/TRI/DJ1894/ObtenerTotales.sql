SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.ObtenerTotales"
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
    
    -- Filtro Rut Administradora
    IN p_RutAdministradora NVARCHAR(40) DEFAULT NULL,
    
    -- Filtro Rut Participe
    IN p_RutParticipe NVARCHAR(40) DEFAULT NULL,

    -- Rango del ID cliente.
    IN p_IdCliente_Desde BIGINT DEFAULT NULL,
    IN p_IdCliente_Hasta BIGINT DEFAULT NULL,

    -- Rango del ID portafolio.
    IN p_IdPortafolio_Desde BIGINT DEFAULT NULL,
    IN p_IdPortafolio_Hasta BIGINT DEFAULT NULL,
				
	-- Rango Monto Nominal Inversion
	IN p_MontoNominalInversion_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_MontoNominalInversion_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Monto Nominal Rescate
	IN p_MontoNominalRescate_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_MontoNominalRescate_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Monto Mayor Valor
	IN p_MayorValorActualizado_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_MayorValorActualizado_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Monto Menor Valor
	IN p_MenorValorActualizado_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_MenorValorActualizado_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Intereses Reales Operaciones negativo
	IN p_MontoReinvertido_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_MontoReinvertido_Hasta DECIMAL(26,8) DEFAULT NULL,
		
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
        

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 													NVARCHAR(100),
        "COUNT" 												BIGINT,
        "MontoNominalInversionActualizado" 								DECIMAL(26,8),
        "MontoNominalRescate" 									DECIMAL(26,8),
        "MayorValorActualizado"									DECIMAL(26,8),
        "MenorValorActualizado" 								DECIMAL(26,8),
        "MontoReinvertido" 										DECIMAL(26,8),
        "CostoUf"												DECIMAL(26,8)
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
	                    SUM("MontoNominalInversionActualizado") AS "MontoNominalInversionActualizado",
	                    SUM("MontoNominalRescate") AS "MontoNominalRescate",
	                    SUM("MayorValorActualizado") AS "MayorValorActualizado",
	                    SUM("MenorValorActualizado") AS "MenorValorActualizado",
	                    SUM("MontoReinvertido") AS "MontoReinvertido",
	                    SUM("CostoUf")	AS "CostoUf"
	                    FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                select
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("MontoNominalInversionActualizado") AS "MontoNominalInversionActualizado",
	                    SUM("MontoNominalRescate") AS "MontoNominalRescate",
	                    SUM("MayorValorActualizado") AS "MayorValorActualizado",
	                    SUM("MenorValorActualizado") AS "MenorValorActualizado",
	                    SUM("MontoReinvertido") AS "MontoReinvertido",
	                    SUM("CostoUf")	AS "CostoUf"
	                    FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos"
	                    WHERE "IdDocumento" = p_IdDocumento
	                    AND ("VboUsuario" LIKE p_VboUsuario or p_VboUsuario IS NULL)
	                    AND ("Observaciones" LIKE p_Observaciones or p_Observaciones IS NULL)
	                    AND ("FuenteDatos" LIKE p_FuenteDatos or p_FuenteDatos IS NULL)
	
--	                    AND ("RutAdministradora" LIKE p_RutAdministradora OR p_RutAdministradora IS NULL)
	                    AND ("RutParticipe" LIKE p_RutParticipe OR p_RutParticipe IS NULL)
	                    AND ("IdCliente" >= p_IdCliente_Desde OR p_IdCliente_Desde IS NULL)
	                    AND ("IdCliente" <= p_IdCliente_Hasta OR p_IdCliente_Hasta IS NULL)
	                    AND ("IdPortafolio" >= p_IdPortafolio_Desde OR p_IdPortafolio_Desde IS NULL)
	                    AND ("IdPortafolio" <= p_IdPortafolio_Hasta OR p_IdPortafolio_Hasta IS NULL)
	                    AND ("MontoNominalInversionActualizado" >= p_MontoNominalInversion_Desde OR p_MontoNominalInversion_Desde IS NULL)
	                    AND ("MontoNominalInversionActualizado" <= p_MontoNominalInversion_Hasta OR p_MontoNominalInversion_Hasta IS NULL)
	                    AND ("MontoNominalRescate" >= p_MontoNominalRescate_Desde OR p_MontoNominalRescate_Desde IS NULL)
	                    AND ("MontoNominalRescate" <= p_MontoNominalRescate_Hasta OR p_MontoNominalRescate_Hasta IS NULL)
	                    AND ("MayorValorActualizado" >= p_MayorValorActualizado_Desde OR p_MayorValorActualizado_Desde IS NULL)
	                    AND ("MayorValorActualizado" <= p_MayorValorActualizado_Hasta OR p_MayorValorActualizado_Hasta IS NULL)
	                    AND ("MenorValorActualizado" >= p_MenorValorActualizado_Desde OR p_MenorValorActualizado_Desde IS NULL)
	                    AND ("MenorValorActualizado" <= p_MenorValorActualizado_Hasta OR p_MenorValorActualizado_Hasta IS NULL)
	                    AND ("MontoReinvertido" >= p_MontoReinvertido_Desde OR p_MontoReinvertido_Desde IS NULL)
	                    AND ("MontoReinvertido" <= p_MontoReinvertido_Hasta OR p_MontoReinvertido_Hasta IS NULL);
      ELSE
	    p_out_tbl = SELECT
	                    'TOTAL' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("MontoNominalInversionActualizado") AS "MontoNominalInversionActualizado",
	                    SUM("MontoNominalRescate") AS "MontoNominalRescate",
	                    SUM("MayorValorActualizado") AS "MayorValorActualizado",
	                    SUM("MenorValorActualizado") AS "MenorValorActualizado",
	                    SUM("MontoReinvertido") AS "MontoReinvertido",
	                     SUM("CostoUf")	AS "CostoUf"
	                    FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                SELECT
	                    'FILTRO' AS "TYPE",
	                    COUNT(1) AS "COUNT",
	                    SUM("MontoNominalInversionActualizado") AS "MontoNominalInversionActualizado",
	                    SUM("MontoNominalRescate") AS "MontoNominalRescate",
	                    SUM("MayorValorActualizado") AS "MayorValorActualizado",
	                    SUM("MenorValorActualizado") AS "MenorValorActualizado",
	                    SUM("MontoReinvertido") AS "MontoReinvertido",
                        SUM("CostoUf")	AS "CostoUf"
	                    FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos"
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