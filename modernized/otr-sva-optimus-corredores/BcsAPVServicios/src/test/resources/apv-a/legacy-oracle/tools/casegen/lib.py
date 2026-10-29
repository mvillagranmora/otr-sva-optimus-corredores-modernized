"""Case input generator of APV family A: helpers.

WHAT THIS IS: the SOURCE of the INPUTS of every case (cases/APV-A-NNN_slug/case.json and seed.sql). The EXPECTED values are never written here:
expected.json is recorded by legacy-oracle/record.sh executing the unmodified legacy class. Regenerating the inputs rewrites case.json/seed.sql
only; run record.sh afterwards for the cases whose inputs changed.
"""
import json
import os
import re

CASOS = []


def _legacy_apv():
    """Legacy APV source folder (read only): LEGACY_ROOT or the workspace found by walking up from this file."""
    root = os.environ.get('LEGACY_ROOT')
    if not root:
        d = os.path.abspath(os.path.dirname(__file__))
        while d and not os.path.isdir(os.path.join(d, 'legacy')):
            n = os.path.dirname(d)
            if n == d:
                raise SystemExit('workspace root not found')
            d = n
        root = os.path.join(d, 'legacy', 'otr-sva-optimus-corredores', 'BSCL')
    return os.path.join(root, 'APV', 'JAVA', 'BcsAPVServicios', 'src', 'cl', 'bcs', 'APV')


_FUENTES = {}


def _lineas(archivo):
    if archivo not in _FUENTES:
        with open(os.path.join(_legacy_apv(), archivo), encoding='utf-8', errors='replace') as f:
            _FUENTES[archivo] = f.read().split(chr(10))
    return _FUENTES[archivo]


def L(archivo, inicio, fin=None):
    """Citation of the legacy lines a case pins, resolved from the SOURCE at generation time: the first line matching the regex `inicio`
    (and, with `fin`, the first line matching `fin` after it). A regex that matches nothing aborts the generation: no citation is ever guessed."""
    ls = _lineas(archivo)
    a = next((i for i, t in enumerate(ls) if re.search(inicio, t)), None)
    if a is None:
        raise SystemExit('anchor not found in %s: %s' % (archivo, inicio))
    if fin is None:
        return '%s:%d' % (archivo, a + 1)
    b = next((i for i in range(a, len(ls)) if re.search(fin, ls[i])), None)
    if b is None:
        raise SystemExit('end anchor not found in %s: %s' % (archivo, fin))
    return '%s:%d-%d' % (archivo, a + 1, b + 1)

# --------------------------------------------------------------------------------------------- SQL helpers
T_CONTRATO = 'BSCL.APV.BD::SCS.ContratoSAAPV'
T_PORCON = 'BSCL.APV.BD::SCS.PortafolioContratoSAAPV'
T_DISTR = 'BSCL.APV.BD::SCS.DistribucionInversion'
T_TRASPASO = 'BSCL.APV.BD::TRA.Traspaso'
T_DETORIGEN = 'BSCL.APV.BD::TRA.DetalleOrigenTraspaso'
T_DETTRASPASO = 'BSCL.APV.BD::TRA.DetalleTraspaso'
T_PORTAFOLIO = 'BSCL.Portafolio.BD::POR.Portafolio'
T_PERSONA = 'BSCL.SocioNegocio.BD::PER.Persona'
T_DOC = 'BSCL.SocioNegocio.BD::PER.DocumentoIdentidad'
T_CLIENTE = 'BSCL.SocioNegocio.BD::CLI.Cliente'
T_MANDATO = 'BSCL.Tesoreria.BD::PAC.MandatoPAC'
T_ASIGN = 'BSCL.APV.BD::REC.AsignacionContratoRecaudacion'
T_CARGA = 'BSCL.APV.BD::APV.CargaMasiva'
T_PERFIL = 'BSCL.APV.BD::APV.PerfilComercialAPV'
T_FOLIO = 'BSCL.APV.BD::SCS.FolioSuscripcion'
T_RELLAB = 'BSCL.SocioNegocio.BD::PER.RelacionLaboral'


def lit(v):
    if v is None:
        return 'NULL'
    if isinstance(v, bool):
        return 'TRUE' if v else 'FALSE'
    if isinstance(v, (int, float)):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


def ins(table, **cols):
    names = ', '.join('"%s"' % k for k in cols)
    vals = ', '.join(lit(v) for v in cols.values())
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (table, names, vals)


