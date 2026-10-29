"""FRESH inputs: PG-X* (GenerarArchivoPago generic), PG-BX* (BANCO_ESTADO via the job), PG-EX* / PG-DX* (direct helpers), PG-CX* (BCP)."""
import sys
sys.path.insert(0, r"T:\otr\BcsTesoreriaJobs\src\test\resources\tesoreria-jobs\legacy-oracle\tools")
import gen_pg3 as g3
from gen_pgbase import fila_detalle as fd, fila_formato as ff, p, proc
from gen_pg3 import BANCOS, CTT, be, det, directo, nomina, vistas

TR = ["TipoRegistroOptimus", "Rut", "Monto"]


def generar_generico():
    p("PG-X01", "FRESH other bank 33, DEC 13,2 largest / 0.01 / 100.05 / 0.10",
      formatos=[ff(IdBanco=33, NombreBanco="OTRO BANCO")], parametros={"IdBanco": "33"},
      detalles=[fd(1, "Rut", "CHAR", 12, orden=1), fd(1, "Monto", "DEC", 13, 2, orden=2)],
      proc_=proc((TR, [["1", "99999999-9", "9999999999.99"], ["1", "1-9", "0.01"], ["1", "2-7", "100.05"], ["1", "3-5", "0.10"]])))
    p("PG-X02", "FRESH DEC separator ',' with decimal parts 5 / 05 / 50 and a field order given by Orden (Monto before Rut)",
      formatos=[fila for fila in [ff(SeparadorDecimales=",")]],
      detalles=[fd(1, "Rut", "CHAR", 10, orden=2), fd(1, "Monto", "DEC", 10, 2, orden=1)],
      proc_=proc((TR, [["1", "5-1", "7.5"], ["1", "6-1", "7.05"], ["1", "7-1", "7.50"]])))
    p("PG-X03", "FRESH CSV, LF, DATE ddMMyyyy: leap day 2024-02-29, 2025-12-31, 2025-01-01",
      formatos=[ff(FormatoSalida="CSV", TipoSaltoLinea="L", RellenarCampo="N")],
      detalles=[fd(1, "Rut", "CHAR", 10, orden=1), fd(1, "Monto", "DATE", 8, formato="ddMMyyyy", orden=2)],
      proc_=proc((TR, [["1", "a", "2024-02-29"], ["1", "b", "2025-12-31"], ["1", "c", "2025-01-01"]])))
    p("PG-X04", "FRESH IdNominaTransferencia 123456789 (large), medio CHE, 1 row, empty NUM takes default 0",
      formatos=[ff(IdMedioDePago="CHE")], parametros={"IdNominaTransferencia": "123456789", "IdMedioDePago": "CHE"},
      detalles=[fd(1, "Rut", "NUM", 6, defecto="0", orden=1), fd(1, "Monto", "DEC", 8, 2, orden=2)],
      proc_=proc((TR, [["1", "", "0.00"]])),
      extra="UPDATE \"BSCL.Tesoreria.BD::TES.NominaTransferencia\" SET \"IdNominaTransferencia\" = 123456789;")
    p("PG-X05", "FRESH 2 record types, the second set listed first, CRLF, '~' separator",
      formatos=[ff(CantidadDeTiposDeRegistro=2, SeparadorCampos="~")],
      detalles=[fd(1, "Rut", "CHAR", 6, orden=1), fd(2, "Cta", "NUM", 6, orden=2), fd(2, "Monto", "DEC", 9, 3, orden=3)],
      proc_=proc((["TipoRegistroOptimus", "Cta", "Monto"], [["2", "77", "3.141"], ["2", "88", "0.001"]]), (["TipoRegistroOptimus", "Rut"], [["1", "9-9"]])))
    p("PG-X06", "FRESH blank CHAR takes default 'N/A', accented value truncated, 25 rows",
      detalles=[fd(1, "Rut", "CHAR", 5, defecto="N/A", orden=1), fd(1, "Monto", "DEC", 10, 2, orden=2)],
      proc_=proc((TR, [["1", "   " if i % 5 == 0 else "A\u00d1O%02d" % i, "%d.%d" % (i * 11, i % 10)] for i in range(25)])))
    p("PG-X07", "FRESH XLS-like unknown format 'csv' lower case with DAT-style name and ';' in the data", formatos=[ff(FormatoSalida="csv", NombreArchivoSalida="MIX")],
      proc_=proc((TR, [["1", "x;y", "10.25"], ["1", "z", "0.50"]])))


