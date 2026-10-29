package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
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

import cl.bcs.custodia.AnularGarantia.RetiroCertificado;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaEntrada;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaSalida;
import cl.bcs.custodia.IngresarGarantiaJava.ObjGarantiaCuentaInversion;
import cl.bcs.custodia.IngresarGarantiaJava.ObjGarantiaCustodia;
import cl.bcs.custodia.IngresarGarantiaJava.ObjGarantiaEspecie;
import cl.bcs.custodia.RetirarGarantiaJava.ObjGarantiasARetirar;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaEntrada;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaSalida;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores.FechasIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarMovimientoContraparteGarantia implements JavaCall{

	static final Logger logger = LogManager.getLogger(GenerarMovimientoContraparteGarantia.class);
	
	private final String SQL_SELECT_IDGARANTIA = loadResourceToString("cl/bcs/custodia/sql/GenerarMovimientoContraparteGarantia_SelectIdGarantias.sql");
	private final String SQL_SELECT_MOVIMIENTOS_EN_TRANSITO_RETIRO_CERTIFICADOS = loadResourceToString("cl/bcs/custodia/sql/AnularGarantia_SelectMovimientosEnTransitoRetiroGarantiaCertificado.sql");
	private final String SQL_UPDATE_EXCLUYE_DE_CARTOLA = loadResourceToString("cl/bcs/custodia/sql/GenerarMovimientoContraparteGarantia_UpdateExcluyeMovimientoEnCartola.sql");
	private final String SQL_SELECT_DATOS_RETIRO_GARANTIA = loadResourceToString("cl/bcs/custodia/sql/GenerarMovimientoContraparteGarantia_SelectDatosRetiroGarantia.sql");
	private final String SQL_UPDATE_GARANTIAS_VIGENTES_ORIGEN = loadResourceToString("cl/bcs/custodia/sql/GenerarMovimientoContraparteGarantia_UpdateGarantiasVigentesOrigen.sql");
	
	public static class EntradaGenerarMovimientoContraparteGarantia extends BcsPojo {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public Long 	p_IdEmpleado;
		public String 	p_IdModulo;
		public String 	p_UsaReglasNegocio;
		public String 	p_SimularOperacion;
		public	String	p_IndicadorIngEgr;
		public List<MovimientoGarantia>	 p_MovimientosIngreso;
		public List<MovimientoGarantia>	 p_MovimientosRetiro;
		
		public EntradaGenerarMovimientoContraparteGarantia () {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdEmpleado = 0L;
			this.p_IdModulo = "";
			this.p_UsaReglasNegocio = "S";
			this.p_SimularOperacion = "S";
			this.p_IndicadorIngEgr = "";
			this.p_MovimientosIngreso = new ArrayList<MovimientoGarantia>();
			this.p_MovimientosRetiro = new ArrayList<MovimientoGarantia>();
		}
	}
	
	public static class MovimientoGarantia extends BcsPojo{
		public Long IdMovimientoCarteraEnTransito;
		public String IdTipoOperacion;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal Cantidad;
		public Long IdCliente;
		public String IdConcepto;
		public Long IdInstrumento;
		public Long IdPortafolio;
		public BigDecimal PrecioCompra;
		public BigDecimal MontoCompra;
		public BigDecimal MontoCompraMonedaInstrumento;
		public BigDecimal ValorRescate;
		public BigDecimal TasaCompra;
		public Long IdUsuario;
		public String IdBolsa;
		public String IdMonedaInstrumento;
		public String IdOrigenGarantia;
		public String IdTipoComprobante;
		public String IdEstado;
		public String IdCondicionLiquidacion;
		public String EsGarantia;
		public String FechaOperacion;
		public Long IdAgente;
		public String IdTipoInstrumento;
		public Long IdOperacion;
		public Long IdContraparteGarantia;
		public BigDecimal CorteMinimo;
		public String EsFungible;
		public Long PlazoEnDias;
		public String IdEmisor;
		public String NemoSVS;
		public String FechaEmisionInstrumento;
		public String FechaVencimientoInstrumento;
		public String IdTipoReajuste;
		public Long IdEspecieGarantia;
		public String Glosa;
		public String ExcluyeMovimientoEnCartola;
		
		public MovimientoGarantia() {
			this.IdMovimientoCarteraEnTransito = 0L;
			this.IdTipoOperacion = "";
			this.IdClasificacionTipoInstrumento = "";
			this.Cantidad = BigDecimal.ZERO;
			this.IdCliente = 0L;
			this.IdConcepto = "";
			this.IdInstrumento = 0L;
			this.IdPortafolio = 0L;
			this.PrecioCompra = BigDecimal.ZERO;
			this.MontoCompra = BigDecimal.ZERO;
			this.MontoCompraMonedaInstrumento = BigDecimal.ZERO;
			this.ValorRescate = BigDecimal.ZERO;
			this.TasaCompra = BigDecimal.ZERO;
			this.IdUsuario = 0L;
			this.IdBolsa = "";
			this.IdMonedaInstrumento = "";
			this.IdOrigenGarantia = "";
			this.IdTipoComprobante = "";
			this.IdEstado = "";
			this.IdCondicionLiquidacion = "";
			this.EsGarantia = "S";
			this.FechaOperacion = "0001-01-01";
			this.IdAgente = 0L;
			this.IdTipoInstrumento = "";
			this.IdOperacion = 0L;
			this.IdContraparteGarantia = 0L;
			this.CorteMinimo = BigDecimal.ZERO;
			this.EsFungible = "N";
			this.PlazoEnDias = 0L;
			this.IdEmisor = "";
			this.NemoSVS = "";
			this.FechaEmisionInstrumento = "0001-01-01";
			this.FechaVencimientoInstrumento = "0001-01-01";
			this.IdTipoReajuste = "";
			this.IdEspecieGarantia = 0L;
			this.Glosa = "";
			this.ExcluyeMovimientoEnCartola = "N";
		}
	}
	public static class Garantia extends BcsPojo{
		public Long IdMovimientoCarteraEnTransito;
		public Long IdGarantia;
		public Long IdGarantiaVigente;
		public Long IdContraparteGarantia;
		public String IdBolsa;
		public Long IdAgente;
		public BigDecimal CantidadVigente;
		public BigDecimal INGMontoATasaGarantiaActualVigente;
		public BigDecimal INGMontoGarantiaActualMonedaInstrumentoVigente;
		
		public Garantia () {
			this.IdMovimientoCarteraEnTransito = 0L;
			this.IdGarantia = 0L;
			this.IdGarantiaVigente = 0L;
			this.IdContraparteGarantia = 0L;
			this.IdBolsa = "";
			this.IdAgente = 0L;
			this.CantidadVigente = BigDecimal.ZERO;
			this.INGMontoATasaGarantiaActualVigente = BigDecimal.ZERO;
			this.INGMontoGarantiaActualMonedaInstrumentoVigente = BigDecimal.ZERO;
		}
	}
	public static class SalidaGenerarMovimientoContraparteGarantia extends BcsPojo {
		public Long					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public SalidaGenerarMovimientoContraparteGarantia () {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public SalidaGenerarMovimientoContraparteGarantia logica (Connection con, EntradaGenerarMovimientoContraparteGarantia entrada ) throws Exception {
		logger.info("INICIO::GenerarMovimientoContraparteGarantia");
		long inicio = System.currentTimeMillis();
		
		SalidaGenerarMovimientoContraparteGarantia salida = new SalidaGenerarMovimientoContraparteGarantia();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		if(!entrada.p_MovimientosIngreso.isEmpty()) {
			salida.P_OUT_MENSAJES.addAll(validacionesMovimientosEnTransito(con, entrada.p_IdEmpresa, fechaSistema, entrada.p_MovimientosIngreso));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			
			if(salida.P_OUT_ERRORESCONTADOR > 0L) {
				long fin = System.currentTimeMillis();
				logger.info("FIN::GenerarMovimientoContraparteGarantia", fin - inicio);
				return salida;
			}
			
			RetirarGarantiaJava retiroGarantia = new RetirarGarantiaJava();
			RetirarGarantiaEntrada entradaRetirar = new RetirarGarantiaEntrada();
			
			entradaRetirar.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaRetirar.p_IdUsuario = entrada.p_IdUsuario;
			entradaRetirar.p_IdModulo = entrada.p_IdModulo;
			entradaRetirar.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaRetirar.p_SimulaOperacion = entrada.p_SimularOperacion;
			entradaRetirar.p_IdEmpleado = entrada.p_IdEmpleado;
			entradaRetirar.p_FechaMovimiento = fechaSistema;
			entradaRetirar.p_IdTipoOperacion = "RETGTA";
			entradaRetirar.p_tablaRetiro = obtenerRetiros(con, entrada.p_MovimientosIngreso);
			
			RetirarGarantiaSalida salidaRetirar = retiroGarantia.logica(con, entradaRetirar);
			
			salida.P_OUT_MENSAJES.addAll(salidaRetirar.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			if(salida.P_OUT_ERRORESCONTADOR > 0L) {
				long fin = System.currentTimeMillis();
				logger.info("FIN::GenerarMovimientoContraparteGarantia", fin - inicio);
				return salida;
			}
			
			List<Long> movsEntrada = entrada.p_MovimientosIngreso.stream().map(ent -> ent.IdMovimientoCarteraEnTransito).collect(Collectors.toList());
			List<Long> movimientosGenerados = salidaRetirar.P_OUT_RESULTADOPROCESO.stream().map(sal -> sal.IdGenerarMovimientoCustodia).collect(Collectors.toList());
			
			actualizarExcluyeMovimientoEnCartola(con, movsEntrada, movimientosGenerados);
			HashMap<String, String> params = new HashMap<String, String>(1);
			for(Long mov : movimientosGenerados) {
				params.put("IdGenerarMovimientoCustodia", mov.toString());
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_GENERAR_MOVIMIENTO_CONTRAPARTIDA_OK",params,con, entrada.p_IdEmpresa));
				params.clear();
			}
		}
		
		if("RETIRO".equals(entrada.p_IndicadorIngEgr)) {
			salida.P_OUT_MENSAJES.addAll(validacionesMovimientosEnTransito(con, entrada.p_IdEmpresa, fechaSistema, entrada.p_MovimientosRetiro));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			
			if(salida.P_OUT_ERRORESCONTADOR > 0L) {
				long fin = System.currentTimeMillis();
				logger.info("FIN::GenerarMovimientoContraparteGarantia", fin - inicio);
				return salida;
			}
			
			IngresarGarantiaJava ingresoGarantia = new IngresarGarantiaJava();
			IngresarGarantiaEntrada entradaIngresar = new IngresarGarantiaEntrada();
			
			entradaIngresar.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaIngresar.p_IdUsuario = entrada.p_IdUsuario;
			entradaIngresar.p_IdModulo = entrada.p_IdModulo;
			entradaIngresar.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaIngresar.p_SimulaOperacion = entrada.p_SimularOperacion;
			entradaIngresar.p_IdEmpleado = entrada.p_IdEmpleado;
			entradaIngresar.p_Certifica = "N";
			entradaIngresar.p_FechaMovimiento = fechaSistema;
			entradaIngresar.p_GarantiaCustodia = obtenerGarantiasCustodia(con, entrada.p_IdEmpresa, monedaSistema, fechaSistema, entrada.p_MovimientosRetiro);
			entradaIngresar.p_GarantiaCuentaInversion = obtenerGarantiasCuentaInversion(con, entrada.p_IdEmpresa, monedaSistema, fechaSistema, entrada.p_MovimientosRetiro);
			entradaIngresar.p_GarantiaEspecies = obtenerGarantiasEspecie(con, entrada.p_MovimientosRetiro);
			entradaIngresar.p_EsProcesoContrapartida = "S";
			IngresarGarantiaSalida salidaIngresar = ingresoGarantia.logica(con, entradaIngresar);
			
			salida.P_OUT_MENSAJES.addAll(salidaIngresar.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			if(salida.P_OUT_ERRORESCONTADOR > 0L) {
				long fin = System.currentTimeMillis();
				logger.info("FIN::GenerarMovimientoContraparteGarantia", fin - inicio);
				return salida;
			}
			List<Long> gtasVigentes = new ArrayList<Long>();
			gtasVigentes.addAll(entradaIngresar.p_GarantiaCustodia.stream().map(gta -> gta.IdGarantiaVigente).collect(Collectors.toList()));
			gtasVigentes.addAll(entradaIngresar.p_GarantiaCuentaInversion.stream().map(gta -> gta.IdGarantiaVigente).collect(Collectors.toList()));
			gtasVigentes.addAll(entradaIngresar.p_GarantiaEspecies.stream().map(gta -> gta.IdGarantiaVigente).collect(Collectors.toList()));
			List<Long> movsEntrada = entrada.p_MovimientosRetiro.stream().map(ent -> ent.IdMovimientoCarteraEnTransito).collect(Collectors.toList());
			List<Long> movimientosGenerados = salidaIngresar.P_OUT_RESULTADOPROCESO.stream().filter(sal -> "CARTERA".equals(sal.TablaOrigen) || ("GARANTIA".equals(sal.TablaOrigen) && "INGRESO".equals(sal.TipoAccion))).map(sal -> sal.IdGenerarMovimientoCustodia).collect(Collectors.toList());
			actualizarExcluyeMovimientoEnCartola(con, movsEntrada, movimientosGenerados);
			actualizarEstadoGarantiaOriginal(con,gtasVigentes);
			HashMap<String, String> params = new HashMap<String, String>(1);
			for(Long mov : movimientosGenerados) {
				params.put("IdGenerarMovimientoCustodia", mov.toString());
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_GENERAR_MOVIMIENTO_CONTRAPARTIDA_OK",params,con, entrada.p_IdEmpresa));
				params.clear();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarMovimientoContraparteGarantia", fin - inicio);
		return salida;
	}
	
	private void actualizarEstadoGarantiaOriginal(Connection con, List<Long> gtasVigentes) throws Exception {
		logger.info("INICIO::GenerarMovimientoContraparteGarantia.actualizarEstadoGarantiaOriginal");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		try {
			List<Garantia> gtas = new ArrayList<>();
			for(Long vig : gtasVigentes) {
				Garantia garantia = new Garantia();
				garantia.IdGarantia = vig;
				gtas.add(garantia);
			}
			sql.batch(con, SQL_UPDATE_GARANTIAS_VIGENTES_ORIGEN, gtas, Garantia.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarMovimientoContraparteGarantia.actualizarEstadoGarantiaOriginal", fin - inicio);
	}

	private List<ObjGarantiaEspecie> obtenerGarantiasEspecie(Connection con, List<MovimientoGarantia> p_MovimientosRetiro) throws Exception {
		logger.info("INICIO::GenerarMovimientoContraparteGarantia.obtenerGarantiasEspecie");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(1);
        List<Long> idMovimientos = new ArrayList<Long>();
        idMovimientos.addAll(p_MovimientosRetiro.parallelStream().map(mov -> {return mov.IdMovimientoCarteraEnTransito;}).collect(Collectors.toList()));
        parametros.put("IdMovimientoCarteraEnTransito", idMovimientos);
        
        List<Garantia> datosGarantias = sql.query(con, SQL_SELECT_DATOS_RETIRO_GARANTIA, parametros, Garantia.class);
        Map<Long,Garantia> mapDatosGarantias = datosGarantias.parallelStream().collect(Collectors.toMap(gta->gta.IdMovimientoCarteraEnTransito, gta->gta));
		
		List<ObjGarantiaEspecie> garantias = new ArrayList<ObjGarantiaEspecie>();
		
		for(MovimientoGarantia mov : p_MovimientosRetiro) {
			if("ESP".equals(mov.IdOrigenGarantia)) {
				Garantia datoGarantia = mapDatosGarantias.get(mov.IdMovimientoCarteraEnTransito);
				ObjGarantiaEspecie gta = new ObjGarantiaEspecie();
				gta.IdEspecie = mov.IdEspecieGarantia;
				gta.IdMoneda = mov.IdMonedaInstrumento;
				gta.MontoMonedaSistema = mov.MontoCompra;
				gta.Glosa = mov.Glosa;
				gta.IdCliente = mov.IdCliente;
				gta.IdPortafolio = mov.IdPortafolio;
				gta.IdAgente = mov.IdAgente;
				gta.IdTipoInstrumento = mov.IdTipoInstrumento;
				gta.IdBolsa = datoGarantia != null ? datoGarantia.IdBolsa : mov.IdBolsa;
				gta.IdInstrumentoGarantizado = 0L;
				gta.IdConcepto = "REGGTA";
				gta.IdContraparteGarantia = datoGarantia != null ? datoGarantia.IdContraparteGarantia : mov.IdContraparteGarantia;
				gta.IdTipoOperacion = "INGGTA";
				gta.IdOperacion = mov.IdOperacion;

				garantias.add(gta);
			}
		}
		
		logger.info("INFO::GenerarMovimientoContraparteGarantia.obtenerGarantiasEspecie -- Se obtuvieron "+ garantias.size() + " Objetos de Garantia Especie" );
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarMovimientoContraparteGarantia.obtenerGarantiasEspecie", fin - inicio);
		return garantias;
	}

	private List<ObjGarantiaCuentaInversion> obtenerGarantiasCuentaInversion(Connection con, Long idEmpresa, String monedaSistema, String fechaSistema, List<MovimientoGarantia> p_MovimientosRetiro) throws Exception {
		logger.info("INICIO::GenerarMovimientoContraparteGarantia.obtenerGarantiasCuentaInversion");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(1);
        List<Long> idMovimientos = new ArrayList<Long>();
        idMovimientos.addAll(p_MovimientosRetiro.parallelStream().map(mov -> {return mov.IdMovimientoCarteraEnTransito;}).collect(Collectors.toList()));
        parametros.put("IdMovimientoCarteraEnTransito", idMovimientos);
        
        List<Garantia> datosGarantias = sql.query(con, SQL_SELECT_DATOS_RETIRO_GARANTIA, parametros, Garantia.class);
        Map<Long,Garantia> mapDatosGarantias = datosGarantias.parallelStream().collect(Collectors.toMap(gta->gta.IdMovimientoCarteraEnTransito, gta->gta));
		
		List<ObjGarantiaCuentaInversion> garantias = new ArrayList<ObjGarantiaCuentaInversion>();
		
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = idEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 1L;
        for(MovimientoGarantia mov : p_MovimientosRetiro) {
        	if(!monedaSistema.equals(mov.IdMonedaInstrumento)) {
	        	CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.IdTrabajo = 1L;
				calculoEntrada.IdInterno =mov.IdMovimientoCarteraEnTransito;
				calculoEntrada.Fecha = fechaSistema;
				calculoEntrada.IdMonedaOrigen = mov.IdMonedaInstrumento;
				calculoEntrada.IdMonedaDestino = monedaSistema;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntrada.MontoOrigen = mov.MontoCompraMonedaInstrumento;
				calculoEntrada.IdUso = "";
	        	calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
        	}
        }
        CalcularParidadSalida calculoMontoMonedaPago = calcularParidad.logica(con, calcularParidadEntrada);
		HashMap<Long,Object> hashMontoMonedaSistema = Utils.List2Hash(calculoMontoMonedaPago.p_out_CalculosSalida, "IdInterno");
		
		for(MovimientoGarantia mov : p_MovimientosRetiro) {
			if("CTA".equals(mov.IdOrigenGarantia)) {
				Garantia datoGarantia = mapDatosGarantias.get(mov.IdMovimientoCarteraEnTransito);
				CalculoSalida montoMonedaSistema = (CalculoSalida) hashMontoMonedaSistema.get(mov.IdMovimientoCarteraEnTransito);
				
				ObjGarantiaCuentaInversion gta = new ObjGarantiaCuentaInversion();
				gta.IdMoneda = mov.IdMonedaInstrumento;
				gta.IdCliente = mov.IdCliente;
				gta.IdPortafolio = mov.IdPortafolio;
				gta.IdAgente = mov.IdAgente;
				gta.IdTipoInstrumento = mov.IdTipoInstrumento;
				gta.IdBolsa = datoGarantia != null ? datoGarantia.IdBolsa : mov.IdBolsa;
				gta.IdInstrumentoGarantizado = 0L;
				gta.IdConcepto = "REGGTA";
				gta.IdContraparteGarantia = datoGarantia != null ? datoGarantia.IdContraparteGarantia : mov.IdContraparteGarantia;
				gta.IdTipoOperacion = "INGGTA";
				gta.IdOperacion = mov.IdOperacion;
				gta.MontoMonedaInstrumento = mov.MontoCompraMonedaInstrumento;
				gta.MontoMonedaSistema = montoMonedaSistema != null ? MonedaFormato.formatear(con, montoMonedaSistema.MontoDestino, MonedaFormato.MONTO, monedaSistema).valor : mov.MontoCompra.compareTo(BigDecimal.ZERO) > 0L ? mov.MontoCompra : mov.MontoCompraMonedaInstrumento;
				gta.IdGarantiaVigente = datoGarantia != null ? datoGarantia.IdGarantiaVigente : 0L;
				gta.CantidadVigente = datoGarantia != null ? datoGarantia.CantidadVigente : BigDecimal.ZERO;
				gta.INGMontoATasaGarantiaActualVigente = datoGarantia != null ? datoGarantia.INGMontoATasaGarantiaActualVigente : BigDecimal.ZERO;
				gta.INGMontoGarantiaActualMonedaInstrumentoVigente = datoGarantia != null ? datoGarantia.INGMontoGarantiaActualMonedaInstrumentoVigente : BigDecimal.ZERO;
				
				garantias.add(gta);
			}
		}
		
		logger.info("INFO::GenerarMovimientoContraparteGarantia.obtenerGarantiasCuentaInversion -- Se obtuvieron "+ garantias.size() + " Objetos de Garantia Cuenta Inversion" );
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarMovimientoContraparteGarantia.obtenerGarantiasCuentaInversion", fin - inicio);
		return garantias;
	}

	private List<ObjGarantiaCustodia> obtenerGarantiasCustodia(Connection con, Long idEmpresa, String monedaSistema, String fechaSistema, List<MovimientoGarantia> p_MovimientosRetiro) throws Exception {
		logger.info("INICIO::GenerarMovimientoContraparteGarantia.obtenerGarantiasCustodia");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(1);
        List<Long> idMovimientos = new ArrayList<Long>();
        idMovimientos.addAll(p_MovimientosRetiro.parallelStream().map(mov -> {return mov.IdMovimientoCarteraEnTransito;}).collect(Collectors.toList()));
        parametros.put("IdMovimientoCarteraEnTransito", idMovimientos);
        
        List<Garantia> datosGarantias = sql.query(con, SQL_SELECT_DATOS_RETIRO_GARANTIA, parametros, Garantia.class);
        Map<Long,Garantia> mapDatosGarantias = datosGarantias.parallelStream().collect(Collectors.toMap(gta->gta.IdMovimientoCarteraEnTransito, gta->gta));
		
        List<ObjGarantiaCustodia> garantias = new ArrayList<ObjGarantiaCustodia>();
		
        CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = idEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 1L;
        for(MovimientoGarantia mov : p_MovimientosRetiro) {
        	if(!monedaSistema.equals(mov.IdMonedaInstrumento)) {
	        	CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.IdTrabajo = 1L;
				calculoEntrada.IdInterno =mov.IdMovimientoCarteraEnTransito;
				calculoEntrada.Fecha = fechaSistema;
				calculoEntrada.IdMonedaOrigen = mov.IdMonedaInstrumento;
				calculoEntrada.IdMonedaDestino = monedaSistema;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntrada.MontoOrigen = mov.MontoCompraMonedaInstrumento;
				calculoEntrada.IdUso = "";
	        	calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
        	}
        }
        CalcularParidadSalida calculoMontoMonedaPago = calcularParidad.logica(con, calcularParidadEntrada);
		HashMap<Long,Object> hashMontoMonedaSistema = Utils.List2Hash(calculoMontoMonedaPago.p_out_CalculosSalida, "IdInterno");
		
		for(MovimientoGarantia mov : p_MovimientosRetiro) {
			if("CUS".equals(mov.IdOrigenGarantia)) {
				Garantia datoGarantia = mapDatosGarantias.get(mov.IdMovimientoCarteraEnTransito);
				CalculoSalida montoMonedaSistema = (CalculoSalida) hashMontoMonedaSistema.get(mov.IdMovimientoCarteraEnTransito);
				
				ObjGarantiaCustodia gta = new ObjGarantiaCustodia();
				gta.IdInstrumento = mov.IdInstrumento;
				gta.IdMoneda = mov.IdMonedaInstrumento;
				gta.IdMonedaEmisionInstrumento = "";
				gta.Cantidad = mov.Cantidad;
				gta.ValorRescate = !"IF".equals(mov.IdClasificacionTipoInstrumento) ? mov.Cantidad : mov.ValorRescate;
				gta.MontoMonedaInstrumento = mov.MontoCompraMonedaInstrumento;
				gta.MontoMonedaSistema = gta.MontoMonedaSistema = montoMonedaSistema != null ? MonedaFormato.formatear(con, montoMonedaSistema.MontoDestino, MonedaFormato.MONTO, monedaSistema).valor : mov.MontoCompra.compareTo(BigDecimal.ZERO) > 0L ? mov.MontoCompra : mov.MontoCompraMonedaInstrumento;// mov.MontoCompra.compareTo(BigDecimal.ZERO) == 0L ? mov.MontoCompraMonedaInstrumento: mov.MontoCompra;
				gta.IdClasificacionTipoInstrumento = mov.IdClasificacionTipoInstrumento;
				gta.IdTipoReajuste = mov.IdTipoReajuste;
				gta.NemoSVS = mov.NemoSVS;
				gta.FechaEmisionInstrumento = mov.FechaEmisionInstrumento;
				gta.FechaVencimientoInstrumento = mov.FechaVencimientoInstrumento;
				gta.IdEmisor = mov.IdEmisor;
				gta.PlazoEnDias = mov.PlazoEnDias;
				gta.EsFungible = mov.EsFungible;
				gta.IdCliente = mov.IdCliente;
				gta.IdPortafolio = mov.IdPortafolio;
				gta.IdAgente = datoGarantia != null ? datoGarantia.IdAgente : mov.IdAgente;
				gta.IdTipoInstrumento = mov.IdTipoInstrumento;
				gta.IdBolsa = datoGarantia != null ? datoGarantia.IdBolsa : mov.IdBolsa;
				gta.IdInstrumentoGarantizado = 0L;
				gta.IdConcepto = "REGGTA";
				gta.IdContraparteGarantia = datoGarantia != null ? datoGarantia.IdContraparteGarantia : mov.IdContraparteGarantia;
				gta.Precio = mov.PrecioCompra;
				gta.IdTipoOperacion = "INGGTA";
				gta.IdOperacion = mov.IdOperacion;
				gta.CorteMinimo = mov.CorteMinimo;
				gta.IdGarantiaVigente = datoGarantia != null ? datoGarantia.IdGarantiaVigente : 0L;
				gta.CantidadVigente = datoGarantia != null ? datoGarantia.CantidadVigente : BigDecimal.ZERO;
				garantias.add(gta);
			}
		}
		
		logger.info("INFO::GenerarMovimientoContraparteGarantia.obtenerGarantiasCustodia -- Se obtuvieron "+ garantias.size() + " Objetos de Garantia Custodia" );
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarMovimientoContraparteGarantia.obtenerGarantiasCustodia", fin - inicio);
		return garantias;
	}

	private void actualizarExcluyeMovimientoEnCartola(Connection con, List<Long> movsEntrada, List<Long> generados) throws Exception {
		logger.info("INICIO::GenerarMovimientoContraparteGarantia.actualizarExcluyeMovimientoEnCartola");
		long inicio = System.currentTimeMillis();
		
		List<Garantia> mvtosExcluyeCartola = new ArrayList<Garantia>();
		for(Long movEntrada : movsEntrada) {
			Garantia gta = new Garantia();
			gta.IdMovimientoCarteraEnTransito = movEntrada;
			mvtosExcluyeCartola.add(gta);
		}
		
		for(Long movGenerado : generados) {
			Garantia gta = new Garantia();
			gta.IdMovimientoCarteraEnTransito = movGenerado;
			mvtosExcluyeCartola.add(gta);
		}
		
		SQL sql = new SQL();
		
        sql.batch(con,SQL_UPDATE_EXCLUYE_DE_CARTOLA, mvtosExcluyeCartola, Garantia.class);
        
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarMovimientoContraparteGarantia.actualizarExcluyeMovimientoEnCartola", fin - inicio);
		
	}
	
	private List<MensajeSalida> validacionesMovimientosEnTransito(Connection con, Long idEmpresa, String fechaSistema, List<MovimientoGarantia> movimientos) throws Exception {
		logger.info("INICIO::GenerarMovimientoContraparteGarantia.validacionesMovimientosEnTransito");
		long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		String fechaLimiteContrapartida = obtenerFechaLimiteAnulacion(con, idEmpresa, fechaSistema);
		
		SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(1);
        
        parametros.put("idsMovimientoCarteraTransito", movimientos.stream().map(mvto -> mvto.IdMovimientoCarteraEnTransito).collect(Collectors.toList()) );
		
		RetiroCertificado retiroCertificado = sql.query(con, SQL_SELECT_MOVIMIENTOS_EN_TRANSITO_RETIRO_CERTIFICADOS, parametros, RetiroCertificado.class).stream().findFirst().orElse(new RetiroCertificado());
		
		if("S".equals(retiroCertificado.MovimientosRetiroEstadoCertificado)) {
			mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_HAY_RETIRO_GARANTIAS_NO_ANULADOS",new HashMap<String,String>(0),con, idEmpresa));
			
			return mensajes;
		}
		
		for(MovimientoGarantia mov : movimientos) {
			if(!"CRT".equals(mov.IdEstado)) {
				mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ESTADO_DISTINTO_CERTIFICADO",new HashMap<String,String>(0),con, idEmpresa));
			}
			if("SI".equals(mov.ExcluyeMovimientoEnCartola)) {
				mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ESTADO_DISTINTO_CERTIFICADO",new HashMap<String,String>(0),con, idEmpresa));
			}
			if(Date.valueOf(mov.FechaOperacion).before(Date.valueOf(fechaLimiteContrapartida))) {
				HashMap<String, String> paramsMensaje = new HashMap<>(2);
				paramsMensaje.put("FechaLimiteContrapartida", fechaLimiteContrapartida);
				paramsMensaje.put("FechaSistema", fechaSistema);
				mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FECHA_OPERACION_MOVIMIENTO_CONTRAPARTIDA",paramsMensaje,con, idEmpresa));
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarMovimientoContraparteGarantia.validacionesMovimientosEnTransito", fin - inicio);
		return mensajes;
	}


	private String obtenerFechaLimiteAnulacion(Connection con, Long idEmpresa, String fechaSistema) throws Exception {
		logger.info("INICIO::GenerarMovimientoContraparteGarantia.obtenerFechaLimiteAnulacion");
		long inicio = System.currentTimeMillis();
		
		InObtenerDiasHabilesAnteriores entradaObtenerDiasHabilesAnteriores = new InObtenerDiasHabilesAnteriores();
		entradaObtenerDiasHabilesAnteriores.idPais = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		entradaObtenerDiasHabilesAnteriores.diaHabil = "S";
		entradaObtenerDiasHabilesAnteriores.soloCaeDiaHabil = "S";
		FechasIn fecha = new FechasIn(fechaSistema, 2L);
		entradaObtenerDiasHabilesAnteriores.fechas.add(fecha);
		ObtenerDiasHabilesAnteriores obtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores();
		OutObtenerDiasHabilesAnteriores salidaObtenerDiaHabilAnterior = obtenerDiasHabilesAnteriores.logica(con, entradaObtenerDiasHabilesAnteriores);
		String fechaLimiteAnulacion = salidaObtenerDiaHabilAnterior.fechas.stream().filter(u -> u.numeroDiasInicial == 2L && fechaSistema.equals(u.fechaInicial)).map(u -> u.fechaCalculada).findFirst().orElse(fechaSistema);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarMovimientoContraparteGarantia.obtenerFechaLimiteAnulacion", fin - inicio);
		return fechaLimiteAnulacion;
	}

	private List<ObjGarantiasARetirar> obtenerRetiros(Connection con, List<MovimientoGarantia> movimientos) throws Exception {
		logger.info("INICIO::GenerarMovimientoContraparteGarantia.obtenerRetiros");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(1);
        List<Long> idMovimientos = new ArrayList<Long>();
        idMovimientos.addAll(movimientos.parallelStream().map(mov -> {return mov.IdMovimientoCarteraEnTransito;}).collect(Collectors.toList()));
        parametros.put("IdMovimientoCarteraEnTransito", idMovimientos);
		
        List<Garantia> garantias = sql.query(con, SQL_SELECT_IDGARANTIA, parametros, Garantia.class);
        
        List<ObjGarantiasARetirar> garantiasARetirar = new ArrayList<ObjGarantiasARetirar>();
        
        for (Garantia gta : garantias) {
        	List<MovimientoGarantia> movsCustodia = movimientos.parallelStream().filter(mov -> mov.IdMovimientoCarteraEnTransito.equals(gta.IdMovimientoCarteraEnTransito)).collect(Collectors.toList());
        	for(MovimientoGarantia mvto  : movsCustodia) {
        		ObjGarantiasARetirar garantiaARetirar = new ObjGarantiasARetirar();
        		garantiaARetirar.IdGarantia = gta.IdGarantia;
        		garantiaARetirar.Cantidad = mvto.Cantidad;
        		garantiaARetirar.ValorRescate = mvto.ValorRescate;
        		garantiaARetirar.MontoMonedaInstrumento = mvto.MontoCompraMonedaInstrumento;
        		garantiaARetirar.PrecioRetiro = mvto.PrecioCompra;
        		garantiaARetirar.TasaRetiro = mvto.TasaCompra;
        		garantiaARetirar.IdConcepto = "REGGTA";
        		garantiaARetirar.ConCertificadoAutomatico = "S";
        		garantiaARetirar.IdClasificacionTipoInstrumento = mvto.IdClasificacionTipoInstrumento;
        		garantiaARetirar.EsProcesoContraparteGarantia = "S";
        		garantiasARetirar.add(garantiaARetirar);
        	}
        }
        
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarMovimientoContraparteGarantia.obtenerRetiros", fin - inicio);
		return garantiasARetirar;
	}


	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = new Gson();
		EntradaGenerarMovimientoContraparteGarantia jin = gson.fromJson(in.toString(), EntradaGenerarMovimientoContraparteGarantia.class);
		SalidaGenerarMovimientoContraparteGarantia out = logica(con, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getPropertiesConnection("master02")) {
			try {
				System.out.println("COMIENZA");
				Gson gson = Utils.getGsonBuilder();
				String jsonIng = "{\r\n"
						+ "    \"p_IdEmpresa\": 1,\r\n"
						+ "    \"p_IdUsuario\": 97,\r\n"
						+ "    \"p_IdEmpleado\": 108,\r\n"
						+ "    \"p_IdModulo\": \"CUS\",\r\n"
						+ "    \"p_UsaReglasNegocio\": \"N\",\r\n"
						+ "    \"p_SimularOperacion\": \"N\",\r\n"
						+ "    \"p_IndicadorIngEgr\": \"INGRESO\",\r\n"
						+ "    \"p_MovimientosIngreso\": [\r\n"
						+ "        {\r\n"
						+ "            \"IdMovimientoCarteraEnTransito\": 801768,\r\n"
						+ "            \"IdTipoOperacion\": \"INGGTA\",\r\n"
						+ "            \"IdEstado\": \"CRT\",\r\n"
						+ "            \"IdClasificacionTipoInstrumento\": \"RV\",\r\n"
						+ "            \"CorteMinimo\": 0,\r\n"
						+ "            \"Cantidad\": 150,\r\n"
						+ "            \"IdCliente\": 11218,\r\n"
						+ "            \"IdConcepto\": \"IGRV81\",\r\n"
						+ "            \"IdInstrumento\": 502,\r\n"
						+ "            \"IdTipoInstrumento\": \"ACN\",\r\n"
						+ "            \"IdCondicionLiquidacion\": \"T0\",\r\n"
						+ "            \"IdPortafolio\": 12089,\r\n"
						+ "            \"PrecioCompra\": 6349.8,\r\n"
						+ "            \"TasaCompra\": 0,\r\n"
						+ "            \"MontoCompra\": 0,\r\n"
						+ "            \"MontoCompraMonedaInstrumento\": 789802.41,\r\n"
						+ "            \"ValorRescate\": 0,\r\n"
						+ "            \"IdUsuario\": 97,\r\n"
						+ "            \"IdBolsa\": \"BCS\",\r\n"
						+ "            \"IdMonedaInstrumento\": \"CLP\",\r\n"
						+ "            \"IdOrigenGarantia\": \"CUS\",\r\n"
						+ "            \"IdTipoComprobante\": \"CUS\",\r\n"
						+ "            \"EsGarantia\": \"S\",\r\n"
						+ "            \"IdAgente\": 15,\r\n"
						+ "            \"FechaOperacion\": \"2024-10-28\",\r\n"
						+ "            \"IdTipoReajuste\": \"\",\r\n"
						+ "            \"NemoSVS\": \"\",\r\n"
						+ "            \"FechaEmisionInstrumento\": \"1935-10-02\",\r\n"
						+ "            \"FechaVencimientoInstrumento\": \"0001-01-01\",\r\n"
						+ "            \"IdEmisor\": \"COPEC\",\r\n"
						+ "            \"PlazoEnDias\": 0,\r\n"
						+ "            \"EsFungible\": \"N\",\r\n"
						+ "            \"IdEspecieGarantia\": 0,\r\n"
						+ "            \"IdContraparteGarantia\": 17,\r\n"
						+ "            \"ExcluyeMovimientoEnCartola\": \"NO\"\r\n"
						+ "        }\r\n"
						+ "    ],\r\n"
						+ "    \"p_MovimientosRetiro\": []\r\n"
						+ "}";
				String asd = "{\r\n"
						+ "    \"p_IdEmpresa\": 1,\r\n"
						+ "    \"p_IdUsuario\": 97,\r\n"
						+ "    \"p_IdEmpleado\": 108,\r\n"
						+ "    \"p_IdModulo\": \"CUS\",\r\n"
						+ "    \"p_UsaReglasNegocio\": \"N\",\r\n"
						+ "    \"p_SimularOperacion\": \"N\",\r\n"
						+ "    \"p_IndicadorIngEgr\": \"INGRESO\",\r\n"
						+ "    \"p_MovimientosIngreso\": [\r\n"
						+ "        {\r\n"
						+ "            \"IdMovimientoCarteraEnTransito\": 805502,\r\n"
						+ "            \"IdTipoOperacion\": \"INGGTA\",\r\n"
						+ "            \"IdEstado\": \"CRT\",\r\n"
						+ "            \"IdClasificacionTipoInstrumento\": \"RV\",\r\n"
						+ "            \"CorteMinimo\": 0,\r\n"
						+ "            \"Cantidad\": 100,\r\n"
						+ "            \"IdCliente\": 2709,\r\n"
						+ "            \"IdConcepto\": \"IGRV81\",\r\n"
						+ "            \"IdInstrumento\": 502,\r\n"
						+ "            \"IdTipoInstrumento\": \"ACN\",\r\n"
						+ "            \"IdCondicionLiquidacion\": \"T0\",\r\n"
						+ "            \"IdPortafolio\": 541,\r\n"
						+ "            \"PrecioCompra\": 6200,\r\n"
						+ "            \"TasaCompra\": 0,\r\n"
						+ "            \"MontoCompra\": 527183.52,\r\n"
						+ "            \"MontoCompraMonedaInstrumento\": 527183.52,\r\n"
						+ "            \"ValorRescate\": 0,\r\n"
						+ "            \"IdUsuario\": 97,\r\n"
						+ "            \"IdBolsa\": \"BCS\",\r\n"
						+ "            \"IdMonedaInstrumento\": \"CLP\",\r\n"
						+ "            \"IdOrigenGarantia\": \"CUS\",\r\n"
						+ "            \"IdTipoComprobante\": \"CUS\",\r\n"
						+ "            \"EsGarantia\": \"S\",\r\n"
						+ "            \"IdAgente\": 31,\r\n"
						+ "            \"FechaOperacion\": \"2024-11-19\",\r\n"
						+ "            \"IdTipoReajuste\": \"\",\r\n"
						+ "            \"NemoSVS\": \"\",\r\n"
						+ "            \"FechaEmisionInstrumento\": \"1935-10-02\",\r\n"
						+ "            \"FechaVencimientoInstrumento\": \"0001-01-01\",\r\n"
						+ "            \"IdEmisor\": \"COPEC\",\r\n"
						+ "            \"PlazoEnDias\": 0,\r\n"
						+ "            \"EsFungible\": \"N\",\r\n"
						+ "            \"IdEspecieGarantia\": 0,\r\n"
						+ "            \"IdContraparteGarantia\": 17,\r\n"
						+ "            \"ExcluyeMovimientoEnCartola\": \"NO\"\r\n"
						+ "        }\r\n"
						+ "    ],\r\n"
						+ "    \"p_MovimientosRetiro\": []\r\n"
						+ "}";
				EntradaGenerarMovimientoContraparteGarantia Objin = gson.fromJson(asd, EntradaGenerarMovimientoContraparteGarantia.class);

				GenerarMovimientoContraparteGarantia kk = new GenerarMovimientoContraparteGarantia();
				SalidaGenerarMovimientoContraparteGarantia asdf = kk.logica(con, Objin);
				System.out.println(asdf);
//				con.commit();
				con.rollback();
				System.out.println("END");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
