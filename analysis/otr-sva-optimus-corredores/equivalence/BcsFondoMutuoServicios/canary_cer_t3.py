"""F3 W3 T3 mutations of the REFERENCE implementation of fronts F-E (cuenta de inversion), F-F (Tesoreria) and F-H (Spot) of CertificarSolicitudAporteRescate (plan F3 section 4: break the code in
one small way that matters, confirm that at least one test goes red, restore). The reference lives in a COPY of the module OUTSIDE the repository (it is not delivered: T3 only delivers the
tests and the oracle). Nothing under legacy/ or under the real module is ever touched:

  py -3 -I canary_cer_t3.py <copy of the module outside the repo> [text of the mutation name]

The copy is made by the caller: a directory that holds the parent pom.xml and BcsFondoMutuoServicios/ (same layout as modernized/otr-sva-optimus-corredores). Each mutation is one line (or one
swap of two adjacent lines) of the production package cl.bcs.fondomutuo.certificacion inside the COPY; after it `mvn -o -q test -Dtest='CerT3*Test' -Dcer.corridas=2` runs in the copy and the mutation
must make at least one test fail; the file is restored right after (and compared with its original bytes). The 5th element of a mutation is the occurrence (1 = the first) of the text.
"""
import pathlib
import shutil
import subprocess
import sys

BASE = pathlib.Path(__file__).resolve().parent
WS = BASE.parents[3]
MVN = shutil.which("mvn") or "mvn"
DIR = "BcsFondoMutuoServicios/src/main/java/cl/bcs/fondomutuo/certificacion/"
C = "CertificarAporteRescateCuentaInversion.java"
T = "CertificarAporteRescateTesoreria.java"
S = "CertificarAporteRescateSpot.java"
D = "CertificarAporteRescateDatosJdbc.java"

RESCATE_CASO = "            if (TIPO_SOLICITUD_RESCATE.equals(sol.TipoSolicitud)) {\n                if (SI.equals(generaCargoAbono(sol.IdTipoMovimientoFondoMutuo))) {"

