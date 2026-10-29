package cl.bcs.tesoreria.javacall;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.carga.CargaMasivaPac.ClaveUniverso;
import cl.bcs.tesoreria.javacall.carga.CargaMasivaPac.DetalleCargaMasiva;
import cl.bcs.tesoreria.javacall.carga.CargaMasivaPac.DetalleUniverso;
import cl.bcs.tesoreria.javacall.carga.CargaMasivaPac.Entrada;
import cl.bcs.tesoreria.javacall.carga.CargaMasivaPac.Salida;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;

/**
 * Legacy {@code cl.bcs.tesoreria.CrearCargaMasivaPac} (TES-B-214..220): replaces the PAC "universe" (file type UNI): the active universe rows
 * become INA, every PAC mandate with an active load state becomes INA, the new lines are inserted (sequence ids, blanks removed from the service
 * identifier) and the mandates that appear in the universe and have a valid contract are reactivated.
 *
 * <p>Request and response use a plain Gson (the legacy {@code new Gson()}: nulls are not serialised).
 */
@Service
public class CrearCargaMasivaPacService {

    private static final Logger log = LoggerFactory.getLogger(CrearCargaMasivaPacService.class);
    private static final Gson GSON = new Gson();

    private static final String SQL_INSERT_UNIVERSO_PAC = SqlNombrado.recurso("CrearCargaMasivaPac_InsertUniversoPac");
    private static final String SQL_UPDATE_UNIVERSO_PAC = SqlNombrado.recurso("CrearCargaMasivaPac_UpdateUniversoPac");
    private static final String SQL_UPDATE_ESTADO_ARCHIVO_PAC = SqlNombrado.recurso("CrearCargaMasivaPac_UpdateEstadoUniversoPac");
    private static final String SQL_UPDATE_DESHABILITAR_M_PAC = SqlNombrado.recurso("CrearCargaMasivaPac_UpdateDeshabilitarMandatoPac");
    private static final String SQL_SELECT_UNIVERSO_PAC = SqlNombrado.recurso("CrearCargaMasivaPac_SelectUniversoPac");

    /** First id of the lines of the load (the SQL takes the real id from the sequence; kept as the legacy parameter). */
    private static final long CONTADOR_INICIAL = 16L;

    private final SqlNombrado sql;

    public CrearCargaMasivaPacService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject jin) throws Exception {
        Entrada in = GSON.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    Salida logica(Entrada entrada) {
        Salida salida = new Salida();
        try {
            for (DetalleCargaMasiva carga0 : entrada.p_CargaMasiva) {
                if (carga0.CodigoTipoCargaMasiva.equals("CARGA_UNIVERSO_PAC")) {
                    ClaveUniverso universo = new ClaveUniverso();
                    universo.tipo = "UNI";
                    sql.lote(SQL_UPDATE_ESTADO_ARCHIVO_PAC, List.of(universo));
                    sql.lote(SQL_UPDATE_DESHABILITAR_M_PAC, List.of(universo));
                    // The legacy committed here (conn.commit()); under Spring the work commits when the call returns, and the failures
                    // below are swallowed exactly as before, so the observable result is the same (TES-B-218).

                    List<DetalleUniverso> lineas = new ArrayList<>();
                    long contador = CONTADOR_INICIAL;
                    for (DetalleUniverso carga : entrada.p_CargaMasivaDetalle) {
                        DetalleUniverso linea = new DetalleUniverso();
                        linea.IdCargaMasivaDetalle = contador;
                        linea.IdCargaMasivaPac = contador;
                        linea.CodigoBanco = carga.CodigoBanco;
                        linea.CodigoEmpresa = carga.CodigoEmpresa;
                        linea.CodigoConvenio = carga.CodigoConvenio;
                        linea.TipoRegistro = carga.TipoRegistro;
                        linea.IdentServicio = carga.IdentServicio.replace(" ", "");
                        linea.FechaGeneracion = carga.FechaGeneracion;
                        linea.FechaInicioServ = carga.FechaInicioServ;
                        lineas.add(linea);
                        contador++;
                    }
                    sql.lote(SQL_INSERT_UNIVERSO_PAC, lineas);

                    List<ClaveUniverso> universoActivo = sql.consultar(SQL_SELECT_UNIVERSO_PAC, Map.of(), ClaveUniverso.class);
                    List<ClaveUniverso> aReactivar = new ArrayList<>();
                    for (ClaveUniverso fila : universoActivo) {
                        ClaveUniverso c = new ClaveUniverso();
                        c.IdentServicio = fila.IdentServicio;
                        aReactivar.add(c);
                    }
                    sql.lote(SQL_UPDATE_UNIVERSO_PAC, aReactivar);
                }
            }
            salida.P_OUT_ERRORESCONTADOR = 0L;
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_PROCESO_EJECUTADO_EXITO", "Archivo Cargado con exito"));
        } catch (Exception e) {
            // LEGACY DEFECT kept (see README TES-B-218..220): every exception becomes a generic error message; earlier changes stay
            log.error("CrearCargaMasivaPac error", e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "Error", "Error en el sistema"));
        }
        return salida;
    }
}
