package cl.bcs.socionegocio.per;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.RegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.EntidadLogGenerarContrato;
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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EliminarPersonaGrupoDireccionElectronicaMasivo implements JavaCall, EjecutarCambiosPersona {

    static final Logger logger = LogManager.getLogger(EliminarPersonaGrupoDireccionElectronicaMasivo.class);

    private final String SQL_SELECT_GRUPO_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaGrupoDireccionElectronicaMasivo_SelectGrupoDireccionElectronica.sql");
    private String SQL_SELECT_GRUPOS_DIREC_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaGrupoDireccionElectronicaMasivo_SelectGrupoDireccionElectronicaPorIdPersona.sql");
    public  String SQL_INSERT_LOG_GENERAR_DOCUMENTOS		= loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarClienteDatosGenerales_InsertLogGenerarContratos.sql");
    
    private final String ID_CONCEPTO = "BORGDE";
    private final String ID_ESTADO_AUTORIZACION = "PEN";
    private final String ID_TIPO_ENTIDAD = "GRUPODIRECCION-BORRAR";
	private final String CODIGO_CAMPO 			= "INGRESAR_GRUPOS_DIRECCIONES_ELECTRONICAS_TABLA";

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
        logger.info("INICIO::EliminarPersonaGrupoDireccionElectronicaMasivo");
        long inicioServicio = System.currentTimeMillis();
        SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();

        List<Long> idsSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
        Long idSolicitudCambio = idsSolicitudCambio.get(0);

        ObtenerDatosCambiosEntidadPersonaEliminar obtenerDatosCambiosPersona = new ObtenerDatosCambiosEntidadPersonaEliminar();
        List<EntidadPersonaEliminar> entidadesPersona = obtenerDatosCambiosPersona.logica(con, entrada, SQL_SELECT_GRUPO_DIRECCION_ELECTRONICA);
        HashMap<String, String> parametrosMensaje = new HashMap<>(2);
        
        if (entidadesPersona.size() == 0) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ENTIDAD_NO_EXISTE", parametrosMensaje, con, entrada.p_IdEmpresa));
            return out;
        }

        Long idPersona = entidadesPersona.stream().findFirst().get().IdPersona;
        List<EntidadPersonaEliminar> registros = obtenerGrupoDireccionElectronicas(con , idPersona);
        EliminarEntidadPersona eliminarEntidadPersona = new EliminarEntidadPersona();
		
		if(eliminarEntidadPersona.alMenosUnRegistro(con, entidadesPersona, registros, idPersona, CODIGO_CAMPO, entrada.p_IdTipoCanal)) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_GRUPO_DIRECCION_ELECTRONICA_REQUERIDO",parametrosMensaje,con,entrada.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_MENSAJES.stream().filter(mensaje -> mensaje.tipo.equals(MensajeSalida.MENSAJE_ERROR)).count();
			out.P_OUT_REFID = idPersona;
			return out;
		}
		
        List<RegistroSolicitudCambio> registrosSolicitudesCambios = obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, entidadesPersona, entrada);

        if (entrada.p_RegistroCambio.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_REGISTRO_CAMBIO", parametrosMensaje, con, entrada.p_IdEmpresa));
            return out;
        }

        CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
        EntradaCrearRegistroSolicitudCambio inCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();

        inCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;
        servicioCrearRegistroSolicitudCambio.logica(con, inCrearRegistroSolicitudCambio);

        String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO);

        if (requiereAutorizacion.equals("S")) {
            out = crearEventoAutorizacion(con, entrada, entidadesPersona);
        } else {
            List<InEntidad> inEntidades = obtenerDatosCambiosPersona.obtenerListaPersonaEntidad(entidadesPersona);
            ConfirmarCambiosPersonaEntidad confirmarCambiosEntidad = new ConfirmarCambiosPersonaEntidad();
            out = confirmarCambiosEntidad.logica(con, entrada, inEntidades, new ConfirmarEliminarPersonaGrupoDireccionElectronicaMasivo());
            
            List<EntidadLogGenerarContrato> params = new ArrayList<EntidadLogGenerarContrato>();	
			List<Long> idSecuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", 1L);
			if(out.P_OUT_ERRORESCONTADOR==0) {
				
				EntidadLogGenerarContrato logGenerarContrato = new EntidadLogGenerarContrato();
				logGenerarContrato.Idlog = idSecuencias.get(0).longValue();
				logGenerarContrato.CodigoFormulario = "GRUPO_DIRECCION_ELECTRONICA_";
				logGenerarContrato.CampoBackend = entrada.p_RegistroCambio.get(0).CampoModificado;
				logGenerarContrato.IdEmpresa = entrada.p_IdEmpresa;
				logGenerarContrato.IdCliente = 0L;
				logGenerarContrato.IdPersona = entrada.p_IdPersona;
				logGenerarContrato.IdUsuario = entrada.p_IdUsuario;
				logGenerarContrato.FechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
				logGenerarContrato.IdPrograma = 0L;
				logGenerarContrato.Estado = "PEN";
				logGenerarContrato.Mensaje = "Persona - EliminarPersonaGrupoDireccionElectronicaMasivo.java";
				params.add(logGenerarContrato);
				
				SQL sql = new SQL();
				sql.batch(con, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, params,EntidadLogGenerarContrato.class);
			}
        }

        out.P_OUT_REFID = idSolicitudCambio;

        long finServicio = System.currentTimeMillis();
        logger.info("FIN::EliminarPersonaGrupoDireccionElectronicaMasivo", finServicio - inicioServicio);
        return out;
    }
    
    private List<EntidadPersonaEliminar> obtenerGrupoDireccionElectronicas(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::EliminarPersonaGrupoDireccionElectronicaMasivo.obtenerGrupoDireccionElectronicas");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		List<Long> idEntidades = new ArrayList<Long>();
		idEntidades.add(idPersona);
		params.put("IdEntidad", idEntidades);
		List<EntidadPersonaEliminar> lstEntidad = sql.query(con, SQL_SELECT_GRUPOS_DIREC_ID_PERSONA, params, EntidadPersonaEliminar.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarPersonaGrupoDireccionElectronicaMasivo.obtenerGrupoDireccionElectronicas", finServicio - inicio);
		return lstEntidad;
	}
    
    private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<EntidadPersonaEliminar> entidadesPersonaEliminar, EntradaEjecutarCambiosPersona entrada) throws Exception {
        logger.trace("INICIO::EliminarPersonaGrupoDireccionElectronicaMasivo.obtenerRegistroSolicitudesCambio");
        long inicio = System.currentTimeMillis();
        List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<>();

        Map<Long, EntidadPersonaEliminar> entidadesPorId = entidadesPersonaEliminar
                .parallelStream()
                .collect(Collectors.toMap(entidad -> entidad.IdEntidadPersonaEliminar, entidad -> entidad));

        for (RegistroCambioMasivo registroCambio : entrada.p_RegistroCambio) {
            EntidadPersonaEliminar entidadPersonaEliminar = entidadesPorId.get(registroCambio.IdEntidad);
            RegistroSolicitudCambio registroSolicitudCambio = new RegistroSolicitudCambio();
            registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
            registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
            registroSolicitudCambio.IdEntidadOrigen = String.valueOf(registroCambio.IdEntidad);
            registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado; // "Borrado"

            ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(entidadPersonaEliminar, EntidadPersonaEliminar.class, registroCambio.CampoModificado);
            registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
            registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
            registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
            registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;

            registroSolicitudesCambio.add(registroSolicitudCambio);
        }
        long fin = System.currentTimeMillis();
        logger.trace("FIN::EliminarPersonaGrupoDireccionElectronicaMasivo.obtenerRegistroSolicitudesCambio", fin - inicio);

        return registroSolicitudesCambio;
    }

    private SalidaEjecutarCambiosPersona crearEventoAutorizacion(Connection con, EntradaEjecutarCambiosPersona entrada, List<EntidadPersonaEliminar> entidadPersonaEliminar) throws Exception {
        logger.debug("INICIO::EliminarPersonaGrupoDireccionElectronicaMasivo.crearEventoAutorizacion");
        long inicio = System.currentTimeMillis();

        CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();

        for (EntidadPersonaEliminar entidad : entidadPersonaEliminar) {
            CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
            evento.IdTipoEvento = ID_CONCEPTO;
            evento.IdModuloSolicita = entrada.p_IdModulo;
            evento.IdUsuarioSolicita = entrada.p_IdUsuario;
            evento.Fecha = fechaSistema;
            evento.IdConcepto = ID_CONCEPTO;
            evento.NumeroOperacionConcepto = String.valueOf(entidad.IdEntidadPersonaEliminar);
            evento.FechaOperacionConcepto = fechaSistema;
            evento.IdCliente = entidad.IdPersona;
            evento.Glosa = entidad.DocumentoIdentidad + " / " + entidad.NombreCompleto + " / " + " / IdGrupoDirEle: " + entidad.IdEntidadPersonaEliminar;
            entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);
        }

        entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
        entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
        entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
        entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
        entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";

        CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);

        SalidaEjecutarCambiosPersona salidaEliminarPersonaDireccionFisicaMasivo = new SalidaEjecutarCambiosPersona();
        salidaEliminarPersonaDireccionFisicaMasivo.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;

        List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
        List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());

        salidaEliminarPersonaDireccionFisicaMasivo.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
        salidaEliminarPersonaDireccionFisicaMasivo.P_OUT_MENSAJES.addAll(mensajesErrores);

        if (salidaEliminarPersonaDireccionFisicaMasivo.P_OUT_ERRORESCONTADOR.equals(0L)) {
            salidaEliminarPersonaDireccionFisicaMasivo.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::EliminarPersonaGrupoDireccionElectronicaMasivo.crearEventoAutorizacion", fin - inicio);
        return salidaEliminarPersonaDireccionFisicaMasivo;
    }


    public static void main(String[] args) throws SQLException {
        System.out.println("== TEST ==");

        try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")){
            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
            String json = "{\r\n"
            		+ "    \"p_IdEmpresa\": 1,\r\n"
            		+ "    \"p_IdUsuario\": 76767,\r\n"
            		+ "    \"p_IdModulo\": \"SOC\",\r\n"
            		+ "    \"p_IdPersona\": 47704,\r\n"
            		+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
            		+ "    \"p_IdTipoCanal\": \"OPT\",\r\n"
            		+ "    \"p_RegistroCambio\": [\r\n"
            		+ "        {\r\n"
            		+ "            \"CampoModificado\": \"Borrado\",\r\n"
            		+ "            \"ValorNuevo\": \"S\",\r\n"
            		+ "            \"IdEntidad\": 128167\r\n"
            		+ "        }\r\n"
            		+ "    ]\r\n"
            		+ "}";
            EntradaEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaEjecutarCambiosPersona.class);

            EliminarPersonaGrupoDireccionElectronicaMasivo demo = new EliminarPersonaGrupoDireccionElectronicaMasivo();
            System.out.println(Objin);

            try {
                SalidaEjecutarCambiosPersona ObjOut = demo.logica(con, Objin);
                con.commit();
                System.out.println(ObjOut);
            } catch (Exception er) {
                er.printStackTrace();
            } finally {
                con.rollback();
            }
        } catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("== END ==");
    }
}