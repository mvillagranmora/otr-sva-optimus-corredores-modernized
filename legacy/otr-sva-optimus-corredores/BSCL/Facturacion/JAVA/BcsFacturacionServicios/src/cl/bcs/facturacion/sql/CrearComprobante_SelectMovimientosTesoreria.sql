SELECT
	"IdComprobanteOrigen",
	(CASE WHEN COALESCE("IdComprobante",0) = 0
	THEN "IdIngresoEgreso"
    ELSE "IdComprobante" END) AS "Id",
    "NombreTipoMovimiento",
    "IdEstadoComprobante",
    "Monto",
    "IdTipoMovimiento",
    "IdConcepto",
    "IdMoneda",
    "IdTipoOperacion",
    "IdTipoProducto"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso"   
WHERE "IdModulo" = 'FAC' 
	AND "IdComprobanteOrigen" IN (:idsComprobante)
	AND "IdModulo" = 'FAC'
	AND "IdEstado" NOT IN ('ANU','MOD');