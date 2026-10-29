package cl.bcs.fondomutuo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GeneracionArchivoAGF implements JavaCall{
	
	private final Log log = new Log("GeneracionArchivoAGF");
	
	private final String SQL_SELECT_PARAMETROS = loadResourceToString("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectParametros.sql");
	private final String SQL_SELECT_FACTURAS_COMPRAS = loadResourceToString("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectFacturaCompras.sql");
	private final String SQL_SELECT_FACTURAS_VENTAS = loadResourceToString("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectFacturaVentas.sql");
	private final String SQL_SELECT_COMPRA_TERMINO_FR = loadResourceToString("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectCompraTerminoFR.sql");
	private final String SQL_SELECT_VENTA_TERMINO_FR = loadResourceToString("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectVentaTerminoFR.sql");
	private final String SQL_SELECT_COMPRA_PACTOS = loadResourceToString("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectCompraPactos.sql");
	private final String SQL_SELECT_VENTA_PACTOS = loadResourceToString("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectVentaPactos.sql");
	private final String SQL_SELECT_COMPRA_SIMULTANEA = loadResourceToString("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectCompraSimultanea.sql");
	private final String SQL_SELECT_VENTA_SIMULTANEA = loadResourceToString("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectVentaSimultanea.sql");
	private final String SQL_SELECT_NC_COMPRA_SIMULTANEA = loadResourceToString("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectNotaCreditoCompraSimultanea.sql");
	private final String SQL_SELECT_NC_VENTA_SIMULTANEA = loadResourceToString("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectNotaCreditoVentaSimultanea.sql");
	
	public static class InGeneracionArchivoAGF{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_FechaDesde;
		public String	p_FechaHasta;
		public String	p_IdAmbiente;
		public String	p_Ruta;
	}
	
	public static class OutGeneracionArchivoAGF{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
	}
	
	public static class ParametrosGeneracion{
		public String	CargosAbonos;
		public String	IngresosRetiros;
		public String	NotasCredito;
	}
	
	public static class Operaciones{
		public String Registro;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InGeneracionArchivoAGF in=gson.fromJson(jin.toString() ,InGeneracionArchivoAGF.class);
		OutGeneracionArchivoAGF out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutGeneracionArchivoAGF logica(Connection con, InGeneracionArchivoAGF entrada) throws Exception {
		OutGeneracionArchivoAGF out = new OutGeneracionArchivoAGF();
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		
		String fechaSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		SimpleDateFormat formatFechaDB=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date fechaDB = formatFechaDB.parse(fechaSistema);
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(fechaDB);
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_ErroresContador = 1L;
		out.p_out_NombreArchivo="FMM/ArchivoAGF_"+fecha+".csv";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="FMM/ArchivoAGF_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		//se crea carpeta 
		File folder = new File(entrada.p_Ruta+"/FMM");
		if(!folder.exists()){
			folder.mkdirs();
		}
		File archivo = new File(out.p_out_RutaArchivo);
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivo;
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			List<ParametrosGeneracion> parametrosGeneracion = sql.query(con,SQL_SELECT_PARAMETROS,params,ParametrosGeneracion.class);
			ParametrosGeneracion parametroGeneracion = parametrosGeneracion.get(0);
			
			params.clear();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("FechaDesde", entrada.p_FechaDesde);
			params.put("FechaHasta", entrada.p_FechaHasta);
			params.put("Ambiente", entrada.p_IdAmbiente);
			
			List<Operaciones> operaciones = new ArrayList<Operaciones>();
			
			operaciones.addAll(sql.query(con,SQL_SELECT_FACTURAS_COMPRAS,params,Operaciones.class));
			
			operaciones.addAll(sql.query(con,SQL_SELECT_FACTURAS_VENTAS,params,Operaciones.class));
			
			operaciones.addAll(sql.query(con,SQL_SELECT_COMPRA_TERMINO_FR,params,Operaciones.class));
			
			operaciones.addAll(sql.query(con,SQL_SELECT_VENTA_TERMINO_FR,params,Operaciones.class));
			
			operaciones.addAll(sql.query(con,SQL_SELECT_COMPRA_PACTOS,params,Operaciones.class));
			
			operaciones.addAll(sql.query(con,SQL_SELECT_VENTA_PACTOS,params,Operaciones.class));
			
			operaciones.addAll(sql.query(con,SQL_SELECT_COMPRA_SIMULTANEA,params,Operaciones.class));
			
			operaciones.addAll(sql.query(con,SQL_SELECT_VENTA_SIMULTANEA,params,Operaciones.class));
			
			if("S".equalsIgnoreCase(parametroGeneracion.NotasCredito)){
				operaciones.addAll(sql.query(con,SQL_SELECT_NC_COMPRA_SIMULTANEA,params,Operaciones.class));
				operaciones.addAll(sql.query(con,SQL_SELECT_NC_VENTA_SIMULTANEA,params,Operaciones.class));
			}
			
			for(Operaciones operacion : operaciones){
				try{
					numeroRegistros++;
					numeroRegistrosCorrecto++;
					bwArchivo.write(operacion.Registro);
					bwArchivo.newLine();
				}catch(Exception e){
					numeroRegistrosErroneos++;
					bwArchivoLog.write("Registro con error," + e.getMessage());
				}
				
			}
			
		}catch(Exception e){
			numeroRegistrosErroneos++;
			bwArchivoLog.write("Error al procesar archivo," + e.getMessage());
		}
		
		try{
			long endTime = System.currentTimeMillis();
			String fechaFin = fm.format(new Date());
			bwArchivoLog.write("Fecha Hora Inicio:" + fecha);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Fecha Hora Termino:" + fechaFin);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Tiempo de ejecución:" + (endTime - startTime));
			bwArchivoLog.newLine();
			bwArchivoLog.write("Total Registros:" + numeroRegistros);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Registros Correctos:" + numeroRegistrosCorrecto);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Registros Erroneos:" + numeroRegistrosErroneos);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Ruta Archivo Salida:" + out.p_out_RutaArchivo);
			out.p_out_ErroresContador = 0L;
		}catch(Exception e){
			log.write(e);
		}finally{
			 log.writeAsJson("SALIDA",out);
			 if(bwArchivo !=null){
				 bwArchivo.close();
			 }
			 if(bwArchivoLog != null){
				 bwArchivoLog.close();
			 }
		}
		
		return out;
	}
	
}
