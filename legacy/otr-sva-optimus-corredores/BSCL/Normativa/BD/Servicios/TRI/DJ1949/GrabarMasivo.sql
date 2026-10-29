set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1949.GrabarMasivo";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1949.GrabarMasivo"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    in p_IdEmpresa bigint,

    -- ID usuario del login.
    in p_IdUsuario bigint,

    -- ID modulo invocador.
    in p_IdModulo nvarchar(3),

    ------------ Parametros de Entrada Propios ----------

    -- ID del documento.
    in p_IdDocumento bigint,

    -- Rango VoBo del usuario.
    in p_VboUsuario_Desde nvarchar(3) default null,
    in p_VboUsuario_Hasta nvarchar(3) default null,

    -- Filtro observaciones.
    in p_Observaciones nvarchar(252) default null,

    -- Filtro fuentes de datos.
    in p_FuenteDatos nvarchar(40) default null,
    
    
     ---- propios
    
	
	in p_RutSociedad 											nvarchar(42) default null,
	in p_TipoSociedad											NVARCHAR(1)	 default null,
	in p_RutCliente												nvarchar(42) default null,
	in p_CantCuotas3112_Desde									decimal(27,8) DEFAULT NULL,
	in p_CantCuotas3112_Hasta									decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017CredIPE_C26_Desde						decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017CredIPE_C26_Hasta						decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecNoRestCDD_C19_Desde			decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecNoRestCDD_C19_Hasta			decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecNoRestCDD_C21_Desde			decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecNoRestCDD_C21_Hasta			decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecNoRestSDD_C18_Desde			decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecNoRestSDD_C18_Hasta			decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecNoRestSDD_C20_Desde			decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecNoRestSDD_C20_Hasta			decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecRestCDD_C23_Desde				decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecRestCDD_C23_Hasta				decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecRestSDD_C22_Desde				decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentAfecRestSDD_C22_Hasta				decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentExenRestCDD_C25_Desde				decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentExenRestCDD_C25_Hasta				decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentExenRestSDD_C24_Desde				decimal(27,8) DEFAULT NULL,
	in p_CIGCDesde01012017RentExenRestSDD_C24_Hasta				decimal(27,8) DEFAULT NULL,
	in p_CIGCHasta31122016CredIPE_C31_Desde						decimal(27,8) DEFAULT NULL,
	in p_CIGCHasta31122016CredIPE_C31_Hasta						decimal(27,8) DEFAULT NULL,
	in p_CIGCHasta31122016RentAfecCDD_C28_Desde					decimal(27,8) DEFAULT NULL,
	in p_CIGCHasta31122016RentAfecCDD_C28_Hasta					decimal(27,8) DEFAULT NULL,
	in p_CIGCHasta31122016RentAfecSDD_C27_Desde					decimal(27,8) DEFAULT NULL,
	in p_CIGCHasta31122016RentAfecSDD_C27_Hasta					decimal(27,8) DEFAULT NULL,
	in p_CIGCHasta31122016RentExenCDD_C30_Desde					decimal(27,8) DEFAULT NULL,
	in p_CIGCHasta31122016RentExenCDD_C30_Hasta					decimal(27,8) DEFAULT NULL,
	in p_CIGCHasta31122016RentExenSDD_C29_Desde					decimal(27,8) DEFAULT NULL,
	in p_CIGCHasta31122016RentExenSDD_C29_Hasta					decimal(27,8) DEFAULT NULL,
	in p_CIGCImptoTasaAdicional_C32_Desde						decimal(27,8) DEFAULT NULL,
	in p_CIGCImptoTasaAdicional_C32_Hasta						decimal(27,8) DEFAULT NULL,
	in p_Cuenta													NVARCHAR(40)  DEFAULT NULL,
	in p_DevCapArt17N7LIR_C33_Desde							    decimal(27,8) DEFAULT NULL,
	in p_DevCapArt17N7LIR_C33_Hasta							    decimal(27,8) DEFAULT NULL,
	in p_IdEventoCapitalCliente_Desde							decimal(27,8) DEFAULT NULL,
	in p_IdEventoCapitalCliente_Hasta							decimal(27,8) DEFAULT NULL,
	in p_Mercado												NVARCHAR(3)   DEFAULT NULL,
	in p_Monto_Desde							    			decimal(27,8) DEFAULT NULL,
	in p_Monto_Hasta							    			decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaAfecGCCCIDPCAL311216_C7_Desde					decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaAfecGCCCIDPCAL311216_C7_Hasta					decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaAfecGCCCIDPC_C6_Desde							decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaAfecGCCCIDPC_C6_Hasta							decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaAfecGCCDIDPCVol_C8_Desde						decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaAfecGCCDIDPCVol_C8_Hasta						decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaAfecGCSinDer_C9_Desde							decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaAfecGCSinDer_C9_Hasta							decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXNoConstRenta_C17_Desde						decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXNoConstRenta_C17_Hasta						decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXRentExIGCAfecIAart11_C15_Desde				decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXRentExIGCAfecIAart11_C15_Hasta				decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXRentExIGCIA_C16_Desde						decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXRentExIGCIA_C16_Hasta						decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXTribCumpExcDist_C12_Desde					decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXTribCumpExcDist_C12_Hasta					decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXTribCumpOtrasRentas_C11_Desde				decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXTribCumpOtrasRentas_C11_Hasta				decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXTribCumpRentasISFUT_C14_Desde				decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXTribCumpRentasISFUT_C14_Hasta				decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXTribCumpRentasRap_C10_Desde				decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXTribCumpRentasRap_C10_Hasta				decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXTribCumpUtiISFUT_C13_Desde					decimal(27,8) DEFAULT NULL,
	in p_MtoDivReaREXTribCumpUtiISFUT_C13_Hasta					decimal(27,8) DEFAULT NULL,
	in p_Nemo													NVARCHAR(100) DEFAULT NULL,
	in p_NumCertificado_C34_Desde								bigint default null,
	in p_NumCertificado_C34_Hasta								bigint default null,
	in p_SecEvento_Desde										bigint default null,
	in p_SecEvento_Hasta										bigint default null,	
		
    -- Valores a modificar.
    
    in x_VboUsuario char(1) default null,
    in x_Observaciones nvarchar(250) default null,
    
    
    
    ------------ Salidas ----------

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    out p_out_AdvertenciasContador int,

    -- Tabla de advertencias.
    out p_out_AdvertenciasDetalle table("Codigo" nvarchar(40), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    out p_out_ErroresContador int,

    -- Tabla de errores.
    out p_out_ErroresDetalle table("Codigo" nvarchar(40), "Descripcion" nvarchar(5000))
)
    language sqlscript
    sql security invoker
    default schema "BSCL"
