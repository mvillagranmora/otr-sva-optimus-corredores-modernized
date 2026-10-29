package cl.bcs.fondomutuo.normal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.out.http.ColaboradoresHttp;
import cl.bcs.fondomutuo.adapter.out.http.RouterClient;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.tesoreria.ColaboradoresTesoreria;
import cl.bcs.fondomutuo.tesoreria.ProcesarIngresoEgresoTesoreriaService;
import cl.bcs.fondomutuo.tesoreria.TesoreriaJdbc;
import cl.bcs.tesoreria.infrastructure.adapter.ConfiguracionSistemaJdbc;
import cl.bcs.tesoreria.infrastructure.adapter.DiccionarioJdbc;
import cl.bcs.tesoreria.infrastructure.adapter.ProcedimientoAlmacenado;
import cl.bcs.tesoreria.javacall.CrearComprobanteJavaService;
import cl.bcs.tesoreria.javacall.CrearComprobanteProcedimiento;
import cl.bcs.tesoreria.javacall.CrearComprobanteService;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService;
import cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService;
import cl.bcs.tesoreria.port.out.Secuencias;

/**
 * F2 W1B, the NEW side of the layer-2 cases: the ported helper over the REAL tesoreria-service library, built with the same classes that
 * {@code TesoreriaEnProcesoConfiguration} imports (Tesoreria's own JDBC adapters, on the connection of the case session; no Spring proxy is needed: one
 * connection, the harness commits or rolls back like CallServlet). Harness stand-ins, each the counterpart of what the oracle does for the legacy:
 * <ul>
 *   <li>router of {@link ColaboradoresTesoreria}: AgregarLog / SaldoFlujoCaja = the scripted collaborators of the case (registered doubles in the oracle);
 *       RevisarRequisitosBasicos = the module's kit port on the same session, NOT recorded (the legacy runs the REAL Plataforma class in process);</li>
 *   <li>Tesoreria's {@code Secuencias}: H2 sequence of the same name, recorded like the oracle's {@code Utils.getSecuencia} shim; the library draws one
 *       value per call where the legacy asks {@code getSecuencia(name, n)} once, so consecutive requests of the same Tesoreria sequence are recorded as
 *       ONE {secuencia, cantidad} (same values drawn, same order);</li>
 *   <li>the procedure CrearComprobante: {@link TesoreriaSesion} at the JDBC call (both sides).</li>
 * </ul>
 */
final class TesoreriaReal {

    private TesoreriaReal() {
    }

    static ProcesarIngresoEgresoTesoreriaService crear(cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo conexion, Connection con, Guion guion,
            JsonArray secuencias, RevisarRequisitosBasicosPort basicos, Compuertas compuertas) {
        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(con, true));
        ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(jdbc);
        DiccionarioJdbc diccionario = new DiccionarioJdbc(jdbc);
        ColaboradoresTesoreria colaboradores = new ColaboradoresTesoreria(router(guion, basicos), compuertas, new ProcedimientoAlmacenado(jdbc),
                new ActualizarSaldosCuentaBancoService(jdbc)::execute);
        CrearComprobanteProcedimiento procedimiento = new CrearComprobanteProcedimiento(jdbc, colaboradores);
        CrearComprobanteService comprobante = new CrearComprobanteService(configuracion, procedimiento,
                new CrearComprobanteJavaService(jdbc, diccionario, procedimiento, colaboradores));
        CrearIngresoEgresoService crearIngresoEgreso = new CrearIngresoEgresoService(jdbc, configuracion, diccionario, secuencias(con, secuencias), comprobante,
                new LiquidarComprobanteService(jdbc, configuracion, diccionario, colaboradores), colaboradores);
        return new ProcesarIngresoEgresoTesoreriaService(new TesoreriaJdbc(conexion), crearIngresoEgreso::logica);
    }

    private static RouterClient router(Guion guion, RevisarRequisitosBasicosPort basicos) {
        return new RouterClient("http://localhost:1", Duration.ofMillis(100), Duration.ofMillis(100), "") {
            @Override
            public JsonObject invocar(String clase, JsonObject params) {
                if (clase.equals("cl.bcs.plataforma.pla.RevisarRequisitosBasicos")) {
                    var s = basicos.revisar(params.get("p_IdEmpresa").getAsLong(), params.get("p_IdUsuario").getAsLong(), params.get("p_IdModulo").getAsString());
                    return JsonLegacy.conNulos().toJsonTree(s).getAsJsonObject();
                }
                String nombre = clase.equals(ColaboradoresHttp.AGREGAR_LOG) ? "AgregarLog" : clase.equals(ColaboradoresHttp.SALDO_FLUJO_CAJA) ? "SaldoFlujoCaja" : null;
                if (nombre == null) throw new IllegalStateException("router of the W1B harness: unexpected collaborator " + clase);
                try {
                    return guion.responder(nombre, params);
                } catch (IllegalStateException sinGuion) {
                    throw sinGuion;
                } catch (RuntimeException fallo) {
                    // a scripted failure of an HTTP collaborator = the router answers 503 (what RouterClient raises, nothing confirmed yet)
                    throw new cl.bcs.fondomutuo.kit.ServicioNoServidoException("ROUTER", clase + " not served (HTTP 503): " + fallo.getMessage());
                }
            }
        };
    }

    private static Secuencias secuencias(Connection con, JsonArray registro) {
        return nombre -> {
            JsonObject ultimo = registro.size() > 0 ? registro.get(registro.size() - 1).getAsJsonObject() : null;
            if (ultimo != null && ultimo.has("tesoreria") && ultimo.get("secuencia").getAsString().equals(nombre)) {
                ultimo.addProperty("cantidad", ultimo.get("cantidad").getAsLong() + 1);
            } else {
                JsonObject o = new JsonObject();
                o.addProperty("secuencia", nombre);
                o.addProperty("cantidad", 1L);
                o.addProperty("tesoreria", true);   // removed from the dump by CasoNormal (marks the entries this recorder may extend)
                registro.add(o);
            }
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT NEXT VALUE FOR \"" + nombre + "\"")) {
                rs.next();
                return rs.getLong(1);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        };
    }
}
