"""ValidaPatrimonioEmisor cases (FM-REQ-23, D-REQ-7)."""
from cases3b_lib import caso, dicc, sql
from cases3b_seeds import fondo, serie, cartera, valor_cuota

S = "ValidaPatrimonioEmisor"
PASO = "BSCL.Orden.BD::GBL.ValidaPatrimonioEmisor_Proceso_Paso"
TABLAS = [(PASO, '"IdCliente"')]
DIC = [dicc("UI_PATRIMONIO_MINIMO_EMISOR_INSUFICIENTE", "Patrimonio minimo insuficiente en ::IdInstrumento::")]
# universe: FM001A (instrumento 900, fondo 10, emisor AGF1, CLP) ya existe; FM002A (901) del mismo emisor exige 1000; FM003A (902) fondo USD emisor AGF2
BASE = DIC + [serie(IdSerieFondo="FM002A", IdInstrumento=901, MontoPatrimonioEmisor=1000, PatrimonioAltos="S"),
              fondo(IdFondo="20", IdEmisor="AGF2", IdMoneda="USD", Nemotecnico="FM003"),
              serie(IdSerieFondo="FM003A", IdInstrumento=902, IdFondo="20", MontoPatrimonioEmisor=500, PatrimonioAltos="S")]


def sol(i, cliente, serie_id, mp=0, vc=None):
    d = {"IdInterno": i, "IdCliente": cliente, "IdSerieFondo": serie_id, "IdEmisor": "AGF1", "MontoPatrimonio": mp}
    if vc is not None:
        d["ValorCuota"] = vc
    return d


