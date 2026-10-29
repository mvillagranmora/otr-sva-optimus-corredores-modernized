package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//Tesoreria
import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV;
import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada;
import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV_Salida;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CambiarEstadoRevocacionSAAPV implements JavaCall {	
	private final Log log = new Log("CambiarEstadoRevocacionSAAPV");	

	public String SQL_SELECT_VALIDACIONES = loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Revocacion_SelectValidaciones.sql");	
	
	public static class InContrato{
		public Long					IdPersona;
		public Long					IdCliente;
		public Long					IdEmpleador;
		public String				IdFolioContratoSAAPV;
		public String				FechaRevocacion; 
		public String				IdEstadoEnvioNotificacion;
		public String				IdEstadoSolicitud;
		public String				IdEstado;		
	    public Long 				IdGlosa;
	    public String 				DescripcionGlosa;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};	
		
	public static class ResultadoValidaciones{	
		public Long IdContratoSAAPV;
		public Long IdEmpleador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	//  ENTRADA
	public static class EntradaRevocacionContrato {		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InContrato>			p_ContratosSAAPV;		
		
		public EntradaRevocacionContrato(){
			p_ContratosSAAPV 					= new ArrayList<InContrato>();			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
		
	// SALIDA
	public static class SalidaRevocacionContrato{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List<EntidadContrato>	Contrato;
		public Long ContadorErrores;
			
		public SalidaRevocacionContrato(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.Contrato = new ArrayList<EntidadContrato>();
			this.ContadorErrores=0L;
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class EntidadContrato{
		public String Folio;
		public Long	IdFolioSAAPV;
		public List<MensajesErrores> Mensaje;
	        
		public EntidadContrato(){
			Mensaje = new ArrayList<MensajesErrores>();
		}
			
		@Override
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
		
	public static class MensajesErrores{
		public String	IdInterno;
		public String	Tipo;
		public String	Codigo;
		public String	Descripcion;
			
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaRevocacionContrato jin = gson.fromJson(in.toString(), EntradaRevocacionContrato.class);
		SalidaRevocacionContrato out = logica(conn,jin);
			
		JsonParser parser = new JsonParser();
	    JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
			
	public SalidaRevocacionContrato logica(Connection con, EntradaRevocacionContrato entrada) throws Exception{
		log.write("INICIO SalidaRevocacionContrato");
		SalidaRevocacionContrato out = new SalidaRevocacionContrato();
		SQL sql=new SQL();
			
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
					
		ArrayList<Long> correlativos = new ArrayList<Long>();
		String folio_saapv = "";
			
		List<Long> secuenciaSAAPV = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::IdSAAPVRevocacion", new Long(entrada.p_ContratosSAAPV.size()));
		int i=0;
		long j=0;
			
		ContratosSAAPV_Entrada contratosSAAPV_Entrada = new ContratosSAAPV_Entrada();
			
		ContratosSAAPV inContrato = new ContratosSAAPV();
			
		for (InContrato mov : entrada.p_ContratosSAAPV ){
			folio_saapv = mov.IdFolioContratoSAAPV;		
			
			HashMap<String,Object> params_ValidacionesContrato = new HashMap<String,Object>();
			params_ValidacionesContrato.put("FolioSAAPV", folio_saapv);
			List<ResultadoValidaciones> contratosExistentesValidaciones = sql.query(con, SQL_SELECT_VALIDACIONES, params_ValidacionesContrato, ResultadoValidaciones.class);
			HashMap<Long,Object> hcontratosExistentesValidaciones = Utils.List2Hash(contratosExistentesValidaciones, "IdContratoSAAPV");
			
			if (mov.IdCliente == null || mov.IdPersona == null) {
				EntidadContrato entidadContrato = new EntidadContrato();
				entidadContrato.Folio= mov.IdFolioContratoSAAPV;
				entidadContrato.IdFolioSAAPV= 0L;
					
				MensajesErrores mensajesErrores = new MensajesErrores();
				mensajesErrores.IdInterno = mov.IdFolioContratoSAAPV;
				mensajesErrores.Tipo = "ERROR"; 
				mensajesErrores.Codigo = "CLIENTE_NO_EXISTE";
				mensajesErrores.Descripcion = "CLIENTE_NO_EXISTE";
				entidadContrato.Mensaje.add(mensajesErrores);	
				out.Contrato.add(entidadContrato);
				j++;
					
				continue;
			
			} else if (hcontratosExistentesValidaciones.size() == 0) {
				//out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONTRATO_NO_VALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));					
				EntidadContrato entidadContrato = new EntidadContrato();
				entidadContrato.Folio= mov.IdFolioContratoSAAPV;
				entidadContrato.IdFolioSAAPV= 0L;
					
				MensajesErrores mensajesErrores = new MensajesErrores();
				mensajesErrores.IdInterno = mov.IdFolioContratoSAAPV;
				mensajesErrores.Tipo = "ERROR"; 
				mensajesErrores.Codigo = "CONTRATO_NO_VALIDO";
				mensajesErrores.Descripcion = "CONTRATO_NO_VALIDO";
				entidadContrato.Mensaje.add(mensajesErrores);	
				out.Contrato.add(entidadContrato);
				j++;
					
				continue;
			}else {
				EntidadContrato entidadContrato = new EntidadContrato();
				entidadContrato.Folio= mov.IdFolioContratoSAAPV;
				entidadContrato.IdFolioSAAPV= secuenciaSAAPV.get(i);
					
				MensajesErrores mensajesErrores = new MensajesErrores();
				mensajesErrores.IdInterno = mov.IdFolioContratoSAAPV;
				mensajesErrores.Tipo = "OK"; 
				mensajesErrores.Codigo = "REVOCACION_CONTRATO_OK";
				mensajesErrores.Descripcion = "REVOCACION_CONTRATO_OK";
				entidadContrato.Mensaje.add(mensajesErrores);	
				out.Contrato.add(entidadContrato);	
			}
				
			ResultadoValidaciones idempleadorobjeto = contratosExistentesValidaciones.get(0);
			Long idempleador = idempleadorobjeto.IdEmpleador;

			inContrato.IdPersona = mov.IdPersona;
			inContrato.IdCliente = mov.IdCliente;
			inContrato.IdEmpleador = idempleador;
			inContrato.IdFolioContratoSAAPV = mov.IdFolioContratoSAAPV;
			inContrato.FechaRevocacion = mov.FechaRevocacion;
			inContrato.IdEstadoEnvioNotificacion = "";
			inContrato.IdEstadoSolicitud = "PDS";
			inContrato.IdEstado = "REV";
			inContrato.IdGlosa = mov.IdGlosa;
			inContrato.DescripcionGlosa = mov.DescripcionGlosa;
			contratosSAAPV_Entrada.p_ContratosSAAPV.add(inContrato);
				
			contratosSAAPV_Entrada.p_IdEmpresa = entrada.p_IdEmpresa;
			contratosSAAPV_Entrada.p_IdUsuario = entrada.p_IdUsuario ;
			contratosSAAPV_Entrada.p_IdModulo="APV";
			contratosSAAPV_Entrada.p_Opcion="REV";
				
			//contratosSAAPV_Entrada.p_ContratosSAAPV.add(inContrato);
			//contratosSAAPV_Entrada.p_ContratosSAAPV = inContrato;
						
			CambiarEstadoContratosSAAPV cambiarEstadoContratosSAAPV = new CambiarEstadoContratosSAAPV();
			ContratosSAAPV_Salida salidaRevocacionContrato = cambiarEstadoContratosSAAPV.logica(con, contratosSAAPV_Entrada);
			i++;	
		}
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.ContadorErrores=j;	
		
		return out;	
	}
	
		/********************
	 * Metodo Principal *
	 ********************/
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"REV\",\"p_ContratosSAAPV\":[{\"DocumentoIdentidad\":null,\"IdPersona\":null,\"IdCliente\":null,\"IdFolioContratoSAAPV\":\"\",\"FechaRevocacion\":\"2018-10-26\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"PDS\",\"IdEstado\":\"REV\",\"IdGlosa\":1,\"DescripcionGlosa\":\"Prueba revocar\"}]}";
				EntradaRevocacionContrato Objin_json = gson.fromJson(json, EntradaRevocacionContrato.class);	
				CambiarEstadoRevocacionSAAPV demo = new CambiarEstadoRevocacionSAAPV();	
					
		 		System.out.println("ENTRADA");
		 		System.out.println(Objin_json.toString());
		 		System.out.println("SALIDA");

		 		try { 
		 			SalidaRevocacionContrato ObjOut = demo.logica(con, Objin_json);
		 			con.rollback();
		 			System.out.println(ObjOut.toString());
		 		} catch(Exception er) {
					er.printStackTrace();
					con.rollback();
				}		
			} catch (Exception e) {
				e.printStackTrace();			
			}	
			System.out.println("== END ==");
		};
	}
}
