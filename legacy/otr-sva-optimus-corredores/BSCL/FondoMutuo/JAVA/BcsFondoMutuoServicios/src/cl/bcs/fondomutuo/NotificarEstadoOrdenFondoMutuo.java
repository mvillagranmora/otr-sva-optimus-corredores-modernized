package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.evt.AgregarEvento.ObjEVENTOS;
import cl.bcs.plataforma.evt.AgregarEvento.ObjIDDIRECCIONES;
import cl.bcs.plataforma.evt.AgregarEvento.ServiceEntrada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarEstadoOrdenFondoMutuo implements JavaCall{
	
	private final Logger logger =  LogManager.getLogger(NotificarEstadoOrdenFondoMutuo.class);
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/NotificarEstadoOrdenFondoMutuo_SelectSolicitudes.sql");
	
	public static class EntradaNotificarEstadoOrdenFondoMutuo{
		
		public Long p_IdEmpresa;		
		public Long p_IdUsuario;		
		public String p_IdModulo;
		public List<Long> p_Solicitudes;
		
		public EntradaNotificarEstadoOrdenFondoMutuo(){
			this.p_Solicitudes = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudNotificar{
		
		public Long FOLIO;
		public Long IdCliente;
		public Long IDCLIENTEPERSONA;
		public String NEMO;
		public String RUT;
		public String CLIENTE;
		public Long IdPortafolioCliente;
		public String CUENTA;
		public Long IdEmpleadoDigitador;
		public String AGENTE;
		public Long IDEMPLEADOPERSONA;
		public String CORREO;
		public String TELEFONO;
		public String FECHA;
		public String HORA;
		public Timestamp FECHA_EVENTO;
		public String PRODUCTO;
		public String TIPO_OPERACION;
		public String CANTIDAD;
		public String PRECIO;
		public String MONTO;
		public String MONEDA;
		public String ESTADO;
		public String CORREDORA;
		public String ATENCIONCLIENTE;
		public String CONDICIONLIQUIDACION;
		public String TIPOPRECIOORDEN;
		public String FECHAVIGENCIA;
		public Long TASA;
		public Long PLAZOVENCIMIENTO;
		public String MONTOPRIMA;
		public String VENTACORTA;
		public String COMISION;
		public String IMPUESTO;
		public String GASTO;
		public String DERECHO;
		public Long IdDireccionEvento;
		public String TipoDestinatarioEvento;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaNotificarEstadoOrdenFondoMutuo{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaNotificarEstadoOrdenFondoMutuo(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaNotificarEstadoOrdenFondoMutuo jin = gson.fromJson(in.toString(), EntradaNotificarEstadoOrdenFondoMutuo.class);
		SalidaNotificarEstadoOrdenFondoMutuo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public void logicaAsync(Connection con,EntradaNotificarEstadoOrdenFondoMutuo entrada) throws Exception {
		Gson gson = getGsonBuilder();	
		JsonParser parser 	= new JsonParser();
        JsonObject dataObj	= parser.parse(gson.toJson(entrada)).getAsJsonObject();		
        String CallBatchURL=ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchURL");
        String CallBatchToken=ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchToken");
		Utils.CallBatchAsync(CallBatchURL, CallBatchToken, "PLUGIN_FMM.jar@cl.bcs.fondomutuo.NotificarEstadoOrdenFondoMutuo", dataObj);
	}
	
	public SalidaNotificarEstadoOrdenFondoMutuo logica(Connection con, EntradaNotificarEstadoOrdenFondoMutuo entrada) throws Exception{
		logger.info("INICIO NotificarEstadoOrdenFondoMutuo");
		SQL sql=new SQL();
		SalidaNotificarEstadoOrdenFondoMutuo out = new SalidaNotificarEstadoOrdenFondoMutuo();
		String segundos = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdSolicitudAporteRescate", entrada.p_Solicitudes);
		params.put("SegundosZonaHoraria", segundos);
		List<SolicitudNotificar> sol = sql.query(con, SQL_SELECT_SOLICITUDES, params, SolicitudNotificar.class);
		ServiceEntrada eventoEntrada = new ServiceEntrada();
		eventoEntrada.P_IDEMPRESA = entrada.p_IdEmpresa;
		eventoEntrada.P_IDMODULO = entrada.p_IdModulo;
		eventoEntrada.P_IDUSUARIO = entrada.p_IdUsuario;
		
		String logoCorredorRutaPublica = "";
		ConsultarImagen consultarImagen = new ConsultarImagen();
		ConsultarImagenEntrada consultarImagenIn = new ConsultarImagenEntrada();
		consultarImagenIn.p_IdEmpresa = entrada.p_IdEmpresa;
		consultarImagenIn.p_imagenNombres.add("LOGO_CORREDOR");
		ConsultarImagenSalida consultarImagenOut = consultarImagen.logica(con, consultarImagenIn);
		if (consultarImagenOut.OUT_IMAGENES.size() > 0 ) {
			logoCorredorRutaPublica = consultarImagenOut.OUT_IMAGENES.get(0).RutaPublica;
		}
		
		for(SolicitudNotificar s : sol){
			ObjEVENTOS evento = new ObjEVENTOS();
			evento.IdSecuencia = s.FOLIO;
			evento.CodigoTipoEvento = "CERTIFICACION_FFMM";
			evento.FechaCreacionSistema = Fecha.toStringTimeStamp(s.FECHA_EVENTO);
			evento.IdPersonaRemitente = s.IDEMPLEADOPERSONA;
			evento.IdPersonaDestinatario = s.IDCLIENTEPERSONA;
			evento.IdPortafolio = s.IdPortafolioCliente;
			eventoEntrada.P_EVENTOS.add(evento);
			eventoEntrada.addValor(s.FOLIO,"FOLIO",s.FOLIO.toString())
						 .addValor(s.FOLIO, "IdCliente", s.IdCliente.toString())
						 .addValor(s.FOLIO, "IDCLIENTEPERSONA", s.IDCLIENTEPERSONA.toString())
						 .addValor(s.FOLIO, "NEMO", s.NEMO)
						 .addValor(s.FOLIO, "RUT", s.RUT)
						 .addValor(s.FOLIO, "CLIENTE", s.CLIENTE)
						 .addValor(s.FOLIO, "IdPortafolioCliente", s.IdPortafolioCliente.toString())
						 .addValor(s.FOLIO, "CUENTA", s.CUENTA)
						 .addValor(s.FOLIO, "IdEmpleadoDigitador", s.IdEmpleadoDigitador.toString())
						 .addValor(s.FOLIO, "AGENTE", s.AGENTE)
						 .addValor(s.FOLIO, "IDEMPLEADOPERSONA", s.IDEMPLEADOPERSONA.toString())
						 .addValor(s.FOLIO, "CORREO", s.CORREO)
						 .addValor(s.FOLIO, "TELEFONO", s.TELEFONO)
						 .addValor(s.FOLIO, "FECHA", s.FECHA)
						 .addValor(s.FOLIO, "HORA", s.HORA)
						 .addValor(s.FOLIO, "PRODUCTO", s.PRODUCTO)
						 .addValor(s.FOLIO, "TIPO_OPERACION", s.TIPO_OPERACION)
						 .addValor(s.FOLIO, "CANTIDAD", s.CANTIDAD)
						 .addValor(s.FOLIO, "PRECIO", s.PRECIO)
						 .addValor(s.FOLIO, "MONTO", s.MONTO)
						 .addValor(s.FOLIO, "MONEDA", s.MONEDA)
						 .addValor(s.FOLIO, "ESTADO", s.ESTADO)
						 .addValor(s.FOLIO, "CORREDORA", s.CORREDORA)
						 .addValor(s.FOLIO, "ATENCIONCLIENTE", s.ATENCIONCLIENTE)
						 .addValor(s.FOLIO, "CONDICIONLIQUIDACION", s.CONDICIONLIQUIDACION)
						 .addValor(s.FOLIO, "TIPOPRECIOORDEN", s.TIPOPRECIOORDEN)
						 .addValor(s.FOLIO, "FECHAVIGENCIA", s.FECHAVIGENCIA)
						 .addValor(s.FOLIO, "TASA", s.TASA.toString())
						 .addValor(s.IdCliente, "PLAZOVENCIMIENTO", s.PLAZOVENCIMIENTO.toString())
						 .addValor(s.FOLIO, "MONTOPRIMA", s.MONTOPRIMA)
						 .addValor(s.FOLIO, "VENTACORTA", s.VENTACORTA)
						 .addValor(s.FOLIO, "COMISION", s.COMISION)
						 .addValor(s.FOLIO, "IMPUESTO", s.IMPUESTO)
						 .addValor(s.FOLIO, "GASTO", s.GASTO)
						 .addValor(s.FOLIO, "DERECHO", s.DERECHO)
						 .addValor(s.FOLIO, "LOGO_CORREDOR", logoCorredorRutaPublica);
			ObjIDDIRECCIONES dir = new ObjIDDIRECCIONES();
			dir.IdDireccion = s.IdDireccionEvento;
			dir.IdSecuencia = s.FOLIO;
			dir.TipoDestinatario = s.TipoDestinatarioEvento;
			eventoEntrada.P_IDDIRECCIONES.add(dir);
		}
		
		if (eventoEntrada.P_EVENTOS.size() > 0) {
		    AgregarEvento notif = new AgregarEvento();
		    notif.logica(con, eventoEntrada);
		}
		
		logger.info("SALIDA NotificarEstadoOrdenFondoMutuo");
		return out;
	}
	
}
