"""Creates FRESH-<F>-NN case dirs (case.json + seed.sql, NO expected.json) in the scratch copy of BcsAPVServicios by cloning a development case
and changing its INPUT (and the seed rows when the input needs them). Expected values are recorded afterwards by the unmodified legacy (record.sh)."""
import copy, glob, json, os, shutil, sys

RES = r"C:\Users\MVILLA~1.MOR\AppData\Local\Temp\claude\C--Users-m-villagran-mora-Downloads-develop-nuam-modernizado\56bac01e-34ae-4fa4-9f7c-8c05255c5d60\scratchpad\fv\modernized\otr-sva-optimus-corredores\BcsAPVServicios\src\test\resources"

SPECS = []


def spec(fam, base, num, slug, title, patch=None, seed=None):
    SPECS.append(dict(fam=fam, base=base, id='FRESH-%s-%02d' % (fam.upper(), num), slug=slug, title=title, patch=patch, seed=seed or []))


def E(c):
    return c['entrada']


# ---------------- family A (contratos)
def p_a1(c):
    E(c)['p_Canal'] = 'Z9'
spec('a', 'APV-A-015_', 1, 'canal-con-folio-cero-da-folio-uno', 'channel Z9 whose counter is 0 (lower boundary): folio text Z91', p_a1, [("('BIZ', 99)", "('Z9', 0)")])


def p_a2(c):
    E(c)['p_Canal'] = ' '
spec('a', 'APV-A-010_', 2, 'canal-un-espacio-no-es-vacio', 'channel " " (one blank, not empty): treated as a new channel', p_a2)


def p_a3(c):
    E(c)['p_ContratosSAAPV'][0]['FechaRevocacion'] = '2024-02-29'
    E(c)['p_ContratosSAAPV'][0]['IdFolioContratoSAAPV'] = 'F-Q1'
spec('a', 'APV-A-261_', 3, 'anular-con-fecha-bisiesto', 'ANU of a PDI contract (folio F-Q1) with traspaso, FechaRevocacion on a leap day', p_a3, [('F-K1', 'F-Q1')])


def p_a4(c):
    E(c)['p_ContratosSAAPV'].reverse()
    for x in E(c)['p_ContratosSAAPV']:
        x['IdFolioContratoSAAPV'] = x['IdFolioContratoSAAPV'].replace('F-K', 'F-Q')
spec('a', 'APV-A-267_', 4, 'anular-dos-contratos-orden-invertido', 'ANU of two contracts (folios F-Q2 vigente listed first, F-Q1 pendiente second)', p_a4, [('F-K1', 'F-Q1'), ('F-K2', 'F-Q2')])


def p_a5(c):
    E(c)['p_ContratosSAAPV'].reverse()
spec('a', 'APV-A-254_', 5, 'blotter-dos-eventos-orden-invertido', 'S + REV with F-K2 first and F-K1 second', p_a5)


def p_a6(c):
    E(c)['p_IdsRecaudacion'] = [20]
spec('a', 'APV-A-070_', 6, 'recaudacion-solo-la-veinte', 'only the second recaudacion (20) of the two-client seed', p_a6)


def p_a7(c):
    E(c)['p_IdsRecaudacion'] = [10, 999]
spec('a', 'APV-A-070_', 7, 'recaudacion-diez-e-inexistente', 'ids 10 and 999 (unknown) on the two-client seed', p_a7)


def p_a8(c):
    E(c)['p_ContratosSAAPV'].reverse()
spec('a', 'APV-A-322_', 8, 'revocacion-valido-invalido-valido-invertido', 'revocation list valid/invalid/valid in reverse order', p_a8)


def p_a9(c):
    r = E(c)['p_RelacionLaboral']
    r[0]['IngresoMensual'] = 0
    r[0]['FechaIngresoCargo'] = '2024-02-29'
    r[1]['IngresoMensual'] = 9999999999999.99
    r[1]['FechaRetiroCargo'] = '2025-12-31'
    r[1]['IdEstado'] = 'INA'
    r.append(dict(r[0], IdPersona=503, IngresoMensual=1, CodigoTipoCargoEmpleado='CAJERO'))
