package cl.bcs.normativa;

import cl.bcs.normativa.domain.Campo;
import cl.bcs.normativa.service.DataParserException;
import cl.bcs.normativa.service.DataParserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Characterization tests for DataParserService.
 *
 * Oracle: legacy cl.bcs.normativa.data.DataParser
 *
 * Business rules pinned:
 *   B-NOR-DP-01: AMOUNT descarta decimales y separadores de miles
 *   B-NOR-DP-02: DECIMAL preserva decimales, elimina separadores de miles
 *   B-NOR-DP-03: QTY descarta decimales (Long)
 *   B-NOR-DP-04: ID es Long estricto (sin separadores)
 *   B-NOR-DP-05: Simbolo decimal invalido lanza DataParserException
 *   B-NOR-DP-06: Campo isSumable para AMOUNT, DECIMAL, QTY
 */
@DisplayName("DataParserService — business rules characterization")
class DataParserServiceTest {

    private final DataParserService parser = new DataParserService();

    private Campo campoTipo(String tipo) {
        Campo c = new Campo();
        c.setNombre("test");
        c.setTipoDato(tipo);
        return c;
    }

    // =========================================================================
    // B-NOR-DP-01: AMOUNT
    // =========================================================================
    @Nested
    @DisplayName("B-NOR-DP-01 AMOUNT")
    class Amount {
        @Test
        void amount_entero_sinDecimales() throws Exception {
            var result = parser.parse(campoTipo("AMOUNT"), "1234");
            assertThat(result).isEqualTo(new BigDecimal("1234"));
        }

        @Test
        void amount_conDecimales_descartaDecimales() throws Exception {
            // B-NOR-DP-01: AMOUNT descarta decimales
            var result = parser.parse(campoTipo("AMOUNT"), "1234.56");
            assertThat(result).isEqualTo(new BigDecimal("1234"));
        }

        @Test
        void amount_conSeparadorMiles_eliminaSeparador() throws Exception {
            // B-NOR-DP-01: separadores de miles se eliminan
            var result = parser.parse(campoTipo("AMOUNT"), "1,234");
            assertThat(result).isEqualTo(new BigDecimal("1234"));
        }

        @Test
        void amount_invalido_lanzaException() {
            assertThatThrownBy(() -> parser.parse(campoTipo("AMOUNT"), "abc"))
                    .isInstanceOf(DataParserException.class);
        }
    }

    // =========================================================================
    // B-NOR-DP-02: DECIMAL
    // =========================================================================
    @Nested
    @DisplayName("B-NOR-DP-02 DECIMAL")
    class Decimal {
        @Test
        void decimal_preservaDecimales() throws Exception {
            var result = parser.parse(campoTipo("DECIMAL"), "1234.56");
            assertThat(result).isEqualTo(new BigDecimal("1234.56"));
        }

        @Test
        void decimal_sinDecimales() throws Exception {
            var result = parser.parse(campoTipo("DECIMAL"), "1000");
            assertThat(result).isEqualTo(new BigDecimal("1000"));
        }
    }

    // =========================================================================
    // B-NOR-DP-03: QTY
    // =========================================================================
    @Nested
    @DisplayName("B-NOR-DP-03 QTY")
    class Qty {
        @Test
        void qty_entero() throws Exception {
            var result = parser.parse(campoTipo("QTY"), "500");
            assertThat(result).isEqualTo(500L);
        }

        @Test
        void qty_conDecimales_descartaDecimales() throws Exception {
            // B-NOR-DP-03: QTY descarta decimales al igual que AMOUNT
            var result = parser.parse(campoTipo("QTY"), "500.99");
            assertThat(result).isEqualTo(500L);
        }
    }

    // =========================================================================
    // B-NOR-DP-04: ID
    // =========================================================================
    @Nested
    @DisplayName("B-NOR-DP-04 ID")
    class Id {
        @Test
        void id_long_valido() throws Exception {
            var result = parser.parse(campoTipo("ID"), "42");
            assertThat(result).isEqualTo(42L);
        }

        @Test
        void idTipo_conPrefijo_tambienParseaComoId() throws Exception {
            // B-NOR-DP-04: tipo "ID_USUARIO" tambien aplica regla ID
            var result = parser.parse(campoTipo("ID_USUARIO"), "99");
            assertThat(result).isEqualTo(99L);
        }
    }

    // =========================================================================
    // B-NOR-DP-05: Simbolo decimal invalido
    // =========================================================================
    @Test
    @DisplayName("B-NOR-DP-05: simbolo decimal null lanza exception")
    void constructor_simboloNull_lanzaException() {
        assertThatThrownBy(() -> new DataParserService(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("B-NOR-DP-05: simbolo decimal invalido lanza DataParserException")
    void constructor_simboloInvalido_lanzaException() {
        assertThatThrownBy(() -> new DataParserService(";"))
                .isInstanceOf(DataParserException.class)
                .hasMessageContaining("invalido");
    }

    // =========================================================================
    // B-NOR-DP-06: isSumable
    // =========================================================================
    @Nested
    @DisplayName("B-NOR-DP-06 isSumable")
    class Sumable {
        @Test
        void amount_esSumable() {
            assertThat(campoTipo("AMOUNT").isSumable()).isTrue();
        }

        @Test
        void decimal_esSumable() {
            assertThat(campoTipo("DECIMAL").isSumable()).isTrue();
        }

        @Test
        void qty_esSumable() {
            assertThat(campoTipo("QTY").isSumable()).isTrue();
        }

        @Test
        void string_noEsSumable() {
            assertThat(campoTipo("STRING").isSumable()).isFalse();
        }

        @Test
        void date_noEsSumable() {
            assertThat(campoTipo("DATE").isSumable()).isFalse();
        }
    }

    // =========================================================================
    // Null input
    // =========================================================================
    @Test
    @DisplayName("null value returns null without exception")
    void nullValue_retornaNull() throws Exception {
        var result = parser.parse(campoTipo("AMOUNT"), null);
        assertThat(result).isNull();
    }

    // =========================================================================
    // Static helpers
    // =========================================================================
    @Test
    @DisplayName("normalizeNumber con coma como decimal")
    void normalizeNumber_comaDecimal() {
        // Punto es separador de miles, coma es decimal -> resultado con punto decimal
        String result = DataParserService.normalizeNumber("1.234,56", ",");
        assertThat(result).isEqualTo("1234.56");
    }

    @Test
    @DisplayName("discardDecimals descarta parte decimal")
    void discardDecimals_descartaDecimales() {
        String result = DataParserService.discardDecimals("1234.56", ".");
        assertThat(result).isEqualTo("1234");
    }
}
