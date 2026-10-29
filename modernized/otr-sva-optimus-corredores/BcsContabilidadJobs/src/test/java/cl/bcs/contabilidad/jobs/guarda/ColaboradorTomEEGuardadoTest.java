package cl.bcs.contabilidad.jobs.guarda;

import cl.bcs.contabilidad.domain.port.out.ProcesoBatchPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The guard on its own (step 6): marks before and after the call, failure path, classification, pass-through, isolation between programs. */
class ColaboradorTomEEGuardadoTest extends GuardaBase {

    private static final String IPB = "InscribirProcesoBatch";

    @Test
    @DisplayName("G6-D01: INTENTO is already COMMITTED (visible from another connection) when the call goes out; HECHO follows; n counts the guarded calls")
    void marcasAntesYDespues() {
        List<EstadoMarcas> vistoEnLaLlamada = new ArrayList<>();
        comportamientoTomee = nombre -> vistoEnLaLlamada.add(marcas.estado(21));
        programa(21);
        guarda.abrir(21, 1, TIPO + 21);

        invocar(IPB);
        invocar(IPB);

        assertThat(vistoEnLaLlamada.get(0).sinCierre()).extracting(MarcaGuarda::n).containsExactly(1);
        assertThat(vistoEnLaLlamada.get(1).sinCierre()).extracting(MarcaGuarda::n).containsExactly(2);
        assertThat(mensajes(21)).containsExactly("H4G|INTENTO|21|1|" + IPB + "|1", "H4G|HECHO|21|1|" + IPB + "|1",
                "H4G|INTENTO|21|1|" + IPB + "|2", "H4G|HECHO|21|1|" + IPB + "|2");
        assertThat(db.jdbc().queryForList("SELECT \"IdTipoPrograma\" FROM \"BSCL.Plataforma.BD::BAT.LogPrograma\" WHERE \"IdPrograma\" = 21", String.class))
                .containsOnly(TIPO + 21);
        assertThat(tomee).containsExactly(IPB, IPB);
    }

    @Test
    @DisplayName("G6-D02: a failing call writes FALLO, rethrows the SAME exception and is not retried")
    void falloSeRelanza() {
        RestClientResponseException error = new RestClientResponseException("500", 500, "Internal Server Error", null, null, null);
        comportamientoTomee = nombre -> {
            throw error;
        };
        programa(22);
        guarda.abrir(22, 1, TIPO + 22);

        assertThatThrownBy(() -> invocar(IPB)).isSameAs(error);

        assertThat(mensajes(22)).containsExactly("H4G|INTENTO|22|1|" + IPB + "|1", "H4G|FALLO|22|1|" + IPB + "|1");
        assertThat(tomee).hasSize(1);
        assertThat(guarda.contexto().actual().orElseThrow().falladas()).hasSize(1);
        assertThat(guarda.contexto().actual().orElseThrow().hechas()).isEmpty();
    }

    @Test
    @DisplayName("G6-D03: a failure that is an Error is also marked FALLO and rethrown")
    void errorTambien() {
        comportamientoTomee = nombre -> {
            throw new StackOverflowError("simulado");
        };
        programa(23);
        guarda.abrir(23, 1, TIPO + 23);

        assertThatThrownBy(() -> invocar(IPB)).isInstanceOf(StackOverflowError.class);
        assertThat(mensajes(23)).containsExactly("H4G|INTENTO|23|1|" + IPB + "|1", "H4G|FALLO|23|1|" + IPB + "|1");
    }

    @Test
    @DisplayName("G6-D04: reads, the in-process clearing, AgregarLog and unknown names go through with NO marks and no counting")
    void noGuardadosNoDejanMarcas() {
        programa(24);
        guarda.abrir(24, 1, TIPO + 24);

        for (String nombre : List.of("ObtenerDiaAnterior", "ConsultarClearingFinanciero", "AgregarLog", "JdbcEnConexion", "Desconocido")) {
            invocar(nombre);
        }
        invocar(IPB);

        assertThat(tomee).hasSize(6);
        assertThat(mensajes(24)).containsExactly("H4G|INTENTO|24|1|" + IPB + "|1", "H4G|HECHO|24|1|" + IPB + "|1"); // n = 1: the others were not counted
    }

    @Test
    @DisplayName("G6-D05: caller mode (ContextoGuarda(false): no idPrograma by design) a guarded name is a pass-through: no marks, the answer unchanged (step 6b: with a programa-mode context and no scope it is REFUSED, see GuardaSinContextoTest)")
    void sinProgramaEsPassThrough() {
        assertThat(new GuardaTomee(marcas, new ContextoGuarda(false)).llamadas().invocar(IPB, () -> {
            tomee.add(IPB);
            return IPB;
        })).isEqualTo(IPB);

        assertThat(tomee).containsExactly(IPB);
        assertThat(db.jdbc().queryForObject("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::BAT.LogPrograma\"", Integer.class)).isZero();
    }

