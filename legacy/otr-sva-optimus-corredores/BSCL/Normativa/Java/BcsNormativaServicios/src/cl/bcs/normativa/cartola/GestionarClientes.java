package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.SaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.EntradaSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SalidaSaldoCuentaInversion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GestionarClientes implements JavaCall{
	
	private final Log log = new Log("NOR.GestionarClientes");
	
	private final String SQL_SELECT_PORTAFOLIOS = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarClientes_SelectPortafolios.sql");
	private final String SQL_SELECT_CUENTA_INVERSION = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarClientes_SelectCuentaInversion.sql");
	private final String SELECT_CARTERA_DISPONIBLE = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarClientes_SelectCarteraDisponible.sql");
	private final String SELECT_MOVIMIENTOS_CUENTA_INVERSION = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarClientes_SelectMovimientosCuentaInversion.sql");
	
	private final List<String> CARTOLA_SEMESTRAL = Arrays.asList("06", "12");
	private final List<String> CARTOLA_TRIMESTRAL = Arrays.asList("03", "06", "09", "12");
	
	public static class EntradaGestionarClientes{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public String p_Periodo;
		public String p_IncluirPortafoliosConSaldo;
	    public String p_IncluirPortafoliosConCustodia;
	    public String p_IncluirPortafoliosConMovimientoCuentaInversion;
	    public String p_IncluirCategoriaInversionista;
	    public String p_CategoriaInversionista;
	    public String p_IncluirTipoPersona;
	    public String p_TipoPersona;
	    public String p_IncluirTipoCliente;
	    public String p_TipoCliente;
	    public String p_IncluirPortafoliosExcluidos;
	    public String p_ExcluirPortafoliosIncluidosOtraNomina;
	    public String p_ExcluirOperadoresDirectos;
		
	    public String toString() {
			return Utils.printJson(this, false);
		};
		
	}
	
	public static class SalidaGestionarClientes{
		
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<PortafolioValido> P_OUT_PORTAFOLIOS;
		
		public SalidaGestionarClientes(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_PORTAFOLIOS = new ArrayList<PortafolioValido>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PortafolioValido{
		public Long	IdCliente;
		public Long	IdPortafolio;
		public String Periodicidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera{
		public Long IdPortafolio;
		public BigDecimal CantidadDisponible;
		
		public Cartera(){
			this.CantidadDisponible=BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoCuentaInversion{
		public Long IdPortafolio;
		public Long CantidadMovimientos;
		
		public MovimientoCuentaInversion(){
			this.CantidadMovimientos=0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentaInversion{
		public Long IdCuentaInversion;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaGestionarClientes in = gson.fromJson(jin.toString(), EntradaGestionarClientes.class);
		SalidaGestionarClientes out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGestionarClientes logica(Connection con, EntradaGestionarClientes entrada) throws Exception {
		log.write("INICIO::GestionarClientes");
		List<String> periodosValidos = new ArrayList<>();
		String mes = YearMonth.parse(entrada.p_Periodo).format(DateTimeFormatter.ofPattern("MM"));
		if(CARTOLA_SEMESTRAL.contains(mes))
			periodosValidos.add("SEMESTRAL");
		if(CARTOLA_TRIMESTRAL.contains(mes))
			periodosValidos.add("TRIMESRAL");
				
		SalidaGestionarClientes out = new SalidaGestionarClientes();
		
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IncluirTipoPersona", entrada.p_IncluirTipoPersona);
		params.put("IdTipoPersona", entrada.p_TipoPersona);
		params.put("IncluirCategoriaInversionista", entrada.p_IncluirCategoriaInversionista);
		params.put("IdCategoriaInversionista", entrada.p_CategoriaInversionista);
		params.put("IncluirTipoCliente", entrada.p_IncluirTipoCliente);
		params.put("IdTipoCliente", entrada.p_TipoCliente);
		params.put("ExcluirOperadoresDirectos", entrada.p_ExcluirOperadoresDirectos);
		params.put("ExcluirPortafoliosIncluidosOtraNomina", entrada.p_ExcluirPortafoliosIncluidosOtraNomina);
		params.put("Periodo",entrada.p_Periodo);
		params.put("IncluirPortafoliosExcluidos", entrada.p_IncluirPortafoliosExcluidos);
		List<PortafolioValido> portafoliosIniciales = sql.query(con, SQL_SELECT_PORTAFOLIOS, params, PortafolioValido.class);
		params.clear();
		
		List<Long> portafolios = new ArrayList<Long>();
		
		HashMap<Long,Object> saldosCuentaInversion = new HashMap<Long,Object>();
		HashMap<Long,Object> saldosCustodia = new HashMap<Long,Object>();
		HashMap<Long,Object> movCuentaInversion = new HashMap<Long,Object>();
		List<CuentaInversion> listCuentasInversion = new ArrayList<CuentaInversion>();
		int i = 1;
		for(PortafolioValido portafolio : portafoliosIniciales){
			
			portafolios.add(portafolio.IdPortafolio);
			if(i%5000 == 0 || i == portafoliosIniciales.size()){
				log.write("Gestionando hasta " + i);
				
				if("S".equals(entrada.p_IncluirPortafoliosConSaldo) && portafoliosIniciales.size() > 0){
					params.put("IdPortafolio", portafolios);
					
					listCuentasInversion.addAll(sql.query(con, SQL_SELECT_CUENTA_INVERSION, params, CuentaInversion.class));
					params.clear();
					
					SaldoCuentaInversion saldoCuentaInversion = new SaldoCuentaInversion();
					EntradaSaldoCuentaInversion entradaSaldoCuentaInversion = new EntradaSaldoCuentaInversion();
					entradaSaldoCuentaInversion.p_FechaDesde = entrada.p_FechaDesde;
					entradaSaldoCuentaInversion.p_FechaHasta = entrada.p_FechaHasta;
					entradaSaldoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
					for(Long idPortafolio : portafolios){
						SaldoCuentaInversion.InCuentaInversionConsultar inCuentaInversionConsultar = new SaldoCuentaInversion.InCuentaInversionConsultar();
						inCuentaInversionConsultar.IdPortafolio = idPortafolio;
						entradaSaldoCuentaInversion.p_CuentaInversionAConsultar.add(inCuentaInversionConsultar);
					}
					log.write(entradaSaldoCuentaInversion);
					SalidaSaldoCuentaInversion salidaSaldoCuentaInversion = saldoCuentaInversion.logica(con, entradaSaldoCuentaInversion);
					saldosCuentaInversion.putAll(Utils.List2Hash(salidaSaldoCuentaInversion.P_OUT_SALDOINICIALFINAL, "IdCuentaInversion"));
				}
				
				if("S".equals(entrada.p_IncluirPortafoliosConCustodia) && portafolios.size() > 0){
					params.put("IdPortafolio", portafolios);
					params.put("FechaHasta", entrada.p_FechaHasta);
					List<Cartera> carteras = sql.query(con, SELECT_CARTERA_DISPONIBLE, params, Cartera.class);
					params.clear();
					saldosCustodia.putAll(Utils.List2Hash(carteras, "IdPortafolio"));
				}
				
				if("S".equals(entrada.p_IncluirPortafoliosConMovimientoCuentaInversion) && portafolios.size() > 0){
					params.put("IdPortafolio", portafolios);
					params.put("FechaDesde", entrada.p_FechaDesde);
					params.put("FechaHasta", entrada.p_FechaHasta);
					List<MovimientoCuentaInversion> movimientos = sql.query(con, SELECT_MOVIMIENTOS_CUENTA_INVERSION, params, MovimientoCuentaInversion.class);
					params.clear();
					movCuentaInversion.putAll(Utils.List2Hash(movimientos, "IdPortafolio"));
				}
				portafolios.clear();
			}
			i++;
		}
		
		for(PortafolioValido portafolio : portafoliosIniciales){
			ClienteValido valido = new ClienteValido();

			if("S".equals(entrada.p_IncluirPortafoliosConSaldo)){
				BigDecimal montoSaldo = BigDecimal.ZERO;
				for(CuentaInversion cuentaInversion : listCuentasInversion){
					if(portafolio.IdPortafolio.equals(cuentaInversion.IdPortafolio)){
						SaldoCuentaInversion.SaldoInicialFinal saldo = (SaldoCuentaInversion.SaldoInicialFinal) saldosCuentaInversion.get(cuentaInversion.IdCuentaInversion);
						if(saldo != null && saldo.SaldoMovimientoFinal.compareTo(BigDecimal.ZERO) == 1){
							montoSaldo = saldo.SaldoMovimientoFinal;
							break;
						}
					}
				}
				
				if(montoSaldo.compareTo(BigDecimal.ZERO) != 1) {
					valido.SALDOCTAINV = false;	
				}
			}
			
			if("S".equals(entrada.p_IncluirPortafoliosConCustodia)){
				Cartera cartera = (Cartera) saldosCustodia.get(portafolio.IdPortafolio);
				if(cartera == null || cartera.CantidadDisponible.compareTo(BigDecimal.ZERO) != 1) {
					valido.CUSTODIA = false;
				}
					
			}
			
			if("S".equals(entrada.p_IncluirPortafoliosConMovimientoCuentaInversion)){
				MovimientoCuentaInversion movimiento = (MovimientoCuentaInversion) movCuentaInversion.get(portafolio.IdPortafolio);
				if(movimiento == null || movimiento.CantidadMovimientos.longValue() == 0) {
					valido.MOVIMIENTOS = false;
				}
					
			}

			if( valido.MOVIMIENTOS || valido.CUSTODIA || valido.SALDOCTAINV || periodosValidos.contains(portafolio.Periodicidad)) {
				out.P_OUT_PORTAFOLIOS.add(portafolio);
			}
		}

		log.write("FIN::GestionarClientes");
		
		return out;
	}
	
	/**
	 * Se usa para determinar si el portafolio tiene saldo en la cuenta inversion,
	 * custodia o movimientos al momento de generar la cartola
	 */
	public class ClienteValido{
		public boolean SALDOCTAINV = true;
		public boolean CUSTODIA = true;
		public boolean MOVIMIENTOS = true;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
}
