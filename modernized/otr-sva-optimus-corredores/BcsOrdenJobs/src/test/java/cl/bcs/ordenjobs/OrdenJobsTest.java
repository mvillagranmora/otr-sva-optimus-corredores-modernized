package cl.bcs.ordenjobs;

import cl.bcs.ordenjobs.domain.CampoSql;
import cl.bcs.ordenjobs.domain.TransaccionRV;
import cl.bcs.ordenjobs.service.ControlMercadoService;
import cl.bcs.ordenjobs.service.SqlTableBuilderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static cl.bcs.ordenjobs.service.ControlMercadoService.*;
import static cl.bcs.ordenjobs.service.SqlTableBuilderService.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for BcsOrdenJobs — oracle de equivalencia.
 *
 * Comportamientos puros pinned:
 *   B-ORD-01: getCreateTableString — casing exacto "Create table"
 *   B-ORD-02: getDropTableString — "drop table" minúscula — CANARY
 *   B-ORD-03: getInsertBatchString(ArrayList) — INSERT con lista de campos
 *   B-ORD-04: getInsertBatchString(Class) — campos _-prefijados omitidos; coma basada en índice
 *   B-ORD-05: clave de trade = tradeID + fechaSistema + tipo
 *   B-ORD-06: constantes COMPRA="COM", VENTA="VEN"
 *   B-ORD-07: FLUJO_MODIFICACION_AUTO="MODIFICACION_AUTO"
 *   B-ORD-08: MOTIVO_ANULACION="ANULACION POR SISTEMA BURSATIL"
 *   B-ORD-09: igualdad numérica usa BigDecimal.compareTo (no .equals)
 *   B-ORD-10: bug coma final cuando campo _ es el último en getFields()
 *   B-ORD-11: igualdad insensible a escala BigDecimal
 *   B-ORD-12: repositorio HANA → UnsupportedOperationException
 *
 * Canary: cambiar "drop table" a "DROP TABLE" en eliminarTabla() → B-ORD-02 falla.
 * Equivalencia ejecutada: 40 casos.
 */
class OrdenJobsTest {

    // ── Clases auxiliares para pruebas de reflexión (getFields solo devuelve public) ──

    public static class DosNormales {
        public String fieldA;
        public String fieldB;
    }

    public static class ConPrefijo {
        public String fieldA;
        public String _oculto;  // último → índice-based comma bug
    }

    public static class SoloPrefijo {
        public String _oculto;
        public String fieldA;  // no último → sin trailing comma
    }

    public static class MixedCentro {
        public String fieldA;
        public String _skip;
        public String fieldB;
    }

    // ── B-ORD-01: CREATE TABLE ───────────────────────────────────────────────────

    @Nested
    class B_ORD_01_CreateTable {

        @Test void un_campo_crea_sql_correcto() {
            // B-ORD-01: "Create table" (C mayúscula, resto minúscula) — casing pinned
            var campos = List.of(new CampoSql("ID", " INTEGER"));
            assertEquals("Create table MI_TABLA(\"ID\" INTEGER);", crearTabla(campos, "MI_TABLA"));
        }

        @Test void dos_campos_separados_con_coma_newline() {
            // B-ORD-01: separador ",\n" entre campos (no al final)
            var campos = List.of(
                new CampoSql("ID", " INTEGER"),
                new CampoSql("NOMBRE", " VARCHAR(100)")
            );
            String expected = "Create table T(\"ID\" INTEGER,\n\"NOMBRE\" VARCHAR(100));";
            assertEquals(expected, crearTabla(campos, "T"));
        }

        @Test void tres_campos_dos_separadores() {
            // B-ORD-01: N-1 separadores para N campos
            var campos = List.of(
                new CampoSql("A", " INT"),
                new CampoSql("B", " VARCHAR(10)"),
                new CampoSql("C", " DECIMAL(10,2)")
            );
            String sql = crearTabla(campos, "TBL");
            assertEquals(2, sql.chars().filter(c -> c == '\n').count());
        }

