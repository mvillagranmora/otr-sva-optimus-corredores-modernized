package cl.bcs.facturacion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.CrearComprobante.CrearComprobante_Entrada;
import cl.bcs.facturacion.CrearComprobante.CrearComprobante_Salida;
import cl.bcs.facturacion.CrearComprobante.MovimientoComprobante_Entrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EmisionMasivaComprobante implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(EmisionMasivaComprobante.class);
	
	private final String SQL_SELECT_MOVIMIENTOS_I = loadResourceToString("cl/bcs/facturacion/sql/EmisionMasivaComprobantea_SelectMovimientosI.sql");
	private final String SQL_SELECT_MOVIMIENTOS_O = loadResourceToString("cl/bcs/facturacion/sql/EmisionMasivaComprobantea_SelectMovimientosO.sql");
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/EmisionMasivaComprobantea_SelectMovimientos.sql");

	/*******************
	 * clases in/out
	 *******************/
	
	public static class InEmisionComprobante{
		
		public Long							p_IdEmpresa;		
		public Long							p_IdUsuario;   
		public String					    p_IdModulo;  
		
		public String	 					p_IdClasificacionTipoInstrumento;
		public String						p_IdTipoInstrumento;
		public String						p_IdTipoComprobante;
		public String						p_IdCondicionLiquidacion;
		public String						p_IdBoveda;
		public String 						p_FechaMovimientoComprobante;
		public String						p_Glosa;
		public String 						p_SelCliente;
				
		public String						p_simularOperacion;				
		public String						p_UsaReglasNegocio;				
		public String						p_EsIngresoAutomatico = "N";
		public JsonObject				    p_MovimientoFacturarjson;
		public String						p_Ruta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}

	} 
	
	public static class OutEmisionComprobante{
		
		public String 	P_OUT_NOMBREARCHIVO;
		public String 	P_OUT_RUTAARCHIVOLOG; 
		public Long 	P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public OutEmisionComprobante(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Movimiento{
		public Long IdPortafolio;
		public Long IdCliente;
		public Long IdMovimientoComprobante;
		public String IdTipoComprobante;
		public Date FechaComprobante;
		public String NombreLargoCliente;
		public String DocumentoIdentidad;
		public String NemotecnicoInstrumento;
		
		public String toLine() {
			return "Movimiento:" + this.IdMovimientoComprobante + "\tDocumentoIdentidad:" + this.DocumentoIdentidad + "\tCliente:" + this.NombreLargoCliente+ "\tNemotecnico:" + this.NemotecnicoInstrumento;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();
		InEmisionComprobante in=gson.fromJson(jin.toString() ,InEmisionComprobante.class);
		OutEmisionComprobante out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	
	
	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutEmisionComprobante logica(Connection con, InEmisionComprobante entrada) throws Exception {
		
		 OutEmisionComprobante salida= new  OutEmisionComprobante();
		 
		 SimpleDateFormat fm=new SimpleDateFormat("yyyyMMddHHmmss");
		 salida.P_OUT_NOMBREARCHIVO="FAC/LOG_COMPROBANTES_MASIVOS_"+entrada.p_IdUsuario+"_"+fm.format(new java.util.Date())+".txt";
		 salida.P_OUT_RUTAARCHIVOLOG=entrada.p_Ruta+"/"+salida.P_OUT_NOMBREARCHIVO;
		 
		File folder = new File(entrada.p_Ruta+"/FAC");
		if(!folder.exists()){
			folder.mkdirs();
		}				 
		 
		 File archivoLog = new File ( salida.P_OUT_RUTAARCHIVOLOG);
		 BufferedWriter bw;
		 bw = new BufferedWriter(new FileWriter(archivoLog));
		
		 try{
			 
			 SQL sql = new SQL();
			 
			 List<Long> clientes = new ArrayList<Long>();
			 List<Long> portafolios = new ArrayList<Long>();
			 for(JsonElement cliente : entrada.p_MovimientoFacturarjson.getAsJsonArray("p_MovimientoFacturarjson").getAsJsonArray()){
				  Long idCliente = cliente.getAsJsonObject().get("IdCliente").getAsLong();
				  Long idPortafolio = cliente.getAsJsonObject().get("IdPortafolio").getAsLong();
				  if(!clientes.contains(idCliente)) clientes.add(idCliente);
				  if(!portafolios.contains(idPortafolio)) portafolios.add(idPortafolio);
			 }
			 HashMap<String,Object> params = new HashMap<String,Object>();
			 params.put("IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
			 params.put("IdTipoInstrumento", entrada.p_IdTipoInstrumento);
			 params.put("IdBoveda", entrada.p_IdBoveda);
			 params.put("IdTipoComprobante", entrada.p_IdTipoComprobante);
			 params.put("IdCondicionLiquidacion", entrada.p_IdCondicionLiquidacion);
			 params.put("FechaMovimientoComprobante", entrada.p_FechaMovimientoComprobante);
			 List<Movimiento> mov = new ArrayList<Movimiento>();
			 if("I".equals(entrada.p_SelCliente)){
				params.put("IdCliente", clientes);
				params.put("IdPortafolio", portafolios);
				mov = sql.query(con, SQL_SELECT_MOVIMIENTOS_I, params, Movimiento.class);
			 }
			 else if("O".equals(entrada.p_SelCliente)){
				 params.put("IdCliente", clientes);
				 mov = sql.query(con, SQL_SELECT_MOVIMIENTOS_O, params, Movimiento.class);
			 }else{
				 mov = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, Movimiento.class);
			 }
			 
			 bw.write("FILTROS*********\n");
			 bw.write("IdClasificacionTipoInstrumento:" + entrada.p_IdClasificacionTipoInstrumento + "\n");
			 bw.write("IdTipoInstrumento:" + entrada.p_IdTipoInstrumento + "\n");
			 bw.write("IdBoveda:" + entrada.p_IdBoveda + "\n");
			 bw.write("IdTipoComprobante:" + entrada.p_IdTipoComprobante + "\n");
			 bw.write("IdCondicionLiquidacion:" + entrada.p_IdCondicionLiquidacion + "\n");
			 bw.write("FechaMovimientoComprobante:" + entrada.p_FechaMovimientoComprobante + "\n");
			 bw.write("CLIENTES:\n");
			 for(Long l : clientes) {
				 bw.write(l+"\t");
			 }
			 bw.write("\n");
			 bw.write("PORTAFOLIOS:\n");
			 for(Long l : portafolios) {
				 bw.write(l+"\t");
			 }
			 bw.write("\n");
			 bw.write("MOVIMIENTOS ENCONTRADOS****\n");
			 for (Movimiento m : mov) {
				 bw.write(m.toLine()+"\n");
			 }
			 if(mov.size() > 0){
				 CrearComprobante crearComprobante = new CrearComprobante();
				 CrearComprobante_Entrada entradaCrearComprobante = new CrearComprobante_Entrada();
				 entradaCrearComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
				 entradaCrearComprobante.p_IdUsuario = entrada.p_IdUsuario;
				 entradaCrearComprobante.p_IdModulo = entrada.p_IdModulo;
				 entradaCrearComprobante.p_Glosa = "";
				 entradaCrearComprobante.p_simularOperacion = "N";
				 entradaCrearComprobante.p_UsaReglasNegocio = "S";
				 entradaCrearComprobante.p_EsIngresoAutomatico = "N";
				 entradaCrearComprobante.p_ServicioAgrupador = "";
				 for(Movimiento m : mov){
					 MovimientoComprobante_Entrada em = new MovimientoComprobante_Entrada();
					 em.IdMovimientoComprobante = m.IdMovimientoComprobante;
					 em.FechaComprobante = Fecha.toStringDate(m.FechaComprobante);
					 entradaCrearComprobante.P_MOVIMIENTOFACTURAR.add(em);
				 }
				 CrearComprobante_Salida salidaCrearComprobante = crearComprobante.logica(con, entradaCrearComprobante);
				 salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaCrearComprobante.P_OUT_MENSAJES);
				 bw.write("RESULTADO*****\n");
				 if (salida.P_OUT_ERRORESCONTADOR > 0) {
					 bw.write("SE ENCONTRARON ERRORES EN EL PROCESO\n");
					 for(MensajeSalida a : salidaCrearComprobante.P_OUT_MENSAJES){
						 if (a.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) {
							 bw.write(a.descripcion+"\n");	 
						 }
					 }
				 }
				 else {
					 salida.P_OUT_MENSAJES.addAll(salidaCrearComprobante.P_OUT_MENSAJES);
					 for(MensajeSalida a : salidaCrearComprobante.P_OUT_MENSAJES){
						 bw.write(a.descripcion+"\n");
					 }	 
				 }
				 
				 
			 }else{
				 MensajeSalida a = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_VIENEN_MOVIMIENTOS-EMISION-MASIVA",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
				 bw.write(a.descripcion+"\n");
				 salida.P_OUT_MENSAJES.add(a);
			 }
		 }
		 catch (Exception e){
			 logger.error(e);
			 bw.write(e.getMessage()+"\n");
			 
			 throw e;
		 }
		 finally{
			 try{
				 logger.info(salida);
				 bw.close();
			 }
			 catch(Exception e){
				 logger.error(e);
			 }
		 }
		 return salida;
	}
	
	public static void main (String [] args) {
 		try {
  			Connection con = PlataformaUtils.getDESAConnection();
  			try{
  				
  				InEmisionComprobante entrada= new InEmisionComprobante();
  				  				
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(4);
  				entrada.p_IdModulo 		= "FAC";
  				
  				entrada.p_IdClasificacionTipoInstrumento ="";
  				entrada.p_IdTipoInstrumento 			 ="";
  				entrada.p_IdTipoComprobante 			 ="";
  				entrada.p_IdCondicionLiquidacion 		 ="";
  				entrada.p_IdBoveda 						 ="";
  				entrada.p_FechaMovimientoComprobante     = "2020-06-01" ;
  				entrada.p_Glosa 						 = "Comprobante Generado Emisión Masiva";
  				entrada.p_SelCliente 					 = "I";    				
  				
  				entrada.p_simularOperacion				 ="N";						
  				entrada.p_UsaReglasNegocio				 ="S";						
  				entrada.p_EsIngresoAutomatico 			 ="N";
  				
  				JsonParser parser     = new JsonParser();
  		        String parMovimientoFacturarjson = "{\"p_MovimientoFacturarjson\":[{\"IdCliente\":1421445,\"IdPortafolio\":1450057},{\"IdCliente\":395174,\"IdPortafolio\":343710}]}";
				JsonObject dataJsonIn  = parser.parse(parMovimientoFacturarjson ).getAsJsonObject();
  				
				entrada.p_MovimientoFacturarjson		 = dataJsonIn;
  				
  				EmisionMasivaComprobante demo= new EmisionMasivaComprobante();
  				JsonObject asdf = demo.call(con, parser.parse(entrada.toString()).getAsJsonObject());
  				con.rollback();
  				
  			}catch(Exception e){
  				con.rollback();
  				e.printStackTrace();
  			}finally{
  				con.close();
  			}
  		}catch (Exception e) {
  			e.printStackTrace();
  		}
 		System.out.println("== END ==");
	}
	
}
