-- case seed on top of seed/base-fmm.sql + seed/base-rre.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "PatrimonioMinimo" = 999999999 WHERE "IdSerieFondo" = 'FMX02U';
