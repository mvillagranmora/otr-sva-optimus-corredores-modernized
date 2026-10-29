package cl.bcs.apv.jobs.programa;

import cl.bcs.apv.jobs.support.ProgramaH2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Job pattern H4, final states: {@link ProgramaRepositorioJdbc#terminar}, {@code error}, {@code requiereRevision} and the
 * {@link ProgramaStatusListener}, with the exact legacy semantics of TransactionManager.updateProgramm (:212-257).
 */
class ProgramaEstadoFinalTest {

    private static final String CLASE = "cl.bcs.apv.Prueba";
    private static final String TIPO = "GENERARARCHIVOPAGO";

    private final ProgramaH2 db = new ProgramaH2();
    private final ProgramaRepositorioJdbc repo = new ProgramaRepositorioJdbc(db.dataSource());

    @AfterEach
    void cerrar() {
        db.close();
    }

    private void programaReclamado(long id, long agenda, int max) {
        db.tipo(TIPO + id, CLASE, max);
        if (agenda != 0) {
            db.agenda(agenda);
        }
        db.programa(id, TIPO + id, agenda);
        assertThat(repo.reclamar(id)).isTrue();
    }

    @Test
    @DisplayName("H4-F01: terminar writes TER, Resultado, fechaFin = database UTC clock and Agenda.UltimaFechaDeEjecucion (updateProgramm 1)")
    void terminarEscribeTer() {
        programaReclamado(400, 5, 3);
        assertThat(db.agendaUltima(5)).isNull();

        assertThat(repo.terminar(400, "Finalizado correctamente.")).isTrue();

        assertThat(db.fila(400)).containsEntry("idEstado", "TER").containsEntry("Resultado", "Finalizado correctamente.")
                .containsEntry("fechaFin", ProgramaH2.RELOJ).containsEntry("Bloqueo", "S").containsEntry("Intento", 1);
        assertThat(db.agendaUltima(5)).isEqualTo(ProgramaH2.RELOJ);
    }

    @Test
    @DisplayName("H4-F02: error writes ERR with the same columns; a program without agenda (IdAgenda 0) updates no Agenda row")
    void errorEscribeErr() {
        programaReclamado(401, 0, 3);
        db.agenda(9);

        assertThat(repo.error(401, "Terminado con errores:boom")).isTrue();

        assertThat(db.fila(401)).containsEntry("idEstado", "ERR").containsEntry("Resultado", "Terminado con errores:boom")
                .containsEntry("fechaFin", ProgramaH2.RELOJ);
        assertThat(db.agendaUltima(9)).isNull();
    }

    @Test
    @DisplayName("H4-F03: Resultado longer than 2000 is cut to 1999 characters; 2000 exactly is kept; 2000 plus blanks is cut too")
    void truncadoDeResultado() {
        programaReclamado(402, 0, 3);
        String largo = "x".repeat(2500);

        repo.terminar(402, largo);
        assertThat((String) db.fila(402).get("Resultado")).hasSize(1999).isEqualTo(largo.substring(0, 1999));

        repo.terminar(402, "y".repeat(2000));
        assertThat((String) db.fila(402).get("Resultado")).hasSize(2000);

        repo.terminar(402, "z".repeat(2000) + "   ");
        assertThat((String) db.fila(402).get("Resultado")).hasSize(1999);
        assertThat(TextoResultado.truncar(null)).isEmpty();
    }

    @Test
    @DisplayName("H4-F04: terminar and error on a missing program return false and write nothing")
    void programaInexistente() {
        assertThat(repo.terminar(999, "x")).isFalse();
        assertThat(repo.error(999, "x")).isFalse();
        assertThat(repo.requiereRevision(999, "x")).isFalse();
    }

    @Test
    @DisplayName("H4-F05: requiereRevision = ERR, Intento = MaximoIntentos, Bloqueo S, Resultado with the 'REQUIERE REVISION: ' prefix")
    void requiereRevision() {
        programaReclamado(403, 5, 3);

        assertThat(repo.requiereRevision(403, "colaborador confirmo y el job fallo")).isTrue();

        assertThat(db.fila(403)).containsEntry("idEstado", "ERR").containsEntry("Intento", 3).containsEntry("Bloqueo", "S")
                .containsEntry("Resultado", "REQUIERE REVISION: colaborador confirmo y el job fallo").containsEntry("fechaFin", ProgramaH2.RELOJ);
        assertThat(db.agendaUltima(5)).isEqualTo(ProgramaH2.RELOJ);
    }

    @Test
    @DisplayName("H4-F06: a program in 'requires review' is neither read by the legacy reader (PEN, Bloqueo N) nor by the modern retry (Intento < Max)")
    void revisionNoLaTomanNiElLegacyNiElReintento() {
        programaReclamado(404, 0, 2);
        repo.requiereRevision(404, "x");

        Integer legacy = db.jdbc().queryForObject("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::BAT.Programa\" P INNER JOIN \"BSCL.Plataforma.BD::BAT.TipoPrograma\" TP "
                + "ON P.\"IdTipoPrograma\" = TP.\"IdTipoPrograma\" WHERE P.\"Bloqueo\" = 'N' AND P.\"idEstado\" = 'PEN' AND P.\"Intento\" < TP.\"MaximoIntentos\"", Integer.class);
        Integer reintento = db.jdbc().queryForObject("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::BAT.Programa\" P INNER JOIN \"BSCL.Plataforma.BD::BAT.TipoPrograma\" TP "
                + "ON P.\"IdTipoPrograma\" = TP.\"IdTipoPrograma\" WHERE P.\"idEstado\" = 'ERR' AND P.\"Bloqueo\" = 'S' AND P.\"Intento\" < TP.\"MaximoIntentos\"", Integer.class);
        assertThat(legacy).isZero();
        assertThat(reintento).isZero();
        assertThat(repo.reclamar(404)).isFalse();
    }

    @Test
    @DisplayName("H4-F07: the listener writes TER 'Finalizado correctamente.' for status(id, false, \"\") and ERR 'Terminado con errores:<msg>' for an error")
    void listenerEscribeElEstadoDelLegacy() {
        programaReclamado(405, 0, 3);
        programaReclamado(406, 0, 3);
        ProgramaStatusListener listener = new ProgramaStatusListener(repo);

        listener.status(405, false, "");
        listener.status(406, true, "fallo en paso 2");

        assertThat(db.fila(405)).containsEntry("idEstado", "TER").containsEntry("Resultado", "Finalizado correctamente.");
        assertThat(db.fila(406)).containsEntry("idEstado", "ERR").containsEntry("Resultado", "Terminado con errores:fallo en paso 2");
    }

    @Test
    @DisplayName("H4-F08: an exception without message gives 'Terminado con errores:null' as the legacy string concatenation; a missing row is only logged")
    void listenerMensajeNuloYFilaInexistente() {
        programaReclamado(407, 0, 3);
        ProgramaStatusListener listener = new ProgramaStatusListener(repo);

        listener.status(407, true, null);
        listener.status(999, false, "");

        assertThat(db.fila(407)).containsEntry("Resultado", "Terminado con errores:null");
    }

    @Test
    @DisplayName("H4-F09: the listener marks 'requires review' on request")
    void listenerMarcaRevision() {
        programaReclamado(408, 0, 2);

        new ProgramaStatusListener(repo).marcarRequiereRevision(408, "motivo");

        assertThat(db.fila(408)).containsEntry("idEstado", "ERR").containsEntry("Intento", 2).containsEntry("Bloqueo", "S");
        assertThat((String) db.fila(408).get("Resultado")).startsWith(ProgramaRepositorio.PREFIJO_REVISION);
        assertThat(db.fila(408).get("fechaFin")).isInstanceOf(Timestamp.class);
    }
}
