SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.EntregarPreciosTasas";
GO
-- 
--  Proposito: Servicio que entrega Precios/Tasas de acuerdo a criterios definidos
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 06/09/2016  
-- 

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.EntregarPreciosTasas"(
	IN p_IdTrabajo BIGINT
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL" 
AS

BEGIN
  	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	
	BEGIN
		--------------------
		--Variables
		--------------------
		
		DECLARE p_IdEmpresa			BIGINT;
		DECLARE p_IdUsuario			BIGINT;
		DECLARE p_IdModulo			NVARCHAR(3);
		DECLARE p_IdPais			BIGINT;
		
		DECLARE v_Count				BIGINT:=0;
		DECLARE ARR_IDREGISTRO		BIGINT			ARRAY;
		DECLARE ARR_IDCRITERIO		BIGINT			ARRAY;
		DECLARE ARR_CODIGOFUENTE	NVARCHAR(40)	ARRAY;
		DECLARE ARR_DIASANTIGUEDAD	BIGINT			ARRAY;
		DECLARE ARR_FECHACONSULTA	DATE			ARRAY;
		DECLARE ARR_INTERPOLAR		NVARCHAR(1)		ARRAY;
		DECLARE v_i 				INTEGER;
		DECLARE v_j 				INTEGER;
		DECLARE v_IdRegistro		INTEGER;
		DECLARE v_IdCriterio		INTEGER;
		DECLARE v_CodigoFuente		NVARCHAR(40);
		DECLARE v_DiasAntiguedad	BIGINT;
		DECLARE v_FechaConsulta		DATE;
		DECLARE v_FechaConsultaAux	DATE;
		DECLARE v_DiaEsHabil		NVARCHAR(1) := 'N';
		DECLARE v_Interpolar		CHAR(1);
		DECLARE v_TasaInterpolacion	DECIMAL(26,8);
		
		DECLARE v_out_PreciosTasas TABLE(
			 "IdRegistro"						BIGINT
			,"IdTrabajo"						BIGINT
			,"IdClasificacionTipoInstrumento"	NVARCHAR(3)
			,"IdTipoInstrumento"				NVARCHAR(3)
			,"IdInstrumento"					BIGINT
			,"Nemotecnico"						NVARCHAR(40)
			,"IdMoneda"							NVARCHAR(3)
			,"IdTipoTasa"						NVARCHAR(40)
			,"Tasa"								DECIMAL(26,8)
			,"IdTipoPrecio"						NVARCHAR(40)
			,"Precio"							DECIMAL(26,8)
			,"CodigoFuente"						NVARCHAR(40)
			,"MensajeFuente"					NVARCHAR(250)
			,"FechaFuente"						SECONDDATE
			,"MensajeError"						NVARCHAR(250))
		DEFAULT
		SELECT
			 0		AS "IdRegistro"
			,0		AS "IdTrabajo"
			,NULL	AS "IdClasificacionTipoInstrumento"
			,NULL 	AS "IdTipoInstrumento"
			,0		AS "IdInstrumento"
			,NULL 	AS "Nemotecnico"
			,NULL	AS "IdMoneda"
			,NULL	AS "IdTipoTasa"
			,0		AS "Tasa"
			,NULL	AS "IdTipoPrecio"
			,0		AS "Precio"
			,NULL	AS "CodigoFuente"
			,NULL	AS "MensajeFuente"
			,NULL	AS "FechaFuente"
			,NULL	AS "MensajeError"
		FROM DUMMY WHERE 1=2;
				
		v_FechasParaCriterios = 
			SELECT 0 AS "IdRegistro", 0 AS "IdCriterioSeleccionTasas", '0001-01-01' AS "FechaCriterioDesde", '0001-01-01' AS "FechaCriterioHasta"
			FROM DUMMY WHERE 1=2;
		
		-- Se obtienen valores de parametro entrada DatosGenerales
		SELECT TOP 1 "IdEmpresa", "IdUsuario", "IdModulo", "IdPais" INTO p_IdEmpresa, p_IdUsuario, p_IdModulo, p_IdPais
		FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosGenerales"
		WHERE "IdTrabajo" = :p_IdTrabajo;
		
		
		-- Se obtienen valores de parametro entrada DatosInstrumentos
		p_Instrumentos =
			SELECT
				 "IdRegistro"
				,"IdTrabajo"
				,"TipoCartera"
				,"IdUso"
				,"IdTipoCriterioTasa"
				,"IdClasificacionTipoInstrumento"
				,"IdTipoInstrumento"
				,"IdInstrumento"
				,"Nemotecnico"
				,"IdMoneda"
				,"FechaConsulta"
				,"PlazoResidual"
			FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosInstrumentos"
			WHERE "IdTrabajo" = :p_IdTrabajo;
		
		v_RegistrosNoProcesados = 
				SELECT "IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
						"Nemotecnico", "IdMoneda", "PlazoResidual" ,"FechaConsulta"
				FROM :p_Instrumentos;
				
		--select 'v_RegistrosNoProcesados',* from :v_RegistrosNoProcesados; --BORRAR


		-- Se obtienen Criterios de acuerdo a :p_Instrumentos
		v_CriteriosSinFechas =
			SELECT
				 DISTINCT(CST."IdCriterioSeleccionTasas") AS "IdCriterioSeleccionTasas"
				,CST."Prioridad"
				,CST."TipoCartera"
				,CST."IdUso"
				,CST."IdTipoCriterioTasa"
				,CST."IdClasificacionTipoInstrumento"
				,CST."IdTipoInstrumento"
				,CST."DiasAntiguedad"
				,INS."FechaConsulta"
				,CST."CodigoFuente"
				,CST."IdTipoTasa"
				,CST."IdTipoPrecio"
				,CST."Interpolar"
				,CST."IdEmpresa"
				,INS."IdRegistro"
			FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" CST
			INNER JOIN :p_Instrumentos INS
				ON ((CST."TipoCartera"			= INS."TipoCartera" OR CST."TipoCartera"='0' OR INS."TipoCartera"='0')	AND	-- '0'='TODOS'
					(CST."IdUso"				= INS."IdUso") AND
					(CST."IdTipoCriterioTasa"	= INS."IdTipoCriterioTasa" OR CST."IdTipoCriterioTasa"='0' OR INS."IdTipoCriterioTasa"='0') AND -- '0'='TODOS'
					(CST."IdClasificacionTipoInstrumento" = INS."IdClasificacionTipoInstrumento") AND
					(CST."IdTipoInstrumento"	= INS."IdTipoInstrumento" OR CST."IdTipoInstrumento"='0' OR INS."IdTipoInstrumento"='0')) -- '0'='TODOS'
			WHERE CST."IdEmpresa"	= p_IdEmpresa
			  AND CST."Borrado"		= 'N'
			ORDER BY CST."Prioridad" ASC;
			
		--select 'v_CriteriosSinFechas', * from :v_CriteriosSinFechas; --BORRAR
		
		
		--------------------
		--Validaciones
		--------------------
		
		-- Validacion parametro TipoCartera
		SELECT COUNT(1) INTO v_Count
		FROM :p_Instrumentos INS
		INNER JOIN "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" CAR
			ON (INS."TipoCartera" = '0' OR (INS."TipoCartera" = CAR."Valor" AND CAR."NombreEntidad" = 'TIPOCARTERATASAS' AND CAR."IdEmpresa" = :p_IdEmpresa));
		IF(v_Count = 0) THEN
			INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle" ("Codigo","Descripcion","IdTrabajo")
			VALUES ('TIPOCARTERA_ERROR','NO EXISTE TIPO CARTERA DE UNO O MAS PARAMETROS DE ENTRADA INSTRUMENTOS',:p_IdTrabajo);
		END IF;
		
		-- Validacion parametro IdUso
		SELECT COUNT(1) INTO v_Count
		FROM :p_Instrumentos INS
		INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarUso" USO
			ON (INS."IdUso" = USO."IdUso" AND USO."IdEmpresa" = :p_IdEmpresa AND USO."IdEstado" = 'ACT');
		IF(v_Count = 0) THEN
			INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle" ("Codigo","Descripcion","IdTrabajo")
			VALUES ('USO_ERROR','NO EXISTE USO DE UNO O MAS PARAMETROS DE ENTRADA INSTRUMENTOS',:p_IdTrabajo);
		END IF;
		
		-- Validacion parametro IdTipoCriterioTasa
		SELECT COUNT(1) INTO v_Count
		FROM :p_Instrumentos INS
		INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoCriterioTasa" TCR
			ON (INS."IdTipoCriterioTasa" = '0' OR (INS."IdTipoCriterioTasa" = TCR."IdTipoCriterioTasa" AND TCR."IdEmpresa" = :p_IdEmpresa));
		IF(v_Count = 0) THEN
			INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle" ("Codigo","Descripcion","IdTrabajo")
			VALUES ('TIPOCRITERIOTASA_ERROR','NO EXISTE TIPO CRITERIO TASA DE UNO O MAS PARAMETROS DE ENTRADA INSTRUMENTOS',:p_IdTrabajo);
		END IF;
		
		-- Validacion parametro IdTipoInstrumento
		SELECT COUNT(1) INTO v_Count
		FROM :p_Instrumentos INS
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIN
			ON (INS."IdTipoInstrumento" = '0' OR (INS."IdTipoInstrumento" = TIN."IdTipoInstrumento" AND TIN."IdEstado" = 'VIG'));
		IF(v_Count = 0) THEN
			INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle" ("Codigo","Descripcion","IdTrabajo")
			VALUES ('TIPOINSTRUMENTO_ERROR','NO EXISTE TIPO INSTRUMENTO DE UNO O MAS PARAMETROS DE ENTRADA INSTRUMENTOS',:p_IdTrabajo);
		END IF;
		
		
		-- Validacion tabla parametro BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosGenerales tiene registros
		IF (IS_EMPTY("BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosGenerales")) THEN
			INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle" ("Codigo","Descripcion","IdTrabajo")
			VALUES ('DATOSGENERALES_ERROR','VALORES ENTRADA DATOS GENERALES SIN REGISTRO',:p_IdTrabajo);
		END IF;
		
		-- Validacion tabla parametro BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosInstrumentos tiene registros
		IF (IS_EMPTY("BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosInstrumentos")) THEN
			INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle" ("Codigo","Descripcion","IdTrabajo")
			VALUES ('INSTRUMENTOS_ERROR','VALORES ENTRADA INSTRUMENTOS SIN REGISTROS',:p_IdTrabajo);
		END IF;
		
		-- Validacion si existen Criterios
		SELECT COUNT(1) INTO v_Count FROM :v_CriteriosSinFechas;
		IF(v_Count = 0) THEN
			INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle" ("Codigo","Descripcion","IdTrabajo")
			VALUES ('CRITERIOS_ERROR','NO EXISTEN CRITERIOS PARA VALORES ENTRADA',:p_IdTrabajo);
		END IF;
		
		--SELECT * FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle";--BORRAR
		-- Se verifica resultado de las validaciones previas
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle"
		WHERE "IdTrabajo"=:p_IdTrabajo;
		IF(v_Count > 0) THEN
			v_out_PreciosTasas =
				SELECT
					"IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico",
					"IdMoneda", NULL AS "IdTipoTasa", NULL AS "Tasa", NULL AS "IdTipoPrecio", NULL AS "Precio", NULL AS "CodigoFuente",
					NULL AS "MensajeFuente", NULL AS "FechaFuente",
					'ERROR::Revise tabla: BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle' AS "MensajeError"
				FROM :p_Instrumentos;
		
		ELSE
