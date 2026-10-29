package cl.bcs.cuentaInversion;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoInicialFinal;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.mer.CalcularParidad;
import  cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class GenerarArchivoCuentasPorCobrarAnexo16 implements JavaCall {

    private static final Logger logger = LogManager.getLogger(GenerarArchivoCuentasPorCobrarAnexo16.class);
    private static final Date _fechaSegura = Date.valueOf(LocalDate.of(1900, 1, 1));
    
    private Date _fechaConsulta = null;
    private GenerarArchivoCuentasPorCobrarAnexo16_Entrada _in = null;
    private GenerarArchivoCuentasPorCobrarAnexo16_Salida _out = null;
    private SQL _sql = null;
    private String _monedaSistema = "";
    private String _fechaDesde 	  = "";

    // Tamos de deuda
    private static final int DIAS_VIGENTE = 2;
    private static final int DIAS_SEGUNDO_TRAMO = 10;
    private static final int DIAS_TERCER_TRAMO = 30;
    private static final String ID_MONEDA_CONSULTAR = null; // CLP, USD, etc null para todas

    private final String SQL_INSERT_ID_CUENTA_INVERSION		    = loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarArchivoCuentasPorCobrarAnexo16_InsertCuentasInversion.sql");
    private final String SQL_SELECT_CARGOS_ABONOS_CTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarArchivoCuentasPorCobrarAnexo16_SelectCargosAbonosCtaInversion.sql");
    private final String SQL_SELECT_RELACIONES 			    	= loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarArchivoCuentasPorCobrarAnexo16_SelectRelacionesClientes.sql");
    private final String SQL_SELECT_CONFIGURACION               = loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarArchivoCuentasPorCobrarAnexo16_SelectConfiguracion.sql");  
    private final String SQL_LOGO_ANEXO16						= loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarArchivoCuentasPorCobrarAnexo16_SelectLogoCorredora.sql");

    public static class GenerarArchivoCuentasPorCobrarAnexo16_Entrada extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_FechaHasta;
    }

    public static class GenerarArchivoCuentasPorCobrarAnexo16_Salida extends BcsPojo {
        public List <MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_RUTAARCHIVO;

        public GenerarArchivoCuentasPorCobrarAnexo16_Salida() {
        	this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_MENSAJES		   = new ArrayList<MensajeSalida>();
        }
    }

    public static class Configuracion extends BcsPojo{
		public String			RutaBase;
		public String			RutaCta;
		public String			RutaArchivo;
	}
    
    public static class RutasArchivoAnexo extends BcsPojo{
    	public String 			RutaFisica;
    	public String			RutaDescarga;
    }
	

    public static class CuentaPorCobrarAnexo16 extends BcsPojo {
        public Long 			IdTrabajo = 0L;
        public Long 			IdCuentaInversion;
        public String			CodigoCuentaInversion;
        public String 			DocumentoIdentidad;
        public String 			NombreCliente;
        public String			DescripcionRelacion;
        public String 			Relacion;
        public Long 			IdCliente;
        public String 			FechaSaldo;
        public Date				FechaUltimoMovimiento;
        public BigDecimal		SaldoEfectivo;
        public SaldosPorTramo	SaldosPorTramo;
        public String 			IdMoneda;
        public BigDecimal		SaldoEfectivoInicial;
        public BigDecimal 		SaldoEfectivoFinal;

        public CuentaPorCobrarAnexo16() {
            this.IdTrabajo		= 0L;
            this.SaldoEfectivo  = BigDecimal.ZERO; 
            this.SaldosPorTramo = new SaldosPorTramo();
        }
    }
    
    public static class SaldosPorTramo extends BcsPojo {
        public BigDecimal DeudaVigente; 					// Hasta 2 dias   (x < 3)
        public BigDecimal DeudaSegundoTramo; 				// Hasta 10 dias  (3 => x < 11) 
        public BigDecimal DeudaTercerTramo; 				// Hasta 30 dias  (11 => x <= 30)
        public BigDecimal DeudaCuartoTramo; 				// Mas de 30 dias ( x > 30) 
        public BigDecimal Saldo;

        public SaldosPorTramo() {
            this.Saldo 				= BigDecimal.ZERO;
            this.DeudaVigente		= BigDecimal.ZERO;
            this.DeudaSegundoTramo	= BigDecimal.ZERO;
            this.DeudaTercerTramo 	= BigDecimal.ZERO;
            this.DeudaCuartoTramo 	= BigDecimal.ZERO;
        }

        public BigDecimal getTotalDeuda() {
            return this.DeudaVigente.add(this.DeudaSegundoTramo)
            		.add(this.DeudaTercerTramo)
            		.add(this.DeudaCuartoTramo);
        }

        public BigInteger getDeudaVigente(){
            return this.getBigInteger(this.DeudaVigente);
        }

        public BigInteger getDeudaSegundoTramo(){
            return this.getBigInteger(this.DeudaSegundoTramo);
        }

        public BigInteger getDeudaTercerTramo(){
            return this.getBigInteger(this.DeudaTercerTramo);
        }

        public BigInteger getDeudaCuartoTramo(){
            return this.getBigInteger(this.DeudaCuartoTramo);
        }

        public BigInteger getSaldo(){
            return this.getBigInteger(this.Saldo);
        }

        private BigInteger getBigInteger(BigDecimal value) {
            return value.setScale(0, RoundingMode.DOWN).toBigIntegerExact();
        }
    }
    
    
    private static class SaldoTramo extends BcsPojo {
    	final LocalDate fechaEfectivaMovimiento;
    	BigDecimal saldoTramo;
    	
    	SaldoTramo(LocalDate fecha, BigDecimal monto){
    		this.fechaEfectivaMovimiento = fecha;
		    this.saldoTramo 			 = monto;
    	}
    }

    public static class CargosAbonosCtaInversion extends BcsPojo {
        public Long			 IdTrabajo;
        public Long 		 IdCargoAbonoMovimiento;
        public String 		 IdTipoMovimiento;
        public String 		 IdConcepto;
        public Long			 IdCuentaInversion;
        public Long			 IdCliente;
        public String		 DocumentoIdentidadCliente;
        public Long 	     IdPortafolio;
        public String 	     CodigoPortafolio;
        public String 		 Proposito;
        public String		 IdMoneda;
        public String		 IdCondicionDeLiquidacion;
        public BigDecimal	 Monto;
        public String 	     IdEstado;
        public BigDecimal    SaldoPorFechaMovimiento;
        public Date 		 FechaSistema;
        public String 		 EsCargoAbonoPorTraspaso;
        public String 		 IdModulo;
        public Date 		 FechaEfectivaMovimiento;
        public Timestamp 	 FechaIngresoRegistro;
        public BigDecimal 	 SaldoEfectivoInicio;
        public BigDecimal	 SaldoEFectivoFinal;

        public CargosAbonosCtaInversion() {
            this.IdTrabajo 					= 0L;
            this.Monto						= BigDecimal.ZERO;
            this.SaldoPorFechaMovimiento 	= BigDecimal.ZERO;
            this.SaldoEfectivoInicio		= BigDecimal.ZERO;
            this.SaldoEFectivoFinal			= BigDecimal.ZERO;
        }
    }

    public static class CuentaInversion extends BcsPojo {
        public Long IdTrabajo;
        public Long IdCuentaInversion;

        public CuentaInversion() {
            this.IdTrabajo = 0L;
        }
    }
    
    public static class LogoCorredora extends BcsPojo {
		public byte[] imagen;
	}
    
      public static class Relacion extends BcsPojo {
    	public Long  IdCliente;
    	public String Relacionado;
    	public String Traduccion;
    }
    

    public GenerarArchivoCuentasPorCobrarAnexo16_Salida logica(
		Connection conn,
		GenerarArchivoCuentasPorCobrarAnexo16_Entrada in
	) throws Exception {

        logger.info("INICIO::GenerarArchivoCuentasPorCobrarAnexo16");
        long startTime = System.currentTimeMillis();
        this._in = in;
        this._out = new GenerarArchivoCuentasPorCobrarAnexo16_Salida();
        this._fechaConsulta = this.convertirFecha(in.p_FechaHasta);
        this._sql = new SQL();
        
		this._monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, this._in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		this._fechaDesde = Date.valueOf(_fechaConsulta.toLocalDate().minusDays(DIAS_TERCER_TRAMO)).toString();
		
        // Consultamos los saldos negativos
        SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuenta = this.obtenerSaldoCuentaInversion(conn, null, this._in.p_FechaHasta, this._in.p_FechaHasta);
        List<CuentaPorCobrarAnexo16> listadoCuentasPorCobrar = new ArrayList<>();
        this.filtrarCuentasPorCobrar(conn, salidaConsultarSaldoCuenta, listadoCuentasPorCobrar);
        
        if(this._out.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0) {
        	return this._out;
        }
                
        // Consultamos los cargos y abonos recientes -30 dias desde la consulta y clasificamos.
        List<CargosAbonosCtaInversion> cargosAbonos = this.obtenerCargosAbonosCuentaInversion(conn, listadoCuentasPorCobrar);
        List<SaldoInicialFinal> saldosIniciales = this.obtenerSaldosInicialesCuentaInversion(conn, cargosAbonos);
        List<CuentaPorCobrarAnexo16> cuentasConMovimientoReciente = this.clasificarTramosDeuda(cargosAbonos, saldosIniciales);
        
        //Agrupar listadoCuentasPorCobrar + cuentasConMovimientoReciente || cuenta.IdCliente + "_" + cuenta.IdCuentaInversion + "_" + cuenta.IdMoneda
        List<CuentaPorCobrarAnexo16> listadoCuentasPorCobrarAgrupadas = this.agruparCuentasIdCuentaInversion(listadoCuentasPorCobrar, cuentasConMovimientoReciente);
        this.calcularParidadMonedas(conn, listadoCuentasPorCobrarAgrupadas);
        List<Relacion> relaciones = this.obtenerRelaciones(conn, listadoCuentasPorCobrarAgrupadas);

        // Agrupamos cuentas por IdCliente y asignamos relaciones
        List<CuentaPorCobrarAnexo16> listadoIdCliente = this.agruparCuentasPorCliente(listadoCuentasPorCobrarAgrupadas, relaciones);
        
        // Obtener rutas de guardado de archivos
        RutasArchivoAnexo rutas	= this.obtenerRutasArchivo(conn);
        
        if(this._out.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0) {
        	return this._out;
        }
        // Generamos el archivo
 		byte[] excelBytes = this.generarExcel(conn, listadoIdCliente);
 		byte[] pdfBytes   = this.generarPdf(conn, listadoIdCliente);
		this.guardarZip(excelBytes, pdfBytes, rutas.RutaFisica);
        this._out.P_OUT_RUTAARCHIVO = rutas.RutaDescarga;

        long endTime = System.currentTimeMillis();
        logger.info("FIN::GenerarArchivoCuentasPorCobrarAnexo16 - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        return this._out;
    }

	
    private List<SaldoInicialFinal> obtenerSaldosInicialesCuentaInversion(
    		Connection conn,
			List<CargosAbonosCtaInversion> cargosAbonos
        ) throws Exception {

        logger.debug("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.obtenerSaldosInicialesCuentaInversion");
        long startTime = System.currentTimeMillis();

        List<InCuentaInversionConsultar> lstCuentaInversionConsultar =
        	    cargosAbonos.stream()
        	        .map(cargoAbono -> cargoAbono.IdPortafolio)
        	        .distinct()
        	        .map(idPortafolio -> {
        	            InCuentaInversionConsultar cuenta = new InCuentaInversionConsultar();
        	            cuenta.IdEjecutivo = 0L;
        	            cuenta.IdMoneda = ID_MONEDA_CONSULTAR;
        	            cuenta.IdPortafolio = idPortafolio;
        	            return cuenta;
        	        })
        	        .collect(Collectors.toList());
        SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuenta = this.obtenerSaldoCuentaInversion(conn, lstCuentaInversionConsultar, this._fechaDesde, this._in.p_FechaHasta);
        long endTime = System.currentTimeMillis();
        logger.info("FIN::GenerarArchivoCuentasPorCobrarAnexo16.obtenerSaldosInicialesCuentaInversion - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
		return salidaConsultarSaldoCuenta.P_OUT_SALDOINICIALFINAL;
	}


	private SalidaConsultarSaldoCuentaInversion obtenerSaldoCuentaInversion(
        Connection conn,
        List<InCuentaInversionConsultar> cuentasInvConsultar,
        String fechaDesde,
        String fechaHasta
    ) throws Exception {

        logger.debug("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.obtenerSaldoCuentaInversion");
        long startTime = System.currentTimeMillis();

        ConsultarSaldoCuentaInversion servicioConsultarSaldoCuenta = new ConsultarSaldoCuentaInversion();
        EntradaConsultarSaldoCuentaInversion entradaConsultarSaldoCuenta = new EntradaConsultarSaldoCuentaInversion();

        entradaConsultarSaldoCuenta.p_IdEmpresa = this._in.p_IdEmpresa;
        entradaConsultarSaldoCuenta.p_IdUsuario = this._in.p_IdUsuario;
        entradaConsultarSaldoCuenta.p_FechaDesde = fechaDesde;
        entradaConsultarSaldoCuenta.p_FechaHasta = fechaHasta;
        entradaConsultarSaldoCuenta.p_IdModulo = "CTA";
        entradaConsultarSaldoCuenta.p_UsaReglasNegocio = "S";
        entradaConsultarSaldoCuenta.p_IncluyeOtrosSaldos = "S";
        entradaConsultarSaldoCuenta.p_SoloSaldoNegativo = "S";
        entradaConsultarSaldoCuenta.p_SoloSaldoDisponible = "N";
        entradaConsultarSaldoCuenta.p_SoloSaldoDisponibleNegativo = "N";
        entradaConsultarSaldoCuenta.p_IdEjecutivo = 0L;
        entradaConsultarSaldoCuenta.p_IdMoneda = ID_MONEDA_CONSULTAR;
        entradaConsultarSaldoCuenta.p_IncluyeProductoApv = "N";
        entradaConsultarSaldoCuenta.p_IdCliente = null;
        entradaConsultarSaldoCuenta.p_IdPortafolio = null;
        entradaConsultarSaldoCuenta.p_IdProductoAgrupador = null;

        if(cuentasInvConsultar == null || cuentasInvConsultar.isEmpty()) {
        	List<InCuentaInversionConsultar> lstCuentaInversionConsultar = new ArrayList<InCuentaInversionConsultar>();
            InCuentaInversionConsultar objCuentaInversionConsultar = new InCuentaInversionConsultar();

            objCuentaInversionConsultar.IdEjecutivo = 0L;
            objCuentaInversionConsultar.IdMoneda = ID_MONEDA_CONSULTAR;
            objCuentaInversionConsultar.IdPortafolio = null;

            lstCuentaInversionConsultar.add(objCuentaInversionConsultar);
            cuentasInvConsultar = lstCuentaInversionConsultar;
        }
        
        entradaConsultarSaldoCuenta.p_CuentaInversionAConsultar = cuentasInvConsultar;

        SalidaConsultarSaldoCuentaInversion out = servicioConsultarSaldoCuenta.logica(conn, entradaConsultarSaldoCuenta);
        long endTime = System.currentTimeMillis();
        logger.debug("FIN::GenerarArchivoCuentasPorCobrarAnexo16.obtenerSaldoCuentaInversion - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        logger.trace("Cantidad de cuentas negativas: {}", out.P_OUT_SALDOPORFECHAEFECTIVA.size());

        return out;

    }
    

	private void filtrarCuentasPorCobrar(
        Connection conn,
        SalidaConsultarSaldoCuentaInversion saldoCuentaInversion,
        List<CuentaPorCobrarAnexo16> listadoCuentasPorCobrar
    ) throws Exception {

        logger.trace("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.filtrarCuentasPorCobrar");
        long startTime = System.currentTimeMillis();

        if (saldoCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA == null || saldoCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty()) {
            this.agregarMensajeError("NO_EXISTEN_MOVIMIENTOS_PARA_GENERAR_ARCHIVO", conn);
            logger.trace("FIN::GenerarArchivoCuentasPorCobrarAnexo16.filtrarCuentasPorCobrar - No hay cuentas por cobrar para procesar");
            return;
        }
        Map<Long , SaldoInicialFinal> mapSaldosInicialFinal = new HashMap<>();
        saldoCuentaInversion.P_OUT_SALDOINICIALFINAL.stream()
            .forEach(saldo -> mapSaldosInicialFinal.put(saldo.IdCuentaInversion, saldo));


        for (SaldoPorFechaEfectiva saldo: saldoCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA) {
            if ((saldo.SaldoDisponible).compareTo(BigDecimal.ZERO) >= 0 || "CPRO".equals(saldo.Relacion)) {
                continue;
            }
            CuentaPorCobrarAnexo16 cuenta = new CuentaPorCobrarAnexo16();
            cuenta.IdCuentaInversion = saldo.IdCuentaInversion;
            cuenta.CodigoCuentaInversion = saldo.CodigoCuentaInversion;
            cuenta.IdCliente = saldo.IdCliente;
            cuenta.DocumentoIdentidad = saldo.DocumentoIdentidad;
            cuenta.NombreCliente = saldo.NombreCliente;
            cuenta.FechaSaldo = saldo.FechaSaldo;
            cuenta.Relacion = saldo.Relacion;
            cuenta.FechaUltimoMovimiento = this.convertirFecha(saldo.FechaUltimoMovimiento);
            cuenta.SaldoEfectivo = saldo.SaldoDisponible;
            cuenta.IdMoneda = saldo.IdMoneda;
            if (mapSaldosInicialFinal.containsKey(saldo.IdCuentaInversion)) {
            	SaldoInicialFinal saldoInicialFinal = mapSaldosInicialFinal.get(saldo.IdCuentaInversion);
                cuenta.SaldoEfectivoInicial = saldoInicialFinal.SaldoEfectivoInicial;
                cuenta.SaldoEfectivoFinal = saldoInicialFinal.SaldoEfectivoFinal;
            } else {
                cuenta.SaldoEfectivoInicial = BigDecimal.ZERO;
                cuenta.SaldoEfectivoFinal = BigDecimal.ZERO;
            }
            listadoCuentasPorCobrar.add(cuenta);
        }

        long endTime = System.currentTimeMillis();
        logger.trace("FIN::GenerarArchivoCuentasPorCobrarAnexo16.filtrarCuentasPorCobrar - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        logger.trace("Cantidad de cuentas por cobrar filtradas: {}", listadoCuentasPorCobrar.size());
    }


    private List<CargosAbonosCtaInversion> obtenerCargosAbonosCuentaInversion(
        Connection conn,
        List<CuentaPorCobrarAnexo16> listadoCuentasPorCobrar
    ) throws Exception {

        logger.debug("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.obtenerCargosAbonosCuentaInversion");
        long startTime = System.currentTimeMillis();
        // Consultamos los Cargos y abonos de las cta inversion dentro de los 30 dias
        Long idTrabajo = this.obtenerIdTrabajo();
        List<CuentaInversion> cuentasInversionConsultar = listadoCuentasPorCobrar.stream()
			.filter(cuenta -> (long) Utils.diferenciaEnDias(this._fechaConsulta, cuenta.FechaUltimoMovimiento) <= DIAS_TERCER_TRAMO)
            .map(cuenta -> {
                CuentaInversion cuentaInversion = new CuentaInversion();
                cuentaInversion.IdTrabajo = idTrabajo;
                cuentaInversion.IdCuentaInversion = cuenta.IdCuentaInversion;
                return cuentaInversion;
            })
            .collect(Collectors.toList());
        
        this._sql.batch(conn, SQL_INSERT_ID_CUENTA_INVERSION, cuentasInversionConsultar, CuentaInversion.class);
        
        
        // Obtenemos los cargos y abonos de los ultimos 32 dias
        HashMap <String, Object> params = new HashMap<String, Object>();
        Date fechaDesde =  Date.valueOf(_fechaConsulta.toLocalDate().minusDays(DIAS_TERCER_TRAMO));
        
		params.put("fechaDesde", fechaDesde);
		params.put("fechaHasta", this._fechaConsulta);
		params.put("idTrabajo", idTrabajo);
        List<CargosAbonosCtaInversion> cargosAbonosCtaInversion = this._sql.query(conn, SQL_SELECT_CARGOS_ABONOS_CTA_INVERSION, params, CargosAbonosCtaInversion.class);
        
        long endTime = System.currentTimeMillis();
        logger.debug("FIN::GenerarArchivoCuentasPorCobrarAnexo16.obtenerCargosAbonosCuentaInversion - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        logger.debug("Cantidad de cargos y abonos obtenidos: {}", cargosAbonosCtaInversion.size());
        
        return cargosAbonosCtaInversion;
    }


    private List<CuentaPorCobrarAnexo16> clasificarTramosDeuda(
            List<CargosAbonosCtaInversion> cargosAbonos,
            List<SaldoInicialFinal> saldosIniciales
    ) {
        logger.trace("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.clasificarTramosDeuda");
        long startTime = System.currentTimeMillis();

        Map<Long, SaldoInicialFinal> mapSaldos = saldosIniciales.stream()
            .collect(Collectors.toMap(
                saldo -> saldo.IdCuentaInversion,
                saldo -> saldo
            ));

        Map<String, List<CargosAbonosCtaInversion>> porClienteCuentaMoneda =
            cargosAbonos.stream()
            .collect(Collectors.groupingBy(c ->
                c.IdCliente + "_" + c.IdCuentaInversion + "_" + c.IdMoneda
            ));

        List<CuentaPorCobrarAnexo16> listadoCuentasClasificadas = new ArrayList<>();

        for (Map.Entry<String, List<CargosAbonosCtaInversion>> entry : porClienteCuentaMoneda.entrySet()) {
            List<CargosAbonosCtaInversion> lista = entry.getValue();
            if (lista == null || lista.isEmpty()) continue;

            lista.sort(Comparator
                .comparing((CargosAbonosCtaInversion c) -> c.FechaEfectivaMovimiento)
                .thenComparingLong(c -> c.IdCargoAbonoMovimiento)
            );

            CargosAbonosCtaInversion primero = lista.get(0);
            SaldoInicialFinal sif = mapSaldos.get(primero.IdCuentaInversion);
            BigDecimal saldoEfectivoInicial = (sif != null && sif.SaldoEfectivoInicial != null)
                    ? sif.SaldoEfectivoInicial
                    : BigDecimal.ZERO;

            List<SaldoTramo> tramos = new ArrayList<>();
            int cabecera = 0;

            BigDecimal deudaAnterior;
            if (saldoEfectivoInicial.compareTo(BigDecimal.ZERO) < 0) {
                BigDecimal deudaInicial = saldoEfectivoInicial.abs();
                LocalDate fechaVieja = this._fechaConsulta.toLocalDate().minusDays(DIAS_TERCER_TRAMO + 10);
                tramos.add(new SaldoTramo(fechaVieja, deudaInicial));
                deudaAnterior = deudaInicial;
            } else {
                deudaAnterior = BigDecimal.ZERO;
            }

            for (CargosAbonosCtaInversion mov : lista) {
                BigDecimal delta = clasificaCargoAbono(mov);
                mov.SaldoEfectivoInicio = saldoEfectivoInicial;
                saldoEfectivoInicial = saldoEfectivoInicial.add(delta);
                mov.SaldoEFectivoFinal = saldoEfectivoInicial;

                BigDecimal deudaActual = (saldoEfectivoInicial.compareTo(BigDecimal.ZERO) < 0)
                        ? saldoEfectivoInicial.abs()
                        : BigDecimal.ZERO;
                BigDecimal variacion = deudaActual.subtract(deudaAnterior);

                if (variacion.compareTo(BigDecimal.ZERO) > 0) {
                    tramos.add(new SaldoTramo(mov.FechaEfectivaMovimiento.toLocalDate(), variacion));
                } else if (variacion.compareTo(BigDecimal.ZERO) < 0) {
                    BigDecimal porPagar = variacion.abs();
                    while (porPagar.compareTo(BigDecimal.ZERO) > 0 && cabecera < tramos.size()) {
                        SaldoTramo saldo = tramos.get(cabecera);
                        if (saldo.saldoTramo.compareTo(porPagar) > 0) {
                        	saldo.saldoTramo = saldo.saldoTramo.subtract(porPagar);
                            porPagar = BigDecimal.ZERO;
                        } else {
                            porPagar = porPagar.subtract(saldo.saldoTramo);
                            cabecera++;
                        }
                    }
                }

                deudaAnterior = deudaActual;
            }

            BigDecimal deudaAlCierre = (saldoEfectivoInicial.compareTo(BigDecimal.ZERO) < 0)
                    ? saldoEfectivoInicial.abs()
                    : BigDecimal.ZERO;

            BigDecimal sumaTramos = BigDecimal.ZERO;
            for (int i = cabecera; i < tramos.size(); i++) {
                SaldoTramo t = tramos.get(i);
                if (t.saldoTramo != null) sumaTramos = sumaTramos.add(t.saldoTramo);
            }

            if (deudaAlCierre.compareTo(sumaTramos) > 0) {
                BigDecimal faltante = deudaAlCierre.subtract(sumaTramos);
                LocalDate fechaUltMov = lista.get(lista.size() - 1).FechaEfectivaMovimiento.toLocalDate();
                tramos.add(new SaldoTramo(fechaUltMov, faltante));
            }

            SaldosPorTramo saldoTramos = new SaldosPorTramo();
            for (int i = cabecera; i < tramos.size(); i++) {
                SaldoTramo saldoTemp = tramos.get(i);
                if (saldoTemp.saldoTramo == null || saldoTemp.saldoTramo.compareTo(BigDecimal.ZERO) <= 0) continue;

                SaldosPorTramo paso = this.clasificarTramosDeudas(saldoTemp);
                saldoTramos.DeudaVigente      = saldoTramos.DeudaVigente.add(paso.DeudaVigente);
                saldoTramos.DeudaSegundoTramo = saldoTramos.DeudaSegundoTramo.add(paso.DeudaSegundoTramo);
                saldoTramos.DeudaTercerTramo  = saldoTramos.DeudaTercerTramo.add(paso.DeudaTercerTramo);
                saldoTramos.DeudaCuartoTramo  = saldoTramos.DeudaCuartoTramo.add(paso.DeudaCuartoTramo);
            }
            saldoTramos.Saldo = saldoTramos.getTotalDeuda();

            CargosAbonosCtaInversion ultimo = lista.get(lista.size() - 1);
            CuentaPorCobrarAnexo16 cta = new CuentaPorCobrarAnexo16();
            cta.IdCliente          = ultimo.IdCliente;
            cta.IdCuentaInversion  = ultimo.IdCuentaInversion;
            cta.DocumentoIdentidad = ultimo.DocumentoIdentidadCliente;
            cta.IdMoneda           = ultimo.IdMoneda;
            cta.SaldosPorTramo     = saldoTramos;

            listadoCuentasClasificadas.add(cta);
        }

        long endTime = System.currentTimeMillis();
        logger.trace("FIN::GenerarArchivoCuentasPorCobrarAnexo16.clasificarTramosDeuda - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        return listadoCuentasClasificadas;
    }



    private SaldosPorTramo clasificarTramosDeudas(
        SaldoTramo saldoTemp
    ) {
        logger.trace("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.clasificarTramosDeudas");
        long startTime = System.currentTimeMillis();

        SaldosPorTramo saldo = new SaldosPorTramo();

        long diferenciaDias = (long) Utils.diferenciaEnDias(this._fechaConsulta, Date.valueOf(saldoTemp.fechaEfectivaMovimiento));
        if (diferenciaDias < 0) diferenciaDias = 0L;

        if (diferenciaDias > DIAS_TERCER_TRAMO) {
            saldo.DeudaCuartoTramo = saldo.DeudaCuartoTramo.add(saldoTemp.saldoTramo);
        } else if (diferenciaDias > DIAS_SEGUNDO_TRAMO) {
            saldo.DeudaTercerTramo = saldo.DeudaTercerTramo.add(saldoTemp.saldoTramo);
        } else if (diferenciaDias > DIAS_VIGENTE) {
            saldo.DeudaSegundoTramo = saldo.DeudaSegundoTramo.add(saldoTemp.saldoTramo);
        } else {
            saldo.DeudaVigente = saldo.DeudaVigente.add(saldoTemp.saldoTramo);
        }
        long endTime = System.currentTimeMillis();
        logger.trace("FIN::GenerarArchivoCuentasPorCobrarAnexo16.clasificarTramosDeudas - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        return saldo;
    }


    private List<CuentaPorCobrarAnexo16> agruparCuentasIdCuentaInversion(
        List<CuentaPorCobrarAnexo16> listadoCuentasServicio,
        List<CuentaPorCobrarAnexo16> listadoCuentasCargoAbono
    ){
	   logger.trace("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.agruparCuentasIdCuentaInversion");
       long startTime = System.currentTimeMillis();

       Set<String> keysCargoAbono = listadoCuentasCargoAbono.stream()
                                   .map(c -> c.IdCliente + "_" + c.IdCuentaInversion + "_" + c.IdMoneda)
                                   .collect(Collectors.toSet());

   		HashMap<String, CuentaPorCobrarAnexo16> mapCargosFiltrado 	= new HashMap<>();
   		List<CuentaPorCobrarAnexo16> cuentasServicioFiltrado = new ArrayList<>();
   		
   		for (CuentaPorCobrarAnexo16 cuenta : listadoCuentasServicio) {
   			String key = cuenta.IdCliente + "_" + cuenta.IdCuentaInversion + "_" + cuenta.IdMoneda;
   			if (keysCargoAbono.contains(key)) {
   				mapCargosFiltrado.put(key, cuenta);
   			} else {
                   cuentasServicioFiltrado.add(cuenta);
               }
   		}

       Map<String, CuentaPorCobrarAnexo16> mapCuentasIdCuentaInv = new HashMap<>();
       for (CuentaPorCobrarAnexo16 cuenta : cuentasServicioFiltrado) {
           String key =  cuenta.IdCliente + "_" + cuenta.IdCuentaInversion + "_" + cuenta.IdMoneda;
           if (!mapCuentasIdCuentaInv.containsKey(key)) {
               CuentaPorCobrarAnexo16 nuevaCuenta = new CuentaPorCobrarAnexo16();
               nuevaCuenta.IdCliente = cuenta.IdCliente;
               nuevaCuenta.NombreCliente = cuenta.NombreCliente;// + " - " + cuenta.IdCuentaInversion + " - " + cuenta.IdMoneda;
               nuevaCuenta.DocumentoIdentidad = cuenta.DocumentoIdentidad;
               nuevaCuenta.Relacion = cuenta.Relacion;
               nuevaCuenta.IdCuentaInversion = cuenta.IdCuentaInversion;
               nuevaCuenta.IdMoneda = cuenta.IdMoneda;
               SaldoTramo saldoTemp = new SaldoTramo(cuenta.FechaUltimoMovimiento.toLocalDate(), cuenta.SaldoEfectivo);
               nuevaCuenta.SaldosPorTramo = this.clasificarTramosDeudas(saldoTemp);
               nuevaCuenta.SaldosPorTramo.Saldo = nuevaCuenta.SaldosPorTramo.getTotalDeuda();
               mapCuentasIdCuentaInv.put(key, nuevaCuenta);

           }else {
               CuentaPorCobrarAnexo16 cuentaExistente = mapCuentasIdCuentaInv.get(key);
               SaldoTramo saldoTemp = new SaldoTramo(cuenta.FechaUltimoMovimiento.toLocalDate(), cuenta.SaldoEfectivo);
               SaldosPorTramo saldosExistentes = cuentaExistente.SaldosPorTramo;
               SaldosPorTramo nuevosSaldos = this.clasificarTramosDeudas(saldoTemp);
               saldosExistentes.DeudaVigente = saldosExistentes.DeudaVigente.add(nuevosSaldos.DeudaVigente);
               saldosExistentes.DeudaSegundoTramo = saldosExistentes.DeudaSegundoTramo.add(nuevosSaldos.DeudaSegundoTramo);
               saldosExistentes.DeudaTercerTramo = saldosExistentes.DeudaTercerTramo.add(nuevosSaldos.DeudaTercerTramo);
               saldosExistentes.DeudaCuartoTramo = saldosExistentes.DeudaCuartoTramo.add(nuevosSaldos.DeudaCuartoTramo);
               saldosExistentes.Saldo = saldosExistentes.getTotalDeuda();
           }
       }

       for (CuentaPorCobrarAnexo16 cuenta : listadoCuentasCargoAbono) {
           String key = cuenta.IdCliente + "_" + cuenta.IdCuentaInversion + "_" + cuenta.IdMoneda;
           CuentaPorCobrarAnexo16 cuentaExistente = mapCargosFiltrado.get(key); // <-- cuentasCargosAbonos30dias
           SaldosPorTramo saldosExistentes = cuentaExistente.SaldosPorTramo;
           saldosExistentes.DeudaVigente = saldosExistentes.DeudaVigente.add(cuenta.SaldosPorTramo.DeudaVigente);
           saldosExistentes.DeudaSegundoTramo = saldosExistentes.DeudaSegundoTramo.add(cuenta.SaldosPorTramo.DeudaSegundoTramo);
           saldosExistentes.DeudaTercerTramo = saldosExistentes.DeudaTercerTramo.add(cuenta.SaldosPorTramo.DeudaTercerTramo);
           saldosExistentes.DeudaCuartoTramo = saldosExistentes.DeudaCuartoTramo.add(cuenta.SaldosPorTramo.DeudaCuartoTramo);
           saldosExistentes.Saldo = saldosExistentes.getTotalDeuda();
           mapCuentasIdCuentaInv.put(key, cuentaExistente);
       }


       List<CuentaPorCobrarAnexo16> listadoCuentasAgrupadas = new ArrayList<>(mapCuentasIdCuentaInv.values());
       long endTime = System.currentTimeMillis();
       logger.trace("FIN::GenerarArchivoCuentasPorCobrarAnexo16.agruparCuentasIdCuentaInversion - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
       return listadoCuentasAgrupadas;
    }
    
    private List<Relacion> obtenerRelaciones(
            Connection conn,
            List<CuentaPorCobrarAnexo16> cuentas
    ) throws Exception {
        logger.debug("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.obtenerRelaciones");
        long startTime = System.currentTimeMillis();

        long idTrabajo = this.obtenerIdTrabajo();
        List<CuentaPorCobrarAnexo16> cuentasAnexo16 = cuentas.stream()
            .map(cuenta -> {
                CuentaPorCobrarAnexo16 cta = new CuentaPorCobrarAnexo16();
                cta.IdTrabajo = idTrabajo;
                cta.IdCuentaInversion = cuenta.IdCuentaInversion;
                return cta;
            })
            .collect(Collectors.toList());
        this._sql.batch(conn, SQL_INSERT_ID_CUENTA_INVERSION, cuentasAnexo16, CuentaPorCobrarAnexo16.class);
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put("idTrabajo", idTrabajo);
        List<Relacion> relaciones = this._sql.query(conn, SQL_SELECT_RELACIONES, params, Relacion.class);
        
        long endTime = System.currentTimeMillis();
        logger.debug("FIN::GenerarArchivoCuentasPorCobrarAnexo16.obtenerRelaciones - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        return relaciones;
    }

    private void calcularParidadMonedas(
        Connection conn,
        List<CuentaPorCobrarAnexo16> cuentas
    ) throws Exception{
        logger.trace("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.calcularParidad");
        long startTime = System.currentTimeMillis();

        CalcularParidad servicioParidad = new CalcularParidad();
        CalcularParidadEntrada entradaParidad = new CalcularParidadEntrada();
        entradaParidad.p_IdEmpresa = this._in.p_IdEmpresa;
        entradaParidad.p_IdTrabajo = 0L;
		
        List<CalculoEntrada> listaParidadEntrada = new ArrayList<>();
        HashMap<String, CuentaPorCobrarAnexo16> mapCuentas = new HashMap<>();

        cuentas.stream().forEach(cuenta -> {
            String key =  cuenta.IdCuentaInversion + "_" + cuenta.IdMoneda;
            mapCuentas.put(key, cuenta);

            this.addParidadEntrada(
                listaParidadEntrada,
                cuenta.IdMoneda,
                cuenta.IdCuentaInversion,
                cuenta.SaldosPorTramo.DeudaVigente,
                "VIGENTE");

            this.addParidadEntrada(
                listaParidadEntrada,
                cuenta.IdMoneda,
                cuenta.IdCuentaInversion,
                cuenta.SaldosPorTramo.DeudaSegundoTramo,
                "SEGUNDO_TRAMO");

            this.addParidadEntrada(
                listaParidadEntrada,
                cuenta.IdMoneda,
                cuenta.IdCuentaInversion,
                cuenta.SaldosPorTramo.DeudaTercerTramo,
                "TERCER_TRAMO");

            this.addParidadEntrada(
                listaParidadEntrada,
                cuenta.IdMoneda,
                cuenta.IdCuentaInversion,
                cuenta.SaldosPorTramo.DeudaCuartoTramo,
                "CUARTO_TRAMO");
        });

        entradaParidad.p_CalculosEntrada = listaParidadEntrada;
        CalcularParidadSalida salidaParidad = servicioParidad.logica(conn, entradaParidad);

        // Reemplazamos
        salidaParidad.p_out_CalculosSalida.stream().forEach(paridad -> {
            String key = paridad.IdInterno + "_" + paridad.IdMonedaOrigen;
            if (mapCuentas.containsKey(key)) {
                CuentaPorCobrarAnexo16 cuenta = mapCuentas.get(key);
                BigDecimal montoLocal = paridad.MontoLocal != null ? paridad.MontoLocal : BigDecimal.ZERO;
                switch (paridad.IdUso) {
                    case "VIGENTE":
                        cuenta.SaldosPorTramo.DeudaVigente = montoLocal;
                        break;
                    case "SEGUNDO_TRAMO":
                        cuenta.SaldosPorTramo.DeudaSegundoTramo = montoLocal;
                        break;
                    case "TERCER_TRAMO":
                        cuenta.SaldosPorTramo.DeudaTercerTramo = montoLocal;
                        break;
                    case "CUARTO_TRAMO":
                        cuenta.SaldosPorTramo.DeudaCuartoTramo = montoLocal;
                        break;
                    default:
                        break;
                }
            }
        });
        
        long endTime = System.currentTimeMillis();
        logger.trace("FIN::GenerarArchivoCuentasPorCobrarAnexo16.calcularParidad - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
    }

    private List<CuentaPorCobrarAnexo16> agruparCuentasPorCliente(
        List<CuentaPorCobrarAnexo16> cuentasAgrupadasId,
        List<Relacion> relaciones
    ){
        logger.trace("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.agruparCuentasPorCliente");
        long startTime = System.currentTimeMillis();
        
        Map<Long, List<CuentaPorCobrarAnexo16>> mapCuentasPorCliente = cuentasAgrupadasId.stream()
            .collect(Collectors.groupingBy(c -> c.IdCliente));
        HashMap<Long, Relacion> mapRelacionesPorCliente = new HashMap<Long, Relacion>();
        for (Relacion relacion : relaciones) {
            mapRelacionesPorCliente.putIfAbsent(relacion.IdCliente, relacion);
        }
        
        List<CuentaPorCobrarAnexo16> listadoCuentasAgrupadas = new ArrayList<>();

        mapCuentasPorCliente.forEach((idCliente, cuentas) -> {
            CuentaPorCobrarAnexo16 cuentaAgregada = new CuentaPorCobrarAnexo16();
            cuentaAgregada.IdCliente = idCliente;
            cuentaAgregada.NombreCliente = cuentas.get(0).NombreCliente;
            cuentaAgregada.DocumentoIdentidad = cuentas.get(0).DocumentoIdentidad;
            cuentaAgregada.Relacion = cuentas.get(0).Relacion;
            cuentaAgregada.SaldosPorTramo = new SaldosPorTramo();

            cuentas.forEach(cuenta -> {
                cuentaAgregada.SaldosPorTramo.DeudaVigente = cuentaAgregada.SaldosPorTramo.DeudaVigente.add(cuenta.SaldosPorTramo.DeudaVigente.abs());
                cuentaAgregada.SaldosPorTramo.DeudaSegundoTramo = cuentaAgregada.SaldosPorTramo.DeudaSegundoTramo.add(cuenta.SaldosPorTramo.DeudaSegundoTramo.abs());
                cuentaAgregada.SaldosPorTramo.DeudaTercerTramo = cuentaAgregada.SaldosPorTramo.DeudaTercerTramo.add(cuenta.SaldosPorTramo.DeudaTercerTramo.abs());
                cuentaAgregada.SaldosPorTramo.DeudaCuartoTramo = cuentaAgregada.SaldosPorTramo.DeudaCuartoTramo.add(cuenta.SaldosPorTramo.DeudaCuartoTramo.abs());
            });
            cuentaAgregada.SaldosPorTramo.Saldo = cuentaAgregada.SaldosPorTramo.getTotalDeuda();

            Relacion relacion = mapRelacionesPorCliente.get(idCliente);
            if (relacion != null) {
                cuentaAgregada.Relacion = relacion.Relacionado;
                cuentaAgregada.DescripcionRelacion = relacion.Traduccion;
            }

            listadoCuentasAgrupadas.add(cuentaAgregada);
        });

        long endTime = System.currentTimeMillis();
        logger.trace("FIN::GenerarArchivoCuentasPorCobrarAnexo16.agruparCuentasPorCliente - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        return listadoCuentasAgrupadas;
    }

    
    private byte[] generarExcel (
    		Connection conn,
    		List<CuentaPorCobrarAnexo16> lista) throws Exception {
        logger.debug("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.generarExcel");
        long startTime = System.currentTimeMillis();
    	 
    	HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Anexo16");
        
        CellStyle styleTitulo = workbook.createCellStyle();
        HSSFFont fontTitulo = workbook.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short)12);
        styleTitulo.setFont(fontTitulo);

        Row infoReporte = sheet.createRow(0);
        infoReporte.createCell(0).setCellValue(this.obtenerTraduccion(conn, "UI_ANEXO16_LIBRO_DIARIO_DE_CUENTAS_POR_COBRAR") + " " + this._in.p_FechaHasta);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 10));
        for (int i = 0; i <= 10; i++) {
            sheet.autoSizeColumn(i);
        }

        
        int rownum = 1;
        HSSFRow header = sheet.createRow(rownum++);
        header.createCell(0).setCellValue(this.obtenerTraduccion(conn, "UI_SECUENCIA"));
        header.createCell(1).setCellValue(this.obtenerTraduccion(conn, "UI_RUT_CLIENTE"));
        header.createCell(2).setCellValue(this.obtenerTraduccion(conn, "UI_NOMBRE_CLIENTE"));
        header.createCell(3).setCellValue(this.obtenerTraduccion(conn, "UI_MONTOCOBRAR"));
        header.createCell(4).setCellValue(this.obtenerTraduccion(conn, "UI_VIGENTES"));
        header.createCell(5).setCellValue(this.obtenerTraduccion(conn, "UI_HASTA10"));
        header.createCell(6).setCellValue(this.obtenerTraduccion(conn, "UI_HASTA30"));
        header.createCell(7).setCellValue(this.obtenerTraduccion(conn, "UI_MAS30"));
        header.createCell(8).setCellValue(this.obtenerTraduccion(conn, "UI_OBSERVACIONES"));
        header.createCell(9).setCellValue(this.obtenerTraduccion(conn, "UI_CLASIFICACION_CONTABLE"));
        header.createCell(10).setCellValue(this.obtenerTraduccion(conn, "UI_RELACIONES"));

        int secuencia = 1;
        for (CuentaPorCobrarAnexo16 c : lista) {
            HSSFRow row = sheet.createRow(rownum++);
            row.createCell(0).setCellValue(secuencia++);
            row.createCell(1).setCellValue(c.DocumentoIdentidad);
            row.createCell(2).setCellValue(c.NombreCliente);
            row.createCell(3).setCellValue(c.SaldosPorTramo.getSaldo().doubleValue());
            row.createCell(4).setCellValue(c.SaldosPorTramo.getDeudaVigente().doubleValue());
            row.createCell(5).setCellValue(c.SaldosPorTramo.getDeudaSegundoTramo().doubleValue());
            row.createCell(6).setCellValue(c.SaldosPorTramo.getDeudaTercerTramo().doubleValue());
            row.createCell(7).setCellValue(c.SaldosPorTramo.getDeudaCuartoTramo().doubleValue());
            row.createCell(8).setCellValue("");
            row.createCell(9).setCellValue(c.DescripcionRelacion);
            row.createCell(10).setCellValue(c.Relacion);
            
            sheet.autoSizeColumn(0);
        }
        byte [] excelBytes;
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            workbook.write(outStream);
            excelBytes = outStream.toByteArray();
        }
        workbook.close();
        
        long endTime = System.currentTimeMillis();
        logger.debug("FIN::GenerarArchivoCuentasPorCobrarAnexo16.generarExcel - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        return excelBytes;
    }

    private byte[] generarPdf(
    		Connection conn,
    		List<CuentaPorCobrarAnexo16> lista) throws Exception {

        logger.debug("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.generarPdf");
        long startTime = System.currentTimeMillis();

		String fechaConsulta = this._in.p_FechaHasta;
		LocalDateTime dia = LocalDateTime.now();
		DateTimeFormatter fechaFormateada = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		DateTimeFormatter horaFormateada = DateTimeFormatter.ofPattern("HH:mm");

		Document document = new Document(PageSize.A4.rotate(), 36, 36, 130, 36);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
		Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 7);
		Font fontCabecera = FontFactory.getFont(FontFactory.HELVETICA, 6);

		Image logo = obtenerLogoCorredora(conn);
		logo.scaleToFit(70, 70);
		String tituloTexto = this.obtenerTraduccion(conn, "UI_ANEXO16_LIBRO_DIARIO_DE_CUENTAS_POR_COBRAR");
		String fecha = dia.format(fechaFormateada);
		String hora = dia.format(horaFormateada);

		String detalleOperacion = this.obtenerTraduccion(conn, "UI_DETALLE_CTAS");

		String notas = this.obtenerTraduccion(conn, "UI_REFERENCIA");
		String notas1 = this.obtenerTraduccion(conn, "UI_CLAS_CONT_NO");
		String notas2 = this.obtenerTraduccion(conn, "UI_CLAS_CONT_SI");
		PdfWriter writer = PdfWriter.getInstance(document, outStream);
		writer.setPageEvent(new HeaderFooter(logo, tituloTexto, fecha, hora, fechaConsulta, detalleOperacion, fontTitulo,
				fontNormal, notas,notas1,notas2, fontNormal));

		document.open();

		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		PdfPTable tabla = new PdfPTable(
				new float[] { 1.2f, 1.5f, 4.0f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 2.5f, 2f, 2f });
		tabla.setWidthPercentage(100);		

		PdfPCell secCell = crearCelda(
                this.obtenerTraduccion(conn, "UI_SECUENCIA"), fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE);
		secCell.setRowspan(2);
		tabla.addCell(secCell);

		PdfPCell rutClienteCell = crearCelda(this.obtenerTraduccion(conn, "UI_RUT_CLIENTE"), fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE);
		rutClienteCell.setRowspan(2);
		tabla.addCell(rutClienteCell);

		PdfPCell nombreClienteCell = crearCelda
                (this.obtenerTraduccion(conn, "UI_NOMBRE_CLIENTE"), fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE);
		nombreClienteCell.setRowspan(2);
		tabla.addCell(nombreClienteCell);

		PdfPCell montoCell = (crearCelda(
                this.obtenerTraduccion(conn, "UI_MONTOCOBRAR"), fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE));
		montoCell.setRowspan(2);
		tabla.addCell(montoCell);

		PdfPCell antiguedadHeader = crearCelda(
				this.obtenerTraduccion(conn, "UI_ANTIGUEDAD"), fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE);
		antiguedadHeader.setColspan(4);
		tabla.addCell(antiguedadHeader);

		PdfPCell observacCell = crearCelda(this.obtenerTraduccion(conn, "UI_OBSERVACION"), fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE);
		observacCell.setRowspan(2);
		tabla.addCell(observacCell);

		PdfPCell clasificCell = crearCelda(this.obtenerTraduccion(conn, "UI_CLASIFICACION_CONTABLE") + "(*)", fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE);
		clasificCell.setRowspan(2);
		tabla.addCell(clasificCell);

		PdfPCell relCell = crearCelda(
				this.obtenerTraduccion(conn, "UI_RELACIONADO") + "(**)", fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE);
		relCell.setRowspan(2);
		tabla.addCell(relCell);

		tabla.addCell(crearCelda(this.obtenerTraduccion(conn, "UI_VIGENTES"), fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE));
		tabla.addCell(crearCelda(this.obtenerTraduccion(conn, "UI_HASTA10"), fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE));
		tabla.addCell(crearCelda(this.obtenerTraduccion(conn, "UI_HASTA30"), fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE));
		tabla.addCell(crearCelda(this.obtenerTraduccion(conn, "UI_MAS30"), fontCabecera,
				Element.ALIGN_CENTER, BaseColor.WHITE));

		tabla.setHeaderRows(2);

		int secuencia = 1;
		for (CuentaPorCobrarAnexo16 c : lista) {
			tabla.addCell(
					crearCelda(String.valueOf(secuencia++), fontNormal, Element.ALIGN_CENTER, BaseColor.WHITE));
			tabla.addCell(
					crearCelda(c.DocumentoIdentidad, fontNormal, Element.ALIGN_CENTER, BaseColor.WHITE));
			tabla.addCell(
                    crearCelda(c.NombreCliente, fontNormal, Element.ALIGN_LEFT, BaseColor.WHITE));
			tabla.addCell(
                    crearCelda(c.SaldosPorTramo.getSaldo().toString(), fontNormal, Element.ALIGN_RIGHT, BaseColor.WHITE));
			tabla.addCell(
                    crearCelda(c.SaldosPorTramo.getDeudaVigente().toString(), fontNormal, Element.ALIGN_RIGHT, BaseColor.WHITE));
			tabla.addCell(
					crearCelda(c.SaldosPorTramo.getDeudaSegundoTramo().toString(), fontNormal, Element.ALIGN_RIGHT, BaseColor.WHITE));
			tabla.addCell(
					crearCelda(c.SaldosPorTramo.getDeudaTercerTramo().toString(), fontNormal, Element.ALIGN_RIGHT, BaseColor.WHITE));
			tabla.addCell(
					crearCelda(c.SaldosPorTramo.getDeudaCuartoTramo().toString(), fontNormal, Element.ALIGN_RIGHT, BaseColor.WHITE));
			tabla.addCell(
                    crearCelda("", fontNormal, Element.ALIGN_LEFT, BaseColor.WHITE));
			tabla.addCell(
                    crearCelda(c.DescripcionRelacion, fontNormal, Element.ALIGN_LEFT, BaseColor.WHITE));
			tabla.addCell(
                    crearCelda(c.Relacion, fontNormal, Element.ALIGN_CENTER, BaseColor.WHITE));
		}
		document.add(tabla);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		document.close();

        long endTime = System.currentTimeMillis();
        logger.debug("FIN::GenerarArchivoCuentasPorCobrarAnexo16.generarPdf - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
		return outStream.toByteArray();
	}
    
    private String obtenerTraduccion(
    		Connection conn,
    		String codigo) throws Exception {
        logger.trace("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.obtenerTraduccion");
        long startTime = System.currentTimeMillis();

    	String traduccion = Diccionario.getMensaje(conn, this._in.p_IdEmpresa, codigo, new HashMap<String, String>());

        long endTime = System.currentTimeMillis();
        logger.trace("FIN::GenerarArchivoCuentasPorCobrarAnexo16.obtenerTraduccion - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        logger.trace("Traducción obtenida para {}: {}", codigo, traduccion);
        return traduccion;
    }
    

    private void guardarZip(
    		byte[] excelBytes,
    		byte[] pdfBytes,
    		String path) throws IOException {
        logger.debug("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.guardarZip");
        long startTime = System.currentTimeMillis();

        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {

            ZipEntry excelEntry = new ZipEntry("reporteAnexo16_"+this._fechaConsulta.toString()+".xls");
            zos.putNextEntry(excelEntry);
            zos.write(excelBytes);
            zos.closeEntry();

            ZipEntry pdfEntry = new ZipEntry("reporteAnexo16_"+this._fechaConsulta.toString()+".pdf");
            zos.putNextEntry(pdfEntry);
            zos.write(pdfBytes);
            zos.closeEntry();
        }

        Files.write(Paths.get(path), zipOut.toByteArray());

        long endTime = System.currentTimeMillis();
        logger.debug("FIN::GenerarArchivoCuentasPorCobrarAnexo16.guardarZip - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
    }

    private Image obtenerLogoCorredora(Connection conn) throws Exception {
    	logger.debug("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.obtenerLogoCorredora");
        long startTime = System.currentTimeMillis();
    	Image imagenCorredora = null;
    	
        HashMap<String,Object> params = new HashMap<String,Object>();
        List<LogoCorredora> imagenes =  this._sql.query(conn, SQL_LOGO_ANEXO16, params, LogoCorredora.class);
        if (imagenes != null || !imagenes.isEmpty()) {
        	LogoCorredora logo = imagenes.get(0);
            imagenCorredora = Image.getInstance(logo.imagen);
        }
    
        long endTime = System.currentTimeMillis();
        logger.debug("FIN::GenerarArchivoCuentasPorCobrarAnexo16.obtenerLogoCorredora - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        return imagenCorredora;
    }

    private RutasArchivoAnexo obtenerRutasArchivo(
        Connection conn
    ) throws Exception {
        logger.debug("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.obtenerRutasArchivo");
        long startTime = System.currentTimeMillis();
        RutasArchivoAnexo rutas = new RutasArchivoAnexo();
       
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put("IdEmpresa", this._in.p_IdEmpresa);
        List<Configuracion> lstConfiguracion = this._sql.query(conn, SQL_SELECT_CONFIGURACION, params, Configuracion.class);

        if (lstConfiguracion.isEmpty()) {
            this.agregarMensajeError("FALTA_CONFIGURACION_RUTA_ARCHIVO", conn);
            return rutas;
        }

        Configuracion conf = lstConfiguracion.get(0);
        String rutaArchivo = conf.RutaArchivo != null ? conf.RutaArchivo : "";
        String rutaCta = conf.RutaCta != null ? conf.RutaCta : "";
        String nombreArchivo = generarNombreArchivo("Anexo16", "zip");
        
        rutas.RutaFisica   =  rutaArchivo + nombreArchivo;
        rutas.RutaDescarga =  rutaCta + nombreArchivo;
        
        long endTime = System.currentTimeMillis();
        logger.debug("FIN::GenerarArchivoCuentasPorCobrarAnexo16.obtenerRutasArchivo - Tiempo transcurrido: {} segundos", (endTime - startTime) / 1000);
        return rutas;
    }

    
    /* Funciones auxiliares */
    private BigDecimal clasificaCargoAbono(CargosAbonosCtaInversion mov) {
        BigDecimal monto = mov.Monto != null ? mov.Monto : BigDecimal.ZERO;
        if ("MOVABN".equals(mov.IdTipoMovimiento)) return monto;
        if ("MOVCAR".equals(mov.IdTipoMovimiento)) return monto.negate();
        return BigDecimal.ZERO;
    }

    private void addParidadEntrada(List<CalculoEntrada> lista,
                                   String monedaOrigen,
                                   Long idCuentaInversion,
                                   BigDecimal montoOrigen,
                                   String idUso) {

        if (!monedaOrigen.equalsIgnoreCase(this._monedaSistema) && montoOrigen.compareTo(BigDecimal.ZERO) != 0) {
            CalculoEntrada entrada = new CalculoEntrada();
            entrada.IdTrabajo                    = 0L;
            entrada.IdInterno                    = idCuentaInversion;
            entrada.Fecha                        = this._in.p_FechaHasta;
            entrada.IdMonedaOrigen               = monedaOrigen;
            entrada.IdMonedaDestino              = this._monedaSistema;
            entrada.ValorCambioLocalModificado   = BigDecimal.ZERO;
            entrada.IdUso                        = idUso;
            entrada.MontoOrigen                  = montoOrigen;
            lista.add(entrada);
        }
        /*
        else{
        	System.out.println("Descartado" + " " + monedaOrigen + " "+ montoOrigen + " "+ idUso);
        }
        */
    }

    private void agregarMensajeError(
        String mensaje,
        Connection conn) throws Exception {
        this._out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, mensaje, new HashMap <String, String>(), conn, this._in.p_IdEmpresa));
        this._out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(this._out.P_OUT_MENSAJES);
    }

    private Date convertirFecha(String fecha) {
        try {
            return Date.valueOf(fecha.trim());
        } catch (Exception e) {
            return _fechaSegura;
        }
    }
    
    private Long obtenerIdTrabajo() {
    	return new Double(Math.random() * 10000000000L).longValue();
    }
    
	private String generarNombreArchivo(String nombre, String extension)
	{
	    DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		String fechaHora = LocalDateTime.now().format(formatoFecha);
		return nombre + "_"+ fechaHora +"."+ extension;
	}
	
    
    private PdfPCell crearCelda(String texto, Font font, int align, BaseColor colorFondo) {
		PdfPCell celda = new PdfPCell(new Phrase(texto, font));
		celda.setHorizontalAlignment(align);
		celda.setBackgroundColor(colorFondo);
		celda.setPadding(5);
		return celda;
	}
    
    public static class HeaderFooter extends PdfPageEventHelper {
        private Image logo;
        private String titulo;
        private String fecha;
        private String hora;
        private String fechaConsulta;
        private String detalleOperacion;
        private Font fontTitulo;
        private Font fontNormal;
        private String notas;
        private String notas1;
        private String notas2;
        private Font fontCabecera;

        public HeaderFooter(Image logo, String titulo, String fecha, String hora,String fechaConsulta, String detalleOperacion,
                Font fontTitulo, Font fontNormal, String notas,String notas1,String notas2, Font fontCabecera) {
            this.logo = logo;
            this.titulo = titulo;
            this.fecha = fecha;
            this.hora = hora;
            this.fechaConsulta=fechaConsulta;
            this.detalleOperacion = detalleOperacion;
            this.fontTitulo = fontTitulo;
            this.fontNormal = fontNormal;
            this.notas = notas;
            this.notas1 = notas1;
            this.notas2 = notas2;
            this.fontCabecera = fontCabecera;

        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable header = new PdfPTable(3);
            try {
                header.setWidthPercentage(100);
                header.setTotalWidth(document.right() - document.left());
                header.setLockedWidth(true);
                header.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                header.setWidths(new float[] { 1, 3, 1 });

                PdfPCell cellLogo = new PdfPCell(logo, false);
                cellLogo.setBorder(Rectangle.NO_BORDER);
                cellLogo.setVerticalAlignment(Element.ALIGN_TOP);
                header.addCell(cellLogo);

                PdfPCell cellTitulo = new PdfPCell(new Phrase(titulo, fontTitulo));
                cellTitulo.setBorder(Rectangle.NO_BORDER);
                cellTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellTitulo.setVerticalAlignment(Element.ALIGN_TOP);
                header.addCell(cellTitulo);

                PdfPCell cellFechaHora = new PdfPCell(
                        new Phrase("Fecha: " + fecha + "\nHora: " + hora, fontNormal));
                cellFechaHora.setBorder(Rectangle.NO_BORDER);
                cellFechaHora.setHorizontalAlignment(Element.ALIGN_RIGHT);
                header.addCell(cellFechaHora);

                PdfPCell emptyRight = new PdfPCell(new Phrase(""));
                emptyRight.setColspan(1);
                emptyRight.setBorder(Rectangle.NO_BORDER);
                header.addCell(emptyRight);

                PdfPCell cellDetalleOperacion = new PdfPCell(new Phrase(detalleOperacion + fechaConsulta, fontNormal));
                cellDetalleOperacion.setColspan(2);
                cellDetalleOperacion.setBorder(Rectangle.NO_BORDER);
                cellDetalleOperacion.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellTitulo.setVerticalAlignment(Element.ALIGN_MIDDLE);
                header.addCell(cellDetalleOperacion);

                header.writeSelectedRows(0, -1, document.leftMargin(),
                        document.getPageSize().getHeight() - document.topMargin() + 90,
                        writer.getDirectContent());

            } catch (Exception e) {
                e.printStackTrace();
            }

            PdfPTable footer = new PdfPTable(1);
            footer.setTotalWidth(520);
            footer.setLockedWidth(true);
            footer.setWidthPercentage(100);

            PdfPCell cellNotas = new PdfPCell(new Phrase(notas,fontNormal));
            cellNotas.setBorder(Rectangle.NO_BORDER);
            cellNotas.setHorizontalAlignment(Element.ALIGN_LEFT);
            footer.addCell(cellNotas);

            PdfPCell cellNotas1 = new PdfPCell(
                    new Phrase("(*)"+ notas1+"\n"
                            + "o "+notas2,fontNormal));
            cellNotas1.setBorder(Rectangle.NO_BORDER);
            cellNotas1.setHorizontalAlignment(Element.ALIGN_LEFT);
            footer.addCell(cellNotas1);

            PdfPCell cellNotas2 = new PdfPCell(new Phrase("(**) Cliente Relacionado: SI - NO.",fontNormal));
            cellNotas2.setBorder(Rectangle.NO_BORDER);
            cellNotas2.setHorizontalAlignment(Element.ALIGN_LEFT);
            footer.addCell(cellNotas2);

            footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() + 10,
            writer.getDirectContent());
        }

    }
    


    @Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();	
		
		GenerarArchivoCuentasPorCobrarAnexo16_Entrada inService = gson.fromJson(in.toString(), GenerarArchivoCuentasPorCobrarAnexo16_Entrada.class);
		GenerarArchivoCuentasPorCobrarAnexo16_Salida outService = logica(conn, inService);
		outService.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(outService.P_OUT_MENSAJES);
        
		JsonParser parser 	= new JsonParser();
        JsonObject outData	= parser.parse(gson.toJson(outService)).getAsJsonObject();
		return outData;
	}

    public static void main(String[] args) throws Exception {
        logger.info("INICIO::GenerarArchivoCuentasPorCobrarAnexo16.main");
        try (Connection con = PlataformaUtils.getPropertiesConnection("prefynsa", "connections.properties")) {

            String entradaJson = "{\r\n" +
                "  \"p_IdEmpresa\": 1,\r\n" +
                "  \"p_FechaHasta\": \"2025-08-14\",\r\n" +
                "  \"p_IdUsuario\": 1\r\n" +
                "}";

            Gson gson = Utils.getGsonBuilder();
            GenerarArchivoCuentasPorCobrarAnexo16_Entrada in = gson.fromJson(entradaJson, GenerarArchivoCuentasPorCobrarAnexo16_Entrada.class);
            GenerarArchivoCuentasPorCobrarAnexo16 demo = new GenerarArchivoCuentasPorCobrarAnexo16();

            try {
                GenerarArchivoCuentasPorCobrarAnexo16_Salida out = demo.logica(con, in);
                System.out.println(out);
            } catch (Exception er) {
                er.printStackTrace();
            }

            con.rollback();
        }
        logger.info("FIN::GenerarArchivoCuentasPorCobrarAnexo16.main");
    }

}