        @Test void sufijo_siempre_termina_en_punto_y_coma() {
            // B-ORD-01: ");" al final
            var campos = List.of(new CampoSql("X", " INT"));
            assertTrue(crearTabla(campos, "T").endsWith(");"));
        }

        @Test void nombre_tabla_aparece_tal_cual() {
            // B-ORD-01: el nombre de tabla no se escapa ni modifica
            var campos = List.of(new CampoSql("ID", " INTEGER"));
            assertTrue(crearTabla(campos, "BSCL.Orden.BD::DMA.TABLA").contains("BSCL.Orden.BD::DMA.TABLA"));
        }
    }

    // ── B-ORD-02: DROP TABLE — CANARY ────────────────────────────────────────────

    @Nested
    class B_ORD_02_DropTable {

        @Test void drop_table_usa_minusculas() {
            // B-ORD-02: CANARY — "drop table", no "DROP TABLE"
            assertEquals("drop table MI_TABLA", eliminarTabla("MI_TABLA"));
        }

        @Test void drop_table_no_tiene_punto_y_coma() {
            // B-ORD-02: sin ";" al final (a diferencia de CREATE)
            assertFalse(eliminarTabla("T").endsWith(";"));
        }

        @Test void drop_table_concatena_nombre_sin_espacio_extra() {
            // B-ORD-02: "drop table " + tableName — exactamente un espacio
            assertEquals("drop table ESQUEMA.TABLA", eliminarTabla("ESQUEMA.TABLA"));
        }
    }

    // ── B-ORD-03: INSERT por lista de CampoSql ───────────────────────────────────

    @Nested
    class B_ORD_03_InsertPorLista {

        @Test void un_campo_genera_sql_correcto() {
            // B-ORD-03
            var campos = List.of(new CampoSql("ID", " INT"));
            String sql = insertarPorLista(campos, "T");
            assertTrue(sql.startsWith("INSERT INTO T(\"ID\")\n"));
            assertTrue(sql.endsWith("VALUES (:ID)"));
        }

        @Test void dos_campos_separados_con_coma() {
            // B-ORD-03: coma entre columnas en SELECT y VALUES
            var campos = List.of(new CampoSql("ID", " INT"), new CampoSql("NOMBRE", " VARCHAR"));
            String sql = insertarPorLista(campos, "T");
            assertTrue(sql.contains("\"ID\",\"NOMBRE\""));
            assertTrue(sql.contains(":ID,:NOMBRE"));
        }

        @Test void sin_coma_al_final_de_ultimo_campo() {
            // B-ORD-03: no trailing comma en INSERT por lista
            var campos = List.of(new CampoSql("A", " INT"), new CampoSql("B", " INT"));
            String sql = insertarPorLista(campos, "T");
            assertFalse(sql.contains("B\",)") || sql.contains(":B,)"));
            assertTrue(sql.contains("\"B\")\n"));
            assertTrue(sql.contains(":B)"));
        }

        @Test void nombre_campo_con_comillas_dobles() {
            // B-ORD-03: columna envuelta en " en el INSERT
            var campos = List.of(new CampoSql("MI_CAMPO", " VARCHAR"));
            String sql = insertarPorLista(campos, "T");
            assertTrue(sql.contains("\"MI_CAMPO\""));
        }
    }

    // ── B-ORD-04: INSERT por Class (reflexión, campos _ omitidos) ────────────────

    @Nested
    class B_ORD_04_InsertPorClase {

        @Test void dos_campos_normales_sin_trailing_comma() {
            // B-ORD-04: sin campos _, resultado limpio
            String sql = insertarPorClase(DosNormales.class, "T");
            assertTrue(sql.contains("\"fieldA\",\"fieldB\""));
            assertTrue(sql.contains(":fieldA,:fieldB"));
            assertFalse(sql.contains(",)") || sql.contains(",\n"));
        }