def seed(*stmts):
    out = []

    def plano(x):
        if isinstance(x, (list, tuple)):
            for y in x:
                plano(y)
        else:
            out.append(x)
    plano(stmts)
    return '\n'.join(out) + '\n'


def tabla(nombre, orden, columnas=None, enmascarar=None, excluir=None, expresiones=None):
    t = {'nombre': nombre, 'orden': orden}
    if columnas:
        t['columnas'] = columnas
    if enmascarar:
        t['enmascarar'] = enmascarar
    if excluir:
        t['excluir'] = excluir
    if expresiones:
        t['expresiones'] = expresiones
    return t


# --------------------------------------------------------------------------------------------- the case registry
def caso(num, servicio, slug, titulo, pina, entrada, seed_sql='', config=None, secuencias=None, colaboradores=None, tablas=None,
         enmascarar_colaboradores=None, reloj_real=False, metodo='call', sin_orden=None):
    """num: int (the id is APV-A-NNN); servicio: legacy class simple name; pina: 'File.java:line-range' of the branch the case pins."""
    cid = 'APV-A-%03d' % num
    for c in CASOS:
        if c['id'] == cid:
            raise SystemExit('duplicate case id ' + cid)
    CASOS.append(dict(id=cid, num=num, servicio=servicio, slug=slug, titulo=titulo, pina=pina, entrada=entrada, seed=seed_sql, config=config,
                      secuencias=secuencias, colaboradores=colaboradores, tablas=tablas or [], enmascararColaboradores=enmascarar_colaboradores,
                      relojReal=reloj_real, metodo=metodo, sinOrden=sin_orden))
    return cid


def escribir(raiz):
    cases = os.path.join(raiz, 'cases')
    os.makedirs(cases, exist_ok=True)
    esperados = set()
    for c in CASOS:
        d = os.path.join(cases, c['id'] + '_' + c['slug'])
        esperados.add(os.path.basename(d))
        os.makedirs(d, exist_ok=True)
        j = {'id': c['id'], 'titulo': c['titulo'], 'pina': c['pina'], 'servicio': c['servicio'], 'metodo': c['metodo']}
        j['entrada'] = c['entrada']
        if c['config']:
            j['config'] = c['config']
        if c['secuencias']:
            j['secuencias'] = c['secuencias']
        if c['colaboradores']:
            j['colaboradores'] = c['colaboradores']
        if c['enmascararColaboradores']:
            j['enmascararColaboradores'] = c['enmascararColaboradores']
        if c['relojReal']:
            j['relojReal'] = True
        if c['sinOrden']:
            j['sinOrden'] = c['sinOrden']
        j['tablas'] = c['tablas']
        with open(os.path.join(d, 'case.json'), 'w', encoding='utf-8', newline='\n') as f:
            json.dump(j, f, indent=2, ensure_ascii=False)
            f.write('\n')
        sp = os.path.join(d, 'seed.sql')
        if c['seed']:
            with open(sp, 'w', encoding='utf-8', newline='\n') as f:
                f.write(c['seed'])
        elif os.path.exists(sp):
            os.remove(sp)
    import shutil
    for d in os.listdir(cases):
        if d.startswith('APV-A-') and d not in esperados and os.path.isdir(os.path.join(cases, d)):
            shutil.rmtree(os.path.join(cases, d))   # a renamed or removed case: its directory (case.json, seed, golden) goes with it
    return esperados


