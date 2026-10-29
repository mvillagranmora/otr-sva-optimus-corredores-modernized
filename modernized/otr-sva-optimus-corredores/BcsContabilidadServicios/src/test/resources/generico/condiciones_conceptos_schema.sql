-- H2 stand-in for the two HANA tables read by
-- ConsultarCondicionesConceptosNormativos_SelectConceptosConfiguraciones.sql.
-- Column names/types follow the legacy query and the pojo
-- ConsultarCondicionesConceptosNormativos.CondicionesConceptosNormativosSQL.
CREATE TABLE "BSCL.Contabilidad.BD::ITZ.ConceptoNormativo" (
    "IdConceptoNormativo" BIGINT PRIMARY KEY,
    "ConceptoNormativo"   VARCHAR(200),
    "IdEstado"            VARCHAR(10)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.CondicionConceptoNormativo" (
    "IdCondicionConceptoNormativo" BIGINT PRIMARY KEY,
    "IdConceptoNormativo"          BIGINT,
    "IdTipoDatoEvento"             VARCHAR(100),
    "ValorVarchar"                 VARCHAR(200),
    "OperadorFuncion"              VARCHAR(50)
);
