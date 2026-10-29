SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa BIGINT,

    -- ID usuario del login.
    IN p_IdUsuario BIGINT,

    -- ID modulo invocador.
    IN p_IdModulo NVARCHAR(3),

    ------------ Parametros de Entrada Propios ----------

    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID BIGINT,

    -- ID del documento.
    IN p_IdDocumento BIGINT,

    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario CHAR(1),
		--IN p_VboUsuario 			NVARCHAR(1),

    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones NVARCHAR(250),

    -- Fuente de los datos.
    IN p_FuenteDatos NVARCHAR(40),

    IN p_Seccion			BIGINT DEFAULT 1,
    IN p_KRutCtraparte1  		NVARCHAR(11),
    IN p_KIdContrato  			NVARCHAR(52),
    IN p_KFechaContrato  		DATE,
    IN p_HoraContrato			CHAR(8),
    IN p_KIdOpeEstruc 			NVARCHAR(2),
    IN p_S1_NaturaInfo  		NVARCHAR(1),
    IN p_S1_EventoReporte   		NVARCHAR(3),
    IN p_S1_LEICtraparte1   		NVARCHAR(20),
    IN p_S1_RutCtraparte2   		NVARCHAR(11),
    IN p_S1_LEICtraparte2   		NVARCHAR(20),
    IN p_S1_NomCtraparte2   		NVARCHAR(150),
    IN p_S1_PaisCtraparte2  		NVARCHAR(3),
    IN p_S1_RutEntidad  		NVARCHAR(11),
    IN p_S1_LEIEntidad  		NVARCHAR(20),
    IN p_S1_RUTLiqCtraparte1  		NVARCHAR(11),
    IN p_S1_LEILiqCtraparte1  		NVARCHAR(20),
    IN p_S1_RutAgenteCal  		NVARCHAR(11),
    IN p_S1_LEIAgenteCal  		NVARCHAR(20),
    IN p_S1_NomAgenteCal  		NVARCHAR(150),
    IN p_S1_RutCedente  		NVARCHAR(11),
    IN p_S1_LEICedente  		NVARCHAR(20),
    IN p_S1_PlataformaNego  		NVARCHAR(4),
    IN p_S1_RutIntermediario  		NVARCHAR(11),
    IN p_S1_LEIIntermediario  		NVARCHAR(20),
    IN p_S2_Instrumento  		NVARCHAR(3),
    IN p_S2_IdContratoAgente  		NVARCHAR(52),
    IN p_S2_OpsClase  			NVARCHAR(2),
    IN p_S2_OpPosicionOp  		NVARCHAR(4),
    IN p_S2_ModalidadLiq 		NVARCHAR(2),
    IN p_S2_MonedaComp  		NVARCHAR(3),
    IN p_S2_FechaInicio  		DATE,
    IN p_S2_FechaFixing1  		DATE,
    IN p_S2_FechaTermino  		DATE,
    IN p_S2_FechaPagOpe  		DATE,
    IN p_S2_FechaInicioMod  		DATE,
    IN p_S2_MonedaPrima  		NVARCHAR(3),
    IN p_S2_MontoPrima  		DECIMAL(20,5),
    IN p_S2_ObjetivoOpe  		NVARCHAR(3),
    IN p_S2_ConvenioMarco  		NVARCHAR(25),
    IN p_S2_PaisContrato  		NVARCHAR(3),
    IN p_S2_Garantias 			NVARCHAR(2),
    IN p_S2_ClausulaRecou  		NVARCHAR(1),
    IN p_S2_ClausulaTermino  		NVARCHAR(3),
    IN p_S2_FechaEfecTermino  		DATE,
    IN p_S2_CompresionCartera  		NVARCHAR(1),
    IN p_S2_NumTotIntercam  		BIGINT,
    IN p_S2_NumTotPagos  		BIGINT,
    IN p_S3_XR_OpInstruSub		NVARCHAR(3),
    IN p_S3_MonedaComprada  		NVARCHAR(3),
    IN p_S3_MontoComprada  		DECIMAL(20,5),
    IN p_S3_XR_MonedaVendida  		NVARCHAR(3),
    IN p_S3_XR_MontoVendida  		DECIMAL(20,5),
    IN p_S3_XR_TipoTasaEnt		NVARCHAR(7),
    IN p_S3_XR_TipoTasaRec		NVARCHAR(7),	
    IN p_S3_R_TasaFija		  	DECIMAL(14,10),
    IN p_S3_R_Spread		  	DECIMAL(14,10),
    IN p_S3_I_TipoActivoSub  		NVARCHAR(7),
    IN p_S3_I_IdActivoSuby  		NVARCHAR(12),
    IN p_S3_I_TasaPactada  		DECIMAL(14,10),
    IN p_S3_XR_FechaFixing2  		DATE,
    IN p_S3_XR_ConvTipoCambio  		NVARCHAR(7),
    IN p_S3_PrecioFuturo  		DECIMAL(20,5),
    IN p_S3_XR_PuntosForward  		DECIMAL(14,10),
    IN p_S3_XR_PrecioEntrada  		DECIMAL(20,5),
    IN p_S3_XR_PrecioSalida  		DECIMAL(20,5),
    IN p_S3_I_PosicionOpe  		NVARCHAR(4),
    IN p_S8_MtodoValoriza  		NVARCHAR(1),
    IN p_S8_MonedaValoriza  		NVARCHAR(3),
    IN p_S8_ValorMercado  		DECIMAL(20,5),
    IN p_S8_OpVolatilidad  		DECIMAL(14,10),
    IN p_S8_OpDelta  			DECIMAL(14,10),
    IN p_S8_OpGamma  			DECIMAL(14,10),
    IN p_S8_OpVega  			DECIMAL(14,10),
    IN p_S8_AjusteRiesgo 	 	DECIMAL(20,5),
    IN p_S8_AjusteBID   		DECIMAL(20,5),
    IN p_S8_OtrosAjustes   		DECIMAL(20,5),
	
    IN p_KS4_XR_NroFlu			BIGINT DEFAULT 0,
    IN p_S4_XR_TipoFlujo		CHAR(1)	DEFAULT '',
    IN p_S4_XR_FacTasa			DECIMAL(14,10) DEFAULT 0,
    IN p_S4_XR_SpreadTasa		DECIMAL(14,10) DEFAULT 0,
    IN p_S4_XR_TasaFija			DECIMAL(14,10) DEFAULT 0,
    IN p_S4_XR_FecFixTasa		DATE DEFAULT '0001-01-01',
    IN p_S4_XR_FecIni			DATE DEFAULT '0001-01-01',
    IN p_S4_XR_FecTer			DATE DEFAULT '0001-01-01',
    IN p_S4_XR_MontoNoc			DECIMAL(20, 5) DEFAULT 0,
    IN p_S4_XR_MontoInt			DECIMAL(20, 5) DEFAULT 0,
    IN p_S4_X_MonMonto			NVARCHAR(3) DEFAULT '',
	
    IN p_KS5_CorrPago			DECIMAL(5, 0) DEFAULT 0,
    IN p_KS5_IdPago			CHAR(1)	DEFAULT '',
    IN p_S5_FecPago			DATE DEFAULT '0001-01-01',
    IN p_S5_MontoPago			DECIMAL(20, 5) DEFAULT 0,
    IN p_S5_TipoPago			CHAR(1)	DEFAULT '',
    IN p_S6_TipoGar			NVARCHAR(1) DEFAULT '',	
    IN p_S6_MonVal			NVARCHAR(3) DEFAULT '',
    IN p_KS6_IdGar			NVARCHAR(52) DEFAULT '',	
    IN p_S6_MontoUmb			DECIMAL(20, 5) DEFAULT 0,
    IN p_S6_ValGtia			DECIMAL(20, 5) DEFAULT 0,
	
    IN p_S7_TipoGar			NVARCHAR(1) DEFAULT '',
    IN p_KS7_IdGar			NVARCHAR(52) DEFAULT '',	
    IN p_S7_TipoActGar			CHAR(4) DEFAULT '',
    IN p_S7_PorcGar			DECIMAL(3, 0) DEFAULT 0,		

    IN p_UsrUltMod  			NVARCHAR(40),
    IN p_FecUltMod  			NVARCHAR(10),
    IN p_Estado  			NVARCHAR(1),
    IN p_kProducto  			NVARCHAR(10),
    IN p_kPeriodoComercial		DATE,
	
	
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

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1" SET
            "VboUsuario"				= p_VboUsuario,
            "Observaciones" 			= p_Observaciones,		
            "HoraContrato"				= p_HoraContrato,
			"S1_NaturaInfo"				= p_S1_NaturaInfo,  		
			"S1_EventoReporte"			= p_S1_EventoReporte,
			"S1_LEICtraparte1"			= p_S1_LEICtraparte1,   	
			"S1_RutCtraparte2"			= p_S1_RutCtraparte2,   	
			"S1_LEICtraparte2"			= p_S1_LEICtraparte2,   	
			"S1_NomCtraparte2"			= p_S1_NomCtraparte2,   	
			"S1_PaisCtraparte2"			= p_S1_PaisCtraparte2,  	
			"S1_RutEntidad"				= p_S1_RutEntidad,  		
			"S1_LEIEntidad"				= p_S1_LEIEntidad,  		
			"S1_RUTLiqCtraparte1"		= p_S1_RUTLiqCtraparte1,  	
			"S1_LEILiqCtraparte1"		= p_S1_LEILiqCtraparte1,  	
			"S1_RutAgenteCal"			= p_S1_RutAgenteCal,  		
			"S1_LEIAgenteCal"			= p_S1_LEIAgenteCal,  		
			"S1_NomAgenteCal"			= p_S1_NomAgenteCal,  		
			"S1_RutCedente"				= p_S1_RutCedente,  		
			"S1_LEICedente"				= p_S1_LEICedente,  		
			"S1_PlataformaNego"			= p_S1_PlataformaNego,  	
			"S1_RutIntermediario"		= p_S1_RutIntermediario,  	
			"S1_LEIIntermediario"		= p_S1_LEIIntermediario,  	
			"S2_Instrumento"			= p_S2_Instrumento,  		
			"S2_IdContratoAgente"		= p_S2_IdContratoAgente,  	
			"S2_OpsClase"				= p_S2_OpsClase,  			
			"S2_OpPosicionOp"			= p_S2_OpPosicionOp, 		
			"S2_ModalidadLiq"			= p_S2_ModalidadLiq, 		
			"S2_MonedaComp"				= p_S2_MonedaComp,  		
			"S2_FechaInicio"			= p_S2_FechaInicio,  		
			"S2_FechaFixing1"			= p_S2_FechaFixing1, 		
			"S2_FechaTermino"			= p_S2_FechaTermino,  		
			"S2_FechaPagOpe"			= p_S2_FechaPagOpe,  		
			"S2_FechaInicioMod"			= p_S2_FechaInicioMod,  	
			"S2_MonedaPrima"			= p_S2_MonedaPrima,  		
			"S2_MontoPrima"				= p_S2_MontoPrima,  	
			"S2_ObjetivoOpe"			= p_S2_ObjetivoOpe,  		
			"S2_ConvenioMarco"			= p_S2_ConvenioMarco,
			"S2_PaisContrato"			= p_S2_PaisContrato,  		
			"S2_Garantias"				= p_S2_Garantias, 			
			"S2_ClausulaRecou"			= p_S2_ClausulaRecou,  	
			"S2_ClausulaTermino"		= p_S2_ClausulaTermino,  	
			"S2_FechaEfecTermino"		= p_S2_FechaEfecTermino,  	
			"S2_CompresionCartera"		= p_S2_CompresionCartera,  
			"S2_NumTotIntercam"			= p_S2_NumTotIntercam,  	
			"S2_NumTotPagos"			= p_S2_NumTotPagos,  		
			"S3_XR_OpInstruSub"			= p_S3_XR_OpInstruSub,
			"S3_MonedaComprada"			= p_S3_MonedaComprada,  	
			"S3_MontoComprada"			= p_S3_MontoComprada,  	
			"S3_XR_MonedaVendida"		= p_S3_XR_MonedaVendida,  	
			"S3_XR_MontoVendida"		= p_S3_XR_MontoVendida,  	
			"S3_XR_TipoTasaEnt"			= p_S3_XR_TipoTasaEnt,
			"S3_XR_TipoTasaRec"			= p_S3_XR_TipoTasaRec,  	
			"S3_R_TasaFija"				= p_S3_R_TasaFija,  	
			"S3_R_Spread"				= p_S3_R_Spread,  	
			"S3_I_TipoActivoSub"		= p_S3_I_TipoActivoSub,  	
			"S3_I_IdActivoSuby"			= p_S3_I_IdActivoSuby,  	
			"S3_I_TasaPactada"			= p_S3_I_TasaPactada,  	
			"S3_XR_FechaFixing2"		= p_S3_XR_FechaFixing2,  	
			"S3_XR_ConvTipoCambio"		= p_S3_XR_ConvTipoCambio,  
			"S3_PrecioFuturo"			= p_S3_PrecioFuturo,		
			"S3_XR_PuntosForward"		= p_S3_XR_PuntosForward,
			"S3_XR_PrecioEntrada"		= p_S3_XR_PrecioEntrada,  	
			"S3_XR_PrecioSalida"		= p_S3_XR_PrecioSalida,  	
			"S3_I_PosicionOpe"			= p_S3_I_PosicionOpe,  	
			"S8_MtodoValoriza"			= p_S8_MtodoValoriza,  	
			"S8_MonedaValoriza"			= p_S8_MonedaValoriza,  	
			"S8_ValorMercado"			= p_S8_ValorMercado,  		
			"S8_OpVolatilidad"			= p_S8_OpVolatilidad,  	
			"S8_OpDelta"				= p_S8_OpDelta,  			
			"S8_OpGamma"				= p_S8_OpGamma,  			
			"S8_OpVega"					= p_S8_OpVega,  			
			"S8_AjusteRiesgo"			= p_S8_AjusteRiesgo, 	 	
			"S8_AjusteBID"				= p_S8_AjusteBID,   		
			"S8_OtrosAjustes"			= p_S8_OtrosAjustes,   
			"S4_XR_TipoFlujo"			= p_S4_XR_TipoFlujo, 
			"S4_XR_FacTasa"				= p_S4_XR_FacTasa,
			"S4_XR_SpreadTasa"			= p_S4_XR_SpreadTasa,
			"S4_XR_TasaFija"			= p_S4_XR_TasaFija,
			"S4_XR_FecFixTasa"			= p_S4_XR_FecFixTasa,
			"S4_XR_FecIni"				= p_S4_XR_FecIni,
			"S4_XR_FecTer"				= p_S4_XR_FecTer,
			"S4_XR_MontoNoc"			= p_S4_XR_MontoNoc,
			"S4_XR_MontoInt"			= p_S4_XR_MontoInt,
			"S4_X_MonMonto"				= p_S4_X_MonMonto,
			"S5_FecPago"				= p_S5_FecPago,
			"S5_MontoPago"				= p_S5_MontoPago,
			"S5_TipoPago"				= p_S5_TipoPago,
			"S6_TipoGar"				= p_S6_TipoGar,
			"S6_MonVal"					= p_S6_MonVal,
			"S6_MontoUmb"				= p_S6_MontoUmb,
			"S6_ValGtia"				= p_S6_ValGtia,
			"S7_TipoGar"				= p_S7_TipoGar,
			"S7_TipoActGar"				= p_S7_TipoActGar,
			"S7_PorcGar"				= p_S7_PorcGar,	
			"UsrUltMod"					= p_UsrUltMod,  			
			"FecUltMod"					= CURRENT_DATE,--p_FecUltMod,  			
			"Estado"					= 'M'
		WHERE "ID" = p_ID
		AND "Seccion" = p_Seccion
	;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1";

        INSERT INTO "BSCL.Normativa.BD::TRI.DatosDJ1"(
            "ID",
            "IdDocumento",
            "VboUsuario",
            "Observaciones",
            "FuenteDatos",
            ---		    
            "Seccion",
            "KRutCtraparte1", 
            "KIdContrato", 
            "KFechaContrato", 
            "HoraContrato",
            "KIdOpeEstruc", 
            "S1_NaturaInfo", 
            "S1_EventoReporte", 
            "S1_LEICtraparte1", 
            "S1_RutCtraparte2", 
            "S1_LEICtraparte2", 
            "S1_NomCtraparte2", 
            "S1_PaisCtraparte2", 
            "S1_RutEntidad", 
            "S1_LEIEntidad", 
            "S1_RUTLiqCtraparte1", 
            "S1_LEILiqCtraparte1", 
            "S1_RutAgenteCal", 
            "S1_LEIAgenteCal", 
            "S1_NomAgenteCal", 
            "S1_RutCedente", 
            "S1_LEICedente", 
            "S1_PlataformaNego", 
            "S1_RutIntermediario", 
            "S1_LEIIntermediario", 
            "S2_Instrumento", 
            "S2_IdContratoAgente", 
            "S2_OpsClase", 
            "S2_OpPosicionOp", 
            "S2_ModalidadLiq",
            "S2_MonedaComp", 
            "S2_FechaInicio", 
            "S2_FechaFixing1", 
            "S2_FechaTermino", 
            "S2_FechaPagOpe", 
            "S2_FechaInicioMod", 
            "S2_MonedaPrima", 
            "S2_MontoPrima", 
            "S2_ObjetivoOpe", 
            "S2_ConvenioMarco", 
            "S2_PaisContrato", 
            "S2_Garantias",
            "S2_ClausulaRecou", 
            "S2_ClausulaTermino", 
            "S2_FechaEfecTermino", 
            "S2_CompresionCartera", 
            "S2_NumTotIntercam", 
            "S2_NumTotPagos", 
            "S3_XR_OpInstruSub", 
            "S3_MonedaComprada", 
            "S3_MontoComprada", 
            "S3_XR_MonedaVendida", 
            "S3_XR_MontoVendida", 
            "S3_XR_TipoTasaEnt",
            "S3_XR_TipoTasaRec",  
            "S3_R_TasaFija",  
            "S3_R_Spread", 
            "S3_I_TipoActivoSub", 
            "S3_I_IdActivoSuby", 
            "S3_I_TasaPactada", 
            "S3_XR_FechaFixing2", 
            "S3_XR_ConvTipoCambio", 
            "S3_PrecioFuturo", 
            "S3_XR_PuntosForward", 
            "S3_XR_PrecioEntrada", 
            "S3_XR_PrecioSalida", 
            "S3_I_PosicionOpe", 
            "S8_MtodoValoriza", 
            "S8_MonedaValoriza", 
            "S8_ValorMercado", 
            "S8_OpVolatilidad", 
            "S8_OpDelta", 
            "S8_OpGamma", 
            "S8_OpVega", 
            "S8_AjusteRiesgo", 
            "S8_AjusteBID", 
            "S8_OtrosAjustes",             
            "KS4_XR_NroFlu",
			"S4_XR_TipoFlujo",
			"S4_XR_FacTasa",
			"S4_XR_SpreadTasa",
			"S4_XR_TasaFija",
			"S4_XR_FecFixTasa",
			"S4_XR_FecIni",
			"S4_XR_FecTer",
			"S4_XR_MontoNoc",
			"S4_XR_MontoInt",
			"S4_X_MonMonto",
			"KS5_CorrPago",
			"KS5_IdPago",
			"S5_FecPago",
			"S5_MontoPago",
			"S5_TipoPago",
			"S6_TipoGar",
			"S6_MonVal",
			"KS6_IdGar",
			"S6_MontoUmb",
			"S6_ValGtia",
			"S7_TipoGar",
			"KS7_IdGar",
			"S7_TipoActGar",
			"S7_PorcGar",	            
            "UsrUltMod", 
            "FecUltMod", 
            "Estado", 
            "kProducto", 
            "kPeriodoComercial"
        ) VALUES (
            p_out_id,
            p_IdDocumento,
            'N',
            '',
       	    'MANTENEDOR',
        ---	
        	p_Seccion,
	        p_KRutCtraparte1,  			
			p_KIdContrato,			
			p_KFechaContrato,
			p_HoraContrato,
			p_KIdOpeEstruc, 			
			p_S1_NaturaInfo,  		
			p_S1_EventoReporte,   	
			p_S1_LEICtraparte1,   	
			p_S1_RutCtraparte2,   		
			p_S1_LEICtraparte2,   	
			p_S1_NomCtraparte2,   	
			p_S1_PaisCtraparte2,  	
			p_S1_RutEntidad,  		
			p_S1_LEIEntidad,  		
			p_S1_RUTLiqCtraparte1,  	
			p_S1_LEILiqCtraparte1,  	
			p_S1_RutAgenteCal,  		
			p_S1_LEIAgenteCal,  		
			p_S1_NomAgenteCal,  		
			p_S1_RutCedente,  		
			p_S1_LEICedente,  		
			p_S1_PlataformaNego,
			p_S1_RutIntermediario,  	
			p_S1_LEIIntermediario,  	
			p_S2_Instrumento,  		
			p_S2_IdContratoAgente,  	
			p_S2_OpsClase,  			
			p_S2_OpPosicionOp, 		
			p_S2_ModalidadLiq, 		
			p_S2_MonedaComp,  		
			p_S2_FechaInicio,  		
			p_S2_FechaFixing1,  		
			p_S2_FechaTermino,  		
			p_S2_FechaPagOpe,  		
			p_S2_FechaInicioMod,  	
			p_S2_MonedaPrima,  		
			p_S2_MontoPrima,  	
			p_S2_ObjetivoOpe,  		
			p_S2_ConvenioMarco,  	
			p_S2_PaisContrato,  		
			p_S2_Garantias, 			
			p_S2_ClausulaRecou,  	
			p_S2_ClausulaTermino,  	
			p_S2_FechaEfecTermino,  	
			p_S2_CompresionCartera,  
			p_S2_NumTotIntercam,  	
			p_S2_NumTotPagos,  		
			p_S3_XR_OpInstruSub,     
			p_S3_MonedaComprada,  	
			p_S3_MontoComprada,  	
			p_S3_XR_MonedaVendida,  	
			p_S3_XR_MontoVendida,  	
			p_S3_XR_TipoTasaEnt,
			p_S3_XR_TipoTasaRec,  	
			p_S3_R_TasaFija,  	
			p_S3_R_Spread,  	
			p_S3_I_TipoActivoSub,  	
			p_S3_I_IdActivoSuby,  	
			p_S3_I_TasaPactada,  	
			p_S3_XR_FechaFixing2,  	
			p_S3_XR_ConvTipoCambio, 
			p_S3_PrecioFuturo,  		
			p_S3_XR_PuntosForward,  	
			p_S3_XR_PrecioEntrada,  	
			p_S3_XR_PrecioSalida,  	
			p_S3_I_PosicionOpe,  	
			p_S8_MtodoValoriza,  	
			p_S8_MonedaValoriza,  	
			p_S8_ValorMercado,  		
			p_S8_OpVolatilidad,  	
			p_S8_OpDelta,  			
			p_S8_OpGamma,  			
			p_S8_OpVega,  			
			p_S8_AjusteRiesgo, 	 	
			p_S8_AjusteBID,   		
			p_S8_OtrosAjustes,   	
			p_KS4_XR_NroFlu,
			p_S4_XR_TipoFlujo,
			p_S4_XR_FacTasa,
			p_S4_XR_SpreadTasa,
			p_S4_XR_TasaFija,
			p_S4_XR_FecFixTasa,
			p_S4_XR_FecIni,
			p_S4_XR_FecTer,
			p_S4_XR_MontoNoc,
			p_S4_XR_MontoInt,
			p_S4_X_MonMonto,
			p_KS5_CorrPago,
			p_KS5_IdPago,
			p_S5_FecPago,
			p_S5_MontoPago,
			p_S5_TipoPago,
			p_S6_TipoGar,	
			p_S6_MonVal,
			p_KS6_IdGar,	
			p_S6_MontoUmb,
			p_S6_ValGtia,	
			p_S7_TipoGar,
			p_KS7_IdGar,	
			p_S7_TipoActGar,
			p_S7_PorcGar,		
			p_UsrUltMod,  			
			CURRENT_DATE,--p_FecUltMod,  			
			p_Estado, 				
			p_kProducto,  						 			
			p_kPeriodoComercial
        );
    END IF;
END;