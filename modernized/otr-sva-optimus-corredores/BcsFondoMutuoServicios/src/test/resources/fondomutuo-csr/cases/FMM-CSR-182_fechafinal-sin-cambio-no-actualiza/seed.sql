-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" SET "FechaFinalReinversion" = DATE '2026-03-03' WHERE "IdReinversionSerieFondo" = 1;
