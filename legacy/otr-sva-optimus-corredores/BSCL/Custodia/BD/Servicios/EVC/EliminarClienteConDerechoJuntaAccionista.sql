SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.EliminarClienteConDerechoJuntaAccionista";

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.EliminarClienteConDerechoJuntaAccionista" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 					BIGINT,
    IN p_IdUsuario 					BIGINT,
    IN p_IdModulo					NVARCHAR(3), 
    IN p_ClienteDerecho				TABLE(
    									"IdInterno"								BIGINT,
    									"IdClienteConDerechoJuntaAccionista"	BIGINT,
    									"IdEventoJuntaAccionista"				BIGINT
    								),
    OUT p_out_Resultado				TABLE(
    									"IdInterno"								BIGINT,
    									"IdClienteConDerechoJuntaAccionista"	BIGINT,
    									"IdEventoJuntaAccionista"				BIGINT,
    									"Resultado"								NVARCHAR(3),
    									"Codigo"								NVARCHAR(100)
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
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Custodia.BD.Servicios::EVC.EliminarClienteConDerechoJuntaAccionista','INICIO','INICIO');
		
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
									, "p_evento"."IdClienteConDerechoJuntaAccionista"
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
						   "IdClienteConDerechoJuntaAccionista",
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
		
		
		v_cliente_derecho = SELECT "p_cliente"."IdInterno"
								   ,"p_cliente"."IdClienteConDerechoJuntaAccionista"
								   ,"p_cliente"."IdEventoJuntaAccionista"
								   ,CASE WHEN COALESCE("cliente"."IdCliente",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoIdClienteDerecho"
							FROM :p_ClienteDerecho AS "p_cliente"
							INNER JOIN :v_junta_accionista_validadas AS "junta_validadas" ON ("p_cliente"."IdEventoJuntaAccionista" = "junta_validadas"."IdEventoJuntaAccionista" AND "p_cliente"."IdInterno" = "junta_validadas"."IdInterno")
							LEFT OUTER JOIN "BSCL.Custodia.BD::CUS.ClienteConDerechoJuntaAccionista" AS "cliente" ON ("cliente"."IdEventoJuntaAccionista" = "p_cliente"."IdEventoJuntaAccionista" 
																												 AND "cliente"."IdClienteConDerechoJuntaAccionista" = "p_cliente"."IdClienteConDerechoJuntaAccionista"
																												 AND "cliente"."IdEstado" = 'ACT');
		
		
		v_errores_cliente_derecho = SELECT "IdInterno"
										   ,"IdClienteConDerechoJuntaAccionista"
										   ,"IdEventoJuntaAccionista"
										   ,"ResultadoIdClienteDerecho"
									FROM :v_cliente_derecho
									WHERE 'ERR' = "ResultadoIdClienteDerecho";
		
		v_cliente_validados = SELECT "CLIENTE"."IdInterno"
									 ,"CLIENTE"."IdEventoJuntaAccionista"
									 ,"CLIENTE"."IdClienteConDerechoJuntaAccionista"
							  FROM :v_cliente_derecho AS "CLIENTE"
							  WHERE 'ERR' != "ResultadoIdClienteDerecho";
		
		
		UPDATE CLIENTE_DERECHO
		SET "IdEstado" = 'INA'
		FROM "BSCL.Custodia.BD::CUS.ClienteConDerechoJuntaAccionista" CLIENTE_DERECHO
		INNER JOIN :v_cliente_validados AS P_CLIENTE_DERECHO ON (CLIENTE_DERECHO."IdClienteConDerechoJuntaAccionista" = P_CLIENTE_DERECHO."IdClienteConDerechoJuntaAccionista"
																 AND CLIENTE_DERECHO."IdEventoJuntaAccionista" = P_CLIENTE_DERECHO."IdEventoJuntaAccionista");
		
		p_out_Resultado = SELECT "IdInterno"
								, "IdClienteConDerechoJuntaAccionista"
								, "IdEventoJuntaAccionista"
								, 'OK' AS "Resultado"
								, 'UI_EXITO' AS "Codigo"
						 FROM :v_cliente_validados
						 UNION ALL
						 SELECT "IdInterno"
						 		,"IdClienteConDerechoJuntaAccionista"
						 		,"IdEventoJuntaAccionista"
						 		,'ERR' AS "Resultado"
						 		,CASE WHEN "ResultadoEstadoCustodia" = 'ERR' THEN 'ESTADO_RESPALDO_CUSTODIA_PENDIENTE'
						 		  	  WHEN "ResultadoIdEventoJuntaAccionista" = 'ERR' THEN 'EVENTO_NO_EXISTE'
						 		  	  WHEN "ResultadoFechaRealizacion" = 'ERR' THEN 'JUNTA_REALIZADA'
						 		 	  WHEN "ResultadoEstado" = 'ERR' THEN CASE WHEN "IdEstadoJunta" = 'ANU' THEN 'JUNTA_ANULADA' 
							   									  WHEN "IdEstadoJunta" = 'CER' THEN 'JUNTA_CERRADA' END 
							   	END AS "Codigo"
						 FROM :v_errores
						 UNION ALL
						 SELECT "IdInterno"
						 		,"IdClienteConDerechoJuntaAccionista"
						 		,"IdEventoJuntaAccionista"
						 		,'ERR' AS "Resultado"
						 		,'CLIENTE_DERECHO_NO_EXISTE' AS "Resultado"
						 FROM :v_errores_cliente_derecho;
		
		v_log = SELECT "IdClienteConDerechoJuntaAccionista" AS "IdCabeceraLog"
						,'ELIMINACION' AS "IdTipoLog"
						,'CUS' AS "IdModulo"
						,:p_IdUsuario AS "IdUsuario"
						,:p_IdEmpresa AS "IdEmpresa"
						,:v_FechaSistema AS "FechaSistemaLog"
						,:p_IdUsuario AS "IdUsuarioAtendido"
						,0  AS "IdPersona"
						,0 AS "IdCliente"
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
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Custodia.BD.Servicios::EVC.EliminarClienteConDerechoJuntaAccionista','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		END;
	END;
END;