-------------------- LOGICA DE CRITERIOS Y ORIGEN DE PRECIOS/TASAS --------------------
			ARR_IDREGISTRO		:= ARRAY_AGG(:v_CriteriosSinFechas."IdRegistro");
			ARR_IDCRITERIO		:= ARRAY_AGG(:v_CriteriosSinFechas."IdCriterioSeleccionTasas");
			ARR_CODIGOFUENTE	:= ARRAY_AGG(:v_CriteriosSinFechas."CodigoFuente");
			ARR_DIASANTIGUEDAD	:= ARRAY_AGG(:v_CriteriosSinFechas."DiasAntiguedad");
			ARR_INTERPOLAR		:= ARRAY_AGG(:v_CriteriosSinFechas."Interpolar");
			ARR_FECHACONSULTA	:= ARRAY_AGG(:v_CriteriosSinFechas."FechaConsulta");
			
			-- Se generan fechas en las que aplican los Criterios obtenidos. Considera solo días hábiles
			FOR v_i IN 1..CARDINALITY(:ARR_IDCRITERIO) DO
				v_IdRegistro	:= :ARR_IDREGISTRO[:v_i];
				v_IdCriterio	:= :ARR_IDCRITERIO[:v_i];
				v_DiasAntiguedad:= :ARR_DIASANTIGUEDAD[:v_i];
				v_FechaConsulta	:= :ARR_FECHACONSULTA[:v_i];
				
				v_j := 0;
				v_FechaConsultaAux := v_FechaConsulta;
				
				WHILE v_j < v_DiasAntiguedad DO
					CALL "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(p_Fecha=>:v_FechaConsultaAux, p_IdPais=>:p_IdPais, p_out_Habil=>:v_DiaEsHabil);
					IF :v_DiaEsHabil = 'S' THEN
						v_j := v_j + 1;
					END IF;
					v_FechaConsultaAux	:= ADD_DAYS(:v_FechaConsultaAux, -1);
				END WHILE;
				
				v_FechasParaCriterios =
					SELECT "IdRegistro", "IdCriterioSeleccionTasas", "FechaCriterioDesde", "FechaCriterioHasta"
					FROM :v_FechasParaCriterios
					UNION ALL
					SELECT
						v_IdRegistro AS "IdRegistro", v_IdCriterio AS "IdCriterioSeleccionTasas",
						v_FechaConsultaAux AS "FechaCriterioDesde", v_FechaConsulta AS "FechaCriterioHasta"
					FROM DUMMY;
			END FOR;
			
			--SELECT 'v_FechasParaCriterios', * FROM :v_FechasParaCriterios; --BORRAR
			
			
			v_Criterios =
				SELECT
					 CST."IdCriterioSeleccionTasas"
					,CST."Prioridad"
					,CST."TipoCartera"
					,CST."IdUso"
					,CST."IdTipoCriterioTasa"
					,CST."IdClasificacionTipoInstrumento"
					,CST."IdTipoInstrumento"
					,CST."DiasAntiguedad"
					,CST."FechaConsulta"
					,CST."CodigoFuente"
					,CST."IdTipoTasa"
					,CST."IdTipoPrecio"
					,CST."Interpolar"
					,CST."IdEmpresa"
					,CRF."FechaCriterioDesde"
					,CRF."FechaCriterioHasta"
					,CST."IdRegistro"
				FROM :v_CriteriosSinFechas CST
				INNER JOIN :v_FechasParaCriterios CRF
					ON (CST."IdCriterioSeleccionTasas" = CRF."IdCriterioSeleccionTasas" AND
						CST."FechaConsulta" = CRF."FechaCriterioHasta" AND
						CST."IdRegistro" = CRF."IdRegistro");
			
			--SELECT 'v_Criterios', * FROM :v_Criterios; --BORRAR
			
							
