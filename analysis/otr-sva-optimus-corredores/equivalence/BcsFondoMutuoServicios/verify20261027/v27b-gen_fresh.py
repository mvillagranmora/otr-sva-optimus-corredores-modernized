"""verify 2026-10-27 (v27b): writes the FRESH inputs (case.json + seed.sql) of IFM, SFM and WS under verify20261027/fresh/<ifm|sfm|ws>/cases/VF-<IFM|SFM|WS>-nn/.
Seed 20261027 for every random value. Written AFTER reading the new code only to avoid repeating the development cases (their slugs and refs were listed first). Inputs only:
the legacy outputs come from the oracle scripts, never from here. Uses the row helpers of the development libs (cases_lect_lib, cases_sfm_lib) read-only.

  py -3 -I v27b-gen_fresh.py    (the -I is dropped: the libs are imported from the equivalence folder through sys.path)
"""
import json
import pathlib
import random
import sys

HERE = pathlib.Path(__file__).resolve().parent
EQ = HERE.parent
sys.path.insert(0, str(EQ))
import cases_lect_lib as L  # noqa: E402
import cases_sfm_lib as S  # noqa: E402

RND = random.Random(20261027)

TABLAS_IFM = [
    {"nombre": "BSCL.FondoMutuo.BD::FMM.ValorCuota", "orden": "\"IdSerieFondo\""},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo", "orden": "\"IdSerieFondo\", \"IdRegimenFondoMutuo\""},
]


def escribir(front, prefijo, n, servicio, esquemas, ref, ent, seed, tablas):
    cid = "%s-%02d" % (prefijo, n)
    d = HERE / "fresh" / front / "cases" / cid
    d.mkdir(parents=True, exist_ok=True)
    doc = {"servicio": servicio, "ref": ref, "esquemasExtra": esquemas, "entrada": ent, "tablas": tablas}
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    head = "-- v27b fresh case on top of seed/base-fmm.sql + %s (fake data)\n" % ", ".join(esquemas)
    (d / "seed.sql").write_text(head + ("\n".join(seed) if seed else "SELECT 1;") + "\n", encoding="utf-8", newline="\n")
    return cid


