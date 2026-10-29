-- case seed on top of seed/base-fmm.sql + seed/base-fsa.sql (fake data)
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ALTER COLUMN "IdEmisor" SET NULL;
UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "IdEmisor" = NULL WHERE "IdFondo" = '51';