-------------------- OBTENCION PRECIOS/TASAS PUBLICADAS --------------------				
			FOR v_i IN 1..CARDINALITY(:ARR_IDCRITERIO) DO
				v_IdRegistro	:= :ARR_IDREGISTRO[:v_i];
				v_IdCriterio	:= :ARR_IDCRITERIO[:v_i];
				v_CodigoFuente	:= :ARR_CODIGOFUENTE[:v_i];
				v_Interpolar	:= :ARR_INTERPOLAR[:v_i];
				v_FechaConsulta	:= :ARR_FECHACONSULTA[:v_i];
				
				IF (v_CodigoFuente <> 'INSTRUMENTO' AND v_CodigoFuente <> 'TIPOINSTRUMENTO' AND v_CodigoFuente <> 'BLOOMBERG' AND v_CodigoFuente <> 'NAV'  AND v_CodigoFuente <> 'PRECIOSBCS' AND v_CodigoFuente <> 'ULTIMATRANSACCIONBCS' AND v_CodigoFuente <> 'PRECIOSAGF' AND v_CodigoFuente <> 'PRECIOMEDIO' AND v_CodigoFuente <> 'VPG' AND v_CodigoFuente <> 'VECTORPRECIOSRIESGO' AND v_CodigoFuente <> 'VECTORPRECIOSMERCADOBVL' AND v_CodigoFuente <> 'VECTORPRECIOSAPERTURABVL' AND v_CodigoFuente <> 'VECTORPRECIOSCIERREBVL' AND v_CodigoFuente <> 'VECTORPRECIOSREPORTEBVL' AND v_CodigoFuente <> 'VECTORPRECIOSCORREDORA') THEN
					-- Se obtienen registros de tabla PrecioTasa de acuerdo a Instrumentos no procesados, Criterio obtenido y Fechas aplica
					v_RegistrosPreciosTasasPublicadas = 
						SELECT
							 RNP."IdRegistro"
							,RNP."IdTrabajo"
							,RNP."IdClasificacionTipoInstrumento"
							,RNP."IdTipoInstrumento"
							,RNP."IdInstrumento"
							,RNP."Nemotecnico"
							,RNP."IdMoneda"
							,RNP."PlazoResidual"
							,PTP."IdPrecioTasa"
							,PTP."IdFuente"
							,PTP."IdTipoTasa"
							,PTP."Tasa"
							,PTP."TasaEstimada"
							,PTP."IdTipoPrecio"
							,PTP."Precio"
							,PTP."FechaSolicitud"
							,PTP."Plazo"
							,RNP."FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" PTP
							ON (RNP."IdClasificacionTipoInstrumento" = PTP."IdClasificacionTipoInstrumento" AND 
								((PTP."IdTipoTasa" IN ('CURVA','PUNTO') /*AND PTP."Nemotecnico" = ''*/) OR
								 (RNP."IdTipoInstrumento" = PTP."IdTipoInstrumento" AND RNP."Nemotecnico" = PTP."Nemotecnico")) AND
								--RNP."PlazoResidual"	<= PTP."Plazo" AND
								RNP."IdMoneda" = PTP."IdMoneda"
								)
						INNER JOIN :v_Criterios CRI
							ON ( RNP."IdClasificacionTipoInstrumento"=CRI."IdClasificacionTipoInstrumento" AND 
								(RNP."IdTipoInstrumento"			= CRI."IdTipoInstrumento" OR CRI."IdTipoInstrumento"='0') AND 
								 PTP."CodigoFuente"					= CRI."CodigoFuente" AND 
								 (PTP."IdTipoTasa" = CRI."IdTipoTasa" OR PTP."IdTipoPrecio" = CRI."IdTipoPrecio") AND 
								 PTP."IdEmpresa"					= CRI."IdEmpresa")
						WHERE PTP."IdEstado" = 'PUB'
						  AND PTP."Borrado" = 'N'
						  AND (TO_DATE(TO_VARCHAR(PTP."FechaSolicitud",'YYYY-MM-DD'),'YYYY-MM-DD') BETWEEN CRI."FechaCriterioDesde" AND CRI."FechaCriterioHasta")
						  AND (RNP."FechaConsulta" BETWEEN CRI."FechaCriterioDesde" AND CRI."FechaCriterioHasta")
						  AND CRI."FechaCriterioHasta" = v_FechaConsulta
						  AND CRI."IdCriterioSeleccionTasas" = v_IdCriterio
						  AND CRI."IdRegistro" = v_IdRegistro
						ORDER BY PTP."Plazo" ASC;
					
					--SELECT 'v_RegistrosPreciosTasasPublicadas', * FROM :v_RegistrosPreciosTasasPublicadas PTP;--BORRAR
					
					IF (NOT IS_EMPTY(:v_RegistrosPreciosTasasPublicadas)) THEN
						-- De los Instrumentos de la variable anterior, se obtienen los más recientes de la tabla PrecioTasa (MAX("FechaSolicitud"))
						v_UltimosPreciosTasasPublicadas = 
							SELECT
								 PTP."IdRegistro"
								,PTP."IdTrabajo"
								,PTP."IdClasificacionTipoInstrumento"
								,PTP."IdTipoInstrumento"
								,PTP."IdInstrumento"
								,PTP."Nemotecnico"
								,PTP."IdMoneda"
								,PTP."PlazoResidual"
								,PTP."IdPrecioTasa"
								,PTP."IdFuente"
								,PTP."IdTipoTasa"
								,PTP."Tasa"
								,PTP."TasaEstimada"
								,PTP."IdTipoPrecio"
								,PTP."Precio"
								,PTP."FechaSolicitud"
								,PTP."Plazo"
							FROM :v_RegistrosPreciosTasasPublicadas PTP
							INNER JOIN (SELECT "Nemotecnico", MAX("FechaSolicitud") AS "FechaSolicitud"
										FROM :v_RegistrosPreciosTasasPublicadas
										GROUP BY "Nemotecnico") REPTP
								ON (PTP."Nemotecnico" = REPTP."Nemotecnico" AND PTP."FechaSolicitud" = REPTP."FechaSolicitud");
						
						--select 'v_UltimosPreciosTasasPublicadas', * from :v_UltimosPreciosTasasPublicadas;--BORRAR
						
						
						--Cuando no se realiza Interpolacion, se obtiene el registro con menor plazo (ya estan ordenados)
						v_PreciosTasasPublicadas = 
							SELECT
								 UPT."IdRegistro"
								,UPT."IdTrabajo"
								,UPT."IdClasificacionTipoInstrumento"
								,UPT."IdTipoInstrumento"
								,UPT."IdInstrumento"
								,UPT."Nemotecnico"
								,UPT."IdMoneda"
								,UPT."PlazoResidual"
								,UPT."IdPrecioTasa"
								,UPT."IdFuente"
								,UPT."IdTipoTasa"
								,UPT."Tasa"
								,UPT."TasaEstimada"
								,UPT."IdTipoPrecio"
								,UPT."Precio"
								,UPT."FechaSolicitud"
							FROM :v_UltimosPreciosTasasPublicadas UPT
							INNER JOIN (SELECT "Nemotecnico", MIN("Plazo") AS "Plazo"
										FROM :v_UltimosPreciosTasasPublicadas
										WHERE "PlazoResidual" <= "Plazo"
										GROUP BY "Nemotecnico") PLPTP
								ON (UPT."Nemotecnico" = PLPTP."Nemotecnico" AND UPT."Plazo" = PLPTP."Plazo");
						
						--Se realiza INTERPOLACION si corresponde
						IF :v_Interpolar = 'S' THEN
							v_PreciosTasasMaxMenores =
								SELECT "IdRegistro", "Nemotecnico", "IdMoneda", "PlazoResidual", MAX("Plazo") AS "Plazo"
								FROM :v_UltimosPreciosTasasPublicadas 
								WHERE "PlazoResidual" >= "Plazo"
								GROUP BY "IdRegistro", "Nemotecnico", "IdMoneda", "PlazoResidual";
							
							--select 'v_PreciosTasasMaxMenores', * from :v_PreciosTasasMaxMenores;--BORRAR
							
							v_PreciosTasasPlazoMenor =
								SELECT
									 UPT."IdRegistro"
									,UPT."IdTrabajo"
									,UPT."IdClasificacionTipoInstrumento"
									,UPT."IdTipoInstrumento"
									,UPT."IdInstrumento"
									,UPT."Nemotecnico"
									,UPT."IdMoneda"
									,UPT."PlazoResidual"
									,UPT."IdPrecioTasa"
									,UPT."IdFuente"
									,UPT."IdTipoTasa"
									,UPT."Tasa"
									,UPT."TasaEstimada"
									,UPT."IdTipoPrecio"
									,UPT."Precio"
									,UPT."FechaSolicitud"
									,PTMEN."Plazo"
								FROM :v_UltimosPreciosTasasPublicadas UPT
								INNER JOIN :v_PreciosTasasMaxMenores PTMEN
									--ON (UPT."Nemotecnico" = PLPTP."Nemotecnico" AND UPT."Plazo" = PLPTP."Plazo")
									ON (UPT."IdRegistro" = PTMEN."IdRegistro" AND 
										UPT."Nemotecnico" = PTMEN."Nemotecnico" AND UPT."Plazo" = PTMEN."Plazo")
								;
							
							--select 'v_PreciosTasasPlazoMenor', * from :v_PreciosTasasPlazoMenor;--BORRAR
							
							
							v_PreciosTasasMinMayores =
								SELECT "IdRegistro", "Nemotecnico", "IdMoneda", "PlazoResidual", MIN("Plazo") AS "Plazo"
								FROM :v_UltimosPreciosTasasPublicadas 
								WHERE "PlazoResidual" <= "Plazo"
								GROUP BY "IdRegistro", "Nemotecnico", "IdMoneda", "PlazoResidual";
							
							--select 'v_PreciosTasasMinMayores', * from :v_PreciosTasasMinMayores;--BORRAR
							
							v_PreciosTasasPlazoMayor =
								SELECT
									 UPT."IdRegistro"
									,UPT."IdTrabajo"
									,UPT."IdClasificacionTipoInstrumento"
									,UPT."IdTipoInstrumento"
									,UPT."IdInstrumento"
									,UPT."Nemotecnico"
									,UPT."IdMoneda"
									,UPT."PlazoResidual"
									,UPT."IdPrecioTasa"
									,UPT."IdFuente"
									,UPT."IdTipoTasa"
									,UPT."Tasa"
									,UPT."TasaEstimada"
									,UPT."IdTipoPrecio"
									,UPT."Precio"
									,UPT."FechaSolicitud"
									,PTMAY."Plazo"
								FROM :v_UltimosPreciosTasasPublicadas UPT
								INNER JOIN :v_PreciosTasasMinMayores PTMAY
									--ON (UPT."Nemotecnico" = PLPTP."Nemotecnico" AND UPT."Plazo" = PLPTP."Plazo")
									ON (UPT."IdRegistro" = PTMAY."IdRegistro" AND 
										UPT."Nemotecnico" = PTMAY."Nemotecnico" AND UPT."Plazo" = PTMAY."Plazo")
								;
							
							--select 'v_PreciosTasasPlazoMayor', * from :v_PreciosTasasPlazoMayor;--BORRAR
							
							
							--Se calcula INTERPOLACION
							v_PreciosTasasRangos =
								SELECT
									 PLMAY."IdRegistro"
									,PLMAY."IdTrabajo"
									,PLMAY."IdClasificacionTipoInstrumento"
									,PLMAY."IdTipoInstrumento"
									,PLMAY."IdInstrumento"
									,PLMAY."Nemotecnico"
									,PLMAY."IdMoneda"
									,PLMAY."PlazoResidual"
									,PLMAY."IdPrecioTasa"
									,PLMAY."IdFuente"
									,PLMAY."IdTipoTasa"
									,PLMAY."TasaEstimada"
									,PLMAY."IdTipoPrecio"
									,PLMAY."Precio"
									,PLMAY."FechaSolicitud"
									,PLMEN."Tasa"	AS "TasMen"
									,PLMAY."Tasa"	AS "TasMay"
									,PLMEN."Plazo"	AS "PlaMen"
									,PLMAY."Plazo"	AS "PlaMay"
								FROM :v_PreciosTasasPlazoMayor PLMAY
								LEFT JOIN :v_PreciosTasasPlazoMenor PLMEN
									ON (PLMAY."IdRegistro"	= PLMEN."IdRegistro" AND 
										PLMAY."Nemotecnico"	= PLMEN."Nemotecnico" AND
										PLMAY."IdMoneda"	= PLMEN."IdMoneda");
							
							v_CalculoInterpolacion =
								SELECT "IdRegistro", "Nemotecnico", "IdMoneda"
									,CASE
										WHEN ("PlaMay"-"PlaMen") <> 0 THEN
											COALESCE(((("PlaMay"-"PlazoResidual")*(("TasMay"-"TasMen")/("PlaMay"-"PlaMen")))-"TasMay")*(-1),0)
										ELSE COALESCE("TasMen","TasMay",0) END AS "TasaInterpolacion"
								FROM :v_PreciosTasasRangos;
							
							--select 'v_PreciosTasasRangos', * from :v_PreciosTasasRangos;--BORRAR
							--select 'v_CalculoInterpolacion', * from :v_CalculoInterpolacion;--BORRAR
							
							
							v_PreciosTasasPublicadas =
								SELECT 
									 PTR."IdRegistro"
									,PTR."IdTrabajo"
									,PTR."IdClasificacionTipoInstrumento"
									,PTR."IdTipoInstrumento"
									,PTR."IdInstrumento"
									,PTR."Nemotecnico"
									,PTR."IdMoneda"
									,PTR."PlazoResidual"
									,PTR."IdPrecioTasa"
									,PTR."IdFuente"
									,PTR."IdTipoTasa"
									,CIP."TasaInterpolacion" AS "Tasa" --PTR."Tasa"
									,PTR."TasaEstimada"
									,PTR."IdTipoPrecio"
									,PTR."Precio"
									,PTR."FechaSolicitud"
									--,PTR."Plazo"
								FROM :v_PreciosTasasRangos PTR
								INNER JOIN :v_CalculoInterpolacion CIP
									ON (PTR."IdRegistro" = CIP."IdRegistro" AND 
										PTR."Nemotecnico" = CIP."Nemotecnico" AND
										PTR."IdMoneda" = CIP."IdMoneda");
								
						END IF;
						
						--SELECT 'v_PreciosTasasPublicadas', * FROM :v_PreciosTasasPublicadas;--BORRAR
						
						v_out_PreciosTasas =
							SELECT
								"IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
								"Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
								"FechaFuente", "MensajeError"
							FROM :v_out_PreciosTasas
							UNION ALL
							SELECT
								PTP."IdRegistro", PTP."IdTrabajo", PTP."IdClasificacionTipoInstrumento", PTP."IdTipoInstrumento",
								PTP."IdInstrumento", PTP."Nemotecnico", PTP."IdMoneda", PTP."IdTipoTasa", PTP."Tasa", PTP."IdTipoPrecio",
								PTP."Precio", v_CodigoFuente AS "CodigoFuente", 'Fuente: PreciosTasasPublicadas' AS "MensajeFuente",
								PTP."FechaSolicitud" AS "FechaFuente", NULL AS "MensajeError"
							FROM :v_PreciosTasasPublicadas PTP;
						
						--select 'v_out_PreciosTasas', * from :v_out_PreciosTasas;--BORRAR
					END IF;
					
					-- Se obtienen Instrumentos que NO estan en tabla PrecioTasa.
					v_RegistrosNoProcesados = 
						SELECT
							 RNP."IdRegistro"
							,RNP."IdTrabajo"
							,RNP."IdClasificacionTipoInstrumento"
							,RNP."IdTipoInstrumento"
							,RNP."IdInstrumento"
							,RNP."Nemotecnico"
							,RNP."IdMoneda"
							,RNP."PlazoResidual"
							,RNP."FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						WHERE NOT EXISTS (SELECT 1 FROM :v_RegistrosPreciosTasasPublicadas PTP
										  WHERE PTP."FechaConsulta" = RNP."FechaConsulta"
										    AND PTP."Nemotecnico" = RNP."Nemotecnico"
										    AND PTP."IdRegistro" = RNP."IdRegistro");
										    
