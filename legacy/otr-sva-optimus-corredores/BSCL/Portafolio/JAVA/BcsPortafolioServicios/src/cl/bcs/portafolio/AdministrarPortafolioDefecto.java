package cl.bcs.portafolio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.portafolio.pojo.PortafolioMantenedor;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AdministrarPortafolioDefecto implements JavaCall {

    static final Logger logger = LogManager.getLogger(AdministrarPortafolioDefecto.class);

    

    private final String SQL_INSERT_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/AdministrarPortafolioDefecto_InsertPortafolio.sql");
    private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/AdministrarPortafolioDefecto_SelectPortafolio.sql");
    private final String SQL_SELECT_PORTAFOLIO_ALL = loadResourceToString("cl/bcs/portafolio/sql/AdministrarPortafolioDefecto_SelectPortafolioAll.sql");
    private final String SQL_DELETE_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/AdministrarPortafolioDefecto_DeletePortafolio.sql");

    private final static String ID_CONCEPTO = "ADMPOR";

    public class EntradaAdministrarPortafolioDefecto extends BcsPojo{
        public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_Opcion;
        
        public List<PortafolioMantenedor> p_Portafolios;

        public EntradaAdministrarPortafolioDefecto(){
            this.p_Portafolios = new ArrayList<>();
        }
    }

    public static class SalidaAdministrarPortafolioDefecto extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<Entidad> P_OUT_REFID;
        public List<MensajeSalida> P_OUT_MENSAJES;

        public SalidaAdministrarPortafolioDefecto() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_REFID = new ArrayList<>();
            this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
        }
    }

    // LOGICA: INCLUYE INSERTAR Y ELIMINAR
    public SalidaAdministrarPortafolioDefecto logica(Connection conn, EntradaAdministrarPortafolioDefecto entrada) throws Exception {
        logger.info("INICIO::AdministrarPortafolioDefecto");
        long inicio = System.currentTimeMillis();

        SalidaAdministrarPortafolioDefecto out = new SalidaAdministrarPortafolioDefecto();
        
        Boolean requiereAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(conn, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
        List<PortafolioMantenedor> portafolios = obtenerPorfatoliosMantenedor(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_Portafolios);
        
        
        if(entrada.p_Opcion.equals("INSERT")) {
        	
        	if(entrada.p_Portafolios.stream().anyMatch(portafolio -> !portafolio.Proposito.equalsIgnoreCase("PROPOSITOAPV"))) {        		
        		
        		String maximoCodigoPortafolio = "";
    			
    			try {
    				maximoCodigoPortafolio = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "SOCIODENEGOCIO", "POR.MaximoCodigoPortafolio");
    			} catch (Exception e) {
    				logger.warn("No se pudo obtener el parametro POR.MaximoCodigoPortafolio. Usando valor por defecto vacio.", e);
    			}
        		if (!maximoCodigoPortafolio.isEmpty()) {
        			for (PortafolioMantenedor portafolio: entrada.p_Portafolios) {
                		if (!portafolio.Proposito.equalsIgnoreCase("PROPOSITOAPV")) {
                			Pattern regexCodigoPortafolio = Pattern.compile(maximoCodigoPortafolio);
            			    Matcher matchCodigoPortafolio = regexCodigoPortafolio.matcher(portafolio.CodigoPortafolio);
            			    if (!matchCodigoPortafolio.find()) {
            				    logger.debug("CODIGO PORTAFOLIO NO CUMPLE CON PARAMETRO POR.MaximoCodigoPortafolio");    
            				    String codigoError = "CODIGO_PORTAFOLIO_NO_VALIDO";
            				    MensajeSalida error = new MensajeSalida(4290672L, "ERROR", codigoError, Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigoError).msg());
            				    out.P_OUT_MENSAJES.add(error);
            				    out.P_OUT_ERRORESCONTADOR++;
            			    }
                		}
                	}
        			
        			if(out.P_OUT_ERRORESCONTADOR > 0) {
        				return out;
        			}
				    
        		}
        	}        	
        	
        	out = instertarPorfatoliosMantenedor(conn, portafolios, entrada, out);
        } else if (entrada.p_Opcion.equals("DELETE")) {
        	out = eliminarPortafolio(conn, entrada, out);
        }
        
        
        if (requiereAutorizacion) {
            out = crearEventoAutorizacion(conn, entrada, portafolios, out);
        } else {
            agregarLog(conn, entrada, portafolios);
        }


        out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
        
        long fin = System.currentTimeMillis();
        logger.info("FIN::AdministrarPortafolioDefecto", fin - inicio);

        return out;
    }
    

    private List<PortafolioMantenedor> obtenerPorfatoliosMantenedor(Connection conn, Long idEmpresa, Long idUsuario, List<PortafolioMantenedor> portafoliosEntrada) throws Exception {
        logger.info("INICIO::AdministrarPortafolioDefecto.obtenerPorfatoliosMantenedor");
        long inicio = System.currentTimeMillis();
        SQL sql = new SQL();
        
        List<Long> secuenciaPortafolio = Utils.getSecuencia(conn, "BSCL.Portafolio.BD.Secuencias::PortafolioConfiguracionDefecto", Long.valueOf(portafoliosEntrada.size()));

        AtomicInteger index = new AtomicInteger(0);

        List<PortafolioMantenedor> portafolios = portafoliosEntrada.parallelStream().map(peticionPortafolio -> {
            Long IdPortafolioConfiguracionDefecto = secuenciaPortafolio.get(index.getAndIncrement());

            return obtenerEntidadPorfatolioMantenedor(peticionPortafolio, idUsuario, IdPortafolioConfiguracionDefecto);
        }).collect(Collectors.toList());
        
        HashMap<String, Object> params = new HashMap<String,Object>();
        
        

		logger.debug("INICIO::SQL_SELECT_PORTAFOLIO_ALL");
		portafolios = sql.query(conn, SQL_SELECT_PORTAFOLIO_ALL, params, PortafolioMantenedor.class);
		logger.debug("FIN::SQL_SELECT_PORTAFOLIO_ALL");
        

        long fin = System.currentTimeMillis();
        logger.info("FIN::AdministrarPortafolioDefecto.obtenerPorfatoliosMantenedor", fin - inicio);

        return portafolios;
        
    }

    private PortafolioMantenedor obtenerEntidadPorfatolioMantenedor(PortafolioMantenedor peticionPortafolio, Long idUsuario, Long idCuentaCustodioPortafolio) {
        logger.info("INICIO::AdministrarPortafolioDefecto.obtenerEntidadPorfatolioMantenedor");
        long inicio = System.currentTimeMillis();

        PortafolioMantenedor portafolio = new PortafolioMantenedor();
        portafolio.IdPortafolioConfiguracionDefecto = peticionPortafolio.IdPortafolioConfiguracionDefecto;
        portafolio.CodigoPortafolio = peticionPortafolio.CodigoPortafolio;
        portafolio.Proposito = peticionPortafolio.Proposito;



        long fin = System.currentTimeMillis();
        logger.info("FIN::AdministrarPortafolioDefecto.obtenerEntidadPorfatolioMantenedor", fin - inicio);

        return portafolio;
    }

    private List<Entidad> obtenerIdsInsertadas(List<PortafolioMantenedor> portafolios) {
        logger.trace("INICIO::AdministrarPortafolioDefecto.obtenerIdsPortafolios");
        long inicio = System.currentTimeMillis();
        AtomicLong indice = new AtomicLong();
        List<Entidad> ids = portafolios
                .stream()
                .map(portafolio -> {
                    Entidad entidad = new Entidad();
                    entidad.IdEntidad = portafolio.IdPortafolioConfiguracionDefecto;
                    entidad.IdInterno = indice.getAndIncrement();

                    return entidad;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::AdministrarPortafolioDefecto.obtenerIdsPortafolios", fin - inicio);

        return ids;
    }
    
    
    // INSERCION DE PORTAFOLIOS
    private SalidaAdministrarPortafolioDefecto instertarPorfatoliosMantenedor(Connection conn, List<PortafolioMantenedor> portafolios, EntradaAdministrarPortafolioDefecto entrada, SalidaAdministrarPortafolioDefecto salida) throws Exception {
        logger.debug("INICIO::AdministrarPortafolioDefecto.instertarPorfatoliosMantenedor");
        long inicio = System.currentTimeMillis();
        List<PortafolioMantenedor> portafolioNuevos = new ArrayList<>();
        SQL sql = new SQL();
        
		HashMap<String, Object> params 	= new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);

        List<Long> secuenciaPortafolio = Utils.getSecuencia(conn, "BSCL.Portafolio.BD.Secuencias::PortafolioConfiguracionDefecto", Long.valueOf(entrada.p_Portafolios.size()));
        
        AtomicInteger indice = new  AtomicInteger();

		for (PortafolioMantenedor portEntrada : entrada.p_Portafolios) {

			portEntrada.IdPortafolioConfiguracionDefecto = secuenciaPortafolio.get(indice.get());

			boolean existe = portafolios.stream().anyMatch(p -> p.CodigoPortafolio.equals(portEntrada.CodigoPortafolio));

			portEntrada.IdInterno = (long) indice.getAndIncrement();

			if (existe) {
				HashMap<String, String> paramsMensaje = new HashMap<String, String>();
				paramsMensaje.put("CodigoPortafolio", portEntrada.CodigoPortafolio);
				logger.error("PORTAFOLIO EXISTE", ID_CONCEPTO, sql, sql, portEntrada, paramsMensaje, conn, portafolios, entrada, sql);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(portEntrada.IdInterno, MensajeSalida.MENSAJE_ERROR, "CODIGO_PORTAFOLIO_YA_EXISTE", paramsMensaje, conn, entrada.p_IdEmpresa));
			} else {
				portafolioNuevos.add(portEntrada);
			}

		}
        
        if(!portafolioNuevos.isEmpty()) {
        	try {
        		logger.debug("INICIO::SQL_INSERT_PORTAFOLIO");
        		sql.batch(conn, SQL_INSERT_PORTAFOLIO, portafolioNuevos, PortafolioMantenedor.class);
        		logger.debug("FIN::SQL_INSERT_PORTAFOLIO");
        		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"INSERCION_PORTAFOLIO_MANT_EXITOSA",new HashMap<String,String>(),conn,entrada.p_IdEmpresa));
                salida.P_OUT_REFID = obtenerIdsInsertadas(portafolioNuevos);
        	} catch (SQLException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        }
    
       

        long fin = System.currentTimeMillis();
        logger.debug("FIN::AdministrarPortafolioDefecto.instertarPorfatoliosMantenedor", fin - inicio);
        return salida;
    }


    private SalidaAdministrarPortafolioDefecto crearEventoAutorizacion(Connection conn, EntradaAdministrarPortafolioDefecto entrada, List<PortafolioMantenedor> portafolios, SalidaAdministrarPortafolioDefecto salida) throws Exception {
        logger.debug("INICIO::AdministrarPortafolioDefecto.crearEventoAutorizacion");
        long inicio = System.currentTimeMillis();

        CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();

        CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
        entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
        entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
        entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
        entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";

        CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(conn, entradaCrearEventoAutorizacionMasivo);

        salida.P_OUT_ERRORESCONTADOR += salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
        salida.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
        salida.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));

        if (salida.P_OUT_ERRORESCONTADOR.equals(0L)) {
        	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<>(), conn, entrada.p_IdEmpresa));
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::AdministrarPortafolioDefecto.crearEventoAutorizacion", fin - inicio);

        return salida;
    }

    // ELIMINAR PORTAFOLIOS
    public SalidaAdministrarPortafolioDefecto eliminarPortafolio(Connection con, EntradaAdministrarPortafolioDefecto entrada, SalidaAdministrarPortafolioDefecto salida) throws Exception {
		logger.info("INICIO::AdministrarPortafolioDefecto.eliminarPortafolio");
		
		SQL sql=new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>(1);
		params.put("CodigosPortafolio", entrada.p_Portafolios.stream().map(portafolio -> portafolio.CodigoPortafolio).collect(Collectors.toList()));
		
		List<PortafolioMantenedor> portafoliosExistentes = sql.query(con, SQL_SELECT_PORTAFOLIO, params, PortafolioMantenedor.class);
		
		HashMap<String,String> paramsMensaje = new HashMap<String,String>(1);
		
		for(PortafolioMantenedor portafolioEntrada : entrada.p_Portafolios) {
			paramsMensaje.put("CodigoPortafolio", portafolioEntrada.CodigoPortafolio);
			
			if (!portafoliosExistentes.stream().anyMatch(port -> port.CodigoPortafolio.equals(portafolioEntrada.CodigoPortafolio)) ) {
															
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CODIGO_PORTAFOLIO_NO_EXISTE",paramsMensaje,con,entrada.p_IdEmpresa));
			}else {				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"ELIMINACION_PORTAFOLIO_MANT_EXITOSA", paramsMensaje,con,entrada.p_IdEmpresa));				
				logger.info("Portafolio Eliminado: " +  portafolioEntrada.CodigoPortafolio);
			}
			paramsMensaje.clear();
		}
							
		logger.debug("INICIO::SQL_DELETE_PORTAFOLIO");
		sql.batch(con, SQL_DELETE_PORTAFOLIO, portafoliosExistentes, PortafolioMantenedor.class);
		logger.debug("FIN::SQL_DELETE_PORTAFOLIO");
		
		logger.info("FIN::eliminarPortafolio");
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		return salida;
	}

    private void agregarLog(Connection conn, EntradaAdministrarPortafolioDefecto entrada, List<PortafolioMantenedor> portafolios) throws Exception {
        logger.trace("INICIO::AdministrarPortafolioDefecto.agregarLog");
        long inicio = System.currentTimeMillis();

        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

        for (PortafolioMantenedor portafolio : entrada.p_Portafolios) {
            //SocioNegocioUtils.Portafolio portafolioSCU = portafoliosPorIdPortafolio.get(portafolio.IdPortafolioConfiguracionDefecto);
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(portafolio.IdPortafolioConfiguracionDefecto, "IdPortafolioConfiguracionDefecto", "", Long.toString(portafolio.IdPortafolioConfiguracionDefecto)));
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(portafolio.IdPortafolioConfiguracionDefecto, "Proposito", "", portafolio.Proposito));

            AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
            log.IdCabeceraLog = portafolio.IdPortafolioConfiguracionDefecto;
            log.IdTipoLog = "INSERT".equals(entrada.p_Opcion) ? "CREACION" : "ELIMINACION";
            log.IdModulo = entrada.p_IdModulo;
            log.IdUsuario = entrada.p_IdUsuario;
            log.IdEmpresa = entrada.p_IdEmpresa;
            log.FechaSistemaLog = fechaSistema;
            log.IdUsuarioAtendido = entrada.p_IdUsuario;
            //log.IdCliente = portafolio.IdCliente;
            log.IdTipoRegistro =  "INSERT".equals(entrada.p_Opcion) ? "CREACION_PORTAFOLIO" : "ELIMINACION_PORTAFOLIO";
            //log.EstadoRegistro = portafolio.IdEstadoAutorizacion;
            //log.EstadoProceso = portafolio.IdEstadoAutorizacion;
            log.NombreProceso =  "INSERT".equals(entrada.p_Opcion) ? "Creacion Portafolio" : "Eliminacion Portafolio";
            log.IdRegistro = String.valueOf(portafolio.IdPortafolioConfiguracionDefecto);

            entradaAgregarLog.p_Log.add(log);
        }

        AgregarLog agregarLogService = new AgregarLog();
        agregarLogService.logica(conn, entradaAgregarLog);

        long fin = System.currentTimeMillis();
        logger.trace("FIN::AdministrarPortafolioDefecto.agregarLog", fin - inicio);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("== INICIO TEST ==");
        String requestString = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1006,\"p_IdModulo\":\"POR\",\"p_UsaReglasNegocio\":\"N\",\"p_Opcion\":\"DELETE\",\"p_Portafolios\":[{\"IdPortafolioConfiguracionDefecto\":0,\"CodigoPortafolio\":\"CASO1\",\"Proposito\":\"FONDOS MUTUOS TEST\"},{\"IdPortafolioConfiguracionDefecto\":0,\"CodigoPortafolio\":\"112\",\"Proposito\":\"FFMM\"},{\"IdPortafolioConfiguracionDefecto\":0,\"CodigoPortafolio\":\"111\",\"Proposito\":\"FFMM\"}]}";

        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")) {
            Gson gson = Utils.getGsonBuilder();
            EntradaAdministrarPortafolioDefecto entrada = gson.fromJson(requestString, EntradaAdministrarPortafolioDefecto.class);
            AdministrarPortafolioDefecto AdministrarPortafolioDefecto = new AdministrarPortafolioDefecto();
            try {
                SalidaAdministrarPortafolioDefecto out = AdministrarPortafolioDefecto.logica(conn, entrada);
                System.out.println(out);
                conn.rollback();
            } catch (Exception e) {
                e.printStackTrace();
                conn.rollback();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }

	@Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaAdministrarPortafolioDefecto jin = gson.fromJson(in.toString(), EntradaAdministrarPortafolioDefecto.class);
        SalidaAdministrarPortafolioDefecto out = logica(conn, jin);

        JsonParser jsonParser = new JsonParser();

        return jsonParser.parse(gson.toJson(out)).getAsJsonObject();
    }	
}