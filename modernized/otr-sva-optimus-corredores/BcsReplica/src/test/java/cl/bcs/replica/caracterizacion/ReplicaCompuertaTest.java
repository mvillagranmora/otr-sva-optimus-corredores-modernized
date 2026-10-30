package cl.bcs.replica.caracterizacion;

import static cl.bcs.replica.caracterizacion.VolcadoAyuda.WS;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.excepcion;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.filas;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.sentencias;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

/**
 * The gates of BcsReplica (no legacy golden: the legacy has no gates, it executes everything). Three gates, closed by default, protect what must never run by accident against HANA:
 * G-TRG (CREATE / DROP TRIGGER of INICIO_PAUTA and FIN_PAUTA), G-SEB (the Sebra backups of ReplicaUtils.procesarRespaldoSebra and procesarRespaldoDiarioCarterasSebra) and G-OPT (the Optimus backup
 * ReplicaUtils.procesarRespaldoOptimus). A closed gate raises ServicioNoServidoException before the first write and before any collaborator call, and affects nothing else. The collaborators that are not
 * ported (ConsultarEmpresa, ObtenerDiaSiguiente, Feriados, ConfiguracionSistema, Utils.getFechaSistema / getSecuencia) are behind ports whose default implementation, ColaboradoresNoPortados, is
 * closed: it raises ServicioNoServidoException when used.
 */
class ReplicaCompuertaTest {

    private static final String SNS = "ServicioNoServidoException";

    private static JsonObject cerrado(String id, boolean triggers, boolean sebra, boolean optimus) throws Exception {
        return CasoReplica.ejecutar(id, c -> c.conCompuertas(triggers, sebra, optimus));
    }

    private static void sinEfectos(JsonObject d, String id) {
        assertEquals(SNS, excepcion(d), id);
        assertEquals(0, sentencias(d), id + ": a closed gate must not send any SQL");
        assertEquals(d.getAsJsonObject("tablasIniciales").toString(), d.getAsJsonObject("tablas").toString(), id + ": a closed gate must not change any table");
    }

    @Test
    @DisplayName("G-TRG: con la compuerta de triggers cerrada INICIO_PAUTA no hace nada ni llama a nadie")
    void triggers_cerrados_inicio_pauta() throws Exception {
        String id = CasoReplica.id("REP-ENV-", "inicio-pauta-fin-de-semana-copia-dos-dias");
        JsonObject d = cerrado(id, false, true, true);
        sinEfectos(d, id);
        assertEquals(0, d.getAsJsonArray("colaboradores").size());
    }

    @Test
    @DisplayName("G-TRG: con la compuerta de triggers cerrada FIN_PAUTA no hace nada ni lee el parametro de desfase")
    void triggers_cerrados_fin_pauta() throws Exception {
        String id = CasoReplica.id("REP-ENV-", "fin-pauta-completo-offset-cero");
        JsonObject d = cerrado(id, false, true, true);
        sinEfectos(d, id);
        assertEquals(0, d.getAsJsonArray("colaboradores").size());
    }

    @Test
    @DisplayName("G-TRG: la compuerta de triggers abierta y las otras cerradas deja pasar INICIO_PAUTA y FIN_PAUTA (equivalen al legacy)")
    void triggers_abiertos_solos() throws Exception {
        for (String f : new String[] { "inicio-pauta-fin-de-semana-copia-dos-dias", "fin-pauta-completo-offset-cero", "inicio-pauta-primer-drop-falla" }) {
            String id = CasoReplica.id("REP-ENV-", f);
            CasoReplica.comparar(id, CasoReplica.golden(id), cerrado(id, true, false, false));
        }
    }

    @Test
    @DisplayName("G-SEB: con la compuerta Sebra cerrada y el parametro en S el respaldo mensual falla tras leer el parametro, sin escribir")
    void sebra_cerrada_con_S() throws Exception {
        String id = CasoReplica.id("REP-SEB-", "param-s-reemplaza-el-periodo-en-las-13");
        JsonObject d = cerrado(id, true, false, true);
        sinEfectos(d, id);
        assertEquals(1, d.getAsJsonArray("colaboradores").size());
    }

