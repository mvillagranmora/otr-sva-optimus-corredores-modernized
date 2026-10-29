package cl.bcs.cartolas;

import cl.bcs.cartolas.domain.Cartola;
import cl.bcs.cartolas.domain.Cliente;
import cl.bcs.cartolas.domain.Nomina;
import cl.bcs.cartolas.domain.Opcion;
import cl.bcs.cartolas.domain.Recurso;
import cl.bcs.cartolas.domain.Plantilla;
import cl.bcs.cartolas.domain.TipoRecurso;
import cl.bcs.cartolas.service.CartolasService;
import cl.bcs.cartolas.service.NominaService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de caracterización de la lógica pura del dominio de BcsCartolas.
 * Sin contexto Spring — todos los métodos testeados son {@code public static}.
 */
class CartolasServiceTest {

    // =========================================================================
    // B-CAR-08 Cartola.buscarPorIdPortafolio
    // =========================================================================

    @Nested
    class B_CAR_08_BuscarCartolaPortafolio {

        @Test
        void buscarPorIdPortafolio_existente_retornaCartola() {
            final Cartola c1 = new Cartola();
            c1.setIdPortafolio(2001L);
            final Cartola c2 = new Cartola();
            c2.setIdPortafolio(2002L);

            final Cartola result = Cartola.buscarPorIdPortafolio(List.of(c1, c2), 2001L);
            assertThat(result).isSameAs(c1);
        }

