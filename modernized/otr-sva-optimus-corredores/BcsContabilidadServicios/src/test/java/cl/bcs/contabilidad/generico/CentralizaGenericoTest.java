package cl.bcs.contabilidad.generico;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.Centralizacion;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.EventoTipo;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.EventoTipoDato;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.EventoTipoDatoValor;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.InsertCentralizacion;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.InsertEvento;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.InsertLog;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.InsertMovimiento;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.InsertMovimientoDato;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.MovimientoPaso;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.MovimientoVigente;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.TipoCentralizacion;
import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort.ValorDatoPaso;
import cl.bcs.contabilidad.domain.service.CentralizaGenerico;
import cl.bcs.contabilidad.domain.service.CentralizaGenerico.Entrada;
import cl.bcs.contabilidad.domain.service.CentralizaGenerico.MensajeSalida;
import cl.bcs.contabilidad.domain.service.CentralizaGenerico.Salida;
import cl.bcs.contabilidad.domain.service.CentralizaGenerico.TipoCentralizacionSeleccionada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Characterization tests for legacy cl.bcs.contabilidad.CentralizaGenerico.logica
 * (CentralizaGenerico.java:329-676): the step that turns the movement / event / dato
 * tables produced by every Java centralisation into ITZ.Centralizacion, ITZ.Movimiento,
 * ITZ.MovimientoDato, ITZ.EventoTipo, ITZ.Evento and ITZ.Log rows.
 *
 * CentralizaGenerico itself has NO simulaOperacion check; CentralizaMultiSeleccion.java:411
 * only calls it when p_SimulaOperacion = "N".
 *
 * Write calls are collected from every invocation of each port method, so the
 * implementation may batch or not, and may skip a call with an empty list.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CentralizaGenericoTest {

    static final String SEQ_CEN = "BSCL.Contabilidad.BD.Secuencias::IdCentralizacion";
    static final String SEQ_MOV = "BSCL.Contabilidad.BD.Secuencias::IdMovimiento";
    static final String SEQ_EVT = "BSCL.Contabilidad.BD.Secuencias::IdEvento";

    static final Long   EMPRESA = 1L;
    static final Long   T440 = 440L;
    static final Long   T430 = 430L;
    static final String NOMBRE_440 = "Devengo Pacto Tasa Compra";
    static final String NOMBRE_430 = "Devengo Pacto Tasa Pacto";
    static final String FECHA_CONTABLE = "2025-03-10";
    static final String FECHA_PROCESO = "2025-03-11";
    static final String HORA_PROCESO = "13:45:10";
    static final String USUARIO = "97";
    static final String GLOSA_700 = "ErrorPrg - Al ingresar dato movimiento, valor de %s %s , no es valido Procedimiento[CTB_ItzCentralizaGenerico][700]";

    @Mock
    CentralizaGenericoPort port;

    CentralizaGenerico service;

    @BeforeEach
    void setUp() {
        service = new CentralizaGenerico(port);
        when(port.queryEventosTipoDato()).thenReturn(List.of(
                new EventoTipoDato("Moneda", "V", "S"),
                new EventoTipoDato("Nemo", "V", "N"),
                new EventoTipoDato("Plazo", "N", "S"),
                new EventoTipoDato("Fecha", "F", "S")));
        when(port.queryTiposCentralizacion(anyList())).thenReturn(List.of(
                new TipoCentralizacion(T440, "BD-440", "A"),
                new TipoCentralizacion(T430, "BD-430", "A")));
        when(port.queryCentralizacionesVigentes(anyList(), anyString())).thenReturn(List.of());
        when(port.queryMovimientosVigentes(anyList())).thenReturn(List.of());
        when(port.queryEventosTipo(anyList())).thenReturn(List.of(
                new EventoTipo("MONTO_OPERACION", "Monto Operacion", "V")));
        when(port.queryEventosTipoDatoValor()).thenReturn(List.of(
                new EventoTipoDatoValor("Moneda", "CLP", null, "V"),
                new EventoTipoDatoValor("Plazo", null, new BigDecimal("30.00"), "V")));
        when(port.obtenerFechaProceso(EMPRESA)).thenReturn(FECHA_PROCESO);
        when(port.obtenerHoraProceso()).thenReturn(HORA_PROCESO);
        Map<String, AtomicLong> next = new HashMap<>(Map.of(
                SEQ_CEN, new AtomicLong(9000), SEQ_MOV, new AtomicLong(7000), SEQ_EVT, new AtomicLong(5000)));
        when(port.obtenerSecuencia(anyString(), anyLong())).thenAnswer(inv -> {
            String name = inv.getArgument(0);
            long n = inv.getArgument(1);
            AtomicLong c = next.get(name);
            return LongStream.range(0, n).map(k -> c.getAndIncrement()).boxed().toList();
        });
    }

    // ------------------------------------------------------------------ helpers

    static TabMovimientoRef mov(Long tipo, Long idMov, String tipoMov, Long numero, String fecha, String doc, Long portafolio) {
        return new TabMovimientoRef(tipo, idMov, tipoMov, EMPRESA, numero, 0L, fecha, doc, portafolio,
                2025L, "02-2025", new BigDecimal("1000.00"), "CLP", "");
    }

    static TabMovimientoRef mov440() {
        return mov(T440, 70001L, "CARTPACCOM", 88001L, FECHA_CONTABLE, "11.111.111-1", 300L);
    }

    static TabEventoRef evt(Long tipo, Long idMov, String idEvento, String nombre, String monto) {
        return new TabEventoRef(tipo, idMov, idEvento, nombre, monto == null ? null : new BigDecimal(monto), "CLP");
    }

    static TabDatoMovimientoRef dato(Long tipo, Long idMov, String tipoDato, String varchar, String dec) {
        return new TabDatoMovimientoRef(tipo, idMov, tipoDato, varchar, new BigDecimal(dec));
    }

    static Entrada entrada(List<TipoCentralizacionSeleccionada> tipos, List<TabMovimientoRef> movs,
                           List<TabEventoRef> evs, List<TabDatoMovimientoRef> datos, List<TabDatoEventoRef> datosEv) {
        return new Entrada(EMPRESA, tipos, 2025L, FECHA_CONTABLE, USUARIO, movs, evs, datos, datosEv);
    }

    static List<TipoCentralizacionSeleccionada> solo440() {
        return List.of(new TipoCentralizacionSeleccionada(T440, NOMBRE_440));
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> written(String method) {
        return mockingDetails(port).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals(method))
                .flatMap(i -> ((List<T>) i.getArgument(0)).stream())
                .toList();
    }

    // ------------------------------------------------------------------ tests

    @Test
    @DisplayName("CONT-C-50: new movement -> one Centralizacion (Estado V, Glosa '', TipoGeneracion=TipoEnvio), one Movimiento, one Evento, one MovimientoDato")
    // Legacy: CentralizaGenerico.java:549-560 (centralizacion), :566-580 (movimiento), :585-597 (evento), :612-622 (dato)
    void contC50_happyPath() {
        Salida s = service.logica(entrada(solo440(), List.of(mov440()),
                List.of(evt(T440, 70001L, "MONTO_OPERACION", "Monto Operacion", "1000")),
                List.of(dato(T440, 70001L, "Moneda", "CLP", "0")), List.of()));

        assertThat(this.<InsertCentralizacion>written("insertarCentralizaciones")).containsExactly(
                new InsertCentralizacion(9000L, T440, EMPRESA, FECHA_PROCESO, HORA_PROCESO, FECHA_CONTABLE, "", USUARIO, "A", "V"));
        assertThat(this.<InsertMovimiento>written("insertarMovimientos")).containsExactly(
                new InsertMovimiento(7000L, "CARTPACCOM", EMPRESA, 88001L, 0L, FECHA_CONTABLE, "11.111.111-1", 300L,
                        2025L, "02-2025", 9000L, "CLP", new BigDecimal("1000.00"), "V"));
        assertThat(this.<InsertEvento>written("insertarEventos")).containsExactly(
                new InsertEvento(EMPRESA, 5000L, "MONTO_OPERACION", 9000L, 7000L, FECHA_CONTABLE, new BigDecimal("1000"),
                        "CLP", USUARIO, FECHA_PROCESO, HORA_PROCESO, "V"));
        assertThat(this.<InsertMovimientoDato>written("insertarMovimientosDato")).containsExactly(
                new InsertMovimientoDato(7000L, "Moneda", "CLP", new BigDecimal("0"), "I", ""));
        assertThat(this.<EventoTipo>written("insertarEventosTipo")).isEmpty();
        assertThat(this.<InsertLog>written("insertarLogs")).isEmpty();
        assertThat(s.erroresContador()).isZero();
    }

    @Test
    @DisplayName("CONT-C-51: one INFORMACION message per selected tipo: CENTRALIZACIONES_GENERADA_CORRECTAMENTE {Centralizacion, IdCentralizacion, ResultadoMovimientos}")
    // Legacy: CentralizaGenerico.java:649-653
    void contC51_mensajePorTipo() {
        Salida s = service.logica(entrada(solo440(), List.of(mov440()), List.of(), List.of(), List.of()));

        assertThat(s.mensajes()).containsExactly(new MensajeSalida(T440, "INFORMACION",
                "CENTRALIZACIONES_GENERADA_CORRECTAMENTE",
                Map.of("Centralizacion", NOMBRE_440, "IdCentralizacion", "9000", "ResultadoMovimientos", "")));
    }

    @Test
    @DisplayName("CONT-C-52: tipo without movements -> Centralizacion still inserted with Estado 'M'; message ResultadoMovimientos '(SIN MOVIMIENTOS)'")
    // Legacy: CentralizaGenerico.java:639-647
    void contC52_sinMovimientos() {
        Salida s = service.logica(entrada(solo440(), List.of(), List.of(), List.of(), List.of()));

        assertThat(this.<InsertCentralizacion>written("insertarCentralizaciones"))
                .singleElement().extracting(InsertCentralizacion::idCentralizacion, InsertCentralizacion::estado)
                .containsExactly(9000L, "M");
        assertThat(s.mensajes()).singleElement().extracting(m -> m.valores().get("ResultadoMovimientos"))
                .isEqualTo("(SIN MOVIMIENTOS)");
        assertThat(this.<InsertMovimiento>written("insertarMovimientos")).isEmpty();
    }

    @Test
    @DisplayName("CONT-C-53: vigente Centralizacion and TipoEnvio != 'M' -> reuse its IdCentralizacion, no new Centralizacion row")
    // Legacy: CentralizaGenerico.java:466-474 (count), :549 / :561-563 (reuse), :644-647
    void contC53_reutilizaCentralizacionVigente() {
        when(port.queryCentralizacionesVigentes(anyList(), anyString())).thenReturn(List.of(new Centralizacion(T440, 8123L)));

        Salida s = service.logica(entrada(solo440(), List.of(mov440()),
                List.of(evt(T440, 70001L, "MONTO_OPERACION", "Monto Operacion", "1000")), List.of(), List.of()));

        assertThat(this.<InsertCentralizacion>written("insertarCentralizaciones")).isEmpty();
        assertThat(this.<InsertMovimiento>written("insertarMovimientos")).singleElement()
                .extracting(InsertMovimiento::idCentralizacion).isEqualTo(8123L);
        assertThat(this.<InsertEvento>written("insertarEventos")).singleElement()
                .extracting(InsertEvento::idCentralizacion).isEqualTo(8123L);
        assertThat(s.mensajes()).singleElement().extracting(m -> m.valores().get("IdCentralizacion")).isEqualTo("8123");
        verify(port).queryCentralizacionesVigentes(List.of(T440), FECHA_CONTABLE);
    }

    @Test
    @DisplayName("CONT-C-53: vigente Centralizacion but TipoEnvio 'M' -> a new Centralizacion is generated anyway")
    // Legacy: CentralizaGenerico.java:470, :549
    void contC53_tipoEnvioManualSiempreNueva() {
        when(port.queryTiposCentralizacion(anyList())).thenReturn(List.of(new TipoCentralizacion(T440, "BD-440", "M")));
        when(port.queryCentralizacionesVigentes(anyList(), anyString())).thenReturn(List.of(new Centralizacion(T440, 8123L)));

        service.logica(entrada(solo440(), List.of(mov440()), List.of(), List.of(), List.of()));

        assertThat(this.<InsertCentralizacion>written("insertarCentralizaciones")).singleElement()
                .extracting(InsertCentralizacion::idCentralizacion, InsertCentralizacion::tipoGeneracion)
                .containsExactly(9000L, "M");
        assertThat(this.<InsertMovimiento>written("insertarMovimientos")).singleElement()
                .extracting(InsertMovimiento::idCentralizacion).isEqualTo(9000L);
    }

    @Test
    @DisplayName("CONT-C-54: movement already vigente (key tipo+cuenta+fecha yyyy-MM-dd+empresa+numero+documento) is skipped, incl. its events")
    // Legacy: CentralizaGenerico.java:428-437 (vigente key), :479-482 (input key, Fecha normalised via Fecha.toDate)
    void contC54_movimientoVigenteSeOmite() {
        // IdTipoMovimiento differs: it is NOT part of the key, so the input movement still counts as existing.
        when(port.queryMovimientosVigentes(anyList())).thenReturn(List.of(
                new MovimientoVigente(EMPRESA, "2025-03-10", "OTRO_TIPO", 88001L, "11.111.111-1", 300L, T440)));
        TabMovimientoRef conHora = mov(T440, 70001L, "CARTPACCOM", 88001L, "2025-03-10 00:00:00", "11.111.111-1", 300L);

        Salida s = service.logica(entrada(solo440(), List.of(conHora),
                List.of(evt(T440, 70001L, "MONTO_OPERACION", "Monto Operacion", "1000")), List.of(), List.of()));

        assertThat(this.<InsertMovimiento>written("insertarMovimientos")).isEmpty();
        assertThat(this.<InsertEvento>written("insertarEventos")).isEmpty();
        assertThat(this.<InsertCentralizacion>written("insertarCentralizaciones")).singleElement()
                .extracting(InsertCentralizacion::estado).isEqualTo("M");
        assertThat(s.mensajes()).singleElement().extracting(m -> m.valores().get("ResultadoMovimientos"))
                .isEqualTo("(SIN MOVIMIENTOS)");
    }

    @Test
    @DisplayName("CONT-C-55: InsertMovimiento.Fecha is the raw input string (not normalised); null DocumentoIdentidad -> ''")
    // Legacy: CentralizaGenerico.java:572-573
    void contC55_fechaCrudaYDocumentoNulo() {
        TabMovimientoRef m = mov(T440, 70001L, "CARTPACCOM", 88001L, "2025-03-10 00:00:00", null, 300L);

        service.logica(entrada(solo440(), List.of(m), List.of(), List.of(), List.of()));

        assertThat(this.<InsertMovimiento>written("insertarMovimientos")).singleElement()
                .extracting(InsertMovimiento::fecha, InsertMovimiento::documentoIdentidad, InsertMovimiento::cuenta)
                .containsExactly("2025-03-10 00:00:00", "", 300L);
    }

    @Test
    @DisplayName("CONT-C-56: event with null MontoEvento -> Monto 0")
    // Legacy: CentralizaGenerico.java:592
    void contC56_montoEventoNuloEsCero() {
        service.logica(entrada(solo440(), List.of(mov440()),
                List.of(evt(T440, 70001L, "MONTO_OPERACION", "Monto Operacion", null)), List.of(), List.of()));

        assertThat(this.<InsertEvento>written("insertarEventos")).singleElement()
                .extracting(InsertEvento::monto).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("CONT-C-57: unknown IdTipoEvento -> one EventoTipo insert (Estado 'I', Nombre of first event); query gets deduped event ids")
    // Legacy: CentralizaGenerico.java:401-406, :450-453, :598-606
    void contC57_eventoTipoNuevo() {
        TabMovimientoRef m2 = mov(T440, 70002L, "CARTPACCOM", 88002L, FECHA_CONTABLE, "11.111.111-1", 300L);
        service.logica(entrada(solo440(), List.of(mov440(), m2), List.of(
                        evt(T440, 70001L, "MONTO_OPERACION", "Monto Operacion", "1"),
                        evt(T440, 70001L, "NUEVO_EVENTO", "Nuevo Evento", "2"),
                        evt(T440, 70002L, "NUEVO_EVENTO", "Otro Nombre", "3")),
                List.of(), List.of()));

        verify(port).queryEventosTipo(List.of("MONTO_OPERACION", "NUEVO_EVENTO"));
        assertThat(this.<EventoTipo>written("insertarEventosTipo"))
                .containsExactly(new EventoTipo("NUEVO_EVENTO", "Nuevo Evento", "I"));
        assertThat(this.<InsertEvento>written("insertarEventos"))
                .extracting(InsertEvento::idEvento, InsertEvento::idTipoEvento, InsertEvento::idMovimiento)
                .containsExactly(tuple(5000L, "MONTO_OPERACION", 7000L), tuple(5001L, "NUEVO_EVENTO", 7000L),
                        tuple(5002L, "NUEVO_EVENTO", 7001L));
    }

    @ParameterizedTest(name = "CONT-C-58 dato {0}={1}/{2} -> Estado {3}")
    @CsvSource({
            "Moneda,      CLP,        0,  I",   // predefined S, TipoDato V, match ValorVarchar
            "Moneda,      USD,        0,  E",   // predefined S, V, no match
            "Nemo,        CUALQUIERA, 0,  I",   // predefined N -> always valid
            "Plazo,       '',         30, I",   // predefined S, TipoDato N, 30 compareTo 30.00 == 0
            "Plazo,       '',         31, E",   // N, no match
            "Fecha,       2025-03-10, 0,  E",   // predefined S but TipoDato neither V nor N
            "Desconocido, x,          0,  E"    // tipo dato not in EventoTipoDato
    })
    @DisplayName("CONT-C-58: ValorDatoEventoValido: every arm; 'E' rows are still inserted and produce one ITZ.Log row")
    // Legacy: CentralizaGenerico.java:678-695, :617-622
    void contC58_validacionDato(String tipoDato, String valor, String dec, String estado) {
        service.logica(entrada(solo440(), List.of(mov440()), List.of(),
                List.of(dato(T440, 70001L, tipoDato, valor, dec)), List.of()));

        assertThat(this.<InsertMovimientoDato>written("insertarMovimientosDato")).singleElement()
                .extracting(InsertMovimientoDato::estado).isEqualTo(estado);
        assertThat(this.<InsertLog>written("insertarLogs")).hasSize("E".equals(estado) ? 1 : 0);
    }

    @Test
    @DisplayName("CONT-C-59: ITZ.Log row for an invalid dato: every field, Glosa text, RUT without dots, IndicadorError 700")
    // Legacy: CentralizaGenerico.java:697-727
    void contC59_logCompleto() {
        service.logica(entrada(solo440(), List.of(mov440()), List.of(),
                List.of(dato(T440, 70001L, "Moneda", "USD", "0")), List.of()));

        assertThat(this.<InsertLog>written("insertarLogs")).containsExactly(new InsertLog(
                2L, EMPRESA, 2025L, 1L, FECHA_PROCESO, HORA_PROCESO, USUARIO,
                String.format(GLOSA_700, "Moneda", "USD"),
                9000L, 7000L, 0L, 0L, 0L, 0L, "", 0L, 0L, "", "Moneda", T440, "CARTPACCOM",
                88001L, FECHA_CONTABLE, "11111111-1", 300L, "700", "E"));
    }

    @Test
    @DisplayName("CONT-C-59: null DocumentoIdentidad -> Log RutMovimiento ''")
    // Legacy: CentralizaGenerico.java:573, :722
    void contC59_logRutNulo() {
        TabMovimientoRef m = mov(T440, 70001L, "CARTPACCOM", 88001L, FECHA_CONTABLE, null, 300L);
        service.logica(entrada(solo440(), List.of(m), List.of(),
                List.of(dato(T440, 70001L, "Moneda", "USD", "0")), List.of()));

        assertThat(this.<InsertLog>written("insertarLogs")).singleElement()
                .extracting(InsertLog::rutMovimiento).isEqualTo("");
    }

    @Test
    @DisplayName("CONT-C-60: datos evento become MovimientoDato rows with TipoEvento from the dato; datos movimiento get TipoEvento ''")
    // Legacy: CentralizaGenerico.java:610-637
    void contC60_datosEvento() {
        service.logica(entrada(solo440(), List.of(mov440()), List.of(),
                List.of(dato(T440, 70001L, "Moneda", "CLP", "0")),
                List.of(new TabDatoEventoRef(T440, 70001L, "Plazo", "MONTO_OPERACION", "", new BigDecimal("30")))));

        assertThat(this.<InsertMovimientoDato>written("insertarMovimientosDato")).containsExactly(
                new InsertMovimientoDato(7000L, "Moneda", "CLP", new BigDecimal("0"), "I", ""),
                new InsertMovimientoDato(7000L, "Plazo", "", new BigDecimal("30"), "I", "MONTO_OPERACION"));
    }

    @Test
    @DisplayName("CONT-C-61: MovimientoDato deduped by IdMovimiento+IdTipoDatoEvento (first wins) but the Log of the dropped one is kept")
    // Legacy: CentralizaGenerico.java:619-621 (log before dedupe), :661-664 (dedupe)
    void contC61_dedupeDatos() {
        service.logica(entrada(solo440(), List.of(mov440()), List.of(),
                List.of(dato(T440, 70001L, "Moneda", "CLP", "0"), dato(T440, 70001L, "Moneda", "USD", "0")), List.of()));

        assertThat(this.<InsertMovimientoDato>written("insertarMovimientosDato")).singleElement()
                .extracting(InsertMovimientoDato::valorVarchar, InsertMovimientoDato::estado).containsExactly("CLP", "I");
        assertThat(this.<InsertLog>written("insertarLogs")).singleElement()
                .extracting(InsertLog::glosa).isEqualTo(String.format(GLOSA_700, "Moneda", "USD"));
    }

    @Test
    @DisplayName("CONT-C-61: dedupe key is plain string concatenation: (1,'2X') and (12,'X') collide -> second dropped")
    // Legacy: CentralizaGenerico.java:663 (mov.IdMovimiento + mov.IdTipoDatoEvento)
    void contC61_dedupeColisionConcatenacion() {
        when(port.obtenerSecuencia(eq(SEQ_MOV), anyLong())).thenReturn(List.of(1L, 12L));
        TabMovimientoRef a = mov(T440, 70001L, "CARTPACCOM", 88001L, FECHA_CONTABLE, "11.111.111-1", 300L);
        TabMovimientoRef b = mov(T440, 70002L, "CARTPACCOM", 88002L, FECHA_CONTABLE, "11.111.111-1", 300L);

        service.logica(entrada(solo440(), List.of(a, b), List.of(),
                List.of(dato(T440, 70001L, "2X", "a", "0"), dato(T440, 70002L, "X", "b", "0")), List.of()));

        assertThat(this.<InsertMovimientoDato>written("insertarMovimientosDato"))
                .extracting(InsertMovimientoDato::idMovimiento, InsertMovimientoDato::idTipoDatoEvento)
                .containsExactly(tuple(1L, "2X"));
        assertThat(this.<InsertLog>written("insertarLogs")).hasSize(2);
    }

    @Test
    @DisplayName("CONT-C-62: MovimientoPaso deduped by empresa_fecha_numero_documento_portafolio_tipoMov and inserted before the tipo queries")
    // Legacy: CentralizaGenerico.java:358-372, :408-416
    void contC62_movimientosPaso() {
        TabMovimientoRef dup = mov(T440, 70009L, "CARTPACCOM", 88001L, FECHA_CONTABLE, "11.111.111-1", 300L);
        TabMovimientoRef otroTipo = mov(T440, 70010L, "CARTPACVEN", 88001L, FECHA_CONTABLE, "11.111.111-1", 300L);

        service.logica(entrada(solo440(), List.of(mov440(), dup, otroTipo), List.of(), List.of(), List.of()));

        assertThat(this.<MovimientoPaso>written("insertarMovimientosPaso")).containsExactly(
                new MovimientoPaso(EMPRESA, FECHA_CONTABLE, "CARTPACCOM", 88001L, "11.111.111-1", 300L),
                new MovimientoPaso(EMPRESA, FECHA_CONTABLE, "CARTPACVEN", 88001L, "11.111.111-1", 300L));
        InOrder o = inOrder(port);
        o.verify(port).insertarMovimientosPaso(anyList());
        o.verify(port).queryMovimientosVigentes(anyList());
    }

    @Test
    @DisplayName("CONT-C-63: ValorDatoPaso only for predefined ('S') tipos, deduped by tipo_varchar_decimal.toPlainString (30.0 and 30.00 both kept)")
    // Legacy: CentralizaGenerico.java:374-399, :411-412
    void contC63_valoresDatoPaso() {
        service.logica(entrada(solo440(), List.of(mov440()), List.of(),
                List.of(dato(T440, 70001L, "Moneda", "CLP", "0"),
                        dato(T440, 70001L, "Moneda", "CLP", "0"),
                        dato(T440, 70001L, "Nemo", "N1", "0"),
                        dato(T440, 70001L, "Desconocido", "x", "0"),
                        dato(T440, 70001L, "Plazo", "", "30.0")),
                List.of(new TabDatoEventoRef(T440, 70001L, "Plazo", "MONTO_OPERACION", "", new BigDecimal("30.00")),
                        new TabDatoEventoRef(T440, 70001L, "Moneda", "MONTO_OPERACION", "CLP", new BigDecimal("0")))));

        assertThat(this.<ValorDatoPaso>written("insertarValoresDatoPaso")).containsExactly(
                new ValorDatoPaso("Moneda", "CLP", new BigDecimal("0")),
                new ValorDatoPaso("Plazo", "", new BigDecimal("30.0")),
                new ValorDatoPaso("Plazo", "", new BigDecimal("30.00")));
        InOrder o = inOrder(port);
        o.verify(port).insertarValoresDatoPaso(anyList());
        o.verify(port).queryEventosTipoDatoValor();
    }

    @Test
    @DisplayName("CONT-C-64: MAMEVLRFIF/MAMEVALRV: two movements sharing IdMovimiento -> events only on the first; datos on both")
    // Legacy: CentralizaGenerico.java:484-503
    void contC64_mamevEventosSoloPrimero() {
        TabMovimientoRef a = mov(T440, 70001L, "MAMEVLRFIF", 1L, FECHA_CONTABLE, "11.111.111-1", 300L);
        TabMovimientoRef b = mov(T440, 70001L, "MAMEVLRFIF", 2L, FECHA_CONTABLE, "11.111.111-1", 301L);

        service.logica(entrada(solo440(), List.of(a, b),
                List.of(evt(T440, 70001L, "MONTO_OPERACION", "Monto Operacion", "10")),
                List.of(dato(T440, 70001L, "Moneda", "CLP", "0")), List.of()));

        assertThat(this.<InsertEvento>written("insertarEventos")).extracting(InsertEvento::idMovimiento).containsExactly(7000L);
        assertThat(this.<InsertMovimientoDato>written("insertarMovimientosDato"))
                .extracting(InsertMovimientoDato::idMovimiento).containsExactly(7000L, 7001L);
        verify(port).obtenerSecuencia(SEQ_EVT, 1L);
    }

    @Test
    @DisplayName("CONT-C-64: other movement types sharing IdMovimiento -> each movement gets ALL events (duplicated)")
    // Legacy: CentralizaGenerico.java:504-515
    void contC64_otrosTiposEventosDuplicados() {
        TabMovimientoRef a = mov(T440, 70001L, "CARTPACCOM", 1L, FECHA_CONTABLE, "11.111.111-1", 300L);
        TabMovimientoRef b = mov(T440, 70001L, "CARTPACCOM", 2L, FECHA_CONTABLE, "11.111.111-1", 301L);

        service.logica(entrada(solo440(), List.of(a, b),
                List.of(evt(T440, 70001L, "MONTO_OPERACION", "Monto Operacion", "10")), List.of(), List.of()));

        assertThat(this.<InsertEvento>written("insertarEventos"))
                .extracting(InsertEvento::idEvento, InsertEvento::idMovimiento)
                .containsExactly(tuple(5000L, 7000L), tuple(5001L, 7001L));
        verify(port).obtenerSecuencia(SEQ_EVT, 2L);
    }

    @Test
    @DisplayName("CONT-C-65: sequences requested once per name with totals; movements of a non-selected tipo consume an id but are not inserted")
    // Legacy: CentralizaGenerico.java:517 (nEventos), :528-530 (getSecuencia), :564 (only selected tipos iterated)
    void contC65_secuencias() {
        TabMovimientoRef ajeno = mov(999L, 80001L, "OTRO", 99L, FECHA_CONTABLE, "11.111.111-1", 300L);

        service.logica(entrada(solo440(), List.of(mov440(), ajeno),
                List.of(evt(T440, 70001L, "MONTO_OPERACION", "Monto Operacion", "1"),
                        evt(999L, 80001L, "MONTO_OPERACION", "Monto Operacion", "1")),
                List.of(), List.of()));

        verify(port).obtenerSecuencia(SEQ_CEN, 1L);
        verify(port).obtenerSecuencia(SEQ_MOV, 2L);
        verify(port).obtenerSecuencia(SEQ_EVT, 2L);
        assertThat(this.<InsertMovimiento>written("insertarMovimientos")).extracting(InsertMovimiento::idMovimiento)
                .containsExactly(7000L);
        assertThat(this.<InsertEvento>written("insertarEventos")).hasSize(1);
    }

    @Test
    @DisplayName("CONT-C-66: several tipos: ids assigned in selection order (not input-movement order); queries receive the deduped tipo list")
    // Legacy: CentralizaGenerico.java:351-356, :415-426, :543-583
    void contC66_variosTipos() {
        when(port.queryCentralizacionesVigentes(anyList(), anyString())).thenReturn(List.of(new Centralizacion(T430, 8430L)));
        TabMovimientoRef m430 = mov(T430, 60001L, "PACTOCOMP", 77001L, FECHA_CONTABLE, "11.111.111-1", 300L);
        List<TipoCentralizacionSeleccionada> tipos = List.of(
                new TipoCentralizacionSeleccionada(T440, NOMBRE_440),
                new TipoCentralizacionSeleccionada(T430, NOMBRE_430));

        Salida s = service.logica(entrada(tipos, List.of(m430, mov440()), List.of(), List.of(), List.of()));

        verify(port).queryTiposCentralizacion(List.of(T440, T430));
        verify(port).queryMovimientosVigentes(List.of(T440, T430));
        // only 440 needs a new IdCentralizacion; 430 reuses its vigente one (CentralizaGenerico.java:466-474)
        verify(port).obtenerSecuencia(SEQ_CEN, 1L);
        verify(port).obtenerSecuencia(SEQ_MOV, 2L);
        assertThat(this.<InsertCentralizacion>written("insertarCentralizaciones"))
                .extracting(InsertCentralizacion::idCentralizacion, InsertCentralizacion::idTipoCentralizacion)
                .containsExactly(tuple(9000L, T440));
        assertThat(this.<InsertMovimiento>written("insertarMovimientos"))
                .extracting(InsertMovimiento::idMovimiento, InsertMovimiento::idCentralizacion, InsertMovimiento::numero)
                .containsExactly(tuple(7000L, 9000L, 88001L), tuple(7001L, 8430L, 77001L));
        assertThat(s.mensajes()).extracting(MensajeSalida::id, m -> m.valores().get("IdCentralizacion"),
                        m -> m.valores().get("Centralizacion"))
                .containsExactly(tuple(T440, "9000", NOMBRE_440), tuple(T430, "8430", NOMBRE_430));
    }

    @Test
    @DisplayName("CONT-C-67: erroresContador stays 0 even when datos are invalid (only ITZ.Log records them)")
    // Legacy: CentralizaGenerico.java:83-88 (init 0), never incremented in :329-676
    void contC67_erroresSiempreCero() {
        Salida s = service.logica(entrada(solo440(), List.of(mov440()), List.of(),
                List.of(dato(T440, 70001L, "Desconocido", "x", "0"), dato(T440, 70001L, "Moneda", "USD", "0")), List.of()));

        assertThat(s.erroresContador()).isZero();
        assertThat(this.<InsertLog>written("insertarLogs")).hasSize(2);
    }

    @Test
    @DisplayName("CONT-C-68: selected tipo missing from ITZ.CentralizacionTipo -> NullPointerException (tipoCentralizacion.TipoEnvio)")
    // Legacy: CentralizaGenerico.java:559 (also :470 when a vigente centralizacion exists)
    void contC68_tipoDesconocidoFalla() {
        when(port.queryTiposCentralizacion(anyList())).thenReturn(List.of());

        assertThatThrownBy(() -> service.logica(entrada(solo440(), List.of(mov440()), List.of(), List.of(), List.of())))
                .isInstanceOf(NullPointerException.class);
        assertThat(this.<InsertMovimiento>written("insertarMovimientos")).isEmpty();
    }

    @Test
    @DisplayName("CONT-C-69: write order Centralizacion -> Movimiento -> MovimientoDato -> EventoTipo -> Evento -> Log")
    // Legacy: CentralizaGenerico.java:655-672
    void contC69_ordenEscrituras() {
        service.logica(entrada(solo440(), List.of(mov440()),
                List.of(evt(T440, 70001L, "NUEVO_EVENTO", "Nuevo Evento", "1")),
                List.of(dato(T440, 70001L, "Moneda", "USD", "0")), List.of()));

        InOrder o = inOrder(port);
        o.verify(port).insertarCentralizaciones(anyList());
        o.verify(port).insertarMovimientos(anyList());
        o.verify(port).insertarMovimientosDato(anyList());
        o.verify(port).insertarEventosTipo(anyList());
        o.verify(port).insertarEventos(anyList());
        o.verify(port).insertarLogs(anyList());
    }

    @Test
    @DisplayName("CONT-C-69: FechaProceso / HoraProceso come from the port (DB clock) and are shared by Centralizacion and Evento")
    // Legacy: CentralizaGenerico.java:525-526 (FechaDB.ObtenerFechaHoraTs / ObtenerHoraUTC)
    void contC69_fechaHoraProceso() {
        service.logica(entrada(solo440(), List.of(mov440()),
                List.of(evt(T440, 70001L, "MONTO_OPERACION", "Monto Operacion", "1")), List.of(), List.of()));

        verify(port).obtenerFechaProceso(EMPRESA);
        assertThat(this.<InsertEvento>written("insertarEventos")).singleElement()
                .extracting(InsertEvento::fechaProceso, InsertEvento::horaProceso)
                .containsExactly(FECHA_PROCESO, HORA_PROCESO);
        assertThat(this.<InsertCentralizacion>written("insertarCentralizaciones")).singleElement()
                .extracting(InsertCentralizacion::fechaProceso, InsertCentralizacion::horaProceso)
                .containsExactly(FECHA_PROCESO, HORA_PROCESO);
        verify(port, org.mockito.Mockito.never()).insertarLogs(org.mockito.ArgumentMatchers.argThat(l -> !l.isEmpty()));
    }
}
