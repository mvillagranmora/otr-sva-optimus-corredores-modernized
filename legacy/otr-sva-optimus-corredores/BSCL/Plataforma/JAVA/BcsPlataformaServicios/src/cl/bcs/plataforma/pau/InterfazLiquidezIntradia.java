package cl.bcs.plataforma.pau;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.liq.ConsultarClearingFinanciero;
import cl.bcs.tesoreria.pojo.Camara;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import org.apache.commons.lang3.StringUtils;

public class InterfazLiquidezIntradia implements JavaCall {
	private static final Logger logger = LogManager.getLogger(InterfazLiquidezIntradia.class);
	
	private final String SQL_SELECT_MOVIMIENTOS_COMPROBANTES = loadResourceToString("cl/bcs/plataforma/pau/sql/InterfazLiquidezIntradia_SelectMovimientosComprobantes.sql");
	private final String SQL_SELECT_COMPRAS_PACTOS = loadResourceToString("cl/bcs/plataforma/pau/sql/InterfazLiquidezIntradia_SelectPactos.sql");
	private final String SQL_SELECT_APORTE_RESCATE_FFMM = loadResourceToString("cl/bcs/plataforma/pau/sql/InterfazLiquidezIntradia_SelectAportesRescateFFMM.sql");
	private final String SQL_SELECT_CARGOS_ABONOS = loadResourceToString("cl/bcs/plataforma/pau/sql/InterfazLiquidezIntradia_SelectCargosAbonos.sql");
	private final String SQL_SELECT_INGRESOS_EGRESOS = loadResourceToString("cl/bcs/plataforma/pau/sql/InterfazLiquidezIntradia_SelectIngresosEgresos.sql");
	private final String SQL_SELECT_DEPOSITOS_PLAZO = loadResourceToString("cl/bcs/plataforma/pau/sql/InterfazLiquidezIntradia_SelectDepositosAPlazo.sql");
	private final String SQL_SELECT_TELERENTA = loadResourceToString("cl/bcs/plataforma/pau/sql/InterfazLiquidezIntradia_SelectTelerenta.sql");
	private final String SQL_SELECT_CONCEPTOS = loadResourceToString("cl/bcs/plataforma/pau/sql/InterfazLiquidezIntradia_SelectConceptos.sql");
	
	private final String RutaInterfazLiquidezIntradia = "sftp/ILI/";

	public static class Cabecera {
		String CodigoRegistro = "01";
		String AreaOrigen = "02";
		String FechaProceso = "00010101";
		
		public Cabecera(String codigoRegistro, String areaOrigen, String fechaProceso) {
			super();
			CodigoRegistro = codigoRegistro;
			AreaOrigen = areaOrigen;
			FechaProceso = fechaProceso;
		}
		
		@Override
	    public String toString() {
	        return CodigoRegistro + AreaOrigen + FechaProceso;
	    }
	}
	
	public static class Total {
		public String CodigoRegistroTotal = "02";
		public Long TotalRegistros = 0L;
		public Long TotalML = 0L;
		public Long TotalMX = 0L;
		public BigDecimal MontoTotal = BigDecimal.ZERO;
		
		@Override
	    public String toString() {
	        return CodigoRegistroTotal + StringUtils.leftPad(TotalRegistros.toString(), 6, '0') +
	        		StringUtils.leftPad(TotalML.toString(), 6, '0') + 
	        		StringUtils.leftPad(TotalMX.toString(), 6, '0') + 
	        		StringUtils.leftPad(MontoTotal.setScale(4, RoundingMode.DOWN).toString().replace(".", ""), 23, '0');
	    }
	}
	
	public static class Concepto {
		public String CodigoOperacion = "02";
		public String Concepto = "0000";
		public String ConceptoPH = "0000";
	}
	
