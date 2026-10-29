package cl.bcs.contabilidad.informe;

import cl.bcs.contabilidad.domain.model.clienterelacionado.FuentesOperaciones;
import cl.bcs.contabilidad.domain.port.out.ClienteRelacionadoRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.informe.InformeClienteRelacionadoWriter;
import cl.bcs.contabilidad.service.ConsultarOperacionesClienteRelacionadoService;
import cl.bcs.contabilidad.service.GenerarInformeClienteRelacionadoService;
import com.google.gson.JsonObject;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import static cl.bcs.contabilidad.informe.ClienteRelacionadoFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Characterization of legacy {@code GenerarInformeClienteRelacionado.logica(conn, in, debug=false)}
 * (GICR = legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/
 * GenerarInformeClienteRelacionado.java, lines 139-240), which is the path production reaches through
 * BcsContabilidadJobs/.../GenerarInformeClienteRelacionadoBatch.java:53 ({@code logica(conn, in, false)}).
 *
 * <p>The real {@link ConsultarOperacionesClienteRelacionadoService} and {@link InformeClienteRelacionadoWriter}
 * are wired; only the repository and the platform port are mocked. The file is really written to a JUnit
 * {@code @TempDir} and read back with POI.
 */
@ExtendWith(MockitoExtension.class)
class GenerarInformeClienteRelacionadoServiceTest {

    static final String NOMBRE = "InformeOperacionesClientesRelacionados_20082025_140509.xls";

    @Mock ClienteRelacionadoRepository repository;
    @Mock PlataformaPort plataforma;
    @TempDir Path tmp;

