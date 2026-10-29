-- H2 (MODE=LEGACY) stand-in for the SAP HANA objects read/written by the APV "family B" (traspasos) legacy classes. Quoted names are the exact HANA
-- catalog names so the production SQL resources (legacy cl/bcs/APV/sql/*.sql) run unmodified through support/H2Hana.java. HANA views are modelled
-- as tables: the seeds insert the rows the view would return. THE HANA DDL IS NOT IN THE REPOSITORY: every column list and type below is INFERRED
-- from the legacy SQL (the columns a statement reads or writes) and from the Java POJOs; unknown types are an ASSUMPTION (money DECIMAL(28,16) so the
-- stand-in itself rounds nothing, dates DATE/TIMESTAMP as the legacy SQL comments say). Executed by BOTH the legacy oracle runner and the modern tests.

CREATE TABLE DUMMY (X VARCHAR(1));
INSERT INTO DUMMY VALUES ('X');

CREATE ALIAS TO_NVARCHAR FOR "cl.bcs.apv.familia_b.support.H2Hana.toNvarchar";
CREATE ALIAS TO_VARCHAR FOR "cl.bcs.apv.familia_b.support.H2Hana.toVarchar";
CREATE ALIAS TO_BIGINT FOR "cl.bcs.apv.familia_b.support.H2Hana.toBigint";
CREATE ALIAS TO_DECIMAL FOR "cl.bcs.apv.familia_b.support.H2Hana.toDecimal";
CREATE ALIAS TO_DATE FOR "cl.bcs.apv.familia_b.support.H2Hana.toDate";
CREATE ALIAS TO_TIMESTAMP FOR "cl.bcs.apv.familia_b.support.H2Hana.toTimestamp";
CREATE ALIAS ADD_SECONDS FOR "cl.bcs.apv.familia_b.support.H2Hana.addSeconds";
CREATE ALIAS ADD_DAYS FOR "cl.bcs.apv.familia_b.support.H2Hana.addDays";
CREATE ALIAS HANA_UTCNOW FOR "cl.bcs.apv.familia_b.support.H2Hana.utcNow";

-- ---------------------------------------------------------------------------------------------- APV.OrigenDestinoTransicion (ModificarOrigenDestino)
CREATE TABLE "BSCL.APV.BD::APV.OrigenDestinoTransicion" (
  "IdOrigenDestinoTransicion" BIGINT NOT NULL PRIMARY KEY,
  "IdEmpresa"                 BIGINT,
  "Operacion"                 VARCHAR(20),
  "OrigenCtaBco"              BIGINT,
  "SocioNegocioCtaBco"        BIGINT,
  "DestinoCtaBco"             BIGINT,
  "MueveCuentaCorriente"      VARCHAR(1),
  "MueveCuentaInversion"      VARCHAR(1),
  "FechaModificacion"         TIMESTAMP,
  "IdUsuarioModificador"      BIGINT,
  "IdEstado"                  VARCHAR(3)
);

-- ---------------------------------------------------------------------------------------------- TRA: traspasos
CREATE TABLE "BSCL.APV.BD::TRA.Traspaso" (
  "IdTraspaso"        BIGINT NOT NULL PRIMARY KEY,
  "IdContratoSAAPV"   BIGINT,
  "IdEmpresa"         BIGINT,
  "TipoMovimiento"    VARCHAR(10),
  "RazonRetiro"       VARCHAR(3),
  "FechaIngreso"      DATE,
  "FechaEgreso"       DATE,
  "FechaModificacion" DATE,
  "FechaSuscripcion"  DATE,
  "CodigoTipoTraspaso" VARCHAR(10),
  "IdCliente"         BIGINT,
  "IdPersona"         BIGINT,
  "Descripcion"       VARCHAR(255),
  "CodigoTipoAhorro"  VARCHAR(10),
  "NombreEntidad"     VARCHAR(100),
  "DocumentoEntidad"  VARCHAR(11),
  "IdEstado"          VARCHAR(3),
  "Canal"             VARCHAR(100),
  "CierreSolicitud"   DATE
);
CREATE TABLE "BSCL.APV.BD::TRA.DetalleOrigenTraspaso" (
  "IdDetalleOrigenTraspaso" BIGINT NOT NULL PRIMARY KEY,
  "IdTraspaso"              BIGINT NOT NULL,
  "CodigoTipoAhorro"        VARCHAR(10),
  "IdEntidadRecaudadora"    BIGINT,
  "NombreEntidad"           VARCHAR(100),
  "DocumentoEntidad"        VARCHAR(11),
  "Fondo"                   VARCHAR(10),
  "MontoTraspaso"           DECIMAL(26,8),
  "UFTraspaso"              DECIMAL(26,8),
  "PorcentajeTraspaso"      DECIMAL(26,8),
  "IdEstado"                VARCHAR(3),
  "IdInstrumento"           BIGINT,
  "IdPortafolio"            BIGINT,
  "CodigoRegimenTributario" VARCHAR(10)
);
CREATE TABLE "BSCL.APV.BD::TRA.OrdenTraspaso" (
  "IdOrdenTraspaso"          BIGINT NOT NULL PRIMARY KEY,
  "IdTraspaso"               BIGINT NOT NULL,
  "IdOrden"                  BIGINT,
  "IdSolicitudAporteRescate" BIGINT
);
CREATE SEQUENCE "BSCL.APV.BD.Secuencia::TRA.IdOrdenTraspaso" START WITH 1 INCREMENT BY 1;
CREATE TABLE "BSCL.APV.BD::TRA.ConfiguracionRetencionTraspaso" (
  "IdConfiguracionRetencion" BIGINT,
  "PorcentajeRetencion"      DECIMAL(28,16),
  "MontoTopeRetencion"       DECIMAL(26,8),
  "CodigoRetiro"             VARCHAR(10),
  "IdPersonaRetencion"       BIGINT,
  "IdRegimenTributario"      BIGINT,
  "IdEstado"                 VARCHAR(3)
);

-- ---------------------------------------------------------------------------------------------- SCS: contratos SAAPV (columns of ContratoSAAPV_Insert + IdPortafolio of ModificaIdsContratoSAAPV)
CREATE TABLE "BSCL.APV.BD::SCS.ContratoSAAPV" (
  "IdContratoSAAPV" BIGINT NOT NULL PRIMARY KEY, "IdEmpresa" BIGINT, "FolioContratoSAAPV" VARCHAR(50), "IdPlan" BIGINT, "NombrePlan" VARCHAR(100),
  "FechaSuscripcion" DATE, "FechaCreacion" DATE, "FechaAprobacion" DATE, "FechaNotificacion" DATE, "FechaRecepcion" DATE, "Folio" VARCHAR(50),
  "IdPersona" BIGINT, "IdEmpleador" BIGINT, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "NombreCliente" VARCHAR(100), "ApellidoPaternoCliente" VARCHAR(100),
  "ApellidoMaternoCliente" VARCHAR(100), "DocumentoIdentidadCliente" VARCHAR(40), "FechaNacimientoCliente" DATE, "EstadoCivil" VARCHAR(2),
  "IdTipoGenero" VARCHAR(2), "IdPaisResidenciaCliente" BIGINT, "IdCiudadCliente" BIGINT, "IdComunaCliente" BIGINT, "DireccionCliente" VARCHAR(100),
  "IdTipoCargoPersona" BIGINT, "TelefonoCliente" VARCHAR(40), "TelefonoComercialCliente" VARCHAR(40), "TelefonoCelularCliente" VARCHAR(40),
  "EmailCliente" VARCHAR(100), "RazonSocialEmpleador" VARCHAR(100), "IdMedioRecaudacionTrabajador" BIGINT, "IdEntidadRecaudadora" BIGINT,
  "DocumentoIdentidadEmpleador" VARCHAR(40), "DireccionComercialEmpleador" VARCHAR(100), "IdComunaEmpleador" BIGINT, "TelefonoEmpleador" VARCHAR(40),
  "NombreEncargado" VARCHAR(100), "EmailEncargado" VARCHAR(100), "TelefonoEncargado" VARCHAR(40), "CodigoPrevision" BIGINT, "Pensionado" VARCHAR(2),
  "Trabajador" VARCHAR(20), "FormaDePago" VARCHAR(10), "IdMedioRecaudacionFormaPago" BIGINT, "DiaDescuento" BIGINT, "Institucion" VARCHAR(100),
  "ComisionTransferencia" DECIMAL(26,8), "CodigoTipoSolicitud" VARCHAR(3), "NombreEntidadOrigen" VARCHAR(100), "RutEntidadOrigen" VARCHAR(11),
  "TipoDeAhorro" VARCHAR(10), "FondoDeOrigen" VARCHAR(10), "TipoTraspaso" VARCHAR(20), "MontoTraspaso" DECIMAL(26,8), "UFTraspaso" DECIMAL(26,8),
  "PorcentajeTraspaso" DECIMAL(26,8), "IdEstadoEnvioNotificacion" VARCHAR(3), "IdEstadoSolicitud" VARCHAR(3), "IdEstado" VARCHAR(3),
  "IdCategoriaInversionista" VARCHAR(40), "IdPerfilComercial" VARCHAR(40), "IdCargaMasiva" BIGINT, "Canal" VARCHAR(100)
);

-- ---------------------------------------------------------------------------------------------- APV.CargaMasiva / Registro_Historico_XML (CargaRegistroHistoricoTraspaso)
CREATE TABLE "BSCL.APV.BD::APV.CargaMasiva" (
  "IdEmpresa" BIGINT, "IdCargaMasiva" BIGINT NOT NULL PRIMARY KEY, "NombreCargaArchivo" VARCHAR(255), "CantidadRegistros" BIGINT, "IdUsuario" BIGINT,
  "IdTipoCarga" BIGINT, "Descripcion" VARCHAR(255), "FechaCarga" VARCHAR(30), "IdEstado" VARCHAR(3)
);
CREATE TABLE "BSCL.APV.BD::APV.Registro_Historico_XML" (
  "IDEMPRESA" BIGINT, "IDMODULO" VARCHAR(10), "IDUSUARIO" BIGINT, "NOMBRECARGA" VARCHAR(255), "ORIGEN" VARCHAR(255), "FECHACARGA" VARCHAR(40), "ARCHIVO" VARCHAR(255)
);

-- ---------------------------------------------------------------------------------------------- views / tables read by ConsultaOrdenesTraspaso.sql (CalculoRetencionTraspasos, CrearTraspasoTesoreria)
CREATE TABLE "BSCL.Orden.BD.Vistas::ConsultarOrden" ("IdOrden" BIGINT, "MontoLiquidacion" DECIMAL(28,16), "IdPortafolio" BIGINT);
CREATE TABLE "BSCL.Orden.BD::ORD.Asignacion" ("IdAsignacion" BIGINT, "IdOrden" BIGINT);
CREATE TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobante" ("IdAsignacion" BIGINT, "IdEstadoFacturacion" VARCHAR(3));
CREATE TABLE "BSCL.Portafolio.BD::POR.Portafolio" (
  "IdPortafolio" BIGINT NOT NULL PRIMARY KEY, "IdCliente" BIGINT, "CodigoPortafolio" VARCHAR(20), "IdMatrizAgrupadorAPV" BIGINT, "IdEstado" VARCHAR(3),
  "IdEstadoAutorizacion" VARCHAR(3), "Proposito" VARCHAR(20)
);
CREATE TABLE "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV" BIGINT NOT NULL PRIMARY KEY, "IdPlanTipoInversion" BIGINT, "IdProductoAgrupador" BIGINT, "IdRegimenTributario" BIGINT);
CREATE TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateUI" ("IdSolicitudAporteRescate" BIGINT, "EstadoSolicitud" VARCHAR(3), "Monto" DECIMAL(28,16), "IdPortafolio" BIGINT);

-- ---------------------------------------------------------------------------------------------- CalculoMontosDesinversion (Deposito_*.sql are not reached; ObtenerDetalleTraspasoDesinversion + CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio)
CREATE TABLE "BSCL.Plataforma.BD::INS.Instrumento" ("IdInstrumento" BIGINT NOT NULL PRIMARY KEY, "IdMoneda" VARCHAR(3));
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ("IdFondo" BIGINT NOT NULL PRIMARY KEY);
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" (
  "IdSerieFondo" VARCHAR(40) NOT NULL, "IdEmpresa" BIGINT NOT NULL, "IdFondo" BIGINT, "DecimalesCuota" BIGINT, "DecimalesValorCuota" BIGINT, "RestringeAporte" VARCHAR(1),
  PRIMARY KEY ("IdSerieFondo", "IdEmpresa")
);
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio" ("IdSerieFondo" VARCHAR(40), "IdEmpresa" BIGINT, "ValorDolarAporte" DECIMAL(28,16), "ValorDolarRescate" DECIMAL(28,16));

-- ---------------------------------------------------------------------------------------------- TraspasoCuentasBancarias (ConsultaOrigenDestinoTransicion / ConsultaOrigenRecaudacion / ConsultarPersonaClienteRecaudacion)
CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" (
  "IdCuentaBanco" BIGINT, "IdBanco" BIGINT, "IdTipoCuentaBancaria" VARCHAR(10), "NombreTipoCuentaBancaria" VARCHAR(50), "NumeroCuentaBancaria" VARCHAR(30)
);
CREATE TABLE "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ("IdEmpresa" BIGINT, "IdSocioDeNegocio" BIGINT, "IdPersona" BIGINT);
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ("IdCliente" BIGINT, "IdPersona" BIGINT, "IdSocioDeNegocio" BIGINT, "IdPortafolioDefecto" BIGINT, "IdEstado" VARCHAR(3));
CREATE TABLE "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdSocioDeNegocio" BIGINT, "IdCliente" BIGINT, "IdPersona" BIGINT);
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" (
  "IdCuentaBancaria" BIGINT, "IdBanco" BIGINT, "IdTipoCuentaBancaria" VARCHAR(10), "NumeroCuentaBancaria" VARCHAR(30)
);
CREATE TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" ("IdCliente" BIGINT NOT NULL PRIMARY KEY, "IdPersona" BIGINT, "IdEstado" VARCHAR(3));
CREATE TABLE "BSCL.APV.BD::REC.Cliente" (
  "IdRecaudacionCliente" BIGINT NOT NULL PRIMARY KEY, "IdRecaudacion" BIGINT, "IdCliente" BIGINT, "MontoDepositoConvenido" DECIMAL(26,8), "MontoDepositoAPV" DECIMAL(26,8),
  "IdEstado" VARCHAR(3), "Origen" VARCHAR(10)
);
CREATE TABLE "BSCL.APV.BD::REC.Recaudacion" (
  "IdRecaudacion" BIGINT NOT NULL PRIMARY KEY, "IdEmpresa" BIGINT, "CodigoArchivo" VARCHAR(10), "FolioCartola" BIGINT, "LineaCartola" BIGINT, "Sucursal" VARCHAR(200),
  "FechaDeposito" DATE, "CuentaCorriente" BIGINT, "MontoDeposito" DECIMAL(26,8), "ConceptoRecaudacion" VARCHAR(300), "Origen" VARCHAR(100), "Observacion" VARCHAR(200),
  "NumeroDocumento" BIGINT, "IdTipoDeposito" BIGINT, "IdBanco" BIGINT, "IdRegimenTributario" BIGINT, "IdCarga" BIGINT, "IdEstado" BIGINT
);

-- ---------------------------------------------------------------------------------------------- detalle de traspaso y portafolio-contrato (GrabarTraspasos, ModificarPortafolioTraspaso)
CREATE TABLE "BSCL.APV.BD::TRA.DetalleTraspaso" (
  "IdTraspaso" BIGINT NOT NULL, "IdDetalleTraspaso" BIGINT NOT NULL, "IdPortafolio" BIGINT, "FechaIngreso" DATE, "FechaModificacion" DATE, "IdPlanTipoInversion" BIGINT,
  "IdProductoAgrupador" BIGINT, "IdRegimenTributario" BIGINT, "MontoPeso" DECIMAL(26,8), "MontoUF" DECIMAL(26,8), "Porcentaje" DECIMAL(26,8), "IdEstado" VARCHAR(3), "EsReapertura" VARCHAR(1)
);
CREATE TABLE "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" (
  "IdPortafolioContratoSAAPV" BIGINT NOT NULL PRIMARY KEY, "IdPortafolio" BIGINT, "IdContratoSAAPV" BIGINT, "IdEstado" VARCHAR(3), "OrigenAhorro" VARCHAR(3), "MontoPesos" DECIMAL(26,8),
  "MontoUF" DECIMAL(26,8), "PorcentajeRegimenImponible" DECIMAL(26,8), "Pago" VARCHAR(3), "PeriodoInicio" VARCHAR(7), "PeriodoTermino" VARCHAR(7)
);
CREATE SEQUENCE "BSCL.APV.BD.Secuencia::SCS.IdPortafolioContratoSAAPV" START WITH 1 INCREMENT BY 1;
CREATE TABLE "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ("IdPortafolio" BIGINT, "CodigoPortafolio" VARCHAR(20), "Proposito" VARCHAR(20), "IdCliente" BIGINT, "IdEstado" VARCHAR(3));
CREATE TABLE "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona" BIGINT NOT NULL PRIMARY KEY, "Tipo" VARCHAR(1), "IdProfesion" BIGINT);
CREATE TABLE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ("IdPersona" BIGINT, "DocumentoIdentidad" VARCHAR(40), "Predeterminado" VARCHAR(1));
CREATE TABLE "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" (
  "IdMatrizAgrupadorAPV" BIGINT, "IdPropositoPortafolio" VARCHAR(20), "NombreAgrupador" VARCHAR(100), "Codigo" VARCHAR(20), "CodigoAgrupador" VARCHAR(40), "NombrePlan" VARCHAR(100)
);

-- ---------------------------------------------------------------------------------------------- ValidaContratoSAAPV: validacion de recaudacion (la consulta se prepara siempre, aunque no haya recaudaciones)
CREATE TABLE "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido" (
  "FolioRecaudacion" BIGINT, "IdRecaudacionCliente" BIGINT, "IdRecaudacionEmpleador" BIGINT, "RutEmpleador" VARCHAR(20), "RecMonto" DECIMAL(26,8), "TipoRegimen" VARCHAR(10),
  "Regimen" VARCHAR(20), "IdCliente" BIGINT
);
CREATE TABLE "BSCL.APV.BD::REC.AsignacionContratoRecaudacion" ("IdRecaudacionCliente" BIGINT, "IdContratoSAAPV" BIGINT);
CREATE TABLE "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV" (
  "IdPortafolio" BIGINT, "IdContratoSAAPV" BIGINT, "IdMedioRecaudacionFormaPago" BIGINT, "RutEmpleador" VARCHAR(20), "Pago" VARCHAR(3), "PeriodoTermino" VARCHAR(7), "MontoPesos" DECIMAL(26,8),
  "OrigenAhorro" VARCHAR(3), "FormaDePago" VARCHAR(10), "RegimenTributario" VARCHAR(20), "IdEstadoPortafolio" VARCHAR(3), "IdEstadoNuevo" VARCHAR(3)
);
CREATE TABLE "BSCL.APV.BD.Vistas::SCS.ConsultarDistribucionInversion" ("IdContratoSAAPV" BIGINT, "Nemotecnico" VARCHAR(40));

-- ---------------------------------------------------------------------------------------------- GrabarContratoTraspasoSAAPV
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" ("IdRelacionPersona" BIGINT, "IdPersonaOrigen" BIGINT, "IdPersonaDestino" BIGINT);
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" ("IdDireccionFisica" BIGINT, "IdPersona" BIGINT, "IdEstado" VARCHAR(3));
CREATE TABLE "BSCL.SocioNegocio.BD::PER.DireccionFisica" (
  "IdDireccionFisica" BIGINT NOT NULL PRIMARY KEY, "Calle" VARCHAR(100), "Numero" VARCHAR(20), "Departamento" VARCHAR(20), "Sector" VARCHAR(50), "CodigoPostal" VARCHAR(10), "IdCiudad" BIGINT,
  "CodigoProposito" VARCHAR(10), "Comentario" VARCHAR(100), "IdEstado" VARCHAR(3), "FechaIngreso" VARCHAR(20), "FechaCreacion" VARCHAR(30), "FechaModificacion" VARCHAR(30), "IdPersona" BIGINT,
  "IdEstadoAutorizacion" VARCHAR(3), "IdCarga" BIGINT, "IdOrigen" BIGINT, "Borrado" VARCHAR(1), "IdUsuarioCreador" BIGINT, "IdDivisionPais" BIGINT, "Predeterminado" VARCHAR(1), "CodigoCompuesto" VARCHAR(100)
);
CREATE SEQUENCE "BSCL.SocioNegocio.BD.Secuencias::DireccionFisica" START WITH 1 INCREMENT BY 1;
CREATE TABLE "BSCL.Plataforma.BD::IND.TipoIndicador" ("IdTipoIndicador" BIGINT, "CodigoTipoIndicador" VARCHAR(40), "IdTipoEntidad" VARCHAR(20), "GrupoIndicador" VARCHAR(40));
CREATE TABLE "BSCL.APV.BD::SCS.DistribucionInversion" (
  "IdDistribucionInversion" BIGINT, "IdContratoSAAPV" BIGINT, "FolioContratoSAAPV" VARCHAR(50), "IdInstrumento" BIGINT, "Nemotecnico" VARCHAR(40), "MontoPesos" DECIMAL(26,8), "Porcentaje" DECIMAL(26,8), "IdEstado" VARCHAR(3)
);
CREATE SEQUENCE "BSCL.APV.BD.Secuencias::SCS.IdDistribucionInversion" START WITH 1 INCREMENT BY 1;

-- ---------------------------------------------------------------------------------------------- IngresoRecaudacionTraspaso / ReaperturarTraspaso / ValidaContratoSAAPV (vistas de traspasos)
CREATE TABLE "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPV" (
  "IdTraspaso" BIGINT, "IdPersona" BIGINT, "IdEmpleador" BIGINT, "FolioContratoSAAPV" VARCHAR(50), "MontoPesoDetalle" DECIMAL(26,8), "Trabajador" VARCHAR(20), "IdEstado" VARCHAR(3), "NombreEntidad" VARCHAR(100)
);
CREATE TABLE "BSCL.APV.BD.Vistas::TRA.ConsultarDetalleTraspasosAPV" (
  "IdTraspaso" BIGINT, "IdDetalleTraspaso" BIGINT, "IdContratoSAAPV" BIGINT, "IdRegimenTributario" BIGINT, "Codigo" VARCHAR(20), "MontoPeso" DECIMAL(26,8), "MontoUF" DECIMAL(26,8),
  "Porcentaje" DECIMAL(26,8), "IdCliente" BIGINT, "IdEstado" VARCHAR(3), "EsReapertura" VARCHAR(1), "IdPortafolio" BIGINT
);
