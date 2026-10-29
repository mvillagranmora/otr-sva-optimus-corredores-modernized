package cl.bcs.plataforma.ins;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;



public class ConsultarNemoNegociacion implements JavaCall {
	private final String SELECT_INSTRUMENTOS_RAIZ  = loadResourceToString("cl/bcs/plataforma/ins/sql/BuscarNemoNegociacion_Select.sql");
 
	public static class ConsultarInstrumentoNegociacion_Entrada{
		public List<DatosInstrumento> p_DatosInstrumentoConsulta;
		public ConsultarInstrumentoNegociacion_Entrada() {
			p_DatosInstrumentoConsulta = new ArrayList<>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosInstrumento{
		public Long IdConsulta = 0L;
		public Long IdInstrumentoRaiz;
		public String IdMoneda;
		public String IdCondLiquidacion;
	}
	
	public static class DatosInstrumentoNegociacion{
		public Long IdConsulta;	
		public Long IdInstrumentoConsultado;
		public String IdMoneda;
		public String IdCondLiquidacion;
		public Long IdInstrumento = 0L;
		public String Nemotecnico = "";
	}
	
	public static class DatosInstrumentoNegociacion_Salida{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public long	P_OUT_ERRORESCONTADOR;
		public List<DatosInstrumentoNegociacion> P_InstrumentoSalida;
		public DatosInstrumentoNegociacion_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
			 P_InstrumentoSalida = new ArrayList<>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentoSeleccionado{
		public Long IdInstrumento;
		public String Nemotecnico;
		public Long IdInstrumentoRaiz;
		public String IdMoneda;
		public String IdCondicionLiquidacion;
	}

	public DatosInstrumentoNegociacion_Salida logica (Connection conn, ConsultarInstrumentoNegociacion_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		DatosInstrumentoNegociacion_Salida salidaInstrumentoNegociacion = new DatosInstrumentoNegociacion_Salida();

		List<DatosInstrumento> listaDatosEntrada = entrada.p_DatosInstrumentoConsulta;
		List<DatosInstrumentoNegociacion> listaInstrumentoNegociacion = new ArrayList<>();
		
		List<InstrumentoSeleccionado> listaInstrumentos = new ArrayList<>();		
		HashMap<String,Object> selectInstrumentoNegociacion = new HashMap<String,Object>();		
		List<Long> listaIdInstrumentos = new ArrayList<>();
		
		for(DatosInstrumento datosInstrumento:listaDatosEntrada){
			DatosInstrumentoNegociacion InstrumentoNegociacion = new DatosInstrumentoNegociacion();
			InstrumentoNegociacion.IdConsulta = datosInstrumento.IdConsulta;
			InstrumentoNegociacion.IdInstrumentoConsultado = datosInstrumento.IdInstrumentoRaiz;
			InstrumentoNegociacion.IdMoneda = datosInstrumento.IdMoneda;
			InstrumentoNegociacion.IdCondLiquidacion = datosInstrumento.IdCondLiquidacion;
			listaInstrumentoNegociacion.add(InstrumentoNegociacion);
			
			listaIdInstrumentos.add(InstrumentoNegociacion.IdInstrumentoConsultado);
		}		
		selectInstrumentoNegociacion.put("IdInstrumento", listaIdInstrumentos);
		
		listaInstrumentos = _sql.query(conn, SELECT_INSTRUMENTOS_RAIZ,selectInstrumentoNegociacion, InstrumentoSeleccionado.class);
		
		Map<String, InstrumentoSeleccionado> listaInstrumentosPorIdInstrumentoRaizCodicionLiquidacionMoneda = listaInstrumentos.stream().collect(Collectors.toMap(instrumento->instrumento.IdInstrumentoRaiz + "_" + instrumento.IdCondicionLiquidacion + "_" + instrumento.IdMoneda, instrumento->instrumento, (primerInstrumento, segundoInstrumento)-> primerInstrumento));
		Map<String, InstrumentoSeleccionado> listaInstrumentosPorIdInstrumentoMoneda = listaInstrumentos.stream().collect(Collectors.toMap(instrumento->instrumento.IdInstrumento + "_" + instrumento.IdMoneda, instrumento->instrumento, (primerInstrumento, segundoInstrumento)-> primerInstrumento));

		HashMap<String,String> mapMsg = new HashMap<>();
		for(DatosInstrumentoNegociacion datosInstrumento:listaInstrumentoNegociacion){
			InstrumentoSeleccionado  instrumentoPorIdInstrumentoRaizCodicionLiquidacionMoneda = listaInstrumentosPorIdInstrumentoRaizCodicionLiquidacionMoneda.get(datosInstrumento.IdInstrumentoConsultado + "_" + datosInstrumento.IdCondLiquidacion + "_" + datosInstrumento.IdMoneda);
			InstrumentoSeleccionado instrumentoPorIdInstrumentoMoneda = listaInstrumentosPorIdInstrumentoMoneda.get(datosInstrumento.IdInstrumentoConsultado + "_" + datosInstrumento.IdMoneda);
			if(instrumentoPorIdInstrumentoRaizCodicionLiquidacionMoneda != null) {
				datosInstrumento.IdInstrumento = instrumentoPorIdInstrumentoRaizCodicionLiquidacionMoneda.IdInstrumento;
				datosInstrumento.Nemotecnico = instrumentoPorIdInstrumentoRaizCodicionLiquidacionMoneda.Nemotecnico;
			}
			else if (instrumentoPorIdInstrumentoMoneda != null) {
				datosInstrumento.IdInstrumento = instrumentoPorIdInstrumentoMoneda.IdInstrumento;
				datosInstrumento.Nemotecnico = instrumentoPorIdInstrumentoMoneda.Nemotecnico;
			}
			if(datosInstrumento.IdInstrumento == 0L){
				mapMsg.clear();
				salidaInstrumentoNegociacion.P_OUT_ERRORESCONTADOR = 1L;
				salidaInstrumentoNegociacion.P_OUT_MENSAJES.add(new MensajeSalida(datosInstrumento.IdConsulta,MensajeSalida.MENSAJE_ERROR,"INSTRUMENTO_NEGOCIACION_NO_EXISTE",mapMsg,conn,1l));
			}
		}
	
		salidaInstrumentoNegociacion.P_InstrumentoSalida = listaInstrumentoNegociacion;
		return salidaInstrumentoNegociacion;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarInstrumentoNegociacion_Entrada jin = gson.fromJson(in.toString(), ConsultarInstrumentoNegociacion_Entrada.class);
		DatosInstrumentoNegociacion_Salida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws SQLException {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json2 = "{\"p_DatosInstrumentoConsulta\":[{\"IdInstrumentoRaiz\":502,\"IdMoneda\":\"USD\",\"IdCondLiquidacion\":\"T1\"}]}";
			String json3 = "{\"p_DatosInstrumentoConsulta\":[{\"IdConsulta\":1,\"IdInstrumentoRaiz\":503,\"IdMoneda\":\"CLP\",\"IdCondLiquidacion\":\"T0\"}"
														+ ",{\"IdConsulta\":2,\"IdInstrumentoRaiz\":504,\"IdMoneda\":\"USD\",\"IdCondLiquidacion\":\"T1\"}"
														+ ",{\"IdConsulta\":3,\"IdInstrumentoRaiz\":502,\"IdMoneda\":\"USD\",\"IdCondLiquidacion\":\"T0\"}]}";
			ConsultarInstrumentoNegociacion_Entrada Objin = gson.fromJson(json3, ConsultarInstrumentoNegociacion_Entrada.class);
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			ConsultarNemoNegociacion demo = new ConsultarNemoNegociacion();
 			try {
 				DatosInstrumentoNegociacion_Salida ObjOut = demo.logica(con, Objin);			    
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
 				System.out.println(er.toString());
				con.rollback();
			}
	    }catch(Exception b){
	    	b.printStackTrace();
	    	System.out.println(b.toString());
	    }
		 	
		System.out.println("== END ==");
	}

}
