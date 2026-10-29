package cl.bcs.cuentaInversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarArchivoCargoAbono implements JavaCall{
	
	private final Log log = new Log("CTA.GenerarArchivoCargoAbono");


	private final String SQL_SELECT_CARGOS_ABONOS         = loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarArchivoCargoAbono_SelectCargosAbonos.sql");  
	private final String SQL_SELECT_CONFIGURACION         = loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarArchivoCargoAbono_SelectConfiguracion.sql");  
	
	
	// ----------------------------------------------------
	// CLASES DE ENTRADA SALIDA
	// ----------------------------------------------------
	public static class GenerarArchivoCargoAbono_Entrada
	{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;	
    	public String	p_FechaDesde;
    	public String	p_FechaHasta;		
		public Long		p_IdEjecutivo;
    	public String	p_IdMoneda;
    	public String	p_IdTipoMovimiento;
    	public String	p_IdConcepto;
    	public String	p_IdEstado;

    			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	public static class GenerarArchivoCargoAbono_Salida {

		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String						P_OUT_RUTAARCHIVO;
		
		
		public Long							P_OUT_CANTIDADREGISTROS;
		
		
		public GenerarArchivoCargoAbono_Salida()
		{
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			
			this.P_OUT_CANTIDADREGISTROS     = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	// ----------------------------------------------------
	// CLASES DE PROCESO
	// ----------------------------------------------------
	public static class CargoAbono 
	{		
		public String			Linea;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}		
	

	public static class Configuracion 
	{
		public String			RutaBase;
		public String			RutaCta;
		public String			RutaArchivo;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// ----------------------------------------------------
	// CALL
	// ----------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		
		GenerarArchivoCargoAbono_Entrada in = gson.fromJson(jin.toString(), GenerarArchivoCargoAbono_Entrada.class);
		GenerarArchivoCargoAbono_Salida out = logica(con,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	
	
	// ----------------------------------------------------
	// METODOS COMPLEMENTARIOS
	// ----------------------------------------------------
	// Genera el nombre de un arhivo , con el formato:	<nombreArhivoSalida>_<fechaHora>.<extension>	
	private String generarNombreArchivo(String nombre, String extension)
	{
		String nombreArchivo = "";
		
		try 
		{
			Date fechaActual = new Date();			
			DateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String fechaHora = formatoFecha.format(fechaActual);
			nombreArchivo =  nombre + "_"+ fechaHora +"."+ extension;			
		}
		catch(Exception ex)
		{
			nombreArchivo = "";
		}
		
		return nombreArchivo;	
	}
	
	
	// ----------------------------------------------------
	// LOGICA DEL PROGRAMA
	// ----------------------------------------------------
	public  GenerarArchivoCargoAbono_Salida logica(Connection con, GenerarArchivoCargoAbono_Entrada in) throws Exception {
		GenerarArchivoCargoAbono_Salida out = new GenerarArchivoCargoAbono_Salida();
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		HashMap<String,String> codigo = new HashMap<String, String>();
		String rutaArchivo 		= "";
		String nombreArchivo 	= "";
		String rutaCta          = "";
		String rutaCompleta 	= "";  // ruta + nombre archivo
		
		log.write("ENTRADA > " + in);
		
		//------------------------------------------------------------------------
		// OBTIENE CONFIGURACION (nombre y ruta para guardar archivo)
		List<Configuracion> lstConfiguracion = new ArrayList<Configuracion>();
		params.clear();
		params.put("IdEmpresa", in.p_IdEmpresa);		
		lstConfiguracion = sql.query(con, SQL_SELECT_CONFIGURACION, params, Configuracion.class);
		
		if (!lstConfiguracion.isEmpty())
		{
			for (Configuracion conf : lstConfiguracion)
			{
				rutaArchivo = conf.RutaArchivo != null ? conf.RutaArchivo : "";
				rutaCta     = conf.RutaCta != null ? conf.RutaCta : "";		
			}		
		}
		
		if (rutaArchivo.equalsIgnoreCase("")|| rutaCta.equalsIgnoreCase(""))
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_CONFIGURACION_RUTA_ARCHIVO",null,con,in.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
			return out;
		}

		nombreArchivo = generarNombreArchivo("MovimientosCargoAbono", "csv");
		rutaCompleta  = rutaArchivo + nombreArchivo; // Ruta completa donde generar el archivo
		rutaCta       = rutaCta + nombreArchivo;     // Ruta Cta para devolver y desde donde leer el archivo
		
		log.write("NOMBRE ARCHIVO > " + nombreArchivo);
		log.write("RUTA COMPLETA > " + rutaCompleta);
		
		
		//------------------------------------------------------------------------
		// OBTIENE DATOS DE MOVIMIENTOS
		List<CargoAbono> lstCargosAbonos = new ArrayList<CargoAbono>();
		params.clear();	
		params.put("FechaDesde", in.p_FechaDesde);
		params.put("FechaHasta", in.p_FechaHasta);
		params.put("IdEjecutivo", in.p_IdEjecutivo);
		params.put("IdMoneda", 	in.p_IdMoneda);
		params.put("IdTipoMovimiento", in.p_IdTipoMovimiento);
		params.put("IdConcepto", in.p_IdConcepto);
		params.put("IdEstado", in.p_IdEstado);
    	
		lstCargosAbonos = sql.query(con, SQL_SELECT_CARGOS_ABONOS, params, CargoAbono.class);

		if (!lstCargosAbonos.isEmpty())
		{		
			log.write("CANTIDAD REGISTROS > " + lstCargosAbonos.size());
			
			//File archivo = new File("C:\\__ARCHIVO_PRUEBA\\" + nombreArchivo); // >> PARA PRUEBA LOCALES
			File archivo = new File(rutaCompleta);       //  >> PARA EN SERVIDOR (cuando se libera debe ser con esta ruta)

			
			
			BufferedWriter bwArchivo;
			bwArchivo = new BufferedWriter(new FileWriter(archivo));	
			
			//Linea para encabezado del archivo csv
			String linea = "";
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_NOMBRE_CLIENTE", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DOCUMENTO_IDENTIDAD", codigo) +";");				
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_PORTAFOLIO", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_FECHA_MOVIMIENTO", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_FECHA_EFECTIVA", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_CONCEPTO", codigo) +";");				
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_ES_TRANSFERENCIA", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_MONEDA", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_CARGO", codigo) +";");				
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_ABONO", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_TIPO_COMPROBANTE", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_SECUENCIA_COMPROBANTE", codigo) +";");				
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_FOLIO_COMPROBANTE", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_FECHA_COMPROBANTE", codigo) +";");			
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_TIPO_PRODUCTO", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_TIPO_OPERACION", codigo) +";");	
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_FECHA_OPERACION", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_FECHA_VENCIMIENTO", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_SECUENCIA_OPERACION", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_MONTO_OPERACION", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_SECUENCIA_MOVIMIENTO", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_SECUENCIA_TRANSFERENCIA", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_NOMBRE_CLIENTE_RELACIONADO", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_PORTAFOLIO_RELACIONADO", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_EMPLEADO_CREADOR", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_FECHA_CERTIFICACION", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_EMPLEADO_CERTIFICADOR", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DESCRIPCION", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_ESTADO", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_MOTIVO_ANULACION", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_AUTORIZACION", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_ESTADO_AUTORIZACION", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_PRODUCTO", codigo) +";");
			linea = linea.concat(Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_RELACION", codigo) +";");
			bwArchivo.write(linea);
			bwArchivo.newLine();
		
			
			for(CargoAbono carAbo : lstCargosAbonos)
			{
				//Linea con data de movimiento para archivo csv
				//log.write("LINEA > " + carAbo.Linea);
				bwArchivo.write(carAbo.Linea);
				bwArchivo.newLine();
			}
			
			bwArchivo.flush();
			bwArchivo.close();
        	
			out.P_OUT_RUTAARCHIVO = rutaCta; // CTA/MovimientosCargoAbono_20200421_135102.csv
			
		}

		
		
		
		out.P_OUT_CANTIDADREGISTROS =  Long.valueOf(lstCargosAbonos.size());
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		log.write("SALIDA > " + out);
		return out;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) 
		{
			Gson gson = Utils.getGsonBuilder();
	    	
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"TES\",\"p_FechaDesde\":\"2020-03-19\",\"p_FechaHasta\":\"2020-03-19\",\"p_IdEjecutivo\":0,\"p_IdMoneda\":\"0\",\"p_IdTipoMovimiento\":\"0\",\"p_IdConcepto\":\"0\";\"p_IdEstado\":\"0\"}";
			
			GenerarArchivoCargoAbono_Entrada Objin = gson.fromJson(json, GenerarArchivoCargoAbono_Entrada.class);
			GenerarArchivoCargoAbono demo = new GenerarArchivoCargoAbono();			
 			System.out.println("ENTRADA:" + Objin.toString());
 			
 			try 
 			{	
 				GenerarArchivoCargoAbono_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				//con.rollback();
 				con.commit();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();	
				//con.commit();
			}
 			
 			con.close();
	    }
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
}
