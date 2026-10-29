 package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.GrabarContratoSAAPV.DistribucionInversion;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosContrato;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosEntradaServicio;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosSalidaServicio;
import cl.bcs.APV.GrabarContratoSAAPV.PortafoliosModificados;
import cl.bcs.APV.GrabarContratoSAAPV.DetalleOrigenTraspaso;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaMasivaArchivo implements JavaCall{	
	
	private final Log log = new Log("CargaMasivaArchivoAPV");
	
	private final String SQL_INSERT_CARGA_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/CargaMasivaArchivo_Insert.sql");
	
	
	public static class CargaArchivo {
		public Long IdEmpresa;
		public Long IdCargaMasiva;
		public String NombreCargaMasiva;
		public Long CantidadRegistros;
		public Long IdUsuario;
		public int IdTipoCarga;
		public String Descripcion;
		public String FechaCarga;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class CargaMasivaArchivo_Entrada {
	    public Long							p_IdEmpresa;
	    public Long							p_IdUsuario;
	    public String						p_IdModulo;
	    public List<CargaArchivo> 			p_Archivo;
	    public List<ParametrosContrato>		p_ParametrosContratos;
		public List<DistribucionInversion> 	p_DistribucionInversion;
		public List<PortafoliosModificados> p_PortafoliosModificados;
		public List<DetalleOrigenTraspaso>  p_DetalleOrigenTraspaso;
	    
	    public CargaMasivaArchivo_Entrada(){
	    	p_Archivo = new ArrayList<CargaArchivo>();
	    	p_ParametrosContratos = new ArrayList<ParametrosContrato>();
	    	p_DistribucionInversion = new ArrayList<DistribucionInversion>();
	    	p_PortafoliosModificados = new ArrayList<PortafoliosModificados>();	
	    	p_DetalleOrigenTraspaso = new ArrayList<DetalleOrigenTraspaso>();
	    }
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CargaMasivaArchivo_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_IDSCARGAMASIVA;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CargaMasivaArchivo_Salida(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_IDSCARGAMASIVA = new ArrayList<Long>();
		}		
	};
			
	public CargaMasivaArchivo_Salida logica(Connection con, CargaMasivaArchivo_Entrada in) throws Exception {
		CargaMasivaArchivo_Salida out = new CargaMasivaArchivo_Salida();
		SQL sql = new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		MensajeSalida msg;
		
		/*Parametro para saber si se autoriza por el blotter de autorizacion o no.*/
		//String envioBlotterAutorizacion = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "PLATAFORMA","APV.AutorizaGestionContrato");
		
		try {
			//******************************************************************************************************************
			//													LOGICA
			//******************************************************************************************************************
			
			//recibo lista de contratos
			if(in.p_Archivo.size() > 0){
				List<Long> idsCargaArchivo = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva", (long)in.p_Archivo.size());
				in.p_Archivo.get(0).IdCargaMasiva = idsCargaArchivo.get(0);
				in.p_Archivo.get(0).FechaCarga = fechaSistema;

				int[] res = sql.batch(con, SQL_INSERT_CARGA_ARCHIVO, in.p_Archivo, CargaArchivo.class);
				if(res!=null && res.length>0 && res[0]==1){
					
					/*
					msg = new MensajeSalida();
					msg.id = 1L;
					msg.tipo = "INFORMACION";
					msg.codigo = "CARGA_MASIVA_OK";
					msg.descripcion = "Carga masiva de contratos exitosa.";
					out.P_OUT_MENSAJES.add(msg);
					*/
					out.P_IDSCARGAMASIVA.add(idsCargaArchivo.get(0));
					
					
					if(in.p_ParametrosContratos.size() > 0){					
						for(ParametrosContrato paramcont :in.p_ParametrosContratos){
							paramcont.IdCargaMasiva = idsCargaArchivo.get(0);
						}
						
						GrabarContratoSAAPV grabacontrato = new GrabarContratoSAAPV();
						ParametrosEntradaServicio paramentrada = new ParametrosEntradaServicio();
						
						paramentrada.p_IdEmpresa = in.p_IdEmpresa;
						paramentrada.p_IdModulo = in.p_IdModulo;
						paramentrada.p_IdUsuario = in.p_IdUsuario;
						paramentrada.p_Tipo = "I";
						paramentrada.p_Parametros = in.p_ParametrosContratos;
						paramentrada.p_DistribucionInversion = in.p_DistribucionInversion;
						paramentrada.p_PortafoliosModificados = in.p_PortafoliosModificados;
						paramentrada.p_DetalleOrigenTraspaso = in.p_DetalleOrigenTraspaso;
						ParametrosSalidaServicio paramsalida = grabacontrato.logica(con, paramentrada);
						
						if(paramsalida.P_OUT_ERRORESCONTADOR == null)
						{
							for(MensajeSalida mensaje :paramsalida.P_OUT_MENSAJES){
								msg = new MensajeSalida();
								msg.id = mensaje.id;
								msg.tipo = mensaje.tipo;
								msg.codigo = mensaje.codigo;
								msg.descripcion = mensaje.descripcion;
								out.P_OUT_MENSAJES.add(msg);
							};
						}
					}else{
						msg = new MensajeSalida();
						msg.id = 1L;
						msg.tipo = "ERROR";
						msg.codigo = "CARGA_CONTRATO_ERROR";
						msg.descripcion = "La tabla de contrato viene vacia.";
						out.P_OUT_MENSAJES.add(msg);
					}
				}else{
					msg = new MensajeSalida();
					msg.id = 1L;
					msg.tipo = "ERROR";
					msg.codigo = "CARGA_MASIVA_ERROR";
					msg.descripcion = "Carga masiva de contratos con problemas de ingreso.";
					out.P_OUT_MENSAJES.add(msg);
				}
			}	
		}
		catch(Exception error){
			msg = new MensajeSalida();
			msg.id = 1L;
			msg.tipo = "ERROR";
			msg.codigo = "CARGA_CONTRATOS_ERROR";
			msg.descripcion = error.getMessage();
			out.P_OUT_MENSAJES.add(msg);
			out.P_OUT_ERRORESCONTADOR = (long)out.P_OUT_MENSAJES.size(); 
			//error.printStackTrace();
		}
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		CargaMasivaArchivo_Entrada jin = gson.fromJson(in.toString() ,CargaMasivaArchivo_Entrada.class);
		CargaMasivaArchivo_Salida out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
			
				String json = "";
				
				CargaMasivaArchivo_Entrada Objin = gson.fromJson(json, CargaMasivaArchivo_Entrada.class);
				
				CargaMasivaArchivo demo = new CargaMasivaArchivo();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try {
	 				CargaMasivaArchivo_Salida ObjOut = demo.logica(con, Objin);
	 				if(ObjOut.P_OUT_ERRORESCONTADOR != null){
	 					con.rollback();
	 				}
	 				else {
	 					con.commit();	
	 				}
	 				System.out.println(ObjOut.toString());
	 			}
	 			catch(Exception er) {
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