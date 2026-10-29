SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarMovimientosFacturarRV";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de MOVIMIENTOS A FACTURAR RV
--  renta variable
--  Autor: - EAR 


"BSCL.Homologacion.BD.Servicios::CargarMovimientosFacturarRV"(

	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD

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
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
	-- declaracion variables
	   DECLARE v_total_mov_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_mov_cargadas DECIMAL(26,8):=0;
	   DECLARE v_total_com_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_com_cargadas DECIMAL(26,8):=0;
	   DECLARE v_total_detcom_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_detcom_cargadas DECIMAL(26,8):=0;	    
	   
	   DECLARE v_Quantity BIGINT:=0;
	   DECLARE v_PerId NVARCHAR(11);
    
	-- creo tabla temporal para cargar los datos
		create local temporary table #t_MovtosFacturarOrigenRV
		(			 
			"IdMovimientoComprobante" BIGINT 
			,"IdEmpresa" BIGINT 
			,"IdConceptoMovimiento" NVARCHAR (6)
			,"SecuenciaAgrupacion" BIGINT 
			,"IdCliente" BIGINT 
			,"IdPortafolio" BIGINT 
			,"IdAgente" BIGINT 
			,"IdSucursal" BIGINT 
			,"IdCartera" BIGINT 
			,"IdInstrumento" BIGINT 
			,"IdInstrumentoSubyacente" BIGINT 
			,"IdTipoInstrumento" NVARCHAR (3)
			,"IdTipoInstrumentoSubyacente" NVARCHAR (3)
			,"IdClasificacionTipoInstrumento" NVARCHAR (3)
			,"IdTipoProducto" NVARCHAR (4)
			,"IdMercado" NVARCHAR (4)
			,"IdCuentaInversion" BIGINT 
			,"IdMoneda" NVARCHAR (3)
			,"IdTipoComprobante" NVARCHAR (3)
			,"IdComprobante" BIGINT 
			,"IdTipoOperacion" NVARCHAR (6)
			,"IdMovimientoRef" BIGINT 
			,"IdBoveda" NVARCHAR (3)
			,"IdCorredoraContraparte" BIGINT 
			,"IdCondicionLiquidacion" NVARCHAR (2)
			,"FechaLiquidacion" DATE 
			,"IdBolsa" NVARCHAR (3)
			,"IdTipoReajuste" NVARCHAR (2)
			,"IdTipoConvenio" NVARCHAR (3)
			,"IdFormaCustodia" NVARCHAR (3)
			,"IdTipoPagoCobroMovimiento" NVARCHAR (3)
			,"IdTipoTransaccion" NVARCHAR (3)
			,"IdEstadoFacturacion" NVARCHAR (3)
			,"IdFormaDePago" BIGINT 
			,"IdOrden" BIGINT 
			,"LineaOrden" INTEGER 
			,"IdAsignacion" BIGINT 
			,"IdTipoCanalIngreso" NVARCHAR (3)
			,"FechaOrden" TIMESTAMP 
			,"IdTransaccion" BIGINT 
			,"FolioTransaccion" BIGINT 
			,"FechaTransaccion" TIMESTAMP 
			,"FechaVencimiento" TIMESTAMP 
			,"FechaParidad" TIMESTAMP 
			,"ValorParidad" DECIMAL (26,8)
			,"ValorCaptacionMonedaOrigen" DECIMAL (26,8)
			,"FolioComprobante" BIGINT 
			,"Tasa" DECIMAL (26,8)
			,"Cantidad" DECIMAL (26,8)
			,"Precio" DECIMAL (26,8)
			,"Monto" DECIMAL (26,8)
			,"MontoDescuento" DECIMAL (26,8)
			,"MontoInteres" DECIMAL (26,8)
			,"Glosa" NVARCHAR (260)
			,"ValorRescate" DECIMAL (26,8)
			,"TasaAgente" DECIMAL (26,8)
			,"Spread" DECIMAL (26,8)
			,"DiasPlazo" DECIMAL (26,8)
			,"CodigoCanalIngreso" NVARCHAR (40)
			,"DocumentoIdentidad" NVARCHAR (40)
			,"TipoDocumentoIdentidad" NVARCHAR (1)
			,"NombreLargoCliente" NVARCHAR (300)
			,"DireccionFacturacion" NVARCHAR (100)
			,"NombreAgente" NVARCHAR (300)
			,"NombreSucursal" NVARCHAR (300)
			,"FolioCartera" BIGINT 
			,"CodigoCuentaInversion" NVARCHAR (40)
			,"CodigoTipoProducto" NVARCHAR (40)
			,"CodigoTipoOperacion" NVARCHAR (40)
			,"CodigoBolsa" NVARCHAR (40)
			,"NombreTipoInstrumento" NVARCHAR (100)
			,"NombreTipoInstrumentoSubyacente" NVARCHAR (100)
			,"NombreClasificacionTipoInstrumento" NVARCHAR (100)
			,"NemotecnicoInstrumento" NVARCHAR (40)
			,"NemotecnicoSubyacente" NVARCHAR (40)
			,"RazonSocialEmisor" NVARCHAR (100)
			,"FechaEmision" TIMESTAMP 
			,"TasaEmision" DECIMAL (26,8)
			,"FechaIngresoRegistro" TIMESTAMP 
			,"FechaSistema" TIMESTAMP 
			,"EsComprobIndividual" NVARCHAR (1)
			,"EsCuentaPropia" NVARCHAR (1)
			,"EsOrdenDirecta" NVARCHAR (1)
			,"EsEnvio" NVARCHAR (1)
			,"EsPrecioPrima" NVARCHAR (1)
			,"EsEnRueda" NVARCHAR (1)
			,"EsRuteo" NVARCHAR (1)
			,"EsIngresoAutomatico" NVARCHAR (1)
			,"IdUsuario" BIGINT 
			,"IdUsuarioBloqueo" BIGINT 
			,"IdCarga" BIGINT 
			,"EsAPlazo" NVARCHAR (1)
			,"MontoAPlazo" DECIMAL (26,8)
			,"PrecioAPlazo" DECIMAL (26,8)
			,"IdMovimientoOdRef" BIGINT 
			,"IdMonedaSubyacente" NVARCHAR (3)
			,"IdMonedaLiquidacion" NVARCHAR (3)
			,"FechaTipoCambio" NVARCHAR (10)
			,"ValorTipoCambio" DECIMAL (26,8)
			,"TasaAPlazo" DECIMAL (26,8)
			,"EsVentaCorta" NVARCHAR (1)
			,"IdComprobanteRef" BIGINT
			,"SecMovimientoFacturarMigracion" BIGINT
		);
		
        -------------------------------------
        -- tabla comprobantes
        -------------------------------------

		create local temporary table #t_MovimientoComprobante (
				   "IdComprobante"          BIGINT,
				   "IdEmpresa"              BIGINT,
				   "IdPortafolio"           BIGINT,
				   "IdCliente"              BIGINT,
				   "DocumentoIdentidad"     NVARCHAR(40),
				   "TipoDocumentoIdentidad" NVARCHAR(1),
				   "NombreLargoCliente"     NVARCHAR(300),
				   "DireccionFacturacion"   NVARCHAR(100),
				   "FolioComprobante"       BIGINT,
				   "IdTipoComprobante"      NVARCHAR(3),
				   "IdMonedaOrigen"         NVARCHAR(3),
				   "IdTipoOperacion"        NVARCHAR(6),
				   "IdTipoProducto"         NVARCHAR(4),
				   "IdClasificacionTipoInstrumento" NVARCHAR(3),
				   "IdTipoInstrumento"      NVARCHAR(3),
				   "Monto"                  DECIMAL(26,8),
				   "MontoCobro"             DECIMAL(26,8),
				   "MontoFinal"             DECIMAL(26,8),
				   "MontoPagado"            DECIMAL(26,8),
				   "IdAgente"               BIGINT,
				   "IdSucursal"             BIGINT,
				   "IdCondicionLiquidacion" NVARCHAR(2),
				   "FechaLiquidacion"       DATE,
				   "FechaEmision"           DATE,
				   "FechaEnvio"             SECONDDATE,
				   "IdEstadoEnvio"          NVARCHAR(3),
				   "FechaRespuestaEnvio"    SECONDDATE,
				   "IdEstadoComprobante"    NVARCHAR(3),
				   "IdTipoComprobanteRef"   NVARCHAR(3),
				   "IdComprobanteRef"       BIGINT,
				   "Glosa"                  NVARCHAR(260),
				   "EsExento"               NVARCHAR(1),
				   "FechaIngresoRegistro"   TIMESTAMP, 
				   "FechaSistema"           DATE, 
				   "FOLIO_COMP_FAC_GES"     BIGINT,
				   "TIPO_COMP_FAC_GES"		VARCHAR(40)
				);
						
		------------------------------------------
		-- tabla comprobantes detalle
        ------------------------------------------

		create local temporary table #t_MovimientoComprobanteDetalle (
		   "IdComprobanteDetalle" BIGINT,
		   "IdComprobante"        BIGINT, 
		   "NroLinea"             INTEGER,
		   "FechaLiquidacion"     DATE,
		   "IdInstrumento"        BIGINT,
		   "NemotecnicoInstrumento" NVARCHAR(100),
		   "IdTipoOperacion"      NVARCHAR(6),
		   "IdTipoProducto"       NVARCHAR(4),
		   "Cantidad"             DECIMAL(26,8),
		   "Precio"               DECIMAL(26,8),
		   "Monto"                DECIMAL(26,8),
		   "IdBolsa"              NVARCHAR(3),
		   "IdAgente"             BIGINT,
		   "IdSucursal"           BIGINT,
		   "IdEstadoComprobanteDetalle" NVARCHAR(3) ,
		   "FechaIngresoRegistro" TIMESTAMP,
		   "FolioComprobanteFacturacionMigracion" BIGINT,
		   "IdTipoComprobante" NVARCHAR(40)
		);
		
		--------------------
		--Negocio Aqui
		--------------------
		
		-- convierto el rut a formato sebra 
		select lpad(p_RutCliente,11) into p_RutCliente from dummy;
		select TRIM(SUBSTRING(p_RutCliente,1,9)) into v_PerId from dummy;


		IF P_Modalidad = 'R' THEN --REPROCESO
		 DELETE FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
         WHERE "IdCarga" = 	p_IdCarga;	
         
         DELETE FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
         WHERE "IdCarga" = 	p_IdCarga;	
         
         DELETE FROM "BSCL.Facturacion.BD::FAC.Comprobante"
         WHERE "IdCarga" = 	p_IdCarga;	
         
		END IF;

        -- RECUPERAMOS LOS VALORES A  CARGAR
       INSERT INTO #t_MovtosFacturarOrigenRV(
        	"IdMovimientoComprobante","IdEmpresa" ,"IdConceptoMovimiento" ,"SecuenciaAgrupacion","IdCliente","IdPortafolio" 
			,"IdAgente","IdSucursal","IdCartera","IdInstrumento","IdInstrumentoSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente"
			,"IdClasificacionTipoInstrumento","IdTipoProducto","IdMercado","IdCuentaInversion","IdMoneda","IdTipoComprobante","IdComprobante"
			,"IdTipoOperacion","IdMovimientoRef","IdBoveda","IdCorredoraContraparte","IdCondicionLiquidacion","FechaLiquidacion"	
			,"IdBolsa","IdTipoReajuste","IdTipoConvenio","IdFormaCustodia","IdTipoPagoCobroMovimiento","IdTipoTransaccion" 
			,"IdEstadoFacturacion","IdFormaDePago","IdOrden","LineaOrden","IdAsignacion","IdTipoCanalIngreso","FechaOrden" 
			,"IdTransaccion","FolioTransaccion","FechaTransaccion","FechaVencimiento","FechaParidad","ValorParidad","ValorCaptacionMonedaOrigen" 
			,"FolioComprobante","Tasa","Cantidad","Precio","Monto","MontoDescuento","MontoInteres","Glosa","ValorRescate" 
			,"TasaAgente","Spread","DiasPlazo","CodigoCanalIngreso" ,"DocumentoIdentidad" ,"TipoDocumentoIdentidad" 
			,"NombreLargoCliente","DireccionFacturacion","NombreAgente" ,"NombreSucursal" ,"FolioCartera","CodigoCuentaInversion"
			,"CodigoTipoProducto","CodigoTipoOperacion"	,"CodigoBolsa" ,"NombreTipoInstrumento"	,"NombreTipoInstrumentoSubyacente"
			,"NombreClasificacionTipoInstrumento","NemotecnicoInstrumento" ,"NemotecnicoSubyacente" ,"RazonSocialEmisor" 
			,"FechaEmision"	,"TasaEmision" ,"FechaIngresoRegistro" 	,"FechaSistema" ,"EsComprobIndividual" ,"EsCuentaPropia" 
			,"EsOrdenDirecta" ,"EsEnvio" ,"EsPrecioPrima" ,"EsEnRueda" ,"EsRuteo" ,"EsIngresoAutomatico" ,"IdUsuario" ,"IdUsuarioBloqueo" 
			,"IdCarga" ,"EsAPlazo" 	,"MontoAPlazo"	,"PrecioAPlazo" ,"IdMovimientoOdRef","IdMonedaSubyacente","IdMonedaLiquidacion"
			,"FechaTipoCambio","ValorTipoCambio","TasaAPlazo","EsVentaCorta","IdComprobanteRef","SecMovimientoFacturarMigracion")
	        SELECT 
	         0 as"IdMovimientoComprobante"
	        ,p_IdEmpresa as "IdEmpresa"
	        ,CASE WHEN (MOV_RV."TIPO_OPERAC" = 'C') THEN CONCAT(TIPOCOMPROBANTEFAC."ValorDestino",'COM')
	          ELSE CONCAT(TIPOCOMPROBANTEFAC."ValorDestino",'VEN')
	         END as "IdConceptoMovimiento"
	        ,0 as "SecuenciaAgrupacion"
	        ,CLI."IdCliente"
	        ,POR."IdPortafolio"
	        ,EMPLEADO_SAP."IdEmpleado" as "IdAgente" 
	        ,SUCUR_SAP."IdSucursal" as "IdSucursal"
	        ,0 as "IdCartera"
	        ,CASE WHEN MOV_RV."IND_SIMUL" = 'TS'
	         	THEN CASE WHEN MOV_RV."TIPO_OPERAC" = 'C' THEN 2248 ELSE 2249 END 
	         ELSE INS."IdInstrumento"
	         END as "IdInstrumento"
	        ,CASE WHEN MOV_RV."IND_SIMUL" ='TS'
	         	THEN INS."IdInstrumento"
	         	ELSE 0
	         END AS "IdInstrumentoSubyacente"   
	        ,CASE WHEN MOV_RV."IND_SIMUL" ='TS'
	        	THEN 'SIM'
	        	ELSE INS."IdTipoInstrumento"
	         END as "IdTipoInstrumento" 
	        ,CASE WHEN MOV_RV."IND_SIMUL" ='TS'
	        	THEN INS."IdTipoInstrumento"
	        	ELSE ''
	        END as "IdTipoInstrumentoSubyacente"
	        ,CLA_T_INS."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
	        ,TIPOPRODUCTO."ValorDestino" as "IdTipoProducto"
	        ,'' as "IdMercado"
	        ,CTA_INV_SAP."IdCuentaInversion" as "IdCuentaInversion"
	    	,INS."IdMoneda" as "IdMoneda"
			,TIPOCOMPROBANTEFAC."ValorDestino" as "IdTipoComprobante" 
			,0 as "IdComprobante" 
        	,CASE WHEN MOV_RV."TIPO_OPERAC" = 'C' AND  MOV_RV."IND_SIMUL" = 'TS' THEN 'COMSIM'
			     WHEN MOV_RV."TIPO_OPERAC" = 'V' AND  MOV_RV."IND_SIMUL" = 'TS' THEN 'VENSIM'
			     WHEN MOV_RV."TIPO_OPERAC" = 'C' AND  Homologacion."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND MOV_RV."IND_SIMUL" <> 'TS' THEN 'COMFUT' 
			     WHEN MOV_RV."TIPO_OPERAC" = 'V' AND  Homologacion."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND MOV_RV."IND_SIMUL" <> 'TS' THEN 'VENFUT'                                  
			     WHEN MOV_RV."TIPO_OPERAC" = 'V' AND  Homologacion."ValorDestino" = 'PAG' THEN 'VENIIF'
			     WHEN MOV_RV."TIPO_OPERAC" = 'C' AND  Homologacion."ValorDestino" = 'PAG' THEN 'COMIIF'
			     WHEN MOV_RV."TIPO_OPERAC" = 'C' THEN  CONCAT ('COM',Homologacion."ValorDestino")
			     WHEN MOV_RV."TIPO_OPERAC" = 'V' THEN  CONCAT ('VEN',Homologacion."ValorDestino")
			     ELSE ''
			 END AS "IdTipoOperacion"
			 ,0 as "IdMovimientoRef" 
			 ,MOV_RV."COD_BOVEDA" as "IdBoveda"
			 ,MOV_RV."COD_CORR_CONTRA" as "IdCorredoraContraparte" 
			 ,CONDLIQ."ValorDestino" as "IdCondicionLiquidacion"
			 ,ORDEN_SAP."FechaLiquidacion" as "FechaLiquidacion"
			 ,MOV_RV."IND_BOLSA"
			 ,'NR' as "IdTipoReajuste"
			 ,'SCV' as "IdTipoConvenio" 
			  ,CASE WHEN MOV_RV."IND_TIT_CUS" = 'C' THEN 'CUS' ELSE 'NOM' END as "IdFormaCustodia"
			 ,'CCI' as "IdTipoPagoCobroMovimiento" 
			 ,CASE WHEN MOV_RV."IND_SIMUL" = 'TS' THEN 'SIM' ELSE 'NOR' END as "IdTipoTransaccion"
			 ,ESTADOFACTURACION."ValorDestino" as "IdEstadoFacturacion"
			 ,0 as "IdFormaDePago"
			 ,ORDEN_SAP."IdOrden" as "IdOrden" 
			,MOV_RV."LIN_ORDEN" as "LineaOrden"
			 ,ASIG_SAP."IdAsignacion" as "IdAsignacion"
			 ,ORDEN_SAP."IdTipoCanalIngreso" as "IdTipoCanalIngreso"
			 ,MOV_RV."FEC_ORDEN" as "FechaOrden" 
			 ,TRANS_SAP."IdTransaccion" as "IdTransaccion" 
			 ,TRANS_SAP."FolioTransaccion" as "FolioTransaccion" 
			 ,CONCAT (SUBSTRING(MOV_RV."FEC_TRANS",1,10), CONCAT(' ',SUBSTRING (MOV_RV."FEC_TRANS", 12, 8))) as "FechaTransaccion" 
			 ,ORDEN_SAP."FechaVencimiento" as "FechaVencimiento" 
			 ,'0001-01-01' as "FechaParidad" 
			 ,MOV_RV."PARIDAD_MORIGEN" as "ValorParidad"
			 ,MOV_RV."CAPTACION_MORIGEN" as "ValorCaptacionMonedaOrigen"
			 ,0 as "FolioComprobante" 
			 ,MOV_RV."TASA" as "Tasa" 
			 ,MOV_RV."CANTIDAD" as "Cantidad" 
			 ,MOV_RV."PRECIO" as "Precio"
			 ,MOV_RV."MONTO" as "Monto"
			 ,0 as "MontoDescuento" 
			 ,0 as "MontoInteres" 
			 ,'' as "Glosa" 
			 ,0  as "ValorRescate" 
			 ,MOV_RV."TASA_AGE" as "TasaAgente" 
			 ,MOV_RV."SPREAD" as  "Spread" 
			 ,ORDEN_SAP."Plazo" as "DiasPlazo"
			 ,'' as "CodigoCanalIngreso"
			 ,CLI."DocumentoIdentidad" as "DocumentoIdentidad"
			 ,CLI."TipoDocumentoIdentidad" as "TipoDocumentoIdentidad"
			 ,CLI."NombreCompleto" as "NombreLargoCliente"
			 ,'' as "DireccionFacturacion"
			 ,'' as "NombreAgente"
			 ,SUCUR_SAP."NombreSucursal" as "NombreSucursal" 
			 ,0 as "FolioCartera"
			 ,CTA_INV_SAP."Codigo" as "CodigoCuentaInversion"
			 ,TIPOPRODUCTO."ValorDestino" as "CodigoTipoProducto" 
			 ,CASE WHEN MOV_RV."TIPO_OPERAC" = 'C' AND  MOV_RV."IND_SIMUL" = 'TS' THEN 'COMSIM'
			     WHEN MOV_RV."TIPO_OPERAC" = 'V' AND  MOV_RV."IND_SIMUL" = 'TS' THEN 'VENSIM'
			     WHEN MOV_RV."TIPO_OPERAC" = 'C' AND  Homologacion."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND MOV_RV."IND_SIMUL" <> 'TS' THEN 'COMFUT' 
			     WHEN MOV_RV."TIPO_OPERAC" = 'V' AND  Homologacion."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND MOV_RV."IND_SIMUL" <> 'TS' THEN 'VENFUT'                                  
			     WHEN MOV_RV."TIPO_OPERAC" = 'V' AND  Homologacion."ValorDestino" = 'PAG' THEN 'VENIIF'
			     WHEN MOV_RV."TIPO_OPERAC" = 'C' AND  Homologacion."ValorDestino" = 'PAG' THEN 'COMIIF'
			     WHEN MOV_RV."TIPO_OPERAC" = 'C' THEN  CONCAT ('COM',Homologacion."ValorDestino")
			     WHEN MOV_RV."TIPO_OPERAC" = 'V' THEN  CONCAT ('VEN',Homologacion."ValorDestino")
			     ELSE ''
			 END AS "CodigoTipoOperacion"
			 ,MOV_RV."IND_BOLSA" as "CodigoBolsa"
			 ,CASE WHEN MOV_RV."IND_SIMUL" = 'TS'
	     	  THEN 'SIMULTÁNEA'
	     	  ELSE  T_INS."Nombre"
	     	  END as "NombreTipoInstrumento"
			 ,CASE WHEN MOV_RV."IND_SIMUL" = 'TS'
	     	  THEN T_INS."Nombre"
	     	  ELSE ''
	     	  END as "NombreTipoInstrumentoSubyacente"
	         ,CASE WHEN MOV_RV."IND_SIMUL" IN ('TS','VC')
	     	  THEN 'Renta Variable'
	     	  ELSE CLA_T_INS."Nombre"
	     	  END as "NombreClasificacionTipoInstrumento"
			 ,CASE WHEN (MOV_RV."IND_SIMUL" ='TS' AND MOV_RV."TIPO_OPERAC" = 'C') THEN 'SIMULTÁNEA COMPRA'
			  ELSE CASE WHEN (MOV_RV."IND_SIMUL" ='TS' AND MOV_RV."TIPO_OPERAC" = 'V') THEN 'SIMULTÁNEA VENTA'
			  	   ELSE INS."Nemotecnico"
			  	   END
			  END as "NemotecnicoInstrumento"
			 ,CASE WHEN MOV_RV."IND_SIMUL" ='TS'
	     	  THEN INS."Nemotecnico"
	     	  ELSE ''
	     	  END as "NemotecnicoSubyacente"
			 ,IFNULL(EMI."RazonSocial",'') as "RazonSocialEmisor" 
			 ,'0001-01-01' as "FechaEmision" 
			 ,0 as "TasaEmision" 
			 ,MOV_RV."FEC_ING_MVTO" as "FechaIngresoRegistro"
			 ,'0001-01-01' as "FechaSistema" 
			 ,MOV_RV."IND_COMP_INDIV" as "EsComprobIndividual"
			 ,CASE WHEN MOV_RV."IND_CTA_PRO" = 'P' THEN 'S' ELSE 'N' END as "EsCuentaPropia"
			 ,CASE WHEN MOV_RV."IND_OD" = 'OD' THEN 'S' ELSE 'N' END as "EsOrdenDirecta"
			 ,'' as "EsEnvio"
			 ,CASE WHEN MOV_RV."IND_PRECIO_PRIMA" ='S' THEN 'S' ELSE 'N' END as "EsPrecioPrima"
			 ,CASE WHEN MOV_RV."RUEDA" ='FR' THEN 'N' ELSE 'S' END as"EsEnRueda"
			 ,CASE WHEN MOV_RV."IND_RUTEO"= 0 THEN 'N' ELSE 'S'END as "EsRuteo"
			 ,'' as "EsIngresoAutomatico"
			 ,p_IdUsuario as "IdUsuario" 
			 , 0 as "IdUsuarioBloqueo"
			 ,p_IdCarga as "IdCarga" 
			 ,'' as "EsAPlazo"
			 ,0 as "MontoAPlazo"
			 ,0 as "PrecioAPlazo"
			 ,0 as "IdMovimientoOdRef"
			,CASE WHEN MOV_RV."IND_SIMUL" ='TS' THEN INS."IdMoneda" ELSE '' END  as "IdMonedaSubyacente" 
			 ,'' as "IdMonedaLiquidacion"
			 ,'0001-01-01' as"FechaTipoCambio" 
			 ,0 as "ValorTipoCambio" 
			 ,0 as "TasaAPlazo"
			,CASE WHEN MOV_RV."IND_SIMUL" ='VC' THEN 'S' ELSE 'N' END as "EsVentaCorta"
			,0 as "IdComprobanteRef" 
			,MOV_RV."SEC_MOVTO_FAC" as "SecMovimientoFacturarMigracion"
	    	FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" as ASIG_RV
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_RV 
	  	   		ON MOV_RV."SEC_MOVTO_FAC" = ASIG_RV."SEC_MOVTO_FAC"
	  	   		AND MOV_RV."FEC_MVTO" >=P_FechaInicial
	  	   		AND MOV_RV."FEC_MVTO" <=P_FechaFinal
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" as ORD_GES
	  	   		ON ORD_GES."sol_sec" = MOV_RV."SEC_ORDEN" 
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
				ON INS."Nemotecnico" = MOV_RV."NEMO"
			LEFT JOIN "BSCL.Mercado.BD.Vistas::MER.ConsultarEmisor" EMI
				ON EMI."IdEmisor" = INS."IdEmisor"
			INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS T_INS
				ON T_INS."IdTipoInstrumento" = INS."IdTipoInstrumento"
			INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA_T_INS
				ON CLA_T_INS."IdClasificacionTipoInstrumento" = T_INS."IdClasificacionTipoInstrumento"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(MOV_RV."RUT_DEST") ELSE p_RutCliente END 
			INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON POR."IdCliente" = CLI."IdCliente"
				AND POR."CodigoPortafolio" = TO_VARCHAR(MOV_RV."SEC_RUT_DEST")
			INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFACTURACION
				ON ESTADOFACTURACION."TipoValor" = 'EstadoFacturacion' 
				AND ESTADOFACTURACION."Entidad" = 'FACTURA' 
				AND ESTADOFACTURACION."ValorOrigen" = MOV_RV."EST_MOVTO_FAC"
			INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  	ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  	AND TIPOPRODUCTO."Entidad" = 'FACTURA' 
			  	AND TIPOPRODUCTO."ValorOrigen" = MOV_RV."PRODUCTO"-- FALTA INCORPORAR LAS SIMULTANEAS Y PRESTAMOS
		    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as CONDLIQ
	  			ON  CONDLIQ."TipoValor" = 'CondicionLiquidacion'
	  			AND	CONDLIQ."Entidad" = 'FACTURA' 
	  			AND CONDLIQ."ValorOrigen" = case when ifnull(MOV_RV."IND_LIQUID",'') = '' then 'PH' else MOV_RV."IND_LIQUID" END
	  	    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTEFAC
	  			ON  TIPOCOMPROBANTEFAC."TipoValor" = 'TipoComprobante'
	  			AND	TIPOCOMPROBANTEFAC."Entidad" = 'FACTURA' 
	  			AND TIPOCOMPROBANTEFAC."ValorOrigen" = MOV_RV."TIPO_COMP_FAC"
	  	    INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarOrden" AS ORDEN_SAP
	  	   		ON ORDEN_SAP."SecOrdenMigracion" =  ORD_GES."ord_sec"
	  	    LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as Homologacion
		  		ON Homologacion."TipoValor" = 'TipoIntrumento'
		  		AND Homologacion."Entidad"  = 'FACTURA'
		  		AND Homologacion."ValorOrigen" = MOV_RV."MERCADO"
		    LEFT JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" as GRUPO_INS 
		 		ON GRUPO_INS."CodigoClasificacion" = 'FUTUROS'
		 		AND GRUPO_INS."IdTipoInstrumento" = Homologacion."ValorDestino"
	  	    INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" TRANS_SAP
	  	   		ON TRANS_SAP."FolioTransaccion" = TRIM(MOV_RV."FOLIO_TRANS")
	  	   		AND TRANS_SAP."IdBolsa" = MOV_RV."IND_BOLSA"
	  	   		AND TRANS_SAP."IdTipoOperacion" = 	CASE WHEN MOV_RV."IND_SIMUL" = 'TS' 
													THEN CASE WHEN MOV_RV."TIPO_OPERAC" = 'V' 
														 	THEN 'VENSIM' 
														 	ELSE 'COMSIM' 
														 END
			 	  									ELSE 
			 	       									CASE WHEN MOV_RV."TIPO_OPERAC" = 'V' 
			 	       									     THEN CASE WHEN IFNULL(GRUPO_INS."IdTipoInstrumento",'') = '' 
			 	       									     		THEN CONCAT('VEN',INS."IdTipoInstrumento")
			 	       									     		ELSE 'VENFUT'
			 	       									     	  END
			 	       									     ELSE CASE WHEN IFNULL(GRUPO_INS."IdTipoInstrumento",'') = '' 
			 	       									     	  	THEN CONCAT('COM',INS."IdTipoInstrumento")
			 	       									     	  	ELSE 'COMFUT'
			 	       									          END
			 	       									     END
			 										END
			    AND TRANS_SAP."FechaOperacionTransaccion" = CONCAT(SUBSTRING(MOV_RV."FEC_TRANS",1,10), CONCAT(' ',SUBSTRING (MOV_RV."FEC_TRANS", 12, 8)))
	  	   		AND TRANS_SAP."IdCarga" = p_IdCarga
	  	    INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CTA_INV_SAP
	  	   		ON CTA_INV_SAP."IdCliente"  = CLI."IdCliente"
	  	   		AND CTA_INV_SAP."IdPortafolio" = POR."IdPortafolio"
	  	   		AND CTA_INV_SAP."IdMoneda" = CASE WHEN MOV_RV."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		                             	ELSE CASE WHEN MOV_RV."IND_TIT_CCTE" = 'V' THEN 'EUR' ELSE 'CLP' END
	  	   		                             END
	  	    INNER JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUCUR_SAP
	  	   		ON SUCUR_SAP."CodigoSucursal" = TRIM(MOV_RV."COD_SUC")
	  	   		AND SUCUR_SAP."IdEmpresa" = p_IdEmpresa
	  	    INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion" as ASIG_SAP
	  	    	ON ASIG_SAP."IdPortafolio" = POR."IdPortafolio"
	  	    	AND ASIG_SAP."IdCliente" = CLI."IdCliente"
	  	    	AND ASIG_SAP."SecAsignacionMigracion" = ASIG_RV."SEC_ASIGNACION"
	  	    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" as EMPLEADO_SAP
	  	     	ON EMPLEADO_SAP."CodigoAgenteMigracion"  = MOV_RV."COD_AGENTE";
     
       --CONTAMOS EL NUMERO DE MOVIMIENTO A FACTURAR
       	 SELECT COUNT(*) INTO v_total_mov_gestion FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" as ASIG_RV
	  	 INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_RV 
	  	   		ON MOV_RV."SEC_MOVTO_FAC" = ASIG_RV."SEC_MOVTO_FAC"
	  	   		AND MOV_RV."FEC_MVTO" >=P_FechaInicial
	  	   		AND MOV_RV."FEC_MVTO" <=P_FechaFinal
	  	   		AND MOV_RV."RUT_DEST" = CASE WHEN p_RutCliente='        0-0' THEN MOV_RV."RUT_DEST" ELSE p_RutCliente END;
	  	   		
       
	    SELECT COUNT(*) INTO v_Quantity from  #t_MovtosFacturarOrigenRV;
	    IF v_Quantity > 0 THEN 	
	        INSERT INTO "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
	         ( "IdMovimientoComprobante","IdEmpresa","IdConceptoMovimiento","SecuenciaAgrupacion","IdCliente","IdPortafolio"
					,"IdAgente","IdSucursal","IdInstrumento","IdInstrumentoSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente"
					,"IdClasificacionTipoInstrumento","IdTipoProducto","IdMercado","IdCuentaInversion","IdMoneda","IdTipoComprobante","IdComprobante"
					,"IdTipoOperacion","IdMovimientoRef","IdBoveda","IdCorredoraContraparte","IdCondicionLiquidacion","FechaLiquidacion","IdBolsa"
					,"IdTipoReajuste","IdTipoConvenio","IdFormaCustodia","IdTipoPagoCobroMovimiento","IdTipoTransaccion","IdEstadoFacturacion"
					,"IdOrden","LineaOrden","IdAsignacion","IdTipoCanalIngreso","FechaOrden","IdTransaccion","FolioTransaccion"
					,"FechaTransaccion","FechaVencimiento","FechaParidad","ValorParidad","ValorCaptacionMonedaOrigen","FolioComprobante","Tasa"
					,"Cantidad","Precio","Monto","MontoDescuento","MontoInteres","Glosa","ValorRescate","TasaAgente","Spread","DiasPlazo"
					,"CodigoCanalIngreso","DocumentoIdentidad","TipoDocumentoIdentidad","NombreLargoCliente","DireccionFacturacion","NombreAgente"
					,"NombreSucursal","FolioCartera","CodigoCuentaInversion","CodigoTipoProducto","CodigoTipoOperacion","CodigoBolsa","NombreTipoInstrumento"
					,"NombreTipoInstrumentoSubyacente","NombreClasificacionTipoInstrumento","NemotecnicoInstrumento","NemotecnicoSubyacente","RazonSocialEmisor"
					,"FechaEmision","TasaEmision","FechaIngresoRegistro","FechaSistema","EsComprobIndividual","EsCuentaPropia","EsOrdenDirecta","EsEnvio"
					,"EsPrecioPrima","EsEnRueda","EsRuteo","EsIngresoAutomatico","IdUsuario","IdUsuarioBloqueo","IdCarga","EsAPlazo","MontoAPlazo","PrecioAPlazo"
					,"IdMovimientoOdRef","IdMonedaSubyacente","IdMonedaLiquidacion","FechaTipoCambio","ValorTipoCambio","TasaAPlazo","EsVentaCorta","IdComprobanteRef"
					,"SecMovimientoFacturarMigracion")
			    SELECT 
			     "BSCL.Facturacion.BD.Secuencias::MovimientoComprobante".NEXTVAL as "IdMovimientoComprobante" 
				,"IdEmpresa","IdConceptoMovimiento","SecuenciaAgrupacion","IdCliente" ,"IdPortafolio" ,"IdAgente" ,"IdSucursal" 
				,"IdInstrumento","IdInstrumentoSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente","IdClasificacionTipoInstrumento"
				,"IdTipoProducto","IdMercado","IdCuentaInversion","IdMoneda","IdTipoComprobante","IdComprobante","IdTipoOperacion","IdMovimientoRef" 
				,"IdBoveda","IdCorredoraContraparte","IdCondicionLiquidacion","FechaLiquidacion","IdBolsa","IdTipoReajuste" ,"IdTipoConvenio" 
				,"IdFormaCustodia","IdTipoPagoCobroMovimiento","IdTipoTransaccion","IdEstadoFacturacion","IdOrden" 
				,"LineaOrden","IdAsignacion","IdTipoCanalIngreso","FechaOrden","IdTransaccion","FolioTransaccion","FechaTransaccion","FechaVencimiento"  
				,"FechaParidad","ValorParidad","ValorCaptacionMonedaOrigen","FolioComprobante","Tasa","Cantidad","Precio","Monto","MontoDescuento" 
				,"MontoInteres","Glosa","ValorRescate" ,"TasaAgente","Spread","DiasPlazo","CodigoCanalIngreso","DocumentoIdentidad" 
				,"TipoDocumentoIdentidad","NombreLargoCliente","DireccionFacturacion","NombreAgente","NombreSucursal","FolioCartera","CodigoCuentaInversion"
				,"CodigoTipoProducto","CodigoTipoOperacion","CodigoBolsa","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente","NombreClasificacionTipoInstrumento"
				,"NemotecnicoInstrumento","NemotecnicoSubyacente","RazonSocialEmisor","FechaEmision","TasaEmision","FechaIngresoRegistro","FechaSistema" 
				,"EsComprobIndividual","EsCuentaPropia","EsOrdenDirecta","EsEnvio","EsPrecioPrima","EsEnRueda","EsRuteo","EsIngresoAutomatico","IdUsuario" 
				,"IdUsuarioBloqueo","IdCarga","EsAPlazo","MontoAPlazo","PrecioAPlazo","IdMovimientoOdRef","IdMonedaSubyacente","IdMonedaLiquidacion"
				,"FechaTipoCambio","ValorTipoCambio","TasaAPlazo","EsVentaCorta","IdComprobanteRef","SecMovimientoFacturarMigracion"
			    FROM #t_MovtosFacturarOrigenRV
			    WHERE NOT EXISTS (select 1 from "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 
			                      where "SecMovimientoFacturarMigracion" = #t_MovtosFacturarOrigenRV."SecMovimientoFacturarMigracion");
		END IF;  
		 
		SELECT COUNT(*) INTO v_total_mov_cargadas FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
		WHERE  "IdCarga" = p_IdCarga;
		
		-- SACAMOS LOS REGISTROS PROCESADOS de los movimientos a facturar
	   	p_ResumenDatosProcesados =  select 'CARGAMOVIMIENTOFACTURACION' as "CodigoProceso" 
   			  							,'FAC' as "Modulo" 
   			 							,v_total_mov_gestion as "TotalRegistrosSebra"
   			  							,v_total_mov_cargadas as "TotalRegistrosSap"  FROM DUMMY
   							    	union all
   							    	select * from :p_ResumenDatosProcesados;		
		 
		--------------------------------------------------
        -- cargar comprobantes y detalle comprobantes
        --------------------------------------------------
        --insertar en esta tabla 

        insert into #t_MovimientoComprobante
		("IdComprobante","IdEmpresa"  ,"IdPortafolio"  ,"IdCliente"  ,"DocumentoIdentidad"  ,"TipoDocumentoIdentidad" 
		,"NombreLargoCliente" ,"DireccionFacturacion"
		,"FolioComprobante"  ,"IdTipoComprobante","IdMonedaOrigen" ,"IdTipoOperacion"  
		,"IdTipoProducto","IdClasificacionTipoInstrumento" ,"IdTipoInstrumento","Monto" ,"MontoCobro" ,"MontoFinal" ,"MontoPagado"  
		,"IdAgente" ,"IdSucursal","IdCondicionLiquidacion" ,"FechaLiquidacion","FechaEmision" ,"FechaEnvio"  ,"IdEstadoEnvio"  
        ,"FechaRespuestaEnvio" ,"IdEstadoComprobante" ,"IdTipoComprobanteRef"  ,"IdComprobanteRef"  ,"Glosa"  ,"EsExento" 
		,"FechaIngresoRegistro" ,"FechaSistema" ,"FOLIO_COMP_FAC_GES","TIPO_COMP_FAC_GES")
        select 
        "BSCL.Facturacion.BD.Secuencias::Comprobante".NEXTVAL  as "IdComprobante"
        ,p_IdEmpresa as "IdEmpresa"
        ,POR."IdPortafolio" as "IdPortafolio"
        ,CLI."IdCliente" as "IdCliente" 
        ,CLI."DocumentoIdentidad" as "DocumentoIdentidad" 
        ,CLI."TipoDocumentoIdentidad" as "TipoDocumentoIdentidad"
        ,CLI."NombreCompleto" as "NombreLargoCliente" 
        ,'' as "DireccionFacturacion"
        , CASE WHEN TIPO_COMPROBANTE."ValorDestino" = 'FAC' THEN "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFactura".NEXTVAL
        	   WHEN TIPO_COMPROBANTE."ValorDestino" = 'FAE' THEN "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFacturaElectronica".NEXTVAL
               WHEN TIPO_COMPROBANTE."ValorDestino" = 'FOE' THEN "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFacturaElectronicaOtrosConceptos".NEXTVAL
               WHEN TIPO_COMPROBANTE."ValorDestino" = 'NCR' THEN "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCredito".NEXTVAL
               WHEN TIPO_COMPROBANTE."ValorDestino" = 'NCE' THEN "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCreditoElectronica".NEXTVAL
               WHEN TIPO_COMPROBANTE."ValorDestino" = 'NDB' THEN "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeDebito".NEXTVAL
               WHEN TIPO_COMPROBANTE."ValorDestino" = 'FAO' THEN "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFacturaOtrosConceptos".NEXTVAL
               WHEN TIPO_COMPROBANTE."ValorDestino" = 'NOE' THEN "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCreditoElectronicaOtrosConceptos".NEXTVAL
               WHEN TIPO_COMPROBANTE."ValorDestino" = 'NCO' THEN "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCreditoOtrosConceptos".NEXTVAL
               ELSE 0
        END as "FolioComprobante"
        ,TIPO_COMPROBANTE."ValorDestino"  as "IdTipoComprobante"
        ,'' as "IdMonedaOrigen"
        ,CASE WHEN GES_DET_FAC."TIPO_OPERAC" = 'C' AND  GES_FAC."IND_SIMUL" = 'TS' THEN 'COMSIM'
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' AND  GES_FAC."IND_SIMUL" = 'TS' THEN 'VENSIM'
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'C' AND  Homologacion."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND GES_FAC."IND_SIMUL" <> 'TS' THEN 'COMFUT' 
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' AND  Homologacion."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND GES_FAC."IND_SIMUL" <> 'TS' THEN 'VENFUT'                                  
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' AND  Homologacion."ValorDestino" = 'PAG' THEN 'VENIIF'
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'C' AND  Homologacion."ValorDestino" = 'PAG' THEN 'COMIIF'
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'C' THEN  CONCAT ('COM',Homologacion."ValorDestino")
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' THEN  CONCAT ('VEN',Homologacion."ValorDestino")
			     ELSE ''
		 END AS "IdTipoOperacion"
        ,SAP_T_OPE."IdTipoProducto" as "IdTipoProducto"
        ,CASE WHEN GES_FAC."IND_SIMUL" = 'TS' then 'RV'
         ELSE
         	INS."IdClasificacionTipoInstrumento"
         END as "IdClasificacionTipoInstrumento"
        ,CASE WHEN GES_FAC."IND_SIMUL" = 'TS' then INS2."IdTipoInstrumento"
         ELSE
         	INS."IdTipoInstrumento"
         END AS "IdTipoInstrumento"
        , CASE WHEN  GES_FAC."MONTO_COM" <> 0 THEN GES_FAC."MONTO_COM" ELSE GES_FAC."MONTO_VEN" END as "Monto"
        , 0 as "MontoCobro"
        , GES_FAC."MONTO_TOTAL" as "MontoFinal"
        , 0 as "MontoPagado"
        , IFNULL(EMPLEADO_SAP."IdEmpleado",0) as "IdAgente"
        , IFNULL(SUCUR_SAP."IdSucursal",0) as "IdSucursal"
        , CONDLIQ."ValorDestino" as "CondicionLiquidacion"
        , GES_FAC."FEC_PAGO" as "FechaLiquidacion"
        , GES_FAC."FEC_EMI_COMP" as "FechaEmision"
        , '0001-01-01' as "FechaEnvio"
        , '' as "IdEstadoEnvio"
        , '0001-01-01' as "FechaRespuestaEnvio"
        , EST_COMPROBANTE."ValorDestino" AS "IdEstadoComprobante"
        , ''  AS "IdTipoComprobanteRef"
        , 0 as "IdComprobanteRef"
        , '' as "Glosa"
        , CASE WHEN GES_FAC."TIPO_COMP_FAC" IN ('FE','FX') THEN 'S' ELSE 'N' END as "EsExento"
        , GES_FAC."FEC_ING_COMP" as "FechaIngresoRegistro"
        , '0001-01-01' as "FechaSistema"
        ,GES_FAC."FOLIO_COMP_FAC" AS "FOLIO_COMP_FAC_GES"  
		,GES_FAC."TIPO_COMP_FAC"
     	FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" GES_FAC
       	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(GES_FAC."RUT_CLI") ELSE p_RutCliente END
		INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON POR."IdCliente" = CLI."IdCliente"
				AND POR."CodigoPortafolio" = TO_VARCHAR(GES_FAC."SEC_RUT_CLI")
		INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as CONDLIQ
	  			ON  CONDLIQ."TipoValor" = 'CondicionLiquidacion'
	  			AND	CONDLIQ."Entidad" = 'FACTURA' 
	  			AND CONDLIQ."ValorOrigen" = case when ifnull(GES_FAC."IND_LIQUID",'') = '' then 'PH' else GES_FAC."IND_LIQUID" END
	    INNER JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" as GES_DET_FAC
			ON GES_DET_FAC."FOLIO_COMP_FAC" = GES_FAC."FOLIO_COMP_FAC"
			AND GES_DET_FAC."TIPO_COMP_FAC" = GES_FAC."TIPO_COMP_FAC"
			AND GES_DET_FAC."LIN_COMP_FAC"  = 1 
	    INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS
	      	ON INS."Nemotecnico" = GES_DET_FAC."NEMO"
	    LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as Homologacion
		  	ON Homologacion."TipoValor" = 'TipoIntrumento'
		  	AND Homologacion."Entidad"  = 'FACTURA'
		  	AND Homologacion."ValorOrigen" = GES_FAC."MERCADO"
		LEFT JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" as GRUPO_INS 
		 	ON GRUPO_INS."CodigoClasificacion" = 'FUTUROS'
		 	AND GRUPO_INS."IdTipoInstrumento" = Homologacion."ValorDestino"	  	
 		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS INS2
	       	ON INS2."IdTipoOperacion" = CASE WHEN (GES_FAC."IND_SIMUL" = 'TS' and GES_DET_FAC."TIPO_OPERAC" = 'C') then 'COMSIM'  
	       	                                 WHEN (GES_FAC."IND_SIMUL" = 'TS' and GES_DET_FAC."TIPO_OPERAC" = 'V') then 'VENSIM'
	       	                                 ELSE ''
	        	                        END		
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" as SAP_T_OPE_T_INS -- REVISAR SIMULTANEAS
			ON 	SAP_T_OPE_T_INS."IdTipoInstrumento" = CASE WHEN GES_FAC."IND_SIMUL" = 'TS' THEN INS2."IdTipoInstrumento"
			                                          	ELSE INS."IdTipoInstrumento"
			                                          END 
			AND SAP_T_OPE_T_INS."IdTipoOperacion" = CASE WHEN GES_FAC."IND_SIMUL" = 'TS' 
													THEN CASE WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' 
														 	THEN 'VENSIM' 
														 	ELSE 'COMSIM' 
														 END
			 	  									ELSE 
			 	       									CASE WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' 
			 	       									     THEN CASE WHEN IFNULL(GRUPO_INS."IdTipoInstrumento",'') = '' 
			 	       									     		THEN CONCAT('VEN',INS."IdTipoInstrumento")
			 	       									     		ELSE 'VENFUT'
			 	       									     	  END
			 	       									     ELSE CASE WHEN IFNULL(GRUPO_INS."IdTipoInstrumento",'') = '' 
			 	       									     	  	THEN CONCAT('COM',INS."IdTipoInstrumento")
			 	       									     	  	ELSE 'COMFUT'
			 	       									          END
			 	       									     END
			 										END
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as SAP_T_OPE
			ON SAP_T_OPE."IdTipoOperacion" = SAP_T_OPE_T_INS."IdTipoOperacion" 
		INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_COMPROBANTE
	  			ON  TIPO_COMPROBANTE."TipoValor" = 'TipoComprobante'
	  			AND	TIPO_COMPROBANTE."Entidad" = 'FACTURA' 
	  			AND TIPO_COMPROBANTE."ValorOrigen" = GES_FAC."TIPO_COMP_FAC"
	  	INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as EST_COMPROBANTE
	  			ON  EST_COMPROBANTE."TipoValor" = 'EstadoComprobante'
	  			AND	EST_COMPROBANTE."Entidad" = 'FACTURA' 
	  			AND EST_COMPROBANTE."ValorOrigen" = GES_FAC."EST_COMP"
	    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" as EMPLEADO_SAP
	  	     	ON EMPLEADO_SAP."CodigoAgenteMigracion"  = GES_FAC."COD_AGENTE"  	
	  	LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUCUR_SAP
	  	   		ON SUCUR_SAP."CodigoSucursal" = TRIM(GES_FAC."COD_SUC")
	  	   		AND SUCUR_SAP."IdEmpresa" = p_IdEmpresa
	  	WHERE GES_FAC."FEC_ING_COMP" >= P_FechaInicial
	  	AND GES_FAC."FEC_ING_COMP" <= P_FechaFinal
	  	AND GES_FAC."MERCADO" NOT IN ('RF','IF');
	  	
	  	

        select count(*) into v_total_com_gestion from "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_FAC
        WHERE GES_FAC."FEC_ING_COMP" >= P_FechaInicial
	  	AND GES_FAC."FEC_ING_COMP" <= P_FechaFinal
        AND GES_FAC."RUT_CLI"  = CASE WHEN p_RutCliente='        0-0' THEN GES_FAC."RUT_CLI" ELSE p_RutCliente END;
        
	    ----------------------------------------------
 		-- cargamos los detalles de los comprobantes
       	----------------------------------------------  

        insert into #t_MovimientoComprobanteDetalle(
					"IdComprobanteDetalle","IdComprobante","NroLinea" ,"FechaLiquidacion"  
					,"IdInstrumento"  ,"NemotecnicoInstrumento","IdTipoOperacion","IdTipoProducto" ,"Cantidad" 
					,"Precio","Monto" ,"IdBolsa"  ,"IdAgente" ,"IdSucursal" ,"IdEstadoComprobanteDetalle" ,"FechaIngresoRegistro"
					,"FolioComprobanteFacturacionMigracion","IdTipoComprobante")
		select 
		  "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle".NEXTVAL as "IdComprobanteDetalle"
		 , COMP_TEMP."IdComprobante" as "IdComprobante"
		 , GES_DET_FAC."LIN_COMP_FAC" as "NroLinea"
		 , GES_CAB_FAC."FEC_PAGO" as "FechaLiquidacion"
		 ,case when GES_CAB_FAC."IND_SIMUL" = 'TS'  THEN INS2."IdInstrumento" ELSE INS."IdInstrumento" END as "IdInstrumento"-- VER TEMA SIMULTANEA
		 ,case when GES_CAB_FAC."IND_SIMUL" = 'TS'  THEN INS2."Nemotecnico"  ELSE INS."Nemotecnico" END as "Nemotecnico"-- VER TEMA SIMULTANEA
		 ,CASE WHEN GES_DET_FAC."TIPO_OPERAC" = 'C' AND  GES_CAB_FAC."IND_SIMUL" = 'TS' THEN 'COMSIM'
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' AND  GES_CAB_FAC."IND_SIMUL" = 'TS' THEN 'VENSIM'
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'C' AND  Homologacion."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND GES_CAB_FAC."IND_SIMUL" <> 'TS' THEN 'COMFUT' 
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' AND  Homologacion."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND GES_CAB_FAC."IND_SIMUL" <> 'TS' THEN 'VENFUT'                                  
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' AND  Homologacion."ValorDestino" = 'PAG' THEN 'VENIIF'
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'C' AND  Homologacion."ValorDestino" = 'PAG' THEN 'COMIIF'
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'C' THEN  CONCAT ('COM',Homologacion."ValorDestino")
			     WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' THEN  CONCAT ('VEN',Homologacion."ValorDestino")
			     ELSE ''
		 END AS "IdTipoOperacion"
		,SAP_T_OPE."IdTipoProducto"
		,GES_DET_FAC."CANTIDAD"
		,GES_DET_FAC."PRECIO"
		,GES_DET_FAC."MONTO"
		,GES_CAB_FAC."IND_BOLSA" as "IdBolsa"
		,EMPLEADO_SAP."IdEmpleado"
		,SUCUR_SAP."IdSucursal"
		,COMP_TEMP."IdEstadoComprobante" as "IdEstadoComprobanteDetalle"
		,GES_DET_FAC."FEC_EMI" as "FechaIngresoRegistro"
        ,COMP_TEMP."FOLIO_COMP_FAC_GES" as "FolioComprobanteFacturacionMigracion"
		,TIPO_COMPROBANTE."ValorDestino" as "IdTipoComprobante"
        FROM "BSCL_VIRTUAL"."GES_TBCTDTCF" as  GES_DET_FAC
		INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" as GES_CAB_FAC
			ON GES_CAB_FAC."FOLIO_COMP_FAC" = GES_DET_FAC."FOLIO_COMP_FAC"
			AND GES_CAB_FAC."TIPO_COMP_FAC" = GES_DET_FAC."TIPO_COMP_FAC"
			AND GES_CAB_FAC.MERCADO NOT IN ('RF','IF')
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS
			ON INS."Nemotecnico" = GES_DET_FAC."NEMO"
	    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_COMPROBANTE
	  		ON  TIPO_COMPROBANTE."TipoValor" = 'TipoComprobante'
	  		AND	TIPO_COMPROBANTE."Entidad" = 'FACTURA' 
	  		AND TIPO_COMPROBANTE."ValorOrigen" = GES_DET_FAC."TIPO_COMP_FAC"
	  	LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as Homologacion
		  	ON Homologacion."TipoValor" = 'TipoIntrumento'
		  	AND Homologacion."Entidad"  = 'FACTURA'
		  	AND Homologacion."ValorOrigen" = GES_DET_FAC."MERCADO"
		LEFT JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" as GRUPO_INS 
		 	ON GRUPO_INS."CodigoClasificacion" = 'FUTUROS'
		 	AND GRUPO_INS."IdTipoInstrumento" = Homologacion."ValorDestino"		
        LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS INS2
	       	ON INS2."IdTipoOperacion" = CASE WHEN (GES_CAB_FAC."IND_SIMUL" = 'TS' and GES_DET_FAC."TIPO_OPERAC" = 'C') then 'COMSIM'  
	       	                                 WHEN   (GES_CAB_FAC."IND_SIMUL" = 'TS' and GES_DET_FAC."TIPO_OPERAC" = 'V') then 'VENSIM'
	       	                                 ELSE ''
	        	                        END		
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" SAP_T_OPE_T_INS -- REVISAR SIMULTANEAS
			ON 	SAP_T_OPE_T_INS."IdTipoInstrumento" = CASE WHEN GES_CAB_FAC."IND_SIMUL" = 'TS' THEN INS2."IdTipoInstrumento"
			                                          	ELSE INS."IdTipoInstrumento"
			                                          END 
			AND SAP_T_OPE_T_INS."IdTipoOperacion" = CASE WHEN GES_CAB_FAC."IND_SIMUL" = 'TS' 
													THEN CASE WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' 
														 	THEN 'VENSIM' 
														 	ELSE 'COMSIM' 
														 END
			 	  									ELSE 
			 	       									CASE WHEN GES_DET_FAC."TIPO_OPERAC" = 'V' 
			 	       									     THEN CASE WHEN IFNULL(GRUPO_INS."IdTipoInstrumento",'') = '' 
			 	       									     		THEN CONCAT('VEN',INS."IdTipoInstrumento")
			 	       									     		ELSE 'VENFUT'
			 	       									     	  END
			 	       									     ELSE CASE WHEN IFNULL(GRUPO_INS."IdTipoInstrumento",'') = '' 
			 	       									     	  	THEN CONCAT('COM',INS."IdTipoInstrumento")
			 	       									     	  	ELSE 'COMFUT'
			 	       									          END
			 	       									     END
			 										END
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as SAP_T_OPE
			ON SAP_T_OPE."IdTipoOperacion" = SAP_T_OPE_T_INS."IdTipoOperacion"
		INNER JOIN #t_MovimientoComprobante AS COMP_TEMP
			ON COMP_TEMP."FOLIO_COMP_FAC_GES" = GES_DET_FAC."FOLIO_COMP_FAC"
			AND COMP_TEMP."TIPO_COMP_FAC_GES" = GES_DET_FAC."TIPO_COMP_FAC"			
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" as EMPLEADO_SAP
	  	   	ON EMPLEADO_SAP."CodigoAgenteMigracion"  = GES_CAB_FAC."COD_AGENTE"
	  	LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUCUR_SAP
	  	   	ON SUCUR_SAP."CodigoSucursal" = TRIM(GES_CAB_FAC."COD_SUC")
	  	   	AND SUCUR_SAP."IdEmpresa" = p_IdEmpresa;
               
       select count(*) into v_total_detcom_gestion from 
        "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_DET_FAC
	   INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" as GES_CAB_FAC
			ON GES_CAB_FAC."FOLIO_COMP_FAC" = GES_DET_FAC."FOLIO_COMP_FAC"
			AND GES_CAB_FAC."TIPO_COMP_FAC" = GES_DET_FAC."TIPO_COMP_FAC"
            AND GES_CAB_FAC."RUT_CLI"       = CASE WHEN p_RutCliente='        0-0' THEN GES_CAB_FAC."RUT_CLI" ELSE p_RutCliente END; 
      -----------------------------------------------
      -- insertamos la cabecera del comprobante -----
 	  -----------------------------------------------

	  insert into "BSCL.Facturacion.BD::FAC.Comprobante"(
		"IdComprobante","IdEmpresa","IdPortafolio","IdCliente","DocumentoIdentidad","TipoDocumentoIdentidad","NombreLargoCliente"
		,"DireccionFacturacion","FolioComprobante","IdTipoComprobante","IdMoneda","IdTipoOperacion","IdTipoProducto"
		,"IdClasificacionTipoInstrumento" ,"IdTipoInstrumento" ,"Monto","MontoCobro","MontoFinal","MontoPagado"
		,"IdAgente","IdSucursal","IdCondicionLiquidacion","FechaLiquidacion","FechaEmision","FechaEnvio","IdEstadoEnvio"
		,"FechaRespuestaEnvio" ,"IdEstadoComprobante" ,"IdTipoComprobanteRef","IdComprobanteRef","Glosa","EsExento"
		,"FechaIngresoRegistro","FechaSistema" ,"IdCarga","FolioComprobanteFacturacionMigracion")
	  select 
	  "IdComprobante","IdEmpresa","IdPortafolio","IdCliente","DocumentoIdentidad","TipoDocumentoIdentidad","NombreLargoCliente"
	   ,"DireccionFacturacion","FolioComprobante","IdTipoComprobante","IdMonedaOrigen","IdTipoOperacion","IdTipoProducto"
	   ,"IdClasificacionTipoInstrumento","IdTipoInstrumento","Monto" ,"MontoCobro" ,"MontoFinal" ,"MontoPagado"  
	   ,"IdAgente" ,"IdSucursal","IdCondicionLiquidacion" ,"FechaLiquidacion","FechaEmision" ,"FechaEnvio"  ,"IdEstadoEnvio"  
       ,"FechaRespuestaEnvio" ,"IdEstadoComprobante" ,"IdTipoComprobanteRef"  ,"IdComprobanteRef"  ,"Glosa"  ,"EsExento" 
	   ,"FechaIngresoRegistro" ,"FechaSistema",p_IdCarga,"FOLIO_COMP_FAC_GES"
	  from #t_MovimientoComprobante
	  where not exists (select 1 from "BSCL.Facturacion.BD::FAC.Comprobante" AS FAC
	                     where FAC."FolioComprobanteFacturacionMigracion" = #t_MovimientoComprobante."FOLIO_COMP_FAC_GES"
	                     and FAC."IdTipoComprobante" = #t_MovimientoComprobante."IdTipoComprobante");

	  select count(*) into v_total_com_cargadas from
	  "BSCL.Facturacion.BD::FAC.Comprobante"
	  where "IdCarga" = p_IdCarga;
	                    
      -----------------------------------------------
      -- insertamos el detalle del comprobante -----
 	  -----------------------------------------------
	  INSERT INTO "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"(
		"IdComprobanteDetalle","IdComprobante","NroLinea","FechaLiquidacion","IdInstrumento","NemotecnicoInstrumento"
		,"IdTipoOperacion","IdTipoProducto","Cantidad","Precio","Monto","IdBolsa"
		,"IdAgente","IdSucursal","IdEstadoComprobanteDetalle","FechaIngresoRegistro"
		,"IdCarga","FolioComprobanteFacturacionMigracion")
	  select 
	  "IdComprobanteDetalle","IdComprobante","NroLinea" ,"FechaLiquidacion","IdInstrumento"  ,"NemotecnicoInstrumento"
	  ,"IdTipoOperacion","IdTipoProducto" ,"Cantidad" ,"Precio","Monto" ,"IdBolsa"
	  ,"IdAgente" ,"IdSucursal" ,"IdEstadoComprobanteDetalle" ,"FechaIngresoRegistro",p_IdCarga
	  ,"FolioComprobanteFacturacionMigracion"
	  from #t_MovimientoComprobanteDetalle
	  where not exists (
	                    select 1 from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteDetalle" as DET
	                    where DET."FolioComprobanteFacturacionMigracion" = #t_MovimientoComprobanteDetalle."FolioComprobanteFacturacionMigracion"
	                      and DET."NroLinea" = #t_MovimientoComprobanteDetalle."NroLinea"
	                      and DET."IdTipoComprobante" = #t_MovimientoComprobanteDetalle."IdTipoComprobante");


     	select count(*) into v_total_detcom_cargadas from 
     	"BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
      	where "IdCarga" = p_IdCarga;

		-- SACAMOS LOS REGISTROS  comprobantes
	   	p_ResumenDatosProcesados =  select 'CARGACOMPROBANTESFACTURACION' as "CodigoProceso" 
   			  							,'FAC' as "Modulo" 
   			 							,v_total_com_gestion as "TotalRegistrosSebra"
   			  							,v_total_com_cargadas as "TotalRegistrosSap"  FROM DUMMY
   							    	union all
   							    	select * from :p_ResumenDatosProcesados;

		-- SACAMOS LOS REGISTROS de detalle comprobantes
	   	p_ResumenDatosProcesados =  select 'CARGADETALLECOMPROBANTESFACTURACION' as "CodigoProceso" 
   			  							,'FAC' as "Modulo" 
   			 							,v_total_detcom_gestion as "TotalRegistrosSebra"
   			  							,v_total_detcom_cargadas as "TotalRegistrosSap"  FROM DUMMY
   							    	union all
   							    	select * from :p_ResumenDatosProcesados;

        drop table #t_MovtosFacturarOrigenRV;
        drop table #t_MovimientoComprobante;
        drop table #t_MovimientoComprobanteDetalle;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
