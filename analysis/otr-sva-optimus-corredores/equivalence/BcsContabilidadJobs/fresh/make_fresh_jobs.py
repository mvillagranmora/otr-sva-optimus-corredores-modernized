"""Writes the fresh (never-used) InterfazContable job inputs JF01..JF10 into a scratch copy of the Servicios envio
cases folder, so the legacy OracleRunner (prefix JF) and the Jobs driver read the same inputs.
Synthetic data only: the seed is CONT-B-45's synthetic seed plus a third pre-voucher (300) with its own partner.
Usage: py -3 make_fresh_jobs.py <scratch>/BcsContabilidadServicios/src/test/resources/envio/cases"""
import json, os, sys

C = sys.argv[1]
base_seed = open(os.path.join(C, 'CONT-B-45_sap-lote-ok-y-error', 'seed.sql'), encoding='utf-8').read()

# third pre-voucher 300 (Detallado, partner CL77000000-0)
seed = base_seed.replace(
    " (555, 10, 1, 2025, 'T', '2025-03-10', 'Precomprobante 555', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V');",
    " (555, 10, 1, 2025, 'T', '2025-03-10', 'Precomprobante 555', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V'),\n"
    " (300, 10, 1, 2025, 'T', '2025-03-12', 'Precomprobante 300', '2025-03-12', '09:00:00', 'usr.original', 55, NULL, 'V');")
seed = seed.replace(
    " (200, '20250311', 'Resumen Diario Tesoreria-200', '20250311', '20250311', 200, 210);",
    " (200, '20250311', 'Resumen Diario Tesoreria-200', '20250311', '20250311', 200, 210),\n"
    " (300, '20250312', 'PROPIA-COMPRA FRESH-300', '20250312', '20250312', 300, 200);")
lineas = [l for l in seed.splitlines() if 'PreComprobanteAsientoEnvioContabilidad" (' in l]
assert lineas, 'asiento insert template not found'
t1 = lineas[0]
debe = (t1.replace("VALUES (1, 10, 100, 100, 0, '', 'Venta'", "VALUES (5, 10, 300, 777.77, 0, '', 'Compra fresh'")
          .replace("'CL76123456-7'", "'CL77000000-0'").replace("'76123456-7'", "'77000000-0'")
          .replace("'Cliente Uno SpA'", "'Cliente Fresh Ltda'").replace("'20250310'", "'20250312'"))
haber = t1.replace("VALUES (1, 10, 100, 100, 0, '', 'Venta'", "VALUES (6, 10, 300, 0, 777.77, '', 'Contrapartida fresh'") \
          .replace("'SAP-1201'", "'SAP-4101'").replace("'CL76123456-7'", "''").replace("'CLIENTE'", "''") \
          .replace("'76123456-7'", "''").replace("'Cliente Uno SpA'", "''").replace("'20250310'", "'20250312'")
assert debe != t1 and haber != t1
seed = seed.rstrip() + '\n' + debe + '\n' + haber + '\n'

LOGIN_OK = {"status": 200, "body": "{\"SessionId\":\"fake-session-0001\",\"Version\":\"1000000\",\"SessionTimeout\":30}",
            "setCookies": ["B1SESSION=fake-session-0001; HttpOnly", "ROUTEID=.node1; path=/b1s"]}
BP76 = "GET /b1s/v1/BusinessPartners('CL76123456-7')"
BP77 = "GET /b1s/v1/BusinessPartners('CL77000000-0')"
ADD = "POST /b1s/v1/JournalVouchersService_Add"
NEWBP = "POST /b1s/v1/BusinessPartners"
OK = {"status": 204}
E5002 = {"status": 400, "body": "{\"error\": {\"code\": -5002, \"message\": {\"lang\": \"en-us\", \"value\": \"Invalid account code 'SAP-1101'\"}}}"}
E1000 = {"status": 400, "body": "{\"error\": {\"code\": -1000, \"message\": {\"lang\": \"en-us\", \"value\": \"Field too long\"}}}"}
EXISTE = {"status": 200, "body": "{\"CardCode\":\"X\"}"}
NOEXISTE = {"status": 404, "body": "{\"error\": {\"code\": -2028, \"message\": {\"lang\": \"en-us\", \"value\": \"No matching records found\"}}}"}