spec('a', 'APV-A-044_', 9, 'relacion-laboral-tres-filas-extremos', 'three labour relations: income 0, very large income with retirement date, income 1', p_a9)


def p_a10(c):
    E(c)['p_AsociarPerfilesAPVIn'].append(dict(IdCategoriaInversionista='CONSERVADOR', IdPerfilComercialAbierto='AB3', IdPerfilComercialActivo='AC3'))
    E(c)['p_AsociarPerfilesAPVIn'].insert(0, dict(IdCategoriaInversionista='CONSERVADOR', IdPerfilComercialAbierto='AB0', IdPerfilComercialActivo='AC0'))
spec('a', 'APV-A-026_', 10, 'perfiles-cuatro-filas', 'four profiles in one call (the id never advances)', p_a10)


def p_a11(c):
    E(c)['p_AsociarPerfilesAPVIn'][0]['IdCategoriaInversionista'] = ' '
spec('a', 'APV-A-021_', 11, 'categoria-un-espacio-no-es-vacia', 'category " " (one blank, not empty): validation passes', p_a11)


def p_a12(c):
    E(c)['p_AsociarPerfilesAPVIn'] = [dict(IdCategoriaInversionista='MODERADO', IdPerfilComercialAbierto='AB', IdPerfilComercialActivo='AC'),
                                      dict(IdCategoriaInversionista='AGRESIVO', IdPerfilComercialAbierto='', IdPerfilComercialActivo='AC9'),
                                      dict(IdCategoriaInversionista='', IdPerfilComercialAbierto='AB8', IdPerfilComercialActivo='')]
spec('a', 'APV-A-021_', 12, 'perfiles-valido-y-dos-invalidos', 'one valid profile plus one with empty open profile and one with empty category and active profile', p_a12)

# ---------------- family B (traspasos)


def p_b1(c):
    E(c)['p_TotalADesinvertir'] = 329999
    c['config']['PLATAFORMA/APV.IndicadorRedondeoDesinversion'] = 'HALF_DOWN'
    zero = {"IdTrabajo": 0, "IdInterno": 1, "IdMonedaOrigen": "CLP", "IdMonedaDestino": "CLP", "ValorCambioLocalModificado": 0, "MontoOrigen": 329999, "IdUso": "", "FactorOrigenDestino": 0,
            "MontoDestino": 329999, "FactorOrigenLocal": 0, "MontoLocal": 329999, "FactorOrigenCambio": 0, "IdTipoRegistroOrigenCambio": "", "FactorCambioLocal": 0, "IdTipoRegistroCambioLocal": "",
            "FactorLocalCambio": 0, "IdTipoRegistroLocalCambio": "", "FactorCambioDestino": 0, "IdTipoRegistroCambioDestino": "", "Resultado": "OK", "ValorParametroFinanciero": 0}
    c['colaboradores']['CalcularParidad'] = {'salida': {'p_out_CalculosSalida': [zero], 'P_OUT_ERRORESCONTADOR': 0}}
spec('b', 'APV-B-113_', 1, 'total-un-peso-bajo-el-patrimonio', 'total one peso below the patrimony (330000)', p_b1)


def p_b2(c):
    E(c)['p_TotalADesinvertir'] = 330001
spec('b', 'APV-B-113_', 2, 'total-un-peso-sobre-el-patrimonio', 'total one peso above the patrimony (330000)', p_b2)


def p_b3(c):
    E(c)['p_PorcentajeADesinvertir'] = 1
spec('b', 'APV-B-117_', 3, 'porcentaje-uno-half-down-rv', 'percentage 1 (smallest value not treated as an amount) with HALF_DOWN on RV', p_b3)


def p_b4(c):
    E(c)['p_PorcentajeADesinvertir'] = 100
spec('b', 'APV-B-117_', 4, 'porcentaje-cien-half-down-rv', 'percentage 100 with HALF_DOWN on RV', p_b4)


def p_b5(c):
    E(c)['p_Parametros'] = [{'IdTraspaso': 152}]
spec('b', 'APV-B-45_', 5, 'retencion-solo-el-traspaso-152', 'retention for traspaso 152 alone (the seed has 151 and 152)', p_b5)


def p_b6(c):
    E(c)['p_Parametros'] = [{'IdTraspaso': 151}, {'IdTraspaso': 151}]
