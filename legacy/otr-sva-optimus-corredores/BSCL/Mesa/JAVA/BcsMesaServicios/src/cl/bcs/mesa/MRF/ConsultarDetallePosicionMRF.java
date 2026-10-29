package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MRF.ConsultarResumenPosicionMRF.ConsultarResumenPosicionMRF_Entrada;
import cl.bcs.mesa.MRF.ConsultarResumenPosicionMRF.ConsultarResumenPosicionMRF_Salida;
import cl.bcs.mesa.MRF.pojo.CacheMRF;
import cl.bcs.plataforma.pla.utils.CacheServicio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarDetallePosicionMRF implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ConsultarDetallePosicionMRF.class);

	private final String SELECT_POSICION_MRF = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectPosicionMRF.sql");
	private final String SELECT_EVENTOS_MANUALES_MRF_FECHAPOSICION = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectEventosManualesRFFechaPosicion.sql");


	private final String SELECT_OPERACIONES_MRF_FECHAPOSICION = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectOperacionesRFFechaPosicion.sql");
	private final String SELECT_ANTICIPOS_OPERACIONES_MRF_FECHAPOSICION = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectAnticiposOperacionesRFFechaPosicion.sql");
	private final String SELECT_FAC_SIM_CONTADO_MRF_FECHAPOSICION = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectFacSimContadoRFFechaPosicion.sql");

	private final String SELECT_VENCIMIENTOS_MRF_FECHAPOSICION = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectVencimientosOperacionesRFFechaPosicion.sql");
	private final String SELECT_FAC_PM_MRF_FECHAPOSICION = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectFacPmRFFechaPosicion.sql");
	private final String SELECT_VENCIMIENTO_TITULOS_MRF_FECHAPOSICION = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectVencimientosTitulosRFFechaPosicion.sql");
	private final String SELECT_FACTURAS_SPOT = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectFacturasSpot.sql");
	private final String SELECT_FACTURAS_SPOT_T0 = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectFacturasSpotT0.sql");
	
	public final List<String> _TiposOperacionRV = Arrays.asList("INAC", "INAX", "INMI", "INCI", "INCX", "INEX", "INEN", "INMO", "INOS");
	
	private SQL _sql = new SQL();
	
	private static final CacheServicio CACHE = CacheServicio.registrar(CacheMRF.DETALLE_POSICION, CacheMRF.TTL_POSICION_MS, CacheMRF.TAG_POSICION);

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarDetallePosicionMRF_Entrada jin = gson.fromJson(in.toString(), ConsultarDetallePosicionMRF_Entrada.class);
		String clave = CacheServicio.clave(jin.p_IdEmpresa, jin.p_IdPais, jin.p_NombreEvento, jin.p_IdModulo, jin.p_FechaPosicion, jin.p_NombreMetodo, jin.p_NombreItem, jin.p_IdMetodo, jin.p_TipoPosicion);
		String json = CACHE.obtener(clave, () -> {
			ConsultarDetallePosicionMRF_Salida out = logica(conn, jin);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return gson.toJson(out);
		});
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(json).getAsJsonObject();
		return dataout;
	}

	public static class ConsultarDetallePosicionMRF_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public Long p_IdPais;
		public String p_NombreEvento;
		public String p_IdModulo;
		public String p_FechaPosicion;
		public String p_NombreMetodo;
		public String p_NombreItem;
		public String p_IdMetodo;
		public String p_TipoPosicion;

		public ConsultarDetallePosicionMRF_Entrada() {
			p_IdEmpresa = 0L;
			p_IdUsuario = 0L;
			p_IdPais = 0L;
			p_NombreEvento = "";
			p_IdModulo = "";
			p_FechaPosicion = "";
			p_NombreMetodo = "";
			p_NombreItem = "";
			p_IdMetodo = "";
			p_TipoPosicion = "";
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Operacion {
		public Long FolioComprobante;
		public String TipoOperacion;
		public String Agrupacion;
		public String FechaComprobante;
		public String FechaLiquidacion;
		public String DocumentoIdentidad;
		public BigDecimal MontoLiquidacionFinal;
		public String SumaResta;
	}

	public static class OperacionesManualesMRF_Salida {
		public Long IdPosicion;
		public String Fecha;
		public Long IdMatriz;
		public BigDecimal MontoEvento;
	}

	public static class ConsultarDetallePosicionMRF_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_IDITEM;
		public String P_OUT_NOMBREITEM;
		public List<Operacion> P_OUT_OPERACIONES;

		public ConsultarDetallePosicionMRF_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_IDITEM = 0L;
			P_OUT_NOMBREITEM = "";
			P_OUT_OPERACIONES = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public static class GrupoOperacion {
		public String grupo;
		public String tipoOperacion;
		public String SumaResta;
		public Long IdMetodo;

		public GrupoOperacion(String grupo, String tipoOperacion, String sumaResta, Long IdMetodo) {
			this.grupo = grupo;
			this.tipoOperacion = tipoOperacion;
			this.SumaResta = sumaResta;
			this.IdMetodo = IdMetodo;
		}
	}

	public static class PosicionMRF {
		public Long IdMetodo;
		public String NombreMetodo;
		public String Glosa;
		public String IdTipoReajuste;
		public Long IdMesa;
		public String Nombre;
		public Long IdPortafolio;
		public Long IdEvento;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoEvento;
		public String NombreEvento;
		public String IndAccion;
		public String IdAgrupacion;
		public String NombreAgrupacion;
		public String IdTipoComprobante;
		public String IdTipoEstado;
		public BigDecimal MtoEvento;
		public BigDecimal MtoSuma;
		public BigDecimal MtoResta;
		public Long OrdenItem;
		public Long OrdenGrupo;
		public String IdTipoPosicion;
		public Long IdMatriz;
	}

	public static class MetodoEventoPosicion {
		public Long IdMatriz;
		public Long IdMetodo;
		public String NombreMetodo;
		public Long IdEvento;
		public String NombreEvento;
		public String Agrupacion;
		public String IndAccion;
	}

	public class QueryConfig {
	    public String consulta;
	    public HashMap<String, Object> filtros;

	    public QueryConfig(String consulta, HashMap<String, Object> filtros) {
	        this.consulta = consulta;
	        this.filtros = filtros;
	    }
	}

	public ConsultarDetallePosicionMRF_Salida logica(Connection conn, ConsultarDetallePosicionMRF_Entrada entrada)
			throws Exception {
		logger.info("INICIO::MRF.ConsultarDetallePosicionMRF");

		ConsultarDetallePosicionMRF_Salida salida = new ConsultarDetallePosicionMRF_Salida();
		List<Operacion> operaciones = new ArrayList<Operacion>();

		logger.debug("Inicio SELECT_POSICION_MRF");
		List<PosicionMRF> posicionMRF = new ArrayList<PosicionMRF>();

		HashMap<String, Object> filtrosConsulta = new HashMap<>();
		filtrosConsulta.put("p_IdMetodo", entrada.p_IdMetodo);

		posicionMRF = _sql.query(conn, SELECT_POSICION_MRF, filtrosConsulta, PosicionMRF.class);

		logger.debug("Fin SELECT_POSICION_MRF");

		if (posicionMRF.size() == 0) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES
					.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "SIN_POSICIONES", "SIN_POSICIONES"));
			return salida;
		}

		operaciones = obtenerData(conn, entrada, posicionMRF);
		if (operaciones == null) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "POSICION_INVALIDA", "POSICION_INVALIDA"));
			return salida;
		}

		// CONSTRUYE SALIDA
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "CONSULTA_DETALLE_POSICIONES_OK",
				"CONSULTA_DETALLE_POSICIONES_OK"));
		salida.P_OUT_OPERACIONES = operaciones;
		logger.info("FIN::MRF.ConsultarPosicionMRF");
		return salida;

	}

	public List<Operacion> obtenerData(Connection conn, ConsultarDetallePosicionMRF_Entrada entrada,
			List<PosicionMRF> posicionMRF) throws SQLException {
		logger.debug("Inicio Calculo Operaciones Posicion");
		List<PosicionMRF> posicionesFiltradas;
		List<Operacion> operaciones = new ArrayList<Operacion>();

		if (entrada.p_TipoPosicion.equals("DET")) {
			posicionesFiltradas = posicionMRF.stream()
					.filter(posicion -> entrada.p_NombreEvento.equals(posicion.NombreEvento))
					.collect(Collectors.toList());
		} else {
			posicionesFiltradas = posicionMRF.stream()
					.filter(posicion -> entrada.p_NombreEvento.equals(posicion.NombreAgrupacion))
					.collect(Collectors.toList());
		}

		for (PosicionMRF posicion : posicionesFiltradas) {
			QueryConfig queryConfig = obtenerQuery(posicion,entrada,conn);			
			if (queryConfig != null) {
			    List<Operacion> operacionesFiltradas = _sql.query(conn, queryConfig.consulta, queryConfig.filtros, Operacion.class);
			    operaciones.addAll(operacionesFiltradas);
			    if (posicion.NombreEvento.equals("POSICION INICIAL")) {			    	
						ConsultarResumenPosicionMRF_Entrada entradaResumen = new ConsultarResumenPosicionMRF_Entrada();
						entradaResumen.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaResumen.p_IdMesa = posicion.IdMesa;
						entradaResumen.p_IdPais = entrada.p_IdPais;
						entradaResumen.p_IdTipoPosicion = "INICIAL";
						entradaResumen.p_FechaPosicion = entrada.p_FechaPosicion;					
						entradaResumen.p_IdUsuario = entrada.p_IdUsuario;
						
						ConsultarResumenPosicionMRF resumen = new ConsultarResumenPosicionMRF();
						try {
							ConsultarResumenPosicionMRF_Salida salidaResumen = resumen.logica(conn, entradaResumen);							
							Operacion posInicial = new Operacion();
							
					    	posInicial.Agrupacion = posicion.NombreAgrupacion;
					    	posInicial.DocumentoIdentidad = "-";
					    	posInicial.FechaComprobante = entrada.p_FechaPosicion; 
					    	posInicial.FechaLiquidacion = entrada.p_FechaPosicion;
					    	posInicial.FolioComprobante = 0L;
					    	posInicial.MontoLiquidacionFinal = salidaResumen.P_OUT_RESUMENPOSICION.get(0).PosicionInicial;
					    	posInicial.SumaResta = posicion.IndAccion;
					    	posInicial.TipoOperacion = posicion.NombreEvento;
					    	salidaResumen.P_OUT_EVENTOS.forEach(eventoPosicion -> {if ("M".equals(eventoPosicion.IdTipoEvento)) {
							    posInicial.MontoLiquidacionFinal = posInicial.MontoLiquidacionFinal.subtract(eventoPosicion.MtoEvento);
					    	}});
					    	operaciones.add(posInicial);
					    	
					    	
							
						} catch (Exception e) {
							e.printStackTrace();
						}					
			    }
			}	else {
				continue;
			}				
		}
		logger.debug("Fin Calculo Operaciones Posicion");
		return operaciones;
	}

    public QueryConfig obtenerQuery(PosicionMRF posicion, ConsultarDetallePosicionMRF_Entrada entrada, Connection conn) {
		HashMap<String, Object> filtrosConsulta = new HashMap<>();
		filtrosConsulta.put("p_FechaPosicion", entrada.p_FechaPosicion);

		if (posicion.IdTipoProducto.equals("MERF")) {
            filtrosConsulta.put("p_IdTipoProducto", posicion.IdTipoProducto);
            filtrosConsulta.put("p_IdTipoOperacion", posicion.IdTipoOperacion);
            filtrosConsulta.put("p_IdTipoReajuste", posicion.IdTipoReajuste);
            filtrosConsulta.put("p_IdMesa", posicion.IdMesa);
            filtrosConsulta.put("p_IdTipoEstado", posicion.IdTipoEstado);
			if (posicion.IdTipoEstado.equals("OPE")) {				
                return new QueryConfig(SELECT_OPERACIONES_MRF_FECHAPOSICION, filtrosConsulta);
			} else if (posicion.IdTipoEstado.equals("ANT")) {
				return new QueryConfig(SELECT_ANTICIPOS_OPERACIONES_MRF_FECHAPOSICION, filtrosConsulta);				
			} else if (posicion.IdTipoEstado.equals("LIQ")) {
                return new QueryConfig(SELECT_VENCIMIENTOS_MRF_FECHAPOSICION, filtrosConsulta);				
			}
		} else if (posicion.IdTipoProducto.equals("INSI")) {
			filtrosConsulta.put("p_IdTipoProducto", posicion.IdTipoProducto);
            filtrosConsulta.put("p_IdTipoOperacion", posicion.IdTipoOperacion);
            filtrosConsulta.put("p_IdPortafolio", posicion.IdPortafolio);

			if (posicion.IdTipoEstado.equals("OPE")) {
				filtrosConsulta.put("p_IdTipoEstado", posicion.IdTipoEstado);	            
				return new QueryConfig(SELECT_FAC_SIM_CONTADO_MRF_FECHAPOSICION, filtrosConsulta);				
			} else if (posicion.IdTipoEstado.equals("ANT")) {
				filtrosConsulta.put("p_IdTipoEstado", posicion.IdTipoEstado);	            
				return new QueryConfig(SELECT_FAC_SIM_CONTADO_MRF_FECHAPOSICION, filtrosConsulta);				
			} else if (posicion.IdTipoEstado.equals("LIQ")) {
	            filtrosConsulta.put("p_IdTipoEstado", "OPE");
	            return new QueryConfig(SELECT_FAC_PM_MRF_FECHAPOSICION, filtrosConsulta);				
			}
		} else if (posicion.IdTipoProducto.equals("INIF")) {
			filtrosConsulta.put("p_IdTipoProducto", posicion.IdTipoProducto);
            filtrosConsulta.put("p_IdTipoOperacion", posicion.IdTipoOperacion);
            filtrosConsulta.put("p_IdPortafolio", posicion.IdPortafolio);
            filtrosConsulta.put("p_IdTipoEstado", posicion.IdTipoEstado);
			if (posicion.IdTipoEstado.equals("OPE")) {
				return new QueryConfig(SELECT_FAC_SIM_CONTADO_MRF_FECHAPOSICION, filtrosConsulta);			
			} else if (posicion.IdTipoEstado.equals("LIQ")) {
				return new QueryConfig(SELECT_FAC_PM_MRF_FECHAPOSICION, filtrosConsulta);			
			}
		} else if (posicion.IdTipoProducto.equals("INRF")) {
			filtrosConsulta.put("p_IdTipoProducto", posicion.IdTipoProducto);
            filtrosConsulta.put("p_IdTipoOperacion", posicion.IdTipoOperacion);
            filtrosConsulta.put("p_IdPortafolio", posicion.IdPortafolio);
            filtrosConsulta.put("p_IdTipoEstado", posicion.IdTipoEstado);
			if (posicion.IdTipoEstado.equals("OPE")) {
				return new QueryConfig(SELECT_FAC_SIM_CONTADO_MRF_FECHAPOSICION, filtrosConsulta);			
			} else if (posicion.IdTipoEstado.equals("LIQ")) {
				return new QueryConfig(SELECT_FAC_PM_MRF_FECHAPOSICION, filtrosConsulta);			
			}
		} else if (posicion.IdTipoProducto.equals("MEME")) {
			if (posicion.IdTipoEstado.equals("OPE")) {
				// operaciones manuales, no se muestran en la grilla
				filtrosConsulta.put("p_IdTipoOperacion", posicion.IdTipoOperacion);
				filtrosConsulta.put("p_IdMetodo", entrada.p_IdMetodo);
				return new QueryConfig(SELECT_FACTURAS_SPOT_T0, filtrosConsulta);	
			} else if (posicion.IdTipoEstado.equals("LIQ")) {
				filtrosConsulta.put("p_IdTipoOperacion", posicion.IdTipoOperacion);
				filtrosConsulta.put("p_IdMetodo", entrada.p_IdMetodo);
				return new QueryConfig(SELECT_FACTURAS_SPOT, filtrosConsulta);			
			}
		} else if (posicion.IdTipoProducto.equals("")) {
			if (posicion.IdTipoEstado.equals("INI")) {
				filtrosConsulta.put("IdTipoPosicion", posicion.IdTipoPosicion);
				return new QueryConfig(SELECT_EVENTOS_MANUALES_MRF_FECHAPOSICION, filtrosConsulta);			

			}
		} else if (posicion.IdTipoProducto.equals("CUST")) {
            filtrosConsulta.put("p_IdTipoReajuste", posicion.IdTipoReajuste);
            filtrosConsulta.put("p_IdPortafolio", posicion.IdPortafolio);
			if (posicion.IdTipoEstado.equals("LIQ")) {
				return new QueryConfig(SELECT_VENCIMIENTO_TITULOS_MRF_FECHAPOSICION, filtrosConsulta);				
			}
		} else if (posicion.IdTipoProducto.equals("TESO")) {
			if (posicion.IdTipoEstado.equals("MAN")) {
				//no se muestra en la grilla, pero si afecta el item posicion inicial en la posicion diaria.
			}
		} else if (_TiposOperacionRV.contains(posicion.IdTipoProducto)) {
			filtrosConsulta.put("p_IdTipoProducto", posicion.IdTipoProducto);
			filtrosConsulta.put("p_IdTipoOperacion", posicion.IdTipoOperacion);
			filtrosConsulta.put("p_IdPortafolio", posicion.IdPortafolio);
            filtrosConsulta.put("p_IdTipoEstado", posicion.IdTipoEstado);
			
			return new QueryConfig(SELECT_FAC_SIM_CONTADO_MRF_FECHAPOSICION, filtrosConsulta);
		}
		return null;
	}


	static class Metodo {
		String IdMetodo;
		String NombreMetodo;

		public Metodo(String IdMetodo, String NombreMetodo) {
			this.IdMetodo = IdMetodo;
			this.NombreMetodo = NombreMetodo;
		}
	}

	static class InputData {

		public int IdEmpresa;
		public int IdUsuario;
		public String IdModulo;
		public long IdPais;
		public String FechaPosicion;
		public String NombreItem;
		public String NombreEvento;
		public String NombreMetodo;
		public String IdMetodo;
		public String TipoPosicion;

	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST =="); //
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			
			Gson gson = new Gson();
			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 97,\r\n"
					+ "    \"p_IdModulo\": \"MRF\",\r\n"
					+ "    \"p_IdPais\": 44,\r\n"
					+ "    \"p_FechaPosicion\": \"2025-07-01\",\r\n"
					+ "    \"p_NombreItem\": \"PACTOS\",\r\n"
					+ "    \"p_NombreEvento\": \"ANTICIPO COMPRA CON PACTO\",\r\n"
					+ "    \"p_IdMetodo\": 2,\r\n"
					+ "    \"p_NombreMetodo\": \"FLUJO CAJA MRF\",\r\n"
					+ "    \"p_TipoPosicion\": \"DET\"\r\n"
					+ "}";
			
			String json2 = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 97,\r\n"
					+ "    \"p_IdModulo\": \"MRF\",\r\n"
					+ "    \"p_IdPais\": 44,\r\n"
					+ "    \"p_FechaPosicion\": \"2025-07-02\",\r\n"
					+ "    \"p_NombreItem\": \"RENTA VARIABLE\",\r\n"
					+ "    \"p_NombreEvento\": \"VENTA ACN\",\r\n"
					+ "    \"p_IdMetodo\": 2,\r\n"
					+ "    \"p_NombreMetodo\": \"FLUJO CAJA MRF\",\r\n"
					+ "    \"p_TipoPosicion\": \"DET\"\r\n"
					+ "}";
			ConsultarDetallePosicionMRF_Entrada objIn = gson.fromJson(json2,ConsultarDetallePosicionMRF_Entrada.class);
			
			ConsultarDetallePosicionMRF demo = new ConsultarDetallePosicionMRF();
			System.out.println("ENTRADA");
			System.out.println(objIn.toString());
			System.out.println("SALIDA");
			try {
				ConsultarDetallePosicionMRF_Salida ObjOut = demo.logica(con, objIn);
				// con.rollback();
				con.rollback();
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				System.out.println(er.toString());
				con.rollback();
			}
			// con.commit();

		}
	}
}
