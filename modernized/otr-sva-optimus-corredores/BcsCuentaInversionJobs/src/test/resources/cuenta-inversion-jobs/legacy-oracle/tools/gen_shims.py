import os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from dump_classes import classes, LEG

R = r"C:\Users\m.villagran.mora\Downloads\develop\nuam-modernizado\modernized\otr-sva-optimus-corredores"
OUT = R + r"\BcsCuentaInversionJobs\src\test\resources\cuenta-inversion-jobs\legacy-oracle\shims"
PLA = r"C:\Users\m.villagran.mora\Downloads\develop\nuam-modernizado\legacy\otr-sva-optimus-corredores\BSCL\Plataforma\JAVA\BcsPlataformaServicios\src\cl\bcs\plataforma\bat\InscribirProcesoBatch.java"

def E(n): return [("Entrada" + n), ("Salida" + n)]
SVC = {
    "ActualizarSaldoCargoAbono": ["ActualizarSaldoCargoAbonoEntrada", "ActualizarSaldoCargoAbonoSalida", "CuentaInversion"],
    "ActualizarSaldosReporteAntiguedad": E("ActualizarSaldosReporteAntiguedad"),
    "CargaInicialSaldosReporteAntiguedad": E("CargaInicialSaldosReporteAntiguedad"),
    "GuardarSaldosReporteAntiguedad": E("GuardarSaldosReporteAntiguedad"),
    "GuardarPatrimonioCaja": E("GuardarPatrimonioCaja"),
    "GenerarInformePatrimonioDiario": E("GenerarInformePatrimonioDiario"),
    "GenerarInformeResumenCobro": E("GenerarInformeResumenCobro"),
    "GenerarArchivoCargoAbono": ["GenerarArchivoCargoAbono_Entrada", "GenerarArchivoCargoAbono_Salida"],
    "GenerarArchivoClientesPorPagarYCobrar": ["EntradaGenerarArchivoClientesPorPagarYCobrar", "SalidaGenerarArchivoClientePorPagarYCobrar", "ClientesPagarCobrar"],
    "GenerarArchivoCuentasPorCobrarAnexo16": ["GenerarArchivoCuentasPorCobrarAnexo16_Entrada", "GenerarArchivoCuentasPorCobrarAnexo16_Salida"],
    "GenerarArchivoSaldoCuentas": ["GenerarArchivoSaldoCuentas_Entrada", "GenerarArchivoSaldoCuentas_Salida"],
    "ConsultaComparacionSaldo": ["ConsultaComparacionSaldoEntrada", "ConsultaComparacionSaldoSalida"],
    "GenerarReporteAcreedores": E("GenerarReporteAcreedores"),
    "GenerarReporteAntiguedadDeudaBolsaCTA": E("GenerarReporteAntiguedadDeudaBolsaCTA"),
    "GenerarReporteAntiguedadDeudaCTA": ["GenerarReporteAntiguedadDeudaCTA_Entrada", "GenerarReporteAntiguedadDeudaCTA_Salida"],
}
KEEP_IMPORT = re.compile(r"^import (java\.|javax\.|com\.google\.gson\.|cl\.bcs\.utils\.(BcsPojo|Utils|JavaCall|Fecha)|cl\.bcs\.plataforma\.pla\.utils\.PlataformaUtils\.MensajeSalida)")

def build(pkg, name, src, nested, entrada, salida):
    cl = dict(classes(src))
    imps = sorted({l.strip() for l in src.splitlines() if KEEP_IMPORT.match(l.strip())})
    body = "\n\n".join("    " + cl[n].replace("\n", "\n    ") for n in nested)
    return f"""package {pkg};

{chr(10).join(imps)}
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy {pkg}.{name}: the Entrada / Salida classes are VERBATIM copies of the legacy nested classes (field names, types and
 * constructor defaults), the service itself is a FAKE: it records {{servicio, entrada}} (Hooks.registrar), may throw what the case scripts
 * ("colaboradores.{name}.lanza"), may run scripted SQL on the job connection ("colaboradores.{name}.sql": the side effects the real
 * service would leave, so commit / rollback are observable in data) and answers "colaboradores.{name}.salida" decoded into the legacy
 * Salida class (an absent field takes the legacy constructor default).
 */
public class {name} implements JavaCall {{

{body}

    public {salida} logica(Connection con, {entrada} entrada) throws Exception {{
        return cl.bcs.oracle.Hooks.servicio("{name}", entrada, {salida}.class, con);
    }}

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {{
        return null;
    }}
}}
"""

def write(pkg, name, text):
    d = os.path.join(OUT, *pkg.split("."))
    os.makedirs(d, exist_ok=True)
    open(os.path.join(d, name + ".java"), "w", encoding="utf-8", newline="\n").write(text)

for name, nested in SVC.items():
    src = open(os.path.join(LEG, name + ".java"), encoding="utf-8", errors="replace").read()
    t = build("cl.bcs.cuentaInversion", name, src, nested, nested[0], nested[1])
    if "import java.sql.Connection;" not in t:
        t = t.replace("import cl.bcs.utils.BcsPojo;", "import java.sql.Connection;\nimport com.google.gson.JsonObject;\nimport cl.bcs.utils.BcsPojo;", 1)
    if "import com.google.gson.JsonObject;" not in t:
        t = t.replace("import cl.bcs.utils.BcsPojo;", "import com.google.gson.JsonObject;\nimport cl.bcs.utils.BcsPojo;", 1)
    write("cl.bcs.cuentaInversion", name, t)
    print("shim", name)
src = open(PLA, encoding="utf-8", errors="replace").read()
t = build("cl.bcs.plataforma.bat", "InscribirProcesoBatch", src,
          ["Parametro", "InTipoPrograma", "OutPrograma", "InscribirProcesoBatchEntrada", "InscribirProcesoBatchSalida"],
          "InscribirProcesoBatchEntrada", "InscribirProcesoBatchSalida")
t = t.replace("import cl.bcs.utils.BcsPojo;", "import java.sql.Connection;\nimport com.google.gson.JsonObject;\nimport cl.bcs.utils.BcsPojo;", 1)
write("cl.bcs.plataforma.bat", "InscribirProcesoBatch", t)
print("shim InscribirProcesoBatch")
