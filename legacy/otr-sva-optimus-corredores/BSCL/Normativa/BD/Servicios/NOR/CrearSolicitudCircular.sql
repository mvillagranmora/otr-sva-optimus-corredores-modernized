SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudCircular";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudCircular"
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
     DECLARE v_idDoc 				    INT DEFAULT 0; 
     DECLARE v_NombreTandem,v_NombreTandemCUS NVARCHAR(100):='';
     DECLARE v_CodigoCorredora NVARCHAR(40):='';
	 DECLARE v_IdNormativa 								NVARCHAR(100) DEFAULT '';
	 DECLARE v_Esquema,v_Version, v_FechaVersion,v_FechaOperacion		NVARCHAR(10) DEFAULT '';
	 DECLARE v_IdTipoProgramaExportacion	  			NVARCHAR(40)  DEFAULT '';
	 
	 DECLARE v_count,v_CountCUS,v_countDocumento,v_countNemo 			INT:=0;
	 DECLARE v_CreaParam		   		NVARCHAR(1):='S';
	 
     DECLARE v_FechaPresentacion,v_FechaLimite, v_param NVARCHAR(100):='';
     DECLARE v_EsPauta						NVARCHAR(100):='N';
     DECLARE v_ExtensionSalida				NVARCHAR(5):='';
     DECLARE v_ComprimirSalida,v_TitulosSalida		NVARCHAR(2):='N';
     DECLARE v_LineasPorSalida				BIGINT:=0;
     
     DECLARE v_PautaNCG380 INT:=0;
     DECLARE v_FechaEjecucion NVARCHAR(100):='';
     DECLARE v_NombreArchivo NVARCHAR(100):='';
     DECLARE v_EstructuraNombre NVARCHAR(100):='';
     DECLARE v_FechaParametro NVARCHAR(1):='N';
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
			WHERE "IdTipoDocumento" = p_IdTipoDocumento;

	IF(v_count=0) THEN
				p_out_ErroresDetalle =  select 'TIPO_DOCUMENTO_NO_CONFIGURADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	  ELSE
	  
	  	SELECT TOP 1 "IdNormativa", "Esquema","IdTipoProgramaExportacion","ExtensionSalida","ComprimirSalida","TitulosSalida", "LineasPorSalida" INTO v_IdNormativa,v_Esquema,v_IdTipoProgramaExportacion,v_ExtensionSalida,v_ComprimirSalida, v_TitulosSalida, v_LineasPorSalida
			FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
			WHERE "IdTipoDocumento" = p_IdTipoDocumento;
	
		---Obtener version del esquema	
		
		IF p_FechaInicio='' OR p_FechaInicio='0001-01-01' THEN
			v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
		ELSE --Se obtiene segun fecha 
			v_FechaVersion :=TO_VARCHAR (p_FechaInicio, 'DD-MM-YYYY');
		END IF;	
		SELECT COUNT(1) INTO v_count FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);

		IF(v_count=0) THEN
				p_out_ErroresDetalle =  select 'VERSION_ESQUEMA_NO_CONFIGURADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	  	ELSE
	  		SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);
  		END IF;

	END IF;
		
	call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
	);	
	

	SELECT "BSCL.Normativa.BD.Secuencias::SolicitudCircular".NEXTVAL INTO v_SecuenciaSolicitud FROM dummy;

	------------------------------------------------------------
	-- PARAMETRO ESTANDAR DE LA SOLICITUD
	------------------------------------------------------------
	SELECT COUNT(1) INTO v_count FROM  :p_TablaParametros WHERE "Tipo"='SOLICITUD'
  	AND "NombreParametro" IN ('FechaPresentacion','FechaLimite') ;
	IF v_count >0 THEN 
		
				p_tablaParametroGrupo =
						SELECT 			 MAX( CASE WHEN SOL."NombreParametro"='FechaPresentacion' 				THEN SOL."Valor" ELSE NULL END) AS "FechaPresentacion"
										,MAX( CASE WHEN SOL."NombreParametro"='FechaLimite'		  				THEN SOL."Valor" ELSE NULL END) AS "FechaLimite"
										,MAX( CASE WHEN SOL."NombreParametro"='EsPauta'		  	  				THEN SOL."Valor" ELSE NULL END) AS "EsPauta"
										,MAX( CASE WHEN SOL."NombreParametro"='FechaVencimientoSolicitud'  	  	THEN SOL."Valor" ELSE NULL END) AS "FechaVencimientoSolicitud"
										
						 FROM  :p_TablaParametros SOL WHERE SOL."Tipo"='SOLICITUD'
					     AND SOL."NombreParametro" IN ('FechaPresentacion','FechaLimite') ;
						
				SELECT TOP 1 "FechaPresentacion","FechaLimite","EsPauta","FechaVencimientoSolicitud"
					   INTO v_FechaPresentacion,v_FechaLimite,v_EsPauta,v_FechaVencimientoSolicitud FROM :p_tablaParametroGrupo;
						
	END IF;


	------------------------------------------------------------
	-- OBTENER DOCUMENTO EXISTENTE
	------------------------------------------------------------


		SELECT COUNT(1) INTO v_countDocumento FROM "BSCL.Normativa.BD::DOC.Documento"
		 WHERE "IdTipoDocumento"=p_IdTipoDocumento
		 AND  "Esquema"=v_Esquema
		 AND  "Version"=v_Version
		 AND  "IdEmpresa"=p_IdEmpresa
		 AND  "FechaLimite"=TRIM(v_FechaLimite)
		 AND  "FechaPresentacion"=TRIM(v_FechaPresentacion);
		
		IF v_countDocumento=0 THEN
			select "BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL into v_idDoc from dummy;
			ELSE
				SELECT TOP 1 "IdDocumento" INTO v_idDoc FROM "BSCL.Normativa.BD::DOC.Documento"
				 WHERE "IdTipoDocumento"=p_IdTipoDocumento
				 AND  "Esquema"=v_Esquema
		   	     AND  "Version"=v_Version
				 AND  "IdEmpresa"=p_IdEmpresa
				 AND  "FechaLimite"=TRIM(v_FechaLimite)
				 AND  "FechaPresentacion"=TRIM(v_FechaPresentacion) ORDER BY "IdDocumento" DESC;
		END IF;
	
	------------------------------------------------------------------
	-- AGREGA PARAMETRO NOMSAL PARA SOLICITUD DESDE PAUTA PARA NCG380
	------------------------------------------------------------------
      -- Se guarda nombre de archivo para CSV de NCG380 generado desde pauta
  SELECT count(*) into v_PautaNCG380 FROM :p_TablaParametros WHERE "NombreParametro" = 'PAUTA_NCG380' AND "Valor" = 'S';
  IF v_PautaNCG380 > 0 THEN
    p_out_resultado = v_SecuenciaSolicitud;
    SELECT "Valor" into v_FechaEjecucion FROM :p_TablaParametros WHERE "NombreParametro" = 'FINI';
    SELECT (CASE WHEN ("Valor" = '' OR "Valor" IS NULL) THEN "ValorDefecto" ELSE "Valor" END) INTO v_EstructuraNombre FROM "BSCL.Normativa.BD::DOC.Parametro" WHERE UPPER("Esquema") = UPPER(p_IdTipoDocumento);
    IF :p_IdTipoDocumento = 'NCG380OPE' THEN 
   		v_FechaEjecucion := TO_VARCHAR(
						        ADD_SECONDS(
						            TO_DATE(v_fechaHoraSistema, 'YYYY-MM-DD HH24:MI:SS'),
						            SECONDS_BETWEEN(
						                UTCTOLOCAL(TO_TIMESTAMP(v_fechaHoraSistema, 'YYYY-MM-DD HH24:MI:SS'), 'America/Santiago', 'platform'),
						                TO_TIMESTAMP(v_fechaHoraSistema, 'YYYY-MM-DD HH24:MI:SS'))),'YYYY-MM-DD HH24:MI:SS');
	END IF;
    -- CELHALCONN-834 para scotia se entrega el nombre del archivo con un dia mas, via parametro para no afectar a otros clientes, por defecto sin modificar fecha.  
		SELECT COALESCE((
		    SELECT "Valor"
		      FROM "BSCL.Normativa.BD::NOR.Configuracion"
		     WHERE "CodigoParametro" = 'FECHA_NCG380'
		), 'N')
		INTO v_FechaParametro
		FROM DUMMY;
		
		IF v_FechaParametro = 'S' THEN
		   v_FechaEjecucion := ADD_DAYS(v_FechaEjecucion, 1);
		END IF;

    v_NombreArchivo = REPLACE(v_EstructuraNombre,'[D]',SUBSTRING(v_FechaEjecucion,9,2));
    v_NombreArchivo = REPLACE(v_NombreArchivo,'[M]',SUBSTRING(v_FechaEjecucion,6,2));
    v_NombreArchivo = REPLACE(v_NombreArchivo,'[A]',SUBSTRING(v_FechaEjecucion,1,4));
    v_NombreArchivo = REPLACE(v_NombreArchivo,'[H]',SUBSTRING(v_FechaEjecucion,12,2));
    v_NombreArchivo = REPLACE(v_NombreArchivo,'[MI]',SUBSTRING(v_FechaEjecucion,15,2));
    v_NombreArchivo = REPLACE(v_NombreArchivo,'[S]',SUBSTRING(v_FechaEjecucion,18,2));
      
    INSERT  INTO  "BSCL.Normativa.BD::NOR.SolicitudParametros" ("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")		
    (SELECT "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'NOMSAL', v_NombreArchivo FROM dummy);
     
     v_EsPauta = 'S';
  END IF;
  ---

	------------------------------------------------------------
	-- CREAR SOLICITUD
	------------------------------------------------------------



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
		v_FechaVencimientoSolicitud,
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
		TRIM(v_EsPauta),
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
		
	
	 /***	Busca parametro para copias en TANDEM	***/
	SELECT COUNT(1) INTO v_Count FROM :p_TablaParametros  DEF	WHERE DEF."Tipo"='SOLICITUD' AND DEF."NombreParametro" IN ('ENVIO_TANDEM') AND DEF."Valor"='S';	
	IF(v_Count>0) THEN
			SELECT COUNT(1) INTO v_Count FROM :p_TablaParametros  DEF	WHERE DEF."Tipo"='SOLICITUD' AND DEF."NombreParametro" IN ('FINI') ;	
			IF(v_Count>0) THEN
			   SELECT TO_VARCHAR (DEF."Valor", 'YYYY-MM-DD') INTO v_FechaOperacion  FROM :p_TablaParametros  DEF	WHERE DEF."Tipo"='SOLICITUD' AND DEF."NombreParametro" IN ('FINI') ;
				ELSE
				v_FechaOperacion=TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD');
			
			END IF;	
			 SELECT COUNT(1) INTO v_Count FROM "BSCL.Normativa.BD::DOC.TipoDocumento" T
			 INNER JOIN "BSCL.Normativa.BD::DOC.Parametro" P ON (P."Esquema"=T."Esquema" AND P."Codigo"='NOMBRE_ENVIO_TANDEM_SVS')
			 WHERE T."IdTipoDocumento"=p_IdTipoDocumento AND LENGTH(("Valor"))>0;
			
			IF(v_Count>0) THEN
				 SELECT TOP 1 P."Valor" INTO v_NombreTandem FROM "BSCL.Normativa.BD::DOC.TipoDocumento" T
				 INNER JOIN "BSCL.Normativa.BD::DOC.Parametro" P ON (P."Esquema"=T."Esquema" AND P."Codigo"='NOMBRE_ENVIO_TANDEM_SVS')
				 WHERE T."IdTipoDocumento"=p_IdTipoDocumento AND LENGTH(TRIM("Valor"))>0 ORDER BY P."Version" DESC;
				
				SELECT TOP  1 "CodigoCorredora" INTO v_CodigoCorredora  FROM  "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"			AS EMP
				 WHERE EMP."IdEmpresa"=p_IdEmpresa AND "IdBolsa"='BCS';
				
				--se arma nombre segun Parametro
				v_NombreTandem = REPLACE(v_NombreTandem,'[DIATRAN]',TO_VARCHAR (v_FechaOperacion, 'DD'));
				v_NombreTandem = REPLACE(v_NombreTandem,'[MESTRAN]',TO_VARCHAR (v_FechaOperacion, 'MM'));
				v_NombreTandem = REPLACE(v_NombreTandem,'[CODCORREDOR]',v_CodigoCorredora);
				p_TablaParametros= SELECT * FROM :p_TablaParametros
						UNION ALL SELECT 'SOLICITUD' AS "Tipo",'NOMBRE_ENVIO_TANDEM_SVS' 	AS "NombreParametro",v_NombreTandem	AS "Valor"	FROM DUMMY
						UNION ALL SELECT 'BATCH' AS "Tipo",'NOMBRE_ENVIO_TANDEM_SVS' 	AS "NombreParametro",v_NombreTandem	AS "Valor"	FROM DUMMY;
							
							
			END IF;			
		 
						
			 SELECT COUNT(1) INTO v_CountCUS FROM "BSCL.Normativa.BD::DOC.TipoDocumento" T
			 INNER JOIN "BSCL.Normativa.BD::DOC.Parametro" P ON (P."Esquema"=T."Esquema" AND P."Codigo"='NOMBRE_ENVIO_TANDEM_CUS')
			 WHERE T."IdTipoDocumento"=p_IdTipoDocumento AND LENGTH(("Valor"))>0;
			
			IF(v_CountCUS>0) THEN
				 SELECT TOP 1 P."Valor" INTO v_NombreTandemCUS FROM "BSCL.Normativa.BD::DOC.TipoDocumento" T
				 INNER JOIN "BSCL.Normativa.BD::DOC.Parametro" P ON (P."Esquema"=T."Esquema" AND P."Codigo"='NOMBRE_ENVIO_TANDEM_CUS')
				 WHERE T."IdTipoDocumento"=p_IdTipoDocumento AND LENGTH(TRIM("Valor"))>0 ORDER BY P."Version" DESC;
				
				SELECT TOP  1 "CodigoCorredora" INTO v_CodigoCorredora  FROM  "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"			AS EMP
				 WHERE EMP."IdEmpresa"=p_IdEmpresa AND "IdBolsa"='BCS';
				
				--se arma nombre segun Parametro
				v_NombreTandemCUS = REPLACE(v_NombreTandemCUS,'[DIATRAN]',TO_VARCHAR (v_FechaOperacion, 'DD'));
				v_NombreTandemCUS = REPLACE(v_NombreTandemCUS,'[MESTRAN]',TO_VARCHAR (v_FechaOperacion, 'MM'));
				v_NombreTandemCUS = REPLACE(v_NombreTandemCUS,'[CODCORREDOR]',v_CodigoCorredora);
				p_TablaParametros= SELECT * FROM :p_TablaParametros
						UNION ALL SELECT 'SOLICITUD' AS "Tipo",'NOMBRE_ENVIO_TANDEM_CUS' 	AS "NombreParametro",v_NombreTandemCUS	AS "Valor"	FROM DUMMY
						UNION ALL SELECT 'BATCH' AS "Tipo",'NOMBRE_ENVIO_TANDEM_CUS' 	AS "NombreParametro",v_NombreTandemCUS	AS "Valor"	FROM DUMMY;
				
				
		  		END IF;	
	END IF;		
	 /*** FIN: 	Busca parametro para copias en TANDEM	***/	

	
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
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipIns ON (tipIns."IdTipoInstrumento" = i."IdTipoInstrumento")
		LEFT JOIN "BSCL.Normativa.BD::NOR.SolicitudParametros" AS s2 ON s2."IdSolicitudCircular" = :v_SecuenciaSolicitud AND s2."TipoParametro" IN ('CLAINS','CLAINS_MAS')
		WHERE s."TipoParametro" IN ('NEMO','NEMO_MAS')
		AND s."IdSolicitudCircular" = :v_SecuenciaSolicitud 
		AND tipIns."IdClasificacionTipoInstrumento" = CASE WHEN COALESCE(s2."Valor", '') <> '' THEN s2."Valor" ELSE tipIns."IdClasificacionTipoInstrumento" END;
		
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
	
	SELECT COUNT(1)
	INTO v_countNemo
	FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" AS SP
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS I ON I."Nemotecnico" = SP."Valor"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TI ON TI."IdTipoInstrumento" = I."IdTipoInstrumento"
	LEFT JOIN "BSCL.Normativa.BD::NOR.SolicitudParametros" AS SP2 ON SP2."IdSolicitudCircular" = :v_SecuenciaSolicitud AND SP2."TipoParametro" IN ('CLAINS','CLAINS_MAS')
	WHERE SP."TipoParametro" IN ('NEMO','NEMO_MAS')
	AND SP."IdSolicitudCircular" = :v_SecuenciaSolicitud 
	AND TI."IdClasificacionTipoInstrumento" = CASE WHEN COALESCE(SP2."Valor", '') <> '' THEN SP2."Valor" ELSE TI."IdClasificacionTipoInstrumento" END;
	
	IF v_countNemo > 1 THEN
	
		INSERT INTO "BSCL.Normativa.BD::NOR.SolicitudParametros" ("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro", "Valor", "IdSistema", "OrigenIdSistema")
		SELECT "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, SP."IdSolicitudCircular", SP."TipoParametro", SP."Valor", I."IdInstrumento", SP."OrigenIdSistema"
		FROM  "BSCL.Normativa.BD::NOR.SolicitudParametros" SP
		INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS I ON (I."Nemotecnico" = SP."Valor")
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TI ON (TI."IdTipoInstrumento" = I."IdTipoInstrumento")
		LEFT JOIN "BSCL.Normativa.BD::NOR.SolicitudParametros" AS S2 ON S2."IdSolicitudCircular" = :v_SecuenciaSolicitud AND S2."TipoParametro" IN ('CLAINS','CLAINS_MAS')
		WHERE SP."TipoParametro" IN ('NEMO','NEMO_MAS')
			AND SP."IdSolicitudCircular" = :v_SecuenciaSolicitud
			AND TI."IdClasificacionTipoInstrumento" = CASE WHEN COALESCE(S2."Valor", '') <> '' THEN S2."Valor" ELSE TI."IdClasificacionTipoInstrumento" END
			AND NOT EXISTS (
			      SELECT 1
			      FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SP2
			      WHERE SP2."TipoParametro" IN ('NEMO', 'NEMO_MAS')
			      AND SP2."IdSolicitudCircular" = :v_SecuenciaSolicitud
			      AND SP2."IdSistema" = I."IdInstrumento"
			);
			
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
		    TRIM(v_FechaLimite) as "FechaLimite",       
		    '' as "IdEstadoDocumento",        
		    CURRENT_UTCTIMESTAMP as "FechaCreacion",     
		    TRIM(v_FechaPresentacion) as "FechaPresentacion", 
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
	SELECT 'idDocumento' 			AS "NombreParametro", to_varchar(v_idDoc) 				AS "Valor" FROM DUMMY
	UNION ALL
	SELECT 'EXTSAL'					AS "NombreParametro", v_ExtensionSalida 				AS "Valor" FROM DUMMY
	UNION ALL
	SELECT 'COMSAL'					AS "NombreParametro", v_ComprimirSalida 				AS "Valor" FROM DUMMY
	UNION ALL
	SELECT 'NOMCOLSAL'				AS "NombreParametro", v_TitulosSalida 					AS "Valor" FROM DUMMY
	UNION ALL
	SELECT 'LINPARSAL'				AS "NombreParametro", to_varchar(v_LineasPorSalida)		AS "Valor" FROM DUMMY
	UNION ALL
	SELECT 'DESCCOL'				AS "NombreParametro", to_varchar(v_LineasPorSalida)		AS "Valor" FROM DUMMY;
	
	
	
	
	
	--DESDE LA UI
	v_TablaParamUI=
	SELECT "NombreParametro" 		AS "NombreParametro", "Valor" 						AS "Valor" 
	FROM :p_TablaParametros WHERE "Tipo"='BATCH';
	
	 ---DE SOLICITUD PARA BATCH -> UI
	v_TablaParamUI= SELECT * FROM :v_TablaParamUI
	UNION ALL 
	SELECT DEF."NombreParametro" 		AS "NombreParametro", DEF."Valor" 				AS "Valor" 
		FROM :p_TablaParametros  DEF
		WHERE DEF."Tipo"='SOLICITUD' AND DEF."NombreParametro" IN ('NOMSAL','ZIPSAL','EXTSAL','COMSAL','NOMCOLSAL','LINPARSAL','DESCCOL')
		AND NOT EXISTS (SELECT 1 FROM :v_TablaParamUI BAT WHERE DEF."NombreParametro"=BAT."NombreParametro");
		

	
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
	(p_IdEmpresa,p_IdUsuario,p_IdModulo,0,v_IdTipoProgramaExportacion,0,:v_TablaParametroBatch,v_CreaParam,out_no_ocupable,p_out_AdvertenciasContador,p_out_AdvertenciasDetalle,p_out_ErroresContador,p_out_ErroresDetalle);
END;
