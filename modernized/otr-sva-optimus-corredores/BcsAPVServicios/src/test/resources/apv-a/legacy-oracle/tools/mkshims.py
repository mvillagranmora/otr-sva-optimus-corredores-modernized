#!/usr/bin/env python3
"""Generates the collaborator FAKES of the APV family A legacy oracle (legacy-oracle/shims/**).

Every fake stands in for a service of ANOTHER module (SocioNegocio, Tesoreria, Portafolio, Custodia, FondoMutuo, CuentaInversion) that the family A
contract classes call in process. A fake keeps the legacy nested POJO classes VERBATIM (extracted from the legacy source by name, brace matched, with the
legacy file and line range written in a comment) and replaces the service logic by a RECORDED, SCRIPTED answer:
cl.bcs.oracle.Hooks.responder(name, entrada, Salida.class) records {servicio, entrada} and answers with colaboradores.<name> of case.json
(a JSON object = every call; a JSON array = the n-th call, the last one repeating) decoded into the legacy Salida class, or an empty Salida.

Some specs have no logica: they only carry nested POJOs of APV classes of OTHER families (family B/C) that family A classes import
(e.g. GrabarRecaudacionTraspaso.CambiarEstadoTraspaso); those are data holders, the behaviour of the class is not reproduced.

Run:  python mkshims.py <workspaceRoot> <shimsDir>      (hand written fakes, e.g. StoredProcedure, are not touched)
"""
import os
import re
import sys

WS = sys.argv[1]
OUT = sys.argv[2]
LEG = WS + '/legacy/otr-sva-optimus-corredores/BSCL'
APV = LEG + '/APV/JAVA/BcsAPVServicios/src/cl/bcs/APV'
TES = LEG + '/Tesoreria/JAVA/BcsTesoreriaServicios/src/cl/bcs/tesoreria'
SOC = LEG + '/SocioNegocio/JAVA/BcsSocioNegocioServicios/src/cl/bcs/socionegocio'
POR = LEG + '/Portafolio/JAVA/BcsPortafolioServicios/src/cl/bcs/portafolio'
CUS = LEG + '/Custodia/JAVA/BcsCustodiaServicios/src/cl/bcs/custodia'
FMM = LEG + '/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo'
CTA = LEG + '/CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion'
REL = os.path.relpath


def extract(text, name):
    m = re.search(r'^[ \t]*(?:public\s+|private\s+|protected\s+)?(?:static\s+)?(?:abstract\s+)?class\s+' + re.escape(name) + r'\b', text, re.M)
    if not m:
        raise SystemExit('nested class not found: ' + name)
    start = m.start()
    i = text.index('{', m.end())
    depth = 0
    n = len(text)
    end = None
    while i < n:
        c = text[i]
        if c == '"':
            i += 1
            while text[i] != '"':
                i += 2 if text[i] == '\\' else 1
        elif c == "'":
            i += 1
            while text[i] != "'":
                i += 2 if text[i] == '\\' else 1
        elif text.startswith('//', i):
            i = text.index('\n', i)
            continue
        elif text.startswith('/*', i):
            i = text.index('*/', i) + 1
        elif c == '{':
            depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0:
                end = i + 1
                break
        i += 1
    l0 = text.count('\n', 0, start) + 1
    l1 = text.count('\n', 0, end) + 1
    return text[start:end], l0, l1


COMMON_IMPORTS = ['java.util.Arrays', 'java.util.Set', 'java.util.HashSet', 'java.math.BigDecimal', 'java.sql.Connection', 'java.sql.Date', 'java.sql.Timestamp', 'java.util.ArrayList', 'java.util.HashMap',
                  'java.util.List', 'java.util.Map', 'cl.bcs.utils.Utils', 'cl.bcs.utils.JavaCall', 'cl.bcs.utils.BcsPojo', 'cl.bcs.oracle.Hooks',
                  'cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida', 'com.google.gson.JsonObject']

SOC_IMPORTS = ['cl.bcs.socionegocio.pojo.Enrolamiento', 'cl.bcs.socionegocio.pojo.ErrorDetalle', 'cl.bcs.socionegocio.pojo.AdvertenciaDetalle',
               'cl.bcs.socionegocio.pojo.SalidaEstandar']

