package cl.bcs.contabilidad.informe;

import cl.bcs.contabilidad.domain.model.basilea.RegistroInterfazBasilea;
import cl.bcs.contabilidad.domain.service.basilea.InterfazBasileaMapper;
import cl.bcs.contabilidad.domain.service.basilea.ResolucionCuentaBasilea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static cl.bcs.contabilidad.informe.ClienteRelacionadoFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of the pure parts of legacy {@code ProcesarInterfazBasilea}
 * (PIB = legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/
 * ProcesarInterfazBasilea.java):
 * <ul>
 *   <li>{@code buscarCuenta} (PIB:347-368) -> {@code InterfazBasileaMapper.buscarCuenta(dato, cuentas)}</li>
 *   <li>{@code crearRegistroBasilea} (PIB:370-447) plus the four assignments of PIB:329-333
 *       -> {@code InterfazBasileaMapper.crearRegistro(dato, fechaEnvio, cuentaContable, rutCorredora)}</li>
 * </ul>
 * NOTE: the legacy "interfaz Basilea" does not parse an input file. It reads five DB queries and inserts rows
 * into ITZ.InterfazBasilea; there is no layout/separator/header/trailer to characterize.
 */
class InterfazBasileaMapperTest {

    private static ResolucionCuentaBasilea buscar(DatoB dato, CuentaB... cuentas) {
        return InterfazBasileaMapper.buscarCuenta(dato.build(), java.util.Arrays.stream(cuentas).map(CuentaB::build).toList());
    }

    private static void assertUnica(ResolucionCuentaBasilea r, String cuenta) {
        assertThat(r.resultado()).isEqualTo(ResolucionCuentaBasilea.Resultado.UNICA);
        assertThat(r.cuentaContable()).isEqualTo(cuenta);
    }

    private static void assertSinCuenta(ResolucionCuentaBasilea r) {
        assertThat(r.resultado()).isEqualTo(ResolucionCuentaBasilea.Resultado.SIN_CUENTA);
    }

    // ------------------------------------------------------------------ CONT-D-60

    @Test
    @DisplayName("CONT-D-60: con todos los filtros vacíos y grupo/producto iguales se encuentra la única cuenta")
    void contD60_coincidenciaBasica() {
        // PIB:348-367
        assertUnica(buscar(dato(), cuenta("110101000001")), "110101000001");
    }

    @ParameterizedTest(name = "CONT-D-60: IdBolsa dato=''{0}'' cuenta=''{1}'' -> coincide={2}")
    @CsvSource(value = {
        "'',  '',      true",    // dato vacío -> cuenta debe ser '' (equalsIgnoreCase)
        "'',  SN,      false",
        "SN,  SN,      true",    // dato no vacío -> cuenta.contains(dato)
        "SN,  'SN,BEC', true",   // lista separada por coma en la configuración
        "SN,  sn,      false",   // contains es case-sensitive
        "S,   SN,      true"     // contains acepta sub-cadena
    })
    void contD60_filtroVacioIgualNoVacioContiene_idBolsa(String datoV, String cuentaV, boolean coincide) {
        // PIB:349
        var r = buscar(dato().idBolsa(datoV), cuenta("1").idBolsa(cuentaV));
        if (coincide) assertUnica(r, "1"); else assertSinCuenta(r);
    }

