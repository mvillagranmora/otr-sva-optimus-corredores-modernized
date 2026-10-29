"""Writes the fresh (never-used) EnvioContabilidad / ReversaPreComprobante cases into a scratch copy.
Usage: python make_fresh.py <scratch>/BcsContabilidadServicios/src/test/resources/envio/cases"""
import json, os, sys

C = sys.argv[1]
P = 'INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES'
A = 'INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES'
EPA = 'INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES'
EV = 'INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES'
MV = 'INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES'
CE = 'INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES'
MD = 'INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES'
G50, G51 = "G" * 50, "H" * 51


def envio(ids, config=None):
    c = {"servicio": "EnvioContabilidad", "descripcion": "fresh",
         "entrada": {"p_IdEmpresa": 1, "p_IdUsuario": 21, "p_IdModulo": "CON", "p_Destino": "B",
                     "p_UsaReglasNegocio": "N", "p_SecCmbte": [{"SecuenciaComprobante": i} for i in ids]}}
    if config:
        c["config"] = config
    return c


def reversa(pares, fecha):
    return {"servicio": "ReversaPreComprobante", "descripcion": "fresh",
            "entrada": {"p_IdEmpresa": 1,
                        "p_IdPreComprobantes": [{"IdPreComprobanteAnt": a, "IdPreComprobante": b} for a, b in pares],
                        "p_FechaContable": fecha, "p_CodigoUsuario": "usr.fresh"}}


