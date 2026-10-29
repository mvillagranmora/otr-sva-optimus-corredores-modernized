package cl.bcs.fondomutuo.valorcuota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallController;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallUnitOfWorkFilter;
import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.RevisarRequisitosBasicosJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.cierre3b.SecuenciaH2;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.normal.SesionHana;
import cl.bcs.fondomutuo.port.out.http.LogPort;

/**
 * Architecture review F3 W2 over the real host (controller + unit-of-work filter on H2): H-1 (the swallowed audit failure is logged without text), M-3 (one INFO per write, counts only),
 * H-3 (fixed lock order, the history keeps its position), L-6 (ids count), the one-flight semaphore (503 CVC_OCUPADA, released in finally), M-1 (scale guard), M-2 (transaction guard)
 * and L-2 (IN chunks). Same H2 universe as {@link CvcAnfitrionTest}; no HANA, no network.
 */
class CvcRevisionArquitecturaTest {

    private static final String VC = "BSCL.FondoMutuo.BD::FMM.ValorCuota";
    private static final String VCH = "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico";
    private static final String LISTA = CargarValorCuotaConfiguracion.LISTA;

    private String url;
    private Connection lector;
    private volatile boolean logFalla;
    private final List<String> escrituras = Collections.synchronizedList(new ArrayList<>());
    private volatile int idsDeMenos;
    private ListAppender<ILoggingEvent> appender;
    private Logger servicioLog;

