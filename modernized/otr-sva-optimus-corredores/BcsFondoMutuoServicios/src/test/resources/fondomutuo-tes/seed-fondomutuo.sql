-- F2 W1 (joint rollback, TesoreriaRollbackConjuntoTest): on top of schema-tesoreria-a.sql + base-tesoreria-a.sql (copies of BcsTesoreriaServicios
-- src/test/resources/tesoreria-a, synthetic data), the two objects that ProcesarIngresoEgresoTesoreria reads and the Tesoreria schema does not have
-- (ASSUMED types, as the oracle cases FMM-NOR-87..96), and the master data of the movement the helper builds (concept / product / operation APORFM, INFM).
CREATE TABLE "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" ("IdPreferenciaPagoPortafolio" BIGINT, "IdCuentaBancaria" BIGINT, "IdBancoCuentaBancaria" BIGINT, "NumeroCuentaBancaria" VARCHAR(100), "IdTipoCuentaBancaria" VARCHAR(4), "NombreTipoCuentaBancaria" VARCHAR(100));
CREATE TABLE "BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor" ("IdTipoMovimiento" VARCHAR(6), "IdConcepto" VARCHAR(6), "IdMedioDePago" VARCHAR(3), "IdCuentaBanco" BIGINT, "IdMoneda" VARCHAR(3), "NombreEntidad" VARCHAR(100), "IdEstado" VARCHAR(3), "IdEmpresa" BIGINT);
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" VALUES (9001, 401, 1, '0002-0002', 'CTC', 'Cuenta corriente');
INSERT INTO "BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor" VALUES ('MOVEGR', 'RESCFM', 'TRA', 301, 'CLP', 'FFM.RescatePorTraspaso', 'ACT', 1);
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","Nombre") VALUES ('APORFM','Aporte fondo mutuo');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConcepto","GeneraComprobanteAutomaticamente","NoValidaSaldoCaja") VALUES ('APORFM','N','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ("IdTipoProducto") VALUES ('INFM');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion") VALUES ('APORFM');
-- the base seed has several rows per payment medium (Tesoreria accepts any of them); the legacy helper maps them with Collectors.toMap (D-TES-2: a
-- repeated medium throws, pinned by FMM-NOR-91), so this database keeps one modality per medium
DELETE FROM "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago";
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdMedioDePago","IdModalidadDePago","IdEstado") VALUES ('TRA','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdMedioDePago","IdModalidadDePago","IdEstado") VALUES ('CHE','MODCAJ','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdMedioDePago","IdModalidadDePago","IdEstado") VALUES ('EFE','MODCAJ','ACT');
