SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.CrearProgramaGenerarCertificadosMasivos";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.CrearProgramaGenerarCertificadosMasivos"
( 
    -- Parametros de Entrada
    IN p_IdModulo					          NVARCHAR(3),
    IN p_IdUsuario							  BIGINT, 
    IN p_IdEmpresa  						  BIGINT,
     
    IN p_IdTipoDocumento  					  NVARCHAR(10) DEFAULT '',
    IN p_FechaInicio        				  NVARCHAR(10) DEFAULT '0001-01-01',
    IN p_FechaTermino          				  NVARCHAR(10) DEFAULT '9999-01-01',
    IN p_IdCliente							  BIGINT DEFAULT 0,
    --Tabla de parametros
	IN  p_TablaParametros		  TABLE("Tipo" 						NVARCHAR(40), ---BATCH/SOLICITUD/ESPECIFICO
										"NombreParametro" 			NVARCHAR(100), 
										"Valor" 					NVARCHAR(100)),-- parametro estandar
    -- Salida
    OUT p_out_resultado						  BIGINT,
   
   -- Parametros de Salida
   OUT p_out_AdvertenciasContador      INT,
   OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   OUT p_out_ErroresContador           INT,   
   OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
         
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
  
    
     DECLARE v_SecuenciaSolicitud		INT DEFAULT 0; 
     DECLARE v_idDoc, v_idDocDJ		    INT DEFAULT 0; 
     
	 DECLARE v_IdNormativa 								NVARCHAR(100) DEFAULT '';
	 DECLARE v_Esquema,v_Version, v_FechaVersion		NVARCHAR(10) DEFAULT '';
	 DECLARE v_IdTipoProgramaExportacion	  			NVARCHAR(40)  DEFAULT '';
	 
	 DECLARE v_count,v_countDocumento 					INT:=0;
	 DECLARE v_CreaParam		   		NVARCHAR(1):='S';
	 
     DECLARE v_FechaPresentacion,v_FechaLimite, v_param NVARCHAR(100):='';
     DECLARE v_EsPauta						NVARCHAR(100):='N';
     DECLARE v_ExtensionSalida				NVARCHAR(5):='';
     DECLARE v_ComprimirSalida,v_TitulosSalida		NVARCHAR(2):='N';
     DECLARE v_LineasPorSalida				BIGINT:=0;
   	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100),"Valor" NVARCHAR(5000));
	DECLARE v_erroroutmsg  				NVARCHAR(5000);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));
    DECLARE v_DocIdentidad  			NVARCHAR(100);
	DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';	
	DECLARE v_FechaVencimientoSolicitud			 		 NVARCHAR(10) DEFAULT '1900-01-01';        	
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
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	
	
	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
	

	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;  	
	p_out_ResultadoProceso			= SELECT '' as "Campo", 0 as "Id", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
	v_ErroresDetalle				= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;

	select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;	
		
		
	--------------------
	--VALIDACION
	--------------------

	/* parametros batch  */
	SELECT COUNT(1) INTO v_count FROM  :p_TablaParametros WHERE "Tipo"='BATCH';
	IF v_count =0 THEN 
	  	p_out_ErroresDetalle =  select 'PARAMETROS_NO_CONFIGURADOS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	END IF;
	

	
	/* parametros Solicitud  */
	SELECT COUNT(1) INTO v_count FROM  :p_TablaParametros PAR WHERE PAR."Tipo" IN  ('SOLICITUD','ESPECIFICO')
	AND NOT EXISTS (SELECT 1 FROM  "BSCL.Normativa.BD::NOR.TipoParametros" TIP
	 				WHERE TRIM(TIP."TipoParametro")=TRIM(PAR."NombreParametro"));
	IF v_count >0 THEN 
		
		SELECT TOP 1 TRIM(PAR."NombreParametro") INTO v_param FROM  :p_TablaParametros PAR WHERE PAR."Tipo" IN  ('SOLICITUD','ESPECIFICO')
			AND NOT EXISTS (SELECT 1 FROM  "BSCL.Normativa.BD::NOR.TipoParametros" TIP
	 				WHERE TRIM(TIP."TipoParametro")=TRIM(PAR."NombreParametro"));
	 			p_out_ErroresDetalle =  select 'PARAMETROS_NO_VALIDOS' || v_param  as "Codigo",:v_erroroutmsg  as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	END IF;
	

	
	/* Obtencion de valores comunes */
	
	SELECT COUNT(1) INTO v_count 
	
			FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
			WHERE "IdTipoDocumento" = :p_IdTipoDocumento;

	IF(v_count=0) THEN
				p_out_ErroresDetalle =  select 'TIPO_DOCUMENTO_NO_CONFIGURADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	  ELSE
	
	  	SELECT TOP 1 "IdNormativa", "Esquema","IdTipoProgramaExportacion" INTO v_IdNormativa,v_Esquema,v_IdTipoProgramaExportacion
			FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
			WHERE "IdTipoDocumento" = p_IdTipoDocumento;
	
		---Obtener version del esquema	
		
		IF p_FechaInicio='' OR p_FechaInicio='0001-01-01' THEN
			v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
		ELSE --Se obtiene segun fecha 
			v_FechaVersion :=TO_VARCHAR (p_FechaInicio, 'DD-MM-YYYY');
		END IF;	
		/*
		SELECT COUNT(1) INTO v_count FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);

		IF(v_count=0) THEN
				p_out_ErroresDetalle =  select 'VERSION_ESQUEMA_NO_CONFIGURADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	  	ELSE
	  		SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);
  		END IF;
  		*/
		  SELECT COUNT(1) INTO v_count FROM  :p_TablaParametros WHERE "Tipo"='BATCH' AND "NombreParametro"='VersionArchivo';
		IF v_count =0 THEN 
		  	p_out_ErroresDetalle =  select 'PARAMETROS_VERSION_ARCHIVO_NO_CONFIGURADOS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA; 	
					ELSE
					SELECT TOP 1 "Valor" INTO v_Version FROM  :p_TablaParametros WHERE "Tipo"='BATCH' AND "NombreParametro"='VersionArchivo';
		END IF;
	

	END IF;
		
	call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
	);	
		
	
	------------------------------------------------------------
	-- OBTENER DOCUMENTO EXISTENTE
	------------------------------------------------------------


		SELECT COUNT(1) INTO v_countDocumento FROM "BSCL.Normativa.BD::DOC.Documento"
		 WHERE "IdTipoDocumento"=p_IdTipoDocumento
		 AND  "Esquema"=v_Esquema
		 AND  "Version"=v_Version
		 AND  "IdEmpresa"=p_IdEmpresa;
		
		IF v_countDocumento=0 THEN
			select "BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL into v_idDoc from dummy;
			ELSE
				SELECT TOP 1 "IdDocumento" INTO v_idDoc FROM "BSCL.Normativa.BD::DOC.Documento"
				 WHERE "IdTipoDocumento"=p_IdTipoDocumento
				 AND  "Esquema"=v_Esquema
		   	     AND  "Version"=v_Version
				 AND  "IdEmpresa"=p_IdEmpresa ORDER BY "IdDocumento" DESC;
		END IF;
		--Documento de la DJ asociada
		SELECT COUNT(1) INTO v_countDocumento FROM "BSCL.Normativa.BD::DOC.Documento"
		 WHERE "IdTipoDocumento"='DECLACORR'
		 AND  "Esquema"=v_Esquema
		 AND  "Version"=v_Version
		 AND  "IdEmpresa"=p_IdEmpresa;
		IF v_countDocumento=0 THEN
			v_idDocDJ=v_idDoc;
			ELSE
				SELECT TOP 1 "IdDocumento" INTO v_idDocDJ FROM "BSCL.Normativa.BD::DOC.Documento"
				 WHERE "IdTipoDocumento"='DECLACORR'
				 AND  "Esquema"=v_Esquema
		   	     AND  "Version"=v_Version
				 AND  "IdEmpresa"=p_IdEmpresa ORDER BY "IdDocumento" DESC;
		END IF;
		
		------------------------------------------------------------
	-- CREAR SOLICITUD
	------------------------------------------------------------

		SELECT "BSCL.Normativa.BD.Secuencias::SolicitudCircular".NEXTVAL INTO v_SecuenciaSolicitud FROM dummy;


		insert into "BSCL.Normativa.BD::NOR.SolicitudCircular"
		(
		"IdSolicitudCircular",
		"IdTipoCircular",
		--"IdEmpresa",
		"IdDocumento",			
		"FechaIngresoSolicitud",			
		"FechaVencimientoSolicitud",	
		"IdUsuario",						
		"EstadoSolicitud",				
	    "Observaciones",
	    "IdTipoDocumento",						
		"IdCliente",						
		"FechaInicio",					
		"FechaFin",						
		"ListaRut",						
		"ListaRutAut", 					
		"ListaNemo",	
		"ListaRutEmpl",					
		"Periodicidad",					
		"RutaArchivo",					
		"NombreArchivo",					
		"NumeroRegistros",
		"EsPauta",
		"FechaIngreso",
		"FechaCreacion",
		"FechaModificacion",
		"IdEstado" )				
		(
		select
		v_SecuenciaSolicitud as IdSolicitudCircular,
		v_IdNormativa,
		v_idDoc,
		v_fechaHoraSistema,
		TO_VARCHAR (CURRENT_UTCTIMESTAMP),
		p_IdUsuario,
		'Pendiente',
		'',
		p_IdTipoDocumento,
		p_IdCliente,
		p_FechaInicio,
		p_FechaTermino,
		0,
		0,
		0,
		0, 
		'',
		'',
		'',
		0,
		'N',
		v_fechaHoraSistema,
		CURRENT_UTCTIMESTAMP,
		CURRENT_UTCTIMESTAMP,
		'PEN'
		from dummy
		);
		
		
			------------------------------------------------------------
	-- AGREGAR PARAMETROS A LA SOLICITUD SOLICITUD
	------------------------------------------------------------

	/*Comunes*/
	INSERT INTO "BSCL.Normativa.BD::NOR.SolicitudParametros" ("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
	(SELECT "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'EMP', p_IdEmpresa from DUMMY);
	
	SELECT COUNT(1) INTO v_count FROM  :p_TablaParametros WHERE "Tipo" = 'SOLICITUD';
	IF v_count >0 THEN 
		
		INSERT INTO "BSCL.Normativa.BD::NOR.SolicitudParametros" (
		  "IdSolicitudParametros","IdSolicitudCircular","TipoParametro","Valor")
		SELECT
		  "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL,
		  :v_SecuenciaSolicitud,"NombreParametro",TRIM("Valor")
		FROM :p_TablaParametros
		WHERE "Tipo"='SOLICITUD';
        
		UPDATE s
		SET
		  "OrigenIdSistema" = 'IdInstrumento',
		  "IdSistema" = i."IdInstrumento"
		FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" AS s
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS i ON (i."Nemotecnico" = s."Valor")
		WHERE s."TipoParametro" IN ('NEMO','NEMO_MAS')
		AND s."IdSolicitudCircular" = :v_SecuenciaSolicitud;
		
		UPDATE s
        SET
          "OrigenIdSistema" = 'IdCliente',
          "IdSistema" = c."IdCliente"
        FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" AS s
        LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS c ON (c."DocumentoIdentidad" = s."Valor"
                                                                           AND c."TipoDocumentoIdentidad" = 'R')
        WHERE s."TipoParametro" IN ('RUT','RUT_MAS')
        AND s."IdSolicitudCircular" = :v_SecuenciaSolicitud;
        
        UPDATE s
        SET
          "OrigenIdSistema" = 'IdPersona',
          "IdSistema" = c."IdPersona"
        FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" AS s
        LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS c ON (c."DocumentoIdentidad" = s."Valor"
                                                                           AND c."TipoDocumentoIdentidad" = 'R')
        WHERE s."TipoParametro" IN ('RUTEMPL','RUTEMPL_MAS')
        AND s."IdSolicitudCircular" = :v_SecuenciaSolicitud;
        
	END IF;
		
		/*Especiales*/
	IF  p_IdCliente <>0 THEN
		SELECT top 1 (ltrim(rtrim(substring("DocumentoIdentidad",1,LENGTH("DocumentoIdentidad")-2)))) into v_DocIdentidad from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente" = p_IdCliente;
		INSERT  INTO  "BSCL.Normativa.BD::NOR.SolicitudParametros" ("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
		(SELECT "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RUT', v_DocIdentidad from dummy);
		
		INSERT  INTO  "BSCL.Normativa.BD::NOR.SolicitudParametros" ("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")		
		(SELECT "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'IDCLI', IFNULL(p_IdCliente,0) from dummy);
	END IF; 
	
  	SELECT COUNT(1) INTO v_count FROM  :p_TablaParametros WHERE "Tipo"='ESPECIFICO'
  	AND "NombreParametro" IN ('RUT_MAS','RUTAUT_MAS','RUTEMPL_MAS','RUT','RUTEMPL','RUTAUT') ;
	IF v_count >0 THEN 
		
		INSERT INTO "BSCL.Normativa.BD::NOR.SolicitudParametros" ("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
		SELECT "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,TRIM("NombreParametro"), (ltrim(rtrim(substring(TRIM("Valor"),1,LENGTH(TRIM("Valor"))-2))))
		FROM  :p_TablaParametros WHERE "Tipo"='ESPECIFICO'
  		AND "NombreParametro" IN ('RUT_MAS','RUTAUT_MAS','RUTEMPL_MAS','RUT','RUTEMPL','RUTAUT') ;
		
	END IF;
	------------------------------------------------------------
	-- CREAR NUEVO DOCUMENTO 
	------------------------------------------------------------

	IF v_countDocumento=0 THEN
		INSERT INTO "BSCL.Normativa.BD::DOC.Documento"
		(	"IdDocumento",
	    	"IdTipoDocumento",
		    "Esquema",
		    "Version",
		    "IdEmpresa",
		    "FechaLimite",
		    "IdEstadoDocumento",
		    "FechaCreacion",
		    "FechaPresentacion",
		    "DocumentoIdentidadDeclarante",
		    "DocumentoIdentidadRepresentante",
		    "DireccionElectronicaDeclaracion",
		    "DireccionFisicaDeclaracion",
		    "ComunaDeclaracion",
		    "TelefonoDeclaracion",
		    "FolioDeclaracion",
		    "EnProceso",
		    "FechaInicioUltimoProceso"	   
			)
		
		select
		    v_idDoc as "IdDocumento",
		    p_IdTipoDocumento as "IdTipoDocumento", 
		    v_Esquema as "Esquema",            
		    v_Version as "Version",                 
		    p_IdEmpresa as "IdEmpresa",                    
		    '1900-01-01' as "FechaLimite",       
		    '' as "IdEstadoDocumento",        
		    CURRENT_UTCTIMESTAMP as "FechaCreacion",     
		    '1900-01-01' as "FechaPresentacion", 
		    '' as "DocumentoIdentidadDeclarante",
		    '' as "DocumentoIdentidadRepresentante",
		    '' as "DireccionElectronicaDeclaracion",
		    '' as "DireccionFisicaDeclaracion",
		    '' as "ComunaDeclaracion",
		    '' as "TelefonoDeclaracion",
		    '' as "FolioDeclaracion",
		    'N' as "EnProceso",
		    null as "EnProcesoDesde"
		from dummy;
	END IF;
	
	
		------------------------------------------------------------
	-- AGREGAR TAREA EN BATCH 
	------------------------------------------------------------
	--BATCH DEFAULT
	v_TablaBatchDefecto=
	SELECT 'NOMSAL'  AS "NombreParametro",''	AS "Valor" FROM DUMMY
	UNION ALL
	SELECT 'ZIPSAL'  AS "NombreParametro",'' 	AS "Valor" FROM DUMMY
	UNION ALL
	select 'EsPauta' AS "NombreParametro",'N' 	AS "Valor" FROM DUMMY
	UNION ALL
	SELECT 'EXTSAL'  AS "NombreParametro",'' 	AS "Valor" FROM DUMMY
	UNION ALL
	SELECT 'COMSAL'  AS "NombreParametro",'N' 	AS "Valor" FROM DUMMY
	UNION ALL
	SELECT 'NOMCOLSAL'  AS "NombreParametro",'N' 	AS "Valor" FROM DUMMY
	UNION ALL 
	SELECT 'LINPARSAL'  AS "NombreParametro",'0' 	AS "Valor" FROM DUMMY
	UNION ALL 
	SELECT 'DESCCOL'  AS "NombreParametro",'0' 	AS "Valor" FROM DUMMY;
	

	
	--TIPO DOCUMENTO
	v_TablaParamDocumento=
	SELECT  'IdSolicitudCircular' 	AS  "NombreParametro",to_varchar(v_SecuenciaSolicitud) 	AS "Valor" FROM DUMMY
	UNION ALL
	SELECT 'idDocumento' 			AS "NombreParametro", to_varchar(v_idDoc) 				AS "Valor" FROM DUMMY;
	
	
	--DESDE LA UI
	v_TablaParamUI=
	SELECT "NombreParametro" 		AS "NombreParametro", "Valor" 						AS "Valor" 
	FROM :p_TablaParametros WHERE "Tipo"='BATCH';
	
	
		v_TablaParametroBatch=
	SELECT "NombreParametro" 				AS "NombreParametro", "Valor" 					AS "Valor" 
	FROM :v_TablaParamUI
	UNION ALL 
		SELECT DEF."NombreParametro" 		AS "NombreParametro", DEF."Valor" 				AS "Valor" 
		FROM :v_TablaParamDocumento DEF
		WHERE NOT EXISTS (SELECT 1 FROM :v_TablaParamUI BAT WHERE DEF."NombreParametro"=BAT."NombreParametro");
	
	
	--Incluir valores por defecto
	v_TablaParametroBatch = SELECT * FROM :v_TablaParametroBatch 
	UNION ALL SELECT DEF.* FROM :v_TablaBatchDefecto DEF
				 WHERE NOT EXISTS (SELECT 1 FROM :v_TablaParametroBatch BAT WHERE DEF."NombreParametro"=BAT."NombreParametro");
	
	
	CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
	(p_IdEmpresa,p_IdUsuario,p_IdModulo,0,v_IdTipoProgramaExportacion,0,:v_TablaParametroBatch,v_CreaParam,p_out_IdPrograma,p_out_AdvertenciasContador,p_out_AdvertenciasDetalle,p_out_ErroresContador,p_out_ErroresDetalle);
	
	INSERT INTO "BSCL.Normativa.BD::TRI.ResumenGeneracion"
	(ID, "IdProceso", "IdDocumento", "IdUsuario", "Fecha", "Glosa", "TotalRegistrosGenerados", "NombreArchivo", "UltimoCorrelativo")
	SELECT  TOP 1 (SELECT max (ID)+ 1  FROM "BSCL.Normativa.BD::TRI.ResumenGeneracion") AS ID
	, "IdPrograma" 	AS "IdProceso"
	, v_idDocDJ		AS "IdDocumento"
	, p_IdUsuario	AS "IdUsuario"
	, CURRENT_UTCTIMESTAMP 	AS "Fecha"
	, 'Encolada' 	AS  "Glosa"
	, 0 			AS "TotalRegistrosGenerados"
	, '' 			AS "NombreArchivo"
	, 0 			AS "UltimoCorrelativo"
	FROM :p_out_IdPrograma;

	
p_out_AdvertenciasDetalle		= SELECT "IdPrograma" as "Codigo",'' as "RequiereAutorizacion",'1' as "Descripcion" FROM :p_out_IdPrograma;
END;