-------------------- OBTENCION PRECIOS VECTOR PRECIO GARANTIAS --------------------									    
				ELSEIF (v_CodigoFuente = 'VPG') THEN
								
				v_PreciosVectorValorizado = 	
						WITH precios as(		
							SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento",prec."FechaVector" desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento",prec."FechaVector" AS "Fecha"
							,prec."ValorPrecioAlgorithmics100" AS "Precio"
							FROM :v_RegistrosNoProcesados RNP
							INNER JOIN "BSCL.Plataforma.BD::INS.VectorPrecios" prec on prec."IdInstrumento" = RNP."IdInstrumento" and prec."FechaVector" <= "FechaConsulta"
						 )
					 	SELECT RNP."IdInstrumento","Precio",RNP."IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Nemotecnico","IdMoneda","Fecha","FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						LEFT JOIN precios pre on RNP."IdInstrumento" = pre."IdInstrumento"
					 	WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "Precio" > 0
					 	ORDER BY "IdInstrumento","FechaConsulta","Fecha" desc;
					 	
					 	
					--  
					
					v_out_PreciosTasas =
						SELECT
							"IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
							"Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
							"FechaFuente", "MensajeError"
						FROM :v_out_PreciosTasas
						UNION ALL
						SELECT
							PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
							PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."Precio", v_CodigoFuente AS "CodigoFuente",
							'Fuente: VPG' AS "MensajeFuente", PTI."Fecha" AS "FechaFuente", NULL AS "MensajeError"
						FROM :v_PreciosVectorValorizado PTI
						INNER JOIN :v_CriteriosSinFechas CRI
							ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
					
					
					-- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
					v_RegistrosNoProcesados = 
						SELECT
							 RNP."IdRegistro"
							,RNP."IdTrabajo"
							,RNP."IdClasificacionTipoInstrumento"
							,RNP."IdTipoInstrumento"
							,RNP."IdInstrumento"
							,RNP."Nemotecnico"
							,RNP."IdMoneda"
							,RNP."PlazoResidual"
							,RNP."FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						WHERE RNP."IdInstrumento" NOT IN (SELECT INS."IdInstrumento" FROM :v_PreciosVectorValorizado INS);
						
-------------------- OBTENCION PRECIOS BLOOMBERG --------------------					
				ELSEIF (v_CodigoFuente = 'BLOOMBERG') THEN

                    v_PreciosBloomberg = 
                        WITH precios as(        
                            SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento", TO_DATE(EVP."Fecha", 'YYYY-MM-DD') desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento","Fecha",
                            EVP."ValorRegistro" AS "PrecioCierre"
                            FROM :v_RegistrosNoProcesados RNP
                            INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = RNP."IdInstrumento"
                            INNER JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio" EVP on EVP."Entidad" = 'BLOOMBERG' AND EVP."TipoRegistro" = 'BLOOMBERG PRECIO'
                                AND INS."Nemotecnico" = EVP."Nemotecnico" and TO_DATE(EVP."Fecha", 'YYYY-MM-DD') <= RNP."FechaConsulta"
							INNER JOIN :v_Criterios CRI
							ON ( RNP."IdClasificacionTipoInstrumento"=CRI."IdClasificacionTipoInstrumento" AND 
								(RNP."IdTipoInstrumento"			= CRI."IdTipoInstrumento" OR CRI."IdTipoInstrumento"='0') AND
								(EVP."Entidad" = CRI."CodigoFuente"))
							WHERE  TO_DATE(TO_VARCHAR(EVP."Fecha",'YYYY-MM-DD'),'YYYY-MM-DD') BETWEEN ADD_DAYS(TO_DATE(RNP."FechaConsulta"), -CRI."DiasAntiguedad") AND CRI."FechaCriterioHasta"
                         )
                        SELECT RNP."IdInstrumento","PrecioCierre",RNP."IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Nemotecnico","IdMoneda","Fecha","FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        LEFT JOIN precios pre on RNP."IdInstrumento" = pre."IdInstrumento"
                        WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "PrecioCierre" > 0
                        ORDER BY "IdInstrumento", "FechaConsulta","Fecha" desc;
                    
                    v_out_PreciosTasas =
                        SELECT
                            "IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
                            "Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
                            "FechaFuente", "MensajeError"
                        FROM :v_out_PreciosTasas
                        UNION ALL
                        SELECT
                            PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
                            PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."PrecioCierre" AS "Precio", v_CodigoFuente AS "CodigoFuente",
                            'Fuente: Bloomberg' AS "MensajeFuente", PTI."Fecha" AS "FechaFuente", NULL AS "MensajeError"
                        FROM :v_PreciosBloomberg PTI
                        INNER JOIN :v_CriteriosSinFechas CRI
                            ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
                    
                    -- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
                    v_RegistrosNoProcesados = 
                        SELECT
                             RNP."IdRegistro"
                            ,RNP."IdTrabajo"
                            ,RNP."IdClasificacionTipoInstrumento"
                            ,RNP."IdTipoInstrumento"
                            ,RNP."IdInstrumento"
                            ,RNP."Nemotecnico"
                            ,RNP."IdMoneda"
                            ,RNP."PlazoResidual"
                            ,RNP."FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        WHERE RNP."IdInstrumento" NOT IN (SELECT INS."IdInstrumento" FROM :v_PreciosBloomberg INS);

