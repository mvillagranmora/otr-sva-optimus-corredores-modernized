"""verify 2026-10-27 (v27c): FRESH inputs for ConsultarDetalleSolicitud (DET), seed 20261027. Writes ONLY under verify20261027/fresh/det/cases/VF-DET-nn/{case.json,seed.sql}.
Re-uses the helpers (read only) of cases_det_lib / cases_det_a; never touches cases-det/ nor any golden. Run: python v27c-gen_fresh_det.py
"""
import json
import pathlib
import random
import shutil
import sys

sys.dont_write_bytecode = True
HERE = pathlib.Path(__file__).resolve().parent
EQ = HERE.parent
sys.path.insert(0, str(EQ))
import cases_det_lib as L  # noqa: E402
import cases_det_a as A  # noqa: E402
from cases3a_lib import clonar_serie, moneda_formato  # noqa: E402

SEED = 20261027
R = random.Random(SEED)
OUT = HERE / "fresh" / "det" / "cases"
_n = {"n": 0}
fecha = A.fecha


def caso(ref, ent, filas=(), seed=(), colaboradores=None):
    _n["n"] += 1
    cid = "VF-DET-%02d" % _n["n"]
    doc = {"servicio": L.SERVICIO, "ref": ref, "esquemasExtra": ["schema-det.sql", "seed/base-det.sql"], "entrada": ent, "tablas": L.TABLAS}
    if colaboradores:
        doc["colaboradores"] = colaboradores
    d = OUT / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    datos = list(seed) + list(filas)
    (d / "seed.sql").write_text("-- fresh case (verify 2026-10-27, seed %d) on top of seed/base-fmm.sql + schema-det.sql + seed/base-det.sql\n" % SEED + "\n".join(datos) + "\n",
                                encoding="utf-8", newline="\n")
    return cid


def price():
    return round(R.uniform(700, 4200), 4)


def ident():
    return R.randint(3001, 8999)


def pct(v):
    return L.config("APV.PorcentajeRetencion", v)


