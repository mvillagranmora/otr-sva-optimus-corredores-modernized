SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarDataPruebaMovimientosFacturarDesdeSebra";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de MOVIMIENTOS A FACTURAR RV
--  renta variable
--  Autor: - EAR
 
"BSCL.Homologacion.BD.Servicios::CargarDataPruebaMovimientosFacturarDesdeSebra"(
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdCarga                   BIGINT,         --ID de la carga
	IN  p_Modulo                    NVARCHAR(3),    -- modulo 
	IN  p_Modo                      NVARCHAR(1),    --"R" reproceso borra todos lo cargado y vuelve hacer la carga desde cero "N" nueva carga
	IN  p_FechaInicial              NVARCHAR(10),   --YYYY-MM-DD
	IN  p_FechaFinal				NVARCHAR(10),   --YYYY-MM-DD
	-- Parametros salida del servicio publico
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
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
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
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
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
	-- declaracion variables
	   DECLARE v_total_mov_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_mov_cargadas DECIMAL(26,8):=0; 
	   DECLARE v_Quantity BIGINT:=0;
	   DECLARE v_FechaSistema VARCHAR(10); 
    
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
			,"IdTipoTituloMovimiento" NVARCHAR (3)
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
		
      
		
		--------------------
		--Negocio Aqui
		--------------------
		IF p_Modo = 'R' THEN --REPROCESO
		 DELETE FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
         WHERE "IdCarga" = 	p_IdCarga;	         
		END IF;
		
		SELECT COUNT(*) INTO v_total_mov_gestion FROM "BSCL_VIRTUAL"."GES_TBCTMFRV";
		
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);

        -- RECUPERAMOS LOS VALORES A  CARGAR
       INSERT INTO #t_MovtosFacturarOrigenRV(
        	"IdMovimientoComprobante","IdEmpresa" ,"IdConceptoMovimiento" ,"SecuenciaAgrupacion","IdCliente","IdPortafolio" 
			,"IdAgente","IdSucursal","IdCartera","IdInstrumento","IdInstrumentoSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente"
			,"IdClasificacionTipoInstrumento","IdTipoProducto","IdMercado","IdCuentaInversion","IdMoneda","IdTipoComprobante","IdComprobante"
			,"IdTipoOperacion","IdMovimientoRef","IdBoveda","IdCorredoraContraparte","IdCondicionLiquidacion","FechaLiquidacion"	
			,"IdBolsa","IdTipoReajuste","IdTipoConvenio","IdTipoTituloMovimiento","IdTipoPagoCobroMovimiento","IdTipoTransaccion" 
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
			,CASE WHEN MOV_RV."IND_SIMUL" = 'TS' 
			      THEN 
			 	       CASE WHEN MOV_RV."TIPO_OPERAC" = 'V' THEN 'VENSIM' ELSE 'COMSIM' END
			 	  ELSE 
			 	       CASE WHEN MOV_RV."TIPO_OPERAC" = 'V' THEN  CONCAT('VEN',INS."IdTipoInstrumento") ELSE CONCAT('COM',INS."IdTipoInstrumento")END
			 END AS "IdTipoOperacion" 
			 ,0 as "IdMovimientoRef" 
			 ,MOV_RV."COD_BOVEDA" as "IdBoveda"
			 ,MOV_RV."COD_CORR_CONTRA" as "IdCorredoraContraparte" 
			 ,CONDLIQ."ValorDestino" as "IdCondicionLiquidacion"
			 ,ORDEN_SAP."FechaLiquidacion" as "FechaLiquidacion"
			 ,MOV_RV."IND_BOLSA"
			 ,'NR' as "IdTipoReajuste"
			 ,'SCV' as "IdTipoConvenio" 
			  ,CASE WHEN MOV_RV."IND_TIT_CUS" = 'C' THEN 'CUS' ELSE 'NOM' END as "IdTipoTituloMovimiento"
			 ,'CCI' as "IdTipoPagoCobroMovimiento" 
			 ,CASE WHEN MOV_RV."IND_SIMUL" = 'TS' THEN 'SIM' ELSE 'NOR' END as "IdTipoTransaccion"
			 ,ESTADOFACTURACION."ValorDestino" as "IdEstadoFacturacion"
			 ,0 as "IdFormaDePago"
			 ,ORDEN_SAP."IdOrden" as "IdOrden" 
			,MOV_RV."LIN_ORDEN" as "LineaOrden"
			 ,ASIG_SAP."IdAsignacion" as "IdAsignacion"
			 ,ORDEN_SAP."IdTipoCanalIngreso" as "IdTipoCanalIngreso"
			 ,v_FechaSistema as "FechaOrden" 
			 ,TRANS_SAP."IdTransaccion" as "IdTransaccion" 
			 ,TRANS_SAP."Folio" as "FolioTransaccion" 
			 ,CONCAT (v_FechaSistema, CONCAT(' ',SUBSTRING (MOV_RV."FEC_TRANS", 12, 8))) as "FechaTransaccion" 
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
			 ,CASE WHEN MOV_RV."IND_SIMUL" = 'TS' 
			      THEN 
			 	       CASE WHEN MOV_RV."TIPO_OPERAC" = 'V' THEN 'VENSIM' ELSE 'COMSIM' END
			 	  ELSE 
			 	       CASE WHEN MOV_RV."TIPO_OPERAC" = 'V' THEN  CONCAT('VEN',INS."IdTipoInstrumento") ELSE CONCAT('COM',INS."IdTipoInstrumento") END
			  END as "CodigoTipoOperacion"
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
			 ,v_FechaSistema as "FechaIngresoRegistro"
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
				ON CLI."DocumentoIdentidad" = MOV_RV."RUT_DEST"
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
	  	    INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" TRANS_SAP
	  	   		ON TRANS_SAP."Folio" = TRIM(MOV_RV."FOLIO_TRANS")
	  	   		AND TRANS_SAP."IdBolsa" = MOV_RV."IND_BOLSA"
	  	   		AND TRANS_SAP."IdTipoOperacion" = 	CASE WHEN MOV_RV."TIPO_OPERAC" = 'C' AND  MOV_RV."IND_SIMUL" = 'TS' THEN 'COMSIM'
			     							 WHEN MOV_RV."TIPO_OPERAC" = 'V' AND  MOV_RV."IND_SIMUL" = 'TS' THEN 'VENSIM'
			     							 WHEN MOV_RV."TIPO_OPERAC" = 'C' THEN  CONCAT ('COM',INS."IdTipoInstrumento")
			     							 WHEN MOV_RV."TIPO_OPERAC" = 'V' THEN  CONCAT ('VEN',INS."IdTipoInstrumento")
			                             END
			    AND TRANS_SAP."FechaOperacionTransaccion" = CONCAT(v_FechaSistema, CONCAT(' ',SUBSTRING (MOV_RV."FEC_TRANS", 12, 8)))
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
	  	    	ON ASIG_SAP."IdPortafolioCliente" = POR."IdPortafolio"
	  	    	AND ASIG_SAP."IdCliente" = CLI."IdCliente"
	  	    	AND ASIG_SAP."SecAsignacionMigracion" = ASIG_RV."SEC_ASIGNACION"
	  	    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" as EMPLEADO_SAP
	  	     	ON EMPLEADO_SAP."CodigoAgenteMigracion"  = MOV_RV."COD_AGENTE";
     
	    SELECT COUNT(*) INTO v_Quantity from  #t_MovtosFacturarOrigenRV;
	    IF v_Quantity > 0 THEN 	
	        INSERT INTO "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
	         ( "IdMovimientoComprobante","IdEmpresa","IdConceptoMovimiento","SecuenciaAgrupacion","IdCliente","IdPortafolio"
					,"IdAgente","IdSucursal","IdInstrumento","IdInstrumentoSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente"
					,"IdClasificacionTipoInstrumento","IdTipoProducto","IdMercado","IdCuentaInversion","IdMoneda","IdTipoComprobante","IdComprobante"
					,"IdTipoOperacion","IdMovimientoRef","IdBoveda","IdCorredoraContraparte","IdCondicionLiquidacion","FechaLiquidacion","IdBolsa"
					,"IdTipoReajuste","IdTipoConvenio","IdTipoTituloMovimiento","IdTipoPagoCobroMovimiento","IdTipoTransaccion","IdEstadoFacturacion"
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
				,"IdTipoTituloMovimiento","IdTipoPagoCobroMovimiento","IdTipoTransaccion","IdEstadoFacturacion","IdOrden" 
				,"LineaOrden","IdAsignacion","IdTipoCanalIngreso","FechaOrden","IdTransaccion","FolioTransaccion","FechaTransaccion","FechaVencimiento"  
				,"FechaParidad","ValorParidad","ValorCaptacionMonedaOrigen","FolioComprobante","Tasa","Cantidad","Precio","Monto","MontoDescuento" 
				,"MontoInteres","Glosa","ValorRescate" ,"TasaAgente","Spread","DiasPlazo","CodigoCanalIngreso","DocumentoIdentidad" 
				,"TipoDocumentoIdentidad","NombreLargoCliente","DireccionFacturacion","NombreAgente","NombreSucursal","FolioCartera","CodigoCuentaInversion"
				,"CodigoTipoProducto","CodigoTipoOperacion","CodigoBolsa","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente","NombreClasificacionTipoInstrumento"
				,"NemotecnicoInstrumento","NemotecnicoSubyacente","RazonSocialEmisor","FechaEmision","TasaEmision","FechaIngresoRegistro","FechaSistema" 
				,"EsComprobIndividual","EsCuentaPropia","EsOrdenDirecta","EsEnvio","EsPrecioPrima","EsEnRueda","EsRuteo","EsIngresoAutomatico","IdUsuario" 
				,"IdUsuarioBloqueo","IdCarga","EsAPlazo","MontoAPlazo","PrecioAPlazo","IdMovimientoOdRef","IdMonedaSubyacente","IdMonedaLiquidacion"
				,"FechaTipoCambio","ValorTipoCambio","TasaAPlazo","EsVentaCorta","IdComprobanteRef","SecMovimientoFacturarMigracion"
			    FROM #t_MovtosFacturarOrigenRV;
			   -- WHERE NOT EXISTS (select 1 from "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 
			   --                   where "SecMovimientoFacturarMigracion" = #t_MovtosFacturarOrigenRV."SecMovimientoFacturarMigracion");
		END IF;  
		 
        drop table #t_MovtosFacturarOrigenRV;

          
	END;	
END;

 
