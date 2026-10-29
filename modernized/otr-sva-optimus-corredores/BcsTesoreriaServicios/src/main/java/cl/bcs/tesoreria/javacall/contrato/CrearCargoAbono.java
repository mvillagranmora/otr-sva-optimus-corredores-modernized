package cl.bcs.tesoreria.javacall.contrato;

import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON contract (legacy field names, P_OUT_*) of the collaborator service {@code cl.bcs.cuentaInversion.CrearCargoAbono} as the Tesoreria services call it
 * through the {@code Colaboradores} port. The nested classes are the legacy Entrada/Salida classes; their no-argument constructors set the same
 * defaults, so decoding an empty answer with Gson gives the legacy empty Salida.
 */
public final class CrearCargoAbono {

	private CrearCargoAbono() {
	}


	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/CrearCargoAbono.java:72-119 (verbatim)
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
		
	}

	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/CrearCargoAbono.java:273-281 (verbatim)
	public static class ObjCargoAbonoGenerado {
		public Long			NumeroCargoAbono; 
		public Long			IdCargoAbonoMovimiento; 
		public String		IdTipoMovimiento; 		
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	}

	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/CrearCargoAbono.java:283-292 (verbatim)
	public static class ObjInformacionDetalle {
		public Long			Obj; 
		public Long			IdCargoAbonoMovimiento; 
		public String		Codigo; 
		public String		Descripcion;
		
        public String toString() {
			return Utils.printJson(this, false);
		};
	}

	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/CrearCargoAbono.java:293-303 (verbatim)
	public static class ObjAdvertenciasDetalle {
		public Long			NumeroCargoAbono; 
		public String		Codigo; 
		public String		RequiereAutorizacion; 
		public String		Descripcion; 
		
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	}

	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/CrearCargoAbono.java:304-312 (verbatim)
	public static class ObjErroresDetalle {
		public Long			NumeroCargoAbono; 
		public String		Codigo; 
		public String		Descripcion; 
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	}

	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/CrearCargoAbono.java:314-331 (verbatim)
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
	}

	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/CrearCargoAbono.java:333-346 (verbatim)
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
	}
}
