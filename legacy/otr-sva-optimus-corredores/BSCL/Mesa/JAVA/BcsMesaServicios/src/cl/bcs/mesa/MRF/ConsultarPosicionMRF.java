package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MRF.ConsultarResumenPosicionMRF.ConsultarResumenPosicionMRF_Entrada;
import cl.bcs.mesa.MRF.ConsultarResumenPosicionMRF.ConsultarResumenPosicionMRF_Salida;
import cl.bcs.mesa.MRF.pojo.CacheMRF;
import cl.bcs.plataforma.pla.utils.CacheServicio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarPosicionMRF implements JavaCall {	
	
	private static final Logger logger = LogManager.getLogger(ConsultarPosicionMRF.class);
	
	private final String SELECT_POSICION_MRF  = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectPosicionMRF.sql");
	private final String SELECT_POSICION_MRF_INICIAL  = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectPosicionMRFInicial.sql");
	private final String SELECT_OPERACIONES_MRF_FECHAPOSICION  = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectOperacionesRFFechaPosicion.sql");
	private final String SELECT_ANTICIPOS_OPERACIONES_MRF_FECHAPOSICION  = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectAnticiposOperacionesRFFechaPosicion.sql");
	private final String SELECT_FAC_SIM_CONTADO_MRF_FECHAPOSICION  = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectFacSimContadoRFFechaPosicion.sql");
	
	private final String SELECT_EVENTOS_MANUALES_MRF_FECHAPOSICION  = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectEventosManualesRFFechaPosicion.sql");
	
	private final String SELECT_VENCIMIENTOS_MRF_FECHAPOSICION  = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectVencimientosOperacionesRFFechaPosicion.sql");
	private final String SELECT_FAC_PM_MRF_FECHAPOSICION  = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectFacPmRFFechaPosicion.sql");
	private final String SELECT_VENCIMIENTS_TITULOS_MRF_FECHAPOSICION  = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectVencimientosTitulosRFFechaPosicion.sql");
	private final String SELECT_FACTURAS_SPOT = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectFacturasSpot.sql");
	private final String SELECT_FACTURAS_SPOT_T0 = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectFacturasSpotT0.sql");
	
	public final List<String> _TiposOperacionRV = Arrays.asList("INAC", "INAX", "INMI", "INCI", "INCX", "INEX", "INEN", "INMO", "INOS");
	
	private static final CacheServicio CACHE = CacheServicio.registrar(CacheMRF.POSICION, CacheMRF.TTL_POSICION_MS, CacheMRF.TAG_POSICION);
	
	private SQL _sql = new SQL();
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarPosicionMRF_Entrada jin = gson.fromJson(in.toString(), ConsultarPosicionMRF_Entrada.class);
		String clave = CacheServicio.clave(jin.p_IdEmpresa, jin.p_IdPais, jin.p_IdMetodo, jin.p_NombreMetodo, jin.p_IdModulo, jin.p_FechaPosicion, jin.p_TipoPosicion);
		String json = CACHE.obtener(clave, () -> {
		    ConsultarPosicionMRF_Salida out = logica(conn, jin);
		    out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		    return gson.toJson(out);
		});
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(json).getAsJsonObject();
		return dataout;
	}

	public static class ConsultarPosicionMRF_Entrada extends BcsPojo{
		public Long p_IdEmpresa;
		public Long	p_IdUsuario;
		public Long	p_IdPais;
		public Long	p_IdMetodo;
		public String p_NombreMetodo;
		public String p_IdModulo;
		public String p_FechaPosicion;
		public String p_TipoPosicion;
		
		public ConsultarPosicionMRF_Entrada(){
			p_IdEmpresa = 0L;
			p_IdUsuario = 0L;
			p_IdPais = 0L;
			p_IdMetodo = 0L;
			p_NombreMetodo = "";
			p_IdModulo = "";
			p_FechaPosicion = "";
			p_TipoPosicion = "";
					
		}
	}
	
	public static class PosicionMRF_Salida extends BcsPojo{
		public BigDecimal Debe;
		public BigDecimal Haber;
		public Long IdEvento;
		public Long IdMesa;
		public Long IdMetodo;
		public String IdTipoEvento;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public String IdTipoReajuste;
		public String IndAccion;
		public String NomMesa;
		public String NombreAgrupacion;
		public String NombreEvento;
		public String NombreMetodo;
	}
	
	public static class OperacionesMRF_Salida extends BcsPojo{
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdEstado;
		public Long IdPortafolio; // Solo lo devuelven las queries de Facturas (batch); en el resto queda null
		public BigDecimal MtoEvento;
	}

	public static class OperacionesManualesMRF_Salida extends BcsPojo{
		public Long IdPosicion;
		public String Fecha;
		public Long IdMatriz; 
		public BigDecimal MontoEvento;
	}
	
	public static class ConsultarPosicionMRF_Salida extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		public Long	P_OUT_IDMESA;
		public BigDecimal	P_OUT_POSICIONFINAL;
		public String	P_OUT_NOMBREMESA;
		public List<PosicionMRF_Salida> P_OUT_POSICION;
		
		public ConsultarPosicionMRF_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_IDMESA = 0L;
			P_OUT_POSICIONFINAL = new BigDecimal(0);
			P_OUT_NOMBREMESA = "";
			P_OUT_POSICION = new ArrayList<>();
		}
	}
	
	public static class PosicionMRF extends BcsPojo{
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
	
	public static class PosicionSpot extends BcsPojo{
	    public BigDecimal total;
	}
	
	
	public ConsultarPosicionMRF_Salida logica(Connection conn, ConsultarPosicionMRF_Entrada entrada) throws Exception {
		logger.info("INICIO::MRF.ConsultarPosicionMRF");

		//La UI envia p_FechaPosicion como datetime ISO completo (ej. "2026-08-18T04:00:00.000Z");
		//las queries esperan solo 'YYYY-MM-DD'. Se normaliza una sola vez al inicio del servicio
		//para que ambos formatos (fecha simple o datetime ISO) funcionen igual en todo el resto del calculo.
		entrada.p_FechaPosicion = normalizarFecha(entrada.p_FechaPosicion);

		ConsultarPosicionMRF_Salida salida = new ConsultarPosicionMRF_Salida();
		
		HashMap<String,Object> filtrosConsulta = new HashMap<>();
		filtrosConsulta.put("p_IdMetodo", entrada.p_IdMetodo);
		logger.debug("Inicio SELECT_POSICION_MRF");
		List<PosicionMRF> posicionMRF = new ArrayList<PosicionMRF>();
		if (entrada.p_NombreMetodo.equalsIgnoreCase("POSICION INICIAL MRF")) {
			posicionMRF = _sql.query(conn, SELECT_POSICION_MRF_INICIAL, filtrosConsulta , PosicionMRF.class);
		}else {
			posicionMRF = _sql.query(conn, SELECT_POSICION_MRF, filtrosConsulta, PosicionMRF.class);
		}
		
		
		logger.debug("Fin SELECT_POSICION_MRF");
		
		if(posicionMRF.size() == 0){
			salida.P_OUT_ERRORESCONTADOR = 1L;
			//salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SIN_POSICIONES",null,conn,entrada.p_IdEmpresa));		
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SIN_POSICIONES","SIN_POSICIONES"));				
			return salida;
		}

		
		calculaPosicion(conn, entrada, posicionMRF);


		
		//CONSTRUYE SALIDA
		salida.P_OUT_ERRORESCONTADOR = 0L;
		//salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CONSULTA_POSICIONES_OK",null,conn,entrada.p_IdEmpresa));
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CONSULTA_POSICIONES_OK","CONSULTA_POSICIONES_OK"));
		
		logger.debug("Obtiene IdMesa");
		//P_OUT_IDMESA
		List<Long> idMesa = posicionMRF.stream().filter(a -> a.IdMesa != null).map(e -> e.IdMesa).distinct().collect(Collectors.toList());
		salida.P_OUT_IDMESA = idMesa.get(0);
		
		logger.debug("Obtiene NombreMesa");
		//P_OUT_NOMBREMESA
		List<String> nombreMesa = posicionMRF.stream().filter(a -> a.Nombre != null).map(e -> e.Nombre).distinct().collect(Collectors.toList());
		salida.P_OUT_NOMBREMESA = nombreMesa.get(0);
		
		logger.debug("Obtiene PosicionFinal");
		//P_OUT_POSICIONFINAL
		BigDecimal SUM_MtoSuma = posicionMRF.stream().map(a -> a.MtoSuma).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal SUM_MtoResta = posicionMRF.stream().map(a -> a.MtoResta).reduce(BigDecimal.ZERO, BigDecimal::add);
		salida.P_OUT_POSICIONFINAL = SUM_MtoSuma.subtract(SUM_MtoResta);
		
		//P_OUT_POSICION
		
	


		List<PosicionMRF_Salida> detallePosicionMRF = new ArrayList<PosicionMRF_Salida>();
		
		logger.debug("Inicio Armar detalle posicion");
		
		for(PosicionMRF posicion: posicionMRF){
			PosicionMRF_Salida detallePosicion = new PosicionMRF_Salida();
			detallePosicion.IdMetodo = posicion.IdMetodo;
			detallePosicion.NombreMetodo = posicion.NombreMetodo;
			detallePosicion.IdTipoReajuste = posicion.IdTipoReajuste;
			detallePosicion.IdMesa = posicion.IdMesa;
			detallePosicion.NomMesa = posicion.Nombre;
			detallePosicion.IdEvento = posicion.IdEvento;
			detallePosicion.IdTipoProducto = posicion.IdTipoProducto;
			detallePosicion.IdTipoOperacion = posicion.IdTipoOperacion;
			detallePosicion.IdTipoEvento = posicion.IdTipoEvento;
			detallePosicion.NombreAgrupacion = posicion.NombreAgrupacion;
			detallePosicion.NombreEvento = posicion.NombreEvento;
			detallePosicion.IndAccion = posicion.IndAccion;
			detallePosicion.Debe = posicion.IndAccion.equals("R")?posicion.MtoEvento:new BigDecimal(0);
			detallePosicion.Haber = posicion.IndAccion.equals("S")?posicion.MtoEvento:new BigDecimal(0);
			detallePosicionMRF.add(detallePosicion);
		}

		logger.debug("Fin Armar detalle posicion");		
		
		//CUANDO SE SOLICITA DETALLE
		salida.P_OUT_POSICION = detallePosicionMRF;
		
		//CUANDO SE SOLICITA RESUMEN
		if(entrada.p_TipoPosicion.equals("RSM")) {
			logger.debug("Inicio Armar resumen posicion");
			List<PosicionMRF_Salida> resumenPosicionMRF = new ArrayList<PosicionMRF_Salida>();
			
			detallePosicionMRF.stream().collect(Collectors.groupingBy(a -> a.NombreAgrupacion, Collectors.mapping(a -> a.Debe, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)))).forEach((nombre,sumMontDebe) -> {
				PosicionMRF_Salida resumenPosicion = new PosicionMRF_Salida();
				resumenPosicion.IdMetodo = -1L;
				resumenPosicion.NombreMetodo = "RSM";
				resumenPosicion.IdTipoReajuste = "RSM";
				resumenPosicion.IdMesa = 0L;
				resumenPosicion.NomMesa = "RSM";
				resumenPosicion.IdEvento = 0L;
				resumenPosicion.IdTipoProducto = "RSM";
				resumenPosicion.IdTipoOperacion = "RSM";
				resumenPosicion.IdTipoEvento = "";
				resumenPosicion.NombreAgrupacion = "";
				resumenPosicion.NombreEvento = nombre;
				resumenPosicion.IndAccion = "";			
				resumenPosicion.Debe = sumMontDebe;
				resumenPosicion.Haber = new BigDecimal(0L);
				resumenPosicionMRF.add(resumenPosicion);
			});
			
			detallePosicionMRF.stream().collect(Collectors.groupingBy(a -> a.NombreAgrupacion, Collectors.mapping(a -> a.Haber, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)))).forEach((nombre,sumMontHaber) -> {
				resumenPosicionMRF.stream().filter(a -> a.NombreEvento.equals(nombre)).forEach((pos) -> {
					pos.Haber = sumMontHaber;
				});
			});
			
			salida.P_OUT_POSICION = resumenPosicionMRF;
			logger.debug("Fin Armar resumen posicion");
		}
		
		logger.info("FIN::MRF.ConsultarPosicionMRF");
		return salida;	
		
	}
	
	/**
	 * Se queda solo con los primeros 10 caracteres ('YYYY-MM-DD'), que es la posicion fija de la fecha
	 * tanto en un datetime ISO completo ("2026-08-18T04:00:00.000Z", formato real que manda la UI)
	 * como en una fecha simple ("2026-08-18", formato usado en pruebas). Si viene mas corto o vacio,
	 * se deja tal cual para no enmascarar un valor invalido.
	 */
	private static String normalizarFecha(String fecha) {
		if (fecha != null && fecha.length() >= 10) {
			return fecha.substring(0, 10);
		}
		return fecha;
	}

	public List<PosicionMRF> calculaPosicion(Connection conn, ConsultarPosicionMRF_Entrada entrada, List<PosicionMRF> posicionMRF) throws SQLException {
		logger.debug("Inicio Calculo Montos Posicion");
		HashMap<String,Object> filtrosConsulta = new HashMap<>();

		//Precarga en batch de Facturas (evita 1 query por posicion, ver precargaFacturas).
		//Se omite cada precarga si ninguna posicion puede llegar a la rama que la usa (Sim.Contado: no-INICIAL, PM: INICIAL),
		//para no pagar una query completa cuando el resultado nunca se va a consultar.
		boolean hayPosicionesNoInicial = posicionMRF.stream().anyMatch(p -> !"INICIAL".equals(p.IdTipoPosicion));
		boolean hayPosicionesInicial = posicionMRF.stream().anyMatch(p -> "INICIAL".equals(p.IdTipoPosicion));

		Map<String, List<OperacionesMRF_Salida>> facSimContadoPorClave = hayPosicionesNoInicial
				? precargaFacturas(conn, entrada, posicionMRF, SELECT_FAC_SIM_CONTADO_MRF_FECHAPOSICION, "Facturas")
				: new HashMap<>();
		Map<String, List<OperacionesMRF_Salida>> facPmPorClave = hayPosicionesInicial
				? precargaFacturas(conn, entrada, posicionMRF, SELECT_FAC_PM_MRF_FECHAPOSICION, "Facturas PM")
				: new HashMap<>();

		for(PosicionMRF posicion: posicionMRF){
			//Renta Fija (Desde la Mesa)
			if(posicion.IdTipoProducto.equals("MERF") 
					&& (posicion.IdTipoEstado.equals("OPE") || posicion.IdTipoEstado.equals("ANT"))
					&& (!posicion.IdTipoPosicion.equals("INICIAL"))) {
				filtrosConsulta = new HashMap<>();
				filtrosConsulta.put("p_FechaPosicion", entrada.p_FechaPosicion);
				filtrosConsulta.put("IdTipoProducto", posicion.IdTipoProducto);
				filtrosConsulta.put("IdTipoOperacion", posicion.IdTipoOperacion);
				filtrosConsulta.put("IdTipoReajuste", posicion.IdTipoReajuste);
				filtrosConsulta.put("IdMesa", posicion.IdMesa);
				
				
				
				//OPERACIONES DEL DIA
				logger.debug("Inicio Query Operaciones MRF");
				List<OperacionesMRF_Salida> operacionesMRF = _sql.query(conn, SELECT_OPERACIONES_MRF_FECHAPOSICION, filtrosConsulta, OperacionesMRF_Salida.class);
				logger.debug("Fin Query Operaciones MRF");
				if(!operacionesMRF.isEmpty() && posicion.IdTipoEstado.equals("OPE")) {
					posicion = incluyeMontoEvento(posicion, operacionesMRF);
					continue;
				}
								
				//ANTICIPOS
				logger.debug("Inicio Query Anticipos MRF");
				List<OperacionesMRF_Salida> anticiposOperacionesMRF = _sql.query(conn, SELECT_ANTICIPOS_OPERACIONES_MRF_FECHAPOSICION, filtrosConsulta, OperacionesMRF_Salida.class);
				logger.debug("Fin Query Anticipos MRF");
				if(!anticiposOperacionesMRF.isEmpty() && posicion.IdTipoEstado.equals("ANT")) {
					posicion = incluyeMontoEvento(posicion, anticiposOperacionesMRF);
					continue;
				}
			}
			
			//Facturas
			else if(("INSI".equals(posicion.IdTipoProducto) || "INIF".equals(posicion.IdTipoProducto) || "INRF".equals(posicion.IdTipoProducto) || _TiposOperacionRV.contains(posicion.IdTipoProducto)) 
					&& (("OPE".equals(posicion.IdTipoEstado) || "ANT".equals(posicion.IdTipoEstado)) || ("OPE".equals(posicion.IdTipoEstado) || "LIQ".equals(posicion.IdTipoEstado)))
					&& (!"INICIAL".equals(posicion.IdTipoPosicion))) {
				//Facturas Simultaneas CONTADO (precargada en batch antes del loop, ver precargaFacturas)
				List<OperacionesMRF_Salida> facSimContado = facSimContadoPorClave.getOrDefault(claveFacturas(posicion), Collections.emptyList());

				if(!facSimContado.isEmpty() && (posicion.IdTipoEstado.equals("OPE") || posicion.IdTipoEstado.equals("ANT"))) {
					posicion = incluyeMontoEventoConFiltroComprobante(posicion, facSimContado);
					continue;
				} 
			}
			else if (posicion.IdTipoProducto.equals("MEME")&& (!posicion.IdTipoPosicion.equals("INICIAL")) ) {
		        List<PosicionSpot> posicionSpot = new ArrayList<>();
				filtrosConsulta = new HashMap<>();
				filtrosConsulta.put("p_FechaPosicion", entrada.p_FechaPosicion);
				filtrosConsulta.put("IdTipoOperacion", posicion.IdTipoOperacion);
				logger.debug("Inicio Query Spot");
				posicionSpot = _sql.query(conn, SELECT_FACTURAS_SPOT_T0, filtrosConsulta, PosicionSpot.class);
				logger.debug("Fin Query Spot");

				if (!posicionSpot.isEmpty() && posicion.IdTipoEstado.equals("OPE")) {
					posicion = incluyeFacturasSpot(posicion, posicionSpot, posicion.IdTipoOperacion);
					continue;
				}				
			}
			
			//Eventos Manuales (Ingresados por Usuario)
			else if(posicion.IdTipoComprobante.equals("MAN")
					&& (!posicion.IdTipoPosicion.equals("INICIAL"))) {
				filtrosConsulta = new HashMap<>();
				filtrosConsulta.put("p_FechaPosicion", entrada.p_FechaPosicion);
				filtrosConsulta.put("p_IdMatriz", posicion.IdMatriz);
				
				filtrosConsulta.put("IdTipoPosicion", "");
				logger.debug("Inicio Query Eventos Manuales");
				List<OperacionesManualesMRF_Salida> eventosManuales = _sql.query(conn, SELECT_EVENTOS_MANUALES_MRF_FECHAPOSICION, filtrosConsulta, OperacionesManualesMRF_Salida.class);
				logger.debug("Fin Query Eventos Manuales");
			
				if(!eventosManuales.isEmpty()) {
					posicion = incluyeMontoEventoConFiltroMatriz(posicion, eventosManuales);
					continue;
				}
				
				//2022-08-19 Si no tiene items ingresados manualmente, debe reflejar la posicion inicial calculada y afectar sobre el calculo de la posicion final
				if(posicion.IdAgrupacion.equals("INICIO")) {
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
						posicion.MtoEvento = posicion.MtoEvento.add(salidaResumen.P_OUT_RESUMENPOSICION.get(0).PosicionInicial);
						posicion.MtoSuma = posicion.MtoSuma.add(posicion.IndAccion.equals("S") ? salidaResumen.P_OUT_RESUMENPOSICION.get(0).PosicionInicial : new BigDecimal(0));
						posicion.MtoResta = posicion.MtoResta.add(posicion.IndAccion.equals("R") ? salidaResumen.P_OUT_RESUMENPOSICION.get(0).PosicionInicial : new BigDecimal(0));						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			//Eventos posicion Inicial
			else if (posicion.IdTipoPosicion.equals("INICIAL")) {
				//Eventos Manuales (Ingresados por Usuario) //08-08-2022 Prioritarios frente al calculado en posicion inicial
				filtrosConsulta = new HashMap<>();
				filtrosConsulta.put("p_FechaPosicion", entrada.p_FechaPosicion);
				filtrosConsulta.put("p_IdMatriz", posicion.IdMatriz);
			
				filtrosConsulta.put("IdTipoPosicion", "INICIAL");
				logger.debug("Inicio Query Eventos Manuales");
				List<OperacionesManualesMRF_Salida> eventosManuales = _sql.query(conn, SELECT_EVENTOS_MANUALES_MRF_FECHAPOSICION, filtrosConsulta, OperacionesManualesMRF_Salida.class);
				logger.debug("Fin Query Eventos Manuales");

				if(!eventosManuales.isEmpty()) {
					posicion = incluyeMontoEventoConFiltroMatriz(posicion, eventosManuales);
					continue;
				}
				
				//VENCIMIENTOS DE PACTOS
				else if(posicion.IdTipoProducto.equals("MERF")) {
					filtrosConsulta = new HashMap<>();
					filtrosConsulta.put("p_FechaPosicion", entrada.p_FechaPosicion);
					filtrosConsulta.put("IdTipoProducto", posicion.IdTipoProducto);
					filtrosConsulta.put("IdTipoOperacion", posicion.IdTipoOperacion);
					filtrosConsulta.put("IdTipoReajuste", posicion.IdTipoReajuste);
					filtrosConsulta.put("IdMesa", posicion.IdMesa);
					
					//OPERACIONES DEL DIA
					logger.debug("Inicio Query Vencimientos");
					List<OperacionesMRF_Salida> vencimientosMRF = _sql.query(conn, SELECT_VENCIMIENTOS_MRF_FECHAPOSICION, filtrosConsulta, OperacionesMRF_Salida.class);
					logger.debug("Fin Query Vencimientos");

					if(!vencimientosMRF.isEmpty() && posicion.IdTipoEstado.equals("LIQ")) {
						posicion = incluyeMontoEvento(posicion, vencimientosMRF);
						continue;
					}
				}
				
				//Facturas
				else if((posicion.IdTipoProducto.equals("INSI") || posicion.IdTipoProducto.equals("INIF") || posicion.IdTipoProducto.equals("INRF"))) {
					//Facturas PM (precargada en batch antes del loop, ver precargaFacturas)
					List<OperacionesMRF_Salida> facPM = facPmPorClave.getOrDefault(claveFacturas(posicion), Collections.emptyList());

					if(!facPM.isEmpty()) {
						posicion = incluyeMontoEventoConFiltroComprobante(posicion, facPM);
						continue;
					}
				}
				
				//VENCIMIENTOS DE TITULOS
				else if(posicion.IdTipoProducto.equals("CUST")) {
					filtrosConsulta = new HashMap<>();
					filtrosConsulta.put("p_FechaPosicion", entrada.p_FechaPosicion);
					filtrosConsulta.put("IdTipoReajuste", posicion.IdTipoReajuste);
					//filtrosConsulta.put("IdMesa", posicion.IdMesa);
					filtrosConsulta.put("IdPortafolio", posicion.IdPortafolio);
					
					//OPERACIONES DEL DIA
					logger.debug("Inicio Query Vencimiento Titulos");
					List<OperacionesMRF_Salida> vencimientosTitMRF = _sql.query(conn, SELECT_VENCIMIENTS_TITULOS_MRF_FECHAPOSICION, filtrosConsulta, OperacionesMRF_Salida.class);
					logger.debug("Fin Query Vencimiento Titulos");

					if(!vencimientosTitMRF.isEmpty() && posicion.IdTipoEstado.equals("LIQ")) {
						posicion = incluyeMontoEvento(posicion, vencimientosTitMRF);
						continue;
					}
				//Operaciones Spot
				} else if (posicion.IdTipoProducto.equals("MEME") && (posicion.IdTipoPosicion.equals("INICIAL"))) {
			        List<PosicionSpot> posicionSpot = new ArrayList<>();
					filtrosConsulta = new HashMap<>();
					filtrosConsulta.put("p_FechaPosicion", entrada.p_FechaPosicion);
					filtrosConsulta.put("IdTipoOperacion", posicion.IdTipoOperacion);
					
					logger.debug("Inicio Query Facturs Spot");
					posicionSpot = _sql.query(conn, SELECT_FACTURAS_SPOT, filtrosConsulta, PosicionSpot.class);
					logger.debug("Fin Query Facturs Spot");

					if (!posicionSpot.isEmpty() && posicion.IdTipoEstado.equals("LIQ")) {
						posicion = incluyeFacturasSpot(posicion, posicionSpot, posicion.IdTipoOperacion);
						continue;
					}				
				}
			}
		}
		logger.debug("Fin Calculo Montos Posicion");
		return posicionMRF;
	}
	
	
	/**
	 * Trae en una sola consulta las Facturas de todas las combinaciones IdTipoProducto/IdTipoOperacion/IdPortafolio
	 * presentes en posicionMRF, en vez de una consulta por cada posicion dentro del loop de calculaPosicion.
	 * FechaPosicion e IdMesa/IdTipoReajuste no varian dentro de una misma llamada (misma Mesa/Metodo), por eso
	 * no hace falta incluirlos en el batch. IdPortafolio si se incluye como lista y ademas la query agrega
	 * (GROUP BY) por el, para no mezclar montos de distintos portafolios bajo el mismo par producto/operacion.
	 */
	private Map<String, List<OperacionesMRF_Salida>> precargaFacturas(Connection conn, ConsultarPosicionMRF_Entrada entrada, List<PosicionMRF> posicionMRF, String sqlFacturas, String logLabel) throws SQLException {
		List<String> productos = new ArrayList<>(posicionMRF.stream().map(p -> p.IdTipoProducto).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
		List<String> operaciones = new ArrayList<>(posicionMRF.stream().map(p -> p.IdTipoOperacion).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
		List<Long> portafolios = new ArrayList<>(posicionMRF.stream().map(p -> p.IdPortafolio).filter(Objects::nonNull).distinct().collect(Collectors.toList()));

		if (productos.isEmpty() || operaciones.isEmpty() || portafolios.isEmpty()) {
			return new HashMap<>();
		}

		HashMap<String,Object> filtrosBatch = new HashMap<>();
		filtrosBatch.put("p_FechaPosicion", entrada.p_FechaPosicion);
		filtrosBatch.put("IdTipoProducto", productos);
		filtrosBatch.put("IdTipoOperacion", operaciones);
		filtrosBatch.put("IdPortafolio", portafolios);

		logger.debug("Inicio Query " + logLabel);
		List<OperacionesMRF_Salida> resultado = _sql.query(conn, sqlFacturas, filtrosBatch, OperacionesMRF_Salida.class);
		logger.debug("Fin Query " + logLabel);

		return resultado.stream().collect(Collectors.groupingBy(ConsultarPosicionMRF::claveFacturas));
	}

	/**
	 * Clave de agrupacion/lookup para el batch de Facturas: debe incluir IdPortafolio ademas de
	 * IdTipoProducto/IdTipoOperacion porque la query SQL ahora agrega (GROUP BY) tambien por portafolio
	 * -- si no coincidieran, se mezclarian montos de distintos portafolios bajo la misma clave.
	 */
	private static String claveFacturas(OperacionesMRF_Salida fila) {
		return fila.IdTipoProducto + "|" + fila.IdTipoOperacion + "|" + fila.IdPortafolio;
	}

	private static String claveFacturas(PosicionMRF posicion) {
		return posicion.IdTipoProducto + "|" + posicion.IdTipoOperacion + "|" + posicion.IdPortafolio;
	}

	private static PosicionMRF incluyeMontoEvento(PosicionMRF posicion,List<OperacionesMRF_Salida> operaciones){
		BigDecimal montoEvento = operaciones.stream().map(a -> a.MtoEvento).reduce(BigDecimal.ZERO, BigDecimal::add);
		posicion.MtoEvento = posicion.MtoEvento.add(montoEvento);
		posicion.MtoSuma = posicion.MtoSuma.add(posicion.IndAccion.equals("S") ? montoEvento : new BigDecimal(0));
		posicion.MtoResta = posicion.MtoResta.add(posicion.IndAccion.equals("R") ? montoEvento : new BigDecimal(0));

		return posicion;
	}
	
	private static PosicionMRF incluyeMontoEventoConFiltroComprobante(PosicionMRF posicion,List<OperacionesMRF_Salida> operaciones){
		BigDecimal montoEvento = operaciones.stream().filter(a -> a.IdEstado.equals(posicion.IdTipoComprobante)).map(a -> a.MtoEvento).reduce(BigDecimal.ZERO, BigDecimal::add);
		posicion.MtoEvento = posicion.MtoEvento.add(montoEvento);
		posicion.MtoSuma = posicion.MtoSuma.add(posicion.IndAccion.equals("S") ? montoEvento : new BigDecimal(0));
		posicion.MtoResta = posicion.MtoResta.add(posicion.IndAccion.equals("R") ? montoEvento : new BigDecimal(0));

		return posicion;
	}
	
	private static PosicionMRF incluyeMontoEventoConFiltroMatriz(PosicionMRF posicion,List<OperacionesManualesMRF_Salida> operaciones){
		BigDecimal montoEvento = operaciones.stream().filter(a -> a.IdMatriz.compareTo(posicion.IdMatriz) == 0).map(a -> a.MontoEvento).reduce(BigDecimal.ZERO, BigDecimal::add);
		posicion.MtoEvento = posicion.MtoEvento.add(montoEvento);
		posicion.MtoSuma = posicion.MtoSuma.add(posicion.IndAccion.equals("S") ? montoEvento : new BigDecimal(0));
		posicion.MtoResta = posicion.MtoResta.add(posicion.IndAccion.equals("R") ? montoEvento : new BigDecimal(0));

		return posicion;
	}
	
	private static PosicionMRF incluyeFacturasSpot(PosicionMRF posicion, List<PosicionSpot> operacionesSpot, String tipoOperacion) {		
        BigDecimal montoSpot = operacionesSpot.stream().map(a -> a.total).reduce(BigDecimal.ZERO, BigDecimal::add);
        posicion.MtoEvento = posicion.MtoEvento.add(montoSpot);
        posicion.MtoSuma = posicion.MtoSuma.add(posicion.IndAccion.equals("S") ? montoSpot : new BigDecimal(0));
		posicion.MtoResta = posicion.MtoResta.add(posicion.IndAccion.equals("R") ? montoSpot : new BigDecimal(0));
	    return posicion;
	}

	public static void main(String[] args) throws Exception  {
		System.out.println("== TEST ==");
		//try (Connection con = PlataformaUtils.getBCOESTADOPREConnection()) {
			try (Connection con = PlataformaUtils.getPropertiesConnection("becbpro1","connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
//			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1651,\"p_IdModulo\":\"MRF\",\"p_IdPais\":44,\"p_FechaPosicion\":\"2022-06-06T04:00:00.000Z\",\"p_IdMetodo\":1,\"p_TipoPosicion\":\"DET\"}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1651,\"p_IdModulo\":\"MRF\",\"p_IdPais\":44,\"p_FechaPosicion\":\"2022-05-30T04:00:00.000Z\",\"p_IdMetodo\":1,\"p_TipoPosicion\":\"DET\"}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":165115,\"p_IdModulo\":\"MRF\",\"p_IdPais\":44,\"p_FechaPosicion\":\"2026-08-17\",\"p_IdMetodo\":2,\"p_NombreMetodo\":\"FLUJO CAJA MRF\",\"p_TipoPosicion\":\"DIARIA\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1646,\"p_IdModulo\":\"MRF\",\"p_IdPais\":44,\"p_FechaPosicion\":\"2026-08-18T04:00:00.000Z\",\"p_IdMetodo\":1,\"p_NombreMetodo\":\"POSICION INICIAL MRF\",\"p_TipoPosicion\":\"RSM\"}";
			ConsultarPosicionMRF_Entrada Objin = gson.fromJson(json, ConsultarPosicionMRF_Entrada.class);
			ConsultarPosicionMRF demo = new ConsultarPosicionMRF();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ConsultarPosicionMRF_Salida ObjOut = demo.logica(con, Objin);
 				con.rollback();
// 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
 				System.out.println(er.toString());
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
	
}