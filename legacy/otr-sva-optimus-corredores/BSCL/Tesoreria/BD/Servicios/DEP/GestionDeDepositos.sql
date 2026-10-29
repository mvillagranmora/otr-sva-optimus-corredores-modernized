SET SCHEMA "BSCL";  
GO

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.DEP::GestionDeDepositos";
GO


CREATE PROCEDURE 
-- 
--  Proposito: Permite Enviar a depositar, liquidar o anular una nomina de deposito
--  Autor: NRM - BCS
--  Fecha Creacion: 03/10/2016
--   
"BSCL.Tesoreria.BD.Servicios.DEP::GestionDeDepositos" (
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_IdUsuario 						BIGINT, 
    IN p_IdModulo 						NVARCHAR(3),
    IN p_simularOperacion 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados. 
    IN p_UsaReglasNegocio 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.   
   
    -- Parametros de Entrada
	IN p_FechaMovimiento				NVARCHAR(10),    
    IN p_TipoEjecucion					NVARCHAR(3),	--	EDP: Enviado a deposito
														--	LIQ: Nomina liquidada
														--	ANU: Nomina Anulada	
	IN p_Registros 						TABLE ("Id" 			BIGINT, 
											   "Entidad" 		NVARCHAR(300),
											   "IdConcepto"		NVARCHAR(6),
											   "Descripcion"	NVARCHAR(300)),
             	
    --Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  		INT,
 	OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,   
    OUT p_out_AdvertenciasContador 		INT,	  
    OUT p_out_AdvertenciasDetalle  		TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
  	OUT p_out_ErroresContador      		INT,
    OUT p_out_ErroresDetalle       		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_EXITOSA					CONDITION;
	DECLARE SALIDA_CONTROLADA 				CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 		CONDITION;
	DECLARE v_InformacionDetalle   			TABLE("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
											DEFAULT SELECT '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	DECLARE v_AdvertenciasDetalle  			TABLE("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000))
											DEFAULT SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	DECLARE v_ErroresDetalle 				TABLE("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
											DEFAULT SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	DECLARE v_TablaCodigosMensajes			TABLE("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520))
											DEFAULT SELECT 0  as "IdInterno",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY WHERE 1=2;
				        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_TablaCodigosMensajes= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);		
		p_out_ErroresDetalle =  SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_EXITOSA     
	BEGIN
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);
		p_out_InformacionDetalle = SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT("Codigo") INTO p_out_InformacionContador FROM :p_out_InformacionDetalle;		
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;
	END;
	
	p_out_InformacionContador		:= 0;
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;

	v_Registros = 
	 SELECT DENSE_RANK() over (ORDER BY "Id", "Entidad") as "IdInterno"
	 		,:p_TipoEjecucion as "TipoEjecucion"
	 		,RA."Id"
	 		,RA."Entidad"
	 		,RA."IdConcepto"
			,RA."Descripcion"
	 		,D."IdDeposito"
	 		,D."IdEstado" AS "IdEstadoDeposito"
	 		,DD."IdDepositoDetalle"	 		
	 		,DD."IdEstado" AS "IdEstadoDepositoDetalle"
	   FROM :p_Registros as RA
	   LEFT JOIN "BSCL.Tesoreria.BD::TES.Deposito" as D ON D."IdDeposito" = CASE WHEN (RA."Entidad" = 'TES.Deposito') THEN RA."Id" ELSE 0 END 
	   LEFT JOIN "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD ON DD."IdDepositoDetalle" = CASE WHEN (RA."Entidad" = 'TES.DepositoDetalle') THEN RA."Id" ELSE 0 END;
	
	
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoRegla	NVARCHAR(40);
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA;
		END IF;
	
			
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionDeDeposito"(
			p_Registros			=>	:v_Registros,
			p_out_Resultado		=> 	v_Resultado
		);
		
		--Se eliminan los registros rechazados
		v_Registros = 
		 SELECT  M."IdInterno"
		 		,M."TipoEjecucion"
		 		,M."Id"
		 		,M."Entidad"
		 		,M."IdConcepto"
				,M."Descripcion"
		 		,M."IdDeposito"
		 		,M."IdEstadoDeposito"
		 		,M."IdDepositoDetalle"	 		
		 		,M."IdEstadoDepositoDetalle"
		   FROM :v_Registros as M
		  INNER JOIN (SELECT DISTINCT "IdInterno" FROM :v_Resultado WHERE "Resultado" = 'OK'
					  EXCEPT 
					  SELECT DISTINCT "IdInterno" FROM :v_Resultado WHERE "Resultado" = 'RECH') as R
				  ON R."IdInterno" = M."IdInterno";
		
		--Se Consulta diccionario
		v_TablaCodigosMensajes =
		SELECT DISTINCT "IdInterno","Codigo",'Id' as "CodigoDato1",CAST("Id" as NVARCHAR(520)) as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
		  FROM :v_Resultado
		 WHERE "Resultado" <> 'OK';
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);		
		
		p_out_ErroresDetalle =  SELECT E."Codigo",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."IdInterno" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'RECH';
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
		
		p_out_AdvertenciasDetalle =  SELECT E."Codigo",'' as "RequiereAutorizacion",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."IdInterno" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'ADVE';
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
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
		DECLARE v_EnviaLiquidar NVARCHAR(1);
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
	 
	 	--********************************************************
		--* Gestion de Nomina cuando entidad es 'TES.Deposito'  *
		--********************************************************
	 	UPDATE 	DD
		   SET	DD."IdEstado" = CASE WHEN :p_TipoEjecucion = 'EDP' AND DD."IdEstado" = 'ING' THEN 'EDP'
		   							 WHEN :p_TipoEjecucion = 'LIQ' AND DD."IdEstado" = 'EDP' THEN 'LIQ'
		   							 WHEN :p_TipoEjecucion = 'ANU' THEN :p_TipoEjecucion
		   							 ELSE :p_TipoEjecucion
		   						 END
		  FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD
		 INNER JOIN :v_Registros as RA ON RA."Id" = DD."IdDeposito"
		 WHERE RA."Entidad" = 'TES.Deposito';
		
		UPDATE D
		   SET D."IdEstado" 			= :p_TipoEjecucion,
		   	   D."IdConcepto"			= RA."IdConcepto",
			   D."Descripcion"			= RA."Descripcion",
		   	   D."IdUsuarioLiquidador" 	= CASE WHEN :p_TipoEjecucion = 'LIQ' THEN :p_IdUsuario ELSE D."IdUsuarioLiquidador" END,
			   D."FechaHoraLiquidacion" = CASE WHEN :p_TipoEjecucion = 'LIQ' 
			   								   THEN TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7') 
			   								   ELSE D."FechaHoraLiquidacion" 
			   							   END			   
		  FROM "BSCL.Tesoreria.BD::TES.Deposito" as D
		 INNER JOIN :v_Registros as RA ON RA."Id" = D."IdDeposito"
		 WHERE RA."Entidad" = 'TES.Deposito';
	 
	 	
	 	
	 	--**************************************************************
		--* Gestion de Nomina cuando entidad es 'TES.DepositoDetalle'  *
		--**************************************************************
	 	UPDATE 	DD
			   SET	DD."IdEstado" 		= :p_TipoEjecucion,
			   		DD."IdConcepto"		= RA."IdConcepto",
			   		DD."Descripcion"	= RA."Descripcion"
			  FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD
			 INNER JOIN :v_Registros as RA ON RA."Id" = DD."IdDepositoDetalle"
			 WHERE RA."Entidad" = 'TES.DepositoDetalle';
			 
	 
	 	UPDATE D
			   SET D."IdEstado"				= CASE WHEN :p_TipoEjecucion = 'LIQ' AND "DocumentoSinLiquidar" > 0  THEN 'LIP'
				   								   WHEN :p_TipoEjecucion = 'LIQ' AND "DocumentoSinLiquidar" = 0  THEN 'LIQ'				   								   
				   								   WHEN :p_TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" > 0 THEN D."IdEstado"
				   								   WHEN :p_TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" = 0 THEN 'ANU'
				   							   END,
				   D."Monto"				= CASE WHEN :p_TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" > 0 THEN (D."Monto" - I."MontoAnulado")
				   								   WHEN :p_TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" = 0 THEN (I."MontoTotal")
				   								   ELSE D."Monto"
				   							   END,
				   D."IdConcepto"			= CASE WHEN :p_TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" = 0 THEN RA."IdConcepto" END,
			   	   D."Descripcion"			= CASE WHEN :p_TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" = 0 THEN RA."Descripcion" END,
			   	   D."IdUsuarioLiquidador" 	= CASE WHEN :p_TipoEjecucion = 'LIQ' THEN :p_IdUsuario ELSE D."IdUsuarioLiquidador" END,
				   D."FechaHoraLiquidacion" = CASE WHEN :p_TipoEjecucion = 'LIQ' 
				   								   THEN TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7') 
				   								   ELSE D."FechaHoraLiquidacion" 
				   							   END				   
			  FROM "BSCL.Tesoreria.BD::TES.Deposito" as D
			 INNER JOIN "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD ON DD."IdDeposito" = D."IdDeposito"		 
			 INNER JOIN (SELECT D."IdDeposito",
								SUM(CFP."Monto") AS "MontoTotal",
			 					SUM(CASE WHEN DD."IdEstado" = 'ANU' THEN CFP."Monto" ELSE 0 END) AS "MontoAnulado",
								(COUNT(DD."IdDepositoDetalle") - SUM( CASE WHEN DD."IdEstado" = 'ANU' THEN 1 ELSE 0 END )) as "NominaDetSinAnular",
								(COUNT(DD."IdDepositoDetalle") - SUM( CASE WHEN DD."IdEstado" IN ('LIQ','ANU') THEN 1 ELSE 0 END )) as "DocumentoSinLiquidar"
						   FROM "BSCL.Tesoreria.BD::TES.Deposito" as D
						  INNER JOIN "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD ON DD."IdDeposito" = D."IdDeposito"
						  INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = DD."IdComprobanteFormaDePago"
						  GROUP BY D."IdDeposito"
			 			) as I
			 		 ON I."IdDeposito" = D."IdDeposito"
			 INNER JOIN :v_Registros as RA ON RA."Id" = DD."IdDepositoDetalle"
			 WHERE RA."Entidad" = 'TES.DepositoDetalle'; 
	 
	 
	 	
	 	IF (:p_TipoEjecucion = 'ANU') THEN			
			--*********************************
			--* Actualización de Comprobantes *
			--*********************************
			UPDATE 	CFP
			   SET	CFP."IdEstado" 		= CASE WHEN instr("EstadosPosibleTipoMovimientoMedioDePago",'CRT')>0 THEN 'CRT' ELSE 'ING' END,
			   		CFP."IdDeposito"  	= 0,
			   		CFP."Depositado"  	= null
			  FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD
			 INNER JOIN :v_Registros as RA ON RA."Id" = CASE WHEN (RA."Entidad" = 'TES.Deposito') THEN DD."IdDeposito"
			 												 ELSE DD."IdDepositoDetalle"
			 											 END 
			 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = DD."IdComprobanteFormaDePago"
			 INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" as TMMP ON TMMP."IdMedioDePago" = CFP."IdMedioDePago" AND TMMP."IdTipoMovimiento" = CFP."IdTipoMovimiento";			
	 	END IF;
	 	
	 	
	 	IF (:p_TipoEjecucion = 'LIQ') THEN
			--*****************************************************************
			--* Liquidar comprobantes                                         *
			--*****************************************************************	
			v_ComprobanteFormaDePago = 
			SELECT CFP."IdComprobante",CFP."IdTipoMovimiento",CFP."IdComprobanteFormaDePago"
			  FROM :v_Registros as RL
			 INNER JOIN "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD
			 		 ON RL."Id" = CASE WHEN (RL."Entidad" = 'TES.Deposito') THEN DD."IdDeposito" ELSE DD."IdDepositoDetalle" END
			 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP 
			 		 ON CFP."IdComprobanteFormaDePago" = DD."IdComprobanteFormaDePago"
			 WHERE DD."IdEstado" = 'LIQ'
			   AND CFP."IdEstado" <> 'LIQ';
			 
			SELECT CASE WHEN COUNT("IdComprobante") > 0 THEN 'S' ELSE 'N' END INTO v_EnviaLiquidar FROM :v_ComprobanteFormaDePago;		
			IF (:v_EnviaLiquidar = 'S') THEN
				v_ComprobantesALiquidar =
				SELECT "IdComprobante","IdTipoMovimiento",'CLI' as "AfectaCuentaInversion" FROM :v_ComprobanteFormaDePago GROUP BY "IdComprobante","IdTipoMovimiento";
				
				v_FormaDePagoALiquidar = 
				SELECT "IdComprobante","IdComprobanteFormaDePago" FROM :v_ComprobanteFormaDePago;
				
							
				CALL "BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes" (
					 p_IdEmpresa					=> p_IdEmpresa
					,p_IdUsuario					=> p_IdUsuario
					,p_IdModulo						=> p_IdModulo
					,p_UsaReglasNegocio				=> 'S'
					,p_FechaMovimiento				=> :p_FechaMovimiento			
					,p_TablaComprobante				=> :v_ComprobantesALiquidar
					,p_TablaComprobanteFormaDePago	=> :v_FormaDePagoALiquidar			
					--,p_out_ResultadoProceso			=> v_ResultadoProceso
					,p_out_InformacionContador  	=> p_out_InformacionContador
					,p_out_AdvertenciasContador		=> p_out_AdvertenciasContador
					,p_out_ErroresContador			=> p_out_ErroresContador
				);
				IF (:p_out_ErroresContador) > 0 THEN
					p_out_AdvertenciasDetalle	= SELECT "Codigo", "RequiereAutorizacion", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_AdvertenciasDetalle" UNION ALL										  
										  		  SELECT "Codigo", "RequiereAutorizacion", "Descripcion" FROM :p_out_AdvertenciasDetalle;
										  
					p_out_ErroresDetalle		= SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ErroresDetalle" UNION ALL										  
												  SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle;
										  
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			ELSE
				-- Liquidar las Formas de Pago
				UPDATE CFP
				   SET  "Depositado"		= 'S',
				   		"FechaAfectaSaldo"	= CASE WHEN instr(TFP."AfectaSaldoEnEstado",'LIQ') > 0 
				    								AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0 
				    								AND COALESCE(CFP."FechaAfectaSaldo",'0001-01-01') = '0001-01-01' 
				    							   THEN ADD_DAYS(TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD'), CFP."DiasRetencion") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
				    							   ELSE CFP."FechaAfectaSaldo"
											   END
			  	FROM :v_Registros as RL
			 	INNER JOIN "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD
			 		    ON RL."Id" = CASE WHEN (RL."Entidad" = 'TES.Deposito') THEN DD."IdDeposito" ELSE DD."IdDepositoDetalle" END
			 	INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP 
			 		    ON CFP."IdComprobanteFormaDePago" = DD."IdComprobanteFormaDePago"
				INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" as TFP
						ON TFP."IdTipoMovimiento" = CFP."IdTipoMovimiento" AND TFP."IdMedioDePago" = CFP."IdMedioDePago"
			 	WHERE DD."IdEstado" = 'LIQ'
			   	  AND CFP."IdEstado" = 'LIQ';
			    
			    -- Actualizar Saldos
				v_CuentaBanco =
				SELECT DISTINCT "IdCuentaBancariaCorredora" as "IdCuentaBanco"
				  FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
				 INNER JOIN "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD
				 		 ON (DD."IdComprobanteFormaDePago" = CFP."IdComprobanteFormaDePago")			 		     
				 INNER JOIN :v_Registros as RL 
				 		 ON RL."Id" = CASE WHEN (RL."Entidad" = 'TES.Deposito') THEN DD."IdDeposito" ELSE DD."IdDepositoDetalle" END
				 WHERE COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0;
		
	
				CALL "BSCL.Tesoreria.BD.Servicios.BAN::ActualizarSaldosCuentaBanco" (
					p_FechaMovimiento 	=> :p_FechaMovimiento,
					p_CuentaBanco		=> :v_CuentaBanco
				);
			    
			END IF;
	 	END IF;	
	 	
	 	
	 	
	 	
	 	--********************************
		--* Resultado del proceso *
		--********************************
		v_TablaCodigosMensajes =
		SELECT  "Id" AS "IdInterno"
			   ,CASE WHEN (:p_TipoEjecucion = 'EDP') THEN 'ENVIO_NOMINA_A_DEPOSITAR_EXITO'
			   		 WHEN (:p_TipoEjecucion = 'LIQ') THEN (CASE WHEN "Entidad" = 'TES.Deposito' THEN 'DEPOSITO_LIQUIDADO_EXITO'
																WHEN "Entidad" = 'TES.DepositoDetalle' THEN 'DEPOSITO_DETALLE_LIQUIDADO_EXITO'
															END)
					 WHEN (:p_TipoEjecucion = 'ANU') THEN (CASE WHEN "Entidad" = 'TES.Deposito' THEN 'DEPOSITO_ANULADO_EXITO'
																WHEN "Entidad" = 'TES.DepositoDetalle' THEN 'DEPOSITO_DETALLE_ANULADO_EXITO'
															END)
			   	 END as "Codigo"			   	 
			   ,'Id' as "CodigoDato1"
			   ,CAST("Id" as NVARCHAR(520)) as "ValorDato1"
			   ,'' as "CodigoDato2"
			   ,'' as "ValorDato2" 
		  FROM :v_Registros;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);
		SIGNAL SALIDA_EXITOSA;
		
		
		--********************************
		--* LOG                          *
		--********************************
	END;
END;
GO