SPECS = [
    dict(pkg='cl.bcs.socionegocio.per', cls='CrearPersona', src=SOC + '/per/CrearPersona.java',
         nested=['EntradaServicioCrearPersona', 'SalidaServicioCrearPersona'],
         logica=('SalidaServicioCrearPersona', 'EntradaServicioCrearPersona'), extra_imports=SOC_IMPORTS),
    dict(pkg='cl.bcs.socionegocio.per', cls='CrearTelefono', src=SOC + '/per/CrearTelefono.java',
         nested=['EntradaCrearTelefono', 'SalidaCrearTelefono'],
         logica=('SalidaCrearTelefono', 'EntradaCrearTelefono'), extra_imports=SOC_IMPORTS),
    dict(pkg='cl.bcs.socionegocio.per', cls='CrearDireccionElectronica', src=SOC + '/per/CrearDireccionElectronica.java',
         nested=['EntradaCrearDireccionElectronica', 'SalidaCrearDireccionElectronica'],
         logica=('SalidaCrearDireccionElectronica', 'EntradaCrearDireccionElectronica'), extra_imports=SOC_IMPORTS),
    dict(pkg='cl.bcs.socionegocio.cli', cls='CrearCliente', src=SOC + '/cli/CrearCliente.java',
         nested=['EntradaServicioCrearCliente', 'SalidaServicioCrearCliente', 'ErrorDetalleCall', 'AdvertenciaDetalleCall'],
         logica=('SalidaServicioCrearCliente', 'EntradaServicioCrearCliente'), extra_imports=SOC_IMPORTS),
    dict(pkg='cl.bcs.tesoreria', cls='AnulacionMandatoPAC', src=TES + '/AnulacionMandatoPAC.java',
         nested=['InContrato', 'EntradaAnulacionContrato', 'SalidaAnulacionContrato'],
         logica=('SalidaAnulacionContrato', 'EntradaAnulacionContrato'), extra_imports=['cl.bcs.fondomutuo.pojo.ResultadoSolicitud']),
    dict(pkg='cl.bcs.tesoreria', cls='RevocacionMandatoPAC', src=TES + '/RevocacionMandatoPAC.java',
         nested=['InMandato', 'EntradaRevocacionMandato', 'SalidaRevocacionMandato'],
         logica=('SalidaRevocacionMandato', 'EntradaRevocacionMandato'), extra_imports=['cl.bcs.fondomutuo.pojo.ResultadoSolicitud']),
    dict(pkg='cl.bcs.portafolio', cls='ModificarPortafolio', src=POR + '/ModificarPortafolio.java',
         nested=['Entrada', 'RegistroCambio', 'Salida', 'ErrorDetalle', 'AdvertenciaDetalle'],
         logica=('Salida', 'Entrada')),
    dict(pkg='cl.bcs.custodia', cls='ConsultarCustodiaPortafolio', src=CUS + '/ConsultarCustodiaPortafolio.java',
         nested=['Entrada_ConsultaCustodiaPortafolio', 'Salida_ConsultaCustodiaPortafolio', 'Tabla_Salida'],
         logica=('Salida_ConsultaCustodiaPortafolio', 'Entrada_ConsultaCustodiaPortafolio'), extra_imports=['cl.bcs.utils.Fecha'],
         prelude=['	// legacy Custodia/JAVA/BcsCustodiaServicios/src/cl/bcs/custodia/ConsultarCustodiaPortafolio.java:53 (verbatim)',
                  '	public static Date fechaInvalida = Fecha.toDate("0001-01-01");']),
    dict(pkg='cl.bcs.fondomutuo', cls='ConsultarCustodiaPortafolioFondoMutuo', src=FMM + '/ConsultarCustodiaPortafolioFondoMutuo.java',
         nested=['EntradaConsultarCustodiaPortafolioFondoMutuo', 'SalidaConsultarCustodiaPortafolioFondoMutuo', 'SalidaCustodia', 'Solicitud'],
         logica=('SalidaConsultarCustodiaPortafolioFondoMutuo', 'EntradaConsultarCustodiaPortafolioFondoMutuo')),
    dict(pkg='cl.bcs.cuentaInversion', cls='SaldoCuentaInversion', src=CTA + '/SaldoCuentaInversion.java',
         nested=['InCuentaInversionConsultar', 'SaldoPorFechaEfectiva', 'SaldoInicialFinal', 'SaldoPorFechaMovimiento'], logica=None),
    dict(pkg='cl.bcs.cuentaInversion', cls='ConsultarSaldoCuentaInversion', src=CTA + '/ConsultarSaldoCuentaInversion.java',
         nested=['EntradaConsultarSaldoCuentaInversion', 'SalidaConsultarSaldoCuentaInversion'],
         logica=('SalidaConsultarSaldoCuentaInversion', 'EntradaConsultarSaldoCuentaInversion'),
         extra_imports=['cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar',
                        'cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva',
                        'cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoInicialFinal',
                        'cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaMovimiento']),
    dict(pkg='cl.bcs.fondomutuo', cls='CrearAporteSAAPV', src=FMM + '/CrearAporteSAAPV.java',
         nested=['SelectDatosAPV', 'SelectIdPortafolio'], logica=None),
    # nested data holders of APV classes of OTHER families that family A classes import (no behaviour reproduced)
    dict(pkg='cl.bcs.APV', cls='GrabarRecaudacionTraspaso', src=APV + '/GrabarRecaudacionTraspaso.java',
         nested=['CambiarEstadoTraspaso'], logica=None),
    dict(pkg='cl.bcs.APV', cls='ProcesarDistribucion', src=APV + '/ProcesarDistribucion.java',
         nested=['Recaudacion'], logica=None, extra_imports=[]),
    dict(pkg='cl.bcs.APV', cls='GrabarTraspasos', src=APV + '/GrabarTraspasos.java',
         nested=['TraspasoModificar'], logica=None),
    dict(pkg='cl.bcs.APV', cls='CargaMasivaPAC', src=APV + '/CargaMasivaPAC.java',
         nested=['Parametros_Entrada'], logica=None),
    dict(pkg='cl.bcs.APV', cls='CargaMasivaPrevired', src=APV + '/CargaMasivaPrevired.java',
         nested=['ExistePersona'], logica=None),
]

