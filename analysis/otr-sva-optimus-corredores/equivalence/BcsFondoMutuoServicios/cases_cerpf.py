"""F3 W3 T6-A (CertificarPorFiltrosSolicitudAporteRescate, cards FM-CXF-01..06): the case writer of cases-cerpf/FMM-CERPF-nnn_slug.

Same method as T1..T5: the case INPUTS (case.json, seed.sql) are written here; the goldens (expected.json) are RECORDED by legacy-oracle/record_cerpf.sh from the REAL legacy class
`cl.bcs.fondomutuo.CertificarPorFiltrosSolicitudAporteRescate` calling the REAL `CertificarSolicitudAporteRescate` (same registered doubles for what is not ported, H2 MODE=LEGACY, 20 runs per case;
an unstable case gets NO golden). NEVER derived from reading the code.

Two kinds of case, both write the WHOLE-run golden of the T5 harness plus the record of the ONE collaborator of the class (`CertificarPorFiltros.certificar`: the complete entry that the class built for
Certificar and the answer; everything Certificar did is inside it, field `internas`):
  * FILTER cases (`caso`): a seed of solicitudes (the rows of the T5 helpers `ap` / `rescate`, with the view columns of the filters written by `vista`, and `detalle` rows of the loads) and one or
    more filters; what the filters select is what Certificar receives (visible in the record of the call), in the order of the filters and WITH duplicates.
  * CERTIFICAR cases (`desde_t5`): the seed, the scripted collaborators and the header of an existing T5 case (cases-cer-t5, copied and NOT edited) with one all-pass filter: they pin that the class
    returns what Certificar answers and lets its exceptions out, for the clean run, the early exits (counter 1), the exceptions and the child certification of the switch.

ref tags: FM-CXF-nn (cards of RULES_FONDOMUTUO_F3_B.md 8.2), `matriz:Cnn` (11.2: FM-CXF-C01..C06), `front:X`, `PF-modo:completo` | `PF-modo:excepcion` (the legacy threw somewhere in the run),
`PF-filtro:<campo>` (the filter field the case exercises), `PF-seleccion:cero|uno|varias`, `PF-duplicados`, `PF-sin-filtros`, `PF-nulo`, `PF-certificar:<what>` (the Certificar behaviour of a derived case).
"""
import json
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cer_lib as L  # noqa: E402
import cases_cer_caso as C  # noqa: E402
import cases_cer_t4 as T4  # noqa: E402
from cases_cer_lib import ABSENT, Num  # noqa: E402,F401
from cases_cer_t5 import ap, rescate, ESQUEMAS, TABLAS as TABLAS_T5, custodia_script  # noqa: E402
from cases_cer_t4_i import movs  # noqa: E402

CASES_PF = L.BASE / "cases-cerpf"
CASES_T5 = L.BASE / "cases-cer-t5"
PRIMER_NUMERO = 1
_n = {"n": PRIMER_NUMERO - 1}
SERVICIO_PF = "CertificarPorFiltrosSolicitudAporteRescate"
ESQUEMAS_PF = ESQUEMAS + ["schema-cerpf.sql"]
T_DET = "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate"
TABLAS = TABLAS_T5 + [{"nombre": T_DET, "orden": '"IdDetalleCargaSolicitudesAporteRescate"'}]

ABSENTE = L.ABSENT


def filtro(**c):
    """a filter. The default is the all-pass one: texts empty, Nacional / MoneyMarket 'N', IdCarga 0. A value ABSENT removes the key (the field reads null); None writes JSON null."""
    d = {"IdEmisor": "", "IdMoneda": "", "IdSerieFondo": "", "Nacional": "N", "TipoSolicitud": "", "IdTipoMovimientoFondoMutuo": "", "MoneyMarket": "N", "IdCarga": 0}
    d.update(c)
    return {k: v for k, v in d.items() if v is not ABSENTE}


def entrada(filtros, fecha="2026-03-02", sim="N", empresa=1, usuario=7, modulo="FMM", reglas="N", canal=ABSENTE, **campos):
    """The request of CertificarPorFiltros. `filtros`: list (or ABSENT / None for the field itself). Extra keywords replace / add fields; ABSENT removes the key."""
    d = {"p_IdEmpresa": empresa, "p_IdUsuario": usuario, "p_IdModulo": modulo, "p_SimularOperacion": sim, "p_UsaReglasNegocio": reglas, "p_FechaCertificacion": fecha}
    if canal is not ABSENTE:
        d["p_IdTipoCanalIngreso"] = canal
    if filtros is not ABSENTE:
        d["p_Filtros"] = filtros
    d.update(campos)
    return {k: v for k, v in d.items() if v is not ABSENTE}


