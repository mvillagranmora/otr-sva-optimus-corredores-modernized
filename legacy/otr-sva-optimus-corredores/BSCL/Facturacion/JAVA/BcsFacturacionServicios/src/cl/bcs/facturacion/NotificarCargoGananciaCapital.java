package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonObject;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.ConvertirMontoEscrito;
import java.text.SimpleDateFormat;

public class NotificarCargoGananciaCapital implements JavaCall {

	private static final Logger logger = LogManager.getLogger(NotificarCargoGananciaCapital.class);
	private final String SQL_SELECT_CONSULTAR_CARGO_GANANCIA_CAPITAL = loadResourceToString("cl/bcs/facturacion/sql/NotificarCargoGananciaCapital_SeleccionComprobanteCargoGananciaCapital.sql");
	private final String SQL_SELECT_DATOS_CORREDORA = loadResourceToString("cl/bcs/facturacion/sql/NotificarCargoGananciaCapital_SeleccionDatosCorredora.sql");
	private final String SQL_SELECT_RETENCION_IGK = loadResourceToString("cl/bcs/facturacion/sql/NotificarCargoGananciaCapital_SeleccionRetencionIGK.sql");
	
	public static class NotificarCargoGananciaCapital_Entrada extends BcsPojo{
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_FechaEjecucion = "0001-01-01";  // Si viene 0001-01-01  toma fecha de sistema
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
    /* Notificaciones */
    public static class ObjNotificaciones{	
    	//Datos Plantilla Correo 	
    	public String CLIENTE;
    	public String RUT;
    	public String CUENTA;
    	public Date FECHA;
    	public String TIPO_MONEDA;
    	public String NOMBRE_MONEDA;
    	public BigDecimal RETENCION_GANANCIA_CAP;
    	public BigDecimal OPERACION_VENTA;
    	public BigDecimal IMPORTE_NETO;
    	public Long NUMERO_COMPROBANTE;
    	public Date FECHA_FACTURACION;
    	public BigDecimal RETENCION_DIAS;
    	//Datos Correo
    	public String CORREO_ELECTRONICO;
    	public Long IdPersona;
    	public Long IdDireccionElectronica;    	
    	public String PaisMoneda;
       				 
	}  
    
    //Informacion Corredora
    public static class ObjCorredora{	
    	public String CORREDORA;    	
    	public String ATENCION_CLIENTE;
    	public Long IdEmpresa;
    	public String Direccion;
    	public String DocumentoIdentidad;       				 
	} 
    
  //Parametrizado Porcentaje de retencion
    public static class ObjRetencion{	
    	public Long IdEmpresa;    	
    	public String CodigoParametro;//IGK_POR_RETEN_NORESIDENTE
    	public String Descripcion;
    	public String Valor;
    	public String EsModificableUsuario;   
    	public String Uso;  
    	public String ValorDefecto;  
    	public String ValoresPosibles;  
    	public String ModificablePorUsuario;  
    	public String ModificablePostProduccion; 
    	public String CreadorParametro;
    	public Date FechaCreacion;
    	public String ModificadorParametro;
    	public Date FechaModificacion;
    	public String Observaciones;
	} 
	
	
	public static class NotificarCargoGananciaCapital_Salida{
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public NotificarCargoGananciaCapital_Salida()
		{
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}	
	// ---------------------------------------------------
	// LOGICA
	// ---------------------------------------------------
	
