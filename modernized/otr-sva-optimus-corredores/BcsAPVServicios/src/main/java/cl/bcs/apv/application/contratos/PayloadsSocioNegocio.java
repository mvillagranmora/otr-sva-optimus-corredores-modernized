package cl.bcs.apv.application.contratos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Wire contract of the collaborators OUTSIDE APV that the contract-saving use cases call through {@code Colaboradores} (SocioNegocio CrearPersona /
 * CrearCliente / CrearTelefono / CrearDireccionElectronica, Plataforma AgregarLog / CrearEventoAutorizacionMasivo): the legacy input and output objects
 * of those services, with the legacy field names, field order and constructor defaults. They are serialised with {@code Json.GSON} (the legacy platform Gson
 * settings) into the JSON the collaborator receives, and the answer is decoded into the {@code Salida*} classes with the same Gson: a key the answer does not
 * carry keeps the legacy constructor default (counters 0, empty lists), exactly as the legacy decoded it.
 */
public final class PayloadsSocioNegocio {

    private PayloadsSocioNegocio() {
    }

    // ------------------------------------------------------------------------------------------------ common

    public static class ErrorDetalle {
        public String Codigo;
        public String Descripcion;
        public Long IdEntidad;

        public ErrorDetalle() {
            this.IdEntidad = 0L;
        }
    }

    public static class AdvertenciaDetalle {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
    }

    public abstract static class SalidaEstandar {
        public long P_OUT_ADVERTENCIASCONTADOR;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
        public long P_OUT_ERRORESCONTADOR;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

        public void addAdvertencia(String codigo, String descripcion) {
            P_OUT_ADVERTENCIASCONTADOR++;
            AdvertenciaDetalle a = new AdvertenciaDetalle();
            a.Codigo = codigo;
            a.RequiereAutorizacion = "N";
            a.Descripcion = descripcion;
            P_OUT_ADVERTENCIASDETALLE.add(a);
        }

        public void addError(String codigo, String descripcion) {
            P_OUT_ERRORESCONTADOR++;
            ErrorDetalle e = new ErrorDetalle();
            e.Codigo = codigo;
            e.Descripcion = descripcion;
            P_OUT_ERRORESDETALLE.add(e);
        }
    }

    // ------------------------------------------------------------------------------------------------ enrolment

