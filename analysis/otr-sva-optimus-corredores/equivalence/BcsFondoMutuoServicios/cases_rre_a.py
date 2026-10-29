"""FMM-RRE-01..20: contract, early exits, lock, simulation vs real, national pair (rule cards FM-REI-01..11, 30..33, 36..39)."""
import cases_rre_lib as L
from cases_rre_lib import RESCATE, APORTE, sol

NAC = L.colab(L.precios({1: "FM001A", -1: "FM002A"}), L.custodia((900, "GEN", 1000)), L.paridad([1, -1]))
LOCK_ID = "500_GEN_FM002A_10_2026-03-02"


def escribir():
    c = L.caso
    c("FMM-RRE-01_real-un-par-nacional", "FM-REI-C02: real, 1 par FM001A->FM002A: INSERT RESREINV, 1 fila de destino, AGF, log; bloqueo creado y eliminado",
      [sol(RESCATE)], [sol(APORTE)], colaboradores=NAC)
    c("FMM-RRE-02_simulacion-un-par", "FM-REI-C01/C16/C36: simulacion: sin bloqueo, sin escritura, resultado por grupo", [sol(RESCATE)], [sol(APORTE)],
      colaboradores=NAC, p_SimularOperacion="S")
    c("FMM-RRE-03_sin-rescates", "FM-REI-02: lista de rescates vacia -> NO_SE_INGRESARON_SOLICITUDES_RESCATE, contador 1", [], [sol(APORTE)], colaboradores=NAC)
    c("FMM-RRE-04_sin-aportes-indexoutofbounds", "FM-REI-02/09/C04: aportes vacios no se validan: IndexOutOfBounds al armar el bloqueo (antes de bloquear)",
      [sol(RESCATE)], [], colaboradores=NAC)
    c("FMM-RRE-05_idinterno-cero", "FM-REI-03: IdInterno 0 -> ID_INTERNO_NO_CERO", [sol(RESCATE, IdInterno=0)], [sol(APORTE, IdInterno=0)], colaboradores=NAC)
    c("FMM-RRE-06_idinterno-repetido", "FM-REI-03: IdInterno repetido -> CORRELATIVO_INCORRECTO", [sol(RESCATE), sol(RESCATE)], [sol(APORTE)], colaboradores=NAC)
    c("FMM-RRE-07_idinterno-nulo-npe", "FM-REI-03: IdInterno nulo -> NullPointerException (sin catch)", [sol(RESCATE, IdInterno=None)], [sol(APORTE)],
      colaboradores=NAC)
    c("FMM-RRE-08_aporte-idinterno-repetido", "FM-REI-06/C08: aportes con IdInterno repetido: List2Hash 'key field is not unique' (no se valida antes)",
      [sol(RESCATE)], [sol(APORTE), sol(APORTE, IdSerieFondo="FM001A")], colaboradores=NAC)
    c("FMM-RRE-09_bloqueo-duplicado", "FM-REI-10/C14, D-F2-1: el id ya esta en la tabla de bloqueo -> UI_ERROR_BLOQUEO_SOLICITUD, return sin borrar nada",
      [sol(RESCATE)], [sol(APORTE)], colaboradores=NAC,
      seed=[L.ins(L.LOCK, {"IdBloqueaReinversion": LOCK_ID, "IdPortafolio": 500, "IdRegimenTributario": "GEN", "IdSerieFondo": "FM002A", "Monto": 0,
                           "NumeroCuotas": 10, "FechaSistema": "2026-03-02", "IdCliente": 0, "IdGrupoRegimen": ""})])
    c("FMM-RRE-10_bloqueo-id-mas-de-70", "FM-REI-10/C15, D-F2-10: id de 71..100 caracteres: la tabla local lo acepta y el procedimiento falla -> "
      "UI_ERROR_BLOQUEO_REINVERSION, tabla local sin DROP", [sol(RESCATE, IdRegimenTributario="R" * 40, NumeroCuotas=10.12345678)], [sol(APORTE)],
      colaboradores=NAC)
    c("FMM-RRE-11_bloqueo-id-mas-de-100", "FM-REI-10, D-F2-10: id > 100: falla el INSERT de la tabla local -> UI_ERROR_BLOQUEO_REINVERSION",
      [sol(RESCATE, IdRegimenTributario="R" * 40, NumeroCuotas=1234567890.12345678)], [sol(APORTE, IdSerieFondo="FM002A" + "X" * 30)], colaboradores=NAC)
    c("FMM-RRE-12_simulacion-nula-es-real", "FM-REI-30/09: p_SimularOperacion nulo = real (Simula N, bloqueo y escritura)", [sol(RESCATE)], [sol(APORTE)],
      colaboradores=NAC, p_SimularOperacion=None)
    c("FMM-RRE-13_simulacion-minuscula", "FM-REI-30: 's' minuscula simula (equalsIgnoreCase) y el bloqueo no se crea ('S'.equalsIgnoreCase)",
      [sol(RESCATE)], [sol(APORTE)], colaboradores=NAC, p_SimularOperacion="s")
    c("FMM-RRE-14_aportes-desordenados-id-por-posicion", "FM-REI-09/C17, D-F2-10: dos pares en distinto orden: la clave del bloqueo toma el aporte de la MISMA "
      "posicion, no el del mismo IdInterno", [sol(RESCATE), sol(RESCATE, IdInterno=2, NumeroCuotas=5, IdAgrupador=2)],
      [sol(APORTE, IdInterno=2, IdAgrupador=2, IdSerieFondo="FM001A"), sol(APORTE)],
      colaboradores=L.colab(L.precios({1: "FM001A", 2: "FM001A", -1: "FM002A", -2: "FM001A"}), L.custodia((900, "GEN", 1000)),
                            L.paridad([1, 2, -1, -2])))
    c("FMM-RRE-15_menos-aportes-que-rescates", "FM-REI-09/C18: 2 rescates y 1 aporte -> IndexOutOfBounds (sin bloqueo)",
      [sol(RESCATE), sol(RESCATE, IdInterno=2)], [sol(APORTE)], colaboradores=NAC)
    c("FMM-RRE-16_canal-nulo-age", "FM-REI-18/C45: canal nulo -> AGE en la fila; canal distinto de WEB: mensaje 1N", [sol(RESCATE)], [sol(APORTE)],
      colaboradores=NAC, p_IdTipoCanalIngreso=None)
    c("FMM-RRE-17_canal-web", "FM-REI-17/35/36/C39: canal WEB: sin batch ni mensaje 1N", [sol(RESCATE)], [sol(APORTE)], colaboradores=NAC,
      p_IdTipoCanalIngreso="WEB")
    c("FMM-RRE-18_canal-eje", "FM-REI-35/36/C39, D-F2-9: canal EJE: sin batch (WEB/EJE) pero CON mensaje 1N (solo WEB lo omite)", [sol(RESCATE)], [sol(APORTE)],
      colaboradores=NAC, p_IdTipoCanalIngreso="EJE")
    c("FMM-RRE-19_falta-parametro-spot", "FM-REI-04/C06: falta RESCATE_GENERA_VENTA_SPOT_DOLAR -> excepcion del parametro (antes del bloqueo)",
      [sol(RESCATE)], [sol(APORTE)], colaboradores=NAC,
      seed=["DELETE FROM %s WHERE \"NombreTipoParametro\" = 'RESCATE_GENERA_VENTA_SPOT_DOLAR';" % L.PARAM])
    c("FMM-RRE-20_perfil-ausente", "FM-REI-05/C07: usuario sin perfil y rescates no batch -> PERFIL_INCORRECTO", [sol(RESCATE)], [sol(APORTE)],
      colaboradores=NAC, p_IdUsuario=8, seed=[L.ins('"BSCL.Plataforma.BD::ACC.Usuario"', {"IdUsuario": 8, "IdEmpresa": 1, "IdEstado": "ACT",
                                                                                            "TipoEntidad": "EMP", "IdEntidad": 80})])
    c("FMM-RRE-21_excepcion-tras-el-bloqueo-lo-deja", "FM-REI-37/C41, D-F2-1: regimen sin fila en RegimenFondoMutuo (GrupoRegimen nulo): NPE en el switch "
      "de tributacion DESPUES de crear el bloqueo: el bloqueo autonomo y la tabla local quedan", [sol(RESCATE, IdRegimenTributario="R" * 40)], [sol(APORTE)],
      colaboradores=NAC, seed=[L.clonar(L.REGSERIE, "IdSerieFondo", "FM001A", IdRegimenFondoMutuo="R" * 40)])
    return 21
