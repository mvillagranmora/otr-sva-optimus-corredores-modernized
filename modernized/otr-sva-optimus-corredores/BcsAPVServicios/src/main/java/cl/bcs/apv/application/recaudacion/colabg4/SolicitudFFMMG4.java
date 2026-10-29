package cl.bcs.apv.application.recaudacion.colabg4;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Verbatim legacy contract (field names = JSON names) of a collaborator outside APV; copied from the legacy source through the oracle shim. */
public final class SolicitudFFMMG4 {

    private SolicitudFFMMG4() {
    }

    /** Legacy PlataformaUtils.MensajeSalida as it travels in the JSON. */
    public static class Msg {
        public Long id;
        public String tipo;
        public String codigo;
        public String descripcion;
    }

    public static class EntradaCrearSolicitudAporteRescateNormal{
    		public Long p_IdEmpresa;
    		public Long p_IdUsuario;
    		public String p_IdModulo;
    		public String p_SimularOperacion;
    		public String p_UsaReglasNegocio;
    		public String p_IdTipoCanalIngreso;
    		public Long p_NumeroDividendo;
    		public List<InSolicitudesAporteRescate> p_SolicitudesAporteRescate;
    		public List<InFormaPagoSpot> p_FormaPagoSpot;
    		public String p_IdOrigenProceso;  // REC: Recaudacion    PAC: Pago automatico  BONIF: Proceso Gestion Bonificacion  CAR: Carga Masiva Fondos Mutuos
    		public String p_PermiteOperarProspectos;
    		
    		public EntradaCrearSolicitudAporteRescateNormal() {
    			p_IdTipoCanalIngreso = "OPT";
    			p_SolicitudesAporteRescate = new ArrayList<InSolicitudesAporteRescate>();
    			p_FormaPagoSpot = new ArrayList<InFormaPagoSpot>();
    			p_NumeroDividendo = 0L;
    			p_IdOrigenProceso = "OPT";
    			p_PermiteOperarProspectos = "N";
    		}
    }

    public static class InSolicitudesAporteRescate{
        	public Long IdInterno;
        	public Long IdCliente;
        	public Long IdPortafolio;
        	public String TipoSolicitud;
        	public String IdSerieFondo;
        	public String CalculoPorMontoCuota;
        	public String EsPorMontoLiquido;
        	public BigDecimal Monto;
        	public BigDecimal NumeroCuotas;
        	public String IdMoneda;
        	public String IdMedioDePago;
        	public Long IdPreferenciaPagoPortafolio;
        	public String Visado;
        	public String SolicitudBatch;
        	public String OrigenOperacion;
        	public String FechaOperacion;
        	public String IdRegimenTributario;
        	public String Glosa;
        	public String IndicadorMLT;
        	public String IdSerieFondoDestino;
        	public String IdEmisorDestino;
        	public Long IdAutorizadoOrigen;
        	public BigDecimal Porcentaje;
        	public Long FolioContrato;
        	public String IdMonedaPago;
        	public BigDecimal MontoPatrimonio;
        	
        	public String IngresoAutomatico;
        	public String ForzarSpot; //Parametro para forzar spot independiente si tiene saldo en dolar, para distribucion y CSC
        	public String TipoContratoOperacion;
        	public String FolioContratoOperacion;
        	public String EsTraspaso;
        	public String Canal;
        	public Long IdSolicitudAporteRescateRelacionado;//Para solicitudes BF
        	public Date FechaValorCuota;
        	public String IgnorarMontoMinimo;	// S / N
        	public String SimulaCambioPortafolioEjecutadoOk;
        	public String IngresoDesde;
        	public InSolicitudesAporteRescate(){
        		this.IdMonedaPago = "";
        		this.IdEmisorDestino = "";
        		this.MontoPatrimonio = BigDecimal.ZERO;
        		this.IngresoAutomatico = "N";
        		this.ForzarSpot = "N";
        		this.TipoContratoOperacion = "";
        		this.FolioContratoOperacion = "";
        		this.EsTraspaso = "N";
        		this.Canal = "";
        		this.IdSolicitudAporteRescateRelacionado = 0L;
        		this.IgnorarMontoMinimo = "N";
        		this.SimulaCambioPortafolioEjecutadoOk = "N";
        		this.IngresoDesde = "";
        	}
    public Long IdOrigenRecaCliente = 0L;
    	}

    public static class InFormaPagoSpot{
    		
        	public Long IdInternoPagoOperacion;
        	public Long IdComprobantePagoOperacion;
        	public String IdTipoMovimiento;
        	public String IdFormaDePago;
        	public String IdMoneda;
        	public String IdMonedaLiquidacion;
        	public Long IdOperacion;
        	public String RecibePagaPortafolio;
        	public BigDecimal MontoFormaDePago;
        	public String AfectarTesoreria;
    }

    public static class SalidaCrearSolicitudAporteRescateNormal{
    		public Long P_OUT_ERRORESCONTADOR;
    		public List<Msg> P_OUT_MENSAJES;
    		public List <ResultadoSolicitud> P_OUT_RESULTADO;
    		
    		public SalidaCrearSolicitudAporteRescateNormal(){
    			this.P_OUT_ERRORESCONTADOR=0L;
    			this.P_OUT_MENSAJES = new ArrayList<Msg>();
    			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
    		}
    }

    /** Legacy cl.bcs.fondomutuo.pojo.ResultadoSolicitud (only what the callers read). */
    public static class ResultadoSolicitud {
        public Long IdInterno;
        public Long IdSolicitudAporteRescate;
    }


}
