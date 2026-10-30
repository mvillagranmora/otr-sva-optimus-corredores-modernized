package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.kit.MensajeSalida;

/**
 * The comparison of a T3 case (see {@link CasoCerT3} and API_ASSUMED_T3.md 0): what F-C..F-H DECIDED (the five steps of T2 plus the cuenta de inversion, Tesoreria and Spot), seen in what the
 * RECORDED legacy did.
 *
 * <p>The golden of a T3 case is the WHOLE legacy run with benign collaborators after F-H (facturacion, reinversion, switch, notifications and log are T4 / T5). The decisions are read in:
 * <ol>
 * <li>the CALLS to the collaborators that F-C..F-H reach ({@link #COLABORADORES_T3}: the cash flow, the custody calculation / entry / retirement, the commission, the UF / UTM parities,
 *     and NOW {@code CrearCargoAbono}, {@code ProcesarIngresoEgresoTesoreria} and {@code ProcesarIngresoOperacionSpot}), with their COMPLETE arguments and the answers they got, in order: this
 *     pins every line of the cuenta de inversion, every field of the pojo that Tesoreria and Spot receive (the legacy copies the WHOLE solicitud, nulls included) and the payment forms of Spot
 *     (the numbers are compared by value, not by scale);</li>
 * <li>the output: state and code of every solicitud, the description of the rejections F-D writes, the counter and the messages that the collaborators add (the counter of Tesoreria is NOT
 *     read by the legacy: D-F3B-03);</li>
 * <li>the columns the UPDATE to CRT wrote (amounts, cost UF, spread, real profitability, cartera, commission, {@code IdIngresoEgreso}, {@code IdOperacionSpot}) of every solicitud that survived.</li>
 * </ol>
 * It reads the golden and the {@link CertificarAporteRescateContexto} that F-C..F-H left, and fails if they disagree. It does NOT restate the rules (that is what the new code is for);
 * {@code CerT3ReglasTest} pins them with literals read in the golden. ONE normalisation, documented: the key {@code p_DatosOrigenInversion} of the Spot entry (always an empty list in
 * Certificar, the default of the legacy class) is removed from both sides when it is empty: the typed {@code ProcesosExternos.EntradaSpot} of the module does not declare it.
 */
final class CerCosturaT3 {

    /** Collaborators that F-C..F-H call (CalcularComisiones, the real one, also reads the company, the custody flow and the quota value). Everything else is a later front. */
    static final Set<String> COLABORADORES_T3 = Set.of("SaldoFlujoCaja", "CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo", "ConsultarFlujoCustodia", "ConsultarEmpresa", "CalcularParidad",
            "EntregarPreciosTasas", "CrearCargoAbono", "ProcesarIngresoEgresoTesoreria", "ProcesarIngresoOperacionSpot");

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static final String T_SOL = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";

    private CerCosturaT3() {
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // entry
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    static void comparar(String id, JsonObject golden, CasoCerT3.Corrida corrida) {
        TimeZone zona = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            compararEnUtc(id, golden, corrida);
        } finally {
            TimeZone.setDefault(zona);
        }
    }

