package cl.bcs.plataforma.frm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.frm.ConsultarCampoFormulario.ConsultarCampoFormulario_Entrada;
import cl.bcs.plataforma.frm.ConsultarCampoFormulario.ConsultarCampoFormulario_Salida;
import cl.bcs.plataforma.frm.pojo.CampoFormulario;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EditarCampoFormulario implements JavaCall {

	static final Logger logger = LogManager.getLogger(EditarCampoFormulario.class);
	
	private String SQL_SELECT_COUNT_CAMPO_FORMULARIO = loadResourceToString("cl/bcs/plataforma/frm/sql/ComunesCampoFormulario_SelectCountCampoFormulario.sql");
	private String SQL_UPDATE_CAMPO_FORMULARIO = loadResourceToString("cl/bcs/plataforma/frm/sql/EditarCampoFormulario_UpdateCampoFormulario.sql");
	
	public static class EntradaEditarCampoFormulario extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public String p_CodigoFormulario;
		public String p_CodigoCampo;
		public String p_IdTipoCanal;
		public Long p_CantidadMinima;
		public String p_Requerido;
		public String p_RegeneraContratos;
		public String p_ValorDefecto;
		public String p_ValorAntes;
		public String p_TipoCampoFrontend;
		
	}
	
	public static class Formulario extends BcsPojo {
		public String CodigoCampo;
		public String NombreFormulario;
		public String NombreCampo;
		public String NombreCanal;
		public String ValorDefectoCampoFormulario;
		public String Requerido;
		public String RegeneraContratos;		
	}
	
	public static class SalidaEditarCampoFormulario extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaEditarCampoFormulario() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class Validacion extends BcsPojo{
		public Boolean EsError;
		public MensajeSalida MensajeError;
		
		public Validacion() {
			this.EsError = false;
		}
	}
	

	public String codigoCampo= "";
	public String nombreFormulario= "";
	public String nombreCampo = "";
	public String nombreCanal = "";
	public String valorDefectoCampoFormulario = "";
	public String requerido = "";
	public String regeneraContratos = "";
	public String valorDespues = "";
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEditarCampoFormulario jin = gson.fromJson(in.toString(), EntradaEditarCampoFormulario.class);
		SalidaEditarCampoFormulario out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaEditarCampoFormulario logica(Connection con, EntradaEditarCampoFormulario entrada) throws Exception {
		logger.info("INICIO::EditarCampoFormulario");
		long inicioServicio = System.currentTimeMillis();
		SalidaEditarCampoFormulario out = new SalidaEditarCampoFormulario();
		
		Validacion validacionExistenciaCampoFormulario = validarExistenciaCampoFormulario(con, entrada);
		out = agregarValidacionSalida(out, validacionExistenciaCampoFormulario);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		actualizarCampoFormulario(con, entrada);
		agregarLogs(con, entrada);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EditarCampoFormulario", finServicio - inicioServicio);
		return out;
	}
	
	private Validacion validarExistenciaCampoFormulario(Connection con, EntradaEditarCampoFormulario entrada) throws Exception {
		logger.debug("INICIO::EditarCampoFormulario.validarExistenciaCampoFormulario");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(3);
		parametros.put("CodigoFormulario", entrada.p_CodigoFormulario);
		parametros.put("CodigoCampo", entrada.p_CodigoCampo);
		parametros.put("IdTipoCanal", entrada.p_IdTipoCanal);
		
		SQL sql = new SQL();
		List<Formulario> listaFormulario = new ArrayList <Formulario>();			
		listaFormulario = sql.query(con, SQL_SELECT_COUNT_CAMPO_FORMULARIO, parametros, Formulario.class);
		
		Validacion validacion = new Validacion();
		if(listaFormulario.size() == 0) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_CAMPO_FORMULARIO_NO_EXISTE", new HashMap<String,String>(), con, entrada.p_IdEmpresa);
		}
		else
		{
			codigoCampo = listaFormulario.get(0).CodigoCampo;
			nombreFormulario = listaFormulario.get(0).NombreFormulario;
			nombreCampo = listaFormulario.get(0).NombreCampo;
			nombreCanal = listaFormulario.get(0).NombreCanal;
			valorDefectoCampoFormulario = listaFormulario.get(0).ValorDefectoCampoFormulario;
			requerido = listaFormulario.get(0).Requerido;
			regeneraContratos = listaFormulario.get(0).RegeneraContratos;
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EditarCampoFormulario.validarExistenciaCampoFormulario", fin - inicio);
		return validacion;
	}
	
	private SalidaEditarCampoFormulario agregarValidacionSalida(SalidaEditarCampoFormulario salida, Validacion validacion) {
		logger.trace("INICIO::EditarCampoFormulario.agregarValidacionSalida");
		long inicio = System.currentTimeMillis();
		if(validacion.EsError) {
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EditarCampoFormulario.agregarValidacionSalida", fin - inicio);
		return salida;
	}
	
	private void actualizarCampoFormulario(Connection con, EntradaEditarCampoFormulario entrada) throws Exception {
		logger.debug("INICIO::EditarCampoFormulario.actualizarCampoFormulario");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		CampoFormulario campoFormulario = new CampoFormulario();
		campoFormulario.CodigoFormulario = entrada.p_CodigoFormulario;
		campoFormulario.CodigoCampo = entrada.p_CodigoCampo;
		campoFormulario.IdTipoCanal = entrada.p_IdTipoCanal;
		campoFormulario.CantidadMinima = entrada.p_CantidadMinima;
		campoFormulario.Requerido = entrada.p_Requerido;
		campoFormulario.RegeneraContratos = entrada.p_RegeneraContratos;
		campoFormulario.ValorDefecto = entrada.p_ValorDefecto;
		
		List<CampoFormulario> camposFormulario = Arrays.asList(campoFormulario);
		sql.batch(con, SQL_UPDATE_CAMPO_FORMULARIO, camposFormulario, CampoFormulario.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EditarCampoFormulario.actualizarCampoFormulario", fin - inicio);
	}
	
	private void agregarLogs(Connection conn, EntradaEditarCampoFormulario entrada) throws Exception {

		logger.debug("INICIO::EditarCampoFormulario.agregarLogs");
		long inicio = System.currentTimeMillis();

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa,
				"PLATAFORMA", "PLA.FechaSistema");
		
		ConsultarCampoFormulario consultarCampoFormulario = new ConsultarCampoFormulario();	
		ConsultarCampoFormulario_Entrada entradaconsultarCampoFormulario = new ConsultarCampoFormulario_Entrada();
		ConsultarCampoFormulario_Salida salidaentradaconsultarCampoFormulario = new ConsultarCampoFormulario_Salida();
		
		
		if (entrada.p_TipoCampoFrontend.equals("bcs-combobox-base")) {		
			entradaconsultarCampoFormulario.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaconsultarCampoFormulario.p_IdUsuario = entrada.p_IdUsuario;
			entradaconsultarCampoFormulario.p_IdModulo = entrada.p_IdModulo;
			entradaconsultarCampoFormulario.p_CodigoFormulario = entrada.p_CodigoFormulario;
			entradaconsultarCampoFormulario.p_CodigoCampo = entrada.p_CodigoCampo;
			entradaconsultarCampoFormulario.p_IdTipoCanal= entrada.p_IdTipoCanal;				
			salidaentradaconsultarCampoFormulario = consultarCampoFormulario.logica(conn,	entradaconsultarCampoFormulario);
	
			if (salidaentradaconsultarCampoFormulario.P_OUT_FORMULARIO.size()> 0) {
				valorDespues = salidaentradaconsultarCampoFormulario.P_OUT_FORMULARIO.get(0).NombreValorDefectoCampoFormulario;			
			}		
		}	

		if (!entrada.p_ValorDefecto.equals(valorDefectoCampoFormulario) || !entrada.p_Requerido.equals(requerido) || !entrada.p_RegeneraContratos.equals(regeneraContratos)) {
			
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			entradaAgregarLog.p_IdTrabajo = 0L;

			AgregarLog.InAgregarLog.Log inLog = new AgregarLog.InAgregarLog.Log();
			inLog.IdCabeceraLog = 1L;
			inLog.IdTipoLog = "MODIFICACION";
			inLog.IdModulo = entrada.p_IdModulo;
			inLog.IdUsuario = entrada.p_IdUsuario;
			inLog.IdEmpresa = entrada.p_IdEmpresa;
			inLog.FechaSistemaLog = fechaSistema;
			inLog.IdUsuarioAtendido = entrada.p_IdUsuario;
			inLog.IdCliente = 0L;
			inLog.EstadoCliente = "";
			inLog.NombreCliente = "";
			inLog.IdTipoDocumentoIdentidad = "";
			inLog.IdDocumentoIdentidad = "";
			inLog.IdPortafolio = 0L;
			inLog.EstadoPortafolio = "";
			inLog.IdTipoRegistro = "FORMULARIO";
			inLog.EstadoRegistro = "";
			inLog.IdTipoOperacion = "";
			inLog.Folio = 0L;
			inLog.Cantidad = BigDecimal.ZERO;
			inLog.PrecioTasa = BigDecimal.ZERO;
			inLog.IdMonedaOrigen = "";
			inLog.IdMonedaLiquidacion = "";
			inLog.TipoCambio = "";
			inLog.Nemo = "";
			inLog.IdProceso = 0L;
			inLog.EstadoProceso = "";
			inLog.NombreProceso = "";						
			entradaAgregarLog.p_Log.add(inLog);
			

			AgregarLog.InAgregarLog.DetalleLog transaccionDetalleLog1 = new AgregarLog.InAgregarLog.DetalleLog();
			AgregarLog.InAgregarLog.DetalleLog transaccionDetalleLog2 = new AgregarLog.InAgregarLog.DetalleLog();
			AgregarLog.InAgregarLog.DetalleLog transaccionDetalleLog3 = new AgregarLog.InAgregarLog.DetalleLog();
			
			if (!entrada.p_ValorDefecto.equals(valorDefectoCampoFormulario)) { 
				transaccionDetalleLog1.IdTrabajo = 0L;
				transaccionDetalleLog1.IdCabeceraLog = 1L;
				transaccionDetalleLog1.SubRegistro =  "CAMPO FORMULARIO";
				transaccionDetalleLog1.CampoRegistro = nombreFormulario + " - " + nombreCampo + " - (" + nombreCanal + ") : Valor Defecto";
				transaccionDetalleLog1.ValorAntiguo = "bcs-combobox-base".equals(entrada.p_TipoCampoFrontend) ? entrada.p_ValorAntes : valorDefectoCampoFormulario;
				transaccionDetalleLog1.ValorNuevo = "bcs-combobox-base".equals(entrada.p_TipoCampoFrontend) ? valorDespues :entrada.p_ValorDefecto;
				entradaAgregarLog.p_LogDetalle.add(transaccionDetalleLog1);
			}	
			
			if (!entrada.p_Requerido.equals(requerido)) {
				transaccionDetalleLog2.IdTrabajo = 0L;
				transaccionDetalleLog2.IdCabeceraLog = 1L;
				transaccionDetalleLog2.SubRegistro =  "CAMPO FORMULARIO";
				transaccionDetalleLog2.CampoRegistro = nombreFormulario + " - " + nombreCampo + " - (" + nombreCanal + ") : Es Requerido";
				transaccionDetalleLog2.ValorAntiguo = requerido;
				transaccionDetalleLog2.ValorNuevo = entrada.p_Requerido;
				entradaAgregarLog.p_LogDetalle.add(transaccionDetalleLog2);
			}

			if (!entrada.p_RegeneraContratos.equals(regeneraContratos)) {
				transaccionDetalleLog3.IdTrabajo = 0L;
				transaccionDetalleLog3.IdCabeceraLog = 1L;
				transaccionDetalleLog3.SubRegistro =  "CAMPO FORMULARIO";
				transaccionDetalleLog3.CampoRegistro = nombreFormulario + " - " + nombreCampo + " - (" + nombreCanal + ") : Regenera Contratos";
				transaccionDetalleLog3.ValorAntiguo = regeneraContratos;
				transaccionDetalleLog3.ValorNuevo = entrada.p_RegeneraContratos;
				entradaAgregarLog.p_LogDetalle.add(transaccionDetalleLog3);
			}
			
			AgregarLog agregarLog = new AgregarLog();
			agregarLog.logica(conn, entradaAgregarLog);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EditarCampoFormulario.agregarLogs {}", fin - inicio);
	}
	
}