def ent(*sols, fecha="2026-03-02"):
    return {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_Fecha": fecha, "p_Solicitudes": list(sols)}


def cases():
    yield caso("REQ-23-01_sin-custodia-bajo-minimo", S, "FM-REQ-23: sin custodia, monto del llamador 500 < 1000",
               ent(sol(1, 100, "FM002A", 500)), BASE, tablas=TABLAS)
    yield caso("REQ-23-02_misma-moneda-suma", S, "FM-REQ-23: custodia 800 (misma moneda, sin valor cuota) + 300 >= 1000: sin error",
               ent(sol(1, 100, "FM002A", 300)), BASE + [cartera(100, "AGF1", "CLP", 10, 800, nemo="OTRO", instr=901)], tablas=TABLAS)
    yield caso("REQ-23-03_otra-moneda-paridad", S, "FM-REQ-23: custodia en USD convertida por CalcularParidad (doble: 15001 para el calculo 1)",
               ent(sol(1, 100, "FM002A", 0)), BASE + [cartera(100, "AGF1", "USD", 10, 50, nemo="OTRO", instr=901)], tablas=TABLAS)
    yield caso("REQ-23-04_serie-inexistente", S, "FM-REQ-23: serie que no existe -> error sin parametros",
               ent(sol(1, 100, "NOEXISTE", 5000)), BASE, tablas=TABLAS)
    yield caso("REQ-23-05_clientes-y-series-repetidos", S, "FM-REQ-23: dos solicitudes del mismo cliente y serie: una fila en la tabla de paso, cada una suma la custodia",
               ent(sol(1, 100, "FM002A", 100), sol(2, 100, "FM002A", 950), sol(3, 101, "FM002A", 1000)),
               BASE + [cartera(100, "AGF1", "CLP", 1, 60, nemo="OTRO", instr=901)], tablas=TABLAS)
    yield caso("REQ-23-06_valoriza-con-valor-cuota", S, "D-REQ-7: custodia valorizada con FMM.ValorCuota (sin fecha) si existe: 2 x 1500.1234",
               ent(sol(1, 100, "FM002A", 0)), BASE + [cartera(100, "AGF1", "CLP", 2, 1, nemo="FM001A", instr=900)], tablas=TABLAS)
    yield caso("REQ-23-07_filtra-fecha-y-estado", S, "FM-REQ-23: FechaLiquidacion > fecha y estado distinto de VIG no cuentan",
               ent(sol(1, 100, "FM002A", 0)),
               BASE + [cartera(100, "AGF1", "CLP", 1, 5000, nemo="OTRO", instr=901, fecha="2026-03-03"),
                       cartera(100, "AGF1", "CLP", 1, 5000, nemo="OTRO2", instr=901, estado="ANU")], tablas=TABLAS)
    yield caso("REQ-23-08_sin-solicitudes-llama-paridad", S, "FM-REQ-23: lista vacia: sin mensajes pero CalcularParidad se invoca una vez",
               ent(), BASE, tablas=TABLAS)
    yield caso("REQ-23-09_emisor-distinto-no-cuenta", S, "FM-REQ-23: custodia de otro emisor no suma",
               ent(sol(1, 100, "FM002A", 0)), BASE + [cartera(100, "AGF9", "CLP", 1, 5000, nemo="OTRO", instr=901)], tablas=TABLAS)
    yield caso("REQ-23-10_paridad-falla", S, "FM-REQ-23: el colaborador CalcularParidad falla: la excepcion sale",
               ent(sol(1, 100, "FM002A", 0)), BASE,
               colaboradores={"CalcularParidad": {"respuestas": [{"error": "paridad no disponible"}]}}, tablas=TABLAS)
    yield caso("REQ-23-11_fila-vieja-en-tabla-de-paso", S, "FM-REQ-23: la tabla de paso se vacia al inicio (fila vieja de otro cliente) y queda con los clientes de la llamada",
               ent(sol(1, 100, "FM002A", 1000)),
               BASE + [sql(PASO, IdCliente=999), cartera(999, "AGF1", "CLP", 1, 7000, nemo="OTRO", instr=901)], tablas=TABLAS)
    yield caso("REQ-23-12_dos-calculos-de-paridad", S, "FM-REQ-23: dos custodias en USD de dos solicitudes: IdInterno de calculo 1 y 2 devueltos por el doble",
               ent(sol(1, 100, "FM002A", 0), sol(2, 101, "FM002A", 0)),
               BASE + [cartera(100, "AGF1", "USD", 1, 10, nemo="OTRO", instr=901), cartera(101, "AGF1", "USD", 1, 20, nemo="OTRO", instr=901)],
               colaboradores={"CalcularParidad": {"defecto": {"p_out_CalculosSalida": [
                   {"IdTrabajo": 1, "IdInterno": 1, "MontoDestino": 400}, {"IdTrabajo": 1, "IdInterno": 2, "MontoDestino": 1200.5}]}}}, tablas=TABLAS)
    yield caso("REQ-23-13_serie-en-usd-custodia-clp", S, "FM-REQ-23: serie de moneda USD (emisor AGF2), custodia CLP del mismo cliente y emisor se convierte",
               {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_Fecha": "2026-03-02",
                "p_Solicitudes": [{"IdInterno": 1, "IdCliente": 100, "IdSerieFondo": "FM003A", "IdEmisor": "AGF2", "MontoPatrimonio": 10}]},
               BASE + [cartera(100, "AGF2", "CLP", 1, 9000, nemo="OTRO", instr=902)],
               colaboradores={"CalcularParidad": {"defecto": {"p_out_CalculosSalida": [{"IdTrabajo": 1, "IdInterno": 1, "MontoDestino": 9.5}]}}},
               tablas=TABLAS)
    yield caso("REQ-23-14_cliente-nulo", S, "FM-REQ-23: IdCliente ausente: la tabla de paso recibe NULL y la solicitud no encuentra custodia",
               {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_Fecha": "2026-03-02", "p_Solicitudes": [{"IdInterno": 1, "IdSerieFondo": "FM002A", "MontoPatrimonio": 5}]},
               BASE, tablas=TABLAS)
