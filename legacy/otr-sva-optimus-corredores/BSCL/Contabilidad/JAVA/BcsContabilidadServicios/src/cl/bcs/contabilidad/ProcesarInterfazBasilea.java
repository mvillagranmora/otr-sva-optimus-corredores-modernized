package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarCustodiaPortafolio.DatosCorredora;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarInterfazBasilea implements JavaCall {

	private final Logger log = LogManager.getLogger(ProcesarInterfazBasilea.class);
	
	private final String SQL_INSERT_INTERFAZ_BASILEA = loadResourceToString("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_InsertInterfazBasilea.sql");
	private final String SQL_SELECT_CUENTAS_BASILEA = loadResourceToString("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectCuentasBasilea.sql");
	private final String DATOS_CTA_PROPIA_SELEC = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultarCtaPropia.sql");
	
	private final String SQL_SELECT_VENTAS_CORTAS = loadResourceToString("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectVentasCortas.sql");
	private final String SQL_SELECT_ACCIONES = loadResourceToString("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectAcciones.sql");
	private final String SQL_SELECT_PACTOS_VIGENTES = loadResourceToString("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectPactosVigentes.sql");
	private final String SQL_SELECT_RF_IF = loadResourceToString("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectRfIf.sql");
	private final String SQL_SELECT_FORWARD = loadResourceToString("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectForward.sql");

	public static class EntradaProcesarInterfazBasilea extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
	}
	
	public static class ReplicaInterfazBasilea extends BcsPojo {
		public Long IdInterno;
		public String FechaEnvio;
		public String TipoProducto;
		public String RelacionContable;
		public String CuentaContable;
		public Long CodigoMoneda;
		public String NumeroOperacion;
		public Long TipoTasaInteres;
		public BigDecimal TasaInteresOperacion;
		public BigDecimal TasaInteresCompuestoAnual;
		public String TipoTasaBase;
		public BigDecimal Spread;
		public String CadenciaRepricing;
		public String CentroGestor;
		public String GestorLibro;
		public String EsTasaTecho;
		public String TipoTasaTecho;
		public BigDecimal TasaTecho;
		public String EsTasaPiso;
		public String TipoTasaPiso;
		public BigDecimal TasaPiso;
		public String OpcionPrepago;
		public String FechaOpcionPrepago;
		public String CodigoProducto;
		public String CodigoSubproducto;
		public String TipoCobertura;
		public String ClaseEmision;
		public Long Cartera;
		public String CodigoEstandardBono;
		public BigDecimal Strike;
		public String FechaVencimientoFinal;
		public String IndicadorCompensacion;
		public String PaisOrigen;
		public String DivisaCompensancion;
		public String FechaInicioOperacion;
		public String Carencia;
		public String TipoAmortizacion;
		public String EsAmortizacionEstandard;
		public String FrecuenciaPago;
		public String FechaRevisionTasa;
		public String TipoOpcion;
		public String TipoVentanaEjecucion;
		public BigDecimal ValorTirCompra;
		public BigDecimal ValorTirMercado;
		public Long IndicadorTipoVencimiento;
		public String IndicadorFlujo;
		public String FechaVencimientoFlujo;
		public String PlazoFlujo;
		public BigDecimal FlujoMonedaOrigen;
		public BigDecimal FlujoValorContable;
		public BigDecimal FlujoValorFinal;
		public String MonedaFlujo;
		public String IndicadorTipoMora;
		public BigDecimal SaldoCapitalMonedaOrigen;
		public BigDecimal SaldoCapitalValorContable;
		public BigDecimal SaldoCapitalValorFinal;
		public BigDecimal ReajusteCapital;
		public String MonedaDevengo;
		public BigDecimal InteresDevengado;
		public BigDecimal InteresDevengadoValorContable;
		public BigDecimal InteresDevengadoValorFinal;
		public BigDecimal CarteraVencidaMonedaOrigen;
		public BigDecimal CarteraVencidaValorContable;
		public BigDecimal CarteraVencidaValorFinal;
		public BigDecimal CastigosMonedaOrigen;
		public BigDecimal CastigosValorContable;
		public BigDecimal CastigosValorFinal;
		public String NumeroCliente;
		public String RutCliente;
		public String DigitoVerificador;
		public String IndicadorLibro;
		public String IndicadorTipoFlujo;
		public String IndicadorReversa;
		public String FechaRegistroOperacionSistema;
		public BigDecimal MontoOriginalOperacion;
		public BigDecimal ValorTasaCostoFondoOperacion;
		public String FechaPrimerFlujo;
		public String Filler;
		
		public ReplicaInterfazBasilea() {
			this.IdInterno = 0L;
			this.FechaEnvio = "1900-01-01";
			this.TipoProducto = "";
			this.RelacionContable = "";
			this.CuentaContable = "";
			this.CodigoMoneda = 0L;
			this.NumeroOperacion = "";
			this.TipoTasaInteres = 0L;
			this.TasaInteresOperacion = BigDecimal.ZERO;
			this.TasaInteresCompuestoAnual = BigDecimal.ZERO;
			this.TipoTasaBase = "";
			this.Spread = BigDecimal.ZERO;
			this.CadenciaRepricing = "";
			this.CentroGestor = "";
			this.GestorLibro = "";
			this.EsTasaTecho = "";
			this.TipoTasaTecho = "";
			this.TasaTecho = BigDecimal.ZERO;
			this.EsTasaPiso = "";
			this.TipoTasaPiso = "";
			this.TasaPiso = BigDecimal.ZERO;
			this.OpcionPrepago = "";
			this.FechaOpcionPrepago = "";
			this.CodigoProducto = "";
			this.CodigoSubproducto = "";
			this.TipoCobertura = "";
			this.ClaseEmision = "";
			this.Cartera = 0L;
			this.CodigoEstandardBono = "";
			this.Strike = BigDecimal.ZERO;
			this.FechaVencimientoFinal = "";
			this.IndicadorCompensacion = "";
			this.PaisOrigen = "";
			this.DivisaCompensancion = "";
			this.FechaInicioOperacion = "";
			this.Carencia = "";
			this.TipoAmortizacion = "";
			this.EsAmortizacionEstandard = "";
			this.FrecuenciaPago = "";
			this.FechaRevisionTasa = "";
			this.TipoOpcion = "";
			this.TipoVentanaEjecucion = "";
			this.ValorTirCompra = BigDecimal.ZERO;
			this.ValorTirMercado = BigDecimal.ZERO;
			this.IndicadorTipoVencimiento = 0L;
			this.IndicadorFlujo = "";
			this.FechaVencimientoFlujo = "";
			this.PlazoFlujo = "";
			this.FlujoMonedaOrigen = BigDecimal.ZERO;
			this.FlujoValorContable = BigDecimal.ZERO;
			this.FlujoValorFinal = BigDecimal.ZERO;
			this.MonedaFlujo = "";
			this.IndicadorTipoMora = "";
			this.SaldoCapitalMonedaOrigen = BigDecimal.ZERO;
			this.SaldoCapitalValorContable = BigDecimal.ZERO;
			this.SaldoCapitalValorFinal = BigDecimal.ZERO;
			this.ReajusteCapital = BigDecimal.ZERO;
			this.MonedaDevengo = "";
			this.InteresDevengado = BigDecimal.ZERO;
			this.InteresDevengadoValorContable = BigDecimal.ZERO;
			this.InteresDevengadoValorFinal = BigDecimal.ZERO;
			this.CarteraVencidaMonedaOrigen = BigDecimal.ZERO;
			this.CarteraVencidaValorContable = BigDecimal.ZERO;
			this.CarteraVencidaValorFinal = BigDecimal.ZERO;
			this.CastigosMonedaOrigen = BigDecimal.ZERO;
			this.CastigosValorContable = BigDecimal.ZERO;
			this.CastigosValorFinal = BigDecimal.ZERO;
			this.NumeroCliente = "";
			this.RutCliente = "";
			this.DigitoVerificador = "";
			this.IndicadorLibro = "";
			this.IndicadorTipoFlujo = "";
			this.IndicadorReversa = "";
			this.FechaRegistroOperacionSistema = "";
			this.MontoOriginalOperacion = BigDecimal.ZERO;
			this.ValorTasaCostoFondoOperacion = BigDecimal.ZERO;
			this.FechaPrimerFlujo = "";
			this.Filler = "";
		}
	}

	public static class  ObjDatosBusqueda extends BcsPojo {
		public String IdBolsa;
		public String EsVentaCorta;
		public String TipoCarteraCorredor;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdEmisor;
		public String Emisor;
		public String Nemotecnico;
		public String Grupo;
		public String Producto;
		public String IdMonedaInstrumento;
		public BigDecimal Monto;
		public String DocumentoIdentidadEmisor;
		public String IdTipoReajuste;
		public String FechaVencimiento;
		public String FechaVencimientoOperacion;
		public String FechaOperacion;
		public String FechaLiquidacion;
		public String Plazo;
		public String Folio;
		public BigDecimal Tasa;
		public String RelacionadoCorredora;
		public String IdTipoOperacion;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String NemoBasilea;
	}
	
	public static class CuentaBasilea extends BcsPojo {
		public String ConceptoAInformar;
		public String TipoCuentaContable;
		public String CuentaContable;
		public String NombreCuentaContable;
		public String IdBolsa;
		public String EsVentaCorta;
		public String RelacionadoCorredora;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdEmisor;
		public String Nemotecnico;
		public String Grupo;
		public String Producto;
		public String IdEstado;
		public String IdTipoOperacion;
	}
	
	public static class SalidaProcesarInterfazBasilea extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;

		public SalidaProcesarInterfazBasilea() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaProcesarInterfazBasilea jin = gson.fromJson(in.toString(), EntradaProcesarInterfazBasilea.class);
		SalidaProcesarInterfazBasilea out = logica(conn, jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaProcesarInterfazBasilea logica(Connection con, EntradaProcesarInterfazBasilea in) throws Exception {
		log.info("INICIO ProcesarInterfazBasilea");

		SalidaProcesarInterfazBasilea out = new SalidaProcesarInterfazBasilea();
		SQL sql = new SQL();
		List<ReplicaInterfazBasilea> ReplicaInterfazBasileaList = new ArrayList<>();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA","PLA.FechaSistema");
		HashMap<String, Object> params = new HashMap<String, Object>(2);

		List<CuentaBasilea> CuentasBasilea = sql.query(con, SQL_SELECT_CUENTAS_BASILEA, params, CuentaBasilea.class);

		params.put("IdEmpresa", in.p_IdEmpresa);
		List<DatosCorredora> DatosCtaPropia = sql.query(con, DATOS_CTA_PROPIA_SELEC, params, DatosCorredora.class);
		Long IdClienteCorredor = DatosCtaPropia.get(0).IdClienteCtaPropia;
		String RutCorredora = DatosCtaPropia.get(0).DocumentoIdentidadCtaPropia;
		
		params.clear();
		List<ObjDatosBusqueda> InterfazBasileaList = new ArrayList<>();
		params.put("IdCliente", IdClienteCorredor);
		InterfazBasileaList.addAll(sql.query(con, SQL_SELECT_ACCIONES, params, ObjDatosBusqueda.class));
		params.put("FechaSistema", fechaSistema);
		InterfazBasileaList.addAll(sql.query(con, SQL_SELECT_RF_IF, params, ObjDatosBusqueda.class));
		InterfazBasileaList.addAll(sql.query(con, SQL_SELECT_VENTAS_CORTAS, params, ObjDatosBusqueda.class));
		params.remove("IdCliente");
		InterfazBasileaList.addAll(sql.query(con, SQL_SELECT_PACTOS_VIGENTES, params, ObjDatosBusqueda.class));
		InterfazBasileaList.addAll(sql.query(con, SQL_SELECT_FORWARD, params, ObjDatosBusqueda.class));
		
		Map<String, MensajeSalida> mensajesSalida = new HashMap<>(5);

		for (ObjDatosBusqueda datosBusqueda : InterfazBasileaList) {
			String cuenta = buscarCuenta.apply(datosBusqueda, CuentasBasilea);
			if ("ERROR".equalsIgnoreCase(cuenta)) {
				out.P_OUT_ERRORESCONTADOR += 1L;
				mensajesSalida.putIfAbsent(datosBusqueda.Grupo + "_" + datosBusqueda.Producto,
						new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, MensajeSalida.MENSAJE_ERROR, String.format(
								"Error al Procesar %s, grupo: %s, se obtuvo más de una cuenta basilea en el filtrado",
								datosBusqueda.Producto, datosBusqueda.Grupo)));
				break;
			} else if (cuenta == null) {
				mensajesSalida.putIfAbsent(datosBusqueda.Grupo + "*" + datosBusqueda.Producto + "*" + datosBusqueda.IdTipoInstrumento + "*" + datosBusqueda.IdClasificacionTipoInstrumento,
						new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, MensajeSalida.MENSAJE_ADVERTENCIA,
								String.format("No se encontró cuenta para los criterios %s_%s_%s_%s",
										datosBusqueda.Grupo, datosBusqueda.Producto, datosBusqueda.IdTipoInstrumento, datosBusqueda.IdClasificacionTipoInstrumento)));
				continue;
			} else if (cuenta.isEmpty()) {
				mensajesSalida.putIfAbsent(datosBusqueda.Grupo + "-" + datosBusqueda.Producto,
						new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, MensajeSalida.MENSAJE_ADVERTENCIA,
								String.format("No existe cuenta configurada para grupo %s, producto %s",
										datosBusqueda.Grupo, datosBusqueda.Producto)));
			}

			ReplicaInterfazBasilea replicaBasilea = crearRegistroBasilea.apply(datosBusqueda);
			replicaBasilea.FechaEnvio = fechaSistema;
			replicaBasilea.CuentaContable = cuenta;
			replicaBasilea.RutCliente = String.format("%08d", Long.valueOf(RutCorredora.split("-")[0]));
			replicaBasilea.DigitoVerificador = RutCorredora.split("-")[1];
			
			ReplicaInterfazBasileaList.add(replicaBasilea);
		}
		
		out.P_OUT_MENSAJES.addAll(mensajesSalida.values());

		if (out.P_OUT_ERRORESCONTADOR == 0) {
			sql.batch(con, SQL_INSERT_INTERFAZ_BASILEA, ReplicaInterfazBasileaList, ReplicaInterfazBasilea.class);
		}

		return out;
	}
	
	BiFunction<ObjDatosBusqueda, List<CuentaBasilea>, String> buscarCuenta = (dato, cuentas) -> {
		List<CuentaBasilea> resultadoFiltro = cuentas.stream()
				.filter(cb -> dato.IdBolsa.isEmpty() ? cb.IdBolsa.equalsIgnoreCase(dato.IdBolsa) : cb.IdBolsa.contains(dato.IdBolsa))
				.filter(cb -> dato.EsVentaCorta.isEmpty() ? cb.EsVentaCorta.equalsIgnoreCase(dato.EsVentaCorta) : cb.EsVentaCorta.contains(dato.EsVentaCorta))
				.filter(cb -> cb.Grupo.equalsIgnoreCase(dato.Grupo))
				.filter(cb -> cb.Producto.equalsIgnoreCase(dato.Producto))
				.filter(cb -> dato.IdClasificacionTipoInstrumento.isEmpty() ? cb.IdClasificacionTipoInstrumento.equalsIgnoreCase(dato.IdClasificacionTipoInstrumento) : cb.IdClasificacionTipoInstrumento.contains(dato.IdClasificacionTipoInstrumento))
				.filter(cb -> dato.IdTipoInstrumento.isEmpty() ? cb.IdTipoInstrumento.equalsIgnoreCase(dato.IdTipoInstrumento) : cb.IdTipoInstrumento.contains(dato.IdTipoInstrumento))
				.filter(cb -> dato.IdEmisor.isEmpty() ? cb.IdEmisor.equalsIgnoreCase(dato.IdEmisor) : cb.IdEmisor.contains(dato.IdEmisor))
				.filter(cb -> dato.Nemotecnico.isEmpty() ? cb.Nemotecnico.equalsIgnoreCase(dato.Nemotecnico) : cb.Nemotecnico.contains(dato.Nemotecnico))
				.filter(cb -> cb.RelacionadoCorredora.equalsIgnoreCase(dato.RelacionadoCorredora))
				.filter(cb -> cb.IdTipoOperacion.equalsIgnoreCase(dato.IdTipoOperacion))
				.collect(Collectors.toList());
		
		if(resultadoFiltro.size() > 1) {
			return "ERROR";
		} else if(resultadoFiltro.size() == 0) {
			return null;
		}
		
		return resultadoFiltro.get(0).CuentaContable;
	};
	
	Function<ObjDatosBusqueda, ReplicaInterfazBasilea> crearRegistroBasilea = (dato) -> {
		ReplicaInterfazBasilea rb = new ReplicaInterfazBasilea();
		rb.IndicadorTipoFlujo = "NATU";
		rb.IndicadorReversa = "N";
		rb.IndicadorTipoVencimiento = 1L;
		rb.IndicadorFlujo = "FK";
		rb.TipoTasaInteres = 1L;
		rb.FlujoMonedaOrigen = dato.Monto;
		rb.FlujoValorContable = dato.Monto;
		rb.FlujoValorFinal = dato.Monto;
		rb.PlazoFlujo = "ACN".equalsIgnoreCase(dato.Grupo) ? "" : dato.Plazo;

		if ("ACN".equalsIgnoreCase(dato.Grupo) && "ACCIONES".equalsIgnoreCase(dato.Producto)) {
			rb.TipoProducto = "AC";
			rb.RelacionContable = "S".equalsIgnoreCase(dato.EsVentaCorta) ? "PAC" : "ACT";
			rb.CodigoMoneda = "USD".equalsIgnoreCase(dato.IdMonedaInstrumento) ? 2L : 0L;
			rb.CodigoProducto = "AC";
			rb.CodigoSubproducto = "AC";
			rb.ClaseEmision = dato.NemoBasilea;
			rb.MonedaFlujo = dato.IdMonedaInstrumento;
		} else if ("GARANTIZADA".equalsIgnoreCase(dato.Grupo) ^ "LIBRE".equalsIgnoreCase(dato.Grupo) && "RF-IF".equalsIgnoreCase(dato.Producto)) {
			rb.NumeroOperacion = dato.Folio;
			rb.TasaInteresOperacion = dato.Tasa;
			rb.TipoProducto = "RF";
			rb.RelacionContable = "ACT";
			rb.CodigoProducto = "RF";
			rb.CodigoSubproducto = "RF";
			rb.ClaseEmision = dato.Emisor;
			rb.FechaVencimientoFinal = dato.FechaVencimiento;
			rb.FechaInicioOperacion = dato.FechaOperacion;
			rb.FechaVencimientoFlujo = dato.FechaVencimiento;
			rb.MonedaFlujo = "UF".equalsIgnoreCase(dato.IdTipoReajuste) ^ "NR".equalsIgnoreCase(dato.IdTipoReajuste)
								? "CLP"
								: dato.IdTipoReajuste;
		} else if ("PACTEADA".equalsIgnoreCase(dato.Grupo) && "RF-IF".equalsIgnoreCase(dato.Producto)) {
			rb.NumeroOperacion = dato.Folio;
			rb.TasaInteresOperacion = dato.Tasa;
			rb.TipoProducto = "PC";
			rb.RelacionContable = "PAS";
			rb.CodigoProducto = "PC";
			rb.CodigoSubproducto = "PC";
			rb.ClaseEmision = dato.Emisor;
			rb.FechaVencimientoFinal = dato.FechaVencimientoOperacion;
			rb.FechaInicioOperacion = dato.FechaLiquidacion;
			rb.FechaVencimientoFlujo = dato.FechaVencimiento;
			rb.CodigoMoneda = ("PACTEADA".equalsIgnoreCase(dato.Grupo)) ? 
	          						("CLP".equalsIgnoreCase(String.valueOf(dato.IdTipoReajuste)) ? 0L :
	          						 "UF".equalsIgnoreCase(String.valueOf(dato.IdTipoReajuste)) ? 1L :
	          				         "USD".equalsIgnoreCase(String.valueOf(dato.IdTipoReajuste)) ? 2L :
    			                     "EUR".equalsIgnoreCase(String.valueOf(dato.IdTipoReajuste)) ? 3L : 0L) : 0L;
			rb.MonedaFlujo = "UF".equalsIgnoreCase(dato.IdTipoReajuste) ^ "NR".equalsIgnoreCase(dato.IdTipoReajuste)
								? "CLP"
								: dato.IdTipoReajuste;
		} else if ("FORWARD".equalsIgnoreCase(dato.Producto)) {
			rb.TipoProducto = "FW";
			rb.RelacionContable = "OBLIG".equalsIgnoreCase(dato.Grupo) ? "PAS" : "ACT"; 
			rb.CodigoProducto = "FW";
			rb.CodigoSubproducto = "FW";
			rb.FechaVencimientoFinal = dato.FechaVencimiento;
			rb.FechaInicioOperacion = dato.FechaOperacion;
			rb.FechaVencimientoFlujo = dato.FechaVencimiento;
			rb.NumeroOperacion = dato.Folio;
			rb.TasaInteresOperacion = dato.Tasa;
			rb.CodigoMoneda = ("OBLIG".equalsIgnoreCase(dato.Grupo)) ? 
					          		("CLP".equalsIgnoreCase(String.valueOf(dato.IdMonedaSecundaria)) ? 0L :
			        				"UF".equalsIgnoreCase(String.valueOf(dato.IdMonedaSecundaria)) ? 1L :
			        				"USD".equalsIgnoreCase(String.valueOf(dato.IdMonedaSecundaria)) ? 2L :
			        			    "EUR".equalsIgnoreCase(String.valueOf(dato.IdMonedaSecundaria)) ? 3L : 0L) : 
        			    	  ("DERECHO".equalsIgnoreCase(dato.Grupo)) ? 
					          		("CLP".equalsIgnoreCase(String.valueOf(dato.IdMonedaPrincipal)) ? 0L :
			        				"UF".equalsIgnoreCase(String.valueOf(dato.IdMonedaPrincipal)) ? 1L :
			        				"USD".equalsIgnoreCase(String.valueOf(dato.IdMonedaPrincipal)) ? 2L :
			        			    "EUR".equalsIgnoreCase(String.valueOf(dato.IdMonedaPrincipal)) ? 3L : 0L) : 0L;
			rb.MonedaFlujo = "OBLIG".equalsIgnoreCase(dato.Grupo) ? dato.IdMonedaSecundaria : dato.IdMonedaPrincipal ;
		}

		return rb;
	};
	
	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02",
				"C:\\optimus-corredores\\connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();
				ProcesarInterfazBasilea test = new ProcesarInterfazBasilea();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CON\"}";
				EntradaProcesarInterfazBasilea in = gson.fromJson(json, EntradaProcesarInterfazBasilea.class);
				test.logica(con, in);
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
