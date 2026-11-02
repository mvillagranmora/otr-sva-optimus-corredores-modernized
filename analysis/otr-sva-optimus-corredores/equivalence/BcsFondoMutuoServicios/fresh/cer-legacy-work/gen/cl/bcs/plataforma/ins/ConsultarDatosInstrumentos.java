// GENERATED DOUBLE of the legacy cl.bcs.plataforma.ins.ConsultarDatosInstrumentos by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.plataforma.ins;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ConsultarDatosInstrumentos implements JavaCall {
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return cl.bcs.oracle.Doubles.answer("ConsultarDatosInstrumentos", "call", new Object[]{conn, in}, JsonObject.class); }
	
	public static class ConsultaInstrumento {
		public Long Correlativo;
		public Long IdInstrumento;	//busca por idinstrumento y luego por nemo
		public String Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FiltroTipoInstrumento {
		public Long Correlativo;
		public String IdTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class FiltroClasificacionTipoInstrumento {
		public Long Correlativo;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ConsultarDatosInstrumento_Entrada {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		
		public String						p_CreaOpcionAutomatica = "N";
		public List<FiltroTipoInstrumento>	p_IdTipoInstrumentos;
		public List<FiltroClasificacionTipoInstrumento>	p_IdClasificacionTipoInstrumentos;
		public List<ConsultaInstrumento>	p_Instrumentos;
		public List<String> p_filtroEstado;
		public int 							p_filtroInstrumentoNegociacion;
		
		public ConsultarDatosInstrumento_Entrada() {
			p_Instrumentos = new ArrayList<>();
			p_IdTipoInstrumentos = new ArrayList<>();
			p_IdClasificacionTipoInstrumentos = new ArrayList<>();
			p_filtroEstado = new ArrayList<>();
			p_filtroInstrumentoNegociacion = 0; // por defecto, no se filtra por instrumento raiz
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarDatosInstrumento_Salida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public List<DatosInstrumento>	P_OUT_INSTRUMENTOS;
		
		public ConsultarDatosInstrumento_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_INSTRUMENTOS = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public ConsultarDatosInstrumento_Salida logica(Connection conn, ConsultarDatosInstrumento_Entrada entrada) throws Exception { return cl.bcs.oracle.Doubles.answer("ConsultarDatosInstrumentos", "logica", new Object[]{conn, entrada}, ConsultarDatosInstrumento_Salida.class); }
	
	public static class BolsaTipoInstrumento {
		public String IdBolsa;
		public String IdTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class BolsaInstrumento {
		public String IdBolsa;
		public Long IdInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosInstrumento {
		public Long Correlativo;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String ISIN;
		public BigDecimal CorteMinimo;
		public String EstaInscrito;
		public String EsDivisible;
		public String RazonSocial;
		public Date FechaEmision;
		public Date FechaVencimiento;
		public Long IdInstrumentoRelacionado;
		public String IdEmisor;
		public String DocumentoIdentidad;
		public String IdTipoReajuste;
		public String Reajustable;
		public String IdMoneda;
		public String IdMonedaEmision;
		public String IdTipoInstrumento;
		public String IdEstado;
		public int DecimalesPrecio;
		public String NombreTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String NombreCompletoEmisor;
		public Long Tamano;
		public String TipoValorizacion;
		public String IndicadorBolson;
		public BigDecimal Tasa;
		public BigDecimal TasaEmision;
		public BigDecimal Precio;
		public String ValorizaPor;
		public String TieneCupon;
		public BigDecimal PorcentajeCastigo;
		public BigDecimal PrecioStrike;
		public String EsPutEsCall;
		public BigDecimal FrecuenciaCupon;
		public String InstrumentoAcreditado;
		public Long IdInstrumentoRaiz;
		public String EsInstrumentoCalificado;
		public Long ConteoNemotecnico;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Instrumento {
		
		public Long IdInstrumento;
		public String Nemotecnico;
		public String ISIN;
		public BigDecimal CorteMinimo;
		public String EstaInscrito;
		public String EsDivisible;
		public String RazonSocial;
		public Date FechaEmision;
		public Date FechaVencimiento;
		public Long IdInstrumentoRelacionado;
		public String IdEmisor;
		public String IdTipoReajuste;
		public String Reajustable;
		public String IdMoneda;
		public String IdMonedaEmision;
		public String IdTipoInstrumento;
		public String IdEstado;
		public String DecimalesPrecio;
		public BigDecimal Tamano;
		public String FechaInicioVigencia;
		public String IndArt104;
		public String TasaFiscal;
		public String TipoValorizacion;
		public String ValorizaPor;
		public BigDecimal Precio;
		public BigDecimal Tasa;
		public String TieneCupon;
		public String NumeroCuotasCirculacionMesActual;
		public String NumeroAccionesCirculacion;
		public String IndicadorLiquidaDCV;
		public String IdPaisEmisor;
		public BigDecimal FrecuenciaCupon;
		public String Serie;
		public BigDecimal PrecioReferencia;
		public String IndicadorBolson;
		public Date FechaReferencia;
		public Integer NumeroCupones;
		public String CodigoRubro;
		public BigDecimal PrecioStrike;
		public String EsPutEsCall;
		public String PactoAndino;
		public String FormadorMercado;
		public String LargoPlazo;
		public String InstrumentoAcreditado;
		public String RelacionadoGrupo;
		public BigDecimal FactorExposicion;
		public String TipoRenta;
		public String OrigenInstrumento;
		public String TipoBonoIRF;
		public String ModalidadLiquidacion;
		public String ModalidadEjercicio;
		public String EstiloOpcion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String[] args) throws Exception  { throw new UnsupportedOperationException("oracle double ConsultarDatosInstrumentos.main: not part of the double"); }
	
}




