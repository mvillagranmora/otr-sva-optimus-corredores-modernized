

package cl.bcs.fondomutuo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cl.bcs.cuentaInversion.CertificarCargoAbono;
import cl.bcs.cuentaInversion.CertificarCargoAbono.CertificarCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CertificarCargoAbono.CertificarCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.fondomutuo.CargarValorCuota.EntradaCargarValorCuota;
import cl.bcs.fondomutuo.CargarValorCuota.SalidaCargarValorCuota;
import cl.bcs.fondomutuo.CertificarPorFiltrosSolicitudAporteRescate.EntradaCertificarPorFiltrosSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarPorFiltrosSolicitudAporteRescate.SalidaCertificarPorFiltrosSolicitudAporteRescate;
import cl.bcs.fondomutuo.CrearIngresoRetiroTituloFondoMutuo.EntradaCrearIngresoRetiroTituloFondoMutuo;
import cl.bcs.fondomutuo.CrearIngresoRetiroTituloFondoMutuo.SalidaCrearIngresoRetiroTituloFondoMutuo;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.GenerarDataReporteTRXIFolioContrato.EntradaGenerarDataReporteTRXIFolioContrato;
import cl.bcs.fondomutuo.GenerarDataReporteTRXIFolioContrato.SalidaGenerarDataReporteTRXIFolioContrato;
import cl.bcs.plataforma.pla.AjustarFechaSistema;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.plataforma.pla.AjustarFechaSistema;
import cl.bcs.plataforma.pla.AjustarFechaSistema.EntradaServicio;
import cl.bcs.plataforma.pla.AjustarFechaSistema.ServicioSalida;

class TestCrearAporteRescateNormalSolicitud {
	
	static String sql = "SELECT x.\"IdCargoAbonoMovimiento\" FROM BSCL.\"BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento\" x WHERE \"IdEstado\" = 'PEN' AND \"Descripcion\" = 'PRUEBA JAVA TEST UNITARIO';";
	String sql_obtenerValorUF = "SELECT x.\"Valor\" FROM BSCL.\"BSCL.Plataforma.BD::MER.ParametroFinanciero\" x WHERE \"Fecha\" = '%s' AND \"IdParametroFinanciero\" = 'UF';";
	String sql_costoUF ="SELECT \"MontoMonedaSistema\",\"CostoUF\",\"ValorUFFechaInversion\" FROM BSCL.\"BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate\" WHERE \"IdSolicitudAporteRescate\" = 17415839;";
	static String SELECT_CONSULTA_CLIENTE = "SELECT \"IdPortafolio\",\"IdCliente\" FROM BSCL.\"BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio\" WHERE \"DocumentoIdentidad\" = '%s' AND \"CodigoPortafolio\" = '%s'";
	
	public static SalidaCrearSolicitudAporteRescateNormal ObjOut_SalidaCrearSolicitudAporteRescateNormal;
	
	//Objetos antes de realizar el test
	public static SalidaCrearIngresoRetiroTituloFondoMutuo ObjOut_SalidaCrearIngresoRetiroTituloFondoMutuo;
	public static CrearCargoAbonoSalida ObjOut_CrearCargoAbonoSalida;
	public static CertificarCargoAbonoSalida ObjOut_CertificarCargoAbonoSalida;
	public static ServicioSalida ObjOut_SalidaServicio;
	public static SalidaCargarValorCuota ObjOut_SalidaCargarValorCuota;
	public static SalidaGenerarDataReporteTRXIFolioContrato ObjOut_SalidaGenerarDataReporteTRXIFolioContrato;
	public static ConsultaCliente clienteAuxiliar;
	static String fechaSistema = "";
	
	public static class SelectIdCargoMovimiento{
		public Long IdCargoAbonoMovimiento;
	}
	
	public static class SelectValorUF{
		public BigDecimal Valor;
	}
	
	public static class SelectValidarCostoUF{
		public BigDecimal MontoMonedaSistema;
		public BigDecimal ValorUF;
		public BigDecimal CostoUF;
	}
	
