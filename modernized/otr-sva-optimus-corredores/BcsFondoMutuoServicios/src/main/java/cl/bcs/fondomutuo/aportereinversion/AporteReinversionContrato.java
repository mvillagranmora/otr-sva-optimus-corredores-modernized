package cl.bcs.fondomutuo.aportereinversion;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.kit.MensajeSalida;

/**
 * Input / output classes of legacy {@code CrearSolicitudAporteReinversion} (nested classes {@code EntradaCrearSolicitudAporteReinversion}, {@code InSolicitudesAporte},
 * {@code ConsultaRegimenReinversion}, {@code SalidaCrearSolicitudAporteReinversion}; FM-REI-41): same public field names, order and constructor defaults, so plain Gson reads and
 * writes the legacy JSON. Shared contract with Certificar ({@code crearAporteReinversion}), the Manejador and ProcesarCarga.
 */
public final class AporteReinversionContrato {

    private AporteReinversionContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public String p_IdTipoCanalIngreso;
        public List<InSolicitudesAporte> p_SolicitudesAporte;

        public Entrada() {
            p_SolicitudesAporte = new ArrayList<>();
        }
    }

    public static class InSolicitudesAporte {
        public Long IdInterno;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdSerieFondoOrigen;
        public String IdEmisorOrigen;
        public BigDecimal CostoUF;
        public String FechaOrigenInversion;
        public BigDecimal MontoOrigen;
        public String IndicadorMLT;
        public String IdSerieFondo;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public String IdMoneda;
        public String IdMedioDePago;
        public String Visado;
        public String SolicitudBatch;
        public String OrigenOperacion;
        public String IdRegimenTributario;
        public String Glosa;
        public Long IdAutorizadoOrigen;
        public Long FolioContrato;
        public Long IdSolicitudAporteRescateRelacionado;
        public String FechaEmision;
        public BigDecimal Porcentaje;
        public Long IdAgrupador;
        public String FechaCertificacionRescateOrigen;
        public String FechaLiquidacionRescateOrigen;
        public String FechaValorCuotaRescateOrigen;
        public String FechaCertificaSpotCompra;
        public String FechaLiquidaSpotCompra;
        public String Switch;
        public String GeneraSpot;
        public BigDecimal MontoPatrimonio;
        public String Canal;
        public String IngresoAutomatico;
        public BigDecimal MontoTotalInversion;
        public Date FechaValorCuota;
        /** SWITCH, INTERNACIONAL, NACIONAL or VALUTASNACUSD: set by the class from the origin and destination series (the callers read it back). */
        public String TipoReinversion = "";

        public InSolicitudesAporte() {
            this.Switch = "N";
            this.IdAgrupador = 0L;
            this.Porcentaje = new BigDecimal("100");
            this.GeneraSpot = "N";
            this.MontoPatrimonio = BigDecimal.ZERO;
            this.IngresoAutomatico = "N";
            this.MontoTotalInversion = BigDecimal.ZERO;
            this.FechaCertificacionRescateOrigen = "0001-01-01";
            this.FechaLiquidacionRescateOrigen = "0001-01-01";
            this.FechaValorCuotaRescateOrigen = "0001-01-01";
            this.CostoUF = BigDecimal.ZERO;
        }
    }

    public static class ConsultaRegimenReinversion {
        public Long IdSolicitudAporteRescate;
        public String IdRegimenTributario;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public List<ResultadoSolicitud> P_OUT_RESULTADO;

        public Salida() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_RESULTADO = new ArrayList<>();
        }
    }
}
