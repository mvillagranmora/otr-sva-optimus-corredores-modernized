SET SCHEMA "BSCL";   

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::GestionFormaDePago";
-- 
--  Proposito: Crear la Forma de Pago para un comprobante de tesorería
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 29/11/2015  
--

CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios::GestionFormaDePago"(
   -- Parametros de Validacion                                                                                            
   IN p_IdEmpresa                     	BIGINT,                                                                            
   IN p_IdUsuario                     	BIGINT,                                                                            
   IN p_IdModulo                      	NVARCHAR(3),
   IN p_simularOperacion              	NVARCHAR(1),
   IN p_UsaReglasNegocio              	NVARCHAR(1),
   IN p_FechaMovimiento			    	NVARCHAR(10),
   
   IN p_TipoEjecucion					NVARCHAR(3), 	-- ING 	: Ingreso de formas de pago
														-- CRT	: Certificacion de formas de pago
														-- EBV	: Envio a bóveda de las formas de pago
														-- EMI	: Forma de pago emitida (se generó el cheque o vale vista)
														-- ANU  : Anulacion de formas de pago 

   IN p_FormasDePago					TABLE("IdComprobante"                   BIGINT,
   											  "IdComprobanteFormaDePago"        BIGINT,
   											  "IdTipoMovimiento" 				NVARCHAR(6), -- IdTipoMovimiento: MOVING, MOVEGR

    										  "IdMedioDePago"					NVARCHAR(3),
										 	  "IdModalidadDePago"				NVARCHAR(6),
										 	  "IdMoneda"						NVARCHAR(3),
											  "Monto"							DECIMAL(26,8),											  
											  "IdCuentaBancariaCorredora"		BIGINT,
											  "IdTarjetaBancariaCorredora"		BIGINT,
											  "IdChequera"						BIGINT,
											  "NumeroDocumentoCorredora"		BIGINT,
											  "FechaDocumentoPagoCorredora"		NVARCHAR(40),

											  "IdCuentaBancariaSocio" 			BIGINT,
											  "IdBancoSocio"					BIGINT,
											  "NombreCuentaBancariaSocio"		NVARCHAR(100),
											  "NumeroCuentaBancariaSocio"		NVARCHAR(100),
											  "IdTipoCuentaBancariaSocio"		NVARCHAR(4),
											  "NombreTipoCuentaBancariaSocio"	NVARCHAR(100),
											  
											  "IdTarjetaBancariaSocio"			BIGINT,
											  "IdTipoTarjetaBancariaSocio"		NVARCHAR(40),
											  "NombreTipoTarjetaBancariaSocio"	NVARCHAR(100),
											  "NumeroTarjetaBancariaSocio"		NVARCHAR(100),
											  "NumeroSerieSocio"				NVARCHAR(40),
											  "NumeroDocumentoSocio"			BIGINT,
											  "FechaDocumentoPagoSocio"			NVARCHAR(40),
											  
											  "CertificadoAutomatico"			NVARCHAR(1)), --S: Certifica al momento de crear FP
											  								
   --Salidas para mensajes de advertencias y errores
   OUT p_out_InformacionContador  	   INT,
   OUT p_out_InformacionDetalle   	   TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
   OUT p_out_AdvertenciasContador      INT,
   OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
   OUT p_out_ErroresContador           INT,   
   OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY
) 
   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
	