    /** Legacy {@code cl.bcs.socionegocio.pojo.Enrolamiento} (the instance is reused and mutated by the legacy flow, as here). */
    public static class Enrolamiento {
        public Long IdEnrolamiento;
        public Long IdEmpresa;
        public Long IdUsuario;
        public String IdTipoEntidad;
        public Long IdEntidadEnrolada;
        public String IdTipoDocumentoIdentidad;
        public String IdTipoDocumentoIdentidadAsociado;
        public String DocumentoIdentidad;
        public String NumeroSerie;
        public String FechaEmision;
        public String FechaVencimiento;
        public String IdTipoDocumentoIdentidadAdicional;
        public String DocumentoIdentidadAdicional;
        public String Tipo;
        public String Nombres;
        public String ApellidoPaterno;
        public String ApellidoMaterno;
        public String RazonSocial;
        public Long IdPaisNacimiento;
        public Long IdPaisNacionalidadPrincipal;
        public Long IdPaisResidencia;
        public String ResidenciaTributariaExtranjera;
        public java.sql.Date FechaNacimiento;
        public String Sexo;
        public String EstadoCivil;
        public Long IdCiudad;
        public Long IdDivisionPais;
        public String IdTipoTelefono;
        public String Telefono;
        public String Email;
        public String DireccionCalle;
        public String DireccionNumero;
        public String DireccionDepartamento;
        public java.sql.Timestamp FechaCreacion;
        public String FechaCreacionStr;
        public java.sql.Timestamp FechaModificacion;
        public String FechaModificacionStr;
        public Long IdPersonaConyuge;
        public String RegimenConyugal;
        public String CLI_IdCategoriaInversionista;
        public String CLI_IdTipoCliente;
        public Long CLI_IdTipoClienteCMF;
        public Long CLI_IdEmpleadoEjecutivo;
        public String CLI_CodigoAdicionalHolding;
        public String CLI_IdClaseCliente;
        public String CLI_ClienteFinal;
        public String CLI_ClienteBloqueado;
        public Long CLI_IdSegmentoCliente;
        public String POR_IdPerfilComercial;
        public Long POR_IdPersonaBrokerExtranjero;
        public String EMP_CodigoEmpleado;
        public String EMP_CodigoTipoCargoEmpleado;
        public Long EMP_IdEmpleadoSupervisor;
        public Long EMP_IdSucursal;
        public java.sql.Date EMP_FechaInicioCargo;
        public java.sql.Date EMP_FechaTerminoCargo;
        public String CNT_CodigoInscripcion;
        public String CNT_Emisor;
        public String CNT_Depositos;
        public String CNT_Bolsa;
        public String CNT_Corredora;
        public String CNT_CamaraDeCompensacion;
        public String CNT_Banco;
        public String CNT_Boveda;
        public String CNT_Otros;
        public Long PRO_IdAreaProveedor;
        public String EMP_CargoIndefinido;
        public Long IdCarga;
        public String EsRutInterno;
        public Long IdProfesion;
        public String TipoEnrolamiento;
        public String CLI_AreaCliente;
        public String UsaDivisionPais;
        public String ConyugeTipoDocumentoIdentidad;
        public String ConyugeDocumentoIdentidad;
        public String ConyugeNombres;
        public String ConyugeApellidoPaterno;
        public String ConyugeApellidoMaterno;
        public transient String EnrolarConyuge;
        public String IdTipoRelacion;
        public String IdTipoCanalIngresoCorredor;
        public String IdSituacionLaboral;
        public Long IdGiro;
        public String CodigoRangoRenta;
        public String CodigoRangoNivelVenta;
        public String CodigoPropositoTransaccion;

        public Enrolamiento() {
            this.IdTipoDocumentoIdentidadAdicional = "";
            this.DocumentoIdentidadAdicional = "";
            this.IdCiudad = 0L;
            this.IdDivisionPais = 0L;
            this.DireccionCalle = "";
            this.DireccionNumero = "";
            this.DireccionDepartamento = "";
            this.IdTipoTelefono = "";
            this.IdPaisNacionalidadPrincipal = 0L;
            this.Email = "";
            this.CLI_IdCategoriaInversionista = "";
            this.CLI_IdTipoCliente = "";
            this.CLI_IdTipoClienteCMF = 0L;
            this.CLI_IdEmpleadoEjecutivo = 0L;
            this.CLI_CodigoAdicionalHolding = "";
            this.CLI_IdClaseCliente = "";
            this.CLI_ClienteFinal = "";
            this.CLI_ClienteBloqueado = "";
            this.CLI_IdSegmentoCliente = 0L;
            this.POR_IdPerfilComercial = "";
            this.POR_IdPersonaBrokerExtranjero = 0L;
            this.EMP_CodigoEmpleado = "";
            this.EMP_CodigoTipoCargoEmpleado = "";
            this.EMP_IdEmpleadoSupervisor = 0L;
            this.EMP_IdSucursal = 0L;
            this.CNT_CodigoInscripcion = "";
            this.CNT_Emisor = "";
            this.CNT_Depositos = "";
            this.CNT_Bolsa = "";
            this.CNT_Corredora = "";
            this.CNT_CamaraDeCompensacion = "";
            this.CNT_Banco = "";
            this.CNT_Boveda = "";
            this.CNT_Otros = "";
            this.PRO_IdAreaProveedor = 0L;
            this.EMP_CargoIndefinido = "";
            this.IdCarga = 0L;
            this.TipoEnrolamiento = "FULL";
            this.CLI_AreaCliente = "";
            this.UsaDivisionPais = "N";
            this.IdTipoRelacion = "";
            this.IdTipoCanalIngresoCorredor = "";
            this.IdSituacionLaboral = "";
            this.IdGiro = 0L;
            this.CodigoRangoRenta = "";
            this.CodigoRangoNivelVenta = "";
            this.CodigoPropositoTransaccion = "";
        }
    }

