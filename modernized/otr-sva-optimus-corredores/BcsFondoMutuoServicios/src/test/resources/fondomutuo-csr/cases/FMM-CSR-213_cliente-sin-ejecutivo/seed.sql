-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
UPDATE "BSCL.SocioNegocio.BD::CLI.Cliente" SET "IdPersonaEjecutivo" = 0 WHERE "IdCliente" = 100;
