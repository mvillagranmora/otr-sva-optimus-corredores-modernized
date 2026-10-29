package cl.bcs.contabilidad.jobs.programa;

import cl.bcs.contabilidad.jobs.support.ProgramaH2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/** Job pattern H4, the atomic claim of {@link ProgramaRepositorioJdbc#reclamar} (improvement over TransactionManager.java:68-73). */
class ProgramaRepositorioReclamoTest {

    private static final String CLASE = "cl.bcs.contabilidad.Prueba";
    private static final String TIPO = "GENERARARCHIVOPAGO";

    private final ProgramaH2 db = new ProgramaH2();
    private final ProgramaRepositorioJdbc repo = new ProgramaRepositorioJdbc(db.dataSource());

    @AfterEach
    void cerrar() {
        db.close();
    }

    private String fechaFin(long id) {
        return db.jdbc().queryForObject("SELECT CAST(\"fechaFin\" AS VARCHAR) FROM \"BSCL.Plataforma.BD::BAT.Programa\" WHERE \"IdPrograma\" = ?",
                String.class, id);
    }

    @Test
    @DisplayName("H4-R01: the claim sets Bloqueo S, ASI, Intento+1 and 'Programa Encolado' (legacy dequeue state), fechaFin 0001-01-01")
    void reclamoDejaElEstadoDelLegacy() {
        db.tipo(TIPO, CLASE, 3).programa(200, TIPO, 0);

        assertThat(repo.reclamar(200)).isTrue();

        assertThat(db.fila(200)).containsEntry("Bloqueo", "S").containsEntry("idEstado", "ASI").containsEntry("Intento", 1)
                .containsEntry("Resultado", "Programa Encolado");
        assertThat(fechaFin(200)).startsWith("0001-01-01 00:00:00");
    }

    @Test
    @DisplayName("H4-R02: a second claim of the same program finds 0 rows and answers false (already claimed)")
    void segundoReclamoFalla() {
        db.tipo(TIPO, CLASE, 3).programa(201, TIPO, 0);

        assertThat(repo.reclamar(201)).isTrue();
        assertThat(repo.reclamar(201)).isFalse();

        assertThat(db.fila(201)).containsEntry("Intento", 1); // not incremented twice
    }

    @Test
    @DisplayName("H4-R03: not claimable: Bloqueo S, not PEN, Intento = MaximoIntentos or missing row; the row is left untouched")
    void noReclamables() {
        db.tipo(TIPO, CLASE, 1).programa(210, TIPO, 0).programa(211, TIPO, 0).programa(212, TIPO, 0);
        db.estado(210, "PEN", 0, "S").estado(211, "TER", 0, "N").estado(212, "PEN", 1, "N");

        assertThat(repo.reclamar(210)).isFalse();
        assertThat(repo.reclamar(211)).isFalse();
        assertThat(repo.reclamar(212)).isFalse();
        assertThat(repo.reclamar(213)).isFalse();

        assertThat(db.fila(210)).containsEntry("idEstado", "PEN").containsEntry("Intento", 0);
        assertThat(db.fila(211)).containsEntry("idEstado", "TER").containsEntry("Bloqueo", "N");
        assertThat(db.fila(212)).containsEntry("Intento", 1).containsEntry("Resultado", "Esperando ejecucion");
    }

    @Test
    @DisplayName("H4-R04: the claim commits by itself: another connection sees it at once, and it is not part of any job transaction")
    void reclamoConfirmaPorSiMismo() throws Exception {
        db.tipo(TIPO, CLASE, 1).programa(220, TIPO, 0);

        repo.reclamar(220);

        try (var con = db.dataSource().getConnection()) {
            con.setAutoCommit(false);
            try (var rs = con.createStatement().executeQuery("SELECT \"Bloqueo\" FROM \"BSCL.Plataforma.BD::BAT.Programa\" WHERE \"IdPrograma\" = 220")) {
                rs.next();
                assertThat(rs.getString(1)).isEqualTo("S");
            }
        }
    }

    @Test
    @DisplayName("H4-R05: race, two threads claim the same program at once: exactly one wins (repeated for 25 programs)")
    void carreraDeDosHilos() throws Exception {
        db.tipo(TIPO, CLASE, 1);
        for (int i = 0; i < 25; i++) {
            db.programa(300 + i, TIPO, 0);
        }
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < 25; i++) {
                long id = 300 + i;
                CountDownLatch salida = new CountDownLatch(1);
                List<Future<Boolean>> r = new ArrayList<>();
                for (int h = 0; h < 2; h++) {
                    r.add(pool.submit(() -> {
                        salida.await();
                        return repo.reclamar(id);
                    }));
                }
                salida.countDown();
                int ganadores = 0;
                for (Future<Boolean> f : r) {
                    ganadores += f.get() ? 1 : 0;
                }
                assertThat(ganadores).as("claims won for program %d", id).isEqualTo(1);
                assertThat(db.fila(id)).containsEntry("Intento", 1).containsEntry("Bloqueo", "S");
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