    // ------------------------------------------------------------------------------------------------ CrearPersona

    public static class EntradaCrearPersona {
        public long p_IdEmpresa;
        public long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio = "N";
        public List<Enrolamiento> p_ConsultarEnrolamiento;
        public String p_IdTipoCanal;

        public EntradaCrearPersona() {
            this.p_IdTipoCanal = "";
        }
    }

    public static class SalidaCrearPersona extends SalidaEstandar {
        public long P_OUT_REFID;
        public long P_OUT_IDSOCIONEGOCIO;
        public long P_OUT_IDENROLAMIENTO;
        public String P_OUT_DOCUMENTOIDENTIDAD = "";

        public SalidaCrearPersona() {
            this.P_OUT_REFID = 0;
            this.P_OUT_IDSOCIONEGOCIO = 0;
            this.P_OUT_ADVERTENCIASCONTADOR = 0;
            this.P_OUT_ERRORESCONTADOR = 0;
            this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
            this.P_OUT_ERRORESDETALLE = new ArrayList<>();
        }
    }

    // ------------------------------------------------------------------------------------------------ CrearCliente

    public static class EntradaCrearCliente {
        public long p_IdEmpresa;
        public long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_CodigoFormulario;
        public String p_IdTipoCanal;
        public List<Enrolamiento> p_ConsultarEnrolamiento;

        public EntradaCrearCliente() {
            this.p_UsaReglasNegocio = "N";
            this.p_CodigoFormulario = "";
            this.p_IdTipoCanal = "";
        }
    }

    public static class SalidaCrearCliente extends SalidaEstandar {
        public long P_OUT_REFIDCLIENTE;
        public long P_OUT_REFIDPORTAFOLIO;
        public String P_OUT_DOCUMENTOIDENTIDAD;
        public List<Long> P_OUT_PORTAFOLIO_IDS;

        public SalidaCrearCliente() {
            this.P_OUT_REFIDCLIENTE = 0;
            this.P_OUT_REFIDPORTAFOLIO = 0;
            this.P_OUT_DOCUMENTOIDENTIDAD = "";
            this.P_OUT_ADVERTENCIASCONTADOR = 0;
            this.P_OUT_ERRORESCONTADOR = 0;
            this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
            this.P_OUT_ERRORESDETALLE = new ArrayList<>();
            this.P_OUT_PORTAFOLIO_IDS = new ArrayList<>();
        }
    }

    // ------------------------------------------------------------------------------------------------ CrearTelefono / CrearDireccionElectronica

    public static class EntradaCrearTelefono {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdPais;
        public String p_IdTipoTelefono;
        public String p_Numero;
        public String p_Anexo;
        public String p_CodigoProposito;
        public String p_Comentario;
        public String p_IdEstado;
        public String p_Tipo;
        public Long p_IdPersona;
        public String p_Predeterminado;

        public EntradaCrearTelefono() {
            this.p_Predeterminado = "N";
        }
    }

    public static class SalidaCrearTelefono extends SalidaEstandar {
        public long p_out_RefId;

        public SalidaCrearTelefono() {
            this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
            this.P_OUT_ERRORESDETALLE = new ArrayList<>();
        }
    }

    public static class EntradaCrearDireccionElectronica {
        public long p_IdEmpresa;
        public long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_Direccion;
        public String p_CodigoProposito;
        public String p_Comentario;
        public String p_IdEstado;
        public String p_Tipo;
        public Long p_IdPersona;
        public String p_Predeterminado;

        public EntradaCrearDireccionElectronica() {
            this.p_Predeterminado = "N";
        }
    }

    public static class SalidaCrearDireccionElectronica extends SalidaEstandar {
        public long p_out_RefId;

        public SalidaCrearDireccionElectronica() {
            this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
            this.P_OUT_ERRORESDETALLE = new ArrayList<>();
        }
    }

    // ------------------------------------------------------------------------------------------------ AgregarLog

