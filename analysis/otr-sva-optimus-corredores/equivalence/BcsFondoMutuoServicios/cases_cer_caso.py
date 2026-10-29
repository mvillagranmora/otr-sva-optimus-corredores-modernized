"""F3 W3 T1: the case writer of the FMM-CER cases (cases-cer/FMM-CER-nnn_slug). Built on cases_cer_lib (rows, helpers); see its docstring for the two kinds of case
(TERMINAL, and CONTINUES = recorded twice: `completo` and `parada`).

  caso(slug, ref, ent, seed=..., colaboradores=..., modo="terminal" | "continua", previas=..., reloj=...)

`seed` is a list of statements, lists of statements and Sol objects (the rows of a solicitud). The solicitudes of the Sol objects feed the BENIGN answers of the collaborators
(`benignos`) of the `completo` variant: the legacy then runs to the end with no later rejection, so that its output and the UPDATE of the certified rows show what F-A and F-B
decided. A case may override the answer of any collaborator through `colaboradores` (the key replaces the benign one).
"""
import cases_cer_lib as L
from cases_cer_lib import ABSENT, NULL, Num, Sol, T_SOL, T_LOG  # noqa: F401

_n = {"n": 0}

TABLAS = [
    {"nombre": T_SOL, "orden": '"IdSolicitudAporteRescate"'},
    {"nombre": T_LOG, "orden": '"IdLogCertificacionAporteRescate"'},
]

# the collaborators F-C / F-D / F-J reach FIRST; in the `parada` variant they fail with this text and the legacy stops at the first one it calls
PARADA = ("SaldoFlujoCaja", "CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo", "NotificarEstadoOrdenFondoMutuo")
TEXTO_PARADA = "PARADA_T1"

PRECIO_SERIE = {"FM001A": 1500.1234, "FM001B": 1200.5, "FM002A": 10.1234, "FM003A": 2500}


def entrada(ids, fecha="2026-03-02", sim="N", canal=ABSENT, **campos):
    """Request. `ids`: list of ids (int), of dicts (a full InSolicitudes element) or None (p_Solicitudes explicit null). Extra keywords replace the standard fields; a value
    ABSENT removes the key (the pojo default applies)."""
    d = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": sim, "p_UsaReglasNegocio": "N", "p_FechaCertificacion": fecha}
    if canal is not ABSENT:
        d["p_IdTipoCanalIngreso"] = canal
    if ids is None:
        d["p_Solicitudes"] = None
    else:
        d["p_Solicitudes"] = [x if isinstance(x, dict) else {"IdSolicitudAporteRescate": x} for x in ids]
    for k, v in campos.items():
        d[k] = v
    return {k: v for k, v in d.items() if v is not ABSENT}


def _aplanar(seed):
    out, specs = [], []
    for it in seed:
        if isinstance(it, Sol):
            specs.append(it.spec)
            out.extend(it)
        elif isinstance(it, (list, tuple)):
            o, s = _aplanar(it)
            out.extend(o)
            specs.extend(s)
        else:
            out.append(it)
    return out, specs


def _precio_de(spec):
    f = spec["FechaValorCuota"]
    fecha = (f if isinstance(f, str) and not f.startswith("@") else "2026-03-02") + " 00:00:00.000"
    return L.precio(spec["IdSolicitudAporteRescate"], PRECIO_SERIE.get(spec["IdSerieFondo"], 1500.1234), fecha,
                    instrumento=spec["IdInstrumento"], moneda=spec["IdMonedaFondo"], nemo=spec["IdSerieFondo"])


def precios_por_defecto(specs):
    return L.precios(*[_precio_de(s) for s in specs])


def paridades(specs):
    """CalcularParidad answers: job 1 = UTM -> CLP of calcularCostoUFRentabilidadRescates (IdInterno 1..4) and the diferido conversions of CalcularMontoNumeroCuota (IdInterno = id);
    job 2 = system currency, amount in UF and value of the UF of CalcularMontoNumeroCuota (IdInterno = id * 4 + 1 / 2 / 3)."""
    j1 = [{"IdTrabajo": 1, "IdInterno": k, "MontoDestino": 66000, "Resultado": "OK"} for k in (1, 2, 3, 4)]
    j2 = []
    for s in specs:
        i = s["IdSolicitudAporteRescate"]
        m = s["CostoMonedaLocal"]
        monto = float(m.texto) if isinstance(m, Num) else float(m) if isinstance(m, (int, float)) and m else 1000000.0
        if s["IdMoneda"] != s["IdMonedaFondo"]:
            j1.append({"IdTrabajo": 1, "IdInterno": i, "FactorCambioLocal": 905.5, "MontoDestino": round(monto / 905.5, 4), "Resultado": "OK"})
        j2.append({"IdTrabajo": 2, "IdInterno": i * 4 + 1, "MontoDestino": round(monto, 2), "Resultado": "OK"})
        j2.append({"IdTrabajo": 2, "IdInterno": i * 4 + 2, "MontoDestino": round(monto / 39000.5, 4), "Resultado": "OK"})
        j2.append({"IdTrabajo": 2, "IdInterno": i * 4 + 3, "FactorOrigenDestino": 39000.5, "MontoDestino": 39000.5, "Resultado": "OK"})
    return {"CalcularParidad": {"respuestas": [{"cuando": {"0.p_IdTrabajo": 1}, "respuesta": {"p_out_CalculosSalida": j1}},
                                               {"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": j2}}], "defecto": {"p_out_CalculosSalida": []}}}


