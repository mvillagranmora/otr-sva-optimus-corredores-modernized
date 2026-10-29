"""ValidaMontoInversion cases (FM-REQ-22, D-REQ-6)."""
from cases3b_lib import caso, set_param, del_param, dicc

S = "ValidaMontoInversion"
DIC = [dicc("UI_ERROR_ALTOS_APORTE_FFMM", "Aportes altos ::Monto:: fuera de ::Minimo:: a ::Maximo::"),
       dicc("UI_ERROR_MONTO_TOTAL_INVERSION_FFMM", "Total ::Monto:: fuera de ::Minimo:: a ::Maximo::")]


def sol(i, folio, monto, altos="N", vmm=None):
    d = {"IdInterno": i, "FolioContrato": folio, "AportesAltos": altos, "Monto": monto}
    if vmm is not None:
        d["ValidaMontoMinimo"] = vmm
    return d


def ent(*sols):
    return {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_Solicitudes": list(sols)}


def cases():
    lim = [set_param("MontoMinimoTotalInversion", "1000"), set_param("MontoMaximoTotalInversion", "50000"),
           set_param("MontoMinimoAportesAltos", "20000"), set_param("MontoMaximoAportesAltos", "90000")]
    yield caso("REQ-22-01_dentro-de-limites", S, "FM-REQ-22: total y altos dentro de limites: sin mensajes",
               ent(sol(1, 0, 5000), sol(2, 0, 30000, "S")), DIC + lim)
    yield caso("REQ-22-02_bajo-el-minimo-total", S, "FM-REQ-22: monto < minimo total con ValidaMontoMinimo por defecto S",
               ent(sol(1, 0, 500)), DIC + lim)
    yield caso("REQ-22-03_minimo-no-aplica-si-N", S, "FM-REQ-22: ValidaMontoMinimo=N omite el minimo (el maximo sigue)",
               ent(sol(1, 0, 500, "N", "N"), sol(2, 0, 60000, "N", "N")), DIC + lim)
    yield caso("REQ-22-04_agrupa-por-folio", S, "FM-REQ-22: dos solicitudes del mismo folio suman; error por cada solicitud del grupo",
               ent(sol(1, 77, 30000), sol(2, 77, 30000), sol(3, 0, 100)), DIC + lim)
    yield caso("REQ-22-05_altos-bajo-minimo", S, "FM-REQ-22: AportesAltos=S bajo el minimo de altos y total ok",
               ent(sol(1, 0, 15000, "S")), DIC + lim)
    yield caso("REQ-22-06_altos-sobre-maximo", S, "FM-REQ-22: AportesAltos=S sobre el maximo de altos (ademas del total)",
               ent(sol(1, 0, 95000, "S")), DIC + lim)
    yield caso("REQ-22-07_maximo-cero-rechaza-todo", S, "D-REQ-6: maximo total 0 rechaza todo aporte positivo",
               ent(sol(1, 0, 10)), DIC + [set_param("MontoMaximoTotalInversion", "0")])
    yield caso("REQ-22-08_parametros-ausentes-son-cero", S, "FM-REQ-22: parametros faltantes = 0 (monto positivo > maximo 0)",
               ent(sol(1, 0, 10)), DIC + [del_param("MontoMinimoTotalInversion"), del_param("MontoMaximoTotalInversion"),
                                          del_param("MontoMinimoAportesAltos"), del_param("MontoMaximoAportesAltos")])
    yield caso("REQ-22-09_lista-vacia", S, "FM-REQ-22: sin solicitudes", ent(), DIC + lim)
    yield caso("REQ-22-10_folio-nulo-NPE", S, "FM-REQ-22: FolioContrato ausente => NullPointerException (legacy)",
               {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_Solicitudes": [{"IdInterno": 1, "Monto": 5}]}, DIC + lim)
    yield caso("REQ-22-11_orden-de-grupos-por-folio", S, "FM-REQ-22: varios folios: orden de los mensajes = orden del HashMap",
               ent(sol(1, 900, 100), sol(2, 5, 200), sol(3, 40, 300), sol(4, 0, 400), sol(5, 123456789012, 500)), DIC + lim)
    yield caso("REQ-22-12_monto-limite-exacto", S, "FM-REQ-22: monto igual al minimo y al maximo no es error",
               ent(sol(1, 0, 1000), sol(2, 0, 50000)), DIC + lim)
    yield caso("REQ-22-13_altos-sin-minimo-por-N", S, "FM-REQ-22: AportesAltos=S con ValidaMontoMinimo=N bajo el minimo altos",
               ent(sol(1, 0, 15000, "S", "N")), DIC + lim)
