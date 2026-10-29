package cl.bcs.portafolio.port.out;

import cl.bcs.portafolio.domain.custodio.CuentaCustodioEntrada;
import cl.bcs.portafolio.domain.custodio.CuentaCustodioResultado;
import cl.bcs.portafolio.domain.preferencia.PreferenciaPagoEntrada;
import cl.bcs.portafolio.domain.preferencia.PreferenciaPagoResultado;

public interface CustodioPreferenciaPort {

    CuentaCustodioResultado consultarCuentaCustodio(CuentaCustodioEntrada entrada);
    void actualizarCuentaCustodio(CuentaCustodioEntrada entrada);
    PreferenciaPagoResultado crearPreferenciaPago(PreferenciaPagoEntrada entrada);
    void actualizarPreferenciaPago(PreferenciaPagoEntrada entrada);
}