	public static class ConsultaCliente{
		public Long IdCliente;
		public Long IdPortafolio;
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	
	/*
	 * CREAR CLASE DE PRUEBA
	 * CLIENTE LE COLOCO EL RUT TE DEVUELVE EL IDCLIENTE,PORTAFOLIO, 
	*/
	
	@BeforeAll
	static void BeforeAll() throws Exception {
		
		
		
		//CREAR UN INGRESO DE TITULO A PROBAR 
		try(Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")){
			
			Map<String, String> relaciones = new HashMap<>();
			relaciones.put("7439235-0", "APV-A");
			relaciones.put("16657034-4", "110");
			//relaciones.put("19087846-5", "APV-B");

			// Ejecutar la consulta
			List<ConsultaCliente> resultados = ejecutarConsulta(con, relaciones);
			clienteAuxiliar = resultados.get(1);
			fechaSistema = obtenerFechaSistema(con,1L);
			crearIngresoTitulo(con);
			crearAbonoCuentaInversion(con);
			certificarCuentaInversion(con);
		}
		
		//REVISAR EL CAMBIO DE FECHA, LUEGO QUE SE CAMBIA LA FECHA NO CONECTA NUEVAMENTE.
		boolean esCertificacionMasiva = false;
		
		//MOVER A LA FECHA DE SISTEMA A LA FECHA DE CERTIFICACION
		if(esCertificacionMasiva) {
			try(Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")){
				
				String fechaCertificacion = ObjOut_SalidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO.get(0).FechaCertificacion;
				
				AjustarFechaSistema ingresoCambioFecha = new AjustarFechaSistema();
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String jsonCertificacionAporte =String.format("{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":'PLA',\"p_SimularOperacion\":'N',\"p_FechaSistema\":[{\"FechaSistema\":'%s',\"IdEmpresa\":1}]}",fechaCertificacion);
				
				EntradaServicio Objin = gson.fromJson(jsonCertificacionAporte, EntradaServicio.class);
				
				ObjOut_SalidaServicio  = ingresoCambioFecha.logica(con, Objin);
				
				assertNotNull(ObjOut_SalidaServicio, "La salida no debería ser nula");
				System.out.println("=========CREACION APORTE NORMAL========");
				System.out.println(ObjOut_SalidaServicio.toString());
				System.out.println("=================");
				con.commit();
				con.close();
			}
			
			
			//CARGAR VALOR CUOTA PARA ESE INSTRUMENTO QUE GENERO EL APORTE
			try(Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")){
				
				BigDecimal valorCuota = ObjOut_SalidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO.get(0).ValorCuota;
				String idSerieFondo = ObjOut_SalidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO.get(0).IdSerieFondo;
				String fechaValorCuota = ObjOut_SalidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO.get(0).FechaValorCuota;
				
				CargarValorCuota ingresoCargaValorCuota = new CargarValorCuota();
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String jsonCargaValorCuota =String.format("{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"FMM\",\"p_UsaReglasNegocio\":\"N\",\"p_SimularOperacion\":\"N\",\"p_AllFunds\":\"N\",\"p_ValorCuota\":[{\"IDSERIEFONDO\":\"%s\",\"FECHACIERRE\":\"%s\",\"VALORCUOTA\":%s,\"IDMONEDA\":\"CLP\",\"OBSERVACION\":\"\",\"MENSAJEVALIDACION\":\"OK\"}]}",idSerieFondo,fechaValorCuota,valorCuota.toString());
	
				EntradaCargarValorCuota Objin = gson.fromJson(jsonCargaValorCuota, EntradaCargarValorCuota.class);
				
				ObjOut_SalidaCargarValorCuota  = ingresoCargaValorCuota.logica(con, Objin);
				
				assertNotNull(ObjOut_SalidaCargarValorCuota, "La salida no debería ser nula");
				System.out.println("=========CREACION APORTE NORMAL========");
				System.out.println(ObjOut_SalidaCargarValorCuota.toString());
				System.out.println("=================");
				con.commit();
				con.close();
			}
	
			//CREAR OMNIBUS DE LA OPERACION CREAR UPDATE A LA IDSOLICITUDAPORTERESCATE DEJAR DISTINTO DE 0
			try(Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")){
				
				Long IdSolicitudAporteRescate = ObjOut_SalidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO.get(0).IdSolicitudAporteRescate;
				
				GenerarDataReporteTRXIFolioContrato ingresoFolioOmnibus = new GenerarDataReporteTRXIFolioContrato();
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String jsonGenerarOmnibus =String.format("{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"FMM\",\"p_Solicitudes\":[{\"IdSolicitudAporteRescate\":%d,\"FolioContrato\":0}]}",IdSolicitudAporteRescate);
	
				EntradaGenerarDataReporteTRXIFolioContrato Objin = gson.fromJson(jsonGenerarOmnibus, EntradaGenerarDataReporteTRXIFolioContrato.class);
				
				ObjOut_SalidaGenerarDataReporteTRXIFolioContrato  = ingresoFolioOmnibus.logica(con, Objin);
				
				assertNotNull(ObjOut_SalidaGenerarDataReporteTRXIFolioContrato, "La salida no debería ser nula");
				System.out.println("=========GENERAR OMNIBUS========");
				System.out.println(ObjOut_SalidaGenerarDataReporteTRXIFolioContrato.toString());
				System.out.println("=================");
				con.commit();
				con.close();
			}
		}
	}

