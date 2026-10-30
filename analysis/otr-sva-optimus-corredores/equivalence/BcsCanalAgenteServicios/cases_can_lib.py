"""BcsCanalAgenteServicios oracle: shared library of the case generators (inputs only: the goldens are recorded by legacy-oracle/record.sh from the unmodified legacy).

A case is a directory cases/CAN-<FAM>-nnn_<slug>/ with case.json (servicio, ref, entrada, colaboradores, fallos, tablas, esquemasExtra) and seed.sql. The numbering is assigned in generation order, so a
generator may be extended at its end only; make_cases.py wipes cases/ first (a renumbered case never keeps a stale golden).
"""
import csv
import json
import pathlib
import re

BASE = pathlib.Path(__file__).resolve().parent
CASES = BASE / "cases"
CAT = BASE / "catalog"

T_POR = "BSCL.Portafolio.BD::POR.Portafolio"
T_CLI = "BSCL.SocioNegocio.BD::CLI.Cliente"
T_PER = "BSCL.SocioNegocio.BD::PER.Persona"
T_DOC = "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
T_FAC = "BSCL.Facturacion.BD::FAC.Comprobante"
T_PROC = "BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso"
T_VISTA = "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"
T_CART_IN = "BSCL.CanalAgente.PRC::CartolaEntrada"
T_CLI_IN = "BSCL.CanalAgente.PRC::ClientesEntrada"

SIN_NULOS_PLANTILLA = None


