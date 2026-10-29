package cl.bcs.socionegocio.per;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaDetalleMasivo.EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaDetalleMasivo.SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaMasivo.EntradaCrearPersonaGrupoDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaMasivo.SalidaCrearPersonaGrupoDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.ValidarGrupoDireccionElectronicaDetalleMasivo.EntradaValidarGrupoDireccionElectronicaDetalleMasivo;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrearPersonaGrupoDireccionElectronicaConDetalle implements JavaCall {

    static final Logger logger = LogManager.getLogger(CrearPersonaGrupoDireccionElectronicaConDetalle.class);

    public static class EntradaCrearGrupoDireccionElectronicaConDetalle extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;

        public GrupoDireccionElectronica p_GrupoDireccionElectronica;

        public EntradaCrearGrupoDireccionElectronicaConDetalle() {
            p_GrupoDireccionElectronica = new GrupoDireccionElectronica();
        }
    }

    public static class SalidaCrearGrupoDireccionElectronicaConDetalle extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Entidad P_OUT_REFID;

        public SalidaCrearGrupoDireccionElectronicaConDetalle() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_REFID = new Entidad();
            this.P_OUT_MENSAJES = new ArrayList<>();
        }

        public void agregarValidaciones(List<Validacion> validaciones) {
            for (Validacion validacion : validaciones) {
                if (validacion.EsError) {
                    this.P_OUT_ERRORESCONTADOR += 1L;
                    this.P_OUT_MENSAJES.add(validacion.MensajeError);
                }
            }
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaCrearGrupoDireccionElectronicaConDetalle jin = gson.fromJson(in.toString(), EntradaCrearGrupoDireccionElectronicaConDetalle.class);

        SalidaCrearGrupoDireccionElectronicaConDetalle out = logica(conn, jin);
        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public SalidaCrearGrupoDireccionElectronicaConDetalle logica(Connection conn, EntradaCrearGrupoDireccionElectronicaConDetalle entrada) throws Exception {
        logger.info("INICIO::CrearPersonaGrupoDireccionElectronicaConDetalle");
        long inicio = System.currentTimeMillis();

        // Agregar ID dummy temporal para validar detalles duplicados en el grupo
        agregarIdGrupoDireccion(1L, entrada.p_GrupoDireccionElectronica.Detalles);

        SalidaCrearGrupoDireccionElectronicaConDetalle salida = new SalidaCrearGrupoDireccionElectronicaConDetalle();

        // Validar detalles
        ValidarGrupoDireccionElectronicaDetalleMasivo validarGrupoDireccionElectronicaDetalleMasivo = new ValidarGrupoDireccionElectronicaDetalleMasivo();
        EntradaValidarGrupoDireccionElectronicaDetalleMasivo entradaValidarGrupoDireccionElectronicaDetalleMasivo = new EntradaValidarGrupoDireccionElectronicaDetalleMasivo();
        entradaValidarGrupoDireccionElectronicaDetalleMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaValidarGrupoDireccionElectronicaDetalleMasivo.p_IdModulo = entrada.p_IdModulo;
        entradaValidarGrupoDireccionElectronicaDetalleMasivo.p_IdUsuario = entrada.p_IdUsuario;
        entradaValidarGrupoDireccionElectronicaDetalleMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
        entradaValidarGrupoDireccionElectronicaDetalleMasivo.p_Detalles = entrada.p_GrupoDireccionElectronica.Detalles;
        List<Validacion> validacionesDetalles = validarGrupoDireccionElectronicaDetalleMasivo.logica(conn, entradaValidarGrupoDireccionElectronicaDetalleMasivo);
        salida.agregarValidaciones(validacionesDetalles);

        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            return salida;
        }

        // Insertar Grupo
        CrearPersonaGrupoDireccionElectronicaMasivo crearPersonaGrupoDireccionElectronicaMasivo = new CrearPersonaGrupoDireccionElectronicaMasivo();
        EntradaCrearPersonaGrupoDireccionElectronicaMasivo entradaCrearPersonaGrupoDireccionElectronicaMasivo = new EntradaCrearPersonaGrupoDireccionElectronicaMasivo();
        entradaCrearPersonaGrupoDireccionElectronicaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaCrearPersonaGrupoDireccionElectronicaMasivo.p_IdModulo = entrada.p_IdModulo;
        entradaCrearPersonaGrupoDireccionElectronicaMasivo.p_IdUsuario = entrada.p_IdUsuario;
        entradaCrearPersonaGrupoDireccionElectronicaMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
        entradaCrearPersonaGrupoDireccionElectronicaMasivo.p_GruposDireccionesElectronicas = Collections.singletonList(entrada.p_GrupoDireccionElectronica);
        SalidaCrearPersonaGrupoDireccionElectronicaMasivo salidaCrearPersonaGrupoDireccionElectronicaMasivo = crearPersonaGrupoDireccionElectronicaMasivo.logica(conn, entradaCrearPersonaGrupoDireccionElectronicaMasivo);
        salida.P_OUT_ERRORESCONTADOR += salidaCrearPersonaGrupoDireccionElectronicaMasivo.P_OUT_ERRORESCONTADOR;

        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            salida.P_OUT_MENSAJES.addAll(salidaCrearPersonaGrupoDireccionElectronicaMasivo.P_OUT_MENSAJES);

            return salida;
        }

        salida.P_OUT_REFID = salidaCrearPersonaGrupoDireccionElectronicaMasivo.P_OUT_REFID.get(0);

        // reemplazar la ID dummy por la ID real ya insertada del GrupoDireccion
        agregarIdGrupoDireccion(salida.P_OUT_REFID.IdEntidad, entrada.p_GrupoDireccionElectronica.Detalles);

        // Insertar Detalles
        CrearPersonaGrupoDireccionElectronicaDetalleMasivo crearPersonaGrupoDireccionElectronicaDetalleMasivo = new CrearPersonaGrupoDireccionElectronicaDetalleMasivo();
        EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo = new EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo();
        entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.p_IdModulo = entrada.p_IdModulo;
        entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.p_IdUsuario = entrada.p_IdUsuario;
        entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
        entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.p_Detalles = entrada.p_GrupoDireccionElectronica.Detalles;
        SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo salidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo = crearPersonaGrupoDireccionElectronicaDetalleMasivo.logica(conn, entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo, false);
        salida.P_OUT_ERRORESCONTADOR += salidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.P_OUT_ERRORESCONTADOR;

        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            salida.P_OUT_MENSAJES.addAll(salidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.P_OUT_MENSAJES);

            return salida;
        }
        
        long fin = System.currentTimeMillis();
        logger.info("FIN::CrearPersonaGrupoDireccionElectronicaConDetalle", fin - inicio);

        return salida;
    }

    private void agregarIdGrupoDireccion(Long idGrupoDireccion, List<GrupoDireccionElectronica.Detalle> detalles) {
        logger.debug("INICIO::CrearPersonaGrupoDireccionElectronicaConDetalle.agregarIdGrupoDireccion");
        long inicio = System.currentTimeMillis();

        for (GrupoDireccionElectronica.Detalle detalle : detalles) {
            detalle.IdGrupoDireccion = idGrupoDireccion;
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::CrearPersonaGrupoDireccionElectronicaConDetalle.agregarIdGrupoDireccion", fin - inicio);
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String requestString = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_GrupoDireccionElectronica\":{\"IdInterno\":1,\"Grupo\":\"Prueba migracion DEV-120321-012\",\"Comentario\":\"Prueba migracion DEV-120321-012\",\"IdPersona\":621869,\"IdEstado\":\"ACT\",\"Detalles\":[{\"IdInterno\":2,\"IdDireccionElectronica\":588085,\"IdTipoDestinatario\":\"PARA\",\"IdEstado\":\"ACT\"},{\"IdInterno\":3,\"IdDireccionElectronica\":1942971,\"IdTipoDestinatario\":\"PARA\",\"IdEstado\":\"ACT\"},{\"IdInterno\":4,\"IdDireccionElectronica\":1943692,\"IdTipoDestinatario\":\"PARA\",\"IdEstado\":\"ACT\"}]}}";

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaCrearGrupoDireccionElectronicaConDetalle entrada = gson.fromJson(requestString, EntradaCrearGrupoDireccionElectronicaConDetalle.class);
            CrearPersonaGrupoDireccionElectronicaConDetalle crearPersonaGrupoDireccionElectronicaConDetalle = new CrearPersonaGrupoDireccionElectronicaConDetalle();

            try {
                SalidaCrearGrupoDireccionElectronicaConDetalle salida = crearPersonaGrupoDireccionElectronicaConDetalle.logica(conn, entrada);
                System.out.println(salida);
                conn.commit();

            } catch (Exception e) {
                e.printStackTrace();
                conn.rollback();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