	@Test
	void CreacionAporteNormal() throws Exception {
	    try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	        CrearSolicitudAporteRescateNormal ingresoAporte = new CrearSolicitudAporteRescateNormal();
	        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

	        
	        String jsonIngresoAporte = JsonGenerator.generateJsonCrearAporteNormal(clienteAuxiliar.IdCliente, clienteAuxiliar.IdPortafolio);

	        EntradaCrearSolicitudAporteRescateNormal Objin = gson.fromJson(jsonIngresoAporte, EntradaCrearSolicitudAporteRescateNormal.class);

	        ObjOut_SalidaCrearSolicitudAporteRescateNormal = ingresoAporte.logica(con, Objin);

	        assertNotNull(ObjOut_SalidaCrearSolicitudAporteRescateNormal, "La salida no debería ser nula");
	        assertEquals(ObjOut_SalidaCrearSolicitudAporteRescateNormal.P_OUT_ERRORESCONTADOR.longValue(), 0L, "Creacion del aporte");
	        System.out.println("=========CREACION APORTE NORMAL========");
	        System.out.println(ObjOut_SalidaCrearSolicitudAporteRescateNormal.toString());
	        System.out.println("=================");
	        con.commit();
	    }
	}
	
	private static void crearIngresoTitulo(Connection con) throws Exception {
	    CrearIngresoRetiroTituloFondoMutuo ingresoTitulo = new CrearIngresoRetiroTituloFondoMutuo();
	    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

	   
	    String jsonIngresoTitulo = JsonGenerator.generateJsonIngresoTitulo(clienteAuxiliar.IdCliente, clienteAuxiliar.IdPortafolio);

	    EntradaCrearIngresoRetiroTituloFondoMutuo objIn = gson.fromJson(jsonIngresoTitulo, EntradaCrearIngresoRetiroTituloFondoMutuo.class);

	    ObjOut_SalidaCrearIngresoRetiroTituloFondoMutuo = ingresoTitulo.logica(con, objIn);

	    System.out.println("=========INGRESO TÍTULO========");
	    System.out.println(ObjOut_SalidaCrearIngresoRetiroTituloFondoMutuo.toString());
	    System.out.println("=================");
	    con.commit();
	}
    
