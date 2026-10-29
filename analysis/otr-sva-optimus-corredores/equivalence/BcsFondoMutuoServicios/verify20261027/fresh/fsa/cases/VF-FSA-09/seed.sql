-- case seed on top of seed/base-fmm.sql + seed/base-fsa.sql (fake data; verify 2026-10-27)
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "DiasValutaValorCuotaAporte" = 15, "DiasValutaCertificacionAporte" = 15, "DiasValutaCurseAporte" = 3 WHERE "IdSerieFondo" = 'FSN02';
