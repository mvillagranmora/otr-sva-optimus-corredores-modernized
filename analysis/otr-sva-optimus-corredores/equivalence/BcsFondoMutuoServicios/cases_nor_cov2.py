"""Step 5 coverage cases of Normal, second batch (FMM-NOR-65..): commissions, back-office aporte, limits, regimes, configuration gaps, OWP variants.
INPUTS only; goldens are RECORDED by legacy-oracle/record.sh from the unmodified legacy. All seeds only add/update rows of the fictitious H2 universe.
Each case names the branch family of the NEW Normal that the 4A matrix left uncovered (tools5/lines-missed-5.txt of the first step-5 coverage run).
"""
import cases3a_lib as L3
import cases_nor_lib as L
from cases_nor_b import PRECIOS2
from cases_nor_cov import VISTA, fila_vista, filas_batch
from cases_nor_lib import APORTE, RESCATE, SALDO_OK, caso, ins, param, sol, upd

PROP = '"BSCL.FondoMutuo.BD::FMM.ConfiguracionPropositoPortafolio"'
IND = '"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"'
TIPO_NN = ins('"BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo"', {"IdTipoMovimientoFondoMutuo": "RESCNN", "Descripcion": "RESCNN", "IndicadorAporteRescate": "RES",
              "IdTipoOperacion": "RESCFM", "TieneCarga": "", "TieneIngresoManual": "", "TieneCertificacion": "", "IdEstado": "ACT", "GeneraOmnibus": ""})
FLUJO = {"ConsultarFlujoCustodia": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_FlujoDetallado": [
    {"IdCliente": 100, "IdPortafolio": 500, "IdInstrumento": 900, "Cantidad": 100, "PrecioCompra": 1500, "IdConcepto": "APORFM",
     "FechaOrigenCompra": "2026-02-10 00:00:00.000", "FechaOperacion": "2026-02-09 00:00:00.000"}],
    "p_out_FlujoTotalizado": [{"IdCliente": 100, "IdPortafolio": 500, "IdInstrumento": 900, "IdRegimenFondoMutuo": "GEN", "Cantidad": 1000,
                               "MontoAMercadoMonedaInstrumento": 1500123}]}}}


def tramos(tipo, pct, calc="N"):
    return [param("CALCULAR_COMISION_CERTIFICACION", calc)] + L3.tramo(10, "FM001A", tipo, 0, 9999, pct) + [L3.comision_maxima("FM001A")]


