package cl.bcs.fondomutuo.fechas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioDiaSiguiente;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;

/**
 * Architecture review F3 W1 (M2): a calendar whose seven weekdays are all non-working used to hang the day-skipping loop for ever; both copies (the shared port and the CPF one,
 * duplicated by D-F3A-29) now stop with an {@link IllegalStateException}. A calendar with working days is untouched.
 */
class DiaSiguienteTopeTest {

    private static CalendarioPort calendario(int diasInhabiles) {
        return new CalendarioPort() {
            @Override
            public List<Map<String, Object>> feriados(List<Long> idPaises) {
                List<Map<String, Object>> filas = new ArrayList<>();
                for (int d = 0; d < diasInhabiles; d++) {
                    Map<String, Object> f = new HashMap<>();
                    f.put("Tipo", "INHABIL");
                    f.put("Dia", d);
                    f.put("IdPais", 1L);
                    filas.add(f);
                }
                return filas;
            }

            @Override
            public List<Map<String, Object>> monedaFormato() {
                return List.of();
            }

            @Override
            public java.sql.Timestamp fechaHoraSistemaLocal(Long idEmpresa) {
                return null;
            }
        };
    }

    private static InObtenerDiaSiguiente entrada(String diaHabil) {
        InObtenerDiaSiguiente e = new InObtenerDiaSiguiente();
        e.p_idPais = 1L;
        e.p_diaHabil = diaHabil;
        e.p_fechas.add(new FechasIn("2026-03-02", 1L, 1L));
        return e;
    }

    @Test
    void compartida_conLosSieteDiasInhabiles_fallaEnVezDeColgarse() {
        assertThatThrownBy(() -> new ObtenerDiaSiguiente(calendario(7)).logica(entrada("S"))).isInstanceOf(IllegalStateException.class).hasMessageContaining("3700");
    }

    @Test
    void compartida_modoN_conSoloCaeDiaHabil_tambienFalla() {
        InObtenerDiaSiguiente e = entrada("N");
        e.p_soloCaeDiaHabil = "S";
        assertThatThrownBy(() -> new ObtenerDiaSiguiente(calendario(7)).logica(e)).isInstanceOf(IllegalStateException.class);
        InObtenerDiaSiguiente anterior = entrada("N");
        anterior.p_soloCaeDiaHabil = "S";
        anterior.p_consideraHabilAnterior = "S";
        assertThatThrownBy(() -> new ObtenerDiaSiguiente(calendario(7)).logica(anterior)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cpf_conLosSieteDiasInhabiles_fallaEnVezDeColgarse() {
        assertThatThrownBy(() -> new ConsultarCustodiaPortafolioDiaSiguiente(calendario(7)).logica(entrada("S"))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3700");
    }

    @Test
    void conDiasHabiles_nadaCambia_sabadoYDomingoInhabiles() throws Exception {
        // optimus days: Monday = 0 ... Saturday = 5, Sunday = 6. 2026-03-06 is a Friday: the next working day is Monday 2026-03-09 (3 real days)
        InObtenerDiaSiguiente a = entrada("S");
        a.p_fechas.get(0).fechaInicial = "2026-03-06";
        OutObtenerDiaSiguiente compartida = new ObtenerDiaSiguiente(calendarioFinDeSemana()).logica(a);
        InObtenerDiaSiguiente b = entrada("S");
        b.p_fechas.get(0).fechaInicial = "2026-03-06";
        OutObtenerDiaSiguiente cpf = new ConsultarCustodiaPortafolioDiaSiguiente(calendarioFinDeSemana()).logica(b);
        for (OutObtenerDiaSiguiente o : List.of(compartida, cpf)) {
            assertThat(o.p_out_fechas.get(0).fechaCalculada).isEqualTo("2026-03-09");
            assertThat(o.p_out_fechas.get(0).numeroDias).isEqualTo(3L);
        }
    }

    private static CalendarioPort calendarioFinDeSemana() {
        CalendarioPort base = calendario(0);
        return new CalendarioPort() {
            @Override
            public List<Map<String, Object>> feriados(List<Long> idPaises) {
                List<Map<String, Object>> filas = new ArrayList<>();
                for (int d : new int[] {5, 6}) {
                    Map<String, Object> f = new HashMap<>();
                    f.put("Tipo", "INHABIL");
                    f.put("Dia", d);
                    f.put("IdPais", 1L);
                    filas.add(f);
                }
                return filas;
            }

            @Override
            public List<Map<String, Object>> monedaFormato() {
                return base.monedaFormato();
            }

            @Override
            public java.sql.Timestamp fechaHoraSistemaLocal(Long idEmpresa) {
                return null;
            }
        };
    }
}
