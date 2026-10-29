"""ServicioCumpleRequisitosCrearSolicitudAporteRescate cases (FM-REQ-01..21, D-REQ-1..4)."""
import copy

from cases3b_lib import caso, dicc, del_param, set_param, sql
from cases3b_rows import fila

S = "ServicioCumpleRequisitosCrearSolicitudAporteRescate"
DIC = [dicc("CLIENTE_BLOQUEADO", "Cliente ::DocumentoIdentidad:: bloqueado"),
       dicc("MONTO_MAXIMO_REQ_APO_RES", "Monto maximo ::IdInstrumento:: ::CodigoPortafolio:: ::DocumentoIdentidad::"),
       dicc("FONDO_CERRADO_REQ_APO_RES", "Fondo ::IdInstrumento:: cerrado"), dicc("NO_MONEY_MARKET", "No es money market"),
       dicc("MONTO_VALIDO", "Monto invalido"),
       dicc("INSTRUMENTO_NO_VIGENTE_RUT_CLIENTE", "Instrumento ::Nemotecnico:: no vigente ::DocumentoIdentidad::"),
       dicc("FECHA_INVALIDA_RESCATE_REQ_APO_RES", "Fecha rescate invalida ::DocumentoIdentidad::"),
       dicc("CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO", "Bloqueado por cumplimiento ::NombreCliente:: ::DocumentoIdentidad::"),
       dicc("NUMERO_CUOTA_VALIDO_REQ_APO_RES", "Cuotas invalidas ::NombreCliente::"),
       dicc("ERROR_AL_ENCONTRAR_ID_USUARIO", "Usuario sin empleado")]
# a second client/portfolio (101 / 501), recently created (24 h window of PLA.HorasOperaCINClienteNuevoBloqueado) and blocked => "prospect"
CLIENTE2 = [sql("BSCL.SocioNegocio.BD::PER.DocumentoIdentidad", IdDocumentoIdentidad=2, DocumentoIdentidad="22222222-2", IdTipoDocumentoIdentidad="RUT",
                Predeterminado="S"),
            sql("BSCL.SocioNegocio.BD::PER.Persona", IdPersona=1001, IdDocumentoIdentidad=2),
            sql("BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto", IdPersona=1001, NombreCompleto="Cliente Ficticio Dos"),
            "INSERT INTO \"BSCL.SocioNegocio.BD::CLI.Cliente\" (\"IdCliente\", \"IdPersona\", \"FechaCreacion\", \"IdPersonaEjecutivo\") VALUES (101, 1001, "
            "TIMESTAMP '2026-03-02 10:00:00', 2000);",
            sql("BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteEstado", IdCliente=101, IdEstado="BLQ"),
            sql("BSCL.Portafolio.BD::POR.Portafolio", IdPortafolio=501, CodigoPortafolio="PF501", IdCliente=101, IdEstado="ACT", Proposito="PROPGEN")]
CUMPLIMIENTO = [sql("BSCL.Plataforma.BD::IND.TipoIndicador", IdTipoIndicador=9, IdTipoEntidad="CLIENTE", CodigoTipoIndicador="BCCUM", IdEstado="ACT"),
                sql("BSCL.Plataforma.BD::IND.Indicador", IdEntidad=100, IdTipoIndicador=9, IdTipoEntidad="CLIENTE", ValorIndicador="S")]

SOL = dict(IdInterno=1, IdCliente=100, IdPortafolio=500, IdSerieFondo="FM001A", IdTipoMovimientoFondoMutuo="APONORM", IdEstadoSerie="ACT", Monto=1000,
           NumeroCuotas=10, EstadoFondo="A", EstadoBloqueo="N", EsFondoValido="S", EsFechaValidaAporte="S", EsFechaValidaRescate="S", EsDiferido="S",
           EsMontoMaximo="S", EsMontoMinimo="S", ValorCuota=100, Overnight="N", EstadoLiquidez="A", EsMoneyMarket="S", EsFamiliaEquivalente="S",
           EsPortafolioRegimenValido="S", EsRegimenValido="S", PermiteAporte="S", PermiteRescateMandato="S", PermiteCambioGrupo="S",
           EsRegimenDestinoValido="S", EsPortafolioValido="S", EsCuentaInversionValido="S", IdTipoOperacion="APORFM", IdTipoProducto="INFM")
PORT = dict(IdInterno=1, IdPortafolio=500, IdPortafolioReal=500, IdEstadoPortafolio="ACT", IdCliente=100, IdClienteReal=100, IdEstadoCliente="ACT",
            IdClienteDestino=0, IdClienteRealDestino=0, IdPortafolioDestino=500, IdPortafolioRealDestino=0, IdEstadoClienteDestino="",
            IdEstadoPortafolioDestino="", DocumentoIdentidad="11111111-1", IdEmpleadoPortafolio=70)


