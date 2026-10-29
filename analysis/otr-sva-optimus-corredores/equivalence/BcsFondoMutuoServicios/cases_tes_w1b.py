"""F2 W1B (layer 2 of PLAN_FONDOMUTUO_F2 7): Normal with G1 lifted, the legacy helper REAL and Tesoreria's legacy CrearIngresoEgreso REAL on the same
H2 session (record_w1b.sh). Ids FMM-NOR-Tnn (prefix FMM-NOR-T, so the layer-1 goldens are never re-recorded by record_w1b.sh).

    py -3 cases_tes_w1b.py           writes ONLY case.json + seed.sql; expected.json is RECORDED by legacy-oracle/record_w1b.sh FMM-NOR-T

Every case loads schema-tesoreria-fm.sql + seed/base-tesoreria-fm.sql ("esquemasExtra") and dumps, besides the FondoMutuo tables, Tesoreria's
movements, payment forms and the two global temporary tables. Plataforma RevisarRequisitosBasicos is real, AgregarLog / SaldoFlujoCaja are registered
doubles (as for Normal); the HANA procedure CrearComprobante is the registered double "CrearComprobante.procedimiento" (cl.bcs.oracle.TesoreriaH2).
"""
import json

from cases_nor_b import PRECIOS2
from cases_nor_cov import filas_batch
from cases_nor_lib import APORTE, CASES, SALDO_OK, TABLAS, ins, sol, upd

TES_TABLAS = [
    {"nombre": "BSCL.Tesoreria.BD::TES.IngresoEgreso", "orden": "\"IdIngresoEgreso\""},
    {"nombre": "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago", "orden": "\"IdIngresoEgresoFormaDePago\""},
    {"nombre": "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso", "orden": "\"NumeroReferencia\""},
    {"nombre": "BSCL.Tesoreria.BD::GBL.CrearIngresoEgresoFormaDePago_Proceso", "orden": "\"NumeroReferenciaFormaDePago\""},
]
EXTRA = ["schema-tesoreria-fm.sql", "seed/base-tesoreria-fm.sql"]
PROC = {"CrearComprobante.procedimiento": {"defecto": {"p_out_InformacionContador": 0, "p_out_AdvertenciasContador": 0, "p_out_ErroresContador": 0}}}
TRA = sol(APORTE, IdMedioDePago="TRA", IdPreferenciaPagoPortafolio=9001)


def caso(cid, ref, solicitudes, seed=None, colab=None, aprobada=None, **cab):
    entrada = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": "N", "p_UsaReglasNegocio": "S",
               "p_SolicitudesAporteRescate": solicitudes, "p_FormaPagoSpot": []}
    entrada.update(cab)
    c = dict(PROC)
    c.update(colab or {})
    doc = {"servicio": "CrearSolicitudAporteRescateNormal", "ref": ref, "tesoreriaReal": True, "esquemasExtra": EXTRA, "entrada": entrada,
           "tablas": TABLAS + TES_TABLAS, "colaboradores": c}
    if aprobada:
        doc["approvedDifference"] = aprobada
    d = CASES / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    texto = "-- case seed on top of seed/base-fmm.sql + schema-tesoreria-fm.sql + seed/base-tesoreria-fm.sql (fake data)\n" + ("\n".join(seed) if seed else "SELECT 1;") + "\n"
    (d / "seed.sql").write_text(texto, encoding="utf-8", newline="\n")


PPC = '"BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor"'
VPP = '"BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio"'
CONF = '"BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"'
PRECIOS3 = {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": [
    {"IdRegistro": i, "IdClasificacionTipoInstrumento": "FM", "IdTipoInstrumento": "FM", "IdInstrumento": 900, "Nemotecnico": "FM001A", "Precio": 1500.1234,
     "CodigoFuente": "OTR", "FechaFuente": "2026-03-02 00:00:00.000", "IdMoneda": "CLP"} for i in (1, 2, 3)]}}}
