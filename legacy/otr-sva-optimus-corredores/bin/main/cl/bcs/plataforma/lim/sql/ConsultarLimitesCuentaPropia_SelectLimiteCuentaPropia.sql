WITH
    CteLimiteCuentaPropia AS (
		SELECT
			Limite."IdLimite"                                                       AS "IdLimite"
			, Limite."IdTipoLimite"                                                 AS "IdTipoLimite"
			, Limite."IdClasificacionTipoInstrumento"                               AS "IdClasificacionTipoInstrumento"
			, Limite."IdTipoInstrumento"                                            AS "IdTipoInstrumento"
			, Limite."IdTipoOperacion"                                              AS "IdTipoOperacion"
			, Limite."IdInstrumento"                                                AS "IdInstrumento"
			, Limite."IdCanalIngreso"                                               AS "IdCanalIngreso"
			, Limite."CodigoOperador"                                               AS "CodigoOperador"
			, Limite."IdMoneda"                                                     AS "IdMoneda"
			, Limite."IdEmisor"                                                     AS "IdEmisor"
			, TipoComportamiento."Prioridad"                                        AS "PrioridadTipoComportamiento"
			, CASE Limite."IdCanalIngreso"
				WHEN '0' THEN 2 -- Comodin
				WHEN '_NA' THEN 1 -- No aplica
				ELSE 0 -- Especifico
			END                                                                     AS "PrioridadColumnaIdCanalIngreso"
			, CASE TRIM(Limite."CodigoOperador")
				WHEN '_NA' THEN 2 -- Comodin
				WHEN '0' THEN 1 -- No aplica
				ELSE 0 -- Especifico
			END                                                                     AS "PrioridadColumnaCodigoOperador"
			, CASE Limite."IdInstrumento"
				WHEN 0 THEN 2 -- Comodin
				WHEN -1 THEN 1 -- No aplica
				ELSE 0 -- Especifico
			END                                                                     AS "PrioridadColumnaIdInstrumento"
			, CASE WHEN (Limite."IdTipoInstrumento" = '0') THEN 1 ELSE 0 END        AS "PrioridadColumnaIdTipoInstrumento"
			, CASE WHEN (Limite."IdMoneda" = '0') THEN 1 ELSE 0 END                 AS "PrioridadColumnaIdMoneda"
			, CASE WHEN (Limite."IdTipoOperacion" = '0') THEN 1 ELSE 0 END          AS "PrioridadColumnaIdTipoOperacion"
			, CASE WHEN (Limite."IdEmisor" = '0') THEN 1 ELSE 0 END                 AS "PrioridadColumnaIdEmisor"
		FROM "BSCL.Plataforma.BD::LIM.Limite" AS Limite
			INNER JOIN "BSCL.Plataforma.BD::REQ.TipoComportamiento" AS TipoComportamiento ON (TipoComportamiento."IdTipoComportamiento" = Limite."IdTipoComportamiento")
			INNER JOIN "BSCL.Plataforma.BD::LIM.TipoLimite" AS TipoLimite ON (TipoLimite."IdTipoLimite" = Limite."IdTipoLimite" AND TipoLimite."IdEstado" = 'ACT')
			INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS Empresa ON (Empresa."IdEmpresa" = TipoLimite."IdEmpresa")
		WHERE (Limite."IdEstado" = 'ACT' AND (Limite."IdEstadoAutorizacion" IN ('AUT', 'ING')))
        	AND Limite."IdEmpresa" = :IdEmpresa
        	AND Limite."EsCuentaPropia" = 'S'
        	AND Limite."Borrado" = 'N'
    )
