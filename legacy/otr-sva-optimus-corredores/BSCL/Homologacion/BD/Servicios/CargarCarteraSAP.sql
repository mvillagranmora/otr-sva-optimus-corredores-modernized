SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraSAP";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraSAP"
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
		--OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalPortafoliosCuentaACargar" DECIMAL(26,8),"TotalPortafoliosConCuentaInversion" DECIMAL(26,8)),
		OUT p_out_ErroresContador int,
		OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
    ) 
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS   
	BEGIN
		-- VARIABLES VALIDACIONES BASICAS
		DECLARE v_out_Resultado  		CHAR(1); 	   --RESULTADO S/N/E
	    DECLARE v_out_Mensaje	 		NVARCHAR(520); --MENSAJE SALIDA
	    
	    DECLARE v_erroroutmsg  			nvarchar(520);    
	    DECLARE v_TotalPortafolioLeidos	BIGINT;	    
	    DECLARE v_TotalCuentasGrabadas	BIGINT;
	    
	    -- Para LOg
	    DECLARE v_out_refID				BIGINT;    
	    DECLARE v_out_ErroresContador	BIGINT; 
	    DECLARE v_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));	
	    DECLARE v_FechaSistema NVARCHAR(40);
	    DECLARE v_MaxPeriodo NVARCHAR(40);
	    DECLARE v_FechaInicioPeriodo NVARCHAR(10);
	    DECLARE v_periodo  DATE := CONCAT(SUBSTRING(p_FechaInicial,1,7),'-01');
        DECLARE v_periodofinal DATE := CONCAT(SUBSTRING(p_FechaFinal,1,7),'-01'); 
	    DECLARE v_RutCli NVARCHAR(11):='0-0';
	    DECLARE v_MonedaSistema NVARCHAR(10);
	        
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
        
				
				SELECT LPAD(p_RutCliente, 11,' ') into v_RutCli FROM DUMMY;
				
				SELECT COALESCE("Valor",'') into v_MonedaSistema from "BSCL.Plataforma.BD::PLA.Configuracion"
        		WHERE  "IdEmpresa" = p_IdEmpresa
        		AND  "CodigoParametro" = 'PLA.MonedaSistema';


                -----------------------------------------------------------
                -- INSERTAMOS EN TABLAS DEL MODELO DE SAP ----------------
                -----------------------------------------------------------            
		        insert into "BSCL.Custodia.BD::CUS.Cartera"
					("IdCartera","FolioCartera","FolioCarteraRelacionada","FechaIngresoRegistro","FechaModificacionRegistro","IdUsuarioReserva","FechaReserva"                         
					,"IdEmpresa","IdUsuario","IdModulo","IdCliente","IdPortafolio","IdEstado","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","CodigoIFRS"	
					,"IdMovimientoCarteraTransito","IdConcepto","IdTipoOperacion","IdOperacion","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion"
					,"IdComprobanteFactReferencia","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion"
					,"EsIntraday","EsNominal","UsaGarantiaEspecifica","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdClasificacionTipoInstrumento","IdTipoInstrumento"
					,"IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible"	
					,"IdBoveda","IdMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra","MontoCompraEnMonedaInstrumento"		
					,"TasaAgente","MontoAPlazo","PrecioAPlazo","INGGlosa","INGFechaOperacion","INGCantidad","INGValorRescate","INGTasaAgente","INGMontoAPlazo"
					,"INGPrecioAPlazo","INGFechaPrecioTasaCompraAnterior","INGPrecioCompraAnterior","INGTasaCompraAnterior","INGMontoATasaCompraAnterior"		
					,"INGMontoInteresCompraAnterior","INGMontoReajusteCompraAnterior","INGDurationCompraAnterior","INGConvexidadCompraAnterior","INGFechaPrecioTasaCompraActual"		
					,"INGPrecioCompraActual","INGTasaCompraActual","INGMontoATasaCompraActual","INGFechaPrecioTasaCompraProximo","INGPrecioCompraProximo","INGTasaCompraProximo"
					,"INGMontoATasaCompraProximo","INGMontoInteresCompraProximo","INGMontoReajusteCompraProximo","INGDurationCompraProximo","INGConvexidadCompraProximo"		  
					,"INGFechaPrecioTasaMercadoAnterior","INGPrecioMercadoAnterior","INGTasaMercadoAnterior","INGIdOrigenTasaPrecioMercadoAnterior","INGMontoATasaMercadoAnterior"	
					,"INGMontoInteresMercadoAnterior","INGMontoReajusteMercadoAnterior","INGDurationMercadoAnterior","INGConvexidadMercadoAnterior","INGFechaPrecioTasaMercadoActual"		
					,"INGIdOrigenTasaPrecioMercadoActual","INGPrecioMercadoActual","INGTasaMercadoActual","INGMontoATasaMercadoActual","INGFechaPrecioTasaMercadoProximo"		
					,"INGPrecioMercadoProximo","INGTasaMercadoProximo","INGIdOrigenTasaPrecioMercadoProximo","INGMontoATasaMercadoProximo","INGMontoInteresMercadoProximo"	
					,"INGMontoReajusteMercadoProximo","INGDurationMercadoProximo","INGConvexidadMercadoProximo","INGFechaPrecioTasaDescuentoAnterior","INGPrecioDescuentoAnterior"
					,"INGTasaDescuentoAnterior","INGMontoATasaDescuentoAnterior","INGMontoInteresDescuentoAnterior","INGMontoReajusteDescuentoAnterior"	
					,"INGDurationDescuentoAnterior","INGConvexidadDescuentoAnterior","INGFechaPrecioTasaDescuentoActual","INGPrecioDescuentoActual"
					,"INGTasaDescuentoActual","INGMontoATasaDescuentoActual","INGFechaPrecioTasaDescuentoProximo","INGPrecioDescuentoProximo","INGTasaDescuentoProximo"
					,"INGMontoATasaDescuentoProximo","INGMontoInteresDescuentoProximo","INGMontoReajusteDescuentoProximo","INGDurationDescuentoProximo","INGConvexidadDescuentoProximo"
					,"INGPrecioPactoAnterior","INGTasaPactoAnterior","INGMontoATasaPactoAnterior","INGMontoInteresPactoAnterior","INGMontoReajustePactoAnterior"		
					,"INGDurationPactoAnterior","INGConvexidadPactoAnterior","INGFechaPrecioTasaPactoActual","INGPrecioPactoActual","INGTasaPactoActual"
					,"INGMontoATasaPactoActual","INGPrecioPactoProximo","INGTasaPactoProximo","INGMontoATasaPactoProximo","INGMontoInteresPactoProximo"		
					,"INGMontoReajustePactoProximo","INGDurationPactoProximo","INGConvexidadPactoProximo","INGTasaCorredora","INGFechaTasaCorredora"
					,"INGMontoATasaCorredora","INGMontoCorteCupon","INGFechaCorteCupon","INGMontoCorteCuponProximo","EGRGlosa","EGRFechaOperacion"
					,"EGRIdTipoOperacion","EGRIdOperacion","EGRIdMovimientoCarteraTransito","EGRIdTipoCanalIngreso","EGRIdCondicionLiquidacion","EGRFechaLiquidacion"
					,"EGRIdComprobanteFactReferencia","EGRFolioExternoReferencia","EGRIdModulo","EGRIdConcepto","EGRIdUsuario","EGRIdSucursal","EGRIdAgente"	
					,"EGRCantidad","EGRMonto","EGRMontoEnMonedaInstrumento","EGRValorRescate","EGRPrecio","EGRTasa","EGRSpreadCompra","EGRSpreadMercado"	
					,"EGRMontoAPlazo","EGRPrecioAPlazo"
				  )
			select 
				 A1."IdCartera",A1."FolioCartera",A1."FolioCarteraRelacionada",A1."FechaIngresoRegistro",A1."FechaModificacionRegistro",A1."IdUsuarioReserva",A1."FechaReserva"                         
				,A1."IdEmpresa",A1."IdUsuario",A1."IdModulo",A1."IdCliente",A1."IdPortafolio",A1."IdEstado",A1."IdTipoProducto",A1."IdTipoCanalIngreso",A1."IdMonedaSistema",A1."CodigoIFRS"	
				,A1."IdMovimientoCarteraTransito",A1."IdConcepto",A1."IdTipoOperacion",A1."IdOperacion",A1."FechaOperacion",A1."FechaVencimientoOperacion",A1."PlazoEnDiasOperacion"
				,A1."IdComprobanteFactReferencia",A1."FolioExternoReferencia",A1."IdMesa",A1."IdBolsa",A1."IdSucursal",A1."IdAgente",A1."IdCondicionLiquidacion",A1."FechaLiquidacion"
				,A1."EsIntraday",A1."EsNominal",A1."UsaGarantiaEspecifica",A1."CobraPrimaAlInicio",A1."IdTipoPrestamo",A1."Glosa",A1."IdClasificacionTipoInstrumento",A1."IdTipoInstrumento"
				,A1."IdInstrumento",A1."Nemotecnico",A1."IdEmisor",A1."NemoSVS",A1."IdTipoReajuste",A1."FechaEmisionInstrumento",A1."FechaVencimientoInstrumento",A1."EsFungible"	
				,A1."IdBoveda",A1."IdMonedaInstrumento",A1."Cantidad",A1."CorteMinimo",A1."ValorRescate",A1."PrecioCompra",A1."TasaCompra",A1."MontoCompra",A1."MontoCompraEnMonedaInstrumento"		
				,A1."TasaAgente",A1."MontoAPlazo",A1."PrecioAPlazo",A1."INGGlosa",A1."INGFechaOperacion",A1."INGCantidad",A1."INGValorRescate",A1."INGTasaAgente",A1."INGMontoAPlazo"
				,A1."INGPrecioAPlazo",A1."INGFechaPrecioTasaCompraAnterior",A1."INGPrecioCompraAnterior",A1."INGTasaCompraAnterior",A1."INGMontoATasaCompraAnterior"		
				,A1."INGMontoInteresCompraAnterior",A1."INGMontoReajusteCompraAnterior",A1."INGDurationCompraAnterior",A1."INGConvexidadCompraAnterior",A1."INGFechaPrecioTasaCompraActual"		
				,A1."INGPrecioCompraActual",A1."INGTasaCompraActual",A1."INGMontoATasaCompraActual",A1."INGFechaPrecioTasaCompraProximo",A1."INGPrecioCompraProximo",A1."INGTasaCompraProximo"
				,A1."INGMontoATasaCompraProximo",A1."INGMontoInteresCompraProximo",A1."INGMontoReajusteCompraProximo",A1."INGDurationCompraProximo",A1."INGConvexidadCompraProximo"		  
				,A1."INGFechaPrecioTasaMercadoAnterior",A1."INGPrecioMercadoAnterior",A1."INGTasaMercadoAnterior",A1."INGIdOrigenTasaPrecioMercadoAnterior",A1."INGMontoATasaMercadoAnterior"	
				,A1."INGMontoInteresMercadoAnterior",A1."INGMontoReajusteMercadoAnterior",A1."INGDurationMercadoAnterior",A1."INGConvexidadMercadoAnterior",A1."INGFechaPrecioTasaMercadoActual"		
				,A1."INGIdOrigenTasaPrecioMercadoActual",A1."INGPrecioMercadoActual",A1."INGTasaMercadoActual",A1."INGMontoATasaMercadoActual",A1."INGFechaPrecioTasaMercadoProximo"		
				,A1."INGPrecioMercadoProximo",A1."INGTasaMercadoProximo",A1."INGIdOrigenTasaPrecioMercadoProximo",A1."INGMontoATasaMercadoProximo",A1."INGMontoInteresMercadoProximo"	
				,A1."INGMontoReajusteMercadoProximo",A1."INGDurationMercadoProximo",A1."INGConvexidadMercadoProximo",A1."INGFechaPrecioTasaDescuentoAnterior",A1."INGPrecioDescuentoAnterior"
				,A1."INGTasaDescuentoAnterior",A1."INGMontoATasaDescuentoAnterior",A1."INGMontoInteresDescuentoAnterior",A1."INGMontoReajusteDescuentoAnterior"	
				,A1."INGDurationDescuentoAnterior",A1."INGConvexidadDescuentoAnterior",A1."INGFechaPrecioTasaDescuentoActual",A1."INGPrecioDescuentoActual"
				,A1."INGTasaDescuentoActual",A1."INGMontoATasaDescuentoActual",A1."INGFechaPrecioTasaDescuentoProximo",A1."INGPrecioDescuentoProximo",A1."INGTasaDescuentoProximo"
				,A1."INGMontoATasaDescuentoProximo",A1."INGMontoInteresDescuentoProximo",A1."INGMontoReajusteDescuentoProximo",A1."INGDurationDescuentoProximo",A1."INGConvexidadDescuentoProximo"
				,A1."INGPrecioPactoAnterior",A1."INGTasaPactoAnterior",A1."INGMontoATasaPactoAnterior",A1."INGMontoInteresPactoAnterior",A1."INGMontoReajustePactoAnterior"		
				,A1."INGDurationPactoAnterior",A1."INGConvexidadPactoAnterior",A1."INGFechaPrecioTasaPactoActual",A1."INGPrecioPactoActual",A1."INGTasaPactoActual"
				,A1."INGMontoATasaPactoActual",A1."INGPrecioPactoProximo",A1."INGTasaPactoProximo",A1."INGMontoATasaPactoProximo",A1."INGMontoInteresPactoProximo"		
				,A1."INGMontoReajustePactoProximo",A1."INGDurationPactoProximo",A1."INGConvexidadPactoProximo",A1."INGTasaCorredora",A1."INGFechaTasaCorredora"
				,A1."INGMontoATasaCorredora",A1."INGMontoCorteCupon",A1."INGFechaCorteCupon",A1."INGMontoCorteCuponProximo",A1."EGRGlosa",A1."EGRFechaOperacion"
				,A1."EGRIdTipoOperacion",A1."EGRIdOperacion",A1."EGRIdMovimientoCarteraTransito",A1."EGRIdTipoCanalIngreso",A1."EGRIdCondicionLiquidacion",A1."EGRFechaLiquidacion"
				,A1."EGRIdComprobanteFactReferencia",A1."EGRFolioExternoReferencia",A1."EGRIdModulo",A1."EGRIdConcepto",A1."EGRIdUsuario",A1."EGRIdSucursal",A1."EGRIdAgente"	
				,A1."EGRCantidad",A1."EGRMonto",A1."EGRMontoEnMonedaInstrumento",A1."EGRValorRescate",A1."EGRPrecio",A1."EGRTasa",A1."EGRSpreadCompra",A1."EGRSpreadMercado"	
				,A1."EGRMontoAPlazo",A1."EGRPrecioAPlazo"
				FROM "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS A1
				LEFT JOIN "BSCL"."BSCL.Custodia.BD::CUS.Cartera" AS A2
				ON A2."IdCartera" =  A1."IdCartera"
				WHERE A2."IdCartera" IS NULL;
 
                -----------------------------------------------------------------------------
                -- insertamos en la cartera en garantia en el modelo de sap -----------------
                -----------------------------------------------------------------------------
              
                insert into "BSCL.Custodia.BD::GTA.Garantia"
                (
					"IdGarantia","FolioGarantia","FolioCarteraOrigen","FolioCarteraGarantizada","EsEspecifica","IdInstrumentoGarantizado","IdContraparteGarantia","IdOrigenGarantia"
					,"IdEspecieGarantia","FechaIngresoRegistro","FechaModificacionRegistro","IdEmpresa","IdUsuario","IdModulo","IdCliente","IdPortafolio","IdEstado","IdTipoProducto"
					,"IdTipoCanalIngreso","IdMonedaSistema","CodigoIFRS","IdConcepto","IdTipoOperacion","IdOperacion","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion"
					,"IdComprobanteFactReferencia","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion"
					,"EsNominal","Glosa","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste"
					,"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdBoveda","IdMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate"
					,"PrecioCompra","TasaCompra","MontoCompra","MontoCompraEnMonedaInstrumento","TasaAgente","MontoAPlazo","PrecioAPlazo","INGGlosa","INGFechaOperacion"
					,"INGCantidad","INGValorRescate","INGTasaAgente","INGMontoAPlazo","INGPrecioAPlazo","INGFechaPrecioTasaCompraAnterior","INGPrecioCompraAnterior"
					,"INGTasaCompraAnterior","INGMontoATasaCompraAnterior","INGMontoInteresCompraAnterior","INGMontoReajusteCompraAnterior","INGDurationCompraAnterior"
					,"INGConvexidadCompraAnterior","INGFechaPrecioTasaCompraActual","INGPrecioCompraActual","INGTasaCompraActual","INGMontoATasaCompraActual","INGFechaPrecioTasaCompraProximo"
					,"INGPrecioCompraProximo","INGTasaCompraProximo","INGMontoATasaCompraProximo","INGMontoInteresCompraProximo","INGMontoReajusteCompraProximo","INGDurationCompraProximo"
					,"INGConvexidadCompraProximo","INGFechaPrecioTasaMercadoAnterior","INGPrecioMercadoAnterior","INGTasaMercadoAnterior","INGIdOrigenTasaPrecioMercadoAnterior","INGMontoATasaMercadoAnterior"
					,"INGMontoInteresMercadoAnterior","INGMontoReajusteMercadoAnterior","INGDurationMercadoAnterior","INGConvexidadMercadoAnterior","INGFechaPrecioTasaMercadoActual","INGIdOrigenTasaPrecioMercadoActual"
					,"INGPrecioMercadoActual","INGTasaMercadoActual","INGMontoATasaMercadoActual","INGFechaPrecioTasaMercadoProximo","INGPrecioMercadoProximo","INGTasaMercadoProximo"
					,"INGIdOrigenTasaPrecioMercadoProximo","INGMontoATasaMercadoProximo","INGMontoInteresMercadoProximo","INGMontoReajusteMercadoProximo","INGDurationMercadoProximo"
					,"INGConvexidadMercadoProximo","INGFechaPrecioTasaDescuentoAnterior","INGPrecioDescuentoAnterior","INGTasaDescuentoAnterior","INGMontoATasaDescuentoAnterior","INGMontoInteresDescuentoAnterior"
					,"INGMontoReajusteDescuentoAnterior","INGDurationDescuentoAnterior","INGConvexidadDescuentoAnterior","INGFechaPrecioTasaDescuentoActual","INGPrecioDescuentoActual"
					,"INGTasaDescuentoActual","INGMontoATasaDescuentoActual","INGFechaPrecioTasaDescuentoProximo","INGPrecioDescuentoProximo","INGTasaDescuentoProximo"
					,"INGMontoATasaDescuentoProximo","INGMontoInteresDescuentoProximo","INGMontoReajusteDescuentoProximo","INGDurationDescuentoProximo","INGConvexidadDescuentoProximo"
					,"INGPrecioPactoAnterior","INGTasaPactoAnterior","INGMontoATasaPactoAnterior","INGMontoInteresPactoAnterior","INGMontoReajustePactoAnterior","INGDurationPactoAnterior"
					,"INGConvexidadPactoAnterior","INGFechaPrecioTasaPactoActual","INGPrecioPactoActual","INGTasaPactoActual","INGMontoATasaPactoActual","INGPrecioPactoProximo"
					,"INGTasaPactoProximo","INGMontoATasaPactoProximo","INGMontoInteresPactoProximo","INGMontoReajustePactoProximo","INGDurationPactoProximo","INGConvexidadPactoProximo"
					,"INGTasaCorredora","INGFechaTasaCorredora","INGMontoATasaCorredora","INGMontoCorteCupon","INGFechaCorteCupon","INGMontoCorteCuponProximo","EGRGlosa"
					,"EGRFechaOperacion","EGRIdTipoOperacion","EGRIdOperacion","EGRIdMovimientoCarteraTransito","EGRIdTipoCanalIngreso","EGRIdCondicionLiquidacion","EGRFechaLiquidacion"
					,"EGRIdComprobanteFactReferencia","EGRFolioExternoReferencia","EGRIdModulo","EGRIdConcepto","EGRIdUsuario","EGRIdSucursal","EGRIdAgente","EGRCantidad"
					,"EGRMonto","EGRMontoEnMonedaInstrumento","EGRValorRescate","EGRPrecio","EGRTasa","EGRSpreadCompra","EGRSpreadMercado","EGRMontoAPlazo","EGRPrecioAPlazo")
				
			     select	A1."IdGarantia",A1."FolioGarantia",A1."FolioCarteraOrigen",A1."FolioCarteraGarantizada",A1."EsEspecifica",A1."IdInstrumentoGarantizado",A1."IdContraparteGarantia",A1."IdOrigenGarantia"
					,A1."IdEspecieGarantia",A1."FechaIngresoRegistro",A1."FechaModificacionRegistro",A1."IdEmpresa",A1."IdUsuario",A1."IdModulo",A1."IdCliente",A1."IdPortafolio",A1."IdEstado",A1."IdTipoProducto"
					,A1."IdTipoCanalIngreso",A1."IdMonedaSistema",A1."CodigoIFRS",A1."IdConcepto",A1."IdTipoOperacion",A1."IdOperacion",A1."FechaOperacion",A1."FechaVencimientoOperacion",A1."PlazoEnDiasOperacion"
					,A1."IdComprobanteFactReferencia",A1."FolioExternoReferencia",A1."IdMesa",A1."IdBolsa",A1."IdSucursal",A1."IdAgente",A1."IdCondicionLiquidacion",A1."FechaLiquidacion"
					,A1."EsNominal",A1."Glosa",A1."IdClasificacionTipoInstrumento",A1."IdTipoInstrumento",A1."IdInstrumento",A1."Nemotecnico",A1."IdEmisor",A1."NemoSVS",A1."IdTipoReajuste"
					,A1."FechaEmisionInstrumento",A1."FechaVencimientoInstrumento",A1."EsFungible",A1."IdBoveda",A1."IdMonedaInstrumento",A1."Cantidad",A1."CorteMinimo",A1."ValorRescate"
					,A1."PrecioCompra",A1."TasaCompra",A1."MontoCompra",A1."MontoCompraEnMonedaInstrumento",A1."TasaAgente",A1."MontoAPlazo",A1."PrecioAPlazo",A1."INGGlosa",A1."INGFechaOperacion"
					,A1."INGCantidad",A1."INGValorRescate",A1."INGTasaAgente",A1."INGMontoAPlazo",A1."INGPrecioAPlazo",A1."INGFechaPrecioTasaCompraAnterior",A1."INGPrecioCompraAnterior"
					,A1."INGTasaCompraAnterior",A1."INGMontoATasaCompraAnterior",A1."INGMontoInteresCompraAnterior",A1."INGMontoReajusteCompraAnterior",A1."INGDurationCompraAnterior"
					,A1."INGConvexidadCompraAnterior",A1."INGFechaPrecioTasaCompraActual",A1."INGPrecioCompraActual",A1."INGTasaCompraActual",A1."INGMontoATasaCompraActual",A1."INGFechaPrecioTasaCompraProximo"
					,A1."INGPrecioCompraProximo",A1."INGTasaCompraProximo",A1."INGMontoATasaCompraProximo",A1."INGMontoInteresCompraProximo",A1."INGMontoReajusteCompraProximo",A1."INGDurationCompraProximo"
					,A1."INGConvexidadCompraProximo",A1."INGFechaPrecioTasaMercadoAnterior",A1."INGPrecioMercadoAnterior",A1."INGTasaMercadoAnterior",A1."INGIdOrigenTasaPrecioMercadoAnterior",A1."INGMontoATasaMercadoAnterior"
					,A1."INGMontoInteresMercadoAnterior",A1."INGMontoReajusteMercadoAnterior",A1."INGDurationMercadoAnterior",A1."INGConvexidadMercadoAnterior",A1."INGFechaPrecioTasaMercadoActual",A1."INGIdOrigenTasaPrecioMercadoActual"
					,A1."INGPrecioMercadoActual",A1."INGTasaMercadoActual",A1."INGMontoATasaMercadoActual",A1."INGFechaPrecioTasaMercadoProximo",A1."INGPrecioMercadoProximo",A1."INGTasaMercadoProximo"
					,A1."INGIdOrigenTasaPrecioMercadoProximo",A1."INGMontoATasaMercadoProximo",A1."INGMontoInteresMercadoProximo",A1."INGMontoReajusteMercadoProximo",A1."INGDurationMercadoProximo"
					,A1."INGConvexidadMercadoProximo",A1."INGFechaPrecioTasaDescuentoAnterior",A1."INGPrecioDescuentoAnterior",A1."INGTasaDescuentoAnterior",A1."INGMontoATasaDescuentoAnterior",A1."INGMontoInteresDescuentoAnterior"
					,A1."INGMontoReajusteDescuentoAnterior",A1."INGDurationDescuentoAnterior",A1."INGConvexidadDescuentoAnterior",A1."INGFechaPrecioTasaDescuentoActual",A1."INGPrecioDescuentoActual"
					,A1."INGTasaDescuentoActual",A1."INGMontoATasaDescuentoActual",A1."INGFechaPrecioTasaDescuentoProximo",A1."INGPrecioDescuentoProximo",A1."INGTasaDescuentoProximo"
					,A1."INGMontoATasaDescuentoProximo",A1."INGMontoInteresDescuentoProximo",A1."INGMontoReajusteDescuentoProximo",A1."INGDurationDescuentoProximo",A1."INGConvexidadDescuentoProximo"
					,A1."INGPrecioPactoAnterior",A1."INGTasaPactoAnterior",A1."INGMontoATasaPactoAnterior",A1."INGMontoInteresPactoAnterior",A1."INGMontoReajustePactoAnterior",A1."INGDurationPactoAnterior"
					,A1."INGConvexidadPactoAnterior",A1."INGFechaPrecioTasaPactoActual",A1."INGPrecioPactoActual",A1."INGTasaPactoActual",A1."INGMontoATasaPactoActual",A1."INGPrecioPactoProximo"
					,A1."INGTasaPactoProximo",A1."INGMontoATasaPactoProximo",A1."INGMontoInteresPactoProximo",A1."INGMontoReajustePactoProximo",A1."INGDurationPactoProximo",A1."INGConvexidadPactoProximo"
					,A1."INGTasaCorredora",A1."INGFechaTasaCorredora",A1."INGMontoATasaCorredora",A1."INGMontoCorteCupon",A1."INGFechaCorteCupon",A1."INGMontoCorteCuponProximo",A1."EGRGlosa"
					,A1."EGRFechaOperacion",A1."EGRIdTipoOperacion",A1."EGRIdOperacion",A1."EGRIdMovimientoCarteraTransito",A1."EGRIdTipoCanalIngreso",A1."EGRIdCondicionLiquidacion",A1."EGRFechaLiquidacion"
					,A1."EGRIdComprobanteFactReferencia",A1."EGRFolioExternoReferencia",A1."EGRIdModulo",A1."EGRIdConcepto",A1."EGRIdUsuario",A1."EGRIdSucursal",A1."EGRIdAgente",A1."EGRCantidad"
					,A1."EGRMonto",A1."EGRMontoEnMonedaInstrumento",A1."EGRValorRescate",A1."EGRPrecio",A1."EGRTasa",A1."EGRSpreadCompra",A1."EGRSpreadMercado",A1."EGRMontoAPlazo",A1."EGRPrecioAPlazo"
				FROM "BSCL"."BSCL.Homologacion.BD::HOM.Garantia" AS A1
				LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS A2
				ON A1."IdGarantia" = A2."IdGarantia"
				WHERE A2."IdGarantia" IS NULL;
				
				-------------------------------------------------------------------------
                -- cargamos cartera subyacente modelo sap                        --------
                -------------------------------------------------------------------------
				insert into "BSCL.Custodia.BD::CUS.Subyacente"(
				"IdSubyacente","FolioSubyacente","FolioCarteraRelacionada","FechaIngresoRegistro" ,"FechaModificacionRegistro","IdEmpresa"
				,"IdUsuario","IdModulo","IdCliente","IdPortafolio","IdEstado","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdConcepto"
				,"IdTipoOperacion","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","IdBolsa","IdMesa","CodigoIFRS","EsNominal"
				,"Glosa","IdCondicionLiquidacion" ,"FechaLiquidacion","IdClasificacionTipoInstrumento" ,"IdTipoInstrumento","IdInstrumento","Nemotecnico"
				,"IdEmisor","NemoSVS","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdBoveda","IdMonedaInstrumento"
				,"Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra","MontoCompraEnMonedaInstrumento" ,"TasaAgente","MontoAPlazo"
				,"PrecioAPlazo","INGGlosa","INGFechaOperacion","INGCantidad","INGValorRescate","INGTasaAgente","INGMontoAPlazo","INGPrecioAPlazo"
				,"INGFechaPrecioTasaCompraAnterior","INGPrecioCompraAnterior","INGTasaCompraAnterior" ,"INGMontoATasaCompraAnterior","INGMontoInteresCompraAnterior" 
				,"INGMontoReajusteCompraAnterior","INGDurationCompraAnterior","INGConvexidadCompraAnterior","INGFechaPrecioTasaCompraActual","INGPrecioCompraActual"
				,"INGTasaCompraActual","INGMontoATasaCompraActual","INGFechaPrecioTasaCompraProximo","INGPrecioCompraProximo","INGTasaCompraProximo","INGMontoATasaCompraProximo"
				,"INGMontoInteresCompraProximo","INGMontoReajusteCompraProximo","INGDurationCompraProximo","INGConvexidadCompraProximo","INGFechaPrecioTasaMercadoAnterior"
				,"INGPrecioMercadoAnterior","INGTasaMercadoAnterior" ,"INGIdOrigenTasaPrecioMercadoAnterior" ,"INGMontoATasaMercadoAnterior" ,"INGMontoInteresMercadoAnterior" 
				,"INGMontoReajusteMercadoAnterior","INGDurationMercadoAnterior","INGConvexidadMercadoAnterior" ,"INGFechaPrecioTasaMercadoActual","INGIdOrigenTasaPrecioMercadoActual"
				,"INGPrecioMercadoActual","INGTasaMercadoActual" ,"INGMontoATasaMercadoActual","INGFechaPrecioTasaMercadoProximo","INGPrecioMercadoProximo","INGTasaMercadoProximo" 
				,"INGIdOrigenTasaPrecioMercadoProximo","INGMontoATasaMercadoProximo","INGMontoInteresMercadoProximo" ,"INGMontoReajusteMercadoProximo","INGDurationMercadoProximo"
				,"INGConvexidadMercadoProximo","INGFechaPrecioTasaDescuentoAnterior","INGPrecioDescuentoAnterior","INGTasaDescuentoAnterior","INGMontoATasaDescuentoAnterior" 
				,"INGMontoInteresDescuentoAnterior","INGMontoReajusteDescuentoAnterior","INGDurationDescuentoAnterior" ,"INGConvexidadDescuentoAnterior" 
				,"INGFechaPrecioTasaDescuentoActual","INGPrecioDescuentoActual","INGTasaDescuentoActual" ,"INGMontoATasaDescuentoActual" ,"INGFechaPrecioTasaDescuentoProximo"
				,"INGPrecioDescuentoProximo","INGTasaDescuentoProximo" ,"INGMontoATasaDescuentoProximo" ,"INGMontoInteresDescuentoProximo" ,"INGMontoReajusteDescuentoProximo"
				,"INGDurationDescuentoProximo","INGConvexidadDescuentoProximo" ,"INGPrecioPactoAnterior","INGTasaPactoAnterior" ,"INGMontoATasaPactoAnterior"
				,"INGMontoInteresPactoAnterior","INGMontoReajustePactoAnterior","INGDurationPactoAnterior","INGConvexidadPactoAnterior","INGFechaPrecioTasaPactoActual"
				,"INGPrecioPactoActual","INGTasaPactoActual","INGMontoATasaPactoActual","INGPrecioPactoProximo","INGTasaPactoProximo","INGMontoATasaPactoProximo"
				,"INGMontoInteresPactoProximo","INGMontoReajustePactoProximo","INGDurationPactoProximo","INGConvexidadPactoProximo","INGTasaCorredora","INGFechaTasaCorredora" 
				,"INGMontoATasaCorredora","INGMontoCorteCupon","INGFechaCorteCupon","INGMontoCorteCuponProximo","EGRGlosa","EGRFechaOperacion","EGRIdTipoOperacion"
				,"EGRIdOperacion","EGRIdMovimientoCarteraTransito","EGRIdTipoCanalIngreso","EGRIdCondicionLiquidacion","EGRFechaLiquidacion","EGRIdComprobanteFactReferencia"
				,"EGRFolioExternoReferencia","EGRIdModulo","EGRIdConcepto","EGRIdUsuario","EGRIdSucursal","EGRIdAgente","EGRCantidad","EGRMonto","EGRMontoEnMonedaInstrumento"
				,"EGRValorRescate","EGRPrecio","EGRTasa","EGRSpreadCompra","EGRSpreadMercado","EGRMontoAPlazo","EGRPrecioAPlazo"
			    )
				select
				 A1."IdSubyacente",A1."FolioSubyacente",A1."FolioCarteraRelacionada",A1."FechaIngresoRegistro" ,A1."FechaModificacionRegistro",A1."IdEmpresa"
				,A1."IdUsuario",A1."IdModulo",A1."IdCliente",A1."IdPortafolio",A1."IdEstado",A1."IdTipoProducto",A1."IdTipoCanalIngreso",A1."IdMonedaSistema",A1."IdConcepto"
				,A1."IdTipoOperacion",A1."FechaOperacion",A1."FechaVencimientoOperacion",A1."PlazoEnDiasOperacion",A1."IdBolsa",A1."IdMesa",A1."CodigoIFRS",A1."EsNominal"
				,A1."Glosa",A1."IdCondicionLiquidacion" ,A1."FechaLiquidacion",A1."IdClasificacionTipoInstrumento" ,A1."IdTipoInstrumento",A1."IdInstrumento",A1."Nemotecnico"
				,A1."IdEmisor",A1."NemoSVS",A1."IdTipoReajuste",A1."FechaEmisionInstrumento",A1."FechaVencimientoInstrumento",A1."EsFungible",A1."IdBoveda",A1."IdMonedaInstrumento"
				,A1."Cantidad",A1."CorteMinimo",A1."ValorRescate",A1."PrecioCompra",A1."TasaCompra",A1."MontoCompra",A1."MontoCompraEnMonedaInstrumento" ,A1."TasaAgente",A1."MontoAPlazo"
				,A1."PrecioAPlazo",A1."INGGlosa",A1."INGFechaOperacion",A1."INGCantidad",A1."INGValorRescate",A1."INGTasaAgente",A1."INGMontoAPlazo",A1."INGPrecioAPlazo"
				,A1."INGFechaPrecioTasaCompraAnterior",A1."INGPrecioCompraAnterior",A1."INGTasaCompraAnterior" ,A1."INGMontoATasaCompraAnterior",A1."INGMontoInteresCompraAnterior" 
				,A1."INGMontoReajusteCompraAnterior",A1."INGDurationCompraAnterior",A1."INGConvexidadCompraAnterior",A1."INGFechaPrecioTasaCompraActual",A1."INGPrecioCompraActual"
				,A1."INGTasaCompraActual",A1."INGMontoATasaCompraActual",A1."INGFechaPrecioTasaCompraProximo",A1."INGPrecioCompraProximo",A1."INGTasaCompraProximo",A1."INGMontoATasaCompraProximo"
				,A1."INGMontoInteresCompraProximo",A1."INGMontoReajusteCompraProximo",A1."INGDurationCompraProximo",A1."INGConvexidadCompraProximo",A1."INGFechaPrecioTasaMercadoAnterior"
				,A1."INGPrecioMercadoAnterior",A1."INGTasaMercadoAnterior" ,A1."INGIdOrigenTasaPrecioMercadoAnterior" ,A1."INGMontoATasaMercadoAnterior" ,A1."INGMontoInteresMercadoAnterior" 
				,A1."INGMontoReajusteMercadoAnterior",A1."INGDurationMercadoAnterior",A1."INGConvexidadMercadoAnterior" ,A1."INGFechaPrecioTasaMercadoActual",A1."INGIdOrigenTasaPrecioMercadoActual"
				,A1."INGPrecioMercadoActual",A1."INGTasaMercadoActual" ,A1."INGMontoATasaMercadoActual",A1."INGFechaPrecioTasaMercadoProximo",A1."INGPrecioMercadoProximo",A1."INGTasaMercadoProximo" 
				,A1."INGIdOrigenTasaPrecioMercadoProximo",A1."INGMontoATasaMercadoProximo",A1."INGMontoInteresMercadoProximo" ,A1."INGMontoReajusteMercadoProximo",A1."INGDurationMercadoProximo"
				,A1."INGConvexidadMercadoProximo",A1."INGFechaPrecioTasaDescuentoAnterior",A1."INGPrecioDescuentoAnterior",A1."INGTasaDescuentoAnterior",A1."INGMontoATasaDescuentoAnterior" 
				,A1."INGMontoInteresDescuentoAnterior",A1."INGMontoReajusteDescuentoAnterior",A1."INGDurationDescuentoAnterior" ,A1."INGConvexidadDescuentoAnterior" 
				,A1."INGFechaPrecioTasaDescuentoActual",A1."INGPrecioDescuentoActual",A1."INGTasaDescuentoActual" ,A1."INGMontoATasaDescuentoActual" ,A1."INGFechaPrecioTasaDescuentoProximo"
				,A1."INGPrecioDescuentoProximo",A1."INGTasaDescuentoProximo" ,A1."INGMontoATasaDescuentoProximo" ,A1."INGMontoInteresDescuentoProximo" ,A1."INGMontoReajusteDescuentoProximo"
				,A1."INGDurationDescuentoProximo",A1."INGConvexidadDescuentoProximo" ,A1."INGPrecioPactoAnterior",A1."INGTasaPactoAnterior" ,A1."INGMontoATasaPactoAnterior"
				,A1."INGMontoInteresPactoAnterior",A1."INGMontoReajustePactoAnterior",A1."INGDurationPactoAnterior",A1."INGConvexidadPactoAnterior",A1."INGFechaPrecioTasaPactoActual"
				,A1."INGPrecioPactoActual",A1."INGTasaPactoActual",A1."INGMontoATasaPactoActual",A1."INGPrecioPactoProximo",A1."INGTasaPactoProximo",A1."INGMontoATasaPactoProximo"
				,A1."INGMontoInteresPactoProximo",A1."INGMontoReajustePactoProximo",A1."INGDurationPactoProximo",A1."INGConvexidadPactoProximo",A1."INGTasaCorredora",A1."INGFechaTasaCorredora" 
				,A1."INGMontoATasaCorredora",A1."INGMontoCorteCupon",A1."INGFechaCorteCupon",A1."INGMontoCorteCuponProximo",A1."EGRGlosa",A1."EGRFechaOperacion",A1."EGRIdTipoOperacion"
				,A1."EGRIdOperacion",A1."EGRIdMovimientoCarteraTransito",A1."EGRIdTipoCanalIngreso",A1."EGRIdCondicionLiquidacion",A1."EGRFechaLiquidacion",A1."EGRIdComprobanteFactReferencia"
				,A1."EGRFolioExternoReferencia",A1."EGRIdModulo",A1."EGRIdConcepto",A1."EGRIdUsuario",A1."EGRIdSucursal",A1."EGRIdAgente",A1."EGRCantidad",A1."EGRMonto",A1."EGRMontoEnMonedaInstrumento"
				,A1."EGRValorRescate",A1."EGRPrecio",A1."EGRTasa",A1."EGRSpreadCompra",A1."EGRSpreadMercado",A1."EGRMontoAPlazo",A1."EGRPrecioAPlazo"
				FROM "BSCL"."BSCL.Homologacion.BD::HOM.Subyacente" AS A1
				LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS A2 
				ON A2."IdSubyacente" = A1."IdSubyacente"
				WHERE A2."IdSubyacente" IS NULL;
				
				-------------------------------------------------
                --- INSERTAMOS CARTERA EN TRANSITO   ------------
                -------------------------------------------------
				
				insert into "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
                (
                     "IdMovimientoCarteraTransito","FechaIngresoRegistro","FechaModificacionRegistro","FolioCarteraEgreso","FolioCarteraRelacionada","EsIngreso"
					,"EsGarantia","IdOrigenGarantia","IdEspecieGarantia","FolioCarteraGarantizada","FolioCarteraOrigen","EsEspecifica","IdInstrumentoGarantizado"
					,"IdContraparteGarantia","IdEmpresa","IdUsuario","IdModulo","IdCliente","IdPortafolio","IdClienteRelacionado","IdPortafolioRelacionado","IdEstado"
					,"IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","CodigoIFRS","IdConcepto","IdTipoOperacion","IdOperacion","FechaOperacion","FechaVencimientoOperacion"
					,"PlazoEnDiasOperacion","IdComprobanteFactReferencia","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion"
					,"Glosa","EsNominal","EsIntraday","UsaGarantiaEspecifica","CobraPrimaAlInicio","IdTipoPrestamo","IdUsuarioReserva","FechaReserva","IdTipoPersona"
					,"IdPersona","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","FechaEmisionInstrumento"
					,"FechaVencimientoInstrumento","EsFungible","IdBoveda","IdMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","Precio","Tasa"
					,"TasaAgente","Monto","MontoEnMonedaInstrumento","MontoAPlazo","PrecioAPlazo")
				SELECT 
				 	 A1."IdMovimientoCarteraTransito",A1."FechaIngresoRegistro",A1."FechaModificacionRegistro",A1."FolioCarteraEgreso",A1."FolioCarteraRelacionada",A1."EsIngreso"
					,A1."EsGarantia",A1."IdOrigenGarantia",A1."IdEspecieGarantia",A1."FolioCarteraGarantizada",A1."FolioCarteraOrigen",A1."EsEspecifica",A1."IdInstrumentoGarantizado"
					,A1."IdContraparteGarantia",A1."IdEmpresa",A1."IdUsuario",A1."IdModulo",A1."IdCliente",A1."IdPortafolio",A1."IdClienteRelacionado",A1."IdPortafolioRelacionado",A1."IdEstado"
					,A1."IdTipoProducto",A1."IdTipoCanalIngreso",A1."IdMonedaSistema",A1."CodigoIFRS",A1."IdConcepto",A1."IdTipoOperacion",A1."IdOperacion",A1."FechaOperacion",A1."FechaVencimientoOperacion"
					,A1."PlazoEnDiasOperacion",A1."IdComprobanteFactReferencia",A1."FolioExternoReferencia",A1."IdMesa",A1."IdBolsa",A1."IdSucursal",A1."IdAgente",A1."IdCondicionLiquidacion",A1."FechaLiquidacion"
					,A1."Glosa",A1."EsNominal",A1."EsIntraday",A1."UsaGarantiaEspecifica",A1."CobraPrimaAlInicio",A1."IdTipoPrestamo",A1."IdUsuarioReserva",A1."FechaReserva",A1."IdTipoPersona"
					,A1."IdPersona",A1."IdClasificacionTipoInstrumento",A1."IdTipoInstrumento",A1."IdInstrumento",A1."Nemotecnico",A1."IdEmisor",A1."NemoSVS",A1."IdTipoReajuste",A1."FechaEmisionInstrumento"
					,A1."FechaVencimientoInstrumento",A1."EsFungible",A1."IdBoveda",A1."IdMonedaInstrumento",A1."Cantidad",A1."CorteMinimo",A1."ValorRescate",A1."Precio",A1."Tasa"
					,A1."TasaAgente",A1."Monto",A1."MontoEnMonedaInstrumento",A1."MontoAPlazo",A1."PrecioAPlazo"
				FROM "BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito" AS A1
				LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS A2
				ON A2."IdMovimientoCarteraTransito" = A1."IdMovimientoCarteraTransito"
				WHERE A1."IdMovimientoCarteraTransito" IS NULL; 
				

                
		               						SIGNAL SALIDA_CONTROLADA;         
	END;