    @Test
    @DisplayName("G6-D06: when the INTENTO mark cannot be written the call is NOT made (fail closed)")
    void sinMarcaNoHayLlamada() {
        MarcasRepositorio roto = new MarcasRepositorio() {
            @Override
            public void registrar(MarcaGuarda marca, String idTipoPrograma) {
                throw new GuardaTomeeException("base de datos caida");
            }

            @Override
            public EstadoMarcas estado(long idPrograma) {
                throw new GuardaTomeeException("base de datos caida");
            }
        };
        guarda.abrir(25, 1, TIPO);

        assertThatThrownBy(() -> new ColaboradorTomEEGuardado(roto, contexto).invocar(IPB, () -> invocar("llamada")))
                .isInstanceOf(GuardaTomeeException.class);
        assertThat(tomee).isEmpty();
    }

    @Test
    @DisplayName("G6-D07: when the HECHO mark cannot be written the job is stopped (exception), the call is counted as done and the INTENTO stays open")
    void sinHechoSeDetiene() {
        List<MarcaGuarda> escritas = new ArrayList<>();
        MarcasRepositorio sinHecho = new MarcasRepositorio() {
            @Override
            public void registrar(MarcaGuarda marca, String idTipoPrograma) {
                if (marca.fase() == MarcaGuarda.Fase.HECHO) {
                    throw new GuardaTomeeException("se cayo");
                }
                escritas.add(marca);
            }

            @Override
            public EstadoMarcas estado(long idPrograma) {
                return EstadoMarcas.deMarcas(escritas, 0);
            }
        };
        guarda.abrir(26, 1, TIPO);

        assertThatThrownBy(() -> new ColaboradorTomEEGuardado(sinHecho, contexto).invocar(IPB, () -> invocar("llamada")))
                .isInstanceOf(GuardaTomeeException.class);

        assertThat(tomee).hasSize(1);
        assertThat(contexto.actual().orElseThrow().hechas()).hasSize(1);
        assertThat(sinHecho.estado(26).sinCierre()).hasSize(1);
    }

    @Test
    @DisplayName("G6-D08: two programs running at the same time on two threads write their own marks and do not see each other's")
    void concurrenciaEntreProgramas() throws Exception {
        programa(31);
        programa(32);
        marca(MarcaGuarda.Fase.INTENTO, 32, 1, IPB, 1); // an open INTENTO of program 32 only
        CountDownLatch dentro = new CountDownLatch(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<EstadoMarcas>> futuros = new ArrayList<>();
            for (long id : new long[]{31, 33}) {
                futuros.add(pool.submit(() -> {
                    guarda.abrir(id, 1, TIPO);
                    try {
                        EstadoMarcas previo = marcas.estado(id);
                        dentro.countDown();
                        dentro.await(10, TimeUnit.SECONDS);
                        invocar(IPB);
                        return previo;
                    } finally {
                        guarda.cerrar();
                    }
                }));
            }
            for (Future<EstadoMarcas> f : futuros) {
                assertThat(f.get(20, TimeUnit.SECONDS).limpio()).isTrue(); // 32's open INTENTO is invisible to 31 and 33
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(mensajes(31)).containsExactly("H4G|INTENTO|31|1|" + IPB + "|1", "H4G|HECHO|31|1|" + IPB + "|1");
        assertThat(mensajes(33)).containsExactly("H4G|INTENTO|33|1|" + IPB + "|1", "H4G|HECHO|33|1|" + IPB + "|1");
        assertThat(mensajes(32)).containsExactly("H4G|INTENTO|32|1|" + IPB + "|1");
        assertThat(contexto.actual()).isEmpty(); // the thread-local of the test thread was never set by the workers
    }

    @Test
    @DisplayName("G6-D09: the real port decorator: ProcesoBatchPort.inscribir is announced, made once, closed with HECHO; a failure marks FALLO and rethrows")
    void decoradorDelPuerto() {
        List<String> llamadasHttp = new ArrayList<>();
        ProcesoBatchPort http = (empresa, modulo, usuario, tipo, desfase, parametros) -> {
            llamadasHttp.add(tipo);
            if ("FALLA".equals(tipo)) {
                throw new IllegalStateException("HTTP 503");
            }
            return new ProcesoBatchPort.Inscripcion(0, List.of(), 7L);
        };
        ProcesoBatchPort guardado = guarda.envolver(http);
        guarda.abrir(41, 1, TIPO);

        assertThat(guardado.inscribir(1L, "CTB", 2L, "ASIENTO_SAP", 0, List.of()).idPrograma()).isEqualTo(7L);
        assertThatThrownBy(() -> guardado.inscribir(1L, "CTB", 2L, "FALLA", 0, List.of())).isInstanceOf(IllegalStateException.class);

        assertThat(llamadasHttp).containsExactly("ASIENTO_SAP", "FALLA");
        assertThat(mensajes(41)).containsExactly("H4G|INTENTO|41|1|" + IPB + "|1", "H4G|HECHO|41|1|" + IPB + "|1",
                "H4G|INTENTO|41|1|" + IPB + "|2", "H4G|FALLO|41|1|" + IPB + "|2");
    }
}
