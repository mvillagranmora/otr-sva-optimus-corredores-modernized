"""FMM-SFM cases, part A (ConsultarSolicitudesFFMM + WS twin): contract FM-SFM-01, filters FM-SFM-02, hour and date of entry FM-SFM-03.
Ids: FM-SFM-01..08 (RULES_FONDOMUTUO_F3_A.md 8.3), D-F3A-09/10/25/28 and D-NOR-10 (section 9), matrix SFM-C06..C08, WS-C01..C02 (section 11).
Inputs only: expected.json is recorded from the legacy by record_sfm.sh. Every scenario is run against BOTH classes (see cases_sfm_lib.par)."""
from cases_sfm_lib import *  # noqa: F401,F403


def generar():
    contrato()
    parametros_y_entrada()
    filtros()
    hora_y_fecha()


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def contrato():
    # ---- FM-SFM-01 / SFM-C01: the contract, Gson plain, counter and messages never filled
    par("defecto-tres-solicitudes", "FM-SFM-01 D-F3A-25: sin filtros (los del constructor del legacy): 3 solicitudes de 2 clientes; salida {P_OUT_ERRORESCONTADOR 0, P_OUT_MENSAJES [], "
        "P_OUT_SOLICITUDES}; new Gson() plano: los nulos no se escriben; cada fila con su Estado (una por grupo)",
        entrada(),
        [fila(1, TipoSolicitud="APO", EstadoDetalle="INGRESADO", IdCliente=100),
         fila(2, TipoSolicitud="RES", EstadoDetalle="TERMINADO", IdCliente=200, Folio="F2"),
         fila(3, TipoSolicitud="APO", EstadoDetalle="ANULADO", IdCliente=100, Folio="F3")])
    par("vista-vacia", "FM-SFM-01: la vista no tiene filas => P_OUT_SOLICITUDES vacia, contador 0, sin mensajes", entrada(), [])
    par("campos-extra-y-cliente-texto", "FM-SFM-01 FM-SFM-02: un campo desconocido se ignora y p_IdCliente como texto '100' lo coacciona Gson a Long (solo el cliente 100)",
        entrada(p_IdCliente="100", p_Otro=1),
        [fila(1, IdCliente=100), fila(2, IdCliente=200), fila(3, IdCliente=100)])
    par("cliente-no-numerico", "FM-SFM-01: p_IdCliente 'abc' => JsonSyntaxException (HTTP 500) antes de leer nada", entrada(p_IdCliente="abc"), [fila(1)])
    par("filtro-estado-no-es-parametro-sql", "FM-SFM-01/02: p_Estado no es parametro del SQL: con p_Estado='TERMINADO' la vista se lee entera y el estado se filtra en memoria",
        entrada(p_Estado="TERMINADO"),
        [fila(1, EstadoDetalle="TERMINADO"), fila(2, EstadoDetalle="INGRESADO")])
    par("orden-del-sql-sin-order-by", "FM-SFM-06 D-F3A-28: la salida sale en el orden de la consulta (sin ORDER BY): filas insertadas 5,1,4,2,3 salen en ese orden",
        entrada(),
        [fila(5, Folio="F5"), fila(1, Folio="F1"), fila(4, Folio="F4"), fila(2, Folio="F2"), fila(3, Folio="F3")])
    par("serie-origen-alias-queryparallel", "D-F3A-28: el SQL escribe \"IdSerieFondo\" as \"IdSerieFondoOrigen\" y queryParallel 'no soporta AS': se observa que campo recibe la columna "
        "(IdSerieFondoOrigen / IdSerieFondo) con la serie FM002B de la vista",
        entrada(), [fila(1, IdSerieFondo="FM002B", SerieFondo="FM002B"), fila(2, IdSerieFondo="FM001A", IdSerieFondoDestino="FM003C")])
    par("tipos-numericos-y-fechas-en-texto", "FM-SFM-01: los 4 montos con 8 decimales, cuotas y valor cuota con su escala, cadenas de fecha tal cual de la vista (sin ajustar)",
        entrada(),
        [fila(1, MontoPesosOrigen="123456789.12345678", MontoCuotasOrigen="0.00000001", ValorCuotaOrigen="1693.85619", FechaCertificacion="2026-03-04",
              FechaLiquidacion="", FechaCurse="2026-03-02", MontoCuotasComision="0.5", PorcentajeOrigen="33.33333333")])


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def parametros_y_entrada():
    # ---- FM-SFM-01: explicit nulls in the request => NullPointerException (isEmpty / equals on a null field)
    for campo in ("p_IdCliente", "p_TipoSolicitud", "p_Origen", "p_Folio", "p_TipoAhorro", "p_TipoCuenta", "p_IdSerieFondo", "p_FechaDesde", "p_FechaHasta", "p_Estado"):
        par("campo-nulo-" + campo.lower().replace("p_", ""), "FM-SFM-01 SFM-C08 D-F3A-25: %s con null explicito => NullPointerException (%s)" % (
            campo, "al decidir el filtro" if campo != "p_Estado" else "DESPUES de leer y calcular, en entrada.p_Estado.isEmpty()"),
            entrada(**{campo: None}), [fila(1)])
    # ---- the three parameters read by the class, and the company
    par("sin-parametro-adc", "FM-SFM-02: falta PLATAFORMA/PLA.FiltroAdministracionCarteraWS => Exception 'Parametro ... no encontrado' (HTTP 500)", entrada(), [fila(1)],
        seed=config("PLA.FiltroAdministracionCarteraWS", None))
    par("sin-parametro-no-automaticas", "FM-SFM-02: falta FONDOMUTUO/WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS => Exception 'Parametro ... no encontrado'", entrada(),
        [fila(1)], seed=parametro("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", None))
    par("empresa-nula", "FM-SFM-02: p_IdEmpresa nulo => NullPointerException (ConfiguracionSistema busca 'PLATAFORMA-null')", entrada(p_IdEmpresa=None), [fila(1)])
    par("empresa-sin-configuracion", "FM-SFM-02: empresa 99 sin registros de configuracion => NullPointerException (registros.get(...) nulo)", entrada(p_IdEmpresa=99), [fila(1)])
    par("empresa-ausente", "FM-SFM-02: sin p_IdEmpresa en la entrada (nulo por defecto del pojo) => NullPointerException", {"p_IdUsuario": 7, "p_IdModulo": "FMM"}, [fila(1)])
    par("sin-segundos-zona-horaria", "FM-SFM-03 D-NOR-10: falta PLA.SegundosZonaHoraria => Exception 'Parametro ... no encontrado' (se lee despues de los otros dos)", entrada(),
        [fila(1)], seed=config("PLA.SegundosZonaHoraria", None))
    par("segundos-no-numericos", "FM-SFM-03: PLA.SegundosZonaHoraria = 'abc' => NumberFormatException", entrada(), [fila(1)], seed=config("PLA.SegundosZonaHoraria", "abc"))
    par("segundos-con-decimales", "FM-SFM-03: PLA.SegundosZonaHoraria = '3600.5' => NumberFormatException (Long.parseLong)", entrada(), [fila(1)],
        seed=config("PLA.SegundosZonaHoraria", "3600.5"))
    par("segundos-con-espacio", "FM-SFM-03: PLA.SegundosZonaHoraria = ' 3600' => NumberFormatException (sin trim)", entrada(), [fila(1)], seed=config("PLA.SegundosZonaHoraria", " 3600"))
    par("segundos-con-signo-mas", "FM-SFM-03: PLA.SegundosZonaHoraria = '+3600' => Long.parseLong lo acepta: hora 13:00:00 -> 14:00:00", entrada(), [fila(1, offset=3600)],
        seed=config("PLA.SegundosZonaHoraria", "+3600"))
    par("segundos-fuera-de-int", "FM-SFM-03 D-F3A-09: PLA.SegundosZonaHoraria = 3000000000 se trunca con (int) a -1294967296: la hora se desplaza por ese valor (13:00:00 -> 03:51:...)",
        entrada(), [fila(1)], seed=config("PLA.SegundosZonaHoraria", "3000000000"))


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def filtros():
    base = [fila(1, IdCliente=100, TipoSolicitud="APO", Origen="WEB", Folio="F1", TipoAhorro="A1", TipoCuenta="C1", IdSerieFondo="FM001A", FechaSolicitud="2026-03-01"),
            fila(2, IdCliente=200, TipoSolicitud="RES", Origen="MESA", Folio="F2", TipoAhorro="A2", TipoCuenta="C2", IdSerieFondo="FM002B", FechaSolicitud="2026-03-05"),
            fila(3, IdCliente=100, TipoSolicitud="RES", Origen="MESA", Folio="F10", TipoAhorro="A1", TipoCuenta="C2", IdSerieFondo="FM001A", FechaSolicitud="2026-03-10"),
            fila(4, IdCliente=300, TipoSolicitud="APO", Origen="WEB", Folio="F2", TipoAhorro="A2", TipoCuenta="C1", IdSerieFondo="FM002B", FechaSolicitud="2026-03-15")]
    # ---- FM-SFM-02: one filter at a time (FiltraX = S when the field is not empty)
    par("filtro-cliente", "FM-SFM-02: p_IdCliente=200 => FiltraCliente S: solo la solicitud 2", entrada(p_IdCliente=200), base)
    par("filtro-cliente-cero", "FM-SFM-02: p_IdCliente=0 => sin filtro de cliente (esFiltro: equals(0L))", entrada(p_IdCliente=0), base)
    par("filtro-cliente-negativo", "FM-SFM-02: p_IdCliente=-1 => es filtro (distinto de 0): ninguna fila", entrada(p_IdCliente=-1), base)
    par("filtro-tipo-solicitud", "FM-SFM-02: p_TipoSolicitud='RES' => solicitudes 2 y 3", entrada(p_TipoSolicitud="RES"), base)
    par("filtro-tipo-solicitud-minuscula", "FM-SFM-02: p_TipoSolicitud='res' => la comparacion SQL es exacta: ninguna fila", entrada(p_TipoSolicitud="res"), base)
    par("filtro-origen", "FM-SFM-02: p_Origen='MESA' => solicitudes 2 y 3", entrada(p_Origen="MESA"), base)
    par("filtro-folio-exacto", "FM-SFM-02: p_Folio='F1' => solo F1 (no F10: igualdad exacta)", entrada(p_Folio="F1"), base)
    par("filtro-tipo-ahorro", "FM-SFM-02: p_TipoAhorro='A2' => solicitudes 2 y 4", entrada(p_TipoAhorro="A2"), base)
    par("filtro-tipo-cuenta", "FM-SFM-02: p_TipoCuenta='C2' => solicitudes 2 y 3", entrada(p_TipoCuenta="C2"), base)
    par("filtro-serie-fondo", "FM-SFM-02: p_IdSerieFondo='FM002B' filtra por la columna IdSerieFondo de la vista => solicitudes 2 y 4", entrada(p_IdSerieFondo="FM002B"), base)
    par("filtro-fecha-desde-inclusive", "FM-SFM-02: p_FechaDesde='2026-03-05' (>=, comparacion de texto) => solicitudes 2, 3 y 4", entrada(p_FechaDesde="2026-03-05"), base)
    par("filtro-fecha-hasta-inclusive", "FM-SFM-02: p_FechaHasta='2026-03-10' (<=) => solicitudes 1, 2 y 3", entrada(p_FechaHasta="2026-03-10"), base)
    par("filtro-rango-de-fechas", "FM-SFM-02: desde 2026-03-05 hasta 2026-03-10 => solicitudes 2 y 3 (extremos incluidos)", entrada(p_FechaDesde="2026-03-05", p_FechaHasta="2026-03-10"), base)
    par("filtro-rango-invertido", "FM-SFM-02: desde > hasta => ninguna fila (no hay validacion)", entrada(p_FechaDesde="2026-03-10", p_FechaHasta="2026-03-05"), base)
    par("filtro-fecha-sin-ceros", "FM-SFM-02: la fecha se compara como TEXTO: '2026-3-5' > '2026-03-15' y '2026-03-10' (caracter '3' > '0')", entrada(p_FechaDesde="2026-3-5"), base)
    par("filtro-fecha-con-hora", "FM-SFM-02: p_FechaHasta='2026-03-10 23:59:59' (mas larga que la fecha de la vista): '2026-03-10' <= '2026-03-10 23:59:59' (prefijo)",
        entrada(p_FechaHasta="2026-03-10 23:59:59"), base)
    par("filtro-fecha-basura", "FM-SFM-02: p_FechaDesde='hoy' (sin validacion de formato): todo texto de fecha < 'hoy' => ninguna fila", entrada(p_FechaDesde="hoy"), base)
    par("filtros-todos-a-la-vez", "FM-SFM-02: los nueve filtros juntos (AND): cliente 100, RES, MESA, F10, A1, C2, FM001A, desde 03-10, hasta 03-10 => solicitud 3",
        entrada(p_IdCliente=100, p_TipoSolicitud="RES", p_Origen="MESA", p_Folio="F10", p_TipoAhorro="A1", p_TipoCuenta="C2", p_IdSerieFondo="FM001A",
                p_FechaDesde="2026-03-10", p_FechaHasta="2026-03-10"), base)
    par("filtros-contradictorios", "FM-SFM-02: cliente 100 y origen MESA y tipo APO: ninguna fila cumple todo", entrada(p_IdCliente=100, p_Origen="MESA", p_TipoSolicitud="APO"), base)
    par("filtro-espacio-no-se-recorta", "FM-SFM-02: p_Origen=' ' (un espacio) NO esta vacio => FiltraOrigen S (sin trim): solo la fila cuyo Origen es ' '",
        entrada(p_Origen=" "), [fila(1, Origen=" "), fila(2, Origen="WEB"), fila(3, Origen="")])
    par("filtro-vacio-no-filtra", "FM-SFM-02: p_Origen='' => sin filtro: las filas con Origen vacio, nulo y lleno salen todas (el nulo con Origen NULL tambien)",
        entrada(p_Origen=""), [fila(1, Origen=""), fila(2, Origen="WEB"), fila(3, Origen=None)])
    par("filtro-folio-con-nulos", "FM-SFM-02: p_Folio='F1' con Folio nulo en la vista: NULL = 'F1' es desconocido => la fila nula no sale", entrada(p_Folio="F1"),
        [fila(1, Folio="F1"), fila(2, Folio=None), fila(3, Folio="f1")])

    # ---- FM-SFM-02: FiltraTipoMovimiento = S always
    tm = [fila(1, TipoMovimiento="RESNORM", TipoSolicitud="RES"), fila(2, TipoMovimiento="INGGTA"), fila(3, TipoMovimiento=None), fila(4, TipoMovimiento="ZZZ"),
          fila(5, TipoMovimiento="APONORM"), fila(6, TipoMovimiento="RESTRASP", TipoSolicitud="RES"), fila(7, TipoMovimiento="apoNorm"), fila(8, TipoMovimiento="APOREINV")]
    par("tipo-movimiento-siempre-filtra", "FM-SFM-02: FiltraTipoMovimiento=S siempre: salen solo las filas cuyo TipoMovimiento esta en FMM.TipoMovimientoFondoMutuo y no es INGGTA "
        "(se excluyen INGGTA, nulo, desconocido ZZZ y 'apoNorm' por mayusculas)", entrada(), tm)
    # ---- FM-SFM-02: PLA.FiltroAdministracionCarteraWS and the not-automatic filter
    adc = [fila(1, EsADC="N"), fila(2, EsADC="S"), fila(3, EsADC=None), fila(4, EsADC=""), fila(5, EsADC="n")]
    for valor, slug in (("S", "s"), ("N", "n"), ("s", "minuscula"), ("SI", "si"), ("", "vacio"), (" S", "espacio")):
        par("adc-" + slug, "FM-SFM-02: PLA.FiltroAdministracionCarteraWS=%r (SI.equals): %s" % (valor, "solo EsADC='N' (se excluyen 'S', nulo, vacio y 'n')" if valor == "S"
            else "no filtra (distinto de 'S' exacto)"), entrada(), adc, seed=config("PLA.FiltroAdministracionCarteraWS", valor))
    auto = [fila(1, IngresoAutomatico="N"), fila(2, IngresoAutomatico="S"), fila(3, IngresoAutomatico=None), fila(4, IngresoAutomatico=""), fila(5, IngresoAutomatico="n")]
    for valor, slug in (("S", "s"), ("N", "n"), ("s", "minuscula"), ("X", "x")):
        par("no-automaticas-" + slug, "FM-SFM-02: WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS=%r: %s" % (valor, "solo IngresoAutomatico='N'" if valor == "S"
            else "no filtra"), entrada(), auto, seed=parametro("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", valor))
    par("adc-y-no-automaticas-juntos", "FM-SFM-02: ambos parametros en S: solo filas con EsADC='N' E IngresoAutomatico='N'", entrada(),
        [fila(1, EsADC="N", IngresoAutomatico="N"), fila(2, EsADC="N", IngresoAutomatico="S"), fila(3, EsADC="S", IngresoAutomatico="N"), fila(4, EsADC="S", IngresoAutomatico="S")],
        seed=config("PLA.FiltroAdministracionCarteraWS", "S") + parametro("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", "S"))


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def hora_y_fecha():
    f = "2026-03-10"
    # ---- FM-SFM-03 (SFM): HoraSolicitud + offset (wraps at midnight, the date is NOT adjusted) and FechaIngreso; WS: from FechaIngresoLocal
    par("hora-23-30-mas-3600", "FM-SFM-03 SFM-C07 D-F3A-09: offset +3600 y hora 23:30:00 => HoraSolicitud 00:30:00 y FechaIngreso '2026-03-10 00:30:00' (la fecha no avanza)",
        entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="23:30:00", offset=3600)], seed=config("PLA.SegundosZonaHoraria", "3600"))
    par("hora-02-15-menos-10800", "FM-SFM-03 SFM-C07 D-F3A-09: offset -10800 y hora 02:15:00 => 23:15:00 con la MISMA fecha (el dia anterior en realidad)",
        entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="02:15:00")])
    par("offset-cero", "FM-SFM-03: offset 0 => la hora queda igual", entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="09:05:07", offset=0)], seed=config("PLA.SegundosZonaHoraria", "0"))
    par("offset-un-dia", "FM-SFM-03: offset 86400 (un dia) => la hora vuelve a ser la misma", entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="09:05:07", offset=86400)],
        seed=config("PLA.SegundosZonaHoraria", "86400"))
    par("offset-un-dia-y-medio", "FM-SFM-03: offset 129600 (36 h) => +12 h con vuelta", entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="09:05:07", offset=129600)],
        seed=config("PLA.SegundosZonaHoraria", "129600"))
    par("offset-menos-un-segundo", "FM-SFM-03: offset -1 con hora 00:00:00 => 23:59:59", entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="00:00:00", offset=-1)],
        seed=config("PLA.SegundosZonaHoraria", "-1"))
    par("hora-medianoche-exacta", "FM-SFM-03: hora 00:00:00 con offset -10800 => 21:00:00", entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="00:00:00")])
    par("hora-con-milisegundos", "FM-SFM-03: HoraSolicitud '10:00:00.123' no cumple HH:mm:ss => DateTimeParseException (SFM); WS no la lee y la toma de FechaIngresoLocal",
        entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="10:00:00.123")])
    par("hora-un-digito", "FM-SFM-03: HoraSolicitud '9:05:00' (un digito) => DateTimeParseException en SFM", entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="9:05:00")])
    par("hora-sin-segundos", "FM-SFM-03: HoraSolicitud '10:00' => DateTimeParseException en SFM", entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="10:00")])
    par("hora-vacia", "FM-SFM-03: HoraSolicitud '' => DateTimeParseException en SFM", entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="")])
    par("hora-nula", "FM-SFM-03 SFM-C08: HoraSolicitud nula => NullPointerException en SFM (el WS no la lee)", entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud=None)])
    par("fecha-solicitud-nula", "FM-SFM-03: FechaSolicitud nula => NullPointerException en SFM (concat); el WS la reemplaza con la de FechaIngresoLocal", entrada(),
        [fila(1, FechaSolicitud=None, HoraSolicitud="13:00:00", FechaIngresoLocal="2026-03-10 10:00:00")])
    par("hora-24-00-00", "FM-SFM-03: HoraSolicitud '24:00:00' (borde del formato HH)", entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="24:00:00")])
    par("hora-limite-23-59-59-mas-1", "FM-SFM-03: 23:59:59 con offset +1 => 00:00:00 (vuelta exacta)", entrada(),
        [fila(1, FechaSolicitud=f, HoraSolicitud="23:59:59", offset=1)], seed=config("PLA.SegundosZonaHoraria", "1"))
    par("fecha-ingreso-concatena-sin-validar", "FM-SFM-03: FechaIngreso = FechaSolicitud + ' ' + hora, FechaSolicitud tal cual aunque no sea fecha ('ayer')", entrada(),
        [fila(1, FechaSolicitud="ayer", HoraSolicitud="13:00:00")])
    # ---- FM-SFM-07 (WS): the entry date and time come from FechaIngresoLocal
    par("fecha-ingreso-local-09-05-00", "FM-SFM-07(c) WS-C02: FechaIngresoLocal 2026-03-10 09:05:00 => FechaIngreso '2026-03-10 09:05:00', FechaSolicitud '2026-03-10', "
        "HoraSolicitud '09:05' (LocalTime.toString omite los segundos en cero)", entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="12:05:00", FechaIngresoLocal="2026-03-10 09:05:00")])
    par("ws-fecha-ingreso-local-con-segundos", "FM-SFM-07(c): FechaIngresoLocal 09:05:07 => HoraSolicitud '09:05:07'", entrada(),
        [fila(1, FechaSolicitud=f, HoraSolicitud="12:05:07", FechaIngresoLocal="2026-03-10 09:05:07")], solo=WS)
    par("ws-fecha-ingreso-local-con-fraccion", "FM-SFM-07(c): FechaIngresoLocal 09:05:00.5 => HoraSolicitud '09:05:00.500' y FechaIngreso sin fraccion (yyyy-MM-dd HH:mm:ss)", entrada(),
        [fila(1, FechaSolicitud=f, HoraSolicitud="12:05:00", FechaIngresoLocal="2026-03-10 09:05:00.5")], solo=WS)
    par("ws-fecha-ingreso-local-medianoche", "FM-SFM-07(c): FechaIngresoLocal 00:00:00 => HoraSolicitud '00:00'", entrada(),
        [fila(1, FechaSolicitud=f, HoraSolicitud="03:00:00", FechaIngresoLocal="2026-03-10 00:00:00")], solo=WS)
    par("fecha-ingreso-local-nula", "FM-SFM-07(c) D-F3A-10: FechaIngresoLocal nula => NullPointerException en el WS (SFM no la lee)", entrada(),
        [fila(1, FechaSolicitud=f, HoraSolicitud="13:00:00", FechaIngresoLocal=None)])
    par("otra-fecha-que-fecha-solicitud", "FM-SFM-07(c) D-F3A-10: FechaSolicitud de la vista '2026-03-10' pero FechaIngresoLocal es del 03-09: el filtro (desde 03-10) usa la columna "
        "de la vista y la salida del WS muestra FechaSolicitud 2026-03-09", entrada(p_FechaDesde="2026-03-10"),
        [fila(1, FechaSolicitud=f, HoraSolicitud="02:00:00", FechaIngresoLocal="2026-03-09 23:00:00")])
    par("ws-offset-invalido-se-lee-igual", "FM-SFM-07(c) D-NOR-10: el WS lee y fija el desfase aunque no lo use: PLA.SegundosZonaHoraria='abc' => NumberFormatException tambien en el WS",
        entrada(), [fila(1)], seed=config("PLA.SegundosZonaHoraria", "abc"), solo=WS)
    par("ws-fecha-ingreso-local-viaja-con-formato-gson", "FM-SFM-07(e) D-F3A-10: FechaIngresoLocal viaja en la respuesta con el formato por defecto de Gson (depende de la JVM y su CLDR)",
        entrada(), [fila(1, FechaSolicitud=f, HoraSolicitud="12:05:07", FechaIngresoLocal="2026-03-10 15:05:07")], solo=WS)
