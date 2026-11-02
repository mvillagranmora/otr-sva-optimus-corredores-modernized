"""F3 W3 T5 (CertificarSolicitudAporteRescate, FRONT J the closing + the facade of the recursion): the case writer of cases-cer-t5/FMM-CER-nnn_slug.

Same method as T1..T4 (cases_cer_caso.py, cases_cer_t2.py, cases_cer_t3.py, cases_cer_t4.py): the case INPUTS (case.json, seed.sql) are written here; the goldens (expected.json) are RECORDED by
legacy-oracle/record_cer_t5.sh from the legacy class `cl.bcs.fondomutuo.CertificarSolicitudAporteRescate` (and its real collaborators of the module) on H2, with the SAME registered doubles for
what is not ported (the only differences with T4-B: the double of the asynchronous notification can also make the two parameter reads of the real method, `"asyncParametros": true`, and the runner is
own). NEVER derived from reading the code.

What is new in T5: the golden is compared AS A WHOLE. A T1..T4 case is compared "up to the seam" (what F-A..F-I decided); a T5 case is a COMPLETE run A..J of the legacy: the output text, the three
dumps of the tables (after the seeds, what the class left in the session, after the host decision), EVERY collaborator call with its complete arguments in order (the calls the CHILD certification
made are inside the record `CertificarSolicitudAporteRescate.hijo`, field `internas`, as T4Spy leaves them), the sequence requests, the session and the exception class. The cases live in their OWN
directory (cases-cer-t5/, numbered from FMM-CER-759: T4-B ends at FMM-CER-758) so that no T1..T4 case or golden is touched. They load the SAME schema and bases as T4 (no new seed file).

ref tags: FM-CER-nn (cards), D-F3B-nn / D-NOR-nn (defects), `matriz:Cnn` (the case of RULES_FONDOMUTUO_F3_B.md 11.1 it realises), `front:F-J`, `T5-modo:completo` | `T5-modo:excepcion` (the
legacy threw somewhere in the run), and the optional `T5-compuerta:G1|G2|G3|G6|G8|GCUS` (the gate a closed-gate test of CerT5CompuertaTest closes for this case), `T5-profundidad` (the legacy recursion
reaches a depth of 2: the only intended behaviour difference of T5), `T5-m3:a|b|c` (the three paths of the review M3), `T5-async-parametros` (the run reads PLA.CallBatchURL / Token).
"""
import json
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cer_lib as L  # noqa: E402
import cases_cer_caso as C  # noqa: E402
import cases_cer_t4 as T4  # noqa: E402
from cases_cer_lib import aporte, rescate as _rescate_base, solo_tabla, config, param, ins, Num, ABSENT, NULL, tipo_movimiento  # noqa: E402,F401
from cases_cer_caso import entrada  # noqa: E402,F401
from cases_cer_t2 import err, resp_custodia, res_custodia, calcula_custodia, fila_saldo, paridad_utm, paridad_uf, paridad_comision, custodia_script, saldo_por_uso  # noqa: E402,F401
from cases_cer_t3 import msg, resp_cuenta, cuenta_script, resp_tesoreria, rech, gen, tesoreria, resp_spot, op, spot, TRAS_APV, TRAS_FM  # noqa: E402,F401
from cases_cer_t4 import (mov, resp_mov_comp, mov_sal, resp_comprobante, facturacion, msg_fact, res_ar, resp_ar, ar, msg_ar, destino, T_RESERIE, ESQUEMAS)  # noqa: E402,F401

CASES_T5 = L.BASE / "cases-cer-t5"
PRIMER_NUMERO = 759        # T4-B ends at FMM-CER-758
_n = {"n": PRIMER_NUMERO - 1}
TABLAS = C.TABLAS + [{"nombre": T_RESERIE, "orden": '"IdReinversionSerieFondo"'}]
FECHA_SISTEMA = "2026-03-02"

OWP_S = config("PLA.NotificarEstadoOrdenOWP", "S")
OWP_PARAM_N = config("PLA.NotificarEstadoOrdenOWP", "N")
CALLBATCH = config("PLA.CallBatchURL", "FAKE-URL-NO-NETWORK") + config("PLA.CallBatchToken", "FAKE-TOKEN-0000")      # fake values: never used, no network


