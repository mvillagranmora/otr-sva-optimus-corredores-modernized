package cl.bcs.normativa.circulares;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.custodia.ConsultarCustodiaMovimientos;
import cl.bcs.custodia.ConsultarCustodiaMovimientos.EntradaConsultarCustodiaMovimientos;
import cl.bcs.custodia.ConsultarCustodiaMovimientos.SalidaConsultarCustodiaMovimientos;
import cl.bcs.custodia.ConsultarCustodiaMovimientos.VariableProceso;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarNCG380Custodia implements JavaCall{

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarNCG380Custodia jin = gson.fromJson(in.toString(), EntradaGenerarNCG380Custodia.class);
		SalidaGenerarNCG380Custodia out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private final static Logger logger = LogManager.getLogger(GenerarNCG380Custodia.class);
	
	private final String SQL_SELECT_PARAMETROS_CIRCULAR				= loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarNCG380Custodia_SelectParametrosCircular.sql");
	private final String SQL_SELECT_HOMOLOGACION_TIPO_MOVIMIENTO	= loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarNCG380Custodia_SelectHomologacionTipoMovimiento.sql");
	private final String SQL_SELECT_SALDO_INICIAL_CARTERA			= loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarNCG380Custodia_SelectSaldoInicialCartera.sql");
	private final String SQL_SELECT_SALDO_INICIAL_GARANTIA			= loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarNCG380Custodia_SelectSaldoInicialGarantia.sql");
	private final String SQL_SELECT_MOVIMIENTOS_CARGO_ABONO			= loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarNCG380Custodia_SelectMovimientosCargoAbono.sql");
	
	private final String NCG380CUS = "NCG380Cus";
	private final String c_NO = "N";
	
	public static class EntradaGenerarNCG380Custodia extends BcsPojo{
		public Long	IdEmpresa;
		public Long	IdSolicitudCircular;
	}
	
	public static class SalidaGenerarNCG380Custodia extends BcsPojo{
		public ArrayList<MensajeSalida>	P_OUT_MENSAJES;
		public Long P_OUT_INFORMACIONCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public Long P_OUT_ERRORESCONTADOR;
		public ArrayList<SalidaNCG380Custodia> P_OUT_TABLA;
		public SalidaGenerarNCG380Custodia(){
			this.P_OUT_ERRORESCONTADOR		= 0L;
			this.P_OUT_ADVERTENCIASCONTADOR	= 0L;
			this.P_OUT_INFORMACIONCONTADOR	= 0L;
			this.P_OUT_MENSAJES  			= new ArrayList<MensajeSalida>();
			this.P_OUT_TABLA  				= new ArrayList<SalidaNCG380Custodia>();
		}
		
	}
	
	public static class SalidaNCG380Custodia extends BcsPojo{
		public String 		NumeroCliente;
		public String		NumeroCuenta;
		public Date			Fecha;
		public String	 	Hora;
		public Long 		NumeroComprobante;
		public String		Emisor;
		public String		TipoMovimiento;
		public String		Movimiento;
		public String		ISIN;
		public String		Nemotecnico;
		public BigDecimal	Cantidad;
		public BigDecimal	Monto;
		public String		Moneda;
		public BigDecimal	Saldo;
		public String		SubCustodio;
		public String		Estado;
		
	}
	
	public static class ParametrosCircular extends BcsPojo{
		public Long		IdSolicitudCircular;
		public String	Parametro;
		public String	Valor;
	}
	
	public static class TiposMovimientosHomologacion extends BcsPojo{
		public String	ValorOrigen;
		public String	IdEstado;
		public String	ValorDestino;
		public String	Hash;
	}
	
	public static class FiltrosMovimientos extends BcsPojo{
		public String	FechaInicio;
		public String	FechaTermino;
		public Long		IdCliente;
		public Long		IdInstrumento;
		public String	Nemotecnico;
		public String	IdClasificacionTipoInstrumento;
		public FiltrosMovimientos() {
			this.IdCliente						= 0L;
			this.IdInstrumento					= 0L;
			this.Nemotecnico					= "";
			this.IdClasificacionTipoInstrumento	= "";
			this.FechaInicio					= "0001-01-01";
			this.FechaTermino					= "0001-01-01";
		}
	}
	
	public static class SaldoInicial extends BcsPojo{
		public String		Nemotecnico;
		public String		IdEmisor;
		public String		ISIN;
		public String		DocumentoIdentidad;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String		CodigoPortafolio;
		public String		IdMonedaInstrumento;
		public String		IdBoveda;
		public BigDecimal	Saldo;
		public String		Moneda;
		
		public String getNemotecnico() {
			return this.Nemotecnico;
		}
		
		public Long getIdCliente() {
			return this.IdCliente;
		}
		
		public Long getIdPortafolio() {
			return this.IdPortafolio;
		}
		
		public String getIdBoveda() {
			return this.IdBoveda;
		}
	}
	
	public static class MovimientosCargoAbono extends BcsPojo{
		public Long			IdCargoAbonoMovimiento;
		public String		DocumentoIdentidad;
		public Long			IdCliente;
		public String		CodigoPortafolio;
		public Long			IdPortafolio;
		public String		IdMoneda;
		public Long			IdCuentaInversion;
		public BigDecimal	Monto;
		public Timestamp	FechaMovimiento;
		public String		IdTipoOperacion;
		public String		IdModulo;
		public Long			FolioComprobante;
		public String		IdTipoMovimiento;
		public String		EsIngresoManual;
		
		public Timestamp getFechaMovimiento() {
			return this.FechaMovimiento;
		}

		public Long getIdCargoAbonoMovimiento() {
			return this.IdCargoAbonoMovimiento;
		}
		
		public Long getIdCuentaInversion() {
			return this.IdCuentaInversion;
		}
	}
	
	public static class MovimientoCustodia extends BcsPojo{
		public String		Nemotecnico;
		public String		IdEmisor;
		public String		ISIN;
		public String		DocumentoIdentidad;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String		CodigoPortafolio;
		public String		IdMonedaInstrumento;
		public String		IdBoveda;
		public BigDecimal	Cantidad;
		public String		Accion;
		public String		IdTipoOperacion;
		public Date			FechaOperacion;
		public Timestamp	FechaRegistro;
		public Long			FolioComprobante;
		public BigDecimal	Monto;
		public Timestamp	FechaCertificacion;
		public Long getIdCliente() {
			return this.IdCliente;
		}
		public Long getIdPortafolio() {
			return this.IdPortafolio;
		}
		
		public String getNemotecnico() {
			return this.Nemotecnico;
		}
		
		public String getIdBoveda() {
			return this.IdBoveda;
		}
		
		public Date	getFechaOperacion() {
			return this.FechaOperacion;
		}
		
		public Timestamp getFechaCertificacion() {
			return this.FechaCertificacion;
		}
		
		public Timestamp getFechaRegistro() {
			return this.FechaRegistro;
		}
	}
	
	public static class SaldoInicialCuentaInversion extends BcsPojo{
		public String		DocumentoIdentidad;
		public Long			IdCliente;
		public String		CodigoPortafolio;
		public Long			IdPortafolio;
		public String		IdMoneda;
		public Long			IdCuentaInversion;
		public BigDecimal	Saldo;
		
		public Long getIdCuentaInversion() {
			return this.IdCuentaInversion;
		}
	}
		
	public SalidaGenerarNCG380Custodia logica (Connection con, EntradaGenerarNCG380Custodia in) throws Exception{
		logger.info("INICIO::GenerarNCG380Custodia");
		
		SalidaGenerarNCG380Custodia out = new SalidaGenerarNCG380Custodia();
		
		SQL sql = new SQL();
		
		Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, in.IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		
		List<ParametrosCircular> parametrosCircular = obtenerParametrosCircular(con, sql, in.IdSolicitudCircular);
		
		if(parametrosCircular.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_AL_OBTENER_PARAMETROS_CIRCULAR", new HashMap<>(), con, in.IdEmpresa));
			return out;
		}
		
		List<TiposMovimientosHomologacion> tiposMovimientosHomologacion = obtenerTiposMovimientosHomologacion(con, sql, NCG380CUS);
		
		if(tiposMovimientosHomologacion.isEmpty()) {
			out.P_OUT_ADVERTENCIASCONTADOR ++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA,"NO_SE_OBTUVIERON_TIPOS_MOVIMIENTOS_HOMOLOGACION", new HashMap<>(), con, in.IdEmpresa));
		}
		
		FiltrosMovimientos filtrosMovimientos = definirFiltrosMovimientos(parametrosCircular);
		
		List<SaldoInicial> saldosIniciales = obtenerSaldosInicialesCartera(con, sql, filtrosMovimientos);
		
		List<SaldoInicial> saldosInicialesGarantia = obtenerSaldosInicialesGarantia(con, sql, filtrosMovimientos);
		
		List<MovimientoCustodia> movimientosPeriodo = obtenerMovimientosPeriodo (con, filtrosMovimientos, in.IdEmpresa);
		
		List<MovimientosCargoAbono> movimientosCargoAbono = obtenerMovimientoCargoAbono(con, sql, filtrosMovimientos);
		
		List<SaldoInicialCuentaInversion> saldoInicialCuentaInversion = obtenerSaldoInicialCuentasInversion(con, filtrosMovimientos, in, idPaisSistema);
				
		HashMap<String, SalidaNCG380Custodia> hashSaldo = new HashMap<>();
		
		HashMap<String, SalidaNCG380Custodia> hashSaldoGarantia = new HashMap<>();
		
		HashMap<Long, SalidaNCG380Custodia> hashSaldoCuentaInversion = new HashMap<>();
		
		List<SalidaNCG380Custodia> salidaPrevia = new ArrayList<>();
		String keyAnterior = "";
		String keyAnteriorGarantia = "";
		Long keyAnteriorCuentaInversion = 0L;
		BigDecimal saldoActual = BigDecimal.ZERO;
		BigDecimal saldoActualGarantia	= BigDecimal.ZERO;
		BigDecimal saldoActualCuentaInversion = BigDecimal.ZERO;
		
		if(movimientosPeriodo.size() == 0) {
			
			if(saldosIniciales.size() > 0) {
				
				saldosIniciales.sort(Comparator.comparing(SaldoInicial::getIdCliente).thenComparing(SaldoInicial::getIdPortafolio).thenComparing(SaldoInicial::getNemotecnico).thenComparing(SaldoInicial::getIdBoveda));
				
				for(SaldoInicial saldo : saldosIniciales) {
					SalidaNCG380Custodia saldoInicialFinal = new SalidaNCG380Custodia();
					saldoInicialFinal.Cantidad			= BigDecimal.ZERO;
					saldoInicialFinal.Emisor			= saldo.IdEmisor;
					saldoInicialFinal.Estado			= "LG";
					saldoInicialFinal.Fecha				= Fecha.toDate(filtrosMovimientos.FechaInicio);
					saldoInicialFinal.Hora				= "";
					saldoInicialFinal.ISIN				= saldo.ISIN;
					saldoInicialFinal.Moneda			= "Saldo Inicial/Final";
					saldoInicialFinal.Monto				= BigDecimal.ZERO;
					saldoInicialFinal.Movimiento		= "";
					saldoInicialFinal.Nemotecnico		= saldo.Nemotecnico;
					saldoInicialFinal.NumeroCliente		= saldo.DocumentoIdentidad;
					saldoInicialFinal.NumeroCuenta		= saldo.CodigoPortafolio;
					saldoInicialFinal.Saldo				= saldo.Saldo;
					saldoInicialFinal.SubCustodio		= saldo.IdBoveda;
					saldoInicialFinal.NumeroComprobante	= 0L;
					saldoInicialFinal.TipoMovimiento	= "";
					
					salidaPrevia.add(saldoInicialFinal);
				}
			}
			
			if(saldosInicialesGarantia.size() > 0) {
				
				saldosInicialesGarantia.sort(Comparator.comparing(SaldoInicial::getIdCliente).thenComparing(SaldoInicial::getIdPortafolio).thenComparing(SaldoInicial::getNemotecnico).thenComparing(SaldoInicial::getIdBoveda));
				
				for(SaldoInicial saldo : saldosInicialesGarantia) {
					SalidaNCG380Custodia saldoInicialGarantia = new SalidaNCG380Custodia();			
					saldoInicialGarantia.Cantidad			= BigDecimal.ZERO;
					saldoInicialGarantia.Emisor				= saldo.IdEmisor;
					saldoInicialGarantia.Estado				= "GT";
					saldoInicialGarantia.Fecha				= Fecha.toDate(filtrosMovimientos.FechaInicio);
					saldoInicialGarantia.Hora				= "";
					saldoInicialGarantia.ISIN				= saldo.ISIN;
					saldoInicialGarantia.Moneda				= "Saldo Inicial/Final";
					saldoInicialGarantia.Monto				= BigDecimal.ZERO;
					saldoInicialGarantia.Movimiento			= "";
					saldoInicialGarantia.Nemotecnico		= saldo.Nemotecnico;
					saldoInicialGarantia.NumeroCliente		= saldo.DocumentoIdentidad;
					saldoInicialGarantia.NumeroCuenta		= saldo.CodigoPortafolio;
					saldoInicialGarantia.Saldo				= saldo.Saldo;
					saldoInicialGarantia.SubCustodio		= saldo.IdBoveda;
					saldoInicialGarantia.NumeroComprobante	= 0L;
					saldoInicialGarantia.TipoMovimiento		= "";
				
					salidaPrevia.add(saldoInicialGarantia);
				}
			}
			
		}else if(movimientosPeriodo.size() > 0) {
			
			//Saldos Iniciales
			if(saldosIniciales.size() > 0) {
				
				saldosIniciales.sort(Comparator.comparing(SaldoInicial::getIdCliente).thenComparing(SaldoInicial::getIdPortafolio).thenComparing(SaldoInicial::getNemotecnico).thenComparing(SaldoInicial::getIdBoveda));
				
				for(SaldoInicial saldo : saldosIniciales) {
					SalidaNCG380Custodia saldoAuxiliar = new SalidaNCG380Custodia();
					
					String key = saldo.IdCliente + "_" + saldo.IdPortafolio + "_" + saldo.Nemotecnico + "_" + saldo.IdBoveda;
					
					saldoAuxiliar.Cantidad			= BigDecimal.ZERO;
					saldoAuxiliar.Emisor			= saldo.IdEmisor;
					saldoAuxiliar.Estado			= "LG";
					saldoAuxiliar.Fecha				= Fecha.toDate(filtrosMovimientos.FechaInicio);
					saldoAuxiliar.Hora				= "";
					saldoAuxiliar.ISIN				= saldo.ISIN;
					saldoAuxiliar.Moneda			= saldo.Moneda;
					saldoAuxiliar.Monto				= BigDecimal.ZERO;
					saldoAuxiliar.Movimiento		= "";
					saldoAuxiliar.Nemotecnico		= saldo.Nemotecnico;
					saldoAuxiliar.NumeroCliente		= saldo.DocumentoIdentidad;
					saldoAuxiliar.NumeroCuenta		= saldo.CodigoPortafolio;
					saldoAuxiliar.Saldo				= saldo.Saldo;
					saldoAuxiliar.SubCustodio		= saldo.IdBoveda;
					saldoAuxiliar.NumeroComprobante	= 0L;
					saldoAuxiliar.TipoMovimiento	= "";
					
					Long movimientosAsociados = movimientosPeriodo.stream()
						.filter(mov -> mov.IdCliente.compareTo(saldo.IdCliente) == 0 && mov.IdPortafolio.compareTo(saldo.IdPortafolio) == 0 && mov.Nemotecnico.equalsIgnoreCase(saldo.Nemotecnico)
						&&  mov.IdBoveda.equalsIgnoreCase(saldo.IdBoveda)).count();
					
					if(movimientosAsociados.compareTo(0L) > 0) {
						hashSaldo.put(key, saldoAuxiliar);
					}else {
						saldoAuxiliar.Moneda		= "Saldo Inicial/Final";
						salidaPrevia.add(saldoAuxiliar);
					}
								
				}
			}
			
			List<MovimientoCustodia> movimientosGarantiaPeriodo = new ArrayList<>();
						
			movimientosPeriodo.sort(Comparator.comparing(MovimientoCustodia::getIdCliente).thenComparing(MovimientoCustodia::getIdPortafolio).thenComparing(MovimientoCustodia::getNemotecnico).thenComparing(MovimientoCustodia::getIdBoveda).thenComparing(MovimientoCustodia::getFechaOperacion).thenComparing(MovimientoCustodia::getFechaRegistro));
			
			//Movimientos LG
			for(int i=0; i < movimientosPeriodo.size(); i++) {
				
				MovimientoCustodia mov = movimientosPeriodo.get(i);
				
				SalidaNCG380Custodia movimiento = new SalidaNCG380Custodia();
				String keyActual 				= mov.IdCliente + "_" + mov.IdPortafolio + "_" + mov.Nemotecnico + "_" + mov.IdBoveda;
				String tipoMovimiento			= definirTipoMovimiento(mov, tiposMovimientosHomologacion);
				String keyProxima				= "";
				String hora						= Fecha.toStringDatetime(mov.FechaRegistro).split(" ")[1];
				if(i+1 < movimientosPeriodo.size()) {
					MovimientoCustodia movSig = movimientosPeriodo.get(i+1);
					keyProxima = movSig.IdCliente + "_" + movSig.IdPortafolio + "_" + movSig.Nemotecnico + "_" + movSig.IdBoveda;
				}
				
				movimiento.Cantidad				= mov.Cantidad.abs(MathContext.DECIMAL128);
				movimiento.Emisor				= mov.IdEmisor;
				movimiento.Estado				= "LG";
				movimiento.Fecha				= mov.FechaOperacion;
				movimiento.Hora					= hora;
				movimiento.ISIN					= mov.ISIN;
				movimiento.Moneda				= definirMonedaMovimiento(mov.IdMonedaInstrumento);
				if(!keyProxima.equalsIgnoreCase(keyActual)) {
					movimiento.Moneda			= movimiento.Moneda + " Saldo Final";
				}
				movimiento.Monto 				= mov.Monto;
				movimiento.Movimiento			= "INGTIT".equalsIgnoreCase(mov.IdTipoOperacion) || "RETTIT".equalsIgnoreCase(mov.IdTipoOperacion) ? "0": "1";
				movimiento.Nemotecnico			= mov.Nemotecnico;
				movimiento.NumeroCliente		= mov.DocumentoIdentidad;
				movimiento.NumeroComprobante	= mov.FolioComprobante;
				movimiento.NumeroCuenta			= mov.CodigoPortafolio;
				movimiento.SubCustodio			= mov.IdBoveda;
				movimiento.TipoMovimiento		= tipoMovimiento;
				
				if(!keyAnterior.equalsIgnoreCase(keyActual)) {
					SalidaNCG380Custodia saldo = hashSaldo.get(keyActual);
					if(saldo != null) {
						movimiento.Saldo					= mov.Cantidad.add(saldo.Saldo, MathContext.DECIMAL128);
						salidaPrevia.add(saldo);
					}else {
						movimiento.Saldo					= mov.Cantidad;
						SalidaNCG380Custodia saldoInicial 	= crearSaldoInicialPeriodo(mov, filtrosMovimientos, false);
						salidaPrevia.add(saldoInicial);
					}
					
				}else {
					movimiento.Saldo				= mov.Cantidad.add(saldoActual, MathContext.DECIMAL128);
				}
				saldoActual 					= movimiento.Saldo;
				keyAnterior						= keyActual;
				
				salidaPrevia.add(movimiento);
				
				//Si el movimiento proviene de una garantia se agrega a la lista
				if("INGGTA".equalsIgnoreCase(mov.IdTipoOperacion) || "RETGTA".equalsIgnoreCase(mov.IdTipoOperacion)) {
					movimientosGarantiaPeriodo.add(mov);
				}
			}
			
			//Saldos Iniciales Garantias
			if(saldosInicialesGarantia.size() > 0) {
				
				saldosInicialesGarantia.sort(Comparator.comparing(SaldoInicial::getIdCliente).thenComparing(SaldoInicial::getIdPortafolio).thenComparing(SaldoInicial::getNemotecnico).thenComparing(SaldoInicial::getIdBoveda));
				
				for(SaldoInicial saldo : saldosInicialesGarantia) {
					SalidaNCG380Custodia saldoAuxiliar = new SalidaNCG380Custodia();
					
					String key = saldo.IdCliente + "_" + saldo.IdPortafolio + "_" + saldo.Nemotecnico + "_" + saldo.IdBoveda;
					
					saldoAuxiliar.Cantidad			= BigDecimal.ZERO;
					saldoAuxiliar.Emisor			= saldo.IdEmisor;
					saldoAuxiliar.Estado			= "GT";
					saldoAuxiliar.Fecha				= Fecha.toDate(filtrosMovimientos.FechaInicio);
					saldoAuxiliar.Hora				= "";
					saldoAuxiliar.ISIN				= saldo.ISIN;
					saldoAuxiliar.Moneda			= "Saldo Inicial/Final";
					saldoAuxiliar.Monto				= BigDecimal.ZERO;
					saldoAuxiliar.Movimiento		= "";
					saldoAuxiliar.Nemotecnico		= saldo.Nemotecnico;
					saldoAuxiliar.NumeroCliente		= saldo.DocumentoIdentidad;
					saldoAuxiliar.NumeroCuenta		= saldo.CodigoPortafolio;
					saldoAuxiliar.Saldo				= saldo.Saldo;
					saldoAuxiliar.SubCustodio		= saldo.IdBoveda;
					saldoAuxiliar.NumeroComprobante	= 0L;
					saldoAuxiliar.TipoMovimiento	= "";
					
					Long movimientosAsociadosGarantias = movimientosPeriodo.stream()
						.filter(mov -> mov.IdCliente.compareTo(saldo.IdCliente) == 0 && mov.IdPortafolio.compareTo(saldo.IdPortafolio) == 0 && mov.Nemotecnico.equalsIgnoreCase(saldo.Nemotecnico)
						&&  mov.IdBoveda.equalsIgnoreCase(saldo.IdBoveda) && ("INGGTA".equalsIgnoreCase(mov.IdTipoOperacion) || "RETGTA".equalsIgnoreCase(mov.IdTipoOperacion))).count();
					
					if(movimientosAsociadosGarantias.compareTo(0L) > 0) {
						hashSaldoGarantia.put(key, saldoAuxiliar);
					}else {
						saldoAuxiliar.Moneda		= "Saldo Inicial/Final";
						salidaPrevia.add(saldoAuxiliar);
					}
				}
			}
			
			//Movimientos GT
			if(movimientosGarantiaPeriodo.size() > 0) {
				
				movimientosGarantiaPeriodo.sort(Comparator.comparing(MovimientoCustodia::getIdCliente).thenComparing(MovimientoCustodia::getIdPortafolio).thenComparing(MovimientoCustodia::getNemotecnico).thenComparing(MovimientoCustodia::getIdBoveda).thenComparing(MovimientoCustodia::getFechaOperacion).thenComparing(MovimientoCustodia::getFechaCertificacion).thenComparing(MovimientoCustodia::getFechaRegistro));
				
				for(int i=0; i < movimientosGarantiaPeriodo.size(); i++) {
					
					MovimientoCustodia mov = movimientosGarantiaPeriodo.get(i);
					
					SalidaNCG380Custodia movimiento = new SalidaNCG380Custodia();
					String keyActual 				= mov.IdCliente + "_" + mov.IdPortafolio + "_" + mov.Nemotecnico + "_" + mov.IdBoveda;
					String tipoMovimiento			= definirTipoMovimiento(mov, tiposMovimientosHomologacion);
					String keyProxima				= "";
					String hora						= Fecha.toStringDatetime(mov.FechaRegistro).split(" ")[1];
					if(i+1 < movimientosGarantiaPeriodo.size()) {
						MovimientoCustodia movSig = movimientosGarantiaPeriodo.get(i+1);
						keyProxima = movSig.IdCliente + "_" + movSig.IdPortafolio + "_" + movSig.Nemotecnico + "_" + movSig.IdBoveda;
					}
					
					movimiento.Cantidad				= mov.Cantidad.abs(MathContext.DECIMAL128);
					movimiento.Emisor				= mov.IdEmisor;
					movimiento.Estado				= "GT";
					movimiento.Fecha				= mov.FechaOperacion;
					movimiento.Hora					= hora;
					movimiento.ISIN					= mov.ISIN;
					movimiento.Moneda				= definirMonedaMovimiento(mov.IdMonedaInstrumento);
					if(!keyProxima.equalsIgnoreCase(keyActual)) {
						movimiento.Moneda			= movimiento.Moneda + " Saldo Final";
					}
					movimiento.Monto 				= mov.Monto;
					movimiento.Movimiento			= "INGTIT".equalsIgnoreCase(mov.IdTipoOperacion) || "RETTIT".equalsIgnoreCase(mov.IdTipoOperacion) ? "0": "1";
					movimiento.Nemotecnico			= mov.Nemotecnico;
					movimiento.NumeroCliente		= mov.DocumentoIdentidad;
					movimiento.NumeroComprobante	= mov.FolioComprobante;
					movimiento.NumeroCuenta			= mov.CodigoPortafolio;
					movimiento.SubCustodio			= mov.IdBoveda;
					movimiento.TipoMovimiento		= tipoMovimiento;
					
					if(!keyAnteriorGarantia.equalsIgnoreCase(keyActual)) {
						SalidaNCG380Custodia saldo = hashSaldoGarantia.get(keyActual);
						if(saldo != null) {
							movimiento.Saldo					= mov.Cantidad.negate(MathContext.DECIMAL128).add(saldo.Saldo, MathContext.DECIMAL128);
							salidaPrevia.add(saldo);
						}else {
							movimiento.Saldo					= mov.Cantidad.negate(MathContext.DECIMAL128);
							SalidaNCG380Custodia saldoInicial 	= crearSaldoInicialPeriodo(mov, filtrosMovimientos ,true);
							salidaPrevia.add(saldoInicial);
						}
						
					}else {
						movimiento.Saldo				= mov.Cantidad.negate(MathContext.DECIMAL128).add(saldoActualGarantia, MathContext.DECIMAL128);
					}
					saldoActualGarantia 			= movimiento.Saldo;
					keyAnteriorGarantia				= keyActual;
					
					salidaPrevia.add(movimiento);
				}
			}
		}
		
		if(movimientosCargoAbono.size() == 0) {
			if(saldoInicialCuentaInversion.size() > 0) {
				
				saldoInicialCuentaInversion.sort(Comparator.comparing(SaldoInicialCuentaInversion::getIdCuentaInversion));
				
				for(SaldoInicialCuentaInversion saldo : saldoInicialCuentaInversion) {
					SalidaNCG380Custodia movimientoCuentaInversion = new SalidaNCG380Custodia();
					String tipoMovimiento						= "";
					movimientoCuentaInversion.Cantidad			= BigDecimal.ZERO;
					movimientoCuentaInversion.Emisor			= saldo.IdMoneda;
					movimientoCuentaInversion.Estado			= "LG";
					movimientoCuentaInversion.Fecha				= Fecha.toDate(filtrosMovimientos.FechaInicio);
					movimientoCuentaInversion.Hora				= "";
					movimientoCuentaInversion.ISIN				= "";
					movimientoCuentaInversion.Moneda			= "Saldo Inicial/Final";
					movimientoCuentaInversion.Monto				= saldo.Saldo;
					movimientoCuentaInversion.Movimiento		= "0"; 
					movimientoCuentaInversion.Nemotecnico		= saldo.IdMoneda;
					movimientoCuentaInversion.NumeroCliente		= saldo.DocumentoIdentidad;
					movimientoCuentaInversion.NumeroCuenta		= saldo.CodigoPortafolio;
					movimientoCuentaInversion.Saldo				= saldo.Saldo;
					movimientoCuentaInversion.SubCustodio		= "COR";
					movimientoCuentaInversion.NumeroComprobante	= 0L;
					movimientoCuentaInversion.TipoMovimiento	= tipoMovimiento;
					
					salidaPrevia.add(movimientoCuentaInversion);
				}
			}
			
		}else if(movimientosCargoAbono.size() > 0) {
			
			movimientosCargoAbono.sort(Comparator.comparing(MovimientosCargoAbono::getIdCuentaInversion).thenComparing(MovimientosCargoAbono::getFechaMovimiento).thenComparing(MovimientosCargoAbono::getIdCargoAbonoMovimiento));
			
			if(saldoInicialCuentaInversion.size() > 0) {
				
				saldoInicialCuentaInversion.sort(Comparator.comparing(SaldoInicialCuentaInversion::getIdCuentaInversion));
				
				for(SaldoInicialCuentaInversion saldo : saldoInicialCuentaInversion) {
					SalidaNCG380Custodia movimientoCuentaInversion = new SalidaNCG380Custodia();
					String tipoMovimiento						= "";
					movimientoCuentaInversion.Cantidad			= BigDecimal.ZERO;
					movimientoCuentaInversion.Emisor			= saldo.IdMoneda;
					movimientoCuentaInversion.Estado			= "LG";
					movimientoCuentaInversion.Fecha				= Fecha.toDate(filtrosMovimientos.FechaInicio);
					movimientoCuentaInversion.Hora				= "";
					movimientoCuentaInversion.ISIN				= "";
					movimientoCuentaInversion.Moneda			= "Saldo Inicial";
					movimientoCuentaInversion.Monto				= BigDecimal.ZERO;
					movimientoCuentaInversion.Movimiento		= "0";
					movimientoCuentaInversion.Nemotecnico		= saldo.IdMoneda;
					movimientoCuentaInversion.NumeroCliente		= saldo.DocumentoIdentidad;
					movimientoCuentaInversion.NumeroCuenta		= saldo.CodigoPortafolio;
					movimientoCuentaInversion.Saldo				= saldo.Saldo;
					movimientoCuentaInversion.SubCustodio		= "COR";
					movimientoCuentaInversion.NumeroComprobante	= 0L;
					movimientoCuentaInversion.TipoMovimiento	= tipoMovimiento;
					
					Long movimientosAsociadosCuentaInversion = movimientosCargoAbono.stream().filter(cta -> saldo.IdCuentaInversion.compareTo(cta.IdCuentaInversion) == 0).count(); 
					if(movimientosAsociadosCuentaInversion.compareTo(0L) > 0) {
						hashSaldoCuentaInversion.put(saldo.IdCuentaInversion, movimientoCuentaInversion);
					}else {
						movimientoCuentaInversion.Moneda		= "Saldo Inicial/Final";
					salidaPrevia.add(movimientoCuentaInversion);
					}
					
				}
			}
			
			for(int i=0; i < movimientosCargoAbono.size(); i++) {
				
				MovimientosCargoAbono cargoAbono = movimientosCargoAbono.get(i);
				SalidaNCG380Custodia movimientoCuentaInversion = new SalidaNCG380Custodia();
				String tipoMovimiento						= definirTipoMovimiento(cargoAbono, tiposMovimientosHomologacion);
				Long keyProxima								= 0L;
				Long keyActual 								= cargoAbono.IdCuentaInversion;
				if(i+1 < movimientosCargoAbono.size()) {
					MovimientosCargoAbono movSig = movimientosCargoAbono.get(i+1);
					keyProxima = movSig.IdCuentaInversion;
				}
				movimientoCuentaInversion.Cantidad			= BigDecimal.ZERO;
				movimientoCuentaInversion.Emisor			= cargoAbono.IdMoneda;
				movimientoCuentaInversion.Estado			= "INGGTA".equalsIgnoreCase(cargoAbono.IdTipoOperacion) || "RETGTA".equalsIgnoreCase(cargoAbono.IdTipoOperacion) ? "GT" : "LG";
				movimientoCuentaInversion.Fecha				= Fecha.toDate(cargoAbono.FechaMovimiento.toString().split(" ")[0]);
				movimientoCuentaInversion.Hora				= Fecha.toStringDatetime(cargoAbono.FechaMovimiento).split(" ")[1];
				movimientoCuentaInversion.ISIN				= "";
				movimientoCuentaInversion.Moneda			= definirMonedaMovimiento(cargoAbono.IdMoneda);
				if(keyProxima.compareTo(keyActual) != 0) {
					movimientoCuentaInversion.Moneda			= movimientoCuentaInversion.Moneda + " Saldo Final";
				}
				movimientoCuentaInversion.Monto				= cargoAbono.Monto;
				movimientoCuentaInversion.Movimiento		= "S".equalsIgnoreCase(cargoAbono.EsIngresoManual) ? "0" : "1";
				movimientoCuentaInversion.Nemotecnico		= cargoAbono.IdMoneda;
				movimientoCuentaInversion.NumeroCliente		= cargoAbono.DocumentoIdentidad;
				movimientoCuentaInversion.NumeroCuenta		= cargoAbono.CodigoPortafolio;
				movimientoCuentaInversion.SubCustodio		= "COR";
				movimientoCuentaInversion.NumeroComprobante	= cargoAbono.FolioComprobante != null ? cargoAbono.FolioComprobante :0L;
				movimientoCuentaInversion.TipoMovimiento	= tipoMovimiento;
				
				if(keyAnteriorCuentaInversion.compareTo(keyActual) != 0) {
					SalidaNCG380Custodia saldo = hashSaldoCuentaInversion.get(keyActual);
					if(saldo != null) {
						movimientoCuentaInversion.Saldo		= movimientoCuentaInversion.Monto.add(saldo.Saldo, MathContext.DECIMAL128);
						salidaPrevia.add(saldo);
					}else {
						movimientoCuentaInversion.Saldo		= movimientoCuentaInversion.Monto;
						SalidaNCG380Custodia saldoInicial	= crearSaldoInicialPeriodo(cargoAbono, filtrosMovimientos ,false);
						salidaPrevia.add(saldoInicial);
					}
					
				}else {
					movimientoCuentaInversion.Saldo			= movimientoCuentaInversion.Monto.add(saldoActualCuentaInversion, MathContext.DECIMAL128);
				}
				saldoActualCuentaInversion 		= movimientoCuentaInversion.Saldo;
				keyAnteriorCuentaInversion		= keyActual;
				
				salidaPrevia.add(movimientoCuentaInversion);
			}
			
		}
		
		out.P_OUT_TABLA.addAll(salidaPrevia);
		
		logger.info("FIN::GenerarNCG380Custodia");
		return out;
	}
	
	private List<ParametrosCircular> obtenerParametrosCircular (Connection con, SQL sql, Long idSolicitudCircular) throws SQLException{
		logger.debug("INICIO::GenerarNCG380Custodia.obtenerParametrosCircular");
		long inicio = System.currentTimeMillis();
		
		List<ParametrosCircular> parametrosCircular = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(1);
		param.put("IdSolicitudCircular", idSolicitudCircular);
		
		parametrosCircular = sql.query(con, SQL_SELECT_PARAMETROS_CIRCULAR, param, ParametrosCircular.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarNCG380Custodia.obtenerParametrosCircular tiempo: " + (fin - inicio));
		
		return parametrosCircular;
	}
	
	private List<TiposMovimientosHomologacion> obtenerTiposMovimientosHomologacion (Connection con, SQL sql, String esquema) throws SQLException{
		logger.debug("INICIO::GenerarNCG380Custodia.obtenerTiposMovimientosHomologacion");
		long inicio = System.currentTimeMillis();
		
		List<TiposMovimientosHomologacion> tiposMovimientosHomologacion = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(1);
		param.put("Esquema", esquema);
		
		tiposMovimientosHomologacion = sql.query(con, SQL_SELECT_HOMOLOGACION_TIPO_MOVIMIENTO, param, TiposMovimientosHomologacion.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarNCG380Custodia.obtenerTiposMovimientosHomologacion tiempo: " + (fin - inicio));
		
		return tiposMovimientosHomologacion;
		
	}
	
	private FiltrosMovimientos definirFiltrosMovimientos (List<ParametrosCircular> parametrosCircular){
		logger.trace("INICIO::GenerarNCG380Custodia.obtenerFiltrosMovimientos");
		long inicio = System.currentTimeMillis();
		
		FiltrosMovimientos filtrosMovimientos = new FiltrosMovimientos();
				
		for(ParametrosCircular parametro : parametrosCircular) {
			if("FechaInicio".equalsIgnoreCase(parametro.Parametro)) {
				filtrosMovimientos.FechaInicio = parametro.Valor != null ? Fecha.toFormat(Fecha.toDate(parametro.Valor),"yyyy-MM-dd") : "0001-01-01";
			}
			if("FechaTermino".equalsIgnoreCase(parametro.Parametro)) {
				filtrosMovimientos.FechaTermino = parametro.Valor != null ? Fecha.toFormat(Fecha.toDate(parametro.Valor),"yyyy-MM-dd") : "0001-01-01";
			}
			if("IdCliente".equalsIgnoreCase(parametro.Parametro)) {
				filtrosMovimientos.IdCliente = parametro.Valor != null ? Long.parseLong(parametro.Valor) : 0L;
			}
			if("Mercado".equalsIgnoreCase(parametro.Parametro)) {
				filtrosMovimientos.IdClasificacionTipoInstrumento = parametro.Valor != null ? parametro.Valor : "";
			}
			if("IdInstrumento".equalsIgnoreCase(parametro.Parametro)) {
				filtrosMovimientos.IdInstrumento = parametro.Valor != null ? Long.parseLong(parametro.Valor) : 0L;
			}
			if("Nemotecnico".equalsIgnoreCase(parametro.Parametro)) {
				filtrosMovimientos.Nemotecnico = parametro.Valor != null ? parametro.Valor : "";
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarNCG380Custodia.obtenerFiltrosMovimientos tiempo: " + (fin - inicio));
		
		return filtrosMovimientos;
	}
	
	private List<MovimientoCustodia> obtenerMovimientosPeriodo (Connection con,FiltrosMovimientos filtrosMovimientos, Long idEmpresa) throws Exception{
		logger.trace("INICIO::GenerarNCG380Custodia.obtenerMovimientosPeriodo");
		long inicio = System.currentTimeMillis();
		
		List<MovimientoCustodia> movimientosPeriodo = new ArrayList<>();
		
		ConsultarCustodiaMovimientos consultarMovimientos = new ConsultarCustodiaMovimientos();
		EntradaConsultarCustodiaMovimientos entradaMovimientos = new EntradaConsultarCustodiaMovimientos();
		entradaMovimientos.p_IdEmpresa = idEmpresa;
		entradaMovimientos.p_IdUsuario = 0L;
		entradaMovimientos.p_IdModulo = "CUS";
		entradaMovimientos.p_FechaInicio = filtrosMovimientos.FechaInicio; 
		entradaMovimientos.p_FechaTermino = filtrosMovimientos.FechaTermino;
		entradaMovimientos.p_IdProductoAgrupador = 0L;
		entradaMovimientos.p_TipoOperacion = "";
		entradaMovimientos.p_IdConcepto = "";
		entradaMovimientos.p_IdBoveda = "";
		entradaMovimientos.p_UIConsulta = "NCG380CUS";
		VariableProceso filtros = new VariableProceso();
		filtros.FolioCartera = 0L;
		filtros.IdClasificacionTipoInstrumento = filtrosMovimientos.IdClasificacionTipoInstrumento;
		filtros.IdCliente = filtrosMovimientos.IdCliente;
		filtros.IdInstrumento = filtrosMovimientos.IdInstrumento;
		filtros.Nemotecnico = filtrosMovimientos.Nemotecnico;
		filtros.IdPortafolio = 0L;
		filtros.IdTipoInstrumento = "";
		List<VariableProceso> listaFiltros = new ArrayList<>();
		listaFiltros.add(filtros);
		entradaMovimientos.p_VariablesProceso = listaFiltros;
		SalidaConsultarCustodiaMovimientos salidaMovimientos = consultarMovimientos.logica(con, entradaMovimientos);
		if(salidaMovimientos.P_OUT_ERRORESCONTADOR == 0L && salidaMovimientos.P_OUT_TABLA.size() > 0) {
			movimientosPeriodo = salidaMovimientos.P_OUT_TABLA.stream().map(salidaMovimiento->{
				MovimientoCustodia mov = new MovimientoCustodia();
				mov.Accion 				= salidaMovimiento.Accion;
				mov.Cantidad			= salidaMovimiento.Cantidad;
				if("IF".equalsIgnoreCase(salidaMovimiento.IdClasificacionTipoInstrumento)) {
					mov.Cantidad 		= "RETIRO".equalsIgnoreCase(mov.Accion) ? salidaMovimiento.ValorRescate.negate(MathContext.DECIMAL128):salidaMovimiento.ValorRescate;
				}
				mov.CodigoPortafolio	= salidaMovimiento.CodigoPortafolio;
				mov.DocumentoIdentidad	= salidaMovimiento.DocumentoIdentidad;
				mov.FechaOperacion		= salidaMovimiento.FechaOperacion;
				mov.FechaRegistro		= salidaMovimiento.FechaRegistro;
				mov.FolioComprobante	= salidaMovimiento.FolioComprobante;
				mov.IdBoveda			= salidaMovimiento.IdBoveda;
				mov.IdCliente			= salidaMovimiento.IdCliente;
				mov.IdEmisor			= salidaMovimiento.IdEmisor;
				mov.IdMonedaInstrumento	= salidaMovimiento.IdMonedaInstr;
				mov.IdPortafolio		= salidaMovimiento.IdPortafolio;
				mov.IdTipoOperacion		= salidaMovimiento.IdTipoOperacion;
				mov.ISIN				= salidaMovimiento.ISIN;
				mov.Monto				= salidaMovimiento.Monto;
				mov.Nemotecnico			= salidaMovimiento.Nemotecnico;
				mov.FechaCertificacion	= salidaMovimiento.FechaCertificacion == null ? Timestamp.valueOf("0001-01-01 00:00:00") : salidaMovimiento.FechaCertificacion;
				return mov;
			}).collect(Collectors.toList());
		}
		
		long fin = System.currentTimeMillis();
		logger.info("GenerarNCG380Custodia.obtenerMovimientosPeriodo - Movimientos Obtenidos: " + salidaMovimientos.P_OUT_TABLA.size());
		logger.trace("FIN::GenerarNCG380Custodia.obtenerMovimientosPeriodo tiempo: " + (fin - inicio));
		
		return movimientosPeriodo;
	}
	
	private List<MovimientosCargoAbono> obtenerMovimientoCargoAbono (Connection con, SQL sql, FiltrosMovimientos filtrosMovimientos) throws SQLException{
		logger.debug("INICIO::GenerarNCG380Custodia.obtenerMovimientoCargoAbono");
		long inicio = System.currentTimeMillis();
		
		List<MovimientosCargoAbono> cargoAbono = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(3);
		param.put("FechaInicio", filtrosMovimientos.FechaInicio);
		param.put("FechaTermino", filtrosMovimientos.FechaTermino);
		param.put("IdCliente", filtrosMovimientos.IdCliente);
		
		cargoAbono = sql.query(con, SQL_SELECT_MOVIMIENTOS_CARGO_ABONO, param, MovimientosCargoAbono.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarNCG380Custodia.obtenerMovimientoCargoAbono tiempo: " + (fin - inicio));
		
		return cargoAbono;
	}
	
	private List<SaldoInicial> obtenerSaldosInicialesCartera (Connection con, SQL sql, FiltrosMovimientos filtrosMovimientos) throws SQLException{
		logger.debug("INICIO::GenerarNCG380Custodia.obtenerSaldosInicialesCartera");
		long inicio = System.currentTimeMillis();
		
		List<SaldoInicial> saldosIniciales = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(4);
		param.put("FechaInicio", filtrosMovimientos.FechaInicio);
		param.put("IdCliente", filtrosMovimientos.IdCliente);
		param.put("IdInstrumento", filtrosMovimientos.IdInstrumento);
		param.put("Nemotecnico", filtrosMovimientos.Nemotecnico);
		param.put("IdClasificacionTipoInstrumento", filtrosMovimientos.IdClasificacionTipoInstrumento);
		
		saldosIniciales = sql.query(con,SQL_SELECT_SALDO_INICIAL_CARTERA, param, SaldoInicial.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarNCG380Custodia.obtenerSaldosInicialesCartera tiempo: " + (fin - inicio));
		
		return saldosIniciales;
	}
	
	private List<SaldoInicial> obtenerSaldosInicialesGarantia (Connection con, SQL sql, FiltrosMovimientos filtrosMovimientos) throws SQLException{
		logger.debug("INICIO::GenerarNCG380Custodia.obtenerSaldosInicialesGarantias");
		long inicio = System.currentTimeMillis();
		
		List<SaldoInicial> saldosIniciales = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(4);
		param.put("FechaInicio", filtrosMovimientos.FechaInicio);
		param.put("IdCliente", filtrosMovimientos.IdCliente);
		param.put("IdInstrumento", filtrosMovimientos.IdInstrumento);
		param.put("Nemotecnico", filtrosMovimientos.Nemotecnico);
		param.put("IdClasificacionTipoInstrumento", filtrosMovimientos.IdClasificacionTipoInstrumento);
		
		saldosIniciales = sql.query(con,SQL_SELECT_SALDO_INICIAL_GARANTIA, param, SaldoInicial.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarNCG380Custodia.obtenerSaldosInicialesGarantias tiempo: " + (fin - inicio));
		
		return saldosIniciales;
	}
	
	private String definirMonedaMovimiento (String monedaInstrumento) {
		logger.trace("INICIO::GenerarNCG380Custodia.definirMonedaMovimiento");
		long inicio = System.currentTimeMillis();
		
		String moneda = "";
		
		if("CLP".equalsIgnoreCase(monedaInstrumento)) {
			moneda = "$$";
		}else if("AUD".equalsIgnoreCase(monedaInstrumento)) {
			moneda = "DA";
		}else {
			moneda = monedaInstrumento;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarNCG380Custodia.definirMonedaMovimiento tiempo: " + (fin - inicio));
		
		return moneda;
	}
	
	private SalidaNCG380Custodia crearSaldoInicialPeriodo(MovimientoCustodia movimiento, FiltrosMovimientos filtrosMovimientos, Boolean garantia) {
		logger.trace("INICIO::GenerarNCG380Custodia.definirMonedaMovimiento");
		long inicio = System.currentTimeMillis();
		
		SalidaNCG380Custodia saldoInicial = new SalidaNCG380Custodia();
		saldoInicial.Cantidad			= BigDecimal.ZERO;
		saldoInicial.Emisor				= movimiento.IdEmisor;
		saldoInicial.Estado				= garantia ? "GT" :"LG";
		saldoInicial.Fecha				= Fecha.toDate(filtrosMovimientos.FechaInicio);
		saldoInicial.Hora				= "";
		saldoInicial.ISIN				= movimiento.ISIN;
		saldoInicial.Moneda				= "Saldo Inicial";
		saldoInicial.Monto				= BigDecimal.ZERO;
		saldoInicial.Movimiento			= "";
		saldoInicial.Nemotecnico		= movimiento.Nemotecnico;
		saldoInicial.NumeroCliente		= movimiento.DocumentoIdentidad;
		saldoInicial.NumeroCuenta		= movimiento.CodigoPortafolio;
		saldoInicial.Saldo				= BigDecimal.ZERO;
		saldoInicial.SubCustodio		= movimiento.IdBoveda;
		saldoInicial.NumeroComprobante	= 0L;
		saldoInicial.TipoMovimiento		= "";
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarNCG380Custodia.definirMonedaMovimiento tiempo: " + (fin - inicio));
		
		return saldoInicial;
	}
	
	private SalidaNCG380Custodia crearSaldoInicialPeriodo(MovimientosCargoAbono movimiento, FiltrosMovimientos filtrosMovimientos, Boolean garantia) {
		logger.trace("INICIO::GenerarNCG380Custodia.definirMonedaMovimiento");
		long inicio = System.currentTimeMillis();
		
		SalidaNCG380Custodia saldoInicial = new SalidaNCG380Custodia();
		saldoInicial.Cantidad			= BigDecimal.ZERO;
		saldoInicial.Emisor				= movimiento.IdMoneda;
		saldoInicial.Estado				= garantia ? "GT" :"LG";
		saldoInicial.Fecha				= Fecha.toDate(filtrosMovimientos.FechaInicio);
		saldoInicial.Hora				= "";
		saldoInicial.ISIN				= "";
		saldoInicial.Moneda				= "Saldo Inicial";
		saldoInicial.Monto				= BigDecimal.ZERO;
		saldoInicial.Movimiento			= "";
		saldoInicial.Nemotecnico		= movimiento.IdMoneda;
		saldoInicial.NumeroCliente		= movimiento.DocumentoIdentidad;
		saldoInicial.NumeroCuenta		= movimiento.CodigoPortafolio;
		saldoInicial.Saldo				= BigDecimal.ZERO;
		saldoInicial.SubCustodio		= "";
		saldoInicial.NumeroComprobante	= 0L;
		saldoInicial.TipoMovimiento		= "";
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarNCG380Custodia.definirMonedaMovimiento tiempo: " + (fin - inicio));
		
		return saldoInicial;
	}
	
	private String definirTipoMovimiento (MovimientoCustodia movimiento, List<TiposMovimientosHomologacion> tiposMovimientosHomologacion) {
		logger.trace("INICIO::GenerarNCG380Custodia.definirTipoMovimiento");
		long inicio = System.currentTimeMillis();
		
		String tipoMovimiento = "";
		
		if("INGGTA".equalsIgnoreCase(movimiento.IdTipoOperacion) || "RETGTA".equalsIgnoreCase(movimiento.IdTipoOperacion)) {
			tipoMovimiento = "RETG";
			if("INGRESO".equalsIgnoreCase(movimiento.Accion)) {
				tipoMovimiento = "INGG";
			}
		}else {
			tipoMovimiento = "RET";
			if("INGRESO".equalsIgnoreCase(movimiento.Accion)) {
				tipoMovimiento = "ING";
			}
		}
		
		String tipoMovimientoHomologacion = "";
		String key = movimiento.IdTipoOperacion + "_" + tipoMovimiento;
		
		if(tiposMovimientosHomologacion.size() > 0 ) {
			TiposMovimientosHomologacion tipoMovHom = tiposMovimientosHomologacion.stream().filter(tipo -> key.equalsIgnoreCase(tipo.Hash)).findFirst().orElse(null);
			if (tipoMovHom != null) {
				tipoMovimientoHomologacion = tipoMovHom.ValorDestino;
			}else {
				if("ING".equalsIgnoreCase(tipoMovimiento)) {
					tipoMovimientoHomologacion = "ICO";
				}else {
					tipoMovimientoHomologacion = "ECO";
				}
			}
		}else {
			if("ING".equalsIgnoreCase(tipoMovimiento)) {
				tipoMovimientoHomologacion = "ICO";
			}else {
				tipoMovimientoHomologacion = "ECO";
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarNCG380Custodia.definirTipoMovimiento tiempo: " + (fin - inicio));
		
		return tipoMovimientoHomologacion;
	}
	
	private String definirTipoMovimiento (MovimientosCargoAbono movimiento, List<TiposMovimientosHomologacion> tiposMovimientosHomologacion) {
		logger.trace("INICIO::GenerarNCG380Custodia.definirTipoMovimiento");
		long inicio = System.currentTimeMillis();
		
		String tipoMovimiento = "";
		
		if("INGGTA".equalsIgnoreCase(movimiento.IdTipoOperacion) || "RETGTA".equalsIgnoreCase(movimiento.IdTipoOperacion)) {
			if("MOVABN".equalsIgnoreCase(movimiento.IdTipoMovimiento)) {
				tipoMovimiento = "INGG";
			}
			tipoMovimiento = "RETG";
		}else {
			if("MOVABN".equalsIgnoreCase(movimiento.IdTipoMovimiento)) {
				tipoMovimiento = "ING";
			}
			tipoMovimiento = "RET";
		}
		
		String tipoMovimientoHomologacion = "";
		String key = movimiento.IdTipoOperacion + "_" + tipoMovimiento;
		
		if(tiposMovimientosHomologacion.size() > 0 ) {
			TiposMovimientosHomologacion tipoMovHom = tiposMovimientosHomologacion.stream().filter(tipo -> key.equalsIgnoreCase(tipo.Hash)).findFirst().orElse(null);
			if (tipoMovHom != null) {
				tipoMovimientoHomologacion = tipoMovHom.ValorDestino;
			}else {
				if("ING".equalsIgnoreCase(tipoMovimiento)) {
					tipoMovimientoHomologacion = "ICO";
				}else {
					tipoMovimientoHomologacion = "ECO";
				}
			}
		}else {
			if("ING".equalsIgnoreCase(tipoMovimiento)) {
				tipoMovimientoHomologacion = "ICO";
			}else {
				tipoMovimientoHomologacion = "ECO";
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarNCG380Custodia.definirTipoMovimiento tiempo: " + (fin - inicio));
		
		return tipoMovimientoHomologacion;
	}
	
	private List<SaldoInicialCuentaInversion> obtenerSaldoInicialCuentasInversion(Connection con, FiltrosMovimientos filtrosMovimientos, EntradaGenerarNCG380Custodia in , Long idPaisSistema) throws Exception{
		logger.trace("INICIO::GenerarNCG380Custodia.obtenerSaldoInicialCuentasInversion");
		long inicio = System.currentTimeMillis();
		
		OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior(con, filtrosMovimientos.FechaInicio, idPaisSistema, c_NO, c_NO);
		String fechaBusqueda = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;
		
		List<SaldoInicialCuentaInversion> ListaCuentasInversion = new ArrayList<>();
		
		ConsultarSaldoCuentaInversion servicioConsultarSaldoCuenta = new ConsultarSaldoCuentaInversion();
		EntradaConsultarSaldoCuentaInversion entradaConsultarSaldoCuenta = new EntradaConsultarSaldoCuentaInversion();
		entradaConsultarSaldoCuenta.p_IdEmpresa 			= in.IdEmpresa;
		entradaConsultarSaldoCuenta.p_IdUsuario         	= 0L;
		entradaConsultarSaldoCuenta.p_IdModulo				= "CTA";
		entradaConsultarSaldoCuenta.p_FechaDesde			= fechaBusqueda;
		entradaConsultarSaldoCuenta.p_FechaHasta			= fechaBusqueda;
		entradaConsultarSaldoCuenta.p_UsaReglasNegocio		= "S";
		entradaConsultarSaldoCuenta.p_IncluyeOtrosSaldos	= "S";		
		entradaConsultarSaldoCuenta.p_SoloSaldoNegativo		= "N";
		entradaConsultarSaldoCuenta.p_SoloSaldoDisponible	= "N";
		entradaConsultarSaldoCuenta.p_IdEjecutivo			= 0L;
		entradaConsultarSaldoCuenta.p_IncluyeProductoApv    = "S";
		entradaConsultarSaldoCuenta.p_IdCliente				= filtrosMovimientos.IdCliente.compareTo(0L) != 0 ? filtrosMovimientos.IdCliente: null;
		entradaConsultarSaldoCuenta.p_IdPortafolio			= null; 
		entradaConsultarSaldoCuenta.p_IdProductoAgrupador	= null;
		
		List<InCuentaInversionConsultar> lstCuentaInversionConsultar = new ArrayList<InCuentaInversionConsultar>();
		InCuentaInversionConsultar objCuentaInversionConsultar = new InCuentaInversionConsultar();
		objCuentaInversionConsultar.IdEjecutivo  = 0L;
		objCuentaInversionConsultar.IdMoneda 	 = null;
		objCuentaInversionConsultar.IdPortafolio = null; 
		lstCuentaInversionConsultar.add(objCuentaInversionConsultar);
		entradaConsultarSaldoCuenta.p_CuentaInversionAConsultar = lstCuentaInversionConsultar;
		
		SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuenta = servicioConsultarSaldoCuenta.logica(con, entradaConsultarSaldoCuenta);
		
		if(salidaConsultarSaldoCuenta.P_OUT_ERRORESCONTADOR.compareTo(0L)==0 && !salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty()){
			
			List<SaldoInicialCuentaInversion> cuentasInversion = salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA.stream()
					.filter(Saldo-> ("CLP".equalsIgnoreCase(Saldo.IdMoneda) || "USD".equalsIgnoreCase(Saldo.IdMoneda) ||"EUR".equalsIgnoreCase(Saldo.IdMoneda))
							&& Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128).compareTo(BigDecimal.ZERO) != 0)
					.map(Saldo ->{
						SaldoInicialCuentaInversion saldoAuxiliar = new SaldoInicialCuentaInversion();
						saldoAuxiliar.CodigoPortafolio		= Saldo.CodigoPortafolio;
						saldoAuxiliar.DocumentoIdentidad	= Saldo.DocumentoIdentidad;
						saldoAuxiliar.IdCliente				= Saldo.IdCliente;
						saldoAuxiliar.IdCuentaInversion		= Saldo.IdCuentaInversion;
						saldoAuxiliar.IdMoneda				= Saldo.IdMoneda;
						saldoAuxiliar.IdPortafolio			= Saldo.IdPortafolio;
						saldoAuxiliar.Saldo					= Saldo.SaldoDisponible.add(Saldo.SaldoRetenido, MathContext.DECIMAL128);
						return saldoAuxiliar;
					})
					.collect(Collectors.toList());
			
			ListaCuentasInversion.addAll(cuentasInversion);
			
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarNCG380Custodia.obtenerSaldoInicialCuentasInversion tiempo: " + (fin - inicio));
		
		return ListaCuentasInversion;
	}
	
	private OutObtenerDiaAnterior obtenerDiaAnterior(Connection con, String fecha,Long idPaisSistema, String diaHabil,String soloCaeDiaHabil) throws Exception {
		logger.trace("INICIO::GenerarNCG380Custodia.obtenerDiaAnterior");
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
		logger.trace("FIN::GenerarNCG380Custodia.obtenerDiaAnterior, tiempo: " + (fin - inicio));
		
		return outObtenerDiaAnterior;
	}
			
}
