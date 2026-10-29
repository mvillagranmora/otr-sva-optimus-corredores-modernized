"""AS: ActualizarSaldoCargoAbonoJob, AR: ActualizarSaldosReporteAntiguedadJob, CI: CargaInicialSaldosReporteAntiguedadJob."""
from gen_common import *

SVC = "ActualizarSaldoCargoAbono"
JOB = "ActualizarSaldoCargoAbonoJob"
P = {"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "FechaSistema": "2025-03-31", "CuentasInversion": "101;102;103"}
T = [PROGRAMA, MARCA, TIPO_EJEC]


def as_(n, desc, p=None, colab=None, **kw):
    escribir("AS-%02d" % n, JOB, desc, parametros=P if p is None else p, colaboradores=colab, tablas=T, **kw)


def gen_as():
    as_(1, "three accounts, service without errors: commit of the job, the state row is EJEC then free, three commits", colab=svc(SVC, ok(), sql=marca("saldos")))
    as_(2, "a single account", p=con(P, CuentasInversion="7"), colab=svc(SVC, ok()))
    as_(3, "service reports 1 error: rollback of the service work, state row freed and committed, ROLLBACK reported as success",
        colab=svc(SVC, err(1), sql=marca("saldos")))
    as_(4, "service reports 3 errors", colab=svc(SVC, err(3)))
    as_(5, "service throws Exception after writing: rollback, state row freed in the finally, ERROR", colab=svc(SVC, sql=marca("parcial"), lanza="lanza:Exception:falla del servicio"))
    as_(6, "service throws RuntimeException (IllegalStateException)", colab=svc(SVC, lanza="lanza:IllegalStateException:estado invalido"))
    as_(7, "service throws java.lang.Error: not caught by catch (Exception); the finally still frees the state row and commits",
        colab=svc(SVC, sql=marca("error"), lanza="lanza:Error:error fatal"))
    as_(8, "IdEmpresa missing: NumberFormatException, rollback, the state row is freed by the finally", p=con(P, IdEmpresa=None))
    as_(9, "IdUsuario not numeric", p=con(P, IdUsuario="abc"))
    as_(10, "FechaSistema missing: null date handed to the service", p=con(P, FechaSistema=None), colab=svc(SVC, ok()))
    as_(11, "CuentasInversion missing: NullPointerException", p=con(P, CuentasInversion=None))
    as_(12, "CuentasInversion empty text: split gives one empty element, NumberFormatException", p=con(P, CuentasInversion=""))
    as_(13, "an empty element between separators", p=con(P, CuentasInversion="101;;103"))
    as_(14, "trailing separator: split drops the empty tail", p=con(P, CuentasInversion="101;102;"), colab=svc(SVC, ok()))
    as_(15, "spaces around an account id: Long.parseLong rejects them", p=con(P, CuentasInversion="101; 102"))
    as_(16, "non numeric account", p=con(P, CuentasInversion="101;A2"))
    as_(17, "zero and negative account ids are parsed", p=con(P, CuentasInversion="0;-5;9223372036854775807"), colab=svc(SVC, ok()))
    as_(18, "500 accounts", p=con(P, CuentasInversion=";".join(str(1000 + i) for i in range(500))), colab=svc(SVC, ok()))
    as_(19, "the state row does not exist: the two updates change nothing", colab=svc(SVC, ok()),
        seed="DELETE FROM \"BSCL.Plataforma.BD::BAT.TipoProgramaEjecucion\";")
    as_(20, "service answers a null error counter: NullPointerException on equals", colab=svc(SVC, {"P_OUT_ERRORESCONTADOR": None, "P_OUT_MENSAJES": []}))
    as_(21, "error messages without a counter are ignored: only the counter decides", colab=svc(SVC, {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_MENSAJES": [msg("ERROR", "X", "ignorado")]}, sql=marca("saldos")))
    as_(22, "the state row is in use (EJEC) before the run", colab=svc(SVC, ok()),
        seed="UPDATE \"BSCL.Plataforma.BD::BAT.TipoProgramaEjecucion\" SET \"IdEstado\" = 'EJEC';")


def simple(prefijo, job, servicio, entrada_params, n_ok):
    """Shape shared by AR and CI: errors == 0 -> job commit + true; otherwise job rollback + false; no catch (BcsJob rolls back)."""
    base = entrada_params

    def c(n, desc, p=None, colab=None, **kw):
        escribir("%s-%02d" % (prefijo, n), job, desc, parametros=base if p is None else p, colaboradores=colab, **kw)

    c(1, "service without errors: job commit, then the BcsJob commit", colab=svc(servicio, ok(), sql=marca("ok")))
    c(2, "service reports 1 error: job rollback (service work discarded), ROLLBACK reported as success", colab=svc(servicio, err(1), sql=marca("ok")))
    c(3, "service reports 4 errors", colab=svc(servicio, err(4)))
    c(4, "service throws Exception after writing: only the BcsJob rollback (the job has no catch), ERROR", colab=svc(servicio, sql=marca("parcial"), lanza="lanza:Exception:falla"))
    c(5, "service throws java.lang.Error", colab=svc(servicio, lanza="lanza:Error:error fatal"))
    c(6, "service throws IllegalArgumentException", colab=svc(servicio, lanza="lanza:RuntimeException:argumento invalido"))
    c(7, "IdEmpresa missing: NumberFormatException before the service", p=con(base, IdEmpresa=None))
    c(8, "IdUsuario not numeric", p=con(base, IdUsuario="1x"))
    c(9, "IdEmpresa with a decimal point", p=con(base, IdEmpresa="1.5"))
    c(10, "error counter null: NullPointerException", colab=svc(servicio, {"P_OUT_ERRORESCONTADOR": None, "P_OUT_MENSAJES": []}))
    c(11, "service answers nothing (every field the legacy default): success", colab=svc(servicio))
    return c


def gen_ar():
    p = {"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "Rut": "11111111-1", "CodigoPortafolio": "PF01", "IdMoneda": "CLP", "FechaMovimiento": "2025-03-01"}
    c = simple("AR", "ActualizarSaldosReporteAntiguedadJob", "ActualizarSaldosReporteAntiguedad", p, 11)
    c(12, "Rut missing: null handed to the service", p=con(p, Rut=None), colab=svc("ActualizarSaldosReporteAntiguedad", ok()))
    c(13, "every optional parameter missing", p={"IdEmpresa": EMPRESA, "IdUsuario": USUARIO}, colab=svc("ActualizarSaldosReporteAntiguedad", ok()))
    c(14, "IdMoneda is handed as p_Moneda; foreign currency and empty portfolio", p=con(p, IdMoneda="USD", CodigoPortafolio=""), colab=svc("ActualizarSaldosReporteAntiguedad", ok()))


def gen_ci():
    p = {"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "FechaSistema": "2025-03-31"}
    c = simple("CI", "CargaInicialSaldosReporteAntiguedadJob", "CargaInicialSaldosReporteAntiguedad", p, 11)
    c(12, "FechaSistema missing: null handed to the service", p=con(p, FechaSistema=None), colab=svc("CargaInicialSaldosReporteAntiguedad", ok()))


def gen_all():
    gen_as()
    gen_ar()
    gen_ci()


if __name__ == "__main__":
    gen_all()
