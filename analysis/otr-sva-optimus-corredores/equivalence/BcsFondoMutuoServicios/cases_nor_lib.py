"""Helpers of the step 4A cases (CrearSolicitudAporteRescateNormal): case directories under cases/ named FMM-NOR-nn_slug (same layout as the pilots).

The case inputs are written here; the goldens (expected.json) are RECORDED by legacy-oracle/record.sh from the unmodified legacy, never by this script.
"""
import copy
import json
import pathlib

BASE = pathlib.Path(__file__).resolve().parent
CASES = BASE / "cases"

SERIE = '"BSCL.FondoMutuo.BD::FMM.SerieDeFondo"'
FONDO = '"BSCL.FondoMutuo.BD::FMM.Fondo"'
REGIMEN = '"BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"'
PARAM = '"BSCL.FondoMutuo.BD::FMM.Parametro"'
PLACONF = '"BSCL.Plataforma.BD::PLA.Configuracion"'
LOCK = '"BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate"'

TABLAS = [
    {"nombre": "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate", "orden": "\"IdSolicitudAporteRescate\""},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate", "orden": "\"IdBloqueoSolicitudAporteRescate\""},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.InterfazAgf", "orden": "\"IdInterfazAgf\""},
]

RESCATE = {
    "IdInterno": 1, "IdCliente": 100, "IdPortafolio": 500, "TipoSolicitud": "RES", "IdSerieFondo": "FM001A", "CalculoPorMontoCuota": "C",
    "EsPorMontoLiquido": "N", "Monto": 0, "NumeroCuotas": 10, "IdMoneda": "CLP", "IdMedioDePago": "CIN", "IdPreferenciaPagoPortafolio": 0,
    "Visado": "N", "SolicitudBatch": "N", "OrigenOperacion": "M", "FechaOperacion": "0001-01-01", "IdRegimenTributario": "GEN", "Glosa": "",
    "IndicadorMLT": "N", "IdSerieFondoDestino": "", "IdEmisorDestino": "", "IdAutorizadoOrigen": 0, "Porcentaje": 0, "FolioContrato": 0,
    "IdMonedaPago": "CLP",
}

APORTE = dict(RESCATE, TipoSolicitud="APO", CalculoPorMontoCuota="M", Monto=100000, NumeroCuotas=0)

SALDO_OK = {"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": [{"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 50000000}]}}}
SALDO_BAJO = {"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": [{"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 10}]}}}


def sol(base, **kw):
    d = copy.deepcopy(base)
    d.update(kw)
    return d


def upd(tabla, sets, donde="1=1"):
    s = ", ".join('"%s" = %s' % (k, lit(v)) for k, v in sets.items())
    return "UPDATE %s SET %s WHERE %s;" % (tabla, s, donde)


def ins(tabla, cols):
    return "INSERT INTO %s (%s) VALUES (%s);" % (tabla, ", ".join('"%s"' % k for k in cols), ", ".join(lit(v) for v in cols.values()))


def lit(v):
    if v is None:
        return "NULL"
    if isinstance(v, (int, float)):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


def param(nombre, valor):
    return upd(PARAM, {"ValorParametro": valor}, "\"NombreTipoParametro\" = '%s'" % nombre)


def caso(cid, ref, solicitudes, seed=None, colab=None, **cab):
    entrada = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": "N", "p_UsaReglasNegocio": "S",
               "p_SolicitudesAporteRescate": solicitudes, "p_FormaPagoSpot": []}
    entrada.update(cab)
    doc = {"servicio": "CrearSolicitudAporteRescateNormal", "ref": ref, "entrada": entrada, "tablas": TABLAS}
    if colab:
        doc["colaboradores"] = colab
    d = CASES / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    texto = "-- case seed on top of seed/base-fmm.sql (fake data)\n" + ("\n".join(seed) if seed else "SELECT 1;") + "\n"
    (d / "seed.sql").write_text(texto, encoding="utf-8", newline="\n")
