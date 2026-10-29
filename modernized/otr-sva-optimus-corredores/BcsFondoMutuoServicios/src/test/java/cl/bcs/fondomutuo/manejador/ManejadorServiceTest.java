package cl.bcs.fondomutuo.manejador;

import static cl.bcs.fondomutuo.manejador.Banco.TODAS;
import static cl.bcs.fondomutuo.manejador.Banco.entrada;
import static cl.bcs.fondomutuo.manejador.Banco.error;
import static cl.bcs.fondomutuo.manejador.Banco.fp;
import static cl.bcs.fondomutuo.manejador.Banco.json;
import static cl.bcs.fondomutuo.manejador.Banco.respuesta;
import static cl.bcs.fondomutuo.manejador.Banco.salidaNormal;
import static cl.bcs.fondomutuo.manejador.Banco.sol;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.fondomutuo.manejador.ManejadorContrato.Entrada;
import cl.bcs.fondomutuo.manejador.ManejadorContrato.Salida;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;

/** FM-MAN-01..06 (dispatch, simulate-then-execute, filter by IdInterno, Spot filter, message/counter mix) over a scripted Normal and destinations. */
class ManejadorServiceTest {

    @Test
    void fmMan01_ordenFijoDeDespacho_cadaTipoTerminaAntesDelSiguiente() throws Exception {
        Banco b = new Banco();
        Entrada e = entrada();
        e.p_SolicitudRescateTraspaso = List.of(json(19));
        e.p_SolicitudRescateTransferencia = List.of(json(18));
        e.p_SolicitudRescateReinversionAporte = List.of(json(17));
        e.p_SolicitudRescateReinversionRescate = List.of(json(16));
        e.p_SolicitudCambioSerieAporte = List.of(json(15));
        e.p_SolicitudCambioSerieRescate = List.of(json(14));
        e.p_SolicitudAporteReinversion = List.of(json(13));
        e.p_SolicitudAporteOvernight = List.of(json(12));
        e.p_SolicitudesAporteRescateProgramado = List.of(json(11));
        e.p_SolicitudesAporteRescateNormal = List.of(sol(1));
        b.destinos.put(Lista.PROGRAMADO, (sim, p) -> respuesta(0, List.of(11L)));
        b.destinos.put(Lista.OVERNIGHT, (sim, p) -> respuesta(0, List.of(12L)));
        b.destinos.put(Lista.REINVERSION_APORTE, (sim, p) -> respuesta(0, List.of(13L)));
        b.destinos.put(Lista.CAMBIO_SERIE, (sim, p) -> respuesta(0, List.of(14L, 15L)));
        b.destinos.put(Lista.RESCATE_REINVERSION, (sim, p) -> respuesta(0, List.of(16L, 17L)));
        b.destinos.put(Lista.TRANSFERENCIA, (sim, p) -> respuesta(0, List.of(18L)));
        b.destinos.put(Lista.TRASPASO, (sim, p) -> respuesta(0, List.of(19L)));
        Salida s = b.servicio(TODAS).logica(e);
        assertThat(b.log).containsExactly("NORMAL:S[1]", "NORMAL:N[1]", "PROGRAMADO:S:A[11]:Rnull", "PROGRAMADO:N:A[11]:Rnull",
                "OVERNIGHT:S:A[12]:Rnull", "OVERNIGHT:N:A[12]:Rnull", "REINVERSION_APORTE:S:A[13]:Rnull", "REINVERSION_APORTE:N:A[13]:Rnull",
                "CAMBIO_SERIE:S:A[15]:R[14]", "CAMBIO_SERIE:N:A[15]:R[14]", "RESCATE_REINVERSION:S:A[17]:R[16]", "RESCATE_REINVERSION:N:A[17]:R[16]",
                "TRANSFERENCIA:S:Anull:R[18]", "TRANSFERENCIA:N:Anull:R[18]", "TRASPASO:S:Anull:R[19]", "TRASPASO:N:Anull:R[19]");
        assertThat(s.P_OUT_RESULTADO).extracting(r -> r.IdInterno).containsExactly(1L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L);
    }

