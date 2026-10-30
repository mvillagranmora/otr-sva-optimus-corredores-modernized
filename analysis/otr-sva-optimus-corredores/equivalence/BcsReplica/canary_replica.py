"""Canarios de BcsReplica: mutaciones de UNA linea sobre el codigo real, en una copia FUERA del repo.

Uso: py -3 -I canary_replica.py <carpeta_modulo> <carpeta_temporal> <log>
Cada mutante se aplica a la copia, se corre `mvn -o test` (se detiene al primer fallo) y debe dejar pruebas en rojo; luego se restaura el archivo.
"""
import os
import shutil
import subprocess
import sys

MUT = "cl/bcs/replica/"
ENV = MUT + "envio/EnviarTablaReplicaServicio.java"
UTI = MUT + "respaldo/ReplicaUtilsServicio.java"
INI = MUT + "inicio/IniciarReplicaBase.java"
SEB = MUT + "adapter/out/jdbc/RespaldoSebraJdbc.java"
OPT = MUT + "adapter/out/jdbc/RespaldoOptimusJdbc.java"
TRG = MUT + "adapter/out/jdbc/TriggersReplicaJdbc.java"
COM = MUT + "kit/Compuertas.java"
WRK = MUT + "domain/Workstack.java"
NOP = MUT + "port/out/ColaboradoresNoPortados.java"
SQLK = MUT + "kit/Sql.java"

# (id, archivo, texto viejo, texto nuevo, ocurrencia 1-based)
MUTANTES = [
    ("M01", ENV, 'proceso.equalsIgnoreCase("INICIO_PAUTA")', 'proceso.equals("INICIO_PAUTA")', 1),
    ("M02", ENV, 'proceso.equalsIgnoreCase("VALORIZAR_FFMM")', 'proceso.equals("VALORIZAR_FFMM")', 1),
    ("M03", ENV, 'compuertas.exigirTriggers();', '', 1),
    ("M04", ENV, 'compuertas.exigirTriggers();', '', 2),
    ("M05", ENV, ';serialDelete:false;truncarDestino:false;serialLaunch:false";', ';serialDelete:false;truncarDestino:true;serialLaunch:false";', 1),
    ("M06", ENV, 'fecha + " ;serialDelete:false;truncarDestino:true', 'fecha + ";serialDelete:false;truncarDestino:true', 1),
    ("M07", ENV, 'plusSeconds(desfase)', 'minusSeconds(desfase)', 1),
    ("M08", ENV, 'if (lista.size() > 0)', 'if (lista.size() > 1)', 1),
    ("M09", ENV, 'if (feriados.esDiaHabil(conn, destino, idPais)) break;', 'if (!feriados.esDiaHabil(conn, destino, idPais)) break;', 1),
    ("M10", ENV, 'getAsLong() != 0L) return;', 'getAsLong() != 0L) break;', 1),
    ("M11", ENV, 'if (feriados.esDiaHabil(con, destino, idPais)) break;', 'if (!feriados.esDiaHabil(con, destino, idPais)) break;', 1),
    ("M12", ENV, '"CUSTODIA ACTUAL DIA INHABIL".equals(tabla)', '"CUSTODIA ACTUAL DIA INHABIL".equals(tabla.trim() + "x")', 1),
    ("M13", ENV, 'catch (Exception e) {', 'catch (IllegalStateException e) {', 1),
    ("M14", ENV, 'catch (Exception e) {', 'catch (IllegalStateException e) {', 2),
    ("M15", ENV, '.replace(":FechaEjecucion", fecha)', '.replace(":FechaEjecucion", "0001-01-01")', 1),
    ("M16", ENV, 'unDia(idPais, destino, idPais));', 'unDia(idPais, destino, idEmpresa));', 1),
    ("M17", UTI, 'respaldar.equalsIgnoreCase("S")', 'respaldar.equals("S")', 1),
    ("M18", UTI, 'substring(0, 7)', 'substring(0, 6)', 1),
    ("M19", UTI, 'substring(8, 10)', 'substring(8, 9)', 1),
    ("M20", UTI, '"90" + ', '"91" + ', 1),
    ("M21", UTI, 'String respaldar = "N";', 'String respaldar = "S";', 1),
    ("M22", UTI, 'catch (ServicioNoServidoException e) {', 'catch (IllegalStateException e) {', 1),
    ("M23", UTI, 'compuertas.exigirRespaldoOptimus();', '', 1),
    ("M24", UTI, 'optimus.copiarDiaInhabil(conn, fechaSistemaEjecucion, destino);', 'optimus.copiarDiaInhabil(conn, destino, fechaSistemaEjecucion);', 1),
    ("M25", UTI, 'guardarCuatro(conn, fechaSistemaEjecucion, destino);', 'guardarCuatro(conn, destino, destino);', 1),
    ("M26", UTI, 'Sql.fila("Fecha", fechaEnvio, "FechaSistema", fechaSistema', 'Sql.fila("Fecha", fechaSistema, "FechaSistema", fechaEnvio', 1),
    ("M27", UTI, 'compuertas.exigirRespaldoSebra();', '', 2),
    ("M28", INI, '"-1"', '"0"', 1),
    ("M29", INI, '"F"', '"M"', 1),
    ("M30", INI, '"ERROR".equals(', '"OK".equals(', 1),
    ("M31", INI, 'new Timestamp(reloj.millis())', 'new Timestamp(reloj.millis() + 1)', 1),
    ("M32", INI, 'fechas.fechaSistema(conn, "BSCL", idEmpresa)', 'fechas.fechaSistema(conn, "BSC", idEmpresa)', 1),
    ("M33", INI, '"INICIAR_REPLICA_PAUTA", "Action"', '"INICIAR_REPLICA", "Action"', 1),
    ("M34", MUT + "inicio/IniciarReplicaFull.java", '"QueryReplica"', '"QueryReplicaDelta"', 1),
    ("M35", SEB, '"TBCPDTPC", "TBCPMSPC"', '"TBCPMSPC", "TBCPDTPC"', 1),
    ("M36", SEB, '.replace(":COD_PER", codPer));\n        }\n    }\n\n    @Override', '.replace(":COD_PER", "x"));\n        }\n    }\n\n    @Override', 1),
    ("M37", SEB, '"TBCTCIFM", "TBCTCIRV", "TBCTCIIF", "TBCTCIRF"', '"TBCTCIRV", "TBCTCIFM", "TBCTCIIF", "TBCTCIRF"', 1),
    ("M38", OPT, 'Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCustodiaFechaOperacionCurrent.sql").replace(":FechaCustodia", fechaSistema));', '', 1),
    ("M39", TRG, 'Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DropTriggerCustodia.sql"));', '', 1),
    ("M40", COM, 'triggers:false', 'triggers:true', 1),
    ("M41", COM, 'respaldo-optimus:false', 'respaldo-optimus:true', 1),
    ("M42", WRK, 'w.Action = "M";', 'w.Action = "N";', 1),
    ("M43", WRK, 'w.Key = "";', 'w.Key = " ";', 1),
    ("M44", NOP, 'throw cerrado("ConsultarEmpresa");', 'return 1L;', 1),
]


def correr(mod, log):
    r = subprocess.run(["mvn", "-o", "-q", "test", "-Dsurefire.skipAfterFailureCount=1"], cwd=mod, capture_output=True, text=True, encoding="utf-8", errors="replace", shell=True)
    salida = r.stdout + r.stderr
    log.write(salida[-1500:] + "\n")
    return r.returncode, salida


def main():
    origen, tmp, ruta_log = sys.argv[1:4]
    if os.path.exists(tmp):
        shutil.rmtree(tmp)
    mod = os.path.join(tmp, "BcsReplica")
    shutil.copytree(origen, mod, ignore=shutil.ignore_patterns("target"))
    vivos = []
    muertos = 0
    with open(ruta_log, "w", encoding="utf-8", newline="\n") as log:
        log.write("Canarios BcsReplica: %d mutantes de una linea sobre el codigo real (copia fuera del repo)\n" % len(MUTANTES))
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
            log.write("=== %s %s: %s -> %s (ocurrencia %d)\n" % (mid, rel, viejo.strip()[:80], nuevo.strip()[:80], k))
            log.flush()
            try:
                rc, salida = correr(mod, log)
            finally:
                with open(ruta, "w", encoding="utf-8", newline="") as f:
                    f.write(original)
            estado = "MUERTO" if rc != 0 and "COMPILATION ERROR" not in salida else ("COMPILA MAL" if rc != 0 else "SOBREVIVE")
            log.write("%s => %s\n" % (mid, estado))
            log.flush()
            if estado == "MUERTO":
                muertos += 1
            else:
                vivos.append(mid + " " + estado)
        log.write("\nRESUMEN: %d mutantes, %d muertos, %d vivos/no validos %s\n" % (len(MUTANTES), muertos, len(vivos), vivos))


main()
