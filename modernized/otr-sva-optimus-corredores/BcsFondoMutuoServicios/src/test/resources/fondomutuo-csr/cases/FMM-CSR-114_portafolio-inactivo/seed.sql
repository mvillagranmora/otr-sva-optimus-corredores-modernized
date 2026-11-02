-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
UPDATE "BSCL.Portafolio.BD::POR.Portafolio" SET "IdEstado" = 'INA' WHERE "IdPortafolio" = 500;
