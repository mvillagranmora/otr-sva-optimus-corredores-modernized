SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CrearEventoJuntaAccionista";

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CrearEventoJuntaAccionista" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 					BIGINT,
    IN p_IdUsuario 					BIGINT,
    IN p_IdModulo					NVARCHAR(3), 
    IN p_JuntaAccionista			TABLE(
    									"IdInterno"				BIGINT,
    									"IdTipoJuntaAccionista"	NVARCHAR(3),
    									"FechaRealizacion"		NVARCHAR(10),
    									"HoraRealizacion"		NVARCHAR(8),
    									"IdEmisor"				NVARCHAR(40),
    									"FechaCierre"			NVARCHAR(10),
    									"Lugar"					NVARCHAR(100),
    									"Instruccion"			NVARCHAR(500),
    									"CuadraturaDCV"			DECIMAL(26,8),
    									"Materias"				NVARCHAR(500)
    								),
    OUT p_out_Resultado				TABLE(
    									"IdInterno"					BIGINT,
    									"IdEventoJuntaAccionista"	BIGINT,
    									"IdTipoJuntaAccionista"		NVARCHAR(3),
    									"FechaRealizacion"			DATE,
    									"HoraRealizacion"			TIME,
    									"IdEmisor"					NVARCHAR(40),
    									"FechaCierre"				NVARCHAR(10),
    									"Lugar"						NVARCHAR(100),
    									"Instruccion"				NVARCHAR(500),
    									"CuadraturaDCV"				DECIMAL(26,8),
    									"Materias"					NVARCHAR(500),
    									"FechaIngreso"				DATE,
    									"FechaIngresoRegistro"		TIMESTAMP,
    									"IdUsuario"					BIGINT,
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
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Custodia.BD.Servicios::EVC.CrearEventoJuntaAccionista','INICIO','INICIO');
		
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
		
		v_junta_accionista = SELECT "JuntaAccionista"."IdInterno"
									,"JuntaAccionista"."IdTipoJuntaAccionista"
									,"JuntaAccionista"."FechaRealizacion"
									,"JuntaAccionista"."HoraRealizacion"
									,"JuntaAccionista"."IdEmisor"
									,"JuntaAccionista"."FechaCierre"
									,"JuntaAccionista"."Lugar"
									,"JuntaAccionista"."Instruccion"
									,"JuntaAccionista"."CuadraturaDCV"
									,"JuntaAccionista"."Materias"
									,'PEN' AS "IdEstadoCustodia"
									,'VIG' AS "IdEstadoJunta"
									,:p_IdEmpresa AS "IdEmpresa"
									,:p_IdUsuario AS "IdUsuario"
									,:v_FechaSistema AS "FechaIngreso"
									,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"
									,CASE WHEN "JuntaAccionista"."FechaRealizacion" < :v_FechaSistema THEN 'ERR' ELSE 'OK' END AS "ResultadoFechaRealizacion"
									,CASE WHEN "JuntaAccionista"."FechaCierre" < :v_FechaSistema THEN 'ERR' ELSE 'OK' END AS "ResultadoFechaCierre"
									,CASE WHEN COALESCE("Emisor"."IdEmisor",'') = '' THEN 'ERR' ELSE 'OK'  END AS "ResultadoEmisor"
							 FROM :p_JuntaAccionista AS "JuntaAccionista"
							 LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "Emisor" ON ("Emisor"."IdEmisor" = "JuntaAccionista"."IdEmisor");
							 
		v_errores = SELECT "IdInterno"
							,"IdTipoJuntaAccionista"
							,"FechaRealizacion"
							,"HoraRealizacion"
							,"IdEmisor"
							,"FechaCierre"
							,"Lugar"
							,"Instruccion"
							,"CuadraturaDCV"
							,"Materias"
							,"IdEmpresa"
							,"IdUsuario"
							,"FechaIngreso"
							,"FechaIngresoRegistro"
							,"ResultadoFechaRealizacion"
							,"ResultadoFechaCierre"
							,"ResultadoEmisor"
					FROM :v_junta_accionista
					WHERE 'ERR' IN ("ResultadoFechaRealizacion", "ResultadoFechaCierre", "ResultadoEmisor");
		
		v_junta_accionista_validadas = SELECT "IdInterno"
											, "BSCL.Custodia.BD.Secuencias::EventoJuntaAccionista".NEXTVAL AS "IdEventoJuntaAccionista"
											,"IdTipoJuntaAccionista"
											,"FechaRealizacion"
											,"HoraRealizacion"
											,"IdEmisor"
											,"FechaCierre"
											,"Lugar"
											,"Instruccion"
											,"CuadraturaDCV"
											,"Materias"
											,"IdEmpresa"
											,"IdUsuario"
											,"FechaIngreso"
											,"FechaIngresoRegistro"
											,"IdEstadoCustodia"
											,"IdEstadoJunta"
										FROM :v_junta_accionista
										WHERE 'ERR' NOT IN ("ResultadoFechaRealizacion", "ResultadoFechaCierre", "ResultadoEmisor");
		
		v_out_ErroresDetalle = SELECT "IdInterno", 'FECHA_REALIZACION_INVALIDA' AS "Codigo", "FechaRealizacion" AS "ValorParametro", 'FechaRealizacion' AS "NombreParametro"
							   FROM :v_errores WHERE "ResultadoFechaRealizacion" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno", 'FECHA_CIERRE_INVALIDA' AS "Codigo", "FechaCierre" AS "ValorParametro", 'FechaCierre' AS "NombreParametro"
							   FROM :v_errores WHERE "ResultadoFechaRealizacion" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno", 'EMISOR_NO_EXISTE' AS "Codigo", "IdEmisor" AS "ValorParametro", 'IdEmisor' AS "NombreParametro"
							   FROM :v_errores WHERE "ResultadoEmisor" = 'ERR'
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) into v_count_juntas_validadas
		FROM :v_junta_accionista_validadas;
		
		IF v_count_juntas_validadas = 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF; 
		
		INSERT INTO "BSCL.Custodia.BD::CUS.EventoJuntaAccionista"(
			"IdEventoJuntaAccionista", "IdTipoJuntaAccionista", "FechaCierre", "FechaRealizacion", "HoraRealizacion", "IdEmisor", "Materias", "IdEstadoCustodia", "IdEstadoJunta"
			,"Lugar", "FechaIngreso", "FechaIngresoRegistro", "Instruccion", "CuadraturaDCV", "IdUsuario", "IdEmpresa"
		)
		SELECT "IdEventoJuntaAccionista", "IdTipoJuntaAccionista", "FechaCierre", "FechaRealizacion", "HoraRealizacion", "IdEmisor", "Materias", "IdEstadoCustodia", "IdEstadoJunta"
			,"Lugar", "FechaIngreso", "FechaIngresoRegistro", "Instruccion", "CuadraturaDCV", "IdUsuario", "IdEmpresa"
		FROM :v_junta_accionista_validadas;
		
		p_out_Resultado = SELECT "IdInterno",
								"IdEventoJuntaAccionista",
								"IdTipoJuntaAccionista",
								"FechaRealizacion",
								"HoraRealizacion",
								"IdEmisor",
								"FechaCierre",
								"Lugar",
								"Instruccion",
								"CuadraturaDCV",
								"Materias",
								"FechaIngreso",
								"FechaIngresoRegistro",
								"IdUsuario",
								"IdEmpresa"
							FROM :v_junta_accionista_validadas;
		
		v_log = SELECT "IdEventoJuntaAccionista" AS "IdCabeceraLog"
						,'INGRESO' AS "IdTipoLog"
						,'CUS' AS "IdModulo"
						,:p_IdUsuario AS "IdUsuario"
						,:p_IdEmpresa AS "IdEmpresa"
						,TO_NVARCHAR("FechaIngreso", 'YYYY-MM-DD') AS "FechaSistemaLog"
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
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Custodia.BD.Servicios::EVC.CrearEventoJuntaAccionista','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		END;
	END;
END;