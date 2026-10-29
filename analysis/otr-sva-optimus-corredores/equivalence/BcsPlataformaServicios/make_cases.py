#!/usr/bin/env python3
"""Writes the INPUT side of the ConsultarEstadoModulo cases (seed/base-esm.sql and cases/<id>_<slug>/{case.json,seed.sql}).
expected.json is NEVER written here: it is recorded by running the legacy (legacy-oracle/record.sh).
Fake data only (invented empresas 1..9, module codes, user names). Run: py -3 make_cases.py   (idempotent, overwrites case.json/seed.sql only)."""
import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
T = '"BSCL.Plataforma.BD::PAU.Modulo"'
TS = "TIMESTAMP '2025-01-15 10:00:00.0000000'"
DUMP = [{"nombre": "BSCL.Plataforma.BD::PAU.Modulo",
         "orden": '"IdEmpresa", "CodigoModulo", "IdEstado", "FechaUltimaActualizacion"'}]


def q(s):
    return "'" + s.replace("'", "''") + "'"


def ins(emp, code, estado, usuario="fake_user"):
    """One PAU.Modulo row: only the NOT NULL columns of the real table are filled (dependencias de cierre 1..5 empty)."""
    return ('INSERT INTO %s ("IdEmpresa","CodigoModulo","IdEstado","CodigoModuloDependenciaCierre1","CodigoModuloDependenciaCierre2",'
            '"CodigoModuloDependenciaCierre3","CodigoModuloDependenciaCierre4","CodigoModuloDependenciaCierre5","CodigoUsuario",'
            '"FechaUltimaActualizacion") VALUES (%s,%s,%s,\'\',\'\',\'\',\'\',\'\',%s,%s);' % (T, emp, q(code), q(estado), q(usuario), TS))


BASE = [(1, "SIS", "ABT"), (2, "SIS", "CER"), (1, "CON", "ABT"), (2, "CON", "CER"), (1, "TES", "CER"), (2, "TES", "ABT"),
        (4, "SIS", "ABT"), (1, "MRF", "ENT"), (1, "MSP", "SIS")]

UI = {"p_IdEmpresa": 1, "p_IdModulo": "", "p_IdUsuario": 97}   # payload sent by baseapp.js:226-229 every 15 s


def u(**kw):
    d = dict(UI)
    d.update(kw)
    return d


CASES = []


def case(cid, slug, ref, entrada, seed=None, tablas=True, nota=""):
    CASES.append((cid, slug, ref, entrada, seed or [], tablas, nota))


R = "ConsultarEstadoModulo.java"
case("PLA-ESM-01", "empresa1-todos-los-estados-payload-de-la-UI",  # slugs: letters, digits and '-' only (folder names)
 R + ":22-23,109-122; baseapp.js:226-229: payload real de la UI, 5 modulos en estados ABT/CER/ENT/SIS en orden de insercion", u())
case("PLA-ESM-02", "empresa4-un-solo-modulo-abierto", R + ":22-23", u(p_IdEmpresa=4))
case("PLA-ESM-03", "empresa2-modulos-cerrados-y-uno-abierto-no-mezcla-empresas", R + ":22-23 (WHERE IdEmpresa)", u(p_IdEmpresa=2))
case("PLA-ESM-04", "empresa-inexistente-lista-vacia", R + ":117-118 (isEmpty -> lista vacia, contadores 0)", u(p_IdEmpresa=999))
case("PLA-ESM-05", "tabla-sin-filas-lista-vacia", R + ":117-118", u(), ["DELETE FROM " + T + ";"])
case("PLA-ESM-06", "p_IdEmpresa-ausente-lista-vacia", R + ":115 (put null), SQL.java:194 (setNull): IdEmpresa = NULL no coincide", {})
case("PLA-ESM-07", "p_IdEmpresa-null-explicito-lista-vacia", R + ":115; SQL.java:194", u(p_IdEmpresa=None))
case("PLA-ESM-08", "D-ESM-1-p_IdModulo-especifico-se-ignora", R + ":22-23 (sin filtro por modulo)", u(p_IdModulo="TES"))
case("PLA-ESM-09", "D-ESM-1-p_IdModulo-inexistente-se-ignora", R + ":22-23", u(p_IdModulo="ZZZ"))
case("PLA-ESM-10", "D-ESM-1-p_IdModulo-null-y-p_IdUsuario-ausente", R + ":46-50", {"p_IdEmpresa": 1, "p_IdModulo": None})
case("PLA-ESM-11", "D-ESM-1-p_IdUsuario-distinto-se-ignora", R + ":46-50", u(p_IdUsuario=123456789))
case("PLA-ESM-12", "D-ESM-6-claves-de-entrada-distinguen-mayusculas", R + ":64 (Gson, nombres de campo exactos)",
     {"P_IDEMPRESA": 1, "p_idempresa": 1, "IdEmpresa": 1, "extra": {"x": [1, 2]}})
