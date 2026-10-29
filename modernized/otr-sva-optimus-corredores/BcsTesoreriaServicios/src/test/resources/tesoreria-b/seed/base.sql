-- Reference data shared by every family-B case (synthetic; no production rows, fake names).
-- Case-specific rows go to cases/<case>/seed.sql.
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda","Nombre") VALUES ('CLP','Peso Chileno');
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda","Nombre") VALUES ('USD','Dolar USA');
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda","Nombre") VALUES ('UF','Unidad de Fomento');

INSERT INTO "BSCL.Plataforma.BD::BAN.Banco" ("IdBanco","NombreBanco") VALUES (17,'Banco Uno');
INSERT INTO "BSCL.Plataforma.BD::BAN.Banco" ("IdBanco","NombreBanco") VALUES (18,'Banco Dos');

INSERT INTO "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco","IdEmpresa","IdBanco","IdMoneda","NumeroCuentaBancaria","NombreCuentaBancaria")
VALUES (136, 1, 17, 'CLP', '000111222', 'Cuenta Corriente CLP');
INSERT INTO "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco","IdEmpresa","IdBanco","IdMoneda","NumeroCuentaBancaria","NombreCuentaBancaria")
VALUES (137, 1, 17, 'USD', '000333444', 'Cuenta Corriente USD');
INSERT INTO "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco","IdEmpresa","IdBanco","IdMoneda","NumeroCuentaBancaria","NombreCuentaBancaria")
VALUES (138, 1, 18, 'CLP', '000555666', 'Cuenta Vista CLP');
INSERT INTO "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco","IdEmpresa","IdBanco","IdMoneda","NumeroCuentaBancaria","NombreCuentaBancaria")
VALUES (900, 2, 18, 'CLP', '000999000', 'Cuenta otra empresa');

-- PLA.MonedaFormato: GEN format of the currencies the seeds use (MONTO 2 decimals round, TIPOCAMBIO 4 round; CLP 0 decimals)
INSERT INTO "BSCL.Plataforma.BD::PLA.MonedaFormato" VALUES (1, 'CLP', 'GEN', '', '', 4, 'R', 4, 'R', 4, 'R', 0, 'R', 2, 'R', 4, 'R', 2, 'R', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.MonedaFormato" VALUES (2, 'USD', 'GEN', '', '', 4, 'R', 4, 'R', 4, 'R', 2, 'R', 2, 'R', 4, 'R', 2, 'R', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.MonedaFormato" VALUES (3, 'UF',  'GEN', '', '', 4, 'R', 4, 'R', 4, 'R', 4, 'T', 2, 'R', 4, 'R', 2, 'R', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.MonedaFormato" VALUES (4, 'EUR', 'GEN', '', '', 4, 'R', 4, 'R', 4, 'R', 2, 'R', 2, 'R', 4, 'R', 2, 'R', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda","Nombre") VALUES ('EUR','Euro');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" VALUES ('ACT','Activo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" VALUES ('ANU','Anulado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" VALUES ('LIQ','Liquidado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" VALUES ('PEN','Pendiente');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" VALUES ('MOD','Modificado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" VALUES ('ING','Ingresado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" VALUES ('PAGPRV','Pago proveedor');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" VALUES ('COBCLI','Cobro cliente');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" VALUES ('SPOT01','Compra venta spot');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" VALUES ('FWD001','Forward');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" VALUES ('CLI','Cliente');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" VALUES ('PRV','Proveedor');
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (8001, 9001, 'ACT', 'PORT-8001');
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (8002, 9002, 'ACT', 'PORT-8002');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" VALUES ('SPOT01','MSP');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" VALUES ('FWD001','MFW');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" VALUES ('PAGPRV','TES');
INSERT INTO "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco","IdEmpresa","IdBanco","IdMoneda","NumeroCuentaBancaria","NombreCuentaBancaria")
VALUES (139, 1, 17, 'EUR', '000777888', 'Cuenta Corriente EUR');

-- Clearing financiero reference data: the brokerage company 1 (person 6001, socio de negocio 5001) and its counterparties
INSERT INTO "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ("IdEmpresa","Codigo","Nombre","IdEstado","IdPais","IdSocioDeNegocio","NombreCorto","IdContraparte","IdPersona","DocumentoIdentidad","RazonSocial","EsCorredora")
VALUES (1, 'EMP1', 'Corredora Test SA', 'ACT', 44, 5001, 'CorrTest', 0, 6001, '76000000-0', 'Corredora Test SA', 'S');
INSERT INTO "BSCL.Plataforma.BD::PLA.Empresa" VALUES (1, 5001);
INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" VALUES (5001, 6001, 0, 1, 'N');
INSERT INTO "BSCL.Plataforma.BD::INS.Corredora" VALUES (700, 6001);
INSERT INTO "BSCL.Plataforma.BD::INS.Corredora" VALUES (701, 6101);
INSERT INTO "BSCL.Plataforma.BD::INS.Corredora" VALUES (702, 6102);
INSERT INTO "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" VALUES (700, '76000000-0', 'CorrTest', 'R-0700');
INSERT INTO "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" VALUES (701, '76111111-1', 'Corredor Alfa', 'R-0701');
INSERT INTO "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" VALUES (702, '76222222-2', 'Corredor Beta', 'R-0702');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" VALUES ('COMACN', 'COMPRA');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" VALUES ('VENACX', 'VENTA');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" VALUES ('COMRFI', 'COMPRA');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" VALUES ('VENRFI', 'VENTA');
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" VALUES ('T0', 'PH', 0);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" VALUES ('T1', 'PM', 1);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" VALUES ('T2', 'CN', 2);
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" VALUES ('ACN', 'RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" VALUES ('SIM', 'RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" VALUES ('DPF', 'RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" VALUES ('IIF', 'IF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" VALUES ('FUT', 'DV');

-- PLA.TipoMovimiento (module TES) and payment mode reference rows
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoMovimiento" VALUES ('TES', 'MOVING', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoMovimiento" VALUES ('TES', 'MOVEGR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoMovimiento" VALUES ('TES', 'MOVOLD', 'INA');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" VALUES ('TRA', 'MODDEP');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" VALUES ('CHE', 'MODCAJ');
