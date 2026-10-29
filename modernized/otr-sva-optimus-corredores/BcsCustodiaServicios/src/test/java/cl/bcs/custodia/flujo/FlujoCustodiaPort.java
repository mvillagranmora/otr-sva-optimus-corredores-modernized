package cl.bcs.custodia.flujo;

import java.sql.Connection;

import com.google.gson.JsonObject;

/**
 * TEST-FACING CONTRACT of the slice cl.bcs.custodia.ConsultarFlujoCustodia (RULES_CUSTODIA_FLUJO.md CUS-FLU-01..36): the JavaCall entry point
 * {@code call(Connection, JsonObject)} of the legacy (ConsultarFlujoCustodia.java:916-924). The new implementation is NOT referenced at compile
 * time (src/main does not have it yet); {@link FlujoCustodiaContrato} finds it by reflection and adapts it to this interface:
 *
 * <pre>
 * public class cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo {        // override with -Dcustodia.flujo.impl=&lt;FQCN&gt;
 *     public ConsultarFlujoCustodiaFlujo(java.util.function.Function&lt;JsonObject, JsonObject&gt; valorizador,
 *                                        java.util.function.BiFunction&lt;String, Long, java.util.List&lt;Long&gt;&gt; secuencias)
 *     public JsonObject call(java.sql.Connection con, JsonObject entrada) throws Exception
 * }
 * </pre>
 * <ul>
 * <li>{@code entrada}: the legacy request JSON (FlujoCustodiaEntrada field names). The returned JSON must be byte-identical to the legacy output
 *     text (field order of FlujoCustodiaSalida, null fields present, BigDecimal scale, dates as "yyyy-MM-dd HH:mm:ss.SSS" in UTC, list order).</li>
 * <li>{@code valorizador}: the ValorizarInstrumento port (out of this slice, CUS-FLU-33..35). It receives the request as the JSON of the legacy
 *     ValorizarInstrumentoEntrada (every field, nulls included; compared key-order-insensitively) and returns the ValorizarInstrumentoSalida JSON.
 *     It may throw (any Throwable, also a checked java.lang.Exception thrown "sneakily"): propagate it unchanged.</li>
 * <li>{@code secuencias}: Utils.getSecuencia(con, name, quantity) (CUS-FLU-36): name and quantity are recorded and compared.</li>
 * <li>{@code con}: an H2 connection seen through the harness dialect bridge (cl.bcs.custodia.flujo.h2.HanaH2): issue the SAME SQL text as the
 *     legacy (src/cl/bcs/custodia/sql/FlujoCustodia_*.sql and the Plataforma SQL of ConsultarEmpresa, ConfiguracionSistema, Diccionario,
 *     ObtenerDiaSiguiente). Do not commit; write and delete CUS.FlujoCustodia_MatrizUso as the legacy does (CUS-FLU-14).</li>
 * </ul>
 */
@FunctionalInterface
public interface FlujoCustodiaPort {

    JsonObject call(Connection con, JsonObject entrada) throws Exception;
}
