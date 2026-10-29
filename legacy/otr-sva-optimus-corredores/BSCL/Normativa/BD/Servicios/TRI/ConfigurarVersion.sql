SET SCHEMA "BSCL";
--GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ConfigurarVersion";

--  Proposito: Configura una version nueva para todos los esquemas.
--  Autor: Gat-Blac 
--  Fecha Creacion: 14/02/2017
--GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ConfigurarVersion"(
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
    IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
	IN p_Versiones								TABLE (
					    							"IdInterno"					BIGINT,
					    							"Version"					NVARCHAR(10),
					    							"FechaPresentacionLimite"	NVARCHAR(10)
					    						),
   	OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
		BEGIN
			select count(1) into p_out_ErroresContador 
			from :v_out_ErroresDetalle;
			select count(1) into p_out_AdvertenciasContador 
			from :v_out_AdvertenciasDetalle;
			
			v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_ErroresDetalle
									UNION ALL
									SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_AdvertenciasDetalle;
									
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdInterno",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterno",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
		DECLARE v_resultadoRegla 		NVARCHAR(40);
		DECLARE v_count					BIGINT;
		DECLARE v_countVersiones		BIGINT;
		DECLARE v_countVersionExistente BIGINT;
		DECLARE v_countEsquemasCopiar 	BIGINT;
		DECLARE v_FechaSistema			NVARCHAR(10);
		DECLARE v_id_homologacion		BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.ConfigurarVersion','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		SELECT COUNT(1) AS "Count" INTO v_countVersiones
		FROM :p_Versiones;
		
		IF (v_countVersiones = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_VERSIONES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") AS "Count" INTO v_count
		FROM :p_Versiones
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		IF (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT COUNT(1) INTO v_countVersionExistente
		FROM :p_Versiones VERSIONES
		INNER JOIN "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA ON VERSIONES."Version" = ESQUEMA."Version";
		

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		SELECT MAX(HOMOLOGACION."IdHomologacion") into v_id_homologacion FROM  "BSCL.Normativa.BD::DOC.Homologacion" HOMOLOGACION;
		
		/**
		********** INICIO TABLAS A COPIAR ESQUEMAS ANUALES **************
		**/
		v_esquemas_copiar = SELECT 	ESQUEMA."Esquema",
									ESQUEMA."Version",
									ESQUEMA."Modulo",
									ESQUEMA."Periodicidad",
									ESQUEMA."Objetivo",
									ESQUEMA."PeriodoInicio",
									ESQUEMA."IdEntidadReguladora",
									ESQUEMA."FechaPresentacionLimite",
									ESQUEMA."FechaDatosDesde",
									ESQUEMA."FechaDatosHasta",
									ESQUEMA."CodigoVersionSII",
									ESQUEMA."HabilitadoMantenedor"
									FROM "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA
									INNER JOIN (
									SELECT A.* FROM (
										SELECT "Esquema",
											   MAX("Version") AS "Version"
										FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Modulo" = 'TRI' AND "Periodicidad" = 'A'
										GROUP BY "Esquema"
									)A WHERE  A."Version" NOT IN ( SELECT  "Version" FROM :p_Versiones)	
										
									) VERSION ON (ESQUEMA."Esquema" = VERSION."Esquema" AND ESQUEMA."Version" = VERSION."Version")
									WHERE ESQUEMA."Modulo" = 'TRI' AND ESQUEMA."Periodicidad" = 'A'
									AND NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Esquema" EXT WHERE  ESQUEMA."Esquema" = EXT."Esquema" AND EXT."Version" IN (SELECT DISTINCT "Version" FROM :p_Versiones))
									--AND  ESQUEMA."Version" NOT IN (SELECT DISTINCT "Version" FROM :p_Versiones)
									UNION ALL
									SELECT  ESQUEMA."Esquema",
											ESQUEMA."Version",
											ESQUEMA."Modulo",
											ESQUEMA."Periodicidad",
											ESQUEMA."Objetivo",
											ESQUEMA."PeriodoInicio",
											ESQUEMA."IdEntidadReguladora",
											ESQUEMA."FechaPresentacionLimite",
											ESQUEMA."FechaDatosDesde",
											ESQUEMA."FechaDatosHasta",
											ESQUEMA."CodigoVersionSII",
											ESQUEMA."HabilitadoMantenedor"
											FROM "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA
											INNER JOIN (
												SELECT A.* FROM (
														SELECT "Esquema",
															   MAX(YEAR(TO_DATE("Version",'YYYY/MM'))) AS "MaxAnio"
														FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Modulo" = 'TRI' AND "Periodicidad" != 'A'
														GROUP BY "Esquema"
												) A WHERE  A."MaxAnio" NOT IN ( SELECT  "Version" FROM :p_Versiones)
											) VERSION ON (ESQUEMA."Esquema" = VERSION."Esquema")
											
											WHERE ESQUEMA."Modulo" = 'TRI' AND ESQUEMA."Periodicidad" != 'A'
											AND YEAR(TO_DATE(ESQUEMA."Version",'YYYY/MM')) = VERSION."MaxAnio"
											AND NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Esquema" EXT WHERE  ESQUEMA."Esquema" = EXT."Esquema" AND LEFT(EXT."Version",4) IN (SELECT DISTINCT "Version" FROM :p_Versiones))
									--SE AÑADE REPORTE MENSUALES PARA SICAM
									UNION ALL
									SELECT  ESQUEMA."Esquema",
											ESQUEMA."Version",
											ESQUEMA."Modulo",
											ESQUEMA."Periodicidad",
											ESQUEMA."Objetivo",
											ESQUEMA."PeriodoInicio",
											ESQUEMA."IdEntidadReguladora",
											ESQUEMA."FechaPresentacionLimite",
											ESQUEMA."FechaDatosDesde",
											ESQUEMA."FechaDatosHasta",
											ESQUEMA."CodigoVersionSII",
											ESQUEMA."HabilitadoMantenedor"
											FROM "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA
											INNER JOIN (
												SELECT A.* FROM (
														SELECT "Esquema",
															   MAX(YEAR(TO_DATE("Version",'YYYY/MM'))) AS "MaxAnio"
														FROM "BSCL.Normativa.BD::DOC.Esquema"
														WHERE "Modulo" = 'NOR' AND "Periodicidad" != 'A' AND "Esquema" IN ('REPO2P1','REPO2P2','REPO3P1')
														GROUP BY "Esquema"
												) A WHERE  A."MaxAnio" NOT IN ( SELECT  "Version" FROM :p_Versiones)
											) VERSION ON (ESQUEMA."Esquema" = VERSION."Esquema")
											
											WHERE "Modulo" ='NOR' AND "Periodicidad" != 'A' AND ESQUEMA."Esquema" IN ('REPO2P1','REPO2P2','REPO3P1')
											AND YEAR(TO_DATE(ESQUEMA."Version",'YYYY/MM')) = VERSION."MaxAnio"
											AND NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Esquema" EXT WHERE  ESQUEMA."Esquema" = EXT."Esquema" AND LEFT(EXT."Version",4) IN (SELECT DISTINCT "Version" FROM :p_Versiones));
									

											
		SELECT COUNT(1) INTO v_countEsquemasCopiar	FROM :v_esquemas_copiar;
		
		IF v_countVersionExistente > 0 AND v_countEsquemasCopiar=0  THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'VERSION_YA_EXISTE' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
			ELSE
			IF v_countVersionExistente = 0 AND v_countEsquemasCopiar=0 THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'ESQUEMA_BASE_NO_EXISTE' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
			END IF;
		END IF;
		
									
		v_documento_copiar = SELECT "BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
									DOCUMENTO."IdTipoDocumento",
									DOCUMENTO."Esquema",
									DOCUMENTO."Version",
									DOCUMENTO."IdEmpresa",
									DOCUMENTO."FechaLimite",
									DOCUMENTO."IdEstadoDocumento",
									DOCUMENTO."FechaCreacion",
									DOCUMENTO."FechaPresentacion",
									DOCUMENTO."DocumentoIdentidadDeclarante",
									DOCUMENTO."DocumentoIdentidadRepresentante",
									DOCUMENTO."DireccionElectronicaDeclaracion",
									DOCUMENTO."DireccionFisicaDeclaracion",
									DOCUMENTO."ComunaDeclaracion",
									DOCUMENTO."TelefonoDeclaracion",
									DOCUMENTO."FolioDeclaracion",
									DOCUMENTO."EnProceso",
									DOCUMENTO."FechaInicioUltimoProceso",
									DOCUMENTO."AnoMes",
									ESQUEMA."Periodicidad"
							  FROM "BSCL.Normativa.BD::DOC.Documento" DOCUMENTO
							  INNER JOIN :v_esquemas_copiar ESQUEMA ON (DOCUMENTO."Esquema" = ESQUEMA."Esquema" AND DOCUMENTO."Version" = ESQUEMA."Version");
							  
		v_campos_copiar = SELECT CAMPO."Esquema",
								 CAMPO."Version",
								 CAMPO."Nombre",
								 CAMPO."CodigoSII",
								 CAMPO."TipoDiccionario",
								 CAMPO."Orden",
								 CAMPO."Titulo",
								 CAMPO."Ayuda",
								 CAMPO."TipoDato",
								 CAMPO."Largo",
								 CAMPO."ValorDefecto",
								 CAMPO."Obligatorio",
								 CAMPO."Editable",
								 CAMPO."ModoMasivo",
								 CAMPO."Exportable",
								 CAMPO."OcultoEnGrilla",
								 CAMPO."OcultoEnMantenedor",
								 CAMPO."Comun",
								 CAMPO."Clave",
								 CAMPO."Grupo",
								 CAMPO."OrdenCargaExterna",
								 CAMPO."CantidadDecimales",
								 ESQUEMA."Periodicidad"
							FROM "BSCL.Normativa.BD::DOC.Campo" CAMPO
							INNER JOIN :v_esquemas_copiar ESQUEMA ON (CAMPO."Esquema" = ESQUEMA."Esquema" AND CAMPO."Version" = ESQUEMA."Version");
		
		v_campo_clasificador_copiar = SELECT CAMPO_CLASIFICADOR."Esquema",
										CAMPO_CLASIFICADOR."Version",
										CAMPO_CLASIFICADOR."TipoClasificador",
										CAMPO_CLASIFICADOR."NombreCampo",
										CAMPO_CLASIFICADOR."ValorClasificador",
										ESQUEMA."Periodicidad"
								  FROM "BSCL.Normativa.BD::DOC.CampoClasificador" CAMPO_CLASIFICADOR
								  INNER JOIN :v_esquemas_copiar ESQUEMA ON (CAMPO_CLASIFICADOR."Esquema" = ESQUEMA."Esquema" AND CAMPO_CLASIFICADOR."Version" = ESQUEMA."Version");
		
		v_clasificador_copiar = SELECT CLASIFICADOR."Esquema",
									   CLASIFICADOR."Version",
									   CLASIFICADOR."Tipo",
									   CLASIFICADOR."Descripcion",
									   ESQUEMA."Periodicidad"
								FROM "BSCL.Normativa.BD::DOC.Clasificador" CLASIFICADOR
								INNER JOIN :v_esquemas_copiar ESQUEMA ON (CLASIFICADOR."Esquema" = ESQUEMA."Esquema" AND CLASIFICADOR."Version" = ESQUEMA."Version");
		
		v_cuadratura_copiar = SELECT CUADRATURA."Esquema",
									 CUADRATURA."Version",
									 CUADRATURA."Tipo",
									 CUADRATURA."Descripcion",
									 ESQUEMA."Periodicidad"
							  FROM "BSCL.Normativa.BD::DOC.Cuadratura" CUADRATURA
							  INNER JOIN :v_esquemas_copiar ESQUEMA ON (CUADRATURA."Esquema" = ESQUEMA."Esquema" AND CUADRATURA."Version" = ESQUEMA."Version");
		
		v_campo_cuadratura_copiar = SELECT CAMPO_CUADRATURA."Esquema",
										   CAMPO_CUADRATURA."Version",
										   CAMPO_CUADRATURA."TipoCuadratura",
										   CAMPO_CUADRATURA."NombreCampo",
										   CAMPO_CUADRATURA."Orden",
										   ESQUEMA."Periodicidad"
									FROM "BSCL.Normativa.BD::DOC.CampoCuadratura" CAMPO_CUADRATURA 
									INNER JOIN :v_esquemas_copiar ESQUEMA ON (CAMPO_CUADRATURA."Esquema" = ESQUEMA."Esquema" AND CAMPO_CUADRATURA."Version" = ESQUEMA."Version");
		
		v_homologacion_copiar = SELECT HOMOLOGACION."IdHomologacion",
									   HOMOLOGACION."Esquema",
									   HOMOLOGACION."Version",
									   HOMOLOGACION."Tipo",
									   HOMOLOGACION."Descripcion",
									   HOMOLOGACION."ValorOrigen",
									   HOMOLOGACION."ValorDestino",
									   HOMOLOGACION."IdEstado",
									   ESQUEMA."Periodicidad"
								FROM "BSCL.Normativa.BD::DOC.Homologacion" HOMOLOGACION
								INNER JOIN :v_esquemas_copiar ESQUEMA ON (HOMOLOGACION."Esquema" = ESQUEMA."Esquema" AND HOMOLOGACION."Version" = ESQUEMA."Version");
		
		v_parametros_copiar = SELECT PARAMETRO."Esquema",
									 PARAMETRO."Version",
									 PARAMETRO."Codigo",
									 PARAMETRO."Descripcion",
									 PARAMETRO."Valor",
									 ESQUEMA."Periodicidad"
							   FROM "BSCL.Normativa.BD::DOC.Parametro" PARAMETRO
							   INNER JOIN :v_esquemas_copiar ESQUEMA ON (PARAMETRO."Esquema" = ESQUEMA."Esquema" AND PARAMETRO."Version" = ESQUEMA."Version");
		
		v_validacion_copiar = SELECT VALIDACION."Esquema",
									 VALIDACION."Version",
									 VALIDACION."Codigo",
									 VALIDACION."Tipo",
									 VALIDACION."Activa",
									 VALIDACION."Descripcion",
									 VALIDACION."Severidad",
									 VALIDACION."Formula",
									 ESQUEMA."Periodicidad"
							  FROM "BSCL.Normativa.BD::DOC.Validacion" VALIDACION
							  INNER JOIN :v_esquemas_copiar ESQUEMA ON (VALIDACION."Esquema" = ESQUEMA."Esquema" AND VALIDACION."Version" = ESQUEMA."Version");
		
		v_fondos_normativos = SELECT FONDOS."IdFondo",
							  ESQUEMA."Esquema",
							  ESQUEMA."Version",
							  0  AS "PorcentajeAccionario",
							  '' AS "Observacion",
							  'ACT' AS "IdEstado",
							  ESQUEMA."Periodicidad"
							FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoCombobox" FONDOS
							,:v_esquemas_copiar ESQUEMA
							WHERE FONDOS."FondoNormativo" = 'N' AND ESQUEMA."Periodicidad" = 'A';
		
		v_fondos_copiar = SELECT FONDOS."IdFondo",
						  FONDOS."Esquema",
						  FONDOS."Version",
						  0  AS "PorcentajeAccionario",
						  '' AS "Observacion",
						  FONDOS."IdEstado",
						  'A' as "Periodicidad"
					FROM "BSCL.Normativa.BD::TRI.Fondos" FONDOS
					INNER JOIN :v_esquemas_copiar ESQUEMA ON (FONDOS."Esquema" = ESQUEMA."Esquema" AND FONDOS."Version" = ESQUEMA."Version" AND "Periodicidad" = 'A')
					UNION ALL
					SELECT FONDOS."IdFondo",
						  FONDOS."Esquema",
						  FONDOS."Version",
						  FONDOS."PorcentajeAccionario",
						  FONDOS."Observacion",
						  FONDOS."IdEstado",
						  'A' as "Periodicidad"
					FROM :v_fondos_normativos FONDOS
					LEFT JOIN "BSCL.Normativa.BD::TRI.Fondos" TRI_FONDOS ON (FONDOS."IdFondo" = TRI_FONDOS."IdFondo" AND TRI_FONDOS."Esquema" = FONDOS."Esquema" AND TRI_FONDOS."Version" = FONDOS."Version")
					WHERE COALESCE(TRI_FONDOS."IdFondo",'') = ''; 
					
					
			v_procesos_copiar = SELECT  CONTROL_PROCESO."Esquema",
										CONTROL_PROCESO."Version",
										'NGE' 	AS "EstadoCertificado",
										'N' 	AS "Carga",
										'N' 	AS "Validacion",
										'N' 	AS "PreDeclaracion",
										'N' 	AS "Certificacion",
										ESQUEMA."Periodicidad"
								  FROM "BSCL.Normativa.BD::TRI.ControlProceso" CONTROL_PROCESO
								  INNER JOIN :v_esquemas_copiar ESQUEMA ON (CONTROL_PROCESO."Esquema" = ESQUEMA."Esquema" AND CONTROL_PROCESO."Version" = ESQUEMA."Version");			


			v_folio_certificado_copiar = SELECT
											FOLIO."Esquema",
											FOLIO."Version",
											FOLIO."UltimoFolio",
											FOLIO."CodigoCertificado",
											FOLIO."NombreCertificado",
											FOLIO."CodigoPrograma",
											ESQUEMA."Periodicidad"
										FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente" FOLIO
										INNER JOIN :v_esquemas_copiar ESQUEMA ON (FOLIO."Esquema" = ESQUEMA."Esquema" AND FOLIO."Version" = ESQUEMA."Version");


			v_archivo_normativa_copiar = SELECT
											ARCHIVO_NORMATIVA."IdArchivoNormativa",
											ESQUEMA."Version" AS "VersionArchivo",
											ARCHIVO_NORMATIVA."Esquema",
											ARCHIVO_NORMATIVA."Version",
											ARCHIVO_NORMATIVA."IdTipoDocumento", 
											ARCHIVO_NORMATIVA."Descripcion", 
											ARCHIVO_NORMATIVA."NombreArchivo", 
											ARCHIVO_NORMATIVA."IdArchivo", 
											ARCHIVO_NORMATIVA."TipoArchivo", 
											ARCHIVO_NORMATIVA."ServicioArchivo", 
											ARCHIVO_NORMATIVA."IdEmpresa", 
											ARCHIVO_NORMATIVA."IdEstado",
											ESQUEMA."Periodicidad"
										FROM "BSCL.Normativa.BD::NOR.ArchivoNormativa" ARCHIVO_NORMATIVA
										INNER JOIN :v_esquemas_copiar ESQUEMA ON (ARCHIVO_NORMATIVA."Esquema" = ESQUEMA."Esquema" AND ARCHIVO_NORMATIVA."VersionArchivo" = ESQUEMA."Version");

		/**
		********** FIN TABLAS A COPIAR **************
		**/
		
		/**
		********** INICIO TABLAS A CREAR **************
		**/
		
		v_esquemas_nuevos = SELECT 	VERSION."IdInterno",
									ESQUEMA."Esquema",
									CASE ESQUEMA."Periodicidad" WHEN 'A' THEN VERSION."Version"
																ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(ESQUEMA."Version",'YYYY/MM'),'MM')
																END AS "Version",
									ESQUEMA."Modulo",
									ESQUEMA."Periodicidad",
									ESQUEMA."Objetivo",
									ESQUEMA."PeriodoInicio",
									ESQUEMA."IdEntidadReguladora",
									VERSION."FechaPresentacionLimite",
									TO_DATE(VERSION."Version" || '-' || TO_CHAR(ESQUEMA."FechaDatosDesde",'MM-DD'),'YYYY-MM-DD') AS "FechaDatosDesde",
									TO_DATE(VERSION."Version" || '-' || TO_CHAR(ESQUEMA."FechaDatosHasta",'MM-DD'),'YYYY-MM-DD') AS "FechaDatosHasta",
									ESQUEMA."CodigoVersionSII",
									ESQUEMA."HabilitadoMantenedor"
							FROM :v_esquemas_copiar AS ESQUEMA,
							:p_Versiones AS VERSION;
		
		v_documento_nuevos = SELECT VERSION."IdInterno",
									DOCUMENTO."IdDocumento",
									DOCUMENTO."IdTipoDocumento",
									DOCUMENTO."Esquema",
									CASE DOCUMENTO."Periodicidad" WHEN 'A' THEN VERSION."Version"
																ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(DOCUMENTO."Version",'YYYY/MM'),'MM')
																END AS "Version",
									DOCUMENTO."IdEmpresa",
									DOCUMENTO."FechaLimite",
									DOCUMENTO."IdEstadoDocumento",
									DOCUMENTO."FechaCreacion",
									DOCUMENTO."FechaPresentacion",
									COALESCE(DOCUMENTO."DocumentoIdentidadDeclarante",'') as "DocumentoIdentidadDeclarante",
									COALESCE(DOCUMENTO."DocumentoIdentidadRepresentante",'') as "DocumentoIdentidadRepresentante",
									COALESCE(DOCUMENTO."DireccionElectronicaDeclaracion",'') as "DireccionElectronicaDeclaracion",
									COALESCE(DOCUMENTO."DireccionFisicaDeclaracion",'') as "DireccionFisicaDeclaracion",
									COALESCE(DOCUMENTO."ComunaDeclaracion",'') as "ComunaDeclaracion",
									COALESCE(DOCUMENTO."TelefonoDeclaracion",'') as "TelefonoDeclaracion",
									COALESCE(DOCUMENTO."FolioDeclaracion",'') as "FolioDeclaracion",
									DOCUMENTO."EnProceso",
									DOCUMENTO."FechaInicioUltimoProceso",
									DOCUMENTO."AnoMes"
							 FROM :v_documento_copiar DOCUMENTO,
							 :p_Versiones AS VERSION;
		
		v_campos_nuevos = SELECT VERSION."IdInterno",
								 CAMPO."Esquema",
								 CASE CAMPO."Periodicidad" WHEN 'A' THEN VERSION."Version"
																ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(CAMPO."Version",'YYYY/MM'),'MM')
																END AS "Version",
								 CAMPO."Nombre",
								 CAMPO."CodigoSII",
								 CAMPO."TipoDiccionario",
								 CAMPO."Orden",
								 CAMPO."Titulo",
								 CAMPO."Ayuda",
								 CAMPO."TipoDato",
								 CAMPO."Largo",
								 CAMPO."ValorDefecto",
								 CAMPO."Obligatorio",
								 CAMPO."Editable",
								 CAMPO."ModoMasivo",
								 CAMPO."Exportable",
								 CAMPO."OcultoEnGrilla",
								 CAMPO."OcultoEnMantenedor",
								 CAMPO."Comun",
								 CAMPO."Clave",
								 CAMPO."Grupo",
								 CAMPO."OrdenCargaExterna",
								 CAMPO."CantidadDecimales"
						 FROM :v_campos_copiar CAMPO,
						 :p_Versiones AS VERSION;
		
		v_campo_clasificador_nuevos = SELECT VERSION."IdInterno",
											 CAMPO_CLASIFICADOR."Esquema",
											 CASE CAMPO_CLASIFICADOR."Periodicidad" WHEN 'A' THEN VERSION."Version"
																	   ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(CAMPO_CLASIFICADOR."Version",'YYYY/MM'),'MM')
																	   END AS "Version",
											 CAMPO_CLASIFICADOR."TipoClasificador",
											 CAMPO_CLASIFICADOR."NombreCampo",
											 CAMPO_CLASIFICADOR."ValorClasificador"
									   FROM :v_campo_clasificador_copiar CAMPO_CLASIFICADOR,
									    :p_Versiones AS VERSION;
					
		v_clasificador_nuevos = SELECT VERSION."IdInterno",
									   CLASIFICADOR."Esquema",
									   CASE CLASIFICADOR."Periodicidad" WHEN 'A' THEN VERSION."Version"
																	   ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(CLASIFICADOR."Version",'YYYY/MM'),'MM')
																	   END AS "Version",
									   CLASIFICADOR."Tipo",
									   CLASIFICADOR."Descripcion"
								FROM :v_clasificador_copiar CLASIFICADOR,
								:p_Versiones AS VERSION;
		
		v_cuadratura_nuevos = SELECT VERSION."IdInterno",
									 CUADRATURA."Esquema",
									 CASE CUADRATURA."Periodicidad" WHEN 'A' THEN VERSION."Version"
																	   ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(CUADRATURA."Version",'YYYY/MM'),'MM')
																	   END AS "Version",
									 CUADRATURA."Tipo",
									 CUADRATURA."Descripcion"
							   FROM :v_cuadratura_copiar CUADRATURA,
							   :p_Versiones AS VERSION;
		
		v_campo_cuadratura_nuevos = SELECT VERSION."IdInterno",
										   CAMPO_CUADRATURA."Esquema",
										   CASE CAMPO_CUADRATURA."Periodicidad" WHEN 'A' THEN VERSION."Version"
																	   ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(CAMPO_CUADRATURA."Version",'YYYY/MM'),'MM')
																	   END AS "Version",
										   CAMPO_CUADRATURA."TipoCuadratura",
										   CAMPO_CUADRATURA."NombreCampo",
										   CAMPO_CUADRATURA."Orden"
									FROM :v_campo_cuadratura_copiar CAMPO_CUADRATURA,
									    :p_Versiones AS VERSION;
		
		v_homologacion_nuevos = SELECT 
									   VERSION."IdInterno",
									   "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion",
									   HOMOLOGACION."Esquema",
									   CASE HOMOLOGACION."Periodicidad" WHEN 'A' THEN VERSION."Version"
																	   ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(HOMOLOGACION."Version",'YYYY/MM'),'MM')
																	   END AS "Version",
									   HOMOLOGACION."Tipo",
									   HOMOLOGACION."Descripcion",
									   HOMOLOGACION."ValorOrigen",
									   HOMOLOGACION."ValorDestino",
									   HOMOLOGACION."IdEstado"
									FROM :v_homologacion_copiar HOMOLOGACION,
									:p_Versiones AS VERSION;
		
		v_parametros_nuevos = SELECT VERSION."IdInterno",
									 PARAMETRO."Esquema",
									 CASE PARAMETRO."Periodicidad" WHEN 'A' THEN VERSION."Version"
																	   ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(PARAMETRO."Version",'YYYY/MM'),'MM')
																	   END AS "Version",
									 PARAMETRO."Codigo",
									 PARAMETRO."Descripcion",
									 PARAMETRO."Valor"
							   FROM :v_parametros_copiar PARAMETRO,
							   :p_Versiones AS VERSION;
		
		v_validacion_nuevos = SELECT VERSION."IdInterno",
									 VALIDACION."Esquema",
									 CASE VALIDACION."Periodicidad" WHEN 'A' THEN VERSION."Version"
																	   ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(VALIDACION."Version",'YYYY/MM'),'MM')
																	   END AS "Version",
									 VALIDACION."Codigo",
									 VALIDACION."Tipo",
									 VALIDACION."Activa",
									 VALIDACION."Descripcion",
									 VALIDACION."Severidad",
									 VALIDACION."Formula"
							  FROM :v_validacion_copiar VALIDACION,
							  :p_Versiones AS VERSION;

		v_fondos_nuevos = SELECT VERSION."IdInterno",
								 FONDOS."IdFondo",
								 FONDOS."Esquema",
								 CASE FONDOS."Periodicidad" WHEN 'A' THEN VERSION."Version"
															ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(FONDOS."Version",'YYYY/MM'),'MM')
															END AS "Version",
								 FONDOS."PorcentajeAccionario",
								 FONDOS."Observacion",
								 FONDOS."IdEstado" 
						   FROM :v_fondos_copiar FONDOS,
						   :p_Versiones AS VERSION;
		select * from :v_fondos_nuevos order by "IdFondo","Esquema";	
		
		
		v_procesos_nuevos = SELECT 
									   VERSION."IdInterno",
									   PROCESO."Esquema",
									   CASE PROCESO."Periodicidad" WHEN 'A' THEN VERSION."Version"
																	   ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(PROCESO."Version",'YYYY/MM'),'MM')
																	   END AS "Version",
									    PROCESO."EstadoCertificado",
										PROCESO."Carga",
										PROCESO."Validacion",
										PROCESO."PreDeclaracion",
										PROCESO."Certificacion"
									FROM :v_procesos_copiar PROCESO,
									:p_Versiones AS VERSION;
		

		v_folio_certificado_nuevos = SELECT VERSION."IdInterno",
										FOLIO."Esquema",
										CASE FOLIO."Periodicidad" WHEN 'A' THEN VERSION."Version"
																	   ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(FOLIO."Version",'YYYY/MM'),'MM')
																	   END AS "Version",
										0 AS "UltimoFolio",
										FOLIO."CodigoCertificado",
										FOLIO."NombreCertificado",
										FOLIO."CodigoPrograma"
									FROM :v_folio_certificado_copiar FOLIO,
									:p_Versiones AS VERSION;
		
		v_archivo_normativa_nuevos = SELECT VERSION."IdInterno",
								ARCHIVO_NORMATIVA."IdArchivoNormativa",
								CASE ARCHIVO_NORMATIVA."Periodicidad" WHEN 'A' THEN VERSION."Version"
																	   ELSE VERSION."Version" || '/' || TO_CHAR(TO_DATE(ARCHIVO_NORMATIVA."Version",'YYYY/MM'),'MM')
																	   END AS "VersionArchivo",
								ARCHIVO_NORMATIVA."Esquema",
								ARCHIVO_NORMATIVA."Version",
								ARCHIVO_NORMATIVA."IdTipoDocumento", 
								ARCHIVO_NORMATIVA."Descripcion", 
								ARCHIVO_NORMATIVA."NombreArchivo", 
								COALESCE(ARCHIVO_NORMATIVA."IdArchivo",'0') AS "IdArchivo", 
								ARCHIVO_NORMATIVA."TipoArchivo", 
								ARCHIVO_NORMATIVA."ServicioArchivo", 
								COALESCE(ARCHIVO_NORMATIVA."IdEmpresa",0) AS "IdEmpresa", 
								ARCHIVO_NORMATIVA."IdEstado"
							FROM :v_archivo_normativa_copiar ARCHIVO_NORMATIVA,
							:p_Versiones AS VERSION;
		
		/**
		********** FIN TABLAS A CREAR **************
		**/
		/**
		******* COMIENZAN LOS INSERT ***************
		**/
		--select * from :v_esquemas_nuevos;
		INSERT INTO "BSCL.Normativa.BD::DOC.Esquema"
		("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio","IdEntidadReguladora","FechaPresentacionLimite",
		"FechaDatosDesde","FechaDatosHasta","CodigoVersionSII","HabilitadoMantenedor")
		SELECT DISTINCT"Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio","IdEntidadReguladora","FechaPresentacionLimite",
		"FechaDatosDesde","FechaDatosHasta","CodigoVersionSII","HabilitadoMantenedor" 
		FROM :v_esquemas_nuevos
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Esquema" AS A
								LEFT JOIN :v_esquemas_nuevos AS B ON (A."Esquema"= B."Esquema")
								WHERE A."Esquema"= B."Esquema" AND A."Version" = B."Version");
		
		INSERT INTO "BSCL.Normativa.BD::DOC.Documento"
		("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento","FechaCreacion","FechaPresentacion",
		 "DocumentoIdentidadDeclarante","DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion","DireccionFisicaDeclaracion",
		 "ComunaDeclaracion","TelefonoDeclaracion","FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
		SELECT DISTINCT"IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento","FechaCreacion","FechaPresentacion",
		 "DocumentoIdentidadDeclarante","DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion","DireccionFisicaDeclaracion",
		 "ComunaDeclaracion","TelefonoDeclaracion","FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes" 
		FROM :v_documento_nuevos
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Documento" AS A
								LEFT JOIN :v_documento_nuevos AS B ON (A."Esquema"= B."Esquema")
								WHERE A."Esquema"= B."Esquema" AND A."Version" = B."Version");
		
		INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
		("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable",
	 	"ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales")
		SELECT DISTINCT "Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable",
		"ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales"
		FROM :v_campos_nuevos AS C
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Campo" AS A
    	WHERE A."Esquema"= C."Esquema" AND A."Version" = C."Version" AND A."Nombre" = C."Nombre");
		
		INSERT INTO "BSCL.Normativa.BD::DOC.Clasificador"
		("Esquema","Version","Tipo","Descripcion")
		SELECT DISTINCT"Esquema","Version","Tipo","Descripcion"
		FROM :v_clasificador_nuevos AS C
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Clasificador" AS A
				WHERE A."Esquema"= C."Esquema" AND A."Version" = C."Version"
				AND A."Tipo" = C."Tipo");
		
		INSERT INTO "BSCL.Normativa.BD::DOC.CampoClasificador"
		("Esquema","Version","TipoClasificador","NombreCampo","ValorClasificador")
		SELECT "Esquema","Version","TipoClasificador","NombreCampo","ValorClasificador"
		FROM :v_campo_clasificador_nuevos AS C
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.CampoClasificador" AS A
						WHERE A."Esquema"= C."Esquema" AND A."Version" = C."Version"
						AND A."TipoClasificador" = C."TipoClasificador"
						AND A."NombreCampo" = C."NombreCampo");
		
		INSERT INTO "BSCL.Normativa.BD::DOC.Cuadratura"
		("Esquema","Version","Tipo","Descripcion")
		SELECT "Esquema","Version","Tipo","Descripcion"
		FROM :v_cuadratura_nuevos AS C
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Cuadratura" AS A
						WHERE A."Esquema"= C."Esquema" AND A."Version" = C."Version"
						AND A."Tipo" = C."Tipo");
		
		INSERT INTO "BSCL.Normativa.BD::DOC.CampoCuadratura"
		("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
		SELECT "Esquema","Version","TipoCuadratura","NombreCampo","Orden"
		FROM :v_campo_cuadratura_nuevos AS C
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.CampoCuadratura" AS A
						WHERE A."Esquema"= C."Esquema" AND A."Version" = C."Version"
						AND A."TipoCuadratura" = C."TipoCuadratura"
						AND A."NombreCampo"= C."NombreCampo");
	
		INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
		("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
		SELECT "IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado"
		FROM :v_homologacion_nuevos AS C
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" AS A
						WHERE A."Esquema"= C."Esquema" AND A."Version" = C."Version");
		
		INSERT INTO "BSCL.Normativa.BD::DOC.Parametro"
		("Esquema","Version","Codigo","Descripcion","Valor")
		SELECT "Esquema","Version","Codigo","Descripcion","Valor"
		FROM :v_parametros_nuevos AS C
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Parametro" AS A
								WHERE A."Esquema"= C."Esquema" AND A."Version" = C."Version"
								AND A."Codigo" = C."Codigo");
		
		INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
		("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
		SELECT "Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula"
		FROM :v_validacion_nuevos AS C
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Validacion" AS A
						WHERE A."Esquema"= C."Esquema" AND A."Version" = C."Version"
						AND A."Codigo" = C."Codigo");

		INSERT INTO "BSCL.Normativa.BD::TRI.ControlProceso"
		("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
		SELECT "Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion"
		FROM :v_procesos_nuevos AS C
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.ControlProceso" AS A
						WHERE A."Esquema"= C."Esquema" AND A."Version" = C."Version");

		INSERT INTO "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
		("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
		SELECT "Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma"
		FROM :v_folio_certificado_nuevos
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente" AS A
						LEFT JOIN :v_folio_certificado_nuevos AS B ON (A."Esquema"= B."Esquema")
						WHERE A."Esquema"= B."Esquema" AND A."Version" = B."Version");

		INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa"
		("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
		SELECT "IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado"
		FROM :v_archivo_normativa_nuevos
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.ArchivoNormativa" AS A
						LEFT JOIN :v_archivo_normativa_nuevos AS B ON (A."Esquema"= B."Esquema")
						WHERE A."Esquema"= B."Esquema" AND A."VersionArchivo" = B."Version");
		
		/**
		******* FIN LOS INSERT ***************
		**/
		
		/**
		**** TODO: Se debe realizar logica para copiar esquemas mensuales y trimestrales. 
		**** Para generar todos los equemas con sus respectivas fechas y cantidad (AÑO/MES x12 y AÑO/TRIMESTRE x3).
		**/
		
		
		
		v_log = SELECT 
						"Version" AS "IdCabeceraLog"
						,'INGRESO' AS "IdTipoLog"
						,'NOR' AS "IdModulo"
						,:p_IdUsuario AS "IdUsuario"
						,:p_IdEmpresa AS "IdEmpresa"
						,:v_FechaSistema AS "FechaSistemaLog"
						,:p_IdUsuario AS "IdUsuarioAtendido"
						,0 AS "IdPersona"
						,0 AS "IdCliente"
						,'' AS "EstadoCliente"
						,'' AS "NombreCliente"
						,'' AS "IdTipoDocumentoIdentidad"
						,'' AS "IdDocumentoIdentidad"
						,0 AS "IdPortafolio"
						,'' AS "EstadoPortafolio"
						,'CREACION_ESQUEMA' AS "IdTipoRegistro"
						,"Version" AS "IdRegistro"
						,'ACT' AS "EstadoRegistro"
						,'' AS "IdTipoOperacion"
						,0 AS "Folio"
						,0 AS "Cantidad"
						,0 AS "PrecioTasa"
						,0 AS "Monto"
						,'' AS "IdMonedaOrigen"
						,'' AS "IdMonedaLiquidacion"
						,'' AS "TipoCambio"
						,'' AS "Nemo"
						,0 AS "IdProceso"
						,'' AS "EstadoProceso"
						,'' AS "NombreProceso"
					FROM :p_Versiones;
			
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.ConfigurarVersion','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;