-------------------- OBTENCION PRECIOS NAV --------------------					
				ELSEIF (v_CodigoFuente = 'NAV') THEN

                    v_PreciosNAV = 
                        WITH precios as(        
                            SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento", TO_DATE(EVP."Fecha", 'YYYY-MM-DD') desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento","Fecha",
                            EVP."ValorRegistro" AS "PrecioCierre"
                            FROM :v_RegistrosNoProcesados RNP
                            INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = RNP."IdInstrumento"
                            INNER JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio" EVP on EVP."Entidad" = 'NAV' AND EVP."TipoRegistro" = 'NAV PRECIO'
                                AND INS."Nemotecnico" = EVP."Nemotecnico" and TO_DATE(EVP."Fecha", 'YYYY-MM-DD') <= RNP."FechaConsulta"
							INNER JOIN :v_Criterios CRI
							ON ( RNP."IdClasificacionTipoInstrumento"=CRI."IdClasificacionTipoInstrumento" AND 
								(RNP."IdTipoInstrumento"			= CRI."IdTipoInstrumento" OR CRI."IdTipoInstrumento"='0') AND
								(EVP."Entidad" = CRI."CodigoFuente"))
							WHERE  TO_DATE(TO_VARCHAR(EVP."Fecha",'YYYY-MM-DD'),'YYYY-MM-DD') BETWEEN ADD_DAYS(TO_DATE(RNP."FechaConsulta"), -CRI."DiasAntiguedad") AND CRI."FechaCriterioHasta"
                         )
                        SELECT RNP."IdInstrumento","PrecioCierre",RNP."IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Nemotecnico","IdMoneda","Fecha","FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        LEFT JOIN precios pre on RNP."IdInstrumento" = pre."IdInstrumento"
                        WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "PrecioCierre" > 0
                        ORDER BY "IdInstrumento", "FechaConsulta","Fecha" desc;
                    
                    v_out_PreciosTasas =
                        SELECT
                            "IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
                            "Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
                            "FechaFuente", "MensajeError"
                        FROM :v_out_PreciosTasas
                        UNION ALL
                        SELECT
                            PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
                            PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."PrecioCierre" AS "Precio", v_CodigoFuente AS "CodigoFuente",
                            'Fuente: NAV' AS "MensajeFuente", PTI."Fecha" AS "FechaFuente", NULL AS "MensajeError"
                        FROM :v_PreciosNAV PTI
                        INNER JOIN :v_CriteriosSinFechas CRI
                            ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
                    
                    -- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
                    v_RegistrosNoProcesados = 
                        SELECT
                             RNP."IdRegistro"
                            ,RNP."IdTrabajo"
                            ,RNP."IdClasificacionTipoInstrumento"
                            ,RNP."IdTipoInstrumento"
                            ,RNP."IdInstrumento"
                            ,RNP."Nemotecnico"
                            ,RNP."IdMoneda"
                            ,RNP."PlazoResidual"
                            ,RNP."FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        WHERE RNP."IdInstrumento" NOT IN (SELECT INS."IdInstrumento" FROM :v_PreciosNAV INS);					
				
-------------------- OBTENCION PRECIOS MERCADO BCS --------------------
				ELSEIF (v_CodigoFuente = 'PRECIOSBCS') THEN
					-- Se obtienen Instrumentos que no han sido procesados y estan en bolsaprecioinstrumento
					
					v_PreciosMercadoBcs = 
						WITH precios as(		
							SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento",prec."Fecha" desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento",prec."Fecha",
							prec."IdBolsa",prec."PrecioCierre"
							FROM :v_RegistrosNoProcesados RNP
							INNER JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec on prec."IdInstrumento" = RNP."IdInstrumento" and prec."Fecha" <= "FechaConsulta"
						 )
					 	SELECT RNP."IdInstrumento","PrecioCierre",RNP."IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Nemotecnico","IdMoneda","Fecha","FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						LEFT JOIN precios pre on RNP."IdInstrumento" = pre."IdInstrumento"
					 	WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "PrecioCierre" > 0
					 	ORDER BY "IdInstrumento","IdBolsa","FechaConsulta","Fecha" desc;
					
					
					v_out_PreciosTasas =
						SELECT
							"IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
							"Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
							"FechaFuente", "MensajeError"
						FROM :v_out_PreciosTasas
						UNION ALL
						SELECT
							PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
							PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."PrecioCierre" AS "Precio", v_CodigoFuente AS "CodigoFuente",
							'Fuente: PreciosMercadoBCS' AS "MensajeFuente", PTI."Fecha" AS "FechaFuente", NULL AS "MensajeError"
						FROM :v_PreciosMercadoBcs PTI
						INNER JOIN :v_CriteriosSinFechas CRI
							ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
					
					
					-- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
					v_RegistrosNoProcesados = 
						SELECT
							 RNP."IdRegistro"
							,RNP."IdTrabajo"
							,RNP."IdClasificacionTipoInstrumento"
							,RNP."IdTipoInstrumento"
							,RNP."IdInstrumento"
							,RNP."Nemotecnico"
							,RNP."IdMoneda"
							,RNP."PlazoResidual"
							,RNP."FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						WHERE RNP."IdInstrumento" NOT IN (SELECT INS."IdInstrumento" FROM :v_PreciosMercadoBcs INS);
						
				
-------------------- OBTENCION PRECIOS MEDIO DEL DIA BCS --------------------
				ELSEIF (v_CodigoFuente = 'PRECIOMEDIO') THEN
					-- Se obtienen Instrumentos que no han sido procesados y estan en bolsaprecioinstrumento
					
					--select 'v_RegistrosNoProcesados',* from :v_RegistrosNoProcesados; --BORRAR

					v_PrecioMedioBcs = 
						WITH precios as(		
							SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento",prec."Fecha" desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento",prec."Fecha",
							prec."IdBolsa",prec."PrecioMedio"
							FROM :v_RegistrosNoProcesados RNP
							INNER JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec on prec."IdBolsa" = 'BCS' and prec."IdInstrumento" = RNP."IdInstrumento" and prec."Fecha" <= "FechaConsulta" and prec."PrecioMedio" > 0
						 )
					 	SELECT RNP."IdInstrumento","PrecioMedio",RNP."IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Nemotecnico","IdMoneda","Fecha","FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						LEFT JOIN precios pre on RNP."IdInstrumento" = pre."IdInstrumento"
					 	WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "PrecioMedio" > 0
					 	ORDER BY "IdInstrumento","IdBolsa","FechaConsulta","Fecha" desc;
					 	
					v_out_PreciosTasas =
						SELECT
							"IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
							"Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
							"FechaFuente", "MensajeError"
						FROM :v_out_PreciosTasas
						UNION ALL
						SELECT
							PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
							PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."PrecioMedio" AS "Precio", v_CodigoFuente AS "CodigoFuente",
							'Fuente: PrecioMedioBCS' AS "MensajeFuente", PTI."Fecha" AS "FechaFuente", NULL AS "MensajeError"
						FROM :v_PrecioMedioBcs PTI
						INNER JOIN :v_CriteriosSinFechas CRI
							ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
					
					
					-- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
					v_RegistrosNoProcesados = 
						SELECT
							 RNP."IdRegistro"
							,RNP."IdTrabajo"
							,RNP."IdClasificacionTipoInstrumento"
							,RNP."IdTipoInstrumento"
							,RNP."IdInstrumento"
							,RNP."Nemotecnico"
							,RNP."IdMoneda"
							,RNP."PlazoResidual"
							,RNP."FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						WHERE RNP."IdInstrumento" NOT IN (SELECT INS."IdInstrumento" FROM :v_PrecioMedioBcs INS);
						
					