    @Test
    @DisplayName("CONT-D-60: EsVentaCorta, Clasificacion, TipoInstrumento, IdEmisor y Nemotecnico usan la misma regla vacío=igual / no-vacío=contains")
    void contD60_demasFiltrosContains() {
        // PIB:350, 353-356
        assertUnica(buscar(dato().esVentaCorta("S"), cuenta("1").esVentaCorta("S,N")), "1");
        assertSinCuenta(buscar(dato().esVentaCorta(""), cuenta("1").esVentaCorta("N")));
        assertUnica(buscar(dato().clasificacion("IF"), cuenta("1").clasificacion("RF,IF")), "1");
        assertSinCuenta(buscar(dato().clasificacion(""), cuenta("1").clasificacion("RF")));
        assertUnica(buscar(dato().tipoInstrumento("BON"), cuenta("1").tipoInstrumento("BONO")), "1");
        assertSinCuenta(buscar(dato().tipoInstrumento("BONO"), cuenta("1").tipoInstrumento("bono")));
        assertUnica(buscar(dato().idEmisor("CENTRAL"), cuenta("1").idEmisor("CENTRAL,TESORERIA")), "1");
        assertSinCuenta(buscar(dato().idEmisor(""), cuenta("1").idEmisor("CENTRAL")));
        assertUnica(buscar(dato().nemotecnico("PDBC"), cuenta("1").nemotecnico("PDBC,PRBC")), "1");
        assertSinCuenta(buscar(dato().nemotecnico(""), cuenta("1").nemotecnico("PDBC")));
    }

    @Test
    @DisplayName("CONT-D-60: Grupo, Producto, RelacionadoCorredora e IdTipoOperacion se comparan con equalsIgnoreCase")
    void contD60_filtrosIgualdadSinMayusculas() {
        // PIB:351-352, 357-358
        assertUnica(buscar(dato().grupo("libre").producto("rf-if"), cuenta("1")), "1");
        assertSinCuenta(buscar(dato().grupo("GARANTIZADA"), cuenta("1")));
        assertUnica(buscar(dato().relacionado("s"), cuenta("1").relacionado("S")), "1");
        assertSinCuenta(buscar(dato().relacionado("S"), cuenta("1").relacionado("S,N")));   // no contains here
        assertUnica(buscar(dato().tipoOperacion("comfwd"), cuenta("1").tipoOperacion("COMFWD")), "1");
    }

    @Test
    @DisplayName("CONT-D-60: RelacionadoCorredora null en el dato (LEFT JOIN de forward sin indicador) nunca coincide")
    void contD60_relacionadoNull_noCoincide() {
        // PIB:357 cb.RelacionadoCorredora.equalsIgnoreCase(null) == false; SelectForward.sql:25 LEFT JOIN
        assertSinCuenta(buscar(dato().relacionado(null), cuenta("1").relacionado("")));
    }

    // ------------------------------------------------------------------ CONT-D-61

    @Test
    @DisplayName("CONT-D-61: más de una cuenta coincide -> MULTIPLE")
    void contD61_masDeUna_multiple() {
        // PIB:361-362 return "ERROR"
        var r = buscar(dato(), cuenta("1"), cuenta("2"), cuenta("3").grupo("OTRO"));
        assertThat(r.resultado()).isEqualTo(ResolucionCuentaBasilea.Resultado.MULTIPLE);
    }

    // ------------------------------------------------------------------ CONT-D-62

    @Test
    @DisplayName("CONT-D-62: ninguna coincide, o la única tiene CuentaContable null -> SIN_CUENTA")
    void contD62_sinCuenta() {
        // PIB:363-364 (size 0 -> null); PIB:367 + 316 (CuentaContable null -> se trata igual que 'no encontrada')
        assertSinCuenta(buscar(dato(), cuenta("1").grupo("GARANTIZADA")));
        assertSinCuenta(buscar(dato()));
        assertSinCuenta(buscar(dato(), cuenta(null)));
    }

    // ------------------------------------------------------------------ CONT-D-63

    @Test
    @DisplayName("CONT-D-63: la única cuenta con CuentaContable '' -> UNICA con cuenta vacía")
    void contD63_cuentaVacia() {
        // PIB:367 -> "" ; PIB:322 rama isEmpty
        assertUnica(buscar(dato(), cuenta("")), "");
    }

    // ------------------------------------------------------------------ CONT-D-64

