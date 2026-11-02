-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "EstadoAporteBackoffice" = 'C', "EstadoLiquidezBackoffice" = 'C' WHERE "IdFondo" = '11';
