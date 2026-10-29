SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.GenerarArchivoTasas";

-- 
--  Proposito: Servicio llamado desde UI Solicitud de Tasas que crea nuevas tasas, las que posteriormente
--				estaran en el archivo de solicitud generado.
--				Luego se realiza inscripcion de tarea en job manager (aqui se crea el archivo).
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 06/09/2016  
-- 

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.GenerarArchivoTasas" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_IdEmpresa			BIGINT,
	IN p_IdUsuario			BIGINT,
	IN p_IdModulo			NVARCHAR(3),
	IN p_UsaReglasNegocio	NVARCHAR(1),	-- S/N se usa en caso de servicios anidados
	
	-- Parametros entrada
	IN p_Fecha				NVARCHAR(10),
	IN p_CodigoFuente		NVARCHAR(40),
	IN p_Mercado			NVARCHAR(40) DEFAULT '',
	
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
	
	DECLARE v_Count						BIGINT := 0;
	DECLARE v_RutaArchivo				NVARCHAR(520);
	DECLARE v_NombreArchivo				NVARCHAR(520);
	DECLARE v_Servicio					NVARCHAR(520);
	DECLARE v_IdFuente					BIGINT := 0;
	DECLARE v_FechaHoraSistema			SECONDDATE; -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
	DECLARE v_FechaHoraUI				SECONDDATE;		-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
	DECLARE v_FechaDesde				SECONDDATE;
	DECLARE v_FechaHasta				SECONDDATE;
	DECLARE v_out_IdPrograma			TABLE("IdPrograma" BIGINT);
	DECLARE v_ValidaModulos             NVARCHAR(1):='N';
	DECLARE v_CountModulos				BIGINT:=0;
	DECLARE v_CountMercado				BIGINT:=0;
	
	DECLARE v_FechaSistemaUTC   		NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL   		NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC  	 	NVARCHAR(40);
	DECLARE v_FechaHoraSistemaLOCAL 	SECONDDATE;
	DECLARE v_FechaHoraUTC   			NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL   			NVARCHAR(40);
	DECLARE v_RowCount					BIGINT:=0;

		        	
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
		--VALIDACIONES
		--------------------	   
		IF p_UsaReglasNegocio = 'S' THEN
			DECLARE v_resultadoRegla 	NVARCHAR(40);
			
			-- Validaciones datos de entrada			
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle,:v_resultadoRegla);
			IF :v_resultadoRegla = 'RECH' THEN
				p_out_ErroresDetalle = select "Codigo", "Descripcion" from :v_out_ErroresDetalle;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
		END IF;
		
		-- Validacion no solicitar Tasas para misma Fecha y Fuente
		/* Se comenta ésta validación debido a lo que se pide en jira IMPCREDOPT-2035.
		   Lo correcto es que permite solicitar tasas las veces que requiere, siempre 
		   y cuando no exista para esa fuente tasas publicadas u oficializadas.*/
		
		/*v_FechaDesde := TO_SECONDDATE(:p_Fecha||' 00:00:00','YYYY-MM-DD HH24:MI:SS');
		v_FechaHasta := TO_SECONDDATE(:p_Fecha||' 23:59:59','YYYY-MM-DD HH24:MI:SS');
		
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" TAS
		INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
			ON (TAS."IdFuente" = FU."IdFuente" AND FU."Codigo" = p_CodigoFuente)
		WHERE (TAS."FechaSolicitud" BETWEEN v_FechaDesde AND v_FechaHasta)
		  AND TAS."IdEmpresa"	= p_IdEmpresa
		  AND TAS."Borrado"		= 'N'
		  AND FU."IdEmpresa" 	= p_IdEmpresa
		  AND FU."IdEstado"		= 'ACT';
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle = select 'EXISTE ARCHIVO DE TASAS GENERADO PARA MISMO DIA Y FUENTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;*/
		
		
		-- Validacion Mesas RF esten cerradas para solicitar tasas
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" CME
		WHERE CME."IdEstadoMesa" = 'A'
		  AND "IdTipoProducto" = 'MERF'
		  AND CME."IdEmpresa" = :p_IdEmpresa;
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle = select 'MESAS RF DEBEN ESTAR CERRADAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		-- Validacion Modulos cerrados para solicitar tasas
		SELECT "Valor" INTO v_ValidaModulos FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
		WHERE "IdEmpresa" = :p_IdEmpresa
		AND "CodigoParametro" = 'PLA.USAVALIDACIONMODULOSTASAS';
	
		IF(v_ValidaModulos = 'S') THEN
			SELECT COUNT(1) INTO v_CountModulos FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
            WHERE "CodigoModulo" IN('ORD','MRF','CUS') 
            AND "IdEstado" = 'ABT';
              
            IF (v_CountModulos > 0) THEN
            	p_out_ErroresDetalle = select 'MODULOS ORDEN, MESA RF Y CUSTODIA DEBEN ESTAR CERRADOS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
            END IF;
        END IF;
		
        --Validacion de Mercado
        IF(:p_Mercado <> '') THEN
        	SELECT COUNT(1) INTO v_CountMercado FROM "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" 
        	WHERE "IdClasificacionTipoInstrumento" = :p_Mercado;
        	
        	 IF (v_CountMercado = 0) THEN
            	p_out_ErroresDetalle = select 'MERCADO NO EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
            END IF;
        END IF;
		
		--------------------
		--LOGICA
		--------------------
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaHoraSistema);
		
		-- Se obtiene la misma FechaHoraSistema que se muestra en la UI
    	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
			p_IdEmpresa					=> p_IdEmpresa,
			p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
			p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
			p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
			p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
			p_out_FechaHoraUTC			=> v_FechaHoraUTC,
			p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		--v_FechaHoraUI := p_Fecha || ' ' || TO_NVARCHAR(v_FechaHoraSistema,'HH24:MI:SS');
		v_FechaHoraUI := p_Fecha || ' ' || TO_NVARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI:SS');
		
		
		-- Ruta Archivo
		SELECT
			(CASE WHEN (COALESCE(CP."Valor",'') <> '' AND COALESCE(CT."Valor",'') <> '') THEN CP."Valor" || CT."Valor" ELSE '' END) INTO v_RutaArchivo
			--'C:\\PROYECTOS\\' INTO v_RutaArchivo -- para las pruebas
		FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarConfiguracionTasas" CT
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" CP
			ON (UCASE(CP."CodigoParametro") = 'PLA.RUTABASEARCHIVOS' AND CT."IdEmpresa" = CP."IdEmpresa")
		WHERE CT."IdEmpresa" = p_IdEmpresa
		  AND CT."NombreEntidad" = 'RUTAARCHIVOTASAS';
		
		-- Nombre Archivo
		SELECT
			p_CodigoFuente || '_' || REPLACE(EMP."DocumentoIdentidad",'-','') || TO_NVARCHAR(p_Fecha,'YYYYMMDD') || '.xml' INTO v_NombreArchivo
			--p_CodigoFuente || '_' || REPLACE(EMP."DocumentoIdentidad",'-','') || TO_NVARCHAR(p_Fecha,'YYYYMMDD') || TO_NVARCHAR(v_FechaHoraSistema,'HH24MISS') || '.xml' INTO v_NombreArchivo
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
		WHERE EMP."IdEmpresa" = p_IdEmpresa;
		
		-- Servicio
		SELECT CT."Valor" INTO v_Servicio
		FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarConfiguracionTasas" CT
		WHERE CT."IdEmpresa" = p_IdEmpresa
		  AND CT."NombreEntidad" = 'SERVICIOCONSULTARTASAS';
		
		-- Fuente
		SELECT FU."IdFuente" INTO v_IdFuente
		FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
		WHERE FU."Codigo" = p_CodigoFuente 
		  AND FU."IdEmpresa" = p_IdEmpresa;
		
		
		--**********************************
		--********** INSTRUMENTOS **********
		--**********************************
		
		--Se obtienen Nemotecnicos repetidos solo si son PAGARE R.
		--Si estan repetidos y no son PAGARE R, no se consideran (data incorrecta).
		v_NemotecnicosRepetidos = 
			SELECT
				"Nemotecnico", count(*) AS "Cantidad"
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas" INS
			WHERE "NemotecnicoAux" = 'PAGARE R'
			AND ("IdClasificacionTipoInstrumento" = :p_Mercado OR COALESCE(:p_Mercado,'') = '')
			GROUP BY "Nemotecnico"
			HAVING count(*) > 1;
		
		--Se obtienen Nemotecnicos no repetidos
		v_NemotecnicosNoRepetidos = 
			SELECT
				"Nemotecnico", count(*) AS "Cantidad"
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas" INS
			WHERE ("IdClasificacionTipoInstrumento" = :p_Mercado OR COALESCE(:p_Mercado,'') = '')
			GROUP BY "Nemotecnico"
			HAVING count(*) < 2;
		
		--Nemotecnicos para el archivo
		v_Instrumentos =
			SELECT
				DISTINCT(INS."Nemotecnico") AS "Nemotecnico",
				 0 AS "IdInstrumento",
				INS."IdClasificacionTipoInstrumento",
				INS."IdTipoInstrumento",
				INS."IdMoneda"
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas" INS
			INNER JOIN (SELECT "Nemotecnico" FROM :v_NemotecnicosRepetidos
						UNION ALL
						SELECT "Nemotecnico" FROM :v_NemotecnicosNoRepetidos) NEM
				ON (INS."Nemotecnico" = NEM."Nemotecnico")
			INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" MES
				ON (INS."IdCliente" = MES."IdCliente" AND 
					INS."IdEmpresa" = MES."IdEmpresa" AND 
					MES."IdEstadoMesa" = 'C' AND MES."IdEstadoRegistro" = 'ACT' AND 
					MES."IdTipoProducto" = 'MERF')
			WHERE INS."IdEmpresa" = p_IdEmpresa
			AND (INS."IdClasificacionTipoInstrumento" = :p_Mercado OR COALESCE(:p_Mercado,'') = '');
		
		
		-- Validacion si existen Instrumentos para generar el Archivo
		SELECT COUNT(1) INTO v_Count FROM :v_Instrumentos;
		
		IF(v_Count < 1) THEN
			p_out_ErroresDetalle = select 'NO EXSITEN DATOS DE INSTRUMENTOS PARA GENERAR ARCHIVO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		select 'v_Instrumentos', * from :v_Instrumentos;
		
		
		--Se eliminan todas las Tasas de misma Fecha y Fuente que no están OFICIALIZADAS ni PUBLICADAS
		DELETE FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
		WHERE TAS."IdPrecioTasa" IN (
			SELECT TAS."IdPrecioTasa" 
			FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
			INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
				ON (TAS."IdFuente" = FU."IdFuente" AND FU."Codigo" = :p_CodigoFuente)
			WHERE TO_VARCHAR(TAS."FechaSolicitud",'YYYY-MM-DD') = :p_Fecha
			  AND TAS."IdEmpresa" = :p_IdEmpresa
			  AND TAS."Borrado" = 'N'
			  AND TAS."IdEstado" NOT IN ('OFI','PUB')
			  AND FU."IdEmpresa" = :p_IdEmpresa
			  AND FU."IdEstado" = 'ACT');
		
		
		-- Se crean registros tabla PrecioTasa
		INSERT INTO "BSCL.Plataforma.BD::TAS.PrecioTasa"(
			"IdPrecioTasa",
		    "IdFuente",
		    "IdTipoInstrumento",
			"IdInstrumento",
			"Nemotecnico",
			"IdTipoTasa",
			"Tasa",
			"TasaEstimada",
			"IdEstado",
			"IdEmpresa",
			"IdUsuarioCreador",
			"FechaSolicitud",
			"FechaIngreso",
			"FechaCreacion",
			"FechaModificacion",
			"RutaArchivo",
			"NombreArchivo",
			"TipoRegistro",
			"IdMoneda",
			"IdClasificacionTipoInstrumento"
			)
		SELECT
			"BSCL.Plataforma.BD.Secuencias::TAS.IdPrecioTasa".NEXTVAL,
			v_IdFuente,
			INS."IdTipoInstrumento",
			INS."IdInstrumento",
			INS."Nemotecnico",
			'TASA',
			0,
			0,
			'CRA',
			p_IdEmpresa,
			p_IdUsuario,
			:v_FechaHoraUI,
			--:v_FechaHoraSistema,
			--:v_FechaHoraSistemaLOCAL,
			:v_FechaHoraSistema,
			CURRENT_UTCTIMESTAMP,
			CURRENT_UTCTIMESTAMP,
			v_RutaArchivo,
			v_NombreArchivo,
			'TASA',
			INS."IdMoneda",
			INS."IdClasificacionTipoInstrumento"
		FROM :v_Instrumentos INS
		ORDER BY INS."Nemotecnico";
		
		SELECT ::ROWCOUNT INTO v_RowCount FROM DUMMY;
		-------------------- INSCRIPCION DE TAREA EN JOB-MANAGER --------------------
		
		-- Genera parámetros para la solicitud(parámetros de entrada para el Job)
 	 	v_TablaParametros = 
 	 		SELECT 'IdEmpresa'		AS "NombreParametro", TO_NVARCHAR(p_IdEmpresa)		AS "Valor" FROM DUMMY
 	 		UNION ALL
 	 		SELECT 'Fecha'			AS "NombreParametro", TO_NVARCHAR(p_Fecha)			AS "Valor" FROM DUMMY
 	 		UNION ALL
 	 		SELECT 'Fuente'			AS "NombreParametro", TO_NVARCHAR(p_CodigoFuente)	AS "Valor" FROM DUMMY
 	 		UNION ALL
 	 		SELECT 'RutaArchivo'	AS "NombreParametro", TO_NVARCHAR(v_RutaArchivo)	AS "Valor" FROM DUMMY
			UNION ALL
			SELECT 'NombreArchivo'	AS "NombreParametro", TO_NVARCHAR(v_NombreArchivo)	AS "Valor" FROM DUMMY
			UNION ALL
 	 		SELECT 'Servicio'		AS "NombreParametro", TO_NVARCHAR(v_Servicio)		AS "Valor" FROM DUMMY
 	 		;
 	 	
 	 	
 	 	-- ***** Ver si agregarlo a una tabla de configuracion
		CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"(
		     p_IdEmpresa     			=> :p_IdEmpresa
			,p_IdUsuario    			=> :p_IdUsuario
			,p_IdModulo    				=> :p_IdModulo					
		 	,p_IdAgenda	    			=> 0						-- *****
			,p_IdTipoTarea				=> 'GENERAR_ARCHIVO_TASAS'	-- *****	-- Job a ejecutar
			,p_Segundos	    			=> 0						-- *****	-- En cuantos segundos más se ejecuta
		    ,p_TablaParametros 			=> :v_TablaParametros
		 	,p_CreaParam				=> 'S'						-- *****	-- S/N indica si vienen parámetros
			,p_out_IdPrograma 			=> :v_out_IdPrograma		-- Id el job creado
			,p_out_AdvertenciasContador => v_AdvertenciasContador
			,p_out_AdvertenciasDetalle 	=> v_AdvertenciasDetalle
			,p_out_ErroresContador 		=> v_ErroresContador
			,p_out_ErroresDetalle 		=> v_ErroresDetalle
		);

		p_out_AdvertenciasDetalle 	= SELECT * FROM :v_AdvertenciasDetalle UNION ALL SELECT * FROM :p_out_AdvertenciasDetalle;
		p_out_ErroresDetalle 		= SELECT * FROM :v_ErroresDetalle UNION ALL SELECT * FROM :p_out_ErroresDetalle;
		
		IF (:v_ErroresContador > 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		-- RESULTADO PROCESO
		p_out_ResultadoProceso = 
			SELECT "NombreParametro", "Valor" FROM :v_TablaParametros
			UNION ALL
			SELECT 'CantidadRegistros' AS "NombreParametro", TO_CHAR(v_RowCount) AS "Valor" FROM DUMMY
			UNION ALL
			SELECT 'RutaTasaArchivo' AS "NombreParametro", CT."Valor" AS "Valor"
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarConfiguracionTasas" CT
			WHERE CT."IdEmpresa" = p_IdEmpresa
			  AND CT."NombreEntidad" = 'RUTAARCHIVOTASAS';
		
  	END;
END;