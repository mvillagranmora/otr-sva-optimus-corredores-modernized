#!/usr/bin/env python3
"""
Generates the oracle fakes of the collaborators OUTSIDE the APV module whose POJOs the APV legacy classes use (CrearPersona, CrearCliente,
CrearTelefono, CrearDireccionElectronica, EntregarPreciosTasas, AnulacionMandatoPAC, RevocacionMandatoPAC).

The nested POJO classes are copied VERBATIM from the legacy source (brace matched, byte for byte, including their helper methods such as
SalidaEstandar.addError), so the oracle's Gson mapping and defaults are the legacy ones. Only `call` and `logica` are replaced: `logica` records
its input in oracle.Hooks and answers the script colaboradores.<Servicio> of the case.

usage: gen_shims.py <legacy BSCL dir> <shims dir>
"""
import pathlib
import sys

BSCL = pathlib.Path(sys.argv[1])
OUT = pathlib.Path(sys.argv[2])

SN = "SocioNegocio/JAVA/BcsSocioNegocioServicios/src/"
PL = "Plataforma/JAVA/BcsPlataformaServicios/src/"
TE = "Tesoreria/JAVA/BcsTesoreriaServicios/src/"

COMMON = ["java.sql.Connection", "java.util.ArrayList", "java.util.List", "java.util.HashMap", "java.math.BigDecimal",
          "com.google.gson.JsonObject", "cl.bcs.utils.Utils", "cl.bcs.utils.JavaCall", "cl.bcs.oracle.Hooks",
          "cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida"]

SPECS = [
    dict(src=SN + "cl/bcs/socionegocio/per/CrearPersona.java", pkg="cl.bcs.socionegocio.per", cls="CrearPersona",
         imports=["cl.bcs.socionegocio.pojo.*"],
         nested=["EntradaServicioCrearPersona", "SalidaServicioCrearPersona"],
         logica=("SalidaServicioCrearPersona", "Connection conn, EntradaServicioCrearPersona in")),
    dict(src=SN + "cl/bcs/socionegocio/cli/CrearCliente.java", pkg="cl.bcs.socionegocio.cli", cls="CrearCliente",
         imports=["cl.bcs.socionegocio.pojo.*"],
         nested=["EntradaServicioCrearCliente", "SalidaServicioCrearCliente", "ErrorDetalleCall", "AdvertenciaDetalleCall"],
         logica=("SalidaServicioCrearCliente", "Connection conn, EntradaServicioCrearCliente in")),
    dict(src=SN + "cl/bcs/socionegocio/per/CrearTelefono.java", pkg="cl.bcs.socionegocio.per", cls="CrearTelefono",
         imports=["cl.bcs.socionegocio.pojo.*"],
         nested=["EntradaCrearTelefono", "SalidaCrearTelefono"],
         logica=("SalidaCrearTelefono", "Connection conn, EntradaCrearTelefono in")),
    dict(src=SN + "cl/bcs/socionegocio/per/CrearDireccionElectronica.java", pkg="cl.bcs.socionegocio.per", cls="CrearDireccionElectronica",
         imports=["cl.bcs.socionegocio.pojo.*"],
         nested=["EntradaCrearDireccionElectronica", "SalidaCrearDireccionElectronica"],
         logica=("SalidaCrearDireccionElectronica", "Connection conn, EntradaCrearDireccionElectronica in")),
    dict(src=PL + "cl/bcs/plataforma/tas/EntregarPreciosTasas.java", pkg="cl.bcs.plataforma.tas", cls="EntregarPreciosTasas",
         imports=["cl.bcs.utils.BcsPojo"],
         nested=["EntregarPreciosTasas_Entrada", "EntregarPreciosTasas_Entrada_DatosInstrumentos", "EntregarPreciosTasas_Salida",
                 "EntregarPreciosTasas_Salida_PreciosTasas"],
         logica=("EntregarPreciosTasas_Salida", "Connection conn, EntregarPreciosTasas_Entrada in")),
    dict(src=TE + "cl/bcs/tesoreria/AnulacionMandatoPAC.java", pkg="cl.bcs.tesoreria", cls="AnulacionMandatoPAC",
         imports=["cl.bcs.fondomutuo.pojo.ResultadoSolicitud"],
         nested=["InContrato", "EntradaAnulacionContrato", "SalidaAnulacionContrato"],
         logica=("SalidaAnulacionContrato", "Connection conn, EntradaAnulacionContrato in")),
    dict(src=TE + "cl/bcs/tesoreria/RevocacionMandatoPAC.java", pkg="cl.bcs.tesoreria", cls="RevocacionMandatoPAC",
         imports=["cl.bcs.fondomutuo.pojo.ResultadoSolicitud"],
         nested=["InMandato", "EntradaRevocacionMandato", "SalidaRevocacionMandato"],
         logica=("SalidaRevocacionMandato", "Connection conn, EntradaRevocacionMandato in")),
    dict(src="APV/JAVA/BcsAPVServicios/src/cl/bcs/APV/CargaMasivaPrevired.java", pkg="cl.bcs.APV", cls="CargaMasivaPrevired",
         imports=[], nested=["ExistePersona"], logica=None),   # compile-only stand-in: the legacy file does not compile (imports a missing CrearOperacionForward.OperacionForward)
    dict(src="APV/JAVA/BcsAPVServicios/src/cl/bcs/APV/DevolucionPagoRezagos.java", pkg="cl.bcs.APV", cls="DevolucionPagoRezagos",
         imports=[], nested=["IdPortafolioDefecto"], logica=None),   # compile-only stand-in (family C class, only its POJO is referenced by ProcesarDistribucion)
    dict(src="Custodia/JAVA/BcsCustodiaServicios/src/cl/bcs/custodia/utils/ValorizarCustodia.java", pkg="cl.bcs.custodia.utils", cls="ValorizarCustodia",
         imports=[], nested=["ObjUpdateTablas"], logica=None),
]


