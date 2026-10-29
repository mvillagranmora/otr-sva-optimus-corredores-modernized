package cl.bcs.apv.application.recaudacion.colabg4;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Verbatim legacy contract (field names = JSON names) of a collaborator outside APV; copied from the legacy source through the oracle shim. */
public final class CuentaInversionG4 {

    private CuentaInversionG4() {
    }

    /** Legacy PlataformaUtils.MensajeSalida as it travels in the JSON. */
    public static class Msg {
        public Long id;
        public String tipo;
        public String codigo;
        public String descripcion;
    }

    public static class ObjCargoAbonoAGenerar {
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
    		
    	}

    public static class ObjCargoAbonoGenerado {
    		public Long			NumeroCargoAbono; 
    		public Long			IdCargoAbonoMovimiento; 
    		public String		IdTipoMovimiento; 		
    
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
    
    	}

    public static class CrearCargoAbonoSalida{
    		public List<Msg>			P_OUT_MENSAJES;
    		public Long							P_OUT_ERRORESCONTADOR;
    		public List<ObjCargoAbonoGenerado>	P_OUT_CARGOABONOGENERADO;
    
    		public CrearCargoAbonoSalida (){
    			P_OUT_MENSAJES 				= new ArrayList<Msg>();
    			P_OUT_CARGOABONOGENERADO	= new ArrayList<ObjCargoAbonoGenerado>();
    		};
    
    	}


}