        @Test void campo_guion_bajo_al_final_produce_trailing_comma() {
            // B-ORD-10/B-ORD-04: CANARY — bug coma basada en índice
            // ConPrefijo: [0]=fieldA (no _), [1]=_oculto (_, skip)
            // i=0: fieldA → comma because 0 < 2-1 → trailing comma PINNED
            String sql = insertarPorClase(ConPrefijo.class, "T");
            assertTrue(sql.contains("\"fieldA\","), "debe haber coma final (bug legacy pinned)");
            assertTrue(sql.contains(":fieldA,"), "debe haber coma final en VALUES (bug legacy pinned)");
        }

        @Test void campo_guion_bajo_al_inicio_no_produce_trailing_comma() {
            // B-ORD-04: [0]=_oculto (skip), [1]=fieldA (no _) → 1 == 2-1 → sin comma
            String sql = insertarPorClase(SoloPrefijo.class, "T");
            assertTrue(sql.contains("\"fieldA\")"));
            assertTrue(sql.contains(":fieldA)"));
        }

        @Test void campo_guion_bajo_en_centro_no_afecta_resultado_logico() {
            // B-ORD-04: [0]=fieldA [1]=_skip [2]=fieldB
            // i=0: fieldA → 0 < 2 → ","
            // i=1: _skip → continue
            // i=2: fieldB → 2 < 2 false → sin comma
            String sql = insertarPorClase(MixedCentro.class, "T");
            assertTrue(sql.contains("\"fieldA\",\"fieldB\""));
            assertTrue(sql.contains(":fieldA,:fieldB"));
        }

        @Test void campos_guion_bajo_no_aparecen_en_sql() {
            // B-ORD-04: _oculto nunca aparece en el SQL generado
            String sql = insertarPorClase(ConPrefijo.class, "T");
            assertFalse(sql.contains("_oculto"));
        }
    }

    // ── B-ORD-05/06: Clave de trade ──────────────────────────────────────────────

    @Nested
    class B_ORD_05_06_TradeKey {

        @Test void key_compra_es_tradeid_mas_fecha_mas_com() {
            // B-ORD-05: keyCompra = tradeID + fechaSistema + COMPRA
            assertEquals("TRD001" + "2024-01-15" + "COM",
                keyCompra("TRD001", "2024-01-15"));
        }

        @Test void key_venta_es_tradeid_mas_fecha_mas_ven() {
            // B-ORD-05: keyVenta = tradeID + fechaSistema + VENTA
            assertEquals("TRD001" + "2024-01-15" + "VEN",
                keyVenta("TRD001", "2024-01-15"));
        }

        @Test void constante_compra_es_com() {
            // B-ORD-06: CANARY — no "COMPRA", no "C", exactamente "COM"
            assertEquals("COM", COMPRA);
        }

        @Test void constante_venta_es_ven() {
            // B-ORD-06: CANARY — exactamente "VEN"
            assertEquals("VEN", VENTA);
        }

        @Test void key_compra_y_venta_difieren_solo_en_sufijo() {
            // B-ORD-05: misma fecha, mismo tradeID, solo cambia sufijo
            String kc = keyCompra("T1", "2024-01-01");
            String kv = keyVenta("T1", "2024-01-01");
            assertEquals(kc.length(), kv.length());
            assertTrue(kc.endsWith("COM"));
            assertTrue(kv.endsWith("VEN"));
        }

        @Test void clave_concatena_sin_separador() {
            // B-ORD-05: no hay guion ni separador entre partes
            assertEquals("ABC2024-03-15COM", keyCompra("ABC", "2024-03-15"));
        }
    }

    // ── B-ORD-07/08: Constantes de flujo y motivo ────────────────────────────────

    @Nested
    class B_ORD_07_08_Constantes {

        @Test void flujo_modificacion_auto_sin_guion() {
            // B-ORD-07: "MODIFICACION_AUTO" — guion bajo, sin espacio
            assertEquals("MODIFICACION_AUTO", FLUJO_MODIFICACION_AUTO);
        }

