SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ModificarEventoJuntaAccionista";

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ModificarEventoJuntaAccionista" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 					BIGINT,
    IN p_IdUsuario 					BIGINT,
    IN p_IdModulo					NVARCHAR(3), 
    IN p_JuntaAccionista			TABLE(
    									"IdInterno"					BIGINT,
    									"IdEventoJuntaAccionista"	BIGINT,
    									"FechaCierre"				NVARCHAR(10),
    									"Lugar"						NVARCHAR(100),
    									"Instruccion"				NVARCHAR(500),
    									"CuadraturaDCV"				DECIMAL(26,8),
    									"Materias"					NVARCHAR(500)
    								),
    OUT p_out_Resultado				TABLE(
    									"IdInterno"					BIGINT,
    									"IdEventoJuntaAccionista"	BIGINT,
    									"FechaCierre"				NVARCHAR(10),
    									"Lugar"						NVARCHAR(100),
    									"Instruccion"				NVARCHAR(500),
    									"CuadraturaDCV"				DECIMAL(26,8),
    									"Materias"					NVARCHAR(500),
    									"IdEmpresa"					BIGINT
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
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Custodia.BD.Servicios::EVC.ModificarEventoJuntaAccionista','INICIO','INICIO');
		
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
		
		v_junta_accionista = SELECT "p_evento"."IdInterno"
									, "p_evento"."IdEventoJuntaAccionista"
									, "p_evento"."FechaCierre"
									, "p_evento"."Lugar"
									, "p_evento"."Instruccion"
									, "p_evento"."CuadraturaDCV"
									, "p_evento"."Materias"
									, "evento"."FechaRealizacion"
									, "evento"."IdEmpresa"
									, "evento"."IdEstadoJunta"
									, CASE WHEN "evento"."IdEstadoCustodia" = 'PRO' AND "p_evento"."FechaCierre" != "evento"."FechaCierre" THEN 'ERR' ELSE 'OK' END AS "ResultadoFechaCierre"
									, CASE WHEN COALESCE("evento"."IdEventoJuntaAccionista",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoIdEventoJuntaAccionista"
									, CASE WHEN :v_FechaSistema > "evento"."FechaRealizacion" THEN 'ERR' ELSE 'OK' END AS "ResultadoFechaRealizacion"
									, CASE WHEN "evento"."IdEstadoJunta" IN ('ANU','CER') THEN 'ERR' ELSE 'OK' END AS "ResultadoEstado"
							FROM :p_JuntaAccionista "p_evento"
							LEFT OUTER JOIN "BSCL.Custodia.BD::CUS.EventoJuntaAccionista" AS "evento" ON ("evento"."IdEventoJuntaAccionista" = "p_evento"."IdEventoJuntaAccionista" AND "evento"."IdEmpresa" = :p_IdEmpresa);
							
		v_errores = SELECT "IdInterno",
						   "IdEventoJuntaAccionista",
						   "ResultadoFechaCierre",
						   "ResultadoIdEventoJuntaAccionista",
						   "ResultadoFechaRealizacion",
						   "ResultadoEstado",
						   "FechaCierre",
						   "IdEstadoJunta",
						   "FechaRealizacion"
					FROM :v_junta_accionista
					WHERE 'ERR' IN ("ResultadoFechaCierre", "ResultadoIdEventoJuntaAccionista", "ResultadoFechaRealizacion", "ResultadoEstado");
		
		v_junta_accionista_validadas = SELECT "IdInterno"
											, "IdEventoJuntaAccionista"
											, "FechaCierre"
											, "Lugar"
											, "Instruccion"
											, "CuadraturaDCV"
											, "Materias"
											, "IdEmpresa"
									   FROM :v_junta_accionista
									   WHERE 'ERR' NOT IN ("ResultadoFechaCierre", "ResultadoIdEventoJuntaAccionista", "ResultadoFechaRealizacion", "ResultadoEstado");
		
		v_out_ErroresDetalle = SELECT "IdInterno", 'FECHA_CIERRE_INVALIDA' AS "Codigo", "FechaCierre" AS "ValorParametro", 'FechaCierre' AS "NombreParametro"
							   FROM :v_errores WHERE "ResultadoFechaCierre" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno", 'EVENTO_NO_EXISTE' AS "Codigo", "IdEventoJuntaAccionista" AS "ValorParametro", 'IdEventoJuntaAccionista' AS "NombreParametro"
							   FROM :v_errores WHERE "ResultadoIdEventoJuntaAccionista" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno", 'JUNTA_REALIZADA' AS "Codigo", "FechaRealizacion" AS "ValorParametro", 'FechaRealizacion' AS "NombreParametro"
							   FROM :v_errores WHERE "ResultadoFechaRealizacion" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno", CASE WHEN "IdEstadoJunta" = 'ANU' THEN 'JUNTA_ANULADA' 
							   							WHEN "IdEstadoJunta" = 'CER' THEN 'JUNTA_CERRADA' END AS "Codigo", "FechaRealizacion" AS "ValorParametro", 'FechaRealizacion' AS "NombreParametro"
							   FROM :v_errores WHERE "ResultadoEstado" = 'ERR'
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) into v_count_juntas_validadas
		FROM :v_junta_accionista_validadas;
		
		IF v_count_juntas_validadas = 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF; 
		
		UPDATE EVENTO
		SET "FechaCierre" = CASE WHEN EVENTO."IdEstadoCustodia" = 'PRO' THEN EVENTO."FechaCierre" ELSE P_EVENTO."FechaCierre" END,
			"Lugar" = P_EVENTO."Lugar",
			"Instruccion" = P_EVENTO."Instruccion",
			"CuadraturaDCV" = P_EVENTO."CuadraturaDCV",
			"Materias" = P_EVENTO."Materias"
		FROM "BSCL.Custodia.BD::CUS.EventoJuntaAccionista" AS EVENTO
		INNER JOIN :v_junta_accionista_validadas AS P_EVENTO ON (EVENTO."IdEventoJuntaAccionista" = P_EVENTO."IdEventoJuntaAccionista" AND EVENTO."IdEmpresa" = P_EVENTO."IdEmpresa");
		
		UPDATE CLIENTES_CON_DERECHO
		SET "Instruccion" = P_EVENTO."Instruccion"
		FROM "BSCL.Custodia.BD::CUS.ClienteConDerechoJuntaAccionista" AS CLIENTES_CON_DERECHO
		INNER JOIN :v_junta_accionista_validadas AS P_EVENTO ON (P_EVENTO."IdEventoJuntaAccionista" = CLIENTES_CON_DERECHO."IdEventoJuntaAccionista")
		WHERE CLIENTES_CON_DERECHO."IdTipoInstruccion" = 'COR' AND "IdEstado" = 'ACT';
		
		p_out_Resultado = SELECT "IdInterno"
								, "IdEventoJuntaAccionista"
								, "FechaCierre"
								, "Lugar"
								, "Instruccion"
								, "CuadraturaDCV"
								, "Materias"
								, "IdEmpresa"
						 FROM :v_junta_accionista_validadas;
		
		v_log = SELECT "IdEventoJuntaAccionista" AS "IdCabeceraLog"
						,'MODIFICACION' AS "IdTipoLog"
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
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Custodia.BD.Servicios::EVC.ModificarEventoJuntaAccionista','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		END;
	END;					 
END;
