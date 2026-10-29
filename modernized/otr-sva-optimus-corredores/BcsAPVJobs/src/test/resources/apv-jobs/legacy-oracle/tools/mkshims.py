#!/usr/bin/env python3
"""Generates the oracle FAKES of the legacy collaborators of the four APV jobs (BcsAPVJobs).

WHAT A FAKE IS: a class with the legacy package, name and public nested data classes (copied VERBATIM, character by character, from the legacy
source, plus the nested classes they use), and a `logica(...)` that does NOT reproduce the collaborator: it records its input in
`cl.bcs.oracle.Hooks` (the collaborator trace) and answers the scripted output of the case (case.json "colaboradores").
The verbatim copy is made by this tool from the legacy sources (never edited by hand): re-run it when the legacy tree changes.

Usage: python mkshims.py <legacy BSCL root> <output shims dir>
"""
import os
import re
import sys

LEGACY = sys.argv[1]
OUT = sys.argv[2]

# (legacy file relative to BSCL, package, class, [root nested classes], logica signature or None, imports that live in other fakes)
SHIMS = [
    ("APV/JAVA/BcsAPVServicios/src/cl/bcs/APV/CargaMasivaPrevired.java", "cl.bcs.APV", "CargaMasivaPrevired",
     ["Parametros_Entrada", "ParametrosSalidaServicio"],
     ("ParametrosSalidaServicio", "Parametros_Entrada", "CargaMasivaPrevired"), []),
    ("Orden/JAVA/BcsOrdenServicios/src/cl/bcs/ordenes/CrearOrdenes.java", "cl.bcs.ordenes", "CrearOrdenes",
     ["CrearOrdenesEntrada", "CrearOrdenesSalida", "ObjOrdenesEntrada", "OrdenProceso"],
     ("CrearOrdenesSalida", "CrearOrdenesEntrada", "CrearOrdenes"), []),
    ("Orden/JAVA/BcsOrdenServicios/src/cl/bcs/ordenes/CalcularMontoYCobrosOrden.java", "cl.bcs.ordenes", "CalcularMontoYCobrosOrden",
     ["CalcularMontoYCobrosOrden_Entrada", "CalcularMontoYCobrosOrden_Salida", "Orden_Entrada", "Orden_Salida"],
     ("CalcularMontoYCobrosOrden_Salida", "CalcularMontoYCobrosOrden_Entrada", "CalcularMontoYCobrosOrden"), []),
    ("Custodia/JAVA/BcsCustodiaServicios/src/cl/bcs/custodia/ConsultarCustodiaPortafolio.java", "cl.bcs.custodia", "ConsultarCustodiaPortafolio",
     ["Entrada_ConsultaCustodiaPortafolio", "Salida_ConsultaCustodiaPortafolio", "Tabla_Salida"],
     ("Salida_ConsultaCustodiaPortafolio", "Entrada_ConsultaCustodiaPortafolio", "ConsultarCustodiaPortafolio"), []),
    ("Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/com/pe/ConsultarCobrosPeru.java", "cl.bcs.plataforma.com.pe", "ConsultarCobrosPeru",
     ["DefinicionRangoCobro"], None, []),
    ("Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/pla/SaldoFlujoCaja.java", "cl.bcs.plataforma.pla", "SaldoFlujoCaja",
     ["ObjFLUJOCAJA", "ObjFLUJOCAJADETALLADO", "ObjERRORESDETALLE"], None, []),
    ("Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/pla/ConsultarFlujoCaja.java", "cl.bcs.plataforma.pla", "ConsultarFlujoCaja",
     ["EntradaConsultarFlujoCaja", "PortafolioAConsultar", "SalidaConsultarFlujoCaja"],
     ("SalidaConsultarFlujoCaja", "EntradaConsultarFlujoCaja", "ConsultarFlujoCaja"),
     ["cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA", "cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJADETALLADO",
      "cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjERRORESDETALLE"]),
    ("FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/ConsultarFlujoCustodiaFondoMutuo.java", "cl.bcs.fondomutuo", "ConsultarFlujoCustodiaFondoMutuo",
     ["EntradaConsultarFlujoCustodiaFondoMutuo", "SalidaConsultarFlujoCustodiaFondoMutuo", "InInstrumento", "InCarteras", "FlujoFondoMutuoTotalizado"],
     ("SalidaConsultarFlujoCustodiaFondoMutuo", "EntradaConsultarFlujoCustodiaFondoMutuo", "ConsultarFlujoCustodiaFondoMutuo"), []),
    ("FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/CrearSolicitudAporteRescateNormal.java", "cl.bcs.fondomutuo", "CrearSolicitudAporteRescateNormal",
     ["EntradaCrearSolicitudAporteRescateNormal", "SalidaCrearSolicitudAporteRescateNormal", "InSolicitudesAporteRescate"],
     ("SalidaCrearSolicitudAporteRescateNormal", "EntradaCrearSolicitudAporteRescateNormal", "CrearSolicitudAporteRescateNormal"), []),
    ("Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/pla/utils/ConsultarEmpresa.java", "cl.bcs.plataforma.pla.utils", "ConsultarEmpresa",
     ["InformacionEmpresa"], ("InformacionEmpresa", None, "ConsultarEmpresa"), []),
]

