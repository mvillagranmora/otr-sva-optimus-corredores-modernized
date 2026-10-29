
-----------------------------------------------------------------
-- Lee los datos guardados en tabla global de detalle comprobante
-----------------------------------------------------------------

SELECT
	"IdInterno",
	"IdComprobanteInformado",	
	"IdComprobanteDetalle",
	"IdTipoMovimiento",
	"IdTipoComprobanteOrigen",
	"SecuenciaRelacionada",
	"PendienteDeLiquidar",
	"IdConcepto",
	"Monto"
FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteDetalle";

