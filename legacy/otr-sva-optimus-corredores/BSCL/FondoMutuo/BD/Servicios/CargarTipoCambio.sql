SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CargarTipoCambio";

CREATE PROCEDURE
--  Proposito: Carga Tipo Cambio, para los instrumentos de fondos mutuos. Dejando un respaldo en la tabla de tipo cambio historico.
--  Autor: - Gat-Blac 
--  Fecha Creacion: 07/10/2016
"BSCL.FondoMutuo.BD.Servicios::CargarTipoCambio"(
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 						BIGINT,
    IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_UsaReglasNegocio 				NVARCHAR(1),
    IN p_SimularOperacion 				NVARCHAR(1),
    IN p_TipoCambio						TABLE	(
											"IDSERIEFONDO"			NVARCHAR(40),
											"FECHACIERRE"			NVARCHAR(10),
											"VALORDOLARAPORTE"		DECIMAL(26,8),
											"VALORDOLARRESCATE"		DECIMAL(26,8),
											"OBSERVACION"			NVARCHAR(100),
											"MENSAJEVALIDACION" 	NVARCHAR(5000)
    									),
    OUT p_out_Resultado					TABLE	(
										"IdSerieFondo"			NVARCHAR(40),
										"FechaVigencia"			DATE,
										"ValorDolarAporte"		DECIMAL(26,8),
										"ValorDolarRescate"		DECIMAL(26,8),
										"Observacion"			NVARCHAR(100),
										"FechaCalculo"			DATE,
										"Estado"				NVARCHAR(3),
										"DescEstado"			NVARCHAR(520),
										"IdEmpresa"				BIGINT
									),
	OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
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
	DECLARE v_out_Resultado				TABLE ("IdSerieFondo" NVARCHAR(40),"FechaVigencia" DATE,"ValorDolarAporte" DECIMAL(26,8),"ValorDolarRescate" DECIMAL(26,8),"Observacion" NVARCHAR(100),"FechaCalculo" DATE,"Estado" NVARCHAR(3),"DescEstado" NVARCHAR(100),"IdEmpresa" BIGINT);
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_Resultado = SELECT '' AS "IdSerieFondo",'0001-01-01' AS "FechaVigencia",0 AS "ValorDolarAporte",0 AS "ValorDolarRescate",'' AS "Observacion",'0001-01-01' AS "FechaCalculo",'' AS "Estado",'' AS "DescEstado",0 AS "IdEmpresa" FROM DUMMY WHERE 1=2;
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
			
			p_out_Resultado = SELECT
									"res"."IdSerieFondo"
									,"res"."FechaVigencia"
									,"res"."ValorDolarAporte"
									,"res"."ValorDolarRescate"
									,"res"."Observacion"
									,"res"."FechaCalculo"
									,"res"."Estado"
									,COALESCE("msj"."Descripcion","res"."DescEstado") AS "DescEstado"
									,"res"."IdEmpresa"
							  FROM :v_out_Resultado as "res"
							  LEFT OUTER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "res"."DescEstado" = "msj"."Codigo";
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
			DECLARE v_resultadoRegla 			  NVARCHAR(40);
			DECLARE v_count						  BIGINT;
			DECLARE v_count_valores				  BIGINT;
			DECLARE v_count_valores_maestro		  BIGINT;
			DECLARE v_FechaSistema 				  NVARCHAR(10);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CARGAR TIPO CAMBIO','INICIO','INICIO');
			
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			SELECT COUNT(1) as "Count" into v_count_valores
			FROM :p_TipoCambio;
			
			IF (v_count_valores = 0) THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_VALORES_TIPO_CAMBIO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_tipo_cambio_maestro = SELECT 
											"P_VALOR_TIPO_CAMBIO"."IDSERIEFONDO" as "IdSerieFondo"
											,"P_VALOR_TIPO_CAMBIO"."FECHACIERRE" as "FechaCierre"
											,"P_VALOR_TIPO_CAMBIO"."VALORDOLARAPORTE" as "ValorDolarAporte"
											,"P_VALOR_TIPO_CAMBIO"."VALORDOLARRESCATE" as "ValorDolarRescate"
											,"P_VALOR_TIPO_CAMBIO"."OBSERVACION" as "Observacion"
											,:p_IdEmpresa AS "IdEmpresa"
											,:v_FechaSistema AS "FechaCarga"
											,CURRENT_UTCTIMESTAMP as "FechaIngresoRegistro"
											,CASE WHEN COALESCE("SerieFondo"."IdSerieFondo",'-1')!='-1' THEN 'OK' ELSE 'ERR' END AS "ResultadoSerie"
											,CASE WHEN COALESCE("TipoCambio"."FechaCierre",'0001-01-01') != "P_VALOR_TIPO_CAMBIO"."FECHACIERRE" THEN 'OK' ELSE 'ADV' END AS "ResultadoFecha"
											,CASE WHEN "P_VALOR_TIPO_CAMBIO"."VALORDOLARAPORTE" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoValorDolarAporte"
											,CASE WHEN "P_VALOR_TIPO_CAMBIO"."VALORDOLARRESCATE" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoValorDolarRescate"
											,CASE WHEN COALESCE("P_VALOR_TIPO_CAMBIO"."FECHACIERRE",'0001-01-01') > :v_FechaSistema THEN 'ERR' ELSE 'OK' END AS "ResultadoFechaActual"
									   FROM  :p_TipoCambio AS "P_VALOR_TIPO_CAMBIO"
									   LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("P_VALOR_TIPO_CAMBIO"."IDSERIEFONDO" = "SerieFondo"."IdSerieFondo" and "SerieFondo"."IdEmpresa" = :p_IdEmpresa)
									   LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio" AS "TipoCambio" ON ("P_VALOR_TIPO_CAMBIO"."IDSERIEFONDO" = "TipoCambio"."IdSerieFondo" and "TipoCambio"."IdEmpresa" = :p_IdEmpresa)
									   WHERE "MENSAJEVALIDACION" = 'OK';
			
			v_tipo_cambio_duplicados = SELECT COUNT(1) AS "Count",
												"P_VALOR_TIPO_CAMBIO"."IDSERIEFONDO" as "IdSerieFondo"
										 FROM :p_TipoCambio AS "P_VALOR_TIPO_CAMBIO"
										 GROUP BY "IDSERIEFONDO";
			
			BEGIN
				
				v_tipo_cambio_validacion = SELECT "TIPO_CAMBIO"."IdSerieFondo"
												,"TIPO_CAMBIO"."FechaCierre"
												,"TIPO_CAMBIO"."ValorDolarAporte"
												,"TIPO_CAMBIO"."ValorDolarRescate"
												,"TIPO_CAMBIO"."Observacion"
												,"TIPO_CAMBIO"."IdEmpresa"
												,"TIPO_CAMBIO"."FechaCarga"
												,"TIPO_CAMBIO"."FechaIngresoRegistro"
												,CASE WHEN "TIPO_CAMBIO"."ResultadoSerie" = 'ERR' OR "TIPO_CAMBIO"."ResultadoValorDolarAporte" = 'ERR' OR "TIPO_CAMBIO"."ResultadoValorDolarRescate" = 'ERR' OR "TIPO_CAMBIO_DUPLICADO"."Count" > 1 OR "TIPO_CAMBIO"."ResultadoFechaActual" = 'ERR' THEN 'ERR'
													  WHEN "TIPO_CAMBIO"."ResultadoFecha" = 'ADV' THEN 'ADV'
													  ELSE 'OK' END AS "Estado"
												,CASE WHEN "TIPO_CAMBIO_DUPLICADO"."Count" > 1 THEN 'VALOR_CUOTA_DUPLICADO'
													  WHEN "TIPO_CAMBIO"."ResultadoSerie" = 'ERR' THEN 'SERIE_NO_EXISTE'
													  WHEN "TIPO_CAMBIO"."ResultadoValorDolarAporte" = 'ERR' THEN 'VALOR_DOLAR_APORTE_INVALIDO'
													  WHEN "TIPO_CAMBIO"."ResultadoValorDolarRescate" = 'ERR' THEN 'VALOR_DOLAR_RESCATE_INVALIDO'
													  WHEN "TIPO_CAMBIO"."ResultadoFechaActual" = 'ERR' THEN 'FECHA_VALOR_DOLAR_APORTE_RESCATE_INCORRECTA'
													  WHEN "TIPO_CAMBIO"."ResultadoFecha" = 'ADV' THEN 'VALOR_EXISTE_FECHA'
													  ELSE 'OK' END AS "DescEstado"
											FROM :v_tipo_cambio_maestro AS "TIPO_CAMBIO"
											INNER JOIN :v_tipo_cambio_duplicados AS "TIPO_CAMBIO_DUPLICADO" ON ("TIPO_CAMBIO"."IdSerieFondo" = "TIPO_CAMBIO_DUPLICADO"."IdSerieFondo");
				
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", "DescEstado" as "Codigo", '' as "ValorParametro", '' AS "NombreParametro"
									   		FROM :v_tipo_cambio_validacion AS "ValoresTipoCambioMaestro" WHERE "Estado" = 'ERR'
									   UNION ALL
									   SELECT 0 AS "IdInterno", 'REGISTRO_CON_ERRORES' as "Codigo", "P_TIPO_CAMBIO"."IDSERIEFONDO" as "ValorParametro", 'IdSerieFondo' AS "NombreParametro"
									   		FROM :p_TipoCambio AS "P_TIPO_CAMBIO" WHERE "MENSAJEVALIDACION" != 'OK';
				
				v_out_AdvertenciasDetalle = SELECT 0 AS "IdInterno", "DescEstado" as "Codigo", 'N' AS "RequiereAutorizacion", '' as "ValorParametro", '' AS "NombreParametro"
									   		FROM :v_tipo_cambio_validacion AS "ValoresTipoCambioMaestro" WHERE "Estado" = 'ADV';
									   		
			END;
			
			v_valores_finales = SELECT 
										"TIPO_CAMBIO_MAESTRO"."IdSerieFondo"
										,"TIPO_CAMBIO_MAESTRO"."FechaCierre"
										,"TIPO_CAMBIO_MAESTRO"."ValorDolarAporte"
										,"TIPO_CAMBIO_MAESTRO"."ValorDolarRescate"
										,"TIPO_CAMBIO_MAESTRO"."Observacion"
										,"TIPO_CAMBIO_MAESTRO"."FechaCarga"
										,"TIPO_CAMBIO_MAESTRO"."FechaIngresoRegistro"
										,"TIPO_CAMBIO_MAESTRO"."IdEmpresa"
								FROM :v_tipo_cambio_validacion AS "TIPO_CAMBIO_MAESTRO"
								WHERE "TIPO_CAMBIO_MAESTRO"."Estado" IN ('OK','ADV');
			
			v_valores_respaldo_maestro = SELECT 
										"TIPO_CAMBIO"."IdSerieFondo"
										,"TIPO_CAMBIO"."FechaCierre"
										,"TIPO_CAMBIO"."ValorDolarAporte"
										,"TIPO_CAMBIO"."ValorDolarRescate"
										,"TIPO_CAMBIO"."Observacion"
										,"TIPO_CAMBIO"."FechaCarga"
										,"TIPO_CAMBIO_FINAL"."FechaIngresoRegistro"
										,"TIPO_CAMBIO"."IdEmpresa"
								FROM :v_valores_finales AS "TIPO_CAMBIO_FINAL"
								INNER JOIN "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio" AS "TIPO_CAMBIO" ON ("TIPO_CAMBIO_FINAL"."IdSerieFondo" = "TIPO_CAMBIO"."IdSerieFondo");
			
			v_valores_respaldo_finales = SELECT 
												"BSCL.FondoMutuo.BD.Secuencias::ValorTipoCambioHistorico".NEXTVAL AS "IdValorTipoCambioHistorico"
												,"VALOR_RESPALDO_MAESTRO"."IdSerieFondo"
												,"VALOR_RESPALDO_MAESTRO"."FechaCierre"
												,"VALOR_RESPALDO_MAESTRO"."ValorDolarAporte"
												,"VALOR_RESPALDO_MAESTRO"."ValorDolarRescate"
												,"VALOR_RESPALDO_MAESTRO"."Observacion"
												,"VALOR_RESPALDO_MAESTRO"."FechaCarga"
												,"VALOR_RESPALDO_MAESTRO"."FechaIngresoRegistro"
												,"VALOR_RESPALDO_MAESTRO"."IdEmpresa"
										FROM :v_valores_respaldo_maestro AS "VALOR_RESPALDO_MAESTRO";
			IF (:p_SimularOperacion = 'N') THEN
				INSERT INTO "BSCL.FondoMutuo.BD::FMM.ValorTipoCambioHistorico"(
					"IdValorTipoCambioHistorico", "IdSerieFondo", "FechaCierre", "ValorDolarAporte", "ValorDolarRescate", "Observacion", "FechaCarga", "FechaIngresoRegistro", "IdEmpresa"
				)
				SELECT "IdValorTipoCambioHistorico", "IdSerieFondo", "FechaCierre", "ValorDolarAporte", "ValorDolarRescate", "Observacion", "FechaCarga", "FechaIngresoRegistro", "IdEmpresa"
				FROM :v_valores_respaldo_finales;
				
				DELETE FROM "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio" AS "TIPO_CAMBIO"
				WHERE EXISTS (SELECT 1 FROM :v_valores_respaldo_finales  AS "VALOR_RESPALDO_FINAL" WHERE "VALOR_RESPALDO_FINAL"."IdSerieFondo" = "TIPO_CAMBIO"."IdSerieFondo");
				
				INSERT INTO "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio"
				(
					"IdSerieFondo", "FechaCierre", "ValorDolarAporte", "ValorDolarRescate", "Observacion", "FechaCarga", "FechaIngresoRegistro", "IdEmpresa"
				)
				SELECT "IdSerieFondo", "FechaCierre", "ValorDolarAporte", "ValorDolarRescate", "Observacion", "FechaCarga", "FechaIngresoRegistro", "IdEmpresa"
				FROM :v_valores_finales;
			END IF;
			
			v_out_Resultado = SELECT
									"IdSerieFondo"
									,"FechaCierre" as "FechaVigencia"
									,"ValorDolarAporte"
									,"ValorDolarRescate"
									,"Observacion"
									,"FechaCarga" as "FechaCalculo"
									,"Estado"
									,"DescEstado"
									,"IdEmpresa"
							  FROM :v_tipo_cambio_validacion;
			
			v_log = SELECT	0 AS "IdCabeceraLog"
					,'MODIFICACION' AS "IdTipoLog"
					,'FMM' AS "IdModulo"
					,:p_IdUsuario AS "IdUsuario"
					,:p_IdEmpresa AS "IdEmpresa"
					,"FechaCarga" AS "FechaSistemaLog"
					,:p_IdUsuario AS "IdUsuarioAtendido"
					,0  AS "IdPersona"
					,0  AS "IdCliente"
					,'' AS "EstadoCliente"
					,'' AS "NombreCliente"
					,'' AS "IdTipoDocumentoIdentidad"
					,'' AS "IdDocumentoIdentidad"
					,0 AS "IdPortafolio"
					,'' AS "EstadoPortafolio"
					,'TIPO_CAMBIO_FMM' AS "IdTipoRegistro"
					,0 AS "IdRegistro"
					,'' AS "EstadoRegistro"
					,'' AS "IdTipoOperacion"
					,0 AS "Folio"
					,0 AS "Cantidad"
					,0 AS "PrecioTasa"
					,"ValorDolarAporte" AS "Monto"
					,'' AS "IdMonedaOrigen"
					,'' AS "IdMonedaLiquidacion"
					,'' AS "TipoCambio"
					,"IdSerieFondo" AS "Nemo"
					,0 AS "IdProceso"
					,'' AS "EstadoProceso"
					,'' AS "NombreProceso"
				FROM :v_valores_finales;
			
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP,1,'CARGAR TIPO CAMBIO','FIN','FIN');
		
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