def extract(text, name):
    """Returns the source of `public static class <name>` (with its leading javadoc/comment lines), brace matched."""
    marker = "static class " + name
    i = text.find(marker)
    while i >= 0:
        nxt = text[i + len(marker)]
        if not (nxt.isalnum() or nxt == "_"):
            break
        i = text.find(marker, i + 1)
    if i < 0:
        raise SystemExit("class %s not found" % name)
    start = text.rfind("\n", 0, i) + 1
    j = text.index("{", i)
    depth = 0
    k = j
    while True:
        c = text[k]
        if c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                break
        k += 1
    return text[start:k + 1]


for sp in SPECS:
    text = (BSCL / sp["src"]).read_text(encoding="utf-8", errors="surrogateescape")
    body = []
    first_line = {}
    for n in sp["nested"]:
        blk = extract(text, n)
        body.append("\t// legacy %s (verbatim)\n%s\n" % (sp["src"].split("/src/")[1] + ":" + str(text[:text.find(blk)].count("\n") + 1), blk))
    out_type, params = sp["logica"] if sp["logica"] else (None, None)
    imports = COMMON + sp["imports"]
    lines = ["package %s;\n" % sp["pkg"]]
    for im in imports:
        lines.append("import %s;" % im)
    lines.append("")
    lines.append("/**")
    lines.append(" * ORACLE FAKE of %s.%s (GENERATED by legacy-oracle/tools/gen_shims.py; do not edit). The nested POJOs are verbatim copies of the" % (sp["pkg"], sp["cls"]))
    lines.append(" * legacy source; logica() only records its input in oracle.Hooks and answers the script colaboradores.%s of the case." % sp["cls"])
    lines.append(" */")
    lines.append("public class %s implements JavaCall {\n" % sp["cls"])
    lines.extend(body)
    lines.append("\t@Override")
    lines.append("\tpublic JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException(\"oracle fake\"); }\n")
    if out_type:
        lines.append("\t/** FAKE of the legacy logica: records the input and answers the case script (see oracle/Hooks.java). */")
        lines.append("\tpublic %s logica(%s) throws Exception {" % (out_type, params))
        lines.append("\t\tHooks.registrar(\"%s\", in);" % sp["cls"])
        lines.append("\t\treturn Hooks.salida(\"%s\", %s.class);" % (sp["cls"], out_type))
        lines.append("\t}")
    lines.append("}")
    dest = OUT / (sp["pkg"].replace(".", "/")) / (sp["cls"] + ".java")
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("generated", dest)
