package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.cuentaInversion.SaldoCuentaInversion. Only the data classes the reporting classes use are
 * kept, copied VERBATIM from the legacy file (SaldoCuentaInversion.java:72-85 InCuentaInversionConsultar; SaldoCuentaInversion.java:150-160 SaldoInicialFinal; SaldoCuentaInversion.java:162-213 SaldoPorFechaEfectiva; SaldoCuentaInversion.java:215-260 SaldoPorFechaMovimiento). The calculation (SaldoCuentaInversion.logica) belongs to the
 * transactional family and is not part of this oracle.
 */
public class SaldoCuentaInversion implements JavaCall {

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        throw new UnsupportedOperationException("not part of the reportes oracle");
    }

	public static class InCuentaInversionConsultar{
		public Long IdEjecutivo;
		public Long IdPortafolio;
		public String IdMoneda;
		public InCuentaInversionConsultar() {
			
		}
		public InCuentaInversionConsultar(Long idPortafolio) {
			this.IdPortafolio = idPortafolio;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SaldoInicialFinal{
		public Long IdCuentaInversion;
		public BigDecimal SaldoEfectivoInicial;
		public BigDecimal SaldoEfectivoFinal;
		public BigDecimal SaldoMovimientoInicial;
		public BigDecimal SaldoMovimientoFinal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SaldoPorFechaEfectiva{
		public Long IdCuentaInversion;
	  	public String CodigoCuentaInversion;
	  	public Long IdCliente;
	  	public Long IdPortafolio;
	  	public String FechaSaldo;
	  	public String IdMoneda;
	  	public BigDecimal Saldo;
	  	public BigDecimal SaldoDisponible;
	  	public BigDecimal SaldoCargosPendientes;
	  	public BigDecimal SaldoAbonosPendientes;	  	
	  	public Long IdEjecutivo;
		public String NombreEjecutivo;
		public String CodigoEjecutivo;
		public String CodigoSucursal;
		public String NombreSucursal;
     	public String NombreCliente;
     	public String DocumentoIdentidad;
     	public String NombrePortafolio;
     	public String PropositoPortafolio;
     	public String CodigoPortafolio;
     	public String CodigoMoneda;
     	public String NombreMoneda;
     	public BigDecimal SaldoEnGarantia;
     	public BigDecimal SaldoAPlazoSimultanea;
     	public BigDecimal SaldoAPlazoVentaCorta;
     	public BigDecimal SaldoAPlazoCompromiso;
     	public BigDecimal SaldoRetiro;
     	public BigDecimal SaldoRetenido;
     	public String Producto;
     	public BigDecimal MontoTransSolicitudesAporte = BigDecimal.ZERO; //nuevo

     	public Long IdProductoAgrupador;
     	public BigDecimal MontoMoneyMarket;
     	public BigDecimal TotalLiquidez;
	  	public Long IdMatrizAgrupador;
	  	public String EsADC;
	  	public String Relacion;
	  	public String TipoCliente = "";
	  	public String FechaUltimoMovimiento;
	  	public BigDecimal SaldoPendienteLibDAP;
	  	
	  	public SaldoPorFechaEfectiva() {
	  		this.SaldoRetenido = BigDecimal.ZERO;
	  		this.FechaUltimoMovimiento = "0001-01-01";
	  		this.SaldoPendienteLibDAP = BigDecimal.ZERO;
	  	}
	  	
	  	public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SaldoPorFechaMovimiento{
		public Long IdCuentaInversion;
	  	public String CodigoCuentaInversion;
	  	public Long IdCliente;
	  	public Long IdPortafolio;
	  	public String FechaSaldo;
	  	public String IdMoneda;
	  	public BigDecimal Saldo;
	  	public BigDecimal SaldoDisponible;                                              	 
	  	public BigDecimal SaldoCargosPendientes;
	  	public BigDecimal SaldoAbonosPendientes;
	  	public Long IdEjecutivo;
		public String NombreEjecutivo;
		public String CodigoEjecutivo;
		public String CodigoSucursal;
		public String NombreSucursal;
     	public String NombreCliente;
     	public String DocumentoIdentidad;
     	public String NombrePortafolio;
     	public String PropositoPortafolio;
     	public String CodigoPortafolio;
     	public String CodigoMoneda;
     	public String NombreMoneda;
     	public BigDecimal SaldoEnGarantia;
     	public BigDecimal SaldoAPlazoSimultanea;
     	public BigDecimal SaldoAPlazoVentaCorta;
     	public BigDecimal SaldoAPlazoCompromiso;
     	public BigDecimal SaldoRetiro;
     	public BigDecimal SaldoRetenido;
     	public String Producto;

     	public Long IdProductoAgrupador;     	
	  	public Long IdMatrizAgrupador;
	  	public String EsADC;
		public BigDecimal MontoTransSolicitudesAporte = BigDecimal.ZERO; //nuevo
		public BigDecimal SaldoPendienteLibDAP;
		
		public SaldoPorFechaMovimiento() {
	  		this.SaldoRetenido = BigDecimal.ZERO;
	  		this.SaldoPendienteLibDAP = BigDecimal.ZERO;
	  	}
	  	
	  	public String toString() {
			return Utils.printJson(this, false);
		}
	}
}