# imports a verbatim nested class may need, when its text uses the simple name
KNOWN = {
    "BigDecimal": "java.math.BigDecimal", "RoundingMode": "java.math.RoundingMode", "Date": "java.sql.Date", "Timestamp": "java.sql.Timestamp",
    "Connection": "java.sql.Connection", "List": "java.util.List", "ArrayList": "java.util.ArrayList", "HashMap": "java.util.HashMap",
    "Map": "java.util.Map", "Utils": "cl.bcs.utils.Utils", "BcsPojo": "cl.bcs.utils.BcsPojo", "MensajeSalida": "cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida",
    "BarraProgreso": "cl.bcs.plataforma.pla.utils.BarraProgreso", "JavaCall": "cl.bcs.utils.JavaCall", "JsonObject": "com.google.gson.JsonObject",
    "ErrorDetalle": "cl.bcs.plataforma.pla.utils.PlataformaUtils.ErrorDetalle", "AdvertenciaDetalle": "cl.bcs.plataforma.pla.utils.PlataformaUtils.AdvertenciaDetalle",
    "InformacionDetalle": "cl.bcs.plataforma.pla.utils.PlataformaUtils.InformacionDetalle",
    "ResultadoSolicitud": "cl.bcs.fondomutuo.pojo.ResultadoSolicitud",
    "DefinicionRangoCobro": "cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.DefinicionRangoCobro",
}


def leer(ruta):
    with open(os.path.join(LEGACY, ruta), encoding="utf-8", errors="replace") as f:
        return f.read()


def anidadas(texto):
    """name -> (start, end) offsets of every `public static class NAME ...{ ... }` declared at depth 1 of the outer class."""
    res = {}
    depth = 0
    i = 0
    n = len(texto)
    en_linea = en_bloque = False
    comilla = None
    inicio_clase = None
    nombre = None
    while i < n:
        c = texto[i]
        d = texto[i + 1] if i + 1 < n else ""
        if en_linea:
            if c == "\n":
                en_linea = False
        elif en_bloque:
            if c == "*" and d == "/":
                en_bloque = False
                i += 1
        elif comilla:
            if c == "\\":
                i += 1
            elif c == comilla:
                comilla = None
        else:
            if c == "/" and d == "/":
                en_linea = True
                i += 1
            elif c == "/" and d == "*":
                en_bloque = True
                i += 1
            elif c in "\"'":
                comilla = c
            elif c == "{":
                depth += 1
            elif c == "}":
                depth -= 1
                if depth == 1 and inicio_clase is not None:
                    res[nombre] = (inicio_clase, i + 1)
                    inicio_clase = None
            if depth == 1 and inicio_clase is None and c == "p":
                m = re.compile(r"public\s+static\s+class\s+(\w+)[^{;]*\{").match(texto, i)
                if m:
                    inicio_clase = i
                    nombre = m.group(1)
                    # skip to the brace: it is counted when the loop reaches it
        i += 1
    return res


def cierre(texto, mapa, raices):
    pendientes = list(raices)
    elegidas = []
    while pendientes:
        nombre = pendientes.pop(0)
        if nombre in elegidas:
            continue
        if nombre not in mapa:
            raise SystemExit("nested class not found: " + nombre)
        elegidas.append(nombre)
        a, b = mapa[nombre]
        cuerpo = texto[a:b]
        for otro in mapa:
            if otro != nombre and re.search(r"\b" + re.escape(otro) + r"\b", cuerpo) and otro not in elegidas:
                pendientes.append(otro)
    return sorted(elegidas, key=lambda x: mapa[x][0])


