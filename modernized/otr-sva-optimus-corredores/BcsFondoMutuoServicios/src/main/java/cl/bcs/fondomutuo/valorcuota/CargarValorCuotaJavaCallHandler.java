package cl.bcs.fondomutuo.valorcuota;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * Direct JavaCall entry of {@code cl.bcs.fondomutuo.CargarValorCuota} (registry id 3035): legacy {@code call(Connection, JsonObject)} with the Gson of
 * {@code Utils.getGsonBuilder()} in and out ({@link CargarValorCuotaJson}, a NEW one per call, nulls written). It WRITES: its unit of work is read-write (the class is NOT in
 * {@code JavaCallDispatcher.CLASES_LECTURA}) and the host commits or rolls back by the error counter. DISABLED BY DEFAULT: unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link CargarValorCuotaConfiguracion#LISTA} the call is a 503 G0 before anything is read or written.
 * <p>ONE FLIGHT PER INSTANCE (decision of the user, architecture review F3 W2): after G0 and before reading anything, a {@link Semaphore}
 * ({@code fondomutuo.valorcuota.max-cargas-simultaneas}, default 1) is taken with a short wait ({@code fondomutuo.valorcuota.espera-ms}, default 2000). Without the
 * permit the call is a 503 gate {@value #COMPUERTA_OCUPADA} (nothing read or written; the router falls to TomEE) and the permit is ALWAYS released in {@code finally}.
 * It only serialises the loads of THIS JVM: with several instances a database lock is needed (not implemented, see RUNBOOK_LISTAS.md).
 */
@Component
public class CargarValorCuotaJavaCallHandler implements JavaCallHandler {

    private final CargarValorCuotaPort servicio;
    private final Compuertas compuertas;
    private final Semaphore vuelo;
    private final long esperaMs;

    /** Gate code of a load turned away because another one is running on this instance. */
    public static final String COMPUERTA_OCUPADA = "CVC_OCUPADA";
    /** Largest integer-digit count and scale magnitude accepted for a VALORCUOTA (M-1: {@code 1E+300000000} would exhaust the CPU in {@code setScale}). */
    static final int MAX_DIGITOS_ENTEROS = 38;
    static final int MAX_ESCALA = 100;

    public CargarValorCuotaJavaCallHandler(CargarValorCuotaPort servicio, Compuertas compuertas) {
        this(servicio, compuertas, 1, 2000L);
    }

    @Autowired
    public CargarValorCuotaJavaCallHandler(CargarValorCuotaPort servicio, Compuertas compuertas,
            @Value("${fondomutuo.valorcuota.max-cargas-simultaneas:1}") int maxCargasSimultaneas,
            @Value("${fondomutuo.valorcuota.espera-ms:2000}") long esperaMs) {
        this.servicio = servicio;
        this.compuertas = compuertas;
        this.vuelo = new Semaphore(Math.max(1, maxCargasSimultaneas));
        this.esperaMs = Math.max(0L, esperaMs);
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CARGAR_VALOR_CUOTA;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        if (!compuertas.listasServidas().contains(CargarValorCuotaConfiguracion.LISTA)) {
            throw new ServicioNoServidoException("G0", "CargarValorCuota is not enabled in this module");
        }
        boolean obtenido;
        try {
            obtenido = vuelo.tryAcquire(esperaMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServicioNoServidoException(COMPUERTA_OCUPADA, "interrupted while waiting for the load permit");
        }
        if (!obtenido) {
            throw new ServicioNoServidoException(COMPUERTA_OCUPADA, "another CargarValorCuota is running on this instance");
        }
        try {
            Gson gson = CargarValorCuotaJson.gson();
            CargarValorCuotaContrato.Entrada jin = gson.fromJson(in.toString(), CargarValorCuotaContrato.Entrada.class);
            validarMagnitudes(jin);
            CargarValorCuotaContrato.Salida out = servicio.logica(jin);
            return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
        } finally {
            vuelo.release();
        }
    }

    /** M-1: rejects (generic 500, no value in the message) a VALORCUOTA whose exponent would make the scale arithmetic explode. */
    private static void validarMagnitudes(CargarValorCuotaContrato.Entrada jin) {
        if (jin == null || jin.p_ValorCuota == null) return;
        for (CargarValorCuotaContrato.ValorCuota v : jin.p_ValorCuota) {
            BigDecimal valor = v == null ? null : v.VALORCUOTA;
            if (valor != null && ((long) valor.precision() - valor.scale() > MAX_DIGITOS_ENTEROS || Math.abs((long) valor.scale()) > MAX_ESCALA)) {
                throw new IllegalArgumentException("VALORCUOTA out of range (precision " + valor.precision() + ", scale " + valor.scale() + ")");
            }
        }
    }
}
