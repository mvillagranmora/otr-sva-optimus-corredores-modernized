INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo", "IdEstado") VALUES (1, 'APV-A', 'VIG');
INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo", "IdEstado") VALUES (2, 'APV-B', 'VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdRegimenTributario") VALUES (1, 1);
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdRegimenTributario") VALUES (2, 2);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio", "IdMatrizAgrupadorAPV", "IdEstado", "IdCliente") VALUES (901, 1, 'ACT', 701);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio", "IdMatrizAgrupadorAPV", "IdEstado", "IdCliente") VALUES (902, 2, 'ACT', 701);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio", "IdMatrizAgrupadorAPV", "IdEstado", "IdCliente") VALUES (903, 1, 'INA', 701);
INSERT INTO "BSCL.APV.BD::INV.Bonificacion" ("IdBonificacion", "IdCliente", "AnoAhorro", "IdEmpresa", "IdEstado", "MontoReajustadoPagadoAfiliado", "MontoBonificacionAfiliadoNeto", "MontoCalculado") VALUES (1, 701, 2024, 1, 'ING', 70000, 1, 0);
INSERT INTO "BSCL.Normativa.BD.Vistas::TRI.DJ1871.Datos" ("ID", "IdCliente", "AnoAhorro", "AhorrosDeposito", "RetirosDeposito", "RutCliente") VALUES (701, 701, 2024, 100000, 100000, '1701-9');