os.makedirs(OUT, exist_ok=True)
for s in SPECS:
    text = open(s['src'], encoding='utf-8', errors='replace').read()
    rel = REL(s['src'], LEG).replace('\\', '/')
    body = []
    # every nested class the requested ones refer to (field types, superclasses, constructors) is copied too, transitively
    all_nested = set(re.findall(r'(?m)^[ \t]*(?:public\s+|private\s+|protected\s+)?(?:static\s+)?(?:abstract\s+)?class\s+(\w+)', text))
    all_nested.discard(s['cls'])
    queue = list(s['nested'])
    done = []
    while queue:
        n = queue.pop(0)
        if n in done:
            continue
        done.append(n)
        code, l0, l1 = extract(text, n)
        for tok in set(re.findall(r'\b\w+\b', code)):
            if tok in all_nested and tok not in done and tok not in queue:
                queue.append(tok)
        body.append('\t// legacy ' + rel + ':' + str(l0) + '-' + str(l1) + ' (verbatim)')
        body.append(code)
        body.append('')
    imports = sorted(set(COMMON_IMPORTS + s.get('extra_imports', [])))
    java = ['package ' + s['pkg'] + ';', '']
    java += ['import ' + i + ';' for i in imports]
    java += ['', '/**',
             ' * ORACLE FAKE (generated by legacy-oracle/tools/mkshims.py) of ' + s['pkg'] + '.' + s['cls'] + ' (' + rel + ').',
             ' * The nested POJOs are verbatim copies; the service logic is replaced by a RECORDED, SCRIPTED answer (see Hooks.responder).' if s['logica']
             else ' * Only the nested POJOs listed below are verbatim copies (data holders imported by family A classes); no behaviour is reproduced.',
             ' */',
             'public class ' + s['cls'] + (' implements JavaCall {' if s['logica'] else ' {'), '']
    java += s.get('prelude', [])
    java += body
    if s['logica']:
        sal, ent = s['logica']
        java += ['\t@Override', '\tpublic JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }', '']
        java += ['\t/** FAKE of ' + s['cls'] + '.logica: records the input as {servicio:"' + s['cls'] + '", entrada} and answers colaboradores.' + s['cls'] + '. */',
                 '\tpublic ' + sal + ' logica(Connection con, ' + ent + ' entrada) throws Exception {',
                 '\t\treturn Hooks.responder("' + s['cls'] + '", entrada, ' + sal + '.class);', '\t}', '']
    java.append('}')
    d = os.path.join(OUT, *s['pkg'].split('.'))
    os.makedirs(d, exist_ok=True)
    open(os.path.join(d, s['cls'] + '.java'), 'w', encoding='utf-8', newline='\n').write('\n'.join(java) + '\n')
    print('generated', s['pkg'] + '.' + s['cls'])