def _lit(v):
    return "NULL" if v is None else ("'%s'" % v if isinstance(v, str) else str(v))


def vista(id_, **c):
    """writes columns of the VIEW row (the stand-in is a table the case writes): EsFondoNacional, MoneyMarket, IdEmisor, IdEmpresa, FechaCertificacion ..."""
    sets = ", ".join('"%s" = %s' % (k, _lit(v)) for k, v in c.items())
    return 'UPDATE "%s" SET %s WHERE "IdSolicitudAporteRescate" = %d;' % (L.V_SOL, sets, id_)


def detalle(id_det, carga, solicitud, proceso="PEN", empresa=1, serie="FM001A"):
    """a row of FMM.DetalleCargaSolicitudesAporteRescate (all the NOT NULL columns with fake values)"""
    return ('INSERT INTO "%s" ("IdDetalleCargaSolicitudesAporteRescate", "IdCargaSolicitudesAporteRescate", "RutEmisor", "CodigoPortafolioEmisor", "TipoMovimiento", "FechaMovimiento", '
            '"IdSolicitudAporteRescate", "DocumentoIdentidad", "CodigoPortafolio", "IdSerieFondo", "CalculoPorMontoCuota", "NumeroCuotas", "ValorCuota", "Monto", "MontoComision", "IdMoneda", '
            '"IdSerieFondoDestino", "IdCliente", "IdPortafolio", "IdEmisor", "Estado", "DescEstado", "EstadoProceso", "Eliminado", "IdEmpresa") VALUES (%d, %d, \'11111111-1\', \'PF500\', \'APORTE\', '
            'DATE \'2026-03-02\', %d, \'11111111-1\', \'PF500\', \'%s\', \'M\', 0, 0, 1000000, 0, \'CLP\', \'\', 100, 500, \'AGF1\', \'ING\', \'Ingresada\', \'%s\', \'N\', %d);'
            % (T_DET, id_det, carga, solicitud, serie, proceso, empresa))


def _usd(id_, **c):
    d = dict(IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="USD", IdMonedaSistema="CLP", MontoMonedaSistema=400000, CostoMonedaLocal=5000, Monto=5000)
    d.update(c)
    return ap(id_, **d)


def base():
    """The standard seed of the FILTER cases (five solicitudes of company 1, date 2026-03-02):
         4001 APO APONORM FM001A CLP emisor AGF1 nacional S money-market N ING
         4002 APO APONORM FM001A CLP emisor AGF2 nacional S money-market S ACP
         4003 APO APONORM FM002A USD emisor AGF1 nacional N money-market N ING
         4004 APO APONORM FM002A USD emisor AGF2 nacional N money-market S ING
         3001 RES RESNORM FM001A CLP emisor AGF1 nacional S money-market N ING
    """
    return [ap(4001), ap(4002, estado="ACP", IdEmisor="AGF2"), _usd(4003), _usd(4004, IdEmisor="AGF2"), rescate(3001),
            vista(4002, MoneyMarket="S"), vista(4003, EsFondoNacional="N"), vista(4004, EsFondoNacional="N", MoneyMarket="S")]


def cargas():
    """the detail rows of four loads: 900 {4001 PEN, 4002 PEN}, 901 {4003 PEN, 4004 ERR}, 902 {4001 PEN twice}, 903 {4001 PEN, 4002 PRO}"""
    return [detalle(1, 900, 4001), detalle(2, 900, 4002, serie="FM001A"), detalle(3, 901, 4003, serie="FM002A"), detalle(4, 901, 4004, proceso="ERR", serie="FM002A"),
            detalle(5, 902, 4001), detalle(6, 902, 4001), detalle(7, 903, 4001), detalle(8, 903, 4002, proceso="PRO")]


def _escribir(cid, doc, seed_texto):
    d = CASES_PF / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(L.dumps(doc), encoding="utf-8", newline="\n")
    (d / "seed.sql").write_text(seed_texto, encoding="utf-8", newline="\n")


