package cl.bcs.contabilidad.jobs.adapter;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Step 5 (JOBS_PATTERN.md H-1, JOBS_H1_H4_INVENTORY.md section A.3 group M): the {@link PlataformaPort} of the job application when
 * {@code contabilidad.jobs.colaboradores-en-proceso=true}.
 *
 * <p>The only group M collaborator the Contabilidad services reach is Tesoreria's {@code ConsultarClearingFinanciero}
 * ({@code CentralizaClearingFinanciero}, legacy {@code ConsultarClearingFinanciero.logica(con, ...)}). Tesoreria is already migrated and packaged
 * as a library this application carries, so it runs IN PROCESS through Tesoreria's own entry point ({@link DespachadorEnProceso}) and joins the
 * transaction the {@code JobRunner} opened for the run (same {@code DataSource}; the Tesoreria service is {@code @Transactional} REQUIRED), as the
 * legacy did when it handed the service its own {@code java.sql.Connection}. The call used to be {@code POST /api/tesoreria/ConsultarClearingFinanciero}
 * to TomEE ({@code TomEePlataformaClient}).
 *
 * <p>Everything else is delegated, untouched, to {@code TomEePlataformaClient}: company data and configuration parameters (JDBC on the caller's
 * connection, no HTTP) and {@code ObtenerDiaAnterior} (Plataforma, still TomEE, HTTP).
 */
public class PlataformaEnProceso implements PlataformaPort {

    /** Legacy class of the collaborator ({@code cl.bcs.tesoreria.liq.ConsultarClearingFinanciero}); Tesoreria's dispatcher knows it by this name. */
    public static final String CLEARING_FINANCIERO = "cl.bcs.tesoreria.liq.ConsultarClearingFinanciero";

    /** Chambers the legacy CentralizaClearingFinanciero asks for (entradaClearingFinanciero.p_Camaras), in its order. */
    static final List<String> CAMARAS = List.of("PM", "RV", "SM", "PH", "HS");

    private final PlataformaPort http;
    private final DespachadorEnProceso despachador;
    private final TransactionTemplate unidadAnidada;

