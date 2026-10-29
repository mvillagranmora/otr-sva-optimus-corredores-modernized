SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1949.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1949.ObtenerTotales"
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
						    
			
							IN p_IdEventoCapitalCliente_Desde 							BIGINT			DEFAULT NULL,
							IN p_IdEventoCapitalCliente_Hasta 							BIGINT			DEFAULT NULL,
							IN p_Mercado												NVARCHAR(3) 	DEFAULT NULL,
							IN p_Nemo													NVARCHAR(100)	DEFAULT NULL,
							--IN p_TipoRegistro											NVARCHAR(1)		DEFAULT NULL,
							IN p_FecMovto_Desde											DATE			DEFAULT NULL,
							IN p_FecMovto_Hasta											DATE			DEFAULT NULL,
							
							IN p_SecEvento_Desde										BIGINT			DEFAULT NULL,
							IN p_SecEvento_Hasta										BIGINT			DEFAULT NULL,
							IN p_RutSociedad											NVARCHAR(40)	DEFAULT NULL,
							IN p_TipoSociedad											NVARCHAR(1)		DEFAULT NULL,
							IN p_RutCliente												NVARCHAR(40)	DEFAULT NULL,
							IN p_Cuenta													NVARCHAR(40)	DEFAULT NULL,
							IN p_CantCuotas3112_Desde									DECIMAL(26,8) DEFAULT NULL,
							IN p_CantCuotas3112_Hasta									DECIMAL(26,8) DEFAULT NULL,
							IN p_Monto_Desde											DECIMAL(26,8) DEFAULT NULL,
							IN p_Monto_Hasta											DECIMAL(26,8) DEFAULT NULL,
							IN p_MontoActualizado_Desde									DECIMAL(26,8) DEFAULT NULL,
							IN p_MontoActualizado_Hasta									DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaAfecGCCCIDPC_C6_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaAfecGCCCIDPC_C6_Hasta							DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaAfecGCCCIDPCAL311216_C7_Desde					DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaAfecGCCCIDPCAL311216_C7_Hasta					DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaAfecGCCDIDPCVol_C8_Desde						DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaAfecGCCDIDPCVol_C8_Hasta						DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaAfecGCSinDer_C9_Desde							DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaAfecGCSinDer_C9_Hasta							DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXTribCumpRentasRap_C10_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXTribCumpRentasRap_C10_Hasta				DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXTribCumpOtrasRentas_C11_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXTribCumpOtrasRentas_C11_Hasta				DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXTribCumpExcDist_C12_Desde					DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXTribCumpExcDist_C12_Hasta					DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXTribCumpUtiISFUT_C13_Desde					DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXTribCumpUtiISFUT_C13_Hasta					DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXTribCumpRentasISFUT_C14_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXTribCumpRentasISFUT_C14_Hasta				DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXRentExIGCAfecIAart11_C15_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXRentExIGCAfecIAart11_C15_Hasta				DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXRentExIGCIA_C16_Desde						DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXRentExIGCIA_C16_Hasta						DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXNoConstRenta_C17_Desde						DECIMAL(26,8) DEFAULT NULL,
							IN p_MtoDivReaREXNoConstRenta_C17_Hasta						DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecNoRestSDD_C18_Desde			DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecNoRestSDD_C18_Hasta			DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecNoRestCDD_C19_Desde			DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecNoRestCDD_C19_Hasta			DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecNoRestSDD_C20_Desde			DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecNoRestSDD_C20_Hasta			DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecNoRestCDD_C21_Desde			DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecNoRestCDD_C21_Hasta			DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecRestSDD_C22_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecRestSDD_C22_Hasta				DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecRestCDD_C23_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentAfecRestCDD_C23_Hasta				DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentExenRestSDD_C24_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentExenRestSDD_C24_Hasta				DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentExenRestCDD_C25_Desde				DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017RentExenRestCDD_C25_Hasta				DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017CredIPE_C26_Desde						DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCDesde01012017CredIPE_C26_Hasta						DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCHasta31122016RentAfecSDD_C27_Desde					DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCHasta31122016RentAfecSDD_C27_Hasta					DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCHasta31122016RentAfecCDD_C28_Desde					DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCHasta31122016RentAfecCDD_C28_Hasta					DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCHasta31122016RentExenSDD_C29_Desde					DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCHasta31122016RentExenSDD_C29_Hasta					DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCHasta31122016RentExenCDD_C30_Desde					DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCHasta31122016RentExenCDD_C30_Hasta					DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCHasta31122016CredIPE_C31_Desde						DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCHasta31122016CredIPE_C31_Hasta						DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCImptoTasaAdicional_C32_Desde						DECIMAL(26,8) DEFAULT NULL,
							IN p_CIGCImptoTasaAdicional_C32_Hasta						DECIMAL(26,8) DEFAULT NULL,
							IN p_TasaEfectCredFUT_Desde									DECIMAL(26,8) DEFAULT NULL,
							IN p_TasaEfectCredFUT_Hasta									DECIMAL(26,8) DEFAULT NULL,
							IN p_TasaEfectCredFUNT_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_TasaEfectCredFUNT_Hasta								DECIMAL(26,8) DEFAULT NULL,
							IN p_DevCapArt17N7LIR_C33_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_DevCapArt17N7LIR_C33_Hasta								DECIMAL(26,8) DEFAULT NULL,
							IN p_NumCertificado_C34_Desde								DECIMAL(26,8) DEFAULT NULL,
							IN p_NumCertificado_C34_Hasta								DECIMAL(26,8) DEFAULT NULL,
							
							IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 													NVARCHAR(100),
        "COUNT" 												BIGINT,
		"Monto"													DECIMAL(26,8),
		"MontoActualizado"										DECIMAL(26,8),
		"MtoDivReaAfecGCCCIDPC_C6" 								DECIMAL(26,8),
		"MtoDivReaAfecGCCCIDPCAL311216_C7"						DECIMAL(26,8),
		"MtoDivReaAfecGCCDIDPCVol_C8"							DECIMAL(26,8),
		"MtoDivReaAfecGCSinDer_C9"								DECIMAL(26,8),
		"MtoDivReaREXTribCumpRentasRap_C10"						DECIMAL(26,8),
		"MtoDivReaREXTribCumpOtrasRentas_C11"					DECIMAL(26,8),
		"MtoDivReaREXTribCumpExcDist_C12" 						DECIMAL(26,8),
		"MtoDivReaREXTribCumpUtiISFUT_C13"						DECIMAL(26,8),
		"MtoDivReaREXTribCumpRentasISFUT_C14"					DECIMAL(26,8),
		"MtoDivReaREXRentExIGCAfecIAart11_C15"					DECIMAL(26,8),
		"MtoDivReaREXRentExIGCIA_C16"							DECIMAL(26,8),
		"MtoDivReaREXNoConstRenta_C17"							DECIMAL(26,8),
		"CIGCDesde01012017RentAfecNoRestSDD_C18"				DECIMAL(26,8),
		"CIGCDesde01012017RentAfecNoRestCDD_C19"				DECIMAL(26,8),
		"CIGCDesde01012017RentAfecNoRestSDD_C20"				DECIMAL(26,8),
		"CIGCDesde01012017RentAfecNoRestCDD_C21"				DECIMAL(26,8),
		"CIGCDesde01012017RentAfecRestSDD_C22"					DECIMAL(26,8),
		"CIGCDesde01012017RentAfecRestCDD_C23"					DECIMAL(26,8),
		"CIGCDesde01012017RentExenRestSDD_C24"					DECIMAL(26,8),
		"CIGCDesde01012017RentExenRestCDD_C25"					DECIMAL(26,8),
		"CIGCDesde01012017CredIPE_C26"							DECIMAL(26,8),
		"CIGCHasta31122016RentAfecSDD_C27"						DECIMAL(26,8),
		"CIGCHasta31122016RentAfecCDD_C28"						DECIMAL(26,8),
		"CIGCHasta31122016RentExenSDD_C29"						DECIMAL(26,8),
		"CIGCHasta31122016RentExenCDD_C30"						DECIMAL(26,8),
		"CIGCHasta31122016CredIPE_C31"							DECIMAL(26,8),
		"CIGCImptoTasaAdicional_C32"							DECIMAL(26,8),
		"TasaEfectCredFUT"										DECIMAL(26,8),
		"TasaEfectCredFUNT"										DECIMAL(26,8),
		"DevCapArt17N7LIR_C33"									DECIMAL(26,8)
        
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
	                    
	                    ,SUM("Monto")														AS "Monto"
						,SUM("MontoActualizado")											AS "MontoActualizado"
						,SUM(ROUND("MtoDivReaAfecGCCCIDPC_C6"))								AS "MtoDivReaAfecGCCCIDPC_C6"
						,SUM(ROUND("MtoDivReaAfecGCCCIDPCAL311216_C7"))						AS "MtoDivReaAfecGCCCIDPCAL311216_C7"
						,SUM(ROUND("MtoDivReaAfecGCCDIDPCVol_C8"))							AS "MtoDivReaAfecGCCDIDPCVol_C8"
						,SUM(ROUND("MtoDivReaAfecGCSinDer_C9"))								AS "MtoDivReaAfecGCSinDer_C9"
						,SUM(ROUND("MtoDivReaREXTribCumpRentasRap_C10"))					AS "MtoDivReaREXTribCumpRentasRap_C10"
						,SUM(ROUND("MtoDivReaREXTribCumpOtrasRentas_C11"))					AS "MtoDivReaREXTribCumpOtrasRentas_C11"
						,SUM(ROUND("MtoDivReaREXTribCumpExcDist_C12"))						AS "MtoDivReaREXTribCumpExcDist_C12"
						,SUM(ROUND("MtoDivReaREXTribCumpUtiISFUT_C13"))						AS "MtoDivReaREXTribCumpUtiISFUT_C13"
						,SUM(ROUND("MtoDivReaREXTribCumpRentasISFUT_C14"))					AS "MtoDivReaREXTribCumpRentasISFUT_C14"
						,SUM(ROUND("MtoDivReaREXRentExIGCAfecIAart11_C15"))					AS "MtoDivReaREXRentExIGCAfecIAart11_C15"
						,SUM(ROUND("MtoDivReaREXRentExIGCIA_C16"))							AS "MtoDivReaREXRentExIGCIA_C16"
						,SUM(ROUND("MtoDivReaREXNoConstRenta_C17"))							AS "MtoDivReaREXNoConstRenta_C17"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestSDD_C18"))				AS "CIGCDesde01012017RentAfecNoRestSDD_C18"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestCDD_C19"))				AS "CIGCDesde01012017RentAfecNoRestCDD_C19"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestSDD_C20"))				AS "CIGCDesde01012017RentAfecNoRestSDD_C20"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestCDD_C21"))				AS "CIGCDesde01012017RentAfecNoRestCDD_C21"
						,SUM(ROUND("CIGCDesde01012017RentAfecRestSDD_C22"))					AS "CIGCDesde01012017RentAfecRestSDD_C22"
						,SUM(ROUND("CIGCDesde01012017RentAfecRestCDD_C23"))					AS "CIGCDesde01012017RentAfecRestCDD_C23"
						,SUM(ROUND("CIGCDesde01012017RentExenRestSDD_C24"))					AS "CIGCDesde01012017RentExenRestSDD_C24"
						,SUM(ROUND("CIGCDesde01012017RentExenRestCDD_C25"))					AS "CIGCDesde01012017RentExenRestCDD_C25"
						,SUM(ROUND("CIGCDesde01012017CredIPE_C26"))							AS "CIGCDesde01012017CredIPE_C26"
						,SUM(ROUND("CIGCHasta31122016RentAfecSDD_C27"))						AS "CIGCHasta31122016RentAfecSDD_C27"
						,SUM(ROUND("CIGCHasta31122016RentAfecCDD_C28"))						AS "CIGCHasta31122016RentAfecCDD_C28"
						,SUM(ROUND("CIGCHasta31122016RentExenSDD_C29"))						AS "CIGCHasta31122016RentExenSDD_C29"
						,SUM(ROUND("CIGCHasta31122016RentExenCDD_C30"))						AS "CIGCHasta31122016RentExenCDD_C30"
						,SUM(ROUND("CIGCHasta31122016CredIPE_C31"))							AS "CIGCHasta31122016CredIPE_C31"
						,SUM(ROUND("CIGCImptoTasaAdicional_C32"))							AS "CIGCImptoTasaAdicional_C32"
						,SUM("TasaEfectCredFUT")											AS "TasaEfectCredFUT"
						,SUM("TasaEfectCredFUNT")											AS "TasaEfectCredFUNT"
						,SUM(ROUND("DevCapArt17N7LIR_C33"))									AS "DevCapArt17N7LIR_C33"
                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1949"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                select
	                    'FILTRO' AS "TYPE"
	                    ,COUNT(1) AS "COUNT"
					 	,SUM("Monto")														AS "Monto"
						,SUM("MontoActualizado")											AS "MontoActualizado"
						,SUM(ROUND("MtoDivReaAfecGCCCIDPC_C6"))								AS "MtoDivReaAfecGCCCIDPC_C6"
						,SUM(ROUND("MtoDivReaAfecGCCCIDPCAL311216_C7"))						AS "MtoDivReaAfecGCCCIDPCAL311216_C7"
						,SUM(ROUND("MtoDivReaAfecGCCDIDPCVol_C8"))							AS "MtoDivReaAfecGCCDIDPCVol_C8"
						,SUM(ROUND("MtoDivReaAfecGCSinDer_C9"))								AS "MtoDivReaAfecGCSinDer_C9"
						,SUM(ROUND("MtoDivReaREXTribCumpRentasRap_C10"))					AS "MtoDivReaREXTribCumpRentasRap_C10"
						,SUM(ROUND("MtoDivReaREXTribCumpOtrasRentas_C11"))					AS "MtoDivReaREXTribCumpOtrasRentas_C11"
						,SUM(ROUND("MtoDivReaREXTribCumpExcDist_C12"))						AS "MtoDivReaREXTribCumpExcDist_C12"
						,SUM(ROUND("MtoDivReaREXTribCumpUtiISFUT_C13"))						AS "MtoDivReaREXTribCumpUtiISFUT_C13"
						,SUM(ROUND("MtoDivReaREXTribCumpRentasISFUT_C14"))					AS "MtoDivReaREXTribCumpRentasISFUT_C14"
						,SUM(ROUND("MtoDivReaREXRentExIGCAfecIAart11_C15"))					AS "MtoDivReaREXRentExIGCAfecIAart11_C15"
						,SUM(ROUND("MtoDivReaREXRentExIGCIA_C16"))							AS "MtoDivReaREXRentExIGCIA_C16"
						,SUM(ROUND("MtoDivReaREXNoConstRenta_C17"))							AS "MtoDivReaREXNoConstRenta_C17"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestSDD_C18"))				AS "CIGCDesde01012017RentAfecNoRestSDD_C18"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestCDD_C19"))				AS "CIGCDesde01012017RentAfecNoRestCDD_C19"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestSDD_C20"))				AS "CIGCDesde01012017RentAfecNoRestSDD_C20"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestCDD_C21"))				AS "CIGCDesde01012017RentAfecNoRestCDD_C21"
						,SUM(ROUND("CIGCDesde01012017RentAfecRestSDD_C22"))					AS "CIGCDesde01012017RentAfecRestSDD_C22"
						,SUM(ROUND("CIGCDesde01012017RentAfecRestCDD_C23"))					AS "CIGCDesde01012017RentAfecRestCDD_C23"
						,SUM(ROUND("CIGCDesde01012017RentExenRestSDD_C24"))					AS "CIGCDesde01012017RentExenRestSDD_C24"
						,SUM(ROUND("CIGCDesde01012017RentExenRestCDD_C25"))					AS "CIGCDesde01012017RentExenRestCDD_C25"
						,SUM(ROUND("CIGCDesde01012017CredIPE_C26"))							AS "CIGCDesde01012017CredIPE_C26"
						,SUM(ROUND("CIGCHasta31122016RentAfecSDD_C27"))						AS "CIGCHasta31122016RentAfecSDD_C27"
						,SUM(ROUND("CIGCHasta31122016RentAfecCDD_C28"))						AS "CIGCHasta31122016RentAfecCDD_C28"
						,SUM(ROUND("CIGCHasta31122016RentExenSDD_C29"))						AS "CIGCHasta31122016RentExenSDD_C29"
						,SUM(ROUND("CIGCHasta31122016RentExenCDD_C30"))						AS "CIGCHasta31122016RentExenCDD_C30"
						,SUM(ROUND("CIGCHasta31122016CredIPE_C31"))							AS "CIGCHasta31122016CredIPE_C31"
						,SUM(ROUND("CIGCImptoTasaAdicional_C32"))							AS "CIGCImptoTasaAdicional_C32"
						,SUM("TasaEfectCredFUT")											AS "TasaEfectCredFUT"
						,SUM("TasaEfectCredFUNT")											AS "TasaEfectCredFUNT"
						,SUM(ROUND("DevCapArt17N7LIR_C33"))									AS "DevCapArt17N7LIR_C33"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1949"
	                    WHERE "IdDocumento" = p_IdDocumento
	                    AND ("VboUsuario" LIKE p_VboUsuario or p_VboUsuario IS NULL)
	                    AND ("Observaciones" LIKE p_Observaciones or p_Observaciones IS NULL)
	                    AND ("FuenteDatos" LIKE p_FuenteDatos or p_FuenteDatos IS NULL)

	                   AND ("IdEventoCapitalCliente"					>= p_IdEventoCapitalCliente_Desde OR p_IdEventoCapitalCliente_Desde   IS NULL)
							AND ("IdEventoCapitalCliente"					<= p_IdEventoCapitalCliente_Hasta OR p_IdEventoCapitalCliente_Hasta   IS NULL)
							AND ("FecMovto"									>= p_FecMovto_Desde OR p_FecMovto_Desde   IS NULL)
							AND ("FecMovto"									<= p_FecMovto_Hasta OR p_FecMovto_Hasta   IS NULL)
							AND ("SecEvento"								>= p_SecEvento_Desde OR p_SecEvento_Desde   IS NULL)
							AND ("SecEvento"								<= p_SecEvento_Hasta OR p_SecEvento_Hasta   IS NULL)
					
							AND (trim("Mercado") like trim(p_Mercado) or p_Mercado is null)
							AND (trim("Nemo") like trim(p_Nemo) or p_Nemo is null)
							--AND (trim("TipoRegistro") like trim(p_TipoRegistro) or p_TipoRegistro is null)
							
							AND (trim("RutSociedad") like trim(p_RutSociedad) or p_RutSociedad is null)
							AND (trim("TipoSociedad") like trim(p_TipoSociedad) or p_TipoSociedad is null)
							AND (trim("RutCliente") like trim(p_RutCliente) or p_RutCliente is null)
							AND (trim("Cuenta") like trim(p_Cuenta) or p_Cuenta is null)
							
							 AND ("CantCuotas3112"					>= p_CantCuotas3112_Desde OR p_CantCuotas3112_Desde   IS NULL)
							 AND ("CantCuotas3112"					<= p_CantCuotas3112_Hasta OR p_CantCuotas3112_Hasta   IS NULL)
							 AND ("Monto"							>= p_Monto_Desde		 OR p_Monto_Desde   IS NULL)
							 AND ("Monto"							<= p_Monto_Hasta		 OR p_Monto_Hasta   IS NULL)
							 AND ("MontoActualizado"					>= p_MontoActualizado_Desde OR p_MontoActualizado_Desde   IS NULL)
							 AND ("MontoActualizado"					<= p_MontoActualizado_Hasta OR p_MontoActualizado_Hasta   IS NULL)
							 AND ("MtoDivReaAfecGCCCIDPC_C6"					>= p_MtoDivReaAfecGCCCIDPC_C6_Desde OR p_MtoDivReaAfecGCCCIDPC_C6_Desde   IS NULL)
							 AND ("MtoDivReaAfecGCCCIDPC_C6"					<= p_MtoDivReaAfecGCCCIDPC_C6_Hasta OR p_MtoDivReaAfecGCCCIDPC_C6_Hasta   IS NULL)
							 AND ("MtoDivReaAfecGCCCIDPCAL311216_C7"					>= p_MtoDivReaAfecGCCCIDPCAL311216_C7_Desde OR p_MtoDivReaAfecGCCCIDPCAL311216_C7_Desde   IS NULL)
							 AND ("MtoDivReaAfecGCCCIDPCAL311216_C7"					<= p_MtoDivReaAfecGCCCIDPCAL311216_C7_Hasta OR p_MtoDivReaAfecGCCCIDPCAL311216_C7_Hasta   IS NULL)
							 AND ("MtoDivReaAfecGCCDIDPCVol_C8"					>= p_MtoDivReaAfecGCCDIDPCVol_C8_Desde	 OR p_MtoDivReaAfecGCCDIDPCVol_C8_Desde   IS NULL)
							 AND ("MtoDivReaAfecGCCDIDPCVol_C8"					<= p_MtoDivReaAfecGCCDIDPCVol_C8_Hasta	 OR p_MtoDivReaAfecGCCDIDPCVol_C8_Hasta   IS NULL)
							 AND ("MtoDivReaAfecGCSinDer_C9"					>= p_MtoDivReaAfecGCSinDer_C9_Desde OR p_MtoDivReaAfecGCSinDer_C9_Desde   IS NULL)
							 AND ("MtoDivReaAfecGCSinDer_C9"					<= p_MtoDivReaAfecGCSinDer_C9_Hasta OR p_MtoDivReaAfecGCSinDer_C9_Hasta   IS NULL)
							 AND ("MtoDivReaREXTribCumpRentasRap_C10"					>= p_MtoDivReaREXTribCumpRentasRap_C10_Desde OR p_MtoDivReaREXTribCumpRentasRap_C10_Desde   IS NULL)
							 AND ("MtoDivReaREXTribCumpRentasRap_C10"					<= p_MtoDivReaREXTribCumpRentasRap_C10_Hasta OR p_MtoDivReaREXTribCumpRentasRap_C10_Hasta   IS NULL)
							 AND ("MtoDivReaREXTribCumpOtrasRentas_C11"					>= p_MtoDivReaREXTribCumpOtrasRentas_C11_Desde OR p_MtoDivReaREXTribCumpOtrasRentas_C11_Desde   IS NULL)
							 AND ("MtoDivReaREXTribCumpOtrasRentas_C11"					<= p_MtoDivReaREXTribCumpOtrasRentas_C11_Hasta OR p_MtoDivReaREXTribCumpOtrasRentas_C11_Hasta   IS NULL)
							 AND ("MtoDivReaREXTribCumpExcDist_C12"						>= p_MtoDivReaREXTribCumpExcDist_C12_Desde OR p_MtoDivReaREXTribCumpExcDist_C12_Desde   IS NULL)
							 AND ("MtoDivReaREXTribCumpExcDist_C12"						<= p_MtoDivReaREXTribCumpExcDist_C12_Hasta OR p_MtoDivReaREXTribCumpExcDist_C12_Hasta   IS NULL)
							 AND ("MtoDivReaREXTribCumpUtiISFUT_C13"					>= p_MtoDivReaREXTribCumpUtiISFUT_C13_Desde OR p_MtoDivReaREXTribCumpUtiISFUT_C13_Desde   IS NULL)
							 AND ("MtoDivReaREXTribCumpUtiISFUT_C13"					<= p_MtoDivReaREXTribCumpUtiISFUT_C13_Hasta OR p_MtoDivReaREXTribCumpUtiISFUT_C13_Hasta   IS NULL)
							 AND ("MtoDivReaREXTribCumpRentasISFUT_C14"					>= p_MtoDivReaREXTribCumpRentasISFUT_C14_Desde OR p_MtoDivReaREXTribCumpRentasISFUT_C14_Desde   IS NULL)
							 AND ("MtoDivReaREXTribCumpRentasISFUT_C14"					<= p_MtoDivReaREXTribCumpRentasISFUT_C14_Hasta OR p_MtoDivReaREXTribCumpRentasISFUT_C14_Hasta   IS NULL)
							 AND ("MtoDivReaREXRentExIGCAfecIAart11_C15"					>= p_MtoDivReaREXRentExIGCAfecIAart11_C15_Desde OR p_MtoDivReaREXRentExIGCAfecIAart11_C15_Desde   IS NULL)
							 AND ("MtoDivReaREXRentExIGCAfecIAart11_C15"					<= p_MtoDivReaREXRentExIGCAfecIAart11_C15_Hasta OR p_MtoDivReaREXRentExIGCAfecIAart11_C15_Hasta   IS NULL)
							 AND ("MtoDivReaREXRentExIGCIA_C16"						>= p_MtoDivReaREXRentExIGCIA_C16_Desde	 OR p_MtoDivReaREXRentExIGCIA_C16_Desde   IS NULL)
							 AND ("MtoDivReaREXRentExIGCIA_C16"						<= p_MtoDivReaREXRentExIGCIA_C16_Hasta	 OR p_MtoDivReaREXRentExIGCIA_C16_Hasta   IS NULL)
							 AND ("MtoDivReaREXNoConstRenta_C17"					>= p_MtoDivReaREXNoConstRenta_C17_Desde	 OR p_MtoDivReaREXNoConstRenta_C17_Desde   IS NULL)
							 AND ("MtoDivReaREXNoConstRenta_C17"					<= p_MtoDivReaREXNoConstRenta_C17_Hasta	 OR p_MtoDivReaREXNoConstRenta_C17_Hasta   IS NULL)
							 AND ("CIGCDesde01012017RentAfecNoRestSDD_C18"					>= p_CIGCDesde01012017RentAfecNoRestSDD_C18_Desde OR p_CIGCDesde01012017RentAfecNoRestSDD_C18_Desde   IS NULL)
							 AND ("CIGCDesde01012017RentAfecNoRestSDD_C18"					<= p_CIGCDesde01012017RentAfecNoRestSDD_C18_Hasta OR p_CIGCDesde01012017RentAfecNoRestSDD_C18_Hasta   IS NULL)
							 AND ("CIGCDesde01012017RentAfecNoRestCDD_C19"					>= p_CIGCDesde01012017RentAfecNoRestCDD_C19_Desde OR p_CIGCDesde01012017RentAfecNoRestCDD_C19_Desde   IS NULL)
							 AND ("CIGCDesde01012017RentAfecNoRestCDD_C19"					<= p_CIGCDesde01012017RentAfecNoRestCDD_C19_Hasta OR p_CIGCDesde01012017RentAfecNoRestCDD_C19_Hasta   IS NULL)
							 AND ("CIGCDesde01012017RentAfecNoRestSDD_C20"					>= p_CIGCDesde01012017RentAfecNoRestSDD_C20_Desde OR p_CIGCDesde01012017RentAfecNoRestSDD_C20_Desde   IS NULL)
							 AND ("CIGCDesde01012017RentAfecNoRestSDD_C20"					<= p_CIGCDesde01012017RentAfecNoRestSDD_C20_Hasta OR p_CIGCDesde01012017RentAfecNoRestSDD_C20_Hasta   IS NULL)
							 AND ("CIGCDesde01012017RentAfecNoRestCDD_C21"					>= p_CIGCDesde01012017RentAfecNoRestCDD_C21_Desde OR p_CIGCDesde01012017RentAfecNoRestCDD_C21_Desde   IS NULL)
							 AND ("CIGCDesde01012017RentAfecNoRestCDD_C21"					<= p_CIGCDesde01012017RentAfecNoRestCDD_C21_Hasta OR p_CIGCDesde01012017RentAfecNoRestCDD_C21_Hasta   IS NULL)
							 AND ("CIGCDesde01012017RentAfecRestSDD_C22"					>= p_CIGCDesde01012017RentAfecRestSDD_C22_Desde OR p_CIGCDesde01012017RentAfecRestSDD_C22_Desde   IS NULL)
							 AND ("CIGCDesde01012017RentAfecRestSDD_C22"					<= p_CIGCDesde01012017RentAfecRestSDD_C22_Hasta OR p_CIGCDesde01012017RentAfecRestSDD_C22_Hasta   IS NULL)
							 AND ("CIGCDesde01012017RentAfecRestCDD_C23"					>= p_CIGCDesde01012017RentAfecRestCDD_C23_Desde OR p_CIGCDesde01012017RentAfecRestCDD_C23_Desde   IS NULL)
							 AND ("CIGCDesde01012017RentAfecRestCDD_C23"					<= p_CIGCDesde01012017RentAfecRestCDD_C23_Hasta OR p_CIGCDesde01012017RentAfecRestCDD_C23_Hasta   IS NULL)
							 AND ("CIGCDesde01012017RentExenRestSDD_C24"					>= p_CIGCDesde01012017RentExenRestSDD_C24_Desde OR p_CIGCDesde01012017RentExenRestSDD_C24_Desde   IS NULL)
							 AND ("CIGCDesde01012017RentExenRestSDD_C24"					<= p_CIGCDesde01012017RentExenRestSDD_C24_Hasta OR p_CIGCDesde01012017RentExenRestSDD_C24_Hasta   IS NULL)
							 AND ("CIGCDesde01012017RentExenRestCDD_C25"					>= p_CIGCDesde01012017RentExenRestCDD_C25_Desde OR p_CIGCDesde01012017RentExenRestCDD_C25_Desde   IS NULL)
							 AND ("CIGCDesde01012017RentExenRestCDD_C25"					<= p_CIGCDesde01012017RentExenRestCDD_C25_Hasta OR p_CIGCDesde01012017RentExenRestCDD_C25_Hasta   IS NULL)
							 AND ("CIGCDesde01012017CredIPE_C26"							>= p_CIGCDesde01012017CredIPE_C26_Desde	 OR p_CIGCDesde01012017CredIPE_C26_Desde   IS NULL)
							 AND ("CIGCDesde01012017CredIPE_C26"						<= p_CIGCDesde01012017CredIPE_C26_Hasta	 OR p_CIGCDesde01012017CredIPE_C26_Hasta   IS NULL)
							 AND ("CIGCHasta31122016RentAfecSDD_C27"					>= p_CIGCHasta31122016RentAfecSDD_C27_Desde OR p_CIGCHasta31122016RentAfecSDD_C27_Desde   IS NULL)
							 AND ("CIGCHasta31122016RentAfecSDD_C27"					<= p_CIGCHasta31122016RentAfecSDD_C27_Hasta OR p_CIGCHasta31122016RentAfecSDD_C27_Hasta   IS NULL)
							 AND ("CIGCHasta31122016RentAfecCDD_C28"					>= p_CIGCHasta31122016RentAfecCDD_C28_Desde OR p_CIGCHasta31122016RentAfecCDD_C28_Desde   IS NULL)
							 AND ("CIGCHasta31122016RentAfecCDD_C28"					<= p_CIGCHasta31122016RentAfecCDD_C28_Hasta OR p_CIGCHasta31122016RentAfecCDD_C28_Hasta   IS NULL)
							 AND ("CIGCHasta31122016RentExenSDD_C29"					>= p_CIGCHasta31122016RentExenSDD_C29_Desde OR p_CIGCHasta31122016RentExenSDD_C29_Desde   IS NULL)
							 AND ("CIGCHasta31122016RentExenSDD_C29"					<= p_CIGCHasta31122016RentExenSDD_C29_Hasta OR p_CIGCHasta31122016RentExenSDD_C29_Hasta   IS NULL)
							 AND ("CIGCHasta31122016RentExenCDD_C30"					>= p_CIGCHasta31122016RentExenCDD_C30_Desde OR p_CIGCHasta31122016RentExenCDD_C30_Desde   IS NULL)
							 AND ("CIGCHasta31122016RentExenCDD_C30"					<= p_CIGCHasta31122016RentExenCDD_C30_Hasta OR p_CIGCHasta31122016RentExenCDD_C30_Hasta   IS NULL)
							 AND ("CIGCHasta31122016CredIPE_C31"					>= p_CIGCHasta31122016CredIPE_C31_Desde	 OR p_CIGCHasta31122016CredIPE_C31_Desde   IS NULL)
							 AND ("CIGCHasta31122016CredIPE_C31"					<= p_CIGCHasta31122016CredIPE_C31_Hasta	 OR p_CIGCHasta31122016CredIPE_C31_Hasta   IS NULL)
							 AND ("CIGCImptoTasaAdicional_C32"					>= p_CIGCImptoTasaAdicional_C32_Desde	 OR p_CIGCImptoTasaAdicional_C32_Desde   IS NULL)
							 AND ("CIGCImptoTasaAdicional_C32"					<= p_CIGCImptoTasaAdicional_C32_Hasta	 OR p_CIGCImptoTasaAdicional_C32_Hasta   IS NULL)
							 AND ("TasaEfectCredFUT"					>= p_TasaEfectCredFUT_Desde OR p_TasaEfectCredFUT_Desde   IS NULL)
							 AND ("TasaEfectCredFUT"					<= p_TasaEfectCredFUT_Hasta OR p_TasaEfectCredFUT_Hasta   IS NULL)
							 AND ("TasaEfectCredFUNT"					>= p_TasaEfectCredFUNT_Desde	 OR p_TasaEfectCredFUNT_Desde   IS NULL)
							 AND ("TasaEfectCredFUNT"					<= p_TasaEfectCredFUNT_Hasta	 OR p_TasaEfectCredFUNT_Hasta   IS NULL)
							 AND ("DevCapArt17N7LIR_C33"					>= p_DevCapArt17N7LIR_C33_Desde	 OR p_DevCapArt17N7LIR_C33_Desde   IS NULL)
							 AND ("DevCapArt17N7LIR_C33"					<= p_DevCapArt17N7LIR_C33_Hasta	 OR p_DevCapArt17N7LIR_C33_Hasta   IS NULL)
							 AND ("NumCertificado_C34"					>= p_NumCertificado_C34_Desde	 OR p_NumCertificado_C34_Desde   IS NULL)
							 AND ("NumCertificado_C34"					<= p_NumCertificado_C34_Hasta	 OR p_NumCertificado_C34_Hasta   IS NULL);
						
	                    
	                    
	                    
	                    
	                  
	                   
      ELSE
	    p_out_tbl = SELECT
	                    'TOTAL' AS "TYPE"
	                    ,COUNT(1) AS "COUNT"
						,SUM("Monto")														AS "Monto"
						,SUM("MontoActualizado")											AS "MontoActualizado"
						,SUM(ROUND("MtoDivReaAfecGCCCIDPC_C6"))								AS "MtoDivReaAfecGCCCIDPC_C6"
						,SUM(ROUND("MtoDivReaAfecGCCCIDPCAL311216_C7"))						AS "MtoDivReaAfecGCCCIDPCAL311216_C7"
						,SUM(ROUND("MtoDivReaAfecGCCDIDPCVol_C8"))							AS "MtoDivReaAfecGCCDIDPCVol_C8"
						,SUM(ROUND("MtoDivReaAfecGCSinDer_C9"))								AS "MtoDivReaAfecGCSinDer_C9"
						,SUM(ROUND("MtoDivReaREXTribCumpRentasRap_C10"))					AS "MtoDivReaREXTribCumpRentasRap_C10"
						,SUM(ROUND("MtoDivReaREXTribCumpOtrasRentas_C11"))					AS "MtoDivReaREXTribCumpOtrasRentas_C11"
						,SUM(ROUND("MtoDivReaREXTribCumpExcDist_C12"))						AS "MtoDivReaREXTribCumpExcDist_C12"
						,SUM(ROUND("MtoDivReaREXTribCumpUtiISFUT_C13"))						AS "MtoDivReaREXTribCumpUtiISFUT_C13"
						,SUM(ROUND("MtoDivReaREXTribCumpRentasISFUT_C14"))					AS "MtoDivReaREXTribCumpRentasISFUT_C14"
						,SUM(ROUND("MtoDivReaREXRentExIGCAfecIAart11_C15"))					AS "MtoDivReaREXRentExIGCAfecIAart11_C15"
						,SUM(ROUND("MtoDivReaREXRentExIGCIA_C16"))							AS "MtoDivReaREXRentExIGCIA_C16"
						,SUM(ROUND("MtoDivReaREXNoConstRenta_C17"))							AS "MtoDivReaREXNoConstRenta_C17"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestSDD_C18"))				AS "CIGCDesde01012017RentAfecNoRestSDD_C18"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestCDD_C19"))				AS "CIGCDesde01012017RentAfecNoRestCDD_C19"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestSDD_C20"))				AS "CIGCDesde01012017RentAfecNoRestSDD_C20"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestCDD_C21"))				AS "CIGCDesde01012017RentAfecNoRestCDD_C21"
						,SUM(ROUND("CIGCDesde01012017RentAfecRestSDD_C22"))					AS "CIGCDesde01012017RentAfecRestSDD_C22"
						,SUM(ROUND("CIGCDesde01012017RentAfecRestCDD_C23"))					AS "CIGCDesde01012017RentAfecRestCDD_C23"
						,SUM(ROUND("CIGCDesde01012017RentExenRestSDD_C24"))					AS "CIGCDesde01012017RentExenRestSDD_C24"
						,SUM(ROUND("CIGCDesde01012017RentExenRestCDD_C25"))					AS "CIGCDesde01012017RentExenRestCDD_C25"
						,SUM(ROUND("CIGCDesde01012017CredIPE_C26"))							AS "CIGCDesde01012017CredIPE_C26"
						,SUM(ROUND("CIGCHasta31122016RentAfecSDD_C27"))						AS "CIGCHasta31122016RentAfecSDD_C27"
						,SUM(ROUND("CIGCHasta31122016RentAfecCDD_C28"))						AS "CIGCHasta31122016RentAfecCDD_C28"
						,SUM(ROUND("CIGCHasta31122016RentExenSDD_C29"))						AS "CIGCHasta31122016RentExenSDD_C29"
						,SUM(ROUND("CIGCHasta31122016RentExenCDD_C30"))						AS "CIGCHasta31122016RentExenCDD_C30"
						,SUM(ROUND("CIGCHasta31122016CredIPE_C31"))							AS "CIGCHasta31122016CredIPE_C31"
						,SUM(ROUND("CIGCImptoTasaAdicional_C32"))							AS "CIGCImptoTasaAdicional_C32"
						,SUM("TasaEfectCredFUT")											AS "TasaEfectCredFUT"
						,SUM("TasaEfectCredFUNT")											AS "TasaEfectCredFUNT"
						,SUM(ROUND("DevCapArt17N7LIR_C33"))									AS "DevCapArt17N7LIR_C33"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1949"
	                    WHERE "IdDocumento" = p_IdDocumento
	                UNION ALL
	                SELECT
	                    'FILTRO' AS "TYPE"
	                    ,COUNT(1) AS "COUNT"
						,SUM("Monto")														AS "Monto"
						,SUM("MontoActualizado")											AS "MontoActualizado"
						,SUM(ROUND("MtoDivReaAfecGCCCIDPC_C6"))								AS "MtoDivReaAfecGCCCIDPC_C6"
						,SUM(ROUND("MtoDivReaAfecGCCCIDPCAL311216_C7"))						AS "MtoDivReaAfecGCCCIDPCAL311216_C7"
						,SUM(ROUND("MtoDivReaAfecGCCDIDPCVol_C8"))							AS "MtoDivReaAfecGCCDIDPCVol_C8"
						,SUM(ROUND("MtoDivReaAfecGCSinDer_C9"))								AS "MtoDivReaAfecGCSinDer_C9"
						,SUM(ROUND("MtoDivReaREXTribCumpRentasRap_C10"))					AS "MtoDivReaREXTribCumpRentasRap_C10"
						,SUM(ROUND("MtoDivReaREXTribCumpOtrasRentas_C11"))					AS "MtoDivReaREXTribCumpOtrasRentas_C11"
						,SUM(ROUND("MtoDivReaREXTribCumpExcDist_C12"))						AS "MtoDivReaREXTribCumpExcDist_C12"
						,SUM(ROUND("MtoDivReaREXTribCumpUtiISFUT_C13"))						AS "MtoDivReaREXTribCumpUtiISFUT_C13"
						,SUM(ROUND("MtoDivReaREXTribCumpRentasISFUT_C14"))					AS "MtoDivReaREXTribCumpRentasISFUT_C14"
						,SUM(ROUND("MtoDivReaREXRentExIGCAfecIAart11_C15"))					AS "MtoDivReaREXRentExIGCAfecIAart11_C15"
						,SUM(ROUND("MtoDivReaREXRentExIGCIA_C16"))							AS "MtoDivReaREXRentExIGCIA_C16"
						,SUM(ROUND("MtoDivReaREXNoConstRenta_C17"))							AS "MtoDivReaREXNoConstRenta_C17"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestSDD_C18"))				AS "CIGCDesde01012017RentAfecNoRestSDD_C18"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestCDD_C19"))				AS "CIGCDesde01012017RentAfecNoRestCDD_C19"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestSDD_C20"))				AS "CIGCDesde01012017RentAfecNoRestSDD_C20"
						,SUM(ROUND("CIGCDesde01012017RentAfecNoRestCDD_C21"))				AS "CIGCDesde01012017RentAfecNoRestCDD_C21"
						,SUM(ROUND("CIGCDesde01012017RentAfecRestSDD_C22"))					AS "CIGCDesde01012017RentAfecRestSDD_C22"
						,SUM(ROUND("CIGCDesde01012017RentAfecRestCDD_C23"))					AS "CIGCDesde01012017RentAfecRestCDD_C23"
						,SUM(ROUND("CIGCDesde01012017RentExenRestSDD_C24"))					AS "CIGCDesde01012017RentExenRestSDD_C24"
						,SUM(ROUND("CIGCDesde01012017RentExenRestCDD_C25"))					AS "CIGCDesde01012017RentExenRestCDD_C25"
						,SUM(ROUND("CIGCDesde01012017CredIPE_C26"))							AS "CIGCDesde01012017CredIPE_C26"
						,SUM(ROUND("CIGCHasta31122016RentAfecSDD_C27"))						AS "CIGCHasta31122016RentAfecSDD_C27"
						,SUM(ROUND("CIGCHasta31122016RentAfecCDD_C28"))						AS "CIGCHasta31122016RentAfecCDD_C28"
						,SUM(ROUND("CIGCHasta31122016RentExenSDD_C29"))						AS "CIGCHasta31122016RentExenSDD_C29"
						,SUM(ROUND("CIGCHasta31122016RentExenCDD_C30"))						AS "CIGCHasta31122016RentExenCDD_C30"
						,SUM(ROUND("CIGCHasta31122016CredIPE_C31"))							AS "CIGCHasta31122016CredIPE_C31"
						,SUM(ROUND("CIGCImptoTasaAdicional_C32"))							AS "CIGCImptoTasaAdicional_C32"
						,SUM("TasaEfectCredFUT")											AS "TasaEfectCredFUT"
						,SUM("TasaEfectCredFUNT")											AS "TasaEfectCredFUNT"
						,SUM(ROUND("DevCapArt17N7LIR_C33"))									AS "DevCapArt17N7LIR_C33"
	                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1949"
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