	public static class Cuerpo {
		public String CodigoRegistro = "99";
		public String Concepto;
		public String TipoMovimiento = "00";
		public String FolioOperacion = "0";
		public String RutCliente = "0";
		public String NombreCliente = "";
		public String BancoIntermediario = "";
		public String Monto;
		public BigDecimal MontoDecimal = new BigDecimal(0L);
		public String Moneda = "CLP";
		public String FechaLiquidacion;
		public String FormaPago = "01";
		public String TipoCliente = "04";
		public String Referenciado = "0";
		public String TipoMercado = "00";
		public String EstadoOperacion = "00";
		
//		@Override
	    public String format() {
	        return CodigoRegistro + Concepto + TipoMovimiento + StringUtils.leftPad(FolioOperacion, 12, '0') + 
	        		StringUtils.leftPad(RutCliente, 12, '0') + StringUtils.rightPad(NombreCliente, 50, ' ') + 
	        		StringUtils.leftPad(BancoIntermediario, 50, ' ') + StringUtils.leftPad(MontoDecimal.abs().setScale(4, RoundingMode.DOWN).toString().replace(".", ""), 23, '0') + 
	        		Moneda + FechaLiquidacion + FormaPago + TipoCliente + 
	        		StringUtils.leftPad(Referenciado, 12, '0') + TipoMercado + EstadoOperacion;
	    }
	}
	
	public static class EntradaInterfazLiquidezIntradia extends BcsPojo {
		public String p_FechaProceso;
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Manual;
		public String p_EnviarArchivoACasilla;
		
		public EntradaInterfazLiquidezIntradia() {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_Manual = "N";
			this.p_FechaProceso = "0001-01-01";
			this.p_EnviarArchivoACasilla = "N";
		}
	}
	
	public static class SalidaInterfazLiquidezIntradia extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public String P_OUT_RUTA_ARCHIVO;
		