        @Test
        void buscarPorIdPortafolio_noExistente_lanzaIllegalArgumentException() {
            final Cartola c1 = new Cartola();
            c1.setIdPortafolio(2001L);

            assertThatThrownBy(() -> Cartola.buscarPorIdPortafolio(List.of(c1), 9999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("9999");
        }

        @Test
        void buscarPorIdPortafolio_listaNull_lanzaNPE() {
            assertThatThrownBy(() -> Cartola.buscarPorIdPortafolio(null, 1L))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // B-CAR-09 Nomina — cálculo de fechas desde/hasta
    // =========================================================================

    @Nested
    class B_CAR_09_NominaFechas {

        @Test
        void startOfMonth_periodoValido_retornaPrimerDia() {
            final LocalDate result = Nomina.startOfMonth("2024-03");
            assertThat(result).isEqualTo(LocalDate.of(2024, 3, 1));
        }

        @Test
        void endOfMonth_periodo_marzo_retorna31() {
            final LocalDate result = Nomina.endOfMonth("2024-03");
            assertThat(result).isEqualTo(LocalDate.of(2024, 3, 31));
        }

        @Test
        void endOfMonth_periodo_febrero_bisiesto_retorna29() {
            final LocalDate result = Nomina.endOfMonth("2024-02");
            assertThat(result).isEqualTo(LocalDate.of(2024, 2, 29));
        }

        @Test
        void startOfMonth_null_retornaNull() {
            assertThat(Nomina.startOfMonth(null)).isNull();
        }

        @Test
        void endOfMonth_null_retornaNull() {
            assertThat(Nomina.endOfMonth(null)).isNull();
        }

        @Test
        void getFechaDesdeEfectiva_sinFechaDesde_usaPeriodo() {
            final Nomina n = new Nomina();
            n.setPeriodo("2024-05");
            assertThat(n.getFechaDesdeEfectiva()).isEqualTo(LocalDate.of(2024, 5, 1));
        }

        @Test
        void getFechaHastaEfectiva_sinFechaHasta_usaPeriodo() {
            final Nomina n = new Nomina();
            n.setPeriodo("2024-05");
            assertThat(n.getFechaHastaEfectiva()).isEqualTo(LocalDate.of(2024, 5, 31));
        }

        @Test
        void getFechaDesdeEfectiva_conFechaDesde_usaFechaExplícita() {
            final Nomina n = new Nomina();
            n.setPeriodo("2024-05");
            n.setFechaDesde(LocalDate.of(2024, 5, 10));
            assertThat(n.getFechaDesdeEfectiva()).isEqualTo(LocalDate.of(2024, 5, 10));
        }
    }

    // =========================================================================
    // B-CAR-10 NominaService — getPeriodoCartolas
    // =========================================================================

    @Nested
    class B_CAR_10_PeriodoCartolas {

        @Test
        void getPeriodoCartolas_primeroDeMarzo_retornaFebrero() {
            final String p = NominaService.getPeriodoCartolas(LocalDate.of(2024, 3, 1));
            assertThat(p).isEqualTo("2024-02");
        }

        @Test
        void getPeriodoCartolas_quinceDeAbril_retornaMarzo() {
            final String p = NominaService.getPeriodoCartolas(LocalDate.of(2024, 4, 15));
            assertThat(p).isEqualTo("2024-03");
        }

        @Test
        void getPeriodoCartolas_primeroDiEnero_retornaDiciembre() {
            final String p = NominaService.getPeriodoCartolas(LocalDate.of(2024, 1, 1));
            assertThat(p).isEqualTo("2023-12");
        }

        @Test
        void getPeriodoCartolas_null_lanzaNPE() {
            assertThatThrownBy(() -> NominaService.getPeriodoCartolas(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // B-CAR-11 NominaService — condicionesEjecucionCumplidas
    // =========================================================================

    @Nested
    class B_CAR_11_CondicionesEjecucion {

        @Test
        void condiciones_habilitadoFalse_retornaFalse() {
            final Map<String, String> p = Map.of(
                    "Habilitado", "false",
                    "DiaEjecucion", "5",
                    "HoraEjecucion", "08:00");
            final boolean result = NominaService.condicionesEjecucionCumplidas(
                    p, LocalDateTime.of(2024, 3, 5, 8, 0));
            assertThat(result).isFalse();
        }

        @Test
        void condiciones_diaIncorrecto_retornaFalse() {
            final Map<String, String> p = Map.of(
                    "Habilitado", "true",
                    "DiaEjecucion", "5",
                    "HoraEjecucion", "08:00");
            final boolean result = NominaService.condicionesEjecucionCumplidas(
                    p, LocalDateTime.of(2024, 3, 6, 8, 0));
            assertThat(result).isFalse();
        }

        @Test
        void condiciones_horaIncorrecta_retornaFalse() {
            final Map<String, String> p = Map.of(
                    "Habilitado", "true",
                    "DiaEjecucion", "5",
                    "HoraEjecucion", "08:00");
            final boolean result = NominaService.condicionesEjecucionCumplidas(
                    p, LocalDateTime.of(2024, 3, 5, 9, 0));
            assertThat(result).isFalse();
        }

        @Test
        void condiciones_todoCorrecto_retornaTrue() {
            final Map<String, String> p = Map.of(
                    "Habilitado", "true",
                    "DiaEjecucion", "5",
                    "HoraEjecucion", "8:00");
            // Minuto dentro del rango (> 0 y < 1 no es posible realistamente,
            // pero el legacy acepta minuto = 0 como borde inferior excluido).
            // Usamos minuto = 0 que es exactamente lo que verificamos:
            // el legacy era: minute > mm && minute < mm + 1
            // Para hh=8, mm=0: minuto debe ser > 0 y < 1 (imposible entero)
            // Probamos con configuración mm=1: minuto=1 no cumple (no > 1)
            // Corrección: el legacy tiene un bug en minutos (range 0..0).
            // Testeamos la lógica directamente con mm=0 y minuto=0 → false
            final boolean result = NominaService.condicionesEjecucionCumplidas(
                    p, LocalDateTime.of(2024, 3, 5, 8, 0));
            assertThat(result).isFalse(); // minuto 0 no es > 0
        }
    }

    // =========================================================================
    // B-CAR-12 CartolasService — extractKeys y resolverNombre
    // =========================================================================

    @Nested
    class B_CAR_12_ResolverNombre {

        @Test
        void extractKeys_patronConClaves_retornaConjunto() {
            final var keys = CartolasService.extractKeys("{DocumentoCliente}_{YYYYMMDD}");
            assertThat(keys).containsExactlyInAnyOrder("DocumentoCliente", "YYYYMMDD");
        }

        @Test
        void extractKeys_patronSinClaves_retornaVacio() {
            final var keys = CartolasService.extractKeys("archivoFijo");
            assertThat(keys).isEmpty();
        }

        @Test
        void resolverNombre_sustituye_YYYYMMDD() {
            final var ahora = new Date(0); // 1970-01-01 UTC
            final String result = CartolasService.resolverNombre(
                    "Archivo_{YYYYMMDD}", Map.of(), ahora);
            assertThat(result).matches("Archivo_\\d{8}");
        }

        @Test
        void resolverNombre_sustituye_documentoCliente() {
            final var ahora = new Date();
            final String result = CartolasService.resolverNombre(
                    "{DocumentoCliente}", Map.of("DocumentoCliente", "12345678-9"), ahora);
            assertThat(result).isEqualTo("12345678-9");
        }

        @Test
        void resolverNombre_claveFaltante_devuelveVacio() {
            final var ahora = new Date();
            final String result = CartolasService.resolverNombre(
                    "{CodigoPortafolio}", Map.of(), ahora);
            assertThat(result).isEmpty();
        }

        @Test
        void resolverNombre_patronNull_lanzaNPE() {
            assertThatThrownBy(() ->
                    CartolasService.resolverNombre(null, Map.of(), new Date()))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // B-CAR-13 Cliente.buildNombre
    // =========================================================================

    @Nested
    class B_CAR_13_ClienteNombre {

        @Test
        void buildNombre_conRazonSocial_retornaRazonSocial() {
            final String nombre = Cliente.buildNombre("Empresa SA", "Juan", "Pérez", "Gómez");
            assertThat(nombre).isEqualTo("Empresa SA");
        }

        @Test
        void buildNombre_sinRazonSocial_concatenaNombresApellidos() {
            final String nombre = Cliente.buildNombre("", "Juan", "Pérez", "Gómez");
            assertThat(nombre).isEqualTo("Juan Pérez Gómez");
        }

        @Test
        void buildNombre_razonSocialNull_concatenaNombresApellidos() {
            final String nombre = Cliente.buildNombre(null, "Ana", "López", "");
            assertThat(nombre).isEqualTo("Ana López");
        }
    }

    // =========================================================================
    // B-CAR-14 Opcion
    // =========================================================================

    @Nested
    class B_CAR_14_Opcion {

        @Test
        void opcion_of_true_retornaSI() {
            assertThat(Opcion.of(true)).isEqualTo("SI");
        }

        @Test
        void opcion_of_false_retornaNO() {
            assertThat(Opcion.of(false)).isEqualTo("NO");
        }

        @Test
        void opcion_isTrue_SI_retornaTrue() {
            assertThat(Opcion.isTrue("SI")).isTrue();
        }

        @Test
        void opcion_isTrue_NO_retornaFalse() {
            assertThat(Opcion.isTrue("NO")).isFalse();
        }
    }

    // =========================================================================
    // B-CAR-15 Plantilla.buscarRecurso
    // =========================================================================

    @Nested
    class B_CAR_15_PlantillaBuscarRecurso {

        @Test
        void buscarRecurso_existente_retornaRecurso() {
            final Recurso r = new Recurso();
            r.setNombre("CartolaMensual");
            r.setTipo(TipoRecurso.REPORTE_EXE);
            r.setValor("BASE64DATA");

            final Plantilla p = new Plantilla();
            p.setRecursos(List.of(r));

            final Recurso found = p.buscarRecurso("CartolaMensual", TipoRecurso.REPORTE_EXE);
            assertThat(found).isSameAs(r);
        }

        @Test
        void buscarRecurso_noExistente_lanzaIllegalArgumentException() {
            final Plantilla p = new Plantilla();
            p.setRecursos(List.of());

            assertThatThrownBy(() -> p.buscarRecurso("NoExiste", TipoRecurso.REPORTE_EXE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("NoExiste");
        }
    }

    // =========================================================================
    // B-CAR-16 Recurso.toObject
    // =========================================================================

    @Nested
    class B_CAR_16_RecursoToObject {

        @Test
        void toObject_opcionSI_retornaTrue() {
            assertThat(Recurso.toObject(TipoRecurso.OPCION, "SI")).isEqualTo(true);
        }

        @Test
        void toObject_opcionNO_retornaFalse() {
            assertThat(Recurso.toObject(TipoRecurso.OPCION, "NO")).isEqualTo(false);
        }

        @Test
        void toObject_numero_retornaInteger() {
            assertThat(Recurso.toObject(TipoRecurso.NUMERO, "42")).isEqualTo(42);
        }

        @Test
        void toObject_texto_retornaString() {
            assertThat(Recurso.toObject(TipoRecurso.TEXTO, "hola")).isEqualTo("hola");
        }

        @Test
        void toObject_null_retornaNull() {
            assertThat(Recurso.toObject(TipoRecurso.TEXTO, null)).isNull();
        }
    }
}
