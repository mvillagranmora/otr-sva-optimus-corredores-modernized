WITH movimientosSpot AS (
	SELECT
		MOVCOM."IdMovimientoComprobante", 
		OPSP."EstadoCaja",
		ROW_NUMBER() OVER (PARTITION BY MOVCOM."IdMovimientoComprobante" ORDER BY OPSP."EstadoCaja" ASC) AS RN
	FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVCOM
	INNER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" OPSP ON OPSP."IdOperacionSpot" = MOVCOM."IdTransaccion" 
	WHERE
		MOVCOM."IdTransaccion" IN (:IdOperacionSpot)
)SELECT 
	"IdMovimientoComprobante",
	"EstadoCaja" 
FROM movimientosSpot WHERE RN = 1;