def sql(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "TRUE" if v else "FALSE"
    if isinstance(v, (int, float)):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


def tabla_q(nombre):
    return '"%s"' % nombre


def insertar(tabla, **cols):
    return "INSERT INTO %s (%s) VALUES (%s);" % (tabla_q(tabla), ", ".join('"%s"' % c for c in cols), ", ".join(sql(v) for v in cols.values()))


def _catalogo():
    cols = {}
    with open(CAT / "canal_columnas_tablas.csv", encoding="utf-8", newline="") as f:
        for r in csv.DictReader(f):
            cols.setdefault(r["TABLE_NAME"], []).append(r)
    return cols


_COLS = _catalogo()


def fila(tabla, **dados):
    """INSERT of a row of a REAL table: the columns given plus every NOT NULL column that has no default, filled with a constant of its type (the other columns keep their default / NULL)"""
    valores = dict(dados)
    for r in _COLS[tabla]:
        c = r["COLUMN_NAME"]
        if c in valores or r["IS_NULLABLE"] != "FALSE" or r["DEFAULT_VALUE"]:
            continue
        t = r["DATA_TYPE_NAME"]
        if t in ("NVARCHAR", "VARCHAR"):
            valores[c] = "Z"
        elif t in ("SECONDDATE", "TIMESTAMP"):
            valores[c] = "2026-01-01 00:00:00"
        elif t == "DATE":
            valores[c] = "2026-01-01"
        elif t == "BLOB":
            valores[c] = "00"
        else:
            valores[c] = 0
    return insertar(tabla, **valores)


# ---------------------------------------------------------------------------------------------------------------------------------- the fictitious world of the seeds
def portafolio(id_, cliente, codigo, **extra):
    return fila(T_POR, IdPortafolio=id_, IdCliente=cliente, CodigoPortafolio=codigo, **extra)


def cliente(id_, persona, **extra):
    return fila(T_CLI, IdCliente=id_, IdPersona=persona, **extra)


def persona(id_, documento_id, **extra):
    return fila(T_PER, IdPersona=id_, IdDocumentoIdentidad=documento_id, **extra)


def documento(id_, persona_id, texto):
    return fila(T_DOC, IdDocumentoIdentidad=id_, IdPersona=persona_id, DocumentoIdentidad=texto)


def comprobante(id_, folio, dte=0, pdf=None):
    return fila(T_FAC, IdComprobante=id_, FolioComprobante=folio, IdRespuestaDTE=dte, RespuestaPdfDTE=pdf)


def mundo():
    """portfolio 100 (client 10, person 1, document 11.111.111-1), 200 (client 20, person 2, 22.222.222-2), 300 (client 30, person 3 WITHOUT document), 400 (client 40 WITHOUT person row);
    vouchers 5001 (folio 7001, DTE 77, pdf), 5002 (folio 7002, DTE 0, no pdf), 5003 (folio 7003, DTE 5, pdf), 5004 (folio 0). FolioComprobante is NOT NULL in HANA: a NULL folio cannot exist"""
    return [
        documento(11, 1, "11.111.111-1"), documento(12, 2, "22.222.222-2"),
        persona(1, 11), persona(2, 12), persona(3, None),
        cliente(10, 1), cliente(20, 2), cliente(30, 3),
        portafolio(100, 10, "PF-100"), portafolio(200, 20, "PF-200"), portafolio(300, 30, "PF-300"), portafolio(400, 40, "PF-400"),
        comprobante(5001, 7001, 77, "https://dte.example/77.pdf"), comprobante(5002, 7002, 0, None), comprobante(5003, 7003, 5, "https://dte.example/5.pdf"), comprobante(5004, 0, 0, None),
    ]


def dump_proc():
    return {"nombre": T_PROC, "orden": '"IdCargoAbonoMovimiento", "IdPortafolio", "IdComprobante"'}


# ---------------------------------------------------------------------------------------------------------------------------------- the collaborator script
def mov(id_, comprobante=None, portafolio=100, **extra):
    """a row of the cartola the double of ConsultarCartolaDeMovimiento answers: the columns the legacy reads plus the ones it ignores"""
    m = {"IdCargoAbonoMovimiento": id_, "FechaSistema": "2026-02-10", "IdCuentaInversion": 21151, "NombreCliente": "Cliente %d" % portafolio, "IdCliente": portafolio // 10, "IdPortafolio": portafolio,
         "IdMoneda": "CLP", "IdConcepto": "ABN", "NombreConcepto": "Abono %s" % id_, "FechaLiquidacionOperacion": "2026-02-12", "Cargo": 0, "Abono": 1500.5, "IdEstado": "VIG", "NombreEstado": "Vigente",
         "NombreTipoComprobante": "Comprobante de abono", "NombreTipoProducto": "Cuenta de inversion", "NombreTipoOperacion": "Abono", "NombreTipoMovimiento": "Abono", "SaldoSimulado": 10000,
         "IdComprobante": comprobante, "IdMatrizAgrupador": 0, "EsADC": "N", "Monto": 1500.5, "FolioComprobante": "NO-USAR", "IdRespuestaDTE": 999}
    m.update(extra)
    return m


def resp(movs, contador=0, errores=None, mensajes=None):
    """the answer of the cartola"""
    return {"P_OUT_CARTOLADEMOVIMIENTO": movs, "P_OUT_ERRORESCONTADOR": contador, "P_OUT_ERRORESDETALLE": errores or [], "P_OUT_MENSAJES": mensajes or []}


def cartola(respuesta=None, error=None, defecto=None):
    """the script of ConsultarCartolaDeMovimiento.logica: a fixed answer, or an error the collaborator throws"""
    if error:
        return {"ConsultarCartolaDeMovimiento.logica": {"respuestas": [{"error": error}]}}
    return {"ConsultarCartolaDeMovimiento.logica": {"defecto": respuesta}}


def entrada_h(portafolio=100, **extra):
    e = {"p_IdEmpresa": 1, "p_IdUsuario": 1646, "p_IdModulo": "CTA", "p_IdPortafolio": portafolio, "p_FechaDesde": "2026-02-01", "p_FechaHasta": "2026-02-28", "p_IdMoneda": "CLP"}
    e.update(extra)
    return e


def fallo(contiene, mensaje="fallo inyectado", ocurrencia=1):
    return {"contiene": contiene, "ocurrencia": ocurrencia, "mensaje": mensaje}


class Gen:
    def __init__(self, familia):
        self.familia = familia
        self.n = 0
        self.ids = []

    def caso(self, slug, servicio, entrada, ref, colaboradores=None, fallos=None, tablas=None, seed=None, esquemas_extra=None):
        self.n += 1
        id_ = "CAN-%s-%03d_%s" % (self.familia, self.n, re.sub(r"[^A-Za-z0-9]+", "-", slug).strip("-").lower())
        d = CASES / id_
        d.mkdir(parents=True)
        cj = {"servicio": servicio, "ref": ref, "entrada": entrada}
        if colaboradores:
            cj["colaboradores"] = colaboradores
        if fallos:
            cj["fallos"] = fallos
        if tablas:
            cj["tablas"] = tablas
        if esquemas_extra:
            cj["esquemasExtra"] = esquemas_extra
        (d / "case.json").write_text(json.dumps(cj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        if seed:
            (d / "seed.sql").write_text("\n".join(seed) + "\n", encoding="utf-8", newline="\n")
        self.ids.append(id_)
        return id_
