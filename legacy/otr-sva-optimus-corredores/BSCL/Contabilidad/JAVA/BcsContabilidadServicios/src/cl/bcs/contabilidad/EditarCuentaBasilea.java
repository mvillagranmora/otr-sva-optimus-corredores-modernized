package cl.bcs.contabilidad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.EditarCuentaBasilea.SalidaEditarCuentaBasilea.ObjCuentaContable;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EditarCuentaBasilea  implements JavaCall {

	private final Logger log = LogManager.getLogger(EditarCuentaBasilea.class);
	
	private final String SQL_SELECT_CUENTA_CONTABLE_CLIENTE = loadResourceToString("cl/bcs/contabilidad/sql/EditarCuentaBasilea_SelectCuentaContableCliente.sql");
	private final String SQL_SELECT_CUENTA_CONTABLE_CLIENTE_ACTUAL = loadResourceToString("cl/bcs/contabilidad/sql/EditarCuentaBasilea_SelectCuentaContableClienteActual.sql");
	private final String SQL_UPDATE_CUENTA_BASILEA = loadResourceToString("cl/bcs/contabilidad/sql/EditarCuentaBasilea_UpdateCuentaBasilea.sql");
	
	public static class EntradaEditarCuentaBasilea extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Accion;
		public String p_Concepto;
		public String p_TipoCuentaContable;
		public String p_CuentaContable;
		public String p_NombreCuentaContable;

	}
	

	public static class SalidaEditarCuentaBasilea extends cl.bcs.utils.BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public ObjCuentaContable CuentaContable;

		public SalidaEditarCuentaBasilea() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}

		public static class ObjCuentaContable extends BcsPojo {
			public Long IdUsuario;
			public String CuentaContable;
			public String NombreCuentaContable;
			public String Concepto;
			public String TipoCuentaContable;
			public String FechaSistema;
		}

		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaEditarCuentaBasilea jin = gson.fromJson(in.toString(), EntradaEditarCuentaBasilea.class);
		SalidaEditarCuentaBasilea out = logica(conn, jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaEditarCuentaBasilea logica(Connection con, EntradaEditarCuentaBasilea in) throws Exception {
		log.info("INICIO EditarCuentaBasilea");
		SalidaEditarCuentaBasilea out = new SalidaEditarCuentaBasilea();

		if ("GUARDAR".equals(in.p_Accion)) {
			guardarCuentaContable(con, in, out);
		} else {
			out.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		log.info("Fin EditarCuentaBasilea");
		return out;
	}

	private void guardarCuentaContable(Connection con, EntradaEditarCuentaBasilea in, SalidaEditarCuentaBasilea out) throws Exception {
		SQL sql = new SQL();
		HashMap<String, Object> parametros = new HashMap<>(2);
		parametros.put("CuentaContabilidad", in.p_CuentaContable);
		String 	fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		log.debug("Consultar Cuenta Contable Homologada");
		List<ObjCuentaContable> cuentaContableList = sql.query(con, SQL_SELECT_CUENTA_CONTABLE_CLIENTE, parametros, ObjCuentaContable.class);
		parametros.clear();
		parametros.put("IdConcepto", in.p_Concepto);
		parametros.put("TipoCuentaContable", in.p_TipoCuentaContable);
		List<ObjCuentaContable> cuentaContableActual = sql.query(con, SQL_SELECT_CUENTA_CONTABLE_CLIENTE_ACTUAL, parametros, ObjCuentaContable.class);
		log.debug("Fin de Consulta Cuenta Contable Homologada");
		cuentaContableList = cuentaContableList.stream().map(cc -> {
			cc.Concepto = in.p_Concepto;
			cc.IdUsuario = in.p_IdUsuario;
			cc.TipoCuentaContable = in.p_TipoCuentaContable;
			cc.FechaSistema = fechaSistema;
			return cc;
		}).collect(Collectors.toList());
		
		if (validaCuentaContable(con, in, out, cuentaContableList)) {
			log.debug("Inicio actualizacion cuenta contable basiela");
			sql.batch(con, SQL_UPDATE_CUENTA_BASILEA, cuentaContableList, ObjCuentaContable.class);
			log.debug("fin actualizacion cuenta contable basiela");
			out.CuentaContable = cuentaContableList.get(0);
			InAgregarLog entradaAgregarLog = new InAgregarLog();
			AtomicLong contador = new AtomicLong();
			String mensaje = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_MODIFICACION_INGRESO_EXITO")
					 .msg();
			entradaAgregarLog.agregarLogBasico(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_IdUsuario, contador.get(), "INFORMATIVO", "EDITAR CUENTA CONTABLE BASILEA", fechaSistema, mensaje);
			entradaAgregarLog.agregarDetalleLogBasico(in.p_IdUsuario, contador.getAndIncrement(), "CuentaContable", cuentaContableActual.get(0).CuentaContable, out.CuentaContable.CuentaContable);
			entradaAgregarLog.agregarDetalleLogBasico(in.p_IdUsuario, contador.getAndIncrement(), "NombreCuentaContable", cuentaContableActual.get(0).NombreCuentaContable, out.CuentaContable.NombreCuentaContable);
			AgregarLog agregarLog = new AgregarLog();
			agregarLog.logica(con, entradaAgregarLog);
		}
	}
	
	private boolean validaCuentaContable(Connection con, EntradaEditarCuentaBasilea in, SalidaEditarCuentaBasilea out,
			List<ObjCuentaContable> cuentaContableList) throws Exception {
		HashMap<String, String> error = new HashMap<>(1);
		
		if (cuentaContableList.size() == 0) {
			error.put("CuentaContable", in.p_CuentaContable);
			out.P_OUT_ERRORESCONTADOR += 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_EXISTE_CUENTA_CONTABLE", error, con, in.p_IdEmpresa));
			return false;
		}
		
		if (cuentaContableList.size() > 1) {
			error.put("CuentaContable", in.p_CuentaContable);
			out.P_OUT_ERRORESCONTADOR += 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_EDITAR_CUENTA_CONTABLE_BASILEA", error, con, in.p_IdEmpresa));
			return false;
		}

		ObjCuentaContable cuentaContable = cuentaContableList.get(0);

		if ("NO HOMOLOGADO".equals(cuentaContable.NombreCuentaContable)) {
			error.put("CuentaContable", in.p_CuentaContable);
			out.P_OUT_ERRORESCONTADOR += 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
					"NO_EXISTE_CUENTA_CONTABLE_HOMOLOGADA", error, con, in.p_IdEmpresa));
			return false;
		}

		if ("GUARDAR".equals(in.p_Accion) && !in.p_NombreCuentaContable.equals(cuentaContable.NombreCuentaContable)) {
			error.put("CuentaContable", in.p_CuentaContable);
			out.P_OUT_ERRORESCONTADOR += 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CUENTA_CONTABLE_ERROR_NOMBRE", error, con, in.p_IdEmpresa));
			return false;
		}

		return true;
	}

	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02",
				"C:\\optimus-corredores\\connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CON\",\"p_Accion\":\"GUARDAR\",\"p_Concepto\":\"CAPITAL VALOR A MERCADO\",\"p_TipoCuentaContable\":\"TÍTULOS VENTA CORTA (PASIVO)\",\"p_CuentaContable\":\"110240910000\",\"p_NombreCuentaContable\":\"CUOTAS FONDOS DE INVERSION\"}";
				EntradaEditarCuentaBasilea Objin = gson.fromJson(json, EntradaEditarCuentaBasilea.class);
				EditarCuentaBasilea ecb = new EditarCuentaBasilea();
				System.out.println(ecb.logica(con, Objin));
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception er) {
			er.printStackTrace();
		}
	}

}