case("PLA-ESM-13", "p_IdEmpresa-texto-numerico-se-acepta", R + ":64 (Gson convierte \"1\" a Long)", u(p_IdEmpresa="1"))
case("PLA-ESM-14", "p_IdEmpresa-1.0-se-acepta", R + ":64 (Gson Long desde 1.0)", u(p_IdEmpresa=1.0))
case("PLA-ESM-15", "D-ESM-5-p_IdEmpresa-fraccion-lanza-NumberFormatException", R + ":64", u(p_IdEmpresa=1.5))
case("PLA-ESM-16", "D-ESM-5-p_IdEmpresa-no-numerico-lanza-excepcion", R + ":64", u(p_IdEmpresa="abc"))
case("PLA-ESM-17", "D-ESM-7-p_IdUsuario-no-numerico-falla-aunque-se-ignore", R + ":64,49", u(p_IdUsuario="abc"))
case("PLA-ESM-18", "D-ESM-7-p_IdModulo-objeto-falla-aunque-se-ignore", R + ":64,48", u(p_IdModulo={"x": 1}))
case("PLA-ESM-19", "p_IdModulo-numerico-se-convierte-a-texto-y-se-ignora", R + ":64", u(p_IdModulo=123))
case("PLA-ESM-20", "p_IdEmpresa-maximo-long", R + ":115; BIGINT(19,0)", u(p_IdEmpresa=9223372036854775807),
     [ins("9223372036854775807", "SIS", "ABT")])
case("PLA-ESM-21", "p_IdEmpresa-cero-y-negativo", R + ":115", u(p_IdEmpresa=0), [ins(0, "SIS", "ABT"), ins(-1, "CON", "CER")])
case("PLA-ESM-22", "variantes-de-acentos-y-mayusculas-se-devuelven-tal-cual", R + ":22 (alias NombreModulo = CodigoModulo, sin normalizar)",
     u(p_IdEmpresa=5), [ins(5, c, "ABT") for c in ["Contabilidad", "CONTABILIDAD", "contabilidad", "Contabilidadá", "Ñandú", "Cámara"]])
case("PLA-ESM-23", "D-ESM-4-estado-sin-validar-dominio", R + ":22 (IdEstado verbatim)", u(p_IdEmpresa=6),
     [ins(6, "A1", "abt"), ins(6, "A2", "XYZ"), ins(6, "A3", "   "), ins(6, "A4", "A"), ins(6, "A5", "ABT")])
case("PLA-ESM-24", "codigo-vacio-y-con-espacios-no-se-recortan", R + ":22", u(p_IdEmpresa=7),
     [ins(7, "", "ABT"), ins(7, " ", "CER"), ins(7, " X ", "ABT")])
case("PLA-ESM-25", "duplicados-misma-empresa-y-codigo-no-se-deduplican", R + ":116-120 (sin DISTINCT) [hipotetico: la PK real es desconocida]",
     u(p_IdEmpresa=3), [ins(3, "SIS", "ABT"), ins(3, "SIS", "ABT"), ins(3, "SIS", "CER")])
