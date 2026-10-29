UPDATE "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
SET "IdPortafolio" = :IdPortafolio,
"NombreLargoCliente" = COALESCE(
(SELECT NULLIF("Alias", '') FROM "BSCL.Portafolio.BD::POR.Portafolio" WHERE "IdPortafolio" = :IdPortafolio),
"NombreLargoCliente"
)
WHERE "IdMovimientoComprobante" = :IdMovimientoComprobante;