	public NotificarCargoGananciaCapital_Salida logica(Connection con, NotificarCargoGananciaCapital_Entrada in) throws Exception {
		
		logger.info(":: INICIO NotificarCargoGananciaCapital ::");		
		NotificarCargoGananciaCapital_Salida out = new NotificarCargoGananciaCapital_Salida();		
		// obtener notificaciones
		List<ObjNotificaciones> listaNotificaciones = obtenerListaNotificaciones (con , in);
		// obtener datos corredora
		ObjCorredora datosCorredora = obtenerDatosCorredora(con , in);
		//obtener datos porcentaje de retencion
		ObjRetencion datosPorcentaje = obtenerRetencionIgk(con,in);
		
		String 	tFechaHoraSistema 	= FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
		Timestamp vFechaHoraSistema = Fecha.toTimeStamp(tFechaHoraSistema);
			
		if(listaNotificaciones.isEmpty()) {		
			logger.info("Lista notificaciones vacia "+in.p_FechaEjecucion);
			out.P_OUT_ERRORESCONTADOR = 404L;
			return out;
		}else
		{
			logger.info("Lista notificaciones cargada");
			//Agregar notificacion
			AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada(); //hay otra clase que usa enviar notificacion
			notifIn.P_IDEMPRESA 		= in.p_IdEmpresa;
	        notifIn.P_IDUSUARIO 		= in.p_IdUsuario;
	        notifIn.P_IDMODULO 			= in.p_IdModulo;
	        notifIn.P_EVENTOS 			= new ArrayList<>();
	        notifIn.P_VALORES 			= new ArrayList<>();
	        notifIn.P_ARCHIVOSADJUNTOS 	= new ArrayList<>();
	        notifIn.P_IDDIRECCIONES 	= new ArrayList<>();			
            
            long contador = 0;
            for(ObjNotificaciones listaIgk : listaNotificaciones) {
            	//validar cliente posea un correo predeterminado
            	if(listaIgk.CORREO_ELECTRONICO == null) {
            		
            		out.P_OUT_MENSAJES.add(new MensajeSalida(new Long(listaIgk.IdPersona),
        					MensajeSalida.MENSAJE_ERROR, "Configuracion CLIENTE "+listaIgk.CLIENTE,
        					"Debe agregar un correo predeterminado al cliente."));
            		
            	}else {
            		// Evento de notificacion
        	        AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
                    evt.CodigoTipoEvento 		= "NOTIFICAR_GANANCIA_CAP";
                    evt.IdSecuencia 			= contador;
                    evt.FechaCreacionSistema 	= Fecha.toStringDate(vFechaHoraSistema);            
                    evt.IdPersonaRemitente 		= 102505L; //mantener igual que el de abajo pero valido
                    evt.IdPersonaDestinatario 	= listaIgk.IdPersona;
                    evt.IdPortafolio 			= (long) 0;
                    notifIn.P_EVENTOS.add(evt);
                    logger.info("Evento notificacion cargado");
                    
        	      //Objetos y llenado para salida de datos Detalle        	       
                    
        	        notifIn.addValor(contador, "NUMERO_COMPROBANTE", listaIgk.NUMERO_COMPROBANTE+"")
             	   .addValor(contador, "CLIENTE", listaIgk.CLIENTE+"")
             	   .addValor(contador, "RUT", listaIgk.RUT+"")
             	   .addValor(contador, "CUENTA", listaIgk.CUENTA+"")
             	   .addValor(contador, "FECHA", new SimpleDateFormat("yyyy-MM-dd").format(listaIgk.FECHA)+"")
             	   .addValor(contador, "CORREDORA", datosCorredora.CORREDORA+"")
             	   .addValor(contador, "PORCENTAJE", datosPorcentaje.Valor+"")
             	   .addValor(contador, "TIPO_MONEDA", listaIgk.TIPO_MONEDA+"")
             	   .addValor(contador, "NOMBRE_MONEDA", listaIgk.NOMBRE_MONEDA+"")
             	   .addValor(contador, "RETENCION_GANANCIA_CAP", listaIgk.RETENCION_GANANCIA_CAP.longValue()+"")
             	   .addValor(contador, "OPERACION_VENTA", listaIgk.OPERACION_VENTA.longValue()+"")
             	   .addValor(contador, "FECHA_FACTURACION", new SimpleDateFormat("yyyy-MM-dd").format(listaIgk.FECHA_FACTURACION)+"")
             	   .addValor(contador, "IMPORTE_NETO", listaIgk.IMPORTE_NETO.longValue()+"")
             	   .addValor(contador, "RETENCION_DIAS", listaIgk.RETENCION_DIAS.longValue()+"")
             	   .addValor(contador, "ATENCION_CLIENTE", datosCorredora.ATENCION_CLIENTE+"")
             	   .addValor(contador, "MONTO_TEXTO", convertirMontoEscrito(listaIgk.IMPORTE_NETO)+"");
        	        
        	        
        	        //Agrega correo electronico
        	        AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
    				ndir.IdSecuencia 		= contador;
    	            ndir.TipoDestinatario 	= "TO";
    	            ndir.IdDireccion 		= listaIgk.IdDireccionElectronica;	
    	            notifIn.P_IDDIRECCIONES.add(ndir);
    	            logger.info("agrega correo electronico");
        	        
            	}
            	contador++;
            }
            
            if (notifIn.P_EVENTOS.size() > 0) {
            	
            	logger.info("Hay "+ notifIn.P_EVENTOS.size() +" notificaciones para enviar");
            	logger.info("Se Llama a Servicio AgregarEvento");
            	
                AgregarEvento notif = new AgregarEvento();
                AgregarEvento.ServiceSalida notifOut = notif.logica(con, notifIn);
                
                out.P_OUT_MENSAJES.add(new MensajeSalida(new Long(notifIn.P_IDEMPRESA),
    					MensajeSalida.MENSAJE_INFORMACION, notifOut.P_OUT_IDEVENTO.toString(),
    					"resultado clase Agregar Evento."));

                logger.info("Finaliza Llamada a Servicio AgregarEvento");
            }else{
            	logger.info("P_EVENTOS vacio");
            }
            
			out.P_OUT_ERRORESCONTADOR = 0L;
		}		
		
		return out;
	}
	

