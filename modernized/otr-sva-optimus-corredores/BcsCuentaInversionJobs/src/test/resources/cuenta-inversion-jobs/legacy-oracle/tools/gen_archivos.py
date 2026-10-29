"""GC: GenerarArchivoCargoAbonoJob, CP: GenerarArchivoClientesPorPagarYCobrarJob, A1: GenerarArchivoCuentasPorCobrarAnexo16Job,
DS: GenerarArchivoDiferenciaSaldoJob."""
from gen_common import *


def salida_ruta(ruta="cta/Archivo_20250331.csv", errores=0, mensajes=None, **extra):
    d = {"P_OUT_ERRORESCONTADOR": errores, "P_OUT_MENSAJES": mensajes or [], "P_OUT_RUTAARCHIVO": ruta}
    d.update(extra)
    return d


def gen_gc():
    S, J = "GenerarArchivoCargoAbono", "GenerarArchivoCargoAbonoJob"
    P = {"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "IdModulo": "CTA", "IdEjecutivo": "0", "IdMoneda": "0", "IdTipoMovimiento": "0",
         "IdConcepto": "CAREAB", "IdEstado": "0", "FechaDesde": "2025-03-01", "FechaHasta": "2025-03-31"}

    def c(n, desc, p=None, colab=None, **kw):
        escribir("GC-%02d" % n, J, desc, parametros=P if p is None else p, colaboradores=colab, **kw)

    r = lambda **kw: salida_ruta("cta/CargoAbono_20250331.csv", **{"P_OUT_CANTIDADREGISTROS": 25, **kw})
    c(1, "25 records: RutaSalida and an empty comment; job commit, then BcsJob commit", colab=svc(S, r(), sql=marca("archivo")))
    c(2, "zero records: the comment says there are no movements", colab=svc(S, r(P_OUT_CANTIDADREGISTROS=0)))
    c(3, "service reports 1 error: false, BcsJob rollback, nothing updated", colab=svc(S, r(errores=1), sql=marca("archivo")))
    c(4, "service throws Exception after writing: job rollback + BcsJob rollback, ERROR", colab=svc(S, sql=marca("parcial"), lanza="lanza:Exception:falla del archivo"))
    c(5, "service throws java.lang.Error", colab=svc(S, lanza="lanza:Error:error fatal"))
    c(6, "IdEjecutivo missing: NumberFormatException before the service", p=con(P, IdEjecutivo=None))
    c(7, "IdEjecutivo not numeric", p=con(P, IdEjecutivo="todos"))
    c(8, "optional texts missing: nulls handed to the service", p=con(P, IdMoneda=None, IdTipoMovimiento=None, IdConcepto=None, IdEstado=None, IdModulo=None), colab=svc(S, r()))
    c(9, "error counter not answered (the legacy class has no default): NullPointerException", colab=svc(S, {"P_OUT_MENSAJES": [], "P_OUT_RUTAARCHIVO": "x.csv", "P_OUT_CANTIDADREGISTROS": 1}))
    c(10, "record count null: NullPointerException", colab=svc(S, r(P_OUT_CANTIDADREGISTROS=None)))
    c(11, "no path: RutaSalida becomes NULL", colab=svc(S, r(P_OUT_RUTAARCHIVO=None)))
    c(12, "a single record", colab=svc(S, r(P_OUT_CANTIDADREGISTROS=1)))
    c(13, "negative record count is not zero", colab=svc(S, r(P_OUT_CANTIDADREGISTROS=-1)))
    c(14, "FechaDesde after FechaHasta is handed as is", p=con(P, FechaDesde="2025-04-30", FechaHasta="2025-01-01"), colab=svc(S, r()))
    c(15, "IdEmpresa missing", p=con(P, IdEmpresa=None))
    c(16, "empty texts", p=con(P, IdMoneda="", IdConcepto="", IdEstado=""), colab=svc(S, r()))
    c(17, "specific executive, movement type and concept", p=con(P, IdEjecutivo="88", IdMoneda="USD", IdTipoMovimiento="MOVCAR", IdConcepto="CARGO"), colab=svc(S, r()))


