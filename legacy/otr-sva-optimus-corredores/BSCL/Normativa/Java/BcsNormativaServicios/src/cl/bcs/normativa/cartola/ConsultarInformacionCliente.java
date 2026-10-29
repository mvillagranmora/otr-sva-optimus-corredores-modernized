package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.SaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.EntradaSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SalidaSaldoCuentaInversion;
import cl.bcs.custodia.ConsultarCustodiaMovimientos;
import cl.bcs.custodia.ConsultarCustodiaMovimientos.EntradaConsultarCustodiaMovimientos;
import cl.bcs.custodia.ConsultarCustodiaMovimientos.SalidaConsultarCustodiaMovimientos;
import cl.bcs.custodia.ConsultarCustodiaPortafolio;
import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.ResumenInversionBase.EntradaResumenInversionBase;
import cl.bcs.normativa.cartola.ResumenInversionBase.OutResumenInversion;
import cl.bcs.normativa.cartola.ResumenInversionBase.SalidaResumenInversionBase;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarInformacionCliente implements JavaCall {
	public final Log log = new Log("NOR.ConsultarInformacionCliente");
	
	private final String SQL_SELECT_MOVIMIENTOS_CAJA = loadResourceToString("cl/bcs/normativa/cartola/sql/ConsultarInformacionCliente_SelectMovimientosCaja.sql");
	private final String SQL_SELECT_MONEDAS = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosCaja_SelectMonedas.sql");
	
	public static class ConsultarInformacionClienteEntrada {
		public Long			p_IdEmpresa;
	    public Long			p_IdUsuario;
	    public String		p_IdModulo;
//	    public String		p_AccionNomina;	//Ingresar, Certificar, Cancelar, Reprocesar, etc
//	    public String		p_TipoProceso;    	
	    public String		p_FechaDesde;
	    public String		p_FechaHasta;
	    public Long	 		p_IdCliente;
	    public Long	 		p_IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarInformacionClienteSalida {
		public boolean 					clienteTieneInformacion;
		public Long 					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		
		public ConsultarInformacionClienteSalida(){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjCliente {
	    public Long	 	IdCliente;
	    public Long	 	IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
    }
	
	public static class Movimiento{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long PosicionRegistro;
		public Long IdCargoAbonoMovimiento;
		public String NombreConcepto;
		public String TipoComprobante;
		public Long IdComprobante;
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public String FechaIngreso;
		public String FechaOperacion;
		public String FechaLiquidacion;
		public String Moneda;
		public String TituloCuentaInversion;
		public BigDecimal Cargo;
		public BigDecimal Abono;
		public BigDecimal Saldo;
		public String IdConcepto;
		public String NombreTipoMovimiento;// nuevo
		public String AfectaRentabilidad;   // nuevo
		public String TipoConcepto;         // nuevo
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MonedaCuentaInversion{
		public String IdMoneda;
		public String Codigo;
		public String Nombre;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ConsultarInformacionClienteSalida logica(Connection con, ConsultarInformacionClienteEntrada entrada) throws Exception {
		ConsultarInformacionClienteSalida out = new ConsultarInformacionClienteSalida();
		out.clienteTieneInformacion = false;
		out.P_OUT_ERRORESCONTADOR = 0L;
		try {
			
			//Valida si cliente tiene saldos
			out.clienteTieneInformacion = getSaldoCliente(con, entrada);
			
			//Valida si cliente tiene Movimientos Caja
			out.clienteTieneInformacion = !out.clienteTieneInformacion ? getMovimientosClienteCaja(con, entrada) : out.clienteTieneInformacion;
			
			//Valida si cliente tiene Movimientos C
			out.clienteTieneInformacion = !out.clienteTieneInformacion ? getMovimientosClienteCustodia(con, entrada) : out.clienteTieneInformacion;
			
			//Valida si cliente tiene custodia
			out.clienteTieneInformacion = !out.clienteTieneInformacion ? getCustodiaCliente(con, entrada) : out.clienteTieneInformacion;
			
			//Valida si cliente tiene resumen inversion
			out.clienteTieneInformacion = !out.clienteTieneInformacion ? getResumenInversion(con, entrada) : out.clienteTieneInformacion;

		}
		catch(Exception e) {
			log.write("== ERROR ==",e);
			out.P_OUT_ERRORESCONTADOR = 1L;
			throw e;
		}		
		return out;
	}
	
	  
	
	private boolean getResumenInversion(Connection con, ConsultarInformacionClienteEntrada entrada) throws Exception {
		ResumenInversionBase resumenInversionBase = new ResumenInversionBase();
		
		EntradaResumenInversionBase entradaResumenInversionBase = new EntradaResumenInversionBase();
		entradaResumenInversionBase.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaResumenInversionBase.p_IdUsuario = entrada.p_IdUsuario;
		entradaResumenInversionBase.p_IdModulo = entrada.p_IdModulo;
		entradaResumenInversionBase.p_FechaDesde = Fecha.toDate(entrada.p_FechaDesde);
		entradaResumenInversionBase.p_FechaHasta = Fecha.toDate(entrada.p_FechaHasta);
		
		Dato datoEntrada = new Dato();
		datoEntrada.IdCliente = entrada.p_IdCliente;
		datoEntrada.IdPortafolio = entrada.p_IdPortafolio;
		
		entradaResumenInversionBase.p_Datos.add(datoEntrada);
		
		SalidaResumenInversionBase salidaResumen = resumenInversionBase.logica(con, entradaResumenInversionBase);
		
		List<OutResumenInversion> salidaConDatos = salidaResumen.p_out_ResumenInversion.parallelStream().filter(resumen-> resumen.MontoMonedaOperacion != null && resumen.MontoMonedaOperacion.compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList());
		
		return salidaConDatos.size() > 0;
		
	}

	private boolean getSaldoCliente(Connection con, ConsultarInformacionClienteEntrada entrada)
			throws Exception {		
		boolean tieneSaldoInicial = false;
		boolean tieneSaldoPorFecha = false;
		boolean tieneSaldoPorMovimiento = false;
		SaldoCuentaInversion saldoCuentaInversion = new SaldoCuentaInversion();
		EntradaSaldoCuentaInversion entradaSaldoCuentaInversion = new EntradaSaldoCuentaInversion();
				
		SaldoCuentaInversion.InCuentaInversionConsultar cuenta = new SaldoCuentaInversion.InCuentaInversionConsultar ();
		cuenta.IdPortafolio = entrada.p_IdPortafolio;
		entradaSaldoCuentaInversion.p_CuentaInversionAConsultar.add(cuenta);

		entradaSaldoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
		Date fechaAnterior = Fecha.addDays(Fecha.toDate(entrada.p_FechaDesde), -1);
		entradaSaldoCuentaInversion.p_FechaDesde = Fecha.stringToSqlDate(fechaAnterior);
		entradaSaldoCuentaInversion.p_FechaHasta = Fecha.stringToSqlDate(fechaAnterior);		
		SalidaSaldoCuentaInversion salidaSaldoCuentaInversion = saldoCuentaInversion.logica(con, entradaSaldoCuentaInversion);
		
		for(SaldoCuentaInversion.SaldoInicialFinal saldo : salidaSaldoCuentaInversion.P_OUT_SALDOINICIALFINAL){
			if((saldo. SaldoEfectivoFinal != null && saldo.SaldoEfectivoFinal.intValue() != 0) || 
					(saldo.SaldoEfectivoInicial != null && saldo.SaldoEfectivoInicial.intValue() != 0) || 
					(saldo.SaldoMovimientoFinal != null && saldo.SaldoMovimientoFinal.intValue() != 0) || 
					(saldo.SaldoMovimientoInicial != null && saldo.SaldoMovimientoInicial.intValue() != 0)) {
				tieneSaldoInicial = true;
				break;
			}
		}
		if(!tieneSaldoInicial) {
			for(SaldoCuentaInversion.SaldoPorFechaEfectiva saldo : salidaSaldoCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA){
				if((saldo.SaldoDisponible != null && saldo.SaldoDisponible.intValue() != 0) || 
						(saldo.MontoMoneyMarket != null && saldo.MontoMoneyMarket.intValue() != 0) || 
						(saldo.Saldo != null && saldo.Saldo.intValue() != 0) || 
						(saldo.SaldoAbonosPendientes != null && saldo.SaldoAbonosPendientes.intValue() != 0) || 
						(saldo.SaldoAPlazoCompromiso != null && saldo.SaldoAPlazoCompromiso.intValue() != 0) || 
						(saldo.SaldoAPlazoSimultanea != null && saldo.SaldoAPlazoSimultanea.intValue() != 0) ||
						(saldo.SaldoAPlazoVentaCorta != null && saldo.SaldoAPlazoVentaCorta.intValue() != 0) || 
						(saldo.SaldoCargosPendientes != null && saldo.SaldoCargosPendientes.intValue() != 0) || 
						(saldo.SaldoEnGarantia != null && saldo.SaldoEnGarantia.intValue() != 0) || 
						(saldo.SaldoRetiro != null && saldo.SaldoRetiro.intValue() != 0) || 
						(saldo.TotalLiquidez != null && saldo.TotalLiquidez.intValue() != 0) ) {
					tieneSaldoPorFecha = true;
					break;
				}
			}
			if(!tieneSaldoPorFecha) {
				for(SaldoCuentaInversion.SaldoPorFechaMovimiento saldo : salidaSaldoCuentaInversion.P_OUT_SALDOPORFECHAMOVIMIENTO){
					if((saldo. SaldoDisponible != null && saldo.SaldoDisponible.intValue() != 0) || 
							(saldo.Saldo != null && saldo.Saldo.intValue() != 0) || 
							(saldo.SaldoAbonosPendientes != null && saldo.SaldoAbonosPendientes.intValue() != 0) || 
							(saldo.SaldoAPlazoCompromiso != null && saldo.SaldoAPlazoCompromiso.intValue() != 0) || 
							(saldo.SaldoAPlazoSimultanea != null && saldo.SaldoAPlazoSimultanea.intValue() != 0) ||
							(saldo.SaldoAPlazoVentaCorta != null && saldo.SaldoAPlazoVentaCorta.intValue() != 0) || 
							(saldo.SaldoCargosPendientes != null && saldo.SaldoCargosPendientes.intValue() != 0) || 
							(saldo.SaldoEnGarantia != null && saldo.SaldoEnGarantia.intValue() != 0) || 
							(saldo.SaldoRetiro != null && saldo.SaldoRetiro.intValue() != 0)) {
						tieneSaldoPorMovimiento = true;
						break;
					}
				}
			}
		}		
		if(tieneSaldoInicial || tieneSaldoPorFecha || tieneSaldoPorMovimiento) {
			return true;
		} else {
			return false;
		}
	};

	private boolean getCustodiaCliente(Connection con, ConsultarInformacionClienteEntrada entrada) 
			throws Exception {
		ConsultarCustodiaPortafolio consultaCustPort = new ConsultarCustodiaPortafolio();
		ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio entradaConsulta = new ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio();
		ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio salidaConsulta = new ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio();
		
		entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsulta.p_IdModulo = entrada.p_IdModulo;
		entradaConsulta.p_IdCliente = entrada.p_IdCliente;
		entradaConsulta.p_IdPortafolio = entrada.p_IdPortafolio;
		entradaConsulta.p_FechaConsulta = entrada.p_FechaHasta;
		entradaConsulta.p_AgrupaPorBoveda = "N";
		entradaConsulta.p_AgrupaPorRelacion = "N";
		entradaConsulta.p_SaldosNegativos = "N";
		entradaConsulta.p_SaldosNegativosPresuntos = "N";
		entradaConsulta.p_TipoFechaConsulta = "ADQ";
		
		salidaConsulta = consultaCustPort.logica(con, entradaConsulta);
		
		if(salidaConsulta == null || salidaConsulta.P_OUT_TBL.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	private boolean getMovimientosClienteCaja(Connection con, ConsultarInformacionClienteEntrada entrada) 
			throws Exception {
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> portafolios = new ArrayList<Long>();
		
		List<MonedaCuentaInversion> monedasCuentaInversion = sql.query(con, SQL_SELECT_MONEDAS, params, MonedaCuentaInversion.class);
		List<String> idMonedas = monedasCuentaInversion.stream().map(moneda -> moneda.IdMoneda).collect(Collectors.toList());
		
		portafolios.add(entrada.p_IdPortafolio);
		params.put("IdPortafolio", portafolios);
		params.put("IdMoneda", idMonedas);
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		
		List<Movimiento> movimientos = sql.query(con, SQL_SELECT_MOVIMIENTOS_CAJA, params, Movimiento.class);
		
		if(movimientos == null || movimientos.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
	
	private boolean getMovimientosClienteCustodia(Connection con, ConsultarInformacionClienteEntrada entrada) 
			throws Exception {
		ConsultarCustodiaMovimientos consulta = new ConsultarCustodiaMovimientos();
		EntradaConsultarCustodiaMovimientos entradaConsulta = new EntradaConsultarCustodiaMovimientos();
		entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsulta.p_IdModulo = entrada.p_IdModulo;
		entradaConsulta.p_FechaInicio = entrada.p_FechaDesde;
		entradaConsulta.p_FechaTermino = entrada.p_FechaHasta;
		ConsultarCustodiaMovimientos.VariableProceso cliente = new ConsultarCustodiaMovimientos.VariableProceso();
		cliente.IdCliente = entrada.p_IdCliente;
		cliente.IdPortafolio = entrada.p_IdPortafolio;
		entradaConsulta.p_VariablesProceso.add(cliente);
		SalidaConsultarCustodiaMovimientos salidaConsulta = consulta.logica(con, entradaConsulta);
		
		if(salidaConsulta == null || salidaConsulta.P_OUT_TABLA.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();	
		ConsultarInformacionClienteEntrada jin=gson.fromJson(in.toString() ,ConsultarInformacionClienteEntrada.class);
		ConsultarInformacionClienteSalida out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":64,\"p_FechaDesde\":\"2019-08-01\",\"p_FechaHasta\":\"2019-08-14\",\"p_IdCliente\":100209,\"p_IdPortafolio\":101011}";
				ConsultarInformacionClienteEntrada Objin = gson.fromJson(json, ConsultarInformacionClienteEntrada.class);
				ConsultarInformacionCliente kk = new ConsultarInformacionCliente();
				kk.logica(con, Objin);
				con.rollback();	
				System.out.println("---- FIN ConsultarInformacionCliente ------");
	    	}catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
