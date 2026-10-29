package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccion;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidarExistenciaBorradorGrupoDireccion implements JavaCall {
    static final Logger logger = LogManager.getLogger(ValidarExistenciaBorradorGrupoDireccion.class);

    private final String SQL_SELECT_GRUPO_DIRECCION = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaBorradorGrupoDireccion_SelectGrupoDireccion.sql");

    public static class EntradaValidarExistenciaBorradorGrupoDireccion extends BcsPojo {
        public Long p_IdEmpresa;
        public List<GrupoDireccion> p_GruposDirecciones;

        public EntradaValidarExistenciaBorradorGrupoDireccion() {
            this.p_GruposDirecciones = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorGrupoDireccion extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorGrupoDireccion() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorGrupoDireccion logica(Connection con, EntradaValidarExistenciaBorradorGrupoDireccion entrada) throws Exception {
        logger.info("INICIO::ValidarExistenciaBorradorGrupoDireccion");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorGrupoDireccion out = new SalidaValidarExistenciaBorradorGrupoDireccion();
        out.validaciones = validarExistenciaGruposDirecciones(con, entrada.p_GruposDirecciones, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarExistenciaBorradorGrupoDireccion", fin - inicio);

        return out;
    }

    private List<Validacion> validarExistenciaGruposDirecciones(Connection con, List<GrupoDireccion> gruposDirecciones, Long idEmpresa) throws Exception {
        logger.debug("INICIO::ValidarExistenciaBorradorGrupoDireccion.validarExistenciaGruposDirecciones");
        long inicio = System.currentTimeMillis();

        List<Long> idsGruposDirecciones = gruposDirecciones
                .parallelStream()
                .map(grupoDireccion -> grupoDireccion.IdGrupoDireccion)
                .collect(Collectors.toList());

        HashMap<String, Object> parametros = new HashMap<>(1);
        parametros.put("IdsGruposDirecciones", idsGruposDirecciones);

        SQL sql = new SQL();
        List<GrupoDireccion> gruposDireccionesExistentes = sql.query(con, SQL_SELECT_GRUPO_DIRECCION, parametros, GrupoDireccion.class);
        Map<Long, GrupoDireccion> gruposDireccionesPorId = gruposDireccionesExistentes
                .parallelStream()
                .collect(Collectors.toMap(grupoDireccion -> grupoDireccion.IdGrupoDireccion, grupoDireccion -> grupoDireccion));

        List<Validacion> validaciones = new ArrayList<>();

        for (GrupoDireccion grupoDireccion : gruposDirecciones) {
            if (gruposDireccionesPorId.get(grupoDireccion.IdGrupoDireccion) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;

                HashMap<String, String> datosMensajes = new HashMap<>(1);
                datosMensajes.put("GrupoDireccion", grupoDireccion.Grupo);

                validacion.MensajeError = new MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_GRUPO_DIRECCION_NO_EXISTE", datosMensajes, con, idEmpresa);
                validaciones.add(validacion);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarExistenciaBorradorGrupoDireccion.validarExistenciaGruposDirecciones", fin - inicio);

        return validaciones;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":1,\"p_UsaReglasNegocio\":1,\"p_CodigoFormulario\":1,\"p_IdTipoCanal\":1,\"p_GruposDirecciones\":[{\"IdEnrolamiento\":1,\"IdGrupoDireccion\":6,\"IdInterno\":1,\"Grupo\":\"Grupo de prueba MODIFICAR main\",\"Comentario\":\"Prueba MODIFICAR main\"},{\"IdEnrolamiento\":2,\"IdGrupoDireccion\":99,\"IdInterno\":2,\"Grupo\":\"Grupo 2 de prueba MODIFICAR main\",\"Comentario\":\"Prueba 2 main\"}]}";

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaValidarExistenciaBorradorGrupoDireccion entrada = gson.fromJson(jsonEntrada, EntradaValidarExistenciaBorradorGrupoDireccion.class);

            ValidarExistenciaBorradorGrupoDireccion validador = new ValidarExistenciaBorradorGrupoDireccion();
            try {
                SalidaValidarExistenciaBorradorGrupoDireccion salida = validador.logica(con, entrada);
                System.out.println(salida);
            } catch (Exception innerException) {
                innerException.printStackTrace();
                con.rollback();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
