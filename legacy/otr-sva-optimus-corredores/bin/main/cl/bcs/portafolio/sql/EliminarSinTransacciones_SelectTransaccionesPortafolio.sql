WITH CuentaInversionCliente AS (
    SELECT "IdCuentaInversion" FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion"
    WHERE "IdPortafolio" = :IdPortafolio
)

SELECT COUNT("Saldo") AS "Cantidad", 'SALDO' AS "Tabla"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
WHERE "IdCuentaInversion" IN (SELECT "IdCuentaInversion" FROM CuentaInversionCliente) -- No debe tener saldos registrados en ninguna cuenta

UNION

SELECT COUNT("IdCargoAbonoMovimiento") AS "Cantidad", 'MOVIMIENTOS' AS "Tabla"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
WHERE "IdCuentaInversion" IN (SELECT "IdCuentaInversion" FROM CuentaInversionCliente)

UNION

SELECT COUNT("IdPortafolio") AS "Cantidad", 'MOVIMIENTOS' AS "Tabla"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
WHERE "IdPortafolio" = :IdPortafolio

UNION

SELECT COUNT("IdPortafolio") AS "Cantidad", 'MESA' AS "Tabla"
FROM "BSCL.Mesa.BD::MES.Operacion"
WHERE "IdPortafolio" = :IdPortafolio

UNION

SELECT COUNT("IdPortafolioCliente") AS "Cantidad", 'ORDEN' AS "Tabla"
FROM "BSCL.Orden.BD::ORD.Orden" 
WHERE "IdPortafolioCliente" = :IdPortafolio

UNION

SELECT COUNT("IdPortafolio") AS "Cantidad", 'CARTERA' AS "Tabla"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "IdPortafolio" = :IdPortafolio

UNION

SELECT COUNT("IdPortafolio") AS "Cantidad", 'CARTERA' AS "Tabla"
FROM "BSCL.Custodia.BD::HIS.Cartera"
WHERE "IdPortafolio" = :IdPortafolio

UNION

SELECT COUNT("IdPortafolio") AS "Cantidad", 'GARANTIA' AS "Tabla"
FROM "BSCL.Custodia.BD::GTA.Garantia"
WHERE "IdPortafolio" = :IdPortafolio

UNION

SELECT COUNT("IdPortafolio") AS "Cantidad", 'GARANTIA' AS "Tabla"
FROM "BSCL.Custodia.BD::HIS.Garantia"
WHERE "IdPortafolio" = :IdPortafolio

UNION

SELECT COUNT("IdPortafolioTitularPago") AS "Cantidad", 'TESORERIA' AS "Tabla"
FROM "BSCL.Tesoreria.BD::TES.Comprobante"
WHERE "IdPortafolioTitularPago" = :IdPortafolio

UNION

SELECT COUNT("IdPortafolio") AS "Cantidad", 'FONDOS_MUTUO' AS "Tabla"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
WHERE "IdPortafolio" = :IdPortafolio

