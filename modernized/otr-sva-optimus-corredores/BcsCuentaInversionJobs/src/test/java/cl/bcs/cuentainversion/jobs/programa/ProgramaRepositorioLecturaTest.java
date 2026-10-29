package cl.bcs.cuentainversion.jobs.programa;

import cl.bcs.cuentainversion.jobs.support.ProgramaH2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Job pattern H4, reading side of {@link ProgramaRepositorioJdbc}: the program row, its class and its parameters (view + ValorBlob). */
class ProgramaRepositorioLecturaTest {

    private static final String CLASE = "cl.bcs.cuentainversion.Prueba";
    private static final String TIPO = "GENERARARCHIVOPAGO";

    private final ProgramaH2 db = new ProgramaH2();
    private final ProgramaRepositorioJdbc repo = new ProgramaRepositorioJdbc(db.dataSource());

    @AfterEach
    void cerrar() {
        db.close();
    }

    @Test
    @DisplayName("H4-L01: cargarPrograma returns the row and the class and MaximoIntentos of its TipoPrograma")
    void cargaFilaYClase() {
        db.tipo(TIPO, CLASE, 3).agenda(5).programa(100, TIPO, 5);

        Programa p = repo.cargarPrograma(100).orElseThrow();

        assertThat(p).isEqualTo(new Programa(100, "PEN", TIPO, 0, "N", "", 5, CLASE, 3));
        assertThat(p.reclamable()).isTrue();
    }

    @Test
    @DisplayName("H4-L02: a program that does not exist is empty (the caller answers 404)")
    void programaInexistente() {
        assertThat(repo.cargarPrograma(999)).isEmpty();
    }

    @Test
    @DisplayName("H4-L03: a program whose type has no TipoPrograma row is returned with a null class, not hidden")
    void tipoSinFila() {
        db.programa(101, "TIPOINEXISTENTE", 0);

        Programa p = repo.cargarPrograma(101).orElseThrow();

        assertThat(p.nombreClase()).isNull();
        assertThat(p.reclamable()).isFalse();
    }

    @Test
    @DisplayName("H4-L04: reclamable() is the legacy reading condition: Bloqueo N, PEN and Intento < MaximoIntentos")
    void reclamableEsLaCondicionDelLegacy() {
        db.tipo(TIPO, CLASE, 2).programa(110, TIPO, 0).programa(111, TIPO, 0).programa(112, TIPO, 0).programa(113, TIPO, 0);
        db.estado(111, "PEN", 0, "S").estado(112, "ASI", 0, "N").estado(113, "PEN", 2, "N");

        assertThat(repo.cargarPrograma(110).orElseThrow().reclamable()).isTrue();
        assertThat(repo.cargarPrograma(111).orElseThrow().reclamable()).as("Bloqueo S").isFalse();
        assertThat(repo.cargarPrograma(112).orElseThrow().reclamable()).as("not PEN").isFalse();
        assertThat(repo.cargarPrograma(113).orElseThrow().reclamable()).as("Intento = Max").isFalse();
    }

    @Test
    @DisplayName("H4-P01: parameters come from the view by name")
    void parametrosDeLaVista() {
        db.tipo(TIPO, CLASE, 1).programa(120, TIPO, 0).programa(121, TIPO, 0);
        db.parametro(120, 1, "IdEmpresa", "1", null).parametro(120, 2, "IdUsuario", "99", null).parametro(121, 3, "IdEmpresa", "2", null);

        assertThat(repo.parametros(120)).containsExactlyInAnyOrderEntriesOf(Map.of("IdEmpresa", "1", "IdUsuario", "99"));
        assertThat(repo.parametros(121)).containsExactlyEntriesOf(Map.of("IdEmpresa", "2"));
        assertThat(repo.parametros(999)).isEmpty();
    }

    @Test
    @DisplayName("H4-P02: ValorBlob, decoded as UTF-8, replaces Valor (TransactionManager.java:490-495)")
    void valorBlobReemplazaAValor() {
        String largo = "Línea ñandú € ".repeat(400); // longer than the 3000 of Valor and with multi-byte characters
        db.tipo(TIPO, CLASE, 1).programa(130, TIPO, 0);
        db.parametro(130, 1, "Texto", "valor corto", largo.getBytes(StandardCharsets.UTF_8)).parametro(130, 2, "Otro", "plano", null);

        Map<String, String> p = repo.parametros(130);

        assertThat(p.get("Texto")).isEqualTo(largo).hasSize(largo.length());
        assertThat(p.get("Otro")).isEqualTo("plano");
    }

    @Test
    @DisplayName("H4-P03: a duplicated name keeps the last value (legacy map put, no ORDER BY); null values are dropped")
    void duplicadosYNulos() {
        db.tipo(TIPO, CLASE, 1).programa(140, TIPO, 0);
        db.parametro(140, 1, "X", "primero", null).parametro(140, 2, "X", "ultimo", null).parametro(140, 3, "Vacio", null, null);

        Map<String, String> p = repo.parametros(140);

        assertThat(p).containsExactlyEntriesOf(Map.of("X", "ultimo"));
    }

    @Test
    @DisplayName("H4-P04: a database error is NOT swallowed (the legacy returned an empty map silently)")
    void errorDeBaseNoSeTraga() {
        db.jdbc().execute("DROP VIEW \"BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas\"");

        assertThatThrownBy(() -> repo.parametros(1)).isInstanceOf(ProgramaJdbcException.class).hasCauseInstanceOf(java.sql.SQLException.class);
    }
}
