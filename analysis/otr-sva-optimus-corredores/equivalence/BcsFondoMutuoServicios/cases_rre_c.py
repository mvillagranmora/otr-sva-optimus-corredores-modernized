"""FMM-RRE-37..50 (amounts, equity, custody, batch program, exceptions) and FMM-RRE-M01..M05 (through the legacy Manejador)."""
import cases_rre_lib as L
from cases_rre_lib import RESCATE, APORTE, sol
from cases_rre_b import par

APV = [L.ins('"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"', {"IdPortafolio": 500, "IdMatrizAgrupadorAPV": 1}),
       L.ins('"BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario"', {"IdMatrizAgrupadorAPV": 1, "IdRegimenTributario": 10}),
       L.ins('"BSCL.APV.BD::REC.RegimenTributario"', {"IdRegimenTributario": 10, "Codigo": "APV-A"})]
DOS = L.colab(L.precios({1: "FM001A", 2: "FM001A", -1: "FM002A", -2: "FM002A"}), L.custodia((900, "GEN", 1000)), L.paridad([1, 2, -1, -2]))
DOS_R = [sol(RESCATE, IdAgrupador=1), sol(RESCATE, IdInterno=2, NumeroCuotas=5, IdAgrupador=2)]
DOS_A = [sol(APORTE, IdAgrupador=1), sol(APORTE, IdInterno=2, IdAgrupador=2)]
MIN = [L.upd(L.SERIE, {"MontoMinimo": 20000}, "\"IdSerieFondo\" = 'FM002A'")]   # each contribution < 20000, the two together >= 20000


def man(cid, ref, rescates, aportes, colaboradores, seed=None, **cab):
    entrada = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": "N", "p_UsaReglasNegocio": "S", "p_IdTipoCanalIngreso": "AGE",
               "p_SolicitudRescateReinversionRescate": rescates, "p_SolicitudRescateReinversionAporte": aportes}
    entrada.update(cab)
    L.caso(cid, ref, None, None, seed=seed, colaboradores=colaboradores, servicio="ManejadorSolicitudAporteRescate", entrada_extra=entrada)


