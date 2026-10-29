"""Shared base of the GenerarArchivoPago / GenerarArchivoPagos / GenerarArchivoPagoBancoEstado generators."""
from gen_common import PROGRAMA, escribir, merge

NOMINA = {"nombre": "BSCL.Tesoreria.BD::TES.NominaTransferencia", "orden": "\"IdNominaTransferencia\""}


def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, (int, float)):
        return str(v)
    return "'" + str(v).replace("'", "''") + "'"


def insert(tabla, fila):
    cols = ", ".join('"%s"' % k for k in fila)
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (tabla, cols, ", ".join(q(v) for v in fila.values()))


def fila_formato(**o):
    f = {"IdEmpresa": 1, "IdBanco": 10, "IdMoneda": "CLP", "IdMedioDePago": "TRA", "IdEstado": "ACT", "IdFormatoArchivo": 5,
         "CodigoFormatoArchivo": "GEN_TXT", "SeparadorCampos": "|", "SeparadorDecimales": ".", "RellenarCampo": "S", "NombreBanco": "BANCO FICTICIO",
         "FormatoSalida": "TXT", "Servicio": "SP_PAGO_GENERICO", "Descripcion": "Pago generico", "RutaTesoreria": "tes/",
         "RutaCompleta": "${RUTA_BASE}/tes/", "NombreArchivoSalida": "PAGO", "CantidadDeTiposDeRegistro": 1, "TipoSaltoLinea": "W"}
    return merge(f, o)


def fila_detalle(tipo, campo, dato, largo, dec=0, defecto="", formato=None, idf=5, orden=1):
    return {"IdFormatoArchivo": idf, "CodigoFormatoArchivo": "GEN_TXT", "IdFormatoArchivoDetalle": 100 + orden, "TipoRegistro": tipo, "Campo": campo,
            "TipoDato": dato, "Largo": largo, "Decimales": dec, "ValorDefecto": defecto, "Orden": orden, "DescripcionFormatoDetalle": "d", "Formato": formato}


def seed(formatos, detalles, extra=""):
    s = ["DELETE FROM \"BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo\";",
         insert("BSCL.Tesoreria.BD::TES.NominaTransferencia", {"IdNominaTransferencia": 48, "IdEmpresa": 1, "NombreArchivo": None})]
    s += [insert("BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo", f) for f in formatos]
    s += [insert("BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivoDetalle", d) for d in detalles]
    return "\n".join(s) + "\n" + extra


def proc(*sets):
    return {"resultados": [{"columnas": c, "filas": f} for c, f in sets]}


BASE = {"job": "Pago", "idPrograma": 900, "dirs": ["tes"], "reloj": "2025-04-01 15:15:30",
        "parametros": {"IdEmpresa": "1", "IdNominaTransferencia": "48", "IdBanco": "10", "IdMoneda": "CLP", "IdMedioDePago": "TRA"},
        "colaboradores": {},
        "tablas": [PROGRAMA, dict(NOMINA, enmascarar=["NombreArchivo"])]}

DET1 = [fila_detalle(1, "Rut", "CHAR", 10, orden=1), fila_detalle(1, "Monto", "DEC", 12, 2, orden=2)]
PROC1 = proc((["TipoRegistroOptimus", "Rut", "Monto"], [["1", "11111111-1", "1500.50"], ["1", "2222-2", "20.5"]]))


def p(cid, desc, formatos=None, detalles=None, proc_=PROC1, extra="", **over):
    caso = merge(BASE, over)
    caso["colaboradores"] = merge(caso["colaboradores"], {"procedimiento": proc_} if proc_ is not None else {})
    caso["descripcion"] = desc
    escribir(cid, caso, seed(formatos if formatos is not None else [fila_formato()], detalles if detalles is not None else DET1, extra))
