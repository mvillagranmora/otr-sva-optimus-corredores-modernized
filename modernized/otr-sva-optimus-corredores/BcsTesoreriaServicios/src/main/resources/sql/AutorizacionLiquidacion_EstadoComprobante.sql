SELECT
	COM."IdComprobante"
	,COM."IdEstado"
FROM "BSCL.Tesoreria.BD::TES.Comprobante" COM
WHERE "IdComprobante" = :IdComprobante;