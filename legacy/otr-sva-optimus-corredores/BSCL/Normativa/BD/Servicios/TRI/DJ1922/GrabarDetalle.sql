SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 									BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 									BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 									NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID 										BIGINT		 ,
    -- ID del documento.
    IN p_IdDocumento 								BIGINT		 ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario 								CHAR(1)		 ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones 								NVARCHAR(250),
    -- Fuente de los datos.
    IN p_FuenteDatos 								NVARCHAR(40) ,
    
    
	IN p_Numero												BIGINT			,
	IN p_IdOperacion										BIGINT			,
	IN p_RutAdministradora									NVARCHAR(40)	,
	IN p_IdEmisor											NVARCHAR(40)	,
	IN p_RutFondo											NVARCHAR(40)	,
	IN p_RunFondo											NVARCHAR(40)	,
	IN p_Fondo												NVARCHAR(100)	,
	IN p_TipoFondo											NVARCHAR(100)	,
	IN p_RutCliente											NVARCHAR(40)	,
	IN p_NombreParticipe									NVARCHAR(250)	,						
	IN p_TaxId												NVARCHAR(250)	,
	IN p_CodigoPais											NVARCHAR(100)	,
	IN p_TipoOperacion										NVARCHAR(100)	,
	IN p_FechaInversion										DATE			,
	IN p_MonedaReajuste										NVARCHAR(100)	,
	IN p_MontoNominalAportes								DECIMAL(26,8)	,
	IN p_MontoNominalAportesActualizado						DECIMAL(26,8)	,
	IN p_NumeroSerieCuotas									BIGINT			,
	IN p_CantidadCuotasParticipe							DECIMAL(26,8)	,
	IN p_ParticipeCumpleRequisitos							BIGINT			,
	IN p_CuotasCumplenRequisitos							BIGINT			,
	IN p_ValorCuotaPesos									DECIMAL(26,8)	,
	IN p_NumCuotasSerieParticipe							DECIMAL(26,8)	,
	IN p_PorcentajeParticipacion31Dic						DECIMAL(26,8)	,
	IN p_ValNominalCuotasRescatadas							DECIMAL(26,8)	,
	IN p_CantidadCuotasRescatadas							DECIMAL(26,8)	,
	IN p_DiferenciaPorRescateOEnajenacion					DECIMAL(26,8)	,
	IN p_DiferenciaPorRescateOEnajenacionActualizado		DECIMAL(26,8)	,
	IN p_DifPorRescateOEnajenacionCFIAcogido107				DECIMAL(26,8)	,
	IN p_DifPorRescateOEnajenacionCFIAcogido107Actualizado	DECIMAL(26,8)	,
	IN p_ValNominalInversionaFechaLiquidacion				DECIMAL(26,8)	,
	IN p_CantCuotasCedidasPorLiquidacion					DECIMAL(26,8)	,
	IN p_DiferenciaValorCuotasFechaLiquidacion				DECIMAL(26,8)	,
	IN p_DiferenciaValorCuotasFechaLiquidacionActualizado	DECIMAL(26,8)	,
	IN p_TributaSa											NVARCHAR(1)		,
	IN p_DivRemDistAfectGCConIDPC  							DECIMAL(26,8)	,
	IN p_DivRemDistAfectGCConIDPCal31122016  				DECIMAL(26,8)	,--DivRemDistAfectGCConIDPCal31122016
	IN p_DivRemDistAfectGCConIDPCVOL  						DECIMAL(26,8)	,--DivRemDistAfectGCConIDPCVOL
	IN p_DivRemDistAfectGCSINDC 							DECIMAL(26,8)	, --DivRemDistAfectGCSINDC
	
	IN p_DisRemRentasTribCProvRap_C96  						DECIMAL(26,8)	,--DisRemRentasTribCProvRap_C96
	IN p_DisRemRentasTribCORP_C97  							DECIMAL(26,8)	,--DisRemRentasTribCORP_C97
	IN p_DisRemRentasTribCExDD_C98  						DECIMAL(26,8)	,--DisRemRentasTribCExDD_C98
	IN p_DisRemRentasTribCUtAFFut_C99  						DECIMAL(26,8)	,--DisRemRentasTribCUtAFFut_C99	
	IN p_DivRemDistNoRentaFUT  								DECIMAL(26,8)	,--DivRemDistNoRentaFUT_C70
	
	IN p_DisRemRentasExIGCAfecImptoAdi_C100  				DECIMAL(26,8)	,--DisRemRentasExIGCAfecImptoAdi_C100
	
	IN p_CredImpGloCompNSRSD  								DECIMAL(26,8)	,--CredImpGloCompNSRSD_C71  
	IN p_CredImpGloCompNSRCD  								DECIMAL(26,8)	,--CredImpGloCompNSRCD_C72
	
	IN p_CredIGCAcumD01012017RANSRD01012020SDD_C101  		DECIMAL(26,8)	,--CredIGCAcumD01012017RANSRD01012020SDD_C101
	IN p_CredIGCAcumD01012017RANSRD01012020CDD_C102  		DECIMAL(26,8)	,--CredIGCAcumD01012017RANSRD01012020CDD_C102
	
	IN p_CredImpGloCompSRSD  								DECIMAL(26,8)	,--CredImpGloCompSRSD_C73
	IN p_CredImpGloCompSRCD  								DECIMAL(26,8)	,--CredImpGloCompSRCD_C74
	
	IN p_CredIGCAcumD01012017RXSRSDD_C103  					DECIMAL(26,8)	,--CredIGCAcumD01012017RXSRSDD_C103
	IN p_CredIGCAcumD01012017RXSRCDD_C104  					DECIMAL(26,8)	,--CredIGCAcumD01012017RXSRCDD_C104
	
	IN p_CredImpGloCompCTDCIF  								DECIMAL(26,8)	,--CredImpGloCompCTDCIF_C75
	
	IN p_CredImpGloCompCDD31122016  						DECIMAL(26,8)	,--CredImpGloCompCDD31122016_C76
	IN p_CredImpGloCompSDD31122016  						DECIMAL(26,8)	,--CredImpGloCompSDD31122016_C77
	
	IN p_CredIGCAcumH31122016RXSDD_C105  					DECIMAL(26,8)	,--CredIGCAcumH31122016RXSDD_C105
	IN p_CredIGCAcumH31122016RXCDD_C106  					DECIMAL(26,8)	,--CredIGCAcumH31122016RXCDD_C106
	IN p_CredImpGloCompCTDCIF31122016  						DECIMAL(26,8)	,--CredImpGloCompCTDCIF31122016_C78
	IN p_CredImpGloCompCITAExArt21  						DECIMAL(26,8)	,--CredImpGloCompCITAExArt21_C79 
	IN p_DetBenNetoPerRutFdo_C107 							NVARCHAR(40)	,--DetBenNetoPerRutFdo_C107
	IN p_DetBenNetoPerPOS_C113  							DECIMAL(26,8)	,--DetBenNetoPerPOS_C113
	IN p_DetBenNetoPerNEG_C114  							DECIMAL(26,8)	,--DetBenNetoPerNEG_C114
	IN p_DividendosExentosIGCoAdicional						DECIMAL(26,8)	,--antes
	IN p_DividendosExentosIGCoAdicionalActualizado			DECIMAL(26,8)	,
	IN p_DividendosNoConstitutivosDeRenta					DECIMAL(26,8)	,
	IN p_DividendosNoConstitutivosDeRentaActualizado		DECIMAL(26,8)	,
	IN p_DivRemDistNoRentaFE 								DECIMAL(26,8)	,--DivRemDistNoRentaFE_C31
	IN p_DividendosAfectosImptoUnicoTasa10					DECIMAL(26,8)	,
	IN p_DividendosAfectosImptoUnicoTasa10Actualizado		DECIMAL(26,8)	,
	IN p_DividendosAfectosImptoUnicoTasa4					DECIMAL(26,8)	,
	IN p_DividendosAfectosImptoUnicoTasa4Actualizado		DECIMAL(26,8)	,
	IN p_DistribucionXDiminucionNoImputadaCapital			DECIMAL(26,8)	,
	IN p_DevolucionesDeCapital								DECIMAL(26,8)	,
	IN p_DevolucionesDeCapitalActualizado					DECIMAL(26,8)	,
	IN p_MontoInversionenCuotasCFIAnterior93				DECIMAL(26,8)	,
	IN p_NumeroCertificado									BIGINT			,
	IN p_FactorActualizacion								DECIMAL(26,8)	,
	IN p_FechaOrigenInversion								DATE			,
	IN p_TipoDocumento 										NVARCHAR(10),
	IN p_NumeroDocumento 									BIGINT,
	IN p_FechaDocumento 									DATE,
	IN p_IdBaseDocumento 									BIGINT,
	--IN p_IdCliente 											BIGINT,
	--IN p_IdPortafolio 										BIGINT,
	IN p_TipoTransaccion 									NVARCHAR(6),
	IN p_IdTipoOperacion 									NVARCHAR(6),
	IN p_IdTipoProducto 									NVARCHAR(4),
	IN p_SecEvento											BIGINT,
	IN p_AcogidoISFUT		NVARCHAR(1),
	IN p_DetBenNetoUtilPer_C133  									DECIMAL(26,8)	,--DetBenNetoUtilPer_C133
	IN p_DetBenNetoIntPer_C134  									DECIMAL(26,8)	,--DetBenNetoIntPer_C134
	IN p_DetBenNetoDivPer_C135 									DECIMAL(26,8)	,--DetBenNetoDivPer_C135
	IN p_DetBenNetoGanCapPer_C136  								DECIMAL(26,8)	,--DetBenNetoGanCapPer_C136
	IN p_DetBenNetoPerGastDev_C137  								DECIMAL(26,8)	,--DetBenNetoPerGastDev_C137
	IN p_NumSerieCuotasParticipe_C138  						DECIMAL(26,8)	,--NumSerieCuotasParticipe_C138
	IN p_RunNemotecnico 									NVARCHAR(21), -- C139
	IN p_TipoContribuyente 									CHAR(1), -- C138
	 
	    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id 									BIGINT		 ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador 					INT			 ,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador 						INT,

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
    DECLARE exit handler FOR salida_controlada
    BEGIN
        SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
        SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
    END;

    DECLARE exit handler FOR sqlexception
    BEGIN
        DECLARE v_errorExt NVARCHAR(40);
        IF ::sql_error_code > 10000 THEN
            -- Error pseudo-controlado pero desde un sub-sp.
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
                                   SELECT v_errorExt, ::sql_error_message FROM dummy;
            SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
        ELSE
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            RESIGNAL;
        END IF;
    END;

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
    p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1 = 2;

    ------------ Logica de Actualizacion ----------

    IF p_ID > 0 THEN
        p_out_id := p_ID;

 
		UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1922"
		SET "IdDocumento"= 	p_IdDocumento
		, "VboUsuario"= 	p_VboUsuario
		, "Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END)
		, "FuenteDatos"= 	p_FuenteDatos
		, "Numero"= 	p_Numero
		, "IdOperacion"= 	p_IdOperacion
		, "RutAdministradora"= 	p_RutAdministradora
		, "IdEmisor"= 	p_IdEmisor
		, "RutFondo"= 	p_RutFondo
		, "RunFondo"= 	p_RunFondo
		, "Fondo"= 	TRIM(p_Fondo)
		, "TipoFondo"= 	p_TipoFondo
		, "RutCliente"= 	p_RutCliente
		, "NombreParticipe"= 	p_NombreParticipe
		, "TaxId"= 	p_TaxId
		, "CodigoPais"= 	p_CodigoPais
		, "TipoOperacion"= 	p_TipoOperacion
		, "FechaInversion"= 	p_FechaInversion
		, "MonedaReajuste"= 	p_MonedaReajuste
		, "MontoNominalAportes"= 	p_MontoNominalAportes
		, "MontoNominalAportesActualizado"= 	p_MontoNominalAportesActualizado
		, "NumeroSerieCuotas"= 	p_NumeroSerieCuotas
		, "CantidadCuotasParticipe"= 	p_CantidadCuotasParticipe
		, "ParticipeCumpleRequisitos"= 	p_ParticipeCumpleRequisitos
		, "CuotasCumplenRequisitos"= 	p_CuotasCumplenRequisitos
		, "ValorCuotaPesos"= 	p_ValorCuotaPesos
		, "NumCuotasSerieParticipe"= 	p_NumCuotasSerieParticipe
		, "PorcentajeParticipacion31Dic"= 	p_PorcentajeParticipacion31Dic
		, "ValNominalCuotasRescatadas"= 	p_ValNominalCuotasRescatadas
		, "CantidadCuotasRescatadas"= 	p_CantidadCuotasRescatadas
		, "DiferenciaPorRescateOEnajenacion"= 	p_DiferenciaPorRescateOEnajenacion
		, "DiferenciaPorRescateOEnajenacionActualizado"= 	p_DiferenciaPorRescateOEnajenacionActualizado
		, "DifPorRescateOEnajenacionCFIAcogido107"= 	p_DifPorRescateOEnajenacionCFIAcogido107
		, "DifPorRescateOEnajenacionCFIAcogido107Actualizado"= 	p_DifPorRescateOEnajenacionCFIAcogido107Actualizado
		, "ValNominalInversionaFechaLiquidacion"= 	p_ValNominalInversionaFechaLiquidacion
		, "CantCuotasCedidasPorLiquidacion"= 	p_CantCuotasCedidasPorLiquidacion
		, "DiferenciaValorCuotasFechaLiquidacion"= 	p_DiferenciaValorCuotasFechaLiquidacion
		, "DiferenciaValorCuotasFechaLiquidacionActualizado"= 	p_DiferenciaValorCuotasFechaLiquidacionActualizado
		, "TributaSa"= 	p_TributaSa
		, "DivRemDistAfectGCConIDPC"= 	p_DivRemDistAfectGCConIDPC
		, "DivRemDistAfectGCConIDPCal31122016"= 	p_DivRemDistAfectGCConIDPCal31122016
		, "DivRemDistAfectGCConIDPCVOL"= 	p_DivRemDistAfectGCConIDPCVOL
		, "DivRemDistAfectGCSINDC"= 	p_DivRemDistAfectGCSINDC
		, "DisRemRentasTribCProvRap_C96"= 	p_DisRemRentasTribCProvRap_C96
		, "DisRemRentasTribCORP_C97"= 	p_DisRemRentasTribCORP_C97
		, "DisRemRentasTribCExDD_C98"= 	p_DisRemRentasTribCExDD_C98
		, "DisRemRentasTribCUtAFFut_C99"= 	p_DisRemRentasTribCUtAFFut_C99
		, "DivRemDistNoRentaFUT"= 	p_DivRemDistNoRentaFUT
		, "DisRemRentasExIGCAfecImptoAdi_C100"= 	p_DisRemRentasExIGCAfecImptoAdi_C100
		, "CredImpGloCompNSRSD"= 	p_CredImpGloCompNSRSD
		, "CredImpGloCompNSRCD"= 	p_CredImpGloCompNSRCD
		, "CredIGCAcumD01012017RANSRD01012020SDD_C101"= 	p_CredIGCAcumD01012017RANSRD01012020SDD_C101
		, "CredIGCAcumD01012017RANSRD01012020CDD_C102"= 	p_CredIGCAcumD01012017RANSRD01012020CDD_C102
		, "CredImpGloCompSRSD"= 	p_CredImpGloCompSRSD
		, "CredImpGloCompSRCD"= 	p_CredImpGloCompSRCD
		, "CredIGCAcumD01012017RXSRSDD_C103"= 	p_CredIGCAcumD01012017RXSRSDD_C103
		, "CredIGCAcumD01012017RXSRCDD_C104"= 	p_CredIGCAcumD01012017RXSRCDD_C104
		, "CredImpGloCompCTDCIF"= 	p_CredImpGloCompCTDCIF
		, "CredImpGloCompCDD31122016"= 	p_CredImpGloCompCDD31122016
		, "CredImpGloCompSDD31122016"= 	p_CredImpGloCompSDD31122016
		, "CredIGCAcumH31122016RXSDD_C105"= 	p_CredIGCAcumH31122016RXSDD_C105
		, "CredIGCAcumH31122016RXCDD_C106"= 	p_CredIGCAcumH31122016RXCDD_C106
		, "CredImpGloCompCTDCIF31122016"= 	p_CredImpGloCompCTDCIF31122016
		, "CredImpGloCompCITAExArt21"= 	p_CredImpGloCompCITAExArt21
		, "DetBenNetoPerRutFdo_C107"= 	p_DetBenNetoPerRutFdo_C107
		, "DetBenNetoPerPOS_C113"= 	p_DetBenNetoPerPOS_C113
		, "DetBenNetoPerNEG_C114"= 	p_DetBenNetoPerNEG_C114
		, "DividendosExentosIGCoAdicional"= 	p_DividendosExentosIGCoAdicional
		, "DividendosExentosIGCoAdicionalActualizado"= 	p_DividendosExentosIGCoAdicionalActualizado
		, "DividendosNoConstitutivosDeRenta"= 	p_DividendosNoConstitutivosDeRenta
		, "DividendosNoConstitutivosDeRentaActualizado"= 	p_DividendosNoConstitutivosDeRentaActualizado
		, "DivRemDistNoRentaFE"= 	p_DivRemDistNoRentaFE
		, "DividendosAfectosImptoUnicoTasa10"= 	p_DividendosAfectosImptoUnicoTasa10
		, "DividendosAfectosImptoUnicoTasa10Actualizado"= 	p_DividendosAfectosImptoUnicoTasa10Actualizado
		, "DividendosAfectosImptoUnicoTasa4"= 	p_DividendosAfectosImptoUnicoTasa4
		, "DividendosAfectosImptoUnicoTasa4Actualizado"= 	p_DividendosAfectosImptoUnicoTasa4Actualizado
		, "DistribucionXDiminucionNoImputadaCapital"= 	p_DistribucionXDiminucionNoImputadaCapital
		, "DevolucionesDeCapital"= 	p_DevolucionesDeCapital
		, "DevolucionesDeCapitalActualizado"= 	p_DevolucionesDeCapitalActualizado
		, "MontoInversionenCuotasCFIAnterior93"= 	p_MontoInversionenCuotasCFIAnterior93
		, "NumeroCertificado"= 	p_NumeroCertificado
		, "FactorActualizacion"= 	p_FactorActualizacion
		, "FechaOrigenInversion"=p_FechaOrigenInversion
		, "TipoDocumento"= 	p_TipoDocumento
		, "NumeroDocumento"= 	p_NumeroDocumento
		, "FechaDocumento"= 	p_FechaDocumento
		, "IdBaseDocumento"= 	p_IdBaseDocumento
		--, "IdCliente"= 	p_IdCliente
		--, "IdPortafolio"= 	p_IdPortafolio
		, "TipoTransaccion"= 	p_TipoTransaccion
		, "IdTipoOperacion"= 	p_IdTipoOperacion
		, "IdTipoProducto"= 	p_IdTipoProducto
		, "SecEvento"	= p_SecEvento
		, "AcogidoISFUT" = CASE WHEN p_AcogidoISFUT = 'S' OR p_AcogidoISFUT = 'N' THEN p_AcogidoISFUT ELSE "AcogidoISFUT" END
		, "DetBenNetoUtilPer_C133" = p_DetBenNetoUtilPer_C133
		, "DetBenNetoIntPer_C134" = p_DetBenNetoIntPer_C134
		, "DetBenNetoDivPer_C135" = p_DetBenNetoDivPer_C135
		, "DetBenNetoGanCapPer_C136" = p_DetBenNetoGanCapPer_C136
		, "DetBenNetoPerGastDev_C137" = p_DetBenNetoPerGastDev_C137
		, "NumSerieCuotasParticipe_C138" = p_NumSerieCuotasParticipe_C138
		, "RunNemotecnico" = p_RunNemotecnico
		, "TipoContribuyente" = p_TipoContribuyente
		WHERE ID = p_ID;
		
		       
       
       
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1922";

		       
		       
		INSERT INTO "BSCL.Normativa.BD::TRI.DatosDJ1922"
		("ID"
		, "IdDocumento"
		, "VboUsuario"
		, "Observaciones"
		, "FuenteDatos"
		, "Numero"
		, "IdOperacion"
		, "RutAdministradora"
		, "IdEmisor"
		, "RutFondo"
		, "RunFondo"
		, "Fondo"
		, "TipoFondo"
		, "RutCliente"
		, "NombreParticipe"
		, "TaxId"
		, "CodigoPais"
		, "TipoOperacion"
		, "FechaInversion"
		, "MonedaReajuste"
		, "MontoNominalAportes"
		, "MontoNominalAportesActualizado"
		, "NumeroSerieCuotas"
		, "CantidadCuotasParticipe"
		, "ParticipeCumpleRequisitos"
		, "CuotasCumplenRequisitos"
		, "ValorCuotaPesos"
		, "NumCuotasSerieParticipe"
		, "PorcentajeParticipacion31Dic"
		, "ValNominalCuotasRescatadas"
		, "CantidadCuotasRescatadas"
		, "DiferenciaPorRescateOEnajenacion"
		, "DiferenciaPorRescateOEnajenacionActualizado"
		, "DifPorRescateOEnajenacionCFIAcogido107"
		, "DifPorRescateOEnajenacionCFIAcogido107Actualizado"
		, "ValNominalInversionaFechaLiquidacion"
		, "CantCuotasCedidasPorLiquidacion"
		, "DiferenciaValorCuotasFechaLiquidacion"
		, "DiferenciaValorCuotasFechaLiquidacionActualizado"
		, "TributaSa"
		, "DivRemDistAfectGCConIDPC"
		, "DivRemDistAfectGCConIDPCal31122016"
		, "DivRemDistAfectGCConIDPCVOL"
		, "DivRemDistAfectGCSINDC"
		, "DisRemRentasTribCProvRap_C96"
		, "DisRemRentasTribCORP_C97"
		, "DisRemRentasTribCExDD_C98"
		, "DisRemRentasTribCUtAFFut_C99"
		, "DivRemDistNoRentaFUT"
		, "DisRemRentasExIGCAfecImptoAdi_C100"
		, "CredImpGloCompNSRSD"
		, "CredImpGloCompNSRCD"
		, "CredIGCAcumD01012017RANSRD01012020SDD_C101"
		, "CredIGCAcumD01012017RANSRD01012020CDD_C102"
		, "CredImpGloCompSRSD"
		, "CredImpGloCompSRCD"
		, "CredIGCAcumD01012017RXSRSDD_C103"
		, "CredIGCAcumD01012017RXSRCDD_C104"
		, "CredImpGloCompCTDCIF"
		, "CredImpGloCompCDD31122016"
		, "CredImpGloCompSDD31122016"
		, "CredIGCAcumH31122016RXSDD_C105"
		, "CredIGCAcumH31122016RXCDD_C106"
		, "CredImpGloCompCTDCIF31122016"
		, "CredImpGloCompCITAExArt21"
		, "DetBenNetoPerRutFdo_C107"
		, "DetBenNetoPerPOS_C113"
		, "DetBenNetoPerNEG_C114"
		, "DividendosExentosIGCoAdicional"
		, "DividendosExentosIGCoAdicionalActualizado"
		, "DividendosNoConstitutivosDeRenta"
		, "DividendosNoConstitutivosDeRentaActualizado"
		, "DivRemDistNoRentaFE"
		, "DividendosAfectosImptoUnicoTasa10"
		, "DividendosAfectosImptoUnicoTasa10Actualizado"
		, "DividendosAfectosImptoUnicoTasa4"
		, "DividendosAfectosImptoUnicoTasa4Actualizado"
		, "DistribucionXDiminucionNoImputadaCapital"
		, "DevolucionesDeCapital"
		, "DevolucionesDeCapitalActualizado"
		, "MontoInversionenCuotasCFIAnterior93"
		, "NumeroCertificado"
		, "FactorActualizacion"
		, "FechaOrigenInversion"
		, "TipoDocumento"
		, "NumeroDocumento"
		, "FechaDocumento"
		, "IdBaseDocumento"
		, "IdCliente"
		, "IdPortafolio"
		, "TipoTransaccion"
		, "IdTipoOperacion"
		, "IdTipoProducto"
		, "SecEvento"
		, "AcogidoISFUT"
		, "DetBenNetoUtilPer_C133"
		, "DetBenNetoIntPer_C134"
		, "DetBenNetoDivPer_C135"
		, "DetBenNetoGanCapPer_C136"
		, "DetBenNetoPerGastDev_C137"
		, "NumSerieCuotasParticipe_C138"
		, "RunNemotecnico"
		, "TipoContribuyente")
		VALUES(
		p_out_id
		, p_IdDocumento
		,'N'
		, ''--p_Observaciones
		,'MANTENEDOR'
		,0 --p_Numero
		,0 --p_IdOperacion
		, p_RutAdministradora
		, COALESCE(p_IdEmisor,(SELECT top 1 "IdEmisor" FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" where "Nemotecnico" = TRIM(p_Fondo)),'')
		, p_RutFondo
		, IFNULL(p_RunFondo,'')
		, TRIM(p_Fondo)
		, IFNULL(p_TipoFondo,'')
		, p_RutCliente
		, p_NombreParticipe
		, p_TaxId
		, p_CodigoPais
		, p_TipoOperacion
		, p_FechaInversion
		, IFNULL(p_MonedaReajuste,'')
		, p_MontoNominalAportes
		, IFNULL(p_MontoNominalAportesActualizado,0)
		, IFNULL(p_NumeroSerieCuotas,0)
		, IFNULL(p_CantidadCuotasParticipe,0)
		, IFNULL(p_ParticipeCumpleRequisitos,0)
		, IFNULL(p_CuotasCumplenRequisitos,0)
		, IFNULL(p_ValorCuotaPesos,0)
		, IFNULL(p_NumCuotasSerieParticipe,0)
		, IFNULL(p_PorcentajeParticipacion31Dic,0)
		, p_ValNominalCuotasRescatadas
		, p_CantidadCuotasRescatadas
		, IFNULL(p_DiferenciaPorRescateOEnajenacion,0)
		, p_DiferenciaPorRescateOEnajenacionActualizado
		, IFNULL(p_DifPorRescateOEnajenacionCFIAcogido107,0)
		, p_DifPorRescateOEnajenacionCFIAcogido107Actualizado
		, p_ValNominalInversionaFechaLiquidacion
		, p_CantCuotasCedidasPorLiquidacion
		, p_DiferenciaValorCuotasFechaLiquidacion
		, p_DiferenciaValorCuotasFechaLiquidacionActualizado
		, p_TributaSa
		, p_DivRemDistAfectGCConIDPC
		, p_DivRemDistAfectGCConIDPCal31122016
		, p_DivRemDistAfectGCConIDPCVOL
		, p_DivRemDistAfectGCSINDC
		, p_DisRemRentasTribCProvRap_C96
		, p_DisRemRentasTribCORP_C97
		, p_DisRemRentasTribCExDD_C98
		, p_DisRemRentasTribCUtAFFut_C99
		, p_DivRemDistNoRentaFUT
		, p_DisRemRentasExIGCAfecImptoAdi_C100
		, p_CredImpGloCompNSRSD
		, p_CredImpGloCompNSRCD
		, p_CredIGCAcumD01012017RANSRD01012020SDD_C101
		, p_CredIGCAcumD01012017RANSRD01012020CDD_C102
		, p_CredImpGloCompSRSD
		, p_CredImpGloCompSRCD
		, p_CredIGCAcumD01012017RXSRSDD_C103
		, p_CredIGCAcumD01012017RXSRCDD_C104
		, p_CredImpGloCompCTDCIF
		, p_CredImpGloCompCDD31122016
		, p_CredImpGloCompSDD31122016
		, p_CredIGCAcumH31122016RXSDD_C105
		, p_CredIGCAcumH31122016RXCDD_C106
		, p_CredImpGloCompCTDCIF31122016
		, p_CredImpGloCompCITAExArt21
		, IFNULL(p_DetBenNetoPerRutFdo_C107,0)
		, IFNULL(p_DetBenNetoPerPOS_C113,0)
		, IFNULL(p_DetBenNetoPerNEG_C114,0)
		, p_DividendosExentosIGCoAdicional
		, p_DividendosExentosIGCoAdicionalActualizado
		, p_DividendosNoConstitutivosDeRenta
		, p_DividendosNoConstitutivosDeRentaActualizado
		, p_DivRemDistNoRentaFE
		, p_DividendosAfectosImptoUnicoTasa10
		, p_DividendosAfectosImptoUnicoTasa10Actualizado
		, p_DividendosAfectosImptoUnicoTasa4
		, p_DividendosAfectosImptoUnicoTasa4Actualizado
		, p_DistribucionXDiminucionNoImputadaCapital
		, p_DevolucionesDeCapital
		, p_DevolucionesDeCapitalActualizado
		, p_MontoInversionenCuotasCFIAnterior93
		, p_NumeroCertificado
		, p_FactorActualizacion
		, p_FechaOrigenInversion
		, p_TipoDocumento
		, p_NumeroDocumento
		, p_FechaDocumento
		, p_IdBaseDocumento
		,0 --p_IdCliente
		,0 --p_IdPortafolio
		, p_TipoTransaccion
		, p_IdTipoOperacion
		, p_IdTipoProducto
		, IFNULL(p_SecEvento,0)
		,p_AcogidoISFUT
		,IFNULL(p_DetBenNetoUtilPer_C133,0)
		,IFNULL(p_DetBenNetoIntPer_C134,0)
		,IFNULL(p_DetBenNetoDivPer_C135,0)
		,IFNULL(p_DetBenNetoGanCapPer_C136,0)
		,IFNULL(p_DetBenNetoPerGastDev_C137,0)
		,IFNULL(p_NumSerieCuotasParticipe_C138,0)
		,p_RunNemotecnico
		,p_TipoContribuyente
		);
		  
       
    END IF;
END;