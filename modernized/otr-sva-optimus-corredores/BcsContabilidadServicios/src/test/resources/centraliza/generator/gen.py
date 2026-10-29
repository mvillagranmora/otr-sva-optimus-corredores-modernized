# Generator for WP-E characterization tests (CentralizaA* .. CentralizaF*).
# Every spec below is transcribed from the LEGACY classes in
# legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/
import os, sys

OUT = sys.argv[1]
NPE = {'dsql', 'ts', 'tostrS', 'tostrD', 'tostrL', 'plain', 'bdL'}
TYP = {'s': 'S', 'svS': 'S', 'svL': 'L', 'svI': 'I', 'dsql': 'D', 'dstr': 'D', 'tostrD': 'D', 'ts': 'T',
       'tostrS': 'S', 'tostrL': 'L', 'plain': 'B', 'dec': 'B', 'bdL': 'L'}
NPE_WHY = {'dsql': 'Fecha.stringToSqlDate(null) -> SimpleDateFormat.format(null) throws NPE',
           'ts': 'Fecha.toStringDatetime(null) throws NPE',
           'tostrS': 'null.toString() throws NPE', 'tostrD': 'null.toString() throws NPE',
           'tostrL': 'null.toString() throws NPE', 'plain': 'null.toPlainString() throws NPE',
           'bdL': 'new BigDecimal((Long) null) unboxing throws NPE'}
ABBR = {'E': 'ID_EMPRESA', 'F': 'FECHA', 'T': 'ID_TIPO', 'D': 'DOC_CORREDORA', 'L': 'LOC', 'M': 'MONEDA_SISTEMA',
        'C': 'CLASIF', 'A': 'FECHA_ANTERIOR'}

counter = [0]


def nid():
    counter[0] += 1
    return counter[0]


def P(s):
    out = []
    for tok in s.split():
        k, v = tok.split(':', 1)
        out.append((k, ABBR.get(v, v)))
    return out


def parse_fields(s):
    res = []
    for tok in s.split():
        if '=' not in tok:
            res.append((tok, 's', tok)); continue
        n, r = tok.split('=', 1)
        if ':' in r:
            k, c = r.split(':', 1)
        else:
            k, c = r, n
        if k == 'lit' and c == '""':
            c = ''
        res.append((n, k, c))
    return res


def jstr(s):
    return '"' + s.replace('\\', '\\\\').replace('"', '\\"') + '"'


class Cols:
    def __init__(self, fields):
        self.cols = {}
        self.order = []
        for n, k, c in fields:
            if k in ('lit', 'cfgDoc', 'cfgNom') or c == 'IdMovimiento':
                continue
            if c not in self.cols:
                self.cols[c] = (TYP[k], len(self.order))
                self.order.append(c)

    def lit(self, c):
        t, i = self.cols[c]
        return {'S': jstr('v' + c), 'L': '%dL' % (5000 + i), 'I': str(3 + i),
                'D': 'java.sql.Date.valueOf("2025-02-%02d")' % (1 + i % 27),
                'T': 'java.sql.Timestamp.valueOf("2025-03-10 14:35:07.123")',
                'B': 'new BigDecimal("%d.3400")' % (12 + i)}[t]

    def i(self, c):
        return self.cols[c][1]


def expect(f, cols, null):
    n, k, c = f
    Z = 'BigDecimal.ZERO'
    if k == 'lit':
        return (jstr(c), Z)
    if k == 'cfgDoc':
        return ('CFG_DOC', Z)
    if k == 'cfgNom':
        return ('CFG_NOM', Z)
    if c == 'IdMovimiento':
        return ('"7001"', Z)
    i = cols.i(c)
    if null and k not in NPE:
        if k == 's': return ('""', Z)
        if k in ('svS', 'svL', 'svI'): return ('"null"', Z)
        if k == 'dstr': return ('""', Z)
        if k == 'dec': return ('""', Z)
    if k in ('s', 'svS', 'tostrS'): return (jstr('v' + c), Z)
    if k in ('svL', 'tostrL'): return ('"%d"' % (5000 + i), Z)
    if k == 'bdL': return ('""', 'new BigDecimal(%dL)' % (5000 + i))
    if k == 'svI': return ('"%d"' % (3 + i), Z)
    if k in ('dsql', 'dstr', 'tostrD'): return ('"2025-02-%02d"' % (1 + i % 27), Z)
    if k == 'ts': return ('"2025-03-10 14:35:07"', Z)
    if k == 'plain': return ('"%d.3400"' % (12 + i), Z)
    if k == 'dec': return ('""', 'new BigDecimal("%d.3400")' % (12 + i))
    raise Exception(k)


