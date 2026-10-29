-- case seed on top of seed/base-fmm.sql + seed/base-rre.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "CambioFondo" = 'N' WHERE "IdSerieFondo" = 'FM001A';
