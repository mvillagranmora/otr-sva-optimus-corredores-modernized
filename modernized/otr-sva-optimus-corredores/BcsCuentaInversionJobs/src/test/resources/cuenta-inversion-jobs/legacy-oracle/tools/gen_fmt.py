"""Helpers of the GenerarArchivo (GA) and formatter (GF) cases: the seed of one file format (the two ARC views) and the scripted stored procedure."""
from gen_common import *

VISTA = "BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivo"
DETALLE = "BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivoDetalle"
RUTA = "${RUTA_BASE}/cta/"


def lit(v):
    if v is None:
        return "NULL"
    if isinstance(v, (int, float)):
        return str(v)
    return "'" + str(v).replace("'", "''") + "'"


def formato(id=1, empresa=1, estado="ACT", codigo="ARCHIVO_X", salida="TXT", servicio="SP_ARCHIVO_X", sep="|", desc="Archivo de prueba", ruta_modulo="cta/",
            ruta=RUTA, nombre="ARCHIVO_X", tipos=1, rellenar="S", sep_dec=".", agrega="N"):
    cols = [empresa, estado, id, codigo, salida, servicio, sep, desc, ruta_modulo, ruta, nombre, tipos, rellenar, sep_dec, agrega]
    return ("INSERT INTO \"%s\" (\"IdEmpresa\", \"IdEstado\", \"IdFormatoArchivo\", \"CodigoFormatoArchivo\", \"FormatoSalida\", \"Servicio\", \"SeparadorCampos\", "
            "\"Descripcion\", \"RutaModulo\", \"RutaCompleta\", \"NombreArchivoSalida\", \"CantidadDeTiposDeRegistro\", \"RellenarCampo\", \"SeparadorDecimales\", "
            "\"AgregoNombreColumna\") VALUES (%s);" % (VISTA, ", ".join(lit(c) for c in cols)))


_ids = [100]


def detalle(campo, tipo_dato, largo, decimales=0, defecto="", orden=1, tipo_registro=1, formato_fecha=None, id_formato=1, id_detalle=None):
    _ids[0] += 1
    idd = id_detalle if id_detalle is not None else _ids[0]
    cols = [id_formato, idd, tipo_registro, campo, tipo_dato, largo, decimales, defecto, orden, "Campo " + campo, formato_fecha]
    return ("INSERT INTO \"%s\" (\"IdFormatoArchivo\", \"IdFormatoArchivoDetalle\", \"TipoRegistro\", \"Campo\", \"TipoDato\", \"Largo\", \"Decimales\", "
            "\"ValorDefecto\", \"Orden\", \"DescripcionFormatoDetalle\", \"Formato\") VALUES (%s);" % (DETALLE, ", ".join(lit(c) for c in cols)))


def resultado(columnas, filas):
    return {"columnas": columnas, "filas": filas}


def proc(*resultados, **extra):
    d = {"resultados": list(resultados)}
    d.update(extra)
    return {"procedimiento": d}


PARAMS = {"IdEmpresa": "1", "IdUsuario": "21", "CodigoFormatoArchivo": "ARCHIVO_X"}


def caso_ga(id, desc, seed, colab, p=None, dirs=("cta",), **kw):
    escribir(id, "GenerarArchivo", desc, parametros=PARAMS if p is None else p, colaboradores=colab, dirs=list(dirs) if dirs else None,
             seed="\n".join(seed) if isinstance(seed, (list, tuple)) else seed, **kw)
