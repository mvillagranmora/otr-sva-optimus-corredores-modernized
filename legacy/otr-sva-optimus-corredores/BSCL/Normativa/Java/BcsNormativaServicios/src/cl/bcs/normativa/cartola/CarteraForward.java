package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CarteraForward implements BcsCartola{
	
	private static final Logger logger = LogManager.getLogger(CarteraForward.class);
	
	private final String SQL_SELECT_CARTERA_ACTUAL = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraForward_SelectCarteraActual.sql");
	private final String SQL_SELECT_CARTERA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraForward_SelectCarteraHistorico.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
	public static class SalidaCarteraForward{
		public Long IdCliente;
		public Long IdPortafolio;
		public String tipo;
		public Integer plazo;
		public String emision;
		public String vencimiento;
		public Integer saldoPlazo;
		public BigDecimal precioCierre;
		public String Fecha;
		public Integer folioOper;
		public String Operacion;
		public BigDecimal monto;
		public String moneda;
		public BigDecimal montoPeso;
		public BigDecimal valorRazonable;
		public BigDecimal valorRazonableActivo;
		public BigDecimal valorRazonablePasivo;
		public String liquidacion;
		public String OperacionMoneda;
		public String WarningFechavalorRazonable;
		public String FechavalorRazonable;
		public BigDecimal TipoCambioMercado;
		public String monedaRecibe;
		public String monedaEntrega;
		public String IdTipoMoneda;
		public String Nemotecnico;
		public Long IdInstrumento; //GCMBIMECAR-1 Mejoramiento de Cartolas
		public BigDecimal MontoMonedaSecundaria; //GCMBIMECAR-1 Mejoramiento de Cartolas
		public BigDecimal TipoCambioCierre; //GCMBIMECAR-1 Mejoramiento de Cartolas
		public BigDecimal TipoCambioCosto; //GCMBIMECAR-1 Mejoramiento de Cartolas
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		logger.info("INICIO::CarteraForward");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
	
		HashMap<String,Object> params = new HashMap<String,Object>();

		List<Long> portafolios = new ArrayList<Long>();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<Portafolio> portObj = new ArrayList<>();
		for(Dato dato : entrada.p_Datos){
			portafolios.add(dato.IdPortafolio);
			Portafolio por = new Portafolio();
			por.IdTrabajo = idTrabajo;
			por.IdPortafolio = dato.IdPortafolio;
			portObj.add(por);
		}
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);

		
		params.put("FechaHasta", entrada.p_FechaHasta);
		
		List<SalidaCarteraForward> salidas = new ArrayList<SalidaCarteraForward>();
		
		if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
			salidas.addAll(sql.query(con, SQL_SELECT_CARTERA_ACTUAL, params, SalidaCarteraForward.class));
		}else {
			
			salidas.addAll(sql.query(con, SQL_SELECT_CARTERA_HISTORICO, params, SalidaCarteraForward.class));
		}
		
		
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));

		
		List<Long> idInternos = new ArrayList<Long>();
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdTrabajo = 1L;
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		for(SalidaCarteraForward salida : salidas) {
			if(!idInternos.contains(salida.folioOper.longValue())) {
				CalculoEntrada calculoEntradaResultado = new CalculoEntrada();
				calculoEntradaResultado.IdTrabajo = 0L;
				calculoEntradaResultado.IdInterno = salida.folioOper.longValue();
				calculoEntradaResultado.Fecha = Fecha.toStringDate(entrada.p_FechaHasta);
				if("ARBITR".equals(salida.Nemotecnico) && "DEB".equals(salida.IdTipoMoneda)) {
					calculoEntradaResultado.IdMonedaOrigen = salida.monedaEntrega;
					calculoEntradaResultado.IdMonedaDestino = salida.monedaRecibe;
				}else {
					calculoEntradaResultado.IdMonedaOrigen = salida.monedaRecibe;
					calculoEntradaResultado.IdMonedaDestino = salida.monedaEntrega;
				}
				calculoEntradaResultado.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntradaResultado.MontoOrigen = BigDecimal.ONE;
				calculoEntradaResultado.IdUso = "";
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaResultado);
				idInternos.add(salida.folioOper.longValue());
			}
			salida.valorRazonable = salida.valorRazonableActivo.subtract(salida.valorRazonablePasivo, MathContext.DECIMAL128).negate();
		}
		
		if(calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
			CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
			HashMap<Long,Object> hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
			calcularParidadEntrada.p_CalculosEntrada.clear();
			idInternos.clear();
			for(SalidaCarteraForward salida : salidas) {
				CalculoSalida calculoSalidaResultado = (CalculoSalida) hCalcularParidadSalida.get(salida.folioOper.longValue());
				if(calculoSalidaResultado != null && "OK".equals(calculoSalidaResultado.Resultado)) {
					salida.TipoCambioMercado = calculoSalidaResultado.MontoDestino;
					if("ARBITR".equals(salida.Nemotecnico) && "DEB".equals(salida.IdTipoMoneda)) {
						salida.montoPeso = "C".equals(salida.tipo) ? salida.monto.divide(salida.TipoCambioMercado.subtract(salida.precioCierre, MathContext.DECIMAL128), MathContext.DECIMAL128) : salida.monto.divide(salida.TipoCambioMercado.subtract(salida.precioCierre, MathContext.DECIMAL128), MathContext.DECIMAL128).negate();
					}else {
						salida.montoPeso = "C".equals(salida.tipo) ? salida.TipoCambioMercado.subtract(salida.precioCierre, MathContext.DECIMAL128).multiply(salida.monto, MathContext.DECIMAL128) : salida.TipoCambioMercado.subtract(salida.precioCierre, MathContext.DECIMAL128).multiply(salida.monto, MathContext.DECIMAL128).negate();
					}
					if(!idInternos.contains(salida.folioOper.longValue())) {
						CalculoEntrada calculoEntradaResultado = new CalculoEntrada();
						calculoEntradaResultado.IdTrabajo = 0L;
						calculoEntradaResultado.IdInterno = salida.folioOper.longValue();
						calculoEntradaResultado.Fecha = Fecha.toStringDate(entrada.p_FechaHasta);;
						calculoEntradaResultado.IdMonedaOrigen = salida.monedaEntrega;
						calculoEntradaResultado.IdMonedaDestino = monedaSistema;
						calculoEntradaResultado.ValorCambioLocalModificado = BigDecimal.ZERO;
						calculoEntradaResultado.MontoOrigen = salida.montoPeso;
						calculoEntradaResultado.IdUso = "";
						calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaResultado);
						idInternos.add(salida.folioOper.longValue());
					}
					
				}
				
			}
		}
		
		//PASAMOS A CLP EL RESULTADO NETO
		if(calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
			CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
			HashMap<Long,Object> hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
			for(SalidaCarteraForward salida : salidas) {
				CalculoSalida calculoSalidaResultado = (CalculoSalida) hCalcularParidadSalida.get(salida.folioOper.longValue());
				if(calculoSalidaResultado != null && "OK".equals(calculoSalidaResultado.Resultado)) {
					salida.montoPeso = calculoSalidaResultado.MontoDestino;
				}
			}
		}
		
		List<HashMap<String,Object>> resultadoCarteraForward = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("carteraForward", resultadoCarteraForward);
		
		logger.info("FIN::CarteraForward");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-10-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-10-22");
		Dato dato = new Dato();
		dato.IdCliente = 524891L;
		dato.IdPortafolio = 1451531L;
		Objin.p_Datos.add(dato);
		CarteraForward demo= new CarteraForward();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
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
