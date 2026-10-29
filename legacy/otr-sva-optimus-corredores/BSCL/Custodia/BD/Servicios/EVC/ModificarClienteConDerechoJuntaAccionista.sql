SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ModificarClienteConDerechoJuntaAccionista";

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ModificarClienteConDerechoJuntaAccionista" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 					BIGINT,
    IN p_IdUsuario 					BIGINT,
    IN p_IdModulo					NVARCHAR(3), 
    IN p_ClienteDerecho				TABLE(
    									"IdInterno"								BIGINT,
    									"IdClienteConDerechoJuntaAccionista"	BIGINT,
    									"IdEventoJuntaAccionista"				BIGINT,
    									"IdInstrumento"							BIGINT,
    									"CantidadDerecho"						DECIMAL(26,8),
    									"Instruccion"							NVARCHAR(500)
    								),
    OUT p_out_Resultado				TABLE(
    									"IdInterno"								BIGINT,
    									"IdClienteConDerechoJuntaAccionista"	BIGINT,
    									"IdEventoJuntaAccionista"				BIGINT,
    									"IdInstrumento"							BIGINT,
    									"CantidadDerecho"						DECIMAL(26,8),
    									"Instruccion"							NVARCHAR(500)
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
		DECLARE v_count_clientes			BIGINT;
		DECLARE v_count_juntas_validadas	BIGINT;
		DECLARE v_count_clientes_validados	BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Custodia.BD.Servicios::EVC.ModificarClienteConDerechoJuntaAccionista','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		SELECT COUNT(1) into v_count_clientes
		FROM :p_ClienteDerecho;
				
		IF v_count_clientes = 0 THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_CLIENTES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") as "Count" into v_count
		FROM :p_ClienteDerecho
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		IF (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_junta_accionista = SELECT "p_evento"."IdInterno"
									, "p_evento"."IdEventoJuntaAccionista"
									, "evento"."IdEmpresa"
									, "evento"."IdEmisor"
									, "evento"."FechaRealizacion"
									, "evento"."IdEstadoJunta"
									, "evento"."IdEstadoCustodia"
									, CASE WHEN "evento"."IdEstadoCustodia" = 'PEN' THEN 'ERR' ELSE 'OK' END AS "ResultadoEstadoCustodia"
									, CASE WHEN COALESCE("evento"."IdEventoJuntaAccionista",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoIdEventoJuntaAccionista"
									, CASE WHEN :v_FechaSistema > "evento"."FechaRealizacion" THEN 'ERR' ELSE 'OK' END AS "ResultadoFechaRealizacion"
									, CASE WHEN "evento"."IdEstadoJunta" IN ('ANU','CER') THEN 'ERR' ELSE 'OK' END AS "ResultadoEstado"
							FROM :p_ClienteDerecho "p_evento"
							LEFT OUTER JOIN "BSCL.Custodia.BD::CUS.EventoJuntaAccionista" AS "evento" ON ("evento"."IdEventoJuntaAccionista" = "p_evento"."IdEventoJuntaAccionista" AND "evento"."IdEmpresa" = :p_IdEmpresa);
		
		v_errores = SELECT "IdInterno",
						   "IdEventoJuntaAccionista",
						   "ResultadoEstadoCustodia",
						   "ResultadoIdEventoJuntaAccionista",
						   "ResultadoFechaRealizacion",
						   "ResultadoEstado",
						   "IdEstadoJunta",
						   "IdEstadoCustodia",
						   "FechaRealizacion"
					FROM :v_junta_accionista
					WHERE 'ERR' IN ("ResultadoEstadoCustodia", "ResultadoIdEventoJuntaAccionista", "ResultadoFechaRealizacion", "ResultadoEstado");
		
		v_junta_accionista_validadas = SELECT "IdInterno"
											, "IdEventoJuntaAccionista"
											, "IdEmisor"
									   FROM :v_junta_accionista
									   WHERE 'ERR' NOT IN ("ResultadoEstadoCustodia", "ResultadoIdEventoJuntaAccionista", "ResultadoFechaRealizacion", "ResultadoEstado");
		
		v_out_ErroresDetalle = SELECT "IdInterno", 'ESTADO_RESPALDO_CUSTODIA_PENDIENTE' AS "Codigo", "IdEstadoCustodia" AS "ValorParametro", 'IdEstadoCustodia' AS "NombreParametro"
							   FROM :v_errores WHERE "ResultadoEstadoCustodia" = 'ERR'
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
		
		v_cliente_derecho = SELECT "p_cliente"."IdInterno"
								   ,"p_cliente"."IdClienteConDerechoJuntaAccionista"
								   ,"p_cliente"."IdEventoJuntaAccionista"
								   ,"p_cliente"."IdInstrumento"
								   ,"p_cliente"."CantidadDerecho"
								   ,"p_cliente"."Instruccion"
								   ,"junta_validadas"."IdEmisor"
								   ,CASE WHEN COALESCE("p_cliente"."Instruccion", '') = "cliente"."Instruccion" THEN 'N' ELSE 'S' END AS "CambiaInstruccion" 
								   ,CASE WHEN COALESCE("cliente"."IdCliente",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoIdClienteDerecho"
								   ,CASE WHEN COALESCE("Instrumento"."IdInstrumento",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoInstrumento"
								   ,CASE WHEN "p_cliente"."CantidadDerecho" < 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoCantidadDerecho"
							FROM :p_ClienteDerecho AS "p_cliente"
							INNER JOIN :v_junta_accionista_validadas AS "junta_validadas" ON ("p_cliente"."IdEventoJuntaAccionista" = "junta_validadas"."IdEventoJuntaAccionista" AND "p_cliente"."IdInterno" = "junta_validadas"."IdInterno")
							LEFT OUTER JOIN "BSCL.Custodia.BD::CUS.ClienteConDerechoJuntaAccionista" AS "cliente" ON ("cliente"."IdEventoJuntaAccionista" = "p_cliente"."IdEventoJuntaAccionista" 
																												 AND "cliente"."IdClienteConDerechoJuntaAccionista" = "p_cliente"."IdClienteConDerechoJuntaAccionista"
																												 AND "cliente"."IdEstado" = 'ACT')
							LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("p_cliente"."IdInstrumento" = "Instrumento"."IdInstrumento" 
																										AND "junta_validadas"."IdEmisor" = "Instrumento"."IdEmisor");
		
		v_errores_cliente_derecho = SELECT "IdInterno"
										   ,"IdClienteConDerechoJuntaAccionista"
										   ,"IdInstrumento"
										   ,"CantidadDerecho"
										   ,"ResultadoIdClienteDerecho"
										   ,"ResultadoInstrumento"
										   ,"ResultadoCantidadDerecho"
									FROM :v_cliente_derecho
									WHERE 'ERR' IN ("ResultadoIdClienteDerecho", "ResultadoInstrumento", "ResultadoCantidadDerecho");
		
		v_cliente_validados = SELECT "CLIENTE"."IdInterno"
									 ,"CLIENTE"."IdClienteConDerechoJuntaAccionista"
									 ,"CLIENTE"."IdEventoJuntaAccionista"
									 ,"CLIENTE"."IdInstrumento"
									 ,"CLIENTE"."CantidadDerecho"
									 ,"CLIENTE"."Instruccion"
									 ,"CLIENTE"."CambiaInstruccion"
									 , :v_FechaSistema AS "FechaModificacion"
									 , CURRENT_UTCTIMESTAMP AS "FechaModificacionRegistro"
									 ,:p_IdUsuario AS "IdUsuario"
							  FROM :v_cliente_derecho AS "CLIENTE"
							  WHERE 'ERR' NOT IN ("ResultadoIdClienteDerecho", "ResultadoInstrumento", "ResultadoCantidadDerecho");
		
		v_out_ErroresDetalle = SELECT "IdInterno", 'CLIENTE_DERECHO_NO_EXISTE' AS "Codigo", "IdClienteConDerechoJuntaAccionista" AS "ValorParametro", 'IdClienteConDerechoJuntaAccionista' AS "NombreParametro"
							   FROM :v_errores_cliente_derecho WHERE "ResultadoIdClienteDerecho" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno", 'INSTRUMENTO_NO_EXISTE' AS "Codigo", "IdInstrumento" AS "ValorParametro", 'IdInstrumento' AS "NombreParametro"
							   FROM :v_errores_cliente_derecho WHERE "ResultadoInstrumento" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno", 'CANTIDAD_DERECHO_INVALIDA' AS "Codigo", "CantidadDerecho" AS "ValorParametro", 'CantidadDerecho' AS "NombreParametro"
							   FROM :v_errores_cliente_derecho WHERE "ResultadoCantidadDerecho" = 'ERR'
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) into v_count_clientes_validados
		FROM :v_cliente_validados;
		
		IF v_count_clientes_validados = 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		UPDATE CLIENTE_DERECHO
		SET "Instruccion" = CASE WHEN P_CLIENTE_DERECHO."CambiaInstruccion" = 'S' THEN P_CLIENTE_DERECHO."Instruccion" ELSE CLIENTE_DERECHO."Instruccion" END,
			"CantidadDerecho" = P_CLIENTE_DERECHO."CantidadDerecho",
			"IdInstrumento" =  P_CLIENTE_DERECHO."IdInstrumento",
			"IdTipoInstruccion" = CASE WHEN P_CLIENTE_DERECHO."CambiaInstruccion" = 'S' THEN 'ESP' ELSE CLIENTE_DERECHO."IdTipoInstruccion" END,
			"FechaModificacion" = P_CLIENTE_DERECHO."FechaModificacion",
			"FechaModificacionRegistro" = P_CLIENTE_DERECHO."FechaModificacionRegistro",
			"IdUsuario" = P_CLIENTE_DERECHO."IdUsuario"
		FROM "BSCL.Custodia.BD::CUS.ClienteConDerechoJuntaAccionista" AS CLIENTE_DERECHO
		INNER JOIN :v_cliente_validados AS P_CLIENTE_DERECHO ON (CLIENTE_DERECHO."IdClienteConDerechoJuntaAccionista" = P_CLIENTE_DERECHO."IdClienteConDerechoJuntaAccionista"
																 AND CLIENTE_DERECHO."IdEventoJuntaAccionista" = P_CLIENTE_DERECHO."IdEventoJuntaAccionista");
		
		p_out_Resultado = SELECT "IdInterno"
								, "IdClienteConDerechoJuntaAccionista"
								, "IdEventoJuntaAccionista"
								, "IdInstrumento"
								, "CantidadDerecho"
								, "Instruccion"
						 FROM :v_cliente_validados;
		
		v_log = SELECT "IdClienteConDerechoJuntaAccionista" AS "IdCabeceraLog"
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
						,'CLIENTE_DERECHO_JUNTA_ACCIONISTA' AS "IdTipoRegistro"
						,"IdClienteConDerechoJuntaAccionista" AS "IdRegistro"
						,'ACT' AS "EstadoRegistro"
						,'' AS "IdTipoOperacion"
						,"IdClienteConDerechoJuntaAccionista" AS "Folio"
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
					FROM :v_cliente_validados;
		
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
			
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Custodia.BD.Servicios::EVC.ModificarClienteConDerechoJuntaAccionista','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		END;
	END;
END;