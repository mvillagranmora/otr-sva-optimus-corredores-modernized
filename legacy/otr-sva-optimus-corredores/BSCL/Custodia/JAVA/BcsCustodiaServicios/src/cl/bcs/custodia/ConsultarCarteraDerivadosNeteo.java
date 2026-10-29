package cl.bcs.custodia;


import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarCarteraDerivados.CustodiaDerivados;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada.ObjInstrumento;
import cl.bcs.custodia.pojo.Cartera;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarCarteraDerivadosNeteo implements JavaCall{
	
	
	private final String SELECT_PORTAFOLIOS =  loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivadosNeteo_SelectPortafolios.sql");
	private final String SELECT_CARTERA_POR_ID = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivadosNeteo_ObtenerCarteraPorId.sql");
	
	//private final String SELECT_CARTERA_POR_ID_PARA_RETIRO = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivadosNeteo_ObtenerCarteraPorIdParaRetiro.sql");
	
	private final String SELECT_POSICIONES_INICIALES_DEL_DIA = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivadosNeteo_ObtenerPosicionesInicialesDelDia.sql");
	private final String SELECT_POSICIONES_INICIALES_DEL_DIA_POR_FECHA = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivadosNeteo_ObtenerPosicionesInicialesDelDiaPorFecha.sql");
	
	private final String SELECT_POSICIONES_DEL_DIA_POR_INSTRUMENTO = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivadosNeteo_ObtenerPosicionesDelDiaPorInstrumento.sql");
	
	private final String SELECT_EJERCIDOS_SORTEADOS = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivadosNeteo_SelectEjercidosSorteados.sql");;
	
	public static class Entrada_ConsultarCarteraDerivados{
		public Long p_IdEmpresa;		
		public String p_IdModulo;		
		public Date p_FechaConsulta;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}	
	
	public static class CarteraDerivados{
		public Long IdCartera;
		public Long FolioCartera;
		public String Instrumento;
		public String Tipo;
		public BigDecimal Contratos;
		public String TipoOpcion;
		public String Mercado;
		public String Familia;
		public String Subyacente;
		public String Estilo;
		public String ModalidadLiquidacion;
		public Date FechaVencimiento;
		public BigDecimal PrecioEjercicio;
		public Integer PrecioSubyacente;
		public String FechaInver;
		public BigDecimal ContratoEjercidos;
		public BigDecimal ContratoSorteados;
		public String IdTipoOperacion;
		
		public CarteraDerivados() {
			FechaInver = "2019-12-17";
			PrecioSubyacente = 0;
			ContratoEjercidos = new BigDecimal(0);
			ContratoSorteados = new BigDecimal(0);
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class EjercidosSorteados{
		public BigDecimal ContratosEjercidos;
		public BigDecimal ContratosSorteados;
		
		public EjercidosSorteados() {
			ContratosEjercidos = BigDecimal.ZERO;
			ContratosSorteados = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Salida_ConsultarCustodiaDerivados{
		public List<CustodiaDerivados> P_OUT_CUSTODIA_DERIVADOS_COMPRAS;
		public List<CustodiaDerivados> P_OUT_CUSTODIA_DERIVADOS_VENTAS;
		public List<CustodiaDerivados> P_OUT_CUSTODIA_DERIVADOS_TRANS;
		
		
		public Salida_ConsultarCustodiaDerivados() {
			P_OUT_CUSTODIA_DERIVADOS_COMPRAS = new ArrayList<CustodiaDerivados>();
			P_OUT_CUSTODIA_DERIVADOS_VENTAS = new ArrayList<CustodiaDerivados>();
			P_OUT_CUSTODIA_DERIVADOS_TRANS = new ArrayList<CustodiaDerivados>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio{
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private List<Portafolio> obtenerPortafolios(Connection con, Entrada_ConsultarCarteraDerivados entrada) throws SQLException{
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("p_FechaOperacion", entrada.p_FechaConsulta);
		List<Portafolio> portafolios = sql.query(con, SELECT_PORTAFOLIOS, params, Portafolio.class);
		return portafolios;
	}
	
	private RetiroCustodia.ObjDatosEntrada obtenerCarteraPorIdParaRetiro(Connection con, Long id) throws SQLException {
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		List<RetiroCustodia.ObjDatosEntrada> carteras = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		params.put("p_IdCartera", id);
		carteras = sql.query(con, SELECT_CARTERA_POR_ID, params, RetiroCustodia.ObjDatosEntrada.class);
		
		return carteras.get(0);		
	}
	
	private ObjDatosEntrada obtenerCarteraPorIdParaIngreso(Connection con, Long id) throws SQLException {
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		List<IngresoCustodia.ObjDatosEntrada> carteras = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
		params.put("p_IdCartera", id);
		carteras = sql.query(con, SELECT_CARTERA_POR_ID, params, ObjDatosEntrada.class);
		return carteras.get(0);				
	}
	
	private List<CarteraDerivados> obtenerPosicionesIniciales(Connection con, Entrada_ConsultarCarteraDerivados entrada ,Date p_FechaConsulta, Long IdPortafolio) throws Exception{
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		List<CarteraDerivados> carterasDerivados = new ArrayList<CarteraDerivados>();
		
		params.put("p_FechaOperacion", p_FechaConsulta);
		//params.put("p_IdCliente", entrada.p_IdCliente);
		params.put("p_IdPortafolio", IdPortafolio);
		carterasDerivados = sql.query(con, SELECT_POSICIONES_INICIALES_DEL_DIA, params, CarteraDerivados.class);
		
		List<CarteraDerivados> carterasDerivadosPorFecha = new ArrayList<CarteraDerivados>();
		params.put("p_FechaOperacion", p_FechaConsulta);
		//params.put("p_IdCliente", entrada.p_IdCliente);
		params.put("p_IdPortafolio", IdPortafolio);
		carterasDerivadosPorFecha = sql.query(con, SELECT_POSICIONES_INICIALES_DEL_DIA_POR_FECHA, params, CarteraDerivados.class);
		carterasDerivados.addAll(carterasDerivadosPorFecha);		
		return carterasDerivados;
	}
	
	
	private List<CarteraDerivados> obtenerPosicionesPorNemotecnico(Connection con, 
			Date FechaOperacion, 
			String Nemotecnico, 
			List<Long> IdsCarteras, Long IdPortafolio) throws Exception{
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		List<CarteraDerivados> carterasDerivados = new ArrayList<CarteraDerivados>();
		params.put("p_FechaOperacion", FechaOperacion);
		params.put("p_Nemotecnico", Nemotecnico);
		//params.put("p_IdCliente", entrada.p_IdCliente);
		params.put("p_IdPortafolio", IdPortafolio);
		params.put("p_IdsCarteras", IdsCarteras);
		carterasDerivados = sql.query(con, SELECT_POSICIONES_DEL_DIA_POR_INSTRUMENTO, params, CarteraDerivados.class);	
		return carterasDerivados;
	}
	
	private EjercidosSorteados obtenerEjercidosSorteados(Connection con, Date FechaOperacion, String Nemotecnico, Long IdPortafolio) throws Exception {
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("p_Nemotecnico", Nemotecnico);
		params.put("p_FechaOperacion", FechaOperacion);
		params.put("p_IdPortafolio", IdPortafolio);
		List<EjercidosSorteados> ejercidosSorteados = new ArrayList<EjercidosSorteados>();
		ejercidosSorteados = sql.query(con, SELECT_EJERCIDOS_SORTEADOS, params, EjercidosSorteados.class);
		if(ejercidosSorteados.isEmpty()) {
			return new EjercidosSorteados();
		}
		return ejercidosSorteados.get(0);
	}
	
	
	private void retirarTitulos(Connection con, RetiroCustodia.ObjDatosEntrada cartera) throws Exception {		
		RetiroCustodia retiroCustodia = new RetiroCustodia();
		RetiroCustodia.RetiroCustodiaEntrada entradaRetiroCustodia = new RetiroCustodia.RetiroCustodiaEntrada();
		entradaRetiroCustodia.p_IdEmpresa = 1L;
		entradaRetiroCustodia.p_IdModulo = "CUS";
		entradaRetiroCustodia.p_IdUsuario = 0L;
		entradaRetiroCustodia.p_SimularOperacion = "N";
		cartera.AccionOperacion = "RETIRO";		
		cartera.TablaAfectada = "CARTERA";
		cartera.IdGenerarMovimientoCustodia = 0L;		
		entradaRetiroCustodia.p_DatosEntrada.add(cartera);		
		retiroCustodia.logica(con, entradaRetiroCustodia);		
	}
	
	private java.sql.Date siguienteDiaHabil(Connection con, Date fechaOperacion) throws Exception {
		ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente entradaObtenerDiaSiguiente = new InObtenerDiaSiguiente();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  		
		entradaObtenerDiaSiguiente.p_soloCaeDiaHabil = "S";
		entradaObtenerDiaSiguiente.p_diaHabil = "S";		
		FechasIn fecha = new FechasIn();
		fecha.fechaInicial = dateFormat.format(fechaOperacion);
		fecha.numeroDias = 1L;
		fecha.pais = 44L;
		entradaObtenerDiaSiguiente.p_fechas.add(fecha);
		OutObtenerDiaSiguiente salidaObtenerDiaSiguiente = obtenerDiaSiguiente.logica(con, entradaObtenerDiaSiguiente);
		
		return java.sql.Date.valueOf( salidaObtenerDiaSiguiente.p_out_fechas.get(0).fechaCalculada );
	}
	
	private void crearNuevaCartera(Connection con, ObjDatosEntrada carteraInicial, BigDecimal total, String operacion) throws Exception {		
		IngresoCustodia ingresoCustodia = new IngresoCustodia();
		IngresoCustodia.IngresoCustodiaEntrada entradaCustodia = new IngresoCustodia.IngresoCustodiaEntrada();
		entradaCustodia.p_IdEmpresa = 1L;
		entradaCustodia.p_IdModulo = "CUS";
		entradaCustodia.p_IdUsuario = 0L;
		entradaCustodia.p_SimularOperacion = "N";
		entradaCustodia.p_TicketAtencion = 0L;
		entradaCustodia.p_ValorizacionAutomatica = "N";
		carteraInicial.IdConcepto = "NETDER"; 
		carteraInicial.AccionOperacion = "INGRESO";
		carteraInicial.EsRegistroNuevo = "S";
		carteraInicial.TablaAfectada = "CARTERA";		
		carteraInicial.IdInstrumento = carteraInicial.IdInstrumento;
		carteraInicial.Cantidad = total;
		carteraInicial.FolioCarteraRelacionada = carteraInicial.FolioCustodia;
		carteraInicial.IdTipoOperacion = operacion;		
		carteraInicial.FechaOperacion = siguienteDiaHabil(con, carteraInicial.FechaOperacion);		
		entradaCustodia.p_DatosEntrada.add(carteraInicial);
		ingresoCustodia.logica(con, entradaCustodia);	
	}
	
	private List<Long> obtenerIdsCarteras(List<CarteraDerivados> carteras){
		List<Long> ids = new ArrayList<Long>();
		for(CarteraDerivados cartera: carteras)
			ids.add(cartera.IdCartera);
		return ids;
	}
	
	private String esOpcionFuturo(String TipoOperacion) {
		String TipoDerivado = "";
		if(TipoOperacion.substring(0,4).contentEquals("COMO") || TipoOperacion.substring(0,4).contentEquals("VENO"))
			TipoDerivado = "OP";
		if(TipoOperacion.substring(0,4).contentEquals("COMF") || TipoOperacion.substring(0,4).contentEquals("VENF"))
			TipoDerivado = "FU";
		return TipoDerivado;
	}
	
	private Boolean estaVencida(Connection con, Entrada_ConsultarCarteraDerivados entrada, CarteraDerivados cartera) throws Exception {
		//String fechaActual = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date fechaActual = format.parse(ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema"));
		if(cartera.FechaVencimiento.compareTo(fechaActual) > 0 ) {
			return false;
		}
		else {
			return true;
		}		
		
	};
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		Entrada_ConsultarCarteraDerivados in=gson.fromJson(jin.toString(),Entrada_ConsultarCarteraDerivados.class);
		Salida_ConsultarCustodiaDerivados out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public Salida_ConsultarCustodiaDerivados logica(Connection con, Entrada_ConsultarCarteraDerivados entrada) throws Exception{
		Salida_ConsultarCustodiaDerivados custodias = new Salida_ConsultarCustodiaDerivados();		
		List<CarteraDerivados> posicionesIniciales = new ArrayList<CarteraDerivados>();
		List<Portafolio> portafolios = obtenerPortafolios(con, entrada);
		for(Portafolio portafolio: portafolios) {
			posicionesIniciales = obtenerPosicionesIniciales(con, entrada, entrada.p_FechaConsulta, portafolio.IdPortafolio);
		
			List<Long> idsCarteras = obtenerIdsCarteras(posicionesIniciales);
		
			String IdTipoOperacion = "";
		
			for(CarteraDerivados posicionInicial: posicionesIniciales) {
				
				List<CarteraDerivados> posicionesDelDiaPorInstrumento = obtenerPosicionesPorNemotecnico(con, entrada.p_FechaConsulta, posicionInicial.Instrumento, idsCarteras, portafolio.IdPortafolio);
				EjercidosSorteados ejercidosSorteados = obtenerEjercidosSorteados(con, entrada.p_FechaConsulta, posicionInicial.Instrumento, portafolio.IdPortafolio);
				
				BigDecimal totalInicial = posicionInicial.Contratos;
				BigDecimal totalCompras = new BigDecimal(0);
				BigDecimal totalVentas = new BigDecimal(0);
				BigDecimal totalContratos =  new BigDecimal(0);
				BigDecimal totalEjercidos =  ejercidosSorteados.ContratosEjercidos;
				BigDecimal totalSorteados =  ejercidosSorteados.ContratosSorteados;
				BigDecimal total = new BigDecimal(0);			
				for(CarteraDerivados posicionDelDiaPorInstrumento: posicionesDelDiaPorInstrumento) {
					if(posicionDelDiaPorInstrumento.Tipo.equals("COMPRA")) {
						totalCompras = totalCompras.add(posicionDelDiaPorInstrumento.Contratos);
					}
					if(posicionDelDiaPorInstrumento.Tipo.equals("VENTA")) {
						totalVentas = totalVentas.add(posicionDelDiaPorInstrumento.Contratos);
					}
					retirarTitulos(con, obtenerCarteraPorIdParaRetiro(con, posicionDelDiaPorInstrumento.IdCartera));
				}		
			
				if(posicionInicial.Tipo.equals("COMPRA")) {
					totalContratos = posicionInicial.ContratoEjercidos;
					total = totalInicial.add(totalCompras).subtract(totalContratos).subtract(totalVentas).subtract(totalEjercidos).subtract(totalSorteados);				
					int resultado = total.compareTo(new BigDecimal(0));
					/* OP */
					if(resultado == 0 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("OP"))
						IdTipoOperacion = "CERRADA";
					else if(resultado == 1 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("OP"))
						IdTipoOperacion = "COMOAC";
					else if(resultado == -1 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("OP"))
						IdTipoOperacion = "VENOAC";
					/* FU */
					if(resultado == 0 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("FU"))
						IdTipoOperacion = "CERRADA";
					else if(resultado == 1 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("FU"))
						IdTipoOperacion = "COMFAC";
					else if(resultado == -1 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("FU"))
						IdTipoOperacion = "VENFAC";
				}			
				if(posicionInicial.Tipo.equals("VENTA")) {
					totalContratos = posicionInicial.ContratoSorteados;
					total = totalInicial.add(totalVentas).subtract(totalContratos).subtract(totalCompras).subtract(totalEjercidos).subtract(totalSorteados);
					int resultado = total.compareTo(new BigDecimal(0));
					/* OP */
					if(resultado == 0 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("OP"))
						IdTipoOperacion = "CERRADA";
					else if(resultado == 1 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("OP"))
						IdTipoOperacion = "VENOAC";
					else if(resultado == -1 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("OP"))
						IdTipoOperacion = "COMOAC";
					/* FU */
					if(resultado == 0 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("FU"))
						IdTipoOperacion = "CERRADA";
					else if(resultado == 1 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("FU"))
						IdTipoOperacion = "VENFAC";
					else if(resultado == -1 && esOpcionFuturo(posicionInicial.IdTipoOperacion).equals("FU"))
						IdTipoOperacion = "COMFAC";
				
				}		
				if(!IdTipoOperacion.equals("CERRADA")) {		
					if(!estaVencida(con, entrada, posicionInicial)) {
						crearNuevaCartera(con, obtenerCarteraPorIdParaIngreso(con, posicionInicial.IdCartera), total.abs(), IdTipoOperacion);						
					}
					retirarTitulos(con, obtenerCarteraPorIdParaRetiro(con, posicionInicial.IdCartera));
				}
			
			}
		}
		
		return custodias;
	}
	
	public static void main(String[] args) { 
		System.out.println("=== BEGIN TEST ===");
		
		try (Connection con =  PlataformaUtils.getDESAConnection()){
			Gson gson = Utils.getGsonBuilder();			
			
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdCliente\":524891,\"p_IdPortafolio\":null,\"p_IdClasificacionTipoInstrumento\":\"DV\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_IdProductoAgrupador\":null,\"p_AgrupaPorRelacion\":\"S\",\"p_SaldosNegativos\":\"N\",\"p_SaldosNegativosPresuntos\":\"N\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_FechaConsulta\":\"2019-12-17\"}";
			
			String json = "{\r\n" + 
					"  \"p_IdEmpresa\": 1,\r\n" + 					
					"  \"p_IdModulo\": \"CUS\",\r\n" + 
					"  \"p_FechaConsulta\": \"2020-02-25\"\r\n" + 					
					//"  \"p_IdCliente\": 67760,\r\n" +
					//"  \"p_TipoDerivado\": \"OP\",\r\n" +
					//"  \"p_IdPortafolio\": 412766\r\n" +
					"}";
			
			Entrada_ConsultarCarteraDerivados entrada = gson.fromJson(json, Entrada_ConsultarCarteraDerivados.class);
			ConsultarCarteraDerivadosNeteo demo = new ConsultarCarteraDerivadosNeteo();
			long init = System.currentTimeMillis();
			Salida_ConsultarCustodiaDerivados salida = demo.logica(con, entrada);			
			System.out.println(String.format("TERMINO EN: %d", (System.currentTimeMillis()-init)/1000));			
			System.out.println(salida);			
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("=== END TEST ===");
	}

}