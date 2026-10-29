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
 * Contract of the legacy cl.bcs.fondomutuo.CalcularMontoNumeroCuota as ProcesarDistribucion uses it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract).
 */
public final class CalcularMontoNumeroCuota {

    private CalcularMontoNumeroCuota() {
    }

    public static class EntradaCalcularMontoNumeroCuota {
        public Long p_IdEmpresa;
        public List<Serie> p_Series;
        
        public EntradaCalcularMontoNumeroCuota() {
            this.p_Series = new ArrayList<Serie>();
        }
        
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    public static class Serie extends BcsPojo{
        public Long IdInterno;
        public String Fecha;
        public String IdSerieFondo;
         public String CalculoPorMontoCuota;
         public BigDecimal Monto;
         public BigDecimal NumeroCuotas;
         public String IdMonedaFondo;
         public String IdMonedaInversion;
         public String TipoSolicitud;
         public String TipoFactorAjuste;
         public Long IdEmpresa;
         public BigDecimal Porcentaje;
         public String IdTipoMovimientoFondoMutuo;
         
         public Serie() {
             this.IdTipoMovimientoFondoMutuo = "";
         }
         
         
    }
    public static class Resultado{
        public Long IdInterno;
        public String TipoSolicitud;
         public BigDecimal Monto;
         public BigDecimal NumeroCuotas;
         public BigDecimal ValorCuota;
         public BigDecimal MontoDestino;
         public BigDecimal ValorParidad;
         public BigDecimal ValorFactorAjuste;
         public BigDecimal CostoEnFactorAjuste;
         public String IdMonedaSistema;
         public BigDecimal MontoMonedaSistema;
         public String Resultado;
         public String CodigoMensaje;
         public BigDecimal Porcentaje;
         public Long DecimalesValorCuota;
         public Long DecimalesNumeroCuotas;
         public String FechaValorCuota;
         
         public String toString() {
            return Utils.printJson(this, false);
        }
    }
    public static class SalidaCalcularMontoNumeroCuota{
        public List<Resultado> P_OUT_RESULTADO;
        
        public SalidaCalcularMontoNumeroCuota(){
            this.P_OUT_RESULTADO = new ArrayList<Resultado>();
        }
        
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static SalidaCalcularMontoNumeroCuota invocar(Colaboradores colaboradores, EntradaCalcularMontoNumeroCuota entrada) {
        JsonObject r = colaboradores.invocar("CalcularMontoNumeroCuota", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, SalidaCalcularMontoNumeroCuota.class);
    }
}
