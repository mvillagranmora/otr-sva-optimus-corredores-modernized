"""F3 W3 T4-B mutations of the REFERENCE implementation of fronts F-G (Facturacion) and F-I (reinversion and switch) of CertificarSolicitudAporteRescate (plan F3 section 4: break the code in
one small way that matters, confirm that at least one test goes red, restore). The reference lives in a COPY of the module OUTSIDE the repository (it is not delivered: T4-B only delivers
the tests and the oracle). Nothing under legacy/ or under the real module is ever touched:

  py -3 -I canary_cer_t4.py <copy of the module outside the repo> [text of the mutation name]

The copy is made by the caller: a directory that holds the parent pom.xml and BcsFondoMutuoServicios/ (same layout as modernized/otr-sva-optimus-corredores). Each mutation is one line (or one
swap of two adjacent lines) of the production package cl.bcs.fondomutuo.certificacion (or of kit/Compuertas) inside the COPY; after it `mvn -o -q test -Dtest=CerT4*Test -Dcer.corridas=2` runs in
the copy and the mutation must make at least one test fail; the file is restored right after (and compared with its original bytes). The 5th element of a mutation is the occurrence (1 = the
first) of the text.
"""
import pathlib
import shutil
import subprocess
import sys

BASE = pathlib.Path(__file__).resolve().parent
WS = BASE.parents[3]
MVN = shutil.which("mvn") or "mvn"
DIR = "BcsFondoMutuoServicios/src/main/java/cl/bcs/fondomutuo/certificacion/"
F = "CertificarAporteRescateFacturacion.java"
I = "CertificarAporteRescateReinversion.java"
W = "CertificarAporteRescateSwitch.java"
K = "../kit/Compuertas.java"
R = "CertificarAporteRescateRescates.java"

