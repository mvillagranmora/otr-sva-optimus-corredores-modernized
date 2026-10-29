package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

/** Queries of the contract search for a collection (legacy BusquedaContratoSAAPVRecaudacion SQL). */
public interface BusquedaContratoRepositorio {

    /** Row of the collections query; the component names are the column labels. */
    record RecaudacionFila(Long IdRecaudacion, Long IdRecaudacionCliente, Long IdCliente, String IdRegimenTributario, String IdEstado,
            String DetalleRecaudacion) {
    }

    /** Row of the contracts query. The legacy POJO defaulted its fields to 0 / empty text; a NULL column overwrote them (kept). */
    record ContratoFila(Long IdCliente, Long IdContratoSAAPV, String TipoContrato, String IdRegimenTributario, String CodigoMedioRecaudacion) {
        public static ContratoFila vacio() {
            return new ContratoFila(0L, 0L, "", "", "");
        }
    }

    List<RecaudacionFila> recaudaciones(List<Long> idsRecaudacion);

    List<ContratoFila> contratos(List<Long> idsCliente);
}
