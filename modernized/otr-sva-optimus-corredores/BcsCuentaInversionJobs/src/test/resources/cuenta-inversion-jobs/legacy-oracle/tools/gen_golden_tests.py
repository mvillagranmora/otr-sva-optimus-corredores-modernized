import os
R = r"C:\Users\m.villagran.mora\Downloads\develop\nuam-modernizado\modernized\otr-sva-optimus-corredores\BcsCuentaInversionJobs\src\test\java\cl\bcs\cuentainversion\jobs\job"
os.makedirs(R, exist_ok=True)

# class name, javadoc, [(prefix, legacy class, minimum cases, method name)]
CLASSES = [
 ("SaldosGoldenTest", "balance jobs and PAUTA steps: ActualizarSaldoCargoAbonoJob (AS), ActualizarSaldosReporteAntiguedadJob (AR), CargaInicialSaldosReporteAntiguedadJob (CI), PautaActualizarSaldoCargoAbono (PS), PautaGuardarSaldosReporteAntiguedad (PG)",
  [("AS-", "ActualizarSaldoCargoAbonoJob", 18, "actualizarSaldoCargoAbono"), ("AR-", "ActualizarSaldosReporteAntiguedadJob", 10, "actualizarSaldosReporteAntiguedad"),
   ("CI-", "CargaInicialSaldosReporteAntiguedadJob", 8, "cargaInicialSaldosReporteAntiguedad"), ("PS-", "PautaActualizarSaldoCargoAbono", 7, "pautaActualizarSaldoCargoAbono"),
   ("PG-", "PautaGuardarSaldosReporteAntiguedad", 7, "pautaGuardarSaldosReporteAntiguedad")]),
 ("InformesPatrimonioGoldenTest", "equity and fee reports: BcsJobGenerarInformePatrimonioDiario (IP), BcsJobGenerarInformeResumenCobro (RC), GuardarPatrimonioCajaJob (GP)",
  [("IP-", "BcsJobGenerarInformePatrimonioDiario", 14, "informePatrimonioDiario"), ("RC-", "BcsJobGenerarInformeResumenCobro", 14, "informeResumenCobro"),
   ("GP-", "GuardarPatrimonioCajaJob", 12, "guardarPatrimonioCaja")]),
 ("ArchivosServiciosGoldenTest", "the file jobs that delegate to a service: GenerarArchivoCargoAbonoJob (GC), GenerarArchivoClientesPorPagarYCobrarJob (CP), GenerarArchivoCuentasPorCobrarAnexo16Job (A1), GenerarArchivoDiferenciaSaldoJob (DS), GenerarArchivoSaldoCuentasJob (SC)",
  [("GC-", "GenerarArchivoCargoAbonoJob", 14, "archivoCargoAbono"), ("CP-", "GenerarArchivoClientesPorPagarYCobrarJob", 14, "archivoClientesPorPagarYCobrar"),
   ("A1-", "GenerarArchivoCuentasPorCobrarAnexo16Job", 12, "archivoAnexo16"), ("DS-", "GenerarArchivoDiferenciaSaldoJob", 12, "archivoDiferenciaSaldo"),
   ("SC-", "GenerarArchivoSaldoCuentasJob", 18, "archivoSaldoCuentas")]),
 ("ReportesAntiguedadGoldenTest", "aging and creditor reports: GenerarReporteAcreedoresJob (RA), GenerarReporteAntiguedadDeudaBolsaCTAJob (AB), GenerarReporteAntiguedadDeudaCTAJob (AC), PautaGenerarReporteAcreedores (PR), PautaGenerarReporteAntiguedadDeudaBolsaCTA (PB)",
  [("RA-", "GenerarReporteAcreedoresJob", 14, "reporteAcreedores"), ("AB-", "GenerarReporteAntiguedadDeudaBolsaCTAJob", 14, "reporteAntiguedadBolsa"),
   ("AC-", "GenerarReporteAntiguedadDeudaCTAJob", 14, "reporteAntiguedad"), ("PR-", "PautaGenerarReporteAcreedores", 7, "pautaReporteAcreedores"),
   ("PB-", "PautaGenerarReporteAntiguedadDeudaBolsaCTA", 7, "pautaReporteAntiguedadBolsa")]),
 ("GenerarArchivoGoldenTest", "the generic file generator GenerarArchivo (with Archivo, FormatoArchivo, FormatoArchivoDetalle): structure, TXT / CSV, directories, failures (GA-nn) and the field formatter (GF-nn)",
  [("GA-", "GenerarArchivo", 36, "generarArchivo"), ("GF-", "GenerarArchivo (formatter)", 36, "formatoDeCampos")]),
 ("LibroInventarioGoldenTest", "LibroInventarioJob: the stored procedure ActualizarSaldoLibroInventario, parameters and failures (LI)",
  [("LI-", "LibroInventarioJob", 14, "libroInventario")]),
]

for name, doc, items in CLASSES:
    counters = "\n".join("    private static final AtomicInteger EJECUTADOS_%s = new AtomicInteger();" % p.rstrip("-") for p, *_ in items)
    methods = []
    for p, legacy, minimo, metodo in items:
        k = p.rstrip("-")
        methods.append(f'''
    static List<String> casos{k}() {{
        return Caso.todos("{p}");
    }}

    @Test
    void inventario{k}() {{
        assertThat(casos{k}()).as("{k} cases on the classpath ({legacy})").hasSizeGreaterThanOrEqualTo({minimo});
    }}

    @ParameterizedTest(name = "{{0}}")
    @MethodSource("casos{k}")
    void {metodo}(String id) {{
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_{k}.incrementAndGet();
    }}''')
    counts = "\n".join('        System.out.println("equivalence cases executed (%s): " + EJECUTADOS_%s.get() + "/" + casos%s().size());' % (legacy, p.rstrip("-"), p.rstrip("-")) for p, legacy, *_ in items)
    src = f'''package cl.bcs.cuentainversion.jobs.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.cuentainversion.jobs.support.Caso;
import cl.bcs.cuentainversion.jobs.support.Equivalencia;
import cl.bcs.cuentainversion.jobs.support.JobsDeCaso;

/**
 * Legacy {doc}, against the goldens recorded by executing the UNMODIFIED legacy classes under the real legacy
 * {{@code BcsJob.run()}} (the PAUTA steps are called directly): outcome, exception, status calls, the order of commits and rollbacks, the
 * tables, the files (bytes) and what the code handed to each collaborator. The cases are under
 * {{@code src/test/resources/cuenta-inversion-jobs/cases}}; the prefix of a case id selects its job.
 */
class {name} {{

{counters}
{"".join(methods)}

    @AfterAll
    static void contar() {{
{counts}
    }}
}}
'''
    open(os.path.join(R, name + ".java"), "w", encoding="utf-8", newline="\n").write(src)
    print("wrote", name, len(src.splitlines()))
