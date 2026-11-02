"""Merges the per-shard stability reports of record_csr_all.sh into determinism-csr.txt, in the format of the single-process report of OracleRunner:
first line "cases executed: N, repeats per case: R, unstable (hard + message-only): U, hard: H", then one line per unstable case.

  py -3 -I merge_reports_csr.py <shardsDir> <out.txt> <repeats>
"""
import pathlib
import re
import sys


def main():
    shards = pathlib.Path(sys.argv[1])
    out = pathlib.Path(sys.argv[2])
    repeats = int(sys.argv[3])
    casos = unst = hard = 0
    lineas = []
    for f in sorted(shards.glob("*.txt")):
        txt = f.read_text(encoding="utf-8").splitlines()
        m = re.match(r"cases executed: (\d+), repeats per case: (\d+), unstable \(hard \+ message-only\): (\d+), hard: (\d+)", txt[0])
        if not m:
            raise SystemExit("unexpected report in %s: %s" % (f, txt[0]))
        if int(m.group(2)) != repeats:
            raise SystemExit("%s ran %s repeats, expected %d" % (f, m.group(2), repeats))
        casos += int(m.group(1))
        unst += int(m.group(3))
        hard += int(m.group(4))
        lineas += txt[1:]
    out.write_text("\n".join(["cases executed: %d, repeats per case: %d, unstable (hard + message-only): %d, hard: %d" % (casos, repeats, unst, hard)] + lineas) + "\n", encoding="utf-8", newline="\n")
    print("merged: %d cases, %d unstable (%d hard) -> %s" % (casos, unst, hard, out))
    if hard:
        raise SystemExit(4)


if __name__ == "__main__":
    main()
