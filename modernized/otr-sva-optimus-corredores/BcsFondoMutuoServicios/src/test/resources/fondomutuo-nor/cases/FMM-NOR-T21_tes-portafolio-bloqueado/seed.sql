-- case seed on top of seed/base-fmm.sql + schema-tesoreria-fm.sql + seed/base-tesoreria-fm.sql (fake data)
UPDATE "BSCL.Portafolio.BD::POR.Portafolio" SET "IdEstado" = 'BLO' WHERE "IdPortafolio" = 500;