    GenerarInformeClienteRelacionadoService service;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(Instant.parse("2025-08-20T14:05:09Z"), ZoneOffset.UTC);
        var consulta = new ConsultarOperacionesClienteRelacionadoService(repository, plataforma);
        service = new GenerarInformeClienteRelacionadoService(consulta, repository, plataforma,
            new InformeClienteRelacionadoWriter(), reloj);
    }

    private static JsonObject entrada(String fechaInicio, String fechaFin) {
        JsonObject in = new JsonObject();
        in.addProperty("p_IdEmpresa", 1L);
        in.addProperty("p_IdUsuario", 97L);
        in.addProperty("p_IdModulo", "CON");
        in.addProperty("p_FechaInicio", fechaInicio);
        in.addProperty("p_FechaFin", fechaFin);
        return in;
    }

    private String rutaBase() {
        return tmp.toString().replace('\\', '/') + "/";
    }

    private void stubRutas(String rutaBase, String rutaInforme) {
        lenient().when(plataforma.obtenerParametroConfiguracion(eq(1L), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any()))
            .thenReturn(rutaBase);
        lenient().when(plataforma.obtenerParametroConfiguracion(eq(1L), eq("CONTABILIDAD"), eq("RutaInformeClienteRel"), any()))
            .thenReturn(rutaInforme);
    }

    private void stubDatos(FuentesOperaciones fuentes, byte[] logo) {
        lenient().when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        lenient().when(repository.consultarConceptosNormativos()).thenReturn(List.of(
            concepto(1, "Acciones", cond("TipoInstrumento", "ACN"))));
        lenient().when(repository.consultarOperaciones(anyString(), anyString())).thenReturn(fuentes);
        lenient().when(repository.consultarLogoCorredora(1L)).thenReturn(logo);
    }

    private static void assertError(JsonObject out, String codigo) {
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        var msgs = out.getAsJsonArray("P_OUT_MENSAJES");
        assertThat(msgs).hasSize(1);
        assertThat(msgs.get(0).getAsJsonObject().get("tipo").getAsString()).isEqualTo("ERROR");
        assertThat(msgs.get(0).getAsJsonObject().get("codigo").getAsString()).isEqualTo(codigo);
        assertThat(!out.has("RutaArchivo") || out.get("RutaArchivo").isJsonNull()).as("sin RutaArchivo").isTrue();
    }

    private long archivosEscritos() throws Exception {
        try (Stream<Path> s = Files.walk(tmp)) {
            return s.filter(Files::isRegularFile).count();
        }
    }

    // ------------------------------------------------------------------ CONT-D-50

    @Test
    @DisplayName("CONT-D-50: éxito - escribe RutaBaseArchivos+RutaInformeClienteRel+nombre y devuelve RutaArchivo = RutaInforme+nombre")
    void contD50_exito_escribeArchivoYDevuelveRutaRelativa() throws Exception {
        // GICR:153-166 (debug=false: rutaFisica = RutaArchivo + RutaInforme; rutaFinal = RutaInforme),
        // GICR:187-192 crea el directorio si no existe, GICR:226-231 RutaArchivo y escritura
        stubRutas(rutaBase(), "informes/");
        stubDatos(fuentes().comprobantes(comprobante(100).operado("1500").build()).build(), new byte[0]);

        JsonObject out = service.execute(entrada("2025-08-01", "2025-08-20"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(out.getAsJsonArray("P_OUT_MENSAJES")).isEmpty();
        assertThat(out.get("RutaArchivo").getAsString()).isEqualTo("informes/" + NOMBRE);

        Path archivo = tmp.resolve("informes").resolve(NOMBRE);
        assertThat(archivo).exists().isRegularFile();
        try (InputStream in = Files.newInputStream(archivo); HSSFWorkbook wb = new HSSFWorkbook(in)) {
            assertThat(wb.getSheetName(0)).isEqualTo("Conceptos");
            assertThat(wb.getSheetName(1)).isEqualTo("Operaciones");
            HSSFSheet conceptos = wb.getSheetAt(0);
            assertThat(conceptos.getRow(0).getCell(0).getStringCellValue())
                .isEqualTo("Informe de Operaciones con Clientes Relacionados\nDESDE 01-08-2025 HASTA 20-08-2025");
            assertThat(conceptos.getRow(11).getCell(0).getStringCellValue()).isEqualTo("   Acciones");
            assertThat(conceptos.getRow(11).getCell(2).getNumericCellValue()).isEqualTo(1500.0);
            assertThat(conceptos.getRow(12).getCell(0).getStringCellValue()).isEqualTo("Total");
            HSSFSheet ops = wb.getSheetAt(1);
            assertThat(ops.getRow(11).getCell(5).getNumericCellValue()).isEqualTo(100.0);
            assertThat(ops.getLastRowNum()).isEqualTo(11);
        }
        verify(repository).consultarOperaciones("2025-08-01", "2025-08-20");
    }

    @Test
    @DisplayName("CONT-D-50: notas de crédito salen negadas en la hoja Operaciones pero suman positivo en Conceptos")
    void contD50_extremoAExtremo_notaCredito() throws Exception {
        // ConsultarOperacionesClienteRelacionado.java:223-350 + GICR:404-405, 532
        stubRutas(rutaBase(), "informes/");
        stubDatos(fuentes().comprobantes(comprobante(7).tipoDocumento("NCR").operado("800").build()).build(), new byte[0]);

        JsonObject out = service.execute(entrada("2025-08-01", "2025-08-20"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        try (InputStream in = Files.newInputStream(tmp.resolve("informes").resolve(NOMBRE));
             HSSFWorkbook wb = new HSSFWorkbook(in)) {
            assertThat(wb.getSheetAt(0).getRow(11).getCell(2).getNumericCellValue()).isEqualTo(800.0);
            assertThat(wb.getSheetAt(1).getRow(11).getCell(3).getStringCellValue()).isEqualTo("NC");
            assertThat(wb.getSheetAt(1).getRow(11).getCell(7).getNumericCellValue()).isEqualTo(-800.0);
        }
    }

    @Test
    @DisplayName("CONT-D-50: fecha inicio = fecha fin es válida; el logo de la corredora se inserta en ambas hojas")
    void contD50_mismoDia_conLogo() throws Exception {
        // GICR:180 isBefore (igual no es error); GICR:214 logo; GICR:268-281, 310-323
        stubRutas(rutaBase(), "informes/");
        stubDatos(fuentes().build(), pngUnPixel());

        JsonObject out = service.execute(entrada("2025-08-20", "2025-08-20"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        try (InputStream in = Files.newInputStream(tmp.resolve("informes").resolve(NOMBRE));
             HSSFWorkbook wb = new HSSFWorkbook(in)) {
            assertThat(wb.getAllPictures()).hasSize(2);
        }
        verify(repository).consultarLogoCorredora(1L);
    }

    // ------------------------------------------------------------------ CONT-D-51

    @Test
    @DisplayName("CONT-D-51: RutaInformeClienteRel vacía -> FALTA_CONFIGURACION_RUTA_ARCHIVO, sin consultar ni escribir")
    void contD51_faltaConfiguracionRuta() throws Exception {
        // GICR:166-173 rutaFinal == "" ; tiene precedencia sobre fechas inválidas (GICR:177-184 va después)
        stubRutas(rutaBase(), "");

        JsonObject out = service.execute(entrada("2025-08-20", "2025-08-01"));

        assertError(out, "FALTA_CONFIGURACION_RUTA_ARCHIVO");
        verify(repository, never()).consultarOperaciones(anyString(), anyString());
        assertThat(archivosEscritos()).isZero();
    }

    // ------------------------------------------------------------------ CONT-D-52

    @Test
    @DisplayName("CONT-D-52: fecha fin anterior a fecha inicio -> UI_FECHAS_INVALIDAS, sin consultar ni escribir")
    void contD52_fechasInvalidas() throws Exception {
        // GICR:177-184
        stubRutas(rutaBase(), "informes/");

        JsonObject out = service.execute(entrada("2025-08-20", "2025-08-19"));

        assertError(out, "UI_FECHAS_INVALIDAS");
        verify(repository, never()).consultarOperaciones(anyString(), anyString());
        verify(repository, never()).consultarConceptosNormativos();
        assertThat(archivosEscritos()).isZero();
    }

    // ------------------------------------------------------------------ CONT-D-53

    @Test
    @DisplayName("CONT-D-53: rango > 3 meses -> el P_OUT_MENSAJE de la consulta ('FECHA_FUERA_RANGO') se usa como código")
    void contD53_errorDeConsulta_seUsaComoCodigo() throws Exception {
        // GICR:204-210 new MensajeSalida(0L, ERROR, salida.P_OUT_MENSAJE, ...)
        stubRutas(rutaBase(), "informes/");
        stubDatos(fuentes().build(), new byte[0]);

        JsonObject out = service.execute(entrada("2025-01-01", "2025-06-30"));

        assertError(out, "FECHA_FUERA_RANGO");
        assertThat(archivosEscritos()).isZero();
    }

    @Test
    @DisplayName("CONT-D-53: falla de BD en la consulta -> el texto de la excepción termina como 'codigo' del mensaje")
    void contD53_falloBd_textoExcepcionComoCodigo() throws Exception {
        // ConsultarOperacionesClienteRelacionado.java:351-354 -> P_OUT_MENSAJE = ex.getMessage(); GICR:207
        stubRutas(rutaBase(), "informes/");
        lenient().when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(repository.consultarConceptosNormativos()).thenReturn(List.of());
        when(repository.consultarOperaciones("2025-08-01", "2025-08-20")).thenThrow(new RuntimeException("falla-bd"));

        JsonObject out = service.execute(entrada("2025-08-01", "2025-08-20"));

        assertError(out, "falla-bd");
        assertThat(archivosEscritos()).isZero();
    }

    // ------------------------------------------------------------------ CONT-D-54

    @Test
    @DisplayName("CONT-D-54: fecha con formato inválido -> UI_ERROR_GENERAR_INFORME")
    void contD54_formatoFechaInvalido() throws Exception {
        // GICR:177 LocalDate.parse lanza dentro del try; GICR:233-237
        stubRutas(rutaBase(), "informes/");

        JsonObject out = service.execute(entrada("2025/08/01", "2025-08-20"));

        assertError(out, "UI_ERROR_GENERAR_INFORME");
        assertThat(archivosEscritos()).isZero();
    }

    @Test
    @DisplayName("CONT-D-54: parámetro de ruta inexistente (excepción de configuración) -> UI_ERROR_GENERAR_INFORME")
    void contD54_parametroInexistente() {
        // ConfiguracionSistema.getParametro lanza "Parametro ... no encontrado"; GICR:162 dentro del try
        when(plataforma.obtenerParametroConfiguracion(anyLong(), anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("Parametro PLA.RutaBaseArchivos en modulo PLATAFORMA no encontrado"));

        JsonObject out = service.execute(entrada("2025-08-01", "2025-08-20"));

        assertError(out, "UI_ERROR_GENERAR_INFORME");
    }

    @Test
    @DisplayName("CONT-D-54: falla al leer el logo -> UI_ERROR_GENERAR_INFORME y no queda archivo")
    void contD54_falloLogo() throws Exception {
        // GICR:214 dentro del try; el archivo se escribe recién en GICR:229
        stubRutas(rutaBase(), "informes/");
        stubDatos(fuentes().build(), new byte[0]);
        when(repository.consultarLogoCorredora(1L)).thenThrow(new RuntimeException("falla logo"));

        JsonObject out = service.execute(entrada("2025-08-01", "2025-08-20"));

        assertError(out, "UI_ERROR_GENERAR_INFORME");
        assertThat(archivosEscritos()).isZero();
    }
}
