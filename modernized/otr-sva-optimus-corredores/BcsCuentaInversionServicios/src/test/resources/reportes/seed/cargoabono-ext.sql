-- Extra columns of CTA.CargoAbonoMovimiento read by ClientesPorPagarYCobrar / Anexo16. Listed FIRST in the cases that need them
-- (the earlier cases insert into the table positionally and must not see these columns).
ALTER TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ADD COLUMN "Saldo" DECIMAL(34,16);
ALTER TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ADD COLUMN "FechaIngresoRegistro" TIMESTAMP;
ALTER TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ADD COLUMN "IdEjecutivo" BIGINT;
ALTER TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ADD COLUMN "IdCondicionDeLiquidacion" VARCHAR(10);
ALTER TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ADD COLUMN "SaldoPorFechaMovimiento" DECIMAL(34,16);
ALTER TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ADD COLUMN "EsCargoAbonoPorTraspaso" VARCHAR(1);
ALTER TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ADD COLUMN "FechaLiquidacionOperacion" DATE;
ALTER TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ADD COLUMN "IdInstrumento" BIGINT;
ALTER TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ADD COLUMN "IdTipoOperacion" VARCHAR(10);
