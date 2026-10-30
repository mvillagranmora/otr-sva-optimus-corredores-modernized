"""BcsReplica: sql-manifest.txt = name, byte length and SHA-256 (line endings normalised to LF) of every SQL file of the legacy module (legacy/.../Replica/JAVA/BcsReplica/src/cl/bcs/replica/sql/, 58 files).
The JUnit test ReplicaSqlRecursosTest requires the production resources cl/bcs/replica/sql/<name> to have the same SHA-256, so the 58 statements are ported byte for byte (modulo CRLF).

  py -3 -I make_sql_manifest.py
"""
import hashlib
import pathlib

BASE = pathlib.Path(__file__).resolve().parent
SQL = BASE.parents[3] / "legacy" / "otr-sva-optimus-corredores" / "BSCL" / "Replica" / "JAVA" / "BcsReplica" / "src" / "cl" / "bcs" / "replica" / "sql"


def main():
    lineas = []
    for f in sorted(SQL.glob("*.sql")):
        b = f.read_bytes().replace(b"\r\n", b"\n")
        lineas.append("%s %d %s" % (f.name, len(b), hashlib.sha256(b).hexdigest()))
    (BASE / "sql-manifest.txt").write_text("\n".join(lineas) + "\n", encoding="utf-8", newline="\n")
    print("sql-manifest.txt:", len(lineas), "files")


if __name__ == "__main__":
    main()
