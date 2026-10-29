INSERT INTO "BSCL.FondoMutuo.BD::FMM.Fondo" VALUES (1, 'S');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.Fondo" VALUES (2, 'N');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" VALUES ('FMMM-A', 1, 501);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" VALUES ('FMNO-A', 2, 502);
INSERT INTO "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota" VALUES (1, 'FMMM-A', 1500.5, DATE '2025-03-14');
INSERT INTO "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota" VALUES (1, 'FMNO-A', 900, DATE '2025-03-14');
INSERT INTO "BSCL.Custodia.BD::CUS.Cartera" ("IdEmpresa","IdCliente","IdPortafolio","Nemotecnico","IdMonedaInstrumento","INGCantidad","IdEstado","IdClasificacionTipoInstrumento","IdTipoReajuste","IdTipoOperacion","INGMontoAPlazo","FechaLiquidacion","FechaVencimientoInstrumento") VALUES (1, 1001, 2001, 'FMMM-A', 'CLP', 10, 'VIG', 'FM', NULL, 'VENTA', 0, DATE '2025-03-01', DATE '2025-12-31');
INSERT INTO "BSCL.Custodia.BD::CUS.Cartera" ("IdEmpresa","IdCliente","IdPortafolio","Nemotecnico","IdMonedaInstrumento","INGCantidad","IdEstado","IdClasificacionTipoInstrumento","IdTipoReajuste","IdTipoOperacion","INGMontoAPlazo","FechaLiquidacion","FechaVencimientoInstrumento") VALUES (1, 1001, 2001, 'FMNO-A', 'CLP', 5, 'VIG', 'FM', NULL, 'VENTA', 0, DATE '2025-03-01', DATE '2025-12-31');