    private static void crearAbonoCuentaInversion(Connection con) throws Exception {
        CrearCargoAbono crearAbono = new CrearCargoAbono();
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

        
        String jsonCrearAbono = JsonGenerator.generateJsonCrearAbono(fechaSistema, clienteAuxiliar.IdPortafolio);

        CrearCargoAbonoEntrada Objin = gson.fromJson(jsonCrearAbono, CrearCargoAbonoEntrada.class);

        ObjOut_CrearCargoAbonoSalida = crearAbono.logica(con, Objin);

        System.out.println("=========CREACION ABONO A LA CIN========");
        System.out.println(ObjOut_CrearCargoAbonoSalida.toString());
        System.out.println("=================");
        con.commit();
    }
    
    
    private static void certificarCuentaInversion(Connection con) throws Exception {
        SQL _sql = new SQL();
        List<SelectIdCargoMovimiento> c = _sql.query(con, sql, new HashMap<>(), SelectIdCargoMovimiento.class);

        CertificarCargoAbono certificarAbono = new CertificarCargoAbono();
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

        
        String jsonCertificacionAbono = JsonGenerator.generateJsonCertificarCuentaInversion(fechaSistema, c.get(0).IdCargoAbonoMovimiento);

        CertificarCargoAbonoEntrada Objin = gson.fromJson(jsonCertificacionAbono, CertificarCargoAbonoEntrada.class);

        ObjOut_CertificarCargoAbonoSalida = certificarAbono.logica(con, Objin);

        System.out.println("=========CERTIFICACION DEL ABONO========");
        System.out.println(ObjOut_CertificarCargoAbonoSalida.toString());
        System.out.println("=================");
        con.commit();
    }
    
	private static String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		return fechaSistema;
	}
	
	// Método para construir la subconsulta dinámicamente
	private static String construirSubconsulta(Map<String, String> relaciones) {
	    StringBuilder subconsulta = new StringBuilder();
	    for (Map.Entry<String, String> entry : relaciones.entrySet()) {
	        String documentoIdentidad = entry.getKey();
	        String codigoPortafolio = entry.getValue();
	        subconsulta.append(String.format("SELECT '%s' AS \"DocumentoIdentidad\", '%s' AS \"CodigoPortafolio\" FROM DUMMY", documentoIdentidad, codigoPortafolio));
	        subconsulta.append(" UNION ALL ");
	    }
	    // Eliminar el último "UNION ALL" innecesario
	    if (subconsulta.length() > 0) {
	        subconsulta.setLength(subconsulta.length() - " UNION ALL ".length());
	    }
	    return subconsulta.toString();
	}

	// Método principal para ejecutar la consulta
	private static List<ConsultaCliente> ejecutarConsulta(Connection con, Map<String, String> relaciones) throws SQLException {
	    // Construir la subconsulta dinámica
	    String subconsulta = construirSubconsulta(relaciones);

	    // Consulta SQL base
	    String SELECT_CONSULTA_CLIENTE = "SELECT P.\"IdPortafolio\", P.\"IdCliente\" " +
	                                     "FROM BSCL.\"BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio\" AS P " +
	                                     "JOIN (" + subconsulta + ") AS Relacion " +
	                                     "ON P.\"DocumentoIdentidad\" = Relacion.\"DocumentoIdentidad\" " +
	                                     "AND P.\"CodigoPortafolio\" = Relacion.\"CodigoPortafolio\";";

	    // Ejecutar la consulta
	    SQL sqlCliente = new SQL();
	    return sqlCliente.query(con, SELECT_CONSULTA_CLIENTE, new HashMap<>(), ConsultaCliente.class);
	}
   
    
}

class JsonGenerator {
	/*
	 * ARGS:
	 * 
	 * 	fechaMovimiento:
	 * 	idCargoAbonoMovimiento:
	 * 
	 * */
    public static String generateJsonCertificarCuentaInversion(String fechaMovimiento, Long idCargoAbonoMovimiento) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("p_IdEmpresa", 1);
        jsonMap.put("p_IdUsuario", 0);
        jsonMap.put("p_IdModulo", "CTA");
        jsonMap.put("p_UsaReglasNegocio", "S");
        jsonMap.put("p_FechaMovimiento", fechaMovimiento);
        jsonMap.put("p_IdPais", 44);

