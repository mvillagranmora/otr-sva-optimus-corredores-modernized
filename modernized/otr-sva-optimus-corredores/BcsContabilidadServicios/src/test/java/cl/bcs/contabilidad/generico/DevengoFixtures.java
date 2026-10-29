package cl.bcs.contabilidad.generico;

import cl.bcs.contabilidad.domain.service.devengo.CarteraValorizada;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Test-only builder for {@link CarteraValorizada} rows (projection of
 * "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" used by the three Devengo
 * centralisations). Keeps the 19-component record constructor in one place.
 *
 * All values are synthetic; no data was copied from any environment.
 */
final class DevengoFixtures {

    static final Long   ID_EMPRESA     = 1L;
    static final String FECHA_CONTABLE = "2025-03-10";
    static final String ID_INFORME     = "INF-2025-03-10-0001";

    /** IdTipoCentralizacion per legacy SQL header comments. */
    static final Long TIPO_TASA_COMPRA_PACTADA = 440L; // CentralizaDevengoATasaCompraCarteraPactada_SelectEventos.sql:1
    static final Long TIPO_TASA_PACTO          = 430L; // CentralizaDevengoATasaPacto_SelectEventos.sql:1
    static final Long TIPO_CARTERA_PROPIA      = 450L; // CentralizaDevengoCarteraPropiaLibreYGarantia_SelectEventos.sql:1

    private DevengoFixtures() {}

    static Builder cartera() {
        return new Builder();
    }

    static final class Builder {
        Long idValorizacion = 70001L;
        Long idCartera = 55501L;
        Long idEmpresa = ID_EMPRESA;
        String idTipoOperacion = "COMPAC";
        String idClasificacionTipoInstrumento = "RF";
        Long folioOperacion = 88001L;
        Long folioCartera = 33001L;
        String documentoIdentidad = "11111111-1";
        Long idPortafolio = 300L;
        LocalDate fechaOperacion = LocalDate.of(2025, 2, 14);
        String idMonedaInstrumento = "CLP";
        String idMonedaSistema = "CLP";
        BigDecimal montoCompra = new BigDecimal("1000000.50");
        BigDecimal montoPactado = new BigDecimal("1005000.49");
        BigDecimal montoActual = new BigDecimal("1002500.50");
        BigDecimal montoProximo = new BigDecimal("1002666.67");
        BigDecimal montoInteres = new BigDecimal("166.67");
        BigDecimal montoInteresCorrido = new BigDecimal("2500.50");
        BigDecimal montoReajuste = new BigDecimal("0.00");

        Builder idValorizacion(Long v) { this.idValorizacion = v; return this; }
        Builder idCartera(Long v) { this.idCartera = v; return this; }
        Builder idTipoOperacion(String v) { this.idTipoOperacion = v; return this; }
        Builder clasificacion(String v) { this.idClasificacionTipoInstrumento = v; return this; }
        Builder folioOperacion(Long v) { this.folioOperacion = v; return this; }
        Builder folioCartera(Long v) { this.folioCartera = v; return this; }
        Builder documentoIdentidad(String v) { this.documentoIdentidad = v; return this; }
        Builder idPortafolio(Long v) { this.idPortafolio = v; return this; }
        Builder fechaOperacion(LocalDate v) { this.fechaOperacion = v; return this; }
        Builder idMonedaInstrumento(String v) { this.idMonedaInstrumento = v; return this; }
        Builder idMonedaSistema(String v) { this.idMonedaSistema = v; return this; }
        Builder montoCompra(String v) { this.montoCompra = dec(v); return this; }
        Builder montoPactado(String v) { this.montoPactado = dec(v); return this; }
        Builder montoActual(String v) { this.montoActual = dec(v); return this; }
        Builder montoProximo(String v) { this.montoProximo = dec(v); return this; }
        Builder montoInteres(String v) { this.montoInteres = dec(v); return this; }
        Builder montoInteresCorrido(String v) { this.montoInteresCorrido = dec(v); return this; }
        Builder montoReajuste(String v) { this.montoReajuste = dec(v); return this; }

        CarteraValorizada build() {
            return new CarteraValorizada(
                    idValorizacion,
                    idCartera,
                    idEmpresa,
                    idTipoOperacion,
                    idClasificacionTipoInstrumento,
                    folioOperacion,
                    folioCartera,
                    documentoIdentidad,
                    idPortafolio,
                    fechaOperacion,
                    idMonedaInstrumento,
                    idMonedaSistema,
                    montoCompra,
                    montoPactado,
                    montoActual,
                    montoProximo,
                    montoInteres,
                    montoInteresCorrido,
                    montoReajuste);
        }

        private static BigDecimal dec(String v) {
            return v == null ? null : new BigDecimal(v);
        }
    }
}
