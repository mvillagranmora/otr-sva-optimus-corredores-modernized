package cl.bcs.apv.application.recaudacion.colabg4;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Verbatim legacy contract (field names = JSON names) of a collaborator outside APV; copied from the legacy source through the oracle shim. */
public final class InstrumentoPrecioG4 {

    private InstrumentoPrecioG4() {
    }

    /** Legacy PlataformaUtils.MensajeSalida as it travels in the JSON. */
    public static class Msg {
        public Long id;
        public String tipo;
        public String codigo;
        public String descripcion;
    }

    public static class ConsultarInstrumentoPrecioBolsa_Entrada {
    		public Long p_IdEmpresa;
    		public Long	p_IdUsuario;
    		public String p_SoloInscritos;
    		public List<Instrumento> p_Instrumentos;
    		public String p_IncluyeNemoNegociacion;
    		
    		public ConsultarInstrumentoPrecioBolsa_Entrada(){
    			p_Instrumentos = new ArrayList<>();
    			this.p_SoloInscritos = "N";
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
    		public Long 		IdInterno;
    		public Long 		IdInstrumento;
    		public String 		IdBolsa;
    		public String 		FechaConsulta;
    		public String 		Fecha;
    		public BigDecimal 	VolumenTransado;
    		public BigDecimal   MontoTransado;
    		public BigDecimal 	PrecioBase;
    		public BigDecimal 	PrecioCierre;
    		public BigDecimal 	PuntaCompra;
    		public BigDecimal 	PuntaVenta;
    		public BigDecimal 	Variacion;
    		public String 	    IdMoneda;
    		public BigDecimal 	PrecioApertura;
    		public BigDecimal	PrecioMayor;
    		public BigDecimal 	PrecioMenor;
    		public BigDecimal 	PrecioMedio;
    		public BigDecimal 	PrecioMercado;
    		public BigDecimal   TasaMercado;
    		public BigDecimal 	CantidadPuntaCompra;
    		public BigDecimal	CantidadPuntaVenta;
    }

    public static class ConsultarInstrumentoPrecioBolsa_Salida {
    		public List<Msg> P_OUT_MENSAJES;
    		public Long	P_OUT_ERRORESCONTADOR;
    		public List<DatosInstrumentosPrecioBolsa_Salida> P_OUT_INSTRUMENTO;
    		
    		public ConsultarInstrumentoPrecioBolsa_Salida() {
    			P_OUT_MENSAJES = new ArrayList<>();
    			P_OUT_INSTRUMENTO = new ArrayList<>();
    			P_OUT_ERRORESCONTADOR = 0L;
    		}
    }


}
