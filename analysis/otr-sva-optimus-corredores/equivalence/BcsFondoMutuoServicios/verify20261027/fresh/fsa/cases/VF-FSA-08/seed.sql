-- case seed on top of seed/base-fmm.sql + seed/base-fsa.sql (fake data; verify 2026-10-27)
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "DiasRetencionesRescate" = 0, "DiasValutaCertificacionRescate" = 9 WHERE "IdSerieFondo" = 'FSN01';
