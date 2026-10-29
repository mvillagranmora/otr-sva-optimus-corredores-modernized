-- Shared fixture of the aging / bolsa / acreedores report cases. Synthetic data.
-- Clients: 1001 natural person (portfolios 2001 and 2004), 1002 legal entity flagged as related party, 1003 natural person.
-- Accounts: 4001 (1001,P001,CLP) 4002 (1001,P001,USD) 4003 (1002,P002,CLP) 4004 (1003,P003,CLP) 4005 (1001,P004,CLP).
-- 4006 (1002,P002,USD) 4007 (1003,P003,USD).
-- Receipts: 6001 is equity (RV) and makes a FAC movement "intermediation"; 6002 is fixed income (RF).
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" VALUES (101, 'N', 'Cliente Uno Natural', NULL, '11111111-1');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" VALUES (102, 'J', 'Persona Dos', 'Empresa Dos Ltda', '76222222-2');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" VALUES (103, 'N', 'Cliente Tres Natural', NULL, '33333333-3');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" VALUES (1001, 1, 101, 0);
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" VALUES (1002, 1, 102, 0);
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" VALUES (1003, 1, 103, 0);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (2001, 1001, 'P001', 'Inversion General', 0);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (2002, 1002, 'P002', 'Cartera Empresa', 0);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (2003, 1003, 'P003', 'Ahorro', 0);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (2004, 1001, 'P004', 'Segundo Portafolio', 0);
INSERT INTO "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" VALUES (102, 'PERSONA', 'RELCORRPER', 'S');
INSERT INTO "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" VALUES (1002, 'CLIENTE', 'RELCORR', 'S');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" VALUES (4001, 1001, 2001, 'CLP', 'ACT');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" VALUES (4002, 1001, 2001, 'USD', 'ACT');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" VALUES (4003, 1002, 2002, 'CLP', 'ACT');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" VALUES (4004, 1003, 2003, 'CLP', 'ACT');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" VALUES (4005, 1001, 2004, 'CLP', 'ACT');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" VALUES (4006, 1002, 2002, 'USD', 'ACT');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" VALUES (4007, 1003, 2003, 'USD', 'ACT');
INSERT INTO "BSCL.Facturacion.BD::FAC.Comprobante" VALUES (6001, 'RV', 'T0');
INSERT INTO "BSCL.Facturacion.BD::FAC.Comprobante" VALUES (6002, 'RF', 'T0');
INSERT INTO "BSCL.Plataforma.BD::IND.TipoIndicador" VALUES (1, 'PERSONA', 'RELCORRPER');
INSERT INTO "BSCL.Plataforma.BD::IND.TipoIndicador" VALUES (2, 'CLIENTE', 'RELCORR');
INSERT INTO "BSCL.Plataforma.BD::IND.Indicador" VALUES (1, 102, 'PERSONA', 1, 'S');
INSERT INTO "BSCL.Plataforma.BD::IND.Indicador" VALUES (2, 1002, 'CLIENTE', 2, 'S');
