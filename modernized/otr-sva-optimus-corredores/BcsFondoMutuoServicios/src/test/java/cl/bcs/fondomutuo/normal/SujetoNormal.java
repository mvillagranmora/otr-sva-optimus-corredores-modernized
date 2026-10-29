package cl.bcs.fondomutuo.normal;

import java.sql.Connection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ComisionesJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.CuotasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.FechasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.PerfilFondoMutuoJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.RevisarRequisitosBasicosJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.agf.AgfJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.cierre3b.SecuenciaH2;
import cl.bcs.fondomutuo.comisionnn.ComisionNnJdbc;
import cl.bcs.fondomutuo.custodia.FlujoCustodiaFmJdbc;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorJdbc;
import cl.bcs.fondomutuo.requisitos.RequisitosJdbc;

/** The Normal service over the REAL JDBC adapters on the H2 of the case; the collaborators outside the closure are answered by the case script ({@link Guion}). */
final class SujetoNormal {

    /** Test hook: scripted F2 collaborators and their gates OPEN (G1, G2). Null = closed and unreachable (the default). */
    static ProcesosExternos.TesoreriaPort tesoreriaF2;
    static ProcesosExternos.SpotPort spotF2;

    /** Same settings as the oracle's {@code cl.bcs.oracle.Doubles}: arguments recorded with nulls, answers decoded as the legacy Gson of BcsUtils. */
    private static final Gson TESORERIA_OUT = new com.google.gson.GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .serializeNulls().disableHtmlEscaping().create();
    private static final Gson TESORERIA_IN = new com.google.gson.GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();

    private SujetoNormal() {
    }

    static NormalPort crear(ConexionUnidadDeTrabajo conexion, Connection con, Guion guion, JsonArray secuencias, Compuertas compuertas) {
        return crear(conexion, con, guion, secuencias, compuertas, false);
    }

    /** {@code tesoreriaReal}: F2 W1B layer 2, the helper over the REAL tesoreria-service library ({@link TesoreriaReal}). */
    static NormalPort crear(ConexionUnidadDeTrabajo conexion, Connection con, Guion guion, JsonArray secuencias, Compuertas compuertas, boolean tesoreriaReal) {
        NormalFabrica.Dependencias d = new NormalFabrica.Dependencias();
        ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(conexion);
        DiccionarioJdbc diccionario = new DiccionarioJdbc(conexion);
        d.configuracion = configuracion;
        d.diccionario = diccionario;
        d.basicos = new RevisarRequisitosBasicosJdbc(conexion, diccionario);
        d.calendario = new CalendarioJdbc(conexion, configuracion);
        d.empresas = id -> new Gson().fromJson(guion.responder("ConsultarEmpresa", new JsonPrimitive(id)), InformacionEmpresa.class);
        d.series = new SeriesFondoJdbc(conexion);
        d.requisitosDatos = new RequisitosJdbc(conexion);
        d.comisionNnDatos = new ComisionNnJdbc(conexion);
        d.normalDatos = new NormalJdbc(conexion);
        d.bloqueo = new BloqueoSolicitudJdbc(conexion);
        d.perfilPort = new PerfilFondoMutuoJdbc(conexion);
        d.fechasPort = new FechasJdbc(conexion);
        d.cuotasPort = new CuotasJdbc(conexion);
        d.comisionesPort = new ComisionesJdbc(conexion);
        d.patrimonioPort = new PatrimonioEmisorJdbc(conexion);
        d.flujoCustodiaDatos = new FlujoCustodiaFmJdbc(conexion);
        d.agfDatos = new AgfJdbc(conexion);
        d.secuencias = new SecuenciaH2(con, secuencias);
        d.paridad = e -> guion.responder("CalcularParidad", e);
        d.precios = e -> guion.responder("EntregarPreciosTasas", e);
        d.custodia = e -> guion.responder("ConsultarFlujoCustodia", e);
        d.flujoCaja = e -> guion.responder("SaldoFlujoCaja", e);
        d.log = e -> guion.responder("AgregarLog", e);
        d.batch = e -> guion.responder("InscribirProcesoBatch", e);
        d.owp = e -> guion.responder("NotificarEstadoOrdenesOWP", e.get("p_Solicitudes"));
        d.productos = e -> guion.responder("ConsultarProductosPortafolio", e);
        // F2 W1: G1 lifted. The REAL new ProcesarIngresoEgresoTesoreria over the H2 of the case; Tesoreria's CrearIngresoEgreso is the scripted
        // collaborator of the oracle (layer 1 of plan 7: the legacy golden records the CrearIngresoEgreso_Entrada its double received). A test that sets
        // tesoreriaF2 replaces the whole helper (NormalTesoreriaSpotAbiertasTest).
        d.tesoreria = tesoreriaF2 != null ? tesoreriaF2
                : tesoreriaReal ? TesoreriaReal.crear(conexion, con, guion, secuencias, d.basicos, compuertas)
                : new cl.bcs.fondomutuo.tesoreria.ProcesarIngresoEgresoTesoreriaService(new cl.bcs.fondomutuo.tesoreria.TesoreriaJdbc(conexion),
                        e -> TESORERIA_IN.fromJson(guion.responder("CrearIngresoEgreso", TESORERIA_OUT.toJsonTree(e)),
                                cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.CrearIngresoEgreso_Salida.class));
        d.spot = spotF2 != null ? spotF2 : e -> {
            throw new IllegalStateException("the Spot port was reached with the gate G2 closed");
        };
        d.compuertas = compuertas;
        return NormalFabrica.crear(d);
    }
}
