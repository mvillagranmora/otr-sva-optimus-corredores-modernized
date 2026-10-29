#!/usr/bin/env python3
"""Generates the collaborator FAKES of the legacy oracle (legacy-oracle/shims/**) that stand in for services of OTHER modules/families.

Each fake keeps the legacy nested POJO classes (Entrada/Salida/...) VERBATIM (extracted from the legacy source by name, brace matched, with
the legacy file and line range written in a comment) and replaces the service logic by a recorded, scripted answer:
cl.bcs.oracle.Hooks.responder(name, entrada, Salida.class) records {servicio, entrada} and answers with colaboradores.<name> of case.json
(a JSON object = every call; a JSON array = the n-th call, the last one repeating) decoded into the legacy Salida class, or an empty Salida.

Run:  python mkshims.py <workspaceRoot> <shimsDir>      (hand edited fakes, e.g. SaldoFlujoCaja, are not touched)
"""
import os
import re
import sys

WS = sys.argv[1]
OUT = sys.argv[2]
LEG = WS + '/legacy/otr-sva-optimus-corredores/BSCL'
TES = LEG + '/Tesoreria/JAVA/BcsTesoreriaServicios/src/cl/bcs/tesoreria'
CTA = LEG + '/CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion'
PLA = LEG + '/Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma'
FMM = LEG + '/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo'
MES = LEG + '/Mesa/JAVA/BcsMesaServicios/src/cl/bcs/mesa'
REL = os.path.relpath


def extract(text, name):
    m = re.search(r'^[ \t]*(?:public\s+|private\s+|protected\s+)?(?:static\s+)?(?:abstract\s+)?class\s+' + re.escape(name) + r'\b', text, re.M)
    if not m:
        raise SystemExit('nested class not found: ' + name)
    start = m.start()
    i = text.index('{', m.end())
    depth = 0
    n = len(text)
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


COMMON_IMPORTS = ['java.math.BigDecimal', 'java.sql.Connection', 'java.sql.Date', 'java.sql.Timestamp', 'java.util.ArrayList', 'java.util.HashMap',
                  'java.util.List', 'java.util.Map', 'cl.bcs.utils.Utils', 'cl.bcs.utils.JavaCall', 'cl.bcs.utils.BcsPojo', 'cl.bcs.oracle.Hooks',
                  'cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida', 'com.google.gson.JsonObject']