    @BeforeEach
    void bd() throws Exception {
        url = "jdbc:h2:mem:cvcrev" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        lector = DriverManager.getConnection(url, "sa", "");
        for (String f : new String[] { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" }) {
            CasoCvc.script(lector, Files.readString(CasoCvc.recursos().getParent().resolve("fondomutuo-3a").resolve(f), StandardCharsets.UTF_8));
        }
        for (String f : new String[] { "schema-cvc.sql", "seed/base-cvc.sql" }) {
            CasoCvc.script(lector, Files.readString(CasoCvc.recursos().resolve(f), StandardCharsets.UTF_8));
        }
        lector.commit();
        servicioLog = (Logger) LoggerFactory.getLogger(CargarValorCuotaService.class);
        appender = new ListAppender<>();
        appender.start();
        servicioLog.addAppender(appender);
        servicioLog.setLevel(Level.INFO);
    }

    @AfterEach
    void cerrar() throws SQLException {
        servicioLog.detachAppender(appender);
        cl.bcs.fondomutuo.BdEfimeraH2.cerrar(lector);
    }

    private DataSource ds() {
        return new AbstractDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return new SesionHana(DriverManager.getConnection(url, "sa", ""), url, CasoCvc.RELOJ).conexion();
            }

            @Override
            public Connection getConnection(String u, String p) throws SQLException {
                return getConnection();
            }
        };
    }

    private CargarValorCuotaPort servicio(DataSource ds) {
        ConexionUnidadDeTrabajo conexion = new ConexionUnidadDeTrabajo(ds);
        CargarValorCuotaFabrica.Dependencias d = new CargarValorCuotaFabrica.Dependencias();
        ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(conexion);
        DiccionarioJdbc diccionario = new DiccionarioJdbc(conexion);
        d.configuracion = configuracion;
        d.diccionario = diccionario;
        d.basicos = new RevisarRequisitosBasicosJdbc(conexion, diccionario);
        d.calendario = new CalendarioJdbc(conexion, configuracion);
        d.series = new SeriesFondoJdbc(conexion);
        d.datos = new CargarValorCuotaDatosJdbc(conexion);
        CargarValorCuotaEscritura real = new CargarValorCuotaEscrituraJdbc(conexion, true);   // M-2 guard ON: the host gives a read-write transaction
        d.escritura = new CargarValorCuotaEscritura() {
            @Override
            public void actualizarUltimaCarga(List<CargarValorCuotaContrato.Resultado> filas) {
                escrituras.add("update:" + series(filas));
                real.actualizarUltimaCarga(filas);
            }

            @Override
            public void insertarHistorico(List<CargarValorCuotaContrato.Resultado> filas, List<Long> ids) {
                escrituras.add("historico:" + series(filas) + ":" + ids);
                real.insertarHistorico(filas, ids);
            }

            @Override
            public void eliminarVigentes(List<CargarValorCuotaContrato.Resultado> filas) {
                escrituras.add("delete:" + series(filas));
                real.eliminarVigentes(filas);
            }

            @Override
            public void insertarVigentes(List<CargarValorCuotaContrato.Resultado> filas) {
                escrituras.add("insert:" + series(filas));
                real.insertarVigentes(filas);
            }
        };
        d.secuencias = (nombre, cantidad) -> {
            Connection tx = DataSourceUtils.getConnection(ds);
            try {
                List<Long> ids = new SecuenciaH2(tx, new JsonArray()).siguientes(nombre, cantidad);
                return idsDeMenos > 0 ? new ArrayList<>(ids.subList(0, ids.size() - idsDeMenos)) : ids;
            } finally {
                DataSourceUtils.releaseConnection(tx, ds);
            }
        };
        LogPort log = e -> {
            if (logFalla) throw new IllegalStateException("router caido SECRETO-LOG");
            return new JsonObject();
        };
        d.log = log;
        return CargarValorCuotaFabrica.crear(d);
    }

    private static List<String> series(List<CargarValorCuotaContrato.Resultado> filas) {
        return filas.stream().map(r -> r.IdSerieFondo).toList();
    }

    private MockMvc host() {
        DataSource ds = ds();
        CargarValorCuotaJavaCallHandler handler = new CargarValorCuotaJavaCallHandler(servicio(ds), new Compuertas(LISTA, false, false, false, false, false));
        JavaCallController controller = new JavaCallController(new JavaCallDispatcher(List.of(handler)), ds);
        return standaloneSetup(controller).addFilters(new JavaCallUnitOfWorkFilter(new DataSourceTransactionManager(ds))).build();
    }

    private org.springframework.mock.web.MockHttpServletResponse llamar(MockMvc mvc, String params) throws Exception {
        return mvc.perform(post("/internal/java-call").contentType("application/json")
                .content("{\"class\":\"" + JavaCallDispatcher.CARGAR_VALOR_CUOTA + "\",\"params\":" + params + "}")).andReturn().getResponse();
    }

    private static String carga(String filas, String simular) {
        return "{\"p_IdEmpresa\":1,\"p_IdUsuario\":7,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"" + simular + "\",\"p_AllFunds\":\"N\",\"p_ValorCuota\":[" + filas + "]}";
    }

    private static String fila(String serie, String fecha, String valor) {
        return "{\"IDSERIEFONDO\":\"" + serie + "\",\"FECHACIERRE\":\"" + fecha + "\",\"VALORCUOTA\":" + valor + ",\"IDMONEDA\":\"CLP\",\"OBSERVACION\":\"x\",\"MENSAJEVALIDACION\":\"OK\"}";
    }

    private int filas(String tabla) throws SQLException {
        try (Statement st = lector.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM \"" + tabla + "\"")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private List<String> mensajes(Level nivel) {
        return appender.list.stream().filter(e -> e.getLevel() == nivel).map(ILoggingEvent::getFormattedMessage).toList();
    }

    // ---- H-1, M-3

    @Test
    void h1_elFalloDelLog_seRegistraConClaseYFilas_sinElTextoDeLaExcepcion_yLaCargaSeConfirma() throws Exception {
        logFalla = true;
        var r = llamar(host(), carga(fila("FM001A", "2026-03-02", "1500.5") + "," + fila("FM001B", "2026-03-02", "77.77"), "N"));
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(filas(VCH)).isEqualTo(2);
        assertThat(mensajes(Level.WARN)).containsExactly("CVC audit failed: 2 rows, exception=java.lang.IllegalStateException");
        assertThat(String.join("|", mensajes(Level.WARN))).doesNotContain("SECRETO-LOG").doesNotContain("router");
        assertThat(mensajes(Level.INFO)).hasSize(1);
        assertThat(mensajes(Level.INFO).get(0)).contains("auditLog=failed");
    }

    @Test
    void m3_unaLineaInfoPorEscritura_conRecuentos_sinValoresNiSeries() throws Exception {
        var r = llamar(host(), carga(fila("FM001A", "2026-03-02", "1500.5") + "," + fila("FM001B", "2026-03-02", "77.77"), "N"));
        assertThat(r.getStatus()).isEqualTo(200);
        List<String> info = mensajes(Level.INFO);
        assertThat(info).hasSize(1);
        assertThat(info.get(0)).startsWith("CVC write: correlation=").contains("empresa=1 series=2 rows=2 ids=").contains("auditLog=ok errors=0")
                .doesNotContain("FM001").doesNotContain("1500").doesNotContain("77.77");
        assertThat(mensajes(Level.WARN)).isEmpty();
    }

    @Test
    void m3_simulacion_noEscribeNiRegistraLineaDeEscritura() throws Exception {
        llamar(host(), carga(fila("FM001A", "2026-03-02", "1500.5"), "S"));
        assertThat(mensajes(Level.INFO)).isEmpty();
        assertThat(escrituras).isEmpty();
    }

    // ---- H-3, L-6

    @Test
    void h3_loteDeVigentesYElUpdateVanPorIdSerieFondo_elHistoricoConservaLaPosicionDeLaEntrada() throws Exception {
        var r = llamar(host(), carga(fila("FM001B", "2026-03-02", "77.77") + "," + fila("FM001A", "2026-03-02", "1500.5") + "," + fila("FM003A", "2026-03-02", "5"), "N"));
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(escrituras).hasSize(4);
        assertThat(escrituras.get(0)).isEqualTo("update:[FM001A, FM001B, FM003A]");
        assertThat(escrituras.get(1)).startsWith("historico:[FM001B, FM001A, FM003A]:");
        assertThat(escrituras.get(2)).isEqualTo("delete:[FM001A, FM001B, FM003A]");
        assertThat(escrituras.get(3)).isEqualTo("insert:[FM001A, FM001B, FM003A]");
        assertThat(filas(VC)).isEqualTo(3);
        try (Statement st = lector.createStatement();
                ResultSet rs = st.executeQuery("SELECT \"IdSerieFondo\" FROM \"" + VCH + "\" ORDER BY \"IdValorCuotaHistorico\"")) {
            List<String> porId = new ArrayList<>();
            while (rs.next()) porId.add(rs.getString(1));
            assertThat(porId).as("the sequence ids follow the position of the input").containsExactly("FM001B", "FM001A", "FM003A");
        }
    }

    @Test
    void l6_unPuertoDeSecuenciaConMenosIds_es500Generico_conRollback_yNoEscribeElHistorico() throws Exception {
        idsDeMenos = 1;
        var r = llamar(host(), carga(fila("FM001A", "2026-03-02", "1500.5") + "," + fila("FM001B", "2026-03-02", "77.77"), "N"));
        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(r.getContentAsString()).contains("\"error\":\"internal error\"").doesNotContain("ids");
        assertThat(escrituras).hasSize(1);
        assertThat(filas(VCH)).isZero();
        assertThat(filas(VC)).isZero();
    }

    // ---- one flight per instance

    private static CargarValorCuotaContrato.Salida vacia() {
        return new CargarValorCuotaContrato.Salida();
    }

    private static Compuertas abiertas() {
        return new Compuertas(LISTA, false, false, false, false, false);
    }

    @Test
    void vuelo_elValorPorDefectoEsUnaCargaSimultanea() throws Exception {
        CargarValorCuotaJavaCallHandler h = new CargarValorCuotaJavaCallHandler(e -> vacia(), abiertas());
        java.lang.reflect.Field f = CargarValorCuotaJavaCallHandler.class.getDeclaredField("vuelo");
        f.setAccessible(true);
        assertThat(((java.util.concurrent.Semaphore) f.get(h)).availablePermits()).isEqualTo(1);
        f = CargarValorCuotaJavaCallHandler.class.getDeclaredField("esperaMs");
        f.setAccessible(true);
        assertThat(f.getLong(h)).isEqualTo(2000L);
    }

    @Test
    void vuelo_elSegundoHiloRecibe503CvcOcupada_sinEjecutarNada_yElPrimeroTermina() throws Exception {
        CountDownLatch dentro = new CountDownLatch(1);
        CountDownLatch soltar = new CountDownLatch(1);
        AtomicInteger ejecuciones = new AtomicInteger();
        CargarValorCuotaPort lento = e -> {
            ejecuciones.incrementAndGet();
            dentro.countDown();
            try {
                soltar.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            return vacia();
        };
        CargarValorCuotaJavaCallHandler h = new CargarValorCuotaJavaCallHandler(lento, abiertas(), 1, 50L);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<JsonObject> primero = pool.submit(() -> h.call(null, new JsonObject()));
            assertThat(dentro.await(5, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> h.call(null, new JsonObject())).isInstanceOf(ServicioNoServidoException.class)
                    .satisfies(ex -> assertThat(((ServicioNoServidoException) ex).compuerta()).isEqualTo("CVC_OCUPADA"));
            assertThat(ejecuciones.get()).as("the second call never reached the service").isEqualTo(1);
            soltar.countDown();
            assertThat(primero.get(5, TimeUnit.SECONDS).toString()).contains("P_OUT_ERRORESCONTADOR");
            h.call(null, new JsonObject());   // the permit came back
            assertThat(ejecuciones.get()).isEqualTo(2);
        } finally {
            soltar.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void vuelo_elPermisoSeLiberaTrasUnaExcepcion_yTrasUnContadorMayorACero() throws Exception {
        AtomicInteger n = new AtomicInteger();
        CargarValorCuotaPort servicio = e -> {
            int i = n.incrementAndGet();
            if (i == 1) throw new IllegalStateException("boom");
            CargarValorCuotaContrato.Salida s = vacia();
            if (i == 2) s.P_OUT_ERRORESCONTADOR = 3L;
            return s;
        };
        CargarValorCuotaJavaCallHandler h = new CargarValorCuotaJavaCallHandler(servicio, abiertas(), 1, 0L);
        assertThatThrownBy(() -> h.call(null, new JsonObject())).isInstanceOf(IllegalStateException.class);
        assertThat(h.call(null, new JsonObject()).get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(3L);
        assertThat(h.call(null, new JsonObject()).get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(n.get()).isEqualTo(3);
    }

    @Test
    void vuelo_laCompuertaG0VaPrimero_yNoTomaElPermiso() throws Exception {
        CargarValorCuotaJavaCallHandler h = new CargarValorCuotaJavaCallHandler(e -> vacia(), new Compuertas("", false, false, false, false, false), 1, 0L);
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> h.call(null, new JsonObject())).isInstanceOf(ServicioNoServidoException.class)
                    .satisfies(ex -> assertThat(((ServicioNoServidoException) ex).compuerta()).isEqualTo("G0"));
        }
    }

    @Test
    void vuelo_conDosPermisosEntranDosCargas() throws Exception {
        CountDownLatch dos = new CountDownLatch(2);
        CountDownLatch soltar = new CountDownLatch(1);
        CargarValorCuotaPort servicio = e -> {
            dos.countDown();
            try {
                soltar.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            return vacia();
        };
        CargarValorCuotaJavaCallHandler h = new CargarValorCuotaJavaCallHandler(servicio, abiertas(), 2, 50L);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<JsonObject> a = pool.submit(() -> h.call(null, new JsonObject()));
            Future<JsonObject> b = pool.submit(() -> h.call(null, new JsonObject()));
            assertThat(dos.await(5, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> h.call(null, new JsonObject())).isInstanceOf(ServicioNoServidoException.class);
            soltar.countDown();
            a.get(5, TimeUnit.SECONDS);
            b.get(5, TimeUnit.SECONDS);
        } finally {
            soltar.countDown();
            pool.shutdownNow();
        }
    }

    // ---- M-1

    private static JsonObject conValor(String valor) {
        return JsonParser.parseString("{\"p_ValorCuota\":[{\"IDSERIEFONDO\":\"FM001A\",\"VALORCUOTA\":" + valor + "}]}").getAsJsonObject();
    }

    @Test
    void m1_unaEscalaDesmesurada_sinNingunaLlamadaAlServicio_yElPermisoVuelve() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        CargarValorCuotaJavaCallHandler h = new CargarValorCuotaJavaCallHandler(e -> {
            llamadas.incrementAndGet();
            return vacia();
        }, abiertas(), 1, 0L);
        String ceros = "0." + "0".repeat(100) + "1";   // scale 101
        for (String malo : new String[] { "1E+5000", "1E-5000", "1E+39", ceros, "123456789012345678901234567890123456789.5" }) {
            assertThatThrownBy(() -> h.call(null, conValor(malo))).as(malo).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("VALORCUOTA out of range");
        }
        // Gson itself refuses an exponent of that size (JsonSyntaxException, also a generic 500); the service is not reached either
        assertThatThrownBy(() -> h.call(null, conValor("1E+300000000"))).isInstanceOf(RuntimeException.class);
        assertThat(llamadas.get()).isZero();
        for (String bueno : new String[] { "1500.50", "0.12345678901234567890123", "1E+37", "99999999999999999999999999999999999999", "0.0000000000000000000000000000000000000000000000000000000001", "0", "-3.5" }) {
            h.call(null, conValor(bueno));
        }
        assertThat(llamadas.get()).isEqualTo(7);
    }

    @Test
    void m1_enElAnfitrion_es500Generico_sinElValor_yNoEscribeNada() throws Exception {
        var r = llamar(host(), carga(fila("FM001A", "2026-03-02", "1E+300000000"), "N"));
        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(r.getContentAsString()).contains("\"error\":\"internal error\"").doesNotContain("E+3");
        assertThat(filas(VCH)).isZero();
    }

    // ---- M-2

    @Test
    void m2_conLaGuardia_unaEscrituraSinTransaccionOEnSoloLectura_esIllegalState() throws Exception {
        DataSource ds = ds();
        CargarValorCuotaEscrituraJdbc escritura = new CargarValorCuotaEscrituraJdbc(new ConexionUnidadDeTrabajo(ds), true);
        assertThatThrownBy(() -> escritura.eliminarVigentes(List.of())).isInstanceOf(IllegalStateException.class).hasMessageContaining("transaction");
        assertThatThrownBy(() -> escritura.actualizarUltimaCarga(List.of())).isInstanceOf(IllegalStateException.class);
        TransactionTemplate lectura = new TransactionTemplate(new DataSourceTransactionManager(ds));
        lectura.setReadOnly(true);
        assertThatThrownBy(() -> lectura.executeWithoutResult(s -> escritura.insertarVigentes(List.of()))).isInstanceOf(IllegalStateException.class);
        new TransactionTemplate(new DataSourceTransactionManager(ds)).executeWithoutResult(s -> {
            escritura.actualizarUltimaCarga(List.of());
            escritura.insertarHistorico(List.of(), List.of());
            escritura.eliminarVigentes(List.of());
            escritura.insertarVigentes(List.of());
        });
        new CargarValorCuotaEscrituraJdbc(new ConexionUnidadDeTrabajo(ds)).eliminarVigentes(List.of());   // the harness constructor has no guard
    }

    // ---- L-2

    @Test
    void l2_losTramosDelIn_son1000_unaListaVaciaEsUnTramoVacio_yElResultadoEsElMismo() throws Exception {
        assertThat(CargarValorCuotaDatosJdbc.tramos(List.<String>of())).hasSize(1);
        List<String> grande = new ArrayList<>();
        for (int i = 0; i < 2500; i++) grande.add("X" + i);
        List<List<String>> t = CargarValorCuotaDatosJdbc.tramos(grande);
        assertThat(t.stream().map(List::size).toList()).containsExactly(1000, 1000, 500);
        assertThat(CargarValorCuotaDatosJdbc.tramos(grande.subList(0, 1000))).hasSize(1);

        CargarValorCuotaDatosJdbc datos = new CargarValorCuotaDatosJdbc(new ConexionUnidadDeTrabajo(ds()));
        List<String> isin = new ArrayList<>();
        for (int i = 0; i < 2500; i++) isin.add("NOISIN" + i);
        isin.set(1, "CL0000000004");
        isin.set(2400, "CL0000000001");
        List<String> chico = List.of("CL0000000004", "CL0000000001");
        assertThat(new HashSet<>(datos.seriesPorIsin(isin).stream().map(s -> s.IdSerieFondo + "/" + s.ISIN).toList()))
                .isEqualTo(new HashSet<>(datos.seriesPorIsin(chico).stream().map(s -> s.IdSerieFondo + "/" + s.ISIN).toList())).hasSize(2);
        List<String> series = new ArrayList<>();
        for (int i = 0; i < 2500; i++) series.add("NOSERIE" + i);
        assertThat(datos.valoresVigentesEnFechas(series, List.of("2026-03-02"))).isEmpty();
    }
}
