-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
DELETE FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = 1 AND "CodigoParametro" = 'PLA.FechaSistema';
