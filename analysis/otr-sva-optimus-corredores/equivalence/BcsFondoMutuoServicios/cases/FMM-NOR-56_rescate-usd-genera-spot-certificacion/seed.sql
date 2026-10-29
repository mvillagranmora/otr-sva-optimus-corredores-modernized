-- case seed on top of seed/base-fmm.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro" = 'S' WHERE "NombreTipoParametro" = 'RESCATE_GENERA_VENTA_SPOT_DOLAR';
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "Diferido" = 'S' WHERE 1=1;
