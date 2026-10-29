#!/usr/bin/env python3
"""Generates apv-a/schema-apv-a.sql: the H2 stand-in of the HANA catalog objects the APV family A legacy classes read or write.

The HANA DDL of most of these objects is NOT in the repository (APV/BD/Scripts/Create_Tables.sql is a partial, outdated subset: it lacks IdEmpresa,
TipoMovimiento, ... that the SQL resources use). The columns are therefore INFERRED from the SQL text the 19 classes run (resources under
cl/bcs/APV/sql plus the SQL literals of the Java sources): INSERT column lists, UPDATE ... SET columns, alias."Col" references. Types: first the
hand-written overrides below (taken from the legacy POJO field types, the `--TYPE` comments of the INSERT statements and Create_Tables.sql), then
name heuristics. HANA views are modelled as plain tables (the seeds insert the rows the view would return).
This is an ASSUMPTION, not a legacy fact: see the README ("Assumptions"). Re-run: python mkschema.py <workspaceRoot> <outFile>
Columns the heuristics cannot place are written NVARCHAR(500); a case that needs another type is a signal to extend OVERRIDES.
"""
import os
import re
import sys

WS = sys.argv[1]
OUT = sys.argv[2]
APV = WS + '/legacy/otr-sva-optimus-corredores/BSCL/APV/JAVA/BcsAPVServicios/src/cl/bcs/APV'
MY = """CrearContratoSAAPV GrabarContratoSAAPV ModificarContratoSAAPV CambiarEstadoContratosSAAPV CambiarEstadoEnvioNotificacionContratoSAAPV
CambiarEstadoRevocacionSAAPV CambiarAPlanActivoContratoSAAPV CambiarAPlanActivoContratoSAAPVWrapper ValidaContratoSAAPV ConsultarClienteContratoSAAPV
ObtenerFolioSuscripcion AsociarPerfilesAPV ModificarRelacionLaboral BusquedaContratoSAAPVRecaudacion AsignarContratoARecaudacion
ValidarYAsignarContratosARecaudaciones CargaMasivaArchivo ConsultarCustodiaPortafolioAPV ConsultarPreferenciaPagoPortafolio""".split()
# resources of other modules loaded by the family A classes (SocioNegocio / FondoMutuo): their SQL is scanned too
EXTRA_SQL = [
    WS + '/legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectDatosAPV.sql',
    WS + '/legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectIdPortafolio.sql',
    WS + '/legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectPrimerIdPortafolio.sql',
    WS + '/legacy/otr-sva-optimus-corredores/BSCL/SocioNegocio/JAVA/BcsSocioNegocioServicios/src/cl/bcs/socionegocio/per/sql/CrearPersonaActividaCargo_Update.sql',
]

TABLE_RE = re.compile(r'"(BSCL\.[A-Za-z]+\.BD(?:\.Vistas)?::[A-Za-z0-9_.]+)"')

sqls = []   # (origin, text)
for c in MY:
    java = open(APV + '/' + c + '.java', encoding='utf-8', errors='replace').read()
    java = java.split('public static void main')[0]
    for r in set(re.findall(r'cl/bcs/APV/sql/([A-Za-z_0-9]+\.sql)', java)):
        sqls.append((r, open(APV + '/sql/' + r, encoding='utf-8', errors='replace').read()))
    for lit in re.findall(r'"((?:[^"\\]|\\.)*BSCL\.(?:[^"\\]|\\.)*)"', java):
        t = lit.replace('\\"', '"')
        if re.search(r'(?i)\b(select|insert|update|delete)\b', t):
            sqls.append((c + '(inline)', t))
for p in EXTRA_SQL:
    sqls.append((os.path.basename(p), open(p, encoding='utf-8', errors='replace').read()))
# the SocioNegocio statements run by GrabarContratoSAAPV through the (scripted) collaborators are NOT scanned: the fakes do not touch the tables

columns = {}        # table -> ordered set of columns
seqs = set()


def add(table, col):
    columns.setdefault(table, [])
    if col not in columns[table]:
        columns[table].append(col)


def strip_comments(t):
    return re.sub(r'(?:/\*(?:[^*]|(?:\*+[^*/]))*\*+/)|(?://.*)|(?:--.*)', '', t)


