package cl.bcs.fondomutuo.manejador;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioConfiguracion;
import cl.bcs.fondomutuo.consultas.detalle.ConsultarDetalleConfiguracion;
import cl.bcs.fondomutuo.consultas.dolar.ConsultarDolarConfiguracion;
import cl.bcs.fondomutuo.consultas.fecha.ConsultarFechaConfiguracion;
import cl.bcs.fondomutuo.consultas.instrumentos.ConsultarInstrumentosConfiguracion;
import cl.bcs.fondomutuo.consultas.reinversion.ConsultarReinversionConfiguracion;
import cl.bcs.fondomutuo.consultas.solicitudes.ConsultarSolicitudesConfiguracion;
import cl.bcs.fondomutuo.consultas.solicitudes.ConsultarSolicitudesWsConfiguracion;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;
import cl.bcs.fondomutuo.normal.NormalPort;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionDestino;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionPort;
import cl.bcs.fondomutuo.valorcuota.CargarValorCuotaConfiguracion;

/**
 * Wiring of the Manejador. Normal comes from step 4A ({@link NormalPort}); until its bean exists the facade answers 503 for the Normal list (the
 * context still starts: the allow-list already names the Manejador). The seven other destinations are {@link TipoNoServido}: F2 replaces one entry per
 * wave and adds the list to {@code fondomutuo.solicitud.listas-servidas}.
 */
@Configuration
public class ManejadorConfiguracion {

    /**
     * Architecture review F3 W1 (HIGH): the names of the eight UI reads (F3 W1). They are NOT lists of the Manejador: each opens its own JavaCall entry and
     * {@code Lista.valueOf} would reject them, making the reads impossible to open by configuration. Any other unknown name still stops the start-up.
     */
    public static final Set<String> LISTAS_LECTURA = Set.of(ConsultarFechaConfiguracion.LISTA, ConsultarDolarConfiguracion.LISTA,
            ConsultarReinversionConfiguracion.LISTA, ConsultarInstrumentosConfiguracion.LISTA, ConsultarSolicitudesConfiguracion.LISTA,
            ConsultarSolicitudesWsConfiguracion.LISTA, ConsultarDetalleConfiguracion.LISTA, ConsultarCustodiaPortafolioConfiguracion.LISTA);

    /**
     * F3 W2: the name of the one UI class that WRITES. Like the reads it is NOT a list of the Manejador (its own handler and gate), but it is deliberately NOT in {@link #LISTAS_LECTURA}
     * (it writes and is not a read-only unit of work). It is accepted by the validator, also next to NORMAL and RESCATE_REINVERSION (custody closed): it is another request, not another list of the Manejador.
     */
    public static final Set<String> LISTAS_ESCRITURA_UI = Set.of(CargarValorCuotaConfiguracion.LISTA);

    @Bean
    public ManejadorSolicitudAporteRescateService manejadorSolicitudAporteRescateService(ObjectProvider<NormalPort> normal,
            ObjectProvider<RescateReinversionPort> rescateReinversion, Compuertas compuertas) {
        NormalPort puerto = normal.getIfAvailable(() -> in -> {
            throw new ServicioNoServidoException("G0", "CrearSolicitudAporteRescateNormal is not available in this module");
        });
        Map<Lista, TipoSolicitudPort> destinos = new EnumMap<>(Lista.class);
        for (Lista l : Lista.values()) {
            if (l != Lista.NORMAL) destinos.put(l, new TipoNoServido(l.name()));
        }
        // F2 W1 front 2: RescateReinversion has a handler; its list is still served ONLY if the property names it (G0 runs first, closed by default)
        RescateReinversionPort rre = rescateReinversion.getIfAvailable();
        if (rre != null) destinos.put(Lista.RESCATE_REINVERSION, new RescateReinversionDestino(rre));
        validarListasServidas(compuertas.listasServidas(), destinos, compuertas.custodiaHabilitada());
        return new ManejadorSolicitudAporteRescateService(puerto, destinos, compuertas::g0ListasConFilas);
    }

    /**
     * Fail closed at start-up: a list named in {@code fondomutuo.solicitud.listas-servidas} must be one of the eight of the Manejador (or one of the {@link #LISTAS_LECTURA} of the UI reads) and must have a real handler (NORMAL
     * always has; the others only when F2 replaces their {@link TipoNoServido}). Otherwise a request could write with Normal and fail later with a 503.
     */
    static void validarListasServidas(Set<String> configuradas, Map<Lista, TipoSolicitudPort> destinos, boolean custodiaHabilitada) {
        validarListasServidas(configuradas, destinos);
        // Architecture review (2026-10-26, HIGH): G0 checks list names only. RescateReinversion always reaches the custody check (G-CUS), even when simulating, so
        // with G-CUS closed a request carrying it AND another served list would commit the other list first and answer 500 at G-CUS: a client retry would duplicate it.
        if (configuradas.contains("RESCATE_REINVERSION") && !custodiaHabilitada && configuradas.stream().anyMatch(n -> !n.equals("RESCATE_REINVERSION") && !LISTAS_LECTURA.contains(n) && !LISTAS_ESCRITURA_UI.contains(n))) {
            throw new IllegalStateException("fondomutuo.solicitud.listas-servidas names RESCATE_REINVERSION together with other lists while "
                    + "fondomutuo.custodia.habilitada is false: a mixed request would write the other list and then fail at the custody gate");
        }
    }

    static void validarListasServidas(Set<String> configuradas, Map<Lista, TipoSolicitudPort> destinos) {
        for (String nombre : configuradas) {
            if (LISTAS_LECTURA.contains(nombre) || LISTAS_ESCRITURA_UI.contains(nombre)) continue;   // a read of the UI, not a list of the Manejador (it has its own handler and gate)
            Lista lista;
            try {
                lista = Lista.valueOf(nombre);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("fondomutuo.solicitud.listas-servidas names an unknown list: " + nombre, e);
            }
            if (lista != Lista.NORMAL && destinos.get(lista) instanceof TipoNoServido) {
                throw new IllegalStateException("fondomutuo.solicitud.listas-servidas names " + nombre + " but this module has no handler for it yet");
            }
        }
    }
}