-------------------- OBTENCION PRECIOS/TASAS INSTRUMENTO --------------------
				ELSEIF (v_CodigoFuente = 'INSTRUMENTO') THEN
					-- Se obtienen Instrumentos que no han sido procesados y estan en ConsultarInstrumento
					v_PreciosTasasInstrumento = 
						SELECT
							 RNP."IdRegistro"
							,RNP."IdTrabajo"
							,RNP."IdClasificacionTipoInstrumento"
							,RNP."IdTipoInstrumento"
							,RNP."IdInstrumento"
							,RNP."Nemotecnico"
							,RNP."IdMoneda"
							,INS."Tasa"
							,INS."Precio"
							,INS."FechaEmision"
						FROM :v_RegistrosNoProcesados RNP
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS
							ON (RNP."IdInstrumento" = INS."IdInstrumento" OR RNP."Nemotecnico" = INS."Nemotecnico")
							WHERE INS."Precio" IS NOT NULL;
					--SELECT 'v_PreciosTasasInstrumento', * FROM :v_PreciosTasasInstrumento PTI;--BORRAR
					
					v_out_PreciosTasas =
						SELECT
							"IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
							"Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
							"FechaFuente", "MensajeError"
						FROM :v_out_PreciosTasas
						UNION ALL
						SELECT
							PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
							PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa", PTI."Tasa", '' AS "IdTipoPrecio", PTI."Precio", v_CodigoFuente AS "CodigoFuente",
							'Fuente: Precio Base Instrumento' AS "MensajeFuente", PTI."FechaEmision" AS "FechaFuente", NULL AS "MensajeError"
						FROM :v_PreciosTasasInstrumento PTI
						INNER JOIN :v_CriteriosSinFechas CRI
							ON ( PTI."IdRegistro" = CRI."IdRegistro" AND CRI."CodigoFuente" = :v_CodigoFuente);
					
					
					-- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
					v_RegistrosNoProcesados = 
						SELECT
							 RNP."IdRegistro"
							,RNP."IdTrabajo"
							,RNP."IdClasificacionTipoInstrumento"
							,RNP."IdTipoInstrumento"
							,RNP."IdInstrumento"
							,RNP."Nemotecnico"
							,RNP."IdMoneda"
							,RNP."PlazoResidual"
							,RNP."FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						WHERE RNP."Nemotecnico" NOT IN (SELECT INS."Nemotecnico" FROM :v_PreciosTasasInstrumento INS);

-------------------- OBTENCION PRECIOS MERCADO AGF --------------------
				ELSEIF (v_CodigoFuente = 'PRECIOSAGF') THEN
					-- Se obtienen Instrumentos que no han sido procesados y estan en bolsaprecioinstrumento
					
					v_PreciosMercadoAGF = 
						WITH preciosAGF as(		
							SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento",prec."FechaCierre" desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento",prec."FechaCierre",
							prec."ValorCuota"
							FROM :v_RegistrosNoProcesados RNP
							INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota" prec on prec."IdInstrumento" = RNP."IdInstrumento" and prec."FechaCierre" = "FechaConsulta"
						 )
					 	SELECT RNP."IdInstrumento","ValorCuota",RNP."IdRegistro",RNP."IdTrabajo",RNP."IdClasificacionTipoInstrumento",RNP."IdTipoInstrumento",RNP."Nemotecnico",RNP."IdMoneda",pre."FechaCierre",RNP."FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						LEFT JOIN preciosAGF pre on RNP."IdInstrumento" = pre."IdInstrumento"
					 	WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "ValorCuota" > 0
					 	ORDER BY "IdInstrumento","FechaConsulta","FechaCierre" desc;
					
					
					v_out_PreciosTasas =
						SELECT
							"IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
							"Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
							"FechaFuente", "MensajeError"
						FROM :v_out_PreciosTasas
						UNION ALL
						SELECT
							PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
							PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."ValorCuota" AS "Precio", v_CodigoFuente AS "CodigoFuente",
							'Fuente: PreciosMercadoAGF' AS "MensajeFuente", PTI."FechaCierre" AS "FechaFuente", NULL AS "MensajeError"
						FROM :v_PreciosMercadoAGF PTI
						INNER JOIN :v_CriteriosSinFechas CRI
							ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
					
					
					-- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
					v_RegistrosNoProcesados = 
						SELECT
							 RNP."IdRegistro"
							,RNP."IdTrabajo"
							,RNP."IdClasificacionTipoInstrumento"
							,RNP."IdTipoInstrumento"
							,RNP."IdInstrumento"
							,RNP."Nemotecnico"
							,RNP."IdMoneda"
							,RNP."PlazoResidual"
							,RNP."FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						WHERE RNP."Nemotecnico" NOT IN (SELECT INS."Nemotecnico" FROM :v_PreciosMercadoAGF INS);

-------------------- OBTENCION PRECIOS ARCHIVO VECTOR PRECIOS RIESGO--------------------
                ELSEIF (v_CodigoFuente = 'VECTORPRECIOSRIESGO') THEN

                    v_PreciosMercadoBcs = 
                        WITH precios as(        
                            SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento", TO_DATE(EVP."Fecha", 'YYYY-MM-DD') desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento","Fecha",
                            EVP."ValorRegistro" AS "PrecioCierre"
                            FROM :v_RegistrosNoProcesados RNP
                            INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = RNP."IdInstrumento"
                            INNER JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio" EVP on EVP."Entidad" = 'TASARIESGO' AND EVP."TipoRegistro" = 'PRECIOMERCADO'
                                AND INS."Nemotecnico" = EVP."Nemotecnico" and TO_DATE(EVP."Fecha", 'YYYY-MM-DD') <= RNP."FechaConsulta"
                         )
                        SELECT RNP."IdInstrumento","PrecioCierre",RNP."IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Nemotecnico","IdMoneda","Fecha","FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        LEFT JOIN precios pre on RNP."IdInstrumento" = pre."IdInstrumento"
                        WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "PrecioCierre" > 0
                        ORDER BY "IdInstrumento", "FechaConsulta","Fecha" desc;
                    
                    v_out_PreciosTasas =
                        SELECT
                            "IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
                            "Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
                            "FechaFuente", "MensajeError"
                        FROM :v_out_PreciosTasas
                        UNION ALL
                        SELECT
                            PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
                            PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."PrecioCierre" AS "Precio", v_CodigoFuente AS "CodigoFuente",
                            'Fuente: VectorPreciosTasaRiesgo' AS "MensajeFuente", PTI."Fecha" AS "FechaFuente", NULL AS "MensajeError"
                        FROM :v_PreciosMercadoBcs PTI
                        INNER JOIN :v_CriteriosSinFechas CRI
                            ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
                    
                    -- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
                    v_RegistrosNoProcesados = 
                        SELECT
                             RNP."IdRegistro"
                            ,RNP."IdTrabajo"
                            ,RNP."IdClasificacionTipoInstrumento"
                            ,RNP."IdTipoInstrumento"
                            ,RNP."IdInstrumento"
                            ,RNP."Nemotecnico"
                            ,RNP."IdMoneda"
                            ,RNP."PlazoResidual"
                            ,RNP."FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        WHERE RNP."IdInstrumento" NOT IN (SELECT INS."IdInstrumento" FROM :v_PreciosMercadoBcs INS);
                        
-------------------- OBTENCION PRECIOS ARCHIVO VECTOR PRECIOS PROMEDIO BVL--------------------
                ELSEIF (v_CodigoFuente = 'VECTORPRECIOSMERCADOBVL') THEN

                    v_PreciosMercadoBcs = 
                        WITH precios as(        
                            SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento", TO_DATE(EVP."Fecha", 'YYYY-MM-DD') desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento","Fecha",
                            EVP."ValorRegistro" AS "PrecioCierre"
                            FROM :v_RegistrosNoProcesados RNP
                            INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = RNP."IdInstrumento"
                            INNER JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio" EVP on EVP."Entidad" = 'BVL' AND EVP."TipoRegistro" = 'PRECIOPROMEDIO'
                                AND INS."Nemotecnico" = EVP."Nemotecnico" and TO_DATE(EVP."Fecha", 'YYYY-MM-DD') <= RNP."FechaConsulta"
                         )
                        SELECT RNP."IdInstrumento","PrecioCierre",RNP."IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Nemotecnico","IdMoneda","Fecha","FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        LEFT JOIN precios pre on RNP."IdInstrumento" = pre."IdInstrumento"
                        WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "PrecioCierre" > 0
                        ORDER BY "IdInstrumento", "FechaConsulta","Fecha" desc;
                    
                    v_out_PreciosTasas =
                        SELECT
                            "IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
                            "Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
                            "FechaFuente", "MensajeError"
                        FROM :v_out_PreciosTasas
                        UNION ALL
                        SELECT
                            PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
                            PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."PrecioCierre" AS "Precio", v_CodigoFuente AS "CodigoFuente",
                            'Fuente: VectorPreciosMercado' AS "MensajeFuente", PTI."Fecha" AS "FechaFuente", NULL AS "MensajeError"
                        FROM :v_PreciosMercadoBcs PTI
                        INNER JOIN :v_CriteriosSinFechas CRI
                            ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
                    
                    -- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
                    v_RegistrosNoProcesados = 
                        SELECT
                             RNP."IdRegistro"
                            ,RNP."IdTrabajo"
                            ,RNP."IdClasificacionTipoInstrumento"
                            ,RNP."IdTipoInstrumento"
                            ,RNP."IdInstrumento"
                            ,RNP."Nemotecnico"
                            ,RNP."IdMoneda"
                            ,RNP."PlazoResidual"
                            ,RNP."FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        WHERE RNP."IdInstrumento" NOT IN (SELECT INS."IdInstrumento" FROM :v_PreciosMercadoBcs INS);        
