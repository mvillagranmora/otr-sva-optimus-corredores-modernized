package cl.bcs.custodia.port.out;

import cl.bcs.custodia.domain.PosicionCustodia;

import java.util.List;

/**
 * Output port for Custodia Servicios data access.
 *
 * Methods marked "UOE" throw UnsupportedOperationException when no HANA SP is available.
 */
public interface CustodiaServiciosDataPort {

    /** Plain-SQL: CONFIGURACION_T WHERE ID_EMPRESA=:idEmpresa AND ID_CONFIGURACION='FechaSistema' */
    String obtenerFechaSistema(Long idEmpresa);

    /** Plain-SQL: POSICION_CUSTODIA_T WHERE ID_PORTAFOLIO=:idPortafolio */
    List<PosicionCustodia> consultarPosicion(Long idPortafolio);

    /** UOE — requires HANA SP */
    void liquidarEmprestito(Long idEmpresa, Long idPortafolio, Long idCliente);

    /** UOE — requires HANA SP */
    void anticiparPrestamo(Long idEmpresa, Long idPortafolio, String tipoPrestamo);

    /** UOE — requires HANA SP */
    void guardarPatrimonio(Long idEmpresa, String fechaInicio, String fechaTermino);
}