    /**
     * @param http        the existing port ({@code TomEePlataformaClient}): everything that is not group M goes to it, and so does the clearing
     *                    when no in-process owner serves it
     * @param despachador the in-process entry points of the owner libraries
     * @param transacciones the transaction manager of the job application (see {@link #consultarClearingFinanciero})
     */
    public PlataformaEnProceso(PlataformaPort http, DespachadorEnProceso despachador, PlatformTransactionManager transacciones) {
        this.http = Objects.requireNonNull(http, "http");
        this.despachador = Objects.requireNonNull(despachador, "despachador");
        // NESTED, as ContabilidadJdbcRepository.persistirCentralizacion: a savepoint inside the transaction of the run (REQUIRED when there is none)
        this.unidadAnidada = new TransactionTemplate(Objects.requireNonNull(transacciones, "transacciones"),
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED));
    }

    @Override
    public List<ClearingFinancieroDetalle> consultarClearingFinanciero(
            Long idEmpresa, Long idUsuario, String idModulo, String fechaLiquidacion) {
        if (!despachador.maneja(CLEARING_FINANCIERO)) {
            return http.consultarClearingFinanciero(idEmpresa, idUsuario, idModulo, fechaLiquidacion);
        }
        JsonObject entrada = new JsonObject();
        entrada.addProperty("p_IdEmpresa", idEmpresa);
        entrada.addProperty("p_IdUsuario", idUsuario);
        entrada.addProperty("p_IdModulo", idModulo != null ? idModulo : "");
        entrada.addProperty("p_UsaReglasNegocio", "S");
        entrada.addProperty("p_FechaLiquidacion", fechaLiquidacion);
        JsonArray camaras = new JsonArray();
        for (String camara : CAMARAS) {
            JsonObject c = new JsonObject();
            c.addProperty("Camara", camara);
            camaras.add(c);
        }
        entrada.add("p_Camaras", camaras);

        // The call joins the transaction of the run through a savepoint. Why: Tesoreria's service is @Transactional REQUIRED, and when it throws the
        // proxy would mark the WHOLE job transaction rollback-only, while CentralizaMultiSeleccionService swallows the RuntimeException of a type
        // (legacy: one connection, the failure of one type did not undo the others). The savepoint is rolled back (and the flag reset) when the service
        // throws, so only that type is affected; the exception itself reaches the caller unchanged. Same pattern as persistirCentralizacion.
        JsonObject salida = unidadAnidada.execute(estado -> despachador.despachar(CLEARING_FINANCIERO, entrada));
        return detalles(salida);
    }

    /**
     * {@code P_OUT_CLEARINGFINANCIERODETALLE} of the Tesoreria answer -> the rows the port returns. Same conversion as the HTTP adapter (absent or null
     * id -> 0, text -> "", amount -> 0), so what the centralizer sees does not depend on the path. The legacy ignores {@code P_OUT_MENSAJES} too.
     */
    static List<ClearingFinancieroDetalle> detalles(JsonObject salida) {
        List<ClearingFinancieroDetalle> filas = new ArrayList<>();
        JsonElement detalle = salida == null ? null : salida.get("P_OUT_CLEARINGFINANCIERODETALLE");
        if (detalle == null || !detalle.isJsonArray()) {
            return filas;
        }
        for (JsonElement fila : detalle.getAsJsonArray()) {
            if (!fila.isJsonObject()) {
                continue;
            }
            JsonObject r = fila.getAsJsonObject();
            filas.add(new ClearingFinancieroDetalle(
                    largo(r, "IdTransaccion"),
                    largo(r, "IdComprobanteTesoreria"),
                    texto(r, "FolioTransaccion"),
                    monto(r, "TotalVenta"),
                    monto(r, "TotalCompra"),
                    texto(r, "IdMonedaTransaccion"),
                    texto(r, "RutContraparte"),
                    texto(r, "RazonSocialContraparte"),
                    texto(r, "CompraVenta"),
                    texto(r, "IdClasificacionTipoInstrumento"),
                    texto(r, "NombreGrupoCamara"),
                    texto(r, "IdCondicionDeLiquidacion"),
                    texto(r, "IdBolsa"),
                    texto(r, "FechaLiquidacion")));
        }
        return filas;
    }

    private static Long largo(JsonObject r, String campo) {
        JsonElement v = r.get(campo);
        if (v != null && v.isJsonPrimitive() && v.getAsJsonPrimitive().isNumber()) {
            return v.getAsLong();
        }
        return 0L;
    }

    private static String texto(JsonObject r, String campo) {
        JsonElement v = r.get(campo);
        return v == null || v.isJsonNull() ? "" : v.getAsString();
    }

    private static BigDecimal monto(JsonObject r, String campo) {
        JsonElement v = r.get(campo);
        if (v == null || v.isJsonNull()) {
            return BigDecimal.ZERO;
        }
        try {
            return v.getAsBigDecimal();
        } catch (RuntimeException e) {
            return BigDecimal.ZERO;
        }
    }

    // ---- everything else: the existing port, untouched

    @Override
    public EmpresaInfo consultarEmpresa(Long idEmpresa) {
        return http.consultarEmpresa(idEmpresa);
    }

    @Override
    public String obtenerDiaAnterior(Long idPais, String fechaContable) {
        return http.obtenerDiaAnterior(idPais, fechaContable);
    }

    @Override
    public String obtenerFechaSistema(Long idEmpresa) {
        return http.obtenerFechaSistema(idEmpresa);
    }

    @Override
    public String obtenerLocalizacionSistema(Long idEmpresa) {
        return http.obtenerLocalizacionSistema(idEmpresa);
    }

    @Override
    public String obtenerMonedaSistema(Long idEmpresa) {
        return http.obtenerMonedaSistema(idEmpresa);
    }

    @Override
    public String obtenerParametroConfiguracion(Long idEmpresa, String modulo, String parametro, String valorDefault) {
        return http.obtenerParametroConfiguracion(idEmpresa, modulo, parametro, valorDefault);
    }
}
