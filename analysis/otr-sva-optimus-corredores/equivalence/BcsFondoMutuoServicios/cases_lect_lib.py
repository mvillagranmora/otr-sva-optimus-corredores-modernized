"""Shared helpers of the F3 W1 fronts 2-4 (read only queries): ConsultarFondoMutuoReinversion (FMR), ConsultarInstrumentosFondosMutuos (IFM),
ConsultarValorDolarFondoMutuo (VDO). Case INPUTS only (case.json, seed.sql) and the seed/base-<cls>.sql files; the goldens (expected.json) are RECORDED by
legacy-oracle/record_<cls>.sh from the unmodified legacy, never by these scripts.

Each class has its OWN case root (cases-fmr/, cases-ifm/, cases-vdo/) so that no other oracle script (record.sh, record_rre.sh, record_fsa.sh, ...) ever reads
them, and its own OracleRunnerLect (runner-lect/). Nothing shared is edited.

The F1 H2 schema (schema-types.sql / schema-fmm.sql, generated from the columns the F1/F2 SQL touch) lacks a few columns and three views that these SQL read:
base-fmr.sql / base-ifm.sql add them first (ALTER ... ADD COLUMN IF NOT EXISTS with the type of the HANA catalog CSV, REAL; the three views are H2 tables
whose columns are ASSUMED (only the ones the SQL names; the catalog CSVs of hana_qa_results do not list these views' columns)).
"""
import json
import pathlib
import sys

BASE = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(BASE))
from cases3a_lib import (lit, update, insert, parametro, config_plataforma, clonar_serie)  # noqa: E402,F401

FONDO = '"BSCL.FondoMutuo.BD::FMM.Fondo"'
SERIE = '"BSCL.FondoMutuo.BD::FMM.SerieDeFondo"'
VCUOTA = '"BSCL.FondoMutuo.BD::FMM.ValorCuota"'
REGIMEN = '"BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"'
REGSERIE = '"BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo"'
TIPOCAMBIO = '"BSCL.FondoMutuo.BD::FMM.ValorTipoCambio"'
V_EMISOR = '"BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor"'
V_CATEGORIA = '"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCategoriaPerfilFondoMutuo"'
V_CONFREG = '"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionRegimenFondoMutuo"'
DICC = '"BSCL.Plataforma.BD::PLA.Diccionario"'
PARAM = '"BSCL.FondoMutuo.BD::FMM.Parametro"'

DDL = [
    "-- columns of the HANA catalog (fmm_columnas_tablas.csv: REAL) that the F1 H2 schema does not declare; DEFAULTs only so that the rows of seed/base-fmm.sql survive",
    'ALTER TABLE %s ADD COLUMN IF NOT EXISTS "InstrumentoOWP" NVARCHAR(1);' % SERIE,
    "ALTER TABLE %s ADD COLUMN IF NOT EXISTS \"FechaCierre\" DATE DEFAULT DATE '0001-01-01' NOT NULL;" % VCUOTA,
    "ALTER TABLE %s ADD COLUMN IF NOT EXISTS \"Descripcion\" NVARCHAR(100) DEFAULT '' NOT NULL;" % REGIMEN,
    "-- the three views the SQL read (H2 tables seeded by the cases; columns ASSUMED, only the ones the SQL names)",
    'CREATE TABLE IF NOT EXISTS %s ("IdEmisor" NVARCHAR(40) NOT NULL, "NombreCompleto" NVARCHAR(200));' % V_EMISOR,
    'CREATE TABLE IF NOT EXISTS %s ("IdCategoriaPerfilFondoMutuo" NVARCHAR(40) NOT NULL, "NombreCategoriaPerfilFondoMutuo" NVARCHAR(100));' % V_CATEGORIA,
    'CREATE TABLE IF NOT EXISTS %s ("IdRegimenFondoMutuoOrigen" NVARCHAR(40), "IdRegimenFondoMutuoDestino" NVARCHAR(40));' % V_CONFREG,
    "-- HANA TO_VARCHAR(date, 'YYYY-MM-DD'): the oracle's own H2 function (cl.bcs.oracle.H2Fn.toNvarchar, also in src/test/java of the module)",
    'CREATE ALIAS IF NOT EXISTS TO_VARCHAR FOR "cl.bcs.oracle.H2Fn.toNvarchar";',
]

_REG_COLS = ["IdRegimenFondoMutuo", "GrupoRegimen", "PropositoSerieRelacionado", "PermiteOtrosGrupos", "ValidaPortafolioCliente", "ValidaPortafolioAGF",
             "PermiteRescate", "TipoCarteraNormativa", "PermiteCambioGrupo", "RescateConMandato", "AceptaMandatoEnReinversion", "PermiteAporte",
             "PermiteTraspaso", "ValidaMontoMinimoInstrumento", "IdEstado"]


