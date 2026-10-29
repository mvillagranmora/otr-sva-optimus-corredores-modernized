package cl.bcs.normativa.cartola;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class GenerarCartolaPDF implements JavaCall{

	public static class GenerarCartolaPDF_Entrada {
		
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Long p_IdUsuario;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public List<Dato> p_Datos;
		public String p_token;
		
		public GenerarCartolaPDF_Entrada() {
			this.p_Datos = new ArrayList<Dato>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	public static class Dato {
		public Long IdCliente;
		public Long IdPortafolio;
	}
	
	public static class GenerarCartolaPDF_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public String CartolaEncoded;
		public String nombrearchivo;

		public GenerarCartolaPDF_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		/**
		 * Return an ISO 8601 combined date and time string for current date/time
		 * 
		 * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
		 */
		public String getISO8601StringForCurrentDate() {
			Date now = new Date();
			return getISO8601StringForDate(now);
		}

		/**
		 * Return an ISO 8601 combined date and time string for specified date/time
		 * 
		 * @param date
		 *            Date
		 * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
		 */
		private String getISO8601StringForDate(Date date) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
			dateFormat.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
			return dateFormat.format(date);
		}

		/**
		 * Private constructor: class cannot be instantiated
		 */

	public GenerarCartolaPDF_Salida logica(Connection conn, GenerarCartolaPDF_Entrada in) throws Exception {
		
		Long out_IdCliente = null;
		String out_IdPortafolio = null;
		String p_ruta = "/BSCL/Plataforma/UI/CAR/Cartola.html";
		String ruta = null;
		String consultahost = null;
		String timestamp = getISO8601StringForCurrentDate();
		String nombrearchivo = null;
		try {
			 ruta = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.RutaArchivosConfiguracion");
			System.out.println(ruta);
		} catch (Exception e) {
			throw e;
		}
		try {
			consultahost =  ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.UrlHana");
		} catch (Exception e) {
			throw e;
		}
		GenerarCartolaPDF_Salida salida = new GenerarCartolaPDF_Salida();
		List<String> tmp_IdPortafolio = new ArrayList<>();
		for(Dato p_datos: in.p_Datos) {
			out_IdCliente = p_datos.IdCliente;
			tmp_IdPortafolio.add(p_datos.IdPortafolio.toString());
		}
		out_IdPortafolio = String.join(",", tmp_IdPortafolio);
		String command = "node " + ruta + "puppeteer/obtenercartolapdf.js " + in.p_IdEmpresa + " "
				+in.p_IdUsuario + " " + in.p_IdModulo + " " + in.p_FechaDesde + " " + in.p_FechaHasta + " "
				+out_IdCliente + " " + out_IdPortafolio + " " + consultahost + " " + p_ruta + " "+timestamp+" "
				+in.p_token;
		System.out.println(command);
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-icm", command); // LINUX
		builder.redirectErrorStream(true);
		builder.directory(new File(ruta+"puppeteer/"));
		Process p = builder.start();
		salida.nombrearchivo = out_IdCliente+"_"+timestamp+".pdf";
		StringBuilder out = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));){
        	String line = null, previous = null;
            while ((line = br.readLine()) != null)
                if (!line.equals(previous)) {
                    previous = line;
                    out.append(line).append('\n');
                    salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "error de ejecucion", line));
                }	
        }        
		if (p.waitFor() == 0) {			
			nombrearchivo = ruta + "puppeteer/" + out_IdCliente + "_" + timestamp + ".pdf";
			byte[] bFile = readBytesFromFile(nombrearchivo);
			if (bFile == null) {
			} else {
				salida.CartolaEncoded = new String(Base64.encodeBase64(bFile));
				File fichero = new File(nombrearchivo);
				if (fichero.delete())
					salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "error de ejecucion", "Se borro el archivo"));
					else
						salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "error de ejecucion", "No se puede borrar el archivo"));
			}
        }else {
        }		
		return salida;
	}

	private static byte[] readBytesFromFile(String filePath) {
        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;
        try {
            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return bytesArray;
    }
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		GenerarCartolaPDF_Entrada in = gson.fromJson(jin.toString(), GenerarCartolaPDF_Entrada.class);
		GenerarCartolaPDF_Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String jsonCL ="{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":133,\"p_FechaDesde\":\"2019-07-01\",\"p_FechaHasta\":\"2019-07-24\",\"p_Datos\":[{\"IdCliente\":395174,\"IdPortafolio\":343710}],\"p_token\":\"MTpydWxsb2E6MjAxOS0wNy0zMCAxMToyOTo1NC45NzIwMDAw\"}"; 
			GenerarCartolaPDF_Entrada Objin = gson.fromJson(jsonCL, GenerarCartolaPDF_Entrada.class);
			GenerarCartolaPDF demo = new GenerarCartolaPDF();
		try {
				GenerarCartolaPDF_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
