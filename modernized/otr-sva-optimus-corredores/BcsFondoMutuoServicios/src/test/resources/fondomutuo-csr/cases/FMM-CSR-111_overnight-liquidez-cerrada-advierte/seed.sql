-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "EstadoLiquidezComercial" = 'C' WHERE "IdFondo" = '11';