MUTACIONES = [
    # ---- F-E cuenta de inversion
    ("E01 aporte: ya no se exige MueveCta", C, "if (SI.equals(sol.MueveCta) && ORIGEN_MANUAL.equals(sol.OrigenOperacion) && SI.equals(", "if (ORIGEN_MANUAL.equals(sol.OrigenOperacion) && SI.equals(", 1),
    ("E02 aporte: ya no se exige el origen manual", C, "if (SI.equals(sol.MueveCta) && ORIGEN_MANUAL.equals(sol.OrigenOperacion) && SI.equals(", "if (SI.equals(sol.MueveCta) && SI.equals(", 1),
    ("E03 aporte: no se exige GeneraCargoAbono del tipo", C, "if (SI.equals(sol.MueveCta) && ORIGEN_MANUAL.equals(sol.OrigenOperacion) && SI.equals(generaCargoAbono(sol.IdTipoMovimientoFondoMutuo))) {",
     "if (SI.equals(sol.MueveCta) && ORIGEN_MANUAL.equals(sol.OrigenOperacion)) {", 1),
    ("E04 rescate: ahora tambien exige MueveCta", C, RESCATE_CASO, "            if (TIPO_SOLICITUD_RESCATE.equals(sol.TipoSolicitud)) {\n                if (SI.equals(sol.MueveCta) && SI.equals(generaCargoAbono(sol.IdTipoMovimientoFondoMutuo))) {", 1),
    ("E05 el tipo sin fila se toma como S", D, 'public String GeneraCargoAbono = "N";', 'public String GeneraCargoAbono = "S";', 1),
    ("E06 switch: se compara con equals y no con equalsIgnoreCase (aporte)", C, 'sol.Switch.equalsIgnoreCase("S")', 'sol.Switch.equals("S")', 1),
    ("E07 fecha de la condicion: la comparacion con la fecha de sistema al reves (aporte)", C, "(fechaSistema.compareTo(sol.FechaLiquidacion) > 0 || sol.Switch", "(fechaSistema.compareTo(sol.FechaLiquidacion) < 0 || sol.Switch", 1),
    ("E08 fecha de liquidacion de la operacion: ternario invertido (aporte)", C, "(fechaSistema.compareTo(sol.FechaLiquidacion) > 0) ? ctx.fechaSistema : FechaLegacy.texto(sol.FechaLiquidacion);",
     "(fechaSistema.compareTo(sol.FechaLiquidacion) > 0) ? FechaLegacy.texto(sol.FechaLiquidacion) : ctx.fechaSistema;", 1),
    ("E09 aporte diferido: el cargo de inversion lleva el monto del fondo", C, "cardif.Monto = sol.MontoPagoLocal;", "cardif.Monto = sol.MontoPago;", 1),
    ("E10 rescate diferido: el abono de inversion lleva el monto del fondo", C, "cardif.Monto = sol.MontoPagoLocal;", "cardif.Monto = sol.MontoPago;", 2),
    ("E11 rescate: el cargo de comision tambien con comision cero", C, "sol.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1", "sol.MontoComisionLocal.compareTo(BigDecimal.ZERO) >= 0", 1),
    ("E12 rescate: RESCOM ya no agrega el cargo de comision", C, " || RESCATE_COMISION.equals(sol.IdTipoMovimientoFondoMutuo))", ")", 1),
    ("E13 rescate: la fecha de operacion es la de certificacion", C, "obj.FechaOperacion = FechaLegacy.texto(sol.FechaAceptacion);", "obj.FechaOperacion = FechaLegacy.texto(sol.FechaCertificacion);", 1),
    ("E14 rescate: el abono principal lleva MontoPago en el monto", C, "obj.Monto = sol.Monto;\n                    obj.IdMonedaOperacion = sol.IdMonedaFondo;\n                    obj.MontoOperacion = sol.MontoPago;",
     "obj.Monto = sol.MontoPago;\n                    obj.IdMonedaOperacion = sol.IdMonedaFondo;\n                    obj.MontoOperacion = sol.MontoPago;", 1),
    ("E15 rescate: la numeracion salta una linea", C, "ids.put(i, sol.IdSolicitudAporteRescate);\n                    i++;\n                    entradaCuenta.p_CargoAbonoAGenerar.add(obj);",
     "ids.put(i, sol.IdSolicitudAporteRescate);\n                    i += 2;\n                    entradaCuenta.p_CargoAbonoAGenerar.add(obj);", 1),
    ("E16 el error no se mapea a la solicitud (queda el numero de linea)", C, "mje.id = ids.get(mje.id);", "mje.id = mje.id;", 1),
    ("E17 la cuenta recibe la simulacion de la entrada", C, "entradaCuenta.p_simularOperacion = NO;", "entradaCuenta.p_simularOperacion = entrada.p_SimularOperacion;", 1),
    ("E18 la cuenta recibe ingreso manual S", C, 'entradaCuenta.p_EsIngresoManual = "N";\n            JsonObject', 'entradaCuenta.p_EsIngresoManual = "S";\n            JsonObject', 1),
    ("E19 el contador de la cuenta no se suma", C, "ctx.salida.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;", "", 1),
    ("E20 se envian tambien las solicitudes ya rechazadas", C, "if (ESTADO_RECHAZADO.equals(sol.EstadoProceso)) continue;", "if (false) continue;", 1),
    ("E21 aporte: el tipo de cambio de la operacion es 1", C, "obj.TipoCambioOperacion = sol.ParidadFechaCertificacion;", "obj.TipoCambioOperacion = BigDecimal.ONE;", 1),
    # ---- F-F Tesoreria
    ("F01 el medio CIN ya no se excluye de la rama a", T, "if (!CUENTA_INVERSION.equals(sol.IdMedioDePago) && !REINVERSION", "if (!REINVERSION", 1),
    ("F02 la reinversion va a Tesoreria", T, " && !REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo) && NO.equals(sol.IndicadorMLT)", " && NO.equals(sol.IndicadorMLT)", 1),
    ("F03 el MLT se compara con distinto de S", T, "NO.equals(sol.IndicadorMLT) && !SI.equals(sol.RelacionTraspaso)", "!SI.equals(sol.IndicadorMLT) && !SI.equals(sol.RelacionTraspaso)", 1),
    ("F04 el rescate con Spot tambien va a Tesoreria", T, "if (!ESTADO_RECHAZADO.equals(sol.EstadoProceso) && !SI.equals(sol.GeneraSpot)) {", "if (!ESTADO_RECHAZADO.equals(sol.EstadoProceso)) {", 1),
    ("F05 la retencion se resta con el monto local", T, "s.MontoPagoLocal.subtract(sol.MontoRetencion)", "s.MontoPagoLocal.subtract(sol.MontoRetencionLocal)", 1),
    ("F06 la bonificacion fiscal usa la persona del SII", T, "persona(s, ctx.personaTGR);", "persona(s, ctx.personaSII);", 1),
    ("F07 el pago al SII solo para APV-B", T, "if (APV_B.equals(sol.IdRegimenTributarioAPV) || APV_AN.equals(sol.IdRegimenTributarioAPV)) {", "if (APV_B.equals(sol.IdRegimenTributarioAPV)) {", 1),
    ("F08 la entrada del SII lleva el id positivo", T, "so.IdInterno = sol.IdSolicitudAporteRescate * -1;", "so.IdInterno = sol.IdSolicitudAporteRescate;", 1),
    ("F09 la entrada del SII lleva la retencion local", T, "so.MontoPagoLocal = sol.MontoRetencion;", "so.MontoPagoLocal = sol.MontoRetencionLocal;", 1),
    ("F10 la fecha de emision es la de sistema cuando es mayor (el `> 1` pasa a `> 0`)", T, "fechaSistema.compareTo(sol.FechaLiquidacion) > 1", "fechaSistema.compareTo(sol.FechaLiquidacion) > 0", 1),
    ("F11 la paridad de inversion es cero", T, "s.ParidadFechaInversion = sol.ParidadFechaCertificacion;\n                    s.RelacionTraspaso = NO;", "s.ParidadFechaInversion = java.math.BigDecimal.ZERO;\n                    s.RelacionTraspaso = NO;", 1),
    ("F12 el traspaso de la solicitud relacionada ya no se busca", T, "if (traspaso == null) traspaso = ctx.traspasos.get(solicitud.IdSolicitudAporteRescateRelacionado);", "", 1),
    ("F13 el rechazo usa el id sin valor absoluto", T, "Long id = Math.abs(rs.Id);", "Long id = rs.Id;", 1),
    ("F14 el resultado se compara sin distinguir mayusculas", T, "if (RECHAZO_TESORERIA.equals(rs.Resultado)) {", "if (RECHAZO_TESORERIA.equalsIgnoreCase(rs.Resultado)) {", 1),
    ("F15 el campo del rechazo ya no se compara", T, "&& rs.Campo.equals(NUMERO_REFERENCIA)) {", ") {", 1),
    ("F16 el ingreso-egreso generado no se asigna", T, "sol.IdIngresoEgreso = rs.IdIngresoEgreso;", "", 1),
    ("F17 Tesoreria recibe la simulacion de la entrada", T, "entrada.p_SimularOperacion = NO;", "entrada.p_SimularOperacion = ctx.entrada.p_SimularOperacion;", 1),
    ("F18 Tesoreria recibe ingreso automatico S", T, "entrada.p_EsIngresoAutomatico = NO;", "entrada.p_EsIngresoAutomatico = SI;", 1),
    ("F19 el autorizado de origen se deja en cero", T, "s.IdAutorizadoOrigen = sol.IdPreferenciaPagoPortafolio;", "", 1),
    ("F20 el rechazo de Tesoreria marca un codigo", T, "sol.EstadoProceso = ESTADO_RECHAZADO;\n                        break;", "sol.EstadoProceso = ESTADO_RECHAZADO;\n                        sol.Codigo = \"X\";\n                        break;", 1),
    ("F21 la rama b tambien exige que no sea Spot ya excluida: el medio no se fuerza a TRA", T, "s.NombreTipoCuentaBancaria = traspaso.NombreTipoCuentaBancaria;\n                        s.IdMedioDePago = MEDIO_TRANSFERENCIA;",
     "s.NombreTipoCuentaBancaria = traspaso.NombreTipoCuentaBancaria;", 1),
    # ---- F-H Spot
    ("H01 Spot: el margen no se calcula", S, "entrada.p_CalculaMargen = SI;", "entrada.p_CalculaMargen = NO;", 1),
    ("H02 Spot: el rescate NN no afecta Tesoreria en la forma que recibe", S, "pagRec.AfectarTesoreria = RESCATE_COMISION_NN.equals(sol.IdTipoMovimientoFondoMutuo) ? SI : NO;", "pagRec.AfectarTesoreria = NO;", 1),
    ("H03 Spot: la comision periodica no cambia la forma", S, "if (COMISION_PERIODICA.equalsIgnoreCase(sol.TipoComision)) {", "if (false) {", 1),
    ("H04 Spot: el medio vacio ya no se toma como CIN", S, 'String idMedioDePago = "".equals(sol.IdMedioDePago) || sol.IdMedioDePago == null ? CUENTA_INVERSION : sol.IdMedioDePago;',
     "String idMedioDePago = sol.IdMedioDePago == null ? CUENTA_INVERSION : sol.IdMedioDePago;", 1),
    ("H05 Spot: la forma que paga afecta Tesoreria al reves", S, "pagEnt.AfectarTesoreria = CUENTA_INVERSION.equals(idMedioDePago) ? NO : SI;", "pagEnt.AfectarTesoreria = CUENTA_INVERSION.equals(idMedioDePago) ? SI : NO;", 1),
    ("H06 Spot: la forma que paga lleva el monto del rescate", S, "pagEnt.MontoFormaDePago = sol.MontoMonedaSistema;", "pagEnt.MontoFormaDePago = sol.Monto;", 1),
    ("H07 Spot: la forma que recibe usa la fecha de aceptacion", S, "pagRec.FechaLiquidacion = sol.FechaLiquidacion;", "pagRec.FechaLiquidacion = sol.FechaAceptacion;", 1),
    ("H08 Spot: la forma que recibe usa la transferencia siempre", S, 'pagRec.IdFormaDePago = idTipoOperacionSpot + "_" + CUENTA_INVERSION + "_"', 'pagRec.IdFormaDePago = idTipoOperacionSpot + "_" + TRANSFERENCIA + "_"', 1),
    ("H09 Spot: el estado RECH se interpreta al reves", S, "if (RECHAZO_SPOT.equals(rs.Estado)) {", "if (!RECHAZO_SPOT.equals(rs.Estado)) {", 1),
    ("H10 Spot: el codigo del rechazo cambia", S, 'sol.Codigo = "ERROR_CREAR_SPOT";', 'sol.Codigo = "ERROR";', 1),
    ("H11 Spot: el mensaje con id cero ya no rechaza a la primera", S, " || rs.id.equals(0L)) {", ") {", 1),
    ("H12 Spot: el tipo ERROR se compara con equals", S, "MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(rs.tipo)", "MensajeSalida.MENSAJE_ERROR.equals(rs.tipo)", 1),
    ("H13 Spot: el contador no se suma", S, "ctx.salida.P_OUT_ERRORESCONTADOR += salidaSpot.P_OUT_ERRORESCONTADOR;", "", 1),
    ("H14 Spot: el id del mensaje sin valor absoluto", S, "sol.IdSolicitudAporteRescate.equals(Math.abs(rs.id))", "sol.IdSolicitudAporteRescate.equals(rs.id)", 1),
    ("H15 Spot: la operacion no se asigna", S, "sol.IdOperacionSpot = rs.IdOperacionSpot;", "", 1),
    ("H16 Spot: permite operar prospectos", S, "entrada.p_PermiteOperarProspectos = NO;", "entrada.p_PermiteOperarProspectos = SI;", 1),
    ("H17 Spot: la forma que recibe es un ingreso", S, "pagRec.IdTipoMovimiento = MOVIMIENTO_EGRESO;", "pagRec.IdTipoMovimiento = MOVIMIENTO_INGRESO;", 1),
    ("H18 Spot: tambien se envian los rescates rechazados", S, "if (!ESTADO_RECHAZADO.equals(sol.EstadoProceso) && SI.equals(sol.GeneraSpot)) {\n                SolicitudAporteRescate s", "if (SI.equals(sol.GeneraSpot)) {\n                SolicitudAporteRescate s", 1),
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
            r = subprocess.run([MVN, "-o", "-q", "test", "-Dtest=CerT3*Test", "-Dcer.corridas=2"], cwd=str(copia / "BcsFondoMutuoServicios"), capture_output=True, text=True)
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