    @Test
    @DisplayName("G-SEB: con la compuerta Sebra cerrada y el parametro distinto de S no pasa nada (es como el legacy); un parametro ausente sigue propagando el error del colaborador")
    void sebra_cerrada_sin_S() throws Exception {
        for (String f : new String[] { "param-n-no-hace-nada", "param-x-no-hace-nada", "param-vacio-no-hace-nada", "param-ausente-se-propaga", "param-nulo-falla-al-comparar", "fecha-nula-con-n-no-falla" }) {
            String id = CasoReplica.id("REP-SEB-", f);
            CasoReplica.comparar(id, CasoReplica.golden(id), cerrado(id, true, false, true));
        }
    }

    @Test
    @DisplayName("G-SEB: el respaldo diario de carteras Sebra con la compuerta cerrada y S falla sin escribir; con N o con el parametro ausente se comporta como el legacy")
    void sebra_diario_cerrado() throws Exception {
        String id = CasoReplica.id("REP-DIA-", "param-s-copia-el-periodo-al-dia");
        JsonObject d = cerrado(id, true, false, true);
        sinEfectos(d, id);
        for (String f : new String[] { "param-n-no-hace-nada", "param-ausente-se-traga", "fecha-nula-con-n-no-falla" }) {
            String otro = CasoReplica.id("REP-DIA-", f);
            CasoReplica.comparar(otro, CasoReplica.golden(otro), cerrado(otro, true, false, true));
        }
    }

    @Test
    @DisplayName("G-SEB: la compuerta Sebra abierta sola deja pasar los dos respaldos Sebra y no abre la de Optimus")
    void sebra_abierta_sola() throws Exception {
        for (String f : new String[] { "param-s-reemplaza-el-periodo-en-las-13", "falla-insercion-de-tbcpdtpc" }) {
            String id = CasoReplica.id("REP-SEB-", f);
            CasoReplica.comparar(id, CasoReplica.golden(id), cerrado(id, false, true, false));
        }
        String dia = CasoReplica.id("REP-DIA-", "param-s-copia-el-periodo-al-dia");
        CasoReplica.comparar(dia, CasoReplica.golden(dia), cerrado(dia, false, true, false));
        String opt = CasoReplica.id("REP-OPT-", "sin-dias-inhabiles-guarda-cuatro-envios");
        sinEfectos(cerrado(opt, false, true, false), opt);
    }

    @Test
    @DisplayName("G-OPT: con la compuerta Optimus cerrada el respaldo Optimus no escribe ni llama a ningun colaborador")
    void optimus_cerrado() throws Exception {
        for (String f : new String[] { "sin-dias-inhabiles-guarda-cuatro-envios", "fin-de-semana-copia-dos-dias-y-guarda-doce-envios", "fecha-nula-falla-en-el-primer-borrado" }) {
            String id = CasoReplica.id("REP-OPT-", f);
            JsonObject d = cerrado(id, true, true, false);
            sinEfectos(d, id);
            assertEquals(0, d.getAsJsonArray("colaboradores").size(), id);
        }
    }

    @Test
    @DisplayName("G-OPT: la compuerta Optimus abierta sola deja pasar el respaldo Optimus (equivale al legacy)")
    void optimus_abierto_solo() throws Exception {
        for (String f : new String[] { "fin-de-semana-copia-dos-dias-y-guarda-doce-envios", "falla-el-upsert-del-envio" }) {
            String id = CasoReplica.id("REP-OPT-", f);
            CasoReplica.comparar(id, CasoReplica.golden(id), cerrado(id, false, false, true));
        }
    }

