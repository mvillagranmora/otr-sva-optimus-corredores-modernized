"""F3 W3 T2 canary of the REAL code of fronts F-C (aportes) and F-D (rescates) of CertificarSolicitudAporteRescate (plan F3 section 4: break the code in one small way that
matters, confirm that at least one test goes red, restore). Nothing under legacy/ or under the real module is ever touched:

  py -3 -I canary_cer_t2.py <copy of the module outside the repo> [text of the mutation name]

The copy is made by the caller: a directory that holds the parent pom.xml and BcsFondoMutuoServicios/ (same layout as modernized/otr-sva-optimus-corredores). Each mutation is one
line (or one swap of two adjacent lines) of the production package cl.bcs.fondomutuo.certificacion inside the COPY; after it `mvn -o -q test -Dtest='CerT2*Test'` runs in the copy and the
mutation must make at least one test fail; the file is restored right after (and compared with its original bytes).
"""
import pathlib
import shutil
import subprocess
import sys

BASE = pathlib.Path(__file__).resolve().parent
WS = BASE.parents[3]
MVN = shutil.which("mvn") or "mvn"
DIR = "BcsFondoMutuoServicios/src/main/java/cl/bcs/fondomutuo/certificacion/"
A = "CertificarAporteRescateAportes.java"
R = "CertificarAporteRescateRescates.java"
C = "CertificarAporteRescateCustodia.java"

