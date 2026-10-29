package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
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

public class GuardarSaldosReporteAntiguedad implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(GuardarSaldosReporteAntiguedad.class);
	
	private final String SQL_SELECT_SALDO_REPORTE_ANTIGUEDAD				= loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarSaldosReporteAntiguedad_SelectSaldoReporteAntiguedad.sql");
	private final String SQL_SELECT_MOVIMIENTOS								= loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarSaldosReporteAntiguedad_SelectMovimientos.sql");
	private final String SQL_UPSERT_SALDOS_REPORTE_ANTIGUEDAD				= loadResourceToString("cl/bcs/cuentaInversion/sql/GuardarSaldosReporteAntiguedad_UpsertSaldoReporteAntiguedad.sql");
		
	private final String MovimientoCargo = "MOVCAR";
	private final String c_SI = "S";
	private final String c_NO = "N";
	
	public static class EntradaGuardarSaldosReporteAntiguedad extends BcsPojo{
		public Long p_IdEmpresa;
	    public Long p_IdUsuario;
		public String p_FechaSistema;
	}
	
	public static class SalidaGuardarSaldosReporteAntiguedad extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public SalidaGuardarSaldosReporteAntiguedad() {
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
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		
		public Cliente(){
			IdCliente = 0L;
			IdPortafolio = 0L;
			IdMoneda = "";
		}
		
		public String getIdentificador() {
			return this.IdCliente.toString() + "-" + this.IdPortafolio.toString() + "-" + this.IdMoneda.toString();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaGuardarSaldosReporteAntiguedad jin = gson.fromJson(in.toString(),EntradaGuardarSaldosReporteAntiguedad.class);
		SalidaGuardarSaldosReporteAntiguedad out = logica(conn, jin);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGuardarSaldosReporteAntiguedad logica(Connection con, EntradaGuardarSaldosReporteAntiguedad in) throws Exception{
		logger.info("INICIO::GuardarSaldosReporteAntiguedad");
		SalidaGuardarSaldosReporteAntiguedad out = new SalidaGuardarSaldosReporteAntiguedad();
		
		SQL sql = new SQL();
		
		Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		Long diasPorReprocesar = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa,"CUENTAINVERSION","DIASREPROCESOSALDOREPORTEANTIGUEDAD");
		
		OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior(con, in.p_FechaSistema, idPaisSistema, c_NO, c_NO, diasPorReprocesar);
		
		String fechaInicio = outObtenerDiaAnterior.fechas.get(outObtenerDiaAnterior.fechas.size()- 1).fechaCalculada;
			
		List<Date> listaFechas = new ArrayList<>();
		
		listaFechas.addAll(outObtenerDiaAnterior.fechas.stream()
				.map(fecha -> {
					Date fechaInhabil = Fecha.toDate(fecha.fechaCalculada);
					return fechaInhabil;
				})
				.collect(Collectors.toList()));
		
		OutObtenerDiaSiguiente outDiasSiguientes = new OutObtenerDiaSiguiente();
		
		OutObtenerDiaSiguiente outDiaSiguiente = obtenerDiaSiguiente(con, in.p_FechaSistema, idPaisSistema, c_SI, c_SI);
		
		if(!outDiaSiguiente.p_out_fechas.get(0).numeroDias.equals(outDiaSiguiente.p_out_fechas.get(0).numeroDiasInicial)) {
			Long dias = outDiaSiguiente.p_out_fechas.get(0).numeroDias;
			outDiasSiguientes = obtenerDiaSiguiente(con, in.p_FechaSistema, idPaisSistema, c_NO, c_NO,dias);
			
			listaFechas.addAll(outDiasSiguientes.p_out_fechas.stream()
					.map(fecha -> {
						Date fechaInhabil = Fecha.toDate(fecha.fechaCalculada);
						return fechaInhabil;
					})
					.collect(Collectors.toList()));
			
			listaFechas.add(Fecha.toDate(in.p_FechaSistema));
			
		}else {
			listaFechas.add(Fecha.toDate(in.p_FechaSistema));
		}
		
		Collections.sort(listaFechas);
		
		OutObtenerDiaAnterior outObtenerDiaAnteriorReporte = obtenerDiaAnterior(con, fechaInicio, idPaisSistema, c_NO, c_NO,1L);
		
		String fechaInicioReporte = outObtenerDiaAnteriorReporte.fechas.get(0).fechaCalculada;

		List<SaldosReporteAntiguedad> saldosReporteAntiguedad= obtenerSaldoAnteriorReporteAntiguedad(con, sql, fechaInicioReporte);
		
		if(saldosReporteAntiguedad.size() == 0) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_AL_OBTENER_SALDOS_REPORTE_ANTIGUEDAD", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		List<SaldosReporteAntiguedad> saldosPorInsertar = new ArrayList<>();
		
		List<Movimiento> movimientosCargoAbono = obtenerMovimientosCargoAbono(con, sql, fechaInicio);
								
		for(Date fecha: listaFechas) {
			
			List<Movimiento> movimientosFiltradosPorFecha = movimientosCargoAbono.stream()
					.filter(Movimiento -> Movimiento.FechaMovimiento.equals(fecha))
					.collect(Collectors.toList());
			
			if(movimientosFiltradosPorFecha.size() > 0) {
				
				List<Cliente> clientes = movimientosCargoAbono.stream()
						.map(mov->{ Cliente cli = new Cliente();
						cli.IdPortafolio = mov.IdPortafolio;
						cli.IdCliente = mov.IdCliente;
						cli.IdMoneda = mov.IdMoneda;
						return cli;
						})
						.collect(Collectors.toList());
						
				HashMap<String,Cliente> hashClientes = new HashMap<>();
						
				for(Cliente cliente: clientes) {
					hashClientes.put(cliente.getIdentificador(), cliente);
				}
				
				if(hashClientes.size() > 0) {
					
					for(Entry<String, Cliente> hashcliente : hashClientes.entrySet()) {
						Cliente clientePorActualizarSaldo = (Cliente)hashcliente.getValue();
						
						List<Movimiento> movimientosFiltradosPorCliente = movimientosFiltradosPorFecha.stream()
								.filter(mov-> mov.IdCliente.equals(clientePorActualizarSaldo.IdCliente) && mov.IdPortafolio.equals(clientePorActualizarSaldo.IdPortafolio) && mov.IdMoneda.equalsIgnoreCase(clientePorActualizarSaldo.IdMoneda))
								.collect(Collectors.toList());
						
						BigDecimal saldoGeneral = BigDecimal.ZERO;
						BigDecimal saldoIntermediacion = BigDecimal.ZERO;
						
						if( movimientosFiltradosPorCliente.stream().filter(mov-> mov.TipoSaldo.equalsIgnoreCase("G")).count() > 0 ) {
							
							List<Movimiento> movimientosFiltradosGeneral = movimientosFiltradosPorCliente.stream().filter(mov-> mov.TipoSaldo.equalsIgnoreCase("G")).collect(Collectors.toList());
							
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
						
						if( movimientosFiltradosPorCliente.stream().filter(mov-> mov.TipoSaldo.equalsIgnoreCase("I")).count() > 0 ) {
							
							List<Movimiento> movimientosFiltradosIntermediacion = movimientosFiltradosPorCliente.stream().filter(mov-> mov.TipoSaldo.equalsIgnoreCase("I")).collect(Collectors.toList());
							
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
						
						List<SaldosReporteAntiguedad> saldosAnteriorCliente = saldosReporteAntiguedad.stream()
								.filter(mov-> mov.IdCliente.equals(clientePorActualizarSaldo.IdCliente) && mov.IdPortafolio.equals(clientePorActualizarSaldo.IdPortafolio) && mov.IdMoneda.equalsIgnoreCase(clientePorActualizarSaldo.IdMoneda))
								.collect(Collectors.toList());
						
						if(saldosAnteriorCliente.size() > 0) {
							//Se Actualiza el saldo de la cuenta
							Integer posicion1 = saldosReporteAntiguedad.indexOf(saldosAnteriorCliente.get(0));
							Integer posicion2 = saldosReporteAntiguedad.indexOf(saldosAnteriorCliente.get(1));
							
							if(saldosReporteAntiguedad.get(posicion1).TipoSaldo.equalsIgnoreCase("I")) {
								saldosReporteAntiguedad.get(posicion1).Saldo = saldosReporteAntiguedad.get(posicion1).Saldo.add(saldoIntermediacion, MathContext.DECIMAL128);
								saldosReporteAntiguedad.get(posicion2).Saldo = saldosReporteAntiguedad.get(posicion2).Saldo.add(saldoGeneral, MathContext.DECIMAL128);
							}else {
								saldosReporteAntiguedad.get(posicion2).Saldo = saldosReporteAntiguedad.get(posicion2).Saldo.add(saldoIntermediacion, MathContext.DECIMAL128);
								saldosReporteAntiguedad.get(posicion1).Saldo = saldosReporteAntiguedad.get(posicion1).Saldo.add(saldoGeneral, MathContext.DECIMAL128);
							}
													
						}else {
							//No hay registro previo de la cuenta
							SaldosReporteAntiguedad saldoGeneralCliente = new SaldosReporteAntiguedad();
							saldoGeneralCliente.IdPortafolio = clientePorActualizarSaldo.IdPortafolio;
							saldoGeneralCliente.IdCliente = clientePorActualizarSaldo.IdCliente;
							saldoGeneralCliente.IdMoneda = clientePorActualizarSaldo.IdMoneda;
							saldoGeneralCliente.FechaSistema = Fecha.toDate(in.p_FechaSistema);
							saldoGeneralCliente.TipoSaldo = "G";	
							saldoGeneralCliente.Saldo = saldoGeneral;
							
							SaldosReporteAntiguedad saldoIntermediacionCliente = new SaldosReporteAntiguedad();
							saldoIntermediacionCliente.IdPortafolio = clientePorActualizarSaldo.IdPortafolio;
							saldoIntermediacionCliente.IdCliente = clientePorActualizarSaldo.IdCliente;
							saldoIntermediacionCliente.IdMoneda = clientePorActualizarSaldo.IdMoneda;
							saldoIntermediacionCliente.FechaSistema = Fecha.toDate(in.p_FechaSistema);
							saldoIntermediacionCliente.TipoSaldo = "I";	
							saldoIntermediacionCliente.Saldo = saldoIntermediacion;
							
							saldosReporteAntiguedad.add(saldoGeneralCliente);
							saldosReporteAntiguedad.add(saldoIntermediacionCliente);
							
						}
						
					}						
					
				}
				
				
			}
			
			saldosPorInsertar.addAll(saldosReporteAntiguedad.stream()
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
		
		Boolean saldosInsertados = insertarSaldosReporteAntiguedad (con, sql, saldosPorInsertar);
		if(!saldosInsertados) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_AL_INSERTAR_LOS_SALDOS_REPORTE_ANTIGUEDAD", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
				
		logger.info("FIN::ConsultaAntiguedadDeudasCuentaInversion");
		return out;
		
	}
	
	private List<SaldosReporteAntiguedad> obtenerSaldoAnteriorReporteAntiguedad (Connection con, SQL sql,String fecha) throws SQLException{
		logger.debug("INICIO::GuardarSaldosReporteAntiguedad.obtenerSaldoAnteriorReporteAntiguedad");
		long inicio = System.currentTimeMillis();
		
		List<SaldosReporteAntiguedad> saldosDiaAnteriorReporteAntiguedad = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(1);
		param.put("fechaConsulta", fecha);
		
		saldosDiaAnteriorReporteAntiguedad = sql.query(con, SQL_SELECT_SALDO_REPORTE_ANTIGUEDAD, param, SaldosReporteAntiguedad.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GuardarSaldosReporteAntiguedad.obtenerSaldoAnteriorReporteAntiguedad", fin - inicio);
		
		return saldosDiaAnteriorReporteAntiguedad;
	}
	
	private List<Movimiento> obtenerMovimientosCargoAbono (Connection con, SQL sql, String fechaInicio) throws SQLException{
		logger.debug("INICIO::GuardarSaldosReporteAntiguedad.obtenerMovimientosCargoAbono");
		long inicio = System.currentTimeMillis();
		
		List<Movimiento> movimientosDelDia = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(1);
		param.put("FechaInicio", fechaInicio);
		
		movimientosDelDia = sql.query(con, SQL_SELECT_MOVIMIENTOS, param, Movimiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GuardarSaldosReporteAntiguedad.obtenerMovimientosCargoAbono", fin - inicio);
		
		return movimientosDelDia;
		
	}
	
	private boolean insertarSaldosReporteAntiguedad(Connection con, SQL sql,List<SaldosReporteAntiguedad> listaSaldosPorInsertar) {
		logger.debug("INICIO::GuardarSaldosReporteAntiguedad.insertarSaldosDiaActualReporteAntiguedad");
		long inicio = System.currentTimeMillis();
		
		boolean resultado = false;
		
		try {
			int resultadoQuery []  =  sql.batch(con,SQL_UPSERT_SALDOS_REPORTE_ANTIGUEDAD,listaSaldosPorInsertar,SaldosReporteAntiguedad.class);
			if(resultadoQuery.length > 0) {
				resultado = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GuardarSaldosReporteAntiguedad.insertarSaldosDiaActualReporteAntiguedad", fin - inicio);
		
		return resultado;
		
	}
	
	private OutObtenerDiaAnterior obtenerDiaAnterior(Connection con, String fecha,Long idPaisSistema, String diaHabil,String soloCaeDiaHabil, Long dias) throws Exception {
		logger.trace("INICIO::GuardarSaldosReporteAntiguedad.obtenerDiaAnterior");
		long inicio = System.currentTimeMillis();
		
		ObtenerDiaAnterior obtenerDiaAnt = new ObtenerDiaAnterior();
		InObtenerDiaAnterior inObtenerDiaAnt = new InObtenerDiaAnterior();
		inObtenerDiaAnt.idPais = idPaisSistema;
		inObtenerDiaAnt.diaHabil = diaHabil;
		inObtenerDiaAnt.soloCaeDiaHabil = soloCaeDiaHabil;
		
		for(long i = 1;i <= dias; i++) {
			InObtenerDiaAnterior.FechasIn fechasIn = new InObtenerDiaAnterior.FechasIn();
			fechasIn.fechaInicial = fecha;
			fechasIn.numeroDias = i;
			inObtenerDiaAnt.fechas.add(fechasIn);
		}
		
		OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnt.logica(con, inObtenerDiaAnt);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GuardarSaldosReporteAntiguedad.obtenerDiaAnterior", fin - inicio);
		
		return outObtenerDiaAnterior;
	}
	
	private OutObtenerDiaSiguiente obtenerDiaSiguiente(Connection con, String fecha,Long idPaisSistema, String diaHabil,String soloCaeDiaHabil) throws Exception {
		logger.trace("INICIO::GuardarSaldosReporteAntiguedad.obtenerDiaSiguiente");
		long inicio = System.currentTimeMillis();
		
		ObtenerDiaSiguiente obtenerDiaSig = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente inObtenerDiaSig = new InObtenerDiaSiguiente();
		InObtenerDiaSiguiente.FechasIn fechasInSig = new InObtenerDiaSiguiente.FechasIn();
		fechasInSig.fechaInicial = fecha;
		fechasInSig.numeroDias = 1L;
		inObtenerDiaSig.p_fechas.add(fechasInSig);
		inObtenerDiaSig.p_idPais = idPaisSistema;
		inObtenerDiaSig.p_diaHabil = diaHabil;
		inObtenerDiaSig.p_soloCaeDiaHabil = soloCaeDiaHabil;
		
		OutObtenerDiaSiguiente outDiaSiguiente = obtenerDiaSig.logica(con, inObtenerDiaSig);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GuardarSaldosReporteAntiguedad.obtenerDiaSiguiente", fin - inicio);
		
		return outDiaSiguiente;
	}
	
	private OutObtenerDiaSiguiente obtenerDiaSiguiente(Connection con, String fecha,Long idPaisSistema, String diaHabil,String soloCaeDiaHabil, Long dias) throws Exception {
		logger.trace("INICIO::GuardarSaldosReporteAntiguedad.obtenerDiaSiguiente");
		long inicio = System.currentTimeMillis();
		
		ObtenerDiaSiguiente obtenerDiaSig = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente inObtenerDiaSig = new InObtenerDiaSiguiente();
		inObtenerDiaSig.p_idPais = idPaisSistema;
		inObtenerDiaSig.p_diaHabil = diaHabil;
		inObtenerDiaSig.p_soloCaeDiaHabil = soloCaeDiaHabil;
		
		for(long i = 1;i < dias; i++) {
			InObtenerDiaSiguiente.FechasIn fechasInSig = new InObtenerDiaSiguiente.FechasIn();
			fechasInSig.fechaInicial = fecha;
			fechasInSig.numeroDias = i;
			inObtenerDiaSig.p_fechas.add(fechasInSig);
		}
		
		OutObtenerDiaSiguiente outDiaSiguiente = obtenerDiaSig.logica(con, inObtenerDiaSig);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GuardarSaldosReporteAntiguedad.obtenerDiaSiguiente", fin - inicio);
		
		return outDiaSiguiente;
	}
}
