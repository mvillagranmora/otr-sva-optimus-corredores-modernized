package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.function.BiFunction;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.GenerarArchivoReferencia;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Proves the FILE-CAPTURE harness (seed placeholder {@code ${RUTA_BASE}}, generated files captured and compared, run timestamp masked as {@code <TS>},
 * golden "archivos") and the GenerarArchivoCargoAbono goldens: an executable reference of the legacy sequence
 * ({@link GenerarArchivoReferencia}, same production SQL) goes GREEN on every golden, and four small mutations of it go RED.
 */
@ExtendWith(ZonaUtc.class)
class ArchivosSelfCheckTest {

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static final List<String> REFERENCIA = List.of(
            "CTA-A-125_sin-configuracion-de-ruta-es-error",
            "CTA-A-126_falta-ruta-cta-es-error",
            "CTA-A-127_genera-csv-con-todos-los-movimientos-del-dia",
            "CTA-A-128_rango-de-fechas-es-inclusivo",
            "CTA-A-129_filtro-por-moneda",
            "CTA-A-130_filtro-por-tipo-de-movimiento",
            "CTA-A-131_filtro-por-concepto",
            "CTA-A-132_filtro-por-estado",
            "CTA-A-133_filtro-por-ejecutivo-excluye-movimientos-sin-ejecutivo",
            "CTA-A-134_sin-registros-no-genera-archivo",
            "CTA-A-135_filtro-moneda-nulo-no-devuelve-filas",
            "CTA-A-136_fecha-nula-en-la-vista-anula-la-linea-y-lanza-excepcion",
            "CTA-A-137_directorio-inexistente-lanza-excepcion");

    private static void comparar(String nombre, BiFunction<String, String, String> mutador) {
        Caso caso = Caso.cargar(nombre);
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> GenerarArchivoReferencia.ejecutar(caso.jdbc(), caso.entrada().deepCopy(), mutador));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r);
        Verifica.archivos(caso);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ArchivosSelfCheck");
        assertThat(EJECUTADOS.total()).isGreaterThan(0);
    }

    @Test
    void referenciaConSqlDeProduccionCoincideConLosGoldensDelLegacyIncluidosLosArchivos() {
        for (String c : REFERENCIA) {
            comparar(c, null);
        }
    }

    // mutant 1: the date range becomes exclusive on the lower bound
    @Test
    void mutanteRangoDeFechasEsDetectado() {
        assertThatThrownBy(() -> comparar(REFERENCIA.get(3), (n, s) ->
                n.endsWith("SelectCargosAbonos") ? s.replace("\"FechaSistema\" \t>= :FechaDesde", "\"FechaSistema\" \t> :FechaDesde") : s))
                .isInstanceOf(AssertionError.class);
    }

    // mutant 2: the number separator of the csv changes (',' decimal -> '.')
    @Test
    void mutanteSeparadorDecimalEsDetectado() {
        assertThatThrownBy(() -> comparar(REFERENCIA.get(2), (n, s) ->
                n.endsWith("SelectCargosAbonos") ? s.replace("'.', ',')  || ';'", "'.', '.')  || ';'") : s))
                .isInstanceOf(AssertionError.class);
    }

    // mutant 3: the state filter is ignored
    @Test
    void mutanteFiltroDeEstadoEsDetectado() {
        assertThatThrownBy(() -> comparar(REFERENCIA.get(7), (n, s) ->
                n.endsWith("SelectCargosAbonos") ? s.replace("\"IdEstado\" \t\t= (CASE WHEN :IdEstado = '0' THEN \"IdEstado\" ELSE :IdEstado END)",
                        "\"IdEstado\" = \"IdEstado\"") : s))
                .isInstanceOf(AssertionError.class);
    }

    // mutant 4: the path returned drops the RutaCta prefix (configuration read with the wrong column)
    @Test
    void mutanteRutaDevueltaEsDetectado() {
        assertThatThrownBy(() -> comparar(REFERENCIA.get(2), (n, s) ->
                n.endsWith("SelectConfiguracion") ? s.replace("AS \"RutaCta\"", "AS \"RutaCtaX\"") : s))
                .isInstanceOf(AssertionError.class);
    }
}
