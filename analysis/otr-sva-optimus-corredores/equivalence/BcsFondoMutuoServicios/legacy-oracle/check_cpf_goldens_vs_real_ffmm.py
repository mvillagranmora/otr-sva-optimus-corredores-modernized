"""READ ONLY check (writes nothing, touches nothing): which of the CPF front's goldens (cases-cpf/, recorded with a scripted DOUBLE of ConsultarCustodiaPortafolioFFMM) script custody rows that the
REAL, unmodified FFMM can NOT produce for the request CPF always sends (p_AgrupaPorBoveda N, p_AgrupaPorRelacion N, p_TipoFechaConsulta LIQ), and so would not survive being replayed over the real
class. Invariants of the real FFMM (RULES_FONDOMUTUO_F3_C.md 8.2, verified by the chain goldens of cases-cpfchain/):

  I1  IdBoveda is '' in EVERY row (the vault is only kept when p_AgrupaPorBoveda is S)                           (FM-CPFF-12, 22.3)
  I2  Relacion is '' in EVERY row (the relation is only computed when p_AgrupaPorRelacion is S)                  (FM-CPFF-11, 22.3)
  I3  no two rows share the group key instrumento_nemotecnico_emisor_cliente_portafolio_regimen                  (FM-CPFF-12)
  I4  CantidadPrecioPromedio = CantidadCustodia + CantidadGarantia and TotalPosicion = CantidadCustodia + CantidadGarantia              (FM-CPFF-15)
  I5  with CantidadGarantia != 0 (and CantidadPrecioPromedio != 0) MontoPrecioPromedio = 0 and PPPCustodia = 0           (D-F3C-01)
  I6  PPPCustodia is null exactly when CantidadPrecioPromedio = 0                                                  (FM-CPFF-15)

The goldens stay VALID for what they pinned: they fix what CPF does with ANY answer of the port (the port is a JSON contract of 29 fields). What they do not and cannot prove is that the real class
produces such answers: that is what cases-cpfchain/ adds. A case that violates I1 / I2 / I3 is "not replayable as is over the real FFMM": CPF would copy the scripted vault / relation / both rows,
the real class answers '' / '' / one group. The goldens are NOT modified.

  py -3 -I legacy-oracle/check_cpf_goldens_vs_real_ffmm.py [-v]
"""
import json
import pathlib
import sys
from decimal import Decimal

HERE = pathlib.Path(__file__).resolve().parent
CASES = HERE.parent / "cases-cpf"


def num(v):
    return None if v is None else Decimal(str(v))


def filas_de(colab):
    """Every scripted answer of the custody double ('defecto' and every 'respuestas' entry): list of row lists."""
    c = colab.get("ConsultarCustodiaPortafolioFFMM")
    if not c:
        return []
    r = []
    if "defecto" in c and c["defecto"]:
        r.append(c["defecto"].get("p_out_tbl") or [])
    for e in c.get("respuestas", []):
        if "respuesta" in e and e["respuesta"]:
            r.append(e["respuesta"].get("p_out_tbl") or [])
    return r


def violaciones(filas):
    v = set()
    claves = {}
    for f in filas:
        if (f.get("IdBoveda") or "") != "":
            v.add("I1")
        if (f.get("Relacion") or "") != "":
            v.add("I2")
        k = "_".join(str(f.get(x)) for x in ("IdInstrumento", "Nemotecnico", "IdEmisor", "IdCliente", "IdPortafolio", "IdRegimenFondoMutuo"))
        claves[k] = claves.get(k, 0) + 1
        cc, cg, cpp, tp = num(f.get("CantidadCustodia")), num(f.get("CantidadGarantia")), num(f.get("CantidadPrecioPromedio")), num(f.get("TotalPosicion"))
        if None not in (cc, cg, cpp) and cpp != cc + cg:
            v.add("I4")
        if None not in (cc, cg, tp) and tp != cc + cg:
            v.add("I4")
        mpp, ppp = num(f.get("MontoPrecioPromedio")), num(f.get("PPPCustodia"))
        if cg not in (None, 0) and cpp not in (None, 0) and (mpp not in (None, 0) or ppp not in (None, 0)):
            v.add("I5")
        if (ppp is None) != (cpp == 0 if cpp is not None else False):
            v.add("I6")
    if any(n > 1 for n in claves.values()):
        v.add("I3")
    return v


def main():
    verbose = "-v" in sys.argv
    total = con_custodia = 0
    por_invariante = {}
    casos_i123 = []
    casos_solo_i456 = []
    for d in sorted(CASES.glob("FMM-CPF-*")):
        cj = json.loads((d / "case.json").read_text(encoding="utf-8"))
        total += 1
        respuestas = filas_de(cj.get("colaboradores", {}))
        filas = [f for r in respuestas for f in r]
        if not filas:
            continue
        con_custodia += 1
        v = set()
        for r in respuestas:
            v |= violaciones(r)
        for i in v:
            por_invariante[i] = por_invariante.get(i, 0) + 1
        if v & {"I1", "I2", "I3"}:
            casos_i123.append((d.name, sorted(v)))
        elif v:
            casos_solo_i456.append((d.name, sorted(v)))
    print("CPF goldens (cases-cpf/):", total)
    print("  with a scripted custody answer with at least one row:", con_custodia)
    print("  with NO row (the custody double answers an empty list, or is not reached):", total - con_custodia)
    for i in sorted(por_invariante):
        print("  invariant %s violated by the scripted rows of %d cases" % (i, por_invariante[i]))
    print("  NOT replayable as is over the real FFMM (I1 / I2 / I3: vault, relation or duplicated group):", len(casos_i123))
    print("  only I4 / I5 / I6 (columns CPF never reads: CantidadPrecioPromedio, MontoPrecioPromedio, TotalPosicion; PPP consistency):", len(casos_solo_i456))
    print("  consistent with the real FFMM:", con_custodia - len(casos_i123) - len(casos_solo_i456))
    if verbose:
        for n, v in casos_i123:
            print("   I123", n, v)
        for n, v in casos_solo_i456:
            print("   I456", n, v)


if __name__ == "__main__":
    main()
