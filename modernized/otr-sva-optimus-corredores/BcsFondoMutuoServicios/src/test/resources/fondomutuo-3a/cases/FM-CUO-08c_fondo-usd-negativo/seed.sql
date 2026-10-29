CREATE LOCAL TEMPORARY TABLE "tmp_fondo" AS SELECT * FROM "BSCL.FondoMutuo.BD::FMM.Fondo" WHERE "IdFondo" = '10';
UPDATE "tmp_fondo" SET "IdMoneda" = 'USD', "IdFondo" = '20' WHERE 1 = 1;
INSERT INTO "BSCL.FondoMutuo.BD::FMM.Fondo" SELECT * FROM "tmp_fondo";
DROP TABLE "tmp_fondo";
CREATE LOCAL TEMPORARY TABLE "tmp_serie" AS SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" WHERE "IdSerieFondo" = 'FM001A';
UPDATE "tmp_serie" SET "IdInstrumento" = 901, "DecimalesCuota" = 2, "DecimalesValorCuota" = 2, "IdSerieFondo" = 'FM002U', "IdFondo" = '20' WHERE 1 = 1;
INSERT INTO "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SELECT * FROM "tmp_serie";
DROP TABLE "tmp_serie";
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio" ("IdSerieFondo", "IdEmpresa", "ValorDolarAporte", "ValorDolarRescate") VALUES ('FM002U', 1, 950, 940);
