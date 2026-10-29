-- case seed on top of seed/base-fmm.sql + seed/base-ifm.sql (fake data)
DELETE FROM "BSCL.FondoMutuo.BD::FMM.ValorCuota" WHERE "IdSerieFondo" = 'S002';
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ValorCuota" ("IdSerieFondo", "ValorCuota", "FechaCierre") VALUES ('S002', 1.5, DATE '2020-01-01');
