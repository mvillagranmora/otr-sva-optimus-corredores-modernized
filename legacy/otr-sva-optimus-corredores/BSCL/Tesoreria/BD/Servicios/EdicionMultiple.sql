SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::EdicionMultiple";

CREATE PROCEDURE 
-- 
--  Proposito: Bloquear o desbloquear movientos o comprobantes de tesoreria
--  Autor: NRM - BCS
--  Fecha Creacion: 12/01/2016
--   
"BSCL.Tesoreria.BD.Servicios::EdicionMultiple" (
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_IdUsuario 						BIGINT, 
    IN p_IdModulo 						NVARCHAR(3),
    IN p_UsaReglasNegocio 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.   
   
    -- Parametros de Entrada 
	IN p_FechaMovimiento				NVARCHAR(10),
	IN p_Entidad						NVARCHAR(300),
	IN p_LiquidaComprobante				NVARCHAR(1) DEFAULT '',
	IN p_FormasDePago					"BSCL.Tesoreria.BD.Tipos::TES.FormaDePago",
	IN p_RegistrosAProcesar 			TABLE ("Id" BIGINT, "Entidad" NVARCHAR(300), "Accion" NVARCHAR(3)),     
    
    --Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  		INT,
    OUT p_out_AdvertenciasContador 		INT,  
  	OUT p_out_ErroresContador      		INT,
  	OUT p_out_FormaDePagoComprobante	TABLE("IdComprobante" BIGINT, "IdComprobanteFormaDePago" BIGINT, "IdTipoMovimiento" NVARCHAR(6))
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA  CONDITION;	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		IF (NOT IS_EMPTY("BSCL.Tesoreria.BD::GBL.EdicionMultiple_Proceso_TablaCodigosMensajes")) THEN
			
			v_Mensajes = SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2" 
						   FROM "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Proceso_TablaCodigosMensajes";
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
			
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_InformacionDetalle"
			SELECT EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Proceso_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'OK';
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_AdvertenciasDetalle"
			SELECT EM."Codigo",TCM."Resultado" as "RequiereAutorizacion",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Proceso_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" IN ('ADVE','RAUT');
						
			INSERT INTO "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_ErroresDetalle"
			SELECT EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Proceso_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'RECH';
		END IF;
		
		SELECT count(1) into p_out_InformacionContador FROM "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_InformacionDetalle";
		SELECT count(1) into p_out_AdvertenciasContador FROM "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_AdvertenciasDetalle";
		--SELECT count(1) into p_out_ErroresContador FROM "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_ErroresDetalle";  --Se comenta para que no haga Rollback en la UI
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			INSERT INTO "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_ErroresDetalle" SELECT v_errorExt as "Codigo",::SQL_ERROR_MESSAGE as "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;										
		ELSE	
			RESIGNAL;
		END IF;
	END;
	
	p_out_InformacionContador		:= 0;
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_FormaDePagoComprobante	= SELECT TO_BIGINT(0) AS "IdComprobante", TO_BIGINT(0) AS "IdComprobanteFormaDePago", '' AS "IdTipoMovimiento" FROM DUMMY;
    

	v_RegistrosAProcesar =  
	SELECT  ROW_NUMBER() OVER (ORDER BY "Id","Entidad") AS "IdInterno",
			"Id",
			"Entidad",
			MAX(CASE WHEN "Accion" = 'BLO' THEN 'BLO' WHEN "Accion" = 'DES' THEN 'ING' END) AS "BLO_DES",
			MAX(CASE WHEN "Accion" = 'AGS' THEN 'S'   WHEN "Accion" = 'AGN' THEN 'N' END) AS "AGRUPA",
			MAX(CASE WHEN "Accion" = 'NTS' THEN 'S'   WHEN "Accion" = 'NTN' THEN 'N' END) AS "NETEA"
	FROM ( SELECT DISTINCT "Id","Entidad","Accion" FROM :p_RegistrosAProcesar)
	GROUP BY "Id","Entidad";	 
	--SELECT 'v_RegistrosAProcesar', * FROM :v_RegistrosAProcesar; -- REVISAR

	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN	
		DECLARE v_resultadoValidacion 	NVARCHAR(40);
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoValidacion);
		IF :v_resultadoValidacion = 'RECH' THEN
			INSERT INTO "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Proceso_TablaCodigosMensajes"
			SELECT 0 as "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2",'RECH' as "Resultado" FROM :v_Errores;				
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEdicionMultiple"(
			p_FechaMovimiento			=> :p_FechaMovimiento,
			p_RegistrosAProcesar		=> :v_RegistrosAProcesar,
			p_out_ResultadoRequisitos	=> v_resultadoRequisitos
		);
		
		v_RegistrosAProcesar =
		SELECT RP."IdInterno",RP."Id",RP."Entidad",RP."BLO_DES",RP."AGRUPA",RP."NETEA"
		  FROM :v_RegistrosAProcesar as RP
		 INNER JOIN :v_resultadoRequisitos as RR
		 		 ON RR."IdInterno" = RP."IdInterno"
		 WHERE RR."Resultado" = 'OK';
		
		
		INSERT INTO  "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Proceso_TablaCodigosMensajes"
		SELECT "IdInterno","Codigo","Resultado","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2"
		  FROM :v_resultadoRequisitos
		 WHERE "Resultado" <> 'OK';
		
		
		IF IS_EMPTY(:v_RegistrosAProcesar) THEN
	  		SIGNAL SALIDA_CONTROLADA;
	  	END IF;
	END; 
	END IF;
    
    
    --------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA;
		END;
		
		--**************************************************
		--INGRESO EGRESO
		--**************************************************
		

		UPDATE IE    
		   SET "IdEstado" 		  = CASE WHEN (RP."BLO_DES" is not null) 
		   								 THEN RP."BLO_DES" ELSE "IdEstado" 
		   							 	 END,
		   	   "IdUsuarioBloqueo" = CASE WHEN (RP."BLO_DES" is not null) 
		   	   							 THEN p_IdUsuario ELSE "IdUsuarioBloqueo" 
		   	   							 END,
		   	   "FechaHoraBloqueo" = CASE WHEN (RP."BLO_DES" is not null) 
		   	   							 THEN CURRENT_UTCTIMESTAMP ELSE "FechaHoraBloqueo" 
		   	   							 END,
			   "AgrupaComprobante"= CASE WHEN (RP."AGRUPA" is not null) 
			   							 THEN RP."AGRUPA" ELSE "AgrupaComprobante" 
			   							 END,
			   "NeteaComprobante" = CASE WHEN (RP."NETEA" is not null) 
			   							 THEN RP."NETEA" ELSE "NeteaComprobante" 
			   							 END
		  FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" as IE
		 INNER JOIN :v_RegistrosAProcesar as RP ON IE."IdIngresoEgreso" = RP."Id"
		 WHERE :p_Entidad = 'TES.IngresoEgreso';


		IF NOT IS_EMPTY(:p_FormasDePago) AND :p_Entidad = 'TES.IngresoEgreso' THEN
		
					
			v_FormaDePago = 
			SELECT  
					"BSCL.Tesoreria.BD.Secuencias::IngresoEgresoFormaDePago".NEXTVAL	AS "IdIngresoEgresoFormaDePago"
					,FormasDePago."NumeroReferencia"									AS "IdIngresoEgreso"
					,IE."IdTipoMovimiento"
					,FormasDePago."IdMedioDePago"
					,COALESCE(MedioDePago."Nombre",'')									AS "NombreMedioDePago"
					,FormasDePago."IdModalidadDePago"
					,COALESCE(ModalidadDePago."Nombre",'')								AS "NombreModalidadDePago"											
					,FormasDePago."IdCuentaBancariaCorredora"
					,COALESCE(CuentaBancariaCorredora."IdTipoCuentaBancaria",'') 	   	AS "IdTipoCuentaBancariaCorredora"
					,COALESCE(CuentaBancariaCorredora."NombreTipoCuentaBancaria",'')   	AS "NombreTipoCuentaBancariaCorredora"					
					,COALESCE(CuentaBancariaCorredora."NombreCuentaBancaria",'') 	   	AS "NombreCuentaBancariaCorredora"				
					,COALESCE(CuentaBancariaCorredora."NumeroCuentaBancaria",'') 	   	AS "NumeroCuentaBancariaCorredora"
					,FormasDePago."IdTarjetaBancariaCorredora"
					,COALESCE(TarjetaBancariaCorredora."IdTipoTarjetaBancaria",'') 	   	AS "IdTipoTarjetaBancariaCorredora"
					,COALESCE(TarjetaBancariaCorredora."NombreTipoTarjetaBancaria",'') 	AS "NombreTipoTarjetaBancariaCorredora"
					,COALESCE(TarjetaBancariaCorredora."NumeroTarjetaBancaria",'') 	   	AS "NumeroTarjetaBancariaCorredora"					
					,FormasDePago."IdChequera"
					,FormasDePago."NumeroDocumentoCorredora"
					,FormasDePago."FechaDocumentoPagoCorredora"
					-----------------------------------------------------------------------------------------------------------
					,COALESCE(FormasDePago."IdCuentaBancariaSocio",FPA."IdCuentaBancariaSocio",0) 					AS "IdCuentaBancariaSocio"
					,COALESCE(FormasDePago."IdBancoSocio",FPA."IdBancoSocio",0)										AS "IdBancoSocio"
					,COALESCE(FormasDePago."IdTipoCuentaBancariaSocio",FPA."IdTipoCuentaBancariaSocio",'')			AS "IdTipoCuentaBancariaSocio"
					,COALESCE(FormasDePago."NombreTipoCuentaBancariaSocio",FPA."NombreTipoCuentaBancariaSocio",'')	AS "NombreTipoCuentaBancariaSocio"					
					,COALESCE(FormasDePago."NombreCuentaBancariaSocio",FPA."NombreCuentaBancariaSocio",'') 	    	AS "NombreCuentaBancariaSocio"
					,COALESCE(FormasDePago."NumeroCuentaBancariaSocio",FPA."NumeroCuentaBancariaSocio",'') 			AS "NumeroCuentaBancariaSocio"				
					,COALESCE(FormasDePago."IdTarjetaBancariaSocio",FPA."IdTarjetaBancariaSocio",0)					AS "IdTarjetaBancariaSocio"
					,COALESCE(FormasDePago."IdTipoTarjetaBancariaSocio",FPA."IdTipoTarjetaBancariaSocio",'') 		AS "IdTipoTarjetaBancariaSocio"
					,COALESCE(FormasDePago."NombreTipoTarjetaBancariaSocio",FPA."NombreTipoTarjetaBancariaSocio",'')AS "NombreTipoTarjetaBancariaSocio"
					,COALESCE(FormasDePago."NumeroTarjetaBancariaSocio",FPA."NumeroTarjetaBancariaSocio",'') 		AS "NumeroTarjetaBancariaSocio"					
					,COALESCE(FormasDePago."NumeroSerieSocio",FPA."NumeroSerieSocio",'')							AS "NumeroSerieSocio"
					,COALESCE(FormasDePago."NumeroDocumentoSocio",FPA."NumeroDocumentoSocio",0)						AS "NumeroDocumentoSocio"
					,COALESCE(FormasDePago."FechaDocumentoPagoSocio",FPA."FechaDocumentoPagoSocio",'')				AS "FechaDocumentoPagoSocio"
					-------------------------------------------------------------------------------------------------------------					
				    ,FormasDePago."IdMoneda"
					,COALESCE(FPA."Monto",FormasDePago."Monto") As "Monto"
				    ,CURRENT_UTCTIMESTAMP	AS "FechaIngresoRegistro"
				    ,'ING'					AS "IdEstado"
			  FROM :p_FormasDePago AS FormasDePago
			 INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
			 		 ON IE."IdIngresoEgreso" = FormasDePago."NumeroReferencia"
			  LEFT JOIN (SELECT   CFP."IdIngresoEgreso"
								 ,CFP."Monto"
								 ,CFP."IdCuentaBancariaSocio"
								 ,CFP."IdBancoSocio"
								 ,CFP."IdTipoCuentaBancariaSocio"
								 ,CFP."NombreTipoCuentaBancariaSocio"
								 ,CFP."NombreCuentaBancariaSocio"
								 ,CFP."NumeroCuentaBancariaSocio"
								 ,CFP."IdTarjetaBancariaSocio"
								 ,CFP."IdTipoTarjetaBancariaSocio"
								 ,CFP."NombreTipoTarjetaBancariaSocio"
								 ,CFP."NumeroTarjetaBancariaSocio"
								 ,CFP."NumeroSerieSocio"
								 ,CFP."NumeroDocumentoSocio"
								 ,CFP."FechaDocumentoPagoSocio"
						  FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" CFP
						 INNER JOIN :p_FormasDePago AS CP ON (CFP."IdIngresoEgreso" = CP."NumeroReferencia")
						 WHERE CFP."IdEstado" = 'ING'
			  			) AS FPA
			  		 ON FPA."IdIngresoEgreso" = IE."IdIngresoEgreso"
			  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarMedioDePago" AS MedioDePago 
			  		 ON MedioDePago."IdMedioDePago" = FormasDePago."IdMedioDePago"
			  LEFT JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS ModalidadDePago 
			  		 ON ModalidadDePago."IdModalidadDePago" = FormasDePago."IdModalidadDePago"
			  LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" AS CuentaBancariaCorredora 
			  		 ON CuentaBancariaCorredora."IdCuentaBanco" = FormasDePago."IdCuentaBancariaCorredora"
			  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" AS TarjetaBancariaCorredora 
			  		 ON TarjetaBancariaCorredora."IdTarjetaBancaria" = FormasDePago."IdTarjetaBancariaCorredora";
	

		    UPDATE CFP
			SET "IdEstado" = 'MOD'
			FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" CFP
			INNER JOIN :p_FormasDePago AS CP ON (CFP."IdIngresoEgreso" = CP."NumeroReferencia");

						
			INSERT INTO  "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"(		
					  "IdIngresoEgresoFormaDePago"
					 ,"IdIngresoEgreso"
					 ,"IdTipoMovimiento"
					 ,"IdMedioDePago"
					 ,"NombreMedioDePago"
					 ,"IdModalidadDePago"
					 ,"NombreModalidadDePago"
					 				 
					 ,"IdCuentaBancariaCorredora"
					 ,"IdTipoCuentaBancariaCorredora"
					 ,"NombreTipoCuentaBancariaCorredora"
					 ,"NombreCuentaBancariaCorredora"
					 ,"NumeroCuentaBancariaCorredora"
					 ,"IdTarjetaBancariaCorredora"
					 ,"IdTipoTarjetaBancariaCorredora"
					 ,"NombreTipoTarjetaBancariaCorredora"
					 ,"NumeroTarjetaBancariaCorredora"
					 ,"IdChequera"
					 ,"NumeroDocumentoCorredora"
					 ,"FechaDocumentoPagoCorredora"
					
					 ,"IdCuentaBancariaSocio"
					 ,"IdBancoSocio"
					 ,"IdTipoCuentaBancariaSocio"
					 ,"NombreTipoCuentaBancariaSocio"
					 ,"NombreCuentaBancariaSocio"
					 ,"NumeroCuentaBancariaSocio"
					 ,"IdTarjetaBancariaSocio"
					 ,"IdTipoTarjetaBancariaSocio"
					 ,"NombreTipoTarjetaBancariaSocio"
					 ,"NumeroTarjetaBancariaSocio"
					 ,"NumeroSerieSocio"
					 ,"NumeroDocumentoSocio"
					 ,"FechaDocumentoPagoSocio"
					
					 ,"IdMoneda"
					 ,"Monto"
					 ,"FechaIngresoRegistro"
					 ,"IdEstado"
			)
			SELECT "IdIngresoEgresoFormaDePago","IdIngresoEgreso","IdTipoMovimiento","IdMedioDePago","NombreMedioDePago","IdModalidadDePago"
					,"NombreModalidadDePago","IdCuentaBancariaCorredora","IdTipoCuentaBancariaCorredora","NombreTipoCuentaBancariaCorredora"
					,"NombreCuentaBancariaCorredora","NumeroCuentaBancariaCorredora","IdTarjetaBancariaCorredora","IdTipoTarjetaBancariaCorredora"
					,"NombreTipoTarjetaBancariaCorredora","NumeroTarjetaBancariaCorredora","IdChequera","NumeroDocumentoCorredora"
					,"FechaDocumentoPagoCorredora","IdCuentaBancariaSocio","IdBancoSocio","IdTipoCuentaBancariaSocio","NombreTipoCuentaBancariaSocio"
					,"NombreCuentaBancariaSocio","NumeroCuentaBancariaSocio","IdTarjetaBancariaSocio","IdTipoTarjetaBancariaSocio"
					,"NombreTipoTarjetaBancariaSocio","NumeroTarjetaBancariaSocio","NumeroSerieSocio","NumeroDocumentoSocio","FechaDocumentoPagoSocio"
					,"IdMoneda",SUM("Monto") as "Monto","FechaIngresoRegistro","IdEstado"
			  FROM :v_FormaDePago
			 GROUP BY "IdIngresoEgresoFormaDePago","IdIngresoEgreso","IdTipoMovimiento","IdMedioDePago","NombreMedioDePago","IdModalidadDePago"
					,"NombreModalidadDePago","IdCuentaBancariaCorredora","IdTipoCuentaBancariaCorredora","NombreTipoCuentaBancariaCorredora"
					,"NombreCuentaBancariaCorredora","NumeroCuentaBancariaCorredora","IdTarjetaBancariaCorredora","IdTipoTarjetaBancariaCorredora"
					,"NombreTipoTarjetaBancariaCorredora","NumeroTarjetaBancariaCorredora","IdChequera","NumeroDocumentoCorredora"
					,"FechaDocumentoPagoCorredora","IdCuentaBancariaSocio","IdBancoSocio","IdTipoCuentaBancariaSocio","NombreTipoCuentaBancariaSocio"
					,"NombreCuentaBancariaSocio","NumeroCuentaBancariaSocio","IdTarjetaBancariaSocio","IdTipoTarjetaBancariaSocio"
					,"NombreTipoTarjetaBancariaSocio","NumeroTarjetaBancariaSocio","NumeroSerieSocio","NumeroDocumentoSocio","FechaDocumentoPagoSocio"
					,"IdMoneda","FechaIngresoRegistro","IdEstado";
			  		 
			--3) Actualiza chequera	
			UPDATE C
			   SET	C."NumeroChequeActual"	= (RP."ChequeHasta" + 1),
			   	   	C."Remanente"			= (C."Remanente" - RP."CantidadCheques")
			  FROM "BSCL.Tesoreria.BD::TES.Chequera" as C
			 INNER JOIN ( SELECT DISTINCT 
								 FP."IdChequera",
								 MIN(COALESCE(FP."NumeroDocumentoCorredora",0)) as "ChequeDesde",
								 MAX(COALESCE(FP."NumeroDocumentoCorredora",0)) as "ChequeHasta",
								 COALESCE(C."NumeroChequeActual",0) as "NumeroChequeActual",
								 (MAX(FP."NumeroDocumentoCorredora") - MIN(FP."NumeroDocumentoCorredora") + 1) as "CantidadCheques"
						   FROM :p_FormasDePago AS FP
						  INNER JOIN "BSCL.Tesoreria.BD::TES.Chequera" as C ON C."IdChequera" = FP."IdChequera"
						  GROUP BY FP."IdChequera",C."NumeroChequeActual"
			 			) as RP 
			 		 ON RP."IdChequera" = C."IdChequera"
			 WHERE RP."IdChequera" > 0
			   AND "ChequeDesde" > 0
			   AND "ChequeHasta" > 0;
			
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Proceso_TablaCodigosMensajes"
			SELECT  DISTINCT
					"Id" AS "IdInterno"
				   ,'UI_MOVIMIENTO_EDITADO' as "Codigo"
				   ,'Id' as "CodigoDato1"
				   ,CAST("Id" as NVARCHAR(520)) as "ValorDato1"
				   ,'' as "CodigoDato2"
				   ,'' as "ValorDato2" 
				   ,'OK' as "Resultado"
			  FROM :v_RegistrosAProcesar
			 WHERE "Entidad" = 'TES.IngresoEgreso'
			   AND ("AGRUPA" is not null OR "NETEA" is not null);
		END IF;
		
		
		
		--**************************************************
		--COMPROBANTES
		--**************************************************
		UPDATE C    
		   SET "IdEstado" 		  = CASE WHEN (RP."BLO_DES" is not null) 
		   								 THEN RP."BLO_DES" ELSE "IdEstado" 
		   							 	 END,
		   	   "IdUsuarioBloqueo" = CASE WHEN (RP."BLO_DES" is not null) 
		   	   							 THEN p_IdUsuario ELSE "IdUsuarioBloqueo" 
		   	   							 END,
		   	   "FechaHoraBloqueo" = CASE WHEN (RP."BLO_DES" is not null) 
		   	   							 THEN CURRENT_UTCTIMESTAMP ELSE "FechaHoraBloqueo" 
		   	   							 END
		  FROM "BSCL.Tesoreria.BD::TES.Comprobante" C
		 INNER JOIN  :v_RegistrosAProcesar as RP ON C."IdComprobante" = RP."Id"
		 WHERE :p_Entidad = 'TES.Comprobante';


		IF NOT IS_EMPTY(:p_FormasDePago) AND :p_Entidad = 'TES.Comprobante' THEN
			v_formasDePagoActualComp =
			SELECT    CFP."IdComprobante"
					 ,CFP."Monto"
					 ,CFP."IdCuentaBancariaSocio"
					 ,CFP."IdBancoSocio"
					 ,CFP."IdTipoCuentaBancariaSocio"
					 ,CFP."NombreTipoCuentaBancariaSocio"
					 ,CFP."NombreCuentaBancariaSocio"
					 ,CFP."NumeroCuentaBancariaSocio"
					 ,CFP."IdTarjetaBancariaSocio"
					 ,CFP."IdTipoTarjetaBancariaSocio"
					 ,CFP."NombreTipoTarjetaBancariaSocio"
					 ,CFP."NumeroTarjetaBancariaSocio" 
					 ,CFP."NumeroSerieSocio"
					 ,CFP."NumeroDocumentoSocio"
					 ,CFP."FechaDocumentoPagoSocio"
			  FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
			 INNER JOIN :p_FormasDePago AS CP ON (CFP."IdComprobante" = CP."NumeroReferencia")
			 WHERE CFP."IdEstado" = 'ING';
		
			v_FormasDePago = 
			SELECT	"IdComprobante","IdComprobanteFormaDePago","IdTipoMovimiento","IdMedioDePago","IdModalidadDePago","IdMoneda"
				   ,SUM("Monto") as "Monto","IdCuentaBancariaCorredora","IdTarjetaBancariaCorredora","IdChequera","NumeroDocumentoCorredora"
				   ,"FechaDocumentoPagoCorredora","IdCuentaBancariaSocio","IdBancoSocio","NombreCuentaBancariaSocio","NumeroCuentaBancariaSocio"
				   ,"IdTipoCuentaBancariaSocio","NombreTipoCuentaBancariaSocio","IdTarjetaBancariaSocio","IdTipoTarjetaBancariaSocio"
				   ,"NombreTipoTarjetaBancariaSocio","NumeroTarjetaBancariaSocio","NumeroSerieSocio","NumeroDocumentoSocio","FechaDocumentoPagoSocio"
				   ,"CertificadoAutomatico"
			  FROM (
			  			SELECT FP."NumeroReferencia" 				AS "IdComprobante"
							  ,0 as "IdComprobanteFormaDePago"
							  ,COM."IdTipoMovimiento"
							  ,FP."IdMedioDePago"
						 	  ,FP."IdModalidadDePago"
						 	  ,FP."IdMoneda"
							  ,COALESCE(FPA."Monto",FP."Monto")		AS "Monto"
							  ,FP."IdCuentaBancariaCorredora"
							  ,FP."IdTarjetaBancariaCorredora"
							  ,FP."IdChequera"
							  ,FP."NumeroDocumentoCorredora"
							  ,FP."FechaDocumentoPagoCorredora"
							  
							  -------------------------------------------------------------------------
							  ,COALESCE(FP."IdCuentaBancariaSocio",FPA."IdCuentaBancariaSocio",0)					AS "IdCuentaBancariaSocio"
							  ,COALESCE(FP."IdBancoSocio",FPA."IdBancoSocio",0)										AS "IdBancoSocio"
							  ,COALESCE(FP."NombreCuentaBancariaSocio",FPA."NombreCuentaBancariaSocio",'')			AS "NombreCuentaBancariaSocio"
							  ,COALESCE(FP."NumeroCuentaBancariaSocio",FPA."NumeroCuentaBancariaSocio",'')			AS "NumeroCuentaBancariaSocio"
							  ,COALESCE(FP."IdTipoCuentaBancariaSocio",FPA."IdTipoCuentaBancariaSocio",'') 			AS "IdTipoCuentaBancariaSocio"
							  ,COALESCE(FP."NombreTipoCuentaBancariaSocio",FPA."NombreTipoCuentaBancariaSocio",'')	AS "NombreTipoCuentaBancariaSocio"
							  ,COALESCE(FP."IdTarjetaBancariaSocio",FPA."IdTarjetaBancariaSocio",0)					AS "IdTarjetaBancariaSocio"
							  ,COALESCE(FP."IdTipoTarjetaBancariaSocio",FPA."IdTipoTarjetaBancariaSocio",'')		AS "IdTipoTarjetaBancariaSocio"
							  ,COALESCE(FP."NombreTipoTarjetaBancariaSocio",FPA."NombreTipoTarjetaBancariaSocio",'')AS "NombreTipoTarjetaBancariaSocio"
							  ,COALESCE(FP."NumeroTarjetaBancariaSocio",FPA."NumeroTarjetaBancariaSocio",'')		AS "NumeroTarjetaBancariaSocio"
							  ,COALESCE(FP."NumeroSerieSocio",FPA."NumeroSerieSocio",'')							AS "NumeroSerieSocio"
							  ,COALESCE(FP."NumeroDocumentoSocio",FPA."NumeroDocumentoSocio",0)						AS "NumeroDocumentoSocio"
							  ,COALESCE(FP."FechaDocumentoPagoSocio",FPA."FechaDocumentoPagoSocio",'')				AS "FechaDocumentoPagoSocio"
							  -------------------------------------------------------------------------
							  ,'N' as "CertificadoAutomatico"
						  FROM :p_FormasDePago as FP
						 INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM 
						 		 ON COM."IdComprobante" = FP."NumeroReferencia"
						  LEFT JOIN :v_formasDePagoActualComp as FPA
						  		 ON FPA."IdComprobante" = FP."NumeroReferencia"
			  	   )
			 GROUP BY "IdComprobante","IdComprobanteFormaDePago","IdTipoMovimiento","IdMedioDePago","IdModalidadDePago","IdMoneda"
				   ,"IdCuentaBancariaCorredora","IdTarjetaBancariaCorredora","IdChequera","NumeroDocumentoCorredora","FechaDocumentoPagoCorredora"
				   ,"IdCuentaBancariaSocio","IdBancoSocio","NombreCuentaBancariaSocio","NumeroCuentaBancariaSocio","IdTipoCuentaBancariaSocio"
				   ,"NombreTipoCuentaBancariaSocio","IdTarjetaBancariaSocio","IdTipoTarjetaBancariaSocio","NombreTipoTarjetaBancariaSocio"
				   ,"NumeroTarjetaBancariaSocio","NumeroSerieSocio","NumeroDocumentoSocio","FechaDocumentoPagoSocio","CertificadoAutomatico";
					
			CALL "BSCL.Tesoreria.BD.Servicios::GestionFormaDePago"(
			   p_IdEmpresa					=> :p_IdEmpresa
			   ,p_IdUsuario					=> :p_IdUsuario
			   ,p_IdModulo					=> :p_IdModulo
			   ,p_simularOperacion			=> 'N'
			   ,p_UsaReglasNegocio			=> 'S'
			   ,p_FechaMovimiento			=> :p_FechaMovimiento
			   ,p_TipoEjecucion				=> 'ING'
			   ,p_FormasDePago				=> :v_FormasDePago
			   ,p_out_InformacionContador	=> p_out_InformacionContador
			   ,p_out_InformacionDetalle	=> p_out_InformacionDetalle
			   ,p_out_AdvertenciasContador	=> p_out_AdvertenciasContador
			   ,p_out_AdvertenciasDetalle	=> p_out_AdvertenciasDetalle
			   ,p_out_ErroresContador		=> p_out_ErroresContador
			   ,p_out_ErroresDetalle		=> p_out_ErroresDetalle
			);			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_InformacionDetalle" SELECT "Codigo","Descripcion" FROM :p_out_InformacionDetalle;
			INSERT INTO "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_ErroresDetalle" SELECT "Codigo","Descripcion" FROM :p_out_ErroresDetalle;
			
			p_out_FormaDePagoComprobante = SELECT CFP."IdComprobante", CFP."IdComprobanteFormaDePago", FP."IdTipoMovimiento" 
				FROM :v_FormasDePago FP
				INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP ON CFP."IdComprobante" = FP."IdComprobante"
				WHERE CFP."IdEstado" = 'ING';
			
			IF :p_LiquidaComprobante = 'S' AND IS_EMPTY(:p_out_ErroresDetalle) AND IS_EMPTY(:p_out_AdvertenciasDetalle) THEN
				v_TablaComprobantes =
				SELECT "IdComprobante", "IdTipoMovimiento",'CLI' as "AfectaCuentaInversion" FROM :v_FormasDePago;  --'CLI': Solo afecta Cta. Inv. del cliente				
				--SELECT 'v_TablaComprobantes', * FROM :v_TablaComprobantes;  -- REVISAR
				
				v_TablaComprobanteFormaDePago = 
				SELECT CFP."IdComprobante", CFP."IdComprobanteFormaDePago"
				FROM :p_out_FormaDePagoComprobante AS CFP;
				
				--SELECT 'v_TablaComprobanteFormaDePago', * FROM :v_TablaComprobanteFormaDePago;  -- REVISAR
				 
				CALL "BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes" (
					p_IdEmpresa						=> :p_IdEmpresa
				   ,p_IdUsuario						=> :p_IdUsuario
				   ,p_IdModulo						=> :p_IdModulo
				   ,p_simularOperacion				=> 'N'
				   ,p_UsaReglasNegocio				=> 'S'
				   ,p_FechaMovimiento				=> :p_FechaMovimiento
				   ,p_TablaComprobante				=> :v_TablaComprobantes
				   ,p_TablaComprobanteFormaDePago	=> :v_TablaComprobanteFormaDePago
				   ,p_out_InformacionContador		=> p_out_InformacionContador
				   ,p_out_AdvertenciasContador		=> p_out_AdvertenciasContador
				   ,p_out_ErroresContador			=> p_out_ErroresContador
				);
				
				INSERT INTO "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_InformacionDetalle" 
				SELECT "Codigo","Descripcion" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_InformacionDetalle";
				INSERT INTO "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_ErroresDetalle" 
				SELECT "Codigo","Descripcion" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ErroresDetalle";
			END IF;
		END IF;
		
		
		INSERT INTO "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Proceso_TablaCodigosMensajes"
		SELECT  DISTINCT
				"Id" AS "IdInterno"
			   ,CASE WHEN "Entidad" = 'TES.IngresoEgreso' AND ("BLO_DES" is not null) AND ("BLO_DES" = 'BLO') THEN 'UI_MOVIMIENTO_BLOQUEADO'
			   		 WHEN "Entidad" = 'TES.IngresoEgreso' AND ("BLO_DES" is not null) AND ("BLO_DES" = 'ING') THEN 'UI_MOVIMIENTO_DESBLOQUEADO' 
			   		 WHEN "Entidad" = 'TES.Comprobante' AND ("BLO_DES" is not null) AND ("BLO_DES" = 'BLO') THEN 'UI_COMPROBANTE_BLOQUEADO'
			   		 WHEN "Entidad" = 'TES.Comprobante' AND ("BLO_DES" is not null) AND ("BLO_DES" = 'ING') THEN 'UI_COMPROBANTE_DESBLOQUEADO'			   		 
			   	 END as "Codigo"
			   ,'Id' as "CodigoDato1"
			   ,CAST("Id" as NVARCHAR(520)) as "ValorDato1"
			   ,'' as "CodigoDato2"
			   ,'' as "ValorDato2" 
			   ,'OK' as "Resultado"
		  FROM :v_RegistrosAProcesar;
		
		SIGNAL SALIDA_CONTROLADA;
	END;
END;
