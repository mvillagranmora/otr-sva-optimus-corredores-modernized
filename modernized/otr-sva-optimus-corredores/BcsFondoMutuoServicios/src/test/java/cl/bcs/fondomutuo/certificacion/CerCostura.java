package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;

/**
 * The comparison of a case that CONTINUES past F-A / F-B (see {@link CasoCer} and API_ASSUMED.md 0): what F-A and F-B DECIDED, seen in what the RECORDED legacy did next.
 *
 * <p>F-A / F-B have no output of their own: their decisions are consumed by the later phases. The legacy golden of the same case (modo `completo` = the whole run with benign
 * collaborators; modo `parada` = the run stopped at the first collaborator F-C / F-D / F-J reach) shows them: the codes of the output, the rows the UPDATE to CRT wrote, and the
 * ARGUMENTS of the first collaborators called. Each check below reads the golden and the {@link CertificarAporteRescateContexto} that the new facade handed to the seam, and fails if
 * they disagree. The "consumer" functions ({@link #paresFlujoCaja}, {@link #rechazadosPorSaldo}, {@link #primerColaborador}) restate, with the legacy line, the little part of
 * F-C / F-D / F-J that decides WHICH collaborator is called first, with which pairs and which aporte the balance rejects: they are the lens to read the seam, not the implementation
 * of those fronts (T2..T5 implement and pin them).
 */
final class CerCostura {

    /** Collaborators that F-A / F-B themselves call: the T1 prefix of the collaborator list of a golden ends at the first call to anything else. */
    static final Set<String> COLABORADORES_T1 = Set.of("EntregarPreciosTasas", "CalcularParidad");

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final String T_SOL = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";
    private static final String CODIGO_SALDO = "UI_SALDO_INSUFICIENTE_FLUJO_CAJA";

    private CerCostura() {
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // entry
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    static void comparar(String id, JsonObject golden, CasoCer.Corrida corrida) {
        TimeZone zona = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            compararEnUtc(id, golden, corrida);
        } finally {
            TimeZone.setDefault(zona);
        }
    }

