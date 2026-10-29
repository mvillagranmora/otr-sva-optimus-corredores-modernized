package cl.bcs.fondomutuo.valorcuota;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import cl.bcs.fondomutuo.normal.SesionHana;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * FM-CVC-14 / D-F3A-22 at the REAL host: the class has no commit or rollback of its own; the host decides by the counter. Here the real {@link JavaCallController} and the real
 * {@link JavaCallUnitOfWorkFilter} (one Spring transaction on a {@code DataSourceTransactionManager}) run the new CargarValorCuota over H2, and the tables are read from ANOTHER
 * connection after the HTTP answer: what is visible is what was COMMITTED. Counter 0 = commit; counter &gt; 0 (a mixed batch: its OK rows included) = rollback of everything; an
 * exception = generic 500 and rollback; the closed gate = 503 with nothing written. Same H2 universe as the cases ({@code fondomutuo-cvc/}); no HANA, no network.
 */
class CvcAnfitrionTest {

    private static final String VC = "BSCL.FondoMutuo.BD::FMM.ValorCuota";
    private static final String VCH = "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico";

    private String url;
    private Connection lector;
    private final AtomicInteger llamadasLog = new AtomicInteger();
    private volatile boolean logFalla;

    @BeforeEach
    void bd() throws Exception {
        url = "jdbc:h2:mem:cvchost" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        lector = DriverManager.getConnection(url, "sa", "");
        for (String f : new String[] { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" }) {
            CasoCvc.script(lector, Files.readString(CasoCvc.recursos().getParent().resolve("fondomutuo-3a").resolve(f), StandardCharsets.UTF_8));
        }
        for (String f : new String[] { "schema-cvc.sql", "seed/base-cvc.sql" }) {
            CasoCvc.script(lector, Files.readString(CasoCvc.recursos().resolve(f), StandardCharsets.UTF_8));
        }
        lector.commit();
    }

    @AfterEach
    void cerrar() throws SQLException {
        lector.close();
    }

    /** Every connection of the pool is the same H2 database seen through the HANA emulation of the harness (CURRENT_UTCTIMESTAMP, sequences); commit / rollback are real. */
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

    private MockMvc host(String listas) {
        DataSource ds = ds();
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
        d.escritura = new CargarValorCuotaEscrituraJdbc(conexion);
        d.secuencias = (nombre, cantidad) -> {
            Connection tx = DataSourceUtils.getConnection(ds);   // the connection of the unit of work (the one of the filter's transaction)
            try {
                return new SecuenciaH2(tx, new JsonArray()).siguientes(nombre, cantidad);
            } finally {
                DataSourceUtils.releaseConnection(tx, ds);
            }
        };
        LogPort log = e -> {
            llamadasLog.incrementAndGet();
            if (logFalla) throw new IllegalStateException("router caido");
            return new JsonObject();
        };
        d.log = log;
        CargarValorCuotaJavaCallHandler handler = new CargarValorCuotaJavaCallHandler(CargarValorCuotaFabrica.crear(d), new Compuertas(listas, false, false, false, false, false));
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
            int n = rs.getInt(1);
            return n;
        }
    }

    private static long contador(String cuerpo) {
        return JsonParser.parseString(cuerpo).getAsJsonObject().get("P_OUT_ERRORESCONTADOR").getAsLong();
    }

    @Test
    void fmCvc14_contadorCero_commit_ySeVenLasFilasDesdeOtraConexion() throws Exception {
        var r = llamar(host(CargarValorCuotaConfiguracion.LISTA), carga(fila("FM001A", "2026-03-02", "1500.5") + "," + fila("FM001B", "2026-03-02", "77.77"), "N"));
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(contador(r.getContentAsString())).isZero();
        assertThat(filas(VCH)).isEqualTo(2);
        assertThat(filas(VC)).isEqualTo(2);
        assertThat(llamadasLog.get()).isEqualTo(1);
    }

    @Test
    void fmCvc14_D_F3A_22_loteMixto_200ConContadorUno_yRollbackDeTodo_incluidasLasFilasOk() throws Exception {
        var r = llamar(host(CargarValorCuotaConfiguracion.LISTA), carga(fila("FM001A", "2026-03-02", "1500.5") + "," + fila("NOEXISTE", "2026-03-02", "1"), "N"));
        assertThat(r.getStatus()).as("a 200 does not mean committed").isEqualTo(200);
        assertThat(contador(r.getContentAsString())).isEqualTo(1);
        assertThat(r.getContentAsString()).contains("\"DescEstado\":\"SERIE_NO_EXISTE\"").contains("\"DescEstado\":\"OK\"");
        assertThat(filas(VCH)).as("the OK row was written by the class and reverted by the host").isZero();
        assertThat(filas(VC)).isZero();
        assertThat(llamadasLog.get()).as("the audit port WAS called with the OK row: by HTTP its line is not reverted").isEqualTo(1);
    }

