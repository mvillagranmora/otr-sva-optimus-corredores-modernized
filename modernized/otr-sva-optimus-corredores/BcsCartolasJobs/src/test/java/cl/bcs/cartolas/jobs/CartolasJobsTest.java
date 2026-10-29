package cl.bcs.cartolas.jobs;

import cl.bcs.cartolas.jobs.domain.CartolasJobRequest;
import cl.bcs.cartolas.jobs.domain.PasosEjecucion;
import cl.bcs.cartolas.jobs.domain.TipoProceso;
import cl.bcs.cartolas.jobs.service.CartolasJobService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de caracterización para BcsCartolasJobs.
 *
 * Oracle:
 *   cl.bcs.normativa.cartolas.tools.ProcesoCartolasApp.procesar()
 *   cl.bcs.normativa.cartolas.jobs.BcsJobCartolas
 *   cl.bcs.normativa.cartolas.jobs.BcsScheduledJobCartolas
 *
 * Reglas cubiertas:
 *   B-CAR-01 — Routing de pasos por TipoProceso
 *   B-CAR-02 — Validación: nómina ya certificada → IllegalStateException
 *   B-CAR-08 — Descripción de flujo por TipoProceso
 *   B-CAR-09 — CartolasJobRequest: validaciones de constructor
 *
 * Tests de integración HANA: @Disabled("requires HANA")
 *
 * Equivalencia ejecutada: 40+ casos.
 *
 * CANARY: cambiar TipoProceso.NORMAL para que no poblarNomina → B-CAR-01 falla.
 */
@DisplayName("BcsCartolasJobs — Characterization Tests")
class CartolasJobsTest {

    // ── B-CAR-01: Routing de pasos por TipoProceso ───────────────────────────

    @Nested
    @DisplayName("B-CAR-01: PasosEjecucion por TipoProceso")
    class B_CAR_01_PasosEjecucion {

        @Test
        @DisplayName("CAR-01-01 [B-CAR-01]: PROGRAMADO → solo guardarParametros")
        void programado_solo_guarda_parametros() {
            // B-CAR-01: ProcesoCartolasApp.procesar() — rama PROGRAMADO
            PasosEjecucion pasos = PasosEjecucion.paraTipoProceso(TipoProceso.PROGRAMADO);

            assertTrue(pasos.guardarParametros(), "PROGRAMADO debe guardar parámetros");
            assertFalse(pasos.poblarNomina(),     "PROGRAMADO NO debe poblar nómina");
            assertFalse(pasos.cargarDatos(),      "PROGRAMADO NO debe cargar datos");
            assertFalse(pasos.emitir(),           "PROGRAMADO NO debe emitir");
        }

        @Test
        @DisplayName("CAR-01-02 [B-CAR-01]: NORMAL → flujo completo (4 pasos)")
        void normal_flujo_completo() {
            // B-CAR-01: ProcesoCartolasApp.procesar() — rama NORMAL — CANARY
            PasosEjecucion pasos = PasosEjecucion.paraTipoProceso(TipoProceso.NORMAL);

            assertTrue(pasos.guardarParametros(), "NORMAL debe guardar parámetros");
            assertTrue(pasos.poblarNomina(),      "NORMAL debe poblar nómina");
            assertTrue(pasos.cargarDatos(),       "NORMAL debe cargar datos");
            assertTrue(pasos.emitir(),            "NORMAL debe emitir");
        }

        @Test
        @DisplayName("CAR-01-03 [B-CAR-01]: RECURRENTE → igual que NORMAL (4 pasos)")
        void recurrente_igual_que_normal() {
            // B-CAR-01: ProcesoCartolasApp.procesar() — rama RECURRENTE
            PasosEjecucion pasos = PasosEjecucion.paraTipoProceso(TipoProceso.RECURRENTE);

            assertTrue(pasos.guardarParametros(), "RECURRENTE debe guardar parámetros");
            assertTrue(pasos.poblarNomina(),      "RECURRENTE debe poblar nómina");
            assertTrue(pasos.cargarDatos(),       "RECURRENTE debe cargar datos");
            assertTrue(pasos.emitir(),            "RECURRENTE debe emitir");
        }

        @Test
        @DisplayName("CAR-01-04 [B-CAR-01]: REPROCESO → carga datos + emite (2 pasos)")
        void reproceso_carga_y_emite() {
            // B-CAR-01: ProcesoCartolasApp.procesar() — rama REPROCESO
            PasosEjecucion pasos = PasosEjecucion.paraTipoProceso(TipoProceso.REPROCESO);

            assertFalse(pasos.guardarParametros(), "REPROCESO NO debe guardar parámetros");
            assertFalse(pasos.poblarNomina(),      "REPROCESO NO debe poblar nómina");
            assertTrue(pasos.cargarDatos(),        "REPROCESO debe cargar datos");
            assertTrue(pasos.emitir(),             "REPROCESO debe emitir");
        }

