SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ999.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ999.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 						BIGINT,

    -- ID usuario del login.
    IN p_IdUsuario 						BIGINT,

    -- ID modulo invocador.
    IN p_IdModulo 						NVARCHAR(3),

    ------------ Parametros de Entrada Propios ----------

    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID 							BIGINT,

    -- ID del documento.
    IN p_IdDocumento 					BIGINT,

    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario 					CHAR(1),

    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones 					NVARCHAR(250),

    -- Fuente de los datos.
    IN p_FuenteDatos 					NVARCHAR(40),

	IN p_IdOperacionSpot 				BIGINT,
	IN p_EventoReporte 					NVARCHAR(1),
	IN p_CodigoInstitucion 				BIGINT,
	IN p_NumeroOperacion 				BIGINT,
	IN p_FechaOperacion 				DATE,
	IN p_TipoDocumento 					BIGINT,
	IN p_CodigoComercio 				BIGINT,
	IN p_IdPais  						BIGINT,
	IN p_MonedaOperacion 				BIGINT,
	IN p_MontoOperacion 				DECIMAL(20,2),
	IN p_MontoDolar 					DECIMAL(20,2),
	IN p_TipoCambio 					DECIMAL(10,2),
	IN p_MontoEquiClp 					DECIMAL(20,2),
	IN p_AfectoADerivado 				BIGINT,
	IN p_DocumentoIdentidad 			NVARCHAR(10),
	IN p_NombreCompleto 				NVARCHAR(100), 
	IN p_TipoAutorizacion 				NVARCHAR(2) DEFAULT '',
	IN p_NumeroAutorizacion 			BIGINT DEFAULT 0,
	IN p_FechaAutorizacion 				DATE DEFAULT '0001-01-01',
	IN p_SectorEconomicoBeneficiario 	BIGINT DEFAULT 0,
	IN p_NumeroInscripcion 				BIGINT DEFAULT 0,
	IN p_FechaDesembolso 				DATE DEFAULT '0001-01-01',
	IN p_SectorEconomicoInversionista 	BIGINT DEFAULT 0,
	IN p_NombreFinancistaExtranjero 	NVARCHAR(100),
	IN p_FechaVencimientoCapital 		DATE DEFAULT '0001-01-01',
	IN p_NroContDerivado 				BIGINT DEFAULT 0,
	IN p_FechaSuscDerivado 				DATE DEFAULT '0001-01-01',
	IN p_FechaVencDerivado 				DATE DEFAULT '0001-01-01',
	IN p_Observacion NVARCHAR(250) 		DEFAULT '',
	IN p_EstadoGeneracion NVARCHAR(1) 	DEFAULT 'N',
    IN p_UsrUltMod  					NVARCHAR(40),
    IN p_FecUltMod  					DATE,
    IN p_EsArbitraje  					BIGINT DEFAULT 0,
	
    ------------ Parametros de Salida Propios ----------

    -- ID del registro.
    OUT p_out_id BIGINT,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador INT,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Variables Propias ----------

    ------------ Bloque Comun de Inicializacion ----------

    DECLARE salida_controlada condition;
    DECLARE EXIT handler FOR salida_controlada
    BEGIN
        SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
        SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
    END;

    DECLARE EXIT handler FOR sqlexception
    BEGIN
        DECLARE v_errorExt NVARCHAR(40);
        IF ::sql_error_code > 10000 THEN
            -- Error pseudo-controlado pero desde un sub-sp.
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle union all
                                   SELECT v_errorExt, ::sql_error_message FROM dummy;
            SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
        ELSE
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            resignal;
        END IF;
    END;

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
    p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1 = 2;

    ------------ Logica de Actualizacion ----------

    IF p_ID > 0 THEN
        p_out_id := p_ID;

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ999" SET
            "VboUsuario"					= p_VboUsuario,
            "Observaciones" 				= p_Observaciones,	
            "IdEmpresa"						= p_IdEmpresa,
            "EventoReporte"					= p_EventoReporte,
			"CodigoInstitucion"				= p_CodigoInstitucion,  		
			"NumeroOperacion"				= p_NumeroOperacion,
			"FechaOperacion"				= p_FechaOperacion,   	
			"TipoDocumento"			    	= p_TipoDocumento,   	
			"CodigoComercio"				= p_CodigoComercio,   	
			"IdPais"			        	= p_IdPais,   	
			"MonedaOperacion"				= p_MonedaOperacion,  	
			"MontoOperacion"				= p_MontoOperacion,  		
			"MontoDolar"					= p_MontoDolar,  		
			"TipoCambio"					= p_TipoCambio,  	
			"MontoEquiClp"					= p_MontoEquiClp,  	
			"AfectoADerivado"				= p_AfectoADerivado,  		
			"DocumentoIdentidad"			= p_DocumentoIdentidad,  		
			"NombreCompleto"				= p_NombreCompleto,  	
			--AUTORIZACION
			"TipoAutorizacion"				= p_TipoAutorizacion,  		
			"NumeroAutorizacion"			= p_NumeroAutorizacion,  		
			"FechaAutorizacion"				= p_FechaAutorizacion,  	
			--CREDITO EXTERNO
			"SectorEconomicoBeneficiario"	= p_SectorEconomicoBeneficiario,  	
			"NumeroInscripcion"				= p_NumeroInscripcion,  	
			"FechaDesembolso"				= p_FechaDesembolso,  		
			"SectorEconomicoInversionista"	= p_SectorEconomicoInversionista,  	
			"NombreFinancistaExtranjero"	= p_NombreFinancistaExtranjero,  			
			"FechaVencimientoCapital"		= p_FechaVencimientoCapital, 		
			--DERIVADOS
			"NroContDerivado"				= p_NroContDerivado, 		
			"FechaSuscDerivado"				= p_FechaSuscDerivado,  		
			"FechaVencDerivado"				= p_FechaVencDerivado,  		
			--OPERACION
			"Observacion"					= p_Observacion,
			--EXTRAS
			"EstadoGeneracion"				= 'N',
			"UsrUltMod"						= p_UsrUltMod,  			
			"FecUltMod"						= TO_DATE(CURRENT_DATE)		
		WHERE "ID" = p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ999";

        INSERT INTO "BSCL.Normativa.BD::TRI.DatosDJ999"(
            "ID",
            "IdDocumento",
            "VboUsuario",
            "Observaciones",
            "FuenteDatos",
            ---OPERACION
            "IdEmpresa",
            "IdOperacionSpot",
            "EventoReporte",			
			"CodigoInstitucion",	 		
			"NumeroOperacion",
			"FechaOperacion",
			"TipoDocumento",
			"CodigoComercio",
			"IdPais",
			"MonedaOperacion",
			"MontoOperacion",
			"MontoDolar",
			"TipoCambio",
			"MontoEquiClp",
			"AfectoADerivado",
			"DocumentoIdentidad",
			"NombreCompleto",
            --AUTORIZACION
			"TipoAutorizacion",	
			"NumeroAutorizacion",	
			"FechaAutorizacion",	
			--CREDITO EXTERNO
			"SectorEconomicoBeneficiario",
			"NumeroInscripcion",	
			"FechaDesembolso",	
			"SectorEconomicoInversionista",	
			"NombreFinancistaExtranjero",		
			"FechaVencimientoCapital",
			--DERIVADOS
			"NroContDerivado",	
			"FechaSuscDerivado",		
			"FechaVencDerivado",	
			--OPERACION
			"Observacion",	
			--EXTRAS
			"EstadoGeneracion",
			"UsrUltMod",		
			"FecUltMod",
			"EsArbitraje"
        ) VALUES (
            p_out_id,
            p_IdDocumento,
            'N',
            '',
       	    'MANTENEDOR',
            ---OPERACION
       	    p_IdEmpresa,
		    p_IdOperacionSpot,
			p_EventoReporte,
			p_CodigoInstitucion,
			CASE WHEN p_NumeroOperacion = 0 THEN "BSCL.Mesa.BD.Secuencias::SPT.FolioPlanillaSpot".NEXTVAL END,
			p_FechaOperacion,
			p_TipoDocumento,
			p_CodigoComercio,
			p_IdPais,
			p_MonedaOperacion,
			p_MontoOperacion,
			p_MontoDolar,
			p_TipoCambio,
			p_MontoEquiClp,
			p_AfectoADerivado,
			p_DocumentoIdentidad,
			p_NombreCompleto,
			--AUTORIZACION
			p_TipoAutorizacion,
			p_NumeroAutorizacion,
			p_FechaAutorizacion,
			--CREDITO EXTERNO
			p_SectorEconomicoBeneficiario,
			p_NumeroInscripcion,
			p_FechaDesembolso,
			p_SectorEconomicoInversionista,
			p_NombreFinancistaExtranjero,
			p_FechaVencimientoCapital,
			--DERIVADOS
			p_NroContDerivado,
			p_FechaSuscDerivado,
			p_FechaVencDerivado,
			--OPERACION
			p_Observacion,
			--EXTRAS
			p_EstadoGeneracion,
    		p_UsrUltMod,
    		p_FecUltMod,
    		p_EsArbitraje
        );
    END IF;
END;