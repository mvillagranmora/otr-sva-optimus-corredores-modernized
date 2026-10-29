package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.centralizagz.Fx.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Behaviour checks shared by every legacy BcsCentralizacion implementation of WP-F.
 * Each per-type test class calls these with its own {@link Spec.Tipo}; the legacy
 * file:line that each check pins is documented on the calling test method.
 */
final class Casos {

    /** Legacy constructors load every SQL with loadResourceToString and throw IllegalStateException if absent (BcsCentralizacion.java:86-90). */
    static void recursosSql(Spec.Tipo tipo) {
        ClassLoader cl = Casos.class.getClassLoader();
        List<String> faltan = new ArrayList<>();
        for (Spec.Consulta c : tipo.consultas()) {
            if (cl.getResource("sql/" + c.archivo()) == null
                    && cl.getResource("cl/bcs/contabilidad/sql/" + c.archivo()) == null) {
                faltan.add(c.archivo());
            }
        }
        assertThat(faltan).as(tipo.nombre() + ": legacy SQL files missing from the classpath").isEmpty();
    }

    /** Each legacy SQL runs exactly once with exactly the legacy named parameters, in the legacy order. */
    static void consultas(Spec.Tipo tipo) {
        Escenario e = new Escenario();
        try {
            e.ejecutar(tipo, "S");
        } catch (RuntimeException ignored) {
            // query evidence is checked below even if the run fails later
        }
        for (Spec.Consulta c : tipo.consultas()) {
            List<Escenario.SqlCall> calls = e.llamadasA(c.archivo());
            assertThat(calls).as(tipo.nombre() + ": calls to " + c.archivo()).hasSize(1);
            assertThat(calls.get(0).params())
                    .as(tipo.nombre() + ": named parameters of " + c.archivo())
                    .containsExactlyInAnyOrderEntriesOf(c.params());
        }
        List<String> orden = e.llamadas.stream()
                .map(Escenario.SqlCall::recurso)
                .filter(r -> tipo.consultas().stream().anyMatch(c -> Escenario.coincide(r, c.archivo())))
                .map(r -> r.substring(r.lastIndexOf('/') + 1))
                .toList();
        assertThat(orden).as(tipo.nombre() + ": order of legacy SQL")
                .containsExactlyElementsOf(tipo.consultas().stream().map(Spec.Consulta::archivo).toList());
    }

    /** Legacy never runs CentralizaAnticipoPactos SQL for this type; the port methods below do (ContabilidadJdbcRepository.java:72-114). */
    static void noUsaSqlDeAnticipoPactos(Spec.Tipo tipo) {
        Escenario e = new Escenario();
        try {
            e.ejecutar(tipo, "S");
        } catch (RuntimeException ignored) {
            // only the port calls matter here
        }
        verify(e.repository, never()).queryMovimientos(any(), any(), any());
        verify(e.repository, never()).queryEventosOperacion(any(), any(), any());
        verify(e.repository, never()).queryEventosSubyacente(any(), any(), any(), any());
        verify(e.repository, never()).queryDatosMovimientoAnticipo(any(), any());
    }

    /** P_TABMOVIMIENTOS / P_TABEVENTOS are exactly the rows of the type's own SQL (sql.query(..., TabMovimientos.class)). */
    static void movimientosYEventos(Spec.Tipo tipo) {
        Escenario e = new Escenario();
        List<Map<String, Object>> movs = List.of(
                Filas.movimiento(7001L, EJERCICIO, "CLP", "1500.75"),
                Filas.movimiento(7002L, EJERCICIO, "CLP", "-20.10"));
        for (Spec.Consulta c : tipo.de(Spec.Rol.MOV)) {
            e.filas(c.archivo(), movs);
        }
        List<TabEventoRef> eventosEsperados = new ArrayList<>();
        int j = 0;
        for (Spec.Consulta c : tipo.de(Spec.Rol.EVE)) {
            List<Map<String, Object>> evs = List.of(
                    Filas.evento(7001L, "EVENTO_" + j + "_A", "250.50", "CLP"),
                    Filas.evento(7002L, "EVENTO_" + j + "_B", "12.25", "USD"));
            e.filas(c.archivo(), evs);
            evs.forEach(r -> eventosEsperados.add(Filas.eventoRef(r)));
            j++;
        }
        SalidaCentralizacion s = e.ejecutar(tipo, "S");
        assertThat(s.tabMovimientos()).as(tipo.nombre() + ": P_TABMOVIMIENTOS")
                .containsExactly(Filas.movimientoRef(movs.get(0)), Filas.movimientoRef(movs.get(1)));
        assertThat(s.tabEventos()).as(tipo.nombre() + ": P_TABEVENTOS (all event queries, legacy order)")
                .containsExactlyElementsOf(eventosEsperados);
        assertThat(s.tabDatosEvento()).as(tipo.nombre() + ": P_TABDATOSEVENTO is never filled").isEmpty();
    }

    /** P_TABDATOSMOVIMIENTO: one row per legacy add(...) per data row, in legacy order, legacy formatting. */
    static List<TabDatoMovimientoRef> datos(Spec.Tipo tipo, boolean nulos) {
        Escenario e = new Escenario();
        List<Map<String, Object>> todas = new ArrayList<>();
        int i = 0;
        for (Spec.Consulta c : tipo.de(Spec.Rol.DAT)) {
            List<Map<String, Object>> rows = Filas.datos(tipo, i++, nulos);
            e.filas(c.archivo(), rows);
            todas.addAll(rows);
        }
        SalidaCentralizacion s = e.ejecutar(tipo, "S");
        List<TabDatoMovimientoRef> esperado = Filas.esperado(tipo, todas);
        assertThat(s.tabDatosMovimiento().stream().map(TabDatoMovimientoRef::tipoDato).toList())
                .as(tipo.nombre() + ": TipoDato names and order")
                .containsExactlyElementsOf(esperado.stream().map(TabDatoMovimientoRef::tipoDato).toList());
        assertThat(s.tabDatosMovimiento()).as(tipo.nombre() + ": TabDatosMovimiento rows (values)")
                .containsExactlyElementsOf(esperado);
        return s.tabDatosMovimiento();
    }

