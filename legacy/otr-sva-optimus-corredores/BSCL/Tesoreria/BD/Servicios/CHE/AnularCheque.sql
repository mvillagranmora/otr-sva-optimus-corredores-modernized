SET SCHEMA "BSCL";   

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CHE::AnularCheque";

-- 
--  Proposito : Anular Cheque en talonario (por que se rompio, se mancho, etc...)
--  Autor ....: MSANDOVAL
--  Fecha ....: 2017-10-30
-- 
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.CHE::AnularCheque"
  (
   -- Parametros de Validacion                                                                                            
   IN  p_IdEmpresa          BIGINT,                                                                            
   IN  p_IdUsuario          BIGINT,                                                                            
   IN  p_IdModulo           NVARCHAR(3),                                                                       
   IN  p_UsaReglasNegocio   NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.                          

   -- Parametros para logica
   IN  p_IdChequera			BIGINT,
   IN  p_NroSerieAnular		BIGINT,
   IN  p_Motivo		  		NVARCHAR(5000),
   OUT p_out_Serie			TABLE ( "SerieActual" BIGINT, "Disponibles" BIGINT ),
   
   -- Parametros de Salida
   OUT p_out_AdvertenciasContador	INT,
   OUT p_out_AdvertenciasDetalle    TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   OUT p_out_ErroresContador        INT,   
   OUT p_out_ErroresDetalle         TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
) 
   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
	
