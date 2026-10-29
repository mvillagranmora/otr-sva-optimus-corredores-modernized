package cl.bcs.tesoreria;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarFormatoArchivo implements JavaCall {
	public String SQL_CONSULTAR_FORMATO_ARCHIVO = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarFormatoArchivo_ConsultarCabecera.sql");
	public String SQL_CONSULTAR_FORMATO_ARCHIVO_DETALLE = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarFormatoArchivo_ConsultarDetalles.sql");
	
	public static class EntradaFormatoArchivo {
		public Long	IdFormatoArchivo;
		public Long IdEmpresa;
		public Long IdUsuario;
		
		public String toString(){
			return Utils.printJson(this, false);
		} 
	}
		
	public static class SalidaFormatoArchivo {
		public Long    IdEmpresa;
		public Long    IdUsuario;
		public Long    IdNominaTransferencia;
		public Long    IdFormatoArchivo;
		public String  CodigoFormatoArchivo;
		public Long	   IdBanco;
		public String  NombreBanco;
		public String  IdMoneda;
		public String  IdMedioDePago;
		public String  FormatoSalida;
		public String  Descripcion;
		public String  Servicio;
		public String  RutaTesoreria; 
		public String  RutaCompleta;  
		public Long    CantidadDeTiposDeRegistro;
		public String  NombreArchivoSalida;
		public String  SeparadorCampos;
		public String  SeparadorDecimales;
		public String  RellenarCampo;
		public List<FormatoArchivoDetalle> Detalles;
		
		public String toString(){
			return Utils.printJson(this, false);
		} 
	}
	
	public static class FormatoArchivoDetalle {
		public Long 	IdFormatoArchivoDetalle;
		public String	CodigoFormatoArchivo;		
		public Integer 	TipoRegistro; 
		public String	Campo; 
		public String	TipoDato; 
		public Integer	Largo; 
		public Integer	Decimales; 
		public String 	ValorDefecto; 
		public Integer	Orden; 
		public String 	DescripcionFormatoDetalle;
		public String 	Formato;
		
		public String toString(){
			return Utils.printJson(this, false);
		} 
	}
	
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaFormatoArchivo in = gson.fromJson(jin.toString(), EntradaFormatoArchivo.class);
		List<SalidaFormatoArchivo> out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}
	
	public List<SalidaFormatoArchivo> logica(Connection conn, EntradaFormatoArchivo in) throws Exception {
		SQL sql = new SQL();	
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdFormatoArchivo", in.IdFormatoArchivo);		
		List<SalidaFormatoArchivo> formatoArchivo = sql.query(conn, SQL_CONSULTAR_FORMATO_ARCHIVO, params, SalidaFormatoArchivo.class);
		if(formatoArchivo.isEmpty())
			return formatoArchivo;
		List<FormatoArchivoDetalle> formatoArchivoDetalles = sql.query(conn, SQL_CONSULTAR_FORMATO_ARCHIVO_DETALLE, params, FormatoArchivoDetalle.class);
		
		formatoArchivo.get(0).Detalles = formatoArchivoDetalles;
		formatoArchivo.get(0).IdEmpresa = in.IdEmpresa;
		formatoArchivo.get(0).IdUsuario = in.IdUsuario;
		return formatoArchivo;
	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = PlataformaUtils.getDESAConnection();		
		System.out.println("== BEGIN TEST ==");
		Gson gson = Utils.getGsonBuilder();
		String json = "{\"IdFormatoArchivo\":8, \"IdUsuario\":1, \"IdEmpresa\":3}";
		EntradaFormatoArchivo Objin = gson.fromJson(json, EntradaFormatoArchivo.class);
		ConsultarFormatoArchivo demo = new ConsultarFormatoArchivo();
		List<SalidaFormatoArchivo> ObjOut = demo.logica(conn, Objin);
		System.out.println(ObjOut);
		System.out.println("== END TEST ==");
	}

}
