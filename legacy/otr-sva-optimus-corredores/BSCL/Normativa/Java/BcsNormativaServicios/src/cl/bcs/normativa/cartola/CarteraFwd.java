package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CarteraFwd implements BcsCartola{
	
	private static final Logger logger = LogManager.getLogger(CarteraFwd.class);
	
	private final String SQL_SELECT_CARTERA_ACTUAL = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraFwd_SelectCarteraActual.sql");
	private final String SQL_SELECT_CARTERA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraFwd_SelectCarteraHistorico.sql");
	
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
	public static class SalidaCarteraFwd{
		public Long IdCliente;
		public Long IdPortafolio;
		public String tipo;
		public String tipoOper;
		public BigDecimal SEC_OPER;
		public String GLOSA;
		public String TIPO_LIQUID;
		public String FEC_OPER;
		public String FEC_VCTO;
		public String moneda;
		public BigDecimal PRECIO_PESOS;
		public String monedaRecibe;
		public BigDecimal montoRecibe;
		public BigDecimal montoEquivRecibe;
		public String monedaEntrega;
		public BigDecimal montoEntrega;
		public BigDecimal montoEquivEntrega;
		public BigDecimal resultado;
		public BigDecimal rentabilidad;
		public BigDecimal TipoCambioMercado;
		public BigDecimal TipoCambioMercadoSecundaria;
		public String IdTipoMoneda;
		public String Nemotecnico;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdTrabajo;
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		logger.debug("INICIO::CarteraFwd");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		Long idTrabajo = Double.valueOf(Math.random()*10000000000L).longValue();
		try {
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
						
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdTrabajo", idTrabajo);
			List<SalidaCarteraFwd> salidas = new ArrayList<SalidaCarteraFwd>();
			
			List<Portafolio> portafoliosBusqueda = entrada.p_Datos.stream()
					.map(dato->{
						Portafolio portafolioBusqueda = new Portafolio();
						portafolioBusqueda.IdPortafolio = dato.IdPortafolio;
						portafolioBusqueda.IdTrabajo = idTrabajo;
						return portafolioBusqueda;
					})
					.collect(Collectors.toList());
			
			sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portafoliosBusqueda, Portafolio.class);
			
			if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
				salidas.addAll(sql.query(con, SQL_SELECT_CARTERA_ACTUAL, params, SalidaCarteraFwd.class));
			}else {
				params.put("FechaHasta", entrada.p_FechaHasta);
				salidas.addAll(sql.query(con, SQL_SELECT_CARTERA_HISTORICO, params, SalidaCarteraFwd.class));
			}
			
			List<Long> idInternos = new ArrayList<Long>();
			CalcularParidad calcularParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdTrabajo = 1L;
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			for(SalidaCarteraFwd salida : salidas) {
				BigDecimal saldoActual = salida.montoRecibe.multiply(salida.PRECIO_PESOS, MathContext.DECIMAL128);
				if(!idInternos.contains(salida.SEC_OPER.longValue() * -1)) {
					CalculoEntrada calculoEntradaResultado = new CalculoEntrada();
					calculoEntradaResultado.IdTrabajo = 0L;
					calculoEntradaResultado.IdInterno = salida.SEC_OPER.longValue() * -1;
					calculoEntradaResultado.Fecha = Fecha.toStringDate(entrada.p_FechaHasta);;
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
					idInternos.add(salida.SEC_OPER.longValue() * -1);
				}
				
				salida.rentabilidad = (saldoActual.subtract(salida.montoEntrega, MathContext.DECIMAL128).divide(salida.montoEntrega, MathContext.DECIMAL128)).multiply(new BigDecimal("100"), MathContext.DECIMAL128);
				if(salida.monedaEntrega.equals(monedaSistema)) {
					salida.montoEquivRecibe = salida.montoEntrega;
					salida.montoEquivEntrega = salida.montoEntrega;
				}else if (!idInternos.contains(salida.SEC_OPER.longValue())){
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.IdTrabajo = 0L;
					calculoEntrada.IdInterno = salida.SEC_OPER.longValue();
					calculoEntrada.Fecha = Fecha.toStringDate(entrada.p_FechaHasta);;
					calculoEntrada.IdMonedaOrigen = salida.monedaEntrega;
					calculoEntrada.IdMonedaDestino = monedaSistema;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					calculoEntrada.MontoOrigen = salida.montoEntrega;
					calculoEntrada.IdUso = "";
					calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
					idInternos.add(salida.SEC_OPER.longValue());
				}
			}
			
			if(calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
				CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
				HashMap<Long,Object> hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
				calcularParidadEntrada.p_CalculosEntrada.clear();
				idInternos.clear();
				for(SalidaCarteraFwd salida : salidas) {
					CalculoSalida calculoSalida = (CalculoSalida) hCalcularParidadSalida.get(salida.SEC_OPER.longValue());
					CalculoSalida calculoSalidaResultado = (CalculoSalida) hCalcularParidadSalida.get(salida.SEC_OPER.longValue() * -1);
					if(calculoSalida != null && "OK".equals(calculoSalida.Resultado)) {
						salida.montoEquivRecibe = calculoSalida.MontoDestino;
						salida.montoEquivEntrega = calculoSalida.MontoDestino;
					}
					if(calculoSalidaResultado != null && "OK".equals(calculoSalidaResultado.Resultado)) {
						salida.TipoCambioMercado = calculoSalidaResultado.MontoDestino;
						if("ARBITR".equals(salida.Nemotecnico) && "DEB".equals(salida.IdTipoMoneda)) {
							salida.resultado = "C".equals(salida.tipo) ? salida.montoRecibe.divide(calculoSalidaResultado.MontoDestino.subtract(salida.PRECIO_PESOS, MathContext.DECIMAL128), MathContext.DECIMAL128) : salida.montoRecibe.divide(calculoSalidaResultado.MontoDestino.subtract(salida.PRECIO_PESOS, MathContext.DECIMAL128), MathContext.DECIMAL128).negate();
						}else {
							salida.resultado = "C".equals(salida.tipo) ? calculoSalidaResultado.MontoDestino.subtract(salida.PRECIO_PESOS, MathContext.DECIMAL128).multiply(salida.montoRecibe, MathContext.DECIMAL128) : calculoSalidaResultado.MontoDestino.subtract(salida.PRECIO_PESOS, MathContext.DECIMAL128).multiply(salida.montoRecibe, MathContext.DECIMAL128).negate();
						}
						if (!idInternos.contains(salida.SEC_OPER.longValue())){
							CalculoEntrada calculoEntrada = new CalculoEntrada();
							calculoEntrada.IdTrabajo = 0L;
							calculoEntrada.IdInterno = salida.SEC_OPER.longValue();
							calculoEntrada.Fecha = Fecha.toStringDate(entrada.p_FechaHasta);;
							calculoEntrada.IdMonedaOrigen = salida.monedaEntrega;
							calculoEntrada.IdMonedaDestino = monedaSistema;
							calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
							calculoEntrada.MontoOrigen = salida.resultado;
							calculoEntrada.IdUso = "";
							calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
							idInternos.add(salida.SEC_OPER.longValue());
						}
					}
				}
			}
			
			//Resultado se pasa a pesos
			if(calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
				CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
				HashMap<Long,Object> hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
				for(SalidaCarteraFwd salida : salidas) {
					CalculoSalida calculoSalida = (CalculoSalida) hCalcularParidadSalida.get(salida.SEC_OPER.longValue());
					if(calculoSalida != null && "OK".equals(calculoSalida.Resultado)) {
						salida.resultado = calculoSalida.MontoDestino;
					}
				}
			}
			
			List<HashMap<String,Object>> resultadoCarteraFwd = Utils.List2ListHashGenericObject(salidas);
			
			out.P_OUT_SALIDA.put("carteraFwd", resultadoCarteraFwd);
		} catch (Exception e) {
			logger.error("ERROR::CarteraFwd", e);
			throw e;
		} finally {
			Portafolio portafolio = new Portafolio();
			portafolio.IdTrabajo = idTrabajo;
			List<Portafolio> portafolios = Arrays.asList(portafolio);
			sql.batch(con, SQL_DELETE_PORTAFOLIOS_LEGACY, portafolios, Portafolio.class);
		}
		
		logger.debug("FIN::CarteraFwd");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-12-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-12-28");
		Dato dato = new Dato();
		dato.IdCliente = 18145L;
		dato.IdPortafolio = 24144L;
		Objin.p_Datos.add(dato);
		CarteraFwd demo= new CarteraFwd();				
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
