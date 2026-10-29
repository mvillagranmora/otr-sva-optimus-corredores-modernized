SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarMovimientoComprobante";
CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de MOVIMIENTOS A FACTURAR RV
--  renta variable
--  Autor: - EAR 
"BSCL.Homologacion.BD.Servicios::CargarMovimientoComprobante"(

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
	OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000))
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
	DECLARE v_errorhashmsg table ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(5000));
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
	   DECLARE v_total_mov_gestion_aux DECIMAL(26,8):=0;
	   DECLARE v_total_mov_cargadas DECIMAL(26,8):=0;
  
	   
	   DECLARE v_Quantity BIGINT:=0;

 	   -- RECUPERAMOS LOS VALORES A  CARGAR
       insert into "BSCL.Homologacion.BD::HOM.MovimientoComprobante"
       ("IdMovimientoComprobante","IdEmpresa","IdConceptoMovimiento","SecuenciaAgrupacion","IdCliente","IdPortafolio","IdAgente"
			,"IdSucursal","IdInstrumento","IdInstrumentoSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente","IdClasificacionTipoInstrumento"
			,"IdTipoProducto","IdMercado","IdCuentaInversion","IdMoneda","IdTipoComprobante","IdComprobante","IdTipoOperacion","IdMovimientoRef","IdBoveda"
			,"IdCorredoraContraparte","IdCondicionLiquidacion","FechaLiquidacion","IdBolsa","IdTipoReajuste","IdTipoConvenio"
			,"IdEstadoFacturacion","IdOrden","LineaOrden","IdAsignacion","IdTipoCanalIngreso","FechaOrden","IdTransaccion","FolioTransaccion","FechaTransaccion"
			,"FechaVencimiento","FechaParidad","ValorParidad","ValorCaptacionMonedaOrigen","FolioComprobante","Tasa","Cantidad","Precio","Monto","MontoDescuento"
			,"MontoInteres","Glosa","ValorRescate","TasaAgente","Spread","DiasPlazo","CodigoCanalIngreso","DocumentoIdentidad","TipoDocumentoIdentidad"
			,"NombreLargoCliente","DireccionFacturacion","NombreAgente","NombreSucursal","FolioCartera","CodigoCuentaInversion","CodigoTipoProducto"
			,"CodigoTipoOperacion","CodigoBolsa","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente","NombreClasificacionTipoInstrumento","NemotecnicoInstrumento"
			,"NemotecnicoSubyacente","RazonSocialEmisor","FechaEmision","TasaEmision","FechaIngresoRegistro","FechaSistema","EsComprobIndividual","EsCuentaPropia"
			,"EsOrdenDirecta","EsPrecioPrima","EsEnRueda","EsRuteo","EsIngresoAutomatico","IdUsuario","IdUsuarioBloqueo","IdCarga","EsAPlazo"
			,"MontoAPlazo","PrecioAPlazo","IdMovimientoOdRef","IdMonedaSubyacente","IdMonedaLiquidacion","ValorTipoCambio","EsVentaCorta","IdComprobanteRef","SecMovimientoFacturarMigracion"
			,"ActualizarCustodia","FechaMovimientoComprobante","FechaTipoCambio","ActualizarCustodiaRelacionado","IdMonedaMovimientoComprobante","MontoMovimientoComprobante"
			,"MontoLiquidacion","IdEmisor","EsFungible","IdFormaCustodia","ActualizarCuentaInversion","ActualizarTesoreria","IdInstrumentoRelacionado","IdTipoInstrumentoRelacionado"
			,"IdClasificacionTipoInstrumentoRelacionado","NombreTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumentoRelacionado","NemotecnicoRelacionado"
			,"IdAsignacionRef","RUT_CLI","SEC_RUT_CLI","TIPO_COMP_FAC","FOLIO_COMP_FAC","SEC_MOVTO_FAC"
	    )
		select 
					"BSCL.Facturacion.BD.Secuencias::MovimientoComprobante".NEXTVAL  as "IdMovimientoComprobante"
					,p_IdEmpresa as "IdEmpresa"
					,'' as "IdConceptoMovimiento"
					,0  as "SecuenciaAgrupacion"
					,CLIENTE."IdCliente" as "IdCliente"
					,PORTAFOLIO."IdPortafolio" as "IdPortafolio"
					,COALESCE(AGENTE."IdEmpleado",0) as "IdAgente"
					,COALESCE(SUCURSAL."IdSucursal",0) as "IdSucursal"
					,coalesce(INSTRUMENTO_SIM."IdInstrumento",INSTRUMENTO_VC."IdInstrumento",INSTRUMENTO."IdInstrumento") as "IdInstrumento"
					,CASE WHEN (INSTRUMENTO_SIM."IdInstrumento" IS NOT NULL OR INSTRUMENTO_VC."IdInstrumento" IS NOT NULL) THEN INSTRUMENTO."IdInstrumento" ELSE 0 END as "IdInstrumentoSubyacente"
					,coalesce(INSTRUMENTO_SIM."IdTipoInstrumento",INSTRUMENTO_VC."IdTipoInstrumento",INSTRUMENTO."IdTipoInstrumento") as "IdTipoInstrumento"
					,CASE WHEN (INSTRUMENTO_SIM."IdInstrumento" IS NOT NULL OR INSTRUMENTO_VC."IdInstrumento" IS NOT NULL) THEN INSTRUMENTO."IdTipoInstrumento" ELSE '' END as "IdTipoInstrumentoSubyacente"
					,'RV' as "IdClasificacionTipoInstrumento"
					,CASE WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('TP','TS'))) THEN 'INSI'
	    			      WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('')))  THEN TIPOPRODUCTO."ValorDestino"
							END as "IdTipoProducto"
					,'' as "IdMercado"
					,CUENTA_INVERSION."IdCuentaInversion"  as "IdCuentaInversion"-- LO SACARE DE LA MONEDA ORIGEN DE LA TABLA TBCTMFRV
					,CUENTA_INVERSION."IdMoneda" as "IdMoneda"--LO SACARE DE LA MONEDA ORIGEN DE LA TABLA TBCTMFRV en caso que sea blanco sera CLP
					,TIPOCOMPROBANTE."ValorDestino" as "IdTipoComprobante"
					,COALESCE(COMPROBANTE."IdComprobante",0) as "IdComprobante"-- debere actualizarlo cuando cree el comprobante
					,CASE WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRV."TIPO_OPERAC"='V' AND GES_TBCTMFRV."TIPO_COMP_FAC"='ND')) THEN 'ANSIMV'  
					 WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRV."TIPO_OPERAC"='C' AND GES_TBCTMFRV."TIPO_COMP_FAC"='ND')) THEN 'ANSIMC'  
					 WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRV."TIPO_OPERAC"='V' AND GES_TBCTMFRV."TIPO_COMP_FAC"='FD')) THEN 'VENSIM'
					 WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRV."TIPO_OPERAC"='C' AND GES_TBCTMFRV."TIPO_COMP_FAC"='FD')) THEN 'COMSIM'					 
					 WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('VC'))) THEN 'PRESTA'	
					 WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('')))  THEN TIPOOPERACION."ValorDestino"
					 END as "IdTipoOperacion"
					,0 as "IdMovimientoRef" -- CUANDO LAS OPERACIONES SON OD SIM O SIMULTANEOA OD  REVISAR
					,GES_TBCTMFRV."COD_BOVEDA" as "IdBoveda" -- REVISAR
					,GES_TBCTMFRV."COD_CORR_CONTRA" as "IdCorredoraContraparte" --debo actualizar u homologar
					,CONDICION_LIQUIDACION."ValorDestino" as "IdCondicionLiquidacion"
					,'0001-01-01' as "FechaLiquidacion" -- hay que revisar  quiza haya que hacer lo mismo que el la transaccion
					,GES_TBCTMFRV."IND_BOLSA" as "IdBolsa"
					,'' as "IdTipoReajuste"
					,'' as "IdTipoConvenio"
					,COALESCE(COMPROBANTE."IdEstadoComprobante",'') as "IdEstadoFacturacion" 
					,COALESCE(ASIGNACION."IdOrden",0) as "IdOrden"
					,GES_TBCTMFRV."LIN_ORDEN" as "LineaOrden"
					,COALESCE(ASIGNACION."IdAsignacion",0) as "IdAsignacion"
					,'' as "IdTipoCanalIngreso"
					,'0001-01-01' as "FechaOrden"
					,COALESCE(ASIGNACION."IdTransaccion",0) as "IdTransaccion"
					,COALESCE(ASIGNACION."FOLIO_TRANS",0) as "FolioTransaccion"
					,substring(GES_TBCTMFRV."FEC_TRANS",1,10)  as "FechaTransaccion"
					,'0001-01-01' as "FechaVencimiento"
					,'0001-01-01' as "FechaParidad"
					,0 as "ValorParidad"   ---- NO TENEMOS INFORMACION SIEMPRE DEBE SER 0
					,GES_TBCTMFRV."CAPTACION_MORIGEN" as "ValorCaptacionMonedaOrigen"
					,0 as "FolioComprobante"
					,GES_TBCTMFRV."TASA" as "Tasa"
					,GES_TBCTMFRV."CANTIDAD" as "Cantidad"
					,GES_TBCTMFRV."PRECIO" as "Precio"
					,GES_TBCTMFRV."MONTO" as "Monto"
					,0 as "MontoDescuento"
					,0 as "MontoInteres"
					,'' as "Glosa"
					,0 as "ValorRescate"
					,0 as "TasaAgente"
					,GES_TBCTMFRV."SPREAD" as "Spread"
					,0 as "DiasPlazo" --ANALIZAR
					,'' as "CodigoCanalIngreso" --- REVISAR
					,trim(GES_TBCTMFRV."RUT_DEST") as "DocumentoIdentidad"
					,'R' as "TipoDocumentoIdentidad"
					,PERSONA."Nombres"||' '||PERSONA."ApellidoPaterno"||' '||PERSONA."ApellidoMaterno" as "NombreLargoCliente"
					,COALESCE(DIRECCION."Calle",'') as "DireccionFacturacion"
					,PERSONA_AGENTE."Nombres"||' '||PERSONA_AGENTE."ApellidoPaterno"||' '||PERSONA_AGENTE."ApellidoMaterno"  as "NombreAgente"
					,SUCURSAL."NombreSucursal" as "NombreSucursal"
					,0  as "FolioCartera"
					,CUENTA_INVERSION."Codigo" as "CodigoCuentaInversion"
					,CASE WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('TP','TS'))) THEN 'INSI'  
	    			      WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('')))  THEN TIPOPRODUCTO."ValorDestino"
					END as "CodigoTipoProducto"
					,CASE WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRV."TIPO_OPERAC"='V' AND GES_TBCTMFRV."TIPO_COMP_FAC"='ND')) THEN 'ANSIMV'  
					 WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRV."TIPO_OPERAC"='C' AND GES_TBCTMFRV."TIPO_COMP_FAC"='ND')) THEN 'ANSIMC'  
					 WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRV."TIPO_OPERAC"='V' AND GES_TBCTMFRV."TIPO_COMP_FAC"='FD')) THEN 'VENSIM'
					 WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRV."TIPO_OPERAC"='C' AND GES_TBCTMFRV."TIPO_COMP_FAC"='FD')) THEN 'COMSIM'					 
					 WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('VC'))) THEN 'PRESTA'	
					 WHEN((GES_TBCTMFRV."IND_SIMUL" IN ('')))  THEN TIPOOPERACION."ValorDestino"
					 END as "CodigoTipoOperacion"
					,GES_TBCTMFRV."IND_BOLSA"  as "CodigoBolsa"
                    ,COALESCE(TIPOINSTRUMENTO_SIM."Nombre",TIPOINSTRUMENTO_VC."Nombre",TIPOINSTRUMENTO."Nombre") AS "NombreTipoInstrumento"
					,CASE WHEN (TIPOINSTRUMENTO_SIM."IdTipoInstrumento" IS NOT NULL OR TIPOINSTRUMENTO_VC."IdTipoInstrumento" IS NOT NULL) THEN TIPOINSTRUMENTO."Nombre" ELSE '' END as "NombreTipoInstrumentoSubyacente"
					,'RENTA VARIABLE' as "NombreClasificacionTipoInstrumento"
					,COALESCE(INSTRUMENTO_SIM."Nemotecnico",INSTRUMENTO_VC."Nemotecnico",INSTRUMENTO."Nemotecnico")as "NemotecnicoInstrumento"
					,CASE WHEN (INSTRUMENTO_SIM."Nemotecnico" IS NOT NULL OR INSTRUMENTO_VC."Nemotecnico" IS NOT NULL) THEN INSTRUMENTO."Nemotecnico" ELSE '' END as "NemotecnicoSubyacente"
					,INSTRUMENTO."IdEmisor" as "RazonSocialEmisor"
					,'0001-01-01' as "FechaEmision"
					,0 as "TasaEmision"
					,GES_TBCTMFRV."FEC_MVTO" as "FechaIngresoRegistro"
					,'0001-01-01' as "FechaSistema"
					,GES_TBCTMFRV."IND_COMP_INDIV" as "EsComprobIndividual"
					,CASE WHEN GES_TBCTMFRV."IND_CTA_PRO" ='P' THEN 'S' ELSE 'N' END as "EsCuentaPropia"
					,CASE WHEN TRIM(GES_TBCTMFRV."IND_OD")<>'' THEN 'S' ELSE 'N' END as "EsOrdenDirecta"
					,'' as "EsPrecioPrima"
					,'S' as "EsEnRueda"
					,'' as "EsRuteo"
					,'' as "EsIngresoAutomatico"
					,0 as "IdUsuario"
					,0 as "IdUsuarioBloqueo"
					,p_IdCarga as "IdCarga"
					,'' as "EsAPlazo"
					,0 as "MontoAPlazo"
					,0 as "PrecioAPlazo"
					,0 as "IdMovimientoOdRef"
					,GES_TBCTMFRV."MON_MORIGEN" as "IdMonedaSubyacente"
					,GES_TBCTMFRV."MON_MORIGEN" as "IdMonedaLiquidacion"
					,0 as "ValorTipoCambio"
					,CASE WHEN GES_TBCTMFRV."IND_SIMUL" = 'VC'  THEN 'S'  ELSE 'N' END as "EsVentaCorta"
					,0 as "IdComprobanteRef"
					,GES_TBCTMFRV."SEC_MOVTO_FAC" as "SecMovimientoFacturarMigracion"
					,CASE WHEN GES_TBCTMFRV."IND_TIT_CUS" ='C' THEN 'S' 
						  WHEN GES_TBCTMFRV."IND_TIT_CUS" ='N' THEN 'N' 
						  WHEN GES_TBCTMFRV."IND_TIT_CUS" =''  THEN 'S' 
				     END  as "ActualizarCustodia"			  
					,GES_TBCTMFRV."FEC_MVTO" as "FechaMovimientoComprobante"
					,'0001-01-01' as "FechaTipoCambio"
					,'' as "ActualizarCustodiaRelacionado"
					,GES_TBCTMFRV."MON_MORIGEN"  as "IdMonedaMovimientoComprobante"
					,GES_TBCTMFRV."MONTO" as "MontoMovimientoComprobante"
					,GES_TBCTMFRV."MONTO" as "MontoLiquidacion"
					,''  as "IdEmisor"
					,CASE WHEN GES_TBCTMFRV."IND_TIT_CUS" ='C' THEN 'S' 
						  WHEN GES_TBCTMFRV."IND_TIT_CUS" ='N' THEN 'N' 
						  WHEN GES_TBCTMFRV."IND_TIT_CUS" =''  THEN 'S'
				     END as "EsFungible"
					,CASE WHEN GES_TBCTMFRV."IND_TIT_CUS" ='C' THEN 'CUS' 
						  WHEN GES_TBCTMFRV."IND_TIT_CUS" ='N' THEN 'NOM' 
						  WHEN GES_TBCTMFRV."IND_TIT_CUS" =''  THEN 'CUS'
				      END as "IdFormaCustodia"
					,'S' as "ActualizarCuentaInversion"
					,'S' as "ActualizarTesoreria"
					,0  as "IdInstrumentoRelacionado"
					,'' as "IdTipoInstrumentoRelacionado"
					,'' as "IdClasificacionTipoInstrumentoRelacionado"
					,'' as "NombreTipoInstrumentoRelacionado"
					,'' as "NombreClasificacionTipoInstrumentoRelacionado"
					,'' as "NemotecnicoRelacionado" 
					,0  as "IdAsignacionRef"
					,GES_TBCTMFRV."RUT_DEST" as "RUT_CLI"
					,GES_TBCTMFRV."SEC_RUT_DEST" as "SEC_RUT_CLI"
					,GES_TBCTMFRV."TIPO_COMP_FAC" as "TIPO_COMP_FAC"
					,GES_TBCTMFRV."FOLIO_COMP_FAC" as "FOLIO_COMP_FAC"
					,GES_TBCTMFRV."SEC_MOVTO_FAC" as "SEC_MOVTO_FAC"
                FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
   				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"		
				INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		                             	   ELSE CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'V' THEN 'EUR' ELSE 'CLP' END
	  	   		                                 END
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRV."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRV."IND_LIQUID" END
                INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" as ASIGNACION
	  	   			ON ASIGNACION."SEC_MOVTO_FAC" = GES_TBCTMFRV."SEC_MOVTO_FAC"
	  	   		INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFRV."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = TRIM(GES_TBCTMFRV."MERCADO")
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFRV."PRODUCTO")
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFRV."TIPO_COMP_FAC")	
			    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFRV."PRODUCTO" 
			   	INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO 
                	ON INSTRUMENTO."Nemotecnico"  = GES_TBCTMFRV."NEMO"    
			   	INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO
			   		ON TIPOINSTRUMENTO."IdTipoInstrumento" =  INSTRUMENTO."IdTipoInstrumento"
			   	INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTE
			  		ON  TIPOCOMPROBANTE."TipoValor" = 'TipoComprobante' 
			  		AND TIPOCOMPROBANTE."ValorOrigen" = GES_TBCTMFRV."TIPO_COMP_FAC"
               	LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO_SIM
		    		ON INSTRUMENTO_SIM."IdInstrumento" = CASE WHEN (GES_TBCTMFRV."IND_SIMUL" = 'TS' OR GES_TBCTMFRV."IND_SIMUL" = 'TP') THEN
		            	                                   CASE WHEN  GES_TBCTMFRV."TIPO_OPERAC" ='V' THEN 
		                	                                     2249
		                    	                           ELSE 
		                                                   		 2248
		                                               	   END
		                                           				ELSE -1
		                                          		   END
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO_SIM
		        	ON TIPOINSTRUMENTO_SIM."IdTipoInstrumento" =  INSTRUMENTO_SIM."IdTipoInstrumento"
		        	
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO_VC  
		        	ON INSTRUMENTO_VC."IdInstrumento" = CASE WHEN GES_TBCTMFRV."IND_SIMUL" = 'VC' THEN
		            	                                          10010
		                                           				ELSE -1
		                                          		 END
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO_VC
		        	ON TIPOINSTRUMENTO_VC."IdTipoInstrumento" =  INSTRUMENTO_VC."IdTipoInstrumento"
		        	
		        LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as AGENTE
		            ON AGENTE."CodigoAgenteMigracion" =  GES_TBCTMFRV."COD_AGENTE"
		        LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA_AGENTE
		        	ON PERSONA_AGENTE."IdPersona" = AGENTE."IdPersona"
		        LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica"  AS DIRECCION
		        	ON DIRECCION."IdDireccionFisica" = PERSONA."IdDireccionFisicaPrincipal"
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Sucursal" as SUCURSAL
		            ON SUCURSAL."CodigoSucursal" =  GES_TBCTMFRV."COD_SUC"
		        LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" AS COMPROBANTE
		        	ON COMPROBANTE."TIPO_COMP_FAC" = GES_TBCTMFRV."TIPO_COMP_FAC"
		        	AND COMPROBANTE."FOLIO_COMP_FAC" = GES_TBCTMFRV."FOLIO_COMP_FAC" 
 	   			LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;
	      
	      -- log movimientos a facturar
	  	  -- todas las operaciones deben cruzar con un documento identidad
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERV rut_cli no tiene DOCUMENTO IDENTIDAD OPTIMUS' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , coalesce(TRIM(GES_TBCTMFRV."RUT_DEST"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'SEC_MOVTO_FAC' as "NombreCampoGenerico8"
                    ,GES_TBCTMFRV."SEC_MOVTO_FAC" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL
	  	   		     AND DOCUMENTO."DocumentoIdentidad" IS NULL;

                
	        -- todo documento identidad debe estar asociado a una persona    
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERV DOCUMENTO IDENTIDAD no tiene PERSONA asociada en optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , coalesce(TRIM(GES_TBCTMFRV."RUT_DEST"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdDocumentoIdentidad' as "NombreCampoGenerico6"
                    , coalesce(DOCUMENTO."IdDocumentoIdentidad",0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
                LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL
	  	   		     AND PERSONA."IdDocumentoIdentidad" IS NULL;
	  	   		  
	  	   	  -- todo persona que tenga una factura debe tener un cliente
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERV DOCUMENTO IDENTIDAD no tiene PERSONA asociada en optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , coalesce(TRIM(GES_TBCTMFRV."RUT_DEST"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdDocumentoIdentidad' as "NombreCampoGenerico6"
                    , coalesce(DOCUMENTO."IdDocumentoIdentidad",0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			LEFT JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"	 
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL
	  	   		     AND CLIENTE."IdPersona" IS NULL;	  
	  	   		  
             -- toda persona debe tener portafolio

			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERV CLIENTE no tiene la CUENTA SEBRA en PORTAFOLIO optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , coalesce(TRIM(GES_TBCTMFRV."RUT_DEST"),'') as "ValorCampoGenerico1"
                    , 'SEC_RUT_DEST' as "NombreCampoGenerico2"
                    , coalesce(TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST"),'') as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , 'CodigoPortafolio' as "NombreCampoGenerico5"
                    , coalesce(TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST"),'') as "ValorCampoGenerico5"
                    ,'IdCliente' as "NombreCampoGenerico6"
                    , coalesce(TO_VARCHAR(CLIENTE."IdCliente"),0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    		    INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST")
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND PORTAFOLIO."IdCliente" IS NULL 
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;
	  	   		     
	  	   	 -- los portafolio deben tener creadas las cuentas de inversion    

			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERV PORTAFOLIO no tiene creada la CUENTA INVERSION optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , coalesce(TRIM(GES_TBCTMFRV."RUT_DEST"),'') as "ValorCampoGenerico1"
                    , 'SEC_RUT_DEST' as "NombreCampoGenerico2"
                    , coalesce(TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST"),'') as "ValorCampoGenerico2"
                    , 'IND_TIT_CCTE' as "NombreCampoGenerico3"
                    ,  GES_TBCTMFRV."IND_TIT_CCTE" as "ValorCampoGenerico3"
                    , 'IdMoneda' as "NombreCampoGenerico4"
                    , CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		                             	   ELSE CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'V' THEN 'EUR' ELSE 'CLP' END
	  	   		      END	 as "ValorCampoGenerico4"
                    ,'CodigoPortafolio' as "NombreCampoGenerico5"
                    , coalesce(TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST"),'') as "ValorCampoGenerico5"
                    ,'IdCliente' as "NombreCampoGenerico6"
                    , coalesce(TO_VARCHAR(CLIENTE."IdCliente"),0) as "ValorCampoGenerico6"
                    ,'IdPortafolio' as "NombreCampoGenerico7"
                    , coalesce(TO_VARCHAR(PORTAFOLIO."IdPortafolio"),0) as "ValorCampoGenerico7"
                    ,'SEC_MOVTO_FAC' as "NombreCampoGenerico8"
                    ,GES_TBCTMFRV."SEC_MOVTO_FAC" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST")
				LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		                             	   ELSE CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'V' THEN 'EUR' ELSE 'CLP' END
	  	   		                                 END	
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND CUENTA_INVERSION."IdCliente" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;     
	  	   		     
	  	   		--- deben tener la condicion liquidacion homologada
	  	   		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERV CONDICION LIQUIDACION SEBRA  no esta HOMOLOGADA' as "Mensaje"
                    , 'IND_LIQUID' as "NombreCampoGenerico1"
                    , coalesce(GES_TBCTMFRV."IND_LIQUID",'') as "ValorCampoGenerico1"
                    , 'TipoValor' as "NombreCampoGenerico2"
                    , 'CondicionLiquidacion' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRV."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		                             	   ELSE CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'V' THEN 'EUR' ELSE 'CLP' END
	  	   		                                 END
	  	   		LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRV."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRV."IND_LIQUID" END
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND CONDICION_LIQUIDACION."TipoValor" IS NULL 
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;
	  	   		     
	  	   		
	  	   	--- debe tener cargada la asignacion
	  	   		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERV no tiene asociada UNA ASIGNACION' as "Mensaje"
                    , '' as "NombreCampoGenerico1"
                    , '' as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRV."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		                             	   ELSE CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'V' THEN 'EUR' ELSE 'CLP' END
	  	   		                                 END
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRV."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRV."IND_LIQUID" END
                LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" as ASIGNACION
	  	   			ON ASIGNACION."SEC_MOVTO_FAC" = GES_TBCTMFRV."SEC_MOVTO_FAC"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND ASIGNACION."SEC_MOVTO_FAC" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;  
	  	   		     
	  	   	  -- el tipo de operacion debe estar homologado
	  	   	  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERV el tipo de operacion no esta homologacion' as "Mensaje"
                    , 'COD_MOVTO'  as "NombreCampoGenerico1"
                    , GES_TBCTMFRV."COD_MOVTO" as "ValorCampoGenerico1"
                    , 'MERCADO'as "NombreCampoGenerico2"
                    , GES_TBCTMFRV."MERCADO" as "ValorCampoGenerico2"
                    , 'PRODUCTO' as "NombreCampoGenerico3"
                    , GES_TBCTMFRV."PRODUCTO" as "ValorCampoGenerico3"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico4"
                    , GES_TBCTMFRV."TIPO_COMP_FAC" as "ValorCampoGenerico4"
                    , 'TipoValor' as "NombreCampoGenerico5"
                    , 'TipoOperacionFacturacion' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRV."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		                             	   ELSE CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'V' THEN 'EUR' ELSE 'CLP' END
	  	   		                                 END
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRV."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRV."IND_LIQUID" END
                LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFRV."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = TRIM(GES_TBCTMFRV."MERCADO")
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFRV."PRODUCTO")
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFRV."TIPO_COMP_FAC")	
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND TIPOOPERACION."TipoValor" IS NULL 
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;  

			 -- el tipo producto debe estar homologado
	  	   	  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERV no tiene asociada UNA ASIGNACION' as "Mensaje"
                    , 'TipoValor'  as "NombreCampoGenerico1"
                    , 'TipoProducto' as "ValorCampoGenerico1"
                    , 'PRODUCTO'  as "NombreCampoGenerico2"
                    ,  GES_TBCTMFRV."PRODUCTO"  as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRV."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"	 
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		                             	   ELSE CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'V' THEN 'EUR' ELSE 'CLP' END
	  	   		                                 END
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRV."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRV."IND_LIQUID" END
                INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFRV."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = TRIM(GES_TBCTMFRV."MERCADO")
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFRV."PRODUCTO")
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFRV."TIPO_COMP_FAC")
			    LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFRV."PRODUCTO"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND TIPOPRODUCTO."TipoValor" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL; 
	  	   		
	  	     -- el instrumento debe existir 
	  	   	  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERV el instrumento no existe' as "Mensaje"
                    , 'NEMO'  as "NombreCampoGenerico1"
                    , coalesce(trim(GES_TBCTMFRV."NEMO"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRV."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		                             	   ELSE CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'V' THEN 'EUR' ELSE 'CLP' END
	  	   		                                 END
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRV."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRV."IND_LIQUID" END
                INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFRV."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = TRIM(GES_TBCTMFRV."MERCADO")
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFRV."PRODUCTO")
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFRV."TIPO_COMP_FAC")
			    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFRV."PRODUCTO"
			  	LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO 
                	ON INSTRUMENTO."Nemotecnico"  = trim(GES_TBCTMFRV."NEMO")
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND INSTRUMENTO."Nemotecnico" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;      
	  	   		-- el tipo instrumento asociado al instrumento debe existir
                
             -- el tipo instrumento debe existir 
	  	   	  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERV el TIPOINSTRUMENTO asociado al INSTRUMENTO no existe' as "Mensaje"
                    , 'NEMO'  as "NombreCampoGenerico1"
                    , coalesce(trim(GES_TBCTMFRV."NEMO"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRV."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as GES_TBCTMFRV
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRV."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"	 
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		                             	   ELSE CASE WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'V' THEN 'EUR' ELSE 'CLP' END
	  	   		                                 END
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRV."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRV."IND_LIQUID" END
                INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFRV."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = TRIM(GES_TBCTMFRV."MERCADO")
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFRV."PRODUCTO")
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFRV."TIPO_COMP_FAC")
			    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFRV."PRODUCTO"
			  	INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO 
                	ON INSTRUMENTO."Nemotecnico"  = trim(GES_TBCTMFRV."NEMO")
                LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO
			   		ON TIPOINSTRUMENTO."IdTipoInstrumento" =  INSTRUMENTO."IdTipoInstrumento"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRV."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRV."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRV."FEC_MVTO" <=P_FechaFinal
	  	   		     AND TIPOINSTRUMENTO."IdTipoInstrumento" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;
	
	
	
	-- mercado IF 
       insert into "BSCL.Homologacion.BD::HOM.MovimientoComprobante"
       ("IdMovimientoComprobante","IdEmpresa","IdConceptoMovimiento","SecuenciaAgrupacion","IdCliente","IdPortafolio","IdAgente"
			,"IdSucursal","IdInstrumento","IdInstrumentoSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente","IdClasificacionTipoInstrumento"
			,"IdTipoProducto","IdMercado","IdCuentaInversion","IdMoneda","IdTipoComprobante","IdComprobante","IdTipoOperacion","IdMovimientoRef","IdBoveda"
			,"IdCorredoraContraparte","IdCondicionLiquidacion","FechaLiquidacion","IdBolsa","IdTipoReajuste","IdTipoConvenio"
			,"IdEstadoFacturacion","IdOrden","LineaOrden","IdAsignacion","IdTipoCanalIngreso","FechaOrden","IdTransaccion","FolioTransaccion","FechaTransaccion"
			,"FechaVencimiento","FechaParidad","ValorParidad","ValorCaptacionMonedaOrigen","FolioComprobante","Tasa","Cantidad","Precio","Monto","MontoDescuento"
			,"MontoInteres","Glosa","ValorRescate","TasaAgente","Spread","DiasPlazo","CodigoCanalIngreso","DocumentoIdentidad","TipoDocumentoIdentidad"
			,"NombreLargoCliente","DireccionFacturacion","NombreAgente","NombreSucursal","FolioCartera","CodigoCuentaInversion","CodigoTipoProducto"
			,"CodigoTipoOperacion","CodigoBolsa","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente","NombreClasificacionTipoInstrumento","NemotecnicoInstrumento"
			,"NemotecnicoSubyacente","RazonSocialEmisor","FechaEmision","TasaEmision","FechaIngresoRegistro","FechaSistema","EsComprobIndividual","EsCuentaPropia"
			,"EsOrdenDirecta","EsPrecioPrima","EsEnRueda","EsRuteo","EsIngresoAutomatico","IdUsuario","IdUsuarioBloqueo","IdCarga","EsAPlazo"
			,"MontoAPlazo","PrecioAPlazo","IdMovimientoOdRef","IdMonedaSubyacente","IdMonedaLiquidacion","ValorTipoCambio","EsVentaCorta","IdComprobanteRef","SecMovimientoFacturarMigracion"
			,"ActualizarCustodia","FechaMovimientoComprobante","FechaTipoCambio","ActualizarCustodiaRelacionado","IdMonedaMovimientoComprobante","MontoMovimientoComprobante"
			,"MontoLiquidacion","IdEmisor","EsFungible","IdFormaCustodia","ActualizarCuentaInversion","ActualizarTesoreria","IdInstrumentoRelacionado","IdTipoInstrumentoRelacionado"
			,"IdClasificacionTipoInstrumentoRelacionado","NombreTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumentoRelacionado","NemotecnicoRelacionado"
			,"IdAsignacionRef","RUT_CLI","SEC_RUT_CLI","TIPO_COMP_FAC","FOLIO_COMP_FAC","SEC_MOVTO_FAC"
	    )
		select 
					"BSCL.Facturacion.BD.Secuencias::MovimientoComprobante".NEXTVAL  as "IdMovimientoComprobante"
					,p_IdEmpresa as "IdEmpresa"
					,'' as "IdConceptoMovimiento"
					,0  as "SecuenciaAgrupacion"
					,CLIENTE."IdCliente" as "IdCliente"
					,PORTAFOLIO."IdPortafolio" as "IdPortafolio"
					,COALESCE(AGENTE."IdEmpleado",0) as "IdAgente"
					,COALESCE(SUCURSAL."IdSucursal",0) as "IdSucursal"
					,coalesce(INSTRUMENTO_SIM."IdInstrumento",INSTRUMENTO_VC."IdInstrumento",INSTRUMENTO."IdInstrumento") as "IdInstrumento"
					,CASE WHEN (INSTRUMENTO_SIM."IdInstrumento" IS NOT NULL OR INSTRUMENTO_VC."IdInstrumento" IS NOT NULL) THEN INSTRUMENTO."IdInstrumento" ELSE 0 END as "IdInstrumentoSubyacente"
					,coalesce(INSTRUMENTO_SIM."IdTipoInstrumento",INSTRUMENTO_VC."IdTipoInstrumento",INSTRUMENTO."IdTipoInstrumento") as "IdTipoInstrumento"
					,CASE WHEN (INSTRUMENTO_SIM."IdInstrumento" IS NOT NULL OR INSTRUMENTO_VC."IdInstrumento" IS NOT NULL) THEN INSTRUMENTO."IdTipoInstrumento" ELSE '' END as "IdTipoInstrumentoSubyacente"
					,'IF' as "IdClasificacionTipoInstrumento"
					,CASE WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('TP','TS'))) THEN 'INSI'
	    			      WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('')))  THEN TIPOPRODUCTO."ValorDestino"
							END as "IdTipoProducto"
					,'' as "IdMercado"
					,CUENTA_INVERSION."IdCuentaInversion"  as "IdCuentaInversion"-- LO SACARE DE LA MONEDA ORIGEN DE LA TABLA TBCTMFRV
					,CUENTA_INVERSION."IdMoneda" as "IdMoneda"--LO SACARE DE LA MONEDA ORIGEN DE LA TABLA TBCTMFRV en caso que sea blanco sera CLP
					,TIPOCOMPROBANTE."ValorDestino" as "IdTipoComprobante"
					,COALESCE(COMPROBANTE."IdComprobante",0) as "IdComprobante"-- debere actualizarlo cuando cree el comprobante
					,CASE WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFIF."TIPO_OPERAC"='V' AND GES_TBCTMFIF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMV'  
					 WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFIF."TIPO_OPERAC"='C' AND GES_TBCTMFIF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMC'  
					 WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFIF."TIPO_OPERAC"='V' AND GES_TBCTMFIF."TIPO_COMP_FAC"='FD')) THEN 'VENSIM'
					 WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFIF."TIPO_OPERAC"='C' AND GES_TBCTMFIF."TIPO_COMP_FAC"='FD')) THEN 'COMSIM'					 
					 WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('VC'))) THEN 'PRESTA'	
					 WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('')))  THEN TIPOOPERACION."ValorDestino"
					 END as "IdTipoOperacion"
					,0 as "IdMovimientoRef" -- CUANDO LAS OPERACIONES SON OD SIM O SIMULTANEOA OD  REVISAR
					,GES_TBCTMFIF."COD_BOVEDA" as "IdBoveda" -- REVISAR
					,GES_TBCTMFIF."COD_CORR_CONTRA" as "IdCorredoraContraparte" --debo actualizar u homologar
					,CONDICION_LIQUIDACION."ValorDestino" as "IdCondicionLiquidacion"
					,'0001-01-01' as "FechaLiquidacion" -- hay que revisar  quiza haya que hacer lo mismo que el la transaccion
					,GES_TBCTMFIF."IND_BOLSA" as "IdBolsa"
					,TIPOREAJUSTE."ValorDestino" as "IdTipoReajuste"
					,'SCV' as "IdTipoConvenio"
					,COALESCE(COMPROBANTE."IdEstadoComprobante",'') as "IdEstadoFacturacion" 
					,COALESCE(ASIGNACION."IdOrden",0) as "IdOrden"
					,GES_TBCTMFIF."LIN_ORDEN" as "LineaOrden"
					,COALESCE(ASIGNACION."IdAsignacion",0) as "IdAsignacion"
					,'' as "IdTipoCanalIngreso"
					,GES_TBCTMFIF."FEC_ORDEN" as "FechaOrden"
					,COALESCE(ASIGNACION."IdTransaccion",0) as "IdTransaccion"
					,COALESCE(ASIGNACION."FOLIO_TRANS",0) as "FolioTransaccion"
					,substring(GES_TBCTMFIF."FEC_TRANS",1,10)  as "FechaTransaccion"
					,GES_TBCTMFIF."FEC_VCTO" as "FechaVencimiento"
					,'0001-01-01' as "FechaParidad"
					,0 as "ValorParidad"   ---- NO TENEMOS INFORMACION SIEMPRE DEBE SER 0
					,GES_TBCTMFIF."CAPTACION_MORIGEN" as "ValorCaptacionMonedaOrigen"
					,0 as "FolioComprobante"
					,GES_TBCTMFIF."TASA" as "Tasa"
					,GES_TBCTMFIF."CANTIDAD" as "Cantidad"
					,0 as "Precio"
					,GES_TBCTMFIF."MONTO" as "Monto"
					,0 as "MontoDescuento"
					,0 as "MontoInteres"
					,'' as "Glosa"
					,GES_TBCTMFIF."VAL_RESC" as "ValorRescate"
					,0 as "TasaAgente"
					,GES_TBCTMFIF."SPREAD_TASA_EMISION" as "Spread"
					,0 as "DiasPlazo" --ANALIZAR
					,'' as "CodigoCanalIngreso" --- REVISAR
					,trim(GES_TBCTMFIF."RUT_DEST") as "DocumentoIdentidad"
					,'R' as "TipoDocumentoIdentidad"
					,PERSONA."Nombres"||' '||PERSONA."ApellidoPaterno"||' '||PERSONA."ApellidoMaterno" as "NombreLargoCliente"
					,COALESCE(DIRECCION."Calle",'') as "DireccionFacturacion"
					,PERSONA_AGENTE."Nombres"||' '||PERSONA_AGENTE."ApellidoPaterno"||' '||PERSONA_AGENTE."ApellidoMaterno"  as "NombreAgente"
					,SUCURSAL."NombreSucursal" as "NombreSucursal"
					,0  as "FolioCartera"
					,CUENTA_INVERSION."Codigo" as "CodigoCuentaInversion"
					,CASE WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('TP','TS'))) THEN 'INSI'  
	    			      WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('')))  THEN TIPOPRODUCTO."ValorDestino"
					END as "CodigoTipoProducto"
					,CASE WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFIF."TIPO_OPERAC"='V' AND GES_TBCTMFIF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMV'  
					 WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFIF."TIPO_OPERAC"='C' AND GES_TBCTMFIF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMC'  
					 WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFIF."TIPO_OPERAC"='V' AND GES_TBCTMFIF."TIPO_COMP_FAC"='FD')) THEN 'VENSIM'
					 WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFIF."TIPO_OPERAC"='C' AND GES_TBCTMFIF."TIPO_COMP_FAC"='FD')) THEN 'COMSIM'					 
					 WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('VC'))) THEN 'PRESTA'	
					 WHEN((GES_TBCTMFIF."IND_SIMUL" IN ('')))  THEN TIPOOPERACION."ValorDestino"
					 END as "CodigoTipoOperacion"
					,GES_TBCTMFIF."IND_BOLSA"  as "CodigoBolsa"
                    ,COALESCE(TIPOINSTRUMENTO_SIM."Nombre",TIPOINSTRUMENTO_VC."Nombre",TIPOINSTRUMENTO."Nombre") AS "NombreTipoInstrumento"
					,CASE WHEN (TIPOINSTRUMENTO_SIM."IdTipoInstrumento" IS NOT NULL OR TIPOINSTRUMENTO_VC."IdTipoInstrumento" IS NOT NULL) THEN TIPOINSTRUMENTO."Nombre" ELSE '' END as "NombreTipoInstrumentoSubyacente"
					,'INTERMEDIACION FINANCIERA' as "NombreClasificacionTipoInstrumento"
					,COALESCE(INSTRUMENTO_SIM."Nemotecnico",INSTRUMENTO_VC."Nemotecnico",INSTRUMENTO."Nemotecnico")as "NemotecnicoInstrumento"
					,CASE WHEN (INSTRUMENTO_SIM."Nemotecnico" IS NOT NULL OR INSTRUMENTO_VC."Nemotecnico" IS NOT NULL) THEN INSTRUMENTO."Nemotecnico" ELSE '' END as "NemotecnicoSubyacente"
					,GES_TBCTMFIF."EMISOR" as "RazonSocialEmisor"
					,GES_TBCTMFIF."FEC_EMI" as "FechaEmision"
					,GES_TBCTMFIF."TASA_EMISION" as "TasaEmision"
					,GES_TBCTMFIF."FEC_MVTO" as "FechaIngresoRegistro"
					,'0001-01-01' as "FechaSistema"
					,GES_TBCTMFIF."IND_COMP_INDIV" as "EsComprobIndividual"
					,CASE WHEN GES_TBCTMFIF."IND_CTA_PRO" ='P' THEN 'S' ELSE 'N' END as "EsCuentaPropia"
					,CASE WHEN TRIM(GES_TBCTMFIF."IND_OD")<>'' THEN 'S' ELSE 'N' END as "EsOrdenDirecta"
					,'' as "EsPrecioPrima"
					,'S' as "EsEnRueda"
					,'' as "EsRuteo"
					,'' as "EsIngresoAutomatico"
					,0 as "IdUsuario"
					,0 as "IdUsuarioBloqueo"
					,p_IdCarga as "IdCarga"
					,'' as "EsAPlazo"
					,0 as "MontoAPlazo"
					,0 as "PrecioAPlazo"
					,0 as "IdMovimientoOdRef"
					,GES_TBCTMFIF."MON_MORIGEN" as "IdMonedaSubyacente"
					,GES_TBCTMFIF."MON_MORIGEN" as "IdMonedaLiquidacion"
					,0 as "ValorTipoCambio"
					,CASE WHEN GES_TBCTMFIF."IND_SIMUL" = 'VC'  THEN 'S'  ELSE 'N' END as "EsVentaCorta"
					,0 as "IdComprobanteRef"
					,GES_TBCTMFIF."SEC_MOVTO_FAC" as "SecMovimientoFacturarMigracion"
					,CASE WHEN GES_TBCTMFIF."IND_TIT_CUS" ='C' THEN 'S' 
						  WHEN GES_TBCTMFIF."IND_TIT_CUS" ='N' THEN 'N' 
						  WHEN GES_TBCTMFIF."IND_TIT_CUS" =''  THEN 'S' 
				     END  as "ActualizarCustodia"			  
					,GES_TBCTMFIF."FEC_MVTO" as "FechaMovimientoComprobante"
					,'0001-01-01' as "FechaTipoCambio"
					,'' as "ActualizarCustodiaRelacionado"
					,GES_TBCTMFIF."MON_MORIGEN"  as "IdMonedaMovimientoComprobante"
					,GES_TBCTMFIF."MONTO" as "MontoMovimientoComprobante"
					,GES_TBCTMFIF."MONTO" as "MontoLiquidacion"
					,GES_TBCTMFIF."EMISOR" as "IdEmisor"
					,CASE WHEN GES_TBCTMFIF."IND_TIT_CUS" ='C' THEN 'S' 
						  WHEN GES_TBCTMFIF."IND_TIT_CUS" ='N' THEN 'N' 
						  WHEN GES_TBCTMFIF."IND_TIT_CUS" =''  THEN 'S'
				     END as "EsFungible"
					,CASE WHEN GES_TBCTMFIF."IND_TIT_CUS" ='C' THEN 'CUS' 
						  WHEN GES_TBCTMFIF."IND_TIT_CUS" ='N' THEN 'NOM' 
						  WHEN GES_TBCTMFIF."IND_TIT_CUS" =''  THEN 'CUS'
				      END as "IdFormaCustodia"
					,'S' as "ActualizarCuentaInversion"
					,'S' as "ActualizarTesoreria"
					,0  as "IdInstrumentoRelacionado"
					,'' as "IdTipoInstrumentoRelacionado"
					,'' as "IdClasificacionTipoInstrumentoRelacionado"
					,'' as "NombreTipoInstrumentoRelacionado"
					,'' as "NombreClasificacionTipoInstrumentoRelacionado"
					,'' as "NemotecnicoRelacionado"
					,0  as "IdAsignacionRef"
					,GES_TBCTMFIF."RUT_DEST" as "RUT_CLI"
					,GES_TBCTMFIF."SEC_RUT_DEST" as "SEC_RUT_CLI"
					,GES_TBCTMFIF."TIPO_COMP_FAC" as "TIPO_COMP_FAC"
					,GES_TBCTMFIF."FOLIO_COMP_FAC" as "FOLIO_COMP_FAC"
					,GES_TBCTMFIF."SEC_MOVTO_FAC" as "SEC_MOVTO_FAC"
                FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
   				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"		
				INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFIF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFIF."IND_LIQUID" END
                INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" as ASIGNACION
	  	   			ON ASIGNACION."SEC_MOVTO_FAC" = GES_TBCTMFIF."SEC_MOVTO_FAC"
	  	   		INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFIF."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = 'IF'
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFIF."PRODUCTO")	
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFIF."TIPO_COMP_FAC")	
			    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFIF."PRODUCTO" 
			   	INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO 
                	ON INSTRUMENTO."Nemotecnico"  = GES_TBCTMFIF."NEMO"    
			   	INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO
			   		ON TIPOINSTRUMENTO."IdTipoInstrumento" =  INSTRUMENTO."IdTipoInstrumento"
			   	INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTE
					ON  TIPOCOMPROBANTE."TipoValor" = 'TipoComprobante' 
					AND TIPOCOMPROBANTE."ValorOrigen" = GES_TBCTMFIF."TIPO_COMP_FAC"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOREAJUSTE
					ON  TIPOREAJUSTE."TipoValor" = 'TipoReajuste' 
					AND TIPOREAJUSTE."ValorOrigen" = GES_TBCTMFIF."TIPO_REAJ"
               	LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO_SIM
		    		ON INSTRUMENTO_SIM."IdInstrumento" = CASE WHEN (GES_TBCTMFIF."IND_SIMUL" = 'TS' OR GES_TBCTMFIF."IND_SIMUL" = 'TP') THEN
		            	                                   CASE WHEN  GES_TBCTMFIF."TIPO_OPERAC" ='V' THEN 
		                	                                     2249
		                    	                           ELSE 
		                                                   		 2248
		                                               	   END
		                                           				ELSE -1
		                                          		   END
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO_SIM
		        	ON TIPOINSTRUMENTO_SIM."IdTipoInstrumento" =  INSTRUMENTO_SIM."IdTipoInstrumento"
		        	
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO_VC  
		        	ON INSTRUMENTO_VC."IdInstrumento" = CASE WHEN GES_TBCTMFIF."IND_SIMUL" = 'VC' THEN
		            	                                          10010
		                                           				ELSE -1
		                                          		 END
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO_VC
		        	ON TIPOINSTRUMENTO_VC."IdTipoInstrumento" =  INSTRUMENTO_VC."IdTipoInstrumento"
		        LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as AGENTE
		            ON AGENTE."CodigoAgenteMigracion" =  GES_TBCTMFIF."COD_AGENTE"
		        LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA_AGENTE
		        	ON PERSONA_AGENTE."IdPersona" = AGENTE."IdPersona"
		        LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica"  AS DIRECCION
		        	ON DIRECCION."IdDireccionFisica" = PERSONA."IdDireccionFisicaPrincipal"
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Sucursal" as SUCURSAL
		            ON SUCURSAL."CodigoSucursal" =  GES_TBCTMFIF."COD_SUC"
		        LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" AS COMPROBANTE
		        	ON COMPROBANTE."TIPO_COMP_FAC" = GES_TBCTMFIF."TIPO_COMP_FAC"
		        	AND COMPROBANTE."FOLIO_COMP_FAC" = GES_TBCTMFIF."FOLIO_COMP_FAC" 
 	   			LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFIF."FEC_MVTO" >='0001-01-01'
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <='4000-01-01'
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL; 
	  	   		     
	  --- log masivo IF

      insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEIF rut_cli no tiene DOCUMENTO IDENTIDAD OPTIMUS' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , coalesce(TRIM(GES_TBCTMFIF."RUT_DEST"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'SEC_MOVTO_FAC' as "NombreCampoGenerico8"
                    ,GES_TBCTMFIF."SEC_MOVTO_FAC" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFIF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL
	  	   		     AND DOCUMENTO."DocumentoIdentidad" IS NULL;
                
	        -- todo documento identidad debe estar asociado a una persona    
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEIF DOCUMENTO IDENTIDAD no tiene PERSONA asociada en optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , coalesce(TRIM(GES_TBCTMFIF."RUT_DEST"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdDocumentoIdentidad' as "NombreCampoGenerico6"
                    , coalesce(DOCUMENTO."IdDocumentoIdentidad",0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFIF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL
	  	   		     AND PERSONA."IdDocumentoIdentidad" IS NULL;
	  	   		     
	  	   -- toda persona debe tener asociado un cliente
           insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEIF CLIENTE no tiene la CUENTA SEBRA en PORTAFOLIO optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , coalesce(TRIM(GES_TBCTMFIF."RUT_DEST"),'') as "ValorCampoGenerico1"
                    , 'SEC_RUT_DEST' as "NombreCampoGenerico2"
                    , coalesce(TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST"),'') as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , 'CodigoPortafolio' as "NombreCampoGenerico5"
                    , coalesce(TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST"),'') as "ValorCampoGenerico5"
                    ,'IdCliente' as "NombreCampoGenerico6"
                    , coalesce(TO_VARCHAR(CLIENTE."IdCliente"),0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    		    LEFT JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFIF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND CLIENTE."IdCliente" IS NULL 
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;		     
	  	   		     
             -- toda persona debe tener portafolio

			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEIF CLIENTE no tiene la CUENTA SEBRA en PORTAFOLIO optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , coalesce(TRIM(GES_TBCTMFIF."RUT_DEST"),'') as "ValorCampoGenerico1"
                    , 'SEC_RUT_DEST' as "NombreCampoGenerico2"
                    , coalesce(TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST"),'') as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , 'CodigoPortafolio' as "NombreCampoGenerico5"
                    , coalesce(TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST"),'') as "ValorCampoGenerico5"
                    ,'IdCliente' as "NombreCampoGenerico6"
                    , coalesce(TO_VARCHAR(CLIENTE."IdCliente"),0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    		    INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST")
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFIF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND PORTAFOLIO."IdCliente" IS NULL 
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;
	  	   		     
	  	   	 -- los portafolio deben tener creadas las cuentas de inversion    

			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEIF PORTAFOLIO no tiene creada la CUENTA INVERSION optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , coalesce(TRIM(GES_TBCTMFIF."RUT_DEST"),'') as "ValorCampoGenerico1"
                    , 'SEC_RUT_DEST' as "NombreCampoGenerico2"
                    , coalesce(TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST"),'') as "ValorCampoGenerico2"
                    , 'IND_TIT_CCTE' as "NombreCampoGenerico3"
                    , 'CLP' as "ValorCampoGenerico3"
                    , 'IdMoneda' as "NombreCampoGenerico4"
                    , 'CLP' as "ValorCampoGenerico4"
                    ,'CodigoPortafolio' as "NombreCampoGenerico5"
                    , coalesce(TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST"),'') as "ValorCampoGenerico5"
                    ,'IdCliente' as "NombreCampoGenerico6"
                    , coalesce(TO_VARCHAR(CLIENTE."IdCliente"),0) as "ValorCampoGenerico6"
                    ,'IdPortafolio' as "NombreCampoGenerico7"
                    , coalesce(TO_VARCHAR(PORTAFOLIO."IdPortafolio"),0) as "ValorCampoGenerico7"
                    ,'SEC_MOVTO_FAC' as "NombreCampoGenerico8"
                    ,GES_TBCTMFIF."SEC_MOVTO_FAC" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST")
				LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFIF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND CUENTA_INVERSION."IdCliente" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;     
	  	   		     
	  	   		--- deben tener la condicion liquidacion homologada
	  	   		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEIF CONDICION LIQUIDACION SEBRA  no esta HOMOLOGADA' as "Mensaje"
                    , 'IND_LIQUID' as "NombreCampoGenerico1"
                    , coalesce(GES_TBCTMFIF."IND_LIQUID",'') as "ValorCampoGenerico1"
                    , 'TipoValor' as "NombreCampoGenerico2"
                    , 'CondicionLiquidacion' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFIF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFIF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFIF."IND_LIQUID" END
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFIF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND CONDICION_LIQUIDACION."TipoValor" IS NULL 
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;
	  	   		     
	  	   		
	  	   	--- debe tener cargada la asignacion
	  	   		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEIF no tiene asociada UNA ASIGNACION' as "Mensaje"
                    , '' as "NombreCampoGenerico1"
                    , '' as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFIF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFIF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFIF."IND_LIQUID" END
                LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" as ASIGNACION
	  	   			ON ASIGNACION."SEC_MOVTO_FAC" = GES_TBCTMFIF."SEC_MOVTO_FAC"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFIF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND ASIGNACION."SEC_MOVTO_FAC" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;  
	  	   		     
	  	   	  -- el tipo de operacion debe estar homologado
	  	   	  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEIF el tipo de operacion no esta homologacion' as "Mensaje"
                    , 'COD_MOVTO'  as "NombreCampoGenerico1"
                    , GES_TBCTMFIF."COD_MOVTO" as "ValorCampoGenerico1"
                    , 'MERCADO'as "NombreCampoGenerico2"
                    , 'IF' as "ValorCampoGenerico2"
                    , 'PRODUCTO' as "NombreCampoGenerico3"
                    , GES_TBCTMFIF."PRODUCTO" as "ValorCampoGenerico3"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico4"
                    , GES_TBCTMFIF."TIPO_COMP_FAC" as "ValorCampoGenerico4"
                    , 'TipoValor' as "NombreCampoGenerico5"
                    , 'TipoOperacionFacturacion' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFIF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFIF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFIF."IND_LIQUID" END
                LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFIF."COD_MOVTO")
	                AND TIPOOPERACION."ValorOrigen2" = 'IF'
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFIF."PRODUCTO")	
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFIF."TIPO_COMP_FAC")	
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE TIPOOPERACION."TipoValor" IS NULL 
 	   			     AND GES_TBCTMFIF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;  
	  	   		          	        
	  	   		     
			 -- el tipo producto debe estar homologado
	  	   	 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEIF no tiene asociada UNA ASIGNACION' as "Mensaje"
                    , 'TipoValor'  as "NombreCampoGenerico1"
                    , 'TipoProducto' as "ValorCampoGenerico1"
                    , 'PRODUCTO'  as "NombreCampoGenerico2"
                    ,  GES_TBCTMFIF."PRODUCTO"  as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFIF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFIF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFIF."IND_LIQUID" END
                INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFIF."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = 'IF'
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFIF."PRODUCTO")	
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFIF."TIPO_COMP_FAC")
			    LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFIF."PRODUCTO"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE GES_TBCTMFIF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND TIPOPRODUCTO."TipoValor" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL; 
	  	   		
	  	     -- el instrumento debe existir 
	  	   	  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEIF el instrumento no existe' as "Mensaje"
                    , 'NEMO'  as "NombreCampoGenerico1"
                    , coalesce(trim(GES_TBCTMFIF."NEMO"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFIF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFIF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFIF."IND_LIQUID" END
                INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFIF."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = 'IF'
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFIF."PRODUCTO")	
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFIF."TIPO_COMP_FAC")
			    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFIF."PRODUCTO"
			  	LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO 
                	ON INSTRUMENTO."Nemotecnico"  = trim(GES_TBCTMFIF."NEMO")
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFIF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND INSTRUMENTO."Nemotecnico" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;      
	  	   		-- el tipo instrumento asociado al instrumento debe existir
                
                  -- el tipo instrumento debe existir 
	  	   	  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEIF el TIPOINSTRUMENTO asociado al INSTRUMENTO no existe' as "Mensaje"
                    , 'NEMO'  as "NombreCampoGenerico1"
                    , coalesce(trim(GES_TBCTMFIF."NEMO"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFIF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as GES_TBCTMFIF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFIF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFIF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFIF."IND_LIQUID" END
                INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFIF."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = 'IF'
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFIF."PRODUCTO")	
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFIF."TIPO_COMP_FAC")
			    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFIF."PRODUCTO"
			  	INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO 
                	ON INSTRUMENTO."Nemotecnico"  = trim(GES_TBCTMFIF."NEMO")
                LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO
			   		ON TIPOINSTRUMENTO."IdTipoInstrumento" =  INSTRUMENTO."IdTipoInstrumento"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFIF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFIF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFIF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND TIPOINSTRUMENTO."IdTipoInstrumento" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;

       --  movientos rf
       insert into "BSCL.Homologacion.BD::HOM.MovimientoComprobante"
       ("IdMovimientoComprobante","IdEmpresa","IdConceptoMovimiento","SecuenciaAgrupacion","IdCliente","IdPortafolio","IdAgente"
			,"IdSucursal","IdInstrumento","IdInstrumentoSubyacente","IdTipoInstrumento","IdTipoInstrumentoSubyacente","IdClasificacionTipoInstrumento"
			,"IdTipoProducto","IdMercado","IdCuentaInversion","IdMoneda","IdTipoComprobante","IdComprobante","IdTipoOperacion","IdMovimientoRef","IdBoveda"
			,"IdCorredoraContraparte","IdCondicionLiquidacion","FechaLiquidacion","IdBolsa","IdTipoReajuste","IdTipoConvenio"
			,"IdEstadoFacturacion","IdOrden","LineaOrden","IdAsignacion","IdTipoCanalIngreso","FechaOrden","IdTransaccion","FolioTransaccion","FechaTransaccion"
			,"FechaVencimiento","FechaParidad","ValorParidad","ValorCaptacionMonedaOrigen","FolioComprobante","Tasa","Cantidad","Precio","Monto","MontoDescuento"
			,"MontoInteres","Glosa","ValorRescate","TasaAgente","Spread","DiasPlazo","CodigoCanalIngreso","DocumentoIdentidad","TipoDocumentoIdentidad"
			,"NombreLargoCliente","DireccionFacturacion","NombreAgente","NombreSucursal","FolioCartera","CodigoCuentaInversion","CodigoTipoProducto"
			,"CodigoTipoOperacion","CodigoBolsa","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente","NombreClasificacionTipoInstrumento","NemotecnicoInstrumento"
			,"NemotecnicoSubyacente","RazonSocialEmisor","FechaEmision","TasaEmision","FechaIngresoRegistro","FechaSistema","EsComprobIndividual","EsCuentaPropia"
			,"EsOrdenDirecta","EsPrecioPrima","EsEnRueda","EsRuteo","EsIngresoAutomatico","IdUsuario","IdUsuarioBloqueo","IdCarga","EsAPlazo"
			,"MontoAPlazo","PrecioAPlazo","IdMovimientoOdRef","IdMonedaSubyacente","IdMonedaLiquidacion","ValorTipoCambio","EsVentaCorta","IdComprobanteRef","SecMovimientoFacturarMigracion"
			,"ActualizarCustodia","FechaMovimientoComprobante","FechaTipoCambio","ActualizarCustodiaRelacionado","IdMonedaMovimientoComprobante","MontoMovimientoComprobante"
			,"MontoLiquidacion","IdEmisor","EsFungible","IdFormaCustodia","ActualizarCuentaInversion","ActualizarTesoreria","IdInstrumentoRelacionado","IdTipoInstrumentoRelacionado"
			,"IdClasificacionTipoInstrumentoRelacionado","NombreTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumentoRelacionado","NemotecnicoRelacionado"
			,"IdAsignacionRef","RUT_CLI","SEC_RUT_CLI","TIPO_COMP_FAC","FOLIO_COMP_FAC","SEC_MOVTO_FAC"
	    )
		select 
					"BSCL.Facturacion.BD.Secuencias::MovimientoComprobante".NEXTVAL  as "IdMovimientoComprobante"
					,p_IdEmpresa as "IdEmpresa"
					,'' as "IdConceptoMovimiento"
					,0  as "SecuenciaAgrupacion"
					,CLIENTE."IdCliente" as "IdCliente"
					,PORTAFOLIO."IdPortafolio" as "IdPortafolio"
					,COALESCE(AGENTE."IdEmpleado",0) as "IdAgente"
					,COALESCE(SUCURSAL."IdSucursal",0) as "IdSucursal"
					,coalesce(INSTRUMENTO_SIM."IdInstrumento",INSTRUMENTO_VC."IdInstrumento",INSTRUMENTO."IdInstrumento") as "IdInstrumento"
					,CASE WHEN (INSTRUMENTO_SIM."IdInstrumento" IS NOT NULL OR INSTRUMENTO_VC."IdInstrumento" IS NOT NULL) THEN INSTRUMENTO."IdInstrumento" ELSE 0 END as "IdInstrumentoSubyacente"
					,coalesce(INSTRUMENTO_SIM."IdTipoInstrumento",INSTRUMENTO_VC."IdTipoInstrumento",INSTRUMENTO."IdTipoInstrumento") as "IdTipoInstrumento"
					,CASE WHEN (INSTRUMENTO_SIM."IdInstrumento" IS NOT NULL OR INSTRUMENTO_VC."IdInstrumento" IS NOT NULL) THEN INSTRUMENTO."IdTipoInstrumento" ELSE '' END as "IdTipoInstrumentoSubyacente"
					,'RV' as "IdClasificacionTipoInstrumento"
					,CASE WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('TP','TS'))) THEN 'INSI'
	    			      WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('')))  THEN TIPOPRODUCTO."ValorDestino"
							END as "IdTipoProducto"
					,'' as "IdMercado"
					,CUENTA_INVERSION."IdCuentaInversion"  as "IdCuentaInversion"-- LO SACARE DE LA MONEDA ORIGEN DE LA TABLA TBCTMFRV
					,CUENTA_INVERSION."IdMoneda" as "IdMoneda"--LO SACARE DE LA MONEDA ORIGEN DE LA TABLA TBCTMFRV en caso que sea blanco sera CLP
					,TIPOCOMPROBANTE."ValorDestino" as "IdTipoComprobante"
					,COALESCE(COMPROBANTE."IdComprobante",0) as "IdComprobante"-- debere actualizarlo cuando cree el comprobante
					,CASE WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRF."TIPO_OPERAC"='V' AND GES_TBCTMFRF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMV'  
					 WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRF."TIPO_OPERAC"='C' AND GES_TBCTMFRF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMC'  
					 WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRF."TIPO_OPERAC"='V' AND GES_TBCTMFRF."TIPO_COMP_FAC"='FD')) THEN 'VENSIM'
					 WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRF."TIPO_OPERAC"='C' AND GES_TBCTMFRF."TIPO_COMP_FAC"='FD')) THEN 'COMSIM'					 
					 WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('VC'))) THEN 'PRESTA'	
					 WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('')))  THEN TIPOOPERACION."ValorDestino"
					 END as "IdTipoOperacion"
					,0 as "IdMovimientoRef" -- CUANDO LAS OPERACIONES SON OD SIM O SIMULTANEOA OD  REVISAR
					,GES_TBCTMFRF."COD_BOVEDA" as "IdBoveda" -- REVISAR
					,GES_TBCTMFRF."COD_CORR_CONTRA" as "IdCorredoraContraparte" --debo actualizar u homologar
					,CONDICION_LIQUIDACION."ValorDestino" as "IdCondicionLiquidacion"
					,'0001-01-01' as "FechaLiquidacion" -- hay que revisar  quiza haya que hacer lo mismo que el la transaccion
					,GES_TBCTMFRF."IND_BOLSA" as "IdBolsa"
					,'' as "IdTipoReajuste"
					,'' as "IdTipoConvenio"
					,COALESCE(COMPROBANTE."IdEstadoComprobante",'') as "IdEstadoFacturacion" 
					,COALESCE(ASIGNACION."IdOrden",0) as "IdOrden"
					,GES_TBCTMFRF."LIN_ORDEN" as "LineaOrden"
					,COALESCE(ASIGNACION."IdAsignacion",0) as "IdAsignacion"
					,'' as "IdTipoCanalIngreso"
					,'0001-01-01' as "FechaOrden"
					,COALESCE(ASIGNACION."IdTransaccion",0) as "IdTransaccion"
					,COALESCE(ASIGNACION."FOLIO_TRANS",0) as "FolioTransaccion"
					,substring(GES_TBCTMFRF."FEC_TRANS",1,10)  as "FechaTransaccion"
					,'0001-01-01' as "FechaVencimiento"
					,'0001-01-01' as "FechaParidad"
					,0 as "ValorParidad"   ---- NO TENEMOS INFORMACION SIEMPRE DEBE SER 0
					,GES_TBCTMFRF."CAPTACION_MORIGEN" as "ValorCaptacionMonedaOrigen"
					,0 as "FolioComprobante"
					,GES_TBCTMFRF."TIR" as "Tasa"
					,GES_TBCTMFRF."CANTIDAD" as "Cantidad"
					,GES_TBCTMFRF."PRECIO" as "Precio"
					,GES_TBCTMFRF."MONTO" as "Monto"
					,0 as "MontoDescuento"
					,0 as "MontoInteres"
					,'' as "Glosa"
					,0 as "ValorRescate"
					,0 as "TasaAgente"
					,0 as "Spread"
					,0 as "DiasPlazo" --ANALIZAR
					,'' as "CodigoCanalIngreso" --- REVISAR
					,trim(GES_TBCTMFRF."RUT_DEST") as "DocumentoIdentidad"
					,'R' as "TipoDocumentoIdentidad"
					,PERSONA."Nombres"||' '||PERSONA."ApellidoPaterno"||' '||PERSONA."ApellidoMaterno" as "NombreLargoCliente"
					,COALESCE(DIRECCION."Calle",'') as "DireccionFacturacion"
					,PERSONA_AGENTE."Nombres"||' '||PERSONA_AGENTE."ApellidoPaterno"||' '||PERSONA_AGENTE."ApellidoMaterno"  as "NombreAgente"
					,SUCURSAL."NombreSucursal" as "NombreSucursal"
					,0  as "FolioCartera"
					,CUENTA_INVERSION."Codigo" as "CodigoCuentaInversion"
					,CASE WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('TP','TS'))) THEN 'INSI'  
	    			      WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('')))  THEN TIPOPRODUCTO."ValorDestino"
					END as "CodigoTipoProducto"
					,CASE WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRF."TIPO_OPERAC"='V' AND GES_TBCTMFRF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMV'  
					 WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRF."TIPO_OPERAC"='C' AND GES_TBCTMFRF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMC'  
					 WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRF."TIPO_OPERAC"='V' AND GES_TBCTMFRF."TIPO_COMP_FAC"='FD')) THEN 'VENSIM'
					 WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTMFRF."TIPO_OPERAC"='C' AND GES_TBCTMFRF."TIPO_COMP_FAC"='FD')) THEN 'COMSIM'					 
					 WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('VC'))) THEN 'PRESTA'	
					 WHEN((GES_TBCTMFRF."IND_SIMUL" IN ('')))  THEN TIPOOPERACION."ValorDestino"
					 END as "CodigoTipoOperacion"
					,GES_TBCTMFRF."IND_BOLSA"  as "CodigoBolsa"
                    ,COALESCE(TIPOINSTRUMENTO_SIM."Nombre",TIPOINSTRUMENTO_VC."Nombre",TIPOINSTRUMENTO."Nombre") AS "NombreTipoInstrumento"
					,CASE WHEN (TIPOINSTRUMENTO_SIM."IdTipoInstrumento" IS NOT NULL OR TIPOINSTRUMENTO_VC."IdTipoInstrumento" IS NOT NULL) THEN TIPOINSTRUMENTO."Nombre" ELSE '' END as "NombreTipoInstrumentoSubyacente"
					,'RENTA VARIABLE' as "NombreClasificacionTipoInstrumento"
					,COALESCE(INSTRUMENTO_SIM."Nemotecnico",INSTRUMENTO_VC."Nemotecnico",INSTRUMENTO."Nemotecnico")as "NemotecnicoInstrumento"
					,CASE WHEN (INSTRUMENTO_SIM."Nemotecnico" IS NOT NULL OR INSTRUMENTO_VC."Nemotecnico" IS NOT NULL) THEN INSTRUMENTO."Nemotecnico" ELSE '' END as "NemotecnicoSubyacente"
					,INSTRUMENTO."IdEmisor" as "RazonSocialEmisor"
					,'0001-01-01' as "FechaEmision"
					,0 as "TasaEmision"
					,GES_TBCTMFRF."FEC_MVTO" as "FechaIngresoRegistro"
					,'0001-01-01' as "FechaSistema"
					,GES_TBCTMFRF."IND_COMP_INDIV" as "EsComprobIndividual"
					,CASE WHEN GES_TBCTMFRF."IND_CTA_PRO" ='P' THEN 'S' ELSE 'N' END as "EsCuentaPropia"
					,CASE WHEN TRIM(GES_TBCTMFRF."IND_OD")<>'' THEN 'S' ELSE 'N' END as "EsOrdenDirecta"
					,'' as "EsPrecioPrima"
					,'S' as "EsEnRueda"
					,'' as "EsRuteo"
					,'' as "EsIngresoAutomatico"
					,0 as "IdUsuario"
					,0 as "IdUsuarioBloqueo"
					,88 as "IdCarga"
					,'' as "EsAPlazo"
					,0 as "MontoAPlazo"
					,0 as "PrecioAPlazo"
					,0 as "IdMovimientoOdRef"
					,GES_TBCTMFRF."MON_MORIGEN" as "IdMonedaSubyacente"
					,GES_TBCTMFRF."MON_MORIGEN" as "IdMonedaLiquidacion"
					,0 as "ValorTipoCambio"
					,CASE WHEN GES_TBCTMFRF."IND_SIMUL" = 'VC'  THEN 'S'  ELSE 'N' END as "EsVentaCorta"
					,0 as "IdComprobanteRef"
					,GES_TBCTMFRF."SEC_MOVTO_FAC" as "SecMovimientoFacturarMigracion"
					,CASE WHEN GES_TBCTMFRF."IND_TIT_CUS" ='C' THEN 'S' 
						  WHEN GES_TBCTMFRF."IND_TIT_CUS" ='N' THEN 'N' 
						  WHEN GES_TBCTMFRF."IND_TIT_CUS" =''  THEN 'S' 
				     END  as "ActualizarCustodia"			  
					,GES_TBCTMFRF."FEC_MVTO" as "FechaMovimientoComprobante"
					,'0001-01-01' as "FechaTipoCambio"
					,'' as "ActualizarCustodiaRelacionado"
					,GES_TBCTMFRF."MON_MORIGEN"  as "IdMonedaMovimientoComprobante"
					,GES_TBCTMFRF."MONTO" as "MontoMovimientoComprobante"
					,GES_TBCTMFRF."MONTO" as "MontoLiquidacion"
					,''  as "IdEmisor"
					,CASE WHEN GES_TBCTMFRF."IND_TIT_CUS" ='C' THEN 'S' 
						  WHEN GES_TBCTMFRF."IND_TIT_CUS" ='N' THEN 'N' 
						  WHEN GES_TBCTMFRF."IND_TIT_CUS" =''  THEN 'S'
				     END as "EsFungible"
					,CASE WHEN GES_TBCTMFRF."IND_TIT_CUS" ='C' THEN 'CUS' 
						  WHEN GES_TBCTMFRF."IND_TIT_CUS" ='N' THEN 'NOM' 
						  WHEN GES_TBCTMFRF."IND_TIT_CUS" =''  THEN 'CUS'
				      END as "IdFormaCustodia"
					,'S' as "ActualizarCuentaInversion"
					,'S' as "ActualizarTesoreria"
					,0  as "IdInstrumentoRelacionado"
					,'' as "IdTipoInstrumentoRelacionado"
					,'' as "IdClasificacionTipoInstrumentoRelacionado"
					,'' as "NombreTipoInstrumentoRelacionado"
					,'' as "NombreClasificacionTipoInstrumentoRelacionado"
					,'' as "NemotecnicoRelacionado"
					,0  as "IdAsignacionRef"
					,GES_TBCTMFRF."RUT_DEST" as "RUT_CLI"
					,GES_TBCTMFRF."SEC_RUT_DEST" as "SEC_RUT_CLI"
					,GES_TBCTMFRF."TIPO_COMP_FAC" as "TIPO_COMP_FAC"
					,GES_TBCTMFRF."FOLIO_COMP_FAC" as "FOLIO_COMP_FAC"
					,GES_TBCTMFRF."SEC_MOVTO_FAC" as "SEC_MOVTO_FAC"
                FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
   				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"		
				INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"    = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRF."IND_LIQUID" END
                INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" as ASIGNACION
	  	   			ON ASIGNACION."SEC_MOVTO_FAC" = GES_TBCTMFRF."SEC_MOVTO_FAC"
	  	   		INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFRF."COD_MOVTO")
	                AND TIPOOPERACION."ValorOrigen2" = 'RF'
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFRF."PRODUCTO")	
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFRF."TIPO_COMP_FAC")	
			    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFRF."PRODUCTO" 
			   	INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO 
                	ON INSTRUMENTO."Nemotecnico"  = GES_TBCTMFRF."NEMO"    
			   	INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO
			   		ON TIPOINSTRUMENTO."IdTipoInstrumento" =  INSTRUMENTO."IdTipoInstrumento"
			   	INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTE
			  		ON  TIPOCOMPROBANTE."TipoValor" = 'TipoComprobante' 
			  		AND TIPOCOMPROBANTE."ValorOrigen" = GES_TBCTMFRF."TIPO_COMP_FAC"
               	LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO_SIM
		    		ON INSTRUMENTO_SIM."IdInstrumento" = CASE WHEN (GES_TBCTMFRF."IND_SIMUL" = 'TS' OR GES_TBCTMFRF."IND_SIMUL" = 'TP') THEN
		            	                                   CASE WHEN  GES_TBCTMFRF."TIPO_OPERAC" ='V' THEN 
		                	                                     2249
		                    	                           ELSE 
		                                                   		 2248
		                                               	   END
		                                           				ELSE -1
		                                          		   END
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO_SIM
		        	ON TIPOINSTRUMENTO_SIM."IdTipoInstrumento" =  INSTRUMENTO_SIM."IdTipoInstrumento"
		        	
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO_VC  
		        	ON INSTRUMENTO_VC."IdInstrumento" = CASE WHEN GES_TBCTMFRF."IND_SIMUL" = 'VC' THEN
		            	                                          10010
		                                           				ELSE -1
		                                          		 END
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO_VC
		        	ON TIPOINSTRUMENTO_VC."IdTipoInstrumento" =  INSTRUMENTO_VC."IdTipoInstrumento"
		        LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as AGENTE
		            ON AGENTE."CodigoAgenteMigracion" =  GES_TBCTMFRF."COD_AGENTE"
		        LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA_AGENTE
		        	ON PERSONA_AGENTE."IdPersona" = AGENTE."IdPersona"
		        LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica"  AS DIRECCION
		        	ON DIRECCION."IdDireccionFisica" = PERSONA."IdDireccionFisicaPrincipal"
		        LEFT JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Sucursal" as SUCURSAL
		            ON SUCURSAL."CodigoSucursal" =  GES_TBCTMFRF."COD_SUC"
		        LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" AS COMPROBANTE
		        	ON COMPROBANTE."TIPO_COMP_FAC" = GES_TBCTMFRF."TIPO_COMP_FAC"
		        	AND COMPROBANTE."FOLIO_COMP_FAC" = GES_TBCTMFRF."FOLIO_COMP_FAC" 
 	   			LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
                WHERE  GES_TBCTMFRF."FEC_MVTO" >='0001-01-01'
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <='4000-01-01'
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;
        
       -- log masivo RF
       -- el cliente debe tener asociado documento identidad   
      insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF rut_cli no tiene DOCUMENTO IDENTIDAD OPTIMUS' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , coalesce(TRIM(GES_TBCTMFRF."RUT_DEST"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'SEC_MOVTO_FAC' as "NombreCampoGenerico8"
                    ,GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL
	  	   		     AND DOCUMENTO."DocumentoIdentidad" IS NULL;
              
	        -- todo documento identidad debe estar asociado a una persona    
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF DOCUMENTO IDENTIDAD no tiene PERSONA asociada en optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , TRIM(GES_TBCTMFRF."RUT_DEST") as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdDocumentoIdentidad' as "NombreCampoGenerico6"
                    , DOCUMENTO."IdDocumentoIdentidad" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'SEC_MOVTO_FAC' as "NombreCampoGenerico8"
                    ,GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL
	  	   		     AND PERSONA."IdDocumentoIdentidad" IS NULL;
	  	   		     
	  	   	-- toda persona que haya hecho operaciones debe tener cliente
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF CLIENTE no creado en optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , TRIM(GES_TBCTMFRF."RUT_DEST") as "ValorCampoGenerico1"
                    , 'SEC_RUT_DEST' as "NombreCampoGenerico2"
                    , TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST") as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'SEC_MOVTO_FAC' as "NombreCampoGenerico8"
                    ,GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND CLIENTE."IdCliente" IS NULL 
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;
	  	   	  
             -- toda cliente debe tener portafolio
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF CLIENTE no tiene la CUENTA SEBRA en PORTAFOLIO optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    ,  GES_TBCTMFRF."RUT_DEST" as "ValorCampoGenerico1"
                    , 'SEC_RUT_DEST' as "NombreCampoGenerico2"
                    , TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST") as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdCliente' as "NombreCampoGenerico6"
                    , CLIENTE."IdCliente" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'SEC_MOVTO_FAC' as "NombreCampoGenerico8"
                    ,GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    		    INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST")
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND PORTAFOLIO."IdCliente" IS NULL 
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;
	  
	  	   	 -- los portafolio deben tener creadas las cuentas de inversion    
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF PORTAFOLIO no tiene creada la CUENTA INVERSION optimus' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , TRIM(GES_TBCTMFRF."RUT_DEST") as "ValorCampoGenerico1"
                    , 'SEC_RUT_DEST' as "NombreCampoGenerico2"
                    , TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST") as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , ''  as "ValorCampoGenerico3"
                    , 'IdMoneda' as "NombreCampoGenerico4"
                    , 'CLP'	 as "ValorCampoGenerico4"
                    ,'CodigoPortafolio' as "NombreCampoGenerico5"
                    , TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST") as "ValorCampoGenerico5"
                    ,'IdCliente' as "NombreCampoGenerico6"
                    , CLIENTE."IdCliente" as "ValorCampoGenerico6"
                    ,'IdPortafolio' as "NombreCampoGenerico7"
                    , PORTAFOLIO."IdPortafolio" as "ValorCampoGenerico7"
                    ,'SEC_MOVTO_FAC' as "NombreCampoGenerico8"
                    ,GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST")
				LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND CUENTA_INVERSION."IdCliente" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;     
	  	  
	  	   	--- deben tener la condicion liquidacion homologada
	  	   		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF CONDICION LIQUIDACION SEBRA  no esta HOMOLOGADA' as "Mensaje"
                    , 'IND_LIQUID' as "NombreCampoGenerico1"
                    , GES_TBCTMFRF."IND_LIQUID" as "ValorCampoGenerico1"
                    , 'TipoValor' as "NombreCampoGenerico2"
                    , 'CondicionLiquidacion' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRF."IND_LIQUID" END
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND CONDICION_LIQUIDACION."TipoValor" IS NULL 
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;
	  	   		     

	  	   	--- debe tener cargada la asignacion
	  	   		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF no tiene asociada UNA ASIGNACION' as "Mensaje"
                    , '' as "NombreCampoGenerico1"
                    , '' as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRF."IND_LIQUID" END
                LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" as ASIGNACION
	  	   			ON ASIGNACION."SEC_MOVTO_FAC" = GES_TBCTMFRF."SEC_MOVTO_FAC"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND ASIGNACION."SEC_MOVTO_FAC" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;  
	  	   	  
	  	   	  -- el tipo de operacion debe estar homologado
	  	   	  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF el tipo de operacion no esta homologacion' as "Mensaje"
                    , 'COD_MOVTO'  as "NombreCampoGenerico1"
                    , GES_TBCTMFRF."COD_MOVTO" as "ValorCampoGenerico1"
                    , 'MERCADO'as "NombreCampoGenerico2"
                    , 'RF' as "ValorCampoGenerico2"
                    , 'PRODUCTO' as "NombreCampoGenerico3"
                    , GES_TBCTMFRF."PRODUCTO" as "ValorCampoGenerico3"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico4"
                    , GES_TBCTMFRF."TIPO_COMP_FAC" as "ValorCampoGenerico4"
                    , 'TipoValor' as "NombreCampoGenerico5"
                    , 'TipoOperacionFacturacion' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRF."IND_LIQUID" END
                LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFRF."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = 'RF'
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFRF."PRODUCTO")	
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFRF."TIPO_COMP_FAC")	
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE TIPOOPERACION."TipoValor" IS NULL
 	   			     AND GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;  
      
	  	   	  -- el tipo operacion debe estar homologado
	  	   	  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF no tiene asociada UNA ASIGNACION' as "Mensaje"
                    , 'COD_MOVTO'  as "NombreCampoGenerico1"
                    , GES_TBCTMFRF."COD_MOVTO" as "ValorCampoGenerico1"
                    , 'MERCADO'as "NombreCampoGenerico2"
                    , 'RF' as "ValorCampoGenerico2"
                    , 'PRODUCTO' as "NombreCampoGenerico3"
                    , GES_TBCTMFRF."PRODUCTO" as "ValorCampoGenerico3"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico4"
                    , GES_TBCTMFRF."TIPO_COMP_FAC" as "ValorCampoGenerico4"
                    , 'TipoValor' as "NombreCampoGenerico5"
                    , 'TipoOperacionFacturacion' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRF."IND_LIQUID" END
                LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFRF."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = 'RF'
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFRF."PRODUCTO")	
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFRF."TIPO_COMP_FAC")	
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND TIPOOPERACION."TipoValor" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;  	        
	  	   		
	  	   		     
			 -- el tipo producto debe estar homologado
	  	   	 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF no tiene asociada UNA ASIGNACION' as "Mensaje"
                    , 'TipoValor'  as "NombreCampoGenerico1"
                    , 'TipoProducto' as "ValorCampoGenerico1"
                    , 'PRODUCTO'  as "NombreCampoGenerico2"
                    ,  GES_TBCTMFRF."PRODUCTO"  as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    		    INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"			
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRF."IND_LIQUID" END
                INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFRF."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = 'RF'
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFRF."PRODUCTO")	
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFRF."TIPO_COMP_FAC")
			    LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFRF."PRODUCTO"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND TIPOPRODUCTO."TipoValor" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL; 

	  	     -- el instrumento debe existir 
	  	   	  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF el instrumento no existe' as "Mensaje"
                    , 'NEMO'  as "NombreCampoGenerico1"
                    , coalesce(trim(GES_TBCTMFRF."NEMO"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRF."IND_LIQUID" END
                INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFRF."COD_MOVTO") 
	                AND TIPOOPERACION."ValorOrigen2" = 'RF'
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFRF."PRODUCTO")	
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFRF."TIPO_COMP_FAC")
			    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFRF."PRODUCTO"
			  	LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO 
                	ON INSTRUMENTO."Nemotecnico"  = trim(GES_TBCTMFRF."NEMO")
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND INSTRUMENTO."Nemotecnico" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;      
	  	   		-- el tipo instrumento asociado al instrumento debe existir
                
                  -- el tipo instrumento debe existir 
	  	   	  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTERF el TIPOINSTRUMENTO asociado al INSTRUMENTO no existe' as "Mensaje"
                    , 'NEMO'  as "NombreCampoGenerico1"
                    , coalesce(trim(GES_TBCTMFRF."NEMO"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , 'SEC_MOVTO_FAC' as "NombreCampoGenerico7"
                    , GES_TBCTMFRF."SEC_MOVTO_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as GES_TBCTMFRF
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO
   					ON DOCUMENTO."DocumentoIdentidad" = CASE WHEN TRIM(p_RutCliente) = '0-0' THEN TRIM(GES_TBCTMFRF."RUT_DEST") ELSE TRIM(p_RutCliente) END
                INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA
    				 ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO."IdDocumentoIdentidad"
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE
    				ON CLIENTE."IdPersona" = PERSONA."IdPersona"
    		    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO 
					ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST")
				INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CUENTA_INVERSION
	  	   			ON  CUENTA_INVERSION."IdCliente"  = CLIENTE."IdCliente"
	  	   			AND CUENTA_INVERSION."IdPortafolio" = PORTAFOLIO."IdPortafolio"
	  	   			AND CUENTA_INVERSION."IdMoneda" = 'CLP'
	  	   		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CONDICION_LIQUIDACION
				    ON  CONDICION_LIQUIDACION."TipoValor" = 'CondicionLiquidacion'         
                 	AND CONDICION_LIQUIDACION."ValorOrigen"  = case when ifnull(GES_TBCTMFRF."IND_LIQUID",'') = '' THEN 'PH' ELSE GES_TBCTMFRF."IND_LIQUID" END
                INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
	                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
	                AND TIPOOPERACION."ValorOrigen"  = TRIM(GES_TBCTMFRF."COD_MOVTO")
	                AND TIPOOPERACION."ValorOrigen2" = 'RF'
	 		        AND TIPOOPERACION."ValorOrigen3" = TRIM(GES_TBCTMFRF."PRODUCTO")	
			        AND TIPOOPERACION."ValorOrigen4" = TRIM(GES_TBCTMFRF."TIPO_COMP_FAC")
			    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  		ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  		AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMFRF."PRODUCTO"
			  	INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO 
                	ON INSTRUMENTO."Nemotecnico"  = trim(GES_TBCTMFRF."NEMO")
                LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINSTRUMENTO
			   		ON TIPOINSTRUMENTO."IdTipoInstrumento" =  INSTRUMENTO."IdTipoInstrumento"
                LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS HOMOLOGACION_MOV_COMPROBANTE
 	   				ON HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" = GES_TBCTMFRF."SEC_MOVTO_FAC"
 	   			WHERE  GES_TBCTMFRF."FEC_MVTO" >=P_FechaInicial
	  	   		     AND GES_TBCTMFRF."FEC_MVTO" <=P_FechaFinal
	  	   		     AND TIPOINSTRUMENTO."IdTipoInstrumento" IS NULL
	  	   		     AND HOMOLOGACION_MOV_COMPROBANTE."SecMovimientoFacturarMigracion" IS NULL;

       --CONTAMOS EL NUMERO DE MOVIMIENTO A FACTURAR
       SELECT COUNT(*) INTO v_total_mov_gestion_aux  FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_RV 
	   WHERE MOV_RV."FEC_MVTO" >=P_FechaInicial
	  	   	AND MOV_RV."FEC_MVTO" <=P_FechaFinal
	  	   	AND MOV_RV."RUT_DEST" = CASE WHEN TRIM(p_RutCliente)='0-0' THEN MOV_RV."RUT_DEST" ELSE TRIM(p_RutCliente) END;
       
       v_total_mov_gestion=v_total_mov_gestion+v_total_mov_gestion_aux;
	   v_total_mov_gestion_aux=0;
	   
	   SELECT COUNT(*) INTO v_total_mov_gestion_aux  FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" as MOV_RV 
	   WHERE MOV_RV."FEC_MVTO" >=P_FechaInicial
	  	   	AND MOV_RV."FEC_MVTO" <=P_FechaFinal
	  	   	AND MOV_RV."RUT_DEST" = CASE WHEN TRIM(p_RutCliente)='0-0' THEN MOV_RV."RUT_DEST" ELSE TRIM(p_RutCliente) END;
       
       v_total_mov_gestion=v_total_mov_gestion+v_total_mov_gestion_aux;
	   v_total_mov_gestion_aux=0;
	   
	   
	   SELECT COUNT(*) INTO v_total_mov_gestion_aux  FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" as MOV_RV 
	   WHERE MOV_RV."FEC_MVTO" >=P_FechaInicial
	  	   	AND MOV_RV."FEC_MVTO" <=P_FechaFinal
	  	   	AND MOV_RV."RUT_DEST" = CASE WHEN TRIM(p_RutCliente)='0-0' THEN MOV_RV."RUT_DEST" ELSE TRIM(p_RutCliente) END;
       
       v_total_mov_gestion=v_total_mov_gestion+v_total_mov_gestion_aux;
	   v_total_mov_gestion_aux=0;
	   

     	select count(*) into v_total_mov_cargadas from 
     	"BSCL.Homologacion.BD::HOM.MovimientoComprobante"
      	where "IdCarga" = p_IdCarga;

		-- SACAMOS LOS REGISTROS  comprobantes
	   	p_ResumenDatosProcesados =  select 'CARGAMOVIMIENTOSCOMPROBANTES' as "CodigoProceso" 
   			  							,'FAC' as "Modulo" 
   			 							,v_total_mov_gestion as "TotalRegistrosSebra"
   			  							,v_total_mov_cargadas as "TotalRegistrosSap"  FROM DUMMY;
   							    	
	END;	
END;

 
