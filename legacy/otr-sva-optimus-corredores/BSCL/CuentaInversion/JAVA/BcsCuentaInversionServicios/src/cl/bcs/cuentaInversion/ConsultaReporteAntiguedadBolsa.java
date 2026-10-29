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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaReporteAntiguedadBolsa implements JavaCall{

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultaReporteAntiguedadBolsa jin = gson.fromJson(in.toString(), EntradaConsultaReporteAntiguedadBolsa.class);
		SalidaConsultaReporteAntiguedadBolsa out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private final static Logger logger = LogManager.getLogger(ConsultaReporteAntiguedadBolsa.class);
	
	private final String SQL_SELECT_DISTINT_MONEDA_CUENTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaReporteAntiguedadBolsa_SelectDistintasMonedasCuentasInversion.sql");
	
	private final String SQL_INSERT_CUENTA_INVERSION_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaReporteAntiguedadBolsa_InsertCuentaInversionTablaDePaso.sql");
	private final String SQL_DELETE_CUENTA_INVERSION_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaReporteAntiguedadBolsa_DeleteCuentaInversionTablaDePaso.sql");
	
	private final String SQL_SELECT_DATOS_CLIENTE = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaReporteAntiguedadBolsa_SelectDatosCliente.sql");
	
	private final String SQL_SELECT_CARGOS = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaReporteAntiguedadBolsa_SelectCargos.sql");
	
	private final String C_Moneda = "TODAS";
	private final String Intermediacion = "I";
	
	public static class EntradaConsultaReporteAntiguedadBolsa extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaConsulta;
		public String p_IdMoneda;
		public Long p_IdEjecutivo;
		public String p_SeparaIntermediacion;
		public String p_AgrupaPortafolios;
		public String p_Consolidar;
		public EntradaConsultaReporteAntiguedadBolsa() {
			p_IdEjecutivo = 0L;
			p_SeparaIntermediacion = "N";
			p_AgrupaPortafolios = "N";
			p_Consolidar = "N";
		}
	}
	
	public static class SalidaConsultaReporteAntiguedadBolsa extends BcsPojo{
		public ArrayList<MensajeSalida>								P_OUT_MENSAJES;
		public Long P_OUT_INFORMACIONCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public Long P_OUT_ERRORESCONTADOR;
		public ArrayList<SalidaReporteAntiguedadBolsa> 		P_OUT_TABLA;
		public SalidaConsultaReporteAntiguedadBolsa() {
			this.P_OUT_ERRORESCONTADOR =  0L;
			this.P_OUT_TABLA 			= new ArrayList<SalidaReporteAntiguedadBolsa>();
			this.P_OUT_MENSAJES  		= new ArrayList<MensajeSalida>();
		}
	}
	
	public static class SalidaReporteAntiguedadBolsa extends BcsPojo{
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String DocumentoIndentidad;
		public String NombreCliente;
		public BigDecimal MontoPorCobrarTotal;
		public BigDecimal TramoA; //Vigente 			
		public BigDecimal TramoB; //3 a 10 dias			
		public BigDecimal TramoC; //11 a 30 dias		
		public BigDecimal TramoD; //mayores a 30 dias	
		public String Observacion;
		public String ClasificacionContable;
		public String Relacionado;
		public BigDecimal CampoBD;
		public BigDecimal Matematica;
		public SalidaReporteAntiguedadBolsa() {
			this.MontoPorCobrarTotal = BigDecimal.ZERO;
			this.TramoA = BigDecimal.ZERO;
			this.TramoB = BigDecimal.ZERO;
			this.TramoC = BigDecimal.ZERO;
			this.TramoD = BigDecimal.ZERO;
		}
	}
	
	public static class Moneda extends BcsPojo{
		public String IdMoneda;
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
	
	public static class MovimientoCargo extends BcsPojo{
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
		
		public MovimientoCargo() {
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
	
	public SalidaConsultaReporteAntiguedadBolsa logica (Connection con, EntradaConsultaReporteAntiguedadBolsa in) throws Exception {
		logger.info("INICIO::ConsultaReporteAntiguedadBolsa");
		
		SalidaConsultaReporteAntiguedadBolsa out = new SalidaConsultaReporteAntiguedadBolsa();
		
		SQL sql = new SQL();
		Long idTrabajo = (long) (Math.random()*10000000000L);
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		
		List<Moneda> listadoMonedas = new ArrayList<Moneda>();
		
		if(in.p_IdMoneda!=null && !in.p_IdMoneda.isEmpty() && !(C_Moneda).equals(in.p_IdMoneda)) {
			Moneda moneda = new Moneda();
			moneda.IdMoneda = in.p_IdMoneda;
			listadoMonedas.add(moneda);
		}
		else {
			listadoMonedas = obtenerDistintasMonedasDesdeCuentasInversion(con, sql);
		}
		
		List<LocalDate> listaFechas = new ArrayList<>();
		
		Map<String, FechaBusqueda> hashRangoFechas = obtenerRangosDeFechas(con, in, empresa.IdPais,listaFechas);
		
		if(hashRangoFechas.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_RANGO_FECHAS", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		List<SaldoPorFechaEfectiva> cuentasInversion = new ArrayList<>();
		
		List<ClientesConsolidado> clientesConsolidado = new ArrayList<>();
		
		if(in.p_Consolidar.equalsIgnoreCase("S") && listadoMonedas.size() == 1) {
			cuentasInversion = obtenerCuentasInversion(con, in, listadoMonedas.get(0).IdMoneda, clientesConsolidado);
		}else{
			cuentasInversion = obtenerCuentasInversionNegativas(con, in, listadoMonedas);	
		}	
		
		if(cuentasInversion.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"SIN_SALDO_CUENTAS_DE_INVERSION_NEGATIVO", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		//Se prepara el saldo final de las cuentas
		List<CuentaInversion> listaCuentaInversionDePaso = prepararSaldoFinalCuentaInversion(cuentasInversion, in.p_Consolidar);
		
		try {
			boolean eliminaCuentaAnterioresDePaso = eliminarCuentasInversionTablaPaso(con, sql);
			boolean insercionCuentaAnterioresDePaso  = insertarCuentasInversionTablaPaso(con, sql, listaCuentaInversionDePaso, idTrabajo);
			if(eliminaCuentaAnterioresDePaso==false && insercionCuentaAnterioresDePaso==false) {
				out.P_OUT_ERRORESCONTADOR ++;
			}
		}catch(Exception e) {
			out.P_OUT_ERRORESCONTADOR ++;
		}finally {
			if(out.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1) {
				out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_AL_GENERAR_REPORTE", new HashMap<>(), con, in.p_IdEmpresa));
				return out;
			}
		}
		
		List<ClientePortafolio> listadoClientePortafolio = obtenerClientesPorPortafolio(con, sql, idTrabajo);
		HashSet<Long> hashIdClientes = new HashSet<Long>();
		HashMap<Long, ClientePortafolio> hashClientes = new HashMap<>();
		for(ClientePortafolio cliente :listadoClientePortafolio) {
			if(hashIdClientes.add(cliente.IdCliente) == true) {
				hashClientes.put(cliente.IdCliente, cliente);
			}
		}
		
		HashMap<String, Object> hashClientePortafolio = Utils.List2Hash(listadoClientePortafolio,"IdPortafolio");
		
		List <MovimientoCargo> cargos = obtenerCargos(con, sql, in.p_FechaConsulta, idTrabajo);
		
		if(cargos.size() == 0) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_AL_OBTENER_LOS_CARGOS", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		List<SalidaReporteAntiguedadBolsa> preSalidaServicio = new ArrayList<>();
		
		if(in.p_Consolidar.equalsIgnoreCase("S") && listadoMonedas.size() == 1) {
			for(ClientesConsolidado cliente : clientesConsolidado) {
				ClientePortafolio clientePortafolio = (ClientePortafolio) hashClientePortafolio.get(cliente.IdPortafolios.get(0));
								
				SalidaReporteAntiguedadBolsa SalidaAntiguedadDeudaCuentaInversionIntermediacion = new SalidaReporteAntiguedadBolsa();
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.DocumentoIndentidad = clientePortafolio.DocumentoIdentidad;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.NombreCliente = clientePortafolio.NombreCompleto;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.ClasificacionContable = "Cuenta de Intermediacion";
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.Relacionado = clientePortafolio.Relacionado;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.CampoBD = cliente.SaldoFinal;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.Observacion = "";
				
				SalidaReporteAntiguedadBolsa SalidaAntiguedadDeudaCuentaInversionGeneral = new SalidaReporteAntiguedadBolsa();
				SalidaAntiguedadDeudaCuentaInversionGeneral.DocumentoIndentidad = clientePortafolio.DocumentoIdentidad;
				SalidaAntiguedadDeudaCuentaInversionGeneral.NombreCliente = clientePortafolio.NombreCompleto;
				SalidaAntiguedadDeudaCuentaInversionGeneral.Relacionado = clientePortafolio.Relacionado;
				SalidaAntiguedadDeudaCuentaInversionGeneral.CampoBD = cliente.SaldoFinal;
				SalidaAntiguedadDeudaCuentaInversionGeneral.Observacion = "";
				SalidaAntiguedadDeudaCuentaInversionGeneral.ClasificacionContable = (clientePortafolio.Relacionado.equals("S"))? "Cuentas con Personas y Empresas Relacionados"
																																: "Cuentas por Cobrar y Cuentas de Documentos";
				//Obtener cargos del cliente
				List <MovimientoCargo> cargosFiltradosPorCliente = cargos.stream()
						.filter(Cargo -> Cargo.IdCliente.compareTo(cliente.IdCliente) == 0)
						.collect(Collectors.toList());
				
				cargosFiltradosPorCliente.sort(Comparator.comparing(MovimientoCargo::getFechaEfectivaMovimiento).thenComparing(MovimientoCargo::getFechaCertificacion).thenComparing(MovimientoCargo::getIdCargoAbonoMovimiento).reversed());
				
				BigDecimal saldoFinal = cliente.SaldoFinal;
				
				for(MovimientoCargo movimiento:cargosFiltradosPorCliente) {
					BigDecimal saldoAuxiliar = saldoFinal.add(movimiento.Monto, MathContext.DECIMAL128);
					String tramo = buscarTramo(hashRangoFechas, movimiento.FechaMovimiento.toLocalDate());
					if(saldoAuxiliar.compareTo(BigDecimal.ZERO) < 0) {
						asignarDeudaATramo(SalidaAntiguedadDeudaCuentaInversionIntermediacion, SalidaAntiguedadDeudaCuentaInversionGeneral, movimiento.Monto, tramo, movimiento.TipoOperacion);
					}else if (saldoAuxiliar.compareTo(BigDecimal.ZERO) == 0) {
						asignarDeudaATramo(SalidaAntiguedadDeudaCuentaInversionIntermediacion, SalidaAntiguedadDeudaCuentaInversionGeneral, movimiento.Monto, tramo, movimiento.TipoOperacion);
						break;
					}else {
						BigDecimal diferencia = movimiento.Monto.subtract(saldoAuxiliar, MathContext.DECIMAL128);
						asignarDeudaATramo(SalidaAntiguedadDeudaCuentaInversionIntermediacion, SalidaAntiguedadDeudaCuentaInversionGeneral, diferencia, tramo, movimiento.TipoOperacion);
						break;
					}
					saldoFinal = saldoAuxiliar;
				}
				
				darFormatoMontos(listadoMonedas, listadoMonedas.get(0).IdMoneda,SalidaAntiguedadDeudaCuentaInversionGeneral, SalidaAntiguedadDeudaCuentaInversionIntermediacion);
				
				SalidaAntiguedadDeudaCuentaInversionGeneral.MontoPorCobrarTotal = 	SalidaAntiguedadDeudaCuentaInversionGeneral.MontoPorCobrarTotal
						.add(SalidaAntiguedadDeudaCuentaInversionGeneral.TramoA, MathContext.DECIMAL128)
						.add(SalidaAntiguedadDeudaCuentaInversionGeneral.TramoB, MathContext.DECIMAL128)
						.add(SalidaAntiguedadDeudaCuentaInversionGeneral.TramoC, MathContext.DECIMAL128)
						.add(SalidaAntiguedadDeudaCuentaInversionGeneral.TramoD, MathContext.DECIMAL128);

				SalidaAntiguedadDeudaCuentaInversionIntermediacion.MontoPorCobrarTotal = 	SalidaAntiguedadDeudaCuentaInversionIntermediacion.MontoPorCobrarTotal
								.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoA, MathContext.DECIMAL128)
								.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoB, MathContext.DECIMAL128)
								.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoC, MathContext.DECIMAL128)
								.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoD, MathContext.DECIMAL128);
				
				if(in.p_SeparaIntermediacion.equalsIgnoreCase("S")) {
					if(SalidaAntiguedadDeudaCuentaInversionIntermediacion.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) == 1) {
						preSalidaServicio.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion);
					}
					if(SalidaAntiguedadDeudaCuentaInversionGeneral.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) == 1) {
						preSalidaServicio.add(SalidaAntiguedadDeudaCuentaInversionGeneral);
					}
				}	
			}
			out.P_OUT_TABLA.addAll(preSalidaServicio);
		}else {
			for(CuentaInversion ctaInver : listaCuentaInversionDePaso) {
				
				ClientePortafolio clientePortafolio = (ClientePortafolio) hashClientePortafolio.get(ctaInver.IdPortafolio);
				
				SalidaReporteAntiguedadBolsa SalidaAntiguedadDeudaCuentaInversionIntermediacion = new SalidaReporteAntiguedadBolsa();
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.IdPortafolio = ctaInver.IdPortafolio;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.DocumentoIndentidad = clientePortafolio.DocumentoIdentidad;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.NombreCliente = clientePortafolio.NombreCompleto;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.ClasificacionContable = "Cuenta de Intermediacion";
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.Relacionado = clientePortafolio.Relacionado;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.CampoBD = ctaInver.SaldoFinal;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.CodigoPortafolio =  clientePortafolio.CodigoPortafolio;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.Observacion = ctaInver.Observacion == null ? "":ctaInver.Observacion;		
				
				SalidaReporteAntiguedadBolsa SalidaAntiguedadDeudaCuentaInversionGeneral = new SalidaReporteAntiguedadBolsa();
				SalidaAntiguedadDeudaCuentaInversionGeneral.IdPortafolio = ctaInver.IdPortafolio;
				SalidaAntiguedadDeudaCuentaInversionGeneral.DocumentoIndentidad = clientePortafolio.DocumentoIdentidad;
				SalidaAntiguedadDeudaCuentaInversionGeneral.NombreCliente = clientePortafolio.NombreCompleto;
				SalidaAntiguedadDeudaCuentaInversionGeneral.Relacionado = clientePortafolio.Relacionado;
				SalidaAntiguedadDeudaCuentaInversionGeneral.CampoBD = ctaInver.SaldoFinal;
				SalidaAntiguedadDeudaCuentaInversionGeneral.CodigoPortafolio = clientePortafolio.CodigoPortafolio;
				SalidaAntiguedadDeudaCuentaInversionGeneral.Observacion = ctaInver.Observacion == null ? "":ctaInver.Observacion;
				SalidaAntiguedadDeudaCuentaInversionGeneral.ClasificacionContable = (clientePortafolio.Relacionado.equals("S"))? "Cuentas con Personas y Empresas Relacionados"
																																: "Cuentas por Cobrar y Cuentas de Documentos";
				
				//Obtener cargos de la cuenta de inversion
				List <MovimientoCargo> cargosFiltradosPorCuentaInversion = cargos.stream()
						.filter(Cargo -> Cargo.IdCuentaInversion.compareTo(ctaInver.IdCuentaInversion) == 0)
						.collect(Collectors.toList());
				
				cargosFiltradosPorCuentaInversion.sort(Comparator.comparing(MovimientoCargo::getFechaEfectivaMovimiento).thenComparing(MovimientoCargo::getFechaCertificacion).thenComparing(MovimientoCargo::getIdCargoAbonoMovimiento).reversed());
				
				BigDecimal saldoFinal = ctaInver.SaldoFinal;
				
				for(MovimientoCargo movimiento:cargosFiltradosPorCuentaInversion) {
					BigDecimal saldoAuxiliar = saldoFinal.add(movimiento.Monto, MathContext.DECIMAL128);
					String tramo = buscarTramo(hashRangoFechas, movimiento.FechaMovimiento.toLocalDate());
					if(saldoAuxiliar.compareTo(BigDecimal.ZERO) < 0) {
						asignarDeudaATramo(SalidaAntiguedadDeudaCuentaInversionIntermediacion, SalidaAntiguedadDeudaCuentaInversionGeneral, movimiento.Monto, tramo, movimiento.TipoOperacion);
					}else if (saldoAuxiliar.compareTo(BigDecimal.ZERO) == 0) {
						asignarDeudaATramo(SalidaAntiguedadDeudaCuentaInversionIntermediacion, SalidaAntiguedadDeudaCuentaInversionGeneral, movimiento.Monto, tramo, movimiento.TipoOperacion);
						break;
					}else {
						BigDecimal diferencia = movimiento.Monto.subtract(saldoAuxiliar, MathContext.DECIMAL128);
						asignarDeudaATramo(SalidaAntiguedadDeudaCuentaInversionIntermediacion, SalidaAntiguedadDeudaCuentaInversionGeneral, diferencia, tramo, movimiento.TipoOperacion);
						break;
					}
					saldoFinal = saldoAuxiliar;
				}
				
				darFormatoMontos(listadoMonedas, listadoMonedas.get(0).IdMoneda,SalidaAntiguedadDeudaCuentaInversionGeneral, SalidaAntiguedadDeudaCuentaInversionIntermediacion);
				
				SalidaAntiguedadDeudaCuentaInversionGeneral.MontoPorCobrarTotal = 	SalidaAntiguedadDeudaCuentaInversionGeneral.MontoPorCobrarTotal
						.add(SalidaAntiguedadDeudaCuentaInversionGeneral.TramoA, MathContext.DECIMAL128)
						.add(SalidaAntiguedadDeudaCuentaInversionGeneral.TramoB, MathContext.DECIMAL128)
						.add(SalidaAntiguedadDeudaCuentaInversionGeneral.TramoC, MathContext.DECIMAL128)
						.add(SalidaAntiguedadDeudaCuentaInversionGeneral.TramoD, MathContext.DECIMAL128);

				SalidaAntiguedadDeudaCuentaInversionIntermediacion.MontoPorCobrarTotal = 	SalidaAntiguedadDeudaCuentaInversionIntermediacion.MontoPorCobrarTotal
								.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoA, MathContext.DECIMAL128)
								.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoB, MathContext.DECIMAL128)
								.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoC, MathContext.DECIMAL128)
								.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoD, MathContext.DECIMAL128);
				
				if(in.p_SeparaIntermediacion.equalsIgnoreCase("S")) {
					if(SalidaAntiguedadDeudaCuentaInversionIntermediacion.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) == 1) {
						preSalidaServicio.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion);
					}
					if(SalidaAntiguedadDeudaCuentaInversionGeneral.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) == 1) {
						preSalidaServicio.add(SalidaAntiguedadDeudaCuentaInversionGeneral);
					}
				}
			}
			
			out.P_OUT_TABLA.addAll(preSalidaServicio);
		}
		
		logger.info("FIN::ConsultaReporteAntiguedadBolsa");
		return out;
		
	}
	
	private List<Moneda> obtenerDistintasMonedasDesdeCuentasInversion(Connection con, SQL sql)  throws SQLException{
		logger.debug("INICIO::ConsultaReporteAntiguedadBolsa.obtenerDistintasMonedasDesdeCuentasInversion");
		long inicio = System.currentTimeMillis();
		HashMap<String, Object> param = new HashMap<>();
		List<Moneda> listaMonedas = sql.query(con, SQL_SELECT_DISTINT_MONEDA_CUENTA_INVERSION, param, Moneda.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAntiguedadBolsa.obtenerDistintasMonedasDesdeCuentasInversion tiempo: " + (fin - inicio));
		return listaMonedas;
	}
	
	private Map<String, FechaBusqueda> obtenerRangosDeFechas(Connection con, EntradaConsultaReporteAntiguedadBolsa in, Long IdPais, List<LocalDate> listaFechas) throws Exception{
		logger.trace("INICIO::ConsultaReporteAntiguedadBolsa.obtenerRangosDeFechas");
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
		logger.trace("FIN::ConsultaReporteAntiguedadBolsa.obtenerRangosDeFechas tiempo: " + (fin - inicio));
		return hashRangosFechas;
	}
	
	private void mapearRangoDeFecha (String rango, HashMap<String, FechaBusqueda> HashFecha ,  List<String> listaFecha){
		logger.trace("INICIO::ConsultaReporteAntiguedadBolsa.mapearRangoDeFecha");
		long inicio = System.currentTimeMillis();
		
		String fechaMinima = Collections.min(listaFecha);
		String fechaMaxima = Collections.max(listaFecha);
		FechaBusqueda fechaBusq = new FechaBusqueda();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		fechaBusq.FechaInicio = LocalDate.parse(fechaMinima, formatter);
		fechaBusq.FechaFin = LocalDate.parse(fechaMaxima, formatter);
		
		HashFecha.put(rango, fechaBusq);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAntiguedadBolsa.mapearRangoDeFecha tiempo: " + (fin - inicio));
	}
	
	private List<SaldoPorFechaEfectiva> obtenerCuentasInversionNegativas(Connection con, EntradaConsultaReporteAntiguedadBolsa in, List<Moneda> monedas) throws Exception {
		logger.trace("INICIO::ConsultaReporteAntiguedadBolsa.obtenerCuentasInversionNegativas");
		long inicio = System.currentTimeMillis();
		List<SaldoPorFechaEfectiva> ListaCuentasInversion = new ArrayList<>();
		
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
		
		List<InCuentaInversionConsultar> lstCuentaInversionConsultar = new ArrayList<InCuentaInversionConsultar>();
		InCuentaInversionConsultar objCuentaInversionConsultar = new InCuentaInversionConsultar();
		objCuentaInversionConsultar.IdEjecutivo  = in.p_IdEjecutivo;
		objCuentaInversionConsultar.IdMoneda 	 = null;
		objCuentaInversionConsultar.IdPortafolio = null; 
		lstCuentaInversionConsultar.add(objCuentaInversionConsultar);
		entradaConsultarSaldoCuenta.p_CuentaInversionAConsultar = lstCuentaInversionConsultar;
		
		SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuenta = servicioConsultarSaldoCuenta.logica(con, entradaConsultarSaldoCuenta);
		
		for(Moneda moneda : monedas) {
			entradaConsultarSaldoCuenta.p_IdMoneda = moneda.IdMoneda;	
			
			if(salidaConsultarSaldoCuenta.P_OUT_ERRORESCONTADOR.compareTo(0L)==0 && !salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty()){

				//Si la moneda es CLP se debe truncar el Saldo disponible
				if(moneda.IdMoneda.equalsIgnoreCase("CLP")) {
					List<SaldoPorFechaEfectiva> cuentasInversionNegativas = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
							.filter(Saldo-> Saldo.IdMoneda.equalsIgnoreCase(moneda.IdMoneda) && Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128).setScale(0, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) == -1)
							.collect(Collectors.toList());
					
					ListaCuentasInversion.addAll(cuentasInversionNegativas);
				}else {
					List<SaldoPorFechaEfectiva> cuentasInversionNegativas = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
							.filter(Saldo-> Saldo.IdMoneda.equalsIgnoreCase(moneda.IdMoneda) && Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128).setScale(2, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) == -1)
							.collect(Collectors.toList());
					
					ListaCuentasInversion.addAll(cuentasInversionNegativas);
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAntiguedadBolsa.obtenerCuentasInversionNegativas tiempo: " + (fin - inicio));
		
		return ListaCuentasInversion;
	}
	
	
	private List<SaldoPorFechaEfectiva> obtenerCuentasInversion(Connection con, EntradaConsultaReporteAntiguedadBolsa in, String moneda, List<ClientesConsolidado> clientesConsolidado) throws Exception {
		logger.debug("INICIO::ConsultaReporteAntiguedadBolsa.obtenerCuentasInversion");
		long inicio = System.currentTimeMillis();
		List<SaldoPorFechaEfectiva> ListaCuentasInversion = new ArrayList<>();
		
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
		entradaConsultarSaldoCuenta.p_IdMoneda = moneda;	
		
		List<InCuentaInversionConsultar> lstCuentaInversionConsultar = new ArrayList<InCuentaInversionConsultar>();
		InCuentaInversionConsultar objCuentaInversionConsultar = new InCuentaInversionConsultar();
		objCuentaInversionConsultar.IdEjecutivo  = in.p_IdEjecutivo;
		objCuentaInversionConsultar.IdMoneda 	 = moneda;
		objCuentaInversionConsultar.IdPortafolio = null; 
		lstCuentaInversionConsultar.add(objCuentaInversionConsultar);
		entradaConsultarSaldoCuenta.p_CuentaInversionAConsultar = lstCuentaInversionConsultar;
		List<SaldoPorFechaEfectiva> ListaCuentasInversionNegativas = new ArrayList<>();
		
		SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuenta = servicioConsultarSaldoCuenta.logica(con, entradaConsultarSaldoCuenta);
		if(salidaConsultarSaldoCuenta.P_OUT_ERRORESCONTADOR.compareTo(0L)==0 && !salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty()){

			//Si la moneda es CLP se debe truncar el Saldo disponible
			if(moneda.equalsIgnoreCase("CLP")) {
				List<SaldoPorFechaEfectiva> cuentasInversionNegativas = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
						.filter(Saldo-> Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128).setScale(0, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) == -1)
						.collect(Collectors.toList());
				
				ListaCuentasInversionNegativas.addAll(cuentasInversionNegativas);
			}else {
				List<SaldoPorFechaEfectiva> cuentasInversionNegativas = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
						.filter(Saldo-> Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128).setScale(2, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) == -1)
						.collect(Collectors.toList());
				
				ListaCuentasInversionNegativas.addAll(cuentasInversionNegativas);
			}
			
			List<Long> idClientesConCuentaNegativas = ListaCuentasInversionNegativas.parallelStream().
					map(cuenta->cuenta.IdCliente)
					.distinct()
					.collect(Collectors.toList());
			
			for(Long idCliente : idClientesConCuentaNegativas) {
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
				if(saldoTotalClienteAprox.compareTo(BigDecimal.ZERO) == -1) {
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
					ListaCuentasInversion.addAll(cuentasInversionCliente);					
				}
			}
		}
			
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAntiguedadBolsa.obtenerCuentasInversion tiempo: " + (fin - inicio));
		
		return ListaCuentasInversion;
	}
	
	private List<CuentaInversion> prepararSaldoFinalCuentaInversion(List<SaldoPorFechaEfectiva> SaldosCuentaInversion, String Consolidado){
		logger.trace("INICIO::ConsultaReporteAntiguedadBolsa.prepararSaldoFinalCuentaInversion");
		long inicio = System.currentTimeMillis();
		List<CuentaInversion> listaCuentaInversionDePaso = new ArrayList<>(SaldosCuentaInversion.size());
		for(SaldoPorFechaEfectiva objCuenInversion : SaldosCuentaInversion) {
			CuentaInversion ctaInver = new CuentaInversion();
			ctaInver.IdCuentaInversion = objCuenInversion.IdCuentaInversion;
			ctaInver.IdCliente = objCuenInversion.IdCliente;
			ctaInver.IdPortafolio = objCuenInversion.IdPortafolio;
			ctaInver.IdMoneda = objCuenInversion.IdMoneda;
			if(Consolidado.equalsIgnoreCase("N")) {
				ctaInver.SaldoFinal = objCuenInversion.SaldoDisponible.add(objCuenInversion.SaldoRetenido, MathContext.DECIMAL128);
			}else {
				ctaInver.SaldoFinal = objCuenInversion.SaldoDisponible;
			}			
			listaCuentaInversionDePaso.add(ctaInver);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAntiguedadBolsa.prepararSaldoFinalCuentaInversion tiempo: " + (fin - inicio));
		
		return listaCuentaInversionDePaso;
	}
	
	private boolean eliminarCuentasInversionTablaPaso(Connection con, SQL sql) {
		logger.debug("INICIO::ConsultaReporteAntiguedadBolsa.eliminarCuentasInversionTablaPaso");
		long inicio = System.currentTimeMillis();
		boolean resultado = false;
		
		try {
			sql.batch(con, SQL_DELETE_CUENTA_INVERSION_PASO , new ArrayList<>() , CuentaInversion.class);
			resultado = true;
		}catch(SQLException SQLe) {
			
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAntiguedadBolsa.eliminarCuentasInversionTablaPaso tiempo: " + (fin - inicio));
		return resultado;
	}
	
	private boolean insertarCuentasInversionTablaPaso(Connection con, SQL sql, List<CuentaInversion> cuentaInversionPaso, Long idTrabajo) {
		logger.debug("INICIO::ConsultaReporteAntiguedadBolsa.insertarCuentasInversionTablaPaso");
		long inicio = System.currentTimeMillis();
		boolean resultado = false;
		
		try {
			int resultadoQuery []  =  sql.batch(con, SQL_INSERT_CUENTA_INVERSION_PASO.replace(":IdTrabajo", idTrabajo+"") ,cuentaInversionPaso , CuentaInversion.class);
			if(resultadoQuery.length>0) {
				resultado = true;
			}
		}catch(SQLException SQLe) {
			SQLe.printStackTrace();
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAntiguedadBolsa.insertarCuentasInversionTablaPaso tiempo: " + (fin - inicio));
		
		return resultado;
	}
	
	private List<ClientePortafolio> obtenerClientesPorPortafolio(Connection con, SQL sql, Long IdTrabajo) throws SQLException{
		logger.debug("INICIO::ConsultaReporteAntiguedadBolsa.obtenerClientesPorPortafolio");
		long inicio = System.currentTimeMillis();
		List<ClientePortafolio> listaClientes = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(1);
		param.put("IdTrabajo", IdTrabajo);
		listaClientes = sql.query(con, SQL_SELECT_DATOS_CLIENTE , param , ClientePortafolio.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAntiguedadBolsa.obtenerClientesPorPortafolio tiempo: " + (fin - inicio));
		
		return listaClientes;
	}
	
	private List<MovimientoCargo> obtenerCargos(Connection con, SQL sql, String fechaBusqueda, Long idTrabajo) throws SQLException{
		logger.debug("INICIO::ConsultaReporteAntiguedadBolsa.obtenerCargos");
		long inicio = System.currentTimeMillis();
		List<MovimientoCargo> movimientos = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(2);
		param.put("IdTrabajo", idTrabajo);
		param.put("FechaBusqueda", fechaBusqueda);
		movimientos = sql.query(con, SQL_SELECT_CARGOS, param, MovimientoCargo.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaReporteAntiguedadBolsa.obtenerCargos tiempo: " + (fin - inicio));
		
		return movimientos;
		
	}
	
	private String buscarTramo(Map<String, FechaBusqueda> hashRangoFechas, LocalDate fechaConsultada) {
		logger.trace("INICIO::ConsultaReporteAntiguedadBolsa.buscarTramo");
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
		logger.trace("FIN::ConsultaReporteAntiguedadBolsa.buscarTramo tiempo: " + (fin - inicio));
		
		return tramo;
	}
	
	private void asignarDeudaATramo(SalidaReporteAntiguedadBolsa SalidaAntiguedadDeudaCuentaInversionIntermediacion, SalidaReporteAntiguedadBolsa SalidaAntiguedadDeudaCuentaInversionGeneral, BigDecimal monto, String tramo, String tipoDeuda) {
		logger.trace("INICIO::ConsultaReporteAntiguedadBolsa.asignarDeudaATramo");
		long inicio = System.currentTimeMillis();
		
		if(tipoDeuda.equalsIgnoreCase(Intermediacion)) {
			switch(tramo) {
				case "TRAMO_A":
					SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoA = SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoA.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_B":
					SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoB = SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoB.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_C":
					SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoC = SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoC.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_D":
					SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoD = SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoD.add(monto, MathContext.DECIMAL128);
					break;
			}
		}else {
			switch(tramo) {
				case "TRAMO_A":
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoA = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoA.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_B":
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoB = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoB.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_C":
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoC = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoC.add(monto, MathContext.DECIMAL128);
					break;
				case "TRAMO_D":
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoD = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoD.add(monto, MathContext.DECIMAL128);
					break;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAntiguedadBolsa.asignarDeudaATramo tiempo: " + (fin - inicio));
	}
	
	public void darFormatoMontos(List<Moneda> listadoMonedas, String idMoneda,SalidaReporteAntiguedadBolsa antiguedadDeudaGeneral, SalidaReporteAntiguedadBolsa antiguedadDeudaIntermediacion) {
		logger.trace("INICIO::ConsultaReporteAntiguedadBolsa.darFormatoMontos");
		long inicio = System.currentTimeMillis();
		
		if(idMoneda.equalsIgnoreCase("CLP") || listadoMonedas.size() > 1) {
			//General
			antiguedadDeudaGeneral.TramoA = antiguedadDeudaGeneral.TramoA.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			antiguedadDeudaGeneral.TramoB = antiguedadDeudaGeneral.TramoB.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128); 
			antiguedadDeudaGeneral.TramoC = antiguedadDeudaGeneral.TramoC.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128); 
			antiguedadDeudaGeneral.TramoD = antiguedadDeudaGeneral.TramoD.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			antiguedadDeudaGeneral.MontoPorCobrarTotal	= antiguedadDeudaGeneral.MontoPorCobrarTotal.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			//Intermediacion
			antiguedadDeudaIntermediacion.TramoA = antiguedadDeudaIntermediacion.TramoA.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			antiguedadDeudaIntermediacion.TramoB = antiguedadDeudaIntermediacion.TramoB.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			antiguedadDeudaIntermediacion.TramoC = antiguedadDeudaIntermediacion.TramoC.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			antiguedadDeudaIntermediacion.TramoD = antiguedadDeudaIntermediacion.TramoD.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			antiguedadDeudaIntermediacion.MontoPorCobrarTotal = antiguedadDeudaIntermediacion.MontoPorCobrarTotal.setScale(0, RoundingMode.DOWN).abs(MathContext.DECIMAL128);
			
		}else
		{
			//General
			antiguedadDeudaGeneral.TramoA = antiguedadDeudaGeneral.TramoA.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			antiguedadDeudaGeneral.TramoB = antiguedadDeudaGeneral.TramoB.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			antiguedadDeudaGeneral.TramoC = antiguedadDeudaGeneral.TramoC.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			antiguedadDeudaGeneral.TramoD = antiguedadDeudaGeneral.TramoD.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			antiguedadDeudaGeneral.MontoPorCobrarTotal = antiguedadDeudaGeneral.MontoPorCobrarTotal.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			//Intermediacion
			antiguedadDeudaIntermediacion.TramoA = antiguedadDeudaIntermediacion.TramoA.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			antiguedadDeudaIntermediacion.TramoB = antiguedadDeudaIntermediacion.TramoB.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			antiguedadDeudaIntermediacion.TramoC = antiguedadDeudaIntermediacion.TramoC.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			antiguedadDeudaIntermediacion.TramoD = antiguedadDeudaIntermediacion.TramoD.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
			antiguedadDeudaIntermediacion.MontoPorCobrarTotal = antiguedadDeudaIntermediacion.MontoPorCobrarTotal.setScale(2, RoundingMode.HALF_EVEN).abs(MathContext.DECIMAL128);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaReporteAntiguedadBolsa.darFormatoMontos tiempo: " + (fin - inicio));
	}
	
}
