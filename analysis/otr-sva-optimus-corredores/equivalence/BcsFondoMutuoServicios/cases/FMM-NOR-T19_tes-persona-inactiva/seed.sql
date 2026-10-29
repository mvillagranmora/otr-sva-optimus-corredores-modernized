-- case seed on top of seed/base-fmm.sql + schema-tesoreria-fm.sql + seed/base-tesoreria-fm.sql (fake data)
UPDATE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaEstado" SET "IdEstado" = 'INA' WHERE "IdPersona" = 1000;
