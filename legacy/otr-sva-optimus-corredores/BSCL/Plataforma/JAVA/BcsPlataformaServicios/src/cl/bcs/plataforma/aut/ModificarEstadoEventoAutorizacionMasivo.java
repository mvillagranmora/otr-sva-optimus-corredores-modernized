package cl.bcs.plataforma.aut;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.aut.pojo.AutorizadoEtapaEvento;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarEstadoEventoAutorizacionMasivo implements JavaCall {

    /*
     * El log
     */
	private static final Logger logger = LogManager.getLogger(ModificarEstadoEventoAutorizacionMasivo.class);
	
    /*
     * Queries que van a ser utilizadas por el servicio.
     */

    private final String SQL_ConsultaEvento = loadResourceToString("cl/bcs/plataforma/aut/sql/ConsultaEvento.sql");
    private final String SQL_ConsultaEtapaevento = loadResourceToString("cl/bcs/plataforma/aut/sql/ConsultaEtapaEvento.sql");
    private final String SQL_ConsultaRolUsuario = loadResourceToString("cl/bcs/plataforma/aut/sql/ConsultaRolUsuario.sql");
    private final String SQL_ConsultaAutorizadoEtapaEvento = loadResourceToString("cl/bcs/plataforma/aut/sql/ConsultaAutorizadoEtapaEvento.sql");
    private final String SQL_ActualizaEvento = loadResourceToString("cl/bcs/plataforma/aut/sql/ActualizaEvento.sql");
    private final String SQL_InsertaAutorizadoEtapaEvento = loadResourceToString("cl/bcs/plataforma/aut/sql/InsertaAutorizadoEtapaEvento.sql");
    private final String SQL_ActualizaAutorizadoEtapaEvento = loadResourceToString("cl/bcs/plataforma/aut/sql/ActualizaAutorizadoEtapaEvento.sql");
    private final String SQL_ActualizaEtapaEvento = loadResourceToString("cl/bcs/plataforma/aut/sql/ActualizaEtapaEvento.sql");
    private final String SQL_ActualizaEventoPorEtapa = loadResourceToString("cl/bcs/plataforma/aut/sql/ActualizaEventoPorEtapa.sql");

    /*
     * Clases IN/OUT
     */
    public static class ServicioEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public List<ServicioEntradaEvento> p_Eventos;
        public List<ServicioEntradaEtapa> p_Etapas;
        public String p_EventoEtapa;
        public String p_IdEstado;
        public String p_Observaciones;
        public String p_Comentario = "";

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ServicioEntradaEvento {
        public Long IdEvento;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ServicioEntradaEtapa {
        public Long IdEvento;
        public Long IdEtapa;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ServicioSalida {
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        public List<ServicioSalidaAdvertencia> P_OUT_ADVERTENCIADETALLE;
        public List<ServicioSalidaError> P_OUT_ERRORESDETALLE;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ServicioSalidaAdvertencia {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
        
        public ServicioSalidaAdvertencia() {
        };

        public ServicioSalidaAdvertencia(String codigo, String mensaje) {
             this.Codigo = codigo;
             this.Descripcion = mensaje;
        }

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ServicioSalidaError {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;

        public ServicioSalidaError() {
        };

        public ServicioSalidaError(String codigo, String mensaje) {
             this.Codigo = codigo;
             this.Descripcion = mensaje;
        }

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    /*
     * Objetos de procesamiento
     */
    public static class EventoPrc extends Evento {
        // Info Adicional
        public Long IdPersonaUsuario;
        public String TipoEntidadUsuario;
        public Long IdEntidadUsuario;
        public Long IdDireccionElectronicaEntidadEMPUsuario;
        public String NombreEvento;
        // Info Caller
        public String SPConfirma;
        public String SPRechaza;
        public Integer SPFormato;
        public String SPCampoAutorizacion;
        public String SPCampoNegocio;
        public String JavaAutorizacion;
        public boolean EventoJava;
        // Procesados
        public boolean IsOK;
        public String MsgError;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class EtapaEventoPrc extends cl.bcs.plataforma.aut.pojo.EtapaEvento {
        // Procesados
        public boolean IsOK;
        public String MsgError;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class UpdateEvento {
        public Long IdEvento;
        public String IdEstado;
        public Long IdUsuarioCambioEstado;
        public Timestamp FechaHoraCambioEstado;
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class UpdateEventoPorEtapa {
        public Long IdEvento;
        public Long IdUsuarioCambioEstado;
        public Timestamp FechaHoraCambioEstado;
        public String Comentario;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class UpdateEtapaEvento {
        public Long IdEtapaEvento;
        public String IdEstado;
        public Long IdUsuarioCambioEstado;
        public Timestamp FechaHoraCambioEstado;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class InsertAutorizadoEtapaEvento extends cl.bcs.plataforma.aut.pojo.AutorizadoEtapaEvento {
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class RolUsuario {
        public Long IdUsuario;
        public Long IdRol;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    /*
     * 
     * Objeto a ser utilizado para convertir y procesar las llamadas de la capa
     * de transporte.
     * 
     */
    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        Gson gson = getGsonBuilder();
        ServicioEntrada in = gson.fromJson(jin.toString(), ServicioEntrada.class);
        ServicioSalida out = logica(con, in);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    /*
     * Autoriza etpas o eventos de autorizacion
     * 
     * 
     */
    public ServicioSalida logica(Connection con, ServicioEntrada in) throws Exception {
        logger.info("Inicio ");
        SQL sql = new SQL();
        int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, in.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
        ServicioSalida out = new ServicioSalida();
        Timestamp vFechaHoraSistema =  FechaDB.ObtenerFechaHoraSistemaTs(con, in.p_IdEmpresa);
        Date fechaSistema = Fecha.toDate(Utils.getConfiguracion(con, Utils.getSchema(), in.p_IdEmpresa, "PLA.FechaSistema"));
        out.P_OUT_ERRORESCONTADOR = new Long(0);
        out.P_OUT_ADVERTENCIASCONTADOR = new Long(0);
        out.P_OUT_ADVERTENCIADETALLE = new ArrayList<ServicioSalidaAdvertencia>();
        out.P_OUT_ERRORESDETALLE = new ArrayList<ServicioSalidaError>();
        List<Long> eventos = null;
        
        List<InsertAutorizadoEtapaEvento> listaAutEtapaEventoPorAutorizar = new ArrayList<InsertAutorizadoEtapaEvento>();
        HashMap<Long, Long> hashRolAutoriza = new HashMap<Long, Long>(2);
        
        String permiteAutorizarMismoUsuario = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "AUT.PermiteAutorizarMismoUsuario");

        if (in.p_EventoEtapa.equals("EVENTO")) {
            // Obtiene datos de eventos
            HashMap<String, Object> parmsEvts = new HashMap<String, Object>();
            eventos = Utils.getColumn(in.p_Eventos, "IdEvento", false);
            parmsEvts.put("ListaIdEventos", eventos);
            parmsEvts.put("IdEmpresa", in.p_IdEmpresa);
            List<EventoPrc> listaEvento = sql.query(con, SQL_ConsultaEvento, parmsEvts, EventoPrc.class);

            List<UpdateEvento> updateEvt = new ArrayList<>();

            // Procesa los eventos
            for (EventoPrc evt : listaEvento) {
                String error = "";
                if (in.p_IdEstado.equals("VCD") == false && evt.FechaHoraVigencia.getTime() < vFechaHoraSistema.getTime()) {
                    error = "EVENTO_NO_ESTA_VIGENTE";
                } else if (evt.IdEstado.equals("RZD") || evt.IdEstado.equals("ANU")) {
                    error = "EVENTO_SE_ENCUENTRA_RECHAZADO_ANULADO";
                } else if (evt.IdEstado.equals("VIG") == false) {
                    error = "EVENTO_NO_ESTA_VIGENTE";
                } else if (in.p_IdEstado.equals("VCD") == false && in.p_IdEstado.equals("ANU") == false && evt.IdUsuarioSuperAutorizador.equals(in.p_IdUsuario) == false) {
                    error = "USUARIO_NO_ES_SUPERAUTORIZADOR";
                } else if (in.p_IdEstado.equals("ANU") && evt.IdUsuario.equals(in.p_IdUsuario) == false) {
                    error = "USUARIO_NO_AUTORIZADO_ANULAR_EVENTO";
                }
                if (error.equals("")) {
                    evt.IsOK = true;
                    UpdateEvento ev = new UpdateEvento();
                    ev.IdEvento = evt.IdEvento;
                    ev.IdEstado = in.p_IdEstado;
                    ev.FechaHoraCambioEstado = vFechaHoraSistema;
                    ev.IdUsuarioCambioEstado = in.p_IdUsuario;
                    updateEvt.add(ev);
                    logger.debug("Cambio estado evento:" + ev.toString());
                } else {
                    evt.IsOK = false;
                    HashMap<String, String> dicVals = new HashMap<>();
                    evt.MsgError = Diccionario.getMensaje(con,in.p_IdEmpresa, error, dicVals);
                }
            }

            // Registros que no existen
            for (ServicioEntradaEvento evt : in.p_Eventos) {
                boolean existe = false;
                for (EventoPrc evt2 : listaEvento) {
                    if (evt.IdEvento.equals(evt2.IdEvento)) {
                        existe = true;
                        break;
                    }
                }
                if (existe == false) {
                    String error = "ETAPA_EVENTO_NO_EXISTE";
                    HashMap<String, String> dicVals = new HashMap<>();
                    dicVals.put("IdEvento", evt.IdEvento + "");
                    dicVals.put("IdUsuario", in.p_IdUsuario + "");
                    String MsgError = Diccionario.getMensaje(con, in.p_IdEmpresa,error, dicVals);
                    if (in.p_IdEstado.equals("VCD")) {
                		ServicioSalidaAdvertencia adv = new ServicioSalidaAdvertencia();
                        adv.Codigo = error;
                        adv.Descripcion = MsgError;
                        adv.RequiereAutorizacion = "N";
                		out.P_OUT_ADVERTENCIASCONTADOR++;
                        out.P_OUT_ADVERTENCIADETALLE.add(adv);
                	} else {
	                    ServicioSalidaError err = new ServicioSalidaError();
	                    err.Codigo = error;
	                    err.Descripcion = MsgError;
	                    err.RequiereAutorizacion = "N";
	                    out.P_OUT_ERRORESCONTADOR++;
	                    out.P_OUT_ERRORESDETALLE.add(err);
                	}
                }
            }
            // Actualiza a BD
            if (out.P_OUT_ADVERTENCIASCONTADOR == 0 && in.p_IdEstado.equals("VCD")) {
                // Actualiza los eventos ok
                sql.batch(con, SQL_ActualizaEvento, updateEvt, UpdateEvento.class);
            }
            if (out.P_OUT_ERRORESCONTADOR == 0) {
                // Actualiza los eventos ok
                sql.batch(con, SQL_ActualizaEvento, updateEvt, UpdateEvento.class);
            }
        } else if (in.p_EventoEtapa.equals("ETAPA") && in.p_IdEstado.equals("ANU") == false) {
            // Obtiene datos de etapas
            HashMap<Long, Object> hlistaEtapaEvInput = Utils.List2Hash(in.p_Etapas, "IdEtapa");
            eventos = Utils.getColumn(in.p_Etapas, "IdEvento", false);

            HashMap<String, Object> parmsEvts = new HashMap<String, Object>();
            parmsEvts.put("ListaIdEventos", eventos);
            parmsEvts.put("IdEmpresa", in.p_IdEmpresa);
            List<EventoPrc> listaEvento = sql.query(con, SQL_ConsultaEvento, parmsEvts, EventoPrc.class);
            HashMap<Long, Object> hlistaEvento = Utils.List2Hash(listaEvento, "IdEvento");

            HashMap<String, Object> parmsEtapaEvts = new HashMap<String, Object>();
            parmsEtapaEvts.put("ListaIdEventos", eventos);
            List<EtapaEventoPrc> listaEtapaEvento = sql.query(con, SQL_ConsultaEtapaevento, parmsEtapaEvts, EtapaEventoPrc.class);

            HashMap<String, Object> parmsRolUser = new HashMap<String, Object>();
            parmsRolUser.put("p_IdUsuario", in.p_IdUsuario);
            List<RolUsuario> listaRolUsuario = sql.query(con, SQL_ConsultaRolUsuario, parmsRolUser, RolUsuario.class);

            HashMap<String, Object> parmsAutEtapaEvts = new HashMap<String, Object>();
            parmsAutEtapaEvts.put("ListaIdEventos", eventos);
            List<AutorizadoEtapaEvento> listaAutEtapaEvento = sql.query(con, SQL_ConsultaAutorizadoEtapaEvento, parmsAutEtapaEvts, AutorizadoEtapaEvento.class);
            HashMap<Long, List<AutorizadoEtapaEvento>> hlistaAutEtapaEvento = new HashMap<>();

            listaAutEtapaEvento.forEach(autEtaEve -> {
                hlistaAutEtapaEvento
                    .computeIfAbsent(autEtaEve.IdEtapaEvento, x -> new ArrayList<>())
                    .add(autEtaEve);
            });

            List<UpdateEventoPorEtapa> updateEvento = new ArrayList<>();
            List<UpdateEtapaEvento> updateEtapaEvento = new ArrayList<>();
            List<InsertAutorizadoEtapaEvento> insertAutEtapaEvento = new ArrayList<>();
            List<InsertAutorizadoEtapaEvento> updateAutEtapaEvento = new ArrayList<>();
            HashMap<Long, String> hashObservaciones = new  HashMap<Long, String>();
            
            // Procesa IdEtapaEvento enviadas
            for (EtapaEventoPrc eta : listaEtapaEvento) {
                String error = "";
                EventoPrc evt = (EventoPrc) hlistaEvento.get(eta.IdEvento);
                List<AutorizadoEtapaEvento> listaAutEtapaEventoSeleccionada = hlistaAutEtapaEvento.get(eta.IdEtapaEvento);
                //solo las autorizaciones del usuario
                List<AutorizadoEtapaEvento> listaAutEtapaEventoFiltrada = listaAutEtapaEventoSeleccionada.stream().filter(aev -> listaRolUsuario.stream().anyMatch(rolu -> (rolu.IdRol.equals(aev.IdRol) && rolu.IdRol != 0) 
                		|| (aev.IdUsuario.equals(in.p_IdUsuario) && aev.IdUsuario != 0)
                		|| (aev.IdUsuarioReemplaza.equals(in.p_IdUsuario)) && aev.IdUsuarioReemplaza != 0)
						&& aev.IdEstado.equals("VIG")).collect(Collectors.toList());

                // Etapa enviada
                if (!hlistaEtapaEvInput.containsKey(eta.IdEtapaEvento))
                    continue;

                // super autorizador
                boolean superAutorizador = false;
                if (evt != null && listaRolUsuario.isEmpty() == false) {
                    if (evt.IdUsuarioSuperAutorizador.equals(in.p_IdUsuario))
                        superAutorizador = true;
                    if (superAutorizador == false) {
                        for (RolUsuario rus : listaRolUsuario) {
                            if (rus.IdRol.equals(evt.IdRolSuperAutorizador)) {
                                superAutorizador = true;
                                break;
                            }
                        }
                    }
                }
                
                // validaciones basicas
                if (evt == null) {
                	error = "ETAPA_EVENTO_NO_EXISTE";
                } else if (eta.IdEstado.equals("RZD") || eta.IdEstado.equals("ANU")) {
                	error = "ETAPA_SE_ENCUENTRA_RECHAZADO_ANULADO";
                } else if (eta.IdEstado.equals("AUT")) {
                	error = "ETAPA_SE_ENCUENTRA_AUTORIZADO";
                } else if (evt.FechaHoraVigencia.getTime() < vFechaHoraSistema.getTime()) {
                	error = "EVENTO_NO_ESTA_VIGENTE";
                } else if (evt.IdEstado.equals("RZD") || evt.IdEstado.equals("ANU")) {
                	error = "EVENTO_SE_ENCUENTRA_RECHAZADO_ANULADO";
                } else if (evt.IdEstado.equals("AUT")) {
                	error = "EVENTO_SE_ENCUENTRA_AUTORIZADO";
                } else if (evt.IdUsuario.equals(in.p_IdUsuario) && permiteAutorizarMismoUsuario.equals("N")) {
                	error = "EVENTO_NO_AUTORIZA_MISMO_USUARIO";
                } else if(superAutorizador) {
	                
                	Iterator<AutorizadoEtapaEvento> iteracion = listaAutEtapaEventoFiltrada.iterator();

                	while (iteracion.hasNext()) {
                	    AutorizadoEtapaEvento aut = iteracion.next();
                	    
                	    if (!aut.IdEtapa.equals(eta.IdEtapa)) {
                	        continue;
                	    }

                	    if (aut.IdUsuarioReemplaza.equals(in.p_IdUsuario)) {
                            if (fechaSistema.getTime() >= aut.ReemplazaDesde.getTime() && fechaSistema.getTime() <= aut.ReemplazaHasta.getTime()
                                    || aut.ReemplazaDesde.equals(Fecha.toDate("0001-01-01")) && aut.ReemplazaHasta.equals(Fecha.toDate("0001-01-01"))) {
                            	logger.debug("Usuario de reemplazo superautorizador" + in.p_IdUsuario + " puede autorizar etapa: " + eta);
                            	hashObservaciones.put(aut.IdAutorizadoEtapaEvento, "REEMPLAZO");
                            } else {
                            	iteracion.remove();
                            }
                        }
                	}     	
                } else {
                    if (("S").equals(evt.EtapasSecuenciales)) {
                        int etapaAnt = eta.Orden - 1;
                        for (EtapaEventoPrc subeta : listaEtapaEvento) {
                            if (subeta.IdEvento.equals(eta.IdEvento) && subeta.Orden.equals(etapaAnt)) {
                                if (!("AUT").equals(subeta.IdEstado)) {
                                	error =  "ETAPA_ANTERIOR_NO_AUTORIZADA";
                                }
                                break;
                            }
                        }
                    }
                    
                    boolean usuarioYaAutorizo = listaAutEtapaEventoSeleccionada.stream()
                    	    .filter(aut -> aut.IdEstado.equals("AUT"))
                    	    .anyMatch(aut -> {
                    	        boolean esUsuarioPrincipal = aut.IdUsuario.equals(in.p_IdUsuario) && !aut.Observaciones.equals("REEMPLAZO");
                    	        boolean esUsuarioReemplazo = aut.IdUsuarioReemplaza.equals(in.p_IdUsuario) && aut.Observaciones.equals("REEMPLAZO");
                    	        boolean esRolAutorizador = hashRolAutoriza.containsKey(in.p_IdUsuario) && hashRolAutoriza.get(in.p_IdUsuario).equals(aut.IdRol);
                    	        return esUsuarioPrincipal || esUsuarioReemplazo || esRolAutorizador;
                    	    });
                    
                    if(usuarioYaAutorizo) {
                    	error = "USUARIO_YA_AUTORIZO";
                    }
                    
                    if (error.isEmpty()) {
                    	    
                        Iterator<AutorizadoEtapaEvento> iteracion = listaAutEtapaEventoFiltrada.iterator();

                        while (iteracion.hasNext()) {
                            AutorizadoEtapaEvento aut = iteracion.next();
                            
                            if (!aut.IdEtapa.equals(eta.IdEtapa)) {
                                continue;
                            }

                            boolean autorizado = false;

                            // Verificacion por usuario 
                            if (aut.IdUsuario.equals(in.p_IdUsuario)) {
                                logger.debug("Usuario " + in.p_IdUsuario + " puede autorizar etapa: " + eta);
                                autorizado = true;
                            }

                            // Verificacion por usuario reemplazo
                            if (aut.IdUsuarioReemplaza.equals(in.p_IdUsuario)) {
                                if (fechaSistema.getTime() >= aut.ReemplazaDesde.getTime() && fechaSistema.getTime() <= aut.ReemplazaHasta.getTime()
                                        || aut.ReemplazaDesde.equals(Fecha.toDate("0001-01-01")) && aut.ReemplazaHasta.equals(Fecha.toDate("0001-01-01"))) {
                                    logger.debug("Usuario de reemplazo " + in.p_IdUsuario + " puede autorizar etapa: " + eta);
                                    hashObservaciones.put(aut.IdAutorizadoEtapaEvento, "REEMPLAZO");
                                    autorizado = true;
                                }
                            }

                            // Verificacion por rol
                            for (RolUsuario rolu : listaRolUsuario) {
                                if (aut.IdRol.equals(rolu.IdRol)) {
                                    logger.debug("Usuario " + in.p_IdUsuario + " puede autorizar x rol etapa: " + eta);
                                    autorizado = true;
                                    hashRolAutoriza.put(in.p_IdUsuario, aut.IdRol);
                                    break;
                                }
                            }

                            if (!autorizado) {
                                iteracion.remove();
                            }
                        }

                        if (listaAutEtapaEventoFiltrada.isEmpty()) {
                        	error = "USUARIO_NO_AUTORIZADO_EN_ETAPA";
                        }
                    }
                }
                
                // Manejo error
                if (error.isEmpty()) {
                    eta.IsOK = true;
                    eta.MsgError = "";
                    
                    // Agrega autorizador ( AutorizadoEtapaEvento )
                    listaAutEtapaEventoPorAutorizar = listaAutEtapaEventoFiltrada.stream().map(aut -> {
     			        InsertAutorizadoEtapaEvento insert = new InsertAutorizadoEtapaEvento();
     			        insert.IdAutorizadoEtapaEvento = aut.IdAutorizadoEtapaEvento;
     			        insert.IdEtapa = aut.IdEtapa;
     			        insert.IdEtapaEvento = aut.IdEtapaEvento;
     			        insert.IdEstado = in.p_IdEstado;
     			        insert.IdRol = aut.IdRol;
     			        insert.IdUsuarioReemplaza  = aut.IdUsuarioReemplaza;
     			        insert.ReemplazaDesde = aut.ReemplazaDesde;
     			        insert.ReemplazaHasta = aut.ReemplazaHasta;
     			        insert.FechaHoraCambioEstado = vFechaHoraSistema;
     			        //se marcan los reemplazos
     			        boolean esReemplazo = hashObservaciones.containsKey(aut.IdAutorizadoEtapaEvento);

     			        if (esReemplazo) {
     			            insert.IdUsuario = aut.IdUsuario;
     			            insert.Observaciones = hashObservaciones.get(aut.IdAutorizadoEtapaEvento);
     			        } else {
     			            insert.IdUsuario = in.p_IdUsuario;
     			            insert.Observaciones = in.p_Observaciones + "";
     			        }
     			        return insert;
     			    })
     			    .collect(Collectors.toList());

                    
                    int vigentes = 0;
                    int autorizadas = 0;
                    int rechazadas = 0;
                    AutorizadoEtapaEvento findautEv = null;
                    for (AutorizadoEtapaEvento autEv : listaAutEtapaEventoSeleccionada) {
                        if (autEv.IdEtapaEvento.equals(eta.IdEtapaEvento)) {
                            if (autEv.IdEstado.equals("VIG")) {
                                vigentes++;
                                if (autEv.IdUsuario.equals(in.p_IdUsuario))
                                    findautEv = autEv;
                                if (autEv.IdUsuarioReemplaza.equals(in.p_IdUsuario))
                                    findautEv = autEv;
                                for (RolUsuario rolu : listaRolUsuario) {
                                    if (autEv.IdRol.equals(rolu.IdRol))
                                        findautEv = autEv;
                                }
                            }
                            if (autEv.IdEstado.equals("AUT"))
                                autorizadas++;
                            if (autEv.IdEstado.equals("RZD"))
                                rechazadas++;
                        }
                    }
                    
                    if (in.p_IdEstado.equals("VIG"))
                        vigentes++;
                    
                    if (in.p_IdEstado.equals("AUT")) {
                    	int porAutorizar = listaAutEtapaEventoPorAutorizar.size();
                    	autorizadas += porAutorizar;
                    	vigentes -= porAutorizar;  
                    }
                    
                    if (in.p_IdEstado.equals("RZD")) {
                        rechazadas++;
                        vigentes--;
                    }

                    if (findautEv != null) {
                    	updateAutEtapaEvento.addAll(listaAutEtapaEventoPorAutorizar);
                    } 

                    // Cambia estado de la etapa
                    boolean existeSuperAutorizador = existeSuperAutorizadorEnEtapa(listaAutEtapaEventoSeleccionada, evt, fechaSistema);
                    
                    // si no existe el superautorizador lo crea en autorizadoetapaevento
                    if (superAutorizador && !existeSuperAutorizador) {
                        insertAutEtapaEvento.add(crearAutorizadoEtapaEvento(eta, evt, vFechaHoraSistema, in));
                    }

                    if (in.p_IdEstado.equals("AUT")) {
                        UpdateEtapaEvento ev = null;
                        
                        if (superAutorizador) {
                        	// Autoriza etapa total
                            ev = crearUpdateEtapaEvento(eta, vFechaHoraSistema, in);
                            logger.debug("Usuario " + in.p_IdUsuario + " autoriza etapa SUPER AUTORIZADOR " + ev.toString());
                        } else if (eta.TipoAutorizacion.equals("T") && rechazadas == 0 && vigentes <= 0 && autorizadas > 0) {
                        	// Autoriza etapa total
                            ev = crearUpdateEtapaEvento(eta, vFechaHoraSistema, in);
                            logger.debug("Usuario " + in.p_IdUsuario + " autoriza etapa TOTAL=" + autorizadas + " " + ev.toString());
                        } else if (eta.TipoAutorizacion.equals("M") && autorizadas >= (int) eta.MinimoAutorizan) {
                            /// Autoriza etapa Minimos
                            ev = crearUpdateEtapaEvento(eta, vFechaHoraSistema, in);
                            logger.debug("Usuario " + in.p_IdUsuario + " autoriza etapa MINIMO=" + autorizadas + " " + ev.toString());
                        } else {
                            // No cumple condiciones para autorizar
                            logger.debug("Usuario " + in.p_IdUsuario + " falta por autorizar aut=" + autorizadas + " vig=" + vigentes + " rzd=" + rechazadas);
                        }
                        
                        if (ev != null) {
                            updateEtapaEvento.add(ev);
                        }
                    } else {
                        // Cambia estado a etapa != 'AUT'
                        UpdateEtapaEvento ev = crearUpdateEtapaEvento(eta, vFechaHoraSistema, in);
                        updateEtapaEvento.add(ev);
                        logger.debug("Usuario " + in.p_IdUsuario + " cambia etapa != AUT " + ev.toString());
                    }
                    
                    // Candidato a autorizar evento
                    UpdateEventoPorEtapa eve = new UpdateEventoPorEtapa();
                    eve.IdEvento = eta.IdEvento;
                    eve.IdUsuarioCambioEstado = in.p_IdUsuario;
                    eve.FechaHoraCambioEstado = vFechaHoraSistema;
                    if(in.p_IdEstado.equals("RZD")) {
                    	eve.Comentario = in.p_Comentario;
                    }else eve.Comentario = "";
                    updateEvento.add(eve);
                } else {
                    eta.IsOK = false;
                    HashMap<String, String> dicVals = new HashMap<>();
                    dicVals.put("IdEvento", eta.IdEvento + "");
                    dicVals.put("IdEtapa", eta.IdEtapa + "");
                    dicVals.put("IdUsuario", in.p_IdUsuario + "");
                    eta.MsgError = Diccionario.getMensaje(con, in.p_IdEmpresa,error, dicVals);
                    out.P_OUT_ERRORESCONTADOR++;
                    ServicioSalidaError err = new ServicioSalidaError();
                    err.Codigo = error;
                    err.Descripcion = eta.MsgError;
                    err.RequiereAutorizacion = "N";
                    out.P_OUT_ERRORESDETALLE.add(err);

                    if(!error.isEmpty() && error.equals("EVENTO_NO_ESTA_VIGENTE")){
                        //correr el servicio de vencimiento eventos (como fallback).
                        try{
                            logger.debug("VENCER EVENTOS");
                            VencerEventosAutorizacion vencer = new VencerEventosAutorizacion();
                            VencerEventosAutorizacion.ServicioEntrada srvIN = new VencerEventosAutorizacion.ServicioEntrada();
                            srvIN.p_IdEmpresa = in.p_IdEmpresa;
                            srvIN.p_IdModulo = "AUT";
                            srvIN.p_IdUsuario = in.p_IdUsuario;
                            vencer.logica(con, srvIN);
                        }catch(Exception e){
                            logger.error("ERROR AL VENCER EVENTOS");
                            logger.error(e);
                        }
                    }
                }
            }

            // Registros que no existen
            for (ServicioEntradaEtapa eta : in.p_Etapas) {
                boolean existe = false;
                for (EtapaEventoPrc eta2 : listaEtapaEvento) {
                    if (eta.IdEvento.equals(eta2.IdEvento) && eta.IdEtapa.equals(eta2.IdEtapaEvento)) {
                        existe = true;
                        break;
                    }
                }
                if (existe == false) {
                    String error = "ETAPA_EVENTO_NO_EXISTE";
                    HashMap<String, String> dicVals = new HashMap<>();
                    dicVals.put("IdEvento", eta.IdEvento + "");
                    dicVals.put("IdEtapa", eta.IdEtapa + "");
                    dicVals.put("IdUsuario", in.p_IdUsuario + "");
                    String MsgError = Diccionario.getMensaje(con,in.p_IdEmpresa, error, dicVals);
                    out.P_OUT_ERRORESCONTADOR++;
                    ServicioSalidaError err = new ServicioSalidaError();
                    err.Codigo = error;
                    err.Descripcion = MsgError;
                    err.RequiereAutorizacion = "N";
                    out.P_OUT_ERRORESDETALLE.add(err);
                }
            }
            // Actualiza a BD
            if (out.P_OUT_ERRORESCONTADOR == 0) {
                // Actualiza en BD Autorizador
                sql.batch(con, SQL_InsertaAutorizadoEtapaEvento, insertAutEtapaEvento, InsertAutorizadoEtapaEvento.class);
                sql.batch(con, SQL_ActualizaAutorizadoEtapaEvento, updateAutEtapaEvento, InsertAutorizadoEtapaEvento.class);

                // Actualiza en BD EtapaEvento
                sql.batch(con, SQL_ActualizaEtapaEvento, updateEtapaEvento, UpdateEtapaEvento.class);

                // Actualiza en BD Evento procesados
                sql.batch(con, SQL_ActualizaEventoPorEtapa, updateEvento, UpdateEventoPorEtapa.class);
            }
        }
        //////////////////////////////////////////////////////////
        // notifica a los negocios del cambio de evento
        //////////////////////////////////////////////////////////
        if (out.P_OUT_ERRORESCONTADOR == 0 && eventos != null) {
            HashMap<String, Object> parmsEvts = new HashMap<String, Object>();
            parmsEvts.put("ListaIdEventos", eventos);
            parmsEvts.put("IdEmpresa", in.p_IdEmpresa);
            List<EventoPrc> listaEvento = sql.query(con, SQL_ConsultaEvento, parmsEvts, EventoPrc.class);
            HashMap<String, List<EventoPrc>> eventosXtipoAut = new HashMap<String, List<EventoPrc>>();
            HashMap<String, List<EventoPrc>> eventosXtipoRzd = new HashMap<String, List<EventoPrc>>();
            for (EventoPrc evt : listaEvento) {
                try {
                	if (evt.JavaAutorizacion != null && evt.JavaAutorizacion.length() > 0) {
                		evt.EventoJava = true;
                	}
                	else {
                		evt.EventoJava = false;
                	}
                	
                	if (evt.EventoJava ) {
                		
                		try {
                			Class.forName(evt.JavaAutorizacion);
                		}
                		catch (ClassNotFoundException e) {
                			
                			throw new IllegalArgumentException("Servicio de autorizacion mal configurado para evento " + evt.IdTipoEvento);
                		}
                	}
                	else {
                		if ((evt.SPConfirma == null || evt.SPConfirma.length() == 0) ) {
                            throw new IllegalArgumentException("Servicio de confirmacion no especificado para tipo evento " + evt.IdTipoEvento);
                		}
                		if ((evt.SPRechaza == null || evt.SPRechaza.length() == 0) ) {
                            throw new IllegalArgumentException("Servicio de rechazo no especificado para tipo evento " + evt.IdTipoEvento);
                        }
                		if ((evt.SPCampoAutorizacion == null || evt.SPCampoAutorizacion.length() == 0)) {
                		    //para java no es necesaria esta validacion, pasamos el evento completo al servicio
                			throw new IllegalArgumentException("SPCampoAutorizacion no especificado para tipo evento " + evt.IdTipoEvento);
                        }
                		if ((evt.SPCampoNegocio == null || evt.SPCampoNegocio.length() == 0)) {
                			//para java no es necesaria esta validacion, pasamos el evento completo al servicio
                            throw new IllegalArgumentException("SPCampoNegocio no especificado para tipo evento " + evt.IdTipoEvento);
                        }
                	}
                	
                    
                } catch (IllegalArgumentException ie) {
                	if (in.p_IdEstado.equals("VCD")) {
                		out.P_OUT_ADVERTENCIASCONTADOR++;
                		out.P_OUT_ADVERTENCIADETALLE.add(new ServicioSalidaAdvertencia("UI_ERROR_CONFIGURACION", ie.getMessage()));
                	}else {
	                    out.P_OUT_ERRORESCONTADOR++;
	                    out.P_OUT_ERRORESDETALLE.add(new ServicioSalidaError("UI_ERROR_CONFIGURACION", ie.getMessage()));
	                    logger.error(ie);
	                    continue;
                	}
                }
                
                if (evt.IdEstado.equals("AUT")) {
                    if (!eventosXtipoAut.containsKey(evt.IdTipoEvento))
                        eventosXtipoAut.put(evt.IdTipoEvento, new ArrayList<EventoPrc>());
                    eventosXtipoAut.get(evt.IdTipoEvento).add(evt);
                }
                if (evt.IdEstado.equals("RZD") || evt.IdEstado.equals("VCD")) {
                	evt.IdEstado = "RZD";//asegurar estado RZD, para vencer las solicitudes de cambio
                    if (!eventosXtipoRzd.containsKey(evt.IdTipoEvento))
                        eventosXtipoRzd.put(evt.IdTipoEvento, new ArrayList<EventoPrc>());
                    eventosXtipoRzd.get(evt.IdTipoEvento).add(evt);
                }
            }
            // Llama a negocio masivamente para autorizar
            for (String IdTipoEvento : eventosXtipoAut.keySet()) {
            	System.out.println("Autorizando...");
                List<EventoPrc> evts = eventosXtipoAut.get(IdTipoEvento);
                EventoPrc info = evts.get(0);
                
                if (info.EventoJava) {
                	
                	SalidaAutorizacion salida = procesarAutorizacionJava(con,evts,info.JavaAutorizacion,in,"AUTORIZA");
                	
					for (MensajeSalida item : salida.P_OUT_MENSAJES) {
						if (item.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) {
							out.P_OUT_ERRORESCONTADOR++;
							ServicioSalidaError err = new ServicioSalidaError();
	                    	err.Codigo = item.codigo;
	                    	err.Descripcion = item.descripcion;
	                    	out.P_OUT_ERRORESDETALLE.add(err);
						}
                        if (item.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ADVERTENCIA)) {
                            out.P_OUT_ADVERTENCIASCONTADOR++;
                            ServicioSalidaAdvertencia advertencia = new ServicioSalidaAdvertencia();
                            advertencia.Codigo = item.codigo;
                            advertencia.Descripcion = item.descripcion;
                            out.P_OUT_ADVERTENCIADETALLE.add(advertencia);
                        }
						
					}
                }
                else {
                	List<Object> Ids = Utils.getColumn(evts, info.SPCampoAutorizacion, false);
                    List<PlataformaUtils.ErrorDetalle> res = armaCallWrapperNegocioSql(con, info.SPCampoNegocio, info.SPFormato, info.SPConfirma, info.IdCanal, Ids, in, IdTipoEvento);
                    for(PlataformaUtils.ErrorDetalle item:res){
                    	out.P_OUT_ERRORESCONTADOR++;
                    	ServicioSalidaError err = new ServicioSalidaError();
                    	err.Codigo = item.Codigo;
                    	err.Descripcion = item.Descripcion;
                    	out.P_OUT_ERRORESDETALLE.add(err);
                    }
                }                
            }
            // Llama a negocio masivamente parar rechazar
            for (String IdTipoEvento : eventosXtipoRzd.keySet()) {
                List<EventoPrc> evts = eventosXtipoRzd.get(IdTipoEvento);
                EventoPrc info = evts.get(0);
                
                if (info.EventoJava) {
                	if (info.EventoJava) {
                    	
                    	SalidaAutorizacion salida = procesarAutorizacionJava(con,evts,info.JavaAutorizacion,in,"RECHAZA");
                    	
    					for (MensajeSalida item : salida.P_OUT_MENSAJES) {
    						if (item.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) {
    							if(in.p_IdEstado.equals("VCD")) {
     	                    		out.P_OUT_ADVERTENCIASCONTADOR++;
     	                    		ServicioSalidaAdvertencia adv = new ServicioSalidaAdvertencia();
     	                    		adv.Codigo = item.codigo;
     	                    		adv.Descripcion = item.descripcion;
     	                    		out.P_OUT_ADVERTENCIADETALLE.add(adv);
    							}else {
	    							out.P_OUT_ERRORESCONTADOR++;
	    							ServicioSalidaError err = new ServicioSalidaError();
	    	                    	err.Codigo = item.codigo;
	    	                    	err.Descripcion = item.descripcion;
	    	                    	out.P_OUT_ERRORESDETALLE.add(err);
    							}
    						}
    						
    					}
                    }
                }
                else {
                	List<Object> Ids = Utils.getColumn(evts, info.SPCampoAutorizacion, false);
                    
                	if(info.IdEstado.equalsIgnoreCase("RZD")){
                		List<PlataformaUtils.ErrorDetalle> res = armaCallWrapperNegocioSql(con, info.SPCampoNegocio, info.SPFormato, info.SPRechaza, info.IdCanal, Ids, in, IdTipoEvento);
 	                    for(PlataformaUtils.ErrorDetalle item:res){
 	                    	if(in.p_IdEstado.equals("VCD")) {
 	                    		out.P_OUT_ADVERTENCIASCONTADOR++;
 	                    		ServicioSalidaAdvertencia adv = new ServicioSalidaAdvertencia();
 	                    		adv.Codigo = item.Codigo;
 	                    		adv.Descripcion = item.Descripcion;
 	                    		out.P_OUT_ADVERTENCIADETALLE.add(adv);
 	                    	}else {
 	                    		out.P_OUT_ERRORESCONTADOR++;
 	 	                    	ServicioSalidaError err = new ServicioSalidaError();
 	 	                    	err.Codigo = item.Codigo;
 	 	                    	err.Descripcion = item.Descripcion;
 	 	                    	out.P_OUT_ERRORESDETALLE.add(err);
 	                    	}
 	                    }
                	}else {
                	
	                    List<PlataformaUtils.ErrorDetalle> res = armaCallWrapperNegocioSql(con, info.SPCampoNegocio, info.SPFormato, info.SPConfirma, info.IdCanal, Ids, in, IdTipoEvento);
	                    for(PlataformaUtils.ErrorDetalle item:res){
	                    	out.P_OUT_ERRORESCONTADOR++;
	                    	ServicioSalidaError err = new ServicioSalidaError();
	                    	err.Codigo = item.Codigo;
	                    	err.Descripcion = item.Descripcion;
	                    	out.P_OUT_ERRORESDETALLE.add(err);
	                    }
                	}
                    
                }
                
            }
            // Llama a Notificaciones
            armaCallNotificaciones(con, in, listaEvento, vFechaHoraSistema);
        }
        // arma salida de respuesta
        logger.info("Fin");
        return out;
    }

    private SalidaAutorizacion procesarAutorizacionJava(Connection conn,List<EventoPrc> evts, String javaAutorizacion,ServicioEntrada in,String autorizaRechaza) 
    		throws Exception {
    	Class<?> clazz = Class.forName(javaAutorizacion);
    	BcsAutorizaciones autorizacion = (BcsAutorizaciones)clazz.newInstance();
    	EntradaAutorizacion entrada = new EntradaAutorizacion();
    	entrada.p_IdEmpresa = in.p_IdEmpresa;
    	entrada.p_IdModulo = in.p_IdModulo;
    	entrada.p_IdUsuario = in.p_IdUsuario;
    	entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
    	for (EventoPrc obj : evts) {
    		entrada.p_Lista.add(obj);
    	}
    	if (autorizaRechaza.equalsIgnoreCase("AUTORIZA")) {
    		return autorizacion.autorizarEvento(conn, entrada);	
    	}
    	else if (autorizaRechaza.equalsIgnoreCase("RECHAZA")) {
    		return autorizacion.rechazarEvento(conn, entrada);
    	}
    	else {
    		throw new Exception ("Evento mal configurado");
    	}
		
	}

	private void armaCallNotificaciones(final Connection con, final ServicioEntrada in, final List<EventoPrc> eventos, final Timestamp fechaHoraSistema) throws Exception {
        AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
        notifIn.P_IDEMPRESA = in.p_IdEmpresa;
        notifIn.P_IDUSUARIO = in.p_IdUsuario;
        notifIn.P_IDMODULO = in.p_IdModulo;
        notifIn.P_EVENTOS = new ArrayList<>();
        notifIn.P_VALORES = new ArrayList<>();
        notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
        notifIn.P_IDDIRECCIONES = new ArrayList<>();

        for (EventoPrc evento : eventos) {
            if (evento.IdEstado.equals("VIG"))
                continue;

            // Evento de notificacion
            AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
            evt.CodigoTipoEvento = "MODIFICACION_EVENTO_AUTORIZACION";
            evt.IdSecuencia = evento.IdEvento;
            evt.FechaCreacionSistema = Fecha.toStringDate(fechaHoraSistema);
            evt.IdPersonaRemitente = new Long(0);
            evt.IdPersonaDestinatario = evento.IdPersonaUsuario;
            evt.IdPortafolio = new Long(0);
            notifIn.P_EVENTOS.add(evt);

            // Valores para el mensaje
			notifIn.addValor(evento.IdEvento, "IdEvento", evento.IdEvento+"")
			       .addValor(evento.IdEvento, "glosa", evento.Glosa.length()>300 ? evento.Glosa.substring(300): evento.Glosa)
			       .addValor(evento.IdEvento, "Autorizacion", evento.NombreEvento+"")
			       .addValor(evento.IdEvento, "HoraRegistro", Fecha.toLocalFormat(evento.FechaHoraRegistro,"dd-MM-yyyy HH:mm"))
			       .addValor(evento.IdEvento, "HoraVigencia", Fecha.toLocalFormat(evento.FechaHoraVigencia,"dd-MM-yyyy HH:mm"))
			       .addValor(evento.IdEvento, "Estado",evento.IdEstado);

            // Correo a empleados
            if (evento.TipoEntidadUsuario != null && evento.TipoEntidadUsuario.equals("EMP") && evento.IdDireccionElectronicaEntidadEMPUsuario != null) {
                AgregarEvento.ObjIDDIRECCIONES dir = new AgregarEvento.ObjIDDIRECCIONES();
                dir.IdSecuencia = evento.IdEvento;
                dir.TipoDestinatario = evento.TipoEntidadUsuario;
                dir.IdDireccion = evento.IdDireccionElectronicaEntidadEMPUsuario;
                notifIn.P_IDDIRECCIONES.add(dir);
            }
        }
        if (notifIn.P_EVENTOS.size() > 0) {
            AgregarEvento notif = new AgregarEvento();
            AgregarEvento.ServiceSalida notifOut = notif.logica(con, notifIn);
            logger.debug("Usuario " + in.p_IdUsuario + " call notificaciones :" + notifOut.toString());
        }
    }

    private List<PlataformaUtils.ErrorDetalle> armaCallWrapperNegocioSql(final Connection con, final String campo, final int formato, final String sp, final String IdCanal, final List<Object> ids, final ServicioEntrada in, final String p_IdConcepto) throws SQLException {
    	List<PlataformaUtils.ErrorDetalle> res= new ArrayList<PlataformaUtils.ErrorDetalle>();
    	SQL sql=new SQL();
    	try {
            if (campo == null || campo.length() == 0)
                throw new IllegalArgumentException("Campo de  negocio no especificado");
            if (sp == null || sp.length() == 0)
                throw new IllegalArgumentException("Sp no especificado para el negocio");

            SQL.exec(con, "CREATE LOCAL TEMPORARY TABLE #AUT_MASIVO (\"" + campo + "\" BIGINT)");

            // TODO: use batch para mejor rendimiento
            for (Object id : ids) {
                SQL.exec(con, "INSERT INTO #AUT_MASIVO VALUES(" + id.toString() + ")");
            }
    		HashMap<String,Object> parmsIn =new HashMap<String,Object>();
    		HashMap<String,Class> parmsOut =new HashMap<String,Class>();
    		String dynsql;
    		HashMap<String,Object> callOut;
            if (formato == 4) {
                dynsql = "CALL \"" + sp + "\" (" + in.p_IdEmpresa + "," + in.p_IdUsuario + ",'" + in.p_IdModulo + "',#AUT_MASIVO)";
                callOut = sql.call(con, dynsql, parmsIn, parmsOut);  
            }else if(formato == 8) {
        		parmsOut.put("P_OUT_ADVERTENCIASCONTADOR", Long.class);
        		parmsOut.put("P_OUT_ADVERTENCIASDETALLE",PlataformaUtils.AdvertenciaDetalle.class);
        		parmsOut.put("P_OUT_ERRORESCONTADOR", Long.class);
        		parmsOut.put("P_OUT_ERRORESDETALLE", PlataformaUtils.ErrorDetalle.class);
        		dynsql = "CALL \"" + sp + "\" (" + in.p_IdEmpresa + "," + in.p_IdUsuario + ",'" + in.p_IdModulo + "',#AUT_MASIVO,"
        				+ "@P_OUT_ADVERTENCIASCONTADOR, $P_OUT_ADVERTENCIASDETALLE, @P_OUT_ERRORESCONTADOR, $P_OUT_ERRORESDETALLE) ";
        		callOut = sql.call(con, dynsql, parmsIn, parmsOut);
        		res = (List<PlataformaUtils.ErrorDetalle>)callOut.get("P_OUT_ERRORESDETALLE");		
            }else if(formato == 10) {
            	System.out.println("llamando SP");
            	//Debería llamarse IdConcepto en el caso de los telefonos... 
        		parmsOut.put("P_OUT_ADVERTENCIASCONTADOR", Long.class);
        		parmsOut.put("P_OUT_ADVERTENCIASDETALLE",PlataformaUtils.AdvertenciaDetalle.class);
        		parmsOut.put("P_OUT_ERRORESCONTADOR", Long.class);
        		parmsOut.put("P_OUT_ERRORESDETALLE", PlataformaUtils.ErrorDetalle.class);
        		
        		if( (p_IdConcepto.equals("NUETEL") && in.p_IdEstado.equals("RZD")) || (p_IdConcepto.equals("NUEDOC") && in.p_IdEstado.equals("RZD")) ) {
        			dynsql = "CALL \"" + sp + "\" (" + in.p_IdEmpresa + "," + in.p_IdUsuario + ",'" + in.p_IdModulo + "','N',#AUT_MASIVO,'" + p_IdConcepto + "',"
            				+ "@P_OUT_ADVERTENCIASCONTADOR, $P_OUT_ADVERTENCIASDETALLE, @P_OUT_ERRORESCONTADOR, $P_OUT_ERRORESDETALLE) ";
        		}else {
        			dynsql = "CALL \"" + sp + "\" (" + in.p_IdEmpresa + "," + in.p_IdUsuario + ",'" + in.p_IdModulo + "','N',#AUT_MASIVO,'" + in.p_IdEstado + "',"
            				+ "@P_OUT_ADVERTENCIASCONTADOR, $P_OUT_ADVERTENCIASDETALLE, @P_OUT_ERRORESCONTADOR, $P_OUT_ERRORESDETALLE) ";
        		}
        		
        		callOut = sql.call(con, dynsql, parmsIn, parmsOut);
        		res = (List<PlataformaUtils.ErrorDetalle>)callOut.get("P_OUT_ERRORESDETALLE");
        		System.out.println(callOut.get("P_OUT_ERRORESDETALLE"));
        		System.out.println(callOut.get("P_OUT_ADVERTENCIASDETALLE"));
            }else if(formato == 12) {
        		parmsOut.put("P_OUT_ADVERTENCIASCONTADOR", Long.class);
        		parmsOut.put("P_OUT_ADVERTENCIASDETALLE",PlataformaUtils.AdvertenciaDetalle.class);
        		parmsOut.put("P_OUT_ERRORESCONTADOR", Long.class);
        		parmsOut.put("P_OUT_ERRORESDETALLE", PlataformaUtils.ErrorDetalle.class);
        		dynsql = "CALL \"" + sp + "\" (" + in.p_IdEmpresa + "," + in.p_IdUsuario + ",'" + in.p_IdModulo + "','N',#AUT_MASIVO,'" + in.p_IdEstado + "','"+IdCanal+ "',"
        				+ "@P_OUT_ADVERTENCIASCONTADOR, $P_OUT_ADVERTENCIASDETALLE, @P_OUT_ERRORESCONTADOR, $P_OUT_ERRORESDETALLE) ";
        		callOut = sql.call(con, dynsql, parmsIn, parmsOut);
        		res = (List<PlataformaUtils.ErrorDetalle>)callOut.get("P_OUT_ERRORESDETALLE");
            }else throw new IllegalArgumentException("Formato invalido!");
        }
        catch (Exception ex) {
            logger.error("Error no controlado al actualizar autorizacion");
            logger.error(ex);
            return res;
        }finally {
            try {
                SQL.exec(con, "DROP TABLE #AUT_MASIVO ");
            } catch (Exception er) {}
        }
        return res;
    }
    
    private boolean existeSuperAutorizadorEnEtapa(List<AutorizadoEtapaEvento> listaAutEtapaEventoSeleccionada, Evento evt, Date fechaSistema) {
        boolean existeUsuarioSuperAutorizador = listaAutEtapaEventoSeleccionada.stream()
            .filter(aut -> aut.IdUsuario != 0)
            .anyMatch(aut -> aut.IdUsuario.equals(evt.IdUsuarioSuperAutorizador));
        
        boolean existeUsuarioReemplazaSuperAutorizador = listaAutEtapaEventoSeleccionada.stream()
                .filter(aut -> aut.IdUsuarioReemplaza != 0 && (
                        (fechaSistema.getTime() >= aut.ReemplazaDesde.getTime()
                        && fechaSistema.getTime() <= aut.ReemplazaHasta.getTime())
                        || (aut.ReemplazaDesde.equals(Fecha.toDate("0001-01-01"))
                        && aut.ReemplazaHasta.equals(Fecha.toDate("0001-01-01")))
                    ))
                .anyMatch(aut -> aut.IdUsuarioReemplaza.equals(evt.IdUsuarioSuperAutorizador));
        
        boolean existeRolSuperAutorizador = listaAutEtapaEventoSeleccionada.stream()
            .filter(aut -> aut.IdRol != 0)
            .anyMatch(aut -> evt.IdRolSuperAutorizador.equals(aut.IdRol));
        
        return existeUsuarioSuperAutorizador || existeUsuarioReemplazaSuperAutorizador || existeRolSuperAutorizador;
    }


    private InsertAutorizadoEtapaEvento crearAutorizadoEtapaEvento(EtapaEventoPrc eta, Evento evt, Timestamp fechaHoraSistema, ServicioEntrada in) {
        InsertAutorizadoEtapaEvento aev = new InsertAutorizadoEtapaEvento();
        aev.IdAutorizadoEtapaEvento = 0L;
        aev.IdEtapaEvento = eta.IdEtapaEvento;
        aev.IdUsuario = in.p_IdUsuario;
        aev.IdRol = evt.IdRolSuperAutorizador;
        aev.IdEstado = in.p_IdEstado;
        aev.FechaHoraCambioEstado = fechaHoraSistema;
        aev.Observaciones = in.p_Observaciones + "";
        return aev;
    }

    private UpdateEtapaEvento crearUpdateEtapaEvento(EtapaEventoPrc eta, Timestamp fechaHoraSistema, ServicioEntrada in) {
        UpdateEtapaEvento ev = new UpdateEtapaEvento();
        ev.IdEtapaEvento = eta.IdEtapaEvento;
        ev.IdEstado = in.p_IdEstado;
        ev.FechaHoraCambioEstado = fechaHoraSistema;
        ev.IdUsuarioCambioEstado = in.p_IdUsuario;
        return ev;
    }

    public static void main(String[] args) {
        System.out.println("== START ==");
        try (Connection con = PlataformaUtils.getPropertiesConnection("fynsapre", "connections.properties")){
            con.setAutoCommit(false);
            // --------------------------------
                JsonParser parser = new JsonParser();

                String lejson = "{\r\n"
                		+ "    \"p_IdEmpresa\": 1,\r\n"
                		+ "    \"p_IdUsuario\": 76767,\r\n"
                		+ "    \"p_IdModulo\": \"AUT\",\r\n"
                		+ "    \"p_simularOperacion\": \"N\",\r\n"
                		+ "    \"p_UsaReglasNegocio\": \"N\",\r\n"
                		+ "    \"p_Comentario\": \"\",\r\n"
                		+ "    \"p_Eventos\": [\r\n"
                		+ "        {\r\n"
                		+ "            \"IdEvento\": 0\r\n"
                		+ "        }\r\n"
                		+ "    ],\r\n"
                		+ "    \"p_Etapas\": [\r\n"
                		+ "        {\r\n"
                		+ "            \"IdEvento\": 868841,\r\n"
                		+ "            \"IdEtapa\": 868842\r\n"
                		+ "        }\r\n"
                		+ "    ],\r\n"
                		+ "    \"p_EventoEtapa\": \"ETAPA\",\r\n"
                		+ "    \"p_IdEstado\": \"AUT\",\r\n"
                		+ "    \"p_Observaciones\": \" \"\r\n"
                		+ "}";
                
                JsonObject dataJsonIn = parser.parse(lejson).getAsJsonObject();
                ModificarEstadoEventoAutorizacionMasivo demo = new ModificarEstadoEventoAutorizacionMasivo();
                System.out.println(demo.call(con, dataJsonIn).toString());
                con.rollback();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");
    }
}
