SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarFacturacionOptimus";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarFacturacionOptimus"(
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_Personas			   table ("RUT_CLI"   NVARCHAR(11), "SEC_RUT_CLI" NVARCHAR(1)),    -- rut del clientes sebra
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_fechaCambio         NVARCHAR(20),   --'YYYY-MM-DD HH:MM:SS' 	
	-- Salidas para mensajes de advertencias y errores
	OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  NVARCHAR(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	
	----------------------------------------------------
	--Declaraciones Variables
	-----------------------------------------------------
	
	-- declaracion variables
	   DECLARE v_total_movfac_hom BIGINT:=0;
	   DECLARE v_total_movfac_car BIGINT:=0;
	   DECLARE v_total_comfac_hom BIGINT:=0;
	   DECLARE v_total_comfac_car BIGINT:=0;
	   DECLARE v_total_detcomfac_hom BIGINT:=0;
	   DECLARE v_total_detcomfac_car BIGINT:=0;	   
	   DECLARE v_total_movdetcomfac_hom BIGINT:= 0;
	   DECLARE v_total_movdetcomfac_car BIGINT:= 0; 
       DECLARE v_Quantity BIGINT:=0;

		--------------------
		--Negocio Aqui
		--------------------
		select COUNT(1) INTO v_Quantity FROM :p_Personas;
		
		----movimientos a facturar-----
		IF v_Quantity > 0 THEN    			
			insert into "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante"(
			"IdMovimientoComprobante","IdEmpresa","IdConceptoMovimiento","SecuenciaAgrupacion","IdCliente","IdPortafolio","IdInstrumento","IdInstrumentoSubyacente"
			,"IdTipoInstrumento","IdTipoInstrumentoSubyacente","IdClasificacionTipoInstrumento","IdTipoProducto","IdCuentaInversion","IdMoneda","IdTipoComprobante"
			,"IdComprobante","IdTipoOperacion","IdBoveda","IdCorredoraContraparte","IdCondicionLiquidacion","FechaLiquidacion","IdBolsa","IdTipoReajuste","IdTipoConvenio"
			,"IdEstadoFacturacion","IdOrden","IdAsignacion","IdTipoCanalIngreso","FechaOrden","IdTransaccion","FolioTransaccion","FechaTransaccion","FechaVencimiento"
			,"FechaParidad","ValorParidad","ValorCaptacionMonedaOrigen","FolioComprobante","Tasa","Cantidad","Precio","Monto","MontoDescuento","MontoInteres","Glosa"
			,"ValorRescate","TasaAgente","Spread","DiasPlazo","CodigoCanalIngreso","DocumentoIdentidad","TipoDocumentoIdentidad","NombreLargoCliente","DireccionFacturacion"
			,"FolioCartera","CodigoTipoProducto","CodigoTipoOperacion","CodigoBolsa","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente","NombreClasificacionTipoInstrumento"
			,"NemotecnicoInstrumento","NemotecnicoSubyacente","RazonSocialEmisor","FechaEmision","TasaEmision","FechaIngresoRegistro","FechaSistema","EsComprobIndividual"
			,"EsCuentaPropia","EsOrdenDirecta","EsPrecioPrima","EsEnRueda","EsRuteo","EsIngresoAutomatico","IdUsuario","IdUsuarioBloqueo","IdCarga","EsAPlazo"
			,"MontoAPlazo","PrecioAPlazo","IdMonedaSubyacente","IdMonedaLiquidacion","ValorTipoCambio","EsVentaCorta","SecMovimientoFacturarMigracion","ActualizarCustodia"
			,"FechaMovimientoComprobante","FechaTipoCambio","ActualizarCustodiaRelacionado","IdMonedaMovimientoComprobante","MontoMovimientoComprobante","MontoLiquidacion"
			,"IdEmisor","EsFungible","IdFormaCustodia","ActualizarCuentaInversion","ActualizarTesoreria","IdInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdClasificacionTipoInstrumentoRelacionado"
			,"NombreTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumentoRelacionado","NemotecnicoRelacionado","IdAsignacionRef","IdEmpleadoDigitador","IdEmpleadoEjecutivoPortafolio"
			,"IdSucursalEmpleadoEjecutivoPortafolio","ModPrefFact","IdSucursalEmpleadoDigitador","NombreEmpleadoDigitador","NombreSucursalEmpleadoDigitador","NombreEmpleadoEjecutivoPortafolio"
			,"NombreSucursalEmpleadoEjecutivoPortafolio","IdComprobanteReferencia","FolioComprobanteReferencia","IdMovimientoRelacionado","IdMovimientoOdRelacionado","FechaComprobanteReferencia"
			,"CodigoIFRS","IdTipoComprobanteReferencia")		        			
			select 	
			  HOMOLOGACION."IdMovimientoComprobante"
			 ,HOMOLOGACION."IdEmpresa"
			 ,HOMOLOGACION."IdConceptoMovimiento"
			 ,HOMOLOGACION."SecuenciaAgrupacion"
			 ,HOMOLOGACION."IdCliente"
			 ,HOMOLOGACION."IdPortafolio"
			 ,HOMOLOGACION."IdInstrumento"
			 ,HOMOLOGACION."IdInstrumentoSubyacente"
			 ,HOMOLOGACION."IdTipoInstrumento"
			 ,HOMOLOGACION."IdTipoInstrumentoSubyacente"
			 ,HOMOLOGACION."IdClasificacionTipoInstrumento"
			 ,HOMOLOGACION."IdTipoProducto"
			 ,HOMOLOGACION."IdCuentaInversion"
			 ,HOMOLOGACION."IdMoneda"
			 ,HOMOLOGACION."IdTipoComprobante"
			 ,HOMOLOGACION."IdComprobante"
			 ,HOMOLOGACION."IdTipoOperacion"
			 ,HOMOLOGACION."IdBoveda"
			 ,HOMOLOGACION."IdCorredoraContraparte"
			 ,HOMOLOGACION."IdCondicionLiquidacion"
			 ,HOMOLOGACION."FechaLiquidacion"
			 ,HOMOLOGACION."IdBolsa"
			 ,HOMOLOGACION."IdTipoReajuste"
			 ,HOMOLOGACION."IdTipoConvenio"
			 ,HOMOLOGACION."IdEstadoFacturacion"
			 ,HOMOLOGACION."IdOrden"
			 ,HOMOLOGACION."IdAsignacion"
			 ,HOMOLOGACION."IdTipoCanalIngreso"
			 ,HOMOLOGACION."FechaOrden"
			 ,HOMOLOGACION."IdTransaccion"
			 ,HOMOLOGACION."FolioTransaccion"
			 ,HOMOLOGACION."FechaTransaccion"
			 ,HOMOLOGACION."FechaVencimiento"
			 ,HOMOLOGACION."FechaParidad"
			 ,HOMOLOGACION."ValorParidad"
			 ,HOMOLOGACION."ValorCaptacionMonedaOrigen"
			 ,HOMOLOGACION."FolioComprobante"
			 ,HOMOLOGACION."Tasa"
			 ,HOMOLOGACION."Cantidad"
			 ,HOMOLOGACION."Precio"
			 ,HOMOLOGACION."Monto"
			 ,HOMOLOGACION."MontoDescuento"
			 ,HOMOLOGACION."MontoInteres"
			 ,HOMOLOGACION."Glosa"
			 ,HOMOLOGACION."ValorRescate"
			 ,HOMOLOGACION."TasaAgente"
			 ,HOMOLOGACION."Spread"
			 ,HOMOLOGACION."DiasPlazo"
			 ,HOMOLOGACION."CodigoCanalIngreso"
			 ,HOMOLOGACION."DocumentoIdentidad"
			 ,HOMOLOGACION."TipoDocumentoIdentidad"
			 ,HOMOLOGACION."NombreLargoCliente"
			 ,HOMOLOGACION."DireccionFacturacion"
			 ,HOMOLOGACION."FolioCartera"
			 ,HOMOLOGACION."CodigoTipoProducto"
			 ,HOMOLOGACION."CodigoTipoOperacion"
			 ,HOMOLOGACION."CodigoBolsa"
			 ,HOMOLOGACION."NombreTipoInstrumento"
			 ,HOMOLOGACION."NombreTipoInstrumentoSubyacente"
			 ,HOMOLOGACION."NombreClasificacionTipoInstrumento"
			 ,HOMOLOGACION."NemotecnicoInstrumento"
			 ,HOMOLOGACION."NemotecnicoSubyacente"
			 ,HOMOLOGACION."RazonSocialEmisor"
			 ,HOMOLOGACION."FechaEmision"
			 ,HOMOLOGACION."TasaEmision"
			 ,HOMOLOGACION."FechaIngresoRegistro"
			 ,HOMOLOGACION."FechaSistema"
			 ,HOMOLOGACION."EsComprobIndividual"
			 ,HOMOLOGACION."EsCuentaPropia"
			 ,HOMOLOGACION."EsOrdenDirecta"
			 ,HOMOLOGACION."EsPrecioPrima"
			 ,HOMOLOGACION."EsEnRueda"
			 ,HOMOLOGACION."EsRuteo"
		     ,HOMOLOGACION."EsIngresoAutomatico"
			 ,HOMOLOGACION."IdUsuario"
			 ,HOMOLOGACION."IdUsuarioBloqueo"
			 ,HOMOLOGACION."IdCarga"
			 ,HOMOLOGACION."EsAPlazo"
			 ,HOMOLOGACION."MontoAPlazo"
			 ,HOMOLOGACION."PrecioAPlazo"
			 ,HOMOLOGACION."IdMonedaSubyacente"
			 ,HOMOLOGACION."IdMonedaLiquidacion"
			 ,HOMOLOGACION."ValorTipoCambio"
			 ,HOMOLOGACION."EsVentaCorta"
			 ,HOMOLOGACION."SecMovimientoFacturarMigracion"
			 ,'' as "ActualizarCustodia"
			 ,HOMOLOGACION."FechaMovimientoComprobante"
			 ,HOMOLOGACION."FechaTipoCambio"
			 ,HOMOLOGACION."ActualizarCustodiaRelacionado"
			 ,HOMOLOGACION."IdMonedaMovimientoComprobante"
			 ,HOMOLOGACION."MontoMovimientoComprobante"
			 ,HOMOLOGACION."MontoLiquidacion"
			 ,HOMOLOGACION."IdEmisor"
			 ,HOMOLOGACION."EsFungible"
			 ,HOMOLOGACION."IdFormaCustodia"
			 ,HOMOLOGACION."ActualizarCuentaInversion"
			 ,HOMOLOGACION."ActualizarTesoreria"
			 ,HOMOLOGACION."IdInstrumentoRelacionado"
			 ,HOMOLOGACION."IdTipoInstrumentoRelacionado"
			 ,HOMOLOGACION."IdClasificacionTipoInstrumentoRelacionado"
			 ,HOMOLOGACION."NombreTipoInstrumentoRelacionado"
			 ,HOMOLOGACION."NombreClasificacionTipoInstrumentoRelacionado"
			 ,HOMOLOGACION."NemotecnicoRelacionado"
			 ,HOMOLOGACION."IdAsignacionRef"
			 ,0  as "IdEmpleadoDigitador"
			 ,0  as "IdEmpleadoEjecutivoPortafolio"
			 ,0  as "IdSucursalEmpleadoEjecutivoPortafolio"
			 ,1  as "ModPrefFact"
			 ,0  as "IdSucursalEmpleadoDigitador"
			 ,'' as "NombreEmpleadoDigitador"
			 ,'' as "NombreSucursalEmpleadoDigitador"
			 ,'' as "NombreEmpleadoEjecutivoPortafolio"
			 ,'' as "NombreSucursalEmpleadoEjecutivoPortafolio"
			 ,0  as "IdComprobanteReferencia"
			 ,0  as "FolioComprobanteReferencia"
			 ,0  as "IdMovimientoRelacionado"
			 ,0  as  "IdMovimientoOdRelacionado"
			 ,'0001-01-01' as "FechaComprobanteReferencia"
			 ,'' as "CodigoIFRS"
			 ,'' as "IdTipoComprobanteReferencia"
			 from "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobante" as HOMOLOGACION
			 inner join :p_Personas as PERSONAS
			  	on PERSONAS."RUT_CLI" = HOMOLOGACION."RUT_CLI"
			  	and PERSONAS."SEC_RUT_CLI" = HOMOLOGACION."SEC_RUT_CLI"
			 left join "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante" as FACTURACION
			  	on HOMOLOGACION."SEC_MOVTO_FAC" = FACTURACION."SecMovimientoFacturarMigracion"
			 where FACTURACION."SecMovimientoFacturarMigracion" IS NULL
			   and HOMOLOGACION."IdCarga" = p_IdCarga;
			 
			 select count(1) into v_total_movfac_hom
			 from "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobante" as HOMOLOGACION
			 inner join :p_Personas as PERSONAS
			  	on PERSONAS."RUT_CLI" = HOMOLOGACION."RUT_CLI"
			  	and PERSONAS."SEC_RUT_CLI" = HOMOLOGACION."SEC_RUT_CLI"
			 WHERE HOMOLOGACION."IdCarga" = p_IdCarga;
			  	
	    ELSE
	        insert into "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante"(
			"IdMovimientoComprobante","IdEmpresa","IdConceptoMovimiento","SecuenciaAgrupacion","IdCliente","IdPortafolio","IdInstrumento","IdInstrumentoSubyacente"
			,"IdTipoInstrumento","IdTipoInstrumentoSubyacente","IdClasificacionTipoInstrumento","IdTipoProducto","IdCuentaInversion","IdMoneda","IdTipoComprobante"
			,"IdComprobante","IdTipoOperacion","IdBoveda","IdCorredoraContraparte","IdCondicionLiquidacion","FechaLiquidacion","IdBolsa","IdTipoReajuste","IdTipoConvenio"
			,"IdEstadoFacturacion","IdOrden","IdAsignacion","IdTipoCanalIngreso","FechaOrden","IdTransaccion","FolioTransaccion","FechaTransaccion","FechaVencimiento"
			,"FechaParidad","ValorParidad","ValorCaptacionMonedaOrigen","FolioComprobante","Tasa","Cantidad","Precio","Monto","MontoDescuento","MontoInteres","Glosa"
			,"ValorRescate","TasaAgente","Spread","DiasPlazo","CodigoCanalIngreso","DocumentoIdentidad","TipoDocumentoIdentidad","NombreLargoCliente","DireccionFacturacion"
			,"FolioCartera","CodigoTipoProducto","CodigoTipoOperacion","CodigoBolsa","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente","NombreClasificacionTipoInstrumento"
			,"NemotecnicoInstrumento","NemotecnicoSubyacente","RazonSocialEmisor","FechaEmision","TasaEmision","FechaIngresoRegistro","FechaSistema","EsComprobIndividual"
			,"EsCuentaPropia","EsOrdenDirecta","EsPrecioPrima","EsEnRueda","EsRuteo","EsIngresoAutomatico","IdUsuario","IdUsuarioBloqueo","IdCarga","EsAPlazo"
			,"MontoAPlazo","PrecioAPlazo","IdMonedaSubyacente","IdMonedaLiquidacion","ValorTipoCambio","EsVentaCorta","SecMovimientoFacturarMigracion","ActualizarCustodia"
			,"FechaMovimientoComprobante","FechaTipoCambio","ActualizarCustodiaRelacionado","IdMonedaMovimientoComprobante","MontoMovimientoComprobante","MontoLiquidacion"
			,"IdEmisor","EsFungible","IdFormaCustodia","ActualizarCuentaInversion","ActualizarTesoreria","IdInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdClasificacionTipoInstrumentoRelacionado"
			,"NombreTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumentoRelacionado","NemotecnicoRelacionado","IdAsignacionRef","IdEmpleadoDigitador","IdEmpleadoEjecutivoPortafolio"
			,"IdSucursalEmpleadoEjecutivoPortafolio","ModPrefFact","IdSucursalEmpleadoDigitador","NombreEmpleadoDigitador","NombreSucursalEmpleadoDigitador","NombreEmpleadoEjecutivoPortafolio"
			,"NombreSucursalEmpleadoEjecutivoPortafolio","IdComprobanteReferencia","FolioComprobanteReferencia","IdMovimientoRelacionado","IdMovimientoOdRelacionado","FechaComprobanteReferencia"
			,"CodigoIFRS","IdTipoComprobanteReferencia")		        			
			select 	
			  HOMOLOGACION."IdMovimientoComprobante"
			 ,HOMOLOGACION."IdEmpresa"
			 ,HOMOLOGACION."IdConceptoMovimiento"
			 ,HOMOLOGACION."SecuenciaAgrupacion"
			 ,HOMOLOGACION."IdCliente"
			 ,HOMOLOGACION."IdPortafolio"
			 ,HOMOLOGACION."IdInstrumento"
			 ,HOMOLOGACION."IdInstrumentoSubyacente"
			 ,HOMOLOGACION."IdTipoInstrumento"
			 ,HOMOLOGACION."IdTipoInstrumentoSubyacente"
			 ,HOMOLOGACION."IdClasificacionTipoInstrumento"
			 ,HOMOLOGACION."IdTipoProducto"
			 ,HOMOLOGACION."IdCuentaInversion"
			 ,HOMOLOGACION."IdMoneda"
			 ,HOMOLOGACION."IdTipoComprobante"
			 ,HOMOLOGACION."IdComprobante"
			 ,HOMOLOGACION."IdTipoOperacion"
			 ,HOMOLOGACION."IdBoveda"
			 ,HOMOLOGACION."IdCorredoraContraparte"
			 ,HOMOLOGACION."IdCondicionLiquidacion"
			 ,HOMOLOGACION."FechaLiquidacion"
			 ,HOMOLOGACION."IdBolsa"
			 ,HOMOLOGACION."IdTipoReajuste"
			 ,HOMOLOGACION."IdTipoConvenio"
			 ,HOMOLOGACION."IdEstadoFacturacion"
			 ,HOMOLOGACION."IdOrden"
			 ,HOMOLOGACION."IdAsignacion"
			 ,HOMOLOGACION."IdTipoCanalIngreso"
			 ,HOMOLOGACION."FechaOrden"
			 ,HOMOLOGACION."IdTransaccion"
			 ,HOMOLOGACION."FolioTransaccion"
			 ,HOMOLOGACION."FechaTransaccion"
			 ,HOMOLOGACION."FechaVencimiento"
			 ,HOMOLOGACION."FechaParidad"
			 ,HOMOLOGACION."ValorParidad"
			 ,HOMOLOGACION."ValorCaptacionMonedaOrigen"
			 ,HOMOLOGACION."FolioComprobante"
			 ,HOMOLOGACION."Tasa"
			 ,HOMOLOGACION."Cantidad"
			 ,HOMOLOGACION."Precio"
			 ,HOMOLOGACION."Monto"
			 ,HOMOLOGACION."MontoDescuento"
			 ,HOMOLOGACION."MontoInteres"
			 ,HOMOLOGACION."Glosa"
			 ,HOMOLOGACION."ValorRescate"
			 ,HOMOLOGACION."TasaAgente"
			 ,HOMOLOGACION."Spread"
			 ,HOMOLOGACION."DiasPlazo"
			 ,HOMOLOGACION."CodigoCanalIngreso"
			 ,HOMOLOGACION."DocumentoIdentidad"
			 ,HOMOLOGACION."TipoDocumentoIdentidad"
			 ,HOMOLOGACION."NombreLargoCliente"
			 ,HOMOLOGACION."DireccionFacturacion"
			 ,HOMOLOGACION."FolioCartera"
			 ,HOMOLOGACION."CodigoTipoProducto"
			 ,HOMOLOGACION."CodigoTipoOperacion"
			 ,HOMOLOGACION."CodigoBolsa"
			 ,HOMOLOGACION."NombreTipoInstrumento"
			 ,HOMOLOGACION."NombreTipoInstrumentoSubyacente"
			 ,HOMOLOGACION."NombreClasificacionTipoInstrumento"
			 ,HOMOLOGACION."NemotecnicoInstrumento"
			 ,HOMOLOGACION."NemotecnicoSubyacente"
			 ,HOMOLOGACION."RazonSocialEmisor"
			 ,HOMOLOGACION."FechaEmision"
			 ,HOMOLOGACION."TasaEmision"
			 ,HOMOLOGACION."FechaIngresoRegistro"
			 ,HOMOLOGACION."FechaSistema"
			 ,HOMOLOGACION."EsComprobIndividual"
			 ,HOMOLOGACION."EsCuentaPropia"
			 ,HOMOLOGACION."EsOrdenDirecta"
			 ,HOMOLOGACION."EsPrecioPrima"
			 ,HOMOLOGACION."EsEnRueda"
			 ,HOMOLOGACION."EsRuteo"
		     ,HOMOLOGACION."EsIngresoAutomatico"
			 ,HOMOLOGACION."IdUsuario"
			 ,HOMOLOGACION."IdUsuarioBloqueo"
			 ,HOMOLOGACION."IdCarga"
			 ,HOMOLOGACION."EsAPlazo"
			 ,HOMOLOGACION."MontoAPlazo"
			 ,HOMOLOGACION."PrecioAPlazo"
			 ,HOMOLOGACION."IdMonedaSubyacente"
			 ,HOMOLOGACION."IdMonedaLiquidacion"
			 ,HOMOLOGACION."ValorTipoCambio"
			 ,HOMOLOGACION."EsVentaCorta"
			 ,HOMOLOGACION."SecMovimientoFacturarMigracion"
			 ,'' as "ActualizarCustodia"
			 ,HOMOLOGACION."FechaMovimientoComprobante"
			 ,HOMOLOGACION."FechaTipoCambio"
			 ,HOMOLOGACION."ActualizarCustodiaRelacionado"
			 ,HOMOLOGACION."IdMonedaMovimientoComprobante"
			 ,HOMOLOGACION."MontoMovimientoComprobante"
			 ,HOMOLOGACION."MontoLiquidacion"
			 ,HOMOLOGACION."IdEmisor"
			 ,HOMOLOGACION."EsFungible"
			 ,HOMOLOGACION."IdFormaCustodia"
			 ,HOMOLOGACION."ActualizarCuentaInversion"
			 ,HOMOLOGACION."ActualizarTesoreria"
			 ,HOMOLOGACION."IdInstrumentoRelacionado"
			 ,HOMOLOGACION."IdTipoInstrumentoRelacionado"
			 ,HOMOLOGACION."IdClasificacionTipoInstrumentoRelacionado"
			 ,HOMOLOGACION."NombreTipoInstrumentoRelacionado"
			 ,HOMOLOGACION."NombreClasificacionTipoInstrumentoRelacionado"
			 ,HOMOLOGACION."NemotecnicoRelacionado"
			 ,HOMOLOGACION."IdAsignacionRef"
			 ,0  as "IdEmpleadoDigitador"
			 ,0  as "IdEmpleadoEjecutivoPortafolio"
			 ,0  as "IdSucursalEmpleadoEjecutivoPortafolio"
			 ,1  as "ModPrefFact"
			 ,0  as "IdSucursalEmpleadoDigitador"
			 ,'' as "NombreEmpleadoDigitador"
			 ,'' as "NombreSucursalEmpleadoDigitador"
			 ,'' as "NombreEmpleadoEjecutivoPortafolio"
			 ,'' as "NombreSucursalEmpleadoEjecutivoPortafolio"
			 ,0  as "IdComprobanteReferencia"
			 ,0  as "FolioComprobanteReferencia"
			 ,0  as "IdMovimientoRelacionado"
			 ,0  as  "IdMovimientoOdRelacionado"
			 ,'0001-01-01' as "FechaComprobanteReferencia"
			 ,'' as "CodigoIFRS"
			 ,'' as "IdTipoComprobanteReferencia"
			
			  from "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobante" as HOMOLOGACION
			  left join "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante" as FACTURACION
			  	on HOMOLOGACION."SEC_MOVTO_FAC" = FACTURACION."SecMovimientoFacturarMigracion"
			  where FACTURACION."SecMovimientoFacturarMigracion" IS NULL
			    and HOMOLOGACION."IdCarga" = p_IdCarga;	
			  
			 select count(1) into v_total_movfac_hom
			 from "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobante" as HOMOLOGACION
			 where HOMOLOGACION."IdCarga" = p_IdCarga; 
			 
		 END IF;	  

		 -- comprobante de facturacion ---
         IF v_Quantity > 0 THEN    
			 insert into "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
				 (
					"IdComprobante","IdEmpresa","IdPortafolio","IdCliente","DocumentoIdentidad","TipoDocumentoIdentidad","NombreLargoCliente","DireccionFacturacion"
					,"FolioComprobante","IdTipoComprobante","IdTipoOperacion","IdTipoProducto","IdClasificacionTipoInstrumento","IdTipoInstrumento","Monto"
					,"MontoCobro","MontoFinal","MontoPagado","IdCondicionLiquidacion","FechaLiquidacion","FechaEmision","FechaEnvio","FechaRespuestaEnvio"
					,"IdEstadoComprobante","Glosa","EsExento","FechaIngresoRegistro","FechaSistema","IdCarga","FolioComprobanteFacturacionMigracion","IdMoneda"
					,"IdMonedaLiquidacion","MontoLiquidacion","IdMonedaComprobante","MontoComprobante","MontoCobroComprobante","MontoCobroLiquidacion","ModPrefFact"
					,"IdConcepto","IdEmpleadoEjecutivoPortafolio","IdSucursalEmpleadoEjecutivoPortafolio","IdBolsa","IdComprobanteReferencia","FolioComprobanteReferencia"
					,"FechaComprobanteReferencia","IdTipoComprobanteReferencia","Cantidad"
				 )
				 
				 select 
					 HOMOLOGACION."IdComprobante"
					,HOMOLOGACION."IdEmpresa"
					,HOMOLOGACION."IdPortafolio"
					,HOMOLOGACION."IdCliente"
					,HOMOLOGACION."DocumentoIdentidad"
					,HOMOLOGACION."TipoDocumentoIdentidad"
					,HOMOLOGACION."NombreLargoCliente"
					,HOMOLOGACION."DireccionFacturacion"
					,HOMOLOGACION."FolioComprobante"
					,HOMOLOGACION."IdTipoComprobante"
					,HOMOLOGACION."IdTipoOperacion"
					,HOMOLOGACION."IdTipoProducto"
					,HOMOLOGACION."IdClasificacionTipoInstrumento"
					,HOMOLOGACION."IdTipoInstrumento"
					,HOMOLOGACION."Monto"
					,HOMOLOGACION."MontoCobro"
					,HOMOLOGACION."MontoFinal"
					,HOMOLOGACION."MontoPagado"
					,HOMOLOGACION."IdCondicionLiquidacion"
					,HOMOLOGACION."FechaLiquidacion"
					,HOMOLOGACION."FechaEmision"
					,HOMOLOGACION."FechaEnvio"
					--,HOMOLOGACION."IdEstadoEnvio"
					,HOMOLOGACION."FechaRespuestaEnvio"
					,HOMOLOGACION."IdEstadoComprobante"
					,HOMOLOGACION."Glosa"
					,HOMOLOGACION."EsExento"
					,HOMOLOGACION."FechaIngresoRegistro"
					,HOMOLOGACION."FechaSistema"
					,HOMOLOGACION."IdCarga"
					,HOMOLOGACION."FolioComprobanteFacturacionMigracion"
					,HOMOLOGACION."IdMoneda"
					,HOMOLOGACION."IdMonedaLiquidacion"
					,HOMOLOGACION."MontoLiquidacion"
					,HOMOLOGACION."IdMonedaComprobante"
					,HOMOLOGACION."MontoComprobante"
					,HOMOLOGACION."MontoCobroComprobante"
					,HOMOLOGACION."MontoCobroLiquidacion"
					,1  as "ModPrefFact"
					,'' as "IdConcepto"
					,0  as "IdEmpleadoEjecutivoPortafolio"
					,0  as "IdSucursalEmpleadoEjecutivoPortafolio"
					,'' as "IdBolsa"
					,coalesce(HOMOLOGACION."IdComprobanteRef",'') as "IdComprobanteReferencia"
					,0 as"FolioComprobanteReferencia"
					,'0001-01-01' as "FechaComprobanteReferencia"
					,0 as "IdTipoComprobanteReferencia"
					,0 as "Cantidad"
					from "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS HOMOLOGACION
					inner join :p_Personas as PERSONAS
				  	on PERSONAS."RUT_CLI" = HOMOLOGACION."RUT_CLI"
				  	and PERSONAS."SEC_RUT_CLI" = HOMOLOGACION."SEC_RUT_CLI"
					left join "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante" as FACTURACION
					  	on HOMOLOGACION."FOLIO_COMP_FAC" = FACTURACION."FolioComprobanteFacturacionMigracion"
					  	and HOMOLOGACION."IdTipoComprobante" = FACTURACION."IdTipoComprobante"
					where FACTURACION."FolioComprobanteFacturacionMigracion" IS NULL
					  and HOMOLOGACION."IdCarga" = p_IdCarga;
					
				select count(1) into v_total_comfac_hom
					from "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS HOMOLOGACION
					inner join :p_Personas as PERSONAS
				  	on PERSONAS."RUT_CLI" = HOMOLOGACION."RUT_CLI"
				  	and PERSONAS."SEC_RUT_CLI" = HOMOLOGACION."SEC_RUT_CLI"
					where HOMOLOGACION."IdCarga" = p_IdCarga; 	
	
			ELSE
				insert into "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
				 (
					"IdComprobante","IdEmpresa","IdPortafolio","IdCliente","DocumentoIdentidad","TipoDocumentoIdentidad","NombreLargoCliente","DireccionFacturacion"
					,"FolioComprobante","IdTipoComprobante","IdTipoOperacion","IdTipoProducto","IdClasificacionTipoInstrumento","IdTipoInstrumento","Monto"
					,"MontoCobro","MontoFinal","MontoPagado","IdCondicionLiquidacion","FechaLiquidacion","FechaEmision","FechaEnvio","FechaRespuestaEnvio"
					,"IdEstadoComprobante","Glosa","EsExento","FechaIngresoRegistro","FechaSistema","IdCarga","FolioComprobanteFacturacionMigracion","IdMoneda"
					,"IdMonedaLiquidacion","MontoLiquidacion","IdMonedaComprobante","MontoComprobante","MontoCobroComprobante","MontoCobroLiquidacion","ModPrefFact"
					,"IdConcepto","IdEmpleadoEjecutivoPortafolio","IdSucursalEmpleadoEjecutivoPortafolio","IdBolsa","IdComprobanteReferencia","FolioComprobanteReferencia"
					,"FechaComprobanteReferencia","IdTipoComprobanteReferencia","Cantidad"
				 )
				 select 
					HOMOLOGACION."IdComprobante"
					,HOMOLOGACION."IdEmpresa"
					,HOMOLOGACION."IdPortafolio"
					,HOMOLOGACION."IdCliente"
					,HOMOLOGACION."DocumentoIdentidad"
					,HOMOLOGACION."TipoDocumentoIdentidad"
					,HOMOLOGACION."NombreLargoCliente"
					,HOMOLOGACION."DireccionFacturacion"
					,HOMOLOGACION."FolioComprobante"
					,HOMOLOGACION."IdTipoComprobante"
					,HOMOLOGACION."IdTipoOperacion"
					,HOMOLOGACION."IdTipoProducto"
					,HOMOLOGACION."IdClasificacionTipoInstrumento"
					,HOMOLOGACION."IdTipoInstrumento"
					,HOMOLOGACION."Monto"
					,HOMOLOGACION."MontoCobro"
					,HOMOLOGACION."MontoFinal"
					,HOMOLOGACION."MontoPagado"
					,HOMOLOGACION."IdCondicionLiquidacion"
					,HOMOLOGACION."FechaLiquidacion"
					,HOMOLOGACION."FechaEmision"
					,HOMOLOGACION."FechaEnvio"
					--,HOMOLOGACION."IdEstadoEnvio"
					,HOMOLOGACION."FechaRespuestaEnvio"
					,HOMOLOGACION."IdEstadoComprobante"
					,HOMOLOGACION."Glosa"
					,HOMOLOGACION."EsExento"
					,HOMOLOGACION."FechaIngresoRegistro"
					,HOMOLOGACION."FechaSistema"
					,HOMOLOGACION."IdCarga"
					,HOMOLOGACION."FolioComprobanteFacturacionMigracion"
					,HOMOLOGACION."IdMoneda"
					,HOMOLOGACION."IdMonedaLiquidacion"
					,HOMOLOGACION."MontoLiquidacion"
					,HOMOLOGACION."IdMonedaComprobante"
					,HOMOLOGACION."MontoComprobante"
					,HOMOLOGACION."MontoCobroComprobante"
					,HOMOLOGACION."MontoCobroLiquidacion"
					,1  as "ModPrefFact"
					,'' as "IdConcepto"
					,0  as "IdEmpleadoEjecutivoPortafolio"
					,0  as "IdSucursalEmpleadoEjecutivoPortafolio"
					,'' as "IdBolsa"
					,0 as "IdComprobanteReferencia"
					,0 as"FolioComprobanteReferencia"
					,'0001-01-01' as "FechaComprobanteReferencia"
					,'' as "IdTipoComprobanteReferencia"
					,0 as "Cantidad"
					from "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS HOMOLOGACION
					left join "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante" as FACTURACION
					  	on HOMOLOGACION."FOLIO_COMP_FAC" = FACTURACION."FolioComprobanteFacturacionMigracion"
					  	and HOMOLOGACION."IdTipoComprobante" = FACTURACION."IdTipoComprobante"
					where FACTURACION."FolioComprobanteFacturacionMigracion" IS NULL
					 and HOMOLOGACION."IdCarga" = p_IdCarga;
					
					select count(1) into v_total_comfac_hom
					from "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS HOMOLOGACION
					where HOMOLOGACION."IdCarga" = p_IdCarga; 	
					
		    END IF;
		    		 
		 
	
			-----------------------------
			-- detalle de comprobante ---	
			-----------------------------            
            IF v_Quantity > 0 THEN   
	            insert into "BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle" ("IdComprobanteDetalle","IdComprobante","NroLinea","FechaLiquidacion","IdInstrumento","NemotecnicoInstrumento","IdTipoOperacion","IdTipoProducto"
					,"Cantidad","Precio","Monto","IdBolsa","IdEstadoComprobanteDetalle","FechaIngresoRegistro","IdCarga","FolioComprobanteFacturacionMigracion","MontoLiquidacion"
					,"IdMoneda","IdMonedaLiquidacion","MontoComprobanteDetalle","IdMonedaComprobanteDetalle","IdInstrumentoSubyacente","IdMonedaSubyacente","IdTransaccion"
					,"FechaComprobanteDetalle","NemotecnicoSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente"
					,"IdClasificacionTipoInstrumento","IdClasificacionTipoInstrumentoRelacionado","IdInstrumentoRelacionado","IdTipoInstrumentoRelacionado","NemotecnicoRelacionado"
					,"NombreClasificacionTipoInstrumentoRelacionado","NombreTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumento","IdEmpleadoEjecutivoPortafolio"
					,"IdSucursalEmpleadoEjecutivoPortafolio","ModPrefFact","IdConcepto")
					
				select             
						 HOMOLOGACION."IdComprobanteDetalle"
						,HOMOLOGACION."IdComprobante"
						,HOMOLOGACION."NroLinea"
						,HOMOLOGACION."FechaLiquidacion"
						,HOMOLOGACION."IdInstrumento"
						,HOMOLOGACION."NemotecnicoInstrumento"
						,HOMOLOGACION."IdTipoOperacion"
						,HOMOLOGACION."IdTipoProducto"
						,HOMOLOGACION."Cantidad"
						,HOMOLOGACION."Precio"
						,HOMOLOGACION."Monto"
						,HOMOLOGACION."IdBolsa"
						,HOMOLOGACION."IdEstadoComprobanteDetalle"
						,HOMOLOGACION."FechaIngresoRegistro"
						,HOMOLOGACION."IdCarga"
						,HOMOLOGACION."FolioComprobanteFacturacionMigracion"
						,HOMOLOGACION."MontoLiquidacion"
						,HOMOLOGACION."IdMoneda"
						,HOMOLOGACION."IdMonedaLiquidacion"
						,HOMOLOGACION."MontoComprobanteDetalle"
						,HOMOLOGACION."IdMonedaComprobanteDetalle"
						,HOMOLOGACION."IdInstrumentoSubyacente"
						,HOMOLOGACION."IdMonedaSubyacente"
						,HOMOLOGACION."IdTransaccion"
						,HOMOLOGACION."FechaComprobanteDetalle"
						,HOMOLOGACION."NemotecnicoSubyacente"
						,HOMOLOGACION."IdTipoInstrumento"
						,HOMOLOGACION."IdTipoInstrumentoSubyacente"
						,HOMOLOGACION."NombreTipoInstrumento"
						,HOMOLOGACION."NombreTipoInstrumentoSubyacente"
						,HOMOLOGACION."IdClasificacionTipoInstrumento"
						,HOMOLOGACION."IdClasificacionTipoInstrumentoRelacionado"
						,HOMOLOGACION."IdInstrumentoRelacionado"
						,HOMOLOGACION."IdTipoInstrumentoRelacionado"
						,HOMOLOGACION."NemotecnicoRelacionado"
						,HOMOLOGACION."NombreClasificacionTipoInstrumentoRelacionado"
						,HOMOLOGACION."NombreTipoInstrumentoRelacionado"
						,HOMOLOGACION."NombreClasificacionTipoInstrumento"
						,0 as "IdEmpleadoEjecutivoPortafolio"
						,0 as "IdSucursalEmpleadoEjecutivoPortafolio"
						,1 as "ModPrefFact"
						,'' as "IdConcepto"
					from "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS HOMOLOGACION
					inner join :p_Personas as PERSONAS
					  	on PERSONAS."RUT_CLI" = HOMOLOGACION."RUT_CLI"
					  	and PERSONAS."SEC_RUT_CLI" = HOMOLOGACION."SEC_RUT_CLI"
					left join "BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle" as FACTURACION
						  	on HOMOLOGACION."FOLIO_COMP_FAC" = FACTURACION."FolioComprobanteFacturacionMigracion"
						  	--and HOMOLOGACION."IdTipoComprobante" = FACTURACION."IdTipoComprobante"
						  	and HOMOLOGACION."NroLinea" = FACTURACION."NroLinea"
					where FACTURACION."FolioComprobanteFacturacionMigracion" IS NULL
					   and HOMOLOGACION."IdCarga"=p_IdCarga;

				select count(1) into v_total_detcomfac_hom
				from "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS HOMOLOGACION
				inner join :p_Personas as PERSONAS
				  	on PERSONAS."RUT_CLI" = HOMOLOGACION."RUT_CLI"
				  	and PERSONAS."SEC_RUT_CLI" = HOMOLOGACION."SEC_RUT_CLI"
				where HOMOLOGACION."IdCarga"=p_IdCarga;
				
			ELSE
			
            insert into "BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle" ("IdComprobanteDetalle","IdComprobante","NroLinea","FechaLiquidacion","IdInstrumento","NemotecnicoInstrumento","IdTipoOperacion","IdTipoProducto"
				,"Cantidad","Precio","Monto","IdBolsa","IdEstadoComprobanteDetalle","FechaIngresoRegistro","IdCarga","FolioComprobanteFacturacionMigracion","MontoLiquidacion"
				,"IdMoneda","IdMonedaLiquidacion","MontoComprobanteDetalle","IdMonedaComprobanteDetalle","IdInstrumentoSubyacente","IdMonedaSubyacente","IdTransaccion"
				,"FechaComprobanteDetalle","NemotecnicoSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente"
				,"IdClasificacionTipoInstrumento","IdClasificacionTipoInstrumentoRelacionado","IdInstrumentoRelacionado","IdTipoInstrumentoRelacionado","NemotecnicoRelacionado"
				,"NombreClasificacionTipoInstrumentoRelacionado","NombreTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumento","IdEmpleadoEjecutivoPortafolio"
				,"IdSucursalEmpleadoEjecutivoPortafolio","ModPrefFact","IdConcepto")

			select             
					 HOMOLOGACION."IdComprobanteDetalle"
					,HOMOLOGACION."IdComprobante"
					,HOMOLOGACION."NroLinea"
					,HOMOLOGACION."FechaLiquidacion"
					,HOMOLOGACION."IdInstrumento"
					,HOMOLOGACION."NemotecnicoInstrumento"
					,HOMOLOGACION."IdTipoOperacion"
					,HOMOLOGACION."IdTipoProducto"
					,HOMOLOGACION."Cantidad"
					,HOMOLOGACION."Precio"
					,HOMOLOGACION."Monto"
					,HOMOLOGACION."IdBolsa"
					,HOMOLOGACION."IdEstadoComprobanteDetalle"
					,HOMOLOGACION."FechaIngresoRegistro"
					,HOMOLOGACION."IdCarga"
					,HOMOLOGACION."FolioComprobanteFacturacionMigracion"
					,HOMOLOGACION."MontoLiquidacion"
					,HOMOLOGACION."IdMoneda"
					,HOMOLOGACION."IdMonedaLiquidacion"
					,HOMOLOGACION."MontoComprobanteDetalle"
					,HOMOLOGACION."IdMonedaComprobanteDetalle"
					,HOMOLOGACION."IdInstrumentoSubyacente"
					,HOMOLOGACION."IdMonedaSubyacente"
					,HOMOLOGACION."IdTransaccion"
					,HOMOLOGACION."FechaComprobanteDetalle"
					,HOMOLOGACION."NemotecnicoSubyacente"
					,HOMOLOGACION."IdTipoInstrumento"
					,HOMOLOGACION."IdTipoInstrumentoSubyacente"
					,HOMOLOGACION."NombreTipoInstrumento"
					,HOMOLOGACION."NombreTipoInstrumentoSubyacente"
					,HOMOLOGACION."IdClasificacionTipoInstrumento"
					,HOMOLOGACION."IdClasificacionTipoInstrumentoRelacionado"
					,HOMOLOGACION."IdInstrumentoRelacionado"
					,HOMOLOGACION."IdTipoInstrumentoRelacionado"
					,HOMOLOGACION."NemotecnicoRelacionado"
					,HOMOLOGACION."NombreClasificacionTipoInstrumentoRelacionado"
					,HOMOLOGACION."NombreTipoInstrumentoRelacionado"
					,HOMOLOGACION."NombreClasificacionTipoInstrumento"
					,0 as "IdEmpleadoEjecutivoPortafolio"
					,0 as "IdSucursalEmpleadoEjecutivoPortafolio"
					,1 as "ModPrefFact"
					,'' as "IdConcepto"
				from "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS HOMOLOGACION
				left join "BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle" as FACTURACION
					  	on HOMOLOGACION."FOLIO_COMP_FAC" = FACTURACION."FolioComprobanteFacturacionMigracion"
					  	--and HOMOLOGACION."IdTipoComprobante" = FACTURACION."IdTipoComprobante"
					  	and HOMOLOGACION."NroLinea" = FACTURACION."NroLinea"
				where FACTURACION."FolioComprobanteFacturacionMigracion" IS NULL
				  and HOMOLOGACION."IdCarga" = p_IdCarga;
				
				select count(1) into v_total_detcomfac_hom
				from "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS HOMOLOGACION
				where HOMOLOGACION."IdCarga"=p_IdCarga;
			
			END IF;

			----------------------------------
			--detalle movimiento comprobante--       
			----------------------------------
            IF v_Quantity > 0 THEN 
            
				insert into "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"( 
					"IdMovimientoComprobanteDetalle","IdComprobanteDetalle","IdMovimientoComprobante","IdTransaccion","IdInstrumento","NemotecnicoInstrumento","IdConceptoMovimiento","Cantidad"
					,"Precio","PrecioPromedio","Monto","Tasa","TasaEmision","ValorRescate","ValorParidad","ValorCaptacionMonedaOrigen","SecuenciaAgrupacion","IdEstadoMovimientoComprobanteDetalle"
					,"FechaIngresoRegistro","MontoLiquidacion","IdMonedaLiquidacion","MontoMovimientoComprobanteDetalle","IdMonedaMovimientoComprobanteDetalle","IdMoneda"
					,"IdInstrumentoSubyacente","IdMonedaSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente"
					,"NemotecnicoSubyacente","IdClasificacionTipoInstrumento","IdClasificacionTipoInstrumentoRelacionado","IdInstrumentoRelacionado","IdTipoInstrumentoRelacionado"
					,"NemotecnicoRelacionado","NombreClasificacionTipoInstrumentoRelacionado","NombreTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumento","ModPrefFact"
					,"IdEmpleadoEjecutivoPortafolio","IdSucursalEmpleadoEjecutivoPortafolio","CodigoIFRS","FolioTransaccion","EsEnRueda","EsOrdenDirecta","Spread","IdTipoReajuste"
					,"SecMovimientoFacturarMigracion","IdCarga")
				
				select 
					 	 HOMOLOGACION."IdMovimientoComprobanteDetalle"
					 	,HOMOLOGACION."IdComprobanteDetalle"
						,HOMOLOGACION."IdMovimientoComprobante"
						,HOMOLOGACION."IdTransaccion"
						,HOMOLOGACION."IdInstrumento"
						,HOMOLOGACION."NemotecnicoInstrumento"
						,HOMOLOGACION."IdConceptoMovimiento"
						,HOMOLOGACION."Cantidad"
						,HOMOLOGACION."Precio"
						,HOMOLOGACION."PrecioPromedio"
						,HOMOLOGACION."Monto"
						,HOMOLOGACION."Tasa"
						,HOMOLOGACION."TasaEmision"
						,HOMOLOGACION."ValorRescate"
						,HOMOLOGACION."ValorParidad"
						,HOMOLOGACION."ValorCaptacionMonedaOrigen"
						,HOMOLOGACION."SecuenciaAgrupacion"
						,HOMOLOGACION."IdEstadoMovimientoComprobanteDetalle"
						,HOMOLOGACION."FechaIngresoRegistro"
						,HOMOLOGACION."MontoLiquidacion"
						,HOMOLOGACION."IdMonedaLiquidacion"
						,HOMOLOGACION."MontoMovimientoComprobanteDetalle"
						,HOMOLOGACION."IdMonedaMovimientoComprobanteDetalle"
						,HOMOLOGACION."IdMoneda"
						,HOMOLOGACION."IdInstrumentoSubyacente"
						,HOMOLOGACION."IdMonedaSubyacente"
						,HOMOLOGACION."IdTipoInstrumento"
						,HOMOLOGACION."IdTipoInstrumentoSubyacente"
						,HOMOLOGACION."NombreTipoInstrumento"
						,HOMOLOGACION."NombreTipoInstrumentoSubyacente"
						,HOMOLOGACION."NemotecnicoSubyacente"
						,HOMOLOGACION."IdClasificacionTipoInstrumento"
						,HOMOLOGACION."IdClasificacionTipoInstrumentoRelacionado"
						,HOMOLOGACION."IdInstrumentoRelacionado"
						,HOMOLOGACION."IdTipoInstrumentoRelacionado"
						,HOMOLOGACION."NemotecnicoRelacionado"
						,HOMOLOGACION."NombreClasificacionTipoInstrumentoRelacionado"
						,HOMOLOGACION."NombreTipoInstrumentoRelacionado"
						,HOMOLOGACION."NombreClasificacionTipoInstrumento"
						,1 as "ModPrefFact"
						,0 as "IdEmpleadoEjecutivoPortafolio"
						,0 as "IdSucursalEmpleadoEjecutivoPortafolio"
					 	,'' as "CodigoIFRS"
						,'' as "FolioTransaccion"
						,'' as "EsEnRueda"
						,'' as "EsOrdenDirecta"
						,0  as "Spread"
						,'' as "IdTipoReajuste"
						,HOMOLOGACION."SecMovimientoFacturarMigracion"
						,HOMOLOGACION."IdCarga"
	                from "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS HOMOLOGACION
					inner join :p_Personas as PERSONAS
					  	on PERSONAS."RUT_CLI" = HOMOLOGACION."RUT_CLI"
					  	and PERSONAS."SEC_RUT_CLI" = HOMOLOGACION."SEC_RUT_CLI"
					left join "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" as FACTURACION
						  	on HOMOLOGACION."SecMovimientoFacturarMigracion" = FACTURACION."SecMovimientoFacturarMigracion"
					where FACTURACION."SecMovimientoFacturarMigracion" IS NULL
					and HOMOLOGACION."IdCarga" = p_IdCarga;
					
					select count(1) into v_total_movdetcomfac_hom
					from "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS HOMOLOGACION
					inner join :p_Personas as PERSONAS
					  	on PERSONAS."RUT_CLI" = HOMOLOGACION."RUT_CLI"
					  	and PERSONAS."SEC_RUT_CLI" = HOMOLOGACION."SEC_RUT_CLI"
					where HOMOLOGACION."IdCarga"= p_IdCarga;
						
				ELSE 
					insert into "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"( 
						"IdMovimientoComprobanteDetalle","IdComprobanteDetalle","IdMovimientoComprobante","IdTransaccion","IdInstrumento","NemotecnicoInstrumento","IdConceptoMovimiento","Cantidad"
						,"Precio","PrecioPromedio","Monto","Tasa","TasaEmision","ValorRescate","ValorParidad","ValorCaptacionMonedaOrigen","SecuenciaAgrupacion","IdEstadoMovimientoComprobanteDetalle"
						,"FechaIngresoRegistro","MontoLiquidacion","IdMonedaLiquidacion","MontoMovimientoComprobanteDetalle","IdMonedaMovimientoComprobanteDetalle","IdMoneda"
						,"IdInstrumentoSubyacente","IdMonedaSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente"
						,"NemotecnicoSubyacente","IdClasificacionTipoInstrumento","IdClasificacionTipoInstrumentoRelacionado","IdInstrumentoRelacionado","IdTipoInstrumentoRelacionado"
						,"NemotecnicoRelacionado","NombreClasificacionTipoInstrumentoRelacionado","NombreTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumento","ModPrefFact"
						,"IdEmpleadoEjecutivoPortafolio","IdSucursalEmpleadoEjecutivoPortafolio","CodigoIFRS","FolioTransaccion","EsEnRueda","EsOrdenDirecta","Spread","IdTipoReajuste"
						,"SecMovimientoFacturarMigracion","IdCarga"
					)
					select 
						 	 HOMOLOGACION."IdMovimientoComprobanteDetalle"
						 	,HOMOLOGACION."IdComprobanteDetalle"
							,HOMOLOGACION."IdMovimientoComprobante"
							,HOMOLOGACION."IdTransaccion"
							,HOMOLOGACION."IdInstrumento"
							,HOMOLOGACION."NemotecnicoInstrumento"
							,HOMOLOGACION."IdConceptoMovimiento"
							,HOMOLOGACION."Cantidad"
							,HOMOLOGACION."Precio"
							,HOMOLOGACION."PrecioPromedio"
							,HOMOLOGACION."Monto"
							,HOMOLOGACION."Tasa"
							,HOMOLOGACION."TasaEmision"
							,HOMOLOGACION."ValorRescate"
							,HOMOLOGACION."ValorParidad"
							,HOMOLOGACION."ValorCaptacionMonedaOrigen"
							,HOMOLOGACION."SecuenciaAgrupacion"
							,HOMOLOGACION."IdEstadoMovimientoComprobanteDetalle"
							,HOMOLOGACION."FechaIngresoRegistro"
							,HOMOLOGACION."MontoLiquidacion"
							,HOMOLOGACION."IdMonedaLiquidacion"
							,HOMOLOGACION."MontoMovimientoComprobanteDetalle"
							,HOMOLOGACION."IdMonedaMovimientoComprobanteDetalle"
							,HOMOLOGACION."IdMoneda"
							,HOMOLOGACION."IdInstrumentoSubyacente"
							,HOMOLOGACION."IdMonedaSubyacente"
							,HOMOLOGACION."IdTipoInstrumento"
							,HOMOLOGACION."IdTipoInstrumentoSubyacente"
							,HOMOLOGACION."NombreTipoInstrumento"
							,HOMOLOGACION."NombreTipoInstrumentoSubyacente"
							,HOMOLOGACION."NemotecnicoSubyacente"
							,HOMOLOGACION."IdClasificacionTipoInstrumento"
							,HOMOLOGACION."IdClasificacionTipoInstrumentoRelacionado"
							,HOMOLOGACION."IdInstrumentoRelacionado"
							,HOMOLOGACION."IdTipoInstrumentoRelacionado"
							,HOMOLOGACION."NemotecnicoRelacionado"
							,HOMOLOGACION."NombreClasificacionTipoInstrumentoRelacionado"
							,HOMOLOGACION."NombreTipoInstrumentoRelacionado"
							,HOMOLOGACION."NombreClasificacionTipoInstrumento"
							,1 as "ModPrefFact"
							,0 as "IdEmpleadoEjecutivoPortafolio"
							,0 as "IdSucursalEmpleadoEjecutivoPortafolio"
						 	,'' as "CodigoIFRS"
							,'' as "FolioTransaccion"
							,'' as "EsEnRueda"
							,'' as "EsOrdenDirecta"
							,0  as "Spread"
							,'' as "IdTipoReajuste"
							,HOMOLOGACION."SecMovimientoFacturarMigracion"
							,HOMOLOGACION."IdCarga"
		                from "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS HOMOLOGACION
						left join "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" as FACTURACION
							  	on HOMOLOGACION."SecMovimientoFacturarMigracion" = FACTURACION."SecMovimientoFacturarMigracion"
						where FACTURACION."SecMovimientoFacturarMigracion" IS NULL;
						
						select count(1) into v_total_movdetcomfac_hom
						from "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS HOMOLOGACION
						where HOMOLOGACION."IdCarga"=p_IdCarga;
                END IF;

	   -- cuanto se cargo  
	   SELECT COUNT(1) INTO v_total_movfac_car FROM "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante" WHERE "IdCarga" = p_IdCarga;
	   SELECT COUNT(1) INTO v_total_comfac_car FROM "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" WHERE "IdCarga" = p_IdCarga;
	   SELECT COUNT(1) INTO v_total_detcomfac_car FROM "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" WHERE "IdCarga" = p_IdCarga;
	   SELECT COUNT(1) INTO v_total_movdetcomfac_car FROM "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" WHERE "IdCarga" = p_IdCarga;

	   -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ORDEN
	   p_ResumenDatosProcesados =   select 'MOVCOMP' as "CodigoProceso" 
	   			  						  ,'FAC' as "Modulo" 
	   			 						  ,v_total_movfac_hom as "TotalRegistrosSebra"
	   			  					      ,v_total_movfac_car as "TotalRegistrosSap"  FROM DUMMY
       							    union all
       							    select 'COMFAC' as "CodigoProceso" 
   			  							  ,'FAC' as "Modulo" 
   			 							  ,v_total_comfac_hom as "TotalRegistrosSebra"
   			  							  ,v_total_comfac_car as "TotalRegistrosSap"  FROM DUMMY
   			  						union all
   							    	select 'DETCOMFAC' as "CodigoProceso" 
   			  						      ,'FAC' as "Modulo" 
   			 							  ,v_total_detcomfac_hom  as "TotalRegistrosSebra"
   			  							  ,v_total_detcomfac_car as "TotalRegistrosSap"  FROM DUMMY
   			  						union all
   							    	select 'MOVDETCOMFAC' as "CodigoProceso" 
   			  						   	  ,'FAC' as "Modulo" 
   			 						      ,v_total_movdetcomfac_hom as "TotalRegistrosSebra"
   			  							  ,v_total_movdetcomfac_car as "TotalRegistrosSap"  FROM DUMMY
	  							    union all
   			  						select * from :p_ResumenDatosProcesados;
	   			  							
	   			  IF (v_total_movdetcomfac_hom <> v_total_movdetcomfac_car) 
	   			     or (v_total_detcomfac_hom <> v_total_detcomfac_car) 
	   			     or (v_total_comfac_hom <> v_total_comfac_car)
	   			     or (v_total_movfac_hom <> v_total_movfac_car) THEN 
				     p_out_ErroresDetalle =  select 'FAC_CARGA_ERR_FAC' as "Codigo",'hubo un error en la carga de facturacion a sap' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			  END IF;
		END;

END;
