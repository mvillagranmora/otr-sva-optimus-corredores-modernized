SET SCHEMA BSCL; 
DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraGRV";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraGRV"
	(  
			IN  p_IdCarga 	       		 BIGINT,          	-- ID de la carga 
			IN  p_Modalidad        	 NVARCHAR(5000),    -- Ingreso I incremental, 
			IN  p_inversa 						 NVARCHAR(5000),	  -- S carga cartera inversa perdiodo anterios N no,
			IN  p_RutCliente       	 NVARCHAR(5000) default '0-0',    -- rut del cliente sebra
			IN  p_Periodo_Inicial        NVARCHAR(5000),   	--YYYY-MM-DD
			OUT p_out_ErroresContador int,
			OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(5000),"Descripcion" NVARCHAR(5000))
    ) 
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS   
	BEGIN
		-- VARIABLES VALIDACIONES BASICAS


		DECLARE  p_IdUsuario		   		 BIGINT DEFAULT 66;						-- ID del usuario que solicita el servicio
		
		DECLARE p_IdModulo         	 NVARCHAR(5000) DEFAULT 'CUS';    -- modulo 
		
		DECLARE p_IdEmpresa		   		 BIGINT DEFAULT 1;						-- ID Empresa destino
		
			DECLARE v_out_Resultado  		CHAR(1); 	   --RESULTADO S/N/E
	    DECLARE v_out_Mensaje	 			NVARCHAR(5000); --MENSAJE SALIDA
	    
	    DECLARE v_erroroutmsg  			NVARCHAR(5000);    
	    DECLARE v_TotalPortafolioLeidos	BIGINT;	    
	    DECLARE v_TotalCuentasGrabadas	BIGINT;
	    
	    -- Para LOg
	    DECLARE v_out_refID				BIGINT;    
	    DECLARE v_out_ErroresContador	BIGINT; 
	    DECLARE v_out_ErroresDetalle table ("Codigo" NVARCHAR(5000),"Descripcion" NVARCHAR(5000));	
	    DECLARE v_FechaSistema NVARCHAR(5000);
	    DECLARE v_MaxPeriodo NVARCHAR(5000);
	    DECLARE v_FechaInicioPeriodo NVARCHAR(5000);
	    DECLARE v_periodo  DATE := CONCAT(SUBSTRING(p_Periodo_Inicial,1,7),'-01');
        DECLARE v_periodofinal DATE := CONCAT(SUBSTRING(p_Periodo_Inicial,1,7),'-01'); 
	    DECLARE v_RutCli NVARCHAR(5000):='0-0';
	    DECLARE v_MonedaSistema NVARCHAR(5000);
	    DECLARE Concepto_Egre 					NVARCHAR(5000) default '';
	    DECLARE Concepto_Ingre 					NVARCHAR(5000) default '';
	    
	    
        DECLARE in_FechaInicialant              NVARCHAR(5000);
        DECLARE v_periodo_ant  DATE := CONCAT(SUBSTRING(p_Periodo_Inicial,1,7),'-01');
        declare	in_out_Contador int;
	        
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
			declare v_errorExt NVARCHAR(5000);
			IF ::SQL_ERROR_CODE > 10000 THEN
				call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
				p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
									   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
				select count(1) into p_out_ErroresContador 
				from :p_out_ErroresDetalle;										
			ELSE
				--Error interno no manejado  (El log ser? captado por la capa de XS control)
				p_out_ErroresDetalle       =  select 'ERROR' as "Codigo", ::SQL_ERROR_CODE as "Descripcion" from dummy;-- UNION ALL		
				RESIGNAL; 
			END IF;			
		END;
		p_out_ErroresContador		:= 0;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;   	
        
				
				SELECT LPAD(p_RutCliente, 11,' ') into v_RutCli FROM DUMMY;
				
				SELECT COALESCE("Valor",'') into v_MonedaSistema from "BSCL.Plataforma.BD::PLA.Configuracion"
        		WHERE  "IdEmpresa" = p_IdEmpresa
        		AND  "CodigoParametro" = 'PLA.MonedaSistema';

            
		IF p_Modalidad = 'R' THEN --REPROCESO
         DELETE FROM "BSCL"."BSCL.Homologacion.BD::HOM.Garantia" 
         WHERE "IdCarga" = 	p_IdCarga;	
         
         DELETE FROM "BSCL"."BSCL.Custodia.BD::GTA.Garantia"
         WHERE "IdCarga" = 	p_IdCarga;	
         
		END IF;

-----------
--- LOG ---
-----------

	insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'HOMOLOG_GT_RV' as "Entidad"
                    ,'TBCTGTIA no se carga en HOMOLOGACIÒN mismo idcarga' as "Mensaje"
                    , 'COD_PER' as "NombreCampoGenerico1"
                    , GES_TBCTGTIA."COD_PER" as "ValorCampoGenerico1"
                    , 'NEMO' as "NombreCampoGenerico2"
                    , GES_TBCTGTIA."NEMO" as "ValorCampoGenerico2"
                    , 'RUT_CLI' as "NombreCampoGenerico3"
                    , GES_TBCTGTIA."RUT_CLI" as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdGarantia' as "NombreCampoGenerico6"
                    , Garantia."IdGarantia" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
           FROM "BSCL_VIRTUAL"."GES_TBCTGTIA" as  GES_TBCTGTIA

		        INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Garantia" AS Garantia
							        ON Garantia."COD_PER" = GES_TBCTGTIA."COD_PER"
							        AND Garantia."RUT_CLI" = GES_TBCTGTIA."RUT_CLI"
							        AND Garantia."SEC_RUT_CLI" = GES_TBCTGTIA."SEC_RUT_CLI"
							        AND Garantia."SEC_CAR_INVER" = GES_TBCTGTIA."CONCEPTO_MVTO" 
							        AND TRIM(Garantia."Nemotecnico") = TRIM(GES_TBCTGTIA."NEMO")
		       WHERE 
							        GES_TBCTGTIA."COD_PER" =  SUBSTRING(p_Periodo_Inicial,1,7) 					
							        AND GES_TBCTGTIA."COD_PER" NOT LIKE '%90%'
							        AND GES_TBCTGTIA."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then GES_TBCTGTIA."RUT_CLI" ELSE p_RutCliente END
							        AND GES_TBCTGTIA."CAN_GARANTIA" > 0
							        AND GES_TBCTGTIA."MERCADO" in  ('AC','CF')
									AND Garantia."IdCarga"=p_IdCarga;
				
				
-------------------- FIN -------------------LOG SEBRA