def row_java(cols, fields, mode, npecol=None):
    parts = ['"IdMovimiento", ID_MOV']
    npecols = {c for n, k, c in fields if k in NPE}
    for c in cols.order:
        v = cols.lit(c)
        if mode == 'null' and c not in npecols:
            v = 'null'
        if mode == 'npe' and c == npecol:
            v = 'null'
        parts.append('%s, %s' % (jstr(c), v))
    return 'row(' + ',\n                '.join(parts) + ')'


def tuples(fields, cols, null):
    return ',\n                        '.join('tuple(%s, %s, %s)' % ((jstr(f[0]),) + expect(f, cols, null)) for f in fields)


def mapjava(params):
    return 'Map.ofEntries(' + ', '.join('Map.entry(%s, %s)' % (jstr(k), v) for k, v in params) + ')'


def ptxt(params):
    return '{' + ', '.join(k for k, v in params) + '}'


def ind(lines, n=8):
    return ''.join(' ' * n + l + '\n' for l in lines)


def gen_class(sp):
    cls = sp['cls']
    leg = sp.get('legacy', cls + '.java')
    tests = []
    helpers = []
    for vi, v in enumerate(sp.get('variants', [])):
        suf = v.get('suffix', '')
        setup = v.get('setup', [])
        for (label, f, params, lines) in v.get('queries', []):
            i = nid()
            ni = ''.join('        assertNotIssued(%s);\n' % jstr(x) for x in v.get('notIssued', []))
            tests.append('''    @Test
    @DisplayName("CONT-E-%03d: %s%s query runs %s with params %s (legacy %s:%s)")
    void contE%03d_%s%sQuery() {
        // Legacy: %s:%s
%s        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf(%s)).isEqualTo(%s);
%s    }
''' % (i, label, (' [' + v['label'] + ']') if v.get('label') else '', f, ptxt(params), leg, lines,
       i, label, suf, leg, lines, ind(setup), jstr(f), mapjava(params), ni))
        if not v.get('fields'):
            continue
        fields = parse_fields(v['fields'])
        cols = Cols(fields)
        helpers.append('''    /** Row as the legacy %s POJO would receive it (one value per column). */
    static Map<String, Object> fullRow%s() {
        return %s;
    }

    /** Same row with every column null except IdMovimiento and the columns whose null makes legacy throw. */
    static Map<String, Object> nullRow%s() {
        return %s;
    }
''' % (v.get('pojo', 'TabDatosMovimientoPaso'), suf, row_java(cols, fields, 'full'), suf, row_java(cols, fields, 'null')))
        datos = v['datos']
        ml = v['maplines']
        i = nid()
        tests.append('''    @Test
    @DisplayName("CONT-E-%03d: datos-movimiento%s: exact field names, order and formatting (%d entries per row, legacy %s:%s)")
    void contE%03d_datosMovimientoFieldsOrderAndFormat%s() {
        // Legacy: %s:%s ; TabDatosMovimiento ctor (pojo/TabDatosMovimiento.java:19-25)
%s        rows(%s, List.of(fullRow%s()));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        %s);
        assertIds(salida.tabDatosMovimiento(), ID_MOV);
    }
''' % (i, (' [' + v['label'] + ']') if v.get('label') else '', len(fields), leg, ml, i, suf, leg, ml, ind(setup),
       jstr(datos), suf, tuples(fields, cols, False)))
        i = nid()
        tests.append('''    @Test
    @DisplayName("CONT-E-%03d: datos-movimiento%s null handling: null String -> \\"\\", String.valueOf(null) -> \\"null\\", toStringDate(null) -> \\"\\", null decimal -> 0 (legacy %s:%s)")
    void contE%03d_datosMovimientoNullHandling%s() {
        // Legacy: %s:%s ; TabDatosMovimiento ctor coalesces ValorVarChar null -> "" and ValorDecimal null -> ZERO,
        // but String.valueOf(null) has already produced the 4-char string "null" before the ctor sees it.
%s        rows(%s, List.of(nullRow%s()));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        %s);
    }
''' % (i, (' [' + v['label'] + ']') if v.get('label') else '', leg, ml, i, suf, leg, ml, ind(setup), jstr(datos), suf,
       tuples(fields, cols, True)))
        npef = [f for f in fields if f[1] in NPE]
        if npef:
            n, k, c = npef[0]
            i = nid()
            helpers.append('''    static Map<String, Object> npeRow%s() {
        return %s;
    }
''' % (suf, row_java(cols, fields, 'npe', c)))
            tests.append('''    @Test
    @DisplayName("CONT-E-%03d: datos-movimiento%s: null %s aborts the centralization (%s; legacy %s:%s)")
    void contE%03d_null%sAborts%s() {
        // Legacy: %s:%s field "%s": %s.
        // The exception propagates out of centralizar(); CentralizaMultiSeleccion.java:361-366 catches it and
        // only records the type in the error header. No partial output is produced.
%s        rows(%s, List.of(npeRow%s()));
        assertThatThrownBy(() -> service.centralizar(entrada("S")))
                .as("legacy throws when %s is null").isInstanceOf(RuntimeException.class);
    }
''' % (i, (' [' + v['label'] + ']') if v.get('label') else '', c, NPE_WHY[k], leg, ml, i, c, suf, leg, ml, n,
       NPE_WHY[k], ind(setup), jstr(datos), suf, c))
    if sp.get('standardPass', True):
        i = nid()
        tests.append('''    @Test
    @DisplayName("CONT-E-%03d: movimientos/eventos are the query rows unchanged; resultados via llenarResultado (Ejercicio as String); 0 errors, no messages, no datos-evento")
    void contE%03d_passthroughResultadosAndNoErrors() {
        // Legacy: %s:%s (P_TABMOVIMIENTOS / P_TABEVENTOS = query rows); BcsCentralizacion.java:100-111 llenarResultado;
        // BcsCentralizacion.java:25-39 SalidaCentralizacion(): P_OUT_ERRORESCONTADOR = 0, all lists empty.
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).containsExactly(MOV);
        assertThat(salida.tabEventos()).containsExactlyElementsOf(%s);
        assertThat(salida.resultados()).singleElement().satisfies(r -> {
            assertThat(r.ejercicio()).isEqualTo("2025");
            assertThat(r.idMovimiento()).isEqualTo(ID_MOV);
            assertThat(r.idTipoMovimiento()).isEqualTo("TIPOMOV");
            assertThat(r.monto()).isEqualTo(new BigDecimal("1500.25"));
            assertThat(r.periodo()).isEqualTo("03");
            assertThat(r.estado()).isEqualTo("V");
        });
        assertThat(salida.erroresContador()).isZero();
        assertThat(salida.mensajes()).isEmpty();
        assertThat(salida.tabDatosEvento()).isEmpty();
    }
''' % (i, i, leg, sp['passLines'], sp.get('eventsExpected', 'List.of(EV)')))
    prep = sp.get('persistPrep')
    if prep is None:
        v0 = sp['variants'][0]
        prep = ['rows(%s, List.of(fullRow()));' % jstr(v0['datos'])]
    i = nid()
    tests.append('''    @Test
    @DisplayName("CONT-E-%03d: simulaOperacion=S never writes")
    void contE%03d_simulationNeverWrites() {
        // Legacy: CentralizaMultiSeleccion.java:411 persists (CentralizaGenerico.logica) only when p_SimulaOperacion is "N";
        // %s itself never writes.
%s        service.centralizar(entrada("S"));
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
''' % (i, i, leg, ind(prep)))
    i = nid()
    tests.append('''    @Test
    @DisplayName("CONT-E-%03d: simulaOperacion=N persists exactly the produced tables once (usuario as String.valueOf(idUsuario))")
    void contE%03d_realRunPersistsProducedTables() {
        // Legacy: CentralizaMultiSeleccion.java:347-357 (salida tables appended to entradaCentralizaGenerico) and
        // :411-417 (p_CodigoUsuario = String.valueOf(p_IdUsuario), p_Ejercicio, p_FechaContable, p_IdEmpresa).
%s        SalidaCentralizacion salida = service.centralizar(entrada("N"));
        verify(repository, times(1)).persistirCentralizacion(eq(ID_TIPO), eq(FECHA), eq(ID_EMPRESA), eq(EJERCICIO), eq("97"),
                eq(salida.tabMovimientos()), eq(salida.tabEventos()), eq(salida.tabDatosMovimiento()), eq(salida.tabDatosEvento()));
    }
''' % (i, i, ind(prep)))
    extra = sp.get('extra', '')
    while '@ID@' in extra:
        i = nid()
        extra = extra.replace('@ID@', '%03d' % i, 2)  # display name + method name
    tests.append(extra)
    helpers.append(sp.get('helpers', ''))

    stub_mov = '        when(repository.queryMovimientos(any(), any(), any())).thenReturn(List.of(MOV));\n' if sp.get('stubMov', True) else ''
    sub = sp.get('sub', 'EV_EXTRA')
    # Legacy runs the type's OWN movimientos/eventos SQL (e.g. CentralizaAjustePreciosVentaCorta.java:76-89), so the
    # default MOV/EV rows are served under those SQL names, with the legacy pojo/TabMovimientos.java and
    # pojo/TabEventos.java column names. A test's own rows(...) for the same file overrides this default.
    own, seen = [], set()
    for v in sp.get('variants', []):
        for q in v.get('queries', []):
            label, f = q[0], q[1]
            ref = {'movimientos': 'movRow(MOV)', 'eventos': 'evRow(EV)', 'eventosSubyacente': 'evRow(%s)' % sub}.get(label)
            if ref and f not in seen and not (label == 'movimientos' and not sp.get('stubMov', True)):
                seen.add(f)
                own.append('rows(%s, List.of(%s));' % (jstr(f), ref))
    setup_extra = ind(own + sp.get('setup', []))
    doc = sp.get('doc', '')
    src = HEADER.format(CLS=cls, LEG=leg, DOC=doc, STUB_MOV=stub_mov, SUB=sub, SETUP=setup_extra,
                        IMPORTS=sp.get('imports', ''))
    src += '\n'.join(t for t in tests if t)
    src += '\n    // ------------------------------------------------------------------ fixtures\n\n'
    src += '\n'.join(h for h in helpers if h)
    src += HELPERS + '}\n'
    with open(os.path.join(OUT, cls + 'Test.java'), 'w', encoding='utf-8', newline='\n') as fh:
        fh.write(src)


