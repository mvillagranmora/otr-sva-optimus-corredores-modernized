package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Tabla_Salida;
import cl.bcs.custodia.GenerarCuadraturaCustodiaCircular1518.DatoCuentaPropia;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import java.sql.Date;
import cl.bcs.utils.Utils;

public class CargarArchivoCircularCarteraCliente  implements JavaCall {

	static final Logger logger = LogManager.getLogger(CargarArchivoCircularCarteraCliente.class);

	private final String SQL_SELECT_DATOS_CUENTA_PROPIA = loadResourceToString("cl/bcs/custodia/sql/CargarArchivoCircularCarteraCliente_SelectDatosCuentaPropia.sql");
	private final String SQL_DELETE_CUADRATURA_CIRCULARCARTERACLIENTE = loadResourceToString("cl/bcs/custodia/sql/CargarArchivoCircularCarteraCliente_DeleteCuadraturaCircularCarteraCliente.sql");
	private final String SQL_INSERT_CUADRATURA_CIRCULARCARTERACLIENTE = loadResourceToString("cl/bcs/custodia/sql/CargarArchivoCircularCarteraCliente_InsertCuadraturaCircularCarteraCliente.sql");
	private final String SQL_UPDATE_CIRCULARCARTERACLIENTE = loadResourceToString("cl/bcs/custodia/sql/CargarArchivoCircularCarteraCliente_UpdateCircularCarteraCliente.sql");

