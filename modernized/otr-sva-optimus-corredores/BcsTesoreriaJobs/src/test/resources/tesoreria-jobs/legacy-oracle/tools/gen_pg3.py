"""GenerarArchivoPago cases, part 3: BCP telecredito, BANCO_ESTADO via the job (GenerarArchivoPagos) and the two helper classes called directly (PG-B*, PG-E*, PG-D*)."""
from gen_common import PROGRAMA, escribir, merge
from gen_pgbase import BASE, NOMINA, fila_detalle, fila_formato, insert, p, proc, seed

V = "BSCL.Tesoreria.BD.Vistas::"


def nomina(**o):
    return insert(V + "ConsultarNominaTransferencia", merge({"IdNominaTransferencia": 48, "IdEmpresa": 1, "IdMedioDePago": "TRA", "Monto": "3000000.00000000",
                  "IdMoneda": "CLP", "NombreBanco": "BANCO ESTADO", "NumeroCuentaBancaria": "000123456789", "FechaLiquidacion": "2025-04-01"}, o))


def det(n, **o):
    return insert(V + "ConsultarNominaTransferenciaDetalle", merge({"IdNominaTransferencia": 48, "IdComprobante": n, "IdEstado": "ING", "IdBancoCorredora": 12,
                  "IdBancoSocio": 12, "DocumentoIdentidad": "12345678-5", "NombrePersona": "JUAN PEREZ", "Monto": "1500000.00000000",
                  "NumeroCuentaBancariaSocio": "00112233", "NombreBancoSocio": "BANCO DE CHILE", "CuentaDCVDestino": "DCV1", "CuentaDCVOrigen": "DCV2",
                  "ClaveDCV": "K" + str(n)}, o))


CTT = insert("BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales", {"IdEmpresa": 1, "RUT": "76123456-7", "RazonSocial": "CORREDORA FICTICIA SA"})
def escala(n):
    return 'ALTER TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" ALTER COLUMN "Monto" SET DATA TYPE NUMERIC(28,%d);' % n


BANCOS = [insert("BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco", {"NombreBanco": "BANCO DE CHILE", "CodigoIso": "CHL"}),
          insert("BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco", {"NombreBanco": "BANCO SANTANDER", "CodigoIso": "SAN"})]
BE_FMT = fila_formato(CodigoFormatoArchivo="BANCO_ESTADO_CLP_TRADVP", FormatoSalida="CSV", NombreArchivoSalida="BE_PAGO", Servicio="SP_NO_SE_USA")


def vistas(*partes):
    return "\n".join(partes)


DEFECTO = vistas(nomina(), det(1), det(2, IdBancoSocio=34, NombreBancoSocio="BANCO SANTANDER", Monto="1500000.00000000"), *BANCOS, CTT)


def be(cid, desc, extra=DEFECTO, formatos=None, **over):
    p(cid, desc, formatos=formatos if formatos is not None else [BE_FMT], detalles=[], proc_=None, extra=extra, **over)


def directo(cid, desc, clase, extra=DEFECTO, archivo=None, **entrada):
    caso = merge(BASE, {"job": clase, "colaboradores": {}, "descripcion": desc, "tablas": [PROGRAMA]})
    caso["entrada"] = merge({"p_IdEmpresa": 1, "p_IdModulo": "TES", "p_Fecha": "2025-04-01", "p_IdNominaTransferencia": 48,
                             "p_Archivo": archivo or {"nombreArchivo": "SALIDA.csv", "extension": "CSV", "ruta": "${RUTA_BASE}/tes/"}}, entrada)
    escribir(cid, caso, "\n".join(["DELETE FROM \"BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo\";", extra]))


