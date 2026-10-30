"""BcsReplica oracle: shared library of the case generators (inputs only: the goldens are recorded by legacy-oracle/record.sh from the unmodified legacy).

A case is a directory cases/REP-<FAM>-nnn_<slug>/ with case.json (servicio, ref, entrada, colaboradores, fallos, tablas, reloj) and seed.sql. The numbering is assigned in
generation order, so a generator may be extended at its end only; make_cases.py wipes cases/ first (a renumbered case never keeps a stale golden).
"""
import csv
import datetime
import json
import pathlib
import re

BASE = pathlib.Path(__file__).resolve().parent
CASES = BASE / "cases"
CAT = BASE / "catalog"

T_WS = "BSCL.Plataforma.BD::PLA.Replica.WorkStack"
T_RES = "BSCL.Plataforma.BD::PLA.ResumenReplica"
T_POST = "BSCL.Replica.BD::EnvioPostPauta"
T_SEF = "BSCL.CuentaInversion.BD::CTA.SaldoFechaEfectiva"
T_SFM = "BSCL.CuentaInversion.BD::CTA.SaldoFechaMovimiento"
T_HLI = "BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion"
T_HOP = "BSCL.Custodia.BD::HIS.CustodiaFechaOperacion"
T_V_SEF = "BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoCuentaInversionFechaEfectivaAnalitica"
T_V_SFM = "BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoCuentaInversionFechaMovimientoAnalitica"
T_V_HOP = "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaFechaOperacionAnalitica"
T_V_HLI = "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaFechaLiquidacionAnalitica"

SEBRA13 = ["TBCPDTPC", "TBCPMSPC", "TBCTCIRF", "TBCTCURF", "TBCTCIRV", "TBCTCURV", "TBCTCIFM", "TBCTCUFM", "TBCTCTCL", "TBCTTRTP", "TBCTCIIF", "TBCTGTIA", "TBCTTRPT"]

_contadores = {}


def sql(v):
    if v is None:
        return "NULL"
    if isinstance(v, (int, float)):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


def tabla_q(nombre):
    return '"%s"' % nombre


def insertar(tabla, **cols):
    return "INSERT INTO %s (%s) VALUES (%s);" % (tabla_q(tabla), ", ".join('"%s"' % c for c in cols), ", ".join(sql(v) for v in cols.values()))


def sumar_dias(fecha, n):
    return (datetime.date.fromisoformat(fecha[:10]) + datetime.timedelta(days=n)).isoformat()


def dump_ws():
    return {"nombre": T_WS, "orden": '"IdStack"'}


def dump_res():
    return {"nombre": T_RES, "orden": '"Nombre", "FechaSistema", "UltimaActualizacion"', "columnas": ["Tipo", "Nombre", "Estado", "Detalle", "FechaSistema", "UltimaActualizacion"]}


def dump_post():
    return {"nombre": T_POST, "orden": '"FechaSistema", "Tabla", "Fecha"'}


SALDO_COLS = ["IdCuentaInversion", "IdCliente", "IdPortafolio", "FechaSaldo", "SaldoDisponible", "SaldoEnGarantia", "SaldoAPlazo", "SaldoRetiro", "SaldoRetenido", "UltimaOperacion",
              "IdMatrizAgrupador", "Checksum"]
CUST_COLS = ["IdEmpresa", "TipoFechaConsulta", "FechaCustodia", "IdCliente", "IdPortafolio", "IdInstrumento", "CantidadCustodia"]


def dump_sef():
    return {"nombre": T_SEF, "orden": '"FechaSaldo", "IdCuentaInversion"', "columnas": SALDO_COLS}


def dump_sfm():
    return {"nombre": T_SFM, "orden": '"FechaSaldo", "IdCuentaInversion"', "columnas": SALDO_COLS}


def dump_hli():
    return {"nombre": T_HLI, "orden": '"FechaCustodia", "IdCliente", "IdInstrumento"', "columnas": CUST_COLS}