for origin, raw in sqls:
    t = strip_comments(raw)
    for m in re.finditer(r'"([^"]+)"\.NEXTVAL', t):
        seqs.add(m.group(1))
    tables = TABLE_RE.findall(t)
    for tb in tables:
        columns.setdefault(tb, [])
    # alias map: "table" [AS] alias
    alias = {}
    for m in re.finditer(r'"(BSCL\.[A-Za-z]+\.BD(?:\.Vistas)?::[A-Za-z0-9_.]+)"\s+(?:AS\s+)?(?!ON\b|WHERE\b|LEFT\b|INNER\b|JOIN\b|SET\b|VALUES\b|GROUP\b|ORDER\b|AND\b|FROM\b|CROSS\b|RIGHT\b|FULL\b|OUTER\b|UNION\b|USING\b)"?([A-Za-z_][A-Za-z_0-9]*)"?', t, re.I):
        alias[m.group(2).upper()] = m.group(1)
    for m in re.finditer(r'(?<![A-Za-z0-9_"])"?([A-Za-z_][A-Za-z_0-9]*)"?\."([A-Za-z0-9_]+)"', t):
        a = m.group(1).upper()
        if a in alias:
            add(alias[a], m.group(2))
    distinct = sorted(set(tables))
    if len(distinct) == 1:
        tb = distinct[0]
        for m in re.finditer(r'"([A-Za-z][A-Za-z0-9_]*)"', t):
            col = m.group(1)
            if col.startswith('BSCL.') or '::' in col:
                continue
            # keep only identifiers used as column names (not string aliases of a select list: those are rare in single-table statements)
            if re.search(r'\bAS\s+"' + re.escape(col) + '"', t) and not re.search(r'[,(\s]"' + re.escape(col) + r'"\s*(=|,|FROM|IN\b|\)|WHERE|$)', t):
                continue
            add(tb, col)

# columns the static scan cannot attribute to a table (unqualified columns inside sub-selects of multi-table statements)
EXTRA_COLUMNS = {
    'BSCL.SocioNegocio.BD::PER.Telefono': ['IdTelefono', 'IdPersona', 'IdTipoTelefono', 'CodigoProposito', 'Borrado', 'Numero'],
    'BSCL.SocioNegocio.BD::PER.Persona': ['IdProfesion'],
    'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica': ['IdDireccionFisica', 'IdPersona', 'IdEstado', 'IdCiudad', 'IdPais', 'IdDivisionPais', 'NombreCiudad', 'NombrePais',
                                                                         'PaisNombreDivisionPais'],
    'BSCL.Plataforma.BD::AUT.Evento': ['IdEvento', 'IdEstado', 'NumeroOperacionConcepto', 'IdCliente', 'IdModulo', 'FechaHoraRegistro'],
    # read by the inline SQL of the authorization hook AutorizacionCambiarAPlanActivoContratoSAAPV (a Java literal that the scan above does not cover)
    'BSCL.Plataforma.BD::AUT.EventoDetalle': ['IdEvento', 'NombreCampo', 'TipoDato', 'Valor', 'Clasificacion'],
}
for tb, cs in EXTRA_COLUMNS.items():
    for c in cs:
        add(tb, c)

# statements that INSERT ... SELECT without a column list rely on the column ORDER of the HANA table: the order of the SELECT list of
# CreaDireccionPropositoAPV.sql is the table order (NEXTVAL id first, 'PREV' in the position of CodigoProposito)
ORDEN = {
    'BSCL.SocioNegocio.BD::PER.DireccionFisica': ['IdDireccionFisica', 'Calle', 'Numero', 'Departamento', 'Sector', 'CodigoPostal', 'IdCiudad', 'CodigoProposito',
                                                  'Comentario', 'IdEstado', 'FechaIngreso', 'FechaCreacion', 'FechaModificacion', 'IdPersona', 'IdEstadoAutorizacion',
                                                  'IdCarga', 'IdOrigen', 'Borrado', 'IdUsuarioCreador', 'IdDivisionPais', 'Predeterminado', 'CodigoCompuesto'],
}
for tb, cs in ORDEN.items():
    columns[tb] = [c for c in cs] + [c for c in columns.get(tb, []) if c not in cs]

