package cl.bcs.tesoreria.javacall.kit.colaborador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;

/**
 * Contract of the legacy service LiquidarComprobante as the Tesoreria family-B services use it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract); toString is the legacy JSON dump.
 */
public final class LiquidarComprobante {

    private LiquidarComprobante() {
    }

    public static class LiquidarComprobante_Entrada 
    {    
        public Long        p_IdEmpresa;                   
        public Long        p_IdUsuario;                    
        public String     p_IdModulo;                 
        public String     p_SimularOperacion = "N";      
        public String     p_UsaReglasNegocio;            
        public String     p_FechaMovimiento;     // Corresponde a Fecha Sistema    
        
        //Uso exclusivo Aporte CSC
        public String    FolioCSC = "";
        // En SQL se llama "p_TablaComprobante"
        public List<ComprobanteLiquidar_Entrada> p_ComprobanteLiquidar;  // Comprobantes a Liquidar
         
        // En SQL se llama "p_TablaComprobanteFormaDePago" 
        public List<FormaDePagoLiquidar_Entrada> p_FormaDePagoLiquidar;  // Formas de Pago a Liquidar > Si no especifica la FP a liquidar, se asume que se deben liquidar todas las FP de ese comprobante 
                
        public LiquidarComprobante_Entrada()
        {
            p_SimularOperacion    = "N";
            p_ComprobanteLiquidar = new ArrayList<ComprobanteLiquidar_Entrada>();
            p_FormaDePagoLiquidar = new ArrayList<FormaDePagoLiquidar_Entrada>();
        }
        
        public String toString() {
            return Utils.printJson(this, false);
        }
    }    
    public static class ComprobanteLiquidar_Entrada
    {
        public Long     IdComprobante;          
        public String     IdTipoMovimiento;          // MOVING / MOVEGR
        public String     AfectaCuentaInversion;  // CLI: Cliente / COR: Corredora / VACIO: Ambos
    }
    public static class FormaDePagoLiquidar_Entrada
    {
        public Long     IdComprobante;
        public Long     IdComprobanteFormaDePago;
    }    
    public static class LiquidarComprobante_Salida 
    {    
        public List<MensajeSalida>                P_OUT_MENSAJES;
        public Long                                P_OUT_ERRORESCONTADOR;
        public List<ResultadoProceso_Salida>    P_OUT_RESULTADOPROCESO;
        
        public LiquidarComprobante_Salida (){
            P_OUT_MENSAJES                 = new ArrayList<MensajeSalida>();
            P_OUT_RESULTADOPROCESO         = new ArrayList<ResultadoProceso_Salida>();
        };
        
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    public static class ResultadoProceso_Salida
    {
        public String     Campo;        // VARCHAR(40)   -- Ej: "IdComprobante"
        public Long       Id;           // BIGINT        -- IdComprobante liquidado  Ej: 9999
        public String     Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes"
        public String    Resultado;    // VARCHAR(40)   -- Ej: "OK"
        
        public String toString(){
            return Utils.printJson(this, false);
        }
    }


    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static LiquidarComprobante_Salida invocar(Colaboradores colaboradores, LiquidarComprobante_Entrada entrada) {
        JsonObject r = colaboradores.invocar("LiquidarComprobante", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, LiquidarComprobante_Salida.class);
    }
}
