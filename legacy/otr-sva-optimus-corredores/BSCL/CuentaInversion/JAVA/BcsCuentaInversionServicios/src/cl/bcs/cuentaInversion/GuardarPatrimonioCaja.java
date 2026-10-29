package cl.bcs.cuentaInversion;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.GuardarPatrimonioCajaCalculoComision.CuentaInversionConsultar;
import cl.bcs.cuentaInversion.GuardarPatrimonioCajaCalculoComision.EntradaGuardarPatrimonioCajaCalculoComision;
import cl.bcs.cuentaInversion.GuardarPatrimonioCajaCalculoComision.SalidaGuardarPatrimonioCajaCalculoComision;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GuardarPatrimonioCaja implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(GuardarPatrimonioCaja.class);
	private final String SQL_SELECT_MONEDAS = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCaja_SelectMonedas.sql");
	private final String SQL_SELECT_CUENTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCaja_SelectCuentaInversion.sql");
	private final String SQL_SELECT_CONFIGURACION_PATRIMONIO_CAJA = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCaja_SelectConfiguracionPatrimonioCaja.sql");
	private final String SQL_INSERT_PATRIMONIO_CAJA = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCaja_InsertPatrimonioCaja.sql");
	private final String SQL_DELETE_PATRIMONIO_CAJA = loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarPatrimonioCaja_DeletePatrimonioCaja.sql");
	
	public static class EntradaGuardarPatrimonioCaja{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaGuardarPatrimonioCaja{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaGuardarPatrimonioCaja() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PatrimonioCaja{
		public Long IdInterno;
		public Long IdCliente;
		public Long IdPortafolio;
		public String FechaPatrimonio;
		public String TipoFechaConsulta; //LIQ,ADQ
		public BigDecimal MontoPatrimonio;
		public BigDecimal SaldoDisponible;
		public BigDecimal SaldoEnGarantia;
		public BigDecimal SaldoRetiro;
		public BigDecimal SaldoAPlazoSimultanea;
		public BigDecimal SaldoAPlazoVentaCorta;
		public BigDecimal SaldoAPlazoCompromiso;
		public String IdMoneda;
		public String IdMonedaSistema;
		public String IdEstado;
		public BigDecimal MontoPatrimonioAfectoComision;
		public BigDecimal MontoComision;
		public BigDecimal PorcentajeCobroComision;
		public String IdEstadoComision;
		public Long IdCuentaInversion;
		
		public PatrimonioCaja() {
			this.MontoPatrimonio = BigDecimal.ZERO;
			this.MontoPatrimonioAfectoComision = BigDecimal.ZERO;
			this.MontoComision = BigDecimal.ZERO;
			this.PorcentajeCobroComision = BigDecimal.ZERO;
			this.IdEstadoComision = "INA";
			this.IdCuentaInversion = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Item{
		public String CodigoItem;
		public String CodigoMontoValorizado;
		public String FuncionOperador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Moneda{
		public String IdMoneda;
		
		public Moneda() {
			this.IdMoneda = "";
		}
		public Moneda(String idMoneda) {
			this.IdMoneda = idMoneda;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaGuardarPatrimonioCaja jin = gson.fromJson(in.toString(), EntradaGuardarPatrimonioCaja.class);
		SalidaGuardarPatrimonioCaja out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGuardarPatrimonioCaja logica(Connection con, EntradaGuardarPatrimonioCaja entrada) throws Exception {
		logger.info("INICIO::GuardarPatrimonioCaja");
		SalidaGuardarPatrimonioCaja out = new SalidaGuardarPatrimonioCaja();
		SQL sql=new SQL();
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String parametroCobroComisionPatrimonio = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "CTA.CobroComisionPatrimonio");
		
		/*
		 * Inicio Eliminar Patrimonio
		*/
		{
			List<PatrimonioCaja> patrimoniosEliminar = new ArrayList<PatrimonioCaja>();
			PatrimonioCaja patrimonioEliminar = new PatrimonioCaja();
			patrimonioEliminar.FechaPatrimonio = entrada.p_Fecha;
			patrimoniosEliminar.add(patrimonioEliminar);
			sql.batch(con, SQL_DELETE_PATRIMONIO_CAJA, patrimoniosEliminar, PatrimonioCaja.class);
		}
		/*
		 * Fin Eliminar Patrimonio
		*/
		AtomicLong index = new AtomicLong(1);
		List<PatrimonioCaja> patrimoniosMonedaSistema = new ArrayList<PatrimonioCaja>();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Item> items = sql.query(con, SQL_SELECT_CONFIGURACION_PATRIMONIO_CAJA, params, Item.class);
		
		params.clear();
		params.put("IdMonedaSistema", idMonedaSistema);
		List<Moneda> monedas = sql.query(con, SQL_SELECT_MONEDAS, params, Moneda.class);
		
		List<CuentaInversionConsultar> cuentasInversionConsultar = new ArrayList<CuentaInversionConsultar>();
		
		/*
		 * Moneda Sistema
		 */
		{
			
			patrimoniosMonedaSistema = llamarCuentaInversion(idMonedaSistema,idMonedaSistema,sql,con, entrada,items,index);
			cuentasInversionConsultar.addAll(patrimoniosMonedaSistema.stream().filter(pat -> ("ADQ").equals(pat.TipoFechaConsulta)).map(pat -> new CuentaInversionConsultar(pat.IdCuentaInversion, pat.IdPortafolio, pat.IdCliente,idMonedaSistema,entrada.p_Fecha)).collect(Collectors.toList()));
		}
		/*
		 * Moneda Sistema
		 */
		{
			for(Moneda moneda : monedas) {
				
				List<PatrimonioCaja> patrimoniosMonedaExtranjera2 = llamarCuentaInversion(moneda.IdMoneda,idMonedaSistema,sql,con, entrada,items,index);
				CalcularParidad calculoParidad = new CalcularParidad();
				CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
				calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				calcularParidadEntrada.p_IdTrabajo = 0L;
				
				List<PatrimonioCaja> patrimoniosMonedaExtranjera = patrimoniosMonedaExtranjera2.parallelStream().filter(patrimonio-> !"INA".equals(patrimonio.IdEstado)).collect(Collectors.toList());
				patrimoniosMonedaExtranjera2 = null;
				calcularParidadEntrada.p_CalculosEntrada = patrimoniosMonedaExtranjera.parallelStream().map(patrimonio->{
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = patrimonio.FechaPatrimonio;
					calculoEntrada.IdInterno = patrimonio.IdInterno;
					calculoEntrada.IdMonedaOrigen = patrimonio.IdMoneda;
					calculoEntrada.IdMonedaDestino = patrimonio.IdMonedaSistema;
					calculoEntrada.IdTrabajo = 0L;
					calculoEntrada.IdUso = "";
					calculoEntrada.MontoOrigen = patrimonio.MontoPatrimonio;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					return calculoEntrada;
				}).collect(Collectors.toList());
				
				logger.debug("COMIENZA:: LLAMADA PARIDAD");
				CalcularParidadSalida calculoSalida = calculoParidad.logica(con,calcularParidadEntrada);
				HashMap<Long,Object> hCalculoSalida = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
				logger.debug("TERMINA:: LLAMADA PARIDAD");
				
				logger.debug("COMIENZA:: PROCESAMIENTO PARIDAD");
				patrimoniosMonedaSistema = patrimoniosMonedaSistema.parallelStream().map(patrimonio -> {
					List<PatrimonioCaja> patrimoniosMonedaExtranjeraProceso = patrimoniosMonedaExtranjera.parallelStream().filter(patrimonioMonedaExtranjera -> patrimonio.IdCliente.equals(patrimonioMonedaExtranjera.IdCliente) 
																																								&& patrimonio.IdPortafolio.equals(patrimonioMonedaExtranjera.IdPortafolio)
																																								&& patrimonio.TipoFechaConsulta.equals(patrimonioMonedaExtranjera.TipoFechaConsulta)).collect(Collectors.toList());
					
					BigDecimal monto = patrimoniosMonedaExtranjeraProceso.parallelStream().map(patrimonioExtranjero-> {
						CalculoSalida paridad = (CalculoSalida) hCalculoSalida.get(patrimonioExtranjero.IdInterno);
						BigDecimal montoParidad = calculoSalida != null && paridad.MontoDestino != null ? paridad.MontoDestino : BigDecimal.ZERO;
						return montoParidad;
					}).collect(Collectors.toList()).stream().reduce(BigDecimal.ZERO,(acum,montoParidad)-> montoParidad.add(acum,MathContext.DECIMAL128));
					
					patrimonio.MontoPatrimonio = patrimonio.MontoPatrimonio.add(monto, MathContext.DECIMAL128);
					patrimonio.IdEstado = patrimonio.MontoPatrimonio.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "ACT";
					
					return patrimonio;
				}).collect(Collectors.toList());
				logger.debug("TERMINA:: PROCESAMIENTO PARIDAD");
			}
		}
		
		monedas.add(new Moneda(idMonedaSistema));
		if("S".equals(parametroCobroComisionPatrimonio)) {
			patrimoniosMonedaSistema = calculoPatrimonioComision(con, patrimoniosMonedaSistema, entrada, cuentasInversionConsultar);			
		}
		
		sql.batch(con, SQL_INSERT_PATRIMONIO_CAJA, patrimoniosMonedaSistema, PatrimonioCaja.class);
		
		/*
		 * Fin Por Fecha Adquisicion
		*/
		
		logger.info("FIN::GuardarPatrimonioCaja");
		return out;
	}
	
	private List<PatrimonioCaja> llamarCuentaInversion(String moneda,String idMonedaSistema,SQL sql,Connection con, EntradaGuardarPatrimonioCaja entrada, List<Item> items, AtomicLong index) throws Exception {
		List<PatrimonioCaja> patrimoniosCaja = new ArrayList<PatrimonioCaja>();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.clear();
		params.put("IdMoneda", moneda);
		List<SaldoCuentaInversion.InCuentaInversionConsultar> cuentasInversion = sql.query(con, SQL_SELECT_CUENTA_INVERSION, params, SaldoCuentaInversion.InCuentaInversionConsultar.class);
		logger.debug("COMIENZA:: LLAMADA SALDO CUENTA INVERSION " + moneda);
		
		EntradaConsultarSaldoCuentaInversion entradaCuentaInversion = new EntradaConsultarSaldoCuentaInversion();
		entradaCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCuentaInversion.p_IdModulo = entrada.p_IdModulo;
		entradaCuentaInversion.p_IdUsuario = entrada.p_IdUsuario;
		entradaCuentaInversion.p_UsaReglasNegocio = "S";
		entradaCuentaInversion.p_FechaDesde = entrada.p_Fecha;
		entradaCuentaInversion.p_FechaHasta = entrada.p_Fecha;
		entradaCuentaInversion.p_IncluyeOtrosSaldos = "S";
		entradaCuentaInversion.p_IdProductoAgrupador = null;
		
		for (int i = 0; i < cuentasInversion.size() ; i++ ){
			entradaCuentaInversion.p_CuentaInversionAConsultar.add(cuentasInversion.get(i));
			if ((i!= 0 && i%1500000 == 0) || i == cuentasInversion.size() - 1) {
				logger.debug("Cuenta Inversion hasta  " + i);
				ConsultarSaldoCuentaInversion consultarSaldoCuentaInversion = new ConsultarSaldoCuentaInversion();
				SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuentaInversion = consultarSaldoCuentaInversion.logica(con, entradaCuentaInversion);
				logger.debug("TERMINA:: LLAMADA SALDO CUENTA INVERSION " + moneda);
				
				logger.debug("COMIENZA:: PROCESAMIENTO CUENTA INVERSION " + moneda);
				/*
				 * Inicio Por Fecha Liquidacion
				*/
				{
					List<PatrimonioCaja> patrimonioCajaLiquidacion = salidaConsultarSaldoCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA.parallelStream().map(saldo -> {
						try {
							PatrimonioCaja patrimonioCaja = new PatrimonioCaja();
							patrimonioCaja.IdInterno = index.getAndIncrement();
							patrimonioCaja.IdCliente = saldo.IdCliente;
							patrimonioCaja.IdPortafolio = saldo.IdPortafolio;
							patrimonioCaja.FechaPatrimonio = entrada.p_Fecha;
							patrimonioCaja.TipoFechaConsulta = "LIQ";
							patrimonioCaja.IdMoneda = saldo.IdMoneda == null ? idMonedaSistema : saldo.IdMoneda;
							patrimonioCaja.IdMonedaSistema = idMonedaSistema;
							patrimonioCaja.SaldoDisponible = MonedaFormato.formatear(con,saldo.SaldoDisponible,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.SaldoEnGarantia = MonedaFormato.formatear(con,saldo.SaldoEnGarantia,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.SaldoRetiro = MonedaFormato.formatear(con,saldo.SaldoRetiro,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.SaldoAPlazoSimultanea = MonedaFormato.formatear(con,saldo.SaldoAPlazoSimultanea,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.SaldoAPlazoVentaCorta = MonedaFormato.formatear(con,saldo.SaldoAPlazoVentaCorta,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.SaldoAPlazoCompromiso = MonedaFormato.formatear(con,saldo.SaldoAPlazoCompromiso,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							for(Item item : items) {
								try{
									Field campoMonto = saldo.getClass().getDeclaredField(item.CodigoItem);
									if(campoMonto != null){
										BigDecimal monto = (BigDecimal) campoMonto.get(saldo);
										if(monto != null) {
											patrimonioCaja.MontoPatrimonio = patrimonioCaja.MontoPatrimonio.add("+".equals(item.FuncionOperador) ? monto : monto.negate(MathContext.DECIMAL128));
										}
									}
								}catch(Exception e){
									logger.warn("No existe item:" + item, e);
								}
							}
							patrimonioCaja.MontoPatrimonio = MonedaFormato.formatear(con,patrimonioCaja.MontoPatrimonio,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.IdEstado = patrimonioCaja.MontoPatrimonio.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "ACT";
							patrimonioCaja.IdCuentaInversion = saldo.IdCuentaInversion;
							return patrimonioCaja;
						}catch(Exception e) {
							return null;
						}
						
					}).collect(Collectors.toList());
					patrimoniosCaja.addAll(patrimonioCajaLiquidacion);
				}
				/*
				 * Fin Por Fecha Liquidacion
				*/
				
				/*
				 * Inicio Por Fecha Adquisicion
				*/
				{
					List<PatrimonioCaja> patrimonioCajaMovimiento = salidaConsultarSaldoCuentaInversion.P_OUT_SALDOPORFECHAMOVIMIENTO.parallelStream().map(saldo -> {
						try {
							PatrimonioCaja patrimonioCaja = new PatrimonioCaja();
							patrimonioCaja.IdInterno = index.getAndIncrement();
							patrimonioCaja.IdCliente = saldo.IdCliente;
							patrimonioCaja.IdPortafolio = saldo.IdPortafolio;
							patrimonioCaja.FechaPatrimonio = entrada.p_Fecha;
							patrimonioCaja.TipoFechaConsulta = "ADQ";
							patrimonioCaja.IdMoneda = saldo.IdMoneda == null ? idMonedaSistema : saldo.IdMoneda;
							patrimonioCaja.IdMonedaSistema = idMonedaSistema;
							patrimonioCaja.SaldoDisponible = MonedaFormato.formatear(con,saldo.SaldoDisponible,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.SaldoEnGarantia = MonedaFormato.formatear(con,saldo.SaldoEnGarantia,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.SaldoRetiro = MonedaFormato.formatear(con,saldo.SaldoRetiro,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.SaldoAPlazoSimultanea = MonedaFormato.formatear(con,saldo.SaldoAPlazoSimultanea,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.SaldoAPlazoVentaCorta = MonedaFormato.formatear(con,saldo.SaldoAPlazoVentaCorta,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.SaldoAPlazoCompromiso = MonedaFormato.formatear(con,saldo.SaldoAPlazoCompromiso,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							for(Item item : items) {
								try{
									Field campoMonto = saldo.getClass().getDeclaredField(item.CodigoItem);
									if(campoMonto != null){
										BigDecimal monto = (BigDecimal) campoMonto.get(saldo);
										if(monto != null) {
											patrimonioCaja.MontoPatrimonio = patrimonioCaja.MontoPatrimonio.add("+".equals(item.FuncionOperador) ? monto : monto.negate(MathContext.DECIMAL128));
										}
									}
								}catch(Exception e){
									logger.warn("No existe item:" + item, e);
								}
							}
							patrimonioCaja.MontoPatrimonio = MonedaFormato.formatear(con,patrimonioCaja.MontoPatrimonio,MonedaFormato.MONTO,saldo.IdMoneda).valor;
							patrimonioCaja.IdEstado = patrimonioCaja.MontoPatrimonio.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "ACT";
							patrimonioCaja.IdCuentaInversion = saldo.IdCuentaInversion;
							return patrimonioCaja;
						}catch(Exception e) {
							return null;
						}
						
					}).collect(Collectors.toList());
					/*
					 * Fin Por Fecha Adquisicion
					*/
					patrimoniosCaja.addAll(patrimonioCajaMovimiento);
				}
				logger.debug("TERMINA:: PROCESAMIENTO CUENTA INVERSION " + moneda);
		        entradaCuentaInversion.p_CuentaInversionAConsultar.clear();
			}
		}
		
		return patrimoniosCaja;
	}
	
	public List<PatrimonioCaja> calculoPatrimonioComision (Connection con,  List<PatrimonioCaja> patrimonios, EntradaGuardarPatrimonioCaja entrada, List<CuentaInversionConsultar> cuentasInversion) throws Exception{
		logger.debug("INICIO :: GuardarPatrimonioCaja.calculoPatrimonioComision");
        
		EntradaGuardarPatrimonioCajaCalculoComision entradaGuardarPatrimonioCajaCalculoComision = new EntradaGuardarPatrimonioCajaCalculoComision();
		entradaGuardarPatrimonioCajaCalculoComision.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaGuardarPatrimonioCajaCalculoComision.p_IdModulo = entrada.p_IdModulo;
		entradaGuardarPatrimonioCajaCalculoComision.p_IdUsuario = entrada.p_IdUsuario;
		entradaGuardarPatrimonioCajaCalculoComision.p_UsaReglasNegocio = "S";
		entradaGuardarPatrimonioCajaCalculoComision.p_FechaConsulta =  entrada.p_Fecha;
		entradaGuardarPatrimonioCajaCalculoComision.p_CuentaInversionAConsultar = cuentasInversion;
		GuardarPatrimonioCajaCalculoComision guardarPatrimonioCajaCalculoComision = new GuardarPatrimonioCajaCalculoComision();
		SalidaGuardarPatrimonioCajaCalculoComision salidaCalculoComision = guardarPatrimonioCajaCalculoComision.logica(con, entradaGuardarPatrimonioCajaCalculoComision);
		
		HashMap<Long, Object> hashPatrimonioCajaCalculoComision = Utils.List2Hash(salidaCalculoComision.P_TBL_PATRIMONIO, "IdPortafolio");
		
		for(PatrimonioCaja patrimonio : patrimonios) {
			if(("ADQ").equals(patrimonio.TipoFechaConsulta)) {
				PatrimonioCaja patrimonioSalida = (PatrimonioCaja) hashPatrimonioCajaCalculoComision.getOrDefault(patrimonio.IdPortafolio, new PatrimonioCaja());
				patrimonio.MontoPatrimonioAfectoComision = patrimonioSalida.MontoPatrimonioAfectoComision;
				patrimonio.MontoComision = patrimonioSalida.MontoComision;
				patrimonio.IdEstadoComision = patrimonioSalida.IdEstadoComision;
				patrimonio.PorcentajeCobroComision = patrimonioSalida.PorcentajeCobroComision;
			}
		}
		
		logger.debug("FIN :: GuardarPatrimonioCaja.calculoPatrimonioComision");
        return patrimonios;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		EntradaGuardarPatrimonioCaja Objin = new EntradaGuardarPatrimonioCaja();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "CUS";
		Objin.p_IdUsuario = 35L;
		Objin.p_Fecha = "2025-04-04";
		GuardarPatrimonioCaja  demo=new GuardarPatrimonioCaja();				
		try {
			SalidaGuardarPatrimonioCaja ObjOut=demo.logica(con, Objin);
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