# ------------------------------------------------------------------------------------------------ types
STRING_IDS = {'IdEstado', 'IdEstadoSolicitud', 'IdEstadoEnvioNotificacion', 'IdEstadoAutorizacion', 'IdTipoGenero', 'IdTipoTelefono',
              'IdTipoDocumentoIdentidad', 'IdTipoOperacion', 'IdTipoEvento', 'IdModulo', 'IdPerfilComercial', 'IdPerfilComercialBase',
              'IdPerfilComercialBaseOrigen', 'IdPerfilComercialAbierto', 'IdPerfilComercialActivo', 'IdPerfilCobros', 'IdCategoriaInversionista',
              'IdMoneda', 'IdSerieFondo', 'IdPropositoPortafolio', 'IdTipoCliente', 'IdClasificacionTipoInstrumento', 'IdTipoInstrumento',
              'IdRegimenTriburario', 'IdTipoDeposito', 'IdPerfilProductos', 'IdPerfilLimites', 'IdPerfilCondicionesOperativas'}
STRING_NAMES = {'Codigo', 'Nombre', 'Estado', 'Tipo', 'Pago', 'Producto', 'Regimen', 'Descripcion', 'Canal', 'Folio', 'Proposito', 'Borrado', 'Sexo',
                'Numero', 'Calle', 'Direccion', 'Nombres', 'ApellidoPaterno', 'ApellidoMaterno', 'RazonSocial', 'EstadoCivil', 'Predeterminado',
                'CodigoPostal', 'Comentario', 'Departamento', 'Sector', 'Anexo', 'Nemotecnico', 'Operacion', 'CodigoProposito', 'RestringeAporte',
                'FormaDePago', 'Institucion', 'Trabajador', 'Pensionado', 'CierreSolicitud', 'TipoMovimiento', 'RazonRetiro', 'TipoTraspaso',
                'TipoDeAhorro', 'FondoDeOrigen', 'Fondo', 'TieneDetalle', 'Asignado', 'GrupoIndicador', 'CodigoTipoIndicador', 'RutEmpleador',
                'RutCliente', 'TipoRegimen', 'PeriodoInicio', 'PeriodoTermino', 'IdSerieFondo', 'FolioSAAPV', 'EsReapertura', 'EmailCliente'}
DECIMALS = ('Monto', 'UF', 'Porcentaje', 'Comision', 'Saldo', 'Cantidad', 'Tasa', 'Precio', 'RecMonto', 'IngresoMensual')
BIGINT_NAMES = {'CodigoPrevision', 'DiaDescuento', 'Folio_Numerico', 'Codigo_Num'}