def generar():
    be("PG-B01", "BANCO_ESTADO: CSV sections, CCT and ITF payment types, accents stripped, RUT sum, amounts without the 9 trailing characters")
    be("PG-B02", "BANCO_ESTADO code in lower case is the same (equalsIgnoreCase)", formatos=[fila_formato(CodigoFormatoArchivo="banco_estado_clp_tradvp", FormatoSalida="CSV", NombreArchivoSalida="BE_PAGO")])
    be("PG-B03", "BANCO_ESTADO with a TXT format: the CSV writer does not exist, only the plain ' ' lines are written",
       formatos=[fila_formato(CodigoFormatoArchivo="BANCO_ESTADO_CLP_TRADVP", FormatoSalida="TXT", NombreArchivoSalida="BE_PAGO")])
    be("PG-B04", "medio de pago TDP -> payment type DCV", extra=vistas(nomina(IdMedioDePago="TDP"), det(1), *BANCOS, CTT))
    be("PG-B05", "all details ANU -> no detail rows, no file, but nomina and RutaSalida are still updated", extra=vistas(nomina(), det(1, IdEstado="ANU"), *BANCOS, CTT))
    be("PG-B06", "nomina does not exist in the view -> no file", extra=vistas(det(1), *BANCOS, CTT))
    be("PG-B07", "beneficiary bank absent from BAN.ConsultarBanco -> inner join drops the detail -> no file", extra=vistas(nomina(), det(1, NombreBancoSocio="BANCO X"), *BANCOS, CTT))
    be("PG-B08", "no CONTRATO row for the empresa -> LEFT JOIN nulls: ordenante name/RUT print as 'null'", extra=vistas(nomina(), det(1), *BANCOS))
    be("PG-B09", "two CONTRATO rows -> the header block is written twice", extra=vistas(nomina(), det(1), *BANCOS, CTT,
       insert("BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales", {"IdEmpresa": 1, "RUT": "99999999-9", "RazonSocial": "OTRA SA"})))
    be("PG-B10", "TRA to a socio at bank 14 with RUT 97029000-1: the CSBC branch is unreachable (ITF wins)",
       extra=vistas(nomina(), det(1, IdBancoCorredora=12, IdBancoSocio=14, DocumentoIdentidad="97029000-1", NombreBancoSocio="BANCO DE CHILE"), *BANCOS, CTT))
    be("PG-B11", "TRA to a socio at bank 14 (other RUT): the AVAP branch is unreachable (ITF wins)",
       extra=vistas(nomina(), det(1, IdBancoCorredora=12, IdBancoSocio=14), *BANCOS, CTT))
    be("PG-B12", "amount with scale 2 (1500.50): substring(0, length - 9) -> StringIndexOutOfBoundsException (legacy defect)",
       extra=vistas(nomina(), det(1, Monto="1500.50"), *BANCOS, CTT, escala(2)))
    be("PG-B13", "amount with scale 8 and decimals 1500.12345678 -> '1500' (decimals dropped)", extra=vistas(nomina(), det(1, Monto="1500.12345678"), *BANCOS, CTT))
    be("PG-B14", "amount with scale 9 -> keeps a trailing dot '1500.'", extra=vistas(nomina(), det(1, Monto="1500.000000000"), *BANCOS, CTT, escala(9)))
    be("PG-B15", "amount zero scale 8 prints as 0E-8 -> exception", extra=vistas(nomina(), det(1, Monto="0.00000000"), *BANCOS, CTT, escala(8)))
    be("PG-B16", "RUT with dots '12.345.678-9' -> NumberFormatException in the RUT sum", extra=vistas(nomina(), det(1, DocumentoIdentidad="12.345.678-9"), *BANCOS, CTT))
    be("PG-B17", "RUT without hyphen is summed whole", extra=vistas(nomina(), det(1, DocumentoIdentidad="12345678"), *BANCOS, CTT))
    be("PG-B18", "NombrePersona null -> NullPointerException in Normalizer", extra=vistas(nomina(), det(1, NombrePersona=None), *BANCOS, CTT))
    be("PG-B19", "name with n-tilde and umlaut: non-ASCII characters are removed, not transliterated",
       extra=vistas(nomina(), det(1, NombrePersona="ÑANDU ÜBERTO MUÑOZ"), *BANCOS, CTT))
    be("PG-B20", "PLA.FechaSistema 2025-12-31 -> '31-12-2025' in the instruction text", config={"PLATAFORMA/PLA.FechaSistema": "2025-12-31"})
    be("PG-B21", "PLA.FechaSistema not a date -> DateTimeParseException", config={"PLATAFORMA/PLA.FechaSistema": "ayer"})
    be("PG-B22", "50 details -> totals", extra=vistas(nomina(), *[det(i, DocumentoIdentidad="%d-1" % (1000 + i), Monto="%d00.00000000" % i) for i in range(1, 51)], *BANCOS, CTT))
    be("PG-B23", "IdNominaTransferencia of another nomina: nothing found", parametros={"IdNominaTransferencia": "99"})
    be("PG-B24", "detail with NULL optional columns (cuenta, DCV, clave)",
       extra=vistas(nomina(), det(1, NumeroCuentaBancariaSocio=None, CuentaDCVDestino=None, CuentaDCVOrigen=None, ClaveDCV=None), *BANCOS, CTT))
    for k, (cid, desc, clase) in enumerate([("PG-E", "GenerarArchivoPagos", "Pagos"), ("PG-D", "GenerarArchivoPagoBancoEstado (older variant)", "BancoEstado")]):
        directo(cid + "01", desc + " called directly: sections and totals", clase)
        directo(cid + "02", desc + ": no encabezado -> no file", clase, extra=vistas(det(1), *BANCOS, CTT))
        directo(cid + "03", desc + ": amount scale 2", clase, extra=vistas(nomina(), det(1, Monto="1500.50"), *BANCOS, CTT, escala(2)))
        directo(cid + "04", desc + ": TXT extension -> no CSV writer", clase, archivo={"nombreArchivo": "S.txt", "extension": "TXT", "ruta": "${RUTA_BASE}/tes/"})
        directo(cid + "05", desc + ": p_Fecha invalid -> exception", clase, p_Fecha="hoy")
        directo(cid + "06", desc + ": p_Fecha with time part", clase, p_Fecha="2025-04-01 10:00:00")
        directo(cid + "07", desc + ": missing folder (one level) is created", clase, archivo={"nombreArchivo": "S.csv", "extension": "CSV", "ruta": "${RUTA_BASE}/nueva/"})
        directo(cid + "08", desc + ": nothing to write -> no file, empty answer", clase, extra=vistas(nomina(), *BANCOS, CTT))
        directo(cid + "09", desc + ": accented beneficiary", clase, extra=vistas(nomina(), det(1, NombrePersona="JOSÉ NÚÑEZ"), *BANCOS, CTT))
        directo(cid + "10", desc + ": two details, one ANU", clase, extra=vistas(nomina(), det(1), det(2, IdEstado="ANU"), *BANCOS, CTT))
    gen_bcp()


