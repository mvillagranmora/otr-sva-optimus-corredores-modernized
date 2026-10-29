SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.GenerarArchivoInformeCarteraPropia";



CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.GenerarArchivoInformeCarteraPropia" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_IdEmpresa			BIGINT,
	IN p_IdUsuario			BIGINT,
	IN p_IdModulo			NVARCHAR(3),
	
	-- Parametros entrada
	IN p_Fecha				DATE,
	IN p_TipoValorizacion	NVARCHAR(1) default 'M',
	IN p_IdCliente			INT,
	IN p_IdPortafolio		INT,
    IN p_IdSocioNegocio     INT,
	IN p_TipoReporte        NVARCHAR(20),

	-- Parametros salida
	OUT p_out_ResultadoProceso		TABLE("NombreParametro" NVARCHAR(5000), "Valor" NVARCHAR(5000)),
	
	--Salidas para mensajes de advertencias y errores
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
	DECLARE v_erroroutmsg  				NVARCHAR(520);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_AdvertenciasDetalle		TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000));
	DECLARE v_ErroresDetalle			TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000));
	DECLARE v_AdvertenciasContador		INT;
	DECLARE v_ErroresContador			INT;	
	DECLARE v_resultadoRegla 			NVARCHAR(40);	        		
	DECLARE v_RechazadosPorValidacion	TABLE ("IdNominaTransferencia"  BIGINT);
	DECLARE v_RutaArchivo				NVARCHAR(520);
	DECLARE v_NombreArchivo				NVARCHAR(520);
	DECLARE v_Servicio					NVARCHAR(520);
	DECLARE v_FechaHoraSistema			SECONDDATE; -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
	DECLARE v_out_IdPrograma			TABLE("IdPrograma" BIGINT);
		        	
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
	
	p_out_ResultadoProceso		= SELECT '' as "NombreParametro", '' as "Valor" FROM dummy where 1=2;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle		= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;  
	
	v_AdvertenciasContador		:= 0;
	v_ErroresContador			:= 0;
	v_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	v_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	
	v_out_IdPrograma			= SELECT 0 as "IdPrograma" FROM dummy where 1=2;
	
	
	BEGIN
		--------------------
		--LOGICA
		--------------------
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaHoraSistema);
		
		-- Ruta Archivo
		SELECT
			(CASE WHEN (COALESCE(ConfigPlataforma."Valor",'') <> '' AND COALESCE(ConfigMesa."Valor",'') <> '') THEN ConfigPlataforma."Valor" || ConfigMesa."Valor" ELSE '' END) 
			into v_RutaArchivo
			--'C:\\PROYECTOS\\' INTO v_RutaArchivo -- para las pruebas
		FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa" ConfigMesa
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" ConfigPlataforma
			ON (UCASE(ConfigPlataforma."CodigoParametro") = 'PLA.RUTABASEARCHIVOS' AND ConfigMesa."IdEmpresa" = ConfigPlataforma."IdEmpresa")
		WHERE ConfigMesa."IdEmpresa" = p_IdEmpresa
		  AND ConfigMesa."CodigoParametro" = 'RUTAARCHIVOINFORMECARTERA';

		-- Nombre Archivo
		SELECT 
			CASE
				WHEN p_TipoReporte = 'CAR_PRO' THEN 'INFORME_CARTERA_PROPIA_'|| TO_NVARCHAR(p_Fecha,'YYYYMMDD') || TO_NVARCHAR(v_FechaHoraSistema,'HH24MISS') || '.xls'
				WHEN p_TipoReporte = 'CAR_PAC' THEN 'INFORME_CARTERA_PACTOS_'|| TO_NVARCHAR(p_Fecha,'YYYYMMDD') || TO_NVARCHAR(v_FechaHoraSistema,'HH24MISS') || '.xls'
			END 
		INTO v_NombreArchivo
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
		WHERE EMP."IdEmpresa" = p_IdEmpresa;
		
		-------------------- INSCRIPCION DE TAREA EN JOB-MANAGER --------------------
		
		-- Genera parámetros para la solicitud(parámetros de entrada para el Job)
 	 	v_TablaParametros = 
 	 		SELECT 'IdEmpresa' AS "NombreParametro", TO_NVARCHAR(p_IdEmpresa)AS "Valor" FROM DUMMY
 	 		UNION ALL
 	 		SELECT 'IdUsuario' AS "NombreParametro", TO_NVARCHAR(p_IdUsuario)			AS "Valor" FROM DUMMY
 	 		UNION ALL
 	 		SELECT 'IdModulo' AS "NombreParametro", TO_NVARCHAR(p_IdModulo)	AS "Valor" FROM DUMMY
 	 		UNION ALL
 	 		SELECT 'Fecha' AS "NombreParametro", TO_NVARCHAR(p_Fecha)	AS "Valor" FROM DUMMY
			UNION ALL
			SELECT 'NombreArchivo'	AS "NombreParametro", TO_NVARCHAR(v_NombreArchivo)	AS "Valor" FROM DUMMY
			UNION ALL
			SELECT 'TipoValorizacion' AS "NombreParametro", TO_NVARCHAR(p_TipoValorizacion)	AS "Valor" FROM DUMMY
			UNION ALL
			SELECT 'IdCliente'	AS "NombreParametro", TO_NVARCHAR(p_IdCliente)     	AS "Valor" FROM DUMMY
			UNION ALL
			SELECT 'IdPortafolio' AS "NombreParametro", TO_NVARCHAR(p_IdPortafolio) AS "Valor" FROM DUMMY
			UNION ALL
			SELECT 'IdSocioNegocio' AS "NombreParametro", TO_NVARCHAR(p_IdSocioNegocio) AS "Valor" FROM DUMMY
			UNION ALL
			SELECT 'RutaArchivo' AS "NombreParametro", TO_NVARCHAR(v_RutaArchivo) AS "Valor" FROM DUMMY
			UNION ALL
 	 		SELECT 'Servicio' AS "NombreParametro", TO_NVARCHAR('BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaValorizada') AS "Valor" FROM DUMMY
			UNION ALL
 	 		SELECT 'TipoReporte' AS "NombreParametro", TO_NVARCHAR(p_TipoReporte) AS "Valor" FROM DUMMY;

 	 	-- ***** Ver si agregarlo a una tabla de configuracion
		CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"(
		     p_IdEmpresa     			=> :p_IdEmpresa
			,p_IdUsuario    			=> :p_IdUsuario
			,p_IdModulo    				=> :p_IdModulo					
		 	,p_IdAgenda	    			=> 0						-- *****
			,p_IdTipoTarea				=> 'GENERAR_ARCHIVO_INFORME_CARTERA_PROPIA'	-- *****	-- Job a ejecutar
			,p_Segundos	    			=> 0						-- *****	-- En cuantos segundos más se ejecuta
		    ,p_TablaParametros 			=> :v_TablaParametros
		 	,p_CreaParam				=> 'S'						-- *****	-- S/N indica si vienen parámetros
			,p_out_IdPrograma 			=> :v_out_IdPrograma		-- Id el job creado
			,p_out_AdvertenciasContador => v_AdvertenciasContador
			,p_out_AdvertenciasDetalle 	=> v_AdvertenciasDetalle
			,p_out_ErroresContador 		=> v_ErroresContador
			,p_out_ErroresDetalle 		=> v_ErroresDetalle
		);
		
		--agregamos al log de ejecucion
		INSERT INTO "BSCL.Mesa.BD::SPT.LogInforme"
		("IdEmpresa", "IdUsuario", "Informe", "FechaEjecucion", "FechaInforme") VALUES
		(:p_IdEmpresa, :p_IdUsuario, 'GENERAR_ARCHIVO_INFORME_CARTERA_PROPIA', TO_NVARCHAR(v_FechaHoraSistema,'YYYY-MM-DD'), :p_Fecha);

		p_out_AdvertenciasDetalle 	= SELECT * FROM :v_AdvertenciasDetalle UNION ALL SELECT * FROM :p_out_AdvertenciasDetalle;
		p_out_ErroresDetalle 		= SELECT * FROM :v_ErroresDetalle UNION ALL SELECT * FROM :p_out_ErroresDetalle;
		
		IF (:v_ErroresContador > 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		-- RESULTADO PROCESO
		p_out_ResultadoProceso = 
			SELECT "NombreParametro", "Valor" FROM :v_TablaParametros
			UNION ALL
			SELECT 'RutaInformeCarteraPropia' AS "NombreParametro", CT."Valor" AS "Valor"
			FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa" CT
			WHERE CT."IdEmpresa" = p_IdEmpresa
			 AND CT."CodigoParametro" = 'RUTAARCHIVOINFORMECARTERA';
			
	END;
END;