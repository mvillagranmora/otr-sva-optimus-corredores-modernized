WITH MovTran AS (
	SELECT 
		T."IdCorredorContraparte",
		CMCT."IdMovimientoCarteraEnTransito" AS "IdRegistro",
		ROW_NUMBER() OVER(PARTITION BY CMCT."IdMovimientoCarteraEnTransito" ORDER BY CMCT."IdMovimientoCarteraEnTransito" DESC) AS "RowNumber"
	FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" CMCT
	LEFT JOIN BSCL."BSCL.Facturacion.BD::FAC.Comprobante" FC 
		ON FC."Cantidad" = CMCT."Cantidad"
		AND FC."FechaLiquidacion" = CMCT."FechaLiquidacion"
		AND FC."FechaLiquidacion" = CMCT."FechaOperacion"
		AND FC."IdPortafolio" = CMCT."IdPortafolio"
		AND FC."NemotecnicoSubyacente" = CMCT."Nemotecnico"
		AND FC."IdInstrumentoSubyacente" = CMCT."IdInstrumento"
		AND FC."IdConcepto" = CMCT."IdConcepto"
	LEFT JOIN BSCL."BSCL.Facturacion.BD::FAC.Comprobante" FCORIG ON FCORIG."IdComprobante" = FC."IdComprobanteReferencia"
	LEFT JOIN BSCL."BSCL.Facturacion.BD::FAC.ComprobanteDetalle" FCD ON FCD."IdComprobante" = FCORIG."IdComprobante"
	LEFT JOIN BSCL."BSCL.Orden.BD::ORD.Transaccion" T ON T."IdTransaccion" = FCD."IdTransaccion" 
	WHERE CMCT."IdMovimientoCarteraEnTransito" IN (:idsMovimientosTransito)
)
SELECT 
	"IdCorredorContraparte",
	"IdRegistro"
FROM MovTran
WHERE "RowNumber" = 1
