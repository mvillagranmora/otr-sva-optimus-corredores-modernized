SET SCHEMA "BSCL";  
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.DEP::CrearDeposito";

CREATE PROCEDURE 
-- 
--  Proposito: Crear el registro de los documentos o dinero recibidos y que deben ser  
--				depositados en una cuenta de la corredora
--  Autor: NRM - BCS
--  Fecha Creacion: 28/03/2016
--   
"BSCL.Tesoreria.BD.Servicios.DEP::CrearDeposito" (
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_IdUsuario 						BIGINT, 
    IN p_IdModulo 						NVARCHAR(3), 
   
   	IN p_simularOperacion 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
    IN p_UsaReglasNegocio 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.   
   
     -- Parametros de Proceso 
	IN p_FechaMovimiento				NVARCHAR(10), 
    IN p_DocumentosADepositar 			TABLE("NumeroDeposito" 				BIGINT,
    										  "IdCuentaBancariaCorredora"	BIGINT, 
    										  "IdComprobanteFormaDePago" 	BIGINT), --Documentos a depositar
   
    -- Salidas Tesoreria
	OUT p_out_IdMoneda					TABLE("NumeroDeposito" BIGINT, "IdMoneda" NVARCHAR(3)) DEFAULT EMPTY,
          
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
	DECLARE v_TablaCodigosMensajes			TABLE("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520))
											DEFAULT SELECT 0  as "IdInterno",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY WHERE 1=2;
	
				        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
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
	p_out_IdMoneda					= SELECT 0  as "NumeroDeposito", 'CLP' as "IdMoneda" FROM dummy WHERE 1=2;
	
	--------------------
	--OBTENCION DE DATA
	--------------------
	v_DocumentosADepositar = 
	SELECT 	 DAD."NumeroDeposito"
			,:p_simularOperacion AS "SimularOperacion"
			,DAD."IdCuentaBancariaCorredora" AS "IdCuentaBancariaCorredoraInformado"
			,DAD."IdComprobanteFormaDePago" AS "IdComprobanteFormaDePagoInformado"
			
			,CBC."IdCuentaBanco"
			,CBC."IdBanco"
			,CBC."NombreCuentaBancaria"
			,CBC."NumeroCuentaBancaria"			
			,CBC."IdTipoCuentaBancaria"
			,TCBC."NombreTipoCuentaBancaria"		
			,CBC."IdMoneda" AS "IdMonedaCuentaBanco"
			,CBC."IdEstado" AS "IdEstadoCuentaBanco"
			
			,CFP."IdComprobanteFormaDePago"
			,CFP."IdCuentaBancariaSocio"
			,CFP."IdBancoSocio"
			,CFP."NumeroSerieSocio"
			,CFP."NumeroDocumentoSocio"
			,CFP."FechaDocumentoPagoSocio"
			,CFP."IdMoneda" AS "IdMonedaFormaDePago"
			,CFP."Monto" AS "MontoFormaDePago"
			,CFP."IdEstado" AS "IdEstadoFormaDePago"			
			
			,CFG."IdTipoMovimiento"
	 		,CFG."IdMedioDePago"
	 		,CFG."TipoMedioDePago"				
	 		,CFG."EstadosPosibleTipoMovimientoMedioDePago"
	 		
	 		,P."IdPlaza"
	 		,P."PlazoRetencionMismoBanco"
	 		,P."PlazoRetencionOtroBanco"
	 		
	 		,CASE WHEN CFG."IdMedioDePago" = 'EFE' THEN 'EFTIVO'
	 			  ELSE CASE WHEN CFP."IdBancoSocio" = CBC."IdBanco" THEN 'MIBNCO' ELSE 'OTBNCO' END
	 		  END "IdTipoDeposito"
			
	  FROM :p_DocumentosADepositar as DAD
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" as CBC ON CBC."IdCuentaBanco" = DAD."IdCuentaBancariaCorredora"
	  LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" as TCBC ON TCBC."IdTipoCuentaBancaria" = CBC."IdTipoCuentaBancaria"
	  --LEFT JOIN "BSCL.Plataforma.BD::BAN.PlazaCiudad" AS PC ON PC."IdCiudad" = CBC."IdCiudad"
	  LEFT JOIN "BSCL.Plataforma.BD::BAN.Plaza" AS P ON P."IdPlaza" = CBC."IdPlaza"	  
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = DAD."IdComprobanteFormaDePago"
	  LEFT JOIN (SELECT  TMMP."IdTipoMovimiento"
	 					,TMMP."IdMedioDePago"
	 					,MP."TipoMedioDePago"				
	 					,TMMP."EstadosPosibleTipoMovimientoMedioDePago" 					
	 			   FROM "BSCL.Plataforma.BD::PAG.MedioDePago" as MP
	 			  INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" as TMMP ON (TMMP."IdMedioDePago" = MP."IdMedioDePago")
	 			) AS CFG ON CFG."IdTipoMovimiento" = CFP."IdTipoMovimiento" AND CFG."IdMedioDePago" = CFP."IdMedioDePago";
	
	
	p_out_IdMoneda = 
	SELECT "NumeroDeposito"
		   ,"IdMonedaFormaDePago" AS "IdMoneda"
	  FROM :v_DocumentosADepositar
	 GROUP BY "NumeroDeposito","IdMonedaFormaDePago";
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoRegla	NVARCHAR(40);
    
		---Validaciones datos de entrada
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA_NO_DATA; 	
		END IF;	
			
		
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearDeposito"(
			p_DocumentosADepositar	=> :v_DocumentosADepositar,
			p_out_Resultado			=>	v_Resultado
		);
			
		--Se eliminan los registros rechazados
		v_DocumentosADepositar = 
		 SELECT  DaD.*
		   FROM :v_DocumentosADepositar as DaD
		  INNER JOIN (SELECT DISTINCT "NumeroDeposito" FROM :v_Resultado WHERE "Resultado" = 'OK'
					  EXCEPT 
					  SELECT DISTINCT "NumeroDeposito" FROM :v_Resultado WHERE "Resultado" = 'RECH') as R
				  ON R."NumeroDeposito" = DaD."NumeroDeposito";
		
		
		select 'v_Resultado', * from :v_Resultado;
		select 'v_DocumentosADepositar', * from :v_DocumentosADepositar;		
		SELECT 	"BSCL.Tesoreria.BD.Secuencias::Deposito".NEXTVAL FROM dummy;
		
		--Se Consulta diccionario
		v_TablaCodigosMensajes =
		SELECT DISTINCT "NumeroDeposito" as "IdInterno","Codigo",'Id' as "CodigoDato1",CAST("NumeroDeposito" as NVARCHAR(520)) as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
		  FROM :v_Resultado
		 WHERE "Resultado" <> 'OK';
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);
		
		
		p_out_ErroresDetalle =  SELECT E."Codigo",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."NumeroDeposito" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'RECH';
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
		
		p_out_AdvertenciasDetalle =  SELECT E."Codigo",'' as "RequiereAutorizacion",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."NumeroDeposito" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'ADVE';
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
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_TablaCodigosMensajes = 
			SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END;

				
		--************************
		--* Ingreso de depósitos *
		--************************
		v_SecuenciaDepositos = 
		SELECT 	CDD."NumeroDeposito"
			   ,"BSCL.Tesoreria.BD.Secuencias::Deposito".NEXTVAL as "IdDeposito"
		  FROM :v_DocumentosADepositar as CDD
		 GROUP BY CDD."NumeroDeposito";

		
		INSERT INTO "BSCL.Tesoreria.BD::TES.Deposito" (
			 "IdDeposito"
			,"IdEmpresa"
			,"FechaMovimiento"
			,"FechaIngresoRegistro"
			,"IdTipoDeposito"
			,"IdTipoCuentaBanco"
			,"IdCuentaBanco"
			,"IdMoneda"
			,"Monto"
			,"IdUsuario"
			,"IdEstado"
		)
		SELECT   SD."IdDeposito"
				,:p_IdEmpresa
				,:p_FechaMovimiento
				,CURRENT_UTCTIMESTAMP
				,CASE WHEN COUNT(DISTINCT CBC."IdTipoDeposito") > 1 THEN 'MZTPDP' ELSE MAX(CBC."IdTipoDeposito") END 				
				,CBC."IdTipoCuentaBancaria"
				,CBC."IdCuentaBanco"
				,CBC."IdMonedaCuentaBanco"
				,SUM(CBC."MontoFormaDePago")
   				,:p_IdUsuario
   				,'ING'
   		  FROM :v_DocumentosADepositar as CBC
   		 INNER JOIN :v_SecuenciaDepositos as SD ON SD."NumeroDeposito" = CBC."NumeroDeposito"
   		 GROUP BY SD."NumeroDeposito",SD."IdDeposito",CBC."IdTipoCuentaBancaria",CBC."IdCuentaBanco",CBC."IdMonedaCuentaBanco";
		
		
		INSERT INTO "BSCL.Tesoreria.BD::TES.DepositoDetalle"(
			 "IdDepositoDetalle"
			,"IdDeposito"
			,"IdMedioDePago"
			,"IdTipoDeposito"			
			,"IdComprobanteFormaDePago"
			,"IdEstado"
		)
		SELECT 	"BSCL.Tesoreria.BD.Secuencias::DepositoDetalle".NEXTVAL
				,SD."IdDeposito"
				,CBC."IdMedioDePago"
				,CBC."IdTipoDeposito"
				,CBC."IdComprobanteFormaDePago"
				,'ING'
		  FROM :v_DocumentosADepositar as CBC
   		 INNER JOIN :v_SecuenciaDepositos as SD ON SD."NumeroDeposito" = CBC."NumeroDeposito";
		
		
		--********************************************
		--* CAMBIO EN FORMAS DE PAGO DEL COMPROBANTE *
		--********************************************
		UPDATE CFP    
		   SET   "IdCuentaBancariaCorredora"		= CBC."IdCuentaBanco"
				,"NombreCuentaBancariaCorredora"	= CBC."NombreCuentaBancaria"
				,"NumeroCuentaBancariaCorredora"	= CBC."NumeroCuentaBancaria"
				,"IdTipoCuentaBancariaCorredora"	= CBC."IdTipoCuentaBancaria"
				,"NombreTipoCuentaBancariaCorredora"= CBC."NombreTipoCuentaBancaria"		   		
		   		,"IdDeposito"						= SD."IdDeposito"
		   		,"Depositado"						= 'N'
		   		,"IdPlaza"							= CBC."IdPlaza"
		   		,"DiasRetencion"					= (CASE 
		   												  WHEN (TMMP."AfectoADiasRetencion" = 'N')  THEN 0	
		   												  WHEN (TMMP."AfectoADiasRetencion" = 'S' AND CFP."IdBancoSocio" = CBC."IdBanco") THEN CBC."PlazoRetencionMismoBanco" 
		   												  WHEN (TMMP."AfectoADiasRetencion" = 'S' AND CFP."IdBancoSocio" <> CBC."IdBanco") THEN CBC."PlazoRetencionOtroBanco" 
		   												  ELSE 0
		   											  END)							  							  
		   		,"IdEstado" 						= CASE WHEN CFP."IdMedioDePago" <> 'EFE' THEN 'NDP' ELSE "IdEstado" END
		  FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP
		 INNER JOIN :v_DocumentosADepositar as CBC ON CBC."IdComprobanteFormaDePago" = CFP."IdComprobanteFormaDePago"
   		 INNER JOIN :v_SecuenciaDepositos as SD ON SD."NumeroDeposito" = CBC."NumeroDeposito"   		 
		 INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" AS TMMP 
				 	 ON TMMP."IdTipoMovimiento" = CFP."IdTipoMovimiento"
			    	AND TMMP."IdMedioDePago" 	= CFP."IdMedioDePago";
   		 
		-- Actualiza fecha afecta saldo (cuando no lo tiene. Ej: efectivo)
		UPDATE CFP    
		SET   
			"FechaAfectaSaldo"		= CASE WHEN instr(TMMP."AfectaSaldoEnEstado",'LIQ') > 0 
											AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0 
				    					    AND COALESCE(CFP."FechaAfectaSaldo",'0001-01-01') = '0001-01-01' 
				    				     THEN ADD_DAYS(TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD'), CFP."DiasRetencion") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
				    				     ELSE CFP."FechaAfectaSaldo"
					    			  END
		FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP
		INNER JOIN :v_DocumentosADepositar as CBC ON CBC."IdComprobanteFormaDePago" = CFP."IdComprobanteFormaDePago"
		INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" AS TMMP 
				 	 ON TMMP."IdTipoMovimiento" = CFP."IdTipoMovimiento"
			    	AND TMMP."IdMedioDePago" 	= CFP."IdMedioDePago";


		--********************************
		--* Resultado del proceso *
		--********************************
		v_TablaCodigosMensajes =
		SELECT  D."IdDeposito" AS "IdInterno"
			   ,'CREADA_NOMINA_DEPOSITO' as "Codigo"
			   ,'IdDeposito' as "CodigoDato1"
			   ,CAST(D."IdDeposito" as NVARCHAR(520)) as "ValorDato1"
			   ,'' as "CodigoDato2"
			   ,'' as "ValorDato2" 
		  FROM "BSCL.Tesoreria.BD::TES.Deposito" as D
		 INNER JOIN :v_SecuenciaDepositos as SD ON SD."IdDeposito" = D."IdDeposito";
				 
		SIGNAL SALIDA_EXITOSA;
  	END;    
END;

  