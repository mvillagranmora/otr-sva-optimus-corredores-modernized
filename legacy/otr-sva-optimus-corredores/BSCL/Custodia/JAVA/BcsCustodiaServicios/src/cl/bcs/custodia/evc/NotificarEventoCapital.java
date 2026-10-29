package cl.bcs.custodia.evc;

/** 
 * Proposito: Emitir la notificación para uno o más Eventos de Capital
 * Autor: ACESPEDES (migración desde SQL del sp "BSCL.Custodia.BD.Servicios::EVC.NotificarVariacionCapital")
 * Fecha Creacion: 15-11-2018
 **/ 

import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarEventoCapital implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(NotificarEventoCapital.class);
	
	public List<Long> secuenciaEventoOrigen = new ArrayList<>();
	public List<String> idTipoEventoOrigen = new ArrayList<>();
	public List<String> nemoEventoOrigen = new ArrayList<>();
	public List<String> tipoEventoOrigen = new ArrayList<>();

	private final String SQL_SELECT_CLIENTES_EVENTO_CAPITAL_A_NOTIFICAR 	= loadResourceToString("cl/bcs/custodia/sql/NotificarEventoCapital_SelectClientesEventoCapitalANotificar.sql");
	private final String SQL_SELECT_CLIENTES_EVENTO_CAPITAL_A_NOTIFICAR2 	= loadResourceToString("cl/bcs/custodia/sql/NotificarEventoCapital_SelectClientesEventoCapitalANotificar_Filtrado.sql");
	private final String SQL_SELECT_CLIENTES_EVENTO_CAPITAL_A_NOTIFICAR3 	= loadResourceToString("cl/bcs/custodia/sql/NotificarEventoCapital_SelectClientesEventoCapitalANotificar_EventoCliente.sql");
	private final String SQL_SELECT_DIRS 									= loadResourceToString("cl/bcs/custodia/sql/NotificarEventoCapital_SelectDirecciones.sql");
	private final String SQL_SELECT_REMITENTE_EVENTO_CAPITAL 				= loadResourceToString("cl/bcs/custodia/sql/NotificarEventoCapital_SelectRemitenteEventoCapital_EventoCliente.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_EVENTO 					= loadResourceToString("cl/bcs/custodia/sql/NotificarEventoCapital_InsertEventoCapitalEvento.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL							= loadResourceToString("cl/bcs/custodia/sql/NotificarEventoCapital_SelectEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL 							= loadResourceToString("cl/bcs/custodia/sql/NotificarEventoCapital_UpdateEventoCapital.sql");
	private final String SQL_SELECT_CONTROL_PROCESO							= loadResourceToString("cl/bcs/custodia/sql/ObtenerConfiguracionControlProceso.sql");
	private final String SQL_SELECT_DATOS_PORTAFOLIOS_SIN_NOTIFICACION		= loadResourceToString("cl/bcs/custodia/sql/NotificarEventoCapital_SelectPortafoliosSinNotificacion.sql");
	
	//-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE ENTRADA
	
	public static class NotificarEventoCapital_Entrada{
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String 			p_IdModulo;
		public String			p_EstadoEventoCapital;
		public Boolean			p_CambiaEstadoANotificado;
		public List<ObjEventoCapital_Entrada> 	p_IdsEventoCapital;  // EV a enviar notificaciones
		public List<ObjEventoCapitalCliente_Entrada> 	p_IdsEventoCapitalCliente;  // EV a enviar notificaciones
		
		public NotificarEventoCapital_Entrada(){
			this.p_EstadoEventoCapital = "PRO";
			this.p_IdsEventoCapital = new ArrayList<>();
			this.p_IdsEventoCapitalCliente = new ArrayList<>();
			this.p_CambiaEstadoANotificado = true;
		}
	    
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ObjEventoCapital_Entrada{
		public Long p_IdEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ObjEventoCapitalCliente_Entrada{
		public Long p_IdEventoCapitalCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class ObjEventoCapitalEvento{
		public String IdEventoCapitalEvento;
		public Long IdEventoCapital;
		public Long IdEvento;
		public Long IdSecuencia;
	}
	
	
		
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE SALIDA	
	
	
    public static class NotificarEventoCapital_Salida{	
    	public List<MensajeSalida> 		P_OUT_MENSAJES;
    	public Long 					P_OUT_ERRORESCONTADOR;
    	   
    	public NotificarEventoCapital_Salida(){
    		this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
    	}
    	
    	public String toString() {
    		return Utils.printJson(this, false);
    	}
    }
    
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS DE APOYO	
	
	public static class EventoCapitalCliente_Proceso
	{
		public Long 	IdEventoCapital;
		public Long 	IdEventoCapitalCliente;
		public String 	IdTipoEventoCapital;  //Ej.: PB / SU
		public String 	IdEstado;
		public Long     IdEmpresa;
		
		//Para validaciones
		public long 	errores 		= 0;
		public long 	advertencias 	= 0;
		public long 	autorizaciones 	= 0;
		public long 	ok 				= 0;
	}
	
	public static class ObjDirs {
		public Long 	  	IdPortafolio;
		public Integer 		IdDireccion;
		public String 	  	TipoDestinatario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class ObjDatosRemitente{
		public Long IdPersona;
		public Long IdDireccionElectronica;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}

	
	public static class ObjClienteANotificar{
		public Long IdEventoCapital;
		public Long IdSecuencia;
		public String Nombre;
		public String SocNeg;
		public String Port;
		public String CodigoPortafolio;
		public String PropositoPortafolio;
		public Long IdPortafolio;
		public String Fono;
		public String Mail;
		public String Agente;
		public String Fecha;
		public String Hora;
		public String Tipo;
		public String Familia;
		public String Pago;
		public String PagoMo;
		public String Monto;
		public String MontoCapital;
		public String MontoCapitalMo;
		public String MontoMo;
		public String TipoEventoCapital;
		public String FechaPago;
		public String Estado;
		public Long IdDireccionElectronicaPrincipal;
		public Long IdPersonaDestinatario;
		public String IdTipoInstrumento;
		public String Corredora;
		public String MailCorredora;
		public String MailAgente;
		public BigDecimal Factor;
		public String FactorS;
		public String AtencionCliente;
		public String Cantidad;
		public String Boveda;
		public Date FechaLimite;
		public Timestamp FechaCreacionSistema;
		public String CodigoTipoEvento;
		public String RazonSocial;
		public Integer IdProductoAgrupador;
		//Incorpora 2 campos adicionales para notificación CIERRE DV
		public String MontoRetencionISFUT;
		public String TasaEfectiva;
		public BigDecimal MontoImpuestoExtranjero;
		public BigDecimal GastoTransferencia;
		public String FechaString = "";
		public String FechaPagoString = "";
		public String FechaCorte = "";
		public String FonoAgente = "";
		public String CantidadNueva = "";

    	public String toString() {
    		return Utils.printJson(this, false);
    	}
		
	}
	
	
	public static class ObjRemitentePersona {
		public Long IdPersonaRemitente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class ControlProceso {
		public String ControlProceso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class ObjPortafolioSinNotificacion {
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String NombreCompleto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	
	public NotificarEventoCapital_Salida logica(Connection conn, NotificarEventoCapital_Entrada paramsIn) throws Exception 
	{
		String boveda;
		
		NotificarEventoCapital_Salida salida = new NotificarEventoCapital_Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		
		ObjDatosRemitente datosRemitente = new ObjDatosRemitente();
		datosRemitente.IdDireccionElectronica = 0L;
		datosRemitente.IdPersona = 0L;
		
		boolean filtraCliente = false;
		

		logger.info("== INICIO GENERACION NOTIFICACIONES EVENTO CAPITAL ==");
		logger.info("PARAMETROS DE ENTRADA : " + paramsIn.toString());
			
		if (paramsIn.p_IdsEventoCapital.size() == 0 && paramsIn.p_IdsEventoCapitalCliente.size() ==0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_ESPECIFICAN_EVENTOS_CAPITAL_A_PROCESAR",null,conn,paramsIn.p_IdEmpresa));				
			return salida;
		}
		
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,paramsIn.p_IdEmpresa,"PLATAFORMA","PLA.FechaSistema");
		String HoraSistema = FechaDB.ObtenerHoraSistemaLocal(conn,paramsIn.p_IdEmpresa);
		
		String tFechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(conn, paramsIn.p_IdEmpresa);
		Timestamp vFechaHoraSistema = Fecha.toTimeStamp(tFechaHoraSistema);
		String logoCorredorRutaPublica = "";
		ConsultarImagen consultarImagen = new ConsultarImagen();
		ConsultarImagenEntrada consultarImagenIn = new ConsultarImagenEntrada();
		consultarImagenIn.p_IdEmpresa = paramsIn.p_IdEmpresa;
		consultarImagenIn.p_imagenNombres.add("LOGO_CORREDOR");
		ConsultarImagenSalida consultarImagenOut = consultarImagen.logica(conn, consultarImagenIn);
		if (consultarImagenOut.OUT_IMAGENES.size() > 0 ) {
			logoCorredorRutaPublica = consultarImagenOut.OUT_IMAGENES.get(0).RutaPublica;
		}
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();

		List<Long> p_IdEventoCapital = new ArrayList<Long>();
		
		for (ObjEventoCapital_Entrada val:paramsIn.p_IdsEventoCapital){
			p_IdEventoCapital.add(val.p_IdEventoCapital);
		}
		
		params.clear();
        params.put("IdsEventoCapital", p_IdEventoCapital);
        params.put("EstadoEvento",paramsIn.p_EstadoEventoCapital);
		List<EventoCapital> eventosCapital = sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, params, EventoCapital.class);
		
		for (EventoCapital evento : eventosCapital) {
		    if (evento.IdEventoCapital != null) {
		        this.secuenciaEventoOrigen.add(evento.Secuencia);
		        this.idTipoEventoOrigen.add(evento.IdTipoEventoCapital);
		        this.nemoEventoOrigen.add(evento.Nemo);
		        this.tipoEventoOrigen.add(evento.TipoEventoCapital);
		    }
		}
		
		params.clear();
		params.put("IdUsuario", paramsIn.p_IdUsuario);
		List<ObjRemitentePersona> remitente = sql.query(conn, SQL_SELECT_REMITENTE_EVENTO_CAPITAL, params, ObjRemitentePersona.class);
		
		if (remitente.size() > 0) {
			datosRemitente.IdPersona = remitente.get(0).IdPersonaRemitente;
		}
		params.clear();
		
		params.put("p_IdEmpresa", paramsIn.p_IdEmpresa);
		params.put("p_IdEventoCapital", p_IdEventoCapital);
		params.put("p_FechaSistema", FechaSistema); 
		params.put("p_Hora", HoraSistema);
		
		if (paramsIn.p_IdsEventoCapitalCliente.size() > 0){
			filtraCliente = true;
			List<Long> IdEventoCapitalCliente = new ArrayList<Long>();
			
			for (ObjEventoCapitalCliente_Entrada val: paramsIn.p_IdsEventoCapitalCliente){
				IdEventoCapitalCliente.add(val.p_IdEventoCapitalCliente);
			}

			params.put("IdEventoCapitalCliente", IdEventoCapitalCliente);
			
		}
		
		List<ObjClienteANotificar> ClientesANotificar;
		
		if (!filtraCliente){
			ClientesANotificar = sql.query(conn, SQL_SELECT_CLIENTES_EVENTO_CAPITAL_A_NOTIFICAR, params, ObjClienteANotificar.class);	
		}else {
			ClientesANotificar = sql.query(conn, SQL_SELECT_CLIENTES_EVENTO_CAPITAL_A_NOTIFICAR2, params, ObjClienteANotificar.class);
		}
		
		
		
		if (ClientesANotificar.size() == 0) {
	
			params.clear();
			params.put("p_IdEmpresa", paramsIn.p_IdEmpresa);
			params.put("p_IdEventoCapital", p_IdEventoCapital);
			params.put("p_FechaSistema", FechaSistema); 
			params.put("p_Hora", HoraSistema);
			
			ClientesANotificar = sql.query(conn, SQL_SELECT_CLIENTES_EVENTO_CAPITAL_A_NOTIFICAR3, params, ObjClienteANotificar.class);
			if(ClientesANotificar.size()==0){
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_NO_HAY_CLIENTES_PARA_NOTIFICAR",new HashMap<String,String>(0),conn,paramsIn.p_IdEmpresa));				
				return salida;
			}
		}
		
		//Obtener info direccion de notificaciones
		HashMap<String, Object> parmsDirs= new HashMap<String, Object>();
		List<Long> portfolios = Utils.getColumn(ClientesANotificar, "IdPortafolio", false);
		parmsDirs.put("ListaIdPortafolio1", portfolios);

		List<ObjDirs> listaDirs  =sql.query(conn, SQL_SELECT_DIRS, parmsDirs, ObjDirs.class);
		
		List<ObjDirs> portafoliosSinNotificaciones = listaDirs.stream().filter(x -> x.IdDireccion == 0).collect(Collectors.toList());
		
		if (portafoliosSinNotificaciones.size() > 0) {
			
			List<Long> idsPortafoliosSinNotificaciones = portafoliosSinNotificaciones.stream().map(x -> x.IdPortafolio).collect(Collectors.toList());
			
			params.clear();
			params.put("idsPortafolios", idsPortafoliosSinNotificaciones);
			
			List<ObjPortafolioSinNotificacion> portafoliosCliente = sql.query(conn, SQL_SELECT_DATOS_PORTAFOLIOS_SIN_NOTIFICACION, params, ObjPortafolioSinNotificacion.class);
			
			for (ObjPortafolioSinNotificacion portafolio : portafoliosCliente) {
				
				HashMap<String, String> valoresMensaje = new HashMap<>(2);

			    valoresMensaje.put("NombreCompleto", portafolio.NombreCompleto);
			    valoresMensaje.put("CodigoPortafolio", portafolio.CodigoPortafolio);
				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA, 
						"UI_PORTAFOLIOS_CLIENTES_SIN_NOTIFICACION_EVENTO_CAPITAL", 
						valoresMensaje, 
						conn, 
						paramsIn.p_IdEmpresa));

				ClientesANotificar.removeIf(cliente -> cliente.IdPortafolio.equals(portafolio.IdPortafolio));
			}
			
		}		
		
		//Guarda el evento de capital relacionada a evento
		List<ObjEventoCapitalEvento> InsertEventoCapitalEvento = new ArrayList<ObjEventoCapitalEvento>();
		
		//Agrega notificacion
        AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
        notifIn.P_IDEMPRESA = paramsIn.p_IdEmpresa;
        notifIn.P_IDUSUARIO = paramsIn.p_IdUsuario;
        notifIn.P_IDMODULO = paramsIn.p_IdModulo;
        notifIn.P_EVENTOS = new ArrayList<>();
        notifIn.P_VALORES = new ArrayList<>();
        notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
        notifIn.P_IDDIRECCIONES = new ArrayList<>();
        notifIn.P_SECUENCIAORIGEN = this.secuenciaEventoOrigen; 
        notifIn.P_IDTIPOEVENTOORIGEN = this.idTipoEventoOrigen; 
        notifIn.P_NEMOEVENTOORIGEN = this.nemoEventoOrigen; 
        notifIn.P_TIPOEVENTOORIGEN = this.tipoEventoOrigen;
     
        
        for (ObjClienteANotificar noti:ClientesANotificar){
            AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
            evt.CodigoTipoEvento = noti.CodigoTipoEvento;
            evt.IdSecuencia = noti.IdSecuencia;
            evt.CodigoTipoEvento =  paramsIn.p_EstadoEventoCapital.equals("PRO")?noti.CodigoTipoEvento:(paramsIn.p_EstadoEventoCapital.equals("CER") && (noti.TipoEventoCapital.equals("DV") || noti.TipoEventoCapital.equals("CC") || noti.TipoEventoCapital.equals("SL"))?"CIERRE_EVENTO_CAPITAL_"+noti.TipoEventoCapital:"CIERRE_EVENTO_CAPITAL_VARIOS");
            evt.FechaCreacionSistema =  Fecha.toStringDate(vFechaHoraSistema);            
            evt.IdPersonaRemitente = datosRemitente.IdPersona;
            evt.IdPersonaDestinatario = noti.IdPersonaDestinatario;            
            evt.IdPortafolio = noti.IdPortafolio;
            notifIn.P_EVENTOS.add(evt);
            
            List<String> CodigoTipoEventoList = new ArrayList<String>();
            CodigoTipoEventoList.add(evt.CodigoTipoEvento);
    		params.clear();
            params.put("CodigoEvento", CodigoTipoEventoList);	
    		List<ControlProceso> estadoControlProceso = sql.query(conn, SQL_SELECT_CONTROL_PROCESO, params, ControlProceso.class);
    		
            notifIn.P_CONTROLPROCESO = estadoControlProceso.get(0).ControlProceso;
            
            for (ObjDirs dir:listaDirs) { 
            	if (!dir.IdPortafolio.equals(noti.IdPortafolio)) continue;
            	if (dir.IdDireccion==null || dir.IdDireccion==0) continue;
	            AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
	            ndir.IdSecuencia = noti.IdSecuencia;
	            ndir.TipoDestinatario = dir.TipoDestinatario;
	            ndir.IdDireccion = new Long(dir.IdDireccion);
	            notifIn.P_IDDIRECCIONES.add(ndir);
	            //Agregar notificacion al empleado
	            ndir = new AgregarEvento.ObjIDDIRECCIONES();
	            ndir.IdSecuencia = noti.IdSecuencia;
	            ndir.TipoDestinatario = "CC";
	            ndir.IdDireccion = datosRemitente.IdDireccionElectronica;
	            notifIn.P_IDDIRECCIONES.add(ndir);
            }
            
            if(noti.Boveda==null) {
            	boveda="0";
            }else {
            	boveda= noti.Boveda;
            }
            
         // Valores generales
            String DateNoti;
            if(noti.FechaLimite==null) {
            	DateNoti="01-01-0001";
            }else {
            	DateNoti = new SimpleDateFormat("dd-MM-yyyy").format(noti.FechaLimite);
            }

            noti.Factor = (noti.Factor.compareTo(BigDecimal.ZERO)) == 0 ? new BigDecimal (0L) : noti.Factor ;
        	String tipoInversion = noti.IdProductoAgrupador.equals(9999) ? "No Previsional" : "Previsional";
        	 
            noti.FactorS=noti.Factor.toString().replace('.', ',');
            
            String cantidadMonto = noti.TipoEventoCapital.equalsIgnoreCase("RC") ||  noti.TipoEventoCapital.equalsIgnoreCase("CC") ||  noti.TipoEventoCapital.equalsIgnoreCase("SL") ?"Monto":"Cantidad";
             
            notifIn.addValor(noti.IdSecuencia, "IDSECUENCIA", noti.IdSecuencia+"")
            	   .addValor(noti.IdSecuencia, "CLIENTE", noti.Nombre)
                   .addValor(noti.IdSecuencia, "DOCUMENTOIDENTIDAD", noti.SocNeg)
                   .addValor(noti.IdSecuencia, "PORTAFOLIO", noti.Port+"")
                   .addValor(noti.IdSecuencia, "CODIGOPORTAFOLIO", noti.CodigoPortafolio+"")
                   .addValor(noti.IdSecuencia, "PROPOSITOPORTAFOLIO", noti.PropositoPortafolio+"")
                   .addValor(noti.IdSecuencia, "TELEFONO", noti.Fono)
                   .addValor(noti.IdSecuencia, "CORREO", noti.Mail)  
                   .addValor(noti.IdSecuencia, "AGENTE", noti.Agente)
                   .addValor(noti.IdSecuencia, "FECHA", noti.Fecha)
                   .addValor(noti.IdSecuencia, "HORA", noti.Hora)
                   .addValor(noti.IdSecuencia, "TIPO", noti.Tipo)
                   .addValor(noti.IdSecuencia, "FAMILIA", noti.Familia)
                   .addValor(noti.IdSecuencia, "PAGO", noti.Pago+"")
                   .addValor(noti.IdSecuencia, "PAGOMO", noti.PagoMo+"")
                   .addValor(noti.IdSecuencia, "MONTOCAPITAL", noti.MontoCapital+"")
                   .addValor(noti.IdSecuencia, "MONTOCAPITALMO", noti.MontoCapitalMo+"")
                   .addValor(noti.IdSecuencia, "MONTO", noti.Monto+"")
                   .addValor(noti.IdSecuencia, "MONTOMO", noti.MontoMo+"")
                   .addValor(noti.IdSecuencia, "FECHAPAGO", noti.FechaPago)
                   .addValor(noti.IdSecuencia, "ESTADO", noti.Estado)
                   .addValor(noti.IdSecuencia, "IDDIRECCIONELECTRONICAPRINCIPAL", noti.IdDireccionElectronicaPrincipal+"")
                   .addValor(noti.IdSecuencia, "IDPERSONA", noti.IdPersonaDestinatario+"")
                   .addValor(noti.IdSecuencia, "CORREDORA", noti.Corredora)
                   .addValor(noti.IdSecuencia, "MAILCORREDORA", noti.MailCorredora)
                   .addValor(noti.IdSecuencia, "MAILAGENTE", noti.MailAgente)
                   .addValor(noti.IdSecuencia, "FACTOR", noti.Factor+"")
                   .addValor(noti.IdSecuencia, "FACTORS", noti.FactorS+"")
                   .addValor(noti.IdSecuencia, "ATENCIONCLIENTE", noti.AtencionCliente)
                   .addValor(noti.IdSecuencia, "TIPOEVENTOCAPITAL", noti.TipoEventoCapital)
                   .addValor(noti.IdSecuencia, "CANTIDAD", noti.Cantidad)
                   .addValor(noti.IdSecuencia, "BOVEDA",  boveda )
                   .addValor(noti.IdSecuencia, "TIPOINSTRUMENTO",  noti.IdTipoInstrumento )
                   .addValor(noti.IdSecuencia, "FECHALIMITE", DateNoti )
                   .addValor(noti.IdSecuencia, "LOGO_PUBLICO", logoCorredorRutaPublica)
                   .addValor(noti.IdSecuencia, "RAZONSOCIAL", noti.RazonSocial)
                   .addValor(noti.IdSecuencia, "CANTIDADMONTO", cantidadMonto)            
                   .addValor(noti.IdSecuencia, "TIPOINVERSION", tipoInversion)
            	   .addValor(noti.IdSecuencia, "TASAEFECTIVA", noti.TasaEfectiva)
            	   .addValor(noti.IdSecuencia, "MONTORETENCIONISFUT",noti.MontoRetencionISFUT)
            	   .addValor(noti.IdSecuencia, "MONTOIMPUESTOEXTRANJERO", noti.MontoImpuestoExtranjero.setScale(2).toString().replace('.', ','))
            	   .addValor(noti.IdSecuencia, "GASTOTRANSFERENCIA", noti.GastoTransferencia.setScale(2).toString().replace('.', ','))
            	   .addValor(noti.IdSecuencia, "FECHASTRING", noti.FechaString)
            	   .addValor(noti.IdSecuencia, "FECHAPAGOSTRING", noti.FechaPagoString)
            	   .addValor(noti.IdSecuencia, "FECHACORTE", noti.FechaCorte)
                   .addValor(noti.IdSecuencia, "TELEFONOAGENTE", noti.FonoAgente)
                   .addValor(noti.IdSecuencia, "CANTIDADNUEVA", noti.CantidadNueva);

        }
        
        
        if (notifIn.P_EVENTOS.size() > 0) {
            AgregarEvento notif = new AgregarEvento();
            AgregarEvento.ServiceSalida notifOut = new AgregarEvento.ServiceSalida(); 
            notifOut = notif.logica(conn, notifIn);
            logger.info("RESULTADO DE SOLICITUD DE NOTIFICACION : " + notifOut.toString());
            
            for (ObjClienteANotificar clienteNotificar:ClientesANotificar) {
            	for(AgregarEvento.ObjIDEVENTO notifi:notifOut.P_OUT_IDEVENTO) {
           
            		if (clienteNotificar.IdSecuencia.equals(notifi.IdSecuencia)) {
            			ObjEventoCapitalEvento EventoCapitalEvento = new ObjEventoCapitalEvento();
            			EventoCapitalEvento.IdEventoCapitalEvento = clienteNotificar.IdEventoCapital+"_"+notifi.IdEvento;
            			EventoCapitalEvento.IdEventoCapital = clienteNotificar.IdEventoCapital;
            			EventoCapitalEvento.IdEvento = notifi.IdEvento;
            			EventoCapitalEvento.IdSecuencia = notifi.IdSecuencia;
            			InsertEventoCapitalEvento.add(EventoCapitalEvento);
            			break;
            		}
            	}
            }
            sql.batch(conn, SQL_INSERT_EVENTO_CAPITAL_EVENTO, InsertEventoCapitalEvento, ObjEventoCapitalEvento.class); 
        }
		
        if (paramsIn.p_CambiaEstadoANotificado) {
			/* POR AHORA SOLO SE CAMBIARA EL ESTADO DEL EVENTO CAPITAL A NOT (NOTIFICADO) si es DV (Dividendos en dinero) || DA (Dividendo en Acciones) || EP (Emision Pagada) || EL (Emision Liberada) || RC (Reparto Capital)*/
			List<EventoCapital> actualizaEventoCapital =  new ArrayList<>();
			
			for (EventoCapital eventoCapital:eventosCapital) {
				
				Set<String> tipoEvento = new HashSet<>(Arrays.asList("DV", "DA", "EP", "EL", "RC"));
				
				if ( tipoEvento.contains(eventoCapital.IdTipoEventoCapital) && paramsIn.p_EstadoEventoCapital.equals("PRO")) {
					actualizaEventoCapital.add(eventoCapital);
				}
				 
			}
			
			sql.batch(conn, SQL_UPDATE_EVENTO_CAPITAL, actualizaEventoCapital, EventoCapital.class);
		}
		/*****/
		
		logger.info("== TERMINO NOTIFICACION EVENTO CAPITAL ==");
		
		if(salida.P_OUT_ERRORESCONTADOR == 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK, 
					"UI_EVENTO_CAPITAL_NOTIFICADO_CORRECTAMENTE", 
					new HashMap<String,String>(0), 
					conn, 
					paramsIn.p_IdEmpresa));
		}
		
		return salida;
	}

	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		//Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		NotificarEventoCapital_Entrada in = gson.fromJson(jin.toString(), NotificarEventoCapital_Entrada.class);
		
		NotificarEventoCapital_Salida out = logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();	
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")){
			Gson gson = Utils.getGsonBuilder();
			
			//String json = "{\"p_IdEmpresa\":1, \"p_IdUsuario\":17, \"p_IdModulo\":\"CUS\", \"p_IdsEventoCapital\":[ {\"p_IdEventoCapital\":364496}], \"p_IdsEventoCapitalCliente\":[ {\"p_IdEventoCapitalCliente\":74781}]     }";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CUS\",\"p_IdsEventoCapital\":[{\"p_IdEventoCapital\":366521}],\"p_IdsEventoCapitalCliente\":[]}";
	//		String jsonPresuraDV = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1006,\"p_IdModulo\":\"CUS\",\"p_IdsEventoCapital\":[{\"p_IdEventoCapital\":6805596}],\"p_IdsEventoCapitalCliente\":[]}";
	//		String jsonPresuraEP = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1006,\"p_IdModulo\":\"CUS\",\"p_IdsEventoCapital\":[{\"p_IdEventoCapital\":6805681}],\"p_IdsEventoCapitalCliente\":[]}";
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_IdsEventoCapital\":[{\"p_IdEventoCapital\":45955}],\"p_IdsEventoCapitalCliente\":[]}";
			
			NotificarEventoCapital_Entrada Objin = gson.fromJson(jsonDesa, NotificarEventoCapital_Entrada.class);
			
			NotificarEventoCapital demo = new NotificarEventoCapital();		
			
			System.out.println("ENTRADA:");
			System.out.println(Objin.toString());
			
			
			try {
				NotificarEventoCapital_Salida ObjOut= demo.logica(con, Objin);
				con.rollback();
				
				System.out.println("SALIDA:");
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
	
	

}
