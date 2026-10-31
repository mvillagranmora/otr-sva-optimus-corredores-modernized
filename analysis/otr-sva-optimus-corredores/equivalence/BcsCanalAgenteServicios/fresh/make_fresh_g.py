"""BcsCanalAgenteServicios, verificacion independiente 2026-10-30 (ronda g): ENTRADAS frescas PROPIAS del verificador, ids CAN-FRG-nnn (el desarrollo no uso ninguna de estas).
Escribe SOLO en fresh/inputs/CAN-FRG-* (case.json + seed.sql). NO borra nada (make_cases.py del oraculo hace rmtree de cases/ y por eso no se ejecuto). No toca cases/ ni ningun golden.
Reutiliza la biblioteca de generadores del oraculo (cases_can_lib) con la carpeta de salida cambiada.

  py -3 -I fresh/make_fresh_g.py
"""
import json
import pathlib
import shutil
import sys

AQUI = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(AQUI.parent))
import cases_can_lib as L  # noqa: E402
from cases_can_lib import *  # noqa: E402,F401,F403
import cases_can_prc as P  # noqa: E402

INPUTS = AQUI / "inputs"
INPUTS.mkdir(exist_ok=True)
L.CASES = AQUI / "_gen"          # se genera aqui y SOLO se copian a inputs/ los casos que aun no existen (nunca se sobrescribe ni se borra un caso ya grabado)
if L.CASES.exists():
    shutil.rmtree(L.CASES)
L.CASES.mkdir()

LOGICA = "ConsultarHistorial.logica"
CALL = "ConsultarHistorial.call"
TP = [dump_proc()]
g = Gen("FRG")


def base(movs, **kw):
    return cartola(resp(movs, **kw))


def mundo2():
    """mundo() del desarrollo MAS portafolios nuevos: 555 (cliente 55, persona 5, documento 5-K), 1 (cliente 5, persona 6, documento 66.666.666-6), 9007199254740993 (cliente 77, sin persona);
    comprobantes nuevos: 6001 (folio 9999999999, DTE 123456, pdf con tilde), 6002 (folio 1, DTE 0, pdf vacio), 6003 (folio 2, DTE 3, sin pdf), 6004 (folio 3000000000)"""
    return mundo() + [
        documento(55, 5, "5-K"), documento(66, 6, "66.666.666-6"),
        persona(5, 55), persona(6, 66),
        cliente(55, 5), cliente(5, 6), cliente(77, 9),
        portafolio(555, 55, "PF-555"), portafolio(1, 5, "PF-001"), portafolio(9007199254740993, 77, "PF-BIG"),
        comprobante(6001, 9999999999, 123456, "https://dte.example/ñandú.pdf"), comprobante(6002, 1, 0, ""), comprobante(6003, 2, 3, None), comprobante(6004, 3000000000, 1, "x"),
    ]


M = mundo2()
REF = ["B-CA-03", "B-CA-06", "B-CA-10"]

# ---- otros ids de portafolio
g.caso("portafolio-555-con-comprobante-grande", LOGICA, entrada_h(555), REF, base([mov(1, 6001, portafolio=555)]), tablas=TP, seed=M)
g.caso("portafolio-1-id-minimo", LOGICA, entrada_h(1), REF, base([mov(1, 6002, portafolio=1), mov(2, 6003, portafolio=1)]), tablas=TP, seed=M)
g.caso("portafolio-bigint-mayor-que-2-a-la-53", LOGICA, entrada_h(9007199254740993), REF, base([mov(9007199254740993, 6004, portafolio=9007199254740993)]), tablas=TP, seed=M)
g.caso("portafolio-negativo-no-existe", LOGICA, entrada_h(-1), ["D-CAN-01"], base([mov(1, 5001)]), tablas=TP, seed=M)
g.caso("portafolio-existe-cliente-sin-persona", LOGICA, entrada_h(9007199254740993, p_IdModulo="PLA", p_IdMoneda="USD"), ["B-CA-04"], base([mov(5, 5001, portafolio=9007199254740993)]), tablas=TP, seed=M)

