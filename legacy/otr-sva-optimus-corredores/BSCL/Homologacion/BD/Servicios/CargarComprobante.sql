SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarComprobante";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de MOVIMIENTOS A FACTURAR RV
--  renta variable
--  Autor: - EAR 

"BSCL.Homologacion.BD.Servicios::CargarComprobante"(

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

		
		--------------------
		--Negocio Aqui
		--------------------
		
		-- convierto el rut a formato sebra 
		select lpad(p_RutCliente,11) into p_RutCliente from dummy;
		select TRIM(SUBSTRING(p_RutCliente,1,9)) into v_PerId from dummy;

        /*
		IF P_Modalidad = 'R' THEN --REPROCESO
         DELETE FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
         WHERE "IdCarga" = 	p_IdCarga;	
         
         DELETE FROM "BSCL.Facturacion.BD::FAC.Comprobante"
         WHERE "IdCarga" = 	p_IdCarga;	
         
		END IF;*/

        -- RECUPERAMOS LOS VALORES A  CARGAR
       INSERT INTO "BSCL.Homologacion.BD::HOM.Comprobante"(
			 "IdComprobante","IdEmpresa","IdPortafolio","IdCliente","DocumentoIdentidad","TipoDocumentoIdentidad" 
			,"NombreLargoCliente" ,"DireccionFacturacion" ,"FolioComprobante" ,"IdTipoComprobante" ,"IdTipoOperacion" 
			,"IdTipoProducto" ,"IdClasificacionTipoInstrumento" ,"IdTipoInstrumento" ,"Monto" ,"MontoCobro" 
			,"MontoFinal" ,"MontoPagado" ,"IdAgente" ,"IdSucursal","IdCondicionLiquidacion" ,"FechaLiquidacion" ,"FechaEmision" 
			,"FechaEnvio" ,"IdEstadoEnvio" ,"FechaRespuestaEnvio" ,"IdEstadoComprobante" ,"IdTipoComprobanteRef" 
			,"IdComprobanteRef" ,"Glosa" ,"EsExento" ,"FechaIngresoRegistro" ,"FechaSistema" ,"IdCarga" 
			,"FolioComprobanteFacturacionMigracion" ,"IdMoneda" ,"IdMonedaLiquidacion" ,"MontoLiquidacion" 
			,"IdMonedaComprobante" ,"MontoComprobante" ,"MontoCobroComprobante" ,"MontoCobroLiquidacion" 
			,"RUT_CLI" ,"SEC_RUT_CLI" ,"TIPO_COMP_FAC" ,"FOLIO_COMP_FAC"  ,"MERCADO" ,"PRODUCTO","FOLIO_COMP_ASOC"
			,"FOLIO_COMP_CONT","FEC_ING_COMP"
			) 
     
			 SELECT 
			 "BSCL.Facturacion.BD.Secuencias::Comprobante".NEXTVAL as "IdComprobante"
			,p_IdEmpresa as "IdEmpresa"
			,POR."IdPortafolio"
			,CLI."IdCliente"
			,TRIM(GES_TBCTMSCF."RUT_CLI") as "DocumentoIdentidad"
			,'R' as "TipoDocumentoIdentidad" 
			,CLI."NombreCompleto" as "NombreLargoCliente" 
			,COALESCE (DIRECCION_FISICA."Calle",'') as "DireccionFacturacion" 
			,GES_TBCTMSCF."FOLIO_COMP_FAC" as "FolioComprobante" 
			,TIPOCOMPROBANTE."ValorDestino" as "IdTipoComprobante" 
			,CASE WHEN((GES_TBCTMSCF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTDTCF."TIPO_OPERAC"='V' AND GES_TBCTMSCF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMV'  
					 WHEN((GES_TBCTMSCF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTDTCF."TIPO_OPERAC"='C' AND GES_TBCTMSCF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMC'  
					 WHEN((GES_TBCTMSCF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTDTCF."TIPO_OPERAC"='V' AND GES_TBCTMSCF."TIPO_COMP_FAC"='FD')) THEN 'VENSIM'
					 WHEN((GES_TBCTMSCF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTDTCF."TIPO_OPERAC"='C' AND GES_TBCTMSCF."TIPO_COMP_FAC"='FD')) THEN 'COMSIM'					 
					 WHEN((IFNULL(TRIM(GES_TBCTMSCF."IND_SIMUL"),'') IN ('')))  THEN COALESCE(TIPOOPERACION."ValorDestino",'')
							END as "IdTipoOperacion" 
			,CASE WHEN((GES_TBCTMSCF."IND_SIMUL" IN ('TP','TS'))) THEN 'INSI'  
	    			 WHEN((IFNULL(TRIM(GES_TBCTMSCF."IND_SIMUL"),'') IN ('')))  THEN coalesce(TIPOPRODUCTO."ValorDestino",'')
							END as "IdTipoProducto"
			,COALESCE(TIPOINSTRUMENTO."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento" 
			,COALESCE(INSTRUMENTO."IdTipoInstrumento",'') as "IdTipoInstrumento" 
			,CASE WHEN ((GES_TBCTMSCF."MONTO_COM") = 0  AND GES_TBCTMSCF."MONTO_VEN"<>0)THEN GES_TBCTMSCF."MONTO_VEN"
			      WHEN ((GES_TBCTMSCF."MONTO_VEN") = 0  AND GES_TBCTMSCF."MONTO_COM"<>0)THEN GES_TBCTMSCF."MONTO_COM"
			         END as "Monto" 
			,GES_TBCTMSCF."MONTO_COMISION"+GES_TBCTMSCF."MONTO_DERECHOS"+GES_TBCTMSCF."MONTO_GASTO"+GES_TBCTMSCF."MONTO_IVA" as "MontoCobro" 
			,GES_TBCTMSCF."MONTO_TOTAL" as "MontoFinal" 
			,GES_TBCTMSCF."MONTO_PAGADO" as "MontoPagado" 
			,coalesce (IDAGENTE."IdEmpleado",0) as "IdAgente" 
			,coalesce (SUCURSAL."IdSucursal",0) as "IdSucursal"
			,CONDICIONLIQUIDACION."IdCondicionDeLiquidacion" as "IdCondicionLiquidacion" 
			,GES_TBCTMSCF."FEC_PAGO" as "FechaLiquidacion" 
			,GES_TBCTMSCF."FEC_EMI_COMP" as "FechaEmision" 
			,GES_TBCTMSCF."FEC_EMI_COMP" as "FechaEnvio" 
			,CASE WHEN ((GES_TBCTMSCF."EST_COMP") = 'E'  AND GES_TBCTMSCF."COD_CORR_CONTRA" ='999')THEN 'ENV'
			  WHEN ((GES_TBCTMSCF."EST_COMP") = 'E' AND GES_TBCTMSCF."COD_CORR_CONTRA" <>'999')THEN 'PEN'
			  WHEN ((GES_TBCTMSCF."EST_COMP") = 'A') THEN 'PEN'
			 END as "IdEstadoEnvio" 
			,'0001-01-01' as "FechaRespuestaEnvio" 
			,CASE WHEN ((GES_TBCTMSCF."EST_COMP") = 'E'  AND GES_TBCTMSCF."COD_CORR_CONTRA" ='999')THEN 'ENV'
			  WHEN ((GES_TBCTMSCF."EST_COMP") = 'E' AND GES_TBCTMSCF."COD_CORR_CONTRA" <>'999')THEN 'FAC'
			  WHEN ((GES_TBCTMSCF."EST_COMP") = 'A') THEN 'ANU'
			 END as "IdEstadoComprobante" 
			,'' as "IdTipoComprobanteRef" 
			,0 as "IdComprobanteRef" 
			,'GLOSA PENDIENTE' as "Glosa" 
			,'N' as "EsExento" 
			,GES_TBCTMSCF."FEC_EMI_COMP" as "FechaIngresoRegistro" 
			,GES_TBCTMSCF."FEC_EMI_COMP" as "FechaSistema" 
			,p_IdCarga as "IdCarga" 
			,GES_TBCTMSCF."FOLIO_COMP_FAC" as "FolioComprobanteFacturacionMigracion" 
			,GES_TBCTMSCF."MON_MORIGEN" as "IdMoneda" 
			,GES_TBCTMSCF."MON_MORIGEN" as "IdMonedaLiquidacion" 
			,CASE WHEN ((GES_TBCTMSCF."MONTO_COM") = 0  AND GES_TBCTMSCF."MONTO_VEN"<>0)THEN GES_TBCTMSCF."MONTO_VEN"
			      WHEN ((GES_TBCTMSCF."MONTO_VEN") = 0  AND GES_TBCTMSCF."MONTO_COM"<>0)THEN GES_TBCTMSCF."MONTO_COM"
			         END as "MontoLiquidacion" 
			,GES_TBCTMSCF."MON_MORIGEN" as "IdMonedaComprobante" 
			,GES_TBCTMSCF."CAPTACION_MORIGEN" as "MontoComprobante" 
			,GES_TBCTMSCF."MONTO_COMISION"+GES_TBCTMSCF."MONTO_DERECHOS"+GES_TBCTMSCF."MONTO_GASTO"+GES_TBCTMSCF."MONTO_IVA" as "MontoCobroComprobante" 
			,GES_TBCTMSCF."MONTO_COMISION"+GES_TBCTMSCF."MONTO_DERECHOS"+GES_TBCTMSCF."MONTO_GASTO"+GES_TBCTMSCF."MONTO_IVA" as "MontoCobroLiquidacion" 
			,GES_TBCTMSCF."RUT_CLI" as "RUT_CLI" 
			,GES_TBCTMSCF."SEC_RUT_CLI" as "SEC_RUT_CLI" 
			,GES_TBCTMSCF."TIPO_COMP_FAC" as "TIPO_COMP_FAC" 
			,GES_TBCTMSCF."FOLIO_COMP_FAC" as "FOLIO_COMP_FAC"  
			,GES_TBCTMSCF."MERCADO" as "MERCADO" 
			,GES_TBCTMSCF."PRODUCTO" as "PRODUCTO" 
			,GES_TBCTMSCF."FOLIO_COMP_ASOC" as "FOLIO_COMP_ASOC"
			,GES_TBCTMSCF."FOLIO_COMP_CONT" as "FOLIO_COMP_CONT"
			,GES_TBCTMSCF."FEC_ING_COMP" as "FEC_ING_COMP"
	    	FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC" =  GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC" =1
	  	   	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(GES_TBCTMSCF."RUT_CLI") ELSE p_RutCliente END 
	  	   	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON POR."IdCliente" = CLI."IdCliente"
				AND POR."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMSCF."SEC_RUT_CLI")
     		INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTE
			  	ON  TIPOCOMPROBANTE."TipoValor" = 'TipoComprobante' 
			  	AND TIPOCOMPROBANTE."ValorOrigen" = TRIM(GES_TBCTMSCF."TIPO_COMP_FAC")
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"  AS INSTRUMENTO
			    ON INSTRUMENTO."Nemotecnico"= TRIM(GES_TBCTDTCF."NEMO")
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento"  AS TIPOINSTRUMENTO
			    ON TIPOINSTRUMENTO."IdTipoInstrumento"=INSTRUMENTO."IdTipoInstrumento"
			INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CONDICIONLIQUIDACION
       			ON CONDICIONLIQUIDACION."Codigo" = CASE WHEN (IFNULL(GES_TBCTMSCF."IND_LIQUID",'')= '' or trim(GES_TBCTMSCF."IND_LIQUID")='') THEN 'PH' ELSE GES_TBCTMSCF."IND_LIQUID" END   
       		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  	ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  	AND TIPOPRODUCTO."Entidad" = 'TipoProducto'
			  	AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMSCF."PRODUCTO" 
       		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
                AND TIPOOPERACION."ValorOrigen"  = IFNULL(TRIM(GES_TBCTDTCF."COD_MOVTO"),'') 
                AND TIPOOPERACION."ValorOrigen2" = IFNULL(TRIM(GES_TBCTMSCF."MERCADO"),'')
 		        AND TIPOOPERACION."ValorOrigen3" = IFNULL(TRIM(GES_TBCTMSCF."PRODUCTO"),'')	
		        AND TIPOOPERACION."ValorOrigen4" = IFNULL(TRIM(GES_TBCTMSCF."TIPO_COMP_FAC"),'')
		    LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIRECCION_FISICA
		    	ON DIRECCION_FISICA."IdDireccionFisica" = CLI."IdDireccionFisicaPrincipal"
   			LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS IDAGENTE
   				ON IDAGENTE."CodigoAgenteMigracion" = GES_TBCTMSCF."COD_AGENTE"
   			LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUCURSAL
   			    ON SUCURSAL."CodigoSucursal" = 	GES_TBCTMSCF."COD_SUC"
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
		    WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND GES_TBCTMSCF."TIPO_COMP_FAC" NOT IN ('FT','NT','FO','NO')-- dividiremso la consulta entre movimientos normales y otros conceptos
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL;
			
			-- insertamos movimiento de otros conceptos
			INSERT INTO "BSCL.Homologacion.BD::HOM.Comprobante"(
			 "IdComprobante","IdEmpresa","IdPortafolio","IdCliente","DocumentoIdentidad","TipoDocumentoIdentidad" 
			,"NombreLargoCliente" ,"DireccionFacturacion" ,"FolioComprobante" ,"IdTipoComprobante" ,"IdTipoOperacion" 
			,"IdTipoProducto" ,"IdClasificacionTipoInstrumento" ,"IdTipoInstrumento" ,"Monto" ,"MontoCobro" 
			,"MontoFinal" ,"MontoPagado" ,"IdAgente" ,"IdSucursal","IdCondicionLiquidacion" ,"FechaLiquidacion" ,"FechaEmision" 
			,"FechaEnvio" ,"IdEstadoEnvio" ,"FechaRespuestaEnvio" ,"IdEstadoComprobante" ,"IdTipoComprobanteRef" 
			,"IdComprobanteRef" ,"Glosa" ,"EsExento" ,"FechaIngresoRegistro" ,"FechaSistema" ,"IdCarga" 
			,"FolioComprobanteFacturacionMigracion" ,"IdMoneda" ,"IdMonedaLiquidacion" ,"MontoLiquidacion" 
			,"IdMonedaComprobante" ,"MontoComprobante" ,"MontoCobroComprobante" ,"MontoCobroLiquidacion" 
			,"RUT_CLI" ,"SEC_RUT_CLI" ,"TIPO_COMP_FAC" ,"FOLIO_COMP_FAC"  ,"MERCADO" ,"PRODUCTO","FOLIO_COMP_ASOC"
			,"FOLIO_COMP_CONT","FEC_ING_COMP"		
			) 
     
			 SELECT 
			 "BSCL.Facturacion.BD.Secuencias::Comprobante".NEXTVAL as "IdComprobante"
			,p_IdEmpresa as "IdEmpresa"
			,POR."IdPortafolio"
			,CLI."IdCliente"
			,TRIM(GES_TBCTMSCF."RUT_CLI") as "DocumentoIdentidad"
			,'R' as "TipoDocumentoIdentidad" 
			,CLI."NombreCompleto" as "NombreLargoCliente" 
			,COALESCE (DIRECCION_FISICA."Calle",'') as "DireccionFacturacion" 
			,GES_TBCTMSCF."FOLIO_COMP_FAC" as "FolioComprobante" 
			,TIPOCOMPROBANTE."ValorDestino" as "IdTipoComprobante" 
			,CASE WHEN((GES_TBCTMSCF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTDTCF."TIPO_OPERAC"='V' AND GES_TBCTMSCF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMV'  
					 WHEN((GES_TBCTMSCF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTDTCF."TIPO_OPERAC"='C' AND GES_TBCTMSCF."TIPO_COMP_FAC"='ND')) THEN 'ANSIMC'  
					 WHEN((GES_TBCTMSCF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTDTCF."TIPO_OPERAC"='V' AND GES_TBCTMSCF."TIPO_COMP_FAC"='FD')) THEN 'VENSIM'
					 WHEN((GES_TBCTMSCF."IND_SIMUL" IN ('TP','TS') AND GES_TBCTDTCF."TIPO_OPERAC"='C' AND GES_TBCTMSCF."TIPO_COMP_FAC"='FD')) THEN 'COMSIM'					 
					 WHEN((IFNULL(TRIM(GES_TBCTMSCF."IND_SIMUL"),'') IN ('')))  THEN coalesce (TIPOOPERACION."ValorDestino",'')
							END as "IdTipoOperacion" 
			,CASE WHEN((GES_TBCTMSCF."IND_SIMUL" IN ('TP','TS'))) THEN 'INSI'  
	    			 WHEN((IFNULL(TRIM(GES_TBCTMSCF."IND_SIMUL"),'') IN ('')))  THEN coalesce(TIPOPRODUCTO."ValorDestino",'')
							END as "IdTipoProducto"
			,COALESCE(TIPOINSTRUMENTO."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento" 
			,COALESCE(INSTRUMENTO."IdTipoInstrumento",'') as "IdTipoInstrumento" 
			,CASE WHEN ((GES_TBCTMSCF."MONTO_COM") = 0  AND GES_TBCTMSCF."MONTO_VEN"<>0)THEN GES_TBCTMSCF."MONTO_VEN"
			      WHEN ((GES_TBCTMSCF."MONTO_VEN") = 0  AND GES_TBCTMSCF."MONTO_COM"<>0)THEN GES_TBCTMSCF."MONTO_COM"
			 END as "Monto" 
			,GES_TBCTMSCF."MONTO_COMISION"+GES_TBCTMSCF."MONTO_DERECHOS"+GES_TBCTMSCF."MONTO_GASTO"+GES_TBCTMSCF."MONTO_IVA" as "MontoCobro" 
			,GES_TBCTMSCF."MONTO_TOTAL" as "MontoFinal" 
			,GES_TBCTMSCF."MONTO_PAGADO" as "MontoPagado" 
			,coalesce (IDAGENTE."IdEmpleado",0) as "IdAgente" 
			,coalesce (SUCURSAL."IdSucursal",0) as "IdSucursal"
			,CONDICIONLIQUIDACION."IdCondicionDeLiquidacion" as "IdCondicionLiquidacion" 
			,GES_TBCTMSCF."FEC_PAGO" as "FechaLiquidacion" 
			,GES_TBCTMSCF."FEC_EMI_COMP" as "FechaEmision" 
			,GES_TBCTMSCF."FEC_EMI_COMP" as "FechaEnvio" 
			,CASE WHEN ((GES_TBCTMSCF."EST_COMP") = 'E'  AND GES_TBCTMSCF."COD_CORR_CONTRA" ='999')THEN 'ENV'
			  WHEN ((GES_TBCTMSCF."EST_COMP") = 'E' AND GES_TBCTMSCF."COD_CORR_CONTRA" <>'999')THEN 'PEN'
			  WHEN ((GES_TBCTMSCF."EST_COMP") = 'A') THEN 'PEN'
			 END as "IdEstadoEnvio" 
			,'0001-01-01' as "FechaRespuestaEnvio" 
			,CASE WHEN ((GES_TBCTMSCF."EST_COMP") = 'E'  AND GES_TBCTMSCF."COD_CORR_CONTRA" ='999')THEN 'ENV'
			  WHEN ((GES_TBCTMSCF."EST_COMP") = 'E' AND GES_TBCTMSCF."COD_CORR_CONTRA" <>'999')THEN 'FAC'
			  WHEN ((GES_TBCTMSCF."EST_COMP") = 'A') THEN 'ANU'
			 END as "IdEstadoComprobante" 
			,'' as "IdTipoComprobanteRef" 
			,0 as "IdComprobanteRef" 
			,'GLOSA PENDIENTE' as "Glosa" 
			,'N' as "EsExento" 
			,GES_TBCTMSCF."FEC_EMI_COMP" as "FechaIngresoRegistro" 
			,GES_TBCTMSCF."FEC_EMI_COMP" as "FechaSistema" 
			,p_IdCarga as "IdCarga" 
			,GES_TBCTMSCF."FOLIO_COMP_FAC" as "FolioComprobanteFacturacionMigracion" 
			,GES_TBCTMSCF."MON_MORIGEN" as "IdMoneda" 
			,GES_TBCTMSCF."MON_MORIGEN" as "IdMonedaLiquidacion" 
			,CASE WHEN ((GES_TBCTMSCF."MONTO_COM") = 0  AND GES_TBCTMSCF."MONTO_VEN"<>0)THEN GES_TBCTMSCF."MONTO_VEN"
			      WHEN ((GES_TBCTMSCF."MONTO_VEN") = 0  AND GES_TBCTMSCF."MONTO_COM"<>0)THEN GES_TBCTMSCF."MONTO_COM"
			         END as "MontoLiquidacion" 
			,GES_TBCTMSCF."MON_MORIGEN" as "IdMonedaComprobante" 
			,GES_TBCTMSCF."CAPTACION_MORIGEN" as "MontoComprobante" 
			,GES_TBCTMSCF."MONTO_COMISION"+GES_TBCTMSCF."MONTO_DERECHOS"+GES_TBCTMSCF."MONTO_GASTO"+GES_TBCTMSCF."MONTO_IVA" as "MontoCobroComprobante" 
			,GES_TBCTMSCF."MONTO_COMISION"+GES_TBCTMSCF."MONTO_DERECHOS"+GES_TBCTMSCF."MONTO_GASTO"+GES_TBCTMSCF."MONTO_IVA" as "MontoCobroLiquidacion" 
			,GES_TBCTMSCF."RUT_CLI" as "RUT_CLI" 
			,GES_TBCTMSCF."SEC_RUT_CLI" as "SEC_RUT_CLI" 
			,GES_TBCTMSCF."TIPO_COMP_FAC" as "TIPO_COMP_FAC" 
			,GES_TBCTMSCF."FOLIO_COMP_FAC" as "FOLIO_COMP_FAC"  
			,GES_TBCTMSCF."MERCADO" as "MERCADO" 
			,GES_TBCTMSCF."PRODUCTO" as "PRODUCTO"
			,GES_TBCTMSCF."FOLIO_COMP_ASOC" as "FOLIO_COMP_ASOC"
			,GES_TBCTMSCF."FOLIO_COMP_CONT" as "FOLIO_COMP_CONT"
			,GES_TBCTMSCF."FEC_ING_COMP" as "FEC_ING_COMP"			
	    	FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC" =  GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC" = 1
	  	   	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(GES_TBCTMSCF."RUT_CLI") ELSE p_RutCliente END 
	  	   	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON POR."IdCliente" = CLI."IdCliente"
				AND POR."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMSCF."SEC_RUT_CLI")
     		INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTE
			  	ON  TIPOCOMPROBANTE."TipoValor" = 'TipoComprobante' 
			  	AND TIPOCOMPROBANTE."ValorOrigen" = TRIM(GES_TBCTMSCF."TIPO_COMP_FAC")
			INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CONDICIONLIQUIDACION
       			ON CONDICIONLIQUIDACION."Codigo" = CASE WHEN (IFNULL(GES_TBCTMSCF."IND_LIQUID",'')= '' or trim(GES_TBCTMSCF."IND_LIQUID")='') THEN 'PH' ELSE GES_TBCTMSCF."IND_LIQUID" END   
       		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
			  	ON  TIPOPRODUCTO."TipoValor" = 'TipoProducto' 
			  	AND TIPOPRODUCTO."Entidad" = 'TipoProducto'
			  	AND TIPOPRODUCTO."ValorOrigen" = GES_TBCTMSCF."PRODUCTO" 
       		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"  AS INSTRUMENTO
			    ON INSTRUMENTO."Nemotecnico"= TRIM(GES_TBCTDTCF."NEMO")
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento"  AS TIPOINSTRUMENTO
			    ON TIPOINSTRUMENTO."IdTipoInstrumento"=INSTRUMENTO."IdTipoInstrumento"
       		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  AS TIPOOPERACION
                ON TIPOOPERACION."TipoValor"='TipoOperacionFacturacion' 
                AND TIPOOPERACION."ValorOrigen"  = IFNULL(TRIM(GES_TBCTDTCF."COD_MOVTO"),'') 
                AND TIPOOPERACION."ValorOrigen2" = IFNULL(TRIM(GES_TBCTMSCF."MERCADO"),'')
 		        AND TIPOOPERACION."ValorOrigen3" = IFNULL(TRIM(GES_TBCTMSCF."PRODUCTO"),'')	
		        AND TIPOOPERACION."ValorOrigen4" = IFNULL(TRIM(GES_TBCTMSCF."TIPO_COMP_FAC"),'')
		    LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIRECCION_FISICA
		    	ON DIRECCION_FISICA."IdDireccionFisica" = CLI."IdDireccionFisicaPrincipal"
   			LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS IDAGENTE
   				ON IDAGENTE."CodigoAgenteMigracion" = GES_TBCTMSCF."COD_AGENTE"
   			LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUCURSAL
   			    ON SUCURSAL."CodigoSucursal" = 	GES_TBCTMSCF."COD_SUC"
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
		    WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND GES_TBCTMSCF."TIPO_COMP_FAC" IN ('FT','NT','FO','NO')-- dividiremso la consulta entre movimientos normales y otros conceptos
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL;

			-- log masivo partimos con los  otros conceptos
            -- la cabecera debe tener detalle
	  	   	insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
            ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'COMPROBANTE' as "Entidad"
                    ,'comprobantes otros conceptos TBCTMSPC No Tiene detalle en TBCTDTPC ' as "Mensaje"
                    , 'FOLIO_COMP_FAC'  as "NombreCampoGenerico1"
                    , GES_TBCTMSCF."FOLIO_COMP_FAC" as "ValorCampoGenerico1"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico2"
                    , GES_TBCTMSCF."TIPO_COMP_FAC" as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
            FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	LEFT JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC" =  GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC" = 1
           LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
	  	   	WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND GES_TBCTDTCF."FOLIO_COMP_FAC" IS NULL
			AND GES_TBCTMSCF."TIPO_COMP_FAC" NOT IN ('FT','NT','FO','NO')
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL; 
            
			-- todo movimiento debe estar asociado a un cliente
     	  	insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
            ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'COMPROBANTE' as "Entidad"
                    ,'comprobantes otros conceptos TBCTMSPC No Tiene detalle en TBCTDTPC ' as "Mensaje"
                    , 'RUT_CLI'  as "NombreCampoGenerico1"
                    , GES_TBCTMSCF."RUT_CLI" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0  as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0  as "ValorCampoGenerico7"
                    ,''  as "NombreCampoGenerico8"
                    , 0  as "ValorCampoGenerico8"
                    ,''  as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
            FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC" =  GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC" =1
	  	   	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(GES_TBCTMSCF."RUT_CLI") ELSE p_RutCliente END 	
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
	  	   	WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND CLI."DocumentoIdentidad" IS NULL
			AND GES_TBCTMSCF."TIPO_COMP_FAC" NOT IN ('FT','NT','FO','NO')
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL;

            -- el movimiento debe tener portafolio
            insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
            ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'COMPROBANTE' as "Entidad"
                    ,'comprobantes otros conceptos NO Existe Portafolio Creado Para el Cliente' as "Mensaje"
                    , 'RUT_CLI'  as "NombreCampoGenerico1"
                    , GES_TBCTMSCF."RUT_CLI" as "ValorCampoGenerico1"
                    , 'SEC_RUT_CLI' as "NombreCampoGenerico2"
                    , GES_TBCTMSCF."SEC_RUT_CLI" as "ValorCampoGenerico2"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico3"
                    , GES_TBCTMSCF."TIPO_COMP_FAC" as "ValorCampoGenerico3"
                    , 'FOLIO_COMP_FAC' as "NombreCampoGenerico4"
                    , GES_TBCTMSCF."FOLIO_COMP_FAC" as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , 'IdCliente' as "NombreCampoGenerico6"
                    ,  CLI."IdCliente"as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
            FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC" =  GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC" =1
	  	   	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(GES_TBCTMSCF."RUT_CLI") ELSE p_RutCliente END
			LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON POR."IdCliente" = CLI."IdCliente"
				AND POR."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMSCF."SEC_RUT_CLI") 	
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
	  	   	WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND CLI."DocumentoIdentidad" IS NULL
			AND GES_TBCTMSCF."TIPO_COMP_FAC" NOT IN ('FT','NT','FO','NO')
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL;

            -- el tipo comprobante debe estar homologado
           insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
            ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'COMPROBANTE' as "Entidad"
                    ,'comprobantes otros concep el tipo comprobante sebra no esta homologado' as "Mensaje"
                    , 'TIPO_COMP_FAC'  as "NombreCampoGenerico1"
                    , GES_TBCTMSCF."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , 'FOLIO_COMP_FAC'  as "NombreCampoGenerico2"
                    , GES_TBCTMSCF."FOLIO_COMP_FAC" as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0  as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
            FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC"  = GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC"  = 1
	  	   	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(GES_TBCTMSCF."RUT_CLI") ELSE p_RutCliente END
			INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON POR."IdCliente" = CLI."IdCliente"
				AND POR."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMSCF."SEC_RUT_CLI")
		    LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTE
			  	ON  TIPOCOMPROBANTE."TipoValor" = 'TipoComprobante' 
			  	AND TIPOCOMPROBANTE."ValorOrigen" = GES_TBCTMSCF."TIPO_COMP_FAC"
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
	  	   	WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND TIPOCOMPROBANTE."TipoValor"  IS NULL
			AND GES_TBCTMSCF."TIPO_COMP_FAC" NOT IN ('FT','NT','FO','NO')
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL;  
			
			--la condicion de liquidacion debe estar homologada
           insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
            ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'COMPROBANTE' as "Entidad"
                    ,'comprobantes otros concep cond.de liquid. sebra no se encuentra en tabla CondicionDeLiquidacion' as "Mensaje"
                    , 'TIPO_COMP_FAC'  as "NombreCampoGenerico1"
                    , GES_TBCTMSCF."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , 'FOLIO_COMP_FAC'  as "NombreCampoGenerico2"
                    , GES_TBCTMSCF."FOLIO_COMP_FAC" as "ValorCampoGenerico2"
                    , 'IND_LIQUID' as "NombreCampoGenerico3"
                    , GES_TBCTMSCF."IND_LIQUID"  as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0  as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
            FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC" =  GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC" =1
	  	   	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(GES_TBCTMSCF."RUT_CLI") ELSE p_RutCliente END
			INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON POR."IdCliente" = CLI."IdCliente"
				AND POR."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMSCF."SEC_RUT_CLI")
		    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTE
			  	ON  TIPOCOMPROBANTE."TipoValor" = 'TipoComprobante' 
			  	AND TIPOCOMPROBANTE."ValorOrigen" = GES_TBCTMSCF."TIPO_COMP_FAC"
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento"  AS INSTRUMENTO
			    ON INSTRUMENTO."Nemotecnico"=TRIM(GES_TBCTDTCF."NEMO")
		    LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CONDICIONLIQUIDACION
       			ON CONDICIONLIQUIDACION."Codigo" = CASE WHEN (IFNULL(GES_TBCTMSCF."IND_LIQUID",'')= '' or trim(GES_TBCTMSCF."IND_LIQUID")='') THEN 'PH' ELSE GES_TBCTMSCF."IND_LIQUID" END   
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
	  	   	WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND GES_TBCTMSCF."TIPO_COMP_FAC" NOT IN ('FT','NT','FO','NO')
			AND CONDICIONLIQUIDACION."Codigo" IS NULL
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL;
 
            -- partimos con los que no son otros conceptos
           	-- la cabecera debe tener detalle

	  	   	insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
            ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'COMPROBANTE' as "Entidad"
                    ,'TBCTMSPC No Tiene detalle en TBCTDTPC ' as "Mensaje"
                    , 'FOLIO_COMP_FAC'  as "NombreCampoGenerico1"
                    , GES_TBCTMSCF."FOLIO_COMP_FAC" as "ValorCampoGenerico1"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico2"
                    , GES_TBCTMSCF."TIPO_COMP_FAC" as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
            FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	LEFT JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC" =  GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC" = 1
           LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
	  	   	WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND GES_TBCTMSCF."TIPO_COMP_FAC" NOT IN ('FT','NT','FO','NO')
			AND GES_TBCTDTCF."FOLIO_COMP_FAC" IS NULL
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL;
     
     	  	-- todo movimiento debe estar asociado a un cliente
     	  	insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
            ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'COMPROBANTE' as "Entidad"
                    ,'TBCTMSPC No Tiene detalle en TBCTDTPC ' as "Mensaje"
                    , 'RUT_CLI'  as "NombreCampoGenerico1"
                    , GES_TBCTMSCF."RUT_CLI" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0  as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0  as "ValorCampoGenerico7"
                    ,''  as "NombreCampoGenerico8"
                    , 0  as "ValorCampoGenerico8"
                    ,''  as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
            FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC" =  GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC" =1
	  	   	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(GES_TBCTMSCF."RUT_CLI") ELSE p_RutCliente END 	
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
	  	   	WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND GES_TBCTMSCF."TIPO_COMP_FAC" NOT IN ('FT','NT','FO','NO')
			AND CLI."DocumentoIdentidad" IS NULL
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL;
     
           -- todo cliente debe tener portafolio 
           insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
            ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'COMPROBANTE' as "Entidad"
                    ,'NO Existe Portafolio Creado Para el Cliente' as "Mensaje"
                    , 'RUT_CLI'  as "NombreCampoGenerico1"
                    , GES_TBCTMSCF."RUT_CLI" as "ValorCampoGenerico1"
                    , 'SEC_RUT_CLI' as "NombreCampoGenerico2"
                    , GES_TBCTMSCF."SEC_RUT_CLI" as "ValorCampoGenerico2"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico3"
                    , GES_TBCTMSCF."TIPO_COMP_FAC" as "ValorCampoGenerico3"
                    , 'FOLIO_COMP_FAC' as "NombreCampoGenerico4"
                    , GES_TBCTMSCF."FOLIO_COMP_FAC" as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , 'IdCliente' as "NombreCampoGenerico6"
                    ,  CLI."IdCliente"as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
            FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC" =  GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC" =1
	  	   	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(GES_TBCTMSCF."RUT_CLI") ELSE p_RutCliente END
			LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON POR."IdCliente" = CLI."IdCliente"
				AND POR."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMSCF."SEC_RUT_CLI") 	
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
	  	   	WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND GES_TBCTMSCF."TIPO_COMP_FAC" NOT IN ('FT','NT','FO','NO')
			AND CLI."DocumentoIdentidad" IS NULL
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL;           
			
			-- el tipo comprobante debe estar homologado
           insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
            ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'COMPROBANTE' as "Entidad"
                    ,'el tipo comprobante sebra no esta homologado' as "Mensaje"
                    , 'TIPO_COMP_FAC'  as "NombreCampoGenerico1"
                    , GES_TBCTMSCF."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , 'FOLIO_COMP_FAC'  as "NombreCampoGenerico2"
                    , GES_TBCTMSCF."FOLIO_COMP_FAC" as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0  as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
            FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC" =  GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC" =1
	  	   	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(GES_TBCTMSCF."RUT_CLI") ELSE p_RutCliente END
			INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON POR."IdCliente" = CLI."IdCliente"
				AND POR."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMSCF."SEC_RUT_CLI")
		    LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTE
			  	ON  TIPOCOMPROBANTE."TipoValor" = 'TipoComprobante' 
			  	AND TIPOCOMPROBANTE."ValorOrigen" = GES_TBCTMSCF."TIPO_COMP_FAC"
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
	  	   	WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND GES_TBCTMSCF."TIPO_COMP_FAC" NOT IN ('FT','NT','FO','NO')
			AND TIPOCOMPROBANTE."TipoValor"  IS NULL
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL;    

			
			--la condicion de liquidacion debe estar homologada
           insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
            ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'COMPROBANTE' as "Entidad"
                    ,'la condicion de liquidacion sebra no se encuentra en la tabla CondicionDeLiquidacion' as "Mensaje"
                    , 'TIPO_COMP_FAC'  as "NombreCampoGenerico1"
                    , GES_TBCTMSCF."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , 'FOLIO_COMP_FAC'  as "NombreCampoGenerico2"
                    , GES_TBCTMSCF."FOLIO_COMP_FAC" as "ValorCampoGenerico2"
                    , 'IND_LIQUID' as "NombreCampoGenerico3"
                    , GES_TBCTMSCF."IND_LIQUID"  as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0  as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
            FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
	  	   	INNER JOIN "BSCL_VIRTUAL"."GES_TBCTDTCF" AS GES_TBCTDTCF 	
	  	   		ON  GES_TBCTDTCF."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF."TIPO_COMP_FAC" =  GES_TBCTMSCF."TIPO_COMP_FAC" 
	  	   		AND GES_TBCTDTCF. "LIN_COMP_FAC" =1
	  	   	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
				ON CLI."DocumentoIdentidad" = CASE WHEN p_RutCliente='        0-0' THEN TRIM(GES_TBCTMSCF."RUT_CLI") ELSE p_RutCliente END
			INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON POR."IdCliente" = CLI."IdCliente"
				AND POR."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMSCF."SEC_RUT_CLI")
		    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTE
			  	ON  TIPOCOMPROBANTE."TipoValor" = 'TipoComprobante' 
			  	AND TIPOCOMPROBANTE."ValorOrigen" = GES_TBCTMSCF."TIPO_COMP_FAC"
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento"  AS INSTRUMENTO
			    ON INSTRUMENTO."Nemotecnico"=TRIM(GES_TBCTDTCF."NEMO")
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento"  AS TIPOINSTRUMENTO
			    ON TIPOINSTRUMENTO."IdTipoInstrumento"=INSTRUMENTO."IdTipoInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CONDICIONLIQUIDACION
       			ON CONDICIONLIQUIDACION."Codigo" = CASE WHEN (IFNULL(GES_TBCTMSCF."IND_LIQUID",'') = '' or trim(GES_TBCTMSCF."IND_LIQUID")='') THEN 'PH' ELSE GES_TBCTMSCF."IND_LIQUID" END   
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
                ON HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
                AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
	  	   	WHERE GES_TBCTMSCF."FEC_ING_COMP" >= P_FechaInicial
			AND GES_TBCTMSCF."FEC_ING_COMP" <= P_FechaFinal
			AND GES_TBCTMSCF."TIPO_COMP_FAC" NOT IN ('FT','NT','FO','NO')
			AND CONDICIONLIQUIDACION."Codigo" IS NULL
			AND HOM_COMP."FOLIO_COMP_FAC" IS NULL;

          -- a las notas de credito le asocio su correspondiente factura
          update NOTASCREDITO SET NOTASCREDITO."IdTipoComprobanteRef"  = FACTURAS."IdTipoComprobante"
			             		 ,NOTASCREDITO."IdComprobanteRef" = FACTURAS."IdComprobante"
           FROM "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" as FACTURAS
               ,"BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" as NOTASCREDITO
           WHERE FACTURAS."TIPO_COMP_FAC" IN ('FA','FD','FO','FT','FE','FX')
            AND NOTASCREDITO."TIPO_COMP_FAC" IN ('NC','NO','ND','NT')
            AND FACTURAS."FOLIO_COMP_FAC" =  NOTASCREDITO."FOLIO_COMP_ASOC";
            

          -- actualizo aquellas notas de credito adicionales es decir que la factura fue anulada por do o mas notas de credito
          -- estas notas de credito no quedan bein referenciadas desde la factura asi que la nota de credito contiene el id de la factura
          -- el riesgo es que dicha nota de credito si voy solo por el folio comp fac podria asociarla erroneamente
          -- se asumira la siguiente duplas (queda pendiente)
		   	
	       select count(*) into v_total_com_gestion from "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_FAC
	       WHERE GES_FAC."FEC_ING_COMP" >= P_FechaInicial
		   AND GES_FAC."FEC_ING_COMP" <= P_FechaFinal
	       AND GES_FAC."RUT_CLI"  = CASE WHEN p_RutCliente='        0-0' THEN GES_FAC."RUT_CLI" ELSE p_RutCliente END;

		   SELECT COUNT(*) INTO v_total_mov_cargadas FROM "BSCL.Homologacion.BD::HOM.Comprobante"
		   WHERE  "IdCarga" = p_IdCarga;
			
			-- SACAMOS LOS REGISTROS PROCESADOS de los movimientos a facturar
		   	p_ResumenDatosProcesados =  select 'CARGAMOVIMIENTOFACTURACION' as "CodigoProceso" 
	   			  							,'FAC' as "Modulo" 
	   			 							,v_total_com_gestion as "TotalRegistrosSebra"
	   			  							,v_total_mov_cargadas as "TotalRegistrosSap"  FROM DUMMY
	   							    	union all
	   							    	select * from :p_ResumenDatosProcesados;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