OVERRIDES = {
    # global overrides by column name
    '*': {
        'IdEstado': 'NVARCHAR(10)', 'FolioContratoSAAPV': 'NVARCHAR(50)', 'Folio': 'NVARCHAR(50)', 'DocumentoIdentidad': 'NVARCHAR(40)',
        'DocumentoIdentidadCliente': 'NVARCHAR(40)', 'DocumentoIdentidadEmpleador': 'NVARCHAR(40)', 'Institucion': 'NVARCHAR(100)',
        'CodigoPrevision': 'BIGINT', 'DiaDescuento': 'BIGINT', 'PeriodoInicio': 'NVARCHAR(7)', 'PeriodoTermino': 'NVARCHAR(7)',
        'Numero': 'NVARCHAR(40)', 'FolioSAAPV': 'NVARCHAR(50)', 'Codigo': 'NVARCHAR(50)', 'IdSerieFondo': 'NVARCHAR(40)',
        'CodigoMedioRecaudacion': 'NVARCHAR(50)', 'NumeroOperacionConcepto': 'NVARCHAR(50)',
        'IdGlosa': 'BIGINT', 'DescripcionGlosa': 'NVARCHAR(500)', 'ValorParametro': 'NVARCHAR(100)', 'TipoParametro': 'NVARCHAR(100)',
        'FechaHoraRegistro': 'TIMESTAMP', 'RecMonto': 'DECIMAL(26,8)', 'MontoRecaudacion': 'DECIMAL(26,8)',
        'IdTipoCargoPersona': 'BIGINT', 'FechaNacimiento': 'DATE', 'IdPlanTipoInversion': 'BIGINT', 'IdProductoAgrupador': 'BIGINT',
        'IdEntidadRecaudadora': 'BIGINT', 'IdMedioRecaudacion': 'BIGINT', 'IdMedioDePago': 'NVARCHAR(10)', 'IdTipoOperacion': 'NVARCHAR(20)',
        'Estado': 'NVARCHAR(10)', 'FolioRecaudacion': 'BIGINT', 'FechaDeposito': 'DATE', 'NombrePais': 'NVARCHAR(100)', 'PaisNombreDivisionPais': 'NVARCHAR(100)',
        'IdDocumentoIdentidad': 'BIGINT', 'IdTelefonoPrincipal': 'BIGINT', 'IdDireccionFisicaPrincipal': 'BIGINT',
        'IdPaisResidencia': 'BIGINT', 'IdEstadoPortafolio': 'NVARCHAR(10)', 'IdEstadoNuevo': 'NVARCHAR(10)', 'IdEstadoContrato': 'NVARCHAR(10)',
        'IdEstadoCliente': 'NVARCHAR(10)', 'IdProfesion': 'BIGINT', 'IdTipoEntidad': 'NVARCHAR(50)', 'IdRelacionPersona': 'BIGINT', 'CantidadRegistros': 'BIGINT', 'IdEvento': 'BIGINT', 'CodigoAgrupador': 'NVARCHAR(100)', 'NombrePlan': 'NVARCHAR(100)', 'NombreAgrupador': 'NVARCHAR(100)',
    },
    'BSCL.APV.BD::REC.AsignacionContratoRecaudacion': {'FechaCreacion': 'TIMESTAMP', 'FechaModificacion': 'TIMESTAMP'},
    'BSCL.APV.BD::GBL.PortafoliosCambioPlanActivo': {'IdPortafolio': 'BIGINT'},
    'BSCL.APV.BD::SCS.FolioSuscripcion': {'Canal': 'NVARCHAR(100)', 'Folio': 'BIGINT'},
    'BSCL.APV.BD::APV.CargaMasiva': {'FechaCarga': 'DATE', 'IdTipoCarga': 'BIGINT', 'IdUsuario': 'BIGINT'},
    'BSCL.APV.BD::APV.Estado': {'IdEstado': 'BIGINT', 'Codigo': 'NVARCHAR(40)'},
    'BSCL.Plataforma.BD::PLA.Estado': {'IdEstado': 'NVARCHAR(10)'},
    'BSCL.APV.BD::REC.Cliente': {'IdRegimenTriburario': 'NVARCHAR(50)', 'IdEstado': 'NVARCHAR(10)'},
    'BSCL.APV.BD::TRA.Traspaso': {'FechaIngreso': 'DATE', 'FechaEgreso': 'DATE', 'FechaModificacion': 'DATE', 'FechaSuscripcion': 'DATE',
                                  'CierreSolicitud': 'DATE'},
    'BSCL.Portafolio.BD::POR.Portafolio': {'IdEstado': 'NVARCHAR(10)', 'IdMatrizAgrupadorAPV': 'BIGINT'},
    'BSCL.SocioNegocio.BD::CLI.Cliente': {'IdEstado': 'NVARCHAR(10)'},
    'BSCL.SocioNegocio.BD::PER.RelacionLaboral': {'CodigoTipoCargoEmpleado': 'NVARCHAR(20)'},
    'BSCL.SocioNegocio.BD::PER.Persona': {'Tipo': 'NVARCHAR(3)', 'FechaNacimiento': 'DATE'},
    'BSCL.SocioNegocio.BD::PER.Telefono': {'IdTipoTelefono': 'NVARCHAR(10)'},
    'BSCL.Plataforma.BD::INS.Instrumento': {'Nemotecnico': 'NVARCHAR(40)'},
    'BSCL.APV.BD::SCS.ContratoSAAPV': {'FechaSuscripcion': 'DATE', 'IdEstado': 'NVARCHAR(10)'},
}


