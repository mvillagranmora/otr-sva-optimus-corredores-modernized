package cl.bcs.contabilidad.domain.model.basilea;

import java.math.BigDecimal;

/**
 * One row of ITZ.InterfazBasilea. Legacy {@code ProcesarInterfazBasilea.ReplicaInterfazBasilea}
 * (ProcesarInterfazBasilea.java:49-209); components keep the legacy field order, which is also the order of the
 * {@code :Name} parameters of ProcesarInterfazBasilea_InsertInterfazBasilea.sql.
 *
 * <p>{@link #builder()} starts from the legacy constructor defaults (empty strings, zero amounts, FechaEnvio
 * 1900-01-01).
 */
public record RegistroInterfazBasilea(
        Long idInterno,
        String fechaEnvio,
        String tipoProducto,
        String relacionContable,
        String cuentaContable,
        Long codigoMoneda,
        String numeroOperacion,
        Long tipoTasaInteres,
        BigDecimal tasaInteresOperacion,
        BigDecimal tasaInteresCompuestoAnual,
        String tipoTasaBase,
        BigDecimal spread,
        String cadenciaRepricing,
        String centroGestor,
        String gestorLibro,
        String esTasaTecho,
        String tipoTasaTecho,
        BigDecimal tasaTecho,
        String esTasaPiso,
        String tipoTasaPiso,
        BigDecimal tasaPiso,
        String opcionPrepago,
        String fechaOpcionPrepago,
        String codigoProducto,
        String codigoSubproducto,
        String tipoCobertura,
        String claseEmision,
        Long cartera,
        String codigoEstandardBono,
        BigDecimal strike,
        String fechaVencimientoFinal,
        String indicadorCompensacion,
        String paisOrigen,
        String divisaCompensancion,
        String fechaInicioOperacion,
        String carencia,
        String tipoAmortizacion,
        String esAmortizacionEstandard,
        String frecuenciaPago,
        String fechaRevisionTasa,
        String tipoOpcion,
        String tipoVentanaEjecucion,
        BigDecimal valorTirCompra,
        BigDecimal valorTirMercado,
        Long indicadorTipoVencimiento,
        String indicadorFlujo,
        String fechaVencimientoFlujo,
        String plazoFlujo,
        BigDecimal flujoMonedaOrigen,
        BigDecimal flujoValorContable,
        BigDecimal flujoValorFinal,
        String monedaFlujo,
        String indicadorTipoMora,
        BigDecimal saldoCapitalMonedaOrigen,
        BigDecimal saldoCapitalValorContable,
        BigDecimal saldoCapitalValorFinal,
        BigDecimal reajusteCapital,
        String monedaDevengo,
        BigDecimal interesDevengado,
        BigDecimal interesDevengadoValorContable,
        BigDecimal interesDevengadoValorFinal,
        BigDecimal carteraVencidaMonedaOrigen,
        BigDecimal carteraVencidaValorContable,
        BigDecimal carteraVencidaValorFinal,
        BigDecimal castigosMonedaOrigen,
        BigDecimal castigosValorContable,
        BigDecimal castigosValorFinal,
        String numeroCliente,
        String rutCliente,
        String digitoVerificador,
        String indicadorLibro,
        String indicadorTipoFlujo,
        String indicadorReversa,
        String fechaRegistroOperacionSistema,
        BigDecimal montoOriginalOperacion,
        BigDecimal valorTasaCostoFondoOperacion,
        String fechaPrimerFlujo,
        String filler) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Long idInterno = 0L;
        private String fechaEnvio = "1900-01-01";
        private String tipoProducto = "";
        private String relacionContable = "";
        private String cuentaContable = "";
        private Long codigoMoneda = 0L;
        private String numeroOperacion = "";
        private Long tipoTasaInteres = 0L;
        private BigDecimal tasaInteresOperacion = BigDecimal.ZERO;
        private BigDecimal tasaInteresCompuestoAnual = BigDecimal.ZERO;
        private String tipoTasaBase = "";
        private BigDecimal spread = BigDecimal.ZERO;
        private String cadenciaRepricing = "";
        private String centroGestor = "";
        private String gestorLibro = "";
        private String esTasaTecho = "";
        private String tipoTasaTecho = "";
        private BigDecimal tasaTecho = BigDecimal.ZERO;
        private String esTasaPiso = "";
        private String tipoTasaPiso = "";
        private BigDecimal tasaPiso = BigDecimal.ZERO;
        private String opcionPrepago = "";
        private String fechaOpcionPrepago = "";
        private String codigoProducto = "";
        private String codigoSubproducto = "";
        private String tipoCobertura = "";
        private String claseEmision = "";
        private Long cartera = 0L;
        private String codigoEstandardBono = "";
        private BigDecimal strike = BigDecimal.ZERO;
        private String fechaVencimientoFinal = "";
        private String indicadorCompensacion = "";
        private String paisOrigen = "";
        private String divisaCompensancion = "";
        private String fechaInicioOperacion = "";
        private String carencia = "";
        private String tipoAmortizacion = "";
        private String esAmortizacionEstandard = "";
        private String frecuenciaPago = "";
        private String fechaRevisionTasa = "";
        private String tipoOpcion = "";
        private String tipoVentanaEjecucion = "";
        private BigDecimal valorTirCompra = BigDecimal.ZERO;
        private BigDecimal valorTirMercado = BigDecimal.ZERO;
        private Long indicadorTipoVencimiento = 0L;
        private String indicadorFlujo = "";
        private String fechaVencimientoFlujo = "";
        private String plazoFlujo = "";
        private BigDecimal flujoMonedaOrigen = BigDecimal.ZERO;
        private BigDecimal flujoValorContable = BigDecimal.ZERO;
        private BigDecimal flujoValorFinal = BigDecimal.ZERO;
        private String monedaFlujo = "";
        private String indicadorTipoMora = "";
        private BigDecimal saldoCapitalMonedaOrigen = BigDecimal.ZERO;
        private BigDecimal saldoCapitalValorContable = BigDecimal.ZERO;
        private BigDecimal saldoCapitalValorFinal = BigDecimal.ZERO;
        private BigDecimal reajusteCapital = BigDecimal.ZERO;
        private String monedaDevengo = "";
        private BigDecimal interesDevengado = BigDecimal.ZERO;
        private BigDecimal interesDevengadoValorContable = BigDecimal.ZERO;
        private BigDecimal interesDevengadoValorFinal = BigDecimal.ZERO;
        private BigDecimal carteraVencidaMonedaOrigen = BigDecimal.ZERO;
        private BigDecimal carteraVencidaValorContable = BigDecimal.ZERO;
        private BigDecimal carteraVencidaValorFinal = BigDecimal.ZERO;
        private BigDecimal castigosMonedaOrigen = BigDecimal.ZERO;
        private BigDecimal castigosValorContable = BigDecimal.ZERO;
        private BigDecimal castigosValorFinal = BigDecimal.ZERO;
        private String numeroCliente = "";
        private String rutCliente = "";
        private String digitoVerificador = "";
        private String indicadorLibro = "";
        private String indicadorTipoFlujo = "";
        private String indicadorReversa = "";
        private String fechaRegistroOperacionSistema = "";
        private BigDecimal montoOriginalOperacion = BigDecimal.ZERO;
        private BigDecimal valorTasaCostoFondoOperacion = BigDecimal.ZERO;
        private String fechaPrimerFlujo = "";
        private String filler = "";

        private Builder() {}

        public Builder idInterno(Long v) {
            this.idInterno = v;
            return this;
        }

        public Builder fechaEnvio(String v) {
            this.fechaEnvio = v;
            return this;
        }

        public Builder tipoProducto(String v) {
            this.tipoProducto = v;
            return this;
        }

        public Builder relacionContable(String v) {
            this.relacionContable = v;
            return this;
        }

        public Builder cuentaContable(String v) {
            this.cuentaContable = v;
            return this;
        }

        public Builder codigoMoneda(Long v) {
            this.codigoMoneda = v;
            return this;
        }

        public Builder numeroOperacion(String v) {
            this.numeroOperacion = v;
            return this;
        }

        public Builder tipoTasaInteres(Long v) {
            this.tipoTasaInteres = v;
            return this;
        }

        public Builder tasaInteresOperacion(BigDecimal v) {
            this.tasaInteresOperacion = v;
            return this;
        }

        public Builder tasaInteresCompuestoAnual(BigDecimal v) {
            this.tasaInteresCompuestoAnual = v;
            return this;
        }

        public Builder tipoTasaBase(String v) {
            this.tipoTasaBase = v;
            return this;
        }

        public Builder spread(BigDecimal v) {
            this.spread = v;
            return this;
        }

        public Builder cadenciaRepricing(String v) {
            this.cadenciaRepricing = v;
            return this;
        }

        public Builder centroGestor(String v) {
            this.centroGestor = v;
            return this;
        }

        public Builder gestorLibro(String v) {
            this.gestorLibro = v;
            return this;
        }

        public Builder esTasaTecho(String v) {
            this.esTasaTecho = v;
            return this;
        }

        public Builder tipoTasaTecho(String v) {
            this.tipoTasaTecho = v;
            return this;
        }

        public Builder tasaTecho(BigDecimal v) {
            this.tasaTecho = v;
            return this;
        }

        public Builder esTasaPiso(String v) {
            this.esTasaPiso = v;
            return this;
        }

        public Builder tipoTasaPiso(String v) {
            this.tipoTasaPiso = v;
            return this;
        }

        public Builder tasaPiso(BigDecimal v) {
            this.tasaPiso = v;
            return this;
        }

        public Builder opcionPrepago(String v) {
            this.opcionPrepago = v;
            return this;
        }

        public Builder fechaOpcionPrepago(String v) {
            this.fechaOpcionPrepago = v;
            return this;
        }

        public Builder codigoProducto(String v) {
            this.codigoProducto = v;
            return this;
        }

        public Builder codigoSubproducto(String v) {
            this.codigoSubproducto = v;
            return this;
        }

        public Builder tipoCobertura(String v) {
            this.tipoCobertura = v;
            return this;
        }

        public Builder claseEmision(String v) {
            this.claseEmision = v;
            return this;
        }

        public Builder cartera(Long v) {
            this.cartera = v;
            return this;
        }

        public Builder codigoEstandardBono(String v) {
            this.codigoEstandardBono = v;
            return this;
        }

        public Builder strike(BigDecimal v) {
            this.strike = v;
            return this;
        }

        public Builder fechaVencimientoFinal(String v) {
            this.fechaVencimientoFinal = v;
            return this;
        }

        public Builder indicadorCompensacion(String v) {
            this.indicadorCompensacion = v;
            return this;
        }

        public Builder paisOrigen(String v) {
            this.paisOrigen = v;
            return this;
        }

        public Builder divisaCompensancion(String v) {
            this.divisaCompensancion = v;
            return this;
        }

        public Builder fechaInicioOperacion(String v) {
            this.fechaInicioOperacion = v;
            return this;
        }

        public Builder carencia(String v) {
            this.carencia = v;
            return this;
        }

        public Builder tipoAmortizacion(String v) {
            this.tipoAmortizacion = v;
            return this;
        }

        public Builder esAmortizacionEstandard(String v) {
            this.esAmortizacionEstandard = v;
            return this;
        }

        public Builder frecuenciaPago(String v) {
            this.frecuenciaPago = v;
            return this;
        }

        public Builder fechaRevisionTasa(String v) {
            this.fechaRevisionTasa = v;
            return this;
        }

        public Builder tipoOpcion(String v) {
            this.tipoOpcion = v;
            return this;
        }

        public Builder tipoVentanaEjecucion(String v) {
            this.tipoVentanaEjecucion = v;
            return this;
        }

        public Builder valorTirCompra(BigDecimal v) {
            this.valorTirCompra = v;
            return this;
        }

        public Builder valorTirMercado(BigDecimal v) {
            this.valorTirMercado = v;
            return this;
        }

        public Builder indicadorTipoVencimiento(Long v) {
            this.indicadorTipoVencimiento = v;
            return this;
        }

        public Builder indicadorFlujo(String v) {
            this.indicadorFlujo = v;
            return this;
        }

        public Builder fechaVencimientoFlujo(String v) {
            this.fechaVencimientoFlujo = v;
            return this;
        }

        public Builder plazoFlujo(String v) {
            this.plazoFlujo = v;
            return this;
        }

        public Builder flujoMonedaOrigen(BigDecimal v) {
            this.flujoMonedaOrigen = v;
            return this;
        }

        public Builder flujoValorContable(BigDecimal v) {
            this.flujoValorContable = v;
            return this;
        }

        public Builder flujoValorFinal(BigDecimal v) {
            this.flujoValorFinal = v;
            return this;
        }

        public Builder monedaFlujo(String v) {
            this.monedaFlujo = v;
            return this;
        }

        public Builder indicadorTipoMora(String v) {
            this.indicadorTipoMora = v;
            return this;
        }

        public Builder saldoCapitalMonedaOrigen(BigDecimal v) {
            this.saldoCapitalMonedaOrigen = v;
            return this;
        }

        public Builder saldoCapitalValorContable(BigDecimal v) {
            this.saldoCapitalValorContable = v;
            return this;
        }

        public Builder saldoCapitalValorFinal(BigDecimal v) {
            this.saldoCapitalValorFinal = v;
            return this;
        }

        public Builder reajusteCapital(BigDecimal v) {
            this.reajusteCapital = v;
            return this;
        }

        public Builder monedaDevengo(String v) {
            this.monedaDevengo = v;
            return this;
        }

        public Builder interesDevengado(BigDecimal v) {
            this.interesDevengado = v;
            return this;
        }

        public Builder interesDevengadoValorContable(BigDecimal v) {
            this.interesDevengadoValorContable = v;
            return this;
        }

        public Builder interesDevengadoValorFinal(BigDecimal v) {
            this.interesDevengadoValorFinal = v;
            return this;
        }

        public Builder carteraVencidaMonedaOrigen(BigDecimal v) {
            this.carteraVencidaMonedaOrigen = v;
            return this;
        }

        public Builder carteraVencidaValorContable(BigDecimal v) {
            this.carteraVencidaValorContable = v;
            return this;
        }

        public Builder carteraVencidaValorFinal(BigDecimal v) {
            this.carteraVencidaValorFinal = v;
            return this;
        }

        public Builder castigosMonedaOrigen(BigDecimal v) {
            this.castigosMonedaOrigen = v;
            return this;
        }

        public Builder castigosValorContable(BigDecimal v) {
            this.castigosValorContable = v;
            return this;
        }

        public Builder castigosValorFinal(BigDecimal v) {
            this.castigosValorFinal = v;
            return this;
        }

        public Builder numeroCliente(String v) {
            this.numeroCliente = v;
            return this;
        }

        public Builder rutCliente(String v) {
            this.rutCliente = v;
            return this;
        }

        public Builder digitoVerificador(String v) {
            this.digitoVerificador = v;
            return this;
        }

        public Builder indicadorLibro(String v) {
            this.indicadorLibro = v;
            return this;
        }

        public Builder indicadorTipoFlujo(String v) {
            this.indicadorTipoFlujo = v;
            return this;
        }

        public Builder indicadorReversa(String v) {
            this.indicadorReversa = v;
            return this;
        }

        public Builder fechaRegistroOperacionSistema(String v) {
            this.fechaRegistroOperacionSistema = v;
            return this;
        }

        public Builder montoOriginalOperacion(BigDecimal v) {
            this.montoOriginalOperacion = v;
            return this;
        }

        public Builder valorTasaCostoFondoOperacion(BigDecimal v) {
            this.valorTasaCostoFondoOperacion = v;
            return this;
        }

        public Builder fechaPrimerFlujo(String v) {
            this.fechaPrimerFlujo = v;
            return this;
        }

        public Builder filler(String v) {
            this.filler = v;
            return this;
        }

        public RegistroInterfazBasilea build() {
            return new RegistroInterfazBasilea(idInterno, fechaEnvio, tipoProducto, relacionContable, cuentaContable, codigoMoneda, numeroOperacion, tipoTasaInteres, tasaInteresOperacion, tasaInteresCompuestoAnual, tipoTasaBase, spread, cadenciaRepricing, centroGestor, gestorLibro, esTasaTecho, tipoTasaTecho, tasaTecho, esTasaPiso, tipoTasaPiso, tasaPiso, opcionPrepago, fechaOpcionPrepago, codigoProducto, codigoSubproducto, tipoCobertura, claseEmision, cartera, codigoEstandardBono, strike, fechaVencimientoFinal, indicadorCompensacion, paisOrigen, divisaCompensancion, fechaInicioOperacion, carencia, tipoAmortizacion, esAmortizacionEstandard, frecuenciaPago, fechaRevisionTasa, tipoOpcion, tipoVentanaEjecucion, valorTirCompra, valorTirMercado, indicadorTipoVencimiento, indicadorFlujo, fechaVencimientoFlujo, plazoFlujo, flujoMonedaOrigen, flujoValorContable, flujoValorFinal, monedaFlujo, indicadorTipoMora, saldoCapitalMonedaOrigen, saldoCapitalValorContable, saldoCapitalValorFinal, reajusteCapital, monedaDevengo, interesDevengado, interesDevengadoValorContable, interesDevengadoValorFinal, carteraVencidaMonedaOrigen, carteraVencidaValorContable, carteraVencidaValorFinal, castigosMonedaOrigen, castigosValorContable, castigosValorFinal, numeroCliente, rutCliente, digitoVerificador, indicadorLibro, indicadorTipoFlujo, indicadorReversa, fechaRegistroOperacionSistema, montoOriginalOperacion, valorTasaCostoFondoOperacion, fechaPrimerFlujo, filler);
        }
    }
}
