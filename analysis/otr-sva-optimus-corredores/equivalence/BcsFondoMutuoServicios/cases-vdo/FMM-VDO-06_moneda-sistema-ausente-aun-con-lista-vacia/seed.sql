-- case seed on top of seed/base-fmm.sql + seed/base-vdo.sql (fake data)
DELETE FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = 1 AND "CodigoParametro" = 'PLA.MonedaSistema';