ERROR_LOG = {"AgregarLog": {"respuestas": [{"cuando": {"0.p_Log.0.IdTipoRegistro": "SOLICITUD_APORTE_RESCATE"}, "error": "falla del log de FondoMutuo"}], "defecto": {}}}


def dos(**kw):
    return [TRA, sol(TRA, IdInterno=2, Monto=200000, **kw)]


def generar():
    T = "F2 W1B: "
    caso("FMM-NOR-T01_tes-aporte-tra-ingreso-real", T + "aporte TRA; Tesoreria REAL crea el ingreso y su forma de pago; COMMIT conjunto", [TRA])
    caso("FMM-NOR-T02_tes-aporte-che-modcaj", T + "aporte con cheque (CHE, modalidad MODCAJ)", [sol(TRA, IdMedioDePago="CHE")])
    caso("FMM-NOR-T03_tes-dos-aportes-tra", T + "dos aportes TRA: dos ingresos, dos formas de pago; secuencias de Tesoreria pedidas una vez por nombre en el legacy", dos(),
         colab=PRECIOS2)
    caso("FMM-NOR-T04_tes-tres-aportes-tra-che-tra", T + "tres aportes (TRA, CHE, TRA)",
         [TRA, sol(TRA, IdInterno=2, Monto=200000, IdMedioDePago="CHE"), sol(TRA, IdInterno=3, Monto=300000)], colab=PRECIOS3)
    caso("FMM-NOR-T05_tes-aporte-tra-y-cin", T + "FM-NOR-38: un aporte TRA y uno CIN: solo el TRA va a Tesoreria",
         [TRA, sol(APORTE, IdInterno=2, Monto=200000)], colab=dict(PRECIOS2, **SALDO_OK))
    caso("FMM-NOR-T06_tes-rollback-conjunto-falla-el-log", T + "ROLLBACK CONJUNTO: Tesoreria escribio el ingreso y despues falla el AgregarLog de FondoMutuo: excepcion, 0 filas en TES",
         [TRA], colab=ERROR_LOG)
    caso("FMM-NOR-T07_tes-rollback-por-contador-segunda-rechazada", T + "ROLLBACK POR CONTADOR: Tesoreria acepta la 1 (la escribe) y rechaza la 2 (cuenta del socio inexistente): contador 1, se rechaza la PRIMERA (D-NOR-5)",
         dos(IdPreferenciaPagoPortafolio=9002), seed=[ins(VPP, {"IdPreferenciaPagoPortafolio": 9002, "IdCuentaBancaria": 499, "IdBancoCuentaBancaria": 1,
                                                                 "NumeroCuentaBancaria": "9", "IdTipoCuentaBancaria": "CTC", "NombreTipoCuentaBancaria": "x"})], colab=PRECIOS2)
    caso("FMM-NOR-T08_tes-procedimiento-comprobante-falla", T + "ROLLBACK POR CONTADOR tras escribir: el procedimiento CrearComprobante falla, Tesoreria lo traga (UI_ERROR_INGRESO_EGRESO)",
         [TRA], colab={"CrearComprobante.procedimiento": {"respuestas": [{"error": "falla del procedimiento"}]}})
    caso("FMM-NOR-T09_tes-procedimiento-informa-errores", T + "el procedimiento devuelve p_out_ErroresContador 1 (escalar que el legacy no lee)",
         [TRA], colab={"CrearComprobante.procedimiento": {"defecto": {"p_out_ErroresContador": 1}}})
    caso("FMM-NOR-T10_tes-socio-cuenta-inexistente", T + "cuenta del socio inexistente: Tesoreria rechaza", [sol(TRA, IdPreferenciaPagoPortafolio=9002)],
         seed=[ins(VPP, {"IdPreferenciaPagoPortafolio": 9002, "IdCuentaBancaria": 499, "IdBancoCuentaBancaria": 1, "NumeroCuentaBancaria": "9",
                         "IdTipoCuentaBancaria": "CTC", "NombreTipoCuentaBancaria": "x"})])
    caso("FMM-NOR-T11_tes-corredora-cuenta-inexistente", T + "cuenta de la corredora inexistente (preferencia del corredor a la cuenta 399)", [TRA],
         seed=[upd(PPC, {"IdCuentaBanco": 399})])
    caso("FMM-NOR-T12_tes-sin-preferencia-portafolio", T + "preferencia de pago del portafolio 0 (sin fila): cuenta del socio 0", [sol(TRA, IdPreferenciaPagoPortafolio=0)])
    caso("FMM-NOR-T13_tes-sin-preferencia-corredor", T + "sin preferencia del corredor: cuenta de la corredora 0", [TRA], seed=["DELETE FROM %s;" % PPC])
    caso("FMM-NOR-T14_tes-concepto-inexistente", T + "concepto APORFM inexistente en Tesoreria", [TRA],
         seed=['DELETE FROM "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" WHERE "IdConcepto" = \'APORFM\';'])
    caso("FMM-NOR-T15_tes-tipo-producto-inexistente", T + "tipo de producto INFM inexistente", [TRA],
         seed=['DELETE FROM "BSCL.Plataforma.BD::INS.TipoProducto" WHERE "IdTipoProducto" = \'INFM\';'])
    caso("FMM-NOR-T16_tes-tipo-operacion-inexistente", T + "tipo de operacion APORFM inexistente", [TRA],
         seed=['DELETE FROM "BSCL.Plataforma.BD::INS.TipoOperacion" WHERE "IdTipoOperacion" = \'APORFM\';'])
    caso("FMM-NOR-T17_tes-condicion-t0-inexistente", T + "condicion de liquidacion T0 inexistente", [TRA],
         seed=['DELETE FROM "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" WHERE "IdCondicionDeLiquidacion" = \'T0\';'])
    caso("FMM-NOR-T18_tes-moneda-no-registrada-en-tesoreria", T + "CLP sin registro en TES.MonedaTesoreria", [TRA],
         seed=['DELETE FROM "BSCL.Tesoreria.BD::TES.MonedaTesoreria" WHERE "IdMoneda" = \'CLP\';'])
    caso("FMM-NOR-T19_tes-persona-inactiva", T + "persona 1000 inactiva para Tesoreria", [TRA],
         seed=['UPDATE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaEstado" SET "IdEstado" = \'INA\' WHERE "IdPersona" = 1000;'])
    caso("FMM-NOR-T20_tes-cliente-bloqueado-cumplimiento", T + "cliente 100 bloqueado por cumplimiento (BCCUM = S)", [TRA],
         seed=[ins('"BSCL.Plataforma.BD::IND.TipoIndicador"', {"IdTipoIndicador": 1, "IdTipoEntidad": "CLIENTE", "CodigoTipoIndicador": "BCCUM", "IdEstado": "ACT"}),
               'UPDATE "BSCL.Plataforma.BD::IND.Indicador" SET "ValorIndicador" = \'S\' WHERE "IdEntidad" = 100 AND "IdTipoIndicador" = 1;'])
    caso("FMM-NOR-T21_tes-portafolio-bloqueado", T + "portafolio 500 bloqueado para Tesoreria (BLO)", [TRA],
         seed=['UPDATE "BSCL.Portafolio.BD::POR.Portafolio" SET "IdEstado" = \'BLO\' WHERE "IdPortafolio" = 500;'])
    caso("FMM-NOR-T22_tes-usuario-sin-empleado", T + "usuario 7 sin empleado: el helper manda IdEmpleadoDigitador 0 y Tesoreria no encuentra empleado", [TRA],
         seed=['DELETE FROM "BSCL.SocioNegocio.BD::EMP.Empleado" WHERE "IdEmpleado" = 70;'])
    caso("FMM-NOR-T23_tes-modalidad-sin-nombre", T + "medio TRA con una modalidad que no existe en PAG.ModalidadDePago", [TRA],
         seed=['UPDATE "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" SET "IdModalidadDePago" = \'MODXXX\' WHERE "IdMedioDePago" = \'TRA\';'])
    caso("FMM-NOR-T24_tes-sin-parametro-separacomprobante", T + "sin el parametro TESORERIA SEPARACOMPROBANTECOMSPT", [TRA],
         seed=["DELETE FROM %s WHERE \"CodigoParametro\" = 'SEPARACOMPROBANTECOMSPT';" % CONF])
    caso("FMM-NOR-T25_tes-simulacion-no-escribe", T + "simulacion: Tesoreria no se llama, nada escrito", [TRA], p_SimularOperacion="S")
    caso("FMM-NOR-T26_tes-aporte-monto-con-decimales", T + "aporte TRA de 123456.78 (monto de pago con decimales hacia Tesoreria)", [sol(TRA, Monto=123456.78)])
    caso("FMM-NOR-T27_tes-dos-aportes-che-sin-modalidad", T + "dos aportes, la 2 con CHE sin fila en PAG.MedioDePagoYModalidadDePago: el helper manda MODCAJ por defecto",
         dos(IdMedioDePago="CHE"), seed=['DELETE FROM "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" WHERE "IdMedioDePago" = \'CHE\';'], colab=PRECIOS2)
    caso("FMM-NOR-T28_tes-solicitud-batch", T + "aporte TRA con SolicitudBatch = S (sin AGF; filas BATCH de la configuracion de fechas, como FMM-NOR-62)",
         [sol(TRA, SolicitudBatch="S")], seed=filas_batch())
    caso("FMM-NOR-T29_tes-concepto-genera-comprobante-automatico", T + "concepto APORFM con GeneraComprobanteAutomaticamente = S", [TRA],
         seed=['UPDATE "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" SET "GeneraComprobanteAutomaticamente" = \'S\' WHERE "IdConcepto" = \'APORFM\';'])
    caso("FMM-NOR-T30_tes-rollback-conjunto-dos-aportes-falla-el-log", T + "ROLLBACK CONJUNTO con dos ingresos escritos: falla el AgregarLog de FondoMutuo", dos(),
         colab=dict(PRECIOS2, **ERROR_LOG))
    caso("FMM-NOR-T31_tes-moneda-inactiva", T + "CLP inactiva en PLA.Moneda para Tesoreria", [TRA],
         seed=['UPDATE "BSCL.Plataforma.BD::PLA.Moneda" SET "IdEstado" = \'INA\' WHERE "IdMoneda" = \'CLP\';'])
    caso("FMM-NOR-T32_tes-corredor-preferencia-otra-empresa", T + "la preferencia del corredor es de otra empresa (filtro IdEmpresa): cuenta 0", [TRA],
         seed=[upd(PPC, {"IdEmpresa": 2})])
    caso("FMM-NOR-T33_tes-dev-f5-agregarlog-de-tesoreria-falla", T + "DEV-F5 dentro de Tesoreria: el AgregarLog que llama Tesoreria falla; el legacy lo traga (200 con UI_ERROR_INGRESO_EGRESO, ROLLBACK por contador); el modulo responde 503 con rollback completo",
         [TRA], colab={"AgregarLog": {"respuestas": [{"cuando": {"0.p_Log.0.IdTipoRegistro": "INGRESO_TESORERIA"}, "error": "falla del log de Tesoreria"}], "defecto": {}}},
         aprobada={"dev": "DEV-F5", "decisionServlet": "ROLLBACK_503",
                   "procedencia": "DEV-F5 en Tesoreria, aprobada por el usuario (Mauricio Villagran Mora) el 2026-10-24: la falla HTTP de un colaborador de Tesoreria responde 503/500 con rollback completo en vez de confirmarse (PLAN_FONDOMUTUO_F2.md seccion 17)"})


if __name__ == "__main__":
    generar()
    print("FMM-NOR-T inputs written (record the goldens with legacy-oracle/record_w1b.sh)")