        @Test
        @DisplayName("CAR-01-05 [B-CAR-01]: REEMPAQUETA → solo emite (1 paso)")
        void reempaqueta_solo_emite() {
            // B-CAR-01: ProcesoCartolasApp.procesar() — rama REEMPAQUETA
            PasosEjecucion pasos = PasosEjecucion.paraTipoProceso(TipoProceso.REEMPAQUETA);

            assertFalse(pasos.guardarParametros(), "REEMPAQUETA NO debe guardar parámetros");
            assertFalse(pasos.poblarNomina(),      "REEMPAQUETA NO debe poblar nómina");
            assertFalse(pasos.cargarDatos(),       "REEMPAQUETA NO debe cargar datos");
            assertTrue(pasos.emitir(),             "REEMPAQUETA debe emitir");
        }

        @Test
        @DisplayName("CAR-01-06 [B-CAR-01]: tipo desconocido → IllegalArgumentException")
        void tipo_desconocido_lanza_exception() {
            // B-CAR-01: PasosEjecucion debe rechazar tipos no reconocidos
            assertThrows(IllegalArgumentException.class,
                    () -> PasosEjecucion.paraTipoProceso("DESCONOCIDO"));
        }

        @Test
        @DisplayName("CAR-01-07 [B-CAR-01]: null → NullPointerException")
        void null_tipo_lanza_npe() {
            // B-CAR-01: Objects.requireNonNull
            assertThrows(NullPointerException.class,
                    () -> PasosEjecucion.paraTipoProceso(null));
        }

        @Test
        @DisplayName("CAR-01-08 [B-CAR-01]: contarPasosActivos PROGRAMADO = 1")
        void contar_pasos_programado_es_1() {
            // B-CAR-01
            assertEquals(1, CartolasJobService.contarPasosActivos(TipoProceso.PROGRAMADO));
        }

        @Test
        @DisplayName("CAR-01-09 [B-CAR-01]: contarPasosActivos NORMAL = 4")
        void contar_pasos_normal_es_4() {
            // B-CAR-01 — CANARY: cambiar a 3 → falla
            assertEquals(4, CartolasJobService.contarPasosActivos(TipoProceso.NORMAL));
        }

        @Test
        @DisplayName("CAR-01-10 [B-CAR-01]: contarPasosActivos RECURRENTE = 4")
        void contar_pasos_recurrente_es_4() {
            // B-CAR-01
            assertEquals(4, CartolasJobService.contarPasosActivos(TipoProceso.RECURRENTE));
        }

        @Test
        @DisplayName("CAR-01-11 [B-CAR-01]: contarPasosActivos REPROCESO = 2")
        void contar_pasos_reproceso_es_2() {
            // B-CAR-01
            assertEquals(2, CartolasJobService.contarPasosActivos(TipoProceso.REPROCESO));
        }

        @Test
        @DisplayName("CAR-01-12 [B-CAR-01]: contarPasosActivos REEMPAQUETA = 1")
        void contar_pasos_reempaqueta_es_1() {
            // B-CAR-01
            assertEquals(1, CartolasJobService.contarPasosActivos(TipoProceso.REEMPAQUETA));
        }
    }

    // ── B-CAR-01: Constantes de TipoProceso ─────────────────────────────────

    @Nested
    @DisplayName("B-CAR-01: Constantes TipoProceso")
    class B_CAR_01_Constantes {

        @Test
        @DisplayName("CAR-CON-01: TipoProceso.PROGRAMADO = 'PROGRAMADO'")
        void programado_constante_valor() {
            // B-CAR-01: valor exacto usado en HANA y routing
            assertEquals("PROGRAMADO", TipoProceso.PROGRAMADO);
        }

        @Test
        @DisplayName("CAR-CON-02: TipoProceso.NORMAL = 'NORMAL'")
        void normal_constante_valor() {
            assertEquals("NORMAL", TipoProceso.NORMAL);
        }

        @Test
        @DisplayName("CAR-CON-03: TipoProceso.RECURRENTE = 'RECURRENTE'")
        void recurrente_constante_valor() {
            assertEquals("RECURRENTE", TipoProceso.RECURRENTE);
        }