SELECT
    DENSE_RANK() OVER(
        PARTITION BY
            Clasificacion."IdInterno"
        ORDER BY
            CteLimite."PrioridadTipoComportamiento" ASC
            , CteLimite."PrioridadColumnaIdCanalIngreso" ASC
            , CteLimite."PrioridadColumnaCodigoOperador" ASC
            , CteLimite."PrioridadColumnaIdInstrumento" ASC
            , CteLimite."PrioridadColumnaIdTipoInstrumento" ASC
            , CteLimite."PrioridadColumnaIdMoneda" ASC
            , CteLimite."PrioridadColumnaIdTipoOperacion" ASC
            , CteLimite."PrioridadColumnaIdEmisor" ASC
            , CteLimite."IdLimite" ASC
    )                                               AS "PrioridadEvaluacion"
	, Clasificacion."IdInterno"                  AS "IdInterno"
	, CteLimite."IdLimite"                       AS "IdLimite"
	, Limite."CodigoOperador"                    AS "CodigoOperador"
	, Limite."IdInstrumento"                     AS "IdInstrumento"
	, Limite."MontoTotal"                        AS "MontoTotal"
	, Limite."MontoIntraday"                     AS "MontoIntraday"
	, Limite."MontoOvernight"                    AS "MontoOvernight"
	, Limite."Porcentaje"                        AS "Porcentaje"
	, Limite."MargenInferior"                    AS "MargenInferior"
	, Limite."MargenSuperior"                    AS "MargenSuperior"
	, Limite."Plazo"                             AS "Plazo"
	, Limite."IdTipoLimite"                      AS "IdTipoLimite"
	, Limite."IdCanalIngreso"                    AS "IdCanalIngreso"
	, Limite."IdTipoOperacion"                   AS "IdTipoOperacion"
	, Limite."UnidadPlazo"                       AS "UnidadPlazo"
	, TRIM(Limite."CodigoOperador")              AS "CodigoOperador"
	, Limite."IdMoneda"                          AS "IdMoneda"
	, Limite."IdTipoInstrumento"                 AS "IdTipoInstrumento"
	, Limite."IdClasificacionTipoInstrumento"    AS "IdClasificacionTipoInstrumento"
	, Limite."IdEmisor"                          AS "IdEmisor"
	, Limite."IdTipoComportamiento"              AS "IdTipoComportamiento"
	, Limite."UsaPlazo"                          AS "UsaPlazo"
	, Limite."EsCuentaPropia"                    AS "EsCuentaPropia"
	, COALESCE(ClasificacionRiesgo."IdEntidadClasificadoraRiesgo", '0') AS "IdEntidadClasificadoraRiesgo"
	, COALESCE(ClasificacionRiesgo."ClasificacionRiesgo", '0')          AS "ValorClasificacionRiesgo"
FROM #ConsultarLimitesCuentaPropia_LimitesPorClasificacion Clasificacion
    INNER JOIN CteLimiteCuentaPropia CteLimite ON (
			(CteLimite."IdClasificacionTipoInstrumento" = Clasificacion."IdClasificacionTipoInstrumento")
		AND (Clasificacion."IdTipoLimite" IS NULL OR CteLimite."IdTipoLimite" = Clasificacion."IdTipoLimite")
		AND (
		    		Clasificacion."IdTipoInstrumento" IS NULL -- Clasificacion no discrimina por IdTipoInstrumento
				OR	CteLimite."IdTipoInstrumento" = Clasificacion."IdTipoInstrumento" 
				OR	CteLimite."IdTipoInstrumento" = '0' -- Comodin
				OR	CteLimite."IdTipoInstrumento" = '' -- Limite no usa IdTipoInstrumento
		    )
		AND (
		    		Clasificacion."IdTipoOperacion" IS NULL
				OR	CteLimite."IdTipoOperacion" = Clasificacion."IdTipoOperacion"
				OR	CteLimite."IdTipoOperacion" = '0'
				OR	CteLimite."IdTipoOperacion" = ''
		    )
		AND (
		    		Clasificacion."IdInstrumento" IS NULL
				OR	CteLimite."IdInstrumento" = Clasificacion."IdInstrumento"
				OR	CteLimite."IdInstrumento" = 0 -- Comodin o limite no usa IdInstrumento
				OR	CteLimite."IdInstrumento" = -1 -- Codigo no aplica IdInstrumento
		    )
		AND (
		    		Clasificacion."IdTipoCanalIngreso" IS NULL
				OR	CteLimite."IdCanalIngreso" = Clasificacion."IdTipoCanalIngreso"
				OR	CteLimite."IdCanalIngreso" = '0' -- Comodin
				OR	CteLimite."IdCanalIngreso" = '' -- IdCanalIngreso no pertenece a la definicion del limite 
				OR	CteLimite."IdCanalIngreso" = '_NA' -- limite no discrimina por canal de ingreso (difiere de comodin al totalizar montos por ej)
		    )
		AND (
		    		Clasificacion."CodigoOperador" IS NULL
				OR	CteLimite."CodigoOperador" = Clasificacion."CodigoOperador"
				OR	CteLimite."CodigoOperador" = '0'
				OR	CteLimite."CodigoOperador" = ''
				OR	CteLimite."CodigoOperador" = '_NA'
				OR 	CteLimite."CodigoOperador" = 'TODOS'
		    )
		AND (
		    		Clasificacion."IdMonedaOperacion" IS NULL
				OR	CteLimite."IdMoneda" = Clasificacion."IdMonedaOperacion"
				OR	CteLimite."IdMoneda" = '0'
				OR	CteLimite."IdMoneda" = ''
		    )
		AND (
		    		Clasificacion."IdEmisor" IS NULL
				OR	CteLimite."IdEmisor" = Clasificacion."IdEmisor"
				OR	CteLimite."IdEmisor" = '0'
				OR	CteLimite."IdEmisor" = ''
		    )
    )
	INNER JOIN "BSCL.Plataforma.BD::LIM.Limite" Limite ON (Limite."IdLimite" = CteLimite."IdLimite")
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ClasificacionRiesgo ON (ClasificacionRiesgo."IdClasificacionRiesgo" = Limite."IdClasificacionRiesgo")
ORDER BY Clasificacion."IdInterno", "PrioridadEvaluacion" ASC;