as
begin
    ------------ Variables Propias ----------

    ------------ Bloque Comun de Inicializacion ----------

    declare salida_controlada condition;
    declare exit handler for salida_controlada
    begin
        select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;
        select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
    end;

    declare exit handler for sqlexception
    begin
        declare v_errorExt nvarchar(40);
        if ::sql_error_code > 10000 then
            -- Error pseudo-controlado pero desde un sub-sp.
            call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = select * from :p_out_ErroresDetalle union all
                                   select v_errorExt, ::sql_error_message from dummy;
            select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
        else
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            resignal;
        end if;
    end;

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = select '' as "Codigo", '' as "RequiereAutorizacion", '' as "Descripcion" from dummy where 1 = 2;
    p_out_ErroresDetalle = select '' as "Codigo", '' as "Descripcion" from dummy where 1 = 2;

    ------------ Logica de Actualizacion ----------
    
   -- Actualizacion 
   IF x_VboUsuario IS NOT NULL THEN
	update "BSCL.Normativa.BD::TRI.DatosDJ1949"
	set "VboUsuario" = x_VboUsuario
	where "IdDocumento" = p_IdDocumento
	
		and ("VboUsuario" >= p_VboUsuario_Desde or p_VboUsuario_Desde is null)
		and ("VboUsuario" <= p_VboUsuario_Hasta or p_VboUsuario_Hasta is null)		
		and ("Observaciones" 	like p_Observaciones or p_Observaciones is null)
		and ("FuenteDatos" 		like p_FuenteDatos or p_FuenteDatos is null)
	
		and ("RutSociedad" 		like p_RutSociedad or p_RutSociedad is null)
		and ("RutCliente" 		like p_RutCliente or p_RutCliente is null)
		and ("TipoSociedad" 	like p_TipoSociedad or p_TipoSociedad is null)
	
		and ("Cuenta" 			like p_Cuenta or p_Cuenta is null)
		and ("Mercado" 			like p_Mercado or p_Mercado is null)
		and ("Nemo" 			like p_Nemo or p_Nemo is null)
		 
		and ("TipoSociedad" 	like p_TipoSociedad or p_TipoSociedad is null)
	
	
		and ("CantCuotas3112" >= p_CantCuotas3112_Desde or p_CantCuotas3112_Desde is null)
		and ("CantCuotas3112" <= p_CantCuotas3112_Hasta or p_CantCuotas3112_Hasta is null)	

		and ("CIGCDesde01012017CredIPE_C26" >= p_CIGCDesde01012017CredIPE_C26_Desde or p_CIGCDesde01012017CredIPE_C26_Desde is null)
		and ("CIGCDesde01012017CredIPE_C26" <= p_CIGCDesde01012017CredIPE_C26_Hasta or p_CIGCDesde01012017CredIPE_C26_Hasta is null)
		
		and ("CIGCDesde01012017RentAfecNoRestCDD_C19" >= p_CIGCDesde01012017RentAfecNoRestCDD_C19_Desde or p_CIGCDesde01012017RentAfecNoRestCDD_C19_Desde is null)
		and ("CIGCDesde01012017RentAfecNoRestCDD_C19" <= p_CIGCDesde01012017RentAfecNoRestCDD_C19_Hasta or p_CIGCDesde01012017RentAfecNoRestCDD_C19_Hasta is null)	

		and ("CIGCDesde01012017RentAfecNoRestCDD_C21" >= p_CIGCDesde01012017RentAfecNoRestCDD_C21_Desde or p_CIGCDesde01012017RentAfecNoRestCDD_C21_Desde is null)
		and ("CIGCDesde01012017RentAfecNoRestCDD_C21" <= p_CIGCDesde01012017RentAfecNoRestCDD_C21_Hasta or p_CIGCDesde01012017RentAfecNoRestCDD_C21_Hasta is null)	

		and ("CIGCDesde01012017RentAfecNoRestSDD_C18" >= p_CIGCDesde01012017RentAfecNoRestSDD_C18_Desde or p_CIGCDesde01012017RentAfecNoRestSDD_C18_Desde is null)
		and ("CIGCDesde01012017RentAfecNoRestSDD_C18" <= p_CIGCDesde01012017RentAfecNoRestSDD_C18_Hasta or p_CIGCDesde01012017RentAfecNoRestSDD_C18_Hasta is null)	

		and ("CIGCDesde01012017RentAfecNoRestSDD_C20" >= p_CIGCDesde01012017RentAfecNoRestSDD_C20_Desde or p_CIGCDesde01012017RentAfecNoRestSDD_C20_Desde is null)
		and ("CIGCDesde01012017RentAfecNoRestSDD_C20" <= p_CIGCDesde01012017RentAfecNoRestSDD_C20_Hasta or p_CIGCDesde01012017RentAfecNoRestSDD_C20_Hasta is null)	

		and ("CIGCDesde01012017RentAfecRestCDD_C23" >= p_CIGCDesde01012017RentAfecRestCDD_C23_Desde or p_CIGCDesde01012017RentAfecRestCDD_C23_Desde is null)
		and ("CIGCDesde01012017RentAfecRestCDD_C23" <= p_CIGCDesde01012017RentAfecRestCDD_C23_Hasta or p_CIGCDesde01012017RentAfecRestCDD_C23_Hasta is null)	

		and ("CIGCDesde01012017RentAfecRestSDD_C22" >= p_CIGCDesde01012017RentAfecRestSDD_C22_Desde or p_CIGCDesde01012017RentAfecRestSDD_C22_Desde is null)
		and ("CIGCDesde01012017RentAfecRestSDD_C22" <= p_CIGCDesde01012017RentAfecRestSDD_C22_Hasta or p_CIGCDesde01012017RentAfecRestSDD_C22_Hasta is null)	


		and ("CIGCDesde01012017RentExenRestCDD_C25" >= p_CIGCDesde01012017RentExenRestCDD_C25_Desde or p_CIGCDesde01012017RentExenRestCDD_C25_Desde is null)
		and ("CIGCDesde01012017RentExenRestCDD_C25" <= p_CIGCDesde01012017RentExenRestCDD_C25_Hasta or p_CIGCDesde01012017RentExenRestCDD_C25_Hasta is null)	

		and ("CIGCDesde01012017RentExenRestSDD_C24" >= p_CIGCDesde01012017RentExenRestSDD_C24_Desde or p_CIGCDesde01012017RentExenRestSDD_C24_Desde is null)
		and ("CIGCDesde01012017RentExenRestSDD_C24" <= p_CIGCDesde01012017RentExenRestSDD_C24_Hasta or p_CIGCDesde01012017RentExenRestSDD_C24_Hasta is null)	

		and ("CIGCHasta31122016CredIPE_C31" >= p_CIGCHasta31122016CredIPE_C31_Desde or p_CIGCHasta31122016CredIPE_C31_Desde is null)
		and ("CIGCHasta31122016CredIPE_C31" <= p_CIGCHasta31122016CredIPE_C31_Hasta or p_CIGCHasta31122016CredIPE_C31_Hasta is null)	
		
		and ("CIGCHasta31122016RentAfecCDD_C28" >= p_CIGCHasta31122016RentAfecCDD_C28_Desde or p_CIGCHasta31122016RentAfecCDD_C28_Desde is null)
		and ("CIGCHasta31122016RentAfecCDD_C28" <= p_CIGCHasta31122016RentAfecCDD_C28_Hasta or p_CIGCHasta31122016RentAfecCDD_C28_Hasta is null)	

		and ("CIGCHasta31122016RentAfecSDD_C27" >= p_CIGCHasta31122016RentAfecSDD_C27_Desde or p_CIGCHasta31122016RentAfecSDD_C27_Desde is null)
		and ("CIGCHasta31122016RentAfecSDD_C27" <= p_CIGCHasta31122016RentAfecSDD_C27_Hasta or p_CIGCHasta31122016RentAfecSDD_C27_Hasta is null)	

		and ("CIGCHasta31122016RentExenCDD_C30" >= p_CIGCHasta31122016RentExenCDD_C30_Desde or p_CIGCHasta31122016RentExenCDD_C30_Desde is null)
		and ("CIGCHasta31122016RentExenCDD_C30" <= p_CIGCHasta31122016RentExenCDD_C30_Hasta or p_CIGCHasta31122016RentExenCDD_C30_Hasta is null)	

		and ("CIGCHasta31122016RentExenSDD_C29" >= p_CIGCHasta31122016RentExenSDD_C29_Desde or p_CIGCHasta31122016RentExenSDD_C29_Desde is null)
		and ("CIGCHasta31122016RentExenSDD_C29" <= p_CIGCHasta31122016RentExenSDD_C29_Hasta or p_CIGCHasta31122016RentExenSDD_C29_Hasta is null)	

		and ("CIGCImptoTasaAdicional_C32" >= p_CIGCImptoTasaAdicional_C32_Desde or p_CIGCImptoTasaAdicional_C32_Desde is null)
		and ("CIGCImptoTasaAdicional_C32" <= p_CIGCImptoTasaAdicional_C32_Hasta or p_CIGCImptoTasaAdicional_C32_Hasta is null)	

		and ("DevCapArt17N7LIR_C33" >= p_DevCapArt17N7LIR_C33_Desde or p_DevCapArt17N7LIR_C33_Desde is null)
		and ("DevCapArt17N7LIR_C33" <= p_DevCapArt17N7LIR_C33_Hasta or p_DevCapArt17N7LIR_C33_Hasta is null)	

		and ("IdEventoCapitalCliente" >= p_IdEventoCapitalCliente_Desde or p_IdEventoCapitalCliente_Desde is null)
		and ("IdEventoCapitalCliente" <= p_IdEventoCapitalCliente_Hasta or p_IdEventoCapitalCliente_Hasta is null)	

		and ("Monto" >= p_Monto_Desde or p_Monto_Desde is null)
		and ("Monto" <= p_Monto_Hasta or p_Monto_Hasta is null)	

		and ("MtoDivReaAfecGCCCIDPCAL311216_C7" >= p_MtoDivReaAfecGCCCIDPCAL311216_C7_Desde or p_MtoDivReaAfecGCCCIDPCAL311216_C7_Desde is null)
		and ("MtoDivReaAfecGCCCIDPCAL311216_C7" <= p_MtoDivReaAfecGCCCIDPCAL311216_C7_Hasta or p_MtoDivReaAfecGCCCIDPCAL311216_C7_Hasta is null)	

		and ("MtoDivReaAfecGCCCIDPC_C6" >= p_MtoDivReaAfecGCCCIDPC_C6_Desde or p_MtoDivReaAfecGCCCIDPC_C6_Desde is null)
		and ("MtoDivReaAfecGCCCIDPC_C6" <= p_MtoDivReaAfecGCCCIDPC_C6_Hasta or p_MtoDivReaAfecGCCCIDPC_C6_Hasta is null)	

		and ("MtoDivReaAfecGCCDIDPCVol_C8" >= p_MtoDivReaAfecGCCDIDPCVol_C8_Desde or p_MtoDivReaAfecGCCDIDPCVol_C8_Desde is null)
		and ("MtoDivReaAfecGCCDIDPCVol_C8" <= p_MtoDivReaAfecGCCDIDPCVol_C8_Hasta or p_MtoDivReaAfecGCCDIDPCVol_C8_Hasta is null)	
	
		and ("MtoDivReaAfecGCSinDer_C9" >= p_MtoDivReaAfecGCSinDer_C9_Desde or p_MtoDivReaAfecGCSinDer_C9_Desde is null)
		and ("MtoDivReaAfecGCSinDer_C9" <= p_MtoDivReaAfecGCSinDer_C9_Hasta or p_MtoDivReaAfecGCSinDer_C9_Hasta is null)	
	
		and ("MtoDivReaREXNoConstRenta_C17" >= p_MtoDivReaREXNoConstRenta_C17_Desde or p_MtoDivReaREXNoConstRenta_C17_Desde is null)
		and ("MtoDivReaREXNoConstRenta_C17" <= p_MtoDivReaREXNoConstRenta_C17_Hasta or p_MtoDivReaREXNoConstRenta_C17_Hasta is null)	
	
		and ("MtoDivReaREXRentExIGCAfecIAart11_C15" >= p_MtoDivReaREXRentExIGCAfecIAart11_C15_Desde or p_MtoDivReaREXRentExIGCAfecIAart11_C15_Desde is null)
		and ("MtoDivReaREXRentExIGCAfecIAart11_C15" <= p_MtoDivReaREXRentExIGCAfecIAart11_C15_Hasta or p_MtoDivReaREXRentExIGCAfecIAart11_C15_Hasta is null)	
	
		and ("MtoDivReaREXTribCumpExcDist_C12" >= p_MtoDivReaREXTribCumpExcDist_C12_Desde or p_MtoDivReaREXTribCumpExcDist_C12_Desde is null)
		and ("MtoDivReaREXTribCumpExcDist_C12" <= p_MtoDivReaREXTribCumpExcDist_C12_Hasta or p_MtoDivReaREXTribCumpExcDist_C12_Hasta is null)	
	
		and ("MtoDivReaREXTribCumpOtrasRentas_C11" >= p_MtoDivReaREXTribCumpOtrasRentas_C11_Desde or p_MtoDivReaREXTribCumpOtrasRentas_C11_Desde is null)
		and ("MtoDivReaREXTribCumpOtrasRentas_C11" <= p_MtoDivReaREXTribCumpOtrasRentas_C11_Hasta or p_MtoDivReaREXTribCumpOtrasRentas_C11_Hasta is null)	
	
		and ("MtoDivReaREXTribCumpRentasISFUT_C14" >= p_MtoDivReaREXTribCumpRentasISFUT_C14_Desde or p_MtoDivReaREXTribCumpRentasISFUT_C14_Desde is null)
		and ("MtoDivReaREXTribCumpRentasISFUT_C14" <= p_MtoDivReaREXTribCumpRentasISFUT_C14_Hasta or p_MtoDivReaREXTribCumpRentasISFUT_C14_Hasta is null)	
	
		and ("MtoDivReaREXTribCumpRentasRap_C10" >= p_MtoDivReaREXTribCumpRentasRap_C10_Desde or p_MtoDivReaREXTribCumpRentasRap_C10_Desde is null)
		and ("MtoDivReaREXTribCumpRentasRap_C10" <= p_MtoDivReaREXTribCumpRentasRap_C10_Hasta or p_MtoDivReaREXTribCumpRentasRap_C10_Hasta is null)	
		
		and ("MtoDivReaREXTribCumpUtiISFUT_C13" >= p_MtoDivReaREXTribCumpUtiISFUT_C13_Desde or p_MtoDivReaREXTribCumpUtiISFUT_C13_Desde is null)
		and ("MtoDivReaREXTribCumpUtiISFUT_C13" <= p_MtoDivReaREXTribCumpUtiISFUT_C13_Hasta or p_MtoDivReaREXTribCumpUtiISFUT_C13_Hasta is null)	
	
		and ("NumCertificado_C34" >= p_NumCertificado_C34_Desde or p_NumCertificado_C34_Desde is null)
		and ("NumCertificado_C34" <= p_NumCertificado_C34_Hasta or p_NumCertificado_C34_Hasta is null)	
		
		and ("SecEvento" >= p_SecEvento_Desde or p_SecEvento_Desde is null)
		and ("SecEvento" <= p_SecEvento_Hasta or p_SecEvento_Hasta is null);	
		
     	
	
    end if;
   
  
   
      IF x_Observaciones IS NOT NULL THEN
	update "BSCL.Normativa.BD::TRI.DatosDJ1949"
	set "Observaciones" = x_Observaciones
	where "IdDocumento" = p_IdDocumento
	
		and ("VboUsuario" >= p_VboUsuario_Desde or p_VboUsuario_Desde is null)
		and ("VboUsuario" <= p_VboUsuario_Hasta or p_VboUsuario_Hasta is null)		
		and ("Observaciones" 	like p_Observaciones or p_Observaciones is null)
		and ("FuenteDatos" 		like p_FuenteDatos or p_FuenteDatos is null)
	
		and ("RutSociedad" 		like p_RutSociedad or p_RutSociedad is null)
		and ("RutCliente" 		like p_RutCliente or p_RutCliente is null)
		and ("TipoSociedad" 	like p_TipoSociedad or p_TipoSociedad is null)
	
		and ("Cuenta" 			like p_Cuenta or p_Cuenta is null)
		and ("Mercado" 			like p_Mercado or p_Mercado is null)
		and ("Nemo" 			like p_Nemo or p_Nemo is null)
		and ("TipoSociedad" 	like p_TipoSociedad or p_TipoSociedad is null)
	
	
		and ("CantCuotas3112" >= p_CantCuotas3112_Desde or p_CantCuotas3112_Desde is null)
		and ("CantCuotas3112" <= p_CantCuotas3112_Hasta or p_CantCuotas3112_Hasta is null)	

		and ("CIGCDesde01012017CredIPE_C26" >= p_CIGCDesde01012017CredIPE_C26_Desde or p_CIGCDesde01012017CredIPE_C26_Desde is null)
		and ("CIGCDesde01012017CredIPE_C26" <= p_CIGCDesde01012017CredIPE_C26_Hasta or p_CIGCDesde01012017CredIPE_C26_Hasta is null)
		
		and ("CIGCDesde01012017RentAfecNoRestCDD_C19" >= p_CIGCDesde01012017RentAfecNoRestCDD_C19_Desde or p_CIGCDesde01012017RentAfecNoRestCDD_C19_Desde is null)
		and ("CIGCDesde01012017RentAfecNoRestCDD_C19" <= p_CIGCDesde01012017RentAfecNoRestCDD_C19_Hasta or p_CIGCDesde01012017RentAfecNoRestCDD_C19_Hasta is null)	

		and ("CIGCDesde01012017RentAfecNoRestCDD_C21" >= p_CIGCDesde01012017RentAfecNoRestCDD_C21_Desde or p_CIGCDesde01012017RentAfecNoRestCDD_C21_Desde is null)
		and ("CIGCDesde01012017RentAfecNoRestCDD_C21" <= p_CIGCDesde01012017RentAfecNoRestCDD_C21_Hasta or p_CIGCDesde01012017RentAfecNoRestCDD_C21_Hasta is null)	

		and ("CIGCDesde01012017RentAfecNoRestSDD_C18" >= p_CIGCDesde01012017RentAfecNoRestSDD_C18_Desde or p_CIGCDesde01012017RentAfecNoRestSDD_C18_Desde is null)
		and ("CIGCDesde01012017RentAfecNoRestSDD_C18" <= p_CIGCDesde01012017RentAfecNoRestSDD_C18_Hasta or p_CIGCDesde01012017RentAfecNoRestSDD_C18_Hasta is null)	

		and ("CIGCDesde01012017RentAfecNoRestSDD_C20" >= p_CIGCDesde01012017RentAfecNoRestSDD_C20_Desde or p_CIGCDesde01012017RentAfecNoRestSDD_C20_Desde is null)
		and ("CIGCDesde01012017RentAfecNoRestSDD_C20" <= p_CIGCDesde01012017RentAfecNoRestSDD_C20_Hasta or p_CIGCDesde01012017RentAfecNoRestSDD_C20_Hasta is null)	

		and ("CIGCDesde01012017RentAfecRestCDD_C23" >= p_CIGCDesde01012017RentAfecRestCDD_C23_Desde or p_CIGCDesde01012017RentAfecRestCDD_C23_Desde is null)
		and ("CIGCDesde01012017RentAfecRestCDD_C23" <= p_CIGCDesde01012017RentAfecRestCDD_C23_Hasta or p_CIGCDesde01012017RentAfecRestCDD_C23_Hasta is null)	

		and ("CIGCDesde01012017RentAfecRestSDD_C22" >= p_CIGCDesde01012017RentAfecRestSDD_C22_Desde or p_CIGCDesde01012017RentAfecRestSDD_C22_Desde is null)
		and ("CIGCDesde01012017RentAfecRestSDD_C22" <= p_CIGCDesde01012017RentAfecRestSDD_C22_Hasta or p_CIGCDesde01012017RentAfecRestSDD_C22_Hasta is null)	


		and ("CIGCDesde01012017RentExenRestCDD_C25" >= p_CIGCDesde01012017RentExenRestCDD_C25_Desde or p_CIGCDesde01012017RentExenRestCDD_C25_Desde is null)
		and ("CIGCDesde01012017RentExenRestCDD_C25" <= p_CIGCDesde01012017RentExenRestCDD_C25_Hasta or p_CIGCDesde01012017RentExenRestCDD_C25_Hasta is null)	

		and ("CIGCDesde01012017RentExenRestSDD_C24" >= p_CIGCDesde01012017RentExenRestSDD_C24_Desde or p_CIGCDesde01012017RentExenRestSDD_C24_Desde is null)
		and ("CIGCDesde01012017RentExenRestSDD_C24" <= p_CIGCDesde01012017RentExenRestSDD_C24_Hasta or p_CIGCDesde01012017RentExenRestSDD_C24_Hasta is null)	

		and ("CIGCHasta31122016CredIPE_C31" >= p_CIGCHasta31122016CredIPE_C31_Desde or p_CIGCHasta31122016CredIPE_C31_Desde is null)
		and ("CIGCHasta31122016CredIPE_C31" <= p_CIGCHasta31122016CredIPE_C31_Hasta or p_CIGCHasta31122016CredIPE_C31_Hasta is null)	
		
		and ("CIGCHasta31122016RentAfecCDD_C28" >= p_CIGCHasta31122016RentAfecCDD_C28_Desde or p_CIGCHasta31122016RentAfecCDD_C28_Desde is null)
		and ("CIGCHasta31122016RentAfecCDD_C28" <= p_CIGCHasta31122016RentAfecCDD_C28_Hasta or p_CIGCHasta31122016RentAfecCDD_C28_Hasta is null)	

		and ("CIGCHasta31122016RentAfecSDD_C27" >= p_CIGCHasta31122016RentAfecSDD_C27_Desde or p_CIGCHasta31122016RentAfecSDD_C27_Desde is null)
		and ("CIGCHasta31122016RentAfecSDD_C27" <= p_CIGCHasta31122016RentAfecSDD_C27_Hasta or p_CIGCHasta31122016RentAfecSDD_C27_Hasta is null)	

		and ("CIGCHasta31122016RentExenCDD_C30" >= p_CIGCHasta31122016RentExenCDD_C30_Desde or p_CIGCHasta31122016RentExenCDD_C30_Desde is null)
		and ("CIGCHasta31122016RentExenCDD_C30" <= p_CIGCHasta31122016RentExenCDD_C30_Hasta or p_CIGCHasta31122016RentExenCDD_C30_Hasta is null)	

		and ("CIGCHasta31122016RentExenSDD_C29" >= p_CIGCHasta31122016RentExenSDD_C29_Desde or p_CIGCHasta31122016RentExenSDD_C29_Desde is null)
		and ("CIGCHasta31122016RentExenSDD_C29" <= p_CIGCHasta31122016RentExenSDD_C29_Hasta or p_CIGCHasta31122016RentExenSDD_C29_Hasta is null)	

		and ("CIGCImptoTasaAdicional_C32" >= p_CIGCImptoTasaAdicional_C32_Desde or p_CIGCImptoTasaAdicional_C32_Desde is null)
		and ("CIGCImptoTasaAdicional_C32" <= p_CIGCImptoTasaAdicional_C32_Hasta or p_CIGCImptoTasaAdicional_C32_Hasta is null)	

		and ("DevCapArt17N7LIR_C33" >= p_DevCapArt17N7LIR_C33_Desde or p_DevCapArt17N7LIR_C33_Desde is null)
		and ("DevCapArt17N7LIR_C33" <= p_DevCapArt17N7LIR_C33_Hasta or p_DevCapArt17N7LIR_C33_Hasta is null)	

		and ("IdEventoCapitalCliente" >= p_IdEventoCapitalCliente_Desde or p_IdEventoCapitalCliente_Desde is null)
		and ("IdEventoCapitalCliente" <= p_IdEventoCapitalCliente_Hasta or p_IdEventoCapitalCliente_Hasta is null)	

		and ("Monto" >= p_Monto_Desde or p_Monto_Desde is null)
		and ("Monto" <= p_Monto_Hasta or p_Monto_Hasta is null)	

		and ("MtoDivReaAfecGCCCIDPCAL311216_C7" >= p_MtoDivReaAfecGCCCIDPCAL311216_C7_Desde or p_MtoDivReaAfecGCCCIDPCAL311216_C7_Desde is null)
		and ("MtoDivReaAfecGCCCIDPCAL311216_C7" <= p_MtoDivReaAfecGCCCIDPCAL311216_C7_Hasta or p_MtoDivReaAfecGCCCIDPCAL311216_C7_Hasta is null)	

		and ("MtoDivReaAfecGCCCIDPC_C6" >= p_MtoDivReaAfecGCCCIDPC_C6_Desde or p_MtoDivReaAfecGCCCIDPC_C6_Desde is null)
		and ("MtoDivReaAfecGCCCIDPC_C6" <= p_MtoDivReaAfecGCCCIDPC_C6_Hasta or p_MtoDivReaAfecGCCCIDPC_C6_Hasta is null)	

		and ("MtoDivReaAfecGCCDIDPCVol_C8" >= p_MtoDivReaAfecGCCDIDPCVol_C8_Desde or p_MtoDivReaAfecGCCDIDPCVol_C8_Desde is null)
		and ("MtoDivReaAfecGCCDIDPCVol_C8" <= p_MtoDivReaAfecGCCDIDPCVol_C8_Hasta or p_MtoDivReaAfecGCCDIDPCVol_C8_Hasta is null)	
	
		and ("MtoDivReaAfecGCSinDer_C9" >= p_MtoDivReaAfecGCSinDer_C9_Desde or p_MtoDivReaAfecGCSinDer_C9_Desde is null)
		and ("MtoDivReaAfecGCSinDer_C9" <= p_MtoDivReaAfecGCSinDer_C9_Hasta or p_MtoDivReaAfecGCSinDer_C9_Hasta is null)	
	
		and ("MtoDivReaREXNoConstRenta_C17" >= p_MtoDivReaREXNoConstRenta_C17_Desde or p_MtoDivReaREXNoConstRenta_C17_Desde is null)
		and ("MtoDivReaREXNoConstRenta_C17" <= p_MtoDivReaREXNoConstRenta_C17_Hasta or p_MtoDivReaREXNoConstRenta_C17_Hasta is null)	
	
		and ("MtoDivReaREXRentExIGCAfecIAart11_C15" >= p_MtoDivReaREXRentExIGCAfecIAart11_C15_Desde or p_MtoDivReaREXRentExIGCAfecIAart11_C15_Desde is null)
		and ("MtoDivReaREXRentExIGCAfecIAart11_C15" <= p_MtoDivReaREXRentExIGCAfecIAart11_C15_Hasta or p_MtoDivReaREXRentExIGCAfecIAart11_C15_Hasta is null)	
	
		and ("MtoDivReaREXTribCumpExcDist_C12" >= p_MtoDivReaREXTribCumpExcDist_C12_Desde or p_MtoDivReaREXTribCumpExcDist_C12_Desde is null)
		and ("MtoDivReaREXTribCumpExcDist_C12" <= p_MtoDivReaREXTribCumpExcDist_C12_Hasta or p_MtoDivReaREXTribCumpExcDist_C12_Hasta is null)	
	
		and ("MtoDivReaREXTribCumpOtrasRentas_C11" >= p_MtoDivReaREXTribCumpOtrasRentas_C11_Desde or p_MtoDivReaREXTribCumpOtrasRentas_C11_Desde is null)
		and ("MtoDivReaREXTribCumpOtrasRentas_C11" <= p_MtoDivReaREXTribCumpOtrasRentas_C11_Hasta or p_MtoDivReaREXTribCumpOtrasRentas_C11_Hasta is null)	
	
		and ("MtoDivReaREXTribCumpRentasISFUT_C14" >= p_MtoDivReaREXTribCumpRentasISFUT_C14_Desde or p_MtoDivReaREXTribCumpRentasISFUT_C14_Desde is null)
		and ("MtoDivReaREXTribCumpRentasISFUT_C14" <= p_MtoDivReaREXTribCumpRentasISFUT_C14_Hasta or p_MtoDivReaREXTribCumpRentasISFUT_C14_Hasta is null)	
	
		and ("MtoDivReaREXTribCumpRentasRap_C10" >= p_MtoDivReaREXTribCumpRentasRap_C10_Desde or p_MtoDivReaREXTribCumpRentasRap_C10_Desde is null)
		and ("MtoDivReaREXTribCumpRentasRap_C10" <= p_MtoDivReaREXTribCumpRentasRap_C10_Hasta or p_MtoDivReaREXTribCumpRentasRap_C10_Hasta is null)	
		
		and ("MtoDivReaREXTribCumpUtiISFUT_C13" >= p_MtoDivReaREXTribCumpUtiISFUT_C13_Desde or p_MtoDivReaREXTribCumpUtiISFUT_C13_Desde is null)
		and ("MtoDivReaREXTribCumpUtiISFUT_C13" <= p_MtoDivReaREXTribCumpUtiISFUT_C13_Hasta or p_MtoDivReaREXTribCumpUtiISFUT_C13_Hasta is null)	
	
		and ("NumCertificado_C34" >= p_NumCertificado_C34_Desde or p_NumCertificado_C34_Desde is null)
		and ("NumCertificado_C34" <= p_NumCertificado_C34_Hasta or p_NumCertificado_C34_Hasta is null)	
		
		and ("SecEvento" >= p_SecEvento_Desde or p_SecEvento_Desde is null)
		and ("SecEvento" <= p_SecEvento_Hasta or p_SecEvento_Hasta is null);	
		
     	
	
    end if;
   
  
   
   
end;