        @Test
        @DisplayName("CAR-CON-04: TipoProceso.REPROCESO = 'REPROCESO'")
        void reproceso_constante_valor() {
            assertEquals("REPROCESO", TipoProceso.REPROCESO);
        }

        @Test
        @DisplayName("CAR-CON-05: TipoProceso.REEMPAQUETA = 'REEMPAQUETA'")
        void reempaqueta_constante_valor() {
            assertEquals("REEMPAQUETA", TipoProceso.REEMPAQUETA);
        }
    }

    // ── B-CAR-02: Validación nómina certificada ──────────────────────────────

    @Nested
    @DisplayName("B-CAR-02: Validación nómina certificada")
    class B_CAR_02_NominaCertificada {

        @Test
        @DisplayName("CAR-02-01 [B-CAR-02]: nómina certificada → IllegalStateException")
        void nomina_certificada_lanza_excepcion() {
            // B-CAR-02: Oracle ProcesoCartolasApp.procesar() — NominaException transformada
            assertThrows(IllegalStateException.class,
                    () -> CartolasJobService.validarNominaNoEstaCertificada(true, 42L));
        }

        @Test
        @DisplayName("CAR-02-02 [B-CAR-02]: nómina no certificada → sin excepción")
        void nomina_no_certificada_no_lanza_excepcion() {
            // B-CAR-02
            assertDoesNotThrow(
                    () -> CartolasJobService.validarNominaNoEstaCertificada(false, 42L));
        }

        @Test
        @DisplayName("CAR-02-03 [B-CAR-02]: mensaje incluye jobId")
        void mensaje_incluye_job_id() {
            // B-CAR-02: NominaException legacy tenía "id="+nomina.getId()
            var ex = assertThrows(IllegalStateException.class,
                    () -> CartolasJobService.validarNominaNoEstaCertificada(true, 999L));
            assertTrue(ex.getMessage().contains("999"),
                    "Mensaje debe incluir el jobId");
        }

        @Test
        @DisplayName("CAR-02-04 [B-CAR-02]: mensaje menciona 'certificada'")
        void mensaje_menciona_certificada() {
            // B-CAR-02
            var ex = assertThrows(IllegalStateException.class,
                    () -> CartolasJobService.validarNominaNoEstaCertificada(true, 1L));
            assertTrue(ex.getMessage().toLowerCase().contains("certif"),
                    "Mensaje debe mencionar 'certificada'");
        }
    }

    // ── B-CAR-08: describirFlujo ─────────────────────────────────────────────

    @Nested
    @DisplayName("B-CAR-08: describirFlujo")
    class B_CAR_08_DescribirFlujo {

        @Test
        @DisplayName("CAR-08-01 [B-CAR-08]: PROGRAMADO → contiene 'PARAMETROS'")
        void flujo_programado_contiene_parametros() {
            // B-CAR-08
            String flujo = CartolasJobService.describirFlujo(TipoProceso.PROGRAMADO);
            assertTrue(flujo.contains("PARAMETROS"));
            assertFalse(flujo.contains("EMISION"));
        }

        @Test
        @DisplayName("CAR-08-02 [B-CAR-08]: NORMAL → contiene todos los pasos")
        void flujo_normal_contiene_todos() {
            // B-CAR-08
            String flujo = CartolasJobService.describirFlujo(TipoProceso.NORMAL);
            assertTrue(flujo.contains("PARAMETROS"));
            assertTrue(flujo.contains("POBLAR_NOMINA"));
            assertTrue(flujo.contains("CARGA_DATOS"));
            assertTrue(flujo.contains("EMISION"));
        }

        @Test
        @DisplayName("CAR-08-03 [B-CAR-08]: REEMPAQUETA → solo 'EMISION'")
        void flujo_reempaqueta_solo_emision() {
            // B-CAR-08
            String flujo = CartolasJobService.describirFlujo(TipoProceso.REEMPAQUETA);
            assertEquals("EMISION", flujo);
        }

        @Test
        @DisplayName("CAR-08-04 [B-CAR-08]: REPROCESO → CARGA_DATOS y EMISION")
        void flujo_reproceso_carga_y_emision() {
            // B-CAR-08
            String flujo = CartolasJobService.describirFlujo(TipoProceso.REPROCESO);
            assertTrue(flujo.contains("CARGA_DATOS"));
            assertTrue(flujo.contains("EMISION"));
            assertFalse(flujo.contains("PARAMETROS"));
            assertFalse(flujo.contains("POBLAR_NOMINA"));
        }
    }

