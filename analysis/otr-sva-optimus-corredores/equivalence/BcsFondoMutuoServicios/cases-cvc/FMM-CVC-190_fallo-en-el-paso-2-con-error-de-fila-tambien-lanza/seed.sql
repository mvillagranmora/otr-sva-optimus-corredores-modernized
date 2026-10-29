-- case seed on top of seed/base-fmm.sql + schema-cvc.sql + seed/base-cvc.sql (fake data)
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" ADD CONSTRAINT "FALLA_INYECTADA" CHECK ("ValorCuota" <> 1500.5);