def escribir():
    c = L.caso
    c("FMM-RRE-37_montos-agrupados-canal-web", "FM-REI-17/21/C30, D-F2-15: canal WEB: la clave de lectura y de escritura coinciden (0): el minimo se mide sobre "
      "la SUMA de los aportes a la serie", DOS_R, DOS_A, colaboradores=DOS, seed=MIN, p_IdTipoCanalIngreso="WEB")
    c("FMM-RRE-38_montos-agrupados-canal-age", "FM-REI-17/21/C30, D-F2-15: canal AGE: la clave de lectura usa el IdAgrupador NUEVO: no encuentra la suma y "
      "mide cada aporte solo (rechazo por minimo)", DOS_R, DOS_A, colaboradores=DOS, seed=MIN)
    c("FMM-RRE-39_patrimonio-altos-distinto-emisor", "FM-REI-22/26/C31: destino con PatrimonioAltos S y emisor distinto: candidato a ValidaPatrimonioEmisor",
      [sol(RESCATE, MontoPatrimonio=1000)], [sol(APORTE)], colaboradores=par("FM001A", "FM002A"),
      seed=[L.upd(L.SERIE, {"PatrimonioAltos": "S"}, "\"IdSerieFondo\" = 'FM002A'")])
    c("FMM-RRE-40_patrimonio-altos-mismo-emisor", "FM-REI-22/C31: mismo emisor: MontoPatrimonio 0", [sol(RESCATE)],
      [sol(APORTE, IdSerieFondo="FM001A", IdEmisor="AGF1")], colaboradores=par("FM001A", "FM001A"),
      seed=[L.upd(L.SERIE, {"PatrimonioAltos": "S"}, "\"IdSerieFondo\" = 'FM001A'")])
    c("FMM-RRE-41_custodia-insuficiente-break", "FM-REI-29/C34, D-F2-8: sin custodia en la primera: break, la segunda no se valida y se inserta",
      [sol(RESCATE, IdAgrupador=1, NumeroCuotas=5000), sol(RESCATE, IdInterno=2, NumeroCuotas=5, IdAgrupador=2)], DOS_A, colaboradores=DOS)
    c("FMM-RRE-42_custodia-insuficiente-car-continue", "FM-REI-29/C34: origen CAR: continue, se validan todas",
      [sol(RESCATE, IdAgrupador=1, NumeroCuotas=5000), sol(RESCATE, IdInterno=2, NumeroCuotas=5000, IdAgrupador=2)], DOS_A, colaboradores=DOS,
      p_IdOrigenProceso="CAR")
    c("FMM-RRE-43_patrimonio-minimo-de-otro-par", "FM-REI-29/C35, D-F2-8: el patrimonio minimo se compara con la serie destino de CUALQUIER rescate de la entrada",
      DOS_R, [sol(APORTE, IdAgrupador=1), sol(APORTE, IdInterno=2, IdAgrupador=2, IdSerieFondo="FMX02U", IdEmisor="EXT1")],
      colaboradores=L.colab(L.precios({1: "FM001A", 2: "FM001A", -1: "FM002A", -2: "FMX02U"}), L.custodia((900, "GEN", 1000)), L.paridad([1, 2, -1, -2])),
      seed=[L.upd(L.SERIE, {"PatrimonioMinimo": 999999999}, "\"IdSerieFondo\" = 'FMX02U'")])
    c("FMM-RRE-44_solicitudbatch-nulo-npe", "FM-REI-12/C43, D-F2-1: SolicitudBatch nulo: toMap NPE despues del bloqueo (el bloqueo queda)",
      [sol(RESCATE, SolicitudBatch=None)], [sol(APORTE)], colaboradores=par("FM001A", "FM002A"))
    c("FMM-RRE-45_folio-contrato-informado", "FM-REI-16/C44: FolioContrato > 0 se respeta (la secuencia se consume igual)", [sol(RESCATE, FolioContrato=777)],
      [sol(APORTE)], colaboradores=par("FM001A", "FM002A"))
    c("FMM-RRE-46_portafolio-apv-programa-batch", "FM-REI-35/C38/C40, D-F2-9: portafolio APV, canal AGE: InscribirProcesoBatch INGRESAR_BONIFICACION_FISCAL_REINVERSION "
      "y el primer resultado duplicado", [sol(RESCATE)], [sol(APORTE)], colaboradores=par("FM001A", "FM002A"), seed=APV)
    c("FMM-RRE-47_apv-dos-folios-incluye-rechazadas", "FM-REI-35/C38/C46, D-F2-9: APV con 2 grupos y folios distintos (una rechazada por custodia): un "
      "parametro por folio con ids de TODAS las de solicitudesProcesar (orden HashMap/HashSet)",
      [sol(RESCATE, IdAgrupador=1, FolioContrato=11), sol(RESCATE, IdInterno=2, NumeroCuotas=5000, IdAgrupador=2, FolioContrato=12)], DOS_A,
      colaboradores=DOS, seed=APV, p_IdOrigenProceso="CAR")
    c("FMM-RRE-48_error-valor-cuota-del-rescate", "FM-REI-14/C19, D-F2-7: sin valor cuota de la serie de rescate: ERROR_VALOR_CUOTA con la serie del grupo, "
      "bloqueo eliminado", [sol(RESCATE)], [sol(APORTE)],
      colaboradores=L.colab(L.precios({-1: "FM002A"}), L.custodia((900, "GEN", 1000)), L.paridad([1, -1])))
    c("FMM-RRE-49_error-segundo-calculo", "FM-REI-15/C20, D-F2-7: falla el calculo del aporte (sin precio de la serie destino): se revisa solo el PRIMER "
      "resultado y se informan todos", [sol(RESCATE), sol(RESCATE, IdInterno=2, NumeroCuotas=5)], [sol(APORTE), sol(APORTE, IdInterno=2)],
      colaboradores=L.colab(L.precios({1: "FM001A", 2: "FM001A"}), L.custodia((900, "GEN", 1000)), L.paridad([1, 2, -1, -2])))
    c("FMM-RRE-50_agregarlog-falla-tras-insertar", "FM-REI-37/38/C42, D-F2-1: excepcion de AgregarLog despues de los INSERT: rollback, el bloqueo autonomo QUEDA",
      [sol(RESCATE)], [sol(APORTE)], colaboradores=par("FM001A", "FM002A", extra={"AgregarLog": {"respuestas": [{"error": "log caido"}]}}))
    sin_fondo = [L.clonar(L.SERIE, "IdSerieFondo", "FM002A", IdSerieFondo="FM003A", IdInstrumento=903, IdFondo="99"),
                 L.clonar(L.REGSERIE, "IdSerieFondo", "FM002A", IdSerieFondo="FM003A")]
    c("FMM-RRE-51_segundo-calculo-sin-valor-cuota", "FM-REI-12/37, D-F2-1: la serie destino no tiene fondo: CalcularFecha del aporte revienta (NPE de IdPais) "
      "DESPUES del bloqueo: el bloqueo queda (la rama hasVC=false de FM-REI-15 no se alcanza asi)", [sol(RESCATE)], [sol(APORTE, IdSerieFondo="FM003A")],
      colaboradores=par("FM001A", "FM002A"), seed=sin_fondo)
    c("FMM-RRE-52_segundo-calculo-primero-ok-segundo-falla", "FM-REI-12/37, D-F2-1: dos pares, el segundo destino sin fondo: la misma NPE tras el bloqueo "
      "de los dos (ambas filas de bloqueo quedan)",
      DOS_R, [sol(APORTE, IdAgrupador=1), sol(APORTE, IdInterno=2, IdAgrupador=2, IdSerieFondo="FM003A")], colaboradores=DOS, seed=sin_fondo)
    nac = par("FM001A", "FM002A")
    man("FMM-RRE-M01_manejador-par-simple", "FM-REI-34/FM-MAN-07: par por el Manejador: simulacion + pasada real", [sol(RESCATE)], [sol(APORTE)], nac)
    man("FMM-RRE-M02_manejador-uno-a-n-pierde-destinos", "FM-REI-34/C25, D-F2-16: 1:N por el Manejador: la simulacion devuelve un resultado por grupo y la pasada "
      "real solo lleva el par 1", [sol(RESCATE, IdInterno=i, IdAgrupador=7) for i in (1, 2)],
      [sol(APORTE, IdInterno=1, IdAgrupador=7, Porcentaje=60), sol(APORTE, IdInterno=2, IdAgrupador=7, Porcentaje=40)], DOS)
    man("FMM-RRE-M03_manejador-solo-aportes", "FM-MAN-07/D-MAN-7: solo la lista de aportes: la simulacion falla sin rescates y no hay pasada real", [],
        [sol(APORTE)], nac)
    man("FMM-RRE-M04_manejador-simulacion-rechaza", "FM-MAN-06/D-MAN-1: la simulacion rechaza todo: sin pasada real, contador 0", [sol(RESCATE)], [sol(APORTE)],
        nac, seed=[L.upd(L.SERIE, {"CambioFondo": "N"}, "\"IdSerieFondo\" = 'FM001A'")])
    man("FMM-RRE-M05_manejador-bloqueo-duplicado-real", "FM-REI-10/D-MAN-5: el bloqueo ya existe: la simulacion pasa y la pasada real responde duplicado "
        "(su mensaje se pierde, solo su contador)", [sol(RESCATE)], [sol(APORTE)], nac,
        seed=[L.ins(L.LOCK, {"IdBloqueaReinversion": "500_GEN_FM002A_10_2026-03-02", "IdPortafolio": 500, "IdRegimenTributario": "GEN",
                             "IdSerieFondo": "FM002A", "Monto": 0, "NumeroCuotas": 10, "FechaSistema": "2026-03-02", "IdCliente": 0, "IdGrupoRegimen": ""})])
    return 21
