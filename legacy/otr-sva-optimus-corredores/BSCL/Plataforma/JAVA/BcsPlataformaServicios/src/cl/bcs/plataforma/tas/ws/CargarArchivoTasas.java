package cl.bcs.plataforma.tas.ws;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class CargarArchivoTasas implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CargarArchivoTasas.class);
	
	public static class EntradaCargarArchivoTasas extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public String p_Fuente;
		public String p_Fecha;
		public String p_Oficializar;
		public List<Tasa> p_Tasas;
		
		public EntradaCargarArchivoTasas() {
			this.p_Tasas = new ArrayList<Tasa>();
		}
		
	}
	
	public static class Tasa extends BcsPojo{
		public String IdMoneda;
		public Long Periodo;
		public BigDecimal Tasa;
		public BigDecimal TasaEstimada;
	}
	
	public static class SalidaCargarArchivoTasas extends BcsPojo{
		public Long P_OUT_ID_PROGRAMA;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCargarArchivoTasas() {
			this.P_OUT_ID_PROGRAMA = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCargarArchivoTasas jin = gson.fromJson(in.toString(), EntradaCargarArchivoTasas.class);
		SalidaCargarArchivoTasas out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargarArchivoTasas logica(Connection con, EntradaCargarArchivoTasas entrada) throws Exception {
		logger.info("INICIO::CargarArchivoTasas");
		
		SalidaCargarArchivoTasas out = new SalidaCargarArchivoTasas();
		
		if(entrada.p_Fecha.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CARGA_TASAS_FECHA_ES_OBLIGATORIO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		AgregarPrograma.OutAgregarPrograma salidaAgendarCarga = agendarCarga(con, entrada);
		
		if(salidaAgendarCarga.P_OUT_ERRORESCONTADOR > 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AGENDAR_CARGA", salidaAgendarCarga.P_OUT_MENSAJE));
			return out;
		}
		
		out.P_OUT_ID_PROGRAMA = salidaAgendarCarga.P_OUT_IDPROGRAMA;
		HashMap<String,String> parametros = new HashMap<String,String>();
		parametros.put("IdPrograma", String.valueOf(out.P_OUT_ID_PROGRAMA));
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CARGA_TASAS_AGENDADA_CORRECTAMENTE",parametros,con,entrada.p_IdEmpresa));
		
		logger.info("FIN::CargarArchivoTasas");
		
		return out;
	}
	
	private AgregarPrograma.OutAgregarPrograma agendarCarga(Connection con, EntradaCargarArchivoTasas entrada) throws Exception {
		logger.trace("INICIO::CargarArchivoTasas.agendarCarga");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
		AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramIdUsuario = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramIdModulo = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramUsaReglasNegocio = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramSimularOperacion = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramFuente = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramFecha = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramOficializar = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramTasas = new AgregarPrograma.InAgregarPrograma.Parametro();
		
		paramIdEmpresa.NombreParametro = "IdEmpresa";
		paramIdEmpresa.Valor = entrada.p_IdEmpresa.toString();
		paramsParametros.add(paramIdEmpresa);
		
		paramIdUsuario.NombreParametro = "IdUsuario";
		paramIdUsuario.Valor = entrada.p_IdUsuario.toString();
		paramsParametros.add(paramIdUsuario);
		
		paramIdModulo.NombreParametro = "IdModulo";
		paramIdModulo.Valor = entrada.p_IdModulo.toString();
		paramsParametros.add(paramIdModulo);
		
		paramUsaReglasNegocio.NombreParametro = "UsaReglasNegocio";
		paramUsaReglasNegocio.Valor = entrada.p_UsaReglasNegocio.toString();
		paramsParametros.add(paramUsaReglasNegocio);
		
		paramSimularOperacion.NombreParametro = "SimularOperacion";
		paramSimularOperacion.Valor = entrada.p_SimularOperacion.toString();
		paramsParametros.add(paramSimularOperacion);
		
		paramFuente.NombreParametro = "Fuente";
		paramFuente.Valor = entrada.p_Fuente.toString();
		paramsParametros.add(paramFuente);
		
		paramFecha.NombreParametro = "Fecha";
		paramFecha.Valor = entrada.p_Fecha.toString();
		paramsParametros.add(paramFecha);
		
		paramOficializar.NombreParametro = "Oficializar";
		paramOficializar.Valor = entrada.p_Oficializar.toString();
		paramsParametros.add(paramOficializar);
		
		paramTasas.NombreParametro = "Tasas";
		paramTasas.Valor = "";
		paramTasas.ValorBlob = entrada.toString();
		paramsParametros.add(paramTasas);
		
		AgregarPrograma.InAgregarPrograma paramsAgregarPrograma = new AgregarPrograma.InAgregarPrograma(); 
		paramsAgregarPrograma.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsAgregarPrograma.p_IdModulo = entrada.p_IdModulo;
		paramsAgregarPrograma.p_IdUsuario = entrada.p_IdUsuario;
		paramsAgregarPrograma.p_IdTipoPrograma	= "CARGA_ARCHIVO_TASAS";
		paramsAgregarPrograma.p_Segundos = 0L;
		paramsAgregarPrograma.p_FechaSistema = fechaSistema;
		paramsAgregarPrograma.p_Parametros = paramsParametros;
		AgregarPrograma agregarPrograma = new AgregarPrograma();
		AgregarPrograma.OutAgregarPrograma salidaAgregarPrograma = agregarPrograma.logica(con, paramsAgregarPrograma);
		
		logger.trace("FIN::CargarArchivoTasas.agendarCarga");
		return salidaAgregarPrograma;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"S\",\"p_SimularOperacion\":\"N\",\"p_Fuente\":\"ML_FINANCE\",\"p_Fecha\":\"2022-10-19\",\"p_Oficializar\":\"S\",\"p_Tasas\":[{\"IdMoneda\":\"USD\",\"Periodo\":7,\"Tasa\":30,\"TasaEstimada\":31}]}";
		EntradaCargarArchivoTasas Objin = gson.fromJson(json, EntradaCargarArchivoTasas.class);
		CargarArchivoTasas demo = new CargarArchivoTasas();				
		try {
			SalidaCargarArchivoTasas ObjOut = demo.logica(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	};
		System.out.println("== END ==");
	}

}
