SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CargarValorCuota";

CREATE PROCEDURE
--  Proposito: Carga Valor Cuota, para los instrumentos de fondos mutuos. Dejando un respaldo en la tabla de valores cuotas historico.
--  Autor: - Gat-Blac 
--  Fecha Creacion: 07/10/2016
"BSCL.FondoMutuo.BD.Servicios::CargarValorCuota"(
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 						BIGINT,
    IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_UsaReglasNegocio 				NVARCHAR(1),
    IN p_SimularOperacion               NVARCHAR(1),
    IN p_ValorCuota						TABLE	(
											"IDSERIEFONDO"			NVARCHAR(40),
											"IDMONEDA"				NVARCHAR(3),
											"FECHACIERRE"			NVARCHAR(10),
											"VALORCUOTA"			DECIMAL(26,8),
											"OBSERVACION"			NVARCHAR(100),
											"MENSAJEVALIDACION" 	NVARCHAR(5000)
    									),
    OUT p_out_Resultado					TABLE	(
											"IdSerieFondo"			NVARCHAR(40),
											"IdMoneda"				NVARCHAR(3),
											"FechaVigencia"			DATE,
											"ValorCuota"			DECIMAL(26,8),
											"Observacion"			NVARCHAR(100),
											"DecimalesValorCuota"	BIGINT,
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
	DECLARE v_out_Resultado				TABLE ("IdSerieFondo" NVARCHAR(40),"IdMoneda" NVARCHAR(3),"FechaVigencia" DATE,"ValorCuota" DECIMAL(26,8),"Observacion" NVARCHAR(100),"DecimalesValorCuota"	BIGINT,"FechaCalculo" DATE,"Estado" NVARCHAR(3),"DescEstado" NVARCHAR(100),"IdEmpresa" BIGINT);
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_Resultado = SELECT '' AS "IdSerieFondo", '' AS "IdMoneda", '0001-01-01' AS "FechaVigencia", 0 AS "ValorCuota", '' AS "Observacion", 0 AS "DecimalesValorCuota", '0001-01-01' AS "FechaCalculo", '' AS "Estado", '' AS "DescEstado", 0 AS "IdEmpresa" FROM DUMMY WHERE 1=2;
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
									,"res"."IdMoneda"
									,"res"."FechaVigencia"
									,"res"."ValorCuota"
									,"res"."Observacion"
									,"res"."DecimalesValorCuota"
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
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CARGAR VALOR CUOTA','INICIO','INICIO');
			
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			SELECT COUNT(1) as "Count" into v_count_valores
			FROM :p_ValorCuota;
			
			IF (v_count_valores = 0) THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_VALORES_CUOTA' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_valores_cuota_maestro = SELECT 
											"P_VALOR_CUOTA"."IDSERIEFONDO" as "IdSerieFondo"
											,"P_VALOR_CUOTA"."IDMONEDA" as "IdMoneda"
											,"P_VALOR_CUOTA"."FECHACIERRE" as "FechaCierre"
											,CASE "P_VALOR_CUOTA"."MENSAJEVALIDACION" WHEN 'OK' THEN TO_DECIMAL(SUBSTR("P_VALOR_CUOTA"."VALORCUOTA",0,instr("P_VALOR_CUOTA"."VALORCUOTA",'.')) || SUBSTR(SUBSTR("P_VALOR_CUOTA"."VALORCUOTA",instr("P_VALOR_CUOTA"."VALORCUOTA",'.')+1,LENGTH("P_VALOR_CUOTA"."VALORCUOTA")),0,"SerieFondo"."DecimalesValorCuota"),26,8) ELSE 0 END as "ValorCuota"
											,"P_VALOR_CUOTA"."OBSERVACION" as "Observacion"
											,"SerieFondo"."DecimalesValorCuota"
											,:p_IdEmpresa AS "IdEmpresa"
											,:v_FechaSistema AS "FechaCarga"
											,CURRENT_UTCTIMESTAMP as "FechaIngresoRegistro"
											,CASE WHEN COALESCE("SerieFondo"."IdSerieFondo",'-1')!='-1' THEN 'OK' ELSE 'ERR' END AS "ResultadoSerie"
											,CASE WHEN COALESCE("ValorCuota"."FechaCierre",'0001-01-01') != "P_VALOR_CUOTA"."FECHACIERRE" THEN 'OK' ELSE 'ADV' END AS "ResultadoFecha"
											,CASE WHEN "P_VALOR_CUOTA"."VALORCUOTA" > 0 AND "P_VALOR_CUOTA"."MENSAJEVALIDACION" = 'OK' THEN 'OK' ELSE 'ERR' END AS "ResultadoValorCuota"
											,CASE WHEN "Fondo"."IdMoneda" = "P_VALOR_CUOTA"."IDMONEDA" THEN 'OK' ELSE 'ERR' END AS "ResultadoMoneda"
									   		,CASE WHEN COALESCE("P_VALOR_CUOTA"."FECHACIERRE",'0001-01-01') > :v_FechaSistema THEN 'ERR' ELSE 'OK' END AS "ResultadoFechaActual"
									   		,"P_VALOR_CUOTA"."MENSAJEVALIDACION"
									   FROM  :p_ValorCuota AS "P_VALOR_CUOTA"
									   LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("P_VALOR_CUOTA"."IDSERIEFONDO" = "SerieFondo"."IdSerieFondo" and "SerieFondo"."IdEmpresa" = :p_IdEmpresa)
									   LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "ValorCuota" ON ("P_VALOR_CUOTA"."IDSERIEFONDO" = "ValorCuota"."IdSerieFondo" and "ValorCuota"."IdEmpresa" = :p_IdEmpresa)
									   LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo");
			
			v_valores_cuota_duplicados = SELECT COUNT(1) AS "Count",
												"P_VALOR_CUOTA"."IDSERIEFONDO" as "IdSerieFondo"
										 FROM :p_ValorCuota AS "P_VALOR_CUOTA"
										 GROUP BY "IDSERIEFONDO";
			
			BEGIN
				
				v_Valor_Cuotas_Validacion = SELECT "VALOR_CUOTA"."IdSerieFondo"
												,"VALOR_CUOTA"."IdMoneda"
												,"VALOR_CUOTA"."FechaCierre"
												,"VALOR_CUOTA"."ValorCuota"
												,"VALOR_CUOTA"."Observacion"
												,"VALOR_CUOTA"."DecimalesValorCuota"
												,"VALOR_CUOTA"."IdEmpresa"
												,"VALOR_CUOTA"."FechaCarga"
												,"VALOR_CUOTA"."FechaIngresoRegistro"
												,CASE WHEN "VALOR_CUOTA"."ResultadoSerie" = 'ERR' OR "VALOR_CUOTA"."ResultadoValorCuota" = 'ERR' OR "VALOR_CUOTA"."ResultadoMoneda" = 'ERR' OR "VALOR_CUOTA_DUPLICADO"."Count" > 1 OR "VALOR_CUOTA"."ResultadoFechaActual" = 'ERR' OR "VALOR_CUOTA"."MENSAJEVALIDACION" != 'OK' THEN 'ERR'
													  WHEN "VALOR_CUOTA"."ResultadoFecha" = 'ADV' THEN 'ADV'
													  ELSE 'OK' END AS "Estado"
												,CASE  
													  WHEN "VALOR_CUOTA"."MENSAJEVALIDACION" != 'OK' THEN 'FORMATO_INVALIDO'
													  WHEN "VALOR_CUOTA_DUPLICADO"."Count" > 1 THEN 'VALOR_CUOTA_DUPLICADO'
													  WHEN "VALOR_CUOTA"."ResultadoSerie" = 'ERR' THEN 'SERIE_NO_EXISTE'
													  WHEN "VALOR_CUOTA"."ResultadoValorCuota" = 'ERR' THEN 'VALOR_CUOTA_INVALIDO'
													  WHEN "VALOR_CUOTA"."ResultadoMoneda" = 'ERR' THEN 'MONEDA_INCORRECTA'
													  WHEN "VALOR_CUOTA"."ResultadoFechaActual" = 'ERR' THEN 'FECHA_VALOR_CUOTA_INCORRECTA'
													  WHEN "VALOR_CUOTA"."ResultadoFecha" = 'ADV' THEN 'VALOR_EXISTE_FECHA'
													  ELSE 'OK' END AS "DescEstado"
											FROM :v_valores_cuota_maestro AS "VALOR_CUOTA"
											INNER JOIN :v_valores_cuota_duplicados AS "VALOR_CUOTA_DUPLICADO" ON ("VALOR_CUOTA"."IdSerieFondo" = "VALOR_CUOTA_DUPLICADO"."IdSerieFondo");
				
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", "DescEstado" as "Codigo", '' as "ValorParametro", '' AS "NombreParametro"
									   		FROM :v_Valor_Cuotas_Validacion AS "ValoresCuotaMaestro" WHERE "Estado" = 'ERR'
									   UNION ALL
									   SELECT 0 AS "IdInterno", 'REGISTRO_CON_ERRORES' as "Codigo", "P_VALOR_CUOTA"."IDSERIEFONDO" as "ValorParametro", 'IdSerieFondo' AS "NombreParametro"
									   		FROM :p_ValorCuota AS "P_VALOR_CUOTA" WHERE "MENSAJEVALIDACION" != 'OK';
				
				v_out_AdvertenciasDetalle = SELECT 0 AS "IdInterno", "DescEstado" as "Codigo", 'N' AS "RequiereAutorizacion", '' as "ValorParametro", '' AS "NombreParametro"
									   		FROM :v_Valor_Cuotas_Validacion AS "ValoresCuotaMaestro" WHERE "Estado" = 'ADV';
									   		
			END;
			
			
			v_valores_finales = SELECT 
										"VALOR_CUOTA_MAESTRO"."IdSerieFondo"
										,"VALOR_CUOTA_MAESTRO"."IdMoneda"
										,"VALOR_CUOTA_MAESTRO"."FechaCierre"
										,"VALOR_CUOTA_MAESTRO"."ValorCuota"
										,"VALOR_CUOTA_MAESTRO"."Observacion"
										,"VALOR_CUOTA_MAESTRO"."DecimalesValorCuota"
										,"VALOR_CUOTA_MAESTRO"."FechaCarga"
										,"VALOR_CUOTA_MAESTRO"."FechaIngresoRegistro"
										,"VALOR_CUOTA_MAESTRO"."IdEmpresa"
								FROM :v_Valor_Cuotas_Validacion AS "VALOR_CUOTA_MAESTRO"
								WHERE "VALOR_CUOTA_MAESTRO"."Estado" IN ('OK','ADV');
			
			v_valores_respaldo_finales = SELECT 
												"BSCL.FondoMutuo.BD.Secuencias::ValorCuotaHistorico".NEXTVAL AS "IdValorCuotaHistorico"
												,"VALOR_RESPALDO_MAESTRO"."IdSerieFondo"
												,"VALOR_RESPALDO_MAESTRO"."FechaCierre"
												,"VALOR_RESPALDO_MAESTRO"."ValorCuota"
												,"VALOR_RESPALDO_MAESTRO"."IdMoneda"
												,"VALOR_RESPALDO_MAESTRO"."Observacion"
												,"VALOR_RESPALDO_MAESTRO"."FechaCarga"
												,"VALOR_RESPALDO_MAESTRO"."FechaIngresoRegistro"
												,'S' AS "EsUltimaCargaDiaria"
												,"VALOR_RESPALDO_MAESTRO"."IdEmpresa"
										FROM :v_valores_finales AS "VALOR_RESPALDO_MAESTRO";
			
			IF (:p_SimularOperacion = 'N') THEN
				
				UPDATE VALORES_HISTORICOS
				SET "EsUltimaCargaDiaria" = 'N'
				FROM "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" AS VALORES_HISTORICOS
				INNER JOIN :v_valores_respaldo_finales AS VALORES_RESPALDO ON (VALORES_HISTORICOS."IdSerieFondo" = VALORES_RESPALDO."IdSerieFondo" 
																				AND VALORES_RESPALDO."FechaCierre" = VALORES_HISTORICOS."FechaCierre" 
																				AND VALORES_RESPALDO."IdEmpresa" = VALORES_HISTORICOS."IdEmpresa");
				
				INSERT INTO "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico"(
					"IdValorCuotaHistorico", "IdSerieFondo", "FechaCierre", "ValorCuota", "IdMoneda", "Observacion", "FechaCarga", "FechaIngresoRegistro", "EsUltimaCargaDiaria", "IdEmpresa"
				)
				SELECT "IdValorCuotaHistorico", "IdSerieFondo", "FechaCierre", "ValorCuota", "IdMoneda", "Observacion", "FechaCarga", "FechaIngresoRegistro","EsUltimaCargaDiaria", "IdEmpresa"
				FROM :v_valores_respaldo_finales;
				
				DELETE FROM "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "VALOR_CUOTA"
				WHERE EXISTS (SELECT 1 FROM :v_valores_respaldo_finales  AS "VALOR_RESPALDO_FINAL" WHERE "VALOR_RESPALDO_FINAL"."IdSerieFondo" = "VALOR_CUOTA"."IdSerieFondo");
				
				INSERT INTO "BSCL.FondoMutuo.BD::FMM.ValorCuota"
				(
					"IdSerieFondo", "FechaCierre", "ValorCuota", "IdMoneda", "Observacion", "FechaCarga", "FechaIngresoRegistro", "IdEmpresa"
				)
				SELECT "IdSerieFondo", "FechaCierre", "ValorCuota", "IdMoneda", "Observacion", "FechaCarga", "FechaIngresoRegistro", "IdEmpresa"
				FROM :v_valores_finales;
		    
		    END IF;
		    
			v_out_Resultado = SELECT
									"IdSerieFondo"
									,"IdMoneda"
									,"FechaCierre" as "FechaVigencia"
									,"ValorCuota"
									,"Observacion"
									,"DecimalesValorCuota"
									,"FechaCarga" as "FechaCalculo"
									,"Estado"
									,"DescEstado"
									,"IdEmpresa"
							  FROM :v_Valor_Cuotas_Validacion;
			
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
					,'VALOR_CUOTA_FMM' AS "IdTipoRegistro"
					,0 AS "IdRegistro"
					,'' AS "EstadoRegistro"
					,'' AS "IdTipoOperacion"
					,0 AS "Folio"
					,0 AS "Cantidad"
					,0 AS "PrecioTasa"
					,"ValorCuota" AS "Monto"
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
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP,1,'CARGAR VALOR CUOTA','FIN','FIN');
		
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