def dump_hop():
    return {"nombre": T_HOP, "orden": '"FechaCustodia", "IdCliente", "IdInstrumento"', "columnas": CUST_COLS}


def saldo(tabla, fecha, idcta, cliente=10, portafolio=100, disp=1000, garantia=0, aplazo=0, retiro=0, retenido=0, ultima=5, matriz=1, checksum=None):
    return insertar(tabla, IdCuentaInversion=idcta, IdCliente=cliente, IdPortafolio=portafolio, FechaSaldo=fecha, SaldoDisponible=disp, SaldoEnGarantia=garantia, SaldoAPlazo=aplazo,
                    SaldoAPlazoPactos=0, SaldoRetiro=retiro, SaldoRetenido=retenido, UltimaOperacion=ultima, IdMatrizAgrupador=matriz, CodigoCuentaInversion="C%d" % idcta, IdMoneda="CLP",
                    Checksum=checksum)


def custodia(tabla, fecha, cliente, instrumento, cantidad=10, portafolio=100, empresa=1):
    return insertar(tabla, IdEmpresa=empresa, TipoFechaConsulta="OPE", FechaCustodia=fecha, IdCliente=cliente, IdPortafolio=portafolio, IdInstrumento=instrumento, CantidadCustodia=cantidad)


def _catalogo():
    cols = {}
    with open(CAT / "replica_columnas_tablas.csv", encoding="utf-8", newline="") as f:
        for r in csv.DictReader(f):
            cols.setdefault(r["TABLE_NAME"], []).append(r)
    claves = {}
    with open(CAT / "replica_claves.csv", encoding="utf-8", newline="") as f:
        for r in csv.DictReader(f):
            claves.setdefault(r["TABLE_NAME"], []).append((int(r["POSITION"]), r["COLUMN_NAME"]))
    return cols, claves


_COLS, _CLAVES = _catalogo()


def clave_tbc(t):
    return [c for _, c in sorted(_CLAVES["BSCL.Replica.BD::" + t])]


def tbc(t, cod_per, n, origen=False):
    """a row of the target table (origen False) or of its Sebra stand-in (origen True) of the table t: the key is filled from n, the other NOT NULL columns with a constant, the rest stays NULL"""
    destino = "BSCL.Replica.BD::" + t
    nombre = "BSCL.Replica.BD.Vistas::Sebra." + t if origen else destino
    valores = {}
    for r in _COLS[destino]:
        c = r["COLUMN_NAME"]
        en_clave = c in clave_tbc(t)
        if c == "COD_PER":
            valores[c] = cod_per
        elif en_clave or r["IS_NULLABLE"] == "FALSE":
            if r["DATA_TYPE_NAME"] in ("CHAR", "NVARCHAR", "VARCHAR"):
                valores[c] = ("K%d" % n if en_clave else "Z")[: int(r["LENGTH"])]
            else:
                valores[c] = n if en_clave else 0
    return insertar(nombre, **valores)


def dump_tbc(t):
    claves = clave_tbc(t)
    cols = ["COD_PER"] + [c for c in claves if c != "COD_PER"]
    return {"nombre": "BSCL.Replica.BD::" + t, "orden": ", ".join('"%s"' % c for c in cols), "columnas": cols}


def dump_tbc_todas():
    return [dump_tbc(t) for t in SEBRA13]


def semilla_tbc_periodo(tablas, antiguos, nuevos, origen_periodos):
    """for every table: `antiguos` = [(cod_per, n)] already in the target, `nuevos` = [(cod_per, n)] in the Sebra stand-in"""
    out = []
    for t in tablas:
        for cp, n in antiguos:
            out.append(tbc(t, cp, n))
        for cp, n in nuevos:
            out.append(tbc(t, cp, n, origen=True))
    return out


def empresa(pais=56):
    return {"defecto": {"IdPais": pais}}


