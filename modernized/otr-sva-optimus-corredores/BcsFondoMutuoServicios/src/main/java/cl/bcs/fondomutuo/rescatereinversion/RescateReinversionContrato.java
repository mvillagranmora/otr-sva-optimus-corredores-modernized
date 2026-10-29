package cl.bcs.fondomutuo.rescatereinversion;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.kit.MensajeSalida;

/**
 * The input and output of legacy {@code cl.bcs.fondomutuo.CrearSolicitudRescateReinversion} (JavaCall id 1436): its nested classes
 * {@code EntradaCrearSolicitudRescateReinversion}, {@code SalidaCrearSolicitudRescateReinversion}, {@code InSolicitudesRescate}, {@code InSolicitudesAporte},
 * with the same public field names, order and constructor / field defaults (FM-REI-01). Mutable on purpose: the legacy mutates the input
 * ({@code TipoReinversion}, {@code IdSerieFondoOrigen}, {@code IdBloqueoRescate}, {@code SolicitudBatch} of the contributions) and the Manejador runs the
 * simulation and the real pass over the SAME objects.
 */
public final class RescateReinversionContrato {

    private RescateReinversionContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public String p_IdTipoCanalIngreso;
        public List<InSolicitudesRescate> p_SolicitudesRescate;
        public List<InSolicitudesAporte> p_SolicitudesAporte;
        public String p_IdOrigenProceso;   // CAR: carga masiva de fondos mutuos

        public Entrada() {
            p_SolicitudesAporte = new ArrayList<InSolicitudesAporte>();
            p_SolicitudesRescate = new ArrayList<InSolicitudesRescate>();
            this.p_IdOrigenProceso = "";
        }
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public List<ResultadoSolicitud> P_OUT_RESULTADO;

        public Salida() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
            this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
        }
    }

    public static class InSolicitudesRescate {
        public Long IdInterno;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdSerieFondo;
        public String CalculoPorMontoCuota;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public String IdMoneda;
        public String IdMedioDePago;
        public String SolicitudBatch;
        public String OrigenOperacion;
        public String Glosa;
        public Long IdAutorizadoOrigen;
        public Long FolioContrato;
        public Long IdAgrupador;
        public String IdRegimenTributario;
        public BigDecimal MontoPatrimonio;
        public BigDecimal Porcentaje;
        public String IngresoAutomatico;
        public Date FechaValorCuota;
        public String IdBloqueoRescate;
        public String TipoReinversion = "";   // SWITCH - INTERNACIONAL - NACIONAL

        public InSolicitudesRescate() {
            this.MontoPatrimonio = BigDecimal.ZERO;
            this.IdAgrupador = 0L;
            this.Porcentaje = new BigDecimal("0");
            this.IngresoAutomatico = "N";
            this.IdBloqueoRescate = "";
        }
    }

    public static class InSolicitudesAporte {
        public Long IdInterno;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdSerieFondo;
        public String IdEmisor;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public String IndicadorMLT;
        public String IdMoneda;
        public String IdMedioDePago;
        public String IdRegimenTributario;
        public String Glosa;
        public Long FolioContrato;
        public BigDecimal Porcentaje;
        public Long IdAgrupador;
        public String TipoReinversion;
        public String IdSerieFondoOrigen;
        public String SolicitudBatch;
        public BigDecimal PorcentajeReal = BigDecimal.ZERO;

        public InSolicitudesAporte() {
            this.IdAgrupador = 0L;
            this.Porcentaje = new BigDecimal("100");
            this.SolicitudBatch = "N";
        }
    }
}
