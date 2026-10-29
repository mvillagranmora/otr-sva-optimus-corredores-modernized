package cl.bcs.contabilidad.infrastructure.plataforma;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.service.FuncionalidadNoMigradaException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * {@link PlataformaPort} adapter during the strangler-fig migration.
 *
 * <p>Configuration parameters and company data are read from HANA on the caller's connection and transaction,
 * like the legacy {@code ConfiguracionSistema} and {@code ConsultarEmpresa} did ({@link PlataformaConfiguracionJdbc}).
 * Only "parameter absent" yields the caller's default; a database error propagates. Business-day arithmetic and
 * the Tesorería clearing query stay on TomEE over HTTP ({@link PlataformaHttp}: explicit timeouts, and any
 * failure becomes a 503 fallback instead of a silent default). The base URL comes from {@code PLATAFORMA_BASE_URL};
 * no credentials are held here.
 */
@Component
public class TomEePlataformaClient implements PlataformaPort {

    private final PlataformaHttp http;
    private final PlataformaConfiguracionJdbc configuracion;
    private final Gson gson;

    public TomEePlataformaClient(
            @Value("${plataforma.base-url:${PLATAFORMA_BASE_URL:http://localhost:8080}}") String baseUrl,
            NamedParameterJdbcTemplate jdbc) {
        this.http = new PlataformaHttp(baseUrl);
        this.configuracion = new PlataformaConfiguracionJdbc(jdbc);
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeNulls()
                .create();
    }

    @Override
    public EmpresaInfo consultarEmpresa(Long idEmpresa) {
        return configuracion.empresa(idEmpresa);
    }

    @Override
    public String obtenerDiaAnterior(Long idPais, String fechaContable) {
        JsonObject request = new JsonObject();
        request.addProperty("idPais", idPais);
        request.addProperty("fechaInicial", fechaContable);
        request.addProperty("numeroDias", 1);
        request.addProperty("diaHabil", "S");
        request.addProperty("soloCaeDiaHabil", "S");

        String responseBody = http.post("/api/plataforma/ObtenerDiaAnterior", request.toString());
        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
        if (json == null || !json.has("fechaCalculada") || json.get("fechaCalculada").isJsonNull()) {
            throw new FuncionalidadNoMigradaException("ObtenerDiaAnterior: respuesta sin fechaCalculada");
        }
        return json.get("fechaCalculada").getAsString();
    }

    @Override
    public String obtenerFechaSistema(Long idEmpresa) {
        return configuracion.parametroRequerido(idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
    }

    @Override
    public String obtenerLocalizacionSistema(Long idEmpresa) {
        return configuracion.parametroRequerido(idEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
    }

    @Override
    public String obtenerMonedaSistema(Long idEmpresa) {
        return configuracion.parametroRequerido(idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
    }

    @Override
    public String obtenerParametroConfiguracion(Long idEmpresa, String modulo, String parametro, String valorDefault) {
        return configuracion.parametro(idEmpresa, modulo, parametro).orElse(valorDefault);
    }

    @Override
    public List<ClearingFinancieroDetalle> consultarClearingFinanciero(
            Long idEmpresa, Long idUsuario, String idModulo, String fechaLiquidacion) {
        JsonObject request = new JsonObject();
        request.addProperty("idEmpresa", idEmpresa);
        request.addProperty("idUsuario", idUsuario);
        request.addProperty("idModulo", idModulo != null ? idModulo : "");
        request.addProperty("fechaLiquidacion", fechaLiquidacion);

        String responseBody = http.post("/api/tesoreria/ConsultarClearingFinanciero", request.toString());

        List<java.util.Map<String, Object>> rows = gson.fromJson(responseBody,
                new TypeToken<List<java.util.Map<String, Object>>>() {}.getType());
        if (rows == null) return List.of();

        return rows.stream().map(r -> new ClearingFinancieroDetalle(
                toLong(r.get("IdTransaccion")),
                toLong(r.get("IdComprobanteTesoreria")),
                str(r.get("FolioTransaccion")),
                toBD(r.get("TotalVenta")),
                toBD(r.get("TotalCompra")),
                str(r.get("IdMonedaTransaccion")),
                str(r.get("RutContraparte")),
                str(r.get("RazonSocialContraparte")),
                str(r.get("CompraVenta")),
                str(r.get("IdClasificacionTipoInstrumento")),
                str(r.get("NombreGrupoCamara")),
                str(r.get("IdCondicionDeLiquidacion")),
                str(r.get("IdBolsa")),
                str(r.get("FechaLiquidacion"))
        )).toList();
    }

    private static Long toLong(Object v) {
        if (v instanceof Number n) return n.longValue(); return 0L;
    }
    private static String str(Object v) { return v == null ? "" : v.toString(); }
    private static BigDecimal toBD(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
