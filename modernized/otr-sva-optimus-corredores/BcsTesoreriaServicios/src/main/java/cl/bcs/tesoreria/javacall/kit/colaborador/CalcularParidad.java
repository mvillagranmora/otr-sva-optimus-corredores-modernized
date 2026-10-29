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
 * Contract of the legacy cl.bcs.plataforma.mer.CalcularParidad as ProcesarDistribucion uses it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract).
 */
public final class CalcularParidad {

    private CalcularParidad() {
    }

    public static class CalcularParidadEntrada {
        public Long p_IdEmpresa;
        public Long p_IdTrabajo;
        public List<CalculoEntrada> p_CalculosEntrada;
        
        public String toString() {
            return Utils.printJson(this, false);
        }
        
        public CalcularParidadEntrada() {
            this.p_CalculosEntrada = new ArrayList<CalculoEntrada>();
        }
    }
    public static class CalculoEntrada {
        public Long IdTrabajo;
        public Long IdInterno;
        public String Fecha;
        public String IdMonedaOrigen;
        public String IdMonedaDestino;
        public BigDecimal ValorCambioLocalModificado = BigDecimal.ZERO;
        public BigDecimal MontoOrigen;
        public String IdUso;
        
        public String toString() {
            return Utils.printJson(this, false);
        }
        
        public CalculoEntrada(){
            this.ValorCambioLocalModificado = BigDecimal.ZERO;
            this.IdUso = "";
        }
    }
    public static class CalculoSalida {
        public Long IdTrabajo;
        public Long IdInterno;
        public java.sql.Date Fecha;
        public String IdMonedaOrigen;
        public String IdMonedaDestino;
        public BigDecimal ValorCambioLocalModificado;
        public BigDecimal MontoOrigen;
        public String IdUso;
        
        public BigDecimal FactorOrigenDestino;
        public BigDecimal MontoDestino;
        public BigDecimal FactorOrigenLocal;
        public BigDecimal MontoLocal;
        
        public BigDecimal FactorOrigenCambio;
        public java.sql.Date FechaOrigenCambio;
        public String IdTipoRegistroOrigenCambio;
        
        public BigDecimal FactorCambioLocal;
        public java.sql.Date FechaCambioLocal;
        public String IdTipoRegistroCambioLocal;
        
        public BigDecimal FactorLocalCambio;
        public java.sql.Date FechaLocalCambio;
        public String IdTipoRegistroLocalCambio;
        
        public BigDecimal FactorCambioDestino;
        public java.sql.Date FechaCambioDestino;
        public String IdTipoRegistroCambioDestino;
        
        public String Resultado;
        public String CodigoMensaje;
        public BigDecimal ValorParametroFinanciero;
        public Date FechaParametroFinanciero;

        public String toString() {
            return Utils.printJson(this, false);
        }
        
        public CalculoSalida(){
            this.FactorOrigenDestino = BigDecimal.ZERO;
            this.FactorOrigenLocal = BigDecimal.ZERO;
            this.FactorOrigenCambio = BigDecimal.ZERO;
            this.IdTipoRegistroOrigenCambio = "";
            this.FactorCambioLocal = BigDecimal.ZERO;
            this.IdTipoRegistroCambioLocal = "";
            this.FactorLocalCambio = BigDecimal.ZERO;
            this.IdTipoRegistroLocalCambio = "";
            this.FactorCambioDestino = BigDecimal.ZERO;
            this.IdTipoRegistroCambioDestino = "";
            this.ValorParametroFinanciero =BigDecimal.ZERO;
        }
    
    }
    public static class CalcularParidadSalida {
        public List<CalculoSalida> p_out_CalculosSalida;
        public Long     P_OUT_ERRORESCONTADOR = 0L;
        
        public CalcularParidadSalida(){
            this.p_out_CalculosSalida = new ArrayList<CalculoSalida>();
        }
        
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static CalcularParidadSalida invocar(Colaboradores colaboradores, CalcularParidadEntrada entrada) {
        JsonObject r = colaboradores.invocar("CalcularParidad", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, CalcularParidadSalida.class);
    }
}
