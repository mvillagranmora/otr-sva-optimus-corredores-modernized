-- case seed on top of seed/base-fmm.sql + seed/base-vdo.sql (fake data)
INSERT INTO "BSCL.FondoMutuo.BD::FMM.Parametro" ("IdEmpresa", "NombreTipoParametro", "ValorParametro") VALUES (2, 'UsaDolarObservado', 'N');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa", "CodigoParametro", "Valor") VALUES (2, 'PLA.MonedaSistema', 'CLP');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa", "CodigoParametro", "Valor") VALUES (2, 'PLA.LocalizacionSistema', 'es');
