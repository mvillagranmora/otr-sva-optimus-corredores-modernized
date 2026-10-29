package cl.bcs.fondomutuo.consultas.solicitudes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.kit.MensajeSalida;

/** JSON types of legacy {@code ConsultarSolicitudesFFMM} (field names and order = legacy: the golden compares the raw text). */
public final class ConsultarSolicitudesContrato {

    private ConsultarSolicitudesContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdCliente = 0L;
        public String p_TipoSolicitud = "";
        public String p_Origen = "";
        public String p_Folio = "";
        public String p_TipoAhorro = "";
        public String p_TipoCuenta = "";
        public String p_IdSerieFondo = "";
        public String p_Estado = "";
        public String p_FechaDesde = "";
        public String p_FechaHasta = "";
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<Solicitud> P_OUT_SOLICITUDES = new ArrayList<>();
    }

    public static class Solicitud {
        public Long IdCliente;
        public String TipoSolicitud;
        public String FechaConsulta;
        public String Origen;
        public Long NumSolicitud;
        public String DetSolicitud;
        public String Estado;
        public String EstadoDetalle;
        public Long Rellamada;
        public String OrigenSolicitud;
        public String FechaSolicitud;
        public String HoraSolicitud;
        public String FechaCierre;
        public String TipoTraspaso;
        public String MotivoRechazo;
        public String Folio;
        public Long FolioRelacionado;
        public String RutCliente;
        public Long CodFondoOrigen;
        public String NombreFondoOrigen;
        public String SerieFondoOrigen;
        public String RegimenFondoOrigen;
        public BigDecimal MontoPesosOrigen;
        public BigDecimal MontoCuotasOrigen;
        public BigDecimal ValorCuotaOrigen;
        public Long CodFondoDestino;
        public String NombreFondoDestino;
        public String SerieFondoDestino;
        public String RegimenFondoDestino;
        public BigDecimal MontoPesosDestino;
        public BigDecimal MontoCuotasDestino;
        public BigDecimal ValorCuotaDestino;
        public BigDecimal MontoCuotasComision;
        public BigDecimal PorcentajeOrigen;
        public BigDecimal PorcentajeDestino;
        public String NombreCliente;
        public String ClasificacionMLT;
        public String DescripcionTipoAhorro;
        public String DescripcionTipoCuenta;
        public String FechaValorCuota;
        public String FechaIngreso;
        public BigDecimal MontoCuotas;
        public BigDecimal MontoCuotasComprometidas = BigDecimal.ZERO;
        public BigDecimal MontoCuotasDisponibles = BigDecimal.ZERO;
        public BigDecimal MontoPesos;
        public BigDecimal MontoPesosComprometidos = BigDecimal.ZERO;
        public BigDecimal MontoPesosDisponibles = BigDecimal.ZERO;
        public String NombreAdministradora;
        public BigDecimal OrigenValorCuota;
        public String RegimenTributario;
        public String SerieFondo;
        public String TipoAhorro;
        public String TipoCuenta;
        public String TipoOperacion;
        public BigDecimal ValorCuota;
        public String TipoMovimiento;
        public Long IdMatrizAgrupador;
        public String EsADC;
        public String IdSerieFondoOrigen;
        public String IdSerieFondoDestino;
        public String FechaCertificacion;
        public String FechaLiquidacion;
        public String FechaCurse;
        public String FechaValorCuotaOrigen;
        public String FechaValorCuotaDestino;
        public String IngresoAutomatico;
        public Long FolioContrato;
        public String Canal;
        public String DescripcionCanal;
        public String IdMedioDePago;
        public String DescripcionMedioPago;
        public String IdSerieFondo;
        public Long IdContrato;
        public String FechaFinalReinversion;
    }
}