MUTACIONES = [
    ("saldo PAC: el canal PAC se mezcla con el resto (totales juntos)", A, 'if (pac == "PAC".equals(sol.IdTipoCanalIngreso)) {', "if (true) {"),
    ("saldo: el total igual al saldo tambien rechaza", A, "if (total.compareTo(saldo) == 1) {", "if (total.compareTo(saldo) >= 0) {"),
    ("saldo: el sobregiro se ignora", A, "return SI.equals(sol.MueveCta) && ORIGEN_MANUAL.equals(sol.OrigenOperacion) && NO.equals(sobregiro);", "return SI.equals(sol.MueveCta) && ORIGEN_MANUAL.equals(sol.OrigenOperacion);"),
    ("orden de pasos: primero el flujo PAC y despues el no PAC", A, "validarSaldoCuentaInversion(ctx, false);\n            validarSaldoCuentaInversion(ctx, true);",
     "validarSaldoCuentaInversion(ctx, true);\n            validarSaldoCuentaInversion(ctx, false);"),
    ("costo UF: el aporte hereda el valor UF del rescate y no su costo", A, "aporte.CostoUF = aporte.MontoMonedaSistema.divide(valorUFRescate, MathContext.DECIMAL64);", "aporte.CostoUF = valorUFRescate;"),
    ("orden: la rentabilidad real se calcula antes de rechazar por los mensajes", A,
     "CertificarAporteRescateCustodia.rechazarPorMensajes(ctx.salida, salidaCustodia.P_OUT_MENSAJES, ctx.aportes);\n            calcularRentabilidadRealAportes(ctx.aportes, salidaCustodia);",
     "calcularRentabilidadRealAportes(ctx.aportes, salidaCustodia);\n            CertificarAporteRescateCustodia.rechazarPorMensajes(ctx.salida, salidaCustodia.P_OUT_MENSAJES, ctx.aportes);"),
    ("custodia disponible: ADV con omnibus 0 tambien rechaza", R, "return idOmnibus != null && idOmnibus > 0;", "return idOmnibus != null;"),
    ("rescate total: se toma la cantidad de custodia sin mirar el omnibus", R, "(CALCULO_POR_TOTAL.equals(sol.CalculoPorMontoCuota) && esOmnibusPorProcesar(sol.IdOmnibus))", "CALCULO_POR_TOTAL.equals(sol.CalculoPorMontoCuota)"),
    ("traspaso: la solicitud relacionada ya no marca el traspaso", R, " || hashTraspasos.get(sol.IdSolicitudAporteRescateRelacionado) != null", ""),
    ("retencion: el Spot no impide la retencion", R, "&& !SI.equals(sol.GeneraSpot) && porcentajeRetencion.compareTo(0L) != 0", "&& porcentajeRetencion.compareTo(0L) != 0"),
    ("retencion: se calcula sobre el costo local y no sobre el monto", R, "sol.MontoRetencion = sol.Monto.multiply(", "sol.MontoRetencion = sol.CostoMonedaLocal.multiply("),
    ("comision: se aplica aunque haya comision NN relacionada", R, "if (comision != null && NO.equals(solicitud.TieneComisionNNRelacionada)) {", "if (comision != null) {"),
    ("retiro: un rescate sin movimiento de retiro ya no se rechaza", R, "if (cantidadCarterasRetiradas == 0) {", "if (cantidadCarterasRetiradas < 0) {"),
    ("retiro: las dos llamadas a custodia se invierten (sin regimen primero)", R, "CertificarAporteRescateCustodia.ingresoRetiro(custodia, rescatesConRegimen, ctx.entrada);\n            ctx.salida.P_OUT_ERRORESCONTADOR += salidaConRegimen.P_OUT_ERRORESCONTADOR;\n\n            CustodiaSalida salidaSinRegimen = CertificarAporteRescateCustodia.ingresoRetiro(custodia, rescatesSinRegimen, ctx.entrada);",
     "CertificarAporteRescateCustodia.ingresoRetiro(custodia, rescatesSinRegimen, ctx.entrada);\n            ctx.salida.P_OUT_ERRORESCONTADOR += salidaConRegimen.P_OUT_ERRORESCONTADOR;\n\n            CustodiaSalida salidaSinRegimen = CertificarAporteRescateCustodia.ingresoRetiro(custodia, rescatesConRegimen, ctx.entrada);"),
    ("spread: el signo de la resta al reves", R, "sol.Spread = sol.CostoUF.subtract(costoUF, MathContext.DECIMAL128);", "sol.Spread = costoUF.subtract(sol.CostoUF, MathContext.DECIMAL128);"),
    ("rentabilidad: la UTM no se usa", R, "sol.MontoMonedaSistema.divide(valorUTM, MathContext.DECIMAL128)", "sol.MontoMonedaSistema.divide(BigDecimal.ONE, MathContext.DECIMAL128)"),
    ("formato: los rescates no se formatean", R, "CertificarAporteRescateReglas.formatearMontos(formato, rescates);", ""),
    ("custodia: la fecha de origen de la compra siempre es la de la cuota", C, "mov.FechaOrigenCompra = APORTE_REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo) &&", "mov.FechaOrigenCompra = false &&"),
]


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    copia = pathlib.Path(sys.argv[1]).resolve()
    filtro = sys.argv[2] if len(sys.argv) > 2 else ""
    real = (WS / "modernized").resolve()
    if real in copia.parents or copia == real:
        sys.exit("refusing to mutate or run Maven inside the real modernized/ tree: pass a COPY outside the repository")
    total = rojas = 0
    for nombre, archivo, viejo, nuevo in MUTACIONES:
        if filtro and filtro not in nombre:
            continue
        f = copia / DIR / archivo
        crudo = f.read_bytes()
        original = crudo.decode("utf-8")
        if viejo not in original:
            print("%-84s: NOT APPLICABLE (text not found in %s)" % (nombre, archivo), flush=True)
            continue
        try:
            f.write_bytes(original.replace(viejo, nuevo, 1).encode("utf-8"))
            r = subprocess.run([MVN, "-o", "-q", "test", "-Dtest=CerT2*Test"], cwd=str(copia / "BcsFondoMutuoServicios"), capture_output=True, text=True)
        finally:
            f.write_bytes(crudo)
        if f.read_bytes() != crudo:
            sys.exit("RESTORE FAILED for " + archivo)
        salida = r.stdout + r.stderr
        compila = "COMPILATION ERROR" not in salida
        rojo = r.returncode != 0 and compila
        fallos = [l for l in salida.splitlines() if "Tests run:" in l and "Fail" in l and "in cl." not in l]
        total += 1
        rojas += 1 if rojo else 0
        print("%-84s: %s %s" % (nombre, "RED (tests fail)" if rojo else ("DOES NOT COMPILE" if not compila else "GREEN: NO TEST FAILS (hole)"), (fallos[-1].strip() if fallos else "")), flush=True)
    print("new-code mutations that turn at least one test red: %d of %d" % (rojas, total))


if __name__ == "__main__":
    main()