    private static void compararEnUtc(String id, JsonObject golden, CasoCerT3.Corrida c) {
        CasoCerT3.Modo modo = CasoCerT3.modo(id);
        assertNotNull(c.contexto, id + " :: the facade never reached the seam (F-A / F-B); it ended with " + c.excepcion);
        JsonObject nuevo = c.volcado;
        // 1. the seed is the one of the golden; F-C..F-H read and call collaborators but WRITE nothing (the UPDATE, the log and the sequences are front J)
        assertEquals(txt(golden.get("tablasIniciales")), txt(nuevo.get("tablasIniciales")), id + " :: tablasIniciales");
        assertEquals(txt(nuevo.get("tablasIniciales")), txt(nuevo.get("tablasTrasLaLlamada")), id + " :: F-C..F-H must not write");
        assertEquals("[]", txt(nuevo.get("secuenciasSolicitadas")), id + " :: F-C..F-H consume no sequence");
        // 2. the collaborator calls: the prefix of F-A / F-B (T1) and then the ones of F-C..F-H, with their complete arguments and answers
        JsonArray gcols = golden.getAsJsonArray("colaboradores");
        JsonArray ncols = nuevo.getAsJsonArray("colaboradores");
        int n = c.llamadasAntesDelSeam;
        assertTrue(n >= 0 && n <= gcols.size(), id + " :: calls before the seam " + n + " of " + gcols.size());
        assertEquals(txtN(simples(trozo(gcols, 0, n))), txtN(simples(trozo(ncols, 0, n))), id + " :: calls of F-A / F-B (T1 prefix)");
        assertEquals(txtN(filtrar(gcols, n)), txtN(filtrar(ncols, n)), id + " :: calls to the collaborators of F-C..F-H (name, complete arguments, answer, order)");
        // 3. how the run ended
        if (modo == CasoCerT3.Modo.EXCEPCION) {
            assertTrue(golden.has("excepcion"), id + " :: tagged T3-costura:excepcion but the golden ends normally");
            assertTrue(c.excepcion != null && !(c.excepcion instanceof ParadaT3), id + " :: F-C..F-H must raise " + golden.get("excepcion") + " but they finished");
            compararExcepcion(id, golden.get("excepcion").getAsString(), CasoCer.texto(c.excepcion));
            return;
        }
        assertTrue(c.terminoF_C_F_D(), id + " :: F-C..F-H must finish without an exception; it raised " + c.excepcion);
        if (modo == CasoCerT3.Modo.EXCEPCION_POSTERIOR) {
            assertTrue(golden.has("excepcion"), id + " :: tagged T3-costura:excepcion-posterior but the golden ends normally");
            return;   // the legacy raised AFTER F-H (a later front): the calls above are all that the golden shows of F-C..F-H
        }
        assertFalse(golden.has("excepcion"), id + " :: the completo golden must not end in an exception: " + golden.get("excepcion"));
        CertificarAporteRescateContexto ctx = c.contexto;
        compararSalida(id, ctx, golden);
        compararFilasActualizadas(id, ctx, golden);
    }

