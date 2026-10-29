-- Financial indexes and the asset classification tree (attribution analysis). Synthetic.
-- IPSA has values on 2025-03-10 (100) and 2025-03-14 (110.5); IGPA only on 2025-03-12; IVSV is referenced by the tree but has no values.
INSERT INTO "BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero" VALUES ('IPSA', 'D', 'Indice IPSA', 'Indice selectivo', 'CLP', 'CLP');
INSERT INTO "BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero" VALUES ('IGPA', 'D', 'Indice IGPA', NULL, 'CLP', 'CLP');
INSERT INTO "BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero" VALUES ('IVSV', 'M', 'Indice sin valores', 'Sin valores', 'CLP', 'USD');
INSERT INTO "BSCL.Plataforma.BD::MER.IndiceFinanciero" VALUES ('IPSA', DATE '2025-03-10', 100);
INSERT INTO "BSCL.Plataforma.BD::MER.IndiceFinanciero" VALUES ('IPSA', DATE '2025-03-12', 105);
INSERT INTO "BSCL.Plataforma.BD::MER.IndiceFinanciero" VALUES ('IPSA', DATE '2025-03-14', 110.5);
INSERT INTO "BSCL.Plataforma.BD::MER.IndiceFinanciero" VALUES ('IGPA', DATE '2025-03-12', 2000);
INSERT INTO "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" VALUES ('USD');
INSERT INTO "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" VALUES ('UF');
INSERT INTO "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" VALUES (1045, 0, 'Renta Variable Nacional', 'IPSA');
INSERT INTO "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" VALUES (1046, 1045, 'Acciones Generales', 'IGPA');
INSERT INTO "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" VALUES (1047, 0, 'Otros sin indice', '');
INSERT INTO "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" VALUES (1048, 0, 'Indice sin valores', 'IVSV');