SPECS = [
    dict(pkg='cl.bcs.tesoreria.ban', cls='ActualizarSaldosCuentaBanco', src=TES + '/ban/ActualizarSaldosCuentaBanco.java',
         nested=['ActualizarSaldosCuentaBanco_Entrada', 'CuentaBanco_Entrada', 'ActualizarSaldosCuentaBanco_Salida'],
         logica=('ActualizarSaldosCuentaBanco_Salida', 'ActualizarSaldosCuentaBanco_Entrada')),
    dict(pkg='cl.bcs.cuentaInversion', cls='CrearCargoAbono', src=CTA + '/CrearCargoAbono.java',
         nested=['ObjCargoAbonoAGenerar', 'ObjCargoAbonoGenerado', 'ObjInformacionDetalle', 'ObjAdvertenciasDetalle', 'ObjErroresDetalle',
                 'CrearCargoAbonoEntrada', 'CrearCargoAbonoSalida'],
         logica=('CrearCargoAbonoSalida', 'CrearCargoAbonoEntrada')),
    dict(pkg='cl.bcs.cuentaInversion', cls='AnularCargoAbono', src=CTA + '/AnularCargoAbono.java',
         nested=['CargoAbono_Entrada', 'AnularCargoAbono_Entrada', 'AnularCargoAbono_Salida', 'ObjInformacionDetalle', 'ObjAdvertenciasDetalle',
                 'ObjErroresDetalle'],
         logica=('AnularCargoAbono_Salida', 'AnularCargoAbono_Entrada')),
    dict(pkg='cl.bcs.plataforma.pla', cls='ConsultarFlujoCaja', src=PLA + '/pla/ConsultarFlujoCaja.java',
         nested=['EntradaConsultarFlujoCaja', 'PortafolioAConsultar', 'ClienteAConsultar', 'ClientePortafolio', 'Uso', 'SalidaConsultarFlujoCaja',
                 'OutFlujoCaja', 'OutFlujoCajaDetallado'],
         logica=('SalidaConsultarFlujoCaja', 'EntradaConsultarFlujoCaja'),
         extra_imports=['cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjERRORESDETALLE', 'cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA',
                        'cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJADETALLADO', 'cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR']),
    dict(pkg='cl.bcs.mesa.SPT', cls='ConsultarOperacionesCanalAgente', src=MES + '/SPT/ConsultarOperacionesCanalAgente.java',
         nested=['Portafolio'], logica=None),
    dict(pkg='cl.bcs.plataforma.aut', cls='ModificarEstadoEventoAutorizacionMasivo', src=PLA + '/aut/ModificarEstadoEventoAutorizacionMasivo.java',
         nested=['ServicioEntrada', 'ServicioEntradaEvento', 'ServicioEntradaEtapa', 'ServicioSalida', 'ServicioSalidaAdvertencia', 'ServicioSalidaError',
                 'EtapaEventoPrc', 'UpdateEventoPorEtapa', 'UpdateEtapaEvento', 'InsertAutorizadoEtapaEvento'],
         logica=('ServicioSalida', 'ServicioEntrada'),
         extra_imports=['cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjERRORESDETALLE']),
    dict(pkg='cl.bcs.tesoreria.nor', cls='AnularNominasTransferencias', src=TES + '/nor/AnularNominasTransferencias.java',
         nested=['RegistrosAnularNominasTransferencias', 'EntradaAnularNominasTransferencias', 'ResultadoAnularNominasTransferencias',
                 'SalidaAnularNominasTransferencias'],
         logica=('SalidaAnularNominasTransferencias', 'EntradaAnularNominasTransferencias'),
         extra_imports=['cl.bcs.plataforma.pla.utils.SalidaServicios']),
    dict(pkg='cl.bcs.fondomutuo.pojo', cls='Traspaso', src=FMM + '/pojo/Traspaso.java', nested=['InMovimientoGenerado'], logica=None),
]

os.makedirs(OUT, exist_ok=True)
for s in SPECS:
    text = open(s['src'], encoding='utf-8', errors='replace').read()
    rel = REL(s['src'], LEG).replace('\\', '/')
    body = []
    for n in s['nested']:
        code, l0, l1 = extract(text, n)
        body.append('\t// legacy ' + rel + ':' + str(l0) + '-' + str(l1) + ' (verbatim)')
        body.append(code)
        body.append('')
    imports = sorted(set(COMMON_IMPORTS + s.get('extra_imports', [])))
    java = ['package ' + s['pkg'] + ';', '']
    java += ['import ' + i + ';' for i in imports]
    java += ['', '/**',
             ' * ORACLE FAKE (generated by legacy-oracle/tools/mkshims.py) of ' + s['pkg'] + '.' + s['cls'] + ' (' + rel + ').',
             ' * The nested POJOs are verbatim copies; the service logic is replaced by a RECORDED, SCRIPTED answer (see Hooks.responder).',
             ' */',
             'public class ' + s['cls'] + ' implements JavaCall {', '']
    java += body
    java += ['\t@Override', '\tpublic JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }', '']
    if s['logica']:
        sal, ent = s['logica']
        java += ['\t/** FAKE of ' + s['cls'] + '.logica: records the input as {servicio:"' + s['cls'] + '", entrada} and answers colaboradores.' + s['cls'] + '. */',
                 '\tpublic ' + sal + ' logica(Connection con, ' + ent + ' entrada) throws Exception {',
                 '\t\treturn Hooks.responder("' + s['cls'] + '", entrada, ' + sal + '.class);', '\t}', '']
    java.append('}')
    d = os.path.join(OUT, *s['pkg'].split('.'))
    os.makedirs(d, exist_ok=True)
    open(os.path.join(d, s['cls'] + '.java'), 'w', encoding='utf-8', newline='\n').write('\n'.join(java) + '\n')
    print('generated', s['pkg'] + '.' + s['cls'])