-------------------- OBTENCION PRECIOS ARCHIVO VECTOR PRECIOS APERTURA BVL--------------------
                ELSEIF (v_CodigoFuente = 'VECTORPRECIOSAPERTURABVL') THEN

                    v_PreciosMercadoBcs = 
                        WITH precios as(        
                            SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento", TO_DATE(EVP."Fecha", 'YYYY-MM-DD') desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento","Fecha",
                            EVP."ValorRegistro" AS "PrecioCierre"
                            FROM :v_RegistrosNoProcesados RNP
                            INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = RNP."IdInstrumento"
                            INNER JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio" EVP on EVP."Entidad" = 'BVL' AND EVP."TipoRegistro" = 'PRECIOAPERTURA'
                                AND INS."Nemotecnico" = EVP."Nemotecnico" and TO_DATE(EVP."Fecha", 'YYYY-MM-DD') <= RNP."FechaConsulta"
                         )
                        SELECT RNP."IdInstrumento","PrecioCierre",RNP."IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Nemotecnico","IdMoneda","Fecha","FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        LEFT JOIN precios pre on RNP."IdInstrumento" = pre."IdInstrumento"
                        WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "PrecioCierre" > 0
                        ORDER BY "IdInstrumento", "FechaConsulta","Fecha" desc;
                    
                    v_out_PreciosTasas =
                        SELECT
                            "IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
                            "Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
                            "FechaFuente", "MensajeError"
                        FROM :v_out_PreciosTasas
                        UNION ALL
                        SELECT
                            PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
                            PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."PrecioCierre" AS "Precio", v_CodigoFuente AS "CodigoFuente",
                            'Fuente: VectorPreciosApertura' AS "MensajeFuente", PTI."Fecha" AS "FechaFuente", NULL AS "MensajeError"
                        FROM :v_PreciosMercadoBcs PTI
                        INNER JOIN :v_CriteriosSinFechas CRI
                            ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
                    
                    -- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
                    v_RegistrosNoProcesados = 
                        SELECT
                             RNP."IdRegistro"
                            ,RNP."IdTrabajo"
                            ,RNP."IdClasificacionTipoInstrumento"
                            ,RNP."IdTipoInstrumento"
                            ,RNP."IdInstrumento"
                            ,RNP."Nemotecnico"
                            ,RNP."IdMoneda"
                            ,RNP."PlazoResidual"
                            ,RNP."FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        WHERE RNP."IdInstrumento" NOT IN (SELECT INS."IdInstrumento" FROM :v_PreciosMercadoBcs INS);
-------------------- OBTENCION PRECIOS ARCHIVO VECTOR PRECIOS CIERRE BVL--------------------
                ELSEIF (v_CodigoFuente = 'VECTORPRECIOSCIERREBVL') THEN

                    v_PreciosMercadoBcs = 
                        WITH precios as(        
                            SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento", TO_DATE(EVP."Fecha", 'YYYY-MM-DD') desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento","Fecha",
                            EVP."ValorRegistro" AS "PrecioCierre"
                            FROM :v_RegistrosNoProcesados RNP
                            INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = RNP."IdInstrumento"
                            INNER JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio" EVP on EVP."Entidad" = 'BVL' AND EVP."TipoRegistro" = 'PRECIOCIERRE'
                                AND INS."Nemotecnico" = EVP."Nemotecnico" and TO_DATE(EVP."Fecha", 'YYYY-MM-DD') <= RNP."FechaConsulta"
                         )
                        SELECT RNP."IdInstrumento","PrecioCierre",RNP."IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Nemotecnico","IdMoneda","Fecha","FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        LEFT JOIN precios pre on RNP."IdInstrumento" = pre."IdInstrumento"
                        WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "PrecioCierre" > 0
                        ORDER BY "IdInstrumento", "FechaConsulta","Fecha" desc;
                    
                    v_out_PreciosTasas =
                        SELECT
                            "IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
                            "Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
                            "FechaFuente", "MensajeError"
                        FROM :v_out_PreciosTasas
                        UNION ALL
                        SELECT
                            PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
                            PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."PrecioCierre" AS "Precio", v_CodigoFuente AS "CodigoFuente",
                            'Fuente: VectorPreciosCierre' AS "MensajeFuente", PTI."Fecha" AS "FechaFuente", NULL AS "MensajeError"
                        FROM :v_PreciosMercadoBcs PTI
                        INNER JOIN :v_CriteriosSinFechas CRI
                            ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
                    
                    -- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
                    v_RegistrosNoProcesados = 
                        SELECT
                             RNP."IdRegistro"
                            ,RNP."IdTrabajo"
                            ,RNP."IdClasificacionTipoInstrumento"
                            ,RNP."IdTipoInstrumento"
                            ,RNP."IdInstrumento"
                            ,RNP."Nemotecnico"
                            ,RNP."IdMoneda"
                            ,RNP."PlazoResidual"
                            ,RNP."FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        WHERE RNP."IdInstrumento" NOT IN (SELECT INS."IdInstrumento" FROM :v_PreciosMercadoBcs INS);
-------------------- OBTENCION PRECIOS ARCHIVO VECTOR PRECIOS REPORTE BVL--------------------
                ELSEIF (v_CodigoFuente = 'VECTORPRECIOSREPORTEBVL') THEN

                    v_PreciosMercadoBcs = 
                        WITH precios as(        
                            SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento", TO_DATE(EVP."Fecha", 'YYYY-MM-DD') desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento","Fecha",
                            EVP."ValorRegistro" AS "PrecioCierre"
                            FROM :v_RegistrosNoProcesados RNP
                            INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = RNP."IdInstrumento"
                            INNER JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio" EVP on EVP."Entidad" = 'BVL' AND EVP."TipoRegistro" = 'PRECIOREPORTE'
                                AND INS."Nemotecnico" = EVP."Nemotecnico" and TO_DATE(EVP."Fecha", 'YYYY-MM-DD') <= RNP."FechaConsulta"
                         )
                        SELECT RNP."IdInstrumento","PrecioCierre",RNP."IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Nemotecnico","IdMoneda","Fecha","FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        LEFT JOIN precios pre on RNP."IdInstrumento" = pre."IdInstrumento"
                        WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "PrecioCierre" > 0
                        ORDER BY "IdInstrumento", "FechaConsulta","Fecha" desc;
                    
                    v_out_PreciosTasas =
                        SELECT
                            "IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
                            "Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
                            "FechaFuente", "MensajeError"
                        FROM :v_out_PreciosTasas
                        UNION ALL
                        SELECT
                            PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
                            PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."PrecioCierre" AS "Precio", v_CodigoFuente AS "CodigoFuente",
                            'Fuente: VectorPreciosReporte' AS "MensajeFuente", PTI."Fecha" AS "FechaFuente", NULL AS "MensajeError"
                        FROM :v_PreciosMercadoBcs PTI
                        INNER JOIN :v_CriteriosSinFechas CRI
                            ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
                    
                    -- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
                    v_RegistrosNoProcesados = 
                        SELECT
                             RNP."IdRegistro"
                            ,RNP."IdTrabajo"
                            ,RNP."IdClasificacionTipoInstrumento"
                            ,RNP."IdTipoInstrumento"
                            ,RNP."IdInstrumento"
                            ,RNP."Nemotecnico"
                            ,RNP."IdMoneda"
                            ,RNP."PlazoResidual"
                            ,RNP."FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        WHERE RNP."IdInstrumento" NOT IN (SELECT INS."IdInstrumento" FROM :v_PreciosMercadoBcs INS);      
                        