CASES = {
    "F01_envio-detallado-monto-maximo-glosa-50-51": (envio([1100]), f"""
{P} (1100, 10, 1, 2025, 'T', '2025-06-30', 'Fresh 1100', '2025-06-30', '23:59:59', 'usr.f', 77, NULL, 'V');
{CE} (1900, 200);
{MV} (17001, 1900, '11111111-1', 1);
{EV} (11001, 17001, 'VENTA', 'CLP', 999999999999.99), (11002, 17001, 'VENTA', 'CLP', -999999999999.99);
{A} (11101, 1, 1100, '1201', 2025, 1, 999999999999.99, 0, '2025-06-30', 'Linea maxima', 'V'), (11102, 1, 1100, '4101', 2025, 2, 0, 999999999999.99, '2025-06-30', 'Linea maxima', 'V');
{EPA} (11101, 11001, '+', 1, 'V', '{G50}'), (11102, 11002, '-', 2, 'V', '{G51}');
{MD} (17001, 'EsContraparte', 'NO', NULL, 'V', ''), (17001, 'Nemo', 'FRESH-1', NULL, 'V', ''), (17001, 'Bpl', '1', NULL, 'V', ''), (17001, 'FechaLiq', '20250701', NULL, 'V', ''), (17001, 'Referencia', 'R-F01', NULL, 'V', '');
"""),
    "F02_envio-resumido-centavo-y-ambos-lados": (envio([1200]), f"""
{P} (1200, 20, 1, 2025, 'T', '2025-01-01', 'Fresh 1200', '2025-01-01', '00:00:00', 'usr.f', 1, NULL, 'D');
{A} (12001, 1, 1200, '1101', 2025, 1, 0.01, 0, '2025-01-01', 'Un centavo', 'V'), (12002, 1, 1200, '2101', 2025, 2, 0, 0.01, '2025-01-01', 'Un centavo', 'V'), (12003, 1, 1200, '4101', 2025, 3, 3, 3, '2025-01-01', 'Ambos lados', 'V');
"""),
    "F03_envio-contraparte-y-proveedor": (envio([1300]), f"""
{P} (1300, 10, 1, 2025, 'T', '2025-02-28', 'Fresh 1300', '2025-02-28', '12:00:00', 'usr.f', 3, NULL, 'V');
{CE} (1901, 200);
{MV} (17301, 1901, '22222222-2', 2), (17302, 1901, '33333333-3', 3);
{EV} (13001, 17301, 'PAGO', 'CLP', 250), (13002, 17302, 'PAGO', 'CLP', -250);
{A} (13101, 1, 1300, '1201', 2025, 1, 250, 0, '2025-02-28', 'Contraparte', 'V'), (13102, 1, 1300, '2101', 2025, 2, 0, 250, '2025-02-28', 'Proveedor', 'V');
{EPA} (13101, 13001, '+', 1, 'V', 'Pago contraparte'), (13102, 13002, '-', 2, 'V', 'Pago proveedor');
{MD} (17301, 'EsContraparte', 'SI', NULL, 'V', ''), (17302, 'EsContraparte', 'NO', NULL, 'V', ''), (17302, 'EsProveedor', 'SI', NULL, 'V', ''), (17301, 'Nemo', 'N-3', NULL, 'V', '');
"""),
    "F04_envio-sin-centro-costo-config-N": (envio([1400], {"CONTABILIDAD/UsaCentroCosto": "N"}), f"""
{P} (1400, 10, 1, 2025, 'T', '2025-03-31', 'Fresh 1400', '2025-03-31', '08:00:00', 'usr.f', 4, NULL, 'V');
{CE} (1902, 200);
{MV} (17401, 1902, '44444444-4', 4);
{EV} (14001, 17401, 'GASTO', 'CLP', 12.34), (14002, 17401, 'GASTO', 'CLP', -12.34);
{A} (14101, 1, 1400, '5101', 2025, 1, 12.34, 0, '2025-03-31', 'Gasto', 'V'), (14102, 1, 1400, '4101', 2025, 2, 0, 12.34, '2025-03-31', 'Ingreso', 'V');
{EPA} (14101, 14001, '+', 1, 'V', 'Gasto bancario'), (14102, 14002, '-', 2, 'V', 'Ingreso comision');
{MD} (17401, 'EsContraparte', 'NO', NULL, 'V', ''), (17401, 'CostingCode', 'CC-Q', NULL, 'V', '');
"""),
    "F05_envio-orden-inusual-repetidos-inexistente": (envio([1502, 0, 1501, 1502, 1502, 123456789]), f"""
{P} (1501, 30, 1, 2025, 'T', '2025-05-05', 'Fresh 1501 inactivo', '2025-05-05', '10:00:00', 'usr.f', 5, NULL, 'V'), (1502, 20, 1, 2025, 'T', '2025-05-06', 'Fresh 1502', '2025-05-06', '10:00:00', 'usr.f', 5, NULL, 'V');
{A} (15011, 1, 1501, '1101', 2025, 1, 10, 0, '2025-05-05', 'Inactivo', 'V'), (15021, 1, 1502, '1101', 2025, 1, 7, 0, '2025-05-06', 'R1', 'V'), (15022, 1, 1502, '2101', 2025, 2, 0, 7, '2025-05-06', 'R2', 'V');
"""),
    "F06_envio-usd-paridad-sin-montoclp": (envio([1600], {"CONTABILIDAD/CalculaParidadCLPCentralizacionTesoreria": "S"}), f"""
{P} (1600, 10, 1, 2025, 'T', '2025-07-15', 'Fresh 1600', '2025-07-15', '16:30:00', 'usr.f', 6, NULL, 'V');
{CE} (1906, 210);
{MV} (17601, 1906, '55555555-5', 5), (17602, 1906, '66666666-6', 6);
{EV} (16001, 17601, 'PAGO', 'USD', 1.01), (16002, 17602, 'PAGO', 'USD', -1.01);
{A} (16101, 1, 1600, '1101', 2025, 1, 1.01, 0, '2025-07-15', 'USD a', 'V'), (16102, 1, 1600, '5101', 2025, 2, 0, 1.01, '2025-07-15', 'USD b', 'V');
{EPA} (16101, 16001, '+', 1, 'V', 'USD con clp'), (16102, 16002, '-', 2, 'V', 'USD sin clp');
{MD} (17601, 'MontoCLP', NULL, -960.5, 'V', '');
"""),
    "F07_envio-cuentas-sys-con-centro-costo": (envio([1700], {"CONTABILIDAD/UsaCuentasSYS": "S"}), f"""
{P} (1700, 10, 1, 2025, 'T', '2025-08-01', 'Fresh 1700', '2025-08-01', '09:09:09', 'usr.f', 7, NULL, 'V');
{CE} (1907, 200);
{MV} (17701, 1907, '77777777-7', 7);
{EV} (17001, 17701, 'VENTA', 'CLP', 100), (17002, 17701, 'VENTA', 'CLP', -100);
{A} (17101, 1, 1700, '1201', 2025, 1, 100, 0, '2025-08-01', 'SYS cli', 'V'), (17102, 1, 1700, '4101', 2025, 2, 0, 100, '2025-08-01', 'SYS ing', 'V');
{EPA} (17101, 17001, '+', 1, 'V', 'Venta sys'), (17102, 17002, '-', 2, 'V', 'Ingreso sys');
{MD} (17701, 'EsContraparte', 'NO', NULL, 'V', ''), (17701, 'CostingCode', 'CC-S', NULL, 'V', '');
"""),
    "F08_envio-estado-E-eur": (envio([1800]), f"""
{P} (1800, 10, 1, 2025, 'T', '2025-09-09', 'Fresh 1800', '2025-09-09', '19:00:00', 'usr.f', 8, NULL, 'E');
{CE} (1908, 200);
{MV} (17801, 1908, '88888888-8', 8);
{EV} (18001, 17801, 'COMPRA', 'EUR', -42.42), (18002, 17801, 'COMPRA', 'EUR', 42.42);
{A} (18101, 1, 1800, '1101', 2025, 1, 42.42, 0, '2025-09-09', 'EUR a', 'V'), (18102, 1, 1800, '1201', 2025, 2, 0, 42.42, '2025-09-09', 'EUR b', 'V');
{EPA} (18101, 18001, '+', 1, 'V', 'Compra euros'), (18102, 18002, '-', 2, 'V', 'Compra euros cliente');
{MD} (17801, 'MontoCLP', NULL, 40000, 'V', ''), (17801, 'EsContraparte', 'NO', NULL, 'V', '');
"""),
    "F09_envio-detallado-linea-cero-sin-homologar": (envio([1910]), f"""
{P} (1910, 10, 1, 2025, 'T', '2025-10-01', 'Fresh 1910', '2025-10-01', '01:02:03', 'usr.f', 9, NULL, 'V');
{CE} (1909, 200);
{MV} (17901, 1909, '99999999-9', 9);
{EV} (19001, 17901, 'VENTA', 'CLP', 0), (19002, 17901, 'VENTA', 'CLP', 5);
{A} (19101, 1, 1910, '1201', 2025, 1, 0, 0, '2025-10-01', 'Cero', 'V'), (19102, 1, 1910, '9998', 2025, 2, 5, 0, '2025-10-01', 'Sin homologar', 'V');
{EPA} (19101, 19001, '+', 1, 'V', 'Cero'), (19102, 19002, '+', 2, 'V', 'Sin homologar');
{MD} (17901, 'EsContraparte', 'NO', NULL, 'V', '');
"""),
    "F10_reversa-resumido-tres-lineas": (reversa([(2000, 0)], "2025-12-31"), f"""
{P} (2000, 20, 1, 2025, 'T', '2025-12-30', 'Fresh 2000', '2025-12-30', '18:00:00', 'usr.o', 20, NULL, 'V');
{A} (20001, 1, 2000, '1101', 2025, 1, 0.01, 0, '2025-12-30', 'A', 'V'), (20002, 1, 2000, '2101', 2025, 2, 0, 999999999999.99, '2025-12-30', 'B', 'V'), (20003, 1, 2000, '4101', 2025, 3, 999999999999.98, 0, '2025-12-30', 'C', 'V');
"""),
    "F11_reversa-dos-con-id-explicito-y-secuencia": (reversa([(2102, 7777), (2101, 0)], "2026-01-02"), f"""
{P} (2101, 10, 1, 2025, 'T', '2025-11-11', 'Fresh 2101', '2025-11-11', '11:11:11', 'usr.o', 21, NULL, 'V'), (2102, 20, 1, 2025, 'T', '2025-11-12', 'Fresh 2102', '2025-11-12', '12:12:12', 'usr.o', 22, NULL, 'V');
{CE} (2190, 200);
{MV} (21901, 2190, '12121212-1', 12);
{EV} (21001, 21901, 'VENTA', 'CLP', 33), (21002, 21901, 'VENTA', 'CLP', -33);
{A} (21011, 1, 2101, '1201', 2025, 1, 33, 0, '2025-11-11', 'X', 'V'), (21012, 1, 2101, '4101', 2025, 2, 0, 33, '2025-11-11', 'Y', 'V'), (21021, 1, 2102, '1101', 2025, 1, 4, 0, '2025-11-12', 'Z', 'V'), (21022, 1, 2102, '2101', 2025, 2, 0, 4, '2025-11-12', 'W', 'V');
{EPA} (21011, 21001, '+', 1, 'V', 'rev x'), (21012, 21002, '-', 2, 'V', 'rev y');
{MD} (21901, 'EsContraparte', 'NO', NULL, 'V', '');
"""),
    "F12_reversa-glosa-vacia-estado-D": (reversa([(2200, 0)], "2025-02-28"), f"""
{P} (2200, 20, 1, 2025, 'T', '2025-02-27', '', '2025-02-27', '07:00:00', 'usr.o', 23, NULL, 'D');
{A} (22001, 1, 2200, '1101', 2025, 1, 50, 0, '2025-02-27', '', 'V'), (22002, 1, 2200, '2101', 2025, 2, 0, 50, '2025-02-27', '', 'V');
"""),
    "F13_envio-dos-precomprobantes-misma-cuenta": (envio([1310, 1311]), f"""
{P} (1310, 10, 1, 2025, 'T', '2025-04-04', 'Fresh 1310', '2025-04-04', '04:04:04', 'usr.f', 31, NULL, 'V'), (1311, 20, 1, 2025, 'T', '2025-04-05', 'Fresh 1311', '2025-04-05', '05:05:05', 'usr.f', 32, NULL, 'V');
{CE} (1931, 200);
{MV} (13101, 1931, '13131313-1', 13), (13102, 1931, '14141414-1', 14);
{EV} (31001, 13101, 'VENTA', 'CLP', 10), (31002, 13102, 'VENTA', 'CLP', 20), (31003, 13101, 'VENTA', 'CLP', -30);
{A} (31101, 1, 1310, '1201', 2025, 1, 30, 0, '2025-04-04', 'Dos movs', 'V'), (31102, 1, 1310, '4101', 2025, 2, 0, 30, '2025-04-04', 'Ing', 'V'), (31111, 1, 1311, '1101', 2025, 1, 8, 0, '2025-04-05', 'Res', 'V'), (31112, 1, 1311, '1101', 2025, 2, 0, 8, '2025-04-05', 'Res', 'V');
{EPA} (31101, 31001, '+', 1, 'V', 'mov a'), (31101, 31002, '+', 1, 'V', 'mov b'), (31102, 31003, '-', 2, 'V', 'ing');
{MD} (13101, 'EsContraparte', 'NO', NULL, 'V', ''), (13102, 'EsContraparte', 'SI', NULL, 'V', ''), (13101, 'Nemo', 'N13', NULL, 'V', '');
"""),
    # verify5 (2026-10-15): two more inputs nobody used before
    "F14_envio-resumido-linea-en-cero-y-cuatro-lineas": (envio([2310]), f"""
{P} (2310, 20, 1, 2025, 'T', '2025-06-01', 'Fresh 2310', '2025-06-01', '06:06:06', 'usr.v5', 41, NULL, 'V');
{A} (23101, 1, 2310, '1101', 2025, 1, 0, 0, '2025-06-01', 'Linea cero', 'V'), (23102, 1, 2310, '1201', 2025, 2, 1000000, 0, '2025-06-01', 'Debe grande', 'V'), (23103, 1, 2310, '2101', 2025, 3, 0, 999999.99, '2025-06-01', 'Haber', 'V'), (23104, 1, 2310, '4101', 2025, 4, 0, 0.01, '2025-06-01', 'Centavo', 'V');
"""),
    "F15_reversa-detallado-dos-eventos-por-linea": (reversa([(2410, 8888)], "2025-09-30"), f"""
{P} (2410, 10, 1, 2025, 'T', '2025-09-29', 'Fresh 2410', '2025-09-29', '22:22:22', 'usr.v5', 42, NULL, 'V');
{CE} (2490, 200);
{MV} (24901, 2490, '24242424-2', 24);
{EV} (24001, 24901, 'VENTA', 'CLP', 70), (24002, 24901, 'VENTA', 'CLP', 30), (24003, 24901, 'VENTA', 'CLP', -100);
{A} (24101, 1, 2410, '1201', 2025, 1, 100, 0, '2025-09-29', 'Dos eventos', 'V'), (24102, 1, 2410, '4101', 2025, 2, 0, 100, '2025-09-29', 'Ingreso', 'V');
{EPA} (24101, 24001, '+', 1, 'V', 'ev a'), (24101, 24002, '+', 1, 'V', 'ev b'), (24102, 24003, '-', 2, 'V', 'ing');
{MD} (24901, 'EsContraparte', 'NO', NULL, 'V', '');
"""),
}

for name, (case, seed) in CASES.items():
    d = os.path.join(C, name)
    os.makedirs(d, exist_ok=True)
    case["descripcion"] = "Fresh verify input " + name
    with open(os.path.join(d, "case.json"), "w", encoding="utf-8") as f:
        json.dump(case, f, indent=2)
    with open(os.path.join(d, "seed.sql"), "w", encoding="utf-8") as f:
        f.write("-- Fresh verify input (synthetic data).\n" + seed.strip() + "\n")
print(len(CASES), "cases written")
