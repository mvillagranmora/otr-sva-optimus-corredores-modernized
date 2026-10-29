-- case seed on top of seed/base-fmm.sql + seed/base-fsa.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "FondoExtranjero" = 'n' WHERE "IdFondo" = '52';