def generar_be():
    L = lambda n, **o: det(n, **o)
    be("PG-BX01", "FRESH BANCO_ESTADO: amounts 123456789012.12345678 and 0.00000100 and 1.00000000, bank 37 beneficiary",
       extra=vistas(nomina(Monto="123456789013.12345679"), L(1, Monto="123456789012.12345678", IdBancoSocio=37, NombreBancoSocio="BANCO SANTANDER"),
                    L(2, Monto="0.00000100"), L(3, Monto="1.00000000"), *BANCOS, CTT))
    be("PG-BX02", "FRESH BANCO_ESTADO: RUTs with K and short digits, details in descending comprobante order",
       extra=vistas(nomina(), L(9, DocumentoIdentidad="9876543-K"), L(5, DocumentoIdentidad="1-9"), L(2, DocumentoIdentidad="12345678-5"), *BANCOS, CTT))
    be("PG-BX03", "FRESH BANCO_ESTADO: 7 details, 3 ANU, medio TDP, other account numbers",
       extra=vistas(nomina(IdMedioDePago="TDP"), *[L(i, IdEstado="ANU" if i % 2 == 0 else "ING", NumeroCuentaBancariaSocio="9%07d" % i,
                    Monto="%d.00000000" % (i * 250000)) for i in range(1, 8)], *BANCOS, CTT))
    be("PG-BX04", "FRESH BANCO_ESTADO: lower-case accents and apostrophe in the name",
       extra=vistas(nomina(), L(1, NombrePersona="mar\u00eda o'higgins \u00f1uble"), *BANCOS, CTT))
    be("PG-BX05", "FRESH BANCO_ESTADO: PLA.FechaSistema 2024-02-29 (leap day) and a nomina of another currency/account",
       config={"PLATAFORMA/PLA.FechaSistema": "2024-02-29"}, extra=vistas(nomina(IdMoneda="USD", NumeroCuentaBancaria="000999888777"), L(1), *BANCOS, CTT))


def generar_directo():
    for cid, clase in (("PG-EX", "Pagos"), ("PG-DX", "BancoEstado")):
        directo(cid + "01", "FRESH direct, p_Fecha leap day 2024-02-29, empresa 1, 3 details with three amounts", clase, p_Fecha="2024-02-29",
                extra=vistas(nomina(), L2(1, "100.00000000"), L2(2, "0.01000000"), L2(3, "99999999.99999999"), *BANCOS, CTT))
        directo(cid + "02", "FRESH direct, nomina 777 other file name, bank 34", clase, p_IdNominaTransferencia=777,
                archivo={"nombreArchivo": "OTRO_ARCH.csv", "extension": "CSV", "ruta": "${RUTA_BASE}/tes/"},
                extra=vistas(nomina(IdNominaTransferencia=777), det(1, IdNominaTransferencia=777, IdBancoSocio=34, NombreBancoSocio="BANCO SANTANDER"), *BANCOS, CTT))


def L2(n, monto):
    return det(n, Monto=monto)


def generar_bcp():
    f = ff(CodigoFormatoArchivo="BCP_PEN_TBC_TELECREDITO", NombreArchivoSalida="TELECREDITO", IdMoneda="PEN", IdMedioDePago="TBC", CantidadDeTiposDeRegistro=3, TipoSaltoLinea="W")
    d = [fd(1, "Cab", "CHAR", 8, orden=1), fd(2, "Det", "CHAR", 8, orden=2), fd(3, "Doc", "CHAR", 8, orden=3)]
    ov = {"parametros": {"IdMoneda": "PEN", "IdMedioDePago": "TBC"}}
    p("PG-CX01", "FRESH BCP: details 30/10/20 each with documents in another order, CRLF, two orphan docs",
      formatos=[f], detalles=d, **ov,
      proc_=proc((["TipoRegistroOptimus", "IdInterno", "Cab"], [["1", "0", "HEAD"]]),
                 (["TipoRegistroOptimus", "IdInterno", "Det"], [["2", "30", "D30"], ["2", "10", "D10"], ["2", "20", "D20"]]),
                 (["TipoRegistroOptimus", "IdInterno", "Doc"], [["3", "10", "a"], ["3", "20", "b"], ["3", "30", "c"], ["3", "10", "d"], ["3", "5", "x"], ["3", "31", "y"]])))


def generar():
    generar_generico()
    generar_be()
    generar_directo()
    generar_bcp()
