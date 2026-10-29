-- Reference data shared by every transaccional case (synthetic; no production rows).
-- Case-specific rows go to cases/<case>/seed.sql.
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" ("IdCuentaInversion","IdEmpresa","IdCliente","IdPortafolio","IdMoneda","IdEstado","Codigo","CuentaInversionPrincipal")
VALUES (7001, 1, 9001, 8001, 'CLP', 'ACT', 'CI-7001', 'S');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" ("IdCuentaInversion","IdEmpresa","IdCliente","IdPortafolio","IdMoneda","IdEstado","Codigo","CuentaInversionPrincipal")
VALUES (7002, 1, 9001, 8001, 'USD', 'ACT', 'CI-7002', 'N');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" ("IdCuentaInversion","IdEmpresa","IdCliente","IdPortafolio","IdMoneda","IdEstado","Codigo","CuentaInversionPrincipal")
VALUES (7003, 1, 9002, 8002, 'CLP', 'ACT', 'CI-7003', 'S');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" ("IdCuentaInversion","IdEmpresa","IdCliente","IdPortafolio","IdMoneda","IdEstado","Codigo","CuentaInversionPrincipal")
VALUES (7004, 1, 9002, 8003, 'CLP', 'INA', 'CI-7004', 'N');

-- Plataforma / Portafolio / SocioNegocio reference rows (synthetic, fake RUTs, fake e-mail domain example.invalid)
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" VALUES ('T0', 0);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" VALUES ('T1', 1);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" VALUES ('T2', 2);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" VALUES ('T3', 3);

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoMovimiento" VALUES ('CTA', 'MOVCAR', 'Cargo');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoMovimiento" VALUES ('CTA', 'MOVABN', 'Abono');

