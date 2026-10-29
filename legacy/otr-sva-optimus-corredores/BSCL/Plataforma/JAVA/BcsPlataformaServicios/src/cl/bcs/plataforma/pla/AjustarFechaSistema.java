package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;

import cl.bcs.plataforma.pla.AgregarDiccionario.EntradaDiccionario;
import cl.bcs.plataforma.pla.AgregarDiccionario.SalidaDiccionario;
import cl.bcs.plataforma.pla.AgregarDiccionario.ServicioSalidaError;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AjustarFechaSistema implements JavaCall{	
	private final Log log = new Log("PLA.HoraSistema");
	//QUERIES
	private final String SQL_AjustarFechaSistema_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/AjustarFechaSistema_Update.sql");
	private final String SQL_ModificarTokenSesion_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/ModificarTokenSesion_Update.sql");
	/*
	 * Clases IN/OUT
	 */
	public static class EntradaServicio {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_SimularOperacion;
		//
		public List<EntradaFechaSistema>	p_FechaSistema;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntradaFechaSistema {
		public String	FechaSistema;
		public Long		IdEmpresa;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServicioSalida {
		public Long								P_OUT_ERRORESCONTADOR;
		public Long								P_OUT_ADVERTENCIASCONTADOR;
		public List<ServicioSalidaAdvertencia>	P_OUT_ADVERTENCIADETALLE;
		public List<ServicioSalidaError>		P_OUT_ERRORESDETALLE;

		public ServicioSalida() {
			P_OUT_ADVERTENCIADETALLE = new ArrayList<>();
			P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServicioSalidaAdvertencia {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServicioSalidaError {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;

		public ServicioSalidaError() {
		}

		public ServicioSalidaError(String codigo, String mensaje) {
			this.Codigo = codigo;
			this.Descripcion = mensaje;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){		
        System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getDESAConnection();
            // ---------------------------------
            try {
                JsonParser parser = new JsonParser();
                
                /*ESTRUCTURA JSON
                EntradaServicio pp=new EntradaServicio();
                pp.p_FechaSistema=new ArrayList<>();
                pp.p_FechaSistema.add(new EntradaFechaSistema());
                System.out.println("->"+pp.toString());
                */

                JsonObject dataJsonIn = parser.parse("{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":'PLA',\"p_SimularOperacion\":'N',\"p_FechaSistema\":[{\"FechaSistema\":'2017-05-22',\"IdEmpresa\":1}]}").getAsJsonObject();
                AjustarFechaSistema demo = new AjustarFechaSistema();
                
                System.out.println(demo.call(con, dataJsonIn).toString());
                con.commit();
            } catch (Exception er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
	}

	/**********************
	 * Transporte
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();
		EntradaServicio in = gson.fromJson(jin.toString(), EntradaServicio.class);
		ServicioSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ServicioSalida logica(Connection con, EntradaServicio entrada) throws Exception {
		ServicioSalida salida = new ServicioSalida();
		salida.P_OUT_ERRORESCONTADOR = new Long(1);
		salida.P_OUT_ADVERTENCIASCONTADOR = 0L;
		SQL sql = new SQL();
		
		String fechaSistemaAnterior = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		//Agregar log
		AgregarLog agregarLog = new AgregarLog();
		InAgregarLog entradaLog = new InAgregarLog();
				
		//Cabecera log
		InAgregarLog.Log headLog = new InAgregarLog.Log();
		headLog.IdCabeceraLog = 1L;
		headLog.IdTipoLog = "INFORMATIVO";
		headLog.IdModulo = "PLA";
		headLog.IdUsuarioAtendido = entrada.p_IdUsuario;
		headLog.IdTipoRegistro = "CAMBIO_FECHA_SISTEMA";
		headLog.IdUsuario = entrada.p_IdUsuario;
		headLog.IdEmpresa = entrada.p_IdEmpresa;
		headLog.NombreProceso = "Ajustar Fecha Sistema";
		if(entrada.p_FechaSistema != null && !entrada.p_FechaSistema.isEmpty() && entrada.p_FechaSistema.get(0) != null) {
			headLog.FechaSistemaLog = entrada.p_FechaSistema.get(0).FechaSistema != null ? entrada.p_FechaSistema.get(0).FechaSistema : "";
		}
		entradaLog.p_Log.add(headLog);
				
		//Detalle log
		InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
		detLog.IdCabeceraLog = 1L;
		detLog.SubRegistro = "CAMBIO_FECHA_SISTEMA";
		detLog.CampoRegistro = "Fecha_Sistema";
		detLog.ValorAntiguo = fechaSistemaAnterior != null ? fechaSistemaAnterior : "";
		if(entrada.p_FechaSistema != null && !entrada.p_FechaSistema.isEmpty() && entrada.p_FechaSistema.get(0) != null) {
			detLog.ValorNuevo = entrada.p_FechaSistema.get(0).FechaSistema != null ? entrada.p_FechaSistema.get(0).FechaSistema : "";
		}
		entradaLog.p_LogDetalle.add(detLog);
				
		//llamar al log
		agregarLog.logica(con, entradaLog);
		
		//Actualiza Fecha del Sistema
		sql.batch(con, SQL_AjustarFechaSistema_Update, entrada.p_FechaSistema, EntradaFechaSistema.class);
		//Deshabilita token usuarios para volver a iniciar sesion con la nueva fecha
		sql.batch(con, SQL_ModificarTokenSesion_Update, entrada.p_FechaSistema, EntradaFechaSistema.class);
		//AVISO MASIVO A USUARIOS CONECTADOS
		AgregarMensajeWebsocket.agregar(con, entrada.p_IdEmpresa, "AVISO", "Se ha cambiado la fecha sistema a: " + entrada.p_FechaSistema.get(0).FechaSistema);
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		return salida;
	}	
}
