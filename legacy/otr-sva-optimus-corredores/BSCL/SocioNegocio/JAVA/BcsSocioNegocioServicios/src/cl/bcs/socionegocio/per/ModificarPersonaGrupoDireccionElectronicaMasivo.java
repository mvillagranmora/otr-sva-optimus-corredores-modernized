package cl.bcs.socionegocio.per;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.RegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModificarPersonaGrupoDireccionElectronicaMasivo implements JavaCall, EjecutarCambiosPersona {

    static final Logger logger = LogManager.getLogger(ModificarPersonaGrupoDireccionElectronicaMasivo.class);

    private final String SQL_SELECT_GRUPO_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaGrupoDireccionElectronicaMasivo_SelectGrupoDireccionElectronica.sql");

    private final String ID_CONCEPTO = "MOGDIE";
    private final String ID_ESTADO_AUTORIZACION = "PEN";
    private final String ID_TIPO_ENTIDAD = "GRUPODIRECCIONELECTRONICA";

    public static class GrupoDireccionElectronica extends cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica {
        public String DocumentoIdentidad;
        public String NombreCompleto;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaEjecutarCambiosPersona.class);
        SalidaEjecutarCambiosPersona out = logica(conn, jin);
        JsonParser parser = new JsonParser();
        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    @Override
    public SalidaEjecutarCambiosPersona logica(Connection con, EntradaEjecutarCambiosPersona entrada) throws Exception {
        logger.info("INICIO::ModificarPersonaGrupoDireccionElectronicaMasivo");
        long inicioServicio = System.currentTimeMillis();
        SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();

        List<Long> idsSolicitudDeCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
        Long idSolicitudCambio = idsSolicitudDeCambio.get(0);

        List<GrupoDireccionElectronica> grupos = obtenerDatosGrupoDireccionElectronica(con, entrada);

        if (grupos.size() == 0) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ENTIDAD_NO_EXISTE", null, con, entrada.p_IdEmpresa));

            return out;
        }

        List<RegistroSolicitudCambio> registrosSolicitudesCambios = obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, grupos, entrada);

        if (entrada.p_RegistroCambio.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_REGISTRO_CAMBIO", null, con, entrada.p_IdEmpresa));

            return out;
        }

        CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
        EntradaCrearRegistroSolicitudCambio inCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();

        inCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;

        servicioCrearRegistroSolicitudCambio.logica(con, inCrearRegistroSolicitudCambio);

        Boolean requiereAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);

        if (requiereAutorizacion) {
            out = crearEventoAutorizacion(con, entrada, grupos);
        } else {
            List<InEntidad> entidades = obtenerListaPersonaEntidad(grupos);
            ConfirmarCambiosPersonaEntidad confirmarCambiosEntidad = new ConfirmarCambiosPersonaEntidad();
            out = confirmarCambiosEntidad.logica(con, entrada, entidades, new ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo());
        }

        out.P_OUT_REFID = idSolicitudCambio;

        long finServicio = System.currentTimeMillis();
        logger.info("FIN::ModificarPersonaGrupoDireccionElectronicaMasivo", finServicio - inicioServicio);
        return out;
    }

    private List<InEntidad> obtenerListaPersonaEntidad(List<GrupoDireccionElectronica> grupos) {
        logger.trace("INICIO::ModificarPersonaGrupoDireccionElectronicaMasivo.obtenerListaPersonaEntidad");
        long inicio = System.currentTimeMillis();

        List<InEntidad> personaEntidades = grupos
                .stream()
                .map(entidad -> new InEntidad(entidad.IdPersona, entidad.IdGrupoDireccion))
                .distinct()
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ModificarPersonaGrupoDireccionElectronicaMasivo.obtenerListaPersonaEntidad", fin - inicio);

        return personaEntidades;
    }

    private List<GrupoDireccionElectronica> obtenerDatosGrupoDireccionElectronica(Connection con, EntradaEjecutarCambiosPersona entrada) throws Exception {
        logger.debug("INICIO::ModificarPersonaGrupoDireccionElectronicaMasivo.obtenerDatosGrupoDireccionElectronica");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> params = new HashMap<>();

        List<Long> idEntidades = entrada.p_RegistroCambio.stream()
                .map(entidad -> entidad.IdEntidad)
                .distinct()
                .collect(Collectors.toList());
        params.put("IdGrupoDireccion", idEntidades);
        List<GrupoDireccionElectronica> grupos = sql.query(con, SQL_SELECT_GRUPO_DIRECCION_ELECTRONICA, params, GrupoDireccionElectronica.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ModificarPersonaGrupoDireccionElectronicaMasivo.obtenerDatosGrupoDireccionElectronica", fin - inicio);

        return grupos;
    }

    private SalidaEjecutarCambiosPersona crearEventoAutorizacion(Connection con, EntradaEjecutarCambiosPersona entrada, List<GrupoDireccionElectronica> entidades) throws Exception {
        logger.debug("INICIO::ModificarPersonaGrupoDireccionElectronicaMasivo.crearEventoAutorizacion");
        long inicio = System.currentTimeMillis();

        CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();

        for (GrupoDireccionElectronica entidad : entidades) {
            CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
            evento.IdTipoEvento = ID_CONCEPTO;
            evento.IdModuloSolicita = entrada.p_IdModulo;
            evento.IdUsuarioSolicita = entrada.p_IdUsuario;
            evento.Fecha = fechaSistema;
            evento.IdConcepto = ID_CONCEPTO;
            evento.NumeroOperacionConcepto = String.valueOf(entidad.IdGrupoDireccion);
            evento.FechaOperacionConcepto = fechaSistema;
            evento.IdCliente = entidad.IdPersona;
            evento.Glosa = entidad.DocumentoIdentidad + " / " + entidad.NombreCompleto + " / IdGrupo: " + entidad.IdGrupoDireccion;
            entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);

        }

        entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
        entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
        entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
        entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
        entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";

        CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);

        SalidaEjecutarCambiosPersona salidaModificarPersonaDireccionFisica = new SalidaEjecutarCambiosPersona();
        salidaModificarPersonaDireccionFisica.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;

        List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
        List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());

        salidaModificarPersonaDireccionFisica.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
        salidaModificarPersonaDireccionFisica.P_OUT_MENSAJES.addAll(mensajesErrores);

        if (salidaModificarPersonaDireccionFisica.P_OUT_ERRORESCONTADOR.equals(0L)) {
            salidaModificarPersonaDireccionFisica.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<>(), con, entrada.p_IdEmpresa));
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ModificarPersonaGrupoDireccionElectronicaMasivo.crearEventoAutorizacion", fin - inicio);
        return salidaModificarPersonaDireccionFisica;
    }

    private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<GrupoDireccionElectronica> entidades, EntradaEjecutarCambiosPersona entrada) throws Exception {
        logger.trace("INICIO::ModificarPersonaGrupoDireccionElectronicaMasivo.obtenerRegistroSolicitudesCambio");
        long inicio = System.currentTimeMillis();
        List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<>();

        Map<Long, GrupoDireccionElectronica> entidadesPorId = entidades
                .parallelStream()
                .collect(Collectors.toMap(entidad -> entidad.IdGrupoDireccion, entidad -> entidad));

        for (RegistroCambioMasivo registroCambio : entrada.p_RegistroCambio) {
            GrupoDireccionElectronica entidadesId = entidadesPorId.get(registroCambio.IdEntidad);
            
            if( entidadesId != null) {
            	RegistroSolicitudCambio registroSolicitudCambio = new RegistroSolicitudCambio();
                registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
                registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
                registroSolicitudCambio.IdEntidadOrigen = String.valueOf(registroCambio.IdEntidad);
                registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;

                ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(entidadesId, GrupoDireccionElectronica.class, registroCambio.CampoModificado);

                registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
                registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
                registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
                registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;

                registroSolicitudesCambio.add(registroSolicitudCambio);
            }
            
        }
        long fin = System.currentTimeMillis();
        logger.trace("FIN::ModificarPersonaGrupoDireccionElectronicaMasivo.obtenerRegistroSolicitudesCambio", fin - inicio);
        return registroSolicitudesCambio;
    }

    public static void main(String[] args) throws SQLException {
        System.out.println("== TEST ==");

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
            String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_RegistroCambio\":[{\"CampoModificado\":\"Grupo\",\"ValorNuevo\":\"Prueba migracion DEV-120321-005 MOD-002\",\"IdEntidad\":10023},{\"CampoModificado\":\"Comentario\",\"ValorNuevo\":\"Prueba migracion DEV-120321-005 MOD-002\",\"IdEntidad\":10023},{\"CampoModificado\":\"IdEstado\",\"ValorNuevo\":\"BLO\",\"IdEntidad\":10023},{\"CampoModificado\":\"IdEstadoAutorizacion\",\"ValorNuevo\":\"ACT\",\"IdEntidad\":10023}]}";
            EntradaEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaEjecutarCambiosPersona.class);

            ModificarPersonaGrupoDireccionElectronicaMasivo demo = new ModificarPersonaGrupoDireccionElectronicaMasivo();
            System.out.println(Objin);

            try {
                SalidaEjecutarCambiosPersona ObjOut = demo.logica(con, Objin);
                System.out.println(ObjOut.toString());
                con.commit();

            } catch (Exception er) {
                er.printStackTrace();

            } finally {
                con.rollback();

            }
        }
        System.out.println("== END ==");
    }
}