def gen_cp():
    S, J = "GenerarArchivoClientesPorPagarYCobrar", "GenerarArchivoClientesPorPagarYCobrarJob"
    P = {"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "IdModulo": "CTA", "FechaSistema": "2025-03-31", "EsBusqueda": "N", "IdMoneda": "CLP", "TipoSaldo": "NEG"}

    def c(n, desc, p=None, colab=None, **kw):
        escribir("CP-%02d" % n, J, desc, parametros=P if p is None else p, colaboradores=colab, **kw)

    c(1, "no errors: RutaSalida updated, job commit, BcsJob commit", colab=svc(S, salida_ruta("cta/ClientesPorPagarYCobrar.csv"), sql=marca("archivo")))
    c(2, "one error: java.lang.Error with the message text; the Error skips catch (Exception), nothing is committed",
      colab=svc(S, salida_ruta(errores=1, mensajes=[msg("ERROR", "CTA-E1", "No hay saldos")]), sql=marca("archivo")))
    c(3, "two messages: type: description, two spaces after each", colab=svc(S, salida_ruta(errores=2, mensajes=[msg("ERROR", "A", "primero", 1), msg("ADVERTENCIA", "B", "segundo", 2)])))
    c(4, "an error counter without messages: Error with an empty message", colab=svc(S, salida_ruta(errores=1)))
    c(5, "a message without type: 'null: ' in the text", colab=svc(S, salida_ruta(errores=1, mensajes=[{"id": 1, "codigo": "X", "descripcion": "sin tipo"}])))
    c(6, "EsBusqueda S and TipoSaldo TOD handed as is", p=con(P, EsBusqueda="S", TipoSaldo="TOD"), colab=svc(S, salida_ruta()))
    c(7, "TipoSaldo POS, no currency", p=con(P, TipoSaldo="POS", IdMoneda=None), colab=svc(S, salida_ruta()))
    c(8, "service throws Exception after writing: rollback + rethrow", colab=svc(S, sql=marca("parcial"), lanza="lanza:Exception:falla"))
    c(9, "service throws java.lang.Error", colab=svc(S, lanza="lanza:Error:error fatal"))
    c(10, "the service answers nothing: the legacy default path is the empty text", colab=svc(S))
    c(11, "null path: RutaSalida becomes NULL", colab=svc(S, salida_ruta(None)))
    c(12, "IdEmpresa missing: NumberFormatException before the try", p=con(P, IdEmpresa=None))
    c(13, "IdUsuario not numeric", p=con(P, IdUsuario="u"))
    c(14, "optional parameters missing", p={"IdEmpresa": EMPRESA, "IdUsuario": USUARIO}, colab=svc(S, salida_ruta()))
    c(15, "error messages null with a counter: NullPointerException in the stream", colab=svc(S, {"P_OUT_ERRORESCONTADOR": 1, "P_OUT_MENSAJES": None, "P_OUT_RUTAARCHIVO": ""}))
    c(16, "messages without a counter are ignored", colab=svc(S, salida_ruta(mensajes=[msg("ERROR", "X", "ignorado")])))
    c(17, "counter null: NullPointerException", colab=svc(S, {"P_OUT_ERRORESCONTADOR": None, "P_OUT_MENSAJES": [], "P_OUT_RUTAARCHIVO": ""}))


def sencillo(prefijo, job, servicio, parametros):
    def c(n, desc, p=None, colab=None, **kw):
        escribir("%s-%02d" % (prefijo, n), job, desc, parametros=parametros if p is None else p, colaboradores=colab, **kw)

    S = servicio
    c(1, "no errors: RutaSalida updated, job commit, BcsJob commit", colab=svc(S, salida_ruta("cta/Reporte_20250331.csv"), sql=marca("archivo")))
    c(2, "one error: false, BcsJob rollback, nothing updated", colab=svc(S, salida_ruta(errores=1), sql=marca("archivo")))
    c(3, "three errors", colab=svc(S, salida_ruta(errores=3)))
    c(4, "service throws Exception after writing: job rollback + BcsJob rollback, ERROR", colab=svc(S, sql=marca("parcial"), lanza="lanza:Exception:falla"))
    c(5, "service throws java.lang.Error", colab=svc(S, lanza="lanza:Error:error fatal"))
    c(6, "IdEmpresa missing: NumberFormatException", p=con(parametros, IdEmpresa=None))
    c(7, "IdUsuario not numeric", p=con(parametros, IdUsuario="x1"))
    c(8, "null path: RutaSalida becomes NULL", colab=svc(S, salida_ruta(None)))
    c(9, "empty path is stored", colab=svc(S, salida_ruta("")))
    c(10, "counter null: NullPointerException", colab=svc(S, {"P_OUT_ERRORESCONTADOR": None, "P_OUT_MENSAJES": [], "P_OUT_RUTAARCHIVO": "x"}))
    c(11, "the program of the run is 901", idPrograma=901, colab=svc(S, salida_ruta("cta/otro.csv")),
      seed="INSERT INTO \"BSCL.Plataforma.BD::BAT.Programa\" (\"IdPrograma\", \"RutaSalida\", \"RutaLog\", \"Comentario\") VALUES (901, 'OTRA', 'OTRA-LOG', 'OTRO');")
    c(12, "the service answers nothing: legacy defaults (no errors, null path)", colab=svc(S))
    c(13, "every optional parameter missing", p={"IdEmpresa": EMPRESA, "IdUsuario": USUARIO}, colab=svc(S, salida_ruta()))
    return c


def gen_a1():
    p = {"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "FechaHasta": "2025-03-31"}
    c = sencillo("A1", "GenerarArchivoCuentasPorCobrarAnexo16Job", "GenerarArchivoCuentasPorCobrarAnexo16", p)
    c(14, "FechaHasta missing: null handed to the service", p=con(p, FechaHasta=None), colab=svc("GenerarArchivoCuentasPorCobrarAnexo16", salida_ruta()))


def gen_ds():
    p = {"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "IdModulo": "CTA", "FechaDesde": "2025-03-01", "FechaHasta": "2025-03-31"}
    c = sencillo("DS", "GenerarArchivoDiferenciaSaldoJob", "ConsultaComparacionSaldo", p)
    c(14, "dates missing: nulls handed to the service", p=con(p, FechaDesde=None, FechaHasta=None), colab=svc("ConsultaComparacionSaldo", salida_ruta()))


def gen_all():
    gen_gc()
    gen_cp()
    gen_a1()
    gen_ds()


if __name__ == "__main__":
    gen_all()
