package cl.bcs.tesoreria;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.LiquidarComprobante.ComprobanteLiquidar_Entrada;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo.EtapaEventoPrc;
import cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo.InsertAutorizadoEtapaEvento;
import cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo.UpdateEtapaEvento;
import cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo.UpdateEventoPorEtapa;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionLiquidacion implements JavaCall, BcsAutorizaciones {
    static final Logger logger = LogManager.getLogger(AutorizacionLiquidacion.class);
    private final String SQL_RECHAZO_COMPROBANTE = loadResourceToString("cl/bcs/tesoreria/sql/AutorizacionLiquidacion_UpdateRechazo.sql");
    private final String SQL_DETALLE_COMPROBANTE = loadResourceToString("cl/bcs/tesoreria/sql/AutorizacionLiquidacion_SelectDetalleComprobante.sql");
    
    private final String SQL_Consultaevento = "SELECT  \"IdEvento\" ,\"IdTipoEvento\" ,\"NumeroOperacionConcepto\",\"IdEstado\" FROM \"BSCL.Plataforma.BD::AUT.Evento\" WHERE \"NumeroOperacionConcepto\"=:IdComprobante and \"IdEstado\"IN ('AUT','VIG')";//  loadResourceToString("cl/bcs/plataforma/aut/sql/ActualizaEvento.sql");
    private final String SQL_ConsultaEtapaevento = loadResourceToString("cl/bcs/plataforma/aut/sql/ConsultaEtapaEvento.sql");
    private final String SQL_InsertaAutorizadoEtapaEvento = loadResourceToString("cl/bcs/plataforma/aut/sql/InsertaAutorizadoEtapaEvento.sql");
    private final String SQL_ActualizaEtapaEvento = loadResourceToString("cl/bcs/plataforma/aut/sql/ActualizaEtapaEvento.sql");
    private final String SQL_ActualizaeventoPorEtapa =loadResourceToString("cl/bcs/tesoreria/sql/AutorizacionLiquidacion_ActualizaEventoPorEtapa.sql");
    private final String SQL_ESTADOCOMPROBANTE =loadResourceToString("cl/bcs/tesoreria/sql/AutorizacionLiquidacion_EstadoComprobante.sql");
    @Override
    public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
    	logger.info("INICIO:AUTORIZACION");
    	
		SalidaAutorizacion s = new SalidaAutorizacion();
		LiquidarComprobante_Entrada entradaLiquidarComprobante = new LiquidarComprobante_Entrada();
		
		HashMap<Long,String> hashSalidaConfirmar = new HashMap<>();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		HashMap<String,Object> inDatos = new HashMap<String,Object>();
		HashMap<String,Object> paramId = new HashMap<>();
		
		SQL sql = new SQL();
		entradaLiquidarComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaLiquidarComprobante.p_IdUsuario = entrada.p_IdUsuario;
		entradaLiquidarComprobante.p_IdModulo = "TES";
		entradaLiquidarComprobante.p_UsaReglasNegocio = "S";
		entradaLiquidarComprobante.p_SimularOperacion = "N";
		entrada.p_Lista.forEach(e -> {
			try {
				parametros.clear();
				parametros.put("IdComprobante", Long.parseLong(e.NumeroOperacionConcepto));
				List<ComprobanteLiquidar_Entrada> comprobante = sql.query(conn, SQL_DETALLE_COMPROBANTE, parametros, ComprobanteLiquidar_Entrada.class);
				entradaLiquidarComprobante.p_ComprobanteLiquidar.addAll(comprobante);
			} catch (Exception er) {
				logger.error("ERROR::autorizarEvento", er);
			}
		});
		try {
			entradaLiquidarComprobante.p_FechaMovimiento = Fecha.toStringDate(FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa));
		} catch (Exception er) {
			logger.error("ERROR::autorizarEvento", er);
		}
		
		try {
			
			for(Evento e:entrada.p_Lista) {
				inDatos.put("IdComprobante",e.NumeroOperacionConcepto);
				paramId.put("IdEvento", e.IdEvento);
			}
			
			List<EventoParaAutorizar> eventos = sql.query(conn, SQL_Consultaevento, inDatos, EventoParaAutorizar.class);
			String estadoComprobante="";
			List<EstadoComprobanteAutorizar>  ListestadoComprobante= sql.query(conn, SQL_ESTADOCOMPROBANTE, inDatos, EstadoComprobanteAutorizar.class);
			for(EstadoComprobanteAutorizar e:ListestadoComprobante) {
				estadoComprobante=e.IdEstado;
			}
			if(eventos.size()==2) {
				for(EventoParaAutorizar ev : eventos) {
					if(!(paramId.get("IdEvento").equals(ev.IdEvento)) && ev.IdEstado.equalsIgnoreCase("VIG")) {
						s.P_OUT_ERRORESCONTADOR = 0L;
						s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"OPERACION_AUTORIZADA","Operacion autorizada con exito, para continuar favor autorizar evento:"+ev.IdEvento));
						ResultadoAutorizacion res = new ResultadoAutorizacion();
						res.evento = paramId.get("IdEvento");
						res.resultado = EXITO_AUTORIZACION;
						s.P_OUT_RESULTADO.add(res);
					}else {
						if(!(paramId.get("IdEvento").equals(ev.IdEvento)) && ev.IdEstado.equalsIgnoreCase("AUT")
								&& !(estadoComprobante.equals("LIQ"))
								) {
							LiquidarComprobante liquidar = new LiquidarComprobante();
							LiquidarComprobante_Salida salidaLiquidacion = liquidar.logica(conn, entradaLiquidarComprobante);
							
							for(MensajeSalida salida : salidaLiquidacion.P_OUT_MENSAJES) {
								hashSalidaConfirmar.put(salida.id, salida.tipo);
							}
							
							for(Evento evento : entrada.p_Lista) {
								if(hashSalidaConfirmar.containsKey(evento.NumeroOperacionConcepto) && !hashSalidaConfirmar.get(evento.NumeroOperacionConcepto).equals("ERROR")) {
									s.P_OUT_ERRORESCONTADOR = 0L;
									s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"OPERACION_AUTORIZADA","Operacion autorizada con exito"));
									ResultadoAutorizacion res = new ResultadoAutorizacion();
									res.evento = evento.IdEvento;
									res.resultado = EXITO_AUTORIZACION;
									s.P_OUT_RESULTADO.add(res);
								}else {
									s.P_OUT_ERRORESCONTADOR = salidaLiquidacion.P_OUT_ERRORESCONTADOR;
									s.P_OUT_MENSAJES = salidaLiquidacion.P_OUT_MENSAJES;
								}		
							}
						}
					}
				}
			}else {

				LiquidarComprobante liquidar = new LiquidarComprobante();
				LiquidarComprobante_Salida salidaLiquidacion = liquidar.logica(conn, entradaLiquidarComprobante);
				
				for(MensajeSalida salida : salidaLiquidacion.P_OUT_MENSAJES) {
					hashSalidaConfirmar.put(salida.id, salida.tipo);
				}
				
				for(Evento evento : entrada.p_Lista) {
					if(hashSalidaConfirmar.containsKey(evento.NumeroOperacionConcepto) && !hashSalidaConfirmar.get(evento.NumeroOperacionConcepto).equals("ERROR")) {
						s.P_OUT_ERRORESCONTADOR = 0L;
						s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"OPERACION_AUTORIZADA","Operacion autorizada con exito"));
						ResultadoAutorizacion res = new ResultadoAutorizacion();
						res.evento = evento.IdEvento;
						res.resultado = EXITO_AUTORIZACION;
						s.P_OUT_RESULTADO.add(res);
					}else {
						s.P_OUT_ERRORESCONTADOR = salidaLiquidacion.P_OUT_ERRORESCONTADOR;
						s.P_OUT_MENSAJES = salidaLiquidacion.P_OUT_MENSAJES;
					}		
				}
			}
		} catch (Exception e1) {
			s.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		logger.info("TERMINO:AUTORIZACION");
		return s;
    }
    
	public static class LiquidacionRechazo{
		public Long IdComprobante;

		public LiquidacionRechazo(Long idComprobante) {
			super();
			IdComprobante = idComprobante;
		}
	}
	
	public static class EstadoComprobanteAutorizar
	{
		public Long   		IdComprobante;
	   	public String 		IdEstado;

	   	public String toString() {
            return Utils.printJson(this, false);
        }
	}
	public static class EventoParaAutorizar
	{
		public Long   		IdEvento;
	   	public String 		IdTipoEvento;
	   	public String 	    NumeroOperacionConcepto;
	   	public String		IdEstado;

	   	public String toString() {
            return Utils.printJson(this, false);
        }
	}
    @Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
    	logger.info("INICIO:RECHAZO");
		SalidaAutorizacion salida = new SalidaAutorizacion();	
		
		SQL sql = new SQL();
		List<LiquidacionRechazo> liquidaciones = entrada.p_Lista.stream().map(liq -> new LiquidacionRechazo(Long.parseLong(liq.NumeroOperacionConcepto))).collect(Collectors.toList());
		HashMap<String,Object> inDatos = new HashMap<String,Object>();
		HashMap<String,Object> paramId = new HashMap<>();
		
		try {
			for(Evento e:entrada.p_Lista) {
				paramId.put("IdEvento",e.IdEvento);
			}
			for (LiquidacionRechazo com: liquidaciones) {
				inDatos.put("IdComprobante", com.IdComprobante);
			}
			List<EventoParaAutorizar> eventos = sql.query(conn, SQL_Consultaevento, inDatos, EventoParaAutorizar.class);
			String estadoextra="";
			if(!eventos.isEmpty()) {
				for(EventoParaAutorizar ev : eventos) {
					if(!(paramId.get("IdEvento").equals(ev.IdEvento))) {
						//Rechazo
						logger.info("ID DIFERENTE");
						estadoextra=ev.IdEstado;
					 // Obtiene datos de etapas

			            Timestamp vFechaHoraSistema =  FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
			            
			            HashMap<String, Object> parmsEtapaEvts = new HashMap<String, Object>();
			            parmsEtapaEvts.put("ListaIdEventos", ev.IdEvento);
			            List<EtapaEventoPrc> listaEtapaEvento = sql.query(conn, SQL_ConsultaEtapaevento, parmsEtapaEvts, EtapaEventoPrc.class);

			            HashMap<String, Object> parmsAutEtapaEvts = new HashMap<String, Object>();
			            parmsAutEtapaEvts.put("ListaIdEventos", eventos);
			            //List<AutorizadoEtapaEvento> listaAutEtapaEvento = sql.query(conn, SQL_ConsultaAutorizadoEtapaEvento, parmsAutEtapaEvts, AutorizadoEtapaEvento.class);

			            List<UpdateEventoPorEtapa> updateEvento = new ArrayList<>();
			            List<UpdateEtapaEvento> updateEtapaEvento = new ArrayList<>();
			            List<InsertAutorizadoEtapaEvento> insertAutEtapaEvento = new ArrayList<>();
			     
			            // Procesa IdEtapaEvento enviadas
			            for (EtapaEventoPrc eta : listaEtapaEvento) {
			            	//if(estadoextra.equalsIgnoreCase("VIG")) {
				            	InsertAutorizadoEtapaEvento aev = new InsertAutorizadoEtapaEvento();
		                        aev.IdAutorizadoEtapaEvento = new Long(0);
		                        aev.IdEtapaEvento = eta.IdEtapaEvento;
		                        aev.IdUsuario = entrada.p_IdUsuario;
		                        aev.IdEstado ="RZD";
		                        aev.FechaHoraCambioEstado = vFechaHoraSistema;
		                        aev.Observaciones = "";
		                        insertAutEtapaEvento.add(aev);
			            	//}
			            	
	                        // cambia estado a etapa != 'AUT'
	                        UpdateEtapaEvento eve = new UpdateEtapaEvento();
	                        eve.IdEtapaEvento = eta.IdEtapaEvento;
	                        eve.IdEstado = "RZD";
	                        eve.FechaHoraCambioEstado = vFechaHoraSistema;
	                        eve.IdUsuarioCambioEstado = entrada.p_IdUsuario;
	                        updateEtapaEvento.add(eve);
	                        logger.debug("Usuario " + entrada.p_IdUsuario + " cambia etapa != AUT" +  " " + eve.toString());
	                    
		                    // Candidato a autorizar evento
		                    UpdateEventoPorEtapa evee = new UpdateEventoPorEtapa();
		                    evee.IdEvento = eta.IdEvento;
		                    evee.IdUsuarioCambioEstado = entrada.p_IdUsuario;
		                    evee.FechaHoraCambioEstado = vFechaHoraSistema;
		                    updateEvento.add(evee);

			                
			            }


			                // Actualiza en BD Autorizador			       
			                sql.batch(conn, SQL_InsertaAutorizadoEtapaEvento, insertAutEtapaEvento, InsertAutorizadoEtapaEvento.class);
			           
			                //sql.batch(conn, SQL_ActualizaAutorizadoEtapaEvento, actualizaAutEtapaEvento, InsertAutorizadoEtapaEvento.class);

			                // Actualiza en BD EtapaEvento
			                sql.batch(conn, SQL_ActualizaEtapaEvento, updateEtapaEvento, UpdateEtapaEvento.class);

			                // Actualiza en BD Evento procesados
			                sql.batch(conn, SQL_ActualizaeventoPorEtapa, updateEvento, UpdateEventoPorEtapa.class);
			               // conn.commit();
			                

					}
				}
				sql.batch(conn, SQL_RECHAZO_COMPROBANTE, liquidaciones, LiquidacionRechazo.class);
			}else {
				sql.batch(conn, SQL_RECHAZO_COMPROBANTE, liquidaciones, LiquidacionRechazo.class);
			}
		} catch (SQLException e1) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"OPERACION_NO_SE_PUEDE_ANULAR","Operacion no se puede Anular"));
			e1.printStackTrace();
		} catch (Exception e1) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"OPERACION_NO_SE_PUEDE_ANULAR","Operacion no se puede Anular"));
			e1.printStackTrace();
		}
		logger.info("TERMINO:RECHAZO");
		return salida;	

	}
    
    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
        	AutorizacionLiquidacion e = new AutorizacionLiquidacion();
        	
            Evento evento = new Evento();
            evento.NumeroOperacionConcepto = "8725";
            
            EntradaAutorizacion entrada = new EntradaAutorizacion();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdModulo = "PLA";
            entrada.p_IdUsuario = 6L;
			entrada.p_Lista.add(evento);
			System.out.println(e.autorizarEvento(conn, entrada));
			System.out.println(e.rechazarEvento(conn, entrada));
			conn.rollback();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