# ---- fechas limite
for slug, d, h in (("bisiesto-2024-02-29", "2024-02-29", "2024-02-29"), ("cambio-de-anio", "2026-12-31", "2027-01-01"), ("centinela-0001-a-9999", "0001-01-01", "9999-12-31"),
                   ("con-hora-y-zona", "2026-02-01T00:00:00Z", "2026-02-28 23:59:59.999"), ("vacias", "", "")):
    g.caso("fechas-limite-%s" % slug, LOGICA, entrada_h(555, p_FechaDesde=d, p_FechaHasta=h), ["B-CA-02", "D-CAN-09"], base([mov(1, 6001, portafolio=555, FechaSistema=h[:10] or None)]), tablas=TP, seed=M)

# ---- cartolas grandes, parciales y sin comprobante
g.caso("ciento-cincuenta-movimientos-mezcla-de-comprobantes", LOGICA, entrada_h(100), ["B-CA-03", "B-CA-07"],
       base([mov(5000 + i, (5001, 5002, 5003, 5004, 6001, None, 9999, 7000 + i)[i % 8], portafolio=(100, 200, 300, 400, 555)[i % 5]) for i in range(150)]), tablas=TP, seed=M)
g.caso("mil-cinco-movimientos-un-lote-grande", LOGICA, entrada_h(100), ["B-CA-03", "B-CA-07"],
       base([mov(i + 1, (5001, 5003, None)[i % 3], portafolio=100) for i in range(1005)]), tablas=TP, seed=M)
g.caso("comprobantes-parciales-la-mitad-no-existe", LOGICA, entrada_h(200), ["B-CA-04"], base([mov(i, 5001 if i % 2 else 8000 + i, portafolio=200) for i in range(1, 11)]), tablas=TP, seed=M)
g.caso("diez-movimientos-sin-comprobante", LOGICA, entrada_h(300), ["B-CA-04", "D-CAN-07"], base([mov(i, None, portafolio=300) for i in range(1, 11)]), tablas=TP, seed=M)
g.caso("ids-repetidos-tres-veces-con-comprobantes-distintos", LOGICA, entrada_h(), ["D-CAN-06", "B-CA-12"], base([mov(7, 5001), mov(7, 6001), mov(7, 5003), mov(8, 6002), mov(8, 6002)]), tablas=TP, seed=M)
g.caso("ids-repetidos-entre-portafolios", LOGICA, entrada_h(), ["D-CAN-06"], base([mov(7, 5001, portafolio=100), mov(7, 5002, portafolio=200), mov(7, None, portafolio=555)]), tablas=TP, seed=M)
g.caso("filas-ajenas-con-ids-repetidos-y-comprobantes-nulos", LOGICA, entrada_h(), ["D-CAN-12"], base([mov(1, 5001), mov(2, 6001)]), tablas=TP,
       seed=M + [insertar(T_PROC, IdCargoAbonoMovimiento=1, IdPortafolio=555, IdComprobante=6002), insertar(T_PROC, IdCargoAbonoMovimiento=2, IdPortafolio=200, IdComprobante=None),
                 insertar(T_PROC, IdCargoAbonoMovimiento=None, IdPortafolio=None, IdComprobante=6003)])

# ---- montos con escalas distintas (la cartola los entrega como JSON; no tocan la base)
g.caso("montos-con-escalas-distintas", LOGICA, entrada_h(555), ["B-CA-06"],
       base([mov(1, 6001, portafolio=555, Cargo=0, Abono=1), mov(2, 6001, portafolio=555, Cargo=1.5, Abono=1.50), mov(3, 6001, portafolio=555, Cargo=100.10, Abono=0.1),
             mov(4, 6001, portafolio=555, Cargo=1000, Abono=1e3), mov(5, 6001, portafolio=555, Cargo=-0.0, Abono=-1234567.891), mov(6, 6001, portafolio=555, Cargo=1e-8, Abono=0.000000001),
             mov(7, 6001, portafolio=555, Cargo=123456789012345.12345678, Abono=99999999999999999999.99999999, SaldoSimulado=1e18)]), tablas=TP, seed=M)
g.caso("montos-enteros-largos-como-long", LOGICA, entrada_h(555), ["B-CA-06"], base([mov(1, 6004, portafolio=555, Cargo=9007199254740993, Abono=-9007199254740993, SaldoSimulado=0)]), tablas=TP, seed=M)