def calendario(origen, habiles, pais_ods=None, contador_en=None, error_ods_en=None, error_fer_en=None, vacia_en=None, contador=1):
    """script of ObtenerDiaSiguiente.logica and Feriados.esDiaHabil for a chain of days: step i asks for the day after the previous one; habiles[i] is what Feriados answers for the
    calculated day. contador_en / error_ods_en / error_fer_en / vacia_en = index of the step whose answer is an error counter, a thrown error, a Feriados error, an empty date list."""
    ods, fer = [], []
    actual = origen
    for i, h in enumerate(habiles):
        sig = sumar_dias(actual, 1)
        cuando = {"0.p_fechas.0.fechaInicial": actual}
        if error_ods_en == i:
            ods.append({"cuando": cuando, "error": "Error al calcular el dia siguiente"})
        elif contador_en == i:
            ods.append({"cuando": cuando, "respuesta": {"P_OUT_ERRORESCONTADOR": contador, "p_out_fechas": []}})
        elif vacia_en == i:
            ods.append({"cuando": cuando, "respuesta": {"P_OUT_ERRORESCONTADOR": 0, "p_out_fechas": []}})
        else:
            ods.append({"cuando": cuando, "respuesta": {"P_OUT_ERRORESCONTADOR": 0, "p_out_fechas": [{"fechaInicial": actual, "numeroDiasInicial": 1, "numeroDias": 1, "fechaCalculada": sig, "idPais": pais_ods, "IdInterno": 0}]}})
        if error_fer_en == i:
            fer.append({"cuando": {"0": sig}, "error": "Error al consultar feriados"})
        else:
            fer.append({"cuando": {"0": sig}, "respuesta": bool(h)})
        actual = sig
    return {"ObtenerDiaSiguiente.logica": {"respuestas": ods}, "Feriados.esDiaHabil": {"respuestas": fer}}


def colab(pais=56, **cal):
    """collaborators of a process with a day calendar: ConsultarEmpresa answers the country, plus the calendar if given (origen + habiles)"""
    c = {"ConsultarEmpresa.logica": empresa(pais)}
    if "origen" in cal:
        c.update(calendario(**cal))
    return c


def config(valor=None, error=None, tipo="Long"):
    if error:
        return {"defecto": None, "respuestas": [{"error": error}]}
    return {"defecto": valor}


class Gen:
    def __init__(self, familia):
        self.familia = familia
        self.n = 0
        self.ids = []

    def caso(self, slug, servicio, entrada, ref, colaboradores=None, fallos=None, tablas=None, seed=None, reloj=None, esquemas_extra=None):
        self.n += 1
        id_ = "REP-%s-%03d_%s" % (self.familia, self.n, re.sub(r"[^A-Za-z0-9]+", "-", slug).strip("-").lower())
        d = CASES / id_
        d.mkdir(parents=True)
        cj = {"servicio": servicio, "ref": ref, "entrada": entrada}
        if colaboradores:
            cj["colaboradores"] = colaboradores
        if fallos:
            cj["fallos"] = fallos
        if tablas:
            cj["tablas"] = tablas
        if reloj:
            cj["reloj"] = reloj
        if esquemas_extra:
            cj["esquemasExtra"] = esquemas_extra
        (d / "case.json").write_text(json.dumps(cj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        if seed:
            (d / "seed.sql").write_text("\n".join(seed) + "\n", encoding="utf-8", newline="\n")
        self.ids.append(id_)
        return id_


def entrada_env(proceso, fecha="2026-03-04", empresa=1, **extra):
    p = {"p_IdEmpresa": empresa, "p_Proceso": proceso, "p_Parametros": {"FechaSistemaEjecucion": fecha}}
    p.update(extra)
    return p


def fallo(contiene, mensaje="fallo inyectado", ocurrencia=1):
    return {"contiene": contiene, "ocurrencia": ocurrencia, "mensaje": mensaje}
