SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CrearEventoCapital";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CrearEventoCapital" (

--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapital					BIGINT     		default 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	default '',
	IN p_Secuencia							BIGINT 			default 0,
	IN p_Descripcion						NVARCHAR(100) 	default '',
	IN p_EsInscrito							NVARCHAR(1) 	default '',
	IN p_IdClasificacionTipoInstrumento		NVARCHAR(3) 	default '',
	IN p_IdTipoInstrumento					NVARCHAR(3) 	default '',  
	IN p_IdInstrumento						BIGINT 			default 0,
	IN p_Nemo								NVARCHAR(40) 	default '',
	IN p_Factor								DECIMAL(26,14) 	default 0,
	IN p_Impuesto							DECIMAL(26,8) 	default 0,
	IN p_IdMoneda							NVARCHAR(3) 	default '',
	IN p_IdMonedaLiquidacion				NVARCHAR(3) 	default '',
	IN p_Paridad							DECIMAL(26,8) 	default 0,
	IN p_IdInstrumentoNuevo					BIGINT 			default 0,
	IN p_NemoNuevo							NVARCHAR(40) 	default '',
	IN p_FactorNuevo						DECIMAL(26,8) 	default 0,
	IN p_FechaActualizacion					NVARCHAR(10) 	default '0001-01-01',     
	IN p_FechaLimite						NVARCHAR(10) 	default '0001-01-01',     
	IN p_FechaPago							NVARCHAR(10) 	default '0001-01-01',     
	IN p_NumeroDividendo					BIGINT 			default 0,
	IN p_IdTipoDividendo					NVARCHAR(2) 	default '',
	IN p_PrecioAnterior						DECIMAL(26,8) 	default 0,
	IN p_FechaHoraIngresoRegistro			NVARCHAR(10) 	default '0001-01-01',     
	IN p_FechaHoraUltimaActualizacion		NVARCHAR(10) 	default '0001-01-01',     
	IN p_TipoIngreso						BIGINT 			default 0,
	IN p_IdEstado							NVARCHAR(3) 	default '',
	IN p_IdEmisor							NVARCHAR(40)	default '',
	IN p_UsaReglasNegocio 					NVARCHAR(1) 	default 'N',
	IN p_IdEmisorNuevo						NVARCHAR(40)	default '',
	IN p_Monto								DECIMAL(26,8) 	default 0,
	IN p_Cuotas								DECIMAL(26,8) 	default 0,
	
	-- Parametros de Salida
	OUT p_out_InformacionContador  			INT,
	OUT p_out_InformacionDetalle   			TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	
	OUT p_out_AdvertenciasContador 			INT,
	OUT p_out_AdvertenciasDetalle 			table ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
	
	OUT p_out_ErroresContador 				INT,   
	OUT p_out_ErroresDetalle 				table ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,

	OUT p_out_Secuencia 					bigint
)   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN SEQUENTIAL EXECUTION
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE v_FechaSistema		TIMESTAMP;
	DECLARE SALIDA_CONTROLADA  	CONDITION;	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		IF (NOT IS_EMPTY("BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes")) THEN
			
			v_Mensajes = SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2" FROM "BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes";
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
												
			INSERT INTO "BSCL.Custodia.BD::GBL.CrearEventoCapital_OUT_InformacionDetalle"							
			SELECT TCM."IdInterno",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'OK';		
			
			INSERT INTO "BSCL.Custodia.BD::GBL.CrearEventoCapital_OUT_AdvertenciasDetalle"
			SELECT TCM."IdInterno",EM."Codigo",TCM."Resultado" as "RequiereAutorizacion",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" IN ('ADVE','RAUT');
						
			INSERT INTO "BSCL.Custodia.BD::GBL.CrearEventoCapital_OUT_ErroresDetalle"
			SELECT TCM."IdInterno",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'RECH';
		END IF;
		
		SELECT count(1) into p_out_InformacionContador FROM "BSCL.Custodia.BD::GBL.CrearEventoCapital_OUT_InformacionDetalle";
		SELECT count(1) into p_out_AdvertenciasContador FROM "BSCL.Custodia.BD::GBL.CrearEventoCapital_OUT_AdvertenciasDetalle";
		SELECT count(1) into p_out_ErroresContador FROM "BSCL.Custodia.BD::GBL.CrearEventoCapital_OUT_ErroresDetalle";
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			INSERT INTO "BSCL.Custodia.BD::GBL.CrearEventoCapital_OUT_ErroresDetalle" SELECT 0,v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;
			SIGNAL SALIDA_CONTROLADA;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;	
	END;
	
	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
		
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_Secuencia				BIGINT := 0;
		DECLARE v_Valor					BIGINT := 0;
		DECLARE v_IdInstrumento 		BIGINT;
		BEGIN
			DELETE FROM "BSCL.Custodia.BD::GBL.CrearEventoCapital_OUT_InformacionDetalle";
			DELETE FROM "BSCL.Custodia.BD::GBL.CrearEventoCapital_OUT_AdvertenciasDetalle";
			DELETE FROM "BSCL.Custodia.BD::GBL.CrearEventoCapital_OUT_ErroresDetalle";
			DELETE FROM "BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes";
		END;
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			DECLARE v_resultadoValidacion 	NVARCHAR(40);
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoValidacion);
			IF :v_resultadoValidacion = 'RECH' THEN
				INSERT INTO "BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes"
				SELECT :p_NumeroDividendo as "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2",'RECH' as "Resultado" FROM :v_Errores;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			IF  p_IdTipoEventoCapital = 'DF' THEN
				SELECT "IdInstrumento" INTO v_IdInstrumento FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS WHERE INS."Nemotecnico" = :p_Nemo;
			END IF;
			
			v_Matriz =
			SELECT   p_IdEventoCapital as "IdEventoCapital"
					,p_IdTipoEventoCapital as "IdTipoEventoCapital"
					,p_Secuencia as "Secuencia"
					,p_EsInscrito as "EsInscrito"
					,p_IdClasificacionTipoInstrumento as "IdClasificacionTipoInstrumentoInformado"
					,p_IdTipoInstrumento as "IdTipoInstrumentoInformado"
					,CASE WHEN p_IdTipoEventoCapital = 'DF' THEN v_IdInstrumento ELSE p_IdInstrumento END as "IdInstrumento"
					,IOLD."IdTipoInstrumento"
					,IOLD."IdClasificacionTipoInstrumento"
					,IOLD."Nemotecnico"
					,IOLD."IdEstado" as "IdEstadoInstrumento"
					,p_Nemo as "Nemo"
					,p_Factor as "Factor"
					,p_IdMoneda as "IdMoneda"
					,p_IdMonedaLiquidacion as "IdMonedaLiquidacion"
					,p_Paridad as "Paridad"
					,p_IdInstrumentoNuevo as "IdInstrumentoNuevo"
					,INEW."IdTipoInstrumento" as "IdTipoInstrumentoNuevo"
					,INEW."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumentoNuevo"
					,INEW."Nemotecnico" as "NemotecnicoNuevo"
					,INEW."IdEstado" as "IdEstadoInstrumentoNuevo"
					,p_NemoNuevo as "NemoNuevo"
					,p_FactorNuevo as "FactorNuevo"
					,p_FechaActualizacion as "FechaActualizacion"
					,p_FechaLimite as "FechaLimite"
					,p_FechaPago as "FechaPago"
					,p_NumeroDividendo as "NumeroDividendo"
					,p_IdTipoDividendo as "IdTipoDividendo"
					,p_PrecioAnterior as "PrecioAnterior"
					,p_FechaHoraIngresoRegistro as "FechaHoraIngresoRegistro"
					,p_FechaHoraUltimaActualizacion as "FechaHoraUltimaActualizacion"
					,p_TipoIngreso as "TipoIngreso"
					,p_IdEstado as "IdEstado"
					,p_IdEmisor as "IdEmisor"
					,p_IdEmisorNuevo as "IdEmisorNuevo"
					,EV."CantidadSecuencia"
					,IBON."CallConfirmado"      -- Para validación PREPAGO BONO
					,IBON."CuponeraVariable"    -- Para validación PREPAGO BONO
					,ICUP."FechaPagoCupon"      -- Para validación PREPAGO BONO
					,1 as "UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE"
					,CASE WHEN :p_IdTipoEventoCapital = 'CN' THEN 1 ELSE 0 END as "CAMBIONEMO_TIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO"
					,CASE WHEN :p_IdTipoEventoCapital = 'CN' THEN 1 ELSE 0 END as "CAMBIONEMO_CLASIFICACIONTIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO"
					,CASE WHEN :p_IdTipoEventoCapital IN ('DS','CN') THEN 1 ELSE 0 END as "DIVISION_SOCIEDAD_INSTRUMENTO_IGUALES"
					,CASE WHEN :p_IdTipoEventoCapital = 'PB' THEN 1 ELSE 0 END as "BONO_SIN_CALL_CONFIRMADO" 	            -- Para validación PREPAGO BONO
					,CASE WHEN :p_IdTipoEventoCapital = 'PB' THEN 1 ELSE 0 END as "BONO_SIN_CUPONERA"			            -- Para validación PREPAGO BONO
					,CASE WHEN :p_IdTipoEventoCapital = 'PB' THEN 1 ELSE 0 END as "FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE"	-- Para validación PREPAGO BONO
					
			  FROM DUMMY
			  LEFT JOIN (SELECT I."IdInstrumento",I."Nemotecnico",TI."IdTipoInstrumento",CTI."IdClasificacionTipoInstrumento",I."IdEstado"
			  			   FROM "BSCL.Plataforma.BD::INS.Instrumento" I
			  			  INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TI ON (TI."IdTipoInstrumento" = I."IdTipoInstrumento")
			  			  INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CTI ON (CTI."IdClasificacionTipoInstrumento" = TI."IdClasificacionTipoInstrumento")
			  			) as IOLD
			  		 ON IOLD."IdInstrumento" = :p_IdInstrumento
			  LEFT JOIN (SELECT I."IdInstrumento",I."Nemotecnico",TI."IdTipoInstrumento",CTI."IdClasificacionTipoInstrumento",I."IdEstado"
			  			   FROM "BSCL.Plataforma.BD::INS.Instrumento" I
			  			  INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TI ON (TI."IdTipoInstrumento" = I."IdTipoInstrumento")
			  			  INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CTI ON (CTI."IdClasificacionTipoInstrumento" = TI."IdClasificacionTipoInstrumento")
			  			) as INEW
			  		 ON INEW."IdInstrumento" = :p_IdInstrumentoNuevo
			  LEFT JOIN (SELECT "NumeroDividendo", COUNT("Secuencia") as "CantidadSecuencia"
			  			   FROM "BSCL.Custodia.BD::CUS.EventoCapital"
			  			  WHERE "IdEmpresa" = :p_IdEmpresa
			  			    AND "Secuencia" = :p_Secuencia
			  			  GROUP BY "NumeroDividendo"
			  			) EV
			  		 ON (EV."NumeroDividendo" = p_NumeroDividendo)
			  LEFT JOIN ( SELECT INS."IdInstrumento",INS."Nemotecnico", BON."CallConfirmado", BON."CuponeraVariable"
			  			  FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
			  			  INNER JOIN "BSCL.Plataforma.BD::INS.InstrumentoBono" AS BON ON (BON."IdInstrumento" = INS."IdInstrumento")
			  			) as IBON
			  		  ON IBON."IdInstrumento" = :p_IdInstrumento
			  -- Obtiene fecha de próximo pago cupón para validación de fecha de pago en Prepago Bono 
			  LEFT JOIN (	SELECT INS."IdInstrumento", INS."Nemotecnico", MIN(CUP."Fecha") AS "FechaPagoCupon" 
							FROM "BSCL.Plataforma.BD::INS.Cupon" AS CUP
							INNER JOIN  "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."Nemotecnico" = CUP."Nemotecnico")
							WHERE CUP."Call" = 'S'
							GROUP BY INS."IdInstrumento",INS."Nemotecnico"			  
			  			) as ICUP
			  		  ON ICUP."IdInstrumento" = :p_IdInstrumento;
			  
			  		  
			v_validaciones =
			SELECT "NumeroDividendo"
				   ,CASE WHEN "UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE" = 1 AND COALESCE("CantidadSecuencia",0) >= 1 THEN 'RECH' ELSE 'OK' END "UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE"				   
				   ,CASE WHEN "DIVISION_SOCIEDAD_INSTRUMENTO_IGUALES" = 1 AND "Nemo" = "NemoNuevo" THEN 'RECH' ELSE 'OK' END "DIVISION_SOCIEDAD_INSTRUMENTO_IGUALES"
				   ,CASE WHEN "CAMBIONEMO_TIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO" = 1 AND "IdTipoInstrumento" <> "IdTipoInstrumentoNuevo" THEN 'RECH' ELSE 'OK' END "CAMBIONEMO_TIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO"
			   	   ,CASE WHEN "CAMBIONEMO_CLASIFICACIONTIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO" = 1 AND "IdClasificacionTipoInstrumento" <> "IdClasificacionTipoInstrumentoNuevo" THEN 'RECH' ELSE 'OK' END "CAMBIONEMO_CLASIFICACIONTIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO"
			   	   ,CASE WHEN "BONO_SIN_CALL_CONFIRMADO" = 1 AND (COALESCE("CallConfirmado",'') = '' OR COALESCE("CallConfirmado",'') = 'N')  THEN 'RECH' ELSE 'OK' END "BONO_SIN_CALL_CONFIRMADO"
			   	   ,CASE WHEN "BONO_SIN_CUPONERA" = 1        AND (COALESCE("CuponeraVariable",'') = '' OR COALESCE("CuponeraVariable",'') = 'N') THEN 'RECH' ELSE 'OK' END "BONO_SIN_CUPONERA"

			   	   ,CASE WHEN "FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE" = 1 AND (COALESCE("FechaPagoCupon",'') <> :p_FechaPago) THEN 'RECH' ELSE 'OK' END "FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE"
			   	   
			  FROM :v_Matriz;
	
			
			INSERT INTO "BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes"
			SELECT DISTINCT R."NumeroDividendo" as "IdInterno", R."Codigo"
				   ,CASE WHEN "Codigo" = 'UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE' THEN 'p_NumeroDividendo'
				   		 WHEN "Codigo" = 'DIVISION_SOCIEDAD_INSTRUMENTO_IGUALES' THEN 'Nemo'
				   		 WHEN "Codigo" = 'CAMBIONEMO_TIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO' THEN 'Nemo1'
			   		 	 WHEN "Codigo" = 'CAMBIONEMO_CLASIFICACIONTIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO' THEN 'Nemo1'			   		 	 
			   		 	 WHEN "Codigo" = 'BONO_SIN_CALL_CONFIRMADO' THEN 'Nemo'
			   		 	 WHEN "Codigo" = 'BONO_SIN_CUPONERA' THEN 'Nemo'
			   		 	 WHEN "Codigo" = 'FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE' THEN 'FechaPago'
				     END as "CodigoDato1"
				   ,CASE WHEN "Codigo" = 'UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE' THEN Cast(M."NumeroDividendo" As NVARCHAR)
				   		 WHEN "Codigo" = 'DIVISION_SOCIEDAD_INSTRUMENTO_IGUALES' THEN M."Nemo"
				   		 WHEN "Codigo" = 'CAMBIONEMO_TIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO' THEN M."Nemotecnico"
			   		 	 WHEN "Codigo" = 'CAMBIONEMO_CLASIFICACIONTIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO' THEN M."Nemotecnico"			   		 	 
			   		 	 WHEN "Codigo" = 'BONO_SIN_CALL_CONFIRMADO' THEN M."Nemotecnico"
			   		 	 WHEN "Codigo" = 'BONO_SIN_CUPONERA' THEN M."Nemotecnico"
			   		 	 WHEN "Codigo" = 'FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE' THEN M."FechaPago"
			   		 	 
				     END as "ValorDato1"			     
				   ,CASE WHEN "Codigo" = 'UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE' THEN 'p_Secuencia'
				   		 WHEN "Codigo" = 'DIVISION_SOCIEDAD_INSTRUMENTO_IGUALES' THEN 'NemoNuevo'
				   		 WHEN "Codigo" = 'CAMBIONEMO_TIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO' THEN 'Nemo2'
			   			 WHEN "Codigo" = 'CAMBIONEMO_CLASIFICACIONTIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO' THEN 'Nemo2'			   			 
			   			 WHEN "Codigo" = 'FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE' THEN 'FechaPagoCupon'
			   			 ELSE ''
				     END as "CodigoDato2"
				   ,CASE WHEN "Codigo" = 'UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE' THEN Cast(M."Secuencia" As NVARCHAR)
				   		 WHEN "Codigo" = 'DIVISION_SOCIEDAD_INSTRUMENTO_IGUALES' THEN M."NemoNuevo"
				   		 WHEN "Codigo" = 'CAMBIONEMO_TIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO' THEN M."NemotecnicoNuevo"
			   		 	 WHEN "Codigo" = 'CAMBIONEMO_CLASIFICACIONTIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO' THEN M."NemotecnicoNuevo"			   		 	 
			   		 	 WHEN "Codigo" = 'FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE' THEN M."FechaPagoCupon"
			   		 	 ELSE ''
				     END as "ValorDato2"
				   ,R."Resultado"	     
			  FROM (
			  		SELECT "NumeroDividendo",
					 		COALESCE(			 			
					 			 "1" ||'UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE'
								,"2" ||'DIVISION_SOCIEDAD_INSTRUMENTO_IGUALES'
								,"3" ||'CAMBIONEMO_TIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO'
								,"4" ||'CAMBIONEMO_CLASIFICACIONTIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO'								
								,"5" ||'BONO_SIN_CALL_CONFIRMADO'
								,"6" ||'BONO_SIN_CUPONERA'
								,"7" ||'FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE'
								
					 		) AS "Codigo",
					 		COALESCE(			 			
					 			 "1" ||"UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE"
								,"2" ||"DIVISION_SOCIEDAD_INSTRUMENTO_IGUALES"
								,"3" ||"CAMBIONEMO_TIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO"
								,"4" ||"CAMBIONEMO_CLASIFICACIONTIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO"
								,"5" ||"BONO_SIN_CALL_CONFIRMADO"
								,"6" ||"BONO_SIN_CUPONERA"
								,"7" ||"FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE"
					 		) AS "Resultado"
					   FROM :v_validaciones,"BSCL.Plataforma.BD::PLA.Identidad"
			  	   ) as R
			 INNER JOIN :v_Matriz as M ON M."NumeroDividendo" = R."NumeroDividendo"
			 WHERE R."Resultado" <> 'OK';
	
			
			IF (NOT IS_EMPTY("BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes")) THEN
				SIGNAL SALIDA_CONTROLADA; 
			END IF; 		
		END;
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------

		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------
		BEGIN
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(5000), "ValorNuevo" nvarchar(5000));
			
			IF p_IdTipoEventoCapital = 'EP' THEN
				v_Valor = p_FactorNuevo;
			END IF;
			
			-- Recuperamos numero de secuencia que usaremos para en el evento
	    	SELECT "BSCL.Custodia.BD.Secuencias::EventoCapital".NEXTVAL INTO p_out_Secuencia FROM DUMMY;
	
	    	
			INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapital"
			    (
				"IdEventoCapital",
				"IdEmpresa",
				"IdTipoEventoCapital",
				"Secuencia",
				"Descripcion",
				"EsInscrito",
				"IdClasificacionTipoInstrumento",
				"IdTipoInstrumento",
				"IdInstrumento",
				"Nemo",
				"Factor",
				"Impuesto",
				"Valor",
				"IdMoneda",
				"IdMonedaLiquidacion",
				"Paridad",
				"IdInstrumentoNuevo",
				"NemoNuevo",
				"FactorNuevo",
				"FechaActualizacion",	
				"FechaLimite",
				"FechaPago",
				"NumeroDividendo",
				"IdTipoDividendo",
				"PrecioAnterior",
				"FechaHoraIngresoRegistro",
				"FechaHoraUltimaActualizacion",
				"TipoIngreso",
				"IdUsuario",
				"IdEstado" ,
				"Origen",
				"IdEmisor",
				"IdEmisorNuevo",
				"CantidadCuotas",
				"Monto"
				)
	   	 	VALUES (	
				:p_out_Secuencia,								--	"IdEventoCapital",
				p_IdEmpresa,									--	"IdEmpresa",
				p_IdTipoEventoCapital,							--	"IdTipoEventoCapital",
-- SE COMENTA ESTO YA QUE LA SECUENCIA A ALMACENAR DEBE SER LA SECUENCIA INGRESADA POR PANTALLA.
--				v_Secuencia,									--	"Secuencia",
				p_Secuencia,									--	"Secuencia",
				p_Descripcion,									--	"Descripcion",
				p_EsInscrito,									--	"EsInscrito",
				p_IdClasificacionTipoInstrumento,				--	"IdClasificacionTipoInstrumento",
				p_IdTipoInstrumento,							--	"IdTipoInstrumento",
				CASE WHEN p_IdTipoEventoCapital = 'DF' THEN :v_IdInstrumento ELSE p_IdInstrumento END, --	"IdInstrumento",
				p_Nemo,											--	"Nemo",
				p_Factor,										--	"Factor",
				p_Impuesto,										--  "impuesto"
				v_Valor,										--	"Valor",
				p_IdMoneda,										--	"IdMoneda",
				p_IdMonedaLiquidacion,
				p_Paridad,
				p_IdInstrumentoNuevo,							--	"IdInstrumentoNuevo",
				p_NemoNuevo,									--	"NemoNuevo",
				p_FactorNuevo,									--	"FactorNuevo",
				TO_DATE(p_FechaActualizacion, 'YYYY-MM-DD'),	--	"FechaActualizacion",	
				TO_DATE(p_FechaLimite, 'YYYY-MM-DD'),			--	"FechaLimite",
				TO_DATE(p_FechaPago, 'YYYY-MM-DD'),				--	"FechaPago",
				p_NumeroDividendo,								--	"NumeroDividendo",
				'P',											--	"IdTipoDividendo",
				0,												--	"PrecioAnterior",
				v_FechaSistema,									--	"FechaHoraIngresoRegistro",
				TO_DATE(p_FechaPago, 'YYYY-MM-DD'),				--	"FechaHoraUltimaActualizacion",
				'C',											--	"TipoIngreso",
				0,												--	"IdUsuario",
				'VIG',											--	"IdEstado"
				'MAN',											--	"Origen"
				p_IdEmisor,
				p_IdEmisorNuevo,
				p_Cuotas,
				p_Monto

		    );
			
			
			INSERT INTO "BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes"
			SELECT :p_NumeroDividendo as "IdInterno",'UI_EVENTO_CAPITAL_CREADO' As "Codigo",'p_NumeroDividendo' As "CodigoDato1",
				   CAST(:p_NumeroDividendo as NVARCHAR) As "ValorDato1",'' As "CodigoDato2",'' As "ValorDato2",'OK' As "Resultado" FROM DUMMY;
					
			SIGNAL SALIDA_CONTROLADA;
			
			

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa => p_IdEmpresa, p_out_FechaHoraSistema => :v_fechaHoraSistema);
		        
		v_DetalleLog = 	SELECT 'INGRESO' AS "SubRegistro", 
		        		'IdEventoCapital' AS "CampoRegistro", 
		        		'' AS "ValorAntiguo", 
		        		:p_out_Secuencia AS "ValorNuevo" FROM dummy;
		        		

		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					 p_IdTipoLog => 'INGRESO' 
					,p_IdModulo => 'CUS' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa
					,p_FechaSistemaLog => TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => 'INGRESAREVENTOCAPITAL'
					,p_IdRegistro => :p_out_Secuencia 
					,p_DetalleLog => :v_DetalleLog  --hacia arriba obligatorios
					,p_NombreProceso => 'BSCL.Custodia.BD.Servicios::EVC.CrearEventoCapital' -- hacia abajo opcionales
					,p_EstadoProceso => ''
					,p_Cantidad => 0
					,p_Nemo => '');
		END;
		
		---------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
	END;
END; 