def caso(slug, ref, ent, seed=(), colaboradores=None, excepcion=False, custodia=None, paridad=(), saldo=None, calcula=None, previas=None, reloj=None, tags=(), **extra):
    """Writes cases-cer-t5/FMM-CER-nnn_slug and returns its id. The benign answers of every collaborator come from the same builders as T4-B (a case may replace any of them through
    `colaboradores`). `excepcion=True`: the legacy is expected to throw somewhere in the run (the golden carries `excepcion`); otherwise the run must end normally. `tags` are extra ref tags."""
    _n["n"] += 1
    sql, specs = C._aplanar(seed)
    colab = {}
    if specs and "EntregarPreciosTasas" not in (colaboradores or {}):
        colab.update(C.precios_por_defecto(specs))
    colab.update(C.benignos(specs))
    colab.update(custodia_script(specs, **(custodia or {})))
    colab.update(T4._paridad_benigna(specs, paridad))
    if saldo is not None:
        colab.update(saldo)
    if calcula is not None:
        colab.update(calcula)
    colab.update(colaboradores or {})
    cid = "FMM-CER-%03d_%s" % (_n["n"], slug)
    refs = list(ref) + ["front:F-J", "T5-modo:excepcion" if excepcion else "T5-modo:completo"] + list(tags)
    doc = {"servicio": L.SERVICIO, "ref": refs, "esquemasExtra": ESQUEMAS, "entrada": ent, "tablas": TABLAS}
    if previas:
        doc["entradasPrevias"] = list(previas)
    if reloj:
        doc["reloj"] = reloj
    doc.update(extra)
    if colab:
        doc["colaboradores"] = colab
    d = CASES_T5 / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(L.dumps(doc), encoding="utf-8", newline="\n")
    texto = ("-- case seed on top of seed/base-fmm.sql + schema-cer.sql + seed/base-cer.sql + seed/base-cer-t2.sql + seed/base-cer-t3.sql + seed/base-cer-t4.sql (fake data)\n"
             + ("\n".join(sql) if sql else "SELECT 1;") + "\n")
    (d / "seed.sql").write_text(texto, encoding="utf-8", newline="\n")
    return cid


def ap(id_, pf=None, **c):
    """an aporte (MueveCta S, manual origin M); the portfolio is `pf` or the IdPortafolio keyword (default 500)"""
    pf = c.pop("IdPortafolio", 500 if pf is None else pf)
    c.setdefault("CodigoPortafolio", "PF%d" % pf)
    return aporte(id_, IdPortafolio=pf, MueveCta="S", **c)


def rescate(id_, **c):
    """a rescate paid by the cuenta de inversion (medio CIN, the base default) unless the case says otherwise"""
    return _rescate_base(id_, **c)


def rescate_tra(id_, **c):
    c.setdefault("IdMedioDePago", "TRA")
    return _rescate_base(id_, **c)


def origen(id_, **c):
    return list(L.rescate(id_, estado="CRT", **c))


def sin_estado(*ids):
    """removes rows of PLA.Estado (a state that is not in the table: procesarSalida raises NullPointerException, FM-CER-67)"""
    return ['DELETE FROM "%s" WHERE "IdEstado" IN (%s);' % (L.ESTADO, ", ".join("'%s'" % i for i in ids))]


def sin_fila_de_tabla(*ids):
    """the solicitud exists in the VIEW but not in the TABLE (the UPDATE of FM-CER-61 then touches nothing)"""
    return ['DELETE FROM "%s" WHERE "IdSolicitudAporteRescate" IN (%s);' % (L.T_SOL, ", ".join(str(i) for i in ids))]


def falla(colaborador, texto):
    return L.falla(colaborador, texto)


def agregar_log(contador=0, mensajes=()):
    """scripted answer of AgregarLog (the legacy never reads it: FM-CER-66)"""
    return {"AgregarLog": {"defecto": {"P_OUT_ERRORESCONTADOR": contador, "P_OUT_MENSAJES": list(mensajes)}}}
