SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.ObtenerTotales"
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
						    
						    -- Rango del ID cliente.
						    IN p_IdCliente_Desde BIGINT DEFAULT NULL,
						    IN p_IdCliente_Hasta BIGINT DEFAULT NULL,
						
						    -- Rango del ID portafolio.
						    IN p_IdPortafolio_Desde BIGINT DEFAULT NULL,
						    IN p_IdPortafolio_Hasta BIGINT DEFAULT NULL,
										
							-- Rango Monto Nominal Aportes
							IN p_MontoNominalAportes_Desde 										DECIMAL(26,8) DEFAULT NULL,
							IN p_MontoNominalAportes_Hasta 										DECIMAL(26,8) DEFAULT NULL,
							IN p_MontoNominalAportesActualizado_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_MontoNominalAportesActualizado_Hasta							DECIMAL(26,8) DEFAULT NULL,
							IN p_CantidadCuotasParticipe_Desde								 	DECIMAL(26,8) DEFAULT NULL,
							IN p_ValorCuotaPesos_Desde										 	DECIMAL(26,8) DEFAULT NULL,
							IN p_NumCuotasSerieParticipe_Desde								 	DECIMAL(26,8) DEFAULT NULL,
							IN p_PorcentajeParticipacion31Dic_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_ValNominalCuotasRescatadas_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_CantidadCuotasRescatadas_Desde								 	DECIMAL(26,8) DEFAULT NULL,
							IN p_DiferenciaPorRescateOEnajenacion_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_DiferenciaPorRescateOEnajenacionActualizado_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_DifPorRescateOEnajenacionCFIAcogido107_Desde					DECIMAL(26,8) DEFAULT NULL,
							IN p_DifPorRescateOEnajenacionCFIAcogido107Actualizado_Desde		DECIMAL(26,8) DEFAULT NULL,
							IN p_ValNominalInversionaFechaLiquidacion_Desde						DECIMAL(26,8) DEFAULT NULL,
							IN p_CantCuotasCedidasPorLiquidacion_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_DiferenciaValorCuotasFechaLiquidacion_Desde					DECIMAL(26,8) DEFAULT NULL,
							IN p_DiferenciaValorCuotasFechaLiquidacionActualizado_Desde			DECIMAL(26,8) DEFAULT NULL,
							IN p_DivRemDistAfectGCConIDPC_Desde								 	DECIMAL(26,8) DEFAULT NULL,
							IN p_DivRemDistAfectGCConIDPCal31122016_Desde						DECIMAL(26,8) DEFAULT NULL,
							IN p_DivRemDistAfectGCConIDPCVOL_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_DivRemDistAfectGCSINDC_Desde									DECIMAL(26,8) DEFAULT NULL,
							IN p_DisRemRentasTribCProvRap_C96_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_DisRemRentasTribCORP_C97_Desde								 	DECIMAL(26,8) DEFAULT NULL,
							IN p_DisRemRentasTribCExDD_C98_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_DisRemRentasTribCUtAFFut_C99_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_DivRemDistNoRentaFUT_Desde								 		DECIMAL(26,8) DEFAULT NULL,
							IN p_DisRemRentasExIGCAfecImptoAdi_C100_Desde						DECIMAL(26,8) DEFAULT NULL,
							IN p_DividendosExentosIGCoAdicional_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_DividendosExentosIGCoAdicionalActualizado_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_DividendosNoConstitutivosDeRenta_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_DividendosNoConstitutivosDeRentaActualizado_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_DivRemDistNoRentaFE_Desde								 		DECIMAL(26,8) DEFAULT NULL,
							IN p_DividendosAfectosImptoUnicoTasa10_Desde						DECIMAL(26,8) DEFAULT NULL,
							IN p_DividendosAfectosImptoUnicoTasa10Actualizado_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_DividendosAfectosImptoUnicoTasa4_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_DividendosAfectosImptoUnicoTasa4Actualizado_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_DistribucionXDiminucionNoImputadaCapital_Desde					DECIMAL(26,8) DEFAULT NULL,
							IN p_DevolucionesDeCapital_Desde								 	DECIMAL(26,8) DEFAULT NULL,
							IN p_DevolucionesDeCapitalActualizado_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_CredImpGloCompNSRSD_Desde								 		DECIMAL(26,8) DEFAULT NULL,
							IN p_CredImpGloCompNSRCD_Desde								 		DECIMAL(26,8) DEFAULT NULL,
							IN p_CredIGCAcumD01012017RANSRD01012020SDD_C101_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_CredIGCAcumD01012017RANSRD01012020CDD_C102_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_CredImpGloCompSRSD_Desde								 		DECIMAL(26,8) DEFAULT NULL,
							IN p_CredImpGloCompSRCD_Desde								 		DECIMAL(26,8) DEFAULT NULL,
							IN p_CredIGCAcumD01012017RXSRSDD_C103_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_CredIGCAcumD01012017RXSRCDD_C104_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_CredImpGloCompCTDCIF_Desde								 		DECIMAL(26,8) DEFAULT NULL,
							IN p_CredImpGloCompSDD31122016_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_CredImpGloCompCDD31122016_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_CredIGCAcumH31122016RXSDD_C105_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_CredIGCAcumH31122016RXCDD_C106_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_CredImpGloCompCTDCIF31122016_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_CredImpGloCompCITAExArt21_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_MontoInversionenCuotasCFIAnterior93_Desde						DECIMAL(26,8) DEFAULT NULL,
							IN p_DetBenNetoPerPOS_C113_Desde								 	DECIMAL(26,8) DEFAULT NULL,
							IN p_DetBenNetoPerNEG_C114_Desde								 	DECIMAL(26,8) DEFAULT NULL,
							IN p_FactorActualizacion_Desde								 		DECIMAL(26,8) DEFAULT NULL,
							IN p_RutCliente nvarchar(42) 										default NULL,
							IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 													NVARCHAR(100),
        "COUNT" 												BIGINT,
		"MontoNominalAportes"									DECIMAL(26,8),
		"MontoNominalAportesActualizado"						DECIMAL(26,8),
		"CantidadCuotasParticipe"								DECIMAL(26,8),
		"ValorCuotaPesos"										DECIMAL(26,8),
		"NumCuotasSerieParticipe"								DECIMAL(26,8),
		"PorcentajeParticipacion31Dic"							DECIMAL(26,8),
		"ValNominalCuotasRescatadas"							DECIMAL(26,8),
		"CantidadCuotasRescatadas"								DECIMAL(26,8),
		"DiferenciaPorRescateOEnajenacion"						DECIMAL(26,8),
		"DiferenciaPorRescateOEnajenacionActualizado"			DECIMAL(26,8),
		"DifPorRescateOEnajenacionCFIAcogido107"				DECIMAL(26,8),
		"DifPorRescateOEnajenacionCFIAcogido107Actualizado"		DECIMAL(26,8),
		"ValNominalInversionaFechaLiquidacion"					DECIMAL(26,8),
		"CantCuotasCedidasPorLiquidacion"						DECIMAL(26,8),
		"DiferenciaValorCuotasFechaLiquidacion"					DECIMAL(26,8),
		"DiferenciaValorCuotasFechaLiquidacionActualizado"		DECIMAL(26,8),
		"DivRemDistAfectGCConIDPC"								DECIMAL(26,8),
		"DivRemDistAfectGCConIDPCal31122016"					DECIMAL(26,8),
		"DivRemDistAfectGCConIDPCVOL"							DECIMAL(26,8),
		"DivRemDistAfectGCSINDC"								DECIMAL(26,8),
		"DisRemRentasTribCProvRap_C96"							DECIMAL(26,8),
		"DisRemRentasTribCORP_C97"								DECIMAL(26,8),
		"DisRemRentasTribCExDD_C98"								DECIMAL(26,8),
		"DisRemRentasTribCUtAFFut_C99"							DECIMAL(26,8),
		"DivRemDistNoRentaFUT"									DECIMAL(26,8),
		"DisRemRentasExIGCAfecImptoAdi_C100"					DECIMAL(26,8),
		"DividendosExentosIGCoAdicional"						DECIMAL(26,8),
		"DividendosExentosIGCoAdicionalActualizado"				DECIMAL(26,8),
		"DividendosNoConstitutivosDeRenta"						DECIMAL(26,8),
		"DividendosNoConstitutivosDeRentaActualizado"			DECIMAL(26,8),
		"DivRemDistNoRentaFE"									DECIMAL(26,8),
		"DividendosAfectosImptoUnicoTasa10"						DECIMAL(26,8),
		"DividendosAfectosImptoUnicoTasa10Actualizado"			DECIMAL(26,8),
		"DividendosAfectosImptoUnicoTasa4"						DECIMAL(26,8),
		"DividendosAfectosImptoUnicoTasa4Actualizado"			DECIMAL(26,8),
		"DistribucionXDiminucionNoImputadaCapital"				DECIMAL(26,8),
		"DevolucionesDeCapital"									DECIMAL(26,8),
		"DevolucionesDeCapitalActualizado"						DECIMAL(26,8),
		"CredImpGloCompNSRSD"									DECIMAL(26,8),
		"CredImpGloCompNSRCD"									DECIMAL(26,8),
		"CredIGCAcumD01012017RANSRD01012020SDD_C101"			DECIMAL(26,8),
		"CredIGCAcumD01012017RANSRD01012020CDD_C102"			DECIMAL(26,8),
		"CredImpGloCompSRSD"									DECIMAL(26,8),
		"CredImpGloCompSRCD"									DECIMAL(26,8),
		"CredIGCAcumD01012017RXSRSDD_C103"						DECIMAL(26,8),
		"CredIGCAcumD01012017RXSRCDD_C104"						DECIMAL(26,8),
		"CredImpGloCompCTDCIF"									DECIMAL(26,8),
		"CredImpGloCompSDD31122016"								DECIMAL(26,8),
		"CredImpGloCompCDD31122016"								DECIMAL(26,8),
		"CredIGCAcumH31122016RXSDD_C105"						DECIMAL(26,8),
		"CredIGCAcumH31122016RXCDD_C106"						DECIMAL(26,8),
		"CredImpGloCompCTDCIF31122016"							DECIMAL(26,8),
		"CredImpGloCompCITAExArt21"								DECIMAL(26,8),
		"MontoInversionenCuotasCFIAnterior93"					DECIMAL(26,8),
		"DetBenNetoPerPOS_C113"									DECIMAL(26,8),
		"DetBenNetoPerNEG_C114"									DECIMAL(26,8),
		"FactorActualizacion"									DECIMAL(26,8)
		
        
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
	                    'TOTAL' AS "TYPE"
	                    ,COUNT(1) AS "COUNT"
							,SUM("MontoNominalAportes")									AS "MontoNominalAportes"
							,SUM("MontoNominalAportesActualizado")						AS "MontoNominalAportesActualizado"
							,SUM("CantidadCuotasParticipe")								AS "CantidadCuotasParticipe"
							,SUM("ValorCuotaPesos")										AS "ValorCuotaPesos"
							,SUM("NumCuotasSerieParticipe")								AS "NumCuotasSerieParticipe"
							,SUM("PorcentajeParticipacion31Dic")						AS "PorcentajeParticipacion31Dic"
							,SUM("ValNominalCuotasRescatadas")							AS "ValNominalCuotasRescatadas"
							,SUM("CantidadCuotasRescatadas")							AS "CantidadCuotasRescatadas"
							,SUM("DiferenciaPorRescateOEnajenacion")					AS "DiferenciaPorRescateOEnajenacion"
							,SUM("DiferenciaPorRescateOEnajenacionActualizado")			AS "DiferenciaPorRescateOEnajenacionActualizado"
							,SUM("DifPorRescateOEnajenacionCFIAcogido107")				AS "DifPorRescateOEnajenacionCFIAcogido107"
							,SUM("DifPorRescateOEnajenacionCFIAcogido107Actualizado")	AS "DifPorRescateOEnajenacionCFIAcogido107Actualizado"
							,SUM("ValNominalInversionaFechaLiquidacion")				AS "ValNominalInversionaFechaLiquidacion"
							,SUM("CantCuotasCedidasPorLiquidacion")						AS "CantCuotasCedidasPorLiquidacion"
							,SUM("DiferenciaValorCuotasFechaLiquidacion")				AS "DiferenciaValorCuotasFechaLiquidacion"
							,SUM("DiferenciaValorCuotasFechaLiquidacionActualizado")	AS "DiferenciaValorCuotasFechaLiquidacionActualizado"
							,SUM("DivRemDistAfectGCConIDPC")							AS "DivRemDistAfectGCConIDPC"
							,SUM("DivRemDistAfectGCConIDPCal31122016")					AS "DivRemDistAfectGCConIDPCal31122016"
							,SUM("DivRemDistAfectGCConIDPCVOL")								AS "DivRemDistAfectGCConIDPCVOL"
							,SUM("DivRemDistAfectGCSINDC")									AS "DivRemDistAfectGCSINDC"
							,SUM("DisRemRentasTribCProvRap_C96")								AS "DisRemRentasTribCProvRap_C96"
							,SUM("DisRemRentasTribCORP_C97")									AS "DisRemRentasTribCORP_C97"
							,SUM("DisRemRentasTribCExDD_C98")									AS "DisRemRentasTribCExDD_C98"
							,SUM("DisRemRentasTribCUtAFFut_C99")							AS "DisRemRentasTribCUtAFFut_C99"
							,SUM("DivRemDistNoRentaFUT")									AS "DivRemDistNoRentaFUT"
							,SUM("DisRemRentasExIGCAfecImptoAdi_C100")					AS "DisRemRentasExIGCAfecImptoAdi_C100"
							,SUM("DividendosExentosIGCoAdicional")						AS "DividendosExentosIGCoAdicional"
							,SUM("DividendosExentosIGCoAdicionalActualizado")				AS "DividendosExentosIGCoAdicionalActualizado"
							,SUM("DividendosNoConstitutivosDeRenta")						AS "DividendosNoConstitutivosDeRenta"
							,SUM("DividendosNoConstitutivosDeRentaActualizado")			AS "DividendosNoConstitutivosDeRentaActualizado"
							,SUM("DivRemDistNoRentaFE")									AS "DivRemDistNoRentaFE"
							,SUM("DividendosAfectosImptoUnicoTasa10")						AS "DividendosAfectosImptoUnicoTasa10"
							,SUM("DividendosAfectosImptoUnicoTasa10Actualizado")				AS "DividendosAfectosImptoUnicoTasa10Actualizado"
							,SUM("DividendosAfectosImptoUnicoTasa4")						AS "DividendosAfectosImptoUnicoTasa4"
							,SUM("DividendosAfectosImptoUnicoTasa4Actualizado")			AS "DividendosAfectosImptoUnicoTasa4Actualizado"
							,SUM("DistribucionXDiminucionNoImputadaCapital")				AS "DistribucionXDiminucionNoImputadaCapital"
							,SUM("DevolucionesDeCapital")									AS "DevolucionesDeCapital"
							,SUM("DevolucionesDeCapitalActualizado")						AS "DevolucionesDeCapitalActualizado"
							,SUM("CredImpGloCompNSRSD")									AS "CredImpGloCompNSRSD"
							,SUM("CredImpGloCompNSRCD")									AS "CredImpGloCompNSRCD"
							,SUM("CredIGCAcumD01012017RANSRD01012020SDD_C101")			AS "CredIGCAcumD01012017RANSRD01012020SDD_C101"
							,SUM("CredIGCAcumD01012017RANSRD01012020CDD_C102")			AS "CredIGCAcumD01012017RANSRD01012020CDD_C102"
							,SUM("CredImpGloCompSRSD")									AS "CredImpGloCompSRSD"
							,SUM("CredImpGloCompSRCD")									AS "CredImpGloCompSRCD"
							,SUM("CredIGCAcumD01012017RXSRSDD_C103")						AS "CredIGCAcumD01012017RXSRSDD_C103"
							,SUM("CredIGCAcumD01012017RXSRCDD_C104")						AS "CredIGCAcumD01012017RXSRCDD_C104"
							,SUM("CredImpGloCompCTDCIF")									AS "CredImpGloCompCTDCIF"
							,SUM("CredImpGloCompSDD31122016")								AS "CredImpGloCompSDD31122016"
							,SUM("CredImpGloCompCDD31122016")								AS "CredImpGloCompCDD31122016"
							,SUM("CredIGCAcumH31122016RXSDD_C105")						AS "CredIGCAcumH31122016RXSDD_C105"
							,SUM("CredIGCAcumH31122016RXCDD_C106")						AS "CredIGCAcumH31122016RXCDD_C106"
							,SUM("CredImpGloCompCTDCIF31122016")							AS "CredImpGloCompCTDCIF31122016"
							,SUM("CredImpGloCompCITAExArt21")								AS "CredImpGloCompCITAExArt21"
							,SUM("MontoInversionenCuotasCFIAnterior93")					AS "MontoInversionenCuotasCFIAnterior93"
							,SUM("DetBenNetoPerPOS_C113")									AS "DetBenNetoPerPOS_C113"
							,SUM("DetBenNetoPerNEG_C114")									AS "DetBenNetoPerNEG_C114"
							,SUM("FactorActualizacion")									AS "FactorActualizacion"
                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1922"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                select
	                    'FILTRO' AS "TYPE"
	                    ,COUNT(1) AS "COUNT"
							,SUM("MontoNominalAportes")									AS "MontoNominalAportes"
							,SUM("MontoNominalAportesActualizado")						AS "MontoNominalAportesActualizado"
							,SUM("CantidadCuotasParticipe")								AS "CantidadCuotasParticipe"
							,SUM("ValorCuotaPesos")										AS "ValorCuotaPesos"
							,SUM("NumCuotasSerieParticipe")								AS "NumCuotasSerieParticipe"
							,SUM("PorcentajeParticipacion31Dic")						AS "PorcentajeParticipacion31Dic"
							,SUM("ValNominalCuotasRescatadas")							AS "ValNominalCuotasRescatadas"
							,SUM("CantidadCuotasRescatadas")							AS "CantidadCuotasRescatadas"
							,SUM("DiferenciaPorRescateOEnajenacion")					AS "DiferenciaPorRescateOEnajenacion"
							,SUM("DiferenciaPorRescateOEnajenacionActualizado")			AS "DiferenciaPorRescateOEnajenacionActualizado"
							,SUM("DifPorRescateOEnajenacionCFIAcogido107")				AS "DifPorRescateOEnajenacionCFIAcogido107"
							,SUM("DifPorRescateOEnajenacionCFIAcogido107Actualizado")	AS "DifPorRescateOEnajenacionCFIAcogido107Actualizado"
							,SUM("ValNominalInversionaFechaLiquidacion")				AS "ValNominalInversionaFechaLiquidacion"
							,SUM("CantCuotasCedidasPorLiquidacion")						AS "CantCuotasCedidasPorLiquidacion"
							,SUM("DiferenciaValorCuotasFechaLiquidacion")				AS "DiferenciaValorCuotasFechaLiquidacion"
							,SUM("DiferenciaValorCuotasFechaLiquidacionActualizado")	AS "DiferenciaValorCuotasFechaLiquidacionActualizado"
							,SUM("DivRemDistAfectGCConIDPC")							AS "DivRemDistAfectGCConIDPC"
							,SUM("DivRemDistAfectGCConIDPCal31122016")					AS "DivRemDistAfectGCConIDPCal31122016"
							,SUM("DivRemDistAfectGCConIDPCVOL")								AS "DivRemDistAfectGCConIDPCVOL"
							,SUM("DivRemDistAfectGCSINDC")									AS "DivRemDistAfectGCSINDC"
							,SUM("DisRemRentasTribCProvRap_C96")								AS "DisRemRentasTribCProvRap_C96"
							,SUM("DisRemRentasTribCORP_C97")									AS "DisRemRentasTribCORP_C97"
							,SUM("DisRemRentasTribCExDD_C98")									AS "DisRemRentasTribCExDD_C98"
							,SUM("DisRemRentasTribCUtAFFut_C99")							AS "DisRemRentasTribCUtAFFut_C99"
							,SUM("DivRemDistNoRentaFUT")									AS "DivRemDistNoRentaFUT"
							,SUM("DisRemRentasExIGCAfecImptoAdi_C100")					AS "DisRemRentasExIGCAfecImptoAdi_C100"
							,SUM("DividendosExentosIGCoAdicional")						AS "DividendosExentosIGCoAdicional"
							,SUM("DividendosExentosIGCoAdicionalActualizado")				AS "DividendosExentosIGCoAdicionalActualizado"
							,SUM("DividendosNoConstitutivosDeRenta")						AS "DividendosNoConstitutivosDeRenta"
							,SUM("DividendosNoConstitutivosDeRentaActualizado")			AS "DividendosNoConstitutivosDeRentaActualizado"
							,SUM("DivRemDistNoRentaFE")									AS "DivRemDistNoRentaFE"
							,SUM("DividendosAfectosImptoUnicoTasa10")						AS "DividendosAfectosImptoUnicoTasa10"
							,SUM("DividendosAfectosImptoUnicoTasa10Actualizado")				AS "DividendosAfectosImptoUnicoTasa10Actualizado"
							,SUM("DividendosAfectosImptoUnicoTasa4")						AS "DividendosAfectosImptoUnicoTasa4"
							,SUM("DividendosAfectosImptoUnicoTasa4Actualizado")			AS "DividendosAfectosImptoUnicoTasa4Actualizado"
							,SUM("DistribucionXDiminucionNoImputadaCapital")				AS "DistribucionXDiminucionNoImputadaCapital"
							,SUM("DevolucionesDeCapital")									AS "DevolucionesDeCapital"
							,SUM("DevolucionesDeCapitalActualizado")						AS "DevolucionesDeCapitalActualizado"
							,SUM("CredImpGloCompNSRSD")									AS "CredImpGloCompNSRSD"
							,SUM("CredImpGloCompNSRCD")									AS "CredImpGloCompNSRCD"
							,SUM("CredIGCAcumD01012017RANSRD01012020SDD_C101")			AS "CredIGCAcumD01012017RANSRD01012020SDD_C101"
							,SUM("CredIGCAcumD01012017RANSRD01012020CDD_C102")			AS "CredIGCAcumD01012017RANSRD01012020CDD_C102"
							,SUM("CredImpGloCompSRSD")									AS "CredImpGloCompSRSD"
							,SUM("CredImpGloCompSRCD")									AS "CredImpGloCompSRCD"
							,SUM("CredIGCAcumD01012017RXSRSDD_C103")						AS "CredIGCAcumD01012017RXSRSDD_C103"
							,SUM("CredIGCAcumD01012017RXSRCDD_C104")						AS "CredIGCAcumD01012017RXSRCDD_C104"
							,SUM("CredImpGloCompCTDCIF")									AS "CredImpGloCompCTDCIF"
							,SUM("CredImpGloCompSDD31122016")								AS "CredImpGloCompSDD31122016"
							,SUM("CredImpGloCompCDD31122016")								AS "CredImpGloCompCDD31122016"
							,SUM("CredIGCAcumH31122016RXSDD_C105")						AS "CredIGCAcumH31122016RXSDD_C105"
							,SUM("CredIGCAcumH31122016RXCDD_C106")						AS "CredIGCAcumH31122016RXCDD_C106"
							,SUM("CredImpGloCompCTDCIF31122016")							AS "CredImpGloCompCTDCIF31122016"
							,SUM("CredImpGloCompCITAExArt21")								AS "CredImpGloCompCITAExArt21"
							,SUM("MontoInversionenCuotasCFIAnterior93")					AS "MontoInversionenCuotasCFIAnterior93"
							,SUM("DetBenNetoPerPOS_C113")									AS "DetBenNetoPerPOS_C113"
							,SUM("DetBenNetoPerNEG_C114")									AS "DetBenNetoPerNEG_C114"
							,SUM("FactorActualizacion")									AS "FactorActualizacion"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1922"
	                    WHERE "IdDocumento" = p_IdDocumento
	                    AND ("VboUsuario" LIKE p_VboUsuario or p_VboUsuario IS NULL)
	                    AND ("Observaciones" LIKE p_Observaciones or p_Observaciones IS NULL)
	                    AND ("FuenteDatos" LIKE p_FuenteDatos or p_FuenteDatos IS NULL)
	                    AND ("RutAdministradora" LIKE p_RutAdministradora OR p_RutAdministradora IS NULL)
	                    AND ("IdCliente" >= p_IdCliente_Desde OR p_IdCliente_Desde IS NULL)
	                    AND ("IdCliente" <= p_IdCliente_Hasta OR p_IdCliente_Hasta IS NULL)
	                    AND ("IdPortafolio" >= p_IdPortafolio_Desde OR p_IdPortafolio_Desde IS NULL)
	                    AND ("IdPortafolio" <= p_IdPortafolio_Hasta OR p_IdPortafolio_Hasta IS NULL)
	                    AND (trim("RutCliente") like trim(p_RutCliente) or p_RutCliente is null)
	                    
					     AND ("MontoNominalAportes"									>= p_MontoNominalAportes_Desde OR p_MontoNominalAportes_Desde IS NULL)
					     AND ("MontoNominalAportes" 								<= p_MontoNominalAportes_Desde OR p_MontoNominalAportes_Desde IS NULL)
						 AND ("MontoNominalAportesActualizado"						>= p_MontoNominalAportesActualizado_Desde OR p_MontoNominalAportesActualizado_Desde IS NULL)
 					     AND ("MontoNominalAportesActualizado" 						<= p_MontoNominalAportesActualizado_Desde OR p_MontoNominalAportesActualizado_Desde IS NULL)
 					     
						 AND ("CantidadCuotasParticipe"								>= p_CantidadCuotasParticipe_Desde OR p_CantidadCuotasParticipe_Desde IS NULL)
						 AND ("ValorCuotaPesos"										>= p_ValorCuotaPesos_Desde OR p_ValorCuotaPesos_Desde IS NULL)
						 AND ("NumCuotasSerieParticipe"								>= p_NumCuotasSerieParticipe_Desde OR p_NumCuotasSerieParticipe_Desde IS NULL)
						 AND ("PorcentajeParticipacion31Dic"						>= p_PorcentajeParticipacion31Dic_Desde OR p_PorcentajeParticipacion31Dic_Desde IS NULL)
						 AND ("ValNominalCuotasRescatadas"							>= p_ValNominalCuotasRescatadas_Desde OR p_ValNominalCuotasRescatadas_Desde IS NULL)
						 AND ("CantidadCuotasRescatadas"							>= p_CantidadCuotasRescatadas_Desde OR p_CantidadCuotasRescatadas_Desde IS NULL)
						 AND ("DiferenciaPorRescateOEnajenacion"					>= p_DiferenciaPorRescateOEnajenacion_Desde OR p_DiferenciaPorRescateOEnajenacion_Desde IS NULL)
						 AND ("DiferenciaPorRescateOEnajenacionActualizado"			>= p_DiferenciaPorRescateOEnajenacionActualizado_Desde OR p_DiferenciaPorRescateOEnajenacionActualizado_Desde IS NULL)
						 AND ("DifPorRescateOEnajenacionCFIAcogido107"				>= p_DifPorRescateOEnajenacionCFIAcogido107_Desde OR p_DifPorRescateOEnajenacionCFIAcogido107_Desde IS NULL)
						 AND ("DifPorRescateOEnajenacionCFIAcogido107Actualizado"	>= p_DifPorRescateOEnajenacionCFIAcogido107Actualizado_Desde OR p_DifPorRescateOEnajenacionCFIAcogido107Actualizado_Desde IS NULL)
						 AND ("ValNominalInversionaFechaLiquidacion"				>= p_ValNominalInversionaFechaLiquidacion_Desde OR p_ValNominalInversionaFechaLiquidacion_Desde IS NULL)
						 AND ("CantCuotasCedidasPorLiquidacion"						>= p_CantCuotasCedidasPorLiquidacion_Desde OR p_CantCuotasCedidasPorLiquidacion_Desde IS NULL)
						 AND ("DiferenciaValorCuotasFechaLiquidacion"				>= p_DiferenciaValorCuotasFechaLiquidacion_Desde OR p_DiferenciaValorCuotasFechaLiquidacion_Desde IS NULL)
						 AND ("DiferenciaValorCuotasFechaLiquidacionActualizado"	>= p_DiferenciaValorCuotasFechaLiquidacionActualizado_Desde OR p_DiferenciaValorCuotasFechaLiquidacionActualizado_Desde IS NULL)
						 AND ("DivRemDistAfectGCConIDPC"							>= p_DivRemDistAfectGCConIDPC_Desde OR p_DivRemDistAfectGCConIDPC_Desde IS NULL)
						 AND ("DivRemDistAfectGCConIDPCal31122016"					>= p_DivRemDistAfectGCConIDPCal31122016_Desde OR p_DivRemDistAfectGCConIDPCal31122016_Desde IS NULL)
						 AND ("DivRemDistAfectGCConIDPCVOL"							>= p_DivRemDistAfectGCConIDPCVOL_Desde OR p_DivRemDistAfectGCConIDPCVOL_Desde IS NULL)
						 AND ("DivRemDistAfectGCSINDC"								>= p_DivRemDistAfectGCSINDC_Desde OR p_DivRemDistAfectGCSINDC_Desde IS NULL)
						 AND ("DisRemRentasTribCProvRap_C96"						>= p_DisRemRentasTribCProvRap_C96_Desde OR p_DisRemRentasTribCProvRap_C96_Desde IS NULL)
						 AND ("DisRemRentasTribCORP_C97"							>= p_DisRemRentasTribCORP_C97_Desde OR p_DisRemRentasTribCORP_C97_Desde IS NULL)
						 AND ("DisRemRentasTribCExDD_C98"							>= p_DisRemRentasTribCExDD_C98_Desde OR p_DisRemRentasTribCExDD_C98_Desde IS NULL)
						 AND ("DisRemRentasTribCUtAFFut_C99"						>= p_DisRemRentasTribCUtAFFut_C99_Desde OR p_DisRemRentasTribCUtAFFut_C99_Desde IS NULL)
						 AND ("DivRemDistNoRentaFUT"								>= p_DivRemDistNoRentaFUT_Desde OR p_DivRemDistNoRentaFUT_Desde IS NULL)
						 AND ("DisRemRentasExIGCAfecImptoAdi_C100"					>= p_DisRemRentasExIGCAfecImptoAdi_C100_Desde OR p_DisRemRentasExIGCAfecImptoAdi_C100_Desde IS NULL)
						 AND ("DividendosExentosIGCoAdicional"						>= p_DividendosExentosIGCoAdicional_Desde OR p_DividendosExentosIGCoAdicional_Desde IS NULL)
						 AND ("DividendosExentosIGCoAdicionalActualizado"			>= p_DividendosExentosIGCoAdicionalActualizado_Desde OR p_DividendosExentosIGCoAdicionalActualizado_Desde IS NULL)
						 AND ("DividendosNoConstitutivosDeRenta"					>= p_DividendosNoConstitutivosDeRenta_Desde OR p_DividendosNoConstitutivosDeRenta_Desde IS NULL)
						 AND ("DividendosNoConstitutivosDeRentaActualizado"			>= p_DividendosNoConstitutivosDeRentaActualizado_Desde OR p_DividendosNoConstitutivosDeRentaActualizado_Desde IS NULL)
						 AND ("DivRemDistNoRentaFE"									>= p_DivRemDistNoRentaFE_Desde OR p_DivRemDistNoRentaFE_Desde IS NULL)
						 AND ("DividendosAfectosImptoUnicoTasa10"					>= p_DividendosAfectosImptoUnicoTasa10_Desde OR p_DividendosAfectosImptoUnicoTasa10_Desde IS NULL)
						 AND ("DividendosAfectosImptoUnicoTasa10Actualizado"		>= p_DividendosAfectosImptoUnicoTasa10Actualizado_Desde OR p_DividendosAfectosImptoUnicoTasa10Actualizado_Desde IS NULL)
						 AND ("DividendosAfectosImptoUnicoTasa4"					>= p_DividendosAfectosImptoUnicoTasa4_Desde OR p_DividendosAfectosImptoUnicoTasa4_Desde IS NULL)
						 AND ("DividendosAfectosImptoUnicoTasa4Actualizado"			>= p_DividendosAfectosImptoUnicoTasa4Actualizado_Desde OR p_DividendosAfectosImptoUnicoTasa4Actualizado_Desde IS NULL)
						 AND ("DistribucionXDiminucionNoImputadaCapital"			>= p_DistribucionXDiminucionNoImputadaCapital_Desde OR p_DistribucionXDiminucionNoImputadaCapital_Desde IS NULL)
						 AND ("DevolucionesDeCapital"								>= p_DevolucionesDeCapital_Desde OR p_DevolucionesDeCapital_Desde IS NULL)
						 AND ("DevolucionesDeCapitalActualizado"					>= p_DevolucionesDeCapitalActualizado_Desde OR p_DevolucionesDeCapitalActualizado_Desde IS NULL)
						 AND ("CredImpGloCompNSRSD"									>= p_CredImpGloCompNSRSD_Desde OR p_CredImpGloCompNSRSD_Desde IS NULL)
						 AND ("CredImpGloCompNSRCD"									>= p_CredImpGloCompNSRCD_Desde OR p_CredImpGloCompNSRCD_Desde IS NULL)
						 AND ("CredIGCAcumD01012017RANSRD01012020SDD_C101"			>= p_CredIGCAcumD01012017RANSRD01012020SDD_C101_Desde OR p_CredIGCAcumD01012017RANSRD01012020SDD_C101_Desde IS NULL)
						 AND ("CredIGCAcumD01012017RANSRD01012020CDD_C102"			>= p_CredIGCAcumD01012017RANSRD01012020CDD_C102_Desde OR p_CredIGCAcumD01012017RANSRD01012020CDD_C102_Desde IS NULL)
						 AND ("CredImpGloCompSRSD"									>= p_CredImpGloCompSRSD_Desde OR p_CredImpGloCompSRSD_Desde IS NULL)
						 AND ("CredImpGloCompSRCD"									>= p_CredImpGloCompSRCD_Desde OR p_CredImpGloCompSRCD_Desde IS NULL)
						 AND ("CredIGCAcumD01012017RXSRSDD_C103"					>= p_CredIGCAcumD01012017RXSRSDD_C103_Desde OR p_CredIGCAcumD01012017RXSRSDD_C103_Desde IS NULL)
						 AND ("CredIGCAcumD01012017RXSRCDD_C104"					>= p_CredIGCAcumD01012017RXSRCDD_C104_Desde OR p_CredIGCAcumD01012017RXSRCDD_C104_Desde IS NULL)
						 AND ("CredImpGloCompCTDCIF"								>= p_CredImpGloCompCTDCIF_Desde OR p_CredImpGloCompCTDCIF_Desde IS NULL)
						 AND ("CredImpGloCompSDD31122016"							>= p_CredImpGloCompSDD31122016_Desde OR p_CredImpGloCompSDD31122016_Desde IS NULL)
						 AND ("CredImpGloCompCDD31122016"							>= p_CredImpGloCompCDD31122016_Desde OR p_CredImpGloCompCDD31122016_Desde IS NULL)
						 AND ("CredIGCAcumH31122016RXSDD_C105"						>= p_CredIGCAcumH31122016RXSDD_C105_Desde OR p_CredIGCAcumH31122016RXSDD_C105_Desde IS NULL)
						 AND ("CredIGCAcumH31122016RXCDD_C106"						>= p_CredIGCAcumH31122016RXCDD_C106_Desde OR p_CredIGCAcumH31122016RXCDD_C106_Desde IS NULL)
						 AND ("CredImpGloCompCTDCIF31122016"						>= p_CredImpGloCompCTDCIF31122016_Desde OR p_CredImpGloCompCTDCIF31122016_Desde IS NULL)
						 AND ("CredImpGloCompCITAExArt21"							>= p_CredImpGloCompCITAExArt21_Desde OR p_CredImpGloCompCITAExArt21_Desde IS NULL)
						 AND ("MontoInversionenCuotasCFIAnterior93"					>= p_MontoInversionenCuotasCFIAnterior93_Desde OR p_MontoInversionenCuotasCFIAnterior93_Desde IS NULL)
						 AND ("DetBenNetoPerPOS_C113"								>= p_DetBenNetoPerPOS_C113_Desde OR p_DetBenNetoPerPOS_C113_Desde IS NULL)
						 AND ("DetBenNetoPerNEG_C114"								>= p_DetBenNetoPerNEG_C114_Desde OR p_DetBenNetoPerNEG_C114_Desde IS NULL)
						 AND ("FactorActualizacion"									>= p_FactorActualizacion_Desde OR p_FactorActualizacion_Desde IS NULL);
						
					                  
	                    
	                    
	                    
	                    
	                  
	                   
      ELSE
	    p_out_tbl = SELECT
	                    'TOTAL' AS "TYPE"
	                    ,COUNT(1) AS "COUNT"
							,SUM("MontoNominalAportes")									AS "MontoNominalAportes"
							,SUM("MontoNominalAportesActualizado")						AS "MontoNominalAportesActualizado"
							,SUM("CantidadCuotasParticipe")								AS "CantidadCuotasParticipe"
							,SUM("ValorCuotaPesos")										AS "ValorCuotaPesos"
							,SUM("NumCuotasSerieParticipe")								AS "NumCuotasSerieParticipe"
							,SUM("PorcentajeParticipacion31Dic")						AS "PorcentajeParticipacion31Dic"
							,SUM("ValNominalCuotasRescatadas")							AS "ValNominalCuotasRescatadas"
							,SUM("CantidadCuotasRescatadas")							AS "CantidadCuotasRescatadas"
							,SUM("DiferenciaPorRescateOEnajenacion")					AS "DiferenciaPorRescateOEnajenacion"
							,SUM("DiferenciaPorRescateOEnajenacionActualizado")			AS "DiferenciaPorRescateOEnajenacionActualizado"
							,SUM("DifPorRescateOEnajenacionCFIAcogido107")				AS "DifPorRescateOEnajenacionCFIAcogido107"
							,SUM("DifPorRescateOEnajenacionCFIAcogido107Actualizado")	AS "DifPorRescateOEnajenacionCFIAcogido107Actualizado"
							,SUM("ValNominalInversionaFechaLiquidacion")				AS "ValNominalInversionaFechaLiquidacion"
							,SUM("CantCuotasCedidasPorLiquidacion")						AS "CantCuotasCedidasPorLiquidacion"
							,SUM("DiferenciaValorCuotasFechaLiquidacion")				AS "DiferenciaValorCuotasFechaLiquidacion"
							,SUM("DiferenciaValorCuotasFechaLiquidacionActualizado")	AS "DiferenciaValorCuotasFechaLiquidacionActualizado"
							,SUM("DivRemDistAfectGCConIDPC")							AS "DivRemDistAfectGCConIDPC"
							,SUM("DivRemDistAfectGCConIDPCal31122016")					AS "DivRemDistAfectGCConIDPCal31122016"
							,SUM("DivRemDistAfectGCConIDPCVOL")								AS "DivRemDistAfectGCConIDPCVOL"
							,SUM("DivRemDistAfectGCSINDC")									AS "DivRemDistAfectGCSINDC"
							,SUM("DisRemRentasTribCProvRap_C96")								AS "DisRemRentasTribCProvRap_C96"
							,SUM("DisRemRentasTribCORP_C97")									AS "DisRemRentasTribCORP_C97"
							,SUM("DisRemRentasTribCExDD_C98")									AS "DisRemRentasTribCExDD_C98"
							,SUM("DisRemRentasTribCUtAFFut_C99")							AS "DisRemRentasTribCUtAFFut_C99"
							,SUM("DivRemDistNoRentaFUT")									AS "DivRemDistNoRentaFUT"
							,SUM("DisRemRentasExIGCAfecImptoAdi_C100")					AS "DisRemRentasExIGCAfecImptoAdi_C100"
							,SUM("DividendosExentosIGCoAdicional")						AS "DividendosExentosIGCoAdicional"
							,SUM("DividendosExentosIGCoAdicionalActualizado")				AS "DividendosExentosIGCoAdicionalActualizado"
							,SUM("DividendosNoConstitutivosDeRenta")						AS "DividendosNoConstitutivosDeRenta"
							,SUM("DividendosNoConstitutivosDeRentaActualizado")			AS "DividendosNoConstitutivosDeRentaActualizado"
							,SUM("DivRemDistNoRentaFE")									AS "DivRemDistNoRentaFE"
							,SUM("DividendosAfectosImptoUnicoTasa10")						AS "DividendosAfectosImptoUnicoTasa10"
							,SUM("DividendosAfectosImptoUnicoTasa10Actualizado")				AS "DividendosAfectosImptoUnicoTasa10Actualizado"
							,SUM("DividendosAfectosImptoUnicoTasa4")						AS "DividendosAfectosImptoUnicoTasa4"
							,SUM("DividendosAfectosImptoUnicoTasa4Actualizado")			AS "DividendosAfectosImptoUnicoTasa4Actualizado"
							,SUM("DistribucionXDiminucionNoImputadaCapital")				AS "DistribucionXDiminucionNoImputadaCapital"
							,SUM("DevolucionesDeCapital")									AS "DevolucionesDeCapital"
							,SUM("DevolucionesDeCapitalActualizado")						AS "DevolucionesDeCapitalActualizado"
							,SUM("CredImpGloCompNSRSD")									AS "CredImpGloCompNSRSD"
							,SUM("CredImpGloCompNSRCD")									AS "CredImpGloCompNSRCD"
							,SUM("CredIGCAcumD01012017RANSRD01012020SDD_C101")			AS "CredIGCAcumD01012017RANSRD01012020SDD_C101"
							,SUM("CredIGCAcumD01012017RANSRD01012020CDD_C102")			AS "CredIGCAcumD01012017RANSRD01012020CDD_C102"
							,SUM("CredImpGloCompSRSD")									AS "CredImpGloCompSRSD"
							,SUM("CredImpGloCompSRCD")									AS "CredImpGloCompSRCD"
							,SUM("CredIGCAcumD01012017RXSRSDD_C103")						AS "CredIGCAcumD01012017RXSRSDD_C103"
							,SUM("CredIGCAcumD01012017RXSRCDD_C104")						AS "CredIGCAcumD01012017RXSRCDD_C104"
							,SUM("CredImpGloCompCTDCIF")									AS "CredImpGloCompCTDCIF"
							,SUM("CredImpGloCompSDD31122016")								AS "CredImpGloCompSDD31122016"
							,SUM("CredImpGloCompCDD31122016")								AS "CredImpGloCompCDD31122016"
							,SUM("CredIGCAcumH31122016RXSDD_C105")						AS "CredIGCAcumH31122016RXSDD_C105"
							,SUM("CredIGCAcumH31122016RXCDD_C106")						AS "CredIGCAcumH31122016RXCDD_C106"
							,SUM("CredImpGloCompCTDCIF31122016")							AS "CredImpGloCompCTDCIF31122016"
							,SUM("CredImpGloCompCITAExArt21")								AS "CredImpGloCompCITAExArt21"
							,SUM("MontoInversionenCuotasCFIAnterior93")					AS "MontoInversionenCuotasCFIAnterior93"
							,SUM("DetBenNetoPerPOS_C113")									AS "DetBenNetoPerPOS_C113"
							,SUM("DetBenNetoPerNEG_C114")									AS "DetBenNetoPerNEG_C114"
							,SUM("FactorActualizacion")									AS "FactorActualizacion"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1922"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                SELECT
	                    'FILTRO' AS "TYPE"
	                    ,COUNT(1) AS "COUNT"
							,SUM("MontoNominalAportes")									AS "MontoNominalAportes"
							,SUM("MontoNominalAportesActualizado")						AS "MontoNominalAportesActualizado"
							,SUM("CantidadCuotasParticipe")								AS "CantidadCuotasParticipe"
							,SUM("ValorCuotaPesos")										AS "ValorCuotaPesos"
							,SUM("NumCuotasSerieParticipe")								AS "NumCuotasSerieParticipe"
							,SUM("PorcentajeParticipacion31Dic")						AS "PorcentajeParticipacion31Dic"
							,SUM("ValNominalCuotasRescatadas")							AS "ValNominalCuotasRescatadas"
							,SUM("CantidadCuotasRescatadas")							AS "CantidadCuotasRescatadas"
							,SUM("DiferenciaPorRescateOEnajenacion")					AS "DiferenciaPorRescateOEnajenacion"
							,SUM("DiferenciaPorRescateOEnajenacionActualizado")			AS "DiferenciaPorRescateOEnajenacionActualizado"
							,SUM("DifPorRescateOEnajenacionCFIAcogido107")				AS "DifPorRescateOEnajenacionCFIAcogido107"
							,SUM("DifPorRescateOEnajenacionCFIAcogido107Actualizado")	AS "DifPorRescateOEnajenacionCFIAcogido107Actualizado"
							,SUM("ValNominalInversionaFechaLiquidacion")				AS "ValNominalInversionaFechaLiquidacion"
							,SUM("CantCuotasCedidasPorLiquidacion")						AS "CantCuotasCedidasPorLiquidacion"
							,SUM("DiferenciaValorCuotasFechaLiquidacion")				AS "DiferenciaValorCuotasFechaLiquidacion"
							,SUM("DiferenciaValorCuotasFechaLiquidacionActualizado")	AS "DiferenciaValorCuotasFechaLiquidacionActualizado"
							,SUM("DivRemDistAfectGCConIDPC")							AS "DivRemDistAfectGCConIDPC"
							,SUM("DivRemDistAfectGCConIDPCal31122016")					AS "DivRemDistAfectGCConIDPCal31122016"
							,SUM("DivRemDistAfectGCConIDPCVOL")								AS "DivRemDistAfectGCConIDPCVOL"
							,SUM("DivRemDistAfectGCSINDC")									AS "DivRemDistAfectGCSINDC"
							,SUM("DisRemRentasTribCProvRap_C96")								AS "DisRemRentasTribCProvRap_C96"
							,SUM("DisRemRentasTribCORP_C97")									AS "DisRemRentasTribCORP_C97"
							,SUM("DisRemRentasTribCExDD_C98")									AS "DisRemRentasTribCExDD_C98"
							,SUM("DisRemRentasTribCUtAFFut_C99")							AS "DisRemRentasTribCUtAFFut_C99"
							,SUM("DivRemDistNoRentaFUT")									AS "DivRemDistNoRentaFUT"
							,SUM("DisRemRentasExIGCAfecImptoAdi_C100")					AS "DisRemRentasExIGCAfecImptoAdi_C100"
							,SUM("DividendosExentosIGCoAdicional")						AS "DividendosExentosIGCoAdicional"
							,SUM("DividendosExentosIGCoAdicionalActualizado")				AS "DividendosExentosIGCoAdicionalActualizado"
							,SUM("DividendosNoConstitutivosDeRenta")						AS "DividendosNoConstitutivosDeRenta"
							,SUM("DividendosNoConstitutivosDeRentaActualizado")			AS "DividendosNoConstitutivosDeRentaActualizado"
							,SUM("DivRemDistNoRentaFE")									AS "DivRemDistNoRentaFE"
							,SUM("DividendosAfectosImptoUnicoTasa10")						AS "DividendosAfectosImptoUnicoTasa10"
							,SUM("DividendosAfectosImptoUnicoTasa10Actualizado")				AS "DividendosAfectosImptoUnicoTasa10Actualizado"
							,SUM("DividendosAfectosImptoUnicoTasa4")						AS "DividendosAfectosImptoUnicoTasa4"
							,SUM("DividendosAfectosImptoUnicoTasa4Actualizado")			AS "DividendosAfectosImptoUnicoTasa4Actualizado"
							,SUM("DistribucionXDiminucionNoImputadaCapital")				AS "DistribucionXDiminucionNoImputadaCapital"
							,SUM("DevolucionesDeCapital")									AS "DevolucionesDeCapital"
							,SUM("DevolucionesDeCapitalActualizado")						AS "DevolucionesDeCapitalActualizado"
							,SUM("CredImpGloCompNSRSD")									AS "CredImpGloCompNSRSD"
							,SUM("CredImpGloCompNSRCD")									AS "CredImpGloCompNSRCD"
							,SUM("CredIGCAcumD01012017RANSRD01012020SDD_C101")			AS "CredIGCAcumD01012017RANSRD01012020SDD_C101"
							,SUM("CredIGCAcumD01012017RANSRD01012020CDD_C102")			AS "CredIGCAcumD01012017RANSRD01012020CDD_C102"
							,SUM("CredImpGloCompSRSD")									AS "CredImpGloCompSRSD"
							,SUM("CredImpGloCompSRCD")									AS "CredImpGloCompSRCD"
							,SUM("CredIGCAcumD01012017RXSRSDD_C103")						AS "CredIGCAcumD01012017RXSRSDD_C103"
							,SUM("CredIGCAcumD01012017RXSRCDD_C104")						AS "CredIGCAcumD01012017RXSRCDD_C104"
							,SUM("CredImpGloCompCTDCIF")									AS "CredImpGloCompCTDCIF"
							,SUM("CredImpGloCompSDD31122016")								AS "CredImpGloCompSDD31122016"
							,SUM("CredImpGloCompCDD31122016")								AS "CredImpGloCompCDD31122016"
							,SUM("CredIGCAcumH31122016RXSDD_C105")						AS "CredIGCAcumH31122016RXSDD_C105"
							,SUM("CredIGCAcumH31122016RXCDD_C106")						AS "CredIGCAcumH31122016RXCDD_C106"
							,SUM("CredImpGloCompCTDCIF31122016")							AS "CredImpGloCompCTDCIF31122016"
							,SUM("CredImpGloCompCITAExArt21")								AS "CredImpGloCompCITAExArt21"
							,SUM("MontoInversionenCuotasCFIAnterior93")					AS "MontoInversionenCuotasCFIAnterior93"
							,SUM("DetBenNetoPerPOS_C113")									AS "DetBenNetoPerPOS_C113"
							,SUM("DetBenNetoPerNEG_C114")									AS "DetBenNetoPerNEG_C114"
							,SUM("FactorActualizacion")									AS "FactorActualizacion"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1922"
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