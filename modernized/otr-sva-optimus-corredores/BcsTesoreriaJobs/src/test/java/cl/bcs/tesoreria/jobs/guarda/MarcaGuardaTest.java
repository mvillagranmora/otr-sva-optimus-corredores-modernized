package cl.bcs.tesoreria.jobs.guarda;

import cl.bcs.tesoreria.jobs.guarda.MarcaGuarda.Fase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The text format of the guard marks (step 6): format, parse, the pairing of INTENTO with its close, and the limits. */
class MarcaGuardaTest {

    @Test
    @DisplayName("G6-M01: the text is exactly H4G|FASE|idPrograma|intento|clase|n")
    void formato() {
        assertThat(new MarcaGuarda(Fase.INTENTO, 4711, 2, "CrearOrdenes", 3).formatear()).isEqualTo("H4G|INTENTO|4711|2|CrearOrdenes|3");
        assertThat(new MarcaGuarda(Fase.HECHO, 1, 1, "ObtenerPortafoliosEjecutivo.restringeCartera", 12).formatear())
                .isEqualTo("H4G|HECHO|1|1|ObtenerPortafoliosEjecutivo.restringeCartera|12");
        assertThat(new MarcaGuarda(Fase.FALLO, 9, 1, "CrearSolicitudAporteRescateNormal", 1).formatear())
                .isEqualTo("H4G|FALLO|9|1|CrearSolicitudAporteRescateNormal|1");
    }

    @Test
    @DisplayName("G6-M02: parse is the inverse of format for every phase")
    void ida_y_vuelta() {
        for (Fase fase : Fase.values()) {
            MarcaGuarda m = new MarcaGuarda(fase, 123456789012L, 7, "AgregarPrograma", 99);
            assertThat(MarcaGuarda.parsear(m.formatear())).contains(m);
        }
    }

    @Test
    @DisplayName("G6-M03: text that is not a well formed mark does not parse")
    void textoIlegible() {
        for (String texto : new String[]{null, "", "Hola", "h4g|INTENTO|1|1|X|1", "H4G|INTENTO|1|1|X", "H4G|INTENTO|1|1|X|1|extra",
                "H4G|OTRA|1|1|X|1", "H4G|INTENTO|uno|1|X|1", "H4G|INTENTO|1|1||1", "H4G|INTENTO|1|1|X|0", "H4G|INTENTO|-1|1|X|1",
                "H4G|INTENTO|1|1|a b|1", "H4G|INTENTO|1|1|X|1.5"}) {
            assertThat(MarcaGuarda.parsear(texto)).as(String.valueOf(texto)).isEmpty();
        }
    }

    @Test
    @DisplayName("G6-M04: a name with the separator, or an out-of-range number, cannot be built (the text could not be matched back)")
    void noSePuedeConstruirInvalida() {
        assertThatThrownBy(() -> new MarcaGuarda(Fase.INTENTO, 1, 1, "A|B", 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MarcaGuarda(Fase.INTENTO, 1, 1, null, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MarcaGuarda(null, 1, 1, "A", 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MarcaGuarda(Fase.INTENTO, 1, 1, "A", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MarcaGuarda(Fase.INTENTO, 1, 1, "A".repeat(101), 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("G6-M05: the longest mark is far below the 5000 characters of LogPrograma.Mensaje")
    void cabeEnLaColumna() {
        String largo = new MarcaGuarda(Fase.INTENTO, Long.MAX_VALUE, Integer.MAX_VALUE, "A".repeat(100), Integer.MAX_VALUE).formatear();
        assertThat(largo.length()).isLessThan(200);
    }

    @Test
    @DisplayName("G6-M06: INTENTO + HECHO or FALLO of the same call is closed; the other INTENTO is open; HECHO is reported as committed")
    void emparejamiento() {
        MarcaGuarda a = new MarcaGuarda(Fase.INTENTO, 5, 1, "CrearOrdenes", 1);
        MarcaGuarda b = new MarcaGuarda(Fase.INTENTO, 5, 1, "CrearOrdenes", 2);
        MarcaGuarda c = new MarcaGuarda(Fase.INTENTO, 5, 1, "AgregarPrograma", 3);
        EstadoMarcas e = EstadoMarcas.deMarcas(List.of(a, b, c, a.en(Fase.HECHO), c.en(Fase.FALLO)), 0);

        assertThat(e.sinCierre()).containsExactly(b);
        assertThat(e.hechas()).containsExactly(a);
        assertThat(e.limpio()).isFalse();
    }

    @Test
    @DisplayName("G6-M07: only FALLO marks, or no marks, are clean; an unreadable mark is not; attempts do not close each other")
    void limpio() {
        MarcaGuarda a = new MarcaGuarda(Fase.INTENTO, 5, 1, "CrearOrdenes", 1);
        assertThat(EstadoMarcas.deMarcas(List.of(), 0).limpio()).isTrue();
        assertThat(EstadoMarcas.deMarcas(List.of(a, a.en(Fase.FALLO)), 0).limpio()).isTrue();
        assertThat(EstadoMarcas.deMarcas(List.of(), 1).limpio()).isFalse();
        MarcaGuarda otroIntento = new MarcaGuarda(Fase.HECHO, 5, 2, "CrearOrdenes", 1);
        assertThat(EstadoMarcas.deMarcas(List.of(a, otroIntento), 0).sinCierre()).containsExactly(a);
    }

    @Test
    @DisplayName("G6-M08: describe lists clase#n(attempt i) and cuts the list")
    void describir() {
        List<MarcaGuarda> l = List.of(new MarcaGuarda(Fase.INTENTO, 1, 3, "A", 1), new MarcaGuarda(Fase.INTENTO, 1, 3, "B", 2),
                new MarcaGuarda(Fase.INTENTO, 1, 3, "C", 3));
        assertThat(EstadoMarcas.describir(l, 2)).isEqualTo("A#1(attempt 3), B#2(attempt 3), ... (1 more)");
    }
}