---------------------------------------------			
--- CARGA GARANTIA INVERSA PERIODO ANTERIOR ---
---------------------------------------------
IF p_inversa = 'S' THEN  		
		
        select count(*) into in_out_Contador 
        	from "BSCL_VIRTUAL"."GES_TBCTGTIA" 
        		WHERE "COD_PER" = SUBSTRING((SELECT ADD_MONTHS (TO_DATE (v_periodo_ant, 'YYYY-MM-DD'), -1) FROM DUMMY),1,7)
        		--SUBSTRING((SELECT ADD_MONTHS (TO_DATE (v_periodo_ant, 'YYYY-MM-DD'), -1) FROM DUMMY),1,7)
       AND "RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then "RUT_CLI" ELSE p_RutCliente END
      				 ;
      IF :in_out_Contador > 0 THEN
      
			-----------------------------------------
			-- insertamos RV TODAS LAS GARANTIA RV > 0
			-----------------------------------------

										INSERT INTO "BSCL.Homologacion.BD::HOM.Garantia" (
										  "IdGarantia"
										, "FolioGarantia"
										, "FolioCarteraOrigen"
										, "FolioCarteraGarantizada"
										, "EsEspecifica"
										, "IdContraparteGarantia"
										, "IdOrigenGarantia"
										, "IdEspecieGarantia"
										, "FechaIngresoRegistro"
										, "FechaModificacionRegistro"
										, "IdEmpresa"
										, "IdUsuario"
										, "IdModulo"
										, "IdCliente"
										, "IdPortafolio"
										, "IdEstado"
										, "IdTipoCanalIngreso"
										, "IdMonedaSistema"
										, "CodigoIFRS"
										, "IdConcepto"
										, "FechaOperacion"
										, "FechaVencimientoOperacion"
										, "PlazoEnDiasOperacion"
										, "FolioExternoReferencia"
										, "IdSucursal"
										, "IdAgente"
										, "IdCondicionLiquidacion"
										, "FechaLiquidacion"
										, "IdClasificacionTipoInstrumento"
										, "IdTipoInstrumento"
										, "IdInstrumento"
										, "Nemotecnico"
										, "IdEmisor"
										, "NemoSVS"
										, "IdTipoReajuste"
										, "FechaEmisionInstrumento"
										, "FechaVencimientoInstrumento"
										, "EsFungible"
										, "IdMonedaInstrumento"
										, "Cantidad"
										, "CorteMinimo"
										, "ValorRescate"
										, "INGFechaOperacion"
										, "INGCantidad"
										, "INGValorRescate"
										, "INGMontoATasaCompraAnterior"
										, "INGMontoInteresCompraAnterior"
										, "INGMontoReajusteCompraAnterior"
										, "INGDurationCompraAnterior"
										, "INGConvexidadCompraAnterior"
										, "INGMontoATasaCompraActual"
										, "INGMontoATasaCompraProximo"
										, "INGMontoInteresCompraProximo"
										, "INGMontoReajusteCompraProximo"
										, "INGDurationCompraProximo"
										, "INGConvexidadCompraProximo"
										, "INGFechaPrecioTasaMercadoAnterior"
										, "INGPrecioMercadoAnterior"
										, "INGTasaMercadoAnterior"
										, "INGMontoATasaMercadoAnterior"
										, "INGMontoInteresMercadoAnterior"
										, "INGMontoReajusteMercadoAnterior"
										, "INGDurationMercadoAnterior"
										, "INGConvexidadMercadoAnterior"
										, "INGFechaPrecioTasaMercadoActual"
										, "INGPrecioMercadoActual"
										, "INGTasaMercadoActual"
										, "INGMontoATasaMercadoActual"
										, "INGFechaPrecioTasaMercadoProximo"
										, "INGPrecioMercadoProximo"
										, "INGTasaMercadoProximo"
										, "INGMontoATasaMercadoProximo"
										, "INGMontoInteresMercadoProximo"
										, "INGMontoReajusteMercadoProximo"
										, "INGDurationMercadoProximo"
										, "INGConvexidadMercadoProximo"
										, "INGTasaCorredora"
										, "INGFechaTasaCorredora"
										, "INGMontoATasaCorredora"
										, "INGMontoCorteCupon"
										, "INGFechaCorteCupon"
										, "EGRFechaOperacion"
										, "EGRIdTipoOperacion"
										, "EGRIdOperacion"
										, "EGRIdTipoCanalIngreso"
										, "EGRIdCondicionLiquidacion"
										, "EGRFechaLiquidacion"
										, "EGRFolioExternoReferencia"
										, "EGRIdModulo"
										, "EGRIdConcepto"
										, "EGRIdUsuario"
										, "EGRIdSucursal"
										, "EGRIdAgente"
										, "EGRCantidad"
										, "EGRMonto"
										, "EGRValorRescate"
										, "EGRPrecio"
										, "EGRTasa"
										, "IdCarga"
										, "RUT_CLI"
										, "SEC_RUT_CLI"
										, "SEC_CAR_INVER"
										, "COD_PER"
										, "FOLIO_COMP_ADJ"
										, "TIPO_COMP_ADJ"
										, "LIN_COMP_ADJ"
										, "LIN_ORDEN"
										, "SEC_ORDEN"
										, "TIPO_CARTERA"
										, "ID_ERROR"
										, "SEC_MOVTO"
										, "IdInstrumentoCarteraGarantizada"
										, "RazonSocialContraparteGarantia"
										, "CuentaContraparteGarantia"
										, "IdMovimientoCarteraEnTransito"
										, "IdConceptoOperacionOperacionOrigen"
										, "IdTipoOperacionOperacionOrigen"
										, "IdBolsaOperacionOrigen"
										, "IdMesaOperacionOrigen"
										, "IdBovedaOperacionOrigen"
										, "CuentaBovedaOperacionOrigen"
										, "IdTipoProductoOperacionOrigen"
										, "IdTipoComprobanteOperacionOrigen"
										, "FolioComprobanteOperacionOrigen"
										, "FechaComprobanteOperacionOrigen"
										, "FolioExternoReferenciaOperacionOrigen"
										, "IdClasificacionTipoInstrumentoOperacionOrigen"
										, "IdTipoInstrumentoOperacionOrigen"
										, "IdInstrumentoOperacionOrigen"
										, "NemotecnicoOperacionOrigen"
										, "IdEmisorOperacionOrigen"
										, "NemoSVSOperacionOrigen"
										, "IdTipoReajusteOperacionOrigen"
										, "IdMonedaInstrumentoOperacionOrigen"
										, "FechaEmisionInstrumentoOperacionOrigen"
										, "FechaVencimientoInstrumentoOperacionOrigen"
										, "EsFungibleOperacionOrigen"
										, "ValorizaPorOperacionOrigen"
										, "TipoValorizacionOperacionOrigen"
										, "CodigoIFRSOperacionOrigen"
										, "TamanoOperacionOrigen"
										, "PrecioCompraOperacionOrigen"
										, "TasaCompraOperacionOrigen"
										, "PrecioPromedioTasaCompraOperacionOrigen"
										, "ValorizaPor"
										, "TipoValorizacion"
										, "Tamano"
										, "TIFAfectoBeneficio"
										, "TIFTasa"
										, "PrecioGarantia"
										, "TasaGarantia"
										, "MontoGarantia"
										, "MontoGarantiaMonedaInstrumento"
										, "IdSecuenciaCarteraMigracion"
										, "CodigoPeriodo"
										, "INGIdCondicionLiquidacion"
										, "INGFechaLiquidacion"
										, "INGFechaValorizacionGarantia"
										, "INGFechaValorizacionAnterior"
										, "INGFechaValorizacionActual"
										, "INGFechaValorizacionProximo"
										, "INGPrecioGarantia"
										, "INGTasaGarantia"
										, "INGMontoATasaGarantiaAnterior"
										, "INGMontoInteresGarantiaAnterior"
										, "INGMontoReajusteGarantiaAnterior"
										, "INGMontoGarantiaAnteriorMonedaInstrumento"
										, "INGMontoInteresGarantiaAnteriorMonedaInstrumento"
										, "INGMontoReajusteGarantiaAnteriorMonedaInstrumento"
										, "INGDurationGarantiaAnterior"
										, "INGConvexidadGarantiaAnterior"
										, "INGMontoGarantiaAnteriorPibMas"
										, "INGMontoGarantiaAnteriorPibMasMonedaInstrumento"
										, "INGMontoGarantiaAnteriorPibMenos"
										, "INGMontoGarantiaAnteriorPibMenosMonedaInstrumento"
										, "INGMontoATasaGarantiaActual"
										, "INGMontoGarantiaActualMonedaInstrumento"
										, "INGDurationGarantiaActual"
										, "INGConvexidadGarantiaActual"
										, "INGMontoGarantiaActualPibMas"
										, "INGMontoGarantiaActualPibMasMonedaInstrumento"
										, "INGMontoGarantiaActualPibMenos"
										, "INGMontoGarantiaActualPibMenosMonedaInstrumento"
										, "INGMontoATasaGarantiaProximo"
										, "INGMontoInteresGarantiaProximo"
										, "INGMontoReajusteGarantiaProximo"
										, "INGMontoGarantiaProximoMonedaInstrumento"
										, "INGMontoInteresGarantiaProximoMonedaInstrumento"
										, "INGMontoReajusteGarantiaProximoMonedaInstrumento"
										, "INGDurationGarantiaProximo"
										, "INGConvexidadGarantiaProximo"
										, "INGFuenteTasaPrecioMercadoAnterior"
										, "INGTablaFuenteTasaPrecioMercadoAnterior"
										, "INGMontoMercadoAnteriorMonedaInstrumento"
										, "INGMontoInteresMercadoAnteriorMonedaInstrumento"
										, "INGMontoReajusteMercadoAnteriorMonedaInstrumento"
										, "INGMontoMercadoAnteriorPibMas"
										, "INGMontoMercadoAnteriorPibMasMonedaInstrumento"
										, "INGMontoMercadoAnteriorPibMenos"
										, "INGMontoMercadoAnteriorPibMenosMonedaInstrumento"
										, "INGFuenteTasaPrecioMercadoActual"
										, "INGTablaFuenteTasaPrecioMercadoActual"
										, "INGMontoMercadoActualMonedaInstrumento"
										, "INGDurationMercadoActual"
										, "INGConvexidadMercadoActual"
										, "INGMontoMercadoActualPibMas"
										, "INGMontoMercadoActualPibMasMonedaInstrumento"
										, "INGMontoMercadoActualPibMenos"
										, "INGMontoMercadoActualPibMenosMonedaInstrumento"
										, "INGFuenteTasaPrecioMercadoProximo"
										, "INGTablaFuenteTasaPrecioMercadoProximo"
										, "INGMontoMercadoProximoMonedaInstrumento"
										, "INGMontoInteresMercadoProximoMonedaInstrumento"
										, "INGMontoReajusteMercadoProximoMonedaInstrumento"
										, "INGMontoCompraAnteriorMonedaInstrumento"
										, "INGMontoInteresCompraAnteriorMonedaInstrumento"
										, "INGMontoReajusteCompraAnteriorMonedaInstrumento"
										, "INGMontoCompraAnteriorPibMas"
										, "INGMontoCompraAnteriorPibMasMonedaInstrumento"
										, "INGMontoCompraAnteriorPibMenos"
										, "INGMontoCompraAnteriorPibMenosMonedaInstrumento"
										, "INGMontoCompraActualMonedaInstrumento"
										, "INGDurationCompraActual"
										, "INGConvexidadCompraActual"
										, "INGMontoCompraActualPibMas"
										, "INGMontoCompraActualPibMasMonedaInstrumento"
										, "INGMontoCompraActualPibMenos"
										, "INGMontoCompraActualPibMenosMonedaInstrumento"
										, "INGMontoCompraProximoMonedaInstrumento"
										, "INGMontoInteresCompraProximoMonedaInstrumento"
										, "INGMontoReajusteCompraProximoMonedaInstrumento"
										, "INGMontoATasaCorredoraMonedaInstrumento"
										, "INGNumeroCupon"
										, "INGIdMonedaPagoCupon"
										, "INGFechaPagoCorteCupon"
										, "INGMontoCapitalCorteCupon"
										, "INGMontoInteresCorteCupon"
										, "INGMontoReajusteCorteCupon"
										, "INGMontoCorteCuponMonedaInstrumento"
										, "INGMontoCapitalCorteCuponMonedaInstrumento"
										, "INGMontoInteresCorteCuponMonedaInstrumento"
										, "INGMontoReajusteCorteCuponMonedaInstrumento"
										, "EGRIdMovimientoCarteraEnTransito"
										, "EGRIdTipoComprobante"
										, "EGRFolioComprobante"
										, "EGRFechaComprobante"
										, "EGRMontoMonedaInstrumento"
										)
										                select 	
										                
										"BSCL.Custodia.BD.Secuencias::GarantiaId".NEXTVAL as  "IdGarantia"
										,"BSCL.Custodia.BD.Secuencias::GarantiaFolio".NEXTVAL as "FolioGarantia"
										,0 as  "FolioCarteraOrigen"
										,0  as  "FolioCarteraGarantizada"
										,'N' as "EsEspecifica"
										,0 as  "IdContraparteGarantia"
										,'CUS' as  "IdOrigenGarantia"
										,0 as  "IdEspecieGarantia"
										,(TO_DATE(CONCAT(GES_TBCTGTIA.COD_PER,'-01'),'YYYY-MM-DD')) as "FechaIngresoRegistro"
										,'0001-01-01' as "FechaModificacionRegistro"
										,p_IdEmpresa as "IdEmpresa" ------p_IdEmpresa
										,p_IdUsuario as "IdUsuario" ---p_IdUsuario
										,p_IdModulo as "IdModulo" ------------p_IdModulo
										,COALESCE(Cliente."IdCliente",0) as   "IdCliente"
										,COALESCE(Portafolio."IdPortafolio",0) as  "IdPortafolio"
										,'VIG' as  "IdEstado"
										,'' as  "IdTipoCanalIngreso"
										,v_MonedaSistema  as  "IdMonedaSistema"   -----v_MonedaSistema
										,'' as "CodigoIFRS"
										,'' as "IdConcepto"
										,(TO_DATE(CONCAT(GES_TBCTGTIA.COD_PER,'-01'),'YYYY-MM-DD')) as  "FechaOperacion"
										,GES_TBADGTIA."FEC_VENCIMIENTO" as "FechaVencimientoOperacion"
										,0 as  "PlazoEnDiasOperacion"
										,0 as "FolioExternoReferencia"
										,0 as  "IdSucursal"
										,0 as  "IdAgente"
										,'T0' as  "IdCondicionLiquidacion"
										,GES_TBADGTIA."FEC_VENCIMIENTO" as "FechaLiquidacion"
										,COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento"
										,COALESCE(Instrumento."IdTipoInstrumento",'') as "IdTipoInstrumento"
										,COALESCE(Instrumento."IdInstrumento",0) as "IdInstrumento"
										,TRIM(GES_TBCTGTIA."NEMO") as  "Nemotecnico"
										,COALESCE(Instrumento."IdEmisor",'') as  "IdEmisor"
										,'' as "NemoSVS"
										,COALESCE(Instrumento."IdMoneda",'') as "IdTipoReajuste"
										,'0001-01-01' as "FechaEmisionInstrumento"
										,'0001-01-01' as "FechaVencimientoInstrumento"
										,'N' as "EsFungible"
										,COALESCE(Instrumento."IdMoneda",'') as "IdMonedaInstrumento"
										,GES_TBCTGTIA."CAN_GARANTIA" as "Cantidad"
										,0 as  "CorteMinimo"
										,0 as  "ValorRescate"
										,(TO_DATE(CONCAT(GES_TBCTGTIA.COD_PER,'-01'),'YYYY-MM-DD')) as "INGFechaOperacion"
										,GES_TBCTGTIA."CAN_GARANTIA" as "INGCantidad"
										,0 as  "INGValorRescate"
										,0 as  "INGMontoATasaCompraAnterior"
										,0 as  "INGMontoInteresCompraAnterior"
										,0 as  "INGMontoReajusteCompraAnterior"
										,0 as  "INGDurationCompraAnterior"
										,0 as  "INGConvexidadCompraAnterior"
										,(GES_TBCTGTIA."CAN_GARANTIA" * GES_TBADGTIA."PRECIO_VALORI") as "INGMontoATasaCompraActual"
										,0 as  "INGMontoATasaCompraProximo"
										,0 as  "INGMontoInteresCompraProximo"
										,0 as  "INGMontoReajusteCompraProximo"
										,0 as  "INGDurationCompraProximo"
										,0 as  "INGConvexidadCompraProximo"
										,'0001-01-01' as "INGFechaPrecioTasaMercadoAnterior"
										,0 as "INGPrecioMercadoAnterior"
										,0 as  "INGTasaMercadoAnterior"
										,0 as  "INGMontoATasaMercadoAnterior"
										,0 as  "INGMontoInteresMercadoAnterior"
										,0 as  "INGMontoReajusteMercadoAnterior"
										,0 as  "INGDurationMercadoAnterior"
										,0 as  "INGConvexidadMercadoAnterior"
										,'0001-01-01' as "INGFechaPrecioTasaMercadoActual"
										,GES_TBADGTIA."PRECIO_VALORI" as "INGPrecioMercadoActual"
										,0 as "INGTasaMercadoActual"
										,(GES_TBCTGTIA."CAN_GARANTIA" * GES_TBADGTIA."PRECIO_VALORI") as "INGMontoATasaMercadoActual"
										,'0001-01-01' as "INGFechaPrecioTasaMercadoProximo"
										,0 as    "INGPrecioMercadoProximo"
										,0 as  "INGTasaMercadoProximo"
										,0 as  "INGMontoATasaMercadoProximo"
										,0 as "INGMontoInteresMercadoProximo"
										,0 as  "INGMontoReajusteMercadoProximo"
										,0 as  "INGDurationMercadoProximo"
										,0 as  "INGConvexidadMercadoProximo"
										,0 as  "INGTasaCorredora"
										,'0001-01-01' as "INGFechaTasaCorredora"
										,0 as "INGMontoATasaCorredora"
										,0 as  "INGMontoCorteCupon"
										,'0001-01-01' as "INGFechaCorteCupon"
										,ADD_MONTHS(TO_DATE(CONCAT(GES_TBCTGTIA.COD_PER,'-01'),'YYYY-MM-DD'),1) as "EGRFechaOperacion"
										,'' as  "EGRIdTipoOperacion"
										,0 as  "EGRIdOperacion"
										,'' as  "EGRIdTipoCanalIngreso"
										,'' as "EGRIdCondicionLiquidacion"
										,'0001-01-01' as "EGRFechaLiquidacion"
										,0 as  "EGRFolioExternoReferencia"
										,p_IdModulo as "EGRIdModulo" -------------p_IdModulo
										,'' as "EGRIdConcepto"
										,0 as  "EGRIdUsuario"
										,0 as  "EGRIdSucursal"
										,0 as  "EGRIdAgente"
										,GES_TBCTGTIA."CAN_GARANTIA" as "EGRCantidad"
										,0 as "EGRMonto"
										,0 as  "EGRValorRescate"
										, 0 as "EGRPrecio"
										,0 as  "EGRTasa"
										,p_IdCarga as "IdCarga" --------p_IdCarga
										,GES_TBCTGTIA."RUT_CLI" as "RUT_CLI"
										,GES_TBCTGTIA."SEC_RUT_CLI" as "SEC_RUT_CLI"
										,GES_TBCTGTIA."CONCEPTO_MVTO" as "SEC_CAR_INVER"
										,GES_TBCTGTIA."COD_PER" as "COD_PER"
										,0 as "FOLIO_COMP_ADJ"
										,'' as "TIPO_COMP_ADJ"
										,0 as  "LIN_COMP_ADJ"
										,0 as  "LIN_ORDEN"
										,0 as  "SEC_ORDEN"
										,'' as  "TIPO_CARTERA"
										,0 as  "ID_ERROR"
										,0 as  "SEC_MOVTO"
										
										,  0 as "IdInstrumentoCarteraGarantizada"
										, '' as  "RazonSocialContraparteGarantia"
										,0 as  "CuentaContraparteGarantia"
										,  0 as "IdMovimientoCarteraEnTransito"
										,  0 as "IdConceptoOperacionOperacionOrigen"
										,  0 as "IdTipoOperacionOperacionOrigen"
										,  0 as "IdBolsaOperacionOrigen"
										,  0 as "IdMesaOperacionOrigen"
										,  0 as "IdBovedaOperacionOrigen"
										, 0 as "CuentaBovedaOperacionOrigen"
										,  0 as "IdTipoProductoOperacionOrigen"
										,  0 as "IdTipoComprobanteOperacionOrigen"
										, 0 as "FolioComprobanteOperacionOrigen"
										, '0001-01-01' as "FechaComprobanteOperacionOrigen"
										, 0 as "FolioExternoReferenciaOperacionOrigen"
										,  0 as "IdClasificacionTipoInstrumentoOperacionOrigen"
										,  0 as "IdTipoInstrumentoOperacionOrigen"
										,  0 as "IdInstrumentoOperacionOrigen"
										, '' as "NemotecnicoOperacionOrigen"
										,  0 as "IdEmisorOperacionOrigen"
										, '' as "NemoSVSOperacionOrigen"
										,  0 as "IdTipoReajusteOperacionOrigen"
										,  0 as "IdMonedaInstrumentoOperacionOrigen"
										, '0001-01-01' as "FechaEmisionInstrumentoOperacionOrigen"
										, '0001-01-01' as "FechaVencimientoInstrumentoOperacionOrigen"
										, '' as "EsFungibleOperacionOrigen"
										, 'T' as "ValorizaPorOperacionOrigen"
										,COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) as  "TipoValorizacionOperacionOrigen"
										,'' as  "CodigoIFRSOperacionOrigen"
										, 0 as "TamanoOperacionOrigen"
										, 0 as "PrecioCompraOperacionOrigen"
										, 0 as "TasaCompraOperacionOrigen"
										, 0 as "PrecioPromedioTasaCompraOperacionOrigen"
										, 'T' as "ValorizaPor"
										,COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) as "TipoValorizacion"
										, 0 as "Tamano"
										, 0 as "TIFAfectoBeneficio"
										, 0 as "TIFTasa"
										, 0 as "PrecioGarantia"
										, 0 as "TasaGarantia"
										, 0 as "MontoGarantia"
										, 0 as "MontoGarantiaMonedaInstrumento"
										,  0 as "IdSecuenciaCarteraMigracion"
										, GES_TBCTGTIA."COD_PER" as "CodigoPeriodo"
										, 0 as "INGIdCondicionLiquidacion"
										, '0001-01-01' as "INGFechaLiquidacion"
										,  '0001-01-01' as "INGFechaValorizacionGarantia"
										,  '0001-01-01' as "INGFechaValorizacionAnterior"
										,  '0001-01-01' as "INGFechaValorizacionActual"
										,  '0001-01-01' as "INGFechaValorizacionProximo"
										, 0 as "INGPrecioGarantia"
										, 0 as "INGTasaGarantia"
										, 0 as "INGMontoATasaGarantiaAnterior"
										, 0 as "INGMontoInteresGarantiaAnterior"
										, 0 as "INGMontoReajusteGarantiaAnterior"
										, 0 as "INGMontoGarantiaAnteriorMonedaInstrumento"
										, 0 as "INGMontoInteresGarantiaAnteriorMonedaInstrumento"
										, 0 as "INGMontoReajusteGarantiaAnteriorMonedaInstrumento"
										, 0 as "INGDurationGarantiaAnterior"
										, 0 as "INGConvexidadGarantiaAnterior"
										, 0 as "INGMontoGarantiaAnteriorPibMas"
										, 0 as "INGMontoGarantiaAnteriorPibMasMonedaInstrumento"
										, 0 as "INGMontoGarantiaAnteriorPibMenos"
										, 0 as "INGMontoGarantiaAnteriorPibMenosMonedaInstrumento"
										, 0 as "INGMontoATasaGarantiaActual"
										, 0 as "INGMontoGarantiaActualMonedaInstrumento"
										, 0 as "INGDurationGarantiaActual"
										, 0 as "INGConvexidadGarantiaActual"
										, 0 as "INGMontoGarantiaActualPibMas"
										, 0 as "INGMontoGarantiaActualPibMasMonedaInstrumento"
										, 0 as "INGMontoGarantiaActualPibMenos"
										, 0 as "INGMontoGarantiaActualPibMenosMonedaInstrumento"
										, 0 as "INGMontoATasaGarantiaProximo"
										, 0 as "INGMontoInteresGarantiaProximo"
										, 0 as "INGMontoReajusteGarantiaProximo"
										, 0 as "INGMontoGarantiaProximoMonedaInstrumento"
										, 0 as "INGMontoInteresGarantiaProximoMonedaInstrumento"
										, 0 as "INGMontoReajusteGarantiaProximoMonedaInstrumento"
										, 0 as "INGDurationGarantiaProximo"
										, 0 as "INGConvexidadGarantiaProximo"
										, 0 as "INGFuenteTasaPrecioMercadoAnterior"
										, 0 as "INGTablaFuenteTasaPrecioMercadoAnterior"
										, 0 as "INGMontoMercadoAnteriorMonedaInstrumento"
										, 0 as "INGMontoInteresMercadoAnteriorMonedaInstrumento"
										, 0 as "INGMontoReajusteMercadoAnteriorMonedaInstrumento"
										, 0 as "INGMontoMercadoAnteriorPibMas"
										, 0 as "INGMontoMercadoAnteriorPibMasMonedaInstrumento"
										, 0 as "INGMontoMercadoAnteriorPibMenos"
										, 0 as "INGMontoMercadoAnteriorPibMenosMonedaInstrumento"
										, 0 as "INGFuenteTasaPrecioMercadoActual"
										, 0 as "INGTablaFuenteTasaPrecioMercadoActual"
										, 0 as "INGMontoMercadoActualMonedaInstrumento"
										, 0 as "INGDurationMercadoActual"
										, 0 as "INGConvexidadMercadoActual"
										, 0 as "INGMontoMercadoActualPibMas"
										, 0 as "INGMontoMercadoActualPibMasMonedaInstrumento"
										, 0 as "INGMontoMercadoActualPibMenos"
										, 0 as "INGMontoMercadoActualPibMenosMonedaInstrumento"
										, 0 as "INGFuenteTasaPrecioMercadoProximo"
										, 0 as "INGTablaFuenteTasaPrecioMercadoProximo"
										, 0 as "INGMontoMercadoProximoMonedaInstrumento"
										, 0 as "INGMontoInteresMercadoProximoMonedaInstrumento"
										, 0 as "INGMontoReajusteMercadoProximoMonedaInstrumento"
										, 0 as "INGMontoCompraAnteriorMonedaInstrumento"
										, 0 as "INGMontoInteresCompraAnteriorMonedaInstrumento"
										, 0 as "INGMontoReajusteCompraAnteriorMonedaInstrumento"
										, 0 as "INGMontoCompraAnteriorPibMas"
										, 0 as "INGMontoCompraAnteriorPibMasMonedaInstrumento"
										, 0 as "INGMontoCompraAnteriorPibMenos"
										, 0 as "INGMontoCompraAnteriorPibMenosMonedaInstrumento"
										, 0 as "INGMontoCompraActualMonedaInstrumento"
										, 0 as "INGDurationCompraActual"
										, 0 as "INGConvexidadCompraActual"
										, 0 as "INGMontoCompraActualPibMas"
										, 0 as "INGMontoCompraActualPibMasMonedaInstrumento"
										, 0 as "INGMontoCompraActualPibMenos"
										, 0 as "INGMontoCompraActualPibMenosMonedaInstrumento"
										, 0 as "INGMontoCompraProximoMonedaInstrumento"
										, 0 as "INGMontoInteresCompraProximoMonedaInstrumento"
										, 0 as "INGMontoReajusteCompraProximoMonedaInstrumento"
										, 0 as "INGMontoATasaCorredoraMonedaInstrumento"
										, 0 as "INGNumeroCupon"
										, 0 as "INGIdMonedaPagoCupon"
										,  '0001-01-01' as "INGFechaPagoCorteCupon"
										, 0 as "INGMontoCapitalCorteCupon"
										, 0 as "INGMontoInteresCorteCupon"
										, 0 as "INGMontoReajusteCorteCupon"
										, 0 as "INGMontoCorteCuponMonedaInstrumento"
										, 0 as "INGMontoCapitalCorteCuponMonedaInstrumento"
										, 0 as "INGMontoInteresCorteCuponMonedaInstrumento"
										, 0 as "INGMontoReajusteCorteCuponMonedaInstrumento"
										, 0 as "EGRIdMovimientoCarteraEnTransito"
										, 0 as "EGRIdTipoComprobante"
										, 0 as "EGRFolioComprobante"
										, '0001-01-01' as "EGRFechaComprobante"
										, 0 as "EGRMontoMonedaInstrumento"
										
                
                FROM "BSCL_VIRTUAL"."GES_TBCTGTIA" AS GES_TBCTGTIA

			            INNER JOIN "BSCL_VIRTUAL"."GES_TBADGTIA" AS GES_TBADGTIA
						            ON TRIM(GES_TBADGTIA."RUT_CLI") = TRIM(GES_TBCTGTIA."RUT_CLI")
						            AND GES_TBADGTIA."SEC_RUT_CLI" = GES_TBCTGTIA."SEC_RUT_CLI"
						            AND TRIM(GES_TBADGTIA."NEMO") = TRIM(GES_TBCTGTIA."NEMO")
						            AND TRIM(GES_TBADGTIA."MERCADO") = TRIM(GES_TBCTGTIA."MERCADO")
						            AND TRIM(GES_TBADGTIA."CONCEPTO_MVTO") = TRIM(GES_TBCTGTIA."CONCEPTO_MVTO")
						            AND GES_TBADGTIA."VAL_MERCADO"<>0 AND GES_TBCTGTIA."CAN_GARANTIA">0
						            AND TRIM(GES_TBADGTIA."COD_PER") = TRIM(GES_TBCTGTIA."COD_PER")

		        LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
							        ON DocumentoIdentidad."DocumentoIdentidad" = TRIM((GES_TBCTGTIA."RUT_CLI"))

		        LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
		        					ON Persona."IdPersona" = DocumentoIdentidad."IdPersona"

		        LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
							        ON Cliente."IdPersona" = Persona."IdPersona"

		        LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
							        ON Portafolio."IdCliente" = Cliente."IdCliente"
							        AND Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTGTIA."SEC_RUT_CLI")

		        LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
							        ON TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTGTIA."NEMO"))

		        LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
							        ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoInstrumento."IdTipoInstrumento"))

		        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Garantia" AS Garantia
							        ON 	Garantia."COD_PER" 	 = GES_TBCTGTIA."COD_PER"
							        AND Garantia."RUT_CLI"	 = GES_TBCTGTIA."RUT_CLI"
							        AND Garantia."SEC_RUT_CLI" = GES_TBCTGTIA."SEC_RUT_CLI"
							        AND Garantia."SEC_CAR_INVER"   = GES_TBCTGTIA."CONCEPTO_MVTO"  
				LEFT JOIN "BSCL"."BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento" AS TipoValorizacionPorTipoInstrumento
											ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoValorizacionPorTipoInstrumento."IdTipoInstrumento"))         

		        WHERE 	
				        		GES_TBCTGTIA."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
						        AND GES_TBCTGTIA."CAN_GARANTIA" > 0
						        AND GES_TBCTGTIA."MERCADO" in  ('AC','CF')
						        AND GES_TBCTGTIA."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' then  GES_TBCTGTIA."RUT_CLI" ELSE p_RutCliente END
		 						AND Garantia."RUT_CLI" IS NULL;


								end if;	        

			END IF;
