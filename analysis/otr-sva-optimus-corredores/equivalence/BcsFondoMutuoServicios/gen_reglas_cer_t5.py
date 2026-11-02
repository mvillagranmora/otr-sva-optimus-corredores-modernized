"""F3 W3 T5: writes src/test/java/cl/bcs/fondomutuo/certificacion/CerT5ReglasTest.java from the RECORDED goldens of cases-cer-t5/ (the expected values of the rule tests are literals READ in
the golden, never computed from the rules). One test per card (FM-CER-14 early exit, 61..71), one per defect (D-F3B-01/02/03/16/30/31/47/49/50, D-NOR-9) and one per path of the review M3
(`T5-m3:a|b|c`); the name carries the id, the @DisplayName names the rule. Each assertion names the case.

  python -X utf8 -I gen_reglas_cer_t5.py [module directory]       (default: modernized/.../BcsFondoMutuoServicios)
Run it after record_cer_t5.sh and before sync_cer_t5.py; the Java file is re-generated, never edited by hand. Every test runs the case through CasoCerT5.ejecutar, so with
`-Dcer.autocomprobacion=true` it reads the golden instead of the production facade (the proof that the assertions describe the legacy).
"""
import json
import pathlib
import sys
from decimal import Context, Decimal

BASE = pathlib.Path(__file__).resolve().parent
WS = BASE.parents[3]
MOD = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios"
OUT = MOD / "src" / "test" / "java" / "cl" / "bcs" / "fondomutuo" / "certificacion" / "CerT5ReglasTest.java"
CASOS = BASE / "cases-cer-t5"
T_SOL = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
T_LOG = "BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate"
HIJO = "CertificarSolicitudAporteRescate.hijo"
LECTURAS = {"EntregarPreciosTasas", "CalcularParidad", "ConsultarEmpresa", "ConsultarFlujoCustodia"}


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


def une(*xs):
    return "|".join(xs)


