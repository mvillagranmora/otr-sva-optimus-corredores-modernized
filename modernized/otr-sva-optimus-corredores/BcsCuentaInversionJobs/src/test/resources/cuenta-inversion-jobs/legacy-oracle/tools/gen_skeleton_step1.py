import os
R = r"C:\Users\m.villagran.mora\Downloads\develop\nuam-modernizado\modernized\otr-sva-optimus-corredores\BcsCuentaInversionJobs\src\main\java\cl\bcs\cuentainversion\jobs"
JDBC = ("org.springframework.jdbc.core.JdbcTemplate", "jdbc")
CFG = ("cl.bcs.cuentainversion.jobs.job.port.ConfiguracionJobs", "configuracion")
COLAB = ("cl.bcs.cuentainversion.port.out.Colaboradores", "colaboradores")
def svc(n): return ("cl.bcs.cuentainversion.service.%sService" % n, n[0].lower() + n[1:] + "Service")

# name, legacy package, legacy lines, doc, ctor, kind
JOBS = [
 ("ActualizarSaldoCargoAbonoJob", "", 170, "recalculates the balances of the selected accounts; marks BAT.TipoProgramaEjecucion EJEC / empty around the run", [svc("ActualizarSaldoCargoAbono"), JDBC], "job"),
 ("ActualizarSaldosReporteAntiguedadJob", "", 100, "updates the aging report balances of one client", [svc("ActualizarSaldosReporteAntiguedad")], "job"),
 ("CargaInicialSaldosReporteAntiguedadJob", "", 86, "initial load of the aging report balances", [svc("CargaInicialSaldosReporteAntiguedad")], "job"),
 ("BcsJobGenerarInformePatrimonioDiario", "", 91, "daily equity report; updates BAT.Programa with the file names", [svc("GenerarInformePatrimonioDiario"), CFG, JDBC], "job"),
 ("BcsJobGenerarInformeResumenCobro", "", 89, "fee summary report; updates BAT.Programa with the file names", [svc("GenerarInformeResumenCobro"), CFG, JDBC], "job"),
 ("GenerarArchivoCargoAbonoJob", "", 183, "debit / credit movements file; updates BAT.Programa", [svc("GenerarArchivoCargoAbono"), JDBC], "job"),
 ("GenerarArchivoClientesPorPagarYCobrarJob", "", 162, "receivables / payables file; a service error becomes a java.lang.Error", [svc("GenerarArchivoClientesPorPagarYCobrar"), JDBC], "job"),
 ("GenerarArchivoCuentasPorCobrarAnexo16Job", "", 120, "annex 16 receivables file; updates BAT.Programa", [svc("GenerarArchivoCuentasPorCobrarAnexo16"), JDBC], "job"),
 ("GenerarArchivoDiferenciaSaldoJob", "", 150, "balance difference file; updates BAT.Programa", [svc("ConsultaComparacionSaldo"), JDBC], "job"),
 ("GenerarArchivoSaldoCuentasJob", "", 188, "account balances file (CSV zipped); updates BAT.Programa", [svc("GenerarArchivoSaldoCuentas"), JDBC], "job"),
 ("GenerarReporteAcreedoresJob", "", 137, "creditors report; updates BAT.Programa", [svc("GenerarReporteAcreedores"), JDBC], "job"),
 ("GenerarReporteAntiguedadDeudaBolsaCTAJob", "", 140, "stock exchange debt aging report; updates BAT.Programa", [svc("GenerarReporteAntiguedadDeudaBolsaCTA"), JDBC], "job"),
 ("GenerarReporteAntiguedadDeudaCTAJob", "", 158, "debt aging report; updates BAT.Programa", [svc("GenerarReporteAntiguedadDeudaCTA"), JDBC], "job"),
 ("GuardarPatrimonioCajaJob", "", 96, "saves the cash equity of a date", [svc("GuardarPatrimonioCaja"), CFG], "job"),
 ("LibroInventarioJob", "", 95, "calls the stored procedure ActualizarSaldoLibroInventario", [("cl.bcs.cuentainversion.jobs.job.port.LibroInventario", "libroInventario")], "job"),
 ("GenerarArchivo", "generarArchivo.", 299, "generic file generator driven by CTA.FormatoArchivo (with Archivo, FormatoArchivo, FormatoArchivoDetalle)",
  [("cl.bcs.cuentainversion.jobs.job.port.ProcedimientoArchivo", "procedimientos"), JDBC, ("java.time.Clock", "clock")], "job"),
 ("PautaActualizarSaldoCargoAbono", "", 70, "pauta: recalculates every balance", [CFG, svc("ActualizarSaldoCargoAbono")], "pauta"),
 ("PautaGuardarSaldosReporteAntiguedad", "", 43, "pauta: saves the aging report balances", [CFG, svc("GuardarSaldosReporteAntiguedad")], "pauta"),
 ("PautaGenerarReporteAcreedores", "", 76, "pauta: enrolls the creditors report as a batch program", [CFG, COLAB], "pauta"),
 ("PautaGenerarReporteAntiguedadDeudaBolsaCTA", "", 77, "pauta: enrolls the stock exchange debt aging report", [CFG, COLAB], "pauta"),
]