-------------------------------------------------------------------------------------		          
                -----------------------------------
                -- insertamos garantias rv      ---
                -----------------------------------
										INSERT INTO "BSCL.Homologacion.BD::HOM.Garantia" (
										  "IdGarantia"
										, "FolioGarantia"
										, "FolioCarteraOrigen"
										, "FolioCarteraGarantizada"
										, "EsEspecifica"
										, "IdContraparteGarantia"
										, "IdOrigenGarantia"
										, "IdEspecieGarantia"
										, "FechaIngresoRegistro"
										, "FechaModificacionRegistro"
										, "IdEmpresa"
										, "IdUsuario"
										, "IdModulo"
										, "IdCliente"
										, "IdPortafolio"
										, "IdEstado"
										, "IdTipoCanalIngreso"
										, "IdMonedaSistema"
										, "CodigoIFRS"
										, "IdConcepto"
										, "FechaOperacion"
										, "FechaVencimientoOperacion"
										, "PlazoEnDiasOperacion"
										, "FolioExternoReferencia"
										, "IdSucursal"
										, "IdAgente"
										, "IdCondicionLiquidacion"
										, "FechaLiquidacion"
										, "IdClasificacionTipoInstrumento"
										, "IdTipoInstrumento"
										, "IdInstrumento"
										, "Nemotecnico"
										, "IdEmisor"
										, "NemoSVS"
										, "IdTipoReajuste"
										, "FechaEmisionInstrumento"
										, "FechaVencimientoInstrumento"
										, "EsFungible"
										, "IdMonedaInstrumento"
										, "Cantidad"
										, "CorteMinimo"
										, "ValorRescate"
										, "INGFechaOperacion"
										, "INGCantidad"
										, "INGValorRescate"
										, "INGMontoATasaCompraAnterior"
										, "INGMontoInteresCompraAnterior"
										, "INGMontoReajusteCompraAnterior"
										, "INGDurationCompraAnterior"
										, "INGConvexidadCompraAnterior"
										, "INGMontoATasaCompraActual"
										, "INGMontoATasaCompraProximo"
										, "INGMontoInteresCompraProximo"
										, "INGMontoReajusteCompraProximo"
										, "INGDurationCompraProximo"
										, "INGConvexidadCompraProximo"
										, "INGFechaPrecioTasaMercadoAnterior"
										, "INGPrecioMercadoAnterior"
										, "INGTasaMercadoAnterior"
										, "INGMontoATasaMercadoAnterior"
										, "INGMontoInteresMercadoAnterior"
										, "INGMontoReajusteMercadoAnterior"
										, "INGDurationMercadoAnterior"
										, "INGConvexidadMercadoAnterior"
										, "INGFechaPrecioTasaMercadoActual"
										, "INGPrecioMercadoActual"
										, "INGTasaMercadoActual"
										, "INGMontoATasaMercadoActual"
										, "INGFechaPrecioTasaMercadoProximo"
										, "INGPrecioMercadoProximo"
										, "INGTasaMercadoProximo"
										, "INGMontoATasaMercadoProximo"
										, "INGMontoInteresMercadoProximo"
										, "INGMontoReajusteMercadoProximo"
										, "INGDurationMercadoProximo"
										, "INGConvexidadMercadoProximo"
										, "INGTasaCorredora"
										, "INGFechaTasaCorredora"
										, "INGMontoATasaCorredora"
										, "INGMontoCorteCupon"
										, "INGFechaCorteCupon"
										, "EGRFechaOperacion"
										, "EGRIdTipoOperacion"
										, "EGRIdOperacion"
										, "EGRIdTipoCanalIngreso"
										, "EGRIdCondicionLiquidacion"
										, "EGRFechaLiquidacion"
										, "EGRFolioExternoReferencia"
										, "EGRIdModulo"
										, "EGRIdConcepto"
										, "EGRIdUsuario"
										, "EGRIdSucursal"
										, "EGRIdAgente"
										, "EGRCantidad"
										, "EGRMonto"
										, "EGRValorRescate"
										, "EGRPrecio"
										, "EGRTasa"
										, "IdCarga"
										, "RUT_CLI"
										, "SEC_RUT_CLI"
										, "SEC_CAR_INVER"
										, "COD_PER"
										, "FOLIO_COMP_ADJ"
										, "TIPO_COMP_ADJ"
										, "LIN_COMP_ADJ"
										, "LIN_ORDEN"
										, "SEC_ORDEN"
										, "TIPO_CARTERA"
										, "ID_ERROR"
										, "SEC_MOVTO"
										, "IdInstrumentoCarteraGarantizada"
										, "RazonSocialContraparteGarantia"
										, "CuentaContraparteGarantia"
										, "IdMovimientoCarteraEnTransito"
										, "IdConceptoOperacionOperacionOrigen"
										, "IdTipoOperacionOperacionOrigen"
										, "IdBolsaOperacionOrigen"
										, "IdMesaOperacionOrigen"
										, "IdBovedaOperacionOrigen"
										, "CuentaBovedaOperacionOrigen"
										, "IdTipoProductoOperacionOrigen"
										, "IdTipoComprobanteOperacionOrigen"
										, "FolioComprobanteOperacionOrigen"
										, "FechaComprobanteOperacionOrigen"
										, "FolioExternoReferenciaOperacionOrigen"
										, "IdClasificacionTipoInstrumentoOperacionOrigen"
										, "IdTipoInstrumentoOperacionOrigen"
										, "IdInstrumentoOperacionOrigen"
										, "NemotecnicoOperacionOrigen"
										, "IdEmisorOperacionOrigen"
										, "NemoSVSOperacionOrigen"
										, "IdTipoReajusteOperacionOrigen"
										, "IdMonedaInstrumentoOperacionOrigen"
										, "FechaEmisionInstrumentoOperacionOrigen"
										, "FechaVencimientoInstrumentoOperacionOrigen"
										, "EsFungibleOperacionOrigen"
										, "ValorizaPorOperacionOrigen"
										, "TipoValorizacionOperacionOrigen"
										, "CodigoIFRSOperacionOrigen"
										, "TamanoOperacionOrigen"
										, "PrecioCompraOperacionOrigen"
										, "TasaCompraOperacionOrigen"
										, "PrecioPromedioTasaCompraOperacionOrigen"
										, "ValorizaPor"
										, "TipoValorizacion"
										, "Tamano"
										, "TIFAfectoBeneficio"
										, "TIFTasa"
										, "PrecioGarantia"
										, "TasaGarantia"
										, "MontoGarantia"
										, "MontoGarantiaMonedaInstrumento"
										, "IdSecuenciaCarteraMigracion"
										, "CodigoPeriodo"
										, "INGIdCondicionLiquidacion"
										, "INGFechaLiquidacion"
										, "INGFechaValorizacionGarantia"
										, "INGFechaValorizacionAnterior"
										, "INGFechaValorizacionActual"
										, "INGFechaValorizacionProximo"
										, "INGPrecioGarantia"
										, "INGTasaGarantia"
										, "INGMontoATasaGarantiaAnterior"
										, "INGMontoInteresGarantiaAnterior"
										, "INGMontoReajusteGarantiaAnterior"
										, "INGMontoGarantiaAnteriorMonedaInstrumento"
										, "INGMontoInteresGarantiaAnteriorMonedaInstrumento"
										, "INGMontoReajusteGarantiaAnteriorMonedaInstrumento"
										, "INGDurationGarantiaAnterior"
										, "INGConvexidadGarantiaAnterior"
										, "INGMontoGarantiaAnteriorPibMas"
										, "INGMontoGarantiaAnteriorPibMasMonedaInstrumento"
										, "INGMontoGarantiaAnteriorPibMenos"
										, "INGMontoGarantiaAnteriorPibMenosMonedaInstrumento"
										, "INGMontoATasaGarantiaActual"
										, "INGMontoGarantiaActualMonedaInstrumento"
										, "INGDurationGarantiaActual"
										, "INGConvexidadGarantiaActual"
										, "INGMontoGarantiaActualPibMas"
										, "INGMontoGarantiaActualPibMasMonedaInstrumento"
										, "INGMontoGarantiaActualPibMenos"
										, "INGMontoGarantiaActualPibMenosMonedaInstrumento"
										, "INGMontoATasaGarantiaProximo"
										, "INGMontoInteresGarantiaProximo"
										, "INGMontoReajusteGarantiaProximo"
										, "INGMontoGarantiaProximoMonedaInstrumento"
										, "INGMontoInteresGarantiaProximoMonedaInstrumento"
										, "INGMontoReajusteGarantiaProximoMonedaInstrumento"
										, "INGDurationGarantiaProximo"
										, "INGConvexidadGarantiaProximo"
										, "INGFuenteTasaPrecioMercadoAnterior"
										, "INGTablaFuenteTasaPrecioMercadoAnterior"
										, "INGMontoMercadoAnteriorMonedaInstrumento"
										, "INGMontoInteresMercadoAnteriorMonedaInstrumento"
										, "INGMontoReajusteMercadoAnteriorMonedaInstrumento"
										, "INGMontoMercadoAnteriorPibMas"
										, "INGMontoMercadoAnteriorPibMasMonedaInstrumento"
										, "INGMontoMercadoAnteriorPibMenos"
										, "INGMontoMercadoAnteriorPibMenosMonedaInstrumento"
										, "INGFuenteTasaPrecioMercadoActual"
										, "INGTablaFuenteTasaPrecioMercadoActual"
										, "INGMontoMercadoActualMonedaInstrumento"
										, "INGDurationMercadoActual"
										, "INGConvexidadMercadoActual"
										, "INGMontoMercadoActualPibMas"
										, "INGMontoMercadoActualPibMasMonedaInstrumento"
										, "INGMontoMercadoActualPibMenos"
										, "INGMontoMercadoActualPibMenosMonedaInstrumento"
										, "INGFuenteTasaPrecioMercadoProximo"
										, "INGTablaFuenteTasaPrecioMercadoProximo"
										, "INGMontoMercadoProximoMonedaInstrumento"
										, "INGMontoInteresMercadoProximoMonedaInstrumento"
										, "INGMontoReajusteMercadoProximoMonedaInstrumento"
										, "INGMontoCompraAnteriorMonedaInstrumento"
										, "INGMontoInteresCompraAnteriorMonedaInstrumento"
										, "INGMontoReajusteCompraAnteriorMonedaInstrumento"
										, "INGMontoCompraAnteriorPibMas"
										, "INGMontoCompraAnteriorPibMasMonedaInstrumento"
										, "INGMontoCompraAnteriorPibMenos"
										, "INGMontoCompraAnteriorPibMenosMonedaInstrumento"
										, "INGMontoCompraActualMonedaInstrumento"
										, "INGDurationCompraActual"
										, "INGConvexidadCompraActual"
										, "INGMontoCompraActualPibMas"
										, "INGMontoCompraActualPibMasMonedaInstrumento"
										, "INGMontoCompraActualPibMenos"
										, "INGMontoCompraActualPibMenosMonedaInstrumento"
										, "INGMontoCompraProximoMonedaInstrumento"
										, "INGMontoInteresCompraProximoMonedaInstrumento"
										, "INGMontoReajusteCompraProximoMonedaInstrumento"
										, "INGMontoATasaCorredoraMonedaInstrumento"
										, "INGNumeroCupon"
										, "INGIdMonedaPagoCupon"
										, "INGFechaPagoCorteCupon"
										, "INGMontoCapitalCorteCupon"
										, "INGMontoInteresCorteCupon"
										, "INGMontoReajusteCorteCupon"
										, "INGMontoCorteCuponMonedaInstrumento"
										, "INGMontoCapitalCorteCuponMonedaInstrumento"
										, "INGMontoInteresCorteCuponMonedaInstrumento"
										, "INGMontoReajusteCorteCuponMonedaInstrumento"
										, "EGRIdMovimientoCarteraEnTransito"
										, "EGRIdTipoComprobante"
										, "EGRFolioComprobante"
										, "EGRFechaComprobante"
										, "EGRMontoMonedaInstrumento"
										)
										                select 	
										                
										"BSCL.Custodia.BD.Secuencias::GarantiaId".NEXTVAL as  "IdGarantia"
										,"BSCL.Custodia.BD.Secuencias::GarantiaFolio".NEXTVAL as "FolioGarantia"
										,0 as  "FolioCarteraOrigen"
										,0  as  "FolioCarteraGarantizada"
										,'N' as "EsEspecifica"
										,0 as  "IdContraparteGarantia"
										,'CUS' as  "IdOrigenGarantia"
										,0 as  "IdEspecieGarantia"
										,(TO_DATE(CONCAT(GES_TBCTGTIA.COD_PER,'-01'),'YYYY-MM-DD')) as "FechaIngresoRegistro"
										,'0001-01-01' as "FechaModificacionRegistro"
										,p_IdEmpresa as "IdEmpresa" ------p_IdEmpresa
										,p_IdUsuario as "IdUsuario" ---p_IdUsuario
										,p_IdModulo as "IdModulo" ------------p_IdModulo
										,COALESCE(Cliente."IdCliente",0) as   "IdCliente"
										,COALESCE(Portafolio."IdPortafolio",0) as  "IdPortafolio"
										,'LIQ' as  "IdEstado"
										,'' as  "IdTipoCanalIngreso"
										,v_MonedaSistema  as  "IdMonedaSistema"   -----v_MonedaSistema
										,'' as "CodigoIFRS"
										,'' as "IdConcepto"
										,'0001-01-01' as  "FechaOperacion"
										,GES_TBADGTIA."FEC_VENCIMIENTO" as "FechaVencimientoOperacion"
										,0 as  "PlazoEnDiasOperacion"
										,0 as "FolioExternoReferencia"
										,0 as  "IdSucursal"
										,0 as  "IdAgente"
										,'T0' as  "IdCondicionLiquidacion"
										,GES_TBCTGTIA."FECHA_EMISION" as "FechaLiquidacion"
										,COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento"
										,COALESCE(Instrumento."IdTipoInstrumento",'') as "IdTipoInstrumento"
										,COALESCE(Instrumento."IdInstrumento",0) as "IdInstrumento"
										,TRIM(GES_TBCTGTIA."NEMO")  as  "Nemotecnico"
										,COALESCE(Instrumento."IdEmisor",'') as  "IdEmisor"
										,'' as "NemoSVS"
										,COALESCE(Instrumento."IdMoneda",'') as "IdTipoReajuste"
										,'0001-01-01' as "FechaEmisionInstrumento"
										,'0001-01-01' as "FechaVencimientoInstrumento"
										,'N' as "EsFungible"
										,COALESCE(Instrumento."IdMoneda",'') as "IdMonedaInstrumento"
										,GES_TBCTGTIA."CAN_GARANTIA" as "Cantidad"
										,0 as  "CorteMinimo"
										,0 as  "ValorRescate"
										,(TO_DATE(CONCAT(GES_TBCTGTIA.COD_PER,'-01'),'YYYY-MM-DD')) as "INGFechaOperacion"
										,GES_TBCTGTIA."CAN_GARANTIA" as "INGCantidad"
										,0 as  "INGValorRescate"
										,0 as  "INGMontoATasaCompraAnterior"
										,0 as  "INGMontoInteresCompraAnterior"
										,0 as  "INGMontoReajusteCompraAnterior"
										,0 as  "INGDurationCompraAnterior"
										,0 as  "INGConvexidadCompraAnterior"
										,(GES_TBCTGTIA."CAN_GARANTIA" * GES_TBADGTIA."PRECIO_VALORI") as "INGMontoATasaCompraActual"
										,0 as  "INGMontoATasaCompraProximo"
										,0 as  "INGMontoInteresCompraProximo"
										,0 as  "INGMontoReajusteCompraProximo"
										,0 as  "INGDurationCompraProximo"
										,0 as  "INGConvexidadCompraProximo"
										,'0001-01-01' as "INGFechaPrecioTasaMercadoAnterior"
										,0 as "INGPrecioMercadoAnterior"
										,0 as  "INGTasaMercadoAnterior"
										,0 as  "INGMontoATasaMercadoAnterior"
										,0 as  "INGMontoInteresMercadoAnterior"
										,0 as  "INGMontoReajusteMercadoAnterior"
										,0 as  "INGDurationMercadoAnterior"
										,0 as  "INGConvexidadMercadoAnterior"
										,'0001-01-01' as "INGFechaPrecioTasaMercadoActual"
										,GES_TBADGTIA."PRECIO_VALORI" as "INGPrecioMercadoActual"
										,0 as "INGTasaMercadoActual"
										,(GES_TBCTGTIA."CAN_GARANTIA" * GES_TBADGTIA."PRECIO_VALORI") as "INGMontoATasaMercadoActual"
										,'0001-01-01' as "INGFechaPrecioTasaMercadoProximo"
										,0 as    "INGPrecioMercadoProximo"
										,0 as  "INGTasaMercadoProximo"
										,0 as  "INGMontoATasaMercadoProximo"
										,0 as "INGMontoInteresMercadoProximo"
										,0 as  "INGMontoReajusteMercadoProximo"
										,0 as  "INGDurationMercadoProximo"
										,0 as  "INGConvexidadMercadoProximo"
										,0 as  "INGTasaCorredora"
										,'0001-01-01' as "INGFechaTasaCorredora"
										,0 as "INGMontoATasaCorredora"
										,0 as  "INGMontoCorteCupon"
										,'0001-01-01' as "INGFechaCorteCupon"
										,'0001-01-01' as "EGRFechaOperacion"
										,'' as  "EGRIdTipoOperacion"
										,0 as  "EGRIdOperacion"
										,'' as  "EGRIdTipoCanalIngreso"
										,'' as "EGRIdCondicionLiquidacion"
										,'0001-01-01' as "EGRFechaLiquidacion"
										,0 as  "EGRFolioExternoReferencia"
										,p_IdModulo as "EGRIdModulo" -------------p_IdModulo
										,'' as "EGRIdConcepto"
										,0 as  "EGRIdUsuario"
										,0 as  "EGRIdSucursal"
										,0 as  "EGRIdAgente"
										,0 as "EGRCantidad"
										,0 as "EGRMonto"
										,0 as  "EGRValorRescate"
										, 0 as "EGRPrecio"
										,0 as  "EGRTasa"
										,p_IdCarga as "IdCarga" --------p_IdCarga
										,GES_TBCTGTIA."RUT_CLI" as "RUT_CLI"
										,GES_TBCTGTIA."SEC_RUT_CLI" as "SEC_RUT_CLI"
										,GES_TBCTGTIA."CONCEPTO_MVTO" as "SEC_CAR_INVER"
										,GES_TBCTGTIA."COD_PER" as "COD_PER"
										,0 as "FOLIO_COMP_ADJ"
										,'' as "TIPO_COMP_ADJ"
										,0 as  "LIN_COMP_ADJ"
										,0 as  "LIN_ORDEN"
										,0 as  "SEC_ORDEN"
										,'' as  "TIPO_CARTERA"
										,0 as  "ID_ERROR"
										,0 as  "SEC_MOVTO"
										
										
										
