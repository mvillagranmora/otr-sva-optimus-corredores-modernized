package cl.bcs.oracle;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
import cl.bcs.fondomutuo.comisionnn.ComisionNnJdbc;
import cl.bcs.fondomutuo.custodia.FlujoCustodiaFmJdbc;
import cl.bcs.fondomutuo.kit.AccesoDatosException;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.normal.BloqueoSolicitudJdbc;
import cl.bcs.fondomutuo.normal.NormalFabrica;
import cl.bcs.fondomutuo.normal.NormalJavaCallHandler;
import cl.bcs.fondomutuo.normal.NormalJdbc;
import cl.bcs.fondomutuo.normal.NormalPort;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorJdbc;
import cl.bcs.fondomutuo.requisitos.RequisitosJdbc;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * The NEW CrearSolicitudAporteRescateNormal as a Subject of the oracle: the same OracleRunner, the same H2 session (SessionConnection: lock procedures, dialect),
 * the same seeded base and the same registered doubles ({@link Doubles}) answer the collaborators outside the closure; the dump has the legacy format.
 * Run with ORACLE_SUBJECT=cl.bcs.oracle.NormalSubject ORACLE_OUT=dir (record.sh) and compare with compare_nor.py. Gates G3, G4 and G-CUS are open (the legacy
 * has no gates); G1/G2 stay closed (F2 classes) and answer a ServicioNoServidoException. Only the Manejador is not served here (step 4B).
 */
public class NormalSubject implements Subject {

    @Override
    public JsonObject call(String servicio, Connection con, JsonObject jin) throws Exception {
        if (!servicio.equals("CrearSolicitudAporteRescateNormal")) throw new IllegalArgumentException("NormalSubject serves only CrearSolicitudAporteRescateNormal");
        DataSource ds = (DataSource) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { DataSource.class }, (p, m, a) -> {
            if (m.getName().equals("getConnection")) return conexionSinCierre(con);
            throw new UnsupportedOperationException(m.getName());
        });
        ConexionUnidadDeTrabajo conexion = new ConexionUnidadDeTrabajo(ds);
        NormalFabrica.Dependencias d = new NormalFabrica.Dependencias();
        ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(conexion);
        DiccionarioJdbc diccionario = new DiccionarioJdbc(conexion);
        d.configuracion = configuracion;
        d.diccionario = diccionario;
        d.basicos = new RevisarRequisitosBasicosJdbc(conexion, diccionario);
        d.calendario = new CalendarioJdbc(conexion, configuracion);
        d.empresas = id -> new Gson().fromJson(Doubles.answer("ConsultarEmpresa", "logica", new Object[] { id }, JsonObject.class), InformacionEmpresa.class);
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
        d.secuencias = secuencias(con);
        d.paridad = e -> doble("CalcularParidad", e);
        d.precios = e -> doble("EntregarPreciosTasas", e);
        d.custodia = e -> doble("ConsultarFlujoCustodia", e);
        d.flujoCaja = e -> doble("SaldoFlujoCaja", e);
        d.log = e -> doble("AgregarLog", e);
        d.batch = e -> doble("InscribirProcesoBatch", e);
        d.owp = e -> Doubles.answer("NotificarEstadoOrdenesOWP", "go", new Object[] { e.get("p_Solicitudes") }, JsonObject.class);
        d.productos = e -> doble("ConsultarProductosPortafolio", e);
        // F2 W1: G1 lifted. The REAL new ProcesarIngresoEgresoTesoreria on the oracle session; Tesoreria's CrearIngresoEgreso is the registered double
        // (layer 1 of PLAN_FONDOMUTUO_F2 7). -Doracle.g1=cerrada restores the F1 behaviour (G1 closed, 503) for the comparison of the F1 approvals.
        boolean g1 = !"cerrada".equals(System.getProperty("oracle.g1"));
        d.tesoreria = new cl.bcs.fondomutuo.tesoreria.ProcesarIngresoEgresoTesoreriaService(new cl.bcs.fondomutuo.tesoreria.TesoreriaJdbc(conexion), e -> {
            JsonObject r = Doubles.answer("CrearIngresoEgreso", "logica", new Object[] { e }, JsonObject.class);
            return r == null ? null : new com.google.gson.GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create().fromJson(r,
                    cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.CrearIngresoEgreso_Salida.class);
        });
        d.spot = e -> {
            throw new cl.bcs.fondomutuo.kit.ServicioNoServidoException("G2", "F2");
        };
        d.compuertas = new Compuertas("NORMAL", g1, false, true, true, true);
        NormalPort normal = NormalFabrica.crear(d);
        try {
            return new NormalJavaCallHandler(normal).call(con, jin);
        } catch (AccesoDatosException e) {
            throw e.getCause() instanceof SQLException ? (SQLException) e.getCause() : e;   // the legacy rethrows the SQLException of the driver
        }
    }

    private static JsonObject doble(String colaborador, JsonObject entrada) {
        return Doubles.answer(colaborador, "logica", new Object[] { entrada }, JsonObject.class);
    }

    /** {@code Utils.getSecuencia} of the oracle shim: one NEXT VALUE per value, the request registered in the dump. */
    private static SecuenciaPort secuencias(Connection con) {
        return (nombre, cantidad) -> {
            List<Long> salida = new ArrayList<>();
            if (cantidad == null || cantidad <= 0) return salida;
            Doubles.registrarSecuencia(nombre, cantidad);
            try {
                for (long k = 0; k < cantidad; k++) {
                    try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT NEXT VALUE FOR \"" + nombre + "\"")) {
                        rs.next();
                        salida.add(rs.getLong(1));
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return salida;
        };
    }

    private static Connection conexionSinCierre(Connection con) {
        return (Connection) Proxy.newProxyInstance(NormalSubject.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            if (m.getName().equals("close")) return null;
            try {
                return m.invoke(con, a);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }
}
