package cl.bcs.fondomutuo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarArchivoMovimientos implements JavaCall{
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultaMovimientosGeneracionArchivoMov_SelectMovimientos.sql");
	private final String SQL_SELECT_HOMOLOGACION = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultaGeneracionArchivos_SelectHomologacion.sql");

	private final static Logger logger = LogManager.getLogger(GenerarArchivoMovimientos.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	private final String [] TIPOS_VALOR = {"IndicadorAporteRescate" ,"CodigoMovimiento" ,"TipoOperacion", "IdMedioDePago"};
	private final int LARGO_LINEA = 203;
	
	public static class GenerarArchivoMovimientos_Entrada extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCurse;
		public String p_TipoSolicitud;
		public String p_IdMoneda;
		public String p_FondoMutuo;
		public String p_Emisor;
		public String p_Estado;
		
		public GenerarArchivoMovimientos_Entrada () {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "FMM";
			this.p_FechaCurse = "0001-01-01";
			this.p_TipoSolicitud = "";
			this.p_IdMoneda = "";
			this.p_FondoMutuo = "";
			this.p_Emisor = "";
			this.p_Estado = "";
		}
	}
		
	public static class GenerarArchivoMovimientos_Salida extends BcsPojo{
		public List<String> 		P_OUT_ARCHIVOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public String				P_OUT_NOMBRE_ARCHIVO;
		public String				P_OUT_RUTA_ARCHIVO; 
		public String				P_OUT_NOMBRE_ARCHIVO_LOG;
		public String				P_OUT_RUTA_ARCHIVO_LOG; 
		public String 				P_OUT_COMENTARIO;
		
		public GenerarArchivoMovimientos_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ARCHIVOS = new ArrayList<String>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_NOMBRE_ARCHIVO = "";	
			this.P_OUT_RUTA_ARCHIVO = "";	
			this.P_OUT_NOMBRE_ARCHIVO_LOG = "";	
			this.P_OUT_RUTA_ARCHIVO_LOG = "";	
			this.P_OUT_COMENTARIO = "";	
		}
	}

	public static class MovimientoFMM extends BcsPojo{	
		public Long IdSolicitudAporteRescate;
		public String 	IndicadorAporteRescate;
		public String	HomologacionIndicadorAporteRescate;
		public String	HomologacionCodigoMovimiento;
		public String	HomologacionTipoOperacion;
		public String	HomologacionMedioPago;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Timestamp FechaIngresoRegistro;
		public String CalculoPorMontoCuota;	
		public BigDecimal NumeroCuotas;
		public BigDecimal Monto;
		public String ValorIndicador;
		public Date FechaLiquidacion;	  
		public String Nemotecnico;
		public String Estado;
		public String IdMoneda;
		public String ValorDestino;
		public Long DiasRetenciones;
		public String IdMonedaFondo;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public String IdSerieFondo;
		public String FechaValorCuota;
		public String IdMedioDePago;
		public String CodigoEjecutivo;
		
		public MovimientoFMM () {
			this.IdSolicitudAporteRescate = 0L;
			this.IndicadorAporteRescate = "";
			this.HomologacionIndicadorAporteRescate = "";
			this.HomologacionCodigoMovimiento = "";
			this.HomologacionTipoOperacion = "";
			this.HomologacionMedioPago = "";
			this.DocumentoIdentidad = "";
			this.CodigoPortafolio = "";
			this.FechaIngresoRegistro = Fecha.toTimeStamp("0001-01-01 00:00:00");
			this.CalculoPorMontoCuota = "";	
			this.NumeroCuotas = BigDecimal.ZERO;
			this.Monto = BigDecimal.ZERO;
			this.ValorIndicador = "";
			this.FechaLiquidacion = Fecha.toDate("0001-01-01");
			this.Nemotecnico = "";
			this.Estado = "";
			this.IdMoneda = "";
			this.ValorDestino = "";
			this.DiasRetenciones = 0L;
			this.IdMonedaFondo = "";
			this.TipoSolicitud = "";
			this.IdTipoMovimientoFondoMutuo = "";
			this.IdSerieFondo = "";
			this.FechaValorCuota = "0001-01-01";
			this.IdMedioDePago = "D";
			this.CodigoEjecutivo = "";
		}
	}
	
	public static class Homologacion extends BcsPojo{	
		public Long		IdHomologacion;  
		public String	ValorOrigen;
		public String	TipoValor;
		public String	Entidad;
		public String	ValorDestino;
		public String	Descripcion;	
		public String	ValorOrigen2;
		public String	ValorOrigen3;
		
		public Homologacion() {
			this.IdHomologacion = 0L;  
			this.ValorOrigen = "";
			this.TipoValor = "";
			this.Entidad = "";
			this.ValorDestino = "";
			this.Descripcion = "";
			this.ValorOrigen2 = "";
			this.ValorOrigen3 = "";
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		GenerarArchivoMovimientos_Entrada in = gson.fromJson(jin.toString(), GenerarArchivoMovimientos_Entrada.class);
		GenerarArchivoMovimientos_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public GenerarArchivoMovimientos_Salida logica(Connection conn, GenerarArchivoMovimientos_Entrada entrada) throws Exception{
		logger.info("INICIO::GenerarArchivoMovimientos");
		String mensajeGeneracionArchivo = "ARCHIVO_GENERADO_EXITOSAMENTE";
		long inicio = System.currentTimeMillis();
		long startTime = System.currentTimeMillis();
		GenerarArchivoMovimientos_Salida salida = new GenerarArchivoMovimientos_Salida();
		
		try {
			SQL _sql = new SQL();
			
			HashMap<String,Object> params =  new HashMap<String,Object>(7);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("FechaCurse", entrada.p_FechaCurse);
			params.put("TipoSolicitud", entrada.p_TipoSolicitud);
			params.put("Moneda", entrada.p_IdMoneda);
			params.put("FondoMutuo", entrada.p_FondoMutuo);
			params.put("Emisor", entrada.p_Emisor);
			params.put("EstadoMovimiento", entrada.p_Estado);
			
			logger.info("INFO::GenerarArchivoMovimientos :: INICIO - Select Obtener informacion movimientos");
			List<MovimientoFMM> movimientos = _sql.query(conn, SQL_SELECT_MOVIMIENTOS,params, MovimientoFMM.class);
			logger.info("INFO::GenerarArchivoMovimientos :: FIN - Select Obtener informacion movimientos");
			
			movimientos = valorizarSolicitud(conn, entrada.p_IdEmpresa, movimientos);
			params.clear();
			
			List<String> tiposValor = new ArrayList<String>(3);
			for(String tipoValor : TIPOS_VALOR) {
				tiposValor.add(tipoValor);
			}
			params.put("TipoValor", tiposValor);
			
			List<Homologacion> homologacion = _sql.query(conn, SQL_SELECT_HOMOLOGACION,params, Homologacion.class);
			
			movimientos=homologar(movimientos,homologacion);
			
			String nombreArchivo = generarNombreArchivo(entrada.p_TipoSolicitud);
			
			String rutaBaseSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
			String rutaBaseFFMM = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "FONDOMUTUO", "RUTAARCHIVOSFFMM");
			
			salida.P_OUT_NOMBRE_ARCHIVO = nombreArchivo+ ".txt" ;
			salida.P_OUT_RUTA_ARCHIVO = rutaBaseFFMM + salida.P_OUT_NOMBRE_ARCHIVO;
			salida.P_OUT_NOMBRE_ARCHIVO_LOG = nombreArchivo + ".log";
			salida.P_OUT_RUTA_ARCHIVO_LOG = rutaBaseFFMM + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
			
			String rutaCompletaArchivo = rutaBaseSistema + rutaBaseFFMM + salida.P_OUT_NOMBRE_ARCHIVO;
		    String rutaCompletaArchivoLog = rutaBaseSistema + rutaBaseFFMM + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
			//String rutaCompletaArchivo = "C:\\archivos\\"  + salida.P_OUT_NOMBRE_ARCHIVO; 			//para debug
			//String rutaCompletaArchivoLog = "C:\\archivos\\"  + salida.P_OUT_NOMBRE_ARCHIVO_LOG;		//para debug
			
			if (!generarArchivo(rutaCompletaArchivo,rutaCompletaArchivoLog, movimientos, startTime)) {
				mensajeGeneracionArchivo = "ERROR_ARCHIVO_GENERADO";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_ARCHIVO_GENERADO",new HashMap<String, String>(0),conn, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}
			
			salida.P_OUT_COMENTARIO =  Diccionario.getMensaje(conn, entrada.p_IdEmpresa, mensajeGeneracionArchivo).msg();
			long fin = System.currentTimeMillis();
			logger.info("Fin de servicio GenerarArchivoMovimientos", fin - inicio);
		} catch (Exception e) {
			e.printStackTrace();
			long fin = System.currentTimeMillis();
			logger.info("Fin de servicio GenerarArchivoMovimientos", fin - inicio);
		}
		return salida;
	}
	
	public String generarNombreArchivo(String tipoSolicitud) throws Exception {
		logger.info("INICIO::GenerarArchivoMovimientos.generarNombreArchivo");
		long inicio = System.currentTimeMillis();
		String nombreArchivo = "";

		if ("APO".equalsIgnoreCase(tipoSolicitud)) {
			nombreArchivo = "AMFMAP";			
		} else if ("RES".equalsIgnoreCase(tipoSolicitud)) {
			nombreArchivo = "AMFMRP";			
		} else {
		    throw new Exception("Valor invalido Tipo Solicitud: " + tipoSolicitud);
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarArchivoMovimientos.generarNombreArchivo", fin - inicio);
		return nombreArchivo;
	}
	
	public List<MovimientoFMM> valorizarSolicitud(Connection con, Long idEmpresa, List<MovimientoFMM> solicitudes) throws Exception{
		logger.info("INICIO::GenerarArchivoMovimientos.valorizarSolicitud");
		long inicio = System.currentTimeMillis();
		
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = idEmpresa;
		
		entradaCalculo.p_Series = solicitudes.parallelStream()
		.filter(solicitud->{
			return esSolicitudPendienteCertificacion(solicitud.Estado);
		})
		.map(solicitud->{
			CalcularMontoNumeroCuota.Serie calculoMontoCuota = new CalcularMontoNumeroCuota.Serie();
			calculoMontoCuota.IdInterno = solicitud.IdSolicitudAporteRescate;
			calculoMontoCuota.Fecha = solicitud.FechaValorCuota;
			calculoMontoCuota.IdSerieFondo = solicitud.IdSerieFondo;
			calculoMontoCuota.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
			calculoMontoCuota.Monto = solicitud.Monto;
		 	calculoMontoCuota.NumeroCuotas = solicitud.NumeroCuotas;
		 	calculoMontoCuota.IdMonedaFondo = solicitud.IdMonedaFondo;
		 	calculoMontoCuota.IdMonedaInversion = solicitud.IdMoneda;
		 	calculoMontoCuota.TipoSolicitud =  solicitud.TipoSolicitud;
		 	calculoMontoCuota.TipoFactorAjuste = _c.UF;
		 	calculoMontoCuota.IdEmpresa = idEmpresa;
		 	calculoMontoCuota.IdTipoMovimientoFondoMutuo =solicitud.IdTipoMovimientoFondoMutuo == null ? "":solicitud.IdTipoMovimientoFondoMutuo;
		 	return calculoMontoCuota;
		}).collect(Collectors.toList());
		
		SalidaCalcularMontoNumeroCuota salidaCalculoMontoNumeroCuota = calculo.logica(con, entradaCalculo);
		
		Map<Long,CalcularMontoNumeroCuota.Resultado> montosValorizados = salidaCalculoMontoNumeroCuota.P_OUT_RESULTADO.parallelStream().collect(Collectors.toMap(resultadoCalculo->resultadoCalculo.IdInterno, resultadoCalculo->resultadoCalculo));
		
		solicitudes.parallelStream()
		.filter(solicitud->{
			return esSolicitudPendienteCertificacion(solicitud.Estado);
		})
		.forEach(solicitud->{
			CalcularMontoNumeroCuota.Resultado resultadoValorizacion = montosValorizados.get(solicitud.IdSolicitudAporteRescate);
			if(esValorizacionCorrecta(resultadoValorizacion)) {
				solicitud.Monto = resultadoValorizacion.Monto;
				solicitud.NumeroCuotas = resultadoValorizacion.NumeroCuotas;
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarArchivoMovimientos.valorizarSolicitud", fin - inicio);
		return solicitudes;
	}
	
	public Boolean esSolicitudPendienteCertificacion(String EstadoSolicitud) {
		logger.info("INICIO::GenerarArchivoMovimientos.esSolicitudPendienteCertificacion");
		long inicio = System.currentTimeMillis();
		Boolean esSolicitudPendienteCertificacion = _c.EstadoIngresado.equals(EstadoSolicitud) || _c.EstadoPendiente.equals(EstadoSolicitud);
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarArchivoMovimientos.esSolicitudPendienteCertificacion", fin - inicio);
		return esSolicitudPendienteCertificacion;
	}

	public Boolean esValorizacionCorrecta(CalcularMontoNumeroCuota.Resultado resultadoValorizacion) {
		logger.info("INICIO::GenerarArchivoMovimientos.esValorizacionCorrecta");
		long inicio = System.currentTimeMillis();
		Boolean esValorizacionCorrecta = resultadoValorizacion != null;
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarArchivoMovimientos.esValorizacionCorrecta", fin - inicio);
		return esValorizacionCorrecta;
	}

	public List<MovimientoFMM> homologar(List<MovimientoFMM> movimientos, List<Homologacion> homologar) throws Exception {	
		logger.info("INICIO::GenerarArchivoMovimientos.homologar");
		long inicio = System.currentTimeMillis();
		
		Map<String,Homologacion> hashIndicadorAporteRescate = homologar.stream().filter(hom-> "IndicadorAporteRescate".equals(hom.TipoValor)).collect(Collectors.toMap(hom -> hom.TipoValor+hom.ValorOrigen+hom.ValorOrigen2+hom.ValorOrigen3, hom->hom));
		Map<String,Homologacion> hashCodigoMovimiento = homologar.stream().filter(hom-> "CodigoMovimiento".equals(hom.TipoValor)).collect(Collectors.toMap(hom -> hom.TipoValor+hom.ValorOrigen+hom.ValorOrigen2+hom.ValorOrigen3, hom->hom));
		Map<String,Homologacion> hashTipoOperacion = homologar.stream().filter(hom-> "TipoOperacion".equals(hom.TipoValor)).collect(Collectors.toMap(hom -> hom.TipoValor+hom.ValorOrigen+hom.ValorOrigen2+hom.ValorOrigen3, hom->hom));
		Map<String,Homologacion> hashMedioPago = homologar.stream().filter(hom-> "IdMedioDePago".equals(hom.TipoValor)).collect(Collectors.toMap(hom -> hom.TipoValor+hom.ValorOrigen+hom.ValorOrigen2+hom.ValorOrigen3, hom->hom));
		
		movimientos = movimientos.stream().map(mov -> {
			Homologacion indApoRes = hashIndicadorAporteRescate.get("IndicadorAporteRescate"+mov.IndicadorAporteRescate+mov.IdMoneda);
			mov.HomologacionIndicadorAporteRescate = indApoRes != null ? indApoRes.ValorDestino : "";
			Homologacion indCodMov = hashCodigoMovimiento.get("CodigoMovimiento"+mov.HomologacionIndicadorAporteRescate);
			mov.HomologacionCodigoMovimiento = indCodMov != null ? indCodMov.ValorDestino : "";
			Homologacion indTipoOpe = hashTipoOperacion.get("TipoOperacion"+mov.HomologacionIndicadorAporteRescate);
			mov.HomologacionTipoOperacion = indTipoOpe != null ? indTipoOpe.ValorDestino : "";
			Homologacion indMedioPago = hashMedioPago.get("IdMedioDePago"+mov.IdMedioDePago);
			mov.HomologacionMedioPago = indMedioPago != null ? indMedioPago.ValorDestino : "L";
			return mov;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarArchivoMovimientos.homologar", fin - inicio);
		return movimientos;
	}
	
	public boolean generarArchivo(String rutaArchivo, String rutaArchivoLog, List<MovimientoFMM> movimientos, long startTime) throws Exception {	
		logger.info("INICIO::GenerarArchivoMovimientos.generarArchivo");
		long inicio = System.currentTimeMillis();
		
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrectos = 0;
		String mensajeErrores = "";

		try  {
			File archivo = new File(rutaArchivo);
			File archivoLog = new File(rutaArchivoLog);
			
			BufferedWriter BwArchivo;
			BwArchivo = new BufferedWriter(new FileWriter(archivo));
			
			BufferedWriter BwArchivoLog;
			BwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
			
			if(movimientos.size() > 0) {
				
				for (MovimientoFMM movimiento : movimientos) {					

					String rut =movimiento.DocumentoIdentidad.substring(0, movimiento.DocumentoIdentidad.indexOf("-"));
					String dv = movimiento.DocumentoIdentidad.substring(movimiento.DocumentoIdentidad.indexOf("-") + 1);

					LocalDateTime  date = movimiento.FechaIngresoRegistro.toLocalDateTime();
					String fechaOperacion = date.format(DateTimeFormatter.ofPattern("yyMMdd"));
					String horaOperacion =date.format(DateTimeFormatter.ofPattern("HHmmss"));
					String fechaMovimiento=date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
					
					String campo_11="P";
					BigDecimal campo_12=movimiento.Monto; 
					if("C".equalsIgnoreCase(movimiento.CalculoPorMontoCuota)) {
						campo_11="C";
						campo_12=movimiento.NumeroCuotas;
					}
					
					BigDecimal format_campo12=campo_12.setScale(6,RoundingMode.DOWN);
					BigInteger parteEntera=format_campo12.movePointRight(6).toBigInteger();
					String montoOCuota=String.format("%017d", parteEntera);
					if(montoOCuota.length()>17) {
						montoOCuota=montoOCuota.substring(montoOCuota.length()-17);						
					}
					
					String campo_22="N";
					if("S".equalsIgnoreCase(movimiento.ValorIndicador)&& movimiento.IndicadorAporteRescate.equalsIgnoreCase("APO")) {
						campo_22="D";
					}
					
					String campo_24=movimiento.IndicadorAporteRescate.equalsIgnoreCase("APO")?"N":((movimiento.IndicadorAporteRescate.equalsIgnoreCase("RES") && movimiento.CalculoPorMontoCuota.equalsIgnoreCase("T"))?"S":movimiento.CalculoPorMontoCuota);
					
					String campo_30=movimiento.Estado.equalsIgnoreCase("PEN")?"P":(movimiento.Estado.equalsIgnoreCase("ING")?"I":(movimiento.Estado.equalsIgnoreCase("CRT")?"T":""));
					
					String linea="";
					linea += "00"; 																							//CodigoFondo_01
					linea += String.format("%2s", movimiento.HomologacionTipoOperacion).substring(0, 2); 					//CodigoMovimiento_02
					linea += String.format("%8s", movimiento.IdSolicitudAporteRescate).replace(" ", "0").substring(0, 8); 	//SecuenciaMovimiento_03
					linea += String.format("%9s",rut).replace(" ", "0").substring(0, 9);  									//RutCliente_04
					linea += String.format("%1s",dv).replace(" ", "0").substring(0, 1); 									//DigitoVerificador_05
					linea += "000000000 "; 																					//Ceros_06
					linea += String.format("%2s",movimiento.CodigoPortafolio).replace(" ", "0").substring(0, 2); 			//Portafolio_07
					linea += String.format("%6s",fechaOperacion).replace(" ", "0").substring(0, 6); 						//FechaOperacion_08
					linea += String.format("%3s","002").substring(0, 3);													//CeroDos_09
					linea += String.format("%-4s",movimiento.CodigoEjecutivo);												//CodigoAgente_10
					linea += String.format("%1s", campo_11).substring(0, 1); 												//MontoCuotas_11
					linea += montoOCuota;																					//MontoCantidad_12
					linea += String.format("%3s",movimiento.DiasRetenciones).replace(' ', '0').substring(0, 3); 			//DiasRetencion_13
					linea += String.format("%1s","0").substring(0, 1);														//AporteRescate_14
					linea += String.format("%2s","00").substring(0, 2);														//Ceros_15
					linea += movimiento.HomologacionCodigoMovimiento;														//Campo_16
					linea += String.format("%4s","    ");																	//CodigoBanco_17
					linea += String.format("%15s","               ");									    				//CuentaBanco_18
					linea += String.format("%1s","F");																		//Horario_19
					linea += String.format("%11s","00000000000").replace(' ', '0'); 										//MontoComision_20
					linea += String.format("%4s","0000");																	//PorcentajeComision_21
					linea += String.format("%1s",campo_22).substring(0, 1);													//InversionistaCalificado_22
					linea += "1";																							//Uno_23
					linea += String.format("%1s",campo_24).substring(0, 1);													//Campo_24
					linea += String.format("%6s",horaOperacion).substring(0, 6);									    	//HoraOperacion_25
					linea += String.format("%3s","   ");																	//SerieFondo_26
					linea += fechaMovimiento;																				//FechaMovimiento_27
					linea += String.format("%10s",movimiento.FechaLiquidacion).substring(0, 10);							//FechaLiquidacion_28
					linea += String.format("%-10.10s",movimiento.IdSerieFondo).substring(0, 10);							//Nemotecnico_29
					linea += String.format("%1s",campo_30).substring(0, 1);													//EstadoMovimiento_30
					linea += String.format("%1s","I"); 																		//Estado_31
					linea += String.format("%4s",movimiento.HomologacionIndicadorAporteRescate).substring(0, 4);			//CodigoMovimiento_32
					linea += String.format("%1s",movimiento.HomologacionMedioPago);																		//PagoCobro_33
					linea += String.format("%4s","    ");;																	//CodigoBancoPago_34
					linea += String.format("%20s","                    ");													//CuentaCorrienteCliente_35
					linea += String.format("%4s","964 ");									    							//CodigoSucursalAgente_36
					linea += String.format("%1s"," ");																		//ReenversionTransferencia_37
					linea += String.format("%10s","          ");															//FondoDestino_38
					linea += String.format("%8s","00000000");																//FolioOrigen_39
					
					numeroRegistros++;
					if(linea.length() != LARGO_LINEA) {
						numeroRegistrosErroneos++;
						mensajeErrores += "Registro "+numeroRegistros+" con error de formato. \n";
					} else {
						numeroRegistrosCorrectos++;
					}
					
					if(numeroRegistros < movimientos.size()) {
						linea += "\r\n";		
					}
					BwArchivo.write(linea);
				}
			}
			
			BwArchivo.close();
			
			//LOG
			SimpleDateFormat fm=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String fecha = fm.format(new Date());
			long endTime = System.currentTimeMillis();
			String fechaFin = fm.format(new Date());
			String fechaInicio = "Fecha Hora Inicio: " + fecha;
			String fechaTermino = "Fecha Hora Termino: " + fechaFin;
			String tiempoEjecucion = "Tiempo de ejecucion: " + (endTime - startTime);
			String numeroRegistroString = "Cantidad de registros: " + numeroRegistros;
			String numeroRegistrosCorrectosString = "Cantidad de registros correctos: " + numeroRegistrosCorrectos;
			String numeroRegistrosErroneosString = "Cantidad de registros con errores: "+ numeroRegistrosErroneos;
			
			BwArchivoLog.write(fechaInicio);
			BwArchivoLog.newLine();
			BwArchivoLog.write(fechaTermino);
			BwArchivoLog.newLine();
			BwArchivoLog.write(tiempoEjecucion);
			BwArchivoLog.newLine();
			BwArchivoLog.write(numeroRegistroString);
			BwArchivoLog.newLine();
			BwArchivoLog.write(numeroRegistrosCorrectosString);
			BwArchivoLog.newLine();
			BwArchivoLog.write(numeroRegistrosErroneosString);
			BwArchivoLog.newLine();
			if(!mensajeErrores.isEmpty()) {
				BwArchivoLog.write(mensajeErrores);
				BwArchivoLog.newLine();
			}
			BwArchivoLog.write("Ruta Archivo Salida: " + rutaArchivo);
			BwArchivoLog.close();
			
			long fin = System.currentTimeMillis();
			logger.info("FIN::GenerarArchivoMovimientos.generarArchivo", fin - inicio);
			return true;    
		}catch(Exception e) {
			logger.error("INFO::GenerarArchivoMovimientos.generarArchivo - Error en la creacion de Archivo de movimientos");
			logger.error(e.getMessage());
			long fin = System.currentTimeMillis();
			logger.info("FIN::GenerarArchivoMovimientos.generarArchivo", fin - inicio);
			return false;
		}
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties"))
		{
			System.out.println("============INICIO============");
			Gson gson = Utils.getGsonBuilder();
	
			String json = "{\r\n"
					+ "	\"p_IdEmpresa\": 1,\r\n"
					+ "	\"p_IdUsuario\": 9784,\r\n"
					+ "	\"p_IdModulo\": \"FFMM\",\r\n"
					+ "	\"p_Ruta\": \"C:\\\\ARCHIVO_PRUEBA\\\\\",\r\n"
					+ "	\"p_FechaCurse\": \"2026-03-31\",\r\n"
					+ "	\"p_TipoSolicitud\": \"RES\",\r\n"
					+ "	\"p_Moneda\": \"CLP\",\r\n"
					+ "	\"p_FondoMutuo\": \"\",\r\n"
					+ "	\"p_Emisor\": \"\",\r\n"
					+ "	\"p_EstadoMovimiento\": \"CRT\"\r\n"
					+ "}"; 
			GenerarArchivoMovimientos_Entrada Objin = gson.fromJson(json, GenerarArchivoMovimientos_Entrada.class);
			GenerarArchivoMovimientos demo = new GenerarArchivoMovimientos();
			try 
			{
				GenerarArchivoMovimientos_Salida ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut);
				
				System.out.println("============FIN============");
			}catch(Exception er) {
				er.printStackTrace();
				logger.error(er.getMessage());
				con.rollback();
			}
		}	
    }
}
