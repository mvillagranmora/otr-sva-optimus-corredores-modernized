-- case seed on top of seed/base-fmm.sql + schema-tesoreria-fm.sql + seed/base-tesoreria-fm.sql (fake data)
UPDATE "BSCL.Plataforma.BD::PLA.Moneda" SET "IdEstado" = 'INA' WHERE "IdMoneda" = 'CLP';