# ---- matriz y ADC poblados
g.caso("matriz-grande-y-adc-s", LOGICA, entrada_h(555, p_IdMatrizAgrupador=999999999999, p_EsADC="S"), ["B-CA-05"], base([mov(1, 6001, portafolio=555, IdMatrizAgrupador=999999999999, EsADC="S")]), tablas=TP, seed=M)
g.caso("matriz-negativa-y-adc-minuscula", LOGICA, entrada_h(555, p_IdMatrizAgrupador=-3, p_EsADC="s"), ["B-CA-05"], base([mov(1, 6001, portafolio=555, IdMatrizAgrupador=-3, EsADC="s")]), tablas=TP, seed=M)
g.caso("adc-vacio-y-con-espacio", LOGICA, entrada_h(555, p_IdMatrizAgrupador=1, p_EsADC=" N "), ["B-CA-05"], base([mov(1, 6001, portafolio=555, IdMatrizAgrupador=1, EsADC=""), mov(2, 6002, portafolio=555, EsADC=" N ")]), tablas=TP, seed=M)

# ---- errores de la cartola con movimientos y textos con caracteres especiales
g.caso("cartola-contador-tres-con-movimientos-y-mensajes", LOGICA, entrada_h(555), ["D-CAN-04"],
       base([mov(1, 6001, portafolio=555)], contador=3, errores=[{"Codigo": "E1", "Descripcion": "uno"}, {"Codigo": "E2", "Descripcion": "dos"}, {"Codigo": "E3", "Descripcion": "tres"}],
            mensajes=[{"id": 1, "tipo": "ERROR", "codigo": "R1", "descripcion": "x"}]), tablas=TP, seed=M)
g.caso("nombres-con-comillas-y-menor-mayor-en-la-salida-logica", LOGICA, entrada_h(555), ["B-CA-06"],
       base([mov(1, 6001, portafolio=555, NombreCliente="O'Higgins <&> = \"x\"", NombreConcepto="Ñandú ñ=1 & 2 > 1 < 3", NombreEstado="Vig'ente")]), tablas=TP, seed=M)

# ---- fallos de la base con filas grandes y textos especiales (el texto de Parameters del SQLException)
g.caso("falla-insercion-con-nombres-especiales-y-tildes", LOGICA, entrada_h(555), ["D-CAN-05"],
       base([mov(1, 6001, portafolio=555, NombreCliente="O'Higgins <&> = \"x\" ñ", NombreConcepto="Abono áéíóú")]), tablas=TP, seed=M, fallos=[fallo("INSERT INTO", "no se puede insertar")])
g.caso("falla-insercion-con-ids-repetidos-y-nulos", LOGICA, entrada_h(555), ["D-CAN-05", "D-CAN-06"], base([mov(3, 6001, portafolio=555), mov(3, None, portafolio=555), mov(None, 6002, portafolio=555)]), tablas=TP, seed=M,
       fallos=[fallo("INSERT INTO", "no se puede insertar")])
g.caso("falla-select-de-comprobantes-con-muchas-filas", LOGICA, entrada_h(100), ["D-CAN-05"], base([mov(i, 5001, portafolio=100) for i in range(1, 31)]), tablas=TP, seed=M,
       fallos=[fallo("TO_VARCHAR", "no se puede leer el comprobante")])

