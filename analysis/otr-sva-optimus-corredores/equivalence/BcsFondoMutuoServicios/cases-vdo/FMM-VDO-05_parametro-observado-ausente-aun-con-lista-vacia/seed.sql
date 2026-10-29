-- case seed on top of seed/base-fmm.sql + seed/base-vdo.sql (fake data)
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'UsaDolarObservado';
