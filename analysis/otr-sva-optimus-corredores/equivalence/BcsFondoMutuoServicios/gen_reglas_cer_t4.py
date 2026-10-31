"""F3 W3 T4-B: writes src/test/java/cl/bcs/fondomutuo/certificacion/CerT4ReglasTest.java from the RECORDED goldens of cases-cer-t4/ (the expected values of the rule tests are literals READ in
the golden, never computed from the rules). One test per card (FM-CER-48..58, 63, 64) and one per defect (D-F3B-01/03/04/13/30/32/45..50, D-NOR-9); the name carries the id, the
@DisplayName names the rule. Each assertion names the case.

  py -3 -I gen_reglas_cer_t4.py [module directory]       (default: modernized/.../BcsFondoMutuoServicios)
Run it after record_cer_t4.sh and before sync_cer_t4.py; the Java file is re-generated, never edited by hand.
"""
import json
import pathlib
import sys
from decimal import Context, Decimal

BASE = pathlib.Path(__file__).resolve().parent
WS = BASE.parents[3]
MOD = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios"
OUT = MOD / "src" / "test" / "java" / "cl" / "bcs" / "fondomutuo" / "certificacion" / "CerT4ReglasTest.java"
CASOS = BASE / "cases-cer-t4"
T_SOL = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
HIJO = "CertificarSolicitudAporteRescate.hijo"


def leer(p):
    return json.loads(p.read_text(encoding="utf-8"), parse_float=Decimal)


def num(v):
    d = Decimal(v)
    if d == 0:
        return "0"
    return format(d.normalize(Context(prec=80)), "f")


def txt(o, k):
    v = o.get(k)
    if v is None:
        return "null"
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, (int, Decimal)):
        return num(v)
    return str(v)


def fecha(o, k):
    v = o.get(k)
    return "null" if v is None else str(v)[:10]


def une(*xs):
    return "|".join(xs)


