SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFormaDePago"; 
 GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFormaDePago" As
 
SELECT 
   "IdMovimientoComprobanteFormaDePago",
   "IdMovimientoComprobante",
   "IdTipoMovimiento",
   "IdMedioDePago",
   "NombreMedioDePago",
   "IdCuentaBancaria",
   "NombreCuentaBancaria",
   "IdTipoCuentaBancaria",
   "NombreTipoCuentaBancaria",
   "NumeroCuentaBancaria",
   "IdTarjetaBancaria",
   "IdTipoTarjetaBancaria",
   "NombreTipoTarjetaBancaria",
   "NumeroTarjetaBancaria",
   "FechaVctoTarjetaBancaria",
   "IdModalidadDePago",
   "NombreModalidadDePago",
   "IdBanco",
   "CodigoBanco",
   "NombreBanco",   
   "MontoLiquidacionFormaDePago" AS "MontoLiquidacionFormaDePago",
   "NumeroSerie",
   "NumeroDocumento",
   "FechaDocumentoPago",
   EstadoFacturacion."IdEstado",
   EstadoPla."Nombre" As "NombreEstado",
   "FechaIngresoRegistro",
   "IdMoneda",
   "IdTipoMovimientoSpot",
   "AfectarTesoreria",
   "AfectarCuentaInversion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" AS MvtoCmbteFPago
  INNER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS EstadoFacturacion 
     ON EstadoFacturacion."IdEstado" = MvtoCmbteFPago."IdEstado"
  INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPla      
     ON EstadoPla."IdEstado" = EstadoFacturacion."IdEstado"
WHERE Upper(EstadoFacturacion."NombreEntidad") = Upper('FAC.MovimientoComprobanteFormaDePago');
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFormaDePago" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

GO