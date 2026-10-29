-- Fixture of GenerarArchivoCuentasPorCobrarAnexo16. Synthetic. Uses clientes-reportes.sql (clients 1001-1003, accounts 4001-4007).
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" VALUES (1001, 101, '11111111-1', 'Cliente Uno Natural');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" VALUES (1002, 102, '76222222-2', 'Empresa Dos Ltda');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" VALUES (1003, 103, '33333333-3', 'Cliente Tres Natural');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" VALUES (1001, 101);
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" VALUES (1002, 102);
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" VALUES (1003, 103);
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarRelacionCorredora" VALUES (102);
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" VALUES (2001, 'P001', 'Inversion General', 'N');
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" VALUES (2002, 'P002', 'Cartera Empresa', 'N');
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" VALUES (2003, 'P003', 'Ahorro', 'N');
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" VALUES (2004, 'P004', 'Segundo Portafolio', 'N');
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" VALUES (4001, 1001, 2001, 'CLP');
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" VALUES (4002, 1001, 2001, 'USD');
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" VALUES (4003, 1002, 2002, 'CLP');
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" VALUES (4004, 1003, 2003, 'CLP');
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" VALUES (4005, 1001, 2004, 'CLP');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" VALUES ('CNC001');
INSERT INTO "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen" VALUES ('LOGO_CORREDOR', 1, X'89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D4944415478DA63FCFFFF3F0005FE02FEA7355AE20000000049454E44AE426082');