		public SalidaInterfazLiquidezIntradia() {
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RUTA_ARCHIVO = "";
		}
	}
	
	public static class Archivo {
		public String Nombre;
		public byte[] Archivo;
		public String Estado;
		public String Contenido;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaInterfazLiquidezIntradia in = gson.fromJson(jin.toString(), EntradaInterfazLiquidezIntradia.class);
		SalidaInterfazLiquidezIntradia out = logica(conn, in, false);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
    
    public SalidaInterfazLiquidezIntradia logica(Connection con, EntradaInterfazLiquidezIntradia entrada, Boolean debug) throws Exception {
    	logger.info("INICIO::InterfazLiquidezIntradia.logica");
    	SalidaInterfazLiquidezIntradia out = new SalidaInterfazLiquidezIntradia();
    	long inicio = System.currentTimeMillis();
    	SQL sql = new SQL();
    	
    	try {
    		// Configuracion sistema
    		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
    		String ruta = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
    		
        	HashMap<String, Object> params = new HashMap<String, Object>();
        	params.put("FechaProceso", entrada.p_FechaProceso);
        	params.put("IdEmpresa", entrada.p_IdEmpresa);
        	
        	List<Cuerpo> listado = new ArrayList<>();
        	
        	listado.addAll(consultarClearingFinanciero(con, sql, entrada));
        	listado.addAll(obtenerComprobantesRVRFIF(con, sql, params));
        	listado.addAll(obtenerOperacionesMRF(con, sql, params, fechaSistema, entrada));
        	listado.addAll(obtenerCargosAbonos(con, sql, params));
        	listado.addAll(obtenerAportesRescatesFFMM(con, sql, params, fechaSistema, entrada));
        	listado.addAll(obtenerIngresosEgresos(con, sql, params));
        	listado.addAll(obtenerPalosTelerenta(con, sql, params));
        	listado.addAll(obtenerDepositosPlazo(con, sql, params));
        	
        	Archivo archivo = creaFormatoTXT(entrada.p_FechaProceso, listado, ruta);
        	
        	if (archivo.Estado.equalsIgnoreCase("NOOK")) {
        		out.P_OUT_ERRORESCONTADOR += 1L;
            	MensajeSalida msgSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_GUARDADO_ARCHIVO", new HashMap<String, String>(), con, entrada.p_IdEmpresa);
            	out.P_OUT_MENSAJES.add(msgSalida);
            	return out;
        	}
        	
        	HashMap<String, String> datos = new HashMap<String, String>();
        	datos.put("Archivo", archivo.Nombre);
        	
        	MensajeSalida msgSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_OK, "UI_EXITO_INTERFAZ_LIQUIDEZ_INTRADIA", datos, con, entrada.p_IdEmpresa);
        	out.P_OUT_MENSAJES.add(msgSalida);
        	out.P_OUT_RUTA_ARCHIVO = RutaInterfazLiquidezIntradia + archivo.Nombre;
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		out.P_OUT_ERRORESCONTADOR = 1L;
        	MensajeSalida msgSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_OK, "UI_ERROR_EJECUCION_INTERFAZ_LIQUIDEZ_INTRADIA", new HashMap<String, String>(), con, entrada.p_IdEmpresa);
        	out.P_OUT_MENSAJES.add(msgSalida);
    		logger.error("ERROR::InterfazLiquidezIntradia.logica - Exception: \n{}", ex);
    		throw ex;
    	}
    	
        long fin = System.currentTimeMillis();
    	logger.info("FIN::InterfazLiquidezIntradia.logica - TIEMPO: {}", fin - inicio);
    	return out;
    }
    
    private List<Cuerpo> obtenerComprobantesRVRFIF(Connection con, SQL sql, HashMap<String, Object> params) throws SQLException {
    	logger.info("INICIO::InterfazLiquidezIntradia.obtenerComprobantesRVRFIF");
    	long inicio = System.currentTimeMillis();
    	List<Cuerpo> data = sql.query(con, SQL_SELECT_MOVIMIENTOS_COMPROBANTES, params, Cuerpo.class);
    	long fin = System.currentTimeMillis();
    	logger.info("FIN::InterfazLiquidezIntradia.obtenerComprobantesRVRFIF - TOTAL OPERACIONES: {} - TIEMPO: {} ", data.size(),  fin - inicio);
    	return data;
    }
    
    private List<Cuerpo> obtenerOperacionesMRF(Connection con, SQL sql, HashMap<String, Object> params, String fechaSistema, EntradaInterfazLiquidezIntradia in) throws SQLException{
    	logger.info("INICIO::InterfazLiquidezIntradia.obtenerOperacionesMRF");
    	long inicio = System.currentTimeMillis();
    	List<Cuerpo> data = new ArrayList<>();
		data = sql.query(con, SQL_SELECT_COMPRAS_PACTOS, params, Cuerpo.class);

    	long fin = System.currentTimeMillis();
    	logger.info("FIN::InterfazLiquidezIntradia.obtenerOperacionesMRF - TOTAL OPERACIONES: {} - TIEMPO: {} ", data.size(),  fin - inicio);
    	return data;
    }
    
    private List<Cuerpo> obtenerCargosAbonos(Connection con, SQL sql, HashMap<String, Object> params) throws SQLException {
    	logger.info("INICIO::InterfazLiquidezIntradia.obtenerCargosAbonos");
    	long inicio = System.currentTimeMillis();
    	List<Cuerpo> data = sql.query(con, SQL_SELECT_CARGOS_ABONOS, params, Cuerpo.class);
    	long fin = System.currentTimeMillis();
    	logger.info("FIN::InterfazLiquidezIntradia.obtenerCargosAbonos - TOTAL OPERACIONES: {} - TIEMPO: {} ", data.size(),  fin - inicio);
    	return data;
    }
    
    private List<Cuerpo> obtenerIngresosEgresos(Connection con, SQL sql, HashMap<String, Object> params) throws SQLException {
    	logger.info("INICIO::InterfazLiquidezIntradia.obtenerIngresosEgresos");
    	long inicio = System.currentTimeMillis();
    	List<Cuerpo> data = sql.query(con, SQL_SELECT_INGRESOS_EGRESOS, params, Cuerpo.class);
    	long fin = System.currentTimeMillis();
    	logger.info("FIN::InterfazLiquidezIntradia.obtenerIngresosEgresos - TOTAL OPERACIONES: {} - TIEMPO: {} ", data.size(),  fin - inicio);
    	return data;
    }
    
    private List<Cuerpo> obtenerDepositosPlazo(Connection con, SQL sql, HashMap<String, Object> params) throws SQLException {
    	logger.info("INICIO::InterfazLiquidezIntradia.obtenerDepositosPlazo");
    	long inicio = System.currentTimeMillis();
    	List<Cuerpo> data = sql.query(con, SQL_SELECT_DEPOSITOS_PLAZO, params, Cuerpo.class);
    	long fin = System.currentTimeMillis();
    	logger.info("FIN::InterfazLiquidezIntradia.obtenerDepositosPlazo - TOTAL OPERACIONES: {} - TIEMPO: {} ", data.size(),  fin - inicio);
    	return data;
    }
    
    private List<Cuerpo> obtenerPalosTelerenta(Connection con, SQL sql, HashMap<String, Object> params) throws SQLException {
    	logger.info("INICIO::InterfazLiquidezIntradia.obtenerPalosTelerenta");
    	long inicio = System.currentTimeMillis();
    	List<Cuerpo> data = new ArrayList<>();
    	data = sql.query(con, SQL_SELECT_TELERENTA, params, Cuerpo.class);
    	long fin = System.currentTimeMillis();
    	logger.info("FIN::InterfazLiquidezIntradia.obtenerPalosTelerenta - TOTAL OPERACIONES: {} - TIEMPO: {} ", data.size(),  fin - inicio);
    	return data;
    }
    
    private List<Cuerpo> obtenerAportesRescatesFFMM(Connection con, SQL sql, HashMap<String, Object> params, String fechaSistema, EntradaInterfazLiquidezIntradia in) throws SQLException {
    	logger.info("INICIO::InterfazLiquidezIntradia.ObtenerAportesRescatesFFMM");
    	long inicio = System.currentTimeMillis();
    	List<Cuerpo> data = new ArrayList<>();
		data = sql.query(con, SQL_SELECT_APORTE_RESCATE_FFMM, params, Cuerpo.class);
    	long fin = System.currentTimeMillis();
    	logger.info("FIN::InterfazLiquidezIntradia.ObtenerAportesRescatesFFMM - TOTAL OPERACIONES: {} - TIEMPO: {} ", data.size(),  fin - inicio);
    	return data;
    }
    
    private List<Cuerpo> consultarClearingFinanciero(Connection con, SQL sql, EntradaInterfazLiquidezIntradia entrada) throws SQLException {
    	logger.info("INICIO::InterfazLiquidezIntradia.consultarClearingFinanciero");
    	long inicio = System.currentTimeMillis();
    	List<Cuerpo> data = new ArrayList<>();
    	
    	ConsultarClearingFinanciero consulta = new ConsultarClearingFinanciero();
    	HashMap<String, Object> params = new HashMap<>();
    	List<String> conceptosCodigos = new ArrayList<String>();
    	conceptosCodigos.add("RV");
    	conceptosCodigos.add("PH+SM");
    	params.put("IdConceptos", conceptosCodigos);
    	List<Concepto> conceptos = sql.query(con, SQL_SELECT_CONCEPTOS, params, Concepto.class);
    	
    	Map<Object, List<Concepto>> hashCuerpo = conceptos.stream().collect(Collectors.groupingBy(obj -> obj.CodigoOperacion));
    	
    	ConsultarClearingFinanciero.EntradaClearingFinanciero in = new ConsultarClearingFinanciero.EntradaClearingFinanciero();
    	Camara camaraRV = new Camara();
    	Camara camaraPH = new Camara();
    	Camara camaraSM = new Camara();
    	camaraRV.Camara = "RV";
    	camaraSM.Camara = "SM";
    	camaraPH.Camara = "PH";
    	
    	in.p_Camaras = Arrays.asList(camaraRV, camaraSM, camaraPH);
    	in.p_IdModulo = "TES";
    	in.p_FechaLiquidacion = entrada.p_FechaProceso;
    	in.p_IdEmpresa = entrada.p_IdEmpresa;
    	in.p_UsaReglasNegocio = "S";
    	
    	LocalDate fecha = LocalDate.parse(entrada.p_FechaProceso);
        String fechaEnNuevoFormato = fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    	
    	ConsultarClearingFinanciero.SalidaClearingFinanciero out = consulta.logica(con, in);
    	
    	data = out.P_OUT_CLEARINGFINANCIERORESUMEN.stream()
    	        .collect(Collectors.groupingBy(
    	                obj -> obj.NombreGrupoCamara,
    	                Collectors.collectingAndThen(
    	                        Collectors.toList(),
    	                        list -> {
    	                            BigDecimal saldoAcreedorTotal = list.stream()
    	                                    .map(obj -> obj.SaldoAcreedor != null ? obj.SaldoAcreedor : BigDecimal.ZERO)
    	                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

    	                            BigDecimal saldoDeudorTotal = list.stream()
    	                                    .map(obj -> obj.SaldoDeudor != null ? obj.SaldoDeudor : BigDecimal.ZERO)
    	                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

    	                            Cuerpo resultado = new Cuerpo();
    	                            resultado.MontoDecimal = saldoAcreedorTotal.subtract(saldoDeudorTotal).abs();
    	                            String nombreCamara = list.get(0).NombreGrupoCamara.replace(" ", "");
    	                            resultado.Concepto = hashCuerpo.get(nombreCamara).get(0).Concepto;
    	                            resultado.TipoMovimiento = saldoAcreedorTotal.compareTo(saldoDeudorTotal) == 1 ? "01" : "02";
    	                            resultado.FechaLiquidacion = fechaEnNuevoFormato;
    	                            return resultado;
    	                        }
    	                )
    	        ))
    	        .values().stream()
    	        .collect(Collectors.toList());
    	
    	long fin = System.currentTimeMillis();
    	logger.info("FIN::InterfazLiquidezIntradia.consultarClearingFinanciero - TOTAL OPERACIONES: {} - TIEMPO: {} ", data.size(),  fin - inicio);
    	return data;
    }
    
    
    private Archivo creaFormatoTXT(String fechaProceso, List<Cuerpo> listado, String ruta) {
    	logger.info("INICIO::InterfazLiquidezIntradia.creaArchivoTXT");
    	long inicio = System.currentTimeMillis();
    	
    	// SE CREA RUTA ARCHIVO SI NO EXISTE
		File folder = new File(ruta + RutaInterfazLiquidezIntradia);
		if(!folder.exists()){
			folder.mkdirs();
		}
		
    	Archivo archivo = new Archivo();
    	
    	LocalDate fecha = LocalDate.parse(fechaProceso);
        String fechaEnNuevoFormato = fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    	
    	String nombreArchivo = "LI_CDB_" + fechaEnNuevoFormato + ".txt";
        
        StringBuilder format = new StringBuilder();
    	Cabecera cabecera = new Cabecera("01", "02", fechaEnNuevoFormato);
        format.append(cabecera + "\n");
        
        Total total = new Total();
        total.TotalRegistros = (long) listado.size();
        total.TotalML = (long) listado.size();
        total.TotalMX = 0L;
        
        BigDecimal montoTotal = BigDecimal.ZERO;

        for (Cuerpo info : listado) {
        	montoTotal = montoTotal.add(info.MontoDecimal.abs());
        	format.append(info.format() + "\n");
        }
        
        total.MontoTotal = montoTotal;
        
        format.append(total + "\n");
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream)) {
            writer.write(format.toString());
        } catch (IOException e) {
        	archivo.Estado = "NOOK";
            e.printStackTrace();
        	long fin = System.currentTimeMillis();
            logger.info("ERROR::InterfazLiquidezIntradia.creaArchivoTXT {} - TIEMPO: {}", e, fin - inicio);
            return archivo;
        }
        
        archivo.Nombre = nombreArchivo;
        archivo.Contenido = format.toString();
        archivo.Estado = "OK";
        archivo.Archivo = byteArrayOutputStream.toByteArray();
        
    	String rutaFinal = ruta + RutaInterfazLiquidezIntradia + nombreArchivo;
        try (FileOutputStream fileOutputStream = new FileOutputStream(rutaFinal)) {
        	logger.info("INFO::InterfazLiquidezIntradia.creaArchivoTXT - Se agrega el Archivo en la Ruta: "+ rutaFinal);
            byteArrayOutputStream.writeTo(fileOutputStream);
        } catch (IOException e) {
        	archivo.Estado = "NOOK";
            e.printStackTrace();
            long fin = System.currentTimeMillis();
            logger.info("ERROR::InterfazLiquidezIntradia.creaArchivoTXT {} - TIEMPO: {}", e, fin - inicio);
            return archivo;
        }
        
    	long fin = System.currentTimeMillis();
    	logger.info("FIN::InterfazLiquidezIntradia.creaArchivoTXT - TIEMPO: {}", fin - inicio);
    	return archivo;
    }
    
    public static void main(String[] args) throws IOException { 
    	System.out.println("== INICIO PRUEBA ==");
    	
        try (Connection con = PlataformaUtils.getPropertiesConnection("desa02")){
        	InterfazLiquidezIntradia demo = new InterfazLiquidezIntradia();
        	EntradaInterfazLiquidezIntradia entrada = new EntradaInterfazLiquidezIntradia();
        	entrada.p_FechaProceso = "2025-02-18";
        	entrada.p_IdEmpresa = 1L;
        	entrada.p_EnviarArchivoACasilla = "S";
        	entrada.p_Manual = "S";
        	SalidaInterfazLiquidezIntradia out = demo.logica(con, entrada, true);
        	System.out.print(out);
        	con.rollback();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        System.out.println("== FIN PRUEBA ==");
    }
}
