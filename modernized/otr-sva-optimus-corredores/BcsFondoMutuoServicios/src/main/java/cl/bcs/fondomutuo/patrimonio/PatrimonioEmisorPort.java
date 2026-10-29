package cl.bcs.fondomutuo.patrimonio;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reads of legacy {@code ValidaPatrimonioEmisor} (verbatim SQL {@code ValidaPatrimonioEmisor_*.sql}). The custody read needs the step table
 * {@code BSCL.Orden.BD::GBL.ValidaPatrimonioEmisor_Proceso_Paso}, a GLOBAL TEMPORARY table (confirmed per session in HANA): its rows live in the
 * session of the call, so the adapter fills and reads it on ONE connection of the call (one connection per call, the per-call transaction). The rows
 * are not removed afterwards (legacy: the next call starts with a DELETE).
 */
public interface PatrimonioEmisorPort {

    /** Row of {@code ValidaPatrimonioEmisor_SelectSerieFondo.sql}. */
    class SerieFondo {
        public String IdSerieFondo;
        public String IdEmisor;
        public BigDecimal MontoPatrimonioEmisor;
        public String IdMoneda;
    }

    /** Row of {@code ValidaPatrimonioEmisor_SelectCustodia.sql}. */
    class Custodia {
        public String Id;
        public String IdClienteEmisor;
        public String IdMonedaInstrumento;
        public Long IdCliente;
        public String IdEmisor;
        public BigDecimal Monto;
        public BigDecimal Cantidad;
    }

    List<SerieFondo> seriesFondo(List<String> idsSerie);

    /** DELETE the step table, INSERT one row per client (legacy batch), then the custody SELECT valued up to {@code fecha}; one connection. */
    List<Custodia> custodia(List<Long> idsCliente, String fecha);
}