# --------------------------------------------------------------------------------------------- fixtures (fake parties, fake documents)
def contrato(idc, folio, **kw):
    """A row of SCS.ContratoSAAPV (fake person/company data; the document numbers are fixtures, not real ones)."""
    d = dict(IdContratoSAAPV=idc, IdEmpresa=1, FolioContratoSAAPV=folio, IdPlan=1, NombrePlan='ABIERTO', FechaSuscripcion='2025-03-01',
             FechaCreacion='2025-03-01', FechaAprobacion='0001-01-01', FechaNotificacion='0001-01-01', FechaRecepcion='0001-01-01', Folio=folio,
             IdPersona=501, IdEmpleador=0, IdCliente=701, NombreCliente='ANA', ApellidoPaternoCliente='PEREZ', ApellidoMaternoCliente='SOTO',
             DocumentoIdentidadCliente='11111111-1', FechaNacimientoCliente='1980-05-10', EstadoCivil='SO', IdTipoGenero='F',
             IdPaisResidenciaCliente=44, IdCiudadCliente=10, IdComunaCliente=20, DireccionCliente='CALLE UNO', IdTipoCargoPersona=1,
             TelefonoCliente='', TelefonoComercialCliente='', TelefonoCelularCliente='+56911112222', EmailCliente='ana@example.invalid',
             RazonSocialEmpleador='', IdMedioRecaudacionTrabajador=1, IdEntidadRecaudadora=1, DocumentoIdentidadEmpleador='', DireccionComercialEmpleador='',
             IdComunaEmpleador=0, TelefonoEmpleador='', NombreEncargado='', EmailEncargado='', TelefonoEncargado='', CodigoPrevision=1, Pensionado='NO',
             Trabajador='INDEPENDIENTE', FormaDePago='DIRECTA', IdMedioRecaudacionFormaPago=1, DiaDescuento=0, Institucion='', ComisionTransferencia=0,
             CodigoTipoSolicitud='', NombreEntidadOrigen='', RutEntidadOrigen='', TipoDeAhorro='', FondoDeOrigen='', TipoTraspaso='', MontoTraspaso=0,
             UFTraspaso=0, PorcentajeTraspaso=0, IdEstadoEnvioNotificacion='NEV', IdEstadoSolicitud='PDA', IdEstado='VIG',
             IdCategoriaInversionista='MODERADO', IdPerfilComercial='PERFIL BASE', IdCargaMasiva=0, Canal='OPT', IdPortafolio=None, IdGlosa=0,
             DescripcionGlosa='')
    d.update(kw)
    return ins(T_CONTRATO, **d)


def persona(idp, doc, nombres='ANA', paterno='PEREZ', materno='SOTO', tipo='N', idc=None, **kw):
    """PER.Persona + PER.DocumentoIdentidad (predeterminado) [+ CLI.Cliente]."""
    out = [ins(T_PERSONA, IdPersona=idp, Nombres=nombres, ApellidoPaterno=paterno, ApellidoMaterno=materno, FechaNacimiento='1980-05-10',
               EstadoCivil='SO', Sexo='F', RazonSocial=kw.get('razon', ''), IdPaisResidencia=44, IdDocumentoIdentidad=idp + 5000,
               IdDireccionFisicaPrincipal=idp + 6000, IdPersonaEmpleador=kw.get('empleador', None), IdTelefonoPrincipal=idp + 7000, Tipo=tipo),
           ins(T_DOC, DocumentoIdentidad=doc, IdDocumentoIdentidad=idp + 5000, IdPersona=idp, Predeterminado='S')]
    if idc is not None:
        out.append(ins(T_CLIENTE, IdCategoriaInversionista='MODERADO', IdPersona=idp, IdCliente=idc, IdEstado=kw.get('estado', 'ACT'), Borrado='N',
                       IdPersonaEjecutivo=kw.get('ejecutivo', None)))
    return out


def portafolio(idpo, idcliente, matriz=1, estado='ACT', proposito='PROPOSITOAPV', **kw):
    return ins(T_PORTAFOLIO, CodigoPortafolio=kw.get('codigo', 'APV-A'), IdMatrizAgrupadorAPV=matriz, IdPortafolio=idpo, IdEstado=estado,
               IdEmpleadoEjecutivo=kw.get('ejecutivo', 335), IdPerfilComercialBaseOrigen=kw.get('perfil', 'PERFIL BASE'), IdCliente=idcliente,
               Proposito=proposito, IdContratoSAAPV=None)


def porcon(idpc, idpo, idc, origen='APV', estado='VIG', monto=100000, pago='IND', ini='2025-03', fin='9999-01', pct=0, uf=0):
    return ins(T_PORCON, IdPortafolioContratoSAAPV=idpc, IdPortafolio=idpo, IdContratoSAAPV=idc, IdEstado=estado, OrigenAhorro=origen,
               MontoPesos=monto, MontoUF=uf, PorcentajeRegimenImponible=pct, Pago=pago, PeriodoInicio=ini, PeriodoTermino=fin)


COLS_CONTRATO_ESTADO = ['IdContratoSAAPV', 'FolioContratoSAAPV', 'IdEstado', 'IdEstadoSolicitud', 'IdEstadoEnvioNotificacion', 'IdGlosa', 'DescripcionGlosa']
