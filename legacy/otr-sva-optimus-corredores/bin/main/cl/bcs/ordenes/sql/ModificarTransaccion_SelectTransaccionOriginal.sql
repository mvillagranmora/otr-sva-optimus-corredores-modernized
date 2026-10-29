SELECT 
	"IdTransaccion",
	"MontoDisponibleTransaccion" as "Monto",
	"EsBilateral",
	"BloqueoLiquidacion",
	TO_VARCHAR("FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion",
	"MontoRetencion",
	"AccionTipoOperacion",
	"TipoCambioCorresponsal",
	"IdCorredorCorresponsal",
	"MontoLiquidacionCorresponsal",
	"GastosCorresponsal",
	"MarcaNegociacion",
	"IdMonedaLiquidacionCorresponsal",
	"IdCamara"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI"
WHERE "IdTransaccion" IN (:IdTransaccion)