HEADER = '''package cl.bcs.contabilidad.centraliza;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.{CLS};
{IMPORTS}import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.Invocation;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Characterization tests for {CLS} (work package WP-E).
 *
 * <p>Oracle: legacy {LEG} (BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/), NOT the
 * modern class. Each test cites the legacy lines it pins. A failing test is a modern-vs-legacy discrepancy.
{DOC} *
 * <p>SQL evidence is read from every repository invocation that carries a String argument naming the legacy
 * SQL file (any port method), so the tests do not depend on which port method the implementation uses.
 * All fixture values are synthetic; no legacy credential or real RUT is used.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class {CLS}Test {{

    // Fixture constants: synthetic values only (the RUT-shaped strings are fake).
    static final Long   ID_EMPRESA     = 1L;
    static final Long   ID_USUARIO     = 97L;
    static final String ID_MODULO      = "CON";
    static final Long   ID_TIPO        = 406L;
    static final String FECHA          = "2025-03-10";
    static final String FECHA_ANTERIOR = "2025-03-07";
    static final String FECHA_HIST     = "2025-03-11"; // system date different from the accounting date
    static final Long   EJERCICIO      = 2025L;
    static final Long   ID_PAIS        = 56L;
    static final String DOC_CORREDORA  = "76000000-0";
    static final String LOC            = "CL";
    static final String MONEDA_SISTEMA = "CLP";
    static final String CLASIF         = "ACC";
    static final String CFG_DOC        = "99999999-9";
    static final String CFG_NOM        = "CONTRAPARTE DE PRUEBA";
    static final Long   ID_MOV         = 7001L;

    static final TabMovimientoRef MOV = new TabMovimientoRef(ID_TIPO, ID_MOV, "TIPOMOV", ID_EMPRESA, 7001L, 1L,
            FECHA, "11111111-1", 300L, EJERCICIO, "03", new BigDecimal("1500.25"), "CLP", "V");
    static final TabEventoRef EV = new TabEventoRef(ID_TIPO, ID_MOV, "MONTO", "Monto", new BigDecimal("1500.25"), "CLP");
    static final TabEventoRef EV_SUB = new TabEventoRef(ID_TIPO, ID_MOV, "SUBYACENTE", "Evento subyacente", new BigDecimal("10.00"), "CLP");
    /** Returned by a port query the legacy type never runs; it must not leak into the output. */
    static final TabEventoRef EV_EXTRA = new TabEventoRef(ID_TIPO, ID_MOV, "EXTRA_NO_LEGACY", "No existe en legacy", BigDecimal.ONE, "CLP");

    @Mock PlataformaPort plataforma;
    @Mock ContabilidadRepository repository;
    {CLS} service;

    @BeforeEach
    void setUp() {{
        service = new {CLS}(plataforma, repository);
        when(plataforma.consultarEmpresa(any())).thenReturn(new EmpresaInfo(ID_EMPRESA, ID_PAIS, DOC_CORREDORA, "CORREDORA DE PRUEBA"));
        when(plataforma.obtenerDiaAnterior(any(), any())).thenReturn(FECHA_ANTERIOR);
        when(plataforma.obtenerLocalizacionSistema(any())).thenReturn(LOC);
        when(plataforma.obtenerMonedaSistema(any())).thenReturn(MONEDA_SISTEMA);
        // Unconfigured parameters answer with the default the caller passes (port contract for "absent").
        when(plataforma.obtenerParametroConfiguracion(any(), any(), any(), any())).thenAnswer(inv -> inv.getArgument(3));
        fechaSistema(FECHA);
        when(repository.queryClasificacionTipoInstrumento(any())).thenReturn(CLASIF);
{STUB_MOV}        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of(EV));
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of({SUB}));
{SETUP}    }}

'''