INSERT INTO "BSCL.Plataforma.BD::PLA.MonedaFormato" VALUES (1, 'CLP', 'GEN', '', '', 4, 'R', 4, 'R', 4, 'R', 0, 'R', 2, 'R', 4, 'R', 2, 'R', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.MonedaFormato" VALUES (2, 'USD', 'GEN', '', '', 4, 'R', 4, 'R', 4, 'R', 2, 'R', 2, 'R', 4, 'R', 2, 'R', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.MonedaFormato" VALUES (3, 'UF',  'GEN', '', '', 4, 'R', 4, 'R', 4, 'R', 4, 'T', 2, 'R', 4, 'R', 2, 'R', 'ACT');

INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" VALUES ('AACFIR', 'Abono cierre FIR');
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" VALUES ('CCACFI', 'Cargo cierre FIR');
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" VALUES ('CMBPTF', 'Cambio de portafolio');

INSERT INTO "BSCL.Plataforma.BD::PLA.Empresa" VALUES (1, 5001, 44);
INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" VALUES (5001, 9002);

INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" VALUES (1, '11111111-1', 'RUT');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" VALUES (2, '22222222-2', 'RUT');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" VALUES (3, '33333333-3', 'RUT');
INSERT INTO "BSCL.SocioNegocio.BD::PER.Telefono" VALUES (1, '+56 2 2000 0001');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DireccionElectronica" VALUES (1, 'cliente1@example.invalid', 'N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DireccionElectronica" VALUES (2, 'ejecutivo@example.invalid', 'N');

-- persona natural (cliente 9001), persona juridica (cliente 9002 = la corredora / empresa 1), empleado 6001
INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona" VALUES (101, 'N', 'Juan', 'Perez', 'Soto', NULL, 1, 1, 1);
INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona" VALUES (102, 'J', NULL, NULL, NULL, 'Empresa Ejemplo SA', 2, NULL, NULL);
INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona" VALUES (103, 'N', 'Ana', 'Lopez', 'Rojas', NULL, 3, 2, NULL);
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" VALUES (9001, 101, 'PER');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" VALUES (9002, 102, 'EMP');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.Empleado" VALUES (6001, 103, 'EJ-6001', 1);
INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" VALUES (97, 6001, 'EMP');
INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" VALUES (96, 9001, 'CLI');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" VALUES (6001, 103, 2);
INSERT INTO "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" VALUES (1, 'Corredora Ejemplo SA', 'contacto@example.invalid', '77777777-7', '+56 2 2999 9999');

INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (8001, 9001, 'POR-8001', 6001, 'Portafolio Juan Perez', 'Ahorro', 0);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (8002, 9002, 'POR-8002', 6001, 'Portafolio Corredora', 'Propio', 0);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (8003, 9002, 'POR-8003', 6001, 'Portafolio Corredora 2', 'Propio', 0);

-- AnularCargoAbono / CrearCargoAbono reference rows
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultaConfiguracionCuentaInversion" VALUES (1, 'S', 'N');
INSERT INTO "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" VALUES ('ANUCAR', 'S');
INSERT INTO "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" VALUES ('ANUABO', 'S');

INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" VALUES ('AACFIR');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" VALUES ('CCACFI');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" VALUES ('CMBPTF');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" VALUES ('AAINST');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" VALUES ('ABNCSC');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" VALUES ('RET104');
INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden" VALUES ('AAINST', 'INGRESOINSTRUCCION');
INSERT INTO "BSCL.Orden.BD::ORD.ConceptoOrden" VALUES ('AACFIR', 'OTROUSO');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" VALUES ('COMPRA');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" VALUES ('VENTA');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" VALUES ('RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" VALUES ('RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" VALUES ('DPF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" VALUES ('FFM');

INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicadoresPortafolioCuentaInversion" VALUES (8001, 'N', 'S');
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicadoresPortafolioCuentaInversion" VALUES (8002, 'N', 'S');
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria" VALUES ('CLP', 'ACT');
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria" VALUES ('USD', 'ACT');
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria" VALUES ('EUR', 'INA');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" VALUES (9001, 101, 'PER', 'Juan Perez Soto', '11111111-1');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" VALUES (9002, 102, 'EMP', 'Empresa Ejemplo SA', '22222222-2');
INSERT INTO "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" VALUES (102, 'Empresa Ejemplo SA');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" VALUES (101, 'Juan Perez Soto', '11111111-1');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" VALUES (102, 'Empresa Ejemplo SA', '22222222-2');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" VALUES (103, 'Ana Lopez Rojas', '33333333-3');
INSERT INTO "BSCL.Plataforma.BD::IND.TipoIndicador" VALUES (1, 'CLIENTE', 'BCCUM', 'ACT');

-- non business days of country 44: Saturday (5) and Sunday (6) as INHABIL (Optimus numbers Monday = 0); no FERIADO rows in the base seed
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" VALUES (1, 'INHABIL', NULL, 5, 'Sabado', NULL, 44, 'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" VALUES (2, 'INHABIL', NULL, 6, 'Domingo', NULL, 44, 'ACT');

-- ConsultarSaldoCuentaInversion reference rows
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" VALUES ('CLP', 'CLP', 'Peso chileno');
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" VALUES ('USD', 'USD', 'Dolar');
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" VALUES ('EUR', 'EUR', 'Euro');
INSERT INTO "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" VALUES (1, 'SUC1', 'Sucursal Centro');
INSERT INTO "BSCL.SocioNegocio.BD::EJE.Ejecutivo" VALUES (6001);
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" VALUES (102, 9002, '22222222-2');

-- carga masiva: tipos de carga
INSERT INTO "BSCL.CuentaInversion.BD::CAR.TipoCargaMasiva" VALUES (1, 'CARGA_ABONO', 'Carga de abonos', 1, 'ACT', 'AACFIR');
INSERT INTO "BSCL.CuentaInversion.BD::CAR.TipoCargaMasiva" VALUES (2, 'CARGA_CARGO', 'Carga de cargos', 1, 'ACT', 'CCACFI');
