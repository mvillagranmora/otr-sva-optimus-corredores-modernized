WITH operacionCustodia AS (
	SELECT
		ROW_NUMBER() OVER (PARTITION BY det."IdOperacion"
	ORDER BY
		det."IdOperacion" DESC) AS "IdInternoDetalle",
		det."IdOperacion",
		det."IdEstado",
		'VIG' AS "IdEstadoSiguiente",
		det."IdReservaDetalle",
		det."FolioCarteraCustodiaDetalle",
		det."IdTipoProducto"
	FROM
		"BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF" det
	INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" Ope ON Ope."IdOperacion" = det."IdOperacion"	
), nuevoEstado AS (
	SELECT
		ope."IdOperacion",
		ope."IdInternoDetalle",
		estado."IdTipoProducto",
		ope."IdEstado",
		estado."IdEstadoSiguiente" AS "IdNuevoEstado",
		ope."IdReservaDetalle" AS "IdReserva",
		ope."FolioCarteraCustodiaDetalle" AS "FolioCarteraCustodia"
	FROM
		operacionCustodia ope
	LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa" estado 
	ON (ope."IdEstado" = estado."IdEstado" 
	AND estado."IdEstadoSiguiente" = ope."IdEstadoSiguiente" 
	AND estado."NombreEntidad"='CAMBIO_ESTADO' 
    AND estado."IdTipoProducto" = ope."IdTipoProducto")
) SELECT
	"IdOperacion",
	"IdInternoDetalle",
	"IdTipoProducto",
	"IdEstado",
	"IdNuevoEstado",
	"IdReserva",
	"FolioCarteraCustodia",
	CAST(0 AS DECIMAL(26,8)) AS "MontoOperacionAnticipo",
	CAST(0 AS DECIMAL(26,8)) AS "MontoOperacionInicialAnticipo",
	CAST(0 AS DECIMAL(26,8)) AS "MontoOperacionFinalAnticipo",
	CAST(0 AS DECIMAL(26,8)) AS "TasaAnticipo",
	CAST(0 AS DECIMAL(26,8)) AS "MontoOperacionMonedaAnticipo",
	CAST(0 AS DECIMAL(26,8)) AS "MontoOperacionInicialMonedaAnticipo",
	CAST(0 AS DECIMAL(26,8)) AS "MontoOperacionFinalMonedaAnticipo",
	'' AS "IdMonedaLiquidacionAnticipo",
	'' AS "IdCondicionLiquidacionAnticipo",
	'' AS "EstadoConfirmacion"
  FROM nuevoEstado;