package cl.bcs.fondomutuo.consultas;

import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioConfiguracion;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaCerrada;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.manejador.ManejadorConfiguracion;

/**
 * Start-up checks of the eight UI reads (architecture review F3 W1): (M1) CPF named in {@code fondomutuo.solicitud.listas-servidas} while its custody adapter is the
 * fail-closed {@link ConsultarCustodiaPortafolioCustodiaCerrada} stops the start-up (every call would be a 503: the router would pay a round trip for nothing; analogous to the
 * RESCATE_REINVERSION + closed custody rule); (M5) INFO log with the reads that are open (names only); (M2) WARN, never a failure, when the JVM zone is not UTC and some read or write is open
 * (the legacy date arithmetic and the dates of the reads assume the zone of the container, UTC).
 */
@Component
public class ListasLecturaArranque {

    private static final Logger log = LoggerFactory.getLogger(ListasLecturaArranque.class);

    public ListasLecturaArranque(Compuertas compuertas, ObjectProvider<ConsultarCustodiaPortafolioCustodiaPort> custodia) {
        iniciar(compuertas.listasServidas(), custodia.getIfAvailable(), TimeZone.getDefault());
    }

    /** The read lists effectively open, sorted (names only). */
    public static Set<String> abiertas(Set<String> configuradas) {
        Set<String> r = new TreeSet<>(configuradas);
        r.retainAll(ManejadorConfiguracion.LISTAS_LECTURA);
        return r;
    }

    /** @throws IllegalStateException CPF configured while its custody adapter is the closed one */
    static void iniciar(Set<String> configuradas, ConsultarCustodiaPortafolioCustodiaPort custodia, TimeZone zona) {
        if (configuradas.contains(ConsultarCustodiaPortafolioConfiguracion.LISTA) && custodia instanceof ConsultarCustodiaPortafolioCustodiaCerrada) {
            throw new IllegalStateException("fondomutuo.solicitud.listas-servidas names " + ConsultarCustodiaPortafolioConfiguracion.LISTA
                    + " but the custody calculation is closed (no custody library adapter in this module): every call would answer 503 at gate G-CUS");
        }
        Set<String> abiertas = abiertas(configuradas);
        log.info("UI reads open (fondomutuo.solicitud.listas-servidas): {}", abiertas);
        Set<String> escritura = new TreeSet<>(configuradas);
        escritura.retainAll(ManejadorConfiguracion.LISTAS_ESCRITURA_UI);
        for (String nombre : escritura) {
            log.info("UI WRITE open: {}", nombre);   // M-4 (architecture review F3 W2): the only UI class that writes (CARGAR_VALOR_CUOTA)
        }
        if ((!abiertas.isEmpty() || !escritura.isEmpty()) && (zona.getRawOffset() != 0 || zona.useDaylightTime())) {
            log.warn("JVM time zone is {} and not UTC while UI reads or writes are open: the legacy date arithmetic assumes UTC (the container zone); see RUNBOOK_LISTAS.md", zona.getID());
        }
    }
}