------------
										
										,  0 as "IdInstrumentoCarteraGarantizada"
										, '' as  "RazonSocialContraparteGarantia"
										,0 as  "CuentaContraparteGarantia"
										,  0 as "IdMovimientoCarteraEnTransito"
										,  0 as "IdConceptoOperacionOperacionOrigen"
										,  0 as "IdTipoOperacionOperacionOrigen"
										,  0 as "IdBolsaOperacionOrigen"
										,  0 as "IdMesaOperacionOrigen"
										,  0 as "IdBovedaOperacionOrigen"
										, 0 as "CuentaBovedaOperacionOrigen"
										,  0 as "IdTipoProductoOperacionOrigen"
										,  0 as "IdTipoComprobanteOperacionOrigen"
										, 0 as "FolioComprobanteOperacionOrigen"
										, '0001-01-01' as "FechaComprobanteOperacionOrigen"
										, 0 as "FolioExternoReferenciaOperacionOrigen"
										,  0 as "IdClasificacionTipoInstrumentoOperacionOrigen"
										,  0 as "IdTipoInstrumentoOperacionOrigen"
										,  0 as "IdInstrumentoOperacionOrigen"
										, '' as "NemotecnicoOperacionOrigen"
										,  0 as "IdEmisorOperacionOrigen"
										, '' as "NemoSVSOperacionOrigen"
										,  0 as "IdTipoReajusteOperacionOrigen"
										,  0 as "IdMonedaInstrumentoOperacionOrigen"
										, '0001-01-01' as "FechaEmisionInstrumentoOperacionOrigen"
										, '0001-01-01' as "FechaVencimientoInstrumentoOperacionOrigen"
										, '' as "EsFungibleOperacionOrigen"
										, 'T' as "ValorizaPorOperacionOrigen"
										,COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) as  "TipoValorizacionOperacionOrigen"
										,'' as  "CodigoIFRSOperacionOrigen"
										, 0 as "TamanoOperacionOrigen"
										, 0 as "PrecioCompraOperacionOrigen"
										, 0 as "TasaCompraOperacionOrigen"
										, 0 as "PrecioPromedioTasaCompraOperacionOrigen"
										, 'T' as "ValorizaPor"
										, COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) as "TipoValorizacion"
										, 0 as "Tamano"
										, 0 as "TIFAfectoBeneficio"
										, 0 as "TIFTasa"
										, 0 as "PrecioGarantia"
										, 0 as "TasaGarantia"
										, 0 as "MontoGarantia"
										, 0 as "MontoGarantiaMonedaInstrumento"
										,  0 as "IdSecuenciaCarteraMigracion"
										, GES_TBCTGTIA."COD_PER" as "CodigoPeriodo"
										, 0 as "INGIdCondicionLiquidacion"
										, '0001-01-01' as "INGFechaLiquidacion"
										,  '0001-01-01' as "INGFechaValorizacionGarantia"
										,  '0001-01-01' as "INGFechaValorizacionAnterior"
										,  '0001-01-01' as "INGFechaValorizacionActual"
										,  '0001-01-01' as "INGFechaValorizacionProximo"
										, 0 as "INGPrecioGarantia"
										, 0 as "INGTasaGarantia"
										, 0 as "INGMontoATasaGarantiaAnterior"
										, 0 as "INGMontoInteresGarantiaAnterior"
										, 0 as "INGMontoReajusteGarantiaAnterior"
										, 0 as "INGMontoGarantiaAnteriorMonedaInstrumento"
										, 0 as "INGMontoInteresGarantiaAnteriorMonedaInstrumento"
										, 0 as "INGMontoReajusteGarantiaAnteriorMonedaInstrumento"
										, 0 as "INGDurationGarantiaAnterior"
										, 0 as "INGConvexidadGarantiaAnterior"
										, 0 as "INGMontoGarantiaAnteriorPibMas"
										, 0 as "INGMontoGarantiaAnteriorPibMasMonedaInstrumento"
										, 0 as "INGMontoGarantiaAnteriorPibMenos"
										, 0 as "INGMontoGarantiaAnteriorPibMenosMonedaInstrumento"
										, 0 as "INGMontoATasaGarantiaActual"
										, 0 as "INGMontoGarantiaActualMonedaInstrumento"
										, 0 as "INGDurationGarantiaActual"
										, 0 as "INGConvexidadGarantiaActual"
										, 0 as "INGMontoGarantiaActualPibMas"
										, 0 as "INGMontoGarantiaActualPibMasMonedaInstrumento"
										, 0 as "INGMontoGarantiaActualPibMenos"
										, 0 as "INGMontoGarantiaActualPibMenosMonedaInstrumento"
										, 0 as "INGMontoATasaGarantiaProximo"
										, 0 as "INGMontoInteresGarantiaProximo"
										, 0 as "INGMontoReajusteGarantiaProximo"
										, 0 as "INGMontoGarantiaProximoMonedaInstrumento"
										, 0 as "INGMontoInteresGarantiaProximoMonedaInstrumento"
										, 0 as "INGMontoReajusteGarantiaProximoMonedaInstrumento"
										, 0 as "INGDurationGarantiaProximo"
										, 0 as "INGConvexidadGarantiaProximo"
										, 0 as "INGFuenteTasaPrecioMercadoAnterior"
										, 0 as "INGTablaFuenteTasaPrecioMercadoAnterior"
										, 0 as "INGMontoMercadoAnteriorMonedaInstrumento"
										, 0 as "INGMontoInteresMercadoAnteriorMonedaInstrumento"
										, 0 as "INGMontoReajusteMercadoAnteriorMonedaInstrumento"
										, 0 as "INGMontoMercadoAnteriorPibMas"
										, 0 as "INGMontoMercadoAnteriorPibMasMonedaInstrumento"
										, 0 as "INGMontoMercadoAnteriorPibMenos"
										, 0 as "INGMontoMercadoAnteriorPibMenosMonedaInstrumento"
										, 0 as "INGFuenteTasaPrecioMercadoActual"
										, 0 as "INGTablaFuenteTasaPrecioMercadoActual"
										, 0 as "INGMontoMercadoActualMonedaInstrumento"
										, 0 as "INGDurationMercadoActual"
										, 0 as "INGConvexidadMercadoActual"
										, 0 as "INGMontoMercadoActualPibMas"
										, 0 as "INGMontoMercadoActualPibMasMonedaInstrumento"
										, 0 as "INGMontoMercadoActualPibMenos"
										, 0 as "INGMontoMercadoActualPibMenosMonedaInstrumento"
										, 0 as "INGFuenteTasaPrecioMercadoProximo"
										, 0 as "INGTablaFuenteTasaPrecioMercadoProximo"
										, 0 as "INGMontoMercadoProximoMonedaInstrumento"
										, 0 as "INGMontoInteresMercadoProximoMonedaInstrumento"
										, 0 as "INGMontoReajusteMercadoProximoMonedaInstrumento"
										, 0 as "INGMontoCompraAnteriorMonedaInstrumento"
										, 0 as "INGMontoInteresCompraAnteriorMonedaInstrumento"
										, 0 as "INGMontoReajusteCompraAnteriorMonedaInstrumento"
										, 0 as "INGMontoCompraAnteriorPibMas"
										, 0 as "INGMontoCompraAnteriorPibMasMonedaInstrumento"
										, 0 as "INGMontoCompraAnteriorPibMenos"
										, 0 as "INGMontoCompraAnteriorPibMenosMonedaInstrumento"
										, 0 as "INGMontoCompraActualMonedaInstrumento"
										, 0 as "INGDurationCompraActual"
										, 0 as "INGConvexidadCompraActual"
										, 0 as "INGMontoCompraActualPibMas"
										, 0 as "INGMontoCompraActualPibMasMonedaInstrumento"
										, 0 as "INGMontoCompraActualPibMenos"
										, 0 as "INGMontoCompraActualPibMenosMonedaInstrumento"
										, 0 as "INGMontoCompraProximoMonedaInstrumento"
										, 0 as "INGMontoInteresCompraProximoMonedaInstrumento"
										, 0 as "INGMontoReajusteCompraProximoMonedaInstrumento"
										, 0 as "INGMontoATasaCorredoraMonedaInstrumento"
										, 0 as "INGNumeroCupon"
										, 0 as "INGIdMonedaPagoCupon"
										,  '0001-01-01' as "INGFechaPagoCorteCupon"
										, 0 as "INGMontoCapitalCorteCupon"
										, 0 as "INGMontoInteresCorteCupon"
										, 0 as "INGMontoReajusteCorteCupon"
										, 0 as "INGMontoCorteCuponMonedaInstrumento"
										, 0 as "INGMontoCapitalCorteCuponMonedaInstrumento"
										, 0 as "INGMontoInteresCorteCuponMonedaInstrumento"
										, 0 as "INGMontoReajusteCorteCuponMonedaInstrumento"
										, 0 as "EGRIdMovimientoCarteraEnTransito"
										, 0 as "EGRIdTipoComprobante"
										, 0 as "EGRFolioComprobante"
										, '0001-01-01' as "EGRFechaComprobante"
										, 0 as "EGRMontoMonedaInstrumento"
										
                
                FROM "BSCL_VIRTUAL"."GES_TBCTGTIA" AS GES_TBCTGTIA

			            INNER JOIN "BSCL_VIRTUAL"."GES_TBADGTIA" AS GES_TBADGTIA
						            ON TRIM(GES_TBADGTIA."RUT_CLI") = TRIM(GES_TBCTGTIA."RUT_CLI")
						            AND GES_TBADGTIA."SEC_RUT_CLI" = GES_TBCTGTIA."SEC_RUT_CLI"
						            AND TRIM(GES_TBADGTIA."NEMO") = TRIM(GES_TBCTGTIA."NEMO")
						            AND TRIM(GES_TBADGTIA."MERCADO") = TRIM(GES_TBCTGTIA."MERCADO")
						            AND TRIM(GES_TBADGTIA."CONCEPTO_MVTO") = TRIM(GES_TBCTGTIA."CONCEPTO_MVTO")
						            AND GES_TBADGTIA."VAL_MERCADO"<>0 AND GES_TBCTGTIA."CAN_GARANTIA">0
						            AND TRIM(GES_TBADGTIA."COD_PER") = TRIM(GES_TBCTGTIA."COD_PER")

		        LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
							        ON DocumentoIdentidad."DocumentoIdentidad" = TRIM((GES_TBCTGTIA."RUT_CLI"))

		        LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
		        					ON Persona."IdPersona" = DocumentoIdentidad."IdPersona"

		        LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
							        ON Cliente."IdPersona" = Persona."IdPersona"

		        LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
							        ON Portafolio."IdCliente" = Cliente."IdCliente"
							        AND Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTGTIA."SEC_RUT_CLI")

		        LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
							        ON TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTGTIA."NEMO"))

		        LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
							        ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoInstrumento."IdTipoInstrumento"))

		        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Garantia" AS Garantia
							        ON 	Garantia."COD_PER" 	 = GES_TBCTGTIA."COD_PER"
							        AND Garantia."RUT_CLI"	 = GES_TBCTGTIA."RUT_CLI"
							        AND Garantia."SEC_RUT_CLI" = GES_TBCTGTIA."SEC_RUT_CLI"
							        AND Garantia."SEC_CAR_INVER"   = GES_TBCTGTIA."CONCEPTO_MVTO"   
			    LEFT JOIN "BSCL"."BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento" AS TipoValorizacionPorTipoInstrumento
											ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoValorizacionPorTipoInstrumento."IdTipoInstrumento"))        

		        WHERE 	
				        		GES_TBCTGTIA."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
						        AND GES_TBCTGTIA."CAN_GARANTIA" > 0
						        AND GES_TBCTGTIA."MERCADO" in  ('AC','CF')
						        AND GES_TBCTGTIA."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' then  GES_TBCTGTIA."RUT_CLI" ELSE p_RutCliente END
		 						AND Garantia."RUT_CLI" IS NULL;
				                 		      