def j(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


def jl(lista):
    return "List.of(" + ", ".join(j(x) for x in lista) + ")" if lista else "List.<String>of()"


# ---------------------------------------------------------------------------------------------------------------------------------------------------
# projections (the SAME text as the Java helpers of the header)
# ---------------------------------------------------------------------------------------------------------------------------------------------------
def estados(g):
    s = g["salida"]
    out = []
    for f in s["P_OUT_RESULTADO"]:
        e = f["Estado"]
        out.append("%s=%s" % (txt(f, "IdSolicitudAporteRescate"), "OK" if e == "APD" else e + ":" + txt(f, "Codigo")))
    return ";".join(out)


def mensajes(g):
    return "|".join("%s:%s:%s:%s" % (txt(m, "id"), txt(m, "tipo"), txt(m, "codigo"), txt(m, "descripcion")) for m in g["salida"]["P_OUT_MENSAJES"])


def orden(g):
    return ["hijo" if c["colaborador"] == HIJO else c["colaborador"] for c in g["colaboradores"] if c["colaborador"] not in LECTURAS]


def filas(g):
    out = []
    for r in g["tablasTrasLaLlamada"][T_SOL]:
        out.append(une(txt(r, "IdSolicitudAporteRescate") + ":" + txt(r, "Estado"), txt(r, "Spread"), txt(r, "CostoUF"), txt(r, "ValorUFFechaInversion"), txt(r, "MontoMonedaSistema"), txt(r, "IdIngresoEgreso"),
                       txt(r, "IdOperacionSpot"), txt(r, "IdCartera"), txt(r, "MontoComision"), txt(r, "ParidadFechaCertificacion"), txt(r, "FechaCertificacionRegistro")))
    return out


def tablas_final(g):
    return ";".join("%s:%s" % (txt(r, "IdSolicitudAporteRescate"), txt(r, "Estado")) for r in g["tablas"][T_SOL])


def log(g):
    return [une(txt(r, "IdLogCertificacionAporteRescate"), txt(r, "IdSolicitudAporteRescate"), txt(r, "Codigo"), txt(r, "IdEstado"), txt(r, "Monto"), txt(r, "TipoCambio"), txt(r, "ValorCuota"),
                txt(r, "NumeroCuotas"), txt(r, "Simulacion")) for r in g["tablasTrasLaLlamada"][T_LOG]]


def notificaciones(g):
    out = []
    for c in g["colaboradores"]:
        if c["colaborador"] == "NotificarEstadoOrdenFondoMutuo":
            a = c["argumentos"][0]
            out.append(une(txt(a, "p_IdEmpresa"), txt(a, "p_IdUsuario"), txt(a, "p_IdModulo"), ",".join(num(x) for x in a["p_Solicitudes"])))
    return out


def owp(g):
    out = []
    for c in g["colaboradores"]:
        if c["colaborador"] == "NotificarEstadoOrdenesOWP":
            out.append(";".join(une(txt(e, "Solsec"), txt(e, "Estado"), txt(e, "Fecha"), txt(e, "SolsecAsoc"), txt(e, "Nemo"), txt(e, "Divisa"), txt(e, "NTitulos"), txt(e, "Precio"), txt(e, "Comision"),
                                    txt(e, "TipoCambio")) for e in c["argumentos"][0]))
    return out


def agregar_log(g):
    out = []
    for c in g["colaboradores"]:
        if c["colaborador"] == "AgregarLog":
            for e in c["argumentos"][0]["p_Log"]:
                out.append(une(txt(e, "IdTipoLog"), txt(e, "IdModulo"), txt(e, "IdUsuario"), txt(e, "IdEmpresa"), txt(e, "FechaSistemaLog"), txt(e, "IdTipoRegistro"), txt(e, "EstadoRegistro")))
    return out


def hijos(g):
    out = []
    for c in g["colaboradores"]:
        if c["colaborador"] == HIJO:
            a = c["argumentos"][0]
            r = c["respuesta"]
            if isinstance(r, str):
                resp = r
            else:
                resp = "contador=%s;%s" % (txt(r, "P_OUT_ERRORESCONTADOR"), ";".join("%s=%s" % (txt(x, "IdSolicitudAporteRescate"), "OK" if x["Estado"] == "APD" else x["Estado"] + ":" + txt(x, "Codigo")) for x in r["P_OUT_RESULTADO"]))
            interno = ",".join("hijo" if x["colaborador"] == HIJO else x["colaborador"] for x in c.get("internas", []) if x["colaborador"] not in LECTURAS)
            out.append(une(*[txt(a, k) for k in ("p_IdEmpresa", "p_IdUsuario", "p_IdModulo", "p_SimularOperacion", "p_UsaReglasNegocio", "p_FechaCertificacion", "p_IdTipoCanalIngreso", "p_NotificaOWP",
                                                   "p_CertificacionAporteSwitchSimulado")], ",".join("%s/%s" % (txt(s, "IdSolicitudAporteRescate"), txt(s, "IdTipoCanalIngreso")) for s in a["p_Solicitudes"]), resp, interno))
    return out


def secuencias(g):
    return ["%s:%s" % (s["secuencia"].split("::")[1], num(s["cantidad"])) for s in g["secuenciasSolicitadas"]]


def bloque(id_, slug, g, caso):
    var = "c" + id_
    exc = g.get("excepcion")
    L = ["        // FMM-CER-%s" % id_, "        CasoCerT5.Corrida %s = corre(%s);" % (var, j(slug))]
    if exc:
        clase, resto = exc.split(": ", 1) if ": " in exc else (exc, "")
        if clase == "Exception":
            L.append("        assertEquals(%s, mensajeDe(%s), %s);" % (j(resto), var, j(slug + " :: message of the plain Exception the legacy threw")))
        else:
            L.append("        assertEquals(%s, claseDe(%s), %s);" % (j(clase), var, j(slug + " :: class of the exception the legacy threw")))
    else:
        L.append("        assertNull(%s.excepcionTexto, %s);" % (var, j(slug + " :: the run finishes")))
        L.append("        assertEquals(%s, estados(%s), %s);" % (j(estados(g)), var, j(slug + " :: the output, row by row (OK = approved; the rows of the child first)")))
        L.append("        assertEquals(%s, contador(%s), %s);" % (j(num(g["salida"]["P_OUT_ERRORESCONTADOR"])), var, j(slug + " :: counter of the output")))
        L.append("        assertEquals(%s, mensajes(%s), %s);" % (j(mensajes(g)), var, j(slug + " :: messages of the output")))
    L.append("        assertEquals(%s, decision(%s), %s);" % (j(g["decisionServlet"]), var, j(slug + " :: decision of the host")))
    L.append("        assertEquals(%s, orden(%s), %s);" % (jl(orden(g)), var, j(slug + " :: order of the calls to the collaborators (the child is one record)")))
    L.append("        assertEquals(%s, filas(%s), %s);" % (jl(filas(g)), var, j(slug + " :: the rows of the table as the class left them: state|Spread|CostoUF|ValorUFFechaInversion|MontoMonedaSistema|IdIngresoEgreso|IdOperacionSpot|IdCartera|MontoComision|Paridad|FechaCertificacionRegistro")))
    L.append("        assertEquals(%s, tablasFinal(%s), %s);" % (j(tablas_final(g)), var, j(slug + " :: the state of the rows after the decision of the host")))
    L.append("        assertEquals(%s, log(%s), %s);" % (jl(log(g)), var, j(slug + " :: rows of the log of certification: id|solicitud|code|state|Monto|TipoCambio|ValorCuota|NumeroCuotas|Simulacion")))
    L.append("        assertEquals(%s, secuencias(%s), %s);" % (jl(secuencias(g)), var, j(slug + " :: sequence requests")))
    L.append("        assertEquals(%s, notificaciones(%s), %s);" % (jl(notificaciones(g)), var, j(slug + " :: asynchronous notifications: empresa|usuario|modulo|ids")))
    L.append("        assertEquals(%s, owp(%s), %s);" % (jl(owp(g)), var, j(slug + " :: OWP notifications: Solsec|Estado|Fecha|SolsecAsoc|Nemo|Divisa|NTitulos|Precio|Comision|TipoCambio")))
    L.append("        assertEquals(%s, agregarLog(%s), %s);" % (jl(agregar_log(g)), var, j(slug + " :: AgregarLog entries: tipo|modulo|usuario|empresa|fecha|tipoRegistro|estadoRegistro")))
    L.append("        assertEquals(%s, hijos(%s), %s);" % (jl(hijos(g)), var, j(slug + " :: the child: entry|solicitudes|answer|inner calls")))
    return L


def main():
    casos = []
    for d in sorted(CASOS.glob("FMM-CER-*")):
        c = leer(d / "case.json")
        g = leer(d / "expected.json")
        casos.append((d.name[8:11], d.name[12:], c, g))
    tarjetas = [("14", "salidaTempranaSinNotificaciones", "FM-CER-14 when no solicitud passes the rules the run ends EARLY: counter 1, the log of certification is written, nothing is notified, AgregarLog is not called"),
                ("61", "actualizacionMasivaACertificado", "FM-CER-61 the UPDATE writes the columns of the pending and warned solicitudes (Spread only RES, CostoUF RES or APO, ValorUFFechaInversion only APOREINV, state CRT) and never touches a rejected one"),
                ("62", "notificacionAsincrona", "FM-CER-62 the asynchronous notification carries the ids of the updated solicitudes in the order of the read, once per level, also in simulation and with an empty list; it fails after the UPDATE"),
                ("63", "recursionDelSwitch", "FM-CER-63 the child certification receives the header of the request with p_NotificaOWP N and the simulation flag, runs its own closing, and its counter, messages and results (first) are added"),
                ("64", "montoMonedaSistema", "FM-CER-64 the rescate of origin of an international reinversion aporte gets the SUM of MontoMonedaSistema AFTER the UPDATE to CRT and the child, BEFORE OWP and the log"),
                ("65", "notificacionOwp", "FM-CER-65 OWP is notified when the parameter AND the entry say S (equalsIgnoreCase): the switch aportes of channel OWP first, then the updated of channel OWP, re-read from the view; the child never notifies"),
                ("66", "logs", "FM-CER-66 AgregarLog receives ONE fixed record (its answer is never read) and the log of certification gets one row per solicitud (rejected included) with the ids of the sequence"),
                ("67", "marcaDeExitoYSalida", "FM-CER-67 the pending solicitudes are approved with UI_EXITO, the output carries the name of the state from PLA.Estado (a missing state raises NullPointerException after every effect) and the translated description"),
                ("68", "modoSimulacion", "FM-CER-68 with p_SimularOperacion exactly S the counter is forced to 1 at the very end (everything ran for real); any other value leaves the counter as the collaborators left it"),
                ("69", "rechazosQueNoSubenElContador", "FM-CER-69 a lot with a rejected solicitud and a certified one commits (counter 0 unless a collaborator adds it) and the rejection lives only in the output with RZD"),
                ("70", "ordenDeEfectos", "FM-CER-70 order of effects: custody, cuenta, Tesoreria, Facturacion, AR, Spot, UPDATE, notification, child, FM-CER-64, OWP, AgregarLog, log of certification; every exception rolls back everything and nothing is compensated"),
                ("71", "excepciones", "FM-CER-71 the exceptions of the catalogue come out untouched, with the effects written before them")]
    defectos = [("D-F3B-01", "defecto01SimulacionCorreDeVerdad", "D-F3B-01 simulation S runs for real (every collaborator, the UPDATE, the child) and only forces the counter to 1 at the end", "D-F3B-01"),
                ("D-F3B-02", "defecto02NotificacionesAntesDelCommit", "D-F3B-02 the asynchronous notification and OWP leave BEFORE the commit, also in simulation or with a counter greater than 0", "D-F3B-02"),
                ("D-F3B-03", "defecto03RechazoConEfectosPrevios", "D-F3B-03 a rejection of Tesoreria / Spot / AR / an exception after the custody and the cuenta leaves those writes done", "D-F3B-03"),
                ("D-F3B-16", "defecto16TipoAjeno", "D-F3B-16 a solicitud of a type other than APO / RES is certified without effect and the UPDATE has no guard of state", "D-F3B-16"),
                ("D-F3B-30", "defecto30SwitchNuloOAporteRechazado", "D-F3B-30 a null Switch raises NullPointerException; the aporte of a rescate AR rejected is certified anyway", "D-F3B-30"),
                ("D-F3B-31", "defecto31ErrataOnmibus", "D-F3B-31 the code of the omnibus rejection keeps its erratum UI_ONMIBUS_NO_CREADO", "D-F3B-31"),
                ("D-F3B-47", "defecto47IvaLeidoSiempre", "D-F3B-47 no row of IVA raises IndexOutOfBoundsException after the writes of the custody and the cuenta", "D-F3B-47"),
                ("D-F3B-49", "defecto49SumaDespuesDelUpdate", "D-F3B-49 FM-CER-64 sums the rows after the UPDATE: the siblings keep their table value, the ones of the request their new one", "D-F3B-49"),
                ("D-F3B-50", "defecto50RecursionSinGuarda", "D-F3B-50 the LEGACY recursion has no guard: the golden ran three levels (the new facade fails closed at depth 2, CerT5CompuertaTest)", "D-F3B-50"),
                ("D-NOR-9", "defectoNor9ClavesRepetidas", "D-NOR-9 repeated keys of a List2Hash raise the legacy Exception 'key field is not unique'", "D-NOR-9"),
                ("M3a", "m3aMismaFamiliaConRetiro", "M3 (a) same family WITH the custody withdrawal: one aporte per cartera, the run reaches the closing", "T5-m3:a"),
                ("M3b", "m3bMismaFamiliaSinRetiro", "M3 (b) same family WITHOUT custody withdrawal for the rescate: the rescate is rejected (FM-CER-40) and AR is never called", "T5-m3:b"),
                ("M3c", "m3cMovimientosEnCero", "M3 (c) same family with every movement in zero: IndexOutOfBoundsException of the first aporte (D-F3B-13 / 45)", "T5-m3:c")]
    metodos = []
    for num_, nombre, doc in tarjetas:
        lista = [(i, s, c, g) for i, s, c, g in casos if c["ref"][0] == "FM-CER-" + num_]
        metodos.append(("fmCer%s_%s" % (num_, nombre), doc, lista))
    for ref, nombre, doc, tag in defectos:
        lista = [(i, s, c, g) for i, s, c, g in casos if tag in c["ref"]]
        metodos.append((nombre, doc, lista))
    L = [PLANTILLA_CABECERA]
    for nombre, doc, lista in metodos:
        if not lista:
            continue
        L.append("")
        L.append("    @Test")
        L.append("    @DisplayName(%s)" % j(doc))
        L.append("    void %s() throws Exception {" % nombre)
        for i, s, c, g in lista:
            L += bloque(i, s, g, c)
        L.append("    }")
    L.append("}")
    OUT.write_text("\n".join(L) + "\n", encoding="utf-8", newline="\n")
    print("CerT5ReglasTest.java written: %d tests, %d case blocks" % (sum(1 for m in metodos if m[2]), sum(len(m[2]) for m in metodos)))


PLANTILLA_CABECERA = r'''package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * GENERATED by gen_reglas_cer_t5.py from the RECORDED goldens (do not edit by hand): rule by rule, with LITERAL values, every card FM-CER-61..71 (front J, the closing) plus the early exit
 * FM-CER-14, the defects they pin (D-F3B-01/02/03/16/30/31/47/49/50, D-NOR-9), the three paths of the review M3 and RULE-076 (no credential, H2 only), against the FACADE (see
 * {@code API_ASSUMED_T5.md}). The expected values are the ones the legacy produced when the goldens were recorded, written as literals read in the golden (each assertion names the case). Each test
 * runs the case through {@link CasoCerT5#ejecutar} (the whole chain A..J of the production facade; with {@code -Dcer.autocomprobacion=true} the GOLDEN itself: the proof that the assertions describe
 * the legacy before any production code exists) and asserts on what the run did: the output row by row, the counter and the messages, the host decision, the order of the calls, the rows of the
 * table as the class left them and after the decision, the log of certification, the sequences, the asynchronous and OWP notifications, the AgregarLog record and the child certification.
 *
 * <p>Projections (the same text for every case): {@code filas} = {@code id:Estado|Spread|CostoUF|ValorUFFechaInversion|MontoMonedaSistema|IdIngresoEgreso|IdOperacionSpot|IdCartera|MontoComision|
 * ParidadFechaCertificacion|FechaCertificacionRegistro}; {@code log} = {@code IdLog|IdSolicitud|Codigo|IdEstado|Monto|TipoCambio|ValorCuota|NumeroCuotas|Simulacion}; {@code owp} = per entry
 * {@code Solsec|Estado|Fecha|SolsecAsoc|Nemo|Divisa|NTitulos|Precio|Comision|TipoCambio}; numbers by value.
 *
 * <p>RED BEFORE CODE: until the facade exists every test fails with "T5 not implemented: the production class ... does not exist yet".
 */
class CerT5ReglasTest {

    private static final String T_SOL = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";
    private static final String T_LOG = "BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate";
    private static final List<String> LECTURAS = List.of("EntregarPreciosTasas", "CalcularParidad", "ConsultarEmpresa", "ConsultarFlujoCustodia");

    private static CasoCerT5.Corrida corre(String slug) throws Exception {
        CasoCerT5.Corrida c = CasoCerT5.ejecutar(CasoCerT5.idPorSlug(slug));
        assertNotNull(c.volcado, slug);
        return c;
    }

    private static String claseDe(CasoCerT5.Corrida c) {
        assertNotNull(c.excepcionTexto, "the run must raise");
        return c.excepcionTexto.substring(0, c.excepcionTexto.indexOf(':'));
    }

    private static String mensajeDe(CasoCerT5.Corrida c) {
        assertNotNull(c.excepcionTexto, "the run must raise");
        return c.excepcionTexto.substring(c.excepcionTexto.indexOf(':') + 2);
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

    private static String une(String... partes) {
        return String.join("|", partes);
    }

    private static String decision(CasoCerT5.Corrida c) {
        return c.volcado.get("decisionServlet").getAsString();
    }

    private static String contador(CasoCerT5.Corrida c) {
        return txt(c.salida, "P_OUT_ERRORESCONTADOR");
    }

    private static String mensajes(CasoCerT5.Corrida c) {
        List<String> ms = new ArrayList<>();
        for (JsonElement e : c.salida.getAsJsonArray("P_OUT_MENSAJES")) {
            JsonObject m = e.getAsJsonObject();
            ms.add(txt(m, "id") + ":" + txt(m, "tipo") + ":" + txt(m, "codigo") + ":" + txt(m, "descripcion"));
        }
        return String.join("|", ms);
    }

    private static String estados(CasoCerT5.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : c.salida.getAsJsonArray("P_OUT_RESULTADO")) {
            JsonObject f = e.getAsJsonObject();
            out.add(txt(f, "IdSolicitudAporteRescate") + "=" + (txt(f, "Estado").equals("APD") ? "OK" : txt(f, "Estado") + ":" + txt(f, "Codigo")));
        }
        return String.join(";", out);
    }

    private static List<String> orden(CasoCerT5.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : c.volcado.getAsJsonArray("colaboradores")) {
            String n = e.getAsJsonObject().get("colaborador").getAsString();
            if (!LECTURAS.contains(n)) out.add(n.equals(CasoCerT5.HIJO) ? "hijo" : n);
        }
        return out;
    }

    private static List<String> filas(CasoCerT5.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : c.volcado.getAsJsonObject("tablasTrasLaLlamada").getAsJsonArray(T_SOL)) {
            JsonObject r = e.getAsJsonObject();
            out.add(une(txt(r, "IdSolicitudAporteRescate") + ":" + txt(r, "Estado"), txt(r, "Spread"), txt(r, "CostoUF"), txt(r, "ValorUFFechaInversion"), txt(r, "MontoMonedaSistema"), txt(r, "IdIngresoEgreso"),
                    txt(r, "IdOperacionSpot"), txt(r, "IdCartera"), txt(r, "MontoComision"), txt(r, "ParidadFechaCertificacion"), txt(r, "FechaCertificacionRegistro")));
        }
        return out;
    }

    private static String tablasFinal(CasoCerT5.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : c.volcado.getAsJsonObject("tablas").getAsJsonArray(T_SOL)) out.add(txt(e.getAsJsonObject(), "IdSolicitudAporteRescate") + ":" + txt(e.getAsJsonObject(), "Estado"));
        return String.join(";", out);
    }

    private static List<String> log(CasoCerT5.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : c.volcado.getAsJsonObject("tablasTrasLaLlamada").getAsJsonArray(T_LOG)) {
            JsonObject r = e.getAsJsonObject();
            out.add(une(txt(r, "IdLogCertificacionAporteRescate"), txt(r, "IdSolicitudAporteRescate"), txt(r, "Codigo"), txt(r, "IdEstado"), txt(r, "Monto"), txt(r, "TipoCambio"), txt(r, "ValorCuota"),
                    txt(r, "NumeroCuotas"), txt(r, "Simulacion")));
        }
        return out;
    }

    private static List<String> secuencias(CasoCerT5.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : c.volcado.getAsJsonArray("secuenciasSolicitadas")) {
            JsonObject s = e.getAsJsonObject();
            String n = s.get("secuencia").getAsString();
            out.add(n.substring(n.indexOf("::") + 2) + ":" + num(s.get("cantidad")));
        }
        return out;
    }

    private static List<JsonObject> llamadas(CasoCerT5.Corrida c, String colaborador) {
        List<JsonObject> out = new ArrayList<>();
        for (JsonElement e : c.volcado.getAsJsonArray("colaboradores")) {
            if (e.getAsJsonObject().get("colaborador").getAsString().equals(colaborador)) out.add(e.getAsJsonObject());
        }
        return out;
    }

    private static List<String> notificaciones(CasoCerT5.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonObject l : llamadas(c, "NotificarEstadoOrdenFondoMutuo")) {
            JsonObject a = l.getAsJsonArray("argumentos").get(0).getAsJsonObject();
            List<String> ids = new ArrayList<>();
            for (JsonElement x : a.getAsJsonArray("p_Solicitudes")) ids.add(num(x));
            out.add(une(txt(a, "p_IdEmpresa"), txt(a, "p_IdUsuario"), txt(a, "p_IdModulo"), String.join(",", ids)));
        }
        return out;
    }

    private static List<String> owp(CasoCerT5.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonObject l : llamadas(c, "NotificarEstadoOrdenesOWP")) {
            List<String> entradas = new ArrayList<>();
            for (JsonElement x : l.getAsJsonArray("argumentos").get(0).getAsJsonArray()) {
                JsonObject e = x.getAsJsonObject();
                entradas.add(une(txt(e, "Solsec"), txt(e, "Estado"), txt(e, "Fecha"), txt(e, "SolsecAsoc"), txt(e, "Nemo"), txt(e, "Divisa"), txt(e, "NTitulos"), txt(e, "Precio"), txt(e, "Comision"), txt(e, "TipoCambio")));
            }
            out.add(String.join(";", entradas));
        }
        return out;
    }

    private static List<String> agregarLog(CasoCerT5.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonObject l : llamadas(c, "AgregarLog")) {
            for (JsonElement x : l.getAsJsonArray("argumentos").get(0).getAsJsonObject().getAsJsonArray("p_Log")) {
                JsonObject e = x.getAsJsonObject();
                out.add(une(txt(e, "IdTipoLog"), txt(e, "IdModulo"), txt(e, "IdUsuario"), txt(e, "IdEmpresa"), txt(e, "FechaSistemaLog"), txt(e, "IdTipoRegistro"), txt(e, "EstadoRegistro")));
            }
        }
        return out;
    }

    private static List<String> hijos(CasoCerT5.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonObject l : llamadas(c, CasoCerT5.HIJO)) {
            JsonObject a = l.getAsJsonArray("argumentos").get(0).getAsJsonObject();
            List<String> sols = new ArrayList<>();
            for (JsonElement s : a.getAsJsonArray("p_Solicitudes")) sols.add(txt(s.getAsJsonObject(), "IdSolicitudAporteRescate") + "/" + txt(s.getAsJsonObject(), "IdTipoCanalIngreso"));
            String resp;
            JsonElement r = l.get("respuesta");
            if (r.isJsonPrimitive()) {
                resp = r.getAsString();
            } else {
                List<String> filas = new ArrayList<>();
                for (JsonElement x : r.getAsJsonObject().getAsJsonArray("P_OUT_RESULTADO")) {
                    JsonObject f = x.getAsJsonObject();
                    filas.add(txt(f, "IdSolicitudAporteRescate") + "=" + (txt(f, "Estado").equals("APD") ? "OK" : txt(f, "Estado") + ":" + txt(f, "Codigo")));
                }
                resp = "contador=" + txt(r.getAsJsonObject(), "P_OUT_ERRORESCONTADOR") + ";" + String.join(";", filas);
            }
            List<String> internas = new ArrayList<>();
            if (l.has("internas")) {
                for (JsonElement x : l.getAsJsonArray("internas")) {
                    String n = x.getAsJsonObject().get("colaborador").getAsString();
                    if (!LECTURAS.contains(n)) internas.add(n.equals(CasoCerT5.HIJO) ? "hijo" : n);
                }
            }
            out.add(une(txt(a, "p_IdEmpresa"), txt(a, "p_IdUsuario"), txt(a, "p_IdModulo"), txt(a, "p_SimularOperacion"), txt(a, "p_UsaReglasNegocio"), txt(a, "p_FechaCertificacion"),
                    txt(a, "p_IdTipoCanalIngreso"), txt(a, "p_NotificaOWP"), txt(a, "p_CertificacionAporteSwitchSimulado"), String.join(",", sols), resp, String.join(",", internas)));
        }
        return out;
    }
'''

if __name__ == "__main__":
    main()
