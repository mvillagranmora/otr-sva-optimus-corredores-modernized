package cl.bcs.contabilidad.domain.service;

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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Generic persistence step of every Java centralization (WP-C, CONT-C-50..69): legacy
 * {@code cl.bcs.contabilidad.CentralizaGenerico.logica} (CentralizaGenerico.java:329-727). It turns the movement, event,
 * datos-movimiento and datos-evento tables of the selected types into ITZ.Centralizacion, ITZ.Movimiento,
 * ITZ.MovimientoDato, ITZ.EventoTipo, ITZ.Evento and ITZ.Log rows.
 *
 * <p>Rules, in legacy order:
 * <ol>
 *   <li>working rows: one MovimientoPaso per distinct movement (:358-372), one ValorDatoPaso per distinct predefined
 *       dato value (:374-399), written before the reads that join them (:408-412);</li>
 *   <li>a type reuses its vigente Centralizacion unless its TipoEnvio is "M" (:466-474, :549-563); a type that is not in
 *       ITZ.CentralizacionTipo aborts the run with a NullPointerException (:470, :559), before anything else is written;</li>
 *   <li>a movement already vigente (type, cuenta, date, empresa, numero, document) is skipped with its events (:428-482);
 *       MAMEVLRFIF / MAMEVALRV movements sharing an IdMovimiento carry the events only once (:484-503);</li>
 *   <li>ids come from the three sequences, requested once each with the totals (:525-530); a type without new
 *       movements still gets its Centralizacion, in Estado M (:639-647);</li>
 *   <li>each dato is validated against EventoTipoDato / EventoTipoDatoValor; an invalid one is still written, in Estado E,
 *       plus one ITZ.Log row with IndicadorError 700 (:610-637, :678-727); datos are then de-duplicated by
 *       IdMovimiento + IdTipoDatoEvento (:661-664);</li>
 *   <li>unknown event types are registered in Estado I (:598-606); writes go Centralizacion, Movimiento, MovimientoDato,
 *       EventoTipo, Evento, Log (:655-672); one INFORMACION message per selected type (:649-653); the error counter
 *       is never incremented (:83-88).</li>
 * </ol>
 *
 * <p>Every run publishes its messages to the caller that bound a destination with {@link #recogiendoMensajes}: the
 * per-type persistence port ({@code ContabilidadRepository.persistirCentralizacion}) returns nothing, so this is how
 * the multi-selection entry point still reports CENTRALIZACIONES_GENERADA_CORRECTAMENTE as legacy did (:419).
 */
@Service
public class CentralizaGenerico {

    public static final String SECUENCIA_CENTRALIZACION = "BSCL.Contabilidad.BD.Secuencias::IdCentralizacion";
    public static final String SECUENCIA_MOVIMIENTO = "BSCL.Contabilidad.BD.Secuencias::IdMovimiento";
    public static final String SECUENCIA_EVENTO = "BSCL.Contabilidad.BD.Secuencias::IdEvento";

    static final String MENSAJE_INFORMACION = "INFORMACION";
    static final String CENTRALIZACIONES_GENERADA_CORRECTAMENTE = "CENTRALIZACIONES_GENERADA_CORRECTAMENTE";
    private static final String SIN_MOVIMIENTOS = "(SIN MOVIMIENTOS)";
    private static final String GLOSA_DATO_INVALIDO =
            "ErrorPrg - Al ingresar dato movimiento, valor de %s %s , no es valido Procedimiento[CTB_ItzCentralizaGenerico][700]";
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ScopedValue<List<MensajeSalida>> DESTINO_MENSAJES = ScopedValue.newInstance();

    private final CentralizaGenericoPort port;

    public CentralizaGenerico(CentralizaGenericoPort port) {
        this.port = port;
    }

    // ------------------------------------------------------------------ contract

    /** Legacy {@code InIdTipoCentralizacion}. A null nombre is taken from ITZ.CentralizacionTipo (same column). */
    public record TipoCentralizacionSeleccionada(Long idTipoCentralizacion, String nombre) {}

    /** Legacy {@code EntradaCentralizaGenerico}. */
    public record Entrada(Long idEmpresa, List<TipoCentralizacionSeleccionada> tiposCentralizacion, Long ejercicio,
                          String fechaContable, String codigoUsuario, List<TabMovimientoRef> movimientos,
                          List<TabEventoRef> eventos, List<TabDatoMovimientoRef> datosMovimiento,
                          List<TabDatoEventoRef> datosEvento) {
        public Entrada {
            tiposCentralizacion = tiposCentralizacion == null ? List.of() : tiposCentralizacion;
            movimientos = movimientos == null ? List.of() : movimientos;
            eventos = eventos == null ? List.of() : eventos;
            datosMovimiento = datosMovimiento == null ? List.of() : datosMovimiento;
            datosEvento = datosEvento == null ? List.of() : datosEvento;
        }
    }

    /** Legacy {@code PlataformaUtils.MensajeSalida} before the dictionary lookup: id, tipo, codigo and its values. */
    public record MensajeSalida(Long id, String tipo, String codigo, Map<String, String> valores) {}

    /** Legacy {@code SalidaCentralizaGenerico}. */
    public record Salida(long erroresContador, List<MensajeSalida> mensajes) {}

    /**
     * Runs {@code accion} collecting into {@code destino} the messages of every {@link #logica} run it triggers on this
     * thread (for instance through {@code ContabilidadRepository.persistirCentralizacion}).
     */
    public static <T> T recogiendoMensajes(List<MensajeSalida> destino, Supplier<T> accion) {
        return ScopedValue.where(DESTINO_MENSAJES, destino).call(accion::get);
    }

    // ------------------------------------------------------------------ flow

    /** One movement to insert with its events and datos (legacy {@code MovimientoProceso}). */
    private record MovimientoProceso(TabMovimientoRef movimiento, List<TabEventoRef> eventos, List<Dato> datos) {}

    /** A datos-movimiento ({@code tipoEvento ""}) or datos-evento row. */
    private record Dato(String tipoDato, String valorVarChar, BigDecimal valorDecimal, String tipoEvento) {}

    private record Clave(Long idTipoCentralizacion, Long idMovimiento) {}

    /** What the reads return, keyed as legacy {@code Utils.List2Hash} did (the last row of a key wins). */
    private record Catalogo(Map<String, EventoTipoDato> tiposDato, Map<Long, TipoCentralizacion> tipos,
                            Map<Long, Centralizacion> vigentes, Set<String> movimientosVigentes,
                            Map<String, EventoTipo> eventosTipo, List<EventoTipoDatoValor> valoresDato) {}

    public Salida logica(Entrada entrada) {
        Map<String, EventoTipoDato> tiposDato = porClave(port.queryEventosTipoDato(), EventoTipoDato::idTipoDatoEvento);
        List<Long> idsTipo = entrada.tiposCentralizacion().stream()
                .map(TipoCentralizacionSeleccionada::idTipoCentralizacion).distinct().toList();

        port.insertarMovimientosPaso(movimientosPaso(entrada.movimientos()));
        port.insertarValoresDatoPaso(valoresDatoPaso(entrada, tiposDato));

        Catalogo cat = new Catalogo(tiposDato,
                porClave(port.queryTiposCentralizacion(idsTipo), TipoCentralizacion::idTipoCentralizacion),
                porClave(port.queryCentralizacionesVigentes(idsTipo, entrada.fechaContable()), Centralizacion::idTipoCentralizacion),
                port.queryMovimientosVigentes(idsTipo).stream().map(CentralizaGenerico::claveVigente).collect(Collectors.toSet()),
                porClave(port.queryEventosTipo(entrada.eventos().stream().map(TabEventoRef::idEvento).distinct().toList()),
                        EventoTipo::idTipoEvento),
                port.queryEventosTipoDatoValor());

        long nuevasCentralizaciones = 0;
        for (TipoCentralizacionSeleccionada sel : entrada.tiposCentralizacion()) {
            if (generaCentralizacion(sel, cat)) nuevasCentralizaciones++;
        }

        List<MovimientoProceso> procesos = movimientosAProcesar(entrada, cat.movimientosVigentes());
        long nEventos = procesos.stream().mapToLong(p -> p.eventos().size()).sum();
        Map<Long, List<MovimientoProceso>> porTipo = procesos.stream().collect(
                Collectors.groupingBy(p -> p.movimiento().idTipoCentralizacion(), LinkedHashMap::new, Collectors.toList()));

        String fechaProceso = port.obtenerFechaProceso(entrada.idEmpresa());
        String horaProceso = port.obtenerHoraProceso();
        Secuencia idsCentralizacion = new Secuencia(port.obtenerSecuencia(SECUENCIA_CENTRALIZACION, nuevasCentralizaciones));
        Secuencia idsMovimiento = new Secuencia(port.obtenerSecuencia(SECUENCIA_MOVIMIENTO, procesos.size()));
        Secuencia idsEvento = new Secuencia(port.obtenerSecuencia(SECUENCIA_EVENTO, nEventos));

        List<InsertCentralizacion> centralizaciones = new ArrayList<>();
        List<InsertMovimiento> movimientos = new ArrayList<>();
        List<InsertEvento> eventos = new ArrayList<>();
        List<InsertMovimientoDato> datos = new ArrayList<>();
        Map<String, EventoTipo> eventosTipoNuevos = new LinkedHashMap<>();
        List<InsertLog> logs = new ArrayList<>();
        List<MensajeSalida> mensajes = new ArrayList<>();

        for (TipoCentralizacionSeleccionada sel : entrada.tiposCentralizacion()) {
            TipoCentralizacion tipo = cat.tipos().get(sel.idTipoCentralizacion());
            boolean nueva = generaCentralizacion(sel, cat);
            Long idCentralizacion = nueva ? idsCentralizacion.siguiente()
                    : cat.vigentes().get(sel.idTipoCentralizacion()).idCentralizacion();

            List<MovimientoProceso> delTipo = porTipo.getOrDefault(sel.idTipoCentralizacion(), List.of());
            for (MovimientoProceso p : delTipo) {
                TabMovimientoRef m = p.movimiento();
                InsertMovimiento mov = new InsertMovimiento(idsMovimiento.siguiente(), m.idTipoMovimiento(), m.idEmpresa(),
                        m.numero(), m.linea(), m.fecha(), Objects.requireNonNullElse(m.documentoIdentidad(), ""),
                        m.idPortafolio(), m.ejercicio(), m.periodo(), idCentralizacion, m.idMoneda(), m.monto(), "V");
                movimientos.add(mov);

                for (TabEventoRef ev : p.eventos()) {
                    eventos.add(new InsertEvento(mov.idEmpresa(), idsEvento.siguiente(), ev.idEvento(), idCentralizacion,
                            mov.idMovimiento(), entrada.fechaContable(),
                            ev.montoEvento() == null ? BigDecimal.ZERO : ev.montoEvento(), ev.codigoMoneda(),
                            entrada.codigoUsuario(), fechaProceso, horaProceso, "V"));
                    if (!cat.eventosTipo().containsKey(ev.idEvento())) {
                        eventosTipoNuevos.putIfAbsent(ev.idEvento(), new EventoTipo(ev.idEvento(), ev.nombre(), "I"));
                    }
                }
                for (Dato d : p.datos()) {
                    String estado = estadoDato(cat.tiposDato().get(d.tipoDato()), d, cat.valoresDato());
                    if ("E".equals(estado)) {
                        logs.add(log(entrada, d, mov, sel.idTipoCentralizacion(), fechaProceso, horaProceso));
                    }
                    datos.add(new InsertMovimientoDato(mov.idMovimiento(), d.tipoDato(), d.valorVarChar(),
                            d.valorDecimal(), estado, d.tipoEvento()));
                }
            }

            if (nueva) {
                centralizaciones.add(new InsertCentralizacion(idCentralizacion, sel.idTipoCentralizacion(),
                        entrada.idEmpresa(), fechaProceso, horaProceso, entrada.fechaContable(), "",
                        entrada.codigoUsuario(), tipo.tipoEnvio(), delTipo.isEmpty() ? "M" : "V"));
            }
            Map<String, String> valores = new HashMap<>();
            valores.put("Centralizacion", sel.nombre() != null ? sel.nombre() : tipo.nombre());
            valores.put("IdCentralizacion", String.valueOf(idCentralizacion));
            valores.put("ResultadoMovimientos", delTipo.isEmpty() ? SIN_MOVIMIENTOS : "");
            mensajes.add(new MensajeSalida(sel.idTipoCentralizacion(), MENSAJE_INFORMACION,
                    CENTRALIZACIONES_GENERADA_CORRECTAMENTE, valores));
        }

        port.insertarCentralizaciones(centralizaciones);
        port.insertarMovimientos(movimientos);
        // Legacy key is the plain concatenation IdMovimiento + IdTipoDatoEvento (1 + "2X" == 12 + "X"); the first wins.
        Set<String> vistos = new HashSet<>();
        port.insertarMovimientosDato(datos.stream().filter(d -> vistos.add(d.idMovimiento() + d.idTipoDatoEvento())).toList());
        port.insertarEventosTipo(List.copyOf(eventosTipoNuevos.values()));
        port.insertarEventos(eventos);
        port.insertarLogs(logs);

        Salida salida = new Salida(0L, List.copyOf(mensajes));
        if (DESTINO_MENSAJES.isBound()) DESTINO_MENSAJES.get().addAll(salida.mensajes());
        return salida;
    }

    // ------------------------------------------------------------------ steps

    /** Legacy :470 / :549. A type missing from ITZ.CentralizacionTipo dereferenced null there. */
    private static boolean generaCentralizacion(TipoCentralizacionSeleccionada sel, Catalogo cat) {
        TipoCentralizacion tipo = Objects.requireNonNull(cat.tipos().get(sel.idTipoCentralizacion()),
                () -> "Tipo de centralizacion " + sel.idTipoCentralizacion() + " no existe en ITZ.CentralizacionTipo");
        return !cat.vigentes().containsKey(sel.idTipoCentralizacion()) || "M".equals(tipo.tipoEnvio());
    }

    /** Legacy :358-372: key empresa_fecha_numero_documento_portafolio_tipoMovimiento, raw Fecha. */
    private static List<MovimientoPaso> movimientosPaso(List<TabMovimientoRef> movimientos) {
        Map<String, MovimientoPaso> paso = new LinkedHashMap<>();
        for (TabMovimientoRef m : movimientos) {
            String clave = m.idEmpresa() + "_" + m.fecha() + "_" + m.numero() + "_" + m.documentoIdentidad() + "_"
                    + m.idPortafolio() + "_" + m.idTipoMovimiento();
            paso.putIfAbsent(clave, new MovimientoPaso(m.idEmpresa(), m.fecha(), m.idTipoMovimiento(), m.numero(),
                    m.documentoIdentidad(), m.idPortafolio()));
        }
        return List.copyOf(paso.values());
    }

    /** Legacy :374-399: predefined ("S") tipos only; key tipo_varchar_decimal.toPlainString(), datos movimiento first. */
    private static List<ValorDatoPaso> valoresDatoPaso(Entrada entrada, Map<String, EventoTipoDato> tiposDato) {
        Map<String, ValorDatoPaso> paso = new LinkedHashMap<>();
        List<Dato> todos = new ArrayList<>();
        entrada.datosMovimiento().forEach(d -> todos.add(new Dato(d.tipoDato(), d.valorVarChar(), d.valorDecimal(), "")));
        entrada.datosEvento().forEach(d -> todos.add(new Dato(d.tipoDato(), d.valorVarChar(), d.valorDecimal(), d.tipoEvento())));
        for (Dato d : todos) {
            String clave = d.tipoDato() + "_" + d.valorVarChar() + "_" + d.valorDecimal().toPlainString();
            EventoTipoDato tipo = tiposDato.get(d.tipoDato());
            if (tipo != null && "S".equals(tipo.datosPreDefinidos())) {
                paso.putIfAbsent(clave, new ValorDatoPaso(d.tipoDato(), d.valorVarChar(), d.valorDecimal()));
            }
        }
        return List.copyOf(paso.values());
    }

    /**
     * Legacy :476-521: the movements that are not vigente yet, each with its events and datos (matched by type and
     * IdMovimiento). Movements of types that were not selected are kept too (they consume a movement id, :529).
     */
    private static List<MovimientoProceso> movimientosAProcesar(Entrada entrada, Set<String> vigentes) {
        Map<Clave, List<TabEventoRef>> eventos = agrupar(entrada.eventos(), e -> new Clave(e.idTipoCentralizacion(), e.idMovimiento()));
        Map<Clave, List<TabDatoMovimientoRef>> datosMov = agrupar(entrada.datosMovimiento(),
                d -> new Clave(d.idTipoCentralizacion(), d.idMovimiento()));
        Map<Clave, List<TabDatoEventoRef>> datosEv = agrupar(entrada.datosEvento(),
                d -> new Clave(d.idTipoCentralizacion(), d.idMovimiento()));

        Set<Long> conEventosMamev = new HashSet<>();
        List<MovimientoProceso> procesos = new ArrayList<>();
        for (TabMovimientoRef m : entrada.movimientos()) {
            if (vigentes.contains(claveEntrada(m))) continue;
            Clave clave = new Clave(m.idTipoCentralizacion(), m.idMovimiento());
            List<TabEventoRef> evs = eventos.getOrDefault(clave, List.of());
            List<TabEventoRef> propios;
            if (esMamev(m.idTipoMovimiento())) {
                // Legacy :484-493: only the first movement of an IdMovimiento (of any type) that has events gets them.
                propios = conEventosMamev.contains(m.idMovimiento()) ? List.of() : evs;
                if (!evs.isEmpty()) conEventosMamev.add(m.idMovimiento());
            } else {
                propios = evs;
            }
            List<Dato> datos = new ArrayList<>();
            datosMov.getOrDefault(clave, List.of())
                    .forEach(d -> datos.add(new Dato(d.tipoDato(), d.valorVarChar(), d.valorDecimal(), "")));
            datosEv.getOrDefault(clave, List.of())
                    .forEach(d -> datos.add(new Dato(d.tipoDato(), d.valorVarChar(), d.valorDecimal(), d.tipoEvento())));
            procesos.add(new MovimientoProceso(m, propios, datos));
        }
        return procesos;
    }

    private static boolean esMamev(String idTipoMovimiento) {
        return "MAMEVLRFIF".equals(idTipoMovimiento) || "MAMEVALRV".equals(idTipoMovimiento);
    }

    /** Legacy :430-436, Utils.appendKeys(tipo, cuenta, fecha, empresa, numero, documento). */
    private static String claveVigente(MovimientoVigente v) {
        return claves(v.idTipoCentralizacion().toString(), v.cuenta().toString(), v.fecha(), v.idEmpresa().toString(),
                v.numero().toString(), v.documentoIdentidad());
    }

    /** Legacy :479-480: same key for an input movement, its Fecha normalized by Fecha.toDate to yyyy-MM-dd. */
    private static String claveEntrada(TabMovimientoRef m) {
        return claves(m.idTipoCentralizacion().toString(), m.idPortafolio().toString(), fecha(m.fecha()),
                m.idEmpresa().toString(), m.numero().toString(), m.documentoIdentidad());
    }

    /** Legacy Fecha.toDate: "yyyy-MM-dd HH:mm:ss" when 19 characters long, "yyyy-MM-dd" otherwise. */
    private static String fecha(String fecha) {
        LocalDate d = fecha.length() == 19 ? LocalDate.parse(fecha, FECHA_HORA) : LocalDate.parse(fecha);
        return d.toString();
    }

    private static String claves(String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String p : partes) sb.append(p).append('-');
        return sb.toString();
    }

    /** Legacy ValorDatoEventoValido (:678-695): "I" valid, "E" invalid. */
    private static String estadoDato(EventoTipoDato tipo, Dato d, List<EventoTipoDatoValor> valores) {
        if (tipo == null) return "E";
        if (!"S".equals(tipo.datosPreDefinidos())) return "I";
        for (EventoTipoDatoValor v : valores) {
            if (v.idTipoDatoEvento().equals(tipo.idTipoDatoEvento())
                    && (("V".equals(tipo.tipoDato()) && d.valorVarChar().equals(v.valorVarchar()))
                    || ("N".equals(tipo.tipoDato()) && d.valorDecimal().compareTo(v.valorDecimal()) == 0))) {
                return "I";
            }
        }
        return "E";
    }

    /** Legacy GenerarLog (:697-727). */
    private static InsertLog log(Entrada entrada, Dato d, InsertMovimiento mov, Long idTipoCentralizacion,
                                 String fechaProceso, String horaProceso) {
        return new InsertLog(2L, entrada.idEmpresa(), entrada.ejercicio(), 1L, fechaProceso, horaProceso,
                entrada.codigoUsuario(), GLOSA_DATO_INVALIDO.formatted(d.tipoDato(), d.valorVarChar()),
                mov.idCentralizacion(), mov.idMovimiento(), 0L, 0L, 0L, 0L, "", 0L, 0L, "", d.tipoDato(),
                idTipoCentralizacion, mov.idTipoMovimiento(), mov.numero(), mov.fecha(),
                mov.documentoIdentidad().replace(".", ""), mov.cuenta(), "700", "E");
    }

    private static <K, V> Map<K, V> porClave(List<V> filas, Function<V, K> clave) {
        Map<K, V> m = new HashMap<>();
        for (V f : filas) m.put(clave.apply(f), f);
        return m;
    }

    private static <T> Map<Clave, List<T>> agrupar(List<T> filas, Function<T, Clave> clave) {
        Map<Clave, List<T>> m = new HashMap<>();
        for (T f : filas) m.computeIfAbsent(clave.apply(f), k -> new ArrayList<>()).add(f);
        return m;
    }

    /** Consumes the ids of one {@code Utils.getSecuencia} call in order. */
    private static final class Secuencia {
        private final List<Long> ids;
        private int siguiente;

        Secuencia(List<Long> ids) {
            this.ids = ids;
        }

        Long siguiente() {
            return ids.get(siguiente++);
        }
    }
}
