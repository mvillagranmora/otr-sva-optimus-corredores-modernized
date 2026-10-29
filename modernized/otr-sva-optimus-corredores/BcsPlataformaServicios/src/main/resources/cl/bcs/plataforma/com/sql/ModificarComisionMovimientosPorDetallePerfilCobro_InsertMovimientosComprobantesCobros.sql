INSERT INTO "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro"
(
"IdMovimientoComprobanteCobro"
, "IdCobro"
, "IdMovimientoComprobante"
, "Porcentaje"
, "FechaSistema"
, "FechaIngresoRegistro"
, "IdEstado"
, "IdMonedaMovimientoComprobanteCobro"
, "MontoMovimientoComprobanteCobro"
, "MontoCobro"
, "IdMonedaLiquidacionCobro"
, "MontoLiquidacionCobro"
, "IdMonedaCobro"
, "Monto"
, "MontoLiquidacion"
, "MontoMovimientoComprobante"
)
VALUES(
  "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteCobro".NEXTVAL
, :IdCobro
, :IdMovimientoComprobante
, :Porcentaje
, :FechaSistema
, :FechaIngresoRegistro
, :IdEstado
, :IdMonedaMovimientoComprobanteCobro
, :MontoMovimientoComprobanteCobro
, :MontoCobro
, :IdMonedaLiquidacionCobro
, :MontoLiquidacionCobro
, :IdMonedaCobro
, :Monto
, :MontoLiquidacion
, :MontoMovimientoComprobante
);