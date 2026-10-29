-- H2 schema of the login tests: ONLY the columns the login path reads or writes, with the types of the legacy pojos (Long, Integer, Date, Timestamp).
-- Fictitious structure for tests; it is not an export of the HANA catalog.
CREATE ALIAS ADD_SECONDS FOR 'cl.bcs.plataforma.login.h2.HanaFunciones.addSeconds';
CREATE ALIAS TO_INT FOR 'cl.bcs.plataforma.login.h2.HanaFunciones.toInt';
CREATE TABLE DUMMY (DUMMY VARCHAR(1));
INSERT INTO DUMMY VALUES ('X');

CREATE TABLE "BSCL.Plataforma.BD::ACC.Usuario" (
    "IdUsuario" BIGINT PRIMARY KEY,
    "IdEmpresa" BIGINT,
    "CodigoUsuario" NVARCHAR(40),
    "Clave" NVARCHAR(100),
    "ClaveProvisoria" NVARCHAR(100) DEFAULT '',
    "FechaClaveProvisoria" TIMESTAMP DEFAULT TIMESTAMP '0001-01-01 00:00:00',
    "IntentosConexionClaveProvisoria" INTEGER DEFAULT 0,
    "FechaVigencia" DATE,
    "IdEstado" NVARCHAR(3),
    "IntentosConexion" INTEGER,
    "FechaBloqueoTemporal" TIMESTAMP,
    "EsAutorizado" NVARCHAR(1),
    "TipoEntidad" NVARCHAR(3),
    "IdEntidad" BIGINT,
    "UsuarioClientes" NVARCHAR(1),
    "UsuarioNegocios" NVARCHAR(1),
    "UsuarioServicios" NVARCHAR(1),
    "UsuarioCorredores" NVARCHAR(1),
    "CodigoMenu" NVARCHAR(40),
    "FechaIngreso" DATE,
    "DebeCambiarClave" NVARCHAR(1) DEFAULT 'N',
    "UltimoCambioClave" DATE,
    "FechaUltimaConexion" TIMESTAMP,
    "Conectado" NVARCHAR(1) DEFAULT 'N',
    "Hora" TIMESTAMP
);

CREATE TABLE "BSCL.Plataforma.BD::ACC.Sesion" (
    "IdTokenSesion" NVARCHAR(1000) PRIMARY KEY,
    "IdUsuario" BIGINT,
    "TokenValido" NVARCHAR(1),
    "FechaUltimoUso" TIMESTAMP,
    "FechaIngreso" TIMESTAMP,
    "IdCanal" NVARCHAR(3),
    "IdEntidadSSO" NVARCHAR(100),
    "UsoToken" INTEGER DEFAULT 0
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa" BIGINT, "CodigoParametro" NVARCHAR(100), "Valor" NVARCHAR(500));

CREATE TABLE "BSCL.SocioNegocio.BD::EMP.Empleado" ("IdEmpleado" BIGINT, "IdEstado" NVARCHAR(3), "EsEnrolado" NVARCHAR(1));
CREATE TABLE "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona" BIGINT, "IdEstado" NVARCHAR(3), "EsEnrolado" NVARCHAR(1));
CREATE TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" ("IdCliente" BIGINT, "IdEstado" NVARCHAR(3), "EsEnrolado" NVARCHAR(1));

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Log" START WITH 1000;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleLog" START WITH 5000;
CREATE TABLE "BSCL.Plataforma.BD::PLA.Log" (
    "IdLog" BIGINT PRIMARY KEY, "IdTipoLog" NVARCHAR(20), "IdModulo" NVARCHAR(3), "IdUsuario" BIGINT, "IdEmpresa" BIGINT, "FechaLog" DATE,
    "PeriodoLog" NVARCHAR(7), "IdUsuarioAtendido" BIGINT, "IdCliente" BIGINT, "EstadoCliente" NVARCHAR(40), "NombreCliente" NVARCHAR(200),
    "IdTipoDocumentoIdentidad" NVARCHAR(10), "IdDocumentoIdentidad" NVARCHAR(40), "IdPortafolio" BIGINT, "EstadoPortafolio" NVARCHAR(40),
    "IdRegistro" NVARCHAR(40), "EstadoRegistro" NVARCHAR(40), "IdTipoRegistro" NVARCHAR(40), "IdTipoOperacion" NVARCHAR(10), "Folio" BIGINT,
    "Cantidad" DECIMAL(28,8), "PrecioTasa" DECIMAL(28,8), "Monto" DECIMAL(28,8), "IdMonedaOrigen" NVARCHAR(3), "IdMonedaLiquidacion" NVARCHAR(3),
    "TipoCambio" NVARCHAR(40), "Nemo" NVARCHAR(40), "IdProceso" BIGINT, "EstadoProceso" NVARCHAR(40), "NombreProceso" NVARCHAR(100),
    "FechaRegistro" TIMESTAMP, "IdPersona" BIGINT, "CodigoAutenticacion" NVARCHAR(100)
);
CREATE TABLE "BSCL.Plataforma.BD::PLA.DetalleLog" (
    "IdDetalleLog" BIGINT PRIMARY KEY, "IdLog" BIGINT, "SubRegistro" NVARCHAR(40), "CampoRegistro" NVARCHAR(40), "ValorAntiguo" NVARCHAR(5000),
    "ValorNuevo" NVARCHAR(5000), "CodigoAutenticacion" NVARCHAR(100)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario" NVARCHAR(100), "CodLenguaje" NVARCHAR(3), "Descripcion" NVARCHAR(500), "Abreviado" NVARCHAR(100));

CREATE TABLE "BSCL.Plataforma.BD::ACC.PasswordProhibidas" ("Palabra" NVARCHAR(100));
CREATE TABLE "BSCL.Plataforma.BD::PLA.Imagenes" ("NombreImagen" NVARCHAR(100), "IdEmpresa" BIGINT, "RutaPublica" NVARCHAR(500));
CREATE TABLE "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdSocioDeNegocio" BIGINT, "IdCliente" BIGINT, "IdEmpleado" BIGINT, "IdPersona" BIGINT, "IdEmpresa" BIGINT);
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" ("IdPersona" BIGINT, "IdDireccionElectronicaPrincipal" BIGINT, "NombreCompleto" NVARCHAR(200));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" ("IdEmpresa" BIGINT, "RazonSocial" NVARCHAR(200));
