-- case seed on top of seed/base-fmm.sql + schema-tesoreria-fm.sql + seed/base-tesoreria-fm.sql (fake data)
UPDATE "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" SET "IdModalidadDePago" = 'MODXXX' WHERE "IdMedioDePago" = 'TRA';
