import datetime, os, sys

OUT = sys.argv[1]
LEG = "legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/"

S, VL, VS, D, TD, TS_, C, DEC, TSD, DT = "STR VALUEOF_LONG VALUEOF_STR DATE TOSTRING_DATE TOSTRING_STR CONST DECIMAL TS_O_DEFECTO DATETIME".split()

def f(spec):
    """'Name' -> STR of column Name; 'Name<-Col'; 'Name:FMT'; 'Name:CONST' (no column); 'Name!:CONST' (CONST but column Name exists in POJO)."""
    out = []
    for tok in spec.split():
        fmt = S
        if ':' in tok:
            tok, fmt = tok.split(':')
        if '<-' in tok:
            name, col = tok.split('<-')
        elif tok.endswith('!'):
            name = tok[:-1]; col = name
        else:
            name, col = tok, tok
            if fmt == C:
                col = None
        out.append((name, col, fmt))
    return out

P = {
 'p_IdEmpresa': 'ID_EMPRESA', 'p_FechaContable': 'FECHA_CONTABLE', 'p_IdTipoCentralizacion': 'ID_TIPO',
 'p_DocumentoIdentidadCorredora': 'RUT_CORREDORA', 'LocSistema': 'LOC_SISTEMA', 'p_IdInforme': 'ID_INFORME',
 'p_DocumentoIdentidad': 'RUT_CORREDORA', 'IdEmpresa': 'ID_EMPRESA', 'FechaContable': 'FECHA_CONTABLE',
 'IdTipoCentralizacion': 'ID_TIPO', 'Sistema': 'LOC_SISTEMA', 'MonedaSistema': 'MONEDA_SISTEMA',
 'IdMonedaSistema': 'MONEDA_SISTEMA', 'DocumentoIdentidadCorredora': 'RUT_CORREDORA', 'DocumentoIdentidad': 'RUT_CORREDORA',
 'FechaAnterior': 'FECHA_ANTERIOR', 'p_FechaAnterior': 'FECHA_ANTERIOR', 'IdClasificacionTipoInstrumento': '"PRE"',
 'FechaSistema': 'FECHA_CONTABLE', 'IdBloqueoCentralizacion': '"470_2025-03-10"',
}
def q(archivo, rol, keys, lines):
    return (archivo, rol, keys.split(), lines)

T = []
def tipo(name, consultas, campos, camposLines, specials=""):
    T.append(dict(name=name, consultas=consultas, campos=f(campos), camposLines=camposLines, specials=specials))

exec(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'specs.py'), encoding='utf-8').read())

counter = [0]
def nid():
    counter[0] += 1
    return counter[0]

def literal_for(campos):
    for i, (name, col, fmt) in enumerate(campos):
        if fmt in (D, TD) and col:
            first = next(j for j, c in enumerate(campos) if c[1] == col)
            d = datetime.date(2025, 2, 1) + datetime.timedelta(days=first)
            return name, d.isoformat()
    for i, (name, col, fmt) in enumerate(campos):
        if fmt == VL and col == 'IdMovimiento':
            return name, '7001'
    for i, (name, col, fmt) in enumerate(campos):
        if fmt == S and col:
            return name, col + '-1'

