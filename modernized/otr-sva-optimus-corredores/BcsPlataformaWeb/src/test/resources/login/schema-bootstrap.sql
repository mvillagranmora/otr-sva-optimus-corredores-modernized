-- H2 schema of the bootstrap tests: only what BootstrapServicio / SesionDatos read. Fictitious structure for tests.
CREATE ALIAS TO_VARCHAR FOR 'cl.bcs.plataforma.login.h2.HanaFunciones.toVarchar';

CREATE TABLE "BSCL.Plataforma.BD::ACC.ConfiguracionSSO" ("IdEntidad" NVARCHAR(100), "RedirectURL" NVARCHAR(500));
CREATE TABLE "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa" BIGINT, "Nombre" NVARCHAR(200), "IdPais" BIGINT, "IdSocioDeNegocio" BIGINT);
CREATE TABLE "BSCL.Plataforma.BD::INS.Corredora" ("IdCorredora" BIGINT, "IdPersona" BIGINT);
CREATE TABLE "BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema" NVARCHAR(100), "Nombre" NVARCHAR(100), "Valor" NVARCHAR(200), "IdEmpresa" BIGINT, "IdEstado" NVARCHAR(3));

CREATE TABLE "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ("IdEmpresa" BIGINT, "Nombre" NVARCHAR(200), "IdEstado" NVARCHAR(3));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen" ("NombreImagen" NVARCHAR(100), "Binario" VARBINARY(100000));
CREATE TABLE "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado" BIGINT, "Tipo" NVARCHAR(10), "Fecha" DATE, "Dia" INTEGER, "DescripcionDia" NVARCHAR(100), "AgnoCarga" INTEGER, "IdPais" BIGINT, "IdEstado" NVARCHAR(3));
CREATE TABLE "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoInstrumento" NVARCHAR(3), "IdTipoOperacion" NVARCHAR(6));
CREATE TABLE "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion" NVARCHAR(6), "AccionOrden" NVARCHAR(20), "IdTipoProducto" NVARCHAR(4), "EsVentaCorta" NVARCHAR(1), "EsPutEsCall" NVARCHAR(1), "EsOrden" NVARCHAR(1));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionUsuario" ("IdEmpresa" BIGINT, "IdUsuario" BIGINT, "Llave" NVARCHAR(100), "Valor" NVARCHAR(500));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionGrilla" ("IdEmpresa" BIGINT, "Llave" NVARCHAR(100), "Valor" NVARCHAR(500));
CREATE TABLE "BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" ("IdConfiguracionFiltroInterfaz" BIGINT, "Controlador" NVARCHAR(100), "TipoObjeto" NVARCHAR(100), "NombreObjeto" NVARCHAR(100), "IdEstado" NVARCHAR(3), "Filtros" NVARCHAR(2000));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso" ("IdEmpresa" BIGINT, "IdUsuario" BIGINT, "IdElementoHtml" NVARCHAR(100), "EsVisibleMenu" NVARCHAR(1), "Existe" NVARCHAR(1), "MostrarAcceso" NVARCHAR(1), "Padre" BIGINT, "Orden" INTEGER, "IdAcceso" BIGINT, "NombreAcceso" NVARCHAR(100), "TipoAcceso" NVARCHAR(10), "IdServicio" BIGINT);

CREATE TABLE "BSCL.Contabilidad.BD::CON.ConfiguracionContabilidad" ("IdEmpresa" BIGINT, "CodigoParametro" NVARCHAR(100), "Valor" NVARCHAR(500));
CREATE TABLE "BSCL.Orden.BD::ORD.ConfiguracionOrdenes" ("IdEmpresa" BIGINT, "CodigoParametro" NVARCHAR(100), "Valor" NVARCHAR(500));
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion" ("IdEmpresa" BIGINT, "CodigoParametro" NVARCHAR(100), "Valor" NVARCHAR(500));
CREATE TABLE "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" ("IdEmpresa" BIGINT, "CodigoParametro" NVARCHAR(100), "Valor" NVARCHAR(500));
CREATE TABLE "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" ("IdEmpresa" BIGINT, "CodigoParametro" NVARCHAR(100), "Valor" NVARCHAR(500));
CREATE TABLE "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio" ("IdEmpresa" BIGINT, "CodigoParametro" NVARCHAR(100), "Valor" NVARCHAR(500));
CREATE TABLE "BSCL.Custodia.BD::CUS.Configuracion" ("IdEmpresa" BIGINT, "CodigoParametro" NVARCHAR(100), "Valor" NVARCHAR(500));
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.Parametro" ("IdEmpresa" BIGINT, "NombreTipoParametro" NVARCHAR(100), "ValorParametro" NVARCHAR(500));
CREATE TABLE "BSCL.APV.BD::PLA.ConfiguracionAPV" ("IdEmpresa" BIGINT, "CodigoParametro" NVARCHAR(100), "Valor" NVARCHAR(500));
CREATE TABLE "BSCL.Mesa.BD::MES.ConfiguracionMesa" ("IdEmpresa" BIGINT, "CodigoParametro" NVARCHAR(100), "Valor" NVARCHAR(500));
CREATE TABLE "BSCL.Plataforma.BD::PRO.Configuracion" ("IdEmpresa" BIGINT, "CodigoParametro" NVARCHAR(100), "Valor" NVARCHAR(500));
