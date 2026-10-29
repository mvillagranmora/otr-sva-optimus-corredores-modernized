-- Custody rows for the end-to-end RescateReinversion cases (F3 custody in process, 2026-10-28). FICTITIOUS data. Applied after the FondoMutuo base seed
-- (fondomutuo-3a/seed/base-fmm.sql + fondomutuo-rre/seed/base-rre.sql) and the merged custody schema (EsquemaCustodiaFusionado). Written for THIS module (not a copy of
-- BcsCustodiaServicios): it makes the real custody flow answer, for the request of ConsultarFlujoCustodiaFondoMutuo (use RESCFM of client 100, portfolio 500, filter
-- FM / instrument 900 / regime GEN), the same totals the scripted double of the RRE cases returns: quantity 1000 of instrument 900, regime GEN.
-- (The double also says MontoAMercadoMonedaInstrumento = 1000000; the real flow does not value (p_ValorizarFlujo = N) so it is 0: the RRE cases do not depend on it.)

-- company 1 of the FondoMutuo seed (IdSocioDeNegocio 10, person 1): the columns of the custody read of the company
UPDATE "BSCL.Plataforma.BD::PLA.Empresa" SET "Codigo" = 'BCS', "Nombre" = 'Corredora Ficticia S.A.', "IdEstado" = 'ACT', "IdPais" = 44 WHERE "IdEmpresa" = 1;
INSERT INTO "BSCL.Plataforma.BD::INS.Corredora" ("IdCorredora", "IdPersona") VALUES (50, 1);
INSERT INTO "BSCL.Plataforma.BD::INS.CorredoraCodigos" ("IdCorredora", "IdBolsa", "CodigoCorredora") VALUES (50, 'BCS', '0099');
INSERT INTO "BSCL.Custodia.BD::CUS.Configuracion" ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, 'CUS.AgregarSimultaneasEventoCapitalCli', 'S');

-- business calendar of country 44 (Saturday and Sunday of the week of the cases; the FondoMutuo table does not allow a NULL date)
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (1, 'INHABIL', DATE '2026-03-07', 5, 'SABADO', 2026, 44, 'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (2, 'INHABIL', DATE '2026-03-08', 6, 'DOMINGO', 2026, 44, 'ACT');

-- the position is read from the portfolio ("INFORMACION_CARTERA") for the usage RESCFM. The configured instrument, client and portfolio are 0 ("any"): the legacy similarity (CUS-FLU-10, D-CUS-02) raises NullPointerException when they are NULL against a requested one
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('INGTIT', 'INGRESO', 'CUS');
INSERT INTO "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" VALUES ('INGTIT', 'N', 'ING');
INSERT INTO "BSCL.Custodia.BD::CUS.UsoFlujoCustodia" ("IdUsoFlujoCustodia", "IdUsuario", "UsoTipoOperacion", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdCliente", "IdPortafolio", "BusquedaEspecifica", "FechaIngresoRegistro", "UsoValorizacion", "IdEstado", "IdEmpresa") VALUES (20, 1, 'RESCFM', NULL, NULL, 0, 0, 0, NULL, TIMESTAMP '2026-01-01 00:00:00', 'VALCUS', 'ACT', 1);
INSERT INTO "BSCL.Custodia.BD::CUS.ItemsFlujoCustodia" ("IdUsoFlujoCustodia", "ItemCalculo", "ItemOrigen", "Descripcion", "FuncionOperador", "IdEstado") VALUES (20, 'INFORMACION_CARTERA', 'CARTERA', 'Item ficticio', '+', 'ACT');
INSERT INTO "BSCL.Custodia.BD::CUS.Cartera" ("IdCartera", "FolioCartera", "IdCliente", "IdPortafolio", "IdEstado", "IdEmpresa", "IdConcepto", "IdTipoOperacion", "IdMesa", "IdBolsa", "TipoCarteraCorredor", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "IdEmisor", "NemoSVS", "CorteMinimo", "IdMonedaInstrumento", "IdMonedaEmision", "IdTipoReajuste", "FechaEmisionInstrumento", "FechaVencimientoInstrumento", "FechaVencimientoOperacion", "CodigoIFRS", "EsFungible", "IdBoveda", "INGFechaLiquidacion", "INGCantidad", "INGValorRescate", "INGPrecioCompra", "INGTasaCompra", "INGMontoCompraActualMonedaInstrumento", "INGPrecioMercadoActual", "INGTasaMercadoActual", "INGMontoMercadoActualMonedaInstrumento", "IdRegimenFondoMutuo", "FechaOrigenCompra", "FechaOperacion", "FolioComprobante", "FolioExternoReferencia", "IdTipoComprobante", "FechaLiquidacion") VALUES (1, 100001, 100, 500, 'VIG', 1, 'CUS', 'INGTIT', 7, 'BCS', 'CL', 'FM', 'CFM', 900, 'FM001A', 'AGF1', 'SVS900', 1, 'CLP', 'CLP', 'CLP', DATE '2020-01-01', DATE '2099-12-31', DATE '2099-12-31', 'VR', 'S', 'AGF', DATE '2026-02-20', 1000, 0, 1500, 0, 1500000, 1500, 0, 1500000, 'GEN', DATE '2026-02-20', DATE '2026-02-18', 0, '0', 'CMP', DATE '2026-02-20');
