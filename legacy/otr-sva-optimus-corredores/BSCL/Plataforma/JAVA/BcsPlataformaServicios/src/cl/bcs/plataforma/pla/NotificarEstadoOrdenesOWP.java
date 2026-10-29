package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.bat.InscribirProcesoBatch;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InTipoPrograma;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InscribirProcesoBatchEntrada;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.Parametro;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarEstadoOrdenesOWP implements JavaCall{	
	
	private final Log log = new Log("PLA.NotificarOWP");
	private final String SQL_SELECT_ESTADOOWP= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarHomologacionEstadoOrden.sql");

	//ENTRADA Y SALIDA DE LA CLASE
	
	public static class NotificarEstadoOrdenes_Entrada {
		public Long IdEmpresa;
		public Long IdUsuario;
		public Long Solsec;
		public String Fecha;
		public String Estado;
		public String Mercado;	
		public BigDecimal NTitulos;
		public BigDecimal Precio;
		public BigDecimal Comision;
		public BigDecimal Gastos;
		public String Divisa;
		public BigDecimal TipoCambio;
		public String SolsecAsoc;
		public String Nemo;
		public String IdClasificacionTipoInstrumento;	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OrdenModificada{
		public Long IdOrden;
		public BigDecimal Cantidad;
		public BigDecimal PrecioOrden;
		public String FechaOperacion;
		public String IdEstadoOrden;
		public String IdMonedaOrden;
		public BigDecimal TipoCambio;
		public String IdClasificacionTipoInstrumento;	
		public String IdEstadoFacturacion;
		public String Nemotecnico;
		public BigDecimal Comision;
		public BigDecimal Gasto;
		

		public BigDecimal Impuesto; // Ej. IVA
		
		public String SolsecAsoc;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class NotificarEstadoOrdenes_Salida {
		public String Message;
		public String RequestCode;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Estado {
		public String IdEstadoOWP;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class AgregarLog{
		public List<AgregarLog.Log> Logs;
		
		public AgregarLog(){
			Logs = new ArrayList<AgregarLog.Log>();
		}
		
	    public static class Log {
			public Long			IdLog; 
			public Long			IdEmpresa; 
			public String		FechaLog; 
			public String		IdCorrelativoOWP; 
			public String		Glosa; 
			public String		Entrada;
			public String		Salida;
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public void go(Connection con, List<NotificarEstadoOrdenes_Entrada> entrada) throws Exception {	
		SQL sql = new SQL();
		          
        JsonArray input = new JsonArray();
        try{
        	//Homologar estado
    		HashMap<String,Object> params = new HashMap<>();
    		params.put("p_Estado", entrada.get(0).Estado);
    		String clasificacion = "A".equalsIgnoreCase(entrada.get(0).Mercado) ? "RV" : "FM";
    		params.put("p_Mercado", clasificacion);
    		
    		List<Estado> _estadosOwp = sql.query(con, SQL_SELECT_ESTADOOWP, params, Estado.class);
    		String estado = _estadosOwp.get(0).IdEstadoOWP;
    		
    		DecimalFormat df = new DecimalFormat();
    		df.setMaximumFractionDigits(2);
    		  		    
        	for (NotificarEstadoOrdenes_Entrada ord : entrada) {
	    		//llenar objeto json
        		JsonObject operacion = new JsonObject();
	        	operacion.addProperty("Solsec", ord.Solsec.toString());//IdOrden
	        	operacion.addProperty("NTitulos", df.format(ord.NTitulos));//Cantidad
	        	operacion.addProperty("Precio", df.format(ord.Precio));
	        	operacion.addProperty("Fecha", ord.Fecha);
	        	operacion.addProperty("Estado", estado);
	        	operacion.addProperty("Comision", df.format(ord.Comision));
	        	operacion.addProperty("Gastos", df.format(ord.Gastos));
	        	operacion.addProperty("Mercado", ord.Mercado);
	        	operacion.addProperty("Divisa", ord.Divisa);
	        	operacion.addProperty("TipoCambio", df.format(ord.TipoCambio));
	        	operacion.addProperty("SolSec_Asoc", ord.SolsecAsoc);
	        	operacion.addProperty("Nemo", ord.Nemo);//Instrumento
	     		input.add(operacion);
	        }
        	
        	InscribirProcesoBatch procesoBatch = new InscribirProcesoBatch();
			InscribirProcesoBatchEntrada entradaProcesoBatch = new InscribirProcesoBatchEntrada();
			
			entradaProcesoBatch.p_Parametro = new ArrayList<Parametro>();
			entradaProcesoBatch.p_TipoPrograma = new ArrayList<InTipoPrograma>();
			
			entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"IdEmpresa",entrada.get(0).IdEmpresa.toString(), null));
			entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"Operaciones","", input.toString()));
		
			
			if(entradaProcesoBatch.p_Parametro.size() > 0){
				InTipoPrograma inTipoPrograma = new InTipoPrograma();
				inTipoPrograma.IdInterno = 0L;
				inTipoPrograma.IdTipoPrograma = "NOTIFICAR_ORDEN_OWP";
				inTipoPrograma.SegundosDesfase = 0L;
				entradaProcesoBatch.p_TipoPrograma.add(inTipoPrograma);
				entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"IdUsuario",entrada.get(0).IdUsuario.toString(), null));
				entradaProcesoBatch.p_IdEmpresa = entrada.get(0).IdEmpresa;
				entradaProcesoBatch.p_IdUsuario = entrada.get(0).IdUsuario;
				entradaProcesoBatch.p_IdModulo = "PLA";
				procesoBatch.logica(con, entradaProcesoBatch);
			}
		
        }catch(Exception e){	
        	log.write("Error no controlado al momento de llamar al servicio OpenWorkPlace: " + e);
        }	
        
	}
	
	
	//PRUEBAS
	public static void main(String[] args) throws Exception{
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			//Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			List<NotificarEstadoOrdenes_Entrada> lista = new ArrayList<NotificarEstadoOrdenes_Entrada>();
			NotificarEstadoOrdenes_Entrada obj = new NotificarEstadoOrdenes_Entrada();
			obj.Solsec = 125907L;
			obj.NTitulos = new BigDecimal(2125.46);
			obj.Precio = new BigDecimal(7443.40000);
			obj.Fecha = "2015-06-29";
			obj.Estado = "FACTURADA";
			obj.Comision = new BigDecimal(0.0000);
			obj.Gastos = new BigDecimal(0.0000);
			obj.Mercado = "F";
			obj.Divisa = "EUR";
			obj.TipoCambio = new BigDecimal(706.01);
			obj.SolsecAsoc = "";
			obj.Nemo  = "CHILE";
			lista.add(obj);
			NotificarEstadoOrdenesOWP demo =new NotificarEstadoOrdenesOWP();				
 			System.out.println("ENTRADA");
 			
 			try {
 				//NotificarEstadoOrdenes_Salida salida = demo.go(con, null);
 				con.rollback();
 				//System.out.println(salida.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
				System.out.println("ERROR");
			}
		}
	}
}



