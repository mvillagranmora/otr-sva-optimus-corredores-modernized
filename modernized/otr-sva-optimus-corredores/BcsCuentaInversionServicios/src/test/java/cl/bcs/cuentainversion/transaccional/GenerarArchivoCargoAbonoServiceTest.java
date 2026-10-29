package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.GenerarArchivoCargoAbonoService;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;
import cl.bcs.cuentainversion.transaccional.support.Archivos;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GenerarArchivoCargoAbono} (entry point {@code call(conn, json)}): writes a csv
 * {@code MovimientosCargoAbono_<yyyyMMdd_HHmmss>.csv} under {@code PLA.RUTABASEARCHIVOS + RUTAARCHIVOSCTA} (read from the configuration tables) with a
 * translated header and one line per row of the view ConsultarCargosAbonos, and answers the relative path. The test directory is the seed placeholder
 * {@code ${RUTA_BASE}}: the generated files are captured and compared; the run timestamp in the name is masked as {@code <TS>} ({@link Archivos#TIMESTAMP}).
 * The modern service does not write any file yet, so these tests are red until it does.
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-125..137}. Legacy: {@code GenerarArchivoCargoAbono}; modern: {@link GenerarArchivoCargoAbonoService}.
 */
@ExtendWith(ZonaUtc.class)
class GenerarArchivoCargoAbonoServiceTest {

    static final List<String> CASOS = List.of(
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

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        GenerarArchivoCargoAbonoService service = new GenerarArchivoCargoAbonoService(caso.jdbc(), new DiccionarioDeCaso());
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
                return GenerarArchivoCargoAbonoServiceTest.<RuntimeException, com.google.gson.JsonElement>lanzar(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R lanzar(Throwable t) throws T {
        throw (T) t;
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("GenerarArchivoCargoAbono");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CTA-A-125: GenerarArchivoCargoAbono.java:169-185: no configuration rows -> FALTA_CONFIGURACION_RUTA_ARCHIVO error, no file
    @Test
    void ctaA125_sinConfiguracionDeRutaEsError() {
        verificar(CASOS.get(0));
    }

    // CTA-A-126: GenerarArchivoCargoAbono.java:174-185: RutaCta missing -> the same error
    @Test
    void ctaA126_faltaRutaCtaEsError() {
        verificar(CASOS.get(1));
    }

    // CTA-A-127: GenerarArchivoCargoAbono.java:207-281: header (:221-256) + one csv line per view row (SelectCargosAbonos.sql); path = RutaCta + name (:189); count
    @Test
    void ctaA127_generaCsvConTodosLosMovimientosDelDia() {
        verificar(CASOS.get(2));
    }

    // CTA-A-128: GenerarArchivoCargoAbono_SelectCargosAbonos.sql: FechaSistema >= desde AND <= hasta, both inclusive
    @Test
    void ctaA128_rangoDeFechasEsInclusivo() {
        verificar(CASOS.get(3));
    }

    // CTA-A-129: GenerarArchivoCargoAbono_SelectCargosAbonos.sql: IdMoneda filter ('0' = all)
    @Test
    void ctaA129_filtroPorMoneda() {
        verificar(CASOS.get(4));
    }

    // CTA-A-130: GenerarArchivoCargoAbono_SelectCargosAbonos.sql: IdTipoMovimiento filter
    @Test
    void ctaA130_filtroPorTipoDeMovimiento() {
        verificar(CASOS.get(5));
    }

    // CTA-A-131: GenerarArchivoCargoAbono_SelectCargosAbonos.sql: IdConcepto filter
    @Test
    void ctaA131_filtroPorConcepto() {
        verificar(CASOS.get(6));
    }

    // CTA-A-132: GenerarArchivoCargoAbono_SelectCargosAbonos.sql: IdEstado filter
    @Test
    void ctaA132_filtroPorEstado() {
        verificar(CASOS.get(7));
    }

    // CTA-A-133: GenerarArchivoCargoAbono_SelectCargosAbonos.sql: COALESCE(IdEjecutivo,0) = :IdEjecutivo drops rows without executive
    @Test
    void ctaA133_filtroPorEjecutivoExcluyeMovimientosSinEjecutivo() {
        verificar(CASOS.get(8));
    }

    // CTA-A-134: GenerarArchivoCargoAbono.java:209: no rows -> no file, P_OUT_RUTAARCHIVO null, count 0
    @Test
    void ctaA134_sinRegistrosNoGeneraArchivo() {
        verificar(CASOS.get(9));
    }

    // CTA-A-135: GenerarArchivoCargoAbono_SelectCargosAbonos.sql: a NULL filter value ('= CASE WHEN NULL = ...') matches nothing (LEGACY QUIRK)
    @Test
    void ctaA135_filtroMonedaNuloNoDevuelveFilas() {
        verificar(CASOS.get(10));
    }

    // CTA-A-136: GenerarArchivoCargoAbono.java:257,265 (LEGACY DEFECT): a NULL date makes the whole line NULL, BufferedWriter.write(null) throws NullPointerException and the empty file stays
    @Test
    void ctaA136_fechaNulaEnLaVistaAnulaLaLineaYLanzaExcepcion() {
        verificar(CASOS.get(11));
    }

    // CTA-A-137: GenerarArchivoCargoAbono.java:219: FileWriter on a directory that does not exist -> FileNotFoundException
    @Test
    void ctaA137_directorioInexistenteLanzaExcepcion() {
        verificar(CASOS.get(12));
    }

}
