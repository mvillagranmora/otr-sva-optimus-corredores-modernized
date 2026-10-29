package cl.bcs.custodia;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaDividendosEnDinero implements JavaCall {
    
    private final Log log = new Log("CUS.CargaDividendosEnDinero");
    
    private final String SQL_CALL_CREAR_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapital_CallCrearEventoCapital.sql");
    private final String SQL_SELECT_ERRORES_DETALLE = loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapital_SelectErroresDetalle.sql");
    
    public static class InCargaDividendosEnDinero{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Dividendos[] p_Dividendos;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

    public static class OutCargaDividendosEnDinero{
    	public String P_OUT_MENSAJE;
    	public Long P_OUT_ERRORESCONTADOR;
    	public List<EventoCapitalErroresDetalle> DETALLE_ERRORES_LIST;
		public Long p_out_Secuencia;

    	public String toString() {
    		return Utils.printJson(this, false);
    	}
    }

	public static class InformacionDetalle {
		public String	Codigo;
		public String	Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ErroresDetalle {
		public String	Codigo;
		public String	Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class AdvertenciasDetalle {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
    
    public static class Dividendos{
		public String DDFM_INSTRUMENTO;
		public Long DDFM_NUMERO_DIVIDENDO;
		public String DDFM_FECHA_LIMITE;
		public String DDFM_FECHA_PAGO;
		public Long DDFM_SECUENCIA;
		public String DDFM_DESCRIPCION;
		public Double DDFM_MONTO;
		public Double DDFM_FACTOR;
		public String DDFM_MONEDA;
		public String DDFM_MONEDA_LIQUIDACION;
		public Double DDFM_VALOR_O_FACTOR;
		public Double DDFM_CUOTAS;
		public String MENSAJEVALIDACION;
    }
    
    public static class EventoCapitalErroresDetalle{
    	public Long IdInterno;
		public String Codigo;
		public String Descripcion;
    }
    
    @Override
    public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaDividendosEnDinero in = gson.fromJson(jin.toString() , InCargaDividendosEnDinero.class);
		OutCargaDividendosEnDinero out = logica(conn,in);
		JsonParser parser = new JsonParser();
			JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();	
		return dataout;
    }
    
    public OutCargaDividendosEnDinero logica(Connection con, InCargaDividendosEnDinero entrada) throws Exception {
		OutCargaDividendosEnDinero out = new OutCargaDividendosEnDinero();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.DETALLE_ERRORES_LIST = new ArrayList<EventoCapitalErroresDetalle>();
		SQL sql = new SQL();
		Long responseErrors = 0L;
		log.write("Inicio");
		List<Long> eventoCapitalSecuencia;
		int contadorAuxiliar=0;
		try {
			eventoCapitalSecuencia= Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapitalSecuencia", new Long(entrada.p_Dividendos.length));
			for (Dividendos dividendo :  entrada.p_Dividendos) {
				if("OK".equalsIgnoreCase(dividendo.MENSAJEVALIDACION)){//Mensaje de validacion generado por UI
					HashMap<String,Object> parmsIn =new HashMap<String,Object>();
					parmsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
					parmsIn.put("p_IdUsuario", entrada.p_IdUsuario);
					parmsIn.put("p_IdModulo", entrada.p_IdModulo);					
					parmsIn.put("p_IdEventoCapital", 0L);
					parmsIn.put("p_IdTipoEventoCapital", "DF");
					parmsIn.put("p_Secuencia", eventoCapitalSecuencia.get(contadorAuxiliar));
					parmsIn.put("p_Descripcion", dividendo.DDFM_DESCRIPCION);
					parmsIn.put("p_EsInscrito", "");
					parmsIn.put("p_IdClasificacionTipoInstrumento", "FM");
					parmsIn.put("p_IdTipoInstrumento", "FFM");
					parmsIn.put("p_IdInstrumento", 0L);
					parmsIn.put("p_Nemo", dividendo.DDFM_INSTRUMENTO);
					parmsIn.put("p_Factor", dividendo.DDFM_FACTOR);
					parmsIn.put("p_Impuesto", 0L);
					parmsIn.put("p_IdMoneda", dividendo.DDFM_MONEDA);
					parmsIn.put("p_IdMonedaLiquidacion", dividendo.DDFM_MONEDA_LIQUIDACION);
					parmsIn.put("p_Paridad", 0L);
					parmsIn.put("p_IdInstrumentoNuevo", 0L);
					parmsIn.put("p_NemoNuevo", "");
					parmsIn.put("p_FactorNuevo", dividendo.DDFM_VALOR_O_FACTOR);
					parmsIn.put("p_FechaActualizacion", "");
					parmsIn.put("p_FechaLimite", dividendo.DDFM_FECHA_LIMITE);
					parmsIn.put("p_FechaPago", dividendo.DDFM_FECHA_PAGO);
					parmsIn.put("p_NumeroDividendo", dividendo.DDFM_NUMERO_DIVIDENDO);			
					parmsIn.put("p_IdTipoDividendo", "");
					parmsIn.put("p_PrecioAnterior", 0L);
					parmsIn.put("p_FechaHoraIngresoRegistro", "");
					parmsIn.put("p_FechaHoraUltimaActualizacion", "");
					parmsIn.put("p_TipoIngreso", 0L);
					parmsIn.put("p_IdEstado", "");
					parmsIn.put("p_IdEmisor", "");
					parmsIn.put("p_UsaReglasNegocio", "");
					parmsIn.put("p_IdEmisorNuevo", "");			
					parmsIn.put("p_Monto", dividendo.DDFM_MONTO);
					parmsIn.put("p_Cuotas", dividendo.DDFM_CUOTAS);
					
					@SuppressWarnings("rawtypes")
					HashMap<String,Class> parmsOut =new HashMap<String,Class>();
					parmsOut.put("p_out_InformacionContador", Long.class);
					parmsOut.put("p_out_InformacionDetalle", InformacionDetalle.class);
					parmsOut.put("p_out_AdvertenciasContador", Long.class);
					parmsOut.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class);
					parmsOut.put("p_out_ErroresContador", Long.class);
					parmsOut.put("p_out_ErroresDetalle", ErroresDetalle.class);
					parmsOut.put("p_out_Secuencia", Long.class);
										
					HashMap<String,Object> responseCrearEventoCapital = sql.call(con, SQL_CALL_CREAR_EVENTO_CAPITAL, parmsIn, parmsOut);
					responseErrors = (Long) responseCrearEventoCapital.get("p_out_ErroresContador");
					
					if(responseErrors > 0){
						HashMap<String,Object> parmsInError =new HashMap<String,Object>();
						parmsInError.put("IdInterno", dividendo.DDFM_NUMERO_DIVIDENDO);
						List<EventoCapitalErroresDetalle> erroresDetalle = sql.query(con, SQL_SELECT_ERRORES_DETALLE, parmsInError, EventoCapitalErroresDetalle.class);
						
						for (EventoCapitalErroresDetalle eventoCapitalErroresDetalle : erroresDetalle) {
							out.DETALLE_ERRORES_LIST.add(eventoCapitalErroresDetalle);
						}
						out.P_OUT_ERRORESCONTADOR += 1L;
						out.P_OUT_MENSAJE = "UI_ERROR";
					} else {
						out.P_OUT_ERRORESCONTADOR = 0L;
						out.P_OUT_MENSAJE = "UI_EXITO";
						out.p_out_Secuencia = (Long) responseCrearEventoCapital.get("p_out_Secuencia");
					}
					contadorAuxiliar= contadorAuxiliar+1;
				} else{
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJE = "UI_ERROR";
				}
			}
		} catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "UI_ERROR";
			log.write("Error", e);
		}
		log.write("Termino");
		return out;
    }
    
    public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_Dividendos\":[{\"DDFM_INSTRUMENTO\":\"FMLIQUIDEZ\",\"DDFM_NUMERO_DIVIDENDO\":136,\"DDFM_FECHA_LIMITE\":\"2018-03-06\",\"DDFM_FECHA_PAGO\":\"2018-03-28\",\"DDFM_SECUENCIA\":137,\"DDFM_DESCRIPCION\":\"TESTDESC17\",\"DDFM_MONTO\":138,\"DDFM_FACTOR\":139,\"DDFM_MONEDA\":\"CLP\",\"DDFM_MONEDA_LIQUIDACION\":\"CLP\",\"DDFM_VALOR_O_FACTOR\":140,\"DDFM_CUOTAS\":141,\"MENSAJEVALIDACION\":\"OK\"},{\"DDFM_INSTRUMENTO\":\"FMLIQUIDEZ\",\"DDFM_NUMERO_DIVIDENDO\":142,\"DDFM_FECHA_LIMITE\":\"2018-03-06\",\"DDFM_FECHA_PAGO\":\"2018-03-28\",\"DDFM_SECUENCIA\":143,\"DDFM_DESCRIPCION\":\"TESTDESC18\",\"DDFM_MONTO\":144,\"DDFM_FACTOR\":145,\"DDFM_MONEDA\":\"CLP\",\"DDFM_MONEDA_LIQUIDACION\":\"CLP\",\"DDFM_VALOR_O_FACTOR\":146,\"DDFM_CUOTAS\":147,\"MENSAJEVALIDACION\":\"OK\"}]}";
			InCargaDividendosEnDinero Objin = gson.fromJson(json, InCargaDividendosEnDinero.class);
			
			CargaDividendosEnDinero demo=new CargaDividendosEnDinero();				
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");
			try {
				OutCargaDividendosEnDinero ObjOut= demo.logica(con, Objin);
				//con.commit();
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
			con.commit();	
		}	
		System.out.println("== END ==");
    }

}
