"""SC: GenerarArchivoSaldoCuentasJob (the parameter parsing has three legacy quirks: IdEjecutivo is compared with != (reference), IdMoneda is
never turned into null, and the service error counter is forced to 0)."""
from gen_common import *
from gen_archivos import salida_ruta

S, J = "GenerarArchivoSaldoCuentas", "GenerarArchivoSaldoCuentasJob"
P = {"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "IdModulo": "CTA", "IdEjecutivo": "15", "IdMoneda": "CLP", "SoloClientesSobregirados": "N",
     "SoloClientesConSaldoDisponible": "N", "SoloClientesConSaldoDisponibleClientesSobregirados": "N", "IncluyeProductoApv": "S",
     "IncluyeOtrosSaldos": "S", "FechaDesde": "2025-03-01", "FechaHasta": "2025-03-31", "Encabezados": "[\"Nombre\",\"Saldo\"]"}
R = salida_ruta("cta/SaldoCuentas_20250331.csv")


def c(n, desc, p=None, colab=None, **kw):
    escribir("SC-%02d" % n, J, desc, parametros=P if p is None else p, colaboradores=colab, **kw)


def gen_sc():
    c(1, "the .csv path becomes .zip; job commit, then BcsJob commit", colab=svc(S, R, sql=marca("archivo")))
    c(2, "IdEjecutivo 0 is NOT turned into null: the legacy compares the text with != (reference), so 0 is handed", p=con(P, IdEjecutivo="0"), colab=svc(S, R))
    c(3, "IdEjecutivo missing: NumberFormatException", p=con(P, IdEjecutivo=None))
    c(4, "IdEjecutivo empty text: NumberFormatException", p=con(P, IdEjecutivo=""))
    c(5, "IdEjecutivo with leading zeros", p=con(P, IdEjecutivo="007"), colab=svc(S, R))
    c(6, "IdEjecutivo not numeric", p=con(P, IdEjecutivo="abc"))
    c(7, "IdMoneda empty text is handed as the empty text (never null)", p=con(P, IdMoneda=""), colab=svc(S, R))
    c(8, "IdMoneda missing: null", p=con(P, IdMoneda=None), colab=svc(S, R))
    c(9, "Encabezados missing: the service gets the default empty list", p=con(P, Encabezados=None), colab=svc(S, R))
    c(10, "Encabezados empty array", p=con(P, Encabezados="[]"), colab=svc(S, R))
    c(11, "Encabezados is not JSON: JsonSyntaxException", p=con(P, Encabezados="no es json"))
    c(12, "Encabezados is a JSON object: JsonSyntaxException", p=con(P, Encabezados="{\"a\":1}"))
    c(13, "Encabezados is the text null: no list, the default stays", p=con(P, Encabezados="null"), colab=svc(S, R))
    c(14, "forty headers with accents", p=con(P, Encabezados=json.dumps(["Encabezado %d áéí" % i for i in range(40)], ensure_ascii=False)), colab=svc(S, R))
    c(15, "the service reports 3 errors: the job overwrites the counter with 0 and commits", colab=svc(S, salida_ruta("cta/Saldo.csv", errores=3, mensajes=[msg("ERROR", "X", "ignorado")]), sql=marca("archivo")))
    c(16, "every .csv in the path is replaced", colab=svc(S, salida_ruta("cta/a.csv.csv")))
    c(17, "upper case .CSV is not replaced", colab=svc(S, salida_ruta("cta/SALDO.CSV")))
    c(18, "no .csv in the path: unchanged", colab=svc(S, salida_ruta("cta/saldo.txt")))
    c(19, "null path: NullPointerException, job rollback + BcsJob rollback", colab=svc(S, salida_ruta(None)))
    c(20, "empty path stays empty", colab=svc(S, salida_ruta("")))
    c(21, "service throws Exception after writing: rollback + rethrow", colab=svc(S, sql=marca("parcial"), lanza="lanza:Exception:falla"))
    c(22, "service throws java.lang.Error", colab=svc(S, lanza="lanza:Error:error fatal"))
    c(23, "IdEmpresa missing", p=con(P, IdEmpresa=None))
    c(24, "every flag Y/N handed as is, dates missing", p=con(P, SoloClientesSobregirados="S", SoloClientesConSaldoDisponible="S", SoloClientesConSaldoDisponibleClientesSobregirados="S",
                                                           IncluyeProductoApv="N", IncluyeOtrosSaldos="N", FechaDesde=None, FechaHasta=None), colab=svc(S, R))
    c(25, "every optional parameter missing but IdEjecutivo", p={"IdEmpresa": EMPRESA, "IdUsuario": USUARIO, "IdEjecutivo": "1"}, colab=svc(S, R))


def gen_all():
    gen_sc()


if __name__ == "__main__":
    gen_all()