    @Test
    @DisplayName("CONT-D-64: valores por defecto del registro (constructor ReplicaInterfazBasilea + campos comunes)")
    void contD64_valoresPorDefecto() {
        // PIB:129-208 constructor; PIB:371-380 comunes; PIB:329-333 FechaEnvio/Cuenta/RUT
        // Grupo/Producto that hit no branch -> only defaults + common fields
        RegistroInterfazBasilea r = InterfazBasileaMapper.crearRegistro(
            dato().grupo("OTRO").producto("OTRO").monto("777").plazo("30").build(), "2025-09-14", "110101000001",
            RUT_CORREDORA);

        assertThat(r.idInterno()).isEqualTo(0L);
        assertThat(r.fechaEnvio()).isEqualTo("2025-09-14");
        assertThat(r.cuentaContable()).isEqualTo("110101000001");
        assertThat(r.rutCliente()).isEqualTo("76123456");
        assertThat(r.digitoVerificador()).isEqualTo("K");
        // common fields
        assertThat(r.indicadorTipoFlujo()).isEqualTo("NATU");
        assertThat(r.indicadorReversa()).isEqualTo("N");
        assertThat(r.indicadorTipoVencimiento()).isEqualTo(1L);
        assertThat(r.indicadorFlujo()).isEqualTo("FK");
        assertThat(r.tipoTasaInteres()).isEqualTo(1L);
        assertThat(r.flujoMonedaOrigen()).isEqualByComparingTo("777");
        assertThat(r.flujoValorContable()).isEqualByComparingTo("777");
        assertThat(r.flujoValorFinal()).isEqualByComparingTo("777");
        assertThat(r.plazoFlujo()).isEqualTo("30");
        // branch-specific fields stay at constructor defaults
        assertThat(r.tipoProducto()).isEmpty();
        assertThat(r.relacionContable()).isEmpty();
        assertThat(r.codigoMoneda()).isEqualTo(0L);
        assertThat(r.numeroOperacion()).isEmpty();
        assertThat(r.tasaInteresOperacion()).isEqualByComparingTo("0");
        assertThat(r.codigoProducto()).isEmpty();
        assertThat(r.codigoSubproducto()).isEmpty();
        assertThat(r.claseEmision()).isEmpty();
        assertThat(r.fechaVencimientoFinal()).isEmpty();
        assertThat(r.fechaInicioOperacion()).isEmpty();
        assertThat(r.fechaVencimientoFlujo()).isEmpty();
        assertThat(r.monedaFlujo()).isEmpty();
        // never assigned anywhere in the legacy: constructor defaults
        for (String s : List.of(r.tipoTasaBase(), r.cadenciaRepricing(), r.centroGestor(), r.gestorLibro(),
                r.esTasaTecho(), r.tipoTasaTecho(), r.esTasaPiso(), r.tipoTasaPiso(), r.opcionPrepago(),
                r.fechaOpcionPrepago(), r.tipoCobertura(), r.codigoEstandardBono(), r.indicadorCompensacion(),
                r.paisOrigen(), r.divisaCompensancion(), r.carencia(), r.tipoAmortizacion(),
                r.esAmortizacionEstandard(), r.frecuenciaPago(), r.fechaRevisionTasa(), r.tipoOpcion(),
                r.tipoVentanaEjecucion(), r.indicadorTipoMora(), r.monedaDevengo(), r.numeroCliente(),
                r.indicadorLibro(), r.fechaRegistroOperacionSistema(), r.fechaPrimerFlujo(), r.filler())) {
            assertThat(s).isEmpty();
        }
        for (BigDecimal b : List.of(r.tasaInteresCompuestoAnual(), r.spread(), r.tasaTecho(), r.tasaPiso(),
                r.strike(), r.valorTirCompra(), r.valorTirMercado(), r.saldoCapitalMonedaOrigen(),
                r.saldoCapitalValorContable(), r.saldoCapitalValorFinal(), r.reajusteCapital(),
                r.interesDevengado(), r.interesDevengadoValorContable(), r.interesDevengadoValorFinal(),
                r.carteraVencidaMonedaOrigen(), r.carteraVencidaValorContable(), r.carteraVencidaValorFinal(),
                r.castigosMonedaOrigen(), r.castigosValorContable(), r.castigosValorFinal(),
                r.montoOriginalOperacion(), r.valorTasaCostoFondoOperacion())) {
            assertThat(b).isEqualByComparingTo("0");
        }
        assertThat(r.cartera()).isEqualTo(0L);
    }

