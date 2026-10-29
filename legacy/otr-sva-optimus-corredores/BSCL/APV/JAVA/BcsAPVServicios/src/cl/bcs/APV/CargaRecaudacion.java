package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CrearRecaudacionDeposito;
import cl.bcs.APV.CrearRecaudacionDeposito.ParametrosRecaudacion;
import cl.bcs.APV.CrearRecaudacionDeposito.ParametrosEntradaServicio;
import cl.bcs.APV.CrearRecaudacionDeposito.ParametrosSalidaServicio;
import cl.bcs.APV.CrearRecaudacionDeposito.Recaudacion;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaRecaudacion implements JavaCall{	
	
	private final Log log = new Log("CargaRecaudacionAPV");
	
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

	public static class pRecaudacion {
		public Long IdCarga;
		public String FechaDeposito;
		public String ConceptoRecaudacion;
		public Long MontoDeposito;
		public Long Banco;
		public int CuentaCorriente;
		public String Sucursal;
		public String NumeroDocumento;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class CargaRecaudacion_Entrada {
	    public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public List<CargaArchivo> p_Archivo;
	    public List<Recaudacion>	p_ParametrosRecaudacion;
	    
	    public CargaRecaudacion_Entrada(){
	    	p_Archivo = new ArrayList<CargaArchivo>();
	    	p_ParametrosRecaudacion = new ArrayList<Recaudacion>();
		}
	   
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CargaRecaudacion_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_IDSCARGAMASIVA;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CargaRecaudacion_Salida(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_IDSCARGAMASIVA = new ArrayList<Long>();
		}		
	};
			
	public CargaRecaudacion_Salida logica(Connection con, CargaRecaudacion_Entrada in) throws Exception {
		CargaRecaudacion_Salida out = new CargaRecaudacion_Salida();
		SQL sql = new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		MensajeSalida msg = new MensajeSalida();
		
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
					msg.id = 1L;
					msg.tipo = "APV";
					msg.codigo = "CARGA_MASIVA_OK";
					msg.descripcion = "Carga masiva de recaudacion exitosa.";
					out.P_IDSCARGAMASIVA.add(idsCargaArchivo.get(0));
					out.P_OUT_MENSAJES.add(msg);
					
					if(in.p_ParametrosRecaudacion.size() > 0){
						for(Recaudacion paramcont :in.p_ParametrosRecaudacion){
							paramcont.IdCarga = idsCargaArchivo.get(0);
						}
						
						CrearRecaudacionDeposito grabaRecaudacion = new CrearRecaudacionDeposito();
						ParametrosEntradaServicio paramentrada = new ParametrosEntradaServicio();
						
						paramentrada.p_IdEmpresa = in.p_IdEmpresa;
						paramentrada.p_IdModulo = in.p_IdModulo;
						paramentrada.p_IdUsuario = in.p_IdUsuario;
						paramentrada.p_Tipo = "I";
						paramentrada.p_Origen = "CARGA MASIVA";
						paramentrada.p_Parametros = in.p_ParametrosRecaudacion;
						ParametrosSalidaServicio paramsalida = grabaRecaudacion.logica(con, paramentrada);
						
						if(paramsalida.P_OUT_ERRORESCONTADOR == null)
						{
							for(MensajeSalida mensaje :paramsalida.P_OUT_MENSAJES){
								msg.id = mensaje.id;
								msg.tipo = mensaje.tipo;
								msg.codigo = mensaje.codigo;
								msg.descripcion = mensaje.descripcion;
								out.P_OUT_MENSAJES.add(msg);
							};
						}
					}else{
						msg.id = 3L;
						msg.tipo = "APV";
						msg.codigo = "CARGA_DEPOSITO_ERROR";
						msg.descripcion = "La tabla de deposito viene vacia.";
						out.P_OUT_MENSAJES.add(msg);
					}
				}else{
					msg.id = 4L;
					msg.tipo = "APV";
					msg.codigo = "CARGA_MASIVA_ERROR";
					msg.descripcion = "Carga masiva de recaudacion con problemas de ingreso.";
					out.P_OUT_MENSAJES.add(msg);
				}
			}
			
		}
		catch(Exception error){
			msg.id = 5L;
			msg.tipo = "APV";
			msg.codigo = "CARGA_ERROR_JSON";
			msg.descripcion = error.getMessage();
			out.P_OUT_MENSAJES.add(msg);
			error.printStackTrace();
		}		
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		CargaRecaudacion_Entrada jin = gson.fromJson(in.toString() ,CargaRecaudacion_Entrada.class);
		CargaRecaudacion_Salida out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
			
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":39,\"p_IdModulo\":\"APV\",\"p_Archivo\":[{\"IdEmpresa\":1,\"NombreCargaMasiva\":\"Depositos.txt\",\"CantidadRegistros\":2,\"IdUsuario\":39,\"IdTipoCarga\":4,\"Descripcion\":\"\",\"FechaCarga\":\"2018-03-13\",\"IdEstado\":\"VIG\"}],\"p_ParametrosRecaudacion\":[{\"IdEmpresa\":1,\"IdRecaudacion\":0,\"FolioCartola\":0,\"LineaCartola\":0,\"Sucursal\":\"SUCUR\",\"FechaDeposito\":\"2017-05-05\",\"CuentaCorriente\":123412341234,\"MontoDeposito\":1000,\"Observacion\":\"CARGA MASIVA\",\"NumeroDocumento\":\"17580478\",\"ConceptoRecaudacion\":\"00\",\"IdBanco\":\"1\",\"IdTipoDeposito\":\"DEP\",\"IdEstado\":\"ING\",\"Origen\":\"CARGA\",\"IdCarga\":0},{\"IdEmpresa\":1,\"IdRecaudacion\":0,\"FolioCartola\":0,\"LineaCartola\":0,\"Sucursal\":\"SUCU2\",\"FechaDeposito\":\"2017-05-05\",\"CuentaCorriente\":234523452345,\"MontoDeposito\":2000,\"Observacion\":\"CARGA MASIVA\",\"NumeroDocumento\":\"17580478\",\"ConceptoRecaudacion\":\"02\",\"IdBanco\":\"1\",\"IdTipoDeposito\":\"DEP\",\"IdEstado\":\"ING\",\"Origen\":\"CARGA\",\"IdCarga\":0}]}";

				CargaRecaudacion_Entrada Objin = gson.fromJson(json, CargaRecaudacion_Entrada.class);
				
				CargaArchivo cargaarchivo = new CargaArchivo();
				List<CargaArchivo> archivo = new ArrayList<CargaArchivo>();
								
				/*Contrato*/
				
				ParametrosEntradaServicio entradacontrato = new ParametrosEntradaServicio();
				ParametrosRecaudacion PD = new ParametrosRecaudacion();
				String fechaString = "2017-10-11";

				CargaRecaudacion demo=new CargaRecaudacion();		
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try {
	 				CargaRecaudacion_Salida ObjOut = demo.logica(con, Objin);
	 				con.commit();
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