package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ValorizacionValoresAdicionalesBatch;
import cl.bcs.custodia.ValorizacionValoresAdicionalesBatch.InValorizacionValoresAdicionales;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.EntradaGenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.SalidaGenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40_Scotia.DatoRUT;
import cl.bcs.normativa.interfaces.GenerarInterfazP40_Scotia.DatosFFMM;
import cl.bcs.normativa.interfaces.GenerarInterfazP40_Scotia.DatosRFIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40_Scotia.DatosRV;
import cl.bcs.normativa.interfaces.GenerarInterfazP40_Scotia.TipoRV;
import cl.bcs.normativa.interfaces.pojo.ParametrosGeneracionP40;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInterfazP40SBIF_Scotia implements GenerarInterfazP40SBIFBase {
	
	/*
	 * CELZORRO-681
	 * Interfaz P40 - SBIF 
	 * 
	 * NOTA:  Esta interfaz genera informacion detallada por el tipo de instrumentos que comprenden rubros de
	 * "Instrumentos para negociacion", "Instrumentos de inversion disponibles para la venta" e "Instrumentos
	 * de inversion hasta el vencimiento", mencionados en el Compendio de Normas Contables.
	 * 
	 * La informacion corresponde a las posiciones mantenidas por cuenta propia de la CORREDORA en forma individual
	 * 
	 * Primer Registro
	 * 		Codigo del banco
	 * 		Identifiacion del archivo (P40)
	 * 		Fecha (AAAAMMDD)
	 * 
	 * Registros siguientes
	 * 		01 	Instrumentos de deuda		(RF e IF)
	 * 		02 	Cuotas de fondos mutuos		(FFMM)
	 * 		03 	Instrumentos de capital		(RV)
	 */	

	private String NOMBRE_ARCHIVO_P40_054 = "P40_085_"; //ScotiaR parametrizado
	private final String SQL_P40_SELECT_RUT = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40SBIF_SelectRUTCorredora.sql");
	private final String SQL_P40_Select_Pais = "SELECT \"IdPais\" FROM BSCL.\"BSCL.Plataforma.BD::PLA.Empresa\" WHERE \"IdEmpresa\" = :IdEmpresa;";
	
	private final String SQL_P40_SELECT_RF_IF = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40SBIF_Scotia_SelectRF_IF.sql");	
	private final String SQL_P40_SELECT_FFMM = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40SBIF_SelectFFMM.sql");	
	private final String SQL_P40_SELECT_RV_CUSTODIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40SBIF_SelectRV_Custodia.sql");
	private final String SQL_P40_SELECT_RV_GARANTIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40SBIF_SelectRV_Garantia.sql");	
	private final String SQL_P40_SELECT_RV_SIMULTANEA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40SBIF_SelectRV_Scotia_Simultanea.sql");	
	
	private final String SQL_P40_HIS_SELECT_RF_IF = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40SBIF_Scotia_SelectHIS_RF_IF.sql");	
	private final String SQL_P40_HIS_SELECT_FFMM = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40SBIF_SelectHIS_FFMM.sql");	
	private final String SQL_P40_HIS_SELECT_RV_CUSTODIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40SBIF_SelectHIS_RV_Custodia.sql");
	private final String SQL_P40_HIS_SELECT_RV_GARANTIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40SBIF_SelectHIS_RV_Garantia.sql");	
	private final String SQL_P40_HIS_SELECT_RV_SIMULTANEA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40SBIF_SelectHIS_RV_Scotia_Simultanea.sql");
	

	public static class DatoPais {
		public Long 		IdPais;
	}
	
	
	public SalidaGenerarInterfazP40SBIF logica (Connection con, EntradaGenerarInterfazP40SBIF entrada) throws Exception {
		NOMBRE_ARCHIVO_P40_054 = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "NORMATIVA", "NOMBRE_P40"); 
		SalidaGenerarInterfazP40SBIF salida = new SalidaGenerarInterfazP40SBIF();
		List<String> listArchivos = new ArrayList<>();
		GenerarInterfazP40_Scotia p40 = new GenerarInterfazP40_Scotia();
		String fechaSistema = Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa);		
		Date fechaActual = Fecha.toDate(fechaSistema);
		Boolean Historico = false;
        // valida si la fecha de entrada es nula
		if (entrada.p_Fecha == null || entrada.p_Fecha.equals("")) {	
			entrada.p_Fecha = fechaActual.toString();
		}
		
		if (!fechaActual.toString().equalsIgnoreCase(entrada.p_Fecha)) // si no es igual la fecha de entrada entonces se utiliza para ver el historico
		{	Historico = true ;		}
		
		// Asignacion de formato en Fechas
		Date pFecha = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);	// Formato de fecha de entrada YYYY-MM-DD
		SimpleDateFormat sdfyyyyMMdd = new SimpleDateFormat("yyyyMMdd"); 			// Salida de Fecha de Proceso YYYYMMDD		
		String fechayyyymmdd = sdfyyyyMMdd.format(pFecha);
		String ano = fechayyyymmdd.substring(0, 4);
	    String mes = fechayyyymmdd.substring(4, 6);
	    String dia = fechayyyymmdd.substring(6, 8);
		NOMBRE_ARCHIVO_P40_054 = NOMBRE_ARCHIVO_P40_054.replace("[ANIO]", ano) 
	             .replace("[MES]", mes)
	             .replace("[DIA]", dia);		
		
		
		// Configuracion para salida de archivo      
		salida.P_OUT_NOMBRE_ARCHIVO = NOMBRE_ARCHIVO_P40_054+ ".txt";
		salida.P_OUT_NOMBRE_ARCHIVO_LOG = NOMBRE_ARCHIVO_P40_054 + "DET.log";		
		salida.P_OUT_RUTA_ARCHIVO = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
		salida.P_OUT_RUTA_ARCHIVO_LOG = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
		
		File archivo = new File(salida.P_OUT_RUTA_ARCHIVO);
		File archivoLog = new File(salida.P_OUT_RUTA_ARCHIVO_LOG);
		
		listArchivos.add(entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO);
		
		
		
		
		try (BufferedWriter bwArchivo = new BufferedWriter(new FileWriter(archivo))) {
			try (BufferedWriter bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));) {
				
				p40.registrarLog(con, entrada.p_IdEmpresa, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_FECHA_EJECUCION_PROCESO").msg(), bwArchivoLog);
				
				SQL sql = new SQL();
				// Obtiene el IdCliente y Rut de la Corredora
				HashMap<String, Object> paramsRut = new HashMap<String, Object>();
				paramsRut.put("IdEmpresa", entrada.p_IdEmpresa);
				List<DatoRUT> datoRut 	= sql.query(con, SQL_P40_SELECT_RUT, paramsRut	, DatoRUT.class);
				Long IdPais 			= sql.query(con, SQL_P40_Select_Pais,paramsRut , DatoPais.class).get(0).IdPais  	;
				
				ValorizacionValoresAdicionalesBatch  valAdicional = new ValorizacionValoresAdicionalesBatch();
				InValorizacionValoresAdicionales entradaVal = new InValorizacionValoresAdicionales();	
				entradaVal.p_IdEmpresa= entrada.p_IdEmpresa;		
				entradaVal.p_IdUsuario = entrada.p_IdUsuario;
				entradaVal.p_IdModulo = entrada.p_IdModulo;
				entradaVal.p_FechaSistema = entrada.p_Fecha;
				entradaVal.p_ValorizaCarteraParaInterfaces = "S";
				entradaVal.p_IdCliente = datoRut.get(0).IdCliente	;
				
				try {
					ValorizacionValoresAdicionalesBatch.OutValorizacionValoresAdicionales salidaVal = valAdicional.logica(con,entradaVal);
				}catch (Exception e) {
					salida.P_OUT_ERRORESCONTADOR = Long.sum(salida.P_OUT_ERRORESCONTADOR, 1L)  ;
					salida.P_OUT_COMENTARIO = e.getMessage();
				} 
				
				
				String jerarquiaValorRazonable = esFinDeMes(con, pFecha) ? "02" : "00";
				
				ParametrosGeneracionP40 parametrosP40 = new ParametrosGeneracionP40();
				parametrosP40.CodigoTenedor = ("0000" + entrada.p_SBIF).substring(entrada.p_SBIF.length());
				parametrosP40.TipoCartera = entrada.p_TipoCartera;
				parametrosP40.FechaOperacionyyyymmdd = fechayyyymmdd;
				parametrosP40.JerarquiaValorRazonable = jerarquiaValorRazonable;
				parametrosP40.Archivo = bwArchivo;
				parametrosP40.ArchivoLog = bwArchivoLog;
				
				// Concatenacion de strings para el archivo CV40_054 (P40 SBIF)
				StringBuilder reg0 = new StringBuilder("");
				reg0.append("0" + entrada.p_Banco);					// (04) Codigo del banco (223)
				reg0.append("P40");									// (03) Identificacion del archivo (P40)
				reg0.append(fechayyyymmdd);							// (08) Fecha del proceso 
				reg0.append(String.format("%293s", " "));			// Filler (260)
				
				p40.writeInFile(reg0.toString(), bwArchivo);	// Registra informacion en la primera linea del archivo
				
				// Consulta la informacion de Renta Fija e Intermediacion financiera (Registro 01)
				HashMap<String, Object> paramsRFIF = new HashMap<String, Object>();
				paramsRFIF.put("IdEmpresa", entrada.p_IdEmpresa);
				paramsRFIF.put("UsaNemoSVS", entrada.p_UsaNemoSVS);
				paramsRFIF.put("Mercado", entrada.p_Mercado);
				paramsRFIF.put("TipoCartera", entrada.p_TipoCartera);
				paramsRFIF.put("IdCliente", datoRut.get(0).IdCliente);	
				paramsRFIF.put("Fecha", entrada.p_Fecha );

				List<DatosRFIF> datosR1;
				if(Historico) 	{						
					datosR1 = sql.query(con, SQL_P40_HIS_SELECT_RF_IF, paramsRFIF, DatosRFIF.class);	}
				else 			{	
					datosR1 = sql.query(con, SQL_P40_SELECT_RF_IF, paramsRFIF, DatosRFIF.class);		}
				
				// Loop RF - IF
				// Consulta las Compras con Pactos (COMPAC) de los Clientes en las operaciones de RF e IF 
				if (entrada.p_Mercado.equals("RF") || entrada.p_Mercado.equals("IF") || entrada.p_Mercado.equals("")) {

				for (GenerarInterfazP40_Scotia.DatosRFIF rfif : datosR1) {
					p40.generarRFIF(rfif, parametrosP40);
				  }
				}

				if (entrada.p_Mercado.equals("FM") || entrada.p_Mercado.equals("")) {
					
					// Consulta la informacion de FFMM (Registro 02)
					HashMap<String, Object> paramsFM = new HashMap<String, Object>();
					paramsFM.put("IdEmpresa", entrada.p_IdEmpresa);
					paramsFM.put("IdCliente", datoRut.get(0).IdCliente);
					
					List<DatosFFMM> datosR2;
					if(Historico) 	{
						paramsFM.put("Fecha", entrada.p_Fecha);
						datosR2 = sql.query(con, SQL_P40_HIS_SELECT_FFMM, paramsFM, DatosFFMM.class);	}
					else 			{	
						datosR2 = sql.query(con, SQL_P40_SELECT_FFMM, paramsFM, DatosFFMM.class);		}					
					// Loop FFMM
					for (DatosFFMM fm : datosR2) {
						p40.generarFFMM(fm, parametrosP40);
					}
				}
				
				if (entrada.p_Mercado.equals("ACN") || entrada.p_Mercado.equals("")) {
					
					// Consulta la informacion de RV - Custodia (Registro 03)
					HashMap<String, Object> paramsRVCustodia = new HashMap<String, Object>();
					paramsRVCustodia.put("IdEmpresa", entrada.p_IdEmpresa);
					paramsRVCustodia.put("Mercado", entrada.p_Mercado);
					paramsRVCustodia.put("IdCliente", datoRut.get(0).IdCliente);
					
					List<DatosRV> datosR3;
					if(Historico) 	{
						paramsRVCustodia.put("Fecha", entrada.p_Fecha);
						datosR3 = sql.query(con, SQL_P40_HIS_SELECT_RV_CUSTODIA, paramsRVCustodia, DatosRV.class);	}
					else			{	
						datosR3 = sql.query(con, SQL_P40_SELECT_RV_CUSTODIA, paramsRVCustodia, DatosRV.class);		}
					 
					
					// Loop RV - Custodia
					for (DatosRV rv : datosR3) {
						p40.generarRV(rv, parametrosP40, TipoRV.CARTERA);
					}
					
					if (entrada.p_ExcluyeGarantia.equals("N")) {
						
						// Consulta la informacion de Garantias
						HashMap<String, Object> paramsRVGarantia = new HashMap<String, Object>();
						paramsRVGarantia.put("IdEmpresa", entrada.p_IdEmpresa);
						paramsRVGarantia.put("Mercado", entrada.p_Mercado);
						paramsRVGarantia.put("IdCliente", datoRut.get(0).IdCliente);
						
						List<DatosRV> datosR3Gar;
						
						if (Historico) 	{	
							paramsRVGarantia.put("Fecha", entrada.p_Fecha);
							datosR3Gar = sql.query(con, SQL_P40_HIS_SELECT_RV_GARANTIA, paramsRVGarantia, DatosRV.class);	}
						else 			{	
							datosR3Gar = sql.query(con, SQL_P40_SELECT_RV_GARANTIA, paramsRVGarantia, DatosRV.class);		}
						
						// Loop RV - Custodia con Garantia
						for (DatosRV rv : datosR3Gar) {
							p40.generarRV(rv, parametrosP40, TipoRV.GARANTIA);
						}
					}	
				}
				
				if (entrada.p_Mercado.equals("") && (entrada.p_ExcluyeSimultanea.equals("N"))) {
						
					// Consulta la informacion de RV - Simultanea
					HashMap<String, Object> paramsRVSimultanea = new HashMap<String, Object>();
					paramsRVSimultanea.put("IdEmpresa", entrada.p_IdEmpresa);
					paramsRVSimultanea.put("IdCliente", datoRut.get(0).IdCliente);
					
					
					List<DatosRV> datosR3SIM ;
					
					 if (Historico) {	
						 paramsRVSimultanea.put("Fecha", entrada.p_Fecha);
						 datosR3SIM = sql.query(con, SQL_P40_HIS_SELECT_RV_SIMULTANEA, paramsRVSimultanea, DatosRV.class);	}
					 else			{	
						 datosR3SIM = sql.query(con, SQL_P40_SELECT_RV_SIMULTANEA, paramsRVSimultanea, DatosRV.class);		}
					 
					 
					// Loop RV - Custodia con Simultanea
					for (DatosRV simult : datosR3SIM) {
						p40.generarRV(simult, parametrosP40, TipoRV.SIMULTANEA);
					}
				}
				
				salida.P_OUT_COMENTARIO = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ARCHIVO_GENERADO_EXITOSAMENTE").msg();
				salida.P_OUT_ARCHIVOS = listArchivos;
				p40.registrarLog(con, entrada.p_IdEmpresa, salida.P_OUT_COMENTARIO, bwArchivoLog);
				
			} catch (NullPointerException e) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_COMENTARIO = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "VALOR_NULO").msg(); 
			} catch (IOException e) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_COMENTARIO = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ERROR_ABRIR_ARCHIVO").msg() + " " + salida.P_OUT_RUTA_ARCHIVO;
			} catch (Exception e) {
				System.out.println(e);
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_COMENTARIO = e.getMessage();
			} 
			
		} catch (IOException e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_COMENTARIO = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ERROR_ABRIR_ARCHIVO").msg() + " " + salida.P_OUT_RUTA_ARCHIVO;
		} catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_COMENTARIO = e.getMessage();
		} 
		
		return salida;
	}
	
	public boolean esFinDeMes(Connection con, Date fechaOperacion) throws Exception {
	    Calendar calFechaOperacion = Calendar.getInstance();
	    calFechaOperacion.setTime(fechaOperacion);

	    int ultimoDiaDelMes = calFechaOperacion.getActualMaximum(Calendar.DAY_OF_MONTH);
	    int diaOperacion = calFechaOperacion.get(Calendar.DAY_OF_MONTH);

	    return diaOperacion == ultimoDiaDelMes;
	}
	
	public String getFechaFinDeMes(Connection con, Date fechaOperacion) throws Exception{
 
        String fechaPrimerDiaMesSiguiente = getFechaPrimerDiaMesSiguiente(fechaOperacion);
        
	    ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
	    InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
	    inObtenerDiaAnterior.idPais = 44L;
	    inObtenerDiaAnterior.diaHabil = "S";
	    inObtenerDiaAnterior.soloCaeDiaHabil = "S";
	    InObtenerDiaAnterior.FechasIn fechaInicial = new InObtenerDiaAnterior.FechasIn();
	    fechaInicial.fechaInicial = fechaPrimerDiaMesSiguiente;
	    fechaInicial.numeroDias = 1L;
	    inObtenerDiaAnterior.fechas.add(fechaInicial);
	    OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(con, inObtenerDiaAnterior);

	    return outObtenerDiaAnterior.fechas.get(0).fechaCalculada;
	    
	}
	
	public String getFechaPrimerDiaMesSiguiente(Date fechaOperacion) {
		
		Calendar cal = Calendar.getInstance();
        cal.setTime(fechaOperacion);
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
        return dateFormat.format(cal.getTime());
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarInterfazP40SBIF in = gson.fromJson(jin.toString(), EntradaGenerarInterfazP40SBIF.class);
		SalidaGenerarInterfazP40SBIF out = logica(conn, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		Connection conn = null;
		Gson gson = Utils.getGsonBuilder();
		try {
			System.out.println("== INICIO ==");
			conn = PlataformaUtils.getDESAConnection();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1646,\"p_Modulo\":\"NOR\",\"p_Fecha\":\"2025-11-30\",\"p_Ruta\":\"C:\\\\BCS\\\\logs\",\"p_Banco\":\"223\",\"p_SBIF\":\"104\",\"p_Mercado\":\"\",\"p_TipoCartera\":\"1\",\"p_UsaNemoSVS\":\"S\",\"p_ExcluyeGarantia\":\"N\",\"p_ExcluyeSimultanea\":\"N\"}";			
			EntradaGenerarInterfazP40SBIF entrada = gson.fromJson(json, EntradaGenerarInterfazP40SBIF.class);
			GenerarInterfazP40SBIF_Scotia demo = new GenerarInterfazP40SBIF_Scotia();
			SalidaGenerarInterfazP40SBIF ObjOut = demo.logica(conn, entrada);
			System.out.println(ObjOut.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		} 
		System.out.println("== FIN ==");		
	}

}