	private List<ObjNotificaciones> obtenerListaNotificaciones(Connection conn, NotificarCargoGananciaCapital_Entrada entrada)  {
		logger.debug("INICIO::NotificarCargoGananciaCapital.obtenerListaNotificaciones");
		long inicio = System.currentTimeMillis();
		List<ObjNotificaciones> listaNotificaciones = new ArrayList<ObjNotificaciones>();
		
		SQL sql = new SQL();		
		try {
			HashMap<String,Object> paramIN = new HashMap<>();
			paramIN.put("p_IdEmpresa", entrada.p_IdEmpresa);
			paramIN.put("p_FechaEjecucion" , entrada.p_FechaEjecucion);	
			
			listaNotificaciones = sql.query(conn, SQL_SELECT_CONSULTAR_CARGO_GANANCIA_CAPITAL, paramIN, ObjNotificaciones.class);
									
		}catch (Exception e) {
			e.printStackTrace();			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::NotificarCargoGananciaCapital.obtenerListaNotificaciones "+ fin +" - "+ inicio);
		return listaNotificaciones;
	}
	
	
	//Obtener datos de retencion IGK
	private ObjRetencion obtenerRetencionIgk(Connection conn, NotificarCargoGananciaCapital_Entrada entrada)  {
		logger.debug("INICIO::NotificarCargoGananciaCapital.obtenerRetencionIgk");
		long inicio = System.currentTimeMillis();
		ObjRetencion datos = new ObjRetencion();
		
		SQL sql = new SQL();		
		
		try {
			HashMap<String,Object> paramIN = new HashMap<>();
			paramIN.put("p_IdEmpresa", entrada.p_IdEmpresa);
			paramIN.put("p_CodigoParametro","IGK_POR_RETEN_NORESIDENTE");
			datos = sql.query(conn, SQL_SELECT_RETENCION_IGK, paramIN, ObjRetencion.class).get(0);									
		}catch (Exception e) {
			e.printStackTrace();			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::NotificarCargoGananciaCapital.obtenerRetencionIgk "+ fin +" - "+ inicio);
		return datos;
	}
	
	//Obtener datos de corredora
	private ObjCorredora obtenerDatosCorredora(Connection conn, NotificarCargoGananciaCapital_Entrada entrada)  {
			logger.debug("INICIO::NotificarCargoGananciaCapital.obtenerDatosCorredora");
			long inicio = System.currentTimeMillis();
			ObjCorredora datosCorredora = new ObjCorredora();
			
			SQL sql = new SQL();		
			
			try {
				HashMap<String,Object> paramIN = new HashMap<>();
				paramIN.put("p_IdEmpresa", entrada.p_IdEmpresa);			
				datosCorredora = sql.query(conn, SQL_SELECT_DATOS_CORREDORA, paramIN, ObjCorredora.class).get(0);									
			}catch (Exception e) {
				e.printStackTrace();			
			}
			
			long fin = System.currentTimeMillis();
			logger.debug("FIN::NotificarCargoGananciaCapital.obtenerDatosCorredora "+ fin +" - "+ inicio);
			return datosCorredora;
	}
	
	
	 
	private String convertirMontoEscrito(BigDecimal monto) {
		logger.debug("INICIO::NotificarCargoGananciaCapital.convertirMontoEscrito");
		long inicio = System.currentTimeMillis();
		String montoEscrito = "";
		try {				 
			 montoEscrito = ConvertirMontoEscrito.cantidadConLetra(monto.toString());
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::NotificarCargoGananciaCapital.convertirMontoEscrito "+ fin +" - "+ inicio);
		return montoEscrito;
	}
	// ---------------------------------------------------
	// CALL
	// ---------------------------------------------------

	@Override	public JsonObject call(Connection conn, JsonObject in) throws Exception {
	return null;
	}
		
	// ---------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ---------------------------------------------------

	public static void main(String[] args){
		
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			NotificarCargoGananciaCapital sp = new NotificarCargoGananciaCapital();
			NotificarCargoGananciaCapital_Entrada in = new NotificarCargoGananciaCapital_Entrada();
			in.p_IdEmpresa = Long.parseLong("1");
			in.p_IdModulo = "PLA";
			in.p_IdUsuario = Long.parseLong("4");
			in.p_FechaEjecucion = "2022-08-08";
			
			NotificarCargoGananciaCapital_Salida out = sp.logica(con, in);
			con.commit();
			
			System.out.println("ENTRADA");
			System.out.println(in);
			System.out.println("SALIDA");
			System.out.println(out.P_OUT_MENSAJES);
			
			//con.rollback();
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}

}
