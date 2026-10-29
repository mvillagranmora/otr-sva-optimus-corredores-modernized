SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CIE::CierreDiario";

CREATE PROCEDURE 
-- 
--  Proposito: registra la revision de procesos de cierre diario para el cierre de modulo
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 14/02/2017
--   
"BSCL.Tesoreria.BD.Servicios.CIE::CierreDiario" (
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
   
   	IN p_simularOperacion 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
    IN p_UsaReglasNegocio 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
   
     -- Parametros de Proceso
	IN p_FechaCierre					NVARCHAR(10),
	IN p_ProcesosACerrar		        TABLE(
												"IdModuloFuncionProceso"   BIGINT,
												"NombreFuncionProceso"	   NVARCHAR(40),
												"TotalOperaciones"         INTEGER,
											    "TotalConProblemas"        INTEGER,
											    "Resultado"                NVARCHAR(520)
											 ) DEFAULT EMPTY,

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
	DECLARE v_CountRevisionCierreDiario 	INT := 0;
	
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
	

	--------------------
	-- VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoRegla	NVARCHAR(40);
		DECLARE v_CantidadProcesosACerrar, v_CantidadErrores INT := 0;
    
		---Validaciones datos de entrada
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA_NO_DATA; 	
		END IF;	
		

		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCierreDiario"
		(
			 p_FechaCierre		=> :p_FechaCierre
			,p_ProcesosACerrar 	=> :p_ProcesosACerrar
			,p_out_Resultado	=> :v_Resultado
		);
		--SELECT 'v_Resultado', * FROM :v_Resultado; -- REVISAR
		
		-- Se Consulta diccionario
		v_TablaCodigosMensajes =
		SELECT DISTINCT "IdModuloFuncionProceso" as "IdInterno", "Codigo", 'DescripcionFuncionProceso' as "CodigoDato1", CAST("DescripcionFuncionProceso" as NVARCHAR(520)) as "ValorDato1", '' as "CodigoDato2", '' as "ValorDato2"
		FROM :v_Resultado
		WHERE "Resultado" <> 'OK';
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);
		
		SELECT Count(1) INTO v_CantidadProcesosACerrar FROM :p_ProcesosACerrar;		
		SELECT Count(1) INTO v_CantidadErrores	FROM :v_TablaCodigosMensajes;

		IF (v_CantidadErrores = v_CantidadProcesosACerrar) THEN
			-- Proceso rechazado. Todos los registros están inválidos
			p_out_ErroresDetalle =  SELECT E."Codigo",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."IdModuloFuncionProceso" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'RECH';
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;

			p_out_AdvertenciasDetalle =  SELECT E."Codigo",'' as "RequiereAutorizacion",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."IdModuloFuncionProceso" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'ADVE';	
			SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		

		ELSE
			-- Preceso realizado parcialmente.  Los errores se envian como advertencia ya que si se envian como errores la UI, esta hace rollback de todos incluyento los Ok	
			p_out_AdvertenciasDetalle =  SELECT E."Codigo",'' as "RequiereAutorizacion",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."IdModuloFuncionProceso" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'ADVE'	
										 UNION ALL
										 SELECT E."Codigo",'' as "RequiereAutorizacion", E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."IdModuloFuncionProceso" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'RECH';
			SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		END IF;
		--SELECT 'v_TablaCodigosMensajes', * FROM :v_TablaCodigosMensajes; -- REVISAR
		--SELECT 'v_erroroutmsg', * FROM :v_erroroutmsg; -- REVISAR
		

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


		--------------------------------------------------------
		-- SE ELIMINAN LOS REGISTROS RECHAZADOS POR VALIDACION
		--------------------------------------------------------
		v_ProcesosACerrar = 
		SELECT 
			PAC."IdModuloFuncionProceso",    -- se usa para hacer referencia a los registro
			PAC."NombreFuncionProceso",			
			PAC."TotalOperaciones",
			PAC."TotalConProblemas",
			PAC."Resultado",
			MFP."Descripcion" AS "DescripcionFuncionProceso",
			(CASE WHEN COALESCE(RCD."IdRevisionCierreDiario",0) = 0 THEN 'N' ELSE 'S' END) "ExisteRevision"    -- Especifica si existe o no revisión de cierre para la fecha indicada
		FROM :p_ProcesosACerrar AS PAC
		INNER JOIN "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" AS MFP ON (MFP."NombreFuncionProceso" = PAC."NombreFuncionProceso")				
		INNER JOIN "BSCL.Plataforma.BD::CRE.ModuloEmpresa" AS ME ON (MFP."IdCREModuloEmpresa" = ME."IdCREModuloEmpresa" AND ME."IdModulo"  = 'TES')
		LEFT JOIN "BSCL.Tesoreria.BD::CIE.RevisionCierreDiario" AS RCD ON (RCD."NombreFuncionProceso" = PAC."NombreFuncionProceso" AND RCD."FechaCierre" = :p_FechaCierre)		
		WHERE PAC."IdModuloFuncionProceso" IN (
		           SELECT DISTINCT "IdModuloFuncionProceso" FROM :v_Resultado WHERE "Resultado" = 'OK'
				   EXCEPT
				   SELECT DISTINCT "IdModuloFuncionProceso" FROM :v_Resultado WHERE "Resultado" = 'RECH'
				   );
		--SELECT 'v_ProcesosACerrar', * FROM :v_ProcesosACerrar; -- REVISAR


		---------------------------------------------------		
		-- REGISTRA REVISION DE CIERRE DIARIO
		---------------------------------------------------	

		-- Si existe revión de cierre la la fecha, se actualiza.
		UPDATE RCD
		SET
			"IdUsuario"     	= :p_IdUsuario,
			"FechaCierre"		= (to_seconddate(:p_FechaCierre  || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS')),									
			"FechaRegistro" 	= CURRENT_UTCTIMESTAMP, 			
			"TotalOperaciones" 	= PAC."TotalOperaciones",
			"TotalConProblemas" = PAC."TotalConProblemas",
			"Resultado"  		= PAC."Resultado",						
			"IdEstado"		    = 'CER'  -- Se cierra el proceso
		FROM "BSCL.Tesoreria.BD::CIE.RevisionCierreDiario" AS RCD
		INNER JOIN :v_ProcesosACerrar AS PAC ON (PAC."NombreFuncionProceso" = RCD."NombreFuncionProceso")
		WHERE RCD."FechaCierre" = :p_FechaCierre
		AND PAC."ExisteRevision" = 'S';


		-- Si no existe revisión de cierre para la fecha, se crea.
		INSERT INTO "BSCL.Tesoreria.BD::CIE.RevisionCierreDiario"("IdRevisionCierreDiario", "NombreFuncionProceso", "IdUsuario", "FechaCierre","FechaRegistro", "TotalOperaciones",	"TotalConProblemas", "Resultado", "IdEstado")
		SELECT
			"BSCL.Tesoreria.BD.Secuencias::CIE.RevisionCierreDiario".NEXTVAL AS "IdRevisionCierreDiario",
			PAC."NombreFuncionProceso",
			:p_IdUsuario,
			:p_FechaCierre,
			CURRENT_UTCTIMESTAMP,			
			PAC."TotalOperaciones",
			PAC."TotalConProblemas",
			PAC."Resultado",
			'CER'  -- Cerrado
		FROM :v_ProcesosACerrar AS PAC
		WHERE "ExisteRevision" = 'N';
			
					
		---------------------------------------------------		
		-- LOG
		---------------------------------------------------	
		v_Log = 
		SELECT
			"IdModuloFuncionProceso" As "IdCabeceraLog",
			'INFORMATIVO' As "IdTipoLog",
			:p_IdModulo   As "IdModulo",
			:p_IdUsuario  As "IdUsuario",
			:p_IdEmpresa  As "IdEmpresa",	
			TO_VARCHAR (p_FechaCierre, 'YYYY-MM-DD') As "FechaSistemaLog",
			:p_IdUsuario  As	"IdUsuarioAtendido",
			0 AS "IdPersona",
			0  As "IdCliente",
			'' As "EstadoCliente",
			'' As "NombreCliente",
			'' As "IdTipoDocumentoIdentidad",
			'' As "IdDocumentoIdentidad",
			0  As "IdPortafolio",
			'' As "EstadoPortafolio",		
			'CIERRE-MODULO' As "IdTipoRegistro",
			'' As "IdRegistro",
			'' As "EstadoRegistro",
			'' As "IdTipoOperacion",
			0  As "Folio",
			0  As "Cantidad",
			0  As "PrecioTasa",
			0  As "Monto",
			'' As "IdMonedaOrigen",
			'' As "IdMonedaLiquidacion",
			'' As "TipoCambio",
			'' As "Nemo",
			1  As "IdProceso",
			'' As "EstadoProceso",
			"NombreFuncionProceso" As "NombreProceso"
		FROM :v_ProcesosACerrar;
		--SELECT 'Log', * FROM :v_Log; -- REVISAR	


		v_LogDetalle =  
		SELECT
			"IdModuloFuncionProceso"  As "IdCabeceraLog",
			'CUADRATURA' As "SubRegistro", 
			'EVENTO' 	 As "CampoRegistro", 
			'' 			 As "ValorAntiguo", 
			'CIEMOD' 	 As "ValorNuevo" 
		FROM :v_ProcesosACerrar;
		--SELECT 'LogDetalle', * FROM :v_LogDetalle; -- REVISAR


		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" 
		(
			p_Log 		 => :v_Log,
			p_LogDetalle => :v_LogDetalle
		);	
		
		
		--------------------------------------
		-- RESULTADO PROCESO
		--------------------------------------
		v_TablaCodigosMensajes =
		SELECT  
				PAC."IdModuloFuncionProceso" AS "IdInterno"
			   ,'PROCESO_CERRADO_EXITOSAMENTE' As "Codigo"
			   ,'DescripcionFuncionProceso' as "CodigoDato1"			   
			   ,CAST(PAC."DescripcionFuncionProceso" as NVARCHAR(520)) as "ValorDato1"			   
			   ,'' as "CodigoDato2"
			   ,'' as "ValorDato2" 
		 FROM :v_ProcesosACerrar AS PAC;
				 
		SIGNAL SALIDA_EXITOSA;
  	END;    
END;

  