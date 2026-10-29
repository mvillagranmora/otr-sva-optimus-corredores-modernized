-- case seed on top of seed/base-fmm.sql + seed/base-ifm.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "InstrumentoOWP" = NULL WHERE "IdSerieFondo" = 'S002';
