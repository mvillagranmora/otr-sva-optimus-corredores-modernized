"""Canarios de la correccion del texto de Parameters de BcsReplica (2026-10-30): mutaciones de UNA linea sobre el codigo real, en una copia FUERA del repo.

Uso: py -3 -I canary_replica_correccion.py <carpeta_modulo> <carpeta_temporal> <log> <carpeta_evidencia>
Cada mutante se aplica a la copia, se corre `mvn -o test` completo y debe dejar pruebas en rojo; se guardan los XML de surefire con fallos y se restaura el archivo.
"""
import glob
import os
import re
import shutil
import subprocess
import sys

MUT = "cl/bcs/replica/"
SQLK = MUT + "kit/Sql.java"
ENV = MUT + "envio/EnviarTablaReplicaServicio.java"
UTI = MUT + "respaldo/ReplicaUtilsServicio.java"
INI = MUT + "inicio/IniciarReplicaBase.java"

# (id, archivo, texto viejo, texto nuevo, ocurrencia 1-based)
MUTANTES = [
    ("N01-map-tostring", SQLK, "map(Sql::json)", "map(Object::toString)", 1),
    ("N02-sin-serializenulls", SQLK, '.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create()', '.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create()', 1),
    ("N03-formato-fecha", SQLK, 'setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")', 'setDateFormat("yyyy-MM-dd HH:mm:ss")', 1),
    ("N04-orden-alfabetico", SQLK, "Map<String, Object> m = new LinkedHashMap<>();", "Map<String, Object> m = new java.util.TreeMap<>();", 1),
    ("N05-sin-stamp-nulo", ENV, '"Status", w.Status, "Stamp", null, "Parameters"', '"Status", w.Status, "Parameters"', 1),
    ("N06-orden-envio-postpauta", UTI, 'Sql.fila("FechaSistema", fechaSistema, "Tabla", tabla, "Fecha", fechaEnvio)', 'Sql.fila("Fecha", fechaEnvio, "FechaSistema", fechaSistema, "Tabla", tabla)', 1),
    ("N07-etiqueta-parameters", SQLK, '" Parameters: "', '" Params: "', 1),
    ("N08-etiqueta-query", SQLK, 'm + " Query: " + sql', 'm + " Query:" + sql', 1),
    ("N09-causa-doble-espacio", SQLK, 'm = m + " " + cause.getCause().getMessage()', 'm = m + "  " + cause.getCause().getMessage()', 1),
    ("N10-orden-iniciar", INI, '"Table", "INICIAR_REPLICA_PAUTA", "Action", "F", "Status", 0, "Stamp", stamp', '"Table", "INICIAR_REPLICA_PAUTA", "Action", "F", "Stamp", stamp, "Status", 0', 1),
]


def correr(mod):
    r = subprocess.run(["mvn", "-o", "test"], cwd=mod, capture_output=True, text=True, encoding="utf-8", errors="replace", shell=True)
    return r.returncode, r.stdout + r.stderr


def main():
    origen, tmp, ruta_log, evid = sys.argv[1:5]
    if os.path.exists(tmp):
        shutil.rmtree(tmp)
    os.makedirs(evid, exist_ok=True)
    mod = os.path.join(tmp, "BcsReplica")
    shutil.copytree(origen, mod, ignore=shutil.ignore_patterns("target"))
    vivos = []
    muertos = 0
    with open(ruta_log, "w", encoding="utf-8", newline="\n") as log:
        log.write("Canarios de la correccion de Parameters de BcsReplica: %d mutantes de una linea sobre el codigo real (copia fuera del repo)\n" % len(MUTANTES))
        for mid, rel, viejo, nuevo, k in MUTANTES:
            ruta = os.path.join(mod, "src/main/java", rel)
            with open(ruta, encoding="utf-8", newline="") as f:
                original = f.read()
            partes = original.split(viejo)
            if len(partes) <= k:
                log.write("%s: la cadena a mutar no existe (ocurrencia %d) en %s\n" % (mid, k, rel))
                vivos.append(mid + " (no aplicable)")
                continue
            mutado = viejo.join(partes[:k]) + nuevo + viejo.join(partes[k:])
            with open(ruta, "w", encoding="utf-8", newline="") as f:
                f.write(mutado)
            log.write("=== %s %s: %s -> %s (ocurrencia %d)\n" % (mid, rel, viejo.strip()[:90], nuevo.strip()[:90], k))
            log.flush()
            try:
                rc, salida = correr(mod)
            finally:
                with open(ruta, "w", encoding="utf-8", newline="") as f:
                    f.write(original)
            for linea in salida.splitlines():
                if "Tests run:" in linea and " in cl.bcs" in linea and ("Failures: 0, Errors: 0" not in linea):
                    log.write(linea + "\n")
            nuevoSql = "ReplicaMensajeSqlTest"
            fallan = []
            d = os.path.join(evid, mid)
            os.makedirs(d, exist_ok=True)
            for x in glob.glob(os.path.join(mod, "target", "surefire-reports", "TEST-*.xml")):
                t = open(x, encoding="utf-8", errors="replace").read()
                m = re.search(r'<testsuite [^>]*tests="(\d+)"[^>]*errors="(\d+)"[^>]*skipped="(\d+)"[^>]*failures="(\d+)"', t)
                if m and (int(m.group(2)) + int(m.group(4))) > 0:
                    shutil.copy(x, d)
                    fallan.append("%s:%d" % (os.path.basename(x)[5:-4].split(".")[-1], int(m.group(2)) + int(m.group(4))))
            estado = "MUERTO" if rc != 0 and "COMPILATION ERROR" not in salida and fallan else ("COMPILA MAL" if rc != 0 else "SOBREVIVE")
            log.write("%s => %s; clases con fallos: %s; ReplicaMensajeSqlTest roja: %s\n" % (mid, estado, fallan, any(f.startswith(nuevoSql) for f in fallan)))
            log.flush()
            if estado == "MUERTO":
                muertos += 1
            else:
                vivos.append(mid + " " + estado)
        log.write("\nRESUMEN: %d mutantes, %d muertos, %d vivos/no validos %s\n" % (len(MUTANTES), muertos, len(vivos), vivos))


main()
