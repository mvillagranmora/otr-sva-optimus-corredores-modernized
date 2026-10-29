INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" ("IdPrograma", "RutaSalida") VALUES (777, NULL);
INSERT INTO "BSCL.Plataforma.BD::BAT.ParametrosPrograma" ("IdPrograma", "IdSecuencia", "NombreParametro", "Valor") VALUES (777, 1, 'IdEmpresa', '1');
INSERT INTO "BSCL.Plataforma.BD::BAT.ParametrosPrograma" ("IdPrograma", "IdSecuencia", "NombreParametro", "Valor") VALUES (777, 2, 'IdOrden', 'x');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdRegimenTributario") VALUES (1, 10);
INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo", "IdEstado") VALUES (10, 'BF', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "AccionOrden") VALUES ('COMACN', 'COMPRA');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "AccionOrden") VALUES ('VENACN', 'VENTA');