case("PLA-ESM-26", "D-ESM-4-columnas-null-usan-ABT-y-vacio-hipotetico", R + ":22 (IFNULL) [hipotetico: en HANA ambas columnas son NOT NULL]", u(p_IdEmpresa=9),
     ['ALTER TABLE ' + T + ' ALTER COLUMN "IdEstado" SET NULL;', 'ALTER TABLE ' + T + ' ALTER COLUMN "CodigoModulo" SET NULL;',
      ins(9, "N1", "CER"),
      'INSERT INTO ' + T + ' ("IdEmpresa","CodigoModulo","IdEstado","CodigoModuloDependenciaCierre1","CodigoModuloDependenciaCierre2",'
      '"CodigoModuloDependenciaCierre3","CodigoModuloDependenciaCierre4","CodigoModuloDependenciaCierre5","CodigoUsuario",'
      '"FechaUltimaActualizacion") VALUES (9,\'N2\',NULL,\'\',\'\',\'\',\'\',\'\',\'fake_user\',' + TS + ');',
      'INSERT INTO ' + T + ' ("IdEmpresa","CodigoModulo","IdEstado","CodigoModuloDependenciaCierre1","CodigoModuloDependenciaCierre2",'
      '"CodigoModuloDependenciaCierre3","CodigoModuloDependenciaCierre4","CodigoModuloDependenciaCierre5","CodigoUsuario",'
      '"FechaUltimaActualizacion") VALUES (9,NULL,\'ABT\',\'\',\'\',\'\',\'\',\'\',\'fake_user\',' + TS + ');',
      'INSERT INTO ' + T + ' ("IdEmpresa","CodigoModulo","IdEstado","CodigoModuloDependenciaCierre1","CodigoModuloDependenciaCierre2",'
      '"CodigoModuloDependenciaCierre3","CodigoModuloDependenciaCierre4","CodigoModuloDependenciaCierre5","CodigoUsuario",'
      '"FechaUltimaActualizacion") VALUES (9,NULL,NULL,\'\',\'\',\'\',\'\',\'\',\'fake_user\',' + TS + ');'])
ORDEN = ["Z9", "A1", "M5", "B2", "Y8", "C3", "X7", "D4", "W6", "E5", "V5", "F6", "U4", "G7", "T3", "H8", "S2", "I9", "R1", "J0", "Q9", "K1", "P8", "L2", "O7"]
case("PLA-ESM-27", "25-modulos-orden-de-insercion-sin-ORDER-BY-D-ESM-3", R + ":22-23 (sin ORDER BY: H2 devuelve insercion, HANA no garantiza)",
     u(p_IdEmpresa=8), [ins(8, c, "ABT" if i % 2 == 0 else "CER") for i, c in enumerate(ORDEN)])
case("PLA-ESM-28", "codigo-de-40-caracteres-y-estado-de-3", R + ":22; NVARCHAR(40)/NVARCHAR(3) reales", u(p_IdEmpresa=10),
     [ins(10, "M" * 40, "ABT"), ins(10, "Á" * 40, "CER")])
case("PLA-ESM-29", "D-ESM-5-error-SQL-tabla-inexistente-se-propaga", R + ":116 (SQL.java:253-254 rethrow con Query y Parameters)", u(),
     ["DROP TABLE " + T + ";"], tablas=False)


def w(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)


def main():
    w(os.path.join(HERE, "seed", "base-esm.sql"),
      "-- base seed of the ConsultarEstadoModulo oracle (fake data, H2 only). Insertion order is deliberately NOT sorted and interleaves empresas.\n"
      + "\n".join(ins(e, c, s) for e, c, s in BASE) + "\n")
    for cid, slug, ref, entrada, seed, tablas, nota in CASES:
        d = os.path.join(HERE, "cases", "%s_%s" % (cid, slug))
        case_json = {"servicio": "cre.ConsultarEstadoModulo", "metodo": "call", "ref": ref, "entrada": entrada,
                     "tablas": DUMP if tablas else []}
        w(os.path.join(d, "case.json"), json.dumps(case_json, indent=2, ensure_ascii=False) + "\n")
        if seed:
            w(os.path.join(d, "seed.sql"), "-- case seed (fake data, H2 only)\n" + "\n".join(seed) + "\n")
    print("cases written:", len(CASES))


if __name__ == "__main__":
    main()
