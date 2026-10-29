package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.FechasIn;
import cl.bcs.plataforma.pla.ObtenerPlazo.InObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInterfazC40INTDET implements JavaCall {

	private final String SQL_SELECT_CARTERA_LIBRE_RF = loadResourceToString(
			"cl/bcs/normativa/interfaces/sql/GenerarInterfazC40INTDET_SelectCarteraLibreRF.sql");
	private final String SQL_SELECT_PACTOSVIGENTES = loadResourceToString(
			"cl/bcs/normativa/interfaces/sql/GenerarInterfazC40INTDET_SelectPactosVigentes.sql");
	private final String SQL_SELECT_SIMULTANEASVIGENTES = loadResourceToString(
			"cl/bcs/normativa/interfaces/sql/GenerarInterfazC40INTDET_SimultaneasVigentes.sql");

	public static class EntradaGenerarInterfazC40INTDET {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Fecha;
		public String p_Ruta;
	}

	public static class SalidaGenerarInterfazC40INTDET {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;

		public String P_OUT_NOMBRE_ARCHIVO_LOG;
		public String P_OUT_RUTA_ARCHIVO_LOG;
		public String P_OUT_NOMBRE_ARCHIVO;
		public String P_OUT_RUTA_ARCHIVO;
		public String P_OUT_COMENTARIO;

		public List<String> P_ARCHIVO_SECUNDARIO;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) {
		JsonObject dataout = new JsonObject();
		try {

			Gson gson = Utils.getGsonBuilder();
			EntradaGenerarInterfazC40INTDET in = gson.fromJson(jin.toString(), EntradaGenerarInterfazC40INTDET.class);
			SalidaGenerarInterfazC40INTDET out = logica(conn, in);

			JsonParser parser = new JsonParser();
			dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dataout;
	}

	public SalidaGenerarInterfazC40INTDET logica(Connection con, EntradaGenerarInterfazC40INTDET entrada) {

		SalidaGenerarInterfazC40INTDET salida = new SalidaGenerarInterfazC40INTDET();

		SimpleDateFormat sdfyyyyMMdd = new SimpleDateFormat("yyMMdd"); 																	
																	
		try {
			
			SQL sql = new SQL();
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("Fecha", entrada.p_Fecha);
			
			List<Archivo> lstRegistros = new ArrayList<Archivo>();

			lstRegistros.addAll(sql.query(con, SQL_SELECT_CARTERA_LIBRE_RF, params, Archivo.class));
			lstRegistros.addAll(sql.query(con, SQL_SELECT_PACTOSVIGENTES, params, Archivo.class));
			lstRegistros.addAll(sql.query(con, SQL_SELECT_SIMULTANEASVIGENTES, params, Archivo.class));

			Archivo archivo = new Archivo("C40DET", "C:\\\\BCS\\\\logs\\");

			String fechaSistema = Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa);
			
			Date fechaActual = new Date();
			if (entrada.p_Fecha.equals("") || entrada.p_Fecha == null) {
				fechaActual = Fecha.toDate(fechaSistema);
				entrada.p_Fecha = fechaActual.toString();
			}else {
				fechaActual = Fecha.toDate(entrada.p_Fecha);
			}

			String fechaNombreArchivo = sdfyyyyMMdd.format(fechaActual);

			salida.P_OUT_NOMBRE_ARCHIVO = archivo.NombreArchivo + fechaNombreArchivo + ".txt";
			salida.P_OUT_RUTA_ARCHIVO = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;

			File archivoIO = new File(salida.P_OUT_RUTA_ARCHIVO);
			
			if(archivoIO.exists()){
				archivoIO.delete();
			}
			
			archivoIO.createNewFile();
			
			try (BufferedWriter bwArchivoDet = new BufferedWriter(new FileWriter(archivoIO, true))) {

				StringBuilder regCabecera = new StringBuilder("");
				regCabecera.append(archivo.addCabecera());
				archivo.writeInFile(regCabecera.toString(), bwArchivoDet);

				if (!lstRegistros.isEmpty()) {

					for (Archivo detalle : lstRegistros) {
						
						String tipoOperacion = detalle.IdTipoOperacion;
						
						//Para obtener Banda
						ObtenerPlazo plazo = new ObtenerPlazo();
						InObtenerPlazo entradaPlazo = new InObtenerPlazo();
						OutObtenerPlazo salidaPlazo = new OutObtenerPlazo();
						entradaPlazo.p_IdPais = 44L;

						FechasIn fechaIn = new FechasIn();
						fechaIn.FechaDesde = entrada.p_Fecha;
						fechaIn.FechaHasta = detalle.FechaVencimiento;
						fechaIn.IdInterno = 0L;
						fechaIn.SoloDiasHabiles = "S";
						entradaPlazo.p_Fechas.add(fechaIn);
						salidaPlazo = plazo.logica(con, entradaPlazo);

						if (detalle.getIdClasificacionTipoInstrumento().equals("RF")) {
							detalle.Mercado = "Cartera Libre RF";
							detalle.IdTipoOperacion = detalle.getTipo(detalle.Nemotecnico, detalle.IdTipoReajuste);
						}
						else {
							detalle.Mercado = "Cartera Libre IF";
							detalle.IdTipoOperacion = detalle.getTipo(detalle.Nemotecnico, detalle.IdTipoReajuste);
						}

						if (tipoOperacion.contains("PACTO"))
						{
							detalle.Mercado = "Pactos Vigentes";
							detalle.IdTipoOperacion = tipoOperacion;
						}

						else if (tipoOperacion.contains("Simultanea")) {
							detalle.Mercado = "Simultaneas Vigentes";
							detalle.IdTipoOperacion = tipoOperacion;
						}
												
						detalle.Banda = detalle.getBanda(salidaPlazo.p_out_Fechas.get(0).Plazo);
						archivo.writeInFile(archivo.addRegistro(detalle), bwArchivoDet);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return salida;
	}

	public static class Archivo {

		public String Mercado;
		public String IdTipoOperacion;
		public String Nemotecnico;
		public Long FolioCartera;
		public String IdTipoReajuste;
		public BigDecimal CantidadNominal;
		public Integer CantidadTramiteCom;
		public String FechaVencimiento;
		public String FechaOperacion;
		public Long PlazoEnDias;
		public String Banda;
		public BigDecimal Capital;
		public BigDecimal INGMontoMonedaPrincipal;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal InteresDeve;
		public BigDecimal ReajusteDev;
		public BigDecimal InteresProy;
		public BigDecimal ReajusteProy;

		public String NombreArchivo;
		public String Ruta;

		public Archivo() {

		}

		public Archivo(String nombre, String ruta) {
			this.NombreArchivo = nombre;
			this.Ruta = ruta;
		}

		public void writeInFile(String msg, BufferedWriter bufferedWriter) throws IOException {
			bufferedWriter.write(msg);
			bufferedWriter.write("\r\n");
		}

		public Long getFolioCartera() {
			if (this.FolioCartera != null)
				return this.FolioCartera;
			else
				return Long.parseLong("0");
		}

		public void setIdClasificacionTipoInstrumento(String idClasificacionTipoInstrumento) {
			this.IdClasificacionTipoInstrumento = idClasificacionTipoInstrumento;
		}
		
		public String getIdClasificacionTipoInstrumento() {
			if(this.IdClasificacionTipoInstrumento != null)
				return this.IdClasificacionTipoInstrumento;
			else 
				return "";
		}
		
		public String getBanda(Long Diasplazo) {

			String banda = "";
			try {

				if (Diasplazo < 31) {// DISPONIBLE HASTA UN MES
					banda = "1";
				} else if (Diasplazo > 30 && Diasplazo < 91) {// MAS DE 1 MES HASTA 3 MESES
					banda = "2";
				} else if (Diasplazo > 90 && Diasplazo < 181) {// MAS DE 3 MESES HASTA 6 MESES
					banda = "3";
				} else if (Diasplazo > 180 && Diasplazo < 271) {// MAS DE 6 MESES HASTA 9 MESES
					banda = "4";
				} else if (Diasplazo > 270 && Diasplazo < 366) {// MAS DE 9 MESES HASTA 1 ANIO
					banda = "05";
				} else if (Diasplazo > 365 && Diasplazo < 731) {// MAS DE 1 ANIO HASTA 2 ANIOS
					banda = "6";
				} else if (Diasplazo > 730 && Diasplazo < 1096) {// MAS DE 2 ANIOS HASTA 3 ANIOS
					banda = "7";
				} else if (Diasplazo > 1095 && Diasplazo < 1461) {// MAS DE 3 ANIOS HASTA 4 ANIOS
					banda = "8";
				} else if (Diasplazo > 1460 && Diasplazo < 1826) {// MAS DE 4 ANIOS HASTA 5 ANIOS
					banda = "9";
				} else if (Diasplazo > 1825 && Diasplazo < 2556) {// MAS DE 5 ANIOS HASTA 7 ANIOS
					banda = "10";
				} else if (Diasplazo > 2555 && Diasplazo < 3651) {// MAS DE 7 ANIOS HASTA 10 ANIOS
					banda = "11";
				} else if (Diasplazo > 3650 && Diasplazo < 5476) {// MAS DE 10 ANIOS HASTA 15 ANIOS
					banda = "12";
				} else if (Diasplazo > 5475 && Diasplazo < 7301) {// MAS DE 15 ANIOS HASTA 20 ANIOS
					banda = "13";
				} else {// MAS DE 20 ANIOS
					banda = "14";
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return banda;
		}
		
		public String getTipo(String nemo, String tiporeajuste) {

			String tipo = "";
			try {

				if(nemo.equals("PRBC"))
					tipo = "PRBC";
				else if(nemo.equals("PDBC"))
						tipo = "PDBC";
				else if(nemo.contains("PRC") || nemo.contains("CERO") || nemo.contains("ZERO"))
						tipo = "PRC-CERO";
				else if(nemo.contains("BR") || nemo.contains("BCP") || nemo.contains("BCU") || nemo.contains("BCD") || nemo.contains("BCX") )
						tipo = "BR-BCP-BCU-BCD-BCX";
				else if(nemo.contains("DPF") || nemo.equals("PAGARE NR"))
						tipo = "DPF";
				else if(nemo.contains("DPR") || nemo.equals("PAGARE R"))
						if(tiporeajuste.equals("US"))
							tipo = "DPR US$";
						else 
							tipo = "DPR";
				else if(nemo.equals("LH"))
					tipo = "L.H.";
				else 
					tipo = "OTROS RENTA FIJA";				
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			return tipo;
		}
		
		

		public String addCabecera() {
			return String.format("%-25s%s%s%s%s%s%s%s%s%s%s%s%s%s", "Mercado;", "Tipo;", "Nemo;", "Secuencia;",
					"Tipo Reajuste;", "Cantidad Nominal;", "Fec.Vcto.Inst;", "Dias;", "Banda;",
					"Fecha Cup?n;", "Capital;Interes Deve.;", "Reajuste Deve.;", "Interes Proy.;", "Reajuste Proy.;");
		}

		public String addRegistro(Archivo archivo) {
			
			DecimalFormat decimalFormat = new DecimalFormat("#####0.0000");
		    DecimalFormatSymbols dfs = decimalFormat.getDecimalFormatSymbols();
		    dfs.setDecimalSeparator(',');
		    decimalFormat.setDecimalFormatSymbols(dfs);
			
			return String.format("%-25s;%-20s;%-10s;%8s;%-2s;%20s;%10s;%5s;%5s;%10s;%20s;%20s;%20s;%20s;%20s%31s",
					archivo.Mercado, 
					archivo.IdTipoOperacion, 
					archivo.Nemotecnico,
					archivo.getFolioCartera() != 0 ? archivo.getFolioCartera().toString() : "", 
					archivo.IdTipoReajuste,
					decimalFormat.format(archivo.CantidadNominal).toString(),
					archivo.FechaVencimiento, 
					archivo.PlazoEnDias,
					archivo.Banda, 
					archivo.FechaOperacion,
					decimalFormat.format(archivo.Capital).toString(),
					decimalFormat.format(archivo.InteresDeve).toString(),
					decimalFormat.format(archivo.ReajusteDev).toString(),
					decimalFormat.format(archivo.InteresProy).toString(),
					decimalFormat.format(archivo.ReajusteProy).toString(),
					"");
		}

	}
	
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		Gson gson = Utils.getGsonBuilder();
		try {
			conn = PlataformaUtils.getQAConnection();
			System.out.println("== INICIO ==");
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_Modulo\":\"NOR\",\"p_Fecha\":\"2022-06-01\",\"p_Ruta\":\"C:\\\\BCS\\\\logs\"}";
			EntradaGenerarInterfazC40INTDET entrada = gson.fromJson(json, EntradaGenerarInterfazC40INTDET.class);
			GenerarInterfazC40INTDET demo = new GenerarInterfazC40INTDET();
			SalidaGenerarInterfazC40INTDET ObjOut = demo.logica(conn, entrada);

			System.out.println(ObjOut.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");
	}

}
