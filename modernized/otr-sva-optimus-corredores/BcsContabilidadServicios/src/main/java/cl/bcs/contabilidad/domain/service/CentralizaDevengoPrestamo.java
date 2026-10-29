package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import org.springframework.stereotype.Service;

/**
 * Accrual of lent securities (legacy CentralizaDevengoPrestamo.java). Characterization: CONT-E-230..CONT-E-242.
 * Movement types DEVPRERV / DEVPRERF, instrument types PRT / PRF; the flow is {@link CentralizaDevengo}.
 */
@Service("centralizaDevengoPrestamo")
public class CentralizaDevengoPrestamo extends CentralizaDevengo {

    public static final String DEVENGO_PRESTAMO_RV = "DEVPRERV";
    public static final String DEVENGO_PRESTAMO_RF = "DEVPRERF";

    public CentralizaDevengoPrestamo(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository, new Variante(
                "cl/bcs/contabilidad/sql/CentralizaDevengoPrestamo_SelectMovimientoTipoCentralizacionTipo.sql",
                "cl/bcs/contabilidad/sql/CentralizaDevengoPrestamo_SelectMovimiento.sql",
                "cl/bcs/contabilidad/sql/CentralizaDevengoPrestamo_SelectMovimientoHistorico.sql",
                DEVENGO_PRESTAMO_RV, DEVENGO_PRESTAMO_RF, "PRT", "PRF"));
    }
}
