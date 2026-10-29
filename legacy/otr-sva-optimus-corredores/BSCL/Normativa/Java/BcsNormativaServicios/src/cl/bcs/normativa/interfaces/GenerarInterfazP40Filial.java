package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ValorizacionValoresAdicionalesBatch;
import cl.bcs.custodia.ValorizacionValoresAdicionalesBatch.InValorizacionValoresAdicionales;
import cl.bcs.normativa.interfaces.GenerarInterfazP40.DatoFilial;
import cl.bcs.normativa.interfaces.GenerarInterfazP40.DatosFFMM;
import cl.bcs.normativa.interfaces.GenerarInterfazP40.DatosRV;
import cl.bcs.normativa.interfaces.GenerarInterfazP40.TipoRV;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.DatoPais;
import cl.bcs.normativa.interfaces.pojo.ParametrosGeneracionP40;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInterfazP40Filial implements JavaCall {
	
	public static final String NOMBRE_ARCHIVO_LOG_PRINCIPAL = "/LogInterfazP40.log";
	static final Logger logger = LogManager.getLogger(GenerarInterfazP40Filial.class);
	/*
	 * Interfaz P40 - Filial
	 * 
	 * NOTA:  Esta interfaz genera informacion detallada por el tipo de instrumentos que comprenden rubros de
	 * "Instrumentos para negociacion", "Instrumentos de inversion disponibles para la venta" e "Instrumentos
	 * de inversion hasta el vencimiento", mencionados en el Compendio de Normas Contables.
	 * 
	 * La informacion corresponde a las posiciones mantenidas por cuenta propia de la FILIAL DE LA CORREDORA 
	 * en forma individual
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
	private final String SQL_P40_SELECT_RUT_FILIAL = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectRUTFilial.sql");
	private final String SQL_P40_Select_Pais = "SELECT \"IdPais\" FROM BSCL.\"BSCL.Plataforma.BD::PLA.Empresa\" WHERE \"IdEmpresa\" = :IdEmpresa;";
	
	private final String SQL_P40_SELECT_RF_IF = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectRF_IF.sql");
	private final String SQL_P40_SELECT_FFMM = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectFFMM.sql");	
	private final String SQL_P40_SELECT_RV_CUSTODIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectRV_Custodia.sql");
	private final String SQL_P40_SELECT_RV_GARANTIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectRV_Garantia.sql");	
	private final String SQL_P40_SELECT_RV_SIMULTANEA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectRV_Simultanea.sql");	

	private final String SQL_P40_SELECT_HIS_RF_IF = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_HIS_SelectRF_IF.sql");
	private final String SQL_P40_SELECT_HIS_FFMM = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_HIS_SelectFFMM.sql");	
	private final String SQL_P40_SELECT_HIS_RV_CUSTODIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_HIS_SelectRV_Custodia.sql");
	private final String SQL_P40_SELECT_HIS_RV_GARANTIA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_HIS_SelectRV_Garantia.sql");	
	private final String SQL_P40_SELECT_HIS_RV_SIMULTANEA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_HIS_SelectRV_Simultanea.sql");
	
	public static class EntradaGenerarInterfazP40Filial {
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String 			p_IdModulo;
		public String			p_Fecha;				// Fecha de proceso		
		public String           p_Rut;					// Rut de filial
		public String			p_Ruta;					// Ruta de archivo de interfaz
		public String			p_Banco;				// Codigo del banco 
		public String			p_Mercado;				// (RF/IF/ACN/FM/"": todos) -> ACN: Acciones Nacionales		
		public String			p_TipoCartera;			// (1)	 -- Activos financieros para negociar a valor razonable con cambios en resultados
		public String			p_UsaNemoSVS;			// (S/N) -- Uso de Nemo SVS (solo para IF)
		public String			p_ExcluyeGarantia;		// (S/N) -- Excluye Garantia 	(solo para RV)		
		public String			p_ExcluyeSimultanea;	// (S/N) -- Excluye Simultanea 	(solo para RV)
		public Integer			p_FamiliaInstrumento;	// (S/N) -- Familia de Instrumento
		
		/*
		 * TIPO DE CARTERA
		 * Indica la clasificacion del instrumento, debiendose utilizar
		 * los siguientes codigos: 
		 * 
		 * 1 Instrumentos para negociacion 
		 * 2 Instrumentos disponibles para la venta 
		 * 3 Inversiones al vencimiento
		 */
		
		public EntradaGenerarInterfazP40Filial() {		// Default values
			p_IdModulo			 = "NOR";
			p_Fecha     		 = "";					// "": Interfaz P40 genera la fecha actual
			p_Banco 			 = "223";
			p_Mercado			 = "";					// (RF/IF/ACN/FM/"": todos) -> ACN: Acciones Nacionales
			p_TipoCartera		 = "1";					// 1: Activos financieros para negociar a valor razonable con cambios en resultados
			p_UsaNemoSVS		 = "S";
			p_ExcluyeGarantia	 = "N";
			p_ExcluyeSimultanea	 = "S";
			p_FamiliaInstrumento = 49; 
		}
	}
	
	public class SalidaArchivoFilial{
		public List<String> 		P_OUT_ARCHIVOS;
		public String		 	 	P_OUT_NOMBRE_ARCHIVO_LOG;
		public String				P_OUT_COMENTARIO;
		public String				P_OUT_NOMBRE_ZIP;
				
		public SalidaArchivoFilial() {
			P_OUT_ARCHIVOS = new ArrayList<String>();
		}
	}
	
	public static class SalidaGenerarInterfazP40Filial {
		public List<String> 		P_OUT_ARCHIVOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR;

		public String				P_OUT_NOMBRE_ARCHIVO_LOG_PRINCIPAL;
		public String				P_OUT_RUTA_ARCHIVO_LOG_PRINCIPAL;
		public List<String>			P_OUT_NOMBRE_ARCHIVO_LOG;
		public List<String> 	 	P_OUT_RUTA_ARCHIVO_LOG;
		public List<String>			P_OUT_NOMBRE_ARCHIVO;
		public List<String>			P_OUT_RUTA_ARCHIVO; 
		public String				P_OUT_COMENTARIO;
		public List<SalidaArchivoFilial>	P_OUT_ARCHIVOS_POR_FILIAL;

		
		public SalidaGenerarInterfazP40Filial() {
			P_OUT_NOMBRE_ARCHIVO_LOG_PRINCIPAL = NOMBRE_ARCHIVO_LOG_PRINCIPAL;
			P_OUT_NOMBRE_ARCHIVO_LOG = new ArrayList<String>();
			P_OUT_RUTA_ARCHIVO_LOG = new ArrayList<String>();
			P_OUT_NOMBRE_ARCHIVO = new ArrayList<String>();			
			P_OUT_RUTA_ARCHIVO = new ArrayList<String>();
			P_OUT_ARCHIVOS = new ArrayList<String>();
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_ARCHIVOS_POR_FILIAL = new ArrayList<SalidaArchivoFilial>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaGenerarInterfazP40Filial logica (Connection con, EntradaGenerarInterfazP40Filial entrada) throws Exception {
		SalidaGenerarInterfazP40Filial salida = new SalidaGenerarInterfazP40Filial();
		List<SalidaArchivoFilial> salidaArchivoFilial = new ArrayList<>();
		salida.P_OUT_RUTA_ARCHIVO_LOG_PRINCIPAL = entrada.p_Ruta + salida.P_OUT_NOMBRE_ARCHIVO_LOG_PRINCIPAL; 
		File log = new File(salida.P_OUT_RUTA_ARCHIVO_LOG_PRINCIPAL);
		try (BufferedWriter bwLog = new BufferedWriter(new FileWriter(log))) {
			
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
			
			GenerarInterfazP40 p40 = new GenerarInterfazP40();
			p40.registrarLog(con, entrada.p_IdEmpresa, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_FECHA_EJECUCION_PROCESO").msg(), bwLog);

			/* IMPORTANTE:
			 * 		 	Las sentencias SQL dependen del Rut de la FILIAL DE LA CORREDORA, las cuales estan registradas en la 
			 * 	  tabla "BSCL.Normativa.BD::INT.FilialesP40". Esta clase esta desacoplada para recibir una filial especifica
			 * 	  o una lista de filiales para ejecutar en la pauta todas las filiales que se encuentran en dicha tabla.
			 */
			
			if (entrada.p_Rut == null) {
				entrada.p_Rut = "";
			}
			
			ValorizacionValoresAdicionalesBatch  valAdicional = new ValorizacionValoresAdicionalesBatch();
			InValorizacionValoresAdicionales entradaVal = new InValorizacionValoresAdicionales();	
			entradaVal.p_IdEmpresa= entrada.p_IdEmpresa;		
			entradaVal.p_IdUsuario = entrada.p_IdUsuario;
			entradaVal.p_IdModulo = entrada.p_IdModulo;
			entradaVal.p_FechaSistema = entrada.p_Fecha;
			entradaVal.p_ValorizaCarteraParaInterfaces = "S";

			try {
			  ValorizacionValoresAdicionalesBatch.OutValorizacionValoresAdicionales salidaVal = valAdicional.logica(con,entradaVal);
			}catch (Exception e) {
				salida.P_OUT_ERRORESCONTADOR = Long.sum(salida.P_OUT_ERRORESCONTADOR, 1L)  ;
				salida.P_OUT_COMENTARIO = e.getMessage();
			} 
			
			SQL sql = new SQL();
			HashMap<String, Object> paramsRut = new HashMap<String, Object>();
			paramsRut.put("IdEmpresa", entrada.p_IdEmpresa);
			Long IdPais 	= sql.query(con, SQL_P40_Select_Pais,paramsRut , DatoPais.class).get(0).IdPais  	;
			paramsRut.put("Rut", entrada.p_Rut);
			List<DatoFilial> datoFilial = sql.query(con, SQL_P40_SELECT_RUT_FILIAL, paramsRut, DatoFilial.class);
			
			// Valida si existen filiales en la consulta
			if (datoFilial.isEmpty()) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_COMENTARIO = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "DATOS_FILIAL_NO_ENCONTRADO").msg();
				p40.writeInFile(salida.P_OUT_COMENTARIO, bwLog);	// Registra en archivo Log principal
				
			} else {
				
				
				for (DatoFilial filial: datoFilial) {

					SalidaArchivoFilial archivoFilial = new SalidaArchivoFilial();
					String nombreZip = filial.NombreZip;
					List<String> listArchivos = new ArrayList<>();
					
					// Archivos P40 de la filial
					salida.P_OUT_NOMBRE_ARCHIVO.add(filial.NombreArchivo + fechayyyymmdd + ".txt");
					salida.P_OUT_NOMBRE_ARCHIVO_LOG.add(filial.NombreArchivo + fechayyyymmdd + "DET.log");		
					salida.P_OUT_RUTA_ARCHIVO.add(entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO.get(salida.P_OUT_NOMBRE_ARCHIVO.size()-1));
					salida.P_OUT_RUTA_ARCHIVO_LOG.add(entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO_LOG.get(salida.P_OUT_NOMBRE_ARCHIVO_LOG.size()-1));
					
					File archivo = new File(salida.P_OUT_RUTA_ARCHIVO.get(salida.P_OUT_RUTA_ARCHIVO.size()-1));
					File archivoLog = new File(salida.P_OUT_RUTA_ARCHIVO_LOG.get(salida.P_OUT_RUTA_ARCHIVO.size()-1));
					
					listArchivos.add(entrada.p_Ruta + "/" + filial.NombreArchivo + fechayyyymmdd + ".txt");
					listArchivos.add(entrada.p_Ruta + "/" + filial.NombreArchivo + fechayyyymmdd + "DET.log");
					
					archivoFilial.P_OUT_ARCHIVOS= listArchivos;
					archivoFilial.P_OUT_NOMBRE_ZIP = nombreZip;
					archivoFilial.P_OUT_NOMBRE_ARCHIVO_LOG = filial.NombreArchivo + fechayyyymmdd + "DET.log";
					
					
					try (BufferedWriter bwArchivo = new BufferedWriter(new FileWriter(archivo))) {
						try (BufferedWriter bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));) {
							// Registro de inicio del proceso en el archivo Log
							p40.registrarLog(con, entrada.p_IdEmpresa, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_FECHA_EJECUCION_PROCESO").msg(), bwArchivoLog);
							
							String jerarquiaValorRazonable = esFinDeMes(con, pFecha) ? "02" : "00";
							
							ParametrosGeneracionP40 parametrosP40 = new ParametrosGeneracionP40();
							parametrosP40.CodigoTenedor = ("0000" + filial.CodigoTenedor).substring(filial.CodigoTenedor.length());
							parametrosP40.TipoCartera = entrada.p_TipoCartera;
							parametrosP40.FechaOperacionyyyymmdd = fechayyyymmdd;
							parametrosP40.JerarquiaValorRazonable = jerarquiaValorRazonable;
							parametrosP40.Archivo = bwArchivo;
							parametrosP40.ArchivoLog = bwArchivoLog;
							
							// Estructura de primer registro
							StringBuilder reg0 = new StringBuilder("");
							reg0.append("0" + entrada.p_Banco);					// (04) Codigo del banco  (223)
							reg0.append("P40");								// (03) Interfaz		  (P40)
							reg0.append(fechayyyymmdd);						// (08) Fecha del proceso 
							reg0.append(String.format("%293s", " "));		// Filler (260)
							
							p40.writeInFile(reg0.toString(), bwArchivo);	// Registra informacion en la primera linea del archivo
						
							// Consulta la informacion de Renta Fija e Intermediacion financiera (Registro 1) 
							HashMap<String, Object> paramsRFIF = new HashMap<String, Object>();
							paramsRFIF.put("IdEmpresa", entrada.p_IdEmpresa);
							paramsRFIF.put("UsaNemoSVS", entrada.p_UsaNemoSVS.toUpperCase());
							paramsRFIF.put("TipoCartera", entrada.p_TipoCartera);
							paramsRFIF.put("Mercado", entrada.p_Mercado);
							paramsRFIF.put("IdCliente", filial.IdCliente);
							paramsRFIF.put("Fecha", entrada.p_Fecha  );
							List<GenerarInterfazP40.DatosRFIF> datosR1;
							
							if (Historico) {							
								datosR1 = sql.query(con, SQL_P40_SELECT_HIS_RF_IF, paramsRFIF, GenerarInterfazP40.DatosRFIF.class);	
							}else {
								datosR1 = sql.query(con, SQL_P40_SELECT_RF_IF, paramsRFIF, GenerarInterfazP40.DatosRFIF.class);		
							}						
							  
							// Loop RF - IF
							// Consulta la cartera, garantias y compras con pactos (COMPAC) en las operaciones de RF e IF 
							if (entrada.p_Mercado.equals("RF") || entrada.p_Mercado.equals("IF") || entrada.p_Mercado.equals("")) { 
								for (GenerarInterfazP40.DatosRFIF rfif : datosR1) {									
									p40.generarRFIF(rfif, parametrosP40);
								}
							}
							
							if (entrada.p_Mercado.equals("FM") || entrada.p_Mercado.equals("")) {
								// Consulta la informacion de FFMM (Registro 2)
								HashMap<String, Object> paramsFM = new HashMap<String, Object>();
								paramsFM.put("IdEmpresa", entrada.p_IdEmpresa);
								paramsFM.put("IdCliente", filial.IdCliente);
								List<DatosFFMM> datosR2;
								if (Historico) {
								paramsFM.put("Fecha", entrada.p_Fecha  );
								datosR2 = sql.query(con, SQL_P40_SELECT_HIS_FFMM, paramsFM, DatosFFMM.class);	
								}else {
									datosR2 = sql.query(con, SQL_P40_SELECT_FFMM, paramsFM, DatosFFMM.class);		
								}
								
								
								// Loop FFMM
								for (DatosFFMM fm : datosR2) {
									p40.generarFFMM(fm, parametrosP40);
								}					
							}
							
							if ((entrada.p_Mercado.equals("ACN") || entrada.p_Mercado.equals(""))) {						
								// Consulta la informacion de RV - Custodia (Registro 3)
								HashMap<String, Object> paramsRVCustodia = new HashMap<String, Object>();
								paramsRVCustodia.put("IdEmpresa", entrada.p_IdEmpresa);
								paramsRVCustodia.put("Mercado", entrada.p_Mercado.toUpperCase());
								paramsRVCustodia.put("IdCliente", filial.IdCliente);
								List<DatosRV> datosR3;
								if (Historico) {
								paramsRVCustodia.put("Fecha", entrada.p_Fecha  );
								datosR3 = sql.query(con, SQL_P40_SELECT_HIS_RV_CUSTODIA, paramsRVCustodia, DatosRV.class);	
								}else {
									datosR3 = sql.query(con, SQL_P40_SELECT_RV_CUSTODIA, paramsRVCustodia, DatosRV.class);		
								}
								
								
								
								// Loop RV - Custodia
								for (DatosRV rv : datosR3) {
									p40.generarRV(rv, parametrosP40, TipoRV.CARTERA);
								}
								
								if (entrada.p_ExcluyeGarantia.equals("N")) {
									// Consulta la informacion de Garantias
									HashMap<String, Object> paramsRVGarantia = new HashMap<String, Object>();
									paramsRVGarantia.put("IdEmpresa", entrada.p_IdEmpresa);
									paramsRVGarantia.put("Mercado", entrada.p_Mercado.toUpperCase());
									paramsRVGarantia.put("IdCliente", filial.IdCliente);
									List<DatosRV> datosR3Gar;
									if (Historico) {
									paramsRVGarantia.put("Fecha", entrada.p_Fecha);
										datosR3Gar = sql.query(con, SQL_P40_SELECT_HIS_RV_GARANTIA, paramsRVGarantia, DatosRV.class);	
									}else {
										datosR3Gar = sql.query(con, SQL_P40_SELECT_RV_GARANTIA, paramsRVGarantia, DatosRV.class);	
									}
									
									
									
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
								paramsRVSimultanea.put("IdCliente", filial.IdCliente);
								List<DatosRV> datosR3SIM;
								if (Historico) {
									paramsRVSimultanea.put("Fecha", entrada.p_Fecha);
									datosR3SIM = sql.query(con, SQL_P40_SELECT_HIS_RV_SIMULTANEA, paramsRVSimultanea, DatosRV.class);	
								}else {
									datosR3SIM = sql.query(con, SQL_P40_SELECT_RV_SIMULTANEA, paramsRVSimultanea, DatosRV.class);	
								}
								
								
								
								// Loop RV - Custodia con Simultanea
								for (DatosRV simult : datosR3SIM) {
									p40.generarRV(simult, parametrosP40, TipoRV.SIMULTANEA);
								}
							}
							// Registro de culminacion del proceso en el archivo Log principal y el de la filial
							String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ARCHIVO_GENERADO_EXITOSAMENTE").msg();
							mensaje = mensaje + " FILIAL: " + filial.NombreFilial.toUpperCase() ; 
							p40.registrarLog(con, entrada.p_IdEmpresa, mensaje, bwArchivoLog);
							p40.writeInFile(mensaje + " -> " + salida.P_OUT_RUTA_ARCHIVO.get(salida.P_OUT_RUTA_ARCHIVO.size()-1), bwLog);
							p40.writeInFile(mensaje + " -> " + salida.P_OUT_RUTA_ARCHIVO_LOG.get(salida.P_OUT_RUTA_ARCHIVO_LOG.size()-1), bwLog);							
							archivoFilial.P_OUT_COMENTARIO = mensaje;
							
						/* Exception de Archivo Log */
						} catch (NullPointerException e) {
							salida.P_OUT_ERRORESCONTADOR = 1L;
							salida.P_OUT_COMENTARIO = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "VALOR_NULO").msg(); 
							archivoFilial.P_OUT_COMENTARIO = salida.P_OUT_COMENTARIO; 
						} catch (Exception e) {
							salida.P_OUT_ERRORESCONTADOR = 1L;
							salida.P_OUT_COMENTARIO = e.getMessage();
							archivoFilial.P_OUT_COMENTARIO = salida.P_OUT_COMENTARIO;
						} finally {
							// Agrega el archivo log generado en la lista de archivos para comprimir en archivo Zip
							salida.P_OUT_ARCHIVOS.add(salida.P_OUT_RUTA_ARCHIVO_LOG.get(salida.P_OUT_RUTA_ARCHIVO_LOG.size()-1));
							if (salida.P_OUT_ERRORESCONTADOR>0L) {
								p40.writeInFile(Diccionario.getMensaje(con, entrada.p_IdEmpresa, salida.P_OUT_COMENTARIO).msg(), bwLog);
							}
						}
					
					/* Exception de Archivo */
					} catch (IOException e) {
						salida.P_OUT_ERRORESCONTADOR = 1L;
						salida.P_OUT_COMENTARIO = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ERROR_ABRIR_ARCHIVO").msg() + " " + salida.P_OUT_RUTA_ARCHIVO;
						archivoFilial.P_OUT_COMENTARIO = salida.P_OUT_COMENTARIO;
					} catch (Exception e) {
						salida.P_OUT_ERRORESCONTADOR = 1L;
						salida.P_OUT_COMENTARIO = e.getMessage();
						archivoFilial.P_OUT_COMENTARIO = salida.P_OUT_COMENTARIO;
					} finally {
						// Agrega el archivo generado en la lista de archivos para comprimir en archivo Zip
						salida.P_OUT_ARCHIVOS.add(salida.P_OUT_RUTA_ARCHIVO.get(salida.P_OUT_RUTA_ARCHIVO.size()-1));
						if (salida.P_OUT_ERRORESCONTADOR>0L) {	// Si ocurre una exception se registra en el archivo Log principal 
							p40.writeInFile(Diccionario.getMensaje(con, entrada.p_IdEmpresa, salida.P_OUT_COMENTARIO).msg(), bwLog);
						}
					}
					
					//agregamos el archivo de la filial leida
					salidaArchivoFilial.add(archivoFilial);
					
				}
				
				// Mensaje satisfactorio en el log principal y en la salida de la interfaz
				if (salida.P_OUT_ERRORESCONTADOR==0L) {
					salida.P_OUT_COMENTARIO = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ARCHIVO_GENERADO_EXITOSAMENTE").msg();
				} else {	// Registra mensaje de error si aplica
					p40.writeInFile(Diccionario.getMensaje(con, entrada.p_IdEmpresa, salida.P_OUT_COMENTARIO).msg(), bwLog);
				}
				p40.registrarLog(con, entrada.p_IdEmpresa, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "PROCESO_FINALIZADO").msg(), bwLog);
				
				salida.P_OUT_ARCHIVOS_POR_FILIAL = salidaArchivoFilial;
				return salida;
			}
			
		/* Exception principal */
		} catch (IOException e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_COMENTARIO = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ERROR_ABRIR_ARCHIVO").msg() + " " + salida.P_OUT_RUTA_ARCHIVO_LOG_PRINCIPAL;
		} catch (NullPointerException e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_COMENTARIO = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "VALOR_NULO").msg();
		} catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_COMENTARIO = e.getMessage();
		}
		
		salida.P_OUT_ARCHIVOS_POR_FILIAL = salidaArchivoFilial;
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
		EntradaGenerarInterfazP40Filial entrada = gson.fromJson(jin.toString(), EntradaGenerarInterfazP40Filial.class);
		SalidaGenerarInterfazP40Filial salida = logica(conn, entrada);
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = null;
		try {
			System.out.println("== INICIO ==");
			conn = PlataformaUtils.getDESAConnection() ;
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_Modulo\":\"NOR\",\"p_DatoFilial\":[{\"Rut\":11111111-1,\"IdCliente\":100209,\"NombreArchivo\":\"LEITO_054_\"}],\"p_Fecha\":\"\",\"p_Ruta\":\"C:\\\\BCS\\\\logs\",\"p_Banco\":\"223\",\"p_SBIF\":\"281\",\"p_Mercado\":\"\",\"p_TipoCartera\":\"0\",\"p_UsaNemoSVS\":\"N\",\"p_ExcluyeGarantia\":\"N\",\"p_ExcluyeSimultanea\":\"N\"}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_Modulo\":\"NOR\",\"p_Rut\":\"\",\"p_Fecha\":\"\",\"p_Ruta\":\"C:\\\\BCS\\\\logs\",\"p_Banco\":\"223\",\"p_Mercado\":\"\",\"p_TipoCartera\":\"0\",\"p_UsaNemoSVS\":\"N\",\"p_ExcluyeGarantia\":\"N\",\"p_ExcluyeSimultanea\":\"N\"}";
			EntradaGenerarInterfazP40Filial entrada = new EntradaGenerarInterfazP40Filial();
			entrada.p_IdEmpresa 			= 1L;
			entrada.p_IdUsuario 			= 51L;
			entrada.p_IdModulo 				= "NOR";
			entrada.p_Fecha 				= ""; // Fecha ejecucion vacia => Hoy ; Diferente a hoy => Historica
			entrada.p_Rut 					= "76727730-K"; // Rut filial , Vacia = todas
			entrada.p_Ruta 					= System.getenv("USERPROFILE") + "\\Desktop\\Interfaces";
			entrada.p_Banco 				= "223";
			entrada.p_Mercado 				= "";
			entrada.p_TipoCartera 			= "1";
			entrada.p_UsaNemoSVS 			= "N";
			entrada.p_ExcluyeGarantia 		= "N";
			entrada.p_ExcluyeSimultanea 	= "N";
			entrada.p_FamiliaInstrumento 	= 49	;GenerarInterfazP40Filial demo = new GenerarInterfazP40Filial();
			SalidaGenerarInterfazP40Filial ObjOut = demo.logica(conn, entrada);
			//logger.info(ObjOut.toString());
			System.out.println(ObjOut.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");		
	}
}
