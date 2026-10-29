"""RA: GenerarReporteAcreedoresJob, AB: GenerarReporteAntiguedadDeudaBolsaCTAJob, AC: GenerarReporteAntiguedadDeudaCTAJob (same shape: the report
path and the broker code go to BAT.Programa; any other answer stores the first message and the run still ends as a success)."""
from gen_common import *

BASE = {"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "P_FechaSolicitud": "2025-03-31", "P_Moneda": "CLP", "P_Consolidar": "S", "NombreInternoArchivo": "Reporte_X1"}
EXTRA = {"P_SeparaIntermediacion": "S", "P_AgrupaPortafolios": "N"}


def rep(ruta="cta/Reporte_20250331.xlsx", corredor="COR01", errores=0, mensajes=None, **kw):
    d = {"P_OUT_ERRORESCONTADOR": errores, "P_OUT_MENSAJES": mensajes if mensajes is not None else [], "P_OUT_RUTAARCHIVO": ruta, "P_OUT_CODIGOCORREDOR": corredor}
    d.update(kw)
    return d


def gen_reporte(prefijo, job, servicio, extra):
    P = dict(BASE)
    if extra:
        P.update(EXTRA)
    M = [msg("INFORMACION", "CTA-I01", "Sin datos para el reporte", 1), msg("ERROR", "CTA-E02", "segundo mensaje", 2)]

    def c(n, desc, p=None, colab=None, **kw):
        escribir("%s-%02d" % (prefijo, n), job, desc, parametros=P if p is None else p, colaboradores=colab, **kw)

    S = servicio
    c(1, "report path and broker code: both stored; BcsJob commit", colab=svc(S, rep(), sql=marca("reporte")))
    c(2, "one error: the first message is stored with an empty path, and the run is STILL a success: the service work is committed",
      colab=svc(S, rep(errores=1, mensajes=M), sql=marca("reporte")))
    c(3, "no errors but an empty path, no messages: IndexOutOfBoundsException, job rollback + BcsJob rollback", colab=svc(S, rep(ruta="")))
    c(4, "blank path with messages: the first message is stored", colab=svc(S, rep(ruta="   ", mensajes=M)))
    c(5, "empty broker code with messages", colab=svc(S, rep(corredor="", mensajes=M)))
    c(6, "errors and messages null: NullPointerException", colab=svc(S, rep(errores=1, mensajes=None, P_OUT_MENSAJES=None)))
    c(7, "path null with no errors: NullPointerException on trim", colab=svc(S, rep(ruta=None)))
    c(8, "broker code null: NullPointerException on trim", colab=svc(S, rep(corredor=None)))
    c(9, "service throws Exception after writing: rollback + rethrow", colab=svc(S, sql=marca("parcial"), lanza="lanza:Exception:falla"))
    c(10, "service throws java.lang.Error", colab=svc(S, lanza="lanza:Error:error fatal"))
    c(11, "IdEmpresa missing", p=con(P, IdEmpresa=None))
    c(12, "NombreInternoArchivo and the other optional values missing: nulls handed to the service", p={"IdEmpresa": EMPRESA, "IdUsuario": USUARIO}, colab=svc(S, rep()))
    c(13, "the first message has no description: Comentario becomes NULL", colab=svc(S, rep(errores=1, mensajes=[{"id": 1, "tipo": "ERROR", "codigo": "X"}])))
    c(14, "only the first of many messages is used", colab=svc(S, rep(errores=2, mensajes=[msg("ERROR", "E%d" % i, "mensaje %d" % i, i) for i in range(1, 8)])))
    c(15, "error counter null: NullPointerException", colab=svc(S, rep(errores=None)))
    c(16, "path with spaces around it is stored as it is", colab=svc(S, rep(ruta=" cta/Reporte.xlsx ", corredor=" C ")))
    c(17, "the program of the run is 901", idPrograma=901, colab=svc(S, rep()),
      seed="INSERT INTO \"BSCL.Plataforma.BD::BAT.Programa\" (\"IdPrograma\", \"RutaSalida\", \"RutaLog\", \"Comentario\") VALUES (901, 'OTRA', 'OTRA-LOG', 'OTRO');")
    if extra:
        c(18, "the two options handed to the service", p=con(P, P_SeparaIntermediacion="N", P_AgrupaPortafolios="S", P_Consolidar="N"), colab=svc(S, rep()))
        c(19, "the two options missing", p=sin(P, "P_SeparaIntermediacion", "P_AgrupaPortafolios"), colab=svc(S, rep()))


def gen_all():
    gen_reporte("RA", "GenerarReporteAcreedoresJob", "GenerarReporteAcreedores", False)
    gen_reporte("AB", "GenerarReporteAntiguedadDeudaBolsaCTAJob", "GenerarReporteAntiguedadDeudaBolsaCTA", True)
    gen_reporte("AC", "GenerarReporteAntiguedadDeudaCTAJob", "GenerarReporteAntiguedadDeudaCTA", True)


if __name__ == "__main__":
    gen_all()