spec('b', 'APV-B-37_', 6, 'retencion-mismo-traspaso-dos-veces', 'retention for traspaso 151 listed twice', p_b6)


def p_b7(c):
    E(c)['p_TraspasosValidos'] = [1002, 1001]
spec('b', 'APV-B-28_', 7, 'anular-validos-orden-invertido', 'annul traspasos 1002 then 1001', p_b7)


def p_b8(c):
    E(c)['p_Parametros'] = [{'IdContratoSAAPV': 500, 'IdEmpresa': 1, 'IdTraspaso': 125, 'IdEstado': 'ANU'}, {'IdContratoSAAPV': 500, 'IdEmpresa': 1, 'IdTraspaso': 124, 'IdEstado': 'COM'}, {'IdContratoSAAPV': 500, 'IdEmpresa': 1, 'IdTraspaso': 0, 'IdEstado': 'COM'}]
spec('b', 'APV-B-12_', 8, 'estado-traspaso-tres-filas-id-cero', 'state change: 125 to ANU, 124 to COM, IdTraspaso 0 (lower boundary)', p_b8)


def p_b9(c):
    E(c)['p_Parametros'] = [{'IdOrigenDestinoTransicion': 1, 'Operacion': 'DISTRIBUCION', 'OrigenCtaBco': 0, 'SocioNegocioCtaBco': 0, 'DestinoCtaBco': 0, 'IdEstado': 'INA'}]
spec('b', 'APV-B-01_', 9, 'origen-destino-ceros-e-inactivo', 'origin/destination row set to account 0 everywhere and state INA', p_b9)

# ---------------- family C (recaudacion)


def p_c1(c):
    E(c)['p_BancosPrevired'] = [{'CodigoBancoPrevired': 'chi', 'IdCuentaBanco': 122, 'IdInterno': 0}, {'CodigoBancoPrevired': 'BCI', 'IdCuentaBanco': 122, 'IdInterno': 2147483647}]
spec('c', 'APV-C-381_', 1, 'bancos-codigo-minuscula-y-existente-id-limite', 'new code "chi" with IdInterno 0, existing code BCI with IdInterno 2147483647', p_c1)


def p_c2(c):
    E(c)['p_BancosPrevired'] = [{'CodigoBancoPrevired': 'SAN', 'IdCuentaBanco': 123, 'IdInterno': 2}, {'CodigoBancoPrevired': 'BCI', 'IdCuentaBanco': 122, 'IdInterno': 1}]
spec('c', 'APV-C-396_', 2, 'modificar-bancos-cuentas-cruzadas', 'two updates in reverse order with the accounts swapped', p_c2)


def p_c3(c):
    E(c)['p_BancosPrevired'] = [{'CodigoBancoPrevired': 'ZZZ', 'IdInterno': 2}, {'CodigoBancoPrevired': 'SAN', 'IdInterno': 1}, {'CodigoBancoPrevired': 'BCI', 'IdInterno': 3}]
spec('c', 'APV-C-404_', 3, 'eliminar-desconocido-primero-y-dos-existentes', 'delete: unknown code first, then two existing codes', p_c3)


def p_c4(c):
    E(c)['p_Parametros'] = [{'FolioRecaudacion': 11, 'IdRecaudacionEmpleador': 21, 'IdRecaudacionCliente': 31, 'IdEstado': 'DIS'}, {'FolioRecaudacion': 10, 'IdRecaudacionEmpleador': 0, 'IdRecaudacionCliente': 30, 'IdEstado': 'CON'}]
spec('c', 'APV-C-428_', 4, 'estado-recaudacion-orden-invertido', 'state change of 11 (DIS) then 10 (CON, client detail only)', p_c4)


def p_c5(c):
    e = E(c)
    e['p_FechaDesde'] = '2025-03-31'
    e['p_FechaHasta'] = '2025-03-01'
    c['secuencias']['BSCL.APV.BD.Secuencias::REC.IdAnotacion'] = 3500
    e['p_Titulo'] = 'Anotacion de prueba con acentos: ni\u00f1o, cami\u00f3n'
spec('c', 'APV-C-430_', 5, 'anotacion-fechas-invertidas-y-acentos', 'annotation with FechaDesde after FechaHasta and accented title', p_c5)


