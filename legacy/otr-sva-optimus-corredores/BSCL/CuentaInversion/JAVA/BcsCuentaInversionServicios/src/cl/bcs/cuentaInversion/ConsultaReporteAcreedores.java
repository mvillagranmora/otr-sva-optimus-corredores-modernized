package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaReporteAcreedores implements JavaCall{

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultaReporteAcreedores jin = gson.fromJson(in.toString(),EntradaConsultaReporteAcreedores.class);
		SalidaConsultaReporteAcreedores out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private final static Logger logger = LogManager.getLogger(ConsultaReporteAcreedores.class);
	
	private final String SQL_INSERT_CUENTA_INVERSION_PASO	= loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaReporteAcreedores_InsertCuentaInversionTablaDePaso.sql");
	private final String SQL_SELECT_DATOS_CLIENTE			= loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaReporteAcreedores_SelectDatosCliente.sql");
	private final String SQL_SELECT_ABONOS					= loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaReporteAcreedores_SelectAbonos.sql");
	private final String SQL_DELETE_CUENTA_INVERSION_PASO	= loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaReporteAcreedores_DeleteCuentaInversionTablaDePaso.sql");
	
	private final String Intermediacion = "I";
	
	public static class EntradaConsultaReporteAcreedores extends BcsPojo{
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public String	p_FechaConsulta;
		public String	p_IdMoneda;
		public Long		p_IdEjecutivo;
		public String	p_Consolidar;
		public EntradaConsultaReporteAcreedores() {
			this.p_IdEjecutivo			= 0L;
			this.p_Consolidar			= "S";
			this.p_IdMoneda				= "";
		}
	}
	
	public static class SalidaConsultaReporteAcreedores extends BcsPojo{
		public Long	P_OUT_INFORMACIONCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public Long P_OUT_ERRORESCONTADOR;
		public ArrayList<MensajeSalida>				P_OUT_MENSAJES;
		public ArrayList<SalidaReporteAcreedores>	P_OUT_TABLA;
		public SalidaConsultaReporteAcreedores () {
			this.P_OUT_ERRORESCONTADOR =  0L;
			this.P_OUT_TABLA	= new ArrayList<SalidaReporteAcreedores>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class SalidaReporteAcreedores extends BcsPojo{
		public Long			IdPortafolio;
		public String		CodigoPortafolio;
		public String		DocumentoIndentidad;
		public String		NombreCliente;
		public BigDecimal	MontoPorPagarTotal;
		public BigDecimal	TramoA; //Vigente 			
		public BigDecimal	TramoB; //3 a 10 dias			
		public BigDecimal	TramoC; //11 a 30 dias		
		public BigDecimal	TramoD; //mayores a 30 dias	
		public String		Observacion;
		public String		ClasificacionContable;
		public String		Relacionado;
		public BigDecimal	CampoBD;
		public SalidaReporteAcreedores(){
			this.MontoPorPagarTotal = BigDecimal.ZERO;
			this.TramoA				= BigDecimal.ZERO;
			this.TramoB				= BigDecimal.ZERO;
			this.TramoC				= BigDecimal.ZERO;
			this.TramoD				= BigDecimal.ZERO;
		}
	}
	
	public static class FechaBusqueda extends BcsPojo{
		public LocalDate FechaInicio;
		public LocalDate FechaFin;
	}
	
	public static class ClientesConsolidado extends BcsPojo{
		public Long			IdCliente;
		public BigDecimal	SaldoFinal;
		public BigDecimal	SaldoInicial;
		public List<Long>	IdCuentasInversion;
		public List<Long>	IdPortafolios;
		public Date 		FechaSaldoInicial;
		public ClientesConsolidado() {
			IdCuentasInversion = new ArrayList<Long>();
			IdPortafolios = new ArrayList<Long>();
			SaldoInicial = BigDecimal.ZERO;
			SaldoFinal = BigDecimal.ZERO;
		}
	}
	
	public static class ClientePortafolio extends BcsPojo{
		public Long IdPersona;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public Long IdCliente;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String Relacionado;
	}
	
	public static class CuentaInversion extends BcsPojo{
		public Long IdCuentaInversion;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal SaldoFinal;
		public BigDecimal SaldoInicial;
		public Date FechaSaldoInicial;
		public String Observacion;
		public CuentaInversion() {
			this.SaldoFinal = BigDecimal.ZERO;
			this.SaldoInicial  = BigDecimal.ZERO;
			this.Observacion = "";
		}
	}
	
	public static class MovimientoAbono extends BcsPojo{
		public Long			IdCargoAbonoMovimiento;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String		IdMoneda;
		public Long			IdCuentaInversion;
		public BigDecimal	Monto;
		public String 		TipoOperacion;
		public Date			FechaMovimiento;
		public String		Observacion;
		public Timestamp	FechaCertificacion;
		public Date			FechaEfectivaMovimiento;
		
		public MovimientoAbono() {
			this.Observacion = "";
		}
		
		public Date getFechaEfectivaMovimiento() {
			return FechaEfectivaMovimiento;
		}
		
		public Timestamp getFechaCertificacion() {
			return this.FechaCertificacion;
		}
		
		public Long getIdCargoAbonoMovimiento() {
			return this.IdCargoAbonoMovimiento;
		}
	}
	
	public static class LimpiarTablaPaso extends BcsPojo{
		public Long	IdTrabajo;
	}
	
	public SalidaConsultaReporteAcreedores logica (Connection con, EntradaConsultaReporteAcreedores in) throws Exception {
		logger.info("INICIO::ConsultaReporteAcreedores");
		
		SalidaConsultaReporteAcreedores out = new SalidaConsultaReporteAcreedores();
		
		SQL sql = new SQL();
		Long idTrabajo = (long) (Math.random()*10000000000L);
		
		Long idPaisEmpresa = obtenerIdPaisEmpresa(con, in.p_IdEmpresa);
		String idMonedaSistema = obtenerMonedaSistema(con, in.p_IdEmpresa);
		
		if(in.p_IdMoneda == null || in.p_IdMoneda.isEmpty()) {
			in.p_IdMoneda = idMonedaSistema;
		}
		
		List<LocalDate> listaFechas = new ArrayList<>();
		
		Map<String, FechaBusqueda> hashRangoFechas = obtenerRangosDeFechas(con, in, idPaisEmpresa,listaFechas);
		
		if(hashRangoFechas.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_RANGO_FECHAS", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		List<SaldoPorFechaEfectiva> cuentasInversion = new ArrayList<>();
		
		List<ClientesConsolidado> clientesConsolidado = new ArrayList<>();
		
		if("S".equalsIgnoreCase(in.p_Consolidar)){
			cuentasInversion = obtenerCuentasInversion(con, in, in.p_IdMoneda, clientesConsolidado);
		}else {
			cuentasInversion = obtenerCuentasInversionPositivas(con, in, in.p_IdMoneda);
		}
		
		if(cuentasInversion.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"SIN_CUENTAS_DE_INVERSION_SALDO_POSITIVO", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		//Se prepara el saldo final de las cuentas
		List<CuentaInversion> listaCuentaInversionDePaso = prepararSaldoFinalCuentaInversion(cuentasInversion, in.p_Consolidar);
		
		try {
			boolean insercionCuentaAnterioresDePaso  = insertarCuentasInversionTablaPaso(con, sql, listaCuentaInversionDePaso, idTrabajo);
			if(insercionCuentaAnterioresDePaso) {
				
				List<ClientePortafolio> listadoClientePortafolio = obtenerDatosCliente(con, sql, idTrabajo);
				
				if(listadoClientePortafolio.size() > 0) {
					
					HashMap<Long, Object> hashClientePortafolio = Utils.List2Hash(listadoClientePortafolio,"IdPortafolio");
					
					List <MovimientoAbono> abonos = obtenerAbonos(con, sql, in.p_FechaConsulta, idTrabajo);
					
					if(abonos.size() > 0) {
						
						List<SalidaReporteAcreedores> preSalidaServicio = new ArrayList<>();
						
						if("S".equalsIgnoreCase(in.p_Consolidar)) {
							for(ClientesConsolidado cliente : clientesConsolidado) {
								ClientePortafolio clientePortafolio = (ClientePortafolio) hashClientePortafolio.get(cliente.IdPortafolios.get(0));
								
								SalidaReporteAcreedores salidaReporteAcreedoresIntermediacion = new SalidaReporteAcreedores();
								salidaReporteAcreedoresIntermediacion.DocumentoIndentidad	= clientePortafolio.DocumentoIdentidad;
								salidaReporteAcreedoresIntermediacion.NombreCliente			= clientePortafolio.NombreCompleto;
								salidaReporteAcreedoresIntermediacion.Relacionado			= clientePortafolio.Relacionado;
								salidaReporteAcreedoresIntermediacion.CampoBD				= cliente.SaldoFinal;
								salidaReporteAcreedoresIntermediacion.Observacion			= "";
								salidaReporteAcreedoresIntermediacion.ClasificacionContable	= "Cuenta de Intermediacion";
								
								SalidaReporteAcreedores salidaReporteAcreedoresGeneral = new SalidaReporteAcreedores();
								salidaReporteAcreedoresGeneral.DocumentoIndentidad		= clientePortafolio.DocumentoIdentidad;
								salidaReporteAcreedoresGeneral.NombreCliente			= clientePortafolio.NombreCompleto;
								salidaReporteAcreedoresGeneral.Relacionado				= clientePortafolio.Relacionado;
								salidaReporteAcreedoresGeneral.CampoBD					= cliente.SaldoFinal;
								salidaReporteAcreedoresGeneral.Observacion				= "";
								salidaReporteAcreedoresGeneral.ClasificacionContable	= ("S".equals(clientePortafolio.Relacionado))? "Cuentas con Personas y Empresas Relacionados"
																																	: "Cuentas por Cobrar y Cuentas de Documentos";
								
								//Obtener abonos del cliente
								List <MovimientoAbono> abonosFiltradosPorCliente = abonos.stream()
										.filter(Cargo -> Cargo.IdCliente.compareTo(cliente.IdCliente) == 0)
										.collect(Collectors.toList());
								
								if(abonosFiltradosPorCliente.size() == 0) {
									continue;
								}
								
								abonosFiltradosPorCliente.sort(Comparator.comparing(MovimientoAbono::getFechaEfectivaMovimiento).thenComparing(MovimientoAbono::getFechaCertificacion).thenComparing(MovimientoAbono::getIdCargoAbonoMovimiento).reversed());
								
								BigDecimal saldoFinal = cliente.SaldoFinal;
								
								for(MovimientoAbono movimiento:abonosFiltradosPorCliente) {
									BigDecimal saldoAuxiliar = saldoFinal.subtract(movimiento.Monto, MathContext.DECIMAL128);
									String tramo = buscarTramo(hashRangoFechas, movimiento.FechaMovimiento.toLocalDate());
									if(saldoAuxiliar.compareTo(BigDecimal.ZERO) > 0) {
										asignarHaberATramo(salidaReporteAcreedoresIntermediacion, salidaReporteAcreedoresGeneral, movimiento.Monto, tramo, movimiento.TipoOperacion);
									}else if (saldoAuxiliar.compareTo(BigDecimal.ZERO) == 0) {
										asignarHaberATramo(salidaReporteAcreedoresIntermediacion, salidaReporteAcreedoresGeneral, movimiento.Monto, tramo, movimiento.TipoOperacion);
										break;
									}else {
										BigDecimal diferencia = movimiento.Monto.add(saldoAuxiliar, MathContext.DECIMAL128);
										asignarHaberATramo(salidaReporteAcreedoresIntermediacion, salidaReporteAcreedoresGeneral, diferencia, tramo, movimiento.TipoOperacion);
										break;
									}
									saldoFinal = saldoAuxiliar;
								}
								
								darFormatoMontos(idMonedaSistema, salidaReporteAcreedoresIntermediacion, salidaReporteAcreedoresGeneral);
								
								salidaReporteAcreedoresGeneral.MontoPorPagarTotal =	salidaReporteAcreedoresGeneral.MontoPorPagarTotal
										.add(salidaReporteAcreedoresGeneral.TramoA, MathContext.DECIMAL128)
										.add(salidaReporteAcreedoresGeneral.TramoB, MathContext.DECIMAL128)
										.add(salidaReporteAcreedoresGeneral.TramoC, MathContext.DECIMAL128)
										.add(salidaReporteAcreedoresGeneral.TramoD, MathContext.DECIMAL128);

								salidaReporteAcreedoresIntermediacion.MontoPorPagarTotal = salidaReporteAcreedoresIntermediacion.MontoPorPagarTotal
												.add(salidaReporteAcreedoresIntermediacion.TramoA, MathContext.DECIMAL128)
												.add(salidaReporteAcreedoresIntermediacion.TramoB, MathContext.DECIMAL128)
												.add(salidaReporteAcreedoresIntermediacion.TramoC, MathContext.DECIMAL128)
												.add(salidaReporteAcreedoresIntermediacion.TramoD, MathContext.DECIMAL128);
								
								if(salidaReporteAcreedoresIntermediacion.MontoPorPagarTotal.compareTo(BigDecimal.ZERO) == 1) {
									preSalidaServicio.add(salidaReporteAcreedoresIntermediacion);
								}
								if(salidaReporteAcreedoresGeneral.MontoPorPagarTotal.compareTo(BigDecimal.ZERO) == 1) {
									preSalidaServicio.add(salidaReporteAcreedoresGeneral);
								}
							}
							out.P_OUT_TABLA.addAll(preSalidaServicio);
						}else {
							for(CuentaInversion ctaInver : listaCuentaInversionDePaso) {
								ClientePortafolio clientePortafolio = (ClientePortafolio) hashClientePortafolio.get(ctaInver.IdPortafolio);
								
								SalidaReporteAcreedores salidaReporteAcreedoresIntermediacion = new SalidaReporteAcreedores();
								salidaReporteAcreedoresIntermediacion.IdPortafolio			= ctaInver.IdPortafolio;
								salidaReporteAcreedoresIntermediacion.DocumentoIndentidad	= clientePortafolio.DocumentoIdentidad;
								salidaReporteAcreedoresIntermediacion.NombreCliente			= clientePortafolio.NombreCompleto;
								salidaReporteAcreedoresIntermediacion.Relacionado			= clientePortafolio.Relacionado;
								salidaReporteAcreedoresIntermediacion.CampoBD				= ctaInver.SaldoFinal;
								salidaReporteAcreedoresIntermediacion.Observacion			= ctaInver.Observacion == null ? "":ctaInver.Observacion;
								salidaReporteAcreedoresIntermediacion.ClasificacionContable	= "Cuenta de Intermediacion";
								
								SalidaReporteAcreedores salidaReporteAcreedoresGeneral = new SalidaReporteAcreedores();
								salidaReporteAcreedoresGeneral.IdPortafolio				= ctaInver.IdPortafolio;
								salidaReporteAcreedoresGeneral.DocumentoIndentidad		= clientePortafolio.DocumentoIdentidad;
								salidaReporteAcreedoresGeneral.NombreCliente			= clientePortafolio.NombreCompleto;
								salidaReporteAcreedoresGeneral.Relacionado				= clientePortafolio.Relacionado;
								salidaReporteAcreedoresGeneral.CampoBD					= ctaInver.SaldoFinal;
								salidaReporteAcreedoresGeneral.Observacion				= ctaInver.Observacion == null ? "":ctaInver.Observacion;
								salidaReporteAcreedoresGeneral.ClasificacionContable	= ("S".equals(clientePortafolio.Relacionado))? "Cuentas con Personas y Empresas Relacionados"
																																	: "Cuentas por Cobrar y Cuentas de Documentos";
								//Obtener abonos por cuenta inversion
								List <MovimientoAbono> abonosFiltradosPorCuentaInversion = abonos.stream()
										.filter(Abono -> Abono.IdCuentaInversion.compareTo(ctaInver.IdCuentaInversion) == 0)
										.collect(Collectors.toList());
								
								if(abonosFiltradosPorCuentaInversion.size() == 0) {
									continue;
								}
								
								abonosFiltradosPorCuentaInversion.sort(Comparator.comparing(MovimientoAbono::getFechaEfectivaMovimiento).thenComparing(MovimientoAbono::getFechaCertificacion).thenComparing(MovimientoAbono::getIdCargoAbonoMovimiento).reversed());
								
								BigDecimal saldoFinal = ctaInver.SaldoFinal;
								
								for(MovimientoAbono movimiento:abonosFiltradosPorCuentaInversion) {
									BigDecimal saldoAuxiliar = saldoFinal.subtract(movimiento.Monto, MathContext.DECIMAL128);
									String tramo = buscarTramo(hashRangoFechas, movimiento.FechaMovimiento.toLocalDate());
									if(saldoAuxiliar.compareTo(BigDecimal.ZERO) > 0) {
										asignarHaberATramo(salidaReporteAcreedoresIntermediacion, salidaReporteAcreedoresGeneral, movimiento.Monto, tramo, movimiento.TipoOperacion);
									}else if (saldoAuxiliar.compareTo(BigDecimal.ZERO) == 0) {
										asignarHaberATramo(salidaReporteAcreedoresIntermediacion, salidaReporteAcreedoresGeneral, movimiento.Monto, tramo, movimiento.TipoOperacion);
										break;
									}else {
										BigDecimal diferencia = movimiento.Monto.add(saldoAuxiliar, MathContext.DECIMAL128);
										asignarHaberATramo(salidaReporteAcreedoresIntermediacion, salidaReporteAcreedoresGeneral, diferencia, tramo, movimiento.TipoOperacion);
										break;
									}
									saldoFinal = saldoAuxiliar;
								}
								
								darFormatoMontos(idMonedaSistema, salidaReporteAcreedoresIntermediacion, salidaReporteAcreedoresGeneral);
								
								salidaReporteAcreedoresGeneral.MontoPorPagarTotal =	salidaReporteAcreedoresGeneral.MontoPorPagarTotal
										.add(salidaReporteAcreedoresGeneral.TramoA, MathContext.DECIMAL128)
										.add(salidaReporteAcreedoresGeneral.TramoB, MathContext.DECIMAL128)
										.add(salidaReporteAcreedoresGeneral.TramoC, MathContext.DECIMAL128)
										.add(salidaReporteAcreedoresGeneral.TramoD, MathContext.DECIMAL128);

								salidaReporteAcreedoresIntermediacion.MontoPorPagarTotal = salidaReporteAcreedoresIntermediacion.MontoPorPagarTotal
												.add(salidaReporteAcreedoresIntermediacion.TramoA, MathContext.DECIMAL128)
												.add(salidaReporteAcreedoresIntermediacion.TramoB, MathContext.DECIMAL128)
												.add(salidaReporteAcreedoresIntermediacion.TramoC, MathContext.DECIMAL128)
												.add(salidaReporteAcreedoresIntermediacion.TramoD, MathContext.DECIMAL128);
								
								if(salidaReporteAcreedoresIntermediacion.MontoPorPagarTotal.compareTo(BigDecimal.ZERO) == 1) {
									preSalidaServicio.add(salidaReporteAcreedoresIntermediacion);
								}
								if(salidaReporteAcreedoresGeneral.MontoPorPagarTotal.compareTo(BigDecimal.ZERO) == 1) {
									preSalidaServicio.add(salidaReporteAcreedoresGeneral);
								}
							}
							out.P_OUT_TABLA.addAll(preSalidaServicio);
						}
						
					}else {
						out.P_OUT_ERRORESCONTADOR ++;
						out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_AL_OBTENER_ABONOS", new HashMap<>(), con, in.p_IdEmpresa));
					}
					
				}else {
					out.P_OUT_ERRORESCONTADOR ++;
					out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_AL_OBTENER_DATOS_CLIENTE", new HashMap<>(), con, in.p_IdEmpresa));
				}
				
			}else {
				out.P_OUT_ERRORESCONTADOR ++;
				out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_AL_INSERTAR_TABLA_TEMPORAL", new HashMap<>(), con, in.p_IdEmpresa));
			}
		}catch(Exception e) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_AL_GENERAR_REPORTE", new HashMap<>(), con, in.p_IdEmpresa));
			logger.error(e.getMessage());
		}finally{
			boolean eliminaCuentaAnterioresDePaso = eliminarCuentasInversionTablaPaso(con, sql,idTrabajo);
			if(!eliminaCuentaAnterioresDePaso) {
				out.P_OUT_ERRORESCONTADOR ++;
				out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_AL_LIMPIAR_TABLA_TEMPORAL", new HashMap<>(), con, in.p_IdEmpresa));
			}
		}
		
		logger.info("FIN::ConsultaReporteAcreedores");
		return out;
	}
	
	private Long obtenerIdPaisEmpresa(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ConsultaReporteAcreedores.obtenerIdPaisEmpresa");
		ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresa.logica(con, idEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		logger.trace("FIN::ConsultaReporteAcreedores.obtenerIdPaisEmpresa");
		return idPaisEmpresa;
	}
	
	private String obtenerMonedaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultaReporteAcreedores.obtenerMonedaSistema");
		long inicio = System.currentTimeMillis();
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAcreedores.obtenerMonedaSistema", fin - inicio);
		return idMonedaSistema;
	}
	
	private Map<String, FechaBusqueda> obtenerRangosDeFechas(Connection con, EntradaConsultaReporteAcreedores in, Long IdPais, List<LocalDate> listaFechas ) throws Exception{
		logger.trace("INICIO::ConsultaReporteAcreedores.obtenerRangosDeFechas");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, FechaBusqueda> hashRangosFechas = new HashMap<>(3);
		
		ObtenerDiaAnterior obteDiaAnterior = new ObtenerDiaAnterior();
		ObtenerDiaAnterior.InObtenerDiaAnterior entradaObteDiaAnterior = new ObtenerDiaAnterior.InObtenerDiaAnterior();
		entradaObteDiaAnterior.idPais = IdPais;
		entradaObteDiaAnterior.diaHabil = "N";
		entradaObteDiaAnterior.soloCaeDiaHabil = "N";
		
		long diasRetroceso = 31;
		for(long i = 0; i <= diasRetroceso; i++) {
			ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn f = new ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn(in.p_FechaConsulta, i);
			entradaObteDiaAnterior.fechas.add(f);
		}
		
		ObtenerDiaAnterior.OutObtenerDiaAnterior salidaObteDiaAnterior = obteDiaAnterior.logica(con, entradaObteDiaAnterior);
		
		List<String> primerTramofechas = new ArrayList<String>(); 	//RANGO 0 a 2 dias
		List<String> segundoTramofechas = new ArrayList<String>(); 	//RANGO 3 a 10 dias
		List<String> tercerTramofechas = new ArrayList<String>();	//RANGO 11 a 30 dias 
		List<String> cuartoTramofechas = new ArrayList<String>();	//RANGO superior a 30 dias
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		if((salidaObteDiaAnterior.erroresContador.compareTo(0L)==0) && !salidaObteDiaAnterior.fechas.isEmpty()) {
			//Se agrupan las fechas en los tramos
			for(ObtenerDiaAnterior.OutObtenerDiaAnterior.FechasOut f : salidaObteDiaAnterior.fechas) {
				if(f.numeroDias!=null) {
					int diaSegunFecha = (int) f.numeroDiasInicial.shortValue();
					listaFechas.add(LocalDate.parse(f.fechaCalculada, formatter));
					if(diaSegunFecha >= 0 && diaSegunFecha <= 2 ) primerTramofechas.add(f.fechaCalculada);
					else if(diaSegunFecha > 2 && diaSegunFecha <= 10) segundoTramofechas.add(f.fechaCalculada);
					else if(diaSegunFecha >= 11 && diaSegunFecha <= 30) tercerTramofechas.add(f.fechaCalculada);
					else if(diaSegunFecha > 30) cuartoTramofechas.add(f.fechaCalculada);
				}
			}
			
			Collections.sort(listaFechas);
		}
		else {
			return hashRangosFechas; // Hash vacio
		}
		//Armar rangos fechas
		mapearRangoDeFecha("TRAMO_A", hashRangosFechas, primerTramofechas);
		mapearRangoDeFecha("TRAMO_B", hashRangosFechas, segundoTramofechas);
		mapearRangoDeFecha("TRAMO_C", hashRangosFechas, tercerTramofechas);
		mapearRangoDeFecha("TRAMO_D", hashRangosFechas, cuartoTramofechas);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAcreedores.obtenerRangosDeFechas tiempo: " + (fin - inicio));
		return hashRangosFechas;
	}
	
	private void mapearRangoDeFecha (String rango, HashMap<String, FechaBusqueda> HashFecha ,  List<String> listaFecha){
		logger.trace("INICIO::ConsultaReporteAcreedores.mapearRangoDeFecha");
		long inicio = System.currentTimeMillis();
		
		String fechaMinima = Collections.min(listaFecha);
		String fechaMaxima = Collections.max(listaFecha);
		FechaBusqueda fechaBusq = new FechaBusqueda();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		fechaBusq.FechaInicio = LocalDate.parse(fechaMinima, formatter);
		fechaBusq.FechaFin = LocalDate.parse(fechaMaxima, formatter);
		
		HashFecha.put(rango, fechaBusq);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAcreedores.mapearRangoDeFecha tiempo: " + (fin - inicio));
	}
	
	private List<SaldoPorFechaEfectiva> obtenerCuentasInversion(Connection con, EntradaConsultaReporteAcreedores in,String moneda, List<ClientesConsolidado> clientesConsolidado) throws Exception {
		logger.debug("INICIO::ConsultaReporteAcreedores.obtenerCuentasInversion");
		long inicio = System.currentTimeMillis();
		List<SaldoPorFechaEfectiva> listaCuentasInversion = new ArrayList<>();
		
		ConsultarSaldoCuentaInversion servicioConsultarSaldoCuenta = new ConsultarSaldoCuentaInversion();
		EntradaConsultarSaldoCuentaInversion entradaConsultarSaldoCuenta = new EntradaConsultarSaldoCuentaInversion();
		entradaConsultarSaldoCuenta.p_IdEmpresa 			= in.p_IdEmpresa;
		entradaConsultarSaldoCuenta.p_IdUsuario         	= in.p_IdUsuario;
		entradaConsultarSaldoCuenta.p_IdModulo				= in.p_IdModulo;
		entradaConsultarSaldoCuenta.p_FechaDesde			= in.p_FechaConsulta;
		entradaConsultarSaldoCuenta.p_FechaHasta			= in.p_FechaConsulta;
		entradaConsultarSaldoCuenta.p_UsaReglasNegocio		= "S";
		entradaConsultarSaldoCuenta.p_IncluyeOtrosSaldos	= "S";		
		entradaConsultarSaldoCuenta.p_SoloSaldoNegativo		= "N";
		entradaConsultarSaldoCuenta.p_SoloSaldoDisponible	= "N";
		entradaConsultarSaldoCuenta.p_IdEjecutivo			= 0L;
		entradaConsultarSaldoCuenta.p_IncluyeProductoApv    = "S";
		entradaConsultarSaldoCuenta.p_IdCliente				= null;
		entradaConsultarSaldoCuenta.p_IdPortafolio			= null; 
		entradaConsultarSaldoCuenta.p_IdProductoAgrupador	= null;	
		entradaConsultarSaldoCuenta.p_IdMoneda				= moneda;
		
		List<InCuentaInversionConsultar> lstCuentaInversionConsultar = new ArrayList<InCuentaInversionConsultar>();
		InCuentaInversionConsultar objCuentaInversionConsultar = new InCuentaInversionConsultar();
		objCuentaInversionConsultar.IdEjecutivo  = in.p_IdEjecutivo;
		objCuentaInversionConsultar.IdMoneda 	 = moneda;
		objCuentaInversionConsultar.IdPortafolio = null; 
		lstCuentaInversionConsultar.add(objCuentaInversionConsultar);
		entradaConsultarSaldoCuenta.p_CuentaInversionAConsultar = lstCuentaInversionConsultar;
		List<SaldoPorFechaEfectiva> listaCuentasInversionPositivas = new ArrayList<>();
		
		SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuenta = servicioConsultarSaldoCuenta.logica(con, entradaConsultarSaldoCuenta);
		if(salidaConsultarSaldoCuenta.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0 && !salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty()){
			
			//Si la moneda es CLP se debe truncar el Saldo disponible
			if("CLP".equalsIgnoreCase(moneda)) {
				List<SaldoPorFechaEfectiva> cuentasInversionPositivas = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
						.filter(Saldo-> Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128).setScale(0, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) == 1)
						.collect(Collectors.toList());
				
				listaCuentasInversionPositivas.addAll(cuentasInversionPositivas);
			}else {
				List<SaldoPorFechaEfectiva> cuentasInversionPositivas = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
						.filter(Saldo-> Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128).setScale(2, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) == 1)
						.collect(Collectors.toList());
				
				listaCuentasInversionPositivas.addAll(cuentasInversionPositivas);
			}
			
			List<Long> idClientesConCuentasPositivas = listaCuentasInversionPositivas.parallelStream().
					map(cuenta->cuenta.IdCliente)
					.distinct()
					.collect(Collectors.toList());
			
			for(Long idCliente : idClientesConCuentasPositivas) {
				ClientesConsolidado clienteConsolidado = new ClientesConsolidado();
				List<SaldoPorFechaEfectiva> cuentasInversionCliente = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
						.filter(Saldo-> Saldo.IdCliente.equals(idCliente))
						.collect(Collectors.toList());
				BigDecimal saldoTotalCliente = cuentasInversionCliente.stream()
						.map(saldo->saldo.SaldoDisponible.add(saldo.SaldoRetenido, MathContext.DECIMAL128))
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal saldoTotalClienteAprox = BigDecimal.ZERO;
				if(moneda.equalsIgnoreCase("CLP")) {
					saldoTotalClienteAprox = saldoTotalCliente.setScale(0, RoundingMode.DOWN);
				}else {
					saldoTotalClienteAprox = saldoTotalCliente.setScale(2, RoundingMode.HALF_EVEN);
				}
				if(saldoTotalClienteAprox.compareTo(BigDecimal.ZERO) == 1) {
					clienteConsolidado.IdCliente = idCliente;
					clienteConsolidado.SaldoFinal = saldoTotalCliente;
					List<Long> idCuentasInversionCliente = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
							.filter(cuenta ->cuenta.IdCliente.equals(idCliente))
							.map(cuenta->cuenta.IdCuentaInversion)
							.distinct()
							.collect(Collectors.toList());
					
					List<Long> idPortafoliosCliente = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
							.filter(cuenta ->cuenta.IdCliente.equals(idCliente))
							.map(cuenta->cuenta.IdPortafolio)
							.distinct()
							.collect(Collectors.toList());
					
					clienteConsolidado.IdCuentasInversion = idCuentasInversionCliente;
					clienteConsolidado.IdPortafolios= idPortafoliosCliente;
					clientesConsolidado.add(clienteConsolidado);
					listaCuentasInversion.addAll(cuentasInversionCliente);					
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAcreedores.obtenerCuentasInversion tiempo: " + (fin - inicio));
		
		return listaCuentasInversion;
	}
	
	private List<SaldoPorFechaEfectiva> obtenerCuentasInversionPositivas(Connection con, EntradaConsultaReporteAcreedores in,String moneda) throws Exception {
		logger.trace("INICIO::ConsultaReporteAcreedores.obtenerCuentasInversionPositivas");
		long inicio = System.currentTimeMillis();
		List<SaldoPorFechaEfectiva> listaCuentasInversion = new ArrayList<>();
		
		ConsultarSaldoCuentaInversion servicioConsultarSaldoCuenta = new ConsultarSaldoCuentaInversion();
		EntradaConsultarSaldoCuentaInversion entradaConsultarSaldoCuenta = new EntradaConsultarSaldoCuentaInversion();
		entradaConsultarSaldoCuenta.p_IdEmpresa 			= in.p_IdEmpresa;
		entradaConsultarSaldoCuenta.p_IdUsuario         	= in.p_IdUsuario;
		entradaConsultarSaldoCuenta.p_IdModulo				= in.p_IdModulo;
		entradaConsultarSaldoCuenta.p_FechaDesde			= in.p_FechaConsulta;
		entradaConsultarSaldoCuenta.p_FechaHasta			= in.p_FechaConsulta;
		entradaConsultarSaldoCuenta.p_UsaReglasNegocio		= "S";
		entradaConsultarSaldoCuenta.p_IncluyeOtrosSaldos	= "S";		
		entradaConsultarSaldoCuenta.p_SoloSaldoNegativo		= "N";
		entradaConsultarSaldoCuenta.p_SoloSaldoDisponible	= "N";
		entradaConsultarSaldoCuenta.p_IdEjecutivo			= 0L;
		entradaConsultarSaldoCuenta.p_IncluyeProductoApv    = "S";
		entradaConsultarSaldoCuenta.p_IdCliente				= null;
		entradaConsultarSaldoCuenta.p_IdPortafolio			= null; 
		entradaConsultarSaldoCuenta.p_IdProductoAgrupador	= null;	
		entradaConsultarSaldoCuenta.p_IdMoneda				= moneda;
		
		List<InCuentaInversionConsultar> lstCuentaInversionConsultar = new ArrayList<InCuentaInversionConsultar>();
		InCuentaInversionConsultar objCuentaInversionConsultar = new InCuentaInversionConsultar();
		objCuentaInversionConsultar.IdEjecutivo  = in.p_IdEjecutivo;
		objCuentaInversionConsultar.IdMoneda 	 = moneda;
		objCuentaInversionConsultar.IdPortafolio = null; 
		lstCuentaInversionConsultar.add(objCuentaInversionConsultar);
		entradaConsultarSaldoCuenta.p_CuentaInversionAConsultar = lstCuentaInversionConsultar;
		
		SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuenta = servicioConsultarSaldoCuenta.logica(con, entradaConsultarSaldoCuenta);
		
		if(salidaConsultarSaldoCuenta.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0 && !salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty()){

			//Si la moneda es CLP se debe truncar el Saldo disponible
			if("CLP".equalsIgnoreCase(moneda)) {
				List<SaldoPorFechaEfectiva> cuentasInversionPositivas = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
						.filter(Saldo-> Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128).setScale(0, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) == 1)
						.collect(Collectors.toList());
				
				listaCuentasInversion.addAll(cuentasInversionPositivas);
			}else {
				List<SaldoPorFechaEfectiva> cuentasInversionPositivas = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
						.filter(Saldo-> Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128).setScale(2, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) == 1)
						.collect(Collectors.toList());
				
				listaCuentasInversion.addAll(cuentasInversionPositivas);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAcreedores.obtenerCuentasInversionPositivas tiempo: " + (fin - inicio));
		
		return listaCuentasInversion;
	}
	
	private List<CuentaInversion> prepararSaldoFinalCuentaInversion(List<SaldoPorFechaEfectiva> SaldosCuentaInversion, String Consolidado){
		logger.trace("INICIO::ConsultaReporteAcreedores.prepararSaldoFinalCuentaInversion");
		long inicio = System.currentTimeMillis();
		
		List<CuentaInversion> listaCuentaInversionDePaso = new ArrayList<>(SaldosCuentaInversion.size());
		for(SaldoPorFechaEfectiva objCuenInversion : SaldosCuentaInversion) {
			CuentaInversion ctaInver = new CuentaInversion();
			ctaInver.IdCuentaInversion = objCuenInversion.IdCuentaInversion;
			ctaInver.IdCliente = objCuenInversion.IdCliente;
			ctaInver.IdPortafolio = objCuenInversion.IdPortafolio;
			ctaInver.IdMoneda = objCuenInversion.IdMoneda;
			if("N".equalsIgnoreCase(Consolidado)) {
				ctaInver.SaldoFinal = objCuenInversion.SaldoDisponible.add(objCuenInversion.SaldoRetenido, MathContext.DECIMAL128);
			}else {
				ctaInver.SaldoFinal = objCuenInversion.SaldoDisponible;
			}			
			listaCuentaInversionDePaso.add(ctaInver);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAcreedores.prepararSaldoFinalCuentaInversion tiempo: " + (fin - inicio));
		return listaCuentaInversionDePaso;
	}
	
	private boolean insertarCuentasInversionTablaPaso(Connection con, SQL sql, List<CuentaInversion> cuentaInversionPaso, Long idTrabajo) {
		logger.debug("INICIO::ConsultaReporteAcreedores.insertarCuentasInversionTablaPaso");
		long inicio = System.currentTimeMillis();
		boolean resultado = false;
		
		try {
			int resultadoQuery []  =  sql.batch(con, SQL_INSERT_CUENTA_INVERSION_PASO.replace(":IdTrabajo", idTrabajo+"") ,cuentaInversionPaso , CuentaInversion.class);
			if(resultadoQuery.length>0) {
				resultado = true;
			}
		}catch(SQLException SQLe) {
			logger.error(SQLe.getMessage());
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAcreedores.insertarCuentasInversionTablaPaso tiempo: " + (fin - inicio));
		
		return resultado;
	}
	
	private List<ClientePortafolio> obtenerDatosCliente(Connection con, SQL sql, Long IdTrabajo) throws SQLException{
		logger.debug("INICIO::ConsultaReporteAcreedores.obtenerClientesPorPortafolio");
		long inicio = System.currentTimeMillis();
		
		List<ClientePortafolio> listaClientes = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(1);
		param.put("IdTrabajo", IdTrabajo);
		
		listaClientes = sql.query(con, SQL_SELECT_DATOS_CLIENTE , param , ClientePortafolio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAcreedores.obtenerClientesPorPortafolio tiempo: " + (fin - inicio));
		
		return listaClientes;
	}
	
	private List<MovimientoAbono> obtenerAbonos(Connection con, SQL sql, String fechaBusqueda, Long idTrabajo) throws SQLException{
		logger.debug("INICIO::ConsultaReporteAcreedores.obtenerAbonos");
		long inicio = System.currentTimeMillis();
		
		List<MovimientoAbono> movimientos = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(2);
		param.put("IdTrabajo", idTrabajo);
		param.put("FechaBusqueda", fechaBusqueda);
		
		movimientos = sql.query(con, SQL_SELECT_ABONOS, param, MovimientoAbono.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAcreedores.obtenerAbonos tiempo: " + (fin - inicio));
		
		return movimientos;
	}
	
	private String buscarTramo(Map<String, FechaBusqueda> hashRangoFechas, LocalDate fechaConsultada) {
		logger.trace("INICIO::ConsultaReporteAcreedores.buscarTramo");
		long inicio = System.currentTimeMillis();
		String tramo = "";
		
		FechaBusqueda fechaBusqVigente 		= hashRangoFechas.get("TRAMO_A");
		FechaBusqueda fechaBusqDe3A10Dias 	= hashRangoFechas.get("TRAMO_B");
		FechaBusqueda fechaBusqDe11A30Dias 	= hashRangoFechas.get("TRAMO_C");
		FechaBusqueda fechaBusqMasDe30Dias 	= hashRangoFechas.get("TRAMO_D");
				
		//Tramo A
		if(	(fechaConsultada.isEqual(fechaBusqVigente.FechaInicio) || fechaConsultada.isAfter(fechaBusqVigente.FechaInicio)) && 
				(fechaConsultada.isEqual(fechaBusqVigente.FechaFin) || fechaConsultada.isBefore(fechaBusqVigente.FechaFin))) {
			tramo = "TRAMO_A";
		}
		//Tramo B
		else if( (fechaConsultada.isEqual(fechaBusqDe3A10Dias.FechaInicio) || fechaConsultada.isAfter(fechaBusqDe3A10Dias.FechaInicio)) && 
				(fechaConsultada.isEqual(fechaBusqDe3A10Dias.FechaFin) || fechaConsultada.isBefore(fechaBusqDe3A10Dias.FechaFin))) {
			tramo = "TRAMO_B";
		}
		//Tramo C
		else if( (fechaConsultada.isEqual(fechaBusqDe11A30Dias.FechaInicio) || fechaConsultada.isAfter(fechaBusqDe11A30Dias.FechaInicio)) && 
				(fechaConsultada.isEqual(fechaBusqDe11A30Dias.FechaFin) || fechaConsultada.isBefore(fechaBusqDe11A30Dias.FechaFin))) {
			tramo = "TRAMO_C";
		}
		//Tramo D
		else if (fechaConsultada.isEqual(fechaBusqMasDe30Dias.FechaFin) || fechaConsultada.isBefore(fechaBusqMasDe30Dias.FechaFin)){
			tramo = "TRAMO_D";
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAcreedores.buscarTramo tiempo: " + (fin - inicio));
		
		return tramo;
	}
	
	private void asignarHaberATramo(SalidaReporteAcreedores salidaReporteAcreedoresIntermediacion,SalidaReporteAcreedores salidaReporteAcreedoresGeneral, BigDecimal monto, String tramo, String tipoHaber) {
		logger.trace("INICIO::ConsultaReporteAcreedores.asignarHaberATramo");
		long inicio = System.currentTimeMillis();
		
		if(Intermediacion.equalsIgnoreCase(tipoHaber)) {
			switch(tramo) {
				case "TRAMO_A":
					salidaReporteAcreedoresIntermediacion.TramoA = salidaReporteAcreedoresIntermediacion.TramoA.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_B":
					salidaReporteAcreedoresIntermediacion.TramoB = salidaReporteAcreedoresIntermediacion.TramoB.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_C":
					salidaReporteAcreedoresIntermediacion.TramoC = salidaReporteAcreedoresIntermediacion.TramoC.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_D":
					salidaReporteAcreedoresIntermediacion.TramoD = salidaReporteAcreedoresIntermediacion.TramoD.add(monto, MathContext.DECIMAL128);
					break;
			}
		}else {
			switch(tramo) {
				case "TRAMO_A":
					salidaReporteAcreedoresGeneral.TramoA = salidaReporteAcreedoresGeneral.TramoA.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_B":
					salidaReporteAcreedoresGeneral.TramoB = salidaReporteAcreedoresGeneral.TramoB.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_C":
					salidaReporteAcreedoresGeneral.TramoC = salidaReporteAcreedoresGeneral.TramoC.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_D":
					salidaReporteAcreedoresGeneral.TramoD = salidaReporteAcreedoresGeneral.TramoD.add(monto, MathContext.DECIMAL128);
					break;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAcreedores.asignarHaberATramo tiempo: " + (fin - inicio));
	}
	
	public void darFormatoMontos(String idMoneda, SalidaReporteAcreedores salidaReporteAcreedoresIntermediacion, SalidaReporteAcreedores salidaReporteAcreedoresGeneral) {
		logger.trace("INICIO::ConsultaReporteAcreedores.darFormatoMontos");
		long inicio = System.currentTimeMillis();
		
		if("CLP".equalsIgnoreCase(idMoneda)) {
			//General
			salidaReporteAcreedoresGeneral.TramoA = salidaReporteAcreedoresGeneral.TramoA.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresGeneral.TramoB = salidaReporteAcreedoresGeneral.TramoB.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128); 
			salidaReporteAcreedoresGeneral.TramoC = salidaReporteAcreedoresGeneral.TramoC.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128); 
			salidaReporteAcreedoresGeneral.TramoD = salidaReporteAcreedoresGeneral.TramoD.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresGeneral.MontoPorPagarTotal	= salidaReporteAcreedoresGeneral.MontoPorPagarTotal.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			//Intermediacion
			salidaReporteAcreedoresIntermediacion.TramoA = salidaReporteAcreedoresIntermediacion.TramoA.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresIntermediacion.TramoB = salidaReporteAcreedoresIntermediacion.TramoB.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresIntermediacion.TramoC = salidaReporteAcreedoresIntermediacion.TramoC.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresIntermediacion.TramoD = salidaReporteAcreedoresIntermediacion.TramoD.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresIntermediacion.MontoPorPagarTotal = salidaReporteAcreedoresIntermediacion.MontoPorPagarTotal.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			
		}else
		{
			//General
			salidaReporteAcreedoresGeneral.TramoA = salidaReporteAcreedoresGeneral.TramoA.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresGeneral.TramoB = salidaReporteAcreedoresGeneral.TramoB.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresGeneral.TramoC = salidaReporteAcreedoresGeneral.TramoC.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresGeneral.TramoD = salidaReporteAcreedoresGeneral.TramoD.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresGeneral.MontoPorPagarTotal = salidaReporteAcreedoresGeneral.MontoPorPagarTotal.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			//Intermediacion
			salidaReporteAcreedoresIntermediacion.TramoA = salidaReporteAcreedoresIntermediacion.TramoA.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresIntermediacion.TramoB = salidaReporteAcreedoresIntermediacion.TramoB.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresIntermediacion.TramoC = salidaReporteAcreedoresIntermediacion.TramoC.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresIntermediacion.TramoD = salidaReporteAcreedoresIntermediacion.TramoD.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			salidaReporteAcreedoresIntermediacion.MontoPorPagarTotal = salidaReporteAcreedoresIntermediacion.MontoPorPagarTotal.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAcreedores.darFormatoMontos tiempo: " + (fin - inicio));
	}
	
	private boolean eliminarCuentasInversionTablaPaso(Connection con, SQL sql, Long idTrabajo) {
		logger.debug("INICIO::ConsultaReporteAcreedores.eliminarCuentasInversionTablaPaso");
		long inicio = System.currentTimeMillis();
		boolean resultado = false;
		
		LimpiarTablaPaso limpiarTablaPaso = new LimpiarTablaPaso();
		limpiarTablaPaso.IdTrabajo = idTrabajo;
		List<LimpiarTablaPaso> listaLimpiarTablaPaso =  Arrays.asList(limpiarTablaPaso);

		try {
			sql.batch(con, SQL_DELETE_CUENTA_INVERSION_PASO, listaLimpiarTablaPaso, LimpiarTablaPaso.class);
			resultado = true;
		}catch(SQLException SQLe) {
			logger.error(SQLe.getMessage());
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAcreedores.eliminarCuentasInversionTablaPaso tiempo: " + (fin - inicio));
		return resultado;
	}
	
}
