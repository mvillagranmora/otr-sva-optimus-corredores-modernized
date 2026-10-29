UPDATE CBM
SET CBM."Saldo" = :Saldo
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CBM
WHERE CBM."IdCargoAbonoMovimiento" = :IdCargoAbonoMovimiento;