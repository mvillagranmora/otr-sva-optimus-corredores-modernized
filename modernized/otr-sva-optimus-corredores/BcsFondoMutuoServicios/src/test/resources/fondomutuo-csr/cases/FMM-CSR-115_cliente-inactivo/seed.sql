-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
UPDATE "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteEstado" SET "IdEstado" = 'INA' WHERE "IdCliente" = 100;
