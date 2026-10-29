package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

/** Query of the payment preferences of the portfolios of a client (legacy ConsultarPreferenciaPagoPortafolio SQL). */
public interface PreferenciaPagoRepositorio {

    /** Row of the query. */
    record PreferenciaFila(Long IdPreferenciaPagoPortafolio, Long IdPortafolio) {
    }

    List<PreferenciaFila> preferenciasDelCliente(Long idCliente);
}
