-- case seed on top of seed/base-fmm.sql + seed/base-rre.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "MontoMaximo" = 100 WHERE "IdSerieFondo" = 'FM002A';
