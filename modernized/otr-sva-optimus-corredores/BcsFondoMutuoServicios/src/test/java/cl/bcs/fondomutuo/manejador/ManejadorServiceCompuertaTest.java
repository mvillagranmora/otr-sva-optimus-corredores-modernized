package cl.bcs.fondomutuo.manejador;

import static cl.bcs.fondomutuo.manejador.Banco.TODAS;
import static cl.bcs.fondomutuo.manejador.Banco.entrada;
import static cl.bcs.fondomutuo.manejador.Banco.error;
import static cl.bcs.fondomutuo.manejador.Banco.json;
import static cl.bcs.fondomutuo.manejador.Banco.respuesta;
import static cl.bcs.fondomutuo.manejador.Banco.salidaNormal;
import static cl.bcs.fondomutuo.manejador.Banco.sol;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.manejador.ManejadorContrato.Entrada;
import cl.bcs.fondomutuo.manejador.ManejadorContrato.Salida;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;

/** FM-MAN-07..12: pairs (D-MAN-7), fields that are not propagated (D-MAN-6), exception (D-MAN-4), gate G0 (FM-MAN-11), empty request (FM-MAN-12). */
class ManejadorServiceCompuertaTest {

    @Test
    void fmMan07_cambioSerieYRescateReinversion_exigenAporteYRescateBuenos_DMAN7() throws Exception {
        for (Lista tipo : new Lista[] {Lista.CAMBIO_SERIE, Lista.RESCATE_REINVERSION}) {
            Banco solo = new Banco();
            Entrada e = entrada();
            if (tipo == Lista.CAMBIO_SERIE) e.p_SolicitudCambioSerieRescate = List.of(json(1)); else e.p_SolicitudRescateReinversionRescate = List.of(json(1));
            solo.destinos.put(tipo, (sim, p) -> respuesta(0, List.of(1L)));
            Salida s = solo.servicio(TODAS).logica(e);
            assertThat(solo.log).as(tipo + " with ONE list: simulation only, no real pass").hasSize(1);
            assertThat(s.P_OUT_RESULTADO).isEmpty();

            Banco soloAporte = new Banco();
            Entrada ea = entrada();
            if (tipo == Lista.CAMBIO_SERIE) ea.p_SolicitudCambioSerieAporte = List.of(json(2)); else ea.p_SolicitudRescateReinversionAporte = List.of(json(2));
            soloAporte.destinos.put(tipo, (sim, p) -> respuesta(0, List.of(2L)));
            assertThat(soloAporte.servicio(TODAS).logica(ea).P_OUT_RESULTADO).as(tipo + " with only the contribution list: simulation only").isEmpty();
            assertThat(soloAporte.log).hasSize(1);

            Banco parMalo = new Banco();
            Entrada e2 = entrada();
            if (tipo == Lista.CAMBIO_SERIE) {
                e2.p_SolicitudCambioSerieRescate = List.of(json(1));
                e2.p_SolicitudCambioSerieAporte = List.of(json(2));
            } else {
                e2.p_SolicitudRescateReinversionRescate = List.of(json(1));
                e2.p_SolicitudRescateReinversionAporte = List.of(json(2));
            }
            parMalo.destinos.put(tipo, (sim, p) -> respuesta(1, List.of(2L), error(1, "RESCATE_RECHAZADO")));
            Salida s2 = parMalo.servicio(TODAS).logica(e2);
            assertThat(parMalo.log).as(tipo + " good contribution but bad rescue: nothing runs").hasSize(1);
            assertThat(s2.P_OUT_MENSAJES).hasSize(1);
            assertThat(s2.P_OUT_RESULTADO).isEmpty();
        }
    }

    @Test
    void fmMan08_camposQueNoSePropagan_simularDeEntradaSeIgnora_canalNuloPisaElOpt_DMAN6() throws Exception {
        Banco b = new Banco();
        Entrada e = entrada();
        e.p_SimularOperacion = "S";
        e.p_IdTipoCanalIngreso = null;
        e.p_SolicitudesAporteRescateNormal = List.of(sol(1));
        b.servicio(TODAS).logica(e);
        assertThat(b.entradasNormal.get(0).p_SimularOperacion).as("the same object: the last pass left N").isEqualTo("N");
        assertThat(b.log).containsExactly("NORMAL:S[1]", "NORMAL:N[1]");
        assertThat(b.entradasNormal.get(0).p_IdTipoCanalIngreso).isNull();
        assertThat(b.entradasNormal.get(0).p_IdOrigenProceso).isEqualTo("OPT");
        assertThat(b.entradasNormal.get(0).p_NumeroDividendo).isZero();
        assertThat(b.entradasNormal.get(0).p_PermiteOperarProspectos).isEqualTo("N");
    }

    @Test
    void fmMan09_excepcionDeUnaPasada_seRelanzaTalCual_DMAN4() {
        Banco b = new Banco();
        RuntimeException boom = new IllegalStateException("boom");
        b.normal = (sim, in) -> {
            if (!sim) throw boom;
            return salidaNormal(0, List.of(1L));
        };
        Entrada e = entrada();
        e.p_SolicitudesAporteRescateNormal = List.of(sol(1));
        e.p_SolicitudAporteOvernight = List.of(json(2));
        assertThatThrownBy(() -> b.servicio(TODAS).logica(e)).isSameAs(boom);
        assertThat(b.log).as("nothing after the failing pass").containsExactly("NORMAL:S[1]", "NORMAL:N[1]");
    }

