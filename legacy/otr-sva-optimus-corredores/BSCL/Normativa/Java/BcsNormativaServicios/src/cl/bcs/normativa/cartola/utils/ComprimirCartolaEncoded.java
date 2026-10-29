package cl.bcs.normativa.cartola.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.icu.text.DecimalFormat;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ComprimirCartolaEncoded implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ComprimirCartolaEncoded.class);

	public static class ComprimirCartolaEncoded_Entrada {
		public Long 	p_IdEmpresa;
		public String 	p_IdModulo;
		public Long 	p_IdUsuario;
		public Long 	p_IdCartola;
		
		public ComprimirCartolaEncoded_Entrada() {
			this.p_IdEmpresa = 1L;
			this.p_IdModulo  = "NOR";
			this.p_IdUsuario = 9L;
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ComprimirCartolaEncoded_Salida {
		public Long 					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		
		public ComprimirCartolaEncoded_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CartolaUpdate {
		public Long 	IdCartola;
		public String 	CartolaEncoded;
	}
	
	private ComprimirCartolaEncoded_Salida logica (Connection conn, ComprimirCartolaEncoded_Entrada in) {
		ComprimirCartolaEncoded_Salida out = new ComprimirCartolaEncoded_Salida();
		try {
			System.out.println("CartolaEncoded");
			out.P_OUT_ERRORESCONTADOR = 0L;
			
			// confirmacion de ejecucion de script
			@SuppressWarnings("resource")
			Scanner confirmacion = new Scanner(System.in);
			System.out.println("*******************       ALERTA       *******************");
			System.out.println("Este script actualiza el campo CartolaEncoded en bytes no cifrados");
			System.out.println("Se recomienda realizar un respaldo de la tabla BSCL.Normativa.BD::CAR.Cartola antes de ejectuar el script");
			System.out.println("**********************************************************");
			System.out.println("¿Desea continuar? escriba SI para continuar");
			System.out.println("**********************************************************");
			String respuestaConfirmacion = confirmacion.nextLine();
			
			if (respuestaConfirmacion.equals("SI")) {
				// Se realiza SELECT de esta manera ya que el SQL Query no es compatible con Blob
				final String sqlCartola = "SELECT \"IdCartola\", \"CartolaEncoded\" FROM \"BSCL.Normativa.BD::CAR.Cartola\" WHERE \"CartolaEncoded\" IS NOT NULL";
				PreparedStatement st = conn.prepareStatement(sqlCartola);
				
				if(st.execute()) {
					DecimalFormat format = new DecimalFormat("##.00");
					double totalPercent = 0;
					int i = 0;
					
					ResultSet rs = st.getResultSet();
					while (rs.next()) {

						String cartolaEncodedOriginal = rs.getString("CartolaEncoded");
						
						// 1. descifrar texto Base64
						Base64 decoder = new Base64();
						byte[] cartolaEncodedDecoded = decoder.decode(cartolaEncodedOriginal);

						// 2. comprimir CartolaEncoded descifrado y actualizar en base de datos
						byte cartolaCompressed[] = comprimir(cartolaEncodedDecoded);
						actualizarCartolaEncoded(conn, cartolaCompressed, rs.getLong("IdCartola"));
						
						// Mensaje de valor actual (Original)
						String message = "Original size: " + cartolaEncodedOriginal.length();
						StringBuilder string = new StringBuilder();
						string.append("IdCartola: " + rs.getLong("IdCartola"));
						string.append(" - ");
						string.append(message);
						
						// Mensaje de valor comprimido
						message = "Compressed size: " + cartolaCompressed.length;
						string.append(" - ");
						string.append(message);
						string.append(" - ");
						
						// calcula porcentaje de compresión
						Double percent = 0d;
						if (cartolaEncodedOriginal.length()>0) {
							percent = calcularPorcentaje(cartolaEncodedOriginal.getBytes(), cartolaCompressed);
						}
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "CARTOLA", message));
						string.append(format.format(percent) + "% compressed");

						System.out.println(string.toString());

						// totaliza el porcentaje para luego calcular el promedio
						totalPercent += percent;
						i++;
					}
					System.out.println("...............");
					if (i>0) {
						double promedio = totalPercent/i;
						System.out.println(i + " registros comprimidos - Promedio de compresión " + format.format(promedio) + "%");
					} else {
						System.out.println("No se encontraron registros para comprimir");
					}
				}			
			} else {
				System.out.println("Proceso cancelado...");
			}
			
		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(out.P_OUT_ERRORESCONTADOR, MensajeSalida.MENSAJE_ERROR, "CARTOLA", e.toString()));
			System.out.println(e.toString());
			return out;
		}
		return out;	
	}
	
	public byte[] comprimir (byte[] data) throws IOException {
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		
		deflater.setInput(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		deflater.finish();
		
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		
		return outputStream.toByteArray();
	}
	
	public byte[] descomprimir (byte[] data) throws IOException, DataFormatException {
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte [1024];
		while(!inflater.finished()) {
			int count = inflater.inflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		
		return outputStream.toByteArray();
	}
	
	private void actualizarCartolaEncoded(Connection conn, byte[] bytes, Long idCartola) throws SQLException  {
		final String sqlUpdateCartolaEncoded = "UPDATE \"BSCL.Normativa.BD::CAR.Cartola\" SET \"CartolaEncoded\" = ? WHERE \"IdCartola\" = ?";
		PreparedStatement update = conn.prepareStatement(sqlUpdateCartolaEncoded);
		update.setBytes(1, bytes);
		update.setLong(2, idCartola);
		update.executeUpdate();
	}
	
	private Double calcularPorcentaje(byte[] original, byte[] comprimido) {
		return (((double)original.length - comprimido.length) / original.length)*100;
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ComprimirCartolaEncoded_Entrada entrada = gson.fromJson(jin.toString(), ComprimirCartolaEncoded_Entrada.class);
		ComprimirCartolaEncoded_Salida salida = logica(conn, entrada);
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) {
		System.out.println("== BEGIN MAIN ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
							
				String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":109,\"p_IdCartola\":225652}";
				ComprimirCartolaEncoded_Entrada Objin = gson.fromJson(json, ComprimirCartolaEncoded_Entrada.class);
				ComprimirCartolaEncoded c = new ComprimirCartolaEncoded();
				ComprimirCartolaEncoded_Salida ObjOut = c.logica(con, Objin);
				//con.rollback();
				con.commit();
				con.close();
				
				logger.info(ObjOut.toString());
				System.out.println(ObjOut.toString());
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END MAIN ==");
	}
}
