SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ModificarAnexoCalificacionTributaria";

--  Proposito: Modificar Anexo Calificacion Tributaria
--  Autor: Gat-Blac 
--  Fecha Creacion: 21/04/2017

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ModificarAnexoCalificacionTributaria"(
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
    IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
	IN p_Anexo									TABLE(
													"IdInterno"					BIGINT,
													"IdCalificacionTributaria"	BIGINT,
													"ValorHistorico"			DECIMAL(26,8),
													"Descripcion"				NVARCHAR(250),
													"TasaCredito"				DECIMAL(26,8),
													"PorcentajeDividendo"		DECIMAL(26,8),
													"IdTipoUtilidades"			NVARCHAR(10),
													"IdEstado"					NVARCHAR(3)
												),
	OUT p_out_Resultado							TABLE(
													"IdInterno"					BIGINT,
													"IdCalificacionTributaria"	BIGINT,
													"ValorHistorico"			DECIMAL(26,8),
													"Descripcion"				NVARCHAR(250),
													"TasaCredito"				DECIMAL(26,8),
													"PorcentajeDividendo"		DECIMAL(26,8),
													"IdTipoUtilidades"			NVARCHAR(10),
													"IdEstado"					NVARCHAR(3),
													"Resultado"					NVARCHAR(100)
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
		DECLARE v_countAnexo				BIGINT;
		DECLARE v_count						BIGINT;
		DECLARE v_FechaSistema				NVARCHAR(10);
		DECLARE v_countCorrectos			BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.ModificarAnexoCalificacionTributaria','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		SELECT COUNT(1) AS "Count" INTO v_countAnexo
		FROM :p_Anexo;
		
		IF (v_countAnexo = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_ANEXOS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") AS "Count" INTO v_count
		FROM :p_Anexo
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		IF (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_validacion_anexo = SELECT	ANEXO."IdInterno"
									,ANEXO."IdCalificacionTributaria"
									,ANEXO."ValorHistorico"
									,ANEXO."Descripcion"
									,ANEXO."TasaCredito"
									,ANEXO."PorcentajeDividendo"
									,ANEXO."IdTipoUtilidades"
									,ANEXO_CREADO."AnioOrigen"
									,0 AS "Factor06"
									,0 AS "Factor07"				
									,0 AS "Factor08"				
									,0 AS "Factor16"				
									,0 AS "Factor17"				
									,0 AS "Factor11"				
									,0 AS "Factor09"				
									,0 AS "Factor13"				
									,0 AS "Factor12"				
									,0 AS "Factor14"				
									,0 AS "Factor15"				
									,0 AS "Factor10"				
									,:v_FechaSistema AS "FechaModificacionSistema"			
									,CURRENT_UTCTIMESTAMP AS "FechaModificacionRegistro"	
									,ANEXO."IdEstado"
									,CASE WHEN COALESCE(ANEXO_CREADO."IdCalificacionTributaria",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoIdCalificacionTributario"
							FROM :p_Anexo AS ANEXO
							LEFT OUTER JOIN "BSCL.Normativa.BD::TRI.CalificacionTributaria" ANEXO_CREADO ON (ANEXO_CREADO."IdCalificacionTributaria" = ANEXO."IdCalificacionTributaria" AND ANEXO_CREADO."TipoRegistro" = 'ANEXO');	
		
		v_anexo_errores = SELECT "IdInterno"
								 ,"IdCalificacionTributaria"
								 FROM :v_validacion_anexo
								 WHERE "ResultadoIdCalificacionTributario" = 'ERR';
		
		v_anexo_correctas = SELECT "IdInterno"
									,"IdCalificacionTributaria"
									,"ValorHistorico"
									,"Descripcion"
									,"TasaCredito"
									,"PorcentajeDividendo"
									,"IdTipoUtilidades"
									,"AnioOrigen"
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
									,"FechaModificacionSistema"			
									,"FechaModificacionRegistro"	
									,"IdEstado"
									FROM :v_validacion_anexo
									WHERE "ResultadoIdCalificacionTributario" = 'OK';
		
		v_out_ErroresDetalle = SELECT "IdInterno",'ANEXO_NO_EXISTE' AS "Codigo","IdCalificacionTributaria" AS "ValorParametro", 'IdCalificacionTributaria' AS "NombreParametro"
							   FROM :v_anexo_errores
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) into v_countCorrectos
		FROM :v_anexo_correctas;
		
		IF :v_countCorrectos = 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_anexo_calculo_tasa = SELECT "IdInterno"
									,"IdCalificacionTributaria"
									,"ValorHistorico"
									,"Descripcion"
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
									,"FechaModificacionSistema"			
									,"FechaModificacionRegistro"
									,"IdEstado"
									,ROUND("PorcentajeDividendo"/100,6) AS "Factor"
									,ROUND(ROUND("PorcentajeDividendo"/100,6) * ROUND("TasaCredito"/(100-"TasaCredito"),6),6) AS "FactorTasa"
									FROM :v_anexo_correctas;
		
		v_anexo_factor = SELECT "IdInterno"
								,"IdCalificacionTributaria"
								,"ValorHistorico"
								,"Descripcion"
								,"AnioOrigen"
								,"TasaCredito"
								,"PorcentajeDividendo"
								,"IdTipoUtilidades"
								,TO_DECIMAL(CASE WHEN "IdTipoUtilidades" = 6 THEN "Factor06" * "Factor" 	 ELSE "Factor06" END,26,8) AS "Factor06"
								,TO_DECIMAL(CASE WHEN "IdTipoUtilidades" = 7 THEN "Factor07" * "Factor"	 	 ELSE "Factor07" END,26,8) AS "Factor07"			
								,TO_DECIMAL(CASE WHEN "IdTipoUtilidades" = 8 THEN "Factor08" * "Factor"	 	 ELSE "Factor08" END,26,8) AS "Factor08"				
								,"Factor16"				
								,"Factor17"				
								,TO_DECIMAL(CASE WHEN "IdTipoUtilidades" = 6 THEN "Factor11" * "FactorTasa"  ELSE "Factor11" END,26,8) AS "Factor11"				
								,TO_DECIMAL(CASE WHEN "IdTipoUtilidades" = 6 THEN "Factor09" * "FactorTasa"  ELSE "Factor09" END,26,8) AS "Factor09"				
								,"Factor13"				
								,"Factor12"				
								,"Factor14"				
								,"Factor15"				
								,"Factor10"				
								,"FechaModificacionSistema"			
								,"FechaModificacionRegistro"
								,"IdEstado"
							FROM :v_anexo_calculo_tasa;
		
		v_anexo_factor_validaciones = SELECT CALIFICACION_TRIBUTARIA."IdInterno"
											,CALIFICACION_TRIBUTARIA."IdCalificacionTributaria"
											,CALIFICACION_TRIBUTARIA."ValorHistorico"
											,CALIFICACION_TRIBUTARIA."Descripcion"
											,CALIFICACION_TRIBUTARIA."AnioOrigen"
											,CALIFICACION_TRIBUTARIA."TasaCredito"
											,CALIFICACION_TRIBUTARIA."PorcentajeDividendo"
											,CALIFICACION_TRIBUTARIA."IdTipoUtilidades"
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
											,CALIFICACION_TRIBUTARIA."FechaModificacionSistema"			
											,CALIFICACION_TRIBUTARIA."FechaModificacionRegistro"	
											,CALIFICACION_TRIBUTARIA."IdEstado"
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
										FROM :v_anexo_factor AS CALIFICACION_TRIBUTARIA
										LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Parametro" P84 ON (P84."Codigo" = 'P84' AND P84."Esquema" = 'DJ1885' AND P84."Version" = CALIFICACION_TRIBUTARIA."AnioOrigen")
										LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Parametro" P28 ON (P28."Codigo" = 'P28' AND P28."Esquema" = 'DJ1885' AND P28."Version" = CALIFICACION_TRIBUTARIA."AnioOrigen")
										LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Parametro" P85 ON (P85."Codigo" = 'P85' AND P85."Esquema" = 'DJ1885' AND P85."Version" = CALIFICACION_TRIBUTARIA."AnioOrigen");
		
		v_advertencia_calculo = SELECT "IdInterno"
										,"ResultadoSumaFactor"
										,"ResultadoH095"
										,"ResultadoH096"
										,"ResultadoH257"
										,"ResultadoH258"
								 FROM :v_anexo_factor_validaciones
								 WHERE 'ADV' IN ("ResultadoSumaFactor","ResultadoH095","ResultadoH096","ResultadoH257","ResultadoH258");
		
		v_errores_calculo = SELECT  "IdInterno"
									,"ResultadoP84"
									,"ResultadoP28"
									,"ResultadoP85"
								 FROM :v_anexo_factor_validaciones
								 WHERE 'ERR' IN ("ResultadoP84","ResultadoP28","ResultadoP85");
								 
		v_out_ErroresDetalle =  SELECT "IdInterno",'NO_EXISTE_P84' AS "Codigo",'' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_errores_calculo
							   WHERE "ResultadoP84" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno",'NO_EXISTE_P28' AS "Codigo",'' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_errores_calculo
							   WHERE "ResultadoP28" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno",'NO_EXISTE_P85' AS "Codigo",'' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_errores_calculo
							   WHERE "ResultadoP85" = 'ERR'
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
		
		v_out_AdvertenciasDetalle = SELECT "IdInterno",'SUMA_FACTORES_INCORRECTA' AS "Codigo", 'N' AS "RequiereAutorizacion",'' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_advertencia_calculo
							   WHERE "ResultadoSumaFactor" = 'ADV'
							   UNION ALL
							   SELECT "IdInterno",'FACTORES_NO_CUMPLEN_REGLA' AS "Codigo", 'N' AS "RequiereAutorizacion",'' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_advertencia_calculo
							   WHERE 'ADV' IN ("ResultadoH095","ResultadoH096","ResultadoH257","ResultadoH258")
							   UNION ALL
							   SELECT * FROM :v_out_AdvertenciasDetalle;
		
		
		v_anexo_correctas = SELECT "IdInterno"
									,"IdCalificacionTributaria"
									,"ValorHistorico"
									,"Descripcion"
									,"TasaCredito"
									,"PorcentajeDividendo"
									,"IdTipoUtilidades"
									,"AnioOrigen"
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
									,"FechaModificacionSistema"			
									,"FechaModificacionRegistro"	
									,"IdEstado"
									FROM :v_anexo_factor_validaciones
									WHERE "ResultadoP84" = 'OK' AND "ResultadoP28" = 'OK' AND "ResultadoP85" = 'OK';
		
		UPDATE ANEXO
		SET ANEXO."ValorHistorico" = ANEXO_CORRECTOS."ValorHistorico",
			ANEXO."Descripcion" = ANEXO_CORRECTOS."Descripcion",
			ANEXO."TasaCredito" = ANEXO_CORRECTOS."TasaCredito",
			ANEXO."PorcentajeDividendo" = ANEXO_CORRECTOS."PorcentajeDividendo",
			ANEXO."IdTipoUtilidades" = ANEXO_CORRECTOS."IdTipoUtilidades",
			ANEXO."Factor06" = ANEXO_CORRECTOS."Factor06",
			ANEXO."Factor07" = ANEXO_CORRECTOS."Factor07",
			ANEXO."Factor08" = ANEXO_CORRECTOS."Factor08",
			ANEXO."Factor16" = ANEXO_CORRECTOS."Factor16",
			ANEXO."Factor17" = ANEXO_CORRECTOS."Factor17",
			ANEXO."Factor11" = ANEXO_CORRECTOS."Factor11",
			ANEXO."Factor09" = ANEXO_CORRECTOS."Factor09",
			ANEXO."Factor13" = ANEXO_CORRECTOS."Factor13",
			ANEXO."Factor12" = ANEXO_CORRECTOS."Factor12",
			ANEXO."Factor14" = ANEXO_CORRECTOS."Factor14",
			ANEXO."Factor15" = ANEXO_CORRECTOS."Factor15",
			ANEXO."Factor10" = ANEXO_CORRECTOS."Factor10",
			ANEXO."FechaModificacionSistema" = ANEXO_CORRECTOS."FechaModificacionSistema",
			ANEXO."FechaModificacionRegistro" = ANEXO_CORRECTOS."FechaModificacionRegistro",
			ANEXO."IdEstado" = ANEXO_CORRECTOS."IdEstado"
		FROM "BSCL.Normativa.BD::TRI.CalificacionTributaria" ANEXO
		INNER JOIN :v_validacion_anexo ANEXO_CORRECTOS ON (ANEXO_CORRECTOS."IdCalificacionTributaria" = ANEXO."IdCalificacionTributaria" AND ANEXO."TipoRegistro" = 'ANEXO');
		
		
		p_out_Resultado = SELECT ANEXO."IdInterno"
									,ANEXO."IdCalificacionTributaria"
									,ANEXO."ValorHistorico"
									,ANEXO."Descripcion"
									,ANEXO."TasaCredito"
									,ANEXO."PorcentajeDividendo"
									,ANEXO."IdTipoUtilidades"			
									,ANEXO."IdEstado"
									,CASE WHEN ANEXO."ResultadoIdCalificacionTributario" = 'ERR' THEN 'ANEXO_NO_EXISTE' ELSE 'UI_EXITO' END AS "Resultado"
							FROM :v_validacion_anexo AS ANEXO;
		
		v_log = SELECT 
						"IdCalificacionTributaria" AS "IdCabeceraLog"
						,'MODIFICACION' AS "IdTipoLog"
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
						,'MODIFICACION_ANEXO' AS "IdTipoRegistro"
						,"IdCalificacionTributaria" AS "IdRegistro"
						,"IdEstado" AS "EstadoRegistro"
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
					FROM :v_anexo_correctas;
					
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.ModificarAnexoCalificacionTributaria','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;
		
