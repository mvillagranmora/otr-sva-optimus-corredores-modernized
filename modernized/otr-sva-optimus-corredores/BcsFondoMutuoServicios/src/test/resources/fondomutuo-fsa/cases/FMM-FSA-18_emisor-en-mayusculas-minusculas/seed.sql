-- case seed on top of seed/base-fmm.sql + seed/base-fsa.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "IdEmisor" = 'ext1' WHERE "IdFondo" = '52';
UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "FondoExtranjero" = 's' WHERE "IdFondo" = '52';
