"""LI: LibroInventarioJob (stored procedure ActualizarSaldoLibroInventario; every failure is swallowed and answered as `false`)."""
from gen_common import *

P = {"FechaMovimiento": "2025-03-31", "IdEjecucion": "272"}


def c(n, desc, p=None, colab=None, **kw):
    escribir("LI-%02d" % n, "LibroInventarioJob", desc, parametros=P if p is None else p, colaboradores=colab, **kw)


def gen_li():
    c(1, "the procedure is called with (date text, execution id); true: BcsJob commit")
    c(2, "IdEjecucion not numeric: NumberFormatException swallowed, nothing is executed, false: BcsJob rollback, reported as success", p=con(P, IdEjecucion="abc"))
    c(3, "IdEjecucion missing: swallowed the same way", p=con(P, IdEjecucion=None))
    c(4, "FechaMovimiento missing: a null text is handed to the procedure", p=con(P, FechaMovimiento=None))
    c(5, "the procedure fails: swallowed, false, ROLLBACK reported as success", colab={"procedimiento.lanza": "fallo del procedimiento"})
    c(6, "no parameters at all", p={})
    c(7, "the largest execution id", p=con(P, IdEjecucion="9223372036854775807"))
    c(8, "an execution id above the range of a long", p=con(P, IdEjecucion="9223372036854775808"))
    c(9, "negative execution id", p=con(P, IdEjecucion="-1"))
    c(10, "execution id with leading zeros", p=con(P, IdEjecucion="007"))
    c(11, "execution id with a space: swallowed", p=con(P, IdEjecucion=" 12"))
    c(12, "the date is not validated: any text is handed over", p=con(P, FechaMovimiento="31/03/2025 texto libre"))
    c(13, "zero affected accounts (only printed)", colab={"procedimiento": {"cuentasAfectadas": 0}})
    c(14, "empty texts", p={"FechaMovimiento": "", "IdEjecucion": ""})
    c(15, "unicode date text", p=con(P, FechaMovimiento="☃-ñ"))
    c(16, "extra parameters are ignored", p=con(P, Otro="x", IdEmpresa="1"))


if __name__ == "__main__":
    gen_li()
