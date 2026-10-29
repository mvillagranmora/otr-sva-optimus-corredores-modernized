package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarActivoPasivoCustodiaPortafolio;
import cl.bcs.custodia.ConsultarActivoPasivoCustodiaPortafolio.Cartera;
import cl.bcs.custodia.ConsultarActivoPasivoCustodiaPortafolio.Salida_ConsultaActivoPasivo;
import cl.bcs.custodia.ConsultarDetallePrecioPromedio;
import cl.bcs.custodia.ConsultarDetallePrecioPromedio.EntradaConsultarDetallePrecioPromedio;
import cl.bcs.custodia.ConsultarDetallePrecioPromedio.SalidaConsultarDetallePrecioPromedio;
import cl.bcs.normativa.cartola.GenerarCartola.NominaPaso;
import cl.bcs.normativa.cartola.GenerarCartola.ServicioEjecutado;
import cl.bcs.normativa.cartola.pojo.Boveda;
import cl.bcs.normativa.cartola.pojo.ClasificacionTipoInstrumento;
import cl.bcs.normativa.cartola.pojo.Instrumento;
import cl.bcs.normativa.cartola.pojo.InstrumentoValorClasificacionRiesgo;
import cl.bcs.normativa.cartola.pojo.Rubro;
import cl.bcs.normativa.cartola.pojo.TipoInstrumento;
import cl.bcs.normativa.cartola.pojo.TipoInstrumentoClasificacionRiesgo;
import cl.bcs.ordenes.AnularOrdenMasiva.AnularOrden;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CarteraRV implements BcsCartola{
	
	public final Log log = new Log("NOR.CarteraRV");
	private final static Logger logger = LogManager.getLogger(CarteraRV.class);
	
	//GENERICOS
	private final String SQL_SELECT_INSTRUMENTO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectInstrumento.sql");
	private final String SQL_SELECT_BOVEDA = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectBoveda.sql");
	private final String SQL_SELECT_CLASIFICACION_INSTRUMENTO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectClasificacionTipoInstrumento.sql");
	private final String SQL_SELECT_INSTRUMENTO_VALOR_CLASIFICACION_RIESGO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectInstrumentoValorClasificacionRiesgo.sql");
	private final String SQL_SELECT_RUBRO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectRubro.sql");
	private final String SQL_SELECT_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectTipoInstrumento.sql");
	private final String SQL_SELECT_TIPO_INSTRUMENTO_CLASIFICACION_RIESGO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectTipoInstrumentoClasificacionRiesgo.sql");
	
	private final String SQL_SELECT_NOMINA_PASO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectNominaPaso.sql");
	
	private final String SQL_SELECT_SERVICIOS_EJECUTADOS = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectServicioEjecutado.sql");
	private final String SQL_INSERT_SERVICIOS_EJECUTADOS = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertServicioEjecutado.sql");
	private final String SQL_INSERT_CARTERA_RV_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRV_InsertOutServicioEjecutado.sql");
	private final String SQL_SELECT_CARTERA_RV_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRV_SelectOUTServicioEjecutado.sql");
	
	private final String SQL_INSERT_PRECIO_TASA_PASO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRV_InsertPrecioTasaTemporal.sql");
	private final String SQL_DELETE_PRECIO_TASA_PASO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRV_DeletePrecioTasaTemporal.sql");
	
	public static class Movimiento{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdBoveda;
		public Long IdInstrumento;
		public String Nemotecnico;
		public BigDecimal	DisponibleActivo;
		public BigDecimal	DisponiblePasivo;
		public BigDecimal	GarantiaActivo;
		public BigDecimal	GarantiaPasivo;
		public BigDecimal	PrestamoActivo;
		public BigDecimal	PrestamoPasivo;
		public BigDecimal	PlazoActivo;
		public BigDecimal	PlazoPasivo;
		public String 	  IdMonedaInstrumento;
		public BigDecimal PrecioPromedioCompra;
		public BigDecimal PrecioPromedioCompraPasivo;
		public BigDecimal MontoPromedioCompraMonedaSistemaActivo;
		public BigDecimal MontoPromedioCompraMonedaAdicionalActivo;
		public BigDecimal TasaMercado;
		public BigDecimal PrecioMercado;
		public BigDecimal MontoMercadoMonedaSistemaActivo;
		public BigDecimal MontoMercadoMonedaAdicionalActivo;
		public BigDecimal MontoMercadoMonedaInstrumentoActivo;
		public BigDecimal MontoATasaCompraActivo;
		public String	  FechaEfectivaMovimiento;
		public String	  EsDividendo;
		
		public BigDecimal MontoMercadoMonedaSistemaPasivo;
		public BigDecimal MontoMercadoMonedaAdicionalPasivo;
		public BigDecimal MontoMercadoMonedaInstrumentoPasivo;
		public BigDecimal MontoPlazoMonedaSistemaPasivo;
		public BigDecimal MontoPlazoMonedaAdicionalPasivo;
		public BigDecimal MontoATasaCompraPasivo;
		public BigDecimal MontoPromedioCompraMonedaSistemaPasivo;
		public BigDecimal MontoPromedioCompraMonedaAdicionalPasivo;
		public BigDecimal GananciaPerdida;
		public BigDecimal GananciaPerdidaMonedaInstrumento;
		public String 	  IdEmisor;
		public Long IdValorizacionActivo;
		public Long IdValorizacionPasivo;
		public Long IdValorizacionPlazoPasivo;
		public Movimiento(){
			this.DisponibleActivo = BigDecimal.ZERO;
			this.DisponiblePasivo= BigDecimal.ZERO;
			this.GarantiaActivo= BigDecimal.ZERO;
			this.GarantiaPasivo= BigDecimal.ZERO;
			this.PrestamoActivo= BigDecimal.ZERO;
			this.PrestamoPasivo= BigDecimal.ZERO;
			this.PlazoActivo= BigDecimal.ZERO;
			this.PlazoPasivo= BigDecimal.ZERO;
			this.PrecioMercado = BigDecimal.ZERO;
			this.TasaMercado = BigDecimal.ZERO;
			this.GananciaPerdida = BigDecimal.ZERO;
			this.GananciaPerdidaMonedaInstrumento = BigDecimal.ZERO;
			this.MontoPromedioCompraMonedaSistemaActivo = BigDecimal.ZERO;
			this.MontoPromedioCompraMonedaAdicionalActivo = BigDecimal.ZERO;
			this.MontoMercadoMonedaSistemaActivo = BigDecimal.ZERO;
			this.MontoMercadoMonedaAdicionalActivo = BigDecimal.ZERO;
			this.MontoMercadoMonedaInstrumentoActivo = BigDecimal.ZERO;
			this.MontoATasaCompraActivo = BigDecimal.ZERO;
			this.MontoMercadoMonedaSistemaPasivo = BigDecimal.ZERO;
			this.MontoMercadoMonedaAdicionalPasivo = BigDecimal.ZERO;
			this.MontoMercadoMonedaInstrumentoPasivo = BigDecimal.ZERO;
			this.MontoPlazoMonedaSistemaPasivo = BigDecimal.ZERO;
			this.MontoPlazoMonedaAdicionalPasivo = BigDecimal.ZERO;
			this.MontoATasaCompraPasivo = BigDecimal.ZERO;
			this.MontoPromedioCompraMonedaSistemaPasivo = BigDecimal.ZERO;
			this.MontoPromedioCompraMonedaAdicionalPasivo = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class dividendosSebra{
		public Long IdPortafolio;
		public String Nemotecnico;
		public BigDecimal Monto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PrecioTasasInstrumento{
		public Long IdPortafolio;
		public Long IdInstrumento;
		public BigDecimal Precio;
		public Long IdTrabajo;
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdTrabajo;
	}
	
	public static class SalidaCarteraRV{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdBoveda;
		public String CodigoBoveda;
		public String NombreBoveda;
		public String Ubicacion;
		public String UbicacionSolicitada;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public String descMercado;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String FechaVencimiento;
		public String FechaEmision;
		public String PlazoResidual;
		public String Rubro;
		public String IdTipoReajuste;
		public String IdEmisor;
		public String RazonSocial;
		public BigDecimal CorteMinimo;
		public BigDecimal TasaEmision;
		public String ValorClasificacionRiesgo;
		public BigDecimal	DisponibleActivo;
		public BigDecimal	DisponiblePasivo;
		public BigDecimal	GarantiaActivo;
		public BigDecimal	GarantiaPasivo;
		public BigDecimal	PrestamoActivo;
		public BigDecimal	PrestamoPasivo;
		public BigDecimal	PlazoActivo;
		public BigDecimal	PlazoPasivo;
				

		public BigDecimal PrecioPromedioCompra;
		public BigDecimal PrecioPromedioCompraPasivo;
		public BigDecimal MontoPromedioCompraMonedaSistemaActivo;
		public BigDecimal MontoPromedioCompraMonedaAdicionalActivo;
		public BigDecimal TasaMercado;
		public BigDecimal PrecioMercado;
		public BigDecimal MontoMercadoMonedaSistemaActivo;
		public BigDecimal MontoMercadoMonedaAdicionalActivo;
		public String 	  Moneda;
		public String 	  IdMonedaOperacion;
		public BigDecimal MontoPromedio;
		public BigDecimal MontoMercado;
		public BigDecimal GananciaPerdida;
		public BigDecimal GananciaPerdidaMonedaInstrumento;
		public BigDecimal RentabilidadActivo;
		public BigDecimal RentabilidadPasivo;
		public BigDecimal MontoPromedioCompraMonedaSistemaPasivo;
		public BigDecimal MontoPromedioCompraMonedaAdicionalPasivo;
		public BigDecimal MontoMercadoMonedaSistemaPasivo;
		public BigDecimal MontoMercadoMonedaAdicionalPasivo;
		public BigDecimal MontoMercadoMonedaInstrumentoActivo;
		public BigDecimal MontoMercadoMonedaInstrumentoPasivo;

		public SalidaCarteraRV(){
			this.CodigoBoveda = "";
			this.NombreBoveda = "";
			this.Ubicacion = "";
			this.UbicacionSolicitada = "";
			this.Rubro = "";
			this.GananciaPerdidaMonedaInstrumento = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaCarteraRVPaso extends SalidaCarteraRV{
		public Long IdNomina;
		public Long IdTrabajo;
		public String Recurso;
		public String Entrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		long inicioServicio = System.currentTimeMillis();
		log.write("INICIO::CarteraRV");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		
		
		
		///////////////////////////////////////////////////////////////////////////////////////////
		//			VALIDA QUE EL PROCESO NO SE EJECUTE VARIAS VECES PARA LA MISMA CARTOLA		 //
		///////////////////////////////////////////////////////////////////////////////////////////
		NominaPaso nomina = new NominaPaso();
		List<NominaPaso> nominas = new ArrayList();
		Boolean primeraEjecucion = false;
		Long idTrabajoEjecutado = 0L;
		try {
			nominas = sql.query(con, SQL_SELECT_NOMINA_PASO, new HashMap<>(), NominaPaso.class);
		} catch (Exception e) {
			logger.info("INICIO::ResumenInversionBase -ERROR SQL_SELECT_NOMINA_PASO "+e);
		}
		if(!nominas.isEmpty()) {
			nomina = nominas.get(0);
			if(nomina.IdNomina.compareTo(0L) != 0L) {
				String jeisonString = entrada.p_Datos.toString();
				String clase = this.getClass().getSimpleName();
				HashMap<String,Object> paramsJson = new HashMap<String,Object>();
				List<ServicioEjecutado> serviciosEjecutados = new ArrayList();
				List<SalidaCarteraRVPaso> outGuardado = new ArrayList();
				List<SalidaCarteraRV> listOutExistente = new ArrayList();
				paramsJson.put("IdNomina", nomina.IdNomina);
				paramsJson.put("Recurso", clase);
				try {
					serviciosEjecutados = sql.query(con, SQL_SELECT_SERVICIOS_EJECUTADOS, paramsJson, ServicioEjecutado.class);
				} catch (Exception e) {
					logger.info("INICIO::CarteraRV -ERROR SQL_SELECT_SERVICIO_EJECUTADO "+e);
				}
				
				if(serviciosEjecutados.isEmpty()) {
					List<ServicioEjecutado> portObj = new ArrayList<>();
					ServicioEjecutado se = new ServicioEjecutado();
					se.IdNomina = nomina.IdNomina;
					se.IdTrabajo = idTrabajo;
					se.Entrada = jeisonString;
					se.Recurso = clase;
					portObj.add(se);
					try {
						sql.batch(con, SQL_INSERT_SERVICIOS_EJECUTADOS, portObj, ServicioEjecutado.class);
					} catch (Exception e) {
						logger.info("INICIO::CarteraRV -ERROR SQL_SELECT_SERVICIO_EJECUTADO "+e);
					}
					primeraEjecucion = true;
				}
				else
				{
					HashMap<String,Object> paramsOut = new HashMap<String,Object>();
					paramsOut.put("IdNomina", nomina.IdNomina);
					outGuardado = sql.query(con, SQL_SELECT_CARTERA_RV_OUT,paramsOut, SalidaCarteraRVPaso.class);
					
					for(ServicioEjecutado se : serviciosEjecutados) {
						if(se.Entrada.equals(jeisonString) && se.IdNomina.compareTo(nomina.IdNomina) == 0L && se.Recurso.equals(clase)) {
							idTrabajoEjecutado = se.IdTrabajo;
							primeraEjecucion = false;
							break;
						}
						else {
							primeraEjecucion = true;
						}
					}
					if(primeraEjecucion == false) {
						listOutExistente = parseaObjetoDePaso(outGuardado,idTrabajoEjecutado);
						if(listOutExistente.size() > 0) {
							listOutExistente.sort(new Comparator<SalidaCarteraRV>() {
								@Override
								public int compare(SalidaCarteraRV mov1, SalidaCarteraRV mov2) {
									  if(mov1.Nemotecnico.compareTo(mov2.Nemotecnico) != 0){
								        	return mov1.Nemotecnico.compareTo(mov2.Nemotecnico);
								        }
									  else  {
								        	return mov1.IdMonedaOperacion.compareTo(mov2.IdMonedaOperacion);
								        }
									}
								}
							);
							List<HashMap<String,Object>> resultadoCarteraRV = Utils.List2ListHashGenericObject(listOutExistente);
							out.P_OUT_SALIDA.put("carteraRV", resultadoCarteraRV);
							log.write("FIN::CarteraRV");
							return out;
						}
						
					}
				}
			}
			else {
				primeraEjecucion = false;
			}
		}
		///////////////////////////////////////////////////////////////////////////////////////////
		//			FIN VALIDA QUE EL PROCESO NO SE EJECUTE VARIAS VECES PARA LA MISMA CARTOLA	 //
		///////////////////////////////////////////////////////////////////////////////////////////
		
		String monedaAdicional = null;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Date dateFechaSistema = Fecha.toDate(fechaSistema);
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		//El Metodo arroja error cuando no encuentra parametro
		try{
			monedaAdicional = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaAdicional");
		}catch(Exception e){
			log.write(e);
		}
		
		if(monedaAdicional == null || monedaAdicional.isEmpty()){
			monedaAdicional = "USD"; //Cuando no existe moneda es USD
		}
		
		EntradaConsultarDetallePrecioPromedio entradaConsultarDetallePrecioPromedio = new EntradaConsultarDetallePrecioPromedio();
		entradaConsultarDetallePrecioPromedio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarDetallePrecioPromedio.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarDetallePrecioPromedio.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarDetallePrecioPromedio.p_FechaBusqueda = Fecha.stringToSqlDate(entrada.p_FechaHasta);
		entradaConsultarDetallePrecioPromedio.p_SumaVentaPlazoEnDisponible = "S";
		
		List<Long> portafolios = new ArrayList<Long>();
		
		List<Portafolio> portObj = new ArrayList<>();
		for(Dato dato : entrada.p_Datos){
			portafolios.add(dato.IdPortafolio);
			ConsultarDetallePrecioPromedio.InPortafolio inPortafolio = new ConsultarDetallePrecioPromedio.InPortafolio();
			inPortafolio.IdPortafolio = dato.IdPortafolio;
			entradaConsultarDetallePrecioPromedio.p_Portafolios.add(inPortafolio);
			Portafolio p = new Portafolio();
			p.IdPortafolio = dato.IdPortafolio;
			p.IdTrabajo = idTrabajo;
			portObj.add(p);
		}
		
		ConsultarDetallePrecioPromedio.InInstrumento inInstrumento = new ConsultarDetallePrecioPromedio.InInstrumento();
		inInstrumento.IdClasificacionTipoInstrumento = "RV";
		entradaConsultarDetallePrecioPromedio.p_Instrumentos.add(inInstrumento);
		
		ConsultarDetallePrecioPromedio consultarDetallePrecioPromedio = new ConsultarDetallePrecioPromedio();
		SalidaConsultarDetallePrecioPromedio salidaConsultarDetallePrecioPromedio = consultarDetallePrecioPromedio.logica(con, entradaConsultarDetallePrecioPromedio);
		Map<String,ConsultarDetallePrecioPromedio.PrecioPromedio> hashPreciosPromedios = salidaConsultarDetallePrecioPromedio.P_OUT_PRECIO_PROMEDIO_ACTIVO.parallelStream().collect(Collectors.toMap(precio-> precio.IdPortafolio + "_" + precio.IdInstrumento + "_" + precio.IdBoveda, precio->precio));
		Map<String,ConsultarDetallePrecioPromedio.PrecioPromedio> hashPreciosPromediosPasivo = salidaConsultarDetallePrecioPromedio.P_OUT_PRECIO_PROMEDIO_PASIVO.parallelStream().collect(Collectors.toMap(precio-> precio.IdPortafolio + "_" + precio.IdInstrumento + "_" + precio.IdBoveda, precio->precio));
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdPortafolio", portafolios);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaHasta", entrada.p_FechaHasta);	
		
		ConsultarActivoPasivoCustodiaPortafolio ConsultaCustodia = new ConsultarActivoPasivoCustodiaPortafolio();
		ConsultarActivoPasivoCustodiaPortafolio.Entrada_ConsultaActivoPasivo entradaConsulta = new ConsultarActivoPasivoCustodiaPortafolio.Entrada_ConsultaActivoPasivo();
		
		entradaConsulta.p_IdEmpresa  = entrada.p_IdEmpresa;
		entradaConsulta.p_FechaDesde = entrada.p_FechaDesde;
		entradaConsulta.p_FechaHasta = entrada.p_FechaHasta;
		entradaConsulta.p_Clientes   = portafolios;
				
		Salida_ConsultaActivoPasivo salidaActPas = ConsultaCustodia.logica(con, entradaConsulta);
		
		params.clear();
		
		List<Movimiento> movimientos = salidaActPas.p_Carteras
			    .stream()
			    .map(car -> {
			        Movimiento mov = new Movimiento();
			        String id = car.IdPortafolio + "_" + car.IdInstrumento + "_" + car.IdBoveda;

			        mov.IdCliente = car.IdCliente;
			        mov.IdPortafolio = car.IdPortafolio;
			        mov.IdClasificacionTipoInstrumento = car.IdClasificacionTipoInstrumento;
			        mov.IdTipoInstrumento = car.IdTipoInstrumento;
			        mov.IdBoveda = car.IdBoveda;
			        mov.IdEmisor = car.IdEmisor;
			        mov.IdInstrumento = car.IdInstrumento;
			        mov.Nemotecnico = car.Nemotecnico;
			        mov.IdMonedaInstrumento = car.IdMonedaInstrumento;

			        mov.DisponibleActivo = car.DisponibleActivo.add(car.DisponibleActivoTra);
			        mov.DisponiblePasivo = car.DisponiblePasivo.add(car.DisponiblePasivoTra);
			        mov.GarantiaActivo = car.GarantiaActivo;
			        mov.GarantiaPasivo = car.GarantiaPasivo;
			        mov.PrestamoActivo = car.PrestamoActivo;
			        mov.PrestamoPasivo = car.PrestamoPasivo;
			        mov.PlazoActivo = car.PlazoActivo;
			        mov.PlazoPasivo = car.PlazoPasivo;

			        mov.PrecioPromedioCompra = 
			            car.DisponibleActivoTra.compareTo(BigDecimal.ZERO) != 0 || 
			            car.PrestamoActivo.compareTo(BigDecimal.ZERO) != 0 
			                ? car.PrecioPromedioCompra 
			                : hashPreciosPromedios.get(id) != null 
			                    ? hashPreciosPromedios.get(id).PrecioPromedio 
			                    : BigDecimal.ZERO;

			        mov.PrecioPromedioCompraPasivo = 
			            car.DisponiblePasivoTra.compareTo(BigDecimal.ZERO) != 0 || 
			            car.PrestamoPasivo.compareTo(BigDecimal.ZERO) != 0 
			                ? car.PrecioPromedioCompra 
			                : hashPreciosPromediosPasivo.get(id) != null 
			                    ? hashPreciosPromediosPasivo.get(id).PrecioPromedio 
			                    : BigDecimal.ZERO;

			        mov.MontoPromedioCompraMonedaSistemaActivo = car.MontoPromedioCompraMonedaSistemaActivo;
			        mov.MontoPromedioCompraMonedaAdicionalActivo = car.MontoPromedioCompraMonedaAdicionalActivo;
			        mov.MontoATasaCompraActivo = car.MontoATasaCompraActivo;
			        mov.PrecioMercado = car.PrecioMercado;
			        mov.MontoMercadoMonedaSistemaActivo = car.MontoMercadoMonedaSistemaActivo;
			        mov.MontoMercadoMonedaAdicionalActivo = car.MontoMercadoMonedaAdicionalActivo;

			        mov.MontoPromedioCompraMonedaSistemaPasivo = car.MontoPromedioCompraMonedaSistemaPasivo;
			        mov.MontoPromedioCompraMonedaAdicionalPasivo = car.MontoPromedioCompraMonedaAdicionalPasivo;
			        mov.MontoATasaCompraPasivo = car.MontoATasaCompraPasivo;
			        mov.MontoMercadoMonedaSistemaPasivo = car.MontoMercadoMonedaSistemaPasivo;
			        mov.MontoMercadoMonedaAdicionalPasivo = car.MontoMercadoMonedaAdicionalPasivo;

			        mov.GananciaPerdida = car.Dividendo;
			        mov.GananciaPerdidaMonedaInstrumento=car.Dividendo;
			        mov.FechaEfectivaMovimiento = car.FechaEfectivaMovimiento;
			        mov.EsDividendo = car.EsDividendo;

			        return mov;
			    })
			    .collect(Collectors.toList());

		
		List<SalidaCarteraRV> salidas = new ArrayList<SalidaCarteraRV>();
		
		if (movimientos.size() > 0) {
			
			//OBTIENE VECTOR PRECIOS
			EntregarPreciosTasas_Entrada entregarPreciosTasasEntrada = new EntregarPreciosTasas_Entrada();
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			entregarPreciosTasasEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			entregarPreciosTasasEntrada.p_IdModulo = entrada.p_IdModulo;
			entregarPreciosTasasEntrada.p_IdPais = idPaisEmpresa;		
			entregarPreciosTasasEntrada.p_IdUsuario = entrada.p_IdUsuario;
			
			Long i = 1L;
			HashMap<Long, Object> mapIdRegistroValorizacion = new HashMap<Long, Object>();
			
			for (Movimiento mov : movimientos) {
				if ((mov.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV") && !mov.IdTipoInstrumento.equalsIgnoreCase("SIM"))) {
					EntregarPreciosTasas_Entrada_DatosInstrumentos entradaDatosInstrumentos = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
					mov.IdValorizacionActivo = i;
					entradaDatosInstrumentos.IdRegistro = mov.IdValorizacionActivo;
					entradaDatosInstrumentos.TipoCartera = "0";
					entradaDatosInstrumentos.IdUso = "CARTERA";
					entradaDatosInstrumentos.IdTipoCriterioTasa = "0";
					entradaDatosInstrumentos.IdClasificacionTipoInstrumento = mov.IdClasificacionTipoInstrumento;
					entradaDatosInstrumentos.IdTipoInstrumento = mov.IdTipoInstrumento==null || mov.IdTipoInstrumento=="" ? "0" : mov.IdTipoInstrumento;
					entradaDatosInstrumentos.IdInstrumento = mov.IdInstrumento;
					entradaDatosInstrumentos.Nemotecnico = mov.Nemotecnico;
					entradaDatosInstrumentos.IdMoneda = mov.IdMonedaInstrumento;
					entradaDatosInstrumentos.FechaConsulta = entrada.p_FechaHasta;  
					entradaDatosInstrumentos.PlazoResidual = 0L;
					entregarPreciosTasasEntrada.p_DatosInstrumentos.add(entradaDatosInstrumentos);
					mapIdRegistroValorizacion.put(entradaDatosInstrumentos.IdRegistro, mov.IdPortafolio);
				}
				i++;
			}
			
			if (entregarPreciosTasasEntrada.p_DatosInstrumentos.size() > 0) {
				EntregarPreciosTasas entregarPreciosTasasService = new EntregarPreciosTasas();
				EntregarPreciosTasas_Salida entregarPreciosTasasSalida = entregarPreciosTasasService.logica(con, entregarPreciosTasasEntrada);
				HashMap<String, Object> precios = Utils.List2Hash(entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS,"IdRegistro");
				SQL.exec(con, SQL_DELETE_PRECIO_TASA_PASO.replace(":IdTrabajo", idTrabajo+""));
				List<PrecioTasasInstrumento> listPrecioTasa = new ArrayList<PrecioTasasInstrumento>();
				if(entregarPreciosTasasSalida != null) {
					if(entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS != null) {
						for(EntregarPreciosTasas_Salida_PreciosTasas precioTasa : entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS) {
							PrecioTasasInstrumento precioTasaInstrumento = new PrecioTasasInstrumento();
							precioTasaInstrumento.IdPortafolio = (Long) mapIdRegistroValorizacion.get(precioTasa.IdRegistro);
							precioTasaInstrumento.IdInstrumento = precioTasa.IdInstrumento;
							precioTasaInstrumento.Precio = precioTasa.Precio;
							precioTasaInstrumento.IdTrabajo = entrada.p_IdTrabajo;
							listPrecioTasa.add(precioTasaInstrumento);
						}
						sql.batch(con, SQL_INSERT_PRECIO_TASA_PASO, listPrecioTasa, PrecioTasasInstrumento.class);
					}
					
				}
				
				
				
				for (Movimiento mov : movimientos) {
					if (!mov.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")){
						EntregarPreciosTasas_Salida_PreciosTasas registroPrecio = (EntregarPreciosTasas_Salida_PreciosTasas) precios.get(mov.IdValorizacionActivo);
						if (registroPrecio != null && registroPrecio.Precio != null /*&& registroPrecio.Precio.doubleValue() > 0*/) {
							mov.PrecioMercado = registroPrecio.Precio;
							mov.TasaMercado = registroPrecio.Tasa;
						
							if(registroPrecio.IdRegistro.equals(mov.IdValorizacionActivo) /*&& (mov.DisponibleActivo.add(mov.GarantiaActivo).add(mov.PrestamoActivo).add(mov.PlazoActivo)).compareTo(new BigDecimal(0)) != 0*/) {
								BigDecimal activos = mov.DisponibleActivo.add(mov.GarantiaActivo).add(mov.PrestamoActivo).add(mov.PlazoActivo);
								mov.MontoMercadoMonedaSistemaActivo = registroPrecio.Precio != null /*&& registroPrecio.Precio.compareTo(BigDecimal.ZERO) != 0*/ ? activos.multiply(registroPrecio.Precio, MathContext.DECIMAL128) : mov.MontoMercadoMonedaSistemaActivo;
								mov.MontoMercadoMonedaAdicionalActivo = registroPrecio.Precio != null /*&& registroPrecio.Precio.compareTo(BigDecimal.ZERO) != 0*/ ? activos.multiply(registroPrecio.Precio, MathContext.DECIMAL128) : mov.MontoMercadoMonedaAdicionalActivo;
								//Monto Moneda Instrumento
								mov.MontoMercadoMonedaInstrumentoActivo = registroPrecio.Precio != null ? activos.multiply(registroPrecio.Precio, MathContext.DECIMAL128) : mov.MontoMercadoMonedaInstrumentoActivo;
							}
							if(registroPrecio.IdRegistro.equals(mov.IdValorizacionActivo) /*&& (mov.DisponiblePasivo.add(mov.GarantiaPasivo).add(mov.PrestamoPasivo).add(mov.PlazoPasivo)).compareTo(new BigDecimal(0)) != 0*/) {
								BigDecimal pasivos = mov.DisponiblePasivo.add(mov.GarantiaPasivo).add(mov.PrestamoPasivo).add(mov.PlazoPasivo);
								mov.MontoMercadoMonedaSistemaPasivo = registroPrecio.Precio != null /*&& registroPrecio.Precio.compareTo(BigDecimal.ZERO) != 0*/ ? pasivos.multiply(registroPrecio.Precio, MathContext.DECIMAL128) : mov.MontoMercadoMonedaSistemaPasivo;
								mov.MontoMercadoMonedaAdicionalPasivo = registroPrecio.Precio != null /*&& registroPrecio.Precio.compareTo(BigDecimal.ZERO) != 0*/ ? pasivos.multiply(registroPrecio.Precio, MathContext.DECIMAL128) : mov.MontoMercadoMonedaAdicionalPasivo;
								//Monto Moneda Instrumento
								mov.MontoMercadoMonedaInstrumentoPasivo = registroPrecio.Precio != null ? pasivos.multiply(registroPrecio.Precio, MathContext.DECIMAL128) : mov.MontoMercadoMonedaInstrumentoPasivo;
							}
							
						}
					}
				}
			}
			
			
			//CALCULA PARIDAD ACTIVOS
			i = 1L;
			CalcularParidad calcularParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 0L;
			for(Movimiento mov : movimientos) {
				mov.IdValorizacionActivo = i;
				CalculoEntrada calculoEntradaMonedaSistema = new CalculoEntrada();
				calculoEntradaMonedaSistema.IdInterno = mov.IdValorizacionActivo;
				calculoEntradaMonedaSistema.IdTrabajo = 0L;
				calculoEntradaMonedaSistema.Fecha = Fecha.stringToSqlDate(entrada.p_FechaHasta);
				calculoEntradaMonedaSistema.IdMonedaOrigen = mov.IdMonedaInstrumento;
				calculoEntradaMonedaSistema.IdMonedaDestino = monedaSistema;
				calculoEntradaMonedaSistema.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntradaMonedaSistema.MontoOrigen = mov.MontoMercadoMonedaSistemaActivo;
				calculoEntradaMonedaSistema.IdUso = "CARTOLA";
				CalculoEntrada calculoEntradaMonedaAdicional = new CalculoEntrada();
				calculoEntradaMonedaAdicional.IdInterno = mov.IdValorizacionActivo * -1;
				calculoEntradaMonedaAdicional.IdTrabajo = 0L;
				calculoEntradaMonedaAdicional.Fecha = Fecha.stringToSqlDate(entrada.p_FechaHasta);
				calculoEntradaMonedaAdicional.IdMonedaOrigen = mov.IdMonedaInstrumento;
				calculoEntradaMonedaAdicional.IdMonedaDestino = monedaAdicional;
				calculoEntradaMonedaAdicional.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntradaMonedaAdicional.MontoOrigen = mov.MontoMercadoMonedaAdicionalActivo;
				calculoEntradaMonedaAdicional.IdUso = "CARTOLA";
				
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaMonedaSistema);
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaMonedaAdicional);
				i++;
			}
			
			CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
			HashMap<Long,Object> hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
	
			for(Movimiento mov : movimientos) {
				Long id = mov.IdValorizacionActivo * -1;
				CalculoSalida calculoSalidaMonedaSistema = (CalculoSalida) hCalcularParidadSalida.get(mov.IdValorizacionActivo);
				CalculoSalida calculoSalidaMonedaAdicional = (CalculoSalida) hCalcularParidadSalida.get(id);
				if("OK".equals(calculoSalidaMonedaSistema.Resultado)) mov.MontoMercadoMonedaSistemaActivo = calculoSalidaMonedaSistema.MontoDestino;
				if("OK".equals(calculoSalidaMonedaAdicional.Resultado)) mov.MontoMercadoMonedaAdicionalActivo = calculoSalidaMonedaAdicional.MontoDestino;	
				
				//Se deben redondear montos al peso
				mov.MontoMercadoMonedaSistemaActivo = mov.MontoMercadoMonedaSistemaActivo.setScale(0, BigDecimal.ROUND_HALF_UP);
				mov.MontoMercadoMonedaAdicionalActivo = mov.MontoMercadoMonedaAdicionalActivo.setScale(0, BigDecimal.ROUND_HALF_UP);
				mov.MontoMercadoMonedaInstrumentoActivo = mov.MontoMercadoMonedaInstrumentoActivo.setScale(0, BigDecimal.ROUND_HALF_UP);
			}
			
			//PARIDAD PARIDAD PASIVOS
			i = 1L;
			calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 0L;
			for(Movimiento mov : movimientos) {
				mov.IdValorizacionPasivo = i;
				CalculoEntrada calculoEntradaMonedaSistema = new CalculoEntrada();
				calculoEntradaMonedaSistema.IdInterno = mov.IdValorizacionPasivo;
				calculoEntradaMonedaSistema.IdTrabajo = 0L;
				calculoEntradaMonedaSistema.Fecha = Fecha.stringToSqlDate(entrada.p_FechaHasta);
				calculoEntradaMonedaSistema.IdMonedaOrigen = mov.IdMonedaInstrumento;
				calculoEntradaMonedaSistema.IdMonedaDestino = monedaSistema;
				calculoEntradaMonedaSistema.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntradaMonedaSistema.MontoOrigen = mov.MontoMercadoMonedaSistemaPasivo;
				calculoEntradaMonedaSistema.IdUso = "CARTOLA";
				CalculoEntrada calculoEntradaMonedaAdicional = new CalculoEntrada();
				calculoEntradaMonedaAdicional.IdInterno = mov.IdValorizacionPasivo * -1;
				calculoEntradaMonedaAdicional.IdTrabajo = 0L;
				calculoEntradaMonedaAdicional.Fecha = Fecha.stringToSqlDate(entrada.p_FechaHasta);
				calculoEntradaMonedaAdicional.IdMonedaOrigen = mov.IdMonedaInstrumento;
				calculoEntradaMonedaAdicional.IdMonedaDestino = monedaAdicional;
				calculoEntradaMonedaAdicional.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntradaMonedaAdicional.MontoOrigen = mov.MontoMercadoMonedaAdicionalPasivo;
				calculoEntradaMonedaAdicional.IdUso = "CARTOLA";
				
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaMonedaSistema);
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaMonedaAdicional);
				i++;
			}
			
			calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
			hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
	
			for(Movimiento mov : movimientos) {
				Long id = mov.IdValorizacionPasivo * -1;
				CalculoSalida calculoSalidaMonedaSistema = (CalculoSalida) hCalcularParidadSalida.get(mov.IdValorizacionPasivo);
				CalculoSalida calculoSalidaMonedaAdicional = (CalculoSalida) hCalcularParidadSalida.get(id);
				if("OK".equals(calculoSalidaMonedaSistema.Resultado)) mov.MontoMercadoMonedaSistemaPasivo = calculoSalidaMonedaSistema.MontoDestino;
				if("OK".equals(calculoSalidaMonedaAdicional.Resultado)) mov.MontoMercadoMonedaAdicionalPasivo = calculoSalidaMonedaAdicional.MontoDestino;
				
				//Se deben redondear montos al peso
				mov.MontoMercadoMonedaSistemaPasivo = mov.MontoMercadoMonedaSistemaPasivo.setScale(0, BigDecimal.ROUND_HALF_UP);
				mov.MontoMercadoMonedaAdicionalPasivo = mov.MontoMercadoMonedaAdicionalPasivo.setScale(0, BigDecimal.ROUND_HALF_UP);
				mov.MontoMercadoMonedaInstrumentoPasivo = mov.MontoMercadoMonedaInstrumentoPasivo.setScale(0, BigDecimal.ROUND_HALF_UP);
			}
			
			List<Long> idsInstrumentos = new ArrayList<Long>();
			List<String> idsClasificacionTipoInstrumento = new ArrayList<String>();
			List<String> idsBovedas = new ArrayList<String>();
			List<String> idsEntidadClasificadoraRiesgo = new ArrayList<String>();
			List<Long> codigosRubros = new ArrayList<Long>();
			List<String> idsTipoInstrumento = new ArrayList<String>();
			
			//RECORREMOS PARA SACAR LOS IDS DE BUSQUEDA
			for(Movimiento mov : movimientos){
				if(!idsInstrumentos.contains(mov.IdInstrumento)) idsInstrumentos.add(mov.IdInstrumento);
				if(!idsClasificacionTipoInstrumento.contains(mov.IdClasificacionTipoInstrumento)) idsClasificacionTipoInstrumento.add(mov.IdClasificacionTipoInstrumento);
				if(!idsBovedas.contains(mov.IdBoveda)) idsBovedas.add(mov.IdBoveda);
				if(!idsTipoInstrumento.contains(mov.IdTipoInstrumento)) idsTipoInstrumento.add(mov.IdTipoInstrumento);
			}
			
			params.clear();
			params.put("IdInstrumento", idsInstrumentos);
			List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_INSTRUMENTO, params, Instrumento.class);
			params.clear();
			params.put("IdClasificacionTipoInstrumento", idsClasificacionTipoInstrumento);
			List<ClasificacionTipoInstrumento> clasificacionesTipoInstrumento = sql.query(con, SQL_SELECT_CLASIFICACION_INSTRUMENTO, params, ClasificacionTipoInstrumento.class);
			params.clear();
			params.put("IdBoveda", idsBovedas);
			List<Boveda> bovedas = sql.query(con, SQL_SELECT_BOVEDA, params, Boveda.class);
			params.clear();
			params.put("IdTipoInstrumento", idsTipoInstrumento);
			List<TipoInstrumento> tiposInstrumentos = sql.query(con, SQL_SELECT_TIPO_INSTRUMENTO, params, TipoInstrumento.class);
			List<TipoInstrumentoClasificacionRiesgo> tiposInstrumentosClasificacionRiesgo = sql.query(con, SQL_SELECT_TIPO_INSTRUMENTO_CLASIFICACION_RIESGO, params, TipoInstrumentoClasificacionRiesgo.class);
			params.clear();
			
			//RECORREMOS LOS INSTRUMENTOS PARA SACAR RUBROS
			for(Instrumento ins : instrumentos){
				if(!codigosRubros.contains(ins.CodigoRubro)) codigosRubros.add(ins.CodigoRubro);
			}
			
			//RECORREMOS LOS CLASIFICACION RIESGO PARA SACAR ENTIDADES
			for(TipoInstrumentoClasificacionRiesgo tip : tiposInstrumentosClasificacionRiesgo){
				if(!idsEntidadClasificadoraRiesgo.contains(tip.IdEntidadClasificadoraRiesgo)) idsEntidadClasificadoraRiesgo.add(tip.IdEntidadClasificadoraRiesgo);
			}
			
			params.clear();
			params.put("CodigoRubro",codigosRubros);
			List<Rubro> rubros = sql.query(con, SQL_SELECT_RUBRO, params, Rubro.class);
			params.clear();
			params.put("IdInstrumento", idsInstrumentos);
			params.put("IdEntidadClasificadoraRiesgo", idsEntidadClasificadoraRiesgo);
			List<InstrumentoValorClasificacionRiesgo> instrumentosValorClasificacionRiesgo = sql.query(con, SQL_SELECT_INSTRUMENTO_VALOR_CLASIFICACION_RIESGO, params, InstrumentoValorClasificacionRiesgo.class);
			
			//PASAMOS A HASHMAP LAS LISTAS
			HashMap<Long,Object> hInstrumentos = Utils.List2Hash(instrumentos, "IdInstrumento");
			HashMap<String,Object> hClasificacionesTipoInstrumento = Utils.List2Hash(clasificacionesTipoInstrumento, "IdClasificacionTipoInstrumento");
			HashMap<String,Object> hBovedas = Utils.List2Hash(bovedas, "IdBoveda");
			HashMap<String,Object> hTiposInstrumentos = Utils.List2Hash(tiposInstrumentos, "IdTipoInstrumento");
			HashMap<String,Object> hTiposInstrumentosClasificacionRiesgo = Utils.List2Hash(tiposInstrumentosClasificacionRiesgo, "IdTipoInstrumento");
			HashMap<Long,Object> hCodigosRubros = Utils.List2Hash(rubros, "CodigoRubro");
			HashMap<String,Object> hInstrumentosValorClasificacionRiesgo = Utils.List2Hash(instrumentosValorClasificacionRiesgo, "Id");
		
			calcularParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntradaDv = new CalcularParidadEntrada();
			calcularParidadEntradaDv.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntradaDv.p_IdTrabajo = 0L;
			List<String> listaParidades = new ArrayList<String>();
			for (Movimiento mov : movimientos) {
				if (mov.EsDividendo.equalsIgnoreCase("S") && mov.FechaEfectivaMovimiento!= null && !mov.IdMonedaInstrumento.equalsIgnoreCase(monedaSistema)) {
					String llave = mov.IdMonedaInstrumento+monedaSistema+mov.FechaEfectivaMovimiento;
					CalculoEntrada calculoMonedaUSD = new CalculoEntrada();
					calculoMonedaUSD.IdInterno = 0L;
					calculoMonedaUSD.IdTrabajo = 0L;
					calculoMonedaUSD.Fecha = mov.FechaEfectivaMovimiento;
					calculoMonedaUSD.IdMonedaOrigen = mov.IdMonedaInstrumento;
					calculoMonedaUSD.IdMonedaDestino = monedaSistema;
					calculoMonedaUSD.ValorCambioLocalModificado = BigDecimal.ZERO;
					calculoMonedaUSD.MontoOrigen = BigDecimal.ONE;
					calculoMonedaUSD.IdUso = "CARTOLA";				
					calcularParidadEntradaDv.p_CalculosEntrada.add(calculoMonedaUSD);
					listaParidades.add(llave);
					
					llave = "USD"+monedaSistema+mov.FechaEfectivaMovimiento;
					CalculoEntrada calculoEntradaMonedaSistema = new CalculoEntrada();
					calculoEntradaMonedaSistema.IdInterno = 0L;
					calculoEntradaMonedaSistema.IdTrabajo = 0L;
					calculoEntradaMonedaSistema.Fecha = mov.FechaEfectivaMovimiento;
					calculoEntradaMonedaSistema.IdMonedaOrigen = "USD";
					calculoEntradaMonedaSistema.IdMonedaDestino = monedaSistema;
					calculoEntradaMonedaSistema.ValorCambioLocalModificado = BigDecimal.ZERO;
					calculoEntradaMonedaSistema.MontoOrigen = BigDecimal.ONE;
					calculoEntradaMonedaSistema.IdUso = "CARTOLA";				
					calcularParidadEntradaDv.p_CalculosEntrada.add(calculoEntradaMonedaSistema);
					listaParidades.add(llave);	
				}
			}
			CalcularParidadSalida calcularParidadDvSalida = calcularParidad.logica(con, calcularParidadEntradaDv);
			HashMap<String, BigDecimal> salidaCalculoParidad = new HashMap<String, BigDecimal>();
			for (CalculoSalida res : calcularParidadDvSalida.p_out_CalculosSalida) {
				String llave = res.IdMonedaOrigen + res.IdMonedaDestino+res.Fecha.toString();
				salidaCalculoParidad.put(llave, res.ValorParametroFinanciero);
			}
			HashMap<String, BigDecimal> mapDvEnMonedaSistema = new HashMap<String, BigDecimal>();
			HashMap<String, BigDecimal> mapDvMonedaInstrumento = new HashMap<String, BigDecimal>();
			for (Movimiento mov : movimientos) {
				if (mov.EsDividendo.equalsIgnoreCase("S")) {
					String key = mov.Nemotecnico + mov.IdPortafolio.toString();
					mapDvMonedaInstrumento.put(key, mov.GananciaPerdidaMonedaInstrumento);

					String llaveConversionUSD = mov.IdMonedaInstrumento+monedaSistema+mov.FechaEfectivaMovimiento;
					String llaveConversionCLP = "USD"+monedaSistema+mov.FechaEfectivaMovimiento;
					if (mov.IdMonedaInstrumento.equalsIgnoreCase(monedaSistema)) {
						mapDvEnMonedaSistema.put(key, mov.GananciaPerdida);
					}
					else if (salidaCalculoParidad.get(llaveConversionCLP) != null && salidaCalculoParidad.get(llaveConversionUSD) != null) {						
						BigDecimal valParidad = !llaveConversionUSD.equalsIgnoreCase(llaveConversionCLP)? salidaCalculoParidad.get(llaveConversionCLP).multiply(salidaCalculoParidad.get(llaveConversionUSD), MathContext.DECIMAL128) : salidaCalculoParidad.get(llaveConversionUSD);
						BigDecimal dividendoRecibido = mov.GananciaPerdida.multiply(valParidad, MathContext.DECIMAL128);
						mapDvEnMonedaSistema.put(key, dividendoRecibido);
					}
				}
			}
			
			for(Movimiento mov : movimientos){
				Instrumento instrumento = (Instrumento) hInstrumentos.get(mov.IdInstrumento);
				ClasificacionTipoInstrumento clasificacionInstrumento = (ClasificacionTipoInstrumento) hClasificacionesTipoInstrumento.get(mov.IdClasificacionTipoInstrumento);
				Boveda boveda = (Boveda) hBovedas.get(mov.IdBoveda);
				TipoInstrumento tipoInstrumento = (TipoInstrumento) hTiposInstrumentos.get(mov.IdTipoInstrumento);
				TipoInstrumentoClasificacionRiesgo tipoInstrumentoClasificacionRiesgo = (TipoInstrumentoClasificacionRiesgo) hTiposInstrumentosClasificacionRiesgo.get(mov.IdTipoInstrumento);
				
				Rubro rubro = instrumento == null ? null : (Rubro) hCodigosRubros.get(instrumento.CodigoRubro);
				InstrumentoValorClasificacionRiesgo instrumentoValorClasificacionRiesgo = tipoInstrumentoClasificacionRiesgo == null ? null : (InstrumentoValorClasificacionRiesgo) hInstrumentosValorClasificacionRiesgo.get(tipoInstrumentoClasificacionRiesgo.IdEntidadClasificadoraRiesgo + "_" + mov.IdInstrumento);
				
				if(instrumento != null && tipoInstrumento != null && ((boveda != null && entrada.p_FiltroUbicacionBoveda.equals(boveda.Ubicacion)) || entrada.p_FiltroUbicacionBoveda.isEmpty())){
					SalidaCarteraRV salida = new SalidaCarteraRV();
					salida.IdCliente = mov.IdCliente;
					salida.IdPortafolio = mov.IdPortafolio;
					salida.IdBoveda = mov.IdBoveda;
					salida.UbicacionSolicitada = entrada.p_FiltroUbicacionBoveda;
					
					if(boveda != null){
						salida.CodigoBoveda = boveda.Codigo;
						salida.NombreBoveda = boveda.Nombre;
						salida.Ubicacion = boveda.Ubicacion;
						salida.UbicacionSolicitada = entrada.p_FiltroUbicacionBoveda.isEmpty() ? boveda.Ubicacion : salida.UbicacionSolicitada;
					}
					salida.IdClasificacionTipoInstrumento 	= mov.IdClasificacionTipoInstrumento;
					salida.IdTipoInstrumento 				= mov.IdTipoInstrumento;
					salida.NombreTipoInstrumento 			= tipoInstrumento.Nombre;
					salida.IdInstrumento 					= mov.IdInstrumento;
					salida.Nemotecnico 						= mov.Nemotecnico;
					salida.FechaEmision 					= Fecha.stringToSqlDate(instrumento.FechaEmision);
					salida.FechaVencimiento 				= Fecha.stringToSqlDate(instrumento.FechaVencimiento);
					salida.PlazoResidual 					= (Long.valueOf(salida.FechaVencimiento.split("-")[0]) - Long.valueOf(Fecha.stringToSqlDate(entrada.p_FechaHasta).split("-")[0])) + "/00";
					salida.Rubro 							= rubro == null ? "" : rubro.Descripcion;
					salida.IdTipoReajuste 					= instrumento.IdTipoReajuste;
					salida.IdEmisor 						= instrumento.IdEmisor;
					salida.RazonSocial 						= instrumento.RazonSocial;
					salida.CorteMinimo 						= instrumento.CorteMinimo;
					salida.TasaEmision 						= instrumento.Tasa;
					salida.ValorClasificacionRiesgo			= instrumentoValorClasificacionRiesgo == null ?  "" : instrumentoValorClasificacionRiesgo.ValorClasificacionRiesgo;
	
					salida.DisponibleActivo 				= mov.DisponibleActivo;
					salida.DisponiblePasivo					= mov.DisponiblePasivo;
					salida.GarantiaActivo					= mov.GarantiaActivo;
					salida.GarantiaPasivo					= mov.GarantiaPasivo;
					salida.PrestamoActivo					= mov.PrestamoActivo;
					salida.PrestamoPasivo					= mov.PrestamoPasivo;
					salida.PlazoActivo						= mov.PlazoActivo;
					salida.PlazoPasivo						= mov.PlazoPasivo;	
					
					salida.PrecioPromedioCompra = mov.PrecioPromedioCompra;
					salida.PrecioPromedioCompraPasivo = mov.PrecioPromedioCompraPasivo;
					salida.MontoPromedioCompraMonedaSistemaActivo = mov.MontoPromedioCompraMonedaSistemaActivo;
					salida.MontoPromedioCompraMonedaAdicionalActivo = mov.MontoPromedioCompraMonedaAdicionalActivo;
					salida.MontoPromedioCompraMonedaSistemaPasivo = mov.MontoPromedioCompraMonedaSistemaPasivo;
					salida.MontoPromedioCompraMonedaAdicionalPasivo = mov.MontoPromedioCompraMonedaAdicionalPasivo;
					salida.TasaMercado = mov.TasaMercado;
					salida.PrecioMercado = mov.PrecioMercado;
					salida.MontoMercadoMonedaSistemaActivo = mov.MontoMercadoMonedaSistemaActivo;
					salida.MontoMercadoMonedaAdicionalActivo = mov.MontoMercadoMonedaAdicionalActivo;
					salida.MontoMercadoMonedaInstrumentoActivo = mov.MontoMercadoMonedaInstrumentoActivo;
					salida.MontoMercadoMonedaSistemaPasivo = mov.MontoMercadoMonedaSistemaPasivo;
					salida.MontoMercadoMonedaAdicionalPasivo = mov.MontoMercadoMonedaAdicionalPasivo;
					salida.MontoMercadoMonedaInstrumentoPasivo = mov.MontoMercadoMonedaInstrumentoPasivo;
					if(monedaSistema.equals(mov.IdMonedaInstrumento) || (!monedaSistema.equals(mov.IdMonedaInstrumento) && "S".equals(entrada.p_FiltroSoloMonedaSistema))){
						salida.Moneda = monedaSistema;
						salida.MontoPromedio = mov.MontoPromedioCompraMonedaSistemaActivo;
						salida.MontoMercado = mov.MontoMercadoMonedaSistemaActivo;
					} else {
						salida.Moneda = monedaAdicional;
						salida.MontoPromedio = mov.MontoPromedioCompraMonedaAdicionalActivo;
						salida.MontoMercado = mov.MontoMercadoMonedaAdicionalActivo;
					}
					salida.IdMonedaOperacion = mov.IdMonedaInstrumento;
					salida.GananciaPerdida =  mov.GananciaPerdida;
					if (mov.PrecioPromedioCompra != null && mov.PrecioMercado != null && mov.PrecioPromedioCompra.compareTo(BigDecimal.ZERO) != 0 && mov.PrecioMercado.compareTo(BigDecimal.ZERO) != 0){
						if((mov.DisponibleActivo.add(mov.GarantiaActivo,  MathContext.DECIMAL128)).compareTo(BigDecimal.ZERO)==0) {
							salida.RentabilidadActivo = mov.PrecioMercado.subtract(mov.PrecioPromedioCompra, MathContext.DECIMAL128).divide(mov.PrecioPromedioCompra, MathContext.DECIMAL128).multiply(new BigDecimal(100), MathContext.DECIMAL128);
						}else {
							BigDecimal valorMercado = mov.PrecioMercado.multiply(mov.DisponibleActivo.add(mov.GarantiaActivo,  MathContext.DECIMAL128), MathContext.DECIMAL128);
							BigDecimal valorCompra = mov.PrecioPromedioCompra.multiply(mov.DisponibleActivo.add(mov.GarantiaActivo,  MathContext.DECIMAL128), MathContext.DECIMAL128);
							salida.RentabilidadActivo = valorMercado.subtract(valorCompra, MathContext.DECIMAL128).add(mov.GananciaPerdida, MathContext.DECIMAL128).divide(valorCompra, MathContext.DECIMAL128).multiply(new BigDecimal(100), MathContext.DECIMAL128);
						}
					} else {
						salida.RentabilidadActivo = BigDecimal.ZERO;
					}
					if (mov.PrecioPromedioCompraPasivo.compareTo(BigDecimal.ZERO) != 0 && mov.PrecioMercado.compareTo(BigDecimal.ZERO) != 0){
						if((mov.DisponiblePasivo.add(mov.GarantiaPasivo,  MathContext.DECIMAL128)).compareTo(BigDecimal.ZERO)==0) {
							salida.RentabilidadPasivo = mov.PrecioMercado.subtract(mov.PrecioPromedioCompraPasivo, MathContext.DECIMAL128).divide(mov.PrecioPromedioCompraPasivo, MathContext.DECIMAL128).multiply(new BigDecimal(100), MathContext.DECIMAL128);
						}else {

							BigDecimal valorMercado = mov.PrecioMercado.multiply(mov.DisponiblePasivo.add(mov.GarantiaPasivo,  MathContext.DECIMAL128), MathContext.DECIMAL128);
							BigDecimal valorCompra = mov.PrecioPromedioCompraPasivo.multiply(mov.DisponiblePasivo.add(mov.GarantiaPasivo,  MathContext.DECIMAL128), MathContext.DECIMAL128);
							salida.RentabilidadPasivo = valorMercado.subtract(valorCompra, MathContext.DECIMAL128).subtract(mov.GananciaPerdida, MathContext.DECIMAL128).divide(valorCompra, MathContext.DECIMAL128).multiply(new BigDecimal(100), MathContext.DECIMAL128);
						}
					} else {
						salida.RentabilidadPasivo = BigDecimal.ZERO;
					}
					salida.descMercado = clasificacionInstrumento == null ? "" : clasificacionInstrumento.Nombre;
					salidas.add(salida);
				}
			}
			
			for (SalidaCarteraRV salida : salidas) {
				BigDecimal dividendoRecibido = mapDvEnMonedaSistema.get(salida.Nemotecnico+salida.IdPortafolio.toString());
				BigDecimal dRMonedaInstrumento = mapDvMonedaInstrumento.get(salida.Nemotecnico+salida.IdPortafolio.toString());
				if (dividendoRecibido != null) {
					salida.GananciaPerdida = dividendoRecibido;
				}
				if (dRMonedaInstrumento != null) {
					salida.GananciaPerdidaMonedaInstrumento = dRMonedaInstrumento;
				}
			}

			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//			OPTIMIZA CARTOLA - GUARDA LOS REGISTROS EN TABLA DE PASO PARA SER UTILIZADOS EN EL SIGUIENTE LLAMADO //
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			if(primeraEjecucion) {
				try {
					List<SalidaCarteraRVPaso> listOutPaso =  new ArrayList();
					for(SalidaCarteraRV salida : salidas) {
						SalidaCarteraRVPaso outPaso = new SalidaCarteraRVPaso();
						outPaso.IdNomina = nomina.IdNomina;
						outPaso.IdTrabajo = idTrabajo;
						outPaso.IdCliente = salida.IdCliente;
						outPaso.IdPortafolio = salida.IdPortafolio;
						outPaso.IdBoveda = salida.IdBoveda;
						outPaso.CodigoBoveda = salida.CodigoBoveda;
						outPaso.NombreBoveda = salida.NombreBoveda;
						outPaso.Ubicacion = salida.Ubicacion;
						outPaso.UbicacionSolicitada = salida.UbicacionSolicitada;
						outPaso.IdClasificacionTipoInstrumento = salida.IdClasificacionTipoInstrumento;
						outPaso.IdTipoInstrumento = salida.IdTipoInstrumento;
						outPaso.NombreTipoInstrumento = salida.NombreTipoInstrumento;
						outPaso.descMercado = salida.descMercado;
						outPaso.IdInstrumento = salida.IdInstrumento;
						outPaso.Nemotecnico = salida.Nemotecnico;
						outPaso.FechaVencimiento = salida.FechaVencimiento;
						outPaso.FechaEmision = salida.FechaEmision;
						outPaso.PlazoResidual = salida.PlazoResidual;
						outPaso.Rubro = salida.Rubro;
						outPaso.IdTipoReajuste = salida.IdTipoReajuste;
						outPaso.IdEmisor = salida.IdEmisor;
						outPaso.RazonSocial = salida.RazonSocial;
						outPaso.CorteMinimo = salida.CorteMinimo;
						outPaso.TasaEmision = salida.TasaEmision;
						outPaso.ValorClasificacionRiesgo = salida.ValorClasificacionRiesgo;
						outPaso.DisponibleActivo = salida.DisponibleActivo;
						outPaso.DisponiblePasivo = salida.DisponiblePasivo;
						outPaso.GarantiaActivo = salida.GarantiaActivo;
						outPaso.GarantiaPasivo = salida.GarantiaPasivo;
						outPaso.PrestamoActivo = salida.PrestamoActivo;
						outPaso.PrestamoPasivo = salida.PrestamoPasivo;
						outPaso.PlazoActivo = salida.PlazoActivo;
						outPaso.PlazoPasivo = salida.PlazoPasivo;
						outPaso.PrecioPromedioCompra = salida.PrecioPromedioCompra;
						outPaso.PrecioPromedioCompraPasivo = salida.PrecioPromedioCompraPasivo;
						outPaso.MontoPromedioCompraMonedaSistemaActivo = salida.MontoPromedioCompraMonedaSistemaActivo;
						outPaso.MontoPromedioCompraMonedaAdicionalActivo = salida.MontoPromedioCompraMonedaAdicionalActivo;
						outPaso.TasaMercado = salida.TasaMercado;
						outPaso.PrecioMercado = salida.PrecioMercado;
						outPaso.MontoMercadoMonedaSistemaActivo = salida.MontoMercadoMonedaSistemaActivo;
						outPaso.MontoMercadoMonedaAdicionalActivo = salida.MontoMercadoMonedaAdicionalActivo;
						outPaso.Moneda = salida.Moneda;
						outPaso.IdMonedaOperacion = salida.IdMonedaOperacion;
						outPaso.MontoPromedio = salida.MontoPromedio;
						outPaso.MontoMercado = salida.MontoMercado;
						outPaso.GananciaPerdida = salida.GananciaPerdida;
						outPaso.GananciaPerdidaMonedaInstrumento = salida.GananciaPerdidaMonedaInstrumento;
						outPaso.RentabilidadActivo = salida.RentabilidadActivo;
						outPaso.RentabilidadPasivo = salida.RentabilidadPasivo;
						outPaso.MontoPromedioCompraMonedaSistemaPasivo = salida.MontoPromedioCompraMonedaSistemaPasivo;
						outPaso.MontoPromedioCompraMonedaAdicionalPasivo = salida.MontoPromedioCompraMonedaAdicionalPasivo;
						outPaso.MontoMercadoMonedaSistemaPasivo = salida.MontoMercadoMonedaSistemaPasivo;
						outPaso.MontoMercadoMonedaAdicionalPasivo = salida.MontoMercadoMonedaAdicionalPasivo;
						outPaso.MontoMercadoMonedaInstrumentoActivo = salida.MontoMercadoMonedaInstrumentoActivo;
						outPaso.MontoMercadoMonedaInstrumentoPasivo = salida.MontoMercadoMonedaInstrumentoPasivo;
						listOutPaso.add(outPaso);
					}
					sql.batch(con, SQL_INSERT_CARTERA_RV_OUT, listOutPaso, SalidaCarteraRVPaso.class);
				} catch (Exception e) {
					logger.info("INICIO::CarteraRV -ERROR SQL_SELECT_OUT_SERVICIO_EJECUTADO "+e);
				}
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//			FIN OPTIMIZA CARTOLA - GUARDA LOS REGISTROS EN TABLA DE PASO PARA SER UTILIZADOS EN EL SIGUIENTE LLAMADO //
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			salidas.sort(new Comparator<SalidaCarteraRV>() {
				@Override
				public int compare(SalidaCarteraRV mov1, SalidaCarteraRV mov2) {
					  if(mov1.Nemotecnico.compareTo(mov2.Nemotecnico) != 0){
				        	return mov1.Nemotecnico.compareTo(mov2.Nemotecnico);
				        }
					  else  {
				        	return mov1.IdMonedaOperacion.compareTo(mov2.IdMonedaOperacion);
				        }
						
				     
				     }
				}
			);
			
		}
		List<HashMap<String,Object>> resultadoCarteraRV = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("carteraRV", resultadoCarteraRV);
		
		long finServicio = System.currentTimeMillis();
		log.write("FIN::CarteraRV : "  + (finServicio - inicioServicio));
		return out;
	}
	
	private List<SalidaCarteraRV> parseaObjetoDePaso(List<SalidaCarteraRVPaso> listObjPaso,Long idTrabajo){
		List<SalidaCarteraRV> listSalidaCartera = new ArrayList<>();
		for(SalidaCarteraRVPaso p : listObjPaso) {
			if(p.IdTrabajo.compareTo(idTrabajo) == 0L) {
				SalidaCarteraRV outExistente = new SalidaCarteraRV();
				outExistente.IdCliente = p.IdCliente;
				outExistente.IdPortafolio = p.IdPortafolio;
				outExistente.IdBoveda = p.IdBoveda;
				outExistente.CodigoBoveda = p.CodigoBoveda;
				outExistente.NombreBoveda = p.NombreBoveda;
				outExistente.Ubicacion = p.Ubicacion;
				outExistente.UbicacionSolicitada = p.UbicacionSolicitada;
				outExistente.IdClasificacionTipoInstrumento = p.IdClasificacionTipoInstrumento;
				outExistente.IdTipoInstrumento = p.IdTipoInstrumento;
				outExistente.NombreTipoInstrumento = p.NombreTipoInstrumento;
				outExistente.descMercado = p.descMercado;
				outExistente.IdInstrumento = p.IdInstrumento;
				outExistente.Nemotecnico = p.Nemotecnico;
				outExistente.FechaVencimiento = p.FechaVencimiento;
				outExistente.FechaEmision = p.FechaEmision;
				outExistente.PlazoResidual = p.PlazoResidual;
				outExistente.Rubro = p.Rubro;
				outExistente.IdTipoReajuste = p.IdTipoReajuste;
				outExistente.IdEmisor = p.IdEmisor;
				outExistente.RazonSocial = p.RazonSocial;
				outExistente.CorteMinimo = p.CorteMinimo;
				outExistente.TasaEmision = p.TasaEmision;
				outExistente.ValorClasificacionRiesgo = p.ValorClasificacionRiesgo;
				outExistente.DisponibleActivo = p.DisponibleActivo;
				outExistente.DisponiblePasivo = p.DisponiblePasivo;
				outExistente.GarantiaActivo = p.GarantiaActivo;
				outExistente.GarantiaPasivo = p.GarantiaPasivo;
				outExistente.PrestamoActivo = p.PrestamoActivo;
				outExistente.PrestamoPasivo = p.PrestamoPasivo;
				outExistente.PlazoActivo = p.PlazoActivo;
				outExistente.PlazoPasivo = p.PlazoPasivo;
				outExistente.PrecioPromedioCompra = p.PrecioPromedioCompra;
				outExistente.PrecioPromedioCompraPasivo = p.PrecioPromedioCompraPasivo;
				outExistente.MontoPromedioCompraMonedaSistemaActivo = p.MontoPromedioCompraMonedaSistemaActivo;
				outExistente.MontoPromedioCompraMonedaAdicionalActivo = p.MontoPromedioCompraMonedaAdicionalActivo;
				outExistente.TasaMercado = p.TasaMercado;
				outExistente.PrecioMercado = p.PrecioMercado;
				outExistente.MontoMercadoMonedaSistemaActivo = p.MontoMercadoMonedaSistemaActivo;
				outExistente.MontoMercadoMonedaAdicionalActivo = p.MontoMercadoMonedaAdicionalActivo;
				outExistente.Moneda = p.Moneda;
				outExistente.IdMonedaOperacion = p.IdMonedaOperacion;
				outExistente.MontoPromedio = p.MontoPromedio;
				outExistente.MontoMercado = p.MontoMercado;
				outExistente.GananciaPerdida = p.GananciaPerdida;
				outExistente.GananciaPerdidaMonedaInstrumento = p.GananciaPerdidaMonedaInstrumento;
				outExistente.RentabilidadActivo = p.RentabilidadActivo;
				outExistente.RentabilidadPasivo = p.RentabilidadPasivo;
				outExistente.MontoPromedioCompraMonedaSistemaPasivo = p.MontoPromedioCompraMonedaSistemaPasivo;
				outExistente.MontoPromedioCompraMonedaAdicionalPasivo = p.MontoPromedioCompraMonedaAdicionalPasivo;
				outExistente.MontoMercadoMonedaSistemaPasivo = p.MontoMercadoMonedaSistemaPasivo;
				outExistente.MontoMercadoMonedaAdicionalPasivo = p.MontoMercadoMonedaAdicionalPasivo;
				outExistente.MontoMercadoMonedaInstrumentoActivo = p.MontoMercadoMonedaInstrumentoActivo != null ? p.MontoMercadoMonedaInstrumentoActivo : BigDecimal.ZERO;
				outExistente.MontoMercadoMonedaInstrumentoPasivo = p.MontoMercadoMonedaInstrumentoPasivo != null ? p.MontoMercadoMonedaInstrumentoPasivo : BigDecimal.ZERO;
				listSalidaCartera.add(outExistente);
			}
		}
		return listSalidaCartera;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("MASTER02", "connections.properties")) {
		//	try (Connection con = PlataformaUtils.getMBIPREConnection()) {	 
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 9218L;
		Objin.p_FechaDesde = Fecha.toDate("2026-03-01");
		Objin.p_FechaHasta = Fecha.toDate("2026-03-30");
		
		Dato dato = new Dato();
		dato.IdCliente = 12559L;
		dato.IdPortafolio =13886L;
		Objin.p_Datos.add(dato);

		
		
		CarteraRV  demo= new CarteraRV();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
		}
		System.out.println("== END ==");
	}

}