def gen(name, pkg, lines, doc, ctor, kind):
    fqn = "cl.bcs.cuentaInversion.jobs." + pkg + name
    imports = {"java.util.Objects"}
    for t, _ in ctor:
        imports.add(t)
    if kind == "job":
        imports |= {"cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob", "cl.bcs.cuentainversion.jobs.runtime.JobContext"}
        iface, tx = "CuentaInversionJob", "    @Override\n    public boolean transaction(JobContext ctx) throws Exception {\n"
    else:
        imports |= {"cl.bcs.cuentainversion.jobs.runtime.ProcesoPauta", "cl.bcs.cuentainversion.jobs.runtime.SalidaPauta", "java.util.Map"}
        iface, tx = "ProcesoPauta", "    @Override\n    public SalidaPauta transaction(Map<String, Object> parametros) throws Exception {\n"
    sub = "job.pauta" if kind == "pauta" else "job"
    pk = "cl.bcs.cuentainversion.jobs." + sub
    imports = sorted(i for i in imports if not i.startswith(pk + ".") or i.count(".") != pk.count(".") + 1)
    imps = "\n".join("import %s;" % i for i in imports)
    fields = "\n".join("    private final %s %s;" % (t.split(".")[-1], n) for t, n in ctor)
    params = ", ".join("%s %s" % (t.split(".")[-1], n) for t, n in ctor)
    assigns = "\n".join('        this.%s = Objects.requireNonNull(%s, "%s");' % (n, n, n) for _, n in ctor)
    return f"""package {pk};

{imps}

/**
 * Legacy {{@code {fqn}}} ({lines} lines): {doc}.
 *
 * <p>SKELETON of Step 1 (characterization tests only): every call throws {{@link UnsupportedOperationException}}; Step 2 ports the
 * legacy logic bug for bug (see the module test README, CIJ-DEF-nn). RULE-061 (no daemon / running flag), RULE-076 (debug main()
 * and debug mode not ported), RULE-077 (no 3DES).
 */
public final class {name} implements {iface} {{

    public static final String CLASE = "{fqn}";

{fields}

    public {name}({params}) {{
{assigns}
    }}

    @Override
    public String clase() {{
        return CLASE;
    }}

{tx}        throw new UnsupportedOperationException("TODO Step 2: {fqn}");
    }}
}}
"""

for j in JOBS:
    name, pkg, lines, doc, ctor, kind = j
    d = os.path.join(R, "job", "pauta") if kind == "pauta" else os.path.join(R, "job")
    os.makedirs(d, exist_ok=True)
    open(os.path.join(d, name + ".java"), "w", encoding="utf-8", newline="\n").write(gen(*j))
    print("wrote", kind, name)