-------------------- OBTENCION PRECIOS ARCHIVO VECTOR PRECIOS REPORTE BVL--------------------
                ELSEIF (v_CodigoFuente = 'VECTORPRECIOSCORREDORA') THEN

                    v_PreciosMercadoBcs = 
                        WITH precios as(        
                            SELECT row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento", TO_DATE(EVP."Fecha", 'YYYY-MM-DD') desc) AS row_num,RNP."IdRegistro",RNP."IdInstrumento","Fecha",
                            EVP."ValorRegistro" AS "PrecioCierre"
                            FROM :v_RegistrosNoProcesados RNP
                            INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = RNP."IdInstrumento"
                            INNER JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio" EVP on EVP."Entidad" = 'CORREDORA' AND EVP."TipoRegistro" = 'TASAANUAL'
                                AND INS."Nemotecnico" = EVP."Nemotecnico" and TO_DATE(EVP."Fecha", 'YYYY-MM-DD') <= RNP."FechaConsulta"
                         )
                        SELECT RNP."IdInstrumento","PrecioCierre",RNP."IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Nemotecnico","IdMoneda","Fecha","FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        LEFT JOIN precios pre on RNP."IdInstrumento" = pre."IdInstrumento"
                        WHERE row_num = 1 AND RNP."IdRegistro" = pre."IdRegistro" AND "PrecioCierre" > 0
                        ORDER BY "IdInstrumento", "FechaConsulta","Fecha" desc;
                    
                    v_out_PreciosTasas =
                        SELECT
                            "IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
                            "Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
                            "FechaFuente", "MensajeError"
                        FROM :v_out_PreciosTasas
                        UNION ALL
                        SELECT
                            PTI."IdRegistro", PTI."IdTrabajo", PTI."IdClasificacionTipoInstrumento", PTI."IdTipoInstrumento", PTI."IdInstrumento",
                            PTI."Nemotecnico", PTI."IdMoneda", '' AS "IdTipoTasa",0 AS "Tasa", '' AS "IdTipoPrecio", PTI."PrecioCierre" AS "Precio", v_CodigoFuente AS "CodigoFuente",
                            'Fuente: VectorPreciosCorredora' AS "MensajeFuente", PTI."Fecha" AS "FechaFuente", NULL AS "MensajeError"
                        FROM :v_PreciosMercadoBcs PTI
                        INNER JOIN :v_CriteriosSinFechas CRI
                            ON ( CRI."CodigoFuente" = :v_CodigoFuente AND PTI."IdRegistro" = CRI."IdRegistro");
                    
                    -- Se obtienen Instrumentos que NO estan en tabla Instrumentos.
                    v_RegistrosNoProcesados = 
                        SELECT
                             RNP."IdRegistro"
                            ,RNP."IdTrabajo"
                            ,RNP."IdClasificacionTipoInstrumento"
                            ,RNP."IdTipoInstrumento"
                            ,RNP."IdInstrumento"
                            ,RNP."Nemotecnico"
                            ,RNP."IdMoneda"
                            ,RNP."PlazoResidual"
                            ,RNP."FechaConsulta"
                        FROM :v_RegistrosNoProcesados RNP
                        WHERE RNP."IdInstrumento" NOT IN (SELECT INS."IdInstrumento" FROM :v_PreciosMercadoBcs INS);                              
-------------------- OBTENCION PRECIOS/TASAS TIPOINSTRUMENTO --------------------
				ELSE
					-- Se obtienen Instrumentos que no han sido procesados y estan en tabla PrecioTasaTipoInstrumento
					v_RegistrosPrecioTasaTipoInstrumento = 
						SELECT
							 RNP."IdRegistro"
							,RNP."IdTrabajo"
							,RNP."IdClasificacionTipoInstrumento"
							,RNP."IdTipoInstrumento"
							,RNP."IdInstrumento"
							,RNP."Nemotecnico"
							,RNP."IdMoneda"
							,PTTI."IdPrecioTasaTipoInstrumento"
							,PTTI."Tasa"
							,PTTI."Precio"
							,ADD_DAYS(PTTI."FechaModificacion", COALESCE(PTTI."PlazoDesde",0)) AS "FechaDesde"
							,ADD_DAYS(PTTI."FechaModificacion", COALESCE(PTTI."PlazoHasta",0)) AS "FechaHasta"
						FROM :v_RegistrosNoProcesados RNP
						INNER JOIN "BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento" PTTI
							ON (RNP."IdTipoInstrumento" = PTTI."IdTipoInstrumento" AND PTTI."IdEstado" = 'ACT');
					
					--SELECT 'v_RegistrosPrecioTasaTipoInstrumento', * from :v_RegistrosPrecioTasaTipoInstrumento; --BORRAR
					
					-- Se obtienen registros donde los Plazos estén en las fechas de los Criterios
					v_RegistrosTipoInstrumentoCriterio = 
						SELECT
							 PTTI."IdPrecioTasaTipoInstrumento"
							,PTTI."IdTipoInstrumento"
							,PTTI."Tasa"
							,PTTI."Precio"
							,PTTI."FechaDesde"
							,PTTI."FechaHasta"
							,:v_IdCriterio AS "IdCriterio"
						FROM :v_RegistrosPrecioTasaTipoInstrumento PTTI
						INNER JOIN :v_Criterios CRI
							ON ( PTTI."IdClasificacionTipoInstrumento" = CRI."IdClasificacionTipoInstrumento" AND
								(PTTI."IdTipoInstrumento" = CRI."IdTipoInstrumento" OR CRI."IdTipoInstrumento" = '0'))
						WHERE PTTI."FechaDesde" <= CRI."FechaCriterioHasta"
						  AND PTTI."FechaHasta" >= CRI."FechaCriterioHasta"
						  AND CRI."IdCriterioSeleccionTasas" = v_IdCriterio;
					--SELECT 'v_RegistrosTipoInstrumentoCriterio', * FROM :v_RegistrosTipoInstrumentoCriterio PTTI; --BORRAR
					
					-- Se obtienen los registros más recientes, MAX("FechaDesde")
					v_PreciosTasasTipoInstrumento =
						SELECT
							 RTIC."IdRegistro"
							,RTIC."IdTrabajo"
							,RTIC."IdClasificacionTipoInstrumento"
							,RTIC."IdTipoInstrumento"
							,RTIC."IdInstrumento"
							,RTIC."Nemotecnico"
							,RTIC."IdMoneda"
							,MAXTI."Tasa"
							,MAXTI."Precio"
							,MAXTI."FechaDesde"
						FROM :v_RegistrosPrecioTasaTipoInstrumento RTIC
						INNER JOIN (SELECT "IdTipoInstrumento", "Tasa", "Precio", MAX("FechaDesde") AS "FechaDesde", "FechaHasta"
									FROM :v_RegistrosTipoInstrumentoCriterio
									GROUP BY "IdTipoInstrumento", "Tasa", "Precio", "FechaHasta") MAXTI
							ON (RTIC."IdTipoInstrumento"= MAXTI."IdTipoInstrumento" AND 
								RTIC."FechaDesde"		= MAXTI."FechaDesde" AND 
								RTIC."FechaHasta"		= MAXTI."FechaHasta");
					--SELECT 'v_PreciosTasasTipoInstrumento', * FROM :v_PreciosTasasTipoInstrumento; --BORRAR
					
					v_out_PreciosTasas =
						SELECT
							"IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento",
							"Nemotecnico", "IdMoneda", "IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente",
							"FechaFuente", "MensajeError"
						FROM :v_out_PreciosTasas
						UNION ALL
						SELECT
							PTTI."IdRegistro", PTTI."IdTrabajo", PTTI."IdClasificacionTipoInstrumento", PTTI."IdTipoInstrumento", PTTI."IdInstrumento",
							PTTI."Nemotecnico", PTTI."IdMoneda", NULL AS "IdTipoTasa", PTTI."Tasa", NULL AS "IdTipoPrecio", PTTI."Precio", v_CodigoFuente AS "CodigoFuente",
							'Fuente: Valores por Tipo Instrumento' AS "MensajeFuente", PTTI."FechaDesde" AS "FechaFuente", NULL AS "MensajeError"
						FROM :v_PreciosTasasTipoInstrumento PTTI;
					
					
					-- Se obtienen registros que no estan en tabla PrecioTasaTipoInstrumento
					v_RegistrosNoProcesados = 
						SELECT
							 RNP."IdRegistro"
							,RNP."IdTrabajo"
							,RNP."IdClasificacionTipoInstrumento"
							,RNP."IdTipoInstrumento"
							,RNP."IdInstrumento"
							,RNP."Nemotecnico"
							,RNP."IdMoneda"
							,RNP."PlazoResidual"
							,RNP."FechaConsulta"
						FROM :v_RegistrosNoProcesados RNP
						WHERE RNP."IdTipoInstrumento" NOT IN (SELECT PTTI."IdTipoInstrumento" FROM :v_RegistrosTipoInstrumentoCriterio PTTI);
					
				END IF;
				
				--SELECT 'v_RegistrosNoProcesados', * FROM :v_RegistrosNoProcesados; --BORRAR
				
				-- Se verifica si todos los :p_Instrumentos obtuvieron valor Precio/Tasa
				SELECT COUNT(1) INTO v_Count FROM :v_RegistrosNoProcesados;
				IF(v_Count > 0) THEN
					CONTINUE;
				ELSE
					BREAK;
				END IF;
				
			END FOR;
			
		END IF;
		
		
		--select * from :v_out_PreciosTasas; --BORRAR
		--select * from :v_RegistrosNoProcesados; --BORRAR
		
		
		-- RESULTADO PROCESO
		INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_PreciosTasasInstrumentos"
		SELECT
			"IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico",
			"IdTipoTasa", "Tasa", "IdTipoPrecio", "Precio", "CodigoFuente", "MensajeFuente", "FechaFuente", "MensajeError", "IdMoneda"
		FROM :v_out_PreciosTasas
		UNION ALL
		SELECT
			"IdRegistro", "IdTrabajo", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico",
			NULL AS "IdTipoTasa", NULL AS "Tasa", NULL AS "IdTipoPrecio", NULL AS "Precio", 'FUENTE NO CONFIGURADA' AS "CodigoFuente",
			NULL AS "MensajeFuente", NULL AS "FechaFuente", 'REGISTRO NO FUE PROCESADO' AS "MensajeError", "IdMoneda"
		FROM :v_RegistrosNoProcesados RNP
		WHERE RNP."IdRegistro" NOT IN (SELECT "IdRegistro" FROM :v_out_PreciosTasas);
		
		
		select * from "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_PreciosTasasInstrumentos";
		
  	END;
END;
