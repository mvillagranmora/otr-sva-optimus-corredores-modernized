"""Helpers of the step 3B case generator."""
import json
import os


def param(nombre, valor, empresa=1):
    return ("INSERT INTO \"BSCL.FondoMutuo.BD::FMM.Parametro\" (\"IdEmpresa\", \"NombreTipoParametro\", \"ValorParametro\") VALUES (%d, '%s', '%s');"
            % (empresa, nombre, valor))


def set_param(nombre, valor, empresa=1):
    return ("UPDATE \"BSCL.FondoMutuo.BD::FMM.Parametro\" SET \"ValorParametro\" = '%s' WHERE \"IdEmpresa\" = %d AND \"NombreTipoParametro\" = '%s';"
            % (valor, empresa, nombre))


def del_param(nombre, empresa=1):
    return ("DELETE FROM \"BSCL.FondoMutuo.BD::FMM.Parametro\" WHERE \"IdEmpresa\" = %d AND \"NombreTipoParametro\" = '%s';" % (empresa, nombre))


def dicc(codigo, texto):
    return ("INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" (\"CodLenguaje\", \"CodDiccionario\", \"Descripcion\") VALUES ('es', '%s', '%s');"
            % (codigo, texto.replace("'", "''")))


def sql(table, **cols):
    names = ", ".join('"%s"' % k for k in cols)
    vals = ", ".join(lit(v) for v in cols.values())
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (table, names, vals)


def lit(v):
    if v is None:
        return "NULL"
    if isinstance(v, (int, float)):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


def caso(id_, servicio, ref, entrada, seed=(), colaboradores=None, tablas=()):
    c = {"servicio": servicio, "ref": ref, "entrada": entrada}
    if colaboradores:
        c["colaboradores"] = colaboradores
    if tablas:
        c["tablas"] = [{"nombre": t, "orden": o} for t, o in tablas]
    return {"id": id_, "case": c, "seed": "\n".join(seed) + ("\n" if seed else "")}


def write(base, case):
    d = os.path.join(base, case["id"])
    os.makedirs(d, exist_ok=True)
    with open(os.path.join(d, "case.json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(case["case"], f, indent=2, ensure_ascii=False)
        f.write("\n")
    with open(os.path.join(d, "seed.sql"), "w", encoding="utf-8", newline="\n") as f:
        f.write(case["seed"])
