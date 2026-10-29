CREATE LOCAL TEMPORARY TABLE "tmp_serie" AS SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" WHERE "IdSerieFondo" = 'FM001A';
UPDATE "tmp_serie" SET "IdSerieFondo" = 'SINFONDO', "IdFondo" = '99' WHERE 1 = 1;
INSERT INTO "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SELECT * FROM "tmp_serie";
DROP TABLE "tmp_serie";
