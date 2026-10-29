package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraIntermediada.GenerarInterfazC40CBI_Entrada;
import cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraIntermediada.GenerarInterfazC40CBI_Salida;
import cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraPacteada.GenerarInterfazC40CarteraPacteada_Entrada;
import cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraPacteada.GenerarInterfazC40CarteraPacteada_Salida;
import cl.bcs.normativa.interfaces.GenerarInterfazC40INT.GenerarInterfazC40INTEntrada;
import cl.bcs.normativa.interfaces.GenerarInterfazC40INT.GenerarInterfazC40INTSalida;
import cl.bcs.normativa.interfaces.GenerarInterfazC40INTDET.EntradaGenerarInterfazC40INTDET;
import cl.bcs.normativa.interfaces.GenerarInterfazC40INTDET.SalidaGenerarInterfazC40INTDET;
import cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraNormal.EntradaGenerarInterfazC40CarteraNormal;
import cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraNormal.SalidaGenerarInterfazC40CarteraNormal;
import cl.bcs.normativa.interfaces.GenerarInterfazC40InformacionContable.InGenerarInterfazC40;
import cl.bcs.normativa.interfaces.GenerarInterfazC40InformacionContable.OutGenerarInterfazC40;
import cl.bcs.normativa.interfaces.GenerarInterfazC08CarteraIntermediada.GenerarInterfazC08CBI_Entrada;
import cl.bcs.normativa.interfaces.GenerarInterfazC08CarteraIntermediada.GenerarInterfazC08CBI_Salida;
import cl.bcs.normativa.interfaces.GenerarInterfazC08CarteraNormal.InGenerarInterfazC08;
import cl.bcs.normativa.interfaces.GenerarInterfazC08CarteraNormal.OutGenerarInterfazC08;
import cl.bcs.normativa.interfaces.GenerarInterfazC08CarteraPacteada.GenerarInterfazC08CarteraPacteada_Entrada;
import cl.bcs.normativa.interfaces.GenerarInterfazC08CarteraPacteada.GenerarInterfazC08CarteraPacteada_Salida;
import cl.bcs.normativa.interfaces.GenerarInterfazC08InformacionContable.GenerarInterfazC08_Entrada;
import cl.bcs.normativa.interfaces.GenerarInterfazC08InformacionContable.GenerarInterfazC08_Salida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class GenerarInterfazICU implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazICU");
		
	public static class EntradaGenerarInterfazICU{
		public Long    p_IdEmpresa;
		public Long    p_IdUsuario;
		public String  p_IdModulo;
		public String  p_Fecha;
		public String  p_Ruta;
		public String  p_Llamado;
		public Boolean p_EsTodos;
		public List<String> p_ArchivoSecundario;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaGenerarInterfazICU{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
		public List<String> p_out_Archivos;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		EntradaGenerarInterfazICU in=gson.fromJson(jin.toString() , EntradaGenerarInterfazICU.class);
		SalidaGenerarInterfazICU out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public  SalidaGenerarInterfazICU logica(Connection con, EntradaGenerarInterfazICU entrada) throws Exception {
		SalidaGenerarInterfazICU out = new SalidaGenerarInterfazICU();
		out.p_out_Archivos = new ArrayList<>();
		String[] codigoInterfaz = entrada.p_Llamado.split("-");

		SimpleDateFormat formatoFechaArchivo = new SimpleDateFormat("yyMMdd");
		SimpleDateFormat formatoFechaTitulo = new SimpleDateFormat("dd/MM/yy");
		Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);
		String fechaddmmyy = formatoFechaTitulo.format(fecha);
		String fechayymmdd = formatoFechaArchivo.format(fecha);

		GenerarInterfazC40CarteraIntermediada c40CarteraIntermediada = new GenerarInterfazC40CarteraIntermediada();
		GenerarInterfazC40CBI_Entrada c40EntradaCarteraIntermediada = new GenerarInterfazC40CBI_Entrada();
		GenerarInterfazC40CBI_Salida c40SalidaCarteraIntermediada = new GenerarInterfazC40CBI_Salida();
		
		GenerarInterfazC40CarteraPacteada c40CarteraPacteada = new GenerarInterfazC40CarteraPacteada();
		GenerarInterfazC40CarteraPacteada_Entrada c40EntradaCarteraPacteada = new GenerarInterfazC40CarteraPacteada_Entrada();
		GenerarInterfazC40CarteraPacteada_Salida c40SalidaCarteraPacteada = new GenerarInterfazC40CarteraPacteada_Salida();

		GenerarInterfazC40CarteraNormal c40CarteraNormal = new GenerarInterfazC40CarteraNormal();
		EntradaGenerarInterfazC40CarteraNormal c40EntradaCarteraNormal = new EntradaGenerarInterfazC40CarteraNormal();
		SalidaGenerarInterfazC40CarteraNormal c40SalidaCarteraNormal = new SalidaGenerarInterfazC40CarteraNormal();
		
		GenerarInterfazC40InformacionContable c40InformacionContable = new GenerarInterfazC40InformacionContable();
		InGenerarInterfazC40 c40EntradaInformacionContable = new InGenerarInterfazC40();
		OutGenerarInterfazC40 c40SalidaInformacionContable = new OutGenerarInterfazC40();
		
		GenerarInterfazC40INT c40INT = new GenerarInterfazC40INT();
		GenerarInterfazC40INTEntrada c40EntradaINT = new GenerarInterfazC40INTEntrada();
		GenerarInterfazC40INTSalida c40SalidaINT = new GenerarInterfazC40INTSalida();
				
		GenerarInterfazC40INTDET c40DET = new GenerarInterfazC40INTDET();
		EntradaGenerarInterfazC40INTDET c40EntradaDET = new EntradaGenerarInterfazC40INTDET();
		SalidaGenerarInterfazC40INTDET c40SalidaDET = new SalidaGenerarInterfazC40INTDET();
		
		GenerarInterfazC08CarteraPacteada c08CarteraPacteada = new GenerarInterfazC08CarteraPacteada();
		GenerarInterfazC08CarteraPacteada_Entrada c08EntradaCarteraPacteada = new GenerarInterfazC08CarteraPacteada_Entrada();
		GenerarInterfazC08CarteraPacteada_Salida c08SalidaCarteraPacteada = new GenerarInterfazC08CarteraPacteada_Salida();
		
		GenerarInterfazC08CarteraIntermediada c08CarteraIntermediada = new GenerarInterfazC08CarteraIntermediada();
		GenerarInterfazC08CBI_Entrada c08EntradaCarteraIntermediada = new GenerarInterfazC08CBI_Entrada();
		GenerarInterfazC08CBI_Salida c08SalidaCarteraIntermediada = new GenerarInterfazC08CBI_Salida();
		
		GenerarInterfazC08CarteraNormal c08CarteraNormal = new GenerarInterfazC08CarteraNormal();
		InGenerarInterfazC08 c08EntradaCarteraNormal = new InGenerarInterfazC08();
		OutGenerarInterfazC08 c08SalidaCarteraNormal = new OutGenerarInterfazC08();
		
		GenerarInterfazC08InformacionContable c08InformacionContable = new GenerarInterfazC08InformacionContable();
		GenerarInterfazC08_Entrada c08EntradaInformacionContable = new GenerarInterfazC08_Entrada();
		GenerarInterfazC08_Salida c08SalidaInformacionContable = new GenerarInterfazC08_Salida();
		
		List<String> carteraIntermediada = new ArrayList<>();
		List<String> carteraPacteada = new ArrayList<>();
		List<String> carteraNormal = new ArrayList<>();
		
		if(codigoInterfaz[1].trim().equals("TODOS")) {
			out.p_out_NombreArchivoLog = codigoInterfaz[0].trim() + "ICU" + fechayymmdd + ".txt";
		}else {
			out.p_out_NombreArchivoLog = codigoInterfaz[0].trim() + "ICU" + fechayymmdd + ".log";
		}
		
		
		out.p_out_RutaArchivoLog = entrada.p_Ruta + "/" + out.p_out_NombreArchivoLog;

		File archivoSecundario = new File(out.p_out_RutaArchivoLog);
		try (BufferedWriter bwArchivoSecundario = new BufferedWriter(new FileWriter(archivoSecundario))) {
			
			if (codigoInterfaz[0].trim().equals("C40")) {// Llamado para interfaz C40
				if(codigoInterfaz[1].trim().equals("TODOS")) {
					// Busca lineas de Cartera Pacteada
					c40EntradaCarteraPacteada.p_Fecha = entrada.p_Fecha;
					c40EntradaCarteraPacteada.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaCarteraPacteada.p_IdModulo = entrada.p_IdModulo;
					c40EntradaCarteraPacteada.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaCarteraPacteada.p_Ruta = entrada.p_Ruta;
					c40EntradaCarteraPacteada.p_EsTodos = true;
					c40SalidaCarteraPacteada = c40CarteraPacteada.logica(con, c40EntradaCarteraPacteada);
					carteraPacteada = c40SalidaCarteraPacteada.P_ARCHIVO_SECUNDARIO;
					out.p_out_Archivos.add(c40SalidaCarteraPacteada.P_OUT_RUTA_ARCHIVO);
					out.p_out_Archivos.add(out.p_out_RutaArchivoLog);
					
					// Busca lineas de cartera intermediada
					c40EntradaCarteraIntermediada.p_Fecha = entrada.p_Fecha;
					c40EntradaCarteraIntermediada.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaCarteraIntermediada.p_IdModulo = entrada.p_IdModulo;
					c40EntradaCarteraIntermediada.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaCarteraIntermediada.p_Ruta = entrada.p_Ruta;
					c40EntradaCarteraIntermediada.p_EsTodos = true;
					c40SalidaCarteraIntermediada = c40CarteraIntermediada.logica(con, c40EntradaCarteraIntermediada);
					carteraIntermediada = c40SalidaCarteraIntermediada.p_out_ArchivoSecundario;
					out.p_out_Archivos.add(c40SalidaCarteraIntermediada.p_out_RutaArchivo);
					
					// Busca lineas Cartera Normal
					c40EntradaCarteraNormal.p_Fecha = entrada.p_Fecha;
					c40EntradaCarteraNormal.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaCarteraNormal.p_IdModulo = entrada.p_IdModulo;
					c40EntradaCarteraNormal.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaCarteraNormal.p_Ruta = entrada.p_Ruta;
					c40EntradaCarteraNormal.p_EsTodos = true;
					c40SalidaCarteraNormal = c40CarteraNormal.logica(con, c40EntradaCarteraNormal);
					carteraNormal = c40SalidaCarteraNormal.p_out_ArchivoSecundario;
					out.p_out_Archivos.add(c40SalidaCarteraNormal.p_out_RutaArchivo);
					
					//Genera archivo Informacion Contable
					c40EntradaInformacionContable.p_Fecha = entrada.p_Fecha;
					c40EntradaInformacionContable.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaInformacionContable.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaInformacionContable.p_Ruta = entrada.p_Ruta;
					c40SalidaInformacionContable = c40InformacionContable.logica(con, c40EntradaInformacionContable);
					out.p_out_Archivos.add(c40SalidaInformacionContable.p_out_RutaArchivo);
					
					//Genera archivo INT
					c40EntradaINT.p_Fecha = entrada.p_Fecha;
					c40EntradaINT.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaINT.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaINT.p_Ruta = entrada.p_Ruta;
					c40EntradaINT.p_IdModulo = entrada.p_IdModulo;
					c40SalidaINT = c40INT.logica(con, c40EntradaINT);
					out.p_out_Archivos.add(c40SalidaINT.P_OUT_RUTA_ARCHIVO);
					
					//Genera archivo INT DET 		
					c40EntradaDET.p_Fecha = entrada.p_Fecha;
					c40EntradaDET.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaDET.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaDET.p_Ruta = entrada.p_Ruta;					
					c40SalidaDET = c40DET.logica(con, c40EntradaDET);
					out.p_out_Archivos.add(c40SalidaDET.P_OUT_RUTA_ARCHIVO);
					
					//Agrega ruta de archivo ICU a la lista de salida
					out.p_out_RutaArchivoLog=c40SalidaCarteraNormal.p_out_RutaArchivoLog;
					
					String nombreArchivoLog = "C40LOG" + entrada.p_Fecha.replace("-", "") + ".log";
					String rutaArchivoLog= entrada.p_Ruta+"/" + nombreArchivoLog;
					File archivoLog = new File(rutaArchivoLog);
					BufferedWriter bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
					bwArchivoLog.write( "Archivos Generados Correctamente");
					bwArchivoLog.close();
					out.p_out_NombreArchivoLog=nombreArchivoLog;

				}
				if (codigoInterfaz[1].trim().equals("CBI")) {// Lamado desde Cartera Intermediada
					// Busca lineas de Cartera Pacteada
					c40EntradaCarteraPacteada.p_Fecha = entrada.p_Fecha;
					c40EntradaCarteraPacteada.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaCarteraPacteada.p_IdModulo = entrada.p_IdModulo;
					c40EntradaCarteraPacteada.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaCarteraPacteada.p_Ruta = entrada.p_Ruta;
					c40EntradaCarteraPacteada.interfaz = false;
					c40SalidaCarteraPacteada = c40CarteraPacteada.logica(con, c40EntradaCarteraPacteada);
					carteraPacteada = c40SalidaCarteraPacteada.P_ARCHIVO_SECUNDARIO;
					
					// Lineas Cartera Intermediada
					carteraIntermediada = entrada.p_ArchivoSecundario;
					
					// Busca lineas Cartera Normal
					c40EntradaCarteraNormal.p_Fecha = entrada.p_Fecha;
					c40EntradaCarteraNormal.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaCarteraNormal.p_IdModulo = entrada.p_IdModulo;
					c40EntradaCarteraNormal.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaCarteraNormal.p_Ruta = entrada.p_Ruta;
					c40EntradaCarteraNormal.p_Interfaz = false;
					c40SalidaCarteraNormal = c40CarteraNormal.logica(con, c40EntradaCarteraNormal);
					carteraNormal = c40SalidaCarteraNormal.p_out_ArchivoSecundario;
				}
				if (codigoInterfaz[1].trim().equals("CBP")) {// Lamado desde Cartera Pacteada
					// Lineas Cartera Pacteda
					carteraPacteada = entrada.p_ArchivoSecundario;

					// Busca lineas de cartera intermediada
					c40EntradaCarteraIntermediada.p_Fecha = entrada.p_Fecha;
					c40EntradaCarteraIntermediada.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaCarteraIntermediada.p_IdModulo = entrada.p_IdModulo;
					c40EntradaCarteraIntermediada.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaCarteraIntermediada.p_Ruta = entrada.p_Ruta;
					c40EntradaCarteraIntermediada.p_Interfaz = false;
					c40SalidaCarteraIntermediada = c40CarteraIntermediada.logica(con, c40EntradaCarteraIntermediada);
					carteraIntermediada = c40SalidaCarteraIntermediada.p_out_ArchivoSecundario;
					
					// Busca lineas Cartera Normal
					c40EntradaCarteraNormal.p_Fecha = entrada.p_Fecha;
					c40EntradaCarteraNormal.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaCarteraNormal.p_IdModulo = entrada.p_IdModulo;
					c40EntradaCarteraNormal.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaCarteraNormal.p_Ruta = entrada.p_Ruta;
					c40EntradaCarteraNormal.p_Interfaz = false;
					c40SalidaCarteraNormal = c40CarteraNormal.logica(con, c40EntradaCarteraNormal);
					carteraNormal = c40SalidaCarteraNormal.p_out_ArchivoSecundario;
				}
				if (codigoInterfaz[1].trim().equals("CBN")) {// Lamado desde Cartera Normal
					// Busca lineas de Cartera Pacteada
					c40EntradaCarteraPacteada.p_Fecha = entrada.p_Fecha;
					c40EntradaCarteraPacteada.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaCarteraPacteada.p_IdModulo = entrada.p_IdModulo;
					c40EntradaCarteraPacteada.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaCarteraPacteada.p_Ruta = entrada.p_Ruta;
					c40EntradaCarteraPacteada.interfaz = false;
					c40SalidaCarteraPacteada = c40CarteraPacteada.logica(con, c40EntradaCarteraPacteada);
					carteraPacteada = c40SalidaCarteraPacteada.P_ARCHIVO_SECUNDARIO;

					// Busca lineas de cartera intermediada
					c40EntradaCarteraIntermediada.p_Fecha = entrada.p_Fecha;
					c40EntradaCarteraIntermediada.p_IdEmpresa = entrada.p_IdEmpresa;
					c40EntradaCarteraIntermediada.p_IdModulo = entrada.p_IdModulo;
					c40EntradaCarteraIntermediada.p_IdUsuario = entrada.p_IdUsuario;
					c40EntradaCarteraIntermediada.p_Ruta = entrada.p_Ruta;
					c40EntradaCarteraIntermediada.p_Interfaz = false;
					c40SalidaCarteraIntermediada = c40CarteraIntermediada.logica(con, c40EntradaCarteraIntermediada);
					carteraIntermediada = c40SalidaCarteraIntermediada.p_out_ArchivoSecundario;
					
					// Lineas Cartera Normal
					carteraNormal = entrada.p_ArchivoSecundario;
				}
			}
			if (codigoInterfaz[0].trim().equals("C08")) {// Llamado para interfaz C08
				if(codigoInterfaz[1].trim().equals("TODOS")) {
					// Busca lineas de Cartera Pacteada
					c08EntradaCarteraPacteada.p_Fecha = entrada.p_Fecha;
					c08EntradaCarteraPacteada.p_IdEmpresa = entrada.p_IdEmpresa;
					c08EntradaCarteraPacteada.p_IdModulo = entrada.p_IdModulo;
					c08EntradaCarteraPacteada.p_IdUsuario = entrada.p_IdUsuario;
					c08EntradaCarteraPacteada.p_Ruta = entrada.p_Ruta;
					c08EntradaCarteraPacteada.p_EsTodos = true;
					c08SalidaCarteraPacteada = c08CarteraPacteada.logica(con, c08EntradaCarteraPacteada);
					carteraPacteada = c08SalidaCarteraPacteada.P_ARCHIVO_SECUNDARIO;
					out.p_out_Archivos.add(c08SalidaCarteraPacteada.P_OUT_RUTA_ARCHIVO);
					out.p_out_Archivos.add(out.p_out_RutaArchivoLog);
					
					// Busca lineas de cartera intermediada
					c08EntradaCarteraIntermediada.p_Fecha = entrada.p_Fecha;
					c08EntradaCarteraIntermediada.p_IdEmpresa = entrada.p_IdEmpresa;
					c08EntradaCarteraIntermediada.p_IdModulo = entrada.p_IdModulo;
					c08EntradaCarteraIntermediada.p_IdUsuario = entrada.p_IdUsuario;
					c08EntradaCarteraIntermediada.p_Ruta = entrada.p_Ruta;
					c08EntradaCarteraIntermediada.p_EsTodos = true;
					c08SalidaCarteraIntermediada = c08CarteraIntermediada.logica(con, c08EntradaCarteraIntermediada);
					carteraIntermediada = c08SalidaCarteraIntermediada.p_out_ArchivoSecundario;
					out.p_out_Archivos.add(c08SalidaCarteraIntermediada.p_out_RutaArchivo);
					
					// Busca lineas Cartera Normal
					c08EntradaCarteraNormal.p_Fecha = entrada.p_Fecha;
					c08EntradaCarteraNormal.p_IdEmpresa = entrada.p_IdEmpresa;
					c08EntradaCarteraNormal.p_IdModulo = entrada.p_IdModulo;
					c08EntradaCarteraNormal.p_IdUsuario = entrada.p_IdUsuario;
					c08EntradaCarteraNormal.p_Ruta = entrada.p_Ruta;
					c08EntradaCarteraNormal.p_EsTodo = true;
					c08SalidaCarteraNormal = c08CarteraNormal.logica(con, c08EntradaCarteraNormal);
					carteraNormal = c08SalidaCarteraNormal.p_out_ArchivoSecundario;
					out.p_out_Archivos.add(c08SalidaCarteraNormal.p_out_RutaArchivo);
					
					//Genera archivo Informacion Contable
					c08EntradaInformacionContable.p_Fecha = entrada.p_Fecha;
					c08EntradaInformacionContable.p_IdEmpresa = entrada.p_IdEmpresa;
					c08EntradaInformacionContable.p_IdUsuario = entrada.p_IdUsuario;
					c08EntradaInformacionContable.p_Ruta = entrada.p_Ruta;
					c08SalidaInformacionContable = c08InformacionContable.logica(con, c08EntradaInformacionContable);
					out.p_out_Archivos.add(c08SalidaInformacionContable.P_OUT_RUTA_ARCHIVO);
					
					//Agrega ruta de archivo ICU a la lista de salida
					//out.p_out_Archivos.add(c40SalidaCarteraNormal.p_out_RutaArchivoLog);
					
					
					String nombreArchivoLog = "C08LOG" + entrada.p_Fecha.replace("-", "") + ".log";
					String rutaArchivoLog= entrada.p_Ruta+"/" + nombreArchivoLog;
					File archivoLog = new File(rutaArchivoLog);
					BufferedWriter bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
					bwArchivoLog.write( "Archivos Generados Correctamente");
					bwArchivoLog.close();
					out.p_out_NombreArchivoLog=nombreArchivoLog;
					out.p_out_RutaArchivoLog=rutaArchivoLog;
				}
				if (codigoInterfaz[1].trim().equals("CBI")) {// Lamado desde Cartera Intermediada
					// Busca lineas de Cartera Pacteada
					c08EntradaCarteraPacteada.p_Fecha = entrada.p_Fecha;
					c08EntradaCarteraPacteada.p_IdEmpresa = entrada.p_IdEmpresa;
					c08EntradaCarteraPacteada.p_IdModulo = entrada.p_IdModulo;
					c08EntradaCarteraPacteada.p_IdUsuario = entrada.p_IdUsuario;
					c08EntradaCarteraPacteada.p_Ruta = entrada.p_Ruta;
					c08EntradaCarteraPacteada.interfaz = false;
					c08SalidaCarteraPacteada = c08CarteraPacteada.logica(con, c08EntradaCarteraPacteada);
					carteraPacteada = c08SalidaCarteraPacteada.P_ARCHIVO_SECUNDARIO;
					
					// Lineas Cartera Intermediada
					carteraIntermediada = entrada.p_ArchivoSecundario;

					// Busca lineas Cartera Normal
					c08EntradaCarteraNormal.p_Fecha = entrada.p_Fecha;
					c08EntradaCarteraNormal.p_IdEmpresa = entrada.p_IdEmpresa;
					c08EntradaCarteraNormal.p_IdModulo = entrada.p_IdModulo;
					c08EntradaCarteraNormal.p_IdUsuario = entrada.p_IdUsuario;
					c08EntradaCarteraNormal.p_Ruta = entrada.p_Ruta;
					c08EntradaCarteraNormal.p_Interfaz = false;
					c08SalidaCarteraNormal = c08CarteraNormal.logica(con, c08EntradaCarteraNormal);
					carteraNormal = c08SalidaCarteraNormal.p_out_ArchivoSecundario;
				}
				if (codigoInterfaz[1].trim().equals("CBP")) {// Lamado desde Cartera Pacteada
					// Lineas Cartera Pacteda
					carteraPacteada = entrada.p_ArchivoSecundario;

					// Busca lineas de cartera intermediada
					c08EntradaCarteraIntermediada.p_Fecha = entrada.p_Fecha;
					c08EntradaCarteraIntermediada.p_IdEmpresa = entrada.p_IdEmpresa;
					c08EntradaCarteraIntermediada.p_IdModulo = entrada.p_IdModulo;
					c08EntradaCarteraIntermediada.p_IdUsuario = entrada.p_IdUsuario;
					c08EntradaCarteraIntermediada.p_Ruta = entrada.p_Ruta;
					c08EntradaCarteraIntermediada.p_Generar_Interfaz = false;
					c08SalidaCarteraIntermediada = c08CarteraIntermediada.logica(con, c08EntradaCarteraIntermediada);
					carteraIntermediada = c08SalidaCarteraIntermediada.p_out_ArchivoSecundario;

					// Busca lineas Cartera Normal
					c08EntradaCarteraNormal.p_Fecha = entrada.p_Fecha;
					c08EntradaCarteraNormal.p_IdEmpresa = entrada.p_IdEmpresa;
					c08EntradaCarteraNormal.p_IdModulo = entrada.p_IdModulo;
					c08EntradaCarteraNormal.p_IdUsuario = entrada.p_IdUsuario;
					c08EntradaCarteraNormal.p_Ruta = entrada.p_Ruta;
					c08EntradaCarteraNormal.p_Interfaz = false;
					c08SalidaCarteraNormal = c08CarteraNormal.logica(con, c08EntradaCarteraNormal);
					carteraNormal = c08SalidaCarteraNormal.p_out_ArchivoSecundario;
				}
				if (codigoInterfaz[1].trim().equals("CBN")) {// Lamado desde Cartera Normal
					// Busca lineas de Cartera Pacteada
					c08EntradaCarteraPacteada.p_Fecha = entrada.p_Fecha;
					c08EntradaCarteraPacteada.p_IdEmpresa = entrada.p_IdEmpresa;
					c08EntradaCarteraPacteada.p_IdModulo = entrada.p_IdModulo;
					c08EntradaCarteraPacteada.p_IdUsuario = entrada.p_IdUsuario;
					c08EntradaCarteraPacteada.p_Ruta = entrada.p_Ruta;
					c08EntradaCarteraPacteada.interfaz = false;
					c08SalidaCarteraPacteada = c08CarteraPacteada.logica(con, c08EntradaCarteraPacteada);
					carteraPacteada = c08SalidaCarteraPacteada.P_ARCHIVO_SECUNDARIO;
					
					// Busca lineas de cartera intermediada
					c08EntradaCarteraIntermediada.p_Fecha = entrada.p_Fecha;
					c08EntradaCarteraIntermediada.p_IdEmpresa = entrada.p_IdEmpresa;
					c08EntradaCarteraIntermediada.p_IdModulo = entrada.p_IdModulo;
					c08EntradaCarteraIntermediada.p_IdUsuario = entrada.p_IdUsuario;
					c08EntradaCarteraIntermediada.p_Ruta = entrada.p_Ruta;
					c08EntradaCarteraIntermediada.p_Generar_Interfaz = false;
					c08SalidaCarteraIntermediada = c08CarteraIntermediada.logica(con, c08EntradaCarteraIntermediada);
					carteraIntermediada = c08SalidaCarteraIntermediada.p_out_ArchivoSecundario;
					
					// Lineas Cartera Normal
					carteraNormal = entrada.p_ArchivoSecundario;
				}
			}
			// Cabecera
			bwArchivoSecundario.write("                            TOTALES INTERFAZ " + codigoInterfaz[0] + " CORREDORA                                                                 ");
			bwArchivoSecundario.write("\r\n");
			bwArchivoSecundario.write("                             Fecha de Proceso (" + fechaddmmyy + ")                                                                  ");
			bwArchivoSecundario.write("\r\n");
			bwArchivoSecundario.write("\r\n");
			bwArchivoSecundario.write("Concepto                                  Total Cartera   Cuenta Contable             Saldo Cuenta               Diferencia");
			bwArchivoSecundario.write("\r\n");
			bwArchivoSecundario.write("---------------------------------------------------------------------------------------------------------------------------");
			bwArchivoSecundario.write("\r\n");
			bwArchivoSecundario.write("\r\n");

			// Escribe en el archivo las tres partes
			bwArchivoSecundario.write("Cartera Intermediada");
			bwArchivoSecundario.write("\r\n");
			if (!carteraIntermediada.isEmpty()) {
				for (String linea : carteraIntermediada) {
					bwArchivoSecundario.write(linea);
					bwArchivoSecundario.write("\r\n");
				}
			}
			bwArchivoSecundario.write("\r\n");
			bwArchivoSecundario.write("Cartera Compromiso");
			bwArchivoSecundario.write("\r\n");
			if (!carteraPacteada.isEmpty()) {
				for (String linea : carteraPacteada) {
					bwArchivoSecundario.write(linea);
					bwArchivoSecundario.write("\r\n");
				}
			}
			bwArchivoSecundario.write("\r\n");
			bwArchivoSecundario.write("Cartera Disponible");
			bwArchivoSecundario.write("\r\n");
			if (!carteraNormal.isEmpty()) {
				for (String linea : carteraNormal) {
					bwArchivoSecundario.write(linea);
					bwArchivoSecundario.write("\r\n");
				}
			}
			out.p_out_Comentario = "Archivos Generados Correctamente";
			out.p_out_ErroresContador = 0L;
			log.writeAsJson("SALIDA", out);
		} catch (IOException e) {
			e.printStackTrace();
			log.write(e);
			out.p_out_Comentario = "Archivo no pudo ser generado";
			out.p_out_ErroresContador = 1L;
		}
		return out;
	}
		
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			EntradaGenerarInterfazICU entrada= new EntradaGenerarInterfazICU();
			entrada.p_IdEmpresa		= new Long(1);
			entrada.p_IdModulo = "NOR";
			entrada.p_EsTodos = true;
			entrada.p_Llamado = "C08-TODOS";
			entrada.p_Fecha	= 	"2022-05-31";
			entrada.p_Ruta			= "C:\\BCS\\logs";
			JsonParser parser 		= new JsonParser();
			JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
			GenerarInterfazICU demo= new GenerarInterfazICU();
			try {
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}