    public static class InAgregarLog {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_IdEstadoAutorizacion;
        public Long p_IdTrabajo = 0L;
        public List<Log> p_Log;
        public List<DetalleLog> p_LogDetalle;

        public InAgregarLog() {
            p_Log = new ArrayList<>();
            p_LogDetalle = new ArrayList<>();
        }

        public static class Log {
            public Long IdTrabajo;
            public Long IdCabeceraLog;
            public String IdTipoLog;
            public String IdModulo;
            public Long IdUsuario;
            public Long IdEmpresa;
            public String FechaSistemaLog;
            public Long IdUsuarioAtendido;
            public Long IdCliente;
            public String EstadoCliente;
            public String NombreCliente;
            public String IdTipoDocumentoIdentidad;
            public String IdDocumentoIdentidad;
            public Long IdPortafolio;
            public String EstadoPortafolio;
            public String IdTipoRegistro;
            public String IdRegistro;
            public String EstadoRegistro;
            public String IdTipoOperacion;
            public Long Folio;
            public BigDecimal Cantidad;
            public BigDecimal PrecioTasa;
            public BigDecimal Monto;
            public String IdMonedaOrigen;
            public String IdMonedaLiquidacion;
            public String TipoCambio;
            public String Nemo;
            public Long IdProceso;
            public String EstadoProceso;
            public String NombreProceso;
            public Long IdPersona;

            public Log() {
                this.IdTrabajo = 0L;
                this.IdCliente = 0L;
                this.EstadoCliente = "";
                this.NombreCliente = "";
                this.IdTipoDocumentoIdentidad = "";
                this.IdDocumentoIdentidad = "";
                this.IdPortafolio = 0L;
                this.EstadoPortafolio = "";
                this.IdRegistro = "";
                this.EstadoRegistro = "";
                this.IdTipoOperacion = "";
                this.Folio = 0L;
                this.Cantidad = BigDecimal.ZERO;
                this.PrecioTasa = BigDecimal.ZERO;
                this.Monto = BigDecimal.ZERO;
                this.IdMonedaOrigen = "";
                this.IdMonedaLiquidacion = "";
                this.TipoCambio = "";
                this.Nemo = "";
                this.IdProceso = 0L;
                this.EstadoProceso = "";
                this.NombreProceso = "";
                this.IdPersona = 0L;
            }
        }

        public static class DetalleLog {
            public Long IdTrabajo;
            public Long IdCabeceraLog;
            public String SubRegistro;
            public String CampoRegistro;
            public String ValorAntiguo;
            public String ValorNuevo;
        }
    }

    // ------------------------------------------------------------------------------------------------ CrearEventoAutorizacionMasivo

    public static class EntradaCrearEventoAutorizacion {
        public Long P_IDEMPRESA;
        public Long P_IDUSUARIO;
        public String P_IDMODULO;
        public String P_SIMULAROPERACION;
        public String P_USAREGLASNEGOCIO;
        public List<EventoAutorizacion> P_TABLAEVENTOS;
        public List<DetalleEventoAutorizacion> P_DETALLE = null;

        public EntradaCrearEventoAutorizacion() {
            this.P_TABLAEVENTOS = new ArrayList<>();
            this.P_DETALLE = new ArrayList<>();
        }
    }

    public static class EventoAutorizacion {
        public Long IdUnion = null;
        public String IdTipoEvento;
        public String IdModuloSolicita;
        public Long IdUsuarioSolicita;
        public String Fecha;
        public String IdTipoOperacion;
        public String IdConcepto;
        public String NumeroOperacionConcepto;
        public String FechaOperacionConcepto;
        public Long IdCliente;
        public Long IdFuncionario;
        public Long IdProveedor;
        public String IdTipoProducto;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String Nemo;
        public Long Cantidad;
        public BigDecimal Monto;
        public BigDecimal Precio;
        public BigDecimal Tasa;
        public String IdMoneda;
        public String Glosa;
    }

    public static class DetalleEventoAutorizacion {
        public Long IdUnion = null;
        public String NombreCampo = "";
        public String TipoDato = "";
        public String Valor = "";
        public String Clasificacion = "";
    }
}
