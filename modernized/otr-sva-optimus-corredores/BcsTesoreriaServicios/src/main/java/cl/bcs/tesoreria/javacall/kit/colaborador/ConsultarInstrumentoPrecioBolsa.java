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
 * Contract of the legacy cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa as ProcesarDistribucion uses it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract).
 */
public final class ConsultarInstrumentoPrecioBolsa {

    private ConsultarInstrumentoPrecioBolsa() {
    }

    public static class ConsultarInstrumentoPrecioBolsa_Entrada {
        public Long p_IdEmpresa;
        public Long    p_IdUsuario;
        public String p_SoloInscritos;
        public List<Instrumento> p_Instrumentos;
        public String p_IncluyeNemoNegociacion;
        
        public ConsultarInstrumentoPrecioBolsa_Entrada(){
            p_Instrumentos = new ArrayList<>();
            this.p_SoloInscritos = "N";
        }
        
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    public static class Instrumento{
        public Long IdInterno;
        public Long IdEmpresa;
        public Long IdInstrumento; 
        public String IdBolsa;//oblig
        public String Fecha;//oblig
        public String IdMoneda;
        public String IdClasificacionTipoInstrumento;//oblig    
        public String IdTipoInstrumento;                 
        public String IdCondicionDeLiquidacion;
        public String Nemotecnico;
        
        public Instrumento() {
            
        };
    }
    public static class DatosInstrumentosPrecioBolsa_Salida {
        public Long         IdInterno;
        public Long         IdInstrumento;
        public String         IdBolsa;
        public String         FechaConsulta;
        public String         Fecha;
        public BigDecimal     VolumenTransado;
        public BigDecimal   MontoTransado;
        public BigDecimal     PrecioBase;
        public BigDecimal     PrecioCierre;
        public BigDecimal     PuntaCompra;
        public BigDecimal     PuntaVenta;
        public BigDecimal     Variacion;
        public String         IdMoneda;
        public BigDecimal     PrecioApertura;
        public BigDecimal    PrecioMayor;
        public BigDecimal     PrecioMenor;
        public BigDecimal     PrecioMedio;
        public BigDecimal     PrecioMercado;
        public BigDecimal   TasaMercado;
        public BigDecimal     CantidadPuntaCompra;
        public BigDecimal    CantidadPuntaVenta;
        
        
        public String toString() {
            return Utils.printJson(this, true);
        }
    }
    public static class ConsultarInstrumentoPrecioBolsa_Salida {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long    P_OUT_ERRORESCONTADOR;
        public List<DatosInstrumentosPrecioBolsa_Salida> P_OUT_INSTRUMENTO;
        
        public ConsultarInstrumentoPrecioBolsa_Salida() {
            P_OUT_MENSAJES = new ArrayList<>();
            P_OUT_INSTRUMENTO = new ArrayList<>();
            P_OUT_ERRORESCONTADOR = 0L;
        }
    
        public String toString() {
            return Utils.printJson(this, true);
        }
    }

    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static ConsultarInstrumentoPrecioBolsa_Salida invocar(Colaboradores colaboradores, ConsultarInstrumentoPrecioBolsa_Entrada entrada) {
        JsonObject r = colaboradores.invocar("ConsultarInstrumentoPrecioBolsa", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, ConsultarInstrumentoPrecioBolsa_Salida.class);
    }
}