def sqltype(table, col):
    ov = OVERRIDES.get(table, {})
    if col in ov:
        return ov[col]
    g = OVERRIDES['*']
    if col in g:
        return g[col]
    if col in STRING_IDS:
        return 'NVARCHAR(100)'
    if col in STRING_NAMES:
        return 'NVARCHAR(500)'
    if col.startswith('Fecha'):
        return 'DATE'
    if col.startswith(DECIMALS):
        return 'DECIMAL(26,8)'
    if col.startswith('Id') or col.startswith('Dia'):
        return 'BIGINT'
    if col.startswith(('Nombre', 'Documento', 'Telefono', 'Email', 'Direccion', 'Rut', 'Codigo', 'Apellido', 'Razon', 'Descripcion', 'Estado', 'Tipo', 'Origen')):
        return 'NVARCHAR(500)'
    return 'NVARCHAR(500)'


# ------------------------------------------------------------------------------------------------ output
head = """-- H2 (MODE=LEGACY) stand-in for the SAP HANA objects read/written by the APV "family A" legacy services (contratos SAAPV):
-- CrearContratoSAAPV, GrabarContratoSAAPV, ModificarContratoSAAPV, CambiarEstado*, ValidaContratoSAAPV, ... Quoted names are the exact HANA catalog
-- names so the production SQL resources (cl/bcs/APV/sql/*.sql) run unmodified (through support/H2Hana.java). HANA views are modelled as tables: the seeds
-- insert the rows the view would return.
-- GENERATED by legacy-oracle/tools/mkschema.py: the column list of each object is INFERRED from the SQL the 19 classes run (the HANA DDL is not in the
-- repository), types from overrides (legacy POJO field types / INSERT comments) and name heuristics. Assumption, not legacy fact (see the README).
-- No primary keys or constraints are modelled (the legacy relies on the HANA ones only where a case says so).
-- This file is executed by BOTH the legacy oracle runner and the modern tests.

CREATE TABLE DUMMY (X VARCHAR(1));
INSERT INTO DUMMY VALUES ('X');

CREATE DOMAIN SECONDDATE AS TIMESTAMP;

CREATE ALIAS TO_NVARCHAR FOR "cl.bcs.apv.familia_a.support.H2Hana.toNvarchar";
CREATE ALIAS TO_VARCHAR FOR "cl.bcs.apv.familia_a.support.H2Hana.toVarchar";
CREATE ALIAS TO_BIGINT FOR "cl.bcs.apv.familia_a.support.H2Hana.toBigint";
CREATE ALIAS TO_DECIMAL FOR "cl.bcs.apv.familia_a.support.H2Hana.toDecimal";
CREATE ALIAS TO_DATE FOR "cl.bcs.apv.familia_a.support.H2Hana.toDate";
CREATE ALIAS TO_TIMESTAMP FOR "cl.bcs.apv.familia_a.support.H2Hana.toTimestamp";
CREATE ALIAS ADD_SECONDS FOR "cl.bcs.apv.familia_a.support.H2Hana.addSeconds";
CREATE ALIAS HANA_UTCNOW FOR "cl.bcs.apv.familia_a.support.H2Hana.utcNow";
CREATE ALIAS HANA_NEXTVAL FOR "cl.bcs.apv.familia_a.support.H2Hana.nextVal";
CREATE ALIAS ADD_DAYS FOR "cl.bcs.apv.familia_a.support.H2Hana.addDays";

-- sequences read directly by the SQL (fixed start values: ids of rows created by a case are part of the goldens)
"""
lines = [head]
start = 8000
for sname in sorted(seqs):
    lines.append('CREATE SEQUENCE "%s" START WITH %d;' % (sname, start))
    start += 100
lines.append('')
for tb in sorted(columns):
    cols = columns[tb]
    if not cols:
        cols = ['Id']
    lines.append('CREATE TABLE "%s" (' % tb)
    lines.append(',\n'.join('  "%s" %s' % (c, sqltype(tb, c)) for c in cols))
    lines.append(');')
    lines.append('')
open(OUT, 'w', encoding='utf-8', newline='\n').write('\n'.join(lines))
print('tables:', len(columns), 'sequences:', len(seqs), '->', OUT)