def benignos(specs):
    """Answers that let the legacy run to the end after F-A / F-B with NO later rejection (the `completo` variant)."""
    rescates = [s for s in specs if s["TipoSolicitud"] == "RES"]
    ids = [s["IdSolicitudAporteRescate"] for s in specs if s["TipoSolicitud"] in ("RES", "APO")]
    movs = [{"IdGenerarMovimientoCustodia": i, "TablaOrigen": "CUS.Cartera", "FolioCustodia": 5000 + i, "IdCustodia": 9000 + i, "Cantidad": 1,
             "CostoInversionEnFactorAjusteARetirar": 10.5, "ValorFactorAjusteaFechaCurseARetirar": 39000.5, "CostoInversionEnFactorAjusteOriginal": 10.5,
             "CostoInversionEnFactorAjusteActual": 10.5, "ValorFactorOriginal": 39000.5, "ValorFactorARetirar": 39000.5, "MontoCompraFactorOriginal": 2.5,
             "MontoCompraFactorARetirar": 2.5, "MontoCompraFactorActual": 2.5} for i in ids]
    def resultado_custodia(s):
        r = {"IdSolicitudAporteRescate": s["IdSolicitudAporteRescate"], "Estado": "APD", "CantidadDisponible": 1000000}
        if s["CalculoPorMontoCuota"] == "T":   # FM-CER-33 (F-D): a total rescate takes the amounts of the custody; the benign answer must carry them
            r.update({"Monto": 150000, "NumeroCuotas": 99.5, "ValorCuota": 1507.5377, "MontoDestino": 150000, "ValorParidad": 1, "CostoEnFactorAjuste": 3.8461, "ValorFactorAjuste": 39000.5,
                      "MontoMonedaSistema": 150000})
        return r
    out = {"ProcesarCustodiaFondoMutuo": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_MENSAJES": [], "P_OUT_MOVIMIENTOS": movs}},
           "CalculaCustodiaRescate": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_MENSAJES": [], "P_OUT_RESULTADO": [resultado_custodia(s) for s in rescates]}}}
    out.update(paridades(specs))
    # a rich cash flow for every (portafolio, moneda) of the aportes that F-C will check, on the date F-C asks for (the later of FechaCertificacion and PLA.FechaSistema)
    filas = []
    for s in specs:
        if s["TipoSolicitud"] == "APO":
            fecha = max(str(s["FechaCertificacion"]), "2026-03-02")
            f = {"IdPortafolio": s["IdPortafolio"], "IdMoneda": s["IdMoneda"], "FechaSaldo": fecha, "Saldo": 999999999}
            if f not in filas:
                filas.append(f)
    out.update(L.saldo_flujo(*filas))
    return out


def caso(slug, ref, ent, seed=(), colaboradores=None, modo="terminal", previas=None, reloj=None, **extra):
    """Writes cases-cer/FMM-CER-nnn_slug (and, for modo='continua', cases-cer/FMM-CER-nnn_slug-parada) and returns the id of the first. `ref` lists the cards (FM-CER-nn) and
    defects (D-F3B-nn) the case pins; `previas` = requests executed BEFORE `ent` in the same H2 session, each with its own commit / rollback decision."""
    _n["n"] += 1
    sql, specs = _aplanar(seed)
    base = {}
    if specs and "EntregarPreciosTasas" not in (colaboradores or {}):
        base.update(precios_por_defecto(specs))
    if modo == "continua":
        base.update(benignos(specs))
    base.update(colaboradores or {})
    variantes = [("", dict(base))]
    if modo == "continua":
        par = dict(base)
        for c in PARADA:
            par.update(L.falla(c, TEXTO_PARADA))
        variantes.append(("-parada", par))
    primero = None
    for sufijo, colab in variantes:
        cid = "FMM-CER-%03d_%s%s" % (_n["n"], slug, sufijo)
        refs = list(ref) + (["T1-costura:parada"] if sufijo else (["T1-costura:completo"] if modo == "continua" else []))
        doc = {"servicio": L.SERVICIO, "ref": refs, "esquemasExtra": ["schema-cer.sql", "seed/base-cer.sql"], "entrada": ent, "tablas": TABLAS}
        if previas:
            doc["entradasPrevias"] = list(previas)
        if reloj:
            doc["reloj"] = reloj
        doc.update(extra)
        if colab:
            doc["colaboradores"] = colab
        d = L.CASES / cid
        d.mkdir(parents=True, exist_ok=True)
        (d / "case.json").write_text(L.dumps(doc), encoding="utf-8", newline="\n")
        texto = "-- case seed on top of seed/base-fmm.sql + schema-cer.sql + seed/base-cer.sql (fake data)\n" + ("\n".join(sql) if sql else "SELECT 1;") + "\n"
        (d / "seed.sql").write_text(texto, encoding="utf-8", newline="\n")
        primero = primero or cid
    return primero