    // ------------------------------------------------------------------ CONT-D-65

    @ParameterizedTest(name = "CONT-D-65: ACN/ACCIONES EsVentaCorta={0} moneda={1} -> RelacionContable {2}, CodigoMoneda {3}")
    @CsvSource({"N,CLP,ACT,0", "S,CLP,PAC,0", "s,USD,PAC,2", "N,usd,ACT,2", "N,UF,ACT,0"})
    void contD65_acciones(String esVentaCorta, String moneda, String relacion, long codigoMoneda) {
        // PIB:380 PlazoFlujo "" para grupo ACN; PIB:382-389
        RegistroInterfazBasilea r = InterfazBasileaMapper.crearRegistro(
            dato().grupo("ACN").producto("ACCIONES").esVentaCorta(esVentaCorta).monedaInstrumento(moneda)
                .nemoBasilea("NEMOFICT").emisor("EMISORX").plazo("99").folio("55").tasa("1.5").build(),
            "2025-09-14", "CTA", RUT_CORREDORA);

        assertThat(r.tipoProducto()).isEqualTo("AC");
        assertThat(r.relacionContable()).isEqualTo(relacion);
        assertThat(r.codigoMoneda()).isEqualTo(codigoMoneda);
        assertThat(r.codigoProducto()).isEqualTo("AC");
        assertThat(r.codigoSubproducto()).isEqualTo("AC");
        assertThat(r.claseEmision()).isEqualTo("NEMOFICT");      // NemoBasilea, not Emisor
        assertThat(r.monedaFlujo()).isEqualTo(moneda);
        assertThat(r.plazoFlujo()).isEmpty();
        assertThat(r.numeroOperacion()).isEmpty();                // Folio not copied for acciones
        assertThat(r.tasaInteresOperacion()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("CONT-D-65: grupo ACN con otro producto -> sólo PlazoFlujo '' (ninguna rama aplica)")
    void contD65_acnOtroProducto() {
        // PIB:380 + PIB:382 (requiere ACCIONES)
        RegistroInterfazBasilea r = InterfazBasileaMapper.crearRegistro(
            dato().grupo("ACN").producto("RF-IF").plazo("99").build(), "2025-09-14", "CTA", RUT_CORREDORA);
        assertThat(r.plazoFlujo()).isEmpty();
        assertThat(r.tipoProducto()).isEmpty();
    }

    // ------------------------------------------------------------------ CONT-D-66

    @ParameterizedTest(name = "CONT-D-66: {0}/RF-IF IdTipoReajuste={1} -> MonedaFlujo ''{2}''")
    @CsvSource({
        "LIBRE,NR,CLP", "GARANTIZADA,UF,CLP", "libre,uf,CLP", "LIBRE,USD,USD", "GARANTIZADA,IVP,IVP"
    })
    void contD66_rfLibreGarantizada(String grupo, String reajuste, String monedaFlujo) {
        // PIB:390-403 ("GARANTIZADA" ^ "LIBRE") && "RF-IF"; MonedaFlujo = (UF ^ NR) ? "CLP" : IdTipoReajuste
        RegistroInterfazBasilea r = InterfazBasileaMapper.crearRegistro(
            dato().grupo(grupo).producto("RF-IF").tipoReajuste(reajuste).folio("123456").tasa("3.25")
                .emisor("EMISORX").fechaVencimiento("2030-06-30").fechaOperacion("2024-01-15").plazo("1750").build(),
            "2025-09-14", "CTA", RUT_CORREDORA);

        assertThat(r.numeroOperacion()).isEqualTo("123456");
        assertThat(r.tasaInteresOperacion()).isEqualByComparingTo("3.25");
        assertThat(r.tipoProducto()).isEqualTo("RF");
        assertThat(r.relacionContable()).isEqualTo("ACT");
        assertThat(r.codigoProducto()).isEqualTo("RF");
        assertThat(r.codigoSubproducto()).isEqualTo("RF");
        assertThat(r.claseEmision()).isEqualTo("EMISORX");
        assertThat(r.fechaVencimientoFinal()).isEqualTo("2030-06-30");
        assertThat(r.fechaInicioOperacion()).isEqualTo("2024-01-15");
        assertThat(r.fechaVencimientoFlujo()).isEqualTo("2030-06-30");
        assertThat(r.monedaFlujo()).isEqualTo(monedaFlujo);
        assertThat(r.codigoMoneda()).as("RF libre/garantizada never sets CodigoMoneda").isEqualTo(0L);
        assertThat(r.plazoFlujo()).isEqualTo("1750");
    }

    @Test
    @DisplayName("CONT-D-66: IdTipoReajuste null -> MonedaFlujo null")
    void contD66_reajusteNull() {
        // PIB:401-403 equalsIgnoreCase(null) false ^ false -> dato.IdTipoReajuste (null)
        RegistroInterfazBasilea r = InterfazBasileaMapper.crearRegistro(
            dato().grupo("LIBRE").producto("RF-IF").tipoReajuste(null).build(), "2025-09-14", "CTA", RUT_CORREDORA);
        assertThat(r.monedaFlujo()).isNull();
    }

    // ------------------------------------------------------------------ CONT-D-67

    @ParameterizedTest(name = "CONT-D-67: PACTEADA IdTipoReajuste={0} -> CodigoMoneda {1}, MonedaFlujo ''{2}''")
    @CsvSource({"CLP,0,CLP", "UF,1,CLP", "USD,2,USD", "EUR,3,EUR", "NR,0,CLP", "eur,3,eur", "IVP,0,IVP"})
    void contD67_pacteada(String reajuste, long codigoMoneda, String monedaFlujo) {
        // PIB:404-422
        RegistroInterfazBasilea r = InterfazBasileaMapper.crearRegistro(
            dato().grupo("PACTEADA").producto("RF-IF").tipoReajuste(reajuste).folio("998877").tasa("0.45")
                .emisor("EMISORX").fechaVencimiento("2030-06-30").fechaVencimientoOperacion("2025-10-01")
                .fechaOperacion("2024-01-15").fechaLiquidacion("2025-09-10").build(),
            "2025-09-14", "CTA", RUT_CORREDORA);

        assertThat(r.numeroOperacion()).isEqualTo("998877");
        assertThat(r.tasaInteresOperacion()).isEqualByComparingTo("0.45");
        assertThat(r.tipoProducto()).isEqualTo("PC");
        assertThat(r.relacionContable()).isEqualTo("PAS");
        assertThat(r.codigoProducto()).isEqualTo("PC");
        assertThat(r.codigoSubproducto()).isEqualTo("PC");
        assertThat(r.claseEmision()).isEqualTo("EMISORX");
        assertThat(r.fechaVencimientoFinal()).isEqualTo("2025-10-01");   // FechaVencimientoOperacion
        assertThat(r.fechaInicioOperacion()).isEqualTo("2025-09-10");    // FechaLiquidacion
        assertThat(r.fechaVencimientoFlujo()).isEqualTo("2030-06-30");   // FechaVencimiento (instrumento)
        assertThat(r.codigoMoneda()).isEqualTo(codigoMoneda);
        assertThat(r.monedaFlujo()).isEqualTo(monedaFlujo);
    }

    // ------------------------------------------------------------------ CONT-D-68

    @ParameterizedTest(name = "CONT-D-68: FORWARD grupo={0} principal={1} secundaria={2} -> {3}, CodigoMoneda {4}, MonedaFlujo {5}")
    @CsvSource({
        "OBLIG,USD,CLP,PAS,0,CLP",
        "OBLIG,CLP,UF,PAS,1,UF",
        "OBLIG,CLP,EUR,PAS,3,EUR",
        "DERECHO,USD,CLP,ACT,2,USD",
        "DERECHO,EUR,CLP,ACT,3,EUR",
        "DERECHO,JPY,CLP,ACT,0,JPY",
        "NOCIONAL,USD,CLP,ACT,0,USD",
        "oblig,USD,usd,PAS,2,usd"
    })
    void contD68_forward(String grupo, String principal, String secundaria, String relacion, long codigoMoneda,
                         String monedaFlujo) {
        // PIB:423-444 (rama por Producto FORWARD, cualquier grupo)
        RegistroInterfazBasilea r = InterfazBasileaMapper.crearRegistro(
            dato().grupo(grupo).producto("FORWARD").monedaPrincipal(principal).monedaSecundaria(secundaria)
                .folio("4455").tasa("5.1").emisor("EMISORX").fechaVencimiento("2025-12-31")
                .fechaOperacion("2025-06-30").plazo("108").build(),
            "2025-09-14", "CTA", RUT_CORREDORA);

        assertThat(r.tipoProducto()).isEqualTo("FW");
        assertThat(r.relacionContable()).isEqualTo(relacion);
        assertThat(r.codigoProducto()).isEqualTo("FW");
        assertThat(r.codigoSubproducto()).isEqualTo("FW");
        assertThat(r.fechaVencimientoFinal()).isEqualTo("2025-12-31");
        assertThat(r.fechaInicioOperacion()).isEqualTo("2025-06-30");
        assertThat(r.fechaVencimientoFlujo()).isEqualTo("2025-12-31");
        assertThat(r.numeroOperacion()).isEqualTo("4455");
        assertThat(r.tasaInteresOperacion()).isEqualByComparingTo("5.1");
        assertThat(r.codigoMoneda()).isEqualTo(codigoMoneda);
        assertThat(r.monedaFlujo()).isEqualTo(monedaFlujo);
        assertThat(r.claseEmision()).as("forward does not set ClaseEmision").isEmpty();
        assertThat(r.plazoFlujo()).isEqualTo("108");
    }

    // ------------------------------------------------------------------ CONT-D-69

    @Test
    @DisplayName("CONT-D-69: ACCIONES con grupo distinto de ACN y RF-IF con grupo desconocido no aplican ninguna rama")
    void contD69_combinacionesSinRama() {
        // PIB:382, 390, 404, 423
        for (var d : List.of(dato().grupo("LIBRE").producto("ACCIONES"), dato().grupo("NOCIONAL").producto("RF-IF"))) {
            RegistroInterfazBasilea r = InterfazBasileaMapper.crearRegistro(d.plazo("12").build(), "2025-09-14", "CTA",
                RUT_CORREDORA);
            assertThat(r.tipoProducto()).isEmpty();
            assertThat(r.codigoProducto()).isEmpty();
            assertThat(r.plazoFlujo()).isEqualTo("12");
        }
    }

    // ------------------------------------------------------------------ CONT-D-70

    @ParameterizedTest(name = "CONT-D-70: RUT corredora ''{0}'' -> RutCliente ''{1}'', DV ''{2}''")
    @CsvSource({"76123456-K,76123456,K", "7123456-5,07123456,5", "123-4,00000123,4", "076123456-k,76123456,k"})
    void contD70_rutCorredora(String rut, String cuerpo, String dv) {
        // PIB:332 String.format("%08d", Long.valueOf(rut.split("-")[0])); PIB:333 split("-")[1]
        RegistroInterfazBasilea r = InterfazBasileaMapper.crearRegistro(dato().build(), "2025-09-14", "CTA", rut);
        assertThat(r.rutCliente()).isEqualTo(cuerpo);
        assertThat(r.digitoVerificador()).isEqualTo(dv);
    }
}
