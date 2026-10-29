package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.TarjetaBancaria;
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

public class ValidarExistenciaBorradorTarjetaBancaria implements JavaCall {

    static final Logger logger = LogManager.getLogger(ValidarExistenciaBorradorTarjetaBancaria.class);

    private final String SQL_SELECT_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaBorradorTarjetaBancaria_SelectTarjetaBancaria.sql");

    public static class EntradaValidarExistenciaBorradorTarjetaBancaria extends BcsPojo {
        public Long p_IdEmpresa;
        public List<TarjetaBancaria> p_TarjetasBancarias;

        public EntradaValidarExistenciaBorradorTarjetaBancaria() {
            this.p_TarjetasBancarias = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorTarjetaBancaria extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorTarjetaBancaria() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorTarjetaBancaria logica(Connection con, EntradaValidarExistenciaBorradorTarjetaBancaria entrada) throws Exception {
        logger.info("INICIO::ValidarExistenciaBorradorTarjetaBancaria");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorTarjetaBancaria out = new SalidaValidarExistenciaBorradorTarjetaBancaria();
        out.validaciones = validarExistenciaTarjetasBancarias(con, entrada.p_TarjetasBancarias, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarExistenciaBorradorTarjetaBancaria", fin - inicio);

        return out;
    }

    private List<Validacion> validarExistenciaTarjetasBancarias(Connection con, List<TarjetaBancaria> tarjetasBancarias, Long idEmpresa) throws Exception {
        logger.debug("INICIO::ValidarExistenciaBorradorTarjetaBancaria.validarExistenciaTarjetasBancarias");
        long inicio = System.currentTimeMillis();

        List<Long> idTarjetaBancaria = tarjetasBancarias.parallelStream().map(tarjetaBancaria -> tarjetaBancaria.IdTarjetaBancaria).collect(Collectors.toList());

        HashMap<String, Object> parametros = new HashMap<>(1);
        parametros.put("IdTarjetaBancaria", idTarjetaBancaria);

        SQL sql = new SQL();
        List<TarjetaBancaria> tarjetasBancariasExistentes = sql.query(con, SQL_SELECT_TARJETA_BANCARIA, parametros, TarjetaBancaria.class);
        Map<Long, TarjetaBancaria> tarjetasBancariasPorIdTarjeta = tarjetasBancariasExistentes.parallelStream().collect(Collectors.toMap(tarjetaBancaria -> tarjetaBancaria.IdTarjetaBancaria, tarjetaBancaria -> tarjetaBancaria));

        List<Validacion> validaciones = new ArrayList<>();

        for (TarjetaBancaria tarjetaBancaria : tarjetasBancarias) {
            if (tarjetasBancariasPorIdTarjeta.get(tarjetaBancaria.IdTarjetaBancaria) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                HashMap<String, String> datosMensajes = new HashMap<>(1);
                datosMensajes.put("NumeroTarjetaBancaria", tarjetaBancaria.NumeroTarjetaBancaria);
                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_TARJETA_BANCARIA_NO_EXISTE", datosMensajes, con, idEmpresa);

                validaciones.add(validacion);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarExistenciaBorradorTarjetaBancaria.validarExistenciaTarjetasBancarias", fin - inicio);

        return validaciones;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":1,\"p_UsaReglasNegocio\":1,\"p_CodigoFormulario\":1,\"p_IdTipoCanal\":1,\"p_TarjetasBancarias\":[{\"IdTarjetaBancaria\":-1, \"NumeroTarjetaBancaria\": \"123\"},{\"IdTarjetaBancaria\":-2, \"NumeroTarjetaBancaria\": \"321\"}]}";

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaValidarExistenciaBorradorTarjetaBancaria entrada = gson.fromJson(jsonEntrada, EntradaValidarExistenciaBorradorTarjetaBancaria.class);

            ValidarExistenciaBorradorTarjetaBancaria validador = new ValidarExistenciaBorradorTarjetaBancaria();
            try {
                SalidaValidarExistenciaBorradorTarjetaBancaria salida = validador.logica(con, entrada);
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