def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)
    ent = L.entrada
    sol, valorizada, APV = A.sol, A.valorizada, A.APV
    usdfund = lambda: clonar_serie("FM002U", "20", fondo={"IdMoneda": "USD"}, serie={"IdInstrumento": 901, "DecimalesCuota": 2, "DecimalesValorCuota": 2}, tipo_cambio=(R.choice([921, 935, 962]), R.choice([918, 931, 955])))  # noqa: E731

    # 1 ING APO por monto con id y monto al azar
    i = ident(); p = price(); m = R.randint(10000, 9000000)
    caso("ING aporte por monto %d CLP, precio %s, id %d" % (m, p, i), ent(i), [sol(i, Monto=m)], colaboradores=valorizada(i, precio=p, monto_uf=round(m / 39000, 4)))

    # 2 PEN RES por cuotas fraccionarias
    i = ident(); p = price(); c = round(R.uniform(1, 900), 4)
    caso("PEN rescate por cuotas %s, precio %s" % (c, p), ent(i), [sol(i, Estado="PEN", TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM", CalculoPorMontoCuota="C", NumeroCuotas=c, Monto=0)],
         colaboradores=valorizada(i, precio=p, monto_uf=round(R.uniform(1, 400), 4)))

    # 3 frontera HALF_DOWN: Monto 3.0001 / precio 2 = 1.50005 exacto (cuotas con 4 decimales)
    i = ident()
    caso("ING por monto en la frontera de redondeo de cuotas: 3.0001 / 2.0000 = 1.50005 (4 decimales)", ent(i), [sol(i, Monto="3.0001")], colaboradores=valorizada(i, precio=2.0, monto_uf=0.0001))

    # 4 ING rescate total APV-B con retencion sobre el monto valorizado, porcentaje con signo '+'
    i = ident(); p = price(); c = round(R.uniform(10, 500), 4)
    caso("ING 'T' rescate total APV-B, medio CHQ, porcentaje '+19' (Long.parseLong lo acepta)", ent(i),
         [sol(i, **dict(APV, Estado="ING", CalculoPorMontoCuota="T", NumeroCuotas=c, Monto=0, IdMedioDePago="CHQ"))], seed=pct("+19"),
         colaboradores=valorizada(i, precio=p, monto_uf=round(R.uniform(1, 90), 4)))

    # 5 PEN rescate APV-AN valorizado, porcentaje '007' (ceros a la izquierda), redondeo
    i = ident(); p = price(); c = round(R.uniform(10, 500), 4)
    caso("PEN rescate APV-AN por cuotas, porcentaje '007'", ent(i),
         [sol(i, **dict(APV, Estado="PEN", IdRegimenTributario="APV-AN", CalculoPorMontoCuota="C", NumeroCuotas=c, Monto=0))], seed=pct("007"),
         colaboradores=valorizada(i, precio=p, monto_uf=round(R.uniform(1, 90), 4)))

    # 6 retencion en la mitad exacta de CLP: Monto 50 con 1% => 0.5 (CER, sin valorizar)
    i = ident()
    caso("CER APV-B Monto 50 y porcentaje 1 => 0.5: mitad exacta del formato de CLP", ent(i), [sol(i, **dict(APV, Estado="CER", Monto=50))], seed=pct("1"))

    # 7 USD en fondo CLP, diferido, aporte, paridad al azar
    i = ident(); p = price(); usd = R.randint(1000, 90000); f = round(R.uniform(880, 990), 2)
    caso("ING aporte en USD %d sobre fondo CLP (diferido), paridad %s" % (usd, f), ent(i), [sol(i, IdMoneda="USD", Monto=usd)],
         colaboradores=conjunto_usd(i, p, f, usd))

    # 8 EUR RES con formato EUR de 3 decimales truncado y retencion APV-B
    i = ident(); p = price(); eur = R.randint(2000, 80000); f = round(R.uniform(1000, 1100), 3)
    caso("PEN rescate APV-B en EUR (formato 3 decimales, trunca), paridad %s" % f, ent(i),
         [sol(i, **dict(APV, Estado="PEN", IdMoneda="EUR", Monto=eur, CalculoPorMontoCuota="M"))], seed=[moneda_formato("EUR", 3, "T")] + pct("13"),
         colaboradores=conjunto_usd(i, p, f, eur, moneda="EUR"))

    # 9 fondo en USD con precio 0 (cuotas por monto) y retencion
    i = ident()
    caso("ING rescate APV-B sobre fondo en USD con precio 0 (division por cero / ERROR_VALORCUOTA)", ent(i),
         [sol(i, **dict(APV, Estado="ING", IdSerieFondo="FM002U", IdMoneda="CLP", Monto=R.randint(100000, 900000), CalculoPorMontoCuota="M"))], seed=usdfund() + pct("12"),
         colaboradores=L.conjunto(L.precios((i, 0, 901)), L.paridad(t2=L.uf(i, 10, 39000, sistema=500000))))

    # 10 fondo en USD aporte en USD (misma moneda), tipo de cambio de la serie
    i = ident(); p = round(R.uniform(10, 90), 2)
    caso("ING aporte en USD sobre fondo USD (misma moneda): sin paridad diferida", ent(i), [sol(i, IdSerieFondo="FM002U", IdMoneda="USD", Monto=R.randint(1000, 50000))], seed=usdfund(),
         colaboradores=L.conjunto(L.precios((i, p, 901)), L.paridad(t2=L.uf(i, round(R.uniform(1, 90), 4), 39000, sistema=R.randint(1000000, 9000000)))))

    # 11 CIN con retencion: ING APV-B medio CIN: valoriza y NO retiene
    i = ident(); p = price(); c = round(R.uniform(10, 500), 4)
    caso("ING rescate APV-B con medio CIN: se valoriza y no retiene", ent(i),
         [sol(i, **dict(APV, Estado="ING", IdMedioDePago="CIN", CalculoPorMontoCuota="C", NumeroCuotas=c, Monto=0))], seed=pct("20"), colaboradores=valorizada(i, precio=p, monto_uf=2.5))

    # 12 traspaso: ING APV-B con orden de traspaso => no retiene; 2 ordenes duplican la fila => IllegalStateException al valorizar
    i = ident(); p = price()
    caso("ING APV-B con dos ordenes de traspaso: la fila repetida por el JOIN choca en el toMap", ent(i),
         [sol(i, **dict(APV, Estado="ING", CalculoPorMontoCuota="C", NumeroCuotas=40, Monto=0))],
         seed=pct("15") + ['INSERT INTO "BSCL.APV.BD::TRA.OrdenTraspaso" ("IdSolicitudAporteRescate", "IdTraspaso") VALUES (%d, 801);' % i,
                            'INSERT INTO "BSCL.APV.BD::TRA.OrdenTraspaso" ("IdSolicitudAporteRescate", "IdTraspaso") VALUES (%d, 802);' % i], colaboradores=valorizada(i, precio=p))

    # 13 estado con espacio al final y minusculas: no se valoriza
    i = ident()
    caso("Estado 'PE ' (la columna es de 3 caracteres) con precio escrito: NO es PEN, no se valoriza", ent(i), [sol(i, Estado="PE ", Monto="98765.4321", NumeroCuotas="3.21", ValorCuota="1111.1")],
         colaboradores=valorizada(i, precio=price()))

    # 14 ING con spot: rescate con spot (se sobrescribe) y fecha valor cuota de fin de ano
    i = ident(); p = price()
    caso("ING rescate CON Spot 300, FechaValorCuota 2026-12-31: MontoMonedaSistema se sobrescribe aunque tenga Spot", ent(i),
         [sol(i, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM", CalculoPorMontoCuota="C", NumeroCuotas="77.7", Monto=0, IdOperacionSpot=300, MontoMonedaSistema=4321,
              FechaValorCuota=fecha("2026-12-31"))],
         seed=['INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpot" ("IdOperacionSpotOrigen", "Estado", "IdOperacionSpot", "EstadoConfirmacion", "IdTipoOperacion", "FechaLiquidacion", '
               '"MontoMonedaSecundaria") VALUES (300, \'ACT\', 301, \'CNF\', \'VTUSD\', DATE \'2027-01-04\', 55);'], colaboradores=valorizada(i, precio=p, monto_uf=1.9))

    # 15 id como texto con ceros y empresa/usuario como texto
    i = ident()
    caso("entrada con p_SolicitudesAporteRescate '000%d', p_IdEmpresa '1' y p_IdUsuario '7' como texto (Gson los coacciona)" % i,
         {"p_IdEmpresa": "1", "p_IdUsuario": "7", "p_IdModulo": "FMM", "p_SolicitudesAporteRescate": "000%d" % i}, [sol(i, Estado="LIQ")])

    # 16 id negativo grande y Long maximo (nunca existe)
    caso("id Long.MAX_VALUE inexistente con valorizacion sin series", ent(9223372036854775807), [sol(5, Estado="CER")])

    # 17 solicitud con TraspasoDetalle y cliente destino: ING aporte por porcentaje
    i = ident(); p = price()
    caso("ING 'P' porcentaje con Monto %d y cuotas 0 en portafolio APV 504 (cliente destino)" % 777000, ent(i),
         [sol(i, CalculoPorMontoCuota="P", NumeroCuotas=0, Monto=777000, IdPortafolio=504, IdCliente=200)], colaboradores=valorizada(i, precio=p, monto_uf=19.9))
    print("fresh DET cases:", _n["n"])


def conjunto_usd(i, p, factor, monto_origen, moneda="USD"):
    destino = int(round(monto_origen * factor))
    return L.conjunto(L.precios((i, p)), L.paridad(t1=[{"IdInterno": i, "FactorCambioLocal": factor, "MontoDestino": destino}],
                                                  t2=L.uf(i, round(destino / 39000, 4), 39000, sistema=destino)))


if __name__ == "__main__":
    main()