def main():
    for ruta, paquete, clase, raices, logica, extras in SHIMS:
        texto = leer(ruta)
        mapa = anidadas(texto)
        elegidas = cierre(texto, mapa, raices)
        cuerpos = []
        for nombre in elegidas:
            a, b = mapa[nombre]
            # the legacy line numbers of the copied class, for the reader
            l1 = texto.count("\n", 0, a) + 1
            l2 = texto.count("\n", 0, b) + 1
            cuerpos.append("\t// legacy %s:%d-%d (verbatim)\n\t%s\n" % (ruta.split("/src/")[1], l1, l2, texto[a:b]))
        todo = "\n".join(cuerpos)
        imports = set(extras)
        imports.update(["java.sql.Connection", "cl.bcs.oracle.Hooks", "cl.bcs.utils.JavaCall", "com.google.gson.JsonObject"])
        for simple, full in KNOWN.items():
            if re.search(r"\b" + simple + r"\b", todo):
                imports.add(full)
        salida = ["package %s;\n" % paquete]
        salida += ["import %s;" % i for i in sorted(imports)]
        salida.append("")
        salida.append("/**")
        salida.append(" * ORACLE FAKE of %s.%s, generated by legacy-oracle/tools/mkshims.py from %s." % (paquete, clase, ruta))
        salida.append(" * The nested data classes are VERBATIM copies of the legacy source; logica() reproduces no behaviour: it records its input in")
        salida.append(" * Hooks (the collaborator trace) and answers the scripted output of the case (case.json \"colaboradores\").")
        salida.append(" */")
        salida.append("public class %s implements JavaCall {" % clase)
        salida.append("")
        salida.append(todo)
        if clase == "ConsultarCustodiaPortafolio":
            salida.append("	/** the legacy static field (ConsultarCustodiaPortafolio.java:53), used by Tabla_Salida's constructor. */")
            salida.append("	public static java.sql.Date fechaInvalida = java.sql.Date.valueOf(\"0001-01-01\");")
        if logica:
            out, entrada, servicio = logica
            if entrada:
                salida.append("\tpublic %s logica(Connection con, %s in) throws Exception {" % (out, entrada))
                salida.append("\t\treturn Hooks.responder(\"%s\", in, %s.class);" % (servicio, out))
                salida.append("\t}")
            else:
                salida.append("\t/** the legacy signature is logica(Connection, Long idEmpresa): the scripted answer or, with none, the legacy exception. */")
                salida.append("\tpublic %s logica(Connection con, Long idEmpresa) throws Exception {" % out)
                salida.append("\t\tJsonObject in = new JsonObject();")
                salida.append("\t\tin.addProperty(\"IdEmpresa\", idEmpresa);")
                salida.append("\t\tHooks.registrar(\"%s\", in);" % servicio)
                salida.append("\t\tcom.google.gson.JsonElement e = Hooks.colaboradores.get(\"%s\");" % servicio)
                salida.append("\t\tif (e == null || e.isJsonNull()) throw new Exception(\"Empresa no existe\"); // ConsultarEmpresa.java:40-41")
                salida.append("\t\tHooks.lanzarSiCorresponde(e);")
                salida.append("\t\t%s r = cl.bcs.utils.Utils.getGsonBuilder().fromJson(e, %s.class);" % (out, out))
                salida.append("\t\treturn r;")
                salida.append("\t}")
        salida.append("")
        salida.append("\t@Override")
        salida.append("\tpublic JsonObject call(Connection conn, JsonObject in) throws Exception {")
        salida.append("\t\treturn null;")
        salida.append("\t}")
        salida.append("}")
        destino = os.path.join(OUT, *paquete.split("."), clase + ".java")
        os.makedirs(os.path.dirname(destino), exist_ok=True)
        with open(destino, "w", encoding="utf-8", newline="\n") as f:
            f.write("\n".join(salida) + "\n")
        print("shim", paquete + "." + clase, "nested:", ", ".join(elegidas))


main()