def caso(slug, ref, ent, seed=None, colaboradores=None, excepcion=False, custodia=None, tags=(), **extra):
    """Writes cases-cerpf/FMM-CERPF-nnn_slug and returns its id. The benign answers of every collaborator come from the same builders as T5. `excepcion=True`: the legacy is expected to throw
    somewhere in the run (the golden carries `excepcion`)."""
    _n["n"] += 1
    seed = base() if seed is None else seed
    sql, specs = C._aplanar(seed)
    colab = {}
    if custodia is None and any(s["TipoSolicitud"] == "RES" and s["IdSolicitudAporteRescate"] == 3001 for s in specs):
        custodia = movs(3001, 100)      # the custody answer of the rescate of the standard seed
    if specs:
        colab.update(C.precios_por_defecto(specs))
    colab.update(C.benignos(specs))
    colab.update(custodia_script(specs, **(custodia or {})))
    colab.update(T4._paridad_benigna(specs, ()))
    colab.update(colaboradores or {})
    cid = "FMM-CERPF-%03d_%s" % (_n["n"], slug)
    refs = list(ref) + ["front:X", "PF-modo:excepcion" if excepcion else "PF-modo:completo"] + list(tags)
    doc = {"servicio": SERVICIO_PF, "ref": refs, "esquemasExtra": ESQUEMAS_PF, "entrada": ent, "tablas": TABLAS}
    doc.update(extra)
    if colab:
        doc["colaboradores"] = colab
    texto = "-- case seed on top of seed/base-fmm.sql + schema-cer.sql + seed/base-cer*.sql + schema-cerpf.sql (fake data)\n" + ("\n".join(sql) if sql else "SELECT 1;") + "\n"
    _escribir(cid, doc, texto)
    return cid


def desde_t5(numero, slug, ref, filtros=None, excepcion=None, tags=(), **cambios):
    """A CERTIFICAR case: the seed, collaborators, clock and header of the case FMM-CER-<numero> of cases-cer-t5 (copied, never edited) with the filters `filtros` (default: ONE all-pass filter).
    `cambios` replace fields of the entry (e.g. sim='S'). The kind (completo / excepcion) is read from the golden of the T5 case unless `excepcion` says it."""
    origen = next(CASES_T5.glob("FMM-CER-%03d_*" % numero))
    c5 = json.loads((origen / "case.json").read_text(encoding="utf-8"))
    g5 = json.loads((origen / "expected.json").read_text(encoding="utf-8"))
    e5 = c5["entrada"]
    ent = entrada([filtro()] if filtros is None else filtros, fecha=e5["p_FechaCertificacion"], sim=e5["p_SimularOperacion"], empresa=e5["p_IdEmpresa"], usuario=e5["p_IdUsuario"],
                  modulo=e5["p_IdModulo"], reglas=e5["p_UsaReglasNegocio"], canal=e5.get("p_IdTipoCanalIngreso", ABSENTE))
    ent.update(cambios)
    _n["n"] += 1
    cid = "FMM-CERPF-%03d_%s" % (_n["n"], slug)
    exc = ("excepcion" in g5) if excepcion is None else excepcion
    refs = list(ref) + ["front:X", "PF-modo:excepcion" if exc else "PF-modo:completo", "PF-certificar:T5-%03d" % numero] + list(tags)
    doc = {"servicio": SERVICIO_PF, "ref": refs, "esquemasExtra": ESQUEMAS_PF, "entrada": ent, "tablas": TABLAS}
    for k in ("reloj", "asyncParametros", "arReal"):
        if k in c5:
            doc[k] = c5[k]
    if "colaboradores" in c5:
        doc["colaboradores"] = c5["colaboradores"]
    seed = (origen / "seed.sql").read_text(encoding="utf-8").split("\n", 1)[1]
    texto = "-- case seed copied from cases-cer-t5/%s on top of the same bases + schema-cerpf.sql (fake data)\n" % origen.name + seed
    _escribir(cid, doc, texto)
    return cid


