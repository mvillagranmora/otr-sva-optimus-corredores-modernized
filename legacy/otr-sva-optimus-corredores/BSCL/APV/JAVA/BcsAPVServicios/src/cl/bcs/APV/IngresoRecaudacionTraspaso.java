package cl.bcs.APV;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresoRecaudacionTraspaso implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(IngresoRecaudacionTraspaso.class);
	
	private final String SQL_SELECT_TRASPASOS = loadResourceToString("cl/bcs/APV/sql/IngresoRecaudacionTraspaso_SelectTraspasos.sql");
	private final String SQL_SELECT_DETALLE_TRASPASOS = loadResourceToString("cl/bcs/APV/sql/IngresoRecaudacionTraspaso_SelectDetalleTraspasos.sql");
	private final String SQL_SELECT_DETALLE_TRASPASOS_REAPERTURA = loadResourceToString("cl/bcs/APV/sql/IngresoRecaudacionTraspaso_SelectDetalleTraspasosReapertura.sql");
	private final String SQL_ELIMINAR_CrearOperacionSpot_Proceso = "TRUNCATE TABLE \"BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso\"";	
	private final String SQL_ELIMINAR_SaldoFlujoCaja = "DELETE FROM \"BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso\"";
	private final String SQL_SELECT_CrearOperacionSpot_Proceso = "SELECT * FROM \"BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso\"";
	
	public static class EntradaIngresoRecaudacionTraspaso extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_TipoTraspaso;
		public EntradaRecaudacion p_Recaudacion;
		public List<EntradaTraspaso> p_Traspasos;
		
		public EntradaIngresoRecaudacionTraspaso(){
			this.p_Traspasos = new ArrayList<EntradaTraspaso>();
		}
	}
	
	public static class EntradaTraspaso extends BcsPojo{
		public Long IdTraspaso;
		public BigDecimal MontoTraspaso;
		public List<Long> IdDetalleTraspaso;
		public EntradaTraspaso(){
			this.IdDetalleTraspaso = new ArrayList<Long>();
		}
	}
	
	public static class EntradaRecaudacion extends BcsPojo{
		public Long FolioCartola;
		public Long LineaCartola;
		public Long IdSucursal;
		public String CuentaCorriente;
		public String IdTipoDeposito;
		public Long IdBanco;
	}
	
	public static class SalidaIngresoRecaudacionTraspaso extends BcsPojo{
		public Long	P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Resultado> P_OUT_IDRESULTADO;
		
		public SalidaIngresoRecaudacionTraspaso() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_IDRESULTADO = new ArrayList<Resultado>();
		}
	}
	
	public static class Resultado extends BcsPojo{
		public String 		CodigoResultado;
		public String 		NombreResultado;
		public Long 		IdResultado;
	}
	
	public static class Traspaso extends BcsPojo{
		public Long IdTraspaso;
		public Long IdPersona;
		public Long	IdEmpleador;
		public String FolioContratoSAAPV;
		public BigDecimal MontoPesoDetalle;
		public String Trabajador;
		public String IdEstado;
		public String NombreEntidad;
	}
	
	public static class DetalleTraspaso extends BcsPojo{
		public Long IdTraspaso;
		public Long IdDetalleTraspaso;
		public Long IdContratoSAAPV;
		public Long IdRegimenTributario;
		public String Regimen;
		public BigDecimal MontoPeso;
		public BigDecimal MontoUF;
		public BigDecimal Porcentaje;
		public Long IdCliente;
	}
	
	public static class Recaudacion extends BcsPojo{
		public Long FolioCartola;
		public Long LineaCartola;
		public Long IdSucursal;
		public String CuentaCorriente;
		public String IdTipoDeposito;
		public Long IdBanco;
		public BigDecimal MontoDeposito;
	}
	
	public static class MontoReapertura extends BcsPojo{
		public BigDecimal MontoReapertura;
	}
	
	public static class PortafolioReaperturaRec extends BcsPojo{
		public Long IdPortafolio;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaIngresoRecaudacionTraspaso jin = gson.fromJson(in.toString() ,EntradaIngresoRecaudacionTraspaso.class);
		SalidaIngresoRecaudacionTraspaso out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaIngresoRecaudacionTraspaso logica(Connection con, EntradaIngresoRecaudacionTraspaso entrada) throws Exception {
		logger.info("INICIO::IngresoRecaudacionTraspaso");
		long inicioServicio = System.currentTimeMillis();
		SalidaIngresoRecaudacionTraspaso result = new SalidaIngresoRecaudacionTraspaso();
		SalidaIngresoRecaudacionTraspaso out = new SalidaIngresoRecaudacionTraspaso();
		MensajeSalida msg = new MensajeSalida();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Long IdResultado = 0L;
		SQL sql = new SQL();
		
		for( EntradaTraspaso fila : entrada.p_Traspasos) {
			Map<Long, Traspaso> traspasos = obtenerTraspasos(con, fila, entrada.p_TipoTraspaso);
			
			if(traspasos != null) {
				Recaudacion recaudacion = obtenerDatosRecaudacion(con, entrada.p_Recaudacion, traspasos, entrada.p_TipoTraspaso, entrada.p_Traspasos, fechaSistema);
				List<DetalleTraspaso> detalleTraspasos = obtenerDetalleTraspasos(con, fila, entrada.p_TipoTraspaso);
				GrabarRecaudacionTraspaso.ParametrosSalidaServicio resultadoGrabarRecaudacionTraspaso = grabarRecaudacionTraspaso(con, entrada, recaudacion, traspasos, detalleTraspasos); 
				SalidaIngresoRecaudacionTraspaso resultIngresoRecaudacionTraspaso = obtenerSalidaIngresoRecaudacionTraspaso(resultadoGrabarRecaudacionTraspaso);
				
				HashMap<String, Object> parametros = new HashMap<String, Object>();	
				Long cantOperaciones = sql.countQuery(con, SQL_SELECT_CrearOperacionSpot_Proceso, parametros);

				if(cantOperaciones != null && cantOperaciones > 0L){
					SQL.exec(con, SQL_ELIMINAR_SaldoFlujoCaja);
					SQL.exec(con, SQL_ELIMINAR_CrearOperacionSpot_Proceso);
				}
				
				if(entrada.p_Traspasos.size() > 1  && resultIngresoRecaudacionTraspaso.P_OUT_ERRORESCONTADOR > 0L) {
					for(MensajeSalida mge : resultIngresoRecaudacionTraspaso.P_OUT_MENSAJES) {
						if(mge.tipo.compareToIgnoreCase("ERROR") == 0) {
							if(mge.codigo.compareToIgnoreCase("ERROR_RECAUDACION_CLIENTE") == 0) {
								mge.descripcion = mge.descripcion.concat(" Folio Traspaso: ".concat(fila.IdTraspaso.toString()));
							}	
							result.P_OUT_MENSAJES.add(mge);
						}
					}
					IdResultado = resultIngresoRecaudacionTraspaso.P_OUT_IDRESULTADO.get(0).IdResultado;
				}else result.P_OUT_MENSAJES.addAll(resultIngresoRecaudacionTraspaso.P_OUT_MENSAJES);
			
				result.P_OUT_ERRORESCONTADOR = result.P_OUT_ERRORESCONTADOR + resultIngresoRecaudacionTraspaso.P_OUT_ERRORESCONTADOR;
				result.P_OUT_IDRESULTADO.addAll(resultIngresoRecaudacionTraspaso.P_OUT_IDRESULTADO);
				
			}
			else {
				msg = new MensajeSalida();
				msg.id = 6L;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR_TRASPASO_RECAUDACION";
				msg.descripcion = "Problemas al validar traspaso.";
				result.P_OUT_MENSAJES.add(msg);
				result.P_OUT_ERRORESCONTADOR++;
			}
			long finServicio = System.currentTimeMillis();
			logger.info("FIN::IngresoRecaudacionTraspaso", finServicio - inicioServicio);
			
		}
		
		if(result.P_OUT_ERRORESCONTADOR > 0L && entrada.p_Traspasos.size() > 1L) {
			for(MensajeSalida mge : result.P_OUT_MENSAJES) {
				if(mge.tipo.compareToIgnoreCase("ERROR") == 0) {
					out.P_OUT_MENSAJES.add(mge);
					out.P_OUT_ERRORESCONTADOR++;
				}
			}		
			Resultado res = new Resultado();
			res.CodigoResultado = "REC";
			res.IdResultado = IdResultado;
			res.NombreResultado = "Recaudacion";
			out.P_OUT_IDRESULTADO.add(res);
			con.rollback();	
			
		}else out = result;
		
		return out;	
	}
	
	private Map<Long, Traspaso> obtenerTraspasos(Connection con, EntradaTraspaso entradaTraspasos, String tipoTraspaso) throws Exception{
		logger.trace("INICIO::IngresoRecaudacionTraspaso.obtenerTraspasos");
		long inicio = System.currentTimeMillis();
		SQL _sql = new SQL();
		List<EntradaTraspaso> lstEntradaTraspasos = new ArrayList<EntradaTraspaso>();
		lstEntradaTraspasos.add(entradaTraspasos);
		List<Long> idsTraspasos = lstEntradaTraspasos.parallelStream().map(traspaso->traspaso.IdTraspaso).distinct().collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdTraspaso", idsTraspasos);
		List<Traspaso> traspasosSinAgrupar = _sql.query(con, SQL_SELECT_TRASPASOS, parametros, Traspaso.class); 
		Map<Long, Traspaso> traspasos = traspasosSinAgrupar.parallelStream().collect(Collectors.toMap(traspaso->traspaso.IdTraspaso, traspaso->traspaso));
		if(validarTraspasosEntrada(con, traspasosSinAgrupar, tipoTraspaso) == false) return null;	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::IngresoRecaudacionTraspaso.obtenerTraspasos", fin - inicio);
		return traspasos;
	}

	private boolean validarTraspasosEntrada(Connection con, List<Traspaso> entradaTraspasos, String tipoTraspaso) {
		logger.trace("INICIO::IngresoRecaudacionTraspaso.validarTraspasosEntrada");
		long inicio = System.currentTimeMillis();
		if(tipoTraspaso == "I") {
			for(Traspaso traspaso : entradaTraspasos) {
				if(traspaso.IdEstado != "COM" || traspaso.NombreEntidad.compareTo(entradaTraspasos.get(0).NombreEntidad) != 0) return false;
			}
		}	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::IngresoRecaudacionTraspaso.validarTraspasosEntrada", fin - inicio);
		return true;	
	}
	
	private Recaudacion obtenerDatosRecaudacion(Connection con, EntradaRecaudacion entradaRecaudacion, Map<Long, Traspaso> traspasos, String tipoTraspaso,  List<EntradaTraspaso> entradaTraspasos, String fechaSistema) throws Exception{
		logger.trace("INICIO::IngresoRecaudacionTraspaso.obtenerDatosRecaudacion");
		long inicio = System.currentTimeMillis();
		Recaudacion recaudacion = new Recaudacion();
		recaudacion.FolioCartola = entradaRecaudacion.FolioCartola;
		recaudacion.LineaCartola = entradaRecaudacion.LineaCartola;
		recaudacion.IdSucursal = entradaRecaudacion.IdSucursal;
		recaudacion.CuentaCorriente = entradaRecaudacion.CuentaCorriente;
		recaudacion.IdTipoDeposito = entradaRecaudacion.IdTipoDeposito;
		recaudacion.IdBanco = entradaRecaudacion.IdBanco;
		recaudacion.MontoDeposito = obtenerMontoDeposito(con, traspasos, tipoTraspaso, entradaTraspasos, fechaSistema);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::IngresoRecaudacionTraspaso.obtenerDatosRecaudacion", fin - inicio);
		return recaudacion;
	}
	
	private BigDecimal obtenerMontoDeposito(Connection con, Map<Long, Traspaso> traspasos, String tipoTraspaso, List<EntradaTraspaso> entradaTraspasos, String fechaSistema) throws Exception{
		logger.trace("INICIO::IngresoRecaudacionTraspaso.obtenerMontoDeposito");
		long inicio = System.currentTimeMillis();
		SQL _sql = new SQL();
		BigDecimal montoDeposito;
		if(tipoTraspaso.compareTo("I") == 0) montoDeposito = traspasos.entrySet().stream().map(traspaso->traspaso.getValue().MontoPesoDetalle).reduce(BigDecimal.ZERO, (subtotal, montoDetalle)-> subtotal.add(montoDetalle, MathContext.DECIMAL128));
		else montoDeposito = entradaTraspasos.get(0).MontoTraspaso;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::IngresoRecaudacionTraspaso.obtenerMontoDeposito", fin - inicio);
		return montoDeposito;
	}
	
	private List<DetalleTraspaso> obtenerDetalleTraspasos(Connection con,EntradaTraspaso entradaTraspasos, String tipoTraspaso) throws Exception{
		logger.trace("INICIO::IngresoRecaudacionTraspaso.obtenerDetalleTraspasos");
		long inicio = System.currentTimeMillis();
		SQL _sql = new SQL();
		List<EntradaTraspaso> lstEntradaTraspasos = new ArrayList<EntradaTraspaso>();
		lstEntradaTraspasos.add(entradaTraspasos);
		List<Long> idsTraspasos = lstEntradaTraspasos.parallelStream().map(traspaso->traspaso.IdTraspaso).distinct().collect(Collectors.toList());
		List<DetalleTraspaso> detalleTraspasos = new ArrayList <DetalleTraspaso>();
		
		if(tipoTraspaso.compareTo("I") == 0) {
			HashMap<String, Object> parametros = new HashMap<String, Object>();
			parametros.put("IdTraspaso", idsTraspasos);			
			detalleTraspasos = _sql.query(con, SQL_SELECT_DETALLE_TRASPASOS, parametros, DetalleTraspaso.class);
		}
		else {
			if(tipoTraspaso.compareTo("R") == 0) {
				for(Long fila : entradaTraspasos.IdDetalleTraspaso) {
					HashMap<String, Object> parametrosReapertura = new HashMap<String, Object>();
					parametrosReapertura.put("IdTraspaso", idsTraspasos.get(0));
					parametrosReapertura.put("IdDetalleTraspaso", fila);
					List<DetalleTraspaso> detalleTraspasoReapertura = _sql.query(con, SQL_SELECT_DETALLE_TRASPASOS_REAPERTURA, parametrosReapertura, DetalleTraspaso.class);
					detalleTraspasos.add(detalleTraspasoReapertura.get(0));
				}
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::IngresoRecaudacionTraspaso.obtenerDetalleTraspasos", fin - inicio);
		return detalleTraspasos;
	}
	
	private GrabarRecaudacionTraspaso.ParametrosSalidaServicio grabarRecaudacionTraspaso(Connection con, EntradaIngresoRecaudacionTraspaso entrada, Recaudacion recaudacion, Map<Long, Traspaso> traspasos, List<DetalleTraspaso> detalleTraspasos) throws Exception{
		logger.trace("INICIO::IngresoRecaudacionTraspaso.grabarRecaudacionTraspaso");
		long inicio = System.currentTimeMillis();
		SQL _sql = new SQL();
		
		//Se obtienen los detalles en caso de ser una reapertura traspaso 
		List<Long> lstIdDetalleTraspaso = new ArrayList<Long>();
		for(DetalleTraspaso fila : detalleTraspasos) {
			lstIdDetalleTraspaso.add(fila.IdDetalleTraspaso);
		}

		List<GrabarRecaudacionTraspaso.ParametrosEntradaServicio> lstGrabarRecaudacionTraspaso = new ArrayList<GrabarRecaudacionTraspaso.ParametrosEntradaServicio>();
		
		//for(EntradaIngresoRecaudacionTraspaso fila : entrada) {
			GrabarRecaudacionTraspaso.ParametrosEntradaServicio entradaGrabarRecaudacion = new GrabarRecaudacionTraspaso.ParametrosEntradaServicio();
			entradaGrabarRecaudacion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaGrabarRecaudacion.p_IdUsuario = entrada.p_IdUsuario;
			entradaGrabarRecaudacion.p_IdModulo = entrada.p_IdModulo;
			entradaGrabarRecaudacion.p_Origen = "TRASPA";
			entradaGrabarRecaudacion.p_Fecha = obtenerFechaSistema(con,entrada.p_IdEmpresa);
			entradaGrabarRecaudacion.p_IdsDetalleTraspasoReapertura.addAll(lstIdDetalleTraspaso);
			entradaGrabarRecaudacion.p_RecaudacionConciliacion = detalleTraspasos.parallelStream().filter(detalleTraspaso->traspasos.get(detalleTraspaso.IdTraspaso) != null).map(detalleTraspaso->{
				GrabarRecaudacionTraspaso.RecaudacionConciliacion recaudacionConciliacion = new GrabarRecaudacionTraspaso.RecaudacionConciliacion();
				Traspaso traspaso = traspasos.get(detalleTraspaso.IdTraspaso);
				recaudacionConciliacion.IdTraspaso = detalleTraspaso.IdTraspaso;
				recaudacionConciliacion.IdDetalleTraspaso = detalleTraspaso.IdDetalleTraspaso;
				recaudacionConciliacion.IdContratoSAAPV = detalleTraspaso.IdContratoSAAPV;
				recaudacionConciliacion.FolioCartola = recaudacion.FolioCartola;
				recaudacionConciliacion.LineaCartola = recaudacion.LineaCartola;
				recaudacionConciliacion.IdSucursal = recaudacion.IdSucursal;
				recaudacionConciliacion.FechaIngreso = entradaGrabarRecaudacion.p_Fecha;
				recaudacionConciliacion.CuentaCorriente	= recaudacion.CuentaCorriente;
				recaudacionConciliacion.IdTipoDeposito = recaudacion.IdTipoDeposito;
				recaudacionConciliacion.IdBanco	= recaudacion.IdBanco;
				recaudacionConciliacion.IdRegimenTributario	= detalleTraspaso.IdRegimenTributario;
				recaudacionConciliacion.Regimen	= detalleTraspaso.Regimen;
				recaudacionConciliacion.NumeroDocumento = "1";
				recaudacionConciliacion.MontoDeposito = recaudacion.MontoDeposito.longValue();
				recaudacionConciliacion.MontoDetalle = obtenerMontoDetalle(detalleTraspaso);
				recaudacionConciliacion.IdCliente = detalleTraspaso.IdCliente;
				recaudacionConciliacion.Origen = "TRASPA";
				recaudacionConciliacion.TipoAfiliado = "DEPENDIENTE".equals(traspaso.Trabajador) ? "DEP" : "IND";
				recaudacionConciliacion.IdPersona = traspaso.IdPersona;
				recaudacionConciliacion.IdEmpleador	 = traspaso.IdEmpleador;
				recaudacionConciliacion.FolioContratoSAAPV  = traspaso.FolioContratoSAAPV;
				return recaudacionConciliacion;
			}).collect(Collectors.toList());
			lstGrabarRecaudacionTraspaso.add(entradaGrabarRecaudacion);
		//}
		
		
		
		GrabarRecaudacionTraspaso grabarRecaudacionTraspaso = new GrabarRecaudacionTraspaso();
		GrabarRecaudacionTraspaso.ParametrosSalidaServicio resultadoGrabarRecaudacionTraspaso = grabarRecaudacionTraspaso.logica(con, entradaGrabarRecaudacion); 
		long fin = System.currentTimeMillis();
		logger.trace("FIN::IngresoRecaudacionTraspaso.grabarRecaudacionTraspaso", fin - inicio);
		return resultadoGrabarRecaudacionTraspaso;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::IngresoRecaudacionTraspaso.obtenerFechaSistema");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::IngresoRecaudacionTraspaso.obtenerFechaSistema", fin - inicio);
		return fechaSistema;
	}
	
	private Long obtenerMontoDetalle(DetalleTraspaso detalleTraspaso) {
		if(detalleTraspaso.MontoPeso.compareTo(BigDecimal.ZERO) != 0 ) {
			return detalleTraspaso.MontoPeso.longValue();
		}
		if(detalleTraspaso.MontoUF.compareTo(BigDecimal.ZERO) != 0 ) {
			return detalleTraspaso.MontoUF.longValue();
		}
		if(detalleTraspaso.Porcentaje.compareTo(BigDecimal.ZERO) != 0 ) {
			return detalleTraspaso.Porcentaje.longValue();
		}
		return 0L;
	}

	private SalidaIngresoRecaudacionTraspaso obtenerSalidaIngresoRecaudacionTraspaso(GrabarRecaudacionTraspaso.ParametrosSalidaServicio resultadoGrabarRecaudacionTraspaso){
		logger.trace("INICIO::IngresoRecaudacionTraspaso.obtenerSalidaIngresoRecaudacionTraspaso");
		long inicio = System.currentTimeMillis();
		SalidaIngresoRecaudacionTraspaso salidaIngresoRecaudacionTraspaso = new SalidaIngresoRecaudacionTraspaso();
		
		for(int i=0; i<resultadoGrabarRecaudacionTraspaso.P_OUT_MENSAJES.size(); i++) {
			if(resultadoGrabarRecaudacionTraspaso.P_OUT_MENSAJES.get(i).tipo.equals("ERROR")) {
				salidaIngresoRecaudacionTraspaso.P_OUT_ERRORESCONTADOR++;
			}
		}
		
		salidaIngresoRecaudacionTraspaso.P_OUT_MENSAJES = resultadoGrabarRecaudacionTraspaso.P_OUT_MENSAJES;
		salidaIngresoRecaudacionTraspaso.P_OUT_IDRESULTADO = resultadoGrabarRecaudacionTraspaso.P_OUT_IDRESULTADO.parallelStream().map(resultadoRecaudacion->{
			Resultado resultado = new Resultado();
			resultado.NombreResultado = resultadoRecaudacion.NombreResultado;
			resultado.IdResultado = resultadoRecaudacion.IdResultado;
			resultado.CodigoResultado = resultadoRecaudacion.CodigoResultado;
			return resultado;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.trace("FIN::IngresoRecaudacionTraspaso.obtenerSalidaIngresoRecaudacionTraspaso", fin - inicio);
		return salidaIngresoRecaudacionTraspaso;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("master01","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			
			//String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_TipoTraspaso\":\"I\",\"p_Recaudacion\":{\"CuentaCorriente\":\"4355700\",\"IdTipoDeposito\":\"TRA\",\"IdBanco\":8},\"p_Traspasos\":[{\"IdTraspaso\":2991231,\"MontoTraspaso\":10097350},{\"IdTraspaso\":2991242,\"MontoTraspaso\":9310349},{\"IdTraspaso\":2991047,\"MontoTraspaso\":37762893}]}";
			//String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_TipoTraspaso\":\"I\",\"p_Recaudacion\":{\"CuentaCorriente\":\"4355700\",\"IdTipoDeposito\":\"TRA\",\"IdBanco\":8},\"p_Traspasos\":[{\"IdTraspaso\":2991242,\"MontoTraspaso\":10097350}]}";
			//String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_TipoTraspaso\":\"I\",\"p_Recaudacion\":{\"CuentaCorriente\":\"4355700\",\"IdTipoDeposito\":\"TRA\",\"IdBanco\":8},\"p_Traspasos\":[{\"IdTraspaso\":2991242,\"MontoTraspaso\":9310349},{\"IdTraspaso\":2991047,\"MontoTraspaso\":37762893}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_TipoTraspaso\":\"I\",\"p_Recaudacion\":{\"CuentaCorriente\":\"4355700\",\"IdTipoDeposito\":\"TRA\",\"IdBanco\":8},\"p_Traspasos\":[{\"IdTraspaso\":2987070,\"MontoTraspaso\":142796646},{\"IdTraspaso\":2987083,\"MontoTraspaso\":363637255},{\"IdTraspaso\":2988140,\"MontoTraspaso\":5003}]}";
			
			EntradaIngresoRecaudacionTraspaso Objin = gson.fromJson(json, EntradaIngresoRecaudacionTraspaso.class);
			IngresoRecaudacionTraspaso  demo=new IngresoRecaudacionTraspaso();				
			try {
				SalidaIngresoRecaudacionTraspaso ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut);
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
				//con.commit();
				con.rollback(); 			
	    	}
			System.out.println("== END ==");
	}
	
}