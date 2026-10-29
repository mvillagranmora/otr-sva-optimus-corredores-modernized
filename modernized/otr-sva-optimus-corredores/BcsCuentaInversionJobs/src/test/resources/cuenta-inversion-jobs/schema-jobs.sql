-- H2 (MODE=LEGACY) schema of the tables and views BcsCuentaInversionJobs reads or writes ITSELF (the services it calls are scripted
-- collaborators and have their own proven tests). INFERRED from the legacy SQL and Java (there is no DDL in the repository for these
-- objects): column types are assumptions, see README "Assumptions". Used by the legacy oracle AND by the modern tests (same file, same H2
-- engine). HANA views are plain tables here.
CREATE ALIAS TO_NVARCHAR FOR "cl.bcs.cuentainversion.jobs.support.H2Hana.toNvarchar";
CREATE ALIAS TO_DECIMAL FOR "cl.bcs.cuentainversion.jobs.support.H2Hana.toDecimal";
CREATE ALIAS HANA_UTCNOW FOR "cl.bcs.cuentainversion.jobs.support.H2Hana.utcNow";
CREATE ALIAS HANA_TODAY FOR "cl.bcs.cuentainversion.jobs.support.H2Hana.today";

CREATE TABLE DUMMY ("DUMMY" VARCHAR(1));

-- the program row of the run; every report job writes its output path / comment here
CREATE TABLE "BSCL.Plataforma.BD::BAT.Programa" ("IdPrograma" BIGINT NOT NULL PRIMARY KEY, "RutaSalida" VARCHAR(1000), "RutaLog" VARCHAR(1000),
  "Comentario" VARCHAR(1000));
-- one row per program type: ActualizarSaldoCargoAbonoJob marks its type as running (EJEC) and frees it (empty text) at the end
CREATE TABLE "BSCL.Plataforma.BD::BAT.TipoProgramaEjecucion" ("IdTipoPrograma" VARCHAR(100) NOT NULL PRIMARY KEY, "IdEstado" VARCHAR(10));

-- the file format of the generic generator (GenerarArchivo / FormatoArchivo): the two views are read with the legacy text SQL
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivo" ("IdEmpresa" BIGINT, "IdEstado" VARCHAR(3), "IdFormatoArchivo" BIGINT,
  "CodigoFormatoArchivo" VARCHAR(100), "FormatoSalida" VARCHAR(10), "Servicio" VARCHAR(200), "SeparadorCampos" VARCHAR(5), "Descripcion" VARCHAR(200),
  "RutaModulo" VARCHAR(200), "RutaCompleta" VARCHAR(500), "NombreArchivoSalida" VARCHAR(100), "CantidadDeTiposDeRegistro" INT,
  "RellenarCampo" VARCHAR(5), "SeparadorDecimales" VARCHAR(5), "AgregoNombreColumna" VARCHAR(5));
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivoDetalle" ("IdFormatoArchivo" BIGINT, "IdFormatoArchivoDetalle" BIGINT NOT NULL
  PRIMARY KEY, "TipoRegistro" INT, "Campo" VARCHAR(100), "TipoDato" VARCHAR(10), "Largo" INT, "Decimales" INT, "ValorDefecto" VARCHAR(100),
  "Orden" INT, "DescripcionFormatoDetalle" VARCHAR(200), "Formato" VARCHAR(50));

-- test-only table: a scripted service writes here (colaboradores.<servicio>.sql) so that commit and rollback are observed in data
CREATE TABLE "TEST.Marca" ("Orden" BIGINT AUTO_INCREMENT PRIMARY KEY, "Valor" VARCHAR(200));
