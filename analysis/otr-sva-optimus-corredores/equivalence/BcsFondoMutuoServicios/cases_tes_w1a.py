"""F2 W1 front 1 (PLAN_FONDOMUTUO_F2 5.2 / 7): Normal with the gate G1 LIFTED, ProcesarIngresoEgresoTesoreria REAL (legacy source as is) and Tesoreria's
CrearIngresoEgreso as the registered double (layer 1 of plan 7: the golden records the CrearIngresoEgreso_Entrada the legacy helper built).

    py -3 cases_tes_w1a.py           writes ONLY case.json + seed.sql of FMM-NOR-87..96; expected.json is RECORDED by legacy-oracle/record.sh FMM-NOR-8 / FMM-NOR-9

Pins FM-NOR-38 (selection of non-CIN contributions), FM-NOR-39 (use of the answer: ERROR message rejects the FIRST request D-NOR-5, RECH by reference,
IdIngresoEgreso stored), FM-NOR-90/91 and FM-TES-02..13 (movement and payment form, D-TES-1, D-TES-2: toMap / List2Hash duplicates).
The three objects the legacy helper reads that the oracle schema does not have are created in the CASE seed (no change of schema-fmm.sql, so no previous
golden changes): PAG.MedioDePagoYModalidadDePago with the REAL catalog types (hana_qa_results/cat_columnas_tablas.csv); the view
ConsultarPreferenciaPagoPortafolio and TES.PreferenciaPagoCorredor with ASSUMED types (no catalog row: the names and Java types of the legacy row classes).
Fake data only.
"""
from cases_nor_b import PRECIOS2
from cases_nor_lib import APORTE, SALDO_OK, caso, ins, sol, upd

PAG = '"BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago"'
VPP = '"BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio"'
PPC = '"BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor"'
USUARIO = '"BSCL.Plataforma.BD::ACC.Usuario"'

DDL = [
    "-- REAL types (cat_columnas_tablas.csv)",
    'CREATE TABLE %s ("IdMedioDePagoYModalidadDePago" BIGINT NOT NULL, "IdMedioDePago" NVARCHAR(3) NOT NULL, "IdModalidadDePago" NVARCHAR(6) NOT NULL,'
    ' "IdEstado" NVARCHAR(3) NOT NULL);' % PAG,
    "-- ASSUMED types (no catalog: legacy row class ProcesarIngresoEgresoTesoreria.PreferenciaPagoPortafolio)",
    'CREATE TABLE %s ("IdPreferenciaPagoPortafolio" BIGINT, "IdCuentaBancaria" BIGINT, "IdBancoCuentaBancaria" BIGINT, "NumeroCuentaBancaria" NVARCHAR(100),'
    ' "IdTipoCuentaBancaria" NVARCHAR(4), "NombreTipoCuentaBancaria" NVARCHAR(100));' % VPP,
    "-- ASSUMED types (no catalog: legacy row class ProcesarIngresoEgresoTesoreria.PreferenciaPagoCorredor + the filter columns of the in-line SQL)",
    'CREATE TABLE %s ("IdTipoMovimiento" NVARCHAR(6), "IdConcepto" NVARCHAR(6), "IdMedioDePago" NVARCHAR(3), "IdCuentaBanco" BIGINT, "IdMoneda" NVARCHAR(3),'
    ' "NombreEntidad" NVARCHAR(100), "IdEstado" NVARCHAR(3), "IdEmpresa" BIGINT);' % PPC,
]


def modalidad(i, medio, modo):
    return ins(PAG, {"IdMedioDePagoYModalidadDePago": i, "IdMedioDePago": medio, "IdModalidadDePago": modo, "IdEstado": "ACT"})


def pref_portafolio(i, cuenta, banco=12, numero="000123", tipo="CTE", nombre="Cuenta Corriente"):
    return ins(VPP, {"IdPreferenciaPagoPortafolio": i, "IdCuentaBancaria": cuenta, "IdBancoCuentaBancaria": banco, "NumeroCuentaBancaria": numero,
                     "IdTipoCuentaBancaria": tipo, "NombreTipoCuentaBancaria": nombre})


def pref_corredor(moneda, cuenta, concepto="RESCFM", entidad="FFM.RescatePorTraspaso", estado="ACT", empresa=1):
    return ins(PPC, {"IdTipoMovimiento": "MOVEGR", "IdConcepto": concepto, "IdMedioDePago": "TRA", "IdCuentaBanco": cuenta, "IdMoneda": moneda,
                     "NombreEntidad": entidad, "IdEstado": estado, "IdEmpresa": empresa})


BASE = DDL + [modalidad(1, "TRA", "MODDEP"), modalidad(2, "CHE", "MODCAJ"), pref_portafolio(9001, 401), pref_portafolio(9002, 402),
              pref_corredor("CLP", 301), pref_corredor("USD", 302), pref_corredor("CLP", 399, concepto="APORFM"),
              pref_corredor("CLP", 398, estado="INA"), pref_corredor("CLP", 397, empresa=2)]
TRA = sol(APORTE, IdMedioDePago="TRA", IdPreferenciaPagoPortafolio=9001)


