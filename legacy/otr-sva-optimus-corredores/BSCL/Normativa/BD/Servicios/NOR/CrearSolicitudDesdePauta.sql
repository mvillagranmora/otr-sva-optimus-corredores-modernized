
SET SCHEMA "BSCL";
 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudDesdePauta";
 
CREATE PROCEDURE 
--
--  Proposito: Servicio para procesar tareas agregadas en la Pauta.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 04/09/2017

"BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudDesdePauta" ( 
    --VARIABLES DEL PROCESO PRINCIPAL
	IN p_IdEmpresa					  BIGINT,
 	IN p_IdTipoTarea   		 		  NVARCHAR(40),  --Tarea de inscripcion en BAT
   	IN p_IdProceso		  			  BIGINT DEFAULT 0
    --VARIABLES DE SALIDA 
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		
	DECLARE v_Count,v_countSal1,v_countSal2,v_IdUsuario,v_CountCUS INT:=0;
	DECLARE v_NombreTandem,v_NombreTandemCUS NVARCHAR(100):='';
	DECLARE v_CodigoCorredora NVARCHAR(40):='';
	DECLARE v_sp NVARCHAR(500):='';
	DECLARE v_IdTipoDocumento NVARCHAR(10) :='';
    DECLARE v_fechaHoraSistema, v_out_MensajeEval		 NVARCHAR(40):='';
    DECLARE v_FechaDesde, v_FechaHasta,v_FechaOperacion NVARCHAR(10):='';

	DECLARE p_out_AdvertenciasContador,p_out_resultado	INT	:= 0;
	DECLARE p_out_ErroresContador			INT:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;  	
	p_out_ResultadoProceso			= SELECT '' as "Campo", 0 as "Id", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
	
------------------------------------------------------
---- ORIGEN PAUTA
---------------------------------------------------------------	
		IF  p_IdTipoTarea <> '' THEN
			---------------------------------------------------------------
			---- PROCESOS
			---------------------------------------------------------------
			
		
			-- Identificar fecha de trabajo
					call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
			);
		
			
			
			
			---Identificar Tipo de documento de la Normativa
			
			IF 	p_IdTipoTarea ='CIR1064FR' OR p_IdTipoTarea ='CIR1064R' OR p_IdTipoTarea ='INFDERECH' THEN
				v_IdTipoDocumento = p_IdTipoTarea;
			ELSE 
				SELECT TOP 1  "Valor" INTO v_IdTipoDocumento 
				FROM "BSCL.Plataforma.BD::BAT.ParametrosPrograma"  WHERE "IdPrograma"=:p_IdProceso AND "NombreParametro"='IdTipoDocumento';	
			END IF;
			
			SELECT top 1 "IdUsuario" INTO v_IdUsuario
			FROM  "BSCL.Plataforma.BD::ACC.Usuario" WHERE "CodigoUsuario" in ('SISTEMA','SISTEMA_PRE');
		

			IF 	v_IdTipoDocumento <> '' THEN 
				
				v_FechaOperacion=TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD');
			  	v_FechaDesde=v_FechaOperacion;
			  	v_FechaHasta=v_FechaDesde;
	
			  	--Parametros comunes
				v_TablaParametros=   SELECT 'BATCH' AS "Tipo",'idUsuario' 			AS "NombreParametro",to_varchar(v_IdUsuario)	AS "Valor"	FROM DUMMY 
							UNION ALL SELECT 'BATCH' AS "Tipo",'schema' 			AS "NombreParametro",CURRENT_SCHEMA AS "Valor"	FROM DUMMY
							UNION ALL SELECT 'BATCH' AS "Tipo",'grabar' 			AS "NombreParametro",'false' 		AS "Valor"	FROM DUMMY
							UNION ALL SELECT 'BATCH' AS "Tipo",'reemplazar' 			AS "NombreParametro",'false' 		AS "Valor"	FROM DUMMY
							UNION ALL SELECT 'BATCH' AS "Tipo",'simboloDecimal' 	AS "NombreParametro",',' 			AS "Valor"	FROM DUMMY
							UNION ALL SELECT 'BATCH' AS "Tipo",'simboloFecha' 		AS "NombreParametro",'dd/MM/yyyy' 	AS "Valor"	FROM DUMMY
							UNION ALL SELECT 'BATCH' AS "Tipo",'aceptarFechasNull' 	AS "NombreParametro",'true'	 		AS "Valor"	FROM DUMMY;
					
		  	--Parametros especificos
					IF 	v_IdTipoDocumento ='CIR1064FR' OR v_IdTipoDocumento ='CIR1064R' OR v_IdTipoDocumento ='CIR1064' OR v_IdTipoDocumento ='INFDERECH'  THEN
						v_TablaParametros= SELECT * FROM :v_TablaParametros
								UNION ALL SELECT 'BATCH' AS "Tipo",'separadorListas' 	AS "NombreParametro",'' 			AS "Valor"	FROM DUMMY
								UNION ALL SELECT 'BATCH' AS "Tipo",'fechaDesde' 		AS "NombreParametro",v_FechaDesde	AS "Valor"	FROM DUMMY
								UNION ALL SELECT 'BATCH' AS "Tipo",'fechaHasta' 		AS "NombreParametro",v_FechaHasta	AS "Valor"	FROM DUMMY
								UNION ALL SELECT 'SOLICITUD' AS "Tipo",'NOMCOLSAL' 		AS "NombreParametro",'N'			AS "Valor"	FROM DUMMY
								UNION ALL SELECT 'SOLICITUD' AS "Tipo",'FINI' 			AS "NombreParametro",v_FechaDesde	AS "Valor"	FROM DUMMY
								UNION ALL SELECT 'SOLICITUD' AS "Tipo",'FTER' 			AS "NombreParametro",v_FechaHasta	AS "Valor"	FROM DUMMY;
								
					END IF;
		
		
					IF 	v_IdTipoDocumento ='CIR1720' THEN
						v_TablaParametros= SELECT * FROM :v_TablaParametros
								UNION ALL SELECT 'BATCH' AS "Tipo",'separadorListas' 	AS "NombreParametro",';' 			AS "Valor"	FROM DUMMY
								UNION ALL SELECT 'SOLICITUD' AS "Tipo",'NOMSAL' 		AS "NombreParametro",'CIRCULAR1720_' ||	v_FechaHasta	AS "Valor"	FROM DUMMY
								UNION ALL SELECT 'SOLICITUD' AS "Tipo",'FTER' 			AS "NombreParametro",v_FechaHasta	AS "Valor"	FROM DUMMY;
								
					END IF;
					
					IF 	v_IdTipoDocumento ='NCG365MAS' THEN
					    v_FechaHasta=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD');
						v_TablaParametros= SELECT * FROM :v_TablaParametros
								UNION ALL SELECT 'BATCH'	 AS "Tipo",'FTER' 			AS "NombreParametro",v_FechaHasta	AS "Valor"	FROM DUMMY
								UNION ALL SELECT 'BATCH'	 AS "Tipo",'IdEmpresa' 		AS "NombreParametro",to_varchar(:p_IdEmpresa)	AS "Valor"	FROM DUMMY
								UNION ALL SELECT 'SOLICITUD' AS "Tipo",'FTER' 			AS "NombreParametro",v_FechaHasta	AS "Valor"	FROM DUMMY;
								
					END IF;
				
				  /***	Busca parametro para copias en TANDEM	***/
			
					 SELECT COUNT(1) INTO v_Count FROM "BSCL.Normativa.BD::DOC.TipoDocumento" T
					 INNER JOIN "BSCL.Normativa.BD::DOC.Parametro" P ON (P."Esquema"=T."Esquema" AND P."Codigo"='NOMBRE_ENVIO_TANDEM_SVS')
					 WHERE T."IdTipoDocumento"=v_IdTipoDocumento AND LENGTH(("Valor"))>0;
					
					IF(v_Count>0) THEN
						 SELECT TOP 1 P."Valor" INTO v_NombreTandem FROM "BSCL.Normativa.BD::DOC.TipoDocumento" T
						 INNER JOIN "BSCL.Normativa.BD::DOC.Parametro" P ON (P."Esquema"=T."Esquema" AND P."Codigo"='NOMBRE_ENVIO_TANDEM_SVS')
						 WHERE T."IdTipoDocumento"=v_IdTipoDocumento AND LENGTH(TRIM("Valor"))>0 ORDER BY P."Version" DESC;
						
						SELECT TOP  1 "CodigoCorredora" INTO v_CodigoCorredora  FROM  "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"			AS EMP
						 WHERE EMP."IdEmpresa"=p_IdEmpresa AND "IdBolsa"='BCS';
						
						--se arma nombre segun Parametro
						v_NombreTandem = REPLACE(v_NombreTandem,'[DIATRAN]',TO_VARCHAR (v_FechaOperacion, 'DD'));
						v_NombreTandem = REPLACE(v_NombreTandem,'[MESTRAN]',TO_VARCHAR (v_FechaOperacion, 'MM'));
						v_NombreTandem = REPLACE(v_NombreTandem,'[CODCORREDOR]',v_CodigoCorredora);
						v_TablaParametros= SELECT * FROM :v_TablaParametros
								UNION ALL SELECT 'SOLICITUD' AS "Tipo",'NOMBRE_ENVIO_TANDEM_SVS' 	AS "NombreParametro",v_NombreTandem	AS "Valor"	FROM DUMMY
								UNION ALL SELECT 'BATCH' AS "Tipo",'NOMBRE_ENVIO_TANDEM_SVS' 	AS "NombreParametro",v_NombreTandem	AS "Valor"	FROM DUMMY;
					
					
			  		END IF;			
 
				
					 SELECT COUNT(1) INTO v_CountCUS FROM "BSCL.Normativa.BD::DOC.TipoDocumento" T
					 INNER JOIN "BSCL.Normativa.BD::DOC.Parametro" P ON (P."Esquema"=T."Esquema" AND P."Codigo"='NOMBRE_ENVIO_TANDEM_CUS')
					 WHERE T."IdTipoDocumento"=v_IdTipoDocumento AND LENGTH(("Valor"))>0;
					
					IF(v_CountCUS>0) THEN
						 SELECT TOP 1 P."Valor" INTO v_NombreTandemCUS FROM "BSCL.Normativa.BD::DOC.TipoDocumento" T
						 INNER JOIN "BSCL.Normativa.BD::DOC.Parametro" P ON (P."Esquema"=T."Esquema" AND P."Codigo"='NOMBRE_ENVIO_TANDEM_CUS')
						 WHERE T."IdTipoDocumento"=v_IdTipoDocumento AND LENGTH(TRIM("Valor"))>0 ORDER BY P."Version" DESC;
						
						SELECT TOP  1 "CodigoCorredora" INTO v_CodigoCorredora  FROM  "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"			AS EMP
						 WHERE EMP."IdEmpresa"=p_IdEmpresa AND "IdBolsa"='BCS';
						
						--se arma nombre segun Parametro
						v_NombreTandemCUS = REPLACE(v_NombreTandemCUS,'[DIATRAN]',TO_VARCHAR (v_FechaOperacion, 'DD'));
						v_NombreTandemCUS = REPLACE(v_NombreTandemCUS,'[MESTRAN]',TO_VARCHAR (v_FechaOperacion, 'MM'));
						v_NombreTandemCUS = REPLACE(v_NombreTandemCUS,'[CODCORREDOR]',v_CodigoCorredora);
						v_TablaParametros= SELECT * FROM :v_TablaParametros
								UNION ALL SELECT 'SOLICITUD' AS "Tipo",'NOMBRE_ENVIO_TANDEM_CUS' 	AS "NombreParametro",v_NombreTandemCUS	AS "Valor"	FROM DUMMY
								UNION ALL SELECT 'BATCH' AS "Tipo",'NOMBRE_ENVIO_TANDEM_CUS' 	AS "NombreParametro",v_NombreTandemCUS	AS "Valor"	FROM DUMMY;
					
					
			  		END IF;	
				
				
				/*
			SELECT top 1 "IdUsuario" 
			FROM  "BSCL.Plataforma.BD::ACC.Usuario" WHERE "CodigoUsuario"='SISTEMA';
		*/
				
				
				
				
				
			
					CALL "BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudCircular"
					(	
						p_IdModulo          => 'NOR',
						p_IdUsuario			=> v_IdUsuario,
						p_IdEmpresa 		=> p_IdEmpresa,
						p_IdTipoDocumento   => v_IdTipoDocumento,
						p_FechaInicio 		=> v_FechaDesde,
						p_FechaTermino 		=> v_FechaHasta,
						p_IdCliente			=> 0,
						p_TablaParametros  	=> :v_TablaParametros,
						p_out_resultado	=>p_out_resultado,
						p_out_AdvertenciasContador =>p_out_AdvertenciasContador,
						p_out_AdvertenciasDetalle =>p_out_AdvertenciasDetalle,
						p_out_ErroresContador =>p_out_ErroresContador,
						p_out_ErroresDetalle =>p_out_ErroresDetalle
					);
			
					
					
					
			
			END IF;

	
			
		END IF;		
	
	
END; 