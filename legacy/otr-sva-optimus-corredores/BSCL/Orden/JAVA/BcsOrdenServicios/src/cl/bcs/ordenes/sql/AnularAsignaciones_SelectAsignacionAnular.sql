WITH
    CteAsignacionEntrada AS (
     	SELECT
			'CteAsignacionEntrada' AS "TablaOrigen"
			, Asignacion."IdAsignacion" AS "IdAsignacion"
			, Asignacion."IdAsignacionRef" AS "IdAsignacionRef"
			, Asignacion."IdAsignacionGrupo" AS "IdAsignacionGrupo"
		FROM
			"BSCL.Orden.BD::ORD.Asignacion" Asignacion
		WHERE
		    Asignacion."IdAsignacion" IN (:IdAsignacion)
	)
	, CteAsignacionGrupo AS (
		SELECT
		    'CteAsignacionGrupo' AS "TablaOrigen"
			, Asignacion."IdAsignacion" AS "IdAsignacion"
			, Asignacion."IdAsignacionRef" AS "IdAsignacionRef"
			, Asignacion."IdAsignacionGrupo" AS "IdAsignacionGrupo"
		FROM
			CteAsignacionEntrada Filtro
			INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" Asignacion ON
				(
				    	Asignacion."IdAsignacionGrupo" = Filtro."IdAsignacionGrupo"
				    AND Asignacion."EsAPlazo" = 'N'
				    AND Asignacion."IdAsignacion" <> Filtro."IdAsignacion"
				)
		WHERE
		    Filtro."IdAsignacionGrupo" > 0
		    
	)
	, CteAsignacionSimultaneaRelacionada AS (
	 	SELECT
	 	    'CteAsignacionSimultaneaRelacionada' AS "TablaOrigen"
			, Asignacion."IdAsignacion" AS "IdAsignacion"
			, Asignacion."IdAsignacionRef" AS "IdAsignacionRef"
			, Asignacion."IdAsignacionGrupo" AS "IdAsignacionGrupo"
	 	FROM
			CteAsignacionEntrada Filtro
			INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" Asignacion ON (Asignacion."IdAsignacion" = Filtro."IdAsignacion")
	 		INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" Transaccion ON (Transaccion."IdTransaccion" = Asignacion."IdTransaccion")
	 		WHERE
	 		    	Asignacion."IdAsignacionRef" > 0
				AND Asignacion."IdAsignacionRef" <> Filtro."IdAsignacion"
	 		    AND (Transaccion."IdTipoInstrumento" = 'SIM' OR Transaccion."IdClasificacionTipoInstrumento" = 'RE') 
	)

SELECT
    Filtro."TablaOrigen" AS "TablaOrigen"
    , Asignacion."IdAsignacion" AS "IdAsignacion"
	, Asignacion."IdAsignacionRef" AS "IdAsignacionRef"
	, Asignacion."IdAsignacionGrupo" AS "IdAsignacionGrupo"
	, Orden."IdInstrumento" AS "IdInstrumento"
	, Orden."IdPortafolioCliente" AS "IdPortafolio"
	, Orden."IdCliente" AS "IdCliente"
	, Orden."IdTipoOperacion" AS "IdTipoOperacion"
	, Orden."IdTipoCanalIngreso" AS "IdTipoCanalIngreso"
	, Orden."IdMonedaOrden" AS "IdMonedaOrden"
	, Asignacion."CantidadAsignada" AS "Cantidad"
	, Asignacion."ValorRescateAsignado" AS "ValorRescate"
	, Asignacion."MontoAsignadoLocal" AS "MontoAsignadoLocal"
	, Asignacion."IdEstadoAsignacion" AS "IdEstadoAsignacion"
	, Transaccion."Folio" AS "Folio"
	, Transaccion."FechaVencimiento" AS "FechaVencimientoInstrumento"
	, Transaccion."Tasa" AS "Tasa"
	, Transaccion."NemotecnicoSVS" AS "NemoSVS"
FROM
    (
		SELECT "IdAsignacion", "TablaOrigen" FROM CteAsignacionEntrada
	  	UNION ALL
		SELECT "IdAsignacion", "TablaOrigen" FROM CteAsignacionGrupo
		UNION ALL
		SELECT "IdAsignacion", "TablaOrigen" FROM CteAsignacionSimultaneaRelacionada
	) Filtro
	INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" Asignacion ON (Asignacion."IdAsignacion" = Filtro."IdAsignacion")
	INNER JOIN "BSCL.Orden.BD::ORD.Orden" Orden ON (Orden."IdOrden" = Asignacion."IdOrden")
	INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" Transaccion ON (Transaccion."IdTransaccion" = Asignacion."IdTransaccion");