BEGIN  
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
--	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	
	DECLARE v_erroroutmsg  				nvarchar(520);	        	
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	
	DECLARE v_CuentaBancariasRechazadas	TABLE( "IdCuentaBanco" BIGINT );
	DECLARE	p_TablaCodigosMensajes 		TABLE( "CodigoMensaje" NVARCHAR(100), "CodigoDato" NVARCHAR(100),
											   "ValorDato" NVARCHAR(520) );
	DECLARE	p_DetalleLog 				TABLE (	"SubRegistro" NVARCHAR(40), 
												"CampoRegistro" NVARCHAR(40), 
												"ValorAntiguo" NVARCHAR(5000), 
												"ValorNuevo" NVARCHAR(5000) );
	
	--* -----------------------------------------------------------------------------------------------
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
        CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" 
        	 (:p_TablaCodigosMensajes, :p_out_TablaMensajes);

		p_out_ErroresDetalle =  Select "Codigo", "Descripcion" 
		                        From :p_out_TablaMensajes Union All
								Select "Codigo", "Descripcion" 
								From :p_out_ErroresDetalle;		
	
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;		 					 	
	END;	
	--* -----------------------------------------------------------------------------------------------	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
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
	--* -----------------------------------------------------------------------------------------------	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		
	BEGIN --LOGICA

	    --------------------
		-- Declare Variables
		--------------------
	    DECLARE v_AdvertenciasContador 			INT;
	    DECLARE v_ErroresContador      			INT;
	    DECLARE v_AdvertenciasDetalle  			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000));	  	

		DECLARE v_Count INT;
		DECLARE v_Glosa NVARCHAR(5000);
		DECLARE	v_IdEstado NVARCHAR(10);
		DECLARE v_Remanente INT;
		DECLARE	v_NumeroChequeDesde BIGINT;
		DECLARE v_NumeroChequeHasta BIGINT;
		DECLARE v_NumeroChequeActual BIGINT;
		DECLARE v_resultadoRegla NVARCHAR(40);
		DECLARE v_FechaHoraSistema SECONDDATE;
      	
    	p_TablaCodigosMensajes = Select '' as "CodigoMensaje", '' as "CodigoDato", '' as "ValorDato" from dummy where 1=2;
		p_out_TablaMensajes = Select '' as "Codigo", '' as "Descripcion" from dummy where 1=2;		
			 	
		--* ------------------------------------------
		--* VALIDACIONES BASICAS
		--* ------------------------------------------ 
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"
			 (:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			p_TablaCodigosMensajes = 
				SELECT 	"Codigo" AS "CodigoMensaje", 
						"CodigoDato1" AS "CodigoDato", 
						"ValorDato1" AS "ValorDato" 
			    FROM	:v_Errores;
			SIGNAL SALIDA_CONTROLADA;
		END IF;   
			
		BEGIN
		
			SELECT	COUNT(1)
			INTO	v_Count
			FROM	"BSCL.Tesoreria.BD::TES.Chequera" 
		   	WHERE	"IdChequera" = p_IdChequera;
			
			IF v_Count = 0 THEN
			   	p_TablaCodigosMensajes = SELECT	'NO_SE_EMCONTRO_CHEQUERA' AS "CodigoMensaje", 'IdChequera' AS "CodigoDato", p_IdChequera AS "ValorDato" 
				                         FROM 	DUMMY UNION ALL
				                         SELECT "CodigoMensaje", "CodigoDato", "ValorDato"
				                         FROM 	:p_TablaCodigosMensajes;
				SIGNAL SALIDA_CONTROLADA;				                        
			END IF;

			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"
				 (p_IdEmpresa,:v_FechaHoraSistema);
			
			SELECT 	"NumeroChequeDesde",
			   		"NumeroChequeHasta",
			   		"NumeroChequeActual",
			   		"Remanente",
			   		"IdEstado" 
			INTO	v_NumeroChequeDesde,
			   		v_NumeroChequeHasta,
			   		v_NumeroChequeActual,
			   		v_Remanente,
			   		v_IdEstado 
		   	FROM 	"BSCL.Tesoreria.BD::TES.Chequera" 
		   	WHERE	"IdChequera" = p_IdChequera;
			
			IF v_NumeroChequeDesde > p_NroSerieAnular OR
			   v_NumeroChequeHasta < p_NroSerieAnular THEN
			   p_TablaCodigosMensajes = Select 'NUMEROS_CHEQUES_ERROR' AS "CodigoMensaje", 'NumeroChequeDesde' AS "CodigoDato", :v_NumeroChequeDesde AS "ValorDato" 
				                        From DUMMY Union All				                         
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;
			END IF;
			
			IF :v_NumeroChequeActual <> :p_NroSerieAnular  THEN
			   p_TablaCodigosMensajes = Select 'CHEQUE_UTILIZADO' AS "CodigoMensaje", 'I' AS "CodigoDato", p_NroSerieAnular AS "ValorDato" 
				                        From DUMMY Union All				                         
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;
			END IF;

			IF :v_IdEstado <> 'ACT' THEN
			   p_TablaCodigosMensajes = Select 'CHEQUERA_NO_ACTIVA' AS "CodigoMensaje", 'IdChequera' AS "CodigoDato", p_IdChequera AS "ValorDato" 
				                        From DUMMY Union All				                         
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;
			END IF;
		END;

		--* Si existen errores en las validaciones debe salir
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_TablaCodigosMensajes; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;					


		--* ----------------------------------------------------
		--* UPDATE DE LA CHEQUERA
		--* ----------------------------------------------------
		UPDATE	"BSCL.Tesoreria.BD::TES.Chequera" 
		SET		"NumeroChequeActual" = v_NumeroChequeActual + 1,
				"Remanente" = v_Remanente - 1
	   	WHERE	"IdChequera" = p_IdChequera;


		p_out_Serie =
			SELECT 	v_NumeroChequeActual + 1 AS "SerieActual",
				    v_Remanente - 1 AS "Disponibles" 
			FROM	DUMMY;

		--* ----------------------------------------------------
		--* LOG
		--* ----------------------------------------------------
/*
		p_DetalleLog =
			SELECT	'ANULAR CHEQUE' AS "SubRegistro",
					'NroSerieActual' AS "CampoRegistro",
					p_NroSerieAnular AS "ValorAntiguo",
					'ANU' AS "ValorNuevo"
			FROM 	DUMMY; 
			
		v_Glosa = TRIM(p_IdChequera) || ' ' || RTRIM(p_Motivo);
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog"(
			 p_IdTipoLog 		 => 'MODIFICACION'
			,p_IdModulo 		 => :p_IdModulo
			,p_IdUsuario 		 => :p_IdUsuario
			,p_IdEmpresa 		 => :p_IdEmpresa
			,p_FechaSistemaLog 	 => TO_NVARCHAR(:v_FechaHoraSistema,'YYYY-MM-DD')
			,p_IdUsuarioAtendido => :p_IdUsuario
			,p_IdTipoRegistro 	 => 'ANULACHEQUE'
			,p_NombreProceso 	 => 'ANULAR CHEQUE'
			,p_Nemo 		 	 => v_Glosa
			,p_DetalleLog		 => :p_DetalleLog
		);
*/		
	   	
   END; 
END;




