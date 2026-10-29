package cl.bcs.normativa.svc.service.ils;

import cl.bcs.normativa.svc.domain.ils.CalcularObligacionesEntrada;
import cl.bcs.normativa.svc.domain.ils.CalcularObligacionesSalida;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class IlsService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Gson GSON = new Gson();

    private final CalcularObligacionesDerechosMonedaExtranjeraService calcularObligacionesSvc;
    private final DetalleCoberturaILSService detalleCoberturaILSSvc;
    private final GeneraReporteILSService generaReporteILSSvc;
    private final HomologarCuentasContablesService homologarCuentasContablesSvc;

    public IlsService(
            CalcularObligacionesDerechosMonedaExtranjeraService calcularObligacionesSvc,
            DetalleCoberturaILSService detalleCoberturaILSSvc,
            GeneraReporteILSService generaReporteILSSvc,
            HomologarCuentasContablesService homologarCuentasContablesSvc) {
        this.calcularObligacionesSvc    = calcularObligacionesSvc;
        this.detalleCoberturaILSSvc     = detalleCoberturaILSSvc;
        this.generaReporteILSSvc        = generaReporteILSSvc;
        this.homologarCuentasContablesSvc = homologarCuentasContablesSvc;
    }

    public static String calcularFechaHasta(String fechaContable) {
        if (fechaContable == null || fechaContable.isBlank()) {
            throw new IllegalArgumentException("fechaContable no puede ser nula o vacía");
        }
        return LocalDate.parse(fechaContable, FMT).plusDays(7).format(FMT);
    }

    public CalcularObligacionesSalida calcularObligaciones(CalcularObligacionesEntrada entrada) {
        JsonObject params = new JsonObject();
        params.addProperty("p_IdEmpresa", entrada.idEmpresa);
        params.addProperty("p_fechaContable", entrada.fechaContable);
        JsonObject result = calcularObligacionesSvc.execute(params);
        CalcularObligacionesSalida out = new CalcularObligacionesSalida();
        out.erroresContador = result.has("P_OUT_ERRORESCONTADOR")
                ? result.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
        out.mensaje = result.has("P_OUT_MENSAJE")
                ? result.get("P_OUT_MENSAJE").getAsString() : "";
        return out;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> detalleCoberturaIls(Map<String, Object> parametros) {
        JsonObject params = GSON.toJsonTree(parametros).getAsJsonObject();
        JsonObject result = detalleCoberturaILSSvc.execute(params);
        return GSON.fromJson(result, Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> generaReporteILS(Map<String, Object> parametros) {
        JsonObject params = GSON.toJsonTree(parametros).getAsJsonObject();
        JsonObject result = generaReporteILSSvc.execute(params);
        return GSON.fromJson(result, Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> homologarCuentasContables(Map<String, Object> parametros) {
        JsonObject params = GSON.toJsonTree(parametros).getAsJsonObject();
        JsonObject result = homologarCuentasContablesSvc.execute(params);
        return GSON.fromJson(result, Map.class);
    }
}
