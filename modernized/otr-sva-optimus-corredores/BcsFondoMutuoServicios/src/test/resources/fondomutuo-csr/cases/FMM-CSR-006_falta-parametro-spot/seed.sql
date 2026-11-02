-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'APORTE_GENERA_COMPRA_SPOT_DOLAR';
