"""BcsCanalAgenteServicios oracle: inputs of the cases of ConsultarHistorialMovimientosCuentaInversion.logica (family LOG)."""
from cases_can_lib import *  # noqa: F401,F403

L = "ConsultarHistorial.logica"
TP = [dump_proc()]


def base(movs, **kw):
    return cartola(resp(movs, **kw))


def generar():
    g = Gen("LOG")
    M = mundo()

    # ------------------------------------------------------------------------------------------------------------------------------ happy path and enrichment
    g.caso("un-movimiento-con-comprobante-completo", L, entrada_h(), ["B-CA-01", "B-CA-02", "B-CA-03", "B-CA-06", "B-CA-08", "B-CA-09", "B-CA-10", "B-CA-13", "D-CAN-02", "D-CAN-03"],
           base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("tres-movimientos-conservan-el-orden-de-la-cartola", L, entrada_h(), ["B-CA-03", "B-CA-07", "B-CA-12"],
           base([mov(30, 5002), mov(10, 5001), mov(20, 5004)]), tablas=TP, seed=M)
    g.caso("movimiento-sin-comprobante-deja-nulos-folio-dte-y-pdf", L, entrada_h(), ["B-CA-04", "D-CAN-07"], base([mov(1, None)]), tablas=TP, seed=M)
    g.caso("comprobante-inexistente-deja-nulos-folio-dte-y-pdf", L, entrada_h(), ["B-CA-04"], base([mov(1, 9999)]), tablas=TP, seed=M)
    g.caso("comprobante-con-dte-y-pdf-propios", L, entrada_h(), ["B-CA-03", "B-CA-10"], base([mov(1, 5003)]), tablas=TP, seed=M)
    g.caso("folio-cero-se-convierte-en-texto-0", L, entrada_h(), ["B-CA-10"], base([mov(1, 5004)]), tablas=TP, seed=M)
    g.caso("comprobante-sin-pdf-ni-dte-usa-el-cero-de-la-tabla", L, entrada_h(), ["B-CA-03", "B-CA-10"], base([mov(1, 5002)]), tablas=TP, seed=M)
    g.caso("el-folio-de-la-cartola-se-ignora", L, entrada_h(), ["B-CA-10"], base([mov(1, 5001, FolioComprobante="CARTOLA-123"), mov(2, None, FolioComprobante="CARTOLA-456")]), tablas=TP, seed=M)
    g.caso("el-dte-de-la-cartola-se-ignora", L, entrada_h(), ["B-CA-03"], base([mov(1, 5001, IdRespuestaDTE=555), mov(2, None, IdRespuestaDTE=666)]), tablas=TP, seed=M)
    g.caso("portafolio-de-la-cartola-desconocido-deja-nulos-codigo-y-documento", L, entrada_h(), ["B-CA-04"], base([mov(1, 5001, portafolio=999)]), tablas=TP, seed=M)
    g.caso("cliente-sin-documento-deja-nulo-el-documento", L, entrada_h(300), ["B-CA-04"], base([mov(1, 5001, portafolio=300)]), tablas=TP, seed=M)
    g.caso("portafolio-sin-fila-de-cliente-deja-nulo-el-documento", L, entrada_h(400), ["B-CA-04"], base([mov(1, 5001, portafolio=400)]), tablas=TP, seed=M)
    g.caso("cada-fila-toma-el-portafolio-de-su-propio-movimiento", L, entrada_h(), ["B-CA-03", "B-CA-04"],
           base([mov(1, 5001, portafolio=100), mov(2, 5002, portafolio=200), mov(3, None, portafolio=300)]), tablas=TP, seed=M)
    g.caso("dos-movimientos-comparten-el-mismo-comprobante", L, entrada_h(), ["B-CA-03", "B-CA-12"], base([mov(1, 5001), mov(2, 5001)]), tablas=TP, seed=M)
    g.caso("mapeo-de-fechas-y-montos-de-la-cartola", L, entrada_h(), ["B-CA-06"],
           base([mov(1, 5001, FechaSistema="2026-02-03", FechaLiquidacionOperacion="2026-02-05", FechaEfectivaMovimiento="2026-02-04", Cargo=1234.5, Abono=0, SaldoSimulado=-50.25)]), tablas=TP, seed=M)
    g.caso("montos-con-ocho-decimales-pasan-sin-redondeo", L, entrada_h(), ["B-CA-06"],
           base([mov(1, 5001, Cargo=0.12345678, Abono=12345.12345678, SaldoSimulado=0.00000001)]), tablas=TP, seed=M)
    g.caso("campos-sin-valor-de-la-cartola-salen-nulos", L, entrada_h(), ["B-CA-06"],
           base([{"IdCargoAbonoMovimiento": 1, "IdComprobante": 5001, "IdPortafolio": 100}]), tablas=TP, seed=M)
    g.caso("matriz-y-adc-de-la-fila-de-cartola-vuelven-en-el-movimiento", L, entrada_h(), ["B-CA-05"],
           base([mov(1, 5001, IdMatrizAgrupador=7, EsADC="S"), mov(2, 5002, IdMatrizAgrupador=None, EsADC=None)]), tablas=TP, seed=M)
    g.caso("veinticinco-movimientos-en-un-solo-lote", L, entrada_h(), ["B-CA-03", "B-CA-07"],
           base([mov(1000 - i, 5001 if i % 2 else 5002) for i in range(25)]), tablas=TP, seed=M)

    # ------------------------------------------------------------------------------------------------------------------------------ delegation to the cartola
    g.caso("la-entrada-completa-se-delega-con-reglas-s-y-saldo-por-fecha-n", L, entrada_h(), ["B-CA-02", "D-CAN-11"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("matriz-y-adc-opcionales-ausentes-viajan-nulos", L, entrada_h(), ["B-CA-05", "B-CA-02"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("matriz-y-adc-presentes-viajan-a-la-cartola", L, entrada_h(p_IdMatrizAgrupador=7, p_EsADC="S"), ["B-CA-05", "B-CA-02"], base([mov(1, 5001, IdMatrizAgrupador=7, EsADC="S")]), tablas=TP, seed=M)
    g.caso("matriz-y-adc-nulos-explicitos", L, entrada_h(p_IdMatrizAgrupador=None, p_EsADC=None), ["B-CA-05"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("matriz-cero-y-adc-n", L, entrada_h(p_IdMatrizAgrupador=0, p_EsADC="N"), ["B-CA-05"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("el-cliente-encontrado-se-envia-a-la-cartola", L, entrada_h(200), ["B-CA-01", "B-CA-02"], base([mov(1, 5001, portafolio=200)]), tablas=TP, seed=M)
    g.caso("moneda-nula-y-fechas-nulas-pasan-tal-cual", L, {"p_IdEmpresa": 1, "p_IdUsuario": 1646, "p_IdModulo": "CTA", "p_IdPortafolio": 100, "p_FechaDesde": None, "p_FechaHasta": None, "p_IdMoneda": None},
           ["B-CA-02", "D-CAN-09"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("empresa-usuario-y-modulo-nulos-pasan-tal-cual", L, {"p_IdPortafolio": 100, "p_FechaDesde": "2026-02-01", "p_FechaHasta": "2026-02-28", "p_IdMoneda": "USD"}, ["B-CA-02", "D-CAN-09"],
           base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("fechas-invertidas-y-formato-libre-pasan-sin-validar", L, entrada_h(p_FechaDesde="28/02/2026", p_FechaHasta="2026-01-01 99:99"), ["D-CAN-09"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("moneda-usd-y-modulo-distinto", L, entrada_h(p_IdMoneda="USD", p_IdModulo="PLA"), ["B-CA-02"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("la-cartola-no-recibe-estado-ni-cuenta-de-inversion", L, entrada_h(), ["B-CA-02", "D-CAN-11"], base([]), tablas=TP, seed=M)

    # ------------------------------------------------------------------------------------------------------------------------------ client not found
    g.caso("portafolio-inexistente-lanza-nullpointer-y-no-llama-a-la-cartola", L, entrada_h(999), ["D-CAN-01", "B-CA-01"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("portafolio-nulo-lanza-nullpointer", L, entrada_h(None), ["D-CAN-01", "D-CAN-09"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("portafolio-cero-lanza-nullpointer", L, entrada_h(0), ["D-CAN-01"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("base-sin-ningun-portafolio-lanza-nullpointer", L, entrada_h(100), ["D-CAN-01"], base([mov(1, 5001)]), tablas=TP, seed=[])
    g.caso("portafolio-con-cliente-sin-fila-de-cliente-igual-delega", L, entrada_h(400), ["B-CA-01", "B-CA-02"], base([mov(1, 5001, portafolio=400)]), tablas=TP, seed=M)

    # ------------------------------------------------------------------------------------------------------------------------------ errors and warnings of the cartola
    g.caso("errores-de-la-cartola-con-lista-vacia-no-se-propagan", L, entrada_h(), ["D-CAN-04", "B-CA-08"],
           base([], contador=2, errores=[{"Codigo": "ERR01", "Descripcion": "Requisito basico"}, {"Codigo": "ERR02", "Descripcion": "Otro"}]), tablas=TP, seed=M)
    g.caso("errores-de-la-cartola-con-movimientos-igual-los-devuelve", L, entrada_h(), ["D-CAN-04", "B-CA-03"],
           base([mov(1, 5001)], contador=1, errores=[{"Codigo": "ERR01", "Descripcion": "Fallo parcial"}]), tablas=TP, seed=M)
    g.caso("mensajes-de-error-de-requisitos-basicos-no-se-propagan", L, entrada_h(), ["D-CAN-04"],
           base([], contador=None, mensajes=[{"id": 1, "tipo": "ERROR", "codigo": "REQ01", "descripcion": "Usuario sin permiso"}]), tablas=TP, seed=M)
    g.caso("mensajes-de-advertencia-no-se-propagan", L, entrada_h(), ["D-CAN-04", "D-CAN-02"],
           base([mov(1, 5001)], mensajes=[{"id": 1, "tipo": "ADVERTENCIA", "codigo": "ADV01", "descripcion": "Saldo negativo"}]), tablas=TP, seed=M)
    g.caso("cartola-sin-movimientos-lote-vacio-y-contador-cero", L, entrada_h(), ["B-CA-08", "B-CA-09", "B-CA-03"], base([]), tablas=TP, seed=M)
    g.caso("saldos-y-advertencias-quedan-nulos-aunque-haya-movimientos", L, entrada_h(), ["D-CAN-02", "D-CAN-03"], base([mov(1, 5001, SaldoSimulado=5000)]), tablas=TP, seed=M)

    # ------------------------------------------------------------------------------------------------------------------------------ defective answers of the cartola
    g.caso("cartola-con-lista-nula-lanza-sqlexception-sin-tocar-la-tabla", L, entrada_h(), ["D-CAN-08"], cartola({"P_OUT_CARTOLADEMOVIMIENTO": None, "P_OUT_ERRORESCONTADOR": 0}), tablas=TP, seed=M)
    g.caso("cartola-con-un-elemento-nulo-lanza-map-object-failed", L, entrada_h(), ["D-CAN-08"], base([mov(1, 5001), None]), tablas=TP, seed=M)
    g.caso("cartola-que-devuelve-nulo-lanza-nullpointer", L, entrada_h(), ["D-CAN-08"], {"ConsultarCartolaDeMovimiento.logica": {"defecto": None}}, tablas=TP, seed=M)
    g.caso("cartola-lanza-error-se-propaga-sin-tocar-la-tabla", L, entrada_h(), ["B-CA-02", "D-CAN-05"], cartola(error="Error en la cartola"), tablas=TP, seed=M)
    g.caso("movimiento-con-id-nulo-se-inserta-y-sale", L, entrada_h(), ["B-CA-12", "D-CAN-06"], base([mov(None, 5001), mov(2, 5002)]), tablas=TP, seed=M)

    # ------------------------------------------------------------------------------------------------------------------------------ duplicates and rows of other calls
    g.caso("id-de-movimiento-repetido-ambos-reciben-el-ultimo-comprobante", L, entrada_h(), ["D-CAN-06", "B-CA-12"], base([mov(1, 5001), mov(1, 5002)]), tablas=TP, seed=M)
    g.caso("id-repetido-con-el-mismo-comprobante", L, entrada_h(), ["D-CAN-06"], base([mov(1, 5001), mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("filas-ajenas-en-la-tabla-de-trabajo-se-mezclan-y-el-delete-las-borra", L, entrada_h(), ["D-CAN-12", "B-CA-09"], base([mov(1, 5001)]), tablas=TP,
           seed=M + [insertar(T_PROC, IdCargoAbonoMovimiento=9001, IdPortafolio=200, IdComprobante=5002)])
    g.caso("fila-ajena-con-el-mismo-id-gana-la-ultima-que-devuelve-el-select", L, entrada_h(), ["D-CAN-12", "D-CAN-06"], base([mov(1, 5001)]), tablas=TP,
           seed=M + [insertar(T_PROC, IdCargoAbonoMovimiento=1, IdPortafolio=200, IdComprobante=5002)])

    # ------------------------------------------------------------------------------------------------------------------------------ failures of the database
    g.caso("falla-el-select-del-cliente", L, entrada_h(), ["B-CA-01", "D-CAN-05"], base([mov(1, 5001)]), tablas=TP, seed=M, fallos=[fallo("SELECT TOP 1", "tabla de portafolio no disponible")])
    g.caso("falla-la-insercion-en-la-tabla-de-trabajo", L, entrada_h(), ["B-CA-03", "D-CAN-05"], base([mov(1, 5001)]), tablas=TP, seed=M, fallos=[fallo("INSERT INTO", "no se puede insertar")])
    g.caso("falla-el-select-de-comprobantes-deja-las-filas-en-la-tabla", L, entrada_h(), ["B-CA-03", "D-CAN-05"], base([mov(1, 5001), mov(2, 5002)]), tablas=TP, seed=M,
           fallos=[fallo("TO_VARCHAR", "no se puede leer el comprobante")])
    g.caso("falla-el-delete-final-pierde-el-resultado-y-deja-las-filas", L, entrada_h(), ["B-CA-09", "D-CAN-05"], base([mov(1, 5001)]), tablas=TP, seed=M, fallos=[fallo("DELETE FROM", "no se puede borrar")])
    g.caso("falla-la-insercion-con-filas-ajenas-conserva-solo-las-ajenas", L, entrada_h(), ["D-CAN-05", "D-CAN-12"], base([mov(1, 5001)]), tablas=TP,
           seed=M + [insertar(T_PROC, IdCargoAbonoMovimiento=9001, IdPortafolio=200, IdComprobante=5002)], fallos=[fallo("INSERT INTO", "no se puede insertar")])
    g.caso("falla-el-select-con-filas-ajenas-deja-ajenas-y-propias", L, entrada_h(), ["D-CAN-05", "D-CAN-12"], base([mov(1, 5001)]), tablas=TP,
           seed=M + [insertar(T_PROC, IdCargoAbonoMovimiento=9001, IdPortafolio=200, IdComprobante=5002)], fallos=[fallo("TO_VARCHAR", "no se puede leer el comprobante")])
    return g.ids