	public static class EntradaCargarArchivoCircularCarteraCliente extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
        public String p_IdModulo;
		public String p_FechaCuadratura;
	}
	public static class SalidaCargarArchivoCircularCarteraCliente extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;

		public SalidaCargarArchivoCircularCarteraCliente() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	public static class Custodia extends BcsPojo {
		public String IdEmisor;
		public String FechaCuadratura;
		public String Nemotecnico;
        public Long IdPortafolio;
        public String ISIN;
        public Long IdCliente;
        public Date FechaVencimiento;
        public String IdMonedaPrincipal;
        public String ClasificacionRiesgoInstrumento;
        public String IdMonedaInstrReajuste;
        public BigDecimal ValorMercadoCustodia;
        public BigDecimal MontoMercadoCustodia;
        public BigDecimal TasaMercadoCustodia;
        public BigDecimal ValorCompra;
        public BigDecimal MontoCompra;
        public String IdBoveda;
		public BigDecimal PrecioCompraCustodia;
    	public BigDecimal CantidadTotal;
		public BigDecimal CantidadGarantia;
		public BigDecimal CantidadCustodia;
		public BigDecimal PrecioMercadoCustodia;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		
		public BigDecimal CantidadPrestamo;
        public BigDecimal CantidadPlazoCompra;
        public BigDecimal CantidadPlazoVenta;
        public BigDecimal ValorizacionGarantia;
        public BigDecimal ValorizacionPrestamo;
        public BigDecimal ValorizacionPlazoCompra;
        public BigDecimal ValorizacionPlazoVenta;
        
        public String ClasificacionInstrumentoCustodia;
        public String ClasificacionInstrumentoGarantia;
        public String ClasificacionInstrumentoPrestamo;
        public String ClasificacionInstrumentoPlazo;
        
        public String IdTipoInstrumentoPlazoCompra;
        public String IdTipoInstrumentoPlazoVenta;
        
        public Long IdInstrumento;
        
        public BigDecimal PrecioMercadoGarantia;
        public BigDecimal PrecioMercadoPrestamo;
        public BigDecimal PrecioMercadoPactosC;
        public BigDecimal PrecioMercadoPactosV;
        public BigDecimal PrecioMercadoSimulCom;
        public BigDecimal PrecioMercadoSimulVen;
        
        public BigDecimal TasaMercadoGarantia;
        public BigDecimal TasaMercadoPrestamo;
        public BigDecimal TasaMercadoPactosC;
        public BigDecimal TasaMercadoPactosV;
        public BigDecimal TasaMercadoSimulCom;
        public BigDecimal TasaMercadoSimulVen;

	public Custodia() {
        this.IdMonedaPrincipal = "";
        this.IdMonedaInstrReajuste = "";
        this.ValorMercadoCustodia = BigDecimal.ZERO;
        this.MontoMercadoCustodia = BigDecimal.ZERO;
        this.TasaMercadoCustodia = BigDecimal.ZERO;
        this.ValorCompra = BigDecimal.ZERO;
        this.MontoCompra = BigDecimal.ZERO;
        this.PrecioCompraCustodia = BigDecimal.ZERO;
        this.CantidadTotal = BigDecimal.ZERO;
        this.CantidadGarantia = BigDecimal.ZERO;
        this.CantidadCustodia = BigDecimal.ZERO;
        this.PrecioMercadoCustodia = BigDecimal.ZERO;
        this.PrecioCompraCustodia = BigDecimal.ZERO;
        this.FechaCuadratura = "";
        this.Nemotecnico = "";
        this.ISIN = "";
        this.ClasificacionRiesgoInstrumento = "";
        this.IdBoveda = "";
        this.IdClasificacionTipoInstrumento = "";
        this.IdTipoInstrumento = "";
        this.IdEmisor = "";
        this.IdPortafolio = 0L;
        this.IdCliente = 0L;
        this.FechaVencimiento = Date.valueOf("1990-01-01");
        
        this.CantidadPrestamo = BigDecimal.ZERO;
        this.CantidadPlazoCompra = BigDecimal.ZERO;
        this.CantidadPlazoVenta = BigDecimal.ZERO;
        this.ValorizacionGarantia = BigDecimal.ZERO;
        this.ValorizacionPrestamo = BigDecimal.ZERO;
        this.ValorizacionPlazoCompra = BigDecimal.ZERO;
        this.ValorizacionPlazoVenta = BigDecimal.ZERO;
        
        this.ClasificacionInstrumentoCustodia = "";
        this.ClasificacionInstrumentoGarantia = "";
        this.ClasificacionInstrumentoPrestamo = "";
        this.ClasificacionInstrumentoPlazo = "";
        
        this.IdTipoInstrumentoPlazoCompra = "";
        this.IdTipoInstrumentoPlazoVenta = "";
        
        this.IdInstrumento = 0L;
        
        this.PrecioMercadoGarantia = BigDecimal.ZERO;
        this.PrecioMercadoPrestamo = BigDecimal.ZERO;
        this.PrecioMercadoPactosC = BigDecimal.ZERO;
        this.PrecioMercadoPactosV = BigDecimal.ZERO;
        this.PrecioMercadoSimulCom = BigDecimal.ZERO;
        this.PrecioMercadoSimulVen = BigDecimal.ZERO;

        this.TasaMercadoGarantia = BigDecimal.ZERO;
        this.TasaMercadoPrestamo = BigDecimal.ZERO;
        this.TasaMercadoPactosC = BigDecimal.ZERO;
        this.TasaMercadoPactosV = BigDecimal.ZERO;
        this.TasaMercadoSimulCom = BigDecimal.ZERO;
        this.TasaMercadoSimulVen = BigDecimal.ZERO;
    }


}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCargarArchivoCircularCarteraCliente jin = gson.fromJson(in.toString(), EntradaCargarArchivoCircularCarteraCliente.class);
		SalidaCargarArchivoCircularCarteraCliente out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaCargarArchivoCircularCarteraCliente logica(Connection con,
			EntradaCargarArchivoCircularCarteraCliente entrada) throws Exception {

		logger.info("INICIO::CircularCartera");
	    long inicioServicio = System.currentTimeMillis();
	    SalidaCargarArchivoCircularCarteraCliente out = new SalidaCargarArchivoCircularCarteraCliente();

	    borrarCargaCuadratura(con, entrada.p_FechaCuadratura);
	    
	    List<Tabla_Salida> custodiasPorCliente = procesarNuevosDatos(con, entrada);
	    String fechaCuadratura;
	    fechaCuadratura = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	    List<Custodia> custodias = obtenerCustodias(custodiasPorCliente, entrada.p_FechaCuadratura);
	    insertarCargaCuadratura(con, custodias);
        
	     

	    long finServicio = System.currentTimeMillis();
	    logger.info("FIN::CircularCartera", finServicio - inicioServicio);
	    return out;
	}

	private void borrarCargaCuadratura(Connection con, String p_FechaCuadratura) throws SQLException {
		logger.debug("INICIO::borrarCargaCIRCULARCARTERACLIENTE");
		long inicio = System.currentTimeMillis();

		Custodia custodia = new Custodia();
		custodia.FechaCuadratura = p_FechaCuadratura;

		SQL _sql = new SQL();
		_sql.batch(con, SQL_DELETE_CUADRATURA_CIRCULARCARTERACLIENTE, Arrays.asList(custodia), Custodia.class);


		long fin = System.currentTimeMillis();
		logger.debug("FIN::borrarCargaCIRCULARCARTERACLIENTE", fin - inicio);

	}

	private List<Tabla_Salida> procesarNuevosDatos(Connection con, EntradaCargarArchivoCircularCarteraCliente entrada) throws SQLException {
		  logger.debug("INICIO::procesarNuevosDatos");
  	    long inicio = System.currentTimeMillis();

  	   List<DatoCuentaPropia> datosCuentaPropia = obtenerDatosCuentaPropia(con, entrada.p_IdEmpresa);


  	   List<Tabla_Salida> custodiasCuentaPropia = obtenerCustodiaCuentaPropia(con, entrada, datosCuentaPropia);


  	    List<Tabla_Salida> custodiasClientes = new ArrayList<>();
  	    custodiasClientes.addAll(custodiasCuentaPropia);

  	    long fin = System.currentTimeMillis();
  	    logger.debug("FIN::procesarNuevosDatos", fin - inicio);
  	    return custodiasCuentaPropia;
	}

	private List<DatoCuentaPropia> obtenerDatosCuentaPropia(Connection con, Long p_IdEmpresa) throws SQLException {
		logger.debug("INICIO::obtenerDatosCuentaPropia");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametro = new HashMap<>();
		parametro.put("IdEmpresa", p_IdEmpresa);

		SQL _sql = new SQL();
		List<DatoCuentaPropia> datosCuentaPropia = _sql.query(con, SQL_SELECT_DATOS_CUENTA_PROPIA, parametro, DatoCuentaPropia.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN:obtenerDatosCuentaPropia", fin - inicio);
		return datosCuentaPropia;

	}
	private List<Tabla_Salida> obtenerCustodiaCuentaPropia(Connection con,
			EntradaCargarArchivoCircularCarteraCliente entrada, List<DatoCuentaPropia> datosCuentaPropia) {


	    logger.debug("INICIO::obtenerCustodiaCuentaPropia");
	    long inicio = System.currentTimeMillis();

	    List<Tabla_Salida> custodiaCuentaPropia = new ArrayList<>();

	    for (DatoCuentaPropia datoCuentaPropia : datosCuentaPropia) {

	        Entrada_ConsultaCustodiaPortafolio entradaConsultarCustodia = new Entrada_ConsultaCustodiaPortafolio();
	        entradaConsultarCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
	        entradaConsultarCustodia.p_IdUsuario = entrada.p_IdUsuario;
	        entradaConsultarCustodia.p_IdModulo = entrada.p_IdModulo;
	        entradaConsultarCustodia.p_IdCliente = datoCuentaPropia.IdCliente;
	        entradaConsultarCustodia.p_IdPortafolio = datoCuentaPropia.IdPortafolio;
	        entradaConsultarCustodia.p_FechaConsulta = entrada.p_FechaCuadratura;
	        entradaConsultarCustodia.p_TipoFechaConsulta = "ADQ";
	        entradaConsultarCustodia.p_AgrupaPorBoveda = "S";
	        entradaConsultarCustodia.p_AgrupaPorRelacion = "S";
	        entradaConsultarCustodia.p_SaldosNegativos = "N";
	        entradaConsultarCustodia.p_SaldosNegativosPresuntos = "N";
	        entradaConsultarCustodia.p_SolicitaDividendo = "S";
	        
	        logger.info("Fecha Consultar Custodia: " + entradaConsultarCustodia.p_FechaConsulta);
	        // Consultar la custodia y agregar los resultados a la lista
	        ConsultarCustodiaPortafolio consultarCustodiaPortafolio = new ConsultarCustodiaPortafolio();
	        Salida_ConsultaCustodiaPortafolio salidaConsultaCustodiaPortafolio;
			try {
				salidaConsultaCustodiaPortafolio = consultarCustodiaPortafolio.logica(con, entradaConsultarCustodia);
				 custodiaCuentaPropia.addAll(salidaConsultaCustodiaPortafolio.P_OUT_TBL);
			} catch (Exception e) {
				// TODO Auto-generated catch block 
				e.printStackTrace();
			}

	    }

	    long fin = System.currentTimeMillis();
	    logger.debug("FIN::obtenerCustodiaCuentaPropia", fin - inicio);
	    return custodiaCuentaPropia;


	}
	private List<Custodia> obtenerCustodias(List<Tabla_Salida> custodiasPorCliente, String fechaCuadratura) {
        logger.debug("INICIO::obtenerCustodias");
        long inicio = System.currentTimeMillis();

        List<Custodia> custodias = new ArrayList<>();
        
        logger.info("Fecha Cuadratura: " + fechaCuadratura);
        
        for (Tabla_Salida registro : custodiasPorCliente) {
            Custodia custodia = new Custodia();
            custodia.IdEmisor = registro.IdEmisor;
            custodia.Nemotecnico = registro.Nemotecnico;
            custodia.IdPortafolio = registro.IdPortafolio;
            custodia.FechaCuadratura = (fechaCuadratura != null && !fechaCuadratura.isEmpty()) ? fechaCuadratura : fechaCuadratura;
            custodia.ISIN = registro.ISIN;
            custodia.IdCliente = registro.IdCliente;
            custodia.FechaVencimiento = registro.FechaVencimiento;
            custodia.IdMonedaPrincipal = registro.IdMonedaPrincipal;
            custodia.ClasificacionRiesgoInstrumento = registro.ClasificacionRiesgoInstrumento;
            custodia.IdMonedaInstrReajuste = registro.IdMonedaInstrReajuste;
            custodia.ValorMercadoCustodia = registro.ValorMercadoCustodia;
            custodia.MontoMercadoCustodia = registro.MontoMercadoCustodia;
            custodia.TasaMercadoCustodia = registro.TasaMercadoCustodia;
            custodia.PrecioCompraCustodia = registro.PrecioCompraCustodia;
            custodia.IdClasificacionTipoInstrumento = registro.IdClasificacionTipoInstrumento;
            custodia.IdTipoInstrumento = registro.IdTipoInstrumento;
            custodia.CantidadTotal = registro.CantidadTotal;
            custodia.ValorCompra = registro.ValorCompra;
            custodia.MontoCompra = registro.MontoCompra;
            custodia.CantidadGarantia = registro.CantidadGarantia;
            custodia.CantidadCustodia = registro.CantidadCustodia;
            custodia.PrecioMercadoCustodia = registro.PrecioMercadoCustodia;
            custodia.PrecioCompraCustodia = registro.PrecioCompraCustodia;
            custodia.CantidadPrestamo = registro.CantidadPrestamo.add(registro.CantidadEmpresti);
            custodia.ValorizacionPrestamo = registro.ValorMercadoPrestamo;
            custodia.ValorizacionGarantia = registro.ValorMercadoGarantia; 
            custodia.CantidadPlazoCompra = registro.IdClasificacionTipoInstrumento.contains("RF") ? registro.CantidadPactosC : registro.CantidadSimulCom;
            custodia.ValorizacionPlazoCompra = registro.IdClasificacionTipoInstrumento.contains("RF") ? registro.MontoMercadoPactosC : registro.MontoMercadoSimulCom;
            custodia.CantidadPlazoVenta = (registro.IdClasificacionTipoInstrumento.contains("RF") || registro.IdClasificacionTipoInstrumento.contains("IF") )? registro.CantidadPactosV : BigDecimal.ZERO;
            custodia.ValorizacionPlazoVenta = (registro.IdClasificacionTipoInstrumento.contains("RF") || registro.IdClasificacionTipoInstrumento.contains("IF") )? registro.MontoMercadoPactosV : BigDecimal.ZERO;
            
            String cuentaContableAcciones = "110221";
            String cuentaContableBonos = "110222";
            String cuentaContableNegativos = "110250";
            
            boolean esBono = (registro.IdClasificacionTipoInstrumento.contains("RF") || registro.IdClasificacionTipoInstrumento.contains("IF"));
        
            if(esBono) {
            	custodia.ClasificacionInstrumentoCustodia = "110212";
				custodia.ClasificacionInstrumentoGarantia = cuentaContableBonos;
				custodia.ClasificacionInstrumentoPrestamo = cuentaContableBonos;
				custodia.ClasificacionInstrumentoPlazo = cuentaContableBonos; 
            } else {
            	custodia.ClasificacionInstrumentoCustodia = "110211";
                custodia.ClasificacionInstrumentoGarantia = cuentaContableAcciones;
                custodia.ClasificacionInstrumentoPrestamo = cuentaContableAcciones;
                custodia.ClasificacionInstrumentoPlazo = cuentaContableAcciones;
            }
            
            custodia.IdTipoInstrumentoPlazoCompra = (custodia.CantidadPlazoCompra.compareTo(BigDecimal.ZERO) > 0) ? "PCCV" : "";
            custodia.IdTipoInstrumentoPlazoVenta = (custodia.CantidadPlazoVenta.compareTo(BigDecimal.ZERO) > 0) ? "PVCC" : "";
            
            custodia.IdInstrumento = registro.IdInstrumento;
            custodia.PrecioMercadoGarantia = registro.PrecioMercadoGarantia;
            custodia.PrecioMercadoPrestamo = registro.PrecioMercadoPrestamo;
            custodia.PrecioMercadoPactosC = registro.PrecioMercadoPactosC;
            custodia.PrecioMercadoPactosV = registro.PrecioMercadoPactosV;
            custodia.PrecioMercadoSimulCom = registro.PrecioMercadoSimulCom;
            custodia.PrecioMercadoSimulVen = registro.PrecioMercadoSimulVen;
            custodia.TasaMercadoGarantia = registro.TasaMercadoGarantia;
            custodia.TasaMercadoPrestamo = registro.TasaMercadoPrestamo;
            custodia.TasaMercadoPactosC = registro.TasaMercadoPactosC;
            custodia.TasaMercadoPactosV = registro.TasaMercadoPactosV;
            custodia.TasaMercadoSimulCom = registro.TasaMercadoSimulCom;
            custodia.TasaMercadoSimulVen = registro.TasaMercadoSimulVen;
            
            custodias.add(custodia);

        }
        long fin = System.currentTimeMillis();
        logger.debug("FIN::obtenerCustodias", fin - inicio);
        return custodias;

	}

	private void insertarCargaCuadratura(Connection con, List<Custodia> custodias)  throws SQLException {
		logger.debug("INICIO::CargarArchivoCIRCULARCARTERACLIENTE.insertarCargaCuadratura");
		long inicio = System.currentTimeMillis();

		SQL _sql = new SQL();
			
 			_sql.batch(con, SQL_INSERT_CUADRATURA_CIRCULARCARTERACLIENTE, custodias, Custodia.class);

			_sql.batch(con, SQL_UPDATE_CIRCULARCARTERACLIENTE, custodias,Custodia.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarArchivoCIRCULARCARTERACLIENTE.insertarCargaCuadratura", fin - inicio);
	}
	
	  public static void main(String[] args) throws SQLException {
	        System.out.println("== TEST ==");
	    	try (Connection con = PlataformaUtils.getDESAConnection()) {
	            EntradaCargarArchivoCircularCarteraCliente entrada = new EntradaCargarArchivoCircularCarteraCliente();
	            entrada.p_IdEmpresa = 1L;
	            entrada.p_IdUsuario = 4L;
	            entrada.p_IdModulo = "CUS";
	            entrada.p_FechaCuadratura = "2023-12-29";

	            CargarArchivoCircularCarteraCliente cargarArchivo = new CargarArchivoCircularCarteraCliente();
	            try {
	                SalidaCargarArchivoCircularCarteraCliente salida = cargarArchivo.logica(con,entrada);
	                System.out.println(salida.toString());
	                con.commit();

				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				} finally {
					con.rollback();
				}

	    	}
			System.out.println("== END ==");
		}

	}