def j(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


def jl(lista):
    return "List.of(" + ", ".join(j(x) for x in lista) + ")" if lista else "List.<String>of()"


def llamadas(g, nombre):
    return [c for c in g["colaboradores"] if c["colaborador"] == nombre]


def proy_fact(g):
    out = []
    for l in llamadas(g, "CrearMovimientoComprobante"):
        a = l["argumentos"][0]
        out.append(("factCabecera", une(*[txt(a, k) for k in ("p_simularOperacion", "p_UsaReglasNegocio", "p_EsIngresoAutomatico", "p_EsCargaMasiva", "p_EsPauta", "p_IdEmpresa", "p_IdUsuario", "p_IdModulo")])))
        out.append(("factMovimientos", [une(txt(m, "NumeroMvtoCmbte"), txt(m, "IdTipoComprobante"), txt(m, "IdConcepto"), txt(m, "Monto"), txt(m, "MontoMovimientoComprobante"), txt(m, "MontoLiquidacion"), txt(m, "Precio"),
                                           txt(m, "IdMoneda"), txt(m, "ActualizarCuentaInversion"), fecha(m, "FechaLiquidacion"), fecha(m, "FechaOrden"), fecha(m, "FechaEmision"), txt(m, "IdCondicionLiquidacion"),
                                           txt(m, "IdTipoOperacion"), txt(m, "ModPrefFact"), txt(m, "IdTipoCanalIngreso"), txt(m, "Cantidad")) for m in a["p_Movimientos"]]))
        out.append(("factCobros", [une(txt(m, "NumeroMvtoCmbte"), txt(m, "NumeroMvtoCmbteCobro"), txt(m, "IdCobro"), txt(m, "IdMonedaCobro"), txt(m, "MontoCobro"), txt(m, "MontoMovimientoComprobanteCobro"),
                                       txt(m, "MontoLiquidacionCobro"), txt(m, "Porcentaje")) for m in a["p_MovimientosCobros"]]))
        out.append(("factFormas", [une(txt(m, "NumeroMvtoCmbte"), txt(m, "IdMedioDePago"), txt(m, "IdMoneda"), txt(m, "MontoLiquidacionFormaDePago"), txt(m, "AfectarCuentaInversion"), txt(m, "AfectarTesoreria"),
                                       fecha(m, "FechaDocumentoPago"), txt(m, "FechaVctoTarjetaBancaria")) for m in a["p_MovimientosFormaDePago"]]))
    return out


def proy_comp(g):
    out = []
    for l in llamadas(g, "CrearComprobante"):
        a = l["argumentos"][0]
        out.append(("compCabecera", une(*[txt(a, k) for k in ("p_simularOperacion", "p_UsaReglasNegocio", "p_EsIngresoAutomatico", "p_IdEmpresa", "p_IdUsuario", "p_IdModulo")])))
        out.append(("compMovimientos", [une(txt(m, "IdMovimientoComprobante"), txt(m, "FechaComprobante")) for m in a["P_MOVIMIENTOFACTURAR"]]))
    return out


def proy_ar(g):
    out = []
    for l in llamadas(g, "CrearSolicitudAporteReinversion"):
        a = l["argumentos"][0]
        out.append(("arCabecera", une(*[txt(a, k) for k in ("p_SimularOperacion", "p_UsaReglasNegocio", "p_IdTipoCanalIngreso", "p_IdEmpresa", "p_IdUsuario", "p_IdModulo")])))
        out.append(("arAportes", [une(txt(m, "IdInterno"), txt(m, "IdSolicitudAporteRescateRelacionado"), txt(m, "IdSerieFondo"), txt(m, "Monto"), txt(m, "MontoOrigen"), txt(m, "CostoUF"), fecha(m, "FechaOrigenInversion"),
                                    fecha(m, "FechaEmision"), txt(m, "NumeroCuotas"), txt(m, "MontoTotalInversion"), txt(m, "Switch"), txt(m, "Canal"), txt(m, "IdRegimenTributario"),
                                    txt(m, "FechaCertificaSpotCompra"), txt(m, "FechaLiquidaSpotCompra"), fecha(m, "FechaCertificacionRescateOrigen"), fecha(m, "FechaLiquidacionRescateOrigen"),
                                    fecha(m, "FechaValorCuotaRescateOrigen")) for m in a["p_SolicitudesAporte"]]))
        out.append(("arCampos", [une(txt(m, "IdInterno"), txt(m, "IdAgrupador"), txt(m, "IngresoAutomatico"), txt(m, "SolicitudBatch"), txt(m, "OrigenOperacion"), txt(m, "Glosa"), txt(m, "FolioContrato"),
                                     txt(m, "IdMedioDePago"), txt(m, "IndicadorMLT"), txt(m, "Visado"), txt(m, "IdAutorizadoOrigen"), txt(m, "IdCliente"), txt(m, "IdPortafolio"), txt(m, "IdSerieFondoOrigen"),
                                     txt(m, "IdEmisorOrigen"), txt(m, "IdMoneda"), txt(m, "GeneraSpot"), txt(m, "Porcentaje")) for m in a["p_SolicitudesAporte"]]))
    return out


def proy_hijo(g):
    out = []
    for l in llamadas(g, HIJO):
        a = l["argumentos"][0]
        out.append(("hijoEntrada", une(*[txt(a, k) for k in ("p_IdEmpresa", "p_IdUsuario", "p_IdModulo", "p_SimularOperacion", "p_UsaReglasNegocio", "p_FechaCertificacion", "p_IdTipoCanalIngreso", "p_NotificaOWP",
                                                               "p_CertificacionAporteSwitchSimulado")])))
        out.append(("hijoSolicitudes", [une(txt(s, "IdSolicitudAporteRescate"), txt(s, "IdTipoCanalIngreso")) for s in a["p_Solicitudes"]]))
        r = l["respuesta"]
        out.append(("hijoRespuesta", "contador=%s;%s" % (txt(r, "P_OUT_ERRORESCONTADOR"), ";".join("%s=%s" % (txt(x, "IdSolicitudAporteRescate"), txt(x, "Estado") + (":" + txt(x, "Codigo") if txt(x, "Codigo") != "UI_EXITO" else "")) for x in r["P_OUT_RESULTADO"]))))
    return out


def estados(g, caso):
    salida = json.loads(g["salidaCruda"], parse_float=Decimal)
    k = 0
    for l in llamadas(g, HIJO):
        k = len(l["respuesta"]["P_OUT_RESULTADO"])
    filas = salida["P_OUT_RESULTADO"][k:]
    out = []
    for f in filas:
        e = f["Estado"]
        out.append("%s=%s" % (f["IdSolicitudAporteRescate"], "OK" if e == "APD" else e + ":" + f["Codigo"]))
    return ";".join(out)


def salida(g, caso):
    s = json.loads(g["salidaCruda"], parse_float=Decimal)
    sim = caso["entrada"].get("p_SimularOperacion") == "S"
    ms = "|".join("%s:%s:%s:%s" % (txt(m, "id"), txt(m, "tipo"), txt(m, "codigo"), txt(m, "descripcion")) for m in s["P_OUT_MENSAJES"])
    return (None if sim else num(s["P_OUT_ERRORESCONTADOR"]), ms)


def monto(g):
    return ";".join("%s=%s" % (r["IdSolicitudAporteRescate"], num(r["MontoMonedaSistema"])) for r in g["tablasTrasLaLlamada"][T_SOL])


def orden(g):
    nombres = {"SaldoFlujoCaja", "CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo", "CrearCargoAbono", "ProcesarIngresoEgresoTesoreria", "ProcesarIngresoOperacionSpot", "CrearMovimientoComprobante",
               "CrearComprobante", "CrearSolicitudAporteReinversion", HIJO}
    return [c["colaborador"] for c in g["colaboradores"] if c["colaborador"] in nombres]


def bloque(id_, slug, g, caso, tipo):
    """the Java statements for ONE case"""
    var = "c%s" % id_
    exc = g.get("excepcion")
    L = ["        // FMM-CER-%s" % id_]
    if exc:
        L.append("        CasoCerT4.Corrida %s = lanza(%s);" % (var, j(slug)))
        if exc.split(":")[0] == "Exception":
            L.append('        assertEquals(%s, %s.excepcion.getMessage(), %s);' % (j(exc.split(": ", 1)[1]), var, j(slug + " :: message of the plain Exception")))
        else:
            L.append('        assertEquals(%s, claseDe(%s), %s);' % (j(exc.split(":")[0]), var, j(slug + " :: class of the exception")))
    else:
        L.append("        CasoCerT4.Corrida %s = corre(%s);" % (var, j(slug)))
        est = estados(g, caso)
        L.append('        assertEquals(%s, estados(%s), %s);' % (j(est), var, j(slug + " :: state of every solicitud (OK = pending, certified by the end)")))
        cont, ms = salida(g, caso)
        L.append('        assertEquals(%s, mensajes(%s), %s);' % (j(ms), var, j(slug + " :: messages of the output")))
        if cont is not None:
            L.append('        assertEquals(%s, contador(%s), %s);' % (j(cont), var, j(slug + " :: counter of the output")))
    L.append('        assertEquals(%s, orden(%s), %s);' % (jl(orden(g)), var, j(slug + " :: order of the calls to the collaborators")))
    for nombre, valor in proy_fact(g) + proy_comp(g) + proy_ar(g) + proy_hijo(g):
        if isinstance(valor, list):
            L.append('        assertEquals(%s, %s(%s), %s);' % (jl(valor), nombre, var, j("%s :: %s" % (slug, nombre))))
        else:
            L.append('        assertEquals(%s, %s(%s), %s);' % (j(valor), nombre, var, j("%s :: %s" % (slug, nombre))))
    if "monto-sistema" in slug and not exc:
        L.append('        assertEquals(%s, montoSistema(%s), %s);' % (j(monto(g)), var, j(slug + " :: MontoMonedaSistema of every row after FM-CER-64")))
    return L


def main():
    casos = []
    for d in sorted(CASOS.glob("FMM-CER-*")):
        c = leer(d / "case.json")
        g = leer(d / "expected.json")
        casos.append((d.name[8:11], d.name[12:], c, g))
    tarjetas = [("48", "facturacionSeleccion", "FM-CER-48 a rescate (RESNORM, RESTRAPOR, RESCOM, not Spot, not rejected, not traspaso, commission > 0) becomes ONE movement of Facturacion; the movement carries the concept, the type of voucher, the net amount and the dates the golden shows"),
                ("49", "ivaNetoYCobro", "FM-CER-49 the IVA read from COM.Cobro makes the net amount commission / (1 + IVA) with 34 digits, the IVA cobro and, for RESNORM, the form of payment CIN by the whole commission"),
                ("50", "invocacionYResultado", "FM-CER-50 Facturacion is called once with simular N, reglas S, ingreso automatico N; its counter is added; every ERROR rejects the first rescate with that id (Math.abs)"),
                ("51", "comprobanteAutomatico", "FM-CER-51 with RESCATE_COMISION_COMPROBANTE_AUTOMATICO S the voucher is asked for the movements of RESNORM / RESREINV rescates with commission, dated the system date; the counter that is added is the one of the MOVEMENT, twice (D-F3B-04)"),
                ("52", "reinversionSeleccion", "FM-CER-52 only the not rejected RESREINV rescates with destinations reach AR, all in ONE call numbered 1..n in the order of the map of destinations"),
                ("53", "misFamiliaMisMoneda", "FM-CER-53 same family and currency: one aporte per retired cartera with ratio = quantity / quotas, amount formatted, cost UF and origin date of the cartera"),
                ("54", "otraFamiliaOMoneda", "FM-CER-54 another family or currency: ONE aporte per destination with the cost UF of the rescate"),
                ("55", "ajusteDeRedondeo", "FM-CER-55 the difference of rounding goes to the FIRST aporte of the rescate (not the last); an empty list raises IndexOutOfBoundsException"),
                ("56", "fechasDelAporte", "FM-CER-56 the date of emission and the dates of the Spot of the aporte depend on the currencies and on the international funds"),
                ("57", "llamadaAAr", "FM-CER-57 AR is called with simular N, canal AGE and the reglas of the request; the messages and results map the correlative to the id of the rescate; the counter is added; every ERROR rejects the rescate"),
                ("58", "aportesDeSwitch", "FM-CER-58 the results of AR with Switch exactly S become the aportes of the switch (id and channel), also those of a rescate AR rejected; a null Switch raises NullPointerException"),
                ("63", "recursionDelSwitch", "FM-CER-63 the child certification receives the header of the request with p_NotificaOWP N and the simulation flag; its counter, messages and results are added (the results come first)"),
                ("64", "montoMonedaSistema", "FM-CER-64 the rescate of origin of an international reinversion aporte gets, AFTER the UPDATE to CRT, the SUM of the MontoMonedaSistema of all the aportes related to it")]
    defectos = [("D-F3B-01", "defecto01Simulacion", "D-F3B-01 simulation S: every collaborator receives simular N; the final counter 1 is forced by front J; FM-CER-64 and the child still run", "D-F3B-01"),
                ("D-F3B-03", "defecto03RechazoConEfectosPrevios", "D-F3B-03 a rejection of Facturacion / AR leaves the custody and the cuenta already written (the calls were made)", "D-F3B-03"),
                ("D-F3B-04", "defecto04ContadorDelComprobante", "D-F3B-04 the counter of the voucher is never added: the one of the movement is added twice", "D-F3B-04"),
                ("D-F3B-13", "defecto13UltimoAporteEsElPrimero", "D-F3B-13 the 'last aporte' that receives the difference is the first one", "D-F3B-13"),
                ("D-F3B-30", "defecto30SwitchNuloOAporteRechazado", "D-F3B-30 a null Switch raises NullPointerException; the aporte of a rescate AR rejected is certified anyway", "D-F3B-30"),
                ("D-F3B-32", "defecto32OrdenDelMapa", "D-F3B-32 the correlative of the aportes follows the order of a HashMap (3008 before 3007)", "D-F3B-32"),
                ("D-F3B-45", "defecto45AjusteCruzado", "D-F3B-45 the rounding difference of the SECOND destination is added to the first aporte of the FIRST destination", "D-F3B-45"),
                ("D-F3B-46", "defecto46PorcentajesQueNoSuman100", "D-F3B-46 percentages that do not add 100 leave the whole difference (positive or negative) in the first aporte", "D-F3B-46"),
                ("D-F3B-47", "defecto47IvaLeidoSiempre", "D-F3B-47 the IVA is read for every request with rescates: no row raises IndexOutOfBoundsException even without commission", "D-F3B-47"),
                ("D-F3B-48", "defecto48RamasInalcanzables", "D-F3B-48 dead branches: the commission equal to BigDecimal(0) never reaches the net formula, GeneraSpot S never reaches FM-CER-64", "D-F3B-48"),
                ("D-F3B-49", "defecto49SumaDespuesDelUpdate", "D-F3B-49 FM-CER-64 sums the rows AFTER the UPDATE: the sibling aportes keep their table value, the ones of the request their new one", "D-F3B-49"),
                ("D-F3B-51", "defecto51Proposito", "D-F3B-51 the concept of Facturacion compares the purpose with the literal APV while the SQL and the retention use PROPOSITOAPV: a PROPOSITOAPV portfolio gets the NON APV concept", "D-F3B-51"),
                ("D-F3B-52", "defecto52BanderasDeLaSolicitud", "D-F3B-52 the international-dollar flags of the origin and destination funds are read from the IdSerieFondoDestino of the rescate row, not from the table of destinations", "D-F3B-52"),
                ("D-F3B-50", "defecto50RecursionSinGuarda", "D-F3B-50 the recursion has no guard: the child reaches a rescate and calls AR again; it ends only because AR answers empty", "D-F3B-50"),
                ("D-NOR-9", "defectoNor9NullPointer", "D-NOR-9 a message or a result without id / switch raises NullPointerException", "D-NOR-9")]
    metodos = []
    for num_, nombre, doc in tarjetas:
        slugs = [(i, s, c, g) for i, s, c, g in casos if c["ref"][0] == "FM-CER-" + num_]
        metodos.append(("fmCer%s_%s" % (num_, nombre), doc, slugs))
    for ref, nombre, doc, tag in defectos:
        slugs = [(i, s, c, g) for i, s, c, g in casos if tag in c["ref"]]
        metodos.append((nombre, doc, slugs))
    L = []
    L.append(PLANTILLA_CABECERA)
    vistos = {}
    for nombre, doc, lista in metodos:
        if not lista:
            continue
        L.append("")
        L.append("    @Test")
        L.append("    @DisplayName(%s)" % j(doc))
        L.append("    void %s() throws Exception {" % nombre)
        for i, s, c, g in lista:
            L += bloque(i, s, g, c, "")
        L.append("    }")
    L.append("}")
    OUT.write_text("\n".join(L) + "\n", encoding="utf-8", newline="\n")
    print("CerT4ReglasTest.java written: %d tests, %d case blocks" % (sum(1 for m in metodos if m[2]), sum(len(m[2]) for m in metodos)))


PLANTILLA_CABECERA = r'''package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;

/**
 * GENERATED by gen_reglas_cer_t4.py from the RECORDED goldens (do not edit by hand): rule by rule, with LITERAL values, every card of FM-CER-48..51 (front F-G, Facturacion) and FM-CER-52..58,
 * 63, 64 (front F-I, reinversion and switch) of RULES_FONDOMUTUO_F3_B.md 8.1 and the defects they pin (D-F3B-01/03/04/13/30/32/45..50, D-NOR-9), against the units of F-G and F-I (see
 * {@code API_ASSUMED_T4.md}). The expected values are the ones the legacy produced when the goldens were recorded, written as literals read in the golden (each assertion names the case). Each
 * test runs the case through {@link CasoCerT4} (F-A and F-B of T1 REAL, then the steps of F-C..F-I) and asserts on what the NEW code did: the movements, cobros and forms it sent to Facturacion, the
 * vouchers it asked for, the aportes it sent to AR, the entry it gave the child certification, the state of each solicitud, the counter and the messages and the MontoMonedaSistema it wrote.
 *
 * <p>Projections (the same text for every case): {@code factMovimientos} = {@code NumeroMvtoCmbte|IdTipoComprobante|IdConcepto|Monto|MontoMovimientoComprobante|MontoLiquidacion|Precio|IdMoneda|
 * ActualizarCuentaInversion|FechaLiquidacion|FechaOrden|FechaEmision|IdCondicionLiquidacion|IdTipoOperacion|ModPrefFact|IdTipoCanalIngreso|Cantidad}; {@code arAportes} = {@code IdInterno|Relacionado|
 * IdSerieFondo|Monto|MontoOrigen|CostoUF|FechaOrigenInversion|FechaEmision|NumeroCuotas|MontoTotalInversion|Switch|Canal|IdRegimenTributario|FechaCertificaSpotCompra|FechaLiquidaSpotCompra|
 * FechaCertificacionRescateOrigen|FechaLiquidacionRescateOrigen|FechaValorCuotaRescateOrigen}; numbers by value, dates as {@code yyyy-MM-dd}.
 *
 * <p>RED BEFORE CODE: until the units exist every test fails with "T4 not implemented: the production class ... does not exist yet".
 */
class CerT4ReglasTest {

    private static CasoCerT4.Corrida corre(String slug) throws Exception {
        CasoCerT4.Corrida c = CasoCerT4.ejecutar(CasoCerT4.idPorSlug(slug));
        assertNotNull(c.contexto, slug + " must reach the seam; it ended with " + c.excepcion);
        assertTrue(c.terminoF_C_F_I(), slug + " must finish F-C..F-I; it raised " + c.excepcion);
        return c;
    }

    private static CasoCerT4.Corrida lanza(String slug) throws Exception {
        CasoCerT4.Corrida c = CasoCerT4.ejecutar(CasoCerT4.idPorSlug(slug));
        assertNotNull(c.contexto, slug + " must reach the seam; it ended with " + c.excepcion);
        assertFalse(c.terminoF_C_F_I(), slug + " must raise inside F-C..F-I");
        return c;
    }

    private static String claseDe(CasoCerT4.Corrida c) {
        return c.excepcion.getClass().getSimpleName();
    }

    private static List<JsonObject> llamadas(CasoCerT4.Corrida c, String colaborador) {
        return CerCosturaT4.llamadas(c.volcado.getAsJsonArray("colaboradores"), colaborador);
    }

    private static JsonObject arg(JsonObject llamada) {
        return llamada.getAsJsonArray("argumentos").get(0).getAsJsonObject();
    }

    private static String num(JsonElement e) {
        return new BigDecimal(e.getAsString()).stripTrailingZeros().toPlainString();
    }

    private static String txt(JsonObject o, String campo) {
        JsonElement e = o.get(campo);
        if (e == null || e.isJsonNull()) return "null";
        if (e.getAsJsonPrimitive().isBoolean()) return e.getAsString();
        if (e.getAsJsonPrimitive().isNumber()) return new BigDecimal(e.getAsString()).signum() == 0 ? "0" : num(e);
        return e.getAsString();
    }

    private static String fecha(JsonObject o, String campo) {
        JsonElement e = o.get(campo);
        return e == null || e.isJsonNull() ? "null" : e.getAsString().substring(0, 10);
    }

    private static String une(String... partes) {
        return String.join("|", partes);
    }

    private static final List<String> NOMBRES_ORDEN = List.of("SaldoFlujoCaja", "CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo", "CrearCargoAbono", "ProcesarIngresoEgresoTesoreria",
            "ProcesarIngresoOperacionSpot", "CrearMovimientoComprobante", "CrearComprobante", "CrearSolicitudAporteReinversion", CasoCerT4.HIJO);

    private static List<String> orden(CasoCerT4.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : c.volcado.getAsJsonArray("colaboradores")) {
            String n = e.getAsJsonObject().get("colaborador").getAsString();
            if (NOMBRES_ORDEN.contains(n)) out.add(n);
        }
        return out;
    }

    private static String factCabecera(CasoCerT4.Corrida c) {
        JsonObject a = arg(llamadas(c, "CrearMovimientoComprobante").get(0));
        return une(txt(a, "p_simularOperacion"), txt(a, "p_UsaReglasNegocio"), txt(a, "p_EsIngresoAutomatico"), txt(a, "p_EsCargaMasiva"), txt(a, "p_EsPauta"), txt(a, "p_IdEmpresa"), txt(a, "p_IdUsuario"), txt(a, "p_IdModulo"));
    }

    private static List<String> factMovimientos(CasoCerT4.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : arg(llamadas(c, "CrearMovimientoComprobante").get(0)).getAsJsonArray("p_Movimientos")) {
            JsonObject m = e.getAsJsonObject();
            out.add(une(txt(m, "NumeroMvtoCmbte"), txt(m, "IdTipoComprobante"), txt(m, "IdConcepto"), txt(m, "Monto"), txt(m, "MontoMovimientoComprobante"), txt(m, "MontoLiquidacion"), txt(m, "Precio"), txt(m, "IdMoneda"),
                    txt(m, "ActualizarCuentaInversion"), fecha(m, "FechaLiquidacion"), fecha(m, "FechaOrden"), fecha(m, "FechaEmision"), txt(m, "IdCondicionLiquidacion"), txt(m, "IdTipoOperacion"), txt(m, "ModPrefFact"),
                    txt(m, "IdTipoCanalIngreso"), txt(m, "Cantidad")));
        }
        return out;
    }

    private static List<String> factCobros(CasoCerT4.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : arg(llamadas(c, "CrearMovimientoComprobante").get(0)).getAsJsonArray("p_MovimientosCobros")) {
            JsonObject m = e.getAsJsonObject();
            out.add(une(txt(m, "NumeroMvtoCmbte"), txt(m, "NumeroMvtoCmbteCobro"), txt(m, "IdCobro"), txt(m, "IdMonedaCobro"), txt(m, "MontoCobro"), txt(m, "MontoMovimientoComprobanteCobro"), txt(m, "MontoLiquidacionCobro"),
                    txt(m, "Porcentaje")));
        }
        return out;
    }

    private static List<String> factFormas(CasoCerT4.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : arg(llamadas(c, "CrearMovimientoComprobante").get(0)).getAsJsonArray("p_MovimientosFormaDePago")) {
            JsonObject m = e.getAsJsonObject();
            out.add(une(txt(m, "NumeroMvtoCmbte"), txt(m, "IdMedioDePago"), txt(m, "IdMoneda"), txt(m, "MontoLiquidacionFormaDePago"), txt(m, "AfectarCuentaInversion"), txt(m, "AfectarTesoreria"), fecha(m, "FechaDocumentoPago"),
                    txt(m, "FechaVctoTarjetaBancaria")));
        }
        return out;
    }

    private static String compCabecera(CasoCerT4.Corrida c) {
        JsonObject a = arg(llamadas(c, "CrearComprobante").get(0));
        return une(txt(a, "p_simularOperacion"), txt(a, "p_UsaReglasNegocio"), txt(a, "p_EsIngresoAutomatico"), txt(a, "p_IdEmpresa"), txt(a, "p_IdUsuario"), txt(a, "p_IdModulo"));
    }

    private static List<String> compMovimientos(CasoCerT4.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : arg(llamadas(c, "CrearComprobante").get(0)).getAsJsonArray("P_MOVIMIENTOFACTURAR")) {
            JsonObject m = e.getAsJsonObject();
            out.add(une(txt(m, "IdMovimientoComprobante"), txt(m, "FechaComprobante")));
        }
        return out;
    }

    private static String arCabecera(CasoCerT4.Corrida c) {
        JsonObject a = arg(llamadas(c, "CrearSolicitudAporteReinversion").get(0));
        return une(txt(a, "p_SimularOperacion"), txt(a, "p_UsaReglasNegocio"), txt(a, "p_IdTipoCanalIngreso"), txt(a, "p_IdEmpresa"), txt(a, "p_IdUsuario"), txt(a, "p_IdModulo"));
    }

    private static List<String> arAportes(CasoCerT4.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : arg(llamadas(c, "CrearSolicitudAporteReinversion").get(0)).getAsJsonArray("p_SolicitudesAporte")) {
            JsonObject m = e.getAsJsonObject();
            out.add(une(txt(m, "IdInterno"), txt(m, "IdSolicitudAporteRescateRelacionado"), txt(m, "IdSerieFondo"), txt(m, "Monto"), txt(m, "MontoOrigen"), txt(m, "CostoUF"), fecha(m, "FechaOrigenInversion"),
                    fecha(m, "FechaEmision"), txt(m, "NumeroCuotas"), txt(m, "MontoTotalInversion"), txt(m, "Switch"), txt(m, "Canal"), txt(m, "IdRegimenTributario"), txt(m, "FechaCertificaSpotCompra"),
                    txt(m, "FechaLiquidaSpotCompra"), fecha(m, "FechaCertificacionRescateOrigen"), fecha(m, "FechaLiquidacionRescateOrigen"), fecha(m, "FechaValorCuotaRescateOrigen")));
        }
        return out;
    }

    private static List<String> arCampos(CasoCerT4.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : arg(llamadas(c, "CrearSolicitudAporteReinversion").get(0)).getAsJsonArray("p_SolicitudesAporte")) {
            JsonObject m = e.getAsJsonObject();
            out.add(une(txt(m, "IdInterno"), txt(m, "IdAgrupador"), txt(m, "IngresoAutomatico"), txt(m, "SolicitudBatch"), txt(m, "OrigenOperacion"), txt(m, "Glosa"), txt(m, "FolioContrato"), txt(m, "IdMedioDePago"),
                    txt(m, "IndicadorMLT"), txt(m, "Visado"), txt(m, "IdAutorizadoOrigen"), txt(m, "IdCliente"), txt(m, "IdPortafolio"), txt(m, "IdSerieFondoOrigen"), txt(m, "IdEmisorOrigen"), txt(m, "IdMoneda"),
                    txt(m, "GeneraSpot"), txt(m, "Porcentaje")));
        }
        return out;
    }

    private static String hijoEntrada(CasoCerT4.Corrida c) {
        JsonObject a = arg(llamadas(c, CasoCerT4.HIJO).get(0));
        return une(txt(a, "p_IdEmpresa"), txt(a, "p_IdUsuario"), txt(a, "p_IdModulo"), txt(a, "p_SimularOperacion"), txt(a, "p_UsaReglasNegocio"), txt(a, "p_FechaCertificacion"), txt(a, "p_IdTipoCanalIngreso"),
                txt(a, "p_NotificaOWP"), txt(a, "p_CertificacionAporteSwitchSimulado"));
    }

    private static List<String> hijoSolicitudes(CasoCerT4.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : arg(llamadas(c, CasoCerT4.HIJO).get(0)).getAsJsonArray("p_Solicitudes")) out.add(une(txt(e.getAsJsonObject(), "IdSolicitudAporteRescate"), txt(e.getAsJsonObject(), "IdTipoCanalIngreso")));
        return out;
    }

    /** what the child answered (the golden's answer, replayed) as the parent added it: counter and, per row, id=state[:code] */
    private static String hijoRespuesta(CasoCerT4.Corrida c) {
        JsonObject r = llamadas(c, CasoCerT4.HIJO).get(0).getAsJsonObject("respuesta");
        List<String> filas = new ArrayList<>();
        for (JsonElement e : r.getAsJsonArray("P_OUT_RESULTADO")) {
            JsonObject x = e.getAsJsonObject();
            filas.add(txt(x, "IdSolicitudAporteRescate") + "=" + txt(x, "Estado") + (txt(x, "Codigo").equals("UI_EXITO") ? "" : ":" + txt(x, "Codigo")));
        }
        return "contador=" + txt(r, "P_OUT_ERRORESCONTADOR") + ";" + String.join(";", filas);
    }

    /** {@code id=OK} when the solicitud was still pending after F-C..F-I (the legacy certifies it), else {@code id=RZD:code} / {@code id=ADV:code}. */
    private static String estados(CasoCerT4.Corrida c) {
        List<String> out = new ArrayList<>();
        for (SolicitudProceso s : c.contexto.solicitudes) {
            boolean marcada = "RZD".equals(s.EstadoProceso) || "ADV".equals(s.EstadoProceso);
            out.add(s.IdSolicitudAporteRescate + "=" + (marcada ? s.EstadoProceso + ":" + s.Codigo : "OK"));
        }
        return String.join(";", out);
    }

    private static String contador(CasoCerT4.Corrida c) {
        return String.valueOf(c.contexto.salida.P_OUT_ERRORESCONTADOR);
    }

    private static String mensajes(CasoCerT4.Corrida c) {
        List<String> ms = new ArrayList<>();
        c.contexto.salida.P_OUT_MENSAJES.forEach(m -> ms.add(m.id + ":" + m.tipo + ":" + m.codigo + ":" + m.descripcion));
        return String.join("|", ms);
    }

    /** {@code id=MontoMonedaSistema} of every row of the table after F-C..F-I (the J-lite column of the harness and FM-CER-64) */
    private static String montoSistema(CasoCerT4.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : c.volcado.getAsJsonObject("tablasTrasLaLlamada").getAsJsonArray(CerCosturaT4.T_SOL)) {
            JsonObject r = e.getAsJsonObject();
            out.add(r.get("IdSolicitudAporteRescate").getAsString() + "=" + (new BigDecimal(r.get("MontoMonedaSistema").getAsString()).signum() == 0 ? "0" : num(r.get("MontoMonedaSistema"))));
        }
        return String.join(";", out);
    }
'''

if __name__ == "__main__":
    main()
