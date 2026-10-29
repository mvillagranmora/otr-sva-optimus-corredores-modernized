package cl.bcs.tesoreria;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.OutAgregarPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.ProcesarDistribucion.EntradaRecaudacionTraspaso;
import cl.bcs.tesoreria.ProcesarDistribucion.SalidaRecaudacionTraspaso;
import cl.bcs.tesoreria.pojo.Traspaso.RegistroRecaudacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearCargaMasivaPacRen implements JavaCall {

	private final Log log = new Log("TES.CrearCargaMasivaPacRen");
	
	private final String SQL_INSERT_RENDICION_PAC = loadResourceToString("cl/bcs/tesoreria/sql/CrearCargaMasivaPacRen_InsertRendicionPac.sql");
	private final String SQL_UPDATE_ESTADO_ARCHIVO_PAC = loadResourceToString("cl/bcs/tesoreria/sql/CrearCargaMasivaPac_UpdateEstadoUniversoPac.sql");
	

	// --AES--
	/******************************************************************************
	 * CLASES
	 ******************************************************************************/
	public static class EntradaCargaMasivaPacRen {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaCarga;
		public String p_simular;
		public List<DetalleCargaMasiva> p_CargaMasiva;
		public List<DetalleTransanccionesRen> p_CargaMasivaDetalle;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DetalleCargaMasiva {
		public Long IdInterno;
		public String NombreArchivo;
		public String CodigoTipoCargaMasiva;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DetalleTransanccionesRen {
		public Long IdCargaMasivaDetalle;
		public Long IdCargaMasivaPac;
		public String CodigoBanco;
		public String CodigoEmpresa;
		public String CodigoConvenio;
		public String TipoRegistro;
		public String IdentServicio;
		public String InfAdicional;
		public String MontoCargo;
		public String FechaVencimiento;
		public String FechaCargo;
		public String EstadoCargo;
		public String CodigoRechazo;
		public String EstadoCSC;
		public String EstadoSAAPV;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DetalleTransanccionesUni2 {

		public String IdentServicio;
		public String tipo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCargaMasivaPac {
		
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<RegistroRecaudacion> P_OUT_SOLICITUDES;
		
		public SalidaCargaMasivaPac() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_SOLICITUDES = new ArrayList<RegistroRecaudacion>();

		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	/******************************************************************************
	 * OBJETO DE TRANSPORTE
	 ******************************************************************************/
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCargaMasivaPacRen in = gson.fromJson(jin.toString(), EntradaCargaMasivaPacRen.class);

		SalidaCargaMasivaPac out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/******************************************************************************
	 * LOGICA
	 * 
	 * @throws Exception
	 ******************************************************************************/
	public SalidaCargaMasivaPac logica(Connection conn, EntradaCargaMasivaPacRen entrada) throws Exception {

		SQL sql = new SQL();
		
		SalidaCargaMasivaPac salida 	= 	new SalidaCargaMasivaPac();
		salida.P_OUT_ERRORESCONTADOR 	= 	0L;
		
		List<DetalleTransanccionesRen> paramInsertMensajeTransaccionPacR 	= 	new ArrayList<DetalleTransanccionesRen>();
		List<DetalleTransanccionesUni2> parametrosCargaMasivaDetallePAC 	= 	new ArrayList<DetalleTransanccionesUni2>();
		DetalleTransanccionesUni2 _mensajeTransaccionP2 					= 	new DetalleTransanccionesUni2();
		_mensajeTransaccionP2.tipo 											= 	"REN";
		parametrosCargaMasivaDetallePAC.add(_mensajeTransaccionP2);
		
		Long contador 	= 	(long) 225;
		
		try {
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			sql.batch(conn, SQL_UPDATE_ESTADO_ARCHIVO_PAC, parametrosCargaMasivaDetallePAC, DetalleTransanccionesUni2.class);


			for (DetalleTransanccionesRen cargaRen : entrada.p_CargaMasivaDetalle) {
				if(cargaRen.TipoRegistro.equalsIgnoreCase("D")){
					DetalleTransanccionesRen _mensajeTransaccionR 	= 	new DetalleTransanccionesRen();
	
					_mensajeTransaccionR.IdCargaMasivaDetalle 	= 	(long) 225; // contador;
					_mensajeTransaccionR.IdCargaMasivaPac 		= 	contador;
					_mensajeTransaccionR.CodigoBanco 			= 	cargaRen.CodigoBanco;
					_mensajeTransaccionR.CodigoEmpresa 			= 	cargaRen.CodigoEmpresa;
					_mensajeTransaccionR.CodigoConvenio 		= 	cargaRen.CodigoConvenio;
					_mensajeTransaccionR.TipoRegistro 			= 	cargaRen.TipoRegistro;
					_mensajeTransaccionR.IdentServicio 			= 	cargaRen.IdentServicio.replace(" ", "");
					_mensajeTransaccionR.InfAdicional 			= 	cargaRen.InfAdicional;
					_mensajeTransaccionR.MontoCargo 			= 	cargaRen.MontoCargo;
					_mensajeTransaccionR.FechaVencimiento 		= 	cargaRen.FechaVencimiento;
					_mensajeTransaccionR.FechaCargo 			= 	cargaRen.FechaCargo;
					_mensajeTransaccionR.EstadoCargo 			= 	cargaRen.EstadoCargo;
					_mensajeTransaccionR.CodigoRechazo 			= 	cargaRen.CodigoRechazo;
					
					paramInsertMensajeTransaccionPacR.add(_mensajeTransaccionR);
	
					
					contador++;
				}
			} 
			
			sql.batch(conn, SQL_INSERT_RENDICION_PAC, paramInsertMensajeTransaccionPacR, DetalleTransanccionesRen.class);
			
			if(entrada.p_simular.equalsIgnoreCase("S")){
				EntradaRecaudacionTraspaso paramntrada = new EntradaRecaudacionTraspaso();
				paramntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				paramntrada.p_IdModulo = entrada.p_IdModulo;
				paramntrada.p_IdUsuario = entrada.p_IdUsuario;
				paramntrada.p_simular = entrada.p_simular;
				
				Long contador1 = (long) 1;
				long contadorErrores = 0l;
				
				ProcesarDistribucion procesoTrasCtaBca = new ProcesarDistribucion();
				log.write("ProcesoTrasCtaBca: ");
				
				SalidaRecaudacionTraspaso OutTrasCtaBca 	= 	procesoTrasCtaBca.ValidaPreRendicion(conn, paramntrada);
				for (MensajeSalida filaMensaje : OutTrasCtaBca.P_OUT_MENSAJES) {
					MensajeSalida msg = new MensajeSalida();
					
					msg.id 			= 	contador;
					msg.tipo 		= 	filaMensaje.tipo;
					msg.codigo 		= 	filaMensaje.codigo;
					msg.descripcion = 	filaMensaje.descripcion;
					
					salida.P_OUT_MENSAJES.add(msg);
					contador1++;
				}
				
				salida.P_OUT_ERRORESCONTADOR 	= contadorErrores;
				salida.P_OUT_SOLICITUDES.addAll(OutTrasCtaBca.P_OUT_SOLICITUDES_NO_PROCESADAS);
				salida.P_OUT_SOLICITUDES.addAll(OutTrasCtaBca.P_OUT_SOLICITUDES_PROCESADAS);
			}
			else {
				
				AgregarPrograma agregarPrograma = new AgregarPrograma();
				AgregarPrograma.InAgregarPrograma inAgregarPrograma = new AgregarPrograma.InAgregarPrograma();
				
				inAgregarPrograma.p_IdEmpresa 	= entrada.p_IdEmpresa;
				inAgregarPrograma.p_IdUsuario 	= entrada.p_IdUsuario;
				inAgregarPrograma.p_IdModulo 	= entrada.p_IdModulo;
				inAgregarPrograma.p_FechaSistema = fechaSistema;
				inAgregarPrograma.p_Segundos 	= 0L;
				inAgregarPrograma.p_IdTipoPrograma = "RENDICION_PAC";
				
				AgregarPrograma.InAgregarPrograma.Parametro parametro = new AgregarPrograma.InAgregarPrograma.Parametro();
				parametro.NombreParametro 	= "IdEmpresa";
				parametro.Valor 			= entrada.p_IdEmpresa.toString();
				inAgregarPrograma.p_Parametros.add(parametro);
				
				parametro = new AgregarPrograma.InAgregarPrograma.Parametro();
				parametro.NombreParametro 	= "IdUsuario";
				parametro.Valor 			= entrada.p_IdUsuario.toString();
				inAgregarPrograma.p_Parametros.add(parametro);
				
				parametro = new AgregarPrograma.InAgregarPrograma.Parametro();
				parametro.NombreParametro 	= "Simular";
				parametro.Valor 			= entrada.p_simular.toString();
				inAgregarPrograma.p_Parametros.add(parametro);
				
				OutAgregarPrograma out = agregarPrograma.logica(conn, inAgregarPrograma);
				
				MensajeSalida msg = new MensajeSalida();
				msg.id 			= 	contador;
				msg.tipo 		= 	MensajeSalida.MENSAJE_INFORMACION;
				msg.codigo 		= 	MensajeSalida.MENSAJE_INFORMACION;
				msg.descripcion =   "Proceso agendado OK, revisar Consulta Programas, ID " + out.P_OUT_IDPROGRAMA;					
				
				salida.P_OUT_MENSAJES.add(msg);
			
			}
		} catch (Exception e) {
			log.write(e);
			throw e;
		}
		return salida;
	}

	/******************************************************************************
	 * MAIN
	 ******************************************************************************/
	public static void main(String[] args) {
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":150,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCarga\":\"2019-10-16\",\"p_CargaMasiva\":[{\"IdInterno\":1,\"NombreArchivo\":\"ARCHIVO RENDICION 13-11_CorrectoPre.txt\",\"CodigoTipoCargaMasiva\":\"CARGA_RENDICION_PAC\"}],\"p_CargaMasivaDetalle\":[{\"CodigoBanco\":\"001\",\"CodigoEmpresa\":\"006\",\"CodigoConvenio\":\"023\",\"TipoRegistro\":\"D\",\"IdentServicio\":\"00011221121210140     \",\"Filler1\":\"\",\"MontoCargo\":\"250000\",\"FechaVencimiento\":\"20191014\",\"FechaCargo\":\"20191105\",\"EstadoCargo\":\"3\",\"CodigoRechazo\":\"00\",\"InfAdicional\":\"0         \",\"Filler\":\"\"}],\"p_simular\":\"N\"}";

			EntradaCargaMasivaPacRen entrada = gson.fromJson(json, EntradaCargaMasivaPacRen.class);
			CrearCargaMasivaPacRen demo = new CrearCargaMasivaPacRen();

			SalidaCargaMasivaPac salida = demo.logica(con, entrada);

			System.out.println("salida --> " + salida.toString());
			//con.commit();
			con.rollback();

		} catch (Exception e) {
			e.printStackTrace();
		} 

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
