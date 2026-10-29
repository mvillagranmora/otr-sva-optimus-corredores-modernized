 package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.CrearCliente.AdvertenciaDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.ErrorDetalleCall;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarTotalesOrigenPorDeposito implements JavaCall{	
	
	private final String SQL_CONSULTAR_TOTALES_ORIGEN_POR_DEPOSITO = loadResourceToString("cl/bcs/APV/sql/Consultar_Totales_Origen_Call.sql");
	
	public static class ParametroRecaudacion {
		public Long					IdRecaudacion;
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class Parametros_Entrada {
	    public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public Long				p_IdRecaudacion;
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class  Parametros_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<TotalesOrigen>	P_TOTALES_ORIGEN;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public Parametros_Salida(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_TOTALES_ORIGEN = new ArrayList<TotalesOrigen>();
		}
	};
		
	public static class TotalesOrigen{
		public String 		Origen;
		public BigDecimal 	Total;
	}
	
	public Parametros_Salida logica(Connection con, Parametros_Entrada in) throws Exception {
		Parametros_Salida out = new Parametros_Salida();
		SQL sql = new SQL();
				
		Parametros_Entrada inParametros = new Parametros_Entrada();
		

		HashMap<String,Object> paramsIn = new HashMap<String,Object>();
		paramsIn.put("IdEmpresa", in.p_IdEmpresa);
		paramsIn.put("IdUsuario", in.p_IdUsuario);
		paramsIn.put("IdModulo", in.p_IdModulo);
		paramsIn.put("IdRecaudacion", in.p_IdRecaudacion);
			
		//Salida HashMap
		HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
		paramsOut.put("p_out_TotalesOrigen", TotalesOrigen.class);
		paramsOut.put("p_out_AdvertenciasContador", Long.class);
		paramsOut.put("p_out_ErroresContador", Long.class);
		paramsOut.put("p_out_ErroresDetalle", ErrorDetalleCall.class);
		paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);
		
		HashMap<String,Object> resultSP = sql.call(con, SQL_CONSULTAR_TOTALES_ORIGEN_POR_DEPOSITO, paramsIn, paramsOut);
		
		if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
			out.P_TOTALES_ORIGEN =  (List<TotalesOrigen>) resultSP.get("p_out_TotalesOrigen");
		}else{
			MensajeSalida mensajeSalida = new MensajeSalida();
			for(ErrorDetalleCall errorDetalle : (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle")){
				mensajeSalida.codigo = errorDetalle.Codigo;
				mensajeSalida.descripcion = errorDetalle.Descripcion;
				out.P_OUT_MENSAJES.add(mensajeSalida);
			}
			for(AdvertenciaDetalleCall advertenciaDetalle : (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle")){
				mensajeSalida.codigo = advertenciaDetalle.Codigo;
				mensajeSalida.descripcion = advertenciaDetalle.Descripcion;
				out.P_OUT_MENSAJES.add(mensajeSalida);
			}
		}
		
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		Parametros_Entrada jin = gson.fromJson(in.toString() ,Parametros_Entrada.class);
		Parametros_Salida out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				//Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
			
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"APV\",\"p_Opcion\":0,\"p_ContratosSAAPV\":[{\"IdPersona\":179580,\"IdCliente\":1759213,\"IdEmpleador\":1759213,\"CodigoTipoCargoEmpleado\":\"JEFCUS\",\"IngresoMensual\":0,\"FechaIngresoCargo\":\"0001-01-01\",\"FechaRetiroCargo\":\"0001-01-01\",\"IdEstado\":\"VIG\"}]}";
				
				Parametros_Entrada Objin = new Parametros_Entrada();
				
				Objin.p_IdEmpresa = 1L;
				Objin.p_IdUsuario = 17L;
				Objin.p_IdModulo = "APV";
				Objin.p_IdRecaudacion = 71L;
				
				ConsultarTotalesOrigenPorDeposito demo = new ConsultarTotalesOrigenPorDeposito();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try {
	 				Parametros_Salida ObjOut = demo.logica(con, Objin);
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