HELPERS = '''
    // ------------------------------------------------------------------ helpers

    EntradaCentralizacion entrada(String simula) {
        return new EntradaCentralizacion(ID_EMPRESA, ID_USUARIO, ID_MODULO, ID_TIPO, FECHA, null, EJERCICIO, simula);
    }

    /** Legacy reads PLA.FechaSistema (MultiSeleccion infoAdicional or ConfiguracionSistema); both port routes agree. */
    void fechaSistema(String f) {
        when(plataforma.obtenerFechaSistema(any())).thenReturn(f);
        when(plataforma.obtenerParametroConfiguracion(any(), eq("PLATAFORMA"), eq("PLA.FechaSistema"), any())).thenReturn(f);
    }

    void localizacion(String loc) {
        when(plataforma.obtenerLocalizacionSistema(any())).thenReturn(loc);
    }

    void config(String modulo, String parametro, String valor) {
        when(plataforma.obtenerParametroConfiguracion(any(), eq(modulo), eq(parametro), any())).thenReturn(valor);
    }

    /** MOV as the row its legacy movimientos SQL returns (columns = pojo/TabMovimientos.java fields). */
    static Map<String, Object> movRow(TabMovimientoRef m) {
        return row("IdTipoCentralizacion", m.idTipoCentralizacion(), "IdMovimiento", m.idMovimiento(),
                "IdTipoMovimiento", m.idTipoMovimiento(), "IdEmpresa", m.idEmpresa(), "Numero", m.numero(),
                "Linea", m.linea(), "Fecha", m.fecha(), "DocumentoIdentidad", m.documentoIdentidad(),
                "IdPortafolio", m.idPortafolio(), "Ejercicio", m.ejercicio(), "Periodo", m.periodo(),
                "Monto", m.monto(), "IdMoneda", m.idMoneda(), "Estado", m.estado());
    }

    /** An event as the row its legacy eventos SQL returns (columns = pojo/TabEventos.java fields). */
    static Map<String, Object> evRow(TabEventoRef e) {
        return row("IdTipoCentralizacion", e.idTipoCentralizacion(), "IdMovimiento", e.idMovimiento(),
                "IdEvento", e.idEvento(), "Nombre", e.nombre(), "MontoEvento", e.montoEvento(),
                "CodigoMoneda", e.codigoMoneda());
    }

    void rows(String sqlFile, List<Map<String, Object>> rows) {
        when(repository.queryDatosMovimientoRows(argThat(n -> isSql(n, sqlFile)), any())).thenReturn(rows);
    }

    static boolean isSql(Object n, String f) {
        return n instanceof String s && (s.equals(f) || s.endsWith("/" + f));
    }

    /** Case-insensitive like the maps JdbcTemplate.queryForList returns. */
    static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedCaseInsensitiveMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    List<String> issuedSql() {
        List<String> out = new ArrayList<>();
        for (Invocation inv : mockingDetails(repository).getInvocations())
            for (Object a : inv.getArguments())
                if (a instanceof String s && s.endsWith(".sql")) out.add(s);
        return out;
    }

    /** Parameters of the single repository invocation that named the given SQL file; fails if 0 or more than 1. */
    @SuppressWarnings("unchecked")
    Map<String, Object> paramsOf(String sqlFile) {
        List<Map<String, Object>> found = new ArrayList<>();
        for (Invocation inv : mockingDetails(repository).getInvocations()) {
            Object[] args = inv.getArguments();
            if (Arrays.stream(args).noneMatch(a -> isSql(a, sqlFile))) continue;
            Map<String, Object> p = new HashMap<>();
            for (Object a : args) {
                if (a instanceof MapSqlParameterSource ps) p = new HashMap<>(ps.getValues());
                else if (a instanceof Map<?, ?> mp) p = new HashMap<>((Map<String, Object>) mp);
            }
            found.add(p);
        }
        assertThat(found)
                .as("legacy runs %s exactly once; modern issued only these SQL resources: %s", sqlFile, issuedSql())
                .hasSize(1);
        return found.get(0);
    }

    void assertNotIssued(String sqlFile) {
        assertThat(issuedSql()).as("legacy does not run %s on this path", sqlFile).noneMatch(n -> isSql(n, sqlFile));
    }

    static void assertIds(List<TabDatoMovimientoRef> datos, Long idMov) {
        assertThat(datos).isNotEmpty().allSatisfy(d -> {
            assertThat(d.idTipoCentralizacion()).isEqualTo(ID_TIPO);
            assertThat(d.idMovimiento()).isEqualTo(idMov);
        });
    }
'''

exec(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'specs.py'), encoding='utf-8').read())

for sp in SPECS:
    gen_class(sp)
print('generated', len(SPECS), 'classes,', counter[0], 'ids')
