-- case seed on top of seed/base-fmm.sql + schema-ffmm.sql + seed/base-ffmm.sql (fake data)
CREATE LOCAL TEMPORARY TABLE "tmp_serie" AS SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" WHERE "IdSerieFondo" = 'FM001A';
UPDATE "tmp_serie" SET "IdInstrumento" = 900, "IdSerieFondo" = 'FM099Z', "IdFondo" = '10' WHERE 1 = 1;
INSERT INTO "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SELECT * FROM "tmp_serie";
DROP TABLE "tmp_serie";
INSERT INTO "BSCL.Custodia.BD::CUS.Cartera" ("IdCartera", "FolioCartera", "IdEmpresa", "IdCliente", "IdPortafolio", "IdEstado", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "IdEmisor", "IdBoveda", "IdRegimenFondoMutuo", "PrecioCompra", "INGFechaOperacion", "INGFechaLiquidacion", "INGCantidad", "INGMontoATasaCompraActual", "INGMontoCompraActualMonedaInstrumento", "EGRFechaOperacion", "EGRFechaLiquidacion", "EGRMonto") VALUES (1, 1, 1, 100, 500, 'VIG', 'FM', 'FFM', 900, 'FM001A', 'AGF1', 'COR', 'GEN', 9800, DATE '2026-01-10', DATE '2026-01-12', 100, 990000, 980000, DATE '0001-01-01', DATE '0001-01-01', 0);
