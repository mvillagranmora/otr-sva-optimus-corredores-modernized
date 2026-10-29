-- File generation settings shared by the file/report cases. ${OUT} is replaced by the run's output directory (oracle: scratch; tests: temp dir).
-- Files are written to <PLA.RutaBaseArchivos><RutaArchivosCta><name>; the case creates the "CTA" sub directory (case.json "directorios").
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" VALUES (1, 'PLA.RutaBaseArchivos', '${OUT}/');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion" VALUES (1, 'RutaArchivosCta', 'CTA/');
