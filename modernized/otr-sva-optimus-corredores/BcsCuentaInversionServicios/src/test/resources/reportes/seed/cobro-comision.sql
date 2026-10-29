-- Shared fixture of the cobro-comision cases (consultar / editar / eliminar / procesar). Synthetic data.
-- Clients: 1001 (non-APV portfolio 2001), 1002 (APV portfolio 2002, product 9). Instruments: 3001 RF, 3002 RV.
-- Periods: 1 = Jan 2025, 2 = Feb 2025, 3 = Mar 2025 (system date is 2025-03-14).
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" VALUES (1001, 101, '76000001-1', 'Cliente Uno SpA');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" VALUES (1002, 102, '12345678-5', 'Cliente Dos');
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (2001, 1001, 'P001', 'Inversion General', 0);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (2002, 1002, 'P002', 'Ahorro Previsional', 55);
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupadorDetalle" VALUES (2002, 9);
INSERT INTO "BSCL.APV.BD.Vistas::ConsultarOtroProductoAgrupador" VALUES (9, 'APV Plan Uno');
INSERT INTO "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" VALUES (2001, 'COBROCUST', '1');
INSERT INTO "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" VALUES (2002, 'COBROCUST', '2');
INSERT INTO "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" VALUES (3001, 'RF', 'S');
INSERT INTO "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" VALUES (3002, 'RV', 'S');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" VALUES (7001, 1, 'N', 'RF', 0.0012, 'ACT', DATE '2025-01-01', '2025-01-01', TIMESTAMP '2025-01-01 00:00:00');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" VALUES (7002, 1, 'N', 'CI', 0.0005, 'ACT', DATE '2025-01-01', '2025-01-01', TIMESTAMP '2025-01-01 00:00:00');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" VALUES (7003, 2, 'S', 'RF', 0.0008, 'ACT', DATE '2025-01-01', '2025-01-01', TIMESTAMP '2025-01-01 00:00:00');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" VALUES (7004, 2, 'S', 'CI', 0.0003, 'INA', DATE '2025-01-01', '2025-01-01', TIMESTAMP '2025-01-01 00:00:00');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.PeriodosComisiones" VALUES (1, DATE '2025-01-01', DATE '2025-01-31');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.PeriodosComisiones" VALUES (2, DATE '2025-02-01', DATE '2025-02-28');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.PeriodosComisiones" VALUES (3, DATE '2025-03-01', DATE '2025-03-31');
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarPeriodosComisiones" VALUES (1, DATE '2025-01-01', DATE '2025-01-31');
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarPeriodosComisiones" VALUES (2, DATE '2025-02-01', DATE '2025-02-28');
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarPeriodosComisiones" VALUES (3, DATE '2025-03-01', DATE '2025-03-31');
-- custody patrimony (RF) and cash patrimony ('CI' = cuenta inversion), February 2025
INSERT INTO "BSCL.Custodia.BD::CUS.PatrimonioCustodia" ("IdPatrimonioCustodia","IdCliente","IdPortafolio","IdInstrumento","FechaPatrimonio","TipoFechaConsulta","MontoComision","IdEstadoComision","MontoPatrimonio") VALUES (8001, 1001, 2001, 3001, DATE '2025-02-10', 'LIQ', 120.50, 'VIG', 1000000);
INSERT INTO "BSCL.Custodia.BD::CUS.PatrimonioCustodia" ("IdPatrimonioCustodia","IdCliente","IdPortafolio","IdInstrumento","FechaPatrimonio","TipoFechaConsulta","MontoComision","IdEstadoComision","MontoPatrimonio") VALUES (8002, 1001, 2001, 3001, DATE '2025-02-11', 'LIQ', 80.25, 'VIG', 700000);
INSERT INTO "BSCL.Custodia.BD::CUS.PatrimonioCustodia" ("IdPatrimonioCustodia","IdCliente","IdPortafolio","IdInstrumento","FechaPatrimonio","TipoFechaConsulta","MontoComision","IdEstadoComision","MontoPatrimonio") VALUES (8003, 1002, 2002, 3001, DATE '2025-02-10', 'LIQ', 40.00, 'VIG', 300000);
INSERT INTO "BSCL.Custodia.BD::CUS.PatrimonioCustodia" ("IdPatrimonioCustodia","IdCliente","IdPortafolio","IdInstrumento","FechaPatrimonio","TipoFechaConsulta","MontoComision","IdEstadoComision","MontoPatrimonio") VALUES (8004, 1001, 2001, 3001, DATE '2025-02-12', 'LIQ', 55.55, 'INA', 500000);
INSERT INTO "BSCL.Custodia.BD::CUS.PatrimonioCustodia" ("IdPatrimonioCustodia","IdCliente","IdPortafolio","IdInstrumento","FechaPatrimonio","TipoFechaConsulta","MontoComision","IdEstadoComision","MontoPatrimonio") VALUES (8005, 1001, 2001, 3001, DATE '2025-02-13', 'LIQ', 0, 'VIG', 0);
INSERT INTO "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" VALUES (9001, 1001, 2001, DATE '2025-02-10', 'LIQ', 'CLP', 'CLP', 1000, 1000, 0, 0, 0, 0, 0, 'ACT', 1000, 30.25, 0.0005, 'VIG');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" VALUES (9002, 1002, 2002, DATE '2025-02-10', 'LIQ', 'CLP', 'CLP', 500, 500, 0, 0, 0, 0, 0, 'ACT', 500, 11.10, 0.0003, 'VIG');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" VALUES (9003, 1001, 2001, DATE '2025-03-03', 'LIQ', 'CLP', 'CLP', 900, 900, 0, 0, 0, 0, 0, 'ACT', 900, 5.00, 0.0005, 'VIG');
INSERT INTO "BSCL.Plataforma.BD::COM.Cobro" VALUES ('IVA', 19);
