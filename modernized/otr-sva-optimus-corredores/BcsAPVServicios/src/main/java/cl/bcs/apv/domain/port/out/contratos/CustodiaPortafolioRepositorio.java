package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

/** Queries of the portfolio custody consultation (legacy ConsultarCustodiaPortafolioAPV SQL). */
public interface CustodiaPortafolioRepositorio {

    /** Row of the portfolio information query. */
    record InformacionPortafolioFila(String NombreProductoAgrupador, Integer IdProductoAgrupador, String Regimen, Long IdPortafolio,
            Long IdRegimenTributario, Integer IdPlanTipoInversion) {
    }

    /** Row of the transfer origin detail query. */
    record DetalleOrigenTraspasoFila(Long IdInstrumento, Long IdTraspaso, Long IdPortafolio, String CodigoRegimenTributario) {
    }

    /** Row of the APV-A tax regime query; the legacy POJO defaulted to 0 / empty text. */
    record RegimenFila(Long IdRegimenTributario, String Codigo) {
        public static RegimenFila vacio() {
            return new RegimenFila(0L, "");
        }
    }

    List<InformacionPortafolioFila> informacionPortafolio(List<Long> idsPortafolio);

    List<DetalleOrigenTraspasoFila> detalleOrigenTraspaso(List<Long> idsInstrumento, Long idTraspaso);

    /** The first row, or the empty regime when there is none. */
    RegimenFila regimenApvA();
}
