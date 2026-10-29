package cl.bcs.apv.domain.port.out.traspasos;

import java.math.BigDecimal;
import java.util.List;

/** Persistence port of {@code CalcularMontosDesinversion} (legacy {@code CalculoMontosDesinversion}). */
public interface CalcularMontosDesinversionRepositorio {

    /** Legacy {@code DetalleTraspasoDesinversion} (row of {@code ObtenerDetalleTraspasoDesinversion.sql}; the constructor defaults are the legacy ones). */
    class DetalleTraspasoDesinversion {
        public Long IdTraspaso;
        public BigDecimal MontoTraspaso;
        public BigDecimal PorcentajeTraspaso;
        public Long IdInstrumento;
        public String CodigoRegimenTributario;
        public Long IdPortafolio;
        public BigDecimal UFTraspaso;
        public String IdMoneda;
        public BigDecimal MontoCalculadoUF;
        public BigDecimal MontoCalculadoCLP;

        public DetalleTraspasoDesinversion() {
            IdTraspaso = 0L;
            IdInstrumento = 0L;
            CodigoRegimenTributario = "";
            IdPortafolio = 0L;
            UFTraspaso = BigDecimal.ZERO;
        }
    }

    /** Legacy {@code CalcularMontoNumeroCuota.ValorCuotaTipoParidad}. */
    class ValorCuotaTipoParidad {
        public String IdSerieFondo;
        public BigDecimal ValorCuota;
        public BigDecimal ValorDolarAporte;
        public BigDecimal ValorDolarRescate;
        public Long DecimalesCuota;
        public Long DecimalesValorCuota;
    }

    /** Origin detail of the traspaso with the currency of its instrument. */
    List<DetalleTraspasoDesinversion> detalleTraspaso(Long idTraspaso);

    /** Decimals of the fund series (the legacy fund module query). */
    List<ValorCuotaTipoParidad> valorCuotaTipoCambio(Long idEmpresa, String idSerieFondo);
}