        @Test void motivo_anulacion_con_espacio_no_guion() {
            // B-ORD-08: "ANULACION POR SISTEMA BURSATIL" — espacios, no guiones
            assertEquals("ANULACION POR SISTEMA BURSATIL", MOTIVO_ANULACION);
        }

        @Test void motivo_anulacion_no_tiene_guiones_bajos() {
            // B-ORD-08
            assertFalse(MOTIVO_ANULACION.contains("_"));
        }
    }

    // ── B-ORD-09/11: Igualdad de transacciones BigDecimal ────────────────────────

    @Nested
    class B_ORD_09_11_TransaccionIgualdad {

        private TransaccionRV trx(BigDecimal quantity, BigDecimal price) {
            return new TransaccionRV(
                "T1", "20240115120000000", "AAPL", "C",
                "INS", "MKT", "SC", "OT", "N",
                quantity, price,
                new BigDecimal("100000.00"),
                new BigDecimal("0.00"),
                "BBR", "BEF", "BET", "SBR", "SEF", "SET",
                new BigDecimal("100.00"), new BigDecimal("150.50"),
                new BigDecimal("200.00"), new BigDecimal("149.50"),
                "ISIN001", "RV", "TIF", "SESS", "ET", "MKT", "12:00:00"
            );
        }

        @Test void misma_escala_son_iguales() {
            // B-ORD-09
            assertTrue(sonTransaccionesIguales(
                trx(new BigDecimal("100"), new BigDecimal("150.50")),
                trx(new BigDecimal("100"), new BigDecimal("150.50"))
            ));
        }

        @Test void escala_diferente_mismo_valor_son_iguales_con_compareTo() {
            // B-ORD-11: 1.0 vs 1.00 — compareTo == 0 aunque .equals() sea false
            assertTrue(sonTransaccionesIguales(
                trx(new BigDecimal("100.0"), new BigDecimal("150.5")),
                trx(new BigDecimal("100.00"), new BigDecimal("150.50"))
            ));
        }

        @Test void valor_diferente_no_son_iguales() {
            // B-ORD-09: cantidad diferente → false
            assertFalse(sonTransaccionesIguales(
                trx(new BigDecimal("100"), new BigDecimal("150.50")),
                trx(new BigDecimal("101"), new BigDecimal("150.50"))
            ));
        }

        @Test void price_diferente_no_son_iguales() {
            // B-ORD-09
            assertFalse(sonTransaccionesIguales(
                trx(new BigDecimal("100"), new BigDecimal("150.50")),
                trx(new BigDecimal("100"), new BigDecimal("150.51"))
            ));
        }

        @Test void big_decimal_equals_rechaza_escala_diferente() {
            // B-ORD-11: prueba que ilustra POR QUÉ compareTo es correcto
            // .equals() devuelve false para 1.0 vs 1.00
            BigDecimal a = new BigDecimal("1.0");
            BigDecimal b = new BigDecimal("1.00");
            assertFalse(a.equals(b), "equals rechaza escala diferente");
            assertEquals(0, a.compareTo(b), "compareTo acepta mismo valor diferente escala");
        }
    }

    // ── B-ORD-12: Repositorio HANA — UnsupportedOperationException ───────────────

    @Nested
    class B_ORD_12_RepositorioHANA {

        @Test void repositorio_lanza_unsupported_para_hana() {
            // B-ORD-12: todas las operaciones HANA → UnsupportedOperationException
            var repo = new cl.bcs.ordenjobs.infrastructure.persistence.OrdenJobRepository();
            assertThrows(UnsupportedOperationException.class,
                () -> repo.ejecutarAsignacionAutomatica(1L, 3L));
        }

        @Test void mensaje_excepcion_menciona_hana() {
            // B-ORD-12: el mensaje cita la tabla/SP de HANA
            var repo = new cl.bcs.ordenjobs.infrastructure.persistence.OrdenJobRepository();
            var ex = assertThrows(UnsupportedOperationException.class,
                () -> repo.controlMercadoFix(1L, 3L));
            assertTrue(ex.getMessage().contains("HANA"));
        }
    }
}
