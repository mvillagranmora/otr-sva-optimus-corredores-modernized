UPDATE CBM
SET CBM."SaldoPorFechaMovimiento" = :Saldo
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CBM
WHERE CBM."IdCargoAbonoMovimiento" = :IdCargoAbonoMovimiento;