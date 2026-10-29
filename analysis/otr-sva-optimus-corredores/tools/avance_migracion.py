"""Regenera analysis/otr-sva-optimus-corredores/AVANCE_MIGRACION.md.

Fuentes: LOC_LEGACY.md (lineas Java legacy por modulo), VERIFICATION.json (veredicto por modulo),
avance_estado.json (estado manual y historial) y las fechas de los archivos de cada modulo (obsolescencia).
Uso:  py -3 -I analysis/otr-sva-optimus-corredores/tools/avance_migracion.py   (desde la raiz del workspace)
"""
import datetime
import json
import os
import re
import sys

RAIZ = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", ".."))
A = os.path.join(RAIZ, "analysis", "otr-sva-optimus-corredores")
M = os.path.join(RAIZ, "modernized", "otr-sva-optimus-corredores")
CARPETA = {"BECB": "BcsBECB", "SURA": "BcsSURA"}
META = 0.80
EXTRAS = ["BcsCrystalReports", "BcsApvJavaCallAdapter", "BcsAPVApi"]  # modulos nuevos fuera de los 42 del brief


def leer_loc():
    filas = []
    en_tabla = False
    for l in open(os.path.join(A, "LOC_LEGACY.md"), encoding="utf-8"):
        if l.startswith("| Modulo | Dominio"):
            en_tabla = True
            continue
        if en_tabla:
            if not l.startswith("|"):
                if filas:
                    break
                continue
            c = [x.strip() for x in l.strip().strip("|").split("|")]
            if len(c) == 4 and re.fullmatch(r"[\d,]+", c[3]):
                filas.append((c[0], c[1], int(c[3].replace(",", ""))))
    return filas


def ultima_modificacion(carpeta):
    t = 0.0
    base = os.path.join(M, carpeta)
    for raiz, dirs, files in os.walk(base):
        dirs[:] = [d for d in dirs if d not in ("target", ".git", "node_modules")]
        for f in files:
            t = max(t, os.path.getmtime(os.path.join(raiz, f)))
    return t


def fmt(n):
    return format(n, ",").replace(",", ".")


def contar_lineas(base, incluir=None, excluir=None):
    """Lineas no vacias de los .java bajo base; incluir/excluir son fragmentos de la ruta relativa (con /)."""
    n = 0
    if not os.path.isdir(base):
        return 0
    for raiz, dirs, files in os.walk(base):
        dirs[:] = [d for d in dirs if d not in ("target", "node_modules", ".git")]
        for f in files:
            if not f.endswith(".java"):
                continue
            ruta = os.path.join(raiz, f)
            rel = os.path.relpath(ruta, base).replace(os.sep, "/")
            if incluir and not any(x in rel for x in incluir):
                continue
            if excluir and any(x in rel for x in excluir):
                continue
            with open(ruta, encoding="utf-8", errors="replace") as fh:
                n += sum(1 for l in fh if l.strip())
    return n


def contar_pruebas(carpeta):
    """Pruebas de la ultima corrida (target/surefire-reports); None si no hay resultados."""
    d = os.path.join(M, carpeta, "target", "surefire-reports")
    if not os.path.isdir(d):
        return None
    total = 0
    for f in os.listdir(d):
        if f.startswith("TEST-") and f.endswith(".xml"):
            txt = open(os.path.join(d, f), encoding="utf-8", errors="replace").read(2000)
            m = re.search(r'<testsuite[^>]*\btests="(\d+)"', txt)
            if m:
                total += int(m.group(1))
    return total


def main():
    loc = leer_loc()
    ver = json.load(open(os.path.join(A, "VERIFICATION.json"), encoding="utf-8"))
    veredicto = {}
    for m in ver["modules"]:
        veredicto[m.get("module") or m.get("name")] = m.get("verdict") or m.get("status")
    t_ver = os.path.getmtime(os.path.join(A, "VERIFICATION.json"))
    est = json.load(open(os.path.join(A, "avance_estado.json"), encoding="utf-8"))
    total = sum(x[2] for x in loc)
    filas, probado, construido = [], 0, 0
    sin_confirmar = []
    nuevo_total, tests_loc_total, pruebas_total = 0, 0, 0
    for nombre, dom, lineas in loc:
        carpeta = CARPETA.get(nombre, nombre)
        v = veredicto.get(carpeta, "-")
        e = est["modulos"].get(carpeta, {})
        obsoleto = v == "PROVEN" and ultima_modificacion(carpeta) > t_ver
        activo = True
        if v == "PROVEN" and e.get("estado") in ("parcial", "en-curso"):
            # verificado pero solo una parte del modulo esta migrada: cuenta unicamente lo migrado
            migrado = e.get("loc_migrado", 0)
            estado = "PROBADO PARCIAL (solo lo migrado)" + (", verificacion desactualizada" if obsoleto else "")
            probado += migrado
            construido += migrado
        elif v == "PROVEN" and e.get("completo") is True and e.get("estado") not in ("parcial", "en-curso"):
            # regla: PROBADO (100 %) solo si el modulo esta marcado explicitamente completo en avance_estado.json
            estado = "PROBADO (verificacion desactualizada)" if obsoleto else "PROBADO"
            probado += lineas
            construido += lineas
            migrado = lineas
        elif v == "PROVEN":
            # PROVEN no basta: sin la marca "completo" no se afirma 100 %. Sus lineas se siguen contando como probadas (verify firmado) pero se rotula y se lista aparte.
            estado = "PROBADO (100 % sin confirmar)" + (", verificacion desactualizada" if obsoleto else "")
            probado += lineas
            construido += lineas
            migrado = lineas
            sin_confirmar.append((nombre, lineas))
        elif e.get("estado") == "construido-sin-probar":
            estado, migrado = "construido, sin probar", lineas
            construido += lineas
        elif e.get("estado") in ("parcial", "en-curso"):
            migrado = e.get("loc_migrado", 0)
            estado = "parcial" if e["estado"] == "parcial" else "en curso"
            construido += migrado
        else:
            estado, migrado, activo = e.get("estado", "pendiente / esqueleto"), 0, False
        base = os.path.join(M, carpeta)
        if activo:
            nuevo = contar_lineas(os.path.join(base, "src", "main", "java"), e.get("incluir"), e.get("excluir"))
            tl = contar_lineas(os.path.join(base, "src", "test", "java"))
            npr = contar_pruebas(carpeta)
        else:
            nuevo, tl, npr = 0, 0, None
        nuevo_total += nuevo
        tests_loc_total += tl
        pruebas_total += npr or 0
        filas.append((dom, nombre, lineas, estado, migrado, v, e.get("nota", ""), nuevo, tl, npr))
    extras = []
    for ex in EXTRAS:
        npr = contar_pruebas(ex)
        nu = contar_lineas(os.path.join(M, ex, "src", "main", "java"))
        tl = contar_lineas(os.path.join(M, ex, "src", "test", "java"))
        extras.append((ex, veredicto.get(ex, "-"), nu, tl, npr))
        nuevo_total += nu
        tests_loc_total += tl
        pruebas_total += npr or 0
    hoy = datetime.date.today().isoformat()
    n_prob = sum(1 for f in filas if f[3].startswith("PROBADO"))
    o = ["# Avance de la migracion (se regenera con `tools/avance_migracion.py`)\n",
         "Generado: %s. Metodo: lineas Java del legacy (`LOC_LEGACY.md`, 42 modulos del brief, %s lineas) como aproximacion; el SQL embebido (188.823 lineas) viaja con cada modulo y no se cuenta aparte. **Probado** = veredicto PROVEN en `VERIFICATION.json`; un modulo solo se rotula `PROBADO` (100 %%) si `avance_estado.json` lo marca `completo`, es decir, si todo su codigo legacy esta portado (los limites de prueba, como H2 frente a HANA, y las firmas de expertos no impiden la marca):  un PROVEN no completo se rotula `PROBADO PARCIAL (solo lo migrado)` o `PROBADO (100 %% sin confirmar)`, nunca `PROBADO`. Las lineas son un proxy del tamano, no del esfuerzo.\n" % (hoy, fmt(total)),
         "## Resumen\n", "| Medida | Valor |", "|---|---:|",
         "| Modulos probados | %d de %d (%.1f %%) |" % (n_prob, len(loc), 100.0 * n_prob / len(loc)),
         "| Codigo migrado y probado | %s de %s lineas (**%.1f %%**) |" % (fmt(probado), fmt(total), 100.0 * probado / total),
         "| Codigo construido (probado + sin probar + parcial) | %s lineas (%.1f %%) |" % (fmt(construido), 100.0 * construido / total),
         "| Meta de las pruebas de conjunto (80 %%) | %s lineas; faltan **%s** |" % (fmt(int(total * META)), fmt(max(0, int(total * META) - probado))),
         "| **Codigo nuevo escrito** (Java activo en `src/main`, lineas no vacias, incluye los modulos fuera de los 42) | **%s lineas** |" % fmt(nuevo_total),
         "| Probado (PROVEN) pero sin marca de modulo completo: no se afirma 100 %% | %s lineas en %d modulos (%s) |" % (fmt(sum(x[1] for x in sin_confirmar)), len(sin_confirmar), ", ".join(x[0] for x in sin_confirmar) or "ninguno"),
         "| Codigo de pruebas escrito (`src/test`, Java) | %s lineas |" % fmt(tests_loc_total),
         "| Pruebas de la ultima corrida registrada en `target/surefire-reports` | %s |" % fmt(pruebas_total),
         "\nLa verificacion firmada es del %s; los modulos marcados *desactualizada* cambiaron despues y hay que re-verificarlos y volver a firmar antes de contarlos como cerrados. Los modulos nuevos fuera de los 42 (BcsCrystalReports, BcsApvJavaCallAdapter, BcsAPVApi) se listan aparte. Nota: en `BcsPlataformaServicios` y `BcsFondoMutuoServicios` el codigo nuevo cuenta solo las carpetas activas (los stubs viejos siguen en el arbol pero fuera del contexto de Spring y no se cuentan). Las pruebas se leen de la ultima corrida que haya dejado resultados: un modulo sin `target/` muestra `-`.\n" % datetime.date.fromtimestamp(t_ver).isoformat(),
         "## Por modulo\n", "| Dominio | Modulo | Avance (porcentaje) | Estado | Lineas Java legacy | Lineas legacy migradas | Nota |", "|---|---|---:|---|---:|---:|---|"]
    for f in sorted(filas, key=lambda x: (x[0], -x[2])):
        avance = 100.0 * f[4] / f[2] if f[2] else 0.0
        o.append("| %s | %s | %.1f %% | %s | %s | %s | %s |" % (f[0], f[1], avance, f[3], fmt(f[2]), fmt(f[4]), f[6]))
    o += ["\n## Modulos nuevos fuera de los 42 del brief\n", "| Modulo | Veredicto pack | Codigo nuevo (lineas) | Pruebas (lineas) | Pruebas (n) |", "|---|---|---:|---:|---:|"]
    for ex in extras:
        o.append("| %s | %s | %s | %s | %s |" % (ex[0], ex[1], fmt(ex[2]), fmt(ex[3]), fmt(ex[4]) if ex[4] else "-"))
    o += ["\n## Historial\n", "| Fecha | Evento |", "|---|---|"]
    for h in reversed(est["historial"]):
        o.append("| %s | %s |" % (h["fecha"], h["evento"]))
    o.append("\nRegla de mantenimiento: cada vez que un modulo se actualiza o se cierra, el asistente agrega una fila al historial de `avance_estado.json`, ajusta su estado y vuelve a ejecutar el script.")
    open(os.path.join(A, "AVANCE_MIGRACION.md"), "w", encoding="utf-8", newline="\n").write("\n".join(o) + "\n")
    print("probado %s/%s = %.2f%%; construido %s = %.2f%%; modulos %d/%d" % (fmt(probado), fmt(total), 100.0 * probado / total, fmt(construido), 100.0 * construido / total, n_prob, len(loc)))


if __name__ == "__main__":
    sys.exit(main())
