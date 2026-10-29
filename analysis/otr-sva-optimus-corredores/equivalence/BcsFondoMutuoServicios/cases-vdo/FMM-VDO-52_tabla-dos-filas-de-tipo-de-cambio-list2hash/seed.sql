-- case seed on top of seed/base-fmm.sql + seed/base-vdo.sql (fake data)
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio" DROP PRIMARY KEY;
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio" ("IdSerieFondo", "IdEmpresa", "ValorDolarAporte", "ValorDolarRescate") VALUES ('V001', 1, 1.0, 2.0);
