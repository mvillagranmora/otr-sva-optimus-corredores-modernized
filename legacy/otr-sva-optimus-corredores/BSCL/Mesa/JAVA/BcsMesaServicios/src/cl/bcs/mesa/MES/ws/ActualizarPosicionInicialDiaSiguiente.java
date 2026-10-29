package cl.bcs.mesa.MES.ws;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.crystaldecisions.reports.formulas.FieldExpression.Value;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.icu.text.DecimalFormat;

import cl.bcs.mesa.MES.ws.IngresoPreciosParidadesSpotWs.AdvertenciasDetalle;
import cl.bcs.mesa.MES.ws.IngresoPreciosParidadesSpotWs.ErrorDetalle;
import cl.bcs.mesa.MES.ws.IngresoPreciosParidadesSpotWs.PreciosParidadesSpot;
import cl.bcs.mesa.SPT.ConsultarPosicion;
import cl.bcs.mesa.SPT.ConsultarPosicion.EntradaConsultarPosicion;
import cl.bcs.mesa.SPT.ConsultarPosicion.Paridad;
import cl.bcs.mesa.SPT.ConsultarPosicion.SalidaConsultaPosicion;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.CrearCliente.Parametros;
import cl.bcs.tesoreria.LiquidarMovimientoAutomatico.IngresoEgreso_Entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarPosicionInicialDiaSiguiente  implements JavaCall {
	
	private final String SQL_INGRESAR_POSICION_INICIAL					= loadResourceToString("cl/bcs/mesa/MES/ws/sql/ActualizarPosicionInicialDiaSiguiente_ActualizarPosicionInicial.sql");
	private final String SQL_SELECT_POSICION_CIERRE 					= loadResourceToString("cl/bcs/mesa/MES/ws/sql/ActualizarPosicionInicialDiaSiguiente_SelectPosicionInicial.sql");
	private final String SQL_LOCALIZACION_SISTEMA						= loadResourceToString("cl/bcs/mesa/MES/ws/sql/ActualizarPosicionInicialDiaSiguiente_SelectPaisPlataforma.sql");
	private final String SQL_CREATE_TABLA_PARAMETROS_POSICION_INICIAL 	= loadResourceToString("cl/bcs/mesa/MES/ws/sql/ActualizarPosicionInicialDiaSiguiente_CrearTablaTemporalParametrosPosicion.sql");
	private final String SQL_INSERT_TABLA_PARAMETROS_POSICION_INICIAL 	= loadResourceToString("cl/bcs/mesa/MES/ws/sql/ActualizarPosicionInicialDiaSiguiente_InsertTablaTemporalParametrosPosicion.sql");
	private final String SQL_DROP_TABLA_PARAMETROS_POSICION_INICIAL 	= "DROP TABLE #ActualizarPosicionInicial_p_DatosPosicion";
	
	static final Logger logger = LogManager.getLogger(ActualizarPosicionInicialDiaSiguiente.class);

	public static class EntradaPosicionInicialDiaSiguiente{
		  public long p_IdEmpresa;
		  public String p_IdUsuario;
		  public String p_IdModulo;
		  public String p_IdMoneda;
		  
		  public EntradaPosicionInicialDiaSiguiente() {
			  this.p_IdEmpresa = 0L;
			  this.p_IdModulo = null;
			  this.p_IdUsuario = null;
			  this.p_IdMoneda = null;
			  }
	}
	
	public static class PosicionInicialDiaSiguiente extends BcsPojo {
		public BigDecimal IdPosicionInicial;
		public String Fecha;
		public String IdMoneda;
		public BigDecimal TipoCambioInicial;
		public BigDecimal CierreIngresado;
		public BigDecimal PosicionInicial;
		public String TipoInstrumento;
		public String Estado;
		public BigDecimal ResultadoPosicionInicial;		
		
		public PosicionInicialDiaSiguiente() {
			this.IdPosicionInicial = BigDecimal.ZERO;
			this.Fecha = null;
			this.IdMoneda = null;
			this.TipoCambioInicial = BigDecimal.ZERO;
			this.CierreIngresado = BigDecimal.ZERO;
			this.PosicionInicial = BigDecimal.ZERO;
			this.TipoInstrumento = null;
			this.Estado = null;
			this.ResultadoPosicionInicial = BigDecimal.ZERO;
		}
	}
	
	public static class InConfiguracion {
		public Long c_idPais;
		
		public InConfiguracion() {
			this.c_idPais = 1L;
		}
	}
	
	public static class ResultadoActualizarPosicionInicialDiaSiguiente {

		public String IdMoneda;
		public BigDecimal Compras; 
		public BigDecimal Ventas;
		public BigDecimal PosicionInicial;
		public BigDecimal Posicion;
		public BigDecimal Paridad;
		public BigDecimal EquivalenteUSD;
		
		public ResultadoActualizarPosicionInicialDiaSiguiente() {
			
			this.IdMoneda = null;
			this.Compras = BigDecimal.ZERO;
			this.Ventas = BigDecimal.ZERO;
			this.PosicionInicial = BigDecimal.ZERO;
			this.Posicion = BigDecimal.ZERO;
			this.Paridad = BigDecimal.ZERO;
			this.EquivalenteUSD = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaActualizarPosicionInicialDiaSiguiente extends BcsPojo {
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public Long								P_OUT_ADVERTENCIASCONTADOR;
		public String							DIA_ACTUALIZACION_POSICION;
		public List<AdvertenciasDetalle> 		P_OUT_ADVERTENCIASDETALLE;

		public SalidaActualizarPosicionInicialDiaSiguiente() {

			P_OUT_ERRORESCONTADOR		= 0L;
			P_OUT_ADVERTENCIASCONTADOR	= 0L;
			DIA_ACTUALIZACION_POSICION	= null;
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			P_OUT_ADVERTENCIASDETALLE 	= new ArrayList<AdvertenciasDetalle>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaPosicionInicialDiaSiguiente jin = gson.fromJson(in.toString(), EntradaPosicionInicialDiaSiguiente.class);
		SalidaActualizarPosicionInicialDiaSiguiente out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
	public SalidaActualizarPosicionInicialDiaSiguiente logica(Connection conn, EntradaPosicionInicialDiaSiguiente in) throws Exception {
		logger.info("INICIO: SalidaActualizarPosicionInicialDiaSiguiente");
		SalidaActualizarPosicionInicialDiaSiguiente Salida = new SalidaActualizarPosicionInicialDiaSiguiente();	
		
		try {
			SQL sql = new SQL();		
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			logger.debug("INICIO::SQL_LOCALIZACION_SISTEMA");
			List<InConfiguracion> idPaisConfiguracion;
			HashMap<String, Object> parametrosConfig = new HashMap<String, Object>();
			parametrosConfig.put("p_IdEmpresa", in.p_IdEmpresa);
			idPaisConfiguracion = sql.query(conn, SQL_LOCALIZACION_SISTEMA, parametrosConfig, InConfiguracion.class);
			logger.debug("FIN::SQL_LOCALIZACION_SISTEMA");
			
			List<PosicionInicialDiaSiguiente> EntradaPosicion = obtenerPosicionCierre(conn, sql, in, fechaSistema, idPaisConfiguracion.get(0).c_idPais);

			logger.debug("INICIO::ObtenerDiaSiguiente");
			ObtenerDiaSiguiente.InObtenerDiaSiguiente entradaDia = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
			ObtenerDiaSiguiente.OutObtenerDiaSiguiente DiaSiguiente = new ObtenerDiaSiguiente.OutObtenerDiaSiguiente();
			
			FechasIn fechaActual = new FechasIn();
			fechaActual.fechaInicial = fechaSistema;
			fechaActual.numeroDias = 1L;
			fechaActual.pais = idPaisConfiguracion.get(0).c_idPais;
			
			List<FechasIn> fechasIn = new ArrayList<FechasIn>();
			fechasIn.add(fechaActual);

			entradaDia.p_diaHabil = "N";
			entradaDia.p_soloCaeDiaHabil = "S";
			entradaDia.p_idPais = idPaisConfiguracion.get(0).c_idPais;
			entradaDia.p_fechas = fechasIn;
			
			ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
			DiaSiguiente = obtenerDiaSiguiente.logica(conn, entradaDia);
			String diaSiguiente = DiaSiguiente.p_out_fechas.get(0).fechaCalculada;
			EntradaPosicion.get(0).Fecha = diaSiguiente;
			logger.debug("FIN::ObtenerDiaSiguiente");

			logger.debug("INICIO::SQL_INGRESAR_POSICION_INICIAL");				

			SQL.exec(conn, SQL_CREATE_TABLA_PARAMETROS_POSICION_INICIAL);	
			sql.batch(conn,SQL_INSERT_TABLA_PARAMETROS_POSICION_INICIAL ,EntradaPosicion, PosicionInicialDiaSiguiente.class);	
			
			HashMap<String, Object> parametrosPosicionInicial = new HashMap<String, Object>();
			parametrosPosicionInicial.put("p_IdEmpresa", in.p_IdEmpresa);
			parametrosPosicionInicial.put("p_IdUsuario", in.p_IdUsuario);
			parametrosPosicionInicial.put("p_IdMoneda", in.p_IdMoneda);
			parametrosPosicionInicial.put("p_Fecha", DiaSiguiente.p_out_fechas.get(0).fechaCalculada);
			parametrosPosicionInicial.put("p_DatosPosicion", EntradaPosicion);
			
			HashMap<String, Class> paramSalidaPosicionInicial = new HashMap<String, Class>();
			HashMap<String, Object> respuestaIngresoPosicionInicialDia = new HashMap<String, Object>();

			paramSalidaPosicionInicial.put("p_out_AdvertenciasContador", Long.class);
			paramSalidaPosicionInicial.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class);
			paramSalidaPosicionInicial.put("p_out_ErroresContador", Long.class);
			paramSalidaPosicionInicial.put("p_out_ErroresDetalle", ErrorDetalle.class);
			
			respuestaIngresoPosicionInicialDia = sql.call(conn, SQL_INGRESAR_POSICION_INICIAL, parametrosPosicionInicial, paramSalidaPosicionInicial);	
		
			SQL.exec(conn, SQL_DROP_TABLA_PARAMETROS_POSICION_INICIAL);			
			if(!respuestaIngresoPosicionInicialDia.isEmpty()) {
				Salida.P_OUT_ADVERTENCIASCONTADOR = (Long) respuestaIngresoPosicionInicialDia.get("p_out_AdvertenciasContador");
				Salida.P_OUT_ADVERTENCIASDETALLE = (List<AdvertenciasDetalle>) respuestaIngresoPosicionInicialDia.get("p_out_AdvertenciasDetalle");
				Salida.P_OUT_ERRORESCONTADOR = (Long) respuestaIngresoPosicionInicialDia.get("p_out_ErroresContador");
				String msge = parametrosPosicionInicial.get("p_Fecha").toString();
				Salida.DIA_ACTUALIZACION_POSICION = msge;
				HashMap<String, String> valoresMensajePosicion = new HashMap<String, String>(2);
				valoresMensajePosicion.put("fechaModificacionPosicion", msge);
				Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "POSICION_INICIAL_ACTUALIZADA_DIA", valoresMensajePosicion, conn, in.p_IdEmpresa));
			}
			logger.debug("FIN::SQL_INGRESAR_POSICION_INICIAL");
			
		} catch (Exception e) {
			System.out.println(e);
			throw e;
		}
		
		logger.info("TERMINO: SalidaActualizarPosicionInicialDiaSiguiente");

		return Salida;	
	}
	
	private List<PosicionInicialDiaSiguiente> obtenerPosicionCierre(Connection con, SQL sql, EntradaPosicionInicialDiaSiguiente in, String fechaSistema, Long idPais) throws Exception {
		logger.info("INICIO::ActualizarPosicionInicialDiaSiguiente.obtenerPosicionCierre");	
		
		PosicionInicialDiaSiguiente p_PosicionInicial = new PosicionInicialDiaSiguiente();
		ObtenerDiaAnterior.OutObtenerDiaAnterior diaAnterior = new ObtenerDiaAnterior.OutObtenerDiaAnterior();
		ObtenerDiaAnterior o = new ObtenerDiaAnterior();


		HashMap<String, Object> parametrosPosicion = new HashMap<String, Object>();
		HashMap<String, Object> respuestaPosicionCierreDia = new HashMap<String, Object>();
		HashMap<String, Class> paramSalida = new HashMap<String, Class>();
		List<ResultadoActualizarPosicionInicialDiaSiguiente> resPosicion;
		
		Long numeroDiasAtras = 0L;
		for(int i=0; i<30; i++) {
			
			parametrosPosicion.put("p_IdEmpresa", in.p_IdEmpresa);
			parametrosPosicion.put("p_FechaOperacion", fechaSistema);
			parametrosPosicion.put("p_IdMoneda", in.p_IdMoneda);
			parametrosPosicion.put("p_IdUsuario", in.p_IdUsuario);
			parametrosPosicion.put("p_TipoMesa", "TODOS");

			paramSalida.put("p_out_AdvertenciasContador", Long.class);
			paramSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class);
			paramSalida.put("p_out_ErroresContador", Long.class);
		    paramSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
		    paramSalida.put("p_out_posicionPorMonedas", ResultadoActualizarPosicionInicialDiaSiguiente.class);
		    	    
			respuestaPosicionCierreDia = sql.call(con, SQL_SELECT_POSICION_CIERRE, parametrosPosicion, paramSalida);
			resPosicion = (List<ResultadoActualizarPosicionInicialDiaSiguiente>) respuestaPosicionCierreDia.get("p_out_posicionPorMonedas");
					
			for(int j=0 ; j < resPosicion.size(); j++) {
				if(resPosicion.get(j).IdMoneda.compareTo(in.p_IdMoneda) == 0){	
					if(resPosicion.get(j).Posicion.compareTo(BigDecimal.ZERO) != 0) {
						p_PosicionInicial.PosicionInicial = resPosicion.get(j).Posicion;
						break;
					}
					else if(resPosicion.get(j).PosicionInicial.compareTo(BigDecimal.ZERO) != 0){
						p_PosicionInicial.PosicionInicial = resPosicion.get(j).PosicionInicial;	
						break;
					}
					else {
						InObtenerDiaAnterior entrada = new InObtenerDiaAnterior();
						entrada.idPais = idPais;
						entrada.diaHabil = "S";
						InObtenerDiaAnterior.FechasIn f1 = new InObtenerDiaAnterior.FechasIn();
						f1.fechaInicial = fechaSistema;
						f1.numeroDias = 1L;
						List<InObtenerDiaAnterior.FechasIn> fechasIn = new ArrayList<InObtenerDiaAnterior.FechasIn>();
						fechasIn.add(f1);
						entrada.fechas = fechasIn;
						diaAnterior = o.logica(con, entrada); 
						numeroDiasAtras++;
						fechaSistema = diaAnterior.fechas.get(0).fechaCalculada;
						break;
					}
				}
			}
			
			if(p_PosicionInicial.PosicionInicial.compareTo(BigDecimal.ZERO) != 0) {
				logger.info("Fecha donde encuentra la posicion::"+fechaSistema);
				break;
			}
			logger.info("Dias hacia atras::"+numeroDiasAtras);
			
		}
		
		p_PosicionInicial.IdPosicionInicial = BigDecimal.ZERO;
		p_PosicionInicial.Fecha = null;
		p_PosicionInicial.IdMoneda = in.p_IdMoneda;
		p_PosicionInicial.TipoCambioInicial = BigDecimal.ZERO;
		p_PosicionInicial.CierreIngresado = BigDecimal.ZERO;
		p_PosicionInicial.TipoInstrumento = "STP";
		p_PosicionInicial.Estado = "ACT";
		p_PosicionInicial.ResultadoPosicionInicial = BigDecimal.ZERO;
		
		List<PosicionInicialDiaSiguiente> EntradaPosicion = new ArrayList<PosicionInicialDiaSiguiente>();
		EntradaPosicion.add(p_PosicionInicial);
		
		logger.info("FIN::ActualizarPosicionInicialDiaSiguiente.obtenerPosicionCierre");	
		return EntradaPosicion;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { // -->FIJARSE EN QUE ENTORNO SE ESTA
													 	     				// PROBANDO<--
			//Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			Gson gson = Utils.getGsonBuilder();
			
			String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"MSP\",\"p_IdMoneda\":\"USD\"}";

			EntradaPosicionInicialDiaSiguiente Objin = gson.fromJson(jsonRF, EntradaPosicionInicialDiaSiguiente.class);
			ActualizarPosicionInicialDiaSiguiente demo = new ActualizarPosicionInicialDiaSiguiente();

			try {

				System.out.println("try");
				SalidaActualizarPosicionInicialDiaSiguiente ObjOut = demo.logica(con, Objin);

				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();

			}

		}
		System.out.println("== END ==");	
	}
}