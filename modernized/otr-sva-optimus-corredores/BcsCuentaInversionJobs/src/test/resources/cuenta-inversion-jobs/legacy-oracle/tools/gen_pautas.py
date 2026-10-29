"""PS: PautaActualizarSaldoCargoAbono, PG: PautaGuardarSaldosReporteAntiguedad (call a service), PR: PautaGenerarReporteAcreedores,
PB: PautaGenerarReporteAntiguedadDeudaBolsaCTA (enrol a batch program). The PAUTA steps are not BcsJobs: IdEmpresa / IdUsuario are JSON numbers
(handed as Long, as the PAUTA runner does) and the observation is the returned SalidaProcesoPauta or the exception."""
from gen_common import *

P = {"IdEmpresa": 1, "IdUsuario": 21}
MENS = [msg("INFORMACION", "CTA-I01", "Primer detalle", 1), msg("ADVERTENCIA", "CTA-A02", "Segundo detalle", 2)]


def pauta(prefijo, job, servicio, salida_ok, **fixed):
    def c(n, desc, p=None, colab=None, **kw):
        escribir("%s-%02d" % (prefijo, n), job, desc, parametros=P if p is None else p, colaboradores=colab, tablas=[PROGRAMA, MARCA], **kw)

    S = servicio
    c(1, "no errors, no messages: the summary is the empty text", colab=svc(S, salida_ok()))
    c(2, "two messages: the summary is each description after a line break", colab=svc(S, salida_ok(mensajes=MENS)))
    c(3, "two errors counted", colab=svc(S, salida_ok(errores=2, mensajes=MENS)))
    c(4, "service throws Exception: logged and rethrown", colab=svc(S, lanza="lanza:Exception:falla del servicio"))
    c(5, "PLA.FechaSistema not configured: the legacy message", config={"PLATAFORMA/PLA.FechaSistema": None})
    c(6, "IdEmpresa missing: a null Long", p={"IdUsuario": 21}, colab=svc(S, salida_ok()))
    c(7, "IdEmpresa is text: ClassCastException", p={"IdEmpresa": "1", "IdUsuario": 21})
    c(8, "IdUsuario missing", p={"IdEmpresa": 1}, colab=svc(S, salida_ok()))
    c(9, "service throws java.lang.Error", colab=svc(S, lanza="lanza:Error:error fatal"))
    c(10, "messages null: NullPointerException when the summary is built", colab=svc(S, salida_ok(P_OUT_MENSAJES=None)))
    c(11, "another system date in the configuration", config={"PLATAFORMA/PLA.FechaSistema": "2025-12-31"}, colab=svc(S, salida_ok()))
    c(12, "error counter null: kept null in the answer", colab=svc(S, salida_ok(P_OUT_ERRORESCONTADOR=None)))
    c(13, "service writes before failing: the step ends in an error and its work is not committed by the step", colab=svc(S, sql=marca("parcial"), lanza="lanza:RuntimeException:falla"))
    return c


def salida_servicio(errores=0, mensajes=None, **kw):
    d = {"P_OUT_ERRORESCONTADOR": errores, "P_OUT_MENSAJES": mensajes if mensajes is not None else []}
    d.update(kw)
    return d


def salida_inscribir(errores=0, mensajes=None, **kw):
    d = {"P_OUT_ERRORESCONTADOR": errores, "P_OUT_PROGRAMA": [], "P_OUT_MENSAJES": mensajes if mensajes is not None else []}
    d.update(kw)
    return d


def gen_all():
    pauta("PS", "PautaActualizarSaldoCargoAbono", "ActualizarSaldoCargoAbono", salida_servicio)
    pauta("PG", "PautaGuardarSaldosReporteAntiguedad", "GuardarSaldosReporteAntiguedad", salida_servicio)
    pauta("PR", "PautaGenerarReporteAcreedores", "InscribirProcesoBatch", salida_inscribir)
    c = pauta("PB", "PautaGenerarReporteAntiguedadDeudaBolsaCTA", "InscribirProcesoBatch", salida_inscribir)
    S = "InscribirProcesoBatch"
    for pre, job in (("PR", "PautaGenerarReporteAcreedores"), ("PB", "PautaGenerarReporteAntiguedadDeudaBolsaCTA")):
        def d(n, desc, p=None, colab=None, **kw):
            escribir("%s-%02d" % (pre, n), job, desc, parametros=P if p is None else p, colaboradores=colab, tablas=[PROGRAMA, MARCA], **kw)
        d(14, "the program is enrolled with IdUsuario 0 whatever the caller says, module CTA", p={"IdEmpresa": 7, "IdUsuario": 99}, colab=svc(S, salida_inscribir()))
        d(15, "IdEmpresa missing: the text 'null' is handed as the IdEmpresa parameter", p={"IdUsuario": 21}, colab=svc(S, salida_inscribir()))
        d(16, "the platform service throws an unchecked exception", colab=svc(S, lanza="lanza:RuntimeException:plataforma no disponible"))
        d(17, "an enrolled program answered by the platform: only the counter and the messages are used",
          colab=svc(S, salida_inscribir(mensajes=[msg("OK", "BAT-OK", "Programa inscrito", 1)])))


if __name__ == "__main__":
    gen_all()
