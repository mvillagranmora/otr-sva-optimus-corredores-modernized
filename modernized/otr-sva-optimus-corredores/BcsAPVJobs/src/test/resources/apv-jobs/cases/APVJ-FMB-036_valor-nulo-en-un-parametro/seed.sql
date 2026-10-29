INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" ("IdPrograma", "RutaSalida") VALUES (777, NULL);
INSERT INTO "BSCL.Plataforma.BD::BAT.ParametrosPrograma" ("IdPrograma", "IdSecuencia", "NombreParametro", "Valor") VALUES (777, 1, 'IdEmpresa', '1');
INSERT INTO "BSCL.Plataforma.BD::BAT.ParametrosPrograma" ("IdPrograma", "IdSecuencia", "NombreParametro", "Valor") VALUES (777, 2, 'IdUsuario', NULL);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ("IdSerieFondo", "IdInstrumento", "IdFondo", "IdEmpresa") VALUES ('FONDO-A', 77, 'F1', 1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.Fondo" ("IdFondo", "IdEmpresa", "IdPaisOrigen", "IdPaisEmisor", "IdMoneda") VALUES ('F1', 1, 152, 152, 'CLP');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, 'PLA.PaisSistema', '152');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdRegimenTributario") VALUES (1, 10);
INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo", "IdEstado") VALUES (10, 'BF', 'ACT');
