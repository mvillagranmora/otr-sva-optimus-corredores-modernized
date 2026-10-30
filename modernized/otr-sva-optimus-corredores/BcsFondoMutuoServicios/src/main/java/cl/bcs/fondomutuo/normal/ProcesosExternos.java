package cl.bcs.fondomutuo.normal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.MensajeSalida;

/**
 * The two classes of the Normal closure that are NOT ported in step 4A (F2, plan 5.1 / 9): {@code ProcesarIngresoEgresoTesoreria} (Tesoreria
 * {@code CrearIngresoEgreso}, gate G1) and {@code ProcesarIngresoOperacionSpot} (Mesa {@code CrearOperacionSpot}, gate G2). The shapes below are the
 * legacy input / output the Normal reads (same field names). Until F2 gives them an implementation, the default beans answer {@code
 * ServicioNoServidoException} (503, the router repeats the request in TomEE) - but the gate is evaluated BEFORE the call, so with the gates closed
 * (default) the port is never reached.
 */
public final class ProcesosExternos {

    private ProcesosExternos() {
    }

    public static class EntradaTesoreria {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public String p_EsIngresoAutomatico;
        public List<SolicitudAporteRescate> p_Solicitudes = new ArrayList<>();
    }

    public static class SalidaTesoreria {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<ResultadoProceso> P_OUT_RESULTADOPROCESO = new ArrayList<>();
        public List<IngresoEgresoGenerado> P_OUT_INGRESOEGRESOGENERADO = new ArrayList<>();
    }

    public static class ResultadoProceso {
        public Long Id;
        public String Campo;
        public String Resultado;
    }

    public static class IngresoEgresoGenerado {
        public Long NumeroReferencia;
        public Long IdIngresoEgreso;
    }

    public static class FormaPagoSpot {
        public String AfectarTesoreria;
        public Long IdComprobantePagoOperacion;
        public String IdFormaDePago;
        public Long IdInternoPagoOperacion;
        public String IdMoneda;
        public String IdMonedaLiquidacion;
        public Long IdOperacion;
        public String IdTipoMovimiento;
        public BigDecimal MontoFormaDePago;
        public String RecibePagaPortafolio;
        public java.util.Date FechaLiquidacion;
    }

    public static class EntradaSpot {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_CalculaMargen;
        public String p_UsaReglasNegocio;
        /** The default of the legacy class; Normal always sets its own value before the call. */
        public String p_PermiteOperarProspectos = "N";
        public List<SolicitudAporteRescate> p_DatosOperacion = new ArrayList<>();
        public List<FormaPagoSpot> p_DatosFormaPago = new ArrayList<>();
    }

    public static class SalidaSpot {
        /** Null when the answer has none: the legacy reader only adds it {@code if != null}. */
        public Long P_OUT_ERRORESCONTADOR;
        public List<OperacionSpot> P_OUT_IDOPERACIONSPOT = new ArrayList<>();
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class OperacionSpot {
        public String IdInterno;
        public String Estado;
        public Long IdOperacionSpot;
    }

    /** Legacy {@code ProcesarIngresoEgresoTesoreria.logica(con, entrada)} (F2: Tesoreria library in process, plan 12). */
    @FunctionalInterface
    public interface TesoreriaPort {
        SalidaTesoreria procesar(EntradaTesoreria entrada) throws Exception;
    }

    /** Legacy {@code ProcesarIngresoOperacionSpot.logica(con, entrada)} (F2: Mesa Spot). */
    @FunctionalInterface
    public interface SpotPort {
        SalidaSpot procesar(EntradaSpot entrada) throws Exception;
    }
}
