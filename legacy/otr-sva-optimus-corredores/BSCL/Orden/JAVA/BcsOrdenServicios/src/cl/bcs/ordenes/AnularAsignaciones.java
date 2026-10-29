package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.plataforma.lim.ValidarLimiteCuentaPropiaIFRF;
import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.utils.BcsPojo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AnularAsignaciones implements JavaCall {
	Logger logger = LogManager.getLogger(AnularAsignaciones.class);

	private final String SQL_INSERT_ANULAR_ASIGNACION_ENTRADA = loadResourceToString("cl/bcs/ordenes/sql/AnularAsignacionMasiva_Entrada.sql");
	private final String SQL_INSERT_ANULAR_ASIGNACION_EJECUTAR = loadResourceToString("cl/bcs/ordenes/sql/AnularAsignacionMasiva.sql");
	private final String SQL_SELECT_ANULAR_ASIGNACION_EJECUTAR = loadResourceToString("cl/bcs/ordenes/sql/SelectAsignacionGrupo.sql");
	private final String SQL_SELECT_ASIGNACION = loadResourceToString("cl/bcs/ordenes/sql/AnularAsignaciones_Select_Asignacion.sql");
	private final String SQL_SELECT_ASIGNACION_ANULAR = loadResourceToString("cl/bcs/ordenes/sql/AnularAsignaciones_SelectAsignacionAnular.sql");
	
	private final ConstantesLimites _cl = new ConstantesLimites();

	/*
	 * Clases Entrada/Salida
	 */
	
	public static class Asignacion_Entrada {
		public Long IdAsignacion;

		@Override
		public boolean equals(Object o) {
			Asignacion_Entrada e = (Asignacion_Entrada) o;
			return this.IdAsignacion.equals(e.IdAsignacion);
		}

		@Override
		public int hashCode() {
			return this.IdAsignacion.hashCode();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AnularAsignaciones_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_AnulaMovimientosFacturar = "N";
		public List<Asignacion_Entrada> asignacionesParaAnular;

		public AnularAsignaciones_Entrada() {
			this.asignacionesParaAnular = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	
	public static class AnularAsignaciones_Salida {
		public List<OrdenAnulada> p_out_OrdenesAnuladas;
		public List<AsignacionSalida> p_out_AsignacionSalida;
		public List<MensajeSalida> p_out_Mensajes;
		public Long P_OUT_ERRORESCONTADOR;

		public AnularAsignaciones_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.p_out_OrdenesAnuladas = new ArrayList<>();
			this.p_out_AsignacionSalida = new ArrayList<>();
			this.p_out_Mensajes = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Asignacion {
		public String TablaOrigen;
		public String Folio;
		public Long IdAsignacion;
		public Long IdAsignacionRef;
		public Long IdAsignacionGrupo;
		public Long IdInstrumento;
		public Long IdPortafolio;
		public Long IdCliente;
		public String IdEstadoAsignacion;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdMonedaOrden;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public BigDecimal MontoAsignadoLocal;
		public java.sql.Date FechaVencimientoInstrumento;
		public BigDecimal Tasa;
		public String NemoSVS;
	}

	public static class AsignacionSalida {
		public Long IdAsignacion;
		public String IdEstadoAsignacion;
		public String FolioTransaccion;
		public Long IdOrden;
		public String NombreCompleto;
		public Date FechaAsignacion;
		public String IdTipoCanalDistribucion;
		public Long IdInstrumentoUI;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class OrdenAnulada {
		public Long IdAsignacion;
		public Long IdOrden;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaDetalle {
		public Long Id;
		public Long IdOperacion;
		public String Resultado;
		public String CodigoMensaje;
		public String CodigoDato1;
		public String ValorDato1;
		public String CodigoDato2;
		public String ValorDato2;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AsignacionGrupo {
		public Long IdAsignacion;
		public Long IdAsignacionGrupo;
		public String EsAPlazo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;
		public Long IdInterno;
		public Long IdOperacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformacionDetalle {
		public String Codigo;
		public String Descripcion;
		public Long IdInterno;
		public Long IdOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		public Long IdInterno;
		public Long IdOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private static class ResultadoObtenerAsignacionesProceso extends BcsPojo {
		public List<Asignacion_Entrada> asignacionesAnular;
		public List<ValidarLimiteCuentaPropiaIFRF.Operacion> asignacionesLimitesCtaPropia;
		
		public ResultadoObtenerAsignacionesProceso() {
			asignacionesAnular = new ArrayList<>();
			asignacionesLimitesCtaPropia = new ArrayList<>();
		}
	}

	@Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();          
        AnularAsignaciones_Entrada jin = gson.fromJson(in.toString() ,AnularAsignaciones_Entrada.class);
        AnularAsignaciones_Salida out = logica(conn,jin);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
	}
	
	@Trace
	public AnularAsignaciones_Salida logica(Connection conn, AnularAsignaciones_Entrada entrada) throws Exception {
		logger.info("INICIO::AnularAsignaciones");
		
		Long init = System.currentTimeMillis();
		
		AnularAsignaciones_Salida salida = new AnularAsignaciones_Salida();
		
		List<Asignacion> asignacionesProceso = obtenerAsignaciones(conn, entrada);
		ResultadoObtenerAsignacionesProceso resultadoObtenerAsignacionesProceso = obtenerAsignacionesProceso(asignacionesProceso);
		
		List<MensajeSalida> mensajesValidacionLimitesCuentaPropia = validarLimitesCuentaPropia(conn, entrada, resultadoObtenerAsignacionesProceso);

		entrada.asignacionesParaAnular = resultadoObtenerAsignacionesProceso.asignacionesAnular;
		SQL sql = new SQL();

		HashMap<String, Object> params = new HashMap<>();
		List<Long> asig = new ArrayList<>();
		for (Asignacion_Entrada ent : entrada.asignacionesParaAnular)
			asig.add(ent.IdAsignacion);
		params.put("IdAsignacion", asig);

		List<AsignacionGrupo> asigGrupo = sql.query(conn, SQL_SELECT_ANULAR_ASIGNACION_EJECUTAR, params, AsignacionGrupo.class);
		for (AsignacionGrupo aGrupo : asigGrupo) {
			Asignacion_Entrada asi = new Asignacion_Entrada();
			asi.IdAsignacion = aGrupo.IdAsignacion;
			entrada.asignacionesParaAnular.add(asi);
		}
		sql.batch(conn, SQL_INSERT_ANULAR_ASIGNACION_ENTRADA, entrada.asignacionesParaAnular, Asignacion_Entrada.class);

		HashMap<String, Object> parmsIN = new HashMap<String, Object>();
		parmsIN.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parmsIN.put("p_IdUsuario", entrada.p_IdUsuario);
		parmsIN.put("p_IdModulo", entrada.p_IdModulo);

		HashMap<String, Class> parmsOUT = new HashMap<String, Class>();
		parmsOUT.put("p_out_OrdenesAnuladas", OrdenAnulada.class);
		parmsOUT.put("p_out_SalidaDetalle", SalidaDetalle.class);
		parmsOUT.put("p_out_InformacionDetalle", InformacionDetalle.class);
		parmsOUT.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
		parmsOUT.put("p_out_ErroresDetalle", ErrorDetalle.class);
		parmsOUT.put("p_out_ErroresContador", Long.class);
		parmsOUT.put("p_out_AdvertenciasContador", Long.class);
		parmsOUT.put("p_out_InformacionContador", Long.class);

		HashMap<String, Object> respuesta = sql.call(conn, SQL_INSERT_ANULAR_ASIGNACION_EJECUTAR, parmsIN, parmsOUT);

		List<InformacionDetalle> infoDet = (List<InformacionDetalle>) respuesta.get(("p_out_InformacionDetalle"));
		List<AdvertenciaDetalle> advDet = (List<AdvertenciaDetalle>) respuesta.get(("p_out_AdvertenciasDetalle"));
		List<ErrorDetalle> errDet = (List<ErrorDetalle>) respuesta.get(("p_out_ErroresDetalle"));

		HashMap<String, Object> dataSQL = new HashMap<>();
		List<Long> IdAsignacion = new ArrayList<>();
		for (Asignacion_Entrada asignacion : entrada.asignacionesParaAnular)
			IdAsignacion.add(asignacion.IdAsignacion);
		dataSQL.put("IdAsignacion", IdAsignacion);
		List<AsignacionSalida> salidaAsignacion = sql.query(conn, SQL_SELECT_ASIGNACION, dataSQL, AsignacionSalida.class);

		HashMap<Long, AsignacionSalida> hashAsignaciones = new HashMap<Long, AsignacionSalida>();
		for (AsignacionSalida asignacion : salidaAsignacion)
			hashAsignaciones.put(asignacion.IdAsignacion, asignacion);
		
		if (errDet != null)
		{
			for (ErrorDetalle errorDetalle : errDet) {
				AsignacionSalida asignacionHash = hashAsignaciones.get(errorDetalle.IdOperacion);
				if(asignacionHash != null) {
					String codigo = errorDetalle.Descripcion +" Secuencia de Orden:" + asignacionHash.IdOrden+" Transaccion:" + asignacionHash.FolioTransaccion + " Nombre:" + asignacionHash.NombreCompleto;
			        MensajeSalida msj = new MensajeSalida(asignacionHash.IdAsignacion,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
			        salida.p_out_Mensajes.add(msj);
				}
			}
		}
		
		if (infoDet != null)
		{
			for (InformacionDetalle infoDetalle : infoDet) {
				AsignacionSalida asignacionHash = hashAsignaciones.get(infoDetalle.IdOperacion);
				if(asignacionHash != null ) {
					String codigo = infoDetalle.Descripcion +" Secuencia de Orden:" + asignacionHash.IdOrden;
		            MensajeSalida msj = new MensajeSalida(asignacionHash.IdAsignacion,MensajeSalida.MENSAJE_OK,codigo,codigo);
		            salida.p_out_Mensajes.add(msj);
				}
			}
		}
		
		if (advDet != null)
		{
			for (AdvertenciaDetalle advDetalle : advDet) {
				AsignacionSalida asignacionHash = hashAsignaciones.get(advDetalle.IdOperacion);
				if(asignacionHash != null ) {
					String codigo = advDetalle.Descripcion +" Secuencia de Orden:" + asignacionHash.IdOrden+ " - "+ advDetalle.RequiereAutorizacion + " Nombre:" + asignacionHash.NombreCompleto+ " Transaccion:" + asignacionHash.FolioTransaccion;
		            MensajeSalida msj = new MensajeSalida(asignacionHash.IdAsignacion,MensajeSalida.MENSAJE_ADVERTENCIA,codigo,codigo);
		            salida.p_out_Mensajes.add(msj);
				}
			}
		}
		salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salida.p_out_Mensajes);
		for (MensajeSalida msje : salida.p_out_Mensajes) {
			msje.descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, msje.codigo, new HashMap<>());
		}
		salida.p_out_Mensajes.addAll(mensajesValidacionLimitesCuentaPropia);
		salida.p_out_AsignacionSalida.addAll(salidaAsignacion);
		
		logger.info("FIN::Anularasignaciones, tiempo: " + (System.currentTimeMillis() - init));
		return salida;
	}
	
	private ResultadoObtenerAsignacionesProceso obtenerAsignacionesProceso(List<Asignacion> asignaciones) {
		logger.trace("INICO::AnularAsignaciones.obtenerAsignacionesProceso");
		long inicio = System.currentTimeMillis();
		
		ResultadoObtenerAsignacionesProceso resultadoObtenerAsignacionesProceso = new ResultadoObtenerAsignacionesProceso();
		asignaciones.forEach(asignacion -> {
			boolean noEsAsignacionRefSimultanea = !"CteAsignacionSimultaneaRelacionada".equals(asignacion.TablaOrigen);
			if (noEsAsignacionRefSimultanea) {
				Asignacion_Entrada asignacionEntrada = new Asignacion_Entrada();
				asignacionEntrada.IdAsignacion = asignacion.IdAsignacion;
				resultadoObtenerAsignacionesProceso.asignacionesAnular.add(asignacionEntrada);
			}
			
			boolean esAnulable = !"EAF".equals(asignacion.IdEstadoAsignacion) && !"ANU".equals(asignacion.IdEstadoAsignacion);
			if (noEsAsignacionRefSimultanea && esAnulable) {
				ValidarLimiteCuentaPropiaIFRF.Operacion operacion = new ValidarLimiteCuentaPropiaIFRF.Operacion();
				operacion.Folio = asignacion.Folio;
				operacion.IdOperacion = asignacion.IdAsignacion;
				operacion.IdInterno = asignacion.IdAsignacion;
				operacion.IdInternoGrupo = asignacion.IdAsignacionGrupo;
				operacion.IdCliente = asignacion.IdCliente;
				operacion.IdPortafolio = asignacion.IdPortafolio;
				operacion.IdInstrumento = asignacion.IdInstrumento;
				operacion.IdTipoCanalIngreso = asignacion.IdTipoCanalIngreso;
				operacion.IdTipoOperacion = asignacion.IdTipoOperacion;
				operacion.IdMonedaOperacion = asignacion.IdMonedaOrden;
				operacion.Cantidad = asignacion.Cantidad;
				operacion.ValorRescate = asignacion.ValorRescate;
				operacion.MontoCompraMonedaSistema = asignacion.MontoAsignadoLocal;
				operacion.FechaVencimientoInstrumento = asignacion.FechaVencimientoInstrumento;
				operacion.Tasa = asignacion.Tasa;
				operacion.NemoSVS = asignacion.NemoSVS;
				resultadoObtenerAsignacionesProceso.asignacionesLimitesCtaPropia.add(operacion);
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AnularAsignaciones.obtenerAsignacionesProceso {}", fin - inicio);
		
		return resultadoObtenerAsignacionesProceso;
	}
	
	private List<Asignacion> obtenerAsignaciones(Connection conn, AnularAsignaciones_Entrada entrada) throws SQLException {
		logger.debug("INICIO::AnularAsignaciones.obtenerAsignacionesAnular");
		long inicio = System.currentTimeMillis();
		
		List<Long> ids = entrada.asignacionesParaAnular.stream().map(u -> u.IdAsignacion).distinct().collect(Collectors.toList());
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdAsignacion", ids);
		
		List<Asignacion> asignaciones = new SQL().query(conn, SQL_SELECT_ASIGNACION_ANULAR, params, Asignacion.class);
		
		long fin = System.currentTimeMillis();			
		logger.debug("FIN::AnularAsignaciones.obtenerAsignacionesAnular {}", fin - inicio);
		
		return asignaciones;
	}
	
	private List<MensajeSalida> validarLimitesCuentaPropia(Connection conn, AnularAsignaciones_Entrada entrada, ResultadoObtenerAsignacionesProceso resultadoObtenerAsignacionesProceso) throws Exception {
		logger.trace("INICIO::AnularAsignaciones.validarLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		ValidarLimiteCuentaPropiaIFRF.EntradaValidarLimiteCuentaPropiaIFRF entradaValidarLimiteCuentaPropiaIFRF = new ValidarLimiteCuentaPropiaIFRF.EntradaValidarLimiteCuentaPropiaIFRF();
		entradaValidarLimiteCuentaPropiaIFRF.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarLimiteCuentaPropiaIFRF.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarLimiteCuentaPropiaIFRF.p_IdModulo = entrada.p_IdModulo;
		entradaValidarLimiteCuentaPropiaIFRF.p_UsaReglasNegocio = _cl.SI;
		entradaValidarLimiteCuentaPropiaIFRF.p_IdTipoProceso = _cl.PROCESO_DESASIGNAR;
		entradaValidarLimiteCuentaPropiaIFRF.p_Operaciones = resultadoObtenerAsignacionesProceso.asignacionesLimitesCtaPropia;
		ValidarLimiteCuentaPropiaIFRF.SalidaValidarLimiteCuentaPropiaIFRF salidaValidarLimiteCuentaPropiaIFRF = new ValidarLimiteCuentaPropiaIFRF().logica(conn, entradaValidarLimiteCuentaPropiaIFRF);
		List<MensajeSalida> mensajes = salidaValidarLimiteCuentaPropiaIFRF.P_OUT_MENSAJES;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AnularAsignaciones.validarLimitesCuentaPropia {}", fin - inicio);
		
		return mensajes;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"N\",\"asignacionesParaAnular\":[{\"IdAsignacion\":3731318}]}";
			
			AnularAsignaciones_Entrada Objin = gson.fromJson(json, AnularAsignaciones_Entrada.class);
			
			AnularAsignaciones demo = new AnularAsignaciones();
			try {
				AnularAsignaciones_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				//con.commit();
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
