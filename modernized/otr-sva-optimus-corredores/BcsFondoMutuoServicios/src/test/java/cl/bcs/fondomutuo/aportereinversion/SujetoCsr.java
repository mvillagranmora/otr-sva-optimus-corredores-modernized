package cl.bcs.fondomutuo.aportereinversion;

import java.sql.Connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
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
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorJdbc;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.http.ProductosPortafolioPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;
import cl.bcs.fondomutuo.requisitos.RequisitosJdbc;

/**
 * The NEW {@code CrearSolicitudAporteReinversion} over the REAL JDBC adapters on the H2 of the case. What is not ported or is a port behind a gate is answered by the case script
 * ({@link Guion}), recording the call with its COMPLETE argument exactly as the oracle's registered doubles do: Plataforma (ConsultarEmpresa, CalcularParidad, EntregarPreciosTasas,
 * SaldoFlujoCaja, AgregarLog, ConsultarProductosPortafolio), Custodia (ConsultarFlujoCustodia) and the two FondoMutuo helpers behind their typed ports,
 * {@code ProcesosExternos.TesoreriaPort} (ProcesarIngresoEgresoTesoreria, gate G1) and {@code ProcesosExternos.SpotPort} (ProcesarIngresoOperacionSpot, gate G2).
 * The production classes are reached ONLY through {@link ApiCsr}.
 */
final class SujetoCsr {

    /** Test hooks: replace a port by the test (null = the case script). */
    static volatile ProcesosExternos.TesoreriaPort tesoreriaPrueba;
    static volatile ProcesosExternos.SpotPort spotPrueba;
    static volatile LogPort logPrueba;

    /** Same settings as the oracle's {@code cl.bcs.oracle.Doubles}: arguments recorded with nulls, answers decoded as the legacy Gson of BcsUtils. */
    private static final Gson SALIDA_JSON = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().disableHtmlEscaping().create();
    private static final Gson ENTRADA_JSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();

    private SujetoCsr() {
    }

    static Object crear(ConexionUnidadDeTrabajo conexion, Connection con, Guion guion, JsonArray secuencias, Compuertas compuertas) {
        Object d = ApiCsr.dependencias();
        ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(conexion);
        DiccionarioJdbc diccionario = new DiccionarioJdbc(conexion);
        ApiCsr.poner(d, "configuracion", configuracion);
        ApiCsr.poner(d, "diccionario", diccionario);
        ApiCsr.poner(d, "basicos", new RevisarRequisitosBasicosJdbc(conexion, diccionario));
        ApiCsr.poner(d, "calendario", new CalendarioJdbc(conexion, configuracion));
        EmpresaPort empresas = id -> new Gson().fromJson(guion.responder("ConsultarEmpresa", new JsonPrimitive(id)), InformacionEmpresa.class);
        ApiCsr.poner(d, "empresas", empresas);
        ApiCsr.poner(d, "series", new SeriesFondoJdbc(conexion));
        ApiCsr.poner(d, "requisitosDatos", new RequisitosJdbc(conexion));
        ApiCsr.poner(d, "comisionNnDatos", new ComisionNnJdbc(conexion));
        ApiCsr.poner(d, "datos", ApiCsr.nuevoDatos(conexion));
        ApiCsr.poner(d, "perfilPort", new PerfilFondoMutuoJdbc(conexion));
        ApiCsr.poner(d, "fechasPort", new FechasJdbc(conexion));
        ApiCsr.poner(d, "cuotasPort", new CuotasJdbc(conexion));
        ApiCsr.poner(d, "patrimonioPort", new PatrimonioEmisorJdbc(conexion));
        ApiCsr.poner(d, "flujoCustodiaDatos", new FlujoCustodiaFmJdbc(conexion));
        ApiCsr.poner(d, "agfDatos", new AgfJdbc(conexion));
        ApiCsr.poner(d, "secuencias", new SecuenciaH2(con, secuencias));
        ParidadPort paridad = e -> guion.responder("CalcularParidad", e);
        PreciosTasasPort precios = e -> guion.responder("EntregarPreciosTasas", e);
        CustodiaPort custodia = e -> guion.responder("ConsultarFlujoCustodia", e);
        FlujoCajaPort flujoCaja = e -> guion.responder("SaldoFlujoCaja", e);
        LogPort log = logPrueba != null ? logPrueba : e -> guion.responder("AgregarLog", e);
        ProductosPortafolioPort productos = e -> guion.responder("ConsultarProductosPortafolio", e);
        ApiCsr.poner(d, "paridad", paridad);
        ApiCsr.poner(d, "precios", precios);
        ApiCsr.poner(d, "custodia", custodia);
        ApiCsr.poner(d, "flujoCaja", flujoCaja);
        ApiCsr.poner(d, "log", log);
        ApiCsr.poner(d, "productos", productos);
        ProcesosExternos.TesoreriaPort tesoreria = tesoreriaPrueba != null ? tesoreriaPrueba
                : e -> ENTRADA_JSON.fromJson(guion.responder("ProcesarIngresoEgresoTesoreria", SALIDA_JSON.toJsonTree(e)), ProcesosExternos.SalidaTesoreria.class);
        ProcesosExternos.SpotPort spot = spotPrueba != null ? spotPrueba
                : e -> ENTRADA_JSON.fromJson(guion.responder("ProcesarIngresoOperacionSpot", SALIDA_JSON.toJsonTree(e)), ProcesosExternos.SalidaSpot.class);
        ApiCsr.poner(d, "tesoreria", tesoreria);
        ApiCsr.poner(d, "spot", spot);
        ApiCsr.poner(d, "compuertas", compuertas);
        return ApiCsr.crear(d);
    }
}