------------------------LOG SEBRA A HOMO 


				  	insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'HOMOLOG_GT_RV' as "Entidad"
                    ,'TBCTGTIA no se carga en HOMOLOGACIÒN datos ya existen o IdCarga no existe' as "Mensaje"
                    , 'COD_PER' as "NombreCampoGenerico1"
                    , GES_TBCTGTIA."COD_PER" as "ValorCampoGenerico1"
                    , 'NEMO' as "NombreCampoGenerico2"
                    , GES_TBCTGTIA."NEMO" as "ValorCampoGenerico2"
                    , 'RUT_CLI' as "NombreCampoGenerico3"
                    , GES_TBCTGTIA."RUT_CLI" as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdGarantia' as "NombreCampoGenerico6"
                    , Garantia."IdGarantia" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
           FROM "BSCL_VIRTUAL"."GES_TBCTGTIA" as  GES_TBCTGTIA

		        INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Garantia" AS Garantia
							        ON Garantia."COD_PER" = GES_TBCTGTIA."COD_PER"
							        AND Garantia."RUT_CLI" = GES_TBCTGTIA."RUT_CLI"
							        AND Garantia."SEC_RUT_CLI" = GES_TBCTGTIA."SEC_RUT_CLI"
							        AND Garantia."SEC_CAR_INVER" = GES_TBCTGTIA."CONCEPTO_MVTO" 
							        AND TRIM(Garantia."Nemotecnico") = TRIM(GES_TBCTGTIA."NEMO")
		       WHERE 
							        GES_TBCTGTIA."COD_PER" =  SUBSTRING(p_Periodo_Inicial,1,7) 					
							        AND GES_TBCTGTIA."COD_PER" NOT LIKE '%90%'
							        AND GES_TBCTGTIA."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then GES_TBCTGTIA."RUT_CLI" ELSE p_RutCliente END
							        AND GES_TBCTGTIA."CAN_GARANTIA" > 0
							        AND GES_TBCTGTIA."MERCADO" in  ('AC','CF')
							  AND Garantia."IdCarga"<>p_IdCarga;
							  
							  
							  
							  
							  

				  	insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'HOMOLOG_GT_RV' as "Entidad"
                    ,'TBCTGTIA no se carga en HOMOLOGACIÒN GES_TBADGTIA."VAL_MERCADO"=0' as "Mensaje"
                    , 'COD_PER' as "NombreCampoGenerico1"
                    , GES_TBCTGTIA."COD_PER" as "ValorCampoGenerico1"
                    , 'NEMO' as "NombreCampoGenerico2"
                    , GES_TBCTGTIA."NEMO" as "ValorCampoGenerico2"
                    , 'RUT_CLI' as "NombreCampoGenerico3"
                    , GES_TBCTGTIA."RUT_CLI" as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
           FROM "BSCL_VIRTUAL"."GES_TBCTGTIA" AS GES_TBCTGTIA

			            INNER JOIN "BSCL_VIRTUAL"."GES_TBADGTIA" AS GES_TBADGTIA
						            ON TRIM(GES_TBADGTIA."RUT_CLI") = TRIM(GES_TBCTGTIA."RUT_CLI")
						            AND GES_TBADGTIA."SEC_RUT_CLI" = GES_TBCTGTIA."SEC_RUT_CLI"
						            AND TRIM(GES_TBADGTIA."NEMO") = TRIM(GES_TBCTGTIA."NEMO")
						            AND TRIM(GES_TBADGTIA."MERCADO") = TRIM(GES_TBCTGTIA."MERCADO")
						            AND TRIM(GES_TBADGTIA."CONCEPTO_MVTO") = TRIM(GES_TBCTGTIA."CONCEPTO_MVTO")
						            AND GES_TBADGTIA."VAL_MERCADO"=0 AND GES_TBCTGTIA."CAN_GARANTIA">0
						            AND TRIM(GES_TBADGTIA."COD_PER") = TRIM(GES_TBCTGTIA."COD_PER")
		       WHERE 
							        GES_TBCTGTIA."COD_PER" =  SUBSTRING(p_Periodo_Inicial,1,7) 					
							        AND GES_TBCTGTIA."COD_PER" NOT LIKE '%90%'
							        AND GES_TBCTGTIA."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then GES_TBCTGTIA."RUT_CLI" ELSE p_RutCliente END
							        AND GES_TBCTGTIA."CAN_GARANTIA" > 0
							        AND GES_TBCTGTIA."MERCADO" in  ('AC','CF');
		
-------------------- FIN -------------------LOG SEBRA A HOMO 

		   		               						SIGNAL SALIDA_CONTROLADA;         
	END;