    static Stream<String> casosSinCompuerta() {
        return CasoReplica.casos().stream().filter(id -> {
            JsonObject c = CasoReplica.caso(id);
            String servicio = c.get("servicio").getAsString();
            if (id.startsWith("REP-SEB-") || id.startsWith("REP-DIA-") || id.startsWith("REP-OPT-")) return false;
            if (servicio.equals("EnviarTablaReplica.logica")) {
                var e = c.getAsJsonObject("entrada");
                String p = e.has("p_Proceso") && !e.get("p_Proceso").isJsonNull() ? e.get("p_Proceso").getAsString() : "";
                return !p.equalsIgnoreCase("INICIO_PAUTA") && !p.equalsIgnoreCase("FIN_PAUTA");
            }
            return true;
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosSinCompuerta")
    @DisplayName("Con las tres compuertas cerradas lo que no pasa por ellas sigue equivaliendo al legacy")
    void con_todo_cerrado_el_resto_equivale(String id) throws Exception {
        CasoReplica.comparar(id, CasoReplica.golden(id), CasoReplica.ejecutar(id, c -> c.conCompuertas(false, false, false)));
    }

    @Test
    @DisplayName("Las compuertas son independientes: se necesitan las tres abiertas solo para los tres caminos")
    void se_recuentan_los_casos_sin_compuerta() {
        long n = casosSinCompuerta().count();
        assertTrue(n > 100, "the cases that do not touch a gate are the majority: " + n);
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- collaborators not ported

    private static JsonObject sinColaboradores(String id) throws Exception {
        return CasoReplica.ejecutar(id, c -> c.conColaboradoresNoPortados());
    }

    @Test
    @DisplayName("Colaboradores no portados: con el puerto por defecto (cerrado) COPIA_PATRIMONIO_PARA_INHABIL, FIN_PAUTA, respaldarDataDiaInhabil y el respaldo Optimus fallan sin escribir")
    void colaboradores_cerrados_sin_efectos() throws Exception {
        for (String[] f : new String[][] { { "REP-ENV-", "copia-patrimonio-fin-de-semana-dos-workstack" }, { "REP-ENV-", "fin-pauta-completo-offset-cero" }, { "REP-INH-", "cuenta-inversion-fin-de-semana" },
                { "REP-INH-", "custodia-fin-de-semana" }, { "REP-ENV-", "valorizar-ffmm-fin-de-semana-copia-custodia" } }) {
            String id = CasoReplica.id(f);
            JsonObject d = sinColaboradores(id);
            assertEquals(SNS, excepcion(d), id);
            assertEquals(0, d.getAsJsonArray("colaboradores").size(), id + ": the closed port does not record");
            if (id.contains("fin-pauta")) assertEquals(0, sentencias(d), id);
        }
    }

    @Test
    @DisplayName("Colaboradores no portados: los respaldos y el inicio de la pauta fallan con ServicioNoServidoException y no con un error de otro tipo")
    void colaboradores_cerrados_clase() throws Exception {
        for (String[] f : new String[][] { { "REP-ENV-", "inicio-pauta-fin-de-semana-copia-dos-dias" }, { "REP-SEB-", "param-s-reemplaza-el-periodo-en-las-13" }, { "REP-DIA-", "param-s-copia-el-periodo-al-dia" },
                { "REP-OPT-", "sin-dias-inhabiles-guarda-cuatro-envios" }, { "REP-INI-", "full-logica-encola-la-pauta" }, { "REP-INI-", "delta-call-devuelve-el-mensaje-ok" } }) {
            String id = CasoReplica.id(f);
            assertEquals(SNS, excepcion(sinColaboradores(id)), id);
        }
    }

    @Test
    @DisplayName("Colaboradores no portados: IniciarReplica falla antes de escribir; lo que no usa colaboradores (guardarEnvioPostPauta, call) no se ve afectado")
    void colaboradores_cerrados_no_afectan_lo_demas() throws Exception {
        String ini = CasoReplica.id("REP-INI-", "full-logica-encola-la-pauta");
        JsonObject d = sinColaboradores(ini);
        assertEquals(0, sentencias(d));
        assertEquals(0, filas(d, WS).size());
        for (String[] f : new String[][] { { "REP-POS-", "guarda-una-terna" }, { "REP-POS-", "misma-terna-no-duplica" }, { "REP-CAL-", "enviar-tabla-replica-call-devuelve-null" },
                { "REP-ENV-", "pauta-patrimonio-encola-sin-tocar-datos" }, { "REP-ENV-", "cambio-fecha-sistema-encola-sin-tocar-datos" } }) {
            String id = CasoReplica.id(f);
            CasoReplica.comparar(id, CasoReplica.golden(id), sinColaboradores(id));
        }
    }

    @Test
    @DisplayName("Colaboradores no portados: cada metodo de cada uno de los 6 puertos del valor por defecto lanza ServicioNoServidoException")
    void cada_puerto_cerrado_lanza() throws Exception {
        Object cerrado = ApiReplica.construir(ApiReplica.P + "ColaboradoresNoPortados");
        int metodos = 0;
        for (String puerto : List.of("ConsultarEmpresaPort", "ObtenerDiaSiguientePort", "FeriadosPort", "ConfiguracionSistemaPort", "FechaSistemaPort", "SecuenciaPort")) {
            Class<?> i = ApiReplica.clase(ApiReplica.P + puerto);
            assertTrue(i.isInstance(cerrado), puerto);
            for (java.lang.reflect.Method m : i.getMethods()) {
                Object[] args = new Object[m.getParameterCount()];
                Class<?>[] t = m.getParameterTypes();
                for (int k = 0; k < t.length; k++) {
                    args[k] = t[k] == Long.class || t[k] == long.class ? (Object) 0L : t[k] == String.class ? "" : t[k] == JsonObject.class ? new JsonObject() : null;
                }
                try {
                    m.invoke(cerrado, args);
                    throw new AssertionError(puerto + "." + m.getName() + " must raise ServicioNoServidoException");
                } catch (java.lang.reflect.InvocationTargetException e) {
                    assertEquals(SNS, e.getCause().getClass().getSimpleName(), puerto + "." + m.getName());
                }
                metodos++;
            }
        }
        assertEquals(7, metodos);
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- production defaults (Spring)

    @Test
    @DisplayName("Compuertas por defecto: sin propiedades las tres compuertas estan cerradas")
    void por_defecto_cerradas() throws Exception {
        Object c = ApiReplica.compuertasSpring(Map.of());
        String inicio = CasoReplica.id("REP-ENV-", "inicio-pauta-sin-inhabiles");
        sinEfectos(CasoReplica.ejecutar(inicio, x -> x.conCompuertasObjeto(c)), inicio);
        String sebra = CasoReplica.id("REP-SEB-", "param-s-reemplaza-el-periodo-en-las-13");
        sinEfectos(CasoReplica.ejecutar(sebra, x -> x.conCompuertasObjeto(c)), sebra);
        String opt = CasoReplica.id("REP-OPT-", "sin-dias-inhabiles-guarda-cuatro-envios");
        sinEfectos(CasoReplica.ejecutar(opt, x -> x.conCompuertasObjeto(c)), opt);
    }

    @Test
    @DisplayName("Compuertas por defecto: cada propiedad replica.compuertas.* abre solo su compuerta, con true exacto")
    void propiedades_abren_su_compuerta() throws Exception {
        Object trg = ApiReplica.compuertasSpring(Map.of("replica.compuertas.triggers", "true"));
        Object seb = ApiReplica.compuertasSpring(Map.of("replica.compuertas.respaldo-sebra", "true"));
        Object opt = ApiReplica.compuertasSpring(Map.of("replica.compuertas.respaldo-optimus", "true"));
        String inicio = CasoReplica.id("REP-ENV-", "inicio-pauta-sin-inhabiles");
        String sebra = CasoReplica.id("REP-SEB-", "param-s-reemplaza-el-periodo-en-las-13");
        String optimus = CasoReplica.id("REP-OPT-", "sin-dias-inhabiles-guarda-cuatro-envios");
        Map<String, Object> abiertas = Map.of(inicio, trg, sebra, seb, optimus, opt);
        for (var par : abiertas.entrySet()) {
            JsonObject d = CasoReplica.ejecutar(par.getKey(), x -> x.conCompuertasObjeto(par.getValue()));
            CasoReplica.comparar(par.getKey(), CasoReplica.golden(par.getKey()), d);
        }
        for (String id : List.of(inicio, sebra, optimus)) {
            for (var par : abiertas.entrySet()) {
                if (par.getKey().equals(id)) continue;
                assertEquals(SNS, excepcion(CasoReplica.ejecutar(id, x -> x.conCompuertasObjeto(par.getValue()))), id + " must stay closed when only the gate of " + par.getKey() + " is open");
            }
        }
        assertNull(excepcion(CasoReplica.ejecutar(inicio, x -> x.conCompuertasObjeto(ApiReplica.compuertasSpring(Map.of("replica.compuertas.triggers", true))))));
    }
}