def regimen(id_reg, descripcion, rescate_mandato="N", acepta_mandato=""):
    v = {"IdRegimenFondoMutuo": id_reg, "GrupoRegimen": "DC", "PropositoSerieRelacionado": "", "PermiteOtrosGrupos": "", "ValidaPortafolioCliente": "",
         "ValidaPortafolioAGF": "", "PermiteRescate": "S", "TipoCarteraNormativa": "", "PermiteCambioGrupo": "", "RescateConMandato": rescate_mandato,
         "AceptaMandatoEnReinversion": acepta_mandato, "PermiteAporte": "S", "PermiteTraspaso": "N", "ValidaMontoMinimoInstrumento": "S", "IdEstado": "ACT"}
    ins = insert(REGIMEN, v)
    return [ins, update(REGIMEN, {"Descripcion": descripcion}, '"IdRegimenFondoMutuo" = %s' % lit(id_reg))]


def reg_serie(serie, reg, estado="ACT", empresa=1):
    return insert(REGSERIE, {"IdSerieFondo": serie, "IdRegimenFondoMutuo": reg, "IdEmpresa": empresa, "IdEstado": estado})


def valor_cuota(serie, valor, fecha):
    return "DELETE FROM %s WHERE \"IdSerieFondo\" = %s;\n%s" % (VCUOTA, lit(serie), insert(VCUOTA, {"IdSerieFondo": serie, "ValorCuota": valor, "FechaCierre": fecha}))


# series of the FAKE universe of FMR and IFM:  id -> (fund id, fund overrides, series overrides, regimes (id, estado), (valor cuota, fecha cierre) | None)
# fund overrides: IdEmisor, FondoExtranjero, IdMoneda, IdCategoriaPerfilFondoMutuo, BloqueoAporte, IdEmpresa, NombreFondo, Nemotecnico
# series overrides: NombreSerieFondo, CodigoSerie, IdFamiliaFondo, IdEstado, IdEmpresa, MontoMinimo, MontoMaximo, CobraComision, InstrumentoOWP, EsAPV,
#                   RestringeAporte, MontoPatrimonioEmisor, DecimalesValorCuota
def _f(emisor="AGF1", ext="N", mon="CLP", cat="CAT1", bloqueo="N", empresa=1, nombre=None, nemo=None):
    return {"IdEmisor": emisor, "FondoExtranjero": ext, "IdMoneda": mon, "IdCategoriaPerfilFondoMutuo": cat, "BloqueoAporte": bloqueo, "IdEmpresa": empresa,
            "NombreFondo": nombre, "Nemotecnico": nemo}


def _s(nombre, cod, fam="FAM1", estado="ACT", empresa=1, minimo=1000, maximo=5000000, comision="S", owp="N", apv="N", restringe="N", patrimonio=100000, dec=4):
    return {"NombreSerieFondo": nombre, "CodigoSerie": cod, "IdFamiliaFondo": fam, "IdEstado": estado, "IdEmpresa": empresa, "MontoMinimo": minimo, "MontoMaximo": maximo,
            "CobraComision": comision, "InstrumentoOWP": owp, "EsAPV": apv, "RestringeAporte": restringe, "MontoPatrimonioEmisor": patrimonio, "DecimalesValorCuota": dec}


SERIES = {
    "S001": ("61", _f("AGF1", "N", "CLP", "CAT1", nombre="Fondo Renta Uno", nemo="FRU01"), _s("Serie A Renta Uno", "A"), [("18Q", "ACT"), ("18T", "ACT")], (1234.5678, "2026-03-01")),
    "S002": ("61", None, _s("Serie B Renta Uno", "B", minimo=2500, maximo=900000, comision="N", patrimonio=250000.5, dec=2), [("18Q", "ACT")], (987.65, "2026-02-27")),
    "S003": ("62", _f("AGF2", "N", "CLP", "CAT2", nombre="Fondo Mixto Dos", nemo="FMD02"), _s("Serie Unica Mixto Dos", "U", fam="FAM2", minimo=0, maximo=0, patrimonio=0), [("57BIS", "ACT")], None),
    "S004": ("63", _f("EXT1", "S", "USD", "CAT2", nombre="Fondo Externo Tres", nemo="FET03"), _s("Serie USD Externo", "X", fam="FAM3", minimo=10, maximo=1000000, dec=6), [("18Q", "ACT"), ("18T", "ACT")], (10.5, "2026-03-02")),
    "S005": ("64", _f("AGF1", "N", "CLP", "CAT1", bloqueo="S", nombre="Fondo Bloqueado Cuatro", nemo="FBC04"), _s("Serie Fondo Bloqueado", "A"), [("18Q", "ACT")], (500.25, "2026-03-01")),
    "S006": ("65", _f("AGF2", "N", "CLP", "CAT1", nombre="Fondo Restringido Cinco", nemo="FRC05"), _s("Serie Aporte Restringido", "A", restringe="S"), [("18Q", "ACT")], (700.5, "2026-03-01")),
    "S007": ("66", _f("AGF1", "N", "CLP", "CAT3", nombre="Fondo Inactivo Seis", nemo="FIS06"), _s("Serie Inactiva", "A", estado="INA"), [("18Q", "ACT")], (300.1, "2026-03-01")),
    "S008": ("67", _f("AGF3", "X", "CLP", "CAT1", nombre="Fondo OWP APV Siete", nemo="FOA07"), _s("Serie OWP APV", "A", fam="FAM2", owp="S", apv="S", comision="N"), [("18T", "ACT")], (150, "2026-03-01")),
    "S009": ("68", _f("AGF2", "N", "CLP", "CAT2", nombre="Fondo Regimen Inactivo Ocho", nemo="FRI08"), _s("Serie Regimen Inactivo", "A"), [("18Q", "INA"), ("GEN", "ACT")], (222.22, "2026-03-01")),
    "S010": ("69", _f("AGF1", "N", "CLP", "CAT1", nombre="Fondo Sin Regimen Nueve", nemo="FSR09"), _s("Serie Sin Regimen", "A"), [], (111.11, "2026-03-01")),
    "S011": ("70", _f("AGF1", "N", "CLP", "CAT1", empresa=2, nombre="Fondo Otra Empresa Diez", nemo="FOE10"), _s("Serie Otra Empresa", "A", empresa=2), [("18Q", "ACT")], (50, "2026-03-01")),
    "S012": ("71", _f("AGF2", "N", "USD", "CAT1", nombre="Fondo Ninguno Once", nemo="FNI11"), _s("Serie Regimen NINGUNO", "A", fam="FAM1"), [("NINGUNO", "ACT")], (80.8, "2026-03-01")),
}


