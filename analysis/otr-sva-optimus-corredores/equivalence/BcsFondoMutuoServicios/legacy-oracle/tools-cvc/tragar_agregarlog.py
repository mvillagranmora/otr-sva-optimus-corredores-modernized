#!/usr/bin/env python3
"""F3 W2 (CargarValorCuota): turns the GENERATED double of Plataforma AgregarLog (tools/gen_doubles.py output, in the temporary directory; legacy/ is never edited) into the
double that behaves as the legacy at the two points CargarValorCuota depends on:

  1. AgregarLog.logica returns at once when p_Log is empty (AgregarLog.java: `if(entrada.p_Log.size() == 0) return salida;` before anything is written): the call is NOT
     recorded, so the new code is not required to call its LogPort with an empty list (the production must skip it, see API_ASSUMED.md);
  2. the write part of AgregarLog.logica is inside `try { ... } catch(Exception e) { e.printStackTrace(); }` (AgregarLog.java:239-241): ANY exception of the write is swallowed
     and the method returns the empty output (P_OUT_ERRORESCONTADOR = 0). The double records the call and, when the case scripts {"error": ...}, swallows it the same way.
     NOT modelled (declared limits): an exception BEFORE that try (the preparation of the headers, List2Hash of the header ids) is not swallowed by the legacy but cannot happen
     with the logs CargarValorCuota builds (one header id 159 per call).

  py -3 -I tragar_agregarlog.py <generated AgregarLog.java>
"""
import re
import sys

NUEVO = """public OutAgregarLog logica(Connection con, InAgregarLog entrada) throws Exception {
\t\tOutAgregarLog salida = new OutAgregarLog();
\t\tsalida.P_OUT_ERRORESCONTADOR = Long.valueOf(0);
\t\tif (entrada.p_Log.size() == 0) { return salida; }
\t\ttry {
\t\t\treturn cl.bcs.oracle.Doubles.answer("AgregarLog", "logica", new Object[]{con, entrada}, OutAgregarLog.class);
\t\t} catch (Exception e) {
\t\t\te.printStackTrace();
\t\t\treturn salida;
\t\t}
\t}"""


def main():
    ruta = sys.argv[1]
    src = open(ruta, encoding="utf-8").read()
    patron = re.compile(r"public OutAgregarLog logica\(Connection con, InAgregarLog entrada\) throws Exception \{ return cl\.bcs\.oracle\.Doubles\.answer\(\"AgregarLog\", \"logica\", "
                        r"new Object\[\]\{con, entrada\}, OutAgregarLog\.class\); \}")
    if not patron.search(src):
        raise SystemExit("generated AgregarLog double: the logica line was not found (gen_doubles changed?)")
    src = patron.sub(lambda m: NUEVO, src, count=1)
    with open(ruta, "w", encoding="utf-8", newline="\n") as f:
        f.write(src)
    print("AgregarLog double: empty-list return and swallow of the write failure applied")


if __name__ == "__main__":
    main()
