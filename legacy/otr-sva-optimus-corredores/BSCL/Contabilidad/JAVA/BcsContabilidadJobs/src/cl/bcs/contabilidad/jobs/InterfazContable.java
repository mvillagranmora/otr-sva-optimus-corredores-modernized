package cl.bcs.contabilidad.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.Gson;

import cl.bcs.contabilidad.GenerarJsonAsientoSap;
import cl.bcs.contabilidad.GenerarJsonAsientoSap.GenerarJsonAsientoSap_Entrada;
import cl.bcs.contabilidad.GenerarJsonAsientoSap.GenerarJsonAsientoSap_Salida;
import cl.bcs.contabilidad.pojo.BusinessPartner;
import cl.bcs.contabilidad.pojo.LoginSAP;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class InterfazContable extends BcsJob {
	
	private final Logger log = LogManager.getLogger(InterfazContable.class);
	
	private boolean DEBUG = false;
	private final String UPDATE_PRECOMPROBANTES = "UPDATE \"BSCL.Contabilidad.BD::ITZ.PreComprobante\" SET \"Estado\" = 'E' WHERE \"IdPreComprobante\" = :IdPreComprobante";
	private final String SELECT_MENSAJES = "SELECT \"Codigo\", \"Valor\", \"Mensaje\" FROM \"BSCL.Contabilidad.BD::ITZ.HomologaRespuestaSap\"";

	@Override
	public boolean transaction(Connection conn) throws Exception  {
		final long idEmpresa =Long.parseLong(this.getParam("IdEmpresa"));//1;//
		final long idUsuario = Long.parseLong(this.getParam("IdUsuario"));//1;//
		final String comprobante = this.getParam("SecCmbte");
		final String destino  = this.getParam("Destino").toString();
		
		return logica(conn, idEmpresa, idUsuario, comprobante, destino);
	}
	public static class PreComprobante{
		public Long IdPreComprobante;
		public PreComprobante() {
			
		}
		public PreComprobante(Long idSecuencia) {
			this.IdPreComprobante = idSecuencia;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class HomologaMensaje{
		public Long Codigo;
		public String Valor;
		public String Mensaje;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public boolean logica(Connection conn, long idEmpresa, long idUsuario, String comprobante, String destino) throws Exception  {
		log.info("INICIO InterfazContable");
		try {
			SQL sql = new SQL();
		    String archivo = "ITZ/"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+ "_" + "InterfazContable.log";
		    String rutaArchivo = ((!DEBUG) ? Utils.getConfiguracion(conn, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos") : "./") + archivo;
		    final File file = new File(rutaArchivo);
		    
			//parse columns base64 to string utf-8.
			String secCmbte = new String(Base64.getDecoder().decode(comprobante), "UTF-8");
			List<Long> secuencias =  Arrays.asList(secCmbte.split(";")).stream().map(sec -> Long.parseLong(sec)).collect(Collectors.toList());
        	
			//Crear json para login
			Properties prop = new Properties();
            InputStream input = new FileInputStream("interfazSAP.properties");
            prop.load(input);
            String serverSAP = prop.getProperty("B1_SERVICE_LAYER_URL");//"https://BCSHANA1CD:50000";//
            String userSAP = prop.getProperty("B1_USER");//"marteaga";//
            String passSAP = prop.getProperty("B1_PASS");//"AccCloud9$Xx8D";//
			String companyDBSAP = prop.getProperty("B1_COMPANY");// "PRUEBAS_MODELO_CONTABLE";//
			
			LoginSAP loginSAP = new LoginSAP(userSAP, passSAP, companyDBSAP);
			List<String> cookiesLogin = new ArrayList<String>();
			List<PreComprobante> secuenciasError = new ArrayList<>();
						
			try (Connection sapConnection = Utils.getSAPConnection(prop.getProperty("HANA_SERVER"),prop.getProperty("HANA_PORT"),prop.getProperty("B1_COMPANY"),prop.getProperty("HANA_USER"),prop.getProperty("HANA_PASS"));){
				cookiesLogin = CallerWSJournalVoucher.DoLogin(serverSAP, loginSAP);
			} catch (Exception er){
					FileUtils.writeStringToFile(file, "ERROR AL ENVIAR PRECOMPROBANTE A SAP. MENSAJE: "+ Diccionario.getMensaje(conn,idEmpresa, "UI_ERROR_CONEXION_SAP", new HashMap<String, String>(0)) + "\n", "UTF-8", true);
					log.debug("EXCEPCION AL ENVIAR PRECOMPROBANTE A SAP: ", er);
				for(Long secuencia : secuencias) {
					secuenciasError.add(new PreComprobante(secuencia));
				}
			}
			
			HashMap<String,Object> params =  new HashMap<>(0);
			List<HomologaMensaje> listaHomologaMensaje = sql.query(conn, SELECT_MENSAJES, params, HomologaMensaje.class);
			//enviar uno a uno los precomprobantes
			if(secuenciasError.isEmpty()) {
				for(Long secuencia : secuencias) {
					try {
						//enviar a sap.
						procesarPreComprobante(idEmpresa, idUsuario, destino, secuencia, conn, cookiesLogin, serverSAP);
						FileUtils.writeStringToFile(file, "PRECOMPROBANTE ENVIADO EXISTOSAMENTE: "+secuencia, "UTF-8", true);
						log.debug("PRECOMPROBANTE ENVIADO EXISTOSAMENTE: "+secuencia);
					}catch(Exception ex) {
						log.debug("PRUEBA ENVIAR PRECOMPROBANTE A SAP: "+ secuencia + ", error:" + ex.toString(), ex.toString());
						Gson gson = Utils.getGsonBuilder();
						String mensajeExcepcionOriginal = ex.getMessage();
						
						try {
							Respuesta respuestaExcepcion = gson.fromJson(mensajeExcepcionOriginal,  Respuesta.class);
							
							int codigo = respuestaExcepcion.Codigo == 400 ? new JSONObject(respuestaExcepcion.Mensaje).getJSONObject("error").getInt("code") : respuestaExcepcion.Codigo;
							String valor = respuestaExcepcion.Codigo == 400 ? new JSONObject(respuestaExcepcion.Mensaje).getJSONObject("error").getJSONObject("message").getString("value") : "";
							
							String mensaje = listaHomologaMensaje.stream().filter(x -> x.Codigo == codigo && valor.contains(x.Valor)).map(x -> x.Mensaje).findFirst().orElse(respuestaExcepcion.Mensaje);
							
							if(codigo == -1000) {
								String inputString = new JSONObject(respuestaExcepcion.Mensaje).getJSONObject("error").getJSONObject("message").getString("value");
						        Pattern pattern = Pattern.compile("\u0027(.*?)\u0027");
						        Matcher matcher = pattern.matcher(inputString);
						        if(matcher.find()) {    
							        String valorExtraido = matcher.group(1);
							        mensaje = mensaje.replace("{nombreCampo}",  valorExtraido);
						        }
							}
							
							if(codigo == -5002) {
								String inputString = new JSONObject(respuestaExcepcion.Mensaje).getJSONObject("error").getJSONObject("message").getString("value");
								Pattern pattern = Pattern.compile("'(.*?)'");
						        Matcher matcher = pattern.matcher(inputString);
						        if(matcher.find()) {    
							        String valorExtraido = matcher.group(1);
							        mensaje = mensaje.replace("{cuentaContable}",  valorExtraido);
						        }
							}

							secuenciasError.add(new PreComprobante(secuencia));

							FileUtils.writeStringToFile(file, "ERROR AL ENVIAR PRECOMPROBANTE A SAP: "+ secuencia + ", Mensaje: "+ mensaje, "UTF-8", true);
							log.debug("EXCEPCION AL ENVIAR PRECOMPROBANTE A SAP: "+secuencia, ex);
						} catch (Exception err) {
							secuenciasError.add(new PreComprobante(secuencia));
							FileUtils.writeStringToFile(file, "ERROR AL ENVIAR PRECOMPROBANTE A SAP: "+ secuencia, "UTF-8", true);
							log.debug("ERROR AL CONTROLAR EXCEPCION AL ENVIAR PRECOMPROBANTE A SAP: "+ secuencia, err);
						}
					}
					
					FileUtils.writeStringToFile(file, "\n", true);
				}
			}
			
			if(!secuenciasError.isEmpty()) {
				//volver a dejar erroneos los precomprobantes que fallaron.
				sql.batch(conn, UPDATE_PRECOMPROBANTES, secuenciasError, PreComprobante.class);
			}
			
			updateRutaSalida(conn, archivo, this.getJobId());
		} catch (Exception e) {
			log.debug("EXCEPCION AL CONECTAR CON SAP",e);
			throw e;
		}
		
		log.info("TERMINO InterfazContable");
		return true;
	}
	
	private void procesarPreComprobante(Long idEmpresa, Long idUsuario, String destino, Long comprobante, Connection dbConnection, List<String> cookiesLogin, String serverSAP) throws Exception {
		//ENVIAMOS LOS COMPROBANTES DE FORMA UNITARIA
		GenerarJsonAsientoSap generarJsonAsientoSapService = new GenerarJsonAsientoSap();
		
		GenerarJsonAsientoSap_Entrada generarJsonAsientoSapEntrada = new GenerarJsonAsientoSap_Entrada();
		generarJsonAsientoSapEntrada.p_Destino = destino;
		generarJsonAsientoSapEntrada.p_IdEmpresa = idEmpresa;
		//ESTE JOB ES UNITARIO
		generarJsonAsientoSapEntrada.p_IdPreComprobantes.add(comprobante);
		generarJsonAsientoSapEntrada.p_IdUsuario = idUsuario;
		
		GenerarJsonAsientoSap_Salida resultado = generarJsonAsientoSapService.logica(dbConnection, generarJsonAsientoSapEntrada );
		
		//CREACION DE NUEVOS SOCIOS
		for (BusinessPartner socio : resultado.P_OUT_SOCIOS) {		
			Respuesta respuestaExisteClienteSap = CallerWSJournalVoucher.ExisteClienteSAP(serverSAP,socio.CardCode ,cookiesLogin);
			if (respuestaExisteClienteSap.Codigo == 404){
				log.debug("Socio " + socio.CardCode + " no existe, creando");
				Respuesta respuestaEnviarClienteSap = CallerWSJournalVoucher.EnviarClienteSAP(serverSAP, socio, cookiesLogin);
				if (respuestaEnviarClienteSap.Codigo != 201) {
					log.debug("Error al crear socio " + respuestaEnviarClienteSap.toString());
					Respuesta respuestaExcepcion = new Respuesta(respuestaExisteClienteSap.Codigo, "Error al crear socio " + respuestaEnviarClienteSap.toString());
					throw new Exception(respuestaExcepcion.toString());
				}
			}
			else if (respuestaExisteClienteSap.Codigo == 200){
				log.debug("Socio " + socio.CardCode + " existe");
			}
			else {
				log.debug("Error al verificar si existe socio " + socio.CardCode + " - " + respuestaExisteClienteSap.toString());
				Respuesta respuestaExcepcion = new Respuesta(respuestaExisteClienteSap.Codigo, "Error al verificar si existe socio " + socio.CardCode + " - " + respuestaExisteClienteSap.toString());
				throw new Exception(respuestaExcepcion.toString());
			}
		}
		
		log.debug("Enviando asiento " + resultado.P_OUT_VOUCHERS.get(0).toString());
		Respuesta respuestaEnviarAsientoSap = CallerWSJournalVoucher.EnviarAsientoSAP(serverSAP, resultado.P_OUT_VOUCHERS.get(0), cookiesLogin);
		log.debug("Respuesta SAP - " + respuestaEnviarAsientoSap.toString());
		
		//actualizar registros cuando ha sido enviado exitosamente a SAP.
		if(respuestaEnviarAsientoSap.Codigo == 204)
		{
			CallableStatement spActualizarEstado = null;
			
			final String callSpActualizarEstado = "CALL \"BSCL.Contabilidad.BD.Servicios::ITZ.ActualizarEstadoPreComprobante\" (?,?,?,?,?,?,?,?,?)";
			
			spActualizarEstado = dbConnection.prepareCall(callSpActualizarEstado);
			spActualizarEstado.setLong(1, idEmpresa);
			spActualizarEstado.setLong(2, idUsuario);
			spActualizarEstado.setString(3, "CON");
			spActualizarEstado.setString(4, "N");
			spActualizarEstado.setLong(5, comprobante);
			spActualizarEstado.registerOutParameter(6, java.sql.Types.INTEGER);//advertencias contador
			spActualizarEstado.registerOutParameter(7, java.sql.Types.INTEGER);//errores contador
			
			spActualizarEstado.execute();
			dbConnection.commit();
		}
		else {
			throw new Exception(respuestaEnviarAsientoSap.toString());
		}
	}
	
	@Override
	public void onError(Throwable t) {
		log.debug(":ASIENTO_SAP:  ****** ERROR  *******",t);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection conn =  PlataformaUtils.getPropertiesConnection("desa","connections.properties")){
			InterfazContable cb = new InterfazContable();
			final long idEmpresa = 1L;
			final long idUsuario = 14L;
			final String comprobante = "MzU5ODE=";
			final String destino  = "B";
			cb.DEBUG = true;
			boolean res = cb.logica(conn, idEmpresa, idUsuario, comprobante, destino);
			conn.rollback();
		}
	}

	
}
