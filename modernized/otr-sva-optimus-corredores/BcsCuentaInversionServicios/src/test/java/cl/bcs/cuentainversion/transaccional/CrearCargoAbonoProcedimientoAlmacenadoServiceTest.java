package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import cl.bcs.cuentainversion.service.CalendarioHabil;
import cl.bcs.cuentainversion.service.CrearCargoAbonoService;
import cl.bcs.cuentainversion.service.FormatoMoneda;
import cl.bcs.cuentainversion.transaccional.support.ColaboradoresDeCaso;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.Canon;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.ProcedimientoDeCaso;
import com.google.gson.JsonObject;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of the HANA stored-procedure path of legacy {@code CrearCargoAbono} ({@code PLA.UsaCargoAbonoJava = N}): the Java side batch-inserts
 * the movements in the global temporary input table, calls the procedure {@code BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono} and reads its four output tables.
 * The procedure itself (HANA SQLScript) is NOT reproducible and its logic stays UNCHARACTERISED: the oracle records the call as a collaborator ("StoredProcedure") and the
 * case seed plays the procedure by pre-filling the output tables. What is pinned: the rows handed to the procedure, how the output rows become messages and counters.
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-241..246}. Legacy: {@code CrearCargoAbono}; modern: {@link CrearCargoAbonoService}.
 */
@ExtendWith(ZonaUtc.class)
class CrearCargoAbonoProcedimientoAlmacenadoServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-241_via-procedimiento-almacenado-lee-las-tablas-de-salida",
            "CTA-A-242_procedimiento-almacenado-con-advertencia-y-error-cuenta-errores",
            "CTA-A-243_procedimiento-almacenado-sin-salidas-devuelve-vacio",
            "CTA-A-244_sin-movimientos-no-inserta-pero-llama-al-procedimiento",
            "CTA-A-245_parametros-de-entrada-del-procedimiento",
            "CTA-A-246_parametro-usa-cargo-abono-java-se-compara-sin-distinguir-mayusculas");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    /** Arrays whose order comes from SQL without ORDER BY. */
    private static final String[] SIN_ORDEN = {"P_OUT_MENSAJES", "P_OUT_CARGOABONOGENERADO"};

    /** The service declares {@code throws Exception} (the legacy threw plain Exceptions); the exception is handed on unchanged so its type is compared. */
    private static com.google.gson.JsonElement ejecutar(CrearCargoAbonoService service, Caso caso) {
        try {
            return service.execute(caso.entrada().deepCopy());
        } catch (Exception e) {
            return CrearCargoAbonoProcedimientoAlmacenadoServiceTest.<RuntimeException>lanzar(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> com.google.gson.JsonElement lanzar(Throwable t) throws E {
        throw (E) t;
    }

    /**
     * The golden "colaboradores" records the procedure call as {procedimiento, entrada}: every scalar the modern code binds must equal it.
     * p_UsaReglasNegocio is not bound: the SQL fixes it to 'S' (LEGACY DEFECT kept, see README CTA-A-245), so it is the only golden key skipped.
     */
    private static void entradasDelProcedimiento(Caso caso, ProcedimientoDeCaso procedimiento) {
        JsonObject esperada = caso.esperado().getAsJsonArray("colaboradores").get(0).getAsJsonObject().getAsJsonObject("entrada").getAsJsonObject("entrada");
        assertThat(procedimiento.llamadas()).as(caso.nombre() + ": the procedure is called once").hasSize(1);
        Map<String, Object> real = procedimiento.llamadas().get(0);
        JsonObject recibida = new JsonObject();
        real.forEach((k, v) -> recibida.add(k, v == null ? com.google.gson.JsonNull.INSTANCE : new com.google.gson.Gson().toJsonTree(v)));
        JsonObject esperadaAcotada = esperada.deepCopy();
        esperadaAcotada.remove("p_UsaReglasNegocio");
        assertThat(Canon.texto(recibida)).as(caso.nombre() + ": scalar inputs of the procedure").isEqualTo(Canon.texto(esperadaAcotada));
    }

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ProcedimientoDeCaso procedimiento = new ProcedimientoDeCaso();
        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(procedimiento.envolver(caso.conexion()), true));
        CrearCargoAbonoService service = new CrearCargoAbonoService(jdbc, new ActualizarSaldoCargoAbonoService(jdbc),
                new CalendarioHabil(jdbc), new FormatoMoneda(jdbc), new ConfiguracionDelCaso(caso), new DiccionarioDeCaso(),
                new ColaboradoresDeCaso(caso));
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> ejecutar(service, caso));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, SIN_ORDEN);
        entradasDelProcedimiento(caso, procedimiento);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearCargoAbono");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CTA-A-241: CrearCargoAbono.java:406-441: input rows batch-inserted, procedure called, generated rows + information messages read back
    @Test
    void ctaA241_viaProcedimientoAlmacenadoLeeLasTablasDeSalida() {
        verificar(CASOS.get(0));
    }

    // CTA-A-242: CrearCargoAbono.java:442-452: advertencias and errores of the procedure become messages; counter = number of ERROR messages
    @Test
    void ctaA242_procedimientoAlmacenadoConAdvertenciaYErrorCuentaErrores() {
        verificar(CASOS.get(1));
    }

    // CTA-A-243: CrearCargoAbono.java:434-452: a procedure that leaves no output rows -> empty output, counter 0
    @Test
    void ctaA243_procedimientoAlmacenadoSinSalidasDevuelveVacio() {
        verificar(CASOS.get(2));
    }

    // CTA-A-244: CrearCargoAbono.java:426-431: no movements -> no batch insert, the procedure is still called
    @Test
    void ctaA244_sinMovimientosNoInsertaPeroLlamaAlProcedimiento() {
        verificar(CASOS.get(3));
    }

    // CTA-A-245: CrearCargoAbono.java:410-431: scalar inputs handed to the procedure (the SQL fixes p_UsaReglasNegocio => 'S')
    @Test
    void ctaA245_parametrosDeEntradaDelProcedimiento() {
        verificar(CASOS.get(4));
    }

    // CTA-A-246: CrearCargoAbono.java:406: PLA.UsaCargoAbonoJava compared with equalsIgnoreCase('N'): lower case n also takes the procedure path
    @Test
    void ctaA246_parametroUsaCargoAbonoJavaSeComparaSinDistinguirMayusculas() {
        verificar(CASOS.get(5));
    }

}