    @Test
    void fmMan02_simulaPrimero_filtraPorIdInterno_ejecutaConLasBuenas_yUnSoloObjetoDeEntradaEnAmbasPasadas() throws Exception {
        Banco b = new Banco();
        b.normal = (sim, in) -> sim ? salidaNormal(1, List.of(1L, 3L), error(2, "X")) : salidaNormal(0, List.of(1L, 3L));
        Entrada e = entrada();
        e.p_SolicitudesAporteRescateNormal = List.of(sol(1), sol(2), sol(3));
        Salida s = b.servicio(TODAS).logica(e);
        assertThat(b.log).containsExactly("NORMAL:S[1,2,3]", "NORMAL:N[1,3]");
        assertThat(b.entradasNormal.get(0)).isSameAs(b.entradasNormal.get(1));
        assertThat(s.P_OUT_ERRORESCONTADOR).isZero();
    }

    @Test
    void fmMan03_idInternoRepetidoEnLosResultadosDeLaSimulacion_lanzaKeyFieldIsNotUnique() {
        Banco b = new Banco();
        b.normal = (sim, in) -> salidaNormal(0, List.of(1L, 1L));
        Entrada e = entrada();
        e.p_SolicitudesAporteRescateNormal = List.of(sol(1));
        assertThatThrownBy(() -> b.servicio(TODAS).logica(e)).isExactlyInstanceOf(Exception.class).hasMessage("key field is not unique");
        assertThat(b.log).containsExactly("NORMAL:S[1]");
    }

    @Test
    void fmMan04_formaPagoSpotSeFiltraPorIdOperacion_yAusenteEsNpeDespuesDeLaSimulacion_DMAN2() throws Exception {
        Banco b = new Banco();
        b.normal = (sim, in) -> salidaNormal(0, List.of(1L));
        Entrada e = entrada();
        e.p_SolicitudesAporteRescateNormal = List.of(sol(1), sol(2));
        e.p_FormaPagoSpot = List.of(fp(1), fp(2), fp(1));
        b.servicio(TODAS).logica(e);
        assertThat(b.entradasNormal.get(1).p_FormaPagoSpot).extracting(f -> f.IdOperacion).containsExactly(1L, 1L);
        Banco c = new Banco();
        Entrada sin = entrada();
        sin.p_FormaPagoSpot = null;
        sin.p_SolicitudesAporteRescateNormal = List.of(sol(1));
        assertThatThrownBy(() -> c.servicio(TODAS).logica(sin)).isInstanceOf(NullPointerException.class);
        assertThat(c.log).as("the simulation pass already ran (and consumed sequences in the real Normal)").containsExactly("NORMAL:S[1]");
    }

    @Test
    void fmMan05_losMensajesSonDeLaSimulacion_elContadorYLosResultadosSonDeLaPasadaReal_DMAN5() throws Exception {
        Banco b = new Banco();
        b.normal = (sim, in) -> sim ? salidaNormal(5, List.of(1L), error(1, "SOLO_SIM")) : salidaNormal(2, List.of(1L), error(9, "SOLO_REAL"));
        Entrada e = entrada();
        e.p_SolicitudesAporteRescateNormal = List.of(sol(1));
        b.destinos.put(Lista.OVERNIGHT, (sim, p) -> sim ? respuesta(7, List.of(5L), error(2, "OV_SIM")) : respuesta(3, List.of(5L), error(8, "OV_REAL")));
        e.p_SolicitudAporteOvernight = List.of(json(5));
        Salida s = b.servicio(TODAS).logica(e);
        assertThat(s.P_OUT_MENSAJES).extracting(m -> m.codigo).containsExactly("SOLO_SIM", "OV_SIM");
        assertThat(s.P_OUT_ERRORESCONTADOR).as("2 (real Normal) + 3 (real Overnight)").isEqualTo(5L);
        assertThat(s.P_OUT_RESULTADO).hasSize(2);
    }

    @Test
    void fmMan06_laSimulacionRechazaTodo_sinPasadaReal_contadorCero_DMAN1() throws Exception {
        Banco b = new Banco();
        b.normal = (sim, in) -> salidaNormal(2, List.of(), error(1, "RECHAZO_A"), error(2, "RECHAZO_B"));
        Entrada e = entrada();
        e.p_SolicitudesAporteRescateNormal = List.of(sol(1), sol(2));
        Salida s = b.servicio(TODAS).logica(e);
        assertThat(b.log).containsExactly("NORMAL:S[1,2]");
        assertThat(s.P_OUT_ERRORESCONTADOR).as("the host would COMMIT: errors only as messages").isZero();
        assertThat(s.P_OUT_MENSAJES).hasSize(2);
        assertThat(s.P_OUT_RESULTADO).isEmpty();
    }
}