def caso(secuencias, sap, descripcion):
    return {"servicio": "InterfazContable", "descripcion": "Fresh verify input (job shell): " + descripcion,
            "entrada": {"idEmpresa": 1, "idUsuario": 21, "destino": "B", "secuencias": secuencias}, "sap": sap}


CASES = {
    "JF01_tres-ok": caso("100;200;300", {"POST /b1s/v1/Login": [LOGIN_OK], BP76: [EXISTE], BP77: [EXISTE], ADD: [OK, OK, OK]}, "three accepted"),
    "JF02_error-ok-error": caso("100;200;300", {"POST /b1s/v1/Login": [LOGIN_OK], BP76: [EXISTE], BP77: [EXISTE], ADD: [E5002, OK, E1000]}, "first and last rejected"),
    "JF03_login-reintento": caso("200", {"POST /b1s/v1/Login": [{"status": 500, "body": "{}"}, LOGIN_OK], ADD: [OK]}, "login fails once then succeeds"),
    "JF04_id-inexistente-en-medio": caso("100;999;200", {"POST /b1s/v1/Login": [LOGIN_OK], BP76: [EXISTE], ADD: [OK, OK]}, "unknown id between two valid ones"),
    "JF05_socio-nuevo-creado": caso("300", {"POST /b1s/v1/Login": [LOGIN_OK], BP77: [NOEXISTE], NEWBP: [{"status": 201, "body": "{\"CardCode\":\"CL77000000-0\"}"}], ADD: [OK]}, "partner created then voucher accepted"),
    "JF06_socio-creacion-falla": caso("300", {"POST /b1s/v1/Login": [LOGIN_OK], BP77: [NOEXISTE], NEWBP: [{"status": 400, "body": "{\"error\": {\"code\": -10, \"message\": {\"lang\": \"en-us\", \"value\": \"bad partner\"}}}"}], ADD: [OK]}, "partner creation rejected"),
    "JF07_ids-repetidos": caso("100;100", {"POST /b1s/v1/Login": [LOGIN_OK], BP76: [EXISTE], ADD: [OK, OK]}, "same id twice"),
    "JF08_reintentos-5xx": caso("200", {"POST /b1s/v1/Login": [LOGIN_OK], ADD: [{"status": 500, "body": "{}"}, {"status": 502, "body": "{}"}, OK]}, "two 5xx then accepted"),
    "JF09_ok-y-campo-largo": caso("100;300", {"POST /b1s/v1/Login": [LOGIN_OK], BP76: [EXISTE], BP77: [EXISTE], ADD: [OK, E1000]}, "second rejected for field length"),
    "JF10_orden-inverso-con-punto-y-coma-final": caso("300;200;100;", {"POST /b1s/v1/Login": [LOGIN_OK], BP76: [EXISTE], BP77: [EXISTE], ADD: [OK, OK, OK]}, "reverse order, trailing semicolon"),
    # verify2 (2026-10-16): two more inputs nobody used before
    "JF11_login-tres-fallos-luego-ok": caso("200;300", {"POST /b1s/v1/Login": [{"status": 401, "body": "{}"}, {"status": 503, "body": "{}"}, {"status": 500, "body": "{}"}, LOGIN_OK], BP77: [EXISTE], ADD: [OK, OK]}, "login fails three times then succeeds"),
    "JF12_verificar-socio-falla-5xx": caso("100;300", {"POST /b1s/v1/Login": [LOGIN_OK], BP76: [EXISTE], BP77: [{"status": 500, "body": "{}"}], ADD: [OK, OK]}, "partner check answers 500 for the second voucher"),
}

for name, c in CASES.items():
    d = os.path.join(C, name)
    os.makedirs(d, exist_ok=True)
    with open(os.path.join(d, 'case.json'), 'w', encoding='utf-8') as f:
        json.dump(c, f, indent=2)
    with open(os.path.join(d, 'seed.sql'), 'w', encoding='utf-8', newline='\n') as f:
        f.write(seed)
print(len(CASES), 'cases written')
