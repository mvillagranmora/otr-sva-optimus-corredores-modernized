package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.AlmacenArchivosDeCaso;
import cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.familia_b.support.ConfiguracionDelCaso;
import cl.bcs.tesoreria.familia_b.support.DiccionarioDeCaso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.SecuenciasDeCaso;
import cl.bcs.tesoreria.familia_b.support.Verifica;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.CargaMasivaIngresoEgresoDAPFFMMService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.CargaMasivaIngresoEgresoDAPFFMM} (entry point {@code call(conn, json)}) as
 * {@code CargaMasivaIngresoEgresoDAPFFMMService#execute(JsonObject)}: reads the day's DAP/FFMM "ingreso/egreso" file from the FTP tables,
 * validates each of its 39-field lines, creates ingresos and egresos (collaborator CrearIngresoEgreso, a recorded fake) and writes the validation
 * result as a csv under {@code PLA.RutaBaseArchivos + TES.RutaSalidaArchivosCargaMasiva} plus the output rows in FTP.Cabecera / FTP.Detalle.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-178..193}: output, FTP tables and the generated file (golden {@code archivos}: relative name -> content,
 * UTF-8 with the BOM the legacy writes, line endings normalised to \n; the file name carries only the load date so the timestamp mask is not needed here).
 * The oracle replaced {@code com.opencsv.CSVWriter} by a shim of the 3/4.x API (the local repository only has opencsv 5.9): see the README.
 */
@ExtendWith(ZonaUtc.class)
class CargaMasivaIngresoEgresoDAPFFMMServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-178_carga-valida-con-ingreso-y-egreso-genera-archivo-de-salida",
            "TES-B-179_sin-archivo-para-cargar-informa-error",
            "TES-B-180_archivo-sin-detalles-informa-archivo-vacio-y-queda-procesado",
            "TES-B-181_validaciones-de-campos-rechazan-las-lineas",
            "TES-B-182_linea-con-menos-de-39-campos-se-rechaza-y-la-validacion-queda-en-el-indice-cero",
            "TES-B-183_carpeta-de-salida-inexistente-devuelve-ruta-vacia-y-deja-pendiente",
            "TES-B-184_error-de-crear-ingreso-egreso-por-referencia-se-informa-en-el-archivo",
            "TES-B-185_solo-ingresos-no-llama-a-crear-egresos",
            "TES-B-186_persona-inexistente-envia-persona-y-portafolio-nulos",
            "TES-B-187_una-linea-valida-y-una-invalida-informa-aplicado-y-rechazado",
            "TES-B-188_linea-en-blanco-se-informa-como-sin-formato",
            "TES-B-189_referencia-de-forma-de-pago-distinta-a-la-del-registro-se-rechaza",
            "TES-B-190_otro-tipo-de-archivo-y-fecha-usan-su-propio-nombre",
            "TES-B-191_cabecera-ya-procesada-no-se-vuelve-a-cargar",
            "TES-B-192_ruta-base-no-configurada-lanza-excepcion",
            "TES-B-193_dos-detalles-misma-referencia-lanza-clave-duplicada");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Caso[] cargado = new Caso[1];
        Equivalencia.verificarConArchivos(EJECUTADOS, nombre, caso -> {
            cargado[0] = caso;
            col[0] = new ColaboradoresDeCaso(caso);
            return new CargaMasivaIngresoEgresoDAPFFMMService(caso.jdbc(), new DiccionarioDeCaso(), new ConfiguracionDelCaso(caso), new SecuenciasDeCaso(caso),
                    new AlmacenArchivosDeCaso(caso), col[0]).execute(caso.entrada().deepCopy());
        });
        // verificarConArchivos has no trace overload: the collaborator calls are compared right after it
        Verifica.colaboradores(cargado[0], col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CargaMasivaIngresoEgresoDAPFFMM");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:158-253,526-585: valid file: movements created, output csv (Aplicado), FTP rows written, header PRO, counter of ingested records
    @Test
    void tesB178_cargaValidaConIngresoYEgresoGeneraArchivoDeSalida() {
        verificar(CASOS.get(0));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:164-171: no pending header for the type and date: UI_CARGA_MASIVA_DAPFFMM_SIN_ARCHIVO_PARA_CARGAR
    @Test
    void tesB179_sinArchivoParaCargarInformaError() {
        verificar(CASOS.get(1));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:179-183,240: header without details: ARCHIVO_CARGA_VACIO, yet the header ends as PRO
    @Test
    void tesB180_archivoSinDetallesInformaArchivoVacioYQuedaProcesado() {
        verificar(CASOS.get(2));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:322-484: field validations (decimal comma, date format, movement type, concept, mandatory fields, S/N flags, reference equality): each invalid line is Rechazado with its text
    @Test
    void tesB181_validacionesDeCamposRechazanLasLineas() {
        verificar(CASOS.get(3));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:330-334,787-796: short line: validation stored at index 0 (legacy defect: the message lands on the FIRST line) and format rejection line
    @Test
    void tesB182_lineaConMenosDe39CamposSeRechazaYLaValidacionQuedaEnElIndiceCero() {
        verificar(CASOS.get(4));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:538-544,216-223: output folder missing: empty path, header back to PEN
    @Test
    void tesB183_carpetaDeSalidaInexistenteDevuelveRutaVaciaYDejaPendiente() {
        verificar(CASOS.get(5));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:612-621: CrearIngresoEgreso errors are matched to the line by NumeroReferencia and written in the comment column
    @Test
    void tesB184_errorDeCrearIngresoEgresoPorReferenciaSeInformaEnElArchivo() {
        verificar(CASOS.get(6));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:200-208: only MOVING lines: CrearIngresoEgreso is called once
    @Test
    void tesB185_soloIngresosNoLlamaACrearEgresos() {
        verificar(CASOS.get(7));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:759-785: unknown document: the LEFT JOIN returns null person/portfolio ids which travel to CrearIngresoEgreso
    @Test
    void tesB186_personaInexistenteEnviaPersonaYPortafolioNulos() {
        verificar(CASOS.get(8));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:587-623: mixed file: Aplicado and Rechazado lines
    @Test
    void tesB187_unaLineaValidaYUnaInvalidaInformaAplicadoYRechazado() {
        verificar(CASOS.get(9));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:326-328,593-597: a blank line is skipped by the validation but reported as lacking the required format
    @Test
    void tesB188_lineaEnBlancoSeInformaComoSinFormato() {
        verificar(CASOS.get(10));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:420-422: NumeroReferenciaFormaDePago must equal NumeroReferencia
    @Test
    void tesB189_referenciaDeFormaDePagoDistintaSeRechaza() {
        verificar(CASOS.get(11));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:214,820-824: type DAP and another date: file and header codes are derived from them
    @Test
    void tesB190_otroTipoDeArchivoYFechaUsanSuPropioNombre() {
        verificar(CASOS.get(12));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:164: only headers in state PEN are loaded
    @Test
    void tesB191_cabeceraYaProcesadaNoSeVuelveACargar() {
        verificar(CASOS.get(13));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:531: PLATAFORMA/PLA.RutaBaseArchivos missing: configuration exception, header PEN
    @Test
    void tesB192_rutaBaseNoConfiguradaLanzaExcepcion() {
        verificar(CASOS.get(14));
    }

    // CargaMasivaIngresoEgresoDAPFFMM.java:259: two lines with the same reference: Collectors.toMap duplicate key (IllegalStateException)
    @Test
    void tesB193_dosDetallesMismaReferenciaLanzaClaveDuplicada() {
        verificar(CASOS.get(15));
    }

}