def generar():
    todas = lambda **k: [filtro(**k)]   # noqa: E731
    # ================================================================== FM-CXF-02 selection: how many rows
    caso("el-filtro-que-deja-pasar-todo-trae-las-cinco-solicitudes", ["FM-CXF-01", "FM-CXF-02", "FM-CXF-05", "matriz:C01"], entrada(todas()), tags=["PF-seleccion:varias"], custodia=movs(3001, 100))
    caso("un-filtro-sin-coincidencias-llama-a-certificar-con-la-lista-vacia", ["FM-CXF-02", "FM-CXF-04", "matriz:C01"], entrada(todas(IdEmisor="ZZZ")), tags=["PF-seleccion:cero", "PF-filtro:IdEmisor"])
    caso("un-filtro-con-una-sola-coincidencia-el-rescate", ["FM-CXF-02", "matriz:C01"], entrada(todas(TipoSolicitud="RES")), tags=["PF-seleccion:uno", "PF-filtro:TipoSolicitud"], custodia=movs(3001, 100))
    caso("dos-coincidencias-por-la-serie-del-fondo", ["FM-CXF-02", "matriz:C01"], entrada(todas(IdSerieFondo="FM002A")), tags=["PF-seleccion:varias", "PF-filtro:IdSerieFondo"])
    # ================================================================== FM-CXF-03 several filters: concatenated in the order of the filters, never de-duplicated
    caso("dos-filtros-disjuntos-se-concatenan-en-el-orden-de-los-filtros", ["FM-CXF-03", "matriz:C03"], entrada([filtro(TipoSolicitud="RES"), filtro(IdSerieFondo="FM002A")]),
         tags=["PF-seleccion:varias"], custodia=movs(3001, 100))
    caso("los-mismos-filtros-en-el-otro-orden-cambian-el-orden-de-las-solicitudes", ["FM-CXF-03", "matriz:C03"], entrada([filtro(IdSerieFondo="FM002A"), filtro(TipoSolicitud="RES")]),
         tags=["PF-seleccion:varias"], custodia=movs(3001, 100))
    caso("el-mismo-filtro-dos-veces-duplica-los-ids-y-certificar-los-rechaza", ["FM-CXF-03", "FM-CER-02", "matriz:C03"], entrada([filtro(IdSerieFondo="FM002A"), filtro(IdSerieFondo="FM002A")]),
         tags=["PF-duplicados", "PF-seleccion:varias"])
    caso("dos-filtros-que-se-solapan-en-una-solicitud-la-duplican", ["FM-CXF-03", "FM-CER-02", "matriz:C03"], entrada([filtro(IdEmisor="AGF2"), filtro(IdSerieFondo="FM002A")]),
         tags=["PF-duplicados", "PF-seleccion:varias"])
    caso("el-filtro-que-deja-pasar-todo-dos-veces-duplica-las-cinco", ["FM-CXF-03", "FM-CER-02", "matriz:C03"], entrada([filtro(), filtro()]), tags=["PF-duplicados", "PF-seleccion:varias"],
         custodia=movs(3001, 100))
    caso("tres-filtros-uno-sin-coincidencias", ["FM-CXF-02", "FM-CXF-03", "matriz:C03"], entrada([filtro(IdEmisor="ZZZ"), filtro(IdEmisor="AGF1", TipoSolicitud="APO"), filtro(IdMoneda="EUR")]),
         tags=["PF-seleccion:varias"])
    # ================================================================== FM-CXF-04 no filters
    caso("p-filtros-vacio-llama-a-certificar-con-la-lista-vacia", ["FM-CXF-04", "matriz:C04"], entrada([]), tags=["PF-sin-filtros", "PF-seleccion:cero"])
    caso("p-filtros-ausente-queda-la-lista-vacia-del-constructor", ["FM-CXF-04", "FM-CXF-01", "matriz:C04"], entrada(ABSENTE), tags=["PF-sin-filtros", "PF-seleccion:cero"])
    caso("p-filtros-nulo-lanza-NullPointerException", ["FM-CXF-04", "FM-CXF-01", "matriz:C04"], entrada(None), excepcion=True, tags=["PF-sin-filtros"])
    caso("un-filtro-sin-ningun-campo-es-el-id-de-carga-nulo-y-no-trae-nada", ["FM-CXF-02", "FM-CXF-05", "matriz:C02"], entrada([{}]), tags=["PF-nulo", "PF-seleccion:cero", "PF-filtro:IdCarga"])
    # ================================================================== FM-CXF-02 each field alone
    caso("IdEmisor-AGF2-trae-las-dos-del-emisor", ["FM-CXF-02", "matriz:C01"], entrada(todas(IdEmisor="AGF2")), tags=["PF-filtro:IdEmisor", "PF-seleccion:varias"])
    caso("IdMoneda-USD-trae-las-dos-en-dolares", ["FM-CXF-02", "matriz:C01"], entrada(todas(IdMoneda="USD")), tags=["PF-filtro:IdMoneda", "PF-seleccion:varias"])
    caso("IdMoneda-sin-ninguna-en-esa-moneda", ["FM-CXF-02", "matriz:C01"], entrada(todas(IdMoneda="EUR")), tags=["PF-filtro:IdMoneda", "PF-seleccion:cero"])
    caso("IdSerieFondo-FM001A-trae-las-tres-de-la-serie", ["FM-CXF-02", "matriz:C01"], entrada(todas(IdSerieFondo="FM001A")), tags=["PF-filtro:IdSerieFondo", "PF-seleccion:varias"], custodia=movs(3001, 100))
    caso("Nacional-N-es-sin-filtro-y-trae-las-cinco", ["FM-CXF-02", "matriz:C05"], entrada(todas(Nacional="N")), tags=["PF-filtro:Nacional", "PF-seleccion:varias"], custodia=movs(3001, 100))
    caso("Nacional-S-solo-los-fondos-nacionales", ["FM-CXF-02", "matriz:C05"], entrada(todas(Nacional="S")), tags=["PF-filtro:Nacional", "PF-seleccion:varias"], custodia=movs(3001, 100))
    caso("Nacional-nulo-es-sin-filtro", ["FM-CXF-02", "matriz:C05"], entrada(todas(Nacional=None)), tags=["PF-filtro:Nacional", "PF-nulo", "PF-seleccion:varias"], custodia=movs(3001, 100))
    caso("Nacional-ausente-es-sin-filtro", ["FM-CXF-02", "matriz:C05"], entrada(todas(Nacional=ABSENTE)), tags=["PF-filtro:Nacional", "PF-nulo", "PF-seleccion:varias"], custodia=movs(3001, 100))
    caso("Nacional-otro-valor-X-no-coincide-con-ninguna", ["FM-CXF-02", "matriz:C05"], entrada(todas(Nacional="X")), tags=["PF-filtro:Nacional", "PF-seleccion:cero"])
    caso("Nacional-vacio-no-es-sin-filtro-y-no-trae-nada", ["FM-CXF-02", "matriz:C05"], entrada(todas(Nacional="")), tags=["PF-filtro:Nacional", "PF-seleccion:cero"])
    caso("Nacional-en-minuscula-s-no-coincide", ["FM-CXF-02", "matriz:C05"], entrada(todas(Nacional="s")), tags=["PF-filtro:Nacional", "PF-seleccion:cero"])
    caso("TipoSolicitud-APO-trae-los-cuatro-aportes", ["FM-CXF-02", "matriz:C01"], entrada(todas(TipoSolicitud="APO")), tags=["PF-filtro:TipoSolicitud", "PF-seleccion:varias"])
    caso("TipoSolicitud-desconocido-no-trae-nada", ["FM-CXF-02", "matriz:C01"], entrada(todas(TipoSolicitud="XXX")), tags=["PF-filtro:TipoSolicitud", "PF-seleccion:cero"])
    caso("IdTipoMovimientoFondoMutuo-RESNORM-trae-el-rescate", ["FM-CXF-02", "matriz:C01"], entrada(todas(IdTipoMovimientoFondoMutuo="RESNORM")),
         tags=["PF-filtro:IdTipoMovimientoFondoMutuo", "PF-seleccion:uno"], custodia=movs(3001, 100))
    caso("IdTipoMovimientoFondoMutuo-APONORM-trae-los-cuatro-aportes", ["FM-CXF-02", "matriz:C01"], entrada(todas(IdTipoMovimientoFondoMutuo="APONORM")),
         tags=["PF-filtro:IdTipoMovimientoFondoMutuo", "PF-seleccion:varias"])
    caso("IdTipoMovimientoFondoMutuo-APOREINV-sin-ninguna", ["FM-CXF-02", "matriz:C01"], entrada(todas(IdTipoMovimientoFondoMutuo="APOREINV")),
         tags=["PF-filtro:IdTipoMovimientoFondoMutuo", "PF-seleccion:cero"])
    caso("MoneyMarket-S-solo-los-money-market", ["FM-CXF-02", "matriz:C05"], entrada(todas(MoneyMarket="S")), tags=["PF-filtro:MoneyMarket", "PF-seleccion:varias"])
    caso("MoneyMarket-vacio-no-es-sin-filtro-y-no-trae-nada", ["FM-CXF-02", "matriz:C05"], entrada(todas(MoneyMarket="")), tags=["PF-filtro:MoneyMarket", "PF-seleccion:cero"])
    caso("MoneyMarket-nulo-es-sin-filtro", ["FM-CXF-02", "matriz:C05"], entrada(todas(MoneyMarket=None)), tags=["PF-filtro:MoneyMarket", "PF-nulo", "PF-seleccion:varias"], custodia=movs(3001, 100))
    caso("todos-los-campos-de-texto-nulos-y-carga-cero-no-filtran", ["FM-CXF-02", "FM-CXF-05", "matriz:C01"],
         entrada([{"IdEmisor": None, "IdMoneda": None, "IdSerieFondo": None, "Nacional": None, "TipoSolicitud": None, "IdTipoMovimientoFondoMutuo": None, "MoneyMarket": None, "IdCarga": 0}]),
         tags=["PF-nulo", "PF-seleccion:varias"], custodia=movs(3001, 100))
    caso("varios-campos-a-la-vez-se-combinan-con-AND", ["FM-CXF-02", "matriz:C01"], entrada(todas(IdEmisor="AGF2", IdMoneda="USD", Nacional="N", MoneyMarket="S", TipoSolicitud="APO")),
         tags=["PF-filtro:IdEmisor", "PF-filtro:IdMoneda", "PF-filtro:Nacional", "PF-filtro:MoneyMarket", "PF-seleccion:uno"])
    caso("IdEmisor-con-un-espacio-no-es-vacio-y-no-coincide-con-ninguna", ["FM-CXF-02", "matriz:C01"], entrada(todas(IdEmisor=" ")), tags=["PF-filtro:IdEmisor", "PF-seleccion:cero"])
    caso("las-claves-desconocidas-del-filtro-se-ignoran", ["FM-CXF-01", "FM-CXF-02", "matriz:C01"], entrada([filtro(IdInterno=0, TipoSolicitud="RES", Extra="x")]), tags=["PF-filtro:TipoSolicitud", "PF-seleccion:uno"],
         custodia=movs(3001, 100))
    caso("la-lista-p-Solicitudes-del-pedido-se-ignora-solo-cuentan-los-filtros", ["FM-CXF-01", "FM-CXF-05", "matriz:C01"],
         entrada(todas(IdSerieFondo="FM002A"), p_Solicitudes=[{"IdSolicitudAporteRescate": 4001}, {"IdSolicitudAporteRescate": 3001}]), tags=["PF-seleccion:varias"])
    # ================================================================== FM-CXF-02 IdCarga
    sc = base() + cargas()
    caso("IdCarga-0-ignora-el-detalle-y-no-repite-el-id-por-sus-dos-filas", ["FM-CXF-02", "matriz:C02"], entrada(todas(IdCarga=0)), seed=sc, tags=["PF-filtro:IdCarga", "PF-seleccion:varias"], custodia=movs(3001, 100))
    caso("IdCarga-900-trae-las-solicitudes-del-detalle-pendiente", ["FM-CXF-02", "matriz:C02"], entrada(todas(IdCarga=900)), seed=sc, tags=["PF-filtro:IdCarga", "PF-seleccion:varias"])
    caso("IdCarga-901-solo-la-fila-PEN-no-la-ERR", ["FM-CXF-02", "matriz:C02"], entrada(todas(IdCarga=901)), seed=sc, tags=["PF-filtro:IdCarga", "PF-seleccion:uno"])
    caso("IdCarga-902-con-dos-filas-de-detalle-PEN-de-la-misma-solicitud-sale-una-vez", ["FM-CXF-02", "matriz:C02"], entrada(todas(IdCarga=902)), seed=sc, tags=["PF-filtro:IdCarga", "PF-seleccion:uno"])
    caso("IdCarga-903-la-fila-PRO-no-cuenta", ["FM-CXF-02", "matriz:C02"], entrada(todas(IdCarga=903)), seed=sc, tags=["PF-filtro:IdCarga", "PF-seleccion:uno"])
    caso("IdCarga-inexistente-no-trae-nada", ["FM-CXF-02", "matriz:C02"], entrada(todas(IdCarga=999)), seed=sc, tags=["PF-filtro:IdCarga", "PF-seleccion:cero"])
    caso("IdCarga-ausente-es-nulo-y-hace-falso-el-OR-sin-filas", ["FM-CXF-02", "matriz:C02"], entrada(todas(IdCarga=ABSENTE)), seed=sc, tags=["PF-filtro:IdCarga", "PF-nulo", "PF-seleccion:cero"])
    caso("IdCarga-nulo-explicito-sin-filas", ["FM-CXF-02", "matriz:C02"], entrada(todas(IdCarga=None)), seed=sc, tags=["PF-filtro:IdCarga", "PF-nulo", "PF-seleccion:cero"])
    caso("IdCarga-900-combinada-con-IdEmisor-AGF2", ["FM-CXF-02", "matriz:C02"], entrada(todas(IdCarga=900, IdEmisor="AGF2")), seed=sc, tags=["PF-filtro:IdCarga", "PF-filtro:IdEmisor", "PF-seleccion:uno"])
    caso("IdCarga-con-detalle-PEN-de-una-solicitud-ya-certificada-no-la-trae", ["FM-CXF-02", "matriz:C02"], entrada(todas(IdCarga=904)),
         seed=base() + [ap(4005, estado="CRT"), detalle(9, 904, 4005), detalle(10, 904, 4001)], tags=["PF-filtro:IdCarga", "PF-seleccion:uno"])
    # ================================================================== FM-CXF-02 states, date, company
    caso("solo-los-estados-ING-y-ACP-se-seleccionan", ["FM-CXF-02", "matriz:C01"], entrada(todas()),
         seed=[ap(4001), ap(4002, estado="ACP"), ap(4005, estado="CRT"), ap(4006, estado="ANU"), ap(4007, estado="REC"), ap(4008, estado="LIQ")], tags=["PF-seleccion:varias"])
    caso("una-solicitud-ACP-sola-se-selecciona", ["FM-CXF-02", "matriz:C01"], entrada(todas()), seed=[ap(4002, estado="ACP")], tags=["PF-seleccion:uno"])
    caso("ninguna-en-ING-ni-ACP-lista-vacia", ["FM-CXF-02", "FM-CXF-04", "matriz:C01"], entrada(todas()), seed=[ap(4005, estado="CRT"), ap(4006, estado="ANU")], tags=["PF-seleccion:cero"])
    caso("la-fecha-del-pedido-elige-las-solicitudes-de-esa-fecha", ["FM-CXF-02", "matriz:C01"], entrada(todas()), seed=[ap(4001), ap(4002, FechaCertificacion="2026-03-09")], tags=["PF-seleccion:uno"])
    caso("el-pedido-con-otra-fecha-trae-solo-las-de-esa-fecha", ["FM-CXF-02", "matriz:C01"], entrada(todas(), fecha="2026-03-09"), seed=[ap(4001), ap(4002, FechaCertificacion="2026-03-09")], tags=["PF-seleccion:uno"])
    caso("fecha-sin-ninguna-solicitud-lista-vacia", ["FM-CXF-02", "FM-CXF-04", "matriz:C01"], entrada(todas(), fecha="2027-01-15"), tags=["PF-seleccion:cero"], custodia=movs(3001, 100))
    caso("la-solicitud-de-otra-empresa-no-se-selecciona", ["FM-CXF-02", "matriz:C01"], entrada(todas()), seed=[ap(4001), ap(4002), vista(4002, IdEmpresa=2)], tags=["PF-seleccion:uno"])
    caso("el-pedido-de-la-empresa-2-no-ve-las-de-la-empresa-1-y-certificar-lanza-sin-su-configuracion", ["FM-CXF-02", "matriz:C01"], entrada(todas(), empresa=2), excepcion=True, tags=["PF-seleccion:cero", "PF-desvio:kit-1"], custodia=movs(3001, 100))
    # ================================================================== FM-CXF-05 header copied
    caso("simular-S-se-copia-y-el-contador-sale-en-uno", ["FM-CXF-05", "FM-CER-68", "matriz:C01"], entrada(todas(IdSerieFondo="FM001A", TipoSolicitud="APO"), sim="S"), tags=["PF-seleccion:varias"])
    caso("simular-vacio-se-copia-tal-cual", ["FM-CXF-05", "matriz:C01"], entrada(todas(TipoSolicitud="APO"), sim=""), tags=["PF-seleccion:varias"])
    caso("simular-ausente-se-copia-como-nulo", ["FM-CXF-05", "matriz:C01"], entrada(todas(TipoSolicitud="APO"), sim=ABSENTE), tags=["PF-seleccion:varias"])
    caso("simular-en-minuscula-s-se-copia-tal-cual", ["FM-CXF-05", "matriz:C01"], entrada(todas(TipoSolicitud="APO"), sim="s"), tags=["PF-seleccion:varias"])
    caso("el-canal-usuario-modulo-y-reglas-se-copian-a-certificar", ["FM-CXF-05", "matriz:C01"], entrada(todas(IdEmisor="AGF1", TipoSolicitud="APO"), canal="OWP", usuario=21, modulo="APV", reglas="S"),
         tags=["PF-seleccion:varias"])
    caso("el-canal-vacio-se-copia-tal-cual", ["FM-CXF-05", "matriz:C01"], entrada(todas(TipoSolicitud="APO"), canal=""), tags=["PF-seleccion:varias"])
    caso("el-pedido-con-campos-de-cabecera-nulos-se-copia-tal-cual", ["FM-CXF-05", "FM-CER-01", "matriz:C01"],
         entrada(todas(), empresa=None, usuario=None, modulo=None, reglas=None, fecha=None), excepcion=True, tags=["PF-nulo", "PF-desvio:kit-1"])
    caso("el-pedido-sin-cabecera-solo-con-filtros-lanza-en-certificar", ["FM-CXF-05", "matriz:C01"], {"p_Filtros": [filtro()]}, excepcion=True, tags=["PF-nulo", "PF-desvio:kit-1"])
    # ================================================================== FM-CXF-01 / 05: what Certificar answers is returned as it is (derived from the cases of T5)
    desde_t5(759, "certificar-un-aporte-normal-sale-certificado", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(760, "certificar-un-rescate-normal-sale-certificado", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(761, "certificar-aporte-y-rescate-en-un-lote", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(804, "certificar-804-sin-solicitudes-en-ING-o-ACP-llega-la-lista-vacia-y-sale-con-contador-uno", ["FM-CXF-01", "FM-CXF-05", "FM-CER-69"])
    desde_t5(891, "certificar-con-un-error-de-la-custodia-en-los-mensajes", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(893, "certificar-tres-rechazadas-por-motivos-distintos-mensajes-de-error", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(825, "certificar-el-contador-mayor-a-cero-con-owp-y-dos-mensajes", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(864, "certificar-contador-acumulado-por-los-colaboradores-se-copia", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(794, "certificar-con-simulacion-S-el-contador-sale-uno", ["FM-CXF-01", "FM-CXF-05", "FM-CER-68"])
    desde_t5(902, "el-id-repetido-en-la-lista-de-T5-aqui-sale-una-vez-por-el-DISTINCT-y-se-certifica", ["FM-CXF-02", "FM-CXF-03"])
    desde_t5(902, "certificar-rechaza-el-id-duplicado-que-vienen-de-dos-filtros", ["FM-CXF-03", "FM-CER-02"], filtros=[filtro(), filtro()], tags=["PF-duplicados"])
    desde_t5(915, "certificar-el-hijo-del-switch-suma-contador-y-mensajes", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(910, "certificar-simulacion-con-el-hijo-del-switch", ["FM-CXF-01", "FM-CXF-05", "FM-CER-68"])
    # an exception of Certificar goes out of the class unchanged (the host rolls back)
    desde_t5(797, "certificar-falla-la-notificacion-asincrona-la-excepcion-sale-sin-cambios", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(799, "certificar-sin-parametros-de-lote-la-excepcion-del-legacy-sale-sin-cambios", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(849, "certificar-estado-no-esta-en-pla-estado-NullPointerException-sale-sin-cambios", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(895, "certificar-rescate-sin-resultado-de-custodia-NullPointerException", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(898, "certificar-indicadores-duplicados-en-la-vista-lanzan-Exception", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(900, "certificar-parametro-obligatorio-ausente-Exception-del-legacy", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(924, "certificar-el-hijo-falla-y-la-excepcion-sube", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(961, "certificar-falla-spot-la-excepcion-sale-sin-cambios", ["FM-CXF-01", "FM-CXF-05"])
    desde_t5(962, "certificar-falla-tesoreria-la-excepcion-sale-sin-cambios", ["FM-CXF-01", "FM-CXF-05"])
