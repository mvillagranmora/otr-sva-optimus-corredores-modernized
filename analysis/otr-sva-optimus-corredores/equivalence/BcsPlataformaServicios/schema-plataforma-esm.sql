-- H2 schema for the ConsultarEstadoModulo oracle (H2 only, never executed against HANA).
-- Table and column types are the REAL ones read from hana_qa_results/cat_columnas_tablas.csv (SYS.TABLE_COLUMNS catalogue, read only,
-- authorised by the user in PLAN_BLOQUE1 section 11 / Q-03): BIGINT(19,0), NVARCHAR(n), TIMESTAMP(27,7); NOT NULL exactly as IS_NULLABLE.
-- Assumptions (documented, cannot be checked without a new catalogue query): PAU.Modulo has NO primary key/unique constraint here because
-- the catalogue extract holds no constraints (probably PK (IdEmpresa, CodigoModulo) in HANA: the duplicates case is therefore hypothetical).
-- HANA NVARCHAR(n) counts characters, as H2 VARCHAR(n) does.
CREATE TABLE "BSCL.Plataforma.BD::PAU.Modulo" (
  "IdEmpresa" BIGINT NOT NULL,
  "CodigoModulo" NVARCHAR(40) NOT NULL,
  "IdEstado" NVARCHAR(3) NOT NULL,
  "CodigoModuloDependenciaCierre1" NVARCHAR(40) NOT NULL,
  "CodigoModuloDependenciaCierre2" NVARCHAR(40) NOT NULL,
  "CodigoModuloDependenciaCierre3" NVARCHAR(40) NOT NULL,
  "CodigoModuloDependenciaCierre4" NVARCHAR(40) NOT NULL,
  "CodigoModuloDependenciaCierre5" NVARCHAR(40) NOT NULL,
  "CodigoUsuario" NVARCHAR(40) NOT NULL,
  "FechaUltimaActualizacion" TIMESTAMP(7) NOT NULL,
  "CodigoModuloDependenciaCierre9" NVARCHAR(40),
  "CodigoModuloDependenciaCierre6" NVARCHAR(40),
  "CodigoModuloDependenciaCierre8" NVARCHAR(40),
  "CodigoModuloDependenciaCierre7" NVARCHAR(40),
  "CodigoModuloDependenciaCierre14" NVARCHAR(40),
  "CodigoModuloDependenciaCierre15" NVARCHAR(40),
  "CodigoModuloDependenciaCierre12" NVARCHAR(40),
  "CodigoModuloDependenciaCierre13" NVARCHAR(40),
  "CodigoModuloDependenciaCierre10" NVARCHAR(40),
  "CodigoModuloDependenciaCierre11" NVARCHAR(40)
);
