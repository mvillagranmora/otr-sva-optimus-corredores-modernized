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
 * Contract of the legacy cl.bcs.cuentaInversion.CrearCargoAbono as ProcesarDistribucion uses it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract).
 */
public final class CrearCargoAbono {

    private CrearCargoAbono() {
    }

    public static class ObjCargoAbonoAGenerar implements Cloneable{
        public Long                    NumeroCargoAbono;
        public Long                    IdPortafolio;
        public String                IdMoneda;
        public String                IdTipoMovimiento;
        public String                IdConcepto;
        public String                Descripcion;
        public String                IdCondicionDeLiquidacion;
        public String                FechaCondicionDeLiquidacion;
        public BigDecimal            Monto; 
        public Long                    IdEjecutivo; 
        public Long                    IdSucursal; 
        public String                IdTipoProducto; 
        public String                IdTipoOperacion; 
        public Long                    IdOperacion; 
        public String                FechaOperacion; 
        public String                FechaLiquidacionOperacion; 
        public String                FechaVencimientoOperacion; 
        public String                IdMonedaOperacion; 
        public BigDecimal            MontoOperacion; 
        public BigDecimal            TipoCambioOperacion; 
        public Long                    IdContraparte; 
        public String                IdTipoComprobante; 
        public Long                    IdComprobante; 
        public String                FolioComprobante; 
        public Long                    IdComprobanteDetalle; 
        public Long                    IdComprobanteFormaDePago; 
        public String                IdTipoMovimientoAsociado; 
        public Long                    IdMovimientoAsociado; 
        public String                FechaComprobante; 
        public Long                    IdPersonaPagoCobro; 
        public Long                    IdInstrumento; 
        public String                IdTipoInstrumento;
        public BigDecimal            Cantidad; 
        public String                Nemo; 
        public String                EsIndicadorBilateral; 
        public String                EsCargoAbonoPorTraspaso; 
        public Long                    IdPortafolioTraspaso; 
        public String                GeneroContraparteEnTesoreria; 
        public String                CertificadoAutomatico;
        
        public String toString() {
            return Utils.printJson(this, false);
        };

        public Object clone(){ Object obj=null; try{ obj=super.clone(); }catch(CloneNotSupportedException ex){ System.out.println(" no se puede duplicar"); } return obj; }
        
    };
    public static class ObjCargoAbonoGenerado {
        public Long            NumeroCargoAbono; 
        public Long            IdCargoAbonoMovimiento; 
        public String        IdTipoMovimiento;         
         
        public String toString() {
            return Utils.printJson(this, false);
        };
    };
    public static class CrearCargoAbonoEntrada{
        public Long                            p_IdEmpresa;
        public Long                            p_IdUsuario;
        public String                        p_IdModulo;
        public String                        p_simularOperacion;
        public String                        p_UsaReglasNegocio;
        public String                        p_FechaSistema;
        public String                        p_EsIngresoManual    = "N";
        public List<ObjCargoAbonoAGenerar>    p_CargoAbonoAGenerar;
        
        public CrearCargoAbonoEntrada (){            
            p_CargoAbonoAGenerar    = new ArrayList<ObjCargoAbonoAGenerar>();
        };
        
        public String toString() {
            return Utils.printJson(this, false);
        };
    };
    public static class CrearCargoAbonoSalida{
        public List<MensajeSalida>            P_OUT_MENSAJES;
        public Long                            P_OUT_ERRORESCONTADOR;
        public List<ObjCargoAbonoGenerado>    P_OUT_CARGOABONOGENERADO;

        public CrearCargoAbonoSalida (){
            P_OUT_MENSAJES                 = new ArrayList<MensajeSalida>();
            P_OUT_CARGOABONOGENERADO    = new ArrayList<ObjCargoAbonoGenerado>();
        };
        
        public String toString() {
            return Utils.printJson(this, false);
        };
    };

    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static CrearCargoAbonoSalida invocar(Colaboradores colaboradores, CrearCargoAbonoEntrada entrada) {
        JsonObject r = colaboradores.invocar("CrearCargoAbono", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, CrearCargoAbonoSalida.class);
    }
}
