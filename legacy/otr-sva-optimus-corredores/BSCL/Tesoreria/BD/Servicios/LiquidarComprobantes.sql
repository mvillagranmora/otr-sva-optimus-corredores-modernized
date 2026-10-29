SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes";

CREATE PROCEDURE
-- 
--  Proposito: Liquidar comprobantes de tesoreria
--  Autor: NRM - BCS
--  Fecha Creacion: 01/01/2016
--
"BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes" (
     -- Parametros de Validacion
     IN p_IdEmpresa 					BIGINT,  
     IN p_IdUsuario 					BIGINT,   
     IN p_IdModulo 						NVARCHAR(3),
     IN p_simularOperacion 				NVARCHAR(1)	DEFAULT 'N',	-- S/N se usa en caso de servicios anidados. 
     IN p_UsaReglasNegocio 				NVARCHAR(1),				-- S/N se usa en caso de servicios anidados.   
   
 
     IN p_FechaMovimiento				NVARCHAR(10),   -- Fecha Sistema

     -- Comprobantes a Liquidar - AfectaCuentaInversion -> CLI: Cliente  COR: Corredora  VACIO: Ambos
     IN p_TablaComprobante 			    TABLE("IdComprobante" BIGINT, "IdTipoMovimiento" NVARCHAR(6), "AfectaCuentaInversion" NVARCHAR(3)),

     -- Formas de Pago a Liquidar 
     -- Si no especifica la FP a liquidar para un comprobante, se asume que se deben liquidar todas las FP de ese comprobante
     IN p_TablaComprobanteFormaDePago	TABLE("IdComprobante" BIGINT, "IdComprobanteFormaDePago" BIGINT),  
     
     --Salidas para mensajes de advertencias y errores
	 OUT p_out_InformacionContador  	INT,
     OUT p_out_AdvertenciasContador 	INT,
  	 OUT p_out_ErroresContador      	INT
  	 
	-- TABLAS SALIDA  	 
	-- "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ResultadoProceso"
	-- "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_InformacionDetalle"
	-- "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_AdvertenciasDetalle"
	-- "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ErroresDetalle"

)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN
  	
  	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	
	DECLARE v_AdvertenciasContador INT := 0;
	DECLARE v_ErroresContador	   INT := 0;
	DECLARE v_IdEjecucion BIGINT;
	DECLARE SALIDA_CONTROLADA  CONDITION;	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		IF (NOT IS_EMPTY("BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_TablaCodigosMensajes")) THEN
			
			v_Mensajes = SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_TablaCodigosMensajes";
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_InformacionDetalle"
			SELECT TCM."IdInterno" as "IdComprobante",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'OK';
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_AdvertenciasDetalle"
			SELECT TCM."IdInterno" as "IdComprobante",EM."Codigo",TCM."Resultado" as "RequiereAutorizacion",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" IN ('ADVE','RAUT');
						
			INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ErroresDetalle"
			SELECT TCM."IdInterno" as "IdComprobante",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'RECH';
		END IF;
		
		SELECT count(1) into p_out_InformacionContador FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_InformacionDetalle";
		SELECT count(1) into p_out_AdvertenciasContador FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_AdvertenciasDetalle";
		SELECT count(1) into p_out_ErroresContador FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ErroresDetalle";
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			p_out_ErroresDetalle = SELECT 0 as "IdComprobante",v_errorExt as "Codigo",::SQL_ERROR_MESSAGE as "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;
	END;
	
	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
  
  
  	------------------------------------------------------------------
	-- ASIGNAR TODAS LAS FORMA DE PAGO A LIQUIDAR (para los comprobante en que no se especificaron)
    -- Este proceso debe ir antes de la validación "ServicioCumpleRequisitosLiquidarComprobantes"
	------------------------------------------------------------------
	
	v_TablaComprobante = 
	SELECT  TC."IdComprobante" AS "IdInterno"
		   ,TC."IdComprobante" AS "IdComprobanteInformado"
		   ,TC."IdTipoMovimiento" AS "IdTipoMovimientoInformado"
		   ,C."IdComprobante"
		   ,C."IdTipoMovimiento"
		   ,C."Monto" AS "MontoComprobante"
		   ,COALESCE(TCFP."Count",0) AS "CantidadFormaDePago"
    	   ,COALESCE(TCFP."Monto",0) AS "MontoTotalFormaDePago"
    	   ,C."IdEstado" AS "IdEstadoComprobante"
    	   ,TC."AfectaCuentaInversion"
    	   ,C."IdPortafolioTitularPago"
    	   ,C."IdConcepto"
    	   ,C."FechaMovimiento"
    	   ,C."IdPersona"
		   ,C."NombrePersona"
		   ,C."TitularPago"
		   ,C."IdTipoPersonaTitularPago"
		   ,C."IdPersonaTitularPago"
		   ,p_FechaMovimiento AS "FechaLiquidacion" --CAMBIO SOLICITADO POR JOSSANDON
		   ,C."IdCaja"
	  FROM :p_TablaComprobante AS TC
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS C ON (C."IdComprobante" = TC."IdComprobante")
	  LEFT JOIN (SELECT "IdComprobante", 
	   					  COUNT("IdComprobanteFormaDePago") as "Count",
	   					  SUM("Monto") as "Monto"
	   			    FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
	   			   WHERE "IdEstado" NOT IN ('ANU','MOD')
	   			   GROUP BY "IdComprobante"
	   			 ) AS TCFP
	   		  ON TCFP."IdComprobante" = TC."IdComprobante"
	    FOR UPDATE OF C."IdComprobante";
	
	
	 v_TablaComprobanteDetalle =
	 SELECT  TC."IdComprobante" AS "IdInterno"
		   	,TC."IdComprobante" AS "IdComprobanteInformado"
		   	,CD."IdComprobanteDetalle"
		   	,CD."IdTipoMovimiento"
		   	,SREL."IdTipoComprobanteOrigen"
		   	,SREL."SecuenciaRelacionada"
		   	,COALESCE(SREL."PendienteDeLiquidar",0) AS "PendienteDeLiquidar"
		   	,CD."IdConcepto"
		   	,CD."Monto"
	  FROM :p_TablaComprobante AS TC
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" as CD 
	   		 ON CD."IdComprobante" = TC."IdComprobante"
	  LEFT JOIN (SELECT "IdComprobante","IdTipoComprobanteOrigen","SecuenciaRelacionada",
	   			         SUM(CASE WHEN "IdEstado" = 'LIQ' THEN 0 ELSE 1 END) as "PendienteDeLiquidar"
					 FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" 
				     WHERE "IdTipoMovimiento" = 'MOVING'
				       AND "IdEstado" NOT IN ('ANU','MOD')		     
				     GROUP BY "IdComprobante","IdTipoComprobanteOrigen","SecuenciaRelacionada"
	   			  ) AS SREL
	   		   ON SREL."IdComprobante" = TC."IdComprobante"
	   		  AND SREL."IdTipoComprobanteOrigen" = CD."IdTipoComprobanteOrigen" 
	   		  AND SREL."SecuenciaRelacionada" = CD."SecuenciaRelacionada";

	
	v_TablaComprobanteFormaDePago = 
	SELECT  TC."IdInterno"
		   ,TC."IdComprobanteInformado"
		   ,CFP."IdComprobanteFormaDePagoInformado"
		   ,CFP."IdComprobanteFormaDePago"
		   ,CFP."IdCuentaBancariaCorredora"
		   ,CFP."IdEstadoFormaDePago"
		   ,TC."IdEstadoComprobante"		   
		   ,CFG."TipoMedioDePago"
		   ,CFG."LiquidacionParcialEnviaACTA"
		   ,CFG."EstadosPosibleTipoMovimientoMedioDePago"
		   ,CFG."AfectaSaldoEnEstado"
		   ,CFP."IdMoneda"
		   ,CFP."Monto"
		   ,CFP."IdDeposito"
		   ,CFP."IdEstadoDepositoDetalle"
		   ,ADD_DAYS (TO_DATE (TC."FechaLiquidacion", 'YYYY-MM-DD'), CFP."DiasRetencion") AS "FechaLiquidacion"
		   ,CFP."DiasRetencion"
		   ,CFP."IdMedioDePago"
		   ,TC."IdCaja"
	  FROM :v_TablaComprobante AS TC
	  LEFT JOIN (SELECT  CFP."IdComprobante"
	  					,COALESCE(TCFP."IdComprobanteFormaDePago",CFP."IdComprobanteFormaDePago") AS "IdComprobanteFormaDePagoInformado"
	  					,CFP."IdComprobanteFormaDePago"
	  					,CFP."IdTipoMovimiento"
	  					,CFP."IdMedioDePago"
						,CFP."IdCuentaBancariaCorredora"
						,CFP."IdEstado" AS "IdEstadoFormaDePago"
						,CFP."IdMoneda"
		   				,CFP."Monto"
		   				,DD."IdDeposito"
		   				,DD."IdEstado" AS "IdEstadoDepositoDetalle"
		   				,CFP."DiasRetencion"
	  			   FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
	  			   LEFT JOIN "BSCL.Tesoreria.BD::TES.DepositoDetalle" AS DD ON DD."IdComprobanteFormaDePago" = CFP."IdComprobanteFormaDePago" AND DD."IdEstado" <> 'ANU'
	  			   LEFT JOIN :p_TablaComprobanteFormaDePago AS TCFP ON (TCFP."IdComprobante" = CFP."IdComprobante")	
	  			  WHERE CFP."IdEstado" NOT IN ('ANU','MOD')
	  			) as CFP ON (CFP."IdComprobante" = TC."IdComprobante") AND (CFP."IdComprobanteFormaDePagoInformado" = CFP."IdComprobanteFormaDePago")
	  LEFT JOIN (SELECT  TMMP."IdTipoMovimiento"
	 					,TMMP."IdMedioDePago"
	 					,MP."TipoMedioDePago"
	 					,CFIG."Valor" AS "LiquidacionParcialEnviaACTA" 					
	 					,TMMP."EstadosPosibleTipoMovimientoMedioDePago"
	 					,TMMP."AfectaSaldoEnEstado"	 					
	 			   FROM "BSCL.Plataforma.BD::PAG.MedioDePago" as MP
	 			  INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" as TMMP ON (TMMP."IdMedioDePago" = MP."IdMedioDePago"),
	 			  "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" as CFIG
	 			  WHERE "CodigoParametro" = 'LIQUIDACIONPARCIALENVIAACTA'
	 			) AS CFG ON CFG."IdTipoMovimiento" = CFP."IdTipoMovimiento" AND CFG."IdMedioDePago" = CFP."IdMedioDePago"
	 WHERE CFP."IdEstadoFormaDePago" <> (CASE WHEN CFG."LiquidacionParcialEnviaACTA" = 'S' THEN 'LIQ' ELSE '' END);
  
  	
  	--------------------
	--VALIDACIONES
	--------------------
	IF :p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoValidacion 	NVARCHAR(40);
		
		---Validaciones datos de entrada			
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoValidacion);
		IF :v_resultadoValidacion = 'RECH' THEN
			INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_TablaCodigosMensajes"
			SELECT 0 as "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2",'RECH' as "Resultado" FROM :v_Errores;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
	
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosLiquidarComprobantes"(
			 p_IdEmpresa                    =>  :p_IdEmpresa
			,p_TablaComprobante				=>	:v_TablaComprobante
		 	,p_TablaComprobanteDetalle		=>	:v_TablaComprobanteDetalle
			,p_TablaComprobanteFormaDePago	=>	:v_TablaComprobanteFormaDePago
			,p_out_Resultado				=>	v_Resultado
		);
		INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_TablaCodigosMensajes"
		SELECT DISTINCT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","Resultado"
		  FROM :v_Resultado
		 WHERE "Resultado" <> 'OK';
		
		--Se eliminan los registros rechazados o que requieren autorizacion
		v_TablaComprobante = 
		SELECT  TC."IdInterno"
			   ,"IdComprobanteInformado"
			   ,"IdTipoMovimientoInformado"
			   ,"IdComprobante"
			   ,"IdTipoMovimiento"
			   ,"MontoComprobante"
			   ,"CantidadFormaDePago"
	    	   ,"MontoTotalFormaDePago"
	    	   ,"IdEstadoComprobante"
	    	   ,"AfectaCuentaInversion"
	    	   ,"IdPortafolioTitularPago"
	    	   ,"IdConcepto"
	    	   ,"FechaMovimiento"
	    	   ,"IdPersona"
			   ,"NombrePersona"
			   ,"TitularPago"
			   ,"IdTipoPersonaTitularPago"
			   ,"IdPersonaTitularPago"
			   ,"FechaLiquidacion"
			   ,"IdCaja"
		  FROM :v_TablaComprobante AS TC
		 INNER JOIN (SELECT DISTINCT "IdInterno" FROM :v_Resultado WHERE "Resultado" = 'OK'
					  EXCEPT 
					 SELECT DISTINCT "IdInterno" FROM :v_Resultado WHERE "Resultado" IN ('RECH','RAUT') ) as R
				 ON R."IdInterno" = TC."IdInterno";
		
		IF IS_EMPTY(:v_TablaComprobante) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
	END;
	END IF;
  
    --------------------
	--SIMULACION
	--------------------
	IF (:p_simularOperacion = 'S') THEN		
		SIGNAL SALIDA_CONTROLADA;
	END IF;
	
	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE v_ComprobantesRelacionadosCTA	TABLE("IdTipoMovimiento" NVARCHAR(6));
		DECLARE v_TabCuentaInversion 			TABLE("IdCuentaInversion" BIGINT);
		
		DECLARE v_LiquidacionParcialEnviaACTA	NVARCHAR(1);
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_TablaCodigosMensajes"
			SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" FROM DUMMY;			
			SIGNAL SALIDA_CONTROLADA;
		END;
		
		 		    		    		    
		--********************************
		--* Información base             *
		--********************************
		-- Obtiene configuración de tesoreria
		SELECT"LIQUIDACIONPARCIALENVIAACTA"
		INTO v_LiquidacionParcialEnviaACTA
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreriaPivot";


		-- Obtiene los datos de la corredora
		v_InfoCorredora =		
		 SELECT  CC."IdPortafolioDefecto" as "IdPortafolioCorredora"
		   FROM "BSCL.Plataforma.BD::PLA.Empresa" AS E
		  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS CSN ON CSN."IdSocioDeNegocio" = E."IdSocioDeNegocio"
		  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON CC."IdCliente" = CSN."IdCliente"		  
	 	  WHERE E."IdEmpresa" = :p_IdEmpresa;
           
	 	  
		--********************************
		--* Proceso de liquidación       *
		--********************************
		-- Liquidar las Formas de Pago
		UPDATE CFP
		SET "IdEstado"             	= 'LIQ',
		    "IdUsuarioLiquidacion" 	= :p_IdUsuario,
		    "FechaHoraLiquidacion"	= TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7'), --CURRENT_UTCTIMESTAMP,
		    "FechaAfectaSaldo"		= CASE WHEN instr(TFP."AfectaSaldoEnEstado",'LIQ') > 0 
		    								AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0 
		    								AND COALESCE(CFP."FechaAfectaSaldo",'0001-01-01') = '0001-01-01' 
		    							   THEN ADD_DAYS(TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD'), CFP."DiasRetencion") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
		    							   ELSE CFP."FechaAfectaSaldo"
		    						   END
		FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
		INNER JOIN :v_TablaComprobante AS CAL 
				ON CFP."IdComprobante" = CAL."IdComprobanteInformado"	
		INNER JOIN :v_TablaComprobanteFormaDePago AS TFP 
				ON TFP."IdComprobanteInformado" = CFP."IdComprobante" 
			   AND TFP."IdComprobanteFormaDePagoInformado" = CFP."IdComprobanteFormaDePago"
	    WHERE CFP."IdEstado" NOT IN ('ANU','LIQ');
		
	    
	    
		-- Obtiene estado de liquidación de las formas de pago(para liquidar comprobante)
		v_EstadoLiquidacionComprobante =
		SELECT
			  COM."IdComprobante"
			 ,(COUNT(CFP."IdComprobanteFormaDePago") - COUNT(CASE WHEN CFP."IdEstado" = 'LIQ' THEN 1 END)) AS "LiquidacionParcial"
		FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS COM
		INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobante" = COM."IdComprobante")
		INNER JOIN :v_TablaComprobante AS CAL ON (CAL."IdComprobanteInformado" = COM."IdComprobante")
		AND CFP."IdEstado" NOT IN ('ANU','MOD')
		GROUP BY COM."IdComprobante"
		HAVING COUNT(CASE WHEN CFP."IdEstado" = 'LIQ' THEN 1 END) > 0; -- por lo menos una forma de pago debe estar liquidada para cambiar estado del comprobante.
		
		
		-- Liquidar Comprobantes
		UPDATE COM
		SET "IdEstado" = (CASE  WHEN (ELC."LiquidacionParcial" = 0) THEN 'LIQ' ELSE 'LIP' END),
		"FechaLiquidacion" = TO_DATE(:p_FechaMovimiento,'YYYY-MM-DD')
		FROM "BSCL.Tesoreria.BD::TES.Comprobante" COM
		INNER JOIN :v_EstadoLiquidacionComprobante AS ELC ON (ELC."IdComprobante" = COM."IdComprobante");
		
		
		-- Liquidar Comprobante detalle
		UPDATE COM
		SET "IdEstado" = 'LIQ'
		FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" COM
		INNER JOIN :v_EstadoLiquidacionComprobante AS ELC ON (ELC."IdComprobante" = COM."IdComprobante")
		WHERE ELC."LiquidacionParcial" = 0;
			
		
		-- Actualizar Saldos
		v_CuentaBanco =
		SELECT DISTINCT "IdCuentaBancariaCorredora" as "IdCuentaBanco"
		  FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
		 INNER JOIN :v_TablaComprobante AS CAL ON (CFP."IdComprobante" = CAL."IdComprobanteInformado")
		 WHERE COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0;

		CALL "BSCL.Tesoreria.BD.Servicios.BAN::ActualizarSaldosCuentaBanco" (
			p_FechaMovimiento 	=> :p_FechaMovimiento,
			p_CuentaBanco		=> :v_CuentaBanco
		);

			
		--**************************************************************************************************************************
		-- Envío a Cuenta Inversión
		--**************************************************************************************************************************
		v_ComprobantesRelacionadosCTA = SELECT 'MOVABN' as "IdTipoMovimiento" FROM DUMMY UNION ALL SELECT 'MOVCAR' as "IdTipoMovimiento" FROM DUMMY;


------------BORRAR UNA VEZ ELIMINADA LA INTERMITENCIA (INI)
	DELETE FROM "BSCL.Tesoreria.BD::TEM.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion" 
	 WHERE "FechaMovimiento" < ADD_DAYS (TO_DATE (:p_FechaMovimiento, 'YYYY-MM-DD'), -90);

	INSERT INTO "BSCL.Tesoreria.BD::TEM.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion"
	SELECT DISTINCT 
 	 	 		 TC."IdComprobanteInformado"
 	 	 		,TFP."IdComprobanteFormaDePagoInformado"
 	 	 		
				,TCD."IdComprobanteDetalle"
				,TFP."IdComprobanteFormaDePagoInformado" AS "IdComprobanteFormaDePago"
				   
				,TC."FechaMovimiento"
				,CASE WHEN TC."IdConcepto" = 'MULCPT' THEN TCD."IdConcepto"							  
					  ELSE TC."IdConcepto"
				  END AS "IdConcepto"
				,TC."IdPersona"
			    ,TC."NombrePersona"
			    ,TC."TitularPago"
			    ,TC."IdTipoPersonaTitularPago"
			    ,TC."IdPersonaTitularPago"
			    
			    ,com."IdMoneda"			    
				,TCD."Monto"
				  
				  
			    ,CP."IdPortafolio"
				,(CASE WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVING' THEN 'MOVABN'
					   WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVCAR'
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVING' THEN 'MOVABN' 
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVCAR'
				   END) AS "IdTipoMovimientoCtaInvCliente"     
				,(CASE WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVING' THEN 'MOVCAR'
					   WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVABN'
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVING' THEN 'MOVCAR' 
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVABN'
				   END) AS "IdTipoMovimientoCtaInvCorredora"			 
				,(CASE WHEN TC."IdConcepto" = 'MULCPT' THEN SUBSTRING(TCD."IdTipoMovimiento", 4, 3)
					   ELSE SUBSTRING(TC."IdTipoMovimiento", 4, 3)
				   END) AS "IdTipoComprobante" -- ING / EGR
				,'' as "IdTipoMovimientoAsociado"
			    ,0 as "IdMovimientoAsociado"
			    ,COM."IdEstado"
			    ,:v_LiquidacionParcialEnviaACTA AS "LiquidacionParcialEnviaACTA"
			    ,TCD."SecuenciaRelacionada"			    
				,TC."AfectaCuentaInversion"
	 	   FROM :v_TablaComprobante AS TC
		  LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as com ON (COM."IdComprobante" = TC."IdComprobanteInformado")
		  LEFT JOIN :v_TablaComprobanteDetalle AS TCD ON (TCD."IdComprobanteInformado" = TC."IdComprobanteInformado")
	
		  LEFT JOIN 
			(	-- Toma solo la primera forma de pago, sino se duplican los registros entre el detalle y las formas de pago
			 	SELECT "IdComprobanteInformado", MAX("IdComprobanteFormaDePagoInformado") AS "IdComprobanteFormaDePagoInformado"
			 	FROM :v_TablaComprobanteFormaDePago
			 	GROUP BY "IdComprobanteInformado"
			) AS TFP  ON (TFP."IdComprobanteInformado" = TC."IdComprobanteInformado")
			
			
	 	  LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON (CP."IdPortafolio" = TC."IdPortafolioTitularPago");	 	 	 	  
------------BORRAR UNA VEZ ELIMINADA LA INTERMITENCIA (FIN)


		
		INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion"
 	 	 SELECT DISTINCT 
 	 	 		 TC."IdComprobanteInformado" AS "IdComprobante"
 	 	 		 
				,TCD."IdComprobanteDetalle"
				,TFP."IdComprobanteFormaDePagoInformado" AS "IdComprobanteFormaDePago"
				
				,TC."AfectaCuentaInversion"
				,TC."FechaMovimiento"
				,CASE WHEN TC."IdConcepto" = 'MULCPT' THEN TCD."IdConcepto"							  
					  ELSE TC."IdConcepto"
				  END AS "IdConcepto"
				,TC."IdPersona"
			    ,TC."NombrePersona"
			    ,TC."TitularPago"
			    ,TC."IdTipoPersonaTitularPago"
			    ,TC."IdPersonaTitularPago"
			    

			    ,com."IdMoneda"			    
				,TCD."Monto"
				
				
			    ,CP."IdPortafolio"
				
				-- OPERACION  |  NEGOCIO | TESORERIA
				---------------------------------------	
				--  Venta     |  Abono   | (EGR) Cargo
				--  Compra    |  Cargo   | (ING) Abono
 	 
				,(CASE WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVING' THEN 'MOVABN'
					   WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVCAR'
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVING' THEN 'MOVABN' 
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVCAR'
				   END) AS "IdTipoMovimientoCtaInvCliente"     
				,(CASE WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVING' THEN 'MOVCAR'
					   WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVABN'
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVING' THEN 'MOVCAR' 
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVABN'
				   END) AS "IdTipoMovimientoCtaInvCorredora"			 
				,(CASE WHEN TC."IdConcepto" = 'MULCPT' THEN SUBSTRING(TCD."IdTipoMovimiento", 4, 3)
					   ELSE SUBSTRING(TC."IdTipoMovimiento", 4, 3)
				   END) AS "IdTipoComprobante" -- ING / EGR
				,'' as "IdTipoMovimientoAsociado"
			    ,0 as "IdMovimientoAsociado"
	 	   FROM :v_TablaComprobante AS TC
		  INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as com ON (COM."IdComprobante" = TC."IdComprobanteInformado")
		  INNER JOIN :v_TablaComprobanteDetalle AS TCD ON (TCD."IdComprobanteInformado" = TC."IdComprobanteInformado")

		  
	
		  	LEFT JOIN 
			(	-- Toma solo la primera forma de pago, sino se duplican los registros entre el detalle y las formas de pago
			 	SELECT "IdComprobanteInformado", MAX("IdComprobanteFormaDePagoInformado") AS "IdComprobanteFormaDePagoInformado"
			 	FROM :v_TablaComprobanteFormaDePago
			 	GROUP BY "IdComprobanteInformado"
			) AS TFP  ON (TFP."IdComprobanteInformado" = TC."IdComprobanteInformado")
		  
		  
	 	   LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON (CP."IdPortafolio" = TC."IdPortafolioTitularPago")
	 	 WHERE COM."IdEstado" = CASE WHEN :v_LiquidacionParcialEnviaACTA = 'N' THEN 'LIQ' 
				 					 WHEN :v_LiquidacionParcialEnviaACTA = 'S' AND TC."IdConcepto" = 'MULCPT' THEN 'LIQ'
				 					 ELSE COM."IdEstado" 
				 				 END
		   AND NOT (TCD."IdTipoMovimiento" = 'MOVEGR' AND COALESCE(TCD."SecuenciaRelacionada",0) > 0);  -- Se excluye los egresos relacionados con un ingreso (SPOT)

	
		
		INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion"
		SELECT 
 	 	 		 TC."IdComprobanteInformado" AS "IdComprobante"
 	 	 		 
				,TCD."IdComprobanteDetalle"
				,TFP."IdComprobanteFormaDePagoInformado" AS "IdComprobanteFormaDePago"
				
				,TC."AfectaCuentaInversion"
				,TC."FechaMovimiento"
				,CASE WHEN TC."IdConcepto" = 'MULCPT' THEN TCD."IdConcepto" ELSE TC."IdConcepto" END AS "IdConcepto"
				,TC."IdPersona"
			    ,TC."NombrePersona"
			    ,TC."TitularPago"
			    ,TC."IdTipoPersonaTitularPago"
			    ,TC."IdPersonaTitularPago"
			    
			    ,com."IdMoneda"			    
				,TCD."Monto"
				  
			    ,CP."IdPortafolio"
				,CRCTA."IdTipoMovimiento" AS "IdTipoMovimientoCtaInvCliente"
				,CASE WHEN CRCTA."IdTipoMovimiento" = 'MOVABN' THEN 'MOVCAR'
					  WHEN CRCTA."IdTipoMovimiento" = 'MOVCAR' THEN 'MOVABN'
				  END AS "IdTipoMovimientoCtaInvCorredora"				  
				,(CASE WHEN TC."IdConcepto" = 'MULCPT' THEN SUBSTRING(TCD."IdTipoMovimiento", 4, 3)
					   ELSE SUBSTRING(TC."IdTipoMovimiento", 4, 3)
				   END) AS "IdTipoComprobante"
				,(CASE WHEN TC."IdConcepto" = 'MULCPT' THEN TCD."IdTipoMovimiento" ELSE TC."IdTipoMovimiento" END) AS "IdTipoMovimientoAsociado"
			    ,TC."IdComprobanteInformado" as "IdMovimientoAsociado"
	 	   FROM :v_TablaComprobante AS TC
		  INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as com ON (COM."IdComprobante" = TC."IdComprobanteInformado")
		  INNER JOIN :v_TablaComprobanteDetalle AS TCD ON (TCD."IdComprobanteInformado" = TC."IdComprobanteInformado")
		  
		  
		 
		  	LEFT JOIN 
			(	-- Toma solo la primera forma de pago, sino se duplican los registros entre el detalle y las formas de pago
			 	SELECT "IdComprobanteInformado", MAX("IdComprobanteFormaDePagoInformado") AS "IdComprobanteFormaDePagoInformado"
			 	FROM :v_TablaComprobanteFormaDePago
			 	GROUP BY "IdComprobanteInformado"
			) AS TFP  ON (TFP."IdComprobanteInformado" = TC."IdComprobanteInformado")
			
			
			
	 	   LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON (CP."IdPortafolio" = TC."IdPortafolioTitularPago"),
	 	   :v_ComprobantesRelacionadosCTA AS CRCTA
	 	 WHERE COM."IdEstado" = CASE WHEN :v_LiquidacionParcialEnviaACTA = 'N' THEN 'LIQ' 
				 					 WHEN :v_LiquidacionParcialEnviaACTA = 'S' AND TC."IdConcepto" = 'MULCPT' THEN 'LIQ'
				 					 ELSE COM."IdEstado" 
				 				 END
		   AND (TCD."IdTipoMovimiento" = 'MOVEGR' AND COALESCE(TCD."SecuenciaRelacionada",0) > 0);
		
				 	 						   
		-- Envío a CTA
	 	v_AfectaCuentaInversionCorredora = 
	 	 SELECT	 COR."IdPortafolioCorredora" as "IdPortafolio"
	 	 		,COM."IdMoneda"
	 	 		,COM."IdTipoMovimientoCtaInvCorredora" as "IdTipoMovimiento" 
	 	 		,CASE WHEN deta."IdConcepto" = 'INGBON' THEN 'ABOBON' /*APV - Se modifica concepto para la Cta Inv*/
	 	 		 ELSE COM."IdConcepto" 
	 	 		 END AS "IdConcepto"
	 	 		,IFNULL(dicc."Descripcion",'LIQUIDACION_COMPROBANTE_TESORERIA') as "Descripcion"
	 	 		,'T0' as "IdCondicionDeLiquidacion"
	 	 		,:p_FechaMovimiento as "FechaCondicionDeLiquidacion"
	 	 		,COM."Monto"
	 	 		,0 as "IdEjecutivo"
	 			,0 as "IdSucursal"
	 	 		,deta."IdTipoProducto"
	 	 		,CASE 
	 	 			WHEN COALESCE(deta."IdComprobanteOrigen",0) <> 0 THEN deta."IdTipoOperacion"
	 	 			ELSE ''
	 	 		END AS "IdTipoOperacion"
			    ,deta."IdComprobanteOrigen" AS "IdOperacion"
			    ,orig."FechaMovimiento" as "FechaOperacion"
			    ,orig."FechaLiquidacion" as "FechaLiquidacionOperacion"
			    ,orig."FechaLiquidacion" as "FechaVencimientoOperacion"
			    ,deta."IdMonedaOperacion"
			    ,orig."Monto" as "MontoOperacion"
			    ,null as "TipoCambioOperacion"
			    ,null as "IdContraparte"
			    ,COM."IdTipoComprobante" as "IdTipoComprobante"
			    ,COM."IdComprobante" as "IdComprobante"
			    ,null as "FolioComprobante"	
			    ,COM."IdComprobanteDetalle" as "IdComprobanteDetalle"
			    ,COM."IdComprobanteFormaDePago" as "IdComprobanteFormaDePago"
			    ,COM."IdTipoMovimientoAsociado"
			    ,COM."IdMovimientoAsociado"
			    ,COM."FechaMovimiento" as "FechaComprobante"
			    ,null as "IdPersonaPagoCobro"
			    ,null as "IdInstrumento"
			    ,null as "IdTipoInstrumento"
			    ,null as "Nemo"
			    ,null as "EsIndicadorBilateral"
			    ,'N' as "EsCargoAbonoPorTraspaso"
			    ,null as "IdPortafolioTraspaso"
			    ,'N' as "GeneroContraparteEnTesoreria"
			    ,'S' as "CertificadoAutomatico"
		   FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion" as COM
 			LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" deta ON deta."IdComprobante" = COM."IdComprobante" AND deta."NroLinea" = 1
		   	LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" orig ON orig."IdComprobante" = COM."IdComprobante"
		   	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" conf on conf."IdEmpresa" = p_IdEmpresa and conf."CodigoParametro" = 'PLA.IdiomaSistema'
		   	LEFT JOIN "BSCL.Plataforma.BD::PLA.Diccionario" dicc on dicc."CodLenguaje" = conf."Valor" and dicc."CodDiccionario" = 'LIQUIDACION_COMPROBANTE_TESORERIA'
		   ,:v_InfoCorredora as COR
		  WHERE (COM."AfectaCuentaInversion" = 'COR' OR COM."AfectaCuentaInversion" = '')
		    AND COALESCE(COR."IdPortafolioCorredora",0) > 0;
		    
	 	v_AfectaCuentaInversionCliente = 
	 	 SELECT	 COM."IdPortafolio"
	 	 		,COM."IdMoneda"
	 	 		,COM."IdTipoMovimientoCtaInvCliente" as "IdTipoMovimiento"
	 	 		,CASE WHEN deta."IdConcepto" = 'INGBON' THEN 'ABOBON' /*APV - Se modifica concepto para la Cta Inv*/
	 	 		 ELSE COM."IdConcepto" 
	 	 		 END AS "IdConcepto"
	 	 		,IFNULL(dicc."Descripcion",'LIQUIDACION_COMPROBANTE_TESORERIA') AS "Descripcion"
	 	 		,'T0' as "IdCondicionDeLiquidacion"
	 	 		,:p_FechaMovimiento as "FechaCondicionDeLiquidacion"
	 	 		,COM."Monto"
	 	 		,0 as "IdEjecutivo"
	 			,0 as "IdSucursal"
	 	 		,deta."IdTipoProducto"
	 	 		,CASE 
	 	 			WHEN COALESCE(deta."IdComprobanteOrigen",0) <> 0 THEN deta."IdTipoOperacion"
	 	 			ELSE ''
	 	 		END AS "IdTipoOperacion"
			    ,deta."IdComprobanteOrigen" AS "IdOperacion"
			    ,orig."FechaMovimiento" as "FechaOperacion"
			    ,orig."FechaLiquidacion" as "FechaLiquidacionOperacion"
			    ,orig."FechaLiquidacion" as "FechaVencimientoOperacion"
			    ,deta."IdMonedaOperacion"
			    ,orig."Monto" as "MontoOperacion"
			    ,null as "TipoCambioOperacion"
			    ,null as "IdContraparte"			    
			    ,COM."IdTipoComprobante" as "IdTipoComprobante"
			    ,COM."IdComprobante" as "IdComprobante"
			    ,null as "FolioComprobante"	
			    ,COM."IdComprobanteDetalle" as "IdComprobanteDetalle"
			    ,COM."IdComprobanteFormaDePago" as "IdComprobanteFormaDePago"
			    ,COM."IdTipoMovimientoAsociado"
			    ,COM."IdMovimientoAsociado"
			    ,COM."FechaMovimiento" as "FechaComprobante"			    
			    ,COM."IdPersonaTitularPago" as "IdPersonaPagoCobro"
			    ,null as "IdInstrumento"
			    ,null as "IdTipoInstrumento"
			    ,null as "Nemo"
			    ,null as "EsIndicadorBilateral"
			    ,'N' as "EsCargoAbonoPorTraspaso"
			    ,null as "IdPortafolioTraspaso"
			    ,'N' as "GeneroContraparteEnTesoreria"
			    ,'S' as "CertificadoAutomatico"
		   FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion" as COM
		   LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" deta ON deta."IdComprobante" = COM."IdComprobante" AND deta."NroLinea" = 1
		   LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" orig ON orig."IdComprobante" = COM."IdComprobante"
		   LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" conf on conf."IdEmpresa" = p_IdEmpresa and conf."CodigoParametro" = 'PLA.IdiomaSistema'
		   LEFT JOIN "BSCL.Plataforma.BD::PLA.Diccionario" dicc on dicc."CodLenguaje" = conf."Valor" and dicc."CodDiccionario" = 'LIQUIDACION_COMPROBANTE_TESORERIA'

		   
		  WHERE (COM."AfectaCuentaInversion" = 'CLI' OR COM."AfectaCuentaInversion" = '')
		    AND COM."IdTipoPersonaTitularPago" = 'CLI'
		    AND COALESCE(COM."IdPortafolio",0) > 0;
	 	
		    
		-- Afectar la cuenta inversión de los clientes que forman una parte de una partida de Ingresos(PARING) / Egresos(PAREGR)
	 	v_AfectaCuentaInversionClientePorPartidaIE = 
	 	 SELECT	 
	 	 		IE."IdPortafolioTitularPago" AS "IdPortafolio"
	 	 		,IE."IdMoneda"
	 	 		,COM."IdTipoMovimientoCtaInvCliente" as "IdTipoMovimiento"
	 	 		,CASE WHEN deta."IdConcepto" = 'INGAPV' THEN 'ABOAPV' /*APV - Se modifica concepto para la Cta Inv*/
	 	 			  WHEN deta."IdConcepto" = 'ITRAPV' THEN 'ABOAPV' /*APV - Se modifica concepto para la Cta Inv*/
	 	 		      WHEN deta."IdConcepto" = 'EGRAPV' THEN 'CARAPV' /*APV - Se modifica concepto para la Cta Inv*/
	 	 		      WHEN deta."IdConcepto" = 'ETRAPV' THEN 'CARAPV' /*APV - Se modifica concepto para la Cta Inv*/
	 	 		      WHEN deta."IdConcepto" = 'INGBON' THEN 'ABOBON' /*APV - Se modifica concepto para la Cta Inv*/ 
	 	 		      ELSE IE."IdConcepto"
	 	 		 END AS "IdConcepto"
	 	 		,CASE
	 	 			WHEN orig."TipoComprobante" = 'PARING' THEN 'LIQUIDACION_PARTIDA_DE_INGRESO_TESORERIA'
	 	 			WHEN orig."TipoComprobante" = 'PAREGR' THEN 'LIQUIDACION_PARTIDA_DE_EGRESO_TESORERIA'
	 	 			ELSE ''
	 	 		END	 as "Descripcion"
	 	 		,'T0' as "IdCondicionDeLiquidacion"
	 	 		,:p_FechaMovimiento as "FechaCondicionDeLiquidacion"
	 	 		,IE."Monto"
	 	 		,0 as "IdEjecutivo"
	 			,0 as "IdSucursal"
	 	 		,IE."IdTipoProducto"
	 	 		,IE."IdTipoOperacion"
			    ,IE."IdOperacion" AS "IdOperacion"			    
			    ,IE."FechaOperacion" as "FechaOperacion"
			    ,orig."FechaLiquidacion" as "FechaLiquidacionOperacion"
			    ,orig."FechaLiquidacion" as "FechaVencimientoOperacion"			    
			    ,IE."IdMonedaOperacion"
			    ,IE."MontoOperacion"
			    ,null as "TipoCambioOperacion"
			    ,null as "IdContraparte"			    
			    ,COM."IdTipoComprobante" as "IdTipoComprobante"
			    ,COM."IdComprobante" as "IdComprobante"
			    ,null as "FolioComprobante"	
			    ,COM."IdComprobanteDetalle" as "IdComprobanteDetalle"
			    ,COM."IdComprobanteFormaDePago" as "IdComprobanteFormaDePago"
			    ,IE."IdTipoMovimiento" AS "IdTipoMovimientoAsociado"
			    ,IE."IdIngresoEgreso" AS "IdMovimientoAsociado"
			    ,COM."FechaMovimiento" as "FechaComprobante"			    
			    ,IE."IdPersonaTitularPago" as "IdPersonaPagoCobro"
			    ,null as "IdInstrumento"
			    ,null as "IdTipoInstrumento"
			    ,null as "Nemo"
			    ,null as "EsIndicadorBilateral"
			    ,'N' as "EsCargoAbonoPorTraspaso"
			    ,null as "IdPortafolioTraspaso"
			    ,'N' as "GeneroContraparteEnTesoreria"
			    ,'S' as "CertificadoAutomatico"
		   FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion" AS COM
		   LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS deta ON (deta."IdComprobante" = COM."IdComprobante")
		   LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS orig ON (orig."IdComprobante" = COM."IdComprobante")		   
		   LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO ON (CDO."IdComprobanteDetalle" = deta."IdComprobanteDetalle")		   
		   LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON (IE."IdIngresoEgreso" = CDO."IdIngresoEgreso")
		   WHERE (COM."AfectaCuentaInversion" = 'CLI' OR COM."AfectaCuentaInversion" = '')
		   AND COALESCE(IE."IdPortafolioTitularPago",0) > 0
		   AND orig."TipoComprobante" IN ('PARING','PAREGR');  -- Solo partidas de ingresos/egreso generadas desde movimientos
		   
		   		   
		   --SELECT 'v_AfectaCuentaInversionClientePorPartidaIE', * FROM :v_AfectaCuentaInversionClientePorPartidaIE;  -- Revisar
		   
	 	
	 	INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar"
	 	("NumeroCargoAbono","IdPortafolio","IdMoneda","IdTipoMovimiento","IdConcepto","Descripcion","IdCondicionDeLiquidacion","FechaCondicionDeLiquidacion"
		,"Monto","IdEjecutivo","IdSucursal","IdTipoProducto","IdTipoOperacion","IdOperacion","FechaOperacion"
		,"FechaLiquidacionOperacion","FechaVencimientoOperacion","IdMonedaOperacion","MontoOperacion","TipoCambioOperacion","IdContraparte"			    
		,"IdTipoComprobante","IdComprobante","FolioComprobante"	,"IdComprobanteDetalle","IdComprobanteFormaDePago"
		,"IdTipoMovimientoAsociado","IdMovimientoAsociado","FechaComprobante","IdPersonaPagoCobro"
		,"IdInstrumento","IdTipoInstrumento","Nemo","EsIndicadorBilateral","EsCargoAbonoPorTraspaso"
		,"IdPortafolioTraspaso","GeneroContraparteEnTesoreria","CertificadoAutomatico")
	 	SELECT CAST(ROW_NUMBER() over (order by "IdPortafolio","IdMoneda") as BIGINT) as "NumeroCargoAbono"
	 	 		,*
	 	   FROM ( SELECT "IdPortafolio","IdMoneda","IdTipoMovimiento","IdConcepto","Descripcion","IdCondicionDeLiquidacion","FechaCondicionDeLiquidacion"
					,"Monto","IdEjecutivo","IdSucursal","IdTipoProducto","IdTipoOperacion","IdOperacion","FechaOperacion"
					,"FechaLiquidacionOperacion","FechaVencimientoOperacion","IdMonedaOperacion","MontoOperacion","TipoCambioOperacion","IdContraparte"			    
					,"IdTipoComprobante","IdComprobante","FolioComprobante"	,"IdComprobanteDetalle","IdComprobanteFormaDePago"
					,"IdTipoMovimientoAsociado","IdMovimientoAsociado","FechaComprobante","IdPersonaPagoCobro"
					,"IdInstrumento","IdTipoInstrumento","Nemo","EsIndicadorBilateral","EsCargoAbonoPorTraspaso"
					,"IdPortafolioTraspaso","GeneroContraparteEnTesoreria","CertificadoAutomatico" FROM :v_AfectaCuentaInversionCliente as CLI
	 	   		   UNION ALL 
	 	   		  SELECT "IdPortafolio","IdMoneda","IdTipoMovimiento","IdConcepto","Descripcion","IdCondicionDeLiquidacion","FechaCondicionDeLiquidacion"
					,"Monto","IdEjecutivo","IdSucursal","IdTipoProducto","IdTipoOperacion","IdOperacion","FechaOperacion"
					,"FechaLiquidacionOperacion","FechaVencimientoOperacion","IdMonedaOperacion","MontoOperacion","TipoCambioOperacion","IdContraparte"			    
					,"IdTipoComprobante","IdComprobante","FolioComprobante"	,"IdComprobanteDetalle","IdComprobanteFormaDePago"
					,"IdTipoMovimientoAsociado","IdMovimientoAsociado","FechaComprobante","IdPersonaPagoCobro"
					,"IdInstrumento","IdTipoInstrumento","Nemo","EsIndicadorBilateral","EsCargoAbonoPorTraspaso"
					,"IdPortafolioTraspaso","GeneroContraparteEnTesoreria","CertificadoAutomatico" FROM :v_AfectaCuentaInversionCorredora as COR
	 	   		  UNION ALL 
	 	   		  SELECT "IdPortafolio","IdMoneda","IdTipoMovimiento","IdConcepto","Descripcion","IdCondicionDeLiquidacion","FechaCondicionDeLiquidacion"
					,"Monto","IdEjecutivo","IdSucursal","IdTipoProducto","IdTipoOperacion","IdOperacion","FechaOperacion"
					,"FechaLiquidacionOperacion","FechaVencimientoOperacion","IdMonedaOperacion","MontoOperacion","TipoCambioOperacion","IdContraparte"			    
					,"IdTipoComprobante","IdComprobante","FolioComprobante"	,"IdComprobanteDetalle","IdComprobanteFormaDePago"
					,"IdTipoMovimientoAsociado","IdMovimientoAsociado","FechaComprobante","IdPersonaPagoCobro"
					,"IdInstrumento","IdTipoInstrumento","Nemo","EsIndicadorBilateral","EsCargoAbonoPorTraspaso"
					,"IdPortafolioTraspaso","GeneroContraparteEnTesoreria","CertificadoAutomatico" FROM :v_AfectaCuentaInversionClientePorPartidaIE as PAR
	 	   		);
	 	
	 	IF NOT IS_EMPTY("BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar") THEN
		 	CALL "BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono" (
				 p_IdEmpresa				=> p_IdEmpresa
			    ,p_IdUsuario				=> p_IdUsuario
			    ,p_IdModulo					=> p_IdModulo
			    ,p_simularOperacion			=> 'N'
				,p_UsaReglasNegocio			=> :p_UsaReglasNegocio
				,p_FechaSistema				=> p_FechaMovimiento
				,p_EsIngresoManual			=> 'N'
				,p_out_InformacionContador	=> p_out_InformacionContador
				,p_out_AdvertenciasContador	=> p_out_AdvertenciasContador
				,p_out_ErroresContador		=> p_out_ErroresContador
			);			
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_AdvertenciasDetalle"
			SELECT "NumeroCargoAbono" as "IdComprobante","Codigo","RequiereAutorizacion","Descripcion" FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle";
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ErroresDetalle"
			SELECT "NumeroCargoAbono" as "IdComprobante","Codigo","Descripcion" FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle";
			
			IF NOT IS_EMPTY("BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle") THEN			
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END IF;
		
		
		--********************************
		--* Resultado del proceso *
		--*******************************
		INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ResultadoProceso"
		SELECT  'IdComprobante' as "Campo",
				"IdComprobante" as "Id",
				'BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes' as "Servicio",
				(CASE WHEN ("LiquidacionParcial" = 0) THEN 'LIQ' ELSE 'LIP' END) as "Resultado"
		  FROM :v_EstadoLiquidacionComprobante;
		INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ResultadoProceso"
		SELECT 'IdCargoAbonoMovimiento' as "Campo",
			   "IdCargoAbonoMovimiento" as "Id",
			   'BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono' as "Servicio",
			   'CRT' as "Resultado"
		  FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_InformacionDetalle";
		
		
		--SELECT * FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ResultadoProceso"  
		
		INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_TablaCodigosMensajes"
		SELECT  "IdComprobante" AS "IdInterno"
			   ,CASE WHEN ("LiquidacionParcial" = 0) THEN 'UI_COMPROBANTE_LIQUIDADO' 
			   		 ELSE 'UI_COMPROBANTE_LIQUIDADO_PARCIAL'  
			   	 END as "Codigo"
			   ,'IdComprobante' as "CodigoDato1"
			   ,CAST("IdComprobante" as NVARCHAR(520)) as "ValorDato1"
			   ,'' as "CodigoDato2"
			   ,'' as "ValorDato2" 
			   ,'OK' as "Resultado"
		  FROM :v_EstadoLiquidacionComprobante;
		  
		v_CargoAbonoAActualizar = 
		SELECT "IdCuentaInversion","IdCargoAbonoMovimiento" 
		FROM :v_TablaComprobante TAB
		LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" ING ON ING."IdComprobante" = TAB."IdComprobante"
		LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarTablaCargoAbonoMovimiento" CA ON CA."IdComprobante" = ING."IdComprobanteOrigen" AND CA."IdTipoComprobante" = ING."IdTipoComprobanteOrigen" AND CA."IdMoneda" = ING."IdMoneda" AND CA."IdPortafolio" = ING."IdPortafolioTitularPago"
		WHERE 
		TAB."IdComprobante" IS NOT NULL
		AND CA."IdComprobante" IS NOT NULL
		AND ING."IdComprobanteOrigen" != '0'
		AND ING."IdTipoComprobanteOrigen" != '0';
		  		
		v_TabCuentaInversion = SELECT DISTINCT "IdCuentaInversion" FROM :v_CargoAbonoAActualizar;			
			
		CALL "BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoCargoAbono" (
			p_IdEmpresa			=> :p_IdEmpresa,
		    p_IdUsuario			=> :p_IdUsuario,
			p_FechaMovimiento	=> :p_FechaMovimiento,
			p_CuentaInversion	=> :v_TabCuentaInversion
		);
		 
		BEGIN
			DECLARE v_IdPersonaRemitente     bigint;
			DECLARE v_IdDireccionPersonaRemitente bigint;
			
			--INFORMACION DE REMITENTE
            SELECT CE."IdPersona", CE."IdDireccionElectronica" INTO v_IdPersonaRemitente, v_IdDireccionPersonaRemitente
            FROM "BSCL.Plataforma.BD::ACC.Usuario" U
            INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  CE ON CE."IdEmpleado" = U."IdEntidad"  
            WHERE "IdUsuario" = :p_IdUsuario; 
            
			--NOTIFICAR
			v_cliente = 
            SELECT
                tc."IdInterno" AS "IdSecuencia",
                c."IdPersona",
                tc."NombrePersona",
                c."IdTipoDocumentoIdentidad",
                c."DocumentoIdentidad",
                "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(c."Monto",CASE WHEN c."IdMoneda"='CLP' THEN 'i' ELSE 'd' END, CASE WHEN c."IdMoneda"='CLP' THEN 0 ELSE 4 END ) AS "Monto",
                c."IdMoneda",
                "BSCL.Plataforma.BD.Utilitarios::DarFormatoFecha"(c."FechaMovimiento", 'd') AS "FechaMovimiento",
                E."Nombre" AS "IdEstado",
                CCP."CodigoPortafolio",
                CPT."Numero" AS "Telefono",
                CPGDEG."Direccion" AS "Correo",
                CE."NombreCompleto" AS "Agente",
                con."Nombre" AS"Concepto",
                CASE WHEN c."IdTipoMovimiento" = 'MOVEGR' THEN 'EGRESO' ELSE 'INGRESO' END AS "TIPOMOVIMIENTO",
                CASE WHEN c."IdTipoMovimiento" = 'MOVEGR' THEN 'EGRESO' ELSE 'INGRESO' END AS "Operacion",
                "BSCL.Plataforma.BD.Utilitarios::DarFormatoFecha"(c."FechaLiquidacion", 'd') AS "FechaLiquidacion",
                CCDG."RazonSocial" AS "Corredora",
                CCDG."Telefono" AS "AtencionCliente",
                CCL."IdDireccionElectronicaPrincipal"
            FROM :v_TablaComprobante tc
            INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS c ON c."IdComprobante" = TC."IdComprobante"
            INNER JOIN "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" CT ON CT."IdConcepto" = c."IdConcepto"
            INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" CCP ON c."IdPortafolioTitularPago" = CCP."IdPortafolio"
            INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CCL ON CCP."IdCliente" = CCL."IdCliente"
            INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" con ON c."IdConcepto" = con."IdConcepto"
            LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" E ON c."IdEstado" = E."IdEstado"
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdPersona" = CCL."IdPersona"
            -- LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronicaGlobal" CPGDEG ON CPGDEG."IdGrupo" = CCL."IdDireccionElectronicaPrincipal"   --comentado ya que duplica los correos
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPGDEG ON CPGDEG."IdDireccionElectronica" = CCL."IdDireccionElectronicaPrincipal"  
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = CCP."IdEmpleadoEjecutivo"
            LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :p_IdEmpresa;

            
            p_Eventos = 
                SELECT 
                'MOV_TESORERIA' AS "CodigoTipoEvento", 
                "IdSecuencia", 
                CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema", 
                :v_IdPersonaRemitente AS "IdPersonaRemitente", 
                "IdPersona" AS "IdPersonaDestinatario",
                0 AS "IdPortafolio"
                FROM :v_cliente;
                
            p_IdDirecciones = 
            SELECT 
                E."IdSecuencia",
                C."IdDireccionElectronicaPrincipal" AS "IdDireccion",
                'TO' AS "TipoDestinatario"
            FROM :p_Eventos E
            INNER JOIN :v_cliente C ON E."IdSecuencia" = C."IdSecuencia"
            UNION ALL
            SELECT 
                E."IdSecuencia",
                v_IdDireccionPersonaRemitente AS "IdDireccion",
                'CC' AS "TipoDestinatario"
            FROM :p_Eventos E;    
            
            p_ArchivosAdjuntos = SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
        
            p_EventoDetalle = 
            SELECT "IdSecuencia", "Llave", "Valor"
            FROM (
                SELECT * ,
                        
                COALESCE(
                "1" ||"NombrePersona", "2" ||"IdTipoDocumentoIdentidad", "3" ||"DocumentoIdentidad", "4" ||"Monto"
                , "5" ||"IdMoneda", "6" ||"FechaMovimiento", "7" ||"IdEstado", "8" ||"CodigoPortafolio"
                , "9" ||"Telefono", "10" ||"Correo", "11" ||"Agente", "12" ||"Concepto"
                , "13" ||"TIPOMOVIMIENTO", "14" ||"FechaLiquidacion", "15" || "Corredora", "16" ||"AtencionCliente"
                , "17" || "IdSecuencia",  "18" || "Operacion"
                ) AS "Valor",
                        
                COALESCE(
                "1" ||'CLIENTE', "2" ||'TIPODOCUMENTOIDENTIDAD', "3" ||'DOCUMENTOIDENTIDAD', "4" ||'MONTO'
                , "5" ||'MONEDA', "6" ||'FECHA', "7" ||'ESTADO', "8" ||'PORTAFOLIO'
                , "9" ||'TELEFONO', "10" ||'CORREO', "11" ||'AGENTE', "12" ||'CONCEPTO'
                , "13" ||'TIPOMOVIMIENTO', "14" ||'FECHALIQUIDACION', "15" || 'CORREDORA', "16" || 'ATENCIONCLIENTE'
                , "17" || 'IDINTERNO', "18" || 'OPERACION'
                ) AS "Llave" 
                FROM :v_cliente C, "BSCL.Plataforma.BD::PLA.Identidad"
            )
            WHERE "Valor" IS NOT NULL;
                
            --Guardar eventos
            CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
                p_IdEmpresa => p_IdEmpresa,
                p_IdUsuario => p_IdUsuario,
                p_IdModulo => p_IdModulo,
                p_Eventos => :p_Eventos,
                p_Valores => :p_EventoDetalle,
                p_ArchivosAdjuntos => :p_ArchivosAdjuntos,
                p_IdDirecciones => :p_IdDirecciones,
                p_out_IdEvento => p_out_IdEvento
            );
    
		END ;
		SIGNAL SALIDA_CONTROLADA;
  	END;  
END;

  
