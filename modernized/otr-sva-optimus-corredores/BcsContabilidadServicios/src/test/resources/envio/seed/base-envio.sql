-- Shared master data for every WP-B case (loaded after schema-envio.sql, before the case seed.sql).
-- All values are synthetic test data; no production data.

-- Accounting types: 10 = detailed format (D), 20 = summarised (R), 30 = detailed but inactive (Estado N).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" VALUES
 (10, 1, 'CART PROPIA - VENTA PACTEADA', 'D', 'V'),
 (20, 1, 'Resumen Diario Tesoreria', 'R', 'V'),
 (30, 1, 'Tipo Inactivo', 'D', 'N');

INSERT INTO "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad" VALUES ('V'), ('N');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" VALUES
 (10, '1101', 1), (10, '1201', 2), (10, '2101', 3), (10, '4101', 4), (10, '5101', 5),
 (20, '1101', 1), (20, '2101', 2), (20, '4101', 3),
 (30, '1101', 1);

-- Accounting type -> centralisation type (210 is the Tesoreria type with the CLP parity rule).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" VALUES
 (10, 200), (20, 210), (30, 230);

-- Optimus account -> SAP account homologation.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" VALUES
 ('1101', 'SAP-1101', 'SYS-1101', '0',     'Banco Estado CLP'),
 ('1201', 'SAP-1201', 'SYS-1201', 'CC-10', 'Clientes por cobrar'),
 ('2101', 'SAP-2101', 'SYS-2101', NULL,    'Proveedores nacionales'),
 ('4101', 'SAP-4101', 'SYS-4101', 'CC-20', 'Ingresos por comisiones de intermediacion de valores mobiliarios'),
 ('5101', 'SAP-5101', 'SYS-5101', 'CC-30', 'Gastos bancarios');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" VALUES
 ('EsContraparte', 'V'), ('EsProveedor', 'V'), ('FolioDTE', 'V'), ('Nemo', 'V'),
 ('MontoCLP', 'N'), ('ProductoMovimiento', 'V'), ('RelacionCorredora', 'V'), ('CodSecuencia', 'V'),
 ('CostingCode', 'V'), ('CostingCode2', 'V'), ('Bpl', 'V'), ('FechaLiq', 'V'), ('Referencia', 'V');

-- SAP field homologation per centralisation type.
--  * MontoMonedaSistema is the system-currency amount used for foreign-currency lines.
--  * BPLId targets a Long field: legacy reflection set fails and is ignored (EnvioContabilidad.java:496-504).
--  * CampoInexistente does not exist on the target: ignored.
--  * ShortName is excluded by the SQL itself (EnvioContabilidad_SelectHomologacionCampos.sql:4).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap" VALUES
 (200, 'U_BCS_Nemo',         'Nemo'),
 (200, 'MontoMonedaSistema', 'MontoCLP'),
 (200, 'BPLId',              'Bpl'),
 (200, 'DueDate',            'FechaLiq'),
 (200, 'CampoInexistente',   'Nemo'),
 (200, 'ShortName',          'Referencia'),
 (200, 'AdditionalReference','Referencia'),
 (210, 'U_BCS_Nemo',         'Nemo'),
 (210, 'MontoMonedaSistema', 'MontoCLP');