    // ── B-CAR-09: CartolasJobRequest validaciones ────────────────────────────

    @Nested
    @DisplayName("B-CAR-09: CartolasJobRequest validaciones")
    class B_CAR_09_CartolasJobRequest {

        @Test
        @DisplayName("CAR-09-01 [B-CAR-09]: tipoProceso null → NullPointerException")
        void tipo_proceso_null_lanza_npe() {
            // B-CAR-09
            assertThrows(NullPointerException.class,
                    () -> new CartolasJobRequest(1L, 1L, null));
        }

        @Test
        @DisplayName("CAR-09-02 [B-CAR-09]: tipoProceso vacío → IllegalArgumentException")
        void tipo_proceso_vacio_lanza_iae() {
            // B-CAR-09
            assertThrows(IllegalArgumentException.class,
                    () -> new CartolasJobRequest(1L, 1L, ""));
        }

        @Test
        @DisplayName("CAR-09-03 [B-CAR-09]: idEmpresa cero → IllegalArgumentException")
        void id_empresa_cero_lanza_iae() {
            // B-CAR-09
            assertThrows(IllegalArgumentException.class,
                    () -> new CartolasJobRequest(1L, 0L, TipoProceso.NORMAL));
        }

        @Test
        @DisplayName("CAR-09-04 [B-CAR-09]: idEmpresa negativo → IllegalArgumentException")
        void id_empresa_negativo_lanza_iae() {
            // B-CAR-09
            assertThrows(IllegalArgumentException.class,
                    () -> new CartolasJobRequest(1L, -1L, TipoProceso.NORMAL));
        }

        @Test
        @DisplayName("CAR-09-05 [B-CAR-09]: request válido → sin excepción")
        void request_valido_sin_excepcion() {
            // B-CAR-09
            assertDoesNotThrow(() -> new CartolasJobRequest(0L, 1L, TipoProceso.NORMAL));
        }

        @Test
        @DisplayName("CAR-09-06 [B-CAR-09]: jobId puede ser 0 (llamada directa)")
        void job_id_puede_ser_cero() {
            // B-CAR-09: jobId=0 es válido (caso ProcesoCartolasApp.main())
            var req = new CartolasJobRequest(0L, 1L, TipoProceso.NORMAL);
            assertEquals(0L, req.jobId());
        }

        @Test
        @DisplayName("CAR-09-07 [B-CAR-09]: valores accesibles tras construcción")
        void valores_accesibles_tras_construccion() {
            // B-CAR-09
            var req = new CartolasJobRequest(42L, 5L, TipoProceso.REPROCESO);
            assertEquals(42L, req.jobId());
            assertEquals(5L, req.idEmpresa());
            assertEquals(TipoProceso.REPROCESO, req.tipoProceso());
        }
    }

    // ── Integración HANA — deshabilitados ───────────────────────────────────

    @Test
    @Disabled("requires HANA: BcsScheduledJobCartolas — ProcesoProgramacionCartolas")
    @DisplayName("CAR-INT-01: Proceso programado verifica pendientes en HANA")
    void proceso_programado_verifica_pendientes_en_hana() {
        // B-CAR-07:
        // Given: HANA con procesos programados pendientes para idEmpresa=1
        // When: ProgramacionCartolasService.procesarPendientes(1L)
        // Then: se ejecutan los procesos pendientes
        //       HANA_URL, HANA_USER, HANA_PASSWORD desde variables de entorno
    }

    @Test
    @Disabled("requires HANA: BcsJobCartolas — ProcesoCartolasApp.procesar()")
    @DisplayName("CAR-INT-02: Proceso NORMAL ejecuta flujo completo en HANA")
    void proceso_normal_flujo_completo_en_hana() {
        // B-CAR-01:
        // Given: HANA con nómina activa no certificada
        //        request = CartolasJobRequest(jobId=1, idEmpresa=1, tipoProceso="NORMAL")
        // When: CartolasJobService.ejecutar(request)
        // Then: nómina queda cerrada en estado OK
        //       HANA_URL, HANA_USER, HANA_PASSWORD desde variables de entorno
    }

    @Test
    @Disabled("requires HANA: ProcesoCartolasApp — nómina certificada")
    @DisplayName("CAR-INT-03: Proceso con nómina certificada → IllegalStateException")
    void proceso_nomina_certificada_lanza_excepcion_en_hana() {
        // B-CAR-02:
        // Given: HANA con nómina certificada para jobId dado
        // When: CartolasJobService.ejecutar(request)
        // Then: throws IllegalStateException ("La nomina ya esta certificada")
    }
}
