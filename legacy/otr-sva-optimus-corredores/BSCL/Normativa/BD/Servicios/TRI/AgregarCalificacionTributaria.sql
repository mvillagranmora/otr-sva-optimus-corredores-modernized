SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.AgregarCalificacionTributaria";

--  Proposito: Agrega Calificacion Tributaria.
--  Autor: Gat-Blac 
--  Fecha Creacion: 20/04/2017

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.AgregarCalificacionTributaria"(
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
    IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
	IN p_CalificacionTributaria					TABLE(
													"IdInterno"							BIGINT,
													"IdClasificacionTipoInstrumento"	NVARCHAR(3),
													"IdInstrumento"						BIGINT,
													"ValorHistorico"					DECIMAL(26,8),
													"FechaPago"							DATE,
													"IdEventoCapital"					BIGINT,
													"Descripcion"						NVARCHAR(250),
													"SecuenciaEvento"					BIGINT,
													"AnioOrigen"						BIGINT,
													"Factor06"							DECIMAL(26,8),
													"Factor07"							DECIMAL(26,8),
													"Factor08"							DECIMAL(26,8),
													"Factor16"							DECIMAL(26,8),
													"Factor17"							DECIMAL(26,8),
													"Factor11"							DECIMAL(26,8),
													"Factor09"							DECIMAL(26,8),
													"Factor13"							DECIMAL(26,8),
													"Factor12"							DECIMAL(26,8),
													"Factor14"							DECIMAL(26,8),
													"Factor15"							DECIMAL(26,8),
													"Factor10"							DECIMAL(26,8)
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
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_countCalificaciones		BIGINT;
		DECLARE v_count						BIGINT;
		DECLARE v_FechaSistema				NVARCHAR(10);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.AgregarCalificacionTributaria','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		SELECT COUNT(1) AS "Count" INTO v_countCalificaciones
		FROM :p_CalificacionTributaria;
		
		IF (v_countCalificaciones = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_CALIFICACIONES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") AS "Count" INTO v_count
		FROM :p_CalificacionTributaria
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		IF (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		
		v_validacion_calificaciones = SELECT	CALIFICACION_TRIBUTARIA."IdInterno"
												,CALIFICACION_TRIBUTARIA."IdClasificacionTipoInstrumento"
												,CALIFICACION_TRIBUTARIA."IdInstrumento"
												,CALIFICACION_TRIBUTARIA."ValorHistorico"
												,CALIFICACION_TRIBUTARIA."FechaPago"
												,CALIFICACION_TRIBUTARIA."IdEventoCapital"
												,CALIFICACION_TRIBUTARIA."Descripcion"
												,CALIFICACION_TRIBUTARIA."SecuenciaEvento"
												,CALIFICACION_TRIBUTARIA."AnioOrigen"
												,0  AS "TasaCredito"
												,0  AS "PorcentajeDividendo"
												,'' AS "IdTipoUtilidades"
												,CALIFICACION_TRIBUTARIA."Factor06"
												,CALIFICACION_TRIBUTARIA."Factor07"				
												,CALIFICACION_TRIBUTARIA."Factor08"				
												,CALIFICACION_TRIBUTARIA."Factor16"				
												,CALIFICACION_TRIBUTARIA."Factor17"				
												,CALIFICACION_TRIBUTARIA."Factor11"				
												,CALIFICACION_TRIBUTARIA."Factor09"				
												,CALIFICACION_TRIBUTARIA."Factor13"				
												,CALIFICACION_TRIBUTARIA."Factor12"				
												,CALIFICACION_TRIBUTARIA."Factor14"				
												,CALIFICACION_TRIBUTARIA."Factor15"				
												,CALIFICACION_TRIBUTARIA."Factor10"				
												,:v_FechaSistema AS "FechaIngresoSistema"			
												,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"	
												,'CALIFICACION' "TipoRegistro"
												,CASE WHEN COALESCE(INSTRUMENTO."IdInstrumento",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoInstrumento"
												,CASE WHEN COALESCE(CALIFICACION_TRIBUTARIA$2."IdCalificacionTributaria",0) = 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoCalificacion"
												,CASE WHEN CALIFICACION_TRIBUTARIA."Factor06" + CALIFICACION_TRIBUTARIA."Factor07" + CALIFICACION_TRIBUTARIA."Factor08" 
												+ CALIFICACION_TRIBUTARIA."Factor16" + CALIFICACION_TRIBUTARIA."Factor17" + CALIFICACION_TRIBUTARIA."Factor11" + CALIFICACION_TRIBUTARIA."Factor09" + 
												CALIFICACION_TRIBUTARIA."Factor13" + CALIFICACION_TRIBUTARIA."Factor12" + CALIFICACION_TRIBUTARIA."Factor14" + 
												CALIFICACION_TRIBUTARIA."Factor15" + CALIFICACION_TRIBUTARIA."Factor10" = 1 THEN 'OK' ELSE 'ADV'
												 END AS "ResultadoSumaFactor"
												,CASE WHEN COALESCE(P84."Esquema",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoP84"
												,CASE WHEN COALESCE(P28."Esquema",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoP28"
												,CASE WHEN COALESCE(P85."Esquema",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoP85"
												,CASE WHEN CALIFICACION_TRIBUTARIA."Factor09" +  CALIFICACION_TRIBUTARIA."Factor12" >= (CALIFICACION_TRIBUTARIA."Factor06" + CALIFICACION_TRIBUTARIA."Factor07" + CALIFICACION_TRIBUTARIA."Factor11") * P84."Valor" THEN 'ADV'
												 	  ELSE 'OK'
												 END AS "ResultadoH095"
												,CASE WHEN CALIFICACION_TRIBUTARIA."Factor10" >= (CALIFICACION_TRIBUTARIA."Factor06" +  CALIFICACION_TRIBUTARIA."Factor07" + CALIFICACION_TRIBUTARIA."Factor11") * P28."Valor" THEN 'ADV'
													  ELSE 'OK'
												 END AS "ResultadoH096"
												,CASE WHEN CALIFICACION_TRIBUTARIA."Factor11" > CALIFICACION_TRIBUTARIA."Factor09" + CALIFICACION_TRIBUTARIA."Factor12" THEN 'ADV'
													  ELSE 'OK'
												 END AS "ResultadoH257"
												,CASE WHEN CALIFICACION_TRIBUTARIA."Factor11" >= (CALIFICACION_TRIBUTARIA."Factor06" +  CALIFICACION_TRIBUTARIA."Factor07") * P85."Valor" THEN 'ADV'
													  ELSE 'OK'
												 END AS "ResultadoH258"
										FROM :p_CalificacionTributaria AS CALIFICACION_TRIBUTARIA
										LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INSTRUMENTO ON (CALIFICACION_TRIBUTARIA."IdInstrumento" = INSTRUMENTO."IdInstrumento")
										LEFT OUTER JOIN "BSCL.Normativa.BD::TRI.CalificacionTributaria" CALIFICACION_TRIBUTARIA$2 ON CALIFICACION_TRIBUTARIA."IdEventoCapital" = CALIFICACION_TRIBUTARIA$2."IdEventoCapital" AND CALIFICACION_TRIBUTARIA."SecuenciaEvento" = CALIFICACION_TRIBUTARIA$2."SecuenciaEvento" AND CALIFICACION_TRIBUTARIA."AnioOrigen" = CALIFICACION_TRIBUTARIA$2."AnioOrigen"
										LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Parametro" P84 ON (P84."Codigo" = 'P84' AND P84."Esquema" = 'DJ1885' AND P84."Version" = CALIFICACION_TRIBUTARIA."AnioOrigen")
										LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Parametro" P28 ON (P28."Codigo" = 'P28' AND P28."Esquema" = 'DJ1885' AND P28."Version" = CALIFICACION_TRIBUTARIA."AnioOrigen")
										LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Parametro" P85 ON (P85."Codigo" = 'P85' AND P85."Esquema" = 'DJ1885' AND P85."Version" = CALIFICACION_TRIBUTARIA."AnioOrigen");			
																								
		
		v_calificaciones_errores = SELECT "IdInterno"
										,"IdInstrumento"
										,"ResultadoInstrumento"
										,"ResultadoCalificacion"
										,"ResultadoP84"
										,"ResultadoP28"
										,"ResultadoP85"
								 FROM :v_validacion_calificaciones
								 WHERE 'ERR' IN ("ResultadoInstrumento","ResultadoCalificacion","ResultadoP84","ResultadoP28","ResultadoP85");
		
		v_calificaciones_advertencias = SELECT "IdInterno"
										,"IdInstrumento"
										,"ResultadoSumaFactor"
										,"ResultadoH095"
										,"ResultadoH096"
										,"ResultadoH257"
										,"ResultadoH258"
								 FROM :v_validacion_calificaciones
								 WHERE 'ADV' IN ("ResultadoSumaFactor","ResultadoH095","ResultadoH096","ResultadoH257","ResultadoH258");
		
		v_calificaciones_correctas = SELECT "IdInterno"
											,"BSCL.Normativa.BD.Secuencias::TRI.CalificacionTributaria".NEXTVAL AS "IdCalificacionTributaria"
											,"IdClasificacionTipoInstrumento"
											,"IdInstrumento"
											,"ValorHistorico"
											,"FechaPago"
											,"IdEventoCapital"
											,"Descripcion"
											,"SecuenciaEvento"
											,"AnioOrigen"
											,"TasaCredito"
											,"PorcentajeDividendo"
											,"IdTipoUtilidades"
											,"Factor06"
											,"Factor07"				
											,"Factor08"				
											,"Factor16"				
											,"Factor17"				
											,"Factor11"				
											,"Factor09"				
											,"Factor13"				
											,"Factor12"				
											,"Factor14"				
											,"Factor15"				
											,"Factor10"				
											,"FechaIngresoSistema"			
											,"FechaIngresoRegistro"	
											,"TipoRegistro"
											,'ACT' AS "IdEstado"
									FROM :v_validacion_calificaciones
									WHERE "ResultadoInstrumento" = 'OK'  AND "ResultadoCalificacion" = 'OK' AND "ResultadoP84" = 'OK' AND "ResultadoP28" = 'OK' AND "ResultadoP85" = 'OK';
		
		v_out_ErroresDetalle = SELECT "IdInterno",'INSTRUMENTO_NO_EXISTE' AS "Codigo",CAST("IdInstrumento" AS NVARCHAR(100)) AS "ValorParametro", 'IdInstrumento' AS "NombreParametro"
							   FROM :v_calificaciones_errores
							   WHERE "ResultadoInstrumento" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno",'EXISTE_CALIFICACION_EDITAR' AS "Codigo",'' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_calificaciones_errores
							   WHERE "ResultadoCalificacion" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno",'NO_EXISTE_P84' AS "Codigo",'' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_calificaciones_errores
							   WHERE "ResultadoP84" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno",'NO_EXISTE_P28' AS "Codigo",'' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_calificaciones_errores
							   WHERE "ResultadoP28" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno",'NO_EXISTE_P85' AS "Codigo",'' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_calificaciones_errores
							   WHERE "ResultadoP85" = 'ERR'
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
		
		v_out_AdvertenciasDetalle = SELECT "IdInterno",'SUMA_FACTORES_INCORRECTA' AS "Codigo", 'N' AS "RequiereAutorizacion",'' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_calificaciones_advertencias
							   WHERE "ResultadoSumaFactor" = 'ADV'
							   UNION ALL
							   SELECT "IdInterno",'FACTORES_NO_CUMPLEN_REGLA' AS "Codigo", 'N' AS "RequiereAutorizacion",'' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_calificaciones_advertencias
							   WHERE 'ADV' IN ("ResultadoH095","ResultadoH096","ResultadoH257","ResultadoH258")
							   UNION ALL
							   SELECT * FROM :v_out_AdvertenciasDetalle;
		
		
		INSERT INTO "BSCL.Normativa.BD::TRI.CalificacionTributaria"
		("IdCalificacionTributaria","IdClasificacionTipoInstrumento","IdInstrumento","ValorHistorico","FechaPago","IdEventoCapital","Descripcion","SecuenciaEvento"
		 ,"AnioOrigen","TasaCredito","PorcentajeDividendo","IdTipoUtilidades","Factor06","Factor07","Factor08","Factor16","Factor17","Factor11"
		 ,"Factor09","Factor13","Factor12","Factor14","Factor15","Factor10","FechaIngresoSistema","FechaIngresoRegistro","TipoRegistro","IdEstado")
		SELECT "IdCalificacionTributaria","IdClasificacionTipoInstrumento","IdInstrumento","ValorHistorico","FechaPago","IdEventoCapital","Descripcion","SecuenciaEvento"
		 ,"AnioOrigen","TasaCredito","PorcentajeDividendo","IdTipoUtilidades","Factor06","Factor07","Factor08","Factor16","Factor17","Factor11"
		 ,"Factor09","Factor13","Factor12","Factor14","Factor15","Factor10","FechaIngresoSistema","FechaIngresoRegistro","TipoRegistro","IdEstado"
		FROM :v_calificaciones_correctas;
		
		
		v_log = SELECT 
						"IdCalificacionTributaria" AS "IdCabeceraLog"
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
						,'INGRESO_CALIFICACION' AS "IdTipoRegistro"
						,"IdCalificacionTributaria" AS "IdRegistro"
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
					FROM :v_calificaciones_correctas;
					
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.AgregarCalificacionTributaria','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;
