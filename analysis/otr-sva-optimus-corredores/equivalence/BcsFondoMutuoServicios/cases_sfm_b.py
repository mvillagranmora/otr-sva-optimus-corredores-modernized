"""FMM-SFM cases, part B (ConsultarSolicitudesFFMM + WS twin): global state FM-SFM-04, committed / available amounts FM-SFM-05, state filter and order FM-SFM-06,
WS Tesoreria join FM-SFM-07/08 and the determinism sets. Ids: RULES_FONDOMUTUO_F3_A.md 8.3, D-F3A-08/10 (section 9), matrix SFM-C01..C05, WS-C01..C03 (section 11).
Inputs only: expected.json is recorded from the legacy by record_sfm.sh."""
from cases_sfm_lib import *  # noqa: F401,F403


def generar():
    estado_global()
    montos()
    filtro_estado()
    reinversion()
    tesoreria_ws()
    determinismo()


def g(ns, estado, folio=555, **ov):
    """a row of the group of FolioContrato `folio` in the given EstadoDetalle"""
    return fila(ns, EstadoDetalle=estado, FolioContrato=folio, **ov)


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def estado_global():
    # ---- FM-SFM-04: the last condition that holds wins: ANULADO (all) < INGRESADO (all) < TERMINADO (all) < EN PROCESO (any)
    par("estado-tres-ingresadas", "FM-SFM-04 SFM-C01: 3 filas del mismo FolioContrato 555, las tres INGRESADO => Estado INGRESADO en las tres",
        entrada(), [g(1, "INGRESADO"), g(2, "INGRESADO"), g(3, "INGRESADO")])
    par("estado-en-proceso-y-terminado", "FM-SFM-04 SFM-C02: EN PROCESO + TERMINADO => EN PROCESO (la mas fuerte, aunque no sean todas)", entrada(),
        [g(1, "EN PROCESO"), g(2, "TERMINADO")])
    par("estado-terminado-y-anulado-se-descarta", "FM-SFM-04 SFM-C02 D-F3A-08: TERMINADO + ANULADO => Estado '' y las dos filas DESAPARECEN del listado por defecto", entrada(),
        [g(1, "TERMINADO"), g(2, "ANULADO")])
    par("estado-ingresado-y-terminado-se-descarta", "FM-SFM-04 SFM-C02 D-F3A-08: INGRESADO + TERMINADO => Estado '' (se descartan en silencio)", entrada(),
        [g(1, "INGRESADO"), g(2, "TERMINADO")])
    par("estado-anulada-sola", "FM-SFM-04 SFM-C02: una sola fila ANULADO => ANULADO", entrada(), [g(1, "ANULADO")])
    par("estado-dos-anuladas", "FM-SFM-04: dos filas ANULADO => ANULADO en las dos", entrada(), [g(1, "ANULADO"), g(2, "ANULADO")])
    par("estado-terminada-sola", "FM-SFM-04: una sola fila TERMINADO => TERMINADO", entrada(), [g(1, "TERMINADO")])
    par("estado-dos-terminadas", "FM-SFM-04: dos filas TERMINADO => TERMINADO", entrada(), [g(1, "TERMINADO"), g(2, "TERMINADO")])
    par("estado-ingresada-y-en-proceso", "FM-SFM-04: INGRESADO + EN PROCESO => EN PROCESO", entrada(), [g(1, "INGRESADO"), g(2, "EN PROCESO")])
    par("estado-en-proceso-solo", "FM-SFM-04: una sola fila EN PROCESO => EN PROCESO", entrada(), [g(1, "EN PROCESO")])
    par("estado-en-proceso-y-anulada", "FM-SFM-04: EN PROCESO + ANULADO => EN PROCESO", entrada(), [g(1, "EN PROCESO"), g(2, "ANULADO")])
    par("estado-anulada-e-ingresada-se-descarta", "FM-SFM-04 D-F3A-08: ANULADO + INGRESADO => ''", entrada(), [g(1, "ANULADO"), g(2, "INGRESADO")])
    par("estado-desconocido", "FM-SFM-04: EstadoDetalle 'PENDIENTE' (no es ninguno de los cuatro) => Estado '' (se descarta)", entrada(), [g(1, "PENDIENTE")])
    par("estado-detalle-nulo", "FM-SFM-04: EstadoDetalle nulo => Estado '' (no cuenta en ningun estado, el total es 1)", entrada(), [g(1, None)])
    par("estado-minusculas", "FM-SFM-04: 'ingresado' en minusculas no es INGRESADO (equals) => ''", entrada(), [g(1, "ingresado")])
    par("estado-ingresada-y-desconocida", "FM-SFM-04: INGRESADO + 'X' => '' (ninguna condicion de 'todas')", entrada(), [g(1, "INGRESADO"), g(2, "X")])
    par("estado-en-proceso-y-desconocida", "FM-SFM-04: EN PROCESO + 'X' => EN PROCESO", entrada(), [g(1, "EN PROCESO"), g(2, "X")])
    par("estado-con-espacio", "FM-SFM-04: 'INGRESADO ' con espacio final no es INGRESADO => ''", entrada(), [g(1, "INGRESADO ")])
    # ---- association keys: FolioContrato != 0 and equal, OR NumSolicitud equal
    par("asocia-por-num-solicitud-sin-folio", "FM-SFM-04: FolioContrato 0 y la misma NumSolicitud 7 en tres filas (EN PROCESO/TERMINADO/ANULADO) => se asocian por NumSolicitud: EN PROCESO",
        entrada(), [fila(7, EstadoDetalle="EN PROCESO"), fila(7, EstadoDetalle="TERMINADO"), fila(7, EstadoDetalle="ANULADO")])
    par("folio-cero-no-asocia", "FM-SFM-04: FolioContrato 0 en dos solicitudes distintas NO las asocia (cada una su Estado)", entrada(),
        [fila(1, EstadoDetalle="TERMINADO"), fila(2, EstadoDetalle="ANULADO")])
    par("asocia-por-folio-contrato-distinta-solicitud", "FM-SFM-04: dos solicitudes distintas (1 y 2) con FolioContrato 555: se asocian por el folio (INGRESADO + EN PROCESO => EN PROCESO)",
        entrada(), [g(1, "INGRESADO"), g(2, "EN PROCESO")])
    par("dos-grupos-independientes", "FM-SFM-04: folios 555 y 556 son grupos distintos: 555 TERMINADO+ANULADO (se descarta) y 556 TERMINADO (sale)", entrada(),
        [g(1, "TERMINADO", 555), g(2, "ANULADO", 555), g(3, "TERMINADO", 556)])
    par("encadenamiento-no-transitivo", "FM-SFM-04: la asociacion NO es transitiva: A(folio 1, solicitud 1), B(folio 1, solicitud 2), C(folio 9, solicitud 2): A ve {A,B}, B ve {A,B,C}, C ve {B,C}",
        entrada(), [fila(1, EstadoDetalle="TERMINADO", FolioContrato=1), fila(2, EstadoDetalle="ANULADO", FolioContrato=1), fila(2, EstadoDetalle="EN PROCESO", FolioContrato=9, Folio="F9")])
    par("folio-contrato-nulo", "FM-SFM-04 SFM-C08 D-NOR-9: FolioContrato nulo en una fila no REV => NullPointerException (Long.equals)", entrada(), [fila(1, FolioContrato=None)])
    par("num-solicitud-nulo", "FM-SFM-04: NumSolicitud nula => NullPointerException", entrada(), [fila(None, EstadoDetalle="INGRESADO")])
    par("fila-unica-folio-contrato-nulo-no-rev", "FM-SFM-04: una fila REV con FolioContrato nulo NO falla (la rama REV no lo lee)", entrada(),
        [fila(1, TipoSolicitud="REV", FolioContrato=None, FolioRelacionado=0)])


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def montos():
    # NOTE: the example of the card FM-SFM-05 (INGRESADO 10,5 + TERMINADO 4 + ANULADO 9) has Estado '' (D-F3A-08) and is NOT visible in the output: the amounts are only
    # observable in groups whose state is not ''. The variant with EN PROCESO (any => EN PROCESO) is the one recorded.
    par("montos-en-proceso-terminado-anulado", "FM-SFM-05 SFM-C03: grupo EN PROCESO (cuotas 10,5 / pesos 1050) + TERMINADO (4 / 400) + ANULADO (9 / 900) => Estado EN PROCESO; "
        "comprometidas 10,5 y 1050; disponibles 4 y 400; la ANULADO no suma. (El ejemplo de la tarjeta con INGRESADO tiene Estado '' y no sale)", entrada(),
        [g(1, "EN PROCESO", MontoCuotasOrigen="10.5", MontoPesosOrigen="1050"), g(2, "TERMINADO", MontoCuotasOrigen="4", MontoPesosOrigen="400"),
         g(3, "ANULADO", MontoCuotasOrigen="9", MontoPesosOrigen="900")])
    par("montos-ingresadas-suman-comprometidas", "FM-SFM-05: 3 INGRESADO (cuotas 1,5 / 2,25 / 3,125; pesos 100 / 200 / 300): comprometidas 6,875 y 600; disponibles 0",
        entrada(), [g(1, "INGRESADO", MontoCuotasOrigen="1.5", MontoPesosOrigen="100"), g(2, "INGRESADO", MontoCuotasOrigen="2.25", MontoPesosOrigen="200"),
                    g(3, "INGRESADO", MontoCuotasOrigen="3.125", MontoPesosOrigen="300")])
    par("montos-terminadas-suman-disponibles", "FM-SFM-05: 2 TERMINADO: disponibles = suma; comprometidas 0", entrada(),
        [g(1, "TERMINADO", MontoCuotasOrigen="7.5", MontoPesosOrigen="750"), g(2, "TERMINADO", MontoCuotasOrigen="2.5", MontoPesosOrigen="250")])
    par("montos-anuladas-no-suman", "FM-SFM-05: 2 ANULADO: ninguna suma (comprometidas y disponibles 0, escala 0)", entrada(),
        [g(1, "ANULADO", MontoCuotasOrigen="7.5", MontoPesosOrigen="750"), g(2, "ANULADO", MontoCuotasOrigen="2.5", MontoPesosOrigen="250")])
    par("montos-estado-desconocido-y-nulo-comprometen", "FM-SFM-05: un EstadoDetalle desconocido ('X') y uno nulo SUMAN a comprometidas (no son TERMINADO ni ANULADO)", entrada(),
        [g(1, "EN PROCESO", MontoCuotasOrigen="1", MontoPesosOrigen="10"), g(2, "X", MontoCuotasOrigen="2", MontoPesosOrigen="20"),
         g(3, None, MontoCuotasOrigen="4", MontoPesosOrigen="40")])
    par("montos-cuotas-nulas", "FM-SFM-05 D-NOR-9: MontoCuotasOrigen nulo en una fila comprometida => NullPointerException (BigDecimal.add(null))", entrada(),
        [g(1, "INGRESADO", MontoCuotasOrigen=None)])
    par("montos-pesos-nulos", "FM-SFM-05: MontoPesosOrigen nulo en una fila comprometida => NullPointerException", entrada(), [g(1, "INGRESADO", MontoPesosOrigen=None)])
    par("montos-nulos-en-anulada-no-fallan", "FM-SFM-05: montos nulos en una fila ANULADO no se leen (ni suma ni NPE)", entrada(),
        [g(1, "ANULADO", MontoCuotasOrigen=None, MontoPesosOrigen=None)])
    par("montos-nulos-en-terminada", "FM-SFM-05: montos nulos en una fila TERMINADO => NullPointerException (suma disponibles)", entrada(),
        [g(1, "TERMINADO", MontoCuotasOrigen=None)])
    par("montos-precision-decimal128", "FM-SFM-05: sumas con 8 decimales y cifras grandes (DECIMAL128: 34 digitos): 123456789012345678.12345678 + 0.00000001", entrada(),
        [g(1, "INGRESADO", MontoCuotasOrigen="123456789012345678.12345678", MontoPesosOrigen="0.00000001"),
         g(2, "INGRESADO", MontoCuotasOrigen="0.00000001", MontoPesosOrigen="123456789012345678.12345678")])
    par("montos-destino-no-se-usa-en-no-rev", "FM-SFM-05: en una fila no REV se suma el ORIGEN; los montos de destino (999) no cuentan", entrada(),
        [g(1, "INGRESADO", MontoCuotasOrigen="3", MontoPesosOrigen="30", MontoCuotasDestino="999", MontoPesosDestino="999")])
    par("montos-negativos", "FM-SFM-05: montos negativos se suman sin validar", entrada(),
        [g(1, "INGRESADO", MontoCuotasOrigen="-5", MontoPesosOrigen="-50"), g(2, "INGRESADO", MontoCuotasOrigen="2", MontoPesosOrigen="20")])
    par("montos-cada-solicitud-recibe-los-de-su-grupo", "FM-SFM-05: dos grupos: cada solicitud recibe los totales de SU grupo (555: 1+2; 556: 10)", entrada(),
        [g(1, "INGRESADO", 555, MontoCuotasOrigen="1"), g(2, "INGRESADO", 555, MontoCuotasOrigen="2"), g(3, "INGRESADO", 556, MontoCuotasOrigen="10")])


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def conjunto_estados():
    """five groups: A INGRESADO, B TERMINADO, C ANULADO, D EN PROCESO and E TERMINADO+ANULADO (state '')"""
    return [g(1, "INGRESADO", 100, MontoCuotasOrigen="1"), g(2, "TERMINADO", 200, MontoCuotasOrigen="2"), g(3, "ANULADO", 300, MontoCuotasOrigen="3"),
            g(4, "EN PROCESO", 400, MontoCuotasOrigen="4"), g(5, "TERMINADO", 500, MontoCuotasOrigen="5"), g(6, "ANULADO", 500, MontoCuotasOrigen="6")]