# ------------------------------------------------------------------ IFM
def ifm():
    out = []
    k = {"n": 0}

    def caso(ref, fondos, seed=None, empresa=1, **extra):
        k["n"] += 1
        ent = {"p_IdEmpresa": empresa, "p_IdUsuario": 7, "p_IdModulo": "FMM"}
        if fondos != "__ausente__":
            ent["p_EntradaFondosMutuos"] = fondos
        ent.update(extra)
        out.append(escribir("ifm", "VF-IFM", k["n"], "ConsultarInstrumentosFondosMutuos", ["seed/base-ifm.sql"], ref, ent, seed or [], TABLAS_IFM))

    def serie(sid, fid, fondo, ser, regs=(), vc=None, empresa=1):
        s = L.clonar_serie(sid, fid, fondo=fondo, serie=ser)
        for r, e in regs:
            s.append(L.reg_serie(sid, r, e, empresa=empresa))
        if vc:
            s.append(L.valor_cuota(sid, vc[0], vc[1]))
        return s

    # new dictionary rows: regime RV99, issuer AGF9 (with accents), category CAT9
    nuevo_universo = []
    nuevo_universo += L.regimen("RV99", "Renta Variable 'especial' Nandu", "S", "S")
    nuevo_universo += L.regimen("APVA", "APV Regimen A", "N", "")
    nuevo_universo.append(L.insert(L.V_EMISOR, {"IdEmisor": "AGF9", "NombreCompleto": "Administradora Ñandú & Cía. \"Nueve\""}))
    nuevo_universo.append(L.insert(L.V_CATEGORIA, {"IdCategoriaPerfilFondoMutuo": "CAT9", "NombreCategoriaPerfilFondoMutuo": "Agresivo"}))
    nuevo_universo.append(L.insert(L.V_CONFREG, {"IdRegimenFondoMutuoOrigen": "RV99", "IdRegimenFondoMutuoDestino": "APVA"}))
    nuevo_universo.append(L.insert(L.V_CONFREG, {"IdRegimenFondoMutuoOrigen": "RV99", "IdRegimenFondoMutuoDestino": "18Q"}))
    nuevo_universo.append(L.insert(L.V_CONFREG, {"IdRegimenFondoMutuoOrigen": "APVA", "IdRegimenFondoMutuoDestino": "GEN"}))

    f9 = lambda *a, **kw: L._f("AGF9", *a, **kw)  # noqa: E731
    # 801..806 on funds 901..904 (normal-branch limits: decimals, zero/negative values, huge amounts, EUR)
    base = list(nuevo_universo)
    base += serie("X801", "901", f9("N", "EUR", "CAT9", nombre="Fondo Euro Ñandú", nemo="FEÑ01"),
                  L._s("Serie Euro Alta Precisión", "E", fam="FAM9", minimo=0.01, maximo=99999999999.99, patrimonio=987654321098.76, dec=8),
                  [("RV99", "ACT"), ("APVA", "ACT"), ("18Q", "INA")], (123456789.12345678, "2026-03-02"))
    base += serie("X802", "901", False, L._s("Serie Euro Mínimo mayor que máximo", "F", fam="FAM9", minimo=5000, maximo=10, patrimonio=-5.5, dec=0), [("RV99", "ACT")], (0, "2026-01-01"))
    base += serie("X803", "902", f9("S", "USD", "CAT9", bloqueo="S", nombre="Fondo Ext Bloq", nemo="FXB02"),
                  L._s("Serie Ext Bloqueada", "B", fam="FAM9", restringe="S", owp="S", apv="S", dec=6), [("RV99", "ACT"), ("GEN", "ACT")], (-12.3456, "2026-02-28"))
    base += serie("X804", "903", f9("X", "CLP", "CAT1", bloqueo="N", nombre="Fondo Ext X", nemo="FXX03"),
                  L._s("Serie Ext X (permiteMLT nulo)", "X", fam="FAM9", restringe="N", estado="INA", dec=3), [("RV99", "ACT"), ("APVA", "INA")], (77.7, "2026-03-02"))
    base += serie("X805", "904", f9("N", "CLP", "CAT9", bloqueo="S", nombre="Fondo Nacional Bloq", nemo="FNB04"),
                  L._s("Serie Fondo Bloqueado, serie libre", "L", fam="FAM9", restringe="N", dec=4), [("RV99", "ACT"), ("57BIS", "ACT")], (1.0001, "2026-03-02"))
    base += serie("X806", "905", f9("N", "CLP", "CAT9", bloqueo="N", nombre="Fondo Nacional Libre", nemo="FNL05"),
                  L._s("Serie Fondo libre, serie restringida", "R", fam="FAM9", restringe="S", dec=4), [("RV99", "ACT")], (3.5, "2026-03-02"))

    caso("IFM-F01 normal: moneda EUR + familia FAM9 + categoria CAT9 sobre universo nuevo (serie 8 decimales, minimo 0.01, patrimonio 12 digitos)",
         {"IdMoneda": "EUR", "IdFamiliaFondo": "FAM9", "IdCategoriaPerfilInversionista": "CAT9"}, base)
    caso("IFM-F02 normal: IdRegimenFondoMutuo [RV99] sin filtro de estado: regimen INA no cuenta, PermiteMLT por FondoExtranjero N/S/X, orden de regimenes",
         {"IdRegimenFondoMutuo": ["RV99"]}, base)
    caso("IFM-F03 normal: OcultarBloqueados S con fondo bloqueado N/S x serie restringida N/S (las cuatro combinaciones) y emisor AGF9",
         {"OcultarBloqueados": "S", "IdEmisor": "AGF9"}, base)
    caso("IFM-F04 normal: OcultarBloqueados ' S' (espacio) no coincide (muestra bloqueados) + estado ACT + moneda CLP + categoria CAT9",
         {"OcultarBloqueados": " S", "IdEstado": "ACT", "IdMoneda": "CLP", "IdCategoriaPerfilInversionista": "CAT9"}, base)
    caso("IFM-F05 normal: lista de regimenes con duplicados, vacio y minuscula [APVA, APVA, '', 'apva', 57BIS] + OWP N + EsAPV N",
         {"IdRegimenFondoMutuo": ["APVA", "APVA", "", "apva", "57BIS"], "InstrumentoOWP": "N", "EsAPV": "N"}, base)
    caso("IFM-F06 normal: EsAPV S + InstrumentoOWP S (X803) y luego estado INA con extranjero X",
         {"EsAPV": "S", "InstrumentoOWP": "S"}, base)
    caso("IFM-F07 normal: FondoExtranjero X + IdEstado INA (serie X804, permiteMLT nulo)",
         {"FondoExtranjero": "X", "IdEstado": "INA"}, base)
    caso("IFM-F08 reinversion: origen X801 (RV99, APVA, 18Q INA) destinos por config RV99->APVA/18Q, APVA->GEN; sin filtro ACT en el destino",
         {"IdSerieFondoOrigen": "X801"}, base)
    caso("IFM-F09 reinversion: origen X805 (RV99+57BIS) con OcultarBloqueados S ignorado, fondo bloqueado X805/X803 excluido, serie restringida X806 excluida",
         {"IdSerieFondoOrigen": "X805", "OcultarBloqueados": "S"}, base)
    caso("IFM-F10 reinversion: origen X806 + filtro IdRegimenFondoMutuo [APVA] (sin ACT) + EsAPV N + moneda EUR",
         {"IdSerieFondoOrigen": "X806", "IdRegimenFondoMutuo": ["APVA"], "EsAPV": "N", "IdMoneda": "EUR"}, base)
    caso("IFM-F11 reinversion: origen X803 (RV99+GEN) acotado a IdSerieFondo X804 (serie INA, fondo extranjero X)",
         {"IdSerieFondoOrigen": "X803", "IdSerieFondo": "X804"}, base)
    emp2 = list(base)
    emp2 += serie("X807", "906", f9("N", "CLP", "CAT9", empresa=2, nombre="Fondo Empresa Dos", nemo="FE2"),
                  L._s("Serie Empresa Dos", "D", fam="FAM9", empresa=2, minimo=123, maximo=456789, patrimonio=31.5, dec=5), [("RV99", "ACT"), ("APVA", "ACT")], (42.4242, "2026-03-01"), empresa=2)
    caso("IFM-F12 normal: empresa 2 (serie X807 de empresa 2 con regimenes RV99/APVA; las series de empresa 1 no salen)", {"IdFamiliaFondo": "FAM9"}, emp2, empresa=2)
    caso("IFM-F13 reinversion: origen X802 (RV99) acotado a estado ACT: destinos por RV99 y por config (APVA, 18Q) con regimen INA admitido",
         {"IdSerieFondoOrigen": "X802", "IdEstado": "ACT"}, base)
    # large set: 40 random series, random attributes (seed 20261027), then a random filter combination
    gran = list(nuevo_universo)
    regs_pool = ["RV99", "APVA", "18Q", "18T", "57BIS", "GEN", "NINGUNO"]
    for i in range(40):
        sid = "R%03d" % (200 + i)
        fid = str(1000 + i // 3)
        fon = None
        if i % 3 == 0:
            fon = L._f(RND.choice(["AGF1", "AGF2", "AGF9", "EXT1", "AGF3"]), RND.choice(["N", "N", "S", "X"]), RND.choice(["CLP", "USD", "EUR"]),
                       RND.choice(["CAT1", "CAT2", "CAT9", "CAT3"]), bloqueo=RND.choice(["N", "N", "S"]), nombre="Fondo Rnd %d" % (1000 + i // 3), nemo="FR%03d" % i)
        rg = [(r, RND.choice(["ACT", "ACT", "INA"])) for r in RND.sample(regs_pool, RND.randint(0, 4))]
        gran += serie(sid, fid, fon if i % 3 == 0 else False,
                      L._s("Serie Rnd %d" % i, chr(65 + i % 26), fam=RND.choice(["FAM1", "FAM2", "FAM9"]), estado=RND.choice(["ACT", "ACT", "INA"]),
                           minimo=RND.randint(0, 9999), maximo=RND.randint(0, 99999999), comision=RND.choice(["S", "N"]), owp=RND.choice(["S", "N"]), apv=RND.choice(["S", "N"]),
                           restringe=RND.choice(["N", "N", "S"]), patrimonio=round(RND.random() * 1e9, 2), dec=RND.randint(0, 8)),
                      rg, (round(RND.random() * 5000, RND.randint(0, 8)), "2026-0%d-%02d" % (RND.randint(1, 3), RND.randint(1, 28))))
    caso("IFM-F14 normal: 40 series aleatorias (semilla 20261027) con filtro estado ACT + regimenes [18Q, RV99, GEN] + OcultarBloqueados s minuscula",
         {"IdEstado": "ACT", "IdRegimenFondoMutuo": ["18Q", "RV99", "GEN"], "OcultarBloqueados": "s"}, gran)
    caso("IFM-F15 reinversion: origen R205 (regimenes aleatorios) sobre las 40 series aleatorias, filtro moneda CLP",
         {"IdSerieFondoOrigen": "R205", "IdMoneda": "CLP"}, gran)
    caso("IFM-F16 normal: sin filtros sobre las 40 series aleatorias (orden de la consulta, determinismo)", {}, gran)
    return out


# ------------------------------------------------------------------ SFM / WS
def sfm_ws():
    sfm, ws = [], []
    ks = {"n": 0}
    kw = {"n": 0}
    T = S.TABLAS

    def caso(servicio, ref, ent, seed):
        if servicio == S.SFM:
            ks["n"] += 1
            sfm.append(escribir("sfm", "VF-SFM", ks["n"], servicio, ["schema-sfm.sql", "seed/base-sfm.sql"], "[SFM] " + ref, ent, seed, T))
        else:
            kw["n"] += 1
            ws.append(escribir("ws", "VF-WS", kw["n"], servicio, ["schema-sfm.sql", "seed/base-sfm.sql"], "[WS] " + ref, ent, seed, T))

    ent = S.entrada
    off = lambda v: S.config("PLA.SegundosZonaHoraria", None if v is None else str(v))  # noqa: E731

    # ---- SFM (HoraSolicitud text + offset, concatenated FechaIngreso)
    caso(S.SFM, "offset +7199 (no multiplo de hora): horas 22:59:59 .. 00:00:00 en cinco solicitudes",
         ent(), off(7199) + [S.fila(i + 1, 7199, HoraSolicitud=h, FechaSolicitud="2026-03-%02d" % (10 + i)) for i, h in enumerate(["22:59:59", "00:00:00", "12:00:01", "23:59:59", "01:02:03"])])
    caso(S.SFM, "offset -43200 y horas 00:00:01 / 12:00:00 / 11:59:59",
         ent(), off(-43200) + [S.fila(i + 1, -43200, HoraSolicitud=h) for i, h in enumerate(["00:00:01", "12:00:00", "11:59:59"])])
    caso(S.SFM, "offset 86399 (maximo del dia) con hora 00:00:00 y 23:59:59",
         ent(), off(86399) + [S.fila(1, 86399, HoraSolicitud="00:00:00"), S.fila(2, 86399, HoraSolicitud="23:59:59")])
    caso(S.SFM, "offset -86400 (un dia exacto hacia atras) con hora 00:00:00",
         ent(), off(-86400) + [S.fila(1, -86400, HoraSolicitud="00:00:00"), S.fila(2, -86400, HoraSolicitud="06:30:15")])
    caso(S.SFM, "grupo FolioContrato 777 con INGRESADO, EN PROCESO, TERMINADO, ANULADO y p_Estado 'EN PROCESO'",
         ent(p_Estado="EN PROCESO"),
         [S.fila(i + 1, FolioContrato=777, EstadoDetalle=e, MontoPesosOrigen="%d.5" % (100 * (i + 1)), MontoCuotasOrigen="%d.25000000" % (i + 3))
          for i, e in enumerate(["INGRESADO", "EN PROCESO", "TERMINADO", "ANULADO"])] + [S.fila(9, FolioContrato=778, EstadoDetalle="ANULADO")])
    caso(S.SFM, "REV: dos ANULADO con FolioRelacionado 9001 y p_Estado ANULADO; un REV TERMINADO aparte con numero comun",
         ent(p_Estado="ANULADO"),
         [S.fila(1, TipoSolicitud="REV", FolioRelacionado=9001, EstadoDetalle="ANULADO", MontoPesosDestino="40.1", MontoCuotasDestino="0.4"),
          S.fila(2, TipoSolicitud="REV", FolioRelacionado=9001, EstadoDetalle="ANULADO"),
          S.fila(3, TipoSolicitud="REV", FolioRelacionado=9002, EstadoDetalle="TERMINADO", MontoPesosDestino="12.3", MontoCuotasDestino="0.123"),
          S.fila(3, TipoSolicitud="REV", FolioRelacionado=0, EstadoDetalle="TERMINADO", MontoPesosDestino="7", MontoCuotasDestino="0.07")])
    caso(S.SFM, "REV no transitivo: A(folio 1,num 10) B(folio 1,num 11) C(folio 2,num 11) mezclando EN PROCESO y TERMINADO",
         ent(),
         [S.fila(10, TipoSolicitud="REV", FolioRelacionado=1, EstadoDetalle="EN PROCESO", MontoPesosDestino="1", MontoCuotasDestino="1"),
          S.fila(11, TipoSolicitud="REV", FolioRelacionado=1, EstadoDetalle="TERMINADO", MontoPesosDestino="2", MontoCuotasDestino="2"),
          S.fila(11, TipoSolicitud="REV", FolioRelacionado=2, EstadoDetalle="TERMINADO", MontoPesosDestino="4", MontoCuotasDestino="4")])
    rows = []
    for i in range(150):
        rows.append(S.fila(1000 + i, IdCliente=RND.randint(500, 530), TipoSolicitud=RND.choice(["APO", "RES", "REV"]),
                           EstadoDetalle=RND.choice(["INGRESADO", "EN PROCESO", "TERMINADO", "ANULADO", "RECHAZADO", None]),
                           FolioContrato=RND.choice([0, 0, 91, 92, 93]), FolioRelacionado=RND.choice([0, 0, 71, 72]),
                           FechaSolicitud="2026-0%d-%02d" % (RND.randint(1, 3), RND.randint(1, 28)), HoraSolicitud="%02d:%02d:%02d" % (RND.randint(0, 23), RND.randint(0, 59), RND.randint(0, 59)),
                           MontoPesosOrigen="%d.%02d" % (RND.randint(0, 99999), RND.randint(0, 99)), MontoCuotasOrigen="%d.%06d" % (RND.randint(0, 999), RND.randint(0, 999999)),
                           MontoPesosDestino="%d.%02d" % (RND.randint(0, 9999), RND.randint(0, 99)), MontoCuotasDestino="%d.%04d" % (RND.randint(0, 99), RND.randint(0, 9999))))
    caso(S.SFM, "150 filas aleatorias (semilla 20261027), 31 clientes, tipos APO/RES/REV y estados mixtos, sin filtros", ent(), rows)
    caso(S.SFM, "las mismas 150 filas: p_FechaDesde 2026-02-10 + p_FechaHasta 2026-03-05 + p_Estado TERMINADO",
         ent(p_FechaDesde="2026-02-10", p_FechaHasta="2026-03-05", p_Estado="TERMINADO"), rows)
    caso(S.SFM, "filtros nuevos p_TipoAhorro A9 + p_TipoCuenta C9 + p_Origen MOB + p_Folio con acento + p_IdCliente 4242",
         ent(p_TipoAhorro="A9", p_TipoCuenta="C9", p_Origen="MOB", p_Folio="Folio-Ñ-77", p_IdCliente=4242),
         [S.fila(1, IdCliente=4242, TipoAhorro="A9", TipoCuenta="C9", Origen="MOB", Folio="Folio-Ñ-77"), S.fila(2, IdCliente=4242, TipoAhorro="A9", TipoCuenta="C9", Origen="MOB", Folio="Folio-Ñ-78"),
          S.fila(3, IdCliente=4243, TipoAhorro="A9", TipoCuenta="C9", Origen="MOB", Folio="Folio-Ñ-77")])
    caso(S.SFM, "ADC S + no automaticas S con EsADC e IngresoAutomatico nulos / vacios / S / N",
         ent(), S.config("PLA.FiltroAdministracionCarteraWS", "S") + S.parametro("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", "S") +
         [S.fila(i + 1, IdCliente=321, Folio="F-321-%d" % i, MontoPesosOrigen="4321.12", EsADC=a, IngresoAutomatico=b) for i, (a, b) in enumerate([("N", "N"), ("S", "N"), ("N", "S"), (None, "N"), ("N", None), ("", ""), ("n", "n")])])
    caso(S.SFM, "montos a escala 8 en 40 filas TERMINADO del FolioContrato 555 (0.00000001 y 123456789012345678.12345678, DECIMAL128) y 3 filas del 556 con un EN PROCESO",
         ent(), [S.fila(i + 1, FolioContrato=555, EstadoDetalle="TERMINADO", MontoPesosOrigen="0.00000001" if i < 39 else "123456789012345678.12345678",
                        MontoCuotasOrigen="%d.00000001" % i) for i in range(40)] +
         [S.fila(50 + i, FolioContrato=556, EstadoDetalle=("EN PROCESO" if i == 0 else "INGRESADO"), MontoPesosOrigen="99999999999999999.99999999", MontoCuotasOrigen="0.00000009") for i in range(3)])
    caso(S.SFM, "montos negativos y cero con p_IdSerieFondo ZZ9: el filtro SQL recorta el grupo antes del estado global",
         ent(p_IdSerieFondo="ZZ9"),
         [S.fila(1, FolioContrato=61, IdSerieFondo="ZZ9", EstadoDetalle="TERMINADO", MontoPesosOrigen="-50.5", MontoCuotasOrigen="-0.5"),
          S.fila(2, FolioContrato=61, IdSerieFondo="OTRA", EstadoDetalle="INGRESADO", MontoPesosOrigen="999", MontoCuotasOrigen="9"),
          S.fila(3, FolioContrato=61, IdSerieFondo="ZZ9", EstadoDetalle="TERMINADO", MontoPesosOrigen="0", MontoCuotasOrigen="0")])
    caso(S.SFM, "fechas limite 2026-12-31 y 2024-02-29 con p_FechaDesde 2024-02-29 y p_FechaHasta 2026-12-31 (inclusive en ambos extremos) y hora 23:59:59 con offset +3600",
         ent(p_FechaDesde="2024-02-29", p_FechaHasta="2026-12-31"),
         off(3600) + [S.fila(1, 3600, FechaSolicitud="2024-02-29", HoraSolicitud="23:59:59"), S.fila(2, 3600, FechaSolicitud="2026-12-31", HoraSolicitud="23:59:59"),
                      S.fila(3, 3600, FechaSolicitud="2024-02-28", HoraSolicitud="00:00:00"), S.fila(4, 3600, FechaSolicitud="2027-01-01", HoraSolicitud="00:00:00")])
    caso(S.SFM, "HoraSolicitud fuera de rango 25:61:61 (limite de parseo) en una fila y 07:05:09 en otra",
         ent(), [S.fila(1, HoraSolicitud="25:61:61"), S.fila(2, HoraSolicitud="07:05:09")])
    caso(S.SFM, "estado en minusculas ingresado / En Proceso: ni coinciden con las constantes: Estado global vacio y se descartan; una TERMINADO sobrevive",
         ent(), [S.fila(1, FolioContrato=31, EstadoDetalle="ingresado"), S.fila(2, FolioContrato=32, EstadoDetalle="En Proceso"), S.fila(3, FolioContrato=33, EstadoDetalle="TERMINADO")])

    # ---- WS (FechaIngresoLocal, Tesoreria)
    rows_ws = []
    tes_ws = []
    nie = 7000
    for i in range(120):
        ie = 0
        if RND.random() < 0.5:
            nie += 1
            ie = nie
            tes_ws += S.tes(ie, RND.choice(["RESCFM", "APORFM", "RESCFM", "OTRO"]),
                            [(nie * 10 + j, 8000 + nie, RND.choice(["ING", "ANU", "ACT", None])) for j in range(RND.choice([0, 1, 2, 3]))])
        ts = "2026-%02d-%02d %02d:%02d:%02d" % (RND.randint(1, 12), RND.randint(1, 28), RND.randint(0, 23), RND.randint(0, 59), RND.randint(0, 59))
        rows_ws.append(S.fila(2000 + i, IdCliente=RND.randint(600, 625), TipoSolicitud=RND.choice(["APO", "RES", "REV"]),
                              EstadoDetalle=RND.choice(["INGRESADO", "EN PROCESO", "TERMINADO", "ANULADO", None, "X"]), FolioContrato=RND.choice([0, 0, 81, 82]),
                              FolioRelacionado=RND.choice([0, 0, 61, 62]), IdIngresoEgreso=ie, FechaIngresoLocal=ts, FechaSolicitud=ts[:10], HoraSolicitud=ts[11:],
                              MontoPesosOrigen="%d.%02d" % (RND.randint(0, 99999), RND.randint(0, 99)), MontoCuotasOrigen="%d.%06d" % (RND.randint(0, 999), RND.randint(0, 999999)),
                              MontoPesosDestino="%d.%02d" % (RND.randint(0, 9999), RND.randint(0, 99)), MontoCuotasDestino="%d.%04d" % (RND.randint(0, 99), RND.randint(0, 9999))))
    caso(S.WS, "120 filas aleatorias (semilla 20261027) con IngresoEgreso/ComprobanteDetalle aleatorios (0..3 detalles, ANU/ING/ACT/nulo): duplicacion de filas por el join",
         ent(), tes_ws + rows_ws)
    caso(S.WS, "las mismas 120 filas con p_Estado EN PROCESO y p_FechaDesde 2026-04-01",
         ent(p_Estado="EN PROCESO", p_FechaDesde="2026-04-01"), tes_ws + rows_ws)
    caso(S.WS, "FechaIngresoLocal 2026-12-31 23:59:59.999999 y 2024-02-29 00:00:00.000001 con offset +86399",
         ent(), off(86399) + [S.fila(1, 86399, FechaIngresoLocal="2026-12-31 23:59:59.999999"), S.fila(2, 86399, FechaIngresoLocal="2024-02-29 00:00:00.000001")])
    caso(S.WS, "FechaIngresoLocal 1999-12-31 23:59:59 / 2038-01-19 03:14:08 / 0001-01-01 00:00:00 con offset -34200",
         ent(), off(-34200) + [S.fila(1, -34200, FechaIngresoLocal="1999-12-31 23:59:59"), S.fila(2, -34200, FechaIngresoLocal="2038-01-19 03:14:08"),
                               S.fila(3, -34200, FechaIngresoLocal="0001-01-01 00:00:00")])
    caso(S.WS, "FechaSolicitud (UTC) distinta de la fecha local: p_FechaDesde 2026-05-02 filtra por la columna de la vista, no por la fecha local",
         ent(p_FechaDesde="2026-05-02", p_FechaHasta="2026-05-02"),
         off(-10800) + [S.fila(1, FechaSolicitud="2026-05-02", HoraSolicitud="01:00:00", FechaIngresoLocal="2026-05-01 22:00:00"),
                        S.fila(2, FechaSolicitud="2026-05-03", HoraSolicitud="01:00:00", FechaIngresoLocal="2026-05-02 22:00:00"),
                        S.fila(3, FechaSolicitud="2026-05-02", HoraSolicitud="23:59:59", FechaIngresoLocal="2026-05-02 20:59:59")])
    caso(S.WS, "IngresoEgreso 9101 (RESCFM) con tres detalles (ING, anu en minuscula, ACT) y 9102 (APORFM) sin detalle; filas REV y APO",
         ent(), S.tes(9101, "RESCFM", [(91011, 1, "ING"), (91012, 1, "anu"), (91013, 2, "ACT")]) + S.tes(9102, "APORFM", []) +
         [S.fila(1, IdIngresoEgreso=9101, TipoSolicitud="REV", FolioRelacionado=44, EstadoDetalle="EN PROCESO", MontoPesosDestino="5", MontoCuotasDestino="0.5"),
          S.fila(2, IdIngresoEgreso=9102, TipoSolicitud="APO", FolioContrato=45), S.fila(3, IdIngresoEgreso=-5, TipoSolicitud="RES", FolioContrato=45)])
    caso(S.WS, "detalles ANU compartidos: un ComprobanteDetalle ANU referenciado por dos IngresoEgreso (NOT IN subconsulta) y otro detalle valido en uno de ellos",
         ent(), S.tes(9201, "RESCFM", [(92011, 5, "ANU")]) + ["INSERT INTO \"BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen\" VALUES (92011, 9202);",
                                                             "INSERT INTO \"BSCL.Tesoreria.BD::TES.IngresoEgreso\" VALUES (9202, 'APORFM');",
                                                             "INSERT INTO \"BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen\" VALUES (92099, 9202);",
                                                             "INSERT INTO \"BSCL.Tesoreria.BD::TES.ComprobanteDetalle\" VALUES (92099, 6, 'ING');"] +
         [S.fila(1, IdIngresoEgreso=9201), S.fila(2, IdIngresoEgreso=9202)])
    caso(S.WS, "ADC S + no automaticas S sobre filas con Tesoreria y offset 19800 (5.5 h)",
         ent(), off(19800) + S.config("PLA.FiltroAdministracionCarteraWS", "S") + S.parametro("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", "S") +
         S.tes(9301, "RESCFM", [(93011, 3, "ING"), (93012, 3, "ACT")]) +
         [S.fila(1, 19800, EsADC="N", IngresoAutomatico="N", IdIngresoEgreso=9301), S.fila(2, 19800, EsADC="S", IngresoAutomatico="N"), S.fila(3, 19800, EsADC="N", IngresoAutomatico="S")])
    caso(S.WS, "estados mixtos en un FolioContrato 303: INGRESADO + TERMINADO + EN PROCESO + nulo y p_Estado vacio; otro grupo ANULADO + ANULADO",
         ent(), [S.fila(i + 1, FolioContrato=303, EstadoDetalle=e, MontoPesosOrigen="%d.75" % (10 * (i + 1))) for i, e in enumerate(["INGRESADO", "TERMINADO", "EN PROCESO", None])] +
         [S.fila(11, FolioContrato=304, EstadoDetalle="ANULADO"), S.fila(12, FolioContrato=304, EstadoDetalle="ANULADO")])
    caso(S.WS, "REV con montos destino a escala 8 y Tesoreria: dos REV mismo FolioRelacionado 515, uno TERMINADO (cuotas 0.00000001) y otro EN PROCESO (cuotas 99999999999999999.99999999)",
         ent(), S.tes(9401, "RESCFM", [(94011, 1, "ING"), (94012, 1, "ING")]) +
         [S.fila(1, TipoSolicitud="REV", FolioRelacionado=515, EstadoDetalle="TERMINADO", MontoPesosDestino="0.00000001", MontoCuotasDestino="0.00000001", IdIngresoEgreso=9401),
          S.fila(2, TipoSolicitud="REV", FolioRelacionado=515, EstadoDetalle="EN PROCESO", MontoPesosDestino="99999999999999999.99999999", MontoCuotasDestino="99999999999999999.99999999")])
    caso(S.WS, "filtros p_IdCliente 777 + p_TipoSolicitud RES + p_Origen API + p_IdSerieFondo QW1 + p_Folio sobre cinco filas con distintos desaciertos",
         ent(p_IdCliente=777, p_TipoSolicitud="RES", p_Origen="API", p_IdSerieFondo="QW1", p_Folio="F-7"),
         [S.fila(1, IdCliente=777, TipoSolicitud="RES", Origen="API", IdSerieFondo="QW1", Folio="F-7"), S.fila(2, IdCliente=777, TipoSolicitud="RES", Origen="API", IdSerieFondo="QW1", Folio="F-8"),
          S.fila(3, IdCliente=778, TipoSolicitud="RES", Origen="API", IdSerieFondo="QW1", Folio="F-7"), S.fila(4, IdCliente=777, TipoSolicitud="APO", Origen="API", IdSerieFondo="QW1", Folio="F-7"),
          S.fila(5, IdCliente=777, TipoSolicitud="RES", Origen="WEB", IdSerieFondo="QW1", Folio="F-7")])
    caso(S.WS, "FechaIngresoLocal con milisegundos 2026-07-04 08:09:10.5 y 10.123456 y 10.000100: formato Gson del timestamp y la hora local",
         ent(), [S.fila(1, FechaIngresoLocal="2026-07-04 08:09:10.5"), S.fila(2, FechaIngresoLocal="2026-07-04 08:09:10.123456"), S.fila(3, FechaIngresoLocal="2026-07-04 08:09:10.0001")])
    caso(S.WS, "tres solicitudes de un mismo NumSolicitud 88 con tipos APO, RES y REV (la asociacion REV usa solo REV; la de aporte/rescate excluye REV)",
         ent(), [S.fila(88, TipoSolicitud="APO", EstadoDetalle="TERMINADO", MontoPesosOrigen="10", MontoCuotasOrigen="1"),
                 S.fila(88, TipoSolicitud="RES", EstadoDetalle="EN PROCESO", MontoPesosOrigen="20", MontoCuotasOrigen="2"),
                 S.fila(88, TipoSolicitud="REV", EstadoDetalle="ANULADO", MontoPesosDestino="30", MontoCuotasDestino="3")])
    gr = []
    tes_g = []
    for i in range(200):
        tes_g += S.tes(7500 + i, "RESCFM", [(75000 + i * 2 + j, 9500, "ING") for j in range(RND.choice([1, 1, 2]))])
        gr.append(S.fila(3000 + i, IdCliente=700, FolioContrato=1234, EstadoDetalle="TERMINADO", IdIngresoEgreso=7500 + i,
                         MontoPesosOrigen="%d.%02d" % (RND.randint(1, 999), RND.randint(0, 99)), MontoCuotasOrigen="%d.%06d" % (RND.randint(0, 9), RND.randint(0, 999999)),
                         FechaIngresoLocal="2026-03-%02d %02d:%02d:%02d" % (RND.randint(1, 28), RND.randint(0, 23), RND.randint(0, 59), RND.randint(0, 59))))
    caso(S.WS, "grupo de 200 solicitudes TERMINADO del mismo FolioContrato 1234 con 1..2 detalles de Tesoreria cada una (filas duplicadas por el join, sumas DECIMAL128)", ent(), tes_g + gr)
    return sfm, ws


if __name__ == "__main__":
    a = ifm()
    s, w = sfm_ws()
    print("IFM", len(a), "SFM", len(s), "WS", len(w))
