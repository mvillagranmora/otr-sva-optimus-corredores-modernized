SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.EliminarEventoJuntaAccionista";

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.EliminarEventoJuntaAccionista" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 					BIGINT,
    IN p_IdUsuario 					BIGINT,
    IN p_IdModulo					NVARCHAR(3), 
    IN p_JuntaAccionista			TABLE(
    									"IdInterno"					BIGINT,
    									"IdEventoJuntaAccionista"	BIGINT
    								),
    OUT p_out_Resultado				TABLE(
    									"IdInterno"					BIGINT,
    									"IdEventoJuntaAccionista"	BIGINT,
    									"Resultado"					NVARCHAR(3),
    									"Codigo"					NVARCHAR(100)
    								),
    OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))	
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
		DECLARE v_resultadoRegla			NVARCHAR(40);
		DECLARE v_FechaSistema 				NVARCHAR(10);
		DECLARE v_count						BIGINT;
		DECLARE v_count_juntas				BIGINT;
		DECLARE v_count_juntas_validadas	BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Custodia.BD.Servicios::EVC.EliminarJuntaAccionista','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		SELECT COUNT(1) into v_count_juntas
		FROM :p_JuntaAccionista;
				
		IF v_count_juntas = 0 THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_JUNTA_ACCIONISTA' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") as "Count" into v_count
		FROM :p_JuntaAccionista
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		IF (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_junta_accionista = SELECT "p_evento"."IdInterno",
									"p_evento"."IdEventoJuntaAccionista",
									"evento"."IdEstadoJunta",
									"evento"."IdEstadoCustodia",
									CASE WHEN COALESCE("evento"."IdEventoJuntaAccionista", 0 ) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoIdEventoJuntaAccionista",
									CASE WHEN "evento"."IdEstadoCustodia" = 'PRO' THEN 'ERR' ELSE 'OK' END AS "ResultadoRespaldoCustodia",
									CASE WHEN "evento"."IdEstadoJunta" = 'ANU' THEN 'ERR' ELSE 'OK' END AS "ResultadoJuntaAnulada",
									CASE WHEN "evento"."IdEstadoJunta" = 'CER' THEN 'ERR' ELSE 'OK' END AS "ResultadoJuntaCerrada",
									:p_IdEmpresa AS "IdEmpresa"
							FROM :p_JuntaAccionista AS "p_evento"
							LEFT OUTER JOIN "BSCL.Custodia.BD::CUS.EventoJuntaAccionista" AS "evento" ON ("evento"."IdEventoJuntaAccionista" = "p_evento"."IdEventoJuntaAccionista" AND "evento"."IdEmpresa" = :p_IdEmpresa);
		
		v_errores = SELECT "IdInterno",
						   "IdEventoJuntaAccionista",
						   "ResultadoIdEventoJuntaAccionista",
						   "ResultadoRespaldoCustodia",
						   "ResultadoJuntaAnulada",
						   "ResultadoJuntaCerrada",
						   "IdEstadoJunta",
						   "IdEstadoCustodia"
					FROM :v_junta_accionista
					WHERE 'ERR' IN ("ResultadoIdEventoJuntaAccionista","ResultadoRespaldoCustodia","ResultadoJuntaAnulada","ResultadoJuntaCerrada");
		
		v_junta_accionista_validadas = SELECT "IdInterno"
											,"IdEventoJuntaAccionista"
											,"IdEmpresa"
									   FROM :v_junta_accionista
									   WHERE 'ERR' NOT IN ("ResultadoIdEventoJuntaAccionista","ResultadoRespaldoCustodia","ResultadoJuntaAnulada","ResultadoJuntaCerrada");
		
		
		UPDATE EVENTO
		SET "IdEstadoJunta" = 'ANU'
		FROM "BSCL.Custodia.BD::CUS.EventoJuntaAccionista" AS EVENTO
		INNER JOIN :v_junta_accionista_validadas AS P_EVENTO ON (EVENTO."IdEventoJuntaAccionista" = P_EVENTO."IdEventoJuntaAccionista" AND EVENTO."IdEmpresa" = P_EVENTO."IdEmpresa");
		
		p_out_Resultado = SELECT "IdInterno"
								, "IdEventoJuntaAccionista"
								, 'OK' AS "Resultado"
								, 'UI_EXITO' AS "Codigo"
						 FROM :v_junta_accionista_validadas
						 UNION ALL
						 SELECT "IdInterno"
						 		,"IdEventoJuntaAccionista"
						 		,'ERR' AS "Resultado"
						 		,CASE WHEN "ResultadoIdEventoJuntaAccionista" 	= 'ERR' THEN 'JUNTA_NO_EXISTE' 
						 		 	  WHEN "ResultadoRespaldoCustodia" 			= 'ERR' THEN 'RESPALDO_CUSTODIA_ENCUENTRA_REALIZADO' 
						 		 	  WHEN "ResultadoJuntaAnulada" 				= 'ERR' THEN 'JUNTA_ENCUENTRA_ANULADA'
						 		      WHEN "ResultadoJuntaCerrada" 				= 'ERR' THEN 'JUNTA_ENCUENTRA_CERRADA' END AS "Codigo"
						 FROM :v_errores;
		
		v_log = SELECT "IdEventoJuntaAccionista" AS "IdCabeceraLog"
						,'ELIMINACION' AS "IdTipoLog"
						,'CUS' AS "IdModulo"
						,:p_IdUsuario AS "IdUsuario"
						,:p_IdEmpresa AS "IdEmpresa"
						,:v_FechaSistema AS "FechaSistemaLog"
						,:p_IdUsuario AS "IdUsuarioAtendido"
						,0  AS "IdPersona"
						,0  AS "IdCliente"
						,'' AS "EstadoCliente"
						,'' AS "NombreCliente"
						,'' AS "IdTipoDocumentoIdentidad"
						,'' AS "IdDocumentoIdentidad"
						,0 AS "IdPortafolio"
						,'' AS "EstadoPortafolio"
						,'EVENTO_JUNTA_ACCIONISTA' AS "IdTipoRegistro"
						,"IdEventoJuntaAccionista" AS "IdRegistro"
						,'ACT' AS "EstadoRegistro"
						,'' AS "IdTipoOperacion"
						,"IdEventoJuntaAccionista" AS "Folio"
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
					FROM :v_junta_accionista_validadas;
		
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
			
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Custodia.BD.Servicios::EVC.EliminarJuntaAccionista','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