# ---- entrada JSON (call): tipos y formas que el desarrollo no uso
g.caso("json-portafolio-con-ceros-a-la-izquierda", CALL, entrada_h(), ["D-CAN-10"], base([mov(1, 5001)]), tablas=TP, seed=M)
cj = json.loads((L.CASES / g.ids[-1] / "case.json").read_text(encoding="utf-8"))
cj["entrada"]["p_IdPortafolio"] = "00100"
(L.CASES / g.ids[-1] / "case.json").write_text(json.dumps(cj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
g.caso("json-portafolio-con-espacios-como-texto", CALL, entrada_h(" 100 "), ["D-CAN-10"], base([mov(1, 5001)]), tablas=TP, seed=M)
g.caso("json-portafolio-en-notacion-cientifica", CALL, entrada_h(1e2), ["D-CAN-10"], base([mov(1, 5001)]), tablas=TP, seed=M)
g.caso("json-portafolio-booleano", CALL, entrada_h(True), ["D-CAN-10"], base([mov(1, 5001)]), tablas=TP, seed=M)
g.caso("json-portafolio-555-matriz-y-adc-como-texto", CALL, entrada_h(555, p_IdMatrizAgrupador="42", p_EsADC="S"), ["D-CAN-10", "B-CA-05"], base([mov(1, 6001, portafolio=555, IdMatrizAgrupador=42, EsADC="S")]), tablas=TP, seed=M)
g.caso("json-nombres-con-apostrofe-igual-y-menor-mayor", CALL, entrada_h(555), ["B-CA-06", "B-CA-11"],
       base([mov(1, 6001, portafolio=555, NombreCliente="O'Higgins <&> = ñ", NombreConcepto="a=b & c<d>e", NombreEstado="    \u0007")]), tablas=TP, seed=M)
g.caso("json-fechas-de-la-cartola-con-cambio-de-anio-y-bisiesto", CALL, entrada_h(555), ["B-CA-06", "B-CA-11"],
       base([mov(1, 6001, portafolio=555, FechaSistema="2024-02-29 23:59:59.999", FechaLiquidacionOperacion="2027-01-01 00:00:00"), mov(2, 6002, portafolio=555, FechaSistema="2026-12-31", FechaLiquidacionOperacion="0001-01-01")]),
       tablas=TP, seed=M)
g.caso("json-ciento-veinte-movimientos", CALL, entrada_h(555), ["B-CA-11", "B-CA-03"], base([mov(i + 1, (6001, 6002, 6003, 6004, None)[i % 5], portafolio=555, Abono=i * 1.25) for i in range(120)]), tablas=TP, seed=M)

# ---- procedimientos (SELECT final de los SQLScript, tablas de apoyo)
S, PRC = P.S, P.PRC
MP = M + [P.vista(100, 10, "PF-100"), P.vista(200, 20, "PF-200"), P.vista(555, 55, "PF-555"), P.vista(1, 5, "PF-001"), P.vista(9007199254740993, 77, "PF-BIG")]
g.caso("historial-muchas-filas-con-portafolios-nuevos", S, P.CI, ["D-CAN-13"],
       seed=MP + [P.car(i, (5001, 6001, 6002, 6003, None, 9999)[i % 6], (100, 555, 1, 9007199254740993, 200)[i % 5], Cargo=i * 0.5, Abono=0, IdMatrizAgrupador=i % 4, EsADC="S" if i % 3 == 0 else "N") for i in range(1, 61)], esquemas_extra=PRC)
g.caso("historial-ids-repetidos-y-folio-grande", S, P.CI, ["D-CAN-13", "D-CAN-06"], seed=MP + [P.car(1, 6001, 555), P.car(1, 6004, 555), P.car(1, 6002, 1)], esquemas_extra=PRC)
g.caso("clientes-con-bloqueos-y-saldos-distintos", S, P.CL, ["D-CAN-15"],
       seed=MP + [P.cli_in(55, 555, IdTipoBloqueoPortafolio="BLO", NombreTipoBloqueoPortafolio="Bloqueo total", SaldoFlujoCaja=-0.5, IdMonedaFlujoCaja="USD"), P.cli_in(5, 1, SaldoFlujoCaja=123456789.12345678),
                  P.cli_in(10, 100, NombreCompleto="Ñandú O'Higgins <&>")], esquemas_extra=PRC)

# ---- cada campo de texto del movimiento con un valor DISTINTO (en el desarrollo NombreTipoOperacion y NombreTipoMovimiento valen lo mismo, "Abono": un cruce entre ambos no se veria)
g.caso("todos-los-campos-de-texto-con-valores-distintos", LOGICA, entrada_h(555), ["B-CA-06"],
       base([mov(1, 6001, portafolio=555, IdMoneda="UF", IdConcepto="CNC-X", NombreConcepto="Concepto X", IdEstado="ANU", NombreEstado="Anulado", NombreTipoComprobante="TC-comprobante", NombreTipoProducto="TP-producto",
                 NombreTipoOperacion="TO-operacion", NombreTipoMovimiento="TM-movimiento", NombreCliente="Cliente distinto", FechaSistema="2026-01-05", FechaLiquidacionOperacion="2026-01-09", Cargo=7.25, Abono=3.5, SaldoSimulado=42.125)]),
       tablas=TP, seed=M)

nuevos = 0
for d in sorted(L.CASES.iterdir()):
    if not (INPUTS / d.name).exists():
        shutil.copytree(d, INPUTS / d.name)
        nuevos += 1
shutil.rmtree(L.CASES)
print("casos nuevos copiados a inputs/: %d" % nuevos)
print("fresh inputs g: %d casos en %s" % (len(g.ids), L.CASES))
(AQUI / "fresh-ids-g.txt").write_text("\n".join(g.ids) + "\n", encoding="utf-8", newline="\n")