def generar():
    R, A, S = RESCATE, APORTE, L.SERIE
    caso("FMM-NOR-65_aporte-con-comision-de-entrada", "step 5 cobertura: aporte CIN con tramo ENTRADA 1.5 % (NormalArmado: comision no trimestral)", [sol(A)], seed=tramos("ENTRADA", 1.5), colab=SALDO_OK)
    caso("FMM-NOR-66_rescate-con-comision-de-salida", "step 5 cobertura: rescate con tramo SALIDA 2 % (monto de pago menos comision)", [sol(R)], seed=tramos("SALIDA", 2.0))
    caso("FMM-NOR-67_aporte-con-comision-trimestral", "step 5 cobertura: aporte con tramo PER (trimestral): NormalArmado:93-94, sin rescate NN", [sol(A)], seed=tramos("PER", 1.2, "S"), colab=SALDO_OK)
    caso("FMM-NOR-68_rescate-comision-trimestral-en-cero", "step 5 cobertura: rescate con tramo PER de 0 %: no se genera rescate NN (NormalCierre:42)", [sol(R)], seed=tramos("PER", 0.0, "S"),
         colab=FLUJO)
    caso("FMM-NOR-69_comision-nn-rechazada-por-bloqueo-de-aporte", "step 5 cobertura: el rescate NN se rechaza en su validacion (Fondo.BloqueoAporte=S): resultado vacio (NormalCierre:70)",
         [sol(R, NumeroCuotas=10)], seed=tramos("PER", 1.2, "S") + [TIPO_NN, upd(L.FONDO, {"BloqueoAporte": "S"})] + filas_batch(), colab=FLUJO)
    bo = [ins('"BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"', {"Key": "PERFIL_BACKOFFICE", "Value": "6"}),
          ins('"BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario"', {"IdPerfil": 6, "IdUsuario": 8}),
          ins('"BSCL.Plataforma.BD::ACC.Usuario"', {"IdUsuario": 8, "IdEmpresa": 1, "IdEstado": "ACT", "TipoEntidad": "EMP", "IdEntidad": 70})]
    caso("FMM-NOR-70_aporte-backoffice", "step 5 cobertura: aporte de un usuario de back office (estado de aporte de back office)", [sol(A)], p_IdUsuario=8,
         seed=bo + [upd(L.FONDO, {"EstadoAporteComercial": "C"})], colab=SALDO_OK)
    caso("FMM-NOR-71_montos-dentro-de-minimo-y-maximo", "step 5 cobertura: MontoMinimo 50000 y MontoMaximo 1000000 con aporte de 100000 (ambos fijados y cumplidos)", [sol(A)],
         seed=[upd(S, {"MontoMinimo": 50000, "MontoMaximo": 1000000})], colab=SALDO_OK)
    caso("FMM-NOR-72_regimen-no-valida-monto-minimo", "step 5 cobertura: regimen con ValidaMontoMinimoInstrumento=N y MontoMinimo 500000 (no se valida)", [sol(A)],
         seed=[upd(S, {"MontoMinimo": 500000}), upd(L.REGIMEN, {"ValidaMontoMinimoInstrumento": "N"})], colab=SALDO_OK)
    caso("FMM-NOR-73_ignora-monto-minimo", "step 5 cobertura: IgnorarMontoMinimo=S con MontoMinimo 500000 y aporte de 100000", [sol(A, IgnorarMontoMinimo="S")],
         seed=[upd(S, {"MontoMinimo": 500000})], colab=SALDO_OK)
    caso("FMM-NOR-74_indicador-cuenta-inversion-en-n", "step 5 cobertura: indicador GEN_OCCINV=N del portafolio y aporte con medio CIN", [sol(A)],
         seed=[upd(IND, {"ValorIndicador": "N"}, "\"IdPortafolio\" = 500")], colab=SALDO_OK)
    caso("FMM-NOR-75_aporte-sin-configuracion-de-proposito", "step 5 cobertura: sin ConfiguracionPropositoPortafolio: el aporte no encuentra la relacion regimen-proposito",
         [sol(A)], seed=["DELETE FROM %s;" % PROP], colab=SALDO_OK)
    caso("FMM-NOR-76_rescate-total-sin-configuracion-de-proposito", "step 5 cobertura: sin ConfiguracionPropositoPortafolio y rescate total (T)",
         [sol(R, CalculoPorMontoCuota="T", NumeroCuotas=1000)], seed=["DELETE FROM %s;" % PROP])
    caso("FMM-NOR-77_aporte-usd-no-diferido", "step 5 cobertura: aporte en USD pagado en USD en una serie no diferida (FONDO_NO_DIFERIDO)", [sol(A, IdMoneda="USD", IdMonedaPago="USD", Monto=100)],
         colab=SALDO_OK)
    caso("FMM-NOR-78_rescate-regimen-solo-traspaso", "step 5 cobertura: regimen con PermiteRescate=N y PermiteTraspaso=S, rescate con EsTraspaso=TRA (traspaso APV)",
         [sol(R, EsTraspaso="TRA", FolioContrato=700)],
         seed=[upd(L.REGIMEN, {"PermiteRescate": "N", "PermiteTraspaso": "S"}), ins('"BSCL.APV.BD::TRA.Traspaso"', {"IdTraspaso": 700, "RazonRetiro": "TRA", "IdPersona": 1000})])
    caso("FMM-NOR-79_monto-minimo-total-de-inversion", "step 5 cobertura: MontoMinimoTotalInversion=150000 con aporte de 100000 (ValidaMontoInversion rechaza)", [sol(A)],
         seed=[param("MontoMinimoTotalInversion", "150000")], colab=SALDO_OK)
    sin_pago = {k: v for k, v in R.items() if k != "IdMonedaPago"}
    caso("FMM-NOR-80_rescate-sin-moneda-de-pago", "step 5 cobertura: IdMonedaPago ausente (null) en el rescate", [sin_pago])
    caso("FMM-NOR-81_canal-y-relacionado-informados", "step 5 cobertura: Canal=WEB y IdSolicitudAporteRescateRelacionado=12345 informados por la solicitud",
         [sol(R, Canal="WEB", IdSolicitudAporteRescateRelacionado=12345)])
    caso("FMM-NOR-82_rescate-clp-con-spot-dolar-activo", "step 5 cobertura: RESCATE_GENERA_VENTA_SPOT_DOLAR=S con rescate en CLP (no genera spot)", [sol(R)],
         seed=[param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")])
    caso("FMM-NOR-83_owp-parametro-s-canal-opt", "step 5 cobertura: PLA.NotificarEstadoOrdenOWP=S pero canal OPT: no se notifica", [sol(R)],
         seed=[upd(L.PLACONF, {"Valor": "S"}, "\"CodigoParametro\" = 'PLA.NotificarEstadoOrdenOWP'")])
    caso("FMM-NOR-84_owp-sin-fila-en-la-vista", "step 5 cobertura: canal OWP y parametro S, la vista no devuelve la solicitud: lista vacia, no se notifica", [sol(R)],
         p_IdTipoCanalIngreso="OWP", colab={"NotificarEstadoOrdenesOWP": {"defecto": {}}},
         seed=['ALTER TABLE "%s" ADD COLUMN "IdOmnibus" BIGINT;' % VISTA, upd(L.PLACONF, {"Valor": "S"}, "\"CodigoParametro\" = 'PLA.NotificarEstadoOrdenOWP'")])
    caso("FMM-NOR-85_aporte-traspaso", "step 5 cobertura: aporte con EsTraspaso=S (movimiento de aporte por traspaso)", [sol(A, EsTraspaso="S")], colab=SALDO_OK)
    caso("FMM-NOR-86_patrimonio-emisor-origen-no-manual", "step 5 cobertura: serie con PatrimonioAltos=S y origen REC (la validacion de patrimonio del emisor no aplica)", [sol(A)],
         seed=[upd(S, {"PatrimonioAltos": "S", "MontoPatrimonioEmisor": 1000})], p_IdOrigenProceso="REC", colab=SALDO_OK)


if __name__ == "__main__":
    generar()
    print("coverage cases (batch 2) written")
