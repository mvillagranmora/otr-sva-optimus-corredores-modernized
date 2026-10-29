-- H2 ONLY (tests of the kit, FM-KIT-nn). Minimal tables/views with the columns the verbatim Plataforma kit SQL touch; types are the real ones of the
-- HANA catalog exports (hana_qa_results/supuestos_columnas_*.csv, cat_*.csv). Never executed on HANA; the module code issues no DDL.
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.Parametro" ("IdEmpresa" BIGINT NOT NULL, "NombreTipoParametro" NVARCHAR(255) NOT NULL, "ValorParametro" NVARCHAR(255) NOT NULL);
CREATE TABLE "BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa" BIGINT NOT NULL, "CodigoParametro" NVARCHAR(100) NOT NULL, "Valor" NVARCHAR(255));
CREATE TABLE "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje" NVARCHAR(40) NOT NULL, "CodDiccionario" NVARCHAR(100) NOT NULL, "Descripcion" NVARCHAR(520) NOT NULL, PRIMARY KEY ("CodDiccionario", "CodLenguaje"));
CREATE TABLE "BSCL.Plataforma.BD::PLA.Modulo" ("IdModulo" NVARCHAR(3) NOT NULL PRIMARY KEY);
CREATE TABLE "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario" BIGINT NOT NULL, "IdEmpresa" BIGINT NOT NULL, "IdEstado" NVARCHAR(3));
CREATE TABLE "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa" BIGINT NOT NULL PRIMARY KEY, "Nombre" NVARCHAR(100), "IdEstado" NVARCHAR(3), "IdPais" BIGINT, "IdSocioDeNegocio" BIGINT);
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" ("IdSocioDeNegocio" BIGINT NOT NULL, "IdPersona" BIGINT NOT NULL, "IdEmpresa" BIGINT NOT NULL, "IdCliente" BIGINT, "IdContraparte" BIGINT);
CREATE TABLE "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado" BIGINT NOT NULL, "Tipo" NVARCHAR(3), "Fecha" DATE, "Dia" INTEGER, "DescripcionDia" NVARCHAR(100), "AgnoCarga" INTEGER, "IdPais" BIGINT NOT NULL, "IdEstado" NVARCHAR(3));
CREATE TABLE "BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato" BIGINT NOT NULL PRIMARY KEY, "IdMoneda" NVARCHAR(3) NOT NULL, "IdUsoMonedaFormato" NVARCHAR(3) NOT NULL, "IdTipoInstrumento" NVARCHAR(3) NOT NULL, "IdTipoOperacion" NVARCHAR(6) NOT NULL,
  "PrecioDecimales" INTEGER NOT NULL, "PrecioTrucaRedondea" NVARCHAR(1) NOT NULL, "TasaDecimales" INTEGER NOT NULL, "TasaTrucaRedondea" NVARCHAR(1) NOT NULL, "CantidadDecimales" INTEGER NOT NULL, "CantidadTrucaRedondea" NVARCHAR(1) NOT NULL,
  "MontoDecimales" INTEGER NOT NULL, "MontoTrucaRedondea" NVARCHAR(1) NOT NULL, "TipoCambioDecimales" INTEGER NOT NULL, "TipoCambioTrucaRedondea" NVARCHAR(1) NOT NULL, "ParidadDecimales" INTEGER NOT NULL, "ParidadTrucaRedondea" NVARCHAR(1) NOT NULL,
  "PuntosDecimales" INTEGER NOT NULL, "PuntosTrucaRedondea" NVARCHAR(1) NOT NULL, "IdEstado" NVARCHAR(3) NOT NULL);
-- tables read by ConsultarEmpresa_Select.sql
CREATE TABLE "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona" BIGINT NOT NULL PRIMARY KEY, "FechaIngreso" TIMESTAMP, "FechaCreacion" TIMESTAMP, "FechaModificacion" TIMESTAMP, "IdDocumentoIdentidad" BIGINT, "Tipo" NVARCHAR(1), "RazonSocial" NVARCHAR(100),
  "Nombres" NVARCHAR(100), "ApellidoPaterno" NVARCHAR(100), "ApellidoMaterno" NVARCHAR(100), "IdPersonaRepresentante" BIGINT, "IdDireccionElectronicaPrincipal" BIGINT, "IdDireccionFisicaPrincipal" BIGINT, "IdTelefonoPrincipal" BIGINT);
CREATE TABLE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ("IdDocumentoIdentidad" BIGINT NOT NULL PRIMARY KEY, "DocumentoIdentidad" NVARCHAR(40));
CREATE TABLE "BSCL.SocioNegocio.BD::PER.DireccionElectronica" ("IdDireccionElectronica" BIGINT NOT NULL PRIMARY KEY, "Direccion" NVARCHAR(255));
CREATE TABLE "BSCL.SocioNegocio.BD::PER.DireccionFisica" ("IdDireccionFisica" BIGINT NOT NULL PRIMARY KEY, "Calle" NVARCHAR(100), "Numero" NVARCHAR(20), "Departamento" NVARCHAR(20), "Sector" NVARCHAR(100), "IdCiudad" BIGINT, "IdDivisionPais" BIGINT);
CREATE TABLE "BSCL.Plataforma.BD::REG.Ciudad" ("IdCiudad" BIGINT NOT NULL PRIMARY KEY, "NombreCiudad" NVARCHAR(100));
CREATE TABLE "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais" BIGINT NOT NULL PRIMARY KEY, "NombreDivisionPais" NVARCHAR(100));
CREATE TABLE "BSCL.SocioNegocio.BD::PER.Telefono" ("IdTelefono" BIGINT NOT NULL PRIMARY KEY, "Numero" NVARCHAR(40));
CREATE TABLE "BSCL.Plataforma.BD::INS.Corredora" ("IdCorredora" BIGINT NOT NULL PRIMARY KEY, "IdPersona" BIGINT);
CREATE TABLE "BSCL.Plataforma.BD::REG.Pais" ("IdPais" BIGINT NOT NULL PRIMARY KEY, "CodigoPais" NVARCHAR(40), "NombrePais" NVARCHAR(100));
CREATE TABLE "BSCL.SocioNegocio.BD::CNT.Contraparte" ("IdContraparte" BIGINT NOT NULL PRIMARY KEY, "CodigoInscripcion" NVARCHAR(40));
CREATE TABLE "BSCL.Plataforma.BD::INS.CorredoraCodigos" ("IdCorredora" BIGINT NOT NULL, "IdBolsa" NVARCHAR(10) NOT NULL, "CodigoCorredora" NVARCHAR(40));
-- HANA's DUMMY with CURRENT_UTCTIMESTAMP as a column (the clock of FechaDB: fixed here)
CREATE VIEW DUMMY AS SELECT TIMESTAMP '2026-03-02 14:30:00' AS CURRENT_UTCTIMESTAMP;