    /** Literal spot-check of one TabDatosMovimiento value of the first data row. */
    static void literal(List<TabDatoMovimientoRef> datos, String tipoDato, String valorEsperado) {
        assertThat(datos).filteredOn(d -> d.tipoDato().equals(tipoDato)).first()
                .extracting(TabDatoMovimientoRef::valorVarChar).isEqualTo(valorEsperado);
    }

    /** A null in a column whose legacy formatting dereferences it aborts the whole type (exception escapes centralizar). */
    static void nuloEnFechaLanza(Spec.Tipo tipo) {
        Spec.Campo campo = tipo.campos().stream()
                .filter(c -> c.columna() != null && c.formato().lanzaConNulo())
                .findFirst().orElseThrow(() -> new AssertionError("spec has no throwing column"));
        Escenario e = new Escenario();
        int i = 0;
        for (Spec.Consulta c : tipo.de(Spec.Rol.DAT)) {
            List<Map<String, Object>> rows = Filas.datos(tipo, i++, false);
            rows.get(0).put(campo.columna(), null);
            e.filas(c.archivo(), rows);
        }
        assertThatThrownBy(() -> e.ejecutar(tipo, "S"))
                .as(tipo.nombre() + ": null " + campo.columna() + " must abort like the legacy " + campo.formato())
                .isInstanceOf(Exception.class);
    }

    /** llenarResultado: one ResultadoCentralizacion per movement, Ejercicio = String.valueOf(Long) ("null" when null). */
    static void resultados(Spec.Tipo tipo) {
        Escenario e = new Escenario();
        List<Map<String, Object>> movs = List.of(
                Filas.movimiento(7001L, EJERCICIO, "CLP", "1500.75"),
                Filas.movimiento(7002L, null, "CLP", "3.00"));
        for (Spec.Consulta c : tipo.de(Spec.Rol.MOV)) {
            e.filas(c.archivo(), movs);
        }
        SalidaCentralizacion s = e.ejecutar(tipo, "S");
        assertThat(s.resultados()).as(tipo.nombre() + ": P_OUT_RESULTADO")
                .containsExactly(Filas.resultado(movs.get(0)), Filas.resultado(movs.get(1)));
        assertThat(s.resultados()).extracting(ResultadoCentralizacion::ejercicio).containsExactly("2025", "null");
    }

    /**
     * simulaOperacion "N": the type's tables are persisted once, as produced, with CodigoUsuario = String.valueOf(IdUsuario)
     * (CentralizaMultiSeleccion.java:354-357, 411-417 -> CentralizaGenerico.logica).
     */
    static void persisteEnEjecucionReal(Spec.Tipo tipo) {
        Escenario e = new Escenario();
        for (Spec.Consulta c : tipo.de(Spec.Rol.MOV)) {
            e.filas(c.archivo(), List.of(Filas.movimiento(7001L, EJERCICIO, "CLP", "10.00")));
        }
        int i = 0;
        for (Spec.Consulta c : tipo.de(Spec.Rol.DAT)) {
            e.filas(c.archivo(), Filas.datos(tipo, i++, false));
        }
        SalidaCentralizacion s = e.ejecutar(tipo, "N");
        verify(e.repository, times(1)).persistirCentralizacion(
                eq(ID_TIPO), eq(FECHA_CONTABLE), eq(ID_EMPRESA), eq(EJERCICIO), eq(CODIGO_USUARIO),
                eq(s.tabMovimientos()), eq(s.tabEventos()), eq(s.tabDatosMovimiento()), eq(List.of()));
    }

    /** simulaOperacion "S": CentralizaGenerico is not called (CentralizaMultiSeleccion.java:411). */
    static void noPersisteEnSimulacion(Spec.Tipo tipo) {
        Escenario e = new Escenario();
        for (Spec.Consulta c : tipo.de(Spec.Rol.MOV)) {
            e.filas(c.archivo(), List.of(Filas.movimiento(7001L, EJERCICIO, "CLP", "10.00")));
        }
        SalidaCentralizacion s = e.ejecutar(tipo, "S");
        verify(e.repository, never()).persistirCentralizacion(any(), anyString(), any(), any(), anyString(),
                any(), any(), any(), any());
        assertThat(s.erroresContador()).isZero();
    }

    /** Empty SQL results: everything empty, P_OUT_ERRORESCONTADOR = 0, no messages (SalidaCentralizacion ctor, BcsCentralizacion.java:25-39). */
    static void vacio(Spec.Tipo tipo) {
        Escenario e = new Escenario();
        SalidaCentralizacion s = e.ejecutar(tipo, "S");
        assertThat(s.erroresContador()).isZero();
        assertThat(s.mensajes()).isEmpty();
        assertThat(s.resultados()).isEmpty();
        assertThat(s.tabMovimientos()).isEmpty();
        assertThat(s.tabEventos()).isEmpty();
        assertThat(s.tabDatosMovimiento()).isEmpty();
        assertThat(s.tabDatosEvento()).isEmpty();
    }

    private Casos() {}
}
