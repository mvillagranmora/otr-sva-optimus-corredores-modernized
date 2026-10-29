package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import org.springframework.stereotype.Service;

/**
 * Accrual of borrowed securities (legacy CentralizaDevengoEmprestito.java). Characterization: CONT-E-217..CONT-E-229.
 * Movement types DEVEMPRV / DEVEMPRF, instrument types EMP / ERF; the flow is {@link CentralizaDevengo}.
 */
@Service("centralizaDevengoEmprestito")
public class CentralizaDevengoEmprestito extends CentralizaDevengo {

    public static final String DEVENGO_EMPRESTITO_RV = "DEVEMPRV";
    public static final String DEVENGO_EMPRESTITO_RF = "DEVEMPRF";

    public CentralizaDevengoEmprestito(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository, new Variante(
                "cl/bcs/contabilidad/sql/CentralizaDevengoEmprestito_SelectMovimientoTipoCentralizacionTipo.sql",
                "cl/bcs/contabilidad/sql/CentralizaDevengoEmprestito_SelectMovimiento.sql",
                "cl/bcs/contabilidad/sql/CentralizaDevengoEmprestito_SelectMovimientoHistorico.sql",
                DEVENGO_EMPRESTITO_RV, DEVENGO_EMPRESTITO_RF, "EMP", "ERF"));
    }
}