BEGIN  
	
	--------------------
	--DECLARE Variables para Log
	--------------------
	DECLARE v_IdTipoLog NVARCHAR(20);
	DECLARE v_IdTipoRegistro NVARCHAR(20);
	DECLARE v_fechaHoraSistema NVARCHAR(40);
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_EXITOSA				CONDITION;
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;	
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_TablaCodigosMensajes		TABLE("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	DECLARE v_CajaMonedaUsuario         TABLE("IdCaja" BIGINT, "IdMoneda" NVARCHAR(3), "IdTipoMovimiento" NVARCHAR(6));
	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
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
	 
	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	 
	-- Busca cajas habilitadas para usuario(para validar IdCaja cuando se edite la modalidad de pago) 
	v_CajaMonedaUsuario =
	SELECT "IdCaja","IdMoneda","IdTipoMovimiento"
	  FROM ( 
			SELECT MAX("IdCaja") over (partition by "IdMoneda") as "MaxIdCaja","IdCaja","IdMoneda","IdTipoMovimiento"
		      FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCajaMonedaUsuario"
		     WHERE "IdEmpresa" = :p_IdEmpresa 		       
		       AND "IdUsuario" = :p_IdUsuario
		       AND "IdEstadoCaja" = 'ABT'
		       AND "IdEstadoCajaMoneda" = 'ACT'
		       AND "IdEstadoCajaUsuario" = 'ACT'
		       AND "FechaInicio" <= :p_FechaMovimiento
			   AND "FechaTermino" >= :p_FechaMovimiento ) AS T
	WHERE "MaxIdCaja" = "IdCaja";
	--	SELECT 'v_CajaMonedaUsuario', * FROM :v_CajaMonedaUsuario; -- revisar

	v_FormasDePago = 
	SELECT   DENSE_RANK() over (ORDER BY FP."IdComprobante") as "IdInterno"
			,FP."IdComprobante" as "IdComprobanteInformado"
			,C."IdComprobante" as "IdComprobante"
			,C."Monto" as "MontoComprobante"
			,C."IdEstado" as "IdEstadoComprobante"
			,FP."IdComprobanteFormaDePago" as "IdComprobanteFormaDePagoInformado"
			,C_FP."Cantidad" as "CantidadFormaDePagoInformadas"
			,C_FP."Monto" as "MontoFormaDePagoInformadas"
			,CFP."IdComprobanteFormaDePago"
			,CFP."IdTipoMovimiento"
			,CFP."IdEstado" as "IdEstadoComprobanteFormaDePago"
   			,FP."IdTipoMovimiento" as "IdTipoMovimientoInformado"
		  	,FP."IdMedioDePago" as "IdMedioDePagoInformado"
		  	,CFP."IdMedioDePago"
		  	,TMMP."TipoMedioDePago"
		  	,TMMP."EstadosPosibleTipoMovimientoMedioDePago"
	 	  	,FP."IdModalidadDePago"
	 	  	,FP."IdMoneda"
		  	,FP."Monto"
		  	,FP."IdCuentaBancariaCorredora"
		  	,FP."IdTarjetaBancariaCorredora"
		  	,FP."IdChequera"
		  	,FP."NumeroDocumentoCorredora"
		  	,FP."FechaDocumentoPagoCorredora"
		  	,FP."IdCuentaBancariaSocio"
		  	,FP."IdBancoSocio"
		  	,FP."NombreCuentaBancariaSocio"
		  	,FP."NumeroCuentaBancariaSocio"
		  	,FP."IdTipoCuentaBancariaSocio"
		  	,FP."NombreTipoCuentaBancariaSocio"		 
		  	,FP."IdTarjetaBancariaSocio"
		  	,FP."IdTipoTarjetaBancariaSocio"
		  	,FP."NombreTipoTarjetaBancariaSocio"
		  	,FP."NumeroTarjetaBancariaSocio"
		  	,FP."NumeroSerieSocio"
		  	,FP."NumeroDocumentoSocio"
		  	,FP."FechaDocumentoPagoSocio"
		  	,FP."CertificadoAutomatico"
			,COALESCE(CMU."IdCaja",0) AS "IdCaja" 
			,CFP."FechaAfectaSaldo"
			,CFP."IdUsuarioLiquidacion"
			,CFP."FechaHoraLiquidacion"

	  FROM :p_FormasDePago as FP
	 INNER JOIN (SELECT "IdComprobante",COUNT("IdComprobanteFormaDePago") as "Cantidad",SUM("Monto") as "Monto" FROM :p_FormasDePago GROUP BY "IdComprobante") as C_FP 
	 		 ON  C_FP."IdComprobante" = FP."IdComprobante"
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as C ON C."IdComprobante" = FP."IdComprobante"
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = FP."IdComprobanteFormaDePago" AND CFP."IdComprobante" = FP."IdComprobante"
	  LEFT JOIN (SELECT TMMP."IdTipoMovimiento",TMMP."IdMedioDePago",MP."TipoMedioDePago",TMMP."EstadosPosibleTipoMovimientoMedioDePago"
	   			   FROM "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" as TMMP
	   			  INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" as MP ON MP."IdMedioDePago" = TMMP."IdMedioDePago") as TMMP	   			  	  			
	  		 ON TMMP."IdTipoMovimiento" = CFP."IdTipoMovimiento" and TMMP."IdMedioDePago" = CFP."IdMedioDePago"
	  LEFT JOIN :v_CajaMonedaUsuario AS CMU ON CMU."IdMoneda" = FP."IdMoneda" AND (CMU."IdTipoMovimiento" = FP."IdTipoMovimiento" OR  CMU."IdTipoMovimiento" = 'TODOS');
	  --SELECT 'v_FormasDePago', * FROM :v_FormasDePago; -- revisar
     		 
	
	v_ChequesUtilizados =
	SELECT DISTINCT 
			FP."IdChequera",
			MIN(COALESCE(FP."NumeroDocumentoCorredora",0)) as "ChequeDesde",
			MAX(COALESCE(FP."NumeroDocumentoCorredora",0)) as "ChequeHasta",
			COALESCE(C."NumeroChequeActual",0) as "NumeroChequeActual",
			(MAX(FP."NumeroDocumentoCorredora") - MIN(FP."NumeroDocumentoCorredora") + 1) as "CantidadCheques"
	  FROM :v_FormasDePago AS FP
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.Chequera" as C ON C."IdChequera" = FP."IdChequera"
	 GROUP BY FP."IdChequera",C."NumeroChequeActual";
	
	--SELECT 'v_FormasDePago', * FROM :v_FormasDePago; --BORRAR
	--SELECT 'v_ChequesUtilizados', * FROM :v_ChequesUtilizados; --BORRAR
	
	--------------------------------------------
	-- VALIDACIONES
	-------------------------------------------- 
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		---Validaciones datos de entrada			
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA; 	
		END IF;

		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionFormaDePago"(
			p_TipoEjecucion			=>	:p_TipoEjecucion,
		    p_FormasDePago			=>	:v_FormasDePago,
		    p_InformacionCheques	=>	:v_ChequesUtilizados,
		    p_out_Resultado			=>	v_Resultado
		);
		
		--Se eliminan los registros rechazados
		v_FormasDePago = 
		 SELECT  M."IdInterno"
			 	,M."IdComprobanteInformado"
				,M."IdComprobante"
				,M."MontoComprobante"
				,M."IdEstadoComprobante"
				,M."IdComprobanteFormaDePagoInformado"
				,M."CantidadFormaDePagoInformadas"
				,M."MontoFormaDePagoInformadas"
				,M."IdComprobanteFormaDePago"
				,M."IdTipoMovimiento"
				,M."IdEstadoComprobanteFormaDePago"
	   			,M."IdTipoMovimientoInformado"
			  	,M."IdMedioDePagoInformado"
			  	,M."IdMedioDePago"
			  	,M."TipoMedioDePago"
			  	,M."EstadosPosibleTipoMovimientoMedioDePago"
		 	  	,M."IdModalidadDePago"
		 	  	,M."IdMoneda"
			  	,M."Monto"
			  	,M."IdCuentaBancariaCorredora"
			  	,M."IdTarjetaBancariaCorredora"
			  	,M."IdChequera"
			  	,M."NumeroDocumentoCorredora"
			  	,M."FechaDocumentoPagoCorredora"
			  	,M."IdCuentaBancariaSocio"
			  	,M."IdBancoSocio"
			  	,M."NombreCuentaBancariaSocio"
			  	,M."NumeroCuentaBancariaSocio"
			  	,M."IdTipoCuentaBancariaSocio"
			  	,M."NombreTipoCuentaBancariaSocio"		 
			  	,M."IdTarjetaBancariaSocio"
			  	,M."IdTipoTarjetaBancariaSocio"
			  	,M."NombreTipoTarjetaBancariaSocio"
			  	,M."NumeroTarjetaBancariaSocio"
			  	,M."NumeroSerieSocio"
			  	,M."NumeroDocumentoSocio"
			  	,M."FechaDocumentoPagoSocio"
			  	,M."CertificadoAutomatico"
			  	,M."IdCaja"
			  	,M."FechaAfectaSaldo"
			  	,M."IdUsuarioLiquidacion"
				,M."FechaHoraLiquidacion"
		   FROM :v_FormasDePago as M
		  INNER JOIN (SELECT DISTINCT "IdInterno" FROM :v_Resultado WHERE "Resultado" = 'OK'
					  EXCEPT 
					  SELECT DISTINCT "IdInterno" FROM :v_Resultado WHERE "Resultado" = 'RECH') as R
				  ON R."IdInterno" = M."IdInterno";
		
		--Se Consulta diccionario
		v_TablaCodigosMensajes =
		SELECT DISTINCT "IdInterno","Codigo",'' as "CodigoDato1",'' as "ValorDato1",'IdComprobante' as "CodigoDato2","IdComprobanteInformado" as "ValorDato2"
		  FROM :v_Resultado
		 WHERE "Resultado" <> 'OK';
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);		
		
		p_out_ErroresDetalle =  SELECT E."Codigo",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."IdInterno" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'RECH';
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
		
		p_out_AdvertenciasDetalle =  SELECT E."Codigo",'' as "RequiereAutorizacion",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."IdInterno" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'ADVE';
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END; 
	END IF;
		
	-- Sólo fue una simulacion del servicio.
	IF p_simularOperacion = 'S' THEN
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

		------------------------------
		--Ingreso de formas de pago
		------------------------------
		IF (:p_TipoEjecucion = 'ING') THEN
			v_IdTipoLog = 'ANULACION';
			v_IdTipoRegistro = 'ANULAR_FORMADEPAGO';
			v_IdComprobantes = SELECT "IdComprobante", SUM("Monto") AS "Monto", "IdMoneda" FROM :v_FormasDePago GROUP BY "IdComprobante", "IdMoneda";
			v_ValorAntiguo = SELECT
								 CFP."IdComprobanteFormaDePago"
								 ,CFP."IdComprobante"
								 ,CFP."IdTipoMovimiento"									 
								 ,CFP."NombreMedioDePago"									
								 ,CFP."NombreModalidadDePago"				 									 
								 ,CFP."NombreCuentaBancariaCorredora"
								 ,CFP."NumeroCuentaBancariaCorredora"									 									 
								 ,CFP."NombreCuentaBancariaSocio"
								 ,CFP."NumeroCuentaBancariaSocio"									 
								 ,CFP."IdMoneda"
								 ,CFP."Monto"									 
								 ,CFP."IdEstado"
					 		FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
					 		INNER JOIN :v_IdComprobantes AS CP ON (CFP."IdComprobante" = CP."IdComprobante") 
					 		WHERE CFP."IdEstado" = 'ING';	
		
			--1) Anula FP actual
		    UPDATE CFP
			SET "IdEstado" = 'MOD'
			FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
			INNER JOIN :v_FormasDePago AS CP ON (CFP."IdComprobante" = CP."IdComprobante");
	
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
			  p_IdEmpresa => p_IdEmpresa,
			  p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			
			v_Log =
				SELECT
				  "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL as "IdCabeceraLog",
				  :v_IdTipoLog as "IdTipoLog",
				  :p_IdModulo as "IdModulo",
				  :p_IdUsuario as "IdUsuario",
				  :p_IdEmpresa as "IdEmpresa",
				  TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') as "FechaSistemaLog",
				  :p_IdUsuario as "IdUsuarioAtendido",
		 		  COALESCE(COMP."IdPersona", 0)  as "IdPersona",
				  0 as "IdCliente",
				  '' as "EstadoCliente",
				  COALESCE(COMP."NombrePersona", '')  as "NombreCliente",
				  COALESCE(COMP."IdTipoDocumentoIdentidad", '') as "IdTipoDocumentoIdentidad",
				  COALESCE (COMP."DocumentoIdentidad", '') as "IdDocumentoIdentidad",
				  0 as "IdPortafolio",
				  '' as "EstadoPortafolio",
				  :v_IdTipoRegistro as "IdTipoRegistro",
				  COALESCE(CP."IdComprobante", 0) as "IdRegistro",
				  COALESCE(COMP."IdEstado", '') as "EstadoRegistro",
				  COALESCE(COMP."IdTipoMovimiento", '') as "IdTipoOperacion",
				  COALESCE(CP."IdComprobante", 0) as "Folio",
				  0 as "Cantidad",
				  0 as "PrecioTasa",
				  COALESCE(CP."Monto", 0) as "Monto",
				  COALESCE(CP."IdMoneda", '') as "IdMonedaOrigen",
				  '' as "IdMonedaLiquidacion",
				  '' as "TipoCambio",
				  '' as "Nemo",
				  0 as "IdProceso",
				  '' as "EstadoProceso",
				  'GestionFormaDePago Tesoreria' as "NombreProceso"
				FROM :v_IdComprobantes AS CP
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COMP ON (CP."IdComprobante" = COMP."IdComprobante");
				
				v_LogDetalleEliminacion = 
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Medio de Pago' AS "CampoRegistro"
						,va."NombreMedioDePago" AS "ValorAntiguo"
						,'' AS "ValorNuevo"
					FROM :v_ValorAntiguo AS va
					INNER JOIN :v_Log AS l ON l."IdRegistro" = va."IdComprobante"
					WHERE COALESCE(va."NombreMedioDePago", '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Modalidad de Pago' AS "CampoRegistro"
						,va."NombreModalidadDePago" AS "ValorAntiguo"
						,'' AS "ValorNuevo"
					FROM :v_ValorAntiguo AS va
					INNER JOIN :v_Log AS l ON l."IdRegistro" = va."IdComprobante"
					WHERE COALESCE(va."NombreModalidadDePago", '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Monto' AS "CampoRegistro"
						,TO_VARCHAR(va."Monto") AS "ValorAntiguo"
						,'' AS "ValorNuevo"
					FROM :v_ValorAntiguo AS va
					INNER JOIN :v_Log AS l ON l."IdRegistro" = va."IdComprobante"
					WHERE COALESCE(TO_VARCHAR(va."Monto"), '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Nombre Cuenta Bancaria Socio' AS "CampoRegistro"
						,va."NombreCuentaBancariaSocio" AS "ValorAntiguo"
						,'' AS "ValorNuevo"
					FROM :v_ValorAntiguo AS va
					INNER JOIN :v_Log AS l ON l."IdRegistro" = va."IdComprobante"
					WHERE COALESCE(va."NombreCuentaBancariaSocio", '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Nombre Cuenta Bancaria Corredora' AS "CampoRegistro"
						,va."NombreCuentaBancariaCorredora" AS "ValorAntiguo"
						,'' AS "ValorNuevo"
					FROM :v_ValorAntiguo AS va
					INNER JOIN :v_Log AS l ON l."IdRegistro" = va."IdComprobante"
					WHERE COALESCE(va."NombreCuentaBancariaCorredora", '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Numero Cuenta Bancaria Socio' AS "CampoRegistro"
						,va."NumeroCuentaBancariaSocio" AS "ValorAntiguo"
						,'' AS "ValorNuevo"
					FROM :v_ValorAntiguo AS va
					INNER JOIN :v_Log AS l ON l."IdRegistro" = va."IdComprobante"
					WHERE COALESCE(va."NumeroCuentaBancariaSocio", '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Numero Cuenta Bancaria Corredora' AS "CampoRegistro"
						,va."NumeroCuentaBancariaCorredora" AS "ValorAntiguo"
						,'' AS "ValorNuevo"
					FROM :v_ValorAntiguo AS va
					INNER JOIN :v_Log AS l ON l."IdRegistro" = va."IdComprobante"
					WHERE COALESCE(va."NumeroCuentaBancariaCorredora", '') <> ''
				
					UNION ALL 
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'ID Forma de Pago' AS "CampoRegistro"
						,CAST(va."IdComprobanteFormaDePago" AS NVARCHAR(50)) AS "ValorAntiguo"
						,'' AS "ValorNuevo"
					FROM :v_ValorAntiguo AS va
					INNER JOIN :v_Log AS l ON l."IdRegistro" = va."IdComprobante"
					WHERE COALESCE(va."IdComprobanteFormaDePago", 0) <> 0;
			
			--2) Inserta FP
			v_DatosFormaDePago = 
				SELECT  
					"BSCL.Tesoreria.BD.Secuencias::ComprobanteFormaDePago".NEXTVAL AS "IdComprobanteFormaDePago"
					,FormasDePago."IdComprobanteInformado" AS "IdComprobante"
					,FormasDePago."IdTipoMovimientoInformado" AS "IdTipoMovimiento"
					,FormasDePago."IdMedioDePagoInformado" AS "IdMedioDePago"
					,COALESCE(MedioDePago."Nombre",'') AS "NombreMedioDePago"
					,FormasDePago."IdModalidadDePago" AS "IdModalidadDePago"
					,COALESCE(ModalidadDePago."Nombre",'') AS "NombreModalidadDePago"
					,FormasDePago."IdCuentaBancariaCorredora" AS "IdCuentaBancariaCorredora"
					,COALESCE(CuentaBancariaCorredora."NombreCuentaBancaria",'') 	   AS "NombreCuentaBancariaCorredora"				
					,COALESCE(CuentaBancariaCorredora."NumeroCuentaBancaria",'') 	   AS "NumeroCuentaBancariaCorredora"			 			 				
					,COALESCE(CuentaBancariaCorredora."IdTipoCuentaBancaria",'') 	   AS "IdTipoCuentaBancariaCorredora"
					,COALESCE(CuentaBancariaCorredora."NombreTipoCuentaBancaria",'')   AS "NombreTipoCuentaBancariaCorredora"
					,FormasDePago."IdTarjetaBancariaCorredora" AS "IdTarjetaBancariaCorredora"
					,COALESCE(TarjetaBancariaCorredora."IdTipoTarjetaBancaria",'') 	   AS "IdTipoTarjetaBancariaCorredora"
					,COALESCE(TarjetaBancariaCorredora."NombreTipoTarjetaBancaria",'') AS "NombreTipoTarjetaBancariaCorredora"
					,COALESCE(TarjetaBancariaCorredora."NumeroTarjetaBancaria",'') 	   AS "NumeroTarjetaBancariaCorredora"
					,FormasDePago."IdChequera" AS "IdChequera"
					,FormasDePago."NumeroDocumentoCorredora" AS "NumeroDocumentoCorredora"
					,FormasDePago."FechaDocumentoPagoCorredora" AS "FechaDocumentoPagoCorredora"
					,FormasDePago."IdCuentaBancariaSocio" AS "IdCuentaBancariaSocio"
					,FormasDePago."IdBancoSocio" AS "IdBancoSocio"
					,COALESCE(FormasDePago."NombreCuentaBancariaSocio",'') 	    AS "NombreCuentaBancariaSocio"
					,COALESCE(FormasDePago."NumeroCuentaBancariaSocio",'') 		AS "NumeroCuentaBancariaSocio"
					,COALESCE(FormasDePago."IdTipoCuentaBancariaSocio",'') 		AS "IdTipoCuentaBancariaSocio"
					,COALESCE(FormasDePago."NombreTipoCuentaBancariaSocio",'') 	AS "NombreTipoCuentaBancariaSocio"
					,FormasDePago."IdTarjetaBancariaSocio" AS "IdTarjetaBancariaSocio"
					,COALESCE(FormasDePago."IdTipoTarjetaBancariaSocio",'') 	AS "IdTipoTarjetaBancariaSocio"
					,COALESCE(FormasDePago."NombreTipoTarjetaBancariaSocio",'') AS "NombreTipoTarjetaBancariaSocio"
					,COALESCE(FormasDePago."NumeroTarjetaBancariaSocio",'') 	AS "NumeroTarjetaBancariaSocio"
					,FormasDePago."NumeroSerieSocio" AS "NumeroSerieSocio"
					,FormasDePago."NumeroDocumentoSocio" AS "NumeroDocumentoSocio"
					,FormasDePago."FechaDocumentoPagoSocio" AS "FechaDocumentoPagoSocio"
				    ,FormasDePago."IdMoneda" AS "IdMoneda"
					,FormasDePago."Monto" AS "Monto"
					,COALESCE(CuentaBancariaCorredora."IdPlaza",0) AS "IdPlaza"
					,CASE 	
						  WHEN (TMMP."AfectoADiasRetencion" = 'S' AND FormasDePago."IdBancoSocio" = CuentaBancariaCorredora."IdBanco" AND FormasDePago."IdBancoSocio" > 0) THEN CuentaBancariaCorredora."PlazoRetencionMismoBanco"
						  WHEN (TMMP."AfectoADiasRetencion" = 'S' AND FormasDePago."IdBancoSocio" <> CuentaBancariaCorredora."IdBanco" AND FormasDePago."IdBancoSocio" > 0) THEN CuentaBancariaCorredora."PlazoRetencionOtroBanco"
						  ELSE 0
		 	  		 END AS "DiasRetencion"
				    ,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"
				    ,CASE WHEN COALESCE(FormasDePago."IdEstadoComprobanteFormaDePago", 'ING') = 'LIQ' THEN FormasDePago."FechaAfectaSaldo"
				    	  WHEN instr(TMMP."AfectaSaldoEnEstado", 
				    				 ( CASE WHEN FormasDePago."IdTipoMovimiento" = 'MOVING' AND FormasDePago."CertificadoAutomatico" = 'N' THEN 'ING'
				    						WHEN FormasDePago."IdTipoMovimiento" = 'MOVING' AND FormasDePago."CertificadoAutomatico" = 'S' AND instr(TMMP."EstadosPosibleTipoMovimientoMedioDePago", 'CRT') = 0 THEN 'ING'
						  					WHEN FormasDePago."IdTipoMovimiento" = 'MOVING' AND FormasDePago."CertificadoAutomatico" = 'S' AND instr(TMMP."EstadosPosibleTipoMovimientoMedioDePago", 'CRT') > 0 THEN 'CRT'
				    						WHEN FormasDePago."IdTipoMovimiento" = 'MOVEGR' AND COALESCE(FormasDePago."NumeroDocumentoCorredora",0) = 0 THEN 'ING'
				    						WHEN FormasDePago."IdTipoMovimiento" = 'MOVEGR' AND COALESCE(FormasDePago."NumeroDocumentoCorredora",0) > 0 AND instr(TMMP."EstadosPosibleTipoMovimientoMedioDePago", 'EMI') = 0 THEN 'ING'
											WHEN FormasDePago."IdTipoMovimiento" = 'MOVEGR' AND COALESCE(FormasDePago."NumeroDocumentoCorredora",0) > 0 AND instr(TMMP."EstadosPosibleTipoMovimientoMedioDePago", 'EMI') > 0 THEN 'EMI'
				    				    END) 
				    				) > 0
				    	   AND COALESCE(FormasDePago."IdCuentaBancariaCorredora",0) > 0
					 	  THEN (CASE WHEN (FormasDePago."IdBancoSocio" = CuentaBancariaCorredora."IdBanco" AND FormasDePago."IdBancoSocio" > 0) 
					 	  		THEN ADD_DAYS(TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD'), CuentaBancariaCorredora."PlazoRetencionMismoBanco") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
					 	  		ELSE ADD_DAYS(TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD'), CuentaBancariaCorredora."PlazoRetencionOtroBanco") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
					 	  		END)
						  ELSE '0001-01-01'
				      END AS "FechaAfectaSaldo"
					,CASE WHEN FormasDePago."IdTipoMovimientoInformado" = 'MOVING' AND FormasDePago."CertificadoAutomatico" = 'N' THEN 'ING'
						  WHEN FormasDePago."IdTipoMovimientoInformado" = 'MOVING' AND FormasDePago."CertificadoAutomatico" = 'S' AND instr(TMMP."EstadosPosibleTipoMovimientoMedioDePago", 'CRT') = 0 THEN 'ING'
						  WHEN FormasDePago."IdTipoMovimientoInformado" = 'MOVING' AND FormasDePago."CertificadoAutomatico" = 'S' AND instr(TMMP."EstadosPosibleTipoMovimientoMedioDePago", 'CRT') > 0 THEN 'CRT'
						  WHEN FormasDePago."IdTipoMovimientoInformado" = 'MOVEGR' AND COALESCE(FormasDePago."NumeroDocumentoCorredora",0) = 0 THEN 'ING'
						  WHEN FormasDePago."IdTipoMovimientoInformado" = 'MOVEGR' AND COALESCE(FormasDePago."NumeroDocumentoCorredora",0) > 0 AND instr(TMMP."EstadosPosibleTipoMovimientoMedioDePago", 'EMI') = 0 THEN 'ING'
						  WHEN FormasDePago."IdTipoMovimientoInformado" = 'MOVEGR' AND COALESCE(FormasDePago."NumeroDocumentoCorredora",0) > 0 AND instr(TMMP."EstadosPosibleTipoMovimientoMedioDePago", 'EMI') > 0 THEN 'EMI'
					  END AS "IdEstado"
			  FROM :v_FormasDePago AS FormasDePago
			  INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" AS TMMP 
					  ON TMMP."IdTipoMovimiento" = FormasDePago."IdTipoMovimientoInformado"
				     AND TMMP."IdMedioDePago" = FormasDePago."IdMedioDePagoInformado"
			   LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarMedioDePago" AS MedioDePago 
			  		  ON MedioDePago."IdMedioDePago" = FormasDePago."IdMedioDePagoInformado"
			   LEFT JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS ModalidadDePago 
			  		  ON ModalidadDePago."IdModalidadDePago" = FormasDePago."IdModalidadDePago"
			   LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" AS CuentaBancariaCorredora 
			  		  ON CuentaBancariaCorredora."IdCuentaBanco" = FormasDePago."IdCuentaBancariaCorredora"
			   LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" AS TarjetaBancariaCorredora 
			  		  ON TarjetaBancariaCorredora."IdTarjetaBancaria" = FormasDePago."IdTarjetaBancariaCorredora";
			  		 
			INSERT INTO  "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"(		
					  "IdComprobanteFormaDePago"
					 ,"IdComprobante"
					 ,"IdTipoMovimiento"
					 ,"IdMedioDePago"
					 ,"NombreMedioDePago"
					 ,"IdModalidadDePago"
					 ,"NombreModalidadDePago"				 
					 ,"IdCuentaBancariaCorredora"
					 ,"NombreCuentaBancariaCorredora"
					 ,"NumeroCuentaBancariaCorredora"
					 ,"IdTipoCuentaBancariaCorredora"
					 ,"NombreTipoCuentaBancariaCorredora"
					 ,"IdTarjetaBancariaCorredora"				 
					 ,"IdTipoTarjetaBancariaCorredora"				 
					 ,"NombreTipoTarjetaBancariaCorredora"
					 ,"NumeroTarjetaBancariaCorredora"
					 ,"IdChequera"
					 ,"NumeroDocumentoCorredora"
					 ,"FechaDocumentoPagoCorredora"
					 ,"IdCuentaBancariaSocio"
					 ,"IdBancoSocio"
					 ,"NombreCuentaBancariaSocio"
					 ,"NumeroCuentaBancariaSocio"
					 ,"IdTipoCuentaBancariaSocio"
					 ,"NombreTipoCuentaBancariaSocio"
					 ,"IdTarjetaBancariaSocio"
					 ,"IdTipoTarjetaBancariaSocio"				 
					 ,"NombreTipoTarjetaBancariaSocio"
					 ,"NumeroTarjetaBancariaSocio"				 
					 ,"NumeroSerieSocio"
					 ,"NumeroDocumentoSocio"
					 ,"FechaDocumentoPagoSocio"
					 ,"IdMoneda"
					 ,"Monto"
					 ,"IdPlaza"
					 ,"DiasRetencion"
					 ,"FechaIngresoRegistro"
					 ,"FechaAfectaSaldo"
					 ,"IdEstado"
			) SELECT 
				"IdComprobanteFormaDePago"
					 ,"IdComprobante"
					 ,"IdTipoMovimiento"
					 ,"IdMedioDePago"
					 ,"NombreMedioDePago"
					 ,"IdModalidadDePago"
					 ,"NombreModalidadDePago"				 
					 ,"IdCuentaBancariaCorredora"
					 ,"NombreCuentaBancariaCorredora"
					 ,"NumeroCuentaBancariaCorredora"
					 ,"IdTipoCuentaBancariaCorredora"
					 ,"NombreTipoCuentaBancariaCorredora"
					 ,"IdTarjetaBancariaCorredora"				 
					 ,"IdTipoTarjetaBancariaCorredora"				 
					 ,"NombreTipoTarjetaBancariaCorredora"
					 ,"NumeroTarjetaBancariaCorredora"
					 ,"IdChequera"
					 ,"NumeroDocumentoCorredora"
					 ,"FechaDocumentoPagoCorredora"
					 ,"IdCuentaBancariaSocio"
					 ,"IdBancoSocio"
					 ,"NombreCuentaBancariaSocio"
					 ,"NumeroCuentaBancariaSocio"
					 ,"IdTipoCuentaBancariaSocio"
					 ,"NombreTipoCuentaBancariaSocio"
					 ,"IdTarjetaBancariaSocio"
					 ,"IdTipoTarjetaBancariaSocio"				 
					 ,"NombreTipoTarjetaBancariaSocio"
					 ,"NumeroTarjetaBancariaSocio"				 
					 ,"NumeroSerieSocio"
					 ,"NumeroDocumentoSocio"
					 ,"FechaDocumentoPagoSocio"
					 ,"IdMoneda"
					 ,"Monto"
					 ,"IdPlaza"
					 ,"DiasRetencion"
					 ,"FechaIngresoRegistro"
					 ,"FechaAfectaSaldo"
					 ,"IdEstado"
			FROM :v_DatosFormaDePago;
			
			v_IdTipoLog = 'CREACION';
			v_IdTipoRegistro = 'CREAR_FORMADEPAGO';
			v_LogCabecera = 
			    SELECT
			        "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL AS "IdCabeceraLog",
			        :v_IdTipoLog AS "IdTipoLog",
			        :p_IdModulo AS "IdModulo",
			        :p_IdUsuario AS "IdUsuario",
			        :p_IdEmpresa AS "IdEmpresa",
			        TO_VARCHAR(v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaSistemaLog",
			        :p_IdUsuario AS "IdUsuarioAtendido",
			        COALESCE(COMP."IdPersona", 0) AS "IdPersona",
			        0 AS "IdCliente",
			        '' AS "EstadoCliente",
			        COALESCE(COMP."NombrePersona", '') AS "NombreCliente",
			        COALESCE(COMP."IdTipoDocumentoIdentidad", '') AS "IdTipoDocumentoIdentidad",
			        COALESCE(COMP."DocumentoIdentidad", '') AS "IdDocumentoIdentidad",
			        0 AS "IdPortafolio",
			        '' AS "EstadoPortafolio",
			        :v_IdTipoRegistro AS "IdTipoRegistro",
			       	COALESCE(DFP."IdComprobante", 0) AS "IdRegistro",
			        COALESCE(COMP."IdEstado", '') AS "EstadoRegistro",
			        COALESCE(COMP."IdTipoMovimiento", '') AS "IdTipoOperacion",
			        COALESCE(DFP."IdComprobante", 0) AS "Folio",
			        0 AS "Cantidad",
			        0 AS "PrecioTasa",
			        SUM(DFP."Monto") AS "Monto",
			        COALESCE(DFP."IdMoneda", '') AS "IdMonedaOrigen",
			        '' AS "IdMonedaLiquidacion",
			        '' AS "TipoCambio",
			        '' AS "Nemo",
			        0 AS "IdProceso",
			        '' AS "EstadoProceso",
			        'GestionFormaDePago Tesoreria' AS "NombreProceso"
			    FROM :v_DatosFormaDePago AS DFP
			    INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COMP ON (DFP."IdComprobante" = COMP."IdComprobante")
			    GROUP BY DFP."IdComprobante", DFP."IdMoneda", COMP."IdPersona", COMP."NombrePersona", COMP."IdTipoDocumentoIdentidad", COMP."DocumentoIdentidad", COMP."IdEstado", COMP."IdTipoMovimiento" ;
			   
			   v_LogDetalleInsercion = 
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Medio de Pago' AS "CampoRegistro"
						,'' AS "ValorAntiguo"
						,dfp."NombreMedioDePago" AS "ValorNuevo"
					FROM :v_DatosFormaDePago AS dfp
					INNER JOIN :v_LogCabecera AS l ON l."IdRegistro" = dfp."IdComprobante"
					WHERE COALESCE(dfp."NombreMedioDePago", '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Modalidad de Pago' AS "CampoRegistro"
						,'' AS "ValorAntiguo"
						,dfp."NombreModalidadDePago" AS "ValorNuevo"
					FROM :v_DatosFormaDePago AS dfp
					INNER JOIN :v_LogCabecera AS l ON l."IdRegistro" = dfp."IdComprobante"
					WHERE COALESCE(dfp."NombreModalidadDePago", '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Monto' AS "CampoRegistro"
						,'' AS "ValorAntiguo"
						,TO_VARCHAR(dfp."Monto") AS "ValorNuevo"
					FROM :v_DatosFormaDePago AS dfp
					INNER JOIN :v_LogCabecera AS l ON l."IdRegistro" = dfp."IdComprobante"
					WHERE COALESCE(TO_VARCHAR(dfp."Monto"), '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Nombre Cuenta Bancaria Socio' AS "CampoRegistro"
						,'' AS "ValorAntiguo"
						,dfp."NombreCuentaBancariaSocio" AS "ValorNuevo"
					FROM :v_DatosFormaDePago AS dfp
					INNER JOIN :v_LogCabecera AS l ON l."IdRegistro" = dfp."IdComprobante"
					WHERE COALESCE(dfp."NombreCuentaBancariaSocio", '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Nombre Cuenta Bancaria Corredora' AS "CampoRegistro"
						,'' AS "ValorAntiguo"
						,dfp."NombreCuentaBancariaCorredora" AS "ValorNuevo"
					FROM :v_DatosFormaDePago AS dfp
					INNER JOIN :v_LogCabecera AS l ON l."IdRegistro" = dfp."IdComprobante"
					WHERE COALESCE(dfp."NombreCuentaBancariaCorredora", '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Numero Cuenta Bancaria Socio' AS "CampoRegistro"
						,'' AS "ValorAntiguo"
						,dfp."NumeroCuentaBancariaSocio" AS "ValorNuevo"
					FROM :v_DatosFormaDePago AS dfp
					INNER JOIN :v_LogCabecera AS l ON l."IdRegistro" = dfp."IdComprobante"
					WHERE COALESCE(dfp."NumeroCuentaBancariaSocio", '') <> ''
					
					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'Numero Cuenta Bancaria Corredora' AS "CampoRegistro"
						,'' AS "ValorAntiguo"
						,dfp."NumeroCuentaBancariaCorredora" AS "ValorNuevo"
					FROM :v_DatosFormaDePago AS dfp
					INNER JOIN :v_LogCabecera AS l ON l."IdRegistro" = dfp."IdComprobante"
					WHERE COALESCE(dfp."NumeroCuentaBancariaCorredora", '') <> ''

					UNION ALL
					
					SELECT
						"IdCabeceraLog" AS "IdCabeceraLog"
						,v_IdTipoRegistro AS "SubRegistro"
						,'ID Forma de Pago' AS "CampoRegistro"
						,'' AS "ValorAntiguo"
						,CAST(dfp."IdComprobanteFormaDePago" AS NVARCHAR(50)) AS "ValorNuevo"
					FROM :v_DatosFormaDePago AS dfp
					INNER JOIN :v_LogCabecera AS l ON l."IdRegistro" = dfp."IdComprobante"
					WHERE COALESCE(CAST(dfp."IdComprobanteFormaDePago" AS NVARCHAR(50)), '') <> '';
			  		  
		    -- Actualiza la caja cuando hay diferencias (el comprobante pudo haber entrado como deposito MODDEP sin caja y se pudo haber editado a caja(MODCAJ) por lo que necesita actulizar IdCaja)
			UPDATE COM
			   SET	COM."IdCaja"	= FP."IdCaja"
			  FROM "BSCL.Tesoreria.BD::TES.Comprobante" as COM			  
			 INNER JOIN :v_FormasDePago as FP ON FP."IdComprobante" = COM."IdComprobante"
			 WHERE FP."IdModalidadDePago" = 'MODCAJ'
			 AND COM."IdCaja"	= FP."IdCaja";

			
			--3) Actualiza chequera	
			UPDATE C
			   SET	C."NumeroChequeActual"	= (RP."ChequeHasta" + 1),
			   	   	C."Remanente"			= (C."Remanente" - RP."CantidadCheques")
			  FROM "BSCL.Tesoreria.BD::TES.Chequera" as C
			 INNER JOIN :v_ChequesUtilizados as RP ON RP."IdChequera" = C."IdChequera"
			 WHERE RP."IdChequera" > 0
			   AND "ChequeDesde" > 0
			   AND "ChequeHasta" > 0;
			   
	    CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log =>:v_Log , p_LogDetalle => :v_LogDetalleEliminacion);
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log =>:v_LogCabecera , p_LogDetalle => :v_LogDetalleInsercion);
		
		END IF;

	
		----------------------------------
		--Certificacion de Formas de pago
		----------------------------------
		If (:p_TipoEjecucion = 'CRT') THEN
			
			UPDATE CFP
			SET "IdEstado" 				 = 'CRT',
				"IdUsuarioCertificacion" = :p_IdUsuario,
				"FechaHoraCertificacion" = TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7'),
				"Depositado"			 = CASE WHEN CFP."IdMedioDePago" = 'EFE' THEN 'N' ELSE "Depositado" END,
				"FechaAfectaSaldo"		 = CASE WHEN instr(TMMP."AfectaSaldoEnEstado",'CRT') > 0 
		    									 AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0 
		    									 AND COALESCE(CFP."FechaAfectaSaldo",'0001-01-01') = '0001-01-01' 
		    							   	    THEN ADD_DAYS(TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD'), CFP."DiasRetencion") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
		    							   	    ELSE CFP."FechaAfectaSaldo"
		    						   		END
			FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
			INNER JOIN :v_FormasDePago AS FP 
					ON CFP."IdComprobanteFormaDePago" = FP."IdComprobanteFormaDePagoInformado"
			INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" AS TMMP
				    ON TMMP."IdTipoMovimiento" = FP."IdTipoMovimientoInformado"
			       AND TMMP."IdMedioDePago" = FP."IdMedioDePagoInformado";
			       
			   
			v_ComprobanteFormaDePago = 
			SELECT CFP."IdComprobante",CFP."IdTipoMovimiento",CFP."IdComprobanteFormaDePago"
			  FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
			 INNER JOIN :v_FormasDePago AS FP 
					 ON CFP."IdComprobanteFormaDePago" = FP."IdComprobanteFormaDePagoInformado"
			 WHERE CFP."IdTipoMovimiento" = 'MOVING' AND CFP."IdMedioDePago" = 'EFE';

			
			SELECT CASE WHEN COUNT("IdComprobante") > 0 THEN 'S' ELSE 'N' END INTO v_EnviaLiquidar FROM :v_ComprobanteFormaDePago;		
			IF (:v_EnviaLiquidar = 'S') THEN
				v_ComprobantesALiquidar =
				SELECT "IdComprobante","IdTipoMovimiento",'' as "AfectaCuentaInversion" FROM :v_ComprobanteFormaDePago GROUP BY "IdComprobante","IdTipoMovimiento";
				
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
					,p_out_InformacionContador  	=> p_out_InformacionContador
					,p_out_AdvertenciasContador		=> p_out_AdvertenciasContador
					,p_out_ErroresContador			=> p_out_ErroresContador
				);			
				p_out_AdvertenciasDetalle	= SELECT "Codigo", "RequiereAutorizacion", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_AdvertenciasDetalle" UNION ALL										  
									  		  SELECT "Codigo", "RequiereAutorizacion", "Descripcion" FROM :p_out_AdvertenciasDetalle;
									  
				p_out_ErroresDetalle		= SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ErroresDetalle" UNION ALL										  
											  SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle;
												  
				IF (:p_out_ErroresContador) > 0 THEN				  
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END IF;						       
		END IF;
		
		
		----------------------------------
		--Emisión de Formas de pago
		----------------------------------
		If (:p_TipoEjecucion = 'EMI') THEN
			
			UPDATE CFP
			SET "IdEstado" 				= 'EMI',
				"FechaAfectaSaldo"		= CASE WHEN instr(TMMP."AfectaSaldoEnEstado",'EMI') > 0 
		    									AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0 
		    									AND COALESCE(CFP."FechaAfectaSaldo",'0001-01-01') = '0001-01-01' 
		    							   	   THEN ADD_DAYS(TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD'), CFP."DiasRetencion") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
		    							   	   ELSE CFP."FechaAfectaSaldo"
		    						   		END
			FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
			INNER JOIN :v_FormasDePago AS FP 
					ON CFP."IdComprobanteFormaDePago" = FP."IdComprobanteFormaDePagoInformado"
			INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" AS TMMP 
				    ON TMMP."IdTipoMovimiento" = FP."IdTipoMovimientoInformado"
			       AND TMMP."IdMedioDePago" = FP."IdMedioDePagoInformado";
		END IF;
		
		
		----------------------------------
		--Envio a Boveda de Formas de pago
		----------------------------------
		IF (:p_TipoEjecucion = 'EBV') THEN
			
			UPDATE CFP
			SET "IdEstado" = 'EBV'
			FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
			INNER JOIN :v_FormasDePago AS FP ON (CFP."IdComprobanteFormaDePago" = FP."IdComprobanteFormaDePagoInformado");			
		END IF;
		
		
		---------------------------------
		--Anulacion de formas de pago
		---------------------------------
		If (:p_TipoEjecucion = 'ANU') THEN
			
			UPDATE CFP
			SET "IdEstado" = 'ANU'
			FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
			INNER JOIN :v_FormasDePago AS FP ON (CFP."IdComprobanteFormaDePago" = FP."IdComprobanteFormaDePagoInformado");			
		END IF;
		
		----------------------------------
		--* ACTUALIZAR SALDOS            *
		----------------------------------
		v_CuentaBanco =
		SELECT DISTINCT FP."IdCuentaBancariaCorredora" as "IdCuentaBanco"
		  FROM :v_FormasDePago AS FP
		 WHERE COALESCE("IdCuentaBancariaCorredora",0) > 0;

		CALL "BSCL.Tesoreria.BD.Servicios.BAN::ActualizarSaldosCuentaBanco" (
			p_FechaMovimiento 	=> :p_FechaMovimiento,
			p_CuentaBanco		=> :v_CuentaBanco
		);
		
		
		--------------------------------------------
		-- RESULTADO DEL PROCESO
		--------------------------------------------
		v_TablaCodigosMensajes =
		SELECT  FP."IdComprobanteInformado" AS "IdInterno"
			   ,CASE WHEN (:p_TipoEjecucion = 'ING') THEN (CASE WHEN FP."IdTipoMovimientoInformado" = 'MOVING' AND FP."CertificadoAutomatico" = 'N' THEN 'FORMA_PAGO_INGRESADA'
																WHEN FP."IdTipoMovimientoInformado" = 'MOVING' AND FP."CertificadoAutomatico" = 'S' THEN 'FORMA_PAGO_INGRESADA_CERTIFICADA'
																WHEN FP."IdTipoMovimientoInformado" = 'MOVEGR' AND COALESCE(FP."NumeroDocumentoCorredora",0) = 0 THEN 'FORMA_PAGO_INGRESADA'
																WHEN FP."IdTipoMovimientoInformado" = 'MOVEGR' AND COALESCE(FP."NumeroDocumentoCorredora",0) > 0 THEN 'FORMA_PAGO_INGRESADA_EMITIDA'
															END)
					 WHEN (:p_TipoEjecucion = 'CRT') THEN 'FORMA_PAGO_CERTIFICADA'
					 WHEN (:p_TipoEjecucion = 'ANU') THEN 'FORMA_PAGO_ANULADA'
					 WHEN (:p_TipoEjecucion = 'EMI') THEN 'FORMA_PAGO_EMITIDA'
					 WHEN (:p_TipoEjecucion = 'EBV') THEN 'FORMA_PAGO_ENVIADA_BOVEDA'
				 END AS "Codigo"
			   ,'IdComprobante' as "CodigoDato1"
			   ,CAST(FP."IdComprobanteInformado" as NVARCHAR(520)) as "ValorDato1"
			   ,'IdComprobante' as "CodigoDato2"
			   ,CAST(FP."IdComprobanteInformado" as NVARCHAR(520)) as "ValorDato2" 
		  FROM :v_FormasDePago AS FP;		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);
		SIGNAL SALIDA_EXITOSA;
    END;
END;