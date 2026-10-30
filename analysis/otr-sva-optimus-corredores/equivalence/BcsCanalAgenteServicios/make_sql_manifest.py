"""BcsCanalAgenteServicios: sql-manifest.txt = name, byte length and SHA-256 (line endings normalised to LF) of the 7 SQL texts of the legacy module:
  sql/<file>          the 4 SQL of the Java class (legacy/.../CanalAgente/JAVA/BcsCanalAgenteServicios/src/cl/bcs/canalagente/sql/), ported to the production resources cl/bcs/canalagente/sql/
  bd/servicios/<file> the 3 SQLScript procedures of BD/Servicios (HANA only: they cannot run on H2), ported to cl/bcs/canalagente/bd/servicios/
The JUnit test CanalSqlRecursosTest requires the production resources to have the same SHA-256, so the texts are ported byte for byte (modulo CRLF).

  py -3 -I make_sql_manifest.py
"""
import hashlib
import pathlib

BASE = pathlib.Path(__file__).resolve().parent
CAN = BASE.parents[3] / "legacy" / "otr-sva-optimus-corredores" / "BSCL" / "CanalAgente"
FUENTES = [("sql", CAN / "JAVA" / "BcsCanalAgenteServicios" / "src" / "cl" / "bcs" / "canalagente" / "sql"), ("bd/servicios", CAN / "BD" / "Servicios")]


def main():
    lineas = []
    for prefijo, carpeta in FUENTES:
        for f in sorted(carpeta.glob("*.sql")):
            b = f.read_bytes().replace(b"\r\n", b"\n")
            lineas.append("%s/%s %d %s" % (prefijo, f.name, len(b), hashlib.sha256(b).hexdigest()))
    (BASE / "sql-manifest.txt").write_text("\n".join(lineas) + "\n", encoding="utf-8", newline="\n")
    print("sql-manifest.txt:", len(lineas), "files")


if __name__ == "__main__":
    main()
