package cl.bcs.fondomutuo.custodia;

import java.util.List;

/** Reads of legacy {@code ConsultarFlujoCustodiaFondoMutuo} (verbatim SQL {@code ConsultarFlujoCustodiaFondoMutuo_*.sql}). */
public interface FlujoCustodiaFmDatos {

    /** Row of {@code ..._SelectSerieFondo.sql}. */
    class SerieFondo {
        public Long IdInstrumento;
        public String IdSerieFondo;
        public String IdMoneda;
    }

    /** Row of {@code ..._SelectRescateTotalTransito.sql}. */
    class RescateTotal {
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdInstrumento;
        public String IdRegimenTributario;
    }

    List<SerieFondo> series(List<Long> idsInstrumento);

    List<RescateTotal> rescatesTotalesEnTransito(List<Long> idsInstrumento, List<Long> idsCliente, List<Long> idsPortafolio);
}
