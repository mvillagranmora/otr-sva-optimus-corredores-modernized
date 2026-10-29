package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarCartaLiquidacion implements JavaCall{
	
	static final Logger logger											= LogManager.getLogger(NotificarCartaLiquidacion.class);
	private final String SQL_SELECT_CLIENTES 							= loadResourceToString("cl/bcs/mesa/FWD/sql/NotificarCartaLiquidacion_SelectInformacionClientes.sql");
	private final String SQL_SELECT_CARTA_LIQUIDACION 					= loadResourceToString("cl/bcs/mesa/FWD/sql/NotificarCartaLiquidacion_SelectInformacionCartaLiquidacion.sql");
	private final String SQL_INSERT_INFO_CARTA_LIQUIDACION_TABLA_PASO 	= loadResourceToString("cl/bcs/mesa/FWD/sql/NotificarCartaLiquidacion_InsertInfoCartasLiquidacionTablaPaso.sql");
	private final String SQL_DELETE_INFO_CARTA_LIQUIDACION_TABLA_PASO 	= loadResourceToString("cl/bcs/mesa/FWD/sql/NotificarCartaLiquidacion_DeleteInfoCartasLiquidacionTablaPaso.sql");

	public static class EntradaNotificarCartaLiquidacion extends BcsPojo {
		
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String 					p_Fecha;
		public List<CartaANotificar> 	p_CartaANotificar;
		
		public EntradaNotificarCartaLiquidacion(){
			
			this.p_CartaANotificar 	= new ArrayList<CartaANotificar>();
			
		}
		
	}
	
	public static class CartaANotificar extends BcsPojo {
		
		public Long 	p_IdCartera;
		public Long 	p_IdOperacion;
		public Long 	p_FolioOperacion;
		public String 	p_FechaOperacion;
		public String	p_IdEstado;

	}
	
	public static class SalidaNotificarCartaLiquidacion extends BcsPojo{
		
		public Long 					P_OUT_ERRORESCONTADOR;
    	public List<MensajeSalida> 		P_OUT_MENSAJES;
		
		public SalidaNotificarCartaLiquidacion() {
			
			this.P_OUT_ERRORESCONTADOR 	= 0L;
    		this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			
		}
	
		
	}
	
	public static class InfoNotificaciones extends BcsPojo{
		
		public Long 	  IdCartera;
		public Long 	  IdClientePersona;
		public Long 	  IdPortafolioCliente;
		public Long	      IdCorreoCliente;
		public String	  Cliente;	
		public String 	  Corredora;
		public String 	  AtencionCliente;
		
	}
	
	public static class CartaDeLiquidacion extends BcsPojo {
		
		  public Long IdCartera;
		  public String TipoOperacion;
		  public String TipoVencimiento;
		  public String IdMonedaPrincipal;
		  public String IdMonedaSecundaria;
		  public BigDecimal MontoMonedaPrincipal;
		  public BigDecimal ValorCampoTCFWD;
		  public BigDecimal PrecioReferenciaDO;
		  public BigDecimal PrecioReferencia;
		  public String ModalidadLiquidacion;
		  public String NombreMedioDePagoEntregamos;
		  public String NombreMedioDePagoRecibimos;
		  public BigDecimal MontoSaldo1;
		  public String IdMoneda1;
		  public String AFavor1;
		  public BigDecimal MontoSaldo2;
		  public String IdMoneda2;
		  public String AFavor2;
		  public String FechaEntregamos;
		  public String FechaRecibimos;
				
	}
	
	public static class TablaPaso extends BcsPojo {
		
		public Long IdTrabajo;
		public Long IdCartera;
		public Long IdOperacion;
		public Long FolioOperacion;
		public String FechaOperacion;
		public String IdEstado;

	}
	
	public static class TablaPasoEliminar extends BcsPojo {
		
		public Long IdTrabajo;

	}
	

	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		
		Gson gson = getGsonBuilder();				
		EntradaNotificarCartaLiquidacion in=gson.fromJson(jin.toString() ,EntradaNotificarCartaLiquidacion.class);
		SalidaNotificarCartaLiquidacion out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
		
	}
	
	public SalidaNotificarCartaLiquidacion logica(Connection con, EntradaNotificarCartaLiquidacion in) throws Exception{
		
		logger.info("INICIO::NotificarCartaLiquidacion");
		
		SQL sql 							= new SQL();
		SalidaNotificarCartaLiquidacion out = new SalidaNotificarCartaLiquidacion();
		Long idTrabajo 						= new Double(Math.random()*10000000000L).longValue();
		HashMap<String, Object> params 		= new HashMap<String, Object>();
		Long inicio 						= System.currentTimeMillis();
		
	    List<Long> idCarteras = this.insertarTablaPaso(con, in.p_CartaANotificar, idTrabajo);
		
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("IdCartera", idCarteras);
		
		List<InfoNotificaciones> infoClientes = sql.query(con, SQL_SELECT_CLIENTES, params, InfoNotificaciones.class);
		
		params.clear();
    	params.put("IdTrabajo", idTrabajo);
		params.put("IdEmpresa", in.p_IdEmpresa);
		
		List<CartaDeLiquidacion> cartasLiquidacion = sql.query(con, SQL_SELECT_CARTA_LIQUIDACION, params, CartaDeLiquidacion.class);
		
		if (cartasLiquidacion.size() == 0) {
			
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_NO_HAY_CARTAS_LIQUIDACION_PARA_NOTIFICAR",null,con,in.p_IdEmpresa));				
			return out;
			
		}
		
		try {
			
			this.generarNotificacion(con, in, cartasLiquidacion, infoClientes);
			
		} catch (Exception err) {
			
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_NOTIFICACION_CARTA_LIQUIDACION",null,con,in.p_IdEmpresa));				
			return out;
			
		} finally {
			
	    	this.eliminarTablaPaso(con, idTrabajo);

		}
    	
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_NOTIFICACION_CARTA_LIQUIDACION",null,con,in.p_IdEmpresa));				
	    Long fin = System.currentTimeMillis();
        logger.info("FIN::NotificarCartaLiquidacion: ", fin - inicio);
		
		return out;
		
	}
	
	private List<Long> insertarTablaPaso (Connection con, List<CartaANotificar> cartas, Long idTrabajo) throws Exception {
		
        logger.info("INICIO::NotificarCartaLiquidacion.insertTablaPaso");
        
		HashMap<String, Object> params 		= new HashMap<String, Object>();
		SQL sql 							= new SQL();
		Long inicio 						= System.currentTimeMillis();
	    List<Long> idCarteras 				= new ArrayList<Long>();

        for( CartaANotificar carta : cartas ) {
			
        	idCarteras.add(carta.p_IdCartera);
			
			TablaPaso datosTablaPaso 				= new TablaPaso();
			datosTablaPaso.IdTrabajo 				= idTrabajo;
			datosTablaPaso.IdCartera 				= carta.p_IdCartera;
			datosTablaPaso.IdOperacion 				= carta.p_IdOperacion;
			datosTablaPaso.FolioOperacion 			= carta.p_FolioOperacion;
			datosTablaPaso.FechaOperacion 			= carta.p_FechaOperacion;
			datosTablaPaso.IdEstado 				= carta.p_IdEstado;
			List<TablaPaso> listaDatosTablaPaso 	= new ArrayList<TablaPaso>();
			listaDatosTablaPaso.add(datosTablaPaso);
			
	    	sql.batch(con, SQL_INSERT_INFO_CARTA_LIQUIDACION_TABLA_PASO, listaDatosTablaPaso, TablaPaso.class);
	    	params.clear();
	    	
	    }

	    Long fin = System.currentTimeMillis();
        logger.info("FIN::NotificarCartaLiquidacion.insertTablaPaso: ", fin - inicio);

		return idCarteras;
	}
	
	private void eliminarTablaPaso (Connection con, Long idTrabajo) throws Exception {
		
        logger.debug("INICIO::NotificarCartaLiquidacion.eliminarTablaPaso");
        
		SQL sql 										= new SQL();
		Long inicio 									= System.currentTimeMillis();

		TablaPasoEliminar idTrabajoEliminar 			= new TablaPasoEliminar();
    	idTrabajoEliminar.IdTrabajo 					= idTrabajo;
		List<TablaPasoEliminar> listaIdTrabajoEliminar 	= new ArrayList<TablaPasoEliminar>();
		listaIdTrabajoEliminar.add(idTrabajoEliminar);
		
        sql.batch(con, SQL_DELETE_INFO_CARTA_LIQUIDACION_TABLA_PASO, listaIdTrabajoEliminar, TablaPasoEliminar.class);
		
	    Long fin = System.currentTimeMillis();
        logger.debug("FIN::NotificarCartaLiquidacion.eliminarTablaPaso: ", fin - inicio);

	}
	
	private void generarNotificacion (Connection con, EntradaNotificarCartaLiquidacion in, List<CartaDeLiquidacion> cartasLiquidacion, List<InfoNotificaciones> infoClientes) throws Exception {
		
		
		logger.info("INICIO::NotificarCartaLiquidacion.generarNotificacion");
		
		
		Long inicio 										= System.currentTimeMillis();
		String tFechaHoraSistema 							= FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
	    Timestamp vFechaHoraSistema 						= Fecha.toTimeStamp(tFechaHoraSistema);
        String rutaLogoCorredora = "";
        ConsultarImagenEntrada imagenCorredoraIn = new ConsultarImagenEntrada();
        imagenCorredoraIn.p_IdEmpresa = in.p_IdEmpresa;
        imagenCorredoraIn.p_imagenNombres.add("LOGO_CORREDOR");
        ConsultarImagenSalida imagenCorredoraOut = new ConsultarImagen().logica(con, imagenCorredoraIn);
        if(imagenCorredoraOut.OUT_IMAGENES.size() > 0) {
        	rutaLogoCorredora = imagenCorredoraOut.OUT_IMAGENES.get(0).RutaPublica;        	
        }
	    SimpleDateFormat fechaFormatoSalida 				= new SimpleDateFormat("dd-MM-yyyy");
	    SimpleDateFormat fechaFormatoEntrada 				= new SimpleDateFormat("yyyy-MM-dd");
	    String fechaSistemaFormateada 						= fechaFormatoSalida.format(fechaFormatoEntrada.parse(in.p_Fecha));
	    
		AgregarEvento.ServiceEntrada entradaNotificacion	= new AgregarEvento.ServiceEntrada();
		entradaNotificacion.P_IDEMPRESA 					= in.p_IdEmpresa;
        entradaNotificacion.P_IDUSUARIO 					= in.p_IdUsuario;
        entradaNotificacion.P_IDMODULO 						= "FWD";
        entradaNotificacion.P_EVENTOS 						= new ArrayList<>();
        entradaNotificacion.P_VALORES 						= new ArrayList<>();
        entradaNotificacion.P_ARCHIVOSADJUNTOS 				= new ArrayList<>();
        entradaNotificacion.P_IDDIRECCIONES 				= new ArrayList<>();
        
        for ( CartaDeLiquidacion cartaLiquidacion: cartasLiquidacion ) {
        	
        	List<InfoNotificaciones> infoCliente 	= infoClientes.stream()
        												.filter(item -> item.IdCartera.equals(cartaLiquidacion.IdCartera))
        												.collect(Collectors.toList()); 
        	
            AgregarEvento.ObjEVENTOS evento 		= new AgregarEvento.ObjEVENTOS();
            evento.CodigoTipoEvento 				= "NOTIF_CARTA_LIQUIDACION";
            evento.IdSecuencia 						= infoCliente.get(0).IdCartera;
            evento.FechaCreacionSistema 			= Fecha.toStringDate(vFechaHoraSistema);            
            evento.IdPersonaRemitente 				= 0L;
            evento.IdPersonaDestinatario 			= infoCliente.get(0).IdClientePersona;            
            evento.IdPortafolio 					= infoCliente.get(0).IdPortafolioCliente;
            entradaNotificacion.P_EVENTOS.add(evento);
            
            AgregarEvento.ObjIDDIRECCIONES direccionElectronica = new AgregarEvento.ObjIDDIRECCIONES();
            direccionElectronica.IdSecuencia 					= infoCliente.get(0).IdCartera;
            direccionElectronica.TipoDestinatario				= "TO";
            direccionElectronica.IdDireccion 					= infoCliente.get(0).IdCorreoCliente;
            entradaNotificacion.P_IDDIRECCIONES.add(direccionElectronica);
            
            entradaNotificacion
				.addValor(cartaLiquidacion.IdCartera, "TIPOOPERACION", cartaLiquidacion.TipoOperacion)
	            .addValor(cartaLiquidacion.IdCartera, "FECHA", fechaSistemaFormateada)
				.addValor(cartaLiquidacion.IdCartera, "TIPOVENCIMIENTO", cartaLiquidacion.TipoVencimiento)
				.addValor(cartaLiquidacion.IdCartera, "MONEDAPRINCIPAL", cartaLiquidacion.IdMonedaPrincipal)
				.addValor(cartaLiquidacion.IdCartera, "MONEDASECUNDARIA", cartaLiquidacion.IdMonedaSecundaria)
				.addValor(cartaLiquidacion.IdCartera, "MONTOMONEDAPRINCIPAL", cartaLiquidacion.MontoMonedaPrincipal.toPlainString())
				.addValor(cartaLiquidacion.IdCartera, "VALORCAMPOTCFWD", cartaLiquidacion.ValorCampoTCFWD.toPlainString())
				.addValor(cartaLiquidacion.IdCartera, "PRECIOREFERENCIADO", cartaLiquidacion.PrecioReferenciaDO.toPlainString())
				.addValor(cartaLiquidacion.IdCartera, "PRECIOREFERENCIA", cartaLiquidacion.PrecioReferencia.toPlainString())
				.addValor(cartaLiquidacion.IdCartera, "MODALIDADLIQUIDACION", cartaLiquidacion.ModalidadLiquidacion)
				.addValor(cartaLiquidacion.IdCartera, "NOMBREMEDIODEPAGOENTREGAMOS", cartaLiquidacion.NombreMedioDePagoEntregamos)
				.addValor(cartaLiquidacion.IdCartera, "NOMBREMEDIODEPAGORECIBIMOS", cartaLiquidacion.NombreMedioDePagoRecibimos)
				.addValor(cartaLiquidacion.IdCartera, "MONTOSALDO1", cartaLiquidacion.MontoSaldo1.toPlainString())
				.addValor(cartaLiquidacion.IdCartera, "IDMONEDA1", cartaLiquidacion.IdMoneda1)
				.addValor(cartaLiquidacion.IdCartera, "AFAVOR1", cartaLiquidacion.AFavor1)
				.addValor(cartaLiquidacion.IdCartera, "MONTOSALDO2", cartaLiquidacion.MontoSaldo2.toPlainString())
				.addValor(cartaLiquidacion.IdCartera, "IDMONEDA2", cartaLiquidacion.IdMoneda2)
				.addValor(cartaLiquidacion.IdCartera, "AFAVOR2", cartaLiquidacion.AFavor2)
				.addValor(cartaLiquidacion.IdCartera, "FECHAENTRAGAMOS", cartaLiquidacion.FechaEntregamos)
				.addValor(cartaLiquidacion.IdCartera, "FECHARECIBIMOS", cartaLiquidacion.FechaRecibimos)
	    		.addValor(cartaLiquidacion.IdCartera, "CLIENTE", infoCliente.get(0).Cliente)
	    		.addValor(cartaLiquidacion.IdCartera, "CORREDORA", infoCliente.get(0).Corredora)
	    		.addValor(cartaLiquidacion.IdCartera, "ATENCIONCLIENTE", infoCliente.get(0).AtencionCliente)
	    		.addValor(cartaLiquidacion.IdCartera, "LOGO_PUBLICO", rutaLogoCorredora);
            
        }
        
        AgregarEvento notif = new AgregarEvento();
        AgregarEvento.ServiceSalida salidaNotificacion = notif.logica(con, entradaNotificacion);
        
        for(AgregarEvento.ObjIDEVENTO notificacion: salidaNotificacion.P_OUT_IDEVENTO ) {
        	
        	logger.info("Evento Creado Id Evento: ", notificacion.IdEvento.toString());
        }
        
	    Long fin = System.currentTimeMillis();
        logger.info("FIN::NotificarCartaLiquidacion.generarNotificacion: ", fin - inicio);
		
	}
	
	public static void main(String[] args){
		
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()){
			Gson gson = Utils.getGsonBuilder();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_Fecha\":\"2021-04-14\",\"p_CartaANotificar\":[{\"p_IdCartera\":1535035,\"p_IdOperacion\":20696571,\"p_FolioOperacion\":3457,\"p_FechaOperacion\":\"2021-03-04\",\"p_IdEstado\":\"LIQ\"}]}";
			EntradaNotificarCartaLiquidacion Objin = gson.fromJson(json, EntradaNotificarCartaLiquidacion.class);
			
			NotificarCartaLiquidacion demo = new NotificarCartaLiquidacion();		
			
			System.out.println("ENTRADA:");
			System.out.println(Objin.toString());
			
			
			try {
				SalidaNotificarCartaLiquidacion ObjOut= demo.logica(con, Objin);
				//con.commit();
				con.rollback();
				
				System.out.println("SALIDA:");
				System.out.println(ObjOut.toString());
				
			} catch(Exception er) {
				
				er.printStackTrace();	
				con.rollback();
				
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
	
}