    @Test
    void fmMan11_g0_laListaNoServidaDaNoServido_antesDeLlamarANadie_tambienEnPeticionMixta() {
        List<java.util.function.Consumer<Entrada>> otras = List.of(e -> e.p_SolicitudesAporteRescateProgramado = List.of(json(1)),
                e -> e.p_SolicitudAporteOvernight = List.of(json(1)), e -> e.p_SolicitudAporteReinversion = List.of(json(1)),
                e -> e.p_SolicitudCambioSerieRescate = List.of(json(1)), e -> e.p_SolicitudCambioSerieAporte = List.of(json(1)),
                e -> e.p_SolicitudRescateReinversionRescate = List.of(json(1)), e -> e.p_SolicitudRescateReinversionAporte = List.of(json(1)),
                e -> e.p_SolicitudRescateTransferencia = List.of(json(1)), e -> e.p_SolicitudRescateTraspaso = List.of(json(1)));
        for (java.util.function.Consumer<Entrada> otra : otras) {
            Banco b = new Banco();
            Entrada e = entrada();
            otra.accept(e);
            assertThatThrownBy(() -> b.servicio(Set.of("NORMAL")).logica(e)).isInstanceOf(ServicioNoServidoException.class);
            Entrada mixta = entrada();
            mixta.p_SolicitudesAporteRescateNormal = List.of(sol(1));
            otra.accept(mixta);
            assertThatThrownBy(() -> b.servicio(Set.of("NORMAL")).logica(mixta)).isInstanceOf(ServicioNoServidoException.class)
                    .extracting(x -> ((ServicioNoServidoException) x).compuerta()).isEqualTo("G0");
            assertThat(b.log).as("503 before the first call: Normal never ran (no write, no sequence)").isEmpty();
        }
    }

    @Test
    void fmMan11b_g0_soloNormalSeSirve_yUnaListaNullNoEsDeLaCompuerta() throws Exception {
        Banco b = new Banco();
        Entrada e = entrada();
        e.p_SolicitudesAporteRescateNormal = List.of(sol(1));
        e.p_SolicitudRescateTraspaso = List.of();
        assertThat(b.servicio(Set.of("NORMAL")).logica(e).P_OUT_RESULTADO).hasSize(1);
        assertThat(b.log).containsExactly("NORMAL:S[1]", "NORMAL:N[1]");
        Banco n = new Banco();
        Entrada conNull = entrada();
        conNull.p_SolicitudRescateTraspaso = null;
        assertThatThrownBy(() -> n.servicio(Set.of("NORMAL")).logica(conNull)).as("the legacy NPE at its turn, not a 503").isInstanceOf(NullPointerException.class);
        assertThat(new Banco().servicio(Set.of()).logica(entrada()).P_OUT_ERRORESCONTADOR).as("empty request needs no list").isZero();
        Banco abierta = new Banco();
        Entrada otra = entrada();
        otra.p_SolicitudRescateTraspaso = List.of(json(9));
        abierta.destinos.put(Lista.TRASPASO, (sim, p) -> respuesta(0, List.of(9L)));
        assertThat(abierta.servicio(TODAS).logica(otra).P_OUT_RESULTADO).as("F2: the list in the property is served").hasSize(1);
    }

    @Test
    void fmMan11c_laConfiguracionDeListasServidas_falla_alArrancar_siNombraUnaListaSinHandlerOUnNombreDesconocido() {
        java.util.Map<Lista, TipoSolicitudPort> f1 = new java.util.EnumMap<>(Lista.class);
        for (Lista l : Lista.values()) if (l != Lista.NORMAL) f1.put(l, new TipoNoServido(l.name()));
        ManejadorConfiguracion.validarListasServidas(Set.of("NORMAL"), f1);
        ManejadorConfiguracion.validarListasServidas(Set.of(), f1);
        assertThatThrownBy(() -> ManejadorConfiguracion.validarListasServidas(Set.of("NORMAL", "PROGRAMADO"), f1)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no handler");
        assertThatThrownBy(() -> ManejadorConfiguracion.validarListasServidas(Set.of("NORMALL"), f1)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unknown list");
        f1.put(Lista.PROGRAMADO, p -> respuesta(0, List.of()));   // F2: a real handler replaces TipoNoServido
        ManejadorConfiguracion.validarListasServidas(Set.of("NORMAL", "PROGRAMADO"), f1);
        assertThatThrownBy(() -> new TipoNoServido("OVERNIGHT").logica(null)).isInstanceOf(ServicioNoServidoException.class);
    }

    @Test
    void fmMan12_sinListasConFilas_ningunIfEntra_contadorCeroSinMensajes() throws Exception {
        Banco b = new Banco();
        Salida s = b.servicio(Set.of("NORMAL")).logica(entrada());
        assertThat(b.log).isEmpty();
        assertThat(s.P_OUT_ERRORESCONTADOR).isZero();
        assertThat(s.P_OUT_MENSAJES).isEmpty();
        assertThat(s.P_OUT_RESULTADO).isEmpty();
    }
}