def universo_series():
    """Rows of the FAKE universe of FMR and IFM (on top of seed/base-fmm.sql: fund 10, series FM001A national CLP issuer AGF1, regime GEN)."""
    s = ["-- regimes (GEN comes from seed/base-fmm.sql: RescateConMandato N, AceptaMandatoEnReinversion '')"]
    s.append(update(REGIMEN, {"Descripcion": "Regimen General"}, '"IdRegimenFondoMutuo" = \'GEN\''))
    s += regimen("18Q", "Articulo 18 Q", "S", "")
    s += regimen("18T", "Articulo 18 T", "N", "S")
    s += regimen("57BIS", "Articulo 57 bis", "N", "N")
    s += regimen("NINGUNO", "Sin regimen", "N", "N")
    s.append("-- views: issuers (AGF3 is deliberately absent: NombreEmisor '' by COALESCE), profile categories (CAT3 absent), regime destinations of a reinversion")
    for e, n in (("AGF1", "Administradora Uno S.A."), ("AGF2", "Administradora Dos S.A."), ("EXT1", "Emisor Externo Uno")):
        s.append(insert(V_EMISOR, {"IdEmisor": e, "NombreCompleto": n}))
    for c, n in (("CAT1", "Conservador"), ("CAT2", "Moderado")):
        s.append(insert(V_CATEGORIA, {"IdCategoriaPerfilFondoMutuo": c, "NombreCategoriaPerfilFondoMutuo": n}))
    for o, d in (("18Q", "18T"), ("18T", "57BIS"), ("GEN", "18Q")):
        s.append(insert(V_CONFREG, {"IdRegimenFondoMutuoOrigen": o, "IdRegimenFondoMutuoDestino": d}))
    s.append("-- base series FM001A: fund 10 -> emisor AGF1, category CAT1; a value of quota and the instrument flags")
    s.append(update(FONDO, {"IdCategoriaPerfilFondoMutuo": "CAT1"}, '"IdFondo" = \'10\''))
    s.append(update(SERIE, {"IdFamiliaFondo": "FAM1", "InstrumentoOWP": "N", "CobraComision": "N"}, '"IdSerieFondo" = \'FM001A\''))
    s.append(valor_cuota("FM001A", 1500.1234, "2026-03-02"))
    for sid, (fondo_id, fondo, serie, regs, vc) in SERIES.items():
        fo = {k: v for k, v in (fondo or {}).items() if v is not None}
        se = {k: v for k, v in serie.items()}
        s += clonar_serie(sid, fondo_id, fondo=fo if fondo is not None else False, serie=se)
        for r, estado in regs:
            s.append(reg_serie(sid, r, estado, empresa=se["IdEmpresa"]))
        if vc:
            s.append(valor_cuota(sid, vc[0], vc[1]))
    s.append("-- FM001A keeps its single GEN regime (seed/base-fmm.sql)")
    return s


def escribir_caso(raiz, prefijo, contador, servicio, esquema, slug, ref, ent, seed=None, colaboradores=None, tablas=None, **cab):
    """Writes <raiz>/<prefijo>-nn_slug/{case.json, seed.sql} and returns the id. nn = order of the call."""
    contador["n"] += 1
    cid = "%s-%02d_%s" % (prefijo, contador["n"], slug)
    doc = {"servicio": servicio, "ref": ref, "esquemasExtra": [esquema], "entrada": ent, "tablas": tablas or []}
    doc.update(cab)
    if colaboradores:
        doc["colaboradores"] = colaboradores
    d = raiz / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    texto = "-- case seed on top of seed/base-fmm.sql + %s (fake data)\n" % esquema + ("\n".join(seed) if seed else "SELECT 1;") + "\n"
    (d / "seed.sql").write_text(texto, encoding="utf-8", newline="\n")
    return cid
