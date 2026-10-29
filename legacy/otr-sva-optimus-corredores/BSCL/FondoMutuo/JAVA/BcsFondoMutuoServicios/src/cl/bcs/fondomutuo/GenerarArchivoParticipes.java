package cl.bcs.fondomutuo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarArchivoParticipes implements JavaCall
{

	private final int LARGO_REGISTRO = 180;
	
	private final String SQL_CONSULTA = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarArchivoParticipes_ConsultaArchivoParticipes.sql");
	private final String SQL_SELECT_HOMOLOGACION = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultaGeneracionArchivos_SelectHomologacion.sql");
	private final static Logger logger = LogManager.getLogger(GenerarArchivoParticipes.class);
	private final String [] TIPOS_VALOR = {"CodigoNacionalidad" ,"CodigoEstadoCivil"};
	
    public static class GenerarArchivoParticipes_Entrada {
    	public String p_IdModulo;
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_FechaCurse;
        public String p_FondoMutuo;
        public String p_Emisor;
        public String p_Estado;
        public String p_TipoSolicitud;
    }
    public static class GenerarArchivoParticipes_Salida{
	    public List<String> 		P_OUT_ARCHIVOS;
	    public List<MensajeSalida> 	P_OUT_MENSAJES;
	    public Long                 P_OUT_ERRORESCONTADOR;
	    public String 				P_OUT_NOMBRE_ARCHIVO;
		public String 				P_OUT_RUTA_ARCHIVO; 
	    public String               P_OUT_NOMBRE_ARCHIVO_LOG;
	    public String 				P_OUT_RUTA_ARCHIVO_LOG; 
	    public String 				P_OUT_COMENTARIO;

	    public GenerarArchivoParticipes_Salida() {
    	   this.P_OUT_ERRORESCONTADOR = 0L;
    	   this.P_OUT_ARCHIVOS = new ArrayList<String>();
    	   this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
    	   this.P_OUT_NOMBRE_ARCHIVO = ""; 
    	   this.P_OUT_RUTA_ARCHIVO = ""; 
    	   this.P_OUT_NOMBRE_ARCHIVO_LOG = ""; 
    	   this.P_OUT_RUTA_ARCHIVO_LOG = ""; 
    	   this.P_OUT_COMENTARIO = ""; 
    	  }

    public String toString() {
    return Utils.printJson(this, false);
    }
    }
    
	public static class  Participante {
			
			public String DocumentoIdentidad;
		    public String Dv;
		    public String TipoPersona;
		    public String NombreCompleto;
		    public String Nombres;
		    public String ApellidoPaterno;
		    public String ApellidoMaterno;
		    public String Calle;
		    public String Numero;
		    public Long CodigoCiudad;
		    public String Telefono;
		    public String Sexo;
		    public String EstadoCivil;
		    public String RegimenConyugal;
		    public String Nacionalidad;
		    public Date FechaNacimiento;
		    public String Constante1;
		    public String CodigoBanco;
		    public String CuentaCorriente;
		    public String ContratoCustodia;
		    public String Constante2;
		    public String CodigoAgente;
		    public Long CodigoComuna;
		    public String EstadoCivilHomologado;
		    public String NacionalidadHomologada;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
	public static class LogEjecucion{
		public Long IdInterfaz;
		public Long NumeroRegistros;
		public Long NumeroRegistrosCorrectos;
		public Long NumeroRegistrosIncorrectos;
	}
	 public static class Homologacion extends BcsPojo{ 
		  public Long  IdHomologacion;  
		  public String ValorOrigen;
		  public String TipoValor;
		  public String Entidad;
		  public String ValorDestino;
		  public String Descripcion; 
		  public String ValorOrigen2;
		  public String ValorOrigen3;
		  
		  public Homologacion() {
		   this.IdHomologacion = 0L;  
		   this.ValorOrigen = "";
		   this.TipoValor = "";
		   this.Entidad = "";
		   this.ValorDestino = "";
		   this.Descripcion = "";
		   this.ValorOrigen2 = "";
		   this.ValorOrigen3 = "";
		  }
		 }
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		GenerarArchivoParticipes_Entrada in = gson.fromJson(jin.toString(), GenerarArchivoParticipes_Entrada.class);
		GenerarArchivoParticipes_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	

	
	public GenerarArchivoParticipes_Salida logica(Connection con, GenerarArchivoParticipes_Entrada in) throws Exception {
		logger.info("Inicio de servicio GenerarArchivoParticipes");
		long startTime = System.currentTimeMillis();
		List<Participante> out = new ArrayList<Participante>();
		GenerarArchivoParticipes_Salida salida = new GenerarArchivoParticipes_Salida();
		String mensajeGeneracionArchivo = "ARCHIVO_GENERADO_EXITOSAMENTE";
		try {
			SQL _sql = new SQL();		
			String nombreArchivo = generarNombreArchivo(in.p_TipoSolicitud);
			
			HashMap<String, Object> parmsIN = new HashMap<String, Object>();
			parmsIN.put("FechaCurse", in.p_FechaCurse);
			parmsIN.put("Estado", in.p_Estado);
			parmsIN.put("FondoMutuo", in.p_FondoMutuo);
			parmsIN.put("IdEmisor", in.p_Emisor);
			parmsIN.put("TipoSolicitud", in.p_TipoSolicitud);
			logger.info("INFO::GenerarArchivoParticipes :: INICIO - Select Obtener informacion participes");
			out = _sql.query(con, SQL_CONSULTA, parmsIN , Participante.class);
			logger.info("INFO::GenerarArchivoParticipes :: FIN - Select Obtener informacion participes");
			out.toString();
			
			parmsIN.clear();
			List<String> tiposValor = new ArrayList<String>(3);
			   for(String tipoValor : TIPOS_VALOR) {
			    tiposValor.add(tipoValor);
			   }
			   parmsIN.put("TipoValor", tiposValor);

			List<Homologacion> homologacion = _sql.query(con, SQL_SELECT_HOMOLOGACION,parmsIN, Homologacion.class);
			
			out = homologarParticipantes(out, homologacion);
			
			String rutaBaseSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
		    String rutaBaseFFMM = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "FONDOMUTUO", "RUTAARCHIVOSFFMM");
		   
		    salida.P_OUT_NOMBRE_ARCHIVO = nombreArchivo+ ".txt" ;//+ periodo + "." + entrada.p_FormatoArchivo; 
		    salida.P_OUT_RUTA_ARCHIVO = rutaBaseFFMM + salida.P_OUT_NOMBRE_ARCHIVO;//CARPETA_DEFAULT + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
		    salida.P_OUT_NOMBRE_ARCHIVO_LOG = nombreArchivo + ".log";//nombreArchivo + periodo + ".log";
		    salida.P_OUT_RUTA_ARCHIVO_LOG = rutaBaseFFMM + salida.P_OUT_NOMBRE_ARCHIVO_LOG;//CARPETA_DEFAULT + "/" + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
		    String rutaCompletaArchivo = rutaBaseSistema + rutaBaseFFMM + salida.P_OUT_NOMBRE_ARCHIVO;
		    String rutaCompletaArchivoLog = rutaBaseSistema + rutaBaseFFMM + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
		    //String rutaCompletaArchivo = "C:\\archivos\\"  + salida.P_OUT_NOMBRE_ARCHIVO; 			//para debug
			//String rutaCompletaArchivoLog = "C:\\archivos\\"  + salida.P_OUT_NOMBRE_ARCHIVO_LOG;		//para debug
			
			if (!generarArchivo(rutaCompletaArchivo,rutaCompletaArchivoLog, out, startTime)) {
				mensajeGeneracionArchivo = "ERROR_ARCHIVO_GENERADO";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_ARCHIVO_GENERADO",new HashMap<String, String>(0),con, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
	
			salida.P_OUT_COMENTARIO =  Diccionario.getMensaje(con, in.p_IdEmpresa, mensajeGeneracionArchivo).msg();

			logger.info("Fin de servicio GenerarArchivoParticipes");
		} catch (Exception e) {
		e.printStackTrace();
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_ARCHIVO_GENERADO",new HashMap<String, String>(0),con, in.p_IdEmpresa));
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		}

		return salida;
		}
		
	 public String generarNombreArchivo(String tipoSolicitud) throws Exception {
		  logger.info("INICIO::GenerarArchivoParticipes.generarNombreArchivo");
		  long inicio = System.currentTimeMillis();
		  String nombreArchivo = "";

		  if ("APO".equalsIgnoreCase(tipoSolicitud)) {
		   nombreArchivo = "APAFM";   
		  } else if ("RES".equalsIgnoreCase(tipoSolicitud)) {
		   nombreArchivo = "APRFM";   
		  } else {
		      throw new Exception("Valor inválido Tipo Solicitud: " + tipoSolicitud);
		  }
		  
		  long fin = System.currentTimeMillis();
		  logger.info("FIN::GenerarArchivoParticipes.generarNombreArchivo", fin - inicio);
		  return nombreArchivo;
		 }

		public boolean generarArchivo(String rutaCompletaArchivo, String rutaCompletaArchivoLog, List<Participante> Data, long startTime) throws Exception {
			logger.info("INICIO::GenerarArchivoParticipes.generarArchivo");
			BufferedWriter bwArchivo;
			BufferedWriter BwArchivoLog;


			Participante DP = null;
	
			try 
			{
				bwArchivo = new BufferedWriter(new FileWriter(rutaCompletaArchivo));
				BwArchivoLog = new BufferedWriter(new FileWriter(rutaCompletaArchivoLog));
				String mensajeErrores = "";
			    int numeroRegistros = 0;
				int numeroRegistrosErroneos = 0;
				int numeroRegistrosCorrecto = 0;
	
			    ListIterator<Participante> iter = Data.listIterator(Data.size());
	
			    while (iter.hasPrevious())
			    {
			        DP = iter.previous();
	
			        String doc = valorNoNulo(DP.DocumentoIdentidad);
			        String rut = "";
			        String dv = "";
	
			        if (doc.contains("-")) {
			            rut = doc.substring(0, doc.length() - 2);
			            dv = doc.substring(doc.length() - 1 );
			        } else if (doc.length() > 1) {
			            rut = doc.substring(0, doc.length() - 1);
			            dv = doc.substring(doc.length() - 1);
			        }
	
			        rut = rellenarIzquierda(rut, 9, '0');
	
			        String tipoPersona = valorNoNulo(DP.TipoPersona);
			        tipoPersona = tipoPersona.length() > 0 ? tipoPersona.substring(0,1) : "";
	
			        String nombre = "";
			        String apePat = "";
			        String apeMat = "";
	
			        if ("J".equals(tipoPersona)) {
			        	String completo = valorNoNulo(DP.NombreCompleto);
			            nombre = subcadena(completo, 0, 20);
			            apePat = subcadena(completo, 20, 40);
			            apeMat = subcadena(completo, 40, 60);
			        } else {
			            nombre = valorNoNulo(DP.Nombres);
			            apePat = valorNoNulo(DP.ApellidoPaterno);
			            apeMat = valorNoNulo(DP.ApellidoMaterno);
			        }
	
			        String direccion = (DP.Calle == null)
			            ? " "
			            : valorNoNulo(DP.Calle) + " " + valorNoNulo(DP.Numero);
	
			        String codCiudad = (DP.CodigoCiudad == null)
			            ? "0023"
			            : String.valueOf(DP.CodigoCiudad);
	
			        String telefono = formatearTelefono(DP.Telefono);
			        
			        String Sexo = valorNoNulo(DP.Sexo);
	
			        String estadoCivil = valorNoNulo(DP.EstadoCivilHomologado);
			        
			        String nacionalidad = valorNoNulo(DP.NacionalidadHomologada);
	
			        String fechaNac = "000000";
			        if (DP.FechaNacimiento != null) {
			            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
			            fechaNac = sdf.format(DP.FechaNacimiento);
			        }
			        String CodigoBanco = rellenarIzquierda(DP.CodigoBanco, 4, '0');
			        
			        String CuentaCorriente = rellenarIzquierda(limpiarCuentaCorriente(DP.CuentaCorriente), 15, '0');
	
			        String contratoCustodia = valorNoNulo(DP.ContratoCustodia);
			        
			        String CodigoAgente = valorNoNulo(DP.CodigoAgente);
			        
			        String CodigoComuna = String.valueOf(DP.CodigoComuna);
			        CodigoComuna = valorNoNulo(CodigoComuna);
			        
			        String registro = "";
	
			        registro += ajustarLongitudDerecha(rut, 9);
			        registro += ajustarLongitudDerecha(dv, 1);
			        registro += ajustarLongitudDerecha(tipoPersona, 1);
			        registro += ajustarLongitudDerecha(nombre, 20);
			        registro += ajustarLongitudDerecha(apePat, 20);
			        registro += ajustarLongitudDerecha(apeMat, 20);
			        registro += ajustarLongitudDerecha(direccion, 50);
			        registro += ajustarLongitudDerecha(codCiudad, 4);
			        registro += ajustarLongitudDerecha(telefono, 11);
			        registro += ajustarLongitudDerecha(Sexo, 1);
			        registro += ajustarLongitudDerecha(estadoCivil, 2);
			        registro += ajustarLongitudDerecha(nacionalidad, 2);
			        registro += ajustarLongitudDerecha(fechaNac, 6);
			        registro += ajustarLongitudDerecha(DP.Constante1, 2);
			        registro += ajustarLongitudDerecha(CodigoBanco, 4);
			        registro += ajustarLongitudDerecha(CuentaCorriente, 15);
			        registro += ajustarLongitudDerecha(contratoCustodia, 1);
			        registro += ajustarLongitudDerecha(DP.Constante2, 3);
			        registro += ajustarLongitudDerecha(CodigoAgente, 4);
			        registro += ajustarLongitudDerecha(CodigoComuna, 4);
	
	
			        numeroRegistros++;
			        if (registro.length() != LARGO_REGISTRO) {
			        	numeroRegistrosErroneos++;
			        	mensajeErrores += "Registro "+numeroRegistros+" con error de formato. \n";
			        } else {
			        	numeroRegistrosCorrecto++;
			        }
			        bwArchivo.write(registro);
		            if(numeroRegistros < Data.size()) {
		            	bwArchivo.newLine();
		            }
			    }
			    bwArchivo.close();
			  //LOG
			SimpleDateFormat fm=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String fecha = fm.format(new Date());
			long endTime = System.currentTimeMillis();
			String fechaFin = fm.format(new Date());
			String fechaInicio = "Fecha Hora Inicio:" + fecha;
			String fechaTermino = "Fecha Hora Termino:" + fechaFin;
			String tiempoEjecucion = "Tiempo de ejecución:" + (endTime - startTime);
			String CantidadRegistros = "Cantidad de registros:" + numeroRegistros;
			String CantidadCorrectos = "Cantidad de registros correctos:" + numeroRegistrosCorrecto;
			String CantidadErrores = "Cantidad de registros con errores: " + numeroRegistrosErroneos;

	
			BwArchivoLog.write(fechaInicio);
			BwArchivoLog.newLine();
			BwArchivoLog.write(fechaTermino);
			BwArchivoLog.newLine();
			BwArchivoLog.write(tiempoEjecucion);
			BwArchivoLog.newLine();
			BwArchivoLog.write(CantidadCorrectos);
			BwArchivoLog.newLine();
			BwArchivoLog.write(CantidadErrores);
			BwArchivoLog.newLine();
			BwArchivoLog.write(CantidadRegistros);
			BwArchivoLog.newLine();
			if(!mensajeErrores.isEmpty()) {
				BwArchivoLog.write(mensajeErrores);
				BwArchivoLog.newLine();
			}
			BwArchivoLog.write("Ruta Archivo Salida:" + rutaCompletaArchivo);
			BwArchivoLog.close();
	
		}
		catch (Exception e)
			{
				logger.error("Error en la creacion de Archivo de movimientos");
				logger.error(e.getMessage());
				return false;
			}
			logger.info("FIN::GenerarArchivoParticipes.generarArchivo");
		return true;
	}
		
		public List<Participante> homologarParticipantes(List<Participante> lista, List<Homologacion> homologaciones) {

		    logger.info("INICIO::GenerarArchivoParticipes.homologarParticipantes");

		    List<Homologacion> homEstadoCivil = homologaciones.stream().filter(h -> "CodigoEstadoCivil".equals(h.TipoValor)).collect(Collectors.toList());
		    List<Homologacion> homNacionalidad = homologaciones.stream().filter(h -> "CodigoNacionalidad".equals(h.TipoValor)).collect(Collectors.toList());

		    lista = lista.stream().map(dp -> {
				String est = valorNoNulo(dp.EstadoCivil);
				String reg = valorNoNulo(dp.RegimenConyugal);
				String nac = valorNoNulo(dp.Nacionalidad);
				
				String estadoCivilFinal = "  ";
				for (Homologacion h : homEstadoCivil) {
				    if (!est.equals(h.ValorOrigen)) continue;
				    if ("TODOS".equalsIgnoreCase(h.ValorOrigen2)) {
				        estadoCivilFinal = h.ValorDestino;
				        break;
				    }
				    if (reg.equals(h.ValorOrigen2)) {
				        estadoCivilFinal = h.ValorDestino;
				        break;
				    }
				}
				dp.EstadoCivilHomologado = estadoCivilFinal;
				
				String nacionalidadFinal = "  ";
				for (Homologacion h : homNacionalidad) {
					 if (nac.equalsIgnoreCase(h.ValorOrigen)) {
					     nacionalidadFinal = h.ValorDestino;
					     break;
					 }
				}
				if ("  ".equals(nacionalidadFinal)) {
				     for (Homologacion h : homNacionalidad) {
				         if ("OTROS".equalsIgnoreCase(h.ValorOrigen)) {
				             nacionalidadFinal = h.ValorDestino;
				             break;
				         }
				     }
				}
		        dp.NacionalidadHomologada = nacionalidadFinal;
		        return dp;
		    }).collect(Collectors.toList());

		    logger.info("FIN::GenerarArchivoParticipes.homologarParticipantes");
		    return lista;
		}

		private String valorNoNulo(String s) {
		    if (s == null) return "";
		    String limpio = s.trim();
		    if (limpio.equalsIgnoreCase("null")) {
		        return "";
		    }
		    return limpio;
		}

	private String rellenarIzquierda(String s, int length, char c) {
	    if (s == null) s = "";
	    if (s.length() >= length) return s.substring(0, length);
	    return String.format("%" + length + "s", s).replace(' ', c);
	}
	private String ajustarLongitudDerecha(String s, int length) {
	    if (s == null) s = "";
	    s = s.trim();
	    if (s.length() > length) {
	        return s.substring(0, length);
	    }
	    return String.format("%1$-" + length + "s", s);
	}
	private String formatearTelefono(String s) {
	    if (s == null) return "00000000000";
	    String limpio = s.replaceAll("[^0-9]", "");
	    if (limpio.length() > 9) {
	        limpio = limpio.substring(limpio.length() - 9);
	    }
	    limpio = "56" + limpio;
	    limpio = (limpio + "00000000000").substring(0, 11);

	    return limpio;
	}
	private String subcadena(String s, int start, int end) {
	    if (s == null || s.length() <= start) return "";
	    return s.substring(start, Math.min(end, s.length()));
	}
	public static String limpiarCuentaCorriente(String cuenta) {
	    if (cuenta == null) {
	        return "";
	    }
	    return cuenta.replaceAll("[^0-9]", "");
	}

	public static void main(String[] args) throws Exception {
		Connection con = null;
		GenerarArchivoParticipes demo = new GenerarArchivoParticipes();

		try  
		{
			con = PlataformaUtils.getPropertiesConnection("master02","connections.properties");
			GenerarArchivoParticipes_Entrada adf = new  GenerarArchivoParticipes_Entrada();
			adf.p_IdEmpresa = 1L;
			adf.p_IdModulo = "FFMM";
			adf.p_IdUsuario= 9784L;
			adf.p_FechaCurse = "2026-03-31";
			adf.p_FondoMutuo = "";
			adf.p_Emisor = "";
			adf.p_Estado = null;
			adf.p_TipoSolicitud = "APO";

			demo.logica(con, adf);
 			//con.commit();
 			
	    	}catch (Exception e){	
				System.out.println(e);
				con.rollback();
				} finally {		con.close();		}
		 	
		System.out.println("== END ==");
	}
	
}