def tes(defecto=None, respuestas=None):
    d = {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_MENSAJES": [], "P_OUT_RESULTADOPROCESO": [], "P_OUT_INGRESOEGRESOGENERADO": []}
    c = {"CrearIngresoEgreso": {"defecto": dict(d, **(defecto or {}))}}
    if respuestas:
        c["CrearIngresoEgreso"]["respuestas"] = respuestas
    return c


def generado(ref, ide):
    return {"NumeroReferencia": ref, "IdIngresoEgreso": ide, "IdTipoMovimiento": "MOVING"}


def generar():
    caso("FMM-NOR-87_tesoreria-aporte-tra-ingreso-generado",
         "F2 W1: FM-NOR-38/39/90/91, FM-TES-02..11: aporte TRA con preferencia de pago del portafolio 9001; Tesoreria devuelve IdIngresoEgreso 777 (se guarda)",
         [TRA], seed=BASE, colab=tes({"P_OUT_INGRESOEGRESOGENERADO": [generado(1, 777)]}))
    caso("FMM-NOR-88_tesoreria-mensaje-error-rechaza-la-primera",
         "F2 W1: FM-NOR-39 / D-NOR-5: dos aportes TRA; Tesoreria informa un ERROR de la solicitud 2: contador 1, mensaje 'TES : ', se rechaza la PRIMERA",
         [TRA, sol(TRA, IdInterno=2, Monto=200000, IdPreferenciaPagoPortafolio=9002)], seed=BASE,
         colab=dict(PRECIOS2, **tes({"P_OUT_ERRORESCONTADOR": 1, "P_OUT_MENSAJES": [{"id": 2, "tipo": "ERROR", "codigo": "ERR_TES", "descripcion": "falla de la solicitud 2"}]})))
    caso("FMM-NOR-89_tesoreria-rech-por-numero-referencia",
         "F2 W1: FM-NOR-39: dos aportes TRA; Tesoreria rechaza la 2 (RECH en NumeroReferencia) y genera el ingreso de la 1",
         [TRA, sol(TRA, IdInterno=2, Monto=200000)], seed=BASE,
         colab=dict(PRECIOS2, **tes({"P_OUT_RESULTADOPROCESO": [{"Campo": "NumeroReferencia", "Id": 2, "Servicio": "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso",
                                                           "Resultado": "RECH", "NumeroReferencia": 2}],
                              "P_OUT_INGRESOEGRESOGENERADO": [generado(1, 778)]})))
    caso("FMM-NOR-90_tesoreria-sin-empleado-ni-preferencias-modcaj",
         "F2 W1: FM-TES-02..05/10: usuario sin empleado (IdEmpleadoDigitador 0), sin modalidades (MODCAJ), preferencia 0 sin fila, sin preferencia del corredor",
         [sol(APORTE, IdMedioDePago="CHE")], seed=DDL + [upd(USUARIO, {"TipoEntidad": "CLI"}, '"IdUsuario" = 7')], colab=tes())
    caso("FMM-NOR-91_tesoreria-modalidad-repetida-toMap",
         "F2 W1: FM-TES-03 / D-TES-2: dos filas del medio TRA en PAG.MedioDePagoYModalidadDePago: Collectors.toMap lanza IllegalStateException",
         [TRA], seed=BASE + [modalidad(3, "TRA", "MODCAJ")], colab=tes())
    caso("FMM-NOR-92_tesoreria-preferencia-corredor-moneda-repetida",
         "F2 W1: FM-TES-05 / D-TES-2: dos preferencias del corredor activas en CLP: List2Hash lanza 'key field is not unique'",
         [TRA], seed=BASE + [pref_corredor("CLP", 303)], colab=tes())
    caso("FMM-NOR-93_tesoreria-simulacion-no-llama",
         "F2 W1: FM-NOR-39: simulacion (p_SimularOperacion=S) con aporte TRA: no se llama a Tesoreria",
         [TRA], seed=BASE, colab=tes(), p_SimularOperacion="S")
    caso("FMM-NOR-94_tesoreria-excepcion-se-propaga",
         "F2 W1: FM-TES-12: CrearIngresoEgreso lanza una excepcion: se propaga (500), el bloqueo se libera",
         [TRA], seed=BASE, colab={"CrearIngresoEgreso": {"respuestas": [{"error": "falla interna de tesoreria"}]}})
    caso("FMM-NOR-95_tesoreria-preferencia-portafolio-repetida",
         "F2 W1: FM-TES-04 / D-TES-2: dos filas de la preferencia 9001 en la vista: List2Hash lanza 'key field is not unique'",
         [TRA], seed=BASE + [pref_portafolio(9001, 499)], colab=tes())
    caso("FMM-NOR-96_tesoreria-aporte-tra-y-aporte-cin",
         "F2 W1: FM-NOR-38: un aporte TRA y uno CIN (otro monto) del mismo portafolio: solo el TRA va a Tesoreria",
         [TRA, sol(APORTE, IdInterno=2, Monto=200000)], seed=BASE,
         colab=dict(PRECIOS2, **dict(SALDO_OK, **tes({"P_OUT_INGRESOEGRESOGENERADO": [generado(1, 779)]}))))


if __name__ == "__main__":
    generar()
    print("FMM-NOR-87..96 inputs written (record the goldens with legacy-oracle/record.sh)")
