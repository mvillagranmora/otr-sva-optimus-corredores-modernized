package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.familia_b.support.ConfiguracionDelCaso;
import cl.bcs.tesoreria.familia_b.support.DiccionarioDeCaso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.ban.ConsultarSaldoCuentaBancoService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ban.ConsultarSaldoCuentaBanco} (entry point {@code call(conn, json)}) as
 * {@link ConsultarSaldoCuentaBancoService#execute(com.google.gson.JsonObject)}: balances (available T0, pending, accounting, projected
 * T1..T3) per bank account and day of a date range.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-01..08}. Collaborator RevisarRequisitosBasicos is a recorded fake (its
 * input is part of each golden, verified in {@code src/test-pending/tesoreria-b/}). The platform configuration
 * (TESORERIA/CONSULTASALDOBANCOINCLUYE...) and the message dictionary have no seam in the modern service yet (see README).
 */
@ExtendWith(ZonaUtc.class)
class ConsultarSaldoCuentaBancoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-01_saldo-un-dia-config-por-defecto",
            "TES-B-02_solo-saldo-disponible-flags-N",
            "TES-B-03_contable-sin-pendiente-NPE",
            "TES-B-04_todas-las-cuentas-rango-dos-dias",
            "TES-B-05_sin-cuentas-NO_EXISTEN_CUENTAS_BANCARIAS",
            "TES-B-06_requisitos-basicos-con-error",
            "TES-B-07_fecha-no-parseable-ParseException",
            "TES-B-08_cuenta-duplicada-clave-no-unica");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            return new ConsultarSaldoCuentaBancoService(caso.jdbc(), new DiccionarioDeCaso(), new ConfiguracionDelCaso(caso), col[0]).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarSaldoCuentaBanco");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ConsultarSaldoCuentaBanco.java:458,466,486,521 + sql/ConsultarSaldoCuentaBanco_CalculaSaldo*.sql: T0 balance of day 2025-04-01 from the last balance of 2025-03-28
    @Test
    void tesB01_saldoDeUnDiaConConfiguracionPorDefecto() {
        verificar(CASOS.get(0));
    }

    // ConsultarSaldoCuentaBanco.java:369-371,466,486,521: the three p_Incluye* flags N skip pending/accounting/projected (zeros and 0001-01-01 in the output)
    @Test
    void tesB02_soloSaldoDisponibleConFlagsEnN() {
        verificar(CASOS.get(1));
    }

    // ConsultarSaldoCuentaBanco.java:486-501: accounting balance requested without pending balance dereferences a null SaldoPendiente (legacy defect)
    @Test
    void tesB03_saldoContableSinPendienteLanzaNullPointerException() {
        verificar(CASOS.get(2));
    }

    // ConsultarSaldoCuentaBanco.java:376-395,396-420: null account filter = every account of the company, two-day range
    @Test
    void tesB04_todasLasCuentasDeLaEmpresaEnUnRangoDeDosDias() {
        verificar(CASOS.get(3));
    }

    // ConsultarSaldoCuentaBanco.java:390: no matching account -> error message NO_EXISTEN_CUENTAS_BANCARIAS and empty balances
    @Test
    void tesB05_sinCuentasInformaNoExistenCuentasBancarias() {
        verificar(CASOS.get(4));
    }

    // ConsultarSaldoCuentaBanco.java:356-360: basic requirements with an error short-circuits with those messages
    @Test
    void tesB06_requisitosBasicosConErrorCortaElServicio() {
        verificar(CASOS.get(5));
    }

    // ConsultarSaldoCuentaBanco.java:345-346: an unparseable date throws java.text.ParseException
    @Test
    void tesB07_fechaNoParseableLanzaParseException() {
        verificar(CASOS.get(6));
    }

    // ConsultarSaldoCuentaBanco.java:395: two requested accounts resolving to the same bank account -> List2Hash "key field is not unique"
    @Test
    void tesB08_cuentaSolicitadaDosVecesLanzaClaveNoUnica() {
        verificar(CASOS.get(7));
    }
}
