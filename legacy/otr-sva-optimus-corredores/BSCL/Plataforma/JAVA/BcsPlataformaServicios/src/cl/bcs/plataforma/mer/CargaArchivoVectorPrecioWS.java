package cl.bcs.plataforma.mer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.fondomutuo.CargarValorCuota;
import cl.bcs.plataforma.mer.CargaArchivoVectorPrecio_mer.ArchivoVectorPrecio;
import cl.bcs.plataforma.mer.CargaArchivoVectorPrecio_mer.EntradaCargaArchivoVectorPrecio;
import cl.bcs.plataforma.mer.CargaArchivoVectorPrecio_mer.SalidaCargaArchivoVectorPrecio;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoVectorPrecioWS implements JavaCall {
	static final Logger logger = LogManager.getLogger(CargarValorCuota.class);
	private final String SQL_SELECT_FUENTE = loadResourceToString("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_SelectFuente.sql");
	private final String SQL_TABLA_REGISTROS = loadResourceToString("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_TablaRegistros.sql");
	private final String SQL_TABLA_REGISTROS_INSERT = loadResourceToString("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_InsertTablaRegistros.sql");
	private final String SQL_SELECT_VALORES = loadResourceToString("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_SelectTablaRegistros.sql");
	private final String SQL_DELETE_TABLA = loadResourceToString("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_DeleteTablaRegistros.sql");
	private final String SQL_INSTRUMENTOS = loadResourceToString("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_SelectInstrumentos.sql");
	
	public static class Instrumento{
		public String Nemotecnico;
		public Long IdInstrumento;
		public String Fecha;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Fuente {
		public Long IdFuente;
		public String Codigo;
	}
	public static class Count{
		public Long Count;
		@Override
		public String toString() {
			return String.format("Count [Count=%s]", Count);
		}
	}
	
	public static class EntradaCargaArchivoVectorPrecioWS{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Entidad;
		public List <PrecioRegistro> p_Precios;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PrecioRegistro {
		public String Nemotecnico;
		public String FechaIngreso;
		public BigDecimal Precio;
	}
	
	public static class PrecioIngresado {
		public String Nemotecnico;
		public String Entidad;
		public String Fecha;
		public BigDecimal Valor;
	}
	
	public static class TablaRegistros {
		public String Nemotecnico;
		public String Entidad;
		public Date Fecha;
		public BigDecimal ValorAntiguo;
		public BigDecimal ValorNuevo;
	}
	
	public static class SalidaPrecio {
		public String Nemotecnico;
		public String FechaActualizacion;
		public BigDecimal PrecioAntiguo;
		public BigDecimal PrecioActualizado;
	}
	
	public static class LogErrorPrecio {
		public String Nemotecnico;
		public String FechaIngreso;
		public String PrecioIngresado;
		public String Mensaje;
	}
	
	public static class IntrumentosVAL {
		public String Nemotecnico;
		public String IdClasificacionTipoInstrumento;
	}
	
	public static class SalidaCargaArchivoVectorPrecioWS extends SalidaEstandar{
		public List<SalidaPrecio> p_PRECIOS;	
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<LogErrorPrecio> P_PRECIOS_ERR;
		
		public SalidaCargaArchivoVectorPrecioWS(){
			super();
			this.p_PRECIOS = new ArrayList<SalidaPrecio>();
			this.P_PRECIOS_ERR = new ArrayList<LogErrorPrecio>();
			this.P_OUT_ADVERTENCIASCONTADOR=0;
			this.P_OUT_ERRORESCONTADOR=0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciaDetalle>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErrorDetalle>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		EntradaCargaArchivoVectorPrecioWS in =gson.fromJson(jin.toString() ,EntradaCargaArchivoVectorPrecioWS.class);
		SalidaCargaArchivoVectorPrecioWS out=logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	 
	public SalidaCargaArchivoVectorPrecioWS logica(Connection conn, EntradaCargaArchivoVectorPrecioWS in) throws Exception {
		SalidaCargaArchivoVectorPrecioWS salida = new SalidaCargaArchivoVectorPrecioWS();		
		SQL sql = new SQL();
		
		RevisarRequisitosBasicos requisitos = new RevisarRequisitosBasicos();		
		RevisarRequisitosBasicosSalida out_requistos = requisitos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, "PLA");
		
		if(!out_requistos.p_out_Mensajes.isEmpty()) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.addAll(out_requistos.p_out_Mensajes);
			return salida;
		}
		
		/// OBTENCION DE PARAMETROS
		System.out.println("Entrada: " + in);
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List <SalidaPrecio> resultadoWS = new ArrayList<SalidaPrecio>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");	
		Date fechaSIS = format.parse(fechaSistema);
		String parametroFecha = "";
		BigDecimal precioMax = new BigDecimal("99999999999");
		
		try {
			parametroFecha = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa,"PLATAFORMA", "PLA.FechaCargaVectorPrecios");
		} catch (Exception e) {
			parametroFecha = "N";
		}
		
		
		//// VALIDACIONES
		
		if (!in.p_Precios.isEmpty()) {
			
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("Entidad", in.p_Entidad);
			
			/// SI EXISTE FUENTE INGRESADA
			
			List<Fuente> fuente = sql.query(conn, SQL_SELECT_FUENTE, params, Fuente.class);
			
			if(fuente.isEmpty()) {
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "FUENTE_NO_EXISTE", new HashMap<>(), conn, in.p_IdEmpresa));
				return salida;
			}
			
			
			//// VALIDACION INSTRUMENTO RV
			
			HashMap<String, Object> paramIns = new HashMap<String, Object>();
			
			List <String> instrumentos = in.p_Precios.stream().map(precio -> precio.Nemotecnico).distinct().collect(Collectors.toList());	
			paramIns.put("Instrumentos", instrumentos);			
			List <IntrumentosVAL> ins = sql.query(conn, SQL_INSTRUMENTOS, paramIns, IntrumentosVAL.class);
			
			Set<String> insRV = ins.stream().map(instrumento -> instrumento.Nemotecnico).collect(Collectors.toSet());
			
			
			/// VALIDACIONES PREVIAS A CREACION DEL ARCHIVO
			
			for (PrecioRegistro pre : in.p_Precios) {
				Date fechaREG = format.parse(pre.FechaIngreso);
					
				if("S".equalsIgnoreCase(parametroFecha)) {
					if (!fechaREG.equals(fechaSIS)) {
						salida.P_PRECIOS_ERR.add(crearLogPrecio(pre, "Fecha de Carga distinta a Fecha de Sistema"));					
						salida.P_OUT_ERRORESCONTADOR++;
					}
				}
					
				else {
					if (fechaREG.after(fechaSIS)) {		
						salida.P_PRECIOS_ERR.add(crearLogPrecio(pre, "Fecha de Carga superior a Fecha de Sistema"));	
						salida.P_OUT_ERRORESCONTADOR++;
					}			
				}
					
				if(!insRV.contains(pre.Nemotecnico)) {					
					salida.P_PRECIOS_ERR.add(crearLogPrecio(pre, "Nemotecnico no existe y/o no pertenece a Clasificacion RV"));
					salida.P_OUT_ERRORESCONTADOR++;
					}
				if(pre.Precio.compareTo(BigDecimal.ZERO) < 0) {
					salida.P_PRECIOS_ERR.add(crearLogPrecio(pre, "Precio Ingresado inferior a 0"));
					salida.P_OUT_ERRORESCONTADOR++;

				}
				else {
					if(pre.Precio.compareTo(precioMax) > 0) {
						salida.P_PRECIOS_ERR.add(crearLogPrecio(pre, "Precio ingresado superior a maximo permitido 99.999.999.999"));
						salida.P_OUT_ERRORESCONTADOR++;
					}
				}
			}
			
			if(salida.P_PRECIOS_ERR.isEmpty() && salida.P_OUT_ERRORESCONTADOR == 0L) {
				
				/// CONSTRUCCION DE ARCHIVO 
				
				SQL.exec(conn, SQL_TABLA_REGISTROS);	
				List <PrecioIngresado> insertRegistros = new ArrayList<PrecioIngresado>();		
				StringBuilder archivo = new StringBuilder();
				Integer contador= 0; 
				
				archivo.append("[");
				
				for(PrecioRegistro reg : in.p_Precios) {
					
					PrecioIngresado registro = new PrecioIngresado();			
					registro.Nemotecnico = reg.Nemotecnico;
					registro.Entidad = in.p_Entidad;
					registro.Fecha = reg.FechaIngreso;
					registro.Valor = reg.Precio;
					
					
					//// PARA GENERAR EL ARCHIVO DE ENTRADA
					
					String fechaFormateada = LocalDate.parse(reg.FechaIngreso).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
					
					archivo.append("{")
					.append("\"INSTRUMENTO\":\"").append(reg.Nemotecnico).append("\",")
					.append("\"FECHA\":\"").append(fechaFormateada).append("\",")
					.append("\"VALOR\":\"").append(reg.Precio).append("\",")
					.append("\"MENSAJEVALIDACION\":\"OK\",")
					.append("\"ENTIDAD\":\"").append(in.p_Entidad).append("\",")
					.append("\"TIPOREGISTRO\":\"").append(in.p_Entidad + " PRECIO").append("\"")
					.append("}");
					
					if(contador < (in.p_Precios.size() - 1)) 
					{
						archivo.append(",");
					}
					
					insertRegistros.add(registro);

					contador++;
				}
				
				archivo.append("]");
				
				System.out.println("archivo: " + archivo.toString());
				
				// INSERTAMOS REGISTROS EN TABLA TEMPORAL
				
				sql.batch(conn, SQL_TABLA_REGISTROS_INSERT, insertRegistros, PrecioIngresado.class);
				
				/// INSTANCIAR CARGAR ARCHIVOS MER
				/// MANDAMOS EL ARCHIVO MODO SIMULACION
				EntradaCargaArchivoVectorPrecio archivoWS = new EntradaCargaArchivoVectorPrecio();
				
				archivoWS.p_IdEmpresa = in.p_IdEmpresa;
				archivoWS.p_IdUsuario = in.p_IdUsuario;
				archivoWS.p_IdModulo = "PLA";
				archivoWS.p_CodigoArchivo = "CARGA_VECTOR_PRECIOS_PLA";
				archivoWS.P_Simular = "S";
				archivoWS.p_Archivo = archivo.toString();
				
				CargaArchivoVectorPrecio_mer demo = new CargaArchivoVectorPrecio_mer();
				
				SalidaCargaArchivoVectorPrecio ca = demo.logica(conn, archivoWS);
				
				if(ca.P_OUT_ERRORESCONTADOR == 0L) {
					
					System.out.println("SE PROCEDE SIN ERRORES");
					
					/// SE RECUPERAN LOS PRECIOS MODIFICADOS
					
					List <TablaRegistros> PreciosModificados = sql.query(conn, SQL_SELECT_VALORES, new HashMap<String, Object>(), TablaRegistros.class); 
					archivoWS.P_Simular= "N";
					
					SalidaCargaArchivoVectorPrecio carga = demo.logica(conn, archivoWS);
					
					if(carga.P_OUT_ERRORESCONTADOR == 0L) {
						
						/// LOG Y SALIDA
						System.out.println("SE ARMA LOG DE SALIDA");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						
						for (TablaRegistros tb : PreciosModificados) {	
							SalidaPrecio precioTB = new SalidaPrecio();
							String fechaFormateada = sdf.format(tb.Fecha);
							
							precioTB.Nemotecnico = tb.Nemotecnico;
							precioTB.FechaActualizacion = fechaFormateada;
							precioTB.PrecioAntiguo = tb.ValorAntiguo;
							precioTB.PrecioActualizado = tb.ValorNuevo;		
							resultadoWS.add(precioTB);	
						}
						
						agregarLog(conn, in, resultadoWS, fechaSistema);
						salida.p_PRECIOS.addAll(resultadoWS);					
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION,  "ARCHIVO_CARGADO_EXITO", new HashMap<>(), conn, in.p_IdEmpresa));
						
						SQL.exec(conn, SQL_DELETE_TABLA);
						conn.commit();
						
						return salida;
					}
					
				}
				else {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "EJECUCION_CON_ERRORES", new HashMap<>(), conn, in.p_IdEmpresa));
					for (ArchivoVectorPrecio res : ca.p_Resultado) {				
						if("ERR".equalsIgnoreCase(res.ESTADOVALIDACION)) {
							LogErrorPrecio logPrecio = new LogErrorPrecio();
							logPrecio.Nemotecnico = res.INSTRUMENTO;
							logPrecio.FechaIngreso = res.FECHA;
							logPrecio.PrecioIngresado = res.VALOR;
							logPrecio.Mensaje = res.MENSAJEVALIDACION;		
							salida.P_PRECIOS_ERR.add(logPrecio);
							salida.P_OUT_ERRORESCONTADOR++;
						}
						
					}	
					SQL.exec(conn, SQL_DELETE_TABLA);
					
					return salida;
				}
			}
			
			else {
				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "EJECUCION_CON_ERRORES", new HashMap<>(), conn, in.p_IdEmpresa));
				return salida;
			}		
		}
		
		else {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "ARCHIVO_VACIO", new HashMap<>(), conn, in.p_IdEmpresa));
			return salida;
		}
		
		return salida;
			
	}
	
	private void agregarLog(Connection con, EntradaCargaArchivoVectorPrecioWS in, List<SalidaPrecio> resultadoPrecio, String FechaSistema) {
		try {
			logger.debug("INICIO::CertificarMovimientosEnTransito.agregarLog");
			long inicio = System.currentTimeMillis();
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			//CABECERA
			log.IdTrabajo = 1L; 
			log.IdCabeceraLog = in.p_IdUsuario; //
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = "PLA";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = FechaSistema;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdCliente = 0L; //
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = in.p_Entidad + " PRECIOS";
			log.IdRegistro = "";
			log.EstadoRegistro = "";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad =  BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = ""; //
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "cl.bcs.plataforma.mer.CargaArchivoVectorPrecioWS";
			
			entradaAgregarLog.p_Log.add(log);
			
			//DETALLE
			for (SalidaPrecio precio : resultadoPrecio) {
				InAgregarLog.DetalleLog detalle= new InAgregarLog.DetalleLog();
				detalle.IdCabeceraLog = in.p_IdUsuario;
				detalle.SubRegistro = precio.Nemotecnico;
				detalle.IdTrabajo = 1L;
				detalle.CampoRegistro = in.p_Entidad + " PRECIOS " + "("+ precio.FechaActualizacion +")";
				detalle.ValorAntiguo = precio.PrecioAntiguo.compareTo(BigDecimal.ZERO) == 0 ? "0": precio.PrecioAntiguo.toString();
				detalle.ValorNuevo = precio.PrecioActualizado.compareTo(BigDecimal.ZERO) == 0? "0": precio.PrecioActualizado.toString();
				
				entradaAgregarLog.p_LogDetalle.add(detalle);
				
			}
			
			//REGISTRO	
			entradaAgregarLog.p_IdEmpresa = in.p_IdEmpresa;
			entradaAgregarLog.p_IdUsuario = in.p_IdUsuario;
			entradaAgregarLog.p_IdTrabajo = 1L;
			entradaAgregarLog.p_IdModulo = "PLA";
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
				
			long fin = System.currentTimeMillis();
			logger.debug("FIN::CargaArchivoVectorPrecioWS.agregarLog:" , fin - inicio);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private static LogErrorPrecio crearLogPrecio (PrecioRegistro registro , String msg){
		
		LogErrorPrecio logPrecio = new LogErrorPrecio();
		
		logPrecio.Nemotecnico = registro.Nemotecnico;
		logPrecio.FechaIngreso = registro.FechaIngreso;
		logPrecio.PrecioIngresado = registro.Precio.toString();
		logPrecio.Mensaje = msg;	
		
		return logPrecio;
	}

	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa2")){	
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\r\n"
						+ "    \"p_IdEmpresa\": 1,\r\n"
						+ "    \"p_IdUsuario\": 9572,\r\n"
						+ "    \"p_Entidad\": \"RISK AMERICA\",\r\n"
						+ "    \"p_Precios\": [\r\n"
						+ "        {\r\n"
						+ "            \"Nemotecnico\": \"COPEC\",\r\n"
						+ "            \"FechaIngreso\": \"2026-05-19\",\r\n"
						+ "            \"Precio\": 150.49\r\n"
						+ "        },\r\n"
						+ "        {\r\n"
						+ "            \"Nemotecnico\": \"BESALCO\",\r\n"
						+ "            \"FechaIngreso\": \"2026-05-19\",\r\n"
						+ "            \"Precio\": 180.17\r\n"
						+ "        },\r\n"
						+ "        {\r\n"
						+ "            \"Nemotecnico\": \"COPEC\",\r\n"
						+ "            \"FechaIngreso\": \"2026-05-01\",\r\n"
						+ "            \"Precio\": 160.17\r\n"
						+ "        }\r\n"
						+ "    ]\r\n"
						+ "}";
				
				EntradaCargaArchivoVectorPrecioWS Objin = gson.fromJson(json, EntradaCargaArchivoVectorPrecioWS.class);
				CargaArchivoVectorPrecioWS demo = new CargaArchivoVectorPrecioWS();
				System.out.println("====== ENTRADA ======");
				SalidaCargaArchivoVectorPrecioWS ObjOut = demo.logica(con, Objin);
				con.rollback();
				//con.commit();
				System.out.println("====== SALIDA ======");
				System.out.println(ObjOut.toString());
				
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