def sol(**ov):
    d = copy.deepcopy(SOL)
    d.update(ov)
    return d


def port(**ov):
    d = copy.deepcopy(PORT)
    d.update(ov)
    return d


def ent(sols, ports=None, **extra):
    ports = ports if ports is not None else [port(IdInterno=s["IdInterno"]) for s in sols]
    e = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_SolicitudesAporteRescate": sols, "p_Portafolios": ports}
    e.update(extra)
    return e


def productos(*items):
    return {"ConsultarProductosPortafolio": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_Mensajes": [], "p_out_PortafolioProductos": [
        {"IdPortafolio": p, "IdEstadoPerfilProducto": e, "IdTipoProducto": t, "IdTipoOperacion": o} for (p, e, t, o) in items]}}}


def cases():
    yield caso("REQ-00_todo-correcto", S, "FM-REQ-02..19: solicitud sin hallazgos", ent([sol()]), DIC)
    yield caso("REQ-02-01_cliente-bloqueado", S, "FM-REQ-02: estado del cliente != ACT => CLIENTE_BLOQUEADO con documento",
               ent([sol()], [port(IdEstadoCliente="BLQ")]), DIC)
    yield caso("REQ-02-02_prospecto-permitido", S, "FM-REQ-02: cliente no ACT pero prospecto nuevo con p_PermiteOperarProspectos=S: sin mensaje",
               ent([sol(IdCliente=101, IdPortafolio=501)], [port(IdPortafolio=501, IdPortafolioReal=501, IdCliente=101, IdClienteReal=101,
                                                                IdEstadoCliente="BLQ", DocumentoIdentidad="22222222-2")], p_PermiteOperarProspectos="S"),
               DIC + CLIENTE2, colaboradores=productos((501, "ACT", "INFM", "0")))
    yield caso("REQ-02-03_prospecto-no-permitido", S, "FM-REQ-02: prospecto sin permiso => CLIENTE_BLOQUEADO",
               ent([sol(IdCliente=101, IdPortafolio=501)], [port(IdPortafolio=501, IdPortafolioReal=501, IdCliente=101, IdClienteReal=101,
                                                                IdEstadoCliente="BLQ", DocumentoIdentidad="22222222-2")]),
               DIC + CLIENTE2, colaboradores=productos((501, "ACT", "INFM", "0")))
    yield caso("REQ-02-04_portafolio-no-habilitado", S, "FM-REQ-02: estado portafolio != ACT => PORTAFOLIO_NO_HABILITADO",
               ent([sol()], [port(IdEstadoPortafolio="INA")]), DIC)
    yield caso("REQ-02-05_cliente-real-nulo", S, "FM-REQ-02: IdClienteReal/IdPortafolioReal nulos => CLIENTE_NO_EXISTE y PORTAFOLIO_NO_EXISTE (alcanzables si p_Portafolios los trae nulos)",
               ent([sol()], [port(IdClienteReal=None, IdPortafolioReal=None)]), DIC)
    yield caso("REQ-03-01_destino-invalido", S, "FM-REQ-03: cliente destino = cliente real, destino bloqueado, portafolio destino no existe/invalido",
               ent([sol()], [port(IdClienteRealDestino=100, IdEstadoClienteDestino="BLQ", IdPortafolioRealDestino=500)]), DIC)
    yield caso("REQ-03-02_destino-inexistente", S, "FM-REQ-03: destino nulo => CLIENTE_DESTINO_NO_EXISTE y PORTAFOLIO_DESTINO_NO_EXISTE",
               ent([sol()], [port(IdClienteRealDestino=None, IdPortafolioRealDestino=None, IdEstadoClienteDestino="ACT")]), DIC)
    yield caso("REQ-04-01_serie-vacia-y-no-vigente", S, "FM-REQ-04: IdSerieFondo '' => INSTRUMENTO_NO_EXISTE; IdEstadoSerie != ACT => NO_VIGENTE",
               ent([sol(IdSerieFondo="", IdEstadoSerie="INA")]), DIC)
    yield caso("REQ-05-01_tipo-movimiento-vacio", S, "FM-REQ-05: IdTipoMovimientoFondoMutuo '' => TIPO_OPERACION_NO_VALIDA_REQ_APO_RES",
               ent([sol(IdTipoMovimientoFondoMutuo="")]), DIC)
    yield caso("REQ-06-01_fondo-cerrado-advertencia", S, "FM-REQ-06: EstadoFondo != A => ADVERTENCIA (no rechaza)", ent([sol(EstadoFondo="C")]), DIC)
    yield caso("REQ-07-01_fondo-bloqueado-e-invalido", S, "FM-REQ-07: EstadoBloqueo != N y EsFondoValido != S", ent([sol(EstadoBloqueo="S", EsFondoValido="N")]), DIC)
    yield caso("REQ-08-01_serie-restringida", S, "FM-REQ-08: RestringeAporte != N con APORFM; RestringeRescate != N con RESCFM",
               ent([sol(RestringeAporte="S"), sol(IdInterno=2, RestringeRescate="S", IdTipoOperacion="RESCFM", IdTipoMovimientoFondoMutuo="RESNORM"),
                    sol(IdInterno=3, RestringeAporte="S", IdTipoOperacion="RESCFM")]), DIC)
    yield caso("REQ-09-01_fechas-invalidas", S, "FM-REQ-09 / D-REQ-2: fecha aporte invalida y fecha rescate invalida (usa el mapa de parametros equivocado)",
               ent([sol(EsFechaValidaAporte="N", EsFechaValidaRescate="N")]), DIC)
    yield caso("REQ-10-01_no-diferido", S, "FM-REQ-10: EsDiferido != S: error; exento con RESTRASP o glosa DESINVERSION APV",
               ent([sol(EsDiferido="N"), sol(IdInterno=2, EsDiferido="N", IdTipoMovimientoFondoMutuo="RESTRASP"),
                    sol(IdInterno=3, EsDiferido="N", Glosa="desinversion apv")]), DIC)
    yield caso("REQ-11-01_cambio-de-fondo", S, "FM-REQ-11: PermiteCambioFondo != S solo cuenta con RESREINV/APOREINV",
               ent([sol(PermiteCambioFondo="N", IdTipoMovimientoFondoMutuo="RESREINV"), sol(IdInterno=2, PermiteCambioFondo="N", IdTipoMovimientoFondoMutuo="APOREINV"),
                    sol(IdInterno=3, PermiteCambioFondo="N")]), DIC)
    yield caso("REQ-12-01_monto-maximo", S, "FM-REQ-12: EsMontoMaximo != S => MONTO_MAXIMO_REQ_APO_RES con serie, portafolio y documento", ent([sol(EsMontoMaximo="N")]), DIC)
    yield caso("REQ-13-01_monto-minimo-no-aplica-a-PAC", S, "FM-REQ-13: EsMontoMinimo != S => MONTO_MINIMO salvo ProcedenciaCanal PAC",
               ent([sol(EsMontoMinimo="N"), sol(IdInterno=2, EsMontoMinimo="N", ProcedenciaCanal="pac")]), DIC)
    yield caso("REQ-14-01_cumplimiento-ingreso-titulo", S, "FM-REQ-14: INGTIT con cliente bloqueado por cumplimiento (indicador BCCUM = S)",
               ent([sol(IdTipoMovimientoFondoMutuo="INGTIT")]), DIC + CUMPLIMIENTO)
    yield caso("REQ-15-01_overnight-sin-liquidez", S, "FM-REQ-15: Overnight=S y EstadoLiquidez != A => ADVERTENCIA FECHA_LIQUIDEZ_CERRADO",
               ent([sol(Overnight="S", EstadoLiquidez="C")]), DIC)
    yield caso("REQ-16-01_valor-monto-cuotas-no-positivos", S, "FM-REQ-16: ValorCuota, Monto y NumeroCuotas <= 0",
               ent([sol(ValorCuota=0, Monto=-1, NumeroCuotas=0)]), DIC)
    yield caso("REQ-01-01_reinversion-entre-familias", S, "FM-REQ-01: VALIDAR_REINVERSION_DISTINTAS_FAMILIAS=S: RESREINV con familia no equivalente",
               ent([sol(IdTipoMovimientoFondoMutuo="RESREINV", EsFamiliaEquivalente="N", PermiteCambioFondo="S")]), DIC + [set_param("VALIDAR_REINVERSION_DISTINTAS_FAMILIAS", "S")])
    yield caso("REQ-01-02_reinversion-sin-validar", S, "FM-REQ-01: parametro N (valor sembrado): no valida familias",
               ent([sol(IdTipoMovimientoFondoMutuo="RESREINV", EsFamiliaEquivalente="N")]), DIC)
    yield caso("REQ-01-03_parametro-ausente-es-S", S, "FM-REQ-01: parametro ausente => S",
               ent([sol(IdTipoMovimientoFondoMutuo="RESREINV", EsFamiliaEquivalente="N")]), DIC + [del_param("VALIDAR_REINVERSION_DISTINTAS_FAMILIAS")])
    yield caso("REQ-17-01_banderas-de-regimen", S, "FM-REQ-17: todas las banderas de regimen, mandato, proposito, cuenta y permisos en N",
               ent([sol(EsMoneyMarket="N", EsPortafolioRegimenValido="N", EsRegimenValido="N", PermiteRescateMandato="N", PermiteCambioGrupo="N",
                        EsPortafolioValido="N", EsCuentaInversionValido="N", PermiteAporte="N", PermiteTraspaso="N", PermiteRescate="N",
                        EsRegimenDestinoValido="N")]), DIC)
    yield caso("REQ-18-01_dedupe-por-descripcion", S, "FM-REQ-18 / D-REQ-1: dos solicitudes con el mismo texto sin parametros: solo queda el primero; con parametros distintos quedan ambos",
               ent([sol(IdInterno=1, EsMoneyMarket="N", Monto=0), sol(IdInterno=2, IdCliente=101, IdPortafolio=501, EsMoneyMarket="N", Monto=0, EsMontoMaximo="N"),
                    sol(IdInterno=3, EsMontoMaximo="N")],
                   [port(), port(IdInterno=2, IdPortafolio=501, IdPortafolioReal=501, IdCliente=101, IdClienteReal=101, DocumentoIdentidad="22222222-2"),
                    port(IdInterno=3)]),
               DIC + CLIENTE2, colaboradores=productos((500, "ACT", "INFM", "0"), (501, "ACT", "INFM", "0")))
    yield caso("REQ-19-01_producto-no-operable", S, "FM-REQ-19: sin detalle activo de producto INFM para el portafolio => PORTAFOLIO_NO_OPERA_PRODUCTO_SELECCIONADO (no se deduplica)",
               ent([sol(), sol(IdInterno=2)]), DIC, colaboradores=productos((500, "INA", "INFM", "0")))
    yield caso("REQ-19-02_producto-por-operacion", S, "FM-REQ-19: el detalle exige operacion concreta (RESCFM) y la solicitud es APORFM; otro detalle con 0 la habilita",
               ent([sol(), sol(IdInterno=2, IdPortafolio=500, IdTipoOperacion="RESCFM")]), DIC, colaboradores=productos((500, "ACT", "infm", "RESCFM")))
    yield caso("REQ-21-01_usuario-sin-empleado", S, "FM-REQ-21 / D-REQ-3: usuario sin IdEntidad de empleado => ERROR_AL_ENCONTRAR_ID_USUARIO por solicitud, con el usuario como empresa del diccionario",
               {"p_IdEmpresa": 1, "p_IdUsuario": 8, "p_SolicitudesAporteRescate": [sol(), sol(IdInterno=2)], "p_Portafolios": [port(), port(IdInterno=2)]},
               DIC + [fila("BSCL.Plataforma.BD::ACC.Usuario", IdUsuario=8, IdEmpresa=1, IdEstado="ACT", TipoEntidad="EMP", IdEntidad=None)])
    yield caso("REQ-21-02_usuario-inexistente", S, "FM-REQ-21: usuario sin fila de empleado: no hay mensaje",
               {"p_IdEmpresa": 1, "p_IdUsuario": 999, "p_SolicitudesAporteRescate": [sol()], "p_Portafolios": [port()]}, DIC)
    yield caso("REQ-02-06_portafolio-sin-par", S, "D-REQ-4: sin InPortafolio para el IdInterno => NullPointerException antes de validar",
               ent([sol(IdInterno=5)], [port(IdInterno=1)]), DIC)
    yield caso("REQ-02-07_cliente-sin-fila", S, "FM-REQ-04: el portafolio no existe en la base: sin fila de cliente => NullPointerException al armar el primer mensaje",
               ent([sol(IdPortafolio=999, IdEstadoSerie="INA")], [port(IdPortafolio=999, IdPortafolioReal=999)]), DIC)
    yield caso("REQ-02-08_interno-repetido-en-portafolios", S, "FM-REQ-02: dos InPortafolio con el mismo IdInterno => Exception key field is not unique",
               ent([sol()], [port(), port()]), DIC)
    yield caso("REQ-00-02_sin-solicitudes", S, "FM-REQ: sin solicitudes ni portafolios: sin mensajes (la consulta de productos igual se invoca)", ent([], []), DIC)
    yield caso("REQ-20-01_fondo-bloqueado-ordenado-por-hash", S, "FM-REQ-18: muchas solicitudes y textos distintos: el orden de salida es el del HashMap de descripciones",
               ent([sol(IdInterno=i, EsFondoValido="N", EstadoBloqueo="S", IdEstadoSerie="INA", EsMoneyMarket="N", EsPortafolioValido="N", PermiteAporte="N")
                    for i in range(1, 4)]), DIC)
