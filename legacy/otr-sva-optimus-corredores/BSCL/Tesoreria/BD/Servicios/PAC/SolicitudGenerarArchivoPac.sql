SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.PAC::SolicitudGenerarArchivoPac";

CREATE PROCEDURE
-- 
--  Proposito: Recibe una solicitud para generar archivo de pago(bancario) y la inscribe en programa de JobManager
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 25/07/2016
--
"BSCL.Tesoreria.BD.Servicios.PAC::SolicitudGenerarArchivoPac" (

     -- Parametros de Validacion
     IN p_IdEmpresa 					BIGINT,  
     IN p_IdUsuario 					BIGINT,   
     IN p_IdModulo 						NVARCHAR(3),    
     IN p_UsaReglasNegocio 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.   

	 IN p_ArchivosAGenerar 				TABLE(
											"IdNominaTransferencia" BIGINT,        -- IdTransferencia ligada al archivo a generar
											"IdBanco"  				BIGINT,
											"IdMoneda" 				NVARCHAR(3),
											"IdMedioDePago" 		NVARCHAR(3),
										 	"IdTipoTarea"			NVARCHAR(40), 
											"IdAgenda"	 		    BIGINT,       -- Agenda(para programar tarea)
											"Segundos"				BIGINT,        -- Segundos en que se ejecuta
											"DiaDescuento"          BIGINT,
											"FechaCargo"			DATE
									 	),
									 	
	 OUT p_out_ResultadoPrograma        TABLE("IdNominaTransferencia" BIGINT, "IdPrograma" BIGINT), 
     

     --Salidas para mensajes de advertencias y errores
     OUT p_out_InformacionDetalle       TABLE("NumeroReferencia" BIGINT,  "Id" BIGINT,  "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000)),
     OUT p_out_AdvertenciasContador 	INT,	  
     OUT p_out_AdvertenciasDetalle  	TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),	
  	 OUT p_out_ErroresContador      	INT,
     OUT p_out_ErroresDetalle       	TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_AdvertenciasDetalle		TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000));
	DECLARE v_ErroresDetalle			TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000));
	DECLARE v_AdvertenciasContador		INT;
	DECLARE v_ErroresContador			INT;	
	DECLARE v_resultadoRegla 			NVARCHAR(40);	        		
	DECLARE v_RechazadosPorValidacion	TABLE ("IdNominaTransferencia"  BIGINT);
	DECLARE v_IdTipoTarea			    NVARCHAR(40);
	DECLARE v_IdAgenda	 		    	BIGINT;
	DECLARE v_Segundos					BIGINT; 
	DECLARE v_Id						BIGINT; 
	DECLARE v_i							INT;
	DECLARE v_ARR_SOL      				BIGINT ARRAY;	
	DECLARE v_IdPrograma				TABLE("IdPrograma" BIGINT); 
	DECLARE v_TablaMensajes				TABLE("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)); 	    
	DECLARE v_DiaDescuento				BIGINT;
	DECLARE v_FechaCargo				DATE;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ResultadoPrograma         = SELECT 0  as "IdNominaTransferencia",0 As "IdPrograma" FROM dummy WHERE 1=2;
	p_out_InformacionDetalle  		= SELECT 0 AS "NumeroReferencia",0 AS "Id", '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1=2;
	
	
	
	
	v_AdvertenciasContador			:= 0;
	v_ErroresContador				:= 0;
	v_AdvertenciasDetalle			= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	v_ErroresDetalle				= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	v_RechazadosPorValidacion		= SELECT 0 as "IdNominaTransferencia" FROM dummy where 1=2;
	v_IdPrograma					= SELECT 0 as "IdPrograma" FROM dummy where 1=2;
	v_TablaMensajes				    = SELECT 0 AS "IdInterno" ,'' AS "Codigo" ,'' AS "Descripcion" FROM dummy where 1=2;
	
	
	--------------------
	--VALIDACIONES
	--------------------	   

	
	--------------------
	--LOGICA
	--------------------
	BEGIN
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;		    	 


		-- Se eliminan las solicitudes que no pasaron las validaciones
		v_ArchivosAGenerar =
	 	SELECT *
	 	FROM :p_ArchivosAGenerar
	 	WHERE "IdNominaTransferencia" NOT IN (SELECT "IdNominaTransferencia" FROM :v_RechazadosPorValidacion);
	 	


		-----------------------------------------------------------
		-- INSCRIPCION DE TAREA EN JOB-MANAGER
		-- Usa ciclo ya que el servicio de inscripcion no es masivo	
		-----------------------------------------------------------		
		v_ARR_SOL	:= ARRAY_AGG(:v_ArchivosAGenerar."IdNominaTransferencia" ORDER BY "IdNominaTransferencia" ASC);
		
		-- Se usa ciclo ya que el servicio no es masivo
		FOR v_i IN 1 .. CARDINALITY(:v_ARR_SOL) DO
				v_Id := :v_ARR_SOL[:v_i];

				-- Recupera informacion de la solicitud
				SELECT "IdTipoTarea", "IdAgenda", "Segundos", "DiaDescuento", "FechaCargo"
				INTO  v_IdTipoTarea, v_IdAgenda, v_Segundos, v_DiaDescuento, v_FechaCargo
				FROM :v_ArchivosAGenerar
				WHERE "IdNominaTransferencia" = :v_Id;
				
				
 	 			-- Genera parámetros para la solicitud(parámetros de entrada para el Job) 	 			
 	 			v_TablaParametros = 
 	 			SELECT 'IdEmpresa' as "NombreParametro", CAST(:p_IdEmpresa AS NVARCHAR(40)) as "Valor" FROM DUMMY
				UNION ALL
				SELECT 'IdNominaTransferencia' as "NombreParametro", CAST(:v_Id AS NVARCHAR(40)) as "Valor" FROM DUMMY
				UNION ALL
				SELECT 'IdBanco' as "NombreParametro", CAST("IdBanco" AS NVARCHAR(40)) as "Valor" FROM :v_ArchivosAGenerar WHERE "IdNominaTransferencia" = :v_Id
				UNION ALL
				SELECT 'IdMoneda' as "NombreParametro", "IdMoneda" as "Valor" FROM :v_ArchivosAGenerar WHERE "IdNominaTransferencia" = :v_Id
				UNION ALL
				SELECT 'IdMedioDePago' as "NombreParametro", "IdMedioDePago" as "Valor" FROM :v_ArchivosAGenerar WHERE "IdNominaTransferencia" = :v_Id
				UNION ALL
				SELECT 'IdUsuario' as "NombreParametro", CAST(:p_IdUsuario AS NVARCHAR(40)) as "Valor" FROM DUMMY
				UNION ALL
				SELECT 'DiaDescuento' as "NombreParametro", CAST("DiaDescuento" AS NVARCHAR(40)) as "Valor" FROM :v_ArchivosAGenerar WHERE "IdNominaTransferencia" = :v_Id
				UNION ALL
				SELECT 'FechaCargo' as "NombreParametro", CAST("FechaCargo" AS NVARCHAR(40)) AS "Valor" FROM :v_ArchivosAGenerar WHERE "IdNominaTransferencia" = :v_Id;
			
 	 			
				CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"    
				(   
				     p_IdEmpresa     			=> :p_IdEmpresa
					,p_IdUsuario    			=> :p_IdUsuario
					,p_IdModulo    				=> :p_IdModulo					
				 	,p_IdAgenda	    			=> :v_IdAgenda
					,p_IdTipoTarea				=> :v_IdTipoTarea  -- Job a ejecutar
					,p_Segundos	    			=> :v_Segundos     -- En cuantos segundos más se ejecuta
				    ,p_TablaParametros 			=> :v_TablaParametros
				 	,p_CreaParam				=> 'S'		       -- S/N indica si vienen parámetros
					,p_out_IdPrograma 			=> v_IdPrograma    -- Id el job creado
					,p_out_AdvertenciasContador => v_AdvertenciasContador
					,p_out_AdvertenciasDetalle 	=> v_AdvertenciasDetalle
					,p_out_ErroresContador 		=> v_ErroresContador
					,p_out_ErroresDetalle 		=> v_ErroresDetalle
				);

				p_out_AdvertenciasDetalle 	= SELECT * FROM :v_AdvertenciasDetalle UNION ALL SELECT * FROM :p_out_AdvertenciasDetalle;
				p_out_ErroresDetalle 		= SELECT * FROM :v_ErroresDetalle UNION ALL SELECT * FROM :p_out_ErroresDetalle;
				p_out_ResultadoPrograma     = SELECT :v_Id AS "IdNominaTransferencia", "IdPrograma" FROM :v_IdPrograma UNION ALL SELECT * FROM :p_out_ResultadoPrograma;
		
				IF (:v_ErroresContador > 0) THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;	

		END FOR;



		------------------------------------------
		-- PREPARA SALIDA
		------------------------------------------
		
		p_out_ResultadoPrograma     = SELECT :v_Id AS "IdNominaTransferencia", "IdPrograma" FROM :p_out_ResultadoPrograma;

		v_Mensajes = SELECT "IdNominaTransferencia" AS "IdInterno", 'SOLICITUD_GENERACION_ARCHIVO_OK' AS "Codigo", 'IdPrograma' AS "CodigoDato1", CAST("IdPrograma" AS NVARCHAR(50)) AS "ValorDato1",'' AS "CodigoDato2", '' AS "ValorDato2" 
					 FROM :p_out_ResultadoPrograma;					 
					 
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes, v_TablaMensajes);
				
		
		p_out_InformacionDetalle = SELECT "IdInterno" AS "NumeroReferencia",  0 AS "Id",  EM."Codigo", EM."Descripcion" 
								   FROM :v_TablaMensajes AS EM;
		 		 
		
  	END;
    
END;

  