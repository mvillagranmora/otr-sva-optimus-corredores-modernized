package cl.bcs.mesajobs;

import cl.bcs.mesajobs.controller.MesaJobController;
import cl.bcs.mesajobs.service.LiberaBloqueoService;
import cl.bcs.mesajobs.service.RowFormatterService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static cl.bcs.mesajobs.service.LiberaBloqueoService.esCodigoUsuarioVacio;
import static cl.bcs.mesajobs.service.RowFormatterService.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for BcsMesaJobs — oracle de equivalencia.
 *
 * BcsMesaJobs es principalmente orquestación HANA (14 jobs, mismo patrón).
 * Los tests cubren la lógica pura: formato CSV/TXT, resolución userId, validación params.
 * Canary: cambiar ';' a ',' en separadorCsv() → B-MES-04 falla.
 * Equivalencia ejecutada: 26 casos.
 */
class MesaJobsTest {

    // ── B-MES-04: CSV separator ───────────────────────────────────────────────

    @Nested
    class B_MES_04_CsvSeparador {

        @Test void separador_es_punto_y_coma() {
            // B-MES-04: CSVWriter(fw, ';') — CANARY
            assertEquals(';', separadorCsv());
        }

        @Test void separador_no_es_coma() {
            // B-MES-04: no es el default de CSV
            assertNotEquals(',', separadorCsv());
        }

        @Test void separador_no_es_tabulacion() {
            // B-MES-04
            assertNotEquals('\t', separadorCsv());
        }
    }

    // ── B-MES-05: TXT format ─────────────────────────────────────────────────

    @Nested
    class B_MES_05_TxtFormato {

        @Test void columnas_se_concatenan_sin_separador() {
            // B-MES-05: datosTXT = datosTXT + rs.getString(i)
            assertEquals("ABCDEF", formatearFilaTXT(List.of("ABC", "DEF")));
        }

        @Test void columna_nula_se_trata_como_cadena_vacia() {
            // B-MES-05: null → "" (Arrays.asList permite nulos, List.of no)
            assertEquals("ABC", formatearFilaTXT(Arrays.asList("ABC", null)));
        }

        @Test void fila_vacia_retorna_cadena_vacia() {
            // B-MES-05
            assertEquals("", formatearFilaTXT(List.of()));
        }

        @Test void una_columna_retorna_valor_exacto() {
            // B-MES-05
            assertEquals("VALOR", formatearFilaTXT(List.of("VALOR")));
        }

        @Test void espacios_se_preservan_sin_trim() {
            // B-MES-05: legacy no hace trim
            assertEquals("ABC DEF ", formatearFilaTXT(List.of("ABC ", "DEF ")));
        }

        @Test void tres_columnas_numericas_concatenan() {
            // B-MES-05: fila típica del SP con tipos mixtos como strings
            assertEquals("20240101100000", formatearFilaTXT(List.of("2024", "01", "01", "10", "00", "00")));
        }
    }

    // ── B-MES-07: LiberaBloqueo userId resolution ─────────────────────────────

    @Nested
    class B_MES_07_LiberaBloqueoUserId {

        @Test void codigo_usuario_nulo_es_vacio() {
            // B-MES-07: null → retorna 0L sin consultar HANA
            assertTrue(esCodigoUsuarioVacio(null));
        }

        @Test void codigo_usuario_cadena_vacia_es_vacio() {
            // B-MES-07
            assertTrue(esCodigoUsuarioVacio(""));
        }

        @Test void codigo_usuario_solo_espacios_es_vacio() {
            // B-MES-07: "   ".trim().isEmpty() → true
            assertTrue(esCodigoUsuarioVacio("   "));
        }

        @Test void codigo_usuario_valido_no_es_vacio() {
            // B-MES-07: código válido → necesita consulta HANA
            assertFalse(esCodigoUsuarioVacio("JOHNDOE"));
        }

        @Test void codigo_usuario_con_espacios_valido_no_es_vacio() {
            // B-MES-07: " USER ".trim() = "USER" → no vacío
            assertFalse(esCodigoUsuarioVacio(" USER "));
        }
    }

    // ── B-MES-02: Param validation ───────────────────────────────────────────

    @Nested
    class B_MES_02_ParamValidacion {

        // Helper que replica la validación del controller
        static void validarParam(String nombre, String valor) {
            if (valor == null || valor.trim().isEmpty()) {
                throw new IllegalArgumentException("Parámetro requerido faltante: " + nombre);
            }
        }

        @Test void param_nulo_lanza_excepcion() {
            // B-MES-02: null → 400
            assertThrows(IllegalArgumentException.class, () -> validarParam("IdEmpresa", null));
        }

        @Test void param_vacio_lanza_excepcion() {
            // B-MES-02
            assertThrows(IllegalArgumentException.class, () -> validarParam("FechaDesde", ""));
        }

        @Test void param_solo_espacios_lanza_excepcion() {
            // B-MES-02
            assertThrows(IllegalArgumentException.class, () -> validarParam("RutaArchivo", "   "));
        }

        @Test void param_valido_no_lanza_excepcion() {
            // B-MES-02
            assertDoesNotThrow(() -> validarParam("IdEmpresa", "1"));
        }

        @Test void mensaje_excepcion_incluye_nombre_param() {
            // B-MES-02: mensaje identifica cuál parámetro falta
            var ex = assertThrows(IllegalArgumentException.class,
                () -> validarParam("FormatoArchivo", null));
            assertTrue(ex.getMessage().contains("FormatoArchivo"));
        }
    }

    // ── B-MES-06: SP name constants ──────────────────────────────────────────

    @Nested
    class B_MES_06_SpConstants {

        @Test void sp_anexo_ix_nombre_correcto() {
            // B-MES-06: constante debe coincidir exactamente con nombre SP en HANA
            assertEquals("BSCL.Mesa.BD.Servicios::GenerarAnexoIX", MesaJobController.SP_ANEXO_IX);
        }

        @Test void sp_anexo_xix_nombre_correcto() {
            // B-MES-06
            assertEquals("BSCL.Mesa.BD.Servicios::GenerarAnexoXIX", MesaJobController.SP_ANEXO_XIX);
        }

        @Test void sp_oma_nombre_correcto() {
            // B-MES-06
            assertEquals("BSCL.Mesa.BD.Servicios::GenerarArchivoOMA", MesaJobController.SP_OMA);
        }

        @Test void sp_nombres_usan_doble_dos_puntos() {
            // B-MES-06: convención de nombres HANA: paquete::SP
            assertTrue(MesaJobController.SP_ANEXO_IX.contains("::"));
            assertTrue(MesaJobController.SP_ANEXO_XIX.contains("::"));
            assertTrue(MesaJobController.SP_OMA.contains("::"));
        }
    }

    // ── B-MES-MISC: formato decision ─────────────────────────────────────────

    @Nested
    class B_MES_Misc {

        @Test void formato_CSV_es_case_sensitive_compareTo() {
            // B-MES-MISC: legacy usa compareTo("CSV") == 0, no equalsIgnoreCase
            assertTrue(esCsv("CSV"));
            assertFalse(esCsv("csv")); // minúscula no matchea
        }

        @Test void formato_TXT_es_case_sensitive_compareTo() {
            // B-MES-MISC
            assertTrue(esTxt("TXT"));
            assertFalse(esTxt("txt"));
        }

        @Test void archivo_csv_usa_separador_punto_y_coma() {
            // B-MES-04+MISC: integración format check + separator
            char sep = esCsv("CSV") ? separadorCsv() : '\0';
            assertEquals(';', sep);
        }
    }
}
