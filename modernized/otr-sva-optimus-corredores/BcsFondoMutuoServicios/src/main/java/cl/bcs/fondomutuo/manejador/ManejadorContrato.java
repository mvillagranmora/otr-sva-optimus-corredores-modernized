package cl.bcs.fondomutuo.manejador;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.normal.NormalContrato;

/**
 * Input / output of legacy {@code ManejadorSolicitudAporteRescate} (nested classes {@code EntradaManejadorSolicitudAporteRescate} and
 * {@code SalidaManejadorSolicitudAporteRescate}): same public field names, same order and same constructor defaults, so the plain Gson of the legacy
 * ({@code new Gson()}, no {@code serializeNulls}) reads and writes the legacy JSON. The ten lists start EMPTY (constructor) but
 * {@code p_FormaPagoSpot} does NOT (it stays {@code null} when the key is missing: D-MAN-2). The list of Normal is typed with the shared contract of
 * step 4A; the lists of the seven other destinations (not served in F1, they answer "no servido" behind gate G0) are kept as raw JSON objects: only
 * their {@code IdInterno} is read by the facade, the destination classes of F2 will bring their own types.
 */
public final class ManejadorContrato {

    private ManejadorContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;      // read and IGNORED (FM-MAN-08): every pass sets "S" / "N" itself
        public String p_UsaReglasNegocio;
        public String p_IdTipoCanalIngreso;

        public List<NormalContrato.InSolicitud> p_SolicitudesAporteRescateNormal;
        public List<JsonObject> p_SolicitudesAporteRescateProgramado;
        public List<JsonObject> p_SolicitudAporteOvernight;
        public List<JsonObject> p_SolicitudAporteReinversion;
        public List<JsonObject> p_SolicitudCambioSerieRescate;
        public List<JsonObject> p_SolicitudCambioSerieAporte;
        public List<JsonObject> p_SolicitudRescateReinversionRescate;
        public List<JsonObject> p_SolicitudRescateReinversionAporte;
        public List<JsonObject> p_SolicitudRescateTransferencia;
        public List<JsonObject> p_SolicitudRescateTraspaso;

        public Entrada() {
            this.p_SolicitudesAporteRescateNormal = new ArrayList<>();
            this.p_SolicitudesAporteRescateProgramado = new ArrayList<>();
            this.p_SolicitudAporteOvernight = new ArrayList<>();
            this.p_SolicitudAporteReinversion = new ArrayList<>();
            this.p_SolicitudCambioSerieRescate = new ArrayList<>();
            this.p_SolicitudCambioSerieAporte = new ArrayList<>();
            this.p_SolicitudRescateReinversionRescate = new ArrayList<>();
            this.p_SolicitudRescateReinversionAporte = new ArrayList<>();
            this.p_SolicitudRescateTransferencia = new ArrayList<>();
            this.p_SolicitudRescateTraspaso = new ArrayList<>();
        }

        public List<NormalContrato.InFormaPagoSpot> p_FormaPagoSpot;   // NOT initialised: null when absent (D-MAN-2)
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public List<ResultadoSolicitud> P_OUT_RESULTADO;

        public Salida() {
            P_OUT_RESULTADO = new ArrayList<>();
            P_OUT_MENSAJES = new ArrayList<>();
            P_OUT_ERRORESCONTADOR = 0L;
        }
    }
}
