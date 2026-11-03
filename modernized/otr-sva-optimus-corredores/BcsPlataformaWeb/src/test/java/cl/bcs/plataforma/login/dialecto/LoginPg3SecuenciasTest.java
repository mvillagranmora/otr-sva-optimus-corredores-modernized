package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * A3-6(c) the two Postgres sequence names that production keeps as Java constants equal the oracle names (xlsx), and no other sequence of the catalog is
 * served: the six sequences of the oracle that the login does not use answer the 503 exception. Also the consistency of the two places where the same fact
 * lives (the Java constant of {@code PostgresDialecto.siguienteValor} and the one-line file {@code sql/postgres/AuditoriaSesion.SECUENCIA_*.sql}).
 */
class LoginPg3SecuenciasTest {

    private static final LoginPgRecursos.Oraculo ORACULO = LoginPgRecursos.Oraculo.cargar();
    private static final Map<String, String> LOGIN = Map.of("BSCL.Plataforma.BD.Secuencias::Log", "AuditoriaSesion.SECUENCIA_LOG", "BSCL.Plataforma.BD.Secuencias::DetalleLog",
            "AuditoriaSesion.SECUENCIA_DETALLE");

    static Stream<String> secuenciasDelLogin() {
        return LOGIN.keySet().stream().sorted();
    }

    static Stream<String> otrasSecuencias() {
        List<String> r = new ArrayList<>();
        for (LoginPgRecursos.Objeto o : ORACULO.objetos.values()) {
            if (o.tipo().equals("secuencia") && !LOGIN.containsKey(o.hana())) {
                r.add(o.hana());
                r.add(o.postgres());
            }
        }
        return r.stream();
    }

    @Test
    @DisplayName("A3-6(c) oracle: 8 sequences in the xlsx, the two of the login among them with the names sq_pla_log and sq_pla_detalle_log")
    void el_oraculo_trae_las_secuencias() {
        long n = ORACULO.objetos.values().stream().filter(o -> o.tipo().equals("secuencia")).count();
        assertEquals(8, n);
        assertEquals("sq_pla_log", ORACULO.objeto("BSCL.Plataforma.BD.Secuencias::Log").postgres());
        assertEquals("sq_pla_detalle_log", ORACULO.objeto("BSCL.Plataforma.BD.Secuencias::DetalleLog").postgres());
        assertEquals(6, otrasSecuencias().count() / 2);
    }

    @ParameterizedTest(name = "A3-6(c) nextval of {0} in Postgres is nextval of the oracle name, nothing else")
    @MethodSource("secuenciasDelLogin")
    void el_nombre_de_la_constante_es_el_del_oraculo(String hana) {
        assertEquals("nextval('" + ORACULO.objeto(hana).postgres() + "')", ApiLoginPg3.postgres().siguienteValor(hana));
    }

    @ParameterizedTest(name = "A3-6(c) the Java constant of {0} and the Postgres file of {1} name the same sequence")
    @MethodSource("pares")
    void constante_y_archivo_coinciden(String hana, String id) {
        String delArchivo = ApiLoginPg3.textoPg(id).strip();
        assertEquals("nextval('" + delArchivo + "')", ApiLoginPg3.postgres().siguienteValor(hana));
        assertEquals(delArchivo, ORACULO.objeto(hana).postgres());
        assertEquals(delArchivo, ApiLoginPg3.sentenciasPostgres().de(SentenciasSql.hana(id)).strip());
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> pares() {
        return LOGIN.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> org.junit.jupiter.params.provider.Arguments.of(e.getKey(), e.getValue()));
    }

    @ParameterizedTest(name = "A3-6(c) the sequence [{0}] of the catalog that the login does not use answers 503 in Postgres, naming it (no name is invented from the oracle at run time)")
    @MethodSource("otrasSecuencias")
    void las_otras_secuencias_del_oraculo_responden_503(String nombre) {
        assertThatThrownBy(() -> ApiLoginPg3.postgres().siguienteValor(nombre)).isInstanceOf(UnsupportedOperationException.class)
                .satisfies(e -> assertThat(String.valueOf(e.getMessage())).contains(nombre));
    }

    @ParameterizedTest(name = "A3-6(c) in HANA the sequence name is quoted and followed by .NEXTVAL, for any of the 8 sequences of the catalog [{0}]")
    @ValueSource(strings = { "BSCL.Plataforma.BD.Secuencias::Log", "BSCL.Plataforma.BD.Secuencias::DetalleLog", "BSCL.Plataforma.BD.Secuencias::Otra" })
    void hana_sin_cambios(String hana) {
        assertEquals("\"" + hana + "\".NEXTVAL", ApiLoginPg3.hana().siguienteValor(hana));
    }
}