    @Test
    void fmCvc14_simulacion_noEscribe_yResponde200ConContadorCero() throws Exception {
        var r = llamar(host(CargarValorCuotaConfiguracion.LISTA), carga(fila("FM001A", "2026-03-02", "1500.5"), "S"));
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(contador(r.getContentAsString())).isZero();
        assertThat(filas(VCH)).isZero();
        assertThat(filas(VC)).isZero();
        assertThat(llamadasLog.get()).isZero();
    }

    @Test
    void fmCvc14_unaEscrituraQueFalla_500Generico_sinTextoDeLaBd_yRollback() throws Exception {
        var r = llamar(host(CargarValorCuotaConfiguracion.LISTA), carga("{\"IDSERIEFONDO\":\"FM001A\",\"FECHACIERRE\":\"2026-03-02\",\"VALORCUOTA\":1500.5,\"IDMONEDA\":\"CLP\","
                + "\"MENSAJEVALIDACION\":\"OK\"}", "N"));   // OBSERVACION absent: NOT NULL violation at the historico insert
        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(r.getContentAsString()).contains("\"error\":\"internal error\"").contains("correlationId").doesNotContain("ValorCuota").doesNotContain("NULL");
        assertThat(filas(VCH)).isZero();
        assertThat(filas(VC)).isZero();
    }

    @Test
    void fmCvc11_elFalloDelLog_noCambiaNada_commit() throws Exception {
        logFalla = true;
        var r = llamar(host(CargarValorCuotaConfiguracion.LISTA), carga(fila("FM001A", "2026-03-02", "1500.5"), "N"));
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(contador(r.getContentAsString())).isZero();
        assertThat(llamadasLog.get()).isEqualTo(1);
        assertThat(filas(VCH)).isEqualTo(1);
        assertThat(filas(VC)).isEqualTo(1);
    }

    @Test
    void fmCvc02_listaVacia_200ConContadorUno_sinEscribir() throws Exception {
        var r = llamar(host(CargarValorCuotaConfiguracion.LISTA), carga("", "N"));
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(contador(r.getContentAsString())).isEqualTo(1);
        assertThat(r.getContentAsString()).contains("NO_SE_INGRESARON_VALORES_CUOTA");
        assertThat(filas(VCH)).isZero();
    }

    @Test
    void compuertaCerradaPorDefecto_503_sinLeerNiEscribir() throws Exception {
        var r = llamar(host(""), carga(fila("FM001A", "2026-03-02", "1500.5"), "N"));
        assertThat(r.getStatus()).isEqualTo(503);
        assertThat(filas(VCH)).isZero();
        assertThat(filas(VC)).isZero();
        assertThat(llamadasLog.get()).isZero();
    }

    @Test
    void repetirLaMismaCargaPorHttp_elHistoricoCreceUnaFila_D_F3A_16() throws Exception {
        MockMvc mvc = host(CargarValorCuotaConfiguracion.LISTA);
        String cuerpo = carga(fila("FM001A", "2026-03-02", "1500.5"), "N");
        assertThat(llamar(mvc, cuerpo).getStatus()).isEqualTo(200);
        var segunda = llamar(mvc, cuerpo);
        assertThat(segunda.getStatus()).isEqualTo(200);
        assertThat(contador(segunda.getContentAsString())).isZero();
        assertThat(segunda.getContentAsString()).doesNotContain("VALOR_EXISTE_FECHA");
        assertThat(filas(VCH)).isEqualTo(2);
        assertThat(filas(VC)).isEqualTo(1);
        List<String> marcas = new ArrayList<>();
        try (Statement st = lector.createStatement(); ResultSet rs = st.executeQuery("SELECT \"EsUltimaCargaDiaria\" FROM \"" + VCH + "\" ORDER BY \"IdValorCuotaHistorico\"")) {
            while (rs.next()) marcas.add(rs.getString(1));
        }
        assertThat(marcas).containsExactly("N", "S");
    }
}
