package cl.bcs.tesoreria.javacall.kit.colaborador;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;

/**
 * Contract of the legacy cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal as ProcesarDistribucion uses it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract).
 */
public final class CrearSolicitudAporteRescateNormal {

    private CrearSolicitudAporteRescateNormal() {
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
        
        public String toString() {
            return Utils.printJson(this, false);
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
        public String IgnorarMontoMinimo;    // S / N
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
        
        public String toString() {
            return Utils.printJson(this, false);
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
    
    
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    public static class SalidaCrearSolicitudAporteRescateNormal{
        public Long P_OUT_ERRORESCONTADOR;
        public List <MensajeSalida> P_OUT_MENSAJES;
        public List <ResultadoSolicitud> P_OUT_RESULTADO;
        
        public SalidaCrearSolicitudAporteRescateNormal(){
            this.P_OUT_ERRORESCONTADOR=0L;
            this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
            this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
        }
        
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static SalidaCrearSolicitudAporteRescateNormal invocar(Colaboradores colaboradores, EntradaCrearSolicitudAporteRescateNormal entrada) {
        JsonObject r = colaboradores.invocar("CrearSolicitudAporteRescateNormal", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, SalidaCrearSolicitudAporteRescateNormal.class);
    }
}