        Map<String, Object> cargoAbono = new HashMap<>();
        cargoAbono.put("IdCargoAbonoMovimiento", idCargoAbonoMovimiento);

        jsonMap.put("p_CargosAbonosACertificar", new Map[]{cargoAbono});

        Gson gson = new Gson();
        return gson.toJson(jsonMap);
    }
    
	/*
	 * ARGS:
	 * 
	 * 	fechaMovimiento:
	 * 	idCargoAbonoMovimiento:
	 * 
	 * */
    
    public static String generateJsonCrearAbono(String fechaSistema, Long idPortafolio) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("p_IdEmpresa", 1);
        jsonMap.put("p_IdUsuario", 0);
        jsonMap.put("p_IdModulo", "CTA");
        jsonMap.put("p_simularOperacion", "N");
        jsonMap.put("p_UsaReglasNegocio", "S");
        jsonMap.put("p_FechaSistema", fechaSistema);
        jsonMap.put("p_EsIngresoManual", "S");

        Map<String, Object> cargoAbono = new HashMap<>();
        cargoAbono.put("NumeroCargoAbono", 1);
        cargoAbono.put("IdPortafolio", idPortafolio);
        cargoAbono.put("IdMoneda", "CLP");
        cargoAbono.put("IdTipoMovimiento", "MOVABN");
        cargoAbono.put("IdConcepto", "AACFIR");
        cargoAbono.put("Descripcion", "PRUEBA JAVA TEST UNITARIO");
        cargoAbono.put("IdCondicionDeLiquidacion", "T0");
        cargoAbono.put("FechaCondicionDeLiquidacion", fechaSistema);
        cargoAbono.put("Monto", 100000);
        cargoAbono.put("IdEjecutivo", 0);
        cargoAbono.put("IdSucursal", 0);
        cargoAbono.put("IdTipoProducto", "");
        cargoAbono.put("IdTipoOperacion", "");
        cargoAbono.put("IdOperacion", 0);
        cargoAbono.put("FechaOperacion", "0001-01-01");
        cargoAbono.put("FechaLiquidacionOperacion", "0001-01-01");
        cargoAbono.put("FechaVencimientoOperacion", null);
        cargoAbono.put("IdMonedaOperacion", "");
        cargoAbono.put("MontoOperacion", null);
        cargoAbono.put("TipoCambioOperacion", 0);
        cargoAbono.put("IdContraparte", null);
        cargoAbono.put("IdTipoComprobante", null);
        cargoAbono.put("IdComprobante", null);
        cargoAbono.put("FolioComprobante", null);
        cargoAbono.put("IdComprobanteDetalle", null);
        cargoAbono.put("IdComprobanteFormaDePago", null);
        cargoAbono.put("IdTipoMovimientoAsociado", null);
        cargoAbono.put("IdMovimientoAsociado", null);
        cargoAbono.put("FechaComprobante", null);
        cargoAbono.put("IdPersonaPagoCobro", null);
        cargoAbono.put("IdInstrumento", null);
        cargoAbono.put("IdTipoInstrumento", null);
        cargoAbono.put("Nemo", null);
        cargoAbono.put("EsIndicadorBilateral", "N");
        cargoAbono.put("EsCargoAbonoPorTraspaso", "N");
        cargoAbono.put("IdPortafolioTraspaso", null);
        cargoAbono.put("GeneroContraparteEnTesoreria", "N");
        cargoAbono.put("CertificadoAutomatico", "N");

        jsonMap.put("p_CargoAbonoAGenerar", new Map[]{cargoAbono});

        Gson gson = new Gson();
        return gson.toJson(jsonMap);
    }
    
    
	/*
	 * ARGS:
	 * 
	 * 	fechaMovimiento:
	 * 	idCargoAbonoMovimiento:
	 * 
	 * */
    
    public static String generateJsonIngresoTitulo(Long idCliente, Long idPortafolio) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("p_IdEmpresa", 1);
        jsonMap.put("p_IdUsuario", 0);
        jsonMap.put("p_IdModulo", "FMM");
        jsonMap.put("p_simularOperacion", "N");
        jsonMap.put("p_UsaReglasNegocio", "S");

        Map<String, Object> solicitud1 = new HashMap<>();
        solicitud1.put("IdInterno", 1);
        solicitud1.put("IdCliente", idCliente);
        solicitud1.put("IdPortafolio", idPortafolio);
        solicitud1.put("IdTipoMovimientoFondoMutuo", "INGTIT");
        solicitud1.put("IdSerieFondo", "00038ACLP");
        solicitud1.put("NumeroCuotas", 100);
        solicitud1.put("SolicitudBatch", "N");
        solicitud1.put("OrigenOperacion", "M");
        solicitud1.put("IdRegimenTributario", "18Q");
        solicitud1.put("Glosa", "PRUEBA EJECUTADO POR JAVA UNITARIA TEST");
        solicitud1.put("IdAutorizadoOrigen", 0);

        Map<String, Object> solicitud2 = new HashMap<>();
        solicitud2.put("IdInterno", 1);
        solicitud2.put("IdCliente", 43003L); // Ejemplo de otro cliente
        solicitud2.put("IdPortafolio", 388037L); // Ejemplo de otro portafolio
        solicitud2.put("IdTipoMovimientoFondoMutuo", "INGTIT");
        solicitud2.put("IdSerieFondo", "00041CCLP");
        solicitud2.put("NumeroCuotas", 100);
        solicitud2.put("SolicitudBatch", "N");
        solicitud2.put("OrigenOperacion", "M");
        solicitud2.put("IdRegimenTributario", "APV-A");
        solicitud2.put("Glosa", "");
        solicitud2.put("IdAutorizadoOrigen", 0);

        jsonMap.put("p_SolicitudesTitulo", new Map[]{solicitud1, solicitud2});

        Gson gson = new Gson();
        return gson.toJson(jsonMap);
    }
    
    public static String generateJsonCrearAporteNormal(Long idCliente, Long idPortafolio) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("p_IdEmpresa", 1);
        jsonMap.put("p_IdUsuario", 0);
        jsonMap.put("p_IdModulo", "FMM");
        jsonMap.put("p_simularOperacion", "N");
        jsonMap.put("p_UsaReglasNegocio", "S");

        Map<String, Object> solicitud = new HashMap<>();
        solicitud.put("IdInterno", 1);
        solicitud.put("IdCliente", idCliente);
        solicitud.put("IdPortafolio", idPortafolio);
        solicitud.put("TipoSolicitud", "APO");
        solicitud.put("IdSerieFondo", "00038ACLP");
        solicitud.put("CalculoPorMontoCuota", "M");
        solicitud.put("EsPorMontoLiquido", "N");
        solicitud.put("Monto", 100000);
        solicitud.put("NumeroCuotas", 58.8097);
        solicitud.put("IdMoneda", "CLP");
        solicitud.put("IdMedioDePago", "CIN");
        solicitud.put("IdPreferenciaPagoPortafolio", 0);
        solicitud.put("Visado", "N");
        solicitud.put("SolicitudBatch", "N");
        solicitud.put("OrigenOperacion", "M");
        solicitud.put("FechaOperacion", "0001-01-01");
        solicitud.put("IdRegimenTributario", "18Q");
        solicitud.put("Glosa", "PRUEBA EJECUTADA POR EL JAVA TEST UNITARIO");
        solicitud.put("IndicadorMLT", "N");
        solicitud.put("IdSerieFondoDestino", "");
        solicitud.put("IdEmisorDestino", "");
        solicitud.put("IdAutorizadoOrigen", 0);
        solicitud.put("Porcentaje", 0);
        solicitud.put("FolioContrato", 0);
        solicitud.put("IdMonedaPago", "");
        solicitud.put("IngresoDesde", "CORREDORES");

        jsonMap.put("p_SolicitudesAporteRescate", new Map[]{solicitud});
        jsonMap.put("p_FormaPagoSpot", new Map[]{});

        Gson gson = new Gson();
        return gson.toJson(jsonMap);
    }
    
}