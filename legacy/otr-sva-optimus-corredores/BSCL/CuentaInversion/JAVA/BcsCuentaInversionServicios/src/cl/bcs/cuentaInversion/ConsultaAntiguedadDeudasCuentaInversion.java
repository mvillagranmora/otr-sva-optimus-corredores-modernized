package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.CallableStatement;
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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaAntiguedadDeudasCuentaInversion implements JavaCall {
	
	private final static Logger logger = LogManager.getLogger(ConsultaAntiguedadDeudasCuentaInversion.class);
	
	/*Inicio Temporales*/
	private final String SQL_INSERT_CUENTA_INVERSION_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaAntiguedadDeudasCuentaInversion_InsertCuentaInversionTablaDePaso.sql");
	private final String SQL_DELETE_CUENTA_INVERSION_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaAntiguedadDeudasCuentaInversion_DeleteCuentaInversionTablaDePaso.sql");
	/*Fin Temporales*/
	
	private final String SQL_SELECT_DISTINT_MONEDA_CUENTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaAntiguedadDeudasCuentaInversion_SelectDistintasMonedasCuentasInversion.sql");
	private final String SQL_SELECT_DATOS_CLIENTE = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaAntiguedadDeudasCuentaInversion_SelectDatosCliente.sql");
		
	private final String SQL_SELECT_SALDOS_REPORTE_ANTIGUEDAD = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaAntiguedadDeudasCuentaInversion_SelectSaldosReporteAntiguedad.sql");
	private final String SQL_SELECT_FECHA_ANTIGUEDAD_DEUDA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaAntiguedadDeudasCuentaInversion_SelectFechaDeudaAntiguedad.sql");

	private final String C_Moneda = "TODAS";
	// ---------------------------------------------------
	// CLASES DE ENTRADA 
	// ---------------------------------------------------
	
	public static class EntradaConsultaAntiguedadDeudasCuentaInversion{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaConsulta;
		public String p_IdMoneda;
		public Long p_IdEjecutivo;
		public String p_SeparaIntermediacion;
		public String p_AgrupaPortafolios;
		public String p_Consolidar;
		public EntradaConsultaAntiguedadDeudasCuentaInversion() {
			p_IdEjecutivo = 0L;
			p_SeparaIntermediacion = "N";
			p_AgrupaPortafolios = "N";
			p_Consolidar = "N";
		}
	}
	
	public static class FechaBusqueda{
		public LocalDate FechaInicio;
		public LocalDate FechaFin;
	}
	
	public static class Moneda{
		public String IdMoneda;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentaInversion{
		public Long IdCuentaInversion;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		//Estos para implementar los tramos
		public BigDecimal SaldoFinal;
		public BigDecimal SaldoInicial;
		public Date FechaSaldoInicial;
		public String Observacion;
		public String SaldoEsIntermediacion;
		public CuentaInversion() {
			this.SaldoFinal = BigDecimal.ZERO;
			this.SaldoInicial  = BigDecimal.ZERO;
			this.Observacion = "";
		}
	}
	
	public static class ClientePortafolio{
		public Long IdPersona;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public Long IdCliente;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String Relacionado;
	}
	
	public static class MovimientoCargoAbono{
		public Long IdMovimientoCargo;
		public Long IdCuentaInversion;
		public String IdMoneda;
		public String IdTipoMovimiento;
		public BigDecimal Monto;
		public String EsIntermediacion;
		public String FechaDeuda;
		public String Observacion;
		public Timestamp FechaCertificacion;
		
		public MovimientoCargoAbono() {
			this.Observacion = "";
		}
		
		public Date getFechaDeuda() {
			return Fecha.toDate(FechaDeuda);
		}
		
		public Timestamp getFechaCertificacion() {
			return this.FechaCertificacion;
		}
	}
	
	public static class CuentasPorActualizarSaldo extends BcsPojo{
		public Long IdEjecucion;
		public Long IdCuentaInversion;
	}
	
	public static class SaldosPorFechas extends BcsPojo{
		public Long 		IdCuentaInversion;
		public Date			FechaSistema; 
		public BigDecimal 	Saldo;
		public String		SaldoEsIntermediacion;
		
		public Date getFechaSistema() {
			return this.FechaSistema;
		}
	}
	
	public static class ClientesConsolidado extends BcsPojo{
		public Long			IdCliente;
		public BigDecimal	SaldoFinal;
		public BigDecimal	SaldoInicial;
		public List<Long>	IdCuentasInversion;
		public List<Long>	IdPortafolios;
		public String		TramoSaldoInicial;
		
		public ClientesConsolidado() {
			IdCuentasInversion = new ArrayList<Long>();
			IdPortafolios = new ArrayList<Long>();
			SaldoInicial = BigDecimal.ZERO;
			SaldoFinal = BigDecimal.ZERO;
			TramoSaldoInicial = "";
		}
	}
	
	public static class SaldosReporteAntiguedad extends BcsPojo{
		public Long			NumeroRegistro;
		public Date			FechaSistema;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String		IdMoneda;
		public String 		TipoSaldo;
		public BigDecimal	Saldo;
		
		public Date getFechaSistema() {
			return this.FechaSistema;
		}
	}
	
	public static class FechaAntiguedadDeuda extends BcsPojo{
		public Date	FechaSistema;
	}

	// ---------------------------------------------------
	// CLASES DE SALIDA 
	// ---------------------------------------------------
	public static class SalidaConsultaAntiguedadDeudasCuentaInversion{
		public ArrayList<MensajeSalida>								P_OUT_MENSAJES;
		public Long P_OUT_INFORMACIONCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public Long P_OUT_ERRORESCONTADOR;
		public ArrayList<SalidaAntiguedadDeudaCuentaInversion> 		P_OUT_TABLA;
		public SalidaConsultaAntiguedadDeudasCuentaInversion() {
			this.P_OUT_ERRORESCONTADOR =  0L;
			this.P_OUT_TABLA 			= new ArrayList<SalidaAntiguedadDeudaCuentaInversion>();
			this.P_OUT_MENSAJES  		= new ArrayList<MensajeSalida>();
		}
	}
	
	public static class SalidaAntiguedadDeudaCuentaInversion{

		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String DocumentoIndentidad;
		public String NombreCliente;
		public BigDecimal MontoPorCobrarTotal;
		//Inicio Antiguedad de monto por Cobrar -- Desgloze por tramos
		public BigDecimal TramoA; //Vigente 			=> TRAMO_A
		public BigDecimal TramoB; //3 a 10 dias			=> TRAMO_B
		public BigDecimal TramoC; //11 a 30 dias		=> TRAMO_C
		public BigDecimal TramoD; //mayores a 30 dias	=> TRAMO_D
		//Fin Antiguedad de monto por Cobrar -- Desgloze por fechas
		public String Observacion;
		public String ClasificacionContable;
		public String Relacionado;
		public String FechaDeuda;
		/*CAMPOS DE PRUEBA*/
		public BigDecimal CampoBD;
		public BigDecimal Matematica;
		public SalidaAntiguedadDeudaCuentaInversion() {
			//this.DocumentoIndentidad = "";
			//this.NombreCliente = "";
			this.MontoPorCobrarTotal = BigDecimal.ZERO;
			this.TramoA = BigDecimal.ZERO;
			this.TramoB = BigDecimal.ZERO;
			this.TramoC = BigDecimal.ZERO;
			this.TramoD = BigDecimal.ZERO;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultaAntiguedadDeudasCuentaInversion jin = gson.fromJson(in.toString(), EntradaConsultaAntiguedadDeudasCuentaInversion.class);
		SalidaConsultaAntiguedadDeudasCuentaInversion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static<T> List<T> reverseList(List<T> list) {
        return new ArrayList<>(Lists.reverse(list));
    }
	
	public SalidaConsultaAntiguedadDeudasCuentaInversion logica (Connection con, EntradaConsultaAntiguedadDeudasCuentaInversion in) throws Exception {
		logger.info("INICIO::ConsultaAntiguedadDeudasCuentaInversion");
		SalidaConsultaAntiguedadDeudasCuentaInversion out = new SalidaConsultaAntiguedadDeudasCuentaInversion();
		SQL sql = new SQL();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		
		List<Moneda> listadoMonedas = new ArrayList<Moneda>();
		
		logger.info(" ConsultaAntiguedadDeudasCuentaInversion MONEDA :" +in.p_IdMoneda);
		if(in.p_IdMoneda!=null && !in.p_IdMoneda.isEmpty() && !(C_Moneda).equals(in.p_IdMoneda)) {
			Moneda moneda = new Moneda();
			moneda.IdMoneda = in.p_IdMoneda;
			listadoMonedas.add(moneda);
			logger.info(" ConsultaAntiguedadDeudasCuentaInversion IF :" +listadoMonedas.size());
		}
		else {
			listadoMonedas = obtenerDistintasMonedasDesdeCuentasInversion(con, sql);
			logger.info(" ConsultaAntiguedadDeudasCuentaInversion ELSE :" +listadoMonedas.size());
		}
		
		Map<String,Moneda> hashMoneda = listadoMonedas.parallelStream().collect(Collectors.toMap(moneda->moneda.IdMoneda, moneda->moneda));
		
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
		
		//llamamos a eliminar e insertar temporal
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
		
		List<SaldosReporteAntiguedad> saldosReporteAntiguedad = obtenerSaldosReporteAntiguedad(con, sql, idTrabajo, hashRangoFechas);
		
		//Si la eleccion es mas de una moneda, todo a CLP(CalcularParidad)
		if(hashMoneda.size() > 1 && in.p_Consolidar.equalsIgnoreCase("N")) {
			List<CuentaInversion> listaCuentaInversionDistinaCLP = listaCuentaInversionDePaso.stream()
																								.filter(CuentaInversion->!CuentaInversion.IdMoneda.equals("CLP"))
																								.collect(Collectors.toList());
			List<CalculoEntrada> paridadCTASaldoFinal = new ArrayList<>();
			//List<CalculoEntrada> paridadCTASaldoInicial = new ArrayList<>();
			for(CuentaInversion cta : listaCuentaInversionDistinaCLP) {
				CalculoEntrada calculoEntradaSaldoFinal = new CalculoEntrada();
				calculoEntradaSaldoFinal.Fecha = in.p_FechaConsulta;
				calculoEntradaSaldoFinal.IdTrabajo = 0L;
				calculoEntradaSaldoFinal.IdInterno = cta.IdCuentaInversion;
				calculoEntradaSaldoFinal.IdMonedaOrigen = cta.IdMoneda;
				calculoEntradaSaldoFinal.IdMonedaDestino = "CLP";
				//SaldoFinal 
				calculoEntradaSaldoFinal.MontoOrigen = cta.SaldoFinal;
				paridadCTASaldoFinal.add(calculoEntradaSaldoFinal);
			}
			
			List<CalculoSalida> paridadCTASaldoFinalSalida = ObtenerCalculoParidad(con, in.p_IdEmpresa, paridadCTASaldoFinal, "SaldoFinalCTA");
			
			listaCuentaInversionDePaso = listaCuentaInversionDePaso.stream()
																	.filter(CuentaInversion->CuentaInversion.IdMoneda.equals("CLP"))
																	.collect(Collectors.toList());
			
			listaCuentaInversionDePaso.addAll(convertirSaldosParidadCTA(listaCuentaInversionDistinaCLP, paridadCTASaldoFinalSalida));
			
			
			List<SaldosReporteAntiguedad> listaSaldosReporteAntiguedadDistinaCLP = saldosReporteAntiguedad.stream()
					.filter(saldoReporteAntiguedad-> !saldoReporteAntiguedad.IdMoneda.equalsIgnoreCase("CLP"))
					.collect(Collectors.toList());
			
			List<CalculoEntrada> paridadSaldoReporte = new ArrayList<>();
			for(SaldosReporteAntiguedad saldo : listaSaldosReporteAntiguedadDistinaCLP) {
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.Fecha = in.p_FechaConsulta;
				calculoEntrada.IdTrabajo = 0L;
				calculoEntrada.IdInterno = saldo.NumeroRegistro;
				calculoEntrada.IdMonedaOrigen = saldo.IdMoneda;
				calculoEntrada.IdMonedaDestino = "CLP";
				calculoEntrada.MontoOrigen = saldo.Saldo;
				paridadSaldoReporte.add(calculoEntrada);
			}
			
			List<CalculoSalida> paridadSaldoReporteAntiguedadSalida = ObtenerCalculoParidad(con, in.p_IdEmpresa, paridadSaldoReporte, "SaldosReporteAntiguedad");
			
			saldosReporteAntiguedad = saldosReporteAntiguedad.stream()
					.filter(saldoReporteAntiguedad-> saldoReporteAntiguedad.IdMoneda.equalsIgnoreCase("CLP"))
					.collect(Collectors.toList());
			
			saldosReporteAntiguedad.addAll(convertirParidadSaldosReporteAntiguedad(listaSaldosReporteAntiguedadDistinaCLP ,paridadSaldoReporteAntiguedadSalida));
		}

		
		List<SalidaAntiguedadDeudaCuentaInversion> preSalidaServicio = new ArrayList<>();
		
		if(in.p_Consolidar.equalsIgnoreCase("S") && listadoMonedas.size() == 1) {
		
			for(ClientesConsolidado cliente : clientesConsolidado) {
				
				List<SaldosReporteAntiguedad> saldosReporteAntiguedadFiltrados = new ArrayList<>();
				
				ClientePortafolio clientePortafolio = (ClientePortafolio) hashClientePortafolio.get(cliente.IdPortafolios.get(0));
				
				SalidaAntiguedadDeudaCuentaInversion SalidaAntiguedadDeudaCuentaInversionIntermediacion = new SalidaAntiguedadDeudaCuentaInversion();
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.DocumentoIndentidad = clientePortafolio.DocumentoIdentidad;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.NombreCliente = clientePortafolio.NombreCompleto;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.ClasificacionContable = "Cuenta de Intermediacion";
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.Relacionado = clientePortafolio.Relacionado;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.CampoBD = cliente.SaldoFinal;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.Observacion = "";
				
				SalidaAntiguedadDeudaCuentaInversion SalidaAntiguedadDeudaCuentaInversionGeneral = new SalidaAntiguedadDeudaCuentaInversion();
				SalidaAntiguedadDeudaCuentaInversionGeneral.DocumentoIndentidad = clientePortafolio.DocumentoIdentidad;
				SalidaAntiguedadDeudaCuentaInversionGeneral.NombreCliente = clientePortafolio.NombreCompleto;
				SalidaAntiguedadDeudaCuentaInversionGeneral.Relacionado = clientePortafolio.Relacionado;
				SalidaAntiguedadDeudaCuentaInversionGeneral.CampoBD = cliente.SaldoFinal;
				SalidaAntiguedadDeudaCuentaInversionGeneral.Observacion = "";
				SalidaAntiguedadDeudaCuentaInversionGeneral.ClasificacionContable = (clientePortafolio.Relacionado.equals("S"))? "Cuentas con Personas y Empresas Relacionados"
																																: "Cuentas por Cobrar y Cuentas de Documentos";
	
				saldosReporteAntiguedadFiltrados.addAll(saldosReporteAntiguedad.stream()
							.filter(saldoReporte -> saldoReporte.IdCliente.equals(cliente.IdCliente))
							.collect(Collectors.toList())
							);
				
				saldosReporteAntiguedadFiltrados.sort(Comparator.comparing(SaldosReporteAntiguedad::getFechaSistema));
				
				//Obtener Fecha Deuda
				List<FechaAntiguedadDeuda> fechaDeudaG = new ArrayList<>();
				List<FechaAntiguedadDeuda> fechaDeudaI = new ArrayList<>();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				HashMap<String, Object> param = new HashMap<>(3);
				if(in.p_SeparaIntermediacion.equalsIgnoreCase("S"))
				{
					param.put("IdCliente", clientePortafolio.IdCliente);
					param.put("IdPortafolio", cliente.IdPortafolios);
					param.put("TipoSaldo", "G");
					param.put("IdMoneda", listadoMonedas.get(0).IdMoneda);
					fechaDeudaG = sql.query(con, SQL_SELECT_FECHA_ANTIGUEDAD_DEUDA, param,FechaAntiguedadDeuda.class);
					if(fechaDeudaG != null && !fechaDeudaG.isEmpty() && fechaDeudaG.get(0).FechaSistema != null) {
						LocalDate fechaLocalG = fechaDeudaG.get(0).FechaSistema.toLocalDate();
						SalidaAntiguedadDeudaCuentaInversionGeneral.FechaDeuda = fechaLocalG.format(formatter);
					}
					param.put("TipoSaldo", "I");
					fechaDeudaI = sql.query(con, SQL_SELECT_FECHA_ANTIGUEDAD_DEUDA, param,FechaAntiguedadDeuda.class);
					if(fechaDeudaI != null && !fechaDeudaI.isEmpty() && fechaDeudaI.get(0).FechaSistema != null) {
						LocalDate fechaLocalI = fechaDeudaI.get(0).FechaSistema.toLocalDate();
						SalidaAntiguedadDeudaCuentaInversionIntermediacion.FechaDeuda = fechaLocalI.format(formatter);
					}
				}else {
					param.put("IdCliente", clientePortafolio.IdCliente);
					param.put("IdPortafolio", cliente.IdPortafolios);
					param.put("TipoSaldo", "");
					param.put("IdMoneda", listadoMonedas.get(0).IdMoneda);
					fechaDeudaG = sql.query(con, SQL_SELECT_FECHA_ANTIGUEDAD_DEUDA, param,FechaAntiguedadDeuda.class);
					if(fechaDeudaG != null && !fechaDeudaG.isEmpty() && fechaDeudaG.get(0).FechaSistema != null) {
						LocalDate fechaLocalG = fechaDeudaG.get(0).FechaSistema.toLocalDate();
						SalidaAntiguedadDeudaCuentaInversionGeneral.FechaDeuda = fechaLocalG.format(formatter);
					}
				}
				
				asignarSaldosPorTramo(in.p_SeparaIntermediacion, saldosReporteAntiguedadFiltrados, SalidaAntiguedadDeudaCuentaInversionIntermediacion, SalidaAntiguedadDeudaCuentaInversionGeneral,
						hashRangoFechas, listaFechas);
				
				repasarMontosPorTramos(cliente.SaldoFinal,SalidaAntiguedadDeudaCuentaInversionIntermediacion, SalidaAntiguedadDeudaCuentaInversionGeneral);
				
				darFormatoMontos(hashMoneda, listadoMonedas.get(0).IdMoneda,SalidaAntiguedadDeudaCuentaInversionGeneral, SalidaAntiguedadDeudaCuentaInversionIntermediacion);
				
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
					preSalidaServicio.add(SalidaAntiguedadDeudaCuentaInversionGeneral);
					if(SalidaAntiguedadDeudaCuentaInversionIntermediacion.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) == 1) {
						preSalidaServicio.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion);
					}
				}
				else {
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoA = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoA.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoA, MathContext.DECIMAL128);
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoB = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoB.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoB, MathContext.DECIMAL128);
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoC = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoC.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoC, MathContext.DECIMAL128);
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoD = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoD.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoD, MathContext.DECIMAL128);
					preSalidaServicio.add(SalidaAntiguedadDeudaCuentaInversionGeneral);
				}
			}
			
			out.P_OUT_TABLA.addAll(preSalidaServicio);
		}else {
			for(CuentaInversion ctaInver : listaCuentaInversionDePaso) {
				
				List<SaldosReporteAntiguedad> saldosReporteAntiguedadFiltrados = new ArrayList<>();
				
				ClientePortafolio clientePortafolio = (ClientePortafolio) hashClientePortafolio.get(ctaInver.IdPortafolio);
				
				SalidaAntiguedadDeudaCuentaInversion SalidaAntiguedadDeudaCuentaInversionIntermediacion = new SalidaAntiguedadDeudaCuentaInversion();
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.IdPortafolio = ctaInver.IdPortafolio;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.DocumentoIndentidad = clientePortafolio.DocumentoIdentidad;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.NombreCliente = clientePortafolio.NombreCompleto;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.ClasificacionContable = "Cuenta de Intermediacion";
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.Relacionado = clientePortafolio.Relacionado;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.CampoBD = ctaInver.SaldoFinal;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.CodigoPortafolio =  clientePortafolio.CodigoPortafolio;
				SalidaAntiguedadDeudaCuentaInversionIntermediacion.Observacion = ctaInver.Observacion == null ? "":ctaInver.Observacion;
				
				SalidaAntiguedadDeudaCuentaInversion SalidaAntiguedadDeudaCuentaInversionGeneral = new SalidaAntiguedadDeudaCuentaInversion();
				SalidaAntiguedadDeudaCuentaInversionGeneral.IdPortafolio = ctaInver.IdPortafolio;
				SalidaAntiguedadDeudaCuentaInversionGeneral.DocumentoIndentidad = clientePortafolio.DocumentoIdentidad;
				SalidaAntiguedadDeudaCuentaInversionGeneral.NombreCliente = clientePortafolio.NombreCompleto;
				SalidaAntiguedadDeudaCuentaInversionGeneral.Relacionado = clientePortafolio.Relacionado;
				SalidaAntiguedadDeudaCuentaInversionGeneral.CampoBD = ctaInver.SaldoFinal;
				SalidaAntiguedadDeudaCuentaInversionGeneral.CodigoPortafolio = clientePortafolio.CodigoPortafolio;
				SalidaAntiguedadDeudaCuentaInversionGeneral.Observacion = ctaInver.Observacion == null ? "":ctaInver.Observacion;
				SalidaAntiguedadDeudaCuentaInversionGeneral.ClasificacionContable = (clientePortafolio.Relacionado.equals("S"))? "Cuentas con Personas y Empresas Relacionados"
																																: "Cuentas por Cobrar y Cuentas de Documentos";
				
				saldosReporteAntiguedadFiltrados.addAll(saldosReporteAntiguedad.stream()
						.filter(saldoReporte -> saldoReporte.IdCliente.equals(ctaInver.IdCliente) && saldoReporte.IdPortafolio.equals(ctaInver.IdPortafolio) && saldoReporte.IdMoneda.equalsIgnoreCase(ctaInver.IdMoneda))
						.collect(Collectors.toList())
						);
				
				saldosReporteAntiguedadFiltrados.sort(Comparator.comparing(SaldosReporteAntiguedad::getFechaSistema));
				
				//Obtener Fecha Deuda
				List<FechaAntiguedadDeuda> fechaDeudaG = new ArrayList<>();
				List<FechaAntiguedadDeuda> fechaDeudaI = new ArrayList<>();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				HashMap<String, Object> param = new HashMap<>(3);
				if(in.p_SeparaIntermediacion.equalsIgnoreCase("S"))
				{
					param.put("IdCliente", clientePortafolio.IdCliente);
					param.put("IdPortafolio", clientePortafolio.IdPortafolio);
					param.put("TipoSaldo", "G");
					param.put("IdMoneda", "");
					fechaDeudaG = sql.query(con, SQL_SELECT_FECHA_ANTIGUEDAD_DEUDA, param,FechaAntiguedadDeuda.class);
					if(fechaDeudaG != null && !fechaDeudaG.isEmpty() && fechaDeudaG.get(0).FechaSistema != null) {
						LocalDate fechaLocalG = fechaDeudaG.get(0).FechaSistema.toLocalDate();
						SalidaAntiguedadDeudaCuentaInversionGeneral.FechaDeuda = fechaLocalG.format(formatter);
					}
					param.put("TipoSaldo", "I");
					fechaDeudaI = sql.query(con, SQL_SELECT_FECHA_ANTIGUEDAD_DEUDA, param,FechaAntiguedadDeuda.class);
					if(fechaDeudaI != null && !fechaDeudaI.isEmpty() && fechaDeudaI.get(0).FechaSistema != null) {
						LocalDate fechaLocalI = fechaDeudaI.get(0).FechaSistema.toLocalDate();
						SalidaAntiguedadDeudaCuentaInversionIntermediacion.FechaDeuda = fechaLocalI.format(formatter);
					}
				}else {
					param.put("IdCliente", clientePortafolio.IdCliente);
					param.put("IdPortafolio", clientePortafolio.IdPortafolio);
					param.put("TipoSaldo", "");
					param.put("IdMoneda", "");
					fechaDeudaG = sql.query(con, SQL_SELECT_FECHA_ANTIGUEDAD_DEUDA, param,FechaAntiguedadDeuda.class);
					if(fechaDeudaG != null && !fechaDeudaG.isEmpty() && fechaDeudaG.get(0).FechaSistema != null) {
						LocalDate fechaLocalG = fechaDeudaG.get(0).FechaSistema.toLocalDate();
						SalidaAntiguedadDeudaCuentaInversionGeneral.FechaDeuda = fechaLocalG.format(formatter);
					}
				}
				

				
				asignarSaldosPorTramo(in.p_SeparaIntermediacion, saldosReporteAntiguedadFiltrados, SalidaAntiguedadDeudaCuentaInversionIntermediacion, SalidaAntiguedadDeudaCuentaInversionGeneral,
						hashRangoFechas, listaFechas);
				
				repasarMontosPorTramos(ctaInver.SaldoFinal,SalidaAntiguedadDeudaCuentaInversionIntermediacion, SalidaAntiguedadDeudaCuentaInversionGeneral);
				
				darFormatoMontos(hashMoneda, listadoMonedas.get(0).IdMoneda,SalidaAntiguedadDeudaCuentaInversionGeneral, SalidaAntiguedadDeudaCuentaInversionIntermediacion);
				
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
					preSalidaServicio.add(SalidaAntiguedadDeudaCuentaInversionGeneral);
					if(SalidaAntiguedadDeudaCuentaInversionIntermediacion.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) == 1) {
						preSalidaServicio.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion);
					}
				}
				else {
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoA = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoA.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoA, MathContext.DECIMAL128);
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoB = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoB.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoB, MathContext.DECIMAL128);
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoC = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoC.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoC, MathContext.DECIMAL128);
					SalidaAntiguedadDeudaCuentaInversionGeneral.TramoD = SalidaAntiguedadDeudaCuentaInversionGeneral.TramoD.add(SalidaAntiguedadDeudaCuentaInversionIntermediacion.TramoD, MathContext.DECIMAL128);
					preSalidaServicio.add(SalidaAntiguedadDeudaCuentaInversionGeneral);
				}
			}
			
			//Totalizado
			if(in.p_AgrupaPortafolios.equalsIgnoreCase("S")) {
				for (Map.Entry<String, Object> ClienPort : hashClientePortafolio.entrySet()) {
					List<SalidaAntiguedadDeudaCuentaInversion> CuentaInversionesDeudaPorPortafolio = preSalidaServicio.stream()
																														.filter(SalidaAntiguedadDeudaCuentaInversion->SalidaAntiguedadDeudaCuentaInversion.IdPortafolio.equals(ClienPort.getKey()))
																														.collect(Collectors.toList());
					
					ClientePortafolio clientePortafolio = (ClientePortafolio) ClienPort.getValue();
					SalidaAntiguedadDeudaCuentaInversion SalidaAntiguedadDeudaCuentaInversionGeneral = new SalidaAntiguedadDeudaCuentaInversion();
					SalidaAntiguedadDeudaCuentaInversionGeneral.IdPortafolio = clientePortafolio.IdPortafolio;
					SalidaAntiguedadDeudaCuentaInversionGeneral.DocumentoIndentidad = clientePortafolio.DocumentoIdentidad;
					SalidaAntiguedadDeudaCuentaInversionGeneral.NombreCliente = clientePortafolio.NombreCompleto;
					SalidaAntiguedadDeudaCuentaInversionGeneral.Relacionado = clientePortafolio.Relacionado;
					SalidaAntiguedadDeudaCuentaInversionGeneral.ClasificacionContable = (clientePortafolio.Relacionado.equals("S"))? "Cuentas con Personas y Empresas Relacionados"
																																	: "Cuentas por Cobrar y Cuentas de Documentos";
					//Obtener Fecha Deuda
					List<FechaAntiguedadDeuda> fechaDeudaG = new ArrayList<>();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
					HashMap<String, Object> param = new HashMap<>(3);
					param.put("IdCliente", clientePortafolio.IdCliente);
					param.put("IdPortafolio", clientePortafolio.IdPortafolio);
					param.put("TipoSaldo", "");
					param.put("IdMoneda", "");
					fechaDeudaG = sql.query(con, SQL_SELECT_FECHA_ANTIGUEDAD_DEUDA, param,FechaAntiguedadDeuda.class);
					if(fechaDeudaG != null && !fechaDeudaG.isEmpty() && fechaDeudaG.get(0).FechaSistema != null) {
						LocalDate fechaLocalG = fechaDeudaG.get(0).FechaSistema.toLocalDate();
						SalidaAntiguedadDeudaCuentaInversionGeneral.FechaDeuda = fechaLocalG.format(formatter);
					}
					
					for(SalidaAntiguedadDeudaCuentaInversion salidaPreviaDeuda : CuentaInversionesDeudaPorPortafolio) {
						SalidaAntiguedadDeudaCuentaInversionGeneral.MontoPorCobrarTotal =  SalidaAntiguedadDeudaCuentaInversionGeneral.MontoPorCobrarTotal.add(salidaPreviaDeuda.MontoPorCobrarTotal, MathContext.DECIMAL128);
						SalidaAntiguedadDeudaCuentaInversionGeneral.TramoA =  SalidaAntiguedadDeudaCuentaInversionGeneral.TramoA.add(salidaPreviaDeuda.TramoA, MathContext.DECIMAL128);
						SalidaAntiguedadDeudaCuentaInversionGeneral.TramoB =  SalidaAntiguedadDeudaCuentaInversionGeneral.TramoB.add(salidaPreviaDeuda.TramoB, MathContext.DECIMAL128);
						SalidaAntiguedadDeudaCuentaInversionGeneral.TramoC =  SalidaAntiguedadDeudaCuentaInversionGeneral.TramoC.add(salidaPreviaDeuda.TramoC, MathContext.DECIMAL128);
						SalidaAntiguedadDeudaCuentaInversionGeneral.TramoD =  SalidaAntiguedadDeudaCuentaInversionGeneral.TramoD.add(salidaPreviaDeuda.TramoD, MathContext.DECIMAL128);
					}
					
					out.P_OUT_TABLA.add(SalidaAntiguedadDeudaCuentaInversionGeneral);
				}
				
			}
			else {
				out.P_OUT_TABLA.addAll(preSalidaServicio);
			}				
		}
		
		boolean eliminaCuentaAnterioresDePaso = eliminarCuentasInversionTablaPaso(con, sql);
		if(eliminaCuentaAnterioresDePaso == false) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_AL_GENERAR_REPORTE", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		logger.info("FIN::ConsultaAntiguedadDeudasCuentaInversion");
		return out;	
	}


	private List<Moneda> obtenerDistintasMonedasDesdeCuentasInversion(Connection con, SQL sql)  throws Exception{
		logger.debug("INICIO::ConsultaAntiguedadDeudasCuentaInversion.obtenerDistintasMonedasDesdeCuentasInversion");
		long inicio = System.currentTimeMillis();
		HashMap<String, Object> param = new HashMap<>();
		List<Moneda> listaMonedas = sql.query(con, SQL_SELECT_DISTINT_MONEDA_CUENTA_INVERSION, param, Moneda.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaDeudasCuentaInversion.obtenerDistintasMonedasDesdeCuentasInversion", fin - inicio);
		return listaMonedas;
	}
	
	private Map<String, FechaBusqueda> obtenerRangosDeFechas(Connection con, EntradaConsultaAntiguedadDeudasCuentaInversion in, Long IdPais, List<LocalDate> listaFechas) throws Exception{
		logger.debug("INICIO::ConsultaAntiguedadDeudasCuentaInversion.obtenerRangosDeFechas");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, FechaBusqueda> hashRangosFechas = new HashMap<>(3);
		
		ObtenerDiaAnterior obteDiaAnterior = new ObtenerDiaAnterior();
		ObtenerDiaAnterior.InObtenerDiaAnterior entradaObteDiaAnterior = new ObtenerDiaAnterior.InObtenerDiaAnterior();
		entradaObteDiaAnterior.idPais = IdPais;
		entradaObteDiaAnterior.diaHabil = "N";
		entradaObteDiaAnterior.soloCaeDiaHabil = "N";
		
		int diasRetroceso = 31;
		for(int i = 0; i <= diasRetroceso; i++) {
			ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn f = new ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn(in.p_FechaConsulta, new Long(i));
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
		//llamada para armar rangos fechas
		mapearRangoDeFecha("TRAMO_A", hashRangosFechas, primerTramofechas);
		mapearRangoDeFecha("TRAMO_B", hashRangosFechas, segundoTramofechas);
		mapearRangoDeFecha("TRAMO_C", hashRangosFechas, tercerTramofechas);
		mapearRangoDeFecha("TRAMO_D", hashRangosFechas, cuartoTramofechas);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaAntiguedadDeudasCuentaInversion.obtenerRangosDeFechas", fin - inicio);
		return hashRangosFechas;
	}
	
	private void mapearRangoDeFecha (String rango, HashMap<String, FechaBusqueda> HashFecha ,  List<String> listaFecha){
		logger.debug("INICIO::ConsultaAntiguedadDeudasCuentaInversion.mapearRangoDeFecha");
		long inicio = System.currentTimeMillis();
		
		String fechaMinima = Collections.min(listaFecha);
		String fechaMaxima = Collections.max(listaFecha);
		FechaBusqueda fechaBusq = new FechaBusqueda();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		fechaBusq.FechaInicio = LocalDate.parse(fechaMinima, formatter);
		fechaBusq.FechaFin = LocalDate.parse(fechaMaxima, formatter);
		
		HashFecha.put(rango, fechaBusq);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaAntiguedadDeudasCuentaInversion.mapearRangoDeFecha", fin - inicio);
	}
	
	private List<SaldoPorFechaEfectiva> obtenerCuentasInversionNegativas(Connection con, EntradaConsultaAntiguedadDeudasCuentaInversion in, List<Moneda> monedas) throws Exception {
		logger.debug("INICIO::ConsultaAntiguedadDeudasCuentaInversion.obtenerCuentasInversionNegativas");
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
		
		for(Moneda moneda : monedas) {
			entradaConsultarSaldoCuenta.p_IdMoneda = moneda.IdMoneda;	
			
			List<InCuentaInversionConsultar> lstCuentaInversionConsultar = new ArrayList<InCuentaInversionConsultar>();
			InCuentaInversionConsultar objCuentaInversionConsultar = new InCuentaInversionConsultar();
			objCuentaInversionConsultar.IdEjecutivo  = in.p_IdEjecutivo;
			objCuentaInversionConsultar.IdMoneda 	 = moneda.IdMoneda;
			objCuentaInversionConsultar.IdPortafolio = null; 
			lstCuentaInversionConsultar.add(objCuentaInversionConsultar);
			entradaConsultarSaldoCuenta.p_CuentaInversionAConsultar = lstCuentaInversionConsultar;
			
			SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuenta = servicioConsultarSaldoCuenta.logica(con, entradaConsultarSaldoCuenta);
			if(salidaConsultarSaldoCuenta.P_OUT_ERRORESCONTADOR.compareTo(0L)==0 && !salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty()){

				//Si la moneda es CLP se debe truncar el Saldo disponible
				if(moneda.IdMoneda.equalsIgnoreCase("CLP")) {
					List<SaldoPorFechaEfectiva> cuentasInversionNegativas = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
							.filter(Saldo-> Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128).setScale(0, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) == -1)
							.collect(Collectors.toList());
					
					ListaCuentasInversion.addAll(cuentasInversionNegativas);
				}else {
					List<SaldoPorFechaEfectiva> cuentasInversionNegativas = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
							.filter(Saldo-> Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128).setScale(2, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) == -1)
							.collect(Collectors.toList());
					
					ListaCuentasInversion.addAll(cuentasInversionNegativas);
				}
				
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaAntiguedadDeudasCuentaInversion.obtenerCuentasInversionNegativas", fin - inicio);
		
		return ListaCuentasInversion;
	}
	
	
	private List<SaldoPorFechaEfectiva> obtenerCuentasInversion(Connection con, EntradaConsultaAntiguedadDeudasCuentaInversion in, String moneda, List<ClientesConsolidado> clientesConsolidado) throws Exception {
		logger.debug("INICIO::ConsultaAntiguedadDeudasCuentaInversion.obtenerCuentasInversion");
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
		logger.debug("FIN::ConsultaAntiguedadDeudasCuentaInversion.obtenerCuentasInversion", fin - inicio);
		
		return ListaCuentasInversion;
	}
	
	private List<CuentaInversion> prepararSaldoFinalCuentaInversion(List<SaldoPorFechaEfectiva> SaldosCuentaInversion, String Consolidado){
		logger.debug("INICIO::ConsultaAntiguedadDeudasCuentaInversion.prepararCuentaInversionTablaPaso");
		long inicio = System.currentTimeMillis();
		List<CuentaInversion> listaCuentaInversionDePaso = new ArrayList<>();
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
		logger.debug("FIN::ConsultaAntiguedadDeudasCuentaInversion.prepararCuentaInversionTablaPaso", fin - inicio);
		
		return listaCuentaInversionDePaso;
	}
	
	private boolean eliminarCuentasInversionTablaPaso(Connection con, SQL sql) {
		logger.debug("INICIO::ConsultaAntiguedadDeudasCuentaInversion.eliminarCuentasInversionTablaPaso");
		long inicio = System.currentTimeMillis();
		boolean resultado = false;
		
		try {
			sql.batch(con, SQL_DELETE_CUENTA_INVERSION_PASO , new ArrayList<>() , CuentaInversion.class);
			resultado = true;
		}catch(SQLException SQLe) {
			
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaAntiguedadDeudasCuentaInversion.eliminarCuentasInversionTablaPaso", fin - inicio);
		return resultado;
	}
	
	private boolean insertarCuentasInversionTablaPaso(Connection con, SQL sql, List<CuentaInversion> cuentaInversionPaso, Long idTrabajo) {
		logger.debug("INICIO::ConsultaAntiguedadDeudasCuentaInversion.insertarCuentasInversionTablaPaso");
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
		logger.debug("FIN::ConsultaAntiguedadDeudasCuentaInversion.insertarCuentasInversionTablaPaso", fin - inicio);
		
		return resultado;
	}
	
	private List<ClientePortafolio> obtenerClientesPorPortafolio(Connection con, SQL sql, Long IdTrabajo) throws Exception{
		logger.debug("INICIO::ConsultaAntiguedadDeudasCuentaInversion.obtenerClientesPorPortafolio");
		long inicio = System.currentTimeMillis();
		List<ClientePortafolio> listaClientes = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(1);
		param.put("IdTrabajo", IdTrabajo);
		listaClientes = sql.query(con, SQL_SELECT_DATOS_CLIENTE , param , ClientePortafolio.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaAntiguedadDeudasCuentaInversion.obtenerClientesPorPortafolio", fin - inicio);
		
		return listaClientes;
	}
		
	private List<CalculoSalida> ObtenerCalculoParidad(Connection con , Long idEmpresa, List<CalculoEntrada> paridades, String tipoProceso) throws Exception {
		logger.trace("INICIO::ConsultaAntiguedadDeudasCuentaInversion.ObtenerCalculoParidad"+tipoProceso);
		long inicio = System.currentTimeMillis();
		List<CalculoSalida> paridadesConvertidas = new ArrayList<>();
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada entradaCalcularParidad = new CalcularParidadEntrada();
		entradaCalcularParidad.p_IdEmpresa = idEmpresa;
		entradaCalcularParidad.p_IdTrabajo = 0L;
		entradaCalcularParidad.p_CalculosEntrada = paridades;
		CalcularParidadSalida salidaCalculoParidad = calcularParidad.logica(con, entradaCalcularParidad);
		if( salidaCalculoParidad.P_OUT_ERRORESCONTADOR.equals(0L) && !salidaCalculoParidad.p_out_CalculosSalida.isEmpty()) {
			paridadesConvertidas = salidaCalculoParidad.p_out_CalculosSalida;
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaDeudasCuentaInversion.ObtenerCalculoParidad"+tipoProceso, fin - inicio);
		
		return paridadesConvertidas;
	}
	
	private List<CuentaInversion> convertirSaldosParidadCTA(List<CuentaInversion> listCTADistintaCLP, List<CalculoSalida> listParidadSaldoInicial) throws Exception {
		logger.trace("INICIO::ConsultaAntiguedadDeudasCuentaInversion.convertirSaldosParidadCTA");
		long inicio = System.currentTimeMillis();
		
		List<CuentaInversion> listaCTAconvertidaParidad = new ArrayList<>();
		HashMap<Long, Object> hashParidadSaldoInicial = Utils.List2Hash(listParidadSaldoInicial,"IdInterno");
		
		for(CuentaInversion cta : listCTADistintaCLP) {
			CuentaInversion CTAconvertida = new CuentaInversion();
			CTAconvertida.IdCuentaInversion = cta.IdCuentaInversion;
			CTAconvertida.IdCliente = cta.IdCliente;
			CTAconvertida.IdMoneda = cta.IdMoneda;
			CTAconvertida.IdPortafolio = cta.IdPortafolio;
			CTAconvertida.IdCuentaInversion = cta.IdCuentaInversion;
			CTAconvertida.FechaSaldoInicial = cta.FechaSaldoInicial;
			CTAconvertida.SaldoEsIntermediacion= cta.SaldoEsIntermediacion;
			if(hashParidadSaldoInicial.containsKey(cta.IdCuentaInversion)) {
				CalculoSalida calculoSalidaSaldoInicial = (CalculoSalida) hashParidadSaldoInicial.get(cta.IdCuentaInversion);
				CTAconvertida.SaldoFinal	= calculoSalidaSaldoInicial.MontoDestino == null ? BigDecimal.ZERO : calculoSalidaSaldoInicial.MontoDestino;
				CTAconvertida.Observacion	= calculoSalidaSaldoInicial.MontoDestino == null ? "Error en la paridad" : "";
			}
			listaCTAconvertidaParidad.add(CTAconvertida);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaDeudasCuentaInversion.convertirSaldosParidadCTA", fin - inicio);
		return listaCTAconvertidaParidad;
	}
	
	private List<SaldosReporteAntiguedad> convertirParidadSaldosReporteAntiguedad(List<SaldosReporteAntiguedad> listaSaldosReporteAntiguedad , List<CalculoSalida> paradidasObtenidas) throws Exception {
		logger.debug("INICIO::ConsultaAntiguedadDeudasCuentaInversion.convertirParidadSaldosReporteAntiguedad");
		long inicio = System.currentTimeMillis();
		
		List<SaldosReporteAntiguedad> listaSaldosReporteAntiguedadConvertidaParidad = new ArrayList<>();
		HashMap<Long, Object> hashParidadSaldoReporte = Utils.List2Hash(paradidasObtenidas,"IdInterno");
		
		for(SaldosReporteAntiguedad saldoReporteAntiguedad : listaSaldosReporteAntiguedad) {
			SaldosReporteAntiguedad saldosReporteAntiguedadConvertido = new SaldosReporteAntiguedad();
			saldosReporteAntiguedadConvertido.FechaSistema		= saldoReporteAntiguedad.FechaSistema;
			saldosReporteAntiguedadConvertido.IdCliente			= saldoReporteAntiguedad.IdCliente;
			saldosReporteAntiguedadConvertido.IdPortafolio		= saldoReporteAntiguedad.IdPortafolio;
			saldosReporteAntiguedadConvertido.IdMoneda			= saldoReporteAntiguedad.IdMoneda;
			saldosReporteAntiguedadConvertido.TipoSaldo			= saldoReporteAntiguedad.TipoSaldo;
			saldosReporteAntiguedadConvertido.NumeroRegistro	= saldoReporteAntiguedad.NumeroRegistro;
			if(hashParidadSaldoReporte.containsKey(saldoReporteAntiguedad.NumeroRegistro)) {
				CalculoSalida calculoSalidaMovCargo = (CalculoSalida) hashParidadSaldoReporte.get(saldoReporteAntiguedad.NumeroRegistro);
				saldosReporteAntiguedadConvertido.Saldo = calculoSalidaMovCargo.MontoDestino == null ? BigDecimal.ZERO : calculoSalidaMovCargo.MontoDestino;
			}
			else {
				saldosReporteAntiguedadConvertido.Saldo = BigDecimal.ZERO;
			}
			listaSaldosReporteAntiguedadConvertidaParidad.add(saldosReporteAntiguedadConvertido);
		}
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaDeudasCuentaInversion.convertirParidadSaldosReporteAntiguedad", fin - inicio);
		return listaSaldosReporteAntiguedadConvertidaParidad;
	}

	public void darFormatoMontos(Map<String,Moneda> hashMoneda, String idMoneda,SalidaAntiguedadDeudaCuentaInversion antiguedadDeudaGeneral, SalidaAntiguedadDeudaCuentaInversion antiguedadDeudaIntermediacion) {
		logger.trace("INICIO::ConsultaAntiguedadDeudasCuentaInversion.darFormatoMontos");
		long inicio = System.currentTimeMillis();
		
		if(idMoneda.equalsIgnoreCase("CLP") || hashMoneda.size() > 1) {
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
		logger.trace("FIN::ConsultaDeudasCuentaInversion.darFormatoMontos", fin - inicio);
	}

	private String buscarTramo(Map<String, FechaBusqueda> hashRangoFechas, LocalDate fechaConsultada) {
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
		
		return tramo;
	}
	
	private List<SaldosReporteAntiguedad> obtenerSaldosReporteAntiguedad (Connection con, SQL sql, Long idTrabajo, Map<String, FechaBusqueda> hashRangoFechas) throws SQLException{
		logger.debug("INICIO::ConsultaAntiguedadDeudasCuentaInversion.obtenerSaldosReporteAntiguedad");
		long inicio = System.currentTimeMillis();
		
		List<SaldosReporteAntiguedad> saldosReporteAntiguedad = new ArrayList<>();
		
		FechaBusqueda fechaBusqVigente 		= hashRangoFechas.get("TRAMO_A");
		FechaBusqueda fechaBusqMasDe30Dias 	= hashRangoFechas.get("TRAMO_D");
		
		String fechaFin		= fechaBusqVigente.FechaFin.toString();
		String fechaInicio	= fechaBusqMasDe30Dias.FechaFin.toString();
		
		HashMap<String, Object> param = new HashMap<>(3);
		param.put("IdTrabajo", idTrabajo);
		param.put("FechaInicio", fechaInicio);
		param.put("FechaFin", fechaFin);
		
		saldosReporteAntiguedad = sql.query(con, SQL_SELECT_SALDOS_REPORTE_ANTIGUEDAD, param,SaldosReporteAntiguedad.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultaDeudasCuentaInversion.obtenerSaldosReporteAntiguedad", fin - inicio);
		
		return saldosReporteAntiguedad;
	}
	
	private void asignarSaldosPorTramo(String separaIntermediacion, List<SaldosReporteAntiguedad> saldosReporteAntiguedadFiltrados, SalidaAntiguedadDeudaCuentaInversion antiguedadDeudaIntermediacion, SalidaAntiguedadDeudaCuentaInversion antiguedadDeudaGeneral,
			Map<String, FechaBusqueda> hashRangoFechas,List<LocalDate> listaFechas){
		logger.trace("INICIO::ConsultaAntiguedadDeudasCuentaInversion.asignarSaldosPorTramo");
		long inicio = System.currentTimeMillis();
			
		for(LocalDate fecha:listaFechas) {
	
			String tramoSaldo = buscarTramo(hashRangoFechas,fecha);
			
			List<SaldosReporteAntiguedad> saldoFiltradosPorFecha = saldosReporteAntiguedadFiltrados.stream()
					.filter(saldo -> saldo.FechaSistema.toLocalDate().equals(fecha))
					.collect(Collectors.toList());
			
			
			BigDecimal saldoAuxiliarTramo = BigDecimal.ZERO;
			
			if(saldoFiltradosPorFecha.size() > 0) {
				
				saldoAuxiliarTramo = saldoFiltradosPorFecha.stream()
						.map(mov->mov.Saldo)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				
				if(saldoAuxiliarTramo.compareTo(BigDecimal.ZERO) >= 0) {
					borrarDeudaTramoAnterior(antiguedadDeudaIntermediacion, antiguedadDeudaGeneral, tramoSaldo);
				}
				
				List<SaldosReporteAntiguedad> saldosFiltradosIntermediacion = saldoFiltradosPorFecha.stream()
						.filter(saldo -> saldo.TipoSaldo.equalsIgnoreCase("I"))
						.collect(Collectors.toList());
				
				List<SaldosReporteAntiguedad> saldosFiltradosGeneral = saldoFiltradosPorFecha.stream()
						.filter(saldo -> saldo.TipoSaldo.equalsIgnoreCase("G"))
						.collect(Collectors.toList());
				
				
				if(saldosFiltradosIntermediacion.size() > 0 && saldosFiltradosGeneral.size() > 0) {
					
					BigDecimal saldoIntermediacion = saldosFiltradosIntermediacion.stream()
							.map(mov->mov.Saldo)
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					
					BigDecimal saldoGeneral = saldosFiltradosGeneral.stream()
							.map(mov->mov.Saldo)
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					
					if(tramoSaldo.equalsIgnoreCase("TRAMO_A")) {
						if(separaIntermediacion.equalsIgnoreCase("S")) {
							antiguedadDeudaIntermediacion.TramoA = saldoIntermediacion;
							antiguedadDeudaGeneral.TramoA = saldoGeneral;
						}else {
							antiguedadDeudaGeneral.TramoA = saldoGeneral.add(saldoIntermediacion, MathContext.DECIMAL128);
						}
					}else if(tramoSaldo.equalsIgnoreCase("TRAMO_B")) {
						if(separaIntermediacion.equalsIgnoreCase("S")) {
							antiguedadDeudaIntermediacion.TramoB = saldoIntermediacion;
							antiguedadDeudaGeneral.TramoB = saldoGeneral;
						}else {
							antiguedadDeudaGeneral.TramoB = saldoGeneral.add(saldoIntermediacion, MathContext.DECIMAL128);
						}
					}else if(tramoSaldo.equalsIgnoreCase("TRAMO_C")) {
						if(separaIntermediacion.equalsIgnoreCase("S")) {
							antiguedadDeudaIntermediacion.TramoC = saldoIntermediacion;
							antiguedadDeudaGeneral.TramoC = saldoGeneral;
						}else {
							antiguedadDeudaGeneral.TramoC = saldoGeneral.add(saldoIntermediacion, MathContext.DECIMAL128);
						}
					}else if(tramoSaldo.equalsIgnoreCase("TRAMO_D")) {
						if(separaIntermediacion.equalsIgnoreCase("S")) {
							antiguedadDeudaIntermediacion.TramoD = saldoIntermediacion;
							antiguedadDeudaGeneral.TramoD = saldoGeneral;
						}else {
							antiguedadDeudaGeneral.TramoD = saldoGeneral.add(saldoIntermediacion, MathContext.DECIMAL128);
						}
					}
					
				}
			}
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaDeudasCuentaInversion.asignarSaldosPorTramo", fin - inicio);	
	}
	
	private void borrarDeudaTramoAnterior(SalidaAntiguedadDeudaCuentaInversion antiguedadDeudaIntermediacion, SalidaAntiguedadDeudaCuentaInversion antiguedadDeudaGeneral, String tramoSaldo) {
		logger.trace("INICIO::ConsultaAntiguedadDeudasCuentaInversion.borrarDeudaTramoAnterior");
		long inicio = System.currentTimeMillis();
		
		if(tramoSaldo.equalsIgnoreCase("TRAMO_A")) {
			antiguedadDeudaIntermediacion.TramoB = antiguedadDeudaIntermediacion.TramoC = antiguedadDeudaIntermediacion.TramoD = BigDecimal.ZERO;
			antiguedadDeudaGeneral.TramoB = antiguedadDeudaGeneral.TramoC = antiguedadDeudaGeneral.TramoD = BigDecimal.ZERO;
		}else if(tramoSaldo.equalsIgnoreCase("TRAMO_B")){
			antiguedadDeudaIntermediacion.TramoC = antiguedadDeudaIntermediacion.TramoD = BigDecimal.ZERO;
			antiguedadDeudaGeneral.TramoC = antiguedadDeudaGeneral.TramoD = BigDecimal.ZERO;
		}else if(tramoSaldo.equalsIgnoreCase("TRAMO_C")) {
			antiguedadDeudaIntermediacion.TramoD = BigDecimal.ZERO;
			antiguedadDeudaGeneral.TramoD = BigDecimal.ZERO;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaDeudasCuentaInversion.borrarDeudaTramoAnterior", fin - inicio);		
	}
	
	private void repasarMontosPorTramos(BigDecimal saldoFinal, SalidaAntiguedadDeudaCuentaInversion antiguedadDeudaIntermediacion, SalidaAntiguedadDeudaCuentaInversion antiguedadDeudaGeneral) {
		logger.trace("INICIO::ConsultaAntiguedadDeudasCuentaInversion.repasarMontosPorTramos");
		long inicio = System.currentTimeMillis();
		
		BigDecimal SaldoTramoAIntermediacion = BigDecimal.ZERO;
		BigDecimal SaldoTramoAGeneral = BigDecimal.ZERO;
		BigDecimal SaldoTramoBIntermediacion = BigDecimal.ZERO;
		BigDecimal SaldoTramoBGeneral = BigDecimal.ZERO;
		BigDecimal SaldoTramoCIntermediacion = BigDecimal.ZERO;
		BigDecimal SaldoTramoCGeneral = BigDecimal.ZERO;
		BigDecimal SaldoTramoDIntermediacion = BigDecimal.ZERO;
		BigDecimal SaldoTramoDGeneral = BigDecimal.ZERO;
		
		SaldoTramoDGeneral = antiguedadDeudaGeneral.TramoD;
		SaldoTramoDIntermediacion = antiguedadDeudaIntermediacion.TramoD;
		
		SaldoTramoCGeneral = antiguedadDeudaGeneral.TramoC;
		SaldoTramoCIntermediacion = antiguedadDeudaIntermediacion.TramoC;
		
		SaldoTramoBGeneral = antiguedadDeudaGeneral.TramoB;
		SaldoTramoBIntermediacion = antiguedadDeudaIntermediacion.TramoB;
		
		SaldoTramoAGeneral = antiguedadDeudaGeneral.TramoA;
		SaldoTramoAIntermediacion = antiguedadDeudaIntermediacion.TramoA;
				
		BigDecimal saldoAuxiliarTramoDGeneral = BigDecimal.ZERO;
		BigDecimal saldoAuxiliarTramoCGeneral = BigDecimal.ZERO;
		BigDecimal saldoAuxiliarTramoBGeneral = BigDecimal.ZERO;
		BigDecimal saldoAuxiliarTramoAGeneral = BigDecimal.ZERO;
		BigDecimal saldoAuxiliarTramoDIntermediacion = BigDecimal.ZERO;
		BigDecimal saldoAuxiliarTramoCIntermediacion = BigDecimal.ZERO;
		BigDecimal saldoAuxiliarTramoBIntermediacion = BigDecimal.ZERO;
		BigDecimal saldoAuxiliarTramoAIntermediacion = BigDecimal.ZERO;
		
		//Eliminar saldo inicial positivo en otro tramo
		if(SaldoTramoDGeneral.compareTo(BigDecimal.ZERO) > 0) {
			SaldoTramoDGeneral = BigDecimal.ZERO;
		}
		if(SaldoTramoDIntermediacion.compareTo(BigDecimal.ZERO) > 0) {
			SaldoTramoDIntermediacion = BigDecimal.ZERO;
		}
		if(SaldoTramoCGeneral.compareTo(BigDecimal.ZERO) > 0) {
			SaldoTramoCGeneral = BigDecimal.ZERO;
		}
		if(SaldoTramoCIntermediacion.compareTo(BigDecimal.ZERO) > 0) {
			SaldoTramoCIntermediacion = BigDecimal.ZERO;
		}
		if(SaldoTramoBGeneral.compareTo(BigDecimal.ZERO) > 0) {
			SaldoTramoBGeneral = BigDecimal.ZERO;
		}
		if(SaldoTramoBIntermediacion.compareTo(BigDecimal.ZERO) > 0) {
			SaldoTramoBIntermediacion = BigDecimal.ZERO;
		}
		if(SaldoTramoAGeneral.compareTo(BigDecimal.ZERO) > 0) {
			SaldoTramoAGeneral = BigDecimal.ZERO;
		}
		if(SaldoTramoAIntermediacion.compareTo(BigDecimal.ZERO) > 0) {
			SaldoTramoAIntermediacion = BigDecimal.ZERO;
		}
		
		//Obtener el monto real de deuda por tramo (intermediacion y general)
		if(SaldoTramoDGeneral.compareTo(SaldoTramoCGeneral) == 0){
			saldoAuxiliarTramoCGeneral = BigDecimal.ZERO;
		}else {
			saldoAuxiliarTramoCGeneral = SaldoTramoCGeneral.subtract(SaldoTramoDGeneral, MathContext.DECIMAL128);
		}
		if(SaldoTramoDIntermediacion.compareTo(SaldoTramoCIntermediacion) == 0) {
			saldoAuxiliarTramoCIntermediacion = BigDecimal.ZERO;
		}else {
			saldoAuxiliarTramoCIntermediacion = SaldoTramoCIntermediacion.subtract(SaldoTramoDIntermediacion, MathContext.DECIMAL128);
		}
		if(SaldoTramoCGeneral.compareTo(SaldoTramoBGeneral) == 0) {
			saldoAuxiliarTramoBGeneral = BigDecimal.ZERO;
		}else {
			saldoAuxiliarTramoBGeneral = SaldoTramoBGeneral.subtract(SaldoTramoCGeneral, MathContext.DECIMAL128);
		}
		if(SaldoTramoCIntermediacion.compareTo(SaldoTramoBIntermediacion) == 0) {
			saldoAuxiliarTramoBIntermediacion = BigDecimal.ZERO;
		}else {
			saldoAuxiliarTramoBIntermediacion = SaldoTramoBIntermediacion.subtract(SaldoTramoCIntermediacion, MathContext.DECIMAL128);
		}
		if(SaldoTramoBGeneral.compareTo(SaldoTramoAGeneral) == 0) {
			saldoAuxiliarTramoAGeneral = BigDecimal.ZERO;
		}else {
			saldoAuxiliarTramoAGeneral = SaldoTramoAGeneral.subtract(SaldoTramoBGeneral, MathContext.DECIMAL128);
		}
		if(SaldoTramoBIntermediacion.compareTo(SaldoTramoAIntermediacion) == 0) {
			saldoAuxiliarTramoAIntermediacion = BigDecimal.ZERO;
		}else {
			saldoAuxiliarTramoAIntermediacion = SaldoTramoAIntermediacion.subtract(SaldoTramoBIntermediacion, MathContext.DECIMAL128);
		}
		
		saldoAuxiliarTramoDGeneral = SaldoTramoDGeneral;
		saldoAuxiliarTramoDIntermediacion = SaldoTramoDIntermediacion;
		List<BigDecimal> saldosAuxiliaresGeneral = new ArrayList();
		List<BigDecimal> saldosAuxiliaresIntermediacion = new ArrayList();
		
		saldosAuxiliaresGeneral.add(saldoAuxiliarTramoAGeneral);//0
		saldosAuxiliaresGeneral.add(saldoAuxiliarTramoBGeneral);//1
		saldosAuxiliaresGeneral.add(saldoAuxiliarTramoCGeneral);//2
		saldosAuxiliaresGeneral.add(saldoAuxiliarTramoDGeneral);//3
		
		saldosAuxiliaresIntermediacion.add(saldoAuxiliarTramoAIntermediacion);
		saldosAuxiliaresIntermediacion.add(saldoAuxiliarTramoBIntermediacion);
		saldosAuxiliaresIntermediacion.add(saldoAuxiliarTramoCIntermediacion);
		saldosAuxiliaresIntermediacion.add(saldoAuxiliarTramoDIntermediacion);
		
		//Repasar la variacion del saldo por tramo
		if(saldosAuxiliaresGeneral.get(2).compareTo(BigDecimal.ZERO) > 0){
			saldosAuxiliaresGeneral.set(3, saldosAuxiliaresGeneral.get(3).add(saldosAuxiliaresGeneral.get(2),MathContext.DECIMAL128));
			saldosAuxiliaresGeneral.set(2, BigDecimal.ZERO);
		}

		if(saldosAuxiliaresIntermediacion.get(2).compareTo(BigDecimal.ZERO) > 0){
			saldosAuxiliaresIntermediacion.set(3, saldosAuxiliaresIntermediacion.get(3).add(saldosAuxiliaresIntermediacion.get(2),MathContext.DECIMAL128));
			saldosAuxiliaresIntermediacion.set(2, BigDecimal.ZERO);
		}

		if(saldosAuxiliaresGeneral.get(1).compareTo(BigDecimal.ZERO) > 0) {
			if(saldosAuxiliaresGeneral.get(2).add(saldosAuxiliaresGeneral.get(1), MathContext.DECIMAL128).compareTo(BigDecimal.ZERO) <= 0) {
				saldosAuxiliaresGeneral.set(2, saldosAuxiliaresGeneral.get(2).add(saldosAuxiliaresGeneral.get(1),MathContext.DECIMAL128));
				saldosAuxiliaresGeneral.set(1, BigDecimal.ZERO);
			}else {
				BigDecimal SaldoAuxiliarCiclo = BigDecimal.ZERO;
				for(int i=2; i<4;i++) {
					SaldoAuxiliarCiclo = saldosAuxiliaresGeneral.get(i).add(saldosAuxiliaresGeneral.get(i-1), MathContext.DECIMAL128);
					saldosAuxiliaresGeneral.set(i, SaldoAuxiliarCiclo);
					saldosAuxiliaresGeneral.set(i-1, BigDecimal.ZERO);
					if(SaldoAuxiliarCiclo.compareTo(BigDecimal.ZERO) <= 0) {
						break;
					}
				}
			}
		}

		if(saldosAuxiliaresIntermediacion.get(1).compareTo(BigDecimal.ZERO) > 0) {
			if(saldosAuxiliaresIntermediacion.get(2).add(saldosAuxiliaresIntermediacion.get(1), MathContext.DECIMAL128).compareTo(BigDecimal.ZERO) <= 0) {
				saldosAuxiliaresIntermediacion.set(2, saldosAuxiliaresIntermediacion.get(2).add(saldosAuxiliaresIntermediacion.get(1),MathContext.DECIMAL128));
				saldosAuxiliaresIntermediacion.set(1, BigDecimal.ZERO);
			}else {
				BigDecimal SaldoAuxiliarCiclo = BigDecimal.ZERO;
				for(int i=2; i<4;i++) {
					SaldoAuxiliarCiclo = saldosAuxiliaresIntermediacion.get(i).add(saldosAuxiliaresIntermediacion.get(i-1), MathContext.DECIMAL128);
					saldosAuxiliaresIntermediacion.set(i, SaldoAuxiliarCiclo);
					saldosAuxiliaresIntermediacion.set(i-1, BigDecimal.ZERO);
					if(SaldoAuxiliarCiclo.compareTo(BigDecimal.ZERO) <= 0) {
						break;
					}
				}
			}
		}

		if(saldosAuxiliaresGeneral.get(0).compareTo(BigDecimal.ZERO) > 0) {
			if(saldosAuxiliaresGeneral.get(1).add(saldosAuxiliaresGeneral.get(0), MathContext.DECIMAL128).compareTo(BigDecimal.ZERO) <= 0) {
				saldosAuxiliaresGeneral.set(1, saldosAuxiliaresGeneral.get(0).add(saldosAuxiliaresGeneral.get(1),MathContext.DECIMAL128));
				saldosAuxiliaresGeneral.set(0, BigDecimal.ZERO);
			}else {
				BigDecimal SaldoAuxiliarCiclo = BigDecimal.ZERO;
				for(int i=1; i<4;i++) {
					SaldoAuxiliarCiclo = saldosAuxiliaresGeneral.get(i).add(saldosAuxiliaresGeneral.get(i-1), MathContext.DECIMAL128);
					saldosAuxiliaresGeneral.set(i, SaldoAuxiliarCiclo);
					saldosAuxiliaresGeneral.set(i-1, BigDecimal.ZERO);
					if(SaldoAuxiliarCiclo.compareTo(BigDecimal.ZERO) <= 0) {
						break;
					}
				}
			}
		}
		
		if(saldosAuxiliaresIntermediacion.get(0).compareTo(BigDecimal.ZERO) > 0) {
			if(saldosAuxiliaresIntermediacion.get(1).add(saldosAuxiliaresIntermediacion.get(0), MathContext.DECIMAL128).compareTo(BigDecimal.ZERO) <= 0) {
				saldosAuxiliaresIntermediacion.set(1, saldosAuxiliaresIntermediacion.get(0).add(saldosAuxiliaresIntermediacion.get(1),MathContext.DECIMAL128));
				saldosAuxiliaresIntermediacion.set(0, BigDecimal.ZERO);
			}else {
				BigDecimal SaldoAuxiliarCiclo = BigDecimal.ZERO;
				for(int i=1; i<4;i++) {
					SaldoAuxiliarCiclo = saldosAuxiliaresIntermediacion.get(i).add(saldosAuxiliaresIntermediacion.get(i-1), MathContext.DECIMAL128);
					saldosAuxiliaresIntermediacion.set(i, SaldoAuxiliarCiclo);
					saldosAuxiliaresIntermediacion.set(i-1, BigDecimal.ZERO);
					if(SaldoAuxiliarCiclo.compareTo(BigDecimal.ZERO) <= 0) {
						break;
					}
				}
			}
		}
				
		//Asignar los montos finales
		antiguedadDeudaGeneral.TramoD = saldosAuxiliaresGeneral.get(3);
		antiguedadDeudaGeneral.TramoC = saldosAuxiliaresGeneral.get(2);
		antiguedadDeudaGeneral.TramoB = saldosAuxiliaresGeneral.get(1);
		antiguedadDeudaGeneral.TramoA = saldosAuxiliaresGeneral.get(0);
		
		antiguedadDeudaIntermediacion.TramoD = saldosAuxiliaresIntermediacion.get(3);
		antiguedadDeudaIntermediacion.TramoC = saldosAuxiliaresIntermediacion.get(2);
		antiguedadDeudaIntermediacion.TramoB = saldosAuxiliaresIntermediacion.get(1);
		antiguedadDeudaIntermediacion.TramoA = saldosAuxiliaresIntermediacion.get(0);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultaDeudasCuentaInversion.repasarMontosPorTramos", fin - inicio);	
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaConsultaAntiguedadDeudasCuentaInversion in= new EntradaConsultaAntiguedadDeudasCuentaInversion();
			in.p_IdUsuario = 9L;
			in.p_IdModulo = "CTA";
			in.p_IdEmpresa = 1L;
			in.p_FechaConsulta = "2023-04-17";
			in.p_IdMoneda = "TODAS";
			in.p_AgrupaPortafolios = "N";
			in.p_SeparaIntermediacion = "N";
			in.p_Consolidar = "N";
			ConsultaAntiguedadDeudasCuentaInversion demo = new ConsultaAntiguedadDeudasCuentaInversion();
			try {
				SalidaConsultaAntiguedadDeudasCuentaInversion ObjOut = demo.logica(con, in);
				System.out.println(ObjOut.toString());
				con.commit();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
				//con.rollback(); 			
	    }			
		System.out.println("== END TEST==");
	}
	
}