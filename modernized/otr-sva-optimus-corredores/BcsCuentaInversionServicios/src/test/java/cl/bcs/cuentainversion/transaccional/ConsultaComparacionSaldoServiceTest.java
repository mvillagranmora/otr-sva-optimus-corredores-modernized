package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.ConsultaComparacionSaldoService;
import cl.bcs.cuentainversion.service.FormatoMoneda;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;
import cl.bcs.cuentainversion.transaccional.support.Archivos;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ConsultaComparacionSaldo}: compares the last certified balance of each account
 * (CTA.CargoAbonoMovimiento) with the balance of the last movement in the views, keeps the work table CTA.Saldo_Cuenta_Proceso only during the run and
 * writes {@code DiferenciaSaldos_<ts>.csv} (same file-capture scheme as GenerarArchivoCargoAbono).
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-138..145}. Legacy: {@code ConsultaComparacionSaldo}; modern: {@link ConsultaComparacionSaldoService}.
 */
@ExtendWith(ZonaUtc.class)
class ConsultaComparacionSaldoServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-138_sin-configuracion-de-ruta-es-error",
            "CTA-A-139_saldos-distintos-generan-linea-de-diferencia",
            "CTA-A-140_saldos-iguales-informan-sin-diferencias-pero-generan-archivo",
            "CTA-A-141_varias-cuentas-moneda-usd-con-decimales",
            "CTA-A-142_movimiento-posterior-a-la-fecha-no-entra",
            "CTA-A-143_toma-el-ultimo-movimiento-certificado-y-ignora-pendientes",
            "CTA-A-144_fecha-efectiva-nula-usa-fecha-hasta",
            "CTA-A-145_sin-movimientos-solo-encabezado");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ConsultaComparacionSaldoService service = new ConsultaComparacionSaldoService(caso.jdbc(), new FormatoMoneda(caso.jdbc()), new DiccionarioDeCaso());
        Verifica.Ejecucion r = ejecutar(() -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r);
        Verifica.archivos(caso);
    }

    /** Runs a service call that may throw a checked exception; it reaches Verifica unchanged (its type is pinned by the goldens). */
    @FunctionalInterface
    private interface Llamada {
        com.google.gson.JsonElement ejecutar() throws Exception;
    }

    private static Verifica.Ejecucion ejecutar(Llamada llamada) {
        return Verifica.Ejecucion.de(() -> {
            try {
                return llamada.ejecutar();
            } catch (Exception e) {
                return ConsultaComparacionSaldoServiceTest.<RuntimeException, com.google.gson.JsonElement>lanzar(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R lanzar(Throwable t) throws T {
        throw (T) t;
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultaComparacionSaldo");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CTA-A-138: ConsultaComparacionSaldo.java:158-172: no configuration -> FALTA_CONFIGURACION_RUTA_ARCHIVO, no file
    @Test
    void ctaA138_sinConfiguracionDeRutaEsError() {
        verificar(CASOS.get(0));
    }

    // CTA-A-139: ConsultaComparacionSaldo.java:212-243 + SelectSaldosMovimientos.sql: differing balances -> one csv line, work table emptied
    @Test
    void ctaA139_saldosDistintosGeneranLineaDeDiferencia() {
        verificar(CASOS.get(1));
    }

    // CTA-A-140: ConsultaComparacionSaldo.java:218-222,243-246 (LEGACY QUIRK): no differences -> NO_SE_ENCONTRARON_DIFERENCIAS counted as an ERROR, yet the header-only csv is generated and its path returned
    @Test
    void ctaA140_saldosIgualesInformanSinDiferenciasPeroGeneranArchivo() {
        verificar(CASOS.get(2));
    }

    // CTA-A-141: ConsultaComparacionSaldo.java:232-234: MonedaFormato.MONTO per account currency (CLP 0 decimals, USD 2)
    @Test
    void ctaA141_variasCuentasMonedaUsdConDecimales() {
        verificar(CASOS.get(3));
    }

    // CTA-A-142: ConsultaComparacionSaldo_SelectSaldosCuentas.sql: only movements effective on or before FechaDesde
    @Test
    void ctaA142_movimientoPosteriorALaFechaNoEntra() {
        verificar(CASOS.get(4));
    }

    // CTA-A-143: ConsultaComparacionSaldo_SelectSaldosCuentas.sql: last CRT movement by certification date; PEN ignored for the balance
    @Test
    void ctaA143_tomaElUltimoMovimientoCertificadoYIgnoraPendientes() {
        verificar(CASOS.get(5));
    }

    // CTA-A-144: ConsultaComparacionSaldo_SelectSaldosCuentas.sql: COALESCE(FechaEfectivaMovimiento, TO_DATE(FechaHasta)) <= FechaDesde
    @Test
    void ctaA144_fechaEfectivaNulaUsaFechaHasta() {
        verificar(CASOS.get(6));
    }

    // CTA-A-145: ConsultaComparacionSaldo.java:217-221: no movements at all -> only the header, NO_SE_ENCONTRARON_DIFERENCIAS
    @Test
    void ctaA145_sinMovimientosSoloEncabezado() {
        verificar(CASOS.get(7));
    }

}