MUTACIONES = [
    # ---- F-G Facturacion
    ("G01 la facturacion tambien incluye a los rescates rechazados", F, "&& !ESTADO_RECHAZADO.equals(sol.EstadoProceso) && ((RESCATE_COMISION.equals", "&& ((RESCATE_COMISION.equals", 1),
    ("G02 RESTRAPOR ya no se factura", F, "|| RESCATE_FONDO_MUTUO.equals(sol.IdTipoMovimientoFondoMutuo) || RESCATE_TRASPASO_PORTAFOLIO.equals(sol.IdTipoMovimientoFondoMutuo))\n", "|| RESCATE_FONDO_MUTUO.equals(sol.IdTipoMovimientoFondoMutuo))\n", 1),
    ("G03 la comision cero tambien se factura", F, "sol.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1 && !SI.equals(sol.RelacionTraspaso))) {", "sol.MontoComisionLocal.compareTo(BigDecimal.ZERO) >= 0 && !SI.equals(sol.RelacionTraspaso))) {", 1),
    ("G04 el rescate con traspaso tambien se factura", F, "sol.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1 && !SI.equals(sol.RelacionTraspaso))) {", "sol.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1)) {", 1),
    ("G05 la cantidad del movimiento es 2", F, "mov.Cantidad = new BigDecimal(1);", "mov.Cantidad = new BigDecimal(2);", 1),
    ("G06 el precio usa DECIMAL64", F, "mov.Precio = new BigDecimal(0).equals(sol.MontoComisionLocal) ? sol.MontoComisionLocal : (sol.MontoComisionLocal.divide(calculaImporteIva, MathContext.DECIMAL128));",
     "mov.Precio = new BigDecimal(0).equals(sol.MontoComisionLocal) ? sol.MontoComisionLocal : (sol.MontoComisionLocal.divide(calculaImporteIva, MathContext.DECIMAL64));", 1),
    ("G07 el monto neto usa DECIMAL64", F, "mov.Monto = new BigDecimal(0).equals(sol.MontoComisionLocal) ? sol.MontoComisionLocal : (sol.MontoComisionLocal.divide(calculaImporteIva, MathContext.DECIMAL128));",
     "mov.Monto = new BigDecimal(0).equals(sol.MontoComisionLocal) ? sol.MontoComisionLocal : (sol.MontoComisionLocal.divide(calculaImporteIva, MathContext.DECIMAL64));", 1),
    ("G08 el IVA se divide por 10", F, ".divide(BigDecimal.valueOf(100));", ".divide(BigDecimal.valueOf(10));", 1),
    ("G09 el divisor del neto no suma el 1", F, "BigDecimal calculaImporteIva = iva.add(new BigDecimal(1));", "BigDecimal calculaImporteIva = iva;", 1),
    ("G10 el RESCOM factura el monto de la comision", F, "mov.Monto = sol.CostoMonedaLocal;", "mov.Monto = sol.MontoComisionLocal;", 1),
    ("G11 el concepto APV de FOE se invierte", F, "mov.IdConcepto = APV.equals(sol.PropositoPortafolio) ? COSAPV : COSAFM;", "mov.IdConcepto = APV.equals(sol.PropositoPortafolio) ? COSAFM : COSAPV;", 1),
    ("G12 el comprobante APV del RESCOM se invierte", F, "APV.equals(sol.PropositoPortafolio) ? ctx.tipoComprobanteFacturaApv : ctx.tipoComprobanteFacturaNoApv", "APV.equals(sol.PropositoPortafolio) ? ctx.tipoComprobanteFacturaNoApv : ctx.tipoComprobanteFacturaApv", 1),
    ("G13 el concepto del RESCOM se invierte", F, "mov.IdConcepto = APV.equals(sol.PropositoPortafolio) ? COCAPV : COAFFM;", "mov.IdConcepto = APV.equals(sol.PropositoPortafolio) ? COAFFM : COCAPV;", 1),
    ("G14 el RESNORM no actualiza la cuenta", F, "mov.ActualizarCuentaInversion = SI;", "mov.ActualizarCuentaInversion = NO;", 1),
    ("G15 el cobro tambien con monto cero", F, "if (mov.Monto.compareTo(BigDecimal.ZERO) == 1) {", "if (mov.Monto.compareTo(BigDecimal.ZERO) >= 0) {", 1),
    ("G16 el porcentaje del cobro es el IVA fraccion", F, "movcob.Porcentaje = new BigDecimal(cobros.get(0).ValorCobro.toString());", "movcob.Porcentaje = iva;", 1),
    ("G17 el RESTRAPOR tambien lleva forma de pago", F, "if (RESCATE_FONDO_MUTUO.equals(sol.IdTipoMovimientoFondoMutuo)) {\n                    FormaPagoEntrada",
     "if (RESCATE_FONDO_MUTUO.equals(sol.IdTipoMovimientoFondoMutuo) || RESCATE_TRASPASO_PORTAFOLIO.equals(sol.IdTipoMovimientoFondoMutuo)) {\n                    FormaPagoEntrada", 1),
    ("G18 la forma de pago lleva el costo del rescate", F, "movfp.MontoLiquidacionFormaDePago = sol.MontoComisionLocal;", "movfp.MontoLiquidacionFormaDePago = sol.CostoMonedaLocal;", 1),
    ("G19 la facturacion recibe la simulacion de la entrada", F, "entrada.p_simularOperacion = NO;\n            entrada.p_UsaReglasNegocio = SI;\n            entrada.p_EsIngresoAutomatico = NO;\n            JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();\n            salida = JsonLegacy.conNulos().fromJson(facturacion.crearMovimientoComprobante",
     "entrada.p_simularOperacion = ctx.entrada.p_SimularOperacion;\n            entrada.p_UsaReglasNegocio = SI;\n            entrada.p_EsIngresoAutomatico = NO;\n            JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();\n            salida = JsonLegacy.conNulos().fromJson(facturacion.crearMovimientoComprobante", 1),
    ("G20 la facturacion recibe las reglas de la entrada", F, "entrada.p_UsaReglasNegocio = SI;\n            entrada.p_EsIngresoAutomatico = NO;\n            JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();\n            salida = JsonLegacy.conNulos().fromJson(facturacion.crearMovimientoComprobante",
     "entrada.p_UsaReglasNegocio = ctx.entrada.p_UsaReglasNegocio;\n            entrada.p_EsIngresoAutomatico = NO;\n            JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();\n            salida = JsonLegacy.conNulos().fromJson(facturacion.crearMovimientoComprobante", 1),
    ("G21 el parametro del comprobante se compara con equals", F, "SI.equalsIgnoreCase(ctx.comisionComprobanteFacturacionAutomatico)", "SI.equals(ctx.comisionComprobanteFacturacionAutomatico)", 1),
    ("G22 se suma el contador del comprobante y no el del movimiento", F, "// D-F3B-04: the counter that is added is the one of the MOVEMENT, again\n            ctx.salida.P_OUT_ERRORESCONTADOR += salidaMovimiento.P_OUT_ERRORESCONTADOR;",
     "// D-F3B-04: the counter that is added is the one of the MOVEMENT, again\n            ctx.salida.P_OUT_ERRORESCONTADOR += salidaComprobante.P_OUT_ERRORESCONTADOR;", 1),
    ("G23 el contador del movimiento no se suma", F, "        ctx.salida.P_OUT_ERRORESCONTADOR += salidaMovimiento.P_OUT_ERRORESCONTADOR;\n        rechazarPorErrores(ctx, salidaMovimiento.P_OUT_MENSAJES, rescates);", "        rechazarPorErrores(ctx, salidaMovimiento.P_OUT_MENSAJES, rescates);", 1),
    ("G24 el id del error no usa el valor absoluto", F, "Long id = Math.abs(msj.id);", "Long id = msj.id;", 1),
    ("G25 el tipo ERROR se compara sin distinguir mayusculas", F, "MensajeSalida.MENSAJE_ERROR.equals(msj.tipo)", "MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(msj.tipo)", 1),
    ("G26 el comprobante ya no exige RESNORM o RESREINV", F, "((RESCATE_FONDO_MUTUO.equals(res.IdTipoMovimientoFondoMutuo) || REINVERSION.equals(res.IdTipoMovimientoFondoMutuo))", "((true || REINVERSION.equals(res.IdTipoMovimientoFondoMutuo))", 1),
    ("G27 el comprobante ya no incluye al rescate de reinversion", F, "(RESCATE_FONDO_MUTUO.equals(res.IdTipoMovimientoFondoMutuo) || REINVERSION.equals(res.IdTipoMovimientoFondoMutuo))", "(RESCATE_FONDO_MUTUO.equals(res.IdTipoMovimientoFondoMutuo))", 1),
    ("G28 el comprobante tambien incluye a los rechazados", F, "if (!ESTADO_RECHAZADO.equals(res.EstadoProceso) && ((", "if (true && ((", 1),
    ("G29 el comprobante se fecha con la certificacion", F, "movimientoAFacturar.FechaComprobante = ctx.fechaSistema;", "movimientoAFacturar.FechaComprobante = ctx.entrada.p_FechaCertificacion;", 1),
    ("G30 se factura aunque no haya rescates (el IVA se lee siempre)", F, "if (ctx.rescates.size() == 0) return;", "", 1),
    ("G31 la fecha de liquidacion del movimiento es la de certificacion", F, "mov.FechaLiquidacion = FechaLegacy.texto(sol.FechaLiquidacion);", "mov.FechaLiquidacion = FechaLegacy.texto(sol.FechaCertificacion);", 1),
    ("G32 el comprobante incluye los rescates con traspaso", F, "&& res.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1 && !SI.equals(res.RelacionTraspaso))) {", "&& res.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1)) {", 1),
    ("G33 el proposito APV se reconoce como PROPOSITOAPV", F, 'private static final String APV = "APV";', 'private static final String APV = "PROPOSITOAPV";', 1),
    # ---- F-I reinversion
    ("I01 la familia vacia tambien cuenta como misma familia", I, "(!sol.IdFamiliaFondo.isEmpty() && sol.IdFamiliaFondo.equals(apor.IdFamiliaFondo))", "(sol.IdFamiliaFondo.equals(apor.IdFamiliaFondo))", 1),
    ("I02 la moneda del destino no se compara", I, "sol.IdFamiliaFondo.equals(apor.IdFamiliaFondo)) && sol.IdMoneda.equals(apor.IdMoneda)) {", "sol.IdFamiliaFondo.equals(apor.IdFamiliaFondo))) {", 1),
    ("I03 ya no se exige el tipo de reinversion", I, "if (!ESTADO_RECHAZADO.equals(sol.EstadoProceso) && REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo)) {", "if (!ESTADO_RECHAZADO.equals(sol.EstadoProceso)) {", 1),
    ("I04 los rescates rechazados tambien reinvierten", I, "if (!ESTADO_RECHAZADO.equals(sol.EstadoProceso) && REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo)) {", "if (REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo)) {", 1),
    ("I05 la razon de cuotas usa DECIMAL32", I, "salidaMovimiento.Cantidad.divide(sol.NumeroCuotas, MathContext.DECIMAL128)", "salidaMovimiento.Cantidad.divide(sol.NumeroCuotas, MathContext.DECIMAL32)", 1),
    ("I06 el aporte en cero tambien se agrega", I, "if (aporte.Monto.compareTo(BigDecimal.ZERO) == 1) {", "if (aporte.Monto.compareTo(BigDecimal.ZERO) >= 0) {", 1),
    ("I07 el correlativo parte de cero", I, "Long c = 1L;", "Long c = 0L;", 1),
    ("I08 el costo UF del aporte sale del rescate y no de la cartera", I, "aporte.CostoUF = salidaMovimiento.CostoInversionEnFactorAjusteARetirar.multiply(", "aporte.CostoUF = sol.CostoUF.multiply(", 1),
    ("I09 la fecha de origen es la de certificacion y no la de la cartera", I, "aporte.FechaOrigenInversion = FechaLegacy.texto(FechaLegacy.aFechaSql(salidaMovimiento.FechaOrigenCompra));", "aporte.FechaOrigenInversion = FechaLegacy.texto(sol.FechaCertificacion);", 1),
    ("I10 el switch del destino no se mira en la fecha de emision", I, '&& "S".equals(solReinversionSerie.FondoDestinoInternacionalDolar) && "N".equals(apor.Switch)) {\n                                            aporte.FechaEmision',
     '&& "S".equals(solReinversionSerie.FondoDestinoInternacionalDolar)) {\n                                            aporte.FechaEmision', 1),
    ("I11 la fecha de emision internacional es la de liquidacion", I, "aporte.FechaEmision = FechaLegacy.texto(sol.FechaCertificacion);\n                                        } else {", "aporte.FechaEmision = FechaLegacy.texto(sol.FechaLiquidacion);\n                                        } else {", 1),
    ("I12 la fecha spot por moneda de sistema usa la certificacion", I, "aporte.FechaCertificaSpotCompra = fechaTexto(sol.FechaAceptacion);", "aporte.FechaCertificaSpotCompra = fechaTexto(sol.FechaCertificacion);", 1),
    ("I13 la condicion de moneda de sistema se invierte", I, "if (sol.IdMonedaSistema.equals(sol.IdMoneda)) {", "if (!sol.IdMonedaSistema.equals(sol.IdMoneda)) {", 1),
    ("I14 el ajuste va al ultimo aporte", I, "collect(Collectors.toList()).get(0);", "collect(Collectors.toList()).stream().reduce((a, b) -> b).get();", 1),
    ("I15 el ajuste final se hace antes del ultimo destino", I, "if (indexTotal.compareTo(tamano) == 1) {", "if (indexTotal.compareTo(tamano) >= 0) {", 1),
    ("I16 AR recibe la simulacion de la entrada", I, "entrada.p_SimularOperacion = NO;", "entrada.p_SimularOperacion = ctx.entrada.p_SimularOperacion;", 1),
    ("I17 AR recibe el canal de la entrada", I, "entrada.p_IdTipoCanalIngreso = CANAL_AGENTE;", "entrada.p_IdTipoCanalIngreso = ctx.entrada.p_IdTipoCanalIngreso;", 1),
    ("I18 AR recibe siempre las reglas S", I, "entrada.p_UsaReglasNegocio = ctx.entrada.p_UsaReglasNegocio;", "entrada.p_UsaReglasNegocio = SI;", 1),
    ("I19 el correlativo del mensaje no se mapea al rescate", I, "if (map.get(msj.id) != null) msj.id = map.get(msj.id);", "", 1),
    ("I20 el switch se compara sin distinguir mayusculas", I, 'if (res.Switch.equals("S")) {', 'if (res.Switch.equalsIgnoreCase("S")) {', 1),
    ("I21 el contador de AR no se suma", I, "ctx.salida.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;", "", 1),
    ("I22 el id del error de AR no usa el valor absoluto", I, "Long id = Math.abs(msj.id);", "Long id = msj.id;", 1),
    ("I23 el canal del aporte es AGE", I, "aporte.Canal = sol.IdTipoCanalIngreso;", 'aporte.Canal = "AGE";', 1),
    ("I24 el aporte no va visado", I, "aporte.Visado = SI;", "aporte.Visado = NO;", 1),
    ("I25 el aporte de otra familia no suma al total de montos", I, "montoCuotasTotal = montoCuotasTotal.add(aporte.Monto, MathContext.DECIMAL128);\n                        entrada.p_SolicitudesAporte.add(aporte);", "entrada.p_SolicitudesAporte.add(aporte);", 1),
    ("I26 los destinos se recorren en orden de clave", I, "Collectors.groupingBy(s -> s.IdSolicitudAporteRescate));\n        List<MovimientoSalida> carteras", "Collectors.groupingBy(s -> s.IdSolicitudAporteRescate, java.util.TreeMap::new, Collectors.toList()));\n        List<MovimientoSalida> carteras", 1),
    ("I27 AR se llama aunque no haya aportes", I, "if (entrada.p_SolicitudesAporte.size() > 0) {", "if (entrada.p_SolicitudesAporte.size() >= 0) {", 1),
    ("I28 el costo UF del aporte de otra familia es el del rescate sin el porcentaje", I, "aporte.CostoUF = sol.CostoUF.multiply(apor.Porcentaje.divide(CIEN, MathContext.DECIMAL128), MathContext.DECIMAL128);", "aporte.CostoUF = sol.CostoUF;", 1),
    ("I29 el monto total de inversion se formatea", I, "aporte.MontoTotalInversion = sol.MontoPagoLocal.multiply(apor.Porcentaje.divide(CIEN, MathContext.DECIMAL128));\n                                    if (DOLAR",
     "aporte.MontoTotalInversion = fmt(sol.MontoPagoLocal.multiply(apor.Porcentaje.divide(CIEN, MathContext.DECIMAL128)), sol.IdMoneda);\n                                    if (DOLAR", 1),
    ("I30 el aporte lleva el regimen del rescate", I, "aporte.IdRegimenTributario = apor.IdRegimenTributario;", "aporte.IdRegimenTributario = sol.IdRegimenTributario;", 1),
    # ---- F-I switch (recursion and FM-CER-64)
    ("W01 el hijo notifica a OWP", W, 'entradaSwitch.p_NotificaOWP = "N";', 'entradaSwitch.p_NotificaOWP = "S";', 1),
    ("W02 el hijo no recibe la simulacion", W, "entradaSwitch.p_CertificacionAporteSwitchSimulado = entradaSwitch.p_SimularOperacion;", 'entradaSwitch.p_CertificacionAporteSwitchSimulado = "N";', 1),
    ("W03 el hijo recibe el canal AGE", W, "entradaSwitch.p_IdTipoCanalIngreso = ctx.entrada.p_IdTipoCanalIngreso;", 'entradaSwitch.p_IdTipoCanalIngreso = "AGE";', 1),
    ("W04 el contador del hijo no se suma", W, "ctx.salida.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;", "", 1),
    ("W05 los mensajes del hijo no se agregan", W, "ctx.salida.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);", "", 1),
    ("W06 los resultados del hijo no se agregan", W, "ctx.salida.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);", "", 1),
    ("W07 el hijo se llama aunque no haya aportes de switch", W, "if (ctx.aportesSwitch.size() > 0) {\n            Entrada", "if (ctx.aportesSwitch.size() >= 0) {\n            Entrada", 1),
    ("W08 el hijo recibe reglas S", W, "entradaSwitch.p_UsaReglasNegocio = ctx.entrada.p_UsaReglasNegocio;", 'entradaSwitch.p_UsaReglasNegocio = "S";', 1),
    ("W09 FM-CER-64 tambien para los rechazados", W, "!ESTADO_RECHAZADO.equals(s.EstadoProceso) && APORTE_REINVERSION", "APORTE_REINVERSION", 1),
    ("W10 FM-CER-64 tambien sin rescate relacionado", W, "s.IdSolicitudAporteRescateRelacionado > 0", "s.IdSolicitudAporteRescateRelacionado >= 0", 1),
    ("W11 FM-CER-64 con moneda de sistema igual", W, "!s.IdMonedaSistema.equals(s.IdMoneda)", "s.IdMonedaSistema.equals(s.IdMoneda)", 1),
    ("W12 FM-CER-64 para cualquier tipo de movimiento", W, "APORTE_REINVERSION.equals(s.IdTipoMovimientoFondoMutuo) && s.IdSolicitudAporteRescateRelacionado", "true && s.IdSolicitudAporteRescateRelacionado", 1),
    ("W13 FM-CER-64 no escribe", W, "escritura.actualizar(sumas);", "", 1),
    # ---- gate and context
    ("K01 G6 abierta", K, 'cerrada(facturacion, "G6"', 'cerrada(true, "G6"', 1),
    ("K02 el constructor de 8 argumentos abre G6", K, "custodiaNormal, comisionesEnProceso, false);", "custodiaNormal, comisionesEnProceso, true);", 1),
    ("R01 el retiro no deja las carteras en el contexto", R, "ctx.carterasRetiradas = carterasRetiradas;", "", 1),
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
    for nombre, archivo, viejo, nuevo, ocurrencia in MUTACIONES:
        if filtro and filtro not in nombre:
            continue
        f = copia / DIR / archivo
        crudo = f.read_bytes()
        original = crudo.decode("utf-8")
        if original.count(viejo) < ocurrencia:
            print("%-84s: NOT APPLICABLE (text not found %d times in %s)" % (nombre, ocurrencia, archivo), flush=True)
            continue
        partes = original.split(viejo)
        mutado = viejo.join(partes[:ocurrencia]) + nuevo + viejo.join(partes[ocurrencia:])
        try:
            f.write_bytes(mutado.encode("utf-8"))
            r = subprocess.run([MVN, "-o", "-q", "test", "-Dtest=CerT4*Test", "-Dcer.corridas=2"], cwd=str(copia / "BcsFondoMutuoServicios"), capture_output=True, text=True)
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
        print("%-84s: %s %s" % (nombre, "RED (tests fail)" if rojo else ("DOES NOT COMPILE" if not compila else "GREEN: NO TEST FAILS (hole / equivalent mutant)"), (fallos[-1].strip() if fallos else "")), flush=True)
    print("reference-code mutations that turn at least one test red: %d of %d" % (rojas, total))


if __name__ == "__main__":
    main()