def p_c6(c):
    e = E(c)
    e['p_IdRecaudacion'] = 11
    e['p_Titulo'] = ''
    e['p_Detalle'] = 'D' * 200
    c['secuencias']['BSCL.APV.BD.Secuencias::REC.IdAnotacion'] = 3600
spec('c', 'APV-C-433_', 6, 'anotacion-titulo-vacio-detalle-largo', 'annotation for recaudacion 11 with empty title and 200-character detail', p_c6)


def p_b10(c):
    E(c)['p_Parametros'] = [{'IdContratoSAAPV': 500, 'IdEmpresa': 1, 'IdTraspaso': 124, 'IdEstado': 'COM'}, {'IdContratoSAAPV': 500, 'IdEmpresa': 1, 'IdTraspaso': 125, 'IdEstado': 'ZZZZZZ'}, {'IdContratoSAAPV': 500, 'IdEmpresa': 1, 'IdTraspaso': 124, 'IdEstado': 'ANU'}]
spec('b', 'APV-B-12_', 10, 'estado-traspaso-bueno-error-sql-y-bueno', 'state change: 124 to COM, 125 to a too long state (SQL error), 124 to ANU', p_b10)


def p_c7(c):
    E(c)['p_BancosPrevired'] = [{'CodigoBancoPrevired': 'BCI', 'IdCuentaBanco': 0, 'IdInterno': 9}, {'CodigoBancoPrevired': 'CHI', 'IdCuentaBanco': 122, 'IdInterno': 9}, {'CodigoBancoPrevired': 'ITA', 'IdCuentaBanco': 123, 'IdInterno': 6}]
spec('c', 'APV-C-381_', 7, 'bancos-id-interno-repetido-y-codigo-existente', 'three banks: repeated IdInterno 9, existing code BCI with account 0, a clean third', p_c7)


def p_c8(c):
    E(c)['p_BancosPrevired'] = [{'CodigoBancoPrevired': 'bci', 'IdCuentaBanco': 0, 'IdInterno': 1}, {'CodigoBancoPrevired': 'BCI', 'IdCuentaBanco': 122, 'IdInterno': 2}]
spec('c', 'APV-C-391_', 8, 'modificar-codigo-minuscula-y-cuenta-cero', 'update "bci" (case differs) with account 0, then BCI with account 122', p_c8)


DROP = {'FRESH-A-05', 'FRESH-A-07', 'FRESH-A-09', 'FRESH-A-10', 'FRESH-A-11', 'FRESH-A-12', 'FRESH-B-07', 'FRESH-B-09', 'FRESH-C-02', 'FRESH-C-04'}


def build():
    out = []
    for s in SPECS:
        if s['id'] in DROP:
            continue
        bd = glob.glob(os.path.join(RES, 'apv-' + s['fam'], 'cases', s['base'] + '*'))
        assert len(bd) == 1, (s['base'], bd)
        c = json.load(open(os.path.join(bd[0], 'case.json'), encoding='utf-8'))
        before = copy.deepcopy(c['entrada'])
        if s['patch']:
            s['patch'](c)
        assert c['entrada'] != before, s['id']
        c['id'] = s['id']
        c['titulo'] = s['title']
        c['pina'] = 'fresh: cloned from %s with a new input' % os.path.basename(bd[0]).split('_')[0]
        name = '%s_%s' % (s['id'], s['slug'])
        assert len(name) < 70, (name, len(name))
        dst = os.path.join(RES, 'apv-' + s['fam'], 'cases', name)
        os.makedirs(dst, exist_ok=True)
        json.dump(c, open(os.path.join(dst, 'case.json'), 'w', encoding='utf-8', newline='\n'), indent=2, ensure_ascii=False)
        sp = os.path.join(bd[0], 'seed.sql')
        if os.path.exists(sp):
            t = open(sp, encoding='utf-8', newline='').read()
            for a, b in s['seed']:
                assert a in t, (s['id'], a)
                t = t.replace(a, b)
            open(os.path.join(dst, 'seed.sql'), 'w', encoding='utf-8', newline='').write(t)
        else:
            assert not s['seed']
        out.append(name)
    print('\n'.join(out))


build()