def gen_bcp():
    f = fila_formato(CodigoFormatoArchivo="BCP_PEN_TBC_TELECREDITO", NombreArchivoSalida="TELECREDITO", IdMoneda="PEN", IdMedioDePago="TBC", CantidadDeTiposDeRegistro=3, TipoSaltoLinea="L")
    d = [fila_detalle(1, "Cab", "CHAR", 6, orden=1), fila_detalle(2, "Det", "CHAR", 6, orden=2), fila_detalle(3, "Doc", "CHAR", 6, orden=3)]
    cab = (["TipoRegistroOptimus", "IdInterno", "Cab"], [["1", "0", "H1"]])
    det2 = (["TipoRegistroOptimus", "IdInterno", "Det"], [["2", "10", "D10"], ["2", "20", "D20"]])
    doc3 = (["TipoRegistroOptimus", "IdInterno", "Doc"], [["3", "20", "X20a"], ["3", "10", "X10"], ["3", "20", "X20b"], ["3", "99", "X99"]])
    ov = {"parametros": {"IdMoneda": "PEN", "IdMedioDePago": "TBC"}}
    p("PG-C01", "BCP telecredito: header, each detail followed by its documents (matched by IdInterno); orphan document dropped", formatos=[f], detalles=d, proc_=proc(cab, det2, doc3), **ov)
    p("PG-C02", "BCP: two header rows are both written", formatos=[f], detalles=d, proc_=proc((cab[0], [["1", "0", "H1"], ["1", "0", "H2"]]), det2, doc3), **ov)
    p("PG-C03", "BCP: no detail rows -> only the header", formatos=[f], detalles=d, proc_=proc(cab, (det2[0], []), doc3), **ov)
    p("PG-C04", "BCP: IdInterno missing -> 0 for every row", formatos=[f], detalles=d, proc_=proc(cab, (["TipoRegistroOptimus", "Det"], [["2", "D1"]]), (["TipoRegistroOptimus", "Doc"], [["3", "X"]])), **ov)
    p("PG-C05", "BCP: IdInterno not numeric -> NumberFormatException", formatos=[f], detalles=d, proc_=proc(cab, (det2[0], [["2", "abc", "D"]]), doc3), **ov)
    p("PG-C06", "BCP code in lower case is still the BCP branch (equalsIgnoreCase)", formatos=[dict(f, CodigoFormatoArchivo="bcp_pen_tbc_telecredito")], detalles=d, proc_=proc(cab, det2, doc3), **ov)
    p("PG-C07", "BCP with a CSV format: the BCP branch only writes TXT lines through escribir(String)", formatos=[dict(f, FormatoSalida="CSV")], detalles=d, proc_=proc(cab, det2, doc3), **ov)
    p("PG-C08", "BCP: rows of unknown record type (4) are ignored", formatos=[f], detalles=d + [fila_detalle(4, "Z", "CHAR", 3, orden=4)], proc_=proc(cab, det2, doc3, (["TipoRegistroOptimus", "IdInterno", "Z"], [["4", "10", "Z"]])), **ov)
