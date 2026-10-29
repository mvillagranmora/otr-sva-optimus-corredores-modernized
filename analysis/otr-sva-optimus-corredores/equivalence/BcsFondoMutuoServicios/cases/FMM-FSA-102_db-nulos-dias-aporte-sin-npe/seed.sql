-- case seed on top of seed/base-fmm.sql + seed/base-fsa.sql (fake data)
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ALTER COLUMN "DiasRetencionesAporte" SET NULL;
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "DiasRetencionesAporte" = NULL WHERE "IdSerieFondo" = 'FSN01';