def filtro_estado():
    for valor, slug, que in (("TERMINADO", "terminado", "solo el grupo B"), ("terminado", "terminado-minuscula", "(equals sensible a mayusculas) nada"),
                             ("ANULADO", "anulado", "solo el grupo C"), ("INGRESADO", "ingresado", "solo el grupo A"), ("EN PROCESO", "en-proceso", "solo el grupo D"),
                             ("XX", "desconocido", "nada"), ("", "vacio-quita-los-sin-estado", "A,B,C,D (el grupo E con Estado '' se descarta)"),
                             (" ", "un-espacio", "' ' no esta vacio: se filtra por Estado = ' ': nada"), ("EN PROCESO ", "con-espacio-final", "nada")):
        par("p-estado-" + slug, "FM-SFM-06 SFM-C05 D-F3A-08: p_Estado=%r => %s" % (valor, que), entrada(p_Estado=valor), conjunto_estados())
    par("p-estado-y-cliente", "FM-SFM-06: p_Estado='TERMINADO' junto con p_IdCliente=200: el estado se filtra DESPUES del SQL (solo las filas del cliente 200 entran al calculo de grupo)",
        entrada(p_Estado="TERMINADO", p_IdCliente=200),
        [fila(1, EstadoDetalle="TERMINADO", FolioContrato=100, IdCliente=200), fila(2, EstadoDetalle="ANULADO", FolioContrato=100, IdCliente=100, Folio="F2"),
         fila(3, EstadoDetalle="TERMINADO", FolioContrato=200, IdCliente=200, Folio="F3")])
    par("filtro-sql-cambia-el-grupo", "FM-SFM-06 D-F3A-08: el filtro del SQL (cliente 200) deja fuera una fila del grupo 100: el grupo visto es parcial (TERMINADO solo) en lugar de TERMINADO+ANULADO",
        entrada(p_IdCliente=200),
        [fila(1, EstadoDetalle="TERMINADO", FolioContrato=100, IdCliente=200), fila(2, EstadoDetalle="ANULADO", FolioContrato=100, IdCliente=100, Folio="F2")])


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def reinversion():
    def rev(ns, estado, rel=900, **ov):
        return fila(ns, TipoSolicitud="REV", EstadoDetalle=estado, FolioRelacionado=rel, **ov)
    par("rev-folio-relacionado-ingresadas", "FM-SFM-04/05 SFM-C04: 2 REV con FolioRelacionado 900 INGRESADO: Estado INGRESADO; comprometidas suman el DESTINO (cuotas 3+4, pesos 30+40), no el origen",
        entrada(), [rev(1, "INGRESADO", MontoCuotasDestino="3", MontoPesosDestino="30", MontoCuotasOrigen="99", MontoPesosOrigen="99"),
                    rev(2, "INGRESADO", MontoCuotasDestino="4", MontoPesosDestino="40", MontoCuotasOrigen="99", MontoPesosOrigen="99")])
    par("rev-terminada-y-en-proceso", "FM-SFM-04/05 SFM-C04: REV TERMINADO (destino 5) + EN PROCESO (destino 6) => EN PROCESO; disponibles 5 y comprometidas 6 (destino)",
        entrada(), [rev(1, "TERMINADO", MontoCuotasDestino="5", MontoPesosDestino="50"), rev(2, "EN PROCESO", MontoCuotasDestino="6", MontoPesosDestino="60")])
    par("rev-mixta-se-descarta", "FM-SFM-04 D-F3A-08: REV TERMINADO + ANULADO => '' (se descartan)", entrada(), [rev(1, "TERMINADO"), rev(2, "ANULADO")])
    par("rev-asocia-por-num-solicitud", "FM-SFM-04: REV con FolioRelacionado 0 se asocian por NumSolicitud igual (7): EN PROCESO + TERMINADO => EN PROCESO", entrada(),
        [rev(7, "EN PROCESO", 0, MontoCuotasDestino="1", MontoPesosDestino="10"), rev(7, "TERMINADO", 0, MontoCuotasDestino="2", MontoPesosDestino="20")])
    par("rev-folio-relacionado-cero-no-asocia", "FM-SFM-04: REV con FolioRelacionado 0 en solicitudes distintas no se asocian", entrada(), [rev(1, "TERMINADO", 0), rev(2, "ANULADO", 0)])
    par("rev-y-aporte-no-se-mezclan", "FM-SFM-04: una REV (FolioRelacionado 900) y un APO con FolioContrato 900 son grupos distintos: no se asocian entre si", entrada(),
        [rev(1, "TERMINADO", 900), fila(2, TipoSolicitud="APO", EstadoDetalle="ANULADO", FolioContrato=900)])
    par("rev-minuscula-es-normal", "FM-SFM-04: 'rev' en minuscula NO es REV (equals): usa FolioContrato y los montos de ORIGEN", entrada(),
        [fila(1, TipoSolicitud="rev", EstadoDetalle="INGRESADO", FolioContrato=5, MontoCuotasOrigen="8", MontoCuotasDestino="1")])
    par("rev-folio-relacionado-nulo", "FM-SFM-04 D-NOR-9: REV con FolioRelacionado nulo => NullPointerException", entrada(), [rev(1, "INGRESADO", None)])
    par("rev-montos-destino-nulos", "FM-SFM-05: REV INGRESADO con MontoCuotasDestino nulo => NullPointerException (suma el destino)", entrada(), [rev(1, "INGRESADO", MontoCuotasDestino=None)])
    par("rev-montos-origen-nulos-no-importan", "FM-SFM-05: REV con montos de ORIGEN nulos no falla (se suma el destino)", entrada(),
        [rev(1, "INGRESADO", MontoCuotasOrigen=None, MontoPesosOrigen=None, MontoCuotasDestino="2", MontoPesosDestino="20")])


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def tesoreria_ws():
    # ---- FM-SFM-07 / 08: the WS LEFT JOINs to Tesoreria (SFM ignores them; same data in both)
    par("tes-un-detalle-de-comprobante", "FM-SFM-07(a) WS-C01: IdIngresoEgreso 5001 (tipo RESCFM) con un detalle de comprobante vigente (comprobante 7001) => IdComprobante 7001 en el WS; "
        "el SFM no trae IdComprobante", entrada(), [fila(1, IdIngresoEgreso=5001, TipoSolicitud="RES")], seed=tes(5001, "RESCFM", [(1, 7001, "ING")]))
    par("tes-dos-detalles-duplican-la-fila", "FM-SFM-08 WS-C03 D-F3A-10: un IngresoEgreso con DOS detalles de comprobante vigentes => el WS devuelve la solicitud dos veces (IdComprobante "
        "7001 y 7002) y las cuenta dos veces: comprometidas dobles", entrada(), [fila(1, IdIngresoEgreso=5001, TipoSolicitud="RES", MontoCuotasOrigen="10", MontoPesosOrigen="100")],
        seed=tes(5001, "RESCFM", [(1, 7001, "ING"), (2, 7002, "LIQ")]))
    par("tes-tres-detalles", "FM-SFM-08 D-F3A-10: tres detalles => tres filas; ademas una segunda solicitud con otro ingreso/egreso", entrada(),
        [fila(1, IdIngresoEgreso=5001, TipoSolicitud="RES"), fila(2, IdIngresoEgreso=5002, TipoSolicitud="APO", Folio="F2")],
        seed=tes(5001, "RESCFM", [(1, 7001, "ING"), (2, 7002, "ING"), (3, 7003, "ING")]) + tes(5002, "APORFM", [(4, 7004, "ING")]))
    par("tes-detalle-anulado-no-cuenta", "FM-SFM-07(a): un detalle ANU (con su CDO) se excluye en el NOT IN y en el JOIN: queda el vigente; si solo hay uno anulado => IdComprobante 0, una fila",
        entrada(), [fila(1, IdIngresoEgreso=5001, TipoSolicitud="RES"), fila(2, IdIngresoEgreso=5002, TipoSolicitud="RES", Folio="F2")],
        seed=tes(5001, "RESCFM", [(1, 7001, "ANU"), (2, 7002, "ING")]) + tes(5002, "RESCFM", [(3, 7003, "ANU")]))
    par("tes-tipo-operacion-distinta", "FM-SFM-07(a): IngresoEgreso con IdTipoOperacion distinto de RESCFM/APORFM no se une => IdComprobante 0", entrada(),
        [fila(1, IdIngresoEgreso=5001)], seed=tes(5001, "OTRO", [(1, 7001, "ING")]))
    par("tes-aporte-tipo-aporfm", "FM-SFM-07(a): tipo APORFM tambien se une", entrada(), [fila(1, IdIngresoEgreso=5001)], seed=tes(5001, "APORFM", [(1, 7001, "ING")]))
    par("tes-ingreso-egreso-cero", "FM-SFM-07(a): IdIngresoEgreso 0 en la vista no se une aunque exista un IngresoEgreso 0 => IdComprobante 0", entrada(),
        [fila(1, IdIngresoEgreso=0)], seed=tes(0, "RESCFM", [(1, 7001, "ING")]))
    par("tes-ingreso-egreso-nulo", "FM-SFM-07(a): IdIngresoEgreso nulo => sin union => IdComprobante 0", entrada(), [fila(1, IdIngresoEgreso=None)])
    par("tes-sin-ingreso-egreso", "FM-SFM-07(a): IdIngresoEgreso 5001 que no existe en Tesoreria => IdComprobante 0", entrada(), [fila(1, IdIngresoEgreso=5001)])
    par("tes-detalle-con-estado-nulo", "FM-SFM-07(a): detalle con IdEstado nulo: el NOT IN del JOIN es desconocido => no se une: IdComprobante 0", entrada(),
        [fila(1, IdIngresoEgreso=5001)], seed=tes(5001, "RESCFM", [(1, 7001, None)]))
    par("tes-origen-sin-detalle", "FM-SFM-07(a): ComprobanteDetalleOrigen apunta a un detalle que no existe => IdComprobante 0", entrada(), [fila(1, IdIngresoEgreso=5001)],
        seed=['INSERT INTO %s VALUES (5001, \'RESCFM\');' % TES_IE, 'INSERT INTO %s VALUES (99, 5001);' % TES_CDO])
    par("tes-comprobante-nulo", "FM-SFM-07(a): detalle vigente con IdComprobante nulo => COALESCE a 0", entrada(), [fila(1, IdIngresoEgreso=5001)],
        seed=tes(5001, "RESCFM", [(1, None, "ING")]))
    par("tes-duplicados-y-estado-global", "FM-SFM-08 D-F3A-10: la solicitud duplicada por el JOIN (2 detalles) y otra en el mismo folio TERMINADO: el conteo por estado se infla "
        "(INGRESADO x2 + TERMINADO => Estado '' y se descarta)", entrada(),
        [g(1, "INGRESADO", IdIngresoEgreso=5001), g(2, "TERMINADO")], seed=tes(5001, "RESCFM", [(1, 7001, "ING"), (2, 7002, "ING")]))
    par("tes-duplicados-con-p-estado", "FM-SFM-08: con p_Estado='INGRESADO' el WS devuelve las dos copias de la solicitud", entrada(p_Estado="INGRESADO"),
        [g(1, "INGRESADO", IdIngresoEgreso=5001, MontoCuotasOrigen="5", MontoPesosOrigen="50")], seed=tes(5001, "RESCFM", [(1, 7001, "ING"), (2, 7002, "ING")]))
    par("tes-detalle-origen-repetido", "FM-SFM-08: la misma pareja ComprobanteDetalleOrigen repetida (dato sucio) duplica la fila otra vez", entrada(),
        [fila(1, IdIngresoEgreso=5001)], seed=tes(5001, "RESCFM", [(1, 7001, "ING")]) + ['INSERT INTO %s VALUES (1, 5001);' % TES_CDO])


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def determinismo():
    # the legacy runs queryParallel, the state calculation and every filter on parallelStream(): large sets exercise the ordering of the output list and of the per-row results
    filas = []
    estados = ["INGRESADO", "TERMINADO", "ANULADO", "EN PROCESO"]
    n = 0
    for grupo in range(1, 21):
        for k in range(3):
            n += 1
            e = estados[(grupo + k) % 4] if grupo % 3 else estados[grupo % 4]
            filas.append(fila(n, EstadoDetalle=e, FolioContrato=1000 + grupo, IdCliente=100 + (grupo % 4), Folio="F%d" % n, MontoCuotasOrigen=str(grupo + k * 0.5),
                              MontoPesosOrigen=str((grupo + k) * 10)))
    for grupo in range(1, 6):
        for k in range(2):
            n += 1
            filas.append(fila(n, TipoSolicitud="REV", EstadoDetalle=estados[(grupo + k) % 4], FolioRelacionado=2000 + grupo, Folio="R%d" % n,
                              MontoCuotasDestino=str(grupo * 3 + k), MontoPesosDestino=str(grupo * 30 + k)))
    par("determinismo-sesenta-y-diez-filas", "FM-SFM-04/05/06 SFM-C01..C05: 20 grupos de 3 filas + 5 grupos REV de 2: el estado, los montos y el orden de salida son los mismos en las 20 corridas "
        "(parallelStream en todo el camino)", entrada(), filas)
    par("determinismo-con-estado", "FM-SFM-06: el mismo conjunto con p_Estado='EN PROCESO'", entrada(p_Estado="EN PROCESO"), filas)
    grande = [fila(i, EstadoDetalle=("INGRESADO" if i % 7 else "EN PROCESO"), FolioContrato=7, Folio="G%d" % i, MontoCuotasOrigen=str(i), MontoPesosOrigen=str(i * 10)) for i in range(1, 201)]
    par("determinismo-grupo-de-doscientas", "FM-SFM-04/05: UN grupo de 200 filas (FolioContrato 7): cada una asocia a las 200; sumas exactas en cualquier orden de reduccion", entrada(), grande)
