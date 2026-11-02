// GENERATED DOUBLE of the legacy cl.bcs.cuentaInversion.CrearCargoAbono by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.utils.BcsPojo;


import com.google.gson.JsonObject;

import cl.bcs.ordenes.CrearOrdenes.RequisitoOperativoCrearOrden;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class CrearCargoAbono implements JavaCall{
	
	public static class ObjCargoAbonoAGenerar implements Cloneable{
		public Long					NumeroCargoAbono;
		public Long					IdPortafolio;
		public String				IdMoneda;
		public String				IdTipoMovimiento;
		public String				IdConcepto;
		public String				Descripcion;
		public String				IdCondicionDeLiquidacion;
		public String				FechaCondicionDeLiquidacion;
		public BigDecimal			Monto; 
		public Long					IdEjecutivo; 
		public Long					IdSucursal; 
		public String				IdTipoProducto; 
		public String				IdTipoOperacion; 
		public Long					IdOperacion; 
		public String				FechaOperacion; 
		public String				FechaLiquidacionOperacion; 
		public String				FechaVencimientoOperacion; 
		public String				IdMonedaOperacion; 
		public BigDecimal			MontoOperacion; 
		public BigDecimal			TipoCambioOperacion; 
		public Long					IdContraparte; 
		public String				IdTipoComprobante; 
		public Long					IdComprobante; 
		public String				FolioComprobante; 
		public Long					IdComprobanteDetalle; 
		public Long					IdComprobanteFormaDePago; 
		public String				IdTipoMovimientoAsociado; 
		public Long					IdMovimientoAsociado; 
		public String				FechaComprobante; 
		public Long					IdPersonaPagoCobro; 
		public Long					IdInstrumento; 
		public String				IdTipoInstrumento;
		public BigDecimal			Cantidad; 
		public String				Nemo; 
		public String				EsIndicadorBilateral; 
		public String				EsCargoAbonoPorTraspaso; 
		public Long					IdPortafolioTraspaso; 
		public String				GeneroContraparteEnTesoreria; 
		public String				CertificadoAutomatico;
		
		public String toString() {
			return Utils.printJson(this, false);
		};

		public Object clone(){ Object obj=null; try{ obj=super.clone(); }catch(CloneNotSupportedException ex){ System.out.println(" no se puede duplicar"); } return obj; }
		
	};
	
	public static class ObjProcesamientoCargoAbonoAGenerar {
		public Long                 IdInterno;
		public Long                 IdCargoAbonoMovimiento;
		public Long					NumeroCargoAbono; 
		public Long					IdPortafolioInformado; 
		public String				IdMonedaInformado; 
		public String				IdTipoMovimientoInformado; 
		public String				IdConceptoInformado; 
		public String				Descripcion; 
		public String				IdCondicionDeLiquidacion; 
		public String				FechaCondicionDeLiquidacion; 
		public BigDecimal			Monto; 
		public Long					IdEjecutivo; 
		public Long					IdSucursal; 
		public String				IdTipoProductoInformado; 
		public String				IdTipoOperacionInformado; 
		public Long					IdOperacion; 
		public String				FechaOperacion; 
		public String				FechaLiquidacionOperacion; 
		public String				FechaVencimientoOperacion; 
		public String				IdMonedaOperacion; 
		public BigDecimal			MontoOperacion; 
		public BigDecimal			TipoCambioOperacion; 
		public Long					IdContraparte; 
		public String				IdTipoComprobante; 
		public Long					IdComprobante; 
		public String				FolioComprobante; 
		public Long					IdComprobanteDetalle; 
		public Long					IdComprobanteFormaDePago; 
		public String				IdTipoMovimientoAsociado; 
		public Long					IdMovimientoAsociado; 
		public String				FechaComprobante; 
		public Long					IdPersonaPagoCobro; 
		public Long					IdInstrumento; 
		public String				IdTipoInstrumentoInformado;
		public BigDecimal			Cantidad; 
		public String				Nemo; 
		public String				EsIndicadorBilateral; 
		public String				EsCargoAbonoPorTraspaso; 
		public Long					IdPortafolioTraspaso; 
		public String				GeneroContraparteEnTesoreria; 
		public String				CertificadoAutomatico;
		public Long                 IdCuentaInversion;
		public Long                 IdPortafolio;
		public String               IdMoneda;
		public String               IdEstado;
		public String               IdTipoMovimiento;
		public String               IdConcepto;
		public String               IdMonedaTesoreria;
		public String               IdEstadoMonedaTesoreria;
		public String 				IdTipoOperacion;
		public String               IdTipoProducto;
		public String               IdTipoInstrumento;
		public String               OperaLineaSobregiro;
		public String               NombreCompleto;
		public long                 KeyTraspaso;
		public long 				errores = 0;
		public long 				advertencias = 0;
		public long 				autorizaciones = 0;
		public long 				ok = 0;
		public String               OperaCuentaInversion;
		

		public String               DocumentoIdentidad;
		

		public String               BloqueadoPorCumplimiento = "N";
		public String 				CodigoPortafoliCargo ="";
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjInsCargoAbono {
        public Long IdInterno;
        public Long IdCargoAbonoMovimiento;
        public Long NumeroCargoAbono;
        public Date FechaSistema;
        public String IdModulo;
        public String IdTipoMovimiento;
        public String EsIngresoManual;
        public Long IdCuentaInversion;
        public String NombreCompleto;
        public String IdConcepto;
        public String Descripcion;
        public String IdCondicionDeLiquidacion;
        public Date FechaCondicionDeLiquidacion;
        public BigDecimal Monto;
        public BigDecimal Saldo;
        public BigDecimal SaldoPorFechaMovimiento;
        public BigDecimal SaldoLibroInventario;
        public String EsCertificadoAutomatico;
        public Timestamp FechaCertificacion;
        public Long IdEjecutivo;
        public Long IdSucursal;
        public String IdTipoComprobante;
        public Long IdComprobante;
        public String FolioComprobante;
        public Long IdComprobanteDetalle;
        public Long IdComprobanteFormaDePago;
        public String IdTipoMovimientoAsociado;
        public Long IdMovimientoAsociado;
        public Date FechaComprobante;
        public String IdTipoProducto;
        public String IdTipoOperacion;
        public Long IdOperacion;
        public Date FechaOperacion;
        public Date FechaLiquidacionOperacion;
        public Date FechaVencimientoOperacion;
        public String IdMonedaOperacion;
        public BigDecimal MontoOperacion;
        public BigDecimal TipoCambioOperacion;
        public Long IdInstrumento;
        public String IdTipoInstrumento;
        public String Nemo;
        public BigDecimal Cantidad;
        public String EsCargoAbonoPorTraspaso;
        public String EsIndicadorBilateral;
        public String GeneroContraparteEnTesoreria;
        public Long IdContraparte;
        public Long IdPersonaPagoCobro;
        public Long IdUsuario;
        public Long IdEmpresa;
        public String IdEstado;
        public Long IdCargoAbonoRelacionadoTraspaso;
        
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjInSumaSaldos {
		public Long IdPortafolio;
		public String IdMoneda;
		public String FechaSaldo;
		public BigDecimal Saldo;		
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjSumaSaldos {
		public Long IdInterno;
		public BigDecimal SaldoFinal;		
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjCargoAbonoGenerado {
		public Long			NumeroCargoAbono; 
		public Long			IdCargoAbonoMovimiento; 
		public String		IdTipoMovimiento; 		
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ObjInformacionDetalle {
		public Long			Obj; 
		public Long			IdCargoAbonoMovimiento; 
		public String		Codigo; 
		public String		Descripcion;
		
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ObjAdvertenciasDetalle {
		public Long			NumeroCargoAbono; 
		public String		Codigo; 
		public String		RequiereAutorizacion; 
		public String		Descripcion; 
		
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ObjErroresDetalle {
		public Long			NumeroCargoAbono; 
		public String		Codigo; 
		public String		Descripcion; 
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class CrearCargoAbonoEntrada{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_simularOperacion;
		public String						p_UsaReglasNegocio;
		public String						p_FechaSistema;
		public String						p_EsIngresoManual	= "N";
		public List<ObjCargoAbonoAGenerar>	p_CargoAbonoAGenerar;
		
		public CrearCargoAbonoEntrada (){			
			p_CargoAbonoAGenerar	= new ArrayList<ObjCargoAbonoAGenerar>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class CrearCargoAbonoSalida{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<ObjCargoAbonoGenerado>	P_OUT_CARGOABONOGENERADO;

		public CrearCargoAbonoSalida (){
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			P_OUT_CARGOABONOGENERADO	= new ArrayList<ObjCargoAbonoGenerado>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ConsultaCuentaPropia_Result{
		public Long IdCuentaInversion;
	}
	
	public static class InformacionCorredora 
	{
	    public String 				Nombre;
	    public Long 				IdPersona;
	    public String				DocumentoIdentidad;
	    
        public String toString() {
			return Utils.printJson(this, false);
		};
	}

	public static class ConceptoOrden extends BcsPojo {
		public String IdConcepto;
	}
	
	public static class ObjCliente extends BcsPojo{
		public Long                 IdCuentaInversion;
		public Long					IdCliente;
	}
	
	public static class ObjComprobanteDetalle extends BcsPojo{
		public String              	IdTipoComprobanteOrigen;
		public Long					IdComprobanteDetalle;
	}

	@SuppressWarnings("rawtypes")
	@Trace
	public CrearCargoAbonoSalida logica(Connection con, CrearCargoAbonoEntrada in) throws Exception { return cl.bcs.oracle.Doubles.answer("CrearCargoAbono", "logica", new Object[]{con, in}, CrearCargoAbonoSalida.class); };
	
	public boolean esCuentaPropia (List<ConsultaCuentaPropia_Result> cuentasPropias, Long idCuenta) { throw new UnsupportedOperationException("oracle double CrearCargoAbono.esCuentaPropia: not part of the double"); }
	
	public List<ObjInsCargoAbono> creaListaCargoAbono (Connection con, CrearCargoAbonoEntrada in ,List<ObjProcesamientoCargoAbonoAGenerar> procesamientoCargoAbono, HashMap<Long,Object> procHash, String fechaSistema ) throws Exception{ throw new UnsupportedOperationException("oracle double CrearCargoAbono.creaListaCargoAbono: not part of the double"); }
	
	public void limpiarCargosAbonos(List<ObjProcesamientoCargoAbonoAGenerar> cargosabonos, List<ObjProcesamientoCargoAbonoAGenerar> cargosabonosConErrores) { throw new UnsupportedOperationException("oracle double CrearCargoAbono.limpiarCargosAbonos: not part of the double"); }
	
	public String obtenerValorRequisitoOperativo(HashMap<String, RequisitoOperativoCrearOrden> requisitos,String IdTipoCanalIngreso,String requisitoOperativo) throws IllegalArgumentException, IllegalAccessException { throw new UnsupportedOperationException("oracle double CrearCargoAbono.obtenerValorRequisitoOperativo: not part of the double"); }
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return cl.bcs.oracle.Doubles.answer("CrearCargoAbono", "call", new Object[]{conn, in}, JsonObject.class); };
	
	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double CrearCargoAbono.main: not part of the double"); };
};