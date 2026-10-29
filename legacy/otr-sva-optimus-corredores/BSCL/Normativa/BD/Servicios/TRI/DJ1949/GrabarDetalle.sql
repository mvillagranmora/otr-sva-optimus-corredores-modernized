SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1949.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1949.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 								BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 								BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 								NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID 									BIGINT		 ,
    -- ID del documento.
    IN p_IdDocumento 							BIGINT		 ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario 							CHAR(1)		 ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones 							NVARCHAR(250),
    -- Fuente de los datos.
    IN p_FuenteDatos 							NVARCHAR(40) ,
   -- IN p_IdComprobante                          BIGINT       ,
	
	IN p_IdEventoCapitalCliente 				BIGINT,
	IN p_Mercado 								NVARCHAR(3),
	IN p_Nemo 									NVARCHAR(100),
	IN p_TipoRegistro 							NVARCHAR(1),
	IN p_FecMovto 								DATE,
	IN p_SecEvento 								BIGINT,
	IN p_RutSociedad 							NVARCHAR(40),
	IN p_TipoSociedad 							NVARCHAR(1),
	IN p_RutCliente 							NVARCHAR(40),
	IN p_Cuenta 								NVARCHAR(40),
	IN p_CantCuotas3112 						DECIMAL(26,8),
	IN p_Monto 									DECIMAL(26,8),
	IN p_MontoActualizado 						DECIMAL(26,8),
	IN p_MtoDivReaAfecGCCCIDPC_C6 				DECIMAL(26,8),
	IN p_MtoDivReaAfecGCCCIDPCAL311216_C7 		DECIMAL(26,8),
	IN p_MtoDivReaAfecGCCDIDPCVol_C8 			DECIMAL(26,8),
	IN p_MtoDivReaAfecGCSinDer_C9 				DECIMAL(26,8),
	IN p_MtoDivReaREXTribCumpRentasRap_C10 		DECIMAL(26,8),
	IN p_MtoDivReaREXTribCumpOtrasRentas_C11 	DECIMAL(26,8),
	IN p_MtoDivReaREXTribCumpExcDist_C12 		DECIMAL(26,8),
	IN p_MtoDivReaREXTribCumpUtiISFUT_C13 		DECIMAL(26,8),
	IN p_MtoDivReaREXTribCumpRentasISFUT_C14 	DECIMAL(26,8),
	IN p_MtoDivReaREXRentExIGCAfecIAart11_C15 	DECIMAL(26,8),
	IN p_MtoDivReaREXRentExIGCIA_C16 			DECIMAL(26,8),
	IN p_MtoDivReaREXNoConstRenta_C17 			DECIMAL(26,8),
	IN p_CIGCDesde01012017RentAfecNoRestSDD_C18 DECIMAL(26,8),
	IN p_CIGCDesde01012017RentAfecNoRestCDD_C19 DECIMAL(26,8),
	IN p_CIGCDesde01012017RentAfecNoRestSDD_C20 DECIMAL(26,8),
	IN p_CIGCDesde01012017RentAfecNoRestCDD_C21 DECIMAL(26,8),
	IN p_CIGCDesde01012017RentAfecRestSDD_C22 	DECIMAL(26,8),
	IN p_CIGCDesde01012017RentAfecRestCDD_C23 	DECIMAL(26,8),
	IN p_CIGCDesde01012017RentExenRestSDD_C24 	DECIMAL(26,8),
	IN p_CIGCDesde01012017RentExenRestCDD_C25 	DECIMAL(26,8),
	IN p_CIGCDesde01012017CredIPE_C26 			DECIMAL(26,8),
	IN p_CIGCHasta31122016RentAfecSDD_C27 		DECIMAL(26,8),
	IN p_CIGCHasta31122016RentAfecCDD_C28 		DECIMAL(26,8),
	IN p_CIGCHasta31122016RentExenSDD_C29 		DECIMAL(26,8),
	IN p_CIGCHasta31122016RentExenCDD_C30 		DECIMAL(26,8),
	IN p_CIGCHasta31122016CredIPE_C31 			DECIMAL(26,8),
	IN p_CIGCImptoTasaAdicional_C32 			DECIMAL(26,8),
	IN p_TasaEfectCredFUT 						DECIMAL(26,8),
	IN p_TasaEfectCredFUNT 						DECIMAL(26,8),
	IN p_DevCapArt17N7LIR_C33 					DECIMAL(26,8),
	IN p_NumCertificado_C34 					BIGINT,
	IN p_AcogidoISFUT		NVARCHAR(1),

    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id 								BIGINT		 ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador 				INT			 ,

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

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1949" 
		SET 
			"IdDocumento"								= p_IdDocumento
			,"VboUsuario"								= p_VboUsuario
			,"Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END)
			,"FuenteDatos"								= p_FuenteDatos
			,"IdEventoCapitalCliente"					= p_IdEventoCapitalCliente
			,"Mercado"									= p_Mercado
			,"Nemo"										= TRIM(p_Nemo)
			,"TipoRegistro"								= p_TipoRegistro
			,"FecMovto"									= p_FecMovto
			,"SecEvento"								= p_SecEvento
			,"RutSociedad"								= p_RutSociedad
			,"TipoSociedad"								= p_TipoSociedad
			,"RutCliente"								= p_RutCliente
			,"Cuenta"									= p_Cuenta
			,"CantCuotas3112"							= p_CantCuotas3112
			,"Monto"									= p_Monto
			,"MontoActualizado"							= p_MontoActualizado
			,"MtoDivReaAfecGCCCIDPC_C6"					= p_MtoDivReaAfecGCCCIDPC_C6
			,"MtoDivReaAfecGCCCIDPCAL311216_C7"			= p_MtoDivReaAfecGCCCIDPCAL311216_C7
			,"MtoDivReaAfecGCCDIDPCVol_C8"				= p_MtoDivReaAfecGCCDIDPCVol_C8
			,"MtoDivReaAfecGCSinDer_C9"					= p_MtoDivReaAfecGCSinDer_C9
			,"MtoDivReaREXTribCumpRentasRap_C10"		= p_MtoDivReaREXTribCumpRentasRap_C10
			,"MtoDivReaREXTribCumpOtrasRentas_C11"		= p_MtoDivReaREXTribCumpOtrasRentas_C11
			,"MtoDivReaREXTribCumpExcDist_C12"			= p_MtoDivReaREXTribCumpExcDist_C12
			,"MtoDivReaREXTribCumpUtiISFUT_C13"			= p_MtoDivReaREXTribCumpUtiISFUT_C13
			,"MtoDivReaREXTribCumpRentasISFUT_C14"		= p_MtoDivReaREXTribCumpRentasISFUT_C14
			,"MtoDivReaREXRentExIGCAfecIAart11_C15"		= p_MtoDivReaREXRentExIGCAfecIAart11_C15
			,"MtoDivReaREXRentExIGCIA_C16"				= p_MtoDivReaREXRentExIGCIA_C16
			,"MtoDivReaREXNoConstRenta_C17"				= p_MtoDivReaREXNoConstRenta_C17
			,"CIGCDesde01012017RentAfecNoRestSDD_C18"	= p_CIGCDesde01012017RentAfecNoRestSDD_C18
			,"CIGCDesde01012017RentAfecNoRestCDD_C19"	= p_CIGCDesde01012017RentAfecNoRestCDD_C19
			,"CIGCDesde01012017RentAfecNoRestSDD_C20"	= p_CIGCDesde01012017RentAfecNoRestSDD_C20
			,"CIGCDesde01012017RentAfecNoRestCDD_C21"	= p_CIGCDesde01012017RentAfecNoRestCDD_C21
			,"CIGCDesde01012017RentAfecRestSDD_C22"		= p_CIGCDesde01012017RentAfecRestSDD_C22
			,"CIGCDesde01012017RentAfecRestCDD_C23"		= p_CIGCDesde01012017RentAfecRestCDD_C23
			,"CIGCDesde01012017RentExenRestSDD_C24"		= p_CIGCDesde01012017RentExenRestSDD_C24
			,"CIGCDesde01012017RentExenRestCDD_C25"		= p_CIGCDesde01012017RentExenRestCDD_C25
			,"CIGCDesde01012017CredIPE_C26"				= p_CIGCDesde01012017CredIPE_C26
			,"CIGCHasta31122016RentAfecSDD_C27"			= p_CIGCHasta31122016RentAfecSDD_C27
			,"CIGCHasta31122016RentAfecCDD_C28"			= p_CIGCHasta31122016RentAfecCDD_C28
			,"CIGCHasta31122016RentExenSDD_C29"			= p_CIGCHasta31122016RentExenSDD_C29
			,"CIGCHasta31122016RentExenCDD_C30"			= p_CIGCHasta31122016RentExenCDD_C30
			,"CIGCHasta31122016CredIPE_C31"				= p_CIGCHasta31122016CredIPE_C31
			,"CIGCImptoTasaAdicional_C32"				= p_CIGCImptoTasaAdicional_C32
			,"TasaEfectCredFUT"							= p_TasaEfectCredFUT
			,"TasaEfectCredFUNT"						= p_TasaEfectCredFUNT
			,"DevCapArt17N7LIR_C33"						= p_DevCapArt17N7LIR_C33
			,"NumCertificado_C34"						= p_NumCertificado_C34
			,"AcogidoISFUT" = CASE WHEN p_AcogidoISFUT = 'S' OR p_AcogidoISFUT = 'N' THEN p_AcogidoISFUT ELSE "AcogidoISFUT" END
