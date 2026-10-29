"""IP: BcsJobGenerarInformePatrimonioDiario, RC: BcsJobGenerarInformeResumenCobro, GP: GuardarPatrimonioCajaJob."""
from gen_common import *

P = {"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "IdCliente": "5001", "IdPortafolio": "3", "IdEstado": "ACT", "IdProductoAgrupador": "12",
     "FechaDesde": "2025-03-01", "FechaHasta": "2025-03-31", "Periodo": "202503"}


def informe_salida(**kw):
    d = {"p_out_NombreArchivo": "Informe_20250331.xlsx", "p_out_NombreArchivoLog": "Informe_20250331.log", "p_out_RutaArchivo": "cta/informes/",
         "p_out_RutaArchivoLog": "cta/informes/", "p_out_ErroresContador": 0, "p_out_Comentario": "Informe generado", "p_out_ArchivoSecundario": []}
    d.update(kw)
    return d


def gen_informe(prefijo, job, servicio, orden_distinto):
    def c(n, desc, p=None, colab=None, **kw):
        escribir("%s-%02d" % (prefijo, n), job, desc, parametros=P if p is None else p, colaboradores=colab, **kw)

    c(1, "the service names a file: BAT.Programa gets name, log and comment; job commit, then BcsJob commit", colab=svc(servicio, informe_salida(), sql=marca("informe")))
    c(2, "no file name: nothing is updated, the job returns false: BcsJob rollback (service work discarded), reported as success",
      colab=svc(servicio, informe_salida(p_out_NombreArchivo=None), sql=marca("informe")))
    c(3, "the service counts errors but names a file: errors are ignored", colab=svc(servicio, informe_salida(p_out_ErroresContador=2)))
    c(4, "no comment: Comentario becomes NULL", colab=svc(servicio, informe_salida(p_out_Comentario=None)))
    c(5, "service throws Exception after writing: job rollback + BcsJob rollback, ERROR", colab=svc(servicio, sql=marca("parcial"), lanza="lanza:Exception:falla del informe"))
    c(6, "service throws java.lang.Error: only the implicit rollback", colab=svc(servicio, lanza="lanza:Error:error fatal"))
    c(7, "FechaDesde with a time part is cut to 10 characters", p=con(P, FechaDesde="2025-03-01 00:00:00", FechaHasta="2025-03-31T23:59:59"), colab=svc(servicio, informe_salida()))
    c(8, "FechaDesde shorter than 10 characters: StringIndexOutOfBoundsException", p=con(P, FechaDesde="2025-3-1"))
    c(9, "FechaHasta missing: NullPointerException", p=con(P, FechaHasta=None))
    c(10, "FechaDesde missing: NullPointerException", p=con(P, FechaDesde=None))
    c(11, "IdCliente not numeric", p=con(P, IdCliente="cliente"))
    c(12, "Periodo missing: NumberFormatException", p=con(P, Periodo=None))
    c(13, "two bad numbers: the first one in read order is reported (IdCliente before IdPortafolio)", p=con(P, IdPortafolio="x", IdCliente="y"))
    c(14, "IdEstado missing: null handed to the service", p=con(P, IdEstado=None), colab=svc(servicio, informe_salida()))
    c(15, "another base path configured", config={"PLATAFORMA/PLA.RutaBaseArchivos": "/mnt/optimus/des/empresa1/"}, colab=svc(servicio, informe_salida()))
    c(16, "the program of the run is 901: only that row is updated", idPrograma=901, colab=svc(servicio, informe_salida()),
      seed="INSERT INTO \"BSCL.Plataforma.BD::BAT.Programa\" (\"IdPrograma\", \"RutaSalida\", \"RutaLog\", \"Comentario\") VALUES (901, 'OTRA', 'OTRA-LOG', 'OTRO');")
    c(17, "the program row does not exist: the update changes nothing and the run is a success", idPrograma=777, colab=svc(servicio, informe_salida()))
    c(18, "secondary files are ignored", colab=svc(servicio, informe_salida(p_out_ArchivoSecundario=["a.csv", "b.csv"])))
    c(19, "two bad values in the order that differs between the two reports: IdProductoAgrupador bad, FechaDesde missing",
      p=con(P, IdProductoAgrupador="z", FechaDesde=None))
    c(20, "empty file name is a name: it is stored", colab=svc(servicio, informe_salida(p_out_NombreArchivo="", p_out_NombreArchivoLog="")))


def gen_gp():
    S = "GuardarPatrimonioCaja"
    Q = {"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "Fecha": "2025-03-28"}

    def c(n, desc, p=None, colab=None, **kw):
        escribir("GP-%02d" % n, "GuardarPatrimonioCajaJob", desc, parametros=Q if p is None else p, colaboradores=colab, **kw)

    c(1, "date given: handed as is, p_IdModulo is PLA; no error: true, BcsJob commit", colab=svc(S, ok(), sql=marca("patrimonio")))
    c(2, "Fecha missing: PLA.FechaSistema of the platform configuration", p=con(Q, Fecha=None), colab=svc(S, ok()))
    c(3, "Fecha empty text: PLA.FechaSistema", p=con(Q, Fecha=""), colab=svc(S, ok()))
    c(4, "Fecha only spaces is a date", p=con(Q, Fecha="  "), colab=svc(S, ok()))
    c(5, "Fecha missing and the configuration has no PLA.FechaSistema: the legacy message", p=con(Q, Fecha=None), config={"PLATAFORMA/PLA.FechaSistema": None})
    c(6, "the service reports 1 error: false, BcsJob rollback, reported as success", colab=svc(S, err(1), sql=marca("patrimonio")))
    c(7, "the service reports 5 errors", colab=svc(S, err(5)))
    c(8, "messages without a counter are ignored", colab=svc(S, {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_MENSAJES": [msg("ERROR", "X", "ignorado")]}))
    c(9, "service throws Exception: ERROR (rethrown)", colab=svc(S, sql=marca("parcial"), lanza="lanza:Exception:falla"))
    c(10, "service throws RuntimeException", colab=svc(S, lanza="lanza:RuntimeException:fallo en tiempo de ejecucion"))
    c(11, "service throws java.lang.Error: not caught by catch (Exception)", colab=svc(S, lanza="lanza:Error:error fatal"))
    c(12, "IdEmpresa missing", p=con(Q, IdEmpresa=None))
    c(13, "IdUsuario not numeric", p=con(Q, IdUsuario="x"))
    c(14, "error counter null: NullPointerException", colab=svc(S, {"P_OUT_ERRORESCONTADOR": None, "P_OUT_MENSAJES": []}))
    c(15, "service answers nothing: success", colab=svc(S))
    c(16, "Fecha missing, another date in the configuration", p=con(Q, Fecha=None), config={"PLATAFORMA/PLA.FechaSistema": "2025-12-31"}, colab=svc(S, ok()))


def gen_all():
    gen_informe("IP", "BcsJobGenerarInformePatrimonioDiario", "GenerarInformePatrimonioDiario", False)
    gen_informe("RC", "BcsJobGenerarInformeResumenCobro", "GenerarInformeResumenCobro", True)
    gen_gp()


if __name__ == "__main__":
    gen_all()
