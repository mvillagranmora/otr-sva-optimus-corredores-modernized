package cl.bcs.custodia;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GuardarPatrimonioCustodia implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(GuardarPatrimonioCustodia.class);
	
	private final String SQL_SELECT_CONFIGURACION_PATRIMONIO_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/GuardarPatrimonioCustodia_SelectConfiguracionPatrimonioCustodia.sql");
	private final String SQL_INSERT_PATRIMONIO_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/GuardarPatrimonioCustodia_InsertPatrimonioCustodia.sql");
	private final String SQL_DELETE_PATRIMONIO_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/GuardarPatrimonioCustodia_DeletePatrimonioCustodia.sql");
	private final String SQL_INSTRUMENTOS = loadResourceToString("cl/bcs/custodia/sql/GuardarPatrimonioCustodia_SelectInstrumentos.sql");
	private final String SQL_INSTRUMENTOS_MERCADO = loadResourceToString("cl/bcs/custodia/sql/GuardarPatrimonioCustodia_SelectMercados.sql");
	
	public static class EntradaGuardarPatrimonioCustodia{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class SalidaGuardarPatrimonioCustodia{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaGuardarPatrimonioCustodia() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PatrimonioCustodia{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String FechaPatrimonio;
		public String TipoFechaConsulta; //LIQ,ADQ
		public BigDecimal CantidadPatrimonio;
		public BigDecimal MontoPatrimonio;
		public BigDecimal CantidadCustodia;
		public BigDecimal MontoMercadoCustodia;
		public BigDecimal CantidadGarantia;
		public BigDecimal MontoMercadoGarantia;
		public BigDecimal CantidadSimulCom;
		public BigDecimal MontoMercadoSimulCom;
		public BigDecimal CantidadSimulVen;
		public BigDecimal MontoMercadoSimulVen;
		public BigDecimal CantidadPrestamo;
		public BigDecimal MontoMercadoPrestamo;
		public BigDecimal CantidadEmpresti;
		public BigDecimal MontoMercadoEmpresti;
		public BigDecimal CantidadPactosV;
		public BigDecimal MontoMercadoPactosV;
		public BigDecimal CantidadPactosC;
		public BigDecimal MontoMercadoPactosC;
		public BigDecimal CantidadTranomEgr;
		public BigDecimal MontoMercadoTransEgrNom;
		public BigDecimal CantidadTranomIng;
		public BigDecimal MontoMercadoTransIngNom;
		public BigDecimal CantidadTransIng;
		public BigDecimal MontoMercadoTransIng;
		public BigDecimal CantidadTransEgr;
		public BigDecimal MontoMercadoTransEgr;
		public BigDecimal CantidadTransVenDesEgr;
		public BigDecimal PrecioMercadoCustodia;
		public String IdRegimenTributario;
		public String IdMonedaSistema;
		public String IdEstado;
		public Date FechaFuente;
		public BigDecimal MontoComision;
		public String IdEstadoComision;
		public BigDecimal PorcentajeCobroComision;
		
		public PatrimonioCustodia() {
			this.CantidadPatrimonio = BigDecimal.ZERO;
			this.MontoPatrimonio = BigDecimal.ZERO;
			this.MontoComision = BigDecimal.ZERO;
			this.PorcentajeCobroComision = BigDecimal.ZERO;
			this.IdEstadoComision = "INA";
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
	
	public static class Instrumento{
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Clasificacion{
		public Long Cantidad;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaGuardarPatrimonioCustodia jin = gson.fromJson(in.toString(), EntradaGuardarPatrimonioCustodia.class);
		SalidaGuardarPatrimonioCustodia out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGuardarPatrimonioCustodia logica(Connection con, EntradaGuardarPatrimonioCustodia entrada) throws Exception {
		logger.info("INICIO::GuardarPatrimonioCustodia");
		SalidaGuardarPatrimonioCustodia out = new SalidaGuardarPatrimonioCustodia();
		SQL sql=new SQL();
		
		/*
		 * Inicio Eliminar Patrimonio
		*/
		{
			List<PatrimonioCustodia> patrimoniosEliminar = new ArrayList<PatrimonioCustodia>();
			PatrimonioCustodia patrimonioEliminar = new PatrimonioCustodia();
			patrimonioEliminar.FechaPatrimonio = entrada.p_Fecha;
			patrimoniosEliminar.add(patrimonioEliminar);
			sql.batch(con, SQL_DELETE_PATRIMONIO_CUSTODIA, patrimoniosEliminar, PatrimonioCustodia.class);
		}
		/*
		 * Fin Eliminar Patrimonio
		*/
		
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String parametroCobroComisionPatrimonio = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "CTA.CobroComisionPatrimonio");
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Item> items = sql.query(con, SQL_SELECT_CONFIGURACION_PATRIMONIO_CUSTODIA, params, Item.class);
		params.clear();
		List<Instrumento> instrumentos = sql.query(con, SQL_INSTRUMENTOS, params, Instrumento.class);
		params.clear();
		List<Clasificacion> clasificaciones = sql.query(con, SQL_INSTRUMENTOS_MERCADO, params, Clasificacion.class);
		params.clear();
		
		for(Clasificacion mercado : clasificaciones) {
			logger.debug("COMIENZA::" + mercado.IdClasificacionTipoInstrumento);
			if(mercado.Cantidad < 5000000) {
				llamarCustodia(mercado, null,entrada,idMonedaSistema,con,sql, items,parametroCobroComisionPatrimonio);
			}else {
				List<Instrumento> instrumentosMercado = instrumentos.parallelStream().filter(instrumento-> mercado.IdClasificacionTipoInstrumento.equals(instrumento.IdClasificacionTipoInstrumento)).collect(Collectors.toList());
				for(Instrumento instrumento : instrumentosMercado){
					logger.debug("COMIENZA::" + instrumento.Nemotecnico);
					llamarCustodia(null, instrumento,entrada,idMonedaSistema,con,sql, items,parametroCobroComisionPatrimonio);
					logger.debug("TERMINA::" + instrumento.Nemotecnico);
				}
			}
			logger.debug("TERMINA::" + mercado.IdClasificacionTipoInstrumento);
		}
		
		logger.info("FIN::GuardarPatrimonioCustodia");
		return out;
	}
	
	private PatrimonioCustodia setPatrimonio(Connection con, ConsultarCustodiaPortafolio.Tabla_Salida custodia, List<Item> items, String tipoFecha, String idMonedaSistema, EntradaGuardarPatrimonioCustodia entrada, String parametroCobroComisionPatrimonio) {
		try {

			PatrimonioCustodia patrimonioCustodia = new PatrimonioCustodia();
			patrimonioCustodia.IdCliente = custodia.IdCliente;
			patrimonioCustodia.IdPortafolio = custodia.IdPortafolio;
			patrimonioCustodia.IdInstrumento = custodia.IdInstrumento;
			patrimonioCustodia.Nemotecnico = custodia.Nemotecnico;
			patrimonioCustodia.FechaPatrimonio = entrada.p_Fecha;
			patrimonioCustodia.TipoFechaConsulta = tipoFecha;
			patrimonioCustodia.CantidadCustodia = MonedaFormato.formatear(con, custodia.CantidadCustodia, MonedaFormato.CANTIDAD, idMonedaSistema,custodia.IdTipoInstrumento,"").valor;
			patrimonioCustodia.MontoMercadoCustodia = MonedaFormato.formatear(con, custodia.MontoMercadoCustodia, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadGarantia = MonedaFormato.formatear(con,custodia.CantidadGarantia, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.MontoMercadoGarantia = MonedaFormato.formatear(con,custodia.MontoMercadoGarantia, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadSimulCom = MonedaFormato.formatear(con,custodia.CantidadSimulCom, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.MontoMercadoSimulCom = MonedaFormato.formatear(con,custodia.MontoMercadoSimulCom, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadSimulVen = MonedaFormato.formatear(con,custodia.CantidadSimulVen, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.MontoMercadoSimulVen = MonedaFormato.formatear(con,custodia.MontoMercadoSimulVen, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadPrestamo = MonedaFormato.formatear(con,custodia.CantidadPrestamo, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.MontoMercadoPrestamo = MonedaFormato.formatear(con,custodia.MontoMercadoPrestamo, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadEmpresti = MonedaFormato.formatear(con,custodia.CantidadEmpresti, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.MontoMercadoEmpresti = MonedaFormato.formatear(con,custodia.MontoMercadoEmpresti, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadPactosV = MonedaFormato.formatear(con,custodia.CantidadPactosV, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.MontoMercadoPactosV = MonedaFormato.formatear(con,custodia.MontoMercadoPactosV, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadPactosC = MonedaFormato.formatear(con,custodia.CantidadPactosC, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.MontoMercadoPactosC = MonedaFormato.formatear(con,custodia.MontoMercadoPactosC, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadTranomEgr = MonedaFormato.formatear(con,custodia.CantidadTranomEgr, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.MontoMercadoTransEgrNom = MonedaFormato.formatear(con,custodia.MontoMercadoTransEgrNom, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadTranomIng = MonedaFormato.formatear(con,custodia.CantidadTranomIng, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.MontoMercadoTransIngNom = MonedaFormato.formatear(con,custodia.MontoMercadoTransIngNom, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadTransIng = MonedaFormato.formatear(con,custodia.CantidadTransIng, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.MontoMercadoTransIng = MonedaFormato.formatear(con,custodia.MontoMercadoTransIng, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadTransEgr = MonedaFormato.formatear(con,custodia.CantidadTransEgr, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.MontoMercadoTransEgr = MonedaFormato.formatear(con,custodia.MontoMercadoTransEgr, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadTransVenDesEgr = MonedaFormato.formatear(con,custodia.CantidadTransVenDesEgr, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.PrecioMercadoCustodia = custodia.PrecioMercadoCustodia;
			patrimonioCustodia.IdRegimenTributario = custodia.IdRegimenTributario;
			patrimonioCustodia.IdMonedaSistema = idMonedaSistema;
			patrimonioCustodia.FechaFuente = custodia.FechaFuente;
			for(Item item : items) {
				try{
					Field campoCantidad = custodia.getClass().getDeclaredField(item.CodigoItem);
					Field campoMonto = custodia.getClass().getDeclaredField(item.CodigoMontoValorizado);
					if(campoCantidad != null){
						BigDecimal cantidad = (BigDecimal) campoCantidad.get(custodia);
						if(cantidad != null) {
							patrimonioCustodia.CantidadPatrimonio = patrimonioCustodia.CantidadPatrimonio.add("+".equals(item.FuncionOperador) ? cantidad : cantidad.negate(MathContext.DECIMAL128));
						}
					}
					if(campoMonto != null){
						BigDecimal monto = (BigDecimal) campoMonto.get(custodia);
						if(monto != null) {
							patrimonioCustodia.MontoPatrimonio = patrimonioCustodia.MontoPatrimonio.add("+".equals(item.FuncionOperador) ? monto : monto.negate(MathContext.DECIMAL128));
						}
					}
				}catch(Exception e){
					logger.warn("No existe item:" + item, e);
				}
			}
			patrimonioCustodia.MontoPatrimonio = MonedaFormato.formatear(con,patrimonioCustodia.MontoPatrimonio, MonedaFormato.MONTO, idMonedaSistema).valor;
			patrimonioCustodia.CantidadPatrimonio = MonedaFormato.formatear(con,patrimonioCustodia.CantidadPatrimonio, MonedaFormato.CANTIDAD, idMonedaSistema).valor;
			patrimonioCustodia.IdEstado = patrimonioCustodia.MontoPatrimonio.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "ACT";
			if("ADQ".equals(tipoFecha) && "S".equals(parametroCobroComisionPatrimonio)) {
				patrimonioCustodia.MontoComision = patrimonioCustodia.MontoPatrimonio.compareTo(BigDecimal.ZERO) != 0  ? patrimonioCustodia.MontoPatrimonio.multiply(custodia.PorcentajeCobroComision, MathContext.DECIMAL128).divide(new BigDecimal(365) , MathContext.DECIMAL128) : BigDecimal.ZERO;
				patrimonioCustodia.PorcentajeCobroComision = custodia.PorcentajeCobroComision;
				patrimonioCustodia.IdEstadoComision = patrimonioCustodia.MontoComision.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "VIG";
			}
			patrimonioCustodia.MontoComision = MonedaFormato.formatear(con,patrimonioCustodia.MontoComision, MonedaFormato.MONTO, idMonedaSistema).valor;
			return patrimonioCustodia;
		}catch(Exception e) {
			return null;
		}
		
	}
	
	private void llamarCustodia(Clasificacion clasificacion, Instrumento instrumento, EntradaGuardarPatrimonioCustodia entrada, String idMonedaSistema, Connection con, SQL sql, List<Item> items, String parametroCobroComisionPatrimonio) throws Exception {
		/*
		 * Inicio Por Fecha Liquidacion
		*/
		{
			logger.debug("COMIENZA::LIQUIDACION");
			Entrada_ConsultaCustodiaPortafolio entradaCustodia = new Entrada_ConsultaCustodiaPortafolio();
			entradaCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCustodia.p_IdModulo = entrada.p_IdModulo;
			entradaCustodia.p_IdUsuario = entrada.p_IdUsuario;
			entradaCustodia.p_AgrupaPorBoveda = "N";
			entradaCustodia.p_AgrupaPorRelacion = "N";
			entradaCustodia.p_FechaConsulta = entrada.p_Fecha;
			entradaCustodia.p_IdBoveda = "";
			entradaCustodia.p_IdClasificacionTipoInstrumento = clasificacion == null ? "" : clasificacion.IdClasificacionTipoInstrumento;
			entradaCustodia.p_IdCliente = 0L;
			entradaCustodia.p_IdInstrumento = instrumento == null ? 0L : instrumento.IdInstrumento;
			entradaCustodia.p_IdPortafolio = 0L;
			entradaCustodia.p_IdProductoAgrupador = null;
			entradaCustodia.p_IdTipoInstrumento = "";
			entradaCustodia.p_SaldosNegativos = "N";
			entradaCustodia.p_SaldosNegativosPresuntos = "N";
			entradaCustodia.p_TipoFechaConsulta = "LIQ";
			entradaCustodia.p_LlamadaDesdePatrimonio = "S";
			
			ConsultarCustodiaPortafolio consultarCustodiaPortafolio = new ConsultarCustodiaPortafolio();
			Salida_ConsultaCustodiaPortafolio salidaCustodia = consultarCustodiaPortafolio.logica(con, entradaCustodia);
			logger.debug("COMIENZA PROCESAMIENTO SALIDA::LIQUIDACION");
			List<PatrimonioCustodia> patrimonioFechaLiquidacion = salidaCustodia.P_OUT_TBL.parallelStream().map(custodia -> {
				return setPatrimonio(con, custodia,items,"LIQ",idMonedaSistema,entrada,parametroCobroComisionPatrimonio);
			}).collect(Collectors.toList());
		
			logger.debug("TERMINA PROCESAMIENTO SALIDA::LIQUIDACION");
			sql.batch(con, SQL_INSERT_PATRIMONIO_CUSTODIA, patrimonioFechaLiquidacion, PatrimonioCustodia.class);
			
			logger.debug("TERMINA::LIQUIDACION");
		}
		/*
		 * Fin Por Fecha Liquidacion
		*/
		
		/*
		 * Inicio Por Fecha Adquisicion
		*/
		{
			logger.debug("COMIENZA::ADQUISION");
			Entrada_ConsultaCustodiaPortafolio entradaCustodia = new Entrada_ConsultaCustodiaPortafolio();
			entradaCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCustodia.p_IdModulo = entrada.p_IdModulo;
			entradaCustodia.p_IdUsuario = entrada.p_IdUsuario;
			entradaCustodia.p_AgrupaPorBoveda = "N";
			entradaCustodia.p_AgrupaPorRelacion = "N";
			entradaCustodia.p_FechaConsulta = entrada.p_Fecha;
			entradaCustodia.p_IdBoveda = "";
			entradaCustodia.p_IdClasificacionTipoInstrumento = clasificacion == null ? "" : clasificacion.IdClasificacionTipoInstrumento;
			entradaCustodia.p_IdCliente = 0L;
			entradaCustodia.p_IdInstrumento = instrumento == null ? 0L : instrumento.IdInstrumento;
			entradaCustodia.p_IdPortafolio = 0L;
			entradaCustodia.p_IdProductoAgrupador = null;
			entradaCustodia.p_IdTipoInstrumento = "";
			entradaCustodia.p_SaldosNegativos = "N";
			entradaCustodia.p_SaldosNegativosPresuntos = "N";
			entradaCustodia.p_TipoFechaConsulta = "ADQ";
			entradaCustodia.p_LlamadaDesdePatrimonio = "S";
			
			ConsultarCustodiaPortafolio consultarCustodiaPortafolio = new ConsultarCustodiaPortafolio();
			Salida_ConsultaCustodiaPortafolio salidaCustodia = consultarCustodiaPortafolio.logica(con, entradaCustodia);
			logger.debug("COMIENZA PROCESAMIENTO SALIDA::ADQUISION");
			List<PatrimonioCustodia> patrimonioFechaAdquisicion = salidaCustodia.P_OUT_TBL.parallelStream().map(custodia -> {
				return setPatrimonio(con, custodia,items,"ADQ",idMonedaSistema,entrada,parametroCobroComisionPatrimonio);
			}).collect(Collectors.toList());
			logger.debug("TERMINA PROCESAMIENTO SALIDA::ADQUISION");
			sql.batch(con, SQL_INSERT_PATRIMONIO_CUSTODIA, patrimonioFechaAdquisicion, PatrimonioCustodia.class);
			logger.debug("TERMINA::ADQUISION");
		}
		/*
		 * Fin Por Fecha Adquisicion
		*/
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa01")) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		EntradaGuardarPatrimonioCustodia Objin = new EntradaGuardarPatrimonioCustodia();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "CUS";
		Objin.p_IdUsuario = 347L;
		Objin.p_Fecha = "2020-10-14";
		GuardarPatrimonioCustodia  demo=new GuardarPatrimonioCustodia();				
		try {
			SalidaGuardarPatrimonioCustodia ObjOut=demo.logica(con, Objin);
//			con.commit();
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
