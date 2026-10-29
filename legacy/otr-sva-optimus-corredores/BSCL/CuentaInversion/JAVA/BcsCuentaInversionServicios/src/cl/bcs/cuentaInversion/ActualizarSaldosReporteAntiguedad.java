package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarSaldosReporteAntiguedad implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(ActualizarSaldosReporteAntiguedad.class);
	
	private final String SQL_SELECT_DATOS_CLIENTE					= loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldosReporteAntiguedad_SelectDatosCliente.sql");
	private final String SQL_SELECT_MOVIMIENTOS_CARGO_ABONO			= loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldosReporteAntiguedad_SelectMovimientosCargoAbono.sql");
	private final String SQL_SELECT_SALDO_REPORTE_ANTIGUEDAD		= loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldosReporteAntiguedad_SelectSaldoReporteAntiguedad.sql");
	private final String SQL_UPSERT_SALDOS_REPORTE_ANTIGUEDAD		= loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldosReporteAntiguedad_UpsertSaldoReporteAntiguedad.sql");
	
	private final String MovimientoCargo = "MOVCAR";
	private final String c_NO = "N";

	public static class EntradaActualizarSaldosReporteAntiguedad extends BcsPojo{
		public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_Rut;
	    public String p_CodigoPortafolio;
	    public String p_Moneda;
		public String p_FechaMovimiento;
	}
	
	
	public static class SalidaActualizarSaldosReporteAntiguedad extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public SalidaActualizarSaldosReporteAntiguedad() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class SaldosReporteAntiguedad extends BcsPojo{
		public Date FechaSistema;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public String TipoSaldo;	
		public BigDecimal Saldo;
	}
	
	public static class Movimiento extends BcsPojo{
		public Long IdCargoAbonoMovimiento;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public String TipoSaldo;	
		public BigDecimal Monto;
		public Date FechaMovimiento;
		public String IdTipoMovimiento;
	}
	
	public static class Cliente extends BcsPojo{
		public Long IdCuentaInversion;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaActualizarSaldosReporteAntiguedad jin = gson.fromJson(in.toString(),EntradaActualizarSaldosReporteAntiguedad.class);
		SalidaActualizarSaldosReporteAntiguedad out = logica(conn, jin);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaActualizarSaldosReporteAntiguedad logica(Connection con, EntradaActualizarSaldosReporteAntiguedad in) throws Exception {
		logger.info("INICIO::ActualizarSaldosReporteAntiguedad");
		SalidaActualizarSaldosReporteAntiguedad out = new SalidaActualizarSaldosReporteAntiguedad();
		
		Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa ,"PLATAFORMA","PLA.FechaSistema");
		
		SQL sql = new SQL();
		
		OutObtenerDiaAnterior outObtenerDiaAnteriorMovimiento = obtenerDiaAnterior(con, in.p_FechaMovimiento, idPaisSistema, c_NO, c_NO);
		LocalDate fechaDiaAnteriorMovimiento = LocalDate.parse(outObtenerDiaAnteriorMovimiento.fechas.get(0).fechaCalculada);
		String fechaInicio = outObtenerDiaAnteriorMovimiento.fechas.get(0).fechaCalculada;
		
		OutObtenerDiaAnterior outObtenerDiaAnteriorSistema = obtenerDiaAnterior(con, fechaSistema, idPaisSistema, c_NO, c_NO);
		LocalDate fechaDiaAnteriorSistema = LocalDate.parse(outObtenerDiaAnteriorSistema.fechas.get(0).fechaCalculada);
		String fechaFin = outObtenerDiaAnteriorSistema.fechas.get(0).fechaCalculada;
		
		long NumeroDeDias = ChronoUnit.DAYS.between(fechaDiaAnteriorMovimiento,fechaDiaAnteriorSistema);
		Cliente cliente = new Cliente();
		
		if(NumeroDeDias > 0L) {
			List<SaldosReporteAntiguedad> saldosPorActualizar = new ArrayList<>();
			OutObtenerDiaSiguiente outDiasSiguientes = obtenerDiaSiguiente(con, fechaInicio, idPaisSistema, c_NO, c_NO,NumeroDeDias);
			cliente = obtenerDatosCliente(con, sql , in);
			List<Date> fechasPorCargar = outDiasSiguientes.p_out_fechas.stream()
					.map(fecha -> {
						Date fechaPorCargar = Fecha.toDate(fecha.fechaCalculada);
						return fechaPorCargar;
					})
					.collect(Collectors.toList());
			if(cliente.IdCuentaInversion != null) {
				List<Movimiento> movimientosCliente =  obtenerMovimientosCargoAbonoCliente(con,sql, cliente.IdCuentaInversion, in.p_FechaMovimiento, fechaFin);
				List <SaldosReporteAntiguedad> saldoCliente = obtenerSaldoCliente (con, sql, cliente, fechaInicio);
				
				if(saldoCliente.size() == 0) {
					out.P_OUT_ERRORESCONTADOR ++;
					out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_AL_OBTENER_SALDOS_REPORTE_ANTIGUEDAD", new HashMap<>(), con, in.p_IdEmpresa));
					return out;
				}
				
				for(Date fecha:fechasPorCargar) {
					
					List<Movimiento> movimientosClienteFiltradosPorFecha = movimientosCliente.stream()
							.filter(Movimiento -> Movimiento.FechaMovimiento.equals(fecha))
							.collect(Collectors.toList());
									
					if(movimientosClienteFiltradosPorFecha.size() > 0) {
						
						BigDecimal saldoGeneral = BigDecimal.ZERO;
						BigDecimal saldoIntermediacion = BigDecimal.ZERO;
						
						if( movimientosClienteFiltradosPorFecha.stream().filter(mov-> mov.TipoSaldo.equalsIgnoreCase("G")).count() > 0 ) {
							
							List<Movimiento> movimientosFiltradosGeneral = movimientosClienteFiltradosPorFecha.stream().filter(mov-> mov.TipoSaldo.equalsIgnoreCase("G")).collect(Collectors.toList());
							
							saldoGeneral = movimientosFiltradosGeneral.stream()
									.map(mov->{
										BigDecimal monto = mov.Monto;
										if(mov.IdTipoMovimiento.equalsIgnoreCase(MovimientoCargo)) {
											monto= monto.negate(MathContext.DECIMAL128);
										}
										return monto;
										}
									)
									.reduce(BigDecimal.ZERO, BigDecimal::add);
						}
						
						if( movimientosClienteFiltradosPorFecha.stream().filter(mov-> mov.TipoSaldo.equalsIgnoreCase("I")).count() > 0 ) {
							
							List<Movimiento> movimientosFiltradosIntermediacion = movimientosClienteFiltradosPorFecha.stream().filter(mov-> mov.TipoSaldo.equalsIgnoreCase("I")).collect(Collectors.toList());
							
							saldoIntermediacion = movimientosFiltradosIntermediacion.stream()
									.map(mov->{
										BigDecimal monto = mov.Monto;
										if(mov.IdTipoMovimiento.equalsIgnoreCase(MovimientoCargo)) {
											monto= monto.negate(MathContext.DECIMAL128);
										}
										return monto;
										}
									)
									.reduce(BigDecimal.ZERO, BigDecimal::add);
						}
						
						if(saldoCliente.size() > 0) {
							Integer posicion1 = saldoCliente.indexOf(saldoCliente.get(0));
							Integer posicion2 = saldoCliente.indexOf(saldoCliente.get(1));
							
							if(saldoCliente.get(posicion1).TipoSaldo.equalsIgnoreCase("I")) {
								saldoCliente.get(posicion1).Saldo = saldoCliente.get(posicion1).Saldo.add(saldoIntermediacion, MathContext.DECIMAL128);
								saldoCliente.get(posicion2).Saldo = saldoCliente.get(posicion2).Saldo.add(saldoGeneral, MathContext.DECIMAL128);
							}else {
								saldoCliente.get(posicion2).Saldo = saldoCliente.get(posicion2).Saldo.add(saldoIntermediacion, MathContext.DECIMAL128);
								saldoCliente.get(posicion1).Saldo = saldoCliente.get(posicion1).Saldo.add(saldoGeneral, MathContext.DECIMAL128);
							}
						}else {
							SaldosReporteAntiguedad saldoGeneralCliente = new SaldosReporteAntiguedad();
							saldoGeneralCliente.IdPortafolio = cliente.IdPortafolio;
							saldoGeneralCliente.IdCliente = cliente.IdCliente;
							saldoGeneralCliente.IdMoneda = cliente.IdMoneda;
							saldoGeneralCliente.FechaSistema = fecha;
							saldoGeneralCliente.TipoSaldo = "G";	
							saldoGeneralCliente.Saldo = saldoGeneral;
							
							SaldosReporteAntiguedad saldoIntermediacionCliente = new SaldosReporteAntiguedad();
							saldoIntermediacionCliente.IdPortafolio = cliente.IdPortafolio;
							saldoIntermediacionCliente.IdCliente = cliente.IdCliente;
							saldoIntermediacionCliente.IdMoneda = cliente.IdMoneda;
							saldoIntermediacionCliente.FechaSistema = fecha;
							saldoIntermediacionCliente.TipoSaldo = "I";	
							saldoIntermediacionCliente.Saldo = saldoIntermediacion;
							
							saldoCliente.add(saldoGeneralCliente);
							saldoCliente.add(saldoIntermediacionCliente);
						}
						
						
					}
					
					
					saldosPorActualizar.addAll(saldoCliente.stream()
							.map(temp->{ SaldosReporteAntiguedad saldoReporte = new SaldosReporteAntiguedad();
							saldoReporte.IdPortafolio = temp.IdPortafolio;
							saldoReporte.IdCliente = temp.IdCliente;
							saldoReporte.IdMoneda = temp.IdMoneda;
							saldoReporte.FechaSistema = fecha;
							saldoReporte.TipoSaldo = temp.TipoSaldo;	
							saldoReporte.Saldo = temp.Saldo;
							return saldoReporte;
							})
					.collect(Collectors.toList()));
					
				}
				
				Boolean saldosActualizados= actualizarSaldosReporteAntiguedad(con, sql, saldosPorActualizar);
				if(!saldosActualizados) {
					out.P_OUT_ERRORESCONTADOR ++;
					out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_AL_ACTUALIZAR_LOS_SALDOS_REPORTE_ANTIGUEDAD", new HashMap<>(), con, in.p_IdEmpresa));
					return out;
				}		
				
			}else {
				out.P_OUT_ERRORESCONTADOR ++;
				out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_NO_SE_ENCONTRO_LA_CUENTA_DEL_CLIENTE", new HashMap<>(), con, in.p_IdEmpresa));
				return out;
			}
			
		}else {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_DE_MOVIMIENTO_MAYOR_O_IGUAL_FECHA_SISTEMA", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		logger.info("FIN::ActualizarSaldosReporteAntiguedad");
		return out;
	}
	
	private OutObtenerDiaAnterior obtenerDiaAnterior(Connection con, String fecha,Long idPaisSistema, String diaHabil,String soloCaeDiaHabil) throws Exception {
		logger.trace("INICIO::ActualizarSaldosReporteAntiguedad.obtenerDiaAnterior");
		long inicio = System.currentTimeMillis();
		
		ObtenerDiaAnterior obtenerDiaAnt = new ObtenerDiaAnterior();
		InObtenerDiaAnterior inObtenerDiaAnt = new InObtenerDiaAnterior();
		InObtenerDiaAnterior.FechasIn fechasIn = new InObtenerDiaAnterior.FechasIn();
		fechasIn.fechaInicial = fecha;
		fechasIn.numeroDias = 1L;
		inObtenerDiaAnt.fechas.add(fechasIn);
		inObtenerDiaAnt.idPais = idPaisSistema;
		inObtenerDiaAnt.diaHabil = diaHabil;
		inObtenerDiaAnt.soloCaeDiaHabil = soloCaeDiaHabil;
		
		OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnt.logica(con, inObtenerDiaAnt);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ActualizarSaldosReporteAntiguedad.obtenerDiaAnterior, tiempo: " + (fin - inicio));
		
		return outObtenerDiaAnterior;
	}
	
	private OutObtenerDiaSiguiente obtenerDiaSiguiente(Connection con, String fecha,Long idPaisSistema, String diaHabil,String soloCaeDiaHabil, Long dias) throws Exception {
		logger.trace("INICIO::ActualizarSaldosReporteAntiguedad.obtenerDiaSiguiente");
		long inicio = System.currentTimeMillis();
		
		ObtenerDiaSiguiente obtenerDiaSig = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente inObtenerDiaSig = new InObtenerDiaSiguiente();
		inObtenerDiaSig.p_idPais = idPaisSistema;
		inObtenerDiaSig.p_diaHabil = diaHabil;
		inObtenerDiaSig.p_soloCaeDiaHabil = soloCaeDiaHabil;
		
		for(long i = 1;i <= dias; i++) {
			InObtenerDiaSiguiente.FechasIn fechasInSig = new InObtenerDiaSiguiente.FechasIn();
			fechasInSig.fechaInicial = fecha;
			fechasInSig.numeroDias = i;
			inObtenerDiaSig.p_fechas.add(fechasInSig);
		}
		
		OutObtenerDiaSiguiente outDiaSiguiente = obtenerDiaSig.logica(con, inObtenerDiaSig);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ActualizarSaldosReporteAntiguedad.obtenerDiaSiguiente, tiempo: " + (fin - inicio));
		
		return outDiaSiguiente;
	}
	
	private Cliente obtenerDatosCliente(Connection con,SQL sql,EntradaActualizarSaldosReporteAntiguedad in) throws SQLException {
		logger.debug("INICIO::ActualizarSaldosReporteAntiguedad.obtenerDatosCliente");
		long inicio = System.currentTimeMillis();
		
		List<Cliente> datosClientes = new ArrayList<>();
		Cliente datosCliente = new Cliente();
		HashMap<String, Object> params = new HashMap<>(3);
		params.put("Rut", in.p_Rut);
		params.put("CodigoPortafolio", in.p_CodigoPortafolio.toUpperCase());
		params.put("IdMoneda", in.p_Moneda.toUpperCase());
		
		datosClientes = sql.query(con, SQL_SELECT_DATOS_CLIENTE, params, Cliente.class);
		
		if(datosClientes.size() > 0) {
			datosCliente = datosClientes.get(0);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarSaldosReporteAntiguedad.obtenerDatosCliente, tiempo: " + (fin - inicio));
		
		return datosCliente;
	}
	
	private List<Movimiento> obtenerMovimientosCargoAbonoCliente(Connection con,SQL sql, Long idCuentaInversion, String fechaInicio, String fechaFin) throws SQLException{
		logger.debug("INICIO::ActualizarSaldosReporteAntiguedad.obtenerMovimientosCargoAbonoCliente");
		long inicio = System.currentTimeMillis();
		
		List<Movimiento> movimientosCliente = new ArrayList<>();
		HashMap<String, Object> params = new HashMap<>(3);
		params.put("FechaInicio", fechaInicio);
		params.put("FechaFin", fechaFin);
		params.put("IdCuentaInversion", idCuentaInversion);
		
		movimientosCliente = sql.query(con, SQL_SELECT_MOVIMIENTOS_CARGO_ABONO, params, Movimiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarSaldosReporteAntiguedad.obtenerMovimientosCargoAbonoCliente, tiempo: " + (fin - inicio));
		
		return movimientosCliente;
	}
	
	private List <SaldosReporteAntiguedad> obtenerSaldoCliente (Connection con,SQL sql, Cliente cliente, String fechaInicio) throws SQLException{
		logger.debug("INICIO::ActualizarSaldosReporteAntiguedad.obtenerSaldoCliente");
		long inicio = System.currentTimeMillis();
		
		List <SaldosReporteAntiguedad> saldosClientes = new ArrayList<>();
		HashMap<String, Object> params = new HashMap<>(5);
		params.put("IdCliente", cliente.IdCliente);
		params.put("IdPortafolio", cliente.IdPortafolio);
		params.put("IdMoneda", cliente.IdMoneda);
		params.put("FechaInicio", fechaInicio);
		
		saldosClientes = sql.query(con, SQL_SELECT_SALDO_REPORTE_ANTIGUEDAD, params, SaldosReporteAntiguedad.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarSaldosReporteAntiguedad.obtenerSaldoCliente, tiempo: " + (fin - inicio));
		
		return saldosClientes;
	}
	
	private boolean actualizarSaldosReporteAntiguedad(Connection con, SQL sql,List<SaldosReporteAntiguedad> listaSaldosPorActualizar) {
		logger.debug("INICIO::ActualizarSaldosReporteAntiguedad.insertarSaldosDiaActualReporteAntiguedad");
		long inicio = System.currentTimeMillis();
		
		boolean resultado = false;
		
		try {
			int resultadoQuery []  =  sql.batch(con,SQL_UPSERT_SALDOS_REPORTE_ANTIGUEDAD,listaSaldosPorActualizar,SaldosReporteAntiguedad.class);
			System.out.println("Registros insertados: " + resultadoQuery.length);
			if(resultadoQuery.length > 0) {
				resultado = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarSaldosReporteAntiguedad.insertarSaldosDiaActualReporteAntiguedad, tiempo: " + (fin - inicio));
		
		return resultado;
	}
}