summary = []
for t in T:
    N = t['name']
    cls = N + 'CharacterizationTest'
    L = []
    w = L.append
    w('package cl.bcs.contabilidad.centralizagz;\n')
    w('import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;')
    w('import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;')
    w('import cl.bcs.contabilidad.domain.service.' + N + ';')
    w('import org.junit.jupiter.api.DisplayName;')
    w('import org.junit.jupiter.api.Test;\n')
    w('import java.math.BigDecimal;')
    w('import java.util.HashMap;')
    w('import java.util.List;')
    w('import java.util.Map;\n')
    w('import static cl.bcs.contabilidad.centralizagz.Fx.*;')
    w('import static cl.bcs.contabilidad.centralizagz.Spec.Formato.*;')
    w('import static org.assertj.core.api.Assertions.assertThat;\n')
    w('/**')
    w(' * WP-F characterization of ' + N + '.')
    w(' * Oracle: ' + LEG + N + '.java (and the SQL files it loads).')
    w(' * Every expectation is transcribed from the legacy source; the modern class is only the subject.')
    w(' */')
    w('@SuppressWarnings("unused")')
    w('class ' + cls + ' {\n')
    mv = [c for c in t['consultas'] if c[1] == 'MOV']
    ev = [c for c in t['consultas'] if c[1] == 'EVE']
    dt = [c for c in t['consultas'] if c[1] == 'DAT']
    w('    static final String MOV = "%s";' % mv[0][0])
    w('    static final String EVE = "%s";' % ev[0][0])
    w('    static final String DAT = "%s";\n' % dt[0][0])
    w('    static final Spec.Tipo TIPO = new Spec.Tipo("%s", %s::new,' % (N, N))
    w('            List.of(')
    cs = []
    for (arch, rol, keys, lines) in t['consultas']:
        kv = ', '.join('"%s", %s' % (k, P[k]) for k in keys)
        cs.append('                    // legacy %s.java:%s\n                    Spec.consulta("%s", Spec.Rol.%s, Spec.params(%s))' % (N, lines, arch, rol, kv))
    w(',\n'.join(cs) + '),')
    w('            // legacy %s.java:%s (one add(...) per line, in this order)' % (N, t['camposLines']))
    w('            List.of(')
    fs = []
    for (name, col, fmt) in t['campos']:
        fs.append('                    Spec.campo("%s", %s, %s)' % (name, ('"%s"' % col) if col else 'null', fmt))
    w(',\n'.join(fs) + '));\n')
    ids = []
    def test(desc, mname, body, comment):
        i = nid()
        tid = 'CONT-F-%03d' % i
        ids.append(tid)
        w('    // ' + comment)
        w('    @Test')
        w('    @DisplayName("%s: %s")' % (tid, desc))
        w('    void contF%03d_%s() {' % (i, mname))
        for b in body.strip('\n').split('\n'):
            w('        ' + b)
        w('    }\n')
    ref = 'legacy ' + N + '.java'
    allLines = ','.join(c[3] for c in t['consultas'])
    test('los SQL legacy del tipo existen en el classpath', 'recursosSqlLegacyEnClasspath', 'Casos.recursosSql(TIPO);',
         ref + ' SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)')
    test('cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden', 'consultasConParametrosLegacy', 'Casos.consultas(TIPO);',
         ref + ':' + allLines)
    test('no ejecuta los SQL de CentralizaAnticipoPactos', 'noEjecutaSqlDeAnticipoPactos', 'Casos.noUsaSqlDeAnticipoPactos(TIPO);',
         ref + ' runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL')
    test('P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias', 'movimientosYEventosDeConsultasPropias', 'Casos.movimientosYEventos(TIPO);',
         ref + ':' + ','.join(c[3] for c in mv + ev) + ' (sql.query into TabMovimientos / TabEventos)')
    lit = literal_for(t['campos'])
    nfields = len(t['campos'])
    test('TabDatosMovimiento: %d TipoDato por fila, orden y formato legacy' % nfields, 'datosMovimientoOrdenYFormato',
         'List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);\nassertThat(datos).hasSize(%d * 2 * %d);\nCasos.literal(datos, "%s", "%s");' % (nfields, len(dt), lit[0], lit[1]),
         ref + ':' + t['camposLines'])
    test('TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0', 'datosMovimientoNulos',
         'Casos.datos(TIPO, true);', ref + ':' + t['camposLines'] + ' + pojo/TabDatosMovimiento.java:19-25')
    if any(c[1] and c[2] in (D, TD, TS_, DT) for c in t['campos']):
        test('una fecha nula aborta la centralizacion del tipo', 'fechaNulaAborta', 'Casos.nuloEnFechaLanza(TIPO);',
             ref + ':' + t['camposLines'] + ' Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)')
    test('P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf', 'resultadosDesdeMovimientos', 'Casos.resultados(TIPO);',
         ref + ' llenarResultado -> BcsCentralizacion.java:100-111')
    test('simulaOperacion N persiste una vez las tablas producidas', 'persisteEnEjecucionReal', 'Casos.persisteEnEjecucionReal(TIPO);',
         'CentralizaMultiSeleccion.java:343,354-357,411-417')
    test('simulaOperacion S no persiste', 'noPersisteEnSimulacion', 'Casos.noPersisteEnSimulacion(TIPO);',
         'CentralizaMultiSeleccion.java:411')
    test('consultas vacias producen salida vacia sin errores', 'salidaVaciaSinErrores', 'Casos.vacio(TIPO);',
         'BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103')
    sp = t['specials']
    while '{id}' in sp:
        i = nid()
        tid = 'CONT-F-%03d' % i
        ids.append(tid)
        sp = sp.replace('{id}', tid, 1).replace('{mid}', 'contF%03d' % i, 1)
    w(sp.rstrip('\n'))
    w('}')
    open(os.path.join(OUT, cls + '.java'), 'w', encoding='utf-8').write('\n'.join(L) + '\n')
    summary.append((cls, ids[0], ids[-1], len(ids)))

for s in summary:
    print(*s)
print('total', counter[0])
