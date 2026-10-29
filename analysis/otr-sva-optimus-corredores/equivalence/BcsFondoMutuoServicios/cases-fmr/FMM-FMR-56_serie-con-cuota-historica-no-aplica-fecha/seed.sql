-- case seed on top of seed/base-fmm.sql + seed/base-fmr.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.ValorCuota" SET "ValorCuota" = 1.5, "FechaCierre" = DATE '2020-01-01' WHERE "IdSerieFondo" = 'S002';
