package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Fecha;


public class CrearSolicitudPuntas implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(CrearSolicitudPuntas.class);
	
	private final String SQL_INSERT_SOLICITUD_PUNTAS = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearSolicitudPuntas_InsertSolicitudPuntas.sql");	
	private final String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearSolicitudPuntas_SelectCliente.sql");	

	public static class EntradaSolicitudPuntas extends BcsPojo {
		public Long 				p_IdEmpresa;
		public Long 				IdSolicitudPuntas;	
		public java.sql.Timestamp 	FechaRegistro;
		public java.sql.Timestamp 	FechaVigencia;
		public Long 				IdOperacion; 
		public String 				IdTipoOperacion;
		public Long 				IdCliente; 
		public Long 				IdPortafolio; 
		public BigDecimal 			TipoCambioCostoSolicitado;
		public BigDecimal 			TipoCambioCostoAprobado;
		public BigDecimal 			MontoMonedaPrincipal;
		public String 				IdMonedaPrincipal;	
		public Long 				IdUsuarioSolicitud; 
		public Long 				IdUsuarioAutorizador;
		public String 				ObservacionSolicitud;
		public String 				IdEstado;			
	}
	
	public static class Persona {
		public Long IdCliente;
		public Long IdPersona;
		public Long IdEmpresa;
		public String IdEstado;
		public String NombreCompleto;
		public String CodigoPortafolio;
		public String DocumentoIdentidad = "";
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearSolicitudPuntas extends BcsPojo {

		public List<MensajeSalida> 	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public SalidaCrearSolicitudPuntas () {	
			
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR 		 = 0L;			
		}
		
	}
	

	public SalidaCrearSolicitudPuntas logica(Connection conn, EntradaSolicitudPuntas in) throws Exception {
		logger.info("INIT");
		
		Long init = System.currentTimeMillis();
		SalidaCrearSolicitudPuntas salida = new SalidaCrearSolicitudPuntas();		
		SQL sql = new SQL();
		Long IdSolicitudPuntas = Utils.getSecuencia(conn, "BSCL.Mesa.BD.Secuencias::SPT.IdSolicitudPuntas", (long) 1).get(0);
		String fechaHoraSistema = Utils.getFechaHoraSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		
		EntradaSolicitudPuntas solicitudPuntas = new EntradaSolicitudPuntas();
		solicitudPuntas.IdSolicitudPuntas = IdSolicitudPuntas;
		solicitudPuntas.p_IdEmpresa = in.p_IdEmpresa;
		solicitudPuntas.FechaRegistro = Fecha.toDateTime(fechaHoraSistema);
		solicitudPuntas.FechaVigencia = Fecha.addDays(Fecha.toDateTime(fechaHoraSistema), 1);
		solicitudPuntas.IdCliente = in.IdCliente;
		solicitudPuntas.IdPortafolio = in.IdPortafolio;
		solicitudPuntas.IdOperacion = in.IdOperacion;
		solicitudPuntas.IdTipoOperacion = in.IdTipoOperacion;
		solicitudPuntas.IdMonedaPrincipal = in.IdMonedaPrincipal;
		solicitudPuntas.MontoMonedaPrincipal = in.MontoMonedaPrincipal;
		solicitudPuntas.IdUsuarioSolicitud = in.IdUsuarioSolicitud;
		solicitudPuntas.IdUsuarioAutorizador = in.IdUsuarioAutorizador;
		solicitudPuntas.TipoCambioCostoSolicitado = in.TipoCambioCostoSolicitado;
		solicitudPuntas.TipoCambioCostoAprobado = in.TipoCambioCostoSolicitado;
		solicitudPuntas.ObservacionSolicitud = in.ObservacionSolicitud;
		solicitudPuntas.IdEstado = in.IdEstado;
		
		List<EntradaSolicitudPuntas> solicitudesPuntas = Arrays.asList(solicitudPuntas);

		sql.batch(conn, SQL_INSERT_SOLICITUD_PUNTAS,solicitudesPuntas, EntradaSolicitudPuntas.class);	
		
		HashMap<String, Object> parCli = new HashMap<>();
		parCli.put("IdCliente", in.IdCliente);
		List<Persona> PersonaList = sql.query(conn, SQL_SELECT_CLIENTE, parCli, Persona.class);
		String nombreCompleto = PersonaList.get(0).NombreCompleto;
		
		AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa, "SOLICITUD_PUNTAS", "SOLICITUD PUNTAS\r\n" 
				+ "Tiene una solicitud por autorizar "
				+ "\r\nIdSolicitud       : "+IdSolicitudPuntas+""
						+ ""
				+ "\r\nOperación: " + ("COMSPT".equals(in.IdTipoOperacion) ? "Compra Spot" : "Venta Spot")+""
				+ "\r\nMoneda: "+in.IdMonedaPrincipal+""
				+ "\r\nMonto: " + MonedaFormato.formatear(conn,in.MontoMonedaPrincipal, "MONTO", in.IdMonedaPrincipal).valorConFormato+""
				+ "\r\nCliente: " + nombreCompleto+""
				+ "\r\nTC Solicitado: "+in.TipoCambioCostoSolicitado.stripTrailingZeros().toPlainString().replace(".", ",")+""
				+ "\r\nObservaciones: "+in.ObservacionSolicitud
				);
		
		String msg=Diccionario.getMensaje(conn,in.p_IdEmpresa,"CREAR_SOLICITUD_PUNTAS_EXITO")
				  .add("IdSolicitudPuntas", IdSolicitudPuntas)
				  .add("UsuarioAutorizador", in.IdUsuarioAutorizador)
				  .msg();			
		salida.P_OUT_MENSAJES.add(new MensajeSalida(IdSolicitudPuntas,MensajeSalida.MENSAJE_OK,"CREAR_SOLICITUD_PUNTAS_EXITO",msg));
		
		logger.info("FIN::CrearSolicitudPuntas, " + (System.currentTimeMillis() - init));
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EntradaSolicitudPuntas in = gson.fromJson(jin.toString(), EntradaSolicitudPuntas.class);
		SalidaCrearSolicitudPuntas out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String jsonOptimus = "{\r\n" + 
					"    \"p_IdEmpresa\": 1,\r\n" + 
					"    \"IdUsuarioSolicitud\": 9,\r\n" + 
					"    \"IdUsuarioAutorizador\": 1958,\r\n" + 
					"    \"IdOperacion\": 0,\r\n" + 
					"    \"IdTipoOperacion\": \"COMSPT\",\r\n" + 
					"    \"MontoMonedaPrincipal\": 800,\r\n" + 
					"    \"IdMonedaPrincipal\": \"USD\",\r\n" + 
					"    \"IdCliente\": 1418705,\r\n" + 
					"    \"Estado\": \"PEN\",\r\n" + 
					"    \"TipoCambioCostoSolicitado\": 850,\r\n" + 
					"    \"ObservacionSolicitud\": \"DASDSADH ASHSDAH\"\r\n" + 
					"}";
			EntradaSolicitudPuntas Objin = gson.fromJson(jsonOptimus, EntradaSolicitudPuntas.class);
			CrearSolicitudPuntas demo = new CrearSolicitudPuntas();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try { 				
 				SalidaCrearSolicitudPuntas ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}
