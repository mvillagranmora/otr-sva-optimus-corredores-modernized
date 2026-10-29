-- case seed on top of seed/base-fmm.sql + schema-cpf.sql + seed/base-cpf.sql (fake data)
CREATE LOCAL TEMPORARY TABLE "tmp_fondo" AS SELECT * FROM "BSCL.FondoMutuo.BD::FMM.Fondo" WHERE "IdFondo" = '10';
UPDATE "tmp_fondo" SET "IdFondo" = '16' WHERE 1 = 1;
INSERT INTO "BSCL.FondoMutuo.BD::FMM.Fondo" SELECT * FROM "tmp_fondo";
DROP TABLE "tmp_fondo";
CREATE LOCAL TEMPORARY TABLE "tmp_serie" AS SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" WHERE "IdSerieFondo" = 'FM001A';
UPDATE "tmp_serie" SET "IdInstrumento" = 900, "IdSerieFondo" = 'FM007F', "IdFondo" = '16' WHERE 1 = 1;
INSERT INTO "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SELECT * FROM "tmp_serie";
DROP TABLE "tmp_serie";
