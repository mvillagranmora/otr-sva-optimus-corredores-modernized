-- Harness objects of the ConsultarFlujoCustodia oracle (H2 only; identical for the legacy and the new implementation).
-- HANA functions without an H2 built-in (TO_BIGINT) or whose H2 built-in differs (TO_DATE, TO_DECIMAL: renamed by HanaH2.translate).
CREATE ALIAS TO_BIGINT FOR 'cl.bcs.custodia.flujo.h2.HanaFunciones.toBigint';
CREATE ALIAS HANA_TO_DATE FOR 'cl.bcs.custodia.flujo.h2.HanaFunciones.toDate';
CREATE ALIAS HANA_TO_DECIMAL FOR 'cl.bcs.custodia.flujo.h2.HanaFunciones.toDecimal';
CREATE TABLE DUMMY (DUMMY VARCHAR(1));
INSERT INTO DUMMY VALUES ('X');

-- witness of CUS.FlujoCustodia_MatrizUso (CUS-FLU-14): every inserted / deleted row, in firing order (MatrizUsoAuditoria.java)
CREATE SEQUENCE "ORACLE.MatrizUsoAuditoriaSeq" START WITH 1;
CREATE TABLE "ORACLE.MatrizUsoAuditoria" (
    "Orden" BIGINT,
    "Operacion" VARCHAR(1),
    "IdCliente" BIGINT,
    "IdPortafolio" BIGINT,
    "UsoTipoOperacion" NVARCHAR(6),
    "IdClasificacionTipoInstrumento" NVARCHAR(3),
    "IdTipoInstrumento" NVARCHAR(3),
    "IdInstrumento" BIGINT,
    "IdEmisor" NVARCHAR(40),
    "FechaVencimiento" DATE,
    "IdTipoReajuste" NVARCHAR(3),
    "EsFungible" NVARCHAR(1),
    "IdBoveda" NVARCHAR(3),
    "IdTipoInstrumentoEspecifico" NVARCHAR(3),
    "IdUsoFlujoCustodia" BIGINT,
    "BusquedaEspecifica" NVARCHAR(40),
    "IdEstado" NVARCHAR(3),
    "IdEmpresa" BIGINT,
    "IdRegimenFondoMutuo" NVARCHAR(40),
    "IdTrabajo" BIGINT
);
CREATE TRIGGER "ORACLE_MZU_INS" AFTER INSERT ON "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" FOR EACH ROW CALL 'cl.bcs.custodia.flujo.h2.MatrizUsoAuditoria';
CREATE TRIGGER "ORACLE_MZU_DEL" AFTER DELETE ON "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" FOR EACH ROW CALL 'cl.bcs.custodia.flujo.h2.MatrizUsoAuditoria';
