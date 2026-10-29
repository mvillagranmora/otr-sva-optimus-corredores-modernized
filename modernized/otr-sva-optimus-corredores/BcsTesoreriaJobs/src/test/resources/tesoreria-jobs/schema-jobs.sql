-- H2 (MODE=LEGACY) schema of the tables and views BcsTesoreriaJobs reads or writes. INFERRED from the legacy SQL and Java (there is no
-- DDL in the repository for these objects): column types are assumptions, see README "Assumptions". Used by the legacy oracle AND by the
-- modern tests (same file, same H2 engine). HANA views are plain tables here.
CREATE ALIAS TO_NVARCHAR FOR "cl.bcs.tesoreria.jobs.support.H2Hana.toNvarchar";
CREATE ALIAS TO_DECIMAL FOR "cl.bcs.tesoreria.jobs.support.H2Hana.toDecimal";
CREATE ALIAS HANA_UTCNOW FOR "cl.bcs.tesoreria.jobs.support.H2Hana.utcNow";
CREATE ALIAS HANA_TODAY FOR "cl.bcs.tesoreria.jobs.support.H2Hana.today";

CREATE TABLE DUMMY ("DUMMY" VARCHAR(1));

CREATE TABLE "BSCL.Plataforma.BD::BAT.Programa" ("IdPrograma" BIGINT NOT NULL PRIMARY KEY, "RutaSalida" VARCHAR(1000));
CREATE TABLE "BSCL.Plataforma.BD::BAT.RangoHorario" ("TipoProposito" VARCHAR(100), "HoraInicio" TIME, "HoraFin" TIME);
CREATE TABLE "BSCL.Plataforma.BD::PAU.Ejecucion" ("FechaSistema" DATE, "IdEmpresa" BIGINT, "Estado" VARCHAR(3));
CREATE TABLE "BSCL.Plataforma.BD::FTP.Cabecera" ("IdCabecera" BIGINT NOT NULL PRIMARY KEY, "IdCodigoTipo" VARCHAR(50), "IdEstado" VARCHAR(3),
  "FechaCreacion" TIMESTAMP, "IdUsuario" BIGINT, "NombreArchivo" VARCHAR(255));
CREATE TABLE "BSCL.Plataforma.BD::FTP.Detalle" ("IdDetalle" BIGINT NOT NULL PRIMARY KEY, "IdCabecera" BIGINT, "Linea" VARCHAR(4000),
  "IdTipoEntidad" VARCHAR(30), "IdEntidad" BIGINT);

CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo" ("IdEmpresa" BIGINT, "IdBanco" BIGINT, "IdMoneda" VARCHAR(10), "IdMedioDePago" VARCHAR(10),
  "IdEstado" VARCHAR(3), "IdFormatoArchivo" BIGINT, "CodigoFormatoArchivo" VARCHAR(50), "SeparadorCampos" VARCHAR(5), "SeparadorDecimales" VARCHAR(5),
  "RellenarCampo" VARCHAR(5), "NombreBanco" VARCHAR(100), "FormatoSalida" VARCHAR(10), "Servicio" VARCHAR(200), "Descripcion" VARCHAR(200),
  "RutaTesoreria" VARCHAR(200), "RutaCompleta" VARCHAR(500), "NombreArchivoSalida" VARCHAR(100), "CantidadDeTiposDeRegistro" INT, "TipoSaltoLinea" VARCHAR(5));
CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivoDetalle" ("IdFormatoArchivo" BIGINT, "CodigoFormatoArchivo" VARCHAR(50),
  "IdFormatoArchivoDetalle" BIGINT, "TipoRegistro" INT, "Campo" VARCHAR(100), "TipoDato" VARCHAR(10), "Largo" INT, "Decimales" INT,
  "ValorDefecto" VARCHAR(100), "Orden" INT, "DescripcionFormatoDetalle" VARCHAR(200), "Formato" VARCHAR(50));

CREATE TABLE "BSCL.Tesoreria.BD::TES.NominaTransferencia" ("IdNominaTransferencia" BIGINT NOT NULL PRIMARY KEY, "IdEmpresa" BIGINT, "NombreArchivo" VARCHAR(255));
-- ASSUMPTION: the BancoEstado code strips 9 characters from the text of MontoRemesa (".00000000": scale 8); NUMERIC(28,8); a case may ALTER the column to another scale
CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" ("IdNominaTransferencia" BIGINT, "IdEmpresa" BIGINT, "IdMedioDePago" VARCHAR(10),
  "Monto" NUMERIC(28,8), "IdMoneda" VARCHAR(10), "NombreBanco" VARCHAR(100), "NumeroCuentaBancaria" VARCHAR(50), "FechaLiquidacion" DATE);
CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" ("IdNominaTransferencia" BIGINT, "IdComprobante" BIGINT, "IdEstado" VARCHAR(3),
  "IdBancoCorredora" BIGINT, "IdBancoSocio" BIGINT, "DocumentoIdentidad" VARCHAR(20), "NombrePersona" VARCHAR(200), "Monto" NUMERIC(28,8),
  "NumeroCuentaBancariaSocio" VARCHAR(50), "NombreBancoSocio" VARCHAR(100), "CuentaDCVDestino" VARCHAR(30), "CuentaDCVOrigen" VARCHAR(30), "ClaveDCV" VARCHAR(30));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" ("NombreBanco" VARCHAR(100), "CodigoIso" VARCHAR(20));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" ("IdEmpresa" BIGINT, "RUT" VARCHAR(20), "RazonSocial" VARCHAR(200));
