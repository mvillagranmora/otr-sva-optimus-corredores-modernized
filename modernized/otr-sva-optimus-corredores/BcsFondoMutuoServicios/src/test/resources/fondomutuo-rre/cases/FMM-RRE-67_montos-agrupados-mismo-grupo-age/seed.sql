-- case seed on top of seed/base-fmm.sql + seed/base-rre.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "MontoMinimo" = 20000 WHERE "IdSerieFondo" = 'FM002A';