WHERE "ID" = p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1949";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1949"(
            "ID"
			,"IdDocumento"
			,"VboUsuario"
			,"Observaciones"
			,"FuenteDatos"							
			,"IdEventoCapitalCliente"					
			,"Mercado"									
			,"Nemo"										
			,"TipoRegistro"								
			,"FecMovto"									
			,"SecEvento"								
			,"RutSociedad"								
			,"TipoSociedad"								
			,"RutCliente"								
			,"Cuenta"									
			,"CantCuotas3112"							
			,"Monto"									
			,"MontoActualizado"							
			,"MtoDivReaAfecGCCCIDPC_C6"					
			,"MtoDivReaAfecGCCCIDPCAL311216_C7"			
			,"MtoDivReaAfecGCCDIDPCVol_C8"				
			,"MtoDivReaAfecGCSinDer_C9"					
			,"MtoDivReaREXTribCumpRentasRap_C10"		
			,"MtoDivReaREXTribCumpOtrasRentas_C11"		
			,"MtoDivReaREXTribCumpExcDist_C12"			
			,"MtoDivReaREXTribCumpUtiISFUT_C13"			
			,"MtoDivReaREXTribCumpRentasISFUT_C14"		
			,"MtoDivReaREXRentExIGCAfecIAart11_C15"		
			,"MtoDivReaREXRentExIGCIA_C16"				
			,"MtoDivReaREXNoConstRenta_C17"				
			,"CIGCDesde01012017RentAfecNoRestSDD_C18"	
			,"CIGCDesde01012017RentAfecNoRestCDD_C19"	
			,"CIGCDesde01012017RentAfecNoRestSDD_C20"	
			,"CIGCDesde01012017RentAfecNoRestCDD_C21"	
			,"CIGCDesde01012017RentAfecRestSDD_C22"		
			,"CIGCDesde01012017RentAfecRestCDD_C23"		
			,"CIGCDesde01012017RentExenRestSDD_C24"		
			,"CIGCDesde01012017RentExenRestCDD_C25"		
			,"CIGCDesde01012017CredIPE_C26"				
			,"CIGCHasta31122016RentAfecSDD_C27"			
			,"CIGCHasta31122016RentAfecCDD_C28"			
			,"CIGCHasta31122016RentExenSDD_C29"			
			,"CIGCHasta31122016RentExenCDD_C30"			
			,"CIGCHasta31122016CredIPE_C31"				
			,"CIGCImptoTasaAdicional_C32"				
			,"TasaEfectCredFUT"							
			,"TasaEfectCredFUNT"						
			,"DevCapArt17N7LIR_C33"						
			,"NumCertificado_C34"						
			, "AcogidoISFUT"
        ) values (
             p_out_id
			,p_IdDocumento
			,'N'
			,p_Observaciones
			,'MANTENEDOR'
			,p_IdEventoCapitalCliente
			,p_Mercado
			,TRIM(p_Nemo)
			,p_TipoRegistro
			,p_FecMovto
			,p_SecEvento
			,p_RutSociedad
			,p_TipoSociedad
			,p_RutCliente
			,p_Cuenta
			,p_CantCuotas3112
			,p_Monto
			,p_MontoActualizado
			,p_MtoDivReaAfecGCCCIDPC_C6
			,p_MtoDivReaAfecGCCCIDPCAL311216_C7
			,p_MtoDivReaAfecGCCDIDPCVol_C8
			,p_MtoDivReaAfecGCSinDer_C9
			,p_MtoDivReaREXTribCumpRentasRap_C10
			,p_MtoDivReaREXTribCumpOtrasRentas_C11
			,p_MtoDivReaREXTribCumpExcDist_C12
			,p_MtoDivReaREXTribCumpUtiISFUT_C13
			,p_MtoDivReaREXTribCumpRentasISFUT_C14
			,p_MtoDivReaREXRentExIGCAfecIAart11_C15
			,p_MtoDivReaREXRentExIGCIA_C16
			,p_MtoDivReaREXNoConstRenta_C17
			,p_CIGCDesde01012017RentAfecNoRestSDD_C18
			,p_CIGCDesde01012017RentAfecNoRestCDD_C19
			,p_CIGCDesde01012017RentAfecNoRestSDD_C20
			,p_CIGCDesde01012017RentAfecNoRestCDD_C21
			,p_CIGCDesde01012017RentAfecRestSDD_C22
			,p_CIGCDesde01012017RentAfecRestCDD_C23
			,p_CIGCDesde01012017RentExenRestSDD_C24
			,p_CIGCDesde01012017RentExenRestCDD_C25
			,p_CIGCDesde01012017CredIPE_C26
			,p_CIGCHasta31122016RentAfecSDD_C27
			,p_CIGCHasta31122016RentAfecCDD_C28
			,p_CIGCHasta31122016RentExenSDD_C29
			,p_CIGCHasta31122016RentExenCDD_C30
			,p_CIGCHasta31122016CredIPE_C31
			,p_CIGCImptoTasaAdicional_C32
			,p_TasaEfectCredFUT
			,p_TasaEfectCredFUNT
			,p_DevCapArt17N7LIR_C33
			,p_NumCertificado_C34
			,p_AcogidoISFUT
        );
    END IF;
END;