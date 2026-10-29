import re, sys, os
LEG = r"C:\Users\m.villagran.mora\Downloads\develop\nuam-modernizado\legacy\otr-sva-optimus-corredores\BSCL\CuentaInversion\JAVA\BcsCuentaInversionServicios\src\cl\bcs\cuentaInversion"

def classes(src):
    """yield (name, text) of every 'public static class X {...}' with brace matching"""
    out = []
    for m in re.finditer(r'public\s+static\s+class\s+(\w+)[^{]*\{', src):
        i = m.end(); d = 1
        while d and i < len(src):
            c = src[i]
            if c == '{': d += 1
            elif c == '}': d -= 1
            i += 1
        out.append((m.group(1), src[m.start():i]))
    return out

if __name__ == '__main__':
    svc = sys.argv[1]
    pat = sys.argv[2] if len(sys.argv) > 2 else 'Entrada|Salida|_Entrada|_Salida'
    src = open(os.path.join(LEG, svc + '.java'), encoding='utf-8', errors='replace').read()
    for n, t in classes(src):
        if re.search(pat, n):
            print('//', n)
            print(t)
