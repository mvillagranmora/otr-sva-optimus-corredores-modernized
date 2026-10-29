SET SCHEMA "BSCL"; 
DROP PROCEDURE   "BSCL.Homologacion.BD.Servicios::CargarMovimientosCuentaInversion";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarMovimientosCuentaInversion"
	 (
		 -- Parametros entrada comunes en todos los servicios publicos
		IN  p_IdEmpresa		      BIGINT,			-- ID Empresa destino
	    IN  p_IdUsuario		      BIGINT,			-- ID del usuario que solicita el servicio
		IN  p_IdCarga 			  BIGINT,           -- ID de la carga 
		IN  p_Modulo              NVARCHAR(3),      -- modulo 
	    IN  P_Modalidad           NVARCHAR(1),      -- Ingreso I incremental, 
	    IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
		IN  p_FechaInicial        NVARCHAR(10),     --YYYY-MM-DD
	    IN  p_FechaFinal		  NVARCHAR(10),     --YYYY-MM-DD
		-- Parametros salida del servicio publico
		-- Salidas para mensajes de advertencias y errores
	    OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),	
		OUT p_out_ErroresContador int,
		OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000))
	 ) 
	    
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA     
	AS
	BEGIN
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
		BEGIN
		-- VARIABLES VALIDACIONES BASICAS
	    
	    DECLARE v_erroroutmsg  				nvarchar(520);    
	    DECLARE v_TotalMovimientosLeidos	BIGINT:=0;
	    DECLARE v_MovimientosSeleccionados	BIGINT:=0;   
	    DECLARE v_TotalMovimientosGrabados	BIGINT:=0;

	    -- Para LOg  
	    DECLARE v_out_ErroresContador	BIGINT:=0; 
	    DECLARE v_FechaSistema NVARCHAR(40);

	   p_out_ErroresContador		:= 0;
	   p_out_ErroresDetalle		    = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		 
       SELECT "Valor" INTO v_FechaSistema
	   FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	   WHERE "CodigoParametro" = 'PLA.FechaSistema'
	   AND "IdEmpresa" = p_IdEmpresa;
	   
	  select lpad(p_RutCliente,11) into p_RutCliente from dummy;
      
      ---1) BUSCAMOS LOS MOVIMINETOS QUE AFECTAN A LA CUENTA DE INVERSION
      -- cargos  y abonos a contado
 
        INSERT INTO "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento"
      ("IdCargoAbonoMovimiento" ,"FechaSistema" ,"IdModulo" ,"IdTipoMovimiento" ,"EsIngresoManual" 
	  ,"IdCuentaInversion" ,"NombreCliente" ,"IdConcepto" ,"Descripcion" ,"IdCondicionDeLiquidacion" ,"FechaEfectivaMovimiento" 
	  ,"Monto" ,"Saldo" ,"IdEvento" ,"EsCertificadoAutomatico" ,"IdUsuarioCertificador" ,"FechaCertificacion" ,"IdTipoComprobante" 
	  ,"IdComprobante" ,"FechaComprobante"  ,"IdTipoProducto" ,"IdTipoOperacion" ,"IdOperacion" ,"FechaOperacion"
	  ,"FechaLiquidacionOperacion" ,"FechaVencimientoOperacion" ,"IdMonedaOperacion" ,"MontoOperacion" ,"TipoCambioOperacion"
	  ,"IdTipoInstrumento" ,"IdInstrumento" ,"Nemo" ,"EsCargoAbonoPorTraspaso" ,"IdCargoAbonoRelacionadoTraspaso" ,"EsIndicadorBilateral" 
	  ,"IdContraparte" ,"IdPersonaPagoCobro" ,"FechaIngresoRegistro" ,"IdUsuario" ,"IdEmpresa" ,"IdEstado" ,"IdCarga" 
	  ,"SecuenciaMovimientoMigracion","RUT_CLI","SEC_RUT_CLI","FEC_MVTO","SEC_MOVTO"	,"COD_MOVTO","FOLIO_COMP_ADJ"
	  ,"TIPO_COMP_ADJ","LIN_COMP_ADJ","COD_BOVEDA","COD_SUC","FEC_TRANS","FEC_LIQUID","FEC_ING_MVTO","IND_TIT_CCTE","COD_PER"
	  ,"FechaMovimientoMigracion")
       SELECT 
       "BSCL.CuentaInversion.BD.Secuencias::CargoAbonoMovimiento".NEXTVAL  as "IdCargoAbonoMovimiento"
       ,v_FechaSistema as "FechaSistema" 
       ,'FAC' as "IdModulo"
       , COALESCE (CASE WHEN MVTO."IND_CCTE_CTDO" = 'A' THEN 'MOVABN' ELSE 'MOVCAR' END,'') as "IdTipoMovimiento"
       ,'S' as "EsIngresoManual"
       ,Cuenta."IdCuentaInversion"
       ,Cliente."NombreCompleto"
       ,'' AS "IdConcepto" -- SE DEBE REVISAR DE DONDE SACAR LOS CONCEPTOS
       ,CODIGO."DESCRIP_MOVTO" as  "Descripcion"
       , CASE WHEN MVTO."IND_LIQUID" = 'PH' THEN 'T0'
			  WHEN MVTO."IND_LIQUID" = 'PM' THEN 'T1'
			  WHEN MVTO."IND_LIQUID" = 'CN' THEN 'T2'
			  WHEN MVTO."IND_LIQUID" = 'T3' THEN 'T3'
			  ELSE 'T0'
	     END AS "CondicionLiquidacion"
       ,MVTO."FEC_MVTO" as "FechaEfectivaMovimiento"
       ,MVTO."MONTO" as "Monto"
       ,0 AS "Saldo"
       ,-1 AS "IdEvento"
       ,'S' AS "EsCertificadoAutomatico" 
       ,0 AS "IdUsuarioCertificador" 
       ,MVTO."FEC_MVTO" AS "FechaCertificacion"
       , '' as "IdTipoComprobante"
       , 0  as "IdComprobante"
       ,'0001-01-01' as "FechaComprobante"
       ,'' as "IdTipoProducto"
       ,'' as "IdTipoOperacion"
       ,0  as "IdOperacion"
       ,MVTO."FEC_MVTO" as "FechaOperacion"
       ,CASE when MVTO."FEC_LIQUID" = '0001-01-01' THEN MVTO."FEC_MVTO" ELSE MVTO."FEC_LIQUID" END as "FechaLiquidacionOperacion"
       ,MVTO."FEC_VCTO" AS "FechaVencimientoOperacion"
       ,CASE WHEN CODIGO."IND_TIT_CCTE" = 'C' THEN 'CLP'
             WHEN CODIGO."IND_TIT_CCTE" = 'V' THEN 'EUR'
             WHEN CODIGO."IND_TIT_CCTE" = 'U' THEN 'USD'
        END as "IdMonedaOperacion"
       ,MVTO."MONTO" as "MontoOperacion"
       , 0 as "TipoCambioOperacion"
       ,COALESCE(INS."IdTipoInstrumento",'')  as "IdTipoInstrumento"
       ,COALESCE(INS."IdInstrumento",0) as "IdInstrumento"
       ,COALESCE(INS."Nemotecnico",'') as "Nemo"
       ,'N' as "EsCargoAbonoPorTraspaso"
       ,0 as "IdCargoAbonoRelacionadoTraspaso"  
       ,'N' as "EsIndicadorBilateral"
       ,0 as "IdContraparte"
       ,0 as "IdPersonaPagoCobro"
       ,MVTO."FEC_ING_MVTO" as "FechaIngresoRegistro"
       ,0 AS "IdUsuario"
       ,P_IdEmpresa as "IdEmpresa"
       ,'CRT' as "Estado"
       ,P_IdCarga as "IdCarga"
        ,MVTO."SEC_MOVTO" as "SecuenciaMovimientoMigracion"
		,MVTO."RUT_CLI"			as "RUT_CLI"		
		,MVTO."SEC_RUT_CLI"		as "SEC_RUT_CLI"	
		,MVTO."FEC_MVTO"		as "FEC_MVTO"		
		,MVTO."SEC_MOVTO"		as "SEC_MOVTO"		
		,MVTO."COD_MOVTO"		as "COD_MOVTO"		
		,MVTO."FOLIO_COMP_ADJ"	as "FOLIO_COMP_ADJ"	
		,MVTO."TIPO_COMP_ADJ"	as "TIPO_COMP_ADJ"	
		,MVTO."LIN_COMP_ADJ"	as "LIN_COMP_ADJ"	
		,MVTO."COD_BOVEDA"		as "COD_BOVEDA"		
		,MVTO."COD_SUC"			as "COD_SUC"		
		,MVTO."FEC_TRANS"     	as "FEC_TRANS"      
		,MVTO."FEC_LIQUID"     	as "FEC_LIQUID"     
		,MVTO."FEC_ING_MVTO"	as "FEC_ING_MVTO"	
		,MVTO."IND_TIT_CCTE"   	as "IND_TIT_CCTE" 
		,SUBSTRING(MVTO."FEC_MVTO",1,7)	as "COD_PER" 
		,MVTO."FEC_MVTO" as "FechaMovimientoMigracion"  
       FROM 
       "BSCL_VIRTUAL"."GES_TBCTMVTO" AS MVTO
       INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cliente 
			ON Cliente."DocumentoIdentidad" = TRIM(MVTO."RUT_CLI")
   	   INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio 
			ON Portafolio."IdCliente" = Cliente."IdCliente"
			AND Portafolio."CodigoPortafolio" = TO_VARCHAR(MVTO."SEC_RUT_CLI")
	   INNER JOIN "BSCL_VIRTUAL"."GES_TBPLTMOV" AS CODIGO
	   		ON CODIGO."TIPO_REG" ='CM'
	   		AND CODIGO."CODIGO" = MVTO."COD_MOVTO"
	   INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as Cuenta
	   		ON Cuenta."IdPortafolio" = Portafolio."IdPortafolio"
	   		AND Cuenta."IdCliente" = Cliente."IdCliente"
	   		AND Cuenta."IdMoneda" = CASE 
	   		                             WHEN CODIGO."IND_TIT_CCTE" = 'U' THEN 'USD'
	   		                             WHEN CODIGO."IND_TIT_CCTE" = 'C' THEN 'CLP'
	   		                             WHEN CODIGO."IND_TIT_CCTE" = 'V' THEN 'EUR'
	   		                        END
	   LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" Empleado 
	   		ON Empleado."CodigoAgenteMigracion" = MVTO."COD_AGENTE"
	   		AND Empleado."IdCarga" > 0
	   LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTEFAC
	   		ON  TIPOCOMPROBANTEFAC."TipoValor" = 'TipoComprobante'
	   		AND	TIPOCOMPROBANTEFAC."Entidad" = 'FACTURA' 
	       	AND TIPOCOMPROBANTEFAC."ValorOrigen" = MVTO."TIPO_COMP_ADJ"
	   LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" as Comprobante
	   		ON  Comprobante."IdCarga" > 0
	   		AND Comprobante."FolioComprobanteFacturacionMigracion" = MVTO."FOLIO_COMP_ADJ"
	   		AND Comprobante."IdTipoComprobante" = TIPOCOMPROBANTEFAC."ValorDestino"
	   LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as INS
	   		ON INS."Nemotecnico" = MVTO."NEMO"
	   LEFT JOIN "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento" AS TEMP
	   		ON  TEMP."SEC_MOVTO" = MVTO."SEC_MOVTO"
	   		AND TEMP."FEC_MVTO" = MVTO."FEC_MVTO"
	   		AND TEMP."IdTipoMovimiento" = COALESCE (CASE WHEN MVTO."IND_CCTE_CTDO" = 'A' THEN 'MOVABN' ELSE 'MOVCAR' END,'')
	   WHERE MVTO."IND_TIT_CCTE" IN ('C','V','U')--solo cargos a contado
         	AND MVTO."EST_MOVTO" IN ('P','T')  
         	AND MVTO."IND_ANULADO" = 'I'
         	AND MVTO."IND_CCTE_CTDO" in ('C','A')	
         	AND MVTO."IND_SALDO_CTDO" = 'S'
         	AND MVTO."RUT_CLI" = CASE WHEN p_RutCliente = '        0-0' THEN MVTO."RUT_CLI" ELSE p_RutCliente END
            AND ((MVTO."FEC_LIQUID" BETWEEN p_FechaInicial AND p_FechaFinal) OR (MVTO."FEC_MVTO" BETWEEN p_FechaInicial AND p_FechaFinal))
         	AND TEMP."SEC_MOVTO" IS NULL;

       /*   
      --cargos abonos a plazo  
      INSERT INTO "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento"
      ("IdCargoAbonoMovimiento" ,"FechaSistema" ,"IdModulo" ,"IdTipoMovimiento" ,"EsIngresoManual" 
	  ,"IdCuentaInversion" ,"NombreCliente" ,"IdConcepto" ,"Descripcion" ,"IdCondicionDeLiquidacion" ,"FechaEfectivaMovimiento" 
	  ,"Monto" ,"Saldo" ,"IdEvento" ,"EsCertificadoAutomatico" ,"IdUsuarioCertificador" ,"FechaCertificacion" ,"IdTipoComprobante" 
	  ,"IdComprobante" ,"FechaComprobante"  ,"IdTipoProducto" ,"IdTipoOperacion" ,"IdOperacion" ,"FechaOperacion"
	  ,"FechaLiquidacionOperacion" ,"FechaVencimientoOperacion" ,"IdMonedaOperacion" ,"MontoOperacion" ,"TipoCambioOperacion"
	  ,"IdTipoInstrumento" ,"IdInstrumento" ,"Nemo" ,"EsCargoAbonoPorTraspaso" ,"IdCargoAbonoRelacionadoTraspaso" ,"EsIndicadorBilateral" 
	  ,"IdContraparte" ,"IdPersonaPagoCobro" ,"FechaIngresoRegistro" ,"IdUsuario" ,"IdEmpresa" ,"IdEstado" ,"IdCarga" 
	  ,"SecuenciaMovimientoMigracion","RUT_CLI","SEC_RUT_CLI","FEC_MVTO","SEC_MOVTO"	,"COD_MOVTO","FOLIO_COMP_ADJ"
	  ,"TIPO_COMP_ADJ","LIN_COMP_ADJ","COD_BOVEDA","COD_SUC","FEC_TRANS","FEC_LIQUID","FEC_ING_MVTO","IND_TIT_CCTE","COD_PER"
	  ,"FechaMovimientoMigracion")
       SELECT 
       "BSCL.CuentaInversion.BD.Secuencias::CargoAbonoMovimiento".NEXTVAL  as "IdCargoAbonoMovimiento"
       ,v_FechaSistema as "FechaSistema" 
       ,'FAC' as "IdModulo"
       , COALESCE (CASE WHEN MVTO."IND_CCTE_APLAZO" = 'A' THEN 'MOVABN' ELSE 'MOVCAR' END,'') as "IdTipoMovimiento"
       ,'S' as "EsIngresoManual"
       ,Cuenta."IdCuentaInversion"
       ,Cliente."NombreCompleto"
       ,'' AS "IdConcepto" -- SE DEBE REVISAR DE DONDE SACAR LOS CONCEPTOS
       ,CODIGO."DESCRIP_MOVTO" as  "Descripcion"
       , CASE WHEN MVTO."IND_LIQUID" = 'PH' THEN 'T0'
			  WHEN MVTO."IND_LIQUID" = 'PM' THEN 'T1'
			  WHEN MVTO."IND_LIQUID" = 'CN' THEN 'T2'
			  WHEN MVTO."IND_LIQUID" = 'T3' THEN 'T3'
			  ELSE 'T0'
	    END AS "CondicionLiquidacion"
       --,case when MVTO."FEC_LIQUID" < MVTO."FEC_MVTO" THEN MVTO."FEC_VCTO" ELSE MVTO."FEC_LIQUID" END as "FechaEfectivaMovimiento"
       ,MVTO."FEC_VCTO" as "FechaEfectivaMovimiento"
       ,MVTO."MONTO" as "Monto"
       ,0 AS "Saldo"
       ,0 AS "IdEvento"
       ,'S' AS "EsCertificadoAutomatico" 
       ,0 AS "IdUsuarioCertificador" 
       ,MVTO."FEC_LIQUID" AS "FechaCertificacion"
       , '' as "IdTipoComprobante"
       , 0  as "IdComprobante"
       ,'0001-01-01' as "FechaComprobante"
       ,'' as "IdTipoProducto"
       ,'' as "IdTipoOperacion"
       ,0  as "IdOperacion"
       ,MVTO."FEC_MVTO" as "FechaOperacion"
       ,MVTO."FEC_VCTO" as "FechaLiquidacionOperacion"
       ,MVTO."FEC_VCTO" AS "FechaVencimientoOperacion"
       ,CASE WHEN CODIGO."IND_TIT_CCTE" = 'C' THEN 'CLP'
             WHEN CODIGO."IND_TIT_CCTE" = 'V' THEN 'EUR'
             WHEN CODIGO."IND_TIT_CCTE" = 'U' THEN 'USD'
        END as "IdMonedaOperacion"
       ,MVTO."MONTO" as "MontoOperacion"
       , 0 as "TipoCambioOperacion"
       ,COALESCE(INS."IdTipoInstrumento",'')  as "IdTipoInstrumento"
       ,COALESCE(INS."IdInstrumento",0) as "IdInstrumento"
       ,COALESCE(INS."Nemotecnico",'') as "Nemo"
       ,'N' as "EsCargoAbonoPorTraspaso"
       ,0 as "IdCargoAbonoRelacionadoTraspaso"  
       ,'N' as "EsIndicadorBilateral"
       ,0 as "IdContraparte"
       ,0 as "IdPersonaPagoCobro"
       ,MVTO."FEC_ING_MVTO" as "FechaIngresoRegistro"
       ,0 AS "IdUsuario"
       ,P_IdEmpresa as "IdEmpresa"
       ,'CRT' as "Estado"
       ,P_IdCarga as "IdCarga"
        ,MVTO."SEC_MOVTO" as "SecuenciaMovimientoMigracion"
		,MVTO."RUT_CLI"			as "RUT_CLI"		
		,MVTO."SEC_RUT_CLI"		as "SEC_RUT_CLI"	
		,MVTO."FEC_MVTO"		as "FEC_MVTO"		
		,MVTO."SEC_MOVTO"		as "SEC_MOVTO"		
		,MVTO."COD_MOVTO"		as "COD_MOVTO"		
		,MVTO."FOLIO_COMP_ADJ"	as "FOLIO_COMP_ADJ"	
		,MVTO."TIPO_COMP_ADJ"	as "TIPO_COMP_ADJ"	
		,MVTO."LIN_COMP_ADJ"	as "LIN_COMP_ADJ"	
		,MVTO."COD_BOVEDA"		as "COD_BOVEDA"		
		,MVTO."COD_SUC"			as "COD_SUC"		
		,MVTO."FEC_TRANS"     	as "FEC_TRANS"      
		,MVTO."FEC_LIQUID"     	as "FEC_LIQUID"     
		,MVTO."FEC_ING_MVTO"	as "FEC_ING_MVTO"	
		,MVTO."IND_TIT_CCTE"   	as "IND_TIT_CCTE" 
		,SUBSTRING(MVTO."FEC_MVTO",1,7)	as "COD_PER" 
		,MVTO."FEC_MVTO" as "FechaMovimientoMigracion"  
       FROM 
       "BSCL_VIRTUAL"."GES_TBCTMVTO" AS MVTO
       INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cliente 
			ON Cliente."DocumentoIdentidad" = TRIM(MVTO."RUT_CLI")
   	   INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio 
			ON Portafolio."IdCliente" = Cliente."IdCliente"
			AND Portafolio."CodigoPortafolio" = TO_VARCHAR(MVTO."SEC_RUT_CLI")
	   INNER JOIN "BSCL_VIRTUAL"."GES_TBPLTMOV" AS CODIGO
	   		ON CODIGO."TIPO_REG" ='CM'
	   		AND CODIGO."CODIGO" = MVTO."COD_MOVTO"
	   INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as Cuenta
	   		ON Cuenta."IdPortafolio" = Portafolio."IdPortafolio"
	   		AND Cuenta."IdCliente" = Cliente."IdCliente"
	   		AND Cuenta."IdMoneda" = CASE 
	   		                             WHEN CODIGO."IND_TIT_CCTE" = 'U' THEN 'USD'
	   		                             WHEN CODIGO."IND_TIT_CCTE" = 'C' THEN 'CLP'
	   		                             WHEN CODIGO."IND_TIT_CCTE" = 'V' THEN 'EUR'
	   		                        END
	   LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" Empleado 
	   		ON Empleado."CodigoAgenteMigracion" = MVTO."COD_AGENTE"
	   		AND Empleado."IdCarga" > 0
	   LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTEFAC
	   		ON  TIPOCOMPROBANTEFAC."TipoValor" = 'TipoComprobante'
	   		AND	TIPOCOMPROBANTEFAC."Entidad" = 'FACTURA' 
	       	AND TIPOCOMPROBANTEFAC."ValorOrigen" = MVTO."TIPO_COMP_ADJ"
	   LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" as Comprobante
	   		ON  Comprobante."IdCarga" > 0
	   		AND Comprobante."FolioComprobanteFacturacionMigracion" = MVTO."FOLIO_COMP_ADJ"
	   		AND Comprobante."IdTipoComprobante" = TIPOCOMPROBANTEFAC."ValorDestino"
	   LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as INS
	   		ON INS."Nemotecnico" = MVTO."NEMO"
	   LEFT JOIN "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento" AS TEMP
	   	ON TEMP."SEC_MOVTO" = MVTO."SEC_MOVTO"
	   	AND TEMP."FEC_MVTO" = MVTO."FEC_MVTO"
        AND TEMP."IdTipoMovimiento" = COALESCE (CASE WHEN MVTO."IND_CCTE_APLAZO" = 'A' THEN 'MOVABN' ELSE 'MOVCAR' END,'') 
       WHERE MVTO."IND_TIT_CCTE" IN ('C','V','U')--solo cargos a contado
         AND MVTO."EST_MOVTO" IN ('P','T')  
         AND MVTO."IND_ANULADO" = 'I'
         AND MVTO."IND_CCTE_APLAZO" in ('C','A')	
         AND MVTO."IND_SALDO_APLAZO" IN ('S','T')
         AND MVTO."RUT_CLI" = CASE WHEN p_RutCliente = '        0-0' THEN MVTO."RUT_CLI" ELSE p_RutCliente END
         AND ((MVTO."FEC_LIQUID" BETWEEN p_FechaInicial AND p_FechaFinal) OR (MVTO."FEC_MVTO" BETWEEN p_FechaInicial AND p_FechaFinal))
         AND TEMP."SEC_MOVTO" IS NULL;
       */ 

         SELECT COUNT(1) INTO v_MovimientosSeleccionados 
	     FROM "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento"
	     where "IdCarga" = p_IdCarga
	      and "IdConcepto" not in ('CAMIGR','ABMIGR');  
	  
        -------------------------------------
        -- actualizo los saldos anteriores  -
        -------------------------------------

	     IF v_MovimientosSeleccionados > 0 THEN
	       --------------------------------------------------------------------
           -- debo buscar todos las cuentas inversiones afectadas            --
           -- para de esa forma usarlas para buscar los movimientos          --
           -- anteriores cargados para calcular el saldo de los movimientos  --
           -- nuevos a cargar                                                --
	       -------------------------------------------------------------------- 

           -- ordeno los movimentos y les entrego un id esto lo hacemos para 
           -- calcular el saldo a fecha de movimiento saldo caja

           movimientos_entrada =  SELECT   ROW_NUMBER () OVER (order by "IdCuentaInversion" asc,"FechaEfectivaMovimiento" asc, "SecuenciaMovimientoMigracion" asc, "IdTipoMovimiento" asc) as "ID",*
                                  FROM "BSCL"."BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento"
                                  ORDER BY "ID" ASC;
   
           movimientos_salida  =   SELECT * FROM :movimientos_entrada ORDER BY "ID" ASC;
          
           saldos_por_movimiento = SELECT "IdCargoAbonoMovimiento",
                                   IFNULL((
                                          SELECT SUM(T1."Monto" * CASE WHEN (T1."IdTipoMovimiento" = 'MOVCAR' AND T1."Monto" > 0) THEN -1 ELSE 1 END)
                                           FROM :movimientos_salida AS T1 
                                           WHERE T1."IdCuentaInversion" = T2."IdCuentaInversion"
                                           AND T1."ID" < T2."ID"  
                                    ),0) + (T2."Monto" * CASE WHEN (T2."IdTipoMovimiento" = 'MOVCAR' AND T2."Monto" > 0) THEN -1 ELSE 1 END) as "Saldo"
                                    FROM :movimientos_salida AS T2
                                    ORDER BY T2."ID" asc;

           -- ordeno los movimentos y les entrego un id esto lo hacemos para 
           -- calcular el saldo a fecha de liquidacion saldo caja

           movimientos_entrada_liq =  SELECT   ROW_NUMBER () OVER (order by "IdCuentaInversion" asc ,"FechaLiquidacionOperacion" asc ,"SecuenciaMovimientoMigracion" asc , "IdTipoMovimiento" asc) as "ID",*
                                      FROM "BSCL"."BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento"
                                      ORDER BY "ID" ASC;                  
                                  
           movimientos_salida_liq  = SELECT * FROM :movimientos_entrada_liq ORDER BY "ID" ASC;
          
           saldos_por_movimiento_liq = SELECT "IdCargoAbonoMovimiento",
                                   IFNULL((SELECT SUM(T1."Monto" * CASE WHEN (T1."IdTipoMovimiento" = 'MOVCAR' AND T1."Monto" > 0) THEN -1 ELSE 1 END)
                                           FROM :movimientos_salida_liq AS T1 
                                           WHERE T1."IdCuentaInversion" = T2."IdCuentaInversion"
                                           AND T1."ID" < T2."ID"
                                    ),0) + (T2."Monto" * CASE WHEN (T2."IdTipoMovimiento" = 'MOVCAR' AND T2."Monto" > 0) THEN -1 ELSE 1 END) as "Saldo"
                                    FROM :movimientos_entrada_liq AS T2
                                    ORDER BY T2."ID" asc;
            
            -- actualizamos los saldo a fecha de movimiento fec_mvto     
            update MOV SET MOV."SaldoPorFechaMovimiento" = SAL."Saldo"
            FROM "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento" AS MOV
	        ,:saldos_por_movimiento AS SAL
	        WHERE SAL."IdCargoAbonoMovimiento" = MOV."IdCargoAbonoMovimiento"; 

	        --actualizamos los saldo a fecha de liquidacion fec_liquid     
            update MOV SET MOV."Saldo" = SAL."Saldo"
            FROM "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento" AS MOV
	        ,:saldos_por_movimiento_liq AS SAL
	        WHERE SAL."IdCargoAbonoMovimiento" = MOV."IdCargoAbonoMovimiento"; 

	    END IF; 

     INSERT INTO "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" 
       		 ("IdCargoAbonoMovimiento","FechaSistema" ,"IdModulo" ,"IdTipoMovimiento","EsIngresoManual" ,"IdCuentaInversion" ,"NombreCliente"
			,"IdConcepto" ,"Descripcion","IdCondicionDeLiquidacion" ,"FechaEfectivaMovimiento","Monto","Saldo","SaldoPorFechaMovimiento","IdEvento" 
			,"EsCertificadoAutomatico","IdUsuarioCertificador" ,"FechaCertificacion","IdTipoComprobante" ,"IdComprobante","FechaComprobante"
			,"IdTipoProducto","IdTipoOperacion" ,"IdOperacion","FechaOperacion","FechaLiquidacionOperacion","FechaVencimientoOperacion","IdMonedaOperacion" 
			,"MontoOperacion","TipoCambioOperacion" ,"IdTipoInstrumento" ,"IdInstrumento","Nemo","EsCargoAbonoPorTraspaso","IdCargoAbonoRelacionadoTraspaso"
			,"EsIndicadorBilateral","IdContraparte","IdPersonaPagoCobro","FechaIngresoRegistro","IdUsuario","IdEmpresa","IdEstado" ,"IdCarga","SecuenciaMovimientoMigracion" 
			,"FechaMovimientoMigracion","CodigoPeriodo") 
   
       SELECT HOM."IdCargoAbonoMovimiento",HOM."FechaSistema" ,HOM."IdModulo" ,HOM."IdTipoMovimiento",HOM."EsIngresoManual" ,HOM."IdCuentaInversion" ,HOM."NombreCliente"
			,HOM."IdConcepto" ,HOM."Descripcion",HOM."IdCondicionDeLiquidacion" ,HOM."FechaEfectivaMovimiento",HOM."Monto",HOM."Saldo",HOM."SaldoPorFechaMovimiento",HOM."IdEvento" 
			,HOM."EsCertificadoAutomatico",HOM."IdUsuarioCertificador" ,HOM."FechaCertificacion",HOM."IdTipoComprobante" ,HOM."IdComprobante",HOM."FechaComprobante"
			,HOM."IdTipoProducto",HOM."IdTipoOperacion" ,HOM."IdOperacion",HOM."FechaOperacion",HOM."FechaLiquidacionOperacion",HOM."FechaVencimientoOperacion",HOM."IdMonedaOperacion" 
			,HOM."MontoOperacion",HOM."TipoCambioOperacion" ,HOM."IdTipoInstrumento" ,HOM."IdInstrumento",HOM."Nemo",HOM."EsCargoAbonoPorTraspaso",HOM."IdCargoAbonoRelacionadoTraspaso"
			,HOM."EsIndicadorBilateral",HOM."IdContraparte",HOM."IdPersonaPagoCobro",HOM."FechaIngresoRegistro",HOM."IdUsuario",HOM."IdEmpresa",HOM."IdEstado" ,HOM."IdCarga",HOM."SecuenciaMovimientoMigracion" 
			,HOM."FechaMovimientoMigracion",HOM."COD_PER"
	    FROM "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento" AS HOM
	    LEFT JOIN  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CTA
	    ON CTA."SecuenciaMovimientoMigracion" = HOM."SecuenciaMovimientoMigracion"
	    AND CTA."FechaMovimientoMigracion" = HOM."FechaMovimientoMigracion"
	    AND CTA."IdTipoMovimiento" = HOM."IdTipoMovimiento"
	    WHERE HOM."IdCarga" = p_IdCarga
	    AND HOM."IdConcepto" not in ('ABMIGR','CAMIGR')
	    AND CTA."SecuenciaMovimientoMigracion" IS NULL;
	    
	    -------------------------------------------------
		-- REVISAMOS SI HUBO ERROR EN LA CARGA ----------
        -------------------------------------------------
	    SELECT COUNT(*) INTO v_TotalMovimientosGrabados	
	    FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" 
	    WHERE "IdCarga" = p_IdCarga
	     AND "IdConcepto" NOT IN ('CAMIGR','ABMIGR');--se descuenta los abono y cargos iniciales por migracion

	    IF v_MovimientosSeleccionados <> v_TotalMovimientosGrabados THEN
	    	v_hash= select 'Valor1' as "Codigo", v_MovimientosSeleccionados   as "Valor" FROM DUMMY UNION ALL
			        select 'Valor2' as "Codigo", v_TotalMovimientosGrabados as "Valor" FROM DUMMY;
			CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ERROR_CARGA_CTA_INV',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'ERROR_CARGA_CTA_INV' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy; 
			p_out_ErroresContador := p_out_ErroresContador + 1;
	    END IF;
	    
        -- SACAMOS LOS REGISTROS PROCESADOS de los movimientos a facturar
	   	p_ResumenDatosProcesados =  select 'CARGAMOVIMIENTOCUENTAINVERSION' as "CodigoProceso" 
   			  							,'CTA' as "Modulo" 
   			 							,v_MovimientosSeleccionados as "TotalRegistrosSebra"
   			  							,v_TotalMovimientosGrabados as "TotalRegistrosSap"  FROM DUMMY
   							    	union all
   							    	select * from :p_ResumenDatosProcesados;
   							    
        -- busco los ultimos movimiento de cada una de las cuenta de inversion por cada periodo 
        -- ordeno de la misma forma que procese los movimientos 
        -- primero por fecha de movimiento

       saldo  = select * from 			       		 
						(			       		 
							select	 
									RANK() over (partition by  "IdCuentaInversion",SUBSTRING("FechaEfectivaMovimiento",1,7) order by "IdCuentaInversion" desc,"FechaEfectivaMovimiento" desc, "SecuenciaMovimientoMigracion" desc, "IdTipoMovimiento" desc) as RANK
									, "IdCuentaInversion"
									, SUBSTRING("FechaEfectivaMovimiento",1,7) as "COD_PER"
									, "SaldoPorFechaMovimiento" as "SaldoFechaMovimiento"
							from "BSCL"."BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento"
						 )
				where RANK = 1; 
         
         
	    -- busco los ultimos movimiento de cada una de las cuenta de inversion por cada periodo 
        -- ordeno de la misma forma que procese los movimientos 
        -- primero por fecha de liquidacion

        saldo_liquid = 	select * from 			       		 
						(			       		 
							select	 
									RANK() over (partition by  "IdCuentaInversion",SUBSTRING("FechaLiquidacionOperacion",1,7) order by "IdCuentaInversion" desc ,"FechaLiquidacionOperacion" desc ,"SecuenciaMovimientoMigracion" desc , "IdTipoMovimiento" desc) as RANK
									, "IdCuentaInversion"
									, SUBSTRING("FechaLiquidacionOperacion",1,7) as "COD_PER"
									, "Saldo"
							from "BSCL"."BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento"
						 )
						where RANK = 1;
		

       -- datos en sebra nos traemos los monto de la cuenta de inversion de sebra y comparamos con 
       -- nuestros saldos calculados
       datos_sebra = SELECT HOM."COD_PER"
				       ,HOM."IdCuentaInversion"
				       ,CLI."IdCliente"
				       ,POR."CodigoPortafolio"
				       ,CTA."IdMoneda"
				       ,TBCL."RUT_CLI"
				       ,TBCL."SEC_RUT_CLI"
				       ,(TBCL."SALDO_DISP"+TBCL."SALDO_RETEN") AS "SaldoSebra"
				       ,HOM."SaldoFechaMovimiento"
                      ,CASE WHEN (TBCL."SALDO_DISP"+TBCL."SALDO_RETEN") <> HOM."SaldoFechaMovimiento" THEN 'DESCUADRATURA'	ELSE 'CUADRADO' END as "Cuadratura"
                      FROM :saldo AS HOM
                      INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTA
                      ON CTA."IdCuentaInversion" = HOM."IdCuentaInversion"
                      INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
                      ON POR."IdPortafolio" = CTA."IdPortafolio"
                      INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
                      ON CLI."IdCliente" = POR."IdCliente"
                      INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
                      ON PER."IdPersona" = CLI."IdPersona"
                      INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
                      ON DOC."IdPersona" = PER."IdPersona"
                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTCTCL" AS TBCL
                      ON TBCL."COD_PER" = HOM."COD_PER"
                      AND TBCL."RUT_CLI" = LPAD(DOC."DocumentoIdentidad",11,' ')
                      AND TBCL."SEC_RUT_CLI" = POR."CodigoPortafolio"
                      AND TBCL."CTA_CLI" = CASE WHEN CTA."IdMoneda" = 'CLP' THEN 'CCTE'
                                                WHEN CTA."IdMoneda" = 'USD' THEN 'CDOL'
                                                WHEN CTA."IdMoneda" = 'EUR' THEN 'EURO'
                                           END;
                                           
         datos_sebra_liq = SELECT HOM."COD_PER"
				       ,HOM."IdCuentaInversion"
				       ,CLI."IdCliente"
				       ,POR."CodigoPortafolio"
				       ,CTA."IdMoneda"
				       ,TBCL."RUT_CLI"
				       ,TBCL."SEC_RUT_CLI"
				       ,TBCL."SALDO_DISP" AS "SaldoSebraliq"
				       ,HOM."Saldo"
                      ,CASE WHEN TBCL."SALDO_DISP" <> HOM."Saldo" THEN 'DESCUADRATURA'	ELSE 'CUADRADO' END as "Cuadratura"
                      FROM :saldo_liquid AS HOM
                      INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTA
                      ON CTA."IdCuentaInversion" = HOM."IdCuentaInversion"
                      INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
                      ON POR."IdPortafolio" = CTA."IdPortafolio"
                      INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
                      ON CLI."IdCliente" = POR."IdCliente"
                      INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
                      ON PER."IdPersona" = CLI."IdPersona"
                      INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
                      ON DOC."IdPersona" = PER."IdPersona"
                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTCTCL" AS TBCL
                      ON TBCL."COD_PER" = HOM."COD_PER"
                      AND TBCL."RUT_CLI" = LPAD(DOC."DocumentoIdentidad",11,' ')
                      AND TBCL."SEC_RUT_CLI" = POR."CodigoPortafolio"
                      AND TBCL."CTA_CLI" = CASE WHEN CTA."IdMoneda" = 'CLP' THEN 'CCTE'
                                                WHEN CTA."IdMoneda" = 'USD' THEN 'CDOL'
                                                WHEN CTA."IdMoneda" = 'EUR' THEN 'EURO'
                                           END;                                           

			select     fec_mov."COD_PER"
					  ,fec_mov."IdCuentaInversion"
				      ,fec_mov."IdCliente"
				      ,fec_mov."CodigoPortafolio"
				      ,fec_mov."IdMoneda"
				      ,fec_mov."RUT_CLI"
				      ,fec_mov."SEC_RUT_CLI" 
				      ,fec_mov."SaldoSebra" as "SaldoSebraMovimiento"
				      ,fec_mov."SaldoFechaMovimiento" as "SaldoFechaMovimientoOptimus"
				      ,fec_mov."Cuadratura" as "EstadoCuadraturaFechaMovimiento"
				      ,fec_liq."SaldoSebraliq" as "SaldoSebraLiquidacion"
				      ,fec_liq."Saldo" as "Saldoliquidacion"
				      ,coalesce(fec_liq."Cuadratura",'SIN MOVIMIENTOS A LIQUIDAR EN EL PERIODO') as "EstadoCuadraturaFechaLiquidacion"
				from :datos_sebra as fec_mov
		        left join :datos_sebra_liq as fec_liq
		        	on  fec_liq."COD_PER" = fec_mov."COD_PER"
		        	and fec_liq."IdCuentaInversion" = fec_mov."IdCuentaInversion"
					and fec_liq."IdCliente" = fec_mov."IdCliente"
					and fec_liq."CodigoPortafolio" = fec_mov."CodigoPortafolio"
		        order by fec_mov."COD_PER",fec_mov."IdCliente",fec_mov."IdCuentaInversion";

        --select 
        --       (SELECT COUNT(*) FROM :datos_sebra WHERE "Cuadratura" = 'CUADRADO') AS "CuentasCuadradas"
        ---      ,(SELECT COUNT(*) FROM :datos_sebra WHERE "Cuadratura" = 'DESCUADRATURA') AS "CuentasDesCuadradas"
        --      ,(SELECT COUNT(*) FROM :datos_sebra) AS "Totales"
        ---from dummy; 
       	SIGNAL SALIDA_CONTROLADA;
       END;    	           
	END;
