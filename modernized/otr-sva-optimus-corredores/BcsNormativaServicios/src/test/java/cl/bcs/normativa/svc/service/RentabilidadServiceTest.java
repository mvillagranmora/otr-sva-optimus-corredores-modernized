package cl.bcs.normativa.svc.service;

import cl.bcs.normativa.svc.domain.cartola.*;
import cl.bcs.normativa.svc.service.cartola.RentabilidadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de caracterización de la lógica pura de rentabilidad.
 * No requieren Spring — todos los métodos bajo prueba son static.
 *
 * <p>Fórmula verificada contra el legacy CalcularRentabilidad:
 * <pre>
 *   numerador_P   = (patrimonioConsulta - saldoInicial_P) - aportes_P + rescates_P
 *   denominador_P = saldoInicial_P + aportesPeriodo_P - rescatesPeriodo_P
 *   rent_P        = (denominador == 0) ? 0 : numerador / denominador [scale=2, HALF_UP]
 * </pre>
 */
class RentabilidadServiceTest {

    // ---- Caso 1: denominador cero → rentabilidad cero ----

    @Test
    @DisplayName("Denominador cero retorna BigDecimal.ZERO")
    void denominadorCero_retornaCero() {
        SaldoCuentaCliente caja    = new SaldoCuentaCliente();
        SaldoCuentaCliente titulos = new SaldoCuentaCliente();
        SaldoFinalCliente  saldo   = new SaldoFinalCliente();
        // saldo todo cero → denominadores = 0

        Rentabilidad r = RentabilidadService.calcularRentabilidadCliente(
                1L, 1L, caja, titulos, saldo);

        assertThat(r.rentabilidadMTD).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(r.rentabilidadYTD).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(r.rentabilidadITD).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---- Caso 2: rentabilidad MTD positiva simple ----

    @Test
    @DisplayName("Rentabilidad MTD positiva simple — sin movimientos de títulos")
    void rentabilidadMTD_positiva_simple() {
        /*
         * patrimonioConsulta = 1_100_000
         * saldoInicialMTD    =   900_000
         * aportesCajaMTD     =    50_000
         * rescatesCajaMTD    =         0
         * aportesPeriodoMTD  =    50_000
         * rescatesPeriodoMTD =         0
         *
         * numerador   = (1_100_000 - 900_000) - 50_000 + 0 = 150_000
         * denominador = 900_000 + 50_000 - 0 = 950_000
         * rentabilidad = 150_000 / 950_000 ≈ 0.16 (escala 2, HALF_UP)
         */
        SaldoCuentaCliente caja = new SaldoCuentaCliente();
        caja.idCliente = 42L;
        caja.totalAbonosCajaMTD = new BigDecimal("50000");
        caja.abonosCajaMTDRentabilidad = new BigDecimal("50000");

        SaldoCuentaCliente titulos = new SaldoCuentaCliente();
        titulos.idCliente = 42L;

        SaldoFinalCliente saldo = new SaldoFinalCliente();
        saldo.idCliente = 42L;
        saldo.totalPatrimonioFechaConsulta = new BigDecimal("1100000");
        saldo.totalMTDSaldoFinal           = new BigDecimal("900000");

        Rentabilidad r = RentabilidadService.calcularRentabilidadCliente(
                42L, 1L, caja, titulos, saldo);

        // 150000 / 950000 = 0.157894... → 0.16 con HALF_UP
        assertThat(r.rentabilidadMTD).isEqualByComparingTo(new BigDecimal("0.16"));
        assertThat(r.idCliente).isEqualTo(42L);
    }

    // ---- Caso 3: rentabilidad negativa (pérdida) ----

    @Test
    @DisplayName("Rentabilidad MTD negativa cuando patrimonio cae")
    void rentabilidadMTD_negativa() {
        /*
         * patrimonioConsulta = 800_000
         * saldoInicialMTD    = 1_000_000
         * (sin movimientos)
         *
         * numerador   = (800_000 - 1_000_000) - 0 + 0 = -200_000
         * denominador = 1_000_000 + 0 - 0 = 1_000_000
         * rentabilidad = -200_000 / 1_000_000 = -0.20
         */
        SaldoCuentaCliente caja    = new SaldoCuentaCliente();
        SaldoCuentaCliente titulos = new SaldoCuentaCliente();
        SaldoFinalCliente saldo    = new SaldoFinalCliente();
        saldo.totalPatrimonioFechaConsulta = new BigDecimal("800000");
        saldo.totalMTDSaldoFinal           = new BigDecimal("1000000");

        Rentabilidad r = RentabilidadService.calcularRentabilidadCliente(
                10L, 1L, caja, titulos, saldo);

        assertThat(r.rentabilidadMTD).isEqualByComparingTo(new BigDecimal("-0.20"));
    }

    // ---- Caso 4: null safe (no NullPointerException con campos null) ----

    @Test
    @DisplayName("Campos null en SaldoCuentaCliente tratados como cero")
    void nullSafe_camposNullTratadosComoCero() {
        SaldoCuentaCliente caja    = new SaldoCuentaCliente();
        caja.totalAbonosCajaMTD = null; // campo null explícito
        SaldoCuentaCliente titulos = new SaldoCuentaCliente();
        SaldoFinalCliente saldo    = new SaldoFinalCliente();

        // No debe lanzar NullPointerException
        Rentabilidad r = RentabilidadService.calcularRentabilidadCliente(
                1L, 1L, caja, titulos, saldo);
        assertThat(r).isNotNull();
        assertThat(r.rentabilidadMTD).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---- Caso 5: lista completa de clientes ----

    @Test
    @DisplayName("calcularRentabilidades retorna un resultado por cliente único")
    void calcularRentabilidades_clientesDistintos() {
        SaldoCuentaCliente caja1 = new SaldoCuentaCliente();
        caja1.idCliente = 1L;
        SaldoCuentaCliente titulos1 = new SaldoCuentaCliente();
        titulos1.idCliente = 1L;
        SaldoFinalCliente saldo1 = new SaldoFinalCliente();
        saldo1.idCliente = 1L;
        saldo1.totalPatrimonioFechaConsulta = new BigDecimal("100000");
        saldo1.totalMTDSaldoFinal = new BigDecimal("90000");

        SaldoCuentaCliente caja2 = new SaldoCuentaCliente();
        caja2.idCliente = 2L;
        SaldoCuentaCliente titulos2 = new SaldoCuentaCliente();
        titulos2.idCliente = 2L;
        SaldoFinalCliente saldo2 = new SaldoFinalCliente();
        saldo2.idCliente = 2L;
        saldo2.totalPatrimonioFechaConsulta = new BigDecimal("200000");
        saldo2.totalMTDSaldoFinal = new BigDecimal("190000");

        CalcularRentabilidadEntrada entrada = new CalcularRentabilidadEntrada();
        entrada.idEmpresa = 1L;
        entrada.esPauta = false;
        entrada.movimientosCaja    = List.of(caja1, caja2);
        entrada.movimientosTitulo  = List.of(titulos1, titulos2);
        entrada.saldoFinal         = List.of(saldo1, saldo2);
        entrada.listaClientes      = List.of(
                new ClienteInfo(1L, 1L), new ClienteInfo(2L, 1L));

        List<RentabilidadService.ClienteEmpresa> clienteEmpresa = List.of(
                new RentabilidadService.ClienteEmpresa(1L, 1L),
                new RentabilidadService.ClienteEmpresa(2L, 1L));

        var resultado = RentabilidadService.calcularRentabilidades(
                clienteEmpresa, entrada.listaClientes, entrada);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.stream().map(r -> r.idCliente)).contains(1L, 2L);
    }

    // ---- Caso 6: cálculo MTD/YTD/ITD independientes ----

    @Test
    @DisplayName("MTD, YTD e ITD se calculan independientemente con saldos distintos")
    void calcularRentabilidad_mTD_yTD_iTD_independientes() {
        SaldoCuentaCliente caja    = new SaldoCuentaCliente();
        SaldoCuentaCliente titulos = new SaldoCuentaCliente();
        SaldoFinalCliente saldo    = new SaldoFinalCliente();

        saldo.totalPatrimonioFechaConsulta = new BigDecimal("1000");
        saldo.totalMTDSaldoFinal           = new BigDecimal("500");   // gain 100%
        saldo.totalYTDSaldoFinal           = new BigDecimal("800");   // gain 25%
        saldo.totalITDSaldoFinal           = new BigDecimal("200");   // gain 400%

        Rentabilidad r = RentabilidadService.calcularRentabilidadCliente(
                1L, 1L, caja, titulos, saldo);

        // MTD: (1000-500)/500 = 1.00
        assertThat(r.rentabilidadMTD).isEqualByComparingTo(new BigDecimal("1.00"));
        // YTD: (1000-800)/800 = 0.25
        assertThat(r.rentabilidadYTD).isEqualByComparingTo(new BigDecimal("0.25"));
        // ITD: (1000-200)/200 = 4.00
        assertThat(r.rentabilidadITD).isEqualByComparingTo(new BigDecimal("4.00"));
    }
}
