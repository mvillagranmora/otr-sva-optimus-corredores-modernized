package cl.bcs.joborders;

import cl.bcs.joborders.domain.Asignacion;
import cl.bcs.joborders.domain.Orden;
import cl.bcs.joborders.domain.Transaccion;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cl.bcs.joborders.service.AsignacionCronologicaService.asignar;
import static cl.bcs.joborders.service.AsignacionCronologicaService.compararFechasConDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for BcsJobOrders — oracle de equivalencia.
 *
 * Fuente: BcsJobOrderChronologicAllocation.java (transaction(), compararFechasConDate())
 * Canary: cambiar '>=' a '>' en precio tipo L → B-JOB-03 falla.
 * Equivalencia ejecutada: 44 casos.
 */
class AsignacionCronologicaServiceTest {

    // ── Builders ──

    static Orden orden(String tipoOp, String tipoPrecio, double precio,
                       double cantDisp, String especificaBolsa) {
        return new Orden(1L, 1, "RV", "ACC", "BCS", tipoOp, "CN",
            precio, cantDisp, 0, 0, "D", "2024-01-01 09:00:00", "2024-01-01 09:00:00",
            1, tipoPrecio, especificaBolsa, 100);
    }

    static Transaccion trx(String tipoOp, double precio, double cantDisp) {
        return new Transaccion(1, "RV", "ACC", "BCS", tipoOp, "CN",
            precio, cantDisp, cantDisp, 0, 0,
            "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
    }

    // ── B-JOB-01: compararFechasConDate ──────────────────────────────────────

    @Nested
    class B_JOB_01_CompararFechas {

        @Test void orden_antes_de_trx_retorna_true() {
            // B-JOB-01
            assertTrue(compararFechasConDate(
                "2024-01-01 09:00:00", "2024-01-02 09:00:00"));
        }

        @Test void orden_despues_de_trx_retorna_false() {
            // B-JOB-01
            assertFalse(compararFechasConDate(
                "2024-01-02 09:00:00", "2024-01-01 09:00:00"));
        }

        @Test void orden_igual_a_trx_retorna_true() {
            // B-JOB-01: iguales → true (else branch legacy)
            assertTrue(compararFechasConDate(
                "2024-01-01 09:00:00", "2024-01-01 09:00:00"));
        }

        @Test void formato_invalido_retorna_false() {
            // B-JOB-01: excepción de parseo → false
            assertFalse(compararFechasConDate("not-a-date", "2024-01-01 09:00:00"));
        }

        @Test void solo_hora_diferente_anterior() {
            // B-JOB-01: componente hora también cuenta
            assertTrue(compararFechasConDate(
                "2024-01-01 08:00:00", "2024-01-01 09:00:00"));
        }

        @Test void solo_hora_diferente_posterior() {
            // B-JOB-01
            assertFalse(compararFechasConDate(
                "2024-01-01 10:00:00", "2024-01-01 09:00:00"));
        }
    }

    // ── B-JOB-02: 6-dimensional matching ─────────────────────────────────────

    @Nested
    class B_JOB_02_MatchingCriteria {

        @Test void match_completo_genera_asignacion() {
            // B-JOB-02
            assertEquals(1, asignar(List.of(orden("COM", "M", 100, 50, "S")),
                List.of(trx("COM", 100, 50))).size());
        }

        @Test void clasificacion_diferente_no_asigna() {
            // B-JOB-02: clasificacionTipoInstrumento mismatch
            var trxDif = new Transaccion(1, "RF", "ACC", "BCS", "COM", "CN",
                100, 50, 50, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            assertEquals(0, asignar(List.of(orden("COM", "M", 100, 50, "S")), List.of(trxDif)).size());
        }

        @Test void tipo_instrumento_diferente_no_asigna() {
            // B-JOB-02
            var trxDif = new Transaccion(1, "RV", "BON", "BCS", "COM", "CN",
                100, 50, 50, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            assertEquals(0, asignar(List.of(orden("COM", "M", 100, 50, "S")), List.of(trxDif)).size());
        }

        @Test void id_instrumento_diferente_no_asigna() {
            // B-JOB-02: idInstrumento int ==
            var trxDif = new Transaccion(1, "RV", "ACC", "BCS", "COM", "CN",
                100, 50, 50, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 999);
            assertEquals(0, asignar(List.of(orden("COM", "M", 100, 50, "S")), List.of(trxDif)).size());
        }

        @Test void bolsa_diferente_con_especifica_S_no_asigna() {
            // B-JOB-10: especificaBolsa "S" + bolsas distintas → no asigna
            var trxDif = new Transaccion(1, "RV", "ACC", "OTR", "COM", "CN",
                100, 50, 50, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            assertEquals(0, asignar(List.of(orden("COM", "M", 100, 50, "S")), List.of(trxDif)).size());
        }

        @Test void condicion_liquidacion_diferente_no_asigna() {
            // B-JOB-02
            var trxDif = new Transaccion(1, "RV", "ACC", "BCS", "COM", "PM",
                100, 50, 50, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            assertEquals(0, asignar(List.of(orden("COM", "M", 100, 50, "S")), List.of(trxDif)).size());
        }

        @Test void tipo_operacion_diferente_no_asigna() {
            // B-JOB-02: COM vs VEN
            var trxDif = new Transaccion(1, "RV", "ACC", "BCS", "VEN", "CN",
                100, 50, 50, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            assertEquals(0, asignar(List.of(orden("COM", "M", 100, 50, "S")), List.of(trxDif)).size());
        }

        @Test void fecha_orden_posterior_a_trx_no_asigna() {
            // B-JOB-01 dentro del loop
            var ord = new Orden(1L, 1, "RV", "ACC", "BCS", "COM", "CN",
                100, 50, 0, 0, "D", "2024-01-02 09:00:00", "2024-01-02 09:00:00",
                1, "M", "S", 100);
            var trxDif = new Transaccion(1, "RV", "ACC", "BCS", "COM", "CN",
                100, 50, 50, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            assertEquals(0, asignar(List.of(ord), List.of(trxDif)).size());
        }

        @Test void clasificacion_case_insensitive_asigna() {
            // B-JOB-02: equalsIgnoreCase
            var trxCi = new Transaccion(1, "rv", "acc", "BCS", "COM", "cn",
                100, 50, 50, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            assertEquals(1, asignar(List.of(orden("COM", "M", 100, 50, "S")), List.of(trxCi)).size());
        }
    }

    // ── B-JOB-10: especificaBolsa reference equality ─────────────────────────

    @Nested
    class B_JOB_10_EspecificaBolsa {

        @Test void especifica_N_literal_acepta_cualquier_bolsa() {
            // B-JOB-10: "N" literal internado → referencia pasa
            var trxOtra = new Transaccion(1, "RV", "ACC", "OTRA", "COM", "CN",
                100, 50, 50, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            assertEquals(1, asignar(List.of(orden("COM", "M", 100, 50, "N")), List.of(trxOtra)).size());
        }

        @Test void especifica_S_requiere_misma_bolsa() {
            // B-JOB-10
            var trxOtra = new Transaccion(1, "RV", "ACC", "OTRA", "COM", "CN",
                100, 50, 50, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            assertEquals(0, asignar(List.of(orden("COM", "M", 100, 50, "S")), List.of(trxOtra)).size());
        }

        @Test void especifica_N_misma_bolsa_tambien_asigna() {
            // B-JOB-10: "N" + misma bolsa (primera condición OR es true)
            assertEquals(1, asignar(List.of(orden("COM", "M", 100, 50, "N")),
                List.of(trx("COM", 100, 50))).size());
        }
    }

    // ── B-JOB-03: COM tipo L ─────────────────────────────────────────────────

    @Nested
    class B_JOB_03_ComTipoL {

        @Test void precio_orden_igual_a_trx_asigna() {
            // B-JOB-03: >= → igual es aceptado (CANARY: si cambia a > este test falla)
            assertEquals(1, asignar(List.of(orden("COM", "L", 100, 50, "S")),
                List.of(trx("COM", 100, 50))).size());
        }

        @Test void precio_orden_mayor_que_trx_asigna() {
            // B-JOB-03
            assertEquals(1, asignar(List.of(orden("COM", "L", 110, 50, "S")),
                List.of(trx("COM", 100, 50))).size());
        }

        @Test void precio_orden_menor_que_trx_no_asigna() {
            // B-JOB-03
            assertEquals(0, asignar(List.of(orden("COM", "L", 90, 50, "S")),
                List.of(trx("COM", 100, 50))).size());
        }

        @Test void precio_cero_en_orden_no_asigna() {
            // B-JOB-03: guard precio > 0
            assertEquals(0, asignar(List.of(orden("COM", "L", 0, 50, "S")),
                List.of(trx("COM", 0, 50))).size());
        }

        @Test void cant_disponible_cero_no_asigna() {
            // B-JOB-03: guard cantDisp > 0
            assertEquals(0, asignar(List.of(orden("COM", "L", 100, 0, "S")),
                List.of(trx("COM", 100, 0))).size());
        }
    }

    // ── B-JOB-04: COM tipo M ─────────────────────────────────────────────────

    @Nested
    class B_JOB_04_ComTipoM {

        @Test void cualquier_precio_asigna_si_positivo() {
            // B-JOB-04: mercado — comparación de precio irrelevante
            assertEquals(1, asignar(List.of(orden("COM", "M", 50, 50, "S")),
                List.of(trx("COM", 200, 50))).size());
        }

        @Test void precio_trx_cero_no_asigna() {
            // B-JOB-04: guard trx.precio > 0
            assertEquals(0, asignar(List.of(orden("COM", "M", 50, 50, "S")),
                List.of(trx("COM", 0, 50))).size());
        }
    }

    // ── B-JOB-05: VEN tipo L ─────────────────────────────────────────────────

    @Nested
    class B_JOB_05_VenTipoL {

        @Test void precio_orden_igual_a_trx_asigna() {
            // B-JOB-05: misma regla que COM tipo L
            assertEquals(1, asignar(List.of(orden("VEN", "L", 100, 50, "S")),
                List.of(trx("VEN", 100, 50))).size());
        }

        @Test void precio_orden_menor_que_trx_no_asigna() {
            // B-JOB-05
            assertEquals(0, asignar(List.of(orden("VEN", "L", 90, 50, "S")),
                List.of(trx("VEN", 100, 50))).size());
        }

        @Test void precio_orden_mayor_asigna() {
            // B-JOB-05
            assertEquals(1, asignar(List.of(orden("VEN", "L", 110, 50, "S")),
                List.of(trx("VEN", 100, 50))).size());
        }
    }

    // ── B-JOB-06: VEN tipo M ─────────────────────────────────────────────────

    @Nested
    class B_JOB_06_VenTipoM {

        @Test void cualquier_precio_asigna() {
            // B-JOB-06
            assertEquals(1, asignar(List.of(orden("VEN", "M", 50, 50, "S")),
                List.of(trx("VEN", 200, 50))).size());
        }
    }

    // ── B-JOB-07 + B-JOB-08: Allocation logic ────────────────────────────────

    @Nested
    class B_JOB_07_08_Allocation {

        @Test void ord_cant_igual_trx_cant_consume_orden_completa() {
            // B-JOB-07: cantDisp iguales → toma lado orden (100)
            var asign = asignar(List.of(orden("COM", "M", 100, 100, "S")),
                List.of(trx("COM", 100, 100)));
            assertEquals(1, asign.size());
            assertEquals(100, asign.get(0).cantidadAsignada());
        }

        @Test void ord_cant_menor_que_trx_cant_consume_orden_completa() {
            // B-JOB-07: ord=50 <= trx=100 → consume 50 de orden
            var asign = asignar(List.of(orden("COM", "M", 100, 50, "S")),
                List.of(trx("COM", 100, 100)));
            assertEquals(1, asign.size());
            assertEquals(50, asign.get(0).cantidadAsignada());
        }

        @Test void ord_cant_mayor_que_trx_cant_consume_transaccion_completa() {
            // B-JOB-08: ord=100 > trx=30 → consume 30 de trx
            var asign = asignar(List.of(orden("COM", "M", 100, 100, "S")),
                List.of(trx("COM", 100, 30)));
            assertEquals(1, asign.size());
            assertEquals(30, asign.get(0).cantidadAsignada());
        }

        @Test void una_orden_multiples_trx_consume_hasta_agotar_orden() {
            // B-JOB-07+08: orden=100 vs dos trx de 60 → primera toma 60, segunda toma 40
            var t1 = new Transaccion(1, "RV", "ACC", "BCS", "COM", "CN",
                100, 60, 60, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            var t2 = new Transaccion(2, "RV", "ACC", "BCS", "COM", "CN",
                100, 60, 60, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            var asign = asignar(List.of(orden("COM", "M", 100, 100, "S")), List.of(t1, t2));
            assertEquals(2, asign.size());
            assertEquals(60, asign.get(0).cantidadAsignada()); // B-JOB-08
            assertEquals(40, asign.get(1).cantidadAsignada()); // B-JOB-07
        }

        @Test void multiples_ordenes_misma_trx_se_asigna_hasta_agotar_trx() {
            // B-JOB-08: dos órdenes de 60 vs una trx de 80 → primera 60, segunda 20
            var o1 = orden("COM", "M", 100, 60, "S");
            var o2 = orden("COM", "M", 100, 60, "S");
            var asign = asignar(List.of(o1, o2), List.of(trx("COM", 100, 80)));
            assertEquals(2, asign.size());
            assertEquals(60, asign.get(0).cantidadAsignada());
            assertEquals(20, asign.get(1).cantidadAsignada());
        }

        @Test void trx_agotada_no_genera_mas_asignaciones() {
            // B-JOB-08: segunda orden no recibe nada si trx ya está agotada
            var o1 = orden("COM", "M", 100, 100, "S");
            var o2 = orden("COM", "M", 100, 100, "S");
            var asign = asignar(List.of(o1, o2), List.of(trx("COM", 100, 50)));
            assertEquals(1, asign.size());
        }
    }

    // ── B-JOB-09: montoTotal int truncation ──────────────────────────────────

    @Nested
    class B_JOB_09_MontoTotal {

        @Test void monto_es_truncado_no_redondeado() {
            // B-JOB-09: (int)((int)50 * 99.9) = (int)(4995.0) = 4995
            var asign = asignar(List.of(orden("COM", "M", 99.9, 50, "S")),
                List.of(trx("COM", 99.9, 50)));
            assertEquals(1, asign.size());
            assertEquals(4995, asign.get(0).montoTotal());
        }

        @Test void monto_entero_exacto_sin_truncamiento() {
            // B-JOB-09: 100 * 50 = 5000
            var asign = asignar(List.of(orden("COM", "M", 100, 50, "S")),
                List.of(trx("COM", 100, 50)));
            assertEquals(5000, asign.get(0).montoTotal());
        }

        @Test void monto_usa_cant_disponible_de_orden_cuando_menor() {
            // B-JOB-09: cdOrd=30 <= cdTrx=50 → monto=(int)(30*precio)
            var asign = asignar(List.of(orden("COM", "M", 100, 30, "S")),
                List.of(trx("COM", 100, 50)));
            assertEquals(3000, asign.get(0).montoTotal());
        }
    }

    // ── B-JOB-11: Guard ──────────────────────────────────────────────────────

    @Nested
    class B_JOB_11_Guard {

        @Test void listas_vacias_no_generan_asignaciones() {
            assertTrue(asignar(List.of(), List.of()).isEmpty());
        }

        @Test void orden_sin_transacciones_no_genera_asignaciones() {
            assertTrue(asignar(List.of(orden("COM", "M", 100, 50, "S")), List.of()).isEmpty());
        }

        @Test void transacciones_sin_ordenes_no_genera_asignaciones() {
            assertTrue(asignar(List.of(), List.of(trx("COM", 100, 50))).isEmpty());
        }
    }

    // ── B-JOB-MISC ───────────────────────────────────────────────────────────

    @Nested
    class B_JOB_Misc {

        @Test void estado_facturacion_es_ANF() {
            // Legacy siempre asigna "ANF"
            var asign = asignar(List.of(orden("COM", "M", 100, 50, "S")),
                List.of(trx("COM", 100, 50)));
            assertEquals("ANF", asign.get(0).idEstadoFacturacion());
        }

        @Test void ids_asignacion_incrementan_secuencialmente() {
            // AtomicInteger comienza en 0 por invocación
            var t1 = trx("COM", 100, 30);
            var t2 = new Transaccion(2, "RV", "ACC", "BCS", "COM", "CN",
                100, 30, 30, 0, 0, "2024-01-01 09:00:00", "2024-01-01 09:00:00", 100);
            var asign = asignar(List.of(orden("COM", "M", 100, 50, "S")), List.of(t1, t2));
            assertEquals(2, asign.size());
            assertEquals(0, asign.get(0).idAsignacionPaso());
            assertEquals(1, asign.get(1).idAsignacionPaso());
        }

        @Test void fecha_asignacion_viene_de_transaccion() {
            // fechaAsignacion = trx.fechaOperacionTransaccion
            var asign = asignar(List.of(orden("COM", "M", 100, 50, "S")),
                List.of(trx("COM", 100, 50)));
            assertEquals("2024-01-01 09:00:00", asign.get(0).fechaAsignacion());
        }
    }
}
