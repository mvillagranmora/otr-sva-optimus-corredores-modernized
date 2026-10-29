import json, os, sys
d = sys.argv[1]
for n in sorted(os.listdir(d)):
    e = json.load(open(os.path.join(d, n, "expected.json"), encoding="utf-8"))
    arch = e.get("archivos")
    sizes = []
    if isinstance(arch, list):
        sizes = [(a.get("nombre") or a.get("ruta") or "?", a.get("longitud", a.get("length"))) for a in arch]
    elif isinstance(arch, dict):
        sizes = [(k, (v.get("longitud") if isinstance(v, dict) else v)) for k, v in arch.items()]
    ex = e.get("excepcion")
    print(n, e.get("desenlace"), (ex or {}).get("tipo", ""), "files:", len(sizes), [s[1] for s in sizes][:3], "salida" if e.get("salida") else "")
