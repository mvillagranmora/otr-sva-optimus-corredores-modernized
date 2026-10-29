package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.normativa.cartola.ResumenInversionBase.EntradaResumenInversionBase;
import cl.bcs.normativa.cartola.ResumenInversionBase.OutResumenInversion;
import cl.bcs.normativa.cartola.ResumenInversionBase.SalidaResumenInversionBase;
import cl.bcs.normativa.cartola.pojo.Moneda;
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

public class PatrimonioValorizado implements BcsCartola{
	
	public final String[] MONEDAS = {"UF", "USD", "EUR"};
	
	private final static Logger logger = LogManager.getLogger(PatrimonioValorizado.class);
	//GENERICOS
	private final String SQL_SELECT_MONEDA = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectMoneda.sql");
	
	public static class SalidaPatrimonioValorizado{
		public Long IdCliente;
		public Long IdPortafolio;
		public String item;
		public BigDecimal monto;
		public String roundMont;
		
		public String toString() {
			return Utils.printJson(this, false);
			
		}
	}
	
	public static class TotalInversion{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMonedaSistema;
		public BigDecimal total;
		
		public TotalInversion() {
			this.total = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
			
		}
	}
	
	public static class PatrimonioValorizadoTemporal{
		public Long IdInterno;
		public Long IdCliente;
		public Long IdPortafolio;
		public String Moneda;
		
		public String toString() {
			return Utils.printJson(this, false);
			
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		logger.info("INICIO::PatrimonioValorizado");
		long inicioServicio = System.currentTimeMillis();
		SalidaCartola out = new SalidaCartola();
		SQL sql = new SQL();
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		ResumenInversionBase resumenInversionBase = new ResumenInversionBase();
		
		if(entrada.p_FechaHastaOrigen == null) {
			entrada.p_FechaHastaOrigen = entrada.p_FechaHasta;
		}
		
		EntradaResumenInversionBase entradaResumenInversionBase = new EntradaResumenInversionBase();
		entradaResumenInversionBase.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaResumenInversionBase.p_IdUsuario = entrada.p_IdUsuario;
		entradaResumenInversionBase.p_FechaDesde = entrada.p_FechaDesde;
		entradaResumenInversionBase.p_FechaHasta = entrada.p_FechaHasta;
		entradaResumenInversionBase.p_Datos = entrada.p_Datos;
		
		SalidaResumenInversionBase salidaResumenInversionBase = resumenInversionBase.logica(con, entradaResumenInversionBase);
		
		HashMap<String,TotalInversion> totales = new HashMap<String,TotalInversion>();
		
		for(OutResumenInversion res : salidaResumenInversionBase.p_out_ResumenInversion) {
				String id = res.IdCliente + "_" + res.IdPortafolio + "_" + res.IdMonedaSistema;
				TotalInversion totalInversion = totales.get(id);
				if(totalInversion == null) {
					totalInversion = new TotalInversion();
					totalInversion.IdCliente = res.IdCliente;
					totalInversion.IdPortafolio = res.IdPortafolio;
					totalInversion.IdMonedaSistema = res.IdMonedaSistema;
				}
				totalInversion.total = totalInversion.total.add(res.MontoMonedaSistema, MathContext.DECIMAL128);
				totales.put(id, totalInversion);
			
		}
		
		List<String> monedasParidad = new ArrayList<String>();
		
		monedasParidad.add(monedaSistema);
		for(String moneda : MONEDAS) {
			monedasParidad.add(moneda);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdMoneda", monedasParidad);
		List<Moneda> lMoneda = sql.query(con, SQL_SELECT_MONEDA, params, Moneda.class);
		HashMap<String,Object> hMoneda = Utils.List2Hash(lMoneda, "IdMoneda");
		
		Long i = 1L;
		List<PatrimonioValorizadoTemporal> patrimonios = new ArrayList<PatrimonioValorizadoTemporal>();
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 0L;
		for(String moneda : monedasParidad) {
			for(Map.Entry<String, TotalInversion> obj : totales.entrySet()) {
				TotalInversion totalInversion = obj.getValue();
				PatrimonioValorizadoTemporal patrimonio = new PatrimonioValorizadoTemporal();
				patrimonio.IdInterno = i;
				patrimonio.IdCliente = totalInversion.IdCliente;
				patrimonio.IdPortafolio = totalInversion.IdPortafolio;
				patrimonio.Moneda = moneda;
				patrimonios.add(patrimonio);
				
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.IdInterno = patrimonio.IdInterno;
				calculoEntrada.IdTrabajo = 0L;
				calculoEntrada.Fecha = Fecha.stringToSqlDate(entrada.p_FechaHastaOrigen);
				calculoEntrada.IdMonedaOrigen = monedaSistema;
				calculoEntrada.IdMonedaDestino = patrimonio.Moneda;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntrada.MontoOrigen = totalInversion.total;
				calculoEntrada.IdUso = "CARTOLA";
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
				i++;
			}
		}
		
		CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
		HashMap<Long,Object> hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
		
		List<SalidaPatrimonioValorizado> salidas = patrimonios.stream()
			    .map(patrimonio -> {
			        CalculoSalida calculoSalida = (CalculoSalida) hCalcularParidadSalida.get(patrimonio.IdInterno);
			        Moneda moneda = (Moneda) hMoneda.get(patrimonio.Moneda);

			        SalidaPatrimonioValorizado salida = new SalidaPatrimonioValorizado();
			        salida.IdCliente = patrimonio.IdCliente;
			        salida.IdPortafolio = patrimonio.IdPortafolio;

			        if (moneda != null) {
			            salida.item = WordUtils.capitalizeFully(moneda.Nombre, new char[]{' '}) + "(" + moneda.IdMoneda + ")";
			            salida.roundMont = "CLP".equals(moneda.IdMoneda) ? "N" : "S";
			        } else {
			            salida.roundMont = "S";
			        }

			        salida.monto = calculoSalida.MontoDestino;
			        return salida;
			    }).collect(Collectors.toList());
		
		List<HashMap<String,Object>> resultadoPatrimonioValorizado = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("patrimonioValorizado", resultadoPatrimonioValorizado);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::PatrimonioValorizado", finServicio - inicioServicio);
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBIConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2019-03-01");
		Objin.p_FechaHasta = Fecha.toDate("2019-03-30");
		Dato dato = new Dato();
		dato.IdCliente = 6143L;
		dato.IdPortafolio = 6581L;
		Objin.p_Datos.add(dato);
		PatrimonioValorizado  demo= new PatrimonioValorizado();				
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
