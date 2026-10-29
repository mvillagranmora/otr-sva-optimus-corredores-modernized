-- Base universe of the ConsultarFlujoCustodia oracle. FICTITIOUS data only (no row copied from any real system; no credential).
-- Company 1, country 44, system date PLA.FechaSistema = 2026-03-02 (a Monday). Business calendar of country 44: Saturday (Dia 5) and Sunday
-- (Dia 6) INHABIL, FERIADO on Wednesday 2026-03-04. So, with p_DiaHabil = S: 2026-03-02 +1 = 03-03, +2 = 03-05, +3 = 03-06.
-- Own-account portfolio (FlujoCustodia_SelectPortafoliosCuentaPropia.sql): 9001 (client 300 of the company's business partner).
-- Cases add their usage configuration, items and source rows in their own seed.sql (and may UPDATE the two parameters).

-- ---------------------------------------------------------------- company (ConsultarEmpresa_Select.sql) and own-account portfolio
INSERT INTO "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa", "Codigo", "Nombre", "IdEstado", "IdPais", "IdSocioDeNegocio") VALUES (1, 'BCS', 'Corredora Ficticia S.A.', 'ACT', 44, 100);
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" ("IdSocioDeNegocio", "IdPersona", "IdEmpresa", "IdCliente", "IdContraparte", "Borrado") VALUES (100, 200, 1, 300, NULL, 'N');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdSocioDeNegocio", "IdPersona", "IdCliente", "IdContraparte", "Borrado") VALUES (100, 200, 300, NULL, 'N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona", "Tipo", "RazonSocial", "IdDocumentoIdentidad", "Borrado") VALUES (200, 'J', 'Corredora Ficticia S.A.', 400, 'N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ("IdDocumentoIdentidad", "IdPersona", "DocumentoIdentidad", "Borrado") VALUES (400, 200, '11111111-1', 'N');
INSERT INTO "BSCL.Plataforma.BD::INS.Corredora" ("IdCorredora", "IdPersona") VALUES (50, 200);
INSERT INTO "BSCL.Plataforma.BD::INS.CorredoraCodigos" ("IdCorredora", "IdBolsa", "CodigoCorredora") VALUES (50, 'BCS', '0099');
INSERT INTO "BSCL.Plataforma.BD::REG.Pais" ("IdPais", "CodigoPais", "NombrePais") VALUES (44, 'XX', 'Pais Ficticio');
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio", "IdCliente") VALUES (9001, 300);

-- ---------------------------------------------------------------- parameters (ConfiguracionSistema) and dictionary
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, 'PLA.FechaSistema', '2026-03-02');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, 'PLA.LocalizacionSistema', 'ES');
INSERT INTO "BSCL.Custodia.BD::CUS.Configuracion" ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, 'CUS.AgregarSimultaneasEventoCapitalCli', 'S');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion") VALUES ('SIN_INFORMAR_USO_FLUJO_CUSTODIA', 'ES', 'Texto ficticio del diccionario');

-- ---------------------------------------------------------------- calendar of country 44 (ConsultarFeriados.sql)
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (1, 'INHABIL', NULL, 5, 'SABADO', 2026, 44, 'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (2, 'INHABIL', NULL, 6, 'DOMINGO', 2026, 44, 'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (3, 'FERIADO', DATE '2026-03-04', NULL, 'FERIADO FICTICIO', 2026, 44, 'ACT');
-- country 77: no holiday at all (cases with p_IdPais = 77)

-- ---------------------------------------------------------------- operation types (INS.TipoOperacion: Accion / IdTipoProducto) and their custody configuration
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('COMCN', 'COMPRA', 'RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('VENCN', 'VENTA', 'RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('INGTIT', 'INGRESO', 'CUS');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('RETTIT', 'RETIRO', 'CUS');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('COMSIM', 'COMPRA', 'SIM');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('VENSIM', 'VENTA', 'SIM');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('COMPAC', 'COMPRA', 'PAC');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('VENPAC', 'VENTA', 'PAC');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('PRESTA', 'VENTA', 'PRE');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('EMPRES', 'COMPRA', 'PRE');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('COMRRV', 'COMPRA', 'REP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('VENRRF', 'VENTA', 'REP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('COMTER', 'COMPRA', 'TER');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Accion", "IdTipoProducto") VALUES ('VENTER', 'VENTA', 'TER');
INSERT INTO "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" VALUES ('COMCN', 'N', 'ING');
INSERT INTO "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" VALUES ('VENCN', 'N', 'RET');
INSERT INTO "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" VALUES ('INGTIT', 'N', 'ING');
INSERT INTO "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" VALUES ('RETTIT', 'N', 'RET');
INSERT INTO "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" VALUES ('COMSIM', 'S', 'ING');
INSERT INTO "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" VALUES ('VENSIM', 'S', 'RET');
INSERT INTO "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" VALUES ('COMPAC', 'S', 'ING');
INSERT INTO "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" VALUES ('VENPAC', 'S', 'RET');
INSERT INTO "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" VALUES ('PRESTA', 'S', 'RET');
INSERT INTO "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" VALUES ('EMPRES', 'S', 'ING');

-- ---------------------------------------------------------------- instruments (INS.Instrumento: TipoValorizacion, IdMoneda)
INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento" VALUES (1001, 'PRECIO', 'CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento" VALUES (1002, 'TASA', 'CLF');
INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento" VALUES (1003, 'PRECIO', 'USD');
INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento" VALUES (2001, 'TASA', 'CLP');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" ("IdRegimenFondoMutuo", "Descripcion", "GrupoRegimen", "IdEstado") VALUES ('APV', 'Regimen ficticio', 'GRUPO_APV', 'ACT');

-- ---------------------------------------------------------------- guarantee types (FlujoCustodia_SelectTipoInstrumentoParaGarantia.sql): the only pair is aval ACC # guaranteed SIM
INSERT INTO "BSCL.Plataforma.BD::INS.Bolsa" ("IdBolsa", "IdEstado") VALUES ('BCS', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento", "IdEstado", "IdClasificacionTipoInstrumento") VALUES ('SIM', 'VIG', 'RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento", "IdEstado", "IdClasificacionTipoInstrumento") VALUES ('ACC', 'VIG', 'RV');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" VALUES (11, 'SIM', 'BCS', 'S');
INSERT INTO "BSCL.Custodia.BD::GTA.TipoInstrumentoParaGarantia" VALUES (11, 'ACC', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" VALUES ('ACT', 'Activo');

-- ---------------------------------------------------------------- underlying instruments (FlujoCustodia_SelectInstrumentosFlujo.sql): SIM#ACC#1001 is an underlying
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" VALUES ('SIM', 'BCS', 'ACC');
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" VALUES ('SIM', 'BCS', 'ACC', 1001);
