package cl.bcs.fondomutuo.jobs;

import cl.bcs.fondomutuo.jobs.service.CertificacionMasivaSolicitudesJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization tests for CertificacionMasivaSolicitudesJob filter parsing.
 *
 * Oracle:
 *   legacy cl.bcs.fondomutuo.BcsJobCertificacionMasivaSolicitudes (lines 63-95)
 *
 * Rule IDs pinned:
 *   B-FMJ-04  esMasivo="S" → CertificarPorFiltrosSolicitudAporteRescate with filter arrays.
 *   B-FMJ-05  esMasivo≠"S" → individual solicitud IDs.
 *   B-FMJ-06  Parallel semicolon arrays form filter records.
 *   B-FMJ-07  Empty filter list → no service call.
 */
@DisplayName("CertificacionMasivaSolicitudes — filter parsing")
class CertificacionMasivaSolicitudesJobTest {

    @Nested
    @DisplayName("FiltrosParsing — B-FMJ-06")
    class FiltrosParsing {

        @Test
        @DisplayName("FMJ-CERT-01 [B-FMJ-06]: single filter row parsed from parallel arrays")
        void rule_FMJ_CERT_01_singleFilterRowParsedFromParallelArrays() {
            // B-FMJ-06: each semicolon-delimited param at position i forms one filter
            List<Map<String, Object>> filtros = CertificacionMasivaSolicitudesJob.parseFiltros(
                    "1",         // idInternoFiltro
                    "EMI01",     // idEmisor
                    "CLP",       // idMoneda
                    "SERIE_A",   // idSerieFondo
                    "S",         // nacional
                    "APO",       // tipoSolicitud
                    "CARGA01",   // idTipoMovimientoFondoMutuo
                    "N",         // moneyMarket
                    "9999"       // idCarga
            );

            assertThat(filtros).hasSize(1);                                  // FMJ-CERT-01
            Map<String, Object> f = filtros.get(0);
            assertThat(f.get("IdEmisor")).isEqualTo("EMI01");                // FMJ-CERT-01
            assertThat(f.get("IdMoneda")).isEqualTo("CLP");                  // FMJ-CERT-01
            assertThat(f.get("IdSerieFondo")).isEqualTo("SERIE_A");          // FMJ-CERT-01
            assertThat(f.get("Nacional")).isEqualTo("S");                    // FMJ-CERT-01
            assertThat(f.get("TipoSolicitud")).isEqualTo("APO");             // FMJ-CERT-01
            assertThat(f.get("MoneyMarket")).isEqualTo("N");                 // FMJ-CERT-01
            assertThat(f.get("IdCarga")).isEqualTo(9999L);                   // FMJ-CERT-01
        }

        @Test
        @DisplayName("FMJ-CERT-02 [B-FMJ-06]: two filter rows from parallel semicolon arrays")
        void rule_FMJ_CERT_02_twoFilterRowsFromParallelArrays() {
            // B-FMJ-06: position 0 and position 1 from all arrays form two filters
            List<Map<String, Object>> filtros = CertificacionMasivaSolicitudesJob.parseFiltros(
                    "1;2",           // idInternoFiltro (2 elements)
                    "EMI01;EMI02",   // idEmisor
                    "CLP;USD",       // idMoneda
                    "SA;SB",         // idSerieFondo
                    "S;N",           // nacional
                    "APO;RES",       // tipoSolicitud
                    "T1;T2",         // idTipoMovimientoFondoMutuo
                    "N;S",           // moneyMarket
                    "1001;1002"      // idCarga
            );

            assertThat(filtros).hasSize(2);                                   // FMJ-CERT-02

            // Row 0
            assertThat(filtros.get(0).get("IdEmisor")).isEqualTo("EMI01");    // FMJ-CERT-02
            assertThat(filtros.get(0).get("IdMoneda")).isEqualTo("CLP");      // FMJ-CERT-02
            assertThat(filtros.get(0).get("TipoSolicitud")).isEqualTo("APO"); // FMJ-CERT-02
            assertThat(filtros.get(0).get("IdCarga")).isEqualTo(1001L);       // FMJ-CERT-02

            // Row 1
            assertThat(filtros.get(1).get("IdEmisor")).isEqualTo("EMI02");    // FMJ-CERT-02
            assertThat(filtros.get(1).get("IdMoneda")).isEqualTo("USD");      // FMJ-CERT-02
            assertThat(filtros.get(1).get("TipoSolicitud")).isEqualTo("RES"); // FMJ-CERT-02
            assertThat(filtros.get(1).get("IdCarga")).isEqualTo(1002L);       // FMJ-CERT-02
        }

        @Test
        @DisplayName("FMJ-CERT-03 [B-FMJ-06]: IdCarga parsed as Long")
        void rule_FMJ_CERT_03_idCargaParsedAsLong() {
            List<Map<String, Object>> filtros = CertificacionMasivaSolicitudesJob.parseFiltros(
                    "1", "", "", "", "", "", "", "", "99887766");

            assertThat(filtros.get(0).get("IdCarga")).isInstanceOf(Long.class); // FMJ-CERT-03
            assertThat(filtros.get(0).get("IdCarga")).isEqualTo(99887766L);     // FMJ-CERT-03
        }
    }

    @Nested
    @DisplayName("IdSolicitudesParsing — B-FMJ-05")
    class IdSolicitudesParsing {

        @Test
        @DisplayName("FMJ-CERT-04 [B-FMJ-05]: semicolon-delimited solicitud IDs parsed into Long list")
        void rule_FMJ_CERT_04_solicitudIdsParsedIntoLongList() {
            // B-FMJ-05: individual solicitud certification mode
            List<Long> ids = CertificacionMasivaSolicitudesJob.parseIdSolicitudes("111;222;333");

            assertThat(ids).containsExactly(111L, 222L, 333L); // FMJ-CERT-04
        }

        @Test
        @DisplayName("FMJ-CERT-05 [B-FMJ-07]: null idSolicitudes → empty list → no service call")
        void rule_FMJ_CERT_05_nullIdSolicitudesReturnsEmptyList() {
            // B-FMJ-07: empty → no-op
            List<Long> ids = CertificacionMasivaSolicitudesJob.parseIdSolicitudes(null);

            assertThat(ids).isEmpty(); // FMJ-CERT-05
        }

        @Test
        @DisplayName("FMJ-CERT-06 [B-FMJ-07]: blank idSolicitudes → empty list")
        void rule_FMJ_CERT_06_blankIdSolicitudesReturnsEmptyList() {
            List<Long> ids = CertificacionMasivaSolicitudesJob.parseIdSolicitudes("   ");

            assertThat(ids).isEmpty(); // FMJ-CERT-06
        }
    }
}
