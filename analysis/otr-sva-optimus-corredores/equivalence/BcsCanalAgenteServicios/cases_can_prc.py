"""BcsCanalAgenteServicios oracle: inputs of the cases of the BD/Servicios procedure texts (family PRC). H2 cannot run a SQLScript procedure; what it can run is the SELECT that builds the output
table of ConsultarHistorialMovimientosCuentaInversion.sql (p_out_Resultado) and of ConsultaAvanzadaClientes.sql (p_out_Clientes), once the table variable it reads is a stand-in table the case seeds
(schema-canalagente-prc.sql). The text comes from the file (legacy/ when recording, the ported resource in the JUnit test). ConsultarHistorialMovimientosCustodia.sql reads the output table of a
Custodia procedure that is not part of this module: its text is only pinned by SHA-256 (no case)."""
from cases_can_lib import *  # noqa: F401,F403

S = "Procedimiento.seleccionFinal"
PRC = ["schema-canalagente-prc.sql"]

CI = {"procedimiento": "ConsultarHistorialMovimientosCuentaInversion.sql", "salida": "p_out_Resultado", "variableTabla": "v_out_CartolaDeMovimiento", "tablaH2": T_CART_IN,
      "orden": '"IdCargoAbonoMovimiento", "IdComprobante"'}
CL = {"procedimiento": "ConsultaAvanzadaClientes.sql", "salida": "p_out_Clientes", "variableTabla": "v_out_Clientes", "tablaH2": T_CLI_IN, "orden": '"IdCliente", "IdPortafolio"'}


def vista(id_, cliente_, codigo):
    return insertar(T_VISTA, IdPortafolio=id_, CodigoPortafolio=codigo, IdCliente=cliente_)


def car(id_, comprobante_, portafolio_, **extra):
    r = dict(IdCargoAbonoMovimiento=id_, NombreCliente="Cliente %d" % portafolio_, IdCliente=portafolio_ // 10, IdPortafolio=portafolio_, FechaSistema="2026-02-10", FechaLiquidacionOperacion="2026-02-12",
             IdMoneda="CLP", IdConcepto="ABN", NombreConcepto="Abono %d" % id_, Cargo=0, Abono=1500.5, IdEstado="VIG", NombreEstado="Vigente", NombreTipoComprobante="Comprobante",
             NombreTipoProducto="Cuenta", NombreTipoOperacion="Abono", NombreTipoMovimiento="Abono", SaldoSimulado=10000, IdComprobante=comprobante_, IdMatrizAgrupador=0, EsADC="N")
    r.update(extra)
    return insertar(T_CART_IN, **r)


def mundo_prc():
    return mundo() + [vista(100, 10, "PF-100"), vista(200, 20, "PF-200"), vista(300, 30, "PF-300"), vista(400, 40, "PF-400")]


def cli_in(cliente_, portafolio_, **extra):
    r = dict(DocumentoIdentidad="%d-K" % cliente_, NombreCompleto="Cliente %d" % cliente_, IdTipoCliente="PN", IdCliente=cliente_, IdPortafolio=portafolio_, IdTipoBloqueoPortafolio="BLQ",
             NombreTipoBloqueoPortafolio="Bloqueo %d" % cliente_, CodigoPortafolio="PF-%d" % portafolio_, SaldoFlujoCaja=12.5, IdMonedaFlujoCaja="CLP")
    r.update(extra)
    return insertar(T_CLI_IN, **r)


def generar():
    g = Gen("PRC")
    M = mundo_prc()
    g.caso("historial-un-movimiento-con-comprobante", S, CI, ["D-CAN-13", "B-CA-03"], seed=M + [car(1, 5001, 100)], esquemas_extra=PRC)
    g.caso("historial-sin-comprobante-deja-nulos-folio-dte-y-pdf", S, CI, ["D-CAN-13", "B-CA-04"], seed=M + [car(1, None, 100), car(2, 9999, 100)], esquemas_extra=PRC)
    g.caso("historial-portafolio-fuera-de-la-vista-deja-nulos-codigo-y-documento", S, CI, ["D-CAN-13", "B-CA-04"], seed=M + [car(1, 5001, 999)], esquemas_extra=PRC)
    g.caso("historial-cliente-sin-documento-deja-nulo-el-documento", S, CI, ["D-CAN-13", "B-CA-04"], seed=M + [car(1, 5001, 300), car(2, 5001, 400)], esquemas_extra=PRC)
    g.caso("historial-id-repetido-devuelve-una-fila-por-fila-de-la-cartola", S, CI, ["D-CAN-13", "D-CAN-06"], seed=M + [car(1, 5001, 100), car(1, 5002, 100)], esquemas_extra=PRC)
    g.caso("historial-el-folio-sale-numerico-y-no-como-texto", S, CI, ["D-CAN-13", "B-CA-10"], seed=M + [car(1, 5001, 100), car(2, 5004, 100), car(3, 5003, 100)], esquemas_extra=PRC)
    g.caso("historial-renombra-las-fechas-y-conserva-el-resto", S, CI, ["D-CAN-13", "B-CA-06"],
           seed=M + [car(1, 5001, 100, FechaSistema="2026-02-03", FechaLiquidacionOperacion="2026-02-05", Cargo=1234.5, Abono=0, SaldoSimulado=-50.25, IdMatrizAgrupador=7, EsADC="S")], esquemas_extra=PRC)
    g.caso("historial-cartola-vacia-devuelve-vacio", S, CI, ["D-CAN-13", "B-CA-08"], seed=M, esquemas_extra=PRC)
    g.caso("clientes-con-fila-de-cliente-agrega-idpersona", S, CL, ["D-CAN-15"], seed=M + [cli_in(10, 100), cli_in(20, 200)], esquemas_extra=PRC)
    g.caso("clientes-sin-fila-de-cliente-se-descartan-por-el-inner-join", S, CL, ["D-CAN-15"], seed=M + [cli_in(10, 100), cli_in(40, 400), cli_in(99, 990)], esquemas_extra=PRC)
    g.caso("clientes-renombra-el-bloqueo-del-portafolio", S, CL, ["D-CAN-15"], seed=M + [cli_in(10, 100, IdTipoBloqueoPortafolio="TMP", NombreTipoBloqueoPortafolio="Temporal")], esquemas_extra=PRC)
    g.caso("clientes-varios-portafolios-del-mismo-cliente-dan-una-fila-cada-uno", S, CL, ["D-CAN-15"], seed=M + [cli_in(10, 101), cli_in(10, 100), cli_in(10, 102)], esquemas_extra=PRC)
    g.caso("clientes-sin-filas-devuelve-vacio", S, CL, ["D-CAN-15"], seed=M, esquemas_extra=PRC)
    return g.ids
