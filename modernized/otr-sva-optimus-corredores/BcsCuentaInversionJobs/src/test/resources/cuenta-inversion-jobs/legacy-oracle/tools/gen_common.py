"""Shared helpers of the case generators. A case is the INPUT (case.json + optional seed.sql); the golden (expected.json) is RECORDED by
legacy-oracle/record.sh from the unmodified legacy classes: never write an expected value here."""
import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
CASES = os.path.normpath(os.path.join(HERE, "..", "..", "cases"))

PROGRAMA = {"nombre": "BSCL.Plataforma.BD::BAT.Programa", "orden": "\"IdPrograma\""}
MARCA = {"nombre": "TEST.Marca", "orden": "\"Orden\""}
TIPO_EJEC = {"nombre": "BSCL.Plataforma.BD::BAT.TipoProgramaEjecucion", "orden": "\"IdTipoPrograma\""}

# fake-looking, same-shape values only (no value of the legacy code is a fixture)
EMPRESA = "1"
USUARIO = "21"


def msg(tipo, codigo, descripcion, id=1):
    return {"id": id, "tipo": tipo, "codigo": codigo, "descripcion": descripcion}


def ok(**extra):
    d = {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_MENSAJES": []}
    d.update(extra)
    return d


def err(n=1, mensajes=None, **extra):
    m = mensajes if mensajes is not None else [msg("ERROR", "CTA-E%03d" % i, "Error simulado %d" % i, i) for i in range(1, n + 1)]
    d = {"P_OUT_ERRORESCONTADOR": n, "P_OUT_MENSAJES": m}
    d.update(extra)
    return d


def marca(texto):
    """The side effect a real service would leave in the database: one row, so commit / rollback are visible in the data."""
    return "INSERT INTO \"TEST.Marca\" (\"Valor\") VALUES ('%s')" % texto


def svc(nombre, salida=None, sql=None, lanza=None, **extra):
    """colaboradores entries of one scripted service."""
    c = {}
    d = {}
    if salida is not None:
        d["salida"] = salida
    if sql:
        d["sql"] = sql if isinstance(sql, list) else [sql]
    d.update(extra)
    if d:
        c[nombre] = d
    if lanza:
        c[nombre + ".lanza"] = lanza
    return c


def mezclar(*partes):
    out = {}
    for p in partes:
        out.update(p)
    return out


def escribir(id, job, descripcion, parametros=None, colaboradores=None, config=None, dirs=None, seed=None, tablas=None, reloj=None,
             idPrograma=None, extra_tablas=None):
    d = os.path.join(CASES, id)
    os.makedirs(d, exist_ok=True)
    c = {"job": job}
    if idPrograma is not None:
        c["idPrograma"] = idPrograma
    if dirs:
        c["dirs"] = dirs
    if reloj:
        c["reloj"] = reloj
    c["parametros"] = parametros if parametros is not None else {}
    if config:
        c["config"] = config
    if colaboradores:
        c["colaboradores"] = colaboradores
    t = tablas if tablas is not None else [PROGRAMA, MARCA]
    c["tablas"] = list(t) + list(extra_tablas or [])
    c["descripcion"] = descripcion
    with open(os.path.join(d, "case.json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(c, f, indent=2, ensure_ascii=False)
        f.write("\n")
    seed_path = os.path.join(d, "seed.sql")
    if seed:
        with open(seed_path, "w", encoding="utf-8", newline="\n") as f:
            f.write(seed if seed.endswith("\n") else seed + "\n")
    elif os.path.exists(seed_path):
        os.remove(seed_path)


def sin(parametros, *claves):
    """The parameters without some keys."""
    return {k: v for k, v in parametros.items() if k not in claves}


def con(parametros, **cambios):
    """The parameters with some values changed; a None value removes the key (JSON null in the case = absent parameter)."""
    d = dict(parametros)
    for k, v in cambios.items():
        d[k] = v
    return d
