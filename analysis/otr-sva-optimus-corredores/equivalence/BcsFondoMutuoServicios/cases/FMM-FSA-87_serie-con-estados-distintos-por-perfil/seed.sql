-- case seed on top of seed/base-fmm.sql + seed/base-fsa.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "EstadoLiquidezBackoffice" = 'A' WHERE "IdFondo" = '51';