    static void compararExcepcion(String id, String esperada, String real) {
        String ec = esperada.substring(0, esperada.indexOf(':'));
        String rc = real.substring(0, real.indexOf(':'));
        if (ec.equals("Exception")) {
            assertEquals(esperada.substring(esperada.indexOf(':') + 1), real.substring(real.indexOf(':') + 1), id + " :: exception message");
        } else {
            assertEquals(ec, rc, id + " :: exception class (" + real + ")");
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the calls
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    static JsonArray trozo(JsonArray a, int desde, int hasta) {
        JsonArray out = new JsonArray();
        for (int i = desde; i < hasta && i < a.size(); i++) out.add(a.get(i));
        return out;
    }

    /** The calls from index {@code desde} on that belong to F-C..F-H, as {colaborador, argumentos, respuesta}. */
    static JsonArray filtrar(JsonArray cols, int desde) {
        JsonArray out = new JsonArray();
        for (int i = desde; i < cols.size(); i++) {
            JsonObject o = cols.get(i).getAsJsonObject();
            if (COLABORADORES_T3.contains(o.get("colaborador").getAsString())) out.add(normalizar(o));
        }
        return simples(out);
    }

    /** The Spot entry key the typed class of the module does not declare (see the class comment): removed when it is an empty list, in the golden and in the new code. */
    private static JsonObject normalizar(JsonObject llamada) {
        if (!"ProcesarIngresoOperacionSpot".equals(llamada.get("colaborador").getAsString())) return llamada;
        JsonObject copia = llamada.deepCopy();
        for (JsonElement a : copia.getAsJsonArray("argumentos")) {
            if (a.isJsonObject()) {
                JsonObject o = a.getAsJsonObject();
                if (o.has("p_DatosOrigenInversion") && o.get("p_DatosOrigenInversion").isJsonArray() && o.getAsJsonArray("p_DatosOrigenInversion").isEmpty()) o.remove("p_DatosOrigenInversion");
            }
        }
        return copia;
    }

    static JsonArray simples(JsonArray colaboradores) {
        JsonArray n = new JsonArray();
        for (JsonElement x : colaboradores) {
            JsonObject o = x.getAsJsonObject();
            JsonObject m = new JsonObject();
            m.add("colaborador", o.get("colaborador"));
            m.add("argumentos", o.get("argumentos"));
            m.add("respuesta", o.get("respuesta"));
            n.add(m);
        }
        return n;
    }

    static List<JsonObject> llamadas(JsonArray cols, String nombre) {
        List<JsonObject> out = new ArrayList<>();
        for (JsonElement e : cols) {
            if (e.getAsJsonObject().get("colaborador").getAsString().equals(nombre)) out.add(e.getAsJsonObject());
        }
        return out;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the output
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static void compararSalida(String id, CertificarAporteRescateContexto ctx, JsonObject golden) {
        JsonObject salida = JsonParser.parseString(golden.get("salidaCruda").getAsString()).getAsJsonObject();
        if ("S".equals(ctx.entrada.p_SimularOperacion)) {
            // D-F3B-01: "simulation" runs for real and the legacy only FORCES the counter to 1 at the very end (front J): the counter of F-C..F-H is checked in CerT2ReglasTest and CerT3ReglasTest
            assertEquals(1L, salida.get("P_OUT_ERRORESCONTADOR").getAsLong(), id + " :: simulation S: the golden counter is the forced 1");
        } else {
            assertEquals(salida.get("P_OUT_ERRORESCONTADOR").getAsLong(), ctx.salida.P_OUT_ERRORESCONTADOR.longValue(),
                    id + " :: counter of the output (what the custody collaborators added; the host rolls back when > 0)");
        }
        JsonArray mensajes = salida.getAsJsonArray("P_OUT_MENSAJES");
        assertEquals(mensajes.size(), ctx.salida.P_OUT_MENSAJES.size(), id + " :: number of messages of the output");
        for (int i = 0; i < mensajes.size(); i++) {
            JsonObject m = mensajes.get(i).getAsJsonObject();
            MensajeSalida r = ctx.salida.P_OUT_MENSAJES.get(i);
            assertEquals(texto(m, "id"), String.valueOf(r.id), id + " :: message " + i + " id (the absolute value of the id of the collaborator)");
            assertEquals(texto(m, "tipo"), r.tipo, id + " :: message " + i + " type");
            assertEquals(texto(m, "codigo"), r.codigo, id + " :: message " + i + " code");
            assertEquals(texto(m, "descripcion"), r.descripcion, id + " :: message " + i + " description");
        }
        JsonArray filas = salida.getAsJsonArray("P_OUT_RESULTADO");
        List<SolicitudProceso> sols = ctx.solicitudes;
        assertEquals(sols.size(), filas.size(), id + " :: one output row per solicitud read");
        for (int i = 0; i < sols.size(); i++) {
            SolicitudProceso s = sols.get(i);
            JsonObject fila = filas.get(i).getAsJsonObject();
            String q = id + " :: solicitud #" + i + " " + s.IdSolicitudAporteRescate;
            assertEquals(s.IdSolicitudAporteRescate.longValue(), fila.get("IdSolicitudAporteRescate").getAsLong(), q + " :: order of the solicitudes (read order)");
            if ("RZD".equals(s.EstadoProceso) || "ADV".equals(s.EstadoProceso)) {
                assertEquals(s.EstadoProceso, fila.get("Estado").getAsString(), q + " :: state after F-C..F-H");
                assertEquals(s.Codigo, fila.get("Codigo").getAsString(), q + " :: code after F-C..F-H");
            } else {
                assertEquals("PEN", s.EstadoProceso, q + " :: a solicitud that F-C..F-H did not touch stays PEN");
                assertEquals("APD", fila.get("Estado").getAsString(), q + " :: pending after F-C..F-H, approved by the legacy at the end");
                assertEquals("UI_EXITO", fila.get("Codigo").getAsString(), q);
            }
            if (s.DescripcionCodigo != null && !s.DescripcionCodigo.isEmpty()) {
                assertEquals(texto(fila, "DescripcionCodigo"), s.DescripcionCodigo, q + " :: description written by F-C..F-H (dictionary text with the parameters of the rule)");
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the columns the UPDATE to CRT wrote
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static void compararFilasActualizadas(String id, CertificarAporteRescateContexto ctx, JsonObject golden) {
        JsonArray antes = golden.getAsJsonObject("tablasIniciales").getAsJsonArray(T_SOL);
        // what the class left in the session when it returned, BEFORE the host decides
        JsonArray despues = golden.getAsJsonObject("tablasTrasLaLlamada").getAsJsonArray(T_SOL);
        for (SolicitudProceso s : ctx.solicitudes) {
            JsonObject a = fila(antes, s.IdSolicitudAporteRescate);
            JsonObject d = fila(despues, s.IdSolicitudAporteRescate);
            String q = id + " :: solicitud " + s.IdSolicitudAporteRescate;
            boolean pendiente = "PEN".equals(s.EstadoProceso) || "ADV".equals(s.EstadoProceso);
            if (!pendiente) {
                assertEquals(txt(a), txt(d), q + " :: a rejected solicitud is not touched (FM-CER-61, FM-CER-69)");
                continue;
            }
            assertEquals("CRT", d.get("Estado").getAsString(), q);
            assertNum(s.Monto, d.get("Monto"), q + " Monto (FM-CER-32/33)");
            assertNum(s.ValorCuota, d.get("ValorCuota"), q + " ValorCuota");
            assertNum(s.NumeroCuotas, d.get("NumeroCuotas"), q + " NumeroCuotas (FM-CER-32/33)");
            assertNum(s.CostoMonedaLocal, d.get("CostoMonedaLocal"), q + " CostoMonedaLocal");
            assertNum(s.MontoPago, d.get("MontoPago"), q + " MontoPago (FM-CER-37)");
            assertNum(s.MontoPagoLocal, d.get("MontoPagoLocal"), q + " MontoPagoLocal (FM-CER-37)");
            assertNum(s.MontoMonedaSistema, d.get("MontoMonedaSistema"), q + " MontoMonedaSistema");
            assertEquals(s.IdMonedaSistema, texto(d, "IdMonedaSistema"), q + " IdMonedaSistema");
            assertNum(s.MontoComision, d.get("MontoComision"), q + " MontoComision (FM-CER-37)");
            assertNum(s.MontoComisionLocal, d.get("MontoComisionLocal"), q + " MontoComisionLocal (FM-CER-37)");
            assertNum(s.CostoUF, d.get("CostoUF"), q + " CostoUF (FM-CER-26 for an aporte of reinversion, FM-CER-41 for a rescate)");
            assertNum(s.RentabilidadReal, d.get("RentabilidadReal"), q + " RentabilidadReal (FM-CER-28 aporte, FM-CER-41 rescate)");
            assertEquals(s.IdCartera == null ? null : String.valueOf(s.IdCartera), texto(d, "IdCartera"), q + " IdCartera (FM-CER-28)");
            assertEquals(s.IdIngresoEgreso == null ? null : String.valueOf(s.IdIngresoEgreso), texto(d, "IdIngresoEgreso"), q + " IdIngresoEgreso (FM-CER-47: the generated income / expense of Tesoreria)");
            assertEquals(s.IdOperacionSpot == null ? null : String.valueOf(s.IdOperacionSpot), texto(d, "IdOperacionSpot"), q + " IdOperacionSpot (FM-CER-60: the Spot operation)");
            if ("RES".equals(s.TipoSolicitud)) {
                // LIMIT OF THE ENGINE: H2 types the parameter of `CASE ... THEN :Spread ELSE 0` as INTEGER and rounds it when it writes the UPDATE; HANA keeps the decimals. The exact Spread is
                // compared in the pojo that Tesoreria / Spot receive (the complete arguments of the call) when the rescate goes there; here only "the same number up to the integer rounding".
                assertNotNull(s.Spread, q + " Spread");
                assertTrue(s.Spread.subtract(d.get("Spread").getAsBigDecimal()).abs().compareTo(new BigDecimal("0.5")) <= 0,
                        q + " :: Spread (FM-CER-41) " + s.Spread.toPlainString() + " must round to the integer " + d.get("Spread") + " that H2 wrote");
            } else {
                assertNum(BigDecimal.ZERO, d.get("Spread"), q + " Spread of an aporte is 0");
            }
            if ("APOREINV".equals(s.IdTipoMovimientoFondoMutuo)) {
                assertNum(s.ValorUFFechaInversion, d.get("ValorUFFechaInversion"), q + " ValorUFFechaInversion of an APOREINV (FM-CER-26)");
            }
            // NOT compared here (the engine): ParidadFechaCertificacion (H2 types COALESCE(:Paridad, 0) as INTEGER)
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // helpers
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static JsonObject fila(JsonArray tabla, Long id) {
        for (JsonElement e : tabla) {
            if (e.getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong() == id) return e.getAsJsonObject();
        }
        throw new AssertionError("row " + id + " not in the dump");
    }

    static String texto(JsonObject o, String k) {
        return o.get(k) == null || o.get(k).isJsonNull() ? null : o.get(k).getAsString();
    }

    /** Numbers are compared by value (scale-insensitive, 8 decimals as the columns): a golden {@code 666.6118} equals {@code 666.61180000}. A null on one side is a null on the other. */
    static void assertNum(BigDecimal esperado, JsonElement real, String donde) {
        boolean nuloReal = real == null || real.isJsonNull();
        if (esperado == null || nuloReal) {
            assertEquals(esperado == null, nuloReal, donde + " :: null / not null (expected " + esperado + ", golden " + real + ")");
            return;
        }
        BigDecimal r = real.getAsBigDecimal();
        assertEquals(0, esperado.setScale(8, RoundingMode.HALF_UP).compareTo(r.setScale(8, RoundingMode.HALF_UP)), donde + " :: expected " + esperado.toPlainString() + " golden " + r.toPlainString());
    }

    static String txt(JsonElement e) {
        return PRETTY.toJson(canon(e, false));
    }

    /** like {@link #txt} but every number is rendered by VALUE ({@code 1.50} = {@code 1.5}): the JSON of the arguments the legacy built carries the scale of its BigDecimals, which is not a rule. */
    static String txtN(JsonElement e) {
        return PRETTY.toJson(canon(e, true));
    }

    static JsonElement canon(JsonElement e, boolean numeros) {
        if (e == null || e.isJsonNull()) return JsonNull.INSTANCE;
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) a.add(canon(x, numeros));
            return a;
        }
        if (e.isJsonObject()) {
            TreeMap<String, JsonElement> m = new TreeMap<>();
            for (String k : e.getAsJsonObject().keySet()) m.put(k, canon(e.getAsJsonObject().get(k), numeros));
            JsonObject o = new JsonObject();
            m.forEach(o::add);
            return o;
        }
        if (numeros && e.getAsJsonPrimitive().isNumber()) {
            return new JsonPrimitive("#" + new BigDecimal(e.getAsString()).stripTrailingZeros().toPlainString());
        }
        return e;
    }
}
