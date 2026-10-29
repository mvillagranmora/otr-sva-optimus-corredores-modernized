SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraFM";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraFM"
	(  
	 	IN  p_IdEmpresa		   			BIGINT,			-- ID Empresa destino
     	IN  p_IdUsuario		   			BIGINT,			-- ID del usuario que solicita el servicio
	 	IN  p_IdCarga 	       			BIGINT,          -- ID de la carga 
	 	IN  p_IdModulo         			NVARCHAR(3),     -- modulo 
     	IN  P_Modalidad        			NVARCHAR(1),     -- Ingreso I incremental, 
     	IN  p_RutCliente       			NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 	IN  p_FechaInicial              NVARCHAR(10),   --YYYY-MM-DD
		IN  p_FechaFinal				NVARCHAR(10),   --YYYY-MM-DD
		-- Parametros salida del servicio publico
		-- Salidas para mensajes de advertencias y errores
		OUT p_out_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
		OUT p_out_ErroresContador int,
		OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000))
    ) 
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS   
	BEGIN
		-- VARIABLES VALIDACIONES BASICAS
		DECLARE v_out_Resultado  		CHAR(1); 	   --RESULTADO S/N/E
	    DECLARE v_out_Mensaje	 		NVARCHAR(5000); --MENSAJE SALIDA
	    
	    DECLARE v_erroroutmsg  			nvarchar(5000);    

	    
	    -- Para LOg
	    DECLARE v_out_refID				BIGINT;    
	    DECLARE v_out_ErroresContador	BIGINT; 
	    DECLARE v_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000));	
	    DECLARE v_FechaSistema NVARCHAR(40);
	    DECLARE v_MaxPeriodo NVARCHAR(40);
	    DECLARE v_FechaInicioPeriodo NVARCHAR(10);
	    DECLARE v_periodo  DATE := CONCAT(SUBSTRING(p_FechaInicial,1,7),'-01');
        DECLARE v_periodofinal DATE := CONCAT(SUBSTRING(p_FechaFinal,1,7),'-01'); 
	    DECLARE p_RutCliente NVARCHAR(11):='0-0';
	    DECLARE v_MonedaSistema NVARCHAR(10);
	    DECLARE Total_Registros_Leidos DECIMAL(26,8) :=0;
	    DECLARE Total_Registros_Cargados DECIMAL(26,8) :=0;
	    
	    
	        
		--------- CONTROL DE ERRORES
		--- Controlado
		DECLARE SALIDA_CONTROLADA CONDITION;    	
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
		BEGIN
			IF :p_out_ErroresContador = 0 THEN	 
		 	    p_out_ErroresDetalle       =  select 'INGRESO OK' as "Codigo",'LOG REGISTRADO' as "Descripcion" from dummy;-- UNION ALL
			END IF;		 					 	
		END;	
	
		--- Excepcion
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			declare v_errorExt nvarchar(40);
			IF ::SQL_ERROR_CODE > 10000 THEN
				call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
				p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
									   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
				select count(1) into p_out_ErroresContador 
				from :p_out_ErroresDetalle;										
			ELSE
				--Error interno no manejado  (El log será captado por la capa de XS control)
				p_out_ErroresDetalle       =  select 'ERROR' as "Codigo", ::SQL_ERROR_CODE as "Descripcion" from dummy;-- UNION ALL		
				RESIGNAL; 
			END IF;			
		END;
		p_out_ErroresContador		:= 0;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;   	
        p_out_ResumenDatosProcesados    = select '' as "CodigoProceso", '' as "Modulo", 0 as "TotalRegistrosSebra",0 as "TotalRegistrosSap" from dummy;
        
				
				SELECT LPAD(p_RutCliente, 11,' ') into p_RutCliente FROM DUMMY;
				
				SELECT COALESCE("Valor",'') into v_MonedaSistema from "BSCL.Plataforma.BD::PLA.Configuracion"
        		WHERE  "IdEmpresa" = p_IdEmpresa
        		AND  "CodigoParametro" = 'PLA.MonedaSistema';
        		
        		--------------------
		        -- insertamos FM
		        --------------------
				
				IF P_Modalidad = 'R' THEN 
					delete from "BSCL.Homologacion.BD::HOM.Cartera"
					where "IdCarga" = p_IdCarga
					   and "IdClasificacionTipoInstrumento" = 'FM';
					
					delete from "BSCL.Custodia.BD::CUS.Cartera"
					where "IdCarga" = p_IdCarga
					   and "IdClasificacionTipoInstrumento" = 'FM';
				END IF;

				delete  from "BSCL.Homologacion.BD::HOM.Error"
				where "IdCarga" = p_IdCarga 
				  and "Entidad" ='CarteraFFMM';
				
				INSERT INTO "BSCL.Homologacion.BD::HOM.Cartera"(
				"IdCartera" ,"FolioCartera" ,"FolioCarteraRelacionada" ,"FolioGarantiaOrigen" ,"FechaIngresoRegistro" ,"FechaModificacionRegistro" ,"IdEmpresa" 
				,"FechaOperacion" ,"FechaVencimientoOperacion" ,"PlazoEnDiasOperacion" ,"IdCliente" ,"IdPortafolio" ,"IdClienteRelacionado" ,"IdPortafolioRelacionado" 
				,"IdEstado" ,"IdTipoProducto" ,"IdTipoCanalIngreso" ,"IdMonedaSistema" ,"IdMovimientoCarteraEnTransito" ,"IdConcepto" ,"IdTipoOperacion" ,"IdOperacion" 
				,"IdTipoComprobante" ,"FolioComprobante" ,"FechaComprobante" ,"FolioExternoReferencia" ,"IdMesa" ,"IdBolsa" ,"IdSucursal" ,"IdAgente" ,"IdCondicionLiquidacion" 
				,"FechaLiquidacion" ,"EsIntraday" ,"UsaGarantiaEspecifica" ,"CobraPrimaAlInicio" ,"IdTipoPrestamo" ,"Glosa" ,"IdUsuario" ,"IdModulo" ,"TipoCarteraNormativa" 
				,"FechaCurseInversion" ,"TipoFactorAjuste" ,"ValorFactorAjusteaFechaCurse" ,"CostoInversionEnFactorAjuste" ,"IdClasificacionTipoInstrumento" ,"IdTipoInstrumento" 
				,"IdInstrumento" ,"Nemotecnico" ,"IdEmisor" ,"NemoSVS" ,"IdTipoReajuste" ,"FechaEmisionInstrumento" ,"FechaVencimientoInstrumento" ,"EsFungible" 
				,"ValorizaPor" ,"TipoValorizacion" ,"CodigoIFRS" ,"IdBoveda" ,"CuentaBoveda" ,"IdMonedaInstrumento" ,"Tamano" ,"TIFAfectoBeneficio" ,"TIFTasa" 
				,"Cantidad" ,"CorteMinimo" ,"ValorRescate" ,"PrecioCompra" ,"TasaCompra" ,"MontoCompra" ,"MontoCompraMonedaInstrumento" ,"TasaAgente" 
				,"PrecioAPlazo" ,"MontoAPlazo" ,"MontoAPlazoMonedaInstrumento" ,"FechaEfectiva" ,"IdPlaza" ,"IdFixing" ,"FechaFixing" ,"HoraFixing" ,"IdModalidadCumplimiento" 
				,"IdReferenciaMercado" ,"IdMonedaPrincipal" ,"IdMonedaSecundaria" ,"ValorTipoCambioCierre" ,"ValorParidadCierre" ,"ValorTipoCambioCosto" 
				,"ValorParidadCosto" ,"MontoMonedaPrincipal" ,"MontoMonedaSecundaria" ,"IdCarga" ,"IdSecuenciaCarteraMigracion" ,"CodigoPeriodo" ,"INGGlosa" 
				,"INGTasaAgente" ,"INGFechaOperacion" ,"INGIdCondicionLiquidacion" ,"INGFechaLiquidacion" ,"INGCantidad" ,"INGValorRescate" 
				,"INGPrecioAPlazo" ,"INGMontoAPlazo" ,"INGMontoAPlazoMonedaInstrumento" ,"INGEsPuntos" ,"INGValorTipoCambioSpot" 
				,"INGValorTipoCambioCotizador" ,"INGTasaPuntosMonedaPrincipal" ,"INGTasaPuntosMonedaSecundaria" ,"INGFechaValorizacionCartera" ,"INGFechaValorizacionAnterior" 
				,"INGFechaValorizacionActual" ,"INGFechaValorizacionProximo" ,"INGPrecioCompra" ,"INGTasaCompra" ,"INGMontoATasaCompraAnterior" ,"INGMontoInteresCompraAnterior" 
				,"INGMontoReajusteCompraAnterior" ,"INGMontoCompraAnteriorMonedaInstrumento" ,"INGMontoInteresCompraAnteriorMonedaInstrumento" ,"INGMontoReajusteCompraAnteriorMonedaInstrumento" 
				,"INGDurationCompraAnterior" ,"INGConvexidadCompraAnterior" ,"INGMontoCompraAnteriorPibMas" ,"INGMontoCompraAnteriorPibMasMonedaInstrumento" 
				,"INGMontoCompraAnteriorPibMenos" ,"INGMontoCompraAnteriorPibMenosMonedaInstrumento" ,"INGMontoATasaCompraActual" ,"INGMontoCompraActualMonedaInstrumento" 
				,"INGDurationCompraActual" ,"INGConvexidadCompraActual" ,"INGMontoCompraActualPibMas" ,"INGMontoCompraActualPibMasMonedaInstrumento" ,"INGMontoCompraActualPibMenos" 
				,"INGMontoCompraActualPibMenosMonedaInstrumento" ,"INGMontoATasaCompraProximo" ,"INGMontoInteresCompraProximo" ,"INGMontoReajusteCompraProximo" 
				,"INGMontoCompraProximoMonedaInstrumento" ,"INGMontoInteresCompraProximoMonedaInstrumento" ,"INGMontoReajusteCompraProximoMonedaInstrumento" 
				,"INGDurationCompraProximo" ,"INGConvexidadCompraProximo" ,"INGFechaPrecioTasaMercadoAnterior" ,"INGFuenteTasaPrecioMercadoAnterior"  
				,"INGPrecioMercadoAnterior" ,"INGTasaMercadoAnterior" ,"INGMontoATasaMercadoAnterior" ,"INGMontoInteresMercadoAnterior" ,"INGMontoReajusteMercadoAnterior" 
				,"INGMontoMercadoAnteriorMonedaInstrumento" ,"INGMontoInteresMercadoAnteriorMonedaInstrumento" ,"INGMontoReajusteMercadoAnteriorMonedaInstrumento" 
				,"INGDurationMercadoAnterior" ,"INGConvexidadMercadoAnterior" ,"INGMontoMercadoAnteriorPibMas" ,"INGMontoMercadoAnteriorPibMasMonedaInstrumento" 
				,"INGMontoMercadoAnteriorPibMenos" ,"INGMontoMercadoAnteriorPibMenosMonedaInstrumento" ,"INGFechaPrecioTasaMercadoActual" ,"INGFuenteTasaPrecioMercadoActual" 
				,"INGPrecioMercadoActual" ,"INGTasaMercadoActual" ,"INGMontoATasaMercadoActual" ,"INGMontoMercadoActualMonedaInstrumento" 
				,"INGDurationMercadoActual" ,"INGConvexidadMercadoActual" ,"INGMontoMercadoActualPibMas" ,"INGMontoMercadoActualPibMasMonedaInstrumento" 
				,"INGMontoMercadoActualPibMenos" ,"INGMontoMercadoActualPibMenosMonedaInstrumento" ,"INGFechaPrecioTasaMercadoProximo" ,"INGFuenteTasaPrecioMercadoProximo" 
				,"INGPrecioMercadoProximo" ,"INGTasaMercadoProximo" ,"INGMontoATasaMercadoProximo" ,"INGMontoInteresMercadoProximo" 
				,"INGMontoReajusteMercadoProximo" ,"INGMontoMercadoProximoMonedaInstrumento" ,"INGMontoInteresMercadoProximoMonedaInstrumento" ,"INGMontoReajusteMercadoProximoMonedaInstrumento" 
				,"INGDurationMercadoProximo" ,"INGConvexidadMercadoProximo" ,"INGTasaCorredora" ,"INGFechaTasaCorredora" ,"INGMontoATasaCorredora" ,"INGMontoATasaCorredoraMonedaInstrumento" 
				,"INGFechaCorteCupon" ,"INGNumeroCupon" ,"INGIdMonedaPagoCupon" ,"INGMontoCorteCupon" ,"INGFechaPagoCorteCupon" ,"INGMontoCapitalCorteCupon" 
				,"INGMontoInteresCorteCupon" ,"INGMontoReajusteCorteCupon" ,"INGMontoCorteCuponMonedaInstrumento" ,"INGMontoCapitalCorteCuponMonedaInstrumento" 
				,"INGMontoInteresCorteCuponMonedaInstrumento" ,"INGMontoReajusteCorteCuponMonedaInstrumento" ,"INGMontoPresenteTIF" ,"INGMontoInteresTIF" 
				,"INGMontoReajusteTIF" ,"INGMontoInteresAcumTIF" ,"INGMontoReajusteAcumTIF" ,"INGMontoPresenteTIFMonedaInstrumento" ,"INGMontoInteresTIFMonedaInstrumento" 
				,"INGMontoReajusteTIFMonedaInstrumento" ,"INGMontoInteresAcumTIFMonedaInstrumento" ,"INGMontoReajusteAcumTIFMonedaInstrumento" ,"INGMontoMonedaPrincipal" 
				,"INGMontoMonedaSecundaria" ,"INGMontoDiferido" ,"INGMontoUtilidadDiferida" ,"INGMontoPerdidaDiferida" ,"INGMontoMargen" ,"INGMontoValorRazonableActivo" 
				,"INGMontoValorRazonablePasivo" ,"INGMontoValorRazonableNeto" ,"INGTasaValorRazonableActivo" ,"INGTasaValorRazonablePasivo" ,"INGMontoDevengoTipoCambio" 
				,"INGMontoDevengoTipoCambioAnterior" ,"INGMontoDiferidoDiario" ,"INGMontoDiferidoAcumulado" ,"INGMontoMatriz" ,"EGRGlosa" ,"EGRFechaOperacion" 
				,"EGRIdTipoOperacion" ,"EGRIdOperacion" ,"EGRIdMovimientoCarteraEnTransito" ,"EGRIdTipoCanalIngreso" ,"EGRIdCondicionLiquidacion" ,"EGRFechaLiquidacion" 
				,"EGRIdTipoComprobante" ,"EGRFolioComprobante" ,"EGRFechaComprobante" ,"EGRFolioExternoReferencia" ,"EGRIdModulo" ,"EGRIdConcepto" ,"EGRIdUsuario" 
				,"EGRIdSucursal" ,"EGRIdAgente" ,"EGRFechaCurseRetiro" ,"EGRCostoRetiroenFactorAjuste" ,"EGRValorFactorAjusteaFechaRetiro" ,"EGRCantidad" ,"EGRValorRescate" 
				,"EGRPrecio" ,"EGRTasa" ,"EGRMonto" ,"EGRMontoMonedaInstrumento" ,"EGRSpread" ,"EGRPrecioAPlazo" ,"EGRMontoAPlazo" ,"EGRMontoAPlazoMonedaInstrumento" 
				,"EGRMontoPresenteTIF" ,"EGRMontoInteresTIF" ,"EGRMontoReajusteTIF" ,"EGRMontoInteresAcumTIF" ,"EGRMontoReajusteAcumTIF" ,"EGRMontoPresenteTIFMonedaInstrumento" 
				,"EGRMontoInteresTIFMonedaInstrumento" ,"EGRMontoReajusteTIFMonedaInstrumento" ,"EGRMontoInteresAcumTIFMonedaInstrumento" ,"EGRMontoReajusteAcumTIFMonedaInstrumento" 
				,"EGRIdSpotAsociado" ,"EGRMontoMonedaPrincipal" ,"EGRMontoMonedaSecundaria" ,"EGRIdMonedaLiquidacion" ,"EGRMontoLiquidacion" ,"EGRMontoLiquidacionMonedaPrincipal" 
				,"EGRValorTipoCambioSpot" ,"EGRValorParidad" ,"EGRValorTipoCambioCotizador" ,"EGREsPuntos" ,"EGRTasaPuntosMonedaPrincipal" ,"EGRTasaPuntosMonedaSecundaria"
				,"RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER" ,"COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN","SEC_ORDEN","TIPO_CARTERA","ID_ERROR","SEC_MOVTO","CAN_GARANTIA"
			    )  
			    select 
			      "BSCL.Custodia.BD.Secuencias::Cartera".NEXTVAL as "IdCartera" 
				, "BSCL.Custodia.BD.Secuencias::FolioCartera".NEXTVAL  as "FolioCartera" 
				, 0 as "FolioCarteraRelacionada" 
				, 0 as "FolioGarantiaOrigen"-- nuevo 
				, TBCTCIFM."FEC_INVER" as "FechaIngresoRegistro" 
				, '0001-01-01' as "FechaModificacionRegistro" 
				, p_IdEmpresa as "IdEmpresa" 
				, TBCTCIFM."FEC_INVER" as "FechaOperacion" 
				, '0001-01-01' as "FechaVencimientoOperacion" 
				, 0 as "PlazoEnDiasOperacion" 
				, CLIENTE."IdCliente" as "IdCliente" 
				, PORTAFOLIO."IdPortafolio" as "IdPortafolio" 
				, 0 as "IdClienteRelacionado" 
				, 0 as "IdPortafolioRelacionado" 
				, CASE WHEN TBCTCIFM."COD_PER" = SUBSTRING(v_periodofinal,1,7) THEN 'LIQ' ELSE 'VIG' END as "IdEstado" 
				, 'INFM' as "IdTipoProducto" 
				, '' as "IdTipoCanalIngreso" 
				, v_MonedaSistema as "IdMonedaSistema" 
				, 0 as "IdMovimientoCarteraEnTransito" 
				, 'APONOR' as "IdConcepto" -- puede ser por aporte o transferencia no existe certeza que podamos cruzar desde cartera a los movimientos
				, 'APORFM' as "IdTipoOperacion" 
				, 0 as "IdOperacion" -- no podremos cruzar con la tabla de movimientos 
				, '' as "IdTipoComprobante" -- revisar facturacion fondos mutuos
				, 0 as "FolioComprobante" -- revisar
				, '0001-01-01' as "FechaComprobante" --  revisar
				, 0 as "FolioExternoReferencia" 
				, 0 as "IdMesa" 
				, 'BCS' as "IdBolsa" --consultar si llegan operaciones otras bolsas de FFMM
				, 0 as "IdSucursal" 
				, 0 as "IdAgente" 
				, 'T0' as "IdCondicionLiquidacion" 
				, TBCTCIFM."FEC_INVER"  as "FechaLiquidacion" 
				, '' as "EsIntraday" 
				, '' as "UsaGarantiaEspecifica" 
				, '' as "CobraPrimaAlInicio" 
				, '' as "IdTipoPrestamo" 
				, '' as "Glosa" 
				, 0  as "IdUsuario" -- revisar ya que se han cargado solo agentes y empleados no usuarios
				, 'FMM' as "IdModulo" 
				, ''  as "TipoCarteraNormativa" 
				, '0001-01-01' as "FechaCurseInversion"-- no se que es esto podria ser la fecha emision
				, 0 as "TipoFactorAjuste" -- no se que es esto
				, 0 as "ValorFactorAjusteaFechaCurse" -- no se de donde sacarlo
				, 0 as "CostoInversionEnFactorAjuste" -- no se de donde sacarlo
				, 'FM' as "IdClasificacionTipoInstrumento" --
				, TIPO_INSTRUMENTO."IdTipoInstrumento" as "IdTipoInstrumento" 
				, INSTRUMENTO."IdInstrumento" as "IdInstrumento" 
				, INSTRUMENTO."Nemotecnico" as "Nemotecnico" 
				, INSTRUMENTO."IdEmisor" as "IdEmisor" 
				, '' as "NemoSVS" 
				, '' as "IdTipoReajuste" 
				, '0001-01-01' as "FechaEmisionInstrumento"
				, '0001-01-01' as "FechaVencimientoInstrumento" 
				, 'N' as "EsFungible" 
				, 'P' as "ValorizaPor" -- T TASA P PRECIO que es esto
				, ''  as "TipoValorizacion" -- que es esto 
				, 0 as "CodigoIFRS" 
				, TBCTCIFM."COD_BOVEDA" as "IdBoveda" 
				, '' as "CuentaBoveda" 
				, INSTRUMENTO."IdMoneda" as "IdMonedaInstrumento" 
				, 0 as "Tamano" 
				, '' as "TIFAfectoBeneficio" 
				, 0 as "TIFTasa" 
				, TBCTCIFM."CAN_ACTUAL" as "Cantidad" 
				, 0 as "CorteMinimo" 
				, 0 as "ValorRescate"-- este campo es para los operaciones de pactos 
				, TBCTCIFM."PRECIO_CIERRE" as "PrecioCompra" 
				, 0 as "TasaCompra" 
				, TBCTCIFM."MONTO_ACT_AJUS" as "MontoCompra" 
				, TBCTCIFM."MONTO_ACT_AJUS" as "MontoCompraMonedaInstrumento" 
				, 0 as "TasaAgente" 
				, 0 as "PrecioAPlazo" 
				, 0 as "MontoAPlazo" 
				, 0 as "MontoAPlazoMonedaInstrumento" 
				, '0001-01-01' as "FechaEfectiva" 
				, 0 as "IdPlaza" 
				, 0 as "IdFixing" 
				, '0001-01-01' as "FechaFixing" 
				, '0:00:00' as "HoraFixing" 
				, 0 as "IdModalidadCumplimiento" 
				, 0 as "IdReferenciaMercado" 
				, '' as "IdMonedaPrincipal" 
				, '' as "IdMonedaSecundaria" 
				, 0 as "ValorTipoCambioCierre" 
				, 0 as "ValorParidadCierre" 
				, 0 as "ValorTipoCambioCosto" 
				, 0 as "ValorParidadCosto" 
				, 0 as "MontoMonedaPrincipal" 
				, 0 as "MontoMonedaSecundaria" 
				, p_IdCarga as "IdCarga" 
				, TBCTCIFM."SEC_CAR_INVER" as "IdSecuenciaCarteraMigracion" 
				, TBCTCIFM."COD_PER" as "CodigoPeriodo" 
				, 'MOVIMIENTO MIGRACION' as "INGGlosa" 
				, 0 as "INGTasaAgente" 
				,LAST_DAY(TO_DATE(CONCAT(TBCTCIFM."COD_PER",'-01'),'YYYY-MM-DD')) as "INGFechaOperacion" 
				, 'T0' as "INGIdCondicionLiquidacion" 
				, '0001-01-01' as "INGFechaLiquidacion" 
				, TBCTCIFM."CAN_ACTUAL" as "INGCantidad" 
				, 0 as "INGValorRescate" 
				--, 0 as "INGPrecioPromedioTasaCompra" 
				, 0 as "INGPrecioAPlazo" 
				, 0 as "INGMontoAPlazo" 
				, 0 as "INGMontoAPlazoMonedaInstrumento" 
				, '' as "INGEsPuntos" -- forward
				, 0 as "INGValorTipoCambioSpot" 
				, 0 as "INGValorTipoCambioCotizador" 
				, 0 as "INGTasaPuntosMonedaPrincipal" 
				, 0 as "INGTasaPuntosMonedaSecundaria" 
				, '0001-01-01' as "INGFechaValorizacionCartera" 
				, '0001-01-01' as "INGFechaValorizacionAnterior" 
				, '0001-01-01' as "INGFechaValorizacionActual" 
				, '' as "INGFechaValorizacionProximo" 
				, TBCTCIFM."PRECIO_CIERRE"  as "INGPrecioCompra" 
				, 0 as "INGTasaCompra" 
				, 0 as "INGMontoATasaCompraAnterior" 
				, 0 as "INGMontoInteresCompraAnterior" 
				, 0 as "INGMontoReajusteCompraAnterior" 
				, 0 as "INGMontoCompraAnteriorMonedaInstrumento" 
				, 0 as "INGMontoInteresCompraAnteriorMonedaInstrumento" 
				, 0 as "INGMontoReajusteCompraAnteriorMonedaInstrumento" 
				, 0 as "INGDurationCompraAnterior" 
				, 0 as "INGConvexidadCompraAnterior" 
				, 0 as "INGMontoCompraAnteriorPibMas" 
				, 0 as "INGMontoCompraAnteriorPibMasMonedaInstrumento" 
				, 0 as "INGMontoCompraAnteriorPibMenos" 
				, 0 as "INGMontoCompraAnteriorPibMenosMonedaInstrumento" 
				, 0 as "INGMontoATasaCompraActual" 
				, 0 as "INGMontoCompraActualMonedaInstrumento" 
				, 0 as "INGDurationCompraActual" 
				, 0 as "INGConvexidadCompraActual" 
				, 0 as "INGMontoCompraActualPibMas" 
				, 0 as "INGMontoCompraActualPibMasMonedaInstrumento" 
				, 0 as "INGMontoCompraActualPibMenos" 
				, 0 as "INGMontoCompraActualPibMenosMonedaInstrumento" 
				, 0 as "INGMontoATasaCompraProximo" 
				, 0 as "INGMontoInteresCompraProximo" 
				, 0 as "INGMontoReajusteCompraProximo" 
				, 0 as "INGMontoCompraProximoMonedaInstrumento" 
				, 0 as "INGMontoInteresCompraProximoMonedaInstrumento" 
				, 0 as "INGMontoReajusteCompraProximoMonedaInstrumento" 
				, 0 as "INGDurationCompraProximo" 
				, 0 as "INGConvexidadCompraProximo" 
				, '0001-01-01' as "INGFechaPrecioTasaMercadoAnterior" 
				, '' as "INGFuenteTasaPrecioMercadoAnterior" 
				--, '' as "INGTablaFuenteTasaPrecioMercadoAnterior" 
				, 0 as "INGPrecioMercadoAnterior" 
				, 0 as "INGTasaMercadoAnterior" 
				, 0 as "INGMontoATasaMercadoAnterior" 
				, 0 as "INGMontoInteresMercadoAnterior" 
				, 0 as "INGMontoReajusteMercadoAnterior" 
				, 0 as "INGMontoMercadoAnteriorMonedaInstrumento" 
				, 0 as "INGMontoInteresMercadoAnteriorMonedaInstrumento" 
				, 0 as "INGMontoReajusteMercadoAnteriorMonedaInstrumento" 
				, 0 as "INGDurationMercadoAnterior" 
				, 0 as "INGConvexidadMercadoAnterior" 
				, 0 as "INGMontoMercadoAnteriorPibMas" 
				, 0 as "INGMontoMercadoAnteriorPibMasMonedaInstrumento" 
				, 0 as "INGMontoMercadoAnteriorPibMenos" 
				, 0 as "INGMontoMercadoAnteriorPibMenosMonedaInstrumento" 
				, '0001-01-01' as "INGFechaPrecioTasaMercadoActual" 
				, '' as "INGFuenteTasaPrecioMercadoActual" 
				--, '' as "INGTablaFuenteTasaPrecioMercadoActual" 
				, 0 as "INGPrecioMercadoActual" 
				, 0 as "INGTasaMercadoActual" 
				, 0 as "INGMontoATasaMercadoActual" 
				, 0 as "INGMontoMercadoActualMonedaInstrumento" 
				, 0 as "INGDurationMercadoActual" 
				, 0 as "INGConvexidadMercadoActual" 
				, 0 as "INGMontoMercadoActualPibMas" 
				, 0 as "INGMontoMercadoActualPibMasMonedaInstrumento" 
				, 0 as "INGMontoMercadoActualPibMenos" 
				, 0 as "INGMontoMercadoActualPibMenosMonedaInstrumento" 
				, '0001-01-01' as "INGFechaPrecioTasaMercadoProximo" 
				, '' as "INGFuenteTasaPrecioMercadoProximo" 
				--, '' as "INGTablaFuenteTasaPrecioMercadoProximo" 
				, 0  as "INGPrecioMercadoProximo" 
				, 0  as "INGTasaMercadoProximo" 
				, 0  as "INGMontoATasaMercadoProximo" 
				, 0  as "INGMontoInteresMercadoProximo" 
				, 0  as "INGMontoReajusteMercadoProximo" 
				, 0  as "INGMontoMercadoProximoMonedaInstrumento" 
				, 0  as "INGMontoInteresMercadoProximoMonedaInstrumento" 
				, 0  as "INGMontoReajusteMercadoProximoMonedaInstrumento" 
				, 0  as "INGDurationMercadoProximo" 
				, 0  as "INGConvexidadMercadoProximo" 
				, 0  as "INGTasaCorredora" 
				, '0001-01-01'  as "INGFechaTasaCorredora" 
				, 0  as "INGMontoATasaCorredora" 
				, 0  as "INGMontoATasaCorredoraMonedaInstrumento" 
				, '0001-01-01'  as "INGFechaCorteCupon" 
				, 0  as "INGNumeroCupon" 
				, ''  as "INGIdMonedaPagoCupon" 
				, 0 as "INGMontoCorteCupon" 
				, '0001-01-01' as "INGFechaPagoCorteCupon" 
				, 0 as "INGMontoCapitalCorteCupon" 
				, 0 as "INGMontoInteresCorteCupon" 
				, 0 as "INGMontoReajusteCorteCupon" 
				, 0 as "INGMontoCorteCuponMonedaInstrumento" 
				, 0 as "INGMontoCapitalCorteCuponMonedaInstrumento" 
				, 0 as "INGMontoInteresCorteCuponMonedaInstrumento" 
				, 0 as "INGMontoReajusteCorteCuponMonedaInstrumento" 
				, 0 as "INGMontoPresenteTIF" 
				, 0 as "INGMontoInteresTIF" 
				, 0 as "INGMontoReajusteTIF" 
				, 0 as "INGMontoInteresAcumTIF" 
				, 0 as "INGMontoReajusteAcumTIF" 
				, 0 as "INGMontoPresenteTIFMonedaInstrumento" 
				, 0 as "INGMontoInteresTIFMonedaInstrumento" 
				, 0 as "INGMontoReajusteTIFMonedaInstrumento" 
				, 0 as "INGMontoInteresAcumTIFMonedaInstrumento" 
				, 0 as "INGMontoReajusteAcumTIFMonedaInstrumento" 
				, 0 as "INGMontoMonedaPrincipal" 
				, 0 as "INGMontoMonedaSecundaria" 
				, 0 as "INGMontoDiferido" 
				, 0 as "INGMontoUtilidadDiferida" 
				, 0 as "INGMontoPerdidaDiferida" 
				, 0 as "INGMontoMargen" 
				, 0 as "INGMontoValorRazonableActivo" 
				, 0 as "INGMontoValorRazonablePasivo" 
				, 0 as "INGMontoValorRazonableNeto" 
				, 0 as "INGTasaValorRazonableActivo" 
				, 0 as "INGTasaValorRazonablePasivo" 
				, 0 as "INGMontoDevengoTipoCambio" 
				, 0 as "INGMontoDevengoTipoCambioAnterior" 
				, 0 as "INGMontoDiferidoDiario" 
				, 0 as "INGMontoDiferidoAcumulado" 
				, 0 as "INGMontoMatriz" 
				, '' as "EGRGlosa" 
				, CASE WHEN TBCTCIFM."COD_PER" = SUBSTRING(v_periodofinal,1,7) 
				    THEN '0001-01-01'  
				    	ELSE LAST_DAY(ADD_MONTHS(TO_DATE(CONCAT(TBCTCIFM."COD_PER",'-01'),'YYYY-MM-DD'),1)) 
				    END as "EGRFechaOperacion" 
				, '' as "EGRIdTipoOperacion" 
				,  0 as "EGRIdOperacion" 
				,  0 as "EGRIdMovimientoCarteraEnTransito" 
				, '' as "EGRIdTipoCanalIngreso" 
				, 'T0' as "EGRIdCondicionLiquidacion" 
				, '0001-01-01' as "EGRFechaLiquidacion" 
				, '' as "EGRIdTipoComprobante" 
				, 0 as "EGRFolioComprobante" 
				, '0001-01-01' as "EGRFechaComprobante" 
				, 0 as "EGRFolioExternoReferencia"      
				, CASE WHEN TBCTCIFM."COD_PER" = SUBSTRING(v_periodofinal,1,7) THEN  '' 
				     ELSE 'FMM' 
				     END  as "EGRIdModulo" 
				, '' as "EGRIdConcepto" 
				, 0 as "EGRIdUsuario" 
				, 0 --SUCURSAL."IdSucursal" as "EGRIdSucursal" 
				, 0 --EMPLEADO."IdEmpleado" as "EGRIdAgente" 
				, '0001-01-01' as "EGRFechaCurseRetiro" 
				, 0 as "EGRCostoRetiroenFactorAjuste" 
				, 0 as "EGRValorFactorAjusteaFechaRetiro" 
				,  CASE WHEN TBCTCIFM."COD_PER" = SUBSTRING(v_periodofinal,1,7) THEN  0 
				     ELSE TBCTCIFM."CAN_ACTUAL" 
				   END as "EGRCantidad" 
				, 0 as "EGRValorRescate" 
				,CASE WHEN TBCTCIFM."COD_PER" = SUBSTRING(v_periodofinal,1,7) THEN  0 
				     ELSE TBCTCIFM."PRECIO_CIERRE"
				   END as "EGRPrecio" 
				, 0 as "EGRTasa"
				,CASE WHEN TBCTCIFM."COD_PER" = SUBSTRING(v_periodofinal,1,7) THEN  0 
				     ELSE TBCTCIFM."MONTO_ACT_AJUS"
				 END as "EGRMonto"
				,CASE WHEN TBCTCIFM."COD_PER" = SUBSTRING(v_periodofinal,1,7) THEN  0 
				     ELSE TBCTCIFM."MONTO_ACT_AJUS"
				  END as "EGRMontoMonedaInstrumento" 
				, 0 as "EGRSpread" 
				, 0 as "EGRPrecioAPlazo" 
				, 0 as "EGRMontoAPlazo" 
				, 0 as "EGRMontoAPlazoMonedaInstrumento" 
				, 0 as "EGRMontoPresenteTIF" 
				, 0 as "EGRMontoInteresTIF" 
				, 0 as "EGRMontoReajusteTIF" 
				, 0 as "EGRMontoInteresAcumTIF" 
				, 0 as "EGRMontoReajusteAcumTIF" 
				, 0 as "EGRMontoPresenteTIFMonedaInstrumento" 
				, 0 as "EGRMontoInteresTIFMonedaInstrumento" 
				, 0 as "EGRMontoReajusteTIFMonedaInstrumento" 
				, 0 as "EGRMontoInteresAcumTIFMonedaInstrumento" 
				, 0 as "EGRMontoReajusteAcumTIFMonedaInstrumento" 
				, 0 as "EGRIdSpotAsociado" 
				, 0 as "EGRMontoMonedaPrincipal" 
				, 0 as "EGRMontoMonedaSecundaria" 
				, 0 as "EGRIdMonedaLiquidacion" 
				, 0 as "EGRMontoLiquidacion" 
				, 0 as "EGRMontoLiquidacionMonedaPrincipal" 
				, 0 as "EGRValorTipoCambioSpot" 
				, 0 as "EGRValorParidad" 
				, 0 as "EGRValorTipoCambioCotizador" 
				, 0 as "EGREsPuntos" 
				, 0 as "EGRTasaPuntosMonedaPrincipal" 
				, 0 as "EGRTasaPuntosMonedaSecundaria"
				,TBCTCIFM."RUT_CLI"	as "RUT_CLI"
	 			,TBCTCIFM."SEC_RUT_CLI"	as "SEC_RUT_CLI"
	 			,TBCTCIFM."SEC_CAR_INVER" as "SEC_CAR_INVER"
	 			,TBCTCIFM."COD_PER"	as "COD_PER"
	 			,TBCTCIFM."FOLIO_COMP_ADJ" as "FOLIO_COMP_ADJ"
	 			,TBCTCIFM."TIPO_COMP_ADJ" as "TIPO_COMP_ADJ"
     			,TBCTCIFM."LIN_COMP_ADJ" as "LIN_COMP_ADJ"
	 			,TBCTCIFM."LIN_ORDEN" as "LIN_ORDEN"
	 			,TBCTCIFM."SEC_ORDEN" as "SEC_ORDEN"
	 			,TBCTCIFM."TIPO_CARTERA" as "TIPO_CARTERA"
	 			,0 as "ID_ERROR"
	 			,0 as "SEC_MOVTO"
	 			,TBCTCIFM."CAN_GARANTIA" as "CAN_GARANTIA"
				FROM "BSCL_VIRTUAL"."GES_TBCTCIFM" AS TBCTCIFM       
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
		        	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = TRIM(TBCTCIFM."RUT_CLI")
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
		        	ON PERSONA."IdPersona" = DOCUMENTO_IDENTIDAD."IdPersona"
		        INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
		       		ON CLIENTE."IdPersona" = PERSONA."IdPersona"
		        INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO
		        	ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
		       		AND PORTAFOLIO."CodigoPortafolio" = TBCTCIFM."SEC_RUT_CLI"||''
		        INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
		       		ON INSTRUMENTO."Nemotecnico" = TRIM(TBCTCIFM."NEMO")
		        INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPO_INSTRUMENTO
		       		ON TIPO_INSTRUMENTO."IdTipoInstrumento" = INSTRUMENTO."IdTipoInstrumento"
		        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS HOMOLOGACION_CARTERA
			        ON HOMOLOGACION_CARTERA."COD_PER" = TBCTCIFM."COD_PER"
			        AND HOMOLOGACION_CARTERA."RUT_CLI" = TBCTCIFM."RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_RUT_CLI" = TBCTCIFM."SEC_RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_CAR_INVER" = TBCTCIFM."SEC_CAR_INVER"
		        WHERE TBCTCIFM."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
		        AND TBCTCIFM."COD_PER" <=  SUBSTRING(v_periodofinal,1,7)
		        AND TBCTCIFM."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then TBCTCIFM."RUT_CLI" ELSE p_RutCliente END
		        AND HOMOLOGACION_CARTERA."RUT_CLI" IS NULL;	

			 -- el documento identidad
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CarteraFFMM' as "Entidad"
                    ,'el documento de identidad no existe EN OPTIMUS' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBCTCIFM."RUT_CLI" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , 'COD_PER' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'RUT_CLI' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'SEC_CAR_INVER' as "NombreCampoGenerico8"
                    , TBCTCIFM."SEC_CAR_INVER" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
             FROM "BSCL_VIRTUAL"."GES_TBCTCIFM" AS TBCTCIFM       
		        LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
		        	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = TRIM(TBCTCIFM."RUT_CLI")
		        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS HOMOLOGACION_CARTERA
			        ON HOMOLOGACION_CARTERA."COD_PER" = TBCTCIFM."COD_PER"
			        AND HOMOLOGACION_CARTERA."RUT_CLI" = TBCTCIFM."RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_RUT_CLI" = TBCTCIFM."SEC_RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_CAR_INVER" = TBCTCIFM."SEC_CAR_INVER"
		        WHERE TBCTCIFM."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
		        AND TBCTCIFM."COD_PER" <=  SUBSTRING(v_periodofinal,1,7)
		        AND TBCTCIFM."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then TBCTCIFM."RUT_CLI" ELSE p_RutCliente END
		        AND HOMOLOGACION_CARTERA."RUT_CLI" IS NULL
		        AND DOCUMENTO_IDENTIDAD."DocumentoIdentidad" IS NULL;	
		        

			 -- la persona debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CarteraFFMM' as "Entidad"
                    ,'la persona no existe en OPTIMUS' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBCTCIFM."RUT_CLI" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , 'COD_PER' as "NombreCampoGenerico5"
                    , TBCTCIFM."COD_PER" as "ValorCampoGenerico5"
                    ,'' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'SEC_CAR_INVER' as "NombreCampoGenerico8"
                    , TBCTCIFM."SEC_CAR_INVER" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
             FROM "BSCL_VIRTUAL"."GES_TBCTCIFM" AS TBCTCIFM       
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
		        	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = TRIM(TBCTCIFM."RUT_CLI")
		        LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
		        	ON PERSONA."IdPersona" = DOCUMENTO_IDENTIDAD."IdPersona"	
		        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS HOMOLOGACION_CARTERA
			        ON HOMOLOGACION_CARTERA."COD_PER" = TBCTCIFM."COD_PER"
			        AND HOMOLOGACION_CARTERA."RUT_CLI" = TBCTCIFM."RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_RUT_CLI" = TBCTCIFM."SEC_RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_CAR_INVER" = TBCTCIFM."SEC_CAR_INVER"
		        WHERE TBCTCIFM."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
		        AND TBCTCIFM."COD_PER" <=  SUBSTRING(v_periodofinal,1,7)
		        AND TBCTCIFM."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then TBCTCIFM."RUT_CLI" ELSE p_RutCliente END
		        AND HOMOLOGACION_CARTERA."RUT_CLI" IS NULL
		        AND PERSONA."IdPersona" IS NULL;
		        
		     -- el cliente debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CarteraFFMM' as "Entidad"
                    ,'la Cliente no existe en OPTIMUS' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBCTCIFM."RUT_CLI" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , 'COD_PER' as "NombreCampoGenerico5"
                    , TBCTCIFM."COD_PER" as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'IdPersona' as "NombreCampoGenerico7"
                    , PERSONA."IdPersona" as "ValorCampoGenerico7"
                    ,'SEC_CAR_INVER' as "NombreCampoGenerico8"
                    , TBCTCIFM."SEC_CAR_INVER" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
             FROM "BSCL_VIRTUAL"."GES_TBCTCIFM" AS TBCTCIFM       
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
		        	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = TRIM(TBCTCIFM."RUT_CLI")
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
		        	ON PERSONA."IdPersona" = DOCUMENTO_IDENTIDAD."IdPersona"	
		        LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
		       		ON CLIENTE."IdPersona" = PERSONA."IdPersona"
		        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS HOMOLOGACION_CARTERA
			        ON HOMOLOGACION_CARTERA."COD_PER" = TBCTCIFM."COD_PER"
			        AND HOMOLOGACION_CARTERA."RUT_CLI" = TBCTCIFM."RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_RUT_CLI" = TBCTCIFM."SEC_RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_CAR_INVER" = TBCTCIFM."SEC_CAR_INVER"
		        WHERE TBCTCIFM."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
		        AND TBCTCIFM."COD_PER" <=  SUBSTRING(v_periodofinal,1,7)
		        AND TBCTCIFM."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then TBCTCIFM."RUT_CLI" ELSE p_RutCliente END
		        AND HOMOLOGACION_CARTERA."RUT_CLI" IS NULL
		        AND CLIENTE."IdPersona" IS NULL; 			        
		        
		        -- el cliente debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CarteraFFMM' as "Entidad"
                    ,'la no existe cliente asociado a la persona OPTIMUS' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBCTCIFM."RUT_CLI" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , 'COD_PER' as "NombreCampoGenerico5"
                    , TBCTCIFM."COD_PER" as "ValorCampoGenerico5"
                    ,'' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'IdPersona' as "NombreCampoGenerico7"
                    , PERSONA."IdPersona" as "ValorCampoGenerico7"
                    ,'SEC_CAR_INVER' as "NombreCampoGenerico8"
                    , TBCTCIFM."SEC_CAR_INVER" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
             FROM "BSCL_VIRTUAL"."GES_TBCTCIFM" AS TBCTCIFM       
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
		        	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = TRIM(TBCTCIFM."RUT_CLI")
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
		        	ON PERSONA."IdPersona" = DOCUMENTO_IDENTIDAD."IdPersona"	
		        LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
		       		ON CLIENTE."IdPersona" = PERSONA."IdPersona"
		        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS HOMOLOGACION_CARTERA
			        ON HOMOLOGACION_CARTERA."COD_PER" = TBCTCIFM."COD_PER"
			        AND HOMOLOGACION_CARTERA."RUT_CLI" = TBCTCIFM."RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_RUT_CLI" = TBCTCIFM."SEC_RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_CAR_INVER" = TBCTCIFM."SEC_CAR_INVER"
		        WHERE TBCTCIFM."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
		        AND TBCTCIFM."COD_PER" <=  SUBSTRING(v_periodofinal,1,7)
		        AND TBCTCIFM."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then TBCTCIFM."RUT_CLI" ELSE p_RutCliente END
		        AND HOMOLOGACION_CARTERA."RUT_CLI" IS NULL
		        AND CLIENTE."IdPersona" IS NULL; 
		        
		     -- el portafolio debe existir 
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CarteraFFMM' as "Entidad"
                    ,'no existe portafolio asociado el cliente OPTIMUS' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBCTCIFM."RUT_CLI" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , 'COD_PER' as "NombreCampoGenerico5"
                    , TBCTCIFM."COD_PER" as "ValorCampoGenerico5"
                    ,'SEC_RUT_CLI' as "NombreCampoGenerico6"
                    , TBCTCIFM."SEC_RUT_CLI" as "ValorCampoGenerico6"
                    ,'IdCliente' as "NombreCampoGenerico7"
                    , CLIENTE."IdCliente" as "ValorCampoGenerico7"
                    ,'SEC_CAR_INVER' as "NombreCampoGenerico8"
                    , TBCTCIFM."SEC_CAR_INVER" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
             FROM "BSCL_VIRTUAL"."GES_TBCTCIFM" AS TBCTCIFM       
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
		        	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = TRIM(TBCTCIFM."RUT_CLI")
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
		        	ON PERSONA."IdPersona" = DOCUMENTO_IDENTIDAD."IdPersona"	
		        INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
		       		ON CLIENTE."IdPersona" = PERSONA."IdPersona"
		       	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO
		        	ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
		       		AND PORTAFOLIO."CodigoPortafolio" = TBCTCIFM."SEC_RUT_CLI"||''
		        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS HOMOLOGACION_CARTERA
			        ON HOMOLOGACION_CARTERA."COD_PER" = TBCTCIFM."COD_PER"
			        AND HOMOLOGACION_CARTERA."RUT_CLI" = TBCTCIFM."RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_RUT_CLI" = TBCTCIFM."SEC_RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_CAR_INVER" = TBCTCIFM."SEC_CAR_INVER"
		        WHERE TBCTCIFM."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
		        AND TBCTCIFM."COD_PER" <=  SUBSTRING(v_periodofinal,1,7)
		        AND TBCTCIFM."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then TBCTCIFM."RUT_CLI" ELSE p_RutCliente END
		        AND HOMOLOGACION_CARTERA."RUT_CLI" IS NULL
		        AND PORTAFOLIO."IdCliente" IS NULL;
		       
		       -- el instrumento debe existir
		       insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CarteraFFMM' as "Entidad"
                    ,'el instrumento debe existir OPTIMUS' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBCTCIFM."RUT_CLI" as "ValorCampoGenerico1"
                    , 'NEMO' as "NombreCampoGenerico2"
                    , TRIM(TBCTCIFM."NEMO") as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , 'COD_PER' as "NombreCampoGenerico5"
                    , TBCTCIFM."COD_PER" as "ValorCampoGenerico5"
                    ,'' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'SEC_CAR_INVER' as "NombreCampoGenerico8"
                    , TBCTCIFM."SEC_CAR_INVER" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
             FROM "BSCL_VIRTUAL"."GES_TBCTCIFM" AS TBCTCIFM       
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
		        	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = TRIM(TBCTCIFM."RUT_CLI")
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
		        	ON PERSONA."IdPersona" = DOCUMENTO_IDENTIDAD."IdPersona"	
		        INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
		       		ON CLIENTE."IdPersona" = PERSONA."IdPersona"
		       	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO
		        	ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
		       		AND PORTAFOLIO."CodigoPortafolio" = TBCTCIFM."SEC_RUT_CLI"||''
		       	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
		       		ON INSTRUMENTO."Nemotecnico" = TRIM(TBCTCIFM."NEMO")
		        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS HOMOLOGACION_CARTERA
			        ON HOMOLOGACION_CARTERA."COD_PER" = TBCTCIFM."COD_PER"
			        AND HOMOLOGACION_CARTERA."RUT_CLI" = TBCTCIFM."RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_RUT_CLI" = TBCTCIFM."SEC_RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_CAR_INVER" = TBCTCIFM."SEC_CAR_INVER"
		        WHERE TBCTCIFM."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
		        AND TBCTCIFM."COD_PER" <=  SUBSTRING(v_periodofinal,1,7)
		        AND TBCTCIFM."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then TBCTCIFM."RUT_CLI" ELSE p_RutCliente END
		        AND HOMOLOGACION_CARTERA."RUT_CLI" IS NULL
		        AND INSTRUMENTO."Nemotecnico" IS NULL;
		           
			 -- el  tipo instrumento debe existir
		     insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CarteraFFMM' as "Entidad"
                    ,'el Tipo instrumento debe existir OPTIMUS' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBCTCIFM."RUT_CLI" as "ValorCampoGenerico1"
                    , 'NEMO' as "NombreCampoGenerico2"
                    , TRIM(TBCTCIFM."NEMO") as "ValorCampoGenerico2"
                    , 'IdTipoInstrumento' as "NombreCampoGenerico3"
                    , INSTRUMENTO."IdTipoInstrumento" as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , 'COD_PER' as "NombreCampoGenerico5"
                    , TBCTCIFM."COD_PER" as "ValorCampoGenerico5"
                    ,'' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'SEC_CAR_INVER' as "NombreCampoGenerico8"
                    , TBCTCIFM."SEC_CAR_INVER" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
             FROM "BSCL_VIRTUAL"."GES_TBCTCIFM" AS TBCTCIFM       
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
		        	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = TRIM(TBCTCIFM."RUT_CLI")
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
		        	ON PERSONA."IdPersona" = DOCUMENTO_IDENTIDAD."IdPersona"	
		        INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
		       		ON CLIENTE."IdPersona" = PERSONA."IdPersona"
		       	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO
		        	ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
		       		AND PORTAFOLIO."CodigoPortafolio" = TBCTCIFM."SEC_RUT_CLI"||''
		       	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
		       		ON INSTRUMENTO."Nemotecnico" = TRIM(TBCTCIFM."NEMO")
		       	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPO_INSTRUMENTO
		       		ON TIPO_INSTRUMENTO."IdTipoInstrumento" = INSTRUMENTO."IdTipoInstrumento"
		        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS HOMOLOGACION_CARTERA
			        ON HOMOLOGACION_CARTERA."COD_PER" = TBCTCIFM."COD_PER"
			        AND HOMOLOGACION_CARTERA."RUT_CLI" = TBCTCIFM."RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_RUT_CLI" = TBCTCIFM."SEC_RUT_CLI"
			        AND HOMOLOGACION_CARTERA."SEC_CAR_INVER" = TBCTCIFM."SEC_CAR_INVER"
		        WHERE TBCTCIFM."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
		        AND TBCTCIFM."COD_PER" <=  SUBSTRING(v_periodofinal,1,7)
		        AND TBCTCIFM."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then TBCTCIFM."RUT_CLI" ELSE p_RutCliente END
		        AND HOMOLOGACION_CARTERA."RUT_CLI" IS NULL
		        AND TIPO_INSTRUMENTO."IdTipoInstrumento" IS NULL;		     

             --REVISAMOS CUANTAS OPERACIONES SERAN CARGADAS
               SELECT COUNT(*) INTO Total_Registros_Leidos
                FROM "BSCL_VIRTUAL"."GES_TBCTCIFM" AS TBCTCIFM
				WHERE TBCTCIFM."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
		        	AND TBCTCIFM."COD_PER" <=  SUBSTRING(v_periodofinal,1,7)
		        	AND TBCTCIFM."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then TBCTCIFM."RUT_CLI" ELSE p_RutCliente END;
          
			-- REVISAMOS CUANTOS REGISTROS FUERON CARGADOS
              SELECT COUNT(*) INTO Total_Registros_Cargados 
             	 FROM "BSCL"."BSCL.Homologacion.BD::HOM.Cartera"
             	 WHERE "IdCarga" = p_IdCarga;
             	 
             
            -- INSERTAREMOS LOS DATOS EN LA TABLAS FISICAS OFICIALES DE OPTIMUS
            --para ello solo cargaremos solo aquellos idcartera que se encuentren en la tabla de 
            -- paso temporal HOM.cartera y evitaremso cargar aquellas ya cargadas 
            -- para acotar la busqueda se hara tambien en funcion de el id de carga

			 INSERT INTO "BSCL"."BSCL.Custodia.BD::CUS.Cartera"(
				"IdCartera" ,"FolioCartera" ,"FolioCarteraRelacionada" ,"FolioGarantiaOrigen" ,"FechaIngresoRegistro" ,"FechaModificacionRegistro" ,"IdEmpresa" 
				,"FechaOperacion" ,"FechaVencimientoOperacion" ,"PlazoEnDiasOperacion" ,"IdCliente" ,"IdPortafolio" ,"IdClienteRelacionado" ,"IdPortafolioRelacionado" 
				,"IdEstado" ,"IdTipoProducto" ,"IdTipoCanalIngreso" ,"IdMonedaSistema" ,"IdMovimientoCarteraEnTransito" ,"IdConcepto" ,"IdTipoOperacion" ,"IdOperacion" 
				,"IdTipoComprobante" ,"FolioComprobante" ,"FechaComprobante" ,"FolioExternoReferencia" ,"IdMesa" ,"IdBolsa" ,"IdSucursal" ,"IdAgente" ,"IdCondicionLiquidacion" 
				,"FechaLiquidacion" ,"EsIntraday" ,"UsaGarantiaEspecifica" ,"CobraPrimaAlInicio" ,"IdTipoPrestamo" ,"Glosa" ,"IdUsuario" ,"IdModulo" ,"TipoCarteraNormativa" 
				,"FechaCurseInversion" ,"TipoFactorAjuste" ,"ValorFactorAjusteaFechaCurse" ,"CostoInversionEnFactorAjuste" ,"IdClasificacionTipoInstrumento" ,"IdTipoInstrumento" 
				,"IdInstrumento" ,"Nemotecnico" ,"IdEmisor" ,"NemoSVS" ,"IdTipoReajuste" ,"FechaEmisionInstrumento" ,"FechaVencimientoInstrumento" ,"EsFungible" 
				,"ValorizaPor" ,"TipoValorizacion" ,"CodigoIFRS" ,"IdBoveda" ,"CuentaBoveda" ,"IdMonedaInstrumento" ,"Tamano" ,"TIFAfectoBeneficio" ,"TIFTasa" 
				,"Cantidad" ,"CorteMinimo" ,"ValorRescate" ,"PrecioCompra" ,"TasaCompra" ,"MontoCompra" ,"MontoCompraMonedaInstrumento" ,"TasaAgente" 
				,"PrecioAPlazo" ,"MontoAPlazo" ,"MontoAPlazoMonedaInstrumento" ,"FechaEfectiva" ,"IdPlaza" ,"IdFixing" ,"FechaFixing" ,"HoraFixing" ,"IdModalidadCumplimiento" 
				,"IdReferenciaMercado" ,"IdMonedaPrincipal" ,"IdMonedaSecundaria" ,"ValorTipoCambioCierre" ,"ValorParidadCierre" ,"ValorTipoCambioCosto" 
				,"ValorParidadCosto" ,"MontoMonedaPrincipal" ,"MontoMonedaSecundaria" ,"IdCarga" ,"IdSecuenciaCarteraMigracion" ,"CodigoPeriodo" ,"INGGlosa" 
				,"INGTasaAgente" ,"INGFechaOperacion" ,"INGIdCondicionLiquidacion" ,"INGFechaLiquidacion" ,"INGCantidad" ,"INGValorRescate" 
				,"INGPrecioAPlazo" ,"INGMontoAPlazo" ,"INGMontoAPlazoMonedaInstrumento" ,"INGEsPuntos" ,"INGValorTipoCambioSpot" 
				,"INGValorTipoCambioCotizador" ,"INGTasaPuntosMonedaPrincipal" ,"INGTasaPuntosMonedaSecundaria" ,"INGFechaValorizacionCartera" ,"INGFechaValorizacionAnterior" 
				,"INGFechaValorizacionActual" ,"INGFechaValorizacionProximo" ,"INGPrecioCompra" ,"INGTasaCompra" ,"INGMontoATasaCompraAnterior" ,"INGMontoInteresCompraAnterior" 
				,"INGMontoReajusteCompraAnterior" ,"INGMontoCompraAnteriorMonedaInstrumento" ,"INGMontoInteresCompraAnteriorMonedaInstrumento" ,"INGMontoReajusteCompraAnteriorMonedaInstrumento" 
				,"INGDurationCompraAnterior" ,"INGConvexidadCompraAnterior" ,"INGMontoCompraAnteriorPibMas" ,"INGMontoCompraAnteriorPibMasMonedaInstrumento" 
				,"INGMontoCompraAnteriorPibMenos" ,"INGMontoCompraAnteriorPibMenosMonedaInstrumento" ,"INGMontoATasaCompraActual" ,"INGMontoCompraActualMonedaInstrumento" 
				,"INGDurationCompraActual" ,"INGConvexidadCompraActual" ,"INGMontoCompraActualPibMas" ,"INGMontoCompraActualPibMasMonedaInstrumento" ,"INGMontoCompraActualPibMenos" 
				,"INGMontoCompraActualPibMenosMonedaInstrumento" ,"INGMontoATasaCompraProximo" ,"INGMontoInteresCompraProximo" ,"INGMontoReajusteCompraProximo" 
				,"INGMontoCompraProximoMonedaInstrumento" ,"INGMontoInteresCompraProximoMonedaInstrumento" ,"INGMontoReajusteCompraProximoMonedaInstrumento" 
				,"INGDurationCompraProximo" ,"INGConvexidadCompraProximo" ,"INGFechaPrecioTasaMercadoAnterior" ,"INGFuenteTasaPrecioMercadoAnterior" 
				,"INGPrecioMercadoAnterior" ,"INGTasaMercadoAnterior" ,"INGMontoATasaMercadoAnterior" ,"INGMontoInteresMercadoAnterior" ,"INGMontoReajusteMercadoAnterior" 
				,"INGMontoMercadoAnteriorMonedaInstrumento" ,"INGMontoInteresMercadoAnteriorMonedaInstrumento" ,"INGMontoReajusteMercadoAnteriorMonedaInstrumento" 
				,"INGDurationMercadoAnterior" ,"INGConvexidadMercadoAnterior" ,"INGMontoMercadoAnteriorPibMas" ,"INGMontoMercadoAnteriorPibMasMonedaInstrumento" 
				,"INGMontoMercadoAnteriorPibMenos" ,"INGMontoMercadoAnteriorPibMenosMonedaInstrumento" ,"INGFechaPrecioTasaMercadoActual" ,"INGFuenteTasaPrecioMercadoActual" 
				,"INGPrecioMercadoActual" ,"INGTasaMercadoActual" ,"INGMontoATasaMercadoActual" ,"INGMontoMercadoActualMonedaInstrumento" 
				,"INGDurationMercadoActual" ,"INGConvexidadMercadoActual" ,"INGMontoMercadoActualPibMas" ,"INGMontoMercadoActualPibMasMonedaInstrumento" 
				,"INGMontoMercadoActualPibMenos" ,"INGMontoMercadoActualPibMenosMonedaInstrumento" ,"INGFechaPrecioTasaMercadoProximo" ,"INGFuenteTasaPrecioMercadoProximo" 
				,"INGPrecioMercadoProximo" ,"INGTasaMercadoProximo" ,"INGMontoATasaMercadoProximo" ,"INGMontoInteresMercadoProximo" 
				,"INGMontoReajusteMercadoProximo" ,"INGMontoMercadoProximoMonedaInstrumento" ,"INGMontoInteresMercadoProximoMonedaInstrumento" ,"INGMontoReajusteMercadoProximoMonedaInstrumento" 
				,"INGDurationMercadoProximo" ,"INGConvexidadMercadoProximo" ,"INGTasaCorredora" ,"INGFechaTasaCorredora" ,"INGMontoATasaCorredora" ,"INGMontoATasaCorredoraMonedaInstrumento" 
				,"INGFechaCorteCupon" ,"INGNumeroCupon" ,"INGIdMonedaPagoCupon" ,"INGMontoCorteCupon" ,"INGFechaPagoCorteCupon" ,"INGMontoCapitalCorteCupon" 
				,"INGMontoInteresCorteCupon" ,"INGMontoReajusteCorteCupon" ,"INGMontoCorteCuponMonedaInstrumento" ,"INGMontoCapitalCorteCuponMonedaInstrumento" 
				,"INGMontoInteresCorteCuponMonedaInstrumento" ,"INGMontoReajusteCorteCuponMonedaInstrumento" ,"INGMontoPresenteTIF" ,"INGMontoInteresTIF" 
				,"INGMontoReajusteTIF" ,"INGMontoInteresAcumTIF" ,"INGMontoReajusteAcumTIF" ,"INGMontoPresenteTIFMonedaInstrumento" ,"INGMontoInteresTIFMonedaInstrumento" 
				,"INGMontoReajusteTIFMonedaInstrumento" ,"INGMontoInteresAcumTIFMonedaInstrumento" ,"INGMontoReajusteAcumTIFMonedaInstrumento" ,"INGMontoMonedaPrincipal" 
				,"INGMontoMonedaSecundaria" ,"INGMontoDiferido" ,"INGMontoUtilidadDiferida" ,"INGMontoPerdidaDiferida" ,"INGMontoMargen" ,"INGMontoValorRazonableActivo" 
				,"INGMontoValorRazonablePasivo" ,"INGMontoValorRazonableNeto" ,"INGTasaValorRazonableActivo" ,"INGTasaValorRazonablePasivo" ,"INGMontoDevengoTipoCambio" 
				,"INGMontoDevengoTipoCambioAnterior" ,"INGMontoDiferidoDiario" ,"INGMontoDiferidoAcumulado" ,"INGMontoMatriz" ,"EGRGlosa" ,"EGRFechaOperacion" 
				,"EGRIdTipoOperacion" ,"EGRIdOperacion" ,"EGRIdMovimientoCarteraEnTransito" ,"EGRIdTipoCanalIngreso" ,"EGRIdCondicionLiquidacion" ,"EGRFechaLiquidacion" 
				,"EGRIdTipoComprobante" ,"EGRFolioComprobante" ,"EGRFechaComprobante" ,"EGRFolioExternoReferencia" ,"EGRIdModulo" ,"EGRIdConcepto" ,"EGRIdUsuario" 
				,"EGRIdSucursal" ,"EGRIdAgente" ,"EGRFechaCurseRetiro" ,"EGRCostoRetiroenFactorAjuste" ,"EGRValorFactorAjusteaFechaRetiro" ,"EGRCantidad" ,"EGRValorRescate" 
				,"EGRPrecio" ,"EGRTasa" ,"EGRMonto" ,"EGRMontoMonedaInstrumento" ,"EGRSpread" ,"EGRPrecioAPlazo" ,"EGRMontoAPlazo" ,"EGRMontoAPlazoMonedaInstrumento" 
				,"EGRMontoPresenteTIF" ,"EGRMontoInteresTIF" ,"EGRMontoReajusteTIF" ,"EGRMontoInteresAcumTIF" ,"EGRMontoReajusteAcumTIF" ,"EGRMontoPresenteTIFMonedaInstrumento" 
				,"EGRMontoInteresTIFMonedaInstrumento" ,"EGRMontoReajusteTIFMonedaInstrumento" ,"EGRMontoInteresAcumTIFMonedaInstrumento" ,"EGRMontoReajusteAcumTIFMonedaInstrumento" 
				,"EGRIdSpotAsociado" ,"EGRMontoMonedaPrincipal" ,"EGRMontoMonedaSecundaria" ,"EGRIdMonedaLiquidacion" ,"EGRMontoLiquidacion" ,"EGRMontoLiquidacionMonedaPrincipal" 
				,"EGRValorTipoCambioSpot" ,"EGRValorParidad" ,"EGRValorTipoCambioCotizador" ,"EGREsPuntos" ,"EGRTasaPuntosMonedaPrincipal" ,"EGRTasaPuntosMonedaSecundaria")
			
			select HOM."IdCartera" ,HOM."FolioCartera" ,HOM."FolioCarteraRelacionada" ,HOM."FolioGarantiaOrigen" ,HOM."FechaIngresoRegistro" ,HOM."FechaModificacionRegistro" ,HOM."IdEmpresa" 
				,HOM."FechaOperacion" ,HOM."FechaVencimientoOperacion" ,HOM."PlazoEnDiasOperacion" ,HOM."IdCliente" ,HOM."IdPortafolio" ,HOM."IdClienteRelacionado" ,HOM."IdPortafolioRelacionado" 
				,HOM."IdEstado" ,HOM."IdTipoProducto" ,HOM."IdTipoCanalIngreso" ,HOM."IdMonedaSistema" ,HOM."IdMovimientoCarteraEnTransito" ,HOM."IdConcepto" ,HOM."IdTipoOperacion" ,HOM."IdOperacion" 
				,HOM."IdTipoComprobante" ,HOM."FolioComprobante" ,HOM."FechaComprobante" ,HOM."FolioExternoReferencia" ,HOM."IdMesa" ,HOM."IdBolsa" ,HOM."IdSucursal" ,HOM."IdAgente" ,HOM."IdCondicionLiquidacion" 
				,HOM."FechaLiquidacion" ,HOM."EsIntraday" ,HOM."UsaGarantiaEspecifica" ,HOM."CobraPrimaAlInicio" ,HOM."IdTipoPrestamo" ,HOM."Glosa" ,HOM."IdUsuario" ,HOM."IdModulo" ,HOM."TipoCarteraNormativa" 
				,HOM."FechaCurseInversion" ,HOM."TipoFactorAjuste" ,HOM."ValorFactorAjusteaFechaCurse" ,HOM."CostoInversionEnFactorAjuste" ,HOM."IdClasificacionTipoInstrumento" ,HOM."IdTipoInstrumento" 
				,HOM."IdInstrumento" ,HOM."Nemotecnico" ,HOM."IdEmisor" ,HOM."NemoSVS" ,HOM."IdTipoReajuste" ,HOM."FechaEmisionInstrumento" ,HOM."FechaVencimientoInstrumento" ,HOM."EsFungible" 
				,HOM."ValorizaPor" ,HOM."TipoValorizacion" ,HOM."CodigoIFRS" ,HOM."IdBoveda" ,HOM."CuentaBoveda" ,HOM."IdMonedaInstrumento" ,HOM."Tamano" ,HOM."TIFAfectoBeneficio" ,HOM."TIFTasa" 
				,HOM."Cantidad" ,HOM."CorteMinimo" ,HOM."ValorRescate" ,HOM."PrecioCompra" ,HOM."TasaCompra" ,HOM."MontoCompra" ,HOM."MontoCompraMonedaInstrumento" ,HOM."TasaAgente" 
				,HOM."PrecioAPlazo" ,HOM."MontoAPlazo" ,HOM."MontoAPlazoMonedaInstrumento" ,HOM."FechaEfectiva" ,HOM."IdPlaza" ,HOM."IdFixing" ,HOM."FechaFixing" ,HOM."HoraFixing" ,HOM."IdModalidadCumplimiento" 
				,HOM."IdReferenciaMercado" ,HOM."IdMonedaPrincipal" ,HOM."IdMonedaSecundaria" ,HOM."ValorTipoCambioCierre" ,HOM."ValorParidadCierre" ,HOM."ValorTipoCambioCosto" 
				,HOM."ValorParidadCosto" ,HOM."MontoMonedaPrincipal" ,HOM."MontoMonedaSecundaria" ,HOM."IdCarga" ,HOM."IdSecuenciaCarteraMigracion" ,HOM."CodigoPeriodo" ,HOM."INGGlosa" 
				,HOM."INGTasaAgente" ,HOM."INGFechaOperacion" ,HOM."INGIdCondicionLiquidacion" ,HOM."INGFechaLiquidacion" ,HOM."INGCantidad" ,HOM."INGValorRescate" 
				,HOM."INGPrecioAPlazo" ,HOM."INGMontoAPlazo" ,HOM."INGMontoAPlazoMonedaInstrumento" ,HOM."INGEsPuntos" ,HOM."INGValorTipoCambioSpot" 
				,HOM."INGValorTipoCambioCotizador" ,HOM."INGTasaPuntosMonedaPrincipal" ,HOM."INGTasaPuntosMonedaSecundaria" ,HOM."INGFechaValorizacionCartera" ,HOM."INGFechaValorizacionAnterior" 
				,HOM."INGFechaValorizacionActual" ,HOM."INGFechaValorizacionProximo" ,HOM."INGPrecioCompra" ,HOM."INGTasaCompra" ,HOM."INGMontoATasaCompraAnterior" ,HOM."INGMontoInteresCompraAnterior" 
				,HOM."INGMontoReajusteCompraAnterior" ,HOM."INGMontoCompraAnteriorMonedaInstrumento" ,HOM."INGMontoInteresCompraAnteriorMonedaInstrumento" ,HOM."INGMontoReajusteCompraAnteriorMonedaInstrumento" 
				,HOM."INGDurationCompraAnterior" ,HOM."INGConvexidadCompraAnterior" ,HOM."INGMontoCompraAnteriorPibMas" ,HOM."INGMontoCompraAnteriorPibMasMonedaInstrumento" 
				,HOM."INGMontoCompraAnteriorPibMenos" ,HOM."INGMontoCompraAnteriorPibMenosMonedaInstrumento" ,HOM."INGMontoATasaCompraActual" ,HOM."INGMontoCompraActualMonedaInstrumento" 
				,HOM."INGDurationCompraActual" ,HOM."INGConvexidadCompraActual" ,HOM."INGMontoCompraActualPibMas" ,HOM."INGMontoCompraActualPibMasMonedaInstrumento" ,HOM."INGMontoCompraActualPibMenos" 
				,HOM."INGMontoCompraActualPibMenosMonedaInstrumento" ,HOM."INGMontoATasaCompraProximo" ,HOM."INGMontoInteresCompraProximo" ,HOM."INGMontoReajusteCompraProximo" 
				,HOM."INGMontoCompraProximoMonedaInstrumento" ,HOM."INGMontoInteresCompraProximoMonedaInstrumento" ,HOM."INGMontoReajusteCompraProximoMonedaInstrumento" 
				,HOM."INGDurationCompraProximo" ,HOM."INGConvexidadCompraProximo" ,HOM."INGFechaPrecioTasaMercadoAnterior" ,HOM."INGFuenteTasaPrecioMercadoAnterior" 
				,HOM."INGPrecioMercadoAnterior" ,HOM."INGTasaMercadoAnterior" ,HOM."INGMontoATasaMercadoAnterior" ,HOM."INGMontoInteresMercadoAnterior" ,HOM."INGMontoReajusteMercadoAnterior" 
				,HOM."INGMontoMercadoAnteriorMonedaInstrumento" ,HOM."INGMontoInteresMercadoAnteriorMonedaInstrumento" ,HOM."INGMontoReajusteMercadoAnteriorMonedaInstrumento" 
				,HOM."INGDurationMercadoAnterior" ,HOM."INGConvexidadMercadoAnterior" ,HOM."INGMontoMercadoAnteriorPibMas" ,HOM."INGMontoMercadoAnteriorPibMasMonedaInstrumento" 
				,HOM."INGMontoMercadoAnteriorPibMenos" ,HOM."INGMontoMercadoAnteriorPibMenosMonedaInstrumento" ,HOM."INGFechaPrecioTasaMercadoActual" ,HOM."INGFuenteTasaPrecioMercadoActual" 
				,HOM."INGPrecioMercadoActual" ,HOM."INGTasaMercadoActual" ,HOM."INGMontoATasaMercadoActual" ,HOM."INGMontoMercadoActualMonedaInstrumento" 
				,HOM."INGDurationMercadoActual" ,HOM."INGConvexidadMercadoActual" ,HOM."INGMontoMercadoActualPibMas" ,HOM."INGMontoMercadoActualPibMasMonedaInstrumento" 
				,HOM."INGMontoMercadoActualPibMenos" ,HOM."INGMontoMercadoActualPibMenosMonedaInstrumento" ,HOM."INGFechaPrecioTasaMercadoProximo" ,HOM."INGFuenteTasaPrecioMercadoProximo" 
				,HOM."INGPrecioMercadoProximo" ,HOM."INGTasaMercadoProximo" ,HOM."INGMontoATasaMercadoProximo" ,HOM."INGMontoInteresMercadoProximo" 
				,HOM."INGMontoReajusteMercadoProximo" ,HOM."INGMontoMercadoProximoMonedaInstrumento" ,HOM."INGMontoInteresMercadoProximoMonedaInstrumento" ,HOM."INGMontoReajusteMercadoProximoMonedaInstrumento" 
				,HOM."INGDurationMercadoProximo" ,HOM."INGConvexidadMercadoProximo" ,HOM."INGTasaCorredora" ,HOM."INGFechaTasaCorredora" ,HOM."INGMontoATasaCorredora" ,HOM."INGMontoATasaCorredoraMonedaInstrumento" 
				,HOM."INGFechaCorteCupon" ,HOM."INGNumeroCupon" ,HOM."INGIdMonedaPagoCupon" ,HOM."INGMontoCorteCupon" ,HOM."INGFechaPagoCorteCupon" ,HOM."INGMontoCapitalCorteCupon" 
				,HOM."INGMontoInteresCorteCupon" ,HOM."INGMontoReajusteCorteCupon" ,HOM."INGMontoCorteCuponMonedaInstrumento" ,HOM."INGMontoCapitalCorteCuponMonedaInstrumento" 
				,HOM."INGMontoInteresCorteCuponMonedaInstrumento" ,HOM."INGMontoReajusteCorteCuponMonedaInstrumento" ,HOM."INGMontoPresenteTIF" ,HOM."INGMontoInteresTIF" 
				,HOM."INGMontoReajusteTIF" ,HOM."INGMontoInteresAcumTIF" ,HOM."INGMontoReajusteAcumTIF" ,HOM."INGMontoPresenteTIFMonedaInstrumento" ,HOM."INGMontoInteresTIFMonedaInstrumento" 
				,HOM."INGMontoReajusteTIFMonedaInstrumento" ,HOM."INGMontoInteresAcumTIFMonedaInstrumento" ,HOM."INGMontoReajusteAcumTIFMonedaInstrumento" ,HOM."INGMontoMonedaPrincipal" 
				,HOM."INGMontoMonedaSecundaria" ,HOM."INGMontoDiferido" ,HOM."INGMontoUtilidadDiferida" ,HOM."INGMontoPerdidaDiferida" ,HOM."INGMontoMargen" ,HOM."INGMontoValorRazonableActivo" 
				,HOM."INGMontoValorRazonablePasivo" ,HOM."INGMontoValorRazonableNeto" ,HOM."INGTasaValorRazonableActivo" ,HOM."INGTasaValorRazonablePasivo" ,HOM."INGMontoDevengoTipoCambio" 
				,HOM."INGMontoDevengoTipoCambioAnterior" ,HOM."INGMontoDiferidoDiario" ,HOM."INGMontoDiferidoAcumulado" ,HOM."INGMontoMatriz" ,HOM."EGRGlosa" ,HOM."EGRFechaOperacion" 
				,HOM."EGRIdTipoOperacion" ,HOM."EGRIdOperacion" ,HOM."EGRIdMovimientoCarteraEnTransito" ,HOM."EGRIdTipoCanalIngreso" ,HOM."EGRIdCondicionLiquidacion" ,HOM."EGRFechaLiquidacion" 
				,HOM."EGRIdTipoComprobante" ,HOM."EGRFolioComprobante" ,HOM."EGRFechaComprobante" ,HOM."EGRFolioExternoReferencia" ,HOM."EGRIdModulo" ,HOM."EGRIdConcepto" ,HOM."EGRIdUsuario" 
				,HOM."EGRIdSucursal" ,HOM."EGRIdAgente" ,HOM."EGRFechaCurseRetiro" ,HOM."EGRCostoRetiroenFactorAjuste" ,HOM."EGRValorFactorAjusteaFechaRetiro" ,HOM."EGRCantidad" ,HOM."EGRValorRescate" 
				,HOM."EGRPrecio" ,HOM."EGRTasa" ,HOM."EGRMonto" ,HOM."EGRMontoMonedaInstrumento" ,HOM."EGRSpread"  ,HOM."EGRPrecioAPlazo" ,HOM."EGRMontoAPlazo" ,HOM."EGRMontoAPlazoMonedaInstrumento" 
				,HOM."EGRMontoPresenteTIF" ,HOM."EGRMontoInteresTIF" ,HOM."EGRMontoReajusteTIF" ,HOM."EGRMontoInteresAcumTIF" ,HOM."EGRMontoReajusteAcumTIF" ,HOM."EGRMontoPresenteTIFMonedaInstrumento" 
				,HOM."EGRMontoInteresTIFMonedaInstrumento" ,HOM."EGRMontoReajusteTIFMonedaInstrumento" ,HOM."EGRMontoInteresAcumTIFMonedaInstrumento" ,HOM."EGRMontoReajusteAcumTIFMonedaInstrumento" 
				,HOM."EGRIdSpotAsociado" ,HOM."EGRMontoMonedaPrincipal" ,HOM."EGRMontoMonedaSecundaria" ,HOM."EGRIdMonedaLiquidacion" ,HOM."EGRMontoLiquidacion" ,HOM."EGRMontoLiquidacionMonedaPrincipal" 
				,HOM."EGRValorTipoCambioSpot" ,HOM."EGRValorParidad" ,HOM."EGRValorTipoCambioCotizador" ,HOM."EGREsPuntos" ,HOM."EGRTasaPuntosMonedaPrincipal" ,HOM."EGRTasaPuntosMonedaSecundaria"
               FROM "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS HOM
               LEFT JOIN "BSCL"."BSCL.Custodia.BD::CUS.Cartera" AS CAR
               		ON CAR."IdCartera" = HOM."IdCartera" 
               WHERE HOM."IdCarga" = p_IdCarga  
                 AND HOM."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then HOM."RUT_CLI" ELSE p_RutCliente END
 				 AND CAR."IdCartera" IS NULL;
                
             -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	   			  p_out_ResumenDatosProcesados =  select 'CARGACARTERAFMM' as "CodigoProceso" 
	   			  									,'FMM' as "Modulo" 
	   			 									,Total_Registros_Leidos as "TotalRegistrosSebra"
	   			  									,Total_Registros_Cargados as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_out_ResumenDatosProcesados;

                  IF Total_Registros_Leidos <> Total_Registros_Cargados THEN 
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_FMM' as "Codigo",'falla en la carga fondos mutuos' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			  END IF;
	   			  
                  SIGNAL SALIDA_CONTROLADA;         
	END;
