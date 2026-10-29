package cl.bcs.jobmanager;

import cl.bcs.jobmanager.adapter.out.ProgramaJdbcAdapter;
import cl.bcs.jobmanager.domain.Programa;
import cl.bcs.jobmanager.support.BatH2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.List;

import static cl.bcs.jobmanager.support.BatH2.AHORA;
import static org.assertj.core.api.Assertions.assertThat;

/** The selection predicate equals the legacy readProgramm (TM:41-47) plus the type filter, over the real BAT names. */
class ProgramaSeleccionTest {

    private BatH2 bat;

    @BeforeEach
    void init() {
        bat = new BatH2();
        bat.tipo("T1", "cl.bcs.tesoreria.jobs.UnJob", 1);
        bat.tipo("T2", "cl.bcs.apv.jobs.OtroJob", 3);
        bat.tipo("T0", "cl.bcs.x.CeroIntentos", 0);
    }

    private List<Long> ids(String nodo, String... tipos) {
        return bat.adapter.readEligiblePrograms(nodo, List.of(tipos), AHORA).stream().map(Programa::getIdPrograma).toList();
    }

    @Test
    void elegibleBase_seSeleccionaConLosDatosDeLaClase() {
        long id = bat.programa("T1").insertar();
        List<Programa> r = bat.adapter.readEligiblePrograms("", List.of("T1"), AHORA);
        assertThat(r).extracting(Programa::getIdPrograma).containsExactly(id);
        assertThat(r.get(0).getNombreClasePrograma()).isEqualTo("cl.bcs.tesoreria.jobs.UnJob");
        assertThat(r.get(0).isDemonio()).isFalse();
    }

    @Test
    void soloEstadoPen() {
        for (String e : List.of("ASI", "TER", "ERR")) {
            bat.programa("T1").estado(e).insertar();
        }
        assertThat(ids("", "T1")).isEmpty();
    }

    @Test
    void soloBloqueoN() {
        bat.programa("T1").bloqueo("S").insertar();
        assertThat(ids("", "T1")).isEmpty();
    }

    @Test
    void intentoDebeSerMenorQueMaximoIntentos() {
        long ok = bat.programa("T2").intento(2).insertar();      // 2 < 3
        bat.programa("T2").intento(3).insertar();                // 3 == 3
        bat.programa("T2").intento(4).insertar();                // 4 > 3
        bat.programa("T1").intento(1).insertar();                // 1 == 1
        bat.programa("T0").intento(0).insertar();                // MaximoIntentos=0: nunca
        assertThat(ids("", "T1", "T2", "T0")).containsExactly(ok);
    }

    @Test
    void soloElNodoConfigurado() {
        long vacio = bat.programa("T1").nodo("").insertar();
        long a = bat.programa("T1").nodo("A").insertar();
        assertThat(ids("", "T1")).containsExactly(vacio);
        assertThat(ids("A", "T1")).containsExactly(a);
        assertThat(ids("Z", "T1")).isEmpty();
    }

    @Test
    void ventanaDeFecha_desdeLasCeroUtcHastaAhora() {
        Instant inicioDia = Instant.parse("2026-10-21T00:00:00Z");
        long enInicio = bat.programa("T1").ejecucion(inicioDia).insertar();
        long justoAntes = bat.programa("T1").ejecucion(inicioDia.minusSeconds(1)).insertar();
        long ayer = bat.programa("T1").ejecucion(inicioDia.minusSeconds(86_400)).insertar();
        long enAhora = bat.programa("T1").ejecucion(AHORA).insertar();
        long futuro = bat.programa("T1").ejecucion(AHORA.plusSeconds(1)).insertar();
        assertThat(ids("", "T1")).containsExactlyInAnyOrder(enInicio, enAhora)
                .doesNotContain(justoAntes, ayer, futuro);
    }

    @Test
    void filtroDeTipo_soloLosHabilitados() {
        long a = bat.programa("T1").insertar();
        long b = bat.programa("T2").insertar();
        assertThat(ids("", "T1")).containsExactly(a);
        assertThat(ids("", "T2")).containsExactly(b);
        assertThat(ids("", "T1", "T2")).containsExactlyInAnyOrder(a, b);
        assertThat(ids("", "NO_EXISTE")).isEmpty();
    }

    @Test
    void agendaDemonioSeMarca() {
        bat.agenda(7, "T1", "S");
        bat.agenda(8, "T1", "N");
        long d = bat.programa("T1").agenda(7).insertar();
        long n = bat.programa("T1").agenda(8).insertar();
        long sin = bat.programa("T1").agenda(0).insertar();
        List<Programa> r = bat.adapter.readEligiblePrograms("", List.of("T1"), AHORA);
        assertThat(r).filteredOn(Programa::isDemonio).extracting(Programa::getIdPrograma).containsExactly(d);
        assertThat(r).filteredOn(p -> !p.isDemonio()).extracting(Programa::getIdPrograma).containsExactlyInAnyOrder(n, sin);
    }

    @Test
    void fifoPorFechaDeCreacion() {
        long tarde = bat.programa("T1").creacion(AHORA.minusSeconds(100)).insertar();
        long temprano = bat.programa("T1").creacion(AHORA.minusSeconds(900)).insertar();
        assertThat(ids("", "T1")).containsExactly(temprano, tarde);
    }

    @Test
    void esSoloLectura_laFilaNoCambia() {
        long id = bat.programa("T1").insertar();
        List<Object> antes = bat.filaCompleta(id);
        ids("", "T1");
        ids("", "T1");
        assertThat(bat.filaCompleta(id)).isEqualTo(antes);
        assertThat(antes).startsWith(0, "PEN").endsWith("N");  // Intento 0, PEN, ..., Bloqueo N
    }

    @Test
    void sinTiposNoHaceNingunaConsulta() {
        NamedParameterJdbcTemplate jdbc = Mockito.mock(NamedParameterJdbcTemplate.class);
        assertThat(new ProgramaJdbcAdapter(jdbc).readEligiblePrograms("", List.of(), AHORA)).isEmpty();
        Mockito.verifyNoInteractions(jdbc);
    }
}
