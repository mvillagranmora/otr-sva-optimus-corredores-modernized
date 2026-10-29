package cl.bcs.fondomutuo.normal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.kit.MensajeSalida;

/**
 * Input / output classes of legacy {@code CrearSolicitudAporteRescateNormal} (nested classes {@code EntradaCrearSolicitudAporteRescateNormal},
 * {@code InSolicitudesAporteRescate}, {@code InFormaPagoSpot}, {@code SalidaCrearSolicitudAporteRescateNormal}): same public field names, same
 * order and the same constructor defaults, so plain Gson reads and writes the legacy JSON. Shared contract with the Manejador (step 4B).
 */
public final class NormalContrato {

    private NormalContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public String p_IdTipoCanalIngreso;
        public Long p_NumeroDividendo;
        public List<InSolicitud> p_SolicitudesAporteRescate;
        public List<InFormaPagoSpot> p_FormaPagoSpot;
        public String p_IdOrigenProceso;
        public String p_PermiteOperarProspectos;

        public Entrada() {
            p_IdTipoCanalIngreso = "OPT";
            p_SolicitudesAporteRescate = new ArrayList<>();
            p_FormaPagoSpot = new ArrayList<>();
            p_NumeroDividendo = 0L;
            p_IdOrigenProceso = "OPT";
            p_PermiteOperarProspectos = "N";
        }
    }

    public static class InSolicitud {
        public Long IdInterno;
        public Long IdCliente;
        public Long IdPortafolio;
        public String TipoSolicitud;
        public String IdSerieFondo;
        public String CalculoPorMontoCuota;
        public String EsPorMontoLiquido;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public String IdMoneda;
        public String IdMedioDePago;
        public Long IdPreferenciaPagoPortafolio;
        public String Visado;
        public String SolicitudBatch;
        public String OrigenOperacion;
        public String FechaOperacion;
        public String IdRegimenTributario;
        public String Glosa;
        public String IndicadorMLT;
        public String IdSerieFondoDestino;
        public String IdEmisorDestino;
        public Long IdAutorizadoOrigen;
        public BigDecimal Porcentaje;
        public Long FolioContrato;
        public String IdMonedaPago;
        public BigDecimal MontoPatrimonio;
        public String IngresoAutomatico;
        public String ForzarSpot;
        public String TipoContratoOperacion;
        public String FolioContratoOperacion;
        public String EsTraspaso;
        public String Canal;
        public Long IdSolicitudAporteRescateRelacionado;
        public Date FechaValorCuota;
        public String IgnorarMontoMinimo;
        public String SimulaCambioPortafolioEjecutadoOk;
        public String IngresoDesde;
        public Long IdOrigenRecaCliente = 0L;

        public InSolicitud() {
            this.IdMonedaPago = "";
            this.IdEmisorDestino = "";
            this.MontoPatrimonio = BigDecimal.ZERO;
            this.IngresoAutomatico = "N";
            this.ForzarSpot = "N";
            this.TipoContratoOperacion = "";
            this.FolioContratoOperacion = "";
            this.EsTraspaso = "N";
            this.Canal = "";
            this.IdSolicitudAporteRescateRelacionado = 0L;
            this.IgnorarMontoMinimo = "N";
            this.SimulaCambioPortafolioEjecutadoOk = "N";
            this.IngresoDesde = "";
        }
    }

    public static class InFormaPagoSpot {
        public Long IdInternoPagoOperacion;
        public Long IdComprobantePagoOperacion;
        public String IdTipoMovimiento;
        public String IdFormaDePago;
        public String IdMoneda;
        public String IdMonedaLiquidacion;
        public Long IdOperacion;
        public String RecibePagaPortafolio;
        public BigDecimal MontoFormaDePago;
        public String AfectarTesoreria;
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