    private static void compararEnUtc(String id, JsonObject golden, CasoCer.Corrida c) {
        CasoCer.Modo modo = CasoCer.modo(id);
        assertTrue(modo != CasoCer.Modo.TERMINAL, id + " :: not a case that continues");
        assertTrue(c.excepcion instanceof ParadaT1, id + " :: the facade must reach the seam (FasesPosteriores) and stop there; it ended with " + c.excepcion);
        assertNotNull(c.contexto, id + " :: the seam received no context");
        JsonObject nuevo = c.volcado;
        // 1. F-A / F-B read, never write: nothing changed in the tables while the facade ran, and the seed is the one of the golden
        assertEquals(txt(golden.get("tablasIniciales")), txt(nuevo.get("tablasIniciales")), id + " :: tablasIniciales");
        assertEquals(txt(nuevo.get("tablasIniciales")), txt(nuevo.get("tablasTrasLaLlamada")), id + " :: F-A and F-B must not write (tablasTrasLaLlamada)");
        assertEquals(txt(nuevo.get("tablasIniciales")), txt(nuevo.get("tablas")), id + " :: F-A and F-B must not write (tablas)");
        assertEquals("[]", txt(nuevo.get("secuenciasSolicitadas")), id + " :: no sequence is consumed before the seam");
        // 2. the collaborator calls of F-A / F-B (EntregarPreciosTasas of FM-CER-05, then the ones of CalcularMontoNumeroCuota): the T1 prefix of the golden
        assertEquals(txt(prefijoT1(golden.getAsJsonArray("colaboradores"))), txt(simples(nuevo.getAsJsonArray("colaboradores"))), id + " :: collaborator calls up to the seam");
        // 3. what F-A / F-B decided, read in what the legacy did next
        CertificarAporteRescateContexto ctx = c.contexto;
        JsonArray cols = golden.getAsJsonArray("colaboradores");
        if (modo == CasoCer.Modo.PARADA) {
            assertEquals("Exception: PARADA_T1", golden.get("excepcion").getAsString(), id + " :: golden stop");
            assertEquals("ROLLBACK_HTTP500", golden.get("decisionServlet").getAsString());
            String ultimo = cols.get(cols.size() - 1).getAsJsonObject().get("colaborador").getAsString();
            if (!ultimo.equals("NotificarEstadoOrdenFondoMutuo")) {
                // every other stop is BEFORE the first write of the legacy; the notification comes after the UPDATE to CRT (C:835-836), which writes the still pending rows
                assertEquals(txt(golden.get("tablasIniciales")), txt(golden.get("tablasTrasLaLlamada")), id + " :: the legacy had written nothing when it stopped");
            }
            assertEquals(primerColaborador(ctx), ultimo, id + " :: the first collaborator the legacy reaches after F-B must be the one that the context implies (partition / indicators / states)");
            compararColaboradoresPosteriores(id, ctx, c, golden, cols, Set.of(), true);
        } else {
            assertTrue(!golden.has("excepcion"), id + " :: the completo golden must not end in an exception: " + golden.get("excepcion"));
            Set<Long> rechazados = rechazadosPorSaldo(ctx, cols);
            compararSalida(id, ctx, golden, rechazados);
            compararColaboradoresPosteriores(id, ctx, c, golden, cols, rechazados, false);
            compararFilasActualizadas(id, ctx, c, golden, rechazados);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the T1 prefix of the collaborator list
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** The golden's collaborator calls up to (not including) the first one that is not a collaborator of F-A / F-B; same projection as {@link #simples}. */
    static JsonArray prefijoT1(JsonArray colaboradores) {
        JsonArray out = new JsonArray();
        for (JsonElement e : colaboradores) {
            JsonObject o = e.getAsJsonObject();
            if (!COLABORADORES_T1.contains(o.get("colaborador").getAsString())) break;
            out.add(o);
        }
        return simples(out);
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

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the consumers of F-B (restated from the legacy, with the line)
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** An aporte that {@code validarSaldoCuentaInversion} (C:1149-1225) / {@code ...PAC} (C:1227-1303) takes into account: not rejected, MueveCta S, origin M, indicator N (absent = N). */
    private static boolean entraAlFlujo(CertificarAporteRescateContexto ctx, SolicitudProceso sol, boolean pac) {
        if ("RZD".equals(sol.EstadoProceso)) return false;
        if (pac != "PAC".equals(sol.IdTipoCanalIngreso)) return false;
        CertificarAporteRescateContrato.IndicadorPortafolio ind = ctx.indicadoresPortafolio.get(sol.IdPortafolio);
        String sobregiro = ind != null ? ind.OperaLineaSobregiro : "N";
        return "S".equals(sol.MueveCta) && "M".equals(sol.OrigenOperacion) && "N".equals(sobregiro);
    }

    /**
     * The pairs of {@code P_PORTAFOLIOACONSULTAR} of the first (pac=false) or second (pac=true) call to SaldoFlujoCaja: the pair is added only when the portfolio is not in
     * {@code portFlujo} OR the currency is not in {@code monedaFlujo} (two INDEPENDENT lists, D-NOR-16).
     */
    static List<String[]> paresFlujoCaja(CertificarAporteRescateContexto ctx, boolean pac) {
        List<Long> portFlujo = new ArrayList<>();
        List<String> monedaFlujo = new ArrayList<>();
        List<String[]> pares = new ArrayList<>();
        for (SolicitudProceso sol : ctx.aportes) {
            if (!entraAlFlujo(ctx, sol, pac)) continue;
            if (!portFlujo.contains(sol.IdPortafolio) || !monedaFlujo.contains(sol.IdMoneda)) {
                pares.add(new String[] { String.valueOf(sol.IdPortafolio), sol.IdMoneda });
                if (!portFlujo.contains(sol.IdPortafolio)) portFlujo.add(sol.IdPortafolio);
                if (!monedaFlujo.contains(sol.IdMoneda)) monedaFlujo.add(sol.IdMoneda);
            }
        }
        return pares;
    }

    /**
     * The aportes the cash-flow balance rejects (C:1186-1224, 1264-1302): total = sum of CostoMonedaLocal (DECIMAL128) of the aportes of the same portfolio and currency that enter the
     * flow; saldo = the LAST row of the SaldoFlujoCaja answer with the same portfolio, currency and date (the later of FechaCertificacion and PLA.FechaSistema), 0 if none; rejected
     * when OperaSaldoNegativo is N (any case, FM-CER-20) and total > saldo. The answer is read from the golden (the benign run answers a rich balance unless the case says otherwise).
     */
    static Set<Long> rechazadosPorSaldo(CertificarAporteRescateContexto ctx, JsonArray cols) {
        Set<Long> rechazados = new LinkedHashSet<>();
        List<JsonObject> llamadas = llamadas(cols, "SaldoFlujoCaja");
        int k = 0;
        LocalDate sistema = LocalDate.parse(ctx.fechaSistema);
        for (boolean pac : new boolean[] { false, true }) {
            if (paresFlujoCaja(ctx, pac).isEmpty()) continue;
            JsonArray flujo = llamadas.get(k++).get("respuesta").getAsJsonObject().getAsJsonArray("P_OUT_FLUJOCAJA");
            TreeMap<String, BigDecimal> totales = new TreeMap<>();
            for (SolicitudProceso sol : ctx.aportes) {
                if (!entraAlFlujo(ctx, sol, pac)) continue;
                totales.merge(sol.IdPortafolio + "_" + sol.IdMoneda, sol.CostoMonedaLocal, (x, y) -> x.add(y, MathContext.DECIMAL128));
            }
            for (SolicitudProceso sol : ctx.aportes) {
                if (!entraAlFlujo(ctx, sol, pac)) continue;
                BigDecimal saldo = BigDecimal.ZERO;
                BigDecimal total = totales.getOrDefault(sol.IdPortafolio + "_" + sol.IdMoneda, BigDecimal.ZERO);
                LocalDate certificacion = sol.FechaCertificacion.toLocalDate();
                String fecha = certificacion.isBefore(sistema) ? sistema.toString() : certificacion.toString();
                for (JsonElement e : flujo) {
                    JsonObject f = e.getAsJsonObject();
                    if (f.get("IdPortafolio").getAsLong() == sol.IdPortafolio && f.get("IdMoneda").getAsString().equals(sol.IdMoneda) && f.get("FechaSaldo").getAsString().equals(fecha)) {
                        saldo = f.get("Saldo").getAsBigDecimal();
                    }
                }
                if ("N".equalsIgnoreCase(sol.OperaSaldoNegativo) && total.compareTo(saldo) == 1) rechazados.add(sol.IdSolicitudAporteRescate);
            }
        }
        return rechazados;
    }

    /**
     * The first collaborator the legacy calls after F-B, in its order (C:575-600, 584, 600-618, 836): SaldoFlujoCaja (any pair), else CalculaCustodiaRescate (a rescate), else
     * ProcesarCustodiaFondoMutuo (an aporte), else the asynchronous notification (nothing left). {@code reCalcularCostoUFAportes} (C:601) would call CalcularParidad before the
     * custody of an APOREINV aporte whose origin rescate is found: the cases never build that data (it is F-C).
     */
    static String primerColaborador(CertificarAporteRescateContexto ctx) {
        if (!paresFlujoCaja(ctx, false).isEmpty() || !paresFlujoCaja(ctx, true).isEmpty()) return "SaldoFlujoCaja";
        if (!ctx.rescates.isEmpty()) return "CalculaCustodiaRescate";
        if (!ctx.aportes.isEmpty()) return "ProcesarCustodiaFondoMutuo";
        return "NotificarEstadoOrdenFondoMutuo";
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the output (completo): codes of the rejections that F-A / F-B decided, order of the solicitudes
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static void compararSalida(String id, CertificarAporteRescateContexto ctx, JsonObject golden, Set<Long> rechazadosPorSaldo) {
        JsonObject salida = JsonParser.parseString(golden.get("salidaCruda").getAsString()).getAsJsonObject();
        JsonArray filas = salida.getAsJsonArray("P_OUT_RESULTADO");
        List<SolicitudProceso> sols = ctx.solicitudes;
        assertEquals(sols.size(), filas.size(), id + " :: one output row per solicitud read");
        for (int i = 0; i < sols.size(); i++) {
            SolicitudProceso s = sols.get(i);
            JsonObject fila = filas.get(i).getAsJsonObject();
            String quien = id + " :: solicitud #" + i + " " + s.IdSolicitudAporteRescate;
            if (!"S".equals(ctx.entrada.p_CertificacionAporteSwitchSimulado)) {
                assertEquals(s.IdSolicitudAporteRescate.longValue(), fila.get("IdSolicitudAporteRescate").getAsLong(), quien + " :: order of the solicitudes (read order, FM-CER-04)");
            }
            if ("RZD".equals(s.EstadoProceso)) {
                assertEquals("RZD", fila.get("Estado").getAsString(), quien + " :: rejected by F-A / F-B");
                assertEquals(s.Codigo, fila.get("Codigo").getAsString(), quien + " :: code of the rejection");
            } else if (rechazadosPorSaldo.contains(s.IdSolicitudAporteRescate)) {
                assertEquals("RZD", fila.get("Estado").getAsString(), quien + " :: rejected by the balance (OperaSaldoNegativo " + s.OperaSaldoNegativo + ", FM-CER-20/21)");
                assertEquals(CODIGO_SALDO, fila.get("Codigo").getAsString(), quien);
            } else {
                assertEquals("APD", fila.get("Estado").getAsString(), quien + " :: pending after F-A / F-B and not rejected by the balance (OperaSaldoNegativo " + s.OperaSaldoNegativo + ")");
                assertEquals("UI_EXITO", fila.get("Codigo").getAsString(), quien);
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // arguments of the collaborators that F-C / F-D / F-J call first
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static void compararColaboradoresPosteriores(String id, CertificarAporteRescateContexto ctx, CasoCer.Corrida c, JsonObject golden, JsonArray cols,
            Set<Long> rechazadosPorSaldo, boolean soloElUltimo) {
        List<JsonObject> saldo = llamadas(cols, "SaldoFlujoCaja");
        List<JsonObject> custodiaRescate = llamadas(cols, "CalculaCustodiaRescate");
        List<JsonObject> custodia = llamadas(cols, "ProcesarCustodiaFondoMutuo");
        List<JsonObject> notificar = llamadas(cols, "NotificarEstadoOrdenFondoMutuo");
        // --- SaldoFlujoCaja (FM-CER-15, 18, 20, 21, 22): the pairs and the effective date
        List<String[]> pares = paresFlujoCaja(ctx, false);
        List<String[]> paresPac = paresFlujoCaja(ctx, true);
        int esperadas = (pares.isEmpty() ? 0 : 1) + (paresPac.isEmpty() ? 0 : 1);
        if (!soloElUltimo) assertEquals(esperadas, saldo.size(), id + " :: number of SaldoFlujoCaja calls (non-PAC pairs " + pares.size() + ", PAC pairs " + paresPac.size() + ")");
        int k = 0;
        for (boolean pac : new boolean[] { false, true }) {
            List<String[]> grupo = pac ? paresPac : pares;
            if (grupo.isEmpty() || k >= saldo.size()) continue;
            JsonObject arg = saldo.get(k++).getAsJsonArray("argumentos").get(0).getAsJsonObject();
            assertEquals(ctx.entrada.p_IdEmpresa.longValue(), arg.get("P_IDEMPRESA").getAsLong(), id);
            assertEquals(pac ? "CERTIFICACION_APORTE_FMM_PAC" : "CERTIFICACION_APORTE_FMM", arg.get("P_USO").getAsString(), id);
            SimpleDateFormat dia = new SimpleDateFormat("yyyy-MM-dd");
            dia.setTimeZone(TimeZone.getTimeZone("UTC"));
            String fecha = dia.format(ctx.fechaCertificacion);
            assertEquals(fecha, arg.get("P_FECHADESDE").getAsString(), id + " :: effective certification date (FM-CER-18)");
            assertEquals(fecha, arg.get("P_FECHAHASTA").getAsString(), id + " :: effective certification date (FM-CER-18)");
            assertEquals("N", arg.get("P_DESPLEGARENUI").getAsString(), id);
            assertEquals("N", arg.get("P_AGRUPARPORORIGEN").getAsString(), id);
            JsonArray cp = arg.getAsJsonArray("P_PORTAFOLIOACONSULTAR");
            assertEquals(grupo.size(), cp.size(), id + " :: pairs sent to SaldoFlujoCaja (FM-CER-15 indicators, FM-CER-21)");
            for (int i = 0; i < grupo.size(); i++) {
                assertEquals(grupo.get(i)[0], cp.get(i).getAsJsonObject().get("IdPortafolio").getAsString(), id + " :: pair " + i);
                assertEquals(grupo.get(i)[1], cp.get(i).getAsJsonObject().get("IdMoneda").getAsString(), id + " :: pair " + i);
            }
        }
        // --- CalculaCustodiaRescate (FM-CER-16, 17, 19): the rescates, in ascending id, with the amounts of the recalculation (before any formatting)
        if (!soloElUltimo) assertEquals(ctx.rescates.isEmpty() ? 0 : 1, custodiaRescate.size(), id + " :: CalculaCustodiaRescate is called once when there are rescates");
        if (!custodiaRescate.isEmpty()) {
            JsonArray sols = custodiaRescate.get(0).getAsJsonArray("argumentos").get(0).getAsJsonObject().getAsJsonArray("p_Solicitudes");
            assertEquals(ctx.rescates.size(), sols.size(), id + " :: rescates sent to CalculaCustodiaRescate");
            for (int i = 0; i < ctx.rescates.size(); i++) {
                SolicitudProceso s = ctx.rescates.get(i);
                JsonObject o = sols.get(i).getAsJsonObject();
                String q = id + " :: rescate #" + i + " " + s.IdSolicitudAporteRescate;
                assertEquals(s.IdSolicitudAporteRescate.longValue(), o.get("IdSolicitudAporteRescate").getAsLong(), q + " :: ascending order (FM-CER-19)");
                assertEquals(s.IdSerieFondo, str(o, "IdSerieFondo"), q);
                assertEquals(s.CalculoPorMontoCuota, str(o, "CalculoPorMontoCuota"), q);
                assertEquals(s.IdMoneda, str(o, "IdMoneda"), q);
                assertEquals(s.IdMonedaFondo, str(o, "IdMonedaFondo"), q);
                assertNum(s.Monto, o.get("Monto"), q + " Monto (FM-CER-16/17)");
                assertNum(s.NumeroCuotas, o.get("NumeroCuotas"), q + " NumeroCuotas (FM-CER-16/17)");
                assertNum(s.ValorCuota, o.get("ValorCuota"), q + " ValorCuota (FM-CER-16/17)");
                assertEquals(s.TipoSolicitud, str(o, "TipoSolicitud"), q);
                assertEquals(s.IdInstrumento == null ? null : String.valueOf(s.IdInstrumento), o.get("IdInstrumento").isJsonNull() ? null : String.valueOf(o.get("IdInstrumento").getAsLong()), q);
                assertEquals(s.IdCliente.longValue(), o.get("IdCliente").getAsLong(), q);
                assertEquals(s.IdPortafolio.longValue(), o.get("IdPortafolio").getAsLong(), q);
                assertEquals(ts(s.FechaValorCuota), str(o, "FechaValorCuota"), q);
                assertEquals(s.IdRegimenTributario, str(o, "IdRegimenTributario"), q);
                assertEquals(s.IdOmnibus == null ? null : String.valueOf(s.IdOmnibus), o.get("IdOmnibus").isJsonNull() ? null : String.valueOf(o.get("IdOmnibus").getAsLong()), q);
            }
        }
        // --- ProcesarCustodiaFondoMutuo of the aportes (FM-CER-17, 19, 23): ascending ids, the formatted amounts
        if (!soloElUltimo) {
            JsonObject llamadaAportes = null;
            for (JsonObject l : custodia) {
                JsonArray movs = l.getAsJsonArray("argumentos").get(0).getAsJsonObject().getAsJsonArray("p_Movimientos");
                if (movs.size() > 0 && "APORFM".equals(movs.get(0).getAsJsonObject().get("IdTipoOperacion").getAsString())) {
                    llamadaAportes = l;
                    break;
                }
            }
            List<Long> vigentes = new ArrayList<>();
            for (SolicitudProceso a : ctx.aportes) {
                if (!rechazadosPorSaldo.contains(a.IdSolicitudAporteRescate)) vigentes.add(a.IdSolicitudAporteRescate);
            }
            if (vigentes.isEmpty()) {
                assertNull(llamadaAportes, id + " :: no aporte survives: no custody entry");
            } else {
                assertNotNull(llamadaAportes, id + " :: custody entry of the aportes");
                JsonArray movs = llamadaAportes.getAsJsonArray("argumentos").get(0).getAsJsonObject().getAsJsonArray("p_Movimientos");
                assertEquals(vigentes.size(), movs.size(), id + " :: aportes sent to custody");
                for (int i = 0; i < movs.size(); i++) {
                    JsonObject m = movs.get(i).getAsJsonObject();
                    long idMov = m.get("IdGenerarMovimientoCustodia").getAsLong();
                    assertEquals(vigentes.get(i).longValue(), idMov, id + " :: aportes in ascending id (FM-CER-19), entry " + i);
                    SolicitudProceso raw = ctx.solicitudes.stream().filter(s -> s.IdSolicitudAporteRescate == idMov).findFirst().orElseThrow();
                    SolicitudProceso f = c.formateadas.get(idMov);
                    String q = id + " :: aporte " + idMov;
                    assertNum(raw.NumeroCuotas, m.get("Cantidad"), q + " Cantidad = NumeroCuotas");
                    assertNum(raw.ValorCuota, m.get("PrecioCompra"), q + " PrecioCompra = ValorCuota");
                    assertNum(f.MontoMonedaSistema, m.get("MontoCompra"), q + " MontoCompra = MontoMonedaSistema formatted (FM-CER-23)");
                    assertNum(f.Monto, m.get("MontoCompraMonedaInstrumento"), q + " MontoCompraMonedaInstrumento = Monto formatted (FM-CER-23)");
                    assertNum(raw.CostoUF, m.get("CostoEnFactorAjuste"), q + " CostoEnFactorAjuste = CostoUF");
                    assertNum(raw.ValorUFFechaInversion, m.get("ValorFactorAjusteaFechaCurse"), q + " ValorFactorAjusteaFechaCurse = ValorUFFechaInversion");
                    assertEquals(ts(raw.FechaCertificacion), str(m, "FechaOperacion"), q);
                    assertEquals(raw.IdMonedaFondo, str(m, "IdMonedaInstrumento"), q);
                    assertEquals(raw.IdRegimenTributario, str(m, "IdRegimenTributario"), q);
                }
            }
        }
        // --- NotificarEstadoOrdenFondoMutuo: ids of the solicitudes still pending, in read order
        if (!notificar.isEmpty()) {
            JsonArray ids = notificar.get(0).getAsJsonArray("argumentos").get(0).getAsJsonObject().getAsJsonArray("p_Solicitudes");
            List<Long> esperados = new ArrayList<>();
            for (SolicitudProceso s : ctx.solicitudes) {
                if (("PEN".equals(s.EstadoProceso) || "ADV".equals(s.EstadoProceso)) && !rechazadosPorSaldo.contains(s.IdSolicitudAporteRescate)) esperados.add(s.IdSolicitudAporteRescate);
            }
            List<Long> reales = new ArrayList<>();
            for (JsonElement e : ids) reales.add(e.getAsLong());
            assertEquals(esperados, reales, id + " :: solicitudes notified = the ones still pending, in read order");
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the rows the UPDATE to CRT wrote (completo): the amounts of FM-CER-16 / 17 after the format of FM-CER-23
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static void compararFilasActualizadas(String id, CertificarAporteRescateContexto ctx, CasoCer.Corrida c, JsonObject golden, Set<Long> rechazadosPorSaldo) {
        JsonArray antes = golden.getAsJsonObject("tablasIniciales").getAsJsonArray(T_SOL);
        // what the class left in the session when it returned, BEFORE the host decides (with p_SimularOperacion S the counter is forced to 1 and the host rolls everything back: D-F3B-01)
        JsonArray despues = golden.getAsJsonObject("tablasTrasLaLlamada").getAsJsonArray(T_SOL);
        for (SolicitudProceso s : ctx.solicitudes) {
            JsonObject a = fila(antes, s.IdSolicitudAporteRescate);
            JsonObject d = fila(despues, s.IdSolicitudAporteRescate);
            String q = id + " :: solicitud " + s.IdSolicitudAporteRescate;
            boolean pendiente = "PEN".equals(s.EstadoProceso) || "ADV".equals(s.EstadoProceso);
            if (!pendiente || rechazadosPorSaldo.contains(s.IdSolicitudAporteRescate)) {
                assertEquals(txt(a), txt(d), q + " :: a rejected solicitud is not touched (FM-CER-61, FM-CER-69)");
                continue;
            }
            assertEquals("CRT", d.get("Estado").getAsString(), q);
            SolicitudProceso f = c.formateadas.get(s.IdSolicitudAporteRescate);
            // a total rescate takes the amounts of the custody (FM-CER-33, F-D): not the business of F-B
            boolean totalPorCustodia = "RES".equals(s.TipoSolicitud) && "T".equals(s.CalculoPorMontoCuota) && (s.IdOmnibus == null || s.IdOmnibus == 0L);
            if (!totalPorCustodia) {
                assertNum(f.Monto, d.get("Monto"), q + " Monto (FM-CER-16/17 + 23)");
                assertNum(f.MontoPago, d.get("MontoPago"), q + " MontoPago");
                assertNum(f.CostoMonedaLocal, d.get("CostoMonedaLocal"), q + " CostoMonedaLocal");
                assertNum(f.MontoPagoLocal, d.get("MontoPagoLocal"), q + " MontoPagoLocal");
                assertNum(f.MontoMonedaSistema, d.get("MontoMonedaSistema"), q + " MontoMonedaSistema");
                assertNum(s.NumeroCuotas, d.get("NumeroCuotas"), q + " NumeroCuotas");
                assertNum(s.ValorCuota, d.get("ValorCuota"), q + " ValorCuota");
                assertEquals(s.IdMonedaSistema, str(d, "IdMonedaSistema"), q + " IdMonedaSistema");
            }
            assertNum(f.MontoComision, d.get("MontoComision"), q + " MontoComision (FM-CER-23)");
            assertNum(f.MontoComisionLocal, d.get("MontoComisionLocal"), q + " MontoComisionLocal (FM-CER-23)");
            assertEquals(s.IdSolicitudAporteRescateRelacionado.longValue(), d.get("IdSolicitudAporteRescateRelacionado").getAsLong(), q);
            if ("APO".equals(s.TipoSolicitud)) {
                assertNum(s.CostoUF, d.get("CostoUF"), q + " CostoUF of an aporte (FM-CER-17)");
            }
            if ("APOREINV".equals(s.IdTipoMovimientoFondoMutuo)) {
                assertNum(s.ValorUFFechaInversion, d.get("ValorUFFechaInversion"), q + " ValorUFFechaInversion of an APOREINV (FM-CER-17)");
            }
            // NOT compared here (later fronts / the engine): CostoUF, Spread, RentabilidadReal of a rescate (F-D, FM-CER-41), IdIngresoEgreso, IdOperacionSpot, IdCartera, and
            // ParidadFechaCertificacion (the H2 types the parameter of COALESCE(:Paridad, 0) as INTEGER and rounds it; HANA keeps the decimals)
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // helpers
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static List<JsonObject> llamadas(JsonArray cols, String nombre) {
        List<JsonObject> out = new ArrayList<>();
        for (JsonElement e : cols) {
            if (e.getAsJsonObject().get("colaborador").getAsString().equals(nombre)) out.add(e.getAsJsonObject());
        }
        return out;
    }

    private static JsonObject fila(JsonArray tabla, Long id) {
        for (JsonElement e : tabla) {
            if (e.getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong() == id) return e.getAsJsonObject();
        }
        throw new AssertionError("row " + id + " not in the dump");
    }

    private static String str(JsonObject o, String k) {
        return o.get(k) == null || o.get(k).isJsonNull() ? null : o.get(k).getAsString();
    }

    /** {@code yyyy-MM-dd HH:mm:ss.SSS} of a date, in UTC (the format of the legacy Gson of the collaborator entries); null stays null. */
    static String ts(java.util.Date d) {
        if (d == null) return null;
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(d);
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
        return PRETTY.toJson(canon(e));
    }

    static JsonElement canon(JsonElement e) {
        if (e == null || e.isJsonNull()) return JsonNull.INSTANCE;
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) a.add(canon(x));
            return a;
        }
        if (e.isJsonObject()) {
            TreeMap<String, JsonElement> m = new TreeMap<>();
            for (String k : e.getAsJsonObject().keySet()) m.put(k, canon(e.getAsJsonObject().get(k)));
            JsonObject o = new JsonObject();
            m.forEach(o::add);
            return o;
        }
        return e;
    }
}
