package cl.bcs.fondomutuo.custodia;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;

/**
 * The MONTO branch of legacy {@code MonedaFormato.formatear(con, valor, MONTO, idMoneda)} (the 3-argument form: use "GEN", instrument type and operation
 * type ""). Step 4A merged the duplicated logic into the shared {@link FormatoMoneda} (3A): this class keeps its signature for the 3B caller (the rows were
 * read by the caller in the call, DEV-F2) and delegates; the exceptions of the legacy (no currency, no format, bad rounding letter) are the same.
 */
public final class FormatoMonto {

    private FormatoMonto() {
    }

    public static BigDecimal formatear(List<Map<String, Object>> formatos, BigDecimal valor, String idMoneda) throws Exception {
        return new FormatoMoneda(new CalendarioPort() {
            @Override
            public List<Map<String, Object>> feriados(List<Long> idPaises) {
                throw new UnsupportedOperationException("only the currency formats are needed");
            }

            @Override
            public List<Map<String, Object>> monedaFormato() {
                return formatos;
            }

            @Override
            public Timestamp fechaHoraSistemaLocal(Long idEmpresa) {
                throw new UnsupportedOperationException("only the currency formats are needed");
            }
        }).formatear(valor, FormatoMoneda.MONTO, idMoneda);
    }
}
