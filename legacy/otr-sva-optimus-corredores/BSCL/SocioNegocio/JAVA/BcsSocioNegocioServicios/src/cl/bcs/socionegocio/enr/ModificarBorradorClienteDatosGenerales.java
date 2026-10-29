package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorEnrolamiento.EntradaValidarExistenciaBorradorEnrolamiento;
import cl.bcs.socionegocio.enr.pojo.ClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.Enrolamiento;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModificarBorradorClienteDatosGenerales implements JavaCall {

    static final Logger logger = LogManager.getLogger(ModificarBorradorClienteDatosGenerales.class);

    private final String SQL_SELECT_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaBorradorEnrolamiento_SelectEnrolamiento.sql");
    private final String SQL_UPDATE_CLIENTE_DATOS_GENERALES = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorClienteDatosGenerales_UpdateClienteDatosGenerales.sql");

    public static class EntradaModificarBorradorClienteDatosGenerales extends EntradaAdministrarBorrador {
        public List<ClienteDatosGenerales> p_ClientesDatosGenerales;

        public EntradaModificarBorradorClienteDatosGenerales() {
            this.p_ClientesDatosGenerales = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaModificarBorradorClienteDatosGenerales jin = gson.fromJson(in.toString(), EntradaModificarBorradorClienteDatosGenerales.class);
        SalidaAdministrarBorrador out = logica(conn, jin);

        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorClienteDatosGenerales entrada) throws Exception {
        logger.info("INICIO::ModificarBorradorClienteDatosGenerales");
        long inicioServicio = System.currentTimeMillis();
        SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

        prepararDatosGeneralesEntrantes(entrada);

        out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_ClientesDatosGenerales, ClienteDatosGenerales.class, out);
        out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_ClientesDatosGenerales, "ClienteDatosGenerales", out);

        if (out.P_OUT_ERRORESCONTADOR > 0) {
            return out;
        }

        EntradaValidarExistenciaBorradorEnrolamiento entradaValidarExistenciaBorradorEnrolamiento = new EntradaValidarExistenciaBorradorEnrolamiento();
        entradaValidarExistenciaBorradorEnrolamiento.p_Enrolamientos = entrada.p_ClientesDatosGenerales.stream().map(datos -> {
            Enrolamiento enrolamiento = new Enrolamiento();
            enrolamiento.IdEnrolamiento = datos.IdEnrolamiento;

            return enrolamiento;
        }).collect(Collectors.toList());

        ValidarExistenciaBorradorEnrolamiento validarExistenciaBorradorEnrolamiento = new ValidarExistenciaBorradorEnrolamiento();
        List<Validacion> validaciones = validarExistenciaBorradorEnrolamiento.logica(con, entradaValidarExistenciaBorradorEnrolamiento).validaciones;
        out.agregarValidacionesSalida(validaciones);

        if (out.P_OUT_ERRORESCONTADOR > 0) {
            return out;
        }

        actualizarDatosGeneralesClientes(con, entrada.p_ClientesDatosGenerales);

        out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_ClientesDatosGenerales);

        long finServicio = System.currentTimeMillis();
        logger.info("FIN::ModificarBorradorClienteDatosGenerales", finServicio - inicioServicio);

        return out;
    }

    private void prepararDatosGeneralesEntrantes(EntradaModificarBorradorClienteDatosGenerales entrada) {
        logger.trace("INICIO::ModificarBorradorClienteDatosGenerales.prepararDatosGeneralesEntrantes");
        long inicio = System.currentTimeMillis();

        for (ClienteDatosGenerales datosGenerales : entrada.p_ClientesDatosGenerales) {
            datosGenerales.EsBorrador = "S";
            datosGenerales.CLI_IdTipoCliente = datosGenerales.IdTipoCliente;
            datosGenerales.CLI_IdEmpleadoEjecutivo = datosGenerales.IdEmpleadoEjecutivo;
            datosGenerales.CLI_IdCategoriaInversionista = datosGenerales.IdCategoriaInversionista;
            datosGenerales.CLI_IdSegmentoCliente = datosGenerales.IdSegmentoCliente;
            datosGenerales.CLI_AreaCliente = datosGenerales.CodigoArea;
        }

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ModificarBorradorClienteDatosGenerales.prepararDatosGeneralesEntrantes {}", fin - inicio);
    }

    private void actualizarDatosGeneralesClientes(Connection con, List<ClienteDatosGenerales> datosGeneralesClientes) throws Exception {
        logger.debug("INICIO::ModificarBorradorClienteDatosGenerales.actualizarDatosGeneralesClientes");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(con, SQL_UPDATE_CLIENTE_DATOS_GENERALES, datosGeneralesClientes, ClienteDatosGenerales.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ModificarBorradorClienteDatosGenerales.actualizarDatosGeneralesClientes", fin - inicio);
    }

    private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<ClienteDatosGenerales> datosGeneralesClientes) {
        logger.trace("INICIO::ModificarBorradorClienteDatosGenerales.obtenerIdsActualizados");
        long inicio = System.currentTimeMillis();

        List<ResultadoAdministrarBorradorEntidad> idsInsertados = datosGeneralesClientes
                .parallelStream()
                .map(datosGenerales -> {
                    ResultadoAdministrarBorradorEntidad resultado = new ResultadoAdministrarBorradorEntidad();
                    resultado.IdInterno = datosGenerales.IdInterno;
                    resultado.IdEntidad = datosGenerales.IdEnrolamiento;

                    return resultado;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ModificarBorradorClienteDatosGenerales.obtenerIdsActualizados", fin - inicio);

        return idsInsertados;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String requestBody = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"DATOS_GENERALES_CLIENTE_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\",\"p_ClientesDatosGenerales\":[{\"IdEnrolamiento\":456,\"IdTipoCliente\":\"RETAIL\",\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdSegmentoCliente\":1,\"IdEmpleadoEjecutivo\":11496,\"CodigoEjecutivoExterno\":\"ASDASD\",\"IdSucursalPreferencia\":1,\"EsClienteFinal\":\"N\",\"CodigoArea\":\"ADMCAR\"}]}\n";

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaModificarBorradorClienteDatosGenerales entrada = gson.fromJson(requestBody, EntradaModificarBorradorClienteDatosGenerales.class);

            ModificarBorradorClienteDatosGenerales modificarBorradorClienteDatosGenerales = new ModificarBorradorClienteDatosGenerales();
            try {
                SalidaAdministrarBorrador salida = modificarBorradorClienteDatosGenerales.logica(con, entrada);
                if (salida.P_OUT_ERRORESCONTADOR > 0) {
                    con.rollback();
                }

                System.out.println(salida);
            } catch (Exception innerException) {
                innerException.printStackTrace();
